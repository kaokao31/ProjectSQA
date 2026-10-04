/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND,  either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.junit.Test;

public class TarArchiveInputStreamTest {

    @Test
    public void testConstructorDefaults() {
        byte[] empty = new byte[0];
        ByteArrayInputStream in = new ByteArrayInputStream(empty);
        TarArchiveInputStream tais = new TarArchiveInputStream(in);
        assertNotNull(tais);
    }

    @Test
    public void testConstructorWithBlockSize() {
        byte[] empty = new byte[0];
        ByteArrayInputStream in = new ByteArrayInputStream(empty);
        TarArchiveInputStream tais = new TarArchiveInputStream(in, 512);
        assertNotNull(tais);
    }

    @Test
    public void testConstructorWithRecordSize() {
        byte[] empty = new byte[0];
        ByteArrayInputStream in = new ByteArrayInputStream(empty);
        TarArchiveInputStream tais = new TarArchiveInputStream(in, 1024, 512);
        assertNotNull(tais);
    }

    @Test
    public void testConstructorWithEncoding() {
        byte[] empty = new byte[0];
        ByteArrayInputStream in = new ByteArrayInputStream(empty);
        TarArchiveInputStream tais = new TarArchiveInputStream(in, "UTF-8");
        assertNotNull(tais);
    }

    @Test
    public void testConstructorWithBlockSizeAndEncoding() {
        byte[] empty = new byte[0];
        ByteArrayInputStream in = new ByteArrayInputStream(empty);
        TarArchiveInputStream tais = new TarArchiveInputStream(in, 512, "UTF-8");
        assertNotNull(tais);
    }

    @Test
    public void testConstructorWithRecordSizeAndEncoding() {
        byte[] empty = new byte[0];
        ByteArrayInputStream in = new ByteArrayInputStream(empty);
        TarArchiveInputStream tais = new TarArchiveInputStream(in, 1024, 512, "UTF-8");
        assertNotNull(tais);
    }

    @Test
    public void testMatches() {
        byte[] signature = "ustar\0".getBytes();
        byte[] header = new byte[512];
        System.arraycopy(signature, 0, header, 257, signature.length);
        
        boolean matches = TarArchiveInputStream.matches(header, header.length);
        assertTrue(matches);

        assertFalse(TarArchiveInputStream.matches(new byte[10], 10));
    }

    private boolean assertFalse(boolean condition) {
        org.junit.Assert.assertFalse(condition);
        return condition;
    }

    @Test
    public void testClose() throws Exception {
        byte[] empty = new byte[0];
        ByteArrayInputStream in = new ByteArrayInputStream(empty);
        TarArchiveInputStream tais = new TarArchiveInputStream(in);
        tais.close();
        // Second close should be safe
        tais.close();
    }

    @Test
    public void testAvailable() throws Exception {
        byte[] empty = new byte[0];
        ByteArrayInputStream in = new ByteArrayInputStream(empty);
        TarArchiveInputStream tais = new TarArchiveInputStream(in);
        assertEquals(0, tais.available());
    }

    @Test
    public void testSkip() throws Exception {
        byte[] empty = new byte[0];
        ByteArrayInputStream in = new ByteArrayInputStream(empty);
        TarArchiveInputStream tais = new TarArchiveInputStream(in);
        assertEquals(0, tais.skip(10));
    }

    @Test
    public void testMarkSupported() {
        byte[] empty = new byte[0];
        ByteArrayInputStream in = new ByteArrayInputStream(empty);
        TarArchiveInputStream tais = new TarArchiveInputStream(in);
        assertFalse(tais.markSupported());
    }

    @Test
    public void testGetAndSetCurrentReset() throws Exception {
        byte[] empty = new byte[0];
        ByteArrayInputStream in = new ByteArrayInputStream(empty);
        TarArchiveInputStream tais = new TarArchiveInputStream(in);
        assertNull(tais.getCurrentEntry());
    }

    @Test
    public void testReadWithNoCurrentEntry() throws Exception {
        byte[] empty = new byte[0];
        ByteArrayInputStream in = new ByteArrayInputStream(empty);
        TarArchiveInputStream tais = new TarArchiveInputStream(in);
        byte[] buf = new byte[10];
        assertEquals(-1, tais.read(buf, 0, 10));
    }

    @Test
    public void testParsePaxHeaderWithEmptyData() throws Exception {
        byte[] empty = new byte[0];
        ByteArrayInputStream in = new ByteArrayInputStream(empty);
        TarArchiveInputStream tais = new TarArchiveInputStream(in);
        Map<String, String> headers = tais.parsePaxHeader(in);
        assertNotNull(headers);
        assertTrue(headers.isEmpty());
    }

    @Test
    public void testParsePaxHeaderWithValidEntry() throws Exception {
        String paxData = "11 path=foo/bar\n";
        ByteArrayInputStream in = new ByteArrayInputStream(paxData.getBytes("UTF-8"));
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        Map<String, String> headers = tais.parsePaxHeader(in);
        assertEquals("foo/bar", headers.get("path"));
    }
}