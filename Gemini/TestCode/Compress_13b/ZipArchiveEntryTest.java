package org.apache.commons.compress.archivers.zip;

import org.junit.Test;

import java.io.File;
import java.util.zip.ZipException;

import static org.junit.Assert.*;

public class ZipArchiveEntryTest {

    @Test
    public void testDefaultConstructor() {
        ZipArchiveEntry entry = new ZipArchiveEntry();
        assertNull(entry.getName());
        assertEquals(0, entry.getInternalAttributes());
        assertEquals(0, entry.getExternalAttributes());
        assertFalse(entry.isDirectory());
    }

    @Test
    public void testStringConstructor() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test/path.txt");
        assertEquals("test/path.txt", entry.getName());
        assertFalse(entry.isDirectory());
    }

    @Test
    public void testFileConstructor() {
        File file = new File("testDir");
        ZipArchiveEntry entry = new ZipArchiveEntry(file, "testDir");
        assertEquals("testDir/", entry.getName());
        assertTrue(entry.isDirectory());

        File file2 = new File("testFile.txt");
        ZipArchiveEntry entry2 = new ZipArchiveEntry(file2, "testFile.txt");
        assertEquals("testFile.txt", entry2.getName());
        assertFalse(entry2.isDirectory());
    }

    @Test
    public void testZipEntryConstructor() throws Exception {
        java.util.zip.ZipEntry ze = new java.util.zip.ZipEntry("zipEntry.txt");
        ZipArchiveEntry entry = new ZipArchiveEntry(ze);
        assertEquals("zipEntry.txt", entry.getName());
    }

    @Test
    public void testZipArchiveEntryConstructor() {
        ZipArchiveEntry original = new ZipArchiveEntry("original.txt");
        original.setInternalAttributes(5);
        original.setExternalAttributes(10L);
        original.setUnixMode(0777);

        ZipArchiveEntry copy = new ZipArchiveEntry(original);
        assertEquals("original.txt", copy.getName());
        assertEquals(5, copy.getInternalAttributes());
        assertEquals(10L, copy.getExternalAttributes());
        assertEquals(0777, copy.getUnixMode());
    }

    @Test
    public void testSetName() {
        ZipArchiveEntry entry = new ZipArchiveEntry();
        entry.setName("foo/bar");
        assertEquals("foo/bar", entry.getName());

        // Test backslash replacement in constructor or name setting if applicable
        ZipArchiveEntry entry2 = new ZipArchiveEntry();
        entry2.setName("foo\\bar");
        assertEquals("foo/bar", entry2.getName());
    }

    @Test
    public void testGetPlatform() {
        ZipArchiveEntry entry = new ZipArchiveEntry();
        assertEquals(ZipArchiveEntry.PLATFORM_UNKNOWN, entry.getPlatform());
        
        entry.setPlatform(ZipArchiveEntry.PLATFORM_FAT);
        assertEquals(ZipArchiveEntry.PLATFORM_FAT, entry.getPlatform());
    }

    @Test
    public void testUnixMode() {
        ZipArchiveEntry entry = new ZipArchiveEntry();
        entry.setUnixMode(0644);
        assertEquals(0644, entry.getUnixMode());
        // Unix mode set should also affect platform/external attributes depending on implementation
        assertTrue(entry.getExternalAttributes() != 0);
    }

    @Test
    public void testGeneralPurposeBit() {
        ZipArchiveEntry entry = new ZipArchiveEntry();
        GeneralPurposeBit b = new GeneralPurposeBit();
        b.useDataDescriptor(true);
        entry.setGeneralPurposeBit(b);
        assertSame(b, entry.getGeneralPurposeBit());
    }

    @Test
    public void testExtraFields() {
        ZipArchiveEntry entry = new ZipArchiveEntry();
        assertNotNull(entry.getExtraFields());
        assertEquals(0, entry.getExtraFields().length);

        ZipShortField f1 = new ZipShortField(1);
        ZipLongField f2 = new ZipLongField(2L);

        entry.addExtraField(f1);
        entry.addAsFirstExtraField(f2);

        ZipExtraField[] fields = entry.getExtraFields();
        assertEquals(2, fields.length);
        assertSame(f2, fields[0]);
        assertSame(f1, fields[1]);

        entry.removeExtraField(ZipShortField.HEADER_ID);
        assertEquals(1, entry.getExtraFields().length);
    }

    @Test
    public void testSetExtra() throws ZipException {
        ZipArchiveEntry entry = new ZipArchiveEntry();
        byte[] extraData = new byte[] { 1, 0, 2, 0, 3, 4 };
        entry.setExtra(extraData);
        assertArrayEquals(extraData, entry.getExtra());
    }

    @Test
    public void testTimeFields() {
        ZipArchiveEntry entry = new ZipArchiveEntry();
        entry.setTime(123456789L);
        assertEquals(123456789L, entry.getTime());
    }

    @Test
    public void testEqualsAndHashCode() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("test");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("test");
        ZipArchiveEntry entry3 = new ZipArchiveEntry("other");

        assertTrue(entry1.equals(entry2));
        assertFalse(entry1.equals(entry3));
        assertFalse(entry1.equals(null));
        assertFalse(entry1.equals("test"));

        assertEquals(entry1.hashCode(), entry2.hashCode());
    }

    @Test
    public void testGetComment() {
        ZipArchiveEntry entry = new ZipArchiveEntry();
        assertNull(entry.getComment());
        entry.setComment("my comment");
        assertEquals("my comment", entry.getComment());
    }
}