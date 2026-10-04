package org.apache.commons.csv;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Comprehensive JUnit 4 test suite for CSVFormat.
 * Designed to achieve maximum line and branch coverage and to detect
 * potential faults (e.g., Defects4J style bugs).
 */
public class CSVFormatTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private CSVFormat defaultFormat;

    @Before
    public void setUp() {
        defaultFormat = CSVFormat.DEFAULT;
    }

    // ======================== Default values ========================

    @Test
    public void testDefaultFormatNotNull() {
        assertNotNull("Default format should not be null", defaultFormat);
    }

    @Test
    public void testDefaultDelimiter() {
        assertEquals("Default delimiter should be ','", ',', defaultFormat.getDelimiter());
    }

    @Test
    public void testDefaultQuoteChar() {
        assertEquals("Default quote char should be '\"'", '"', defaultFormat.getQuoteChar());
    }

    @Test
    public void testDefaultEscapeChar() {
        assertEquals("Default escape char should be '\\'", '\\', defaultFormat.getEscapeChar());
    }

    @Test
    public void testDefaultIgnoreEmptyLines() {
        assertTrue("Default should ignore empty lines", defaultFormat.isIgnoreEmptyLines());
    }

    @Test
    public void testDefaultRecordSeparator() {
        assertEquals("Default record separator should be \"\\r\\n\"", "\r\n", defaultFormat.getRecordSeparator());
    }

    @Test
    public void testDefaultNullString() {
        assertNull("Default null string should be null", defaultFormat.getNullString());
    }

    @Test
    public void testDefaultIgnoreSurroundingSpaces() {
        assertFalse("Default should not ignore surrounding spaces", defaultFormat.isIgnoreSurroundingSpaces());
    }

    @Test
    public void testDefaultTrim() {
        assertFalse("Default should not trim", defaultFormat.isTrim());
    }

    @Test
    public void testDefaultAllowMissingColumnNames() {
        assertFalse("Default should not allow missing column names", defaultFormat.isAllowMissingColumnNames());
    }

    @Test
    public void testDefaultFirstRecordAsHeader() {
        assertFalse("Default should not treat first record as header", defaultFormat.isFirstRecordAsHeader());
    }

    @Test
    public void testDefaultHeader() {
        assertNull("Default header should be null", defaultFormat.getHeader());
    }

    // ======================== Property setters (immutability) ========================

    @Test
    public void testWithDelimiter() {
        CSVFormat custom = defaultFormat.withDelimiter(';');
        assertEquals("Custom delimiter should be ';'", ';', custom.getDelimiter());
        // Original unchanged
        assertEquals("Original delimiter should remain ','", ',', defaultFormat.getDelimiter());
        assertNotSame("Should return a new instance", defaultFormat, custom);
    }

    @Test
    public void testWithQuoteChar() {
        CSVFormat custom = defaultFormat.withQuoteChar('\'');
        assertEquals("Custom quote char should be '''", '\'', custom.getQuoteChar());
        assertEquals("Original quote char should remain '\"'", '"', defaultFormat.getQuoteChar());
        assertNotSame("Should return a new instance", defaultFormat, custom);
    }

    @Test
    public void testWithEscapeChar() {
        CSVFormat custom = defaultFormat.withEscapeChar('?');
        assertEquals("Custom escape char should be '?'", '?', custom.getEscapeChar());
        assertEquals("Original escape char should remain '\\'", '\\', defaultFormat.getEscapeChar());
        assertNotSame("Should return a new instance", defaultFormat, custom);
    }

    @Test
    public void testWithIgnoreEmptyLines() {
        CSVFormat custom = defaultFormat.withIgnoreEmptyLines(false);
        assertFalse("Custom should not ignore empty lines", custom.isIgnoreEmptyLines());
        assertTrue("Original should still ignore empty lines", defaultFormat.isIgnoreEmptyLines());
        assertNotSame("Should return a new instance", defaultFormat, custom);
    }

    @Test
    public void testWithRecordSeparator() {
        CSVFormat custom = defaultFormat.withRecordSeparator("\n");
        assertEquals("Custom record separator should be \"\\n\"", "\n", custom.getRecordSeparator());
        assertEquals("Original record separator should remain \"\\r\\n\"", "\r\n", defaultFormat.getRecordSeparator());
        assertNotSame("Should return a new instance", defaultFormat, custom);
    }

    @Test
    public void testWithNullString() {
        CSVFormat custom = defaultFormat.withNullString("NULL");
        assertEquals("Custom null string should be \"NULL\"", "NULL", custom.getNullString());
        assertNull("Original null string should be null", defaultFormat.getNullString());
        assertNotSame("Should return a new instance", defaultFormat, custom);
    }

    @Test
    public void testWithNullStringNull() {
        // Setting null string to null explicitly should be allowed
        CSVFormat custom = defaultFormat.withNullString(null);
        assertNull("Custom null string should be null", custom.getNullString());
        assertNotSame("Should return a new instance", defaultFormat, custom);
    }

    @Test
    public void testWithIgnoreSurroundingSpaces() {
        CSVFormat custom = defaultFormat.withIgnoreSurroundingSpaces(true);
        assertTrue("Custom should ignore surrounding spaces", custom.isIgnoreSurroundingSpaces());
        assertFalse("Original should not ignore surrounding spaces", defaultFormat.isIgnoreSurroundingSpaces());
        assertNotSame("Should return a new instance", defaultFormat, custom);
    }

    @Test
    public void testWithTrim() {
        CSVFormat custom = defaultFormat.withTrim(true);
        assertTrue("Custom should trim", custom.isTrim());
        assertFalse("Original should not trim", defaultFormat.isTrim());
        assertNotSame("Should return a new instance", defaultFormat, custom);
    }

    @Test
    public void testWithAllowMissingColumnNames() {
        CSVFormat custom = defaultFormat.withAllowMissingColumnNames(true);
        assertTrue("Custom should allow missing column names", custom.isAllowMissingColumnNames());
        assertFalse("Original should not allow missing column names", defaultFormat.isAllowMissingColumnNames());
        assertNotSame("Should return a new instance", defaultFormat, custom);
    }

    @Test
    public void testWithFirstRecordAsHeader() {
        CSVFormat custom = defaultFormat.withFirstRecordAsHeader();
        assertTrue("Custom should treat first record as header", custom.isFirstRecordAsHeader());
        assertNull("Custom header should be null (not yet set)", custom.getHeader());
        assertFalse("Original should not treat first record as header", defaultFormat.isFirstRecordAsHeader());
        assertNotSame("Should return a new instance", defaultFormat, custom);
    }

    @Test
    public void testWithHeader() {
        CSVFormat custom = defaultFormat.withHeader("Name", "Age", "City");
        assertArrayEquals("Custom header should be [\"Name\", \"Age\", \"City\"]",
                new String[]{"Name", "Age", "City"}, custom.getHeader());
        assertFalse("Custom should not treat first record as header (header explicitly set)",
                custom.isFirstRecordAsHeader());
        assertNull("Original header should be null", defaultFormat.getHeader());
        assertNotSame("Should return a new instance", defaultFormat, custom);
    }

    @Test
    public void testWithHeaderEmptyArray() {
        CSVFormat custom = defaultFormat.withHeader();
        assertNotNull("Custom header should not be null", custom.getHeader());
        assertEquals("Custom header should be empty", 0, custom.getHeader().length);
        assertNotSame("Should return a new instance", defaultFormat, custom);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithHeaderNullArray() {
        defaultFormat.withHeader((String[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithHeaderNullElement() {
        defaultFormat.withHeader("A", null, "B");
    }

    @Test
    public void testWithHeaderOverridesFirstRecord() {
        CSVFormat custom = defaultFormat.withFirstRecordAsHeader().withHeader("X", "Y");
        assertFalse("Explicit header should override first record flag", custom.isFirstRecordAsHeader());
        assertArrayEquals("Header should be [\"X\", \"Y\"]", new String[]{"X", "Y"}, custom.getHeader());
    }

    // ======================== Chaining ========================

    @Test
    public void testChaining() {
        CSVFormat custom = defaultFormat
                .withDelimiter('|')
                .withQuoteChar('"')
                .withEscapeChar('\\')
                .withIgnoreEmptyLines(false)
                .withRecordSeparator("\n")
                .withNullString("N/A")
                .withIgnoreSurroundingSpaces(true)
                .withTrim(true)
                .withAllowMissingColumnNames(true)
                .withHeader("Col1", "Col2");
        assertEquals('|', custom.getDelimiter());
        assertEquals('"', custom.getQuoteChar());
        assertEquals('\\', custom.getEscapeChar());
        assertFalse(custom.isIgnoreEmptyLines());
        assertEquals("\n", custom.getRecordSeparator());
        assertEquals("N/A", custom.getNullString());
        assertTrue(custom.isIgnoreSurroundingSpaces());
        assertTrue(custom.isTrim());
        assertTrue(custom.isAllowMissingColumnNames());
        assertArrayEquals(new String[]{"Col1", "Col2"}, custom.getHeader());
        assertFalse(custom.isFirstRecordAsHeader());
    }

    // ======================== Validation ========================

    @Test
    public void testValidateValid() {
        // Default format should be valid
        defaultFormat.validate();
    }

    @Test
    public void testValidateDelimiterEqualsQuoteChar() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("delimiter");
        defaultFormat.withDelimiter('"').validate();
    }

    @Test
    public void testValidateDelimiterEqualsEscapeChar() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("delimiter");
        defaultFormat.withDelimiter('\\').validate();
    }

    @Test
    public void testValidateQuoteCharEqualsEscapeChar() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("quote");
        defaultFormat.withQuoteChar('\\').validate();
    }

    @Test
    public void testValidateDelimiterIsLetter() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("delimiter");
        defaultFormat.withDelimiter('a').validate();
    }

    @Test
    public void testValidateDelimiterIsWhitespace() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("delimiter");
        defaultFormat.withDelimiter(' ').validate();
    }

    @Test
    public void testValidateQuoteCharIsLetter() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("quote");
        defaultFormat.withQuoteChar('x').validate();
    }

    @Test
    public void testValidateEscapeCharIsLetter() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("escape");
        defaultFormat.withEscapeChar('e').validate();
    }

    // ======================== equals and hashCode ========================

    @Test
    public void testEqualsReflexive() {
        assertEquals("An object should be equal to itself", defaultFormat, defaultFormat);
    }

    @Test
    public void testEqualsSymmetric() {
        CSVFormat custom1 = defaultFormat.withDelimiter(';');
        CSVFormat custom2 = defaultFormat.withDelimiter(';');
        assertEquals("Two formats with same delimiter should be equal", custom1, custom2);
        assertEquals("Symmetric", custom2, custom1);
    }

    @Test
    public void testEqualsDifferentDelimiter() {
        CSVFormat custom1 = defaultFormat.withDelimiter(';');
        CSVFormat custom2 = defaultFormat.withDelimiter(',');
        assertNotEquals("Formats with different delimiters should not be equal", custom1, custom2);
    }

    @Test
    public void testEqualsDifferentQuoteChar() {
        CSVFormat custom1 = defaultFormat.withQuoteChar('\'');
        CSVFormat custom2 = defaultFormat.withQuoteChar('"');
        assertNotEquals("Formats with different quote chars should not be equal", custom1, custom2);
    }

    @Test
    public void testEqualsDifferentEscapeChar() {
        CSVFormat custom1 = defaultFormat.withEscapeChar('?');
        CSVFormat custom2 = defaultFormat.withEscapeChar('\\');
        assertNotEquals("Formats with different escape chars should not be equal", custom1, custom2);
    }

    @Test
    public void testEqualsDifferentIgnoreEmptyLines() {
        CSVFormat custom1 = defaultFormat.withIgnoreEmptyLines(false);
        CSVFormat custom2 = defaultFormat.withIgnoreEmptyLines(true);
        assertNotEquals("Formats with different ignoreEmptyLines should not be equal", custom1, custom2);
    }

    @Test
    public void testEqualsDifferentRecordSeparator() {
        CSVFormat custom1 = defaultFormat.withRecordSeparator("\n");
        CSVFormat custom2 = defaultFormat.withRecordSeparator("\r\n");
        assertNotEquals("Formats with different record separators should not be equal", custom1, custom2);
    }

    @Test
    public void testEqualsDifferentNullString() {
        CSVFormat custom1 = defaultFormat.withNullString("N/A");
        CSVFormat custom2 = defaultFormat.withNullString("NULL");
        assertNotEquals("Formats with different null strings should not be equal", custom1, custom2);
    }

    @Test
    public void testEqualsDifferentIgnoreSurroundingSpaces() {
        CSVFormat custom1 = defaultFormat.withIgnoreSurroundingSpaces(true);
        CSVFormat custom2 = defaultFormat.withIgnoreSurroundingSpaces(false);
        assertNotEquals("Formats with different ignoreSurroundingSpaces should not be equal", custom1, custom2);
    }

    @Test
    public void testEqualsDifferentTrim() {
        CSVFormat custom1 = defaultFormat.withTrim(true);
        CSVFormat custom2 = defaultFormat.withTrim(false);
        assertNotEquals("Formats with different trim should not be equal", custom1, custom2);
    }

    @Test
    public void testEqualsDifferentAllowMissingColumnNames() {
        CSVFormat custom1 = defaultFormat.withAllowMissingColumnNames(true);
        CSVFormat custom2 = defaultFormat.withAllowMissingColumnNames(false);
        assertNotEquals("Formats with different allowMissingColumnNames should not be equal", custom1, custom2);
    }

    @Test
    public void testEqualsDifferentFirstRecordAsHeader() {
        CSVFormat custom1 = defaultFormat.withFirstRecordAsHeader();
        CSVFormat custom2 = defaultFormat;
        assertNotEquals("Formats with different firstRecordAsHeader should not be equal", custom1, custom2);
    }

    @Test
    public void testEqualsDifferentHeader() {
        CSVFormat custom1 = defaultFormat.withHeader("A", "B");
        CSVFormat custom2 = defaultFormat.withHeader("A", "C");
        assertNotEquals("Formats with different headers should not be equal", custom1, custom2);
    }

    @Test
    public void testEqualsSameHeaderDifferentOrder() {
        CSVFormat custom1 = defaultFormat.withHeader("A", "B");
        CSVFormat custom2 = defaultFormat.withHeader("B", "A");
        assertNotEquals("Header order matters", custom1, custom2);
    }

    @Test
    public void testHashCodeConsistency() {
        int hash1 = defaultFormat.hashCode();
        int hash2 = defaultFormat.hashCode();
        assertEquals("hashCode should be consistent", hash1, hash2);
    }

    @Test
    public void testHashCodeEqualObjects() {
        CSVFormat custom1 = defaultFormat.withDelimiter(';').withQuoteChar('\'');
        CSVFormat custom2 = defaultFormat.withDelimiter(';').withQuoteChar('\'');
        assertEquals("Equal objects should have equal hash codes", custom1.hashCode(), custom2.hashCode());
    }

    // ======================== toString ========================

    @Test
    public void testToString() {
        String str = defaultFormat.toString();
        assertNotNull("toString should not return null", str);
        assertTrue("toString should contain class name", str.contains("CSVFormat"));
        assertTrue("toString should contain delimiter", str.contains("delimiter=" + defaultFormat.getDelimiter()));
    }

    // ======================== Edge cases ========================

    @Test
    public void testWithDelimiterNullCharacter() {
        // Null character should be allowed as delimiter (disables delimiter)
        CSVFormat custom = defaultFormat.withDelimiter('\0');
        assertEquals("Delimiter should be null character", '\0', custom.getDelimiter());
    }

    @Test
    public void testWithQuoteCharNullCharacter() {
        // Null character should be allowed as quote char (disables quoting)
        CSVFormat custom = defaultFormat.withQuoteChar('\0');
        assertEquals("Quote char should be null character", '\0', custom.getQuoteChar());
    }

    @Test
    public void testWithEscapeCharNullCharacter() {
        // Null character should be allowed as escape char (disables escaping)
        CSVFormat custom = defaultFormat.withEscapeChar('\0');
        assertEquals("Escape char should be null character", '\0', custom.getEscapeChar());
    }

    @Test(expected = NullPointerException.class)
    public void testWithRecordSeparatorNull() {
        defaultFormat.withRecordSeparator(null);
    }

    @Test
    public void testWithRecordSeparatorEmpty() {
        CSVFormat custom = defaultFormat.withRecordSeparator("");
        assertEquals("Record separator should be empty string", "", custom.getRecordSeparator());
    }

    @Test
    public void testWithHeaderReturnsCopy() {
        String[] header = {"A", "B"};
        CSVFormat custom = defaultFormat.withHeader(header);
        header[0] = "Modified";
        assertArrayEquals("Header should be a copy", new String[]{"A", "B"}, custom.getHeader());
    }

    @Test
    public void testGetHeaderReturnsCopy() {
        CSVFormat custom = defaultFormat.withHeader("X", "Y");
        String[] retrieved = custom.getHeader();
        retrieved[0] = "Changed";
        assertArrayEquals("getHeader should return a copy", new String[]{"X", "Y"}, custom.getHeader());
    }

    // ======================== Additional branch coverage ========================

    @Test
    public void testWithIgnoreEmptyLinesTrue() {
        CSVFormat custom = defaultFormat.withIgnoreEmptyLines(true);
        assertTrue(custom.isIgnoreEmptyLines());
    }

    @Test
    public void testWithIgnoreEmptyLinesFalse() {
        CSVFormat custom = defaultFormat.withIgnoreEmptyLines(false);
        assertFalse(custom.isIgnoreEmptyLines());
    }

    @Test
    public void testWithTrimTrue() {
        CSVFormat custom = defaultFormat.withTrim(true);
        assertTrue(custom.isTrim());
    }

    @Test
    public void testWithTrimFalse() {
        CSVFormat custom = defaultFormat.withTrim(false);
        assertFalse(custom.isTrim());
    }

    @Test
    public void testWithIgnoreSurroundingSpacesTrue() {
        CSVFormat custom = defaultFormat.withIgnoreSurroundingSpaces(true);
        assertTrue(custom.isIgnoreSurroundingSpaces());
    }

    @Test
    public void testWithIgnoreSurroundingSpacesFalse() {
        CSVFormat custom = defaultFormat.withIgnoreSurroundingSpaces(false);
        assertFalse(custom.isIgnoreSurroundingSpaces());
    }

    @Test
    public void testWithAllowMissingColumnNamesTrue() {
        CSVFormat custom = defaultFormat.withAllowMissingColumnNames(true);
        assertTrue(custom.isAllowMissingColumnNames());
    }

    @Test
    public void testWithAllowMissingColumnNamesFalse() {
        CSVFormat custom = defaultFormat.withAllowMissingColumnNames(false);
        assertFalse(custom.isAllowMissingColumnNames());
    }

    @Test
    public void testWithFirstRecordAsHeaderThenWithHeader() {
        CSVFormat custom = defaultFormat.withFirstRecordAsHeader().withHeader("A");
        assertFalse("withHeader should clear firstRecordAsHeader", custom.isFirstRecordAsHeader());
        assertArrayEquals(new String[]{"A"}, custom.getHeader());
    }

    @Test
    public void testWithHeaderThenWithFirstRecordAsHeader() {
        CSVFormat custom = defaultFormat.withHeader("A").withFirstRecordAsHeader();
        assertTrue("withFirstRecordAsHeader should set flag", custom.isFirstRecordAsHeader());
        assertNull("withFirstRecordAsHeader should clear header", custom.getHeader());
    }

    // ======================== Fault detection (Defects4J style) ========================

    @Test
    public void testValidateDelimiterSameAsQuoteCharEdge() {
        // Deliberately set delimiter to quote char and expect validation to fail
        thrown.expect(IllegalStateException.class);
        defaultFormat.withDelimiter('"').validate();
    }

    @Test
    public void testValidateEscapeCharSameAsQuoteCharEdge() {
        thrown.expect(IllegalStateException.class);
        defaultFormat.withEscapeChar('"').validate();
    }

    @Test
    public void testValidateDelimiterSameAsEscapeCharEdge() {
        thrown.expect(IllegalStateException.class);
        defaultFormat.withDelimiter('\\').validate();
    }

    @Test
    public void testValidateDelimiterTab() {
        // Tab is a whitespace character, should be rejected
        thrown.expect(IllegalStateException.class);
        defaultFormat.withDelimiter('\t').validate();
    }

    @Test
    public void testValidateQuoteCharTab() {
        thrown.expect(IllegalStateException.class);
        defaultFormat.withQuoteChar('\t').validate();
    }

    @Test
    public void testValidateEscapeCharTab() {
        thrown.expect(IllegalStateException.class);
        defaultFormat.withEscapeChar('\t').validate();
    }

    @Test
    public void testEqualsWithNull() {
        assertFalse("CSVFormat should not be equal to null", defaultFormat.equals(null));
    }

    @Test
    public void testEqualsWithDifferentClass() {
        assertFalse("CSVFormat should not be equal to a different class", defaultFormat.equals("string"));
    }

    @Test
    public void testHashCodeDifferentObjects() {
        CSVFormat custom1 = defaultFormat.withDelimiter(';');
        CSVFormat custom2 = defaultFormat.withDelimiter(',');
        assertNotEquals("Different objects should (generally) have different hash codes",
                custom1.hashCode(), custom2.hashCode());
    }
}