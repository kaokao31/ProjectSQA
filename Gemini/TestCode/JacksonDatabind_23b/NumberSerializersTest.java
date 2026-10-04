package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class NumberSerializersTest {

    @Test
    public void testNumberSerializersInstantiation() throws Exception {
        Constructor<NumberSerializers> ctor = NumberSerializers.class.getDeclaredConstructor();
        Assert.assertTrue(Modifier.isPrivate(ctor.getModifiers()));
        ctor.setAccessible(true);
        NumberSerializers ns = ctor.newInstance();
        Assert.assertNotNull(ns);
    }

    @Test
    public void testIntegerSerializer() throws Exception {
        ObjectMapper mapper = new JsonMapper();
        NumberSerializers.IntegerSerializer serializer = new NumberSerializers.IntegerSerializer(Integer.class);
        
        // Test basic serialization
        String json = mapper.writeValueAsString(123);
        Assert.assertEquals("123", json);

        // Test acceptJsonFormatVisitor
        // Just verify it doesn't throw exception
        serializer.acceptJsonFormatVisitor(null, null);
    }

    @Test
    public void testLongSerializer() throws Exception {
        ObjectMapper mapper = new JsonMapper();
        NumberSerializers.LongSerializer serializer = new NumberSerializers.LongSerializer(Long.class);

        String json = mapper.writeValueAsString(456L);
        Assert.assertEquals("456", json);

        serializer.acceptJsonFormatVisitor(null, null);
    }

    @Test
    public void testIntLikeSerializer() throws Exception {
        ObjectMapper mapper = new JsonMapper();
        NumberSerializers.IntLikeSerializer serializer = NumberSerializers.IntLikeSerializer.instance;

        String json = mapper.writeValueAsString(789);
        Assert.assertEquals("789", json);

        serializer.acceptJsonFormatVisitor(null, null);
    }

    @Test
    public void testFloatSerializer() throws Exception {
        ObjectMapper mapper = new JsonMapper();
        NumberSerializers.FloatSerializer serializer = NumberSerializers.FloatSerializer.instance;

        String json = mapper.writeValueAsString(1.23f);
        Assert.assertEquals("1.23", json);

        serializer.acceptJsonFormatVisitor(null, null);
    }

    @Test
    public void testDoubleSerializer() throws Exception {
        ObjectMapper mapper = new JsonMapper();
        NumberSerializers.DoubleSerializer serializer = new NumberSerializers.DoubleSerializer(Double.class);

        String json = mapper.writeValueAsString(4.56d);
        Assert.assertEquals("4.56", json);

        serializer.acceptJsonFormatVisitor(null, null);
    }

    @Test
    public void testShortSerializer() throws Exception {
        ObjectMapper mapper = new JsonMapper();
        NumberSerializers.ShortSerializer serializer = NumberSerializers.ShortSerializer.instance;

        String json = mapper.writeValueAsString((short) 12);
        Assert.assertEquals("12", json);

        serializer.acceptJsonFormatVisitor(null, null);
    }

    @Test
    public void testBeanPropertyRegistration() throws Exception {
        ObjectMapper mapper = new JsonMapper();
        
        // Use a test bean to trigger property-based serialization for various number types
        BeanWithNumbers bean = new BeanWithNumbers();
        bean.i = 1;
        bean.l = 2L;
        bean.f = 3.0f;
        bean.d = 4.0;
        bean.s = (short) 5;
        bean.intLike = 6;

        String json = mapper.writeValueAsString(bean);
        Assert.assertTrue(json.contains("\"i\":1"));
        Assert.assertTrue(json.contains("\"l\":2"));
        Assert.assertTrue(json.contains("\"f\":3.0"));
        Assert.assertTrue(json.contains("\"d\":4.0"));
        Assert.assertTrue(json.contains("\"s\":5"));
        Assert.assertTrue(json.contains("\"intLike\":6"));
    }

    static class BeanWithNumbers {
        public int i;
        public long l;
        public float f;
        public double d;
        public short s;
        public Integer intLike;
    }
}