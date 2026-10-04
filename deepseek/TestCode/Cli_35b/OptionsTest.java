package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class OptionsTest {

    private Options options;

    @Before
    public void setUp() {
        options = new Options();
    }

    @Test
    public void testAddOptionShort() {
        options.addOption("a", "alpha", false, "Alpha option");
        assertTrue(options.hasOption("a"));
        assertTrue(options.hasOption("alpha"));
        Option opt = options.getOption("a");
        assertNotNull(opt);
        assertEquals("a", opt.getOpt());
        assertEquals("alpha", opt.getLongOpt());
        assertFalse(opt.hasArg());
        assertEquals("Alpha option", opt.getDescription());
    }

    @Test
    public void testAddOptionShortOnly() {
        options.addOption("b", false, "Beta option");
        assertTrue(options.hasOption("b"));
        Option opt = options.getOption("b");
        assertNotNull(opt);
        assertEquals("b", opt.getOpt());
        assertNull(opt.getLongOpt());
    }

    @Test
    public void testAddOptionWithArg() {
        options.addOption("c", "char", true, "Char option");
        assertTrue(options.hasOption("c"));
        Option opt = options.getOption("char");
        assertTrue(opt.hasArg());
        assertEquals("c", opt.getOpt());
        assertEquals("char", opt.getLongOpt());
    }

    @Test
    public void testAddOptionWithNullShortOpt() {
        // Option with only long name
        options.addOption(null, "longname", false, "Long only");
        assertTrue(options.hasOption("longname"));
        Option opt = options.getOption("longname");
        assertNotNull(opt);
        assertNull(opt.getOpt());
        assertEquals("longname", opt.getLongOpt());
    }

    @Test
    public void testAddOptionGroup() {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        Option opt1 = new Option("x", "xray", false, "X option");
        Option opt2 = new Option("y", "yankee", false, "Y option");
        group.addOption(opt1);
        group.addOption(opt2);
        options.addOptionGroup(group);

        assertTrue(options.hasOption("x"));
        assertTrue(options.hasOption("y"));
        assertSame(group, options.getOptionGroup(opt1));
        assertSame(group, options.getOptionGroup(opt2));
        assertTrue(options.getRequiredOptions().contains(group));
    }

    @Test
    public void testGetOptionGroupForNonGrouped() {
        Option opt = new Option("z", "zulu", false, "Z option");
        options.addOption(opt);
        assertNull(options.getOptionGroup(opt));
    }

    @Test
    public void testGetMatchingOptionsPrefix() {
        options.addOption("a", "alpha", false, "Alpha");
        options.addOption("b", "beta", false, "Beta");

        List<String> matches = options.getMatchingOptions("al");
        assertEquals(1, matches.size());
        assertTrue(matches.contains("alpha"));

        matches = options.getMatchingOptions("a");
        assertEquals(2, matches.size());
        assertTrue(matches.contains("a"));
        assertTrue(matches.contains("alpha"));
    }

    @Test
    public void testGetMatchingOptionsNoMatch() {
        options.addOption("a", "alpha", false, "Alpha");
        List<String> matches = options.getMatchingOptions("b");
        assertTrue(matches.isEmpty());
    }

    @Test
    public void testGetMatchingOptionsEmptyPrefix() {
        options.addOption("a", "alpha", false, "Alpha");
        List<String> matches = options.getMatchingOptions("");
        assertFalse(matches.isEmpty());
    }

    @Test
    public void testGetMatchingOptionsExactMatch() {
        options.addOption("a", "alpha", false, "Alpha");
        List<String> matches = options.getMatchingOptions("alpha");
        assertEquals(1, matches.size());
        assertTrue(matches.contains("alpha"));
    }

    @Test
    public void testGetMatchingOptionsCaseSensitive() {
        options.addOption("a", "Alpha", false, "Alpha uppercase");
        List<String> matches = options.getMatchingOptions("Al");
        assertEquals(1, matches.size());
        assertTrue(matches.contains("Alpha"));
    }

    @Test
    public void testGetOptionsCollection() {
        options.addOption("a", "alpha", false, "Alpha");
        options.addOption("b", "beta", true, "Beta");
        Collection<Option> opts = options.getOptions();
        assertEquals(2, opts.size());
    }

    @Test
    public void testGetRequiredOptionsEmpty() {
        assertTrue(options.getRequiredOptions().isEmpty());
    }

    @Test
    public void testGetRequiredOptionsWithRequiredOption() {
        Option opt = new Option("a", "alpha", false, "Alpha");
        opt.setRequired(true);
        options.addOption(opt);
        assertTrue(options.getRequiredOptions().contains(opt));
    }

    @Test
    public void testAddOptions() {
        Options opts = new Options();
        opts.addOption("a", "alpha", false, "Alpha");
        options.addOptions(opts);
        assertTrue(options.hasOption("a"));
    }

    @Test
    public void testAddDuplicateShortOption() {
        options.addOption("a", "alpha", false, "Alpha");
        options.addOption("a", "alternate", false, "Alternate");
        Option opt = options.getOption("a");
        assertEquals("alternate", opt.getLongOpt());
    }

    @Test
    public void testGetOptionNull() {
        assertNull(options.getOption(null));
    }

    @Test
    public void testHasOptionNull() {
        assertFalse(options.hasOption(null));
    }

    @Test
    public void testGetOptionByLongName() {
        options.addOption("a", "alpha", false, "Alpha");
        Option opt = options.getOption("alpha");
        assertNotNull(opt);
        assertEquals("a", opt.getOpt());
    }

    @Test
    public void testGetOptionByShortName() {
        options.addOption("a", "alpha", false, "Alpha");
        Option opt = options.getOption("a");
        assertNotNull(opt);
        assertEquals("alpha", opt.getLongOpt());
    }

    @Test
    public void testGetOptionUnknown() {
        assertNull(options.getOption("unknown"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddOptionWithEmptyShortOpt() {
        options.addOption("", "longname", false, "Empty short");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddOptionWithNullLongOptAndNullShortOpt() {
        options.addOption(null, null, false, "Both null");
    }
}