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
package org.apache.commons.compress.archivers.zip;

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
import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class ZipArchiveInputStreamTest {

    @Test
    public void testMatches() {
        byte[] validSignature = new byte[] {
            (byte) 'P', (byte) 'K', 0x03, 0x04, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        };
        assertTrue(ZipArchiveInputStream.matches(validSignature, validSignature.length));

        byte[] invalidSignature = new byte[] {
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12
        };
        org.junit.Assert.assertFalse(ZipArchiveInputStream.matches(invalidSignature, invalidSignature.length));

        org.junit.Assert.assertFalse(ZipArchiveInputStream.matches(validSignature, 3));
        org.junit.Assert.assertFalse(ZipArchiveInputStream.matches(null, 4));
    }

    @Test
    public void testReadNullByteArray() throws IOException {
        byte[] empty = new byte[0];
        try (ZipArchiveInputStream zais = new ZipArchiveInputStream(new ByteArrayInputStream(empty))) {
            try {
                zais.read(null, 0, 1);
                fail("Expected NullPointerException");
            } catch (NullPointerException expected) {
                // expected
            }
        }
    }

    @Test
    public void testReadWithInvalidOffsetAndLength() throws IOException {
        byte[] empty = new byte[0];
        try (ZipArchiveInputStream zais = new ZipArchiveInputStream(new ByteArrayInputStream(empty))) {
            byte[] buf = new byte[10];
            try {
                zais.read(buf, -1, 1);
                fail("Expected IndexOutOfBoundsException");
            } catch (IndexOutOfBoundsException expected) {
                // expected
            }

            try {
                zais.read(buf, 0, -1);
                fail("Expected IndexOutOfBoundsException");
            } catch (IndexOutOfBoundsException expected) {
                // expected
            }

            try {
                zais.read(buf, 0, 11);
                fail("Expected IndexOutOfBoundsException");
            } catch (IndexOutOfBoundsException expected) {
                // expected
            }

            assertEquals(0, zais.read(buf, 0, 0));
        }
    }

    @Test
    public void testEmptyStream() throws IOException {
        byte[] empty = new byte[0];
        try (ZipArchiveInputStream zais = new ZipArchiveInputStream(new ByteArrayInputStream(empty))) {
            assertNull(zais.getNextZipEntry());
        }
    }

    @Test
    public void testCloseIdempotency() throws IOException {
        byte[] empty = new byte[0];
        ZipArchiveInputStream zais = new ZipArchiveInputStream(new ByteArrayInputStream(empty));
        zais.close();
        zais.close(); // Should not throw
    }

    @Test
    public void testUncompressedLocalFileHeaderAndData() throws Exception {
        // Construct a minimal valid ZIP archive containing one small file entry
        // Local file header signature: 0x04034b50 (PK\x03\x04)
        // Version needed: 20 (0x0014)
        // General purpose bit flag: 0
        // Compression method: 0 (STORED)
        // Last mod time/date: 0
        // CRC-32: 0xbc26d5cc (for "test")
        // Compressed size: 4
        // Uncompressed size: 4
        // File name length: 4 ("test")
        // Extra field length: 0
        
        byte[] zipData = new byte[] {
            // Local File Header
            0x50, 0x4b, 0x03, 0x04, // signature
            0x14, 0x00,             // version needed to extract
            0x00, 0x00,             // general purpose bit flag
            0x00, 0x00,             // compression method (STORED)
            0x00, 0x00, 0x00, 0x00, // file last modification time & date
            (byte) 0xcc, (byte) 0xd5, 0x26, (byte) 0xbc, // CRC-32
            0x04, 0x00, 0x00, 0x00, // compressed size
            0x04, 0x00, 0x00, 0x00, // uncompressed size
            0x04, 0x00,             // file name length
            0x00, 0x00,             // extra field length
            // File name
            't', 'e', 's', 't',
            // File data
            'D', 'a', 't', 'a',
            // Central Directory can be appended or omitted depending on parser; 
            // ZipArchiveInputStream relies on local file headers.
            // Let's also add Central Directory to fully exercise parsing if needed, 
            // but ZipArchiveInputStream reads up to central directory via local headers.
            
            // Central directory header signature: 0x02014b50
            0x50, 0x4b, 0x01, 0x02,
            0x14, 0x00, 0x14, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            (byte) 0xcc, (byte) 0xd5, 0x26, (byte) 0xbc,
            0x04, 0x00, 0x00, 0x00,
            0x04, 0x00, 0x00, 0x00,
            0x04, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            't', 'e', 's', 't',
            
            // End of central directory signature: 0x06054b50
            0x50, 0x4b, 0x05, 0x06,
            0x00, 0x00, 0x00, 0x00,
            0x01, 0x00, 0x01, 0x00,
            0x2c, 0x00, 0x00, 0x00,
            0x30, 0x00, 0x00, 0x00,
            0x00, 0x00
        };

        try (ZipArchiveInputStream zais = new ZipArchiveInputStream(new ByteArrayInputStream(zipData))) {
            ZipArchiveEntry entry = zais.getNextZipEntry();
            assertNotNull(entry);
            assertEquals("test", entry.getName());
            assertEquals(4, entry.getSize());

            byte[] buf = new byte[10];
            int read = zais.read(buf, 0, 10);
            assertEquals(4, read);
            assertEquals('D', buf[0]);
            assertEquals('a', buf[1]);
            assertEquals('t', buf[2]);
            assertEquals('a', buf[3]);

            assertEquals(-1, zais.read(buf, 0, 1));
            assertNull(zais.getNextZipEntry());
        }
    }

    @Test
    public void testEncoding() throws IOException {
        byte[] empty = new byte[0];
        try (ZipArchiveInputStream zais = new ZipArchiveInputStream(new ByteArrayInputStream(empty), "UTF-8")) {
            assertEquals("UTF-8", zais.getEncoding());
        }
    }
}