package org.apache.commons.compress.archivers.tar;

import org.apache.commons.compress.archivers.zip.ZipEncoding;
import org.apache.commons.compress.archivers.zip.ZipEncodingHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class TarArchiveOutputStreamTest {

    private ByteArrayOutputStream byteArrayOutputStream;
    private TarArchiveOutputStream tarArchiveOutputStream;

    @Before
    public void setUp() {
        byteArrayOutputStream = new ByteArrayOutputStream();
        tarArchiveOutputStream = new TarArchiveOutputStream(byteArrayOutputStream);
    }

    @After
    public void tearDown() throws IOException {
        if (tarArchiveOutputStream != null) {
            tarArchiveOutputStream.close();
        }
    }

    @Test
    public void testConstructorWithBlockSize() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(out, 1024);
        assertNotNull(tos);
    }

    @Test
    public void testConstructorWithBlockSizeAndEncoding() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(out, 1024, "UTF-8");
        assertNotNull(tos);
    }

    @Test
    public void testConstructorWithEncoding() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(out, "UTF-8");
        assertNotNull(tos);
    }

    @Test
    public void testSetLongFileMode() {
        tarArchiveOutputStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        assertEquals(TarArchiveOutputStream.LONGFILE_GNU, 
                getPrivateField(tarArchiveOutputStream, "longFileMode"));
    }

    @Test
    public void testSetBigNumberMode() {
        tarArchiveOutputStream.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
        assertEquals(TarArchiveOutputStream.BIGNUMBER_POSIX, 
                getPrivateField(tarArchiveOutputStream, "bigNumberMode"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutArchiveEntryNull() throws IOException {
        tarArchiveOutputStream.putArchiveEntry(null);
    }

    @Test
    public void testPutAndCloseArchiveEntry() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(5);
        
        tarArchiveOutputStream.putArchiveEntry(entry);
        tarArchiveOutputStream.write("12345".getBytes());
        tarArchiveOutputStream.closeArchiveEntry();
        
        tarArchiveOutputStream.finish();
        assertTrue(byteArrayOutputStream.size() > 0);
    }

    @Test(expected = IOException.class)
    public void testWriteBeyondDeclaredSize() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(2);
        
        tarArchiveOutputStream.putArchiveEntry(entry);
        tarArchiveOutputStream.write("12345".getBytes());
    }

    @Test
    public void testWriteByteArrayWithOffset() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(3);
        
        tarArchiveOutputStream.putArchiveEntry(entry);
        byte[] data = "ABCDE".getBytes();
        tarArchiveOutputStream.write(data, 1, 3);
        tarArchiveOutputStream.closeArchiveEntry();
    }

    @Test
    public void testFlushDoesNothing() throws IOException {
        tarArchiveOutputStream.flush();
        // Flush in TarArchiveOutputStream is a no-op, but should not throw
    }

    @Test
    public void testCreateArchiveEntry() throws IOException {
        File tempFile = File.createTempFile("commons-compress-test", ".tmp");
        tempFile.deleteOnExit();
        
        TarArchiveEntry entry = tarArchiveOutputStream.createArchiveEntry(tempFile, "test-entry.tmp");
        assertNotNull(entry);
        assertEquals("test-entry.tmp", entry.getName());
    }

    @Test
    public void testWritePaxHeadersWithGnuMode() throws IOException {
        tarArchiveOutputStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        
        TarArchiveEntry entry = new TarArchiveEntry("a_very_long_file_name_that_exceeds_the_standard_tar_limit_of_one_hundred_characters_to_force_pax_headers_generation_and_ensure_all_branches_are_covered.txt");
        entry.setSize(0);
        
        Map<String, String> paxHeaders = new HashMap<>();
        paxHeaders.put("comment", "test pax header");
        
        tarArchiveOutputStream.writePaxHeaders(entry, "long/name", paxHeaders);
        tarArchiveOutputStream.closeArchiveEntry();
    }

    @Test
    public void testWritePaxHeadersWithStarMode() throws IOException {
        tarArchiveOutputStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        
        TarArchiveEntry entry = new TarArchiveEntry("star_mode_test.txt");
        entry.setSize(0);
        
        Map<String, String> paxHeaders = new HashMap<>();
        
        tarArchiveOutputStream.writePaxHeaders(entry, "star/name", paxHeaders);
    }

    @Test
    public void testWriteWithLongFileNameGNU() throws IOException {
        tarArchiveOutputStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        
        String longName = "this/is/a/very/long/path/name/that/should/trigger/the/gnu/long/file/header/mechanism/in/tar/archive/output/stream/implementation/to/ensure/robustness/and/correctness/filename.txt";
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(4);
        
        tarArchiveOutputStream.putArchiveEntry(entry);
        tarArchiveOutputStream.write("test".getBytes());
        tarArchiveOutputStream.closeArchiveEntry();
    }

    @Test(expected = IOException.class)
    public void testWriteWithLongFileNameError() throws IOException {
        tarArchiveOutputStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_ERROR);
        
        String longName = "this/is/a/very/long/path/name/that/should/trigger/an/error/because/long/file/mode/is/set/to/error/exceeding/one/hundred/bytes/limit.txt";
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        
        tarArchiveOutputStream.putArchiveEntry(entry);
    }

    @Test
    public void testWriteBigNumberPOSIX() throws IOException {
        tarArchiveOutputStream.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
        
        TarArchiveEntry entry = new TarArchiveEntry("bignumber.txt");
        entry.setSize(0);
        entry.setGroupId(Long.MAX_VALUE);
        entry.setUserId(Long.MAX_VALUE);
        entry.setModTime(Long.MAX_VALUE / 1000);
        
        tarArchiveOutputStream.putArchiveEntry(entry);
        tarArchiveOutputStream.closeArchiveEntry();
    }

    @Test(expected = RuntimeException.class)
    public void testWriteBigNumberError() throws IOException {
        tarArchiveOutputStream.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_ERROR);
        
        TarArchiveEntry entry = new TarArchiveEntry("bignumber.txt");
        entry.setSize(0);
        entry.setGroupId(Long.MAX_VALUE); // Too large for standard 8-byte octal field
        
        tarArchiveOutputStream.putArchiveEntry(entry);
    }

    @Test
    public void testGetRecordSize() {
        assertEquals(512, tarArchiveOutputStream.getRecordSize());
    }

    @Test
    public void testFinishWithoutEntries() throws IOException {
        tarArchiveOutputStream.finish();
        assertTrue(byteArrayOutputStream.size() > 0);
    }

    private Object getPrivateField(Object target, String fieldName) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}