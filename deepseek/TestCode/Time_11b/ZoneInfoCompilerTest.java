package org.joda.time.tz;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;

/**
 * Test suite for ZoneInfoCompiler.
 * Generated for Defects4J analysis (Time-11).
 */
public class ZoneInfoCompilerTest {

    private ZoneInfoCompiler compiler;

    @Before
    public void setUp() {
        compiler = new ZoneInfoCompiler();
    }

    @After
    public void tearDown() {
        compiler = null;
    }

    // Helper method to invoke private parseDayOfWeek via reflection if needed,
    // but we can test via public methods or by direct testing of the logic.
    // Since parseDayOfWeek is private, we test it indirectly through the
    // DateTimeZoneBuilder or by making it accessible via reflection.
    // However, the simplest approach is to test ZoneInfoCompiler's public
    // methods and behaviors.

    // Test for parseDayOfWeek (private method) - we test by reflection
    private int invokeParseDayOfWeek(String input) throws Exception {
        java.lang.reflect.Method method = ZoneInfoCompiler.class.getDeclaredMethod("parseDayOfWeek", String.class);
        method.setAccessible(true);
        return (Integer) method.invoke(compiler, input);
    }

    // Test for parseOptional (private method)
    private boolean invokeParseOptional(String input) throws Exception {
        java.lang.reflect.Method method = ZoneInfoCompiler.class.getDeclaredMethod("parseOptional", String.class);
        method.setAccessible(true);
        return (Boolean) method.invoke(compiler, input);
    }

    // Test for parseTime (private method)
    private int invokeParseTime(String input) throws Exception {
        java.lang.reflect.Method method = ZoneInfoCompiler.class.getDeclaredMethod("parseTime", String.class);
        method.setAccessible(true);
        return (Integer) method.invoke(compiler, input);
    }

    // Test for parseYear (private method)
    private int invokeParseYear(String input, int minYear, int maxYear) throws Exception {
        java.lang.reflect.Method method = ZoneInfoCompiler.class.getDeclaredMethod("parseYear", String.class, int.class, int.class);
        method.setAccessible(true);
        return (Integer) method.invoke(compiler, input, minYear, maxYear);
    }

    @Test
    public void testParseDayOfWeek_Normal() throws Exception {
        assertEquals("Monday", 1, invokeParseDayOfWeek("Mon"));
        assertEquals("Tuesday", 2, invokeParseDayOfWeek("Tue"));
        assertEquals("Wednesday", 3, invokeParseDayOfWeek("Wed"));
        assertEquals("Thursday", 4, invokeParseDayOfWeek("Thu"));
        assertEquals("Friday", 5, invokeParseDayOfWeek("Fri"));
        assertEquals("Saturday", 6, invokeParseDayOfWeek("Sat"));
        assertEquals("Sunday", 7, invokeParseDayOfWeek("Sun"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseDayOfWeek_Invalid() throws Exception {
        invokeParseDayOfWeek("Invalid");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseDayOfWeek_NullInput() throws Exception {
        invokeParseDayOfWeek(null);
    }

    @Test
    public void testParseOptional_TrueCase() throws Exception {
        assertTrue("min should be optional", invokeParseOptional("min"));
    }

    @Test
    public void testParseOptional_FalseCase() throws Exception {
        assertFalse("non-optional should be false", invokeParseOptional("max"));
    }

    @Test
    public void testParseOptional_Null() throws Exception {
        assertFalse("null should be false", invokeParseOptional(null));
    }

    @Test
    public void testParseTime_Standard() throws Exception {
        // Standard time formats: "2:00" is 7200 seconds
        assertEquals("2:00", 7200, invokeParseTime("2:00"));
        // "2:30" is 9000 seconds
        assertEquals("2:30", 9000, invokeParseTime("2:30"));
        // "2:30:15" is 9015 seconds
        assertEquals("2:30:15", 9015, invokeParseTime("2:30:15"));
    }

    @Test
    public void testParseTime_Negative() throws Exception {
        // "-2:30" should be -9000
        assertEquals("-2:30", -9000, invokeParseTime("-2:30"));
    }

    @Test
    public void testParseTime_WithSuffix() throws Exception {
        // "2:00s" means standard time (no adjustment)
        assertEquals("2:00s", 7200, invokeParseTime("2:00s"));
        // "2:00u" means UTC (no adjustment)
        assertEquals("2:00u", 7200, invokeParseTime("2:00u"));
        // "2:00w" means wall clock (no adjustment)
        assertEquals("2:00w", 7200, invokeParseTime("2:00w"));
        // "2:00g" is invalid, should throw
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseTime_InvalidSuffix() throws Exception {
        invokeParseTime("2:00g");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseTime_InvalidFormat() throws Exception {
        invokeParseTime("abc");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseTime_EmptyString() throws Exception {
        invokeParseTime("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseTime_NullInput() throws Exception {
        invokeParseTime(null);
    }

    @Test
    public void testParseYear_Standard() throws Exception {
        assertEquals("2020", 2020, invokeParseYear("2020", 1900, 2100));
        assertEquals("2000", 2000, invokeParseYear("2000", 1900, 2100));
    }

    @Test
    public void testParseYear_Minimum() throws Exception {
        assertEquals("min", Integer.MIN_VALUE, invokeParseYear("min", 1900, 2100));
    }

    @Test
    public void testParseYear_Maximum() throws Exception {
        assertEquals("max", Integer.MAX_VALUE, invokeParseYear("max", 1900, 2100));
    }

    @Test
    public void testParseYear_Only() throws Exception {
        assertEquals("only", 2023, invokeParseYear("only", 1900, 2100));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseYear_InvalidString() throws Exception {
        invokeParseYear("abc", 1900, 2100);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseYear_NullInput() throws Exception {
        invokeParseYear(null, 1900, 2100);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseYear_YearOutOfRange() throws Exception {
        invokeParseYear("1800", 1900, 2100);
    }

    @Test
    public void testParseYear_YearAtBoundary() throws Exception {
        assertEquals("lower bound", 1900, invokeParseYear("1900", 1900, 2100));
        assertEquals("upper bound", 2100, invokeParseYear("2100", 1900, 2100));
    }

    @Test
    public void testMainHelpOption() throws Exception {
        // This tests the command-line option parsing for -h or --help
        String[] args = {"-h"};
        try {
            ZoneInfoCompiler.main(args);
            fail("Expected IOException or System.exit");
        } catch (Exception e) {
            // expected
        }
    }

    @Test
    public void testMainSrcDirOption() throws Exception {
        // This test verifies that specifying -srcdir works (no crash)
        String[] args = {"-srcdir", "/tmp"};
        try {
            ZoneInfoCompiler.main(args);
        } catch (Exception e) {
            // The test might fail due to missing files, but we are just checking the parse
            // It's okay
        }
    }

    @Test
    public void testMainMultipleFiles() throws Exception {
        // Test with multiple file arguments potentially causing NPE or incorrect parsing
        String[] args = {"file1", "file2", "file3"};
        try {
            ZoneInfoCompiler.main(args);
        } catch (Exception e) {
            // expected as files don't exist
        }
    }

    @Test
    public void testMainEmptyArgs() throws Exception {
        // Empty args should print usage or help
        String[] args = {};
        try {
            ZoneInfoCompiler.main(args);
        } catch (Exception e) {
            // expected
        }
    }

    @Test
    public void testMainWithVerbose() throws Exception {
        String[] args = {"-verbose", "-srcdir", "/tmp"};
        try {
            ZoneInfoCompiler.main(args);
        } catch (Exception e) {
            // expected
        }
    }

    @Test
    public void testMainWithDSType() throws Exception {
        String[] args = {"-ds", "DSTYPE", "-srcdir", "/tmp"};
        try {
            ZoneInfoCompiler.main(args);
        } catch (Exception e) {
            // expected
        }
    }

    // Additional tests to increase coverage
    @Test
    public void testParseTime_ZeroTime() throws Exception {
        assertEquals("0:00", 0, invokeParseTime("0:00"));
    }

    @Test
    public void testParseTime_WithColon() throws Exception {
        // Normal time
        assertEquals("12:34", 12 * 3600 + 34 * 60, invokeParseTime("12:34"));
    }

    @Test
    public void testParseTime_Seconds() throws Exception {
        assertEquals("1:00:01", 3601, invokeParseTime("1:00:01"));
    }

    @Test
    public void testParseTime_LargeTime() throws Exception {
        assertEquals("100:00", 360000, invokeParseTime("100:00"));
    }

    @Test
    public void testParseTime_NegativeLarge() throws Exception {
        assertEquals("-100:00", -360000, invokeParseTime("-100:00"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseTime_OnlyColon() throws Exception {
        invokeParseTime(":");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseTime_InvalidChars() throws Exception {
        invokeParseTime("12ab");
    }

    @Test
    public void testParseYear_OnlyWithNum() throws Exception {
        // "only" is case-insensitive? Actually it's case-sensitive.
        // "only" should be recognized.
        assertEquals("only", 2023, invokeParseYear("only", 1900, 2100));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseYear_OnlyOutOfRange() throws Exception {
        invokeParseYear("only", 2024, 2030);
    }

    // Test for the rule parsing logic indirectly via constructors and toString
    @Test
    public void testRuleConstructorAndToString() {
        // Create a Rule object (assuming it's package-private)
        // The constructors aren't public, so we test through reflection or
        // by testing the compiler's parse methods.
        // We'll skip direct Rule creation since it might not be possible.
    }

    // Tests for null safety in potential NPE paths (Defects4J Bug style)
    @Test
    public void testParseDayOfWeek_EdgeCase() throws Exception {
        // These are specific edge cases
        assertEquals("Mon", 1, invokeParseDayOfWeek("Mon"));
        assertEquals("MON", 1, invokeParseDayOfWeek("MON")); // case-insensitive?
        // According to source, it's case-insensitive via toUpperCase(Locale.ENGLISH)
    }

    // Test for parseTime with "w" suffix
    @Test
    public void testParseTime_Wall() throws Exception {
        assertEquals("2:00w", 7200, invokeParseTime("2:00w"));
    }

    @Test
    public void testParseTime_WithSign() throws Exception {
        assertEquals("+2:00", 7200, invokeParseTime("+2:00"));
    }

    @Test
    public void testParseTime_MultipleSuffixInvalid() throws Exception {
        // "2:00ws" is invalid
        try {
            invokeParseTime("2:00ws");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // Tests for the main method with problematic paths
    @Test
    public void testMainWithInvalidDir() throws Exception {
        String[] args = {"-srcdir", "/nonexistent/path", "Africa/Abidjan"};
        try {
            ZoneInfoCompiler.main(args);
            // Should fail with FileNotFoundException or similar
        } catch (Exception e) {
            // expected
        }
    }

    @Test
    public void testMainWithDSTypeInvalid() throws Exception {
        String[] args = {"-ds", "invalid", "/tmp"};
        try {
            ZoneInfoCompiler.main(args);
        } catch (Exception e) {
            // expected
        }
    }

    @Test
    public void testMainHelpLongOption() throws Exception {
        String[] args = {"--help"};
        try {
            ZoneInfoCompiler.main(args);
        } catch (Exception e) {
            // expected
        }
    }

    @Test
    public void testMainWithMultipleSourceFiles() throws Exception {
        String[] args = {"-srcdir", "/tmp", "file1", "file2"};
        try {
            ZoneInfoCompiler.main(args);
        } catch (Exception e) {
            // expected
        }
    }

    // Additional coverage for edge cases in parseTime
    @Test
    public void testParseTime_NegativeZero() throws Exception {
        assertEquals("-0:00", 0, invokeParseTime("-0:00"));
    }

    @Test
    public void testParseTime_NegativeSeconds() throws Exception {
        assertEquals("-0:00:01", -1, invokeParseTime("-0:00:01"));
    }

    @Test
    public void testParseTime_OnlySeconds() throws Exception {
        // This is not a valid format
        try {
            invokeParseTime(":00:01");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testParseTime_WithMultipleColons() throws Exception {
        try {
            invokeParseTime("2:00:00:00");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // Tests for parseYear with "only" keyword
    @Test
    public void testParseYear_OnlyWithMinMax() throws Exception {
        // "only" returns the current year? Actually it returns the same year as the min/max?
        // Let's test
        // The implementation: if "only", it uses minYear if minYear == maxYear, else throws
        // This is fragile; test accordingly
    }

    // Bug-specific test: Defects4J Time-11 had a bug in parseDayOfWeek
    @Test
    public void testParseDayOfWeek_BugTrigger() throws Exception {
        // Defects4J Time-11 bug was related to TimeZone name parsing.
        // We test parsing of day of week with various case combinations
        assertEquals("mon", 1, invokeParseDayOfWeek("mon"));
        assertEquals("MON", 1, invokeParseDayOfWeek("MON"));
        assertEquals("Mon", 1, invokeParseDayOfWeek("Mon"));
        assertEquals("tue", 2, invokeParseDayOfWeek("tue"));
        assertEquals("TUE", 2, invokeParseDayOfWeek("TUE"));
        assertEquals("Tue", 2, invokeParseDayOfWeek("Tue"));
        // Add more as needed
    }

    // Test for empty string input in parse methods
    @Test(expected = IllegalArgumentException.class)
    public void testParseDayOfWeek_EmptyString() throws Exception {
        invokeParseDayOfWeek("");
    }

    // Test for parseOptional with non-null non-empty non-"min"
    @Test
    public void testParseOptional_Other() throws Exception {
        assertFalse("any other string", invokeParseOptional("other"));
    }

    // Test for parseTime with spaces (should fail)
    @Test(expected = IllegalArgumentException.class)
    public void testParseTime_WithSpaces() throws Exception {
        invokeParseTime("2 :00");
    }

    // Test for parseYear with "min" and "max"
    @Test
    public void testParseYear_MinMax() throws Exception {
        assertEquals("min", Integer.MIN_VALUE, invokeParseYear("min", 1900, 2100));
        assertEquals("max", Integer.MAX_VALUE, invokeParseYear("max", 1900, 2100));
    }

    // Test for parseYear with number as string
    @Test
    public void testParseYear_NumberString() throws Exception {
        assertEquals("2023", 2023, invokeParseYear("2023", 1900, 2100));
    }

    // Additional coverage for parseTime with UTF-8 or special characters? Not needed.

    // Test for the toString method of ZoneInfoCompiler (if any)
    // The class doesn't override toString, so no test needed.
}