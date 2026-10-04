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
package org.apache.commons.compress.archivers.sevenz;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SevenZOutputFileTest {

    private File tempFile;

    @Before
    public void setUp() throws IOException {
        tempFile = File.createTempFile("sevenZTest", ".7z");
        tempFile.deleteOnExit();
    }

    @After
    public void tearDown() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    public void testCreateAndCloseEmptyArchive() throws IOException {
        SevenZOutputFile out = new SevenZOutputFile(tempFile);
        out.close();
        Assert.assertTrue(tempFile.exists());
        Assert.assertTrue(tempFile.length() > 0);
    }

    @Test
    public void testAddArchiveEntryAndWriteData() throws IOException {
        SevenZOutputFile out = new SevenZOutputFile(tempFile);
        
        SevenZArchiveEntry entry = out.createArchiveEntry(new File("some-dummy-file"), "test.txt");
        entry.setHasLastModifiedDate(true);
        entry.setLastModifiedDate(new Date());
        
        out.putArchiveEntry(entry);
        byte[] data = "Hello, SevenZ World!".getBytes("UTF-8");
        out.write(data);
        out.closeArchiveEntry();
        
        out.finish();
        out.close();

        Assert.assertTrue(tempFile.length() > 0);
    }

    @Test
    public void testWriteMultipleEntriesAndSingleBytes() throws IOException {
        SevenZOutputFile out = new SevenZOutputFile(tempFile);

        // Entry 1: Write using write(byte[], int, int)
        SevenZArchiveEntry entry1 = out.createArchiveEntry(new File("dummy1"), "file1.txt");
        out.putArchiveEntry(entry1);
        byte[] data1 = "Data for file 1".getBytes("UTF-8");
        out.write(data1, 0, data1.length);
        out.closeArchiveEntry();

        // Entry 2: Write using write(int)
        SevenZArchiveEntry entry2 = out.createArchiveEntry(new File("dummy2"), "file2.txt");
        out.putArchiveEntry(entry2);
        out.write('A');
        out.write('B');
        out.write('C');
        out.closeArchiveEntry();

        out.finish();
        out.close();
        
        Assert.assertTrue(tempFile.exists());
    }

    @Test
    public void testCustomContentMethods() throws IOException {
        SevenZOutputFile out = new SevenZOutputFile(tempFile);
        out.setContentMethods(new SevenZMethodConfiguration[] {
            new SevenZMethodConfiguration(SevenZMethod.COPY)
        });

        SevenZArchiveEntry entry = out.createArchiveEntry(new File("dummy3"), "copy.txt");
        out.putArchiveEntry(entry);
        out.write("Copy method content".getBytes("UTF-8"));
        out.closeArchiveEntry();

        out.finish();
        out.close();
        Assert.assertTrue(tempFile.exists());
    }
}