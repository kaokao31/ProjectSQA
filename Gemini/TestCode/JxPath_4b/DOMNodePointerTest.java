package org.apache.commons.jxpath.ri.model.dom;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.util.TypeValueUtils;
import org.apache.html.dom.HTMLDocumentImpl;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.dom.ElementImpl;
import org.apache.xerces.dom.TextImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

import java.util.Locale;

import static org.junit.Assert.*;

public class DOMNodePointerTest {

    private Document document;
    private DOMNodePointer rootPointer;

    @Before
    public void setUp() throws Exception {
        document = new DocumentImpl();
        rootPointer = new DOMNodePointer(document, Locale.ENGLISH);
    }

    @After
    public void tearDown() throws Exception {
        document = null;
        rootPointer = null;
    }

    @Test
    public void testConstructionAndBasics() {
        assertNotNull(rootPointer);
        assertEquals(document, rootPointer.getBaseValue());
        assertFalse(rootPointer.isCollection());
        assertEquals(1, rootPointer.getLength());
        assertFalse(rootPointer.isLeaf());
        assertNull(rootPointer.getNamespaceURI());
        assertEquals("", rootPointer.getNamespaceURI(""));
    }

    @Test
    public void testNodeNames() {
        // Document node name
        QName docName = rootPointer.getName();
        assertNull(docName.getName());

        Element element = document.createElement("testElement");
        DOMNodePointer elPointer = new DOMNodePointer(rootPointer, element);
        assertEquals("testElement", elPointer.getName().getName());

        Element nsElement = document.createElementNS("http://example.com", "prefix:localName");
        DOMNodePointer nsElPointer = new DOMNodePointer(rootPointer, nsElement);
        assertEquals("localName", nsElPointer.getName().getName());
        assertEquals("prefix", nsElPointer.getName().getPrefix());

        Attr attr = document.createAttribute("testAttr");
        DOMNodePointer attrPointer = new DOMNodePointer(rootPointer, attr);
        assertEquals("testAttr", attrPointer.getName().getName());
    }

    @Test
    public void testGetNamespaceURI() {
        Element element = document.createElementNS("http://example.com/ns", "ns:element");
        DOMNodePointer elPointer = new DOMNodePointer(rootPointer, element);
        assertEquals("http://example.com/ns", elPointer.getNamespaceURI("ns"));
        assertEquals("http://example.com/ns", elPointer.getNamespaceURI(""));

        Element elementWithoutNs = document.createElement("element");
        DOMNodePointer elNoNsPointer = new DOMNodePointer(rootPointer, elementWithoutNs);
        assertNull(elNoNsPointer.getNamespaceURI(""));
    }

    @Test
    public void testGetImmediateChildersAndTestNode() {
        Element parent = document.createElement("parent");
        Element child1 = document.createElement("child1");
        Element child2 = document.createElement("child2");
        parent.appendChild(child1);
        parent.appendChild(child2);

        DOMNodePointer parentPointer = new DOMNodePointer(rootPointer, parent);
        NodeTest nodeTest = new NodeNameTest(new QName("child1"));
        
        NodePointer childPtr = parentPointer.childIterator(nodeTest, false, null).getNodePointer();
        assertNotNull(childPtr);
        assertEquals(child1, childPtr.getNode());
    }

    @Test
    public void testTestNodeTypes() {
        Element element = document.createElement("elem");
        DOMNodePointer elPtr = new DOMNodePointer(rootPointer, element);

        // NodeTypeTest - ELEMENT
        assertTrue(DOMNodePointer.testNode(element, new NodeTypeTest(NodePointer.NODE_TYPE_NODE)));
        assertTrue(DOMNodePointer.testNode(element, new NodeTypeTest(NodePointer.NODE_TYPE_ELEMENT)));
        assertFalse(DOMNodePointer.testNode(element, new NodeTypeTest(NodePointer.NODE_TYPE_TEXT)));

        // Text node
        Text text = document.createTextNode("hello");
        DOMNodePointer textPtr = new DOMNodePointer(rootPointer, text);
        assertTrue(DOMNodePointer.testNode(text, new NodeTypeTest(NodePointer.NODE_TYPE_TEXT)));
        assertTrue(DOMNodePointer.testNode(text, new NodeTypeTest(NodePointer.NODE_TYPE_NODE)));
        assertFalse(DOMNodePointer.testNode(text, new NodeTypeTest(NodePointer.NODE_TYPE_ELEMENT)));

        // Comment node
        Comment comment = document.createComment("comm");
        assertTrue(DOMNodePointer.testNode(comment, new NodeTypeTest(NodePointer.NODE_TYPE_COMMENT)));

        // Processing Instruction
        ProcessingInstruction pi = document.createProcessingInstruction("target", "data");
        assertTrue(DOMNodePointer.testNode(pi, new NodeTypeTest(NodePointer.NODE_TYPE_PROCESSING_INSTRUCTION)));

        // Attr node
        Attr attr = document.createAttribute("att");
        assertFalse(DOMNodePointer.testNode(attr, new NodeTypeTest(NodePointer.NODE_TYPE_ELEMENT)));
    }

    @Test
    public void testTestNodeNames() {
        Element element = document.createElementNS("http://ns", "p:elem");
        NodeNameTest nameTest = new NodeNameTest(new QName("elem"));
        NodeNameTest qNameTest = new NodeNameTest(new QName("p", "elem"));
        NodeNameTest wildcardTest = new NodeNameTest(new QName(null, "*"));

        assertTrue(DOMNodePointer.testNode(element, nameTest));
        assertTrue(DOMNodePointer.testNode(element, qNameTest));
        assertTrue(DOMNodePointer.testNode(element, wildcardTest));

        // Attr testing
        Attr attr = document.createAttribute("att");
        NodeNameTest attrNameTest = new NodeNameTest(new QName("att"));
        assertTrue(DOMNodePointer.testNode(attr, attrNameTest));
    }

    @Test
    public void testGetValueAndSetValue() {
        Element element = document.createElement("elem");
        Text text = document.createTextNode("textValue");
        element.appendChild(text);

        DOMNodePointer elPtr = new DOMNodePointer(rootPointer, element);
        assertEquals("textValue", elPtr.getValue());

        DOMNodePointer textPtr = new DOMNodePointer(elPtr, text);
        assertEquals("textValue", textPtr.getValue());

        textPtr.setValue("newValue");
        assertEquals("newValue", text.getData());

        // Test setting value to null (should remove text/element content or handle gracefully)
        textPtr.setValue(null);
        assertEquals("", text.getData());
    }

    @Test
    public void testSetValueWithElement() {
        Element element = document.createElement("elem");
        DOMNodePointer elPtr = new DOMNodePointer(rootPointer, element);
        
        elPtr.setValue("New Text");
        assertEquals("New Text", elPtr.getValue());
    }

    @Test
    public void testAttributePointerCreation() {
        Element element = document.createElement("elem");
        element.setAttribute("attrName", "attrValue");
        DOMNodePointer elPtr = new DOMNodePointer(rootPointer, element);

        NodePointer attrPtr = elPtr.attributeIterator(new QName("attrName")).getNodePointer();
        assertNotNull(attrPtr);
        assertEquals("attrValue", attrPtr.getValue());
        assertEquals("attrName", attrPtr.getName().getName());
    }

    @Test
    public void testNamespacePointerCreation() {
        Element element = document.createElement("elem");
        DOMNodePointer elPtr = new DOMNodePointer(rootPointer, element);

        NodePointer nsPtr = elPtr.namespacePointer("xml");
        assertNotNull(nsPtr);
        assertEquals("xml", nsPtr.getName().getName());
    }

    @Test
    public void testHashCodeAndEquals() {
        Element element = document.createElement("elem");
        DOMNodePointer ptr1 = new DOMNodePointer(rootPointer, element);
        DOMNodePointer ptr2 = new DOMNodePointer(rootPointer, element);
        DOMNodePointer ptr3 = new DOMNodePointer(rootPointer, document.createElement("elem"));

        assertEquals(ptr1, ptr2);
        assertEquals(ptr1.hashCode(), ptr2.hashCode());
        assertNotEquals(ptr1, ptr3);
        assertNotEquals(ptr1, null);
        assertNotEquals(ptr1, "string");
    }

    @Test
    public void testCompareChildNodePointers() {
        Element parent = document.createElement("parent");
        Element child1 = document.createElement("child1");
        Element child2 = document.createElement("child2");
        parent.appendChild(child1);
        parent.appendChild(child2);

        DOMNodePointer ptr1 = new DOMNodePointer(rootPointer, child1);
        DOMNodePointer ptr2 = new DOMNodePointer(rootPointer, child2);

        assertTrue(ptr1.compareChildNodePointers(ptr1, ptr2) < 0);
        assertTrue(ptr2.compareChildNodePointers(ptr2, ptr1) > 0);
        assertEquals(0, ptr1.compareChildNodePointers(ptr1, ptr1));
    }

    @Test
    public void testChildIterator() {
        Element parent = document.createElement("parent");
        parent.appendChild(document.createTextNode("text"));
        parent.appendChild(document.createElement("child"));
        parent.appendChild(document.createComment("comment"));

        DOMNodePointer parentPtr = new DOMNodePointer(rootPointer, parent);
        
        // Iterate all nodes
        org.apache.commons.jxpath.ri.model.NodeIterator it = parentPtr.childIterator(null, false, null);
        assertTrue(it.setPosition(1));
        assertNotNull(it.getNodePointer());
    }

    @Test
    public void testCDATANode() {
        CDATASection cdata = document.createCDATASection("cdata-content");
        DOMNodePointer cdataPtr = new DOMNodePointer(rootPointer, cdata);
        assertEquals("cdata-content", cdataPtr.getValue());
        assertTrue(DOMNodePointer.testNode(cdata, new NodeTypeTest(NodePointer.NODE_TYPE_TEXT)));
    }
}