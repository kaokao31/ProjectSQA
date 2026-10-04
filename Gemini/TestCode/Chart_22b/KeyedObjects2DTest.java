package org.jfree.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * A comprehensive JUnit 4 test suite for {@link KeyedObjects2D}.
 * Designed for Defects4J Chart 22 compatibility.
 */
public class KeyedObjects2DTest {

    private KeyedObjects2D table;

    @Before
    public void setUp() {
        table = new KeyedObjects2D();
    }

    @Test
    public void testConstructorAndInitialState() {
        assertEquals(0, table.getRowCount());
        assertEquals(0, table.getColumnCount());
        assertTrue(table.getRowKeys().isEmpty());
        assertTrue(table.getColumnKeys().isEmpty());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetRowKeyOutOfBoundsNegative() {
        table.getRowKey(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetRowKeyOutOfBoundsPositive() {
        table.getRowKey(0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetColumnKeyOutOfBoundsNegative() {
        table.getColumnKey(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetColumnKeyOutOfBoundsPositive() {
        table.getColumnKey(0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetRowIndexNegative() {
        table.getRowIndex(null);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetColumnIndexNegative() {
        table.getColumnIndex(null);
    }

    @Test
    public void testAddAndGetValues() {
        table.addObject("V1", "R1", "C1");
        table.addObject("V2", "R1", "C2");
        table.addObject("V3", "R2", "C1");

        assertEquals(2, table.getRowCount());
        assertEquals(2, table.getColumnCount());

        assertEquals("R1", table.getRowKey(0));
        assertEquals("R2", table.getRowKey(1));
        assertEquals("C1", table.getColumnKey(0));
        assertEquals("C2", table.getColumnKey(1));

        assertEquals(0, table.getRowIndex("R1"));
        assertEquals(1, table.getRowIndex("R2"));
        assertEquals(0, table.getColumnIndex("C1"));
        assertEquals(1, table.getColumnIndex("C2"));

        assertEquals("V1", table.getObject("R1", "C1"));
        assertEquals("V2", table.getObject("R1", "C2"));
        assertEquals("V3", table.getObject("R2", "C1"));
        assertNull(table.getObject("R2", "C2"));

        assertEquals("V1", table.getObject(0, 0));
        assertEquals("V2", table.getObject(0, 1));
        assertEquals("V3", table.getObject(1, 0));
        assertNull(table.getObject(1, 1));
    }

    @Test
    public void testSetObject() {
        table.setObject("Val1", "RowA", "ColA");
        assertEquals("Val1", table.getObject("RowA", "ColA"));

        // Overwrite existing
        table.setObject("Val2", "RowA", "ColA");
        assertEquals("Val2", table.getObject("RowA", "ColA"));

        // Set with row/col creation
        table.setObject("Val3", "RowB", "ColA");
        assertEquals("Val3", table.getObject("RowB", "ColA"));
        assertEquals(2, table.getRowCount());
        assertEquals(1, table.getColumnCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRowIndexNull() {
        table.getRowIndex(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetColumnIndexNull() {
        table.getColumnIndex(null);
    }

    @Test
    public void testRemoveRowByKey() {
        table.addObject("V1", "R1", "C1");
        table.addObject("V2", "R2", "C1");

        assertEquals(2, table.getRowCount());
        table.removeRow("R1");
        assertEquals(1, table.getRowCount());
        assertEquals("R2", table.getRowKey(0));
        assertNull(table.getObject("R1", "C1"));
    }

    @Test(expected = UnknownKeyException.class)
    public void testRemoveRowUnknownKey() {
        table.removeRow("UNKNOWN");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveRowByIndexOutOfBounds() {
        table.removeRow(0);
    }

    @Test
    public void testRemoveRowByIndex() {
        table.addObject("V1", "R1", "C1");
        table.addObject("V2", "R2", "C1");

        table.removeRow(0);
        assertEquals(1, table.getRowCount());
        assertEquals("R2", table.getRowKey(0));
    }

    @Test
    public void testRemoveColumnByKey() {
        table.addObject("V1", "R1", "C1");
        table.addObject("V2", "R1", "C2");

        assertEquals(2, table.getColumnCount());
        table.removeColumn("C1");
        assertEquals(1, table.getColumnCount());
        assertEquals("C2", table.getColumnKey(0));
        assertNull(table.getObject("R1", "C1"));
    }

    @Test(expected = UnknownKeyException.class)
    public void testRemoveColumnUnknownKey() {
        table.removeColumn("UNKNOWN");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveColumnByIndexOutOfBounds() {
        table.removeColumn(0);
    }

    @Test
    public void testRemoveColumnByIndex() {
        table.addObject("V1", "R1", "C1");
        table.addObject("V2", "R1", "C2");

        table.removeColumn(0);
        assertEquals(1, table.getColumnCount());
        assertEquals("C2", table.getColumnKey(0));
    }

    @Test
    public void testClear() {
        table.addObject("V1", "R1", "C1");
        table.addObject("V2", "R2", "C2");
        assertEquals(2, table.getRowCount());
        assertEquals(2, table.getColumnCount());

        table.clear();
        assertEquals(0, table.getRowCount());
        assertEquals(0, table.getColumnCount());
        assertTrue(table.getRowKeys().isEmpty());
        assertTrue(table.getColumnKeys().isEmpty());
    }

    @Test
    public void testEqualsAndHashCode() {
        KeyedObjects2D t1 = new KeyedObjects2D();
        KeyedObjects2D t2 = new KeyedObjects2D();

        assertTrue(t1.equals(t2));
        assertEquals(t1.hashCode(), t2.hashCode());

        t1.addObject("Val", "Row1", "Col1");
        assertFalse(t1.equals(t2));

        t2.addObject("Val", "Row1", "Col1");
        assertTrue(t1.equals(t2));
        assertEquals(t1.hashCode(), t2.hashCode());

        assertFalse(t1.equals(null));
        assertFalse(t1.equals("NotAKeyedObjects2D"));
    }

    @Test
    public void testCloning() throws CloneNotSupportedException {
        table.addObject("Val1", "R1", "C1");
        KeyedObjects2D clone = (KeyedObjects2D) table.clone();

        assertNotNull(clone);
        assertTrue(table.equals(clone));
        // Ensure deep-ish or independent structure check if necessary
        clone.setObject("Val2", "R1", "C1");
        assertFalse(table.equals(clone));
    }

    @Test
    public void testGetCellNullCases() {
        table.addObject("V1", "R1", "C1");
        // Unknown row or column keys should return null for getObject
        assertNull(table.getObject("UNKNOWN", "C1"));
        assertNull(table.getObject("R1", "UNKNOWN"));
        assertNull(table.getObject("UNKNOWN1", "UNKNOWN2"));
    }

    @Test
    public void testRowAndColumnKeyLists() {
        table.addObject("A", "R2", "C2");
        table.addObject("B", "R1", "C1");

        List rKeys = table.getRowKeys();
        List cKeys = table.getColumnKeys();

        assertNotNull(rKeys);
        assertNotNull(cKeys);
        assertEquals(2, rKeys.size());
        assertEquals(2, cKeys.size());
    }
}