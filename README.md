# Zani Launcher

Zani Launcher is an early, open-source Android project for a Minecraft: Java Edition launcher. It aims to grow into a capable launcher with a polished Android interface and a native core. **This repository is currently a starter scaffold, not a playable Minecraft launcher.**

## What is here

- **Kotlin** Android app shell with a dark launcher dashboard and a selectable version label.
- **C and C++** Android NDK library exposed to Kotlin through a small JNI bridge.
- **Rust** platform-independent core crate with version-ID validation and unit tests.
- **Makefile** convenience targets for native and Rust development.

The Play button deliberately reports that game execution is not implemented. The current version picker is UI-only; it does not download Minecraft. Authentication, asset/library downloads, Java runtime provisioning, mod loaders, and actual game launch are future work.

## Open in Android Studio

1. Install Android Studio with Android SDK Platform 35, Android SDK Build-Tools, NDK, and CMake 3.22.1.
2. Open this repository as a Gradle project and allow Gradle sync to finish.
3. Run the `app` configuration on an Android 8.0+ device or emulator (API 26 or newer).

The first Gradle sync may require an internet connection to fetch the Android Gradle Plugin and Kotlin plugin. A Gradle wrapper is not included yet; Android Studio can import the project, or you can add a wrapper with a locally installed Gradle distribution.

## Native development

```sh
cargo test --manifest-path native/rust/Cargo.toml
make native-build
```

The standalone `make native-build` target builds the C/C++ sources on a host when a C/C++ compiler and CMake are installed. For Android ABI builds, use the app's Gradle `externalNativeBuild` configuration so CMake receives the Android NDK toolchain.

## Roadmap

1. Add secure Microsoft account authentication and account/session storage.
2. Implement version metadata resolution and resumable downloads with integrity checks.
3. Provision a compatible Java runtime and map required native libraries per Android ABI.
4. Implement sandboxed game directory management, JVM argument generation, and process output handling.
5. Integrate and test the Rust core with Android using a documented JNI/UniFFI boundary.
6. Add profile management, performance settings, accessibility, and device compatibility testing.

## Legal and safety notes

This project is not affiliated with, endorsed by, or sponsored by Mojang or Microsoft. Minecraft is a trademark of Mojang Synergies AB. Use an account and game files you are authorized to use; this project does not provide game files or bypass authentication/licensing. Do not enter account credentials into this starter app: account sign-in is not implemented.

## License

See [LICENSE](LICENSE).
