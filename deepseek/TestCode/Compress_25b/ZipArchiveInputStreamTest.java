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

    private ZipArchiveInputStream zipArchiveInputStream;

    @Before
    public void setUp() {
        // Initialize with empty stream by default
        zipArchiveInputStream = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
    }

    @After
    public void tearDown() throws IOException {
        if (zipArchiveInputStream != null) {
            zipArchiveInputStream.close();
        }
    }

    @Test(expected = IOException.class)
    public void testReadEmptyStream() throws IOException {
        zipArchiveInputStream.read(new byte[10], 0, 10);
    }

    @Test(expected = IOException.class)
    public void testGetNextEntryEmptyStream() throws IOException {
        zipArchiveInputStream.getNextZipEntry();
    }

    @Test
    public void testCanReadEntryDataNullEntry() {
        assertFalse(zipArchiveInputStream.canReadEntryData(null));
    }

    @Test
    public void testMatchesEmptySignature() {
        assertFalse(ZipArchiveInputStream.matches(new byte[0], 0));
    }

    @Test
    public void testMatchesShortSignature() {
        byte[] shortSig = new byte[3];
        assertFalse(ZipArchiveInputStream.matches(shortSig, 3));
    }

    @Test
    public void testMatchesInvalidSignature() {
        byte[] invalidSig = new byte[]{0x00, 0x01, 0x02, 0x03};
        assertFalse(ZipArchiveInputStream.matches(invalidSig, 4));
    }

    @Test
    public void testMatchesValidSignature() {
        byte[] validSig = new byte[]{0x50, 0x4B, 0x03, 0x04};
        assertTrue(ZipArchiveInputStream.matches(validSig, 4));
    }

    @Test
    public void testMatchesValidSignatureWithExtraBytes() {
        byte[] validSig = new byte[]{0x50, 0x4B, 0x03, 0x04, 0x00, 0x00};
        assertTrue(ZipArchiveInputStream.matches(validSig, 6));
    }

    @Test
    public void testReadWithNullBuffer() throws IOException {
        // Create a minimal valid zip file
        byte[] zipData = createMinimalZip();
        zipArchiveInputStream = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        zipArchiveInputStream.getNextZipEntry();
        try {
            zipArchiveInputStream.read(null, 0, 10);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadWithNegativeOffset() throws IOException {
        byte[] zipData = createMinimalZip();
        zipArchiveInputStream = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        zipArchiveInputStream.getNextZipEntry();
        zipArchiveInputStream.read(new byte[10], -1, 5);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadWithNegativeLength() throws IOException {
        byte[] zipData = createMinimalZip();
        zipArchiveInputStream = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        zipArchiveInputStream.getNextZipEntry();
        zipArchiveInputStream.read(new byte[10], 0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadWithOffsetPlusLengthExceedsBuffer() throws IOException {
        byte[] zipData = createMinimalZip();
        zipArchiveInputStream = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        zipArchiveInputStream.getNextZipEntry();
        zipArchiveInputStream.read(new byte[10], 8, 5);
    }

    @Test
    public void testReadWithZeroLength() throws IOException {
        byte[] zipData = createMinimalZip();
        zipArchiveInputStream = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        zipArchiveInputStream.getNextZipEntry();
        int result = zipArchiveInputStream.read(new byte[10], 0, 0);
        assertEquals(0, result);
    }

    @Test
    public void testReadAfterAllEntries() throws IOException {
        byte[] zipData = createMinimalZip();
        zipArchiveInputStream = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        zipArchiveInputStream.getNextZipEntry();
        byte[] buffer = new byte[1024];
        while (zipArchiveInputStream.read(buffer) != -1) {
            // consume all data
        }
        assertEquals(-1, zipArchiveInputStream.read(buffer));
    }

    @Test
    public void testGetNextEntryAfterAllEntries() throws IOException {
        byte[] zipData = createMinimalZip();
        zipArchiveInputStream = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        assertNotNull(zipArchiveInputStream.getNextZipEntry());
        assertNull(zipArchiveInputStream.getNextZipEntry());
    }

    @Test
    public void testGetNextEntryWithStoredEntry() throws IOException {
        byte[] zipData = createZipWithStoredEntry("test.txt", "Hello World".getBytes());
        zipArchiveInputStream = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        ZipArchiveEntry entry = zipArchiveInputStream.getNextZipEntry();
        assertNotNull(entry);
        assertEquals("test.txt", entry.getName());
        assertEquals(11, entry.getSize());
    }

    @Test
    public void testReadStoredEntryContent() throws IOException {
        String content = "Hello World";
        byte[] zipData = createZipWithStoredEntry("test.txt", content.getBytes());
        zipArchiveInputStream = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        zipArchiveInputStream.getNextZipEntry();
        byte[] buffer = new byte[1024];
        int bytesRead = zipArchiveInputStream.read(buffer);
        assertEquals(content.length(), bytesRead);
        assertEquals(content, new String(buffer, 0, bytesRead));
    }

    @Test
    public void testGetNextEntryWithDeflatedEntry() throws IOException {
        byte[] zipData = createZipWithDeflatedEntry("data.bin", "Compressed data".getBytes());
        zipArchiveInputStream = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        ZipArchiveEntry entry = zipArchiveInputStream.getNextZipEntry();
        assertNotNull(entry);
        assertEquals("data.bin", entry.getName());
    }

    @Test
    public void testReadDeflatedEntryContent() throws IOException {
        String content = "This is some test data that will be compressed";
        byte[] zipData = createZipWithDeflatedEntry("data.txt", content.getBytes());
        zipArchiveInputStream = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        zipArchiveInputStream.getNextZipEntry();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = zipArchiveInputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        assertEquals(content, baos.toString("UTF-8"));
    }

    @Test
    public void testMultipleEntries() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        
        ZipEntry entry1 = new ZipEntry("file1.txt");
        zos.putNextEntry(entry1);
        zos.write("Content1".getBytes());
        zos.closeEntry();
        
        ZipEntry entry2 = new ZipEntry("file2.txt");
        zos.putNextEntry(entry2);
        zos.write("Content2".getBytes());
        zos.closeEntry();
        
        zos.close();
        
        zipArchiveInputStream = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        
        ZipArchiveEntry entry = zipArchiveInputStream.getNextZipEntry();
        assertNotNull(entry);
        assertEquals("file1.txt", entry.getName());
        
        entry = zipArchiveInputStream.getNextZipEntry();
        assertNotNull(entry);
        assertEquals("file2.txt", entry.getName());
        
        assertNull(zipArchiveInputStream.getNextZipEntry());
    }

    @Test
    public void testCanReadEntryDataWithValidEntry() throws IOException {
        byte[] zipData = createMinimalZip();
        zipArchiveInputStream = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        ZipArchiveEntry entry = zipArchiveInputStream.getNextZipEntry();
        assertTrue(zipArchiveInputStream.canReadEntryData(entry));
    }

    @Test
    public void testClose() throws IOException {
        zipArchiveInputStream.close();
        // Should not throw exception when closing again
        zipArchiveInputStream.close();
    }

    @Test(expected = IOException.class)
    public void testReadAfterClose() throws IOException {
        zipArchiveInputStream.close();
        zipArchiveInputStream.read(new byte[10]);
    }

    @Test(expected = IOException.class)
    public void testGetNextEntryAfterClose() throws IOException {
        zipArchiveInputStream.close();
        zipArchiveInputStream.getNextZipEntry();
    }

    @Test
    public void testConstructorWithEncoding() {
        ZipArchiveInputStream stream = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]), "UTF-8");
        assertNotNull(stream);
        try {
            stream.close();
        } catch (IOException e) {
            // ignore
        }
    }

    @Test
    public void testConstructorWithAllParams() {
        ZipArchiveInputStream stream = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]), "UTF-8", true, true);
        assertNotNull(stream);
        try {
            stream.close();
        } catch (IOException e) {
            // ignore
        }
    }

    @Test
    public void testConstructorWithUnzipFlag() {
        ZipArchiveInputStream stream = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]), "UTF-8", true);
        assertNotNull(stream);
        try {
            stream.close();
        } catch (IOException e) {
            // ignore
        }
    }

    @Test
    public void testGetNextEntryWithEmptyName() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        ZipEntry entry = new ZipEntry("");
        zos.putNextEntry(entry);
        zos.write("data".getBytes());
        zos.closeEntry();
        zos.close();
        
        zipArchiveInputStream = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        ZipArchiveEntry zipEntry = zipArchiveInputStream.getNextZipEntry();
        assertNotNull(zipEntry);
        assertEquals("", zipEntry.getName());
    }

    @Test
    public void testGetNextEntryWithDirectoryEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        ZipEntry entry = new ZipEntry("dir/");
        zos.putNextEntry(entry);
        zos.closeEntry();
        zos.close();
        
        zipArchiveInputStream = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        ZipArchiveEntry zipEntry = zipArchiveInputStream.getNextZipEntry();
        assertNotNull(zipEntry);
        assertTrue(zipEntry.isDirectory());
    }

    @Test
    public void testReadWithLargeBuffer() throws IOException {
        byte[] data = "Test data for reading".getBytes();
        byte[] zipData = createZipWithStoredEntry("test.txt", data);
        zipArchiveInputStream = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        zipArchiveInputStream.getNextZipEntry();
        byte[] buffer = new byte[4096];
        int bytesRead = zipArchiveInputStream.read(buffer);
        assertEquals(data.length, bytesRead);
    }

    @Test
    public void testReadWithSmallBuffer() throws IOException {
        byte[] data = "Test data for reading with small buffer".getBytes();
        byte[] zipData = createZipWithStoredEntry("test.txt", data);
        zipArchiveInputStream = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        zipArchiveInputStream.getNextZipEntry();
        byte[] buffer = new byte[4];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int bytesRead;
        while ((bytesRead = zipArchiveInputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        assertArrayEquals(data, baos.toByteArray());
    }

    @Test
    public void testGetNextEntryWithUnixMode() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        ZipEntry entry = new ZipEntry("file.txt");
        entry.setUnixMode(0755);
        zos.putNextEntry(entry);
        zos.write("data".getBytes());
        zos.closeEntry();
        zos.close();
        
        zipArchiveInputStream = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        ZipArchiveEntry zipEntry = zipArchiveInputStream.getNextZipEntry();
        assertNotNull(zipEntry);
        assertEquals(0755, zipEntry.getUnixMode());
    }

    @Test
    public void testGetNextEntryWithExtraFields() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        ZipEntry entry = new ZipEntry("file.txt");
        entry.setComment("Test comment");
        zos.putNextEntry(entry);
        zos.write("data".getBytes());
        zos.closeEntry();
        zos.close();
        
        zipArchiveInputStream = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        ZipArchiveEntry zipEntry = zipArchiveInputStream.getNextZipEntry();
        assertNotNull(zipEntry);
        assertEquals("Test comment", zipEntry.getComment());
    }

    @Test
    public void testGetNextEntryWithTime() throws IOException {
        long time = System.currentTimeMillis();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        ZipEntry entry = new ZipEntry("file.txt");
        entry.setTime(time);
        zos.putNextEntry(entry);
        zos.write("data".getBytes());
        zos.closeEntry();
        zos.close();
        
        zipArchiveInputStream = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        ZipArchiveEntry zipEntry = zipArchiveInputStream.getNextZipEntry();
        assertNotNull(zipEntry);
        assertEquals(time, zipEntry.getTime());
    }

    @Test
    public void testGetNextEntryWithSize() throws IOException {
        byte[] data = "Size test data".getBytes();
        byte[] zipData = createZipWithStoredEntry("file.txt", data);
        zipArchiveInputStream = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        ZipArchiveEntry entry = zipArchiveInputStream.getNextZipEntry();
        assertNotNull(entry);
        assertEquals(data.length, entry.getSize());
    }

    @Test
    public void testGetNextEntryWithCompressedSize() throws IOException {
        byte[] data = "Compressed size test".getBytes();
        byte[] zipData = createZipWithDeflatedEntry("file.txt", data);
        zipArchiveInputStream = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        ZipArchiveEntry entry = zipArchiveInputStream.getNextZipEntry();
        assertNotNull(entry);
        assertTrue(entry.getCompressedSize() > 0);
    }

    @Test
    public void testGetNextEntryWithCrc() throws IOException {
        byte[] data = "CRC test data".getBytes();
        byte[] zipData = createZipWithStoredEntry("file.txt", data);
        zipArchiveInputStream = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        ZipArchiveEntry entry = zipArchiveInputStream.getNextZipEntry();
        assertNotNull(entry);
        assertTrue(entry.getCrc() != 0);
    }

    @Test
    public void testGetNextEntryWithMethod() throws IOException {
        byte[] data = "Method test".getBytes();
        byte[] zipData = createZipWithStoredEntry("file.txt", data);
        zipArchiveInputStream = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        ZipArchiveEntry entry = zipArchiveInputStream.getNextZipEntry();
        assertNotNull(entry);
        assertEquals(ZipEntry.STORED, entry.getMethod());
    }

    @Test
    public void testGetNextEntryWithDeflatedMethod() throws IOException {
        byte[] data = "Deflated method test".getBytes();
        byte[] zipData = createZipWithDeflatedEntry("file.txt", data);
        zipArchiveInputStream = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        ZipArchiveEntry entry = zipArchiveInputStream.getNextZipEntry();
        assertNotNull(entry);
        assertEquals(ZipEntry.DEFLATED, entry.getMethod());
    }

    @Test
    public void testReadWithOffset() throws IOException {
        byte[] data = "Offset test data".getBytes();
        byte[] zipData = createZipWithStoredEntry("file.txt", data);
        zipArchiveInputStream = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        zipArchiveInputStream.getNextZipEntry();
        byte[] buffer = new byte[20];
        int bytesRead = zipArchiveInputStream.read(buffer, 5, 10);
        assertEquals(data.length, bytesRead);
        assertEquals("Offset", new String(buffer, 5, 6));
    }

    @Test
    public void testReadWithMultipleReads() throws IOException {
        byte[] data = "This is a longer test data that requires multiple reads".getBytes();
        byte[] zipData = createZipWithStoredEntry("file.txt", data);
        zipArchiveInputStream = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        zipArchiveInputStream.getNextZipEntry();
        byte[] buffer = new byte[10];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int bytesRead;
        while ((bytesRead = zipArchiveInputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        assertArrayEquals(data, baos.toByteArray());
    }

    @Test
    public void testGetNextEntryWithMultipleEntriesAndRead() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        
        ZipEntry entry1 = new ZipEntry("file1.txt");
        zos.putNextEntry(entry1);
        zos.write("Content1".getBytes());
        zos.closeEntry();
        
        ZipEntry entry2 = new ZipEntry("file2.txt");
        zos.putNextEntry(entry2);
        zos.write("Content2".getBytes());
        zos.closeEntry();
        
        zos.close();
        
        zipArchiveInputStream = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        
        ZipArchiveEntry entry = zipArchiveInputStream.getNextZipEntry();
        assertNotNull(entry);
        assertEquals("file1.txt", entry.getName());
        
        byte[] buffer = new byte[1024];
        int bytesRead = zipArchiveInputStream.read(buffer);
        assertEquals(8, bytesRead);
        assertEquals("Content1", new String(buffer, 0, bytesRead));
        
        entry = zipArchiveInputStream.getNextZipEntry();
        assertNotNull(entry);
        assertEquals("file2.txt", entry.getName());
        
        bytesRead = zipArchiveInputStream.read(buffer);
        assertEquals(8, bytesRead);
        assertEquals("Content2", new String(buffer, 0, bytesRead));
        
        assertNull(zipArchiveInputStream.getNextZipEntry());
    }

    @Test
    public void testReadWithEmptyEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        ZipEntry entry = new ZipEntry("empty.txt");
        zos.putNextEntry(entry);
        zos.closeEntry();
        zos.close();
        
        zipArchiveInputStream = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        zipArchiveInputStream.getNextZipEntry();
        byte[] buffer = new byte[1024];
        int bytesRead = zipArchiveInputStream.read(buffer);
        assertEquals(-1, bytesRead);
    }

    @Test
    public void testGetNextEntryWithSpecialCharacters() throws IOException {
        String fileName = "test file (1).txt";
        byte[] data = "Special chars".getBytes();
        byte[] zipData = createZipWithStoredEntry(fileName, data);
        zipArchiveInputStream = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        ZipArchiveEntry entry = zipArchiveInputStream.getNextZipEntry();
        assertNotNull(entry);
        assertEquals(fileName, entry.getName());
    }

    @Test
    public void testGetNextEntryWithLongName() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("a");
        }
        sb.append(".txt");
        String longName = sb.toString();
        byte[] data = "Long name test".getBytes();
        byte[] zipData = createZipWithStoredEntry(longName, data);
        zipArchiveInputStream = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        ZipArchiveEntry entry = zipArchiveInputStream.getNextZipEntry();
        assertNotNull(entry);
        assertEquals(longName, entry.getName());
    }

    @Test
    public void testGetNextEntryWithUnicodeName() throws IOException {
        String unicodeName = "测试文件.txt";
        byte[] data = "Unicode test".getBytes();
        byte[] zipData = createZipWithStoredEntry(unicodeName, data);
        zipArchiveInputStream = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        ZipArchiveEntry entry = zipArchiveInputStream.getNextZipEntry();
        assertNotNull(entry);
        assertEquals(unicodeName, entry.getName());
    }

    @Test
    public void testReadWithDeflatedEntryAndSmallBuffer() throws IOException {
        String content = "This is a longer test data that will be compressed and read with small buffer";
        byte[] zipData = createZipWithDeflatedEntry("data.txt", content.getBytes());
        zipArchiveInputStream = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        zipArchiveInputStream.getNextZipEntry();
        byte[] buffer = new byte[3];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int bytesRead;
        while ((bytesRead = zipArchiveInputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        assertEquals(content, baos.toString("UTF-8"));
    }

    @Test
    public void testReadWithDeflatedEntryAndLargeBuffer() throws IOException {
        String content = "Short data";
        byte[] zipData = createZipWithDeflatedEntry("data.txt", content.getBytes());
        zipArchiveInputStream = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        zipArchiveInputStream.getNextZipEntry();
        byte[] buffer = new byte[4096];
        int bytesRead = zipArchiveInputStream.read(buffer);
        assertEquals(content.length(), bytesRead);
        assertEquals(content, new String(buffer, 0, bytesRead));
    }

    @Test
    public void testGetNextEntryWithMultipleEntriesAndDifferentMethods() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        
        ZipEntry entry1 = new ZipEntry("stored.txt");
        entry1.setMethod(ZipEntry.STORED);
        byte[] data1 = "Stored data".getBytes();
        entry1.setSize(data1.length);
        java.util.zip.CRC32 crc = new java.util.zip.CRC32();
        crc.update(data1);
        entry1.setCrc(crc.getValue());
        zos.putNextEntry(entry1);
        zos.write(data1);
        zos.closeEntry();
        
        ZipEntry entry2 = new ZipEntry("deflated.txt");
        zos.putNextEntry(entry2);
        zos.write("Deflated data".getBytes());
        zos.closeEntry();
        
        zos.close();
        
        zipArchiveInputStream = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        
        ZipArchiveEntry entry = zipArchiveInputStream.getNextZipEntry();
        assertNotNull(entry);
        assertEquals("stored.txt", entry.getName());
        assertEquals(ZipEntry.STORED, entry.getMethod());
        
        byte[] buffer = new byte[1024];
        int bytesRead = zipArchiveInputStream.read(buffer);
        assertEquals(11, bytesRead);
        assertEquals("Stored data", new String(buffer, 0, bytesRead));
        
        entry = zipArchiveInputStream.getNextZipEntry();
        assertNotNull(entry);
        assertEquals("deflated.txt", entry.getName());
        assertEquals(ZipEntry.DEFLATED, entry.getMethod());
        
        bytesRead = zipArchiveInputStream.read(buffer);
        assertEquals(13, bytesRead);
        assertEquals("Deflated data", new String(buffer, 0, bytesRead));
        
        assertNull(zipArchiveInputStream.getNextZipEntry());
    }

    // Helper methods to create test zip data
    private byte[] createMinimalZip() {
        return createZipWithStoredEntry("test.txt", "test".getBytes());
    }

    private byte[] createZipWithStoredEntry(String name, byte[] data) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos);
            ZipEntry entry = new ZipEntry(name);
            entry.setMethod(ZipEntry.STORED);
            entry.setSize(data.length);
            java.util.zip.CRC32 crc = new java.util.zip.CRC32();
            crc.update(data);
            entry.setCrc(crc.getValue());
            zos.putNextEntry(entry);
            zos.write(data);
            zos.closeEntry();
            zos.close();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create test zip", e);
        }
    }

    private byte[] createZipWithDeflatedEntry(String name, byte[] data) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos);
            ZipEntry entry = new ZipEntry(name);
            entry.setMethod(ZipEntry.DEFLATED);
            zos.putNextEntry(entry);
            zos.write(data);
            zos.closeEntry();
            zos.close();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create test zip", e);
        }
    }
}