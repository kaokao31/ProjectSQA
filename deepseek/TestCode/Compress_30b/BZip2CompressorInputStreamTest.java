package org.apache.commons.compress.compressors.bzip2;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BZip2CompressorInputStreamTest {

    private static final byte[] EMPTY_BZ2 = {
        0x42, 0x5A, 0x68, 0x39, 0x17, 0x72, 0x45, 0x38,
        0x50, (byte)0x90, 0x00, 0x00, 0x00, 0x00, 0x00
    };

    private static final byte[] ONE_BYTE_BZ2 = {
        0x42, 0x5A, 0x68, 0x39, 0x17, 0x72, 0x45, 0x38,
        0x50, (byte)0x90, 0x00, 0x00, 0x00, 0x00, 0x01,
        0x00, 0x00, 0x00, 0x00, (byte)0x80, 0x00, 0x2A,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };

    private static final byte[] CORRUPTED_BZ2 = {
        0x42, 0x5A, 0x68, 0x39, 0x17, 0x72, 0x45, 0x38,
        0x50, (byte)0x90, 0x00, 0x00, 0x00, 0x00, 0x00
    };

    @Test(expected = NullPointerException.class)
    public void testNullInputStream() throws IOException {
        new BZip2CompressorInputStream(null);
    }

    @Test(expected = IOException.class)
    public void testEmptyStream() throws IOException {
        new BZip2CompressorInputStream(new ByteArrayInputStream(new byte[0]));
    }

    @Test(expected = IOException.class)
    public void testNotABZipFile() throws IOException {
        new BZip2CompressorInputStream(new ByteArrayInputStream("Not a bzip2 file".getBytes()));
    }

    @Test
    public void testReadFromEmptyBZip2() throws IOException {
        BZip2CompressorInputStream in = new BZip2CompressorInputStream(new ByteArrayInputStream(EMPTY_BZ2));
        assertEquals(-1, in.read());
        in.close();
    }

    @Test
    public void testReadOneByte() throws IOException {
        BZip2CompressorInputStream in = new BZip2CompressorInputStream(new ByteArrayInputStream(ONE_BYTE_BZ2));
        int b = in.read();
        assertTrue("Should read at least 0 bits", b >= 0);
        assertTrue("Should read <= 255 bits", b <= 255);
        assertEquals(-1, in.read());
        in.close();
    }

    @Test(expected = IOException.class)
    public void testCorruptedStream() throws IOException {
        BZip2CompressorInputStream in = new BZip2CompressorInputStream(new ByteArrayInputStream(CORRUPTED_BZ2));
        in.read();
    }

    @Test
    public void testReadByteArray() throws IOException {
        BZip2CompressorInputStream in = new BZip2CompressorInputStream(new ByteArrayInputStream(ONE_BYTE_BZ2));
        byte[] buf = new byte[10];
        int count = in.read(buf);
        assertTrue("Should read at least some bytes", count > 0 || count == -1);
        if (count > 0) {
            assertTrue("Bytes read should be within array bounds", count <= 10);
        }
        in.close();
    }

    @Test
    public void testReadByteArrayWithOffset() throws IOException {
        BZip2CompressorInputStream in = new BZip2CompressorInputStream(new ByteArrayInputStream(ONE_BYTE_BZ2));
        byte[] buf = new byte[10];
        int count = in.read(buf, 2, 5);
        assertTrue("Should read at least some bytes", count > 0 || count == -1);
        if (count > 0) {
            assertTrue("Read length should be valid", count <= 5);
        }
        in.close();
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadByteArrayNegativeOffset() throws IOException {
        BZip2CompressorInputStream in = new BZip2CompressorInputStream(new ByteArrayInputStream(ONE_BYTE_BZ2));
        byte[] buf = new byte[10];
        in.read(buf, -1, 5);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadByteArrayNegativeLength() throws IOException {
        BZip2CompressorInputStream in = new BZip2CompressorInputStream(new ByteArrayInputStream(ONE_BYTE_BZ2));
        byte[] buf = new byte[10];
        in.read(buf, 0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadByteArrayOverflow() throws IOException {
        BZip2CompressorInputStream in = new BZip2CompressorInputStream(new ByteArrayInputStream(ONE_BYTE_BZ2));
        byte[] buf = new byte[10];
        in.read(buf, 8, 5);
    }

    @Test
    public void testAvailable() throws IOException {
        BZip2CompressorInputStream in = new BZip2CompressorInputStream(new ByteArrayInputStream(ONE_BYTE_BZ2));
        int avail = in.available();
        assertTrue("Available should be non-negative", avail >= 0);
        in.close();
    }

    @Test
    public void testSkip() throws IOException {
        BZip2CompressorInputStream in = new BZip2CompressorInputStream(new ByteArrayInputStream(ONE_BYTE_BZ2));
        long skipped = in.skip(5);
        assertTrue("Skipped bytes should be non-negative", skipped >= 0);
        in.close();
    }

    @Test
    public void testSkipNegative() throws IOException {
        BZip2CompressorInputStream in = new BZip2CompressorInputStream(new ByteArrayInputStream(ONE_BYTE_BZ2));
        long skipped = in.skip(-1);
        assertEquals("Skipping negative should return 0", 0, skipped);
        in.close();
    }

    @Test
    public void testClose() throws IOException {
        BZip2CompressorInputStream in = new BZip2CompressorInputStream(new ByteArrayInputStream(ONE_BYTE_BZ2));
        in.close();
        in.close(); // Closing twice should be no-op
    }

    @Test
    public void testReadAfterClose() throws IOException {
        BZip2CompressorInputStream in = new BZip2CompressorInputStream(new ByteArrayInputStream(ONE_BYTE_BZ2));
        in.close();
        try {
            in.read();
            fail("Should throw IOException after close");
        } catch (IOException e) {
            // Expected
        }
    }

    @Test(expected = IOException.class)
    public void testTruncatedStream() throws IOException {
        byte[] truncated = new byte[10];
        System.arraycopy(ONE_BYTE_BZ2, 0, truncated, 0, 10);
        BZip2CompressorInputStream in = new BZip2CompressorInputStream(new ByteArrayInputStream(truncated));
        in.read();
    }

    @Test
    public void testReadZeroBytes() throws IOException {
        BZip2CompressorInputStream in = new BZip2CompressorInputStream(new ByteArrayInputStream(ONE_BYTE_BZ2));
        byte[] buf = new byte[0];
        int count = in.read(buf);
        assertEquals("Reading into zero-length buffer should return 0", 0, count);
        in.close();
    }

    @Test
    public void testReadNullBuffer() throws IOException {
        BZip2CompressorInputStream in = new BZip2CompressorInputStream(new ByteArrayInputStream(ONE_BYTE_BZ2));
        try {
            in.read(null, 0, 1);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
        in.close();
    }
}