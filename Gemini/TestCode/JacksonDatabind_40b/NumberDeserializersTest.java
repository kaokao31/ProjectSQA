package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class NumberDeserializersTest {

    @Test
    public void testPrimitiveIntegerDeserializationNullHandling() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        NumberDeserializers.IntegerDeserializer deser = NumberDeserializers.IntegerDeserializer.primitiveInstance;
        
        // Use an anonymous or mock JsonParser/DeserializationContext or trigger via ObjectMapper with null values
        // Jackson's default behavior for primitive types when encountering JSON null is to throw an exception
        // or return primitive default if configured. Let's test standard parse behavior using ObjectMapper.
        
        String json = "null";
        try {
            mapper.readValue(json, int.class);
            // If it doesn't throw, depending on config, but primitive usually throws MismatchedInputException / InvalidNullException
        } catch (JsonProcessingException e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testWrapperIntegerDeserializationNullHandling() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "null";
        Integer val = mapper.readValue(json, Integer.class);
        Assert.assertNull(val);
    }

    @Test
    public void testAllStandardDeserializersInstantiationAndFind() {
        // Exercise the find() method or inner classes of NumberDeserializers
        JsonDeserializer<?> d1 = NumberDeserializers.find(Integer.class, "java.lang.Integer");
        Assert.assertNotNull(d1);

        JsonDeserializer<?> d2 = NumberDeserializers.find(Integer.TYPE, "int");
        Assert.assertNotNull(d2);

        JsonDeserializer<?> d3 = NumberDeserializers.find(Long.class, "java.lang.Long");
        Assert.assertNotNull(d3);

        JsonDeserializer<?> d4 = NumberDeserializers.find(Long.TYPE, "long");
        Assert.assertNotNull(d4);

        JsonDeserializer<?> d5 = NumberDeserializers.find(Double.class, "java.lang.Double");
        Assert.assertNotNull(d5);

        JsonDeserializer<?> d6 = NumberDeserializers.find(Double.TYPE, "double");
        Assert.assertNotNull(d6);

        JsonDeserializer<?> d7 = NumberDeserializers.find(Boolean.class, "java.lang.Boolean");
        Assert.assertNotNull(d7);

        JsonDeserializer<?> d8 = NumberDeserializers.find(Boolean.TYPE, "boolean");
        Assert.assertNotNull(d8);

        JsonDeserializer<?> d9 = NumberDeserializers.find(Float.class, "java.lang.Float");
        Assert.assertNotNull(d9);

        JsonDeserializer<?> d10 = NumberDeserializers.find(Float.TYPE, "float");
        Assert.assertNotNull(d10);

        JsonDeserializer<?> d11 = NumberDeserializers.find(Short.class, "java.lang.Short");
        Assert.assertNotNull(d11);

        JsonDeserializer<?> d12 = NumberDeserializers.find(Short.TYPE, "short");
        Assert.assertNotNull(d12);

        JsonDeserializer<?> d13 = NumberDeserializers.find(Byte.class, "java.lang.Byte");
        Assert.assertNotNull(d13);

        JsonDeserializer<?> d14 = NumberDeserializers.find(Byte.TYPE, "byte");
        Assert.assertNotNull(d14);

        JsonDeserializer<?> d15 = NumberDeserializers.find(Number.class, "java.lang.Number");
        Assert.assertNotNull(d15);

        // Unknown class should return null
        JsonDeserializer<?> dUnknown = NumberDeserializers.find(String.class, "java.lang.String");
        Assert.assertNull(dUnknown);
    }

    @Test
    public void testBigDecimalAndBigIntegerDeserializers() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        
        java.math.BigDecimal bd = mapper.readValue("123.456", java.math.BigDecimal.class);
        Assert.assertEquals(new java.math.BigDecimal("123.456"), bd);

        java.math.BigInteger bi = mapper.readValue("12345678901234567890", java.math.BigInteger.class);
        Assert.assertEquals(new java.math.BigInteger("12345678901234567890"), bi);
    }

    @Test
    public void testPrimitiveIntegerNullAsZeroConfig() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        // Test primitive wrapper/deserializer behavior specifically targeting Bug 40 (often related to primitive types getNullValue returning null instead of 0)
        NumberDeserializers.IntegerDeserializer deser = NumberDeserializers.IntegerDeserializer.primitiveInstance;
        Assert.assertNotNull(deser);
        
        // In Jackson Databind 40, primitive deserializers should return logical empty/zero equivalents for null if configured, 
        // or getNullValue(DeserializationContext) needs to return Integer(0) for primitive types to avoid NPEs.
        // Let's call getNullValue directly via reflection or standard API if available.
        Object nullVal = deser.getNullValue(null);
        // Depending on exact version/fix for Bug 40:
        // For primitive type deserializers, getNullValue should return 0 (or equivalent boxed primitive default).
        Assert.assertEquals(0, nullVal);
    }

    @Test
    public void testPrimitiveLongNullAsZeroConfig() {
        NumberDeserializers.LongDeserializer deser = NumberDeserializers.LongDeserializer.primitiveInstance;
        Assert.assertNotNull(deser);
        Object nullVal = deser.getNullValue(null);
        Assert.assertEquals(0L, nullVal);
    }

    @Test
    public void testPrimitiveDoubleNullAsZeroConfig() {
        NumberDeserializers.DoubleDeserializer deser = NumberDeserializers.DoubleDeserializer.primitiveInstance;
        Assert.assertNotNull(deser);
        Object nullVal = deser.getNullValue(null);
        Assert.assertEquals(0.0, nullVal);
    }

    @Test
    public void testPrimitiveBooleanNullAsZeroConfig() {
        NumberDeserializers.BooleanDeserializer deser = NumberDeserializers.BooleanDeserializer.primitiveInstance;
        Assert.assertNotNull(deser);
        Object nullVal = deser.getNullValue(null);
        Assert.assertEquals(Boolean.FALSE, nullVal);
    }

    @Test
    public void testPrimitiveFloatNullAsZeroConfig() {
        NumberDeserializers.FloatDeserializer deser = NumberDeserializers.FloatDeserializer.primitiveInstance;
        Assert.assertNotNull(deser);
        Object nullVal = deser.getNullValue(null);
        Assert.assertEquals(0.0f, nullVal);
    }

    @Test
    public void testPrimitiveShortNullAsZeroConfig() {
        NumberDeserializers.ShortDeserializer deser = NumberDeserializers.ShortDeserializer.primitiveInstance;
        Assert.assertNotNull(deser);
        Object nullVal = deser.getNullValue(null);
        Assert.assertEquals((short) 0, nullVal);
    }

    @Test
    public void testPrimitiveByteNullAsZeroConfig() {
        NumberDeserializers.ByteDeserializer deser = NumberDeserializers.ByteDeserializer.primitiveInstance;
        Assert.assertNotNull(deser);
        Object nullVal = deser.getNullValue(null);
        Assert.assertEquals((byte) 0, nullVal);
    }
}