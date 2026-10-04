package org.apache.commons.jxpath.ri.model.dom;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.ProcessingInstructionTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.Comment;
import org.w3c.dom.ProcessingInstruction;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Locale;

import static org.junit.Assert.*;

public class DOMNodePointerTest {

    private Document document;
    private DOMNodePointer nodePointer;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        document = db.newDocument();
        
        Element root = document.createElementNS("http://example.com/ns", "prefix:root");
        root.setAttribute("xmlns:prefix", "http://example.com/ns");
        root.setAttribute("id", "rootId");
        document.appendChild(root);

        Element child = document.createElement("child");
        Text text = document.createTextNode("textValue");
        child.appendChild(text);
        root.appendChild(child);

        Comment comment = document.createComment("commentValue");
        root.appendChild(comment);

        ProcessingInstruction pi = document.createProcessingInstruction("target", "data");
        root.appendChild(pi);

        nodePointer = new DOMNodePointer(root, Locale.getDefault());
    }

    @After
    public void tearDown() {
        document = null;
        nodePointer = null;
    }

    @Test
    public void testTestNode() {
        // Test NodeTypeTest.NODE
        NodeTest nodeTypeTest = new NodeTypeTest(NodeTest.NODE);
        assertTrue(DOMNodePointer.testNode(nodePointer, document.getDocumentElement(), nodeTypeTest));

        // Test NodeTypeTest.TEXT
        NodeTest textTest = new NodeTypeTest(NodeTest.TEXT);
        Node textNode = document.getDocumentElement().getFirstChild().getFirstChild();
        DOMNodePointer textPointer = new DOMNodePointer(nodePointer, textNode);
        assertTrue(DOMNodePointer.testNode(textPointer, textNode, textTest));
        assertFalse(DOMNodePointer.testNode(nodePointer, document.getDocumentElement(), textTest));

        // Test NodeTypeTest.COMMENT
        NodeTest commentTest = new NodeTypeTest(NodeTest.COMMENT);
        Node commentNode = document.getDocumentElement().getElementsByTagName("child").item(0).getNextSibling();
        DOMNodePointer commentPointer = new DOMNodePointer(nodePointer, commentNode);
        assertTrue(DOMNodePointer.testNode(commentPointer, commentNode, commentTest));

        // Test NodeTypeTest.PI
        NodeTest piTest = new ProcessingInstructionTest("target");
        Node piNode = document.getDocumentElement().getLastChild();
        DOMNodePointer piPointer = new DOMNodePointer(nodePointer, piNode);
        assertTrue(DOMNodePointer.testNode(piPointer, piNode, piTest));

        // Test NodeNameTest with matching namespace and local name
        NodeNameTest nameTest = new NodeNameTest(new QName("http://example.com/ns", "root"), "prefix:root");
        assertTrue(DOMNodePointer.testNode(nodePointer, document.getDocumentElement(), nameTest));

        // Test NodeNameTest with wildcard local name
        NodeNameTest wildcardLocalTest = new NodeNameTest(new QName(null, "*"));
        assertTrue(DOMNodePointer.testNode(nodePointer, document.getDocumentElement(), wildcardLocalTest));

        // Test NodeNameTest with wildcard prefix/namespace
        NodeNameTest wildcardNsTest = new NodeNameTest(new QName("http://example.com/ns", "*"));
        assertTrue(DOMNodePointer.testNode(nodePointer, document.getDocumentElement(), wildcardNsTest));

        // Test NodeNameTest mismatch
        NodeNameTest mismatchTest = new NodeNameTest(new QName("http://example.com/ns", "wrongName"));
        assertFalse(DOMNodePointer.testNode(nodePointer, document.getDocumentElement(), mismatchTest));
    }

    @Test
    public void testGetNamespaceURI() {
        String ns = DOMNodePointer.getNamespaceURI(document.getDocumentElement());
        assertEquals("http://example.com/ns", ns);

        Element noNsElement = document.createElement("noNs");
        assertEquals("", DOMNodePointer.getNamespaceURI(noNsElement));

        assertNull(DOMNodePointer.getNamespaceURI(null));
    }

    @Test
    public void testLocalName() {
        assertEquals("root", DOMNodePointer.getLocalName(document.getDocumentElement()));
        
        Element elWithoutColon = document.createElement("elementWithoutColon");
        assertEquals("elementWithoutColon", DOMNodePointer.getLocalName(elWithoutColon));
        
        assertNull(DOMNodePointer.getLocalName(null));
    }

    @Test
    public void testPrefix() {
        assertEquals("prefix", DOMNodePointer.getPrefix(document.getDocumentElement()));
        
        Element elWithoutColon = document.createElement("elementWithoutColon");
        assertNull(DOMNodePointer.getPrefix(elWithoutColon));
        
        assertNull(DOMNodePointer.getPrefix(null));
    }

    @Test
    public void testNodePointerOperations() {
        assertEquals(Integer.MIN_VALUE, nodePointer.getLength());
        assertFalse(nodePointer.isCollection());
        assertFalse(nodePointer.isLeaf());
        assertNotNull(nodePointer.getBaseValue());
        assertNotNull(nodePointer.getImmediateValuePointer());
        
        nodePointer.setValue("newValue");
        assertEquals("newValue", nodePointer.getValue());
    }

    @Test
    public void testCompareChildNodePointers() {
        Node child1 = document.getDocumentElement().getFirstChild();
        Node child2 = document.getDocumentElement().getLastChild();
        
        DOMNodePointer p1 = new DOMNodePointer(nodePointer, child1);
        DOMNodePointer p2 = new DOMNodePointer(nodePointer, child2);
        
        assertTrue(nodePointer.compareChildNodePointers(p1, p2) < 0);
        assertTrue(nodePointer.compareChildNodePointers(p2, p1) > 0);
        assertEquals(0, nodePointer.compareChildNodePointers(p1, p1));
    }

    @Test
    public void testAttributePointer() {
        NodePointer attrPointer = nodePointer.attributePointer(new QName("id"));
        assertNotNull(attrPointer);
        assertEquals("rootId", attrPointer.getValue());
    }

    @Test
    public void testNamespaceOperations() {
        assertNull(nodePointer.getNamespaceURI());
        assertEquals("http://example.com/ns", nodePointer.getNamespaceURI("prefix"));
        
        nodePointer.registerNamespace("testPrefix", "http://test.com");
        assertEquals("http://test.com", nodePointer.getNamespaceURI("testPrefix"));
    }

    @Test
    public void testHashCodeAndEquals() {
        DOMNodePointer p1 = new DOMNodePointer(nodePointer, document.getDocumentElement());
        DOMNodePointer p2 = new DOMNodePointer(nodePointer, document.getDocumentElement());
        DOMNodePointer p3 = new DOMNodePointer(nodePointer, document.getDocumentElement().getFirstChild());

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1, p3);
        assertNotEquals(p1, null);
        assertNotEquals(p1, "some string");
    }

    @Test
    public void testAsPath() {
        assertEquals("/.", nodePointer.asPath());
        
        Node child = document.getDocumentElement().getFirstChild();
        DOMNodePointer childPointer = new DOMNodePointer(nodePointer, child);
        assertTrue(childPointer.asPath().contains("child"));
    }
}