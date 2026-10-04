/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * licenses this file to you under the Apache License, Version 2.0 (the
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
package org.apache.commons.compress.archivers.tar;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static org.junit.Assert.*;

public class TarArchiveOutputStreamTest {

    @Test
    public void testLongNamePaxHeaderAndTruncation() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);

        // Create a very long file name that exceeds standard tar name length (100 chars)
        // but can be stored in a PAX extended header.
        StringBuilder longNameBuilder = new StringBuilder();
        for (int i = 0; i < 150; i++) {
            longNameBuilder.append("a");
        }
        String longName = longNameBuilder.toString() + "/file.txt";

        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(5);

        tos.putArchiveEntry(entry);
        tos.write(new byte[] {1, 2, 3, 4, 5});
        tos.closeArchiveEntry();
        tos.finish();
        tos.close();

        assertTrue(bos.size() > 0);
    }

    @Test(expected = RuntimeException.class)
    public void testLongNameErrorMode() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_ERROR);

        StringBuilder longNameBuilder = new StringBuilder();
        for (int i = 0; i < 150; i++) {
            longNameBuilder.append("a");
        }
        String longName = longNameBuilder.toString() + "/file.txt";

        TarArchiveEntry entry = new TarArchiveEntry(longName);
        tos.putArchiveEntry(entry);
    }

    @Test
    public void testLongNameTruncateMode() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_TRUNCATE);

        StringBuilder longNameBuilder = new StringBuilder();
        for (int i = 0; i < 150; i++) {
            longNameBuilder.append("a");
        }
        String longName = longNameBuilder.toString() + "/file.txt";

        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();
        
        assertTrue(bos.size() > 0);
    }

    @Test
    public void testCreateArchiveEntryFile() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        File tempFile = File.createTempFile("commons-compress-test", ".txt");
        tempFile.deleteOnExit();

        ArchiveEntry entry = tos.createArchiveEntry(tempFile, "test-entry-name");
        assertNotNull(entry);
        assertEquals("test-entry-name", entry.getName());

        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();
        tos.close();
    }

    @Test
    public void testWriteByteArrayOffset() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        byte[] data = "Hello, World!".getBytes("UTF-8");
        entry.setSize(data.length);

        tos.putArchiveEntry(entry);
        tos.write(data, 0, data.length);
        tos.closeArchiveEntry();
        tos.finish();
        tos.close();

        assertTrue(bos.size() > 0);
    }

    @Test(expected = IOException.class)
    public void testWriteExceedSize() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(2);

        tos.putArchiveEntry(entry);
        tos.write(new byte[] {1, 2, 3, 4, 5}); // Exceeds declared size of 2
    }

    @Test
    public void testAddPaxHeadersExplicitly() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("pax.txt");
        entry.setSize(0);

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("comment", "test-comment");
        
        tos.writePaxHeaders(entry, "PaxHeader", headers);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();
        tos.close();

        assertTrue(bos.size() > 0);
    }
}