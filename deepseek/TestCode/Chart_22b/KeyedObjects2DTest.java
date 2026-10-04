package org.jfree.data;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@link KeyedObjects2D}. 
 * Designed to achieve high code coverage and detect potential faults.
 */
public class KeyedObjects2DTest {

    private KeyedObjects2D data;

    @Before
    public void setUp() {
        data = new KeyedObjects2D();
    }

    // ---------- addObject ----------

    @Test
    public void testAddObject() {
        data.addObject(1.0, "Row1", "Col1");
        assertEquals("Row count should be 1", 1, data.getRowCount());
        assertEquals("Column count should be 1", 1, data.getColumnCount());
        assertEquals("Value should be 1.0", 1.0, data.getObject("Row1", "Col1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddObjectNullRowKey() {
        data.addObject(1.0, null, "Col1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddObjectNullColumnKey() {
        data.addObject(1.0, "Row1", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddObjectBothNullKeys() {
        data.addObject(1.0, null, null);
    }

    // ---------- getObject ----------

    @Test
    public void testGetObjectForExistingKey() {
        data.addObject(2.5, "R1", "C1");
        assertEquals(2.5, data.getObject("R1", "C1"));
    }

    @Test
    public void testGetObjectNonExistentKey() {
        assertNull(data.getObject("MissingRow", "MissingCol"));
        assertNull(data.getObject("MissingRow", "C1"));
        assertNull(data.getObject("R1", "MissingCol"));
    }

    @Test
    public void testGetObjectAfterRemove() {
        data.addObject(10, "R", "C");
        data.removeRow("R");
        assertNull(data.getObject("R", "C"));
    }

    // ---------- removeRow ----------

    @Test
    public void testRemoveRowByKey() {
        data.addObject(1, "RowA", "ColA");
        data.addObject(2, "RowB", "ColA");
        data.removeRow("RowA");
        assertEquals("Row count after removal", 1, data.getRowCount());
        assertNull(data.getObject("RowA", "ColA"));
        assertNotNull(data.getObject("RowB", "ColA"));
    }

    @Test
    public void testRemoveRowByKeyNonExistent() {
        data.addObject(1, "RowA", "ColA");
        data.removeRow("NonExistent");
        assertEquals("Row count unchanged", 1, data.getRowCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveRowByKeyNull() {
        data.removeRow((Comparable) null);
    }

    @Test
    public void testRemoveRowByIndex() {
        data.addObject(1, "R1", "C1");
        data.addObject(2, "R2", "C1");
        data.removeRow(0);
        assertEquals("Row count after removal", 1, data.getRowCount());
        assertEquals("Remaining row key", "R2", data.getRowKey(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveRowByIndexNegative() {
        data.addObject(1, "R1", "C1");
        data.removeRow(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveRowByIndexTooLarge() {
        data.addObject(1, "R1", "C1");
        data.removeRow(1);
    }

    // ---------- removeColumn ----------

    @Test
    public void testRemoveColumnByKey() {
        data.addObject(1, "R1", "C1");
        data.addObject(2, "R1", "C2");
        data.removeColumn("C1");
        assertEquals("Column count after removal", 1, data.getColumnCount());
        assertNull(data.getObject("R1", "C1"));
        assertNotNull(data.getObject("R1", "C2"));
    }

    @Test
    public void testRemoveColumnByKeyNonExistent() {
        data.addObject(1, "R1", "C1");
        data.removeColumn("Missing");
        assertEquals("Column count unchanged", 1, data.getColumnCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveColumnByKeyNull() {
        data.removeColumn((Comparable) null);
    }

    @Test
    public void testRemoveColumnByIndex() {
        data.addObject(1, "R1", "C1");
        data.addObject(2, "R1", "C2");
        data.removeColumn(0);
        assertEquals("Column count after removal", 1, data.getColumnCount());
        assertEquals("Remaining column key", "C2", data.getColumnKey(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveColumnByIndexNegative() {
        data.addObject(1, "R1", "C1");
        data.removeColumn(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveColumnByIndexTooLarge() {
        data.addObject(1, "R1", "C1");
        data.removeColumn(1);
    }

    // ---------- getRowKey / getColumnKey ----------

    @Test
    public void testGetRowKey() {
        data.addObject(1, "Row", "Col");
        assertEquals("Row", data.getRowKey(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetRowKeyNegative() {
        data.getRowKey(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetRowKeyTooLarge() {
        data.getRowKey(0);
    }

    @Test
    public void testGetColumnKey() {
        data.addObject(1, "Row", "Col");
        assertEquals("Col", data.getColumnKey(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetColumnKeyNegative() {
        data.getColumnKey(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetColumnKeyTooLarge() {
        data.getColumnKey(0);
    }

    // ---------- clone ----------

    @Test
    public void testClone() throws CloneNotSupportedException {
        data.addObject(10, "R1", "C1");
        data.addObject(20, "R2", "C2");
        KeyedObjects2D clone = (KeyedObjects2D) data.clone();
        assertNotSame("Clone should be different object", data, clone);
        assertEquals("Clone row count", 2, clone.getRowCount());
        assertEquals("Clone value", 10, clone.getObject("R1", "C1"));
        // Modify original to verify independence
        data.addObject(30, "R1", "C3");
        assertNull("Clone should not see added column", clone.getObject("R1", "C3"));
    }

    // ---------- equals ---------- (optional but useful for coverage)

    @Test
    public void testEquals() {
        KeyedObjects2D obj1 = new KeyedObjects2D();
        KeyedObjects2D obj2 = new KeyedObjects2D();
        assertTrue("Empty objects should be equal", obj1.equals(obj2));
        obj1.addObject(1, "R", "C");
        assertFalse("After adding one element, equal should fail", obj1.equals(obj2));
        obj2.addObject(1, "R", "C");
        assertTrue("Both contain identical element", obj1.equals(obj2));
        obj2.addObject(2, "R2", "C");
        assertFalse("Different sizes", obj1.equals(obj2));
    }

    @Test
    public void testEqualsSameObject() {
        assertTrue("Object equals itself", data.equals(data));
    }

    @Test
    public void testEqualsWithNull() {
        assertFalse("Null not equal", data.equals(null));
    }

    @Test
    public void testEqualsWithDifferentType() {
        assertFalse("Different type not equal", data.equals("string"));
    }

    // ---------- setObject (update existing) ----------

    @Test
    public void testSetObject() {
        data.addObject(1, "R", "C");
        data.addObject(2, "R", "C"); // update
        assertEquals("Value should be updated", 2, data.getObject("R", "C"));
        assertEquals("Row count unchanged", 1, data.getRowCount());
        assertEquals("Column count unchanged", 1, data.getColumnCount());
    }

    // ---------- multiple row/column operations ----------

    @Test
    public void testRemoveAllRows() {
        data.addObject(1, "R1", "C1");
        data.addObject(2, "R2", "C1");
        data.removeRow("R1");
        data.removeRow("R2");
        assertEquals("No rows left", 0, data.getRowCount());
        assertEquals("Columns should also be zero", 0, data.getColumnCount());
    }

    @Test
    public void testRemoveAllColumns() {
        data.addObject(1, "R1", "C1");
        data.addObject(2, "R1", "C2");
        data.removeColumn("C1");
        data.removeColumn("C2");
        assertEquals("No columns left", 0, data.getColumnCount());
        assertEquals("Rows should also be zero", 0, data.getRowCount());
    }

    // ---------- large number of entries ----------

    @Test
    public void testMultipleAddsAndGets() {
        for (int r = 0; r < 100; r++) {
            for (int c = 0; c < 100; c++) {
                data.addObject(r * 100 + c, "R" + r, "C" + c);
            }
        }
        assertEquals("Row count", 100, data.getRowCount());
        assertEquals("Column count", 100, data.getColumnCount());
        assertEquals("Value at (50,50)", 5050, data.getObject("R50", "C50"));
    }

    // ---------- getRowCount / getColumnCount after varying operations ----------

    @Test
    public void testCountsAfterAllOperations() {
        assertEquals(0, data.getRowCount());
        assertEquals(0, data.getColumnCount());
        data.addObject(1, "R", "C");
        assertEquals(1, data.getRowCount());
        assertEquals(1, data.getColumnCount());
        data.addObject(2, "R2", "C");
        assertEquals(2, data.getRowCount());
        assertEquals(1, data.getColumnCount());
        data.addObject(3, "R", "C2");
        assertEquals(2, data.getRowCount());
        assertEquals(2, data.getColumnCount());
        data.removeRow("R");
        assertEquals(1, data.getRowCount());
        assertEquals(1, data.getColumnCount()); // after removing row, column with that row only might disappear
        // However if there is another row with column C? Actually R2 has column C, so column C remains.
        // But column C2 had only row R, so it disappears. So count becomes 1.
    }

    // ---------- Additional edge: getObject with null keys ----------

    @Test(expected = IllegalArgumentException.class)
    public void testGetObjectNullRowKey() {
        data.getObject(null, "Col");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetObjectNullColumnKey() {
        data.getObject("Row", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetObjectBothNullKeys() {
        data.getObject(null, null);
    }

    // ---------- hashCode (if equals is overridden, we should test) ----------

    @Test
    public void testHashCodeConsistency() {
        KeyedObjects2D obj1 = new KeyedObjects2D();
        obj1.addObject(1, "R", "C");
        KeyedObjects2D obj2 = new KeyedObjects2D();
        obj2.addObject(1, "R", "C");
        assertEquals("Equal objects should have equal hashcodes", obj1.hashCode(), obj2.hashCode());
    }
}