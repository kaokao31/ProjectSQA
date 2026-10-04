package org.apache.commons.jxpath.ri.model.dom;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static org.junit.Assert.*;

/**
 * Test suite for DOMAttributeIterator.
 * Covers edge cases, null inputs, boundary conditions, and potential faults.
 */
public class DOMAttributeIteratorTest {

    private Document document;
    private Element rootElement;
    private DOMNodePointer rootPointer;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.newDocument();

        rootElement = document.createElementNS("http://example.com/ns", "root");
        rootElement.setAttribute("id", "1");
        rootElement.setAttributeNS("http://example.com/ns", "ns:attr", "value");
        rootElement.setAttribute("empty", "");
        document.appendChild(rootElement);

        rootPointer = new DOMNodePointer(rootElement, null, null);
    }

    // ========== Constructor and basic iteration ==========

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullParentPointer() {
        new DOMAttributeIterator(null, new QName("test"));
    }

    @Test
    public void testConstructorWithNullNodeTest() {
        DOMAttributeIterator iterator = new DOMAttributeIterator(rootPointer, null);
        assertNotNull("Iterator should be created", iterator);
        assertTrue("Should have at least one attribute", iterator.setPosition(1));
    }

    @Test
    public void testIterateAllAttributes() {
        DOMAttributeIterator iterator = new DOMAttributeIterator(rootPointer, null);
        int count = 0;
        while (iterator.setPosition(count + 1)) {
            count++;
            assertNotNull("NodePointer should not be null", iterator.getNodePointer());
        }
        assertEquals("Should have 3 attributes", 3, count);
    }

    @Test
    public void testIterateSpecificAttribute() {
        QName idQName = new QName(null, "id");
        DOMAttributeIterator iterator = new DOMAttributeIterator(rootPointer, idQName);
        assertTrue("Should find attribute 'id'", iterator.setPosition(1));
        assertEquals("id", iterator.getNodePointer().getName().getName());
        assertFalse("Should not have more", iterator.setPosition(2));
    }

    @Test
    public void testIterateNonExistentAttribute() {
        QName nonExistent = new QName(null, "nonexistent");
        DOMAttributeIterator iterator = new DOMAttributeIterator(rootPointer, nonExistent);
        assertFalse("Should not find attribute", iterator.setPosition(1));
    }

    @Test
    public void testIterateWithWildcard() {
        QName wildcard = new QName(null, "*");
        DOMAttributeIterator iterator = new DOMAttributeIterator(rootPointer, wildcard);
        int count = 0;
        while (iterator.setPosition(count + 1)) {
            count++;
        }
        assertEquals("Wildcard should match all attributes", 3, count);
    }

    @Test
    public void testIterateWithNamespace() {
        QName nsAttr = new QName("http://example.com/ns", "attr");
        DOMAttributeIterator iterator = new DOMAttributeIterator(rootPointer, nsAttr);
        assertTrue("Should find namespaced attribute", iterator.setPosition(1));
        assertEquals("attr", iterator.getNodePointer().getName().getName());
        assertFalse("Should not have more", iterator.setPosition(2));
    }

    // ========== Edge cases ==========

    @Test
    public void testEmptyAttributeValue() {
        QName empty = new QName(null, "empty");
        DOMAttributeIterator iterator = new DOMAttributeIterator(rootPointer, empty);
        assertTrue("Should find empty attribute", iterator.setPosition(1));
        assertEquals("", iterator.getNodePointer().getValue());
    }

    @Test
    public void testParentIsDocumentNode() {
        // Document node has no attributes
        DOMNodePointer docPointer = new DOMNodePointer(document, null, null);
        DOMAttributeIterator iterator = new DOMAttributeIterator(docPointer, null);
        assertFalse("Document node should have no attributes", iterator.setPosition(1));
    }

    @Test
    public void testParentIsTextNode() {
        Element child = document.createElement("child");
        child.appendChild(document.createTextNode("text"));
        rootElement.appendChild(child);
        DOMNodePointer textPointer = new DOMNodePointer(child.getFirstChild(), null, null);
        DOMAttributeIterator iterator = new DOMAttributeIterator(textPointer, null);
        assertFalse("Text node should have no attributes", iterator.setPosition(1));
    }

    @Test
    public void testParentIsAttributeNode() {
        Attr attr = document.createAttribute("test");
        DOMNodePointer attrPointer = new DOMNodePointer(attr, null, null);
        DOMAttributeIterator iterator = new DOMAttributeIterator(attrPointer, null);
        assertFalse("Attribute node should have no attributes", iterator.setPosition(1));
    }

    // ========== Position manipulation ==========

    @Test
    public void testSetPositionOutOfBounds() {
        DOMAttributeIterator iterator = new DOMAttributeIterator(rootPointer, null);
        assertFalse("Negative position should fail", iterator.setPosition(-1));
        assertFalse("Zero position should fail", iterator.setPosition(0));
        assertTrue("Position 1 should succeed", iterator.setPosition(1));
        assertTrue("Position 2 should succeed", iterator.setPosition(2));
        assertTrue("Position 3 should succeed", iterator.setPosition(3));
        assertFalse("Position 4 should fail (only 3 attributes)", iterator.setPosition(4));
    }

    @Test
    public void testGetPositionAfterSetPosition() {
        DOMAttributeIterator iterator = new DOMAttributeIterator(rootPointer, null);
        iterator.setPosition(2);
        assertEquals("Position should be 2", 2, iterator.getPosition());
    }

    @Test
    public void testGetNodePointerAfterSetPosition() {
        DOMAttributeIterator iterator = new DOMAttributeIterator(rootPointer, null);
        iterator.setPosition(1);
        assertNotNull("NodePointer should not be null", iterator.getNodePointer());
        assertEquals("id", iterator.getNodePointer().getName().getName());
    }

    @Test
    public void testGetNodePointerWithoutSettingPosition() {
        DOMAttributeIterator iterator = new DOMAttributeIterator(rootPointer, null);
        // Without setPosition, getNodePointer may return null or throw
        assertNull("NodePointer should be null before setPosition", iterator.getNodePointer());
    }

    // ========== Fault detection (Defects4J bug 11 related) ==========

    @Test
    public void testDocumentNodeWithNamespaceTest() {
        // Bug scenario: Document node with a namespace test might incorrectly iterate
        DOMNodePointer docPointer = new DOMNodePointer(document, null, null);
        QName nsTest = new QName("http://example.com/ns", "*");
        DOMAttributeIterator iterator = new DOMAttributeIterator(docPointer, nsTest);
        assertFalse("Document node should have no attributes even with namespace test", iterator.setPosition(1));
    }

    @Test
    public void testElementWithNoAttributesAndWildcard() {
        Element noAttrElement = document.createElement("noattr");
        rootElement.appendChild(noAttrElement);
        DOMNodePointer noAttrPointer = new DOMNodePointer(noAttrElement, null, null);
        DOMAttributeIterator iterator = new DOMAttributeIterator(noAttrPointer, new QName(null, "*"));
        assertFalse("Element with no attributes should return none", iterator.setPosition(1));
    }

    @Test
    public void testMultipleCallsToSetPosition() {
        DOMAttributeIterator iterator = new DOMAttributeIterator(rootPointer, null);
        iterator.setPosition(3);
        assertEquals("Position should be 3", 3, iterator.getPosition());
        iterator.setPosition(1);
        assertEquals("Position should be 1 after reset", 1, iterator.getPosition());
        assertEquals("NodePointer should now point to first attribute", "id", iterator.getNodePointer().getName().getName());
    }

    @Test
    public void testIterateAfterReset() {
        DOMAttributeIterator iterator = new DOMAttributeIterator(rootPointer, null);
        iterator.setPosition(2);
        iterator.setPosition(1);
        // Ensure we can iterate forward again
        assertTrue("Should be able to move to position 2 again", iterator.setPosition(2));
        assertEquals("attr", iterator.getNodePointer().getName().getName());
    }
}