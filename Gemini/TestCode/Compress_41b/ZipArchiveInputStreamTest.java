package org.apache.commons.compress.archivers.zip;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;

public class ZipArchiveInputStreamTest {

    @Test
    public void testMatches() {
        byte[] signature = ZipArchiveInputStream.getBytes(ZipArchiveOutputStream.LFH_SIG);
        assertTrue(ZipArchiveInputStream.matches(signature, signature.length));
        
        byte[] shortSig = new byte[] { signature[0], signature[1] };
        assertFalse(ZipArchiveInputStream.matches(shortSig, shortSig.length));
        
        assertFalse(ZipArchiveInputStream.matches(new byte[] { 0, 0, 0, 0 }, 4));
        assertFalse(ZipArchiveInputStream.matches(null, 4));
    }

    @Test
    public void testCtorWithEncoding() {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        ZipArchiveInputStream zais = new ZipArchiveInputStream(bais, "UTF-8");
        assertNotNull(zais);
    }

    @Test
    public void testCtorWithEncodingAndDetectLead() {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        ZipArchiveInputStream zais = new ZipArchiveInputStream(bais, "UTF-8", true);
        assertNotNull(zais);
    }

    @Test
    public void testCtorWithEncodingDetectLeadAndAllowStored() {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        ZipArchiveInputStream zais = new ZipArchiveInputStream(bais, "UTF-8", true, true);
        assertNotNull(zais);
    }

    @Test
    public void testGetNextZipEntryNullWhenEmpty() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        try (ZipArchiveInputStream zais = new ZipArchiveInputStream(bais)) {
            assertNull(zais.getNextZipEntry());
        }
    }

    @Test
    public void testClose() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        ZipArchiveInputStream zais = new ZipArchiveInputStream(bais);
        zais.close();
        // Subsequent reads or operations should handle closed state or throw IOException
        try {
            zais.read();
        } catch (IOException e) {
            // expected or handled gracefully
        }
    }

    @Test
    public void testReadOnEmptyStream() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        try (ZipArchiveInputStream zais = new ZipArchiveInputStream(bais)) {
            byte[] buf = new byte[10];
            int read = zais.read(buf, 0, 10);
            assertEquals(-1, read);
        }
    }
}