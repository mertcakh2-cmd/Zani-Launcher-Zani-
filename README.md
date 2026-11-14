# Zani Launcher

Zani Launcher is an early Android launcher project for Minecraft: Java Edition. This iteration adds a genuine Rust static library and a C/JNI game-directory manager. The launcher creates an app-private `files/minecraft/` workspace with conventional folders for `versions`, `assets`, `libraries`, `runtime`, `logs`, `mods`, and `config`.

## What works now

- Kotlin Android dashboard and version selector.
- C JNI bridge to initialize and validate the on-device game directory.
- Rust core crate with a C ABI for version-ID validation; Android CMake links it when its ABI archive has been built.
- Rust and native C validation fallbacks, with unit tests and a Rust Android build script.

The app does **not** yet download/install Minecraft, authenticate Microsoft accounts, provision a Java runtime, resolve version manifests/libraries, or start a game process. Creating folders is not equivalent to having Minecraft assets or JARs. The app does not bundle Mojang/Microsoft game files or Java; use only game files and accounts you are authorized to use. The PLAY control must not be treated as a working game launcher until the missing authenticated installation and Android runtime pipeline is implemented and tested.

## Build

1. Open this repository in Android Studio with Android SDK Platform 35, NDK, and CMake 3.22.1 installed.
2. For the C/JNI-only build, sync and run the `app` configuration. The app creates `files/minecraft/` on startup.
3. To compile and link the Rust core for Android, install Rust and set `ANDROID_NDK_HOME` to your NDK, then run:

   ```sh
   ./scripts/build-rust-android.sh
   ```

   Install the Rust targets and rebuild the app whenever the Rust source changes. The script builds `arm64-v8a`, `armeabi-v7a`, and `x86_64` archives; CMake reports whether the Rust archive was found for the selected ABI.
4. Run core tests with `cargo test --manifest-path native/rust/Cargo.toml`. Build the host C library with `make native-build`.

## Game directory layout

The app's internal workspace is `Context.getFilesDir()/minecraft`. The native manager initializes these directories:

```text
minecraft/
├── versions/       # version metadata and version JARs (not downloaded yet)
├── assets/
│   ├── indexes/
│   └── objects/
├── libraries/      # version-specific libraries (not downloaded yet)
├── runtime/        # compatible Java runtime (not provisioned yet)
├── logs/
├── mods/
└── config/
```

Android keeps this app-private directory sandboxed. No proprietary assets or JARs are checked into this repository.

## Inspiration and licensing

The project takes high-level product inspiration from [Zalith Launcher 2](https://github.com/ZalithLauncher/ZalithLauncher2), an Android Minecraft: Java Edition launcher. No Zalith source code or assets are copied here. Zalith Launcher 2 is GPL-3.0; review its license before adapting or copying any of its implementation. This project remains governed by the repository's own [LICENSE](LICENSE).

This project is not affiliated with Mojang or Microsoft. Minecraft is a trademark of Mojang Synergies AB.
