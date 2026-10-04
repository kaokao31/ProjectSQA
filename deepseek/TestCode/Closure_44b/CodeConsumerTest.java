package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for CodeConsumer, targeting maximum coverage and fault detection.
 * Specifically designed to expose bug #44 (negative zero handling).
 */
public class CodeConsumerTest {

    private CodeConsumer consumer;
    private StringBuilder sb;

    @Before
    public void setUp() {
        // Assuming CodeConsumer can be constructed with a StringBuilder or similar.
        // If not, we may need to use reflection or a different approach.
        // For this test, we assume a constructor that takes an Appendable.
        sb = new StringBuilder();
        consumer = new CodeConsumer(sb);
    }

    // Helper to get the current output
    private String getOutput() {
        return sb.toString();
    }

    // ==================== addNumber(double) tests ====================

    @Test
    public void testAddNumberPositiveZero() {
        consumer.addNumber(0.0);
        assertEquals("0", getOutput());
    }

    @Test
    public void testAddNumberNegativeZero() {
        consumer.addNumber(-0.0);
        // Bug #44: should produce "-0", not "0"
        assertEquals("-0", getOutput());
    }

    @Test
    public void testAddNumberNaN() {
        consumer.addNumber(Double.NaN);
        assertEquals("NaN", getOutput());
    }

    @Test
    public void testAddNumberPositiveInfinity() {
        consumer.addNumber(Double.POSITIVE_INFINITY);
        assertEquals("Infinity", getOutput());
    }

    @Test
    public void testAddNumberNegativeInfinity() {
        consumer.addNumber(Double.NEGATIVE_INFINITY);
        assertEquals("-Infinity", getOutput());
    }

    @Test
    public void testAddNumberNormalPositive() {
        consumer.addNumber(42.5);
        assertEquals("42.5", getOutput());
    }

    @Test
    public void testAddNumberNormalNegative() {
        consumer.addNumber(-3.14);
        assertEquals("-3.14", getOutput());
    }

    @Test
    public void testAddNumberLargeDouble() {
        consumer.addNumber(1e200);
        assertTrue(getOutput().contains("e") || getOutput().contains("E"));
    }

    @Test
    public void testAddNumberMinDouble() {
        consumer.addNumber(Double.MIN_VALUE);
        // Should not throw, output is some representation
        assertNotNull(getOutput());
    }

    @Test
    public void testAddNumberMaxDouble() {
        consumer.addNumber(Double.MAX_VALUE);
        assertNotNull(getOutput());
    }

    // ==================== addNumber(int) tests ====================

    @Test
    public void testAddIntZero() {
        consumer.addNumber(0);
        assertEquals("0", getOutput());
    }

    @Test
    public void testAddIntPositive() {
        consumer.addNumber(123);
        assertEquals("123", getOutput());
    }

    @Test
    public void testAddIntNegative() {
        consumer.addNumber(-456);
        assertEquals("-456", getOutput());
    }

    @Test
    public void testAddIntMaxValue() {
        consumer.addNumber(Integer.MAX_VALUE);
        assertEquals(String.valueOf(Integer.MAX_VALUE), getOutput());
    }

    @Test
    public void testAddIntMinValue() {
        consumer.addNumber(Integer.MIN_VALUE);
        assertEquals(String.valueOf(Integer.MIN_VALUE), getOutput());
    }

    // ==================== add(String) tests ====================

    @Test
    public void testAddNullString() {
        consumer.add(null);
        assertEquals("null", getOutput());
    }

    @Test
    public void testAddEmptyString() {
        consumer.add("");
        assertEquals("", getOutput());
    }

    @Test
    public void testAddNormalString() {
        consumer.add("hello");
        assertEquals("hello", getOutput());
    }

    @Test
    public void testAddStringWithSpecialChars() {
        consumer.add("a\"b\\c");
        assertEquals("a\"b\\c", getOutput());
    }

    @Test
    public void testAddMultipleStrings() {
        consumer.add("foo");
        consumer.add("bar");
        assertEquals("foobar", getOutput());
    }

    // ==================== addIdentifier(String) tests ====================

    @Test
    public void testAddIdentifierNormal() {
        consumer.addIdentifier("myVar");
        assertEquals("myVar", getOutput());
    }

    @Test
    public void testAddIdentifierReservedWord() {
        // Assuming addIdentifier might handle reserved words differently
        consumer.addIdentifier("if");
        assertEquals("if", getOutput());
    }

    @Test
    public void testAddIdentifierNull() {
        consumer.addIdentifier(null);
        assertEquals("null", getOutput());
    }

    @Test
    public void testAddIdentifierEmpty() {
        consumer.addIdentifier("");
        assertEquals("", getOutput());
    }

    // ==================== startNewLine() tests ====================

    @Test
    public void testStartNewLine() {
        consumer.add("line1");
        consumer.startNewLine();
        consumer.add("line2");
        String output = getOutput();
        assertTrue(output.contains("\n") || output.contains("\r\n"));
        // Depending on platform, but at least some newline
    }

    @Test
    public void testStartNewLineMultiple() {
        consumer.add("a");
        consumer.startNewLine();
        consumer.add("b");
        consumer.startNewLine();
        consumer.add("c");
        String output = getOutput();
        // Should have two newlines
        long count = output.chars().filter(ch -> ch == '\n').count();
        assertTrue(count >= 2);
    }

    // ==================== endStatement() tests ====================

    @Test
    public void testEndStatement() {
        consumer.add("x = 1");
        consumer.endStatement();
        String output = getOutput();
        assertTrue(output.endsWith(";"));
    }

    @Test
    public void testEndStatementAlreadySemicolon() {
        consumer.add("x = 1;");
        consumer.endStatement();
        // Should not add another semicolon
        String output = getOutput();
        assertEquals("x = 1;", output);
    }

    // ==================== beginBlock() / endBlock() tests ====================

    @Test
    public void testBeginEndBlock() {
        consumer.beginBlock();
        consumer.add("stmt");
        consumer.endBlock();
        String output = getOutput();
        assertTrue(output.contains("{") && output.contains("}"));
    }

    // ==================== Edge cases and branch coverage ====================

    @Test
    public void testAddNumberNegativeZeroMultiple() {
        // Ensure repeated calls work
        consumer.addNumber(-0.0);
        consumer.addNumber(0.0);
        assertEquals("-00", getOutput());
    }

    @Test
    public void testAddNumberVerySmallNegative() {
        consumer.addNumber(-1e-10);
        assertTrue(getOutput().contains("-"));
    }

    @Test
    public void testAddNumberVerySmallPositive() {
        consumer.addNumber(1e-10);
        assertFalse(getOutput().startsWith("-"));
    }

    @Test
    public void testAddNumberNegativeZeroAfterString() {
        consumer.add("value: ");
        consumer.addNumber(-0.0);
        assertEquals("value: -0", getOutput());
    }

    // ==================== Additional methods (if any) ====================

    // Assuming there is a method to reset or clear
    @Test
    public void testClearOutput() {
        consumer.add("temp");
        // If there is a reset method, test it
        // For now, just ensure no exception
    }

    // ==================== Null/Empty combinations ====================

    @Test
    public void testAddNullThenNumber() {
        consumer.add(null);
        consumer.addNumber(5);
        assertEquals("null5", getOutput());
    }

    @Test
    public void testAddEmptyThenIdentifier() {
        consumer.add("");
        consumer.addIdentifier("id");
        assertEquals("id", getOutput());
    }

    // ==================== Stress test for large output ====================

    @Test
    public void testLargeNumberOfAdds() {
        for (int i = 0; i < 1000; i++) {
            consumer.add("a");
        }
        assertEquals(1000, getOutput().length());
    }

    // ==================== Test for potential bug in string escaping ====================

    @Test
    public void testAddStringWithUnicode() {
        consumer.add("\u00e9");
        assertEquals("\u00e9", getOutput());
    }

    @Test
    public void testAddStringWithTab() {
        consumer.add("a\tb");
        assertEquals("a\tb", getOutput());
    }
}