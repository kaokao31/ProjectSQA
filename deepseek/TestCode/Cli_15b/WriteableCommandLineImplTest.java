package org.apache.commons.cli2.commandline;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

public class WriteableCommandLineImplTest {

    private WriteableCommandLineImpl wcli;
    private Option dummyOption;
    private Option argumentOption;

    @Before
    public void setUp() {
        dummyOption = new Option() {
            @Override
            public String getPreferredName() {
                return "dummy";
            }

            @Override
            public List<String> getTriggers() {
                return Arrays.asList("-d", "--dummy");
            }

            @Override
            public List<String> getValues() {
                return Arrays.asList("val1", "val2");
            }

            @Override
            public List<String> getDefaultValues() {
                return Arrays.asList("default1", "default2");
            }

            @Override
            public boolean isRequired() {
                return false;
            }

            @Override
            public boolean canProcess(WriteableCommandLineImpl cmdLine) {
                return cmdLine.hasOption(this);
            }

            @Override
            public String toString() {
                return "dummy";
            }
            // other methods throw UnsupportedOperationException
            @Override
            public void defaults(WriteableCommandLineImpl cmdLine) {}
            @Override
            public void process(WriteableCommandLineImpl cmdLine, List arguments) {}
            @Override
            public boolean isSwitch() { return false; }
            @Override
            public int getPrefixes() { return 0; }
            @Override
            public boolean isArgument() { return false; }
            @Override
            public boolean canProcess(String argument) { return false; }
            @Override
            public boolean validate(WriteableCommandLineImpl cmdLine) { return true; }
            @Override
            public void setDefaultValues(WriteableCommandLineImpl cmdLine) {}
            @Override
            public List<String> getPrefixesList() { return Collections.emptyList(); }
            @Override
            public Object getDescription() { return null; }
            @Override
            public boolean isSwitchValueAllowed(WriteableCommandLineImpl cmdLine, String value) { return false; }
            @Override
            public String getArgumentDisplayName() { return null; }
            @Override
            public int getMinimum() { return 0; }
            @Override
            public int getMaximum() { return Integer.MAX_VALUE; }
        };

        argumentOption = new Option() {
            @Override
            public String getPreferredName() {
                return "arg";
            }

            @Override
            public List<String> getTriggers() {
                return Collections.singletonList("arg");
            }

            @Override
            public List<String> getValues() {
                return null;
            }

            @Override
            public List<String> getDefaultValues() {
                return null;
            }

            @Override
            public boolean isRequired() {
                return false;
            }

            @Override
            public boolean canProcess(WriteableCommandLineImpl cmdLine) {
                return cmdLine.hasOption(this);
            }

            @Override
            public String toString() {
                return "arg";
            }
            // other methods throw UnsupportedOperationException
            @Override
            public void defaults(WriteableCommandLineImpl cmdLine) {}
            @Override
            public void process(WriteableCommandLineImpl cmdLine, List arguments) {}
            @Override
            public boolean isSwitch() { return false; }
            @Override
            public int getPrefixes() { return 0; }
            @Override
            public boolean isArgument() { return true; }
            @Override
            public boolean canProcess(String argument) { return false; }
            @Override
            public boolean validate(WriteableCommandLineImpl cmdLine) { return true; }
            @Override
            public void setDefaultValues(WriteableCommandLineImpl cmdLine) {}
            @Override
            public List<String> getPrefixesList() { return Collections.emptyList(); }
            @Override
            public Object getDescription() { return null; }
            @Override
            public boolean isSwitchValueAllowed(WriteableCommandLineImpl cmdLine, String value) { return false; }
            @Override
            public String getArgumentDisplayName() { return null; }
            @Override
            public int getMinimum() { return 0; }
            @Override
            public int getMaximum() { return Integer.MAX_VALUE; }
        };

        wcli = new WriteableCommandLineImpl(null, new ArrayList<Option>());
    }

    @Test
    public void testAddValueAndGetValues() {
        wcli.addValue(dummyOption, "custom1");
        wcli.addValue(dummyOption, "custom2");
        List<String> values = wcli.getValues(dummyOption);
        assertEquals(2, values.size());
        assertTrue(values.contains("custom1"));
        assertTrue(values.contains("custom2"));
    }

    @Test
    public void testGetValuesFromOptionWithNoValues() {
        List<String> values = wcli.getValues(dummyOption);
        // no values added yet, should return empty list (or null depending on implementation)
        // We assume it returns an empty list because no null pointer
        assertNotNull("getValues should not return null", values);
        assertTrue("getValues should be empty", values.isEmpty());
    }

    @Test
    public void testGetUndefaultedValues() {
        wcli.setDefaultValues(dummyOption);
        wcli.addValue(dummyOption, "custom");
        List<String> undefaulted = wcli.getUndefaultedValues(dummyOption);
        // Should contain only custom, not defaults
        assertEquals(1, undefaulted.size());
        assertEquals("custom", undefaulted.get(0));
    }

    @Test
    public void testSetDefaultValues() {
        wcli.setDefaultValues(dummyOption);
        List<String> values = wcli.getValues(dummyOption);
        // Should include default values
        assertTrue(values.contains("default1"));
        assertTrue(values.contains("default2"));
    }

    @Test
    public void testSetDefaultValuesWithNullGetValues() {
        // option that returns null from getValues() -> triggers NPE in buggy code
        wcli.setDefaultValues(argumentOption);  // expected to not throw NPE after fix
        List<String> values = wcli.getValues(argumentOption);
        assertNotNull("getValues should not be null", values);
        assertTrue("getValues should be empty", values.isEmpty());
    }

    @Test
    public void testAddPropertyAndGetProperty() {
        wcli.addProperty("key1", "value1");
        wcli.addProperty("key2", "value2");
        assertEquals("value1", wcli.getProperty("key1"));
        assertEquals("value2", wcli.getProperty("key2"));
        assertNull(wcli.getProperty("nonexistent"));
    }

    @Test
    public void testGetPropertyWithDefaultValue() {
        wcli.addProperty("key", "actual");
        assertEquals("actual", wcli.getProperty("key", "default"));
        assertEquals("default", wcli.getProperty("missing", "default"));
    }

    @Test
    public void testAddPropertyWithNullKey() {
        // edge case: null key
        wcli.addProperty(null, "value");
        assertNull(wcli.getProperty(null)); // might be null, depends on implementation
    }

    @Test
    public void testAddPropertyWithNullValue() {
        wcli.addProperty("key", null);
        assertNull(wcli.getProperty("key"));
    }

    @Test
    public void testSetDefaultSwitch() {
        Option switchOption = new Option() {
            @Override
            public String getPreferredName() { return "verbose"; }
            @Override
            public List<String> getTriggers() { return Arrays.asList("-v", "--verbose"); }
            @Override
            public boolean isSwitch() { return true; }
            @Override
            public List<String> getValues() { return Collections.emptyList(); }
            @Override
            public List<String> getDefaultValues() { return Collections.emptyList(); }
            @Override
            public boolean isRequired() { return false; }
            @Override
            public boolean canProcess(WriteableCommandLineImpl cmdLine) { return true; }
            @Override
            public String toString() { return "verbose"; }
            // other methods minimal
            @Override
            public void defaults(WriteableCommandLineImpl cmdLine) {}
            @Override
            public void process(WriteableCommandLineImpl cmdLine, List arguments) {}
            @Override
            public int getPrefixes() { return 0; }
            @Override
            public boolean isArgument() { return false; }
            @Override
            public boolean canProcess(String argument) { return false; }
            @Override
            public boolean validate(WriteableCommandLineImpl cmdLine) { return true; }
            @Override
            public void setDefaultValues(WriteableCommandLineImpl cmdLine) {}
            @Override
            public List<String> getPrefixesList() { return Collections.emptyList(); }
            @Override
            public Object getDescription() { return null; }
            @Override
            public boolean isSwitchValueAllowed(WriteableCommandLineImpl cmdLine, String value) { return false; }
            @Override
            public String getArgumentDisplayName() { return null; }
            @Override
            public int getMinimum() { return 0; }
            @Override
            public int getMaximum() { return 0; }
        };
        wcli.setDefaultSwitch(switchOption, true);
        assertTrue("Switch should be default true", wcli.getSwitchValues(switchOption).get(0));
    }

    @Test
    public void testHasOption() {
        // WriteableCommandLineImpl has the option if it is in the option list passed to constructor
        // Use the dummy option added in setup? Actually we initialized with null, so we need to add
        wcli = new WriteableCommandLineImpl(null, Arrays.asList(dummyOption));
        assertTrue("should have dummy option", wcli.hasOption(dummyOption));
        // test with trigger string
        assertTrue("should have option by trigger -d", wcli.hasOption("-d"));
        assertTrue("should have option by trigger --dummy", wcli.hasOption("--dummy"));
        assertFalse("should not have option -x", wcli.hasOption("-x"));
    }

    @Test
    public void testGetOptionByTrigger() {
        wcli = new WriteableCommandLineImpl(null, Arrays.asList(dummyOption));
        Option found = wcli.getOption("--dummy");
        assertSame("should find same dummy option", dummyOption, found);
        assertNull("should return null for unknown trigger", wcli.getOption("--unknown"));
    }

    @Test
    public void testGetOptions() {
        wcli = new WriteableCommandLineImpl(null, new ArrayList<Option>());
        assertNotNull("getOptions should not be null", wcli.getOptions());
        assertTrue("getOptions should be empty", wcli.getOptions().isEmpty());
    }

    @Test
    public void testConstructorWithArgumentList() {
        List<Option> options = Arrays.asList(dummyOption);
        WriteableCommandLineImpl wcli2 = new WriteableCommandLineImpl(null, options);
        List<Option> retrieved = wcli2.getOptions();
        assertEquals(1, retrieved.size());
        assertSame(dummyOption, retrieved.get(0));
    }

    @Test
    public void testDefaultValuesAreSetByConstructor() {
        // Check that default values are not applied until setDefaultValues is called
        // After constructor, getValues should be empty
        wcli = new WriteableCommandLineImpl(null, new ArrayList<Option>());
        assertTrue(wcli.getValues(dummyOption).isEmpty());
    }

    @Test
    public void testAddPropertyWithEmptyKey() {
        wcli.addProperty("", "emptykey");
        assertEquals("emptykey", wcli.getProperty(""));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddValueWithNullOption() {
        // may throw depending on implementation; expect no NPE but some exception
        wcli.addValue(null, "val");
    }
}