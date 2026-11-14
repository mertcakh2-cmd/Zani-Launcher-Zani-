#include <jni.h>
#include <stdlib.h>

extern const char *zani_native_message(void);
extern int zani_validate_version(const char *version);
extern int zani_rust_available(void);
extern int zani_prepare_game_home(const char *path, char *error, size_t error_size);

JNIEXPORT jstring JNICALL
Java_com_zani_launcher_myapp_MainActivity_nativeStatus(JNIEnv *env, jobject thiz) {
    (void)thiz;
    return (*env)->NewStringUTF(env, zani_native_message());
}

JNIEXPORT jboolean JNICALL
Java_com_zani_launcher_myapp_MainActivity_nativeRustAvailable(JNIEnv *env, jobject thiz) {
    (void)env;
    (void)thiz;
    return zani_rust_available() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_zani_launcher_myapp_MainActivity_nativeValidateVersion(JNIEnv *env, jobject thiz, jstring value) {
    (void)thiz;
    if (value == NULL) return JNI_FALSE;
    const char *version = (*env)->GetStringUTFChars(env, value, NULL);
    if (version == NULL) return JNI_FALSE;
    int valid = zani_validate_version(version);
    (*env)->ReleaseStringUTFChars(env, value, version);
    return valid ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jstring JNICALL
Java_com_zani_launcher_myapp_MainActivity_nativePrepareGameHome(JNIEnv *env, jobject thiz, jstring path) {
    (void)thiz;
    if (path == NULL) return (*env)->NewStringUTF(env, "Minecraft folder path is missing.");
    const char *root = (*env)->GetStringUTFChars(env, path, NULL);
    if (root == NULL) return (*env)->NewStringUTF(env, "Could not read Minecraft folder path.");
    char error[256] = {0};
    int result = zani_prepare_game_home(root, error, sizeof(error));
    (*env)->ReleaseStringUTFChars(env, path, root);
    return (*env)->NewStringUTF(env, result == 0 ? "Minecraft folder structure is ready." : error);
}
