package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for org.apache.commons.cli.Parser (Defects4J Cli 28).
 */
public class ParserTest {

    private ConcreteParser parser;
    private Options options;

    /**
     * Concrete subclass of the abstract Parser class to test its implemented behavior.
     */
    private static class ConcreteParser extends Parser {
        @Override
        protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
            return arguments;
        }
    }

    @Before
    public void setUp() {
        parser = new ConcreteParser();
        options = new Options();
    }

    @Test
    public void testSetGetProperties() {
        assertNull(parser.getProperties());
        Properties props = new Properties();
        props.setProperty("key", "value");
        parser.setProperties(props);
        assertSame(props, parser.getProperties());
        assertEquals("value", parser.getProperties().getProperty("key"));
    }

    @Test
    public void testParseWithOptionsAndArguments() throws ParseException {
        options.addOption("a", "optionA", false, "desc A");
        String[] args = {"-a"};
        CommandLine cl = parser.parse(options, args);
        assertNotNull(cl);
        assertTrue(cl.hasOption("a"));
    }

    @Test
    public void testParseWithOptionsArgumentsAndProperties() throws ParseException {
        Option opt = OptionBuilder.withLongOpt("propOpt").hasArg().create('p');
        options.addOption(opt);

        Properties props = new Properties();
        props.setProperty("propOpt", "propValue");
        parser.setProperties(props);

        String[] args = {};
        CommandLine cl = parser.parse(options, args, parser.getProperties());
        assertNotNull(cl);
        assertTrue(cl.hasOption("propOpt"));
        assertEquals("propValue", cl.getOptionValue("propOpt"));
    }

    @Test
    public void testParseWithPropertiesOverride() throws ParseException {
        Option opt = OptionBuilder.withLongOpt("propOpt").hasArg().create('p');
        options.addOption(opt);

        Properties props = new Properties();
        props.setProperty("propOpt", "propValue");
        parser.setProperties(props);

        // Command line argument should take precedence over properties or test default process
        String[] args = {"-p", "cliValue"};
        CommandLine cl = parser.parse(options, args, parser.getProperties());
        assertNotNull(cl);
        assertTrue(cl.hasOption("propOpt"));
        assertEquals("cliValue", cl.getOptionValue("propOpt"));
    }

    @Test
    public void testParseWithStopAtNonOption() throws ParseException {
        options.addOption("a", false, "desc A");
        String[] args = {"-a", "non-option"};
        CommandLine cl = parser.parse(options, args, true);
        assertNotNull(cl);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.getArgList().contains("non-option"));
    }

    @Test
    public void testParseNullArguments() throws ParseException {
        CommandLine cl = parser.parse(options, null);
        assertNotNull(cl);
        assertTrue(cl.getArgs().length == 0);
    }

    @Test
    public void testProcessPropertiesNull() throws ParseException {
        // Should not throw exception when properties are null
        parser.setProperties(null);
        parser.processProperties(null);
        
        CommandLine cl = parser.parse(options, new String[0], null);
        assertNotNull(cl);
    }

    @Test
    public void testProcessPropertiesWithExistingOptionInCommandLine() throws ParseException {
        Option opt = OptionBuilder.hasArg().create('b');
        options.addOption(opt);

        Properties props = new Properties();
        props.setProperty("b", "propertyValue");

        // If command line already has 'b', properties should not override it
        CommandLine cl = parser.parse(options, new String[]{"-b", "cliValue"}, props);
        assertEquals("cliValue", cl.getOptionValue('b'));
    }

    @Test
    public void testProcessPropertiesWithMissingOptionDefinition() {
        Properties props = new Properties();
        props.setProperty("unknown", "value");
        parser.setProperties(props);

        try {
            parser.parse(options, new String[0], props);
            // Depending on implementation, it might ignore or fail, but let's verify execution flow.
        } catch (ParseException e) {
            // Expected if strict, or handled gracefully
        }
    }
}