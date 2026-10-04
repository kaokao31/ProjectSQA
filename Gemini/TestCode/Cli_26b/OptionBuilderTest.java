package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class OptionBuilderTest {

    @Before
    public void setUp() {
        // OptionBuilder is a class with static methods and a static reset mechanism.
        // It's good practice to ensure state is clean, though OptionBuilder.create() resets it.
    }

    @Test
    public void testCompleteOptionBuilding() {
        Option option = OptionBuilder.withDescription("description")
                .hasArg()
                .isRequired()
                .hasArgs()
                .withArgName("argName")
                .withValueSeparator('=')
                .withType(Number.class)
                .create("a");

        assertNotNull(option);
        assertEquals("a", option.getOpt());
        assertEquals("description", option.getDescription());
        assertTrue(option.hasArg());
        assertTrue(option.isRequired());
        assertTrue(option.hasArgs());
        assertEquals("argName", option.getArgName());
        assertEquals('=', option.getValueSeparator());
        assertEquals(Number.class, option.getType());
    }

    @Test
    public void testCreateWithNoArgsAndCharOpt() {
        Option option = OptionBuilder.create('b');
        assertNotNull(option);
        assertEquals("b", option.getOpt());
        assertNull(option.getDescription());
        assertFalse(option.hasArg());
    }

    @Test
    public void testCreateWithNoArgsAndStringOpt() {
        Option option = OptionBuilder.create("c");
        assertNotNull(option);
        assertEquals("c", option.getOpt());
        assertNull(option.getDescription());
        assertFalse(option.hasArg());
    }

    @Test
    public void testCreateWithNoArgumentsMethod() {
        OptionBuilder.withDescription("test");
        OptionBuilder.hasArg(true);
        Option option = OptionBuilder.create();
        assertNotNull(option);
        assertNull(option.getOpt());
        assertEquals("test", option.getDescription());
        assertTrue(option.hasArg());
    }

    @Test
    public void testHasArgBoolean() {
        Option optionTrue = OptionBuilder.hasArg(true).create("d");
        assertTrue(optionTrue.hasArg());

        Option optionFalse = OptionBuilder.hasArg(false).create("e");
        assertFalse(optionFalse.hasArg());
    }

    @Test
    public void testHasOptionalArg() {
        Option option = OptionBuilder.hasOptionalArg().create("f");
        assertTrue(option.hasArg());
        assertTrue(option.optionalArg());
    }

    @Test
    public void testHasOptionalArgs() {
        Option option = OptionBuilder.hasOptionalArgs().create("g");
        assertTrue(option.hasArgs());
        assertTrue(option.optionalArg());
    }

    @Test
    public void testHasOptionalArgsWithNum() {
        Option option = OptionBuilder.hasOptionalArgs(3).create("h");
        assertTrue(option.hasArgs());
        assertEquals(3, option.getArgs());
        assertTrue(option.optionalArg());
    }

    @Test
    public void testWithArgs() {
        Option option = OptionBuilder.hasArgs(5).create("i");
        assertTrue(option.hasArgs());
        assertEquals(5, option.getArgs());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateNullOpt() {
        // Depending on CLI version, passing null or invalid characters might throw exception or handle it
        OptionBuilder.create((String) null);
    }

    @Test
    public void testResetState() {
        // Verify that after create(), the internal state is reset so next option doesn't inherit old values
        Option option1 = OptionBuilder.withDescription("desc1")
                .isRequired()
                .create("opt1");
        assertTrue(option1.isRequired());
        assertEquals("desc1", option1.getDescription());

        Option option2 = OptionBuilder.create("opt2");
        assertFalse(option2.isRequired());
        assertNull(option2.getDescription());
    }
}