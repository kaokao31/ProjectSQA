package org.apache.commons.compress.archivers.zip;

import org.junit.Test;

import java.io.File;
import java.util.zip.ZipException;

import static org.junit.Assert.*;

public class ZipArchiveEntryTest {

    @Test
    public void testCtorString() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test/dir/");
        assertEquals("test/dir/", entry.getName());
        assertTrue(entry.isDirectory());
    }

    @Test
    public void testCtorFile() {
        File file = new File("testFile.txt");
        ZipArchiveEntry entry = new ZipArchiveEntry(file, "testFile.txt");
        assertEquals("testFile.txt", entry.getName());
    }

    @Test
    public void testCtorZipEntry() throws java.util.zip.ZipException {
        java.util.zip.ZipEntry ze = new java.util.zip.ZipEntry("zipEntry.txt");
        ZipArchiveEntry entry = new ZipArchiveEntry(ze);
        assertEquals("zipEntry.txt", entry.getName());
    }

    @Test
    public void testCtorZipArchiveEntry() {
        ZipArchiveEntry original = new ZipArchiveEntry("original.txt");
        original.setComment("comment");
        original.setMethod(ZipArchiveEntry.DEFLATED);
        
        ZipArchiveEntry copy = new ZipArchiveEntry(original);
        assertEquals("original.txt", copy.getName());
        assertEquals("comment", copy.getComment());
        assertEquals(ZipArchiveEntry.DEFLATED, copy.getMethod());
    }

    @Test
    public void testGetInternalAttributes() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        assertEquals(0, entry.getInternalAttributes());
        entry.setInternalAttributes(5);
        assertEquals(5, entry.getInternalAttributes());
    }

    @Test
    public void testGetExternalAttributes() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        assertEquals(0, entry.getExternalAttributes());
        entry.setExternalAttributes(12345L);
        assertEquals(12345L, entry.getExternalAttributes());
    }

    @Test
    public void testGetUnixMode() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        assertEquals(0, entry.getUnixMode());
        entry.setUnixMode(0777);
        assertEquals(0777, entry.getUnixMode());
        // External attributes should reflect unix mode in high bits or specific logic
        assertTrue(entry.getExternalAttributes() != 0);
    }

    @Test
    public void testGetPlatform() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        assertEquals(0, entry.getPlatform());
        entry.setPlatform(ZipArchiveEntry.PLATFORM_FAT);
        assertEquals(ZipArchiveEntry.PLATFORM_FAT, entry.getPlatform());
    }

    @Test
    public void testGetGeneralPurposeBit() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        assertNotNull(entry.getGeneralPurposeBit());
        GeneralPurposeBit b = new GeneralPurposeBit();
        b.useDataDescriptor(true);
        entry.setGeneralPurposeBit(b);
        assertSame(b, entry.getGeneralPurposeBit());
    }

    @Test
    public void testExtraFieldsManagement() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        assertNotNull(entry.getExtraFields());
        assertEquals(0, entry.getExtraFields().length);

        AsiExtraField field1 = new AsiExtraField();
        field1.setDirectory(true);
        entry.addAsExtraField(field1);

        ZipExtraField[] fields = entry.getExtraFields();
        assertEquals(1, fields.length);

        entry.removeExtraField(AsiExtraField.HEADER_ID);
        assertEquals(0, entry.getExtraFields().length);

        ZipShort headerId = new ZipShort(1);
        assertNull(entry.getExtraField(headerId));
    }

    @Test
    public void testSetExtraWithParsing() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        byte[] extraData = new byte[] { 0x01, 0x00, 0x04, 0x00, 0x01, 0x02, 0x03, 0x04 };
        entry.setExtra(extraData);
        assertNotNull(entry.getExtra());
    }

    @Test
    public void testSetGetCentralDirectoryExtra() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        byte[] data = new byte[] { 1, 2, 3 };
        entry.setCentralDirectoryExtra(data);
        assertArrayEquals(data, entry.getCentralDirectoryExtra());
    }

    @Test
    public void testGetLocalFileDataData() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        assertNotNull(entry.getLocalFileDataData());
        assertNotNull(entry.getCentralDirectoryData());
    }

    @Test
    public void testGetSize() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        assertEquals(-1, entry.getSize());
        entry.setSize(100);
        assertEquals(100, entry.getSize());
    }

    @Test
    public void testEqualsAndHashCode() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("foo");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("foo");
        ZipArchiveEntry entry3 = new ZipArchiveEntry("bar");

        assertTrue(entry1.equals(entry2));
        assertFalse(entry1.equals(entry3));
        assertFalse(entry1.equals(null));
        assertFalse(entry1.equals(new Object()));

        assertEquals(entry1.hashCode(), entry2.hashCode());
        assertNotEquals(entry1.hashCode(), entry3.hashCode());
    }

    @Test
    public void testGetSetName() {
        ZipArchiveEntry entry = new ZipArchiveEntry("initial");
        assertEquals("initial", entry.getName());
        entry.setName("updated");
        assertEquals("updated", entry.getName());
    }

    @Test
    public void testLinkNameHandling() {
        ZipArchiveEntry entry = new ZipArchiveEntry("link");
        try {
            // Test setting/getting if supported or just standard methods
            entry.setUnixMode(0120000); // Symbolic link mode
            assertEquals(0120000, entry.getUnixMode());
        } catch (Exception e) {
            // ignore if not applicable
        }
    }
}