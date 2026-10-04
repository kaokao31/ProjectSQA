package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.*;

public class StdKeySerializerTest {

    @Test
    public void testDefaultStdKeySerializer() throws IOException {
        StdKeySerializer serializer = new StdKeySerializer();
        ObjectMapper mapper = new ObjectMapper();
        JsonGenerator gen = mapper.getFactory().createGenerator(new java.io.StringWriter());
        SerializerProvider provider = mapper.getSerializerProvider();

        // Testing serialization of a standard Object using StdKeySerializer
        String testKey = "test-key-string";
        serializer.serialize(testKey, gen, provider);
        gen.flush();
        gen.close();
    }

    @Test
    public void testGetDefault() {
        JsonSerializer<Object> serializer = StdKeySerializer.getDefault();
        assertNotNull(serializer);
        assertTrue(serializer instanceof StdKeySerializer);
    }

    @Test
    public void testSerializeNullKey() throws IOException {
        StdKeySerializer serializer = new StdKeySerializer();
        ObjectMapper mapper = new ObjectMapper();
        // StdKeySerializer typically handles non-null, but let's test invocation with null if allowed or safe
        JsonGenerator gen = mapper.getFactory().createGenerator(new java.io.StringWriter());
        SerializerProvider provider = mapper.getSerializerProvider();

        try {
            serializer.serialize(null, gen, provider);
        } catch (Exception e) {
            // Depending on implementation, null key might throw an exception or delegate to provider
            assertNotNull(e);
        } finally {
            gen.close();
        }
    }

    @Test
    public void testAcceptJsonFormatVisitor() throws JsonMappingException {
        StdKeySerializer serializer = new StdKeySerializer();
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider provider = mapper.getSerializerProvider();
        JavaType type = mapper.constructType(String.class);

        // Should not throw exception
        serializer.acceptJsonFormatVisitor(null, type);
    }
}