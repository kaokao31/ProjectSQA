package org.apache.commons.compress.utils;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class ChecksumCalculatingInputStreamTest {

    @Test
    public void testReadSingleByte() throws IOException {
        byte[] data = {1, 2, 3, 4, 5};
        Checksum checksum = new CRC32();
        ChecksumCalculatingInputStream cis = new ChecksumCalculatingInputStream(checksum, new ByteArrayInputStream(data));

        int firstByte = cis.read();
        Assert.assertEquals(1, firstByte);

        // Read remaining bytes
        byte[] rest = new byte[4];
        int bytesRead = cis.read(rest);
        Assert.assertEquals(4, bytesRead);
        Assert.assertEquals(2, rest[0]);
        Assert.assertEquals(3, rest[1]);
        Assert.assertEquals(4, rest[2]);
        Assert.assertEquals(5, rest[3]);

        // EOF
        Assert.assertEquals(-1, cis.read());
        
        // Verify checksum has been updated
        Assert.assertNotEquals(0, cis.getValue());
    }

    @Test
    public void testReadByteArrayWithOffsets() throws IOException {
        byte[] data = {10, 20, 30, 40, 50};
        Checksum checksum = new CRC32();
        ChecksumCalculatingInputStream cis = new ChecksumCalculatingInputStream(checksum, new ByteArrayInputStream(data));

        byte[] buffer = new byte[10];
        // Read into buffer starting at offset 2, length 3
        int read = cis.read(buffer, 2, 3);
        Assert.assertEquals(3, read);
        Assert.assertEquals(10, buffer[2]);
        Assert.assertEquals(20, buffer[3]);
        Assert.assertEquals(30, buffer[4]);
        Assert.assertEquals(0, buffer[0]); // untouched

        // Read the rest
        int read2 = cis.read(buffer, 5, 2);
        Assert.assertEquals(2, read2);
        Assert.assertEquals(40, buffer[5]);
        Assert.assertEquals(50, buffer[6]);

        // EOF read
        int readEof = cis.read(buffer, 0, 1);
        Assert.assertEquals(-1, readEof);
    }

    @Test
    public void testReadIntoFullByteArray() throws IOException {
        byte[] data = {9, 8, 7, 6};
        Checksum checksum = new CRC32();
        ChecksumCalculatingInputStream cis = new ChecksumCalculatingInputStream(checksum, new ByteArrayInputStream(data));

        byte[] buffer = new byte[4];
        int read = cis.read(buffer);
        Assert.assertEquals(4, read);
        Assert.assertEquals(9, buffer[0]);
        Assert.assertEquals(8, buffer[1]);
        Assert.assertEquals(7, buffer[2]);
        Assert.assertEquals(6, buffer[3]);

        Assert.assertEquals(-1, cis.read(buffer));
    }

    @Test
    public void testSkip() throws IOException {
        byte[] data = {1, 2, 3, 4, 5};
        Checksum checksum = new CRC32();
        ChecksumCalculatingInputStream cis = new ChecksumCalculatingInputStream(checksum, new ByteArrayInputStream(data));

        long skipped = cis.skip(2);
        Assert.assertEquals(2, skipped);

        // Next read should be 3
        Assert.assertEquals(3, cis.read());
        Assert.assertEquals(4, cis.read());
        Assert.assertEquals(5, cis.read());
        Assert.assertEquals(-1, cis.read());
    }

    @Test(expected = NullPointerException.class)
    public void testNullChecksumConstructor() throws IOException {
        new ChecksumCalculatingInputStream(null, new ByteArrayInputStream(new byte[0]));
    }

    @Test(expected = NullPointerException.class)
    public void testNullStreamConstructor() throws IOException {
        new ChecksumCalculatingInputStream(new CRC32(), null);
    }

    @Test
    public void testGettersAndValue() throws IOException {
        byte[] data = {1, 1, 1};
        Checksum checksum = new CRC32();
        ChecksumCalculatingInputStream cis = new ChecksumCalculatingInputStream(checksum, new ByteArrayInputStream(data));

        Assert.assertEquals(0, cis.getValue());
        
        cis.read();
        long val1 = cis.getValue();
        Assert.assertNotEquals(0, val1);

        cis.read(new byte[2]);
        long val2 = cis.getValue();
        Assert.assertTrue(val2 >= val1);
    }

    @Test
    public void testCloseDelegation() throws IOException {
        DummyInputStream dummy = new DummyInputStream(new byte[]{1, 2});
        ChecksumCalculatingInputStream cis = new ChecksumCalculatingInputStream(new CRC32(), dummy);
        
        Assert.assertFalse(dummy.isClosed());
        cis.close();
        Assert.assertTrue(dummy.isClosed());
    }

    private static class DummyInputStream extends InputStream {
        private final InputStream delegate;
        private boolean closed = false;

        public DummyInputStream(byte[] data) {
            this.delegate = new ByteArrayInputStream(data);
        }

        @Override
        public int read() throws IOException {
            return delegate.read();
        }

        @Override
        public void close() throws IOException {
            closed = true;
            delegate.close();
        }

        public boolean isClosed() {
            return closed;
        }
    }
}