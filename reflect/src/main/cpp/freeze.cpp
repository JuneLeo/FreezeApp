//
// Created by juneleo on 2025/2/6.
//
#include "jni.h"
#include "freeze.h"
#include "sys/system_properties.h"
#include <android/log.h>
#include <dlfcn.h>
#include <android/dlext.h>

#define LOGV(...)  ((void)__android_log_print(ANDROID_LOG_INFO, "FreezeReflect", __VA_ARGS__))

template<typename T>
int findOffset(void *start, int regionStart, int regionEnd, T value) {

    if (NULL == start || regionEnd <= 0 || regionStart < 0) {
        return -1;
    }
    char *c_start = (char *) start;

    for (int i = regionStart; i < regionEnd; i += 4) {
        T *current_value = (T *) (c_start + i);
        if (value == *current_value) {
            LOGV("found offset: %d", i);
            return i;
        }
    }
    return -2;
}

template<typename Runtime>
int unseal0(Runtime *partialRuntime) {
    bool is_native_debuggable = partialRuntime->is_native_debuggable_;
    bool safe_mode = partialRuntime->safe_mode_;

    // TODO validate

    LOGV("is_native_debuggable: %d, safe_mode: %d",
         is_native_debuggable, safe_mode);
    LOGV("hidden api policy before : %d", partialRuntime->hidden_api_policy_);
    LOGV("fingerprint: %s", partialRuntime->fingerprint_.c_str());

    partialRuntime->hidden_api_policy_ = EnforcementPolicy::kNoChecks;
    LOGV("hidden api policy after: %d", partialRuntime->hidden_api_policy_);
    return 0;
}

int unseal(JNIEnv *env, jint targetSdkVersion) {

    char api_level_str[5];
    char preview_api_str[5];
    __system_property_get("ro.build.version.sdk", api_level_str);
    __system_property_get("ro.build.version.preview_sdk", preview_api_str);

    int api_level = atoi(api_level_str);
    bool is_preview = atoi(preview_api_str) > 0;
    bool isAndroidR = api_level >= 30 || (api_level == 29 && is_preview);

    JavaVM *javaVM;
    env->GetJavaVM(&javaVM);

    JavaVMExt *javaVMExt = (JavaVMExt *) javaVM;
    void *runtime = javaVMExt->runtime;


    LOGV("runtime ptr: %p, vmExtPtr: %p", runtime, javaVMExt);

    const int MAX = 2000;
    int offsetOfVmExt = findOffset(runtime, 0, MAX, (size_t) javaVMExt);
    LOGV("offsetOfVmExt: %d", offsetOfVmExt);

    if (offsetOfVmExt < 0) {
        return -1;
    }


    int startOffset = offsetOfVmExt;
    if (isAndroidR) {
        startOffset += 200;
    }

    int targetSdkVersionOffset = findOffset(runtime, startOffset, MAX, targetSdkVersion);
    LOGV("target: %d", targetSdkVersionOffset);

    if (targetSdkVersionOffset < 0) {
        return -2;
    }

    if (api_level >= 35) {
        auto *partialRuntime = reinterpret_cast<PartialRuntime35 *>((char *) runtime +
                                                                    targetSdkVersionOffset);
        unseal0<PartialRuntime35>(partialRuntime);
    } else if (api_level >= 34) {
        auto *partialRuntime = reinterpret_cast<PartialRuntime34 *>((char *) runtime +
                                                                    targetSdkVersionOffset);
        unseal0<PartialRuntime34>(partialRuntime);

    } else if (isAndroidR) {
        auto *partialRuntime = reinterpret_cast<PartialRuntimeR *>((char *) runtime +
                                                                   targetSdkVersionOffset);
        unseal0<PartialRuntimeR>(partialRuntime);
    } else {
        auto *partialRuntime = (PartialRuntime *) ((char *) runtime +
                                                   targetSdkVersionOffset);
        unseal0<PartialRuntime>(partialRuntime);
    }

    return 0;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_john_reflect_NativeFreeze_nativeInit(JNIEnv *env, jclass clazz, jint targetSdkVersion) {

    unseal(env, targetSdkVersion);
}