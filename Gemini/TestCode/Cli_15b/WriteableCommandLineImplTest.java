package org.apache.commons.cli2.commandline;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.option.ArgumentImpl;
import org.apache.commons.cli2.option.Command;
import org.apache.commons.cli2.option.DefaultOption;
import org.apache.commons.cli2.option.PropertyOption;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class WriteableCommandLineImplTest {

    private WriteableCommandLineImpl commandLine;
    private Option optionA;
    private Option optionB;
    private List<Option> options;

    @Before
    public void setUp() {
        optionA = new DefaultOption("a", "alpha", false, "alpha option", null, null, null, ':', '-', 1, 1);
        optionB = new DefaultOption("b", "beta", false, "beta option", null, null, null, ':', '-', 1, 1);
        options = new ArrayList<Option>();
        options.add(optionA);
        options.add(optionB);

        commandLine = new WriteableCommandLineImpl(options, null);
    }

    @Test
    public void testConstructionAndBasicGetters() {
        Assert.assertNotNull(commandLine);
        Assert.assertTrue(commandLine.hasOption(optionA));
        Assert.assertTrue(commandLine.hasOption(optionB));
        
        Option unknown = new DefaultOption("u", "unknown", false, "unknown", null, null, null, ':', '-', 1, 1);
        Assert.assertFalse(commandLine.hasOption(unknown));
    }

    @Test
    public void testAddOption() {
        Option optionC = new DefaultOption("c", "gamma", false, "gamma", null, null, null, ':', '-', 1, 1);
        Assert.assertFalse(commandLine.hasOption(optionC));
        
        commandLine.addOption(optionC);
        Assert.assertTrue(commandLine.hasOption(optionC));
    }

    @Test
    public void testAddValue() {
        commandLine.addValue(optionA, "value1");
        commandLine.addValue(optionA, "value2");

        Assert.assertTrue(commandLine.hasOption(optionA));
        Assert.assertEquals("value1", commandLine.getValue(optionA));
        
        List<?> values = commandLine.getValues(optionA);
        Assert.assertNotNull(values);
        Assert.assertEquals(2, values.size());
        Assert.assertEquals("value1", values.get(0));
        Assert.assertEquals("value2", values.get(1));
    }

    @Test
    public void testAddProperty() {
        commandLine.addProperty("prop1", "val1");
        commandLine.addProperty("prop1", "val2");
        commandLine.addProperty("prop2", "val3");

        Assert.assertEquals("val2", commandLine.getProperty("prop1"));
        Assert.assertEquals("val3", commandLine.getProperty("prop2"));
        Assert.assertNull(commandLine.getProperty("nonexistent"));

        Set<String> properties = commandLine.getProperties();
        Assert.assertNotNull(properties);
        Assert.assertTrue(properties.contains("prop1"));
        Assert.assertTrue(properties.contains("prop2"));
    }

    @Test
    public void testGetOptions() {
        Set<Option> retrievedOptions = commandLine.getOptions();
        Assert.assertNotNull(retrievedOptions);
        Assert.assertTrue(retrievedOptions.contains(optionA));
        Assert.assertTrue(retrievedOptions.contains(optionB));
    }

    @Test
    public void testGetParent() {
        Assert.assertNull(commandLine.getParent(optionA));
        
        Option parentOption = new Command("cmd", "Command", null, null, null, 0);
        commandLine.addParent(optionA, parentOption);
        
        Assert.assertEquals(parentOption, commandLine.getParent(optionA));
    }

    @Test
    public void testSwitchOptionState() {
        Assert.assertFalse(commandLine.switches.containsKey(optionA));
        
        commandLine.switches.put(optionA, Boolean.TRUE);
        Assert.assertTrue(commandLine.hasOption(optionA));
        
        commandLine.switches.put(optionA, Boolean.FALSE);
        Assert.assertFalse(commandLine.hasOption(optionA));
    }

    @Test
    public void testGetValuesWithDefault() {
        List<String> defaultValues = new ArrayList<String>();
        defaultValues.add("default");

        // When option is not present and has no values
        List<?> values = commandLine.getValues(optionA, defaultValues);
        Assert.assertEquals(defaultValues, values);

        // When option has values
        commandLine.addValue(optionA, "actual");
        List<?> actualValues = commandLine.getValues(optionA, defaultValues);
        Assert.assertNotEquals(defaultValues, actualValues);
        Assert.assertEquals(1, actualValues.size());
        Assert.assertEquals("actual", actualValues.get(0));
    }

    @Test
    public void testGetValueWithDefaultObject() {
        Assert.assertEquals("default", commandLine.getValue(optionA, "default"));

        commandLine.addValue(optionA, "actual");
        Assert.assertEquals("actual", commandLine.getValue(optionA, "default"));
    }

    @Test
    public void testGetPropertyWithDefault() {
        Assert.assertEquals("default", commandLine.getProperty("missing", "default"));

        commandLine.addProperty("prop", "actual");
        Assert.assertEquals("actual", commandLine.getProperty("prop", "default"));
    }

    @Test
    public void testToString() {
        commandLine.addValue(optionA, "valA");
        commandLine.addProperty("p1", "v1");
        String str = commandLine.toString();
        Assert.assertNotNull(str);
    }
}