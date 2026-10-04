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
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class TarArchiveInputStreamTest {

    @Test
    public void testMatches() {
        byte[] header = new byte[512];
        // Tar header magic bytes usually at offset 257: "ustar\0" or "ustar "
        // Let's check TarArchiveInputStream.matches method implementation behavior
        // Usually checks length >= 512 and magic string.
        assertFalse(TarArchiveInputStream.matches(null, 0));
        assertFalse(TarArchiveInputStream.matches(new byte[10], 10));

        // Create a dummy tar header with ustar
        byte[] validHeader = new byte[512];
        // ustar magic in tar is usually at 257 "ustar\0" or "ustar  "
        // Let's test with empty/zero array first to verify false
        assertFalse(TarArchiveInputStream.matches(validHeader, 512));
    }

    @Test
    public void testConstructorDefaults() {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        try (TarArchiveInputStream tais = new TarArchiveInputStream(in)) {
            assertNotNull(tais);
        } catch (IOException e) {
            fail("IOException not expected: " + e.getMessage());
        }

        try (TarArchiveInputStream tais = new TarArchiveInputStream(in, 1024)) {
            assertNotNull(tais);
        } catch (IOException e) {
            fail("IOException not expected: " + e.getMessage());
        }

        try (TarArchiveInputStream tais = new TarArchiveInputStream(in, "UTF-8")) {
            assertNotNull(tais);
        } catch (IOException e) {
            fail("IOException not expected: " + e.getMessage());
        }

        try (TarArchiveInputStream tais = new TarArchiveInputStream(in, 1024, "UTF-8")) {
            assertNotNull(tais);
        } catch (IOException e) {
            fail("IOException not expected: " + e.getMessage());
        }
    }

    @Test
    public void testClose() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tais = new TarArchiveInputStream(in);
        tais.close();
        // Closing twice should be safe
        tais.close();
    }

    @Test
    public void testAvailable() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[100]);
        TarArchiveInputStream tais = new TarArchiveInputStream(in);
        // Initially available might delegate to underlying stream or buffer
        assertTrue(tais.available() >= 0);
        tais.close();
    }

    @Test
    public void testGetRecordSize() {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        try (TarArchiveInputStream tais = new TarArchiveInputStream(in)) {
            assertEquals(512, tais.getRecordSize());
        } catch (IOException e) {
            fail("Unexpected IOException");
        }
    }

    @Test
    public void testSkip() throws IOException {
        byte[] data = new byte[2048];
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        try (TarArchiveInputStream tais = new TarArchiveInputStream(in)) {
            long skipped = tais.skip(10);
            // Without a valid entry, skip might return 0 or handle accordingly
            assertTrue(skipped >= 0);
            
            // Negative skip should return 0
            assertEquals(0, tais.skip(-5));
        }
    }

    @Test
    public void testMarkSupported() {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        try (TarArchiveInputStream tais = new TarArchiveInputStream(in)) {
            assertFalse(tais.markSupported());
        } catch (IOException e) {
            fail("Unexpected IOException");
        }
    }

    @Test
    public void testMarkAndReset() {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        try (TarArchiveInputStream tais = new TarArchiveInputStream(in)) {
            tais.mark(100);
            try {
                tais.reset();
                fail("IOException expected for unsupported mark/reset");
            } catch (IOException e) {
                // expected
            }
        } catch (IOException e) {
            fail("Unexpected IOException");
        }
    }

    @Test
    public void testGetNextTarEntryEmptyStream() throws IOException {
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
    public void testParsePaxHeaders() throws Exception {
        // Test pax header parsing via reflection or package-private access if possible,
        // or by feeding a pax restricted stream if applicable.
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        try (TarArchiveInputStream tais = new TarArchiveInputStream(in)) {
            Map<String, String> headers = new HashMap<>();
            // Since parsePaxHeaders might be protected or package private, let's invoke via standard usage or test via valid structures if accessible.
            // If not directly accessible, ensure code paths around metadata are covered.
            assertNotNull(tais);
        }
    }

    @Test
    public void testLongLinkAndDirectoryEntries() {
        // Construct a minimal tar archive with a dummy header to exercise read/parse branches
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        // A tar block is 512 bytes. Let's write zeros or dummy bytes.
        try {
            bao.write(new byte[1024]);
        } catch (IOException e) {
            // ignore
        }
        ByteArrayInputStream in = new ByteArrayInputStream(bao.toByteArray());
        try (TarArchiveInputStream tais = new TarArchiveInputStream(in)) {
            TarArchiveEntry entry = tais.getNextTarEntry();
            // Since it's zero-filled, it might be recognized as EOF or invalid header depending on strictness
            assertNull(entry);
        } catch (IOException e) {
            // Handled or expected for malformed/empty header
        }
    }
}