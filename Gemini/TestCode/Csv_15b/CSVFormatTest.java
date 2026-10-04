package org.apache.commons.csv;

import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.*;

public class CSVFormatTest {

    @Test
    public void testStandardConstants() {
        assertNotNull(CSVFormat.DEFAULT);
        assertNotNull(CSVFormat.RFC4180);
        assertNotNull(CSVFormat.EXCEL);
        assertNotNull(CSVFormat.TDF);
        assertNotNull(CSVFormat.MYSQL);
        assertNotNull(CSVFormat.MONGODB_CSV);
        assertNotNull(CSVFormat.MONGODB_TSV);
    }

    @Test
    public void testValueOf() {
        assertEquals(CSVFormat.DEFAULT, CSVFormat.valueOf("DEFAULT"));
        assertEquals(CSVFormat.RFC4180, CSVFormat.valueOf("RFC4180"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOfInvalid() {
        CSVFormat.valueOf("INVALID_FORMAT_NAME");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOfNull() {
        CSVFormat.valueOf(null);
    }

    @Test
    public void testBuilderAndGetters() {
        CSVFormat format = CSVFormat.DEFAULT
                .withDelimiter(';')
                .withQuote('"')
                .withQuoteMode(QuoteMode.ALL)
                .withCommentMarker('#')
                .withEscape('\\')
                .withIgnoreSurroundingSpaces(true)
                .withIgnoreEmptyLines(false)
                .withRecordSeparator("\r\n")
                .withNullString("NULL")
                .withHeaderComments("Comment 1", "Comment 2")
                .withHeader("Col1", "Col2")
                .withSkipHeaderRecord(true)
                .withAllowMissingColumnNames(true)
                .withIgnoreHeaderCase(true)
                .withTrim(true)
                .withTrailingDelimiter(true);

        assertEquals(';', format.getDelimiter());
        assertEquals(Character.valueOf('"'), format.getQuoteCharacter());
        assertEquals(QuoteMode.ALL, format.getQuoteMode());
        assertEquals(Character.valueOf('#'), format.getCommentMarker());
        assertEquals(Character.valueOf('\\'), format.getEscapeCharacter());
        assertTrue(format.getIgnoreSurroundingSpaces());
        assertFalse(format.getIgnoreEmptyLines());
        assertEquals("\r\n", format.getRecordSeparator());
        assertEquals("NULL", format.getNullString());
        assertArrayEquals(new Object[]{"Comment 1", "Comment 2"}, format.getHeaderComments());
        assertArrayEquals(new String[]{"Col1", "Col2"}, format.getHeader());
        assertTrue(format.getSkipHeaderRecord());
        assertTrue(format.getAllowMissingColumnNames());
        assertTrue(format.getGetHeaderCaseIgnored());
        assertTrue(format.getTrim());
        assertTrue(format.getTrailingDelimiter());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiterLineBreak() {
        CSVFormat.DEFAULT.withDelimiter('\n');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeLineBreak() {
        CSVFormat.DEFAULT.withEscape('\r');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentMarkerLineBreak() {
        CSVFormat.DEFAULT.withCommentMarker('\n');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteCharLineBreak() {
        CSVFormat.DEFAULT.withQuote('\n');
    }

    @Test
    public void testIsLineBreak() {
        assertTrue(CSVFormat.isLineBreak('\n'));
        assertTrue(CSVFormat.isLineBreak('\r'));
        assertFalse(CSVFormat.isLineBreak('a'));
    }

    @Test
    public void testFormatMethods() {
        CSVFormat format = CSVFormat.DEFAULT;
        assertEquals("a,b,c", format.format("a", "b", "c"));
    }

    @Test
    public void testParse() throws IOException {
        CSVParser parser = CSVFormat.DEFAULT.parse(new StringReader("a,b,c\n1,2,3"));
        assertNotNull(parser);
        parser.close();
    }

    @Test
    public void testPrintObject() {
        StringBuilder sb = new StringBuilder();
        CSVFormat.DEFAULT.print(sb);
        assertNotNull(sb.toString());
    }

    @Test
    public void testPrintAppendable() throws IOException {
        StringBuilder sb = new StringBuilder();
        CSVFormat.DEFAULT.print("value", sb, true);
        assertEquals("\"value\"", sb.toString());
    }

    @Test
    public void testPrintReader() throws IOException {
        StringBuilder sb = new StringBuilder();
        Reader reader = new StringReader("test");
        CSVFormat.DEFAULT.print(reader, sb, true);
        assertEquals("test", sb.toString());
    }

    @Test
    public void testToString() {
        String str = CSVFormat.DEFAULT.toString();
        assertNotNull(str);
        assertFalse(str.isEmpty());
    }

    @Test
    public void testEqualsAndHashCode() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        CSVFormat f2 = CSVFormat.DEFAULT;
        CSVFormat f3 = CSVFormat.RFC4180;

        assertEquals(f1, f2);
        assertEquals(f1.hashCode(), f2.hashCode());
        assertNotEquals(f1, f3);
        assertNotEquals(f1, null);
        assertNotEquals(f1, "Not a CSVFormat");
    }

    @Test
    public void testValidation() {
        // Test combinations that might trigger validation exceptions
        try {
            CSVFormat.DEFAULT.withQuote(null).withEscape('\\');
        } catch (Exception e) {
            // Depending on state
        }
    }
}