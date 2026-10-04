package org.jfree.chart.block;

import org.junit.Before;
import org.junit.Test;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import static org.junit.Assert.*;

/**
 * Test suite for BorderArrangement, targeting maximum coverage and fault detection.
 * Based on Defects4J Chart-13 bug context.
 */
public class BorderArrangementTest {

    private BorderArrangement arrangement;
    private Graphics2D g2;
    private BlockContainer container;

    @Before
    public void setUp() {
        arrangement = new BorderArrangement();
        // Create a real Graphics2D from a BufferedImage to avoid null issues
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        g2 = image.createGraphics();
        container = new BlockContainer(arrangement);
    }

    // ========== Basic arrange tests ==========

    @Test
    public void testArrangeWithEmptyContainer() {
        RectangleConstraint constraint = new RectangleConstraint(100, 100);
        Size2D size = arrangement.arrange(container, g2, constraint);
        assertEquals(0.0, size.width, 0.0001);
        assertEquals(0.0, size.height, 0.0001);
    }

    @Test
    public void testArrangeWithOnlyCenterBlock() {
        Block center = new EmptyBlock(50, 30);
        container.add(center, RectangleEdge.CENTER);
        RectangleConstraint constraint = new RectangleConstraint(200, 200);
        Size2D size = arrangement.arrange(container, g2, constraint);
        assertEquals(50.0, size.width, 0.0001);
        assertEquals(30.0, size.height, 0.0001);
    }

    @Test
    public void testArrangeWithAllEdges() {
        Block top = new EmptyBlock(100, 10);
        Block bottom = new EmptyBlock(100, 10);
        Block left = new EmptyBlock(20, 50);
        Block right = new EmptyBlock(20, 50);
        Block center = new EmptyBlock(40, 30);
        container.add(top, RectangleEdge.TOP);
        container.add(bottom, RectangleEdge.BOTTOM);
        container.add(left, RectangleEdge.LEFT);
        container.add(right, RectangleEdge.RIGHT);
        container.add(center, RectangleEdge.CENTER);

        RectangleConstraint constraint = new RectangleConstraint(200, 200);
        Size2D size = arrangement.arrange(container, g2, constraint);
        // Expected: width = left(20) + center(40) + right(20) = 80
        // height = top(10) + center(30) + bottom(10) = 50
        assertEquals(80.0, size.width, 0.0001);
        assertEquals(50.0, size.height, 0.0001);
    }

    @Test
    public void testArrangeWithOnlyTopAndBottom() {
        Block top = new EmptyBlock(100, 15);
        Block bottom = new EmptyBlock(100, 20);
        container.add(top, RectangleEdge.TOP);
        container.add(bottom, RectangleEdge.BOTTOM);
        RectangleConstraint constraint = new RectangleConstraint(200, 200);
        Size2D size = arrangement.arrange(container, g2, constraint);
        // width = max(top, bottom) = 100, height = 15+20 = 35
        assertEquals(100.0, size.width, 0.0001);
        assertEquals(35.0, size.height, 0.0001);
    }

    @Test
    public void testArrangeWithOnlyLeftAndRight() {
        Block left = new EmptyBlock(30, 100);
        Block right = new EmptyBlock(40, 100);
        container.add(left, RectangleEdge.LEFT);
        container.add(right, RectangleEdge.RIGHT);
        RectangleConstraint constraint = new RectangleConstraint(200, 200);
        Size2D size = arrangement.arrange(container, g2, constraint);
        // width = 30+40 = 70, height = max(left,right) = 100
        assertEquals(70.0, size.width, 0.0001);
        assertEquals(100.0, size.height, 0.0001);
    }

    // ========== Edge cases and null/empty constraints ==========

    @Test(expected = IllegalArgumentException.class)
    public void testArrangeWithNullContainer() {
        arrangement.arrange(null, g2, new RectangleConstraint(100, 100));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testArrangeWithNullGraphics() {
        arrangement.arrange(container, null, new RectangleConstraint(100, 100));
    }

    @Test
    public void testArrangeWithNullConstraint() {
        // Bug context: Chart-13 may fail with null constraint
        Block center = new EmptyBlock(50, 30);
        container.add(center, RectangleEdge.CENTER);
        try {
            Size2D size = arrangement.arrange(container, g2, null);
            // If no exception, check that size is reasonable (maybe default)
            assertNotNull(size);
        } catch (NullPointerException e) {
            // Expected if bug exists; test should fail if bug is fixed
            fail("Null constraint should be handled gracefully");
        }
    }

    @Test
    public void testArrangeWithZeroConstraint() {
        RectangleConstraint constraint = new RectangleConstraint(0, 0);
        Block center = new EmptyBlock(50, 30);
        container.add(center, RectangleEdge.CENTER);
        Size2D size = arrangement.arrange(container, g2, constraint);
        // Should still return the block's size (constraint is minimum)
        assertEquals(50.0, size.width, 0.0001);
        assertEquals(30.0, size.height, 0.0001);
    }

    @Test
    public void testArrangeWithNegativeConstraint() {
        RectangleConstraint constraint = new RectangleConstraint(-10, -20);
        Block center = new EmptyBlock(50, 30);
        container.add(center, RectangleEdge.CENTER);
        Size2D size = arrangement.arrange(container, g2, constraint);
        // Negative constraint should be treated as zero or ignored
        assertEquals(50.0, size.width, 0.0001);
        assertEquals(30.0, size.height, 0.0001);
    }

    // ========== Tests with no blocks added ==========

    @Test
    public void testArrangeWithNoBlocks() {
        RectangleConstraint constraint = new RectangleConstraint(100, 100);
        Size2D size = arrangement.arrange(container, g2, constraint);
        assertEquals(0.0, size.width, 0.0001);
        assertEquals(0.0, size.height, 0.0001);
    }

    // ========== Tests for arrange method with BlockContainer and Graphics2D only ==========

    @Test
    public void testArrangeNoConstraint() {
        Block center = new EmptyBlock(50, 30);
        container.add(center, RectangleEdge.CENTER);
        // This arrange method does not take a constraint; it uses the container's size?
        // Actually BorderArrangement has arrange(BlockContainer, Graphics2D) inherited from AbstractArrangement
        // It should arrange without constraint (using natural size)
        Size2D size = arrangement.arrange(container, g2);
        assertEquals(50.0, size.width, 0.0001);
        assertEquals(30.0, size.height, 0.0001);
    }

    // ========== Tests for clear method ==========

    @Test
    public void testClear() {
        Block center = new EmptyBlock(50, 30);
        container.add(center, RectangleEdge.CENTER);
        arrangement.clear(container);
        // After clear, container should have no blocks
        assertEquals(0, container.getBlocks().size());
    }

    // ========== Tests for add method with invalid edge ==========

    @Test(expected = IllegalArgumentException.class)
    public void testAddWithNullEdge() {
        arrangement.add(container, new EmptyBlock(10, 10), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddWithNullBlock() {
        arrangement.add(container, null, RectangleEdge.CENTER);
    }

    // ========== Tests for arrange with constrained width/height ==========

    @Test
    public void testArrangeWithWidthConstraint() {
        Block top = new EmptyBlock(200, 10);
        Block center = new EmptyBlock(50, 30);
        container.add(top, RectangleEdge.TOP);
        container.add(center, RectangleEdge.CENTER);
        // Constrain width to 100
        RectangleConstraint constraint = new RectangleConstraint(100, RectangleConstraint.NONE);
        Size2D size = arrangement.arrange(container, g2, constraint);
        // Top block should be trimmed to 100 width, center remains 50
        assertEquals(100.0, size.width, 0.0001);
        assertEquals(40.0, size.height, 0.0001);
    }

    @Test
    public void testArrangeWithHeightConstraint() {
        Block left = new EmptyBlock(20, 200);
        Block center = new EmptyBlock(50, 30);
        container.add(left, RectangleEdge.LEFT);
        container.add(center, RectangleEdge.CENTER);
        RectangleConstraint constraint = new RectangleConstraint(RectangleConstraint.NONE, 100);
        Size2D size = arrangement.arrange(container, g2, constraint);
        // Left block trimmed to 100 height, center remains 30
        assertEquals(70.0, size.width, 0.0001);
        assertEquals(100.0, size.height, 0.0001);
    }

    // ========== Tests for multiple blocks on same edge (should replace) ==========

    @Test
    public void testAddMultipleBlocksSameEdge() {
        Block top1 = new EmptyBlock(100, 10);
        Block top2 = new EmptyBlock(200, 20);
        container.add(top1, RectangleEdge.TOP);
        container.add(top2, RectangleEdge.TOP); // should replace top1
        RectangleConstraint constraint = new RectangleConstraint(300, 300);
        Size2D size = arrangement.arrange(container, g2, constraint);
        // Only top2 should be present
        assertEquals(200.0, size.width, 0.0001);
        assertEquals(20.0, size.height, 0.0001);
    }

    // ========== Tests for arrange with no center block ==========

    @Test
    public void testArrangeWithoutCenter() {
        Block top = new EmptyBlock(100, 10);
        Block bottom = new EmptyBlock(100, 20);
        container.add(top, RectangleEdge.TOP);
        container.add(bottom, RectangleEdge.BOTTOM);
        RectangleConstraint constraint = new RectangleConstraint(200, 200);
        Size2D size = arrangement.arrange(container, g2, constraint);
        assertEquals(100.0, size.width, 0.0001);
        assertEquals(30.0, size.height, 0.0001);
    }

    // ========== Tests for arrange with only one side block ==========

    @Test
    public void testArrangeWithOnlyLeft() {
        Block left = new EmptyBlock(30, 100);
        container.add(left, RectangleEdge.LEFT);
        RectangleConstraint constraint = new RectangleConstraint(200, 200);
        Size2D size = arrangement.arrange(container, g2, constraint);
        assertEquals(30.0, size.width, 0.0001);
        assertEquals(100.0, size.height, 0.0001);
    }

    @Test
    public void testArrangeWithOnlyRight() {
        Block right = new EmptyBlock(40, 80);
        container.add(right, RectangleEdge.RIGHT);
        RectangleConstraint constraint = new RectangleConstraint(200, 200);
        Size2D size = arrangement.arrange(container, g2, constraint);
        assertEquals(40.0, size.width, 0.0001);
        assertEquals(80.0, size.height, 0.0001);
    }

    // ========== Tests for arrange with large blocks exceeding constraint ==========

    @Test
    public void testArrangeWithBlockLargerThanConstraint() {
        Block center = new EmptyBlock(300, 300);
        container.add(center, RectangleEdge.CENTER);
        RectangleConstraint constraint = new RectangleConstraint(100, 100);
        Size2D size = arrangement.arrange(container, g2, constraint);
        // Should be constrained to 100x100
        assertEquals(100.0, size.width, 0.0001);
        assertEquals(100.0, size.height, 0.0001);
    }

    // ========== Tests for arrange with mixed edges and constraint ==========

    @Test
    public void testArrangeWithAllEdgesAndConstraint() {
        Block top = new EmptyBlock(50, 10);
        Block bottom = new EmptyBlock(50, 10);
        Block left = new EmptyBlock(10, 30);
        Block right = new EmptyBlock(10, 30);
        Block center = new EmptyBlock(20, 20);
        container.add(top, RectangleEdge.TOP);
        container.add(bottom, RectangleEdge.BOTTOM);
        container.add(left, RectangleEdge.LEFT);
        container.add(right, RectangleEdge.RIGHT);
        container.add(center, RectangleEdge.CENTER);

        // Constrain to 100x100
        RectangleConstraint constraint = new RectangleConstraint(100, 100);
        Size2D size = arrangement.arrange(container, g2, constraint);
        // Expected: width = left(10) + center(20) + right(10) = 40
        // height = top(10) + center(20) + bottom(10) = 40
        assertEquals(40.0, size.width, 0.0001);
        assertEquals(40.0, size.height, 0.0001);
    }

    // ========== Tests for arrange with no constraint (natural size) ==========

    @Test
    public void testArrangeNaturalSize() {
        Block top = new EmptyBlock(80, 10);
        Block bottom = new EmptyBlock(60, 15);
        Block left = new EmptyBlock(20, 50);
        Block right = new EmptyBlock(30, 50);
        Block center = new EmptyBlock(40, 25);
        container.add(top, RectangleEdge.TOP);
        container.add(bottom, RectangleEdge.BOTTOM);
        container.add(left, RectangleEdge.LEFT);
        container.add(right, RectangleEdge.RIGHT);
        container.add(center, RectangleEdge.CENTER);

        Size2D size = arrangement.arrange(container, g2);
        // width = left(20) + center(40) + right(30) = 90
        // height = top(10) + center(25) + bottom(15) = 50
        assertEquals(90.0, size.width, 0.0001);
        assertEquals(50.0, size.height, 0.0001);
    }

    // ========== Tests for arrange with null blocks in container ==========

    @Test
    public void testArrangeWithNullBlockInContainer() {
        // Adding null block should be handled gracefully
        container.add(null, RectangleEdge.CENTER);
        RectangleConstraint constraint = new RectangleConstraint(100, 100);
        Size2D size = arrangement.arrange(container, g2, constraint);
        // Should treat null block as empty
        assertEquals(0.0, size.width, 0.0001);
        assertEquals(0.0, size.height, 0.0001);
    }

    // ========== Tests for arrange with Graphics2D null (should throw) ==========

    @Test(expected = IllegalArgumentException.class)
    public void testArrangeWithNullGraphics2D() {
        arrangement.arrange(container, null, new RectangleConstraint(100, 100));
    }

    // ========== Tests for arrange with no constraint and null graphics ==========

    @Test(expected = IllegalArgumentException.class)
    public void testArrangeNoConstraintNullGraphics() {
        arrangement.arrange(container, null);
    }

    // ========== Tests for arrange with empty container and null constraint ==========

    @Test
    public void testArrangeEmptyContainerNullConstraint() {
        try {
            Size2D size = arrangement.arrange(container, g2, null);
            assertNotNull(size);
            assertEquals(0.0, size.width, 0.0001);
            assertEquals(0.0, size.height, 0.0001);
        } catch (NullPointerException e) {
            // Acceptable if bug exists; test should fail if fixed
            fail("Null constraint should not cause NPE");
        }
    }

    // ========== Tests for arrange with only top and left ==========

    @Test
    public void testArrangeTopLeftOnly() {
        Block top = new EmptyBlock(100, 10);
        Block left = new EmptyBlock(20, 50);
        container.add(top, RectangleEdge.TOP);
        container.add(left, RectangleEdge.LEFT);
        RectangleConstraint constraint = new RectangleConstraint(200, 200);
        Size2D size = arrangement.arrange(container, g2, constraint);
        // width = left(20) + (no center/right) = 20, but top width is 100, so overall width = max(20,100) = 100
        // height = top(10) + (no center/bottom) = 10, left height 50, so overall height = max(10,50) = 50
        assertEquals(100.0, size.width, 0.0001);
        assertEquals(50.0, size.height, 0.0001);
    }

    // ========== Tests for arrange with only bottom and right ==========

    @Test
    public void testArrangeBottomRightOnly() {
        Block bottom = new EmptyBlock(80, 15);
        Block right = new EmptyBlock(30, 60);
        container.add(bottom, RectangleEdge.BOTTOM);
        container.add(right, RectangleEdge.RIGHT);
        RectangleConstraint constraint = new RectangleConstraint(200, 200);
        Size2D size = arrangement.arrange(container, g2, constraint);
        // width = right(30) + (no left/center) = 30, bottom width 80, so overall width = max(30,80) = 80
        // height = bottom(15) + (no top/center) = 15, right height 60, so overall height = max(15,60) = 60
        assertEquals(80.0, size.width, 0.0001);
        assertEquals(60.0, size.height, 0.0001);
    }

    // ========== Tests for arrange with all edges but center missing ==========

    @Test
    public void testArrangeAllEdgesNoCenter() {
        Block top = new EmptyBlock(100, 10);
        Block bottom = new EmptyBlock(100, 10);
        Block left = new EmptyBlock(20, 50);
        Block right = new EmptyBlock(20, 50);
        container.add(top, RectangleEdge.TOP);
        container.add(bottom, RectangleEdge.BOTTOM);
        container.add(left, RectangleEdge.LEFT);
        container.add(right, RectangleEdge.RIGHT);
        RectangleConstraint constraint = new RectangleConstraint(200, 200);
        Size2D size = arrangement.arrange(container, g2, constraint);
        // width = left(20) + right(20) = 40, but top/bottom width 100, so overall width = max(40,100) = 100
        // height = top(10) + bottom(10) = 20, left/right height 50, so overall height = max(20,50) = 50
        assertEquals(100.0, size.width, 0.0001);
        assertEquals(50.0, size.height, 0.0001);
    }

    // ========== Tests for arrange with only center and no constraint ==========

    @Test
    public void testArrangeCenterOnlyNoConstraint() {
        Block center = new EmptyBlock(70, 40);
        container.add(center, RectangleEdge.CENTER);
        Size2D size = arrangement.arrange(container, g2);
        assertEquals(70.0, size.width, 0.0001);
        assertEquals(40.0, size.height, 0.0001);
    }

    // ========== Tests for arrange with multiple blocks on different edges ==========

    @Test
    public void testArrangeMultipleBlocksSameEdgeReplacement() {
        Block top1 = new EmptyBlock(50, 10);
        Block top2 = new EmptyBlock(100, 20);
        container.add(top1, RectangleEdge.TOP);
        container.add(top2, RectangleEdge.TOP); // replaces top1
        Block center = new EmptyBlock(30, 30);
        container.add(center, RectangleEdge.CENTER);
        RectangleConstraint constraint = new RectangleConstraint(200, 200);
        Size2D size = arrangement.arrange(container, g2, constraint);
        // Only top2 and center
        assertEquals(100.0, size.width, 0.0001);
        assertEquals(50.0, size.height, 0.0001);
    }

    // ========== Tests for arrange with zero-sized blocks ==========

    @Test
    public void testArrangeWithZeroSizedBlocks() {
        Block top = new EmptyBlock(0, 0);
        Block center = new EmptyBlock(0, 0);
        container.add(top, RectangleEdge.TOP);
        container.add(center, RectangleEdge.CENTER);
        RectangleConstraint constraint = new RectangleConstraint(100, 100);
        Size2D size = arrangement.arrange(container, g2, constraint);
        assertEquals(0.0, size.width, 0.0001);
        assertEquals(0.0, size.height, 0.0001);
    }

    // ========== Tests for arrange with negative-sized blocks ==========

    @Test
    public void testArrangeWithNegativeSizedBlocks() {
        Block center = new EmptyBlock(-10, -20);
        container.add(center, RectangleEdge.CENTER);
        RectangleConstraint constraint = new RectangleConstraint(100, 100);
        Size2D size = arrangement.arrange(container, g2, constraint);
        // Negative size should be treated as zero or absolute? Usually zero
        assertEquals(0.0, size.width, 0.0001);
        assertEquals(0.0, size.height, 0.0001);
    }

    // ========== Tests for arrange with very large constraint ==========

    @Test
    public void testArrangeWithLargeConstraint() {
        Block center = new EmptyBlock(50, 30);
        container.add(center, RectangleEdge.CENTER);
        RectangleConstraint constraint = new RectangleConstraint(1000, 1000);
        Size2D size = arrangement.arrange(container, g2, constraint);
        assertEquals(50.0, size.width, 0.0001);
        assertEquals(30.0, size.height, 0.0001);
    }

    // ========== Tests for arrange with width constraint only ==========

    @Test
    public void testArrangeWithWidthConstraintOnly() {
        Block top = new EmptyBlock(200, 10);
        container.add(top, RectangleEdge.TOP);
        RectangleConstraint constraint = new RectangleConstraint(100, RectangleConstraint.UNCONSTRAINED);
        Size2D size = arrangement.arrange(container, g2, constraint);
        // Width constrained to 100, height natural (10)
        assertEquals(100.0, size.width, 0.0001);
        assertEquals(10.0, size.height, 0.0001);
    }

    // ========== Tests for arrange with height constraint only ==========

    @Test
    public void testArrangeWithHeightConstraintOnly() {
        Block left = new EmptyBlock(20, 200);
        container.add(left, RectangleEdge.LEFT);
        RectangleConstraint constraint = new RectangleConstraint(RectangleConstraint.UNCONSTRAINED, 100);
        Size2D size = arrangement.arrange(container, g2, constraint);
        // Height constrained to 100, width natural (20)
        assertEquals(20.0, size.width, 0.0001);
        assertEquals(100.0, size.height, 0.0001);
    }

    // ========== Tests for arrange with no blocks and null constraint ==========

    @Test
    public void testArrangeNoBlocksNullConstraint() {
        try {
            Size2D size = arrangement.arrange(container, g2, null);
            assertNotNull(size);
            assertEquals(0.0, size.width, 0.0001);
            assertEquals(0.0, size.height, 0.0001);
        } catch (NullPointerException e) {
            fail("Null constraint should not cause NPE");
        }
    }

    // ========== Tests for arrange with only one block and null constraint ==========

    @Test
    public void testArrangeOneBlockNullConstraint() {
        Block center = new EmptyBlock(50, 30);
        container.add(center, RectangleEdge.CENTER);
        try {
            Size2D size = arrangement.arrange(container, g2, null);
            assertNotNull(size);
            // Should return natural size
            assertEquals(50.0, size.width, 0.0001);
            assertEquals(30.0, size.height, 0.0001);
        } catch (NullPointerException e) {
            fail("Null constraint should not cause NPE");
        }
    }

    // ========== Tests for arrange with all edges and null constraint ==========

    @Test
    public void testArrangeAllEdgesNullConstraint() {
        Block top = new EmptyBlock(100, 10);
        Block bottom = new EmptyBlock(100, 10);
        Block left = new EmptyBlock(20, 50);
        Block right = new EmptyBlock(20, 50);
        Block center = new EmptyBlock(40, 30);
        container.add(top, RectangleEdge.TOP);
        container.add(bottom, RectangleEdge.BOTTOM);
        container.add(left, RectangleEdge.LEFT);
        container.add(right, RectangleEdge.RIGHT);
        container.add(center, RectangleEdge.CENTER);
        try {
            Size2D size = arrangement.arrange(container, g2, null);
            assertNotNull(size);
            assertEquals(80.0, size.width, 0.0001);
            assertEquals(50.0, size.height, 0.0001);
        } catch (NullPointerException e) {
            fail("Null constraint should not cause NPE");
        }
    }
}