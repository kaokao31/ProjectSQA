package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Test suite for {@link Parser} targeting Defects4J bug Cli-28.
 * Covers option parsing branches, argument validation, and exception paths.
 */
public class ParserTest {

    private Options options;
    private TestParser parser;

    @Before
    public void setUp() {
        options = new Options();
        parser = new TestParser();
    }

    // ------------------------------------------------------------------
    // Nested concrete Parser for testing abstract class
    // ------------------------------------------------------------------
    private static class TestParser extends Parser {
        @Override
        protected String[] flatten(Options options, String[] arguments, boolean stopAtNonOption) {
            // Simple flatten: return arguments as-is
            return arguments;
        }
    }

    // ------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------
    private void addRequiredOption(boolean required) {
        Option opt = new Option("r", "required", false, "required option");
        opt.setRequired(required);
        options.addOption(opt);
    }

    private void addOptionWithArg(boolean required) {
        Option opt = new Option("o", "option", true, "option with arg");
        opt.setRequired(required);
        options.addOption(opt);
    }

    private void addOptionWithoutArg() {
        options.addOption("a", "all", false, "no arg option");
    }

    // ------------------------------------------------------------------
    // Normal parsing scenarios
    // ------------------------------------------------------------------
    @Test
    public void testParseNoArguments() throws ParseException {
        CommandLine cmd = parser.parse(options, new String[]{});
        assertNotNull(cmd);
        assertFalse(cmd.hasOption("a"));
    }

    @Test
    public void testParseSingleOptionNoArg() throws ParseException {
        addOptionWithoutArg();
        CommandLine cmd = parser.parse(options, new String[]{"-a"});
        assertTrue(cmd.hasOption("a"));
        assertNull(cmd.getOptionValue("a"));
    }

    @Test
    public void testParseLongOptionNoArg() throws ParseException {
        addOptionWithoutArg();
        CommandLine cmd = parser.parse(options, new String[]{"--all"});
        assertTrue(cmd.hasOption("all"));
    }

    @Test
    public void testParseOptionWithArg() throws ParseException {
        addOptionWithArg(false);
        CommandLine cmd = parser.parse(options, new String[]{"-o", "value"});
        assertEquals("value", cmd.getOptionValue("o"));
    }

    @Test
    public void testParseLongOptionWithArg() throws ParseException {
        addOptionWithArg(false);
        CommandLine cmd = parser.parse(options, new String[]{"--option", "value"});
        assertEquals("value", cmd.getOptionValue("option"));
    }

    @Test
    public void testParseMultipleOptions() throws ParseException {
        addOptionWithoutArg();
        addOptionWithArg(false);
        CommandLine cmd = parser.parse(options, new String[]{"-a", "-o", "val"});
        assertTrue(cmd.hasOption("a"));
        assertEquals("val", cmd.getOptionValue("o"));
    }

    // ------------------------------------------------------------------
    // Boundary / bug-triggering scenarios (Cli-28 related)
    // ------------------------------------------------------------------
    @Test(expected = MissingArgumentException.class)
    public void testMissingArgumentForRequiredArgOptionThrowsException() throws ParseException {
        addOptionWithArg(false);
        // Option requires an argument, but no argument supplied
        parser.parse(options, new String[]{"-o"});
    }

    @Test(expected = MissingArgumentException.class)
    public void testMissingArgumentForLongOptionThrowsException() throws ParseException {
        addOptionWithArg(false);
        parser.parse(options, new String[]{"--option"});
    }

    @Test(expected = MissingOptionException.class)
    public void testRequiredOptionMissingThrowsException() throws ParseException {
        addRequiredOption(true);
        parser.parse(options, new String[]{});
    }

    @Test(expected = MissingOptionException.class)
    public void testMultipleRequiredOptionsMissingThrowsException() throws ParseException {
        addRequiredOption(true);
        addRequiredOption(true);
        parser.parse(options, new String[]{});
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testUnknownOptionThrowsException() throws ParseException {
        parser.parse(options, new String[]{"-x"});
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testUnknownLongOptionThrowsException() throws ParseException {
        parser.parse(options, new String[]{"--unknown"});
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testMixedKnownAndUnknownOptionThrowsException() throws ParseException {
        addOptionWithoutArg();
        parser.parse(options, new String[]{"-a", "-z"});
    }

    // ------------------------------------------------------------------
    // Properties processing (covers processProperties route)
    // ------------------------------------------------------------------
    @Test
    public void testPropertiesContainOption() throws ParseException {
        addOptionWithoutArg();
        Properties props = new Properties();
        props.setProperty("a", "true");
        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertTrue(cmd.hasOption("a"));
    }

    @Test
    public void testPropertiesProvideOptionValue() throws ParseException {
        addOptionWithArg(false);
        Properties props = new Properties();
        props.setProperty("o", "fromProps");
        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertEquals("fromProps", cmd.getOptionValue("o"));
    }

    @Test(expected = MissingArgumentException.class)
    public void testPropertiesOptionNeedsArgButEmptyValue() throws ParseException {
        addOptionWithArg(false);
        Properties props = new Properties();
        props.setProperty("o", "");
        parser.parse(options, new String[]{}, props);
    }

    // ------------------------------------------------------------------
    // Combined properties and command line options (priority behavior)
    // ------------------------------------------------------------------
    @Test
    public void testCommandLineOverridesProperties() throws ParseException {
        addOptionWithArg(false);
        Properties props = new Properties();
        props.setProperty("o", "propertyValue");
        CommandLine cmd = parser.parse(options, new String[]{"-o", "cliValue"}, props);
        assertEquals("cliValue", cmd.getOptionValue("o"));
    }

    @Test
    public void testPropertiesNotOverrideWhenOptionExistsInCommandLine() throws ParseException {
        addOptionWithoutArg();
        Properties props = new Properties();
        props.setProperty("a", "true");
        // Command line has -a, but no value; properties should still be considered
        CommandLine cmd = parser.parse(options, new String[]{"-a"}, props);
        assertTrue(cmd.hasOption("a"));
    }

    // ------------------------------------------------------------------
    // Edge cases: empty string arguments, non-option tokens
    // ------------------------------------------------------------------
    @Test
    public void testParseEmptyStringArgument() throws ParseException {
        addOptionWithoutArg();
        CommandLine cmd = parser.parse(options, new String[]{""});
        assertFalse(cmd.hasOption("a"));
    }

    @Test
    public void testParseNonOptionWithStopAtNonOption() throws ParseException {
        addOptionWithoutArg();
        // Non-option token should be treated as a positional argument
        String[] args = new String[]{"file.txt"};
        CommandLine cmd = parser.parse(options, args);
        assertEquals(1, cmd.getArgs().length);
        assertEquals("file.txt", cmd.getArgs()[0]);
    }

    @Test
    public void testParseOptionValueSeparatedByEquals() throws ParseException {
        addOptionWithArg(false);
        CommandLine cmd = parser.parse(options, new String[]{"-o=value"});
        assertEquals("value", cmd.getOptionValue("o"));
    }

    @Test
    public void testParseLongOptionValueSeparatedByEquals() throws ParseException {
        addOptionWithArg(false);
        CommandLine cmd = parser.parse(options, new String[]{"--option=value"});
        assertEquals("value", cmd.getOptionValue("option"));
    }

    // ------------------------------------------------------------------
    // Multiple options, some with args, some without
    // ------------------------------------------------------------------
    @Test
    public void testParseMultipleOptionsWithArgs() throws ParseException {
        addOptionWithoutArg();
        addOptionWithArg(false);
        addOptionWithArg(true);
        CommandLine cmd = parser.parse(options, new String[]{"-a", "-o", "val1", "-r", "val2"});
        assertTrue(cmd.hasOption("a"));
        assertEquals("val1", cmd.getOptionValue("o"));
        assertEquals("val2", cmd.getOptionValue("r"));
    }

    @Test(expected = MissingArgumentException.class)
    public void testMissingArgumentInPresenceOfOtherOptions() throws ParseException {
        addOptionWithoutArg();
        addOptionWithArg(false);
        parser.parse(options, new String[]{"-a", "-o"}); // -o missing its argument
    }

    // ------------------------------------------------------------------
    // Null / empty options object handling
    // ------------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
    public void testParseWithNullOptionsThrowsException() throws ParseException {
        parser.parse(null, new String[]{"-a"});
    }

    @Test
    public void testParseWithNullArguments() throws ParseException {
        addOptionWithoutArg();
        CommandLine cmd = parser.parse(options, null);
        assertNotNull(cmd);
        assertFalse(cmd.hasOption("a"));
    }

    // ------------------------------------------------------------------
    // Test reusing parser after an exception
    // ------------------------------------------------------------------
    @Test
    public void testParserCanBeReusedAfterException() throws ParseException {
        addOptionWithoutArg();
        try {
            parser.parse(options, new String[]{"-z"});
            fail("Expected UnrecognizedOptionException");
        } catch (UnrecognizedOptionException e) {
            // expected
        }
        // parser should still work
        CommandLine cmd = parser.parse(options, new String[]{"-a"});
        assertTrue(cmd.hasOption("a"));
    }
}