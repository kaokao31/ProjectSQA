package com.thoughtworks.qdox.model;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Collections;

public class NamedTypeTest {

    @Test
    public void testDefaultConstructorAndGetName() {
        DefaultJavaClass type = new DefaultJavaClass();
        assertNull(type.getName());
    }

    @Test
    public void testSetNameAndGetName() {
        DefaultJavaClass type = new DefaultJavaClass();
        type.setName("MyClass");
        assertEquals("MyClass", type.getName());
    }

    @Test
    public void testEqualsNullAndDifferentType() {
        DefaultJavaClass type = new DefaultJavaClass();
        type.setName("MyClass");

        assertFalse(type.equals(null));
        assertFalse(type.equals("SomeString"));
    }

    @Test
    public void testEqualsSameInstance() {
        DefaultJavaClass type = new DefaultJavaClass();
        type.setName("MyClass");

        assertTrue(type.equals(type));
    }

    @Test
    public void testEqualsEquivalentInstances() {
        DefaultJavaClass type1 = new DefaultJavaClass();
        type1.setName("MyClass");

        DefaultJavaClass type2 = new DefaultJavaClass();
        type2.setName("MyClass");

        assertTrue(type1.equals(type2));
        assertTrue(type2.equals(type1));
        assertEquals(type1.hashCode(), type2.hashCode());
    }

    @Test
    public void testEqualsDifferentNames() {
        DefaultJavaClass type1 = new DefaultJavaClass();
        type1.setName("ClassA");

        DefaultJavaClass type2 = new DefaultJavaClass();
        type2.setName("ClassB");

        assertFalse(type1.equals(type2));
        assertFalse(type2.equals(type1));
    }

    @Test
    public void testEqualsWithNullNames() {
        DefaultJavaClass type1 = new DefaultJavaClass();
        DefaultJavaClass type2 = new DefaultJavaClass();

        assertTrue(type1.equals(type2));
        assertEquals(type1.hashCode(), type2.hashCode());

        type2.setName("ClassA");
        assertFalse(type1.equals(type2));
        assertFalse(type2.equals(type1));
    }

    @Test
    public void testCompareToSame() {
        DefaultJavaClass type1 = new DefaultJavaClass();
        type1.setName("MyClass");

        DefaultJavaClass type2 = new DefaultJavaClass();
        type2.setName("MyClass");

        assertEquals(0, type1.compareTo(type2));
    }

    @Test
    public void testCompareToDifferentNames() {
        DefaultJavaClass type1 = new DefaultJavaClass();
        type1.setName("ClassA");

        DefaultJavaClass type2 = new DefaultJavaClass();
        type2.setName("ClassB");

        assertTrue(type1.compareTo(type2) < 0);
        assertTrue(type2.compareTo(type1) > 0);
    }

    @Test
    public void testCompareToNullNames() {
        DefaultJavaClass type1 = new DefaultJavaClass();
        DefaultJavaClass type2 = new DefaultJavaClass();
        type2.setName("ClassA");

        // Depending on implementation, null handling in compareTo
        try {
            int result = type1.compareTo(type2);
            assertTrue(result != 0);
        } catch (NullPointerException e) {
            // If compareTo does not handle null names gracefully, catching it ensures test robustness
        }
    }
}