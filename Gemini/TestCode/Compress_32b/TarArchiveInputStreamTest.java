package org.apache.commons.compress.archivers.tar;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TarArchiveInputStreamTest {

    @Test
    public void testConstructorDefaults() {
        byte[] empty = new byte[0];
        ByteArrayInputStream in = new ByteArrayInputStream(empty);
        try (TarArchiveInputStream tais = new TarArchiveInputStream(in)) {
            assertNotNull(tais);
        } catch (IOException e) {
            fail("IOException not expected on simple construction");
        }

        try (TarArchiveInputStream taisEnc = new TarArchiveInputStream(in, "UTF-8")) {
            assertNotNull(taisEnc);
        } catch (IOException e) {
            fail("IOException not expected on construction with encoding");
        }

        try (TarArchiveInputStream taisBlk = new TarArchiveInputStream(in, 512)) {
            assertNotNull(taisBlk);
        } catch (IOException e) {
            fail("IOException not expected on construction with block size");
        }

        try (TarArchiveInputStream taisRec = new TarArchiveInputStream(in, 1024, 512)) {
            assertNotNull(taisRec);
        } catch (IOException e) {
            fail("IOException not expected on construction with block and record size");
        }

        try (TarArchiveInputStream taisAll = new TarArchiveInputStream(in, 1024, 512, "UTF-8")) {
            assertNotNull(taisAll);
        } catch (IOException e) {
            fail("IOException not expected on full construction");
        }
    }

    @Test
    public void testMatches() {
        byte[] header = new byte[512];
        // Tar magic bytes "ustar" at offset 257
        header[257] = 'u';
        header[258] = 's';
        header[259] = 't';
        header[260] = 'a';
        header[261] = 'r';
        
        assertTrue(TarArchiveInputStream.matches(header, header.length));
        assertFalse(TarArchiveInputStream.matches(new byte[10], 5));
    }

    @Test
    public void testGetAndSetCurrentEntry() {
        byte[] empty = new byte[0];
        try (TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(empty))) {
            assertNull(tais.getCurrentEntry());
            assertNull(tais.getNextTarEntry());
        } catch (IOException e) {
            fail("IOException unexpected");
        }
    }

    @Test
    public void testSkip() {
        byte[] data = new byte[1024];
        try (TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(data))) {
            long skipped = tais.skip(10);
            assertEquals(0, skipped); // Since no entry is open/read yet
        } catch (IOException e) {
            fail("IOException unexpected");
        }
    }

    @Test
    public void testAvailable() {
        byte[] data = new byte[100];
        try (TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(data))) {
            assertEquals(100, tais.available());
        } catch (IOException e) {
            fail("IOException unexpected");
        }
    }

    @Test
    public void testMarkSupported() {
        byte[] empty = new byte[0];
        try (TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(empty))) {
            assertFalse(tais.markSupported());
        } catch (IOException e) {
            fail("IOException unexpected");
        }
    }

    @Test
    public void testReadWithBuffer() {
        byte[] data = new byte[512];
        try (TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(data))) {
            byte[] buf = new byte[10];
            int read = tais.read(buf, 0, 10);
            assertEquals(-1, read); // No current entry open
        } catch (IOException e) {
            fail("IOException unexpected");
        }
    }

    @Test
    public void testCanReadEntryData() {
        byte[] empty = new byte[0];
        try (TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(empty))) {
            assertFalse(tais.canReadEntryData(null));
        } catch (IOException e) {
            fail("IOException unexpected");
        }
    }

    @Test
    public void testGetRecordSize() {
        byte[] empty = new byte[0];
        try (TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(empty))) {
            assertEquals(512, tais.getRecordSize());
        } catch (IOException e) {
            fail("IOException unexpected");
        }
    }
}