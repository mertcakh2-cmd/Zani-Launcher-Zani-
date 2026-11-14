//! Small launcher primitives shared across Android ABIs through a C-compatible FFI.
//! This crate validates identifiers and describes the on-device Minecraft directory layout;
//! it does not redistribute game files or replace Mojang's Java runtime/game libraries.

use std::ffi::CStr;
use std::os::raw::c_char;

/// Returns whether a version identifier is safe to use as one path component.
pub fn is_valid_version_id(version: &str) -> bool {
    !version.is_empty()
        && version.len() <= 32
        && version
            .bytes()
            .all(|byte| byte.is_ascii_alphanumeric() || matches!(byte, b'.' | b'_' | b'-'))
}

/// The launcher-managed game directory's conventional top-level folders.
pub const GAME_HOME_DIRECTORIES: &[&str] = &[
    "versions", "assets", "assets/indexes", "assets/objects", "libraries", "runtime",
    "logs", "mods", "config",
];

/// C ABI: validate a UTF-8 version id. Returns 1 for valid, 0 otherwise.
/// A null or invalid UTF-8 pointer is rejected.
#[no_mangle]
pub unsafe extern "C" fn zani_rust_validate_version(version: *const c_char) -> i32 {
    if version.is_null() {
        return 0;
    }
    CStr::from_ptr(version)
        .to_str()
        .map(is_valid_version_id)
        .unwrap_or(false) as i32
}

/// Identifies the Rust FFI version for the C/JNI status screen.
#[no_mangle]
pub extern "C" fn zani_rust_core_version() -> *const c_char {
    b"zani-core/0.2.0\0".as_ptr().cast()
}

#[cfg(test)]
mod tests {
    use super::{is_valid_version_id, GAME_HOME_DIRECTORIES};

    #[test]
    fn accepts_common_release_ids() {
        assert!(is_valid_version_id("1.21.1"));
        assert!(is_valid_version_id("snapshot_24w14a"));
    }

    #[test]
    fn rejects_empty_or_path_like_ids() {
        assert!(!is_valid_version_id(""));
        assert!(!is_valid_version_id("../outside"));
        assert!(!is_valid_version_id("x".repeat(33).as_str()));
    }

    #[test]
    fn game_home_contains_assets_and_version_directories() {
        assert!(GAME_HOME_DIRECTORIES.contains(&"assets/objects"));
        assert!(GAME_HOME_DIRECTORIES.contains(&"versions"));
    }
}
