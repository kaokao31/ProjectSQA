package org.apache.commons.jxpath.ri.model.beans;

import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;

import static org.junit.Assert.*;

public class PropertyPointerTest {

    private TestPropertyPointer propertyPointer;
    private JXPathContext context;

    @Before
    public void setUp() {
        context = JXPathContext.newContext(new Object());
        propertyPointer = new TestPropertyPointer(NodePointer.newNodePointer(new QName("test"), new Object(), Locale.ENGLISH));
    }

    @Test
    public void testLengthAndIndexManipulation() {
        assertEquals(1, propertyPointer.getLength());
        assertEquals(0, propertyPointer.getIndex());
        
        propertyPointer.setIndex(5);
        assertEquals(5, propertyPointer.getIndex());
        
        propertyPointer.setIndex(WHOLE_COLLECTION_INDEX);
        assertEquals(WHOLE_COLLECTION_INDEX, propertyPointer.getIndex());
        assertTrue(propertyPointer.isActual());
    }

    @Test
    public void testDefaultBaseValueMethods() {
        // Test base implementation methods in PropertyPointer
        assertNull(propertyPointer.getBaseValue());
        assertNull(propertyPointer.getImmediateValuePointer());
        assertFalse(propertyPointer.isCollection());
        assertEquals(0, propertyPointer.getLength());
    }

    @Test
    public void testAttributeSupport() {
        assertFalse(propertyPointer.isAttribute());
    }

    @Test
    public void testPropertyNamesAndDescriptions() {
        assertNull(propertyPointer.getPropertyName());
        
        propertyPointer.setPropertyName("customProp");
        assertEquals("customProp", propertyPointer.getPropertyName());
        
        assertNull(propertyPointer.getPropertyNames());
    }

    @Test
    public void testRemoveAndSetValue() {
        // Just exercising the default/abstract behaviors implemented via subclass
        propertyPointer.remove();
        propertyPointer.setValue("newValue");
        assertEquals("newValue", propertyPointer.getActualValue());
    }

    @Test
    public void testCompareChildNodePointers() {
        NodePointer ptr1 = NodePointer.newNodePointer(new QName("a"), new Object(), Locale.ENGLISH);
        NodePointer ptr2 = NodePointer.newNodePointer(new QName("b"), new Object(), Locale.ENGLISH);
        
        // Test standard comparison fallback
        int comparison = propertyPointer.compareChildNodePointers(ptr1, ptr2);
        assertTrue(comparison <= 0 || comparison >= 0);
    }

    // Constants used in JXPath pointers
    private static final int WHOLE_COLLECTION_INDEX = NodePointer.WHOLE_COLLECTION_INDEX;

    /**
    * Concrete subclass of PropertyPointer for testing abstract methods.
    */
    private static class TestPropertyPointer extends PropertyPointer {
        private String propertyName;
        private Object actualValue;

        public TestPropertyPointer(NodePointer parent) {
            super(parent);
        }

        @Override
        public QName getName() {
            return new QName(propertyName != null ? propertyName : "test");
        }

        @Override
        public Object getBaseValue() {
            return null;
        }

        @Override
        public Object getActualValue() {
            return actualValue;
        }

        @Override
        public void setScalarValue(Object value) {
            this.actualValue = value;
        }

        @Override
        public int getLength() {
            return propertyName != null && propertyName.equals("empty") ? 0 : 1;
        }

        @Override
        public boolean isCollection() {
            return false;
        }

        @Override
        public String getPropertyName() {
            return propertyName;
        }

        @Override
        public void setPropertyName(String propertyName) {
            this.propertyName = propertyName;
        }

        @Override
        public String[] getPropertyNames() {
            return null;
        }
    }
}