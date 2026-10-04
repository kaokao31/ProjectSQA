package org.jfree.data.xy;

import org.junit.Test;
import static org.junit.Assert.*;

public class XYSeriesTest {

    @Test
    public void testConstructorAndBasicProperties() {
        XYSeries series = new XYSeries("Series 1");
        assertEquals("Series 1", series.getKey());
        assertTrue(series.getAllowDuplicateXValues());
        assertTrue(series.getAutoSort());
        assertEquals(0, series.getItemCount());
        assertNotNull(series.getItems());
    }

    @Test
    public void testConstructorWithAutoSort() {
        XYSeries series = new XYSeries("Series 2", false);
        assertEquals("Series 2", series.getKey());
        assertTrue(series.getAllowDuplicateXValues());
        assertFalse(series.getAutoSort());
    }

    @Test
    public void testConstructorWithAutoSortAndDuplicates() {
        XYSeries series = new XYSeries("Series 3", false, false);
        assertEquals("Series 3", series.getKey());
        assertFalse(series.getAllowDuplicateXValues());
        assertFalse(series.getAutoSort());
    }

    @Test
    public void testAddPrimitiveDoubles() {
        XYSeries series = new XYSeries("Series");
        series.add(1.0, 2.0);
        series.add(3.0, 4.0);
        assertEquals(2, series.getItemCount());
        assertEquals(1.0, series.getX(0).doubleValue(), 0.0001);
        assertEquals(2.0, series.getY(0).doubleValue(), 0.0001);
        assertEquals(3.0, series.getX(1).doubleValue(), 0.0001);
        assertEquals(4.0, series.getY(1).doubleValue(), 0.0001);
    }

    @Test
    public void testAddNumbers() {
        XYSeries series = new XYSeries("Series");
        series.add(new Integer(1), new Double(2.5));
        assertEquals(1, series.getItemCount());
        assertEquals(1, series.getX(0).intValue());
        assertEquals(2.5, series.getY(0).doubleValue(), 0.0001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullXNumber() {
        XYSeries series = new XYSeries("Series");
        series.add(null, 1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullXPrimitive() {
        XYSeries series = new XYSeries("Series");
        // Passing null Number object
        Number n = null;
        series.add(n, 1.0);
    }

    @Test
    public void testAddDuplicateXAllowed() {
        XYSeries series = new XYSeries("Series", true, true);
        series.add(1.0, 1.0);
        series.add(1.0, 2.0);
        assertEquals(2, series.getItemCount());
    }

    @Test(expected = SeriesException.class)
    public void testAddDuplicateXNotAllowed() {
        XYSeries series = new XYSeries("Series", true, false);
        series.add(1.0, 1.0);
        series.add(1.0, 2.0);
    }

    @Test
    public void testAutoSortBehavior() {
        XYSeries series = new XYSeries("Series", true, true);
        series.add(2.0, 2.0);
        series.add(1.0, 1.0);
        series.add(3.0, 3.0);
        
        assertEquals(3, series.getItemCount());
        assertEquals(1.0, series.getX(0).doubleValue(), 0.0001);
        assertEquals(2.0, series.getX(1).doubleValue(), 0.0001);
        assertEquals(3.0, series.getX(2).doubleValue(), 0.0001);
    }

    @Test
    public void testNoAutoSortBehavior() {
        XYSeries series = new XYSeries("Series", false, true);
        series.add(2.0, 2.0);
        series.add(1.0, 1.0);
        series.add(3.0, 3.0);
        
        assertEquals(3, series.getItemCount());
        assertEquals(2.0, series.getX(0).doubleValue(), 0.0001);
        assertEquals(1.0, series.getX(1).doubleValue(), 0.0001);
        assertEquals(3.0, series.getX(2).doubleValue(), 0.0001);
    }

    @Test
    public void testRemoveItemByIndex() {
        XYSeries series = new XYSeries("Series");
        series.add(1.0, 1.0);
        series.add(2.0, 2.0);
        
        DataItem item = series.remove(0);
        assertNotNull(item);
        assertEquals(1.0, item.getX().doubleValue(), 0.0001);
        assertEquals(1, series.getItemCount());
        assertEquals(2.0, series.getX(0).doubleValue(), 0.0001);
    }

    @Test
    public void testRemoveItemByXValue() {
        XYSeries series = new XYSeries("Series");
        series.add(1.0, 1.0);
        series.add(2.0, 2.0);
        
        series.remove(new Double(1.0));
        assertEquals(1, series.getItemCount());
        assertEquals(2.0, series.getX(0).doubleValue(), 0.0001);
    }

    @Test
    public void testClear() {
        XYSeries series = new XYSeries("Series");
        series.add(1.0, 1.0);
        series.add(2.0, 2.0);
        assertEquals(2, series.getItemCount());
        
        series.clear();
        assertEquals(0, series.getItemCount());
    }

    @Test
    public void testUpdateByIndex() {
        XYSeries series = new XYSeries("Series");
        series.add(1.0, 1.0);
        series.update(0, 5.0);
        assertEquals(5.0, series.getY(0).doubleValue(), 0.0001);
    }

    @Test
    public void testUpdateWithNaN() {
        XYSeries series = new XYSeries("Series");
        series.add(1.0, 1.0);
        series.update(0, Double.NaN);
        assertTrue(Double.isNaN(series.getY(0).doubleValue()));
    }

    @Test
    public void testAddOrUpdate() {
        XYSeries series = new XYSeries("Series", true, false); // no duplicates
        series.add(1.0, 1.0);
        series.addOrUpdate(1.0, 5.0); // should update existing X=1.0
        
        assertEquals(1, series.getItemCount());
        assertEquals(5.0, series.getY(0).doubleValue(), 0.0001);
        
        series.addOrUpdate(2.0, 2.0); // should add new
        assertEquals(2, series.getItemCount());
        assertEquals(2.0, series.getY(1).doubleValue(), 0.0001);
    }

    @Test
    public void testAddOrUpdateWithAutoSortFalse() {
        XYSeries series = new XYSeries("Series", false, true);
        series.add(1.0, 1.0);
        series.addOrUpdate(1.0, 3.0);
        assertEquals(1, series.getItemCount());
        assertEquals(3.0, series.getY(0).doubleValue(), 0.0001);
        
        series.addOrUpdate(0.5, 0.5);
        assertEquals(2, series.getItemCount());
        assertEquals(0.5, series.getX(1).doubleValue(), 0.0001);
    }

    @Test
    public void testCreateCopy() throws CloneNotSupportedException {
        XYSeries series = new XYSeries("Series");
        series.add(1.0, 2.0);
        series.add(3.0, 4.0);
        
        XYSeries copy = (XYSeries) series.clone();
        assertEquals(series.getKey(), copy.getKey());
        assertEquals(series.getItemCount(), copy.getItemCount());
        assertEquals(series.getX(0), copy.getX(0));
        assertEquals(series.getY(1), copy.getY(1));
    }

    @Test
    public void testEqualsAndHashCode() {
        XYSeries series1 = new XYSeries("Series");
        series1.add(1.0, 1.0);
        
        XYSeries series2 = new XYSeries("Series");
        series2.add(1.0, 1.0);
        
        assertEquals(series1, series2);
        assertEquals(series1.hashCode(), series2.hashCode());
        
        series2.add(2.0, 2.0);
        assertNotEquals(series1, series2);
        
        assertFalse(series1.equals(null));
        assertFalse(series1.equals("Some String"));
    }

    @Test
    public void testSetMaximumItemCount() {
        XYSeries series = new XYSeries("Series");
        series.setMaximumItemCount(2);
        assertEquals(2, series.getMaximumItemCount());
        
        series.add(1.0, 1.0);
        series.add(2.0, 2.0);
        series.add(3.0, 3.0); // should drop first item (1.0)
        
        assertEquals(2, series.getItemCount());
        assertEquals(2.0, series.getX(0).doubleValue(), 0.0001);
        assertEquals(3.0, series.getX(1).doubleValue(), 0.0001);
    }

    @Test
    public void testSetMaximumItemCountWithNotification() {
        XYSeries series = new XYSeries("Series");
        series.add(1.0, 1.0);
        series.add(2.0, 2.0);
        series.add(3.0, 3.0);
        
        // Setting max item count lower than current items should drop oldest
        series.setMaximumItemCount(1);
        assertEquals(1, series.getItemCount());
        assertEquals(3.0, series.getX(0).doubleValue(), 0.0001);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetXOutOfBoundsNegative() {
        XYSeries series = new XYSeries("Series");
        series.getX(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetXOutOfBoundsLarge() {
        XYSeries series = new XYSeries("Series");
        series.getX(0);
    }

    @Test
    public void testGetItem() {
        XYSeries series = new XYSeries("Series");
        series.add(10.0, 20.0);
        DataItem item = series.getDataItem(0);
        assertNotNull(item);
        assertEquals(10.0, item.getX().doubleValue(), 0.0001);
        assertEquals(20.0, item.getY().doubleValue(), 0.0001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetItemNullArgument() {
        XYSeries series = new XYSeries("Series");
        series.getDataItem(null);
    }
}