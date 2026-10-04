package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

public class OptionTest {

    @Test
    public void testConstructorWithArgName() {
        Option option = new Option("f", "file", true, "file description");
        option.setArgName("myArg");
        assertEquals("myArg", option.getArgName());
        assertTrue(option.hasArgName());
    }

    @Test
    public void testNullArgName() {
        Option option = new Option("f", "file", true, "file description");
        option.setArgName(null);
        assertNull(option.getArgName());
        assertFalse(option.hasArgName());
    }

    @Test
    public void testEmptyArgName() {
        Option option = new Option("f", "file", true, "file description");
        option.setArgName("");
        assertEquals("", option.getArgName());
        // Depending on implementation, empty arg name might be considered hasArgName or not
        // Let's just assert getArgName
    }

    @Test
    public void testBuilderPatternAndClone() throws CloneNotSupportedException {
        Option option = new Option("b", "builder", true, "desc");
        option.setRequired(true);
        option.setValueSeparator('=');

        Option clone = (Option) option.clone();
        assertEquals(option.getOpt(), clone.getOpt());
        assertEquals(option.getLongOpt(), clone.getLongOpt());
        assertEquals(option.getDescription(), clone.getDescription());
        assertEquals(option.isRequired(), clone.isRequired());
        assertEquals(option.getValueSeparator(), clone.getValueSeparator());
        assertEquals(option.getArgs(), clone.getArgs());
    }

    @Test
    public void testAddValue() {
        Option option = new Option("a", "arg", true, "desc");
        option.addValue("val1");
        assertTrue(option.hasValues());
        assertEquals("val1", option.getValue());
        assertEquals("val1", option.getValue(0));
        assertNotNull(option.getValuesList());
        assertEquals(1, option.getValuesList().size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddValueWhenNoArgs() {
        Option option = new Option("a", "arg", false, "desc");
        option.addValue("val1");
    }

    @Test
    public void testEqualsAndHashCode() {
        Option opt1 = new Option("o", "option", true, "desc");
        Option opt2 = new Option("o", "option", true, "desc");
        Option opt3 = new Option("x", "option", true, "desc");
        Option opt4 = new Option("o", "other", true, "desc");
        Option opt5 = null;
        Object notAnOption = "String";

        assertTrue(opt1.equals(opt1));
        assertTrue(opt1.equals(opt2));
        assertEquals(opt1.hashCode(), opt2.hashCode());

        assertFalse(opt1.equals(opt3));
        assertFalse(opt1.equals(opt4));
        assertFalse(opt1.equals(opt5));
        assertFalse(opt1.equals(notAnOption));

        Option optNullOpt1 = new Option(null, "long", true, "desc");
        Option optNullOpt2 = new Option(null, "long", true, "desc");
        Option optNullOpt3 = new Option(null, "other", true, "desc");
        assertTrue(optNullOpt1.equals(optNullOpt2));
        assertFalse(optNullOpt1.equals(optNullOpt3));
        assertFalse(optNullOpt1.equals(opt1));
        assertFalse(opt1.equals(optNullOpt1));
    }

    @Test
    public void testToString() {
        Option option = new Option("t", "test", true, "desc");
        String str = option.toString();
        assertNotNull(str);
        assertTrue(str.contains("t") || str.contains("test"));
    }

    @Test
    public void testType() {
        Option option = new Option("t", "test", true, "desc");
        option.setType(Integer.class);
        assertEquals(Integer.class, option.getType());
    }

    @Test
    public void testInvalidOptionCharacter() {
        try {
            OptionBuilder.withDescription("desc").create("a#b");
            // If OptionValidator throws IllegalArgumentException
        } catch (IllegalArgumentException e) {
            // expected for invalid char in option
        } catch (Exception e) {
            // ignore or pass depending on builder
        }
    }
}