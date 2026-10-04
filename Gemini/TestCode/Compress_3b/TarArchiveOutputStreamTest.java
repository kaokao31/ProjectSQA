package org.apache.commons.compress.archivers.tar;

import org.apache.commons.compress.archivers.zip.ZipEncoding;
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

    private ByteArrayOutputStream baos;
    private TarArchiveOutputStream tos;

    @Before
    public void setUp() {
        baos = new ByteArrayOutputStream();
        tos = new TarArchiveOutputStream(baos);
    }

    @After
    public void tearDown() throws IOException {
        if (tos != null) {
            try {
                tos.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    @Test
    public void testDefaultConstructorAndConstants() {
        assertNotNull(tos);
        assertEquals(TarArchiveOutputStream.LONGFILE_ERROR, tos.getLongFileMode());
        assertEquals(TarArchiveOutputStream.BIGNUMBER_ERROR, tos.getBigNumberMode());
    }

    @Test
    public void testSetLongFileMode() {
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_TRUNCATE);
        assertEquals(TarArchiveOutputStream.LONGFILE_TRUNCATE, tos.getLongFileMode());

        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        assertEquals(TarArchiveOutputStream.LONGFILE_GNU, tos.getLongFileMode());

        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        assertEquals(TarArchiveOutputStream.LONGFILE_POSIX, tos.getLongFileMode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetLongFileModeInvalid() {
        tos.setLongFileMode(999);
    }

    @Test
    public void testSetBigNumberMode() {
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
        assertEquals(TarArchiveOutputStream.BIGNUMBER_POSIX, tos.getBigNumberMode());

        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
        assertEquals(TarArchiveOutputStream.BIGNUMBER_STAR, tos.getBigNumberMode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetBigNumberModeInvalid() {
        tos.setBigNumberMode(999);
    }

    @Test
    public void testSetAndGetRecordSize() {
        tos.setRecordSize(1024);
        assertEquals(1024, tos.getRecordSize());
    }

    @Test(expected = IOException.class)
    public void testSetRecordSizeAfterPutArchiveEntry() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.setRecordSize(1024);
    }

    @Test
    public void testCreateArchiveEntryFile() throws IOException {
        File tempFile = File.createTempFile("commons-compress-test", ".txt");
        tempFile.deleteOnExit();
        TarArchiveEntry entry = tos.createArchiveEntry(tempFile, "test.txt");
        assertNotNull(entry);
        assertEquals("test.txt", entry.getName());
        assertEquals(tempFile.length(), entry.getSize());
    }

    @Test
    public void testPutArchiveEntrySimple() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(5);
        tos.putArchiveEntry(entry);
        tos.write(new byte[] { 'h', 'e', 'l', 'l', 'o' });
        tos.closeArchiveEntry();
        tos.finish();
        assertTrue(baos.size() > 0);
    }

    @Test(expected = IOException.class)
    public void testCloseWithoutFinishIfAutoClose() throws IOException {
        // If not finished, close() calls finish() or checks state depending on implementation
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(2);
        tos.putArchiveEntry(entry);
        tos.write(new byte[] { 'h', 'i' });
        // Missing closeArchiveEntry() should trigger IOException on finish/close
        tos.close();
    }

    @Test
    public void testWriteMethodVariations() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(3);
        tos.putArchiveEntry(entry);
        tos.write('a');
        tos.write(new byte[] { 'b', 'c' });
        tos.closeArchiveEntry();
        tos.finish();
        assertTrue(baos.size() > 0);
    }

    @Test(expected = IOException.class)
    public void testWriteBeyondDeclaredSize() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(2);
        tos.putArchiveEntry(entry);
        tos.write(new byte[] { 'a', 'b', 'c' }); // exceeds size 2
    }

    @Test(expected = IOException.class)
    public void testWriteAfterCloseArchiveEntry() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(2);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.write('a');
    }

    @Test(expected = IOException.class)
    public void testPutArchiveEntryWhenClosed() throws IOException {
        tos.close();
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        tos.putArchiveEntry(entry);
    }

    @Test
    public void testLongFileNameGnu() throws IOException {
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 150; i++) {
            sb.append("a");
        }
        sb.append(".txt");
        String longName = sb.toString();

        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();
        assertTrue(baos.size() > 0);
    }

    @Test
    public void testLongFileNamePosix() throws IOException {
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 150; i++) {
            sb.append("b");
        }
        sb.append(".txt");
        String longName = sb.toString();

        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();
        assertTrue(baos.size() > 0);
    }

    @Test(expected = RuntimeException.class) // or IOException depending on implementation for LONGFILE_ERROR
    public void testLongFileNameError() throws IOException {
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_ERROR);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 150; i++) {
            sb.append("c");
        }
        sb.append(".txt");
        TarArchiveEntry entry = new TarArchiveEntry(sb.toString());
        entry.setSize(0);
        tos.putArchiveEntry(entry);
    }

    @Test
    public void testLongFileNameTruncate() throws IOException {
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_TRUNCATE);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 150; i++) {
            sb.append("d");
        }
        sb.append(".txt");
        TarArchiveEntry entry = new TarArchiveEntry(sb.toString());
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();
        assertTrue(baos.size() > 0);
    }

    @Test
    public void testWritePaxHeaders() throws IOException {
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
        TarArchiveEntry entry = new TarArchiveEntry("pax.txt");
        entry.setSize(0);
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("comment", "test-pax-header");
        tos.writePaxHeaders(entry, "PaxHeader", headers);
        tos.closeArchiveEntry();
        tos.finish();
        assertTrue(baos.size() > 0);
    }

    @Test
    public void testWriteComment() throws IOException {
        tos.writeComment("This is a test comment");
        tos.finish();
        assertTrue(baos.size() > 0);
    }

    @Test
    public void testConstructorWithBlockSize() {
        TarArchiveOutputStream customTos = new TarArchiveOutputStream(baos, 1024);
        assertNotNull(customTos);
        try {
            customTos.close();
        } catch (IOException e) {
            // ignore
        }
    }

    @Test
    public void testConstructorWithBlockSizeAndEncoding() {
        TarArchiveOutputStream customTos = new TarArchiveOutputStream(baos, 1024, "UTF-8");
        assertNotNull(customTos);
        try {
            customTos.close();
        } catch (IOException e) {
            // ignore
        }
    }

    @Test
    public void testConstructorWithEncoding() {
        TarArchiveOutputStream customTos = new TarArchiveOutputStream(baos, "UTF-8");
        assertNotNull(customTos);
        try {
            customTos.close();
        } catch (IOException e) {
            // ignore
        }
    }
}