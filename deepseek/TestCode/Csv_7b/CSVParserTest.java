package org.apache.commons.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive JUnit 4 test suite for CSVParser.
 * Designed to achieve high code coverage and detect faults,
 * including the duplicate header bug (Defects4J bug 7).
 */
public class CSVParserTest {

    private CSVFormat formatWithHeader;
    private CSVFormat formatWithoutHeader;
    private CSVFormat formatWithCustomDelimiter;
    private CSVFormat formatWithQuote;

    @Before
    public void setUp() {
        formatWithHeader = CSVFormat.DEFAULT.withHeader("A", "B", "C");
        formatWithoutHeader = CSVFormat.DEFAULT;
        formatWithCustomDelimiter = CSVFormat.DEFAULT.withDelimiter(';');
        formatWithQuote = CSVFormat.DEFAULT.withQuote('\'');
    }

    // ========== Basic Parsing ==========

    @Test
    public void testParseSimpleRecords() throws IOException {
        String csv = "a,b,c\n1,2,3\n4,5,6";
        try (CSVParser parser = CSVParser.parse(csv, formatWithoutHeader)) {
            java.util.List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            assertEquals("1", records.get(0).get(0));
            assertEquals("6", records.get(1).get(2));
        }
    }

    @Test
    public void testParseWithHeader() throws IOException {
        String csv = "A,B,C\n1,2,3\n4,5,6";
        try (CSVParser parser = CSVParser.parse(csv, formatWithHeader)) {
            Map<String, Integer> headerMap = parser.getHeaderMap();
            assertNotNull(headerMap);
            assertEquals(3, headerMap.size());
            assertTrue(headerMap.containsKey("A"));
            assertEquals(0, headerMap.get("A").intValue());
            assertEquals(1, headerMap.get("B").intValue());
            assertEquals(2, headerMap.get("C").intValue());

            java.util.List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            assertEquals("1", records.get(0).get("A"));
            assertEquals("6", records.get(1).get("C"));
        }
    }

    @Test
    public void testParseEmptyInput() throws IOException {
        String csv = "";
        try (CSVParser parser = CSVParser.parse(csv, formatWithoutHeader)) {
            java.util.List<CSVRecord> records = parser.getRecords();
            assertTrue(records.isEmpty());
        }
    }

    @Test
    public void testParseOnlyHeader() throws IOException {
        String csv = "A,B,C";
        try (CSVParser parser = CSVParser.parse(csv, formatWithHeader)) {
            Map<String, Integer> headerMap = parser.getHeaderMap();
            assertNotNull(headerMap);
            assertEquals(3, headerMap.size());
            java.util.List<CSVRecord> records = parser.getRecords();
            assertTrue(records.isEmpty());
        }
    }

    @Test
    public void testParseWithMissingValues() throws IOException {
        String csv = "a,b,c\n1,,3\n4,5,";
        try (CSVParser parser = CSVParser.parse(csv, formatWithoutHeader)) {
            java.util.List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            assertEquals("", records.get(0).get(1));
            assertEquals("", records.get(1).get(2));
        }
    }

    // ========== Custom Delimiter ==========

    @Test
    public void testParseWithCustomDelimiter() throws IOException {
        String csv = "a;b;c\n1;2;3";
        try (CSVParser parser = CSVParser.parse(csv, formatWithCustomDelimiter)) {
            java.util.List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertEquals("1", records.get(0).get(0));
            assertEquals("3", records.get(0).get(2));
        }
    }

    // ========== Quotes and Escapes ==========

    @Test
    public void testParseWithQuotes() throws IOException {
        String csv = "'a','b','c'\n'1','2','3'";
        try (CSVParser parser = CSVParser.parse(csv, formatWithQuote)) {
            java.util.List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertEquals("1", records.get(0).get(0));
            assertEquals("3", records.get(0).get(2));
        }
    }

    @Test
    public void testParseWithEmbeddedQuote() throws IOException {
        String csv = "a,\"b\"\"c\",d\n1,2,3";
        try (CSVParser parser = CSVParser.parse(csv, formatWithoutHeader)) {
            java.util.List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertEquals("b\"c", records.get(0).get(1));
        }
    }

    // ========== Header Map Edge Cases ==========

    @Test
    public void testHeaderMapWithoutHeader() throws IOException {
        String csv = "a,b,c";
        try (CSVParser parser = CSVParser.parse(csv, formatWithoutHeader)) {
            assertNull(parser.getHeaderMap());
        }
    }

    @Test
    public void testHeaderMapWithEmptyHeaderName() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("", "B", "C");
        String csv = ",B,C\n1,2,3";
        try (CSVParser parser = CSVParser.parse(csv, format)) {
            Map<String, Integer> headerMap = parser.getHeaderMap();
            assertNotNull(headerMap);
            // Empty string header should be present
            assertTrue(headerMap.containsKey(""));
            assertEquals(0, headerMap.get("").intValue());
        }
    }

    // ========== Duplicate Headers (Defects4J Bug 7) ==========

    @Test
    public void testDuplicateHeaders() throws IOException {
        // Bug: duplicate headers caused IllegalArgumentException in some versions.
        // Expected: parser should handle duplicates, mapping to the last column index.
        CSVFormat format = CSVFormat.DEFAULT.withHeader("A", "B", "A");
        String csv = "A,B,A\n1,2,3\n4,5,6";
        try (CSVParser parser = CSVParser.parse(csv, format)) {
            Map<String, Integer> headerMap = parser.getHeaderMap();
            assertNotNull(headerMap);
            // The duplicate "A" should map to the last occurrence (index 2)
            assertEquals(2, headerMap.get("A").intValue());
            assertEquals(1, headerMap.get("B").intValue());
            // Verify record access using duplicate header
            java.util.List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            assertEquals("3", records.get(0).get("A")); // should get value from last column
            assertEquals("6", records.get(1).get("A"));
        }
    }

    @Test
    public void testDuplicateHeadersCaseSensitive() throws IOException {
        // Headers are case-sensitive by default
        CSVFormat format = CSVFormat.DEFAULT.withHeader("A", "a", "A");
        String csv = "A,a,A\n1,2,3";
        try (CSVParser parser = CSVParser.parse(csv, format)) {
            Map<String, Integer> headerMap = parser.getHeaderMap();
            assertNotNull(headerMap);
            assertEquals(2, headerMap.get("A").intValue()); // last occurrence
            assertEquals(1, headerMap.get("a").intValue());
        }
    }

    // ========== Iterator Tests ==========

    @Test
    public void testIterator() throws IOException {
        String csv = "a,b\n1,2\n3,4";
        try (CSVParser parser = CSVParser.parse(csv, formatWithoutHeader)) {
            int count = 0;
            for (CSVRecord record : parser) {
                count++;
                if (count == 1) {
                    assertEquals("1", record.get(0));
                } else if (count == 2) {
                    assertEquals("4", record.get(1));
                }
            }
            assertEquals(2, count);
        }
    }

    @Test
    public void testIteratorWithHeader() throws IOException {
        String csv = "X,Y\n1,2\n3,4";
        CSVFormat format = CSVFormat.DEFAULT.withHeader("X", "Y");
        try (CSVParser parser = CSVParser.parse(csv, format)) {
            int count = 0;
            for (CSVRecord record : parser) {
                count++;
                if (count == 1) {
                    assertEquals("1", record.get("X"));
                } else if (count == 2) {
                    assertEquals("4", record.get("Y"));
                }
            }
            assertEquals(2, count);
        }
    }

    // ========== Null and Invalid Input ==========

    @Test(expected = NullPointerException.class)
    public void testParseNullReader() throws IOException {
        CSVParser.parse((java.io.Reader) null, formatWithoutHeader);
    }

    @Test(expected = NullPointerException.class)
    public void testParseNullFormat() throws IOException {
        CSVParser.parse(new StringReader("a,b"), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseWithNullHeaderName() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("A", null, "C");
        CSVParser.parse(new StringReader("A,,C\n1,2,3"), format);
    }

    // ========== Large Input and Performance ==========

    @Test
    public void testLargeCsv() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("a,b,c\n");
        for (int i = 0; i < 1000; i++) {
            sb.append(i).append(",").append(i+1).append(",").append(i+2).append("\n");
        }
        try (CSVParser parser = CSVParser.parse(sb.toString(), formatWithoutHeader)) {
            java.util.List<CSVRecord> records = parser.getRecords();
            assertEquals(1000, records.size());
            assertEquals("999", records.get(999).get(0));
        }
    }

    // ========== Multiple Lines with Quotes ==========

    @Test
    public void testMultiLineQuotedField() throws IOException {
        String csv = "a,\"b\nc\",d\n1,2,3";
        try (CSVParser parser = CSVParser.parse(csv, formatWithoutHeader)) {
            java.util.List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertEquals("b\nc", records.get(0).get(1));
        }
    }

    // ========== Skip Header Record ==========

    @Test
    public void testSkipHeaderRecord() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader().withSkipHeaderRecord(true);
        String csv = "A,B,C\n1,2,3\n4,5,6";
        try (CSVParser parser = CSVParser.parse(csv, format)) {
            // Header is skipped, so first data record is "1,2,3"
            java.util.List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            assertEquals("1", records.get(0).get(0));
            // Header map should still be available
            Map<String, Integer> headerMap = parser.getHeaderMap();
            assertNotNull(headerMap);
            assertEquals(3, headerMap.size());
        }
    }

    // ========== Comment Lines ==========

    @Test
    public void testCommentLines() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        String csv = "# comment\na,b,c\n1,2,3";
        try (CSVParser parser = CSVParser.parse(csv, format)) {
            java.util.List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertEquals("1", records.get(0).get(0));
        }
    }

    // ========== Trim ==========

    @Test
    public void testTrim() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withTrim();
        String csv = " a , b , c \n 1 , 2 , 3 ";
        try (CSVParser parser = CSVParser.parse(csv, format)) {
            java.util.List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertEquals("a", records.get(0).get(0));
            assertEquals("3", records.get(0).get(2));
        }
    }

    // ========== Ignore Empty Lines ==========

    @Test
    public void testIgnoreEmptyLines() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        String csv = "a,b,c\n\n1,2,3\n\n4,5,6";
        try (CSVParser parser = CSVParser.parse(csv, format)) {
            java.util.List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());
            assertEquals("1", records.get(0).get(0));
        }
    }

    // ========== Record Number ==========

    @Test
    public void testRecordNumber() throws IOException {
        String csv = "a,b\n1,2\n3,4";
        try (CSVParser parser = CSVParser.parse(csv, formatWithoutHeader)) {
            java.util.List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.get(0).getRecordNumber());
            assertEquals(2, records.get(1).getRecordNumber());
        }
    }

    // ========== Close and Reuse ==========

    @Test
    public void testParserClosed() throws IOException {
        CSVParser parser = CSVParser.parse("a,b\n1,2", formatWithoutHeader);
        parser.close();
        try {
            parser.getRecords();
            fail("Expected IllegalStateException after close");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    // ========== Null Characters in Input ==========

    @Test
    public void testNullCharacterInField() throws IOException {
        String csv = "a,\0b,c\n1,2,3";
        try (CSVParser parser = CSVParser.parse(csv, formatWithoutHeader)) {
            java.util.List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertEquals("\0b", records.get(0).get(1));
        }
    }
}