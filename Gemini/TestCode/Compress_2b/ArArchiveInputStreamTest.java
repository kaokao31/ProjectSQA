/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * licenses this file to the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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
package org.apache.commons.compress.archivers.ar;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class ArArchiveInputStreamTest {

    @Test
    public void testMatches() {
        byte[] valid = { '!', '<', 'a', 'r', 'c', 'h', '>', '\n' };
        assertTrue(ArArchiveInputStream.matches(valid, valid.length));

        byte[] invalid = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h' };
        org.junit.Assert.assertFalse(ArArchiveInputStream.matches(invalid, invalid.length));

        org.junit.Assert.assertFalse(ArArchiveInputStream.matches(valid, 4));
        org.junit.Assert.assertFalse(ArArchiveInputStream.matches(null, 8));
    }

    @Test
    public void testEmptyStream() throws Exception {
        byte[] empty = new byte[0];
        ArArchiveInputStream in = new ArArchiveInputStream(new ByteArrayInputStream(empty));
        assertNull(in.getNextArEntry());
        in.close();
    }

    @Test(expected = IOException.class)
    public void testInvalidHeader() throws Exception {
        byte[] badHeader = { '!', '<', 'b', 'a', 'd', '>', '\n', 0 };
        ArArchiveInputStream in = new ArArchiveInputStream(new ByteArrayInputStream(badHeader));
        in.getNextArEntry();
        in.close();
    }

    @Test
    public void testReadArchiveWithMultipleEntries() throws Exception {
        StringBuilder sb = new StringBuilder();
        // Global header
        sb.append("!<arch>\n");
        
        // Entry 1: name (16), last modified (12), uid (6), gid (6), mode (8), size (10), trailer (2)
        // Total header size = 60 bytes
        // Header 1
        String header1 = pad("file1.txt", 16) +
                         pad("123456789", 12) +
                         pad("0", 6) +
                         pad("0", 6) +
                         pad("100644", 8) +
                         pad("5", 10) +
                         "`\n";
        sb.append(header1);
        sb.append("hello"); // 5 bytes

        // Entry 2
        String header2 = pad("file2.txt", 16) +
                         pad("123456789", 12) +
                         pad("0", 6) +
                         pad("0", 6) +
                         pad("100644", 8) +
                         pad("3", 10) +
                         "`\n";
        sb.append(header2);
        sb.append("world"); // 3 bytes (actually requested size 3)

        byte[] data = sb.toString().getBytes("US-ASCII");
        ArArchiveInputStream in = new ArArchiveInputStream(new ByteArrayInputStream(data));

        ArArchiveEntry entry1 = in.getNextArEntry();
        assertNotNull(entry1);
        assertEquals("file1.txt", entry1.getName());
        assertEquals(5, entry1.getSize());

        byte[] buf = new byte[10];
        int read = in.read(buf, 0, 5);
        assertEquals(5, read);
        assertArrayEquals("hello".getBytes("US-ASCII"), java.util.Arrays.copyOf(buf, 5));

        ArArchiveEntry entry2 = in.getNextArEntry();
        assertNotNull(entry2);
        assertEquals("file2.txt", entry2.getName());
        assertEquals(3, entry2.getSize());

        read = in.read();
        assertEquals('w', read);

        assertNull(in.getNextArEntry());
        in.close();
    }

    @Test
    public void testReadWithOffsetAndLength() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("!<arch>\n");
        String header = pad("test.txt", 16) +
                        pad("123", 12) +
                        pad("0", 6) +
                        pad("0", 6) +
                        pad("100644", 8) +
                        pad("4", 10) +
                        "`\n";
        sb.append(header);
        sb.append("abcd");

        ArArchiveInputStream in = new ArArchiveInputStream(new ByteArrayInputStream(sb.toString().getBytes("US-ASCII")));
        assertNotNull(in.getNextArEntry());

        byte[] buffer = new byte[10];
        int read = in.read(buffer, 2, 2);
        assertEquals(2, read);
        assertEquals('a', buffer[2]);
        assertEquals('b', buffer[3]);

        in.close();
    }

    @Test(expected = NullPointerException.class)
    public void testNullConstructor() throws Exception {
        new ArArchiveInputStream(null);
    }

    private String pad(String s, int length) {
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < length) {
            sb.append(' ');
        }
        return sb.toString();
    }
}