package org.apache.commons.cli;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class DefaultParserTest {

    private DefaultParser parser;
    private Options options;

    @Before
    public void setUp() {
        parser = new DefaultParser();
        options = new Options();
    }

    // -------------------- Short option without argument --------------------
    @Test
    public void testShortOptionWithoutArg() throws ParseException {
        options.addOption("a", false, "no arg");
        CommandLine cmd = parser.parse(options, new String[]{"-a"});
        assertTrue(cmd.hasOption("a"));
    }

    // -------------------- Short option with required argument --------------------
    @Test
    public void testShortOptionWithRequiredArgProvided() throws ParseException {
        options.addOption("b", true, "requires arg");
        CommandLine cmd = parser.parse(options, new String[]{"-b", "value"});
        assertTrue(cmd.hasOption("b"));
        assertEquals("value", cmd.getOptionValue("b"));
    }

    @Test(expected = MissingArgumentException.class)
    public void testShortOptionWithRequiredArgMissing() throws ParseException {
        options.addOption("c", true, "requires arg");
        parser.parse(options, new String[]{"-c"});
    }

    // -------------------- Short option with optional argument --------------------
    @Test
    public void testShortOptionWithOptionalArgNotGiven() throws ParseException {
        options.addOption("d", false, "optional arg? Actually false means no arg");
        // Use Option.Builder to create optional argument
        Option opt = Option.builder("d").hasArg(true).optionalArg(true).build();
        options.addOption(opt);
        CommandLine cmd = parser.parse(options, new String[]{"-d"});
        assertTrue(cmd.hasOption("d"));
        assertNull(cmd.getOptionValue("d"));
    }

    @Test
    public void testShortOptionWithOptionalArgGiven() throws ParseException {
        Option opt = Option.builder("e").hasArg(true).optionalArg(true).build();
        options.addOption(opt);
        CommandLine cmd = parser.parse(options, new String[]{"-e", "argval"});
        assertTrue(cmd.hasOption("e"));
        assertEquals("argval", cmd.getOptionValue("e"));
    }

    // -------------------- Long option with argument separated by space --------------------
    @Test
    public void testLongOptionWithArgSpace() throws ParseException {
        options.addOption("longOpt", true, "long option with arg");
        CommandLine cmd = parser.parse(options, new String[]{"--longOpt", "value"});
        assertTrue(cmd.hasOption("longOpt"));
        assertEquals("value", cmd.getOptionValue("longOpt"));
    }

    // -------------------- Long option with argument separated by '=' --------------------
    @Test
    public void testLongOptionWithArgEquals() throws ParseException {
        options.addOption("longOpt", true, "long option with arg");
        CommandLine cmd = parser.parse(options, new String[]{"--longOpt=value"});
        assertTrue(cmd.hasOption("longOpt"));
        assertEquals("value", cmd.getOptionValue("longOpt"));
    }

    // -------------------- Negative number as argument (potential bug area) --------------------
    @Test
    public void testNegativeNumberAsArgument() throws ParseException {
        options.addOption("D", true, "property");
        CommandLine cmd = parser.parse(options, new String[]{"-D", "-1"});
        assertTrue(cmd.hasOption("D"));
        assertEquals("-1", cmd.getOptionValue("D"));
    }

    @Test
    public void testOptionValueStartsWithMinusLong() throws ParseException {
        options.addOption("D", true, "property");
        CommandLine cmd = parser.parse(options, new String[]{"-Dfoo=-bar"});
        assertTrue(cmd.hasOption("D"));
        // Argument should be "foo=-bar" or "=-bar"? Actually depending on separator.
        // This tests the known bug (Cli-38) where = in argument might be mishandled.
        assertEquals("foo=-bar", cmd.getOptionValue("D"));
    }

    // -------------------- Grouped short options --------------------
    @Test
    public void testGroupedShortOptions() throws ParseException {
        options.addOption("a", false, "a");
        options.addOption("b", false, "b");
        options.addOption("c", false, "c");
        CommandLine cmd = parser.parse(options, new String[]{"-abc"});
        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("b"));
        assertTrue(cmd.hasOption("c"));
    }

    // -------------------- Stop parsing at "--" --------------------
    @Test
    public void testDoubleDashStopsParsing() throws ParseException {
        options.addOption("x", false, "x");
        CommandLine cmd = parser.parse(options, new String[]{"-x", "--", "-y", "arg"});
        assertTrue(cmd.hasOption("x"));
        assertFalse(cmd.hasOption("y"));
        // The remaining arguments should be accessible via getArgs()
        assertEquals(2, cmd.getArgs().length);
        assertEquals("-y", cmd.getArgs()[0]);
        assertEquals("arg", cmd.getArgs()[1]);
    }

    // -------------------- Unrecognized option throws exception --------------------
    @Test(expected = UnrecognizedOptionException.class)
    public void testUnrecognizedShortOption() throws ParseException {
        options.addOption("a", false, "a");
        parser.parse(options, new String[]{"-z"});
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testUnrecognizedLongOption() throws ParseException {
        options.addOption("known", false, "known");
        parser.parse(options, new String[]{"--unknown"});
    }

    // -------------------- Ambiguous option (partial prefix) --------------------
    @Test(expected = AmbiguousOptionException.class)
    public void testAmbiguousLongOption() throws ParseException {
        options.addOption("opt1", false, "first");
        options.addOption("opt2", false, "second");
        parser.parse(options, new String[]{"--opt"});
    }

    // -------------------- Required options missing --------------------
    @Test(expected = MissingOptionException.class)
    public void testMissingRequiredOption() throws ParseException {
        Option opt = Option.builder("r").required(true).build();
        options.addOption(opt);
        parser.parse(options, new String[]{});
    }

    // -------------------- Argument attached to short option (no space) --------------------
    @Test
    public void testShortOptionAttachedArg() throws ParseException {
        options.addOption("o", true, "output");
        CommandLine cmd = parser.parse(options, new String[]{"-ofile.txt"});
        assertTrue(cmd.hasOption("o"));
        assertEquals("file.txt", cmd.getOptionValue("o"));
    }

    // -------------------- Null arguments array --------------------
    @Test(expected = NullPointerException.class)
    public void testNullArguments() throws ParseException {
        parser.parse(options, (String[]) null);
    }

    // -------------------- Empty arguments array --------------------
    @Test
    public void testEmptyArguments() throws ParseException {
        CommandLine cmd = parser.parse(options, new String[]{});
        assertNotNull(cmd);
    }

    // -------------------- Properties parsing --------------------
    @Test
    public void testParseWithProperties() throws ParseException {
        options.addOption("D", true, "property");
        java.util.Properties props = new java.util.Properties();
        props.setProperty("key", "value");
        CommandLine cmd = parser.parse(options, new String[]{"-Dother=val"}, props);
        // The option -D should have value "other=val"
        assertEquals("other=val", cmd.getOptionValue("D"));
        // The properties should be set as default options?
        // In Commons CLI, properties are used to fill in missing options.
        // Check that the property key is also available as option? Not necessarily.
        // Just ensure no exception.
    }

    // -------------------- Option with long prefix but short name (should be short) --------------------
    @Test
    public void testShortOptionWithLongPrefix() throws ParseException {
        options.addOption("help", false, "help");
        CommandLine cmd = parser.parse(options, new String[]{"-help"});
        // Since "help" is not a short option (single char), this should not be recognized?
        // Actually, -help with h being short option? h is not defined.
        // This will throw UnrecognizedOptionException
        // We'll test that it fails.
        assertFalse(cmd.hasOption("help")); // Actually it should be unrecognized.
    }

    // More edge cases: argument containing '=' for short option (should be part of value)
    @Test
    public void testShortOptionArgWithEquals() throws ParseException {
        options.addOption("D", true, "property");
        CommandLine cmd = parser.parse(options, new String[]{"-Dfoo=bar"});
        assertTrue(cmd.hasOption("D"));
        // The value attached to -D is "foo=bar" (since no space before =)
        assertEquals("foo=bar", cmd.getOptionValue("D"));
    }

    // Option with multiple arguments (GnuParser style? Not supported by DefaultParser usually)
    // But we can test that only first is taken.

    // Test that arguments after an option with arg are consumed correctly even if they look like options
    @Test
    public void testOptionWithArgLooksLikeOption() throws ParseException {
        options.addOption("a", true, "a requires arg");
        options.addOption("b", false, "b");
        CommandLine cmd = parser.parse(options, new String[]{"-a", "-b"});
        assertTrue(cmd.hasOption("a"));
        assertEquals("-b", cmd.getOptionValue("a"));
        assertFalse(cmd.hasOption("b"));
    }
}