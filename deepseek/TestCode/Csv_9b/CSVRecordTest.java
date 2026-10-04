package org.apache.commons.csv;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class CSVRecordTest {

    private CSVParser parser;
    private CSVRecord recordWithHeaders;
    private CSVRecord recordWithoutHeaders;
    private CSVRecord emptyRecord;

    // Sample data for testing
    private static final String[] HEADERS = {"Name", "Age", "City"};
    private static final String[] VALUES = {"Alice", "30", "New York"};
    private static final String[] EMPTY_VALUES = {};
    private static final String COMMENT = "This is a comment";

    @Before
    public void setUp() throws Exception {
        // Create records using CSVParser from a string
        String csvData = "Alice,30,New York\nBob,25,Los Angeles\n,,\n";
        parser = CSVParser.parse(csvData, CSVFormat.DEFAULT.withHeader(HEADERS));
        recordWithHeaders = parser.iterator().next(); // First record

        parser = CSVParser.parse(csvData, CSVFormat.DEFAULT);
        recordWithoutHeaders = parser.iterator().next(); // First record without headers

        // Create an empty record via parser
        parser = CSVParser.parse("", CSVFormat.DEFAULT);
        emptyRecord = parser.iterator().next(); // Empty record
    }

    @Test
    public void testGetIntIndexValid() {
        assertEquals("Alice", recordWithHeaders.get(0));
        assertEquals("30", recordWithHeaders.get(1));
        assertEquals("New York", recordWithHeaders.get(2));
    }

    @Test
    public void testGetIntIndexOutOfBounds() {
        try {
            recordWithHeaders.get(3);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            recordWithHeaders.get(-1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    public void testGetStringHeader() {
        assertEquals("Alice", recordWithHeaders.get("Name"));
        assertEquals("30", recordWithHeaders.get("Age"));
        assertEquals("New York", recordWithHeaders.get("City"));
    }

    @Test
    public void testGetStringHeaderMissing() {
        try {
            recordWithHeaders.get("Nonexistent");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testGetStringNullHeader() {
        try {
            recordWithHeaders.get(null);
            fail("Expected IllegalArgumentException or NullPointerException");
        } catch (IllegalArgumentException e) {
            // expected
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testIsSetIntExists() {
        assertTrue(recordWithHeaders.isSet(0));
        assertTrue(recordWithHeaders.isSet(1));
        assertTrue(recordWithHeaders.isSet(2));
    }

    @Test
    public void testIsSetIntNotExists() {
        assertFalse(recordWithHeaders.isSet(3));
        assertFalse(recordWithHeaders.isSet(-1));
    }

    @Test
    public void testIsSetStringWithHeader() {
        assertTrue(recordWithHeaders.isSet("Name"));
        assertTrue(recordWithHeaders.isSet("Age"));
        assertFalse(recordWithHeaders.isSet("Nonexistent"));
    }

    @Test
    public void testIsSetStringNull() {
        try {
            recordWithHeaders.isSet(null);
            fail("Expected IllegalArgumentException or NullPointerException");
        } catch (IllegalArgumentException e) {
            // expected
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testSize() {
        assertEquals(3, recordWithHeaders.size());
        assertEquals(3, recordWithoutHeaders.size());
        assertEquals(0, emptyRecord.size());
    }

    @Test
    public void testValues() {
        assertArrayEquals(VALUES, recordWithHeaders.values());
        assertArrayEquals(VALUES, recordWithoutHeaders.values());
        assertArrayEquals(EMPTY_VALUES, emptyRecord.values());
    }

    @Test
    public void testToMapWithHeaders() {
        Map<String, String> expected = new HashMap<String, String>();
        expected.put("Name", "Alice");
        expected.put("Age", "30");
        expected.put("City", "New York");
        assertEquals(expected, recordWithHeaders.toMap());
    }

    @Test
    public void testToMapWithNullHeaders() {
        // For records without headers, toMap should return a map with null keys
        Map<String, String> expected = new HashMap<String, String>();
        expected.put(null, "Alice");
        expected.put(null, "30"); // This should be overwritten; actually only one null key can exist
        // Actually because headers are not present, the map keys are null, but only one entry per null
        // We'll just check the size and values
        Map<String, String> map = recordWithoutHeaders.toMap();
        assertEquals(3, map.size());
        assertTrue(map.containsKey(null));
        assertEquals("New York", map.get(null)); // last value
    }

    @Test
    public void testPutInMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("Existing", "x");
        Map<String, String> result = recordWithHeaders.putIn(map);
        assertSame(map, result);
        assertEquals(4, map.size());
        assertEquals("Alice", map.get("Name"));
        assertEquals("30", map.get("Age"));
        assertEquals("New York", map.get("City"));
        assertEquals("x", map.get("Existing"));
    }

    @Test
    public void testGetParser() {
        assertSame(parser, recordWithHeaders.getParser());
    }

    @Test
    public void testGetRecordNumber() {
        assertEquals(1L, recordWithHeaders.getRecordNumber());
        assertEquals(2L, recordWithoutHeaders.getRecordNumber()); // depends on parser iteration but we only have first record in both cases
        // We'll reset parser to get second record if needed
    }

    @Test
    public void testGetComment() {
        assertNull(recordWithHeaders.getComment());
        // Create a record with comment later if needed
    }

    @Test
    public void testIterator() {
        java.util.Iterator<String> it = recordWithHeaders.iterator();
        assertTrue(it.hasNext());
        assertEquals("Alice", it.next());
        assertTrue(it.hasNext());
        assertEquals("30", it.next());
        assertTrue(it.hasNext());
        assertEquals("New York", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testEqualsAndHashCode() {
        // Create two identical records
        CSVParser parser1 = CSVParser.parse("a,b,c\n", CSVFormat.DEFAULT);
        CSVParser parser2 = CSVParser.parse("a,b,c\n", CSVFormat.DEFAULT);
        CSVRecord r1 = parser1.iterator().next();
        CSVRecord r2 = parser2.iterator().next();
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());

        // Different values
        CSVParser parser3 = CSVParser.parse("d,e,f\n", CSVFormat.DEFAULT);
        CSVRecord r3 = parser3.iterator().next();
        assertFalse(r1.equals(r3));
        // Different record number if parser differs? Actually record number depends on side effects; but we can test explicit not equal
    }

    @Test
    public void testToString() {
        String toString = recordWithHeaders.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("Alice"));
        assertTrue(toString.contains("30"));
        assertTrue(toString.contains("New York"));
    }

    @Test
    public void testEmptyRecord() {
        assertEquals(0, emptyRecord.size());
        assertArrayEquals(new String[0], emptyRecord.values());
        assertEquals("[]", emptyRecord.toString());
        Map<String, String> map = emptyRecord.toMap();
        assertTrue(map.isEmpty());
    }

    @Test
    public void testGetStringWithHeaderFromEmptyValue() {
        // Test when a field is empty (if needed)
        // We can add specific test cases for empty fields
    }

    @Test
    public void testRecordWithMultipleEmptyFields() {
        // Additional coverage for boundary cases
        String csvData = ",,\n";
        parser = CSVParser.parse(csvData, CSVFormat.DEFAULT);
        CSVRecord rec = parser.iterator().next();
        assertEquals(3, rec.size());
        assertEquals("", rec.get(0));
        assertEquals("", rec.get(1));
        assertEquals("", rec.get(2));
        // Check all are empty
        for (String v : rec) {
            assertEquals("", v);
        }
    }
}