package org.apache.commons.jxpath.ri.compiler;

import org.junit.Test;
import static org.junit.Assert.*;

public class CoreFunctionTest {

    // Test string-length function
    @Test
    public void testStringLengthEmpty() {
        assertEquals(0, CoreFunction.stringLength(""));
    }

    @Test
    public void testStringLengthNull() {
        assertEquals(0, CoreFunction.stringLength(null));
    }

    @Test
    public void testStringLengthNormal() {
        assertEquals(5, CoreFunction.stringLength("Hello"));
    }

    // Test concat function
    @Test
    public void testConcat() {
        assertEquals("HelloWorld", CoreFunction.concat("Hello", "World"));
    }

    @Test
    public void testConcatWithNull() {
        assertEquals("Hello", CoreFunction.concat("Hello", null));
    }

    // Test contains function
    @Test
    public void testContainsTrue() {
        assertTrue(CoreFunction.contains("Hello World", "World"));
    }

    @Test
    public void testContainsFalse() {
        assertFalse(CoreFunction.contains("Hello World", "xyz"));
    }

    @Test
    public void testContainsEmptySubstring() {
        assertTrue(CoreFunction.contains("Hello", ""));
    }

    // Test substring function
    @Test
    public void testSubstring() {
        assertEquals("ell", CoreFunction.substring("Hello", 2, 4));
    }

    @Test
    public void testSubstringStartBeyondLength() {
        assertEquals("", CoreFunction.substring("Hello", 10, 20));
    }

    // Test string function (converts to string)
    @Test
    public void testString() {
        assertEquals("123", CoreFunction.string(123));
    }

    @Test
    public void testStringNull() {
        assertEquals("", CoreFunction.string(null));
    }

    // Test number function
    @Test
    public void testNumber() {
        assertEquals(3.14, CoreFunction.number("3.14"), 1e-9);
    }

    @Test
    public void testNumberNaN() {
        assertTrue(Double.isNaN(CoreFunction.number("abc")));
    }

    // Test boolean function
    @Test
    public void testBooleanTrue() {
        assertTrue(CoreFunction.booleanValue("true"));
    }

    @Test
    public void testBooleanFalse() {
        assertFalse(CoreFunction.booleanValue("false"));
    }

    // Test sum function
    @Test
    public void testSum() {
        assertEquals(6.0, CoreFunction.sum(new double[]{1,2,3}), 1e-9);
    }

    @Test
    public void testSumEmpty() {
        assertEquals(0.0, CoreFunction.sum(new double[]{}), 1e-9);
    }

    // Test count function
    @Test
    public void testCount() {
        assertEquals(3, CoreFunction.count(new Object[]{1,2,3}));
    }

    @Test
    public void testCountEmpty() {
        assertEquals(0, CoreFunction.count(new Object[]{}));
    }
}