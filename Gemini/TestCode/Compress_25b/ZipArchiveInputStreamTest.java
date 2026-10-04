package org.apache.commons.compress.archivers.zip;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;

public class ZipArchiveInputStreamTest {

    @Test
    public void testMatchesValidSignature() {
        // Test valid ZIP local file header signature: 0x04034b50
        byte[] validHeader = new byte[] { 0x50, 0x4b, 0x03, 0x04, 0x00, 0x00, 0x00, 0x00 };
        assertTrue(ZipArchiveInputStream.matches(validHeader, 4));
    }

    @Test
    public void testMatchesTooShort() {
        byte[] shortHeader = new byte[] { 0x50, 0x4b, 0x03 };
        assertFalse(ZipArchiveInputStream.matches(shortHeader, 3));
    }

    @Test
    public void testMatchesInvalidSignature() {
        byte[] invalidHeader = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
        assertFalse(ZipArchiveInputStream.matches(invalidHeader, 4));
    }

    @Test
    public void testConstructorWithEncoding() {
        byte[] data = new byte[0];
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        try (ZipArchiveInputStream zais = new ZipArchiveInputStream(bais, "UTF8")) {
            assertNotNull(zais);
        } catch (IOException e) {
            fail("IOException should not be thrown for empty stream initialization");
        }
    }

    @Test
    public void testConstructorWithEncodingAndDetectFlag() {
        byte[] data = new byte[0];
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        try (ZipArchiveInputStream zais = new ZipArchiveInputStream(bais, "UTF8", true)) {
            assertNotNull(zais);
        } catch (IOException e) {
            fail("IOException should not be thrown");
        }
    }

    @Test
    public void testGetNextZipEntryNullOrEmpty() {
        byte[] data = new byte[0];
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        try (ZipArchiveInputStream zais = new ZipArchiveInputStream(bais)) {
            assertNull(zais.getNextZipEntry());
        } catch (IOException e) {
            fail("IOException should not be thrown");
        }
    }

    @Test
    public void testCloseDoesNotThrow() {
        byte[] data = new byte[0];
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        try {
            ZipArchiveInputStream zais = new ZipArchiveInputStream(bais);
            zais.close();
            // Closing again should be safe or handled properly
            zais.close();
        } catch (IOException e) {
            fail("IOException should not be thrown on close");
        }
    }

    @Test
    public void testSkipNegativeOrZero() {
        byte[] data = new byte[] { 0x50, 0x4b, 0x03, 0x04, 0x01, 0x02, 0x03, 0x04 };
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        try (ZipArchiveInputStream zais = new ZipArchiveInputStream(bais)) {
            long skipped = zais.skip(0);
            assertEquals(0, skipped);

            long skippedNegative = zais.skip(-5);
            assertEquals(0, skippedNegative);
        } catch (IOException e) {
            fail("IOException should not be thrown");
        }
    }

    @Test
    public void testCanReadEntryDataWithNull() {
        byte[] data = new byte[0];
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        try (ZipArchiveInputStream zais = new ZipArchiveInputStream(bais)) {
            assertFalse(zais.canReadEntryData(null));
        } catch (IOException e) {
            fail("IOException should not be thrown");
        }
    }

    @Test
    public void testCanReadEntryDataWithZipArchiveEntry() {
        byte[] data = new byte[0];
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        try (ZipArchiveInputStream zais = new ZipArchiveInputStream(bais)) {
            ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
            // Depending on implementation, non-encrypted normal entry returns true
            assertTrue(zais.canReadEntryData(entry));
        } catch (IOException e) {
            fail("IOException should not be thrown");
        }
    }

    @Test
    public void testReadWithEmptyStream() {
        byte[] data = new byte[0];
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        try (ZipArchiveInputStream zais = new ZipArchiveInputStream(bais)) {
            byte[] buf = new byte[10];
            int read = zais.read(buf, 0, 10);
            assertEquals(-1, read);
        } catch (IOException e) {
            fail("IOException should not be thrown");
        }
    }
}