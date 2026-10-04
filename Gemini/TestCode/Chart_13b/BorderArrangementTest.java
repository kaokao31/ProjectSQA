package org.jfree.chart.block;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.jfree.chart.util.RectangleEdge;
import org.jfree.ui.Size2D;
import org.junit.Test;

public class BorderArrangementTest {

    @Test
    public void testConstructorAndAdd() {
        BorderArrangement ba = new BorderArrangement();
        BlockContainer container = new BlockContainer(ba);
        
        EmptyBlock b1 = new EmptyBlock(10, 10);
        EmptyBlock b2 = new EmptyBlock(20, 20);
        EmptyBlock b3 = new EmptyBlock(30, 30);
        EmptyBlock b4 = new EmptyBlock(40, 40);
        EmptyBlock b5 = new EmptyBlock(50, 50);

        ba.add(b1, RectangleEdge.TOP);
        ba.add(b2, RectangleEdge.BOTTOM);
        ba.add(b3, RectangleEdge.LEFT);
        ba.add(b4, RectangleEdge.RIGHT);
        ba.add(b5, null); // Center block (default)

        // Test remove
        ba.remove(b1);
        ba.clear();
    }

    @Test
    public void testArrangeNN() {
        BorderArrangement ba = new BorderArrangement();
        BlockContainer container = new BlockContainer(ba);
        
        ba.add(new EmptyBlock(10, 10), RectangleEdge.TOP);
        ba.add(new EmptyBlock(10, 10), RectangleEdge.BOTTOM);
        ba.add(new EmptyBlock(10, 10), RectangleEdge.LEFT);
        ba.add(new EmptyBlock(10, 10), RectangleEdge.RIGHT);
        ba.add(new EmptyBlock(10, 10), null);

        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();

        Size2D size = ba.arrangeNN(container, g2);
        assertNotNull(size);
    }

    @Test
    public void testArrangeFF() {
        BorderArrangement ba = new BorderArrangement();
        BlockContainer container = new BlockContainer(ba);
        
        ba.add(new EmptyBlock(10, 10), RectangleEdge.TOP);
        ba.add(new EmptyBlock(10, 10), RectangleEdge.BOTTOM);
        ba.add(new EmptyBlock(10, 10), RectangleEdge.LEFT);
        ba.add(new EmptyBlock(10, 10), RectangleEdge.RIGHT);
        ba.add(new EmptyBlock(10, 10), null);

        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();

        RectangleConstraint constraint = new RectangleConstraint(100.0, 100.0);
        Size2D size = ba.arrange(container, g2, constraint);
        assertNotNull(size);
    }

    @Test
    public void testArrangeRF() {
        BorderArrangement ba = new BorderArrangement();
        BlockContainer container = new BlockContainer(ba);
        
        ba.add(new EmptyBlock(10, 10), RectangleEdge.TOP);
        ba.add(new EmptyBlock(10, 10), RectangleEdge.BOTTOM);
        ba.add(new EmptyBlock(10, 10), RectangleEdge.LEFT);
        ba.add(new EmptyBlock(10, 10), RectangleEdge.RIGHT);
        ba.add(new EmptyBlock(10, 10), null);

        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();

        RectangleConstraint constraint = RectangleConstraint.deriveWidthRange(
            new RectangleConstraint(100.0, 100.0), 50.0, 150.0
        );
        Size2D size = ba.arrange(container, g2, constraint);
        assertNotNull(size);
    }

    @Test
    public void testArrangeFR() {
        BorderArrangement ba = new BorderArrangement();
        BlockContainer container = new BlockContainer(ba);
        
        ba.add(new EmptyBlock(10, 10), RectangleEdge.TOP);
        ba.add(new EmptyBlock(10, 10), RectangleEdge.BOTTOM);
        ba.add(new EmptyBlock(10, 10), RectangleEdge.LEFT);
        ba.add(new EmptyBlock(10, 10), RectangleEdge.RIGHT);
        ba.add(new EmptyBlock(10, 10), null);

        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();

        RectangleConstraint constraint = RectangleConstraint.deriveHeightRange(
            new RectangleConstraint(100.0, 100.0), 50.0, 150.0
        );
        Size2D size = ba.arrange(container, g2, constraint);
        assertNotNull(size);
    }

    @Test
    public void testArrangeRR() {
        BorderArrangement ba = new BorderArrangement();
        BlockContainer container = new BlockContainer(ba);
        
        ba.add(new EmptyBlock(10, 10), RectangleEdge.TOP);
        ba.add(new EmptyBlock(10, 10), RectangleEdge.BOTTOM);
        ba.add(new EmptyBlock(10, 10), RectangleEdge.LEFT);
        ba.add(new EmptyBlock(10, 10), RectangleEdge.RIGHT);
        ba.add(new EmptyBlock(10, 10), null);

        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();

        RectangleConstraint constraint = new RectangleConstraint(
            org.jfree.chart.util.Range.NONE, 
            org.jfree.chart.util.Range.NONE
        );
        // Better RF / RR combo using bounds
        RectangleConstraint rc = new RectangleConstraint(100.0, new org.jfree.chart.util.Range(10.0, 50.0),
                org.jfree.chart.data.RangeType.RANGE, 100.0, new org.jfree.chart.util.Range(10.0, 50.0),
                org.jfree.chart.data.RangeType.RANGE);

        Size2D size = ba.arrange(container, g2, rc);
        assertNotNull(size);
    }

    @Test
    public void testArrangeRN() {
        BorderArrangement ba = new BorderArrangement();
        BlockContainer container = new BlockContainer(ba);
        ba.add(new EmptyBlock(10, 10), RectangleEdge.TOP);
        
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();

        RectangleConstraint constraint = new RectangleConstraint(
            new org.jfree.chart.util.Range(10.0, 50.0), null
        );
        Size2D size = ba.arrange(container, g2, constraint);
        assertNotNull(size);
    }

    @Test
    public void testArrangeFN() {
        BorderArrangement ba = new BorderArrangement();
        BlockContainer container = new BlockContainer(ba);
        ba.add(new EmptyBlock(10, 10), RectangleEdge.LEFT);
        
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();

        RectangleConstraint constraint = new RectangleConstraint(
            100.0, null
        );
        Size2D size = ba.arrange(container, g2, constraint);
        assertNotNull(size);
    }

    @Test
    public void testArrangeNF() {
        BorderArrangement ba = new BorderArrangement();
        BlockContainer container = new BlockContainer(ba);
        ba.add(new EmptyBlock(10, 10), RectangleEdge.TOP);
        
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();

        RectangleConstraint constraint = new RectangleConstraint(
            null, 100.0
        );
        Size2D size = ba.arrange(container, g2, constraint);
        assertNotNull(size);
    }

    @Test
    public void testArrangeNR() {
        BorderArrangement ba = new BorderArrangement();
        BlockContainer container = new BlockContainer(ba);
        ba.add(new EmptyBlock(10, 10), RectangleEdge.TOP);
        
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();

        RectangleConstraint constraint = new RectangleConstraint(
            null, new org.jfree.chart.util.Range(10.0, 50.0)
        );
        Size2D size = ba.arrange(container, g2, constraint);
        assertNotNull(size);
    }

    @Test
    public void testEqualsAndHashCode() {
        BorderArrangement ba1 = new BorderArrangement();
        BorderArrangement ba2 = new BorderArrangement();

        assertTrue(ba1.equals(ba2));
        assertEquals(ba1.hashCode(), ba2.hashCode());
        
        assertTrue(ba1.equals(ba1));
        assertFalse(ba1.equals(null));
        assertFalse(ba1.equals("Some String"));
    }

    @Test
    public void testCloning() throws CloneNotSupportedException {
        BorderArrangement ba1 = new BorderArrangement();
        ba1.add(new EmptyBlock(10, 10), RectangleEdge.TOP);
        
        BorderArrangement ba2 = (BorderArrangement) ba1.clone();
        assertTrue(ba1.equals(ba2));
    }
}