package com.fasterxml.jackson.core.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.Assert.*;

public class JsonGeneratorImplTest {

    private JsonFactory factory;
    private ByteArrayOutputStream outputStream;
    private JsonGenerator generator;

    @Before
    public void setUp() throws IOException {
        factory = new JsonFactory();
        outputStream = new ByteArrayOutputStream();
        generator = factory.createGenerator(outputStream);
    }

    @Test
    public void testWriteSimpleObject() throws IOException {
        generator.writeStartObject();
        generator.writeStringField("name", "value");
        generator.writeEndObject();
        generator.close();

        String json = outputStream.toString("UTF-8");
        assertTrue(json.contains("\"name\""));
        assertTrue(json.contains("\"value\""));
    }

    @Test
    public void testWriteNullFieldName() throws IOException {
        generator.writeStartObject();
        try {
            generator.writeFieldName(null);
            fail("Expected JsonParseException for null field name");
        } catch (JsonParseException e) {
            // expected
        }
        generator.close();
    }

    @Test
    public void testWriteAfterClose() throws IOException {
        generator.close();
        try {
            generator.writeStartObject();
            fail("Expected IOException when writing after close");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void testFlush() throws IOException {
        generator.writeStartObject();
        generator.writeEndObject();
        generator.flush();
        String json = outputStream.toString("UTF-8");
        assertNotNull(json);
        assertFalse(json.isEmpty());
        generator.close();
    }

    @Test
    public void testCloseFlushesAndClosesStream() throws IOException {
        OutputStream spyStream = new OutputStream() {
            boolean closed = false;
            boolean flushed = false;

            @Override
            public void write(int b) throws IOException {
                // no-op
            }

            @Override
            public void flush() throws IOException {
                flushed = true;
            }

            @Override
            public void close() throws IOException {
                closed = true;
            }
        };

        JsonGenerator gen = factory.createGenerator(spyStream);
        gen.writeStartObject();
        gen.writeEndObject();
        gen.close();

        assertTrue("Stream should be flushed on close", ((SpyOutputStream) spyStream).flushed);
        assertTrue("Stream should be closed on close", ((SpyOutputStream) spyStream).closed);
    }

    // Helper inner class to track flush/close
    private static class SpyOutputStream extends OutputStream {
        boolean flushed = false;
        boolean closed = false;

        @Override
        public void write(int b) throws IOException {
            // no-op
        }

        @Override
        public void flush() throws IOException {
            flushed = true;
        }

        @Override
        public void close() throws IOException {
            closed = true;
        }
    }

    @Test
    public void testWriteEmptyString() throws IOException {
        generator.writeStartObject();
        generator.writeStringField("empty", "");
        generator.writeEndObject();
        generator.close();

        String json = outputStream.toString("UTF-8");
        assertTrue(json.contains("\"empty\":\"\""));
    }

    @Test
    public void testWriteMultipleFields() throws IOException {
        generator.writeStartObject();
        generator.writeNumberField("int", 42);
        generator.writeBooleanField("bool", true);
        generator.writeNullField("nullField");
        generator.writeEndObject();
        generator.close();

        String json = outputStream.toString("UTF-8");
        assertTrue(json.contains("\"int\":42"));
        assertTrue(json.contains("\"bool\":true"));
        assertTrue(json.contains("\"nullField\":null"));
    }

    @Test
    public void testWriteNestedObject() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("outer");
        generator.writeStartObject();
        generator.writeStringField("inner", "value");
        generator.writeEndObject();
        generator.writeEndObject();
        generator.close();

        String json = outputStream.toString("UTF-8");
        assertTrue(json.contains("\"outer\""));
        assertTrue(json.contains("\"inner\""));
    }

    @Test(expected = IOException.class)
    public void testWriteFieldNameWithoutObject() throws IOException {
        generator.writeFieldName("noObject");
        generator.close();
    }

    @Test
    public void testWriteRawValue() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("raw");
        generator.writeRawValue("123");
        generator.writeEndObject();
        generator.close();

        String json = outputStream.toString("UTF-8");
        assertTrue(json.contains("\"raw\":123"));
    }

    @Test
    public void testWriteBinary() throws IOException {
        byte[] data = {1, 2, 3};
        generator.writeStartObject();
        generator.writeBinaryField("bin", data);
        generator.writeEndObject();
        generator.close();

        String json = outputStream.toString("UTF-8");
        assertTrue(json.contains("\"bin\""));
        // Base64 encoded representation should be present
        assertTrue(json.matches(".*\"bin\":\"[A-Za-z0-9+/=]+\".*"));
    }

    @Test
    public void testWriteNumberOverflow() throws IOException {
        generator.writeStartObject();
        generator.writeNumberField("big", Long.MAX_VALUE);
        generator.writeEndObject();
        generator.close();

        String json = outputStream.toString("UTF-8");
        assertTrue(json.contains(String.valueOf(Long.MAX_VALUE)));
    }

    @Test
    public void testWriteEscapedCharacters() throws IOException {
        generator.writeStartObject();
        generator.writeStringField("escape", "tab\tnewline\nquote\"backslash\\");
        generator.writeEndObject();
        generator.close();

        String json = outputStream.toString("UTF-8");
        assertTrue(json.contains("\\t"));
        assertTrue(json.contains("\\n"));
        assertTrue(json.contains("\\\""));
        assertTrue(json.contains("\\\\"));
    }
}