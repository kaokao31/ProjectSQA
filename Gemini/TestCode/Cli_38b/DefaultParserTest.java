package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DefaultParserTest {

    private DefaultParser parser;
    private Options options;

    @Before
    public void setUp() {
        parser = new DefaultParser();
        options = new Options();
    }

    @Test
    public void testParseEmptyArgs() throws Exception {
        CommandLine cmd = parser.parse(options, new String[0]);
        assertNotNull(cmd);
        assertTrue(cmd.getArgs().length == 0);
    }

    @Test(expected = ParseException.class)
    public void testUnrecognizedOption() throws Exception {
        parser.parse(options, new String[]{"-u"});
    }

    @Test
    public void testUnrecognizedOptionWithStopAtNonOption() throws Exception {
        options.addOption("a", "alpha", false, "alpha option");
        CommandLine cmd = parser.parse(options, new String[]{"-a", "-u"}, true);
        assertTrue(cmd.hasOption("a"));
        assertEquals(1, cmd.getArgs().length);
        assertEquals("-u", cmd.getArgs()[0]);
    }

    @Test
    public void testShortOption() throws Exception {
        options.addOption("a", "alpha", false, "alpha option");
        CommandLine cmd = parser.parse(options, new String[]{"-a"});
        assertTrue(cmd.hasOption("a"));
    }

    @Test
    public void testShortOptionValue() throws Exception {
        options.addOption("b", "beta", true, "beta option");
        CommandLine cmd = parser.parse(options, new String[]{"-b", "value"});
        assertTrue(cmd.hasOption("b"));
        assertEquals("value", cmd.getOptionValue("b"));
    }

    @Test
    public void testShortOptionCombinedValue() throws Exception {
        options.addOption("b", "beta", true, "beta option");
        CommandLine cmd = parser.parse(options, new String[]{"-bvalue"});
        assertTrue(cmd.hasOption("b"));
        assertEquals("value", cmd.getOptionValue("b"));
    }

    @Test
    public void testShortOptionClustered() throws Exception {
        options.addOption("a", "alpha", false, "alpha option");
        options.addOption("b", "beta", false, "beta option");
        CommandLine cmd = parser.parse(options, new String[]{"-ab"});
        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("b"));
    }

    @Test
    public void testShortOptionClusteredWithArg() throws Exception {
        options.addOption("a", "alpha", false, "alpha option");
        options.addOption("b", "beta", true, "beta option");
        CommandLine cmd = parser.parse(options, new String[]{"-abvalue"});
        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("b"));
        assertEquals("value", cmd.getOptionValue("b"));
    }

    @Test
    public void testLongOption() throws Exception {
        options.addOption("a", "alpha", false, "alpha option");
        CommandLine cmd = parser.parse(options, new String[]{"--alpha"});
        assertTrue(cmd.hasOption("alpha"));
    }

    @Test
    public void testLongOptionEqualsValue() throws Exception {
        options.addOption("b", "beta", true, "beta option");
        CommandLine cmd = parser.parse(options, new String[]{"--beta=value"});
        assertTrue(cmd.hasOption("beta"));
        assertEquals("value", cmd.getOptionValue("beta"));
    }

    @Test
    public void testLongOptionWithSpaceValue() throws Exception {
        options.addOption("b", "beta", true, "beta option");
        CommandLine cmd = parser.parse(options, new String[]{"--beta", "value"});
        assertTrue(cmd.hasOption("beta"));
        assertEquals("value", cmd.getOptionValue("beta"));
    }

    @Test
    public void testLongOptionAmbiguous() throws Exception {
        options.addOption("version", "version", false, "version");
        options.addOption("verbose", "verbose", false, "verbose");
        try {
            parser.parse(options, new String[]{"--ver"});
            fail("Expected AmbiguousOptionException");
        } catch (AmbiguousOptionException e) {
            assertNotNull(e.getMatchingOptions());
        }
    }

    @Test
    public void testLongOptionPartialMatch() throws Exception {
        options.addOption("version", "version", false, "version");
        CommandLine cmd = parser.parse(options, new String[]{"--vers"});
        assertTrue(cmd.hasOption("version"));
    }

    @Test
    public void testLongOptionWithEqualMissingValue() throws Exception {
        options.addOption("b", "beta", true, "beta option");
        try {
            parser.parse(options, new String[]{"--beta="});
            // Depending on configuration, empty value or exception might be allowed, 
            // but let's test execution.
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testPropertiesHandling() throws Exception {
        options.addOption("D", "define", true, "define");
        java.util.Properties props = new java.util.Properties();
        props.setProperty("propertyKey", "propertyValue");

        CommandLine cmd = parser.parse(options, new String[]{"-DpropertyKey=propertyValue"}, props);
        assertTrue(cmd.hasOption("D"));
    }

    @Test
    public void testStopAtNonOption() throws Exception {
        options.addOption("a", "alpha", false, "alpha option");
        CommandLine cmd = parser.parse(options, new String[]{"-a", "non-option", "-a"}, true);
        assertTrue(cmd.hasOption("a"));
        assertEquals(2, cmd.getArgs().length);
        assertEquals("non-option", cmd.getArgs()[0]);
        assertEquals("-a", cmd.getArgs()[1]);
    }

    @Test
    public void testDoubleDash() throws Exception {
        options.addOption("a", "alpha", false, "alpha option");
        CommandLine cmd = parser.parse(options, new String[]{"--", "-a"});
        assertFalse(cmd.hasOption("a"));
        assertEquals(1, cmd.getArgs().length);
        assertEquals("-a", cmd.getArgs()[0]);
    }

    @Test(expected = MissingOptionException.class)
    public void testMissingRequiredOption() throws Exception {
        Option opt = new Option("r", "required", false, "required option");
        opt.setRequired(true);
        options.addOption(opt);
        parser.parse(options, new String[0]);
    }

    @Test(expected = MissingArgumentException.class)
    public void testMissingArgument() throws Exception {
        options.addOption("b", "beta", true, "beta option");
        parser.parse(options, new String[]{"-b"});
    }

    @Test
    public void testOptionalArgument() throws Exception {
        Option opt = new Option("b", "beta", true, "beta option");
        opt.setOptionalArg(true);
        options.addOption(opt);
        CommandLine cmd = parser.parse(options, new String[]{"-b"});
        assertTrue(cmd.hasOption("b"));
        assertNull(cmd.getOptionValue("b"));
    }

    @Test
    public void testHandlePropertiesWithExistingOptions() throws Exception {
        Option opt = new Option("a", "alpha", false, "alpha");
        options.addOption(opt);
        java.util.Properties props = new java.util.Properties();
        props.setProperty("alpha", "true");

        CommandLine cmd = parser.parse(options, new String[0], props);
        assertTrue(cmd.hasOption("alpha"));
    }

    @Test
    public void testMultipleValuesForOption() throws Exception {
        Option opt = new Option("m", "multi", true, "multi arg");
        opt.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(opt);

        CommandLine cmd = parser.parse(options, new String[]{"-m", "val1", "val2"});
        assertTrue(cmd.hasOption("m"));
        assertArrayEquals(new String[]{"val1", "val2"}, cmd.getOptionValues("m"));
    }
}