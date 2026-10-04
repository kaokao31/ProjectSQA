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
    public void testGetNames() {
        Option opt1 = new Option("a", "alpha", false, "alpha option");
        Option opt2 = new Option("b", "beta", false, "beta option");

        group.addOption(opt1);
        group.addOption(opt2);

        Collection<String> names = group.getNames();
        assertEquals(2, names.size());
        assertTrue(names.contains("a"));
        assertTrue(names.contains("b"));
    }

    @Test(expected = AlreadySelectedException.class)
    public void testSetSelectedTwiceDifferentOptions() throws AlreadySelectedException {
        Option opt1 = new Option("a", "alpha", false, "alpha option");
        Option opt2 = new Option("b", "beta", false, "beta option");

        group.addOption(opt1);
        group.addOption(opt2);

        group.setSelected(opt1);
        assertEquals(opt1, group.getSelected());

        // Selecting a different option in the same group should throw AlreadySelectedException
        group.setSelected(opt2);
    }

    @Test
    public void testSetSelectedSameOptionTwice() throws AlreadySelectedException {
        Option opt1 = new Option("a", "alpha", false, "alpha option");

        group.addOption(opt1);

        group.setSelected(opt1);
        assertEquals(opt1, group.getSelected());

        // Selecting the same option again should not throw an exception
        group.setSelected(opt1);
        assertEquals(opt1, group.getSelected());
    }

    @Test
    public void testSetSelectedNull() throws AlreadySelectedException {
        Option opt1 = new Option("a", "alpha", false, "alpha option");

        group.addOption(opt1);

        group.setSelected(opt1);
        assertEquals(opt1, group.getSelected());

        // Setting selected to null should clear the selected option
        group.setSelected(null);
        assertNull(group.getSelected());
    }

    @Test(expected = AlreadySelectedException.class)
    public void testSetSelectedOptionNotInGroup() throws AlreadySelectedException {
        Option opt1 = new Option("a", "alpha", false, "alpha option");
        Option optNotInGroup = new Option("x", "xray", false, "not in group");

        group.addOption(opt1);

        // Selecting an option that belongs to the group first
        group.setSelected(opt1);

        // Now select an option that was never added to the group (and selected is not null)
        group.setSelected(optNotInGroup);
    }

    @Test
    public void testSetSelectedOptionNotInGroupWhenSelectedIsNull() throws AlreadySelectedException {
        Option optNotInGroup = new Option("x", "xray", false, "not in group");

        // When selected is currently null, setting an option not in the group should actually allow it or set it (depending on implementation, 
        // but let's test the branch where selected == null).
        group.setSelected(optNotInGroup);
        assertEquals(optNotInGroup, group.getSelected());
    }

    @Test
    public void testSetSelectedSameOptionNotInGroupWhenSelectedIsNotNull() throws AlreadySelectedException {
        Option optNotInGroup = new Option("x", "xray", false, "not in group");

        group.setSelected(optNotInGroup);
        // Setting the exact same option when it is already selected (even if not explicitly in group map/list)
        group.setSelected(optNotInGroup);
        assertEquals(optNotInGroup, group.getSelected());
    }

    @Test
    public void testIsRequired() {
        assertFalse(group.isRequired());

        group.setRequired(true);
        assertTrue(group.isRequired());

        group.setRequired(false);
        assertFalse(group.isRequired());
    }

    @Test
    public void testToString() {
        Option opt1 = new Option("a", "alpha", false, "alpha option");
        Option opt2 = new Option("b", "beta", false, "beta option");

        group.addOption(opt1);
        group.addOption(opt2);

        String str = group.toString();
        assertNotNull(str);
        assertTrue(str.contains("[a"));
        assertTrue(str.contains("[b"));
    }
}