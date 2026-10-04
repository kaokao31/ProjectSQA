package org.apache.commons.jxpath.ri.model.dom;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.parser.XPathParser;
import org.apache.commons.jxpath.util.ValueUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Locale;

import static org.junit.Assert.*;

public class DOMNodePointerTest {

    private Document document;
    private DOMNodePointer documentPointer;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        document = db.newDocument();
        documentPointer = new DOMNodePointer(document, Locale.getDefault());
    }

    @After
    public void tearDown() {
        document = null;
        documentPointer = null;
    }

    @Test
    public void testConstructorsAndBasicProperties() {
        Element element = document.createElement("root");
        DOMNodePointer ptr1 = new DOMNodePointer(element, Locale.ENGLISH);
        assertEquals(Locale.ENGLISH, ptr1.getLocale());
        assertSame(element, ptr1.getNode());

        Element element2 = document.createElement("child");
        DOMNodePointer ptr2 = new DOMNodePointer(ptr1, element2);
        assertSame(element2, ptr2.getNode());
        assertEquals(ptr1, ptr2.getImmediateParentPointer());

        assertFalse(ptr1.isCollection());
        assertEquals(1, ptr1.getLength());
        assertFalse(ptr1.isLeaf());
    }

    @Test
    public void testNodeTypesAndNames() {
        // Element
        Element elem = document.createElementNS("http://example.com", "ns:element");
        DOMNodePointer elemPtr = new DOMNodePointer(elem, Locale.getDefault());
        assertEquals("ns:element", elemPtr.getName().getName());
        assertEquals("http://example.com", elemPtr.getNamespaceURI());

        Element elemNoNs = document.createElement("elementNoNs");
        DOMNodePointer elemNoNsPtr = new DOMNodePointer(elemNoNs, Locale.getDefault());
        assertNull(elemNoNsPtr.getNamespaceURI());

        // Attr
        Attr attr = document.createAttribute("attrName");
        attr.setValue("val");
        DOMNodePointer attrPtr = new DOMNodePointer(elemNoNsPtr, attr);
        assertEquals("attrName", attrPtr.getName().getName());
        assertFalse(attrPtr.isLeaf());

        // Text
        Text text = document.createTextNode("text content");
        DOMNodePointer textPtr = new DOMNodePointer(elemNoNsPtr, text);
        assertNull(textPtr.getName().getName());
        assertTrue(textPtr.isLeaf());

        // Comment
        Comment comment = document.createComment("comment");
        DOMNodePointer commentPtr = new DOMNodePointer(elemNoNsPtr, comment);
        assertNull(commentPtr.getName().getName());
        assertTrue(commentPtr.isLeaf());

        // ProcessingInstruction
        ProcessingInstruction pi = document.createProcessingInstruction("target", "data");
        DOMNodePointer piPtr = new DOMNodePointer(elemNoNsPtr, pi);
        assertEquals("target", piPtr.getName().getName());
        assertTrue(piPtr.isLeaf());

        // Document
        DOMNodePointer docPtr = new DOMNodePointer(document, Locale.getDefault());
        assertNull(docPtr.getName().getName());
        assertFalse(docPtr.isLeaf());
    }

    @Test
    public void testGetNamespaceURIByPrefix() {
        Element elem = document.createElementNS("http://root.ns", "root");
        elem.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xml", "http://www.w3.org/XML/1998/namespace");
        elem.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:prefix1", "http://example.com/ns1");
        document.appendChild(elem);

        DOMNodePointer ptr = new DOMNodePointer(elem, Locale.getDefault());
        assertEquals("http://example.com/ns1", ptr.getNamespaceURI("prefix1"));
        assertEquals("http://www.w3.org/XML/1998/namespace", ptr.getNamespaceURI("xml"));
        assertNull(ptr.getNamespaceURI("nonexistent"));
        
        // Test namespace resolver with null/empty prefix
        assertNull(ptr.getNamespaceURI(null));
        assertNull(ptr.getNamespaceURI(""));
    }

    @Test
    public void testValueOperations() {
        Element elem = document.createElement("elem");
        Text text1 = document.createTextNode("Hello ");
        Text text2 = document.createTextNode("World");
        elem.appendChild(text1);
        elem.appendChild(text2);

        DOMNodePointer ptr = new DOMNodePointer(elem, Locale.getDefault());
        assertEquals("Hello World", ptr.getValue());

        ptr.setValue("New Value");
        assertEquals("New Value", ptr.getValue());
        
        // Remove value / set null
        ptr.setValue(null);
        assertEquals("", ptr.getValue());
    }

    @Test
    public void testAttributeValueOperations() {
        Element elem = document.createElement("elem");
        Attr attr = document.createAttribute("myAttr");
        attr.setValue("oldVal");
        elem.setAttributeNode(attr);

        DOMNodePointer attrPtr = new DOMNodePointer(documentPointer, attr);
        assertEquals("oldVal", attrPtr.getValue());

        attrPtr.setValue("newVal");
        assertEquals("newVal", attr.getValue());

        attrPtr.setValue(null);
        assertEquals("", attr.getValue());
        
        // Test remove
        attrPtr.remove();
        assertNull(elem.getAttributeNode("myAttr"));
    }

    @Test
    public void testElementRemove() {
        Element parent = document.createElement("parent");
        Element child = document.createElement("child");
        parent.appendChild(child);
        document.appendChild(parent);

        DOMNodePointer childPtr = new DOMNodePointer(new DOMNodePointer(parent, Locale.getDefault()), child);
        childPtr.remove();
        assertNull(child.getParentNode());
        
        // Remove document itself should do nothing or not throw severe error
        documentPointer.remove();
    }

    @Test
    public void testHashCodeAndEquals() {
        Element elem1 = document.createElement("elem");
        Element elem2 = document.createElement("elem");

        DOMNodePointer ptr1 = new DOMNodePointer(elem1, Locale.getDefault());
        DOMNodePointer ptr2 = new DOMNodePointer(elem1, Locale.getDefault());
        DOMNodePointer ptr3 = new DOMNodePointer(elem2, Locale.getDefault());

        assertTrue(ptr1.equals(ptr1));
        assertTrue(ptr1.equals(ptr2));
        assertFalse(ptr1.equals(ptr3));
        assertFalse(ptr1.equals(null));
        assertFalse(ptr1.equals("someString"));

        assertEquals(ptr1.hashCode(), ptr2.hashCode());
    }

    @Test
    public void testCompareChildNodePointers() {
        Element parent = document.createElement("parent");
        Element child1 = document.createElement("child1");
        Element child2 = document.createElement("child2");
        parent.appendChild(child1);
        parent.appendChild(child2);

        DOMNodePointer ptr1 = new DOMNodePointer(documentPointer, child1);
        DOMNodePointer ptr2 = new DOMNodePointer(documentPointer, child2);

        assertTrue(ptr1.compareChildNodePointers(ptr1, ptr2) < 0);
        assertTrue(ptr2.compareChildNodePointers(ptr2, ptr1) > 0);
        assertEquals(0, ptr1.compareChildNodePointers(ptr1, ptr1));
    }

    @Test
    public void testTestNodePointerWithLocalNameAndNamespace() {
        Element elem = document.createElementNS("http://ns.com", "prefix:localName");
        DOMNodePointer ptr = new DOMNodePointer(elem, Locale.getDefault());

        QName qNameMatch = new QName("http://ns.com", "localName");
        assertTrue(ptr.testNode(qNameMatch));

        QName qNameWildcardNs = new QName("http://ns.com", "*");
        assertTrue(ptr.testNode(qNameWildcardNs));

        QName qNameWildcardLocal = new QName(null, "*");
        assertTrue(ptr.testNode(qNameWildcardLocal));

        QName qNameMismatchLocal = new QName("http://ns.com", "otherName");
        assertFalse(ptr.testNode(qNameMismatchLocal));

        QName qNameMismatchNs = new QName("http://wrong.com", "localName");
        assertFalse(ptr.testNode(qNameMismatchNs));
    }

    @Test
    public void testTestNodePointerWithNullNode() {
        DOMNodePointer nullNodePtr = new DOMNodePointer(null, Locale.getDefault());
        assertFalse(nullNodePtr.testNode(new QName("test")));
    }

    @Test
    public void testAsPath() {
        Element elem = document.createElement("root");
        DOMNodePointer ptr = new DOMNodePointer(elem, Locale.getDefault());
        assertNotNull(ptr.asPath());
    }
}