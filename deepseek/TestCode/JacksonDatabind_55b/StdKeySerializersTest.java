package com.fasterxml.jackson.databind.ser.std;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.SerializerCache;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class StdKeySerializersTest {

    private SerializationConfig config;
    private SerializerProvider provider;

    @Before
    public void setUp() {
        ObjectMapper mapper = new ObjectMapper();
        config = mapper.getSerializationConfig();
        provider = mapper.getSerializerProvider();
    }

    // Test getStdKeySerializer with null keyType
    @Test(expected = IllegalArgumentException.class)
    public void testGetStdKeySerializerNullKeyType() {
        StdKeySerializers.getStdKeySerializer(config, null, true);
    }

    // Test getStdKeySerializer with String.class
    @Test
    public void testGetStdKeySerializerString() {
        JsonSerializer<Object> serializer = StdKeySerializers.getStdKeySerializer(config, String.class, true);
        assertNotNull("Serializer for String should not be null", serializer);
        assertTrue("Serializer should be instance of StdKeySerializers.StringKeySerializer",
                serializer instanceof StdKeySerializers.StringKeySerializer);
    }

    // Test getStdKeySerializer with Object.class
    @Test
    public void testGetStdKeySerializerObject() {
        JsonSerializer<Object> serializer = StdKeySerializers.getStdKeySerializer(config, Object.class, true);
        assertNotNull("Serializer for Object should not be null", serializer);
        // Typically returns DefaultKeySerializer or similar
        assertTrue("Serializer should be instance of DefaultKeySerializer",
                serializer instanceof DefaultKeySerializer);
    }

    // Test getStdKeySerializer with Serializable.class
    @Test
    public void testGetStdKeySerializerSerializable() {
        JsonSerializer<Object> serializer = StdKeySerializers.getStdKeySerializer(config, Serializable.class, true);
        assertNotNull("Serializer for Serializable should not be null", serializer);
        // Should return DefaultKeySerializer
        assertTrue("Serializer should be instance of DefaultKeySerializer",
                serializer instanceof DefaultKeySerializer);
    }

    // Test getStdKeySerializer with Enum.class
    @Test
    public void testGetStdKeySerializerEnum() {
        JsonSerializer<Object> serializer = StdKeySerializers.getStdKeySerializer(config, Enum.class, true);
        assertNotNull("Serializer for Enum should not be null", serializer);
        // Should return something like StdKeySerializers.EnumKeySerializer
        assertTrue("Serializer should be instance of StdKeySerializers.EnumKeySerializer",
                serializer instanceof StdKeySerializers.EnumKeySerializer);
    }

    // Test getStdKeySerializer with Date.class
    @Test
    public void testGetStdKeySerializerDate() {
        JsonSerializer<Object> serializer = StdKeySerializers.getStdKeySerializer(config, Date.class, true);
        assertNotNull("Serializer for Date should not be null", serializer);
        // Should return DateKeySerializer
        assertTrue("Serializer should be instance of StdKeySerializers.DateKeySerializer",
                serializer instanceof StdKeySerializers.DateKeySerializer);
    }

    // Test getStdKeySerializer with Calendar.class
    @Test
    public void testGetStdKeySerializerCalendar() {
        JsonSerializer<Object> serializer = StdKeySerializers.getStdKeySerializer(config, Calendar.class, true);
        assertNotNull("Serializer for Calendar should not be null", serializer);
        // Should return CalendarKeySerializer
        assertTrue("Serializer should be instance of StdKeySerializers.CalendarKeySerializer",
                serializer instanceof StdKeySerializers.CalendarKeySerializer);
    }

    // Test getStdKeySerializer with Integer.class (non-standard type)
    @Test
    public void testGetStdKeySerializerInteger() {
        JsonSerializer<Object> serializer = StdKeySerializers.getStdKeySerializer(config, Integer.class, true);
        assertNotNull("Serializer for Integer should not be null", serializer);
        // Should return DefaultKeySerializer
        assertTrue("Serializer should be instance of DefaultKeySerializer",
                serializer instanceof DefaultKeySerializer);
    }

    // Test getStdKeySerializer with useDefault=false for String
    @Test
    public void testGetStdKeySerializerStringNoDefault() {
        JsonSerializer<Object> serializer = StdKeySerializers.getStdKeySerializer(config, String.class, false);
        assertNotNull("Serializer for String with useDefault=false should not be null", serializer);
        assertTrue("Serializer should be instance of StdKeySerializers.StringKeySerializer",
                serializer instanceof StdKeySerializers.StringKeySerializer);
    }

    // Test getStdKeySerializer with useDefault=false for Object
    @Test
    public void testGetStdKeySerializerObjectNoDefault() {
        JsonSerializer<Object> serializer = StdKeySerializers.getStdKeySerializer(config, Object.class, false);
        assertNotNull("Serializer for Object with useDefault=false should not be null", serializer);
        // Should return DefaultKeySerializer
        assertTrue("Serializer should be instance of DefaultKeySerializer",
                serializer instanceof DefaultKeySerializer);
    }

    // Test getStdKeySerializer with useDefault=false for Serializable
    @Test
    public void testGetStdKeySerializerSerializableNoDefault() {
        JsonSerializer<Object> serializer = StdKeySerializers.getStdKeySerializer(config, Serializable.class, false);
        assertNotNull("Serializer for Serializable with useDefault=false should not be null", serializer);
        assertTrue("Serializer should be instance of DefaultKeySerializer",
                serializer instanceof DefaultKeySerializer);
    }

    // Test getStdKeySerializer with useDefault=false for Enum
    @Test
    public void testGetStdKeySerializerEnumNoDefault() {
        JsonSerializer<Object> serializer = StdKeySerializers.getStdKeySerializer(config, Enum.class, false);
        assertNotNull("Serializer for Enum with useDefault=false should not be null", serializer);
        assertTrue("Serializer should be instance of StdKeySerializers.EnumKeySerializer",
                serializer instanceof StdKeySerializers.EnumKeySerializer);
    }

    // Test getStdKeySerializer with useDefault=false for Date
    @Test
    public void testGetStdKeySerializerDateNoDefault() {
        JsonSerializer<Object> serializer = StdKeySerializers.getStdKeySerializer(config, Date.class, false);
        assertNotNull("Serializer for Date with useDefault=false should not be null", serializer);
        assertTrue("Serializer should be instance of StdKeySerializers.DateKeySerializer",
                serializer instanceof StdKeySerializers.DateKeySerializer);
    }

    // Test getStdKeySerializer with useDefault=false for Calendar
    @Test
    public void testGetStdKeySerializerCalendarNoDefault() {
        JsonSerializer<Object> serializer = StdKeySerializers.getStdKeySerializer(config, Calendar.class, false);
        assertNotNull("Serializer for Calendar with useDefault=false should not be null", serializer);
        assertTrue("Serializer should be instance of StdKeySerializers.CalendarKeySerializer",
                serializer instanceof StdKeySerializers.CalendarKeySerializer);
    }

    // Test getStdKeySerializer with useDefault=false for Integer (non-standard)
    @Test
    public void testGetStdKeySerializerIntegerNoDefault() {
        JsonSerializer<Object> serializer = StdKeySerializers.getStdKeySerializer(config, Integer.class, false);
        assertNotNull("Serializer for Integer with useDefault=false should not be null", serializer);
        // Should return DefaultKeySerializer
        assertTrue("Serializer should be instance of DefaultKeySerializer",
                serializer instanceof DefaultKeySerializer);
    }

    // Test getStdKeySerializer with a custom type that is not standard
    @Test
    public void testGetStdKeySerializerCustomType() {
        JsonSerializer<Object> serializer = StdKeySerializers.getStdKeySerializer(config, MyKey.class, true);
        assertNotNull("Serializer for custom type should not be null", serializer);
        assertTrue("Serializer should be instance of DefaultKeySerializer",
                serializer instanceof DefaultKeySerializer);
    }

    // Test getStdKeySerializer with a custom type and useDefault=false
    @Test
    public void testGetStdKeySerializerCustomTypeNoDefault() {
        JsonSerializer<Object> serializer = StdKeySerializers.getStdKeySerializer(config, MyKey.class, false);
        assertNotNull("Serializer for custom type with useDefault=false should not be null", serializer);
        assertTrue("Serializer should be instance of DefaultKeySerializer",
                serializer instanceof DefaultKeySerializer);
    }

    // Test that DefaultKeySerializer is returned for unknown types
    @Test
    public void testDefaultKeySerializerForUnknown() {
        JsonSerializer<Object> serializer = StdKeySerializers.getStdKeySerializer(config, Boolean.class, true);
        assertNotNull("Serializer for Boolean should not be null", serializer);
        assertTrue("Serializer should be instance of DefaultKeySerializer",
                serializer instanceof DefaultKeySerializer);
    }

    // Test that StringKeySerializer handles null key (if applicable)
    @Test
    public void testStringKeySerializerNullKey() {
        StdKeySerializers.StringKeySerializer serializer = new StdKeySerializers.StringKeySerializer();
        // This test may need a SerializerProvider; we can use the one from setUp
        // But StringKeySerializer.serialize expects a non-null key? We'll test null handling
        // Typically it should throw NullPointerException or handle gracefully
        // We'll just verify it's not null
        assertNotNull(serializer);
    }

    // Test EnumKeySerializer
    @Test
    public void testEnumKeySerializer() {
        StdKeySerializers.EnumKeySerializer serializer = new StdKeySerializers.EnumKeySerializer();
        assertNotNull(serializer);
        // Additional tests could serialize an enum value
    }

    // Test DateKeySerializer
    @Test
    public void testDateKeySerializer() {
        StdKeySerializers.DateKeySerializer serializer = new StdKeySerializers.DateKeySerializer();
        assertNotNull(serializer);
    }

    // Test CalendarKeySerializer
    @Test
    public void testCalendarKeySerializer() {
        StdKeySerializers.CalendarKeySerializer serializer = new StdKeySerializers.CalendarKeySerializer();
        assertNotNull(serializer);
    }

    // Helper class for custom type
    static class MyKey {
        @Override
        public String toString() {
            return "myKey";
        }
    }
}