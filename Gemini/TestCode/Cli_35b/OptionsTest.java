package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

public class OptionsTest {

    private Options options;

    @Before
    public void setUp() {
        options = new Options();
    }

    @Test
    public void testAddOptionGroup() {
        OptionGroup group = new OptionGroup();
        Option optA = new Option("a", "alpha", false, "alpha option");
        Option optB = new Option("b", "beta", false, "beta option");
        group.addOption(optA);
        group.addOption(optB);

        options.addOptionGroup(group);

        assertTrue(options.hasOptionGroup(optA));
        assertTrue(options.hasOptionGroup(optB));
        assertSame(group, options.getOptionGroup(optA));
        assertSame(group, options.getOptionGroup(optB));

        Collection<OptionGroup> groups = options.getOptionGroups();
        assertTrue(groups.contains(group));
    }

    @Test
    public void testAddOptionGroupWithRequired() {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        Option optA = new Option("a", "alpha", false, "alpha option");
        group.addOption(optA);

        options.addOptionGroup(group);

        assertTrue(options.getOptionGroups().contains(group));
        assertTrue(group.isRequired());
    }

    @Test
    public void testAddOptionStringAndDetails() {
        options.addOption("a", "alpha", true, "alpha desc");

        assertTrue(options.hasOption("a"));
        assertTrue(options.hasOption("alpha"));

        Option opt = options.getOption("a");
        assertNotNull(opt);
        assertEquals("a", opt.getOpt());
        assertEquals("alpha", opt.getLongOpt());
        assertTrue(opt.hasArg());
        assertEquals("alpha desc", opt.getDescription());
    }

    @Test
    public void testAddOptionStringWithoutLongOpt() {
        options.addOption("b", false, "beta desc");

        assertTrue(options.hasOption("b"));
        Option opt = options.getOption("b");
        assertNotNull(opt);
        assertEquals("b", opt.getOpt());
        assertNull(opt.getLongOpt());
        assertFalse(opt.hasArg());
        assertEquals("beta desc", opt.getDescription());
    }

    @Test
    public void testAddOptionObject() {
        Option opt = new Option("c", "charles", true, "charles desc");
        options.addOption(opt);

        assertTrue(options.hasOption("c"));
        assertTrue(options.hasOption("charles"));
        assertSame(opt, options.getOption("c"));
    }

    @Test
    public void testAddDuplicateOption() {
        Option opt1 = new Option("d", "delta", false, "first");
        Option opt2 = new Option("d", "delta", true, "second");

        options.addOption(opt1);
        options.addOption(opt2);

        // Depending on implementation, it might overwrite or keep first.
        // Let's verify behavior.
        Option opt = options.getOption("d");
        assertEquals("second", opt.getDescription());
    }

    @Test
    public void testGetOptions() {
        options.addOption("a", false, "a");
        options.addOption("b", false, "b");

        Collection<Option> allOpts = options.getOptions();
        assertEquals(2, allOpts.size());
    }

    @Test
    public void testGetRequiredOptions() {
        Option opt1 = new Option("a", false, "a");
        opt1.setRequired(true);
        Option opt2 = new Option("b", false, "b");
        opt2.setRequired(false);

        options.addOption(opt1);
        options.addOption(opt2);

        List<?> required = options.getRequiredOptions();
        assertEquals(1, required.size());
        assertTrue(required.contains(opt1));
    }

    @Test
    public void testGetOptionWithLongOptAndHyphens() {
        // Specifically targeting long option retrieval variations (e.g., with or without leading hyphens)
        Option opt = new Option(null, "long-opt", true, "desc");
        options.addOption(opt);

        assertTrue(options.hasOption("long-opt"));
        assertTrue(options.hasOption("--long-opt")); // if handled, or test standard
        assertNotNull(options.getOption("long-opt"));
    }

    @Test
    public void testGetMatchingOptionsExactMatch() {
        options.addOption(new Option(null, "version", false, "version"));
        options.addOption(new Option(null, "verbose", false, "verbose"));

        List<String> matches = options.getMatchingOptions("version");
        assertNotNull(matches);
        assertTrue(matches.contains("version"));
        assertFalse(matches.contains("verbose"));
    }

    @Test
    public void testGetMatchingOptionsPartialMatch() {
        options.addOption(new Option(null, "verbose", false, "verbose"));
        options.addOption(new Option(null, "version", false, "version"));

        List<String> matches = options.getMatchingOptions("verb");
        assertNotNull(matches);
        assertTrue(matches.contains("verbose"));
        assertFalse(matches.contains("version"));
    }

    @Test
    public void testGetMatchingOptionsNoMatch() {
        options.addOption(new Option(null, "help", false, "help"));

        List<String> matches = options.getMatchingOptions("invalid");
        assertNotNull(matches);
        assertTrue(matches.isEmpty());
    }

    @Test
    public void testToString() {
        options.addOption("a", "alpha", false, "alpha desc");
        String str = options.toString();
        assertNotNull(str);
        assertFalse(str.isEmpty());
    }
}