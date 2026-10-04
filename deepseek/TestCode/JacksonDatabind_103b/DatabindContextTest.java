package com.fasterxml.jackson.databind;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.cfg.DatabindConfig;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospector;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ClassUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@RunWith(MockitoJUnitRunner.class)
public class DatabindContextTest {

    @Mock
    private DatabindConfig mockConfig;
    @Mock
    private AnnotationIntrospector mockAnnotationIntrospector;
    @Mock
    private TypeFactory mockTypeFactory;
    @Mock
    private JsonParser mockParser;
    @Mock
    private JsonGenerator mockGenerator;
    @Mock
    private DateFormat mockDateFormat;
    @Mock
    private ObjectMapper mockObjectMapper;

    private TestDatabindContext context;

    @Before
    public void setUp() {
        context = new TestDatabindContext();
        context.config = mockConfig;
        context.annotationIntrospector = mockAnnotationIntrospector;
        context.typeFactory = mockTypeFactory;
        context.parser = mockParser;
        context.generator = mockGenerator;
        context.dateFormat = mockDateFormat;
        context.objectMapper = mockObjectMapper;
    }

    // -----------------------------------------------------------------------
    // Tests for concrete methods: getAttribute / setAttribute
    // -----------------------------------------------------------------------

    @Test
    public void testGetAttributeNullKey() {
        assertNull("getAttribute(null) should return null", context.getAttribute(null));
    }

    @Test
    public void testGetAttributeNonExistentKey() {
        assertNull("getAttribute(nonExistent) should return null", context.getAttribute("unknown"));
    }

    @Test
    public void testSetAndGetAttribute() {
        context.setAttribute("key1", "value1");
        assertEquals("value1", context.getAttribute("key1"));
    }

    @Test
    public void testSetAttributeNullValue() {
        context.setAttribute("key2", null);
        assertNull("getAttribute after setting null should return null", context.getAttribute("key2"));
    }

    @Test
    public void testSetAttributeOverwrite() {
        context.setAttribute("key3", "old");
        context.setAttribute("key3", "new");
        assertEquals("new", context.getAttribute("key3"));
    }

    @Test
    public void testSetAttributeNullKey() {
        // Should not throw; behavior is to ignore or store? In Jackson it stores null key.
        context.setAttribute(null, "value");
        assertNull("getAttribute(null) after setting null key should return null", context.getAttribute(null));
    }

    @Test
    public void testGetAttributeAfterClear() {
        context.setAttribute("temp", "value");
        context.setAttribute("temp", null);
        assertNull("getAttribute after clearing via set null should return null", context.getAttribute("temp"));
    }

    // -----------------------------------------------------------------------
    // Tests for abstract methods (via concrete implementation)
    // -----------------------------------------------------------------------

    @Test
    public void testGetConfig() {
        assertSame(mockConfig, context.getConfig());
    }

    @Test
    public void testGetAnnotationIntrospector() {
        assertSame(mockAnnotationIntrospector, context.getAnnotationIntrospector());
    }

    @Test
    public void testGetTypeFactory() {
        assertSame(mockTypeFactory, context.getTypeFactory());
    }

    @Test
    public void testConstructTypeWithClass() {
        JavaType expected = mock(JavaType.class);
        when(mockTypeFactory.constructType(String.class)).thenReturn(expected);
        assertSame(expected, context.constructType(String.class));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructTypeNull() {
        context.constructType(null);
    }

    @Test
    public void testConstructTypeWithParameterizedType() throws Exception {
        Type paramType = getParameterizedType();
        JavaType expected = mock(JavaType.class);
        when(mockTypeFactory.constructType(paramType)).thenReturn(expected);
        assertSame(expected, context.constructType(paramType));
    }

    @Test
    public void testConstructTypeWithTypeVariable() throws Exception {
        Type typeVar = getTypeVariable();
        JavaType expected = mock(JavaType.class);
        when(mockTypeFactory.constructType(typeVar)).thenReturn(expected);
        assertSame(expected, context.constructType(typeVar));
    }

    @Test
    public void testConstructTypeWithWildcardType() throws Exception {
        Type wildcard = getWildcardType();
        JavaType expected = mock(JavaType.class);
        when(mockTypeFactory.constructType(wildcard)).thenReturn(expected);
        assertSame(expected, context.constructType(wildcard));
    }

    @Test
    public void testConstructTypeWithGenericArrayType() throws Exception {
        Type genericArray = getGenericArrayType();
        JavaType expected = mock(JavaType.class);
        when(mockTypeFactory.constructType(genericArray)).thenReturn(expected);
        assertSame(expected, context.constructType(genericArray));
    }

    @Test
    public void testResolveTypeWithClass() {
        JavaType input = mock(JavaType.class);
        JavaType resolved = mock(JavaType.class);
        when(mockTypeFactory.resolveType(input)).thenReturn(resolved);
        assertSame(resolved, context.resolveType(input));
    }

    @Test(expected = NullPointerException.class)
    public void testResolveTypeNull() {
        context.resolveType(null);
    }

    @Test
    public void testGetActiveViewDefault() {
        assertNull("Default active view should be null", context.getActiveView());
    }

    @Test
    public void testSetActiveViewNonNull() {
        context.setActiveView(String.class);
        assertEquals(String.class, context.getActiveView());
    }

    @Test
    public void testSetActiveViewNull() {
        context.setActiveView(null);
        assertNull("Active view after setting null should be null", context.getActiveView());
    }

    @Test
    public void testGetLocaleDefault() {
        assertEquals(Locale.getDefault(), context.getLocale());
    }

    @Test
    public void testGetTimeZoneDefault() {
        assertEquals(TimeZone.getDefault(), context.getTimeZone());
    }

    @Test
    public void testGetBase64VariantDefault() {
        assertSame(Base64Variants.getDefaultVariant(), context.getBase64Variant());
    }

    @Test
    public void testGetParser() {
        assertSame(mockParser, context.getParser());
    }

    @Test
    public void testGetGenerator() {
        assertSame(mockGenerator, context.getGenerator());
    }

    @Test
    public void testGetDateFormat() {
        assertSame(mockDateFormat, context.getDateFormat());
    }

    @Test
    public void testGetObjectMapper() {
        assertSame(mockObjectMapper, context.getObjectMapper());
    }

    @Test
    public void testIsEnabledDeserializationFeature() {
        DeserializationFeature feature = DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS;
        when(mockConfig.isEnabled(feature)).thenReturn(true);
        assertTrue(context.isEnabled(feature));
    }

    @Test
    public void testIsEnabledSerializationFeature() {
        SerializationFeature feature = SerializationFeature.INDENT_OUTPUT;
        when(mockConfig.isEnabled(feature)).thenReturn(false);
        assertFalse(context.isEnabled(feature));
    }

    @Test
    public void testIsEnabledMapperFeature() {
        MapperFeature feature = MapperFeature.USE_ANNOTATIONS;
        when(mockConfig.isEnabled(feature)).thenReturn(true);
        assertTrue(context.isEnabled(feature));
    }

    @Test(expected = NullPointerException.class)
    public void testIsEnabledNullFeature() {
        context.isEnabled((DeserializationFeature) null);
    }

    @Test
    public void testHasLengthOfString() {
        assertFalse("Default implementation should return false", context.hasLengthOfString());
    }

    @Test
    public void testHasLengthOfStringValue() {
        assertFalse("Default implementation should return false", context.hasLengthOfStringValue());
    }

    // -----------------------------------------------------------------------
    // Helper methods to create various java.lang.reflect.Type instances
    // -----------------------------------------------------------------------

    private static class GenericHolder<T> {
        public T field;
        public List<T> listField;
    }

    private static class StringHolder extends GenericHolder<String> {
    }

    private Type getParameterizedType() throws Exception {
        // Get the generic superclass of StringHolder: GenericHolder<String>
        return StringHolder.class.getGenericSuperclass();
    }

    private Type getTypeVariable() throws Exception {
        // Get the type variable T from GenericHolder
        TypeVariable<?> tv = GenericHolder.class.getTypeParameters()[0];
        return tv;
    }

    private Type getWildcardType() throws Exception {
        // Create a field with wildcard type: List<? extends Number>
        Field field = WildcardHolder.class.getDeclaredField("wildcardField");
        return field.getGenericType();
    }

    private static class WildcardHolder {
        public List<? extends Number> wildcardField;
    }

    private Type getGenericArrayType() throws Exception {
        // Create a field with generic array type: List<String>[]
        Field field = ArrayHolder.class.getDeclaredField("arrayField");
        return field.getGenericType();
    }

    private static class ArrayHolder {
        public List<String>[] arrayField;
    }

    // -----------------------------------------------------------------------
    // Concrete implementation of DatabindContext for testing
    // -----------------------------------------------------------------------

    private static class TestDatabindContext extends DatabindContext {
        DatabindConfig config;
        AnnotationIntrospector annotationIntrospector;
        TypeFactory typeFactory;
        JsonParser parser;
        JsonGenerator generator;
        DateFormat dateFormat;
        ObjectMapper objectMapper;
        Class<?> activeView;
        Locale locale = Locale.getDefault();
        TimeZone timeZone = TimeZone.getDefault();
        Base64Variant base64Variant = Base64Variants.getDefaultVariant();

        @Override
        public DatabindConfig getConfig() {
            return config;
        }

        @Override
        public AnnotationIntrospector getAnnotationIntrospector() {
            return annotationIntrospector;
        }

        @Override
        public TypeFactory getTypeFactory() {
            return typeFactory;
        }

        @Override
        public JavaType constructType(Type type) {
            // Delegate to TypeFactory
            return getTypeFactory().constructType(type);
        }

        @Override
        public JavaType resolveType(JavaType type) {
            return getTypeFactory().resolveType(type);
        }

        @Override
        public Class<?> getActiveView() {
            return activeView;
        }

        @Override
        public void setActiveView(Class<?> activeView) {
            this.activeView = activeView;
        }

        @Override
        public Locale getLocale() {
            return locale;
        }

        @Override
        public TimeZone getTimeZone() {
            return timeZone;
        }

        @Override
        public Base64Variant getBase64Variant() {
            return base64Variant;
        }

        @Override
        public JsonParser getParser() {
            return parser;
        }

        @Override
        public JsonGenerator getGenerator() {
            return generator;
        }

        @Override
        public DateFormat getDateFormat() {
            return dateFormat;
        }

        @Override
        public ObjectMapper getObjectMapper() {
            return objectMapper;
        }

        @Override
        public boolean isEnabled(DeserializationFeature feature) {
            return getConfig().isEnabled(feature);
        }

        @Override
        public boolean isEnabled(SerializationFeature feature) {
            return getConfig().isEnabled(feature);
        }

        @Override
        public boolean isEnabled(MapperFeature feature) {
            return getConfig().isEnabled(feature);
        }

        @Override
        public boolean isEnabled(FormatFeature feature) {
            return getConfig().isEnabled(feature);
        }

        @Override
        public boolean hasLengthOfString() {
            return false;
        }

        @Override
        public boolean hasLengthOfStringValue() {
            return false;
        }
    }
}