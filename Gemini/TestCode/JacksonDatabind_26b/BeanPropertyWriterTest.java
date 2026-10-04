package com.fasterxml.jackson.databind.ser;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.impl.UnwrappingBeanPropertyWriter;
import com.fasterxml.jackson.databind.util.NameTransformer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class BeanPropertyWriterTest {

    private ObjectMapper objectMapper;
    private SerializerProvider serializerProvider;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        serializerProvider = objectMapper.getSerializerProvider();
    }

    @After
    public void tearDown() {
        objectMapper = null;
        serializerProvider = null;
    }

    static class SampleBean {
        public String name = "testName";
        public int value = 42;
        public String nullField = null;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public String getNullField() {
            return nullField;
        }

        public String getName() {
            return name;
        }

        public int getValue() {
            return value;
        }
    }

    @Test
    public void testBeanPropertyWriterConstructionAndGetters() throws Exception {
        JavaType type = objectMapper.constructType(SampleBean.class);
        BeanDescription beanDesc = objectMapper.getSerializationConfig().introspect(type);
        BeanPropertyWriter bpw = new BeanPropertyWriter();
        
        assertNotNull(bpw.getName());
        assertFalse(bpw.hasNullSerializer());
        assertFalse(bpw.hasSerializer());
    }

    @Test
    public void testRename() throws Exception {
        JavaType type = objectMapper.constructType(SampleBean.class);
        BeanDescription beanDesc = objectMapper.getSerializationConfig().introspect(type);
        AnnotatedMethod am = beanDesc.findMethod("getName", null);
        
        BeanPropertyWriter bpw = new BeanPropertyWriter(
                beanDesc.createProperties().get(0),
                am,
                beanDesc.getClassAnnotations(),
                type,
                null,
                null,
                type,
                false,
                null
        );

        BeanPropertyWriter renamed = bpw.rename(NameTransformer.simplePrefix("prefix_"));
        assertNotNull(renamed);
        assertEquals("prefix_name", renamed.getName());
    }

    @Test
    public void testAssignSerializer() throws Exception {
        JavaType type = objectMapper.constructType(SampleBean.class);
        BeanDescription beanDesc = objectMapper.getSerializationConfig().introspect(type);
        BeanPropertyWriter bpw = new BeanPropertyWriter();
        
        JsonSerializer<Object> ser = objectMapper.findValueSerializer(String.class);
        bpw.assignSerializer(ser);
        assertTrue(bpw.hasSerializer());
        assertSame(ser, bpw.getSerializer());
    }

    @Test
    public void testAssignNullSerializer() throws Exception {
        BeanPropertyWriter bpw = new BeanPropertyWriter();
        JsonSerializer<Object> nullSer = objectMapper.findValueSerializer(String.class);
        bpw.assignNullSerializer(nullSer);
        assertTrue(bpw.hasNullSerializer());
    }

    @Test
    public void testDepositSchemaProperty() throws Exception {
        BeanPropertyWriter bpw = new BeanPropertyWriter();
        JsonObjectFormatVisitor v = new JsonObjectFormatVisitor() {
            @Override
            public void property(BeanProperty writer) {}
            @Override
            public void property(String name, JsonFormatVisitable handler, JavaType propertyType) {}
            @Override
            public void optionalProperty(BeanProperty writer) {}
            @Override
            public void optionalProperty(String name, JsonFormatVisitable handler, JavaType propertyType) {}
            @Override
            public SerializerProvider getProvider() { return serializerProvider; }
            @Override
            public void setProvider(SerializerProvider p) {}
        };
        // Should not throw
        bpw.depositSchemaProperty(v);
    }

    @Test
    public void testUnwrappingBeanPropertyWriter() throws Exception {
        BeanPropertyWriter bpw = new BeanPropertyWriter();
        UnwrappingBeanPropertyWriter upw = new UnwrappingBeanPropertyWriter(bpw, NameTransformer.NOP);
        assertNotNull(upw);
        BeanPropertyWriter renamed = upw.rename(NameTransformer.NOP);
        assertNotNull(renamed);
    }

    @Test
    public void testGettersAndBasicState() {
        BeanPropertyWriter bpw = new BeanPropertyWriter();
        assertNull(bpw.getAnnotation(JsonInclude.class));
        assertNull(bpw.getContextAnnotation(JsonInclude.class));
        assertNull(bpw.getMember());
        assertNull(bpw.getType());
        assertFalse(bpw.isRequired());
        assertFalse(bpw.isVirtual());
    }
}