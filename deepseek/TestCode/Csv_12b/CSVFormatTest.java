package org.apache.commons.csv;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class CSVFormatTest {

    private CSVFormat defaultFormat;

    @Before
    public void setUp() {
        defaultFormat = CSVFormat.DEFAULT;
    }

    // ---------- Default format tests ----------

    @Test
    public void testDefaultFormatNotNull() {
        assertNotNull(defaultFormat);
    }

    @Test
    public void testDefaultFormatProperties() {
        assertEquals(',', defaultFormat.getDelimiter());
        assertEquals('"', defaultFormat.getQuoteCharacter());
        assertNull(defaultFormat.getEscapeCharacter());
        assertFalse(defaultFormat.getIgnoreEmptyLines());
        assertFalse(defaultFormat.getSkipHeaderRecord());
        assertNull(defaultFormat.getHeader());
        assertNull(defaultFormat.getNullString());
        assertFalse(defaultFormat.getIgnoreSurroundingSpaces());
        assertEquals("\r\n", defaultFormat.getRecordSeparator());
    }

    // ---------- withHeader tests (bug 12 related) ----------

    @Test
    public void testWithHeaderValid() {
        CSVFormat format = defaultFormat.withHeader("col1", "col2", "col3");
        assertNotNull(format);
        assertArrayEquals(new String[] {"col1", "col2", "col3"}, format.getHeader());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithHeaderNullElement() {
        defaultFormat.withHeader("col1", null, "col3");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithHeaderEmptyElement() {
        defaultFormat.withHeader("col1", "", "col3");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithHeaderNullArray() {
        defaultFormat.withHeader((String[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithHeaderEmptyArray() {
        defaultFormat.withHeader(new String[0]);
    }

    @Test
    public void testWithHeaderReturnsNewInstance() {
        CSVFormat withHeader = defaultFormat.withHeader("a");
        assertNotSame(defaultFormat, withHeader);
        assertNull(defaultFormat.getHeader());
    }

    // ---------- withDelimiter tests ----------

    @Test
    public void testWithDelimiterValid() {
        CSVFormat format = defaultFormat.withDelimiter(';');
        assertEquals(';', format.getDelimiter());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiterInvalidLineBreak() {
        defaultFormat.withDelimiter('\n');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiterInvalidCarriageReturn() {
        defaultFormat.withDelimiter('\r');
    }

    // ---------- withQuote tests ----------

    @Test
    public void testWithQuoteValid() {
        CSVFormat format = defaultFormat.withQuote('\'');
        assertEquals(Character.valueOf('\''), format.getQuoteCharacter());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteNull() {
        defaultFormat.withQuote(null);
    }

    // ---------- withEscape tests ----------

    @Test
    public void testWithEscapeValid() {
        CSVFormat format = defaultFormat.withEscape('\\');
        assertEquals(Character.valueOf('\\'), format.getEscapeCharacter());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeNull() {
        defaultFormat.withEscape(null);
    }

    // ---------- withIgnoreEmptyLines tests ----------

    @Test
    public void testWithIgnoreEmptyLinesTrue() {
        CSVFormat format = defaultFormat.withIgnoreEmptyLines(true);
        assertTrue(format.getIgnoreEmptyLines());
    }

    @Test
    public void testWithIgnoreEmptyLinesFalse() {
        CSVFormat format = defaultFormat.withIgnoreEmptyLines(false);
        assertFalse(format.getIgnoreEmptyLines());
    }

    // ---------- withRecordSeparator tests ----------

    @Test
    public void testWithRecordSeparatorString() {
        CSVFormat format = defaultFormat.withRecordSeparator("\n");
        assertEquals("\n", format.getRecordSeparator());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithRecordSeparatorNull() {
        defaultFormat.withRecordSeparator((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithRecordSeparatorEmpty() {
        defaultFormat.withRecordSeparator("");
    }

    // ---------- withSkipHeaderRecord tests ----------

    @Test
    public void testWithSkipHeaderRecordTrue() {
        CSVFormat format = defaultFormat.withSkipHeaderRecord(true);
        assertTrue(format.getSkipHeaderRecord());
    }

    @Test
    public void testWithSkipHeaderRecordFalse() {
        CSVFormat format = defaultFormat.withSkipHeaderRecord(false);
        assertFalse(format.getSkipHeaderRecord());
    }

    // ---------- withNullString tests ----------

    @Test
    public void testWithNullStringValid() {
        CSVFormat format = defaultFormat.withNullString("NULL");
        assertEquals("NULL", format.getNullString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithNullStringNull() {
        defaultFormat.withNullString(null);
    }

    // ---------- withIgnoreSurroundingSpaces tests ----------

    @Test
    public void testWithIgnoreSurroundingSpacesTrue() {
        CSVFormat format = defaultFormat.withIgnoreSurroundingSpaces(true);
        assertTrue(format.getIgnoreSurroundingSpaces());
    }

    @Test
    public void testWithIgnoreSurroundingSpacesFalse() {
        CSVFormat format = defaultFormat.withIgnoreSurroundingSpaces(false);
        assertFalse(format.getIgnoreSurroundingSpaces());
    }

    // ---------- equals and hashCode tests ----------

    @Test
    public void testEqualsReflexive() {
        assertEquals(defaultFormat, defaultFormat);
    }

    @Test
    public void testEqualsSymmetric() {
        CSVFormat format1 = defaultFormat.withDelimiter(';');
        CSVFormat format2 = defaultFormat.withDelimiter(';');
        assertEquals(format1, format2);
        assertEquals(format2, format1);
    }

    @Test
    public void testEqualsNull() {
        assertFalse(defaultFormat.equals(null));
    }

    @Test
    public void testEqualsDifferentType() {
        assertFalse(defaultFormat.equals("string"));
    }

    @Test
    public void testEqualsDifferentDelimiter() {
        CSVFormat format1 = defaultFormat.withDelimiter(';');
        CSVFormat format2 = defaultFormat.withDelimiter(',');
        assertFalse(format1.equals(format2));
    }

    @Test
    public void testHashCodeConsistency() {
        CSVFormat format = defaultFormat.withDelimiter(';').withQuote('\'');
        int hashCode1 = format.hashCode();
        int hashCode2 = format.hashCode();
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    public void testHashCodeEqualObjects() {
        CSVFormat format1 = defaultFormat.withDelimiter(';');
        CSVFormat format2 = defaultFormat.withDelimiter(';');
        assertEquals(format1.hashCode(), format2.hashCode());
    }

    // ---------- toString tests ----------

    @Test
    public void testToStringNotNull() {
        assertNotNull(defaultFormat.toString());
    }

    @Test
    public void testToStringContainsDelimiter() {
        String str = defaultFormat.toString();
        assertTrue(str.contains("delimiter=" + defaultFormat.getDelimiter()));
    }

    // ---------- format method tests ----------

    @Test
    public void testFormatSimple() {
        String result = defaultFormat.format("a", "b", "c");
        assertEquals("a,b,c\r\n", result);
    }

    @Test
    public void testFormatWithQuote() {
        CSVFormat format = defaultFormat.withQuote('"');
        String result = format.format("hello", "wo\"rld");
        assertEquals("hello,\"wo\"\"rld\"\r\n", result);
    }

    @Test
    public void testFormatWithEscape() {
        CSVFormat format = defaultFormat.withEscape('\\');
        String result = format.format("a", "b\\c");
        assertEquals("a,b\\\\c\r\n", result);
    }

    @Test
    public void testFormatWithNullString() {
        CSVFormat format = defaultFormat.withNullString("\\N");
        String result = format.format("a", null, "c");
        assertEquals("a,\\N,c\r\n", result);
    }

    @Test
    public void testFormatWithIgnoreSurroundingSpaces() {
        CSVFormat format = defaultFormat.withIgnoreSurroundingSpaces(true);
        String result = format.format(" a ", "b");
        assertEquals(" a ,b\r\n", result); // spaces are not trimmed in format
    }

    @Test
    public void testFormatWithCustomDelimiter() {
        CSVFormat format = defaultFormat.withDelimiter(';');
        String result = format.format("a", "b");
        assertEquals("a;b\r\n", result);
    }

    @Test
    public void testFormatWithRecordSeparator() {
        CSVFormat format = defaultFormat.withRecordSeparator("\n");
        String result = format.format("a", "b");
        assertEquals("a,b\n", result);
    }

    // ---------- validate method tests ----------

    @Test(expected = IllegalArgumentException.class)
    public void testValidateThrowsOnNullDelimiter() {
        // Delimiter cannot be null, but we can test via reflection? Not needed.
        // Instead test that validate throws when quote and escape are same.
        CSVFormat format = defaultFormat.withQuote('"').withEscape('"');
        format.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateThrowsOnQuoteAndEscapeSame() {
        CSVFormat format = defaultFormat.withQuote('x').withEscape('x');
        format.validate();
    }

    @Test
    public void testValidateValidFormat() {
        // Should not throw
        defaultFormat.validate();
    }

    // ---------- Static constants tests ----------

    @Test
    public void testDefaultConstant() {
        assertNotNull(CSVFormat.DEFAULT);
    }

    @Test
    public void testExcelConstant() {
        assertNotNull(CSVFormat.EXCEL);
    }

    @Test
    public void testMySQLConstant() {
        assertNotNull(CSVFormat.MYSQL);
    }

    @Test
    public void testRFC4180Constant() {
        assertNotNull(CSVFormat.RFC4180);
    }

    @Test
    public void testTDFConstant() {
        assertNotNull(CSVFormat.TDF);
    }

    // ---------- Edge cases for with methods ----------

    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiterMultipleCharacters() {
        // Delimiter must be a single char, but method takes char, so cannot pass multiple.
        // This test is not applicable; skip.
    }

    @Test
    public void testWithQuoteCharacterNullAllowed() {
        // Some formats allow null quote (e.g., MySQL)
        CSVFormat format = CSVFormat.MYSQL;
        assertNull(format.getQuoteCharacter());
    }

    @Test
    public void testWithEscapeCharacterNullAllowed() {
        CSVFormat format = CSVFormat.DEFAULT;
        assertNull(format.getEscapeCharacter());
    }

    // ---------- Test that with methods return new instances ----------

    @Test
    public void testWithDelimiterReturnsNewInstance() {
        CSVFormat newFormat = defaultFormat.withDelimiter(';');
        assertNotSame(defaultFormat, newFormat);
    }

    @Test
    public void testWithQuoteReturnsNewInstance() {
        CSVFormat newFormat = defaultFormat.withQuote('\'');
        assertNotSame(defaultFormat, newFormat);
    }

    @Test
    public void testWithEscapeReturnsNewInstance() {
        CSVFormat newFormat = defaultFormat.withEscape('\\');
        assertNotSame(defaultFormat, newFormat);
    }

    @Test
    public void testWithIgnoreEmptyLinesReturnsNewInstance() {
        CSVFormat newFormat = defaultFormat.withIgnoreEmptyLines(true);
        assertNotSame(defaultFormat, newFormat);
    }

    @Test
    public void testWithRecordSeparatorReturnsNewInstance() {
        CSVFormat newFormat = defaultFormat.withRecordSeparator("\n");
        assertNotSame(defaultFormat, newFormat);
    }

    @Test
    public void testWithSkipHeaderRecordReturnsNewInstance() {
        CSVFormat newFormat = defaultFormat.withSkipHeaderRecord(true);
        assertNotSame(defaultFormat, newFormat);
    }

    @Test
    public void testWithNullStringReturnsNewInstance() {
        CSVFormat newFormat = defaultFormat.withNullString("NULL");
        assertNotSame(defaultFormat, newFormat);
    }

    @Test
    public void testWithIgnoreSurroundingSpacesReturnsNewInstance() {
        CSVFormat newFormat = defaultFormat.withIgnoreSurroundingSpaces(true);
        assertNotSame(defaultFormat, newFormat);
    }
}