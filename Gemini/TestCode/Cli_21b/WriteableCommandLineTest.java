package org.apache.commons.cli2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for WriteableCommandLine (CLI Bug ID 21 context).
 * Since WriteableCommandLine is an abstract class, we create an anonymous 
 * or concrete subclass to thoroughly test all its concrete methods.
 */
public class WriteableCommandLineTest {

    private WriteableCommandLine cmd;
    private Option testOption;
    private Option testOption2;

    private static class ConcreteWriteableCommandLine extends WriteableCommandLine {
        private final Map<Option, List<String>> values = new HashMap<>();
        private final Set<Option> switches = new HashSet<>();
        private final Map<String, Object> properties = new HashMap<>();

        @Override
        public void addValue(Option option, Object value) {
            List<String> list = values.get(option);
            if (list == null) {
                list = new ArrayList<>();
                values.put(option, list);
            }
            list.add(value.toString());
        }

        @Override
        public void addSwitch(Option option, boolean value) {
            if (value) {
                switches.add(option);
            } else {
                switches.remove(option);
            }
        }

        @Override
        public void 
        (String property, Object value) {
            properties.put(property, value);
        }

        @Override
        public List getValues(Option option, List defaultValues) {
            List<String> list = values.get(option);
            if (list == null || list.isEmpty()) {
                return defaultValues;
            }
            return list;
        }

        @Override
        public Boolean getSwitch(Option option, Boolean defaultValue) {
            if (switches.contains(option)) {
                return Boolean.TRUE;
            }
            return defaultValue;
        }

        @Override
        public Object getProperty(Option option, String property, Object defaultValue) {
            Object val = properties.get(property);
            return val != null ? val : defaultValue;
        }

        @Override
        public Set getOptions() {
            Set<Option> all = new HashSet<>();
            all.addAll(values.keySet());
            all.addAll(switches);
            return all;
        }

        @Override
        public Option getOption(String trigger) {
            for (Option opt : getOptions()) {
                if (opt.Triggers().contains(trigger)) {
                    return opt;
                }
            }
            return null;
        }

        @Override
        public boolean hasOption(Option option) {
            return values.containsKey(option) || switches.contains(option);
        }

        @Override
        public Map getProperties() {
            return properties;
        }
    }

    private static class DummyOption extends Option {
        private final String name;
        private final Set<String> triggers;

        public DummyOption(String name) {
            this.name = name;
            this.triggers = new HashSet<>();
            this.triggers.add(name);
        }

        @Override
        public int getId() {
            return name.hashCode();
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Set<String> Triggers() {
            return triggers;
        }

        @Override
        public void validate(WriteableCommandLine commandLine) throws OptionException {
        }

        @Override
        public void 
        (WriteableCommandLine commandLine, ListIterator iterator) throws OptionException {
        }

        @Override
        public void appendUsage(StringBuffer buffer, Set helpSettings, Comparator comparator) {
        }

        @Override
        public Option findOption(String trigger) {
            return triggers.contains(trigger) ? this : null;
        }

        @Override
        public boolean canProcess(WriteableCommandLine commandLine, String argument) {
            return triggers.contains(argument);
        }
    }

    @Before
    public void setUp() {
        cmd = new ConcreteWriteableCommandLine();
        testOption = new DummyOption("option1");
        testOption2 = new DummyOption("option2");
    }

    @Test
    public void testAddValueAndGetValues() {
        cmd.addValue(testOption, "val1");
        cmd.addValue(testOption, "val2");

        List values = cmd.getValues(testOption);
        assertNotNull(values);
        assertEquals(2, values.size());
        assertEquals("val1", values.get(0));
        assertEquals("val2", values.get(1));
    }

    @Test
    public void testGetValuesWithDefaultList() {
        List defaultList = new ArrayList();
        defaultList.add("defaultVal");

        List values = cmd.getValues(testOption, defaultList);
        assertEquals(defaultList, values);

        // Also test the single-argument overload getValues(Option)
        List emptyValues = cmd.getValues(testOption);
        assertNotNull(emptyValues);
        assertTrue(emptyValues.isEmpty());
    }

    @Test
    public void testAddSwitchAndGetSwitch() {
        assertFalse(cmd.hasOption(testOption));
        
        cmd.addSwitch(testOption, true);
        assertTrue(cmd.hasOption(testOption));
        assertTrue(cmd.getSwitch(testOption));
        assertEquals(Boolean.TRUE, cmd.getSwitch(testOption, Boolean.FALSE));

        cmd.addSwitch(testOption, false);
        assertFalse(cmd.getSwitch(testOption, false));
    }

    @Test
    public void testGetSwitchWithDefault() {
        assertNull(cmd.getSwitch(testOption));
        assertEquals(Boolean.FALSE, cmd.getSwitch(testOption, Boolean.FALSE));
    }

    @Test
    public void testPropertyMethods() {
        cmd.addProperty("propKey", "propVal");
        
        assertEquals("propVal", cmd.getProperty(testOption, "propKey", "defaultProp"));
        assertEquals("defaultProp", cmd.getProperty(testOption, "nonExistent", "defaultProp"));
        
        // Test single argument getProperty(String)
        assertEquals("propVal", cmd.getProperty("propKey"));
        assertNull(cmd.getProperty("nonExistent"));

        Map properties = cmd.getProperties();
        assertNotNull(properties);
        assertEquals(1, properties.size());
        assertEquals("propVal", properties.get("propKey"));
    }

    @Test
    public void testGetOptionsAndOption() {
        cmd.addValue(testOption, "val");
        cmd.addSwitch(testOption2, true);

        Set options = cmd.getOptions();
        assertNotNull(options);
        assertEquals(2, options.size());
        assertTrue(options.contains(testOption));
        assertTrue(options.contains(testOption2));

        assertEquals(testOption, cmd.getOption("option1"));
        assertEquals(testOption2, cmd.getOption("option2"));
        assertNull(cmd.getOption("nonExistentTrigger"));
    }
}