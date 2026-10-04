package org.apache.commons.jxpath.ri.model.dom;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.xml.DOMParser;
import org.apache.commons.jxpath.xml.DocumentContainer;
import org.apache.commons.jxpath.JXPathContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.Locale;

import static org.junit.Assert.*;

public class DOMNodePointerTest {

    private Document document;
    private Element rootElement;
    private Element childElement;
    private Text textNode;
    private DOMNodePointer rootPointer;
    private DOMNodePointer childPointer;
    private DOMNodePointer textPointer;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.newDocument();

        rootElement = document.createElementNS("http://example.com/ns", "prefix:root");
        childElement = document.createElement("child");
        textNode = document.createTextNode("textValue");

        rootElement.appendChild(childElement);
        childElement.appendChild(textNode);
        document.appendChild(rootElement);

        rootPointer = new DOMNodePointer(rootElement, Locale.ENGLISH);
        childPointer = new DOMNodePointer(rootPointer, childElement);
        textPointer = new DOMNodePointer(childPointer, textNode);
    }

    @After
    public void tearDown() throws Exception {
        document = null;
        rootElement = null;
        childElement = null;
        textNode = null;
        rootPointer = null;
        childPointer = null;
        textPointer = null;
    }

    @Test
    public void testGetBaseValue() {
        assertEquals(rootElement, rootPointer.getBaseValue());
        assertEquals(childElement, childPointer.getBaseValue());
        assertEquals(textNode, textPointer.getBaseValue());
    }

    @Test
    public void testIsCollection() {
        assertFalse(rootPointer.isCollection());
    }

    @Test
    public void testGetLength() {
        assertEquals(1, rootPointer.getLength());
    }

    @Test
    public void testIsLeaf() {
        assertFalse(rootPointer.isLeaf());
        DOMNodePointer leafPointer = new DOMNodePointer(textPointer, textNode);
        // Depending on implementation, empty text or leaf check:
        // Let's test with a node having no children
        Element emptyChild = document.createElement("empty");
        DOMNodePointer emptyPointer = new DOMNodePointer(childPointer, emptyChild);
        assertTrue(emptyPointer.isLeaf());
    }

    @Test
    public void testGetNamespaceURI() {
        assertEquals("http://example.com/ns", rootPointer.getNamespaceURI("prefix"));
        assertEquals("http://example.com/ns", rootPointer.getNamespaceURI());
        assertNull(childPointer.getNamespaceURI("nonexistent"));
        
        // Test local namespace resolution if any
        DOMNodePointer nullNsPointer = new DOMNodePointer(document.createElement("nonNs"), Locale.ENGLISH);
        assertNull(nullNsPointer.getNamespaceURI());
    }

    @Test
    public void testHashCode() {
        DOMNodePointer sameRootPointer = new DOMNodePointer(rootElement, Locale.ENGLISH);
        assertEquals(rootPointer.hashCode(), sameRootPointer.hashCode());
    }

    @Test
    public void testEquals() {
        DOMNodePointer sameRootPointer = new DOMNodePointer(rootElement, Locale.ENGLISH);
        assertTrue(rootPointer.equals(sameRootPointer));
        assertTrue(rootPointer.equals(rootPointer));
        assertFalse(rootPointer.equals(childPointer));
        assertFalse(rootPointer.equals(null));
        assertFalse(rootPointer.equals("some string"));
    }

    @Test
    public void testTestNode() {
        NodePointer.testNode(rootPointer, rootElement, new QName("prefix:root"));
        NodePointer.testNode(rootPointer, rootElement, new QName(null, "*"));
        NodePointer.testNode(rootPointer, textNode, new QName(null, "text()"));
        NodePointer.testNode(rootPointer, document.createComment("comment"), new QName(null, "comment()"));
        NodePointer.testNode(rootPointer, document.createProcessingInstruction("target", "data"), new QName(null, "processing-instruction('target')"));
    }

    @Test
    public void testCompareChildNodePointers() {
        NodePointer p1 = new DOMNodePointer(rootPointer, childElement);
        NodePointer p2 = new DOMNodePointer(rootPointer, textNode);
        // Comparing order in DOM
        assertTrue(rootPointer.compareChildNodePointers(p1, p2) < 0);
        assertTrue(rootPointer.compareChildNodePointers(p2, p1) > 0);
        assertEquals(0, rootPointer.compareChildNodePointers(p1, p1));
    }

    @Test
    public void testNamespaceDeclarations() {
        Element elWithNs = document.createElementNS("http://ns.com", "ns:elem");
        elWithNs.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:foo", "http://foo.com");
        DOMNodePointer p = new DOMNodePointer(elWithNs, Locale.ENGLISH);
        assertEquals("http://foo.com", p.getNamespaceURI("foo"));
    }

    @Test
    public void testPointerMethods() {
        assertNotNull(rootPointer.getImmediateParentPointer());
        assertNull(new DOMNodePointer(document, Locale.ENGLISH).getImmediateParentPointer());
        
        assertNotNull(rootPointer.getAxiomType());
        assertFalse(rootPointer.isActual());
    }

    @Test
    public void testAsPath() {
        assertEquals("/", new DOMNodePointer(document, Locale.ENGLISH).asPath());
        assertEquals("/prefix:root[1]", rootPointer.asPath());
        assertEquals("/prefix:root[1]/child[1]", childPointer.asPath());
    }

    @Test
    public void testNodeName() {
        assertNotNull(rootPointer.getName());
        assertEquals("prefix:root", rootPointer.getName().toString());
        
        DOMNodePointer commentPtr = new DOMNodePointer(rootPointer, document.createComment("test"));
        assertNull(commentPtr.getName());

        DOMNodePointer piPtr = new DOMNodePointer(rootPointer, document.createProcessingInstruction("target", "data"));
        assertEquals("target", piPtr.getName().getName());
    }

    @Test
    public void testCreateChild() {
        JXPathContext context = JXPathContext.newContext(document);
        QName qname = new QName("newChild");
        NodePointer created = rootPointer.createChild(context, qname, 1);
        assertNotNull(created);
    }

    @Test
    public void testCreateAttribute() {
        JXPathContext context = JXPathContext.newContext(document);
        QName qname = new QName("attrName");
        NodePointer attrPtr = rootPointer.createAttribute(context, qname);
        assertNotNull(attrPtr);
    }

    @Test
    public void testRemove() {
        DOMNodePointer tempPointer = new DOMNodePointer(childPointer, textNode);
        tempPointer.remove();
        assertNull(textNode.getParentNode());
    }

    @Test
    public void testChildIterator() {
        org.apache.commons.jxpath.ri.model.NodeIterator iterator = rootPointer.childIterator(null, false, null);
        assertNotNull(iterator);
    }

    @Test
    public void testAttributeIterator() {
        rootElement.setAttribute("testAttr", "val");
        org.apache.commons.jxpath.ri.model.NodeIterator iterator = rootPointer.attributeIterator(new QName("testAttr"));
        assertNotNull(iterator);
        assertTrue(iterator.setPosition(1));
    }

    @Test
    public void testNamespaceIterator() {
        org.apache.commons.jxpath.ri.model.NodeIterator iterator = rootPointer.namespaceIterator();
        assertNotNull(iterator);
    }
}