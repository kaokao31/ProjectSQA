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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Test;

/**
 * Comprehensive test suite for CSVFormat.
 */
public class CSVFormatTest {

    @Test
    public void testStandardConstants() {
        assertNotNull(CSVFormat.DEFAULT);
        assertNotNull(CSVFormat.EXCEL);
        assertNotNull(CSVFormat.RFC4180);
        assertNotNull(CSVFormat.MYSQL);
        assertNotNull(CSVFormat.TDF);
    }

    @Test
    public void testNewFormat() {
        CSVFormat format = CSVFormat.newFormat(';');
        assertEquals(';', format.getDelimiter());
        assertNull(format.getQuoteChar());
        assertNull(format.getCommentStart());
        assertNull(format.getEscape());
        assertFalse(format.isSurroundingSpacesIgnored());
        assertFalse(format.isRecordingEmptyLines());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDelimiterSameAsQuoteChar() {
        CSVFormat.DEFAULT.withQuoteChar('"');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDelimiterSameAsCommentStart() {
        CSVFormat.DEFAULT.withCommentStart(',');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDelimiterSameAsEscape() {
        CSVFormat.DEFAULT.withEscape(',');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQuoteCharSameAsCommentStart() {
        CSVFormat.DEFAULT.withQuoteChar('#').withCommentStart('#');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQuoteCharSameAsEscape() {
        CSVFormat.DEFAULT.withQuoteChar('\\').withEscape('\\');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCommentStartSameAsEscape() {
        CSVFormat.DEFAULT.withCommentStart('#').withEscape('#');
    }

    @Test
    public void testWithMethods() {
        CSVFormat format = CSVFormat.DEFAULT
                .withDelimiter('|')
                .withQuoteChar('\'')
                .withQuotePolicy(CSVFormat.Quote.ALL)
                .withCommentStart('!')
                .withEscape('/')
                .withIgnoreSurroundingSpaces(true)
                .withIgnoreEmptyLines(false)
                .withRecordSeparator("\r\n")
                .withHeader("A", "B", "C");

        assertEquals('|', format.getDelimiter());
        assertEquals(Character.valueOf('\''), format.getQuoteChar());
        assertEquals(CSVFormat.Quote.ALL, format.getQuotePolicy());
        assertEquals(Character.valueOf('!'), format.getCommentStart());
        assertEquals(Character.valueOf('/'), format.getEscape());
        assertTrue(format.isSurroundingSpacesIgnored());
        assertFalse(format.isRecordingEmptyLines());
        assertEquals("\r\n", format.getRecordSeparator());
        assertNotNull(format.getHeader());
        assertEquals(3, format.getHeader().length);
    }

    @Test
    public void testEqualsAndHashCode() {
        CSVFormat f1 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat f2 = CSVFormat.DEFAULT.withDelimiter(',');
        CSVFormat f3 = CSVFormat.DEFAULT.withDelimiter(';');

        assertEquals(f1, f1);
        assertEquals(f1, f2);
        assertEquals(f1.hashCode(), f2.hashCode());
        assertFalse(f1.equals(f3));
        assertFalse(f1.equals(null));
        assertFalse(f1.equals("SomeString"));
    }

    @Test
    public void testToString() {
        String str = CSVFormat.DEFAULT.toString();
        assertNotNull(str);
        assertTrue(str.length() > 0);
    }

    @Test
    public void testFormatMethods() {
        String formatted = CSVFormat.DEFAULT.format("a", "b", "c");
        assertEquals("a,b,c", formatted);
    }

    @Test
    public void testPrintObject() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat.DEFAULT.print("a", sw, true);
        assertEquals("a", sw.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateInvalidDelimiter() {
        // Line feed or carriage return as delimiter should fail
        CSVFormat.DEFAULT.withDelimiter('\n');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateInvalidDelimiterCR() {
        CSVFormat.DEFAULT.withDelimiter('\r');
    }
}