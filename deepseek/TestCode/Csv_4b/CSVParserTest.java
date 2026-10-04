package csvparser;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * JUnit 4 test suite for CSVParser.
 * Designed to achieve high line and branch coverage,
 * and to trigger potential bugs in CSV parsing logic.
 */
public class CSVParserTest {

    private CSVParser parser;

    @Before
    public void setUp() {
        parser = new CSVParser();
    }

    // ===================== Basic parsing =====================

    @Test
    public void testParseEmptyLine() {
        // Edge case: empty input string
        List<String> result = parser.parseLine("");
        assertNotNull("Result should not be null", result);
        assertTrue("Empty line should produce empty list", result.isEmpty());
    }

    @Test
    public void testParseNullInput() {
        // Edge case: null input (should not throw NPE)
        List<String> result = parser.parseLine(null);
        assertNull("Null input should return null", result);
    }

    @Test
    public void testSingleFieldNoQuotes() {
        List<String> result = parser.parseLine("hello");
        assertEquals("Single field should produce one element", 
                     Arrays.asList("hello"), result);
    }

    @Test
    public void testMultipleFieldsNoQuotes() {
        List<String> result = parser.parseLine("a,b,c");
        assertEquals(Arrays.asList("a", "b", "c"), result);
    }

    @Test
    public void testFieldsWithLeadingTrailingWhitespace() {
        // Typical bug: whitespace not trimmed or considered part of field
        List<String> result = parser.parseLine(" a , b , c ");
        assertEquals("Whitespace should be kept as-is unless trimmed by parser", 
                     Arrays.asList(" a ", " b ", " c "), result);
    }

    @Test
    public void testFieldsWithEmptyMiddleField() {
        List<String> result = parser.parseLine("a,,c");
        assertEquals(Arrays.asList("a", "", "c"), result);
    }

    @Test
    public void testFieldsWithEmptyFirstField() {
        List<String> result = parser.parseLine(",b,c");
        assertEquals(Arrays.asList("", "b", "c"), result);
    }

    @Test
    public void testFieldsWithEmptyLastField() {
        List<String> result = parser.parseLine("a,b,");
        assertEquals(Arrays.asList("a", "b", ""), result);
    }

    // ===================== Quoted fields =====================

    @Test
    public void testQuotedField() {
        List<String> result = parser.parseLine("\"hello world\"");
        assertEquals(Arrays.asList("hello world"), result);
    }

    @Test
    public void testQuotedFieldWithComma() {
        List<String> result = parser.parseLine("\"a,b\",c");
        assertEquals(Arrays.asList("a,b", "c"), result);
    }

    @Test
    public void testQuotedFieldWithEscapedQuote() {
        // Standard CSV double-double-quote escaping
        List<String> result = parser.parseLine("\"a\"\"b\"");
        assertEquals(Arrays.asList("a\"b"), result);
    }

    @Test
    public void testMultipleQuotedFields() {
        List<String> result = parser.parseLine("\"x\",\"y\"");
        assertEquals(Arrays.asList("x", "y"), result);
    }

    @Test
    public void testQuotedFieldWithWhitespaceInside() {
        List<String> result = parser.parseLine("\"  spaced  \"");
        assertEquals(Arrays.asList("  spaced  "), result);
    }

    @Test
    public void testUnclosedQuoteAtEnd() {
        // Edge case: malformed input; implementation should handle gracefully
        List<String> result = parser.parseLine("a,\"unclosed");
        // Depending on implementation: might return field with missing quote or throw
        // We test that it does not crash and returns something reasonable.
        assertNotNull(result);
        assertEquals(2, result.size());
        // Common bug: the second field might be null or contain the opening quote.
        // We just assert the field is present.
        assertTrue("Second field should be present", result.get(1) != null);
    }

    @Test
    public void testQuoteInMiddleOfField() {
        // Field contains a quote that is not at beginning
        List<String> result = parser.parseLine("ab\"c");
        // Quoting rules: if a field contains a quote, it must be escaped or the whole field quoted.
        // This is invalid CSV; parser might treat as literal quote or error.
        // We test for robustness.
        assertNotNull(result);
        assertEquals(1, result.size());
        // Depending on implementation: could be "ab\"c" or "abc" etc. Just check non-null.
    }

    // ===================== Embedded newlines =====================

    @Test
    public void testFieldWithEmbeddedNewline() {
        // Valid CSV: newline inside a quoted field
        List<String> result = parser.parseLine("\"line1\nline2\",end");
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("line1\nline2", result.get(0));
        assertEquals("end", result.get(1));
    }

    // ===================== Handling of escaped characters =====================

    @Test
    public void testBackslashNotSpecial() {
        // Most CSV parsers treat backslash as literal unless specified
        List<String> result = parser.parseLine("a\\,b");
        // If backslash escapes comma, then result should be one field "a,b".
        // Otherwise two fields. Test the implementation's behavior.
        // We just check that the output is consistent and no exception.
        assertNotNull(result);
        // Expecting two fields because backslash is literal: "a\\" and "b"
        assertEquals(Arrays.asList("a\\", "b"), result);
    }

    // ===================== Multiple lines (if applicable) =====================

    // If the parser has a method to parse multiple lines, we would test it.
    // Assuming parseLine is per line.

    // ===================== Performance boundary tests =====================

    @Test
    public void testVeryLongField() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append('x');
        }
        String longField = sb.toString();
        String input = longField + "," + "y";
        List<String> result = parser.parseLine(input);
        assertEquals(2, result.size());
        assertEquals(longField, result.get(0));
        assertEquals("y", result.get(1));
    }

    // ===================== Special values =====================

    @Test
    public void testFieldWithOnlyQuotes() {
        List<String> result = parser.parseLine("\"\"");
        assertEquals("Empty quoted field should yield empty string",
                     Arrays.asList(""), result);
    }

    @Test
    public void testEscapedQuoteInsideUnquotedField() {
        // Not standard, but some parsers allow.
        List<String> result = parser.parseLine("a\"\"b");
        assertNotNull(result);
        // Should be treated as literal double quote characters.
        // Expect single field "a\"\"b".
        assertEquals(Arrays.asList("a\"\"b"), result);
    }

    // ===================== Regression tests for known bugs in Defects4J =====================

    // Bug: Some parsers incorrectly handle fields with leading zeros 
    // (if they assume numeric conversion) but CSV should treat as string.
    @Test
    public void testFieldWithLeadingZeros() {
        List<String> result = parser.parseLine("00123");
        assertEquals(Arrays.asList("00123"), result);
    }

    // Bug: Multiple consecutive commas in middle
    @Test
    public void testMultipleCommas() {
        List<String> result = parser.parseLine("a,,,b");
        assertEquals(Arrays.asList("a", "", "", "b"), result);
    }

    // Bug: Closing quote followed by non-comma character
    @Test
    public void testQuotedFieldFollowedByGarbage() {
        // Invalid: "hello"world
        List<String> result = parser.parseLine("\"hello\"world");
        // Implementation may treat as single field with quoted part and literal,
        // or as error. We just ensure no exception.
        assertNotNull(result);
        // Common result: one field "helloworld" or two fields?
        // We'll assert result size is 1 or 2, but not crash.
        assertTrue(result.size() >= 1);
    }
}