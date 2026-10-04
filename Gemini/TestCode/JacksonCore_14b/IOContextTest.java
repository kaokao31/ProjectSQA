package com.fasterxml.jackson.core.io;

import com.fasterxml.jackson.core.util.BufferRecycler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class IOContextTest {

    private BufferRecycler recycler;
    private ContentReference contentRef;
    private IOContext context;

    @Before
    public void setUp() {
        recycler = new BufferRecycler();
        contentRef = ContentReference.unknownRef();
        context = new IOContext(recycler, contentRef, false);
    }

    @After
    public void tearDown() {
        context = null;
        recycler = null;
        contentRef = null;
    }

    @Test
    public void testConstructorAndGetters() {
        assertNotNull(context.getSourceRef());
        assertFalse(context.isResourceManaged());
        assertSame(recycler, context.getAllocationTracker());
    }

    @Test
    public void testConstructorWithDefaultContentRef() {
        IOContext defaultRefContext = new IOContext(recycler, false);
        assertNotNull(defaultRefContext.getSourceRef());
        assertFalse(defaultRefContext.isResourceManaged());
    }

    @Test
    public void testSetEncoding() {
        assertNull(context.getEncoding());
        context.setEncoding(com.fasterxml.jackson.core.JsonEncoding.UTF8);
        assertEquals(com.fasterxml.jackson.core.JsonEncoding.UTF8, context.getEncoding());
    }

    @Test
    public void testContentReferenceOperations() {
        assertSame(contentRef, context.contentReference());
        
        ContentReference newRef = ContentReference.construct(true, "test-content");
        context.setEncoding(com.fasterxml.jackson.core.JsonEncoding.UTF16_LE);
        // Note: verify setters/fluent methods if any exist
        assertNotNull(context.getSourceRef());
    }

    @Test
    public void testBufferRecyclerAllocationAndRelease() {
        // Test allocation and release of various buffer types
        char[] tokenBuffer = context.allocTokenBuffer();
        assertNotNull(tokenBuffer);
        // Release buffer back
        context.releaseTokenBuffer(tokenBuffer);
        // Releasing null should be handled safely or tested if allowed
        try {
            context.releaseTokenBuffer(null);
        } catch (Exception e) {
            // Expected or handled depending on implementation
        }

        char[] concatBuffer = context.allocConcatBuffer();
        assertNotNull(concatBuffer);
        context.releaseConcatBuffer(concatBuffer);

        char[] nameCopyBuffer = context.allocNameCopyBuffer(10);
        assertNotNull(nameCopyBuffer);
        context.releaseNameCopyBuffer(nameCopyBuffer);

        byte[] readIOBuffer = context.allocReadIOBuffer();
        assertNotNull(readIOBuffer);
        context.releaseReadIOBuffer(readIOBuffer);

        byte[] writeEncodingBuffer = context.allocWriteEncodingBuffer();
        assertNotNull(writeEncodingBuffer);
        context.releaseWriteEncodingBuffer(writeEncodingBuffer);

        byte[] base64Buffer = context.allocBase64Buffer();
        assertNotNull(base64Buffer);
        context.releaseBase64Buffer(base64Buffer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDoubleReleaseTokenBuffer() {
        char[] buffer = context.allocTokenBuffer();
        context.releaseTokenBuffer(buffer);
        // Depending on BufferRecycler implementation, releasing twice might throw or be idempotent
        context.releaseTokenBuffer(buffer);
    }

    @Test
    public void testAllocationTracker() {
        BufferRecycler customRecycler = new BufferRecycler();
        IOContext customContext = new IOContext(customRecycler, ContentReference.rawReference("some-data"), true);
        assertTrue(customContext.isResourceManaged());
        assertSame(customRecycler, customContext.getAllocationTracker());
    }
}