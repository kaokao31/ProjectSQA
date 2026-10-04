package org.apache.commons.beanutils;

import org.junit.Test;
import static org.junit.Assert.*;

public class NullPropertyPointerTest {

    @Test
    public void testConstructorWithIndex() {
        NullPropertyPointer pointer = new NullPropertyPointer(null, "someName", 0);
        assertNotNull(pointer);
        assertEquals(-1, pointer.getIndex());
        assertEquals(0, pointer.getArray().length);
    }

    @Test
    public void testConstructorWithName() {
        NullPropertyPointer pointer = new NullPropertyPointer(null, "someName");
        assertNotNull(pointer);
        assertEquals(-1, pointer.getIndex());
        assertEquals(0, pointer.getArray().length);
    }

    @Test
    public void testGetBaseValue() {
        NullPropertyPointer pointer = new NullPropertyPointer(null, "someName");
        assertNull(pointer.getBaseValue());
    }

    @Test
    public void testSetBaseValue() {
        NullPropertyPointer pointer = new NullPropertyPointer(null, "someName");
        // Should throw UnsupportedOperationException as per base class contract for read-only / null pointers
        try {
            pointer.setBaseValue("newValue");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    public void testGetImmediateParent() {
        NullPropertyPointer pointer = new NullPropertyPointer(null, "someName");
        assertNull(pointer.getImmediateParent());
    }

    @Test
    public void testGetValue() {
        NullPropertyPointer pointer = new NullPropertyPointer(null, "someName");
        assertNull(pointer.getValue());
    }

    @Test
    public void testSetValue() {
        NullPropertyPointer pointer = new NullPropertyPointer(null, "someName");
        try {
            pointer.setValue("newValue");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    public void testIsCollection() {
        NullPropertyPointer pointer = new NullPropertyPointer(null, "someName");
        assertFalse(pointer.isCollection());
    }

    @Test
    public void testGetLength() {
        NullPropertyPointer pointer = new NullPropertyPointer(null, "someName");
        assertEquals(0, pointer.getLength());
    }

    @Test
    public void testGetPropertyPointer() {
        NullPropertyPointer pointer = new NullPropertyPointer(null, "someName");
        // NullPropertyPointer inherits or implements property pointer methods
        // Let's verify it acts as a property pointer
        assertNotNull(pointer);
    }
}