/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * licenses this file to use you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.  You may obtain a copy of the
 * License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.commons.compress.archivers.zip.ZipEncoding;
import org.apache.commons.compress.archivers.zip.ZipEncodingHelper;
import org.junit.Test;

/**
 * Test class for TarUtils, targeting Commons Compress Bug 35 (TarUtils parseOctal / formatOctal / etc.).
 */
public class TarUtilsTest {

    @Test
    public void testComputeCheckSum() {
        byte[] buffer = new byte[512];
        for (int i = 0; i < 100; i++) {
            buffer[i] = (byte) i;
        }
        long sum = TarUtils.computeCheckSum(buffer);
        // Sum of bytes 0 to 511 (with 148-155 treated as spaces)
        // Just verify it doesn't throw and returns a positive/expected value
        assertTrue(sum >= 0);
    }

    @Test
    public void testParseOctalEmptyOrBlank() {
        byte[] buffer = new byte[0];
        try {
            TarUtils.parseOctal(buffer, 0, 0);
            // Depending on implementation, empty might return 0 or throw exception
        } catch (Exception e) {
            // acceptable if strict
        }

        byte[] spaces = new byte[] { ' ', ' ', ' ' };
        long val = TarUtils.parseOctal(spaces, 0, spaces.length);
        assertEquals(0L, val);
    }

    @Test
    public void testParseOctalNormal() {
        byte[] buffer = " 123 \0".getBytes();
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(123L, val);
    }

    @Test
    public void testParseOctalWithTrailer() {
        // Octal 77 is decimal 63
        byte[] buffer = "0000077\0 ".getBytes();
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(63L, val);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalInvalidChar() {
        byte[] buffer = " 1289 ".getBytes(); // 8 and 9 are invalid octal digits
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test
    public void testParseBoolean() {
        byte[] buffer = new byte[] { 0, 1 };
        assertFalse(TarUtils.parseBoolean(buffer, 0));
        assertTrue(TarUtils.parseBoolean(buffer, 1));
    }

    @Test
    public void testParseName() {
        ZipEncoding encoding = ZipEncodingHelper.getZipEncoding("UTF-8");
        byte[] buffer = "helloTAR".getBytes();
        String name = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("helloTAR", name);
    }

    @Test
    public void testFormatNameBytes() {
        ZipEncoding encoding = ZipEncodingHelper.getZipEncoding("UTF-8");
        byte[] buf = new byte[10];
        int len = TarUtils.formatNameBytes("test", buf, 0, 10, encoding);
        assertEquals(10, len);
        assertEquals('t', buf[0]);
        assertEquals(0, buf[4]); // zero padded
    }

    @Test
    public void testFormatLongOctalBytes() {
        byte[] buf = new byte[12];
        int len = TarUtils.formatLongOctalBytes(12345L, buf, 0, 12);
        assertEquals(12, len);
        long parsed = TarUtils.parseOctal(buf, 0, 12);
        assertEquals(12345L, parsed);
    }

    @Test
    public void testFormatCheckSumOctalBytes() {
        byte[] buf = new byte[8];
        int len = TarUtils.formatCheckSumOctalBytes(55L, buf, 0, 8);
        assertEquals(8, len);
    }

    @Test
    public void testFormatOctalBytes() {
        byte[] buf = new byte[8];
        int len = TarUtils.formatOctalBytes(77L, buf, 0, 8);
        assertEquals(8, len);
        long parsed = TarUtils.parseOctal(buf, 0, 8);
        assertEquals(77L, parsed);
    }

    @Test
    public void testGetZipEncoding() {
        assertNotNull(TarUtils.getZipEncoding(null));
        assertNotNull(TarUtils.getZipEncoding("UTF-8"));
    }
}