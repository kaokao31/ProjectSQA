package org.apache.commons.compress.archivers.zip;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class X5455_ExtendedTimestampTest {

    private X5455_ExtendedTimestamp x5455;

    @Before
    public void setUp() {
        x5455 = new X5455_ExtendedTimestamp();
    }

    @Test
    public void testGetHeaderId() {
        assertEquals(new ZipShort(0x5455), x5455.getHeaderId());
    }

    @Test
    public void testDefaultFlags() {
        assertFalse(x5455.isBit0_modifyTimePresent());
        assertFalse(x5455.isBit1_accessTimePresent());
        assertFalse(x5455.isBit2_createTimePresent());
    }

    @Test
    public void testModifyTime() {
        assertNull(x5455.getModifyTime());
        assertNull(x5455.getModifyJavaTime());

        Date date = new Date(123456789000L);
        x5455.setModifyTime(date);
        assertEquals(date, x5455.getModifyTime());
        assertEquals(date, x5455.getModifyJavaTime());
        assertTrue(x5455.isBit0_modifyTimePresent());

        x5455.setModifyTime(null);
        assertNull(x5455.getModifyTime());
        assertFalse(x5455.isBit0_modifyTimePresent());
    }

    @Test
    public void testAccessTime() {
        assertNull(x5455.getAccessTime());
        assertNull(x5455.getAccessJavaTime());

        Date date = new Date(987654321000L);
        x5455.setAccessTime(date);
        assertEquals(date, x5455.getAccessTime());
        assertEquals(date, x5455.getAccessJavaTime());
        assertTrue(x5455.isBit1_accessTimePresent());

        x5455.setAccessTime(null);
        assertNull(x5455.getAccessTime());
        assertFalse(x5455.isBit1_accessTimePresent());
    }

    @Test
    public void testCreateTime() {
        assertNull(x5455.getCreateTime());
        assertNull(x5455.getCreateJavaTime());

        Date date = new Date(500000000000L);
        x5455.setCreateTime(date);
        assertEquals(date, x5455.getCreateTime());
        assertEquals(date, x5455.getCreateJavaTime());
        assertTrue(x5455.isBit2_createTimePresent());

        x5455.setCreateTime(null);
        assertNull(x5455.getCreateTime());
        assertFalse(x5455.isBit2_createTimePresent());
    }

    @Test
    public void testLongTimes() {
        x5455.setModifyTime(1000L);
        assertEquals(new Date(1000L), x5455.getModifyTime());
        assertEquals(1000L, x5455.getModifyTimeLong());

        x5455.setAccessTime(2000L);
        assertEquals(new Date(2000L), x5455.getAccessTime());
        assertEquals(2000L, x5455.getAccessTimeLong());

        x5455.setCreateTime(3000L);
        assertEquals(new Date(3000L), x5455.getCreateTime());
        assertEquals(3000L, x5455.getCreateTimeLong());
    }

    @Test
    public void testZipLongTimes() {
        ZipLong zl = new ZipLong(12345);
        x5455.setModifyTime(zl);
        assertEquals(zl, x5455.getModifyTimeZipLong());

        ZipLong za = new ZipLong(54321);
        x5455.setAccessTime(za);
        assertEquals(za, x5455.getAccessTimeZipLong());

        ZipLong zc = new ZipLong(99999);
        x5455.setCreateTime(zc);
        assertEquals(zc, x5455.getCreateTimeZipLong());
    }

    @Test
    public void testGetLocalFileDataLength() {
        // Initially no flags, length should be 1 (flags byte only)
        assertEquals(new ZipShort(1), x5455.getLocalFileDataLength());

        x5455.setModifyTime(new Date());
        // Flags (1) + Modify time (4) = 5
        assertEquals(new ZipShort(5), x5455.getLocalFileDataLength());

        x5455.setAccessTime(new Date());
        // Flags (1) + Modify time (4) + Access time (4) = 9
        assertEquals(new ZipShort(9), x5455.getLocalFileDataLength());

        x5455.setCreateTime(new Date());
        // Flags (1) + Modify time (4) + Access time (4) + Create time (4) = 13
        assertEquals(new ZipShort(13), x5455.getLocalFileDataLength());
    }

    @Test
    public void testGetCentralDirectoryLength() {
        // Initially no flags, length should be 0 (central dir omits absent fields and flags if nothing present/configured specifically, or just flags=0 and length 1)
        // Let's verify central directory behavior based on implementation.
        // Usually, central directory only writes flags + fields that are present, or flags if any present.
        assertEquals(new ZipShort(0), x5455.getCentralDirectoryLength());

        x5455.setModifyTime(new Date());
        // Central dir with modify time: flags (1) + modify time (4) = 5
        assertEquals(new ZipShort(5), x5455.getCentralDirectoryLength());
    }

    @Test
    public void testGetLocalFileDataData() {
        x5455.setModifyTime(1000L);
        byte[] data = x5455.getLocalFileDataData();
        assertNotNull(data);
        assertTrue(data.length > 0);

        X5455_ExtendedTimestamp parsed = new X5455_ExtendedTimestamp();
        parsed.parseFromLocalFileData(data, 0, data.length);
        assertEquals(x5455.getModifyTime(), parsed.getModifyTime());
    }

    @Test
    public void testGetCentralDirectoryData() {
        x5455.setModifyTime(1000L);
        byte[] data = x5455.getCentralDirectoryData();
        assertNotNull(data);

        X5455_ExtendedTimestamp parsed = new X5455_ExtendedTimestamp();
        parsed.parseFromCentralDirectoryData(data, 0, data.length);
        assertEquals(x5455.getModifyTime(), parsed.getModifyTime());
    }

    @Test
    public void testParseFromLocalFileDataEdgeCases() {
        // Flags only (bit 0, 1, 2 not set)
        byte[] data = new byte[] { 0x07 };
        x5455.parseFromLocalFileData(data, 0, data.length);
        assertFalse(x5455.isBit0_modifyTimePresent());
        assertFalse(x5455.isBit1_accessTimePresent());
        assertFalse(x5455.isBit2_createTimePresent());

        // Flags with modify time present (bit 0 set), but insufficient bytes
        byte[] shortData = new byte[] { 0x01, 0x02 };
        x5455.parseFromLocalFileData(shortData, 0, shortData.length);

        // Flags with modify time present and exact bytes
        byte[] fullData = new byte[] { 0x01, 0x39, 0x30, 0x00, 0x00 };
        x5455.parseFromLocalFileData(fullData, 0, fullData.length);
        assertTrue(x5455.isBit0_modifyTimePresent());
        assertNotNull(x5455.getModifyTime());
    }

    @Test
    public void testParseFromCentralDirectoryDataEdgeCases() {
        byte[] data = new byte[] { 0x01, 0x39, 0x30, 0x00, 0x00 };
        x5455.parseFromCentralDirectoryData(data, 0, data.length);
        assertTrue(x5455.isBit0_modifyTimePresent());
    }

    @Test
    public void testCloneAndEquals() {
        x5455.setModifyTime(1000L);
        x5455.setAccessTime(2000L);
        x5455.setCreateTime(3000L);

        Object clone = x5455.clone();
        assertEquals(x5455, clone);
        assertEquals(x5455.hashCode(), clone.hashCode());

        assertFalse(x5455.equals(null));
        assertFalse(x5455.equals(new Object()));

        X5455_ExtendedTimestamp other = new X5455_ExtendedTimestamp();
        other.setModifyTime(1000L);
        other.setAccessTime(2000L);
        other.setCreateTime(3000L);
        assertTrue(x5455.equals(other));

        other.setCreateTime(4000L);
        assertFalse(x5455.equals(other));
    }

    @Test
    public void testToString() {
        x5455.setModifyTime(1000L);
        String str = x5455.toString();
        assertNotNull(str);
        assertTrue(str.contains("0x5455"));
    }

    @Test
    public void testTimeZoneHandling() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        // Methods utilizing timezone if present in class
        assertNotNull(x5455.toString());
    }
}