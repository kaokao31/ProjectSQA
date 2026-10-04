package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class DateTimeSerializerBaseTest {

    private static class ConcreteDateTimeSerializer extends DateTimeSerializerBase<Date> {
        private final boolean _serializeAsTimestamp;
        private final DateFormat _customFormat;

        protected ConcreteDateTimeSerializer(boolean serializeAsTimestamp, DateFormat customFormat) {
            super(Date.class, serializeAsTimestamp, customFormat);
            _serializeAsTimestamp = serializeAsTimestamp;
            _customFormat = customFormat;
        }

        @Override
        public DateTimeSerializerBase<Date> withFormat(boolean timestamp, DateFormat customFormat) {
            return new ConcreteDateTimeSerializer(timestamp, customFormat);
        }

        @Override
        public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (_serializeAsTimestamp) {
                gen.writeNumber(_timestamp(value));
            } else if (_customFormat != null) {
                synchronized (_customFormat) {
                    gen.writeString(_customFormat.format(value));
                }
            } else {
                serializers.defaultSerializeDateValue(value, gen);
            }
        }
    }

    private ObjectMapperWrapper objectMapper;
    private SerializerProvider serializerProvider;

    // Helper wrapper to avoid full databind dependency complexities for simple providers
    private static class ObjectMapperWrapper {
        private final JsonMapper mapper = new JsonMapper();
        public SerializerProvider getSerializerProvider() {
            return mapper.getSerializerProviderInstance();
        }
    }

    @Before
    public void setUp() {
        objectMapper = new ObjectMapperWrapper();
        serializerProvider = objectMapper.getSerializerProvider();
    }

    @Test
    public void testConstructorAndGetSchema() {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer(true, null);
        assertNotNull(serializer);
        // getSchema returns JsonNode, often string/number schema based on timestamp
        assertNotNull(serializer.getSchema(serializerProvider, null));

        ConcreteDateTimeSerializer serializerStr = new ConcreteDateTimeSerializer(false, null);
        assertNotNull(serializerStr.getSchema(serializerProvider, null));
    }

    @Test
    public void testAcceptJsonFormatVisitor() throws JsonMappingException {
        ConcreteDateTimeSerializer serializerTimestamp = new ConcreteDateTimeSerializer(true, null);
        // Should not throw
        serializerTimestamp.acceptJsonFormatVisitor(null, null);

        ConcreteDateTimeSerializer serializerString = new ConcreteDateTimeSerializer(false, null);
        serializerString.acceptJsonFormatVisitor(null, null);
    }

    @Test
    public void testIsEmpty() {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer(true, null);
        assertTrue(serializer.isEmpty(serializerProvider, null));
        assertTrue(serializer.isEmpty(serializerProvider, new Date(0L))); // Depending on implementation, but typically checks null or specific values. Wait, base class checks `value == null`.
        assertFalse(serializer.isEmpty(serializerProvider, new Date()));
    }

    @Test
    public void testContextualNullProperty() throws JsonMappingException {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer(false, null);
        JsonSerializer<?> contextual = serializer.createContextual(serializerProvider, null);
        assertSame(serializer, contextual);
    }

    @Test
    public void testContextualWithFormatShapeAny() throws JsonMappingException {
        final AtomicBoolean annotatedCalled = new AtomicBoolean(false);
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer(false, null) {
            @Override
            public DateTimeSerializerBase<Date> withFormat(boolean timestamp, DateFormat customFormat) {
                annotatedCalled.set(true);
                return super.withFormat(timestamp, customFormat);
            }
        };

        BeanProperty.Std property = new BeanProperty.Std(
                null, null, null, null, null, null, null
        );

        // Without format annotation
        JsonSerializer<?> contextual = serializer.createContextual(serializerProvider, property);
        assertSame(serializer, contextual);
    }

    @Test
    public void testContextualWithTimestampShape() throws JsonMappingException {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer(false, null);

        // We can mock or use a real property with JsonFormat annotation
        // Since constructing a real annotated property can be verbose, we can test via a custom BeanProperty with annotation or subclassing findFormatOverrides
        ConcreteDateTimeSerializer serializerWithFormat = new ConcreteDateTimeSerializer(false, null) {
            @Override
            protected JsonFormat.Value findFormatOverrides(SerializerProvider provider, BeanProperty type, Class<?> handledType) {
                return new JsonFormat.Value().withShape(JsonFormat.Shape.NUMBER);
            }
        };

        BeanProperty.Std property = new BeanProperty.Std(
                null, null, null, null, null, null, null
        );

        JsonSerializer<?> contextual = serializerWithFormat.createContextual(serializerProvider, property);
        assertNotNull(contextual);
        assertNotSame(serializerWithFormat, contextual);
    }

    @Test
    public void testContextualWithStringShapeAndPattern() throws JsonMappingException {
        ConcreteDateTimeSerializer serializerWithFormat = new ConcreteDateTimeSerializer(true, null) {
            @Override
            protected JsonFormat.Value findFormatOverrides(SerializerProvider provider, BeanProperty type, Class<?> handledType) {
                return new JsonFormat.Value()
                        .withShape(JsonFormat.Shape.STRING)
                        .withPattern("yyyy-MM-dd")
                        .withLocale(Locale.US)
                        .withTimeZone(TimeZone.getTimeZone("GMT"));
            }
        };

        BeanProperty.Std property = new BeanProperty.Std(
                null, null, null, null, null, null, null
        );

        JsonSerializer<?> contextual = serializerWithFormat.createContextual(serializerProvider, property);
        assertNotNull(contextual);
    }

    @Test
    public void testContextualWithLenientSetting() throws JsonMappingException {
        ConcreteDateTimeSerializer serializerWithFormat = new ConcreteDateTimeSerializer(true, null) {
            @Override
            protected JsonFormat.Value findFormatOverrides(SerializerProvider provider, BeanProperty type, Class<?> handledType) {
                return new JsonFormat.Value()
                        .withShape(JsonFormat.Shape.STRING)
                        .withPattern("yyyy-MM-dd")
                        .withLenient(true);
            }
        };

        BeanProperty.Std property = new BeanProperty.Std(
                null, null, null, null, null, null, null
        );

        JsonSerializer<?> contextual = serializerWithFormat.createContextual(serializerProvider, property);
        assertNotNull(contextual);
    }

    @Test
    public void testContextualWithStdDateFormat() throws JsonMappingException {
        ConcreteDateTimeSerializer serializerWithFormat = new ConcreteDateTimeSerializer(false, null) {
            @Override
            protected JsonFormat.Value findFormatOverrides(SerializerProvider provider, BeanProperty type, Class<?> handledType) {
                return new JsonFormat.Value()
                        .withShape(JsonFormat.Shape.STRING)
                        // no pattern, but timezone/locale to trigger StdDateFormat branching
                        .withLocale(Locale.GERMANY)
                        .withTimeZone(TimeZone.getTimeZone("UTC"));
            }
        };

        BeanProperty.Std property = new BeanProperty.Std(
                null, null, null, null, null, null, null
        );

        JsonSerializer<?> contextual = serializerWithFormat.createContextual(serializerProvider, property);
        assertNotNull(contextual);
    }

    @Test
    public void testContextualWithCustomDateFormatButNoPattern() throws JsonMappingException {
        ConcreteDateTimeSerializer serializerWithFormat = new ConcreteDateTimeSerializer(false, null) {
            @Override
            protected JsonFormat.Value findFormatOverrides(SerializerProvider provider, BeanProperty type, Class<?> handledType) {
                // Return a format where pattern is empty/null, but has timezone
                return new JsonFormat.Value()
                        .withTimeZone(TimeZone.getTimeZone("PST"));
            }
        };

        // Also mock serializerProvider to have a DateFormat config
        SerializerProvider sp = objectMapper.getSerializerProvider();
        
        BeanProperty.Std property = new BeanProperty.Std(
                null, null, null, null, null, null, null
        );

        JsonSerializer<?> contextual = serializerWithFormat.createContextual(sp, property);
        assertNotNull(contextual);
    }
}