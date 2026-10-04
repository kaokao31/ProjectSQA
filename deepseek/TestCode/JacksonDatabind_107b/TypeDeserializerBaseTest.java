package com.fasterxml.jackson.databind.jsontype.impl;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.impl.TypeDeserializerBase;

import java.io.IOException;

/**
 * Test suite for TypeDeserializerBase.
 * Covers constructors, forProperty, getPropertyName, getTypeId,
 * _deserialize, _deserializeWithType, and _findDeserializer.
 */
public class TypeDeserializerBaseTest {

    private static final String TEST_TYPE_ID = "testTypeId";
    private static final String TEST_PROPERTY_NAME = "testProperty";

    private TestTypeDeserializer base;
    private BeanProperty mockProperty;
    private JsonParser mockParser;
    private DeserializationContext mockContext;

    @Before
    public void setUp() throws Exception {
        // Create a simple BeanProperty stub
        mockProperty = new BeanProperty.Std(TEST_PROPERTY_NAME, null, null, null, null);
        // Create a concrete subclass for testing
        base = new TestTypeDeserializer(TEST_TYPE_ID, null);
        // Create minimal mocks for parser and context (using anonymous classes)
        mockParser = new JsonParser() {
            @Override
            public Object getCurrentValue() { return null; }
            @Override
            public JsonToken getCurrentToken() { return JsonToken.START_OBJECT; }
            @Override
            public JsonToken nextToken() throws IOException { return JsonToken.END_OBJECT; }
            // Other abstract methods – minimal stubs
            @Override
            public void close() throws IOException {}
            @Override
            public int getCurrentTokenId() { return 0; }
            @Override
            public boolean hasCurrentToken() { return true; }
            @Override
            public boolean hasToken(JsonToken t) { return false; }
            @Override
            public boolean hasTokenId(int id) { return false; }
            @Override
            public JsonLocation getCurrentLocation() { return null; }
            @Override
            public JsonLocation getTokenLocation() { return null; }
            @Override
            public String getCurrentName() throws IOException { return null; }
            @Override
            public void clearCurrentToken() {}
            @Override
            public JsonParser skipChildren() throws IOException { return this; }
            @Override
            public boolean isClosed() { return false; }
            @Override
            public ObjectCodec getCodec() { return null; }
            @Override
            public void setCodec(ObjectCodec c) {}
            // Override needed for test
            @Override
            public String getText() throws IOException { return "testText"; }
            @Override
            public char[] getTextCharacters() throws IOException { return new char[0]; }
            @Override
            public int getTextLength() throws IOException { return 0; }
            @Override
            public int getTextOffset() throws IOException { return 0; }
            @Override
            public int getText(Writer writer) throws IOException { return 0; }
            @Override
            public Number getNumberValue() throws IOException { return 0; }
            @Override
            public Number getNumberValueExact() throws IOException { return 0; }
            @Override
            public int getIntValue() throws IOException { return 0; }
            @Override
            public long getLongValue() throws IOException { return 0; }
            @Override
            public BigInteger getBigIntegerValue() throws IOException { return BigInteger.ZERO; }
            @Override
            public float getFloatValue() throws IOException { return 0f; }
            @Override
            public double getDoubleValue() throws IOException { return 0d; }
            @Override
            public BigDecimal getDecimalValue() throws IOException { return BigDecimal.ZERO; }
            @Override
            public byte[] getBinaryValue(Base64Variant b64variant) throws IOException { return new byte[0]; }
            @Override
            public String getValueAsString() throws IOException { return null; }
            @Override
            public String getValueAsString(String defaultValue) throws IOException { return defaultValue; }
            @Override
            public boolean getValueAsBoolean(boolean defaultValue) throws IOException { return defaultValue; }
            @Override
            public int getValueAsInt(int defaultValue) throws IOException { return defaultValue; }
            @Override
            public long getValueAsLong(long defaultValue) throws IOException { return defaultValue; }
            @Override
            public double getValueAsDouble(double defaultValue) throws IOException { return defaultValue; }
            @Override
            public Object getEmbeddedObject() { return null; }
            @Override
            public byte[] getByteArray() throws IOException { return new byte[0]; }
            @Override
            public boolean nextFieldName(SerializableString str) throws IOException { return false; }
            @Override
            public String nextFieldName() throws IOException { return null; }
            @Override
            public String nextTextValue() throws IOException { return null; }
            @Override
            public int nextIntValue(int defaultValue) throws IOException { return defaultValue; }
            @Override
            public long nextLongValue(long defaultValue) throws IOException { return defaultValue; }
            @Override
            public Boolean nextBooleanValue() throws IOException { return null; }
            @Override
            public void finishToken() throws IOException {}
            @Override
            public JsonParser overrideCurrentName(String name) { return this; }
            @Override
            public boolean requiresCustomCodec() { return false; }
            @Override
            public Version version() { return Version.unknownVersion(); }
            @Override
            public int getFormatFeatures() { return 0; }
            @Override
            public int getFeatureMask() { return 0; }
            @Override
            public JsonParser setFeatureMask(int mask) { return this; }
            @Override
            public JsonParser enable(JsonParser.Feature f) { return this; }
            @Override
            public JsonParser disable(JsonParser.Feature f) { return this; }
            @Override
            public boolean isEnabled(JsonParser.Feature f) { return false; }
            @Override
            public boolean canUseSchema(FormatSchema schema) { return false; }
            @Override
            public void setSchema(FormatSchema schema) {}
            @Override
            public FormatSchema getSchema() { return null; }
            @Override
            public boolean canReadTypeId() { return false; }
            @Override
            public boolean canReadObjectId() { return false; }
            @Override
            public Object getTypeId() throws IOException { return null; }
            @Override
            public Object getObjectId() throws IOException { return null; }
        };
        mockContext = new DeserializationContext(null, null, null, null) {
            // Minimal stub – override abstract methods
            @Override
            public DeserializerFactory getFactory() { return null; }
            @Override
            public int getDeserializationFeatures() { return 0; }
            @Override
            public boolean hasDeserializationFeatures(int featureMask) { return false; }
            @Override
            public boolean hasSomeOfFeatures(int featureMask) { return false; }
            @Override
            public boolean isEnabled(DeserializationFeature feature) { return false; }
            @Override
            public int getAttribute(Object key) { return 0; }
            @Override
            public void setAttribute(Object key, Object value) {}
            @Override
            public JsonDeserializer<Object> deserializerInstance(Annotated annotated, Object deserDef) throws JsonMappingException { return null; }
            @Override
            public Object keyDeserializerInstance(Annotated annotated, Object deserDef) throws JsonMappingException { return null; }
            @Override
            public JsonNode getNodeFactory() { return null; }
            @Override
            public Locale getLocale() { return null; }
            @Override
            public TimeZone getTimeZone() { return null; }
            @Override
            public boolean hasValueDeserializerFor(JavaType type, UnknownTypeIdResolver idResolver) { return false; }
            @Override
            public JavaType unknownType() { return null; }
            @Override
            public JavaType resolveType(JavaType type) { return type; }
            @Override
            public JsonParser getParser() { return mockParser; }
            @Override
            public Object findInjectableValue(Object valueId, BeanProperty forProperty, Object beanInstance) { return null; }
            @Override
            public Base64Variant getBase64Variant() { return null; }
            @Override
            public JsonDeserializer<?> findRootValueDeserializer(JavaType type) throws JsonMappingException { return null; }
            @Override
            public JsonDeserializer<?> findValueDeserializer(JavaType type, BeanProperty prop) throws JsonMappingException { return null; }
            @Override
            public JsonDeserializer<?> findContextualValueDeserializer(JavaType type, BeanProperty prop) throws JsonMappingException { return null; }
            @Override
            public KeyDeserializer findKeyDeserializer(JavaType type, BeanProperty prop) throws JsonMappingException { return null; }
            @Override
            public TypeDeserializer findTypeDeserializer(JavaType type, BeanProperty prop) throws JsonMappingException { return null; }
            @Override
            public Object readValue(JsonParser p, JavaType valueType) throws IOException { return null; }
            @Override
            public <T> T readValue(JsonParser p, Class<T> valueType) throws IOException { return null; }
            @Override
            public void reportUnknownProperty(Object bean, String fieldName, JsonParser p) throws IOException {}
            @Override
            public void reportMissingKey(JsonParser p) throws IOException {}
            @Override
            public void reportMappingException(String message, Object... args) throws JsonMappingException {}
            @Override
            public void reportWrongTokenException(JsonParser p, JsonToken expToken, String msg, Object... args) throws JsonMappingException {}
            @Override
            public void reportWrongTokenException(JavaType t, JsonToken expToken, String msg, Object... args) throws JsonMappingException {}
            @Override
            public void reportInputMismatch(BeanProperty prop, String msg, Object... args) throws JsonMappingException {}
            @Override
            public void reportInputMismatch(JavaType targetType, String msg, Object... args) throws JsonMappingException {}
            @Override
            public void reportBadDefinition(JavaType type, String msg) throws JsonMappingException {}
            @Override
            public <T> T reportBadMerge(JsonDeserializer<?> deser) throws JsonMappingException { return null; }
            @Override
            public JsonMappingException instantiationException(JavaType type, Throwable cause) { return null; }
            @Override
            public JsonMappingException instantiationException(Class<?> instClass, Throwable cause) { return null; }
            @Override
            public JsonMappingException weirdStringException(String value, Class<?> type, String msg, Object... args) { return null; }
            @Override
            public JsonMappingException weirdNumberException(Number value, Class<?> type, String msg, Object... args) { return null; }
            @Override
            public JsonMappingException weirdKeyException(Class<?> keyClass, String key, String msg, Object... args) { return null; }
            @Override
            public JsonMappingException wrongTokenException(JsonParser p, JsonToken expToken, String msg, Object... args) { return null; }
            @Override
            public JsonMappingException unknownTypeException(JavaType type, String id, String msg) { return null; }
            @Override
            public JsonMappingException unknownPropertyException(Object bean, String propertyName, JsonParser p) { return null; }
            @Override
            public JsonMappingException missingPropertyException(BeanProperty prop) { return null; }
            @Override
            public JsonMappingException nodeNotNumberException(JsonNode node, String msg) { return null; }
            @Override
            public JsonMappingException nodeNotStringException(JsonNode node, String msg) { return null; }
            @Override
            public JsonMappingException nodeNotBooleanException(JsonNode node, String msg) { return null; }
            @Override
            public JsonMappingException handleUnexpectedToken(JavaType targetType, JsonToken token, JsonParser p, String msg, Object... args) { return null; }
            @Override
            public JsonMappingException handleUnexpectedToken(Class<?> instClass, JsonToken token, JsonParser p, String msg, Object... args) { return null; }
            @Override
            public JsonMappingException handleWeirdStringValue(Class<?> targetClass, String value, String msg, Object... args) { return null; }
            @Override
            public JsonMappingException handleWeirdNumberValue(Class<?> targetClass, Number value, String msg, Object... args) { return null; }
            @Override
            public JsonMappingException handleWeirdKey(Class<?> keyClass, String key, String msg, Object... args) { return null; }
            @Override
            public JsonMappingException handleMissingInstantiator(Class<?> instClass, JsonParser p, String msg, Object... args) { return null; }
            @Override
            public JsonMappingException handleMissingInstantiator(JavaType type, JsonParser p, String msg, Object... args) { return null; }
            @Override
            public JsonMappingException handleInstantiationProblem(Class<?> instClass, Object argument, Throwable t) { return null; }
            @Override
            public JsonMappingException handleInstantiationProblem(JavaType type, Object argument, Throwable t) { return null; }
            @Override
            public JsonMappingException handleUnknownProperty(Object bean, String propertyName, JsonParser p) { return null; }
            @Override
            public JsonMappingException handleUnknownTypeId(JavaType type, String typeId, TypeDeserializer typeDeser, String msg) { return null; }
            @Override
            public JsonMappingException handleMissingTypeId(JavaType type, TypeDeserializer typeDeser, String msg) { return null; }
            @Override
            public JsonMappingException handleNotDeserializable(Class<?> targetClass, String msg) { return null; }
            @Override
            public JsonMappingException handleBadMerge(JsonDeserializer<?> deser) { return null; }
            @Override
            public JsonMappingException endOfInputException(Class<?> instClass) { return null; }
            @Override
            public JsonMappingException invalidTypeIdException(JavaType type, String typeId, String msg) { return null; }
            @Override
            public JsonMappingException wrongTokenException(JavaType type, JsonToken token, String msg, Object... args) { return null; }
            @Override
            public void reportTrailingTokens(Object value, JsonParser p) throws IOException {}
            @Override
            public void reportUnresolvedObjectId(Object reader, String msg) throws JsonMappingException {}
            @Override
            public Object getOwner() { return null; }
            @Override
            public DeserializationConfig getConfig() { return null; }
            @Override
            public AnnotationIntrospector getAnnotationIntrospector() { return null; }
            @Override
            public TypeFactory getTypeFactory() { return null; }
            @Override
            public Class<?> getActiveView() { return null; }
            @Override
            public boolean canOverrideAccessModifiers() { return false; }
            @Override
            public boolean hasDeserializationFeature(DeserializationFeature feature) { return false; }
            @Override
            public boolean isEnabled(JsonParser.Feature feature) { return false; }
            @Override
            public boolean isEnabled(JsonGenerator.Feature feature) { return false; }
            @Override
            public boolean isEnabled(MapperFeature feature) { return false; }
            @Override
            public boolean isEnabled(FormatFeature feature) { return false; }
            @Override
            public int getFeatureIndex(DeserializationFeature feature) { return 0; }
            @Override
            public int getDeserializationFeatureMask() { return 0; }
            @Override
            public Object getAttribute(Object key) { return null; }
            @Override
            public void setAttribute(Object key, Object value) {}
            @Override
            public JavaType constructType(Class<?> cls) { return null; }
            @Override
            public JavaType constructType(TypeReference<?> typeRef) { return null; }
            @Override
            public JavaType constructSpecializedType(JavaType baseType, Class<?> subclass) { return null; }
            @Override
            public JsonDeserializer<Object> findDeserializer(JavaType type, BeanProperty prop) throws JsonMappingException { return null; }
            @Override
            public JsonDeserializer<Object> findContextualDeserializer(JavaType type, BeanProperty prop) throws JsonMappingException { return null; }
            @Override
            public KeyDeserializer findKeyDeserializer(JavaType type, BeanProperty prop) throws JsonMappingException { return null; }
            @Override
            public TypeDeserializer findTypeDeserializer(JavaType type, BeanProperty prop) throws JsonMappingException { return null; }
            @Override
            public Object findInjectableValue(Object valueId, BeanProperty forProperty, Object beanInstance) { return null; }
            @Override
            public Base64Variant getBase64Variant() { return null; }
            @Override
            public JsonNode getNodeFactory() { return null; }
            @Override
            public Locale getLocale() { return null; }
            @Override
            public TimeZone getTimeZone() { return null; }
            @Override
            public boolean hasValueDeserializerFor(JavaType type, UnknownTypeIdResolver idResolver) { return false; }
            @Override
            public JavaType unknownType() { return null; }
            @Override
            public JavaType resolveType(JavaType type) { return type; }
            @Override
            public JsonParser getParser() { return mockParser; }
            @Override
            public Object readValue(JsonParser p, JavaType valueType) throws IOException { return null; }
            @Override
            public <T> T readValue(JsonParser p, Class<T> valueType) throws IOException { return null; }
            @Override
            public void reportUnknownProperty(Object bean, String fieldName, JsonParser p) throws IOException {}
            @Override
            public void reportMissingKey(JsonParser p) throws IOException {}
            @Override
            public void reportMappingException(String message, Object... args) throws JsonMappingException {}
            @Override
            public void reportWrongTokenException(JsonParser p, JsonToken expToken, String msg, Object... args) throws JsonMappingException {}
            @Override
            public void reportWrongTokenException(JavaType t, JsonToken expToken, String msg, Object... args) throws JsonMappingException {}
            @Override
            public void reportInputMismatch(BeanProperty prop, String msg, Object... args) throws JsonMappingException {}
            @Override
            public void reportInputMismatch(JavaType targetType, String msg, Object... args) throws JsonMappingException {}
            @Override
            public void reportBadDefinition(JavaType type, String msg) throws JsonMappingException {}
            @Override
            public <T> T reportBadMerge(JsonDeserializer<?> deser) throws JsonMappingException { return null; }
            @Override
            public JsonMappingException instantiationException(JavaType type, Throwable cause) { return null; }
            @Override
            public JsonMappingException instantiationException(Class<?> instClass, Throwable cause) { return null; }
            @Override
            public JsonMappingException weirdStringException(String value, Class<?> type, String msg, Object... args) { return null; }
            @Override
            public JsonMappingException weirdNumberException(Number value, Class<?> type, String msg, Object... args) { return null; }
            @Override
            public JsonMappingException weirdKeyException(Class<?> keyClass, String key, String msg, Object... args) { return null; }
            @Override
            public JsonMappingException wrongTokenException(JsonParser p, JsonToken expToken, String msg, Object... args) { return null; }
            @Override
            public JsonMappingException unknownTypeException(JavaType type, String id, String msg) { return null; }
            @Override
            public JsonMappingException unknownPropertyException(Object bean, String propertyName, JsonParser p) { return null; }
            @Override
            public JsonMappingException missingPropertyException(BeanProperty prop) { return null; }
            @Override
            public JsonMappingException nodeNotNumberException(JsonNode node, String msg) { return null; }
            @Override
            public JsonMappingException nodeNotStringException(JsonNode node, String msg) { return null; }
            @Override
            public JsonMappingException nodeNotBooleanException(JsonNode node, String msg) { return null; }
            @Override
            public JsonMappingException handleUnexpectedToken(JavaType targetType, JsonToken token, JsonParser p, String msg, Object... args) { return null; }
            @Override
            public JsonMappingException handleUnexpectedToken(Class<?> instClass, JsonToken token, JsonParser p, String msg, Object... args) { return null; }
            @Override
            public JsonMappingException handleWeirdStringValue(Class<?> targetClass, String value, String msg, Object... args) { return null; }
            @Override
            public JsonMappingException handleWeirdNumberValue(Class<?> targetClass, Number value, String msg, Object... args) { return null; }
            @Override
            public JsonMappingException handleWeirdKey(Class<?> keyClass, String key, String msg, Object... args) { return null; }
            @Override
            public JsonMappingException handleMissingInstantiator(Class<?> instClass, JsonParser p, String msg, Object... args) { return null; }
            @Override
            public JsonMappingException handleMissingInstantiator(JavaType type, JsonParser p, String msg, Object... args) { return null; }
            @Override
            public JsonMappingException handleInstantiationProblem(Class<?> instClass, Object argument, Throwable t) { return null; }
            @Override
            public JsonMappingException handleInstantiationProblem(JavaType type, Object argument, Throwable t) { return null; }
            @Override
            public JsonMappingException handleUnknownProperty(Object bean, String propertyName, JsonParser p) { return null; }
            @Override
            public JsonMappingException handleUnknownTypeId(JavaType type, String typeId, TypeDeserializer typeDeser, String msg) { return null; }
            @Override
            public JsonMappingException handleMissingTypeId(JavaType type, TypeDeserializer typeDeser, String msg) { return null; }
            @Override
            public JsonMappingException handleNotDeserializable(Class<?> targetClass, String msg) { return null; }
            @Override
            public JsonMappingException handleBadMerge(JsonDeserializer<?> deser) { return null; }
            @Override
            public JsonMappingException endOfInputException(Class<?> instClass) { return null; }
            @Override
            public JsonMappingException invalidTypeIdException(JavaType type, String typeId, String msg) { return null; }
            @Override
            public JsonMappingException wrongTokenException(JavaType type, JsonToken token, String msg, Object... args) { return null; }
            @Override
            public void reportTrailingTokens(Object value, JsonParser p) throws IOException {}
            @Override
            public void reportUnresolvedObjectId(Object reader, String msg) throws JsonMappingException {}
        };
    }

    // Concrete subclass for testing abstract methods
    private static class TestTypeDeserializer extends TypeDeserializerBase {
        public TestTypeDeserializer(String typeId, BeanProperty property) {
            super(typeId, property);
        }

        @Override
        public TypeDeserializer forProperty(BeanProperty property) {
            return new TestTypeDeserializer(_typeId, property);
        }

        @Override
        public Object deserializeTypedFromObject(JsonParser p, DeserializationContext ctxt) throws IOException {
            return "deserializedObject";
        }

        @Override
        public Object deserializeTypedFromArray(JsonParser p, DeserializationContext ctxt) throws IOException {
            return "deserializedArray";
        }

        @Override
        public Object deserializeTypedFromScalar(JsonParser p, DeserializationContext ctxt) throws IOException {
            return "deserializedScalar";
        }

        @Override
        public Object deserializeTypedFromAny(JsonParser p, DeserializationContext ctxt) throws IOException {
            return "deserializedAny";
        }

        @Override
        protected Object _deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return "deserializedBase";
        }

        @Override
        protected Object _deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeser) throws IOException {
            return "deserializedWithType";
        }

        @Override
        protected JsonDeserializer<Object> _findDeserializer(DeserializationContext ctxt, String typeId) throws IOException {
            if (typeId == null) {
                throw new IllegalArgumentException("typeId cannot be null");
            }
            return null; // simplified
        }
    }

    // Test constructor with null typeId
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullTypeId() {
        new TestTypeDeserializer(null, mockProperty);
    }

    // Test constructor with valid typeId
    @Test
    public void testConstructorWithValidTypeId() {
        TestTypeDeserializer instance = new TestTypeDeserializer(TEST_TYPE_ID, mockProperty);
        assertNotNull(instance);
        assertEquals(TEST_TYPE_ID, instance.getTypeId());
        assertEquals(TEST_PROPERTY_NAME, instance.getPropertyName());
    }

    // Test forProperty returns new instance with property set
    @Test
    public void testForProperty() {
        BeanProperty newProperty = new BeanProperty.Std("newProperty", null, null, null, null);
        TypeDeserializer result = base.forProperty(newProperty);
        assertNotNull(result);
        assertTrue(result instanceof TestTypeDeserializer);
        TestTypeDeserializer casted = (TestTypeDeserializer) result;
        assertEquals(TEST_TYPE_ID, casted.getTypeId());
        assertEquals("newProperty", casted.getPropertyName());
    }

    // Test getPropertyName when property is null
    @Test
    public void testGetPropertyNameWithNullProperty() {
        TestTypeDeserializer noProp = new TestTypeDeserializer(TEST_TYPE_ID, null);
        assertNull(noProp.getPropertyName());
    }

    // Test getPropertyName when property is set
    @Test
    public void testGetPropertyNameWithProperty() {
        assertEquals(TEST_PROPERTY_NAME, base.getPropertyName());
    }

    // Test getTypeId
    @Test
    public void testGetTypeId() {
        assertEquals(TEST_TYPE_ID, base.getTypeId());
    }

    // Test _deserialize
    @Test
    public void testDeserialize() throws Exception {
        Object result = base._deserialize(mockParser, mockContext);
        assertEquals("deserializedBase", result);
    }

    // Test _deserializeWithType
    @Test
    public void testDeserializeWithType() throws Exception {
        Object result = base._deserializeWithType(mockParser, mockContext, base);
        assertEquals("deserializedWithType", result);
    }

    // Test _findDeserializer with null typeId
    @Test(expected = IllegalArgumentException.class)
    public void testFindDeserializerWithNullTypeId() throws Exception {
        base._findDeserializer(mockContext, null);
    }

    // Test _findDeserializer with valid typeId
    @Test
    public void testFindDeserializerWithValidTypeId() throws Exception {
        JsonDeserializer<Object> deser = base._findDeserializer(mockContext, TEST_TYPE_ID);
        assertNull(deser); // our stub returns null
    }

    // Test deserializeTypedFromObject
    @Test
    public void testDeserializeTypedFromObject() throws Exception {
        Object result = base.deserializeTypedFromObject(mockParser, mockContext);
        assertEquals("deserializedObject", result);
    }

    // Test deserializeTypedFromArray
    @Test
    public void testDeserializeTypedFromArray() throws Exception {
        Object result = base.deserializeTypedFromArray(mockParser, mockContext);
        assertEquals("deserializedArray", result);
    }

    // Test deserializeTypedFromScalar
    @Test
    public void testDeserializeTypedFromScalar() throws Exception {
        Object result = base.deserializeTypedFromScalar(mockParser, mockContext);
        assertEquals("deserializedScalar", result);
    }

    // Test deserializeTypedFromAny
    @Test
    public void testDeserializeTypedFromAny() throws Exception {
        Object result = base.deserializeTypedFromAny(mockParser, mockContext);
        assertEquals("deserializedAny", result);
    }

    // Edge case: empty typeId
    @Test
    public void testEmptyTypeId() {
        TestTypeDeserializer empty = new TestTypeDeserializer("", mockProperty);
        assertEquals("", empty.getTypeId());
    }

    // Edge case: null property in forProperty
    @Test
    public void testForPropertyWithNullProperty() {
        TypeDeserializer result = base.forProperty(null);
        assertNotNull(result);
        assertTrue(result instanceof TestTypeDeserializer);
        TestTypeDeserializer casted = (TestTypeDeserializer) result;
        assertEquals(TEST_TYPE_ID, casted.getTypeId());
        assertNull(casted.getPropertyName());
    }
}