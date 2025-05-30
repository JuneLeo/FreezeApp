package com.john.hidden.api;

import com.android.build.api.instrumentation.InstrumentationScope;
import com.android.build.api.variant.AndroidComponentsExtension;
import com.android.build.api.variant.Component;
import kotlin.Unit;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import javax.annotation.Nonnull;

/**
 * Gradle plugin that do the class rename works.
 */
@SuppressWarnings("unused")
public class ReplacePlugin implements Plugin<Project> {
    @Override
    public void apply(@Nonnull final Project target) {
        if (!target.getPlugins().hasPlugin("com.android.base")) {
            throw new GradleException("This plugin must be applied after `com.android.application` or `com.android.library`.");
        }

        final AndroidComponentsExtension<?, ?, ?> components = target.getExtensions().getByType(AndroidComponentsExtension.class);
        boolean isLibrary = components instanceof com.android.build.api.variant.LibraryAndroidComponentsExtension;
        components.onVariants(components.selector().all(), variant -> {
            for (final Component component : variant.getComponents()) {
                component.getInstrumentation().transformClassesWith(
                        ReplaceFactory.class,
                        isLibrary ? InstrumentationScope.PROJECT : InstrumentationScope.ALL,
                        (parameters) -> Unit.INSTANCE
                );
            }
        });
    }
}