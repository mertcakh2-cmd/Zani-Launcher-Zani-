//! Small, platform-independent launcher-core primitives.
//! Android integration is intentionally a later milestone; this crate currently builds and tests on its own.

/// Returns a basic validation result for a Minecraft version identifier.
pub fn is_valid_version_id(version: &str) -> bool {
    !version.is_empty()
        && version.len() <= 32
        && version
            .bytes()
            .all(|byte| byte.is_ascii_alphanumeric() || matches!(byte, b'.' | b'_' | b'-'))
}

#[cfg(test)]
mod tests {
    use super::is_valid_version_id;

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
}
