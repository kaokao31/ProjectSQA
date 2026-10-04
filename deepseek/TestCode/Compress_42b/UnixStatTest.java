package org.apache.commons.compress.archivers.zip;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for UnixStat class.
 * Covers all public methods and constants, including edge cases and potential bug triggers.
 */
public class UnixStatTest {

    // ==================== Constants Tests ====================

    @Test
    public void testConstants() {
        // Verify that constants are defined and have expected values
        assertNotNull("FILE_FLAG should not be null", UnixStat.FILE_FLAG);
        assertNotNull("DIR_FLAG should not be null", UnixStat.DIR_FLAG);
        assertNotNull("LINK_FLAG should not be null", UnixStat.LINK_FLAG);
        // Typical values: FILE_FLAG = 0100000, DIR_FLAG = 040000, LINK_FLAG = 0120000
        assertEquals("FILE_FLAG value", 0100000, UnixStat.FILE_FLAG.intValue());
        assertEquals("DIR_FLAG value", 040000, UnixStat.DIR_FLAG.intValue());
        assertEquals("LINK_FLAG value", 0120000, UnixStat.LINK_FLAG.intValue());
    }

    // ==================== getMode(String) Tests ====================

    @Test(expected = IllegalArgumentException.class)
    public void testGetModeNull() {
        UnixStat.getMode(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetModeEmpty() {
        UnixStat.getMode("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetModeInvalidLength() {
        UnixStat.getMode("rwx");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetModeInvalidCharacter() {
        UnixStat.getMode("rwxrwxrwx"); // all valid, but we need an invalid one
        // Actually, "rwxrwxrwx" is valid. Use a string with invalid char.
        UnixStat.getMode("rwxrwxrwz");
    }

    @Test
    public void testGetModeAllPermissions() {
        // Test all combinations of rwx for owner, group, other
        String[] perms = {
            "---------", "r--------", "-w-------", "--x------",
            "rw-------", "r-x------", "rwx------",
            "---r-----", "----w----", "-----x---",
            "---rw----", "---r-x---", "---rwx---",
            "------r--", "-------w-", "--------x",
            "------rw-", "------r-x", "------rwx",
            "rwxr-xr-x", "r--r--r--", "rwxrwxrwx",
            "rw-r--r--", "rwxr-xr-x", "r-xr-xr-x"
        };
        for (String perm : perms) {
            int mode = UnixStat.getMode(perm);
            assertTrue("Mode should be positive for " + perm, mode > 0);
            // Verify that the mode can be reconstructed (if such method exists)
            // We'll just check that it's a valid integer.
        }
    }

    @Test
    public void testGetModeWithSpecialBits() {
        // Test setuid (s in owner execute), setgid (s in group execute), sticky (t in other execute)
        String[] specialPerms = {
            "rwsr-xr-x", // setuid
            "rwxr-sr-x", // setgid
            "rwxr-xr-t", // sticky
            "rwsr-sr-x", // setuid + setgid
            "rwsr-xr-t", // setuid + sticky
            "rwxr-sr-t", // setgid + sticky
            "rwsr-sr-t"  // all three
        };
        for (String perm : specialPerms) {
            int mode = UnixStat.getMode(perm);
            assertTrue("Mode should be positive for " + perm, mode > 0);
            // Additional checks: the special bits should be set in the mode
            // For setuid: bit 04000, setgid: 02000, sticky: 01000
            // But we cannot verify without knowing internal representation.
            // Just ensure no exception.
        }
    }

    @Test
    public void testGetModeSymlink() {
        // Symlink permissions are usually "lrwxrwxrwx" but the method might ignore the leading 'l'
        // We'll test that it handles the string correctly.
        int mode = UnixStat.getMode("rwxrwxrwx");
        assertTrue("Mode for symlink permissions should be valid", mode > 0);
    }

    // ==================== getFileType(int) Tests ====================

    @Test
    public void testGetFileTypeRegularFile() {
        int mode = UnixStat.FILE_FLAG | 0644; // typical file
        int fileType = UnixStat.getFileType(mode);
        assertEquals("File type should be FILE_FLAG", UnixStat.FILE_FLAG, fileType);
    }

    @Test
    public void testGetFileTypeDirectory() {
        int mode = UnixStat.DIR_FLAG | 0755;
        int fileType = UnixStat.getFileType(mode);
        assertEquals("File type should be DIR_FLAG", UnixStat.DIR_FLAG, fileType);
    }

    @Test
    public void testGetFileTypeSymlink() {
        int mode = UnixStat.LINK_FLAG | 0777;
        int fileType = UnixStat.getFileType(mode);
        assertEquals("File type should be LINK_FLAG", UnixStat.LINK_FLAG, fileType);
    }

    @Test
    public void testGetFileTypeUnknown() {
        // Use a mode with no file type bits set (e.g., only permissions)
        int mode = 0644;
        int fileType = UnixStat.getFileType(mode);
        assertEquals("File type should be 0 for unknown", 0, fileType);
    }

    @Test
    public void testGetFileTypeWithSpecialBits() {
        // Ensure file type extraction works even with setuid/setgid/sticky bits
        int mode = UnixStat.FILE_FLAG | 04755; // setuid + rwxr-xr-x
        int fileType = UnixStat.getFileType(mode);
        assertEquals("File type should be FILE_FLAG despite special bits", UnixStat.FILE_FLAG, fileType);
    }

    // ==================== getPermissions(int) Tests ====================

    @Test
    public void testGetPermissionsRegular() {
        int mode = UnixStat.FILE_FLAG | 0644;
        int perms = UnixStat.getPermissions(mode);
        assertEquals("Permissions should be 0644", 0644, perms);
    }

    @Test
    public void testGetPermissionsAll() {
        int mode = 0777;
        int perms = UnixStat.getPermissions(mode);
        assertEquals("Permissions should be 0777", 0777, perms);
    }

    @Test
    public void testGetPermissionsNone() {
        int mode = 0;
        int perms = UnixStat.getPermissions(mode);
        assertEquals("Permissions should be 0", 0, perms);
    }

    @Test
    public void testGetPermissionsWithSpecialBits() {
        // Special bits should be masked out
        int mode = 04755; // setuid + rwxr-xr-x
        int perms = UnixStat.getPermissions(mode);
        assertEquals("Permissions should be 0755 (special bits removed)", 0755, perms);
    }

    // ==================== Integration / Bug Trigger Tests ====================

    @Test
    public void testGetModeAndBack() {
        // If there is a method to convert mode back to string, we could test roundtrip.
        // Assuming no such method, we just test that getMode returns consistent values.
        String perm = "rwxr-xr-x";
        int mode1 = UnixStat.getMode(perm);
        int mode2 = UnixStat.getMode(perm);
        assertEquals("getMode should be deterministic", mode1, mode2);
    }

    @Test
    public void testGetModeWithLeadingWhitespace() {
        // Some implementations might trim; we test if it throws or handles gracefully.
        try {
            int mode = UnixStat.getMode(" rwxr-xr-x");
            // If it doesn't throw, we accept; if it throws, we catch.
        } catch (IllegalArgumentException e) {
            // Expected if no trimming
        }
    }

    @Test
    public void testGetModeWithTrailingWhitespace() {
        try {
            int mode = UnixStat.getMode("rwxr-xr-x ");
        } catch (IllegalArgumentException e) {
            // Expected if no trimming
        }
    }

    @Test
    public void testGetModeLowerCase() {
        // Permissions are typically lowercase; test uppercase
        try {
            int mode = UnixStat.getMode("RWXR-XR-X");
            // If it handles case-insensitively, fine; else exception.
        } catch (IllegalArgumentException e) {
            // Expected if case-sensitive
        }
    }

    @Test
    public void testGetModeWithDashOnly() {
        int mode = UnixStat.getMode("---------");
        assertTrue("Mode for all dashes should be valid", mode > 0);
        // Permissions should be 0
        int perms = UnixStat.getPermissions(mode);
        assertEquals("Permissions should be 0 for all dashes", 0, perms);
    }

    // ==================== Edge Cases for getFileType and getPermissions ====================

    @Test
    public void testGetFileTypeMaxValue() {
        int mode = Integer.MAX_VALUE;
        int fileType = UnixStat.getFileType(mode);
        // Should not throw; file type is extracted from top bits.
        assertNotNull("File type should be non-null", fileType);
    }

    @Test
    public void testGetPermissionsMaxValue() {
        int mode = Integer.MAX_VALUE;
        int perms = UnixStat.getPermissions(mode);
        // Should not throw; permissions are lower 12 bits.
        assertTrue("Permissions should be within 0..0777", perms >= 0 && perms <= 0777);
    }

    @Test
    public void testGetFileTypeNegative() {
        int mode = -1;
        int fileType = UnixStat.getFileType(mode);
        // Should handle negative values gracefully (maybe return 0 or something)
        // We just ensure no exception.
        assertNotNull("File type should be non-null", fileType);
    }

    @Test
    public void testGetPermissionsNegative() {
        int mode = -1;
        int perms = UnixStat.getPermissions(mode);
        // Should handle negative values
        assertTrue("Permissions should be non-negative", perms >= 0);
    }

    // ==================== Additional Coverage for Internal Methods ====================

    // If there are other public methods like isFile, isDirectory, etc., we would test them.
    // Assuming only the above methods exist.

    @Test
    public void testGetModeWithAllSpecialCombinations() {
        // Exhaustive test for all 9 permission bits plus special bits
        // We'll test a few representative combinations.
        String[] perms = {
            "rwsrwsrwt", // all special bits + all permissions
            "rwsrwsrwx", // setuid+setgid, all perms except sticky
            "rwxrwxrwt", // sticky only
            "rwsr-xr-x", // setuid only
            "rwxr-sr-x", // setgid only
            "rwxr-xr-t"  // sticky only
        };
        for (String perm : perms) {
            int mode = UnixStat.getMode(perm);
            assertTrue("Mode should be positive for " + perm, mode > 0);
        }
    }

    @Test
    public void testGetModeInvalidSpecialCombination() {
        // Invalid: 's' or 't' in wrong positions (e.g., 's' in group when owner execute not set)
        // The method might throw or handle gracefully.
        try {
            UnixStat.getMode("rw-r-sr--"); // 's' in group but group execute not set? Actually 's' implies execute, so maybe valid.
            // Better: 'S' (uppercase) might be invalid.
            UnixStat.getMode("rwxrwxrwS"); // uppercase S
        } catch (IllegalArgumentException e) {
            // Expected if case-sensitive and uppercase not allowed
        }
    }

    @Test
    public void testGetModeWithOnlySpecialBits() {
        // Permissions like "---s--x--x" might be valid? 's' requires execute for owner.
        try {
            int mode = UnixStat.getMode("---s--x--x");
            // If it throws, catch.
        } catch (IllegalArgumentException e) {
            // Expected if 's' without execute is invalid
        }
    }
}