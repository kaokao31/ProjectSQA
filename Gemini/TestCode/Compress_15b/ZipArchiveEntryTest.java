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
        assertEquals(0, entry.getMethod());
        assertEquals(-1, entry.getSize());
    }

    @Test
    public void testNameConstructor() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test/path/file.txt");
        assertEquals("test/path/file.txt", entry.getName());
    }

    @Test
    public void testFileConstructor() {
        File file = new File("dummyfile.txt");
        ZipArchiveEntry entry = new ZipArchiveEntry(file, "dummyfile.txt");
        assertEquals("dummyfile.txt", entry.getName());
    }

    @Test
    public void testJavaUtilZipEntryConstructor() {
        java.util.zip.ZipEntry ze = new java.util.zip.ZipEntry("utilzip.txt");
        ze.setSize(100);
        ZipArchiveEntry entry = new ZipArchiveEntry(ze);
        assertEquals("utilzip.txt", entry.getName());
        assertEquals(100, entry.getSize());
    }

    @Test
    public void testZipArchiveEntryCopyConstructor() {
        ZipArchiveEntry original = new ZipArchiveEntry("original.txt");
        original.setSize(500);
        original.setMethod(ZipArchiveEntry.DEFLATED);

        ZipArchiveEntry copy = new ZipArchiveEntry(original);
        assertEquals("original.txt", copy.getName());
        assertEquals(500, copy.getSize());
        assertEquals(ZipArchiveEntry.DEFLATED, copy.getMethod());
    }

    @Test
    public void testGetCentralDirectoryExtra() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        byte[] extra = new byte[]{1, 2, 3, 4};
        entry.setCentralDirectoryExtra(extra);
        assertArrayEquals(extra, entry.getCentralDirectoryExtra());
    }

    @Test
    public void testSetDataOffset() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setDataOffset(12345L);
        assertEquals(12345L, entry.getDataOffset());
    }

    @Test
    public void testSetInternalAttributes() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setInternalAttributes(42);
        assertEquals(42, entry.getInternalAttributes());
    }

    @Test
    public void testSetExternalAttributes() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setExternalAttributes(9999L);
        assertEquals(9999L, entry.getExternalAttributes());
    }

    @Test
    public void testSetVersionRequired() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setVersionRequired(20);
        assertEquals(20, entry.getVersionRequired());
    }

    @Test
    public void testSetVersionMadeBy() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setVersionMadeBy(30);
        assertEquals(30, entry.getVersionMadeBy());
    }

    @Test
    public void testSetPlatform() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setPlatform(ZipArchiveEntry.PLATFORM_FAT);
        assertEquals(ZipArchiveEntry.PLATFORM_FAT, entry.getPlatform());
    }

    @Test
    public void testSetComment() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setComment("my comment");
        assertEquals("my comment", entry.getComment());
    }

    @Test
    public void testSetMethod() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setMethod(ZipArchiveEntry.STORED);
        assertEquals(ZipArchiveEntry.STORED, entry.getMethod());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMethodInvalid() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setMethod(999);
    }

    @Test
    public void testExtraFieldsManagement() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        
        ZipShort headerId = new ZipShort(1);
        AsiExtraField field1 = new AsiExtraField();
        field1.setHeaderId(headerId);

        entry.addExtraField(field1);
        assertEquals(1, entry.getExtraFields().length);

        entry.removeExtraField(headerId);
        assertEquals(0, entry.getExtraFields().length);
    }

    @Test
    public void testAddAsFirstExtraField() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        
        AsiExtraField field1 = new AsiExtraField();
        field1.setHeaderId(new ZipShort(1));

        AsiExtraField field2 = new AsiExtraField();
        field2.setHeaderId(new ZipShort(2));

        entry.addExtraField(field1);
        entry.addAsFirstExtraField(field2);

        ZipExtraField[] fields = entry.getExtraFields();
        assertEquals(2, fields.length);
        assertEquals(2, fields[0].getHeaderId().getValue());
    }

    @Test
    public void testSetExtraWithBytes() throws ZipException {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        byte[] data = new byte[]{1, 0, 2, 0, 3, 4};
        entry.setExtra(data);
        assertNotNull(entry.getExtra());
    }

    @Test
    public void testGetGeneralPurposeBit() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        assertNotNull(entry.getGeneralPurposeBit());
    }

    @Test
    public void testEqualsAndHashCode() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("test.txt");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("test.txt");
        ZipArchiveEntry entry3 = new ZipArchiveEntry("other.txt");

        assertEquals(entry1, entry2);
        assertEquals(entry1.hashCode(), entry2.hashCode());
        assertNotEquals(entry1, entry3);
        assertNotEquals(entry1, null);
        assertNotEquals(entry1, new Object());
    }

    @Test
    public void testGetDiskNumberStart() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        assertEquals(0, entry.getDiskNumberStart());
        entry.setDiskNumberStart(5);
        assertEquals(5, entry.getDiskNumberStart());
    }
}