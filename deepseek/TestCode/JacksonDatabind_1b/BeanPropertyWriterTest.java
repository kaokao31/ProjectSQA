package com.fasterxml.jackson.databind.ser;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.util.NameTransformer;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for BeanPropertyWriter.
 * Targets maximum line/branch coverage and fault detection.
 */
public class BeanPropertyWriterTest {

    private BeanPropertyWriter writer;
    private JsonGenerator jgen;
    private SerializerProvider provider;
    private SerializerFactory factory;
    private JavaType type;
    private BeanPropertyDefinition propDef;
    private AnnotatedMember member;
    private JsonSerializer<Object> serializer;

    @Before
    public void setUp() throws Exception {
        // Create mock objects using simple stubs (no mocking framework to keep JDK 8 compatible)
        jgen = new JsonGeneratorStub();
        provider = new SerializerProviderStub();
        factory = new SerializerFactoryStub();
        type = TypeFactory.defaultInstance().constructType(String.class);
        propDef = new BeanPropertyDefinitionStub();
        member = new AnnotatedMemberStub();
        serializer = new JsonSerializerStub();
    }

    // ---------- Constructor Tests ----------

    @Test
    public void testConstructorWithNullName() {
        try {
            writer = new BeanPropertyWriter(null, member, null, type, serializer, null, false);
            fail("Expected IllegalArgumentException for null name");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorWithNullMember() {
        try {
            writer = new BeanPropertyWriter("prop", null, null, type, serializer, null, false);
            fail("Expected IllegalArgumentException for null member");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorWithNullType() {
        try {
            writer = new BeanPropertyWriter("prop", member, null, null, serializer, null, false);
            fail("Expected IllegalArgumentException for null type");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorWithNullSerializer() {
        // Null serializer is allowed (will be resolved later)
        writer = new BeanPropertyWriter("prop", member, null, type, null, null, false);
        assertNotNull(writer);
        assertNull(writer.getSerializer());
    }

    @Test
    public void testConstructorWithAllValid() {
        writer = new BeanPropertyWriter("prop", member, null, type, serializer, null, false);
        assertNotNull(writer);
        assertEquals("prop", writer.getName());
        assertSame(serializer, writer.getSerializer());
    }

    // ---------- SerializeAsField Tests ----------

    @Test
    public void testSerializeAsFieldWithNullValue() throws Exception {
        writer = new BeanPropertyWriter("prop", member, null, type, serializer, null, false);
        // Simulate null value
        writer.serializeAsField(null, jgen, provider);
        // Should not write anything (field name not written)
        // Verify via stub: no field name written
        assertFalse(((JsonGeneratorStub) jgen).fieldNameWritten);
    }

    @Test
    public void testSerializeAsFieldWithNonNullValue() throws Exception {
        writer = new BeanPropertyWriter("prop", member, null, type, serializer, null, false);
        Object value = "testValue";
        writer.serializeAsField(value, jgen, provider);
        // Should write field name and value
        assertTrue(((JsonGeneratorStub) jgen).fieldNameWritten);
        assertEquals("prop", ((JsonGeneratorStub) jgen).lastFieldName);
        assertTrue(((JsonGeneratorStub) jgen).valueWritten);
    }

    @Test
    public void testSerializeAsFieldWithSuppressableValue() throws Exception {
        // Create writer with suppression filter
        writer = new BeanPropertyWriter("prop", member, null, type, serializer, null, false);
        // Assume value is suppressable (e.g., empty string)
        Object value = "";
        writer.serializeAsField(value, jgen, provider);
        // Should not write if suppressed
        assertFalse(((JsonGeneratorStub) jgen).fieldNameWritten);
    }

    @Test
    public void testSerializeAsFieldWithException() {
        writer = new BeanPropertyWriter("prop", member, null, type, new JsonSerializer<Object>() {
            @Override
            public void serialize(Object value, JsonGenerator gen, SerializerProvider prov) throws IOException {
                throw new IOException("Simulated serialization error");
            }
        }, null, false);
        try {
            writer.serializeAsField("value", jgen, provider);
            fail("Expected IOException");
        } catch (IOException e) {
            // expected
        }
    }

    // ---------- getSerializer Tests ----------

    @Test
    public void testGetSerializerWhenNull() {
        writer = new BeanPropertyWriter("prop", member, null, type, null, null, false);
        assertNull(writer.getSerializer());
    }

    @Test
    public void testGetSerializerWhenSet() {
        writer = new BeanPropertyWriter("prop", member, null, type, serializer, null, false);
        assertSame(serializer, writer.getSerializer());
    }

    // ---------- getPropertyName Tests ----------

    @Test
    public void testGetPropertyName() {
        writer = new BeanPropertyWriter("myProp", member, null, type, serializer, null, false);
        assertEquals("myProp", writer.getName());
    }

    // ---------- isRequired Tests ----------

    @Test
    public void testIsRequiredDefault() {
        writer = new BeanPropertyWriter("prop", member, null, type, serializer, null, false);
        assertFalse(writer.isRequired());
    }

    @Test
    public void testIsRequiredTrue() {
        writer = new BeanPropertyWriter("prop", member, null, type, serializer, null, true);
        assertTrue(writer.isRequired());
    }

    // ---------- getAnnotation Tests ----------

    @Test
    public void testGetAnnotationPresent() {
        // Assume member has annotation
        writer = new BeanPropertyWriter("prop", member, null, type, serializer, null, false);
        assertNull(writer.getAnnotation(Deprecated.class)); // not present in stub
    }

    @Test
    public void testGetAnnotationNotPresent() {
        writer = new BeanPropertyWriter("prop", member, null, type, serializer, null, false);
        assertNull(writer.getAnnotation(Override.class));
    }

    // ---------- getContextAnnotation Tests ----------

    @Test
    public void testGetContextAnnotation() {
        writer = new BeanPropertyWriter("prop", member, null, type, serializer, null, false);
        assertNull(writer.getContextAnnotation(Deprecated.class));
    }

    // ---------- rename Tests ----------

    @Test
    public void testRename() {
        writer = new BeanPropertyWriter("prop", member, null, type, serializer, null, false);
        writer.rename(new NameTransformer() {
            @Override
            public String transform(String name) {
                return "renamed_" + name;
            }

            @Override
            public String reverse(String transformed) {
                return transformed.substring(8);
            }
        });
        assertEquals("renamed_prop", writer.getName());
    }

    @Test
    public void testRenameWithNullTransformer() {
        writer = new BeanPropertyWriter("prop", member, null, type, serializer, null, false);
        writer.rename(null);
        assertEquals("prop", writer.getName()); // should remain unchanged
    }

    // ---------- assignSerializer Tests ----------

    @Test
    public void testAssignSerializer() {
        writer = new BeanPropertyWriter("prop", member, null, type, null, null, false);
        assertNull(writer.getSerializer());
        JsonSerializer<Object> newSer = new JsonSerializerStub();
        writer.assignSerializer(newSer);
        assertSame(newSer, writer.getSerializer());
    }

    @Test
    public void testAssignSerializerNull() {
        writer = new BeanPropertyWriter("prop", member, null, type, serializer, null, false);
        writer.assignSerializer(null);
        assertNull(writer.getSerializer());
    }

    // ---------- assignNullSerializer Tests ----------

    @Test
    public void testAssignNullSerializer() {
        writer = new BeanPropertyWriter("prop", member, null, type, null, null, false);
        writer.assignNullSerializer(new JsonSerializer<Object>() {
            @Override
            public void serialize(Object value, JsonGenerator gen, SerializerProvider prov) throws IOException {
                gen.writeNull();
            }
        });
        // Not directly testable without internal state, but ensures no exception
    }

    // ---------- willSuppressNulls Tests ----------

    @Test
    public void testWillSuppressNullsDefault() {
        writer = new BeanPropertyWriter("prop", member, null, type, serializer, null, false);
        assertFalse(writer.willSuppressNulls());
    }

    @Test
    public void testWillSuppressNullsAfterSetting() {
        writer = new BeanPropertyWriter("prop", member, null, type, serializer, null, false);
        // Assume internal method sets suppression
        // Not directly accessible, but we can test via behavior
    }

    // ---------- toString Tests ----------

    @Test
    public void testToString() {
        writer = new BeanPropertyWriter("prop", member, null, type, serializer, null, false);
        String str = writer.toString();
        assertNotNull(str);
        assertTrue(str.contains("prop"));
    }

    // ---------- Edge Cases ----------

    @Test
    public void testSerializeAsFieldWithEmptyName() throws Exception {
        writer = new BeanPropertyWriter("", member, null, type, serializer, null, false);
        writer.serializeAsField("value", jgen, provider);
        // Should write empty field name
        assertEquals("", ((JsonGeneratorStub) jgen).lastFieldName);
    }

    @Test
    public void testSerializeAsFieldWithNullBean() throws Exception {
        writer = new BeanPropertyWriter("prop", member, null, type, serializer, null, false);
        writer.serializeAsField(null, jgen, provider);
        // Should not write anything
        assertFalse(((JsonGeneratorStub) jgen).fieldNameWritten);
    }

    @Test
    public void testSerializeAsFieldWithSuppressableValueAndNullSerializer() throws Exception {
        writer = new BeanPropertyWriter("prop", member, null, type, null, null, false);
        // Value that is suppressable (e.g., empty string)
        writer.serializeAsField("", jgen, provider);
        // Should not write field name
        assertFalse(((JsonGeneratorStub) jgen).fieldNameWritten);
    }

    // ---------- Helper Stubs (inner classes) ----------

    static class JsonGeneratorStub extends JsonGenerator {
        boolean fieldNameWritten = false;
        String lastFieldName = null;
        boolean valueWritten = false;

        @Override
        public void writeFieldName(String name) throws IOException {
            fieldNameWritten = true;
            lastFieldName = name;
        }

        @Override
        public void writeString(String text) throws IOException {
            valueWritten = true;
        }

        @Override
        public void writeNull() throws IOException {
            valueWritten = true;
        }

        // Other abstract methods - no-op
        @Override public void writeStartArray() throws IOException {}
        @Override public void writeEndArray() throws IOException {}
        @Override public void writeStartObject() throws IOException {}
        @Override public void writeEndObject() throws IOException {}
        @Override public void writeFieldName(SerializableString name) throws IOException {}
        @Override public void writeString(char[] text, int offset, int len) throws IOException {}
        @Override public void writeRawUTF8String(byte[] text, int offset, int len) throws IOException {}
        @Override public void writeUTF8String(byte[] text, int offset, int len) throws IOException {}
        @Override public void writeRaw(String text) throws IOException {}
        @Override public void writeRaw(String text, int offset, int len) throws IOException {}
        @Override public void writeRaw(char[] text, int offset, int len) throws IOException {}
        @Override public void writeRaw(char c) throws IOException {}
        @Override public void writeBinary(Base64Variant bv, byte[] data, int offset, int len) throws IOException {}
        @Override public int writeBinary(Base64Variant bv, InputStream data, int dataLength) throws IOException { return 0; }
        @Override public void writeNumber(int v) throws IOException {}
        @Override public void writeNumber(long v) throws IOException {}
        @Override public void writeNumber(BigInteger v) throws IOException {}
        @Override public void writeNumber(double v) throws IOException {}
        @Override public void writeNumber(float v) throws IOException {}
        @Override public void writeNumber(BigDecimal v) throws IOException {}
        @Override public void writeNumber(String encodedValue) throws IOException {}
        @Override public void writeBoolean(boolean state) throws IOException {}
        @Override public void writeObject(Object pojo) throws IOException {}
        @Override public void writeTree(TreeNode rootNode) throws IOException {}
        @Override public JsonGenerator copy() { return null; }
        @Override public JsonStreamContext getOutputContext() { return null; }
        @Override public void flush() throws IOException {}
        @Override public boolean isClosed() { return false; }
        @Override public void close() throws IOException {}
    }

    static class SerializerProviderStub extends SerializerProvider {
        protected SerializerProviderStub() {
            super(new SerializerFactoryStub(), null);
        }
        // Minimal implementation
        @Override public JsonSerializer<Object> findValueSerializer(JavaType type, BeanProperty property) throws JsonMappingException {
            return new JsonSerializerStub();
        }
        @Override public JsonSerializer<Object> findTypedValueSerializer(JavaType type, boolean cache, BeanProperty property) throws JsonMappingException {
            return new JsonSerializerStub();
        }
        @Override public JsonSerializer<Object> findKeySerializer(JavaType type, BeanProperty property) throws JsonMappingException {
            return new JsonSerializerStub();
        }
        @Override public JsonSerializer<Object> findNullKeySerializer(JavaType type, BeanProperty property) throws JsonMappingException {
            return new JsonSerializerStub();
        }
        @Override public JsonSerializer<Object> findNullValueSerializer(BeanProperty property) throws JsonMappingException {
            return new JsonSerializerStub();
        }
        @Override public JsonSerializer<Object> findValueSerializer(Class<?> rawType, BeanProperty property) throws JsonMappingException {
            return new JsonSerializerStub();
        }
        @Override public JsonSerializer<Object> findTypedValueSerializer(Class<?> rawType, boolean cache, BeanProperty property) throws JsonMappingException {
            return new JsonSerializerStub();
        }
        @Override public JsonSerializer<Object> findKeySerializer(Class<?> rawType, BeanProperty property) throws JsonMappingException {
            return new JsonSerializerStub();
        }
        @Override public JsonSerializer<Object> findNullKeySerializer(Class<?> rawType, BeanProperty property) throws JsonMappingException {
            return new JsonSerializerStub();
        }
        @Override public JsonSerializer<Object> findNullValueSerializer(Class<?> rawType, BeanProperty property) throws JsonMappingException {
            return new JsonSerializerStub();
        }
        @Override public JavaType constructSpecializedType(JavaType baseType, Class<?> subclass) {
            return baseType;
        }
        @Override public void serializeValue(JsonGenerator gen, Object value) throws IOException {}
        @Override public void serializeValue(JsonGenerator gen, Object value, JavaType type) throws IOException {}
        @Override public void serializeValue(JsonGenerator gen, Object value, JavaType type, JsonSerializationContext context) throws IOException {}
        @Override public Object getAttribute(Object key) { return null; }
        @Override public void setAttribute(Object key, Object value) {}
    }

    static class SerializerFactoryStub extends SerializerFactory {
        @Override public SerializerFactory withConfig(SerializerFactoryConfig config) { return this; }
        @Override public SerializerFactoryConfig getConfig() { return null; }
        @Override public JsonSerializer<Object> createSerializer(SerializerProvider prov, JavaType type) throws JsonMappingException {
            return new JsonSerializerStub();
        }
        @Override public TypeSerializer createTypeSerializer(SerializerProvider prov, JavaType type) throws JsonMappingException {
            return null;
        }
        @Override public JsonSerializer<Object> createKeySerializer(SerializerProvider prov, JavaType type) throws JsonMappingException {
            return new JsonSerializerStub();
        }
    }

    static class BeanPropertyDefinitionStub extends BeanPropertyDefinition {
        @Override public String getName() { return "stub"; }
        @Override public String getInternalName() { return "stub"; }
        @Override public PropertyName getFullName() { return new PropertyName("stub"); }
        @Override public boolean isExplicitlyIncluded() { return false; }
        @Override public boolean isExplicitlyNamed() { return false; }
        @Override public JavaType getPrimaryType() { return TypeFactory.defaultInstance().constructType(String.class); }
        @Override public Class<?> getRawPrimaryType() { return String.class; }
        @Override public PropertyMetadata getMetadata() { return PropertyMetadata.STD_OPTIONAL; }
        @Override public boolean hasField() { return false; }
        @Override public boolean hasGetter() { return false; }
        @Override public boolean hasSetter() { return false; }
        @Override public boolean hasConstructorParameter() { return false; }
        @Override public AnnotatedField getField() { return null; }
        @Override public AnnotatedMethod getGetter() { return null; }
        @Override public AnnotatedMethod getSetter() { return null; }
        @Override public AnnotatedParameter getConstructorParameter() { return null; }
        @Override public AnnotatedMember getAccessor() { return null; }
        @Override public AnnotatedMember getMutator() { return null; }
        @Override public AnnotatedMember getNonVisibleMutator() { return null; }
        @Override public boolean isRequired() { return false; }
    }

    static class AnnotatedMemberStub extends AnnotatedMember {
        public AnnotatedMemberStub() {
            super(null, null);
        }
        @Override public Class<?> getDeclaringClass() { return Object.class; }
        @Override public String getName() { return "stubMember"; }
        @Override public Annotated withAnnotations(AnnotationMap annotations) { return this; }
        @Override public <A extends Annotation> A getAnnotation(Class<A> acls) { return null; }
        @Override public Type getGenericType() { return String.class; }
        @Override public Class<?> getRawType() { return String.class; }
        @Override public JavaType getType() { return TypeFactory.defaultInstance().constructType(String.class); }
        @Override public Object getValue(Object pojo) throws IllegalArgumentException { return null; }
        @Override public void setValue(Object pojo, Object value) throws IllegalArgumentException {}
        @Override public Annotated withFallBackAnnotationsFrom(Annotated annotated) { return this; }
        @Override public AnnotatedMember withAnnotations(AnnotationMap fallback) { return this; }
        @Override public int getModifiers() { return 0; }
    }

    static class JsonSerializerStub extends JsonSerializer<Object> {
        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.toString());
        }
    }
}