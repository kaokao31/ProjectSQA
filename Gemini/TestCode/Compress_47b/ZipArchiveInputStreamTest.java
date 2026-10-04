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
 * KIND,  either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.commons.compress.archivers.zip;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;

import static org.junit.Assert.*;

public class ZipArchiveInputStreamTest {

    @Test
    public void testMatchesValidSignature() {
        // Zip local file header signature: PK\003\004
        byte[] validHeader = new byte[] { 0x50, 0x4b, 0x03, 0x04, 0x0a, 0x00, 0x00, 0x00, 0x00, 0x00 };
        assertTrue(ZipArchiveInputStream.matches(validHeader, validHeader.length));
    }

    @Test
    public void testMatchesInvalidSignature() {
        byte[] invalidHeader = new byte[] { 0x00, 0x00, 0x03, 0x04, 0x0a, 0x00, 0x00, 0x00, 0x00, 0x00 };
        assertFalse(ZipArchiveInputStream.matches(invalidHeader, invalidHeader.length));
    }

    @Test
    public void testMatchesTooShort() {
        byte[] shortHeader = new byte[] { 0x50, 0x4b };
        assertFalse(ZipArchiveInputStream.matches(shortHeader, shortHeader.length));
    }

    @Test
    public void testNullInputStreamRead() {
        try {
            ZipArchiveInputStream zais = new ZipArchiveInputStream(null);
            fail("Expected NullPointerException or IllegalArgumentException");
        } catch (Exception e) {
            // Expected exception depending on constructor implementation
        }
    }

    @Test
    public void testEmptyStreamGetNextZipEntry() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        try (ZipArchiveInputStream zais = new ZipArchiveInputStream(bais)) {
            assertNull(zais.getNextZipEntry());
        }
    }

    @Test
    public void testCloseDoesNotThrow() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        ZipArchiveInputStream zais = new ZipArchiveInputStream(bais);
        zais.close();
        // Calling close multiple times should be safe
        zais.close();
    }
}