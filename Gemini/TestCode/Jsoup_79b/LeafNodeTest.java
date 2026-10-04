package org.jsoup.nodes;

import org.junit.Test;

import static org.junit.Assert.*;

public class LeafNodeTest {

    // A concrete subclass of LeafNode to test its methods
    private static class DummyLeafNode extends LeafNode {
        private Object value;

        public DummyLeafNode(Object value) {
            this.value = value;
        }

        @Override
        public String nodeName() {
            return "dummy";
        }

        @Override
        public Object coreValue() {
            return value;
        }

        @Override
        protected void coreValue(Object value) {
            this.value = value;
        }
    }

    @Test
    public void testCoreValueOperations() {
        DummyLeafNode node = new DummyLeafNode("initialValue");
        assertEquals("initialValue", node.coreValue());

        node.coreValue("newValue");
        assertEquals("newValue", node.coreValue());
    }

    @Test
    public void testAbsUrl() {
        DummyLeafNode node = new DummyLeafNode("");
        // LeafNode's absUrl method delegates or checks attributes
        // Depending on baseUri handling:
        node.setBaseUri("http://example.com/");
        // If it doesn't have an attribute key, absUrl usually returns empty or resolves.
        // Let's test setting an attribute that represents a URL or href.
        node.attr("href", "path/to/page");
        assertEquals("http://example.com/path/to/page", node.absUrl("href"));
    }

    @Test
    public void testChildNodeSize() {
        DummyLeafNode node = new DummyLeafNode("test");
        assertEquals(0, node.childNodeSize());
    }

    @Test
    public void testDoSetBaseUri() {
        DummyLeafNode node = new DummyLeafNode("test");
        node.doSetBaseUri("http://foo.bar");
        assertEquals("http://foo.bar", node.baseUri());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEnsureChildNode() {
        DummyLeafNode node = new DummyLeafNode("test");
        node.ensureChildNode();
    }

    @Test
    public void testDoClone() {
        DummyLeafNode node = new DummyLeafNode("test");
        node.setBaseUri("http://base.com");
        node.attr("key", "val");

        LeafNode clone = (LeafNode) node.doClone(null);
        assertNotNull(clone);
        assertEquals(node.coreValue(), clone.coreValue());
        assertEquals(node.baseUri(), clone.baseUri());
        assertEquals(node.attr("key"), clone.attr("key"));
    }
}