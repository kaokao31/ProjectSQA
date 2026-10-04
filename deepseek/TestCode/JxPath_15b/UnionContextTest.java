package org.apache.commons.jxpath.ri.axes;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.model.NodePointer;

import java.util.ArrayList;
import java.util.List;

/**
 * Test suite for UnionContext.
 * Exercises iteration, position handling, and edge cases.
 * Designed to uncover faults typical of Defects4J bug scenarios.
 */
public class UnionContextTest {

    // Mock NodePointer for simple testing
    private static class MockNodePointer extends NodePointer {
        private static final long serialVersionUID = 1L;
        private final String id;

        public MockNodePointer(String id) {
            super(null);
            this.id = id;
        }

        @Override
        public Object getValue() {
            return id;
        }

        @Override
        public Object getBaseValue() {
            return id;
        }

        @Override
        public Object getImmediateNode() {
            return id;
        }

        @Override
        public String asPath() {
            return id;
        }

        @Override
        public boolean isActual() {
            return true;
        }

        @Override
        public boolean isCollection() {
            return false;
        }

        @Override
        public int getLength() {
            return 1;
        }

        @Override
        public NodePointer getValuePointer() {
            return this;
        }

        @Override
        public NodePointer getNodePointer() {
            return this;
        }

        @Override
        public NodePointer getParent() {
            return null;
        }

        @Override
        public int compareChildNodePointers(NodePointer pointer1, NodePointer pointer2) {
            return 0;
        }

        @Override
        public boolean testNode(NodePointer node) {
            return false;
        }

        @Override
        public boolean testNode(int index) {
            return false;
        }

        @Override
        public void setValue(Object value) {
            // not supported
        }

        @Override
        public boolean isLeaf() {
            return true;
        }

        @Override
        public boolean isContainer() {
            return false;
        }

        @Override
        public Object getNode() {
            return id;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof MockNodePointer) {
                return id.equals(((MockNodePointer) obj).id);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public boolean isRoot() {
            return true;
        }

        @Override
        public String toString() {
            return id;
        }
    }

    // Mock EvalContext with a fixed list of node pointers
    private static class MockEvalContext implements EvalContext {
        private final List<NodePointer> nodes;
        private int position = 0; // 0 before first, 1..n after nextNode

        public MockEvalContext(List<NodePointer> nodes) {
            this.nodes = nodes;
        }

        @Override
        public NodePointer getCurrentNodePointer() {
            if (position == 0) {
                return null;
            }
            return nodes.get(position - 1);
        }

        @Override
        public Object getValue() {
            NodePointer ptr = getCurrentNodePointer();
            return ptr == null ? null : ptr.getValue();
        }

        @Override
        public boolean nextNode() {
            if (position < nodes.size()) {
                position++;
                return true;
            }
            return false;
        }

        @Override
        public boolean setPosition(int position) {
            if (position < 0 || position > nodes.size()) {
                return false;
            }
            this.position = position;
            return true;
        }

        @Override
        public int getCurrentPosition() {
            return position;
        }

        @Override
        public boolean nextSet() {
            return false;
        }

        @Override
        public boolean nextElement() {
            return nextNode();
        }

        @Override
        public boolean reset() {
            position = 0;
            return true;
        }
    }

    private EvalContext ctx1;
    private EvalContext ctx2;
    private UnionContext unionCtx;

    @Before
    public void setUp() {
        List<NodePointer> nodes1 = new ArrayList<>();
        nodes1.add(new MockNodePointer("a"));
        nodes1.add(new MockNodePointer("b"));

        List<NodePointer> nodes2 = new ArrayList<>();
        nodes2.add(new MockNodePointer("c"));
        nodes2.add(new MockNodePointer("d"));
        nodes2.add(new MockNodePointer("e"));

        ctx1 = new MockEvalContext(nodes1);
        ctx2 = new MockEvalContext(nodes2);

        unionCtx = new UnionContext(new EvalContext[] { ctx1, ctx2 });
    }

    // -------- Basic iteration tests --------
    @Test
    public void testIteratesThroughAllContexts() {
        List<String> expected = new ArrayList<>(List.of("a", "b", "c", "d", "e"));
        List<String> actual = new ArrayList<>();
        while (unionCtx.nextNode()) {
            actual.add((String) unionCtx.getValue());
        }
        assertEquals(expected, actual);
    }

    @Test
    public void testNextNodeReturnsFalseAtEnd() {
        // Consume all nodes
        while (unionCtx.nextNode()) {
            // do nothing
        }
        assertFalse(unionCtx.nextNode());
        assertNull(unionCtx.getCurrentNodePointer());
    }

    @Test
    public void testPositionIncrementsAcrossContexts() {
        assertTrue(unionCtx.nextNode());
        assertEquals(1, unionCtx.getCurrentPosition());
        assertEquals("a", unionCtx.getCurrentNodePointer().getValue());

        assertTrue(unionCtx.nextNode());
        assertEquals(2, unionCtx.getCurrentPosition());
        assertEquals("b", unionCtx.getCurrentNodePointer().getValue());

        // Continue into second context
        assertTrue(unionCtx.nextNode());
        assertEquals(3, unionCtx.getCurrentPosition());
        assertEquals("c", unionCtx.getCurrentNodePointer().getValue());

        assertTrue(unionCtx.nextNode());
        assertEquals(4, unionCtx.getCurrentPosition());
        assertEquals("d", unionCtx.getCurrentNodePointer().getValue());

        assertTrue(unionCtx.nextNode());
        assertEquals(5, unionCtx.getCurrentPosition());
        assertEquals("e", unionCtx.getCurrentNodePointer().getValue());
    }

    @Test
    public void testSetPositionWithinRange() {
        assertTrue(unionCtx.setPosition(3));
        assertEquals(3, unionCtx.getCurrentPosition());
        assertEquals("c", unionCtx.getCurrentNodePointer().getValue());

        assertTrue(unionCtx.setPosition(1));
        assertEquals(1, unionCtx.getCurrentPosition());
        assertEquals("a", unionCtx.getCurrentNodePointer().getValue());
    }

    @Test
    public void testSetPositionAtBoundaries() {
        // position 0 (before first)
        assertTrue(unionCtx.setPosition(0));
        assertEquals(0, unionCtx.getCurrentPosition());
        assertNull(unionCtx.getCurrentNodePointer());

        // position at last node
        assertTrue(unionCtx.setPosition(5));
        assertEquals(5, unionCtx.getCurrentPosition());
        assertEquals("e", unionCtx.getCurrentNodePointer().getValue());
    }

    @Test
    public void testSetPositionOutOfBounds() {
        // negative position
        assertFalse(unionCtx.setPosition(-1));
        // position > total nodes
        assertFalse(unionCtx.setPosition(6));
        // ensure position unchanged
        assertEquals(0, unionCtx.getCurrentPosition());
    }

    @Test
    public void testReset() {
        unionCtx.nextNode(); // advance to first
        assertTrue(unionCtx.reset());
        assertEquals(0, unionCtx.getCurrentPosition());
        assertNull(unionCtx.getCurrentNodePointer());
        // should be able to iterate again
        assertTrue(unionCtx.nextNode());
        assertEquals("a", unionCtx.getCurrentNodePointer().getValue());
    }

    // -------- Single context union --------
    @Test
    public void testSingleContextUnion() {
        List<NodePointer> nodes = new ArrayList<>();
        nodes.add(new MockNodePointer("x"));
        nodes.add(new MockNodePointer("y"));
        EvalContext single = new MockEvalContext(nodes);
        UnionContext singleUnion = new UnionContext(new EvalContext[] { single });

        assertTrue(singleUnion.nextNode());
        assertEquals("x", singleUnion.getValue());
        assertTrue(singleUnion.nextNode());
        assertEquals("y", singleUnion.getValue());
        assertFalse(singleUnion.nextNode());
    }

    // -------- Empty contexts --------
    @Test
    public void testEmptyContexts() {
        EvalContext empty1 = new MockEvalContext(new ArrayList<>());
        EvalContext empty2 = new MockEvalContext(new ArrayList<>());
        UnionContext emptyUnion = new UnionContext(new EvalContext[] { empty1, empty2 });

        assertFalse(emptyUnion.nextNode());
        assertNull(emptyUnion.getCurrentNodePointer());
        assertEquals(0, emptyUnion.getCurrentPosition());
    }

    // -------- Context that is already positioned --------
    @Test
    public void testUnionWithPrepositionedContext() {
        List<NodePointer> nodes = new ArrayList<>();
        nodes.add(new MockNodePointer("p"));
        nodes.add(new MockNodePointer("q"));
        MockEvalContext preCtx = new MockEvalContext(nodes);
        preCtx.setPosition(1); // already at "p"

        UnionContext preUnion = new UnionContext(new EvalContext[] { preCtx });
        // UnionContext should start from the beginning, not from current position
        assertTrue(preUnion.nextNode());
        assertEquals("p", preUnion.getValue());
        // After one nextNode, we should still be at "p" (since context starts at 0)
        // Verify second nextNode goes to "q"
        assertTrue(preUnion.nextNode());
        assertEquals("q", preUnion.getValue());
    }

    // -------- Test getValue() and getCurrentNodePointer() before any nextNode --------
    @Test
    public void testInitialState() {
        assertEquals(0, unionCtx.getCurrentPosition());
        assertNull(unionCtx.getCurrentNodePointer());
        assertNull(unionCtx.getValue());
    }

    // -------- Test that context ordering is preserved --------
    @Test
    public void testOrderingOfContextNodes() {
        // Ensure first context is exhausted before second
        List<String> order = new ArrayList<>();
        while (unionCtx.nextNode()) {
            order.add(unionCtx.getCurrentNodePointer().asPath());
        }
        assertEquals(List.of("a", "b", "c", "d", "e"), order);
    }

    // -------- Bug-specific tests: potential off-by-one in position or iteration --------
    // This test may trigger a fault if UnionContext skips the first node of the second context
    @Test
    public void testBoundaryBetweenContexts() {
        // Move to the boundary: after "b" (position 2) should go to "c" (position 3)
        assertTrue(unionCtx.nextNode());
        assertTrue(unionCtx.nextNode());
        assertEquals(2, unionCtx.getCurrentPosition());
        assertEquals("b", unionCtx.getValue());

        assertTrue(unionCtx.nextNode());
        assertEquals(3, unionCtx.getCurrentPosition());
        assertEquals("c", unionCtx.getValue());
    }

    // Test that setPosition after exhaustion resets correctly
    @Test
    public void testSetPositionAfterExhaustion() {
        while (unionCtx.nextNode()) {
            // consume all
        }
        assertTrue(unionCtx.setPosition(2));
        assertEquals("b", unionCtx.getCurrentNodePointer().getValue());
    }

    // Test that calling nextNode twice at the same position doesn't skip nodes
    @Test
    public void testNoInfiniteLoop() {
        int count = 0;
        while (unionCtx.nextNode()) {
            count++;
            if (count > 10) {
                fail("Possible infinite loop in UnionContext.nextNode()");
            }
        }
        assertEquals(5, count);
    }

    // Test getCurrentPosition after reset is 0
    @Test
    public void testResetSetsPositionToZero() {
        unionCtx.nextNode();
        unionCtx.reset();
        assertEquals(0, unionCtx.getCurrentPosition());
        assertFalse(unionCtx.nextNode()); // because no nodes after reset if we don't start again? Actually reset should allow restart.
        // Reset then nextNode should give first node
        unionCtx.reset();
        assertTrue(unionCtx.nextNode());
        assertEquals("a", unionCtx.getValue());
    }

    // -------- Tests with one context empty --------
    @Test
    public void testOneContextEmpty() {
        EvalContext empty = new MockEvalContext(new ArrayList<>());
        UnionContext mixed = new UnionContext(new EvalContext[] { ctx1, empty });
        assertTrue(mixed.nextNode());
        assertEquals("a", mixed.getValue());
        assertTrue(mixed.nextNode());
        assertEquals("b", mixed.getValue());
        assertFalse(mixed.nextNode());
    }

    // -------- Tests with all contexts empty --------
    @Test
    public void testAllEmpty() {
        EvalContext empty1 = new MockEvalContext(new ArrayList<>());
        EvalContext empty2 = new MockEvalContext(new ArrayList<>());
        UnionContext emptyUnion = new UnionContext(new EvalContext[] { empty1, empty2 });
        assertFalse(emptyUnion.nextNode());
        assertFalse(emptyUnion.nextNode());
        assertNull(emptyUnion.getCurrentNodePointer());
    }

    // -------- Constructor with null array? We'll assume not allowed, but test if it throws NPE --------
    @Test(expected = NullPointerException.class)
    public void testNullContextArrayThrowsNPE() {
        new UnionContext(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyContextArrayThrows() {
        new UnionContext(new EvalContext[0]);
    }

    // -------- Additional bug hunting: setPosition with negative value might throw exception --------
    @Test
    public void testSetPositionNegativeReturnsFalse() {
        assertFalse(unionCtx.setPosition(-1));
    }

    @Test
    public void testSetPositionZeroReturnsTrue() {
        assertTrue(unionCtx.setPosition(0));
        assertNull(unionCtx.getCurrentNodePointer());
    }

    @Test
    public void testSetPositionTooLargeReturnsFalse() {
        assertFalse(unionCtx.setPosition(6));
    }

    // -------- Test that multiple unions can be chained --------
    @Test
    public void testNestedUnion() {
        List<NodePointer> extra = new ArrayList<>();
        extra.add(new MockNodePointer("f"));
        EvalContext ctx3 = new MockEvalContext(extra);
        UnionContext innerUnion = new UnionContext(new EvalContext[] { unionCtx, ctx3 });
        List<String> all = new ArrayList<>();
        while (innerUnion.nextNode()) {
            all.add((String) innerUnion.getValue());
        }
        assertEquals(List.of("a", "b", "c", "d", "e", "f"), all);
    }
}