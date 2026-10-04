package org.apache.commons.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

/**
 * Comprehensive JUnit 4 test suite for GnuParser targeting high coverage
 * and the known Defects4J Cli-12 bug (options with optional arguments).
 */
public class GnuParserTest {

    private GnuParser parser;
    private Options options;
    private Option verbose;
    private Option debug;
    private Option help;
    private Option output;
    private Option input;
    private Option config;

    @Before
    public void setUp() {
        parser = new GnuParser();

        verbose = Option.builder("v")
                .longOpt("verbose")
                .desc("enable verbose output")
                .build();
        debug = Option.builder("d")
                .longOpt("debug")
                .desc("enable debug mode")
                .build();
        help = Option.builder("h")
                .longOpt("help")
                .desc("print help message")
                .build();

        // Option with a required argument
        output = Option.builder("o")
                .longOpt("output")
                .hasArg()
                .argName("file")
                .desc("output file")
                .build();

        // Option with an optional argument
        input = Option.builder("i")
                .longOpt("input")
                .optionalArg(true)
                .hasArg()
                .argName("file")
                .desc("input file (optional)")
                .build();

        // Option with an optional argument (long form)
        config = Option.builder("c")
                .longOpt("config")
                .optionalArg(true)
                .hasArg()
                .argName("path")
                .desc("config file (optional)")
                .build();

        options = new Options();
        options.addOption(verbose);
        options.addOption(debug);
        options.addOption(help);
        options.addOption(output);
        options.addOption(input);
        options.addOption(config);
    }

    // ---------- Basic Parsing ----------

    @Test
    public void testShortOptionNoArg() throws ParseException {
        CommandLine cl = parser.parse(options, new String[]{"-v"});
        assertTrue(cl.hasOption("v"));
        assertFalse(cl.hasOption("d"));
    }

    @Test
    public void testMultipleShortOptions() throws ParseException {
        CommandLine cl = parser.parse(options, new String[]{"-v", "-d", "-h"});
        assertTrue(cl.hasOption("v"));
        assertTrue(cl.hasOption("d"));
        assertTrue(cl.hasOption("h"));
    }

    @Test
    public void testShortOptionsCombined() throws ParseException {
        CommandLine cl = parser.parse(options, new String[]{"-vdh"});
        assertTrue(cl.hasOption("v"));
        assertTrue(cl.hasOption("d"));
        assertTrue(cl.hasOption("h"));
    }

    @Test
    public void testLongOptionNoArg() throws ParseException {
        CommandLine cl = parser.parse(options, new String[]{"--verbose"});
        assertTrue(cl.hasOption("verbose"));
    }

    // ---------- Options with Arguments ----------

    @Test
    public void testShortOptionWithArg() throws ParseException {
        CommandLine cl = parser.parse(options, new String[]{"-o", "out.txt"});
        assertEquals("out.txt", cl.getOptionValue("o"));
    }

    @Test
    public void testShortOptionWithArgEquals() throws ParseException {
        CommandLine cl = parser.parse(options, new String[]{"-o=out.txt"});
        assertEquals("out.txt", cl.getOptionValue("o"));
    }

    @Test
    public void testLongOptionWithArg() throws ParseException {
        CommandLine cl = parser.parse(options, new String[]{"--output", "out.txt"});
        assertEquals("out.txt", cl.getOptionValue("output"));
    }

    @Test
    public void testLongOptionWithArgEquals() throws ParseException {
        CommandLine cl = parser.parse(options, new String[]{"--output=out.txt"});
        assertEquals("out.txt", cl.getOptionValue("output"));
    }

    // ---------- Optional Argument Bug (Defects4J Cli-12) ----------

    @Test
    public void testOptionalArgConsumesNextOption() throws ParseException {
        // GNU behavior: optional arg should not consume a following option
        // Cli-12 bug: GnuParser incorrectly consumed -d as argument of -i
        CommandLine cl = parser.parse(options, new String[]{"-i", "-d"});
        assertTrue("Option -i should be present", cl.hasOption("i"));
        assertTrue("Option -d should be present", cl.hasOption("d"));
        assertNull("Optional argument for -i should be null", cl.getOptionValue("i"));
    }

    @Test
    public void testOptionalArgWithActualValue() throws ParseException {
        CommandLine cl = parser.parse(options, new String[]{"-i", "file.txt"});
        assertEquals("file.txt", cl.getOptionValue("i"));
    }

    @Test
    public void testOptionalArgLongOptionConsumesNextOption() throws ParseException {
        // Cli-12: long form with optional arg
        CommandLine cl = parser.parse(options, new String[]{"--input", "--debug"});
        assertTrue("Option --input should be present", cl.hasOption("input"));
        assertTrue("Option --debug should be present", cl.hasOption("debug"));
        assertNull("Optional argument for --input should be null", cl.getOptionValue("input"));
    }

    @Test
    public void testOptionalArgLongOptionWithEqualsNoValue() throws ParseException {
        CommandLine cl = parser.parse(options, new String[]{"--config=", "--help"});
        assertTrue("Option --config should be present", cl.hasOption("config"));
        assertEquals("Value for --config should be empty string", "", cl.getOptionValue("config"));
        assertTrue("Option --help should be present", cl.hasOption("help"));
    }

    @Test
    public void testOptionalArgLongOptionWithValueAfterEquals() throws ParseException {
        CommandLine cl = parser.parse(options, new String[]{"--config=/path/to/config"});
        assertEquals("/path/to/config", cl.getOptionValue("config"));
    }

    // ---------- Ambiguous / Unknown / Missing Args ----------

    @Test(expected = UnrecognizedOptionException.class)
    public void testUnknownShortOption() throws ParseException {
        parser.parse(options, new String[]{"-x"});
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testUnknownLongOption() throws ParseException {
        parser.parse(options, new String[]{"--unknown"});
    }

    @Test(expected = MissingArgumentException.class)
    public void testMissingRequiredArgument() throws ParseException {
        parser.parse(options, new String[]{"-o"});
    }

    @Test(expected = MissingArgumentException.class)
    public void testMissingRequiredArgumentLongOpt() throws ParseException {
        parser.parse(options, new String[]{"--output"});
    }

    // ---------- Stop at non-option (--) ----------

    @Test
    public void testStopAtNonOption() throws ParseException {
        String[] args = {"-v", "--", "-d", "foo"};
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("v"));
        assertFalse(cl.hasOption("d"));
        assertEquals(2, cl.getArgList().size());
        assertEquals("-d", cl.getArgs()[0]);
        assertEquals("foo", cl.getArgs()[1]);
    }

    @Test
    public void testStopAtNonOptionWithOptionalArg() throws ParseException {
        // Ensure that -- terminates option processing even for optional args
        String[] args = {"-i", "--", "bar"};
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("i"));
        assertNull(cl.getOptionValue("i"));
        assertEquals(1, cl.getArgList().size());
        assertEquals("bar", cl.getArgs()[0]);
    }

    // ---------- Properties ----------

    @Test
    public void testWithProperties() throws ParseException {
        Properties props = new Properties();
        props.setProperty("verbose", "true");
        CommandLine cl = parser.parse(options, new String[0], props);
        assertTrue(cl.hasOption("verbose"));
    }

    @Test
    public void testWithPropertiesOverride() throws ParseException {
        Properties props = new Properties();
        props.setProperty("output", "default.txt");
        CommandLine cl = parser.parse(options, new String[]{"-o", "override.txt"}, props);
        assertEquals("override.txt", cl.getOptionValue("output"));
    }

    // ---------- Mixed short/long, order, multiple ----------

    @Test
    public void testMixedOptions() throws ParseException {
        CommandLine cl = parser.parse(options, new String[]{"-v", "--output=out.txt", "-d"});
        assertTrue(cl.hasOption("v"));
        assertTrue(cl.hasOption("d"));
        assertTrue(cl.hasOption("output"));
        assertEquals("out.txt", cl.getOptionValue("output"));
    }

    @Test
    public void testMultipleValuesForSameOption() throws ParseException {
        Option multi = Option.builder("m")
                .longOpt("multi")
                .hasArgs()
                .valueSeparator(',')
                .build();
        options.addOption(multi);
        CommandLine cl = parser.parse(options, new String[]{"-m", "a,b,c"});
        assertTrue(cl.hasOption("m"));
        String[] values = cl.getOptionValues("m");
        assertEquals(3, values.length);
        assertEquals("a", values[0]);
        assertEquals("b", values[1]);
        assertEquals("c", values[2]);
    }

    // ---------- Edge Cases: Empty args, null, etc. ----------

    @Test
    public void testEmptyArgs() throws ParseException {
        CommandLine cl = parser.parse(options, new String[0]);
        assertFalse(cl.hasOption("v"));
        assertTrue(cl.getArgList().isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void testNullArgs() throws ParseException {
        parser.parse(options, null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullOptions() throws ParseException {
        parser.parse(null, new String[]{"-v"});
    }

    // ---------- Bug specific: optional arg followed by another option with optional arg ----------

    @Test
    public void testOptionalArgFollowedByAnotherOptionalArg() throws ParseException {
        // Two options with optional args: -i (optional) and -c (optional)
        // Neither should consume the other as argument
        CommandLine cl = parser.parse(options, new String[]{"-i", "-c"});
        assertTrue(cl.hasOption("i"));
        assertTrue(cl.hasOption("c"));
        assertNull(cl.getOptionValue("i"));
        assertNull(cl.getOptionValue("c"));
    }

    @Test
    public void testOptionalArgWithValueFollowedByAnotherOption() throws ParseException {
        CommandLine cl = parser.parse(options, new String[]{"-i", "myfile", "-d"});
        assertTrue(cl.hasOption("i"));
        assertEquals("myfile", cl.getOptionValue("i"));
        assertTrue(cl.hasOption("d"));
    }

    // ---------- Long option with optional arg and equals sign ----------

    @Test
    public void testLongOptionalArgWithEqualsNoValueThenAnotherLong() throws ParseException {
        CommandLine cl = parser.parse(options, new String[]{"--config=", "--verbose"});
        assertTrue(cl.hasOption("config"));
        assertEquals("", cl.getOptionValue("config"));
        assertTrue(cl.hasOption("verbose"));
    }

    @Test
    public void testLongOptionalArgEqualsValueNoSpace() throws ParseException {
        CommandLine cl = parser.parse(options, new String[]{"--config=/path"});
        assertEquals("/path", cl.getOptionValue("config"));
    }

    // ---------- Test that required options are present (negative) ----------

    @Test(expected = MissingOptionException.class)
    public void testMissingRequiredOption() throws ParseException {
        Options reqOptions = new Options();
        reqOptions.addOption(Option.builder("r").required().hasArg().build());
        parser.parse(reqOptions, new String[]{"-v"}); // no -r
    }

    // ---------- Test unrecognized option in combined short ----------

    @Test(expected = UnrecognizedOptionException.class)
    public void testCombinedShortWithUnknown() throws ParseException {
        parser.parse(options, new String[]{"-vx"}); // -x is unknown
    }

    // ---------- Test option with long prefix only ----------

    @Test
    public void testLongOptionAmbiguousPrefix() throws ParseException {
        // Should not happen with GnuParser; it requires exact match? Actually, it matches prefix if unique.
        // We have verbose and help, both starting with '--h'? Not exactly. Add a unique prefix test.
        // Since we have --input and --output, they are not ambiguous.
        CommandLine cl = parser.parse(options, new String[]{"--in"});
        assertTrue(cl.hasOption("input"));
    }

    // ---------- Negative: test that non-option arguments are collected ----------

    @Test
    public void testNonOptionArgs() throws ParseException {
        CommandLine cl = parser.parse(options, new String[]{"arg1", "arg2"});
        assertEquals(2, cl.getArgList().size());
        assertEquals("arg1", cl.getArgs()[0]);
        assertEquals("arg2", cl.getArgs()[1]);
    }

    // ---------- Test properties with optional args ----------

    @Test
    public void testPropertiesOptionalArgOverride() throws ParseException {
        Properties props = new Properties();
        props.setProperty("input", "propFile");
        CommandLine cl = parser.parse(options, new String[]{"-i", "cmdFile"}, props);
        assertEquals("cmdFile", cl.getOptionValue("input"));
    }

    @Test
    public void testPropertiesOptionalArgNoCmdLine() throws ParseException {
        Properties props = new Properties();
        props.setProperty("input", "propFile");
        CommandLine cl = parser.parse(options, new String[0], props);
        assertEquals("propFile", cl.getOptionValue("input"));
    }

    // ---------- Test that long option with optional arg does not consume next long option ----------

    @Test
    public void testOptionalLongArgDoesNotConsumeNextLongOpt() throws ParseException {
        CommandLine cl = parser.parse(options, new String[]{"--config", "--output=file"});
        assertTrue(cl.hasOption("config"));
        assertNull(cl.getOptionValue("config"));
        assertTrue(cl.hasOption("output"));
        assertEquals("file", cl.getOptionValue("output"));
    }

    // ---------- Test large number of arguments ----------

    @Test
    public void testManyArguments() throws ParseException {
        String[] args = new String[100];
        for (int i = 0; i < 50; i++) {
            args[2*i] = "-v";
            args[2*i+1] = "-d";
        }
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("v"));
        assertTrue(cl.hasOption("d"));
    }
}