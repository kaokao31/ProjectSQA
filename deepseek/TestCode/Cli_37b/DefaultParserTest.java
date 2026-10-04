package org.apache.commons.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive unit tests for DefaultParser covering edge cases,
 * branches, and potential fault triggers (including Cli-37 style bugs).
 */
public class DefaultParserTest {

    private Options options;
    private DefaultParser parser;

    @Before
    public void setUp() {
        options = new Options();
        options.addOption("a", "alpha", false, "alpha flag");
        options.addOption("b", "beta", true, "beta with arg");
        options.addOption("c", "charlie", false, "charlie flag");
        options.addOption("d", "delta", true, "delta with arg");
        options.addOption("e", false, "epsilon short-only");
        options.addOption("f", "foxtrot", false, "foxtrot flag");
        options.addOption("g", "golf", true, "golf with arg");
        options.addOption("h", true, "hotel short-only with arg");
        options.addOption(Option.builder("i").longOpt("india").argName("val").hasArg().build());
        options.addOption(Option.builder("j").longOpt("juliet").optionalArg(true).argName("val").build());
        parser = new DefaultParser();
    }

    @Test
    public void testParseSimpleFlags() throws ParseException {
        CommandLine cl = parser.parse(options, new String[] {"-a", "-c"});
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("c"));
        assertFalse(cl.hasOption("b"));
        assertEquals(2, cl.getOptions().length);
    }

    @Test
    public void testParseLongOptionWithValue() throws ParseException {
        CommandLine cl = parser.parse(options, new String[] {"--beta=foo"});
        assertTrue(cl.hasOption("b"));
        assertEquals("foo", cl.getOptionValue("b"));
    }

    @Test
    public void testParseShortOptionWithValue() throws ParseException {
        CommandLine cl = parser.parse(options, new String[] {"-b", "bar"});
        assertTrue(cl.hasOption("b"));
        assertEquals("bar", cl.getOptionValue("b"));
    }

    @Test
    public void testParseMultipleValues() throws ParseException {
        CommandLine cl = parser.parse(options, new String[] {"-d", "val1", "-d", "val2"});
        assertTrue(cl.hasOption("d"));
        String[] vals = cl.getOptionValues("d");
        assertNotNull(vals);
        assertEquals(2, vals.length);
        assertEquals("val1", vals[0]);
        assertEquals("val2", vals[1]);
    }

    @Test
    public void testParseOptionalArgPresent() throws ParseException {
        CommandLine cl = parser.parse(options, new String[] {"-j", "optVal"});
        assertTrue(cl.hasOption("j"));
        assertEquals("optVal", cl.getOptionValue("j"));
    }

    @Test
    public void testParseOptionalArgAbsent() throws ParseException {
        CommandLine cl = parser.parse(options, new String[] {"-j"});
        assertTrue(cl.hasOption("j"));
        assertNull(cl.getOptionValue("j"));
    }

    @Test(expected = ParseException.class)
    public void testParseMissingRequiredArg() throws ParseException {
        parser.parse(options, new String[] {"-b"});
    }

    @Test(expected = ParseException.class)
    public void testParseUnknownOption() throws ParseException {
        parser.parse(options, new String[] {"-z"});
    }

    @Test(expected = ParseException.class)
    public void testParseUnknownLongOption() throws ParseException {
        parser.parse(options, new String[] {"--zulu"});
    }

    @Test
    public void testParseDoubleDashStopsOptions() throws ParseException {
        CommandLine cl = parser.parse(options, new String[] {"-a", "--", "-b", "foo"});
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        assertEquals(2, cl.getArgList().size());
        assertEquals("-b", cl.getArgList().get(0));
        assertEquals("foo", cl.getArgList().get(1));
    }

    @Test
    public void testParseWithStopAtNonOption() throws ParseException {
        CommandLine cl = parser.parse(options, new String[] {"-a", "nonOption", "-c"}, true);
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("c"));
        assertEquals(1, cl.getArgList().size());
        assertEquals("nonOption", cl.getArgList().get(0));
    }

    @Test
    public void testParseWithMultipleFlagsCombined() throws ParseException {
        CommandLine cl = parser.parse(options, new String[] {"-ac"});
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("c"));
        assertFalse(cl.hasOption("e"));
    }

    @Test
    public void testParseWithShortOptionRequiringArgFollowedByFlag() throws ParseException {
        CommandLine cl = parser.parse(options, new String[] {"-b", "arg", "-c"});
        assertTrue(cl.hasOption("b"));
        assertEquals("arg", cl.getOptionValue("b"));
        assertTrue(cl.hasOption("c"));
    }

    @Test
    public void testParseWithPropertiesOverride() throws ParseException {
        java.util.Properties props = new java.util.Properties();
        props.setProperty("a", "true");
        CommandLine cl = parser.parse(options, new String[] {"-c"}, props, false);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("c"));
    }

    @Test
    public void testParseLongOptionEqualsEmpty() throws ParseException {
        CommandLine cl = parser.parse(options, new String[] {"--beta="});
        assertTrue(cl.hasOption("b"));
        assertEquals("", cl.getOptionValue("b"));
    }

    @Test
    public void testParseLongOptionEqualsMissing() throws ParseException {
        try {
            parser.parse(options, new String[] {"--beta"});
            fail("Expected ParseException for missing required arg");
        } catch (ParseException e) {
            // expected
        }
    }

    @Test
    public void testParseShortOptionWithoutArgFlagAfter() throws ParseException {
        CommandLine cl = parser.parse(options, new String[] {"-b", "-c"});
        // -b requires an arg, so -c should be treated as the arg for -b? Actually DefaultParser behavior:
        // If option expects an arg and the next token is a valid option, it may treat it as arg.
        // This test covers that branch. Common behavior: -b takes -c as argument (if not starting with -)
        // But since -c starts with -, some parsers treat as option. DefaultParser typically uses the next token 
        // as argument even if it looks like an option. So we expect -b's value to be "-c".
        assertTrue(cl.hasOption("b"));
        assertEquals("-c", cl.getOptionValue("b"));
        assertFalse(cl.hasOption("c"));
    }

    @Test
    public void testParseNegativeNumberAsArgument() throws ParseException {
        // Cli-37 typical case: option expecting argument, argument is a negative number
        options.addOption("n", "number", true, "number option");
        CommandLine cl = parser.parse(options, new String[] {"-n", "-5"});
        assertTrue(cl.hasOption("n"));
        assertEquals("-5", cl.getOptionValue("n"));
    }

    @Test
    public void testParseNegativeNumberAsPositionalAfterDoubleDash() throws ParseException {
        CommandLine cl = parser.parse(options, new String[] {"--", "-5"});
        assertEquals(1, cl.getArgList().size());
        assertEquals("-5", cl.getArgList().get(0));
    }

    @Test
    public void testParseLongOptionWithValueUsingEqualsNegativeNumber() throws ParseException {
        options.addOption("n", "number", true, "number option");
        CommandLine cl = parser.parse(options, new String[] {"--number=-3"});
        assertTrue(cl.hasOption("n"));
        assertEquals("-3", cl.getOptionValue("n"));
    }

    @Test
    public void testParseRequiredOptionMissing() {
        Options reqOpts = new Options();
        reqOpts.addRequiredOption("r", "required", true, "required option");
        DefaultParser parser2 = new DefaultParser();
        try {
            parser2.parse(reqOpts, new String[] {});
            fail("Expected ParseException for missing required option");
        } catch (ParseException e) {
            // expected
        }
    }

    @Test
    public void testParseWithWhitespaceInArgument() throws ParseException {
        CommandLine cl = parser.parse(options, new String[] {"-b", "value with spaces"});
        assertEquals("value with spaces", cl.getOptionValue("b"));
    }

    @Test
    public void testParseNullArguments() throws ParseException {
        CommandLine cl = parser.parse(options, null);
        assertNotNull(cl);
        assertEquals(0, cl.getOptions().length);
    }

    @Test(expected = NullPointerException.class)
    public void testParseNullOptions() throws ParseException {
        parser.parse(null, new String[] {"-a"});
    }

    @Test
    public void testParseEmptyArguments() throws ParseException {
        CommandLine cl = parser.parse(options, new String[] {});
        assertNotNull(cl);
        assertEquals(0, cl.getOptions().length);
    }

    @Test
    public void testParsePartialMatchingOption() throws ParseException {
        // Long option matching should be full, not partial
        options.addOption("alpha2", false, "another alpha");
        CommandLine cl = parser.parse(options, new String[] {"--alpha"});
        assertTrue(cl.hasOption("alpha"));
        assertFalse(cl.hasOption("alpha2"));
    }

    @Test
    public void testParseOptionGroup() throws ParseException {
        OptionGroup group = new OptionGroup();
        group.addOption(Option.builder("x").build());
        group.addOption(Option.builder("y").build());
        group.setRequired(true);
        options.addOptionGroup(group);

        CommandLine cl = parser.parse(options, new String[] {"-x"});
        assertTrue(cl.hasOption("x"));
        assertFalse(cl.hasOption("y"));
    }

    @Test(expected = ParseException.class)
    public void testParseConflictOptionGroup() throws ParseException {
        OptionGroup group = new OptionGroup();
        group.addOption(Option.builder("x").build());
        group.addOption(Option.builder("y").build());
        options.addOptionGroup(group);

        parser.parse(options, new String[] {"-x", "-y"});
    }

    @Test
    public void testParsePropertiesAsDefaultValues() throws ParseException {
        java.util.Properties props = new java.util.Properties();
        props.setProperty("b", "propVal");
        CommandLine cl = parser.parse(options, new String[] {"-c"}, props, false);
        assertTrue(cl.hasOption("b"));
        assertEquals("propVal", cl.getOptionValue("b"));
    }

    @Test
    public void testParsePropertiesOverrideWithCommandLine() throws ParseException {
        java.util.Properties props = new java.util.Properties();
        props.setProperty("b", "propVal");
        CommandLine cl = parser.parse(options, new String[] {"-b", "cliVal"}, props, false);
        assertTrue(cl.hasOption("b"));
        assertEquals("cliVal", cl.getOptionValue("b"));
    }

    @Test
    public void testParseMultipleLongOptionsWithEquals() throws ParseException {
        CommandLine cl = parser.parse(options, new String[] {"--alpha", "--beta=test"});
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertEquals("test", cl.getOptionValue("b"));
    }

    @Test
    public void testParseBurstShortOptionsWithArgAtEnd() throws ParseException {
        CommandLine cl = parser.parse(options, new String[] {"-ae", "-h", "hotelVal"});
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("e"));
        assertTrue(cl.hasOption("h"));
        assertEquals("hotelVal", cl.getOptionValue("h"));
    }

    @Test
    public void testParseBurstShortOptionsWithArgImmediately() throws ParseException {
        // -h takes arg, so -h hotelVal is separate; but when burst, -h should consume next token
        CommandLine cl = parser.parse(options, new String[] {"-ah", "argForH"});
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("h"));
        assertEquals("argForH", cl.getOptionValue("h"));
    }

    @Test(expected = ParseException.class)
    public void testParseBurstShortOptionsMissingArg() throws ParseException {
        // -h requires arg, but token ends after e, so missing arg
        parser.parse(options, new String[] {"-aeh"});
    }

    @Test
    public void testParseArgAfterOptions() throws ParseException {
        CommandLine cl = parser.parse(options, new String[] {"-a", "arg1", "arg2"});
        assertTrue(cl.hasOption("a"));
        assertEquals(2, cl.getArgList().size());
        assertEquals("arg1", cl.getArgList().get(0));
        assertEquals("arg2", cl.getArgList().get(1));
    }

    @Test
    public void testParseStandaloneDoubleDash() throws ParseException {
        CommandLine cl = parser.parse(options, new String[] {"--"});
        assertEquals(0, cl.getOptions().length);
        assertEquals(0, cl.getArgList().size());
    }
}