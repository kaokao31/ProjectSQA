package org.apache.commons.codec.binary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class Base64InputStreamTest {

    @Test
    public void testConstructorDefault() throws Exception {
        byte[] bArray = StringUtils.getBytesUtf8("SGVsbG8gV29ybGQ=");
        ByteArrayInputStream bais = new ByteArrayInputStream(bArray);
        try (Base64InputStream b64is = new Base64InputStream(bais)) {
            byte[] buf = new byte[1024];
            int actual = b64is.read(buf);
            String result = new String(buf, 0, actual, "UTF-8");
            assertEquals("Hello World", result);
        }
    }

    @Test
    public void testConstructorBoolean() throws Exception {
        byte[] bArray = StringUtils.getBytesUtf8("SGVsbG8gV29ybGQ=");
        ByteArrayInputStream bais = new ByteArrayInputStream(bArray);
        try (Base64InputStream b64is = new Base64InputStream(bais, true)) {
            byte[] buf = new byte[1024];
            int actual = b64is.read(buf);
            String result = new String(buf, 0, actual, "UTF-8");
            assertEquals("Hello World", result);
        }
    }

    @Test
    public void testConstructorBooleanByteArrayInt() throws Exception {
        byte[] bArray = StringUtils.getBytesUtf8("SGVsbG8gV29ybGQ=");
        ByteArrayInputStream bais = new ByteArrayInputStream(bArray);
        byte[] encodedLine = new byte[] { '\r', '\n' };
        try (Base64InputStream b64is = new Base64InputStream(bais, true, 76, encodedLine)) {
            byte[] buf = new byte[1024];
            int actual = b64is.read(buf);
            String result = new String(buf, 0, actual, "UTF-8");
            assertEquals("Hello World", result);
        }
    }

    @Test
    public void testReadNullByteArray() throws Exception {
        byte[] bArray = StringUtils.getBytesUtf8("SGVsbG8gV29ybGQ=");
        ByteArrayInputStream bais = new ByteArrayInputStream(bArray);
        try (Base64InputStream b64is = new Base64InputStream(bais)) {
            try {
                b64is.read(null, 0, 1);
                fail("Expected NullPointerException");
            } catch (NullPointerException e) {
                // Expected
            }
        }
    }

    @Test
    public void testReadOutOfBounds() throws Exception {
        byte[] bArray = StringUtils.getBytesUtf8("SGVsbG8gV29ybGQ=");
        ByteArrayInputStream bais = new ByteArrayInputStream(bArray);
        try (Base64InputStream b64is = new Base64InputStream(bais)) {
            byte[] buf = new byte[10];
            try {
                b64is.read(buf, -1, 1);
                fail("Expected IndexOutOfBoundsException");
            } catch (IndexOutOfBoundsException e) {
                // Expected
            }
            try {
                b64is.read(buf, 0, -1);
                fail("Expected IndexOutOfBoundsException");
            } catch (IndexOutOfBoundsException e) {
                // Expected
            }
            try {
                b64is.read(buf, 0, 11);
                fail("Expected IndexOutOfBoundsException");
            } catch (IndexOutOfBoundsException e) {
                // Expected
            }
            try {
                b64is.read(buf, 5, 6);
                fail("Expected IndexOutOfBoundsException");
            } catch (IndexOutOfBoundsException e) {
                // Expected
            }
        }
    }

    @Test
    public void testReadZeroLength() throws Exception {
        byte[] bArray = StringUtils.getBytesUtf8("SGVsbG8gV29ybGQ=");
        ByteArrayInputStream bais = new ByteArrayInputStream(bArray);
        try (Base64InputStream b64is = new Base64InputStream(bais)) {
            byte[] buf = new byte[10];
            int actual = b64is.read(buf, 0, 0);
            assertEquals(0, actual);
        }
    }

    @Test
    public void testSkip() throws Exception {
        byte[] bArray = StringUtils.getBytesUtf8("SGVsbG8gV29ybGQ=");
        ByteArrayInputStream bais = new ByteArrayInputStream(bArray);
        try (Base64InputStream b64is = new Base64InputStream(bais)) {
            long skipped = b64is.skip(3);
            assertEquals(3, skipped);
            
            byte[] buf = new byte[1024];
            int actual = b64is.read(buf);
            String result = new String(buf, 0, actual, "UTF-8");
            assertEquals("lo World", result);
        }
    }

    @Test
    public void testSkipNegative() throws Exception {
        byte[] bArray = StringUtils.getBytesUtf8("SGVsbG8gV29ybGQ=");
        ByteArrayInputStream bais = new ByteArrayInputStream(bArray);
        try (Base64InputStream b64is = new Base64InputStream(bais)) {
            try {
                b64is.skip(-1);
                fail("Expected IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                // Expected
            }
        }
    }

    @Test
    public void testSkipZero() throws Exception {
        byte[] bArray = StringUtils.getBytesUtf8("SGVsbG8gV29ybGQ=");
        ByteArrayInputStream bais = new ByteArrayInputStream(bArray);
        try (Base64InputStream b64is = new Base64InputStream(bais)) {
            long skipped = b64is.skip(0);
            assertEquals(0, skipped);
        }
    }

    @Test
    public void testAvailable() throws Exception {
        byte[] bArray = StringUtils.getBytesUtf8("SGVsbG8gV29ybGQ=");
        ByteArrayInputStream bais = new ByteArrayInputStream(bArray);
        try (Base64InputStream b64is = new Base64InputStream(bais)) {
            // Base64InputStream available() generally returns 0 or delegates in a specific way depending on codec implementation
            int available = b64is.available();
            assertEquals(0, available);
        }
    }

    @Test
    public void testMarkSupported() throws Exception {
        byte[] bArray = StringUtils.getBytesUtf8("SGVsbG8gV29ybGQ=");
        ByteArrayInputStream bais = new ByteArrayInputStream(bArray);
        try (Base64InputStream b64is = new Base64InputStream(bais)) {
            assertFalse(b64is.markSupported());
        }
    }

    @Test
    public void testMarkAndReset() throws Exception {
        byte[] bArray = StringUtils.getBytesUtf8("SGVsbG8gV29ybGQ=");
        ByteArrayInputStream bais = new ByteArrayInputStream(bArray);
        try (Base64InputStream b64is = new Base64InputStream(bais)) {
            b64is.mark(10);
            try {
                b64is.reset();
                fail("Expected IOException");
            } catch (IOException e) {
                // Expected
            }
        }
    }

    @Test
    public void testReadSingleByte() throws Exception {
        byte[] bArray = StringUtils.getBytesUtf8("SGVsbG8=");
        ByteArrayInputStream bais = new ByteArrayInputStream(bArray);
        try (Base64InputStream b64is = new Base64InputStream(bais)) {
            int b1 = b64is.read(); // 'H'
            int b2 = b64is.read(); // 'e'
            int b3 = b64is.read(); // 'l'
            int b4 = b64is.read(); // 'l'
            int b5 = b64is.read(); // 'o'
            int b6 = b64is.read(); // -1 (EOF)
            
            assertEquals('H', b1);
            assertEquals('e', b2);
            assertEquals('l', b3);
            assertEquals('l', b4);
            assertEquals('o', b5);
            assertEquals(-1, b6);
        }
    }

    @Test
    public void testEncodeStream() throws Exception {
        byte[] bArray = StringUtils.getBytesUtf8("Hello World");
        ByteArrayInputStream bais = new ByteArrayInputStream(bArray);
        try (Base64InputStream b64is = new Base64InputStream(bais, true)) {
            byte[] buf = new byte[1024];
            int actual = b64is.read(buf);
            String result = new String(buf, 0, actual, "UTF-8");
            assertEquals("SGVsbG8gV29ybGQ=", result);
        }
    }
}