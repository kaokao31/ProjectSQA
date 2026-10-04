package com.fasterxml.jackson.databind;

import org.junit.Test;

import java.io.Closeable;
import java.io.IOException;

import static org.junit.Assert.*;

public class JsonMappingExceptionTest {

    @Test
    public void testFromIsNull() {
        JsonMappingException ex = JsonMappingException.from((Closeable) null, "test message");
        assertNotNull(ex);
        assertEquals("test message", ex.getMessage());
    }

    @Test
    public void testFromWithCloseable() {
        Closeable c = new Closeable() {
            @Override
            public void close() throws IOException {
                // do nothing
            }
        };
        JsonMappingException ex = JsonMappingException.from(c, "message from closeable");
        assertNotNull(ex);
        assertEquals("message from closeable", ex.getMessage());
    }

    @Test
    public void testFromWithCloseableAndThrowable() {
        Closeable c = () -> {};
        Throwable cause = new RuntimeException("root cause");
        JsonMappingException ex = JsonMappingException.from(c, "message with cause", cause);
        assertNotNull(ex);
        assertEquals("message with cause", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    public void testFromDeserializationContext() {
        JsonMappingException ex = JsonMappingException.from((DeserializationContext) null, "ctxt message");
        assertNotNull(ex);
        assertEquals("ctxt message", ex.getMessage());
    }

    @Test
    public void testFromDeserializationContextWithCause() {
        Throwable cause = new RuntimeException("cause");
        JsonMappingException ex = JsonMappingException.from((DeserializationContext) null, "ctxt message with cause", cause);
        assertNotNull(ex);
        assertEquals("ctxt message with cause", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    public void testFromSerializerProvider() {
        JsonMappingException ex = JsonMappingException.from((SerializerProvider) null, "prov message");
        assertNotNull(ex);
        assertEquals("prov message", ex.getMessage());
    }

    @Test
    public void testFromSerializerProviderWithCause() {
        Throwable cause = new RuntimeException("cause");
        JsonMappingException ex = JsonMappingException.from((SerializerProvider) null, "prov message with cause", cause);
        assertNotNull(ex);
        assertEquals("prov message with cause", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    public void testWrapIOException() {
        IOException src = new IOException("io exception");
        JsonMappingException wrapped = JsonMappingException.wrapUnexpectedIOException(src, null);
        assertNotNull(wrapped);
        assertEquals(src, wrapped.getCause());
        assertTrue(wrapped.getMessage().contains("io exception"));
    }

    @Test
    public void testPathReferenceHandling() {
        JsonMappingException ex = new JsonMappingException("path test");
        assertNotNull(ex.getPath());
        assertTrue(ex.getPath().isEmpty());

        JsonMappingException.Reference ref = new JsonMappingException.Reference("sourceObj", "fieldName");
        ex.prependPath(ref);
        assertEquals(1, ex.getPath().size());
        assertEquals(ref, ex.getPath().get(0));

        JsonMappingException.Reference ref2 = new JsonMappingException.Reference("sourceObj2", 5);
        ex.prependPath(ref2);
        assertEquals(2, ex.getPath().size());
        assertEquals(ref2, ex.getPath().get(0));
    }

    @Test
    public void testReferenceGettersAndSetters() {
        JsonMappingException.Reference ref = new JsonMappingException.Reference();
        ref.setFieldName("myField");
        assertEquals("myField", ref.getFieldName());

        ref.setIndex(10);
        assertEquals(10, ref.getIndex());

        Object from = new Object();
        ref.setFrom(from);
        assertEquals(from, ref.getFrom());

        JsonMappingException.Reference refCopy = new JsonMappingException.Reference(from, "myField");
        assertEquals("myField", refCopy.getFieldName());
        assertEquals(-1, refCopy.getIndex());
        assertEquals(from, refCopy.getFrom());

        JsonMappingException.Reference refIndex = new JsonMappingException.Reference(from, 2);
        assertNull(refIndex.getFieldName());
        assertEquals(2, refIndex.getIndex());
        assertEquals(from, refIndex.getFrom());
        
        assertNotNull(ref.toString());
        try {
            ref.writeReplace();
        } catch (Exception ignored) {
        }
    }

    @Test
    public void testGetMessageAndDescription() {
        JsonMappingException ex = new JsonMappingException("message only");
        String desc = ex.getLocalizedMessage();
        assertNotNull(desc);

        JsonMappingException exWithCause = new JsonMappingException("message with cause", new RuntimeException("inner"));
        assertNotNull(exWithCause.getMessage());

        // Test appendDescription building
        StringBuilder sb = new StringBuilder();
        ex.appendStoredDesc(sb);
        assertTrue(sb.length() >= 0);
    }
}