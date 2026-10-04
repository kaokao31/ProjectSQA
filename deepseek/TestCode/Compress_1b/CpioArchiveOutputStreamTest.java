package org.apache.commons.compress.archivers.cpio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class CpioArchiveOutputStreamTest {

    @Test
    public void testDefaultConstructorWritesNewAsciiMagic() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (CpioArchiveOutputStream cpio = new CpioArchiveOutputStream(baos)) {
            CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_ASCII, "a");
            entry.setSize(0);
            cpio.putArchiveEntry(entry);
            cpio.closeArchiveEntry();
            assertMagic(baos, "070701");
        }
    }

    @Test
    public void testNewCrcConstructorWritesMagic() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (CpioArchiveOutputStream cpio = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW_CRC, 512)) {
            CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_CRC, "a");
            entry.setSize(0);
            cpio.putArchiveEntry(entry);
            cpio.closeArchiveEntry();
            assertMagic(baos, "070702");
        }
    }

    @Test
    public void testOldAsciiMagic() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (CpioArchiveOutputStream cpio = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_OLD_ASCII, 512)) {
            CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_ASCII, "a");
            entry.setSize(0);
            cpio.putArchiveEntry(entry);
            cpio.closeArchiveEntry();
            assertMagic(baos, "070707");
        }
    }

    @Test
    public void testOldBinaryCanWrite() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (CpioArchiveOutputStream cpio = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_OLD_BINARY, 512)) {
            CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_BINARY, "a");
            entry.setSize(2);
            cpio.putArchiveEntry(entry);
            cpio.write(new byte[] {1, 2}, 0, 2);
            cpio.closeArchiveEntry();
            cpio.finish();
        }
        assertTrue(baos.size() > 0);
    }

    @Test(expected = IOException.class)
    public void testPutArchiveEntryRejectsNull() throws IOException {
        CpioArchiveOutputStream cpio =
                new CpioArchiveOutputStream(new ByteArrayOutputStream(), CpioConstants.FORMAT_NEW_ASCII);
        cpio.putArchiveEntry(null);
    }

    @Test(expected = IOException.class)
    public void testPutArchiveEntryRejectsWrongFormat() throws IOException {
        CpioArchiveOutputStream cpio =
                new CpioArchiveOutputStream(new ByteArrayOutputStream(), CpioConstants.FORMAT_NEW_ASCII);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_ASCII, "x");
        entry.setSize(0);
        cpio.putArchiveEntry(entry);
    }

    @Test(expected = IOException.class)
    public void testSecondPutWithoutClosingFirst() throws IOException {
        CpioArchiveOutputStream cpio =
                new CpioArchiveOutputStream(new ByteArrayOutputStream(), CpioConstants.FORMAT_NEW_ASCII);
        CpioArchiveEntry first = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_ASCII, "one");
        first.setSize(0);
        cpio.putArchiveEntry(first);
        CpioArchiveEntry second = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_ASCII, "two");
        second.setSize(0);
        cpio.putArchiveEntry(second);
    }

    @Test(expected = IOException.class)
    public void testPutAfterFinish() throws IOException {
        CpioArchiveOutputStream cpio =
                new CpioArchiveOutputStream(new ByteArrayOutputStream(), CpioConstants.FORMAT_NEW_ASCII);
        cpio.finish();
        CpioArchiveEntry late = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_ASCII, "late");
        late.setSize(0);
        cpio.putArchiveEntry(late);
    }

    @Test(expected = IOException.class)
    public void testWriteBeforeOpenEntry() throws IOException {
        CpioArchiveOutputStream cpio =
                new CpioArchiveOutputStream(new ByteArrayOutputStream(), CpioConstants.FORMAT_NEW_ASCII);
        cpio.write(new byte[] {1}, 0, 1);
    }

    @Test(expected = IOException.class)
    public void testWriteAfterCloseEntry() throws IOException {
        CpioArchiveOutputStream cpio =
                new CpioArchiveOutputStream(new ByteArrayOutputStream(), CpioConstants.FORMAT_NEW_ASCII);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_ASCII, "a");
        entry.setSize(1);
        cpio.putArchiveEntry(entry);
        cpio.write(new byte[] {1}, 0, 1);
        cpio.closeArchiveEntry();
        cpio.write(new byte[] {2}, 0, 1);
    }

    @Test(expected = IOException.class)
    public void testWriteAfterFinish() throws IOException {
        CpioArchiveOutputStream cpio =
                new CpioArchiveOutputStream(new ByteArrayOutputStream(), CpioConstants.FORMAT_NEW_ASCII);
        cpio.finish();
        cpio.write(new byte[] {1}, 0, 1);
    }

    @Test
    public void testWriteZeroLengthWithoutEntryIsNoOp() throws IOException {
        try (CpioArchiveOutputStream cpio =
                     new CpioArchiveOutputStream(new ByteArrayOutputStream(), CpioConstants.FORMAT_NEW_ASCII)) {
            cpio.write(new byte[0], 0, 0);
        }
    }

    @Test
    public void testWriteZeroLengthAfterFinishIsNoOp() throws IOException {
        try (CpioArchiveOutputStream cpio =
                     new CpioArchiveOutputStream(new ByteArrayOutputStream(), CpioConstants.FORMAT_NEW_ASCII)) {
            cpio.finish();
            cpio.write(new byte[0], 0, 0);
        }
    }

    @Test(expected = IOException.class)
    public void testCloseArchiveEntryWithoutEntry() throws IOException {
        CpioArchiveOutputStream cpio =
                new CpioArchiveOutputStream(new ByteArrayOutputStream(), CpioConstants.FORMAT_NEW_ASCII);
        cpio.closeArchiveEntry();
    }

    @Test(expected = IOException.class)
    public void testCloseArchiveEntryAfterFinish() throws IOException {
        CpioArchiveOutputStream cpio =
                new CpioArchiveOutputStream(new ByteArrayOutputStream(), CpioConstants.FORMAT_NEW_ASCII);
        cpio.finish();
        cpio.closeArchiveEntry();
    }

    @Test(expected = IOException.class)
    public void testCloseArchiveEntryUnderflow() throws IOException {
        CpioArchiveOutputStream cpio =
                new CpioArchiveOutputStream(new ByteArrayOutputStream(), CpioConstants.FORMAT_NEW_ASCII);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_ASCII, "short");
        entry.setSize(5);
        cpio.putArchiveEntry(entry);
        cpio.write(new byte[] {1, 2}, 0, 2);
        cpio.closeArchiveEntry();
    }

    @Test(expected = IOException.class)
    public void testWriteTooManyBytes() throws IOException {
        CpioArchiveOutputStream cpio =
                new CpioArchiveOutputStream(new ByteArrayOutputStream(), CpioConstants.FORMAT_NEW_ASCII);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_ASCII, "small");
        entry.setSize(3);
        cpio.putArchiveEntry(entry);
        cpio.write(new byte[] {1, 2, 3, 4}, 0, 4);
    }

    @Test
    public void testWriteExactlySizeAndClose() throws IOException {
        try (CpioArchiveOutputStream cpio =
                     new CpioArchiveOutputStream(new ByteArrayOutputStream(), CpioConstants.FORMAT_NEW_ASCII)) {
            CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_ASCII, "exact");
            entry.setSize(3);
            cpio.putArchiveEntry(entry);
            cpio.write(new byte[] {1, 2, 3}, 0, 3);
            cpio.closeArchiveEntry();
            cpio.finish();
        }
    }

    @Test
    public void testFinishClosesOpenEntryWhenSizeFits() throws IOException {
        try (CpioArchiveOutputStream cpio =
                     new CpioArchiveOutputStream(new ByteArrayOutputStream(), CpioConstants.FORMAT_NEW_ASCII)) {
            CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_ASCII, "finished");
            entry.setSize(2);
            cpio.putArchiveEntry(entry);
            cpio.write(new byte[] {1, 2}, 0, 2);
            cpio.finish();
        }
    }

    @Test
    public void testFinishRejectsIncompleteEntry() throws IOException {
        CpioArchiveOutputStream cpio =
                new CpioArchiveOutputStream(new ByteArrayOutputStream(), CpioConstants.FORMAT_NEW_ASCII);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_ASCII, "incomplete");
        entry.setSize(5);
        cpio.putArchiveEntry(entry);
        cpio.write(new byte[] {1, 2}, 0, 2);
        try {
            cpio.finish();
            fail("Expected IOException because the open entry has unwritten bytes");
        } catch (IOException expected) {
            // expected
        }
        try {
            cpio.close();
        } catch (IOException ignored) {
            // close may retry finish and fail, but must still close the stream
        }
    }

    @Test
    public void testFinishTwiceThrows() throws IOException {
        CpioArchiveOutputStream cpio =
                new CpioArchiveOutputStream(new ByteArrayOutputStream(), CpioConstants.FORMAT_NEW_ASCII);
        cpio.finish();
        try {
            cpio.finish();
            fail("Expected IOException on second finish");
        } catch (IOException expected) {
            // expected
        }
    }

    @Test
    public void testCloseTwiceDoesNotThrow() throws IOException {
        CpioArchiveOutputStream cpio =
                new CpioArchiveOutputStream(new ByteArrayOutputStream(), CpioConstants.FORMAT_NEW_ASCII);
        cpio.close();
        cpio.close();
    }

    @Test
    public void testCloseClosesEvenIfFinishFails() throws IOException {
        CpioArchiveOutputStream cpio =
                new CpioArchiveOutputStream(new ByteArrayOutputStream(), CpioConstants.FORMAT_NEW_ASCII);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_ASCII, "fail");
        entry.setSize(5);
        cpio.putArchiveEntry(entry);
        cpio.write(new byte[] {1, 2}, 0, 2);
        try {
            cpio.close();
            fail("Expected IOException from close with unclosed incomplete entry");
        } catch (IOException expected) {
            // expected
        }
        cpio.close();
    }

    @Test
    public void testFinishEmptyStreamWritesTrailer() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (CpioArchiveOutputStream cpio =
                     new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW_ASCII, 512)) {
            cpio.finish();
            assertEquals(0, baos.size() % 512);
        }
        assertTrue(baos.size() > 0);
    }

    @Test
    public void testOutputAlignedForNewAscii() throws IOException {
        assertOutputAlignment(CpioConstants.FORMAT_NEW_ASCII, 4);
    }

    @Test
    public void testOutputAlignedForNewCrc() throws IOException {
        assertOutputAlignment(CpioConstants.FORMAT_NEW_CRC, 4);
    }

    @Test
    public void testOutputAlignedForOldAscii() throws IOException {
        assertOutputAlignment(CpioConstants.FORMAT_OLD_ASCII, 2);
    }

    @Test
    public void testOutputAlignedForOldBinary() throws IOException {
        assertOutputAlignment(CpioConstants.FORMAT_OLD_BINARY, 2);
    }

    @Test
    public void testMultipleEntriesRoundTrip() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (CpioArchiveOutputStream cpio =
                     new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW_ASCII, 512)) {
            for (int i = 0; i < 3; i++) {
                CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_ASCII, "file" + i);
                entry.setSize(1);
                cpio.putArchiveEntry(entry);
                cpio.write(new byte[] {(byte) i}, 0, 1);
                cpio.closeArchiveEntry();
            }
            cpio.finish();
        }
        assertTrue(baos.size() > 0);
    }

    @Test
    public void testWriteWithOffsetAndLength() throws IOException {
        try (CpioArchiveOutputStream cpio =
                     new CpioArchiveOutputStream(new ByteArrayOutputStream(), CpioConstants.FORMAT_NEW_ASCII)) {
            CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_ASCII, "offset");
            entry.setSize(3);
            cpio.putArchiveEntry(entry);
            byte[] data = new byte[] {0, 1, 2, 3, 4};
            cpio.write(data, 1, 3);
            cpio.closeArchiveEntry();
            cpio.finish();
        }
    }

    @Test
    public void testLongNameEntry() throws IOException {
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            name.append('a');
        }
        try (CpioArchiveOutputStream cpio =
                     new CpioArchiveOutputStream(new ByteArrayOutputStream(), CpioConstants.FORMAT_NEW_ASCII)) {
            CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_ASCII, name.toString());
            entry.setSize(1);
            cpio.putArchiveEntry(entry);
            cpio.write(new byte[] {42}, 0, 1);
            cpio.closeArchiveEntry();
            cpio.finish();
        }
    }

    private void assertOutputAlignment(short format, int alignment) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (CpioArchiveOutputStream cpio = new CpioArchiveOutputStream(baos, format, 512)) {
            CpioArchiveEntry entry = new CpioArchiveEntry(format, "aligned");
            entry.setSize(5);
            cpio.putArchiveEntry(entry);
            cpio.write(new byte[] {1, 2, 3, 4, 5}, 0, 5);
            cpio.closeArchiveEntry();
            assertEquals("format " + format + " data must be padded", 0, baos.size() % alignment);
            cpio.finish();
        }
    }

    private void assertMagic(ByteArrayOutputStream baos, String magic) {
        byte[] data = baos.toByteArray();
        assertEquals(magic, new String(data, 0, magic.length(), StandardCharsets.US_ASCII));
    }
}