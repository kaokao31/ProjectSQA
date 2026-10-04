package org.apache.commons.jxpath.ri.model.dom;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.w3c.dom.*;

/**
 * JUnit 4 test suite for DOMNodePointer.
 * Designed to achieve high line/branch coverage and detect potential faults.
 */
public class DOMNodePointerTest {

    private Document document;
    private Element rootElement;
    private DOMNodePointer rootPointer;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.newDocument();
        rootElement = document.createElement("root");
        document.appendChild(rootElement);
        rootPointer = new DOMNodePointer(null, rootElement);
    }

    // ==================== Constructor Tests ====================

    @Test
    public void testConstructorWithNullParent() {
        DOMNodePointer pointer = new DOMNodePointer(null, rootElement);
        assertNotNull(pointer);
        assertNull(pointer.getParent());
    }

    @Test
    public void testConstructorWithParent() {
        DOMNodePointer parent = new DOMNodePointer(null, document);
        DOMNodePointer child = new DOMNodePointer(parent, rootElement);
        assertSame(parent, child.getParent());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullNode() {
        new DOMNodePointer(null, null);
    }

    // ==================== asPath() Tests ====================

    @Test
    public void testAsPathRootNode() {
        DOMNodePointer pointer = new DOMNodePointer(null, document);
        assertEquals("/", pointer.asPath());
    }

    @Test
    public void testAsPathElement() {
        // Root element path
        assertEquals("/root", rootPointer.asPath());
    }

    @Test
    public void testAsPathNestedElement() {
        Element child = document.createElement("child");
        rootElement.appendChild(child);
        DOMNodePointer childPointer = new DOMNodePointer(rootPointer, child);
        assertEquals("/root/child", childPointer.asPath());
    }

    @Test
    public void testAsPathWithTextNode() {
        Text text = document.createTextNode("text");
        rootElement.appendChild(text);
        DOMNodePointer textPointer = new DOMNodePointer(rootPointer, text);
        // Text nodes are typically not represented in path; depends on implementation
        // We just ensure no exception
        assertNotNull(textPointer.asPath());
    }

    @Test
    public void testAsPathWithAttribute() {
        rootElement.setAttribute("attr", "value");
        Attr attr = rootElement.getAttributeNode("attr");
        DOMNodePointer attrPointer = new DOMNodePointer(rootPointer, attr);
        String path = attrPointer.asPath();
        assertTrue(path.contains("@attr"));
    }

    @Test
    public void testAsPathWithNamespace() {
        // Namespace handling may be complex; test basic
        Element nsElement = document.createElementNS("http://example.com", "ns:prefixed");
        rootElement.appendChild(nsElement);
        DOMNodePointer nsPointer = new DOMNodePointer(rootPointer, nsElement);
        assertNotNull(nsPointer.asPath());
    }

    // ==================== getImmediateNode() Tests ====================

    @Test
    public void testGetImmediateNode() {
        assertSame(rootElement, rootPointer.getImmediateNode());
    }

    @Test
    public void testGetImmediateNodeForDocument() {
        DOMNodePointer docPointer = new DOMNodePointer(null, document);
        assertSame(document, docPointer.getImmediateNode());
    }

    // ==================== isLeaf() Tests ====================

    @Test
    public void testIsLeafElementWithChildren() {
        Element child = document.createElement("child");
        rootElement.appendChild(child);
        assertFalse(rootPointer.isLeaf());
    }

    @Test
    public void testIsLeafElementWithoutChildren() {
        assertTrue(rootPointer.isLeaf()); // no children yet
    }

    @Test
    public void testIsLeafTextNode() {
        Text text = document.createTextNode("text");
        rootElement.appendChild(text);
        DOMNodePointer textPointer = new DOMNodePointer(rootPointer, text);
        assertTrue(textPointer.isLeaf());
    }

    @Test
    public void testIsLeafAttribute() {
        rootElement.setAttribute("attr", "val");
        Attr attr = rootElement.getAttributeNode("attr");
        DOMNodePointer attrPointer = new DOMNodePointer(rootPointer, attr);
        assertTrue(attrPointer.isLeaf());
    }

    @Test
    public void testIsLeafDocument() {
        DOMNodePointer docPointer = new DOMNodePointer(null, document);
        assertFalse(docPointer.isLeaf()); // document has root element
    }

    // ==================== getImmediateValue() Tests ====================

    @Test
    public void testGetImmediateValueElement() {
        assertNull(rootPointer.getImmediateValue());
    }

    @Test
    public void testGetImmediateValueText() {
        Text text = document.createTextNode("hello");
        rootElement.appendChild(text);
        DOMNodePointer textPointer = new DOMNodePointer(rootPointer, text);
        assertEquals("hello", textPointer.getImmediateValue());
    }

    @Test
    public void testGetImmediateValueAttribute() {
        rootElement.setAttribute("attr", "value");
        Attr attr = rootElement.getAttributeNode("attr");
        DOMNodePointer attrPointer = new DOMNodePointer(rootPointer, attr);
        assertEquals("value", attrPointer.getImmediateValue());
    }

    // ==================== getLength() Tests ====================

    @Test
    public void testGetLength() {
        assertEquals(1, rootPointer.getLength());
    }

    // ==================== isCollection() Tests ====================

    @Test
    public void testIsCollection() {
        assertFalse(rootPointer.isCollection());
    }

    // ==================== getIndex() Tests ====================

    @Test
    public void testGetIndex() {
        assertEquals(DOMNodePointer.WHOLE_COLLECTION, rootPointer.getIndex());
    }

    // ==================== compareChildNodePointers() Tests ====================

    @Test
    public void testCompareChildNodePointersSame() {
        DOMNodePointer p1 = new DOMNodePointer(rootPointer, rootElement);
        DOMNodePointer p2 = new DOMNodePointer(rootPointer, rootElement);
        assertEquals(0, rootPointer.compareChildNodePointers(p1, p2));
    }

    @Test
    public void testCompareChildNodePointersDifferentOrder() {
        Element child1 = document.createElement("a");
        Element child2 = document.createElement("b");
        rootElement.appendChild(child1);
        rootElement.appendChild(child2);
        DOMNodePointer p1 = new DOMNodePointer(rootPointer, child1);
        DOMNodePointer p2 = new DOMNodePointer(rootPointer, child2);
        assertTrue(rootPointer.compareChildNodePointers(p1, p2) < 0);
        assertTrue(rootPointer.compareChildNodePointers(p2, p1) > 0);
    }

    @Test
    public void testCompareChildNodePointersWithTextAndElement() {
        Text text = document.createTextNode("text");
        Element elem = document.createElement("elem");
        rootElement.appendChild(text);
        rootElement.appendChild(elem);
        DOMNodePointer textPtr = new DOMNodePointer(rootPointer, text);
        DOMNodePointer elemPtr = new DOMNodePointer(rootPointer, elem);
        // Order depends on DOM implementation; just ensure no exception
        int result = rootPointer.compareChildNodePointers(textPtr, elemPtr);
        assertTrue(result != 0);
    }

    // ==================== attributeIterator() Tests ====================

    @Test
    public void testAttributeIteratorNoAttributes() {
        NodeIterator iter = rootPointer.attributeIterator(null);
        assertNotNull(iter);
        assertNull(iter.next());
    }

    @Test
    public void testAttributeIteratorWithAttributes() {
        rootElement.setAttribute("id", "1");
        rootElement.setAttribute("class", "test");
        NodeIterator iter = rootPointer.attributeIterator(null);
        assertNotNull(iter);
        Object first = iter.next();
        assertNotNull(first);
        assertTrue(first instanceof DOMNodePointer);
        Object second = iter.next();
        assertNotNull(second);
        assertNull(iter.next());
    }

    // ==================== namespaceIterator() Tests ====================

    @Test
    public void testNamespaceIterator() {
        NodeIterator iter = rootPointer.namespaceIterator();
        assertNotNull(iter);
        // May return empty or default namespaces; just ensure no exception
        while (iter.next() != null) {
            // consume
        }
    }

    // ==================== childIterator() Tests ====================

    @Test
    public void testChildIteratorNoChildren() {
        NodeIterator iter = rootPointer.childIterator(null, false, null);
        assertNotNull(iter);
        assertNull(iter.next());
    }

    @Test
    public void testChildIteratorWithChildren() {
        Element child = document.createElement("child");
        rootElement.appendChild(child);
        NodeIterator iter = rootPointer.childIterator(null, false, null);
        assertNotNull(iter);
        Object first = iter.next();
        assertNotNull(first);
        assertTrue(first instanceof DOMNodePointer);
        assertNull(iter.next());
    }

    @Test
    public void testChildIteratorReverseOrder() {
        Element child1 = document.createElement("a");
        Element child2 = document.createElement("b");
        rootElement.appendChild(child1);
        rootElement.appendChild(child2);
        NodeIterator iter = rootPointer.childIterator(null, true, null);
        assertNotNull(iter);
        Object first = iter.next();
        assertNotNull(first);
        Object second = iter.next();
        assertNotNull(second);
        assertNull(iter.next());
    }

    // ==================== getParent() Tests ====================

    @Test
    public void testGetParentRoot() {
        assertNull(rootPointer.getParent());
    }

    @Test
    public void testGetParentChild() {
        Element child = document.createElement("child");
        rootElement.appendChild(child);
        DOMNodePointer childPointer = new DOMNodePointer(rootPointer, child);
        assertSame(rootPointer, childPointer.getParent());
    }

    // ==================== getImmediateNodePointer() Tests ====================

    @Test
    public void testGetImmediateNodePointer() {
        assertSame(rootPointer, rootPointer.getImmediateNodePointer());
    }

    // ==================== getValuePointer() Tests ====================

    @Test
    public void testGetValuePointerElement() {
        assertSame(rootPointer, rootPointer.getValuePointer());
    }

    @Test
    public void testGetValuePointerText() {
        Text text = document.createTextNode("val");
        rootElement.appendChild(text);
        DOMNodePointer textPointer = new DOMNodePointer(rootPointer, text);
        assertSame(textPointer, textPointer.getValuePointer());
    }

    // ==================== isActual() Tests ====================

    @Test
    public void testIsActual() {
        assertTrue(rootPointer.isActual());
    }

    // ==================== isContainer() Tests ====================

    @Test
    public void testIsContainer() {
        assertFalse(rootPointer.isContainer());
    }

    // ==================== isRoot() Tests ====================

    @Test
    public void testIsRoot() {
        DOMNodePointer docPointer = new DOMNodePointer(null, document);
        assertTrue(docPointer.isRoot());
        assertFalse(rootPointer.isRoot());
    }

    // ==================== hashCode() and equals() Tests ====================

    @Test
    public void testEqualsSameObject() {
        assertTrue(rootPointer.equals(rootPointer));
    }

    @Test
    public void testEqualsDifferentObjectSameNode() {
        DOMNodePointer other = new DOMNodePointer(null, rootElement);
        assertTrue(rootPointer.equals(other));
    }

    @Test
    public void testEqualsDifferentNode() {
        Element otherElem = document.createElement("other");
        DOMNodePointer other = new DOMNodePointer(null, otherElem);
        assertFalse(rootPointer.equals(other));
    }

    @Test
    public void testEqualsNull() {
        assertFalse(rootPointer.equals(null));
    }

    @Test
    public void testHashCodeConsistency() {
        DOMNodePointer other = new DOMNodePointer(null, rootElement);
        assertEquals(rootPointer.hashCode(), other.hashCode());
    }

    // ==================== Edge Cases and Fault Detection ====================

    @Test
    public void testAsPathWithEmptyStringAttribute() {
        rootElement.setAttribute("empty", "");
        Attr attr = rootElement.getAttributeNode("empty");
        DOMNodePointer attrPointer = new DOMNodePointer(rootPointer, attr);
        String path = attrPointer.asPath();
        assertTrue(path.contains("@empty"));
    }

    @Test
    public void testAsPathWithSpecialCharactersInName() {
        Element special = document.createElement("my-element");
        rootElement.appendChild(special);
        DOMNodePointer specialPointer = new DOMNodePointer(rootPointer, special);
        String path = specialPointer.asPath();
        assertEquals("/root/my-element", path);
    }

    @Test
    public void testChildIteratorWithNullTest() {
        // Some implementations may handle null test gracefully
        NodeIterator iter = rootPointer.childIterator(null, false, null);
        assertNotNull(iter);
    }

    @Test
    public void testAttributeIteratorWithNullPrefix() {
        NodeIterator iter = rootPointer.attributeIterator(null);
        assertNotNull(iter);
    }

    @Test
    public void testNamespaceIteratorWithNoNamespaces() {
        NodeIterator iter = rootPointer.namespaceIterator();
        assertNotNull(iter);
        // Should return at least the default namespace? Depends.
    }

    @Test
    public void testCompareChildNodePointersWithNull() {
        // Edge case: comparing with null pointers
        try {
            rootPointer.compareChildNodePointers(null, null);
            fail("Expected NullPointerException or similar");
        } catch (NullPointerException e) {
            // expected
        } catch (Exception e) {
            // other exceptions may be acceptable depending on implementation
        }
    }

    @Test
    public void testGetImmediateValueOnDocument() {
        DOMNodePointer docPointer = new DOMNodePointer(null, document);
        // Document node may return null or some value; just ensure no exception
        Object val = docPointer.getImmediateValue();
        // Accept any result
    }

    @Test
    public void testAsPathOnDocumentFragment() {
        DocumentFragment fragment = document.createDocumentFragment();
        DOMNodePointer fragPointer = new DOMNodePointer(null, fragment);
        String path = fragPointer.asPath();
        assertNotNull(path);
    }

    @Test
    public void testIsLeafOnDocumentFragmentWithChildren() {
        DocumentFragment fragment = document.createDocumentFragment();
        Element child = document.createElement("c");
        fragment.appendChild(child);
        DOMNodePointer fragPointer = new DOMNodePointer(null, fragment);
        assertFalse(fragPointer.isLeaf());
    }

    @Test
    public void testIsLeafOnDocumentFragmentEmpty() {
        DocumentFragment fragment = document.createDocumentFragment();
        DOMNodePointer fragPointer = new DOMNodePointer(null, fragment);
        assertTrue(fragPointer.isLeaf());
    }

    @Test
    public void testGetParentOnDocumentFragment() {
        DocumentFragment fragment = document.createDocumentFragment();
        DOMNodePointer fragPointer = new DOMNodePointer(null, fragment);
        assertNull(fragPointer.getParent());
    }

    @Test
    public void testGetIndexOnChildPointer() {
        Element child = document.createElement("child");
        rootElement.appendChild(child);
        DOMNodePointer childPointer = new DOMNodePointer(rootPointer, child);
        assertEquals(DOMNodePointer.WHOLE_COLLECTION, childPointer.getIndex());
    }

    // ==================== Additional Coverage for Internal Methods ====================

    @Test
    public void testSetValueOnElement() {
        // setValue may be unsupported; expect exception or no-op
        try {
            rootPointer.setValue("newValue");
            // If no exception, check that value was set (maybe on text child)
        } catch (Exception e) {
            // acceptable
        }
    }

    @Test
    public void testSetValueOnText() {
        Text text = document.createTextNode("old");
        rootElement.appendChild(text);
        DOMNodePointer textPointer = new DOMNodePointer(rootPointer, text);
        textPointer.setValue("new");
        assertEquals("new", text.getNodeValue());
    }

    @Test
    public void testSetValueOnAttribute() {
        rootElement.setAttribute("attr", "old");
        Attr attr = rootElement.getAttributeNode("attr");
        DOMNodePointer attrPointer = new DOMNodePointer(rootPointer, attr);
        attrPointer.setValue("new");
        assertEquals("new", attr.getValue());
    }

    @Test
    public void testRemoveChild() {
        Element child = document.createElement("child");
        rootElement.appendChild(child);
        DOMNodePointer childPointer = new DOMNodePointer(rootPointer, child);
        childPointer.remove();
        assertFalse(rootElement.hasChildNodes());
    }

    @Test
    public void testRemoveAttribute() {
        rootElement.setAttribute("attr", "val");
        Attr attr = rootElement.getAttributeNode("attr");
        DOMNodePointer attrPointer = new DOMNodePointer(rootPointer, attr);
        attrPointer.remove();
        assertFalse(rootElement.hasAttribute("attr"));
    }

    @Test
    public void testRemoveTextNode() {
        Text text = document.createTextNode("text");
        rootElement.appendChild(text);
        DOMNodePointer textPointer = new DOMNodePointer(rootPointer, text);
        textPointer.remove();
        assertEquals(0, rootElement.getChildNodes().getLength());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveDocumentNode() {
        DOMNodePointer docPointer = new DOMNodePointer(null, document);
        docPointer.remove();
    }

    // ==================== Test for Defects4J specific bug patterns ====================

    @Test
    public void testAsPathWithMultipleSameNameSiblings() {
        Element child1 = document.createElement("item");
        Element child2 = document.createElement("item");
        rootElement.appendChild(child1);
        rootElement.appendChild(child2);
        DOMNodePointer p1 = new DOMNodePointer(rootPointer, child1);
        DOMNodePointer p2 = new DOMNodePointer(rootPointer, child2);
        String path1 = p1.asPath();
        String path2 = p2.asPath();
        // Paths should be distinct (e.g., /root/item[1] and /root/item[2])
        assertNotEquals(path1, path2);
        assertTrue(path1.contains("[1]") || path1.contains("[2]"));
    }

    @Test
    public void testAsPathWithIndexingForSingleChild() {
        Element child = document.createElement("item");
        rootElement.appendChild(child);
        DOMNodePointer childPointer = new DOMNodePointer(rootPointer, child);
        String path = childPointer.asPath();
        // Should not have index for single child
        assertFalse(path.contains("["));
    }

    @Test
    public void testCompareChildNodePointersWithDifferentTypes() {
        // Compare element and text node
        Text text = document.createTextNode("t");
        Element elem = document.createElement("e");
        rootElement.appendChild(text);
        rootElement.appendChild(elem);
        DOMNodePointer textPtr = new DOMNodePointer(rootPointer, text);
        DOMNodePointer elemPtr = new DOMNodePointer(rootPointer, elem);
        int result = rootPointer.compareChildNodePointers(textPtr, elemPtr);
        // Should be non-zero; order depends on DOM
        assertTrue(result != 0);
    }

    @Test
    public void testChildIteratorWithNameTest() {
        Element child1 = document.createElement("a");
        Element child2 = document.createElement("b");
        rootElement.appendChild(child1);
        rootElement.appendChild(child2);
        // Use a name test for "a"
        NodePointer nameTest = new NodeNameTest(null, "a");
        NodeIterator iter = rootPointer.childIterator(nameTest, false, null);
        assertNotNull(iter);
        Object first = iter.next();
        assertNotNull(first);
        assertNull(iter.next()); // only one match
    }

    @Test
    public void testAttributeIteratorWithNameTest() {
        rootElement.setAttribute("id", "1");
        rootElement.setAttribute("class", "test");
        NodePointer nameTest = new NodeNameTest(null, "id");
        NodeIterator iter = rootPointer.attributeIterator(nameTest);
        assertNotNull(iter);
        Object first = iter.next();
        assertNotNull(first);
        assertNull(iter.next());
    }
}