package org.apache.commons.csv;

import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.*;

public class CSVFormatTest {

    @Test
    public void testDefaultValues() {
        CSVFormat format = CSVFormat.DEFAULT;
        assertEquals(',', format.getDelimiter());
        assertEquals('"', format.getQuoteCharacter().charValue());
        assertNull(format.getQuoteMode());
        assertNull(format.getCommentMarker());
        assertNull(format.getEscapeCharacter());
        assertFalse(format.getIgnoreSurroundingSpaces());
        assertFalse(format.getIgnoreEmptyLines());
        assertEquals("\r\n", format.getRecordSeparator());
        assertNull(format.getNullString());
        assertNull(format.getHeaderComments());
        assertNull(format.getHeader());
        assertFalse(format.getSkipHeaderRecord());
        assertFalse(format.getAllowMissingColumnNames());
        assertFalse(format.getIgnoreHeaderCase());
    }

    @Test
    public void testStandardPredefinedFormats() {
        assertNotNull(CSVFormat.EXCEL);
        assertNotNull(CSVFormat.RFC4180);
        assertNotNull(CSVFormat.MYSQL);
        assertNotNull(CSVFormat.TDF);
    }

    @Test
    public void testWithDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(';');
        assertEquals(';', format.getDelimiter());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiterInvalid() {
        CSVFormat.DEFAULT.withDelimiter('\n');
    }

    @Test
    public void testWithQuoteChar() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('\'');
        assertEquals('\'', format.getQuoteCharacter().charValue());

        CSVFormat formatNull = CSVFormat.DEFAULT.withQuote(null);
        assertNull(formatNull.getQuoteCharacter());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteCharInvalid() {
        CSVFormat.DEFAULT.withQuote('\r');
    }

    @Test
    public void testWithCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertEquals('#', format.getCommentMarker().charValue());

        CSVFormat formatNull = CSVFormat.DEFAULT.withCommentMarker(null);
        assertNull(formatNull.getCommentMarker());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentMarkerInvalid() {
        CSVFormat.DEFAULT.withCommentMarker('\n');
    }

    @Test
    public void testWithEscape() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        assertEquals('\\', format.getEscapeCharacter().charValue());

        CSVFormat formatNull = CSVFormat.DEFAULT.withEscape(null);
        assertNull(formatNull.getEscapeCharacter());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeInvalid() {
        CSVFormat.DEFAULT.withEscape('\n');
    }

    @Test
    public void testWithRecordSeparator() {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator("\n");
        assertEquals("\n", format.getRecordSeparator());

        CSVFormat formatChar = CSVFormat.DEFAULT.withRecordSeparator(':');
        assertEquals(":", format.getRecordSeparator());

        CSVFormat formatNull = CSVFormat.DEFAULT.withRecordSeparator(null);
        assertNull(formatNull.getRecordSeparator());
    }

    @Test
    public void testWithNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        assertEquals("NULL", format.getNullString());
    }

    @Test
    public void testWithHeaderComments() {
        CSVFormat format = CSVFormat.DEFAULT.withHeaderComments("Comment 1", "Comment 2");
        assertArrayEquals(new Object[]{"Comment 1", "Comment 2"}, format.getHeaderComments());
    }

    @Test
    public void testWithHeader() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("Col1", "Col2");
        assertArrayEquals(new String[]{"Col1", "Col2"}, format.getHeader());
    }

    @Test
    public void testWithSkipHeaderRecord() {
        CSVFormat format = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        assertTrue(format.getSkipHeaderRecord());
    }

    @Test
    public void testWithAllowMissingColumnNames() {
        CSVFormat format = CSVFormat.DEFAULT.withAllowMissingColumnNames(true);
        assertTrue(format.getAllowMissingColumnNames());
    }

    @Test
    public void testWithIgnoreHeaderCase() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreHeaderCase(true);
        assertTrue(format.getIgnoreHeaderCase());
    }

    @Test
    public void testWithIgnoreSurroundingSpaces() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        assertTrue(format.getIgnoreSurroundingSpaces());
    }

    @Test
    public void testWithIgnoreEmptyLines() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        assertTrue(format.getIgnoreEmptyLines());
    }

    @Test
    public void testWithQuoteMode() {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);
        assertEquals(QuoteMode.ALL, format.getQuoteMode());
    }

    @Test
    public void testParse() throws IOException {
        Reader reader = new StringReader("A,B,C\n1,2,3");
        CSVParser parser = CSVFormat.DEFAULT.withHeader("Col1", "Col2", "Col3").parse(reader);
        assertNotNull(parser);
    }

    @Test
    public void testFormat() {
        String formatted = CSVFormat.DEFAULT.format("a", "b", "c");
        assertEquals("a,b,c", formatted);
    }

    @Test
    public void testPrint() throws IOException {
        Appendable out = new StringWriter();
        CSVPrinter printer = CSVFormat.DEFAULT.print(out);
        assertNotNull(printer);
    }

    @Test
    public void testEqualsAndHashCode() {
        CSVFormat format1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format2 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat format3 = CSVFormat.DEFAULT.withDelimiter(';');

        assertEquals(format1, format2);
        assertEquals(format1.hashCode(), format2.hashCode());
        assertNotEquals(format1, format3);
        assertNotEquals(format1, null);
        assertNotEquals(format1, "SomeString");
        assertEquals(format1, format1);
    }

    @Test
    public void testToString() {
        String str = CSVFormat.DEFAULT.toString();
        assertNotNull(str);
        assertTrue(str.contains("Delimiter"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateDuplicateHeaderNames() {
        CSVFormat.DEFAULT.withHeader("A", "A");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateConflictingDelimiters() {
        CSVFormat.DEFAULT.withDelimiter('"');
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testValidateConflictingEscape() {
        CSVFormat.DEFAULT.withEscape('"').withQuote('"');
    }
}