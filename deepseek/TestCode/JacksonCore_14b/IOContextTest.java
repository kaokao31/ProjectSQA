package com.fasterxml.jackson.core.io;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link IOContext} focusing on buffer allocation, recycling,
 * and edge cases to uncover potential faults.
 */
public class IOContextTest {

    private static final int DEFAULT_SIZE = 4000;
    private static final int SMALL_SIZE = 100;
    private static final int LARGE_SIZE = 8000;
    private static final int MAX_SIZE = Integer.MAX_VALUE;

    private IOContext context;
    private Object sourceRef;

    @Before
    public void setUp() {
        sourceRef = new Object();
        context = new IOContext(null, sourceRef, true); // managed resource
    }

    // ===================================================================
    // Constructor tests
    // ===================================================================

    @Test
    public void testConstructorWithNullIOContext() {
        IOContext ctx = new IOContext(null, sourceRef, false);
        assertNotNull("Context should not be null", ctx);
        assertFalse("Not managed", ctx.isResourceManaged());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullSourceRef() {
        new IOContext(null, null, true);
    }

    @Test
    public void testConstructorManagedTrue() {
        IOContext ctx = new IOContext(null, sourceRef, true);
        assertTrue("Should be managed", ctx.isResourceManaged());
    }

    @Test
    public void testConstructorManagedFalse() {
        IOContext ctx = new IOContext(null, sourceRef, false);
        assertFalse("Should not be managed", ctx.isResourceManaged());
    }

    // ===================================================================
    // allocReadBuffer tests
    // ===================================================================

    @Test
    public void testAllocReadBufferDefault() {
        byte[] buf = context.allocReadBuffer();
        assertNotNull("Read buffer should not be null", buf);
        assertEquals("Default size", DEFAULT_SIZE, buf.length);
    }

    @Test
    public void testAllocReadBufferMinimumSize() {
        byte[] buf = context.allocReadBuffer(SMALL_SIZE);
        assertNotNull(buf);
        assertTrue("Buffer size should be at least minimum", buf.length >= SMALL_SIZE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAllocReadBufferNegativeSize() {
        context.allocReadBuffer(-1);
    }

    @Test
    public void testAllocReadBufferZeroSize() {
        // Should allocate at least default size
        byte[] buf = context.allocReadBuffer(0);
        assertNotNull(buf);
        assertTrue(buf.length > 0);
    }

    @Test
    public void testAllocReadBufferLargeSize() {
        byte[] buf = context.allocReadBuffer(LARGE_SIZE);
        assertNotNull(buf);
        assertTrue("Buffer size >= " + LARGE_SIZE, buf.length >= LARGE_SIZE);
    }

    @Test
    public void testAllocReadBufferMaxSize() {
        byte[] buf = context.allocReadBuffer(MAX_SIZE);
        assertNotNull(buf);
        assertTrue(buf.length >= MAX_SIZE);
    }

    // ===================================================================
    // allocWriteBuffer tests
    // ===================================================================

    @Test
    public void testAllocWriteBufferDefault() {
        byte[] buf = context.allocWriteBuffer();
        assertNotNull(buf);
        assertEquals("Default write buffer size", DEFAULT_SIZE, buf.length);
    }

    @Test
    public void testAllocWriteBufferMinimumSize() {
        byte[] buf = context.allocWriteBuffer(SMALL_SIZE);
        assertNotNull(buf);
        assertTrue(buf.length >= SMALL_SIZE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAllocWriteBufferNegativeSize() {
        context.allocWriteBuffer(-10);
    }

    @Test
    public void testAllocWriteBufferZeroSize() {
        byte[] buf = context.allocWriteBuffer(0);
        assertNotNull(buf);
        assertTrue(buf.length > 0);
    }

    // ===================================================================
    // allocTokenBuffer tests (if present)
    // ===================================================================

    @Test
    public void testAllocTokenBufferDefault() {
        char[] buf = context.allocTokenBuffer();
        assertNotNull(buf);
        // Token buffer size depends on implementation; typically at least 2000
        assertTrue("Token buffer size >= 2000", buf.length >= 2000);
    }

    @Test
    public void testAllocTokenBufferMinimumSize() {
        char[] buf = context.allocTokenBuffer(SMALL_SIZE);
        assertNotNull(buf);
        assertTrue(buf.length >= SMALL_SIZE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAllocTokenBufferNegativeSize() {
        context.allocTokenBuffer(-5);
    }

    // ===================================================================
    // allocCharConversionBuffer tests (if present)
    // ===================================================================

    @Test
    public void testAllocCharConversionBufferDefault() {
        char[] buf = context.allocCharConversionBuffer();
        assertNotNull(buf);
        assertTrue(buf.length >= DEFAULT_SIZE);
    }

    // ===================================================================
    // releaseReadBuffer tests
    // ===================================================================

    @Test
    public void testReleaseReadBufferReusesBuffer() {
        byte[] buf1 = context.allocReadBuffer();
        context.releaseReadBuffer(buf1);
        byte[] buf2 = context.allocReadBuffer();
        assertSame("Buffer should be reused", buf1, buf2);
    }

    @Test
    public void testReleaseReadBufferWithWrongSize() {
        byte[] buf = new byte[1234];
        // Should not cause exception; simply ignore
        context.releaseReadBuffer(buf);
    }

    @Test
    public void testReleaseReadBufferNull() {
        // Should not throw
        context.releaseReadBuffer(null);
    }

    // ===================================================================
    // releaseWriteBuffer tests
    // ===================================================================

    @Test
    public void testReleaseWriteBufferReusesBuffer() {
        byte[] buf1 = context.allocWriteBuffer();
        context.releaseWriteBuffer(buf1);
        byte[] buf2 = context.allocWriteBuffer();
        assertSame("Write buffer should be reused", buf1, buf2);
    }

    @Test
    public void testReleaseWriteBufferNull() {
        context.releaseWriteBuffer(null);
    }

    // ===================================================================
    // releaseTokenBuffer tests
    // ===================================================================

    @Test
    public void testReleaseTokenBufferReusesBuffer() {
        char[] buf1 = context.allocTokenBuffer();
        context.releaseTokenBuffer(buf1);
        char[] buf2 = context.allocTokenBuffer();
        assertSame("Token buffer should be reused", buf1, buf2);
    }

    // ===================================================================
    // releaseCharConversionBuffer tests
    // ===================================================================

    @Test
    public void testReleaseCharConversionBufferReusesBuffer() {
        char[] buf1 = context.allocCharConversionBuffer();
        context.releaseCharConversionBuffer(buf1);
        char[] buf2 = context.allocCharConversionBuffer();
        assertSame("Char conversion buffer should be reused", buf1, buf2);
    }

    // ===================================================================
    // charset encoding tests
    // ===================================================================

    @Test
    public void testGetEncodingNotSet() {
        assertNull("Encoding should be null initially", context.getEncoding());
    }

    @Test
    public void testSetEncoding() {
        context.setEncoding("UTF-8");
        assertEquals("UTF-8", context.getEncoding());
    }

    @Test
    public void testSetEncodingNull() {
        context.setEncoding(null);
        assertNull(context.getEncoding());
    }

    // ===================================================================
    // Object handling
    // ===================================================================

    @Test
    public void testGetSourceReference() {
        assertSame("Source reference should be the same", sourceRef, context.getSourceReference());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetEncodingEmptyString() {
        context.setEncoding("");
    }

    // ===================================================================
    // Additional edge cases for buffer allocation with multiple buffers
    // ===================================================================

    @Test
    public void testMultipleAllocReadBufferReturnsDifferentBuffers() {
        byte[] buf1 = context.allocReadBuffer();
        byte[] buf2 = context.allocReadBuffer();
        assertNotSame("Should be different buffers", buf1, buf2);
    }

    @Test
    public void testAllocReadBufferAfterReleaseReturnsSame() {
        byte[] buf1 = context.allocReadBuffer();
        context.releaseReadBuffer(buf1);
        byte[] buf2 = context.allocReadBuffer();
        assertSame(buf1, buf2);
    }

    @Test
    public void testReleaseReadBufferThenAllocWriteBuffer() {
        byte[] rBuf = context.allocReadBuffer();
        context.releaseReadBuffer(rBuf);
        byte[] wBuf = context.allocWriteBuffer();
        // Should be a different buffer because they are separate pools
        assertNotSame("Read and write buffers are separate pools", rBuf, wBuf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAllocReadBufferExtremelyLarge() {
        context.allocReadBuffer(Integer.MAX_VALUE - 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAllocWriteBufferExtremelyLarge() {
        context.allocWriteBuffer(Integer.MAX_VALUE - 1);
    }

    // ===================================================================
    // Stress test (if needed)
    // ===================================================================

    @Test(timeout = 1000)
    public void testBufferAllocationRecyclingStress() {
        for (int i = 0; i < 1000; i++) {
            byte[] buf = context.allocReadBuffer(100);
            context.releaseReadBuffer(buf);
        }
        // Should not run out of memory or throw
    }

    // ===================================================================
    // Test that context is correctly marked as managed or not
    // ===================================================================

    @Test
    public void testIsResourceManagedTrue() {
        assertTrue(context.isResourceManaged());
    }

    @Test
    public void testIsResourceManagedFalse() {
        IOContext ctx = new IOContext(null, sourceRef, false);
        assertFalse(ctx.isResourceManaged());
    }

    // ===================================================================
    // Test behavior when sourceRef is an object that implements required
    // ===================================================================

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullSourceRefAndManagedTrue() {
        new IOContext(null, null, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullSourceRefAndManagedFalse() {
        new IOContext(null, null, false);
    }
}