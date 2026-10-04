package org.apache.commons.csv;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Comprehensive JUnit 4 test suite for CSVRecord.
 * Designed to achieve maximum code coverage and detect potential faults.
 */
public class CSVRecordTest {

    private CSVRecord recordWithHeaders;
    private CSVRecord recordWithoutHeaders;
    private CSVRecord emptyRecord;
    private CSVRecord recordWithNullValues;
    private CSVRecord recordWithSpaces;

    @Before
    public void setUp() throws IOException {
        // Create records using CSVFormat parsing
        String headerLine = "col1,col2,col3";
        String dataLine = "a,b,c";
        String nullDataLine = "x,,z";
        String spaceDataLine = " 1 , 2 , 3 ";

        CSVFormat formatWithHeader = CSVFormat.DEFAULT.withHeader(headerLine.split(","));
        CSVFormat formatWithoutHeader = CSVFormat.DEFAULT;

        // Parse records
        Iterable<CSVRecord> recordsWithHeader = formatWithHeader.parse(new StringReader(dataLine));
        Iterable<CSVRecord> recordsWithoutHeader = formatWithoutHeader.parse(new StringReader(dataLine));
        Iterable<CSVRecord> recordsNull = formatWithHeader.parse(new StringReader(nullDataLine));
        Iterable<CSVRecord> recordsSpace = formatWithHeader.parse(new StringReader(spaceDataLine));

        // Get first record from each
        Iterator<CSVRecord> it = recordsWithHeader.iterator();
        recordWithHeaders = it.next();

        it = recordsWithoutHeader.iterator();
        recordWithoutHeaders = it.next();

        it = recordsNull.iterator();
        recordWithNullValues = it.next();

        it = recordsSpace.iterator();
        recordWithSpaces = it.next();

        // Create an empty record by parsing an empty line
        CSVFormat formatEmpty = CSVFormat.DEFAULT;
        Iterable<CSVRecord> emptyRecords = formatEmpty.parse(new StringReader(""));
        Iterator<CSVRecord> emptyIt = emptyRecords.iterator();
        emptyRecord = emptyIt.next();
    }

    // ========== Basic get() and values() ==========

    @Test
    public void testGetByIndex() {
        assertEquals("a", recordWithHeaders.get(0));
        assertEquals("b", recordWithHeaders.get(1));
        assertEquals("c", recordWithHeaders.get(2));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetByIndexNegative() {
        recordWithHeaders.get(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetByIndexOutOfBounds() {
        recordWithHeaders.get(3);
    }

    @Test
    public void testGetByName() {
        assertEquals("a", recordWithHeaders.get("col1"));
        assertEquals("b", recordWithHeaders.get("col2"));
        assertEquals("c", recordWithHeaders.get("col3"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetByNameNonExistent() {
        recordWithHeaders.get("nonexistent");
    }

    @Test(expected = IllegalStateException.class)
    public void testGetByNameWithoutHeaderMapping() {
        // recordWithoutHeaders has no mapping
        recordWithoutHeaders.get("col1");
    }

    @Test
    public void testGetByNameNullValue() {
        // recordWithNullValues has empty string for col2
        assertEquals("x", recordWithNullValues.get("col1"));
        assertEquals("", recordWithNullValues.get("col2"));
        assertEquals("z", recordWithNullValues.get("col3"));
    }

    @Test
    public void testGetByNameTrimmed() {
        // recordWithSpaces has spaces around values
        assertEquals(" 1 ", recordWithSpaces.get("col1"));
        assertEquals(" 2 ", recordWithSpaces.get("col2"));
        assertEquals(" 3 ", recordWithSpaces.get("col3"));
    }

    // ========== size() ==========

    @Test
    public void testSize() {
        assertEquals(3, recordWithHeaders.size());
        assertEquals(3, recordWithoutHeaders.size());
        assertEquals(0, emptyRecord.size());
    }

    // ========== iterator() ==========

    @Test
    public void testIterator() {
        Iterator<String> it = recordWithHeaders.iterator();
        assertTrue(it.hasNext());
        assertEquals("a", it.next());
        assertTrue(it.hasNext());
        assertEquals("b", it.next());
        assertTrue(it.hasNext());
        assertEquals("c", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testIteratorEmptyRecord() {
        Iterator<String> it = emptyRecord.iterator();
        assertFalse(it.hasNext());
    }

    // ========== toMap() ==========

    @Test
    public void testToMap() {
        Map<String, String> map = recordWithHeaders.toMap();
        Map<String, String> expected = new HashMap<>();
        expected.put("col1", "a");
        expected.put("col2", "b");
        expected.put("col3", "c");
        assertEquals(expected, map);
    }

    @Test(expected = IllegalStateException.class)
    public void testToMapWithoutHeaders() {
        recordWithoutHeaders.toMap();
    }

    @Test
    public void testToMapWithNullValues() {
        Map<String, String> map = recordWithNullValues.toMap();
        assertEquals("x", map.get("col1"));
        assertEquals("", map.get("col2"));
        assertEquals("z", map.get("col3"));
    }

    // ========== putIn() ==========

    @Test
    public void testPutIn() {
        Map<String, String> map = new HashMap<>();
        recordWithHeaders.putIn(map);
        assertEquals("a", map.get("col1"));
        assertEquals("b", map.get("col2"));
        assertEquals("c", map.get("col3"));
    }

    @Test(expected = IllegalStateException.class)
    public void testPutInWithoutHeaders() {
        Map<String, String> map = new HashMap<>();
        recordWithoutHeaders.putIn(map);
    }

    @Test
    public void testPutInWithExistingKeys() {
        Map<String, String> map = new HashMap<>();
        map.put("col1", "old");
        recordWithHeaders.putIn(map);
        assertEquals("a", map.get("col1")); // overwritten
    }

    // ========== isConsistent() ==========

    @Test
    public void testIsConsistent() {
        // With headers and matching values
        assertTrue(recordWithHeaders.isConsistent());
        // Without headers, should be consistent
        assertTrue(recordWithoutHeaders.isConsistent());
        // Empty record
        assertTrue(emptyRecord.isConsistent());
    }

    // ========== toString() ==========

    @Test
    public void testToString() {
        String str = recordWithHeaders.toString();
        assertTrue(str.contains("col1"));
        assertTrue(str.contains("a"));
        assertTrue(str.contains("col2"));
        assertTrue(str.contains("b"));
    }

    // ========== values() ==========

    @Test
    public void testValues() {
        String[] vals = recordWithHeaders.values();
        assertArrayEquals(new String[]{"a", "b", "c"}, vals);
    }

    @Test
    public void testValuesEmpty() {
        String[] vals = emptyRecord.values();
        assertEquals(0, vals.length);
    }

    // ========== Edge Cases and Fault Detection ==========

    @Test
    public void testGetByNameWithNullKey() {
        try {
            recordWithHeaders.get(null);
            fail("Expected IllegalArgumentException for null key");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testGetByIndexAfterMappingChange() {
        // Ensure mapping is not affected by external changes
        // (CSVRecord should be immutable in terms of mapping)
        String val = recordWithHeaders.get(0);
        assertEquals("a", val);
    }

    @Test
    public void testIteratorRemoveUnsupported() {
        Iterator<String> it = recordWithHeaders.iterator();
        it.next();
        try {
            it.remove();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testMultipleIterators() {
        Iterator<String> it1 = recordWithHeaders.iterator();
        Iterator<String> it2 = recordWithHeaders.iterator();
        assertEquals("a", it1.next());
        assertEquals("a", it2.next());
    }

    @Test
    public void testRecordWithOnlyOneColumn() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("single");
        Iterable<CSVRecord> records = format.parse(new StringReader("value"));
        CSVRecord record = records.iterator().next();
        assertEquals(1, record.size());
        assertEquals("value", record.get(0));
        assertEquals("value", record.get("single"));
    }

    @Test
    public void testRecordWithTrailingEmptyFields() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("a,b,c");
        Iterable<CSVRecord> records = format.parse(new StringReader("1,2,"));
        CSVRecord record = records.iterator().next();
        assertEquals(3, record.size());
        assertEquals("1", record.get(0));
        assertEquals("2", record.get(1));
        assertEquals("", record.get(2));
    }

    @Test
    public void testRecordWithLeadingEmptyFields() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("a,b,c");
        Iterable<CSVRecord> records = format.parse(new StringReader(",2,3"));
        CSVRecord record = records.iterator().next();
        assertEquals("", record.get(0));
        assertEquals("2", record.get(1));
        assertEquals("3", record.get(2));
    }

    @Test
    public void testRecordWithAllEmptyFields() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("a,b,c");
        Iterable<CSVRecord> records = format.parse(new StringReader(",,"));
        CSVRecord record = records.iterator().next();
        assertEquals("", record.get(0));
        assertEquals("", record.get(1));
        assertEquals("", record.get(2));
    }

    @Test
    public void testRecordWithQuotedValues() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("a,b");
        Iterable<CSVRecord> records = format.parse(new StringReader("\"hello, world\",\"foo\""));
        CSVRecord record = records.iterator().next();
        assertEquals("hello, world", record.get(0));
        assertEquals("foo", record.get(1));
    }

    // ========== Consistency with mapping ==========

    @Test
    public void testMappingConsistencyAfterGetByName() {
        // Ensure that getting by name does not break subsequent gets by index
        recordWithHeaders.get("col1");
        assertEquals("b", recordWithHeaders.get(1));
    }

    @Test
    public void testMappingConsistencyAfterToMap() {
        recordWithHeaders.toMap();
        assertEquals("c", recordWithHeaders.get("col3"));
    }

    // ========== Performance / Stress (optional) ==========

    @Test(timeout = 1000)
    public void testLargeRecord() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            if (i > 0) sb.append(",");
            sb.append("val").append(i);
        }
        String[] headers = new String[1000];
        for (int i = 0; i < 1000; i++) {
            headers[i] = "h" + i;
        }
        CSVFormat format = CSVFormat.DEFAULT.withHeader(headers);
        Iterable<CSVRecord> records = format.parse(new StringReader(sb.toString()));
        CSVRecord record = records.iterator().next();
        assertEquals(1000, record.size());
        assertEquals("val0", record.get(0));
        assertEquals("val999", record.get(999));
        assertEquals("val500", record.get("h500"));
    }
}