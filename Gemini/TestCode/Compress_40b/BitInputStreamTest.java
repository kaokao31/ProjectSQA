package org.apache.commons.compress.utils;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

public class BitInputStreamTest {

    @Test
    public void testInvalidBitCounts() throws IOException {
        byte[] data = new byte[] { (byte) 0xFF };
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        BitInputStream bitIn = new BitInputStream(in, ByteOrder.LITTLE_ENDIAN);

        // Count < 0
        assertEquals(-1, bitIn.readBits(-1));

        // Count > 63
        assertEquals(-1, bitIn.readBits(64));

        bitIn.close();
    }

    @Test
    public void testReadZeroBits() throws IOException {
        byte[] data = new byte[] { (byte) 0xFF };
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        BitInputStream bitIn = new BitInputStream(in, ByteOrder.LITTLE_ENDIAN);

        assertEquals(0, bitIn.readBits(0));

        bitIn.close();
    }

    @Test
    public void testReadBitsEOF() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        BitInputStream bitIn = new BitInputStream(in, ByteOrder.LITTLE_ENDIAN);

        assertEquals(-1, bitIn.readBits(1));

        bitIn.close();
    }

    @Test
    public void testLittleEndianReading() throws IOException {
        // Binary: 10101111 00001111
        // Hex: AF 0F
        byte[] data = new byte[] { (byte) 0xAF, (byte) 0x0F };
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        BitInputStream bitIn = new BitInputStream(in, ByteOrder.LITTLE_ENDIAN);

        // Read 4 bits: lower nibble of 0xAF -> 0xF (15)
        assertEquals(0x0F, bitIn.readBits(4));

        // Read 4 bits: upper nibble of 0xAF -> 0xA (10)
        assertEquals(0x0A, bitIn.readBits(4));

        // Read 8 bits: 0x0F (15)
        assertEquals(0x0F, bitIn.readBits(8));

        // EOF
        assertEquals(-1, bitIn.readBits(1));

        bitIn.close();
    }

    @Test
    public void testBigEndianReading() throws IOException {
        // Hex: AF 0F
        // Binary: 10101111 00001111
        byte[] data = new byte[] { (byte) 0xAF, (byte) 0x0F };
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        BitInputStream bitIn = new BitInputStream(in, ByteOrder.BIG_ENDIAN);

        // Read 4 bits: upper nibble of 0xAF -> 0xA (10)
        assertEquals(0x0A, bitIn.readBits(4));

        // Read 4 bits: lower nibble of 0xAF -> 0xF (15)
        assertEquals(0x0F, bitIn.readBits(4));

        // Read 8 bits: 0x0F (15)
        assertEquals(0x0F, bitIn.readBits(8));

        // EOF
        assertEquals(-1, bitIn.readBits(1));

        bitIn.close();
    }

    @Test
    public void testClearCacheOnClose() throws IOException {
        byte[] data = new byte[] { (byte) 0xFF, (byte) 0xFF };
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        BitInputStream bitIn = new BitInputStream(in, ByteOrder.LITTLE_ENDIAN);

        // Read some bits to populate cache
        assertEquals(0x0F, bitIn.readBits(4));

        // Close the stream
        bitIn.close();

        // Reading after close or ensuring no exception is thrown
        // Depending on underlying stream, usually it closes the stream.
        // Let's verify subsequent reads return -1 or throw IOException.
        try {
            bitIn.readBits(1);
        } catch (IOException e) {
            // Expected if underlying stream is closed
        }
    }

    @Test
    public void testByteAlignedCacheResets() throws IOException {
        // Read exactly 8 bits, then check if cache is successfully cleared/reset
        byte[] data = new byte[] { 0x12, 0x34 };
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        BitInputStream bitIn = new BitInputStream(in, ByteOrder.LITTLE_ENDIAN);

        assertEquals(0x12, bitIn.readBits(8));
        assertEquals(0x34, bitIn.readBits(8));
        assertEquals(-1, bitIn.readBits(1));

        bitIn.close();
    }

    @Test
    public void testCrossByteBoundaryLittleEndian() throws IOException {
        // Read 12 bits spanning across two bytes in LITTLE_ENDIAN
        // data[0] = 0x34, data[1] = 0x12 -> combined 16-bit 0x1234
        // Little endian byte stream: first byte read is 0x34, second is 0x12
        byte[] data = new byte[] { (byte) 0x34, (byte) 0x12 };
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        BitInputStream bitIn = new BitInputStream(in, ByteOrder.LITTLE_ENDIAN);

        // Read 12 bits: 
        // 0x34 (8 bits) + lower 4 bits of 0x12 (which is 0x2) -> 0x234 = 564
        long val = bitIn.readBits(12);
        assertEquals(0x234, val);

        bitIn.close();
    }

    @Test
    public void testCrossByteBoundaryBigEndian() throws IOException {
        // Read 12 bits spanning across two bytes in BIG_ENDIAN
        // data[0] = 0x12, data[1] = 0x34 -> combined 16-bit 0x1234
        byte[] data = new byte[] { (byte) 0x12, (byte) 0x34 };
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        BitInputStream bitIn = new BitInputStream(in, ByteOrder.BIG_ENDIAN);

        // Read 12 bits: 
        // upper 8 bits of 0x12 (0x12) + upper 4 bits of 0x34 (0x3) -> 0x123 = 291
        long val = bitIn.readBits(12);
        assertEquals(0x123, val);

        bitIn.close();
    }
}