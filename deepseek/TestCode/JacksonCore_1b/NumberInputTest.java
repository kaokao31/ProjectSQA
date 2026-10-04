package defects4j;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for NumberInput class.
 * Assumes NumberInput has a static method: public static int parseNumber(String input)
 * that parses an integer from a string, supporting decimal, hex (0x), octal (0), binary (0b).
 * Throws NumberFormatException for invalid inputs, IllegalArgumentException for null.
 */
public class NumberInputTest {

    @Test(expected = IllegalArgumentException.class)
    public void testNullInput() {
        NumberInput.parseNumber(null);
    }

    @Test(expected = NumberFormatException.class)
    public void testEmptyInput() {
        NumberInput.parseNumber("");
    }

    @Test
    public void testValidDecimal() {
        assertEquals(123, NumberInput.parseNumber("123"));
    }

    @Test
    public void testNegativeDecimal() {
        assertEquals(-123, NumberInput.parseNumber("-123"));
    }

    @Test
    public void testPositiveWithPlus() {
        assertEquals(123, NumberInput.parseNumber("+123"));
    }

    @Test
    public void testHex() {
        assertEquals(26, NumberInput.parseNumber("0x1A"));
    }

    @Test
    public void testHexLowerCase() {
        assertEquals(26, NumberInput.parseNumber("0x1a"));
    }

    @Test
    public void testOctal() {
        assertEquals(15, NumberInput.parseNumber("017"));
    }

    @Test
    public void testBinary() {
        assertEquals(5, NumberInput.parseNumber("0b101"));
    }

    @Test(expected = NumberFormatException.class)
    public void testInvalidHex() {
        NumberInput.parseNumber("0xG");
    }

    @Test(expected = NumberFormatException.class)
    public void testInvalidOctal() {
        NumberInput.parseNumber("018");
    }

    @Test(expected = NumberFormatException.class)
    public void testInvalidBinary() {
        NumberInput.parseNumber("0b102");
    }

    @Test
    public void testZero() {
        assertEquals(0, NumberInput.parseNumber("0"));
    }

    @Test
    public void testNegativeZero() {
        assertEquals(0, NumberInput.parseNumber("-0"));
    }

    @Test
    public void testMaxInt() {
        assertEquals(Integer.MAX_VALUE, NumberInput.parseNumber("2147483647"));
    }

    @Test
    public void testMinInt() {
        assertEquals(Integer.MIN_VALUE, NumberInput.parseNumber("-2147483648"));
    }

    @Test(expected = NumberFormatException.class)
    public void testOverflow() {
        NumberInput.parseNumber("2147483648");
    }

    @Test(expected = NumberFormatException.class)
    public void testUnderflow() {
        NumberInput.parseNumber("-2147483649");
    }

    @Test(expected = NumberFormatException.class)
    public void testLeadingSpaces() {
        NumberInput.parseNumber(" 123");
    }

    @Test(expected = NumberFormatException.class)
    public void testTrailingSpaces() {
        NumberInput.parseNumber("123 ");
    }

    @Test
    public void testHexPrefixOnly() {
        try {
            NumberInput.parseNumber("0x");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test
    public void testBinaryPrefixOnly() {
        try {
            NumberInput.parseNumber("0b");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test
    public void testMultipleSigns() {
        try {
            NumberInput.parseNumber("+-123");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test
    public void testLeadingZeros() {
        assertEquals(123, NumberInput.parseNumber("00123"));
    }

    @Test
    public void testNegativeHex() {
        assertEquals(-26, NumberInput.parseNumber("-0x1A"));
    }

    @Test
    public void testNegativeBinary() {
        assertEquals(-5, NumberInput.parseNumber("-0b101"));
    }
}