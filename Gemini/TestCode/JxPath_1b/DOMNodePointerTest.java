package org.apache.commons.jxpath.ri.model.dom;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.ProcessingInstructionTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
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
import java.io.ByteArrayInputStream;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DOMNodePointerTest {

    private Document document;
    private Element root;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.newDocument();

        root = document.createElementNS("http://test.org/ns", "test:root");
        root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:test", "http://test.org/ns");
        root.setAttributeNS("http://www.w3.org/XML/1998/namespace", "xml:lang", "en-US");
        root.setAttribute("id", "rootId");
        document.appendChild(root);
    }

    @Test
    public void testConstructorsAndBasicProperties() {
        DOMNodePointer pointer = new DOMNodePointer(root, Locale.US);
        assertEquals(root, pointer.getNode());
        assertEquals(root, pointer.getBaseValue());
        assertEquals(root, pointer.getImmediateNode());
        assertTrue(pointer.isActual());
        assertFalse(pointer.isCollection());
        assertEquals(1, pointer.getLength());
        assertFalse(pointer.isLeaf());

        DOMNodePointer pointerWithId = new DOMNodePointer(root, Locale.US, "myId");
        assertEquals("id('myId')", pointerWithId.asPath());

        DOMNodePointer docPointer = new DOMNodePointer(document, Locale.US);
        assertEquals("", docPointer.asPath());
        assertFalse(docPointer.isLeaf());
    }

    @Test
    public void testGetNameAndNamespace() {
        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.US);
        QName name = rootPointer.getName();
        assertEquals("test", name.getPrefix());
        assertEquals("root", name.getName());
        assertEquals("http://test.org/ns", rootPointer.getNamespaceURI());

        Element child = document.createElement("child");
        root.appendChild(child);
        DOMNodePointer childPointer = new DOMNodePointer(rootPointer, child);
        QName childName = childPointer.getName();
        assertNull(childName.getPrefix());
        assertEquals("child", childName.getName());

        Text text = document.createTextNode("Sample Text");
        root.appendChild(text);
        DOMNodePointer textPointer = new DOMNodePointer(rootPointer, text);
        assertNull(textPointer.getName());
        assertTrue(textPointer.isLeaf());

        ProcessingInstruction pi = document.createProcessingInstruction("target", "data");
        root.appendChild(pi);
        DOMNodePointer piPointer = new DOMNodePointer(rootPointer, pi);
        assertEquals("target", piPointer.getName().getName());
        assertTrue(piPointer.isLeaf());

        Comment comment = document.createComment("comment");
        root.appendChild(comment);
        DOMNodePointer commentPointer = new DOMNodePointer(rootPointer, comment);
        assertNull(commentPointer.getName());
        assertTrue(commentPointer.isLeaf());
    }

    @Test
    public void testGetNamespaceURIWithPrefixes() {
        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.US);
        assertEquals("http://test.org/ns", rootPointer.getNamespaceURI("test"));
        assertEquals("http://www.w3.org/XML/1998/namespace", rootPointer.getNamespaceURI("xml"));
        assertNull(rootPointer.getNamespaceURI("nonexistent"));

        root.setAttribute("xmlns", "http://default.org");
        assertEquals("http://default.org", rootPointer.getDefaultNamespaceURI());
        assertEquals("http://default.org", rootPointer.getNamespaceURI(null));
        assertEquals("http://default.org", rootPointer.getNamespaceURI(""));
    }

    @Test
    public void testIsLanguage() {
        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.US);
        assertTrue(rootPointer.isLanguage("en"));
        assertTrue(rootPointer.isLanguage("en-US"));
        assertTrue(rootPointer.isLanguage("EN"));
        assertFalse(rootPointer.isLanguage("fr"));

        Element childNoLang = document.createElement("childNoLang");
        root.appendChild(childNoLang);
        DOMNodePointer childPointer = new DOMNodePointer(rootPointer, childNoLang);
        assertTrue(childPointer.isLanguage("en"));

        childNoLang.setAttributeNS("http://www.w3.org/XML/1998/namespace", "xml:lang", "fr-CA");
        assertTrue(childPointer.isLanguage("fr"));
        assertFalse(childPointer.isLanguage("en"));
    }

    @Test
    public void testNodeTestMatching() {
        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.US);

        assertTrue(DOMNodePointer.testNode(rootPointer, root, null));

        NodeNameTest nodeNameTest = new NodeNameTest(new QName("test", "root"));
        assertTrue(DOMNodePointer.testNode(rootPointer, root, nodeNameTest));

        NodeNameTest wrongPrefixTest = new NodeNameTest(new QName("wrong", "root"));
        assertFalse(DOMNodePointer.testNode(rootPointer, root, wrongPrefixTest));

        NodeNameTest wildcardTest = new NodeNameTest(new QName(null, "*"));
        assertTrue(DOMNodePointer.testNode(rootPointer, root, wildcardTest));

        NodeTypeTest nodeTypeNode = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        assertTrue(DOMNodePointer.testNode(rootPointer, root, nodeTypeNode));

        Text textNode = document.createTextNode("text");
        NodeTypeTest nodeTypeText = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        assertTrue(DOMNodePointer.testNode(rootPointer, textNode, nodeTypeText));
        assertFalse(DOMNodePointer.testNode(rootPointer, root, nodeTypeText));

        Comment comment = document.createComment("hello");
        NodeTypeTest nodeTypeComment = new NodeTypeTest(Compiler.NODE_TYPE_COMMENT);
        assertTrue(DOMNodePointer.testNode(rootPointer, comment, nodeTypeComment));
        assertFalse(DOMNodePointer.testNode(rootPointer, root, nodeTypeComment));

        ProcessingInstruction pi = document.createProcessingInstruction("target", "data");
        NodeTypeTest nodeTypePI = new NodeTypeTest(Compiler.NODE_TYPE_PI);
        assertTrue(DOMNodePointer.testNode(rootPointer, pi, nodeTypePI));
        assertFalse(DOMNodePointer.testNode(rootPointer, root, nodeTypePI));

        ProcessingInstructionTest piTest = new ProcessingInstructionTest("target");
        assertTrue(DOMNodePointer.testNode(rootPointer, pi, piTest));
        ProcessingInstructionTest piWrongTest = new ProcessingInstructionTest("other");
        assertFalse(DOMNodePointer.testNode(rootPointer, pi, piWrongTest));

        NodeTypeTest unsupportedType = new NodeTypeTest(999);
        assertFalse(DOMNodePointer.testNode(rootPointer, root, unsupportedType));
    }

    @Test
    public void testSetValueAndGetValue() {
        Element element = document.createElement("valueNode");
        root.appendChild(element);
        DOMNodePointer pointer = new DOMNodePointer(new DOMNodePointer(root, Locale.US), element);

        pointer.setValue("new value");
        assertEquals("new value", pointer.getValue());

        Text text = document.createTextNode("initial text");
        root.appendChild(text);
        DOMNodePointer textPointer = new DOMNodePointer(new DOMNodePointer(root, Locale.US), text);
        assertEquals("initial text", textPointer.getValue());

        textPointer.setValue("updated text");
        assertEquals("updated text", textPointer.getValue());

        textPointer.setValue("");
        assertEquals("", textPointer.getValue());

        Comment comment = document.createComment("initial comment");
        root.appendChild(comment);
        DOMNodePointer commentPointer = new DOMNodePointer(new DOMNodePointer(root, Locale.US), comment);
        assertEquals("initial comment", commentPointer.getValue());
        commentPointer.setValue("updated comment");
        assertEquals("updated comment", commentPointer.getValue());

        ProcessingInstruction pi = document.createProcessingInstruction("target", "initial pi");
        root.appendChild(pi);
        DOMNodePointer piPointer = new DOMNodePointer(new DOMNodePointer(root, Locale.US), pi);
        assertEquals("initial pi", piPointer.getValue());
        piPointer.setValue("updated pi");
        assertEquals("updated pi", piPointer.getValue());
    }

    @Test
    public void testSetValueWithNullOrEmpty() {
        Element element = document.createElement("child");
        element.appendChild(document.createTextNode("some text"));
        root.appendChild(element);

        DOMNodePointer pointer = new DOMNodePointer(new DOMNodePointer(root, Locale.US), element);
        pointer.setValue(null);
        assertEquals("", pointer.getValue());
        assertEquals(0, element.getChildNodes().getLength());

        pointer.setValue("");
        assertEquals("", pointer.getValue());
    }

    @Test
    public void testCreateChildAndCreateAttribute() {
        JXPathContext context = JXPathContext.newContext(document);
        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.US);

        NodePointer attrPointer = rootPointer.createAttribute(context, new QName("attr1"));
        assertNotNull(attrPointer);
        assertEquals("attr1", attrPointer.getName().getName());

        NodePointer childPointer = rootPointer.createChild(context, new QName("child"), 0);
        assertNotNull(childPointer);
        assertEquals("child", childPointer.getName().getName());

        NodePointer childWithValue = rootPointer.createChild(context, new QName("childWithValue"), 1, "Val1");
        assertNotNull(childWithValue);
        assertEquals("Val1", childWithValue.getValue());

        NodePointer textChild = rootPointer.createChild(context, new QName("text()"), 0, "TextContent");
        assertNotNull(textChild);
    }

    @Test
    public void testRemove() {
        Element child = document.createElement("toRemove");
        root.appendChild(child);

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.US);
        DOMNodePointer childPointer = new DOMNodePointer(rootPointer, child);

        childPointer.remove();
        assertEquals(0, root.getElementsByTagName("toRemove").getLength());

        DOMNodePointer docPointer = new DOMNodePointer(document, Locale.US);
        try {
            docPointer.remove();
            fail("Removing document should throw JXPathException");
        } catch (JXPathException e) {
            // Expected
        }
    }

    @Test
    public void testAsPath() {
        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.US);
        assertEquals("/test:root[1]", rootPointer.asPath());

        Element child1 = document.createElement("child");
        Element child2 = document.createElement("child");
        root.appendChild(child1);
        root.appendChild(child2);

        DOMNodePointer childPointer1 = new DOMNodePointer(rootPointer, child1);
        DOMNodePointer childPointer2 = new DOMNodePointer(rootPointer, child2);

        assertEquals("/test:root[1]/child[1]", childPointer1.asPath());
        assertEquals("/test:root[1]/child[2]", childPointer2.asPath());

        Text text1 = document.createTextNode("text1");
        Text text2 = document.createTextNode("text2");
        child1.appendChild(text1);
        child1.appendChild(text2);

        DOMNodePointer textPointer1 = new DOMNodePointer(childPointer1, text1);
        DOMNodePointer textPointer2 = new DOMNodePointer(childPointer1, text2);

        assertEquals("/test:root[1]/child[1]/text()[1]", textPointer1.asPath());
        assertEquals("/test:root[1]/child[1]/text()[2]", textPointer2.asPath());

        ProcessingInstruction pi = document.createProcessingInstruction("target", "data");
        child1.appendChild(pi);
        DOMNodePointer piPointer = new DOMNodePointer(childPointer1, pi);
        assertEquals("/test:root[1]/child[1]/processing-instruction('target')[1]", piPointer.asPath());

        Comment comment = document.createComment("comm");
        child1.appendChild(comment);
        DOMNodePointer commentPointer = new DOMNodePointer(childPointer1, comment);
        assertEquals("/test:root[1]/child[1]/node()[4]", commentPointer.asPath());
    }

    @Test
    public void testIterators() {
        Element child1 = document.createElement("item");
        Element child2 = document.createElement("item");
        root.appendChild(child1);
        root.appendChild(child2);

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.US);

        NodeIterator childIter = rootPointer.childIterator(new NodeNameTest(new QName("item")), false, null);
        assertNotNull(childIter);
        assertTrue(childIter.setPosition(1));
        assertEquals(child1, childIter.getNodePointer().getNode());
        assertTrue(childIter.setPosition(2));
        assertEquals(child2, childIter.getNodePointer().getNode());
        assertFalse(childIter.setPosition(3));

        NodeIterator attrIter = rootPointer.attributeIterator(new QName("id"));
        assertNotNull(attrIter);
        assertTrue(attrIter.setPosition(1));
        assertEquals("rootId", attrIter.getNodePointer().getValue());

        NodeIterator nsIter = rootPointer.namespaceIterator();
        assertNotNull(nsIter);
        assertTrue(nsIter.setPosition(1));
        assertNotNull(nsIter.getNodePointer());

        NodePointer testNsPointer = rootPointer.namespacePointer("test");
        assertNotNull(testNsPointer);
        assertEquals("http://test.org/ns", testNsPointer.getNamespaceURI());
    }

    @Test
    public void testEqualsAndHashCode() {
        DOMNodePointer p1 = new DOMNodePointer(root, Locale.US);
        DOMNodePointer p2 = new DOMNodePointer(root, Locale.US);
        DOMNodePointer p3 = new DOMNodePointer(document, Locale.US);

        assertTrue(p1.equals(p1));
        assertTrue(p1.equals(p2));
        assertFalse(p1.equals(p3));
        assertFalse(p1.equals(null));
        assertFalse(p1.equals("Not a pointer"));
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void testCompareChildNodePointers() {
        Element child1 = document.createElement("c1");
        Element child2 = document.createElement("c2");
        root.appendChild(child1);
        root.appendChild(child2);

        DOMNodePointer rootPointer = new DOMNodePointer(root, Locale.US);
        DOMNodePointer p1 = new DOMNodePointer(rootPointer, child1);
        DOMNodePointer p2 = new DOMNodePointer(rootPointer, child2);

        assertEquals(0, rootPointer.compareChildNodePointers(p1, p1));
        assertEquals(-1, rootPointer.compareChildNodePointers(p1, p2));
        assertEquals(1, rootPointer.compareChildNodePointers(p2, p1));

        Attr attr1 = document.createAttribute("a1");
        Attr attr2 = document.createAttribute("a2");
        root.setAttributeNode(attr1);
        root.setAttributeNode(attr2);

        DOMAttributePointer ap1 = new DOMAttributePointer(rootPointer, attr1);
        DOMAttributePointer ap2 = new DOMAttributePointer(rootPointer, attr2);

        assertEquals(-1, rootPointer.compareChildNodePointers(ap1, ap2));
        assertEquals(1, rootPointer.compareChildNodePointers(ap2, ap1));
        assertEquals(-1, rootPointer.compareChildNodePointers(ap1, p1));
        assertEquals(1, rootPointer.compareChildNodePointers(p1, ap1));
    }

    @Test
    public void testFindEnclosingAttribute() {
        Element parent = document.createElement("parent");
        parent.setAttribute("testAttr", "parentValue");
        Element child = document.createElement("child");
        parent.appendChild(child);

        assertEquals("parentValue", DOMNodePointer.findEnclosingAttribute(child, "testAttr"));
        assertNull(DOMNodePointer.findEnclosingAttribute(child, "nonExistentAttr"));
        assertNull(DOMNodePointer.findEnclosingAttribute(null, "testAttr"));
    }
}