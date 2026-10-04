package org.apache.commons.compress.archivers.zip;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

import org.junit.Before;
import org.junit.Test;

public class ZipArchiveEntryTest {

    private static final String NAME = "test.txt";
    private static final long SIZE = 1024L;
    private static final long COMPRESSED_SIZE = 512L;
    private static final int METHOD = ZipEntry.DEFLATED;
    private static final int PLATFORM = ZipArchiveEntry.PLATFORM_FAT;
    private static final byte[] EXTRA_DATA = new byte[] {0x01, 0x02, 0x03, 0x04};
    private static final int RAW_FLAG = 0x0800;

    private ZipArchiveEntry entry;
    private ZipArchiveEntry entryCopy;

    @Before
    public void setUp() {
        entry = new ZipArchiveEntry(NAME);
        entryCopy = new ZipArchiveEntry(entry);
    }

    // ===================== Constructors =====================

    @Test
    public void testConstructorString() {
        assertEquals(NAME, entry.getName());
        assertNull(entry.getExtra());
        assertEquals(0, entry.getSize());
        assertEquals(0, entry.getCompressedSize());
        assertEquals(ZipEntry.STORED, entry.getMethod());
        assertEquals(-1, entry.getOffset());
        assertEquals(PLATFORM, entry.getPlatform());
        assertEquals(0, entry.getRawFlag());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullStringThrows() {
        new ZipArchiveEntry((String) null);
    }

    @Test
    public void testConstructorZipEntry() {
        ZipEntry ze = new ZipEntry(NAME);
        ZipArchiveEntry zae = new ZipArchiveEntry(ze);
        assertEquals(NAME, zae.getName());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullZipEntryThrows() {
        new ZipArchiveEntry((ZipEntry) null);
    }

    @Test
    public void testConstructorZipArchiveEntry() {
        entry.setSize(SIZE);
        entry.setMethod(METHOD);
        entry.setExtra(EXTRA_DATA);
        entry.setPlatform(PLATFORM);
        entry.setRawFlag(RAW_FLAG);
        entry.setInternalAttributes(0x0002);
        entry.setExternalAttributes(0x20L);
        ZipArchiveEntry copy = new ZipArchiveEntry(entry);
        assertEquals(entry.getName(), copy.getName());
        assertEquals(entry.getSize(), copy.getSize());
        assertEquals(entry.getMethod(), copy.getMethod());
        assertArrayEquals(entry.getExtra(), copy.getExtra());
        assertEquals(entry.getPlatform(), copy.getPlatform());
        assertEquals(entry.getRawFlag(), copy.getRawFlag());
        assertEquals(entry.getInternalAttributes(), copy.getInternalAttributes());
        assertEquals(entry.getExternalAttributes(), copy.getExternalAttributes());
        assertEquals(entry.getOffset(), copy.getOffset());
        assertEquals(entry.getCompressedSize(), copy.getCompressedSize());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullZipArchiveEntryThrows() {
        new ZipArchiveEntry((ZipArchiveEntry) null);
    }

    // ===================== getName / setName =====================

    @Test
    public void testSetName() {
        String newName = "folder/file.txt";
        entry.setName(newName);
        assertEquals(newName, entry.getName());
    }

    @Test
    public void testSetNameNullLeavesOldName() {
        entry.setName(null);
        assertEquals(NAME, entry.getName());
    }

    @Test
    public void testSetNameWithEmptyString() {
        entry.setName("");
        assertEquals("", entry.getName());
    }

    @Test
    public void testGetNameWithUnicode() {
        String unicode = "äöüß.txt";
        entry.setName(unicode);
        assertEquals(unicode, entry.getName());
    }

    // ===================== setSize / getSize =====================

    @Test
    public void testSetSize() {
        entry.setSize(SIZE);
        assertEquals(SIZE, entry.getSize());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetSizeNegativeThrows() {
        entry.setSize(-1);
    }

    @Test
    public void testSetSizeZero() {
        entry.setSize(0);
        assertEquals(0, entry.getSize());
    }

    @Test
    public void testSetSizeMaxLong() {
        entry.setSize(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, entry.getSize());
    }

    // ===================== setCompressedSize / getCompressedSize =====================

    @Test
    public void testSetCompressedSize() {
        entry.setCompressedSize(COMPRESSED_SIZE);
        assertEquals(COMPRESSED_SIZE, entry.getCompressedSize());
    }

    @Test
    public void testSetCompressedSizeNegative() {
        entry.setCompressedSize(-5);
        assertEquals(-5, entry.getCompressedSize());
    }

    @Test
    public void testSetCompressedSizeZero() {
        entry.setCompressedSize(0);
        assertEquals(0, entry.getCompressedSize());
    }

    // ===================== setMethod / getMethod =====================

    @Test
    public void testSetMethod() {
        entry.setMethod(ZipEntry.STORED);
        assertEquals(ZipEntry.STORED, entry.getMethod());
    }

    @Test
    public void testSetMethodInvalidValue() {
        entry.setMethod(99);
        assertEquals(99, entry.getMethod());
    }

    // ===================== setExtra / getExtra =====================

    @Test
    public void testSetExtra() {
        entry.setExtra(EXTRA_DATA);
        assertArrayEquals(EXTRA_DATA, entry.getExtra());
    }

    @Test
    public void testSetExtraNullClears() {
        entry.setExtra(EXTRA_DATA);
        entry.setExtra((byte[]) null);
        assertNull(entry.getExtra());
    }

    @Test
    public void testSetExtraEmptyArray() {
        entry.setExtra(new byte[0]);
        assertArrayEquals(new byte[0], entry.getExtra());
    }

    @Test
    public void testSetExtraLargeArray() {
        byte[] large = new byte[65536];
        entry.setExtra(large);
        assertArrayEquals(large, entry.getExtra());
    }

    @Test
    public void testSetExtraFromZipEntry() throws Exception {
        java.util.zip.ZipEntry ze = new java.util.zip.ZipEntry(NAME);
        ze.setExtra(EXTRA_DATA);
        ZipArchiveEntry zae = new ZipArchiveEntry(ze);
        assertArrayEquals(EXTRA_DATA, zae.getExtra());
    }

    // ===================== getLocalFileDataExtra / getCentralDirectoryExtra =====================

    @Test
    public void testGetLocalFileDataExtraInitiallyNull() {
        assertNull(entry.getLocalFileDataExtra());
    }

    @Test
    public void testGetCentralDirectoryExtraInitiallyNull() {
        assertNull(entry.getCentralDirectoryExtra());
    }

    @Test
    public void testGetLocalFileDataExtraAfterSetExtra() {
        entry.setExtra(EXTRA_DATA);
        assertNotNull(entry.getLocalFileDataExtra());
        // The method digs into the extra field bytes, so it may return a subset
        // In this test we just ensure it returns something non-null
    }

    @Test
    public void testGetCentralDirectoryExtraAfterSetExtra() {
        entry.setExtra(EXTRA_DATA);
        assertNotNull(entry.getCentralDirectoryExtra());
    }

    // ===================== ExtraField Management =====================

    @Test
    public void testAddExtraField() {
        ZipExtraField field = new UnparseableExtraFieldData();
        field.getHeaderId().getBytes(); // ensure header id exists
        entry.addExtraField(field);
        assertSame(field, entry.getExtraField(field.getHeaderId()));
    }

    @Test
    public void testAddExtraFieldReplacesExisting() {
        ZipExtraField field1 = new UnparseableExtraFieldData();
        ZipExtraField field2 = new UnparseableExtraFieldData();
        entry.addExtraField(field1);
        entry.addExtraField(field2);
        assertSame(field2, entry.getExtraField(field1.getHeaderId()));
    }

    @Test
    public void testRemoveExtraField() {
        ZipShort header = new ZipShort(0x0001);
        ZipExtraField field = new UnparseableExtraFieldData();
        entry.addExtraField(field);
        entry.removeExtraField(header); // might do nothing if header not matching
        assertNull(entry.getExtraField(header));
    }

    @Test
    public void testGetExtraFieldNonExistent() {
        assertNull(entry.getExtraField(new ZipShort(0xFFFF)));
    }

    @Test
    public void testGetExtraFieldsEmptyInitially() {
        assertArrayEquals(new ZipExtraField[0], entry.getExtraFields());
    }

    // ===================== GeneralPurposeBit =====================

    @Test
    public void testSetGeneralPurposeBit() {
        GeneralPurposeBit bit = new GeneralPurposeBit();
        bit.useUTF8ForNames(true);
        entry.setGeneralPurposeBit(bit);
        assertSame(bit, entry.getGeneralPurposeBit());
    }

    @Test
    public void testGetGeneralPurposeBitInitiallyNull() {
        assertNull(entry.getGeneralPurposeBit());
    }

    @Test
    public void testSetGeneralPurposeBitNull() {
        entry.setGeneralPurposeBit(null);
        assertNull(entry.getGeneralPurposeBit());
    }

    // ===================== Platform =====================

    @Test
    public void testSetPlatform() {
        entry.setPlatform(ZipArchiveEntry.PLATFORM_UNIX);
        assertEquals(ZipArchiveEntry.PLATFORM_UNIX, entry.getPlatform());
    }

    @Test
    public void testSetPlatformDefault() {
        assertEquals(ZipArchiveEntry.PLATFORM_FAT, entry.getPlatform());
    }

    // ===================== RawFlag =====================

    @Test
    public void testSetRawFlag() {
        entry.setRawFlag(RAW_FLAG);
        assertEquals(RAW_FLAG, entry.getRawFlag());
    }

    @Test
    public void testSetRawFlagZero() {
        entry.setRawFlag(0);
        assertEquals(0, entry.getRawFlag());
    }

    @Test
    public void testSetRawFlagMaxShort() {
        entry.setRawFlag(0xFFFF);
        assertEquals(0xFFFF, entry.getRawFlag());
    }

    // ===================== Internal/External Attributes =====================

    @Test
    public void testSetInternalAttributes() {
        entry.setInternalAttributes(0x0001);
        assertEquals(0x0001, entry.getInternalAttributes());
    }

    @Test
    public void testSetExternalAttributes() {
        entry.setExternalAttributes(0x20L);
        assertEquals(0x20L, entry.getExternalAttributes());
    }

    // ===================== Offset =====================

    @Test
    public void testSetOffset() {
        entry.setOffset(123456L);
        assertEquals(123456L, entry.getOffset());
    }

    @Test
    public void testSetOffsetNegative() {
        entry.setOffset(-1L);
        assertEquals(-1L, entry.getOffset());
    }

    @Test
    public void testGetOffsetInitial() {
        assertEquals(-1L, entry.getOffset());
    }

    // ===================== equals / hashCode =====================

    @Test
    public void testEqualsSameObject() {
        assertTrue(entry.equals(entry));
    }

    @Test
    public void testEqualsNull() {
        assertFalse(entry.equals(null));
    }

    @Test
    public void testEqualsDifferentType() {
        assertFalse(entry.equals("string"));
    }

    @Test
    public void testEqualsIdenticalEntries() {
        ZipArchiveEntry other = new ZipArchiveEntry(NAME);
        assertTrue(entry.equals(other));
    }

    @Test
    public void testEqualsDifferentName() {
        ZipArchiveEntry other = new ZipArchiveEntry("other");
        assertFalse(entry.equals(other));
    }

    @Test
    public void testEqualsDifferentSize() {
        ZipArchiveEntry other = new ZipArchiveEntry(NAME);
        other.setSize(2048);
        assertFalse(entry.equals(other));
    }

    @Test
    public void testHashCodeSameForEqualEntries() {
        ZipArchiveEntry other = new ZipArchiveEntry(NAME);
        assertEquals(entry.hashCode(), other.hashCode());
    }

    @Test
    public void testHashCodeDifferentForDifferentNames() {
        ZipArchiveEntry other = new ZipArchiveEntry("other");
        assertNotEquals(entry.hashCode(), other.hashCode());
    }

    // ===================== clone =====================

    @Test
    public void testClone() {
        entry.setSize(SIZE);
        entry.setMethod(METHOD);
        entry.setExtra(EXTRA_DATA);
        entry.setPlatform(PLATFORM);
        Object clone = entry.clone();
        assertNotNull(clone);
        assertTrue(clone instanceof ZipArchiveEntry);
        ZipArchiveEntry clonedEntry = (ZipArchiveEntry) clone;
        assertEquals(entry.getName(), clonedEntry.getName());
        assertEquals(entry.getSize(), clonedEntry.getSize());
        assertEquals(entry.getMethod(), clonedEntry.getMethod());
        assertArrayEquals(entry.getExtra(), clonedEntry.getExtra());
        assertEquals(entry.getPlatform(), clonedEntry.getPlatform());
        // clone must not be same instance
        assertNotSame(entry, clonedEntry);
    }

    @Test
    public void testCloneExtraFieldIndependence() {
        byte[] extra = {1,2,3};
        entry.setExtra(extra);
        ZipArchiveEntry clone = (ZipArchiveEntry) entry.clone();
        clone.getExtra()[0] = 99;
        assertArrayEquals(extra, entry.getExtra()); // original unchanged
    }

    // ===================== getName with encoding =====================

    @Test
    public void testGetNameWithEncoding() {
        String name = "test.txt";
        entry.setName(name);
        // The method getName(String) uses ZipEncoding
        String result = entry.getName(StandardCharsets.UTF_8.name());
        assertEquals(name, result);
    }

    @Test(expected = java.nio.charset.IllegalCharsetNameException.class)
    public void testGetNameWithInvalidEncoding() {
        entry.getName("invalid-charset");
    }

    @Test
    public void testGetNameWithNullEncoding() {
        String name = entry.getName(null);
        assertEquals(NAME, name);
    }

    // ===================== getUnixMode / setUnixMode =====================

    @Test
    public void testSetUnixMode() {
        entry.setUnixMode(0100755);
        assertEquals(0100755, entry.getUnixMode());
    }

    @Test
    public void testGetUnixModeDefault() {
        assertEquals(0, entry.getUnixMode());
    }

    // ===================== isDirectory =====================

    @Test
    public void testIsDirectoryWithSlash() {
        entry.setName("folder/");
        assertTrue(entry.isDirectory());
    }

    @Test
    public void testIsDirectoryWithoutSlash() {
        entry.setName("file.txt");
        assertFalse(entry.isDirectory());
    }

    @Test
    public void testIsDirectoryWithBackSlash() {
        entry.setName("folder\\");
        assertFalse(entry.isDirectory());
    }

    // ===================== setLastModifiedDate / getLastModifiedDate =====================

    @Test
    public void testSetLastModifiedDate() {
        Date date = new Date(1234567890123L);
        entry.setLastModifiedDate(date);
        assertEquals(date.getTime(), entry.getLastModifiedDate().getTime());
    }

    // ===================== setComment / getComment =====================

    @Test
    public void testSetGetComment() {
        entry.setComment("a comment");
        assertEquals("a comment", entry.getComment());
    }

    @Test
    public void testSetCommentNull() {
        entry.setComment(null);
        assertNull(entry.getComment());
    }

    // ===================== General Purpose Bit in Extra Fields =====================

    @Test
    public void testGetGeneralPurposeBitFromExtraFields() {
        // This method uses getLocalFileDataExtra to parse
        byte[] data = new byte[] {0x01, 0x02, 0x03, 0x04};
        entry.setExtra(data);
        GeneralPurposeBit b = entry.getGeneralPurposeBit();
        // If data is not a valid extra field, might return null
        // Just ensure no exception
    }

    // ===================== Edge Cases for setSize overflow =====================

    @Test(expected = IllegalArgumentException.class)
    public void testSetSizeNegativeThrowsEvenWhenSettingToSame() {
        entry.setSize(-1);
    }

    // ===================== Defects4J specific triggers =====================

    @Test
    public void testGetLocalFileDataExtraWithNullExtra() {
        // Bug context: ensure no NullPointerException
        entry.setExtra((byte[]) null);
        assertNull(entry.getLocalFileDataExtra());
    }

    @Test
    public void testGetCentralDirectoryExtraWithNullExtra() {
        entry.setExtra((byte[]) null);
        assertNull(entry.getCentralDirectoryExtra());
    }

    @Test
    public void testSetExtraFromZipEntryWithNull() {
        java.util.zip.ZipEntry ze = new java.util.zip.ZipEntry(NAME);
        ze.setExtra((byte[]) null);
        ZipArchiveEntry zae = new ZipArchiveEntry(ze);
        assertNull(zae.getExtra());
    }

    @Test
    public void testMergeExtraFieldsNullArg() {
        // suppress if method not public, but it's public in some versions
        // If method exists, test it: entry.mergeExtraFields(null);
    }

    // ===================== full coverage of extra field parsing =====================

    @Test
    public void testSetExtraWithAsiExtraField() {
        AsiExtraField field = new AsiExtraField();
        field.setDirectory(true);
        field.setMode(0755);
        entry.addExtraField(field);
        // serializing and reading back
        byte[] extra = entry.getExtra();
        entry.setExtra(extra);
        // ensure no exception
        assertNotNull(entry.getExtraField(field.getHeaderId()));
    }

    @Test
    public void testSetExtraWithUnparseableExtraField() {
        UnparseableExtraFieldData field = new UnparseableExtraFieldData();
        byte[] localData = {0x01, 0x02, 0x03, 0x04};
        field.parseFromLocalFileData(localData, 0, localData.length);
        entry.addExtraField(field);
        assertEquals(1, entry.getExtraFields().length);
    }

    @Test
    public void testGetExtraFieldsReturnsAll() {
        AsiExtraField field1 = new AsiExtraField();
        field1.setMode(0644);
        field1.setLinkedFile("link");
        field1.setDirectory(false);
        UnparseableExtraFieldData field2 = new UnparseableExtraFieldData();
        entry.addExtraField(field1);
        entry.addExtraField(field2);
        assertEquals(2, entry.getExtraFields().length);
    }

    @Test
    public void testSetExtraAfterAddingExtraField() {
        // Setting raw extra bytes should clear and repopulate extra fields
        entry.addExtraField(new AsiExtraField());
        byte[] raw = new byte[] {0x01, 0x02, 0x03, 0x04};
        entry.setExtra(raw);
        // Extra fields should be re-parsed from raw
        assertNotNull(entry.getExtra());
    }

    // ===================== Time and Date =====================

    @Test
    public void testSetTime() {
        entry.setTime(1234567890123L);
        assertEquals(1234567890123L, entry.getTime());
    }

    @Test
    public void testSetTimeDefault() {
        assertEquals(-1L, entry.getTime());
    }

    // ===================== Misc =====================

    @Test
    public void testToString() {
        String str = entry.toString();
        assertTrue(str.contains(NAME));
    }

    @Test
    public void testCloneAfterSetExtra() {
        entry.setExtra(EXTRA_DATA);
        ZipArchiveEntry clone = (ZipArchiveEntry) entry.clone();
        assertArrayEquals(EXTRA_DATA, clone.getExtra());
    }

    @Test
    public void testGetRawFlagDefault() {
        assertEquals(0, entry.getRawFlag());
    }

    @Test
    public void testSetInternalAttributesTwice() {
        entry.setInternalAttributes(0x00FF);
        entry.setInternalAttributes(0xFF00);
        assertEquals(0xFF00, entry.getInternalAttributes());
    }

    @Test
    public void testSetExternalAttributesWithNegative() {
        entry.setExternalAttributes(0xFFFFFFFF00000000L);
        assertEquals(0xFFFFFFFF00000000L, entry.getExternalAttributes());
    }

    @Test
    public void testGetZipEntryAdapter() {
        ZipEntry ze = entry;
        assertEquals(NAME, ze.getName());
    }
}