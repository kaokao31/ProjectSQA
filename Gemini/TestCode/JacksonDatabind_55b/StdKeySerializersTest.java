package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdKeySerializers;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class StdKeySerializersTest {

    @Test
    public void testGetStdKeySerializerWithNullType() {
        com.fasterxml.jackson.databind.JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(null);
        Assert.assertNotNull(ser);
    }

    @Test
    public void testGetStdKeySerializerWithString() {
        com.fasterxml.jackson.databind.JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(String.class);
        Assert.assertNotNull(ser);
    }

    @Test
    public void testGetStdKeySerializerWithBoolean() {
        com.fasterxml.jackson.databind.JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(Boolean.class);
        Assert.assertNotNull(ser);

        com.fasterxml.jackson.databind.JsonSerializer<Object> serPrim = StdKeySerializers.getStdKeySerializer(boolean.class);
        Assert.assertNotNull(serPrim);
    }

    @Test
    public void testGetStdKeySerializerWithDate() {
        com.fasterxml.jackson.databind.JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(Date.class);
        Assert.assertNotNull(ser);
    }

    @Test
    public void testGetStdKeySerializerWithCalendar() {
        com.fasterxml.jackson.databind.JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(Calendar.class);
        Assert.assertNotNull(ser);
    }

    @Test
    public void testGetStdKeySerializerWithClass() {
        com.fasterxml.jackson.databind.JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(Class.class);
        Assert.assertNotNull(ser);
    }

    @Test
    public void testGetStdKeySerializerWithUUID() {
        com.fasterxml.jackson.databind.JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(UUID.class);
        Assert.assertNotNull(ser);
    }

    @Test
    public void testGetStdKeySerializerWithEnum() {
        com.fasterxml.jackson.databind.JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(MockEnum.class);
        Assert.assertNotNull(ser);
    }

    @Test
    public void testGetStdKeySerializerWithGenericObject() {
        com.fasterxml.jackson.databind.JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(Object.class);
        Assert.assertNotNull(ser);
    }

    @Test
    public void testDefaultKeySerializer() throws IOException {
        StdKeySerializers.Default serializer = new StdKeySerializers.Default(0, Object.class);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        
        // Just verify it doesn't throw unexpected exceptions on valid invocations or can be instantiated
        Assert.assertNotNull(serializer);
    }

    @Test
    public void testDynamic() throws IOException {
        StdKeySerializers.Dynamic serializer = new StdKeySerializers.Dynamic();
        Assert.assertNotNull(serializer);
    }

    @Test
    public void testEnumSerializer() throws IOException {
        StdKeySerializers.EnumKeySerializer serializer = new StdKeySerializers.EnumKeySerializer(null);
        Assert.assertNotNull(serializer);
    }

    @Test
    public void testStringKeySerializer() throws IOException {
        StdKeySerializers.StringKeySerializer serializer = new StdKeySerializers.StringKeySerializer();
        Assert.assertNotNull(serializer);
    }

    private enum MockEnum {
        A, B
    }
}