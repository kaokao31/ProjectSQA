package org.apache.commons.cli;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for PosixParser, targeting Cli-18 bug (argument handling with '=').
 */
public class PosixParserTest {

    private Options options;
    private PosixParser parser;

    @Before
    public void setUp() {
        options = new Options();
        parser = new PosixParser();
    }

    // ========== Basic short options ==========

    @Test
    public void testShortOptionNoArg() throws ParseException {
        options.addOption("a", false, "no arg");
        CommandLine cmd = parser.parse(options, new String[]{"-a"});
        assertTrue(cmd.hasOption("a"));
        assertEquals(0, cmd.getArgs().length);
    }

    @Test
    public void testShortOptionWithArgSpace() throws ParseException {
        options.addOption("b", true, "requires arg");
        CommandLine cmd = parser.parse(options, new String[]{"-b", "value"});
        assertTrue(cmd.hasOption("b"));
        assertEquals("value", cmd.getOptionValue("b"));
    }

    @Test
    public void testShortOptionWithArgAttached() throws ParseException {
        options.addOption("c", true, "requires arg");
        CommandLine cmd = parser.parse(options, new String[]{"-cvalue"});
        assertTrue(cmd.hasOption("c"));
        assertEquals("value", cmd.getOptionValue("c"));
    }

    @Test
    public void testShortOptionWithArgAttachedEquals() throws ParseException {
        // Short options typically don't use '=', but PosixParser may handle it
        options.addOption("d", true, "requires arg");
        CommandLine cmd = parser.parse(options, new String[]{"-d=value"});
        assertTrue(cmd.hasOption("d"));
        assertEquals("=value", cmd.getOptionValue("d")); // '=' is part of value for short opts
    }

    // ========== Long options ==========

    @Test
    public void testLongOptionNoArg() throws ParseException {
        options.addOption("longa", false, "no arg");
        CommandLine cmd = parser.parse(options, new String[]{"--longa"});
        assertTrue(cmd.hasOption("longa"));
    }

    @Test
    public void testLongOptionWithArgSpace() throws ParseException {
        options.addOption("longb", true, "requires arg");
        CommandLine cmd = parser.parse(options, new String[]{"--longb", "value"});
        assertTrue(cmd.hasOption("longb"));
        assertEquals("value", cmd.getOptionValue("longb"));
    }

    @Test
    public void testLongOptionWithArgAttachedEquals() throws ParseException {
        // This is the core of Cli-18 bug: --opt=value should work
        options.addOption("longc", true, "requires arg");
        CommandLine cmd = parser.parse(options, new String[]{"--longc=value"});
        assertTrue(cmd.hasOption("longc"));
        assertEquals("value", cmd.getOptionValue("longc"));
    }

    @Test
    public void testLongOptionWithArgAttachedNoEquals() throws ParseException {
        // --opt value (space) already tested; --optvalue is ambiguous but PosixParser may treat as --opt value?
        // Typically long options require '=' or space; --optvalue is not standard.
        options.addOption("longd", true, "requires arg");
        CommandLine cmd = parser.parse(options, new String[]{"--longdvalue"});
        // This might be interpreted as option "longdvalue" which doesn't exist -> UnrecognizedOptionException
        // Or if option exists, it might be treated as --longd with value "value"? Unlikely.
        // We'll expect exception for unrecognized option.
        try {
            cmd = parser.parse(options, new String[]{"--longdvalue"});
            fail("Expected UnrecognizedOptionException");
        } catch (UnrecognizedOptionException e) {
            // expected
        }
    }

    // ========== Combined short options ==========

    @Test
    public void testCombinedShortOptions() throws ParseException {
        options.addOption("a", false, "no arg");
        options.addOption("b", false, "no arg");
        options.addOption("c", false, "no arg");
        CommandLine cmd = parser.parse(options, new String[]{"-abc"});
        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("b"));
        assertTrue(cmd.hasOption("c"));
    }

    @Test
    public void testCombinedShortOptionsWithArg() throws ParseException {
        options.addOption("a", false, "no arg");
        options.addOption("b", true, "requires arg");
        CommandLine cmd = parser.parse(options, new String[]{"-ab", "value"});
        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("b"));
        assertEquals("value", cmd.getOptionValue("b"));
    }

    @Test
    public void testCombinedShortOptionsWithArgAttached() throws ParseException {
        options.addOption("a", false, "no arg");
        options.addOption("b", true, "requires arg");
        CommandLine cmd = parser.parse(options, new String[]{"-abvalue"});
        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("b"));
        assertEquals("value", cmd.getOptionValue("b"));
    }

    // ========== Mixed short and long options ==========

    @Test
    public void testMixedOptions() throws ParseException {
        options.addOption("a", false, "no arg");
        options.addOption("long", true, "requires arg");
        CommandLine cmd = parser.parse(options, new String[]{"-a", "--long", "val"});
        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("long"));
        assertEquals("val", cmd.getOptionValue("long"));
    }

    // ========== Non-option arguments ==========

    @Test
    public void testNonOptionArgs() throws ParseException {
        options.addOption("a", false, "no arg");
        CommandLine cmd = parser.parse(options, new String[]{"-a", "file1", "file2"});
        assertTrue(cmd.hasOption("a"));
        assertArrayEquals(new String[]{"file1", "file2"}, cmd.getArgs());
    }

    @Test
    public void testStopAtNonOption() throws ParseException {
        options.addOption("a", false, "no arg");
        // Default behavior: stop at first non-option unless POSIXLY_CORRECT? PosixParser stops.
        CommandLine cmd = parser.parse(options, new String[]{"-a", "file", "-b"});
        assertTrue(cmd.hasOption("a"));
        assertArrayEquals(new String[]{"file", "-b"}, cmd.getArgs());
    }

    // ========== Edge cases ==========

    @Test(expected = UnrecognizedOptionException.class)
    public void testUnknownShortOption() throws ParseException {
        parser.parse(options, new String[]{"-x"});
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testUnknownLongOption() throws ParseException {
        parser.parse(options, new String[]{"--unknown"});
    }

    @Test(expected = MissingArgumentException.class)
    public void testMissingArgForShortOption() throws ParseException {
        options.addOption("a", true, "requires arg");
        parser.parse(options, new String[]{"-a"});
    }

    @Test(expected = MissingArgumentException.class)
    public void testMissingArgForLongOption() throws ParseException {
        options.addOption("long", true, "requires arg");
        parser.parse(options, new String[]{"--long"});
    }

    @Test
    public void testEmptyArgs() throws ParseException {
        CommandLine cmd = parser.parse(options, new String[0]);
        assertFalse(cmd.hasOption("a"));
        assertEquals(0, cmd.getArgs().length);
    }

    @Test
    public void testNullArgs() throws ParseException {
        // parse may accept null? Typically not, but we test robustness.
        try {
            parser.parse(options, null);
            fail("Expected NullPointerException or IllegalArgumentException");
        } catch (NullPointerException | IllegalArgumentException e) {
            // expected
        }
    }

    // ========== Option with long option only ==========

    @Test
    public void testLongOptionWithArgEqualsAndNoSpace() throws ParseException {
        // Additional test for Cli-18: ensure --opt=value works when option has long only
        options.addOption(null, "longonly", true, "long only");
        CommandLine cmd = parser.parse(options, new String[]{"--longonly=value"});
        assertTrue(cmd.hasOption("longonly"));
        assertEquals("value", cmd.getOptionValue("longonly"));
    }

    @Test
    public void testLongOptionWithArgSpaceAndNoShort() throws ParseException {
        options.addOption(null, "longonly", true, "long only");
        CommandLine cmd = parser.parse(options, new String[]{"--longonly", "value"});
        assertTrue(cmd.hasOption("longonly"));
        assertEquals("value", cmd.getOptionValue("longonly"));
    }

    // ========== Option with multiple values ==========

    @Test
    public void testOptionWithMultipleArgs() throws ParseException {
        // Options can accept multiple arguments if set accordingly (but PosixParser doesn't handle natively)
        // We'll test basic case where option is defined with value separator? Not in PosixParser.
        // Just ensure that extra args become non-option args.
        options.addOption("a", true, "requires arg");
        CommandLine cmd = parser.parse(options, new String[]{"-a", "val1", "val2"});
        assertTrue(cmd.hasOption("a"));
        assertEquals("val1", cmd.getOptionValue("a"));
        assertArrayEquals(new String[]{"val2"}, cmd.getArgs());
    }

    // ========== Test flatten method indirectly ==========

    @Test
    public void testFlattenWithMixedArgs() throws ParseException {
        // flatten is called internally; we test through parse
        options.addOption("a", false, "no arg");
        options.addOption("b", true, "requires arg");
        options.addOption("c", false, "no arg");
        CommandLine cmd = parser.parse(options, new String[]{"-a", "-b", "val", "-c", "extra"});
        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("b"));
        assertEquals("val", cmd.getOptionValue("b"));
        assertTrue(cmd.hasOption("c"));
        assertArrayEquals(new String[]{"extra"}, cmd.getArgs());
    }

    // ========== Test bug scenario: option with '=' and no space ==========

    @Test
    public void testBugCli18_AttachedEqualsLongOption() throws ParseException {
        // This is the specific bug: --opt=value should be parsed correctly
        options.addOption("o", "opt", true, "option with arg");
        CommandLine cmd = parser.parse(options, new String[]{"--opt=value"});
        assertTrue(cmd.hasOption("o"));
        assertTrue(cmd.hasOption("opt"));
        assertEquals("value", cmd.getOptionValue("o"));
        assertEquals("value", cmd.getOptionValue("opt"));
    }

    @Test
    public void testBugCli18_AttachedEqualsShortOption() throws ParseException {
        // Short option with '=' is not standard but PosixParser may handle it
        options.addOption("o", true, "option with arg");
        CommandLine cmd = parser.parse(options, new String[]{"-o=value"});
        assertTrue(cmd.hasOption("o"));
        assertEquals("=value", cmd.getOptionValue("o")); // '=' is part of value for short
    }

    // ========== Test with required options ==========

    @Test(expected = MissingOptionException.class)
    public void testMissingRequiredOption() throws ParseException {
        Option opt = Option.builder("r").required(true).hasArg(false).build();
        options.addOption(opt);
        parser.parse(options, new String[]{});
    }

    @Test
    public void testRequiredOptionPresent() throws ParseException {
        Option opt = Option.builder("r").required(true).hasArg(false).build();
        options.addOption(opt);
        CommandLine cmd = parser.parse(options, new String[]{"-r"});
        assertTrue(cmd.hasOption("r"));
    }

    // ========== Test with option groups ==========

    @Test
    public void testOptionGroup() throws ParseException {
        OptionGroup group = new OptionGroup();
        group.addOption(Option.builder("a").build());
        group.addOption(Option.builder("b").build());
        options.addOptionGroup(group);
        CommandLine cmd = parser.parse(options, new String[]{"-a"});
        assertTrue(cmd.hasOption("a"));
        assertFalse(cmd.hasOption("b"));
    }

    @Test(expected = AlreadySelectedException.class)
    public void testOptionGroupConflict() throws ParseException {
        OptionGroup group = new OptionGroup();
        group.addOption(Option.builder("a").build());
        group.addOption(Option.builder("b").build());
        options.addOptionGroup(group);
        parser.parse(options, new String[]{"-a", "-b"});
    }

    // ========== Test with argument separator ==========

    @Test
    public void testArgumentSeparator() throws ParseException {
        options.addOption("a", false, "no arg");
        CommandLine cmd = parser.parse(options, new String[]{"-a", "--", "arg1", "-b"});
        assertTrue(cmd.hasOption("a"));
        assertArrayEquals(new String[]{"arg1", "-b"}, cmd.getArgs());
    }

    // ========== Test with long option and equals with empty value ==========

    @Test
    public void testLongOptionWithEmptyValue() throws ParseException {
        options.addOption("opt", true, "requires arg");
        CommandLine cmd = parser.parse(options, new String[]{"--opt="});
        assertTrue(cmd.hasOption("opt"));
        assertEquals("", cmd.getOptionValue("opt"));
    }

    // ========== Test with multiple long options ==========

    @Test
    public void testMultipleLongOptions() throws ParseException {
        options.addOption("opt1", true, "arg1");
        options.addOption("opt2", false, "no arg");
        CommandLine cmd = parser.parse(options, new String[]{"--opt1=val1", "--opt2"});
        assertTrue(cmd.hasOption("opt1"));
        assertEquals("val1", cmd.getOptionValue("opt1"));
        assertTrue(cmd.hasOption("opt2"));
    }

    // ========== Test with option that has optional argument ==========

    @Test
    public void testOptionalArgShortOption() throws ParseException {
        Option opt = Option.builder("o").hasArg(true).optionalArg(true).build();
        options.addOption(opt);
        // Without argument
        CommandLine cmd1 = parser.parse(options, new String[]{"-o"});
        assertTrue(cmd1.hasOption("o"));
        assertNull(cmd1.getOptionValue("o"));
        // With argument
        CommandLine cmd2 = parser.parse(options, new String[]{"-o", "val"});
        assertTrue(cmd2.hasOption("o"));
        assertEquals("val", cmd2.getOptionValue("o"));
    }

    @Test
    public void testOptionalArgLongOption() throws ParseException {
        Option opt = Option.builder("longopt").hasArg(true).optionalArg(true).build();
        options.addOption(opt);
        // Without argument
        CommandLine cmd1 = parser.parse(options, new String[]{"--longopt"});
        assertTrue(cmd1.hasOption("longopt"));
        assertNull(cmd1.getOptionValue("longopt"));
        // With argument (space)
        CommandLine cmd2 = parser.parse(options, new String[]{"--longopt", "val"});
        assertTrue(cmd2.hasOption("longopt"));
        assertEquals("val", cmd2.getOptionValue("longopt"));
        // With argument (equals)
        CommandLine cmd3 = parser.parse(options, new String[]{"--longopt=val"});
        assertTrue(cmd3.hasOption("longopt"));
        assertEquals("val", cmd3.getOptionValue("longopt"));
    }

    // ========== Test with property option ==========

    @Test
    public void testPropertyOption() throws ParseException {
        options.addOption("D", true, "define property");
        CommandLine cmd = parser.parse(options, new String[]{"-Dkey=value"});
        assertTrue(cmd.hasOption("D"));
        assertEquals("key=value", cmd.getOptionValue("D"));
    }

    // ========== Test with negative numbers (not options) ==========

    @Test
    public void testNegativeNumberAsArg() throws ParseException {
        options.addOption("a", false, "no arg");
        CommandLine cmd = parser.parse(options, new String[]{"-a", "-5"});
        assertTrue(cmd.hasOption("a"));
        assertArrayEquals(new String[]{"-5"}, cmd.getArgs());
    }

    // ========== Test with double dash ==========

    @Test
    public void testDoubleDashOnly() throws ParseException {
        CommandLine cmd = parser.parse(options, new String[]{"--", "arg"});
        assertArrayEquals(new String[]{"arg"}, cmd.getArgs());
    }

    // ========== Test with empty string option ==========

    @Test(expected = UnrecognizedOptionException.class)
    public void testEmptyOption() throws ParseException {
        parser.parse(options, new String[]{"-"});
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testEmptyLongOption() throws ParseException {
        parser.parse(options, new String[]{"--"});
    }
}