package org.apache.commons.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class for OptionBuilder.
 * Designed to achieve high coverage and detect the known bug in Defects4J Cli 26
 * (type field not reset between option creations).
 */
public class OptionBuilderTest {

    @Before
    public void setUp() {
        // Reset the builder state before each test to ensure isolation
        OptionBuilder.reset();
    }

    @Test
    public void testBasicOptionWithShortOpt() {
        Option opt = OptionBuilder.create("a");
        assertNotNull("Option should not be null", opt);
        assertEquals("Short option should be 'a'", "a", opt.getOpt());
        assertNull("Long option should be null", opt.getLongOpt());
        assertFalse("Should not be required", opt.isRequired());
        assertNull("Type should be null", opt.getType());
        assertEquals("Description should be null", null, opt.getDescription());
    }

    @Test
    public void testOptionWithLongOpt() {
        Option opt = OptionBuilder.withLongOpt("long-option").create("l");
        assertEquals("Long option should be 'long-option'", "long-option", opt.getLongOpt());
    }

    @Test
    public void testOptionWithDescription() {
        Option opt = OptionBuilder.withDescription("A test option").create("d");
        assertEquals("Description should match", "A test option", opt.getDescription());
    }

    @Test
    public void testOptionWithNullDescription() {
        Option opt = OptionBuilder.withDescription(null).create("n");
        assertNull("Description should be null", opt.getDescription());
    }

    @Test
    public void testOptionWithEmptyDescription() {
        Option opt = OptionBuilder.withDescription("").create("e");
        assertEquals("Description should be empty", "", opt.getDescription());
    }

    @Test
    public void testOptionRequired() {
        Option opt = OptionBuilder.isRequired().create("r");
        assertTrue("Option should be required", opt.isRequired());
    }

    @Test
    public void testOptionNotRequired() {
        Option opt = OptionBuilder.create("nr");
        assertFalse("Option should not be required", opt.isRequired());
    }

    @Test
    public void testOptionWithArgName() {
        Option opt = OptionBuilder.withArgName("arg").hasArg().create("a");
        assertTrue("Option should have an argument", opt.hasArg());
        assertEquals("Arg name should be 'arg'", "arg", opt.getArgName());
    }

    @Test
    public void testOptionWithType() {
        Option opt = OptionBuilder.withType(Integer.class).create("t");
        assertEquals("Type should be Integer", Integer.class, opt.getType());
    }

    @Test
    public void testOptionWithTypeAndReset() {
        // First option with type
        Option opt1 = OptionBuilder.withType(Integer.class).create("t1");
        assertEquals("First option type should be Integer", Integer.class, opt1.getType());

        // Second option without explicit type (should be null after reset)
        Option opt2 = OptionBuilder.create("t2");
        assertNull("Second option type should be null (reset)", opt2.getType());
    }

    @Test
    public void testOptionWithValueSeparator() {
        Option opt = OptionBuilder.withValueSeparator(',').hasArgs().create("s");
        assertTrue("Option should have args", opt.hasArgs());
        assertEquals("Value separator should be ','", ',', opt.getValueSeparator());
    }

    @Test
    public void testOptionWithArgs() {
        Option opt = OptionBuilder.hasArgs().create("m");
        assertTrue("Option should have multiple args", opt.hasArgs());
        assertTrue("Option should have an argument", opt.hasArg());
    }

    @Test
    public void testOptionWithOptionalArg() {
        Option opt = OptionBuilder.hasOptionalArg().create("o");
        assertTrue("Option should have optional arg", opt.hasOptionalArg());
    }

    @Test
    public void testOptionWithOptionalArgs() {
        Option opt = OptionBuilder.hasOptionalArgs().create("oa");
        assertTrue("Option should have optional args", opt.hasOptionalArg());
        assertTrue("Option should have multiple args", opt.hasArgs());
    }

    @Test
    public void testOptionWithOptionalArgsCount() {
        Option opt = OptionBuilder.hasOptionalArgs(3).create("oc");
        assertTrue("Option should have optional args", opt.hasOptionalArg());
        assertEquals("Maximum args should be 3", 3, opt.getArgs());
    }

    @Test
    public void testOptionWithArgsCount() {
        Option opt = OptionBuilder.hasArgs(5).create("ac");
        assertEquals("Maximum args should be 5", 5, opt.getArgs());
    }

    @Test
    public void testOptionWithLongOptAndShortOpt() {
        Option opt = OptionBuilder.withLongOpt("long").create("s");
        assertEquals("Short opt should be 's'", "s", opt.getOpt());
        assertEquals("Long opt should be 'long'", "long", opt.getLongOpt());
    }

    @Test
    public void testMultipleOptionsInSequence() {
        Option opt1 = OptionBuilder.withDescription("first").isRequired().create("1");
        assertTrue("First option should be required", opt1.isRequired());
        assertEquals("First description", "first", opt1.getDescription());

        Option opt2 = OptionBuilder.withDescription("second").create("2");
        assertFalse("Second option should not be required", opt2.isRequired());
        assertEquals("Second description", "second", opt2.getDescription());
    }

    @Test
    public void testResetClearsAllFields() {
        // Set various fields
        OptionBuilder.withLongOpt("long");
        OptionBuilder.withDescription("desc");
        OptionBuilder.isRequired();
        OptionBuilder.withArgName("arg");
        OptionBuilder.hasArg();
        OptionBuilder.withType(Integer.class);
        OptionBuilder.withValueSeparator(':');

        // Reset
        OptionBuilder.reset();

        // Create an option and verify defaults
        Option opt = OptionBuilder.create("r");
        assertNull("Long opt should be null after reset", opt.getLongOpt());
        assertNull("Description should be null after reset", opt.getDescription());
        assertFalse("Required should be false after reset", opt.isRequired());
        assertEquals("Arg name should be default", "arg", opt.getArgName()); // default arg name is "arg"
        assertFalse("Has arg should be false after reset", opt.hasArg());
        assertNull("Type should be null after reset", opt.getType());
        assertEquals("Value separator should be default", (char) 0, opt.getValueSeparator());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithNullOpt() {
        OptionBuilder.create(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithEmptyOpt() {
        OptionBuilder.create("");
    }

    @Test
    public void testDeprecatedWithArgs() {
        // hasArgs() is not deprecated, but withArgs() is? Actually, withArgs() is deprecated.
        // We'll test it for coverage.
        Option opt = OptionBuilder.withArgs(2).create("da");
        assertEquals("Args should be 2", 2, opt.getArgs());
    }

    @Test
    public void testDeprecatedHasArgs() {
        // hasArgs() is not deprecated, but we test it.
        Option opt = OptionBuilder.hasArgs().create("ha");
        assertTrue("Should have args", opt.hasArgs());
    }

    @Test
    public void testOptionWithTypeAndNoArg() {
        // Type set but no arg - should still have type
        Option opt = OptionBuilder.withType(Float.class).create("f");
        assertEquals("Type should be Float", Float.class, opt.getType());
        assertFalse("Should not have arg", opt.hasArg());
    }

    @Test
    public void testOptionWithArgNameAndNoArg() {
        // Setting arg name without hasArg should not affect hasArg
        Option opt = OptionBuilder.withArgName("file").create("fn");
        assertFalse("Should not have arg", opt.hasArg());
        // Arg name is stored even if no arg? In OptionBuilder, argName is set regardless.
        // But Option's getArgName() returns the arg name only if hasArg? Actually, Option stores argName always.
        // We'll just check it's set.
        assertEquals("Arg name should be 'file'", "file", opt.getArgName());
    }

    @Test
    public void testOptionWithValueSeparatorAndNoArgs() {
        // Setting value separator without hasArgs should not cause issues
        Option opt = OptionBuilder.withValueSeparator(';').create("vs");
        assertEquals("Value separator should be ';'", ';', opt.getValueSeparator());
        assertFalse("Should not have args", opt.hasArgs());
    }

    @Test
    public void testOptionWithLongOptOnly() {
        // Option with only long option (no short opt) - but create requires a short opt.
        // This is not possible via OptionBuilder.create(String). So we skip.
    }

    @Test
    public void testBuilderChaining() {
        Option opt = OptionBuilder.withLongOpt("chain")
                .withDescription("chained")
                .isRequired()
                .hasArg()
                .withArgName("value")
                .withType(String.class)
                .create("c");
        assertEquals("Long opt", "chain", opt.getLongOpt());
        assertEquals("Description", "chained", opt.getDescription());
        assertTrue("Required", opt.isRequired());
        assertTrue("Has arg", opt.hasArg());
        assertEquals("Arg name", "value", opt.getArgName());
        assertEquals("Type", String.class, opt.getType());
    }

    @Test
    public void testCreateWithSpecialCharacters() {
        Option opt = OptionBuilder.create("x-y");
        assertEquals("Short opt should be 'x-y'", "x-y", opt.getOpt());
    }

    @Test
    public void testCreateWithNumericOpt() {
        Option opt = OptionBuilder.create("1");
        assertEquals("Short opt should be '1'", "1", opt.getOpt());
    }
}