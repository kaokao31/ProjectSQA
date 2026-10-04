package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.assertArrayEquals;
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
    public void testMatches() {
        byte[] header = new byte[512];
        // Tar magic is usually at offset 257 "ustar"
        header[257] = (byte) 'u';
        header[258] = (byte) 's';
        header[259] = (byte) 't';
        header[260] = (byte) 'a';
        header[261] = (byte) 'r';

        assertTrue(TarArchiveInputStream.matches(header, 512));
        assertFalse(TarArchiveInputStream.matches(new byte[10], 10));
    }

    @Test
    public void testConstructorDefaults() {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        try (TarArchiveInputStream tais = new TarArchiveInputStream(in)) {
            assertNotNull(tais);
            assertEquals(512, tais.getRecordSize());
        } catch (IOException e) {
            fail("IOException not expected");
        }
    }

    @Test
    public void testConstructorWithBlockSize() {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        try (TarArchiveInputStream tais = new TarArchiveInputStream(in, 1024)) {
            assertNotNull(tais);
            assertEquals(1024, tais.getRecordSize());
        } catch (IOException e) {
            fail("IOException not expected");
        }
    }

    @Test
    public void testConstructorWithEncoding() {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        try (TarArchiveInputStream tais = new TarArchiveInputStream(in, "UTF-8")) {
            assertNotNull(tais);
        } catch (IOException e) {
            fail("IOException not expected");
        } 
    }

    @Test
    public void testConstructorWithBlockSizeAndEncoding() {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        try (TarArchiveInputStream tais = new TarArchiveInputStream(in, 1024, "UTF-8")) {
            assertNotNull(tais);
            assertEquals(1024, tais.getRecordSize());
        } catch (IOException e) {
            fail("IOException not expected");
        }
    }

    @Test
    public void testConstructorWithBufferSizeAndEncoding() {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        try (TarArchiveInputStream tais = new TarArchiveInputStream(in, 2048, 1024, "UTF-8")) {
            assertNotNull(tais);
        } catch (IOException e) {
            fail("IOException not expected");
        }
    }

    @Test
    public void testClose() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tais = new TarArchiveInputStream(in);
        tais.close();
        // Subsequent reads or operations might behave differently, but close should not throw.
    }

    @Test
    public void testAvailable() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[100]);
        try (TarArchiveInputStream tais = new TarArchiveInputStream(in)) {
            assertTrue(tais.available() >= 0);
        }
    }

    @Test
    public void testSkip() throws IOException {
        byte[] data = new byte[1024];
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        try (TarArchiveInputStream tais = new TarArchiveInputStream(in)) {
            long skipped = tais.skip(10);
            assertEquals(10, skipped);
        }
    }

    @Test
    public void testMarkSupported() {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        try (TarArchiveInputStream tais = new TarArchiveInputStream(in)) {
            assertFalse(tais.markSupported());
        } catch (IOException e) {
            fail("IOException not expected");
        }
    }

    @Test
    public void testMarkAndReset() {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        try (TarArchiveInputStream tais = new TarArchiveInputStream(in)) {
            tais.mark(10);
            try {
                tais.reset();
                fail("IOException expected");
            } catch (IOException e) {
                // expected
            }
        } catch (IOException e) {
            fail("IOException not expected");
        }
    }

    @Test
    public void testGetNextTarEntryEmpty() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        try (TarArchiveInputStream tais = new TarArchiveInputStream(in)) {
            assertNull(tais.getNextTarEntry());
        }
    }

    @Test
    public void testReadWithNoCurrentEntry() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[100]);
        try (TarArchiveInputStream tais = new TarArchiveInputStream(in)) {
            byte[] buf = new byte[10];
            int read = tais.read(buf, 0, 10);
            assertEquals(-1, read);
        }
    }

    @Test
    public void testParsePaxHeaders() {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        try (TarArchiveInputStream tais = new TarArchiveInputStream(in)) {
            ByteArrayInputStream paxStream = new ByteArrayInputStream("10 path=foo\n".getBytes());
            Map<String, String> headers = tais.parsePaxHeaders(paxStream);
            assertNotNull(headers);
            assertEquals("foo", headers.get("path"));
        } catch (Exception e) {
            fail("Exception not expected: " + e.getMessage());
        }
    }

    @Test
    public void testGetLongNameData() {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        try (TarArchiveInputStream tais = new TarArchiveInputStream(in)) {
            assertNull(tais.getLongNameData());
        } catch (IOException e) {
            // expected or handled
        }
    }

    @Test
    public void testCurrentBlockNumAndRow() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[1024]);
        try (TarArchiveInputStream tais = new TarArchiveInputStream(in)) {
            assertEquals(0, tais.getCurrentBlockNum());
            assertEquals(0, tais.getCurrentRecordNum());
        }
    }
}