package org.apache.commons.cli2.option;

import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.DisplaySetting;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.WriteableCommandLine;
import org.apache.commons.cli2.resource.ResourceConstants;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Test suite for GroupImpl, targeting Cli-14 bug.
 */
public class GroupImplTest {

    private GroupImpl group;
    private Option optionA;
    private Option optionB;
    private Option optionC;
    private Option requiredOption;

    @Before
    public void setUp() throws Exception {
        // Create simple options for testing
        optionA = new OptionImpl(new ArrayList<String>(), false, false, 'a', "option-a", "Option A",
                new ArrayList<String>(), new ArrayList<String>(), new HashSet<DisplaySetting>()) {
            @Override
            public void process(String trigger, List<String> args, WriteableCommandLine commandLine) {
                commandLine.addOption(this);
            }

            @Override
            public List<String> getTriggers() {
                List<String> triggers = new ArrayList<String>();
                triggers.add("-a");
                return triggers;
            }

            @Override
            public boolean canProcess(final WriteableCommandLine commandLine, final String argument) {
                return argument.equals("-a");
            }

            @Override
            public void validate(final WriteableCommandLine commandLine, final Option option) throws OptionException {
            }

            @Override
            public void defaults(final WriteableCommandLine commandLine) {
            }

            @Override
            public String toString() {
                return "OptionA";
            }
        };

        optionB = new OptionImpl(new ArrayList<String>(), false, false, 'b', "option-b", "Option B",
                new ArrayList<String>(), new ArrayList<String>(), new HashSet<DisplaySetting>()) {
            @Override
            public void process(String trigger, List<String> args, WriteableCommandLine commandLine) {
                commandLine.addOption(this);
            }

            @Override
            public List<String> getTriggers() {
                List<String> triggers = new ArrayList<String>();
                triggers.add("-b");
                return triggers;
            }

            @Override
            public boolean canProcess(final WriteableCommandLine commandLine, final String argument) {
                return argument.equals("-b");
            }

            @Override
            public void validate(final WriteableCommandLine commandLine, final Option option) throws OptionException {
            }

            @Override
            public void defaults(final WriteableCommandLine commandLine) {
            }

            @Override
            public String toString() {
                return "OptionB";
            }
        };

        optionC = new OptionImpl(new ArrayList<String>(), false, false, 'c', "option-c", "Option C",
                new ArrayList<String>(), new ArrayList<String>(), new HashSet<DisplaySetting>()) {
            @Override
            public void process(String trigger, List<String> args, WriteableCommandLine commandLine) {
                commandLine.addOption(this);
            }

            @Override
            public List<String> getTriggers() {
                List<String> triggers = new ArrayList<String>();
                triggers.add("-c");
                return triggers;
            }

            @Override
            public boolean canProcess(final WriteableCommandLine commandLine, final String argument) {
                return argument.equals("-c");
            }

            @Override
            public void validate(final WriteableCommandLine commandLine, final Option option) throws OptionException {
            }

            @Override
            public void defaults(final WriteableCommandLine commandLine) {
            }

            @Override
            public String toString() {
                return "OptionC";
            }
        };

        // Creating a required option (with argument)
        requiredOption = new OptionImpl(new ArrayList<String>(), true, true, 'r', "required-option", "Required Option",
                new ArrayList<String>(), new ArrayList<String>(), new HashSet<DisplaySetting>()) {
            @Override
            public void process(String trigger, List<String> args, WriteableCommandLine commandLine) {
                commandLine.addOption(this);
                if (args.size() > 0) {
                    commandLine.addValue(this, args.get(0));
                }
            }

            @Override
            public List<String> getTriggers() {
                List<String> triggers = new ArrayList<String>();
                triggers.add("-r");
                return triggers;
            }

            @Override
            public boolean canProcess(final WriteableCommandLine commandLine, final String argument) {
                return argument.equals("-r");
            }

            @Override
            public void validate(final WriteableCommandLine commandLine, final Option option) throws OptionException {
                if (!commandLine.hasOption(this)) {
                    throw new OptionException(this, ResourceConstants.OPTION_REQUIRED);
                }
                if (commandLine.getValues(this).isEmpty()) {
                    throw new OptionException(this, ResourceConstants.MISSING_VALUE);
                }
            }

            @Override
            public void defaults(final WriteableCommandLine commandLine) {
            }

            @Override
            public String toString() {
                return "RequiredOption";
            }
        };
    }

    @Test
    public void testConstructorAndBasicAccessors() {
        List<Option> options = new ArrayList<Option>();
        options.add(optionA);
        options.add(optionB);
        // GroupImpl(List<Option> options, List<Option> required, List<Option> xor, String name, int min, int max)
        group = new GroupImpl(options, new ArrayList<Option>(), new ArrayList<Option>(), "testGroup", 0, 1);
        assertEquals("testGroup", group.getName());
        assertFalse(group.isRequired());
        assertTrue(group.canProcess(null, "-a"));
        assertTrue(group.canProcess(null, "-b"));
        assertFalse(group.canProcess(null, "-c"));
        List<Option> retrieved = group.getOptions();
        assertEquals(2, retrieved.size());
        assertTrue(retrieved.contains(optionA));
        assertTrue(retrieved.contains(optionB));
    }

    @Test
    public void testGroupIsRequired() {
        List<Option> options = new ArrayList<Option>();
        options.add(optionA);
        List<Option> required = new ArrayList<Option>();
        required.add(optionA);
        group = new GroupImpl(options, required, new ArrayList<Option>(), "requiredGroup", 1, 1);
        assertTrue(group.isRequired());
    }

    @Test
    public void testGetTriggers() {
        List<Option> options = new ArrayList<Option>();
        options.add(optionA);
        options.add(optionB);
        group = new GroupImpl(options, new ArrayList<Option>(), new ArrayList<Option>(), "triggersGroup", 0, 1);
        Set<String> triggers = group.getTriggers();
        assertTrue(triggers.contains("-a"));
        assertTrue(triggers.contains("-b"));
        assertFalse(triggers.contains("-c"));
    }

    @Test
    public void testValidateWithNoRequiredOptions() throws OptionException {
        List<Option> options = new ArrayList<Option>();
        options.add(optionA);
        options.add(optionB);
        group = new GroupImpl(options, new ArrayList<Option>(), new ArrayList<Option>(), "noRequired", 0, 2);
        // Create a command line with optionA
        WriteableCommandLine commandLine = new WriteableCommandLineImpl();
        commandLine.addOption(optionA);
        group.validate(commandLine);
        // Should not throw, even if optionB not present
    }

    @Test
    public void testValidateRequiredOptionsNotAllPresent() {
        List<Option> options = new ArrayList<Option>();
        options.add(optionA);
        options.add(optionB);
        List<Option> required = new ArrayList<Option>();
        required.add(optionA);
        required.add(optionB);
        group = new GroupImpl(options, required, new ArrayList<Option>(), "requiredGroup", 0, 2);
        WriteableCommandLine commandLine = new WriteableCommandLineImpl();
        commandLine.addOption(optionA);
        // Only one required present -> should throw
        try {
            group.validate(commandLine);
            fail("Expected OptionException because not all required options are present");
        } catch (OptionException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testValidateAllRequiredOptionsPresent() throws OptionException {
        List<Option> options = new ArrayList<Option>();
        options.add(optionA);
        options.add(optionB);
        List<Option> required = new ArrayList<Option>();
        required.add(optionA);
        required.add(optionB);
        group = new GroupImpl(options, required, new ArrayList<Option>(), "requiredGroup", 0, 2);
        WriteableCommandLine commandLine = new WriteableCommandLineImpl();
        commandLine.addOption(optionA);
        commandLine.addOption(optionB);
        // Both required present - should not throw (this targets Cli-14 bug)
        group.validate(commandLine);
    }

    @Test
    public void testValidateRequiredOptionWithMissingValue() {
        List<Option> options = new ArrayList<Option>();
        options.add(requiredOption);
        List<Option> required = new ArrayList<Option>();
        required.add(requiredOption);
        // Required option requires an argument
        group = new GroupImpl(options, required, new ArrayList<Option>(), "requiredWithValue", 0, 1);
        WriteableCommandLine commandLine = new WriteableCommandLineImpl();
        commandLine.addOption(requiredOption);
        // Option present but no value attached
        try {
            group.validate(commandLine);
            fail("Expected OptionException because required option has no value");
        } catch (OptionException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testValidateRequiredOptionWithValue() throws OptionException {
        List<Option> options = new ArrayList<Option>();
        options.add(requiredOption);
        List<Option> required = new ArrayList<Option>();
        required.add(requiredOption);
        group = new GroupImpl(options, required, new ArrayList<Option>(), "requiredWithValue", 0, 1);
        WriteableCommandLine commandLine = new WriteableCommandLineImpl();
        commandLine.addOption(requiredOption);
        commandLine.addValue(requiredOption, "value");
        // Should not throw
        group.validate(commandLine);
    }

    @Test
    public void testValidateTooFewOptions() {
        List<Option> options = new ArrayList<Option>();
        options.add(optionA);
        options.add(optionB);
        group = new GroupImpl(options, new ArrayList<Option>(), new ArrayList<Option>(), "rangeGroup", 2, 2);
        WriteableCommandLine commandLine = new WriteableCommandLineImpl();
        commandLine.addOption(optionA);
        // Only one option provided, minimum is 2 -> should throw
        try {
            group.validate(commandLine);
            fail("Expected OptionException because minimum number of options not reached");
        } catch (OptionException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testValidateTooManyOptions() {
        List<Option> options = new ArrayList<Option>();
        options.add(optionA);
        options.add(optionB);
        group = new GroupImpl(options, new ArrayList<Option>(), new ArrayList<Option>(), "rangeGroup", 0, 1);
        WriteableCommandLine commandLine = new WriteableCommandLineImpl();
        commandLine.addOption(optionA);
        commandLine.addOption(optionB);
        // Two options provided, maximum is 1 -> should throw
        try {
            group.validate(commandLine);
            fail("Expected OptionException because maximum number of options exceeded");
        } catch (OptionException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testValidateExactlyExpectedCount() throws OptionException {
        List<Option> options = new ArrayList<Option>();
        options.add(optionA);
        options.add(optionB);
        group = new GroupImpl(options, new ArrayList<Option>(), new ArrayList<Option>(), "exactGroup", 1, 1);
        WriteableCommandLine commandLine = new WriteableCommandLineImpl();
        commandLine.addOption(optionA);
        // Exactly one -> pass
        group.validate(commandLine);
    }

    @Test
    public void testGroupWithXorOptions() throws OptionException {
        List<Option> options = new ArrayList<Option>();
        options.add(optionA);
        options.add(optionB);
        List<Option> xor = new ArrayList<Option>();
        xor.add(optionA);
        xor.add(optionB);
        group = new GroupImpl(options, new ArrayList<Option>(), xor, "xorGroup", 0, 1);
        // Only one from xor set allowed
        WriteableCommandLine commandLine = new WriteableCommandLineImpl();
        commandLine.addOption(optionA);
        // Should not throw
        group.validate(commandLine);
    }

    @Test
    public void testGroupWithXorOptionsBothSelected() {
        List<Option> options = new ArrayList<Option>();
        options.add(optionA);
        options.add(optionB);
        List<Option> xor = new ArrayList<Option>();
        xor.add(optionA);
        xor.add(optionB);
        group = new GroupImpl(options, new ArrayList<Option>(), xor, "xorGroup", 0, 1);
        WriteableCommandLine commandLine = new WriteableCommandLineImpl();
        commandLine.addOption(optionA);
        commandLine.addOption(optionB);
        // Both selected -> should throw because xor set conflict
        try {
            group.validate(commandLine);
            fail("Expected OptionException because both options in xor set are present");
        } catch (OptionException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testProcessAndDefaults() {
        List<Option> options = new ArrayList<Option>();
        options.add(optionA);
        options.add(optionB);
        group = new GroupImpl(options, new ArrayList<Option>(), new ArrayList<Option>(), "processGroup", 0, 2);
        WriteableCommandLine commandLine = new WriteableCommandLineImpl();
        // Process should just add options to command line (delegates)
        group.process("-a", new ArrayList<String>(), commandLine);
        assertTrue(commandLine.hasOption(optionA));
        group.process("-b", new ArrayList<String>(), commandLine);
        assertTrue(commandLine.hasOption(optionB));
    }

    @Test
    public void testGetPreferredOption() {
        List<Option> options = new ArrayList<Option>();
        Option preferred = optionA;
        options.add(preferred);
        options.add(optionB);
        // GroupImpl with preferred option? Actually GroupImpl doesn't have setPreferred, but constructor may include it.
        // We'll just test getOption for a trigger.
        group = new GroupImpl(options, new ArrayList<Option>(), new ArrayList<Option>(), "preferred", 0, 2);
        // getOption by trigger
        Option retrieved = group.getOption("-a");
        assertSame(optionA, retrieved);
        retrieved = group.getOption("-c");
        assertNull(retrieved);
    }

    @Test
    public void testGetOptionsUnmodifiable() {
        List<Option> options = new ArrayList<Option>();
        options.add(optionA);
        group = new GroupImpl(options, new ArrayList<Option>(), new ArrayList<Option>(), "unmod", 0, 1);
        List<Option> retrieved = group.getOptions();
        try {
            retrieved.add(optionB);
            fail("Should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testGetTriggersEmptyGroup() {
        List<Option> options = new ArrayList<Option>();
        group = new GroupImpl(options, new ArrayList<Option>(), new ArrayList<Option>(), "empty", 0, 0);
        Set<String> triggers = group.getTriggers();
        assertTrue(triggers.isEmpty());
    }

    @Test
    public void testValidateWithEmptyGroup() throws OptionException {
        List<Option> options = new ArrayList<Option>();
        group = new GroupImpl(options, new ArrayList<Option>(), new ArrayList<Option>(), "empty", 0, 0);
        WriteableCommandLine commandLine = new WriteableCommandLineImpl();
        group.validate(commandLine);
        // Should not throw
    }

    // Inner class to simulate WriteableCommandLine for testing
    private static class WriteableCommandLineImpl implements WriteableCommandLine {
        private final Set<Option> options = new HashSet<Option>();
        private final java.util.Map<Option, List<String>> values = new java.util.HashMap<Option, List<String>>();

        @Override
        public void addOption(Option option) {
            options.add(option);
        }

        @Override
        public boolean hasOption(Option option) {
            return options.contains(option);
        }

        @Override
        public Option getOption(String trigger) {
            return null; // Not needed for tests
        }

        @Override
        public List<?> getValues(Option option) {
            List<String> list = values.get(option);
            return list != null ? list : new ArrayList<String>(0);
        }

        @Override
        public void addValue(Option option, Object value) {
            List<String> list = values.get(option);
            if (list == null) {
                list = new ArrayList<String>();
                values.put(option, list);
            }
            list.add(value.toString());
        }

        @Override
        public void setDefaultValues(Option option, List<?> defaults) {
        }

        @Override
        public void addProperty(String property, Object value) {
        }

        @Override
        public void addProperty(String property, Object value, Option option) {
        }

        @Override
        public Object getProperty(String property) {
            return null;
        }

        @Override
        public Set<String> getProperties() {
            return Collections.emptySet();
        }

        @Override
        public boolean looksLikeOption(String argument) {
            return argument.startsWith("-");
        }

        @Override
        public String getUndefaultedValue() {
            return null;
        }

        @Override
        public Option getCurrentOption() {
            return null;
        }

        @Override
        public void clearValues() {
        }

        @Override
        public void addProperty(String property, Object value, Option option, boolean append) {
        }

        // Not fully implemented; just enough for validation tests
    }
}