/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.dll
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

/**
 * Comprehensive test suite for CSVPrinter to maximize code coverage and catch Defects4J-style bugs.
 */
public class CSVPrinterTest {

    @Test
    public void testBasicPrint() throws IOException {
        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT)) {
            printer.print("a");
            printer.print("b");
            printer.println();
        }
        assertEquals("a,b\r\n", sw.toString());
    }

    @Test
    public void testPrintRecord() throws IOException {
        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT)) {
            printer.printRecord("a", "b", "c");
        }
        assertEquals("a,b,c\r\n", sw.toString());
    }

    @Test
    public void testPrintRecordIterable() throws IOException {
        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT)) {
            printer.printRecord(Arrays.asList("x", "y", "z"));
        }
        assertEquals("x,y,z\r\n", sw.toString());
    }

    @Test
    public void testPrintRecordsWithVariousTypes() throws IOException {
        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT)) {
            printer.printRecords(
                Arrays.asList("a", "b"),
                Arrays.asList("c", "d")
            );
        }
        assertEquals("a\r\nb\r\nc\r\nd\r\n", sw.toString());
    }

    @Test
    public void testPrintln() throws IOException {
        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT)) {
            printer.println();
        }
        assertEquals("\r\n", sw.toString());
    }

    @Test
    public void testComment() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withCommentStart('#');
        try (CSVPrinter printer = new CSVPrinter(sw, format)) {
            printer.printComment("This is a comment");
        }
        assertEquals("# This is a comment\r\n", sw.toString());
    }

    @Test
    public void testCommentMultiline() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withCommentStart('#');
        try (CSVPrinter printer = new CSVPrinter(sw, format)) {
            printer.printComment("Line 1\nLine 2\r\nLine 3");
        }
        assertEquals("# Line 1\r\n# Line 2\r\n# Line 3\r\n", sw.toString());
    }

    @Test
    public void testNullValues() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        try (CSVPrinter printer = new CSVPrinter(sw, format)) {
            printer.printRecord((Object) null, "a", null);
        }
        assertEquals("NULL,a,NULL\r\n", sw.toString());
    }

    @Test
    public void testNullValueWithoutNullString() throws IOException {
        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT)) {
            printer.printRecord((Object) null);
        }
        assertEquals("\r\n", sw.toString());
    }

    @Test
    public void testQuoteAll() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuotePolicy(Quote.ALL);
        try (CSVPrinter printer = new CSVPrinter(sw, format)) {
            printer.printRecord("a", "b,c", "d");
        }
        assertEquals("\"a\",\"b,c\",\"d\"\r\n", sw.toString());
    }

    @Test
    public void testQuoteNonNumeric() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withQuotePolicy(Quote.NON_NUMERIC);
        try (CSVPrinter printer = new CSVPrinter(sw, format)) {
            printer.printRecord("123", "abc");
        }
        assertEquals("123,\"abc\"\r\n", sw.toString());
    }

    @Test
    public void testEscapeCharacter() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\').withQuote(null);
        try (CSVPrinter printer = new CSVPrinter(sw, format)) {
            printer.print("a,b\\c");
        }
        assertEquals("a\\,b\\\\c", sw.toString());
    }

    @Test
    public void testGetOut() throws IOException {
        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT)) {
            assertNotNull(printer.getOut());
        }
    }

    @Test
    public void testPrintAndFlush() throws IOException {
        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT)) {
            printer.print("flushTest");
            printer.flush();
        }
        assertEquals("flushTest", sw.toString());
    }
}