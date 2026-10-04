package org.apache.commons.compress.archivers.zip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Test suite for Zip64ExtendedInformationExtraField.
 * Covers all methods and edge cases, including parsing, serialization,
 * and reparse scenarios to expose potential bugs.
 */
public class Zip64ExtendedInformationExtraFieldTest {

    /* ---------- Helper methods ---------- */

    private ZipEightByteInteger createZip8(byte[] bytes, int offset) {
        return new ZipEightByteInteger(bytes, offset);
    }

    private ZipLong createZip4(byte[] bytes, int offset) {
        return new ZipLong(bytes, offset);
    }

    private byte[] createLocalData(boolean includeSize, boolean includeCompressedSize) {
        int length = (includeSize ? 8 : 0) + (includeCompressedSize ? 8 : 0);
        byte[] data = new byte[length];
        int offset = 0;
        if (includeSize) {
            for (int i = 0; i < 8; i++) {
                data[offset++] = (byte) i;
            }
        }
        if (includeCompressedSize) {
            for (int i = 0; i < 8; i++) {
                data[offset++] = (byte) (i + 10);
            }
        }
        return data;
    }

    private byte[] createCentralData(boolean includeSize, boolean includeCompressedSize,
                                     boolean includeRelativeOffset, boolean includeDiskStart) {
        int length = (includeSize ? 8 : 0) + (includeCompressedSize ? 8 : 0)
                + (includeRelativeOffset ? 8 : 0) + (includeDiskStart ? 4 : 0);
        byte[] data = new byte[length];
        int offset = 0;
        if (includeSize) {
            for (int i = 0; i < 8; i++) data[offset++] = (byte) (i + 20);
        }
        if (includeCompressedSize) {
            for (int i = 0; i < 8; i++) data[offset++] = (byte) (i + 30);
        }
        if (includeRelativeOffset) {
            for (int i = 0; i < 8; i++) data[offset++] = (byte) (i + 40);
        }
        if (includeDiskStart) {
            for (int i = 0; i < 4; i++) data[offset++] = (byte) (i + 50);
        }
        return data;
    }

    /* ---------- Constructors and getters ---------- */

    @Test
    public void testDefaultConstructor() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        assertNull(field.getSize());
        assertNull(field.getCompressedSize());
        assertNull(field.getRelativeHeaderOffset());
        assertNull(field.getDiskStart());
    }

    @Test
    public void testConstructorWithValues() {
        ZipEightByteInteger size = new ZipEightByteInteger(100L);
        ZipEightByteInteger compSize = new ZipEightByteInteger(200L);
        ZipEightByteInteger offset = new ZipEightByteInteger(300L);
        ZipLong disk = new ZipLong(4L);
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField(size, compSize, offset, disk);
        assertEquals(size, field.getSize());
        assertEquals(compSize, field.getCompressedSize());
        assertEquals(offset, field.getRelativeHeaderOffset());
        assertEquals(disk, field.getDiskStart());
    }

    /* ---------- Setters ---------- */

    @Test
    public void testSetSize() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        ZipEightByteInteger size = new ZipEightByteInteger(42L);
        field.setSize(size);
        assertEquals(size, field.getSize());
    }

    @Test
    public void testSetCompressedSize() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        ZipEightByteInteger comp = new ZipEightByteInteger(43L);
        field.setCompressedSize(comp);
        assertEquals(comp, field.getCompressedSize());
    }

    @Test
    public void testSetRelativeHeaderOffset() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        ZipEightByteInteger offset = new ZipEightByteInteger(44L);
        field.setRelativeHeaderOffset(offset);
        assertEquals(offset, field.getRelativeHeaderOffset());
    }

    @Test
    public void testSetDiskStart() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        ZipLong disk = new ZipLong(5L);
        field.setDiskStart(disk);
        assertEquals(disk, field.getDiskStart());
    }

    /* ---------- Header ID ---------- */

    @Test
    public void testGetHeaderId() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        assertEquals(0x0001, field.getHeaderId().getValue());
    }

    /* ---------- Local file data length ---------- */

    @Test
    public void testLocalFileDataLengthBothNull() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        assertEquals(0, field.getLocalFileDataLength().getValue());
    }

    @Test
    public void testLocalFileDataLengthSizeOnly() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.setSize(new ZipEightByteInteger(100L));
        assertEquals(8, field.getLocalFileDataLength().getValue());
    }

    @Test
    public void testLocalFileDataLengthCompressedSizeOnly() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.setCompressedSize(new ZipEightByteInteger(200L));
        assertEquals(8, field.getLocalFileDataLength().getValue());
    }

    @Test
    public void testLocalFileDataLengthBoth() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.setSize(new ZipEightByteInteger(100L));
        field.setCompressedSize(new ZipEightByteInteger(200L));
        assertEquals(16, field.getLocalFileDataLength().getValue());
    }

    /* ---------- Local file data bytes ---------- */

    @Test
    public void testLocalFileDataBytesBothNull() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        assertArrayEquals(new byte[0], field.getLocalFileDataData());
    }

    @Test
    public void testLocalFileDataBytesSizeOnly() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        byte[] expected = new byte[8];
        for (int i = 0; i < 8; i++) expected[i] = (byte) i;
        field.setSize(new ZipEightByteInteger(expected));
        assertArrayEquals(expected, field.getLocalFileDataData());
    }

    @Test
    public void testLocalFileDataBytesCompressedSizeOnly() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        byte[] expected = new byte[8];
        for (int i = 0; i < 8; i++) expected[i] = (byte) (i + 10);
        field.setCompressedSize(new ZipEightByteInteger(expected));
        assertArrayEquals(expected, field.getLocalFileDataData());
    }

    @Test
    public void testLocalFileDataBytesBoth() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        byte[] sizeBytes = new byte[8];
        byte[] compBytes = new byte[8];
        byte[] expected = new byte[16];
        for (int i = 0; i < 8; i++) {
            sizeBytes[i] = (byte) i;
            compBytes[i] = (byte) (i + 10);
            expected[i] = sizeBytes[i];
            expected[i + 8] = compBytes[i];
        }
        field.setSize(new ZipEightByteInteger(sizeBytes));
        field.setCompressedSize(new ZipEightByteInteger(compBytes));
        assertArrayEquals(expected, field.getLocalFileDataData());
    }

    /* ---------- Central directory length ---------- */

    @Test
    public void testCentralDirectoryLengthAllNull() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        assertEquals(0, field.getCentralDirectoryLength().getValue());
    }

    @Test
    public void testCentralDirectoryLengthSizeOnly() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.setSize(new ZipEightByteInteger(100L));
        assertEquals(8, field.getCentralDirectoryLength().getValue());
    }

    @Test
    public void testCentralDirectoryLengthSizeAndCompressed() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.setSize(new ZipEightByteInteger(100L));
        field.setCompressedSize(new ZipEightByteInteger(200L));
        assertEquals(16, field.getCentralDirectoryLength().getValue());
    }

    @Test
    public void testCentralDirectoryLengthSizeCompressedAndRelative() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.setSize(new ZipEightByteInteger(100L));
        field.setCompressedSize(new ZipEightByteInteger(200L));
        field.setRelativeHeaderOffset(new ZipEightByteInteger(300L));
        assertEquals(24, field.getCentralDirectoryLength().getValue());
    }

    @Test
    public void testCentralDirectoryLengthAllFields() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.setSize(new ZipEightByteInteger(100L));
        field.setCompressedSize(new ZipEightByteInteger(200L));
        field.setRelativeHeaderOffset(new ZipEightByteInteger(300L));
        field.setDiskStart(new ZipLong(4L));
        assertEquals(28, field.getCentralDirectoryLength().getValue());
    }

    /* ---------- Central directory data ---------- */

    @Test
    public void testCentralDirectoryDataAllNull() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        assertArrayEquals(new byte[0], field.getCentralDirectoryData());
    }

    @Test
    public void testCentralDirectoryDataAllFields() {
        byte[] size = {(byte) 0, 1, 2, 3, 4, 5, 6, 7};
        byte[] comp = {(byte) 10, 11, 12, 13, 14, 15, 16, 17};
        byte[] rel = {(byte) 20, 21, 22, 23, 24, 25, 26, 27};
        byte[] disk = {(byte) 30, 31, 32, 33};
        byte[] expected = new byte[28];
        System.arraycopy(size, 0, expected, 0, 8);
        System.arraycopy(comp, 0, expected, 8, 8);
        System.arraycopy(rel, 0, expected, 16, 8);
        System.arraycopy(disk, 0, expected, 24, 4);

        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.setSize(new ZipEightByteInteger(size));
        field.setCompressedSize(new ZipEightByteInteger(comp));
        field.setRelativeHeaderOffset(new ZipEightByteInteger(rel));
        field.setDiskStart(new ZipLong(disk));
        assertArrayEquals(expected, field.getCentralDirectoryData());
    }

    /* ---------- parseFromLocalFileData ---------- */

    @Test
    public void testParseFromLocalFileDataValid() throws Exception {
        byte[] data = createLocalData(true, true);
        int offset = 0;
        int length = data.length;
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromLocalFileData(data, offset, length);
        assertNotNull(field.getSize());
        assertNotNull(field.getCompressedSize());
        assertNull(field.getRelativeHeaderOffset());
        assertNull(field.getDiskStart());
        // Verify values
        byte[] expectedSize = new byte[8];
        byte[] expectedComp = new byte[8];
        for (int i = 0; i < 8; i++) {
            expectedSize[i] = data[i];
            expectedComp[i] = data[i + 8];
        }
        assertArrayEquals(expectedSize, field.getSize().getBytes());
        assertArrayEquals(expectedComp, field.getCompressedSize().getBytes());
    }

    @Test
    public void testParseFromLocalFileDataInsufficientLength() {
        byte[] data = new byte[8];
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        try {
            field.parseFromLocalFileData(data, 0, 8);
            fail("Expected ZipException for insufficient length");
        } catch (Exception e) {
            // expected
        }
    }

    @Test
    public void testParseFromLocalFileDataExtraBytes() throws Exception {
        byte[] data = createLocalData(true, true);
        byte[] full = new byte[data.length + 10];
        System.arraycopy(data, 0, full, 0, data.length);
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromLocalFileData(full, 0, full.length);
        // Should still read only first 16 bytes
        assertNotNull(field.getSize());
        assertNotNull(field.getCompressedSize());
        assertNull(field.getRelativeHeaderOffset());
        assertNull(field.getDiskStart());
    }

    /* ---------- parseFromCentralDirectoryData ---------- */

    @Test
    public void testParseFromCentralDirectoryDataLength16() throws Exception {
        byte[] data = createCentralData(true, true, false, false);
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromCentralDirectoryData(data, 0, data.length);
        assertNotNull(field.getSize());
        assertNotNull(field.getCompressedSize());
        assertNull(field.getRelativeHeaderOffset());
        assertNull(field.getDiskStart());
    }

    @Test
    public void testParseFromCentralDirectoryDataLength24() throws Exception {
        byte[] data = createCentralData(true, true, true, false);
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromCentralDirectoryData(data, 0, data.length);
        assertNotNull(field.getSize());
        assertNotNull(field.getCompressedSize());
        assertNotNull(field.getRelativeHeaderOffset());
        assertNull(field.getDiskStart());
    }

    @Test
    public void testParseFromCentralDirectoryDataLength28() throws Exception {
        byte[] data = createCentralData(true, true, true, true);
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromCentralDirectoryData(data, 0, data.length);
        assertNotNull(field.getSize());
        assertNotNull(field.getCompressedSize());
        assertNotNull(field.getRelativeHeaderOffset());
        assertNotNull(field.getDiskStart());
    }

    @Test
    public void testParseFromCentralDirectoryDataInsufficientLength() {
        byte[] data = new byte[8];
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        try {
            field.parseFromCentralDirectoryData(data, 0, 8);
            fail("Expected ZipException for insufficient length");
        } catch (Exception e) {
            // expected
        }
    }

    /* ---------- reparseCentralDirectoryData ---------- */

    @Test
    public void testReparseCentralDirectoryDataAllTrue() throws Exception {
        byte[] data = createCentralData(true, true, true, true);
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromCentralDirectoryData(data, 0, data.length);
        // Reset size and compressedSize to null (the constructor gives all null)
        // Now call reparse with all true
        field.reparseCentralDirectoryData(true, true, true, true);
        assertNotNull(field.getSize());
        assertNotNull(field.getCompressedSize());
        assertNotNull(field.getRelativeHeaderOffset());
        assertNotNull(field.getDiskStart());
    }

    @Test
    public void testReparseCentralDirectoryDataOnlySize() throws Exception {
        byte[] data = createCentralData(true, true, true, true);
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromCentralDirectoryData(data, 0, data.length);
        field.reparseCentralDirectoryData(true, false, false, false);
        assertNotNull(field.getSize());
        assertNull(field.getCompressedSize());
        assertNull(field.getRelativeHeaderOffset());
        assertNull(field.getDiskStart());
    }

    @Test
    public void testReparseCentralDirectoryDataOnlyCompressedSize() throws Exception {
        byte[] data = createCentralData(true, true, true, true);
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromCentralDirectoryData(data, 0, data.length);
        field.reparseCentralDirectoryData(false, true, false, false);
        assertNull(field.getSize());
        assertNotNull(field.getCompressedSize());
        assertNull(field.getRelativeHeaderOffset());
        assertNull(field.getDiskStart());
    }

    @Test
    public void testReparseCentralDirectoryDataOnlyRelativeOffset() throws Exception {
        byte[] data = createCentralData(true, true, true, true);
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromCentralDirectoryData(data, 0, data.length);
        field.reparseCentralDirectoryData(false, false, true, false);
        assertNull(field.getSize());
        assertNull(field.getCompressedSize());
        assertNotNull(field.getRelativeHeaderOffset());
        assertNull(field.getDiskStart());
    }

    @Test
    public void testReparseCentralDirectoryDataOnlyDiskStart() throws Exception {
        byte[] data = createCentralData(true, true, true, true);
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromCentralDirectoryData(data, 0, data.length);
        field.reparseCentralDirectoryData(false, false, false, true);
        assertNull(field.getSize());
        assertNull(field.getCompressedSize());
        assertNull(field.getRelativeHeaderOffset());
        assertNotNull(field.getDiskStart());
    }

    @Test
    public void testReparseCentralDirectoryDataWithSomeFieldsAlreadySet() throws Exception {
        byte[] data = createCentralData(true, true, true, true);
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromCentralDirectoryData(data, 0, data.length);
        // Set size and diskStart already
        byte[] presized = new byte[8];
        for (int i = 0; i < 8; i++) presized[i] = (byte) 99;
        field.setSize(new ZipEightByteInteger(presized));
        field.setDiskStart(new ZipLong(44L));
        // Now reparse with all true; size and diskStart should not be overwritten
        field.reparseCentralDirectoryData(true, true, true, true);
        // size should remain 99
        assertArrayEquals(presized, field.getSize().getBytes());
        // diskStart should remain 44
        assertEquals(44L, field.getDiskStart().getValue());
        // compressedSize and relativeHeaderOffset should be set
        assertNotNull(field.getCompressedSize());
        assertNotNull(field.getRelativeHeaderOffset());
    }

    @Test
    public void testReparseCentralDirectoryDataNullRawData() throws Exception {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        // No call to parse, rawCentralData should be null
        field.reparseCentralDirectoryData(true, true, true, true);
        // Should do nothing, all remain null
        assertNull(field.getSize());
        assertNull(field.getCompressedSize());
        assertNull(field.getRelativeHeaderOffset());
        assertNull(field.getDiskStart());
    }

    /* ---------- equals and hashCode ---------- */

    @Test
    public void testEqualsAndHashCode() {
        Zip64ExtendedInformationExtraField field1 = new Zip64ExtendedInformationExtraField(
                new ZipEightByteInteger(1L), new ZipEightByteInteger(2L), null, null);
        Zip64ExtendedInformationExtraField field2 = new Zip64ExtendedInformationExtraField(
                new ZipEightByteInteger(1L), new ZipEightByteInteger(2L), null, null);
        assertEquals(field1, field2);
        assertEquals(field1.hashCode(), field2.hashCode());
    }

    @Test
    public void testEqualsNull() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        assertFalse(field.equals(null));
    }

    /* ---------- Clone (if implemented) ---------- */

    @Test
    public void testClone() throws Exception {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField(
                new ZipEightByteInteger(1L), new ZipEightByteInteger(2L), null, null);
        Object clone = field.clone();
        assertNotNull(clone);
        assertTrue(clone instanceof Zip64ExtendedInformationExtraField);
        Zip64ExtendedInformationExtraField copy = (Zip64ExtendedInformationExtraField) clone;
        assertEquals(field, copy);
        // Ensure it's a copy, not the same reference
        assertFalse(field == copy);
    }

    /* ---------- Additional edge cases ---------- */

    @Test
    public void testZeroValues() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField(
                new ZipEightByteInteger(0L), new ZipEightByteInteger(0L), new ZipEightByteInteger(0L), new ZipLong(0L));
        assertEquals(0L, field.getSize().getValue());
        assertEquals(0L, field.getCompressedSize().getValue());
        assertEquals(0L, field.getRelativeHeaderOffset().getValue());
        assertEquals(0L, field.getDiskStart().getValue());
        // Check lengths still correct
        assertEquals(16, field.getLocalFileDataLength().getValue());
        assertEquals(28, field.getCentralDirectoryLength().getValue());
    }
}