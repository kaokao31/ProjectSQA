package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class DateTimeSerializerBaseTest {

    private ObjectMapperStub objectMapper;
    private SerializerProvider serializerProvider;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapperStub();
        serializerProvider = objectMapper.getSerializerProvider();
    }

    @Test
    public void testCreateContextualWithNullProperty() throws Exception {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer(Date.class);
        JsonSerializer<?> contextual = serializer.createContextual(serializerProvider, null);
        assertSame(serializer, contextual);
    }

    @Test
    public void testCreateContextualWithoutAnnotation() throws Exception {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer(Date.class);
        BeanProperty.Std property = new BeanProperty.Std(
                null, null, null, null, null, null
        );
        JsonSerializer<?> contextual = serializer.createContextual(serializerProvider, property);
        assertSame(serializer, contextual);
    }

    @Test
    public void testCreateContextualWithShapeAny() throws Exception {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer(Date.class);
        JsonFormat.Value format = new JsonFormat.Value().withShape(JsonFormat.Shape.ANY);
        BeanProperty.Std property = createPropertyWithFormat(format);

        JsonSerializer<?> contextual = serializer.createContextual(serializerProvider, property);
        assertSame(serializer, contextual);
    }

    @Test
    public void testCreateContextualWithShapeStringAndPattern() throws Exception {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer(Date.class);
        String pattern = "yyyy-MM-dd";
        JsonFormat.Value format = new JsonFormat.Value()
                .withShape(JsonFormat.Shape.STRING)
                .withPattern(pattern);
        BeanProperty.Std property = createPropertyWithFormat(format);

        JsonSerializer<?> contextual = serializer.createContextual(serializerProvider, property);
        assertNotSame(serializer, contextual);
        assertTrue(contextual instanceof DateTimeSerializerBase);
    }

    @Test
    public void testCreateContextualWithShapeStringAndLocaleLanguage() throws Exception {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer(Date.class);
        JsonFormat.Value format = new JsonFormat.Value()
                .withShape(JsonFormat.Shape.STRING)
                .withLocale(java.util.Locale.GERMAN);
        BeanProperty.Std property = createPropertyWithFormat(format);

        JsonSerializer<?> contextual = serializer.createContextual(serializerProvider, property);
        assertNotSame(serializer, contextual);
    }

    @Test
    public void testCreateContextualWithShapeStringAndEmptyPatternUsesDefault() throws Exception {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer(Date.class);
        JsonFormat.Value format = new JsonFormat.Value()
                .withShape(JsonFormat.Shape.STRING)
                .withPattern("");
        BeanProperty.Std property = createPropertyWithFormat(format);

        JsonSerializer<?> contextual = serializer.createContextual(serializerProvider, property);
        assertNotSame(serializer, contextual);
    }

    @Test
    public void testCreateContextualWithNumericShape() throws Exception {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer(Date.class);
        JsonFormat.Value format = new JsonFormat.Value()
                .withShape(JsonFormat.Shape.NUMBER);
        BeanProperty.Std property = createPropertyWithFormat(format);

        JsonSerializer<?> contextual = serializer.createContextual(serializerProvider, property);
        assertNotSame(serializer, contextual);
    }

    @Test
    public void testCreateContextualWithNumericIntShape() throws Exception {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer(Date.class);
        JsonFormat.Value format = new JsonFormat.Value()
                .withShape(JsonFormat.Shape.NUMBER_INT);
        BeanProperty.Std property = createPropertyWithFormat(format);

        JsonSerializer<?> contextual = serializer.createContextual(serializerProvider, property);
        assertNotSame(serializer, contextual);
    }

    @Test
    public void testCreateContextualWithZone() throws Exception {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer(Date.class);
        JsonFormat.Value format = new JsonFormat.Value()
                .withShape(JsonFormat.Shape.STRING)
                .withTimezone(TimeZone.getTimeZone("GMT+2"));
        BeanProperty.Std property = createPropertyWithFormat(format);

        JsonSerializer<?> contextual = serializer.createContextual(serializerProvider, property);
        assertNotSame(serializer, contextual);
    }

    @Test
    public void testGetSchema() throws Exception {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer(Date.class);
        com.fasterxml.jackson.databind.JsonNode schema = serializer.getSchema(serializerProvider, null);
        assertNotNull(schema);
    }

    @Test
    public void testAcceptJsonFormatVisitor() throws Exception {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer(Date.class);
        AtomicBoolean visited = new AtomicBoolean(false);
        
        com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper visitor = 
            new com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper() {
                @Override
                public com.fasterxml.jackson.databind.jsonFormatVisitors.JsonStringFormatVisitor expectStringFormat(JavaType type) throws JsonMappingException {
                    visited.set(true);
                    return null;
                }
                @Override
                public com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) throws JsonMappingException {
                    visited.set(true);
                    return null;
                }
                @Override
                public com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormatVisitor expectValueFormat(JavaType type) throws JsonMappingException {
                    visited.set(true);
                    return null;
                }
                @Override
                public SerializerProvider getProvider() {
                    return serializerProvider;
                }
                @Override
                public void setProvider(SerializerProvider p) {}
            };

        serializer.acceptJsonFormatVisitor(visitor, null);
        assertTrue(visited.get());
    }

    @Test
    public void testAcceptJsonFormatVisitorWithNumericShape() throws Exception {
        JsonFormat.Value format = new JsonFormat.Value().withShape(JsonFormat.Shape.NUMBER);
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer(Date.class, format, null);
        AtomicBoolean visited = new AtomicBoolean(false);

        com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper visitor = 
            new com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper() {
                @Override
                public com.fasterxml.jackson.databind.jsonFormatVisitors.JsonStringFormatVisitor expectStringFormat(JavaType type) throws JsonMappingException {
                    return null;
                }
                @Override
                public com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) throws JsonMappingException {
                    visited.set(true);
                    return null;
                }
                @Override
                public com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormatVisitor expectValueFormat(JavaType type) throws JsonMappingException {
                    return null;
                }
                @Override
                public SerializerProvider getProvider() {
                    return serializerProvider;
                }
                @Override
                public void setProvider(SerializerProvider p) {}
            };

        serializer.acceptJsonFormatVisitor(visitor, null);
        assertTrue(visited.get());
    }

    private BeanProperty.Std createPropertyWithFormat(JsonFormat.Value format) {
        return new BeanProperty.Std(
                null, null, null, null, null,
                null
        ) {
            @Override
            public JsonFormat.Value findPropertyFormat(MapperConfig<?> config, Class<?> baseType) {
                return format;
            }
            @Override
            public JsonFormat.Value findFormatOverrides(AnnotationIntrospector intr) {
                return format;
            }
        };
    }

    private static class ConcreteDateTimeSerializer extends DateTimeSerializerBase<Date> {
        public ConcreteDateTimeSerializer(Class<Date> type) {
            super(type, null, null);
        }

        public ConcreteDateTimeSerializer(Class<Date> type, Boolean useTimestamp, DateFormat customFormat) {
            super(type, useTimestamp, customFormat);
        }

        @Override
        public DateTimeSerializerBase<Date> withFormat(Boolean useTimestamp, DateFormat customFormat) {
            return new ConcreteDateTimeSerializer(_handledType, useTimestamp, customFormat);
        }

        @Override
        public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.toString());
        }
    }

    private static class ObjectMapperStub {
        private final JsonMapper mapper = new JsonMapper();
        public SerializerProvider getSerializerProvider() {
            return mapper.getSerializerProvider();
        }
    }

    private interface MapperConfig<T extends MapperConfig<T>> {
    }

    private interface AnnotationIntrospector {
    }
}