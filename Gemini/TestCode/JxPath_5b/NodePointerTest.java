package org.apache.commons.jxpath.ri.model;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.*;

public class NodePointerTest {

    private static class ConcreteNodePointer extends NodePointer {
        private Object node;

        protected ConcreteNodePointer(NodePointer parent) {
            super(parent);
        }

        protected ConcreteNodePointer(NodePointer parent, QName name) {
            super(parent, name);
        }

        @Override
        public QName getName() {
            return new QName("test");
        }

        @Override
        public Object getBaseValue() {
            return node;
        }

        public void setBaseValue(Object node) {
            this.node = node;
        }

        @Override
        public Object getImmediateNode() {
            return node;
        }

        @Override
        public boolean isCollection() {
            return false;
        }

        @Override
        public int getLength() {
            return 1;
        }

        @Override
        public NodeIterator childIterator(NodeTest test, boolean reverse, NodePointer startWith) {
            return null;
        }

        @Override
        public NodeIterator attributeIterator(QName qname) {
            return null;
        }

        @Override
        public NodeIterator namespaceIterator() {
            return null;
        }

        @Override
        public NodePointer namespacePointer(String namespace) {
            return null;
        }

        @Override
        public String getXPath() {
            return "/test";
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj;
        }
    }

    @Test
    public void testCreateChild() {
        QName qname = new QName("prefix", "local");
        ConcreteNodePointer parent = new ConcreteNodePointer(null);
        NodePointer child = parent.createChild(null, qname, 0, "value");
        assertNotNull(child);
    }

    @Test
    public void testCompareChildNodePointers() {
        ConcreteNodePointer parent = new ConcreteNodePointer(null);
        ConcreteNodePointer child1 = new ConcreteNodePointer(parent);
        ConcreteNodePointer child2 = new ConcreteNodePointer(parent);

        child1.setIndex(0);
        child2.setIndex(1);

        int result = parent.compareChildNodePointers(child1, child2);
        assertTrue(result < 0);

        int resultReversed = parent.compareChildNodePointers(child2, child1);
        assertTrue(resultReversed > 0);

        int resultEqual = parent.compareChildNodePointers(child1, child1);
        assertEquals(0, resultEqual);
    }

    @Test
    public void testFindNamespacePointer() {
        ConcreteNodePointer parent = new ConcreteNodePointer(null);
        assertNull(parent.findNamespacePointer("ns"));
    }

    @Test
    public void testGetNamespaceURIWithPrefix() {
        ConcreteNodePointer parent = new ConcreteNodePointer(null);
        assertNull(parent.getNamespaceURI("xml"));
    }

    @Test
    public void testGetNamespaceURIInternal() {
        ConcreteNodePointer parent = new ConcreteNodePointer(null);
        assertNull(parent.getNamespaceURI());
    }

    @Test
    public void testGetId() {
        ConcreteNodePointer parent = new ConcreteNodePointer(null);
        assertNull(parent.getId());
    }

    @Test
    public void testGetDefaultNamespaceURI() {
        ConcreteNodePointer parent = new ConcreteNodePointer(null);
        assertNull(parent.getDefaultNamespaceURI());
    }

    @Test
    public void testIsLanguage() {
        ConcreteNodePointer parent = new ConcreteNodePointer(null);
        parent.setLocale(Locale.ENGLISH);
        assertTrue(parent.isLanguage("en"));
        assertFalse(parent.isLanguage("fr"));
    }

    @Test
    public void testTestNode() {
        ConcreteNodePointer parent = new ConcreteNodePointer(null);
        parent.setBaseValue(new Object());
        
        NodeTypeTest nodeTest = new NodeTypeTest(1);
        assertTrue(NodePointer.testNode(parent, nodeTest));
        
        assertFalse(NodePointer.testNode(null, nodeTest));
        
        assertTrue(NodePointer.testNode(parent, null));
    }

    @Test
    public void testSetIndex() {
        ConcreteNodePointer parent = new ConcreteNodePointer(null);
        parent.setIndex(5);
        assertEquals(5, parent.getIndex());
    }

    @Test
    public void testAttributeIteratorEdgeCases() {
        ConcreteNodePointer parent = new ConcreteNodePointer(null);
        assertNull(parent.attributeIterator(new QName("test")));
    }

    @Test
    public void testNullParentHandling() {
        ConcreteNodePointer parent = new ConcreteNodePointer(null);
        assertNull(parent.getImmediateParentPointer());
        assertFalse(parent.isActual());
    }
}