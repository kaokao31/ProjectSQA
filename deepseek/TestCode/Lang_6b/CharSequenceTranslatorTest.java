package org.apache.commons.lang3.text.translate;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class CharSequenceTranslatorTest {

    private CharSequenceTranslator identityTranslator;
    private CharSequenceTranslator surrogatePairBugTranslator;

    @Before
    public void setUp() {
        // A translator that passes through all characters unchanged
        identityTranslator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                out.write(input.charAt(index));
                return 1;
            }
        };

        // A translator that attempts to consume a surrogate pair when it sees a high surrogate,
        // without checking if there is a low surrogate. This reproduces the bug pattern.
        surrogatePairBugTranslator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                char c = input.charAt(index);
                if (Character.isHighSurrogate(c)) {
                    // Intentionally no bounds check: index+1 may be out of range
                    char low = input.charAt(index + 1);
                    out.write("PAIR");
                    return 2;
                } else {
                    out.write(c);
                    return 1;
                }
            }
        };
    }

    @Test
    public void testTranslateNullInput() {
        assertNull(identityTranslator.translate((CharSequence) null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTranslateNullWriter() throws IOException {
        identityTranslator.translate("test", null);
    }

    @Test
    public void testTranslateEmptyString() {
        assertEquals("", identityTranslator.translate(""));
    }

    @Test
    public void testTranslateSingleChar() {
        assertEquals("a", identityTranslator.translate("a"));
    }

    @Test
    public void testTranslateAsciiString() {
        assertEquals("hello", identityTranslator.translate("hello"));
    }

    @Test
    public void testTranslateWithNoConsumption() {
        // A translator returning 0 should cause the loop to write the char and advance manually
        CharSequenceTranslator zeroConsumeTranslator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };
        // Using zeroConsumeTranslator, the result should be the same string (write each char separately)
        assertEquals("abc", zeroConsumeTranslator.translate("abc"));
    }

    @Test
    public void testTranslatePartialConsumption() {
        // Consume 2 chars but write only the first (simulate a two-char sequence that reduces)
        CharSequenceTranslator partialTranslator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                out.write(input.charAt(index));
                return 2; // consumes two but only writes one
            }
        };
        // For "abcd", should write 'a', advance to index2 ('c'), write 'c', advance to end.
        // Result "ac"
        assertEquals("ac", partialTranslator.translate("abcd"));
    }

    @Test
    public void testTranslateSurrogatePair() throws IOException {
        // Use the identity translator on a genuine surrogate pair
        String surrogatePair = "\uD800\uDC00"; // Unicode code point U+10000
        String result = identityTranslator.translate(surrogatePair);
        assertEquals(surrogatePair, result);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testTranslateSurrogatePairEndingWithHighSurrogate() throws IOException {
        // A string that ends with a high surrogate, no low surrogate
        String malformed = "\uD800"; // high surrogate only
        // The buggy translator will try to access index+1, causing out-of-bounds
        surrogatePairBugTranslator.translate(malformed);
    }

    @Test
    public void testTranslateViaWriter() throws IOException {
        String input = "test";
        StringWriter writer = new StringWriter();
        identityTranslator.translate(input, writer);
        assertEquals(input, writer.toString());
    }

    @Test
    public void testTranslateWriterNullInput() throws IOException {
        StringWriter writer = new StringWriter();
        identityTranslator.translate(null, writer);
        assertTrue(writer.toString().isEmpty()); // Null input should not write anything
    }

    @Test
    public void testTranslateWriterWithMultipleCalls() throws IOException {
        // Verify that the translate(Writer) method can be called multiple times
        String input = "hello";
        StringWriter writer = new StringWriter();
        identityTranslator.translate(input, writer);
        identityTranslator.translate(" world", writer);
        assertEquals("hello world", writer.toString());
    }

    @Test(expected = IOException.class)
    public void testTranslateWriterThrowsIOException() throws IOException {
        // A translator that throws IOException
        CharSequenceTranslator throwingTranslator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                throw new IOException("forced");
            }
        };
        throwingTranslator.translate("test", new StringWriter());
    }

    @Test
    public void testTranslateLargeString() {
        // Test with a string longer than typical to stress the loop
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append((char) ('a' + (i % 26)));
        }
        String largeInput = sb.toString();
        assertEquals(largeInput, identityTranslator.translate(largeInput));
    }
}