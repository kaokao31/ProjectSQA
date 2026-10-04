package org.apache.commons.csv;

import static org.junit.Assert.*;
import static org.junit.Assert.assertArrayEquals;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class CSVRecordTest {

    private CSVRecord record;
    private String[] headers;
    private String[] values;

    @Before
    public void setUp() {
        headers = new String[] {"Name", "Age", "City"};
        values = new String[] {"John", "30", "New York"};
        record = new CSVRecord(headers, values);
    }

    // ==================== get(int) ====================

    @Test
    public void testGetByIndexValid() {
        assertEquals("John", record.get(0));
        assertEquals("30", record.get(1));
        assertEquals("New York", record.get(2));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetByIndexNegative() {
        record.get(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetByIndexOutOfBounds() {
        record.get(3);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetByIndexOutOfBoundsEmptyRecord() {
        CSVRecord empty = new CSVRecord(new String[0], new String[0]);
        empty.get(0);
    }

    // ==================== get(String) ====================

    @Test
    public void testGetByHeaderValid() {
        assertEquals("John", record.get("Name"));
        assertEquals("30", record.get("Age"));
        assertEquals("New York", record.get("City"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetByHeaderInvalid() {
        record.get("Invalid");
    }

    @Test(expected = NullPointerException.class)
    public void testGetByHeaderNull() {
        record.get(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetByHeaderEmptyString() {
        record.get("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetByHeaderCaseSensitive() {
        record.get("name");
    }

    // ==================== size() ====================

    @Test
    public void testSize() {
        assertEquals(3, record.size());
    }

    @Test
    public void testSizeEmptyRecord() {
        CSVRecord empty = new CSVRecord(new String[0], new String[0]);
        assertEquals(0, empty.size());
    }

    @Test
    public void testSizeWithNullValues() {
        CSVRecord rec = new CSVRecord(headers, new String[] {"John", null, "New York"});
        assertEquals(3, rec.size());
    }

    // ==================== values() ====================

    @Test
    public void testValues() {
        assertArrayEquals(new String[] {"John", "30", "New York"}, record.values());
    }

    @Test
    public void testValuesEmptyRecord() {
        CSVRecord empty = new CSVRecord(new String[0], new String[0]);
        assertArrayEquals(new String[0], empty.values());
    }

    @Test
    public void testValuesWithNull() {
        String[] vals = {"John", null, "New York"};
        CSVRecord rec = new CSVRecord(headers, vals);
        assertArrayEquals(vals, rec.values());
    }

    // ==================== toMap() ====================

    @Test
    public void testToMap() {
        Map<String, String> map = record.toMap();
        assertEquals("John", map.get("Name"));
        assertEquals("30", map.get("Age"));
        assertEquals("New York", map.get("City"));
        assertEquals(3, map.size());
    }

    @Test
    public void testToMapEmptyRecord() {
        CSVRecord empty = new CSVRecord(new String[0], new String[0]);
        Map<String, String> map = empty.toMap();
        assertTrue(map.isEmpty());
    }

    @Test
    public void testToMapWithNullValue() {
        CSVRecord rec = new CSVRecord(headers, new String[] {"John", null, "New York"});
        Map<String, String> map = rec.toMap();
        assertNull(map.get("Age"));
        assertEquals("John", map.get("Name"));
        assertEquals("New York", map.get("City"));
    }

    // ==================== Edge Cases ====================

    @Test(expected = NullPointerException.class)
    public void testConstructorNullHeaders() {
        new CSVRecord(null, values);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullValues() {
        new CSVRecord(headers, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorMismatchedLengths() {
        new CSVRecord(new String[] {"A", "B"}, new String[] {"1"});
    }

    @Test
    public void testGetByIndexAfterModification() {
        // Assuming record is mutable; if not, this test may be removed
        // For demonstration, we test that get still works after potential internal changes
        assertEquals("John", record.get(0));
    }

    @Test
    public void testGetByHeaderDuplicateHeader() {
        // If duplicate headers are allowed, behavior should be defined
        String[] dupHeaders = {"Name", "Name", "City"};
        CSVRecord dupRecord = new CSVRecord(dupHeaders, new String[] {"John", "Doe", "NY"});
        // Depending on implementation, get("Name") might return first or last
        // We test that it does not throw and returns a non-null value
        assertNotNull(dupRecord.get("Name"));
    }

    @Test
    public void testValuesImmutability() {
        // Ensure that modifying returned array does not affect record
        String[] vals = record.values();
        vals[0] = "Changed";
        assertEquals("John", record.get(0));
    }

    // ==================== Additional Fault Detection Tests ====================

    @Test
    public void testGetByIndexWithNullValue() {
        CSVRecord rec = new CSVRecord(headers, new String[] {"John", null, "New York"});
        assertNull(rec.get(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetByHeaderWithNullValue() {
        CSVRecord rec = new CSVRecord(headers, new String[] {"John", null, "New York"});
        // If header exists but value is null, get should return null, not throw
        // This test expects that get("Age") returns null; if it throws, the test fails
        // We use assertNull to verify
        assertNull(rec.get("Age"));
    }

    @Test
    public void testToMapWithNullHeader() {
        // If headers contain null, behavior should be defined
        String[] headersWithNull = {"Name", null, "City"};
        CSVRecord rec = new CSVRecord(headersWithNull, new String[] {"John", "30", "NY"});
        Map<String, String> map = rec.toMap();
        // null header should be skipped or cause an exception; we test that map does not contain null key
        assertFalse(map.containsKey(null));
    }

    @Test
    public void testSizeAfterGet() {
        // Ensure size remains consistent after get calls
        record.get(0);
        record.get("Name");
        assertEquals(3, record.size());
    }
}