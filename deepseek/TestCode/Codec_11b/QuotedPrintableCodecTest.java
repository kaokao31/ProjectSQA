package org.apache.commons.codec.net;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.nio.charset.StandardCharsets;
import java.util.BitSet;

import org.apache.commons.codec.DecoderException;
import org.junit.Test;

public class QuotedPrintableCodecTest {

    @Test
    public void testNullInputs() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec("UTF-8");

        assertNull(QuotedPrintableCodec.encodeQuotedPrintable(null, null));
        assertNull(QuotedPrintableCodec.decodeQuotedPrintable(null));

        assertNull(codec.encode((byte[]) null));
        assertNull(codec.decode((byte[]) null));
        assertNull(codec.encode((String) null));
        assertNull(codec.decode((String) null));
        assertNull(codec.encode((String) null, "UTF-8"));
        assertNull(codec.decode((String) null, "UTF-8"));
    }

    @Test
    public void testDefaultConstructorHasCharset() {
        assertNotNull(new QuotedPrintableCodec().getDefaultCharset());
    }

    @Test
    public void testDefaultCharset() {
        assertEquals("UTF-8", new QuotedPrintableCodec("UTF-8").getDefaultCharset());
        assertEquals("ISO-8859-1", new QuotedPrintableCodec("ISO-8859-1").getDefaultCharset());
    }

    @Test
    public void testEncodeQuotedPrintableEmpty() {
        assertArrayEquals(new byte[0], QuotedPrintableCodec.encodeQuotedPrintable(null, new byte[0]));
    }

    @Test
    public void testEncodeQuotedPrintablePrintable() throws Exception {
        byte[] input = "Hello, World! 123".getBytes(StandardCharsets.US_ASCII);
        assertArrayEquals(input, QuotedPrintableCodec.encodeQuotedPrintable(null, input));

        byte[] tab = new byte[]{'\t'};
        assertArrayEquals(tab, QuotedPrintableCodec.encodeQuotedPrintable(null, tab));
    }

    @Test
    public void testEncodeQuotedPrintableNonPrintable() throws Exception {
        byte[] input = new byte[]{0, 1, 31, 127, (byte) 128, (byte) 255};
        byte[] expected = "=00=01=1F=7F=80=FF".getBytes(StandardCharsets.US_ASCII);
        assertArrayEquals(expected, QuotedPrintableCodec.encodeQuotedPrintable(null, input));
    }

    @Test
    public void testEncodeQuotedPrintableEqualsAndLineBreaks() {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();

        assertArrayEquals("=3D".getBytes(StandardCharsets.US_ASCII), codec.encode(new byte[]{'='}));
        assertArrayEquals("=0D".getBytes(StandardCharsets.US_ASCII), codec.encode(new byte[]{'\r'}));
        assertArrayEquals("=0A".getBytes(StandardCharsets.US_ASCII), codec.encode(new byte[]{'\n'}));
    }

    @Test
    public void testEncodeQuotedPrintableCustomBitSet() throws Exception {
        BitSet printable = new BitSet();
        printable.set('B');
        assertArrayEquals("=41B".getBytes(StandardCharsets.US_ASCII),
                QuotedPrintableCodec.encodeQuotedPrintable(printable, "AB".getBytes(StandardCharsets.US_ASCII)));

        BitSet empty = new BitSet();
        assertArrayEquals("=20".getBytes(StandardCharsets.US_ASCII),
                QuotedPrintableCodec.encodeQuotedPrintable(empty, new byte[]{' '}));

        BitSet high = new BitSet();
        high.set(128);
        assertArrayEquals(new byte[]{(byte) 0x80},
                QuotedPrintableCodec.encodeQuotedPrintable(high, new byte[]{(byte) 0x80}));
    }

    @Test
    public void testDecodeQuotedPrintableSimple() throws Exception {
        byte[] input = "abc".getBytes(StandardCharsets.US_ASCII);
        assertArrayEquals(input, QuotedPrintableCodec.decodeQuotedPrintable(input));

        byte[] mixed = "ab=40cd".getBytes(StandardCharsets.US_ASCII);
        assertArrayEquals("ab@cd".getBytes(StandardCharsets.US_ASCII), QuotedPrintableCodec.decodeQuotedPrintable(mixed));
    }

    @Test
    public void testDecodeQuotedPrintableHex() throws Exception {
        assertArrayEquals("A".getBytes(StandardCharsets.US_ASCII),
                QuotedPrintableCodec.decodeQuotedPrintable("=41".getBytes(StandardCharsets.US_ASCII)));
        assertArrayEquals("ABC".getBytes(StandardCharsets.US_ASCII),
                QuotedPrintableCodec.decodeQuotedPrintable("=41=42=43".getBytes(StandardCharsets.US_ASCII)));
        assertArrayEquals("j".getBytes(StandardCharsets.US_ASCII),
                QuotedPrintableCodec.decodeQuotedPrintable("=6a".getBytes(StandardCharsets.US_ASCII)));
        assertArrayEquals(new byte[]{(byte) 0x80},
                QuotedPrintableCodec.decodeQuotedPrintable("=80".getBytes(StandardCharsets.US_ASCII)));
    }

    @Test
    public void testDecodeQuotedPrintableInvalid() throws Exception {
        assertDecodeInvalid(new byte[]{'='});
        assertDecodeInvalid("=4".getBytes(StandardCharsets.US_ASCII));
        assertDecodeInvalid("=GG".getBytes(StandardCharsets.US_ASCII));
        assertDecodeInvalid("=4G".getBytes(StandardCharsets.US_ASCII));
        assertDecodeInvalid("=G4".getBytes(StandardCharsets.US_ASCII));
        assertDecodeInvalid("= ".getBytes(StandardCharsets.US_ASCII));
    }

    @Test
    public void testDecodeQuotedPrintableSoftLineBreak() throws Exception {
        assertArrayEquals(new byte[0],
                QuotedPrintableCodec.decodeQuotedPrintable("=\r\n".getBytes(StandardCharsets.US_ASCII)));
        assertArrayEquals("abcdef".getBytes(StandardCharsets.US_ASCII),
                QuotedPrintableCodec.decodeQuotedPrintable("abc=\r\ndef".getBytes(StandardCharsets.US_ASCII)));
        assertArrayEquals("AB".getBytes(StandardCharsets.US_ASCII),
                QuotedPrintableCodec.decodeQuotedPrintable("=41=\r\n=42".getBytes(StandardCharsets.US_ASCII)));
        assertArrayEquals("abc".getBytes(StandardCharsets.US_ASCII),
                QuotedPrintableCodec.decodeQuotedPrintable("abc=\r\n".getBytes(StandardCharsets.US_ASCII)));

        assertEquals("abcdef", new QuotedPrintableCodec().decode("abc=\r\ndef", "US-ASCII"));
    }

    @Test
    public void testStringEncodeDecode() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec("UTF-8");

        assertEquals("=C3=A9", codec.encode("\u00e9"));
        assertEquals("\u00e9", codec.decode("=C3=A9"));
        assertEquals("abc", codec.encode("abc"));
        assertEquals("abc", codec.decode("abc"));
    }

    @Test
    public void testStringEncodeDecodeWithCharset() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec("ISO-8859-1");

        assertEquals("=E9", codec.encode("\u00e9", "ISO-8859-1"));
        assertEquals("\u00e9", codec.decode("=E9", "ISO-8859-1"));
    }

    private void assertDecodeInvalid(byte[] input) throws DecoderException {
        try {
            QuotedPrintableCodec.decodeQuotedPrintable(input);
            fail("Expected DecoderException for invalid input");
        } catch (DecoderException e) {
            // Expected
        }
    }
}