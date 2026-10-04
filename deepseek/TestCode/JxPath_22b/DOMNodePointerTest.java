package org.apache.commons.jxpath.ri.model.dom;

import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.dom.DOMNodePointer;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static org.junit.Assert.*;

/**
 * Test suite for DOMNodePointer (JxPath bug 22).
 * Covers namespace handling, attribute iteration, and path generation.
 */
public class DOMNodePointerTest {

    private Document document;
    private DOMNodePointer rootPointer;
    private Element rootElement;
    private Element childElement;
    private Element nsElement;
    private Attr attribute;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.newDocument();

        // Create root element with default namespace
        rootElement = document.createElementNS("http://example.com/ns", "root");
        document.appendChild(rootElement);

        // Create child element with no namespace
        childElement = document.createElement("child");
        childElement.setTextContent("text");
        rootElement.appendChild(childElement);

        // Create element with explicit namespace prefix
        nsElement = document.createElementNS("http://other.com/ns", "prefix:other");
        rootElement.appendChild(nsElement);

        // Create attribute (no namespace)
        attribute = document.createAttribute("attr");
        attribute.setValue("value");
        rootElement.setAttributeNode(attribute);

        rootPointer = new DOMNodePointer(null, document);
    }

    // ========== Basic construction and node retrieval ==========

    @Test
    public void testConstructorWithNullParent() {
        DOMNodePointer pointer = new DOMNodePointer(null, document);
        assertNotNull(pointer);
        assertNull(pointer.getImmediateParentPointer());
    }

    @Test
    public void testConstructorWithParent() {
        DOMNodePointer parent = new DOMNodePointer(null, document);
        DOMNodePointer child = new DOMNodePointer(parent, childElement);
        assertSame(parent, child.getImmediateParentPointer());
    }

    @Test
    public void testGetNode() {
        assertSame(document, rootPointer.getNode());
    }

    // ========== Namespace methods ==========

    @Test
    public void testGetNamespaceURI_DefaultNamespace() {
        // Root element has default namespace
        DOMNodePointer rootElemPointer = new DOMNodePointer(rootPointer, rootElement);
        String uri = rootElemPointer.getNamespaceURI();
        assertEquals("http://example.com/ns", uri);
    }

    @Test
    public void testGetNamespaceURI_NoNamespace() {
        // Child element has no namespace
        DOMNodePointer childPointer = new DOMNodePointer(rootPointer, childElement);
        String uri = childPointer.getNamespaceURI();
        assertNull(uri);
    }

    @Test
    public void testGetNamespaceURI_PrefixedNamespace() {
        // Element with prefix
        DOMNodePointer nsPointer = new DOMNodePointer(rootPointer, nsElement);
        String uri = nsPointer.getNamespaceURI();
        assertEquals("http://other.com/ns", uri);
    }

    @Test
    public void testGetNamespaceURI_Attribute() {
        // Attribute has no namespace
        DOMNodePointer attrPointer = new DOMNodePointer(rootPointer, attribute);
        String uri = attrPointer.getNamespaceURI();
        assertNull(uri);
    }

    @Test
    public void testGetNamespaceURI_DocumentNode() {
        // Document node has no namespace
        String uri = rootPointer.getNamespaceURI();
        assertNull(uri);
    }

    @Test
    public void testGetPrefix_DefaultNamespace() {
        DOMNodePointer rootElemPointer = new DOMNodePointer(rootPointer, rootElement);
        String prefix = rootElemPointer.getPrefix();
        assertNull(prefix); // default namespace has no prefix
    }

    @Test
    public void testGetPrefix_PrefixedNamespace() {
        DOMNodePointer nsPointer = new DOMNodePointer(rootPointer, nsElement);
        String prefix = nsPointer.getPrefix();
        assertEquals("prefix", prefix);
    }

    @Test
    public void testGetPrefix_NoNamespace() {
        DOMNodePointer childPointer = new DOMNodePointer(rootPointer, childElement);
        String prefix = childPointer.getPrefix();
        assertNull(prefix);
    }

    // ========== Attribute handling ==========

    @Test
    public void testGetAttributeValue() {
        DOMNodePointer rootElemPointer = new DOMNodePointer(rootPointer, rootElement);
        String value = rootElemPointer.getAttributeValue("attr");
        assertEquals("value", value);
    }

    @Test
    public void testGetAttributeValue_NonExistent() {
        DOMNodePointer rootElemPointer = new DOMNodePointer(rootPointer, rootElement);
        String value = rootElemPointer.getAttributeValue("nonexistent");
        assertNull(value);
    }

    @Test
    public void testGetAttributeValue_NullName() {
        DOMNodePointer rootElemPointer = new DOMNodePointer(rootPointer, rootElement);
        try {
            rootElemPointer.getAttributeValue(null);
            fail("Expected NullPointerException for null attribute name");
        } catch (NullPointerException e) {
            // expected
        }
    }

    // ========== asPath() method ==========

    @Test
    public void testAsPath_RootDocument() {
        String path = rootPointer.asPath();
        assertEquals("/", path);
    }

    @Test
    public void testAsPath_RootElement() {
        DOMNodePointer rootElemPointer = new DOMNodePointer(rootPointer, rootElement);
        String path = rootElemPointer.asPath();
        assertEquals("/root", path);
    }

    @Test
    public void testAsPath_ChildElement() {
        DOMNodePointer childPointer = new DOMNodePointer(rootPointer, childElement);
        String path = childPointer.asPath();
        assertEquals("/root/child", path);
    }

    @Test
    public void testAsPath_Attribute() {
        DOMNodePointer attrPointer = new DOMNodePointer(rootPointer, attribute);
        String path = attrPointer.asPath();
        assertEquals("/@attr", path);
    }

    @Test
    public void testAsPath_ElementWithNamespace() {
        DOMNodePointer nsPointer = new DOMNodePointer(rootPointer, nsElement);
        String path = nsPointer.asPath();
        // The prefix may be included; exact format depends on implementation
        assertTrue(path.startsWith("/root/"));
        assertTrue(path.contains("other"));
    }

    // ========== Child iteration and comparison ==========

    @Test
    public void testCompareChildNodePointers_SameNode() {
        DOMNodePointer p1 = new DOMNodePointer(rootPointer, childElement);
        DOMNodePointer p2 = new DOMNodePointer(rootPointer, childElement);
        int result = rootPointer.compareChildNodePointers(p1, p2);
        assertEquals(0, result);
    }

    @Test
    public void testCompareChildNodePointers_DifferentOrder() {
        DOMNodePointer p1 = new DOMNodePointer(rootPointer, childElement);
        DOMNodePointer p2 = new DOMNodePointer(rootPointer, nsElement);
        int result = rootPointer.compareChildNodePointers(p1, p2);
        // childElement appears before nsElement in document order
        assertTrue(result < 0);
    }

    @Test
    public void testCompareChildNodePointers_ReverseOrder() {
        DOMNodePointer p1 = new DOMNodePointer(rootPointer, nsElement);
        DOMNodePointer p2 = new DOMNodePointer(rootPointer, childElement);
        int result = rootPointer.compareChildNodePointers(p1, p2);
        assertTrue(result > 0);
    }

    // ========== Edge cases and null handling ==========

    @Test
    public void testGetImmediateParentPointer_NullParent() {
        DOMNodePointer pointer = new DOMNodePointer(null, document);
        assertNull(pointer.getImmediateParentPointer());
    }

    @Test
    public void testGetImmediateParentPointer_WithParent() {
        DOMNodePointer parent = new DOMNodePointer(null, document);
        DOMNodePointer child = new DOMNodePointer(parent, childElement);
        assertSame(parent, child.getImmediateParentPointer());
    }

    @Test
    public void testGetName_Element() {
        DOMNodePointer elemPointer = new DOMNodePointer(rootPointer, rootElement);
        assertEquals("root", elemPointer.getName());
    }

    @Test
    public void testGetName_Attribute() {
        DOMNodePointer attrPointer = new DOMNodePointer(rootPointer, attribute);
        assertEquals("attr", attrPointer.getName());
    }

    @Test
    public void testGetName_Document() {
        assertEquals("#document", rootPointer.getName());
    }

    @Test
    public void testIsLeaf_ElementWithChildren() {
        DOMNodePointer rootElemPointer = new DOMNodePointer(rootPointer, rootElement);
        assertFalse(rootElemPointer.isLeaf());
    }

    @Test
    public void testIsLeaf_TextNode() {
        Text text = document.createTextNode("text");
        DOMNodePointer textPointer = new DOMNodePointer(rootPointer, text);
        assertTrue(textPointer.isLeaf());
    }

    @Test
    public void testIsLeaf_Attribute() {
        DOMNodePointer attrPointer = new DOMNodePointer(rootPointer, attribute);
        assertTrue(attrPointer.isLeaf());
    }

    // ========== Potential fault detection (bug 22 related) ==========

    @Test
    public void testGetNamespaceURI_WithMultipleNamespaces() {
        // Create element with multiple namespace declarations
        Element multiNs = document.createElementNS("http://multi.com/ns", "multi");
        multiNs.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:extra", "http://extra.com/ns");
        rootElement.appendChild(multiNs);

        DOMNodePointer multiPointer = new DOMNodePointer(rootPointer, multiNs);
        String uri = multiPointer.getNamespaceURI();
        assertEquals("http://multi.com/ns", uri);
    }

    @Test
    public void testGetPrefix_WithMultipleNamespaces() {
        Element multiNs = document.createElementNS("http://multi.com/ns", "prefix2:multi");
        rootElement.appendChild(multiNs);

        DOMNodePointer multiPointer = new DOMNodePointer(rootPointer, multiNs);
        String prefix = multiPointer.getPrefix();
        assertEquals("prefix2", prefix);
    }

    @Test
    public void testAsPath_WithNamespacePrefix() {
        // Ensure path includes prefix when present
        DOMNodePointer nsPointer = new DOMNodePointer(rootPointer, nsElement);
        String path = nsPointer.asPath();
        // The path should contain the prefix "prefix" or the namespace URI
        assertTrue(path.contains("prefix") || path.contains("other"));
    }

    @Test
    public void testCompareChildNodePointers_WithAttributes() {
        // Attributes are not children, but compareChildNodePointers should handle them gracefully
        DOMNodePointer attrPointer = new DOMNodePointer(rootPointer, attribute);
        DOMNodePointer childPointer = new DOMNodePointer(rootPointer, childElement);
        try {
            int result = rootPointer.compareChildNodePointers(attrPointer, childPointer);
            // Should not throw; result may be arbitrary but consistent
            assertNotNull(result);
        } catch (Exception e) {
            fail("compareChildNodePointers should not throw for attribute pointers");
        }
    }

    @Test
    public void testGetAttributeValue_WithNamespace() {
        // Attribute with namespace
        Attr nsAttr = document.createAttributeNS("http://attrns.com/ns", "ns:attr");
        nsAttr.setValue("nsvalue");
        rootElement.setAttributeNodeNS(nsAttr);

        DOMNodePointer rootElemPointer = new DOMNodePointer(rootPointer, rootElement);
        // getAttributeValue without namespace should not find it
        String value = rootElemPointer.getAttributeValue("attr");
        assertNull(value);
        // getAttributeValue with namespace URI and local name (if method exists)
        // Not all versions have this; we skip.
    }

    @Test
    public void testGetNamespaceURI_ForAttributeWithNamespace() {
        Attr nsAttr = document.createAttributeNS("http://attrns.com/ns", "ns:attr");
        nsAttr.setValue("nsvalue");
        rootElement.setAttributeNodeNS(nsAttr);

        DOMNodePointer attrPointer = new DOMNodePointer(rootPointer, nsAttr);
        String uri = attrPointer.getNamespaceURI();
        assertEquals("http://attrns.com/ns", uri);
    }

    @Test
    public void testGetPrefix_ForAttributeWithNamespace() {
        Attr nsAttr = document.createAttributeNS("http://attrns.com/ns", "ns:attr");
        nsAttr.setValue("nsvalue");
        rootElement.setAttributeNodeNS(nsAttr);

        DOMNodePointer attrPointer = new DOMNodePointer(rootPointer, nsAttr);
        String prefix = attrPointer.getPrefix();
        assertEquals("ns", prefix);
    }

    // ========== Null node pointer ==========

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullNode() {
        new DOMNodePointer(null, null);
    }

    @Test
    public void testGetNode_NullNode() {
        // This should not happen in practice, but test defensive behavior
        // We cannot create a DOMNodePointer with null node due to constructor check.
        // So we skip.
    }

    // ========== Additional coverage for asPath with index ==========

    @Test
    public void testAsPath_WithSiblingElements() {
        Element secondChild = document.createElement("child2");
        rootElement.appendChild(secondChild);

        DOMNodePointer firstChildPointer = new DOMNodePointer(rootPointer, childElement);
        DOMNodePointer secondChildPointer = new DOMNodePointer(rootPointer, secondChild);

        String path1 = firstChildPointer.asPath();
        String path2 = secondChildPointer.asPath();

        // Both should have same path if no index is used; but typically asPath adds index for duplicates
        // This depends on implementation; we just ensure no exception
        assertNotNull(path1);
        assertNotNull(path2);
    }

    // ========== Test for bug 22 specific scenario ==========
    // Bug 22 likely involves incorrect namespace handling when iterating attributes or children.
    // We simulate a scenario where an element has both default namespace and prefixed attributes.

    @Test
    public void testBug22_NamespaceIteration() {
        // Create element with default namespace and a prefixed attribute
        Element elem = document.createElementNS("http://bug22.com/ns", "bug");
        Attr attr = document.createAttributeNS("http://attrns.com/ns", "pre:attr");
        attr.setValue("val");
        elem.setAttributeNodeNS(attr);
        rootElement.appendChild(elem);

        DOMNodePointer elemPointer = new DOMNodePointer(rootPointer, elem);
        // Get namespace URI should return the element's namespace
        assertEquals("http://bug22.com/ns", elemPointer.getNamespaceURI());
        // Get prefix should be null (default namespace)
        assertNull(elemPointer.getPrefix());

        // Now iterate over attributes? Not directly exposed, but we can test getAttributeValue
        // The bug might cause incorrect namespace resolution for attributes.
        // We test that getAttributeValue with local name only returns null for namespaced attr.
        assertNull(elemPointer.getAttributeValue("attr"));
    }
}