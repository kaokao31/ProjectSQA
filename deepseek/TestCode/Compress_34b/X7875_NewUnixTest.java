package org.apache.commons.compress.archivers.zip;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for X7875_NewUnix, targeting maximum coverage and fault detection.
 * Based on Defects4J bug 34 (Compress).
 */
public class X7875_NewUnixTest {

    private X7875_NewUnix zipField;

    @Before
    public void setUp() {
        zipField = new X7875_NewUnix();
    }

    // ========== Default constructor ==========
    @Test
    public void testDefaultValues() {
        assertEquals("Default atime should be 0", 0L, zipField.getAccessTime());
        assertEquals("Default mtime should be 0", 0L, zipField.getModifyTime());
        assertEquals("Default uid should be 0", 0L, zipField.getUID());
        assertEquals("Default gid should be 0", 0L, zipField.getGID());
        assertNotNull("Header ID should not be null", zipField.getHeaderId());
        assertEquals("Header ID should be 0x7875", 0x7875, zipField.getHeaderId().getValue());
    }

    // ========== Setter/Getter for timestamps ==========
    @Test
    public void testSetAccessTimeZero() {
        zipField.setAccessTime(0L);
        assertEquals(0L, zipField.getAccessTime());
    }

    @Test
    public void testSetAccessTimePositive() {
        zipField.setAccessTime(123456789L);
        assertEquals(123456789L, zipField.getAccessTime());
    }

    @Test
    public void testSetAccessTimeNegative() {
        zipField.setAccessTime(-1L);
        assertEquals(-1L, zipField.getAccessTime());
    }

    @Test
    public void testSetAccessTimeMinValue() {
        zipField.setAccessTime(Long.MIN_VALUE);
        assertEquals(Long.MIN_VALUE, zipField.getAccessTime());
    }

    @Test
    public void testSetAccessTimeMaxValue() {
        zipField.setAccessTime(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, zipField.getAccessTime());
    }

    @Test
    public void testSetModifyTimeZero() {
        zipField.setModifyTime(0L);
        assertEquals(0L, zipField.getModifyTime());
    }

    @Test
    public void testSetModifyTimePositive() {
        zipField.setModifyTime(987654321L);
        assertEquals(987654321L, zipField.getModifyTime());
    }

    @Test
    public void testSetModifyTimeNegative() {
        zipField.setModifyTime(-100L);
        assertEquals(-100L, zipField.getModifyTime());
    }

    @Test
    public void testSetModifyTimeMinValue() {
        zipField.setModifyTime(Long.MIN_VALUE);
        assertEquals(Long.MIN_VALUE, zipField.getModifyTime());
    }

    @Test
    public void testSetModifyTimeMaxValue() {
        zipField.setModifyTime(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, zipField.getModifyTime());
    }

    // ========== Setter/Getter for UID/GID ==========
    @Test
    public void testSetUIDZero() {
        zipField.setUID(0L);
        assertEquals(0L, zipField.getUID());
    }

    @Test
    public void testSetUIDPositive() {
        zipField.setUID(1000L);
        assertEquals(1000L, zipField.getUID());
    }

    @Test
    public void testSetUIDNegative() {
        zipField.setUID(-1L);
        assertEquals(-1L, zipField.getUID());
    }

    @Test
    public void testSetUIDLargeUnsigned() {
        // UID stored as long but may be truncated to int in some implementations
        zipField.setUID(0xFFFFFFFFL);
        assertEquals(0xFFFFFFFFL, zipField.getUID());
    }

    @Test
    public void testSetGIDZero() {
        zipField.setGID(0L);
        assertEquals(0L, zipField.getGID());
    }

    @Test
    public void testSetGIDPositive() {
        zipField.setGID(500L);
        assertEquals(500L, zipField.getGID());
    }

    @Test
    public void testSetGIDNegative() {
        zipField.setGID(-2L);
        assertEquals(-2L, zipField.getGID());
    }

    @Test
    public void testSetGIDLargeUnsigned() {
        zipField.setGID(0x100000000L);
        assertEquals(0x100000000L, zipField.getGID());
    }

    // ========== Serialization round-trip ==========
    @Test
    public void testRoundTripDefault() throws Exception {
        byte[] localData = zipField.getLocalFileDataData();
        X7875_NewUnix parsed = new X7875_NewUnix();
        parsed.parseFromLocalFileData(localData, 0, localData.length);
        assertEquals(zipField.getAccessTime(), parsed.getAccessTime());
        assertEquals(zipField.getModifyTime(), parsed.getModifyTime());
        assertEquals(zipField.getUID(), parsed.getUID());
        assertEquals(zipField.getGID(), parsed.getGID());
    }

    @Test
    public void testRoundTripWithValues() throws Exception {
        zipField.setAccessTime(123456789L);
        zipField.setModifyTime(987654321L);
        zipField.setUID(1000L);
        zipField.setGID(500L);
        byte[] localData = zipField.getLocalFileDataData();
        X7875_NewUnix parsed = new X7875_NewUnix();
        parsed.parseFromLocalFileData(localData, 0, localData.length);
        assertEquals(123456789L, parsed.getAccessTime());
        assertEquals(987654321L, parsed.getModifyTime());
        assertEquals(1000L, parsed.getUID());
        assertEquals(500L, parsed.getGID());
    }

    @Test
    public void testRoundTripNegativeTimestamps() throws Exception {
        zipField.setAccessTime(-1L);
        zipField.setModifyTime(-100L);
        zipField.setUID(0L);
        zipField.setGID(0L);
        byte[] localData = zipField.getLocalFileDataData();
        X7875_NewUnix parsed = new X7875_NewUnix();
        parsed.parseFromLocalFileData(localData, 0, localData.length);
        assertEquals(-1L, parsed.getAccessTime());
        assertEquals(-100L, parsed.getModifyTime());
    }

    @Test
    public void testRoundTripMinMaxTimestamps() throws Exception {
        zipField.setAccessTime(Long.MIN_VALUE);
        zipField.setModifyTime(Long.MAX_VALUE);
        zipField.setUID(0L);
        zipField.setGID(0L);
        byte[] localData = zipField.getLocalFileDataData();
        X7875_NewUnix parsed = new X7875_NewUnix();
        parsed.parseFromLocalFileData(localData, 0, localData.length);
        assertEquals(Long.MIN_VALUE, parsed.getAccessTime());
        assertEquals(Long.MAX_VALUE, parsed.getModifyTime());
    }

    @Test
    public void testRoundTripLargeUIDGID() throws Exception {
        zipField.setAccessTime(0L);
        zipField.setModifyTime(0L);
        zipField.setUID(0xFFFFFFFFL);
        zipField.setGID(0x100000000L);
        byte[] localData = zipField.getLocalFileDataData();
        X7875_NewUnix parsed = new X7875_NewUnix();
        parsed.parseFromLocalFileData(localData, 0, localData.length);
        assertEquals(0xFFFFFFFFL, parsed.getUID());
        assertEquals(0x100000000L, parsed.getGID());
    }

    // ========== Central directory round-trip ==========
    @Test
    public void testCentralDirectoryRoundTrip() throws Exception {
        zipField.setAccessTime(111L);
        zipField.setModifyTime(222L);
        zipField.setUID(33L);
        zipField.setGID(44L);
        byte[] centralData = zipField.getCentralDirectoryData();
        X7875_NewUnix parsed = new X7875_NewUnix();
        parsed.parseFromCentralDirectoryData(centralData, 0, centralData.length);
        assertEquals(111L, parsed.getAccessTime());
        assertEquals(222L, parsed.getModifyTime());
        assertEquals(33L, parsed.getUID());
        assertEquals(44L, parsed.getGID());
    }

    // ========== Length methods ==========
    @Test
    public void testLocalFileDataLength() {
        // Length depends on UID/GID size; default should be minimal
        assertTrue("Local data length should be > 0", zipField.getLocalFileDataLength().getValue() > 0);
    }

    @Test
    public void testCentralDirectoryLength() {
        assertTrue("Central directory length should be > 0", zipField.getCentralDirectoryLength().getValue() > 0);
    }

    @Test
    public void testLocalAndCentralLengthsEqual() {
        assertEquals("Local and central lengths should be equal",
                zipField.getLocalFileDataLength().getValue(),
                zipField.getCentralDirectoryLength().getValue());
    }

    // ========== Parsing edge cases ==========
    @Test(expected = IllegalArgumentException.class)
    public void testParseFromLocalFileDataNullData() {
        zipField.parseFromLocalFileData(null, 0, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseFromLocalFileDataNegativeOffset() {
        byte[] data = new byte[10];
        zipField.parseFromLocalFileData(data, -1, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseFromLocalFileDataNegativeLength() {
        byte[] data = new byte[10];
        zipField.parseFromLocalFileData(data, 0, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseFromLocalFileDataTooShort() {
        byte[] data = new byte[2]; // Minimum length is > 2
        zipField.parseFromLocalFileData(data, 0, data.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseFromCentralDirectoryDataNullData() {
        zipField.parseFromCentralDirectoryData(null, 0, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseFromCentralDirectoryDataNegativeOffset() {
        byte[] data = new byte[10];
        zipField.parseFromCentralDirectoryData(data, -1, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseFromCentralDirectoryDataNegativeLength() {
        byte[] data = new byte[10];
        zipField.parseFromCentralDirectoryData(data, 0, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseFromCentralDirectoryDataTooShort() {
        byte[] data = new byte[2];
        zipField.parseFromCentralDirectoryData(data, 0, data.length);
    }

    // ========== Header ID ==========
    @Test
    public void testHeaderIdValue() {
        assertEquals(0x7875, zipField.getHeaderId().getValue());
    }

    // ========== Clone (if applicable) ==========
    @Test
    public void testClone() throws CloneNotSupportedException {
        zipField.setAccessTime(10L);
        zipField.setModifyTime(20L);
        zipField.setUID(30L);
        zipField.setGID(40L);
        X7875_NewUnix clone = (X7875_NewUnix) zipField.clone();
        assertNotNull("Clone should not be null", clone);
        assertEquals(zipField.getAccessTime(), clone.getAccessTime());
        assertEquals(zipField.getModifyTime(), clone.getModifyTime());
        assertEquals(zipField.getUID(), clone.getUID());
        assertEquals(zipField.getGID(), clone.getGID());
    }

    // ========== Additional fault detection: negative timestamps in central directory ==========
    @Test
    public void testCentralDirectoryNegativeTimestamps() throws Exception {
        zipField.setAccessTime(-1L);
        zipField.setModifyTime(-2L);
        byte[] centralData = zipField.getCentralDirectoryData();
        X7875_NewUnix parsed = new X7875_NewUnix();
        parsed.parseFromCentralDirectoryData(centralData, 0, centralData.length);
        assertEquals(-1L, parsed.getAccessTime());
        assertEquals(-2L, parsed.getModifyTime());
    }

    // ========== Test that getLocalFileDataData and getCentralDirectoryData produce same bytes ==========
    @Test
    public void testLocalAndCentralDataEqual() {
        zipField.setAccessTime(123L);
        zipField.setModifyTime(456L);
        zipField.setUID(789L);
        zipField.setGID(101112L);
        assertArrayEquals("Local and central data should be identical",
                zipField.getLocalFileDataData(),
                zipField.getCentralDirectoryData());
    }

    // ========== Test parsing with offset > 0 ==========
    @Test
    public void testParseWithOffset() throws Exception {
        byte[] fullData = new byte[100];
        // Fill with dummy data
        for (int i = 0; i < 100; i++) {
            fullData[i] = (byte) i;
        }
        // Create a valid X7875_NewUnix data at offset 10
        X7875_NewUnix original = new X7875_NewUnix();
        original.setAccessTime(999L);
        original.setModifyTime(888L);
        original.setUID(77L);
        original.setGID(66L);
        byte[] originalData = original.getLocalFileDataData();
        System.arraycopy(originalData, 0, fullData, 10, originalData.length);

        X7875_NewUnix parsed = new X7875_NewUnix();
        parsed.parseFromLocalFileData(fullData, 10, originalData.length);
        assertEquals(999L, parsed.getAccessTime());
        assertEquals(888L, parsed.getModifyTime());
        assertEquals(77L, parsed.getUID());
        assertEquals(66L, parsed.getGID());
    }

    // ========== Test that setting UID/GID to large values does not cause overflow ==========
    @Test
    public void testLargeUIDGIDNoOverflow() {
        zipField.setUID(0xFFFFFFFFL);
        zipField.setGID(0xFFFFFFFFL);
        assertEquals(0xFFFFFFFFL, zipField.getUID());
        assertEquals(0xFFFFFFFFL, zipField.getGID());
    }

    // ========== Test that negative UID/GID are preserved ==========
    @Test
    public void testNegativeUIDGID() {
        zipField.setUID(-1L);
        zipField.setGID(-100L);
        assertEquals(-1L, zipField.getUID());
        assertEquals(-100L, zipField.getGID());
    }
}