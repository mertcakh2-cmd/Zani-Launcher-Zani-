#include <errno.h>
#include <stdio.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/types.h>

#if defined(__GNUC__)
extern int zani_rust_validate_version(const char *version) __attribute__((weak));
extern const char *zani_rust_core_version(void) __attribute__((weak));
#endif

static int valid_version_fallback(const char *version) {
    if (version == NULL || version[0] == '\0' || strlen(version) > 32) return 0;
    for (const unsigned char *p = (const unsigned char *)version; *p; ++p) {
        if (!( (*p >= 'a' && *p <= 'z') || (*p >= 'A' && *p <= 'Z') ||
               (*p >= '0' && *p <= '9') || *p == '.' || *p == '_' || *p == '-')) return 0;
    }
    return 1;
}

int zani_rust_available(void) {
#if defined(__GNUC__)
    return zani_rust_core_version != NULL;
#else
    return 0;
#endif
}

int zani_validate_version(const char *version) {
#if defined(__GNUC__)
    if (zani_rust_validate_version != NULL) return zani_rust_validate_version(version);
#endif
    return valid_version_fallback(version);
}

const char *zani_native_message(void) {
    return zani_rust_available()
        ? "C JNI bridge connected to the Rust core."
        : "C JNI bridge loaded; Rust core is not built for this ABI yet.";
}

static int make_directory(const char *path, char *error, size_t error_size) {
    if (mkdir(path, 0700) == 0 || errno == EEXIST) {
        struct stat info;
        if (stat(path, &info) == 0 && S_ISDIR(info.st_mode)) return 0;
        snprintf(error, error_size, "A game folder path exists but is not a directory: %.180s", path);
        return -1;
    }
    snprintf(error, error_size, "Cannot create game folder (error %d): %.180s", errno, path);
    return -1;
}

int zani_prepare_game_home(const char *path, char *error, size_t error_size) {
    static const char *folders[] = {
        "versions", "assets", "assets/indexes", "assets/objects", "libraries",
        "runtime", "logs", "mods", "config"
    };
    if (path == NULL || path[0] != '/' || strlen(path) > 300 || strstr(path, "/../") != NULL ||
        strcmp(path, "/..") == 0 || error == NULL || error_size == 0) {
        if (error != NULL && error_size > 0) snprintf(error, error_size, "Invalid or unsafe Minecraft folder path.");
        return -1;
    }
    if (make_directory(path, error, error_size) != 0) return -1;
    for (size_t i = 0; i < sizeof(folders) / sizeof(folders[0]); ++i) {
        char child[512];
        int written = snprintf(child, sizeof(child), "%s/%s", path, folders[i]);
        if (written < 0 || (size_t)written >= sizeof(child) || make_directory(child, error, error_size) != 0) return -1;
    }
    if (error_size > 0) error[0] = '\0';
    return 0;
}
