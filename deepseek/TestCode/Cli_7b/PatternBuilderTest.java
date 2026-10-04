package org.apache.commons.cli2.builder;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for PatternBuilder.
 * Designed to achieve maximum code coverage and detect the known Defects4J Cli-7 bug.
 */
public class PatternBuilderTest {

    private PatternBuilder builder;

    @Before
    public void setUp() {
        builder = new PatternBuilder();
    }

    // ---------- Null and empty pattern tests ----------

    @Test(expected = NullPointerException.class)
    public void testWithPatternNull() {
        builder.withPattern(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithPatternEmpty() {
        builder.withPattern("");
    }

    // ---------- Single option without argument ----------

    @Test
    public void testSingleOptionNoArg() {
        builder.withPattern("a");
        Options options = builder.create();
        assertNotNull("Options should not be null", options);
        assertTrue("Option 'a' should exist", options.hasOption("a"));
        Option opt = options.getOption("a");
        assertFalse("Option 'a' should not require an argument", opt.isRequired());
        assertNull("Option 'a' should have no argument name", opt.getArgumentName());
    }

    // ---------- Option with required argument (colon) ----------

    @Test
    public void testOptionWithRequiredArg() {
        builder.withPattern("a:b");
        Options options = builder.create();
        Option opt = options.getOption("a");
        assertNotNull("Option 'a' should exist", opt);
        assertTrue("Option 'a' should require an argument", opt.isRequired());
        assertEquals("Argument name should be 'b'", "b", opt.getArgumentName());
    }

    // ---------- Option with optional argument (question mark) ----------

    @Test
    public void testOptionWithOptionalArg() {
        builder.withPattern("a?b");
        Options options = builder.create();
        Option opt = options.getOption("a");
        assertNotNull("Option 'a' should exist", opt);
        assertFalse("Option 'a' should not require an argument", opt.isRequired());
        assertEquals("Argument name should be 'b'", "b", opt.getArgumentName());
    }

    // ---------- Multiple options ----------

    @Test
    public void testMultipleOptions() {
        builder.withPattern("ab");
        Options options = builder.create();
        assertTrue("Option 'a' should exist", options.hasOption("a"));
        assertTrue("Option 'b' should exist", options.hasOption("b"));
    }

    @Test
    public void testMultipleOptionsWithArgs() {
        builder.withPattern("a:b?c");
        Options options = builder.create();
        Option optA = options.getOption("a");
        assertTrue("Option 'a' should require an argument", optA.isRequired());
        assertEquals("Argument name for 'a' should be 'b'", "b", optA.getArgumentName());

        Option optC = options.getOption("c");
        assertFalse("Option 'c' should not require an argument", optC.isRequired());
        assertEquals("Argument name for 'c' should be 'c'", "c", optC.getArgumentName());
    }

    // ---------- Pattern with spaces (should be trimmed or ignored) ----------

    @Test
    public void testPatternWithSpaces() {
        builder.withPattern("a b");
        Options options = builder.create();
        assertTrue("Option 'a' should exist", options.hasOption("a"));
        assertTrue("Option 'b' should exist", options.hasOption("b"));
    }

    // ---------- Pattern with special characters ----------

    @Test
    public void testPatternWithSpecialChars() {
        builder.withPattern("a:b?c d:e");
        Options options = builder.create();
        Option optA = options.getOption("a");
        assertEquals("b", optA.getArgumentName());
        Option optC = options.getOption("c");
        assertEquals("c", optC.getArgumentName());
        Option optD = options.getOption("d");
        assertEquals("e", optD.getArgumentName());
    }

    // ---------- Repeated calls to withPattern (chaining) ----------

    @Test
    public void testChainedWithPattern() {
        builder.withPattern("a").withPattern("b");
        Options options = builder.create();
        assertTrue("Option 'a' should exist", options.hasOption("a"));
        assertTrue("Option 'b' should exist", options.hasOption("b"));
    }

    // ---------- Edge case: argument name with colon in pattern ----------

    @Test
    public void testArgNameWithColon() {
        // Pattern "a:b" should set argument name to "b", not "b" with extra colon
        builder.withPattern("a:b");
        Options options = builder.create();
        Option opt = options.getOption("a");
        assertEquals("Argument name should be exactly 'b'", "b", opt.getArgumentName());
    }

    // ---------- Edge case: argument name with question mark ----------

    @Test
    public void testArgNameWithQuestionMark() {
        builder.withPattern("a?b");
        Options options = builder.create();
        Option opt = options.getOption("a");
        assertEquals("Argument name should be exactly 'b'", "b", opt.getArgumentName());
    }

    // ---------- Bug detection: Defects4J Cli-7 ----------
    // The bug was that when a pattern contains a required argument (colon),
    // the argument name might be incorrectly set or the option might be
    // treated as optional. This test verifies correct behavior.

    @Test
    public void testBugCli7RequiredArg() {
        builder.withPattern("x:y");
        Options options = builder.create();
        Option opt = options.getOption("x");
        assertNotNull("Option 'x' must exist", opt);
        assertTrue("Option 'x' must be required (colon)", opt.isRequired());
        assertEquals("Argument name must be 'y'", "y", opt.getArgumentName());
    }

    @Test
    public void testBugCli7OptionalArg() {
        builder.withPattern("x?y");
        Options options = builder.create();
        Option opt = options.getOption("x");
        assertNotNull("Option 'x' must exist", opt);
        assertFalse("Option 'x' must not be required (question mark)", opt.isRequired());
        assertEquals("Argument name must be 'y'", "y", opt.getArgumentName());
    }

    // ---------- Mixed pattern with both required and optional ----------

    @Test
    public void testMixedRequiredOptional() {
        builder.withPattern("a:b?c");
        Options options = builder.create();
        Option optA = options.getOption("a");
        assertTrue(optA.isRequired());
        assertEquals("b", optA.getArgumentName());
        Option optC = options.getOption("c");
        assertFalse(optC.isRequired());
        assertEquals("c", optC.getArgumentName());
    }

    // ---------- Pattern with multiple characters per option (should be ignored?) ----------
    // According to CLI spec, each character is a separate option.

    @Test
    public void testMultipleCharsPerOption() {
        builder.withPattern("ab");
        Options options = builder.create();
        assertTrue("Option 'a' should exist", options.hasOption("a"));
        assertTrue("Option 'b' should exist", options.hasOption("b"));
        assertFalse("Option 'ab' should not exist as a single option", options.hasOption("ab"));
    }

    // ---------- Pattern with trailing colon ----------

    @Test(expected = IllegalArgumentException.class)
    public void testPatternWithTrailingColon() {
        builder.withPattern("a:");
    }

    // ---------- Pattern with trailing question mark ----------

    @Test(expected = IllegalArgumentException.class)
    public void testPatternWithTrailingQuestionMark() {
        builder.withPattern("a?");
    }

    // ---------- Pattern with only colon ----------

    @Test(expected = IllegalArgumentException.class)
    public void testPatternWithOnlyColon() {
        builder.withPattern(":");
    }

    // ---------- Pattern with only question mark ----------

    @Test(expected = IllegalArgumentException.class)
    public void testPatternWithOnlyQuestionMark() {
        builder.withPattern("?");
    }

    // ---------- Create without calling withPattern ----------

    @Test
    public void testCreateWithoutPattern() {
        Options options = builder.create();
        assertNotNull("Options should not be null", options);
        assertTrue("Options should be empty", options.getOptions().isEmpty());
    }

    // ---------- Multiple calls to create ----------

    @Test
    public void testMultipleCreateCalls() {
        builder.withPattern("a");
        Options first = builder.create();
        assertTrue(first.hasOption("a"));
        // Second call should return a new Options object with the same pattern
        Options second = builder.create();
        assertTrue(second.hasOption("a"));
        assertNotSame("Should be different objects", first, second);
    }

    // ---------- Pattern with uppercase letters ----------

    @Test
    public void testUppercaseOption() {
        builder.withPattern("A");
        Options options = builder.create();
        assertTrue("Option 'A' should exist", options.hasOption("A"));
    }

    // ---------- Pattern with digits ----------

    @Test
    public void testDigitOption() {
        builder.withPattern("1");
        Options options = builder.create();
        assertTrue("Option '1' should exist", options.hasOption("1"));
    }

    // ---------- Pattern with mixed case and digits ----------

    @Test
    public void testMixedCaseDigits() {
        builder.withPattern("aB1");
        Options options = builder.create();
        assertTrue("Option 'a' should exist", options.hasOption("a"));
        assertTrue("Option 'B' should exist", options.hasOption("B"));
        assertTrue("Option '1' should exist", options.hasOption("1"));
    }
}