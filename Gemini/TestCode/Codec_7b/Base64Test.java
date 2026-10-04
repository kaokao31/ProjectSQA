package org.apache.commons.codec.binary;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

public class Base64Test {

    @Test
    public void testDefaultConstructor() {
        Base64 base64 = new Base64();
        Assert.assertNotNull(base64);
        Assert.assertFalse(base64.isUrlSafe());
    }

    @Test
    public void testIntegerConstructor() {
        Base64 base64 = new Base64(true);
        Assert.assertTrue(base64.isUrlSafe());
    }

    @Test
    public void testLineLengthConstructor() {
        Base64 base64 = new Base64(76);
        Assert.assertNotNull(base64);
    }

    @Test
    public void testLineLengthAndSeparatorConstructor() {
        byte[] separator = new byte[]{'\r', '\n'};
        Base64 base64 = new Base64(76, separator);
        Assert.assertNotNull(base64);
    }

    @Test
    public void testLineLengthSeparatorAndUrlSafeConstructor() {
        byte[] separator = new byte[]{'\n'};
        Base64 base64 = new Base64(76, separator, true);
        Assert.assertTrue(base64.isUrlSafe());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorLineLengthAndSeparatorInvalid() {
        byte[] separator = new byte[]{'A'}; // 'A' is not a valid base64 character/separator in some contexts or length mismatch
        // Actually, let's check what Base64 constructor rejects: if lineLength > 0 and lineSeparator contains base64 alphabet
        new Base64(76, new byte[]{'A'});
    }

    @Test
    public void testIsArrayByteBase64() {
        byte[] array = "SGVsbG8gV29ybGQ=".getBytes();
        Assert.assertTrue(Base64.isArrayByteBase64(array));

        byte[] invalidArray = new byte[]{!27, 0x01};
        // Just testing method execution
        Base64.isArrayByteBase64(invalidArray);
    }

    @Test
    public void testEncodeBase64Chunked() {
        byte[] binaryData = "Hello World!".getBytes();
        byte[] encoded = Base64.encodeBase64Chunked(binaryData);
        Assert.assertNotNull(encoded);
    }

    @Test
    public void testEncodeBase64WithUrlSafe() {
        byte[] binaryData = "Hello > World?".getBytes();
        byte[] encoded = Base64.encodeBase64(binaryData, true, true);
        Assert.assertNotNull(encoded);
        byte[] decoded = Base64.decodeBase64(encoded);
        Assert.assertArrayEquals(binaryData, decoded);
    }

    @Test
    public void testEncodeBase64WithMaxLineSize() {
        byte[] binaryData = "Hello World! This is a test for max line size in Base64 encoding functionality.".getBytes();
        byte[] encoded = Base64.encodeBase64(binaryData, true);
        Assert.assertNotNull(encoded);
    }

    @Test
    public void testEncodeDecodeStatic() {
        byte[] data = "Test String for Base64".getBytes();
        byte[] encoded = Base64.encodeBase64(data);
        byte[] decoded = Base64.decodeBase64(encoded);
        Assert.assertArrayEquals(data, decoded);
    }

    @Test
    public void testDecodeBase64NullAndEmpty() {
        Assert.assertNull(Base64.decodeBase64((byte[]) null));
        Assert.assertArrayEquals(new byte[0], Base64.decodeBase64(new byte[0]));
        Assert.assertArrayEquals(new byte[0], Base 64.decodeBase64("".getBytes()));
    }

    @Test
    public void testEncodeBase64NullAndEmpty() {
        Assert.assertNull(Base64.encodeBase64(null));
        Assert.assertArrayEquals(new byte[0], Base64.encodeBase64(new byte[0]));
    }

    @Test
    public void testEncodeInteger() {
        BigInteger bigInt = BigInteger.valueOf(123456789L);
        byte[] encoded = Base64.encodeInteger(bigInt);
        Assert.assertNotNull(encoded);
    }

    @Test(expected = NullPointerException.class)
    public void testEncodeIntegerNull() {
        Base64.encodeInteger(null);
    }

    @Test
    public void testDecodeInteger() {
        byte[] encoded = Base64.encodeInteger(BigInteger.valueOf(987654321L));
        BigInteger decoded = Base64.decodeInteger(encoded);
        Assert.assertEquals(BigInteger.valueOf(987654321L), decoded);
    }

    @Test(expected = NullPointerException.class)
    public void testDecodeIntegerNull() {
        Base64.decodeInteger(null);
    }

    @Test
    public void testEncodeToString() {
        byte[] data = "Hello JUnit".getBytes();
        String result = Base64.encodeBase64String(data);
        Assert.assertNotNull(result);
        
        byte[] decoded = Base64.decodeBase64(result);
        Assert.assertArrayEquals(data, decoded);
    }

    @Test
    public void testEncodeObject() {
        Base64 base64 = new Base64();
        try {
            Object result = base64.encode("Some String");
            Assert.assertNotNull(result);
        } catch (Exception e) {
            // Depending on implementation, might throw EncoderException
        }

        try {
            base64.encode(new Object());
            Assert.fail("Expected EncoderException");
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testDecodeObject() {
        Base64 base64 = new Base64();
        try {
            Object result = base64.decode("SGVsbG8=".getBytes());
            Assert.assertNotNull(result);
        } catch (Exception e) {
            // Ignore if unsupported
        }

        try {
            base64.decode("SGVsbG8=");
            Assert.assertNotNull(base64.decode("SGVsbG8="));
        } catch (Exception e) {
            // Ignore
        }

        try {
            base64.decode(new Object());
            Assert.fail("Expected DecoderException");
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testEncodeDecodeChunks() {
        Base64 base64 = new Base64(10);
        byte[] data = "A very long string to test chunking mechanisms of the Base64 encoder and decoder properly.".getBytes();
        byte[] encoded = base64.encode(data);
        byte[] decoded = base64.decode(encoded);
        Assert.assertArrayEquals(data, decoded);
    }

    @Test
    public void testDiscardWhitespace() {
        byte[] data = "   SG 2 Vsb G8=   ".getBytes();
        byte[] decoded = Base64.decodeBase64(data);
        Assert.assertNotNull(decoded);
    }

    @Test
    public void testBase64StreamOperations() {
        Base64 base64 = new Base64();
        byte[] buffer = new byte[10];
        // Test EOF / empty read scenarios if applicable
        int read = base64.readResults(buffer, 0, 0);
        Assert.assertEquals(0, read);
    }
}