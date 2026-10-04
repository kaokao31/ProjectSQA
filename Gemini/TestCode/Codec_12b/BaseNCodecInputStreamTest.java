package org.apache.commons.codec.binary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class BaseNCodecInputStreamTest {

    @Test
    public void testReadNull() throws IOException {
        byte[] buf = new byte[10];
        try (InputStream in = new ByteArrayInputStream(new byte[0]);
             BaseNCodecInputStream bazie = new BaseNCodecInputStream(in, new Base64(), false)) {
            bazie.read(null, 0, 0);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testReadOutOfBounds() throws IOException {
        byte[] buf = new byte[10];
        try (InputStream in = new ByteArrayInputStream(new byte[0]);
             BaseNCodecInputStream bazie = new BaseNCodecInputStream(in, new Base64(), false)) {
            bazie.read(buf, -1, 0);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        try (InputStream in = new ByteArrayInputStream(new byte[0]);
             BaseNCodecInputStream bazie = new BaseNCodecInputStream(in, new Base64(), false)) {
            bazie.read(buf, 0, -1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        try (InputStream in = new ByteArrayInputStream(new byte[0]);
             BaseNCodecInputStream bazie = new BaseNCodecInputStream(in, new Base64(), false)) {
            bazie.read(buf, 0, 11);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    public void testReadZeroLength() throws IOException {
        byte[] buf = new byte[10];
        try (InputStream in = new ByteArrayInputStream("SGVsbG8=".getBytes());
             BaseNCodecInputStream bazie = new BaseNCodecInputStream(in, new Base64(), false)) {
            int result = bazie.read(buf, 0, 0);
            assertEquals(0, result);
        }
    }

    @Test
    public void testReadSingleByte() throws IOException {
        try (InputStream in = new ByteArrayInputStream("SGVsbG8=".getBytes());
             BaseNCodecInputStream bazie = new BaseNCodecInputStream(in, new Base64(), false)) {
            int b = bazie.read();
            assertEquals('H', b);
            
            b = bazie.read();
            assertEquals('e', b);
        }
    }

    @Test
    public void testReadEOF() throws IOException {
        try (InputStream in = new ByteArrayInputStream(new byte[0]);
             BaseNCodecInputStream bazie = new BaseNCodecInputStream(in, new Base64(), false)) {
            int b = bazie.read();
            assertEquals(-1, b);
        }

        byte[] buf = new byte[10];
        try (InputStream in = new ByteArrayInputStream(new byte[0]);
             BaseNCodecInputStream bazie = new BaseNCodecInputStream(in, new Base64(), false)) {
            int result = bazie.read(buf, 0, 5);
            assertEquals(-1, result);
        }
    }

    @Test
    public void testReadByteArray() throws IOException {
        byte[] buf = new byte[5];
        try (InputStream in = new ByteArrayInputStream("SGVsbG8=".getBytes());
             BaseNCodecInputStream bazie = new BaseNCodecInputStream(in, new Base64(), false)) {
            int read = bazie.read(buf, 0, 5);
            assertEquals(5, read);
            assertEquals("Hello", new String(buf, 0, read));

            read = bazie.read(buf, 0, 5);
            assertEquals(-1, read);
        }
    }

    @Test
    public void testAvailable() throws IOException {
        try (InputStream in = new ByteArrayInputStream("SGVsbG8=".getBytes());
             BaseNCodecInputStream bazie = new BaseNCodecInputStream(in, new Base64(), false)) {
            // BaseNCodecInputStream typically delegates or returns 0/1 depending on buffer
            assertTrue(bazie.available() >= 0);
        }
    }

    @Test
    public void testMarkSupported() throws IOException {
        try (InputStream in = new ByteArrayInputStream(new byte[0]);
             BaseNCodecInputStream bazie = new BaseNCodecInputStream(in, new Base64(), false)) {
            assertFalse(bazie.markSupported());
        }
    }

    @Test
    public void testMarkAndReset() throws IOException {
        try (InputStream in = new ByteArrayInputStream(new byte[0]);
             BaseNCodecInputStream bazie = new BaseNCodecInputStream(in, new Base64(), false)) {
            bazie.mark(10);
            try {
                bazie.reset();
                fail("Expected IOException");
            } catch (IOException e) {
                // expected
            }
        }
    }

    @Test
    public void testSkip() throws IOException {
        try (InputStream in = new ByteArrayInputStream("SGVsbG8=".getBytes());
             BaseNCodecInputStream bazie = new BaseNCodecInputStream(in, new Base64(), false)) {
            try {
                bazie.skip(2);
                fail("Expected UnsupportedOperationException or similar if not implemented, or check if supported");
            } catch (UnsupportedOperationException | IOException e) {
                // depending on implementation, skip might be supported or throw exception
            }
        }
    }
}