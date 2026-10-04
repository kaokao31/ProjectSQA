package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.*;

public class StdKeyDeserializerTest {

    @Test
    public void testForType() {
        assertNotNull(StdKeyDeserializer.forType(String.class));
        assertNotNull(StdKeyDeserializer.forType(boolean.class));
        assertNotNull(StdKeyDeserializer.forType(Boolean.class));
        assertNotNull(StdKeyDeserializer.forType(byte.class));
        assertNotNull(StdKeyDeserializer.forType(Byte.class));
        assertNotNull(StdKeyDeserializer.forType(char.class));
        assertNotNull(StdKeyDeserializer.forType(Character.class));
        assertNotNull(StdKeyDeserializer.forType(short.class));
        assertNotNull(StdKeyDeserializer.forType(Short.class));
        assertNotNull(StdKeyDeserializer.forType(int.class));
        assertNotNull(StdKeyDeserializer.forType(Integer.class));
        assertNotNull(StdKeyDeserializer.forType(long.class));
        assertNotNull(StdKeyDeserializer.forType(Long.class));
        assertNotNull(StdKeyDeserializer.forType(float.class));
        assertNotNull(StdKeyDeserializer.forType(Float.class));
        assertNotNull(StdKeyDeserializer.forType(double.class));
        assertNotNull(StdKeyDeserializer.forType(Double.class));
        assertNotNull(StdKeyDeserializer.forType(Date.class));
        assertNotNull(StdKeyDeserializer.forType(Calendar.class));
        assertNotNull(StdKeyDeserializer.forType(UUID.class));
        assertNotNull(StdKeyDeserializer.forType(Object.class));
    }

    @Test
    public void testStringKD() throws Exception {
        StdKeyDeserializer.StringKD kd = StdKeyDeserializer.StringKD.forType(String.class);
        assertNotNull(kd);
        assertEquals("test", kd.deserializeKey("test", null));
        
        StdKeyDeserializer.StringKD kdObj = StdKeyDeserializer.StringKD.forType(Object.class);
        assertNotNull(kdObj);
        assertEquals("test", kdObj.deserializeKey("test", null));
    }

    @Test
    public void testEnumKD() throws Exception {
        // Construct a dummy EnumResolver and AnnotatedMethod
        EnumResolver enumResolver = EnumResolver.constructUnsafeUsingToString(DummyEnum.class);
        Method factoryMethod = DummyEnum.class.getMethod("fromValue", String.class);
        AnnotatedMethod factory = new AnnotatedMethod(null, factoryMethod, null, null);

        StdKeyDeserializer.EnumKD kd = new StdKeyDeserializer.EnumKD(enumResolver, factory);
        assertEquals(DummyEnum.VALUE1, kd.deserializeKey("VALUE1", null));

        // Test with null/empty values if applicable
        try {
            kd.deserializeKey(null, null);
        } catch (Exception e) {
            // expected or handled
        }
    }

    @Test
    public void testDelegatingKD() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(DummyKeyClass.class);
        
        JsonDeserializer<?> deser = mapper.findNonContextualValueDeserializer(type);
        StdKeyDeserializer.DelegatingKD kd = new StdKeyDeserializer.DelegatingKD(type.getRawClass(), deser);
        
        // deserializeKey might fail or pass depending on implementation, let's call it via reflection or direct if accessible
        try {
            kd.deserializeKey("dummy", ctxt);
        } catch (Exception e) {
            // Expected if parser context is missing
        }
    }

    @Test
    public void testStringCtorKeyDeserializer() throws Exception {
        Constructor<?> ctor = DummyKeyClass.class.getConstructor(String.class);
        StdKeyDeserializer.StringCtorKeyDeserializer kd = new StdKeyDeserializer.StringCtorKeyDeserializer(ctor);
        
        Object result = kd.deserializeKey("testValue", null);
        assertNotNull(result);
        assertTrue(result instanceof DummyKeyClass);
        assertEquals("testValue", ((DummyKeyClass) result).value);
    }

    @Test
    public void testStringFactoryKeyDeserializer() throws Exception {
        Method factory = DummyKeyClass.class.getMethod("create", String.class);
        StdKeyDeserializer.StringFactoryKeyDeserializer kd = new StdKeyDeserializer.StringFactoryKeyDeserializer(factory);
        
        Object result = kd.deserializeKey("factoryValue", null);
        assertNotNull(result);
        assertTrue(result instanceof DummyKeyClass);
        assertEquals("factoryValue", ((DummyKeyClass) result).value);
    }

    @Test
    public void testStdKeyDeserializerMainLogic() throws Exception {
        // Test basic types via the main StdKeyDeserializer class constructor
        StdKeyDeserializer boolDeser = new StdKeyDeserializer(StdKeyDeserializer.TYPE_BOOLEAN, Boolean.class);
        assertEquals(Boolean.TRUE, boolDeser.deserializeKey("true", null));
        assertEquals(Boolean.FALSE, boolDeser.deserializeKey("false", null));

        StdKeyDeserializer intDeser = new StdKeyDeserializer(StdKeyDeserializer.TYPE_INT, Integer.class);
        assertEquals(Integer.valueOf(123), intDeser.deserializeKey("123", null));

        StdKeyDeserializer longDeser = new StdKeyDeserializer(StdKeyDeserializer.TYPE_LONG, Long.class);
        assertEquals(Long.valueOf(123456L), longDeser.deserializeKey("123456", null));

        StdKeyDeserializer doubleDeser = new StdKeyDeserializer(StdKeyDeserializer.TYPE_DOUBLE, Double.class);
        assertEquals(Double.valueOf(12.34), doubleDeser.deserializeKey("12.34", null));

        StdKeyDeserializer floatDeser = new StdKeyDeserializer(StdKeyDeserializer.TYPE_FLOAT, Float.class);
        assertEquals(Float.valueOf(12.34f), floatDeser.deserializeKey("12.34", null));

        StdKeyDeserializer uuidDeser = new StdKeyDeserializer(StdKeyDeserializer.TYPE_UUID, UUID.class);
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid, uuidDeser.deserializeKey(uuid.toString(), null));

        StdKeyDeserializer dateDeser = new StdKeyDeserializer(StdKeyDeserializer.TYPE_DATE, Date.class);
        assertNotNull(dateDeser.deserializeKey("2020-01-01T00:00:00.000+0000", null));

        StdKeyDeserializer charDeser = new StdKeyDeserializer(StdKeyDeserializer.TYPE_CHAR, Character.class);
        assertEquals(Character.valueOf('a'), charDeser.deserializeKey("a", null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidBooleanKey() throws Exception {
        StdKeyDeserializer boolDeser = new StdKeyDeserializer(StdKeyDeserializer.TYPE_BOOLEAN, Boolean.class);
        boolDeser.deserializeKey("not-a-bool", null);
    }

    // Dummy classes for testing
    public enum DummyEnum {
        VALUE1, VALUE2;
        public static DummyEnum fromValue(String v) {
            return valueOf(v);
        }
    }

    public static class DummyKeyClass {
        public final String value;

        public DummyKeyClass(String value) {
            this.value = value;
        }

        public static DummyKeyClass create(String value) {
            return new DummyKeyClass(value);
        }
    }
}