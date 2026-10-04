package org.apache.commons.codec.binary;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import org.junit.Test;

public class Base64InputStreamTest {

    @Test
    public void testDecodeEmptyStream() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(new byte[0]));
        assertEquals(-1, in.read());
        in.close();

        Base64InputStream in2 = new Base64InputStream(new ByteArrayInputStream(new byte[0]));
        assertEquals(-1, in2.read(new byte[1], 0, 1));
        in2.close();

        Base64InputStream in3 = new Base64InputStream(new ByteArrayInputStream(new byte[0]));
        assertArrayEquals(new byte[0], readAll(in3));
        in3.close();
    }

    @Test
    public void testDecodeBasic() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(bytes("TWFu")));
        assertArrayEquals(bytes("Man"), readAll(in));
        in.close();
    }

    @Test
    public void testDecodeTwoBytesWithPadding() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(bytes("TWE=")));
        assertArrayEquals(bytes("Ma"), readAll(in));
        in.close();
    }

    @Test
    public void testDecodeOneByteWithPadding() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(bytes("TQ==")));
        assertArrayEquals(bytes("M"), readAll(in));
        in.close();
    }

    @Test
    public void testDecodeUnpaddedFinalTriplet() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(bytes("TWF")));
        assertArrayEquals(bytes("Ma"), readAll(in));
        in.close();
    }

    @Test
    public void testDecodeIgnoresWhitespace() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(bytes("T W F u")));
        assertArrayEquals(bytes("Man"), readAll(in));
        in.close();
    }

    @Test
    public void testDecodeIgnoresLineSeparators() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(bytes("TWFu\r\nTWFu")));
        assertArrayEquals(bytes("ManMan"), readAll(in));
        in.close();
    }

    @Test
    public void testDecodeChunkedWithConstructedLineLength() throws IOException {
        byte[] sep = new byte[] {'\r', '\n'};
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(bytes("TWFu\r\nTWFu")), false, 4, sep);
        assertArrayEquals(bytes("ManMan"), readAll(in));
        in.close();
    }

    @Test
    public void testDecodeMultipleOfThree() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(bytes("TWFuTWFu")));
        assertArrayEquals(bytes("ManMan"), readAll(in));
        in.close();
    }

    @Test
    public void testDecodeLeadingAndTrailingWhitespace() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(bytes("  \r\n TWFu \n ")));
        assertArrayEquals(bytes("Man"), readAll(in));
        in.close();
    }

    @Test
    public void testEncodeEmptyStream() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(new byte[0]), true);
        assertArrayEquals(new byte[0], readAll(in));
        in.close();
    }

    @Test
    public void testEncodeOneByte() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(bytes("M")), true);
        assertArrayEquals(bytes("TQ=="), readAll(in));
        in.close();
    }

    @Test
    public void testEncodeTwoBytes() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(bytes("Ma")), true);
        assertArrayEquals(bytes("TWE="), readAll(in));
        in.close();
    }

    @Test
    public void testEncodeThreeBytes() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(bytes("Man")), true);
        assertArrayEquals(bytes("TWFu"), readAll(in));
        in.close();
    }

    @Test
    public void testEncodeChunkedWithLineLength() throws IOException {
        byte[] sep = new byte[] {'\r', '\n'};
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(bytes("ManMan")), true, 4, sep);
        assertArrayEquals(bytes("TWFu\r\nTWFu"), readAll(in));
        in.close();
    }

    @Test
    public void testEncodeWithLineLengthZeroAndNullSeparator() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(bytes("Man")), true, 0, null);
        assertArrayEquals(bytes("TWFu"), readAll(in));
        in.close();
    }

    @Test
    public void testReadSingleByte() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(bytes("TWFu")));
        assertEquals('M', in.read());
        assertEquals('a', in.read());
        assertEquals('n', in.read());
        assertEquals(-1, in.read());
        in.close();
    }

    @Test
    public void testReadSingleByteEncode() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(bytes("Man")), true);
        assertEquals('T', in.read());
        assertEquals('W', in.read());
        assertEquals('F', in.read());
        assertEquals('u', in.read());
        assertEquals(-1, in.read());
        in.close();
    }

    @Test(timeout = 5000)
    public void testDecodeChunkedInputSingleByteChunks() throws IOException {
        InputStream slow = new ChunkedInputStream(bytes("TWFuTWFu"), 1);
        Base64InputStream in = new Base64InputStream(slow);
        assertArrayEquals(bytes("ManMan"), readAll(in));
        in.close();
    }

    @Test(timeout = 5000)
    public void testDecodeChunkedInputTwoByteChunks() throws IOException {
        InputStream slow = new ChunkedInputStream(bytes("TWFuTWFu"), 2);
        Base64InputStream in = new Base64InputStream(slow);
        assertArrayEquals(bytes("ManMan"), readAll(in));
        in.close();
    }

    @Test(timeout = 5000)
    public void testEncodeChunkedInputSingleByteChunks() throws IOException {
        InputStream slow = new ChunkedInputStream(bytes("ManMan"), 1);
        Base64InputStream in = new Base64InputStream(slow, true);
        assertArrayEquals(bytes("TWFuTWFu"), readAll(in));
        in.close();
    }

    @Test(timeout = 5000)
    public void testReadMethodWithSlowDecodeStreamDoesNotHang() throws IOException {
        InputStream slow = new ChunkedInputStream(bytes("TWFu"), 1);
        Base64InputStream in = new Base64InputStream(slow);
        assertEquals('M', in.read());
        assertEquals('a', in.read());
        assertEquals('n', in.read());
        assertEquals(-1, in.read());
        in.close();
    }

    @Test
    public void testReadArrayOffsetAndLength() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(bytes("TWFu")));
        byte[] buffer = new byte[5];
        int n = in.read(buffer, 1, 3);
        assertEquals(3, n);
        assertEquals('M', buffer[1]);
        assertEquals('a', buffer[2]);
        assertEquals('n', buffer[3]);
        assertEquals(-1, in.read());
        in.close();
    }

    @Test
    public void testReadArrayZeroLengthReturnsZero() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(bytes("TWFu")));
        byte[] b = new byte[3];
        assertEquals(0, in.read(b, 0, 0));
        assertEquals(0, in.read(new byte[0], 0, 0));
        assertEquals('M', in.read());
        in.close();
    }

    @Test
    public void testReadNullArrayThrowsNullPointerException() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(bytes("TWFu")));
        try {
            in.read(null, 0, 1);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        in.close();
    }

    @Test
    public void testReadNegativeOffsetThrowsIndexOutOfBounds() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(bytes("TWFu")));
        byte[] b = new byte[3];
        try {
            in.read(b, -1, 1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            in.read(b, 0, -1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            in.read(b, 1, 3);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        in.close();
    }

    @Test
    public void testMarkSupportedReturnsFalse() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(bytes("TWFu")));
        assertFalse(in.markSupported());
        in.close();
    }

    @Test
    public void testCloseIsIdempotent() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(bytes("TWFu")));
        in.close();
        in.close();
    }

    @Test(expected = IOException.class)
    public void testReadPropagatesIOException() throws IOException {
        InputStream broken = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("boom");
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                throw new IOException("boom");
            }
        };
        Base64InputStream in = new Base64InputStream(broken);
        in.read();
    }

    @Test(expected = IOException.class)
    public void testReadArrayPropagatesIOException() throws IOException {
        InputStream broken = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("boom");
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                throw new IOException("boom");
            }
        };
        Base64InputStream in = new Base64InputStream(broken);
        in.read(new byte[10], 0, 10);
    }

    @Test
    public void testDecodeRandomData() throws IOException {
        Random rnd = new Random(42);
        int[] lengths = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 16, 27, 54, 55, 56, 57, 100, 1024, 4096};
        for (int len : lengths) {
            byte[] original = new byte[len];
            rnd.nextBytes(original);
            String encoded = java.util.Base64.getEncoder().encodeToString(original);
            Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(bytes(encoded)));
            byte[] decoded = readAll(in);
            assertArrayEquals("Failed for length " + len, original, decoded);
            in.close();
        }
    }

    @Test
    public void testEncodeRandomData() throws IOException {
        Random rnd = new Random(42);
        int[] lengths = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 16, 27, 54, 55, 56, 57, 100, 1024, 4096};
        for (int len : lengths) {
            byte[] original = new byte[len];
            rnd.nextBytes(original);
            String expected = java.util.Base64.getEncoder().encodeToString(original);
            Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(original), true);
            byte[] encoded = readAll(in);
            assertEquals("Failed for length " + len, expected, new String(encoded, StandardCharsets.US_ASCII));
            in.close();
        }
    }

    @Test
    public void testDecodeMimeEncodedData() throws IOException {
        byte[] original = new byte[200];
        new Random(99).nextBytes(original);
        String encoded = java.util.Base64.getMimeEncoder(76, "\r\n".getBytes(StandardCharsets.US_ASCII)).encodeToString(original);
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(bytes(encoded)));
        assertArrayEquals(original, readAll(in));
        in.close();
    }

    @Test
    public void testDecodeRandomChunkedInput() throws IOException {
        Random rnd = new Random(7);
        byte[] original = new byte[1000];
        rnd.nextBytes(original);
        String encoded = java.util.Base64.getEncoder().encodeToString(original);
        for (int chunk : new int[] {1, 3, 7}) {
            InputStream slow = new ChunkedInputStream(bytes(encoded), chunk);
            Base64InputStream in = new Base64InputStream(slow);
            assertArrayEquals("chunk=" + chunk, original, readAll(in));
            in.close();
        }
    }

    private byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[256];
        int n;
        while ((n = in.read(buffer)) != -1) {
            if (n == 0) {
                throw new IOException("read(byte[]) returned 0 while expecting data");
            }
            out.write(buffer, 0, n);
        }
        return out.toByteArray();
    }

    private byte[] bytes(String s) {
        return s.getBytes(StandardCharsets.US_ASCII);
    }

    private static class ChunkedInputStream extends InputStream {
        private final byte[] data;
        private final int chunkSize;
        private int pos;

        ChunkedInputStream(byte[] data, int chunkSize) {
            this.data = data;
            this.chunkSize = chunkSize;
        }

        @Override
        public int read() {
            if (pos >= data.length) {
                return -1;
            }
            return data[pos++] & 0xff;
        }

        @Override
        public int read(byte[] b, int off, int len) {
            if (b == null) {
                throw new NullPointerException();
            }
            if (off < 0 || len < 0 || off + len > b.length) {
                throw new IndexOutOfBoundsException();
            }
            if (len == 0) {
                return 0;
            }
            if (pos >= data.length) {
                return -1;
            }
            int n = Math.min(len, Math.min(chunkSize, data.length - pos));
            System.arraycopy(data, pos, b, off, n);
            pos += n;
            return n;
        }
    }
}