package org.apache.commons.jxpath.ri.model.dom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.ProcessingInstructionTest;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

public class DOMNodePointerTest {

    private Document document;
    private Element root;
    private Element child;
    private Text text;
    private Comment comment;
    private ProcessingInstruction pi;
    private Attr attr;

    private DOMNodePointer documentPointer;
    private DOMNodePointer rootPointer;
    private DOMNodePointer childPointer;
    private DOMNodePointer textPointer;
    private DOMNodePointer commentPointer;
    private DOMNodePointer piPointer;
    private DOMNodePointer attrPointer;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        document = factory.newDocumentBuilder().newDocument();

        root = document.createElement("root");
        document.appendChild(root);

        child = document.createElement("child");
        root.appendChild(child);

        text = document.createTextNode("some text");
        child.appendChild(text);

        comment = document.createComment("a comment");
        root.appendChild(comment);

        pi = document.createProcessingInstruction("target", "data");
        root.appendChild(pi);

        attr = document.createAttribute("name");
        attr.setValue("attr-value");
        root.setAttributeNode(attr);

        documentPointer = new DOMNodePointer(document, Locale.ENGLISH);
        rootPointer = new DOMNodePointer(root, Locale.ENGLISH);
        childPointer = new DOMNodePointer(child, Locale.ENGLISH);
        textPointer = new DOMNodePointer(text, Locale.ENGLISH);
        commentPointer = new DOMNodePointer(comment, Locale.ENGLISH);
        piPointer = new DOMNodePointer(pi, Locale.ENGLISH);
        attrPointer = new DOMNodePointer(attr, Locale.ENGLISH);
    }

    private DOMNodePointer pointerFor(Node node) {
        return new DOMNodePointer(node, Locale.ENGLISH);
    }

    @Test
    public void testGetNode() {
        assertSame(document, documentPointer.getNode());
        assertSame(root, rootPointer.getNode());
        assertSame(child, childPointer.getNode());
        assertSame(attr, attrPointer.getNode());
    }

    @Test
    public void testIsLeaf() {
        assertFalse(documentPointer.isLeaf());
        assertFalse(rootPointer.isLeaf());
        assertFalse(childPointer.isLeaf());
        assertTrue(textPointer.isLeaf());
        assertTrue(commentPointer.isLeaf());

        Element empty = document.createElement("empty");
        root.appendChild(empty);
        assertTrue(pointerFor(empty).isLeaf());
    }

    @Test
    public void testGetName() {
        QName rootName = rootPointer.getName();
        assertEquals("root", rootName.getName());

        Element nsElem = document.createElementNS("urn:test", "p:item");
        root.appendChild(nsElem);
        QName nsName = pointerFor(nsElem).getName();
        assertEquals("item", nsName.getName());
    }

    @Test
    public void testGetValue() {
        assertEquals("some text", textPointer.getValue());
        assertEquals("some text", childPointer.getValue());
        assertEquals("a comment", commentPointer.getValue());
        assertEquals("data", piPointer.getValue());
        assertEquals("attr-value", attrPointer.getValue());
        assertNull(documentPointer.getValue());
    }

    @Test
    public void testSetValueOnTextNode() {
        textPointer.setValue("updated text");
        assertEquals("updated text", textPointer.getValue());
        assertEquals("updated text", text.getNodeValue());
    }

    @Test
    public void testSetValueOnElement() {
        childPointer.setValue("replacement value");
        assertEquals("replacement value", childPointer.getValue());
        assertEquals("replacement value", child.getFirstChild().getNodeValue());
    }

    @Test
    public void testSetValueOnAttribute() {
        attrPointer.setValue("new-attr-value");
        assertEquals("new-attr-value", attrPointer.getValue());
        assertEquals("new-attr-value", attr.getValue());
    }

    @Test
    public void testGetNamespaceURI() {
        Element nsElem = document.createElementNS("urn:test", "p:item");
        root.appendChild(nsElem);
        DOMNodePointer nsPointer = pointerFor(nsElem);

        assertEquals("urn:test", nsPointer.getNamespaceURI());
        assertEquals("urn:test", nsPointer.getNamespaceURI("p"));
        assertNull(nsPointer.getNamespaceURI("unknown"));
        assertNotNull(rootPointer.getNamespaceURI("xml"));
    }

    @Test
    public void testGetNamespaceURITraversesAncestors() {
        Element nsElem = document.createElementNS("urn:test", "p:item");
        root.appendChild(nsElem);
        Element plain = document.createElement("plain");
        nsElem.appendChild(plain);

        DOMNodePointer plainPointer = pointerFor(plain);
        assertEquals("urn:test", plainPointer.getNamespaceURI("p"));
    }

    @Test
    public void testGetLanguage() {
        root.setAttribute("xml:lang", "en");
        assertEquals("en", childPointer.getLanguage());
    }

    @Test
    public void testNodeNullTest() {
        assertTrue(rootPointer.testNode(null));
    }

    @Test
    public void testNodeNameTest() {
        assertTrue(childPointer.testNode(new NodeNameTest(new QName("child"))));
        assertFalse(childPointer.testNode(new NodeNameTest(new QName("other"))));
        assertFalse(textPointer.testNode(new NodeNameTest(new QName("child"))));
    }

    @Test
    public void testNodeTypeText() {
        assertTrue(textPointer.testNode(new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        assertFalse(rootPointer.testNode(new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
        assertFalse(commentPointer.testNode(new NodeTypeTest(Compiler.NODE_TYPE_TEXT)));
    }

    @Test
    public void testNodeTypeComment() {
        assertTrue(commentPointer.testNode(new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));
        assertFalse(textPointer.testNode(new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));
        assertFalse(rootPointer.testNode(new NodeTypeTest(Compiler.NODE_TYPE_COMMENT)));
    }

    @Test
    public void testNodeTypePI() {
        assertTrue(piPointer.testNode(new NodeTypeTest(Compiler.NODE_TYPE_PI)));
        assertFalse(textPointer.testNode(new NodeTypeTest(Compiler.NODE_TYPE_PI)));
        assertFalse(commentPointer.testNode(new NodeTypeTest(Compiler.NODE_TYPE_PI)));
    }

    @Test
    public void testNodeTypeNodeMatchesAllNodeKinds() {
        NodeTypeTest nodeTest = new NodeTypeTest(Compiler.NODE_TYPE_NODE);

        assertTrue("node() must match element nodes", rootPointer.testNode(nodeTest));
        assertTrue("node() must match text nodes", textPointer.testNode(nodeTest));
        assertTrue("node() must match comment nodes", commentPointer.testNode(nodeTest));
        assertTrue("node() must match processing instruction nodes", piPointer.testNode(nodeTest));
        assertTrue("node() must match attribute nodes", attrPointer.testNode(nodeTest));
    }

    @Test
    public void testProcessingInstructionTest() {
        assertTrue(piPointer.testNode(new ProcessingInstructionTest("target")));
        assertFalse(piPointer.testNode(new ProcessingInstructionTest("other")));
        assertFalse(commentPointer.testNode(new ProcessingInstructionTest("target")));
    }

    @Test
    public void testGetPointerByIDFromDocumentNode() {
        Element item = document.createElement("item");
        item.setAttribute("id", "id1");
        item.setIdAttribute("id", true);
        root.appendChild(item);

        JXPathContext context = JXPathContext.newContext(document);
        Pointer pointer = documentPointer.getPointerByID(context, "id1");

        assertNotNull("ID lookup should work from a Document node", pointer);
        assertSame(item, ((DOMNodePointer) pointer).getNode());
    }

    @Test
    public void testGetPointerByIDFromElementNode() {
        Element item = document.createElement("item");
        item.setAttribute("id", "id2");
        item.setIdAttribute("id", true);
        root.appendChild(item);

        JXPathContext context = JXPathContext.newContext(document);
        Pointer pointer = rootPointer.getPointerByID(context, "id2");

        assertNotNull(pointer);
        assertSame(item, ((DOMNodePointer) pointer).getNode());
    }
}