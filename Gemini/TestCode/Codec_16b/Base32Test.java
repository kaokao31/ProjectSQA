package org.apache.commons.codec.binary;

import org.junit.Assert;
import org.junit.Test;

public class Base32Test {

    @Test
    public void testBase32DefaultsAndConstructors() {
        Base32 base32Default = new Base32();
        Assert.assertNotNull(base32Default);

        Base32 base32LineLength = new Base32(16);
        Assert.assertNotNull(base32LineLength);

        Base32 base32LineLengthSeparator = new Base32(16, new byte[] { '\r', '\n' });
        Assert.assertNotNull(base32LineLengthSeparator);

        Base32 base32Hex = new Base32(true);
        Assert.assertNotNull(base32Hex);

        Base32 base32HexPadding = new Base32(true, (byte) '=');
        Assert.assertNotNull(base32HexPadding);
    }

    @Test
    public void testEncodeDecodeEmptyAndNull() {
        Base32 base32 = new Base32();

        // Encode null / empty
        Assert.assertNull(base32.encode(null));
        Assert.assertArrayEquals(new byte[0], base32.encode(new byte[0]));

        // Decode null / empty
        Assert.assertNull(base32.decode((String) null));
        Assert.assertNull(base32.decode((byte[]) null));
        Assert.assertArrayEquals(new byte[0], base32.decode(new byte[0]));
        Assert.assertArrayEquals(new byte[0], base32.decode(""));
    }

    @Test
    public void testEncodeDecodeBasic() {
        Base32 base32 = new Base32();

        // "foobar" in Base32 is MZXW6YTBOI======
        byte[] input = "foobar".getBytes();
        byte[] encoded = base32.encode(input);
        Assert.assertNotNull(encoded);

        byte[] decoded = base32.decode(encoded);
        Assert.assertArrayEquals(input, decoded);

        // String decode
        String encodedStr = new String(encoded);
        byte[] decodedStr = base32.decode(encodedStr);
        Assert.assertArrayEquals(input, decodedStr);
    }

    @Test
    public void testHexBase32() {
        Base32 base32Hex = new Base32(true);
        byte[] input = "foobar".getBytes();
        byte[] encoded = base32Hex.encode(input);
        Assert.assertNotNull(encoded);

        byte[] decoded = base32Hex.decode(encoded);
        Assert.assertArrayEquals(input, decoded);
    }

    @Test
    public void testIsInAlphabet() {
        Base32 base32 = new Base32();
        // Check valid Base32 characters (e.g., 'A', 'Z', '2', '7')
        Assert.assertTrue(base32.isInAlphabet((byte) 'A'));
        Assert.assertTrue(base32.isInAlphabet((byte) '2'));
        // Check invalid characters
        Assert.assertFalse(base32.isInAlphabet((byte) '8'));
        Assert.assertFalse(base32.isInAlphabet((byte) '9'));
        Assert.assertFalse(base32.isInAlphabet((byte) '='));
    }

    @Test
    public void testObjectEncodeDecode() throws Exception {
        Base32 base32 = new Base32();
        byte[] input = "test".getBytes();

        Object encodedObj = base32.encode((Object) input);
        Assert.assertNotNull(encodedObj);

        Object decodedObj = base32.decode(encodedObj);
        Assert.assertNotNull(decodedObj);
    }

    @Test(expected = org.apache.commons.codec.EncoderException.class)
    public void testObjectEncodeException() throws Exception {
        Base32 base32 = new Base32();
        base32.encode(new Object());
    }

    @Test(expected = org.apache.commons.codec.DecoderException.class)
    public void testObjectDecodeException() throws Exception {
        Base32 base32 = new Base32();
        base32.decode(new Object());
    }

    @Test
    public void testStreamDecodeAndChunking() {
        Base32 base32 = new Base32(4, new byte[] { '\n' });
        byte[] input = "The quick brown fox jumps over the lazy dog.".getBytes();
        byte[] encoded = base32.encode(input);
        byte[] decoded = base32.decode(encoded);
        Assert.assertArrayEquals(input, decoded);
    }
}