package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test suite for PosixParser, targeting Cli-17 bug and high coverage.
 */
public class PosixParserTest {

    private Options options;
    private PosixParser parser;

    @Before
    public void setUp() {
        options = new Options();
        parser = new PosixParser();
    }

    // ---------- Basic option parsing ----------

    @Test
    public void testSimpleShortOption() throws ParseException {
        options.addOption("a", false, "alpha");
        options.addOption("b", false, "beta");

        CommandLine cl = parser.parse(options, new String[]{"-a", "-b"});
        assertTrue("Option 'a' should be set", cl.hasOption("a"));
        assertTrue("Option 'b' should be set", cl.hasOption("b"));
        assertEquals("Arg list should be empty", 0, cl.getArgs().length);
    }

    @Test
    public void testSimpleLongOption() throws ParseException {
        options.addOption("a", "alpha", false, "alpha option");
        options.addOption("b", "beta", false, "beta option");

        CommandLine cl = parser.parse(options, new String[]{"--alpha", "--beta"});
        assertTrue("Option 'a' should be set", cl.hasOption("a"));
        assertTrue("Option 'b' should be set", cl.hasOption("b"));
    }

    @Test
    public void testShortOptionWithArgument() throws ParseException {
        options.addOption("o", true, "output file");

        CommandLine cl = parser.parse(options, new String[]{"-o", "file.txt"});
        assertTrue("Option 'o' should be set", cl.hasOption("o"));
        assertEquals("Argument should be 'file.txt'", "file.txt", cl.getOptionValue("o"));
    }

    @Test
    public void testLongOptionWithArgument() throws ParseException {
        options.addOption("o", "output", true, "output file");

        CommandLine cl = parser.parse(options, new String[]{"--output", "file.txt"});
        assertTrue("Option 'o' should be set", cl.hasOption("o"));
        assertEquals("Argument should be 'file.txt'", "file.txt", cl.getOptionValue("o"));
    }

    // ---------- Combined short options ----------

    @Test
    public void testCombinedShortOptions() throws ParseException {
        options.addOption("a", false, "alpha");
        options.addOption("b", false, "beta");
        options.addOption("c", false, "gamma");

        CommandLine cl = parser.parse(options, new String[]{"-abc"});
        assertTrue("Option 'a' should be set", cl.hasOption("a"));
        assertTrue("Option 'b' should be set", cl.hasOption("b"));
        assertTrue("Option 'c' should be set", cl.hasOption("c"));
    }

    @Test
    public void testCombinedShortOptionsWithArgument() throws ParseException {
        options.addOption("a", false, "alpha");
        options.addOption("b", true, "beta with arg");
        options.addOption("c", false, "gamma");

        CommandLine cl = parser.parse(options, new String[]{"-ab", "argForB", "-c"});
        assertTrue("Option 'a' should be set", cl.hasOption("a"));
        assertTrue("Option 'b' should be set", cl.hasOption("b"));
        assertEquals("Argument for b should be 'argForB'", "argForB", cl.getOptionValue("b"));
        assertTrue("Option 'c' should be set", cl.hasOption("c"));
    }

    // ---------- Option with argument attached (e.g., -Dfoo=bar) ----------

    @Test
    public void testShortOptionWithAttachedArgument() throws ParseException {
        options.addOption("D", true, "define property");

        CommandLine cl = parser.parse(options, new String[]{"-Dfoo=bar"});
        assertTrue("Option 'D' should be set", cl.hasOption("D"));
        assertEquals("Argument should be 'foo=bar'", "foo=bar", cl.getOptionValue("D"));
    }

    @Test
    public void testLongOptionWithAttachedArgument() throws ParseException {
        options.addOption("D", "define", true, "define property");

        CommandLine cl = parser.parse(options, new String[]{"--define=foo=bar"});
        assertTrue("Option 'D' should be set", cl.hasOption("D"));
        assertEquals("Argument should be 'foo=bar'", "foo=bar", cl.getOptionValue("D"));
    }

    // ---------- Negative numbers as arguments ----------

    @Test
    public void testNegativeNumberAsArgument() throws ParseException {
        options.addOption("n", true, "number");

        CommandLine cl = parser.parse(options, new String[]{"-n", "-5"});
        assertTrue("Option 'n' should be set", cl.hasOption("n"));
        assertEquals("Argument should be '-5'", "-5", cl.getOptionValue("n"));
    }

    @Test
    public void testNegativeNumberAsSeparateToken() throws ParseException {
        options.addOption("n", true, "number");

        CommandLine cl = parser.parse(options, new String[]{"-n", "-5"});
        assertTrue("Option 'n' should be set", cl.hasOption("n"));
        assertEquals("Argument should be '-5'", "-5", cl.getOptionValue("n"));
    }

    // ---------- Stop at non-option (--) ----------

    @Test
    public void testStopAtDoubleDash() throws ParseException {
        options.addOption("a", false, "alpha");

        CommandLine cl = parser.parse(options, new String[]{"-a", "--", "-b", "foo"});
        assertTrue("Option 'a' should be set", cl.hasOption("a"));
        assertEquals("Remaining args should be 2", 2, cl.getArgs().length);
        assertEquals("First remaining arg should be '-b'", "-b", cl.getArgs()[0]);
        assertEquals("Second remaining arg should be 'foo'", "foo", cl.getArgs()[1]);
    }

    // ---------- Unknown option ----------

    @Test(expected = UnrecognizedOptionException.class)
    public void testUnknownShortOption() throws ParseException {
        options.addOption("a", false, "alpha");
        parser.parse(options, new String[]{"-x"});
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testUnknownLongOption() throws ParseException {
        options.addOption("a", "alpha", false, "alpha");
        parser.parse(options, new String[]{"--unknown"});
    }

    // ---------- Missing argument for option that requires argument ----------

    @Test(expected = MissingArgumentException.class)
    public void testMissingArgumentForShortOption() throws ParseException {
        options.addOption("o", true, "output");
        parser.parse(options, new String[]{"-o"});
    }

    @Test(expected = MissingArgumentException.class)
    public void testMissingArgumentForLongOption() throws ParseException {
        options.addOption("o", "output", true, "output");
        parser.parse(options, new String[]{"--output"});
    }

    // ---------- Option with optional argument ----------

    @Test
    public void testOptionalArgumentNotProvided() throws ParseException {
        Option opt = OptionBuilder.withLongOpt("opt").hasOptionalArg().create('o');
        options.addOption(opt);

        CommandLine cl = parser.parse(options, new String[]{"-o"});
        assertTrue("Option 'o' should be set", cl.hasOption("o"));
        assertNull("Argument should be null", cl.getOptionValue("o"));
    }

    @Test
    public void testOptionalArgumentProvided() throws ParseException {
        Option opt = OptionBuilder.withLongOpt("opt").hasOptionalArg().create('o');
        options.addOption(opt);

        CommandLine cl = parser.parse(options, new String[]{"-o", "value"});
        assertTrue("Option 'o' should be set", cl.hasOption("o"));
        assertEquals("Argument should be 'value'", "value", cl.getOptionValue("o"));
    }

    // ---------- Multiple arguments ----------

    @Test
    public void testMultipleArguments() throws ParseException {
        options.addOption("a", false, "alpha");
        options.addOption("b", true, "beta");

        CommandLine cl = parser.parse(options, new String[]{"-a", "-b", "arg", "extra1", "extra2"});
        assertTrue("Option 'a' should be set", cl.hasOption("a"));
        assertTrue("Option 'b' should be set", cl.hasOption("b"));
        assertEquals("Argument for b should be 'arg'", "arg", cl.getOptionValue("b"));
        assertEquals("Remaining args should be 2", 2, cl.getArgs().length);
        assertEquals("First extra arg should be 'extra1'", "extra1", cl.getArgs()[0]);
        assertEquals("Second extra arg should be 'extra2'", "extra2", cl.getArgs()[1]);
    }

    // ---------- Flatten method tests ----------

    @Test
    public void testFlattenSimple() {
        options.addOption("a", false, "alpha");
        options.addOption("b", false, "beta");

        String[] result = parser.flatten(options, new String[]{"-a", "-b"}, false);
        assertArrayEquals("Flatten should return same tokens", new String[]{"-a", "-b"}, result);
    }

    @Test
    public void testFlattenWithArgument() {
        options.addOption("o", true, "output");

        String[] result = parser.flatten(options, new String[]{"-o", "file.txt"}, false);
        assertArrayEquals("Flatten should return tokens as is", new String[]{"-o", "file.txt"}, result);
    }

    @Test
    public void testFlattenCombinedShortOptions() {
        options.addOption("a", false, "alpha");
        options.addOption("b", false, "beta");

        String[] result = parser.flatten(options, new String[]{"-ab"}, false);
        assertArrayEquals("Flatten should expand combined options", new String[]{"-a", "-b"}, result);
    }

    @Test
    public void testFlattenCombinedWithArgument() {
        options.addOption("a", false, "alpha");
        options.addOption("b", true, "beta");

        String[] result = parser.flatten(options, new String[]{"-ab", "arg"}, false);
        assertArrayEquals("Flatten should expand and keep argument", new String[]{"-a", "-b", "arg"}, result);
    }

    @Test
    public void testFlattenAttachedArgument() {
        options.addOption("D", true, "define");

        String[] result = parser.flatten(options, new String[]{"-Dfoo=bar"}, false);
        assertArrayEquals("Flatten should keep attached argument", new String[]{"-Dfoo=bar"}, result);
    }

    @Test
    public void testFlattenStopAtNonOption() {
        options.addOption("a", false, "alpha");

        String[] result = parser.flatten(options, new String[]{"-a", "--", "-b"}, false);
        assertArrayEquals("Flatten should stop at '--'", new String[]{"-a", "--", "-b"}, result);
    }

    @Test
    public void testFlattenWithStopAtNonOptionTrue() {
        options.addOption("a", false, "alpha");

        String[] result = parser.flatten(options, new String[]{"-a", "foo", "-b"}, true);
        assertArrayEquals("Flatten should stop at first non-option when stopAtNonOption is true",
                new String[]{"-a", "foo", "-b"}, result);
    }

    @Test
    public void testFlattenWithStopAtNonOptionFalse() {
        options.addOption("a", false, "alpha");
        options.addOption("b", false, "beta");

        String[] result = parser.flatten(options, new String[]{"-a", "foo", "-b"}, false);
        assertArrayEquals("Flatten should not stop at non-option when stopAtNonOption is false",
                new String[]{"-a", "foo", "-b"}, result);
    }

    // ---------- Edge cases ----------

    @Test
    public void testEmptyArgs() throws ParseException {
        CommandLine cl = parser.parse(options, new String[]{});
        assertEquals("No args should be present", 0, cl.getArgs().length);
    }

    @Test
    public void testNullArgs() throws ParseException {
        // parse with null should be treated as empty array
        CommandLine cl = parser.parse(options, null);
        assertEquals("No args should be present", 0, cl.getArgs().length);
    }

    @Test
    public void testOptionWithLongPrefixOnly() throws ParseException {
        options.addOption("a", "alpha", false, "alpha");

        CommandLine cl = parser.parse(options, new String[]{"--alpha"});
        assertTrue("Option 'a' should be set", cl.hasOption("a"));
    }

    @Test
    public void testOptionWithShortPrefixOnly() throws ParseException {
        options.addOption("a", false, "alpha");

        CommandLine cl = parser.parse(options, new String[]{"-a"});
        assertTrue("Option 'a' should be set", cl.hasOption("a"));
    }

    // ---------- Bug-specific tests for Cli-17 ----------

    @Test
    public void testOptionWithAttachedArgumentAndEqualsSign() throws ParseException {
        // This test targets the Cli-17 bug: argument attached with '=' should be parsed correctly
        options.addOption("D", true, "define property");

        CommandLine cl = parser.parse(options, new String[]{"-Dproperty=value"});
        assertTrue("Option 'D' should be set", cl.hasOption("D"));
        assertEquals("Argument should be 'property=value'", "property=value", cl.getOptionValue("D"));
    }

    @Test
    public void testLongOptionWithAttachedArgumentAndEqualsSign() throws ParseException {
        options.addOption("D", "define", true, "define property");

        CommandLine cl = parser.parse(options, new String[]{"--define=property=value"});
        assertTrue("Option 'D' should be set", cl.hasOption("D"));
        assertEquals("Argument should be 'property=value'", "property=value", cl.getOptionValue("D"));
    }

    @Test
    public void testMultipleAttachedArguments() throws ParseException {
        options.addOption("D", true, "define");
        options.addOption("P", true, "path");

        CommandLine cl = parser.parse(options, new String[]{"-Dkey=val", "-P/usr/local"});
        assertTrue("Option 'D' should be set", cl.hasOption("D"));
        assertEquals("Argument for D should be 'key=val'", "key=val", cl.getOptionValue("D"));
        assertTrue("Option 'P' should be set", cl.hasOption("P"));
        assertEquals("Argument for P should be '/usr/local'", "/usr/local", cl.getOptionValue("P"));
    }

    @Test
    public void testOptionWithAttachedArgumentAndNoEquals() throws ParseException {
        options.addOption("D", true, "define");

        CommandLine cl = parser.parse(options, new String[]{"-Dvalue"});
        assertTrue("Option 'D' should be set", cl.hasOption("D"));
        assertEquals("Argument should be 'value'", "value", cl.getOptionValue("D"));
    }

    @Test
    public void testLongOptionWithAttachedArgumentAndNoEquals() throws ParseException {
        options.addOption("D", "define", true, "define");

        CommandLine cl = parser.parse(options, new String[]{"--definevalue"});
        assertTrue("Option 'D' should be set", cl.hasOption("D"));
        assertEquals("Argument should be 'value'", "value", cl.getOptionValue("D"));
    }

    // ---------- Additional edge cases for coverage ----------

    @Test
    public void testOptionWithArgumentAndExtraDashes() throws ParseException {
        options.addOption("o", true, "output");

        CommandLine cl = parser.parse(options, new String[]{"-o", "--", "file"});
        assertTrue("Option 'o' should be set", cl.hasOption("o"));
        assertEquals("Argument should be '--'", "--", cl.getOptionValue("o"));
        assertEquals("Remaining args should be 1", 1, cl.getArgs().length);
        assertEquals("Remaining arg should be 'file'", "file", cl.getArgs()[0]);
    }

    @Test
    public void testOptionWithArgumentThatLooksLikeOption() throws ParseException {
        options.addOption("o", true, "output");
        options.addOption("x", false, "xray");

        CommandLine cl = parser.parse(options, new String[]{"-o", "-x"});
        assertTrue("Option 'o' should be set", cl.hasOption("o"));
        assertEquals("Argument for o should be '-x'", "-x", cl.getOptionValue("o"));
        assertFalse("Option 'x' should not be set", cl.hasOption("x"));
    }

    @Test
    public void testMultipleShortOptionsWithOneRequiringArg() throws ParseException {
        options.addOption("a", false, "alpha");
        options.addOption("b", true, "beta");
        options.addOption("c", false, "gamma");

        CommandLine cl = parser.parse(options, new String[]{"-abc", "argForB"});
        assertTrue("Option 'a' should be set", cl.hasOption("a"));
        assertTrue("Option 'b' should be set", cl.hasOption("b"));
        assertEquals("Argument for b should be 'argForB'", "argForB", cl.getOptionValue("b"));
        assertTrue("Option 'c' should be set", cl.hasOption("c"));
    }

    @Test
    public void testFlattenWithNullOptions() {
        // flatten should handle null options gracefully (though not expected)
        String[] result = parser.flatten(null, new String[]{"-a"}, false);
        assertArrayEquals("Flatten with null options should return tokens as is", new String[]{"-a"}, result);
    }

    @Test
    public void testFlattenWithEmptyTokens() {
        options.addOption("a", false, "alpha");
        String[] result = parser.flatten(options, new String[]{}, false);
        assertArrayEquals("Flatten with empty tokens should return empty array", new String[]{}, result);
    }

    @Test
    public void testFlattenWithNullTokens() {
        options.addOption("a", false, "alpha");
        String[] result = parser.flatten(options, null, false);
        assertArrayEquals("Flatten with null tokens should return empty array", new String[]{}, result);
    }
}