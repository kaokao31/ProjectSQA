package org.apache.commons.math.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class MultidimensionalCounterTest {

    @Test
    public void testMultidimensionalCounterValid() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 3, 4);
        Assert.assertEquals(3, counter.getDimension());
        
        int[] counts = counter.getCounts(5);
        Assert.assertArrayEquals(new int[]{0, 1, 1}, counts);
        
        Assert.assertEquals(5, counter.getCount(0, 1, 1));
        
        int[] sizes = counter.getSizes();
        Assert.assertArrayEquals(new int[]{12, 4, 1}, sizes);
        
        Assert.assertEquals(24, counter.getSize());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorZeroDimension() {
        new MultidimensionalCounter(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNegativeDimension() {
        new MultidimensionalCounter(2, -1, 4);
    }

    @Test(expected = OutOfRangeException.class)
    public void testGetCountOutOfBoundsLow() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 2);
        counter.getCount(-1, 0);
    }

    @Test(expected = OutOfRangeException.class)
    public void testGetCountOutOfBoundsHigh() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 2);
        counter.getCount(2, 0);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testGetCountWrongDimension() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 2);
        counter.getCount(1, 0, 0);
    }

    @Test(expected = OutOfRangeException.class)
    public void testGetCountsOutOfBounds() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 2);
        counter.getCounts(4);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testGetCountsWrongDimension() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 2);
        counter.getCounts(new int[]{1});
    }

    @Test(expected = OutOfRangeException.class)
    public void testGetCountsArrayOutOfBounds() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 2);
        counter.getCounts(new int[]{2, 0});
    }

    @Test
    public void testIterator() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 2);
        Iterator<MultidimensionalCounter.Entry> iterator = counter.iterator();
        
        Assert.assertTrue(iterator.hasNext());
        
        MultidimensionalCounter.Entry entry1 = iterator.next();
        Assert.assertEquals(0, entry1.getCount());
        Assert.assertArrayEquals(new int[]{0, 0}, entry1.getIndices());
        Assert.assertEquals(0, entry1.getIndex(0));
        Assert.assertEquals(0, entry1.getIndex(1));
        
        // Test toString and other iterator elements
        Assert.assertNotNull(entry1.toString());

        // Iterate through all
        while (iterator.hasNext()) {
            iterator.next();
        }
        
        Assert.assertFalse(iterator.hasNext());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testIteratorRemove() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 2);
        Iterator<MultidimensionalCounter.Entry> iterator = counter.iterator();
        iterator.remove();
    }

    @Test(expected = NoSuchElementException.class)
    public void testIteratorNoSuchElement() {
        MultidimensionalCounter counter = new MultidimensionalCounter(1, 1);
        Iterator<MultidimensionalCounter.Entry> iterator = counter.iterator();
        Assert.assertTrue(iterator.hasNext());
        iterator.next();
        Assert.assertFalse(iterator.hasNext());
        iterator.next();
    }

    @Test
    public void testToString() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 2);
        String str = counter.toString();
        Assert.assertNotNull(str);
        Assert.assertTrue(str.length() > 0);
    }
}