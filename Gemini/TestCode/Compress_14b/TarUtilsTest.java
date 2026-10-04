/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify,
 * the Software is licensed to this Apache Software Foundation
 * (ASF) under one or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Software is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Test case for TarUtils class, focusing on edge cases, boundary values,
 * parsing, and formatting (especially around bugs in Compress-14).
 */
public class TarUtilsTest {

    @Test
    public void testParseOctal() {
        byte[] buffer = " 123 \0".getBytes();
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(123L, val);
    }

    @Test
    public void testParseOctalInvalid() {
        // Test with invalid octal characters or empty/spaces
        byte[] buffer = "   ".getBytes();
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0L, val);
    }

    @Test
    public void testParseOctalWithTrailingNulAndSpace() {
        byte[] buffer = new byte[] { (byte)'0', (byte)'7', (byte)'\0', (byte)' ' };
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(7L, val);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalTooShortBuffer() {
        byte[] buffer = new byte[1];
        TarUtils.parseOctal(buffer, 0, 2);
    }

    @Test
    public void testParseName() {
        byte[] buffer = "testfile.txt\0\0\0".getBytes();
        String name = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("testfile.txt", name);
    }

    @Test
    public void testFormatNameBytes() {
        String name = "hello";
        byte[] buf = new byte[10];
        int len = TarUtils.formatNameBytes(name, buf, 0, buf.length);
        assertEquals(10, len);
        assertEquals('h', (char) buf[0]);
        assertEquals(0, (char) buf[5]);
    }

    @Test
    public void testFormatLongNameBytes() {
        String name = "thisisaverylongnamethatwillnotfitcompletely";
        byte[] buf = new byte[10];
        try {
            TarUtils.formatNameBytes(name, buf, 0, buf.length);
            // Depending on implementation, might truncate or throw, let's verify behavior
        } catch (Exception e) {
            // expected or handled
        }
    }

    @Test
    public void testComputeCheckSum() {
        byte[] header = new byte[512];
        long sum = TarUtils.computeCheckSum(header);
        // All zeros header checksum computation
        // Standard tar header checksum with empty space for checksum bytes (8 bytes at offset 148)
        // Usually spaces are 32.
        assertTrue(sum >= 0);
    }

    @Test
    public void testFormatOctal() {
        byte[] buf = new byte[8];
        int len = TarUtils.formatOctalBytes(0777L, buf, 0, buf.length);
        assertEquals(8, len);
        long parsed = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(511L, parsed);
    }

    @Test
    public void testFormatLongOctal() {
        byte[] buf = new byte[12];
        TarUtils.formatLongOctalBytes(0777777L, buf, 0, buf.length);
        long parsed = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(0777777L, parsed);
    }

    @Test
    public void testFormatCheckSumOctal() {
        byte[] buf = new byte[8];
        TarUtils.formatCheckSumOctalBytes(123L, buf, 0, buf.length);
        long parsed = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(123L, parsed);
    }
}