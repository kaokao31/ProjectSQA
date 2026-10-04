package org.apache.commons.compress.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ChecksumCalculatingInputStreamTest {

    private TestChecksum checksum;
    private ChecksumCalculatingInputStream in;

    @Before
    public void setUp() {
        // initialized per test
    }

    private void createStream(byte[] data) {
        checksum = new TestChecksum();
        in = new ChecksumCalculatingInputStream(checksum, new ByteArrayInputStream(data));
    }

    private static class TestChecksum implements Checksum {
        private long value = 0;
        private java.io.ByteArrayOutputStream written = new java.io.ByteArrayOutputStream();

        @Override
        public void update(int b) {
            value += b;
            written.write(b);
        }

        @Override
        public void update(byte[] b, int off, int len) {
            for (int i = 0; i < len; i++) {
                value += b[off + i];
            }
            written.write(b, off, len);
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public void reset() {
            value = 0;
            written.reset();
        }

        public byte[] getWritten() {
            return written.toByteArray();
        }
    }

    @Test
    public void testReadSingleByte() throws IOException {
        byte[] data = new byte[]{1, 2, 3};
        createStream(data);
        int b = in.read();
        Assert.assertEquals(1, b);
        Assert.assertEquals(1L, checksum.getValue());
        Assert.assertArrayEquals(new byte[]{1}, checksum.getWritten());
    }

    @Test
    public void testReadArray() throws IOException {
        byte[] data = new byte[]{10, 20, 30};
        createStream(data);
        byte[] buf = new byte[2];
        int n = in.read(buf);
        Assert.assertEquals(2, n);
        Assert.assertEquals(30L, checksum.getValue());
        Assert.assertArrayEquals(new byte[]{10, 20}, checksum.getWritten());
    }

    @Test
    public void testReadZeroLength() throws IOException {
        byte[] data = new byte[]{1, 2, 3};
        createStream(data);
        byte[] buf = new byte[5];
        int n = in.read(buf, 0, 0);
        Assert.assertEquals(0, n);
        Assert.assertEquals(0L, checksum.getValue());
        Assert.assertEquals(0, checksum.getWritten().length);
    }

    @Test
    public void testReadAfterEof() throws IOException {
        byte[] data = new byte[]{5};
        createStream(data);
        int b = in.read();
        Assert.assertEquals(5, b);
        Assert.assertEquals(5L, checksum.getValue());
        b = in.read();
        Assert.assertEquals(-1, b);
        Assert.assertEquals(5L, checksum.getValue());
        byte[] buf = new byte[1];
        int n = in.read(buf);
        Assert.assertEquals(-1, n);
        Assert.assertEquals(5L, checksum.getValue());
    }

    @Test
    public void testReadLargeData() throws IOException {
        byte[] data = new byte[1024];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i % 256);
        }
        createStream(data);
        byte[] buf = new byte[256];
        int total = 0;
        int n;
        while ((n = in.read(buf)) != -1) {
            total += n;
        }
        Assert.assertEquals(data.length, total);
        long expected = 0;
        for (byte b : data) {
            expected += b;
        }
        Assert.assertEquals(expected, checksum.getValue());
    }

    @Test(expected = NullPointerException.class)
    public void testReadWithNullBuffer() throws IOException {
        createStream(new byte[1]);
        in.read(null, 0, 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadWithNegativeOffset() throws IOException {
        createStream(new byte[1]);
        byte[] buf = new byte[5];
        in.read(buf, -1, 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadWithNegativeLength() throws IOException {
        createStream(new byte[1]);
        byte[] buf = new byte[5];
        in.read(buf, 0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadWithOffsetOutOfBounds() throws IOException {
        createStream(new byte[1]);
        byte[] buf = new byte[5];
        in.read(buf, 5, 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadWithLengthOutOfBounds() throws IOException {
        createStream(new byte[1]);
        byte[] buf = new byte[5];
        in.read(buf, 0, 6);
    }

    @Test
    public void testMultipleReads() throws IOException {
        byte[] data = new byte[]{1, 2, 3, 4, 5};
        createStream(data);
        byte[] buf = new byte[2];
        Assert.assertEquals(2, in.read(buf));
        Assert.assertEquals(3L, checksum.getValue());
        Assert.assertEquals(2, in.read(buf));
        Assert.assertEquals(10L, checksum.getValue());
        Assert.assertEquals(1, in.read(buf));
        Assert.assertEquals(15L, checksum.getValue());
        Assert.assertEquals(-1, in.read(buf));
        Assert.assertEquals(15L, checksum.getValue());
    }

    @Test
    public void testReadSingleByteAfterEof() throws IOException {
        createStream(new byte[0]);
        Assert.assertEquals(-1, in.read());
        Assert.assertEquals(0L, checksum.getValue());
    }

    @Test
    public void testReadArrayAfterEof() throws IOException {
        createStream(new byte[0]);
        byte[] buf = new byte[1];
        Assert.assertEquals(-1, in.read(buf));
        Assert.assertEquals(0L, checksum.getValue());
    }

    @Test
    public void testReadSingleByteUpdatesChecksum() throws IOException {
        byte[] data = new byte[]{42};
        createStream(data);
        int b = in.read();
        Assert.assertEquals(42, b);
        Assert.assertEquals(42L, checksum.getValue());
    }

    @Test
    public void testReadArrayUpdatesChecksum() throws IOException {
        byte[] data = new byte[]{1, 2, 3};
        createStream(data);
        byte[] buf = new byte[3];
        int n = in.read(buf);
        Assert.assertEquals(3, n);
        Assert.assertEquals(6L, checksum.getValue());
    }

    @Test
    public void testReadWithOffset() throws IOException {
        byte[] data = new byte[]{1, 2, 3, 4, 5};
        createStream(data);
        byte[] buf = new byte[10];
        int n = in.read(buf, 2, 3);
        Assert.assertEquals(3, n);
        Assert.assertEquals(6L, checksum.getValue());
        Assert.assertArrayEquals(new byte[]{1, 2, 3}, checksum.getWritten());
    }

    @Test
    public void testReadPartialBuffer() throws IOException {
        byte[] data = new byte[]{1, 2, 3};
        createStream(data);
        byte[] buf = new byte[10];
        int n = in.read(buf, 0, 10);
        Assert.assertEquals(3, n);
        Assert.assertEquals(6L, checksum.getValue());
    }

    @Test
    public void testReadByteArraySingleArg() throws IOException {
        byte[] data = new byte[]{1, 2, 3};
        createStream(data);
        byte[] buf = new byte[2];
        int n = in.read(buf);
        Assert.assertEquals(2, n);
        Assert.assertEquals(3L, checksum.getValue());
    }
}