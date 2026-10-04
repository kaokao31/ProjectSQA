package org.apache.commons.lang3.text.translate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Unit tests for {@link NumericEntityUnescaper}.
 */
public class NumericEntityUnescaperTest {

    @Test
    public void testSupplementaryUnescaping() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        String input = "&#x12040;";
        String expected = "\uD808\uDC40";
        String result = neu.translate(input);
        assertEquals("Failed to unescape numeric entities supplementary characters", expected, result);

        input = "&#73792;";
        result = neu.translate(input);
        assertEquals("Failed to unescape numeric entities supplementary characters (decimal)", expected, result);
    }

    @Test
    public void testDecimalUnescaping() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        String input = "&#65;&#66;&#67;";
        String expected = "ABC";
        String result = neu.translate(input);
        assertEquals("Failed to unescape decimal numeric entities", expected, result);
    }

    @Test
    public void testHexUnescaping() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        String input = "&#x41;&#X42;&#x43;";
        String expected = "ABC";
        String result = neu.translate(input);
        assertEquals("Failed to unescape hexadecimal numeric entities", expected, result);
    }

    @Test
    public void testUnfinishedEntity() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        String input = "&#";
        String result = neu.translate(input);
        assertEquals("&#", result);

        input = "&#x";
        result = neu.translate(input);
        assertEquals("&#x", result);

        input = "&";
        result = neu.translate(input);
        assertEquals("&", result);

        input = "Plain text with & and &# not entity";
        result = neu.translate(input);
        assertEquals("Plain text with & and &# not entity", result);
    }

    @Test
    public void testOutOfBoundsAndEmpty() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        assertEquals("", neu.translate(""));
        assertEquals(null, neu.translate(null));
    }

    @Test
    public void testMixedContent() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        String input = "Hello &#65; World &#x42;! &#x12040; Test";
        String expected = "Hello A World B! \uD808\uDC40 Test";
        String result = neu.translate(input);
        assertEquals(expected, result);
    }

    @Test
    public void testVariousOptionsIfAvailable() {
        try {
            NumericEntityUnescaper neu = new NumericEntityUnescaper(NumericEntityUnescaper.OPTION.semiColonOptional);
            String input = "&#65";
            String result = neu.translate(input);
            assertEquals("A", result);

            input = "&#x41";
            result = neu.translate(input);
            assertEquals("A", result);
        } catch (NoSuchFieldError | NoClassDefFoundError ignored) {
            // Options might not exist in all versions of the class
        }
    }

    @Test
    public void testErrorIfNoSemiColon() {
        try {
            NumericEntityUnescaper neu = new NumericEntityUnescaper(NumericEntityUnescaper.OPTION.errorIfNoSemiColon);
            try {
                neu.translate("&#65");
                fail("Expected IllegalArgumentException when semicolon is missing");
            } catch (IllegalArgumentException e) {
                // Expected exception
            }
        } catch (NoSuchFieldError | NoClassDefFoundError ignored) {
            // Options might not exist in all versions of the class
        }
    }
}