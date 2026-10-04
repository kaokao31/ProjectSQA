package org.apache.commons.jxpath.ri.model.dom;

import static org.junit.Assert.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Test suite for DOMAttributeIterator.
 * Designed to achieve high line and branch coverage and detect potential faults.
 */
public class DOMAttributeIteratorTest {

    private Document document;
    private Element elementWithAttrs;
    private Element elementNoAttrs;
    private Element elementWithNSAttrs;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.newDocument();

        // Element with no attributes
        elementNoAttrs = document.createElement("empty");

        // Element with standard attributes
        elementWithAttrs = document.createElement("withAttrs");
        elementWithAttrs.setAttribute("id", "123");
        elementWithAttrs.setAttribute("name", "test");
        elementWithAttrs.setAttribute("value", "xyz");

        // Element with namespace-aware attributes
        elementWithNSAttrs = document.createElementNS("http://example.com", "ns:prefixed");
        elementWithNSAttrs.setAttributeNS("http://example.com", "ns:attr1", "val1");
        elementWithNSAttrs.setAttribute("plain", "plainVal");
    }

    // Test iterator on element with no attributes
    @Test
    public void testNoAttributes() {
        DOMAttributeIterator iterator = new DOMAttributeIterator(elementNoAttrs);
        assertFalse("Should have no attributes", iterator.hasNext());
        assertNull("Next should be null", iterator.next());
    }

    // Test iterator on element with multiple attributes
    @Test
    public void testMultipleAttributes() {
        DOMAttributeIterator iterator = new DOMAttributeIterator(elementWithAttrs);
        assertTrue("Should have attributes", iterator.hasNext());
        Node first = iterator.next();
        assertNotNull("First attribute should not be null", first);
        assertTrue("First should be Attr node", first instanceof Attr);
        assertEquals("First attribute name should be id", "id", first.getNodeName());
        assertEquals("First attribute value should be 123", "123", first.getNodeValue());

        assertTrue("Should have more attributes", iterator.hasNext());
        Node second = iterator.next();
        assertNotNull("Second attribute should not be null", second);
        assertTrue("Second should be Attr node", second instanceof Attr);
        assertEquals("Second attribute name should be name", "name", second.getNodeName());
        assertEquals("Second attribute value should be test", "test", second.getNodeValue());

        assertTrue("Should have more attributes", iterator.hasNext());
        Node third = iterator.next();
        assertNotNull("Third attribute should not be null", third);
        assertTrue("Third should be Attr node", third instanceof Attr);
        assertEquals("Third attribute name should be value", "value", third.getNodeName());
        assertEquals("Third attribute value should be xyz", "xyz", third.getNodeValue());

        assertFalse("Should have no more attributes", iterator.hasNext());
        assertNull("Next should be null after exhaustion", iterator.next());
    }

    // Test iterator on element with namespace attributes
    @Test
    public void testNamespaceAttributes() {
        DOMAttributeIterator iterator = new DOMAttributeIterator(elementWithNSAttrs);
        // Expect both namespace and plain attributes (order may vary)
        int count = 0;
        while (iterator.hasNext()) {
            Node attr = iterator.next();
            assertNotNull("Attribute should not be null", attr);
            assertTrue("Should be Attr node", attr instanceof Attr);
            count++;
        }
        assertEquals("Should have exactly 2 attributes", 2, count);
    }

    // Test iterator on null element (should handle gracefully)
    @Test(expected = NullPointerException.class)
    public void testNullElement() {
        new DOMAttributeIterator(null);
    }

    // Test iterator on element with a single attribute
    @Test
    public void testSingleAttribute() {
        Element singleAttr = document.createElement("single");
        singleAttr.setAttribute("only", "one");
        DOMAttributeIterator iterator = new DOMAttributeIterator(singleAttr);
        assertTrue("Should have one attribute", iterator.hasNext());
        Node attr = iterator.next();
        assertNotNull("Attribute should not be null", attr);
        assertEquals("Attribute name should be only", "only", attr.getNodeName());
        assertEquals("Attribute value should be one", "one", attr.getNodeValue());
        assertFalse("Should have no more attributes", iterator.hasNext());
        assertNull("Next should be null", iterator.next());
    }

    // Test iterator on element with attribute that has special characters
    @Test
    public void testSpecialCharactersInAttribute() {
        Element special = document.createElement("special");
        special.setAttribute("data", "value with spaces & symbols");
        DOMAttributeIterator iterator = new DOMAttributeIterator(special);
        assertTrue("Should have attribute", iterator.hasNext());
        Node attr = iterator.next();
        assertEquals("Attribute value should match", "value with spaces & symbols", attr.getNodeValue());
    }

    // Test that iterator returns the same set of attributes as NamedNodeMap
    @Test
    public void testConsistencyWithNamedNodeMap() {
        NamedNodeMap map = elementWithAttrs.getAttributes();
        DOMAttributeIterator iterator = new DOMAttributeIterator(elementWithAttrs);
        int mapLength = map.getLength();
        int iterCount = 0;
        while (iterator.hasNext()) {
            Node iterAttr = iterator.next();
            boolean found = false;
            for (int i = 0; i < mapLength; i++) {
                if (map.item(i).isEqualNode(iterAttr)) {
                    found = true;
                    break;
                }
            }
            assertTrue("Iterator attribute should be in NamedNodeMap", found);
            iterCount++;
        }
        assertEquals("Iterator count should match NamedNodeMap length", mapLength, iterCount);
    }

    // Test that iterator works on a document node (should have no attributes)
    @Test
    public void testDocumentNode() {
        DOMAttributeIterator iterator = new DOMAttributeIterator(document);
        assertFalse("Document node should have no attributes", iterator.hasNext());
        assertNull("Next should be null", iterator.next());
    }

    // Test that iterator works on a text node (should have no attributes)
    @Test
    public void testTextNode() {
        Node textNode = document.createTextNode("some text");
        DOMAttributeIterator iterator = new DOMAttributeIterator(textNode);
        assertFalse("Text node should have no attributes", iterator.hasNext());
        assertNull("Next should be null", iterator.next());
    }

    // Test that iterator works on an attribute node itself (should have no attributes)
    @Test
    public void testAttrNode() {
        Attr attr = document.createAttribute("testAttr");
        DOMAttributeIterator iterator = new DOMAttributeIterator(attr);
        assertFalse("Attr node should have no attributes", iterator.hasNext());
        assertNull("Next should be null", iterator.next());
    }

    // Test that iterator handles element with empty attribute value
    @Test
    public void testEmptyAttributeValue() {
        Element emptyVal = document.createElement("emptyVal");
        emptyVal.setAttribute("empty", "");
        DOMAttributeIterator iterator = new DOMAttributeIterator(emptyVal);
        assertTrue("Should have attribute", iterator.hasNext());
        Node attr = iterator.next();
        assertEquals("Attribute value should be empty string", "", attr.getNodeValue());
        assertFalse("Should have no more attributes", iterator.hasNext());
    }

    // Test that iterator handles element with attribute name that is a reserved word
    @Test
    public void testReservedWordAttribute() {
        Element reserved = document.createElement("reserved");
        reserved.setAttribute("class", "myClass");
        DOMAttributeIterator iterator = new DOMAttributeIterator(reserved);
        assertTrue("Should have attribute", iterator.hasNext());
        Node attr = iterator.next();
        assertEquals("Attribute name should be class", "class", attr.getNodeName());
    }

    // Test that iterator does not include namespace declarations (xmlns) if present
    @Test
    public void testExcludeNamespaceDeclarations() {
        Element nsDecl = document.createElementNS("http://example.com", "ns:root");
        nsDecl.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:ns", "http://example.com");
        nsDecl.setAttribute("regular", "value");
        DOMAttributeIterator iterator = new DOMAttributeIterator(nsDecl);
        int count = 0;
        while (iterator.hasNext()) {
            Node attr = iterator.next();
            assertNotNull("Attribute should not be null", attr);
            // Namespace declaration attributes are not returned by DOMAttributeIterator
            assertFalse("Should not include xmlns attributes", attr.getNodeName().startsWith("xmlns"));
            count++;
        }
        assertEquals("Should have exactly 1 attribute (regular)", 1, count);
    }

    // Test that iterator returns attributes in document order (as per NamedNodeMap)
    @Test
    public void testOrderOfAttributes() {
        Element ordered = document.createElement("ordered");
        ordered.setAttribute("z", "last");
        ordered.setAttribute("a", "first");
        ordered.setAttribute("m", "middle");
        DOMAttributeIterator iterator = new DOMAttributeIterator(ordered);
        // The order is the same as NamedNodeMap (insertion order)
        String[] expectedNames = {"z", "a", "m"};
        int index = 0;
        while (iterator.hasNext()) {
            Node attr = iterator.next();
            assertEquals("Attribute name should match expected order", expectedNames[index], attr.getNodeName());
            index++;
        }
        assertEquals("Should have iterated all attributes", 3, index);
    }

    // Test that iterator can be called multiple times (should reset? Typically not, but we test behavior)
    @Test
    public void testMultipleIterations() {
        DOMAttributeIterator iterator = new DOMAttributeIterator(elementWithAttrs);
        // First iteration
        int count1 = 0;
        while (iterator.hasNext()) {
            iterator.next();
            count1++;
        }
        assertEquals("First iteration should have 3 attributes", 3, count1);
        // Second iteration (should start from beginning? Usually not, but we test)
        // After exhaustion, hasNext returns false and next returns null
        assertFalse("After exhaustion, hasNext should be false", iterator.hasNext());
        assertNull("After exhaustion, next should be null", iterator.next());
    }

    // Test that iterator handles element with many attributes (stress test)
    @Test
    public void testManyAttributes() {
        Element many = document.createElement("many");
        for (int i = 0; i < 100; i++) {
            many.setAttribute("attr" + i, "val" + i);
        }
        DOMAttributeIterator iterator = new DOMAttributeIterator(many);
        int count = 0;
        while (iterator.hasNext()) {
            Node attr = iterator.next();
            assertNotNull("Attribute should not be null", attr);
            count++;
        }
        assertEquals("Should have 100 attributes", 100, count);
    }

    // Test that iterator does not throw exception when element has no attributes but has child nodes
    @Test
    public void testElementWithChildrenButNoAttributes() {
        Element parent = document.createElement("parent");
        Element child = document.createElement("child");
        parent.appendChild(child);
        DOMAttributeIterator iterator = new DOMAttributeIterator(parent);
        assertFalse("Parent with children but no attributes should have no attributes", iterator.hasNext());
        assertNull("Next should be null", iterator.next());
    }
}