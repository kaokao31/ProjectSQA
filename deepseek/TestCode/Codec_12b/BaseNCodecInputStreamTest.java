package org.apache.commons.codec.binary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

/**
 * Test suite for BaseNCodecInputStream.
 * Targets maximum coverage and fault detection (Defects4J bug 12).
 */
public class BaseNCodecInputStreamTest {

    // Utility to create a Base64InputStream wrapping an encoded string
    private InputStream createBase64InputStream(final String original, final boolean encode) throws IOException {
        final byte[] encodedData = Base64.encodeBase64(original.getBytes("UTF-8"));
        final InputStream sourceStream = new ByteArrayInputStream(encode ? encodedData : original.getBytes("UTF-8"));
        // For encode mode we use the reverse? Actually Base64InputStream can decode or encode.
        // We'll use decode mode for reading decoded output.
        // If encode is true, we want to read the original by encoding? Not needed for tests.
        return new Base64InputStream(sourceStream, !encode);
    }

    // Helper: assert that reading a stream gives the expected string
    private void assertStreamRead(final String expected, final InputStream stream) throws IOException {
        final byte[] buffer = new byte[expected.length() + 10];
        int totalRead = 0;
        int read;
        while ((read = stream.read(buffer, totalRead, buffer.length - totalRead)) != -1) {
            totalRead += read;
        }
        final String result = new String(buffer, 0, totalRead, "UTF-8");
        assertEquals(expected, result);
        stream.close();
    }

    // =========================================================================
    // 1. Basic read(byte[], off, len) with varied offsets and lengths
    // =========================================================================

    @Test
    public void testReadFullBuffer() throws IOException {
        final String original = "Hello World";
        final InputStream stream = createBase64InputStream(original, false);
        assertStreamRead(original, stream);
    }

    @Test
    public void testReadPartialSmallBuffer() throws IOException {
        final String original = "Hello World";
        final InputStream stream = createBase64InputStream(original, false);
        final byte[] buf = new byte[5];
        int r = stream.read(buf, 0, 5);
        assertEquals(5, r);
        assertEquals("Hello", new String(buf, "UTF-8"));
        stream.close();
    }

    @Test
    public void testReadWithOffsetNonZero() throws IOException {
        final String original = "Hello World";
        final InputStream stream = createBase64InputStream(original, false);
        final byte[] buf = new byte[20];
        // offset 5, length 5
        int r = stream.read(buf, 5, 5);
        assertEquals(5, r);
        assertEquals("Hello", new String(buf, 5, 5, "UTF-8"));
        stream.close();
    }

    @Test
    public void testReadWithZeroLength() throws IOException {
        final InputStream stream = createBase64InputStream("test", false);
        final byte[] buf = new byte[10];
        int r = stream.read(buf, 0, 0);
        assertEquals(0, r);
        stream.close();
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadNegativeOffset() throws IOException {
        final InputStream stream = createBase64InputStream("test", false);
        final byte[] buf = new byte[10];
        stream.read(buf, -1, 5);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadNegativeLength() throws IOException {
        final InputStream stream = createBase64InputStream("test", false);
        final byte[] buf = new byte[10];
        stream.read(buf, 0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadOffsetBeyondLength() throws IOException {
        final InputStream stream = createBase64InputStream("test", false);
        final byte[] buf = new byte[10];
        stream.read(buf, 15, 2);
    }

    // =========================================================================
    // 2. End-of-stream behavior (bug trigger for Defects4J bug 12)
    // =========================================================================

    @Test
    public void testReadAtEndReturnsMinusOne() throws IOException {
        // Empty original -> encoded stream is empty
        final InputStream stream = createBase64InputStream("", false);
        final byte[] buf = new byte[10];
        int r = stream.read(buf);
        assertEquals(-1, r);
        stream.close();
    }

    @Test
    public void testReadAfterFullReadReturnsMinusOne() throws IOException {
        final InputStream stream = createBase64InputStream("A", false);
        final byte[] buf = new byte[10];
        // read all bytes
        while (stream.read(buf) != -1);
        // next read should be -1
        assertEquals(-1, stream.read(buf));
        stream.close();
    }

    @Test
    public void testReadAfterPartialReadReturnsMinusOne() throws IOException {
        final String original = "Hi";
        final InputStream stream = createBase64InputStream(original, false);
        final byte[] buf = new byte[20];
        // read exactly the first byte
        assertEquals(1, stream.read(buf, 0, 1));
        // read remaining bytes
        assertEquals(1, stream.read(buf, 0, 20));
        // next read should be -1
        assertEquals(-1, stream.read(buf, 0, 20));
        stream.close();
    }

    // =========================================================================
    // 3. Read single byte via read()
    // =========================================================================

    @Test
    public void testReadSingleByte() throws IOException {
        final String original = "ABC";
        final InputStream stream = createBase64InputStream(original, false);
        assertEquals('A', stream.read());
        assertEquals('B', stream.read());
        assertEquals('C', stream.read());
        assertEquals(-1, stream.read());
        stream.close();
    }

    @Test
    public void testReadSingleByteAtEnd() throws IOException {
        final InputStream stream = createBase64InputStream("", false);
        assertEquals(-1, stream.read());
        stream.close();
    }

    // =========================================================================
    // 4. Available method
    // =========================================================================

    @Test
    public void testAvailableFreshStream() throws IOException {
        // With non-empty underlying stream, available should be >0
        final InputStream stream = createBase64InputStream("Some data", false);
        assertTrue(stream.available() > 0);
        stream.close();
    }

    @Test
    public void testAvailableAfterRead() throws IOException {
        final InputStream stream = createBase64InputStream("12345", false);
        // read a few bytes
        stream.read(new byte[3]);
        int avail = stream.available();
        // We cannot predict exact, but must be non-negative
        assertTrue(avail >= 0);
        stream.close();
    }

    @Test
    public void testAvailableAtEnd() throws IOException {
        final InputStream stream = createBase64InputStream("", false);
        assertEquals(0, stream.available());
        stream.close();
    }

    // =========================================================================
    // 5. Constructor and null checks
    // =========================================================================

    @Test(expected = NullPointerException.class)
    public void testConstructorNullInputStream() throws IOException {
        new Base64InputStream(null, false);
    }

    // BaseNCodecInputStream is abstract; we use Base64InputStream which takes a codec or boolean.
    // For codec null, it may throw. But we cannot instantiate directly.
    // Instead, we can try to create a Base64InputStream with invalid arguments.
    // Base64InputStream(InputStream, boolean) uses Base64 codec internally so it's safe.
    // For null codec, we cannot test unless we subclass. Skip.

    // =========================================================================
    // 6. Mark/Reset (if supported by underlying stream, but not required)
    // =========================================================================

    @Test
    public void testMarkNotSupported() throws IOException {
        final InputStream stream = createBase64InputStream("test", false);
        assertFalse(stream.markSupported());
        stream.close();
    }

    // =========================================================================
    // 7. Large data chunked read (boundary conditions)
    // =========================================================================

    @Test
    public void testReadChunkedLargeData() throws IOException {
        final StringBuilder sb = new StringBuilder(5000);
        for (int i = 0; i < 1000; i++) {
            sb.append("abcdefghij"); // 10 chars each
        }
        final String original = sb.toString();
        final InputStream stream = createBase64InputStream(original, false);
        final byte[] buf = new byte[100]; // small chunk
        int totalRead = 0;
        int read;
        while ((read = stream.read(buf)) != -1) {
            totalRead += read;
        }
        assertEquals(original.length(), totalRead);
        stream.close();
    }

    @Test
    public void testReadExactBufferSize() throws IOException {
        // Use a string whose encoded form is exactly the buffer size
        final String original = "Hello"; // 5 bytes
        final InputStream stream = createBase64InputStream(original, false);
        final byte[] buf = new byte[5]; // exact size
        int r = stream.read(buf);
        assertEquals(5, r);
        assertEquals(-1, stream.read(buf));
        stream.close();
    }

    // =========================================================================
    // 8. Skip (if available)
    // =========================================================================

    @Test
    public void testSkip() throws IOException {
        final String original = "SkipMe";
        final InputStream stream = createBase64InputStream(original, false);
        stream.skip(4);
        // remaining: "Me"
        assertEquals('M', stream.read());
        assertEquals('e', stream.read());
        assertEquals(-1, stream.read());
        stream.close();
    }

    @Test
    public void testSkipNegative() throws IOException {
        final InputStream stream = createBase64InputStream("test", false);
        long skipped = stream.skip(-5);
        assertEquals(0, skipped);
        stream.close();
    }

    // =========================================================================
    // 9. Multiple consecutive reads with varying lengths (stress EOF)
    // =========================================================================

    @Test
    public void testReadMultipleSmallChunks() throws IOException {
        final String original = "Four";
        final InputStream stream = createBase64InputStream(original, false);
        final byte[] buf = new byte[10];
        int total = 0;
        // read one byte at a time
        int b;
        while ((b = stream.read()) != -1) {
            buf[total++] = (byte) b;
        }
        assertEquals(original, new String(buf, 0, total, "UTF-8"));
        stream.close();
    }
}