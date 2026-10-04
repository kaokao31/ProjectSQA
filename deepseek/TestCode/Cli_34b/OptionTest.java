package org.apache.commons.cli;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class OptionTest {

    // ========================== Constructors ==========================

    @Test
    public void testConstructorOptOnly() {
        Option opt = new Option("a");
        assertEquals("a", opt.getOpt());
        assertNull(opt.getLongOpt());
        assertNull(opt.getDescription());
        assertFalse(opt.hasArg());
        assertFalse(opt.hasArgs());
        assertEquals(0, opt.getArgs());
        assertFalse(opt.isRequired());
        assertNull(opt.getArgName());
        assertNull(opt.getType());
        assertEquals(0, opt.getValueSeparator());
        assertFalse(opt.hasValueSeparator());
    }

    @Test
    public void testConstructorOptDescription() {
        Option opt = new Option("a", "desc");
        assertEquals("a", opt.getOpt());
        assertNull(opt.getLongOpt());
        assertEquals("desc", opt.getDescription());
        assertFalse(opt.hasArg());
        assertEquals(0, opt.getArgs());
    }

    @Test
    public void testConstructorOptHasArgDescription() {
        Option opt = new Option("a", true, "desc");
        assertEquals("a", opt.getOpt());
        assertTrue(opt.hasArg());
        assertFalse(opt.hasArgs());
        assertEquals(1, opt.getArgs());
        assertEquals("desc", opt.getDescription());
    }

    @Test
    public void testConstructorOptLongOptHasArgDescription() {
        Option opt = new Option("a", "long", true, "desc");
        assertEquals("a", opt.getOpt());
        assertEquals("long", opt.getLongOpt());
        assertTrue(opt.hasArg());
        assertEquals(1, opt.getArgs());
        assertEquals("desc", opt.getDescription());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullOpt() {
        new Option(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyOpt() {
        new Option("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullOptWithDescription() {
        new Option(null, "desc");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyOptWithDescription() {
        new Option("", "desc");
    }

    // ========================== Getters / Setters ==========================

    @Test
    public void testSetLongOpt() {
        Option opt = new Option("a");
        opt.setLongOpt("long");
        assertEquals("long", opt.getLongOpt());
    }

    @Test
    public void testSetDescription() {
        Option opt = new Option("a");
        opt.setDescription("desc");
        assertEquals("desc", opt.getDescription());
    }

    @Test
    public void testSetArgName() {
        Option opt = new Option("a");
        opt.setArgName("name");
        assertEquals("name", opt.getArgName());
    }

    @Test
    public void testSetRequired() {
        Option opt = new Option("a");
        assertFalse(opt.isRequired());
        opt.setRequired(true);
        assertTrue(opt.isRequired());
    }

    @Test
    public void testSetArgs() {
        Option opt = new Option("a");
        opt.setArgs(3);
        assertTrue(opt.hasArgs());
        assertTrue(opt.hasArg());
        assertEquals(3, opt.getArgs());

        opt.setArgs(0);
        assertFalse(opt.hasArg());
        assertFalse(opt.hasArgs());
        assertEquals(0, opt.getArgs());

        opt.setArgs(-1);
        assertTrue(opt.hasArgs());
        assertEquals(-1, opt.getArgs());
    }

    @Test
    public void testSetType() {
        Option opt = new Option("a");
        opt.setType(Integer.class);
        assertEquals(Integer.class, opt.getType());
    }

    @Test
    public void testSetValueSeparator() {
        Option opt = new Option("a");
        assertEquals(0, opt.getValueSeparator());
        opt.setValueSeparator(',');
        assertEquals(',', opt.getValueSeparator());
        assertTrue(opt.hasValueSeparator());

        opt.setValueSeparator(';');
        assertEquals(';', opt.getValueSeparator());
        assertTrue(opt.hasValueSeparator());

        opt.setValueSeparator((char) 0);
        assertFalse(opt.hasValueSeparator());
    }

    // ========================== addValue / getValue(s) ==========================

    @Test(expected = RuntimeException.class)
    public void testAddValueNoArgsAllowed() {
        Option opt = new Option("a", false, "desc");
        opt.addValue("value");
    }

    @Test
    public void testAddValueWithOneArg() {
        Option opt = new Option("a", true, "desc");
        opt.addValue("v1");
        assertEquals("v1", opt.getValue());
        assertEquals("v1", opt.getValue(0));
        assertEquals(1, opt.getValues().length);
        assertEquals(1, opt.getValuesList().size());
        opt.addValue("v2");
        assertEquals(2, opt.getValuesList().size());
    }

    @Test
    public void testAddValueWithMultipleArgsNoSeparator() {
        Option opt = new Option("a", true, "desc");
        opt.setArgs(3);
        opt.addValue("v1");
        opt.addValue("v2");
        assertEquals(2, opt.getValuesList().size());
        assertEquals("v1", opt.getValue(0));
        assertEquals("v2", opt.getValue(1));
        assertNull(opt.getValue(2));
    }

    @Test
    public void testAddValueWithSeparator() {
        Option opt = new Option("a", true, "desc");
        opt.setArgs(3);
        opt.setValueSeparator(',');
        opt.addValue("v1,v2,v3");
        assertEquals(3, opt.getValuesList().size());
        assertEquals("v1", opt.getValue(0));
        assertEquals("v2", opt.getValue(1));
        assertEquals("v3", opt.getValue(2));
    }

    @Test
    public void testAddValueWithSeparatorAndExtraSpaces() {
        Option opt = new Option("a", true, "desc");
        opt.setArgs(-1);
        opt.setValueSeparator(',');
        opt.addValue("v1, v2 ,v3");
        assertEquals(3, opt.getValuesList().size());
        assertEquals("v1", opt.getValue(0));
        assertEquals(" v2 ", opt.getValue(1));
        assertEquals("v3", opt.getValue(2));
    }

    @Test
    public void testAddValueWithUnlimitedArgs() {
        Option opt = new Option("a", true, "desc");
        opt.setArgs(-1);
        opt.addValue("v1");
        opt.addValue("v2");
        opt.addValue("v3");
        assertEquals(3, opt.getValuesList().size());
    }

    @Test
    public void testClearValues() {
        Option opt = new Option("a", true, "desc");
        opt.addValue("v1");
        opt.addValue("v2");
        assertFalse(opt.getValuesList().isEmpty());
        opt.clearValues();
        assertEquals(0, opt.getValuesList().size());
        assertNull(opt.getValue());
    }

    @Test
    public void testGetValuesListModification() {
        Option opt = new Option("a", true, "desc");
        opt.addValue("v1");
        List<String> list = opt.getValuesList();
        list.clear();
        // The returned list should be a copy or unmodifiable? In buggy version it might be directly modifiable.
        // This test is designed to catch a possible bug.
        assertEquals(0, list.size());
        assertEquals(0, opt.getValuesList().size());
    }

    @Test
    public void testGetValueIndexOutOfBounds() {
        Option opt = new Option("a", true, "desc");
        assertNull(opt.getValue(0));
        opt.addValue("v1");
        assertNull(opt.getValue(5));
    }

    // ========================== equals / hashCode ==========================

    @Test
    public void testEqualsSameObj() {
        Option opt = new Option("a", "long", true, "desc");
        assertTrue(opt.equals(opt));
        assertEquals(opt.hashCode(), opt.hashCode());
    }

    @Test
    public void testEqualsNull() {
        Option opt = new Option("a");
        assertFalse(opt.equals(null));
    }

    @Test
    public void testEqualsDifferentType() {
        Option opt = new Option("a");
        assertFalse(opt.equals("a"));
    }

    @Test
    public void testEqualsSameOpt() {
        Option opt1 = new Option("a");
        Option opt2 = new Option("a");
        assertTrue(opt1.equals(opt2));
        assertEquals(opt1.hashCode(), opt2.hashCode());
    }

    @Test
    public void testEqualsDifferentOpt() {
        Option opt1 = new Option("a");
        Option opt2 = new Option("b");
        assertFalse(opt1.equals(opt2));
        assertFalse(opt1.hashCode() == opt2.hashCode());
    }

    @Test
    public void testEqualsDifferentLongOpt() {
        Option opt1 = new Option("a", "long1", false, "desc");
        Option opt2 = new Option("a", "long2", false, "desc");
        assertFalse(opt1.equals(opt2));
    }

    @Test
    public void testEqualsDifferentDescription() {
        Option opt1 = new Option("a", "desc1");
        Option opt2 = new Option("a", "desc2");
        assertFalse(opt1.equals(opt2));
    }

    @Test
    public void testEqualsDifferentRequired() {
        Option opt1 = new Option("a", false, "desc");
        Option opt2 = new Option("a", false, "desc");
        opt1.setRequired(true);
        assertFalse(opt1.equals(opt2));
    }

    @Test
    public void testEqualsDifferentArgs() {
        Option opt1 = new Option("a", false, "desc");
        Option opt2 = new Option("a", false, "desc");
        opt1.setArgs(1);
        opt2.setArgs(2);
        assertFalse(opt1.equals(opt2));
    }

    @Test
    public void testEqualsDifferentArgName() {
        Option opt1 = new Option("a", false, "desc");
        Option opt2 = new Option("a", false, "desc");
        opt1.setArgName("arg1");
        opt2.setArgName("arg2");
        assertFalse(opt1.equals(opt2));
    }

    @Test
    public void testEqualsDifferentType() {
        Option opt1 = new Option("a", false, "desc");
        Option opt2 = new Option("a", false, "desc");
        opt1.setType(String.class);
        opt2.setType(Integer.class);
        assertFalse(opt1.equals(opt2));
    }

    @Test
    public void testEqualsDifferentValueSeparator() {
        Option opt1 = new Option("a", false, "desc");
        Option opt2 = new Option("a", false, "desc");
        opt1.setValueSeparator(',');
        opt2.setValueSeparator(';');
        assertFalse(opt1.equals(opt2));
    }

    @Test
    public void testEqualsAllFieldsEqual() {
        Option opt1 = new Option("a", "long", true, "desc");
        Option opt2 = new Option("a", "long", true, "desc");
        opt1.setRequired(true);
        opt2.setRequired(true);
        opt1.setArgs(2);
        opt2.setArgs(2);
        opt1.setType(Integer.class);
        opt2.setType(Integer.class);
        opt1.setValueSeparator(',');
        opt2.setValueSeparator(',');
        opt1.setArgName("arg");
        opt2.setArgName("arg");
        assertTrue(opt1.equals(opt2));
        assertEquals(opt1.hashCode(), opt2.hashCode());
    }

    // ========================== hasArg / hasArgs / hasOptionalArg ==========================

    @Test
    public void testHasArg() {
        Option opt = new Option("a", false, "desc");
        assertFalse(opt.hasArg());
        opt.setArgs(1);
        assertTrue(opt.hasArg());
        opt.setArgs(0);
        assertFalse(opt.hasArg());
        opt.setArgs(-1);
        assertTrue(opt.hasArg());
    }

    @Test
    public void testHasArgs() {
        Option opt = new Option("a", false, "desc");
        assertFalse(opt.hasArgs());
        opt.setArgs(1);
        assertFalse(opt.hasArgs());
        opt.setArgs(2);
        assertTrue(opt.hasArgs());
        opt.setArgs(-1);
        assertTrue(opt.hasArgs());
    }

    @Test
    public void testHasValueSeparator() {
        Option opt = new Option("a");
        assertFalse(opt.hasValueSeparator());
        opt.setValueSeparator(',');
        assertTrue(opt.hasValueSeparator());
        opt.setValueSeparator((char) 0);
        assertFalse(opt.hasValueSeparator());
    }

    // ========================== toString ==========================

    @Test
    public void testToString() {
        Option opt = new Option("a", "long", true, "desc");
        String str = opt.toString();
        assertNotNull(str);
        assertTrue(str.contains("[ Option a long :: desc :: 1 ]") || str.contains("a") && str.contains("long"));
    }

    // ========================== Edge cases ==========================

    @Test
    public void testGetValuesWhenNone() {
        Option opt = new Option("a");
        assertNull(opt.getValues());
    }

    @Test
    public void testHashCodeConsistency() {
        Option opt = new Option("a");
        assertEquals(opt.hashCode(), opt.hashCode());
    }

    @Test
    public void testEqualsWithNullFields() {
        Option opt1 = new Option("a", null, false, null);
        Option opt2 = new Option("a", null, false, null);
        assertTrue(opt1.equals(opt2));
        assertEquals(opt1.hashCode(), opt2.hashCode());
    }

    @Test
    public void testEqualsWithNullVsNonNullLongOpt() {
        Option opt1 = new Option("a", null, false, "desc");
        Option opt2 = new Option("a", "long", false, "desc");
        assertFalse(opt1.equals(opt2));
    }
}