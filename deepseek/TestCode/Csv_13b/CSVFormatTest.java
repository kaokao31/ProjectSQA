package org.apache.commons.csv;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Test;

public class CSVFormatTest {

    @Test
    public void testDefaultFormatFields() {
        CSVFormat format = CSVFormat.DEFAULT;
        assertEquals(',', format.getDelimiter());
        assertEquals(Character.valueOf('"'), format.getQuoteChar());
        assertEquals(QuotePolicy.MINIMAL, format.getQuotePolicy());
        assertNull(format.getCommentMarker());
        assertNull(format.getEscapeCharacter());
        assertTrue(format.getIgnoreEmptyLines());
        assertFalse(format.getIgnoreSurroundingSpaces());
        assertEquals("\r\n", format.getRecordSeparator());
        assertNull(format.getNullString());
        assertNull(format.getHeader());
        assertFalse(format.getSkipHeaderRecord());
        assertFalse(format.getAllowMissingColumnNames());
        assertFalse(format.getTrailingDelimiter());
        assertFalse(format.getTrim());
    }

    @Test
    public void testWithDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(';');
        assertEquals(';', format.getDelimiter());
        assertEquals(',', CSVFormat.DEFAULT.getDelimiter());
    }

    @Test
    public void testWithDelimiterInvalidNullChar() {
        try {
            CSVFormat.DEFAULT.withDelimiter('\0');
            fail("Expected IllegalArgumentException for null delimiter");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testWithDelimiterInvalidLineBreak() {
        try {
            CSVFormat.DEFAULT.withDelimiter('\n');
            fail("Expected IllegalArgumentException for line break delimiter");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testWithDelimiterInvalidQuoteChar() {
        try {
            CSVFormat.DEFAULT.withDelimiter('"');
            fail("Expected IllegalArgumentException for quote delimiter");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testWithQuote() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('\'');
        assertEquals(Character.valueOf('\''), format.getQuoteChar());
        assertEquals(Character.valueOf('"'), CSVFormat.DEFAULT.getQuoteChar());
    }

    @Test
    public void testWithQuoteNull() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote(null);
        assertNull(format.getQuoteChar());
        assertNotNull(CSVFormat.DEFAULT.getQuoteChar());
    }

    @Test
    public void testWithQuotePolicy() {
        CSVFormat format = CSVFormat.DEFAULT.withQuotePolicy(QuotePolicy.ALL);
        assertEquals(QuotePolicy.ALL, format.getQuotePolicy());
        assertEquals(QuotePolicy.MINIMAL, CSVFormat.DEFAULT.getQuotePolicy());
    }

    @Test
    public void testWithCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertEquals(Character.valueOf('#'), format.getCommentMarker());
        assertNull(CSVFormat.DEFAULT.getCommentMarker());
    }

    @Test
    public void testWithCommentMarkerNull() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker(null);
        assertNull(format.getCommentMarker());
    }

    @Test
    public void testWithEscape() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        assertEquals(Character.valueOf('\\'), format.getEscapeCharacter());
        assertNull(CSVFormat.DEFAULT.getEscapeCharacter());
    }

    @Test
    public void testWithEscapeNull() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape(null);
        assertNull(format.getEscapeCharacter());
    }

    @Test
    public void testWithIgnoreEmptyLines() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(false);
        assertFalse(format.getIgnoreEmptyLines());
        assertTrue(CSVFormat.DEFAULT.getIgnoreEmptyLines());
    }

    @Test
    public void testWithIgnoreSurroundingSpaces() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        assertTrue(format.getIgnoreSurroundingSpaces());
        assertFalse(CSVFormat.DEFAULT.getIgnoreSurroundingSpaces());
    }

    @Test
    public void testWithRecordSeparator() {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator("\n");
        assertEquals("\n", format.getRecordSeparator());
        assertEquals("\r\n", CSVFormat.DEFAULT.getRecordSeparator());
    }

    @Test
    public void testWithRecordSeparatorNull() {
        try {
            CSVFormat.DEFAULT.withRecordSeparator(null);
            fail("Expected IllegalArgumentException for null record separator");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testWithRecordSeparatorEmptyString() {
        try {
            CSVFormat.DEFAULT.withRecordSeparator("");
            fail("Expected IllegalArgumentException for empty record separator");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testWithNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("N/A");
        assertEquals("N/A", format.getNullString());
        assertNull(CSVFormat.DEFAULT.getNullString());
    }

    @Test
    public void testWithNullStringNull() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString(null);
        assertNull(format.getNullString());
    }

    @Test
    public void testWithHeader() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("A", "B");
        assertArrayEquals(new String[]{"A", "B"}, format.getHeader());
        assertNull(CSVFormat.DEFAULT.getHeader());
    }

    @Test
    public void testWithHeaderNull() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader((String[]) null);
        assertNull(format.getHeader());
        assertNull(CSVFormat.DEFAULT.getHeader());
    }

    @Test
    public void testWithHeaderEmpty() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader();
        assertNotNull(format.getHeader());
        assertEquals(0, format.getHeader().length);
        assertNull(CSVFormat.DEFAULT.getHeader());
    }

    @Test
    public void testWithHeaderWithNullEntries() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("A", null, "C");
        assertArrayEquals(new String[]{"A", null, "C"}, format.getHeader());
    }

    @Test
    public void testWithSkipHeaderRecord() {
        CSVFormat format = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        assertTrue(format.getSkipHeaderRecord());
        assertFalse(CSVFormat.DEFAULT.getSkipHeaderRecord());
    }

    @Test
    public void testWithAllowMissingColumnNames() {
        CSVFormat format = CSVFormat.DEFAULT.withAllowMissingColumnNames(true);
        assertTrue(format.getAllowMissingColumnNames());
        assertFalse(CSVFormat.DEFAULT.getAllowMissingColumnNames());
    }

    @Test
    public void testWithTrailingDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withTrailingDelimiter(true);
        assertTrue(format.getTrailingDelimiter());
        assertFalse(CSVFormat.DEFAULT.getTrailingDelimiter());
    }

    @Test
    public void testWithTrim() {
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        assertTrue(format.getTrim());
        assertFalse(CSVFormat.DEFAULT.getTrim());
    }

    @Test
    public void testEqualsAndHashCode() {
        CSVFormat fmt1 = CSVFormat.DEFAULT.withDelimiter(';');
        CSVFormat fmt2 = CSVFormat.DEFAULT.withDelimiter(';');
        assertEquals(fmt1, fmt2);
        assertEquals(fmt1.hashCode(), fmt2.hashCode());
    }

    @Test
    public void testNotEquals() {
        CSVFormat fmt1 = CSVFormat.DEFAULT;
        CSVFormat fmt2 = CSVFormat.DEFAULT.withDelimiter(';');
        assertFalse(fmt1.equals(fmt2));
    }

    @Test
    public void testFormat() {
        CSVFormat format = CSVFormat.DEFAULT;
        String result = format.format("a", "b", "c");
        assertEquals("a,b,c\r\n", result);
    }

    @Test
    public void testFormatTrailingDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withTrailingDelimiter(true);
        String result = format.format("a", "b");
        assertEquals("a,b,\r\n", result);
    }

    @Test
    public void testFormatTrim() {
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        String result = format.format(" a ", " b ");
        assertEquals("a,b\r\n", result);
    }

    @Test
    public void testParse() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT;
        CSVParser parser = format.parse(new StringReader("a,b,c\r\n"));
        assertNotNull(parser);
        parser.close();
    }

    @Test
    public void testConstantsNotNull() {
        assertNotNull(CSVFormat.DEFAULT);
        assertNotNull(CSVFormat.EXCEL);
        assertNotNull(CSVFormat.RFC4180);
        assertNotNull(CSVFormat.TDF);
        assertNotNull(CSVFormat.MYSQL);
    }

    @Test
    public void testExcelFormatFields() {
        CSVFormat format = CSVFormat.EXCEL;
        assertEquals(',', format.getDelimiter());
        assertEquals(Character.valueOf('"'), format.getQuoteChar());
        assertFalse(format.getIgnoreEmptyLines());
        assertEquals("\r\n", format.getRecordSeparator());
    }

    @Test
    public void testRFC4180FormatFields() {
        CSVFormat format = CSVFormat.RFC4180;
        assertEquals(',', format.getDelimiter());
        assertEquals(Character.valueOf('"'), format.getQuoteChar());
        assertFalse(format.getIgnoreEmptyLines());
        assertEquals("\r\n", format.getRecordSeparator());
    }

    @Test
    public void testTDFFormatFields() {
        CSVFormat format = CSVFormat.TDF;
        assertEquals('\t', format.getDelimiter());
        assertEquals(Character.valueOf('"'), format.getQuoteChar());
        assertTrue(format.getIgnoreEmptyLines());
    }

    @Test
    public void testMYSQLFormatFields() {
        CSVFormat format = CSVFormat.MYSQL;
        assertEquals('\t', format.getDelimiter());
        assertEquals(Character.valueOf('"'), format.getQuoteChar());
        assertTrue(format.getIgnoreEmptyLines());
        assertTrue(format.getIgnoreSurroundingSpaces());
        assertEquals("\n", format.getRecordSeparator());
    }
}