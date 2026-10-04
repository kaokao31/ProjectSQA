package org.apache.commons.lang3.text.translate;

import org.junit.Assert;
import org.junit.Test;
import java.io.IOException;
import java.io.Writer;
import java.io.StringWriter;

/**
 * Test suite for CharSequenceTranslator.
 * Targets the bug present in Defects4J Lang-17 (supplementary character handling).
 */
public class CharSequenceTranslatorTest {

    // A translator that returns 0 for all inputs (no consumption).
    private static final CharSequenceTranslator NO_OP_TRANSLATOR = new CharSequenceTranslator() {
        @Override
        public int translate(CharSequence input, int index, Writer out) throws IOException {
            return 0; // claim no consumption
        }
    };

    // A translator that returns the correct number of chars consumed for the code point at index,
    // without writing anything. For BMP char: 1; for supplementary char: 2.
    private static final CharSequenceTranslator CHAR_COUNT_TRANSLATOR = new CharSequenceTranslator() {
        @Override
        public int translate(CharSequence input, int index, Writer out) throws IOException {
            if (index >= input.length()) {
                return 0;
            }
            int cp = Character.codePointAt(input, index);
            // Returns number of chars consumed (charCount) as expected by the base class.
            return Character.charCount(cp);
        }
    };

    // A translator that always throws IOException for testing exception handling.
    private static final CharSequenceTranslator IO_EXCEPTION_TRANSLATOR = new CharSequenceTranslator() {
        @Override
        public int translate(CharSequence input, int index, Writer out) throws IOException {
            throw new IOException("Forced exception");
        }
    };

    // -----------------------------------------------------------------------
    // Tests for translate(CharSequence) method
    // -----------------------------------------------------------------------

    @Test
    public void testTranslateNullInput() {
        Assert.assertNull("Null input should return null", NO_OP_TRANSLATOR.translate(null));
    }

    @Test
    public void testTranslateEmptyInput() {
        Assert.assertEquals("Empty input should return empty string", "", NO_OP_TRANSLATOR.translate(""));
    }

    @Test
    public void testTranslateAsciiInputNoOp() {
        String input = "Hello World!";
        Assert.assertEquals("For no-op translator, output should equal input", input, NO_OP_TRANSLATOR.translate(input));
    }

    @Test
    public void testTranslateSupplementaryCharacterNoOp() {
        // Supplementary character U+20BB7 (𠮷): high surrogate \uD842, low surrogate \uDFB7
        String input = "\uD842\uDFB7";
        // Under buggy implementation, this may produce incorrect result due to handling of consumed==0 branch.
        // The correct output should be the same string.
        Assert.assertEquals("Supplementary character should be preserved", input, NO_OP_TRANSLATOR.translate(input));
    }

    @Test
    public void testTranslateSupplementaryCharFollowedByAsciiNoOp() {
        // Input: supplementary character followed by 'A'
        String input = "\uD842\uDFB7A";
        // Buggy version might corrupt the 'A' (e.g. skip it or replace with '?').
        Assert.assertEquals("Supplementary char + 'A' should be preserved", input, NO_OP_TRANSLATOR.translate(input));
    }

    @Test
    public void testTranslateSupplementaryCharWithCharCountTranslator() {
        // Translator returns correct char count for each code point.
        String input = "\uD842\uDFB7A";
        // Under buggy version, the for loop in else branch double-advances the position.
        // Expected output is the input itself (since translator does not write anything).
        Assert.assertEquals("Input with supplementary char should be unchanged", input, CHAR_COUNT_TRANSLATOR.translate(input));
    }

    @Test
    public void testTranslateMixedCharsCharCountTranslator() {
        String input = "abc\uD842\uDFB7def";
        Assert.assertEquals("Mixed characters should be preserved", input, CHAR_COUNT_TRANSLATOR.translate(input));
    }

    @Test(expected = RuntimeException.class)
    public void testTranslateIOException() {
        // This should throw a RuntimeException wrapping the IOException from the translator.
        IO_EXCEPTION_TRANSLATOR.translate("test");
    }

    // -----------------------------------------------------------------------
    // Tests for translate(CharSequence, Writer) method
    // -----------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void testTranslateToNullWriter() throws IOException {
        NO_OP_TRANSLATOR.translate("test", null);
    }

    @Test
    public void testTranslateToWriterNullInput() throws IOException {
        Writer writer = new StringWriter();
        // Should not write anything and not throw.
        NO_OP_TRANSLATOR.translate(null, writer);
        Assert.assertEquals("Writer should be empty for null input", "", writer.toString());
    }

    @Test
    public void testTranslateToWriterEmptyInput() throws IOException {
        Writer writer = new StringWriter();
        NO_OP_TRANSLATOR.translate("", writer);
        Assert.assertEquals("Writer should be empty", "", writer.toString());
    }

    @Test
    public void testTranslateToWriterAsciiNoOp() throws IOException {
        Writer writer = new StringWriter();
        String input = "Hello";
        NO_OP_TRANSLATOR.translate(input, writer);
        // Because translator returns 0, the base class will write each char individually.
        Assert.assertEquals("Writer output should equal input", input, writer.toString());
    }

    @Test
    public void testTranslateToWriterSupplementaryCharNoOp() throws IOException {
        Writer writer = new StringWriter();
        String input = "\uD842\uDFB7";
        NO_OP_TRANSLATOR.translate(input, writer);
        // Under buggy implementation, this may not preserve the supplementary character correctly.
        // The expected output is the same string.
        Assert.assertEquals("Supplementary character should be preserved", input, writer.toString());
    }

    @Test
    public void testTranslateToWriterSupplementaryCharAndAsciiNoOp() throws IOException {
        Writer writer = new StringWriter();
        String input = "\uD842\uDFB7A";
        NO_OP_TRANSLATOR.translate(input, writer);
        // This is a key test for the bug: the 'A' may be lost or corrupted.
        Assert.assertEquals("Supplementary char + 'A' should be preserved", input, writer.toString());
    }

    @Test
    public void testTranslateToWriterCharCountTranslator() throws IOException {
        Writer writer = new StringWriter();
        String input = "a\uD842\uDFB7b";
        // Translator returns char count (2 for supplementary, 1 for BMP).
        // In buggy version, the for loop in else branch incorrectly advances the position.
        // Output should be the input (since translator does not write).
        CHAR_COUNT_TRANSLATOR.translate(input, writer);
        Assert.assertEquals("Input should be unchanged", input, writer.toString());
    }

    @Test
    public void testTranslateToWriterLoneHighSurrogate() throws IOException {
        Writer writer = new StringWriter();
        // Lone high surrogate (invalid in valid Unicode string, but possible in char sequence)
        String input = "\uD842";
        NO_OP_TRANSLATOR.translate(input, writer);
        // The buggy version might treat it incorrectly; expected is the same surrogate.
        Assert.assertEquals("Lone high surrogate should be preserved", input, writer.toString());
    }

    @Test
    public void testTranslateToWriterLoneLowSurrogate() throws IOException {
        Writer writer = new StringWriter();
        String input = "\uDFB7";
        NO_OP_TRANSLATOR.translate(input, writer);
        Assert.assertEquals("Lone low surrogate should be preserved", input, writer.toString());
    }

    @Test
    public void testTranslateToWriterReversedSurrogates() throws IOException {
        Writer writer = new StringWriter();
        // Invalid order: low then high surrogate
        String input = "\uDFB7\uD842";
        NO_OP_TRANSLATOR.translate(input, writer);
        // The buggy version may produce different output; expected is the invalid sequence.
        Assert.assertEquals("Reversed surrogates should be preserved as is", input, writer.toString());
    }

    // -----------------------------------------------------------------------
    // Additional edge case tests for the translate method with Writer
    // -----------------------------------------------------------------------

    @Test
    public void testTranslateToWriterWithEmptyResultFromTranslator() throws IOException {
        // Translator returns 0 for all, so the base class writes each char individually.
        Writer writer = new StringWriter();
        String input = "test";
        NO_OP_TRANSLATOR.translate(input, writer);
        Assert.assertEquals("Writer must contain all characters", input, writer.toString());
    }

    @Test
    public void testTranslateToWriterCharCountWithMultipleSupplementary() throws IOException {
        Writer writer = new StringWriter();
        // Two supplementary characters
        String input = "\uD842\uDFB7\uD843\uDFB7";
        CHAR_COUNT_TRANSLATOR.translate(input, writer);
        Assert.assertEquals("Multiple supplementary chars should be preserved", input, writer.toString());
    }
}