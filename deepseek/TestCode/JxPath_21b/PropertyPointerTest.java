package org.apache.commons.jxpath.ri.model.beans;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.dom.DOMNodePointer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test suite for PropertyPointer to achieve high coverage and detect potential faults.
 */
public class PropertyPointerTest {

    private NodePointer parentPointer;
    private PropertyPointer propertyPointer;

    @Before
    public void setUp() {
        // Create a simple parent pointer (using DOMNodePointer as a concrete implementation)
        // For testing, we use a dummy context and null node to avoid complex setup.
        JXPathContext context = JXPathContext.newContext(new Object());
        parentPointer = new DOMNodePointer(null, null, null);
        propertyPointer = new PropertyPointer(parentPointer, "testProperty");
    }

    @Test
    public void testGetPropertyPointer() {
        // Should return itself
        assertSame(propertyPointer, propertyPointer.getPropertyPointer());
    }

    @Test
    public void testGetValuePointer() {
        // For a simple property, getValuePointer may return itself or a child pointer
        NodePointer valuePointer = propertyPointer.getValuePointer();
        assertNotNull(valuePointer);
        // Typically returns a child pointer (e.g., ValuePointer) but we just check not null
    }

    @Test
    public void testGetImmediateNode() {
        // Without a backing bean, getImmediateNode may return null
        assertNull(propertyPointer.getImmediateNode());
    }

    @Test
    public void testSetValue() {
        // Setting value should not throw for a valid property
        try {
            propertyPointer.setValue("newValue");
        } catch (Exception e) {
            fail("setValue threw an exception: " + e.getMessage());
        }
    }

    @Test
    public void testIsCollection() {
        // Simple property is not a collection
        assertFalse(propertyPointer.isCollection());
    }

    @Test
    public void testGetLength() {
        // Default length is 1 for a simple property
        assertEquals(1, propertyPointer.getLength());
    }

    @Test
    public void testGetIndex() {
        // Default index is -1
        assertEquals(-1, propertyPointer.getIndex());
    }

    @Test
    public void testGetPropertyName() {
        assertEquals("testProperty", propertyPointer.getPropertyName());
    }

    @Test
    public void testGetPropertyCount() {
        // For a simple property, count is 1
        assertEquals(1, propertyPointer.getPropertyCount());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCreatePath() {
        propertyPointer.createPath(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemove() {
        propertyPointer.remove();
    }

    // Edge cases: null property name
    @Test
    public void testNullPropertyName() {
        PropertyPointer nullProp = new PropertyPointer(parentPointer, null);
        assertNull(nullProp.getPropertyName());
        // getImmediateNode should not throw NPE
        assertNull(nullProp.getImmediateNode());
    }

    // Edge cases: empty property name
    @Test
    public void testEmptyPropertyName() {
        PropertyPointer emptyProp = new PropertyPointer(parentPointer, "");
        assertEquals("", emptyProp.getPropertyName());
        assertNull(emptyProp.getImmediateNode());
    }

    // Edge cases: numeric property name (array index)
    @Test
    public void testNumericPropertyName() {
        PropertyPointer numericProp = new PropertyPointer(parentPointer, "0");
        assertEquals("0", numericProp.getPropertyName());
        // Should not throw
        assertNull(numericProp.getImmediateNode());
    }

    // Test getValuePointer with null parent (potential NPE)
    @Test(expected = NullPointerException.class)
    public void testGetValuePointerWithNullParent() {
        PropertyPointer nullParent = new PropertyPointer(null, "test");
        nullParent.getValuePointer();
    }

    // Test setValue with null value (should be allowed)
    @Test
    public void testSetValueNull() {
        propertyPointer.setValue(null);
        assertNull(propertyPointer.getImmediateNode());
    }

    // Test getImmediateNode after setValue
    @Test
    public void testGetImmediateNodeAfterSetValue() {
        propertyPointer.setValue("someValue");
        // getImmediateNode may still return null if not backed by a bean
        // but we just ensure no exception
        assertNull(propertyPointer.getImmediateNode());
    }

    // Test isLeaf (inherited from NodePointer)
    @Test
    public void testIsLeaf() {
        // Default is false for property pointer
        assertFalse(propertyPointer.isLeaf());
    }

    // Test isRoot (inherited)
    @Test
    public void testIsRoot() {
        assertFalse(propertyPointer.isRoot());
    }

    // Test asPath (inherited)
    @Test
    public void testAsPath() {
        String path = propertyPointer.asPath();
        assertNotNull(path);
        assertTrue(path.contains("testProperty"));
    }

    // Test clone
    @Test
    public void testClone() {
        PropertyPointer cloned = (PropertyPointer) propertyPointer.clone();
        assertNotNull(cloned);
        assertEquals(propertyPointer.getPropertyName(), cloned.getPropertyName());
    }

    // Test compareChildNodePointers (inherited, may throw)
    @Test(expected = UnsupportedOperationException.class)
    public void testCompareChildNodePointers() {
        propertyPointer.compareChildNodePointers(null, null);
    }

    // Test testNode (inherited)
    @Test
    public void testTestNode() {
        // Default implementation may return false
        assertFalse(propertyPointer.testNode(null));
    }
}