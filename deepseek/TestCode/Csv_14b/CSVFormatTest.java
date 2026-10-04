package org.apache.commons.csv;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class CSVFormatTest {

    private CSVFormat format;

    @Before
    public void setUp() {
        format = CSVFormat.newFormat(',');
    }

    @Test
    public void testDefaultFormat() {
        assertEquals(',', format.getDelimiter());
        assertEquals(Character.valueOf('"'), format.getQuoteChar());
        assertNull(format.getEscapeChar());
        assertTrue(format.isIgnoreEmptyLines());
        assertEquals("\r\n", format.getRecordSeparator());
        assertFalse(format.isSkipHeaderRecord());
        assertFalse(format.isIgnoreSurroundingSpaces());
        assertNull(format.getNullString());
        assertNull(format.getCommentMarker());
        assertFalse(format.isAllowMissingColumnNames());
        assertFalse(format.isTrim());
        assertFalse(format.isHeader());
    }

    @Test
    public void testDelimiter() {
        CSVFormat newFormat = format.withDelimiter(';');
        assertEquals(';', newFormat.getDelimiter());
        assertEquals(',', format.getDelimiter());
        assertNotSame(format, newFormat);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDelimiterInvalid() {
        format.withDelimiter('a');
    }

    @Test
    public void testQuoteChar() {
        CSVFormat nullQuote = format.withQuoteChar(null);
        assertNull(nullQuote.getQuoteChar());
        CSVFormat withQuote = format.withQuoteChar('\'');
        assertEquals(Character.valueOf('\''), withQuote.getQuoteChar());
        assertEquals(Character.valueOf('"'), format.getQuoteChar());
        assertNotSame(format, nullQuote);
        assertNotSame(format, withQuote);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQuoteCharInvalid() {
        format.withQuoteChar('a');
    }

    @Test
    public void testEscapeChar() {
        CSVFormat nullEscape = format.withEscapeChar(null);
        assertNull(nullEscape.getEscapeChar());
        CSVFormat withEscape = format.withEscapeChar('\\');
        assertEquals(Character.valueOf('\\'), withEscape.getEscapeChar());
        assertNull(format.getEscapeChar());
        assertNotSame(format, nullEscape);
        assertNotSame(format, withEscape);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEscapeCharInvalid() {
        format.withEscapeChar('a');
    }

    @Test
    public void testIgnoreEmptyLines() {
        CSVFormat ignoreFalse = format.withIgnoreEmptyLines(false);
        assertFalse(ignoreFalse.isIgnoreEmptyLines());
        CSVFormat ignoreTrue = format.withIgnoreEmptyLines(true);
        assertTrue(ignoreTrue.isIgnoreEmptyLines());
        assertTrue(format.isIgnoreEmptyLines());
        assertNotSame(format, ignoreFalse);
        assertNotSame(format, ignoreTrue);
    }

    @Test
    public void testRecordSeparator() {
        CSVFormat withSep = format.withRecordSeparator("\n");
        assertEquals("\n", withSep.getRecordSeparator());
        assertEquals("\r\n", format.getRecordSeparator());
        assertNotSame(format, withSep);
    }

    @Test(expected = NullPointerException.class)
    public void testRecordSeparatorNull() {
        format.withRecordSeparator(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRecordSeparatorEmpty() {
        format.withRecordSeparator("");
    }

    @Test
    public void testSkipHeaderRecord() {
        CSVFormat skipTrue = format.withSkipHeaderRecord(true);
        assertTrue(skipTrue.isSkipHeaderRecord());
        CSVFormat skipFalse = format.withSkipHeaderRecord(false);
        assertFalse(skipFalse.isSkipHeaderRecord());
        assertFalse(format.isSkipHeaderRecord());
        assertNotSame(format, skipTrue);
        assertNotSame(format, skipFalse);
    }

    @Test
    public void testIgnoreSurroundingSpaces() {
        CSVFormat ignoreTrue = format.withIgnoreSurroundingSpaces(true);
        assertTrue(ignoreTrue.isIgnoreSurroundingSpaces());
        CSVFormat ignoreFalse = format.withIgnoreSurroundingSpaces(false);
        assertFalse(ignoreFalse.isIgnoreSurroundingSpaces());
        assertFalse(format.isIgnoreSurroundingSpaces());
        assertNotSame(format, ignoreTrue);
        assertNotSame(format, ignoreFalse);
    }

    @Test
    public void testNullString() {
        CSVFormat nullString = format.withNullString(null);
        assertNull(nullString.getNullString());
        CSVFormat withNull = format.withNullString("N/A");
        assertEquals("N/A", withNull.getNullString());
        assertNull(format.getNullString());
        assertNotSame(format, nullString);
        assertNotSame(format, withNull);
    }

    @Test
    public void testCommentMarker() {
        CSVFormat nullMarker = format.withCommentMarker(null);
        assertNull(nullMarker.getCommentMarker());
        CSVFormat withMarker = format.withCommentMarker('#');
        assertEquals(Character.valueOf('#'), withMarker.getCommentMarker());
        assertNull(format.getCommentMarker());
        assertNotSame(format, nullMarker);
        assertNotSame(format, withMarker);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCommentMarkerInvalid() {
        format.withCommentMarker('a');
    }

    @Test
    public void testAllowMissingColumnNames() {
        CSVFormat allowTrue = format.withAllowMissingColumnNames(true);
        assertTrue(allowTrue.isAllowMissingColumnNames());
        CSVFormat allowFalse = format.withAllowMissingColumnNames(false);
        assertFalse(allowFalse.isAllowMissingColumnNames());
        assertFalse(format.isAllowMissingColumnNames());
        assertNotSame(format, allowTrue);
        assertNotSame(format, allowFalse);
    }

    @Test
    public void testTrim() {
        CSVFormat trimTrue = format.withTrim(true);
        assertTrue(trimTrue.isTrim());
        CSVFormat trimFalse = format.withTrim(false);
        assertFalse(trimFalse.isTrim());
        assertFalse(format.isTrim());
        assertNotSame(format, trimTrue);
        assertNotSame(format, trimFalse);
    }

    @Test
    public void testHeader() {
        CSVFormat custom = format
            .withDelimiter(';')
            .withQuoteChar(null)
            .withEscapeChar('\\')
            .withIgnoreEmptyLines(false)
            .withRecordSeparator("\n")
            .withSkipHeaderRecord(true)
            .withIgnoreSurroundingSpaces(true)
            .withNullString("NULL")
            .withCommentMarker('#')
            .withAllowMissingColumnNames(true)
            .withTrim(true);
        CSVFormat headerFormat = custom.withHeader();
        assertTrue(headerFormat.isHeader());
        assertEquals(';', headerFormat.getDelimiter());
        assertNull(headerFormat.getQuoteChar());
        assertEquals(Character.valueOf('\\'), headerFormat.getEscapeChar());
        assertFalse(headerFormat.isIgnoreEmptyLines()); // Bug: should be false
        assertEquals("\n", headerFormat.getRecordSeparator());
        assertTrue(headerFormat.isSkipHeaderRecord());
        assertTrue(headerFormat.isIgnoreSurroundingSpaces());
        assertEquals("NULL", headerFormat.getNullString());
        assertEquals(Character.valueOf('#'), headerFormat.getCommentMarker());
        assertTrue(headerFormat.isAllowMissingColumnNames());
        assertTrue(headerFormat.isTrim());
        assertFalse(format.isHeader());
        assertNotSame(custom, headerFormat);
    }

    @Test
    public void testHeaderCalledTwice() {
        CSVFormat header1 = format.withHeader();
        CSVFormat header2 = header1.withHeader();
        assertTrue(header2.isHeader());
        assertEquals(',', header2.getDelimiter());
    }

    @Test
    public void testToString() {
        String str = format.toString();
        assertNotNull(str);
        assertTrue(str.contains("Delimiter="));
    }

    @Test
    public void testEquals() {
        CSVFormat format1 = CSVFormat.newFormat(',');
        CSVFormat format2 = CSVFormat.newFormat(',');
        assertEquals(format1, format2);
        assertEquals(format1.hashCode(), format2.hashCode());
        CSVFormat format3 = CSVFormat.newFormat(';');
        assertFalse(format1.equals(format3));
        assertFalse(format1.equals(null));
        assertFalse(format1.equals("string"));
    }

    @Test
    public void testClone() {
        assertNotSame(format, format.withDelimiter(','));
        assertNotSame(format, format.withQuoteChar('"'));
    }

    @Test
    public void testStaticFormats() {
        CSVFormat rfc = CSVFormat.RFC4180;
        assertEquals(',', rfc.getDelimiter());
        assertEquals(Character.valueOf('"'), rfc.getQuoteChar());
        CSVFormat excel = CSVFormat.EXCEL;
        assertEquals(',', excel.getDelimiter());
        CSVFormat tdf = CSVFormat.TDF;
        assertEquals('\t', tdf.getDelimiter());
        CSVFormat mysql = CSVFormat.MYSQL;
        assertEquals('\t', mysql.getDelimiter());
    }

    @Test
    public void testNewFormat() {
        CSVFormat custom = CSVFormat.newFormat('|');
        assertEquals('|', custom.getDelimiter());
        assertEquals(Character.valueOf('"'), custom.getQuoteChar());
    }

    @Test
    public void testValidate() {
        format.validate();
        CSVFormat invalid = format.withDelimiter('"').withQuoteChar('"');
        try {
            invalid.validate();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateInvalidDelimiter() {
        CSVFormat invalid = format.withDelimiter('a');
        invalid.validate();
    }
}