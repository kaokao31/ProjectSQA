package org.apache.commons.compress.archivers.cpio;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Test;

public class CpioArchiveInputStreamTest {

    private static byte[] createArchive(short format, String[] names, byte[][] contents) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, format);
        try {
            for (int i = 0; i < names.length; i++) {
                byte[] content = contents == null || contents[i] == null ? new byte[0] : contents[i];
                CpioArchiveEntry entry = new CpioArchiveEntry(format, names[i]);
                entry.setSize(content.length);
                out.putArchiveEntry(entry);
                if (content.length > 0) {
                    out.write(content);
                }
                out.closeArchiveEntry();
            }
        } finally {
            out.close();
        }
        return bos.toByteArray();
    }

    private static byte[] readAll(CpioArchiveInputStream in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[256];
        int n;
        while ((n = in.read(buf)) != -1) {
            bos.write(buf, 0, n);
        }
        return bos.toByteArray();
    }

    @Test
    public void testMatches() {
        assertTrue(CpioArchiveInputStream.matches(new byte[] {'0','7','0','7','0','1'}, 6));
        assertTrue(CpioArchiveInputStream.matches(new byte[] {'0','7','0','7','0','2'}, 6));
        assertTrue(CpioArchiveInputStream.matches(new byte[] {'0','7','0','7','0','7'}, 6));
        assertTrue(CpioArchiveInputStream.matches(new byte[] {'0','7','0','7','0','1', 'x'}, 7));
        assertFalse(CpioArchiveInputStream.matches(new byte[] {'0','7','0','7','0','0'}, 6));
        assertFalse(CpioArchiveInputStream.matches(new byte[] {'0','7'}, 2));
        assertFalse(CpioArchiveInputStream.matches(null, 0));
    }

    @Test
    public void testGetNextEntryOnEmptyStream() throws Exception {
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        try {
            assertNull(in.getNextEntry());
        } finally {
            in.close();
        }
    }

    @Test
    public void testReadSingleEntryNewFormat() throws Exception {
        byte[] data = "Hello, World!".getBytes("UTF-8");
        byte[] archive = createArchive(CpioConstants.FORMAT_NEW, new String[] {"file.txt"}, new byte[][] {data});
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        try {
            ArchiveEntry entry = in.getNextEntry();
            assertNotNull(entry);
            assertEquals("file.txt", entry.getName());
            assertEquals(data.length, entry.getSize());
            assertArrayEquals(data, readAll(in));
            assertEquals(-1, in.read());
            assertNull(in.getNextEntry());
        } finally {
            in.close();
        }
    }

    @Test
    public void testReadSingleEntryNewCRCFormat() throws Exception {
        byte[] data = "CRC content".getBytes("UTF-8");
        byte[] archive = createArchive(CpioConstants.FORMAT_NEW_CRC, new String[] {"crc.txt"}, new byte[][] {data});
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        try {
            ArchiveEntry entry = in.getNextEntry();
            assertNotNull(entry);
            assertEquals("crc.txt", entry.getName());
            assertArrayEquals(data, readAll(in));
            assertNull(in.getNextEntry());
        } finally {
            in.close();
        }
    }

    @Test
    public void testReadSingleEntryOldASCIIFormat() throws Exception {
        byte[] data = "Old ASCII".getBytes("UTF-8");
        byte[] archive = createArchive(CpioConstants.FORMAT_OLD_ASCII, new String[] {"old.txt"}, new byte[][] {data});
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        try {
            ArchiveEntry entry = in.getNextEntry();
            assertNotNull(entry);
            assertEquals("old.txt", entry.getName());
            assertArrayEquals(data, readAll(in));
            assertNull(in.getNextEntry());
        } finally {
            in.close();
        }
    }

    @Test
    public void testReadSingleEntryOldBinaryFormat() throws Exception {
        byte[] data = "Binary".getBytes("UTF-8");
        byte[] archive = createArchive(CpioConstants.FORMAT_OLD_BINARY, new String[] {"bin.txt"}, new byte[][] {data});
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        try {
            ArchiveEntry entry = in.getNextEntry();
            assertNotNull(entry);
            assertEquals("bin.txt", entry.getName());
            assertArrayEquals(data, readAll(in));
            assertNull(in.getNextEntry());
        } finally {
            in.close();
        }
    }

    @Test
    public void testMultipleEntries() throws Exception {
        String[] names = {"a.txt", "b.txt", "c.txt"};
        byte[][] data = {"aaa".getBytes("UTF-8"), "bbbb".getBytes("UTF-8"), "ccccc".getBytes("UTF-8")};
        byte[] archive = createArchive(CpioConstants.FORMAT_NEW, names, data);
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        try {
            for (int i = 0; i < names.length; i++) {
                ArchiveEntry e = in.getNextEntry();
                assertNotNull(e);
                assertEquals(names[i], e.getName());
                assertEquals(data[i].length, e.getSize());
                assertArrayEquals(data[i], readAll(in));
            }
            assertNull(in.getNextEntry());
        } finally {
            in.close();
        }
    }

    @Test
    public void testZeroLengthEntry() throws Exception {
        byte[] archive = createArchive(CpioConstants.FORMAT_NEW, new String[] {"empty.txt"}, new byte[][] {new byte[0]});
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        try {
            ArchiveEntry e = in.getNextEntry();
            assertNotNull(e);
            assertEquals(0, e.getSize());
            assertEquals(-1, in.read());
            assertEquals(0, in.read(new byte[0], 0, 0));
            assertNull(in.getNextEntry());
        } finally {
            in.close();
        }
    }

    @Test
    public void testReadZeroLengthBufferWithoutEntry() throws Exception {
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        try {
            assertEquals(0, in.read(new byte[0], 0, 0));
        } finally {
            in.close();
        }
    }

    @Test
    public void testReadZeroLengthBufferAfterConsumingEntry() throws Exception {
        byte[] data = "abc".getBytes("UTF-8");
        byte[] archive = createArchive(CpioConstants.FORMAT_NEW, new String[] {"data"}, new byte[][] {data});
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        try {
            in.getNextEntry();
            assertArrayEquals(data, readAll(in));
            assertEquals(0, in.read(new byte[0], 0, 0));
        } finally {
            in.close();
        }
    }

    @Test
    public void testReadByteArrayWithOffset() throws Exception {
        byte[] data = "HelloWorld".getBytes("UTF-8");
        byte[] archive = createArchive(CpioConstants.FORMAT_NEW, new String[] {"hello"}, new byte[][] {data});
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        try {
            in.getNextEntry();
            byte[] buf = new byte[20];
            assertEquals(5, in.read(buf, 5, 5));
            assertEquals("Hello", new String(buf, 5, 5, "UTF-8"));
            assertEquals(5, in.read(buf, 0, 10));
            assertEquals("World", new String(buf, 0, 5, "UTF-8"));
            assertEquals(-1, in.read(buf, 0, 1));
        } finally {
            in.close();
        }
    }

    @Test
    public void testReadSingleBytes() throws Exception {
        byte[] data = "XYZ".getBytes("UTF-8");
        byte[] archive = createArchive(CpioConstants.FORMAT_NEW, new String[] {"xyz"}, new byte[][] {data});
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        try {
            in.getNextEntry();
            assertEquals('X', in.read());
            assertEquals('Y', in.read());
            assertEquals('Z', in.read());
            assertEquals(-1, in.read());
        } finally {
            in.close();
        }
    }

    @Test
    public void testSkipWithinEntry() throws Exception {
        byte[] data = "0123456789".getBytes("UTF-8");
        byte[] archive = createArchive(CpioConstants.FORMAT_NEW, new String[] {"digits"}, new byte[][] {data});
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        try {
            in.getNextEntry();
            assertEquals(4, in.skip(4));
            assertEquals('4', in.read());
            assertEquals(5, in.skip(100));
            assertEquals(-1, in.read());
        } finally {
            in.close();
        }
    }

    @Test
    public void testCloseEntrySkipsRemaining() throws Exception {
        byte[] archive = createArchive(CpioConstants.FORMAT_NEW,
                new String[] {"alpha", "beta"},
                new byte[][] {"abcdefghij".getBytes("UTF-8"), "b".getBytes("UTF-8")});
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        try {
            ArchiveEntry alpha = in.getNextEntry();
            assertNotNull(alpha);
            byte[] buf = new byte[2];
            assertEquals(2, in.read(buf));
            assertEquals("ab", new String(buf, "UTF-8"));
            in.closeEntry();
            ArchiveEntry beta = in.getNextEntry();
            assertNotNull(beta);
            assertEquals("beta", beta.getName());
            assertArrayEquals("b".getBytes("UTF-8"), readAll(in));
            assertNull(in.getNextEntry());
        } finally {
            in.close();
        }
    }

    @Test
    public void testCloseEntryTwice() throws Exception {
        byte[] archive = createArchive(CpioConstants.FORMAT_NEW, new String[] {"a"}, new byte[][] {"a".getBytes("UTF-8")});
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        try {
            in.getNextEntry();
            in.closeEntry();
            in.closeEntry();
            assertNull(in.getNextEntry());
        } finally {
            in.close();
        }
    }

    @Test
    public void testInvalidMagic() throws Exception {
        byte[] bad = new byte[] {'0','7','0','7','0','8','0','1','2','3','4','5','6','7','8','9'};
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(bad));
        try {
            in.getNextEntry();
            fail("Expected IOException for unknown magic");
        } catch (IOException expected) {
        } finally {
            in.close();
        }
    }

    @Test
    public void testTruncatedHeader() throws Exception {
        byte[] truncated = new byte[] {'0','7'};
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(truncated));
        try {
            in.getNextEntry();
            fail("Expected IOException for truncated header");
        } catch (IOException expected) {
        } finally {
            in.close();
        }
    }

    @Test
    public void testInvalidReadArguments() throws Exception {
        byte[] archive = createArchive(CpioConstants.FORMAT_NEW, new String[] {"a"}, new byte[][] {"abc".getBytes("UTF-8")});
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        try {
            in.getNextEntry();
            byte[] buf = new byte[10];
            try {
                in.read(buf, -1, 0);
                fail("Expected IndexOutOfBoundsException");
            } catch (IndexOutOfBoundsException expected) {
            }
            try {
                in.read(buf, 0, -1);
                fail("Expected IndexOutOfBoundsException");
            } catch (IndexOutOfBoundsException expected) {
            }
            try {
                in.read(buf, 0, 11);
                fail("Expected IndexOutOfBoundsException");
            } catch (IndexOutOfBoundsException expected) {
            }
            try {
                in.read(null, 0, 1);
                fail("Expected NullPointerException");
            } catch (NullPointerException expected) {
            }
        } finally {
            in.close();
        }
    }

    @Test
    public void testCloseAndReadAfterClose() throws Exception {
        byte[] archive = createArchive(CpioConstants.FORMAT_NEW, new String[] {"a"}, new byte[][] {"a".getBytes("UTF-8")});
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        in.close();
        in.close();
        try {
            in.read();
            fail("Expected IOException after close");
        } catch (IOException expected) {
        }
        try {
            in.getNextEntry();
            fail("Expected IOException after close");
        } catch (IOException expected) {
        }
    }

    @Test
    public void testGetNextCPIOEntryDirectly() throws Exception {
        byte[] data = "direct".getBytes("UTF-8");
        byte[] archive = createArchive(CpioConstants.FORMAT_NEW, new String[] {"d"}, new byte[][] {data});
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        try {
            CpioArchiveEntry e = in.getNextCPIOEntry();
            assertNotNull(e);
            assertEquals("d", e.getName());
            assertArrayEquals(data, readAll(in));
            assertNull(in.getNextCPIOEntry());
        } finally {
            in.close();
        }
    }

    @Test
    public void testCustomBlockSizeConstructor() throws Exception {
        byte[] archive = createArchive(CpioConstants.FORMAT_NEW, new String[] {"x"}, new byte[][] {"x".getBytes("UTF-8")});
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive), 64);
        try {
            ArchiveEntry e = in.getNextEntry();
            assertNotNull(e);
            assertEquals("x", e.getName());
            assertEquals('x', in.read());
            assertEquals(-1, in.read());
            assertNull(in.getNextEntry());
        } finally {
            in.close();
        }
    }
}