CMAKE ?= cmake
BUILD_DIR ?= build/native

.PHONY: help rust-test rust-check native-configure native-build clean

help:
	@echo "Targets: rust-test, rust-check, native-configure, native-build, clean"

rust-test:
	cargo test --manifest-path native/rust/Cargo.toml

rust-check:
	cargo check --manifest-path native/rust/Cargo.toml

native-configure:
	$(CMAKE) -S app/src/main/cpp -B $(BUILD_DIR)

native-build: native-configure
	$(CMAKE) --build $(BUILD_DIR)

clean:
	rm -rf $(BUILD_DIR) native/rust/target
