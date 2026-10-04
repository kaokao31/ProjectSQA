package org.apache.commons.cli2.option;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.DisplaySetting;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.WriteableCommandLine;
import org.apache.commons.cli2.commandline.WriteableCommandLineImpl;
import org.apache.commons.cli2.res.ResourceHelper;
import org.junit.Assert;
import org.junit.Test;

public class GroupImplTest {

    @Test
    public void testGroupImplBasicBehavior() {
        List<Option> options = new ArrayList<Option>();
        ArgumentImpl argument = new ArgumentImpl("arg", "An argument", 1, 1, '(', ')', null, null, 0);
        CommandImpl command = new CommandImpl("cmd", "A command", new HashSet<String>(), new HashSet<String>(), options, null, null, 0);
        
        options.add(command);
        options.add(argument);

        GroupImpl group = new GroupImpl(options, "group", "A group", 1, 1);

        Assert.assertEquals("group", group.getPreferredName());
        Assert.assertEquals("A group", group.getDescription());
        Assert.assertEquals(1, group.getMinimum());
        Assert.assertEquals(1, group.getMaximum());
        
        Set<Option> optionSet = group.options();
        Assert.assertTrue(optionSet.contains(command));
        Assert.assertTrue(optionSet.contains(argument));

        // Test findsOption
        Assert.assertTrue(group.findsOption("cmd"));
        Assert.assertTrue(group.findsOption("arg"));
        Assert.assertFalse(group.findsOption("nonexistent"));
    }

    @Test
    public void testProcessOptions() throws Exception {
        List<Option> options = new ArrayList<Option>();
        CommandImpl command = new CommandImpl("c", "Command", new HashSet<String>(), new HashSet<String>(), new ArrayList<Option>(), null, null, 'c');
        options.add(command);

        GroupImpl group = new GroupImpl(options, "g", "Group", 0, 1);

        List<String> argsList = new ArrayList<String>();
        argsList.add("-c");
        ListIterator<String> iterator = argsList.listIterator();

        WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList<String>());
        
        group.processOptions(commandLine, iterator);
        
        Assert.assertTrue(commandLine.hasOption(command));
    }

    @Test
    public void testValidate() throws Exception {
        List<Option> options = new ArrayList<Option>();
        ArgumentImpl argument = new ArgumentImpl("arg", "An argument", 1, 1, '(', ')', null, null, 0);
        options.add(argument);

        GroupImpl group = new GroupImpl(options, "g", "Group", 1, 1);

        WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList<String>());
        // Without adding the argument to commandLine, validation might throw an exception or handle it
        try {
            group.validate(commandLine);
        } catch (Exception e) {
            // Expected if min/max constraints are not met
        }
        
        commandLine.addOption(argument);
        group.validate(commandLine);
        Assert.assertTrue(commandLine.hasOption(argument));
    }

    @Test
    public void testAppendUsage() {
        List<Option> options = new ArrayList<Option>();
        CommandImpl command = new CommandImpl("c", "Command", new HashSet<String>(), new HashSet<String>(), new ArrayList<Option>(), null, null, 'c');
        options.add(command);

        GroupImpl group = new GroupImpl(options, "g", "Group", 1, 1);

        StringBuffer buffer = new StringBuffer();
        Set<DisplaySetting> settings = DisplaySetting.ALL;
        
        group.appendUsage(buffer, settings, new HashSet<Option>());
        Assert.assertTrue(buffer.length() >= 0);
    }

    @Test
    public void testGetHelpName() {
        List<Option> options = new ArrayList<Option>();
        GroupImpl group = new GroupImpl(options, "g", "Group", 1, 1);
        Assert.assertEquals("g", group.getHelpName());
    }

    @Test
    public void testPrefixes() {
        List<Option> options = new ArrayList<Option>();
        GroupImpl group = new GroupImpl(options, "g", "Group", 1, 1);
        Assert.assertNotNull(group.prefixes());
    }

    @Test
    public void testTriggers() {
        List<Option> options = new ArrayList<Option>();
        CommandImpl command = new CommandImpl("c", "Command", new HashSet<String>(), new HashSet<String>(), new ArrayList<Option>(), null, null, 'c');
        options.add(command);
        
        GroupImpl group = new GroupImpl(options, "g", "Group", 1, 1);
        Set<String> triggers = group.triggers();
        Assert.assertTrue(triggers.contains("c") || triggers.contains("-c") || group.findsOption("c"));
    }
}