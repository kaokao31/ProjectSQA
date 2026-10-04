package org.apache.commons.compress.archivers.zip;

import org.junit.Test;

import static org.junit.Assert.*;

public class Zip64ExtendedInformationExtraFieldTest {

    @Test
    public void testEmptyConstructorAndGetters() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        assertNotNull(field.getHeaderId());
        assertEquals(0x0001, field.getHeaderId().getValue());
        assertNull(field.getSize());
        assertNull(field.getCompressedSize());
        assertNull(field.getRelativeHeaderOffset());
        assertNull(field.getDiskStartNumber());
    }

    @Test
    public void testConstructorWithZipEightByteInteger() {
        ZipEightByteInteger size = new ZipEightByteInteger(100);
        ZipEightByteInteger compSize = new ZipEightByteInteger(80);
        ZipEightByteInteger offset = new ZipEightByteInteger(10);
        ZipLong diskStart = new ZipLong(5);

        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField(
                size, compSize, offset, diskStart);

        assertEquals(size, field.getSize());
        assertEquals(compSize, field.getCompressedSize());
        assertEquals(offset, field.getRelativeHeaderOffset());
        assertEquals(diskStart, field.getDiskStartNumber());
    }

    @Test
    public void testConstructorWithArraysOnly() {
        ZipEightByteInteger size = new ZipEightByteInteger(100);
        ZipEightByteInteger compSize = new ZipEightByteInteger(80);

        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField(
                size, compSize);

        assertEquals(size, field.getSize());
        assertEquals(compSize, field.getCompressedSize());
        assertNull(field.getRelativeHeaderOffset());
        assertNull(field.getDiskStartNumber());
    }

    @Test
    public void testGetLocalFileDataLength() {
        // Size and compressed size present (16 bytes)
        Zip64ExtendedInformationExtraField field1 = new Zip64ExtendedInformationExtraField(
                new ZipEightByteInteger(1), new ZipEightByteInteger(2));
        assertEquals(16, field1.getLocalFileDataLength().getValue());

        // All fields present (24 or 28? Let's check implementation: size(8) + compSize(8) + offset(8) + disk(4) = 28)
        Zip64ExtendedInformationExtraField field2 = new Zip64ExtendedInformationExtraField(
                new ZipEightByteInteger(1), new ZipEightByteInteger(2),
                new ZipEightByteInteger(3), new ZipLong(4));
        assertEquals(28, field2.getLocalFileDataLength().getValue());

        // Only offset present (via setters or constructor if possible, but constructor requires size/compSize if using 4-arg)
        Zip64ExtendedInformationExtraField field3 = new Zip64ExtendedInformationExtraField();
        field3.setRelativeHeaderOffset(new ZipEightByteInteger(10));
        assertEquals(8, field3.getLocalFileDataLength().getValue());

        // Only disk start present
        Zip64ExtendedInformationExtraField field4 = new Zip64ExtendedInformationExtraField();
        field4.setDiskStartNumber(new ZipLong(1));
        assertEquals(4, field4.getLocalFileDataLength().getValue());

        // Empty
        Zip64ExtendedInformationExtraField field5 = new Zip64ExtendedInformationExtraField();
        assertEquals(0, field5.getLocalFileDataLength().getValue());
    }

    @Test
    public void testGetCentralDirectoryLength() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField(
                new ZipEightByteInteger(1), new ZipEightByteInteger(2),
                new ZipEightByteInteger(3), new ZipLong(4));
        assertEquals(field.getLocalFileDataLength(), field.getCentralDirectoryLength());
    }

    @Test
    public void testGetHeaderId() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        assertEquals(Zip64ExtendedInformationExtraField.HEADER_ID, field.getHeaderId());
    }

    @Test
    public void testParseFromLocalFileDataComplete() {
        // Construct a byte array containing size (8 bytes), compSize (8 bytes), offset (8 bytes), diskStart (4 bytes)
        byte[] data = new byte[28];
        // size = 1
        System.arraycopy(ZipEightByteInteger.getBytes(1), 0, data, 0, 8);
        // compSize = 2
        System.arraycopy(ZipEightByteInteger.getBytes(2), 0, data, 8, 8);
        // offset = 3
        System.arraycopy(ZipEightByteInteger.getBytes(3), 0, data, 16, 8);
        // diskStart = 4
        System.arraycopy(ZipLong.getBytes(4), 0, data, 24, 4);

        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromLocalFileData(data, 0, data.length);

        assertEquals(1, field.getSize().getLongValue());
        assertEquals(2, field.getCompressedSize().getLongValue());
        assertEquals(3, field.getRelativeHeaderOffset().getLongValue());
        assertEquals(4, field.getDiskStartNumber().getValue());
    }

    @Test
    public void testParseFromLocalFileDataPartialSizes() {
        // Only size and compressed size (16 bytes)
        byte[] data = new byte[16];
        System.arraycopy(ZipEightByteInteger.getBytes(10), 0, data, 0, 8);
        System.arraycopy(ZipEightByteInteger.getBytes(20), 0, data, 8, 8);

        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromLocalFileData(data, 0, data.length);

        assertEquals(10, field.getSize().getLongValue());
        assertEquals(20, field.getCompressedSize().getLongValue());
        assertNull(field.getRelativeHeaderOffset());
        assertNull(field.getDiskStartNumber());
    }

    @Test
    public void testParseFromLocalFileDataOnlyOffset() {
        // Only offset (8 bytes)
        byte[] data = new byte[8];
        System.arraycopy(ZipEightByteInteger.getBytes(55), 0, data, 0, 8);

        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromLocalFileData(data, 0, data.length);

        assertNull(field.getSize());
        assertNull(field.getCompressedSize());
        assertEquals(55, field.getRelativeHeaderOffset().getLongValue());
        assertNull(field.getDiskStartNumber());
    }

    @Test
    public void testParseFromLocalFileDataOnlyDiskStart() {
        // Only disk start (4 bytes)
        byte[] data = new byte[4];
        System.arraycopy(ZipLong.getBytes(77), 0, data, 0, 4);

        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromLocalFileData(data, 0, data.length);

        assertNull(field.getSize());
        assertNull(field.getCompressedSize());
        assertNull(field.getRelativeHeaderOffset());
        assertEquals(77, field.getDiskStartNumber().getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseFromLocalFileDataInvalidLength() {
        // Length is 15, which doesn't match expected standard configurations (not 0, 8, 16, 24, 28, etc. properly aligned)
        // Specifically, let's check what Zip64ExtendedInformationExtraField expects. 
        // Usually, if length > 0 and < 16, or if leftover bytes... Let's trigger the exception.
        byte[] data = new byte[15];
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromLocalFileData(data, 0, data.length);
    }

    @Test
    public void testGetLocalFileDataData() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField(
                new ZipEightByteInteger(10), new ZipEightByteInteger(20),
                new ZipEightByteInteger(30), new ZipLong(40));

        byte[] data = field.getLocalFileDataData();
        assertNotNull(data);
        assertEquals(28, data.length);

        Zip64ExtendedInformationExtraField field2 = new Zip64ExtendedInformationExtraField();
        field2.parseFromLocalFileData(data, 0, data.length);
        assertEquals(field.getSize(), field2.getSize());
        assertEquals(field.getCompressedSize(), field2.getCompressedSize());
        assertEquals(field.getRelativeHeaderOffset(), field2.getRelativeHeaderOffset());
        assertEquals(field.getDiskStartNumber(), field2.getDiskStartNumber());
    }

    @Test
    public void testGetCentralDirectoryDataWithReassignedFields() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.setRelativeHeaderOffset(new ZipEightByteInteger(500));
        byte[] data = field.getCentralDirectoryData();
        assertNotNull(data);
        assertEquals(8, data.length);
    }

    @Test
    public void testReificationAndSetters() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.setSize(new ZipEightByteInteger(99));
        field.setCompressedSize(new ZipEightByteInteger(88));
        field.setRelativeHeaderOffset(new ZipEightByteInteger(77));
        field.setDiskStartNumber(new ZipLong(66));

        assertEquals(99, field.getSize().getLongValue());
        assertEquals(88, field.getCompressedSize().getLongValue());
        assertEquals(77, field.getRelativeHeaderOffset().getLongValue());
        assertEquals(66, field.getDiskStartNumber().getValue());
    }
}