package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateTimeSerializerBaseTest {

    // Concrete implementation of abstract DateTimeSerializerBase for testing
    private static class ConcreteDateTimeSerializer extends DateTimeSerializerBase<Object> {
        public ConcreteDateTimeSerializer() {
            super(Object.class, false, null);
        }

        public ConcreteDateTimeSerializer(boolean usTimestamp, Boolean leniency, DateFormat customFormat) {
            super(Object.class, usTimestamp, leniency, customFormat);
        }

        @Override
        public DateTimeSerializerBase<Object> withFormat(boolean usTimestamp, DateFormat customFormat) {
            return new ConcreteDateTimeSerializer(usTimestamp, _lenient, customFormat);
        }

        @Override
        public void serialize(Object value, com.fasterxml.jackson.core.JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value != null ? value.toString() : "null");
        }
    }

    @Test
    public void testAcceptJsonFormatVisitor() throws JsonMappingException {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer();
        ObjectMapperMock mapper = new ObjectMapperMock();
        SerializerProvider provider = mapper.getSerializerProvider();
        JavaType type = mapper.constructType(Date.class);

        // Test with timestamp true
        ConcreteDateTimeSerializer timestampSerializer = new ConcreteDateTimeSerializer(true, null, null);
        // Should not throw exception
        timestampSerializer.acceptJsonFormatVisitor(null, type);

        // Test with timestamp false and custom format
        ConcreteDateTimeSerializer formatSerializer = new ConcreteDateTimeSerializer(false, null, new StdDateFormat());
        formatSerializer.acceptJsonFormatVisitor(null, type);
    }

    @Test
    public void testGetSchema() {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer();
        ObjectMapperMock mapper = new ObjectMapperMock();
        SerializerProvider provider = mapper.getSerializerProvider();
        
        // Test with timestamp true
        ConcreteDateTimeSerializer timestampSerializer = new ConcreteDateTimeSerializer(true, null, null);
        com.fasterxml.jackson.databind.JsonNode schemaTimestamp = timestampSerializer.getSchema(provider, Object.class);
        Assert.assertNotNull(schemaTimestamp);

        // Test with timestamp false
        ConcreteDateTimeSerializer formatSerializer = new ConcreteDateTimeSerializer(false, null, null);
        com.fasterxml.jackson.databind.JsonNode schemaFormat = formatSerializer.getSchema(provider, Object.class);
        Assert.assertNotNull(schemaFormat);
    }

    @Test
    public void testCreateContextualWithNullProperty() throws JsonMappingException {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer();
        JsonMapper mapper = new JsonMapper();
        SerializerProvider provider = mapper.getSerializerProvider();

        JsonSerializer<?> contextual = serializer.createContextual(provider, null);
        Assert.assertSame(serializer, contextual);
    }

    @Test
    public void testCreateContextualWithShapeAny() throws JsonMappingException {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer();
        JsonMapper mapper = new JsonMapper();
        SerializerProvider provider = mapper.getSerializerProvider();

        BeanProperty.Std property = new BeanProperty.Std(
                com.fasterxml.jackson.databind.util.NameTransformer.NOP.transform("test"),
                mapper.constructType(Date.class),
                null,
                null,
                com.fasterxml.jackson.databind.introspect.AnnotatedMember.EMPTY_COLLECTION,
                new JsonFormat.Value().withShape(JsonFormat.Shape.ANY)
        );

        JsonSerializer<?> contextual = serializer.createContextual(provider, property);
        Assert.assertSame(serializer, contextual);
    }

    @Test
    public void testCreateContextualWithNumericTimestamp() throws JsonMappingException {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer();
        JsonMapper mapper = new JsonMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        SerializerProvider provider = mapper.getSerializerProvider();

        // Shape.STRING -> useTimestamp = false
        BeanProperty.Std propertyString = new BeanProperty.Std(
                com.fasterxml.jackson.databind.util.NameTransformer.NOP.transform("test"),
                mapper.constructType(Date.class),
                null,
                null,
                com.fasterxml.jackson.databind.introspect.AnnotatedMember.EMPTY_COLLECTION,
                new JsonFormat.Value().withShape(JsonFormat.Shape.STRING)
        );
        JsonSerializer<?> contextualString = serializer.createContextual(provider, propertyString);
        Assert.assertNotNull(contextualString);

        // Shape.NUMBER -> useTimestamp = true
        BeanProperty.Std propertyNumber = new BeanProperty.Std(
                com.fasterxml.jackson.databind.util.NameTransformer.NOP.transform("test"),
                mapper.constructType(Date.class),
                null,
                null,
                com.fasterxml.jackson.databind.introspect.AnnotatedMember.EMPTY_COLLECTION,
                new JsonFormat.Value().withShape(JsonFormat.Shape.NUMBER)
        );
        JsonSerializer<?> contextualNumber = serializer.createContextual(provider, propertyNumber);
        Assert.assertNotNull(contextualNumber);
    }

    @Test
    public void testCreateContextualWithCustomPattern() throws JsonMappingException {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer();
        JsonMapper mapper = new JsonMapper();
        SerializerProvider provider = mapper.getSerializerProvider();

        BeanProperty.Std property = new BeanProperty.Std(
                com.fasterxml.jackson.databind.util.NameTransformer.NOP.transform("test"),
                mapper.constructType(Date.class),
                null,
                null,
                com.fasterxml.jackson.databind.introspect.AnnotatedMember.EMPTY_COLLECTION,
                new JsonFormat.Value().withPattern("yyyy-MM-dd").withLocale(java.util.Locale.US).withTimeZone(TimeZone.getDefault())
        );

        JsonSerializer<?> contextual = serializer.createContextual(provider, property);
        Assert.assertNotNull(contextual);
    }

    @Test
    public void testCreateContextualWithTimeZoneOnly() throws JsonMappingException {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer();
        JsonMapper mapper = new JsonMapper();
        SerializerProvider provider = mapper.getSerializerProvider();

        BeanProperty.Std property = new BeanProperty.Std(
                com.fasterxml.jackson.databind.util.NameTransformer.NOP.transform("test"),
                mapper.constructType(Date.class),
                null,
                null,
                com.fasterxml.jackson.databind.introspect.AnnotatedMember.EMPTY_COLLECTION,
                new JsonFormat.Value().withTimeZone(TimeZone.getTimeZone("GMT"))
        );

        JsonSerializer<?> contextual = serializer.createContextual(provider, property);
        Assert.assertNotNull(contextual);
    }

    @Test
    public void testCreateContextualWithEmptyPattern() throws JsonMappingException {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer();
        JsonMapper mapper = new JsonMapper();
        SerializerProvider provider = mapper.getSerializerProvider();

        BeanProperty.Std property = new BeanProperty.Std(
                com.fasterxml.jackson.databind.util.NameTransformer.NOP.transform("test"),
                mapper.constructType(Date.class),
                null,
                null,
                com.fasterxml.jackson.databind.introspect.AnnotatedMember.EMPTY_COLLECTION,
                new JsonFormat.Value().withPattern("")
        );

        JsonSerializer<?> contextual = serializer.createContextual(provider, property);
        Assert.assertNotNull(contextual);
    }

    @Test
    public void testCreateContextualWithLenient() throws JsonMappingException {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer();
        JsonMapper mapper = new JsonMapper();
        SerializerProvider provider = mapper.getSerializerProvider();

        BeanProperty.Std property = new BeanProperty.Std(
                com.fasterxml.jackson.databind.util.NameTransformer.NOP.transform("test"),
                mapper.constructType(Date.class),
                null,
                null,
                com.fasterxml.jackson.databind.introspect.AnnotatedMember.EMPTY_COLLECTION,
                new JsonFormat.Value().withLenient(true)
        );

        JsonSerializer<?> contextual = serializer.createContextual(provider, property);
        Assert.assertNotNull(contextual);
    }

    @Test
    public void test_useTimestamp_logic() {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer();
        JsonMapper mapper = new JsonMapper();
        SerializerProvider provider = mapper.getSerializerProvider();
        
        // This exercises the helper method _useTimestamp through various config states
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        Assert.assertTrue(serializer._useTimestamp(provider));

        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        Assert.assertFalse(serializer._useTimestamp(provider));
    }

    // Helper mock classes if needed
    private static class ObjectMapperMock extends JsonMapper {
        // Can be extended if necessary
    }
}