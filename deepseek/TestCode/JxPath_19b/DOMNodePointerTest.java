package org.apache.commons.jxpath.ri.model.dom;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;

import static org.junit.Assert.*;

/**
 * Test suite for DOMNodePointer.
 * Designed to achieve high coverage and detect faults, including Defects4J JxPath-19.
 */
public class DOMNodePointerTest {

    private Document document;
    private Element rootElement;
    private JXPathContext context;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.newDocument();
        rootElement = document.createElement("root");
        document.appendChild(rootElement);
        context = JXPathContext.newContext(document);
    }

    // Helper to create a DOMNodePointer for a given node
    private DOMNodePointer createPointer(Node node) {
        return new DOMNodePointer(node);
    }

    // Helper to create a DOMNodePointer with parent
    private DOMNodePointer createPointer(DOMNodePointer parent, Node node) {
        return new DOMNodePointer(parent, node);
    }

    // --- Basic creation and getImmediateNode ---

    @Test
    public void testConstructorWithNode() {
        DOMNodePointer pointer = createPointer(rootElement);
        assertNotNull(pointer);
        assertEquals(rootElement, pointer.getImmediateNode());
    }

    @Test
    public void testConstructorWithParentAndNode() {
        DOMNodePointer parent = createPointer(document);
        DOMNodePointer child = createPointer(parent, rootElement);
        assertNotNull(child);
        assertEquals(rootElement, child.getImmediateNode());
    }

    // --- Test getImmediateNode with various node types ---

    @Test
    public void testGetImmediateNodeWithDocumentNode() {
        // Directly using Document node
        DOMNodePointer pointer = createPointer(document);
        assertNotNull(pointer.getImmediateNode());
        assertEquals(document, pointer.getImmediateNode());
    }

    @Test
    public void testGetImmediateNodeWithTextNode() {
        Text text = document.createTextNode("text");
        rootElement.appendChild(text);
        DOMNodePointer pointer = createPointer(text);
        assertEquals(text, pointer.getImmediateNode());
    }

    @Test
    public void testGetImmediateNodeWithAttribute() {
        Attr attr = document.createAttribute("attr");
        attr.setValue("val");
        rootElement.setAttributeNode(attr);
        DOMNodePointer pointer = createPointer(attr);
        assertEquals(attr, pointer.getImmediateNode());
    }

    @Test
    public void testGetImmediateNodeWithComment() {
        Comment comment = document.createComment("comment");
        rootElement.appendChild(comment);
        DOMNodePointer pointer = createPointer(comment);
        assertEquals(comment, pointer.getImmediateNode());
    }

    // --- isCollection and isLeaf ---

    @Test
    public void testIsCollection() {
        DOMNodePointer pointer = createPointer(rootElement);
        assertFalse(pointer.isCollection());
    }

    @Test
    public void testIsLeafWithElementWithChildren() {
        Element child = document.createElement("child");
        rootElement.appendChild(child);
        DOMNodePointer pointer = createPointer(rootElement);
        assertFalse(pointer.isLeaf());
    }

    @Test
    public void testIsLeafWithElementWithoutChildren() {
        DOMNodePointer pointer = createPointer(rootElement);
        assertFalse(pointer.isLeaf()); // rootElement has no children but text is not null? Actually empty element -> not leaf
    }

    @Test
    public void testIsLeafWithTextNode() {
        Text text = document.createTextNode("text");
        rootElement.appendChild(text);
        DOMNodePointer pointer = createPointer(text);
        assertTrue(pointer.isLeaf());
    }

    @Test
    public void testIsLeafWithAttribute() {
        Attr attr = document.createAttribute("attr");
        attr.setValue("val");
        rootElement.setAttributeNode(attr);
        DOMNodePointer pointer = createPointer(attr);
        assertTrue(pointer.isLeaf());
    }

    // --- isNamespacePointer ---

    @Test
    public void testIsNamespacePointerWithElement() {
        DOMNodePointer pointer = createPointer(rootElement);
        assertFalse(pointer.isNamespacePointer());
    }

    @Test
    public void testIsNamespacePointerWithDocumentNode() {
        DOMNodePointer pointer = createPointer(document);
        // Document node: should be false, but JxPath-19 caused NPE
        assertFalse(pointer.isNamespacePointer());
    }

    @Test
    public void testIsNamespacePointerWithNamespaceNode() {
        // Create a namespace aware document to get a namespace node
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document nsDoc = builder.newDocument();
            Element nsRoot = nsDoc.createElementNS("http://example.com/ns", "ns:root");
            nsDoc.appendChild(nsRoot);
            // Obtain namespace node: not directly supported, but we can test with a pointer to an attribute that is xmlns
            Attr xmlns = nsDoc.createAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:pre");
            xmlns.setValue("http://my.ns");
            nsRoot.setAttributeNodeNS(xmlns);
            DOMNodePointer pointer = createPointer(xmlns);
            assertTrue(pointer.isNamespacePointer());
        } catch (Exception e) {
            fail("Failed to create namespace-aware document: " + e.getMessage());
        }
    }

    // --- asPath ---

    @Test
    public void testAsPathForElement() {
        DOMNodePointer pointer = createPointer(rootElement);
        String path = pointer.asPath();
        assertNotNull(path);
        assertTrue(path.startsWith("/"));
    }

    @Test
    public void testAsPathForDocumentNode() {
        DOMNodePointer pointer = createPointer(document);
        // Should not throw exception (JxPath-19)
        String path = pointer.asPath();
        assertEquals("/", path);
    }

    @Test
    public void testAsPathForNestedElement() {
        Element child = document.createElement("child");
        rootElement.appendChild(child);
        DOMNodePointer pointer = createPointer(child);
        String path = pointer.asPath();
        assertEquals("/root/child", path);
    }

    @Test
    public void testAsPathForAttribute() {
        Attr attr = document.createAttribute("attr");
        attr.setValue("val");
        rootElement.setAttributeNode(attr);
        DOMNodePointer pointer = createPointer(attr);
        String path = pointer.asPath();
        assertTrue(path.contains("@attr"));
    }

    @Test
    public void testAsPathForTextNode() {
        Text text = document.createTextNode("text");
        rootElement.appendChild(text);
        DOMNodePointer pointer = createPointer(text);
        String path = pointer.asPath();
        assertEquals("/root/text()", path);
    }

    // --- getNamespaceURI ---

    @Test
    public void testGetNamespaceURIForElementWithoutNamespace() {
        DOMNodePointer pointer = createPointer(rootElement);
        assertNull(pointer.getNamespaceURI());
    }

    @Test
    public void testGetNamespaceURIForDocumentNode() {
        DOMNodePointer pointer = createPointer(document);
        // Should not throw exception (JxPath-19)
        assertNull(pointer.getNamespaceURI());
    }

    // --- getPointerByID ---

    @Test
    public void testGetPointerByIDNoAttribute() {
        // No ID attribute present
        Pointer p = DOMNodePointer.getPointerByID(context, "nonexistent");
        assertNull(p);
    }

    @Test
    public void testGetPointerByIDWithIDAttribute() {
        // Using DTD to define ID attribute? Simpler: set ID attribute via DOM
        rootElement.setAttribute("id", "myid");
        // But the attribute is not recognized as ID type; getPointerByID may not find it.
        // This test is more for coverage; may not find a pointer.
        Pointer p = DOMNodePointer.getPointerByID(context, "myid");
        // Without schema, may be null.
        assertNull(p);
    }

    // --- getDefaultNamespaceURI ---

    @Test
    public void testGetDefaultNamespaceURI() {
        String uri = DOMNodePointer.getDefaultNamespaceURI(context);
        assertNull(uri); // No default namespace in this document
    }

    // --- Edge cases to trigger bugs ---

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullNode() {
        new DOMNodePointer(null);
    }

    @Test
    public void testAsPathForDocumentWithNoDocumentElement() throws Exception {
        // Create a document without a document element
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document emptyDoc = builder.newDocument();
        DOMNodePointer pointer = createPointer(emptyDoc);
        String path = pointer.asPath();
        assertEquals("/", path);
    }

    @Test
    public void testIsNamespacePointerOnDocumentAfterRemoveChild() throws Exception {
        // Create document with element, remove it, then test
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element elem = doc.createElement("elem");
        doc.appendChild(elem);
        doc.removeChild(elem);
        DOMNodePointer pointer = createPointer(doc);
        // Should not throw NPE (related to JxPath-19)
        assertFalse(pointer.isNamespacePointer());
    }

    @Test
    public void testGetImmediateNodeForDocumentWithNoDocumentElement() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        DOMNodePointer pointer = createPointer(doc);
        assertNotNull(pointer.getImmediateNode());
        assertEquals(doc, pointer.getImmediateNode());
    }

    // --- Additional coverage for internal methods ---

    @Test
    public void testAttributePointer() {
        Attr attr = document.createAttribute("attr");
        attr.setValue("val");
        rootElement.setAttributeNode(attr);
        DOMNodePointer parent = createPointer(rootElement);
        DOMNodePointer attrPointer = createPointer(parent, attr);
        // Ensure it is an attribute pointer
        assertFalse(attrPointer.isCollection());
        assertTrue(attrPointer.isLeaf());
    }

    @Test
    public void testMultipleChildrenAsPath() {
        Element child1 = document.createElement("child");
        Element child2 = document.createElement("child");
        rootElement.appendChild(child1);
        rootElement.appendChild(child2);
        DOMNodePointer pointer2 = createPointer(child2);
        String path = pointer2.asPath();
        assertEquals("/root/child[2]", path);
    }

    @Test
    public void testNamespacePrefixResolution() {
        // This test requires namespace-aware parsing
        String xml = "<root xmlns:ns='http://ns'><ns:child/></root>";
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
            NodeList children = doc.getDocumentElement().getChildNodes();
            Node nsChild = children.item(0);
            DOMNodePointer pointer = createPointer(nsChild);
            // Should not throw
            assertNotNull(pointer.getNamespaceURI());
            assertEquals("http://ns", pointer.getNamespaceURI());
        } catch (Exception e) {
            fail("Parsing failed: " + e.getMessage());
        }
    }

    @Test
    public void testPointerByIDWithAttributeIDType() throws Exception {
        // Use DOM Level 3 to set id attribute
        String xml = "<!DOCTYPE doc [\n" +
                     "  <!ATTLIST doc id ID #IMPLIED>\n" +
                     "]><doc id='myid'>text</doc>";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        JXPathContext ctx = JXPathContext.newContext(doc);
        Pointer p = DOMNodePointer.getPointerByID(ctx, "myid");
        assertNotNull(p);
        assertTrue(p instanceof DOMNodePointer);
    }
}