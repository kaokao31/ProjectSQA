package org.apache.commons.compress.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class BitInputStreamTest {

    private BitInputStream createBitInputStream(byte[] data, BitOrder order) {
        InputStream in = new ByteArrayInputStream(data);
        return new BitInputStream(in, order);
    }

    @Test
    public void testReadBitsZero() throws IOException {
        byte[] data = { (byte)0xFF };
        BitInputStream bis = createBitInputStream(data, BitOrder.BIG_ENDIAN);
        assertEquals(0, bis.readBits(0));
        assertEquals(0xFF, bis.readBits(8));
    }

    @Test
    public void testReadBitsOne() throws IOException {
        byte[] data = { (byte)0x80 };
        BitInputStream bis = createBitInputStream(data, BitOrder.BIG_ENDIAN);
        assertEquals(1, bis.readBits(1));
        assertEquals(0, bis.readBits(7));
    }

    @Test
    public void testReadBitsMultiple() throws IOException {
        byte[] data = { (byte)0xAB, (byte)0xCD };
        BitInputStream bis = createBitInputStream(data, BitOrder.BIG_ENDIAN);
        assertEquals(10, bis.readBits(4));
        assertEquals(23, bis.readBits(5));
        assertEquals(77, bis.readBits(7));
    }

    @Test
    public void testReadBitsAllAtOnce() throws IOException {
        byte[] data = { (byte)0x12, (byte)0x34 };
        BitInputStream bis = createBitInputStream(data, BitOrder.BIG_ENDIAN);
        assertEquals(0x1234, bis.readBits(16));
    }

    @Test(expected = IOException.class)
    public void testReadBitsMoreThanAvailable() throws IOException {
        byte[] data = { (byte)0xFF };
        BitInputStream bis = createBitInputStream(data, BitOrder.BIG_ENDIAN);
        bis.readBits(9);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadBitsNegativeCount() throws IOException {
        byte[] data = { (byte)0xFF };
        BitInputStream bis = createBitInputStream(data, BitOrder.BIG_ENDIAN);
        bis.readBits(-1);
    }

    @Test
    public void testReadBitsBigEndian() throws IOException {
        byte[] data = { (byte)0xAB, (byte)0xCD };
        BitInputStream bis = createBitInputStream(data, BitOrder.BIG_ENDIAN);
        assertEquals(0xAB, bis.readBits(8));
        assertEquals(0xCD, bis.readBits(8));
    }

    @Test
    public void testReadBitsLittleEndian() throws IOException {
        byte[] data = { (byte)0xAB };
        BitInputStream bis = createBitInputStream(data, BitOrder.LITTLE_ENDIAN);
        assertEquals(0xD5, bis.readBits(8));
    }

    @Test(expected = IOException.class)
    public void testReadBitsAfterClose() throws IOException {
        byte[] data = { (byte)0xFF };
        BitInputStream bis = createBitInputStream(data, BitOrder.BIG_ENDIAN);
        bis.close();
        bis.readBits(1);
    }

    @Test
    public void testReadBitsLargeCount() throws IOException {
        byte[] data = { (byte)0x12, (byte)0x34, (byte)0x56, (byte)0x78 };
        BitInputStream bis = createBitInputStream(data, BitOrder.BIG_ENDIAN);
        assertEquals(0x12345678, bis.readBits(32));
    }

    @Test
    public void testReadBitsLargeCountSignExtension() throws IOException {
        byte[] data = { (byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00 };
        BitInputStream bis = createBitInputStream(data, BitOrder.BIG_ENDIAN);
        assertEquals(0x80000000, bis.readBits(32));
    }

    @Test
    public void testReadBitsAcrossByteBoundary() throws IOException {
        byte[] data = { (byte)0x80, (byte)0x00 };
        BitInputStream bis = createBitInputStream(data, BitOrder.BIG_ENDIAN);
        assertEquals(256, bis.readBits(9));
    }

    @Test
    public void testReadBitsSequence() throws IOException {
        byte[] data = { (byte)0xAA, (byte)0xBB, (byte)0xCC };
        BitInputStream bis = createBitInputStream(data, BitOrder.BIG_ENDIAN);
        assertEquals(0xAA, bis.readBits(8));
        assertEquals(0xBB, bis.readBits(8));
        assertEquals(0xCC, bis.readBits(8));
    }

    @Test(expected = IOException.class)
    public void testReadBitsFromEmptyStream() throws IOException {
        byte[] data = {};
        BitInputStream bis = createBitInputStream(data, BitOrder.BIG_ENDIAN);
        bis.readBits(1);
    }

    @Test
    public void testReadBitsZeroLittleEndian() throws IOException {
        byte[] data = { (byte)0xFF };
        BitInputStream bis = createBitInputStream(data, BitOrder.LITTLE_ENDIAN);
        assertEquals(0, bis.readBits(0));
        assertEquals(0xFF, bis.readBits(8));
    }

    @Test
    public void testReadBitsLittleEndianBitByBit() throws IOException {
        byte[] data = { (byte)0xAB };
        BitInputStream bis = createBitInputStream(data, BitOrder.LITTLE_ENDIAN);
        assertEquals(1, bis.readBits(1));
        assertEquals(1, bis.readBits(1));
        assertEquals(0, bis.readBits(1));
        assertEquals(1, bis.readBits(1));
        assertEquals(0, bis.readBits(1));
        assertEquals(1, bis.readBits(1));
        assertEquals(0, bis.readBits(1));
        assertEquals(1, bis.readBits(1));
    }

    @Test(expected = IOException.class)
    public void testReadBitsExactThenMore() throws IOException {
        byte[] data = { (byte)0xFF };
        BitInputStream bis = createBitInputStream(data, BitOrder.BIG_ENDIAN);
        bis.readBits(8);
        bis.readBits(1);
    }

    @Test
    public void testReadBitsLittleEndianMultipleBytes() throws IOException {
        byte[] data = { (byte)0x01, (byte)0x02 };
        BitInputStream bis = createBitInputStream(data, BitOrder.LITTLE_ENDIAN);
        assertEquals(0x4080, bis.readBits(16));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullOrder() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        new BitInputStream(in, null);
    }

    @Test
    public void testReadBitsZeroFromEmptyStream() throws IOException {
        byte[] data = {};
        BitInputStream bis = createBitInputStream(data, BitOrder.BIG_ENDIAN);
        assertEquals(0, bis.readBits(0));
    }
}