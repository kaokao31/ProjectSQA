package org.apache.commons.cli;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class OptionGroupTest {

    private OptionGroup group;

    @Before
    public void setUp() {
        group = new OptionGroup();
    }

    @Test
    public void testAddOption() {
        Option opt = new Option("a", "aaa", false, "desc");
        assertFalse(group.getOptions().contains(opt));
        group.addOption(opt);
        assertTrue(group.getOptions().contains(opt));
        assertEquals(opt, group.getOptions().get(0));
    }

    @Test
    public void testGetOptionsInitiallyEmpty() {
        assertTrue(group.getOptions().isEmpty());
        assertEquals(0, group.getOptions().size());
    }

    @Test
    public void testSetRequiredTrue() {
        assertFalse(group.isRequired());
        group.setRequired(true);
        assertTrue(group.isRequired());
    }

    @Test
    public void testSetRequiredFalse() {
        group.setRequired(true);
        assertTrue(group.isRequired());
        group.setRequired(false);
        assertFalse(group.isRequired());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetSelectedWithNullOptionThrowsException() {
        group.setSelected(null);
    }

    @Test
    public void testSetSelectedValidOption() {
        Option opt = new Option("b", "bbb", false, "desc");
        group.addOption(opt);
        assertEquals(null, group.getSelected());
        group.setSelected(opt);
        assertEquals("bbb", group.getSelected());
    }

    @Test(expected = AlreadySelectedException.class)
    public void testSetSelectedWithTwoDifferentOptionsThrowsException() {
        Option opt1 = new Option("c", "ccc", false, "desc");
        Option opt2 = new Option("d", "ddd", false, "desc");
        group.addOption(opt1);
        group.addOption(opt2);
        group.setSelected(opt1);
        group.setSelected(opt2);
    }

    @Test
    public void testSetSelectedSameOptionTwiceNoException() {
        Option opt = new Option("e", "eee", false, "desc");
        group.addOption(opt);
        group.setSelected(opt);
        group.setSelected(opt);
        assertEquals("eee", group.getSelected());
    }

    @Test
    public void testGetSelectedInitiallyNull() {
        assertNull(group.getSelected());
    }

    @Test
    public void testIsRequiredDefaultFalse() {
        assertFalse(group.isRequired());
    }

    @Test
    public void testToStringWithOneOption() {
        Option opt = new Option("f", "fff", false, "desc");
        group.addOption(opt);
        String result = group.toString();
        assertTrue(result.contains("["));
        assertTrue(result.contains("]"));
        assertTrue(result.contains("-f"));
        assertTrue(result.contains("fff"));
    }

    @Test
    public void testToStringWithTwoOptions() {
        Option opt1 = new Option("g", "ggg", false, "desc");
        Option opt2 = new Option("h", "hhh", false, "desc");
        group.addOption(opt1);
        group.addOption(opt2);
        String result = group.toString();
        assertTrue(result.startsWith("["));
        assertTrue(result.endsWith("]"));
        assertTrue(result.contains("-g") || result.contains("-h"));
    }

    @Test
    public void testToStringEmptyGroup() {
        String result = group.toString();
        assertEquals("[]", result);
    }

    @Test
    public void testSetSelectedWithMultipleOptionsFirstOneSuccess() {
        Option opt1 = new Option("i", "iii", false, "desc");
        Option opt2 = new Option("j", "jjj", false, "desc");
        group.addOption(opt1);
        group.addOption(opt2);
        group.setSelected(opt1);
        assertEquals("iii", group.getSelected());
    }

    @Test(expected = AlreadySelectedException.class)
    public void testSetSelectedWithMultipleOptionsSecondOptionThrows() {
        Option opt1 = new Option("k", "kkk", false, "desc");
        Option opt2 = new Option("l", "lll", false, "desc");
        group.addOption(opt1);
        group.addOption(opt2);
        group.setSelected(opt1);
        group.setSelected(opt2);
    }

    @Test
    public void testSetSelectedWithLongOption() {
        Option opt = new Option(null, "longOpt", false, "desc");
        group.addOption(opt);
        group.setSelected(opt);
        assertEquals("longOpt", group.getSelected());
    }

    @Test
    public void testSetSelectedWithBothShortAndLongOption() {
        Option opt = new Option("m", "mmm", false, "desc");
        group.addOption(opt);
        group.setSelected(opt);
        assertEquals("mmm", group.getSelected());
    }

    @Test
    public void testGetOptionsUnmodifiable() {
        Option opt = new Option("n", "nnn", false, "desc");
        group.addOption(opt);
        try {
            group.getOptions().add(new Option("o", "ooo", false, "desc"));
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(expected = AlreadySelectedException.class)
    public void testBug36Scenario() {
        Option opt1 = new Option("a", "aaa", false, "desc");
        Option opt2 = new Option("b", "bbb", false, "desc");
        Option opt3 = new Option("c", "ccc", false, "desc");
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        group.setSelected(opt1);
        group.setSelected(opt2);
    }

    @Test
    public void testSetSelectedWithNullInGroupThrows() {
        Option opt = new Option("p", "ppp", false, "desc");
        group.addOption(opt);
        group.setSelected(opt);
        try {
            group.setSelected(null);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("eee", group.getSelected());
        }
    }

    @Test
    public void testAddAndCheckMultipleOptions() {
        Option opt1 = new Option("q", "qqq", false, "desc");
        Option opt2 = new Option("r", "rrr", false, "desc");
        group.addOption(opt1);
        group.addOption(opt2);
        assertEquals(2, group.getOptions().size());
        assertTrue(group.getOptions().contains(opt1));
        assertTrue(group.getOptions().contains(opt2));
    }

    @Test
    public void testToStringWithMultipleOptionsOrder() {
        Option opt1 = new Option("s", "sss", false, "desc");
        Option opt2 = new Option("t", "ttt", false, "desc");
        group.addOption(opt1);
        group.addOption(opt2);
        String result = group.toString();
        int idx1 = result.indexOf("-s");
        int idx2 = result.indexOf("-t");
        assertTrue(idx1 >= 0);
        assertTrue(idx2 >= 0);
    }

    @Test(expected = AlreadySelectedException.class)
    public void testSetSelectedAfterAnotherOption() {
        Option opt1 = new Option("u", "uuu", false, "desc");
        Option opt2 = new Option("v", "vvv", false, "desc");
        group.addOption(opt1);
        group.addOption(opt2);
        group.setSelected(opt1);
        group.setSelected(opt2);
    }

    @Test
    public void testSetSelectedSameOptionTwice() {
        Option opt = new Option("w", "www", false, "desc");
        group.addOption(opt);
        group.setSelected(opt);
        group.setSelected(opt);
        assertEquals("www", group.getSelected());
    }
}