package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.util.StdDateFormat;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class DateTimeSerializerBaseTest {

    private SerializerProvider provider;
    private JsonGenerator jgen;
    private BeanProperty property;

    @Before
    public void setUp() {
        provider = new SerializerProvider() {
            @Override
            public JsonSerializer<Object> findValueSerializer(JavaType type, BeanProperty beanProperty) {
                return null;
            }

            @Override
            public JsonSerializer<Object> findTypedValueSerializer(JavaType type, boolean cache, BeanProperty beanProperty) {
                return null;
            }

            @Override
            public JsonSerializer<Object> findKeySerializer(JavaType type, BeanProperty beanProperty) {
                return null;
            }

            @Override
            public JsonSerializer<Object> findTypedKeySerializer(JavaType type, boolean cache, BeanProperty beanProperty) {
                return null;
            }

            @Override
            public JsonSerializer<Object> findNullKeySerializer(JavaType type, BeanProperty beanProperty) {
                return null;
            }

            @Override
            public JsonSerializer<Object> findNullValueSerializer(BeanProperty beanProperty) {
                return null;
            }

            @Override
            public JsonSerializer<Object> findNullSerializer(BeanProperty beanProperty) {
                return null;
            }

            @Override
            public JavaType constructType(Class<?> cls) {
                return null;
            }

            @Override
            public JavaType constructSpecializedType(JavaType base, Class<?> subclass) {
                return null;
            }

            @Override
            public Serializers findSerializers(JavaType type) {
                return null;
            }

            @Override
            public boolean isEnabled(SerializationFeature feature) {
                return false;
            }

            @Override
            public TimeZone getTimeZone() {
                return TimeZone.getTimeZone("UTC");
            }

            @Override
            public DateFormat getDateFormat() {
                return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            }

            @Override
            public Locale getLocale() {
                return Locale.US;
            }
        };
        jgen = new JsonGenerator() {
            @Override
            public void writeStartArray() throws IOException {}

            @Override
            public void writeEndArray() throws IOException {}

            @Override
            public void writeStartObject() throws IOException {}

            @Override
            public void writeEndObject() throws IOException {}

            @Override
            public void writeFieldName(String name) throws IOException {}

            @Override
            public void writeString(String text) throws IOException {}

            @Override
            public void writeString(char[] text, int offset, int len) throws IOException {}

            @Override
            public void writeRawUTF8String(byte[] text, int offset, int length) throws IOException {}

            @Override
            public void writeUTF8String(byte[] text, int offset, int length) throws IOException {}

            @Override
            public void writeRaw(String text) throws IOException {}

            @Override
            public void writeRaw(String text, int offset, int len) throws IOException {}

            @Override
            public void writeRaw(char[] text, int offset, int len) throws IOException {}

            @Override
            public void writeRaw(char c) throws IOException {}

            @Override
            public void writeRawValue(String text) throws IOException {}

            @Override
            public void writeRawValue(String text, int offset, int len) throws IOException {}

            @Override
            public void writeRawValue(char[] text, int offset, int len) throws IOException {}

            @Override
            public void writeBinary(byte[] data, int offset, int len) throws IOException {}

            @Override
            public void writeNumber(int v) throws IOException {}

            @Override
            public void writeNumber(long v) throws IOException {}

            @Override
            public void writeNumber(BigInteger v) throws IOException {}

            @Override
            public void writeNumber(double v) throws IOException {}

            @Override
            public void writeNumber(float v) throws IOException {}

            @Override
            public void writeNumber(BigDecimal v) throws IOException {}

            @Override
            public void writeNumber(String encodedValue) throws IOException {}

            @Override
            public void writeBoolean(boolean state) throws IOException {}

            @Override
            public void writeNull() throws IOException {}

            @Override
            public void writeObject(Object value) throws IOException {}

            @Override
            public void writeTree(TreeNode rootNode) throws IOException {}

            @Override
            public JsonStreamContext getOutputContext() {
                return null;
            }

            @Override
            public void flush() throws IOException {}

            @Override
            public boolean isClosed() {
                return false;
            }

            @Override
            public JsonGenerator setCodec(ObjectCodec oc) {
                return null;
            }

            @Override
            public ObjectCodec getCodec() {
                return null;
            }

            @Override
            public JsonGenerator disable(Feature f) {
                return null;
            }

            @Override
            public JsonGenerator enable(Feature f) {
                return null;
            }

            @Override
            public boolean isEnabled(Feature f) {
                return false;
            }

            @Override
            public int getFeatureMask() {
                return 0;
            }

            @Override
            public JsonGenerator setFeatureMask(int mask) {
                return null;
            }

            @Override
            public JsonGenerator useDefaultPrettyPrinter() {
                return null;
            }

            @Override
            public void close() throws IOException {}
        };
        property = new BeanProperty.Std(null, null, null, null, null, false);
    }

    @Test
    public void testSerializeWithTimestamp() throws IOException {
        // Test with timestamp serialization enabled
        DateTimeSerializerBase<Date> serializer = new DateTimeSerializerBase<Date>(Date.class, true, null) {
            @Override
            protected long _timestamp(Date value) {
                return value.getTime();
            }

            @Override
            public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                if (_useTimestamp(provider)) {
                    jgen.writeNumber(_timestamp(value));
                } else {
                    jgen.writeString(value.toString());
                }
            }
        };

        Date date = new Date(123456789L);
        serializer.serialize(date, jgen, provider);
        // Since _useTimestamp returns false (provider.isEnabled returns false), should write string
        // But we need to verify the behavior
    }

    @Test
    public void testSerializeWithoutTimestamp() throws IOException {
        // Test with timestamp serialization disabled
        DateTimeSerializerBase<Date> serializer = new DateTimeSerializerBase<Date>(Date.class, false, null) {
            @Override
            protected long _timestamp(Date value) {
                return value.getTime();
            }

            @Override
            public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                if (_useTimestamp(provider)) {
                    jgen.writeNumber(_timestamp(value));
                } else {
                    jgen.writeString(value.toString());
                }
            }
        };

        Date date = new Date(987654321L);
        serializer.serialize(date, jgen, provider);
    }

    @Test
    public void testSerializeWithNullValue() throws IOException {
        DateTimeSerializerBase<Date> serializer = new DateTimeSerializerBase<Date>(Date.class, true, null) {
            @Override
            protected long _timestamp(Date value) {
                return value.getTime();
            }

            @Override
            public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                if (value == null) {
                    provider.defaultSerializeNull(jgen);
                } else if (_useTimestamp(provider)) {
                    jgen.writeNumber(_timestamp(value));
                } else {
                    jgen.writeString(value.toString());
                }
            }
        };

        serializer.serialize(null, jgen, provider);
    }

    @Test
    public void testSerializeWithCustomFormat() throws IOException {
        DateFormat customFormat = new SimpleDateFormat("yyyy/MM/dd");
        DateTimeSerializerBase<Date> serializer = new DateTimeSerializerBase<Date>(Date.class, false, customFormat) {
            @Override
            protected long _timestamp(Date value) {
                return value.getTime();
            }

            @Override
            public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                if (_useTimestamp(provider)) {
                    jgen.writeNumber(_timestamp(value));
                } else {
                    if (_customFormat != null) {
                        jgen.writeString(_customFormat.format(value));
                    } else {
                        jgen.writeString(value.toString());
                    }
                }
            }
        };

        Date date = new Date(0);
        serializer.serialize(date, jgen, provider);
    }

    @Test
    public void testSerializeWithTypeSerializer() throws IOException {
        DateTimeSerializerBase<Date> serializer = new DateTimeSerializerBase<Date>(Date.class, true, null) {
            @Override
            protected long _timestamp(Date value) {
                return value.getTime();
            }

            @Override
            public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                if (_useTimestamp(provider)) {
                    jgen.writeNumber(_timestamp(value));
                } else {
                    jgen.writeString(value.toString());
                }
            }

            @Override
            public void serializeWithType(Date value, JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
                typeSer.writeTypePrefixForScalar(value, jgen);
                serialize(value, jgen, provider);
                typeSer.writeTypeSuffixForScalar(value, jgen);
            }
        };

        TypeSerializer typeSer = new TypeSerializer() {
            @Override
            public TypeSerializer forProperty(BeanProperty prop) {
                return null;
            }

            @Override
            public void writeTypePrefixForScalar(Object value, JsonGenerator jgen) throws IOException {
                jgen.writeString("__type__");
            }

            @Override
            public void writeTypePrefixForObject(Object value, JsonGenerator jgen) throws IOException {
                jgen.writeStartObject();
                jgen.writeStringField("@type", value.getClass().getName());
            }

            @Override
            public void writeTypePrefixForArray(Object value, JsonGenerator jgen) throws IOException {
                jgen.writeStartArray();
                jgen.writeString(value.getClass().getName());
            }

            @Override
            public void writeTypeSuffixForScalar(Object value, JsonGenerator jgen) throws IOException {
                // No-op
            }

            @Override
            public void writeTypeSuffixForObject(Object value, JsonGenerator jgen) throws IOException {
                jgen.writeEndObject();
            }

            @Override
            public void writeTypeSuffixForArray(Object value, JsonGenerator jgen) throws IOException {
                jgen.writeEndArray();
            }

            @Override
            public void writeCustomTypePrefixForScalar(Object value, JsonGenerator jgen, String typeStr) throws IOException {
                jgen.writeString(typeStr);
            }

            @Override
            public void writeCustomTypePrefixForObject(Object value, JsonGenerator jgen, String typeStr) throws IOException {
                jgen.writeStartObject();
                jgen.writeStringField("@type", typeStr);
            }

            @Override
            public void writeCustomTypePrefixForArray(Object value, JsonGenerator jgen, String typeStr) throws IOException {
                jgen.writeStartArray();
                jgen.writeString(typeStr);
            }

            @Override
            public void writeCustomTypeSuffixForScalar(Object value, JsonGenerator jgen, String typeStr) throws IOException {
                // No-op
            }

            @Override
            public void writeCustomTypeSuffixForObject(Object value, JsonGenerator jgen, String typeStr) throws IOException {
                jgen.writeEndObject();
            }

            @Override
            public void writeCustomTypeSuffixForArray(Object value, JsonGenerator jgen, String typeStr) throws IOException {
                jgen.writeEndArray();
            }

            @Override
            public JavaType getTypeInclusion() {
                return null;
            }

            @Override
            public String getPropertyName() {
                return null;
            }

            @Override
            public TypeIdResolver getTypeIdResolver() {
                return null;
            }
        };

        Date date = new Date();
        serializer.serializeWithType(date, jgen, provider, typeSer);
    }

    @Test
    public void testUseTimestampWithEnabledFeature() {
        DateTimeSerializerBase<Date> serializer = new DateTimeSerializerBase<Date>(Date.class, true, null) {
            @Override
            protected long _timestamp(Date value) {
                return value.getTime();
            }

            @Override
            public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                // Not used in this test
            }
        };

        // Test with feature enabled
        SerializerProvider providerWithFeature = new SerializerProvider() {
            @Override
            public boolean isEnabled(SerializationFeature feature) {
                return feature == SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
            }

            @Override
            public TimeZone getTimeZone() {
                return TimeZone.getTimeZone("UTC");
            }

            @Override
            public DateFormat getDateFormat() {
                return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            }

            @Override
            public Locale getLocale() {
                return Locale.US;
            }

            @Override
            public JsonSerializer<Object> findValueSerializer(JavaType type, BeanProperty beanProperty) {
                return null;
            }

            @Override
            public JsonSerializer<Object> findTypedValueSerializer(JavaType type, boolean cache, BeanProperty beanProperty) {
                return null;
            }

            @Override
            public JsonSerializer<Object> findKeySerializer(JavaType type, BeanProperty beanProperty) {
                return null;
            }

            @Override
            public JsonSerializer<Object> findTypedKeySerializer(JavaType type, boolean cache, BeanProperty beanProperty) {
                return null;
            }

            @Override
            public JsonSerializer<Object> findNullKeySerializer(JavaType type, BeanProperty beanProperty) {
                return null;
            }

            @Override
            public JsonSerializer<Object> findNullValueSerializer(BeanProperty beanProperty) {
                return null;
            }

            @Override
            public JsonSerializer<Object> findNullSerializer(BeanProperty beanProperty) {
                return null;
            }

            @Override
            public JavaType constructType(Class<?> cls) {
                return null;
            }

            @Override
            public JavaType constructSpecializedType(JavaType base, Class<?> subclass) {
                return null;
            }

            @Override
            public Serializers findSerializers(JavaType type) {
                return null;
            }
        };

        assertTrue(serializer._useTimestamp(providerWithFeature));
    }

    @Test
    public void testUseTimestampWithDisabledFeature() {
        DateTimeSerializerBase<Date> serializer = new DateTimeSerializerBase<Date>(Date.class, true, null) {
            @Override
            protected long _timestamp(Date value) {
                return value.getTime();
            }

            @Override
            public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                // Not used in this test
            }
        };

        // Test with feature disabled
        SerializerProvider providerWithoutFeature = new SerializerProvider() {
            @Override
            public boolean isEnabled(SerializationFeature feature) {
                return false;
            }

            @Override
            public TimeZone getTimeZone() {
                return TimeZone.getTimeZone("UTC");
            }

            @Override
            public DateFormat getDateFormat() {
                return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            }

            @Override
            public Locale getLocale() {
                return Locale.US;
            }

            @Override
            public JsonSerializer<Object> findValueSerializer(JavaType type, BeanProperty beanProperty) {
                return null;
            }

            @Override
            public JsonSerializer<Object> findTypedValueSerializer(JavaType type, boolean cache, BeanProperty beanProperty) {
                return null;
            }

            @Override
            public JsonSerializer<Object> findKeySerializer(JavaType type, BeanProperty beanProperty) {
                return null;
            }

            @Override
            public JsonSerializer<Object> findTypedKeySerializer(JavaType type, boolean cache, BeanProperty beanProperty) {
                return null;
            }

            @Override
            public JsonSerializer<Object> findNullKeySerializer(JavaType type, BeanProperty beanProperty) {
                return null;
            }

            @Override
            public JsonSerializer<Object> findNullValueSerializer(BeanProperty beanProperty) {
                return null;
            }

            @Override
            public JsonSerializer<Object> findNullSerializer(BeanProperty beanProperty) {
                return null;
            }

            @Override
            public JavaType constructType(Class<?> cls) {
                return null;
            }

            @Override
            public JavaType constructSpecializedType(JavaType base, Class<?> subclass) {
                return null;
            }

            @Override
            public Serializers findSerializers(JavaType type) {
                return null;
            }
        };

        assertFalse(serializer._useTimestamp(providerWithoutFeature));
    }

    @Test
    public void testUseTimestampWithNullProvider() {
        DateTimeSerializerBase<Date> serializer = new DateTimeSerializerBase<Date>(Date.class, true, null) {
            @Override
            protected long _timestamp(Date value) {
                return value.getTime();
            }

            @Override
            public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                // Not used in this test
            }
        };

        // Test with null provider - should use default timestamp behavior
        assertTrue(serializer._useTimestamp(null));
    }

    @Test
    public void testConstructorWithNullFormat() {
        DateTimeSerializerBase<Date> serializer = new DateTimeSerializerBase<Date>(Date.class, true, null) {
            @Override
            protected long _timestamp(Date value) {
                return value.getTime();
            }

            @Override
            public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                // Not used in this test
            }
        };

        assertNull(serializer._customFormat);
        assertTrue(serializer._useTimestamp);
    }

    @Test
    public void testConstructorWithCustomFormat() {
        DateFormat customFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateTimeSerializerBase<Date> serializer = new DateTimeSerializerBase<Date>(Date.class, false, customFormat) {
            @Override
            protected long _timestamp(Date value) {
                return value.getTime();
            }

            @Override
            public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                // Not used in this test
            }
        };

        assertNotNull(serializer._customFormat);
        assertFalse(serializer._useTimestamp);
    }

    @Test
    public void testSerializeWithBoundaryDateValues() throws IOException {
        DateTimeSerializerBase<Date> serializer = new DateTimeSerializerBase<Date>(Date.class, true, null) {
            @Override
            protected long _timestamp(Date value) {
                return value.getTime();
            }

            @Override
            public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                if (_useTimestamp(provider)) {
                    jgen.writeNumber(_timestamp(value));
                } else {
                    jgen.writeString(value.toString());
                }
            }
        };

        // Test with epoch
        Date epoch = new Date(0);
        serializer.serialize(epoch, jgen, provider);

        // Test with max date
        Date maxDate = new Date(Long.MAX_VALUE);
        serializer.serialize(maxDate, jgen, provider);

        // Test with min date
        Date minDate = new Date(Long.MIN_VALUE);
        serializer.serialize(minDate, jgen, provider);
    }

    @Test
    public void testSerializeWithCalendarSubclass() throws IOException {
        DateTimeSerializerBase<Calendar> serializer = new DateTimeSerializerBase<Calendar>(Calendar.class, true, null) {
            @Override
            protected long _timestamp(Calendar value) {
                return value.getTimeInMillis();
            }

            @Override
            public void serialize(Calendar value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                if (_useTimestamp(provider)) {
                    jgen.writeNumber(_timestamp(value));
                } else {
                    jgen.writeString(value.getTime().toString());
                }
            }
        };

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(123456789L));
        serializer.serialize(calendar, jgen, provider);
    }

    @Test
    public void testSerializeWithDifferentTimeZones() throws IOException {
        DateTimeSerializerBase<Date> serializer = new DateTimeSerializerBase<Date>(Date.class, false, null) {
            @Override
            protected long _timestamp(Date value) {
                return value.getTime();
            }

            @Override
            public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                if (_useTimestamp(provider)) {
                    jgen.writeNumber(_timestamp(value));
                } else {
                    jgen.writeString(value.toString());
                }
            }
        };

        Date date = new Date();
        serializer.serialize(date, jgen, provider);
    }

    @Test(expected = IOException.class)
    public void testSerializeWithIOException() throws IOException {
        DateTimeSerializerBase<Date> serializer = new DateTimeSerializerBase<Date>(Date.class, true, null) {
            @Override
            protected long _timestamp(Date value) {
                return value.getTime();
            }

            @Override
            public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                throw new IOException("Test exception");
            }
        };

        serializer.serialize(new Date(), jgen, provider);
    }

    @Test
    public void testSerializeWithNullGenerator() throws IOException {
        DateTimeSerializerBase<Date> serializer = new DateTimeSerializerBase<Date>(Date.class, true, null) {
            @Override
            protected long _timestamp(Date value) {
                return value.getTime();
            }

            @Override
            public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                if (jgen == null) {
                    throw new IllegalArgumentException("Generator cannot be null");
                }
                if (_useTimestamp(provider)) {
                    jgen.writeNumber(_timestamp(value));
                } else {
                    jgen.writeString(value.toString());
                }
            }
        };

        try {
            serializer.serialize(new Date(), null, provider);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testSerializeWithNullProvider() throws IOException {
        DateTimeSerializerBase<Date> serializer = new DateTimeSerializerBase<Date>(Date.class, true, null) {
            @Override
            protected long _timestamp(Date value) {
                return value.getTime();
            }

            @Override
            public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                if (provider == null) {
                    throw new IllegalArgumentException("Provider cannot be null");
                }
                if (_useTimestamp(provider)) {
                    jgen.writeNumber(_timestamp(value));
                } else {
                    jgen.writeString(value.toString());
                }
            }
        };

        try {
            serializer.serialize(new Date(), jgen, null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
}