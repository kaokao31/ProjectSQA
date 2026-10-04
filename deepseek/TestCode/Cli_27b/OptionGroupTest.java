package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class OptionGroupTest {

    private OptionGroup group;
    private Option opt1;
    private Option opt2;
    private Option opt3;

    @Before
    public void setUp() {
        group = new OptionGroup();
        opt1 = new Option("a", "alpha", false, "first option");
        opt2 = new Option("b", "beta", false, "second option");
        opt3 = new Option("c", "gamma", false, "third option");
    }

    // ------------------------------------------------------------------
    // Basic addOption and getOptions
    // ------------------------------------------------------------------
    @Test
    public void testAddOptionAndGetOptions() {
        group.addOption(opt1);
        group.addOption(opt2);
        assertEquals(2, group.getOptions().size());
        assertTrue(group.getOptions().contains(opt1));
        assertTrue(group.getOptions().contains(opt2));
    }

    @Test
    public void testAddOptionNull() {
        try {
            group.addOption(null);
            fail("Expected IllegalArgumentException for null Option");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testGetOptionsEmpty() {
        assertTrue(group.getOptions().isEmpty());
    }

    // ------------------------------------------------------------------
    // setSelected and getSelected
    // ------------------------------------------------------------------
    @Test
    public void testSetSelectedFirstTime() {
        group.addOption(opt1);
        group.addOption(opt2);
        group.setSelected(opt1);
        assertSame(opt1, group.getSelected());
    }

    @Test
    public void testSetSelectedTwiceSameOption() throws Exception {
        group.addOption(opt1);
        group.addOption(opt2);
        group.setSelected(opt1);
        group.setSelected(opt1);  // selecting same option again should be OK
        assertSame(opt1, group.getSelected());
    }

    @Test(expected = AlreadySelectedException.class)
    public void testSetSelectedDifferentOptionThrowsException() {
        group.addOption(opt1);
        group.addOption(opt2);
        group.setSelected(opt1);
        // This should throw AlreadySelectedException
        group.setSelected(opt2);
    }

    @Test
    public void testSetSelectedNullClearsSelection() {
        group.addOption(opt1);
        group.setSelected(opt1);
        group.setSelected(null);
        assertNull(group.getSelected());
    }

    @Test
    public void testSetSelectedNullOnEmptyGroup() {
        group.setSelected(null);
        assertNull(group.getSelected());
    }

    // ------------------------------------------------------------------
    // isRequired / setRequired
    // ------------------------------------------------------------------
    @Test
    public void testDefaultNotRequired() {
        assertFalse(group.isRequired());
    }

    @Test
    public void testSetRequiredTrue() {
        group.setRequired(true);
        assertTrue(group.isRequired());
    }

    @Test
    public void testSetRequiredFalse() {
        group.setRequired(true);
        group.setRequired(false);
        assertFalse(group.isRequired());
    }

    // ------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------
    @Test
    public void testToStringEmptyGroup() {
        assertNotNull(group.toString());
        assertTrue(group.toString().contains("[]"));
    }

    @Test
    public void testToStringWithOptions() {
        group.addOption(opt1);
        group.addOption(opt2);
        String str = group.toString();
        assertNotNull(str);
        // Should include option keys (short opts)
        assertTrue(str.contains("-a") || str.contains("-b"));
    }

    // ------------------------------------------------------------------
    // Multiple selections with same option (different instance) 
    // ------------------------------------------------------------------
    @Test
    public void testSetSelectedDifferentInstanceSameKey() {
        Option opt1a = new Option("a", "alpha1", false, "another a");
        group.addOption(opt1);
        group.addOption(opt2);
        group.setSelected(opt1);
        // Selecting a different Option object with the same key should also fail
        // (assuming OptionGroup uses equals/hashCode)
        try {
            group.setSelected(opt1a);
            fail("Expected AlreadySelectedException for different instance same key");
        } catch (AlreadySelectedException e) {
            // expected
        }
    }

    // ------------------------------------------------------------------
    // Edge case: selection after removing option? (not supported by API)
    // ------------------------------------------------------------------

    // ------------------------------------------------------------------
    // Empty group setSelected non-null
    // ------------------------------------------------------------------
    @Test
    public void testSetSelectedOnEmptyGroup() {
        // Should be allowed? Depends on implementation. Usually it is allowed.
        group.setSelected(opt1);
        assertSame(opt1, group.getSelected());
    }

    // ------------------------------------------------------------------
    // Multiple options added, then setSelected null and re-select
    // ------------------------------------------------------------------
    @Test
    public void testResetSelectionThenSelectAnother() {
        group.addOption(opt1);
        group.addOption(opt2);
        group.setSelected(opt1);
        group.setSelected(null);
        group.setSelected(opt2);  // should work
        assertSame(opt2, group.getSelected());
    }

    // ------------------------------------------------------------------
    // select same option after clearing selection
    // ------------------------------------------------------------------
    @Test
    public void testSelectAfterClear() {
        group.addOption(opt1);
        group.setSelected(opt1);
        group.setSelected(null);
        group.setSelected(opt1);
        assertSame(opt1, group.getSelected());
    }

    // ------------------------------------------------------------------
    // getOptions returns unmodifiable collection? (commons-cli returns Set)
    // ------------------------------------------------------------------
    @Test
    public void testGetOptionsUnmodifiable() {
        group.addOption(opt1);
        java.util.Collection<Option> opts = group.getOptions();
        try {
            opts.add(opt2);
            fail("getOptions() should return an unmodifiable set");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // ------------------------------------------------------------------
    // AlreadySelectedException message content
    // ------------------------------------------------------------------
    @Test
    public void testAlreadySelectedExceptionMessage() {
        group.addOption(opt1);
        group.addOption(opt2);
        group.setSelected(opt1);
        try {
            group.setSelected(opt2);
            fail("Expected AlreadySelectedException");
        } catch (AlreadySelectedException e) {
            String msg = e.getMessage();
            assertNotNull(msg);
            assertTrue(msg.contains("option 'b'"));
        }
    }
}