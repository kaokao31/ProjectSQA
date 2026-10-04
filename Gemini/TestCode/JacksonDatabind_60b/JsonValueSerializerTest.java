package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class JsonValueSerializerTest {

    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
    }

    @After
    public void tearDown() {
        objectMapper = null;
    }

    static class SampleValue {
        private final String name;

        public SampleValue(String name) {
            this.name = name;
        }

        @JsonValue
        public String getName() {
            return name;
        }
    }

    static class NullValue {
        @JsonValue
        public String getName() {
            return null;
        }
    }

    static class UnsupportedValue {
        @JsonValue
        public Object getValue() {
            return new Object();
        }
    }

    @Test
    public void testBasicSerialization() throws Exception {
        SampleValue value = new SampleValue("Jackson");
        String json = objectMapper.writeValueAsString(value);
        assertEquals("\"Jackson\"", json);
    }

    @Test
    public void testNullValueSerialization() throws Exception {
        NullValue value = new NullValue();
        String json = objectMapper.writeValueAsString(value);
        assertEquals("null", json);
    }

    @Test
    public void testSerializerConstructionAndAccessors() throws Exception {
        Method m = SampleValue.class.getMethod("getName");
        AnnotatedMethod am = new ObjectMapper().getSerializationConfig()
                .introspect(JavaType.fromClass(SampleValue.class))
                .findMethod("getName", new Class[0]);

        JsonValueSerializer ser = new JsonValueSerializer(am, null);
        assertNotNull(ser);

        // Test with type serializer
        JsonValueSerializer serWithType = new JsonValueSerializer(ser, null, null, true);
        assertNotNull(serWithType);

        // Test withSuppressNulls
        JsonValueSerializer serSuppress = ser.withSuppressNulls(true);
        assertNotNull(serSuppress);

        JsonValueSerializer serNoSuppress = ser.withSuppressNulls(false);
        assertNotNull(serNoSuppress);
    }

    @Test
    public void testIsEmpty() throws Exception {
        Method m = SampleValue.class.getMethod("getName");
        AnnotatedMethod am = new ObjectMapper().getSerializationConfig()
                .introspect(JavaType.fromClass(SampleValue.class))
                .findMethod("getName", new Class[0]);

        JsonValueSerializer ser = new JsonValueSerializer(am, null);
        SerializerProvider provider = objectMapper.getSerializerProvider();

        assertFalse(ser.isEmpty(provider, new SampleValue("test")));
        assertTrue(ser.isEmpty(provider, null));
    }

    @Test(expected = JsonMappingException.class)
    public void testSerializationFailureDueToUnsupportedType() throws Exception {
        UnsupportedValue value = new UnsupportedValue();
        objectMapper.writeValueAsString(value);
    }
}