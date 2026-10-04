package org.apache.commons.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class CSVParserTest {

    private CSVFormat formatWithHeader;
    private CSVFormat formatWithoutHeader;
    private CSVFormat formatWithIgnoreEmptyLines;

    @Before
    public void setUp() {
        formatWithHeader = CSVFormat.DEFAULT.withHeader("A", "B", "C");
        formatWithoutHeader = CSVFormat.DEFAULT;
        formatWithIgnoreEmptyLines = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
    }

    // ========== Basic Parsing ==========

    @Test
    public void testParseSimple() throws IOException {
        String csv = "a,b,c\n1,2,3\n4,5,6";
        try (CSVParser parser = CSVParser.parse(csv, formatWithoutHeader)) {
            assertEquals(2, parser.getRecords().size());
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
            assertTrue(headerMap.containsKey("B"));
            assertTrue(headerMap.containsKey("C"));
            assertEquals(0, headerMap.get("A").intValue());
            assertEquals(1, headerMap.get("B").intValue());
            assertEquals(2, headerMap.get("C").intValue());
            assertEquals(2, parser.getRecords().size());
        }
    }

    @Test
    public void testParseEmptyString() throws IOException {
        String csv = "";
        try (CSVParser parser = CSVParser.parse(csv, formatWithoutHeader)) {
            assertEquals(0, parser.getRecords().size());
        }
    }

    @Test(expected = NullPointerException.class)
    public void testParseNullInput() throws IOException {
        CSVParser.parse(null, formatWithoutHeader);
    }

    // ========== Edge Cases: Trailing Newlines and Empty Lines ==========

    @Test
    public void testParseWithTrailingNewline() throws IOException {
        // CSV ending with newline after header and data
        String csv = "A,B,C\n1,2,3\n";
        try (CSVParser parser = CSVParser.parse(csv, formatWithHeader)) {
            assertEquals(1, parser.getRecords().size());
            CSVRecord record = parser.iterator().next();
            assertEquals("1", record.get("A"));
            assertEquals("2", record.get("B"));
            assertEquals("3", record.get("C"));
        }
    }

    @Test
    public void testParseWithTrailingNewlineAndEmptyLine() throws IOException {
        // CSV ending with newline after header, then an empty line
        String csv = "A,B,C\n1,2,3\n\n";
        try (CSVParser parser = CSVParser.parse(csv, formatWithHeader)) {
            assertEquals(1, parser.getRecords().size());
        }
    }

    @Test
    public void testParseWithEmptyLinesIgnored() throws IOException {
        String csv = "a,b,c\n\n1,2,3\n\n4,5,6\n";
        try (CSVParser parser = CSVParser.parse(csv, formatWithIgnoreEmptyLines)) {
            assertEquals(2, parser.getRecords().size());
        }
    }

    @Test
    public void testParseWithEmptyLinesNotIgnored() throws IOException {
        String csv = "a,b,c\n\n1,2,3";
        try (CSVParser parser = CSVParser.parse(csv, formatWithoutHeader)) {
            // Empty line should produce a record with empty fields
            assertEquals(2, parser.getRecords().size());
            CSVRecord first = parser.getRecords().get(0);
            assertEquals(3, first.size());
            assertEquals("", first.get(0));
            assertEquals("", first.get(1));
            assertEquals("", first.get(2));
        }
    }

    // ========== Quoted Fields and Escape Characters ==========

    @Test
    public void testParseQuotedFields() throws IOException {
        String csv = "\"a\",\"b\",\"c\"\n\"1,2\",\"3\",\"4\"";
        try (CSVParser parser = CSVParser.parse(csv, formatWithoutHeader)) {
            assertEquals(1, parser.getRecords().size());
            CSVRecord record = parser.getRecords().get(0);
            assertEquals("1,2", record.get(0));
            assertEquals("3", record.get(1));
            assertEquals("4", record.get(2));
        }
    }

    @Test
    public void testParseEscapedQuote() throws IOException {
        String csv = "\"a\"\"b\",c";
        try (CSVParser parser = CSVParser.parse(csv, formatWithoutHeader)) {
            CSVRecord record = parser.getRecords().get(0);
            assertEquals("a\"b", record.get(0));
            assertEquals("c", record.get(1));
        }
    }

    // ========== Header Map Edge Cases ==========

    @Test
    public void testHeaderMapWithDuplicateHeader() throws IOException {
        // Duplicate header names should be handled (last one wins or exception?)
        // CSVFormat default behavior: duplicate headers cause exception
        String csv = "A,A,C\n1,2,3";
        try {
            CSVParser.parse(csv, CSVFormat.DEFAULT.withHeader("A", "A", "C"));
            fail("Expected IllegalArgumentException for duplicate headers");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testHeaderMapWithNullHeader() throws IOException {
        // Header with null name should be handled
        String csv = ",B,C\n1,2,3";
        try (CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT.withHeader("", "B", "C"))) {
            Map<String, Integer> headerMap = parser.getHeaderMap();
            assertNotNull(headerMap);
            assertTrue(headerMap.containsKey(""));
            assertEquals(0, headerMap.get("").intValue());
        }
    }

    // ========== Iterator and Record Number ==========

    @Test
    public void testIterator() throws IOException {
        String csv = "a,b\n1,2\n3,4";
        try (CSVParser parser = CSVParser.parse(csv, formatWithoutHeader)) {
            Iterator<CSVRecord> iterator = parser.iterator();
            assertTrue(iterator.hasNext());
            CSVRecord first = iterator.next();
            assertEquals(1, first.getRecordNumber());
            assertEquals("1", first.get(0));
            assertTrue(iterator.hasNext());
            CSVRecord second = iterator.next();
            assertEquals(2, second.getRecordNumber());
            assertEquals("3", second.get(0));
            assertFalse(iterator.hasNext());
        }
    }

    @Test
    public void testGetRecordNumberAfterParsing() throws IOException {
        String csv = "a,b\n1,2\n3,4";
        try (CSVParser parser = CSVParser.parse(csv, formatWithoutHeader)) {
            parser.getRecords();
            // After getting all records, iterator should be exhausted
            assertFalse(parser.iterator().hasNext());
        }
    }

    // ========== Bug-Specific Tests (Defects4J Bug 11) ==========

    @Test
    public void testTrailingNewlineAfterHeaderOnly() throws IOException {
        // CSV with only header and trailing newline
        String csv = "A,B,C\n";
        try (CSVParser parser = CSVParser.parse(csv, formatWithHeader)) {
            assertEquals(0, parser.getRecords().size());
            Map<String, Integer> headerMap = parser.getHeaderMap();
            assertNotNull(headerMap);
            assertEquals(3, headerMap.size());
        }
    }

    @Test
    public void testTrailingNewlineAfterHeaderAndDataWithMissingField() throws IOException {
        // CSV with header, data line missing last field, and trailing newline
        String csv = "A,B,C\n1,2\n";
        try (CSVParser parser = CSVParser.parse(csv, formatWithHeader)) {
            assertEquals(1, parser.getRecords().size());
            CSVRecord record = parser.getRecords().get(0);
            assertEquals("1", record.get("A"));
            assertEquals("2", record.get("B"));
            // C should be empty or null depending on format
            assertEquals("", record.get("C"));
        }
    }

    @Test
    public void testMultipleTrailingNewlines() throws IOException {
        String csv = "A,B,C\n1,2,3\n\n\n";
        try (CSVParser parser = CSVParser.parse(csv, formatWithHeader)) {
            assertEquals(1, parser.getRecords().size());
        }
    }

    @Test
    public void testCarriageReturnNewline() throws IOException {
        String csv = "A,B,C\r\n1,2,3\r\n";
        try (CSVParser parser = CSVParser.parse(csv, formatWithHeader)) {
            assertEquals(1, parser.getRecords().size());
        }
    }

    // ========== Additional Edge Cases ==========

    @Test
    public void testParseWithDifferentDelimiter() throws IOException {
        String csv = "a;b;c\n1;2;3";
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(';');
        try (CSVParser parser = CSVParser.parse(csv, format)) {
            assertEquals(1, parser.getRecords().size());
            CSVRecord record = parser.getRecords().get(0);
            assertEquals("1", record.get(0));
            assertEquals("2", record.get(1));
            assertEquals("3", record.get(2));
        }
    }

    @Test
    public void testParseWithCommentMarker() throws IOException {
        String csv = "# comment\na,b\n1,2";
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        try (CSVParser parser = CSVParser.parse(csv, format)) {
            assertEquals(1, parser.getRecords().size());
        }
    }

    @Test
    public void testParseWithSkipHeaderRecord() throws IOException {
        String csv = "A,B,C\n1,2,3";
        CSVFormat format = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        try (CSVParser parser = CSVParser.parse(csv, format)) {
            assertEquals(1, parser.getRecords().size());
            CSVRecord record = parser.getRecords().get(0);
            assertEquals("1", record.get(0));
            assertEquals("2", record.get(1));
            assertEquals("3", record.get(2));
        }
    }

    @Test
    public void testParseWithNullString() throws IOException {
        String csv = "a,b\n1,\"\"";
        CSVFormat format = CSVFormat.DEFAULT.withNullString("");
        try (CSVParser parser = CSVParser.parse(csv, format)) {
            CSVRecord record = parser.getRecords().get(0);
            assertNull(record.get(1));
        }
    }

    @Test
    public void testParseWithIgnoreSurroundingSpaces() throws IOException {
        String csv = " a , b , c \n 1 , 2 , 3 ";
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        try (CSVParser parser = CSVParser.parse(csv, format)) {
            CSVRecord record = parser.getRecords().get(0);
            assertEquals("1", record.get(0));
            assertEquals("2", record.get(1));
            assertEquals("3", record.get(2));
        }
    }

    @Test
    public void testParseWithEscape() throws IOException {
        String csv = "a\\,b,c";
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        try (CSVParser parser = CSVParser.parse(csv, format)) {
            CSVRecord record = parser.getRecords().get(0);
            assertEquals("a,b", record.get(0));
            assertEquals("c", record.get(1));
        }
    }

    @Test
    public void testCloseParser() throws IOException {
        String csv = "a,b\n1,2";
        CSVParser parser = CSVParser.parse(csv, formatWithoutHeader);
        parser.close();
        // After close, iterator should throw IllegalStateException
        try {
            parser.iterator().hasNext();
            fail("Expected IllegalStateException after close");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void testGetRecordsAfterClose() throws IOException {
        String csv = "a,b\n1,2";
        CSVParser parser = CSVParser.parse(csv, formatWithoutHeader);
        parser.close();
        try {
            parser.getRecords();
            fail("Expected IllegalStateException after close");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    // ========== Large Input and Performance (optional) ==========

    @Test
    public void testParseLargeNumberOfRecords() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("a,b,c\n");
        for (int i = 0; i < 1000; i++) {
            sb.append(i).append(",").append(i+1).append(",").append(i+2).append("\n");
        }
        try (CSVParser parser = CSVParser.parse(sb.toString(), formatWithHeader)) {
            assertEquals(1000, parser.getRecords().size());
        }
    }
}