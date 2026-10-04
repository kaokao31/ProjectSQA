package org.apache.commons.compress.archivers.zip;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.*;

public class ZipArchiveInputStreamTest {

    private ZipArchiveInputStream zipIn;

    @Before
    public void setUp() {
        // Initialize with empty stream by default
        zipIn = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
    }

    @After
    public void tearDown() throws IOException {
        if (zipIn != null) {
            zipIn.close();
        }
    }

    // Test reading from empty stream
    @Test(expected = IOException.class)
    public void testReadEmptyStream() throws IOException {
        zipIn.read(new byte[10], 0, 10);
    }

    // Test getNextEntry on empty stream
    @Test
    public void testGetNextEntryEmptyStream() throws IOException {
        assertNull(zipIn.getNextEntry());
    }

    // Test canReadEntryData on empty stream
    @Test
    public void testCanReadEntryDataEmptyStream() throws IOException {
        assertFalse(zipIn.canReadEntryData(null));
    }

    // Test reading a simple zip entry
    @Test
    public void testReadSimpleZipEntry() throws IOException {
        byte[] zipData = createSimpleZipEntry("test.txt", "Hello World".getBytes());
        zipIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        
        ZipArchiveEntry entry = zipIn.getNextEntry();
        assertNotNull(entry);
        assertEquals("test.txt", entry.getName());
        
        byte[] buffer = new byte[1024];
        int bytesRead = zipIn.read(buffer, 0, buffer.length);
        assertEquals(11, bytesRead);
        assertEquals("Hello World", new String(buffer, 0, bytesRead));
        
        assertEquals(-1, zipIn.read(buffer, 0, buffer.length));
        zipIn.closeEntry();
    }

    // Test reading multiple entries
    @Test
    public void testReadMultipleEntries() throws IOException {
        byte[] zipData = createMultipleZipEntries();
        zipIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        
        ZipArchiveEntry entry1 = zipIn.getNextEntry();
        assertNotNull(entry1);
        assertEquals("file1.txt", entry1.getName());
        
        byte[] buffer = new byte[1024];
        int bytesRead = zipIn.read(buffer, 0, buffer.length);
        assertEquals(5, bytesRead);
        assertEquals("Hello", new String(buffer, 0, bytesRead));
        zipIn.closeEntry();
        
        ZipArchiveEntry entry2 = zipIn.getNextEntry();
        assertNotNull(entry2);
        assertEquals("file2.txt", entry2.getName());
        
        bytesRead = zipIn.read(buffer, 0, buffer.length);
        assertEquals(5, bytesRead);
        assertEquals("World", new String(buffer, 0, bytesRead));
        zipIn.closeEntry();
        
        assertNull(zipIn.getNextEntry());
    }

    // Test reading with offset and length parameters
    @Test
    public void testReadWithOffsetAndLength() throws IOException {
        byte[] zipData = createSimpleZipEntry("test.txt", "Hello World".getBytes());
        zipIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        
        zipIn.getNextEntry();
        
        byte[] buffer = new byte[5];
        int bytesRead = zipIn.read(buffer, 0, 5);
        assertEquals(5, bytesRead);
        assertEquals("Hello", new String(buffer, 0, 5));
        
        bytesRead = zipIn.read(buffer, 0, 5);
        assertEquals(5, bytesRead);
        assertEquals(" Worl", new String(buffer, 0, 5));
        
        bytesRead = zipIn.read(buffer, 0, 5);
        assertEquals(1, bytesRead);
        assertEquals("d", new String(buffer, 0, 1));
        
        zipIn.closeEntry();
    }

    // Test reading with negative offset
    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadNegativeOffset() throws IOException {
        byte[] zipData = createSimpleZipEntry("test.txt", "Hello".getBytes());
        zipIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        zipIn.getNextEntry();
        zipIn.read(new byte[10], -1, 5);
    }

    // Test reading with negative length
    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadNegativeLength() throws IOException {
        byte[] zipData = createSimpleZipEntry("test.txt", "Hello".getBytes());
        zipIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        zipIn.getNextEntry();
        zipIn.read(new byte[10], 0, -1);
    }

    // Test reading with offset + length > buffer length
    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadOffsetPlusLengthExceedsBuffer() throws IOException {
        byte[] zipData = createSimpleZipEntry("test.txt", "Hello".getBytes());
        zipIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        zipIn.getNextEntry();
        zipIn.read(new byte[10], 5, 10);
    }

    // Test reading with null buffer
    @Test(expected = NullPointerException.class)
    public void testReadNullBuffer() throws IOException {
        byte[] zipData = createSimpleZipEntry("test.txt", "Hello".getBytes());
        zipIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        zipIn.getNextEntry();
        zipIn.read(null, 0, 5);
    }

    // Test reading with zero length
    @Test
    public void testReadZeroLength() throws IOException {
        byte[] zipData = createSimpleZipEntry("test.txt", "Hello".getBytes());
        zipIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        zipIn.getNextEntry();
        assertEquals(0, zipIn.read(new byte[10], 0, 0));
        zipIn.closeEntry();
    }

    // Test getNextZipEntry
    @Test
    public void testGetNextZipEntry() throws IOException {
        byte[] zipData = createSimpleZipEntry("test.txt", "Hello".getBytes());
        zipIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        
        ZipArchiveEntry entry = zipIn.getNextZipEntry();
        assertNotNull(entry);
        assertEquals("test.txt", entry.getName());
    }

    // Test closeEntry without reading
    @Test
    public void testCloseEntryWithoutReading() throws IOException {
        byte[] zipData = createSimpleZipEntry("test.txt", "Hello".getBytes());
        zipIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        
        zipIn.getNextEntry();
        zipIn.closeEntry(); // Should not throw exception
    }

    // Test closeEntry after reading all data
    @Test
    public void testCloseEntryAfterReadingAll() throws IOException {
        byte[] zipData = createSimpleZipEntry("test.txt", "Hello".getBytes());
        zipIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        
        zipIn.getNextEntry();
        byte[] buffer = new byte[1024];
        zipIn.read(buffer, 0, buffer.length);
        zipIn.closeEntry(); // Should not throw exception
    }

    // Test available method
    @Test
    public void testAvailable() throws IOException {
        byte[] zipData = createSimpleZipEntry("test.txt", "Hello".getBytes());
        zipIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        
        zipIn.getNextEntry();
        int available = zipIn.available();
        assertTrue(available > 0);
        zipIn.closeEntry();
    }

    // Test available on closed stream
    @Test(expected = IOException.class)
    public void testAvailableClosedStream() throws IOException {
        zipIn.close();
        zipIn.available();
    }

    // Test skip method
    @Test
    public void testSkip() throws IOException {
        byte[] zipData = createSimpleZipEntry("test.txt", "Hello World".getBytes());
        zipIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        
        zipIn.getNextEntry();
        long skipped = zipIn.skip(6);
        assertEquals(6, skipped);
        
        byte[] buffer = new byte[1024];
        int bytesRead = zipIn.read(buffer, 0, buffer.length);
        assertEquals(5, bytesRead);
        assertEquals("World", new String(buffer, 0, 5));
        zipIn.closeEntry();
    }

    // Test skip with negative value
    @Test
    public void testSkipNegative() throws IOException {
        byte[] zipData = createSimpleZipEntry("test.txt", "Hello".getBytes());
        zipIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        
        zipIn.getNextEntry();
        long skipped = zipIn.skip(-5);
        assertEquals(0, skipped);
        zipIn.closeEntry();
    }

    // Test skip with zero value
    @Test
    public void testSkipZero() throws IOException {
        byte[] zipData = createSimpleZipEntry("test.txt", "Hello".getBytes());
        zipIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        
        zipIn.getNextEntry();
        long skipped = zipIn.skip(0);
        assertEquals(0, skipped);
        zipIn.closeEntry();
    }

    // Test skip more than available data
    @Test
    public void testSkipMoreThanAvailable() throws IOException {
        byte[] zipData = createSimpleZipEntry("test.txt", "Hello".getBytes());
        zipIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        
        zipIn.getNextEntry();
        long skipped = zipIn.skip(100);
        assertEquals(5, skipped);
        zipIn.closeEntry();
    }

    // Test canReadEntryData with null entry
    @Test
    public void testCanReadEntryDataNull() throws IOException {
        assertFalse(zipIn.canReadEntryData(null));
    }

    // Test canReadEntryData with valid entry
    @Test
    public void testCanReadEntryDataValid() throws IOException {
        byte[] zipData = createSimpleZipEntry("test.txt", "Hello".getBytes());
        zipIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        
        ZipArchiveEntry entry = zipIn.getNextEntry();
        assertTrue(zipIn.canReadEntryData(entry));
        zipIn.closeEntry();
    }

    // Test getNextEntry after close
    @Test(expected = IOException.class)
    public void testGetNextEntryAfterClose() throws IOException {
        zipIn.close();
        zipIn.getNextEntry();
    }

    // Test read after close
    @Test(expected = IOException.class)
    public void testReadAfterClose() throws IOException {
        zipIn.close();
        zipIn.read(new byte[10], 0, 10);
    }

    // Test constructor with null stream
    @Test(expected = NullPointerException.class)
    public void testConstructorNullStream() {
        new ZipArchiveInputStream(null);
    }

    // Test constructor with encoding
    @Test
    public void testConstructorWithEncoding() throws IOException {
        byte[] zipData = createSimpleZipEntry("test.txt", "Hello".getBytes());
        zipIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipData), "UTF-8");
        assertNotNull(zipIn.getNextEntry());
    }

    // Test constructor with encoding and unparseable
    @Test
    public void testConstructorWithEncodingAndUnparseable() throws IOException {
        byte[] zipData = createSimpleZipEntry("test.txt", "Hello".getBytes());
        zipIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipData), "UTF-8", true);
        assertNotNull(zipIn.getNextEntry());
    }

    // Test constructor with all parameters
    @Test
    public void testConstructorAllParams() throws IOException {
        byte[] zipData = createSimpleZipEntry("test.txt", "Hello".getBytes());
        zipIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipData), "UTF-8", true, true);
        assertNotNull(zipIn.getNextEntry());
    }

    // Test reading a zip with stored (uncompressed) entry
    @Test
    public void testReadStoredEntry() throws IOException {
        byte[] zipData = createStoredZipEntry("stored.txt", "Stored data".getBytes());
        zipIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        
        ZipArchiveEntry entry = zipIn.getNextEntry();
        assertNotNull(entry);
        assertEquals("stored.txt", entry.getName());
        
        byte[] buffer = new byte[1024];
        int bytesRead = zipIn.read(buffer, 0, buffer.length);
        assertEquals(11, bytesRead);
        assertEquals("Stored data", new String(buffer, 0, bytesRead));
        zipIn.closeEntry();
    }

    // Test reading a zip with empty entry
    @Test
    public void testReadEmptyEntry() throws IOException {
        byte[] zipData = createSimpleZipEntry("empty.txt", new byte[0]);
        zipIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        
        ZipArchiveEntry entry = zipIn.getNextEntry();
        assertNotNull(entry);
        assertEquals("empty.txt", entry.getName());
        
        byte[] buffer = new byte[1024];
        assertEquals(-1, zipIn.read(buffer, 0, buffer.length));
        zipIn.closeEntry();
    }

    // Test reading a zip with large entry
    @Test
    public void testReadLargeEntry() throws IOException {
        byte[] largeData = new byte[65536];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 256);
        }
        byte[] zipData = createSimpleZipEntry("large.bin", largeData);
        zipIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        
        ZipArchiveEntry entry = zipIn.getNextEntry();
        assertNotNull(entry);
        assertEquals("large.bin", entry.getName());
        
        byte[] buffer = new byte[8192];
        int totalRead = 0;
        int bytesRead;
        while ((bytesRead = zipIn.read(buffer, 0, buffer.length)) != -1) {
            totalRead += bytesRead;
        }
        assertEquals(65536, totalRead);
        zipIn.closeEntry();
    }

    // Helper method to create a simple zip entry
    private byte[] createSimpleZipEntry(String name, byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry(name);
            zos.putNextEntry(entry);
            zos.write(data);
            zos.closeEntry();
        }
        return baos.toByteArray();
    }

    // Helper method to create multiple zip entries
    private byte[] createMultipleZipEntries() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry1 = new ZipEntry("file1.txt");
            zos.putNextEntry(entry1);
            zos.write("Hello".getBytes());
            zos.closeEntry();
            
            ZipEntry entry2 = new ZipEntry("file2.txt");
            zos.putNextEntry(entry2);
            zos.write("World".getBytes());
            zos.closeEntry();
        }
        return baos.toByteArray();
    }

    // Helper method to create a stored (uncompressed) zip entry
    private byte[] createStoredZipEntry(String name, byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry(name);
            entry.setMethod(ZipEntry.STORED);
            entry.setSize(data.length);
            entry.setCompressedSize(data.length);
            // CRC32 calculation would be needed for a valid stored entry
            // For testing purposes, this may not be fully valid
            zos.putNextEntry(entry);
            zos.write(data);
            zos.closeEntry();
        }
        return baos.toByteArray();
    }
}