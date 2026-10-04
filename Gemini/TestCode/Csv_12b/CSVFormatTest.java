package org.apache.commons.csv;

import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.*;

public class CSVFormatTest {

    @Test
    public void testPredefinedFormats() {
        assertNotNull(CSVFormat.DEFAULT);
        assertNotNull(CSVFormat.EXCEL);
        assertNotNull(CSVFormat.RFC4180);
        assertNotNull(CSVFormat.TDF);
        assertNotNull(CSVFormat.MYSQL);
    }

    @Test
    public void testNewFormat() {
        CSVFormat format = CSVFormat.newFormat(';');
        assertEquals(';', format.getDelimiter().charValue());
        assertNull(format.getQuoteChar());
        assertNull(format.getCommentMarker());
        assertNull(format.getEscape());
        assertFalse(format.getIgnoreSurroundingSpaces());
        assertFalse(format.getIgnoreEmptyLines());
        assertNull(format.getRecordSeparator());
        assertNull(format.getNullString());
        assertNull(format.getHeader());
    }

    @Test
    public void testWithDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter('|');
        assertEquals('|', format.getDelimiter().charValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiterLF() {
        CSVFormat.DEFAULT.withDelimiter('\n');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiterCR() {
        CSVFormat.DEFAULT.withDelimiter('\r');
    }

    @Test
    public void testWithQuoteChar() {
        CSVFormat format = CSVFormat.DEFAULT.withQuoteChar('\'');
        assertEquals('\'', format.getQuoteChar().charValue());

        CSVFormat formatNull = CSVFormat.DEFAULT.withQuoteChar((Character) null);
        assertNull(formatNull.getQuoteChar());

        CSVFormat formatStrNull = CSVFormat.DEFAULT.withQuote((String) null);
        assertNull(formatStrNull.getQuoteChar());

        CSVFormat formatStr = CSVFormat.DEFAULT.withQuote("'");
        assertEquals('\'', formatStr.getQuoteChar().charValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteCharLF() {
        CSVFormat.DEFAULT.withQuoteChar('\n');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteCharCR() {
        CSVFormat.DEFAULT.withQuoteChar('\r');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteEmptyString() {
        CSVFormat.DEFAULT.withQuote("");
    }

    @Test
    public void testWithCommentMarker() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        assertEquals('#', format.getCommentMarker().charValue());

        CSVFormat formatNull = CSVFormat.DEFAULT.withCommentMarker(null);
        assertNull(formatNull.getCommentMarker());

        CSVFormat formatStr = CSVFormat.DEFAULT.withComment_("#");
        assertEquals('#', formatStr.getCommentMarker().charValue());

        CSVFormat formatStrNull = CSVFormat.DEFAULT.withComment_((String) null);
        assertNull(formatStrNull.getCommentMarker());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentMarkerLF() {
        CSVFormat.DEFAULT.withCommentMarker('\n');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentMarkerCR() {
        CSVFormat.DEFAULT.withCommentMarker('\r');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentDelimiterOverlap() {
        CSVFormat.DEFAULT.withDelimiter(',').withCommentMarker(',');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentQuoteOverlap() {
        CSVFormat.DEFAULT.withQuoteChar('"').withCommentMarker('"');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentEscapeOverlap() {
        CSVFormat.DEFAULT.withEscape('\\').withCommentMarker('\\');
    }

    @Test
    public void testWithEscape() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        assertEquals('\\', format.getEscape().charValue());

        CSVFormat formatNull = CSVFormat.DEFAULT.withEscape(null);
        assertNull(formatNull.getEscape());

        CSVFormat formatStr = CSVFormat.DEFAULT.withEscape("\\");
        assertEquals('\\', formatStr.getEscape().charValue());

        CSVFormat formatStrNull = CSVFormat.DEFAULT.withEscape((String) null);
        assertNull(formatStrNull.getEscape());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeLF() {
        CSVFormat.DEFAULT.withEscape('\n');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeCR() {
        CSVFormat.DEFAULT.withEscape('\r');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeDelimiterOverlap() {
        CSVFormat.DEFAULT.withDelimiter(',').withEscape(',');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeQuoteOverlap() {
        CSVFormat.DEFAULT.withQuoteChar('"').withEscape('"');
    }

    @Test
    public void testWithIgnoreSurroundingSpaces() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        assertTrue(format.getIgnoreSurroundingSpaces());

        CSVFormat formatNoArg = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces();
        assertTrue(formatNoArg.getIgnoreSurroundingSpaces());
    }

    @Test
    public void testWithIgnoreEmptyLines() {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(false);
        assertFalse(format.getIgnoreEmptyLines());

        CSVFormat formatNoArg = CSVFormat.DEFAULT.withIgnoreEmptyLines();
        assertTrue(formatNoArg.getIgnoreEmptyLines());
    }

    @Test
    public void testWithRecordSeparator() {
        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator("\n");
        assertEquals("\n", format.getRecordSeparator());

        CSVFormat formatChar = CSVFormat.DEFAULT.withRecordSeparator('\n');
        assertEquals("\n", formatChar.getRecordSeparator());

        CSVFormat formatNull = CSVFormat.DEFAULT.withRecordSeparator((String) null);
        assertNull(formatNull.getRecordSeparator());
    }

    @Test
    public void testWithNullString() {
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        assertEquals("NULL", format.getNullString());
    }

    @Test
    public void testWithHeader() {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("Col1", "Col2");
        assertArrayEquals(new String[]{"Col1", "Col2"}, format.getHeader());

        CSVFormat formatStream = CSVFormat.DEFAULT.withHeader(new String[]{"A", "B"});
        assertArrayEquals(new String[]{"A", "B"}, formatStream.getHeader());

        CSVFormat formatClass = CSVFormat.DEFAULT.withHeader(String.class);
        assertNotNull(formatClass.getHeader());
    }

    @Test
    public void testWithSkipHeaderRecord() {
        CSVFormat format = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        assertTrue(format.getSkipHeaderRecord());

        CSVFormat formatNoArg = CSVFormat.DEFAULT.withSkipHeaderRecord();
        assertTrue(formatNoArg.getSkipHeaderRecord());
    }

    @Test
    public void testParse() throws IOException {
        Reader reader = new StringReader("a,b,c\n1,2,3");
        CSVParser parser = CSVFormat.DEFAULT.parse(reader);
        assertNotNull(parser);
    }

    @Test
    public void testFormatRecord() {
        String formatted = CSVFormat.DEFAULT.format("a", "b", "c");
        assertEquals("a,b,c", formatted);
    }

    @Test
    public void testPrintObject() throws IOException {
        Appendable out = new StringWriter();
        CSVPrinter printer = CSVFormat.DEFAULT.print(out);
        assertNotNull(printer);
    }

    @Test
    public void testToString() {
        String str = CSVFormat.DEFAULT.toString();
        assertNotNull(str);
    }

    @Test
    public void testEqualsAndHashCode() {
        CSVFormat f1 = CSVFormat.DEFAULT;
        CSVFormat f2 = CSVFormat.DEFAULT;
        CSVFormat f3 = CSVFormat.EXCEL;

        assertEquals(f1, f1);
        assertEquals(f1, f2);
        assertEquals(f1.hashCode(), f2.hashCode());
        assertNotEquals(f1, f3);
        assertNotEquals(f1, null);
        assertNotEquals(f1, "SomeString");

        CSVFormat f4 = CSVFormat.DEFAULT.withDelimiter(';');
        assertNotEquals(f1, f4);

        CSVFormat f5 = CSVFormat.DEFAULT.withQuoteChar(null);
        CSVFormat f6 = CSVFormat.DEFAULT.withQuoteChar('\'');
        assertNotEquals(f5, f6);
        assertNotEquals(f6, f5);

        CSVFormat f7 = CSVFormat.DEFAULT.withCommentMarker(null);
        CSVFormat f8 = CSVFormat.DEFAULT.withCommentMarker('#');
        assertNotEquals(f7, f8);
        assertNotEquals(f8, f7);

        CSVFormat f9 = CSVFormat.DEFAULT.withEscape(null);
        CSVFormat f10 = CSVFormat.DEFAULT.withEscape('\\');
        assertNotEquals(f9, f10);
        assertNotEquals(f10, f9);

        CSVFormat f11 = CSVFormat.DEFAULT.withRecordSeparator(null);
        CSVFormat f12 = CSVFormat.DEFAULT.withRecordSeparator("\n");
        assertNotEquals(f11, f12);
        assertNotEquals(f12, f11);

        CSVFormat f13 = CSVFormat.DEFAULT.withNullString(null);
        CSVFormat f14 = CSVFormat.DEFAULT.withNullString("NULL");
        assertNotEquals(f13, f14);
        assertNotEquals(f14, f13);
    }

    @Test
    public void testValidate() {
        // Just calling validate through constructor/with methods which invoke it internally
        try {
            CSVFormat.DEFAULT.withDelimiter('"');
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }
}