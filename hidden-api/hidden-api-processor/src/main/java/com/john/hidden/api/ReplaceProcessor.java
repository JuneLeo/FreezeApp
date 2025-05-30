package com.john.hidden.api;

import com.google.auto.service.AutoService;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.tools.FileObject;
import com.john.hidden.api.Replace;
import com.john.hidden.api.Constants;
import com.john.hidden.api.Descriptor;

@AutoService(Processor.class)
public class ReplaceProcessor extends AbstractProcessor {

    public ReplaceProcessor() {
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(Replace.class.getName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_11;
    }

    private static String resolveClassName(final Element element) {
        final Element enclosing = element.getEnclosingElement();

        if (enclosing instanceof TypeElement) {
            return resolveClassName(enclosing) + "$" + element.getSimpleName();
        } else if (enclosing instanceof PackageElement) {
            return ((PackageElement) enclosing).getQualifiedName() + "." + element.getSimpleName();
        }

        return element.getSimpleName().toString();
    }

    private void writeRefineMetadata(final String from, final String to, final Element... dependencies) throws IOException {
        final String metadataName = from + "$" + Constants.REFINE_METADATA_CLASS_NAME;
        final String refineToAnnotation = Constants.REFINE_NS_PACKAGE + "." + to;

        final ClassWriter metadataWriter = new ClassWriter(0);
        metadataWriter.visit(
                Opcodes.V1_8,
                Opcodes.ACC_FINAL | Opcodes.ACC_PRIVATE | Opcodes.ACC_SUPER,
                metadataName.replace('.', '/'),
                null,
                Type.getInternalName(Object.class),
                null
        );

        final AnnotationVisitor descriptor = metadataWriter.visitAnnotation(Type.getDescriptor(Descriptor.class), false);
        descriptor.visit("from", Type.getType("L" + from.replace('.', '/') + ';'));
        descriptor.visit("to", Type.getType("L" + to.replace('.', '/') + ";"));
        descriptor.visitEnd();

        metadataWriter.visitAnnotation("L" + refineToAnnotation.replace('.', '/') + ";", false).visitEnd();
        metadataWriter.visitEnd();

        final FileObject metadataFile = processingEnv.getFiler().createClassFile(metadataName, dependencies);
        try (final OutputStream stream = metadataFile.openOutputStream()) {
            stream.write(metadataWriter.toByteArray());
        }

        if (processingEnv.getElementUtils().getTypeElement(to.replace('$', '.')) == null) {
            final ClassWriter stubWriter = new ClassWriter(0);
            stubWriter.visit(
                    Opcodes.V1_8,
                    Opcodes.ACC_FINAL | Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER,
                    to.replace('.', '/'),
                    null,
                    Type.getInternalName(Object.class),
                    null
            );
            stubWriter.visitEnd();

            final FileObject stubFile = processingEnv.getFiler().createClassFile(to, dependencies);
            try (final OutputStream stream = stubFile.openOutputStream()) {
                stream.write(stubWriter.toByteArray());
            }
        }
    }

    private void processElement(final TypeElement element, final String toClass) throws IOException {
        final String fromClass = resolveClassName(element);

        writeRefineMetadata(fromClass, toClass, element);

        for (final Element enclosedElement : element.getEnclosedElements()) {
            if (!(enclosedElement instanceof TypeElement)) {
                continue;
            }

            if (enclosedElement.getAnnotation(Replace.class) != null) {
                continue;
            }

            processElement((TypeElement) enclosedElement, toClass + "$" + enclosedElement.getSimpleName());
        }
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        try {
            final TypeElement eRefineAs = processingEnv.getElementUtils().getTypeElement(Replace.class.getName());
            final ExecutableElement eRefineAsValue = (ExecutableElement) eRefineAs.getEnclosedElements().stream()
                    .filter(e -> (e instanceof ExecutableElement) && e.getSimpleName().contentEquals("value"))
                    .findAny()
                    .orElseThrow(() -> new IllegalStateException("Invalid @RefineAs annotation."));

            for (final Element element : roundEnv.getElementsAnnotatedWith(eRefineAs)) {
                if (!(element instanceof TypeElement)) {
                    continue;
                }

                final AnnotationMirror refineAs = element.getAnnotationMirrors().stream()
                        .filter(a -> eRefineAs.equals(a.getAnnotationType().asElement()))
                        .findAny()
                        .orElse(null);
                if (refineAs == null) {
                    continue;
                }

                final AnnotationValue refineAsValue = refineAs.getElementValues().get(eRefineAsValue);
                if (refineAsValue == null) {
                    continue;
                }

                processElement((TypeElement) element, resolveClassName(((DeclaredType) refineAsValue.getValue()).asElement()));
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        return true;
    }


}