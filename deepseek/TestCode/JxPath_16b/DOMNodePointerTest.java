package org.apache.commons.jxpath.ri.model.dom;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;

import static org.junit.Assert.*;

public class DOMNodePointerTest {

    private DocumentBuilder builder;
    private Document document;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        builder = factory.newDocumentBuilder();
    }

    @Test
    public void testGetNamespaceURIWithPrefix() throws Exception {
        String xml = "<root xmlns:ns='http://example.com/ns'><ns:child/></root>";
        document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        Element child = (Element) document.getDocumentElement().getFirstChild();
        DOMNodePointer pointer = new DOMNodePointer(null, child);
        String nsURI = pointer.getNamespaceURI();
        assertEquals("http://example.com/ns", nsURI);
    }

    @Test
    public void testGetNamespaceURIWithNullPrefix() throws Exception {
        String xml = "<root xmlns='http://example.com/default'><child/></root>";
        document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        Element child = (Element) document.getDocumentElement().getFirstChild();
        DOMNodePointer pointer = new DOMNodePointer(null, child);
        // When prefix is null, getNamespaceURI should return the default namespace
        String nsURI = pointer.getNamespaceURI();
        assertEquals("http://example.com/default", nsURI);
    }

    @Test
    public void testGetNamespaceURIWithNoNamespace() throws Exception {
        String xml = "<root><child/></root>";
        document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        Element child = (Element) document.getDocumentElement().getFirstChild();
        DOMNodePointer pointer = new DOMNodePointer(null, child);
        String nsURI = pointer.getNamespaceURI();
        assertNull(nsURI);
    }

    @Test
    public void testGetNamespaceURIWithPrefixButNoNamespace() throws Exception {
        // Create a node with a prefix but no namespace declaration (should not happen in well-formed XML, but can be constructed)
        // We'll create an element manually without namespace
        document = builder.newDocument();
        Element root = document.createElement("root");
        document.appendChild(root);
        Element child = document.createElementNS(null, "ns:child");
        root.appendChild(child);
        DOMNodePointer pointer = new DOMNodePointer(null, child);
        String nsURI = pointer.getNamespaceURI();
        // The prefix "ns" has no mapping, so lookupNamespaceURI should return null
        assertNull(nsURI);
    }

    @Test
    public void testGetNamespaceURIWithAttribute() throws Exception {
        String xml = "<root xmlns:ns='http://example.com/ns'><child ns:attr='value'/></root>";
        document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        Element child = (Element) document.getDocumentElement().getFirstChild();
        Attr attr = child.getAttributeNodeNS("http://example.com/ns", "attr");
        DOMNodePointer pointer = new DOMNodePointer(null, attr);
        String nsURI = pointer.getNamespaceURI();
        assertEquals("http://example.com/ns", nsURI);
    }

    @Test
    public void testGetNamespaceURIWithTextNode() throws Exception {
        String xml = "<root xmlns='http://example.com/ns'>text</root>";
        document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        Text text = (Text) document.getDocumentElement().getFirstChild();
        DOMNodePointer pointer = new DOMNodePointer(null, text);
        String nsURI = pointer.getNamespaceURI();
        assertNull(nsURI);
    }

    @Test
    public void testGetPrefix() throws Exception {
        String xml = "<root xmlns:ns='http://example.com/ns'><ns:child/></root>";
        document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        Element child = (Element) document.getDocumentElement().getFirstChild();
        DOMNodePointer pointer = new DOMNodePointer(null, child);
        String prefix = pointer.getPrefix();
        assertEquals("ns", prefix);
    }

    @Test
    public void testGetPrefixWithDefaultNamespace() throws Exception {
        String xml = "<root xmlns='http://example.com/ns'><child/></root>";
        document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        Element child = (Element) document.getDocumentElement().getFirstChild();
        DOMNodePointer pointer = new DOMNodePointer(null, child);
        String prefix = pointer.getPrefix();
        assertNull(prefix);
    }

    @Test
    public void testGetPrefixWithNoNamespace() throws Exception {
        String xml = "<root><child/></root>";
        document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        Element child = (Element) document.getDocumentElement().getFirstChild();
        DOMNodePointer pointer = new DOMNodePointer(null, child);
        String prefix = pointer.getPrefix();
        assertNull(prefix);
    }

    @Test
    public void testGetLocalName() throws Exception {
        String xml = "<root><child/></root>";
        document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        Element child = (Element) document.getDocumentElement().getFirstChild();
        DOMNodePointer pointer = new DOMNodePointer(null, child);
        String localName = pointer.getLocalName();
        assertEquals("child", localName);
    }

    @Test
    public void testGetLocalNameWithPrefix() throws Exception {
        String xml = "<root xmlns:ns='http://example.com/ns'><ns:child/></root>";
        document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        Element child = (Element) document.getDocumentElement().getFirstChild();
        DOMNodePointer pointer = new DOMNodePointer(null, child);
        String localName = pointer.getLocalName();
        assertEquals("child", localName);
    }

    @Test
    public void testGetLocalNameWithAttribute() throws Exception {
        String xml = "<root><child ns:attr='value' xmlns:ns='http://example.com/ns'/></root>";
        document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        Element child = (Element) document.getDocumentElement().getFirstChild();
        Attr attr = child.getAttributeNodeNS("http://example.com/ns", "attr");
        DOMNodePointer pointer = new DOMNodePointer(null, attr);
        String localName = pointer.getLocalName();
        assertEquals("attr", localName);
    }

    @Test
    public void testAsString() throws Exception {
        String xml = "<root>text</root>";
        document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        Text text = (Text) document.getDocumentElement().getFirstChild();
        DOMNodePointer pointer = new DOMNodePointer(null, text);
        String str = pointer.asString();
        assertEquals("text", str);
    }

    @Test
    public void testAsStringWithElement() throws Exception {
        String xml = "<root><child/></root>";
        document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        Element child = (Element) document.getDocumentElement().getFirstChild();
        DOMNodePointer pointer = new DOMNodePointer(null, child);
        String str = pointer.asString();
        assertEquals("", str);
    }

    @Test
    public void testAsStringWithAttribute() throws Exception {
        String xml = "<root><child attr='value'/></root>";
        document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        Element child = (Element) document.getDocumentElement().getFirstChild();
        Attr attr = child.getAttributeNode("attr");
        DOMNodePointer pointer = new DOMNodePointer(null, attr);
        String str = pointer.asString();
        assertEquals("value", str);
    }

    @Test
    public void testGetNameWithElement() throws Exception {
        String xml = "<root><child/></root>";
        document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        Element child = (Element) document.getDocumentElement().getFirstChild();
        DOMNodePointer pointer = new DOMNodePointer(null, child);
        String name = pointer.getName();
        assertEquals("child", name);
    }

    @Test
    public void testGetNameWithPrefixedElement() throws Exception {
        String xml = "<root xmlns:ns='http://example.com/ns'><ns:child/></root>";
        document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        Element child = (Element) document.getDocumentElement().getFirstChild();
        DOMNodePointer pointer = new DOMNodePointer(null, child);
        String name = pointer.getName();
        assertEquals("ns:child", name);
    }

    @Test
    public void testGetNameWithAttribute() throws Exception {
        String xml = "<root><child attr='value'/></root>";
        document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        Element child = (Element) document.getDocumentElement().getFirstChild();
        Attr attr = child.getAttributeNode("attr");
        DOMNodePointer pointer = new DOMNodePointer(null, attr);
        String name = pointer.getName();
        assertEquals("attr", name);
    }

    @Test
    public void testGetNameWithPrefixedAttribute() throws Exception {
        String xml = "<root xmlns:ns='http://example.com/ns'><child ns:attr='value'/></root>";
        document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        Element child = (Element) document.getDocumentElement().getFirstChild();
        Attr attr = child.getAttributeNodeNS("http://example.com/ns", "attr");
        DOMNodePointer pointer = new DOMNodePointer(null, attr);
        String name = pointer.getName();
        assertEquals("ns:attr", name);
    }

    @Test
    public void testGetImmediateNode() throws Exception {
        String xml = "<root><child/></root>";
        document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        Element child = (Element) document.getDocumentElement().getFirstChild();
        DOMNodePointer pointer = new DOMNodePointer(null, child);
        Node immediate = pointer.getImmediateNode();
        assertSame(child, immediate);
    }

    @Test
    public void testIsLeaf() throws Exception {
        String xml = "<root><child/></root>";
        document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        Element child = (Element) document.getDocumentElement().getFirstChild();
        DOMNodePointer pointer = new DOMNodePointer(null, child);
        assertTrue(pointer.isLeaf());
    }

    @Test
    public void testIsLeafWithText() throws Exception {
        String xml = "<root>text</root>";
        document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        Text text = (Text) document.getDocumentElement().getFirstChild();
        DOMNodePointer pointer = new DOMNodePointer(null, text);
        assertTrue(pointer.isLeaf());
    }

    @Test
    public void testIsLeafWithElementWithChildren() throws Exception {
        String xml = "<root><parent><child/></parent></root>";
        document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        Element parent = (Element) document.getDocumentElement().getFirstChild();
        DOMNodePointer pointer = new DOMNodePointer(null, parent);
        assertFalse(pointer.isLeaf());
    }

    @Test
    public void testGetChildCount() throws Exception {
        String xml = "<root><a/><b/><c/></root>";
        document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        Element root = document.getDocumentElement();
        DOMNodePointer pointer = new DOMNodePointer(null, root);
        int count = pointer.getChildCount();
        assertEquals(3, count);
    }

    @Test
    public void testGetChildCountWithText() throws Exception {
        String xml = "<root>text</root>";
        document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        Element root = document.getDocumentElement();
        DOMNodePointer pointer = new DOMNodePointer(null, root);
        int count = pointer.getChildCount();
        assertEquals(1, count);
    }

    @Test
    public void testGetChildCountWithNoChildren() throws Exception {
        String xml = "<root/>";
        document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        Element root = document.getDocumentElement();
        DOMNodePointer pointer = new DOMNodePointer(null, root);
        int count = pointer.getChildCount();
        assertEquals(0, count);
    }

    @Test
    public void testGetChildCountWithAttribute() throws Exception {
        String xml = "<root attr='value'/>";
        document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        Element root = document.getDocumentElement();
        DOMNodePointer pointer = new DOMNodePointer(null, root);
        // Attributes are not children in DOM, so count should be 0
        int count = pointer.getChildCount();
        assertEquals(0, count);
    }

    @Test
    public void testGetChildCountWithDocumentNode() throws Exception {
        String xml = "<root/>";
        document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        DOMNodePointer pointer = new DOMNodePointer(null, document);
        int count = pointer.getChildCount();
        assertEquals(1, count); // document has one child (root element)
    }

    @Test
    public void testGetImmediateNodeWithDocument() throws Exception {
        String xml = "<root/>";
        document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        DOMNodePointer pointer = new DOMNodePointer(null, document);
        Node immediate = pointer.getImmediateNode();
        assertSame(document, immediate);
    }

    @Test
    public void testGetNameWithDocument() throws Exception {
        String xml = "<root/>";
        document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        DOMNodePointer pointer = new DOMNodePointer(null, document);
        String name = pointer.getName();
        assertEquals("#document", name);
    }

    @Test
    public void testGetLocalNameWithDocument() throws Exception {
        String xml = "<root/>";
        document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        DOMNodePointer pointer = new DOMNodePointer(null, document);
        String localName = pointer.getLocalName();
        assertEquals("#document", localName);
    }

    @Test
    public void testGetNamespaceURIWithDocument() throws Exception {
        String xml = "<root/>";
        document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        DOMNodePointer pointer = new DOMNodePointer(null, document);
        String nsURI = pointer.getNamespaceURI();
        assertNull(nsURI);
    }

    @Test
    public void testGetPrefixWithDocument() throws Exception {
        String xml = "<root/>";
        document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        DOMNodePointer pointer = new DOMNodePointer(null, document);
        String prefix = pointer.getPrefix();
        assertNull(prefix);
    }

    @Test
    public void testAsStringWithDocument() throws Exception {
        String xml = "<root/>";
        document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        DOMNodePointer pointer = new DOMNodePointer(null, document);
        String str = pointer.asString();
        assertEquals("", str);
    }

    @Test
    public void testIsLeafWithDocument() throws Exception {
        String xml = "<root/>";
        document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        DOMNodePointer pointer = new DOMNodePointer(null, document);
        assertFalse(pointer.isLeaf());
    }

    @Test
    public void testGetChildCountWithNullNode() throws Exception {
        // Create a pointer with null node (should not happen normally, but test for robustness)
        DOMNodePointer pointer = new DOMNodePointer(null, null);
        int count = pointer.getChildCount();
        assertEquals(0, count);
    }

    @Test(expected = NullPointerException.class)
    public void testGetNamespaceURIWithNullNode() throws Exception {
        // This should throw NPE if the implementation doesn't handle null node
        DOMNodePointer pointer = new DOMNodePointer(null, null);
        pointer.getNamespaceURI();
    }

    @Test(expected = NullPointerException.class)
    public void testGetPrefixWithNullNode() throws Exception {
        DOMNodePointer pointer = new DOMNodePointer(null, null);
        pointer.getPrefix();
    }

    @Test(expected = NullPointerException.class)
    public void testGetLocalNameWithNullNode() throws Exception {
        DOMNodePointer pointer = new DOMNodePointer(null, null);
        pointer.getLocalName();
    }

    @Test(expected = NullPointerException.class)
    public void testAsStringWithNullNode() throws Exception {
        DOMNodePointer pointer = new DOMNodePointer(null, null);
        pointer.asString();
    }

    @Test(expected = NullPointerException.class)
    public void testGetNameWithNullNode() throws Exception {
        DOMNodePointer pointer = new DOMNodePointer(null, null);
        pointer.getName();
    }

    @Test(expected = NullPointerException.class)
    public void testIsLeafWithNullNode() throws Exception {
        DOMNodePointer pointer = new DOMNodePointer(null, null);
        pointer.isLeaf();
    }

    @Test(expected = NullPointerException.class)
    public void testGetImmediateNodeWithNullNode() throws Exception {
        DOMNodePointer pointer = new DOMNodePointer(null, null);
        pointer.getImmediateNode();
    }
}