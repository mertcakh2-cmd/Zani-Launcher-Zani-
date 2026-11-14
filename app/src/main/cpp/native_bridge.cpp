#include <jni.h>

extern "C" const char *zani_native_message(void);

extern "C" JNIEXPORT jstring JNICALL
Java_com_zani_launcher_myapp_MainActivity_nativeStatus(JNIEnv *env, jobject /* this */) {
    return env->NewStringUTF(zani_native_message());
}
