package org.apache.commons.cli;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.util.Properties;

public class ParserTest {
    private Parser parser;
    private Options options;

    @Before
    public void setUp() {
        parser = new GnuParser();
        options = new Options();
    }

    @Test
    public void testSimpleRequiredOption() throws ParseException {
        Option opt = OptionBuilder.hasArg().create("a");
        opt.setRequired(true);
        options.addOption(opt);
        String[] args = {"-a", "value"};
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertEquals("value", cl.getOptionValue("a"));
    }

    @Test(expected = MissingOptionException.class)
    public void testMissingRequiredOption() throws ParseException {
        Option opt = OptionBuilder.hasArg().create("a");
        opt.setRequired(true);
        options.addOption(opt);
        String[] args = {};
        parser.parse(options, args);
    }

    @Test
    public void testOptionalOption() throws ParseException {
        options.addOption("a", "arg");
        String[] args = {};
        CommandLine cl = parser.parse(options, args);
        assertFalse(cl.hasOption("a"));
    }

    @Test
    public void testRequiredOptionGroupSatisfied() throws ParseException {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(OptionBuilder.hasArg().create("a"));
        group.addOption(OptionBuilder.create("b"));
        options.addOptionGroup(group);

        String[] args = {"-a", "value"};
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
    }

    @Test(expected = MissingOptionException.class)
    public void testRequiredOptionGroupNotSatisfied() throws ParseException {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(OptionBuilder.hasArg().create("a"));
        group.addOption(OptionBuilder.create("b"));
        options.addOptionGroup(group);

        String[] args = {};
        parser.parse(options, args);
    }

    @Test
    public void testRequiredGroupWithArgOptionAndSeparateRequiredOption() throws ParseException {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(OptionBuilder.hasArg().create("a"));
        group.addOption(OptionBuilder.create("b"));
        options.addOptionGroup(group);
        options.addOption(OptionBuilder.isRequired().create("c"));

        String[] args = {"-a", "value", "-c", "value"};
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("c"));
    }

    @Test(expected = MissingOptionException.class)
    public void testRequiredGroupWithArgOptionSatisfiedButOtherRequiredMissing() throws ParseException {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(OptionBuilder.hasArg().create("a"));
        group.addOption(OptionBuilder.create("b"));
        options.addOptionGroup(group);
        options.addOption(OptionBuilder.isRequired().create("c"));

        String[] args = {"-a", "value"};
        parser.parse(options, args);
    }

    @Test
    public void testParseWithProperties() throws ParseException {
        options.addOption("a", "arg");
        Properties props = new Properties();
        props.setProperty("a", "propValue");
        String[] args = {};
        CommandLine cl = parser.parse(options, args, props);
        assertEquals("propValue", cl.getOptionValue("a"));
    }

    @Test
    public void testStopAtNonOption() throws ParseException {
        options.addOption("a", false, "flag");
        String[] args = {"-a", "nonOption", "-b"};
        CommandLine cl = parser.parse(options, args, true);
        assertTrue(cl.hasOption("a"));
        assertEquals(1, cl.getArgList().size());
    }

    @Test
    public void testParseWithLongOption() throws ParseException {
        options.addOption("long", "a-long-option", false, "long opt");
        String[] args = {"--a-long-option"};
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("long"));
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testUnrecognizedOption() throws ParseException {
        options.addOption("a", "arg");
        String[] args = {"-b"};
        parser.parse(options, args);
    }

    @Test(expected = AlreadySelectedException.class)
    public void testConflictingOptionsInGroup() throws ParseException {
        OptionGroup group = new OptionGroup();
        group.addOption(OptionBuilder.create("a"));
        group.addOption(OptionBuilder.create("b"));
        options.addOptionGroup(group);
        String[] args = {"-a", "-b"};
        parser.parse(options, args);
    }

    @Test(expected = NullPointerException.class)
    public void testNullArgs() throws ParseException {
        parser.parse(options, (String[]) null);
    }

    @Test
    public void testEmptyArgs() throws ParseException {
        CommandLine cl = parser.parse(options, new String[0]);
        assertNotNull(cl);
    }
}