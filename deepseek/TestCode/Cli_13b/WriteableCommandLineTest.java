package org.apache.commons.cli2;

import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

/**
 * Test suite for WriteableCommandLine interface using the default implementation.
 * Targets maximum coverage and fault detection for CLI-13 related issues.
 */
public class WriteableCommandLineTest {

    private WriteableCommandLine cmd;
    private Option optionA;
    private Option optionB;

    @Before
    public void setUp() {
        // Use the concrete implementation from commons-cli2
        cmd = new WriteableCommandLineImpl();
        // Create simple options for testing
        optionA = new OptionBuilder().withLongOpt("optionA").create();
        optionB = new OptionBuilder().withLongOpt("optionB").create();
    }

    // ========== addOption tests ==========

    @Test
    public void testAddOptionNull() {
        // Edge case: adding null option
        try {
            cmd.addOption(null);
            fail("Expected IllegalArgumentException for null option");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testAddOptionValid() {
        cmd.addOption(optionA);
        assertTrue("Option should be present after add", cmd.hasOption(optionA));
    }

    @Test
    public void testAddOptionDuplicate() {
        cmd.addOption(optionA);
        cmd.addOption(optionA); // should not throw, but may overwrite or ignore
        assertTrue("Option should still be present", cmd.hasOption(optionA));
    }

    // ========== addValue tests ==========

    @Test
    public void testAddValueNullOption() {
        try {
            cmd.addValue(null, "value");
            fail("Expected IllegalArgumentException for null option");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testAddValueNullValue() {
        cmd.addOption(optionA);
        // Adding null value should be allowed (potential bug area)
        cmd.addValue(optionA, null);
        List<?> values = cmd.getValues(optionA, new ArrayList<>());
        assertTrue("Values should contain null", values.contains(null));
    }

    @Test
    public void testAddValueMultipleValues() {
        cmd.addOption(optionA);
        cmd.addValue(optionA, "first");
        cmd.addValue(optionA, "second");
        List<?> values = cmd.getValues(optionA, new ArrayList<>());
        assertEquals(2, values.size());
        assertEquals("first", values.get(0));
        assertEquals("second", values.get(1));
    }

    @Test
    public void testAddValueForUnregisteredOption() {
        // Adding value to an option not yet added should be handled gracefully
        cmd.addValue(optionB, "value");
        // Option may be auto-added or ignored; check behavior
        assertTrue("Option should be present after addValue", cmd.hasOption(optionB));
    }

    // ========== getValues tests ==========

    @Test
    public void testGetValuesNullOption() {
        try {
            cmd.getValues(null, new ArrayList<>());
            fail("Expected IllegalArgumentException for null option");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testGetValuesWithDefault() {
        List<String> defaultList = new ArrayList<>();
        defaultList.add("default");
        List<?> values = cmd.getValues(optionA, defaultList);
        // Option not added, should return default
        assertEquals("Should return default list", defaultList, values);
    }

    @Test
    public void testGetValuesNullDefault() {
        cmd.addOption(optionA);
        List<?> values = cmd.getValues(optionA, null);
        // Should return empty list or null? Typically returns empty list
        assertNotNull("Should not return null", values);
        assertTrue("Should be empty", values.isEmpty());
    }

    @Test
    public void testGetValuesAfterAddValue() {
        cmd.addOption(optionA);
        cmd.addValue(optionA, "test");
        List<?> values = cmd.getValues(optionA, new ArrayList<>());
        assertEquals(1, values.size());
        assertEquals("test", values.get(0));
    }

    // ========== hasOption tests ==========

    @Test
    public void testHasOptionNull() {
        try {
            cmd.hasOption(null);
            fail("Expected IllegalArgumentException for null option");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testHasOptionNotAdded() {
        assertFalse("Option not added should return false", cmd.hasOption(optionA));
    }

    @Test
    public void testHasOptionAfterAdd() {
        cmd.addOption(optionA);
        assertTrue("Option should be present", cmd.hasOption(optionA));
    }

    // ========== getOption tests ==========

    @Test
    public void testGetOptionByTrigger() {
        cmd.addOption(optionA);
        Option retrieved = cmd.getOption("optionA");
        assertNotNull("Should retrieve option by long trigger", retrieved);
        assertSame("Should be the same option instance", optionA, retrieved);
    }

    @Test
    public void testGetOptionByTriggerNotFound() {
        Option retrieved = cmd.getOption("nonexistent");
        assertNull("Should return null for unknown trigger", retrieved);
    }

    @Test
    public void testGetOptionNullTrigger() {
        try {
            cmd.getOption(null);
            fail("Expected IllegalArgumentException for null trigger");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ========== removeOption tests (if present) ==========

    @Test
    public void testRemoveOption() {
        cmd.addOption(optionA);
        cmd.removeOption(optionA);
        assertFalse("Option should be removed", cmd.hasOption(optionA));
    }

    @Test
    public void testRemoveOptionNotPresent() {
        // Should not throw
        cmd.removeOption(optionA);
        // No exception expected
    }

    // ========== getOptions tests ==========

    @Test
    public void testGetOptionsEmpty() {
        List<?> options = cmd.getOptions();
        assertNotNull("Should not return null", options);
        assertTrue("Should be empty initially", options.isEmpty());
    }

    @Test
    public void testGetOptionsAfterAdd() {
        cmd.addOption(optionA);
        cmd.addOption(optionB);
        List<?> options = cmd.getOptions();
        assertEquals(2, options.size());
        assertTrue(options.contains(optionA));
        assertTrue(options.contains(optionB));
    }

    // ========== Edge cases for CLI-13 bug ==========
    // Bug may involve handling of null values or duplicate additions

    @Test
    public void testAddValueThenGetValuesWithNullDefault() {
        cmd.addOption(optionA);
        cmd.addValue(optionA, "value");
        List<?> values = cmd.getValues(optionA, null);
        assertNotNull("Should not return null", values);
        assertEquals(1, values.size());
        assertEquals("value", values.get(0));
    }

    @Test
    public void testAddMultipleOptionsAndValues() {
        cmd.addOption(optionA);
        cmd.addOption(optionB);
        cmd.addValue(optionA, "A1");
        cmd.addValue(optionA, "A2");
        cmd.addValue(optionB, "B1");
        List<?> valuesA = cmd.getValues(optionA, new ArrayList<>());
        List<?> valuesB = cmd.getValues(optionB, new ArrayList<>());
        assertEquals(2, valuesA.size());
        assertEquals(1, valuesB.size());
        assertEquals("A1", valuesA.get(0));
        assertEquals("A2", valuesA.get(1));
        assertEquals("B1", valuesB.get(0));
    }

    @Test
    public void testAddValueWithEmptyString() {
        cmd.addOption(optionA);
        cmd.addValue(optionA, "");
        List<?> values = cmd.getValues(optionA, new ArrayList<>());
        assertEquals(1, values.size());
        assertEquals("", values.get(0));
    }

    @Test
    public void testAddValueWithSpecialCharacters() {
        cmd.addOption(optionA);
        cmd.addValue(optionA, "value with spaces");
        cmd.addValue(optionA, "value\nnewline");
        List<?> values = cmd.getValues(optionA, new ArrayList<>());
        assertEquals(2, values.size());
        assertEquals("value with spaces", values.get(0));
        assertEquals("value\nnewline", values.get(1));
    }

    // ========== Tests for potential null pointer issues ==========

    @Test
    public void testGetValuesAfterRemoveOption() {
        cmd.addOption(optionA);
        cmd.addValue(optionA, "value");
        cmd.removeOption(optionA);
        // After removal, getValues should return default or empty
        List<String> defaultList = new ArrayList<>();
        defaultList.add("default");
        List<?> values = cmd.getValues(optionA, defaultList);
        assertEquals("Should return default after removal", defaultList, values);
    }

    @Test
    public void testHasOptionAfterRemove() {
        cmd.addOption(optionA);
        cmd.removeOption(optionA);
        assertFalse("Option should not be present after removal", cmd.hasOption(optionA));
    }

    // ========== Tests for option with short trigger ==========

    @Test
    public void testGetOptionByShortTrigger() {
        Option shortOpt = new OptionBuilder().withShortOpt('a').create();
        cmd.addOption(shortOpt);
        Option retrieved = cmd.getOption("a");
        assertNotNull("Should retrieve by short trigger", retrieved);
        assertSame(shortOpt, retrieved);
    }

    @Test
    public void testGetOptionByLongTriggerPreference() {
        Option longOpt = new OptionBuilder().withLongOpt("long").withShortOpt('l').create();
        cmd.addOption(longOpt);
        // Both triggers should work
        assertNotNull(cmd.getOption("long"));
        assertNotNull(cmd.getOption("l"));
    }
}