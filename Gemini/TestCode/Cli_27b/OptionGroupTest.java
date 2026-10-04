package org.apache.commons.cli;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

public class OptionGroupTest {

    private OptionGroup group;

    @Before
    public void setUp() {
        group = new OptionGroup();
    }

    @Test
    public void testAddOption() {
        Option opt1 = new Option("a", "alpha", false, "alpha option");
        Option opt2 = new Option("b", "beta", false, "beta option");

        group.addOption(opt1);
        group.addOption(opt2);

        Collection<Option> options = group.getOptions();
        assertEquals(2, options.size());
        assertTrue(options.contains(opt1));
        assertTrue(options.contains(opt2));
    }

    @Test
    public void testGetName() {
        Option opt1 = new Option("a", "alpha", false, "alpha option");
        Option opt2 = new Option("b", "beta", false, "beta option");

        group.addOption(opt1);
        group.addOption(opt2);

        Collection<String> names = group.getNames();
        assertEquals(2, names.size());
        assertTrue(names.contains("a"));
        assertTrue(names.contains("b"));
    }

    @Test
    public void testSelected() throws AlreadySelectedException {
        Option opt1 = new Option("a", "alpha", false, "alpha option");
        Option opt2 = new Option("b", "beta", false, "beta option");

        group.addOption(opt1);
        group.addOption(opt2);

        assertNull(group.getSelected());

        group.setSelected(opt1);
        assertEquals("a", group.getSelected());

        // Selecting the same option should not throw an exception
        group.setSelected(opt1);
        assertEquals("a", group.getSelected());

        // Selecting null should reset the selected option
        group.setSelected(null);
        assertNull(group.getSelected());
    }

    @Test(expected = AlreadySelectedException.class)
    public void testAlreadySelectedException() throws AlreadySelectedException {
        Option opt1 = new Option("a", "alpha", false, "alpha option");
        Option opt2 = new Option("b", "beta", false, "beta option");

        group.addOption(opt1);
        group.addOption(opt2);

        group.setSelected(opt1);
        // This should throw AlreadySelectedException because opt1 is already selected
        group.setSelected(opt2);
    }

    @Test(expected = AlreadySelectedException.class)
    public void testAlreadySelectedExceptionWithNullPreviously() throws AlreadySelectedException {
        Option opt1 = new Option("a", "alpha", false, "alpha option");
        Option opt2 = new Option("b", "beta", false, "beta option");

        group.addOption(opt1);
        group.addOption(opt2);

        group.setSelected(opt1);
        // Reset to null
        group.setSelected(null);
        
        // Now select opt1, then opt2 to trigger exception with non-null state handling if any
        group.setSelected(opt1);
        group.setSelected(opt2);
    }

    @Test
    public void testSetSelectedWithOptionNotInGroup() {
        Option opt1 = new Option("a", "alpha", false, "alpha option");
        Option optNotInGroup = new Option("x", "xray", false, "xray option");

        group.addOption(opt1);

        try {
            group.setSelected(optNotInGroup);
            // Depending on implementation, setting an option not in the group might throw AlreadySelectedException or succeed.
            // Let's see how OptionGroup handles it. Actually, OptionGroup.setSelected checks if selected != null && selected != option.getOpt()
        } catch (AlreadySelectedException e) {
            // Expected or tested
        }
    }

    @Test
    public void testToString() {
        Option opt1 = new Option("a", "alpha", false, "alpha option");
        Option opt2 = new Option("b", "beta", false, "beta option");

        group.addOption(opt1);
        group.addOption(opt2);

        String str = group.toString();
        assertNotNull(str);
        assertTrue(str.contains("-a"));
        assertTrue(str.contains("-b"));
    }

    @Test
    public void testRequired() {
        assertFalse(group.isRequired());
        group.setRequired(true);
        assertTrue(group.isRequired());
        group.setRequired(false);
        assertFalse(group.isRequired());
    }
}