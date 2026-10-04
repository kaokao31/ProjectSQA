package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

public class DefaultParserTest {

    private Options options;
    private DefaultParser parser;
    private DefaultParser parserAllowPartial;

    @Before
    public void setUp() {
        options = new Options();

        // Add various types of options
        Option help = Option.builder("h").longOpt("help").desc("print help").build();
        Option verbose = Option.builder("v").longOpt("verbose").desc("verbose output").build();
        Option output = Option.builder("o").longOpt("output").hasArg().argName("file").desc("output file").build();
        Option input = Option.builder("i").longOpt("input").hasArg().argName("dir").desc("input directory").build();
        Option debug = Option.builder("d").longOpt("debug").optionalArg(true).hasArg().argName("level").desc("debug level").build();
        Option properties = Option.builder("D").longOpt("define").hasArgs().valueSeparator('=').argName("property=value").desc("define a property").build();
        Option req = Option.builder("r").longOpt("required").required(true).desc("required option").build();
        Option negative = Option.builder("n").longOpt("negative").type(Number.class).desc("negative number argument").build();
        Option multi = Option.builder("m").longOpt("multi").hasArgs().argName("values").desc("multiple values").build();

        options.addOption(help);
        options.addOption(verbose);
        options.addOption(output);
        options.addOption(input);
        options.addOption(debug);
        options.addOption(properties);
        options.addOption(req);
        options.addOption(negative);
        options.addOption(multi);

        parser = new DefaultParser();
        parserAllowPartial = new DefaultParser(true); // allow partial matching
    }

    // --- Basic tests ---

    @Test
    public void testParseNoOptions() throws ParseException {
        String[] args = {};
        CommandLine cmd = parser.parse(options, args);
        assertNotNull(cmd);
        assertEquals(0, cmd.getArgs().length);
    }

    @Test
    public void testParseShortOptions() throws ParseException {
        String[] args = {"-h", "-v"};
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("h"));
        assertTrue(cmd.hasOption("v"));
    }

    @Test
    public void testParseLongOptions() throws ParseException {
        String[] args = {"--help", "--verbose"};
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("help"));
        assertTrue(cmd.hasOption("verbose"));
    }

    @Test
    public void testParseOptionWithArgument() throws ParseException {
        String[] args = {"-o", "output.txt", "-i", "src"};
        CommandLine cmd = parser.parse(options, args);
        assertEquals("output.txt", cmd.getOptionValue("o"));
        assertEquals("src", cmd.getOptionValue("i"));
    }

    @Test
    public void testParseOptionWithArgumentUsingEquals() throws ParseException {
        String[] args = {"--output=out.txt", "--input=in"};
        CommandLine cmd = parser.parse(options, args);
        assertEquals("out.txt", cmd.getOptionValue("output"));
        assertEquals("in", cmd.getOptionValue("input"));
    }

    @Test
    public void testParseShortOptionBursting() throws ParseException {
        String[] args = {"-hv"};
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("h"));
        assertTrue(cmd.hasOption("v"));
    }

    // --- Test required option ---

    @Test(expected = MissingOptionException.class)
    public void testParseMissingRequiredOption() throws ParseException {
        String[] args = {"-h"};
        parser.parse(options, args);
    }

    @Test
    public void testParseRequiredOptionPresent() throws ParseException {
        String[] args = {"-r"};
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("r"));
    }

    // --- Test stop at non-option ---

    @Test
    public void testParseStopAtNonOption() throws ParseException {
        String[] args = {"-h", "--", "--output", "file.txt"};
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("h"));
        assertEquals(3, cmd.getArgs().length);
        assertEquals("--output", cmd.getArgs()[0]);
        assertEquals("file.txt", cmd.getArgs()[1]);
    }

    // --- Test negative numbers ---

    @Test
    public void testParseNegativeNumber() throws ParseException {
        String[] args = {"-n", "-5"};
        CommandLine cmd = parser.parse(options, args);
        assertEquals("-5", cmd.getOptionValue("n"));
    }

    @Test
    public void testParseNegativeNumberAsArgument() throws ParseException {
        String[] args = {"--negative=-3"};
        CommandLine cmd = parser.parse(options, args);
        assertEquals("-3", cmd.getOptionValue("negative"));
    }

    // --- Test properties ---

    @Test
    public void testParseProperties() throws ParseException {
        Properties p = new Properties();
        p.setProperty("input", "src");
        p.setProperty("output", "build");
        String[] args = {"-r"}; // need required option
        CommandLine cmd = parser.parse(options, args, p);
        assertEquals("src", cmd.getOptionValue("input"));
        assertEquals("build", cmd.getOptionValue("output"));
    }

    @Test(expected = ParseException.class)
    public void testParsePropertiesWithUnknownOption() throws ParseException {
        Properties p = new Properties();
        p.setProperty("unknown", "value");
        String[] args = {"-r"};
        parser.parse(options, args, p);
    }

    // Test properties with null value (potential bug: Cli-30)
    @Test
    public void testParsePropertiesWithNullValue() throws ParseException {
        Properties p = new Properties();
        p.setProperty("input", null); // null value
        String[] args = {"-r"};
        CommandLine cmd = parser.parse(options, args, p);
        // The parser should ignore null values
        assertNull(cmd.getOptionValue("input"));
    }

    // Test properties with empty string value
    @Test
    public void testParsePropertiesWithEmptyValue() throws ParseException {
        Properties p = new Properties();
        p.setProperty("input", "");
        String[] args = {"-r"};
        CommandLine cmd = parser.parse(options, args, p);
        assertEquals("", cmd.getOptionValue("input"));
    }

    // --- Test optional argument ---

    @Test
    public void testParseOptionalArgPresent() throws ParseException {
        String[] args = {"-d", "debug1", "-v"};
        CommandLine cmd = parser.parse(options, args);
        assertEquals("debug1", cmd.getOptionValue("d"));
    }

    @Test
    public void testParseOptionalArgAbsent() throws ParseException {
        String[] args = {"-d", "-v"};
        CommandLine cmd = parser.parse(options, args);
        assertNull(cmd.getOptionValue("d"));
        assertTrue(cmd.hasOption("v"));
    }

    // --- Test multiple arguments ---

    @Test
    public void testParseMultipleArgs() throws ParseException {
        String[] args = {"-m", "a", "b", "c"};
        CommandLine cmd = parser.parse(options, args);
        assertArrayEquals(new String[]{"a", "b", "c"}, cmd.getOptionValues("m"));
    }

    // --- Test unrecognized option ---

    @Test(expected = UnrecognizedOptionException.class)
    public void testParseUnknownOption() throws ParseException {
        String[] args = {"-z"};
        parser.parse(options, args);
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testParseUnknownLongOption() throws ParseException {
        String[] args = {"--unknown"};
        parser.parse(options, args);
    }

    // --- Test partial matching (allowPartial) ---

    @Test
    public void testParsePartialMatching() throws ParseException {
        Options opts = new Options();
        opts.addOption(Option.builder("longopt").longOpt("longoption").build());
        DefaultParser parserPartial = new DefaultParser(true);
        String[] args = {"--long"};
        CommandLine cmd = parserPartial.parse(opts, args);
        assertTrue(cmd.hasOption("longoption"));
    }

    @Test(expected = AmbiguousOptionException.class)
    public void testParseAmbiguousPartial() throws ParseException {
        Options opts = new Options();
        opts.addOption(Option.builder("longopt1").longOpt("longoption1").build());
        opts.addOption(Option.builder("longopt2").longOpt("longoption2").build());
        DefaultParser parserPartial = new DefaultParser(true);
        String[] args = {"--long"};
        parserPartial.parse(opts, args);
    }

    // --- Test default option separator ---

    @Test
    public void testParseWithPropertyOption() throws ParseException {
        String[] args = {"-Dkey=value", "-Dname=John"};
        CommandLine cmd = parser.parse(options, args);
        assertEquals("value", cmd.getOptionProperties("D").getProperty("key"));
        assertEquals("John", cmd.getOptionProperties("D").getProperty("name"));
    }

    // --- Test empty args ---

    @Test
    public void testParseEmptyArgs() throws ParseException {
        String[] args = {};
        CommandLine cmd = parser.parse(options, args);
        assertNotNull(cmd);
        assertEquals(0, cmd.getArgs().length);
    }

    // --- Test with builder options (long-only) ---

    @Test
    public void testParseLongOnlyNoMatch() throws ParseException {
        Options opts = new Options();
        opts.addOption(Option.builder("a").longOpt("aaa").build());
        opts.addOption(Option.builder("b").longOpt("bbb").build());
        DefaultParser parserLong = new DefaultParser(false);
        String[] args = {"--a"}; // not valid long option, should not match partial
        try {
            parserLong.parse(opts, args);
            fail("Expected UnrecognizedOptionException");
        } catch (UnrecognizedOptionException e) {
            // expected
        }
    }

    // --- Test default constructor ---

    @Test
    public void testDefaultConstructor() throws ParseException {
        DefaultParser defaultParser = new DefaultParser();
        assertNotNull(defaultParser);
    }

    // --- Test boolean options ---

    @Test
    public void testParseBooleanOptions() throws ParseException {
        Options opts = new Options();
        opts.addOption(Option.builder("x").build());
        opts.addOption(Option.builder("y").build());
        String[] args = {"-x", "-y"};
        CommandLine cmd = parser.parse(opts, args);
        assertTrue(cmd.hasOption("x"));
        assertTrue(cmd.hasOption("y"));
    }

    // --- Test that properties override command line? ---

    @Test
    public void testPropertyOverridesCommandLine() throws ParseException {
        Properties p = new Properties();
        p.setProperty("output", "prop_value");
        String[] args = {"-r", "-o", "cmd_value"};
        CommandLine cmd = parser.parse(options, args, p);
        // The default behavior: properties are parsed after command line, so property may override? Actually depends on implementation.
        // According to DefaultParser, properties are processed after command line arguments, and for options that have a value from properties,
        // they may override. We'll test the actual behavior: we expect "prop_value" because property overrides.
        assertEquals("prop_value", cmd.getOptionValue("output"));
    }

    // --- Test argument capture without options ---

    @Test
    public void testParseArgsCapture() throws ParseException {
        String[] args = {"file1", "file2", "-v"};
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("v"));
        assertArrayEquals(new String[]{"file1", "file2"}, cmd.getArgs());
    }

    // --- Test confusing short/long options ---

    @Test
    public void testParseShortOptionWithHyphenInArg() throws ParseException {
        String[] args = {"-o", "-output.txt"};
        CommandLine cmd = parser.parse(options, args);
        assertEquals("-output.txt", cmd.getOptionValue("o"));
    }

    // --- Test with null args (should throw NullPointerException) ---

    @Test(expected = NullPointerException.class)
    public void testParseNullArgs() throws ParseException {
        parser.parse(options, null);
    }

    // --- Test with null options ---

    @Test(expected = NullPointerException.class)
    public void testParseNullOptions() throws ParseException {
        String[] args = {"-h"};
        parser.parse(null, args);
    }

    // --- Test with null properties ---

    @Test
    public void testParseNullProperties() throws ParseException {
        String[] args = {"-r"};
        CommandLine cmd = parser.parse(options, args, null);
        assertNotNull(cmd);
    }
}