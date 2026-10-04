package org.apache.commons.compress.archivers.zip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.zip.ZipEntry;

import org.apache.commons.compress.archivers.zip.UnicodePathExtraField;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit 4 test suite for ZipFile, targeting high coverage and fault detection.
 * Designed to trigger potential bugs related to Unicode extra fields and entry enumeration.
 */
public class ZipFileTest {

    private File tempZipFile;

    @Before
    public void setUp() throws IOException {
        tempZipFile = File.createTempFile("zipfiletest", ".zip");
        tempZipFile.deleteOnExit();
    }

    @After
    public void tearDown() {
        if (tempZipFile != null && tempZipFile.exists()) {
            tempZipFile.delete();
        }
    }

    // Helper to create a zip file with given entries (name -> content bytes)
    private void createZipFile(java.util.Map<String, byte[]> entries) throws IOException {
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(tempZipFile)) {
            for (java.util.Map.Entry<String, byte[]> entry : entries.entrySet()) {
                ZipArchiveEntry ze = new ZipArchiveEntry(entry.getKey());
                ze.setSize(entry.getValue().length);
                zos.putArchiveEntry(ze);
                zos.write(entry.getValue());
                zos.closeArchiveEntry();
            }
        }
    }

    // Helper to create a zip file with entries that have extra fields
    private void createZipFileWithExtraFields(java.util.Map<String, byte[]> entries,
                                              java.util.Map<String, java.util.List<ZipExtraField>> extraFields) throws IOException {
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(tempZipFile)) {
            for (java.util.Map.Entry<String, byte[]> entry : entries.entrySet()) {
                ZipArchiveEntry ze = new ZipArchiveEntry(entry.getKey());
                ze.setSize(entry.getValue().length);
                if (extraFields.containsKey(entry.getKey())) {
                    ze.setExtraFields(extraFields.get(entry.getKey()).toArray(new ZipExtraField[0]));
                }
                zos.putArchiveEntry(ze);
                zos.write(entry.getValue());
                zos.closeArchiveEntry();
            }
        }
    }

    @Test
    public void testReadNormalZip() throws IOException {
        java.util.Map<String, byte[]> entries = new java.util.LinkedHashMap<>();
        entries.put("file1.txt", "Hello".getBytes(StandardCharsets.UTF_8));
        entries.put("dir/file2.txt", "World".getBytes(StandardCharsets.UTF_8));
        entries.put("file3.txt", "Test".getBytes(StandardCharsets.UTF_8));
        createZipFile(entries);

        try (ZipFile zipFile = new ZipFile(tempZipFile)) {
            Enumeration<ZipArchiveEntry> en = zipFile.getEntries();
            int count = 0;
            while (en.hasMoreElements()) {
                ZipArchiveEntry ze = en.nextElement();
                assertNotNull(ze);
                assertTrue(entries.containsKey(ze.getName()));
                count++;
            }
            assertEquals(entries.size(), count);
        }
    }

    @Test
    public void testReadUnicodeZip() throws IOException {
        java.util.Map<String, byte[]> entries = new java.util.LinkedHashMap<>();
        entries.put("äöü.txt", "Umlaut".getBytes(StandardCharsets.UTF_8));
        entries.put("中文.txt", "Chinese".getBytes(StandardCharsets.UTF_8));
        entries.put("file.txt", "ASCII".getBytes(StandardCharsets.UTF_8));
        createZipFile(entries);

        try (ZipFile zipFile = new ZipFile(tempZipFile)) {
            Enumeration<ZipArchiveEntry> en = zipFile.getEntries();
            int count = 0;
            while (en.hasMoreElements()) {
                ZipArchiveEntry ze = en.nextElement();
                assertNotNull(ze);
                assertTrue("Entry name not found: " + ze.getName(), entries.containsKey(ze.getName()));
                count++;
            }
            assertEquals(entries.size(), count);
        }
    }

    @Test
    public void testReadZipWithUnicodePathExtraField() throws IOException {
        // This test targets the known bug: ZipFile may fail to read entries with UnicodePathExtraField.
        java.util.Map<String, byte[]> entries = new java.util.LinkedHashMap<>();
        entries.put("normal.txt", "Normal".getBytes(StandardCharsets.UTF_8));
        entries.put("unicode.txt", "Unicode".getBytes(StandardCharsets.UTF_8));

        java.util.Map<String, java.util.List<ZipExtraField>> extraFields = new java.util.HashMap<>();
        java.util.List<ZipExtraField> unicodeExtra = new java.util.ArrayList<>();
        unicodeExtra.add(new UnicodePathExtraField("unicode.txt", "unicode.txt".getBytes(StandardCharsets.UTF_8)));
        extraFields.put("unicode.txt", unicodeExtra);

        createZipFileWithExtraFields(entries, extraFields);

        try (ZipFile zipFile = new ZipFile(tempZipFile)) {
            Enumeration<ZipArchiveEntry> en = zipFile.getEntries();
            int count = 0;
            while (en.hasMoreElements()) {
                ZipArchiveEntry ze = en.nextElement();
                assertNotNull(ze);
                assertTrue("Entry name not found: " + ze.getName(), entries.containsKey(ze.getName()));
                count++;
            }
            assertEquals(entries.size(), count);
        }
    }

    @Test
    public void testReadEmptyZip() throws IOException {
        // Create an empty zip (just the central directory)
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(tempZipFile)) {
            // no entries
        }

        try (ZipFile zipFile = new ZipFile(tempZipFile)) {
            Enumeration<ZipArchiveEntry> en = zipFile.getEntries();
            assertFalse(en.hasMoreElements());
            assertEquals(0, zipFile.getEntries().asIterator().next()); // no entries
        }
    }

    @Test
    public void testReadZipWithDirectoryEntries() throws IOException {
        java.util.Map<String, byte[]> entries = new java.util.LinkedHashMap<>();
        entries.put("dir/", new byte[0]); // directory entry
        entries.put("dir/file.txt", "File in dir".getBytes(StandardCharsets.UTF_8));
        createZipFile(entries);

        try (ZipFile zipFile = new ZipFile(tempZipFile)) {
            Enumeration<ZipArchiveEntry> en = zipFile.getEntries();
            int count = 0;
            while (en.hasMoreElements()) {
                ZipArchiveEntry ze = en.nextElement();
                assertNotNull(ze);
                assertTrue(entries.containsKey(ze.getName()));
                count++;
            }
            assertEquals(entries.size(), count);
        }
    }

    @Test
    public void testGetEntry() throws IOException {
        java.util.Map<String, byte[]> entries = new java.util.LinkedHashMap<>();
        entries.put("file1.txt", "Hello".getBytes(StandardCharsets.UTF_8));
        entries.put("file2.txt", "World".getBytes(StandardCharsets.UTF_8));
        createZipFile(entries);

        try (ZipFile zipFile = new ZipFile(tempZipFile)) {
            ZipArchiveEntry entry = zipFile.getEntry("file1.txt");
            assertNotNull(entry);
            assertEquals("file1.txt", entry.getName());

            entry = zipFile.getEntry("nonexistent.txt");
            assertNull(entry);
        }
    }

    @Test
    public void testGetEntriesEnumeration() throws IOException {
        java.util.Map<String, byte[]> entries = new java.util.LinkedHashMap<>();
        entries.put("a.txt", "A".getBytes(StandardCharsets.UTF_8));
        entries.put("b.txt", "B".getBytes(StandardCharsets.UTF_8));
        createZipFile(entries);

        try (ZipFile zipFile = new ZipFile(tempZipFile)) {
            Enumeration<ZipArchiveEntry> en = zipFile.getEntries();
            assertTrue(en.hasMoreElements());
            ZipArchiveEntry first = en.nextElement();
            assertNotNull(first);
            assertTrue(en.hasMoreElements());
            ZipArchiveEntry second = en.nextElement();
            assertNotNull(second);
            assertFalse(en.hasMoreElements());
        }
    }

    @Test
    public void testReadZipWithManyEntries() throws IOException {
        java.util.Map<String, byte[]> entries = new java.util.LinkedHashMap<>();
        for (int i = 0; i < 100; i++) {
            entries.put("entry" + i + ".txt", ("Content" + i).getBytes(StandardCharsets.UTF_8));
        }
        createZipFile(entries);

        try (ZipFile zipFile = new ZipFile(tempZipFile)) {
            Enumeration<ZipArchiveEntry> en = zipFile.getEntries();
            int count = 0;
            while (en.hasMoreElements()) {
                ZipArchiveEntry ze = en.nextElement();
                assertNotNull(ze);
                count++;
            }
            assertEquals(entries.size(), count);
        }
    }

    @Test
    public void testReadZipWithCompressedSizes() throws IOException {
        java.util.Map<String, byte[]> entries = new java.util.LinkedHashMap<>();
        entries.put("small.txt", "Hi".getBytes(StandardCharsets.UTF_8));
        entries.put("large.txt", new byte[1024 * 10]); // 10KB of zeros
        createZipFile(entries);

        try (ZipFile zipFile = new ZipFile(tempZipFile)) {
            Enumeration<ZipArchiveEntry> en = zipFile.getEntries();
            while (en.hasMoreElements()) {
                ZipArchiveEntry ze = en.nextElement();
                assertNotNull(ze);
                assertTrue(entries.containsKey(ze.getName()));
                // Verify size
                assertEquals(entries.get(ze.getName()).length, ze.getSize());
            }
        }
    }

    @Test
    public void testReadZipWithComment() throws IOException {
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(tempZipFile)) {
            zos.setComment("Test comment");
            ZipArchiveEntry ze = new ZipArchiveEntry("file.txt");
            ze.setSize(4);
            zos.putArchiveEntry(ze);
            zos.write("data".getBytes(StandardCharsets.UTF_8));
            zos.closeArchiveEntry();
        }

        try (ZipFile zipFile = new ZipFile(tempZipFile)) {
            assertEquals("Test comment", zipFile.getComment());
            Enumeration<ZipArchiveEntry> en = zipFile.getEntries();
            assertTrue(en.hasMoreElements());
            ZipArchiveEntry ze = en.nextElement();
            assertEquals("file.txt", ze.getName());
        }
    }

    @Test(expected = IOException.class)
    public void testOpenNonExistentFile() throws IOException {
        File nonExistent = new File("nonexistent.zip");
        new ZipFile(nonExistent);
    }

    @Test(expected = IOException.class)
    public void testOpenInvalidZipFile() throws IOException {
        // Create a file that is not a valid zip
        Files.write(tempZipFile.toPath(), "Not a zip".getBytes(StandardCharsets.UTF_8));
        new ZipFile(tempZipFile);
    }
}