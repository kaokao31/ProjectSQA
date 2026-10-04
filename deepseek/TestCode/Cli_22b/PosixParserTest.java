package org.apache.commons.cli;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

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
    public void testSimpleShortOption() throws ParseException {
        options.addOption("a", false, "desc");
        CommandLine cl = parser.parse(options, new String[]{"-a"});
        assertTrue(cl.hasOption("a"));
        assertEquals(0, cl.getArgs().length);
    }

    @Test
    public void testShortOptionWithSpaceArgument() throws ParseException {
        options.addOption("a", true, "desc");
        CommandLine cl = parser.parse(options, new String[]{"-a", "value"});
        assertTrue(cl.hasOption("a"));
        assertEquals("value", cl.getOptionValue("a"));
        assertEquals(0, cl.getArgs().length);
    }

    @Test
    public void testShortOptionWithAttachedArgument() throws ParseException {
        options.addOption("a", true, "desc");
        CommandLine cl = parser.parse(options, new String[]{"-avalue"});
        assertTrue(cl.hasOption("a"));
        assertEquals("value", cl.getOptionValue("a"));
        assertEquals(0, cl.getArgs().length);
    }

    @Test
    public void testShortOptionWithoutArgumentButArgumentGiven() {
        options.addOption("a", false, "desc");
        try {
            parser.parse(options, new String[]{"-a", "value"});
            fail("Expected UnrecognizedOptionException or similar");
        } catch (ParseException e) {
            // expected
        }
    }

    // ========== Long options ==========

    @Test
    public void testSimpleLongOption() throws ParseException {
        options.addOption("long", false, "desc");
        CommandLine cl = parser.parse(options, new String[]{"--long"});
        assertTrue(cl.hasOption("long"));
        assertEquals(0, cl.getArgs().length);
    }

    @Test
    public void testLongOptionWithSpaceArgument() throws ParseException {
        options.addOption("long", true, "desc");
        CommandLine cl = parser.parse(options, new String[]{"--long", "value"});
        assertTrue(cl.hasOption("long"));
        assertEquals("value", cl.getOptionValue("long"));
        assertEquals(0, cl.getArgs().length);
    }

    @Test
    public void testLongOptionWithEqualsArgument() throws ParseException {
        options.addOption("long", true, "desc");
        CommandLine cl = parser.parse(options, new String[]{"--long=value"});
        assertTrue(cl.hasOption("long"));
        assertEquals("value", cl.getOptionValue("long"));
        assertEquals(0, cl.getArgs().length);
    }

    @Test
    public void testLongOptionWithOptionalArgumentNoArg() throws ParseException {
        Option opt = Option.builder("long").optionalArg(true).hasArg(true).build();
        options.addOption(opt);
        CommandLine cl = parser.parse(options, new String[]{"--long"});
        assertTrue(cl.hasOption("long"));
        assertNull(cl.getOptionValue("long"));
    }

    @Test
    public void testLongOptionWithOptionalArgumentEquals() throws ParseException {
        Option opt = Option.builder("long").optionalArg(true).hasArg(true).build();
        options.addOption(opt);
        CommandLine cl = parser.parse(options, new String[]{"--long=value"});
        assertTrue(cl.hasOption("long"));
        assertEquals("value", cl.getOptionValue("long"));
    }

    @Test
    public void testLongOptionWithoutArgumentButArgumentGiven() {
        options.addOption("long", false, "desc");
        try {
            parser.parse(options, new String[]{"--long=value"});
            fail("Expected UnrecognizedOptionException");
        } catch (UnrecognizedOptionException e) {
            // expected
        } catch (ParseException e) {
            // also acceptable
        }
    }

    // ========== Combined short options ==========

    @Test
    public void testCombinedShortOptions() throws ParseException {
        options.addOption("a", false, "desc");
        options.addOption("b", false, "desc");
        options.addOption("c", false, "desc");
        CommandLine cl = parser.parse(options, new String[]{"-abc"});
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertTrue(cl.hasOption("c"));
    }

    @Test
    public void testCombinedShortOptionsWithArgument() throws ParseException {
        options.addOption("a", false, "desc");
        options.addOption("b", true, "desc");
        options.addOption("c", false, "desc");
        CommandLine cl = parser.parse(options, new String[]{"-abc", "value"});
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertEquals("value", cl.getOptionValue("b"));
        assertTrue(cl.hasOption("c"));
        assertEquals(0, cl.getArgs().length);
    }

    @Test
    public void testCombinedShortOptionsWithAttachedArgument() throws ParseException {
        options.addOption("a", false, "desc");
        options.addOption("b", true, "desc");
        CommandLine cl = parser.parse(options, new String[]{"-abvalue"});
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertEquals("value", cl.getOptionValue("b"));
    }

    // ========== Double dash (--) ==========

    @Test
    public void testDoubleDashStopsParsing() throws ParseException {
        options.addOption("a", false, "desc");
        CommandLine cl = parser.parse(options, new String[]{"-a", "--", "-b", "foo"});
        assertTrue(cl.hasOption("a"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("-b", cl.getArgs()[0]);
        assertEquals("foo", cl.getArgs()[1]);
    }

    // ========== Negative numbers ==========

    @Test
    public void testNegativeNumberNotTreatedAsOption() throws ParseException {
        options.addOption("a", false, "desc");
        CommandLine cl = parser.parse(options, new String[]{"-a", "-1"});
        assertTrue(cl.hasOption("a"));
        assertEquals(1, cl.getArgs().length);
        assertEquals("-1", cl.getArgs()[0]);
    }

    @Test
    public void testNegativeNumberAsArgument() throws ParseException {
        options.addOption("n", true, "desc");
        CommandLine cl = parser.parse(options, new String[]{"-n", "-5"});
        assertTrue(cl.hasOption("n"));
        assertEquals("-5", cl.getOptionValue("n"));
        assertEquals(0, cl.getArgs().length);
    }

    // ========== Unknown options ==========

    @Test(expected = UnrecognizedOptionException.class)
    public void testUnknownShortOption() throws ParseException {
        parser.parse(options, new String[]{"-x"});
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testUnknownLongOption() throws ParseException {
        parser.parse(options, new String[]{"--unknown"});
    }

    // ========== Missing argument ==========

    @Test(expected = MissingArgumentException.class)
    public void testMissingArgumentForRequiredOption() throws ParseException {
        options.addOption("r", true, "desc");
        parser.parse(options, new String[]{"-r"});
    }

    // ========== Empty and null inputs ==========

    @Test
    public void testEmptyArgs() throws ParseException {
        CommandLine cl = parser.parse(options, new String[]{});
        assertFalse(cl.hasOption("a"));
        assertEquals(0, cl.getArgs().length);
    }

    @Test(expected = NullPointerException.class)
    public void testNullArgs() throws ParseException {
        parser.parse(options, null);
    }

    // ========== Multiple options and arguments ==========

    @Test
    public void testMultipleOptionsAndArgs() throws ParseException {
        options.addOption("a", false, "desc");
        options.addOption("b", true, "desc");
        options.addOption("c", false, "desc");
        CommandLine cl = parser.parse(options, new String[]{"-a", "-b", "val", "-c", "extra"});
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertEquals("val", cl.getOptionValue("b"));
        assertTrue(cl.hasOption("c"));
        assertEquals(1, cl.getArgs().length);
        assertEquals("extra", cl.getArgs()[0]);
    }

    // ========== Long option with equals and extra args ==========

    @Test
    public void testLongOptionEqualsWithExtraArgs() throws ParseException {
        options.addOption("opt", true, "desc");
        CommandLine cl = parser.parse(options, new String[]{"--opt=value", "extra"});
        assertTrue(cl.hasOption("opt"));
        assertEquals("value", cl.getOptionValue("opt"));
        assertEquals(1, cl.getArgs().length);
        assertEquals("extra", cl.getArgs()[0]);
    }

    // ========== Option with long name and short name ==========

    @Test
    public void testOptionWithBothNames() throws ParseException {
        options.addOption("a", "alpha", false, "desc");
        CommandLine cl = parser.parse(options, new String[]{"-a"});
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("alpha"));
        cl = parser.parse(options, new String[]{"--alpha"});
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("alpha"));
    }

    // ========== Argument starting with dash after option ==========

    @Test
    public void testArgumentStartingWithDash() throws ParseException {
        options.addOption("a", true, "desc");
        CommandLine cl = parser.parse(options, new String[]{"-a", "-b"});
        assertTrue(cl.hasOption("a"));
        assertEquals("-b", cl.getOptionValue("a"));
        assertEquals(0, cl.getArgs().length);
    }

    // ========== Long option with equals and no argument defined ==========

    @Test(expected = UnrecognizedOptionException.class)
    public void testLongOptionEqualsWhenNoArgumentExpected() throws ParseException {
        options.addOption("opt", false, "desc");
        parser.parse(options, new String[]{"--opt=value"});
    }

    // ========== Bug-specific: equals sign handling with required arg ==========

    @Test
    public void testLongOptionEqualsWithRequiredArg() throws ParseException {
        options.addOption("output", true, "output file");
        CommandLine cl = parser.parse(options, new String[]{"--output=file.txt"});
        assertTrue(cl.hasOption("output"));
        assertEquals("file.txt", cl.getOptionValue("output"));
    }

    @Test
    public void testLongOptionEqualsWithOptionalArgAndNoArg() throws ParseException {
        Option opt = Option.builder("opt").optionalArg(true).hasArg(true).build();
        options.addOption(opt);
        CommandLine cl = parser.parse(options, new String[]{"--opt="});
        assertTrue(cl.hasOption("opt"));
        // The value after equals is empty string
        assertEquals("", cl.getOptionValue("opt"));
    }

    // ========== Edge: option with argument and equals sign but no value ==========

    @Test
    public void testLongOptionEqualsEmptyValue() throws ParseException {
        options.addOption("opt", true, "desc");
        CommandLine cl = parser.parse(options, new String[]{"--opt="});
        assertTrue(cl.hasOption("opt"));
        assertEquals("", cl.getOptionValue("opt"));
    }

    // ========== Multiple long options with equals ==========

    @Test
    public void testMultipleLongOptionsWithEquals() throws ParseException {
        options.addOption("a", true, "desc");
        options.addOption("b", true, "desc");
        CommandLine cl = parser.parse(options, new String[]{"--a=1", "--b=2"});
        assertTrue(cl.hasOption("a"));
        assertEquals("1", cl.getOptionValue("a"));
        assertTrue(cl.hasOption("b"));
        assertEquals("2", cl.getOptionValue("b"));
    }

    // ========== Combined short options with long option ==========

    @Test
    public void testMixedShortAndLongOptions() throws ParseException {
        options.addOption("a", false, "desc");
        options.addOption("long", true, "desc");
        CommandLine cl = parser.parse(options, new String[]{"-a", "--long=value"});
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("long"));
        assertEquals("value", cl.getOptionValue("long"));
    }

    // ========== Option with argument and no space before next option ==========

    @Test
    public void testOptionArgumentFollowedByOption() throws ParseException {
        options.addOption("a", true, "desc");
        options.addOption("b", false, "desc");
        CommandLine cl = parser.parse(options, new String[]{"-a", "val", "-b"});
        assertTrue(cl.hasOption("a"));
        assertEquals("val", cl.getOptionValue("a"));
        assertTrue(cl.hasOption("b"));
        assertEquals(0, cl.getArgs().length);
    }

    // ========== Test that parser does not modify original args array ==========

    @Test
    public void testArgsArrayNotModified() throws ParseException {
        options.addOption("a", false, "desc");
        String[] args = new String[]{"-a", "extra"};
        CommandLine cl = parser.parse(options, args);
        assertEquals("-a", args[0]);
        assertEquals("extra", args[1]);
    }
}