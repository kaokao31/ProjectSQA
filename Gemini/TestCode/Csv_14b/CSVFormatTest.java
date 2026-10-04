/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.csv;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.junit.Test;

public class CSVFormatTest {

    @Test
    public void testStandardConstants() {
        assertNotNull(CSVFormat.DEFAULT);
        assertNotNull(CSVFormat.RFC4180);
        assertNotNull(CSVFormat.EXCEL);
        assertNotNull(CSVFormat.TDF);
        assertNotNull(CSVFormat.MYSQL);
    }

    @Test
    public void testValueOf() {
        assertEquals(CSVFormat.DEFAULT, CSVFormat.valueOf("DEFAULT"));
        assertEquals(CSVFormat.RFC4180, CSVFormat.valueOf("RFC4180"));
        assertEquals(CSVFormat.EXCEL, CSVFormat.valueOf("EXCEL"));
        assertEquals(CSVFormat.TDF, CSVFormat.valueOf("TDF"));
        assertEquals(CSVFormat.MYSQL, CSVFormat.valueOf("MYSQL"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOfInvalid() {
        CSVFormat.valueOf("NON_EXISTENT");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOfNull() {
        CSVFormat.valueOf(null);
    }

    @Test
    public void testFormatMethods() {
        final CSVFormat format = CSVFormat.DEFAULT;
        assertEquals("a,b,c", format.format("a", "b", "c"));
        assertEquals("\"a,b\",c", format.format("a,b", "c"));
        assertEquals("\"\"\"a\"\"\",b", format.format("\"a\"", "b"));
    }

    @Test
    public void testFormatWithNullValues() {
        final CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        assertEquals("NULL,b", format.format(null, "b"));
        
        final CSVFormat formatNoNull = CSVFormat.DEFAULT.withNullString(null);
        assertEquals(",b", formatNoNull.format(null, "b"));
    }

    @Test
    public void testBuilderPatternAndGetters() {
        final Character delimiter = ';';
        const:
        final Character quoteChar = '\'';
        final Character quotePolicy = null;
        final Character commentStart = '#';
        final Character escape = '\\';
        final boolean ignoreSurroundingSpaces = true;
        final boolean ignoreEmptyLines = true;
        final String recordSeparator = "\r\n";
        final String nullString = "NULL";
        final String[] header = {"h1", "h2"};
        final String[] headerComments = {"hc1"};
        final boolean skipHeaderRecord = true;
        final boolean allowMissingColumnNames = true;
        final boolean ignoreHeaderCase = true;
        final boolean trim = true;
        final boolean trailingDelimiter = true;
        final boolean autoFlush = true;

        CSVFormat format = CSVFormat.DEFAULT
                .withDelimiter(delimiter)
                .withQuote(quoteChar)
                .withQuoteMode(QuoteMode.ALL)
                .withCommentMarker(commentStart)
                .withEscape(escape)
                .withIgnoreSurroundingSpaces(ignoreSurroundingSpaces)
                .withIgnoreEmptyLines(ignoreEmptyLines)
                .withRecordSeparator(recordSeparator)
                .withNullString(nullString)
                .withHeader(header)
                .withHeaderComments(headerComments)
                .withSkipHeaderRecord(skipHeaderRecord)
                .withAllowMissingColumnNames(allowMissingColumnNames)
                .withIgnoreHeaderCase(ignoreHeaderCase)
                .withTrim(trim)
                .withTrailingDelimiter(trailingDelimiter)
                .withAutoFlush(autoFlush);

        assertEquals(delimiter.charValue(), format.getDelimiter());
        assertEquals(quoteChar, format.getQuoteCharacter());
        assertEquals(QuoteMode.ALL, format.getQuoteMode());
        assertEquals(commentStart, format.getCommentMarker());
        assertEquals(escape, format.getEscapeCharacter());
        assertEquals(ignoreSurroundingSpaces, format.getIgnoreSurroundingSpaces());
        assertEquals(ignoreEmptyLines, format.getIgnoreEmptyLines());
        assertEquals(recordSeparator, format.getRecordSeparator());
        assertEquals(nullString, format.getNullString());
        assertArrayEquals(header, format.getHeader());
        assertArrayEquals(headerComments, format.getHeaderComments());
        assertEquals(skipHeaderRecord, format.getSkipHeaderRecord());
        assertEquals(allowMissingColumnNames, format.getAllowMissingColumnNames());
        assertEquals(ignoreHeaderCase, format.getIgnoreHeaderCase());
        assertEquals(trim, format.getTrim());
        assertEquals(trailingDelimiter, format.getTrailingDelimiter());
        assertEquals(autoFlush, format.getAutoFlush());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiterLF() {
        CSVFormat.DEFAULT.withDelimiter('\n');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiterCR() {
        CSVFormat.DEFAULT.withDelimiter('\r');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithQuoteCharLF() {
        CSVFormat.DEFAULT.withQuote('\n');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithCommentStartCR() {
        CSVFormat.DEFAULT.withCommentMarker('\r');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithEscapeLF() {
        CSVFormat.DEFAULT.withEscape('\n');
    }

    @Test
    public void testEqualsAndHashCode() {
        final CSVFormat f1 = CSVFormat.DEFAULT;
        final CSVFormat f2 = CSVFormat.DEFAULT;
        final CSVFormat f3 = CSVFormat.RFC4180;

        assertTrue(f1.equals(f1));
        assertTrue(f1.equals(f2));
        assertEquals(f1.hashCode(), f2.hashCode());

        assertFalse(f1.equals(null));
        assertFalse(f1.equals("Some String"));
        assertFalse(f1.equals(f3));

        // Test inequality branches in equals
        final CSVFormat diffDelimiter = CSVFormat.DEFAULT.withDelimiter(';');
        assertFalse(CSVFormat.DEFAULT.equals(diffDelimiter));

        final CSVFormat diffQuoteChar = CSVFormat.DEFAULT.withQuote('\'');
        assertFalse(CSVFormat.DEFAULT.equals(diffQuoteChar));

        final CSVFormat diffQuoteMode = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);
        assertFalse(CSVFormat.DEFAULT.equals(diffQuoteMode));

        final CSVFormat diffComment = CSVFormat.DEFAULT.withCommentMarker('#');
        assertFalse(CSVFormat.DEFAULT.equals(diffComment));

        final CSVFormat diffEscape = CSVFormat.DEFAULT.withEscape('\\');
        assertFalse(CSVFormat.DEFAULT.equals(diffEscape));

        final CSVFormat diffNull = CSVFormat.DEFAULT.withNullString("NULL");
        assertFalse(CSVFormat.DEFAULT.equals(diffNull));

        final CSVFormat diffRecordSep = CSVFormat.DEFAULT.withRecordSeparator("XYZ");
        assertFalse(CSVFormat.DEFAULT.equals(diffRecordSep));

        final CSVFormat diffSkipHeader = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        assertFalse(CSVFormat.DEFAULT.equals(diffSkipHeader));

        final CSVFormat diffAllowMissing = CSVFormat.DEFAULT.withAllowMissingColumnNames(true);
        assertFalse(CSVFormat.DEFAULT.equals(diffAllowMissing));

        final CSVFormat diffIgnoreSurrounding = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        assertFalse(CSVFormat.DEFAULT.equals(diffIgnoreSurrounding));

        final CSVFormat diffIgnoreEmpty = CSVFormat.DEFAULT.withIgnoreEmptyLines(false);
        assertFalse(CSVFormat.DEFAULT.equals(diffIgnoreEmpty));

        final CSVFormat diffHeaderCase = CSVFormat.DEFAULT.withIgnoreHeaderCase(true);
        assertFalse(CSVFormat.DEFAULT.equals(diffHeaderCase));

        final CSVFormat diffTrim = CSVFormat.DEFAULT.withTrim(true);
        assertFalse(CSVFormat.DEFAULT.equals(diffTrim));

        final CSVFormat diffTrailing = CSVFormat.DEFAULT.withTrailingDelimiter(true);
        assertFalse(CSVFormat.DEFAULT.equals(diffTrailing));

        final CSVFormat diffAutoFlush = CSVFormat.DEFAULT.withAutoFlush(true);
        assertFalse(CSVFormat.DEFAULT.equals(diffAutoFlush));
    }

    @Test
    public void testEqualsWithNullFields() {
        final CSVFormat f1 = CSVFormat.DEFAULT.withCommentMarker(null).withEscape(null).withNullString(null)
                .withQuote(null).withRecordSeparator(null);
        final CSVFormat f2 = CSVFormat.DEFAULT.withCommentMarker(null).withEscape(null).withNullString(null)
                .withQuote(null).withRecordSeparator(null);
        assertEquals(f1, f2);
    }

    @Test
    public void testToString() {
        final String str = CSVFormat.DEFAULT.toString();
        assertNotNull(str);
        assertTrue(str.contains("Delimiter"));
    }

    @Test
    public void testParse() throws IOException {
        final Reader reader = new StringReader("a,b,c\n1,2,3");
        final CSVParser parser = CSVFormat.DEFAULT.parse(reader);
        assertNotNull(parser);
        assertEquals(2, parser.getRecords().size());
    }

    @Test
    public void testWithHeaderClass() {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader(EnumTest.class);
        assertNotNull(format.getHeader());
        assertArrayEquals(new String[] { "ONE", "TWO", "THREE" }, format.getHeader());
    }

    private enum EnumTest {
        ONE, TWO, THREE
    }

    @Test
    public void testValidate() {
        // Valid by default
        CSVFormat.DEFAULT.validate();

        // Invalid: delimiter equals quote character
        try {
            CSVFormat.DEFAULT.withDelimiter('"').withQuote('"').validate();
            fail("Expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            // expected
        }

        // Invalid: delimiter equals comment start
        try {
            CSVFormat.DEFAULT.withDelimiter('#').withCommentMarker('#').validate();
            fail("Expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            // expected
        }

        // Invalid: delimiter equals escape
        try {
            CSVFormat.DEFAULT.withDelimiter('\\').withEscape('\\').validate();
            fail("Expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            // expected
        }

        // Invalid: quote equals comment start
        try {
            CSVFormat.DEFAULT.withQuote('#').withCommentMarker('#').validate();
            fail("Expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            // expected
        }

        // Invalid: quote equals escape
        try {
            CSVFormat.DEFAULT.withQuote('\\').withEscape('\\').validate();
            fail("Expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            // expected
        }

        // Invalid: comment start equals escape
        try {
            CSVFormat.DEFAULT.withCommentMarker('#').withEscape('#').validate();
            fail("Expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            // expected
        }
    }
}