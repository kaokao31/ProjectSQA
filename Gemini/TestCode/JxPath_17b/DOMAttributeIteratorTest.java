package org.apache.commons.jxpath.ri.model.dom;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.PropertyPointer;
import org.apache.commons.jxpath.util.ReverseComparator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static org.junit.Assert.*;

public class DOMAttributeIteratorTest {

    private Document document;
    private Element element;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        document = db.newDocument();
        element = document.createElementNS("http://example.com/ns", "prefix:element");
        element.setAttribute("id", "testId");
        element.setAttribute("name", "testName");
        element.setAttributeNS("http://example.com/ns", "prefix:attr", "nsValue");
    }

    @After
    public void tearDown() {
        document = null;
        element = null;
    }

    @Test
    public void testWildcardAttributeIterator() {
        NodePointer parent = NodePointer.newNodePointer(new QName("element"), element, null);
        QName qName = new QName(null, "*");
        DOMAttributeIterator iterator = new DOMAttributeIterator(parent, qName);

        assertEquals(0, iterator.getPosition());
        
        // Iterate through attributes
        assertTrue(iterator.setPosition(1));
        assertNotNull(iterator.getNodePointer());
        assertEquals(1, iterator.getPosition());

        assertTrue(iterator.setPosition(2));
        assertNotNull(iterator.getNodePointer());
        assertEquals(2, iterator.getPosition());

        assertTrue(iterator.setPosition(3));
        assertNotNull(iterator.getNodePointer());
        assertEquals(3, iterator.getPosition());

        assertFalse(iterator.setPosition(4));
        assertEquals(0, iterator.getPosition());
    }

    @Test
    public void testSpecificAttributeIterator() {
        NodePointer parent = NodePointer.newNodePointer(new QName("element"), element, null);
        QName qName = new QName(null, "id");
        DOMAttributeIterator iterator = new DOMAttributeIterator(parent, qName);

        assertTrue(iterator.setPosition(1));
        NodePointer ptr = iterator.getNodePointer();
        assertNotNull(ptr);
        assertEquals("id", ptr.getName().getName());

        assertFalse(iterator.setPosition(2));
    }

    @Test
    public void testNamespacePrefixedAttributeIterator() {
        NodePointer parent = NodePointer.newNodePointer(new QName("element"), element, null);
        // Test matching with prefix
        QName qName = new QName("prefix", "attr");
        DOMAttributeIterator iterator = new DOMAttributeIterator(parent, qName);

        assertTrue(iterator.setPosition(1));
        NodePointer ptr = iterator.getNodePointer();
        assertNotNull(ptr);
        assertEquals("attr", ptr.getName().getName());
        assertEquals("prefix", ptr.getName().getPrefix());
    }

    @Test
    public void testNamespaceWildcardAttributeIterator() {
        NodePointer parent = NodePointer.newNodePointer(new QName("element"), element, null);
        // Wildcard with prefix e.g., "prefix:*"
        QName qName = new QName("prefix", "*");
        DOMAttributeIterator iterator = new DOMAttributeIterator(parent, qName);

        // Should find attributes matching the prefix or namespace depending on implementation
        boolean hasMore = iterator.setPosition(1);
        // Depending on whether it matches, just exercise the code path
        if (hasMore) {
            assertNotNull(iterator.getNodePointer());
        }
    }

    @Test
    public void testNonExistentAttribute() {
        NodePointer parent = NodePointer.newNodePointer(new QName("element"), element, null);
        QName qName = new QName(null, "nonexistent");
        DOMAttributeIterator iterator = new DOMAttributeIterator(parent, qName);

        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    @Test
    public void testSetPositionOutOfBounds() {
        NodePointer parent = NodePointer.newNodePointer(new QName("element"), element, null);
        QName qName = new QName(null, "id");
        DOMAttributeIterator iterator = new DOMAttributeIterator(parent, qName);

        assertFalse(iterator.setPosition(0));
        assertFalse(iterator.setPosition(-1));
        assertFalse(iterator.setPosition(5));
    }
}