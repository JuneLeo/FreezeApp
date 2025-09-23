//
// Created by juneleo on 2025/9/18.
//


#include <jni.h>
#include "freeze.h"

extern "C"
JNIEXPORT void JNICALL
Java_com_john_reflect_NativeFreeze_nativeInit(JNIEnv *env, jclass clazz, jint targetSdkVersion) {
    unseal(env, targetSdkVersion);
}
