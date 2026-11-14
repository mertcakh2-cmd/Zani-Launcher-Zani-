#!/usr/bin/env bash
set -euo pipefail

# Build Rust static libraries that app/src/main/cpp/CMakeLists.txt links per ABI.
# Android NDK must be installed and ANDROID_NDK_HOME (or ANDROID_NDK_ROOT) set.
NDK="${ANDROID_NDK_HOME:-${ANDROID_NDK_ROOT:-}}"
if [[ -z "$NDK" || ! -d "$NDK" ]]; then
  echo "Set ANDROID_NDK_HOME to your Android NDK installation." >&2
  exit 1
fi

for target in aarch64-linux-android armv7-linux-androideabi x86_64-linux-android; do
  rustup target add "$target"
done

HOST_TAG="$(uname -s | tr '[:upper:]' '[:lower:]')-$(uname -m)"
TOOLCHAIN="$NDK/toolchains/llvm/prebuilt/$HOST_TAG/bin"
if [[ ! -d "$TOOLCHAIN" ]]; then
  echo "Could not find NDK toolchain: $TOOLCHAIN" >&2
  exit 1
fi
export CARGO_TARGET_AARCH64_LINUX_ANDROID_LINKER="$TOOLCHAIN/aarch64-linux-android21-clang"
export CARGO_TARGET_ARMV7_LINUX_ANDROIDEABI_LINKER="$TOOLCHAIN/armv7a-linux-androideabi21-clang"
export CARGO_TARGET_X86_64_LINUX_ANDROID_LINKER="$TOOLCHAIN/x86_64-linux-android21-clang"

for target in aarch64-linux-android armv7-linux-androideabi x86_64-linux-android; do
  cargo build --manifest-path native/rust/Cargo.toml --release --target "$target"
done

echo "Rust core built. Now build/run the Android app; CMake will link the matching ABI archive."
