/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.commons.compress.archivers.zip.ZipEncoding;
import org.apache.commons.compress.archivers.zip.ZipEncodingHelper;
import org.junit.Test;

/**
 * Test case for TarUtils, focusing on parsing/formatting octal, long, and boolean values,
 * name/string handling, and handling edge cases like trailing spaces, null bytes, and invalid lengths.
 */
public class TarUtilsTest {

    @Test
    public void testComputeCheckSum() {
        byte[] buf = new byte[512];
        long sum = TarUtils.computeCheckSum(buf);
        assertEquals(0L, sum);

        for (int i = 0; i < buf.length; i++) {
            buf[i] = 1;
        }
        sum = TarUtils.computeCheckSum(buf);
        assertEquals(512L, sum);
    }

    @Test
    public void testParseName() {
        byte[] buffer = "hello world\0\0".getBytes();
        String name = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("hello world", name);

        // Test with encoding
        ZipEncoding encoding = ZipEncodingHelper.getZipEncoding("UTF-8");
        String nameEnc = TarUtils.parseName(buffer, 0, 5, encoding);
        assertEquals("hello", nameEnc);
    }

    @Test
    public void testFormatNameBytes() {
        byte[] buf = new byte[10];
        int len = TarUtils.formatNameBytes("test", buf, 0, 10);
        assertEquals(10, len);
        assertEquals('t', buf[0]);
        assertEquals(0, buf[4]);

        ZipEncoding encoding = ZipEncodingHelper.getZipEncoding("UTF-8");
        len = TarUtils.formatNameBytes("foo", buf, 0, 5, encoding);
        assertEquals(5, len);
        assertEquals('f', buf[0]);
        assertEquals(0, buf[3]);
    }

    @Test
    public void testParseCheckSumOctal() {
        byte[] buffer = " 123 \0".getBytes();
        long val = TarUtils.parseCheckSumOctal(buffer, 0, buffer.length);
        assertEquals(83L, val); // 123 in octal is 83 decimal
    }

    @Test
    public void testParseOctal() {
        byte[] buffer = "0000123\0 ".getBytes();
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(83L, val);

        // Test invalid/empty/spaces octal
        byte[] spaces = "   ".getBytes();
        assertEquals(0L, TarUtils.parseOctal(spaces, 0, spaces.length));

        byte[] leadingSpace = " 123".getBytes();
        assertEquals(83L, TarUtils.parseOctal(leadingSpace, 0, leadingSpace.length));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalInvalidLength() {
        byte[] buffer = "123".getBytes();
        TarUtils.parseOctal(buffer, 0, 1); // length < 2 should throw exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalInvalidSyntax() {
        byte[] buffer = "1289".getBytes(); // 8 and 9 are not valid octal digits
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test
    public void testParseOctalOrBinary() {
        byte[] buffer = "0000123\0 ".getBytes();
        long val = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(83L, val);

        // Binary format test (highest bit set)
        byte[] binaryBuffer = new byte[8];
        binaryBuffer[0] = (byte) 0x80; // positive binary marker
        binaryBuffer[7] = 5;
        long binVal = TarUtils.parseOctalOrBinary(binaryBuffer, 0, binaryBuffer.length);
        assertEquals(5L, binVal);

        // Negative binary format test
        byte[] negBinaryBuffer = new byte[8];
        negBinaryBuffer[0] = (byte) 0xFF; 
        negBinaryBuffer[7] = 1;
        long negBinVal = TarUtils.parseOctalOrBinary(negBinaryBuffer, 0, negBinaryBuffer.length);
        assertTrue(negBinVal < 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalOrBinaryValueLengthExceeds() {
        byte[] binaryBuffer = new byte[2];
        binaryBuffer[0] = (byte) 0x80;
        // 8 bytes needed for long, but we provide only 2
        TarUtils.parseOctalOrBinary(binaryBuffer, 0, 2);
    }

    @Test
    public void testParseBoolean() {
        byte[] buffer = new byte[2];
        buffer[0] = 1;
        assertTrue(TarUtils.parseBoolean(buffer, 0));

        buffer[0] = 0;
        assertFalse(TarUtils.parseBoolean(buffer, 0));
    }

    @Test
    public void testFormatBoolean() {
        byte[] buffer = new byte[2];
        TarUtils.formatBoolean(true, buffer, 0);
        assertEquals(1, buffer[0]);

        TarUtils.formatBoolean(false, buffer, 0);
        assertEquals(0, buffer[0]);
    }

    @Test
    public void testFormatOctalBytes() {
        byte[] buffer = new byte[8];
        int len = TarUtils.formatOctalBytes(83L, buffer, 0, 8);
        assertEquals(8, len);
        String parsed = TarUtils.parseName(buffer, 0, 8);
        assertEquals("0123", parsed.trim());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatOctalBytesValueTooLarge() {
        byte[] buffer = new byte[2];
        TarUtils.formatOctalBytes(1000L, buffer, 0, 2);
    }

    @Test
    public void testFormatLongOctalBytes() {
        byte[] buffer = new byte[12];
        int len = TarUtils.formatLongOctalBytes(83L, buffer, 0, 12);
        assertEquals(12, len);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytes() {
        byte[] buffer = new byte[8];
        // Test normal octal formatting for small numbers
        int len = TarUtils.formatLongOctalOrBinaryBytes(83L, buffer, 0, 8);
        assertEquals(8, len);

        // Test binary formatting for numbers too large for octal string representation
        long largeVal = 0xFFFFFFFFFFL;
        int lenBin = TarUtils.formatLongOctalOrBinaryBytes(largeVal, buffer, 0, 8);
        assertEquals(8, lenBin);
        assertTrue((buffer[0] & 0x80) != 0); // Check binary flag
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalOrBinaryBytesTooLargeForBuffer() {
        byte[] buffer = new byte[1]; // too small for long
        TarUtils.formatLongOctalOrBinaryBytes(Long.MAX_VALUE, buffer, 0, 1);
    }

    @Test
    public void testFormatCheckSumOctalBytes() {
        byte[] buffer = new byte[8];
        TarUtils.formatCheckSumOctalBytes(100L, buffer, 0, 8);
        assertEquals('\0', buffer[7]);
        assertEquals(' ', buffer[6]);
    }
}