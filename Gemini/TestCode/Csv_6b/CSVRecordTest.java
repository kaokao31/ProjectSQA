package org.apache.commons.csv;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class CSVRecordTest {

    private String[] values;
    private Map<String, Integer> mapping;
    private CSVRecord record;

    @Before
    public void setUp() {
        values = new String[] { "A", "B", "C" };
        mapping = new HashMap<String, Integer>();
        mapping.put("first", 0);
        mapping.put("second", 1);
        mapping.put("third", 2);
        
        record = new CSVRecord(values, mapping, "comment", 1L);
    }

    @Test
    public void testGetWithValidIndex() {
        assertEquals("A", record.get(0));
        assertEquals("B", record.get(1));
        assertEquals("C", record.get(2));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetWithNegativeIndex() {
        record.get(-1);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetWithOutOfBoundsIndex() {
        record.get(3);
    }

    @Test
    public void testGetWithValidName() {
        assertEquals("A", record.get("first"));
        assertEquals("B", record.get("second"));
        assertEquals("C", record.get("third"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetWithNullMappingName() {
        record.get("nonexistent");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetWithUnmappedName() {
        CSVRecord recordNoMapping = new CSVRecord(values, null, "comment", 1L);
        recordNoMapping.get("first");
    }

    @Test
    public void testGetComment() {
        assertEquals("comment", record.getComment());
    }

    @Test
    public void testGetRecordNumber() {
        assertEquals(1L, record.getRecordNumber());
    }

    @Test
    public void testIterator() {
        int i = 0;
        for (String val : record) {
            assertEquals(values[i++], val);
        }
        assertEquals(3, i);
    }

    @Test
    public void testPutInMap() {
        Map<String, String> map = new HashMap<String, String>();
        record.putIn(map);
        assertEquals(3, map.size());
        assertEquals("A", map.get("first"));
        assertEquals("B", map.get("second"));
        assertEquals("C", map.get("third"));
    }

    @Test
    public void testSize() {
        assertEquals(3, record.size());
    }

    @Test
    public void testValues() {
        String[] recordValues = record.values();
        assertNotNull(recordValues);
        assertArrayEquals(values, recordValues);
        // Ensure it's not exposing the internal array directly or test behavior
        assertSame(values, recordValues);
    }

    @Test
    public void testToMap() {
        Map<String, String> map = record.toMap();
        assertNotNull(map);
        assertEquals(3, map.size());
        assertEquals("A", map.get("first"));
        assertEquals("B", map.get("second"));
        assertEquals("C", map.get("third"));
    }

    @Test
    public void testNullMappingAndComment() {
        CSVRecord nullRec = new CSVRecord(new String[] { "X" }, null, null, 10L);
        assertNull(nullRec.getComment());
        assertEquals(10L, nullRec.getRecordNumber());
        assertEquals(1, nullRec.size());
        assertEquals("X", nullRec.get(0));
    }

    @Test
    public void testGetWithMappingNullValuesMap() {
        // Specifically targeting lines where mapping is present but record values might be handled
        Map<String, Integer> complexMap = new HashMap<String, Integer>();
        complexMap.put("col1", 0);
        complexMap.put("col2", 5); // Index out of bounds for values length 2
        
        CSVRecord r = new CSVRecord(new String[] { "val1", "val2" }, complexMap, "", 2L);
        assertEquals("val1", r.get("col1"));
        
        try {
            r.get("col2");
            fail("Expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
    }
}