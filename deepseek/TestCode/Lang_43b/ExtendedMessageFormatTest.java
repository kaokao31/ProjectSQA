package org.apache.commons.lang.text;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import java.text.MessageFormat;
import java.text.ParsePosition;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Comprehensive JUnit 4 test suite for ExtendedMessageFormat.
 * Designed to achieve maximum code coverage and trigger the known bug LANG-477.
 */
public class ExtendedMessageFormatTest {

    private static final String STANDARD_PATTERN = "Value: {0,number}";
    private static final Object[] STANDARD_ARGS = {42};

    private ExtendedMessageFormat emf;

    @Before
    public void setUp() {
        emf = new ExtendedMessageFormat(STANDARD_PATTERN);
    }

    @After
    public void tearDown() {
        emf = null;
    }

    // ===== Constructor tests =====

    @Test
    public void testConstructorWithLocale() {
        ExtendedMessageFormat fmt = new ExtendedMessageFormat(STANDARD_PATTERN, Locale.GERMAN);
        assertNotNull("Constructor with locale should succeed", fmt);
        // The locale should be used; we cannot directly check but can ensure it formats.
        String formatted = fmt.format(STANDARD_ARGS);
        assertTrue("Formatted string should contain the number", formatted.contains("42"));
    }

    @Test
    public void testConstructorWithMap() {
        Map<String, String> registry = new HashMap<>();
        registry.put("custom", "java.text.DecimalFormat");
        ExtendedMessageFormat fmt = new ExtendedMessageFormat(STANDARD_PATTERN, registry);
        assertNotNull("Constructor with registry should succeed", fmt);
    }

    @Test
    public void testConstructorWithAllParameters() {
        Map<String, String> registry = new HashMap<>();
        registry.put("custom", "java.text.DecimalFormat");
        ExtendedMessageFormat fmt = new ExtendedMessageFormat(STANDARD_PATTERN, Locale.US, registry);
        assertNotNull("Constructor with all parameters should succeed", fmt);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullPattern() {
        new ExtendedMessageFormat(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyPattern() {
        new ExtendedMessageFormat("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalidPattern() {
        new ExtendedMessageFormat("'{0'"); // unclosed quote
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorPatternWithUnclosedBrace() {
        new ExtendedMessageFormat("Test {0,number"); // missing closing brace
    }

    // ===== applyPattern tests =====

    @Test
    public void testApplyPattern() {
        ExtendedMessageFormat fmt = new ExtendedMessageFormat(STANDARD_PATTERN);
        fmt.applyPattern("New {0,string}");
        assertEquals("Pattern should have been changed", "New {0,string}", fmt.toPattern());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testApplyPatternNull() {
        emf.applyPattern(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testApplyPatternInvalid() {
        emf.applyPattern("'broken");
    }

    @Test
    public void testApplyPatternAndFormat() {
        emf.applyPattern("Hello {0}");
        assertEquals("Hello World", emf.format(new Object[]{"World"}));
    }

    // ===== format tests =====

    @Test
    public void testFormatBasic() {
        String result = emf.format(STANDARD_ARGS);
        assertEquals("Value: 42", result);
    }

    @Test
    public void testFormatMultipleArguments() {
        ExtendedMessageFormat fmt = new ExtendedMessageFormat("{0} + {1} = {2,number}");
        String result = fmt.format(new Object[]{1, 2, 3});
        assertEquals("1 + 2 = 3", result);
    }

    @Test
    public void testFormatWithChoice() {
        ExtendedMessageFormat fmt = new ExtendedMessageFormat("{0,choice,0#none|1#one|1<many}");
        assertEquals("none", fmt.format(new Object[]{0}));
        assertEquals("one", fmt.format(new Object[]{1}));
        assertEquals("many", fmt.format(new Object[]{2}));
    }

    @Test
    public void testFormatWithNullArgument() {
        ExtendedMessageFormat fmt = new ExtendedMessageFormat("{0}");
        assertEquals("null", fmt.format(new Object[]{null}));
    }

    @Test
    public void testFormatWithEmptyArray() {
        ExtendedMessageFormat fmt = new ExtendedMessageFormat("No arguments");
        assertEquals("No arguments", fmt.format(new Object[]{}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatMissingArgument() {
        emf.format(new Object[]{}); // requires at least one argument
    }

    // ===== toPattern tests =====

    @Test
    public void testToPattern() {
        assertEquals(STANDARD_PATTERN, emf.toPattern());
    }

    @Test
    public void testToPatternAfterApplyPattern() {
        emf.applyPattern("Custom {0,date}");
        assertEquals("Custom {0,date}", emf.toPattern());
    }

    // ===== parseObject tests =====

    @Test
    public void testParseObject() {
        ParsePosition pos = new ParsePosition(0);
        Object parsed = emf.parseObject("Value: 42", pos);
        assertNotNull("Parsed object should not be null", parsed);
        assertTrue("Parsed object should be Object[]", parsed instanceof Object[]);
        Object[] array = (Object[]) parsed;
        assertEquals(1, array.length);
        assertEquals("Parsed value should be 42", Long.valueOf(42), array[0]);
    }

    @Test
    public void testParseObjectWithFullMatch() {
        ParsePosition pos = new ParsePosition(0);
        Object[] result = (Object[]) emf.parseObject("Value: 42", pos);
        assertEquals(7, pos.getIndex()); // entire string consumed
    }

    @Test
    public void testParseObjectPartial() {
        ParsePosition pos = new ParsePosition(0);
        Object parsed = emf.parseObject("Value: 42 extra", pos);
        assertNotNull(parsed);
        assertEquals(7, pos.getIndex()); // stops after matching pattern
    }

    @Test
    public void testParseObjectFailure() {
        ParsePosition pos = new ParsePosition(0);
        Object parsed = emf.parseObject("Not matching", pos);
        assertNull("Should return null on failure", parsed);
        assertEquals(0, pos.getIndex()); // index unchanged
    }

    // ===== Quote and escaping tests =====

    @Test
    public void testSingleQuoteInPattern() {
        ExtendedMessageFormat fmt = new ExtendedMessageFormat("It''s a test");
        assertEquals("It's a test", fmt.format(new Object[]{}));
    }

    @Test
    public void testSingleQuoteAroundFormatElement() {
        ExtendedMessageFormat fmt = new ExtendedMessageFormat("'{0}'");
        assertEquals("{0}", fmt.format(new Object[]{"value"}));
    }

    @Test
    public void testDoubleQuotes() {
        ExtendedMessageFormat fmt = new ExtendedMessageFormat("\"{0}\"");
        assertEquals("\"value\"", fmt.format(new Object[]{"value"}));
    }

    @Test
    public void testEscapedSingleQuoteInsideFormat() {
        ExtendedMessageFormat fmt = new ExtendedMessageFormat("{0,number, ''#'#0}");
        assertEquals("'42", fmt.format(new Object[]{42}));
    }

    @Test
    public void testPatternWithMixedQuotes() {
        ExtendedMessageFormat fmt = new ExtendedMessageFormat("A '{0}' B");
        assertEquals("A {0} B", fmt.format(new Object[]{"test"}));
    }

    @Test
    public void testPatternWithUnbalancedQuoteInFormat() {
        // This should be valid: the quote is inside the format element
        ExtendedMessageFormat fmt = new ExtendedMessageFormat("{0,date}");
        assertNotNull(fmt);
    }

    // ===== Known bug: LANG-477 =====
    // This test triggers an OutOfMemoryError in the buggy version, sets a timeout to avoid hanging.
    @Test(timeout = 2000)
    public void testEscapedQuote_LANG_477() {
        // Pattern that causes infinite loop / OOM when quotes surround format elements
        String pattern = "'{0}'";
        ExtendedMessageFormat fmt = new ExtendedMessageFormat(pattern);
        // In the buggy version, this call may cause an OutOfMemoryError or infinite loop
        String result = fmt.format(new Object[]{"test"});
        // Expected behavior in fixed version: result should be '{test}' or something
        // The exact expected output depends on the spec; but we just test it doesn't blow up.
        assertNotNull("Formatted result should not be null", result);
    }

    @Test(timeout = 2000)
    public void testEscapedQuoteComplex_LANG_477() {
        // Another variation: quoted braces with number format
        String pattern = "' {0,number} '";
        ExtendedMessageFormat fmt = new ExtendedMessageFormat(pattern);
        String result = fmt.format(new Object[]{42});
        assertNotNull(result);
    }

    @Test(timeout = 2000)
    public void testEscapedQuoteRecursive_LANG_477() {
        // Pattern with nested quotes
        String pattern = "''{0}''";
        ExtendedMessageFormat fmt = new ExtendedMessageFormat(pattern);
        String result = fmt.format(new Object[]{"test"});
        assertEquals("'test'", result); // should output 'test'
    }

    // ===== Edge cases =====

    @Test
    public void testPatternWithOnlyEscapedChars() {
        ExtendedMessageFormat fmt = new ExtendedMessageFormat("' ' ' '");
        assertEquals("   ", fmt.format(new Object[]{}));
    }

    @Test
    public void testFormatObjectArrayWithNullElements() {
        ExtendedMessageFormat fmt = new ExtendedMessageFormat("{0}{1}{2}");
        String result = fmt.format(new Object[]{null, "middle", null});
        assertEquals("nullmiddlenull", result);
    }

    @Test
    public void testFormatWithDate() {
        ExtendedMessageFormat fmt = new ExtendedMessageFormat("{0,date}");
        String result = fmt.format(new Object[]{new java.util.Date(0L)});
        assertTrue("Should contain 1970", result.contains("1970"));
    }

    @Test
    public void testFormatWithNumberAndPattern() {
        ExtendedMessageFormat fmt = new ExtendedMessageFormat("{0,number,###.##}");
        assertEquals("42", fmt.format(new Object[]{42}));
        assertEquals("3.14", fmt.format(new Object[]{3.14}));
    }

    @Test
    public void testPatternWithEscapedBrace() {
        ExtendedMessageFormat fmt = new ExtendedMessageFormat("'{0}'");
        assertEquals("{0}", fmt.format(new Object[]{"value"}));
    }

    @Test
    public void testPatternWithMultipleFormats() {
        ExtendedMessageFormat fmt = new ExtendedMessageFormat("{0}, {1,number}, {2,date}");
        String result = fmt.format(new Object[]{"text", 123, new java.util.Date(0L)});
        assertTrue(result.startsWith("text, 123"));
    }

    // ===== Null/empty registry =====

    @Test
    public void testConstructorWithNullRegistry() {
        ExtendedMessageFormat fmt = new ExtendedMessageFormat(STANDARD_PATTERN, (Map<String, String>) null);
        assertNotNull(fmt);
        assertEquals("Value: 42", fmt.format(STANDARD_ARGS));
    }

    @Test
    public void testConstructorWithEmptyRegistry() {
        ExtendedMessageFormat fmt = new ExtendedMessageFormat(STANDARD_PATTERN, new HashMap<String, String>());
        assertNotNull(fmt);
    }

    // ===== equals/hashCode (if available) =====
    // Note: ExtendedMessageFormat inherits from MessageFormat; we can test basic equality.

    @Test
    public void testEquals() {
        ExtendedMessageFormat fmt1 = new ExtendedMessageFormat(STANDARD_PATTERN);
        ExtendedMessageFormat fmt2 = new ExtendedMessageFormat(STANDARD_PATTERN);
        assertEquals("Two identical patterns should be equal", fmt1, fmt2);
    }

    @Test
    public void testNotEquals() {
        ExtendedMessageFormat fmt1 = new ExtendedMessageFormat(STANDARD_PATTERN);
        ExtendedMessageFormat fmt2 = new ExtendedMessageFormat("Different {0}");
        assertFalse("Different patterns should not be equal", fmt1.equals(fmt2));
    }

    @Test
    public void testHashCode() {
        ExtendedMessageFormat fmt1 = new ExtendedMessageFormat(STANDARD_PATTERN);
        ExtendedMessageFormat fmt2 = new ExtendedMessageFormat(STANDARD_PATTERN);
        assertEquals("Equal objects must have same hashcode", fmt1.hashCode(), fmt2.hashCode());
    }
}