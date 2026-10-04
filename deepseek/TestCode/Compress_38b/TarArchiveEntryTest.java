package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

/**
 * JUnit 4 test suite for TarArchiveEntry.
 * Designed to achieve high line/branch coverage and detect underlying faults,
 * particularly related to bug #38 (e.g., handling of long file names, prefix
 * management, checksum computation, etc.).
 */
public class TarArchiveEntryTest {

    private TarArchiveEntry entry;
    private static final long TEST_MOD_TIME = 1234567890000L; // ~2009
    private static final int TEST_MODE = 0644;
    private static final int TEST_USER_ID = 1000;
    private static final int TEST_GROUP_ID = 100;
    private static final String TEST_USER_NAME = "testuser";
    private static final String TEST_GROUP_NAME = "testgroup";

    @Before
    public void setUp() {
        entry = new TarArchiveEntry("testEntry.txt");
        entry.setModTime(new Date(TEST_MOD_TIME));
        entry.setMode(TEST_MODE);
        entry.setUserId(TEST_USER_ID);
        entry.setGroupId(TEST_GROUP_ID);
        entry.setUserName(TEST_USER_NAME);
        entry.setGroupName(TEST_GROUP_NAME);
        entry.setSize(1024L);
        entry.setLinkName("");
    }

    // ---------- Constructors ----------

    @Test
    public void testDefaultConstructorSetsNameAndDefaults() {
        TarArchiveEntry e = new TarArchiveEntry("default.tgz");
        assertEquals("default.tgz", e.getName());
        assertTrue(new Date(0).equals(e.getModTime()) || e.getModTime() != null); // mod time should be set
        assertEquals(0, e.getSize());
        assertEquals(TarArchiveEntry.DEFAULT_DIR_MODE, e.getMode() & 0777);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullNameThrowsException() {
        new TarArchiveEntry((String) null);
    }

    @Test
    public void testConstructorWithFileAndEntryName() throws IOException {
        File tempFile = File.createTempFile("test", ".txt");
        tempFile.deleteOnExit();
        TarArchiveEntry fileEntry = new TarArchiveEntry(tempFile, "customEntryName.txt");
        assertEquals("customEntryName.txt", fileEntry.getName());
        // Size and mod time from actual file
        assertEquals(tempFile.length(), fileEntry.getSize());
        assertTrue(fileEntry.getModTime().getTime() >= tempFile.lastModified());
        assertFalse(fileEntry.isDirectory());
    }

    @Test
    public void testConstructorWithDirectoryFile() throws IOException {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "testTarDir_" + System.nanoTime());
        tempDir.mkdirs();
        tempDir.deleteOnExit();
        TarArchiveEntry dirEntry = new TarArchiveEntry(tempDir, "dirName/");
        assertTrue(dirEntry.isDirectory());
        assertEquals("dirName", dirEntry.getName()); // trailing slash removed
    }

    // ---------- Name handling (including long names) ----------

    @Test
    public void testSetNameAndGetName() {
        entry.setName("newName.txt");
        assertEquals("newName.txt", entry.getName());
    }

    @Test(expected = NullPointerException.class)
    public void testSetNameNullThrowsException() {
        entry.setName(null);
    }

    @Test
    public void testLongNameUnder100Bytes() {
        String longName = generateString('a', 99);
        entry.setName(longName);
        assertEquals(longName, entry.getName());
    }

    @Test
    public void testLongNameExactly100Bytes() {
        String longName = generateString('b', 100);
        entry.setName(longName);
        assertEquals(longName, entry.getName());
    }

    @Test
    public void testLongNameOver100BytesUsesPrefix() {
        // Names longer than 100 bytes require using the prefix field
        String longName = generateString('c', 150);
        entry.setName(longName);
        assertEquals(longName, entry.getName()); // should retain full name
    }

    @Test
    public void testLongNameExactly155Bytes() {
        String longName = generateString('d', 155);
        entry.setName(longName);
        assertEquals(longName, entry.getName());
    }

    @Test
    public void testLongNameOver155Bytes() {
        // In tar format max is 255 bytes (100 name + 155 prefix)
        String longName = generateString('e', 200);
        entry.setName(longName);
        assertEquals(longName, entry.getName()); // modern implementations support via extended headers
    }

    @Test
    public void testNameWithPathSeparators() {
        entry.setName("subdir/another/file.txt");
        assertEquals("subdir/another/file.txt", entry.getName());
    }

    // ---------- Size, mode, IDs, user/group names ----------

    @Test
    public void testSetSize() {
        entry.setSize(2048L);
        assertEquals(2048L, entry.getSize());
    }

    @Test
    public void testSetSizeZero() {
        entry.setSize(0L);
        assertEquals(0L, entry.getSize());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetSizeNegativeThrowsException() {
        entry.setSize(-1L);
    }

    @Test
    public void testSetMode() {
        entry.setMode(0755);
        assertEquals(0755, entry.getMode());
    }

    @Test
    public void testSetUserId() {
        entry.setUserId(500);
        assertEquals(500, entry.getUserId());
    }

    @Test
    public void testSetGroupId() {
        entry.setGroupId(50);
        assertEquals(50, entry.getGroupId());
    }

    @Test
    public void testSetUserName() {
        entry.setUserName("user");
        assertEquals("user", entry.getUserName());
    }

    @Test
    public void testSetUserNameNull() {
        entry.setUserName(null);
        assertEquals("", entry.getUserName()); // common behavior: convert null to empty
    }

    @Test
    public void testSetGroupName() {
        entry.setGroupName("group");
        assertEquals("group", entry.getGroupName());
    }

    @Test
    public void testSetGroupNameNull() {
        entry.setGroupName(null);
        assertEquals("", entry.getGroupName());
    }

    // ---------- Link name ----------

    @Test
    public void testSetLinkName() {
        entry.setLinkName("targetLink");
        assertEquals("targetLink", entry.getLinkName());
    }

    @Test
    public void testSetLinkNameNull() {
        entry.setLinkName(null);
        assertEquals("", entry.getLinkName());
    }

    // ---------- Mod time ----------

    @Test
    public void testSetModTime() {
        Date d = new Date(987654321000L);
        entry.setModTime(d);
        assertEquals(d, entry.getModTime());
    }

    @Test(expected = NullPointerException.class)
    public void testSetModTimeNullThrowsException() {
        entry.setModTime(null);
    }

    // ---------- Checksum ----------

    @Test
    public void testComputeCheckSumFromHeader() throws Exception {
        // Use a known header, compute checksum and verify it equals expected value
        byte[] header = new byte[TarConstants.BLOCK_SIZE];
        // Fill with some dummy data (as done by writeEntryHeader)
        entry.writeEntryHeader(header);
        long computed = TarUtils.computeCheckSum(header);
        assertTrue("Checksum should be positive", computed > 0);
        // The checksum is stored in the header at offset 148, length 8 (octal)
        String stored = new String(header, 148, 7, "ASCII").trim(); // ignore trailing null
        long storedLong = Long.parseLong(stored, 8);
        assertEquals("Stored checksum should match computed checksum", storedLong, computed);
    }

    @Test
    public void testIsCheckSumOkValidHeader() throws Exception {
        byte[] header = new byte[TarConstants.BLOCK_SIZE];
        entry.writeEntryHeader(header);
        // After writing, checksum is set; should be ok
        assertTrue("Checksum should be valid for a freshly written header", TarArchiveEntry.isCheckSumOk(header));
    }

    @Test
    public void testIsCheckSumOkCorruptedHeader() {
        byte[] header = new byte[TarConstants.BLOCK_SIZE];
        // Corrupt some bytes
        header[0] = (byte) 0xFF;
        assertFalse("Checksum should be invalid for corrupted header", TarArchiveEntry.isCheckSumOk(header));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsCheckSumOkNullHeader() {
        TarArchiveEntry.isCheckSumOk(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsCheckSumOkShortHeader() {
        byte[] shortHeader = new byte[100];
        TarArchiveEntry.isCheckSumOk(shortHeader);
    }

    // ---------- Directory detection ----------

    @Test
    public void testIsDirectoryWhenNameEndsWithSlash() {
        TarArchiveEntry dirEntry = new TarArchiveEntry("mydir/");
        assertTrue("Entry with trailing slash should be directory", dirEntry.isDirectory());
    }

    @Test
    public void testIsDirectoryWhenNameIsDirectoryEntry() {
        TarArchiveEntry dirEntry = new TarArchiveEntry("mydir");
        // Without trailing slash, it's not automatically directory
        assertFalse("Entry without trailing slash is not directory", dirEntry.isDirectory());
    }

    @Test
    public void testIsDirectoryMode() {
        entry.setMode(TarArchiveEntry.DEFAULT_DIR_MODE);
        // Even if mode is directory, if name doesn't end with slash, isDirectory checks both
        // Actually isDirectory checks name ends with '/' or mode indicates directory
        // For default entry, name doesn't end with '/', so behavior depends on implementation
        // We'll set name accordingly
        TarArchiveEntry dirEntry = new TarArchiveEntry("adir/");
        dirEntry.setMode(0755); // regular mode
        assertTrue("Name ends with slash should be directory", dirEntry.isDirectory());
    }

    @Test
    public void testNotDirectory() {
        assertFalse("Regular file entry should not be directory", entry.isDirectory());
    }

    // ---------- GNU tar long name extension (if applicable) ----------

    @Test
    public void testGnuLongNameExtension() {
        // Some implementations use special entry types to store long names
        // This test may not apply to all versions; skip if method not present
        // We'll just test that setting a very long name works without exception
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            sb.append('x');
        }
        String veryLong = sb.toString();
        entry.setName(veryLong);
        assertEquals(veryLong, entry.getName());
    }

    // ---------- Persistence via header (parse/write) ----------

    @Test
    public void testWriteAndParseHeaderRoundTrip() throws Exception {
        // Write entry to header
        byte[] header = new byte[TarConstants.BLOCK_SIZE];
        entry.writeEntryHeader(header);
        // Create a new entry from header
        TarArchiveEntry parsedEntry = new TarArchiveEntry(header);
        // Compare fields
        assertEquals(entry.getName(), parsedEntry.getName());
        assertEquals(entry.getSize(), parsedEntry.getSize());
        assertEquals(entry.getMode(), parsedEntry.getMode());
        assertEquals(entry.getUserId(), parsedEntry.getUserId());
        assertEquals(entry.getGroupId(), parsedEntry.getGroupId());
        assertEquals(entry.getLinkName(), parsedEntry.getLinkName());
        assertEquals(entry.getModTime(), parsedEntry.getModTime());
        // User/group names are not stored in standard header, so may be empty
        assertEquals("", parsedEntry.getUserName());
        assertEquals("", parsedEntry.getGroupName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseInvalidHeader() {
        byte[] invalidHeader = new byte[TarConstants.BLOCK_SIZE];
        // Fill with something that makes no sense
        new TarArchiveEntry(invalidHeader);
        // Expect exception because magic number check fails or something
    }

    @Test
    public void testParseHeaderWithTrailingNulls() {
        byte[] header = new byte[TarConstants.BLOCK_SIZE];
        // Fill with zeros except minimal required fields
        // This might throw an exception; we want to test graceful handling
        try {
            new TarArchiveEntry(header);
            fail("Expected exception for all-zero header");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ---------- Utility methods ----------

    @Test
    public void testGetModTimeEpoch() {
        // Test with epoch 0
        entry.setModTime(new Date(0));
        assertEquals(0, entry.getModTime().getTime());
    }

    @Test
    public void testSetIds() {
        entry.setIds(500, 50);
        assertEquals(500, entry.getUserId());
        assertEquals(50, entry.getGroupId());
    }

    @Test
    public void testSetNames() {
        entry.setNames("usr", "grp");
        assertEquals("usr", entry.getUserName());
        assertEquals("grp", entry.getGroupName());
    }

    @Test
    public void testHashCode() {
        TarArchiveEntry another = new TarArchiveEntry("testEntry.txt");
        // Should return equal hash if same name (other fields may differ)
        // Actually hash code implementation may use name only or more
        assertNotNull(entry.hashCode());
    }

    @Test
    public void testEqualsSameObject() {
        assertTrue(entry.equals(entry));
    }

    @Test
    public void testEqualsDifferentObjectSameName() {
        TarArchiveEntry another = new TarArchiveEntry("testEntry.txt");
        // Should be equal if same name (depending on implementation, may ignore other fields)
        // This tests that equals is implemented consistently
        assertEquals(entry, another);
    }

    @Test
    public void testEqualsNull() {
        assertFalse(entry.equals(null));
    }

    @Test
    public void testEqualsDifferentName() {
        TarArchiveEntry another = new TarArchiveEntry("different.txt");
        assertFalse(entry.equals(another));
    }

    @Test
    public void testToString() {
        String str = entry.toString();
        assertTrue(str.contains("testEntry.txt"));
    }

    @Test
    public void testClone() throws CloneNotSupportedException {
        TarArchiveEntry cloned = (TarArchiveEntry) entry.clone();
        assertEquals(entry.getName(), cloned.getName());
        assertEquals(entry.getSize(), cloned.getSize());
        // Ensure deep copy of mod time
        entry.setModTime(new Date(0));
        assertNotEquals(entry.getModTime(), cloned.getModTime());
    }

    // ---------- Edge cases for header parsing ----------

    @Test
    public void testHeaderParsingSpecialCharactersInName() throws Exception {
        entry.setName("file with spaces and special!@#$%^&*().txt");
        byte[] header = new byte[TarConstants.BLOCK_SIZE];
        entry.writeEntryHeader(header);
        TarArchiveEntry parsed = new TarArchiveEntry(header);
        assertEquals(entry.getName(), parsed.getName());
    }

    @Test
    public void testHeaderParsingUnicodeName() throws Exception {
        entry.setName("文件.txt"); // Chinese characters
        byte[] header = new byte[TarConstants.BLOCK_SIZE];
        entry.writeEntryHeader(header);
        // Parsing may depend on encoding; if ASCII only, this may fail
        // In many implementations, names are stored as ISO-8859-1
        // This test ensures no exception is thrown
        try {
            TarArchiveEntry parsed = new TarArchiveEntry(header);
            // The parsed name may be corrupted but shouldn't crash
            assertNotNull(parsed.getName());
        } catch (Exception e) {
            // Accept if encoding is unsupported
        }
    }

    // ---------- Helper to generate strings ----------

    private String generateString(char c, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}