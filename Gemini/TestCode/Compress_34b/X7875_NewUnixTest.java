package org.apache.commons.compress.archivers.zip;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.zip.ZipException;

public class X7875_NewUnixTest {

    private X7875_NewUnix x7875;

    @Before
    public void setUp() {
        x7875 = new X7875_NewUnix();
    }

    @Test
    public void testGetHeaderId() {
        Assert.assertEquals(new ZipShort(0x7875), x7875.getHeaderId());
    }

    @Test
    public void testDefaultValues() {
        // UID and GID default to 1000 in Commons Compress X7875
        Assert.assertEquals(1000L, x7875.getUID());
        Assert.assertEquals(1000L, x7875.getGID());
    }

    @Test
    public void testSetAndGetUID() {
        x7875.setUID(0L);
        Assert.assertEquals(0L, x7875.getUID());

        x7875.setUID(12345L);
        Assert.assertEquals(12345L, x7875.getUID());

        x7875.setUID(4294967295L); // Max uint32
        Assert.assertEquals(4294967295L, x7875.getUID());
    }

    @Test
    public void testSetAndGetGID() {
        x7875.setGID(0L);
        Assert.assertEquals(0L, x7875.getGID());

        x7875.setGID(54321L);
        Assert.assertEquals(54321L, x7875.getGID());

        x7875.setGID(4294967295L); // Max uint32
        Assert.assertEquals(4294967295L, x7875.getGID());
    }

    @Test
    public void testGetLocalFileDataDataDefault() {
        byte[] data = x7875.getLocalFileDataData();
        // Version 1, UID size 2 (bytes for 1000 = 0x03E8), UID (0x03E8), GID size 2, GID (0x03E8)
        // Let's verify it's not null and has a valid length
        Assert.assertNotNull(data);
        Assert.assertTrue(data.length > 0);
    }

    @Test
    public void testGetCentralDirectoryData() {
        // Central directory for X7875 usually returns empty array if not explicitly populated differently
        byte[] cdData = x7875.getCentralDirectoryData();
        byte[] localData = x7875.getLocalFileDataData();
        // Depending on implementation, central directory might mirror local or be empty.
        // Let's assert it returns something non-null.
        Assert.assertNotNull(cdData);
    }

    @Test
    public void testParseFromLocalFileDataValid() throws ZipException {
        // Construct a valid local file data byte array
        // Format: version (1 byte = 1), uidSize (1 byte), uid (uidSize bytes), gidSize (1 byte), gid (gidSize bytes)
        // UID = 1000 (0x03E8 -> 2 bytes: 0xE8, 0x03 in little-endian)
        // GID = 1000 (0x03E8 -> 2 bytes: 0xE8, 0x03 in little-endian)
        byte[] buffer = new byte[] {
            1,          // version
            2,          // UID size
            (byte) 0xE8, (byte) 0x03, // UID = 1000
            2,          // GID size
            (byte) 0xE8, (byte) 0x03  // GID = 1000
        };

        x7875.parseFromLocalFileData(buffer, 0, buffer.length);
        Assert.assertEquals(1000L, x7875.getUID());
        Assert.assertEquals(1000L, x7875.getGID());
    }

    @Test
    public void testParseFromLocalFileDataDifferentSizes() throws ZipException {
        // Test with 4-byte UID and 4-byte GID
        // UID = 0x01020304, GID = 0x05060708
        byte[] buffer = new byte[] {
            1,          // version
            4,          // UID size
            4, 3, 2, 1, // UID little-endian
            4,          // GID size
            8, 7, 6, 5  // GID little-endian
        };

        x7875.parseFromLocalFileData(buffer, 0, buffer.length);
        Assert.assertEquals(0x01020304L, x7875.getUID());
        Assert.assertEquals(0x05060708L, x7875.getGID());
    }

    @Test(expected = ZipException.class)
    public void testParseFromLocalFileDataTooShort() throws ZipException {
        // Buffer too short to even read version and sizes
        byte[] buffer = new byte[] { 1, 2 };
        x7875.parseFromLocalFileData(buffer, 0, buffer.length);
    }

    @Test
    public void testClone() {
        x7875.setUID(123L);
        x7875.setGID(456L);
        Object cloneObj = x7875.clone();
        Assert.assertTrue(cloneObj instanceof X7875_NewUnix);
        X7875_NewUnix cloned = (X7875_NewUnix) cloneObj;
        Assert.assertEquals(123L, cloned.getUID());
        Assert.assertEquals(456L, cloned.getGID());
    }

    @Test
    public void testEqualsAndHashCode() {
        X7875_NewUnix e1 = new X7875_NewUnix();
        X7875_NewUnix e2 = new X7875_NewUnix();

        e1.setUID(100L);
        e1.setGID(200L);

        e2.setUID(100L);
        e2.setGID(200L);

        Assert.assertEquals(e1, e2);
        Assert.assertEquals(e1.hashCode(), e2.hashCode());

        e2.setUID(101L);
        Assert.assertNotEquals(e1, e2);

        Assert.assertNotEquals(e1, null);
        Assert.assertNotEquals(e1, "SomeString");
    }

    @Test
    public void testToString() {
        String str = x7875.toString();
        Assert.assertNotNull(str);
        Assert.assertTrue(str.contains("0x7875"));
    }
}