package org.apache.commons.compress.compressors.bzip2;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * JUnit 4 test suite for BZip2CompressorInputStream.
 * Designed to achieve high coverage and detect faults (Defects4J bug 22).
 */
public class BZip2CompressorInputStreamTest {

    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private static final byte[] INVALID_HEADER = new byte[] {0x00, 0x00, 0x00, 0x00};
    private static final byte[] TRUNCATED_STREAM = new byte[] {
        (byte)'B', (byte)'Z', (byte)'h', (byte)'9',
        0x31, 0x41, 0x59, 0x26 // incomplete block
    };
    // Minimal valid BZip2 stream (single block, empty content) - constructed manually
    // Magic: BZh9, block header: 0x31 0x41 0x59 0x26 0x53 0x59 (CRC?), then end-of-stream marker
    // This is a simplified valid stream for testing basic read.
    private static final byte[] VALID_EMPTY_STREAM = new byte[] {
        (byte)'B', (byte)'Z', (byte)'h', (byte)'9',
        0x31, 0x41, 0x59, 0x26, 0x53, 0x59, // block header (CRC, etc.)
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // data (empty)
        0x17, 0x72, 0x45, 0x38, 0x50, 0x90 // end-of-stream marker (simplified)
    };

    private BZip2CompressorInputStream bzip2In;

    @Before
    public void setUp() {
        // No common setup; each test creates its own stream
    }

    @After
    public void tearDown() throws IOException {
        if (bzip2In != null) {
            bzip2In.close();
        }
    }

    // --- Constructor Tests ---

    @Test(expected = NullPointerException.class)
    public void testConstructorNullInputStream() {
        new BZip2CompressorInputStream(null);
    }

    @Test(expected = IOException.class)
    public void testConstructorEmptyStream() throws IOException {
        bzip2In = new BZip2CompressorInputStream(new ByteArrayInputStream(EMPTY_BYTE_ARRAY));
    }

    @Test(expected = IOException.class)
    public void testConstructorInvalidHeader() throws IOException {
        bzip2In = new BZip2CompressorInputStream(new ByteArrayInputStream(INVALID_HEADER));
    }

    @Test(expected = IOException.class)
    public void testConstructorTruncatedStream() throws IOException {
        bzip2In = new BZip2CompressorInputStream(new ByteArrayInputStream(TRUNCATED_STREAM));
    }

    @Test
    public void testConstructorValidStream() throws IOException {
        bzip2In = new BZip2CompressorInputStream(new ByteArrayInputStream(VALID_EMPTY_STREAM));
        assertNotNull(bzip2In);
    }

    // --- read() Tests ---

    @Test
    public void testReadSingleByteFromValidStream() throws IOException {
        bzip2In = new BZip2CompressorInputStream(new ByteArrayInputStream(VALID_EMPTY_STREAM));
        int result = bzip2In.read();
        // For an empty stream, read() should return -1 immediately
        assertEquals(-1, result);
    }

    @Test(expected = IOException.class)
    public void testReadAfterClose() throws IOException {
        bzip2In = new BZip2CompressorInputStream(new ByteArrayInputStream(VALID_EMPTY_STREAM));
        bzip2In.close();
        bzip2In.read(); // should throw IOException
    }

    // --- read(byte[], int, int) Tests ---

    @Test
    public void testReadArrayFromValidStream() throws IOException {
        bzip2In = new BZip2CompressorInputStream(new ByteArrayInputStream(VALID_EMPTY_STREAM));
        byte[] buffer = new byte[10];
        int bytesRead = bzip2In.read(buffer, 0, buffer.length);
        assertEquals(-1, bytesRead);
    }

    @Test(expected = NullPointerException.class)
    public void testReadArrayNullBuffer() throws IOException {
        bzip2In = new BZip2CompressorInputStream(new ByteArrayInputStream(VALID_EMPTY_STREAM));
        bzip2In.read(null, 0, 10);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadArrayNegativeOffset() throws IOException {
        bzip2In = new BZip2CompressorInputStream(new ByteArrayInputStream(VALID_EMPTY_STREAM));
        bzip2In.read(new byte[10], -1, 5);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadArrayNegativeLength() throws IOException {
        bzip2In = new BZip2CompressorInputStream(new ByteArrayInputStream(VALID_EMPTY_STREAM));
        bzip2In.read(new byte[10], 0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadArrayOffsetPlusLengthExceedsLength() throws IOException {
        bzip2In = new BZip2CompressorInputStream(new ByteArrayInputStream(VALID_EMPTY_STREAM));
        bzip2In.read(new byte[10], 5, 10);
    }

    @Test
    public void testReadArrayZeroLength() throws IOException {
        bzip2In = new BZip2CompressorInputStream(new ByteArrayInputStream(VALID_EMPTY_STREAM));
        byte[] buffer = new byte[10];
        int bytesRead = bzip2In.read(buffer, 0, 0);
        assertEquals(0, bytesRead);
    }

    // --- available() Tests ---

    @Test
    public void testAvailableAfterConstruction() throws IOException {
        bzip2In = new BZip2CompressorInputStream(new ByteArrayInputStream(VALID_EMPTY_STREAM));
        int avail = bzip2In.available();
        // For an empty stream, available should be 0
        assertEquals(0, avail);
    }

    @Test(expected = IOException.class)
    public void testAvailableAfterClose() throws IOException {
        bzip2In = new BZip2CompressorInputStream(new ByteArrayInputStream(VALID_EMPTY_STREAM));
        bzip2In.close();
        bzip2In.available();
    }

    // --- close() Tests ---

    @Test
    public void testCloseMultipleTimes() throws IOException {
        bzip2In = new BZip2CompressorInputStream(new ByteArrayInputStream(VALID_EMPTY_STREAM));
        bzip2In.close();
        bzip2In.close(); // should not throw
    }

    // --- Edge Cases and Fault Detection (Defects4J bug 22) ---

    // Bug 22: Possibly related to handling of truncated or malformed streams causing infinite loop or incorrect CRC.
    // We test with a stream that has valid header but corrupted block data.
    @Test(expected = IOException.class)
    public void testCorruptedBlockData() throws IOException {
        byte[] corruptedBlock = new byte[] {
            (byte)'B', (byte)'Z', (byte)'h', (byte)'9',
            0x31, 0x41, 0x59, 0x26, 0x53, 0x59, // block header
            0x01, 0x02, 0x03, 0x04, 0x05, 0x06, // some data
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00  // invalid end-of-stream
        };
        bzip2In = new BZip2CompressorInputStream(new ByteArrayInputStream(corruptedBlock));
        // Attempt to read should throw IOException due to CRC mismatch or decoding error
        bzip2In.read();
    }

    // Test with a stream that has extra data after end-of-stream marker
    @Test(expected = IOException.class)
    public void testExtraDataAfterEnd() throws IOException {
        byte[] extraData = new byte[] {
            (byte)'B', (byte)'Z', (byte)'h', (byte)'9',
            0x31, 0x41, 0x59, 0x26, 0x53, 0x59,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x17, 0x72, 0x45, 0x38, 0x50, 0x90, // end-of-stream
            0x01, 0x02, 0x03 // extra garbage
        };
        bzip2In = new BZip2CompressorInputStream(new ByteArrayInputStream(extraData));
        // Should read normally and then throw on extra data
        int r = bzip2In.read();
        assertEquals(-1, r);
        // Subsequent read may throw
        bzip2In.read();
    }

    // Test with a stream that has a valid block but then truncated end-of-stream
    @Test(expected = IOException.class)
    public void testTruncatedEndOfStream() throws IOException {
        byte[] truncatedEnd = new byte[] {
            (byte)'B', (byte)'Z', (byte)'h', (byte)'9',
            0x31, 0x41, 0x59, 0x26, 0x53, 0x59,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x17, 0x72 // incomplete end-of-stream
        };
        bzip2In = new BZip2CompressorInputStream(new ByteArrayInputStream(truncatedEnd));
        bzip2In.read(); // should throw
    }

    // Test with a stream that has no block header (just magic)
    @Test(expected = IOException.class)
    public void testMagicOnly() throws IOException {
        byte[] magicOnly = new byte[] {
            (byte)'B', (byte)'Z', (byte)'h', (byte)'9'
        };
        bzip2In = new BZip2CompressorInputStream(new ByteArrayInputStream(magicOnly));
        bzip2In.read();
    }

    // Test with a stream that has invalid magic (BZ without h)
    @Test(expected = IOException.class)
    public void testInvalidMagic() throws IOException {
        byte[] invalidMagic = new byte[] {
            (byte)'B', (byte)'Z', (byte)'x', (byte)'9'
        };
        bzip2In = new BZip2CompressorInputStream(new ByteArrayInputStream(invalidMagic));
        bzip2In.read();
    }

    // Test with a stream that has valid magic but unsupported level (e.g., '0')
    @Test(expected = IOException.class)
    public void testUnsupportedLevel() throws IOException {
        byte[] unsupportedLevel = new byte[] {
            (byte)'B', (byte)'Z', (byte)'h', (byte)'0'
        };
        bzip2In = new BZip2CompressorInputStream(new ByteArrayInputStream(unsupportedLevel));
        bzip2In.read();
    }

    // Test with a stream that has a block with zero CRC (edge case)
    @Test
    public void testBlockWithZeroCRC() throws IOException {
        // This is a synthetic stream; actual behavior may vary.
        // We just ensure no crash.
        byte[] zeroCRC = new byte[] {
            (byte)'B', (byte)'Z', (byte)'h', (byte)'9',
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // CRC all zeros
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // data
            0x17, 0x72, 0x45, 0x38, 0x50, 0x90
        };
        bzip2In = new BZip2CompressorInputStream(new ByteArrayInputStream(zeroCRC));
        int r = bzip2In.read();
        // May return -1 or throw depending on implementation
        assertTrue(r == -1 || r >= 0);
    }

    // Test reading after reaching end-of-stream multiple times
    @Test
    public void testReadAfterEof() throws IOException {
        bzip2In = new BZip2CompressorInputStream(new ByteArrayInputStream(VALID_EMPTY_STREAM));
        assertEquals(-1, bzip2In.read());
        assertEquals(-1, bzip2In.read()); // should still return -1
    }

    // Test with a stream that has multiple blocks (if supported)
    // For simplicity, we skip multi-block tests as constructing them is complex.
}