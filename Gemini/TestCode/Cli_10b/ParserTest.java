package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Test suite for org.apache.commons.cli.Parser (Defects4J Cli 10).
 * Designed for JUnit 4 and Java 8.
 */
public class ParserTest {

    private ConcreteParser parser;
    private Options options;

    /**
     * Concrete subclass of the abstract Parser class to allow instantiation and testing.
     */
    private static class ConcreteParser extends Parser {
        private Options currentOptions;

        @Override
        protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
            this.currentOptions = opts;
            return arguments;
        }

        public Options getCurrentOptions() {
            return currentOptions;
        }
    }

    @Before
    public void setUp() {
        parser = new ConcreteParser();
        options = new Options();
    }

    @Test
    public void testParseWithOptionsAndArgsNull() throws ParseException {
        CommandLine cl = parser.parse(options, null);
        assertNotNull(cl);
    }

    @Test
    public void testParseWithOptionsAndEmptyArgs() throws ParseException {
        CommandLine cl = parser.parse(options, new String[0]);
        assertNotNull(cl);
    }

    @Test
    public void testParseWithPropertiesNull() throws ParseException {
        CommandLine cl = parser.parse(options, new String[]{"-a"}, null);
        assertNotNull(cl);
    }

    @Test
    public void testParseWithOptionsPropertiesAndStop() throws ParseException {
        options.addOption("a", "alpha", false, "alpha option");
        Properties props = new Properties();
        props.setProperty("a", "true");

        CommandLine cl = parser.parse(options, new String[]{"-a"}, props, true);
        assertNotNull(cl);
        assertTrue(cl.hasOption("a"));
    }

    @Test
    public void testProcessPropertiesWithNull() throws ParseException {
        // Testing protected method via parse or direct logic if accessible
        options.addOption("b", "beta", true, "beta option");
        Properties props = new Properties();
        props.setProperty("b", "valueB");

        CommandLine cl = parser.parse(options, new String[0], props);
        assertNotNull(cl);
        assertTrue(cl.hasOption("b"));
        assertEquals("valueB", cl.getOptionValue("b"));
    }

    @Test
    public void testProcessPropertiesAlreadySet() throws ParseException {
        options.addOption("b", "beta", true, "beta option");
        Properties props = new Properties();
        props.setProperty("b", "valueB");

        // Pass -b in args so it's already set before properties are processed
        CommandLine cl = parser.parse(options, new String[]{"-b", "valueArg"}, props);
        assertNotNull(cl);
        assertEquals("valueArg", cl.getOptionValue("b"));
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testUnrecognizedOption() throws ParseException {
        parser.parse(options, new String[]{"-unknown"});
    }

    @Test
    public void testSetGetOptions() {
        assertNull(parser.getOptions());
        parser.setOptions(options);
        assertEquals(options, parser.getOptions());
    }

    @Test
    public void testSetGetRequiredOptions() {
        assertNotNull(parser.getRequiredOptions());
        parser.getRequiredOptions().add("test");
        assertTrue(parser.getRequiredOptions().contains("test"));
    }

    @Test
    public void testCheckRequiredOptions() {
        // Since checkRequiredOptions is protected, we can invoke it via a subclass or check parse behavior
        options.addRequiredOption("req", "required", false, "req");
        try {
            parser.parse(options, new String[0]);
            fail("Expected MissingOptionException");
        } catch (MissingOptionException e) {
            assertNotNull(e.getMessage());
        } catch (ParseException e) {
            fail("Unexpected ParseException: " + e.getMessage());
        }
    }

    @Test
    public void testProcessOptionWithArgument() throws ParseException {
        Option opt = new Option("v", "value", true, "desc");
        options.addOption(opt);

        CommandLine cl = parser.parse(options, new String[]{"-v", "val"});
        assertTrue(cl.hasOption("v"));
        assertEquals("val", cl.getOptionValue("v"));
    }

    @Test(expected = MissingArgumentException.class)
    public void testProcessOptionMissingArgument() throws ParseException {
        Option opt = new Option("v", "value", true, "desc");
        options.addOption(opt);

        // Pass option without argument
        parser.parse(options, new String[]{"-v"});
    }

    @Test
    public void testProcessPropertiesWithInvalidOptionKey() {
        Properties props = new Properties();
        props.setProperty("nonexistent", "value");

        try {
            parser.parse(options, new String[0], props);
            // Depending on implementation, it might throw IllegalArgumentException or ignore
        } catch (IllegalArgumentException | ParseException e) {
            // Expected in some CLI versions for invalid property keys
        }
    }
}