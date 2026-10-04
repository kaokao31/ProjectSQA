package org.apache.commons.compress.archivers.zip;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import java.util.zip.ZipEntry;

/**
 * Comprehensive JUnit 4 test suite for ZipArchiveEntry, targeting maximum coverage
 * and fault detection (especially for Defects4J bug 13 related to ZIP64 handling).
 */
public class ZipArchiveEntryTest {

    private ZipArchiveEntry entry;
    private static final long ZIP64_MAGIC = 0xFFFFFFFFL;
    private static final long TEST_SIZE = 12345L;
    private static final long TEST_COMPRESSED_SIZE = 67890L;
    private static final long TEST_OFFSET = 1000L;
    private static final int TEST_DISK = 1;

    @Before
    public void setUp() {
        entry = new ZipArchiveEntry("testEntry.txt");
    }

    // ==================== Constructor Tests ====================

    @Test
    public void testDefaultConstructor() {
        ZipArchiveEntry e = new ZipArchiveEntry();
        assertNotNull(e);
        assertNull(e.getName());
    }

    @Test
    public void testConstructorWithName() {
        ZipArchiveEntry e = new ZipArchiveEntry("foo");
        assertEquals("foo", e.getName());
    }

    @Test
    public void testConstructorWithZipEntry() {
        ZipEntry ze = new ZipEntry("bar");
        ze.setSize(100);
        ze.setCompressedSize(50);
        ZipArchiveEntry e = new ZipArchiveEntry(ze);
        assertEquals("bar", e.getName());
        assertEquals(100, e.getSize());
        assertEquals(50, e.getCompressedSize());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullZipEntry() {
        new ZipArchiveEntry((ZipEntry) null);
    }

    // ==================== Basic Getter/Setter Tests ====================

    @Test
    public void testSetGetName() {
        entry.setName("newName");
        assertEquals("newName", entry.getName());
    }

    @Test
    public void testSetGetComment() {
        entry.setComment("a comment");
        assertEquals("a comment", entry.getComment());
    }

    @Test
    public void testSetGetSize() {
        entry.setSize(TEST_SIZE);
        assertEquals(TEST_SIZE, entry.getSize());
    }

    @Test
    public void testSetGetCompressedSize() {
        entry.setCompressedSize(TEST_COMPRESSED_SIZE);
        assertEquals(TEST_COMPRESSED_SIZE, entry.getCompressedSize());
    }

    @Test
    public void testSetGetLocalHeaderOffset() {
        entry.setLocalHeaderOffset(TEST_OFFSET);
        assertEquals(TEST_OFFSET, entry.getLocalHeaderOffset());
    }

    @Test
    public void testSetGetDiskNumberStart() {
        entry.setDiskNumberStart(TEST_DISK);
        assertEquals(TEST_DISK, entry.getDiskNumberStart());
    }

    @Test
    public void testSetGetInternalAttributes() {
        entry.setInternalAttributes(0x1234);
        assertEquals(0x1234, entry.getInternalAttributes());
    }

    @Test
    public void testSetGetExternalAttributes() {
        entry.setExternalAttributes(0x5678L);
        assertEquals(0x5678L, entry.getExternalAttributes());
    }

    @Test
    public void testSetGetPlatform() {
        entry.setPlatform(3);
        assertEquals(3, entry.getPlatform());
    }

    // ==================== Extra Fields Tests ====================

    @Test
    public void testAddAndGetExtraField() {
        ZipShort headerId = new ZipShort(0xCAFE);
        UnrecognizedExtraField field = new UnrecognizedExtraField();
        field.setHeaderId(headerId);
        field.setLocalFileDataData(new byte[] {1,2,3});
        field.setCentralDirectoryData(new byte[] {4,5,6});
        entry.addExtraField(field);
        assertSame(field, entry.getExtraField(headerId));
    }

    @Test
    public void testRemoveExtraField() {
        ZipShort headerId = new ZipShort(0xCAFE);
        UnrecognizedExtraField field = new UnrecognizedExtraField();
        field.setHeaderId(headerId);
        entry.addExtraField(field);
        assertNotNull(entry.getExtraField(headerId));
        entry.removeExtraField(headerId);
        assertNull(entry.getExtraField(headerId));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveNonExistentExtraField() {
        entry.removeExtraField(new ZipShort(0xDEAD));
    }

    @Test
    public void testGetExtraFields() {
        ZipShort id1 = new ZipShort(1);
        ZipShort id2 = new ZipShort(2);
        UnrecognizedExtraField f1 = new UnrecognizedExtraField();
        f1.setHeaderId(id1);
        UnrecognizedExtraField f2 = new UnrecognizedExtraField();
        f2.setHeaderId(id2);
        entry.addExtraField(f1);
        entry.addExtraField(f2);
        ZipExtraField[] fields = entry.getExtraFields();
        assertEquals(2, fields.length);
    }

    @Test
    public void testSetExtraFields() {
        ZipExtraField[] fields = new ZipExtraField[1];
        ZipShort id = new ZipShort(0x1234);
        UnrecognizedExtraField f = new UnrecognizedExtraField();
        f.setHeaderId(id);
        fields[0] = f;
        entry.setExtraFields(fields);
        assertSame(f, entry.getExtraField(id));
    }

    @Test
    public void testGetExtraFieldWithNullId() {
        assertNull(entry.getExtraField(null));
    }

    // ==================== Central/Local Extra Data Tests ====================

    @Test
    public void testGetCentralDirectoryExtra() {
        byte[] data = entry.getCentralDirectoryExtra();
        assertNotNull(data);
    }

    @Test
    public void testGetLocalFileDataExtra() {
        byte[] data = entry.getLocalFileDataExtra();
        assertNotNull(data);
    }

    // ==================== ZIP64 Handling Tests (Bug 13) ====================

    @Test
    public void testGetSizeWithZIP64ExtraField() {
        // Simulate ZIP64: set size to magic value, then add ZIP64 extra field with actual size
        entry.setSize(ZIP64_MAGIC);
        assertEquals(ZIP64_MAGIC, entry.getSize()); // before extra field, returns magic

        Zip64ExtendedInformationExtraField zip64 = new Zip64ExtendedInformationExtraField();
        zip64.setSize(TEST_SIZE);
        entry.addExtraField(zip64);
        // After adding ZIP64 extra field, getSize() should return the value from the extra field
        assertEquals(TEST_SIZE, entry.getSize());
    }

    @Test
    public void testGetCompressedSizeWithZIP64ExtraField() {
        entry.setCompressedSize(ZIP64_MAGIC);
        assertEquals(ZIP64_MAGIC, entry.getCompressedSize());

        Zip64ExtendedInformationExtraField zip64 = new Zip64ExtendedInformationExtraField();
        zip64.setCompressedSize(TEST_COMPRESSED_SIZE);
        entry.addExtraField(zip64);
        assertEquals(TEST_COMPRESSED_SIZE, entry.getCompressedSize());
    }

    @Test
    public void testGetLocalHeaderOffsetWithZIP64ExtraField() {
        entry.setLocalHeaderOffset(ZIP64_MAGIC);
        assertEquals(ZIP64_MAGIC, entry.getLocalHeaderOffset());

        Zip64ExtendedInformationExtraField zip64 = new Zip64ExtendedInformationExtraField();
        zip64.setLocalHeaderOffset(TEST_OFFSET);
        entry.addExtraField(zip64);
        assertEquals(TEST_OFFSET, entry.getLocalHeaderOffset());
    }

    @Test
    public void testGetDiskNumberStartWithZIP64ExtraField() {
        entry.setDiskNumberStart(0xFFFF);
        assertEquals(0xFFFF, entry.getDiskNumberStart());

        Zip64ExtendedInformationExtraField zip64 = new Zip64ExtendedInformationExtraField();
        zip64.setDiskNumberStart(TEST_DISK);
        entry.addExtraField(zip64);
        assertEquals(TEST_DISK, entry.getDiskNumberStart());
    }

    @Test
    public void testSetSizeUpdatesZIP64ExtraField() {
        // When size is set to a value that requires ZIP64, the extra field should be created/updated
        entry.setSize(ZIP64_MAGIC + 1); // > 0xFFFFFFFF
        Zip64ExtendedInformationExtraField zip64 = 
            (Zip64ExtendedInformationExtraField) entry.getExtraField(Zip64ExtendedInformationExtraField.HEADER_ID);
        assertNotNull("ZIP64 extra field should be present for large size", zip64);
        assertEquals(ZIP64_MAGIC + 1, zip64.getSize().getLongValue());
    }

    @Test
    public void testSetCompressedSizeUpdatesZIP64ExtraField() {
        entry.setCompressedSize(ZIP64_MAGIC + 1);
        Zip64ExtendedInformationExtraField zip64 = 
            (Zip64ExtendedInformationExtraField) entry.getExtraField(Zip64ExtendedInformationExtraField.HEADER_ID);
        assertNotNull(zip64);
        assertEquals(ZIP64_MAGIC + 1, zip64.getCompressedSize().getLongValue());
    }

    @Test
    public void testSetLocalHeaderOffsetUpdatesZIP64ExtraField() {
        entry.setLocalHeaderOffset(ZIP64_MAGIC + 1);
        Zip64ExtendedInformationExtraField zip64 = 
            (Zip64ExtendedInformationExtraField) entry.getExtraField(Zip64ExtendedInformationExtraField.HEADER_ID);
        assertNotNull(zip64);
        assertEquals(ZIP64_MAGIC + 1, zip64.getLocalHeaderOffset().getLongValue());
    }

    @Test
    public void testSetDiskNumberStartUpdatesZIP64ExtraField() {
        entry.setDiskNumberStart(0xFFFF);
        Zip64ExtendedInformationExtraField zip64 = 
            (Zip64ExtendedInformationExtraField) entry.getExtraField(Zip64ExtendedInformationExtraField.HEADER_ID);
        assertNotNull(zip64);
        assertEquals(0xFFFF, zip64.getDiskNumberStart().getLongValue());
    }

    @Test
    public void testGetSizeWithoutZIP64ExtraField() {
        entry.setSize(TEST_SIZE);
        assertEquals(TEST_SIZE, entry.getSize());
    }

    @Test
    public void testGetCompressedSizeWithoutZIP64ExtraField() {
        entry.setCompressedSize(TEST_COMPRESSED_SIZE);
        assertEquals(TEST_COMPRESSED_SIZE, entry.getCompressedSize());
    }

    // ==================== equals() and hashCode() Tests ====================

    @Test
    public void testEqualsSameObject() {
        assertTrue(entry.equals(entry));
    }

    @Test
    public void testEqualsNull() {
        assertFalse(entry.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        assertFalse(entry.equals("string"));
    }

    @Test
    public void testEqualsDifferentName() {
        ZipArchiveEntry other = new ZipArchiveEntry("other");
        assertFalse(entry.equals(other));
    }

    @Test
    public void testEqualsSameName() {
        ZipArchiveEntry other = new ZipArchiveEntry("testEntry.txt");
        assertTrue(entry.equals(other));
    }

    @Test
    public void testHashCodeConsistency() {
        int hash1 = entry.hashCode();
        int hash2 = entry.hashCode();
        assertEquals(hash1, hash2);
    }

    @Test
    public void testHashCodeDifferentEntries() {
        ZipArchiveEntry other = new ZipArchiveEntry("other");
        assertNotEquals(entry.hashCode(), other.hashCode());
    }

    // ==================== clone() Test ====================

    @Test
    public void testClone() throws CloneNotSupportedException {
        entry.setSize(100);
        entry.setComment("clone test");
        ZipArchiveEntry clone = (ZipArchiveEntry) entry.clone();
        assertNotSame(entry, clone);
        assertEquals(entry.getName(), clone.getName());
        assertEquals(entry.getSize(), clone.getSize());
        assertEquals(entry.getComment(), clone.getComment());
    }

    // ==================== Edge Cases ====================

    @Test
    public void testSetSizeNegative() {
        entry.setSize(-1);
        assertEquals(-1, entry.getSize());
    }

    @Test
    public void testSetCompressedSizeNegative() {
        entry.setCompressedSize(-1);
        assertEquals(-1, entry.getCompressedSize());
    }

    @Test
    public void testSetLocalHeaderOffsetNegative() {
        entry.setLocalHeaderOffset(-1);
        assertEquals(-1, entry.getLocalHeaderOffset());
    }

    @Test
    public void testSetDiskNumberStartNegative() {
        entry.setDiskNumberStart(-1);
        assertEquals(-1, entry.getDiskNumberStart());
    }

    @Test
    public void testSetNameNull() {
        entry.setName(null);
        assertNull(entry.getName());
    }

    @Test
    public void testSetCommentNull() {
        entry.setComment(null);
        assertNull(entry.getComment());
    }

    @Test
    public void testSetExtraFieldsNull() {
        entry.setExtraFields(null);
        assertNotNull(entry.getExtraFields());
        assertEquals(0, entry.getExtraFields().length);
    }

    @Test
    public void testAddExtraFieldNull() {
        entry.addExtraField(null);
        // should not throw, but no field added
    }

    @Test
    public void testGetExtraFieldNonExistent() {
        assertNull(entry.getExtraField(new ZipShort(0x9999)));
    }

    @Test
    public void testGetCentralDirectoryExtraWithExtraFields() {
        UnrecognizedExtraField field = new UnrecognizedExtraField();
        field.setHeaderId(new ZipShort(0x1111));
        field.setCentralDirectoryData(new byte[] {10,20});
        entry.addExtraField(field);
        byte[] data = entry.getCentralDirectoryExtra();
        assertTrue(data.length > 0);
    }

    @Test
    public void testGetLocalFileDataExtraWithExtraFields() {
        UnrecognizedExtraField field = new UnrecognizedExtraField();
        field.setHeaderId(new ZipShort(0x2222));
        field.setLocalFileDataData(new byte[] {30,40});
        entry.addExtraField(field);
        byte[] data = entry.getLocalFileDataExtra();
        assertTrue(data.length > 0);
    }

    // ==================== Additional Coverage ====================

    @Test
    public void testSetGetMethod() {
        entry.setMethod(ZipEntry.DEFLATED);
        assertEquals(ZipEntry.DEFLATED, entry.getMethod());
    }

    @Test
    public void testSetGetTime() {
        long time = System.currentTimeMillis();
        entry.setTime(time);
        assertEquals(time, entry.getTime());
    }

    @Test
    public void testSetGetCrc() {
        entry.setCrc(0x12345678L);
        assertEquals(0x12345678L, entry.getCrc());
    }

    @Test
    public void testSetGetExtra() {
        byte[] extra = new byte[] {0,1,2,3};
        entry.setExtra(extra);
        assertArrayEquals(extra, entry.getExtra());
    }

    @Test
    public void testSetExtraNull() {
        entry.setExtra(null);
        assertNull(entry.getExtra());
    }

    @Test
    public void testIsDirectory() {
        assertFalse(entry.isDirectory());
        entry.setName("dir/");
        assertTrue(entry.isDirectory());
    }

    @Test
    public void testToString() {
        assertNotNull(entry.toString());
    }
}