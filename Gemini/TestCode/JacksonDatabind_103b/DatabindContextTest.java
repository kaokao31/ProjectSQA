package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class DatabindContextTest {

    private DatabindContext context;

    @Before
    public void setUp() {
        context = new ConcreteDatabindContext();
    }

    @Test
    public void testFormatMethodsDefault() {
        assertNull(context.getDefaultPropertyFormat(null));
        assertNull(context.getDefaultLocale());
        assertNull(context.getDefaultTimeZone());
    }

    @Test
    public void testAnnotationIntrospector() {
        assertNull(context.getAnnotationIntrospector());
    }

    @Test
    public void testTypeFactory() {
        assertNull(context.getTypeFactory());
    }

    @Test
    public void testIsEnabled() {
        assertFalse(context.isEnabled(MapperFeature.AUTO_DETECT_CREATORS));
    }

    @Test
    public void testGetConfig() {
        assertNull(context.getConfig());
    }

    @Test
    public void testConstructType() {
        Type type = String.class;
        assertNull(context.constructType(type));
    }

    @Test
    public void testConstructSpecializedType() {
        assertNull(context.constructSpecializedType(null, String.class));
    }

    @Test
    public void testObjectIdGenerator() {
        assertNull(context.objectIdGeneratorInstance(null, null));
    }

    @Test
    public void testObjectIdResolver() {
        assertNull(context.objectIdResolverInstance(null, null));
    }

    @Test
    public void testConverterInstance() {
        assertNull(context.converterInstance(null, null));
    }

    @Test
    public void testIncludeFilterInstance() {
        assertNull(context.includeFilterInstance(null, null));
    }

    @Test
    public void testIncludeFilterSuppressNulls() {
        assertFalse(context.includeFilterSuppressNulls(null));
    }

    @Test
    public void testReportBadDefinition() throws JsonMappingException {
        try {
            context.reportBadDefinition(String.class, "Bad definition");
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertEquals("Bad definition", e.getMessage());
        }
    }

    @Test
    public void testReportBadDefinitionJavaType() throws JsonMappingException {
        try {
            TypeFactory tf = TypeFactory.defaultInstance();
            JavaType type = tf.constructType(String.class);
            context.reportBadDefinition(type, "Bad type definition");
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertEquals("Bad type definition", e.getMessage());
        }
    }

    @Test
    public void testInstantiationException() throws JsonMappingException {
        try {
            context.instantiationException(String.class, new RuntimeException("Cause"));
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("String"));
        }
    }

    @Test
    public void testInvalidTypeIdException() throws JsonMappingException {
        try {
            TypeFactory tf = TypeFactory.defaultInstance();
            JavaType type = tf.constructType(String.class);
            context.invalidTypeIdException(type, "type-id", "Extra message");
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("type-id"));
        }
    }

    @Test
    public void testUnknownTypeException() throws JsonMappingException {
        try {
            TypeFactory tf = TypeFactory.defaultInstance();
            JavaType type = tf.constructType(String.class);
            context.unknownTypeException(type, "Unknown message");
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Unknown message"));
        }
    }

    @Test
    public void testCoercionAction() {
        // Default implementation checks
        assertNotNull(context.findCoercionAction(null, null, null));
        assertNotNull(context.findCoercionFrom Blank_Empty(null, null, null));
    }

    /**
     * Concrete subclass of abstract DatabindContext for testing default methods.
     */
    private static class ConcreteDatabindContext extends DatabindContext {
        @Override
        public MapperConfig<?> getConfig() {
            return null;
        }

        @Override
        public AnnotationIntrospector getAnnotationIntrospector() {
            return null;
        }

        @Override
        public JavaType constructType(Type t) {
            return null;
        }

        @Override
        public JavaType constructSpecializedType(JavaType baseType, Class<?> subclass) {
            return null;
        }

        @Override
        public TypeFactory getTypeFactory() {
            return null;
        }

        @Override
        public boolean isEnabled(MapperFeature feature) {
            return false;
        }

        @Override
        public JsonFormat.Value getDefaultPropertyFormat(Class<?> baseType) {
            return null;
        }

        @Override
        public Locale getDefaultLocale() {
            return null;
        }

        @Override
        public TimeZone getDefaultTimeZone() {
            return null;
        }

        @Override
        public Object oids() {
            return null;
        }

        @Override
        public void acceptJsonFormatVisitor(JavaType javaType, com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper visitor) throws JsonMappingException {
        }
    }
}