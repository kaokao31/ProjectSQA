package org.apache.commons.cli2;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class WriteableCommandLineTest {

    private WriteableCommandLine commandLine;
    private Option option;
    private Option option2;
    private OptionGroup group;
    private Argument argument;

    @Before
    public void setUp() {
        commandLine = new WriteableCommandLineImpl(new org.apache.commons.cli2.util.HelpFormatter());
        option = new Option.Builder().withLongName("verbose").create();
        option2 = new Option.Builder().withLongName("quiet").create();
        group = new OptionGroup();
        argument = new Argument.Builder().withName("file").withMinimum(1).create();
    }

    @Test
    public void testAddOption() {
        assertTrue("Option list should be empty initially", commandLine.getOptions().isEmpty());
        commandLine.addOption(option);
        assertEquals("One option should be present", 1, commandLine.getOptions().size());
        assertTrue("Verbose option should be in list", commandLine.getOptions().contains(option));
    }

    @Test
    public void testAddOptionMultipleTimes() {
        commandLine.addOption(option);
        commandLine.addOption(option);
        assertEquals("Option should be added only once", 1, commandLine.getOptions().size());
    }

    @Test
    public void testHasOption() {
        assertFalse("Should not have verbose option initially", commandLine.hasOption(option));
        commandLine.addOption(option);
        assertTrue("Should have verbose option after adding", commandLine.hasOption(option));
    }

    @Test
    public void testHasOptionNull() {
        assertFalse("Null option should return false", commandLine.hasOption(null));
    }

    @Test
    public void testGetOptionByTrigger() {
        commandLine.addOption(option);
        assertSame("Should retrieve the same option", option, commandLine.getOption(option.getPreferredName()));
        assertNull("Non-existent trigger should return null", commandLine.getOption("nonexistent"));
    }

    @Test
    public void testGetOptionByTriggerNull() {
        assertNull("Null trigger should return null", commandLine.getOption(null));
    }

    @Test
    public void testGetValues() {
        commandLine.addOption(option);
        List<String> values = commandLine.getValues(option);
        assertNotNull("Values list should not be null", values);
        assertTrue("Values list should be empty initially", values.isEmpty());
    }

    @Test
    public void testGetValuesWithNullOption() {
        List<String> values = commandLine.getValues(null);
        assertNull("Null option should return null", values);
    }

    @Test
    public void testAddValue() {
        commandLine.addOption(option);
        commandLine.addValue(option, "test.txt");
        List<String> values = commandLine.getValues(option);
        assertEquals("One value should be present", 1, values.size());
        assertEquals("Value should be test.txt", "test.txt", values.get(0));
    }

    @Test
    public void testAddValueMultiple() {
        commandLine.addOption(option);
        commandLine.addValue(option, "a.txt");
        commandLine.addValue(option, "b.txt");
        List<String> values = commandLine.getValues(option);
        assertEquals("Two values should be present", 2, values.size());
        assertEquals("First value should be a.txt", "a.txt", values.get(0));
        assertEquals("Second value should be b.txt", "b.txt", values.get(1));
    }

    @Test
    public void testAddValueNullValue() {
        commandLine.addOption(option);
        commandLine.addValue(option, null);
        List<String> values = commandLine.getValues(option);
        assertEquals("One null value should be added", 1, values.size());
        assertNull("Value should be null", values.get(0));
    }

    @Test
    public void testGetUndefaultedValues() {
        commandLine.addOption(option);
        List<String> values = commandLine.getUndefaultedValues(option);
        assertNotNull("Undefaulted values should not be null", values);
        assertTrue("Undefaulted values should be empty initially", values.isEmpty());
    }

    @Test
    public void testGetUndefaultedValuesWithDefaults() {
        commandLine.addOption(option);
        commandLine.addValue(option, "value1");
        commandLine.setDefaultValues(option, new String[]{"default1"});
        List<String> undefaulted = commandLine.getUndefaultedValues(option);
        assertEquals("Undefaulted should show only explicit values", 1, undefaulted.size());
        assertEquals("Undefaulted should be actual value", "value1", undefaulted.get(0));
    }

    @Test
    public void testGetDefaultValues() {
        commandLine.addOption(option);
        List<String> defaults = commandLine.getDefaultValues(option);
        assertNotNull("Default values should not be null", defaults);
        assertTrue("Default values should be empty initially", defaults.isEmpty());
    }

    @Test
    public void testSetDefaultValues() {
        commandLine.addOption(option);
        commandLine.setDefaultValues(option, new String[]{"default1", "default2"});
        List<String> defaults = commandLine.getDefaultValues(option);
        assertEquals("Two default values should be present", 2, defaults.size());
        assertEquals("First default should be default1", "default1", defaults.get(0));
        assertEquals("Second default should be default2", "default2", defaults.get(1));
    }

    @Test
    public void testSetDefaultValuesOverwrite() {
        commandLine.addOption(option);
        commandLine.setDefaultValues(option, new String[]{"oldDefault"});
        commandLine.setDefaultValues(option, new String[]{"newDefault"});
        List<String> defaults = commandLine.getDefaultValues(option);
        assertEquals("One default after overwrite", 1, defaults.size());
        assertEquals("Default should be newDefault", "newDefault", defaults.get(0));
    }

    @Test
    public void testSetDefaultValuesNull() {
        commandLine.addOption(option);
        commandLine.setDefaultValues(option, null);
        List<String> defaults = commandLine.getDefaultValues(option);
        assertNotNull("Default values should not be null after null set", defaults);
        assertTrue("Default values should be empty after null set", defaults.isEmpty());
    }

    @Test
    public void testGetProperties() {
        Properties props = commandLine.getProperties(argument);
        assertNotNull("Properties should not be null", props);
        assertTrue("Properties should be empty initially", props.isEmpty());
    }

    @Test
    public void testGetPropertiesWithValues() {
        commandLine.addOption(option);
        commandLine.addValue(option, "key1=value1");
        commandLine.addValue(option, "key2=value2");
        commandLine.addProperty(option, "key1", "value1");
        commandLine.addProperty(option, "key2", "value2");
        Properties props = commandLine.getProperties(option);
        assertEquals("Two properties should be present", 2, props.size());
        assertEquals("key1 property should be value1", "value1", props.get("key1"));
        assertEquals("key2 property should be value2", "value2", props.get("key2"));
    }

    @Test
    public void testGetPropertiesNullOption() {
        Properties props = commandLine.getProperties(null);
        assertNull("Properties for null option should be null", props);
    }

    @Test
    public void testAddProperty() {
        commandLine.addOption(option);
        commandLine.addProperty(option, "myKey", "myValue");
        Properties props = commandLine.getProperties(option);
        assertEquals("One property should be present", 1, props.size());
        assertEquals("Property value should be myValue", "myValue", props.get("myKey"));
    }

    @Test
    public void testAddPropertyNullKey() {
        commandLine.addOption(option);
        commandLine.addProperty(option, null, "value");
        Properties props = commandLine.getProperties(option);
        assertNotNull("Properties should not be null", props);
        assertEquals("Map should be empty after null key", 0, props.size());
    }

    @Test
    public void testAddPropertyNullValue() {
        commandLine.addOption(option);
        commandLine.addProperty(option, "key", null);
        Properties props = commandLine.getProperties(option);
        assertEquals("One property with null value", 1, props.size());
        assertTrue("Property key should exist", props.containsKey("key"));
        assertNull("Property value should be null", props.get("key"));
    }

    @Test
    public void testGetOptionWithNames() {
        commandLine.addOption(option);
        assertSame("Should get option by long name", option, commandLine.getOption("--verbose"));
        assertNull("Non-existent name should return null", commandLine.getOption("--nonexistent"));
    }

    @Test
    public void testGetOptionWithNullName() {
        assertNull("Null name should return null", commandLine.getOption(null));
    }

    @Test
    public void testGetOptionsImmutable() {
        commandLine.addOption(option);
        List<Option> options = commandLine.getOptions();
        assertNotNull("Options list should not be null", options);
        try {
            options.add(option2);
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testGetValuesImmutable() {
        commandLine.addOption(option);
        commandLine.addValue(option, "test");
        List<String> values = commandLine.getValues(option);
        try {
            values.add("newValue");
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testGetDefaultValuesImmutable() {
        commandLine.addOption(option);
        commandLine.setDefaultValues(option, new String[]{"default"});
        List<String> defaults = commandLine.getDefaultValues(option);
        try {
            defaults.add("extra");
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testGetPropertiesImmutable() {
        commandLine.addOption(option);
        commandLine.addProperty(option, "key", "value");
        Properties props = commandLine.getProperties(option);
        try {
            props.put("newKey", "newValue");
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testMultipleOptions() {
        commandLine.addOption(option);
        commandLine.addOption(option2);
        assertEquals("Two options should be present", 2, commandLine.getOptions().size());
        assertTrue("Verbose should be present", commandLine.hasOption(option));
        assertTrue("Quiet should be present", commandLine.hasOption(option2));
    }

    @Test
    public void testAddValueToNonExistentOption() {
        // Adding value to an option not in the command line, should still work
        commandLine.addValue(option, "value");
        assertTrue("Option should be automatically added", commandLine.hasOption(option));
        List<String> values = commandLine.getValues(option);
        assertEquals("One value should be present", 1, values.size());
    }

    @Test
    public void testSetDefaultValuesWithNullOption() {
        commandLine.setDefaultValues(null, new String[]{"default"});
        List<String> defaults = commandLine.getDefaultValues(null);
        assertNull("Default values for null option should be null", defaults);
    }

    @Test
    public void testGetUndefaultedValuesWithNull() {
        List<String> undefaulted = commandLine.getUndefaultedValues(null);
        assertNull("Undefaulted for null option should be null", undefaulted);
    }

    @Test
    public void testAddPropertyWithNullOption() {
        commandLine.addProperty(null, "key", "value");
        Properties props = commandLine.getProperties(null);
        assertNull("Properties for null should still be null", props);
    }

    @Test
    public void testAddPropertyWithoutOption() {
        commandLine.addProperty(option, "key", "value");
        assertTrue("Option should be added when property added", commandLine.hasOption(option));
        Properties props = commandLine.getProperties(option);
        assertEquals("One property should be present", 1, props.size());
    }

    @Test
    public void testLargeNumberOfValues() {
        commandLine.addOption(option);
        for (int i = 0; i < 1000; i++) {
            commandLine.addValue(option, "value" + i);
        }
        List<String> values = commandLine.getValues(option);
        assertEquals("Should have 1000 values", 1000, values.size());
    }

    @Test
    public void testGetValuesAfterSetDefault() {
        commandLine.addOption(option);
        commandLine.addValue(option, "explicitValue");
        commandLine.setDefaultValues(option, new String[]{"defaultValue"});
        List<String> values = commandLine.getValues(option);
        assertEquals("Should return explicit value", 1, values.size());
        assertEquals("Should be explicitValue", "explicitValue", values.get(0));
    }

    @Test
    public void testGetValuesWithOnlyDefaults() {
        commandLine.addOption(option);
        commandLine.setDefaultValues(option, new String[]{"default1", "default2"});
        List<String> values = commandLine.getValues(option);
        assertEquals("Should return default values", 2, values.size());
        assertEquals("First should be default1", "default1", values.get(0));
        assertEquals("Second should be default2", "default2", values.get(1));
    }

    @Test
    public void testUndefaultedValuesWithOnlyDefaults() {
        commandLine.addOption(option);
        commandLine.setDefaultValues(option, new String[]{"default1", "default2"});
        List<String> undefaulted = commandLine.getUndefaultedValues(option);
        assertTrue("Undefaulted should be empty when only defaults", undefaulted.isEmpty());
    }

    @Test
    public void testNullTriggersInOption() {
        // Test if option with null trigger is handled
        Option nullTriggerOption = new Option.Builder().withLongName("test").create();
        commandLine.addOption(nullTriggerOption);
        assertTrue("Should have the option", commandLine.hasOption(nullTriggerOption));
    }

    @Test
    public void testDuplicateTriggers() {
        Option optionA = new Option.Builder().withLongName("same").create();
        Option optionB = new Option.Builder().withLongName("same").create();
        commandLine.addOption(optionA);
        commandLine.addOption(optionB);
        assertTrue("Should contain first option", commandLine.getOptions().contains(optionA));
        assertTrue("Should contain second option", commandLine.getOptions().contains(optionB));
        // Should not throw when both options with same trigger are added
    }

    @Test
    public void testClearValues() {
        commandLine.addOption(option);
        commandLine.addValue(option, "value");
        commandLine.getValues(option).clear(); // This should fail because it's unmodifiable
        List<String> values = commandLine.getValues(option);
        assertEquals("Values should still be present after attempted clear", 1, values.size());
    }

    @Test
    public void testClearUndefaultedValues() {
        commandLine.addOption(option);
        commandLine.addValue(option, "value");
        commandLine.getUndefaultedValues(option).clear(); // This should fail because it's unmodifiable
        List<String> values = commandLine.getUndefaultedValues(option);
        assertEquals("Undefaulted values should still be present", 1, values.size());
    }

    @Test
    public void testOptionEquality() {
        Option sameOption = new Option.Builder().withLongName("verbose").create();
        commandLine.addOption(option);
        assertFalse("Same trigger but different object should still work", commandLine.getOption("--verbose").equals(sameOption));
        assertEquals("Same trigger should return the option even if different instance", option, commandLine.getOption("--verbose"));
    }
}