package org.apache.commons.compress.archivers.zip;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.*;
import java.util.zip.*;
import java.util.Arrays;

public class ZipArchiveInputStreamTest {

    @Test(expected = NullPointerException.class)
    public void testNullInputStream() {
        new ZipArchiveInputStream(null);
    }

    @Test
    public void testEmptyZip() throws IOException {
        // Create an empty zip (no entries)
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(bos);
        zos.finish();
        zos.close();
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ZipArchiveInputStream zais = new ZipArchiveInputStream(bis);
        assertNull(zais.getNextZipEntry());
        zais.close();
    }

    @Test
    public void testSingleDeflatedEntry() throws IOException {
        byte[] content = "Hello, world!".getBytes("UTF-8");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(bos);
        ZipEntry entry = new ZipEntry("test.txt");
        zos.putNextEntry(entry);
        zos.write(content);
        zos.closeEntry();
        zos.close();
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ZipArchiveInputStream zais = new ZipArchiveInputStream(bis);
        ZipArchiveEntry ze = zais.getNextZipEntry();
        assertNotNull(ze);
        assertEquals("test.txt", ze.getName());
        byte[] readContent = new byte[content.length];
        int offset = 0;
        int bytesRead;
        while ((bytesRead = zais.read(readContent, offset, readContent.length - offset)) != -1) {
            offset += bytesRead;
        }
        assertArrayEquals(content, readContent);
        assertNull(zais.getNextZipEntry());
        zais.close();
    }

    @Test
    public void testMultipleEntries() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(bos);
        zos.putNextEntry(new ZipEntry("a.txt"));
        zos.write("AAA".getBytes("UTF-8"));
        zos.closeEntry();
        zos.putNextEntry(new ZipEntry("b.txt"));
        zos.write("BBB".getBytes("UTF-8"));
        zos.closeEntry();
        zos.close();
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ZipArchiveInputStream zais = new ZipArchiveInputStream(bis);
        ZipArchiveEntry ze1 = zais.getNextZipEntry();
        assertNotNull(ze1);
        assertEquals("a.txt", ze1.getName());
        byte[] buf1 = new byte[10];
        int len1 = zais.read(buf1);
        assertEquals(3, len1);
        assertEquals("AAA", new String(buf1, 0, len1, "UTF-8"));
        ZipArchiveEntry ze2 = zais.getNextZipEntry();
        assertNotNull(ze2);
        assertEquals("b.txt", ze2.getName());
        byte[] buf2 = new byte[10];
        int len2 = zais.read(buf2);
        assertEquals(3, len2);
        assertEquals("BBB", new String(buf2, 0, len2, "UTF-8"));
        assertNull(zais.getNextZipEntry());
        zais.close();
    }

    @Test
    public void testStoredEntryWithDataDescriptor() throws IOException {
        // Use ZipArchiveOutputStream to create a stored entry with data descriptor
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(bos);
        zaos.setUseDataDescriptor(true);
        ZipArchiveEntry entry = new ZipArchiveEntry("stored.txt");
        entry.setMethod(ZipEntry.STORED);
        byte[] content = "Stored content".getBytes("UTF-8");
        // For stored entries, we must set sizes and CRC before writing
        entry.setSize(content.length);
        entry.setCompressedSize(content.length);
        CRC32 crc = new CRC32();
        crc.update(content);
        entry.setCrc(crc.getValue());
        zaos.putArchiveEntry(entry);
        zaos.write(content);
        zaos.closeArchiveEntry();
        zaos.close();
        byte[] zipBytes = bos.toByteArray();

        ByteArrayInputStream bis = new ByteArrayInputStream(zipBytes);
        ZipArchiveInputStream zais = new ZipArchiveInputStream(bis);
        ZipArchiveEntry ze = zais.getNextZipEntry();
        assertNotNull(ze);
        assertEquals("stored.txt", ze.getName());
        assertEquals(ZipEntry.STORED, ze.getMethod());
        byte[] readContent = new byte[content.length];
        int offset = 0;
        int bytesRead;
        while ((bytesRead = zais.read(readContent, offset, readContent.length - offset)) != -1) {
            offset += bytesRead;
        }
        assertArrayEquals(content, readContent);
        assertNull(zais.getNextZipEntry());
        zais.close();
    }

    @Test
    public void testReadAfterEntries() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(bos);
        zos.putNextEntry(new ZipEntry("entry"));
        zos.write("data".getBytes("UTF-8"));
        zos.closeEntry();
        zos.close();
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ZipArchiveInputStream zais = new ZipArchiveInputStream(bis);
        assertNotNull(zais.getNextZipEntry());
        // Read all data
        byte[] buf = new byte[1024];
        while (zais.read(buf) != -1) {}
        // Now end of entries
        assertNull(zais.getNextZipEntry());
        // Further read should return -1
        assertEquals(-1, zais.read(buf));
        zais.close();
    }

    @Test(expected = IOException.class)
    public void testReadAfterClose() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(bos);
        zos.putNextEntry(new ZipEntry("entry"));
        zos.write("data".getBytes("UTF-8"));
        zos.closeEntry();
        zos.close();
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ZipArchiveInputStream zais = new ZipArchiveInputStream(bis);
        zais.close();
        zais.read(new byte[10]); // should throw IOException
    }

    @Test
    public void testSkip() throws IOException {
        byte[] content = "Hello, world!".getBytes("UTF-8");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(bos);
        zos.putNextEntry(new ZipEntry("test.txt"));
        zos.write(content);
        zos.closeEntry();
        zos.close();
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ZipArchiveInputStream zais = new ZipArchiveInputStream(bis);
        ZipArchiveEntry ze = zais.getNextZipEntry();
        assertNotNull(ze);
        // Skip the entire entry
        long skipped = zais.skip(content.length);
        assertEquals(content.length, skipped);
        // Should be at end of entry, next read returns -1
        assertEquals(-1, zais.read());
        zais.close();
    }

    @Test
    public void testAvailable() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(bos);
        zos.putNextEntry(new ZipEntry("entry"));
        zos.write("data".getBytes("UTF-8"));
        zos.closeEntry();
        zos.close();
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ZipArchiveInputStream zais = new ZipArchiveInputStream(bis);
        ZipArchiveEntry ze = zais.getNextZipEntry();
        assertNotNull(ze);
        // available should return >0 before reading
        assertTrue(zais.available() > 0);
        zais.close();
    }

    @Test
    public void testConstructorWithEncoding() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(bos);
        zos.putNextEntry(new ZipEntry("entry"));
        zos.write("data".getBytes("UTF-8"));
        zos.closeEntry();
        zos.close();
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ZipArchiveInputStream zais = new ZipArchiveInputStream(bis, "UTF-8");
        assertNotNull(zais.getNextZipEntry());
        zais.close();
    }

    @Test
    public void testConstructorWithUseUnicodeExtraFields() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(bos);
        zos.putNextEntry(new ZipEntry("entry"));
        zos.write("data".getBytes("UTF-8"));
        zos.closeEntry();
        zos.close();
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ZipArchiveInputStream zais = new ZipArchiveInputStream(bis, "UTF-8", true);
        assertNotNull(zais.getNextZipEntry());
        zais.close();
    }

    @Test
    public void testConstructorWithAllParams() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(bos);
        zos.putNextEntry(new ZipEntry("entry"));
        zos.write("data".getBytes("UTF-8"));
        zos.closeEntry();
        zos.close();
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ZipArchiveInputStream zais = new ZipArchiveInputStream(bis, "UTF-8", true, true);
        assertNotNull(zais.getNextZipEntry());
        zais.close();
    }

    @Test(expected = IOException.class)
    public void testCorruptZip() throws IOException {
        byte[] corruptBytes = new byte[]{0, 1, 2}; // Not a valid zip
        ByteArrayInputStream bis = new ByteArrayInputStream(corruptBytes);
        ZipArchiveInputStream zais = new ZipArchiveInputStream(bis);
        zais.getNextZipEntry(); // should throw IOException
    }

    @Test
    public void testLargeEntry() throws IOException {
        // Create a zip with a single large stored entry to test reading in chunks
        int size = 65536; // 64KB
        byte[] content = new byte[size];
        Arrays.fill(content, (byte) 'A');
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(bos);
        zaos.setUseDataDescriptor(false);
        ZipArchiveEntry entry = new ZipArchiveEntry("large.bin");
        entry.setMethod(ZipEntry.STORED);
        entry.setSize(size);
        entry.setCompressedSize(size);
        CRC32 crc = new CRC32();
        crc.update(content);
        entry.setCrc(crc.getValue());
        zaos.putArchiveEntry(entry);
        zaos.write(content);
        zaos.closeArchiveEntry();
        zaos.close();
        byte[] zipBytes = bos.toByteArray();

        ByteArrayInputStream bis = new ByteArrayInputStream(zipBytes);
        ZipArchiveInputStream zais = new ZipArchiveInputStream(bis);
        ZipArchiveEntry ze = zais.getNextZipEntry();
        assertNotNull(ze);
        byte[] readContent = new byte[size];
        int totalRead = 0;
        int read;
        while ((read = zais.read(readContent, totalRead, size - totalRead)) != -1) {
            totalRead += read;
        }
        assertEquals(size, totalRead);
        assertArrayEquals(content, readContent);
        zais.close();
    }
}