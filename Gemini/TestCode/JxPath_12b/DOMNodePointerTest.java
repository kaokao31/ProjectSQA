package org.apache.commons.jxpath.ri.model.dom;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Attr;
import org.w3c.dom.Text;
import org.w3c.dom.Comment;
import org.w3c.dom.ProcessingInstruction;

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
        
        Element root = document.createElementNS("http://example.com/ns", "prefix:root");
        root.setAttribute("xmlns:prefix", "http://example.com/ns");
        root.setAttribute("attrKey", "attrValue");
        
        Text textNode = document.createTextNode("childText");
        root.appendChild(textNode);
        
        Comment commentNode = document.createComment("childComment");
        root.appendChild(commentNode);
        
        ProcessingInstruction piNode = document.createProcessingInstruction("target", "data");
        root.appendChild(piNode);
        
        document.appendChild(root);

        documentPointer = new DOMNodePointer(document, Locale.getDefault());
    }

    @Test
    public void testCompareChildNodePointers() {
        Element root = (Element) document.getDocumentElement();
        Text text = (Text) root.getFirstChild();
        Comment comment = (Comment) text.getNextSibling();

        DOMNodePointer p1 = new DOMNodePointer(documentPointer, root, text);
        DOMNodePointer p2 = new DOMNodePointer(documentPointer, root, comment);

        assertTrue(p1.compareTo(p2) < 0);
        assertTrue(p2.compareTo(p1) > 0);
        assertEquals(0, p 1.compareTo(p1));
    }

    @Test
    public void testCompareWithNonDOMNodePointer() {
        Element root = (Element) document.getDocumentElement();
        DOMNodePointer p1 = new DOMNodePointer(documentPointer, root, root.getFirstChild());
        NodePointer p2 = new DOMNodePointer(documentPointer) {
            @Override public Object getBaseValue() { return null; }
            @Override public Object getImmediateNode() { return null; }
            @Override public NodePointer createPath(org.apache.commons.jxpath.JXPathContext context) { return null; }
            @Override public boolean isCollection() { return false; }
            @Override public int getLength() { return 0; }
            @Override public QName getName() { return null; }
            @Override public boolean isLeaf() { return false; }
        };

        // Should return non-zero based on default pointer ordering or class comparison
        assertNotEquals(0, p1.compareTo(p2));
    }

    @Test
    public void testTestNodeNamespaceAndName() {
        Element root = (Element) document.getDocumentElement();
        DOMNodePointer rootPointer = new DOMNodePointer(documentPointer, root);

        // Test NodeTypeTest
        NodeTest nodeTypeTest = new NodeTypeTest(1); // Node.ELEMENT_NODE or similar type
        assertTrue(DOMNodePointer.testNode(rootPointer, root, nodeTypeTest));

        // Test NodeNameTest with matching prefix/local name
        QName qName = new QName("http://example.com/ns", "root");
        NodeNameTest nameTest = new NodeNameTest(qName, "http://example.com/ns");
        assertTrue(DOMNodePointer.testNode(rootPointer, root, nameTest));
    }

    @Test
    public void testTestNodeAttribute() {
        Element root = (Element) document.getDocumentElement();
        Attr attr = root.getAttributeNode("attrKey");
        DOMNodePointer attrPointer = new DOMNodePointer(documentPointer, attr);

        QName qName = new QName(null, "attrKey");
        NodeNameTest nameTest = new NodeNameTest(qName);
        assertTrue(DOMNodePointer.testNode(attrPointer, attr, nameTest));
    }

    @Test
    public void testGetNamespaceURI() {
        Element root = (Element) document.getDocumentElement();
        DOMNodePointer rootPointer = new DOMNodePointer(documentPointer, root);
        
        assertEquals("http://example.com/ns", rootPointer.getNamespaceURI("prefix"));
        assertEquals("http://example.com/ns", rootPointer.getNamespaceURI("http://example.com/ns"));
        assertNull(rootPointer.getNamespaceURI("nonexistent"));
    }

    @Test
    public void testFindNamespaceURI() {
        Element root = (Element) document.getDocumentElement();
        assertEquals("http://example.com/ns", DOMNodePointer.getNamespaceURI(root, "prefix"));
        assertNull(DOMNodePointer.getNamespaceURI(root, "unknown"));
    }

    @Test
    public void testGetLanguage() {
        Element root = (Element) document.getDocumentElement();
        root.setAttribute("xml:lang", "en-US");
        DOMNodePointer rootPointer = new DOMNodePointer(documentPointer, root);
        assertEquals("en-US", rootPointer.getLanguage());
    }

    @Test
    public void testIsLeaf() {
        Element root = (Element) document.getDocumentElement();
        Text text = (Text) root.getFirstChild();
        DOMNodePointer textPointer = new DOMNodePointer(documentPointer, text);
        assertTrue(textPointer.isLeaf());

        DOMNodePointer rootPointer = new DOMNodePointer(documentPointer, root);
        assertFalse(rootPointer.isLeaf());
    }

    @Test
    public void testHashCodeAndEquals() {
        Element root = (Element) document.getDocumentElement();
        DOMNodePointer p1 = new DOMNodePointer(documentPointer, root);
        DOMNodePointer p2 = new DOMNodePointer(documentPointer, root);
        DOMNodePointer p3 = new DOMNodePointer(documentPointer, document);

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1, p3);
        assertFalse(p1.equals("someString"));
    }

    @Test
    public void testPointerAttributesAndMethods() {
        Element root = (Element) document.getDocumentElement();
        DOMNodePointer rootPointer = new DOMNodePointer(documentPointer, root);

        assertNotNull(rootPointer.getImmediateNode());
        assertFalse(rootPointer.isCollection());
        assertEquals(1, rootPointer.getLength());
        assertEquals("root", rootPointer.getName().getName());
    }
}