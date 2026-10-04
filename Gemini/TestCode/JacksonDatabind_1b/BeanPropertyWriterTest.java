package com.fasterxml.jackson.databind.ser;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.impl.PropertySerializerMap;
import com.fasterxml.jackson.databind.ser.impl.UnwrappingBeanPropertyWriter;
import com.fasterxml.jackson.databind.util.Annotations;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.fasterxml.jackson.databind.util.SimpleBeanPropertyDefinition;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

import static org.junit.Assert.*;

public class BeanPropertyWriterTest {

    @Retention(RetentionPolicy.RUNTIME)
    @interface CustomAnnotation {
        String value() default "";
    }

    static class DummyBean {
        @CustomAnnotation("testField")
        public String field = "testValue";

        public String getGetterProperty() {
            return "getterValue";
        }

        public String throwingGetter() {
            throw new RuntimeException("getter exception");
        }
    }

    static class NullBean {
        public String nullField = null;
    }

    private ObjectMapper objectMapper;
    private SerializerProvider serializerProvider;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        serializerProvider = objectMapper.getSerializerProviderInstance();
    }

    private BeanPropertyWriter createWriter(Class<?> cls, String fieldName, JavaType type) throws Exception {
        Field f = cls.getDeclaredField(fieldName);
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(cls, objectMapper.getDeserializationConfig());
        AnnotationMap am = new AnnotationMap();
        for (java.lang.annotation.Annotation ann : f.getDeclaredAnnotations()) {
            am.add(ann);
        }
        AnnotatedField af = new AnnotatedField(ac, f, am);
        SimpleBeanPropertyDefinition propDef = SimpleBeanPropertyDefinition.construct(
                objectMapper.getSerializationConfig(), af, new PropertyName(fieldName));

        return new BeanPropertyWriter(
                propDef,
                af,
                am,
                type,
                null,
                null,
                type,
                false,
                null
        );
    }

    private BeanPropertyWriter createMethodWriter(Class<?> cls, String methodName, String propName, JavaType type) throws Exception {
        Method m = cls.getDeclaredMethod(methodName);
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(cls, objectMapper.getDeserializationConfig());
        AnnotationMap am = new AnnotationMap();
        for (java.lang.annotation.Annotation ann : m.getDeclaredAnnotations()) {
            am.add(ann);
        }
        AnnotatedMethod ameth = new AnnotatedMethod(ac, m, am, null);
        SimpleBeanPropertyDefinition propDef = SimpleBeanPropertyDefinition.construct(
                objectMapper.getSerializationConfig(), ameth, new PropertyName(propName));

        return new BeanPropertyWriter(
                propDef,
                ameth,
                am,
                type,
                null,
                null,
                type,
                false,
                null
        );
    }

    @Test
    public void testBasicPropertiesAndAccessors() throws Exception {
        JavaType type = objectMapper.constructType(String.class);
        BeanPropertyWriter bpw = createWriter(DummyBean.class, "field", type);

        assertEquals("field", bpw.getName());
        assertEquals(new PropertyName("field"), bpw.getFullName());
        assertEquals(new SerializedString("field"), bpw.getSerializedName());
        assertEquals(type, bpw.getType());
        assertNull(bpw.getWrapperName());
        assertNotNull(bpw.getMember());
        assertFalse(bpw.isUnwrapping());
        assertFalse(bpw.willSuppressNulls());
        assertNull(bpw.getSerializer());
        assertNull(bpw.getNullSerializer());
        assertNull(bpw.getTypeSerializer());
        assertFalse(bpw.hasSerializer());
        assertFalse(bpw.hasNullSerializer());
        assertNotNull(bpw.toString());

        CustomAnnotation ann = bpw.getAnnotation(CustomAnnotation.class);
        assertNotNull(ann);
        assertEquals("testField", ann.value());
        assertNull(bpw.getContextAnnotation(Deprecated.class));
    }

    @Test
    public void testGetFieldValue() throws Exception {
        JavaType type = objectMapper.constructType(String.class);
        BeanPropertyWriter bpw = createWriter(DummyBean.class, "field", type);
        DummyBean bean = new DummyBean();

        Object value = bpw.get(bean);
        assertEquals("testValue", value);
    }

    @Test
    public void testGetMethodValue() throws Exception {
        JavaType type = objectMapper.constructType(String.class);
        BeanPropertyWriter bpw = createMethodWriter(DummyBean.class, "getGetterProperty", "getterProperty", type);
        DummyBean bean = new DummyBean();

        Object value = bpw.get(bean);
        assertEquals("getterValue", value);
    }

    @Test
    public void testGetExceptionPropagation() throws Exception {
        JavaType type = objectMapper.constructType(String.class);
        BeanPropertyWriter bpw = createMethodWriter(DummyBean.class, "throwingGetter", "throwingGetter", type);
        DummyBean bean = new DummyBean();

        try {
            bpw.get(bean);
            fail("Expected exception not thrown");
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException || e.getCause() instanceof RuntimeException);
        }
    }

    @Test
    public void testAssignSerializerAndNullSerializer() throws Exception {
        JavaType type = objectMapper.constructType(String.class);
        BeanPropertyWriter bpw = createWriter(DummyBean.class, "field", type);

        JsonSerializer<Object> ser = objectMapper.findValueSerializer(String.class, bpw);
        bpw.assignSerializer(ser);
        assertTrue(bpw.hasSerializer());
        assertSame(ser, bpw.getSerializer());

        JsonSerializer<Object> nullSer = objectMapper.findNullValueSerializer(bpw);
        bpw.assignNullSerializer(nullSer);
        assertTrue(bpw.hasNullSerializer());
        assertSame(nullSer, bpw.getNullSerializer());

        // Assigning again with same or different
        bpw.assignSerializer(ser);
        assertSame(ser, bpw.getSerializer());
    }

    @Test
    public void testInternalSettings() throws Exception {
        JavaType type = objectMapper.constructType(String.class);
        BeanPropertyWriter bpw = createWriter(DummyBean.class, "field", type);

        String key = "testKey";
        assertNull(bpw.getInternalSetting(key));

        bpw.setInternalSetting(key, "testValue");
        assertEquals("testValue", bpw.getInternalSetting(key));

        Object removed = bpw.removeInternalSetting(key);
        assertEquals("testValue", removed);
        assertNull(bpw.getInternalSetting(key));

        assertNull(bpw.removeInternalSetting("nonExistent"));
    }

    @Test
    public void testRenameAndUnwrapping() throws Exception {
        JavaType type = objectMapper.constructType(String.class);
        BeanPropertyWriter bpw = createWriter(DummyBean.class, "field", type);

        BeanPropertyWriter renamed = bpw.rename(new PropertyName("renamedField"));
        assertEquals("renamedField", renamed.getName());
        assertEquals(new PropertyName("renamedField"), renamed.getFullName());

        BeanPropertyWriter unwrapped = bpw.unwrappingWriter(NameTransformer.NOP);
        assertNotNull(unwrapped);
        assertTrue(unwrapped.isUnwrapping());
        assertTrue(unwrapped instanceof UnwrappingBeanPropertyWriter);
    }

    @Test
    public void testWouldConflictWithName() throws Exception {
        JavaType type = objectMapper.constructType(String.class);
        BeanPropertyWriter bpw = createWriter(DummyBean.class, "field", type);

        assertTrue(bpw.wouldConflictWithName(new PropertyName("field")));
        assertFalse(bpw.wouldConflictWithName(new PropertyName("other")));
    }

    @Test
    public void testSerializeAsFieldSuccess() throws Exception {
        JavaType type = objectMapper.constructType(String.class);
        BeanPropertyWriter bpw = createWriter(DummyBean.class, "field", type);
        bpw.assignSerializer(objectMapper.findValueSerializer(String.class, bpw));

        DummyBean bean = new DummyBean();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = objectMapper.getFactory().createGenerator(out);

        gen.writeStartObject();
        bpw.serializeAsField(bean, gen, serializerProvider);
        gen.writeEndObject();
        gen.close();

        String json = out.toString("UTF-8");
        assertEquals("{\"field\":\"testValue\"}", json);
    }

    @Test
    public void testSerializeAsFieldWithNullValue() throws Exception {
        JavaType type = objectMapper.constructType(String.class);
        BeanPropertyWriter bpw = createWriter(NullBean.class, "nullField", type);

        NullBean bean = new NullBean();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = objectMapper.getFactory().createGenerator(out);

        gen.writeStartObject();
        bpw.serializeAsField(bean, gen, serializerProvider);
        gen.writeEndObject();
        gen.close();

        String json = out.toString("UTF-8");
        assertEquals("{\"nullField\":null}", json);
    }

    @Test
    public void testSerializeAsFieldSuppressNull() throws Exception {
        JavaType type = objectMapper.constructType(String.class);
        Field f = NullBean.class.getDeclaredField("nullField");
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(NullBean.class, objectMapper.getDeserializationConfig());
        AnnotationMap am = new AnnotationMap();
        AnnotatedField af = new AnnotatedField(ac, f, am);
        SimpleBeanPropertyDefinition propDef = SimpleBeanPropertyDefinition.construct(
                objectMapper.getSerializationConfig(), af, new PropertyName("nullField"));

        BeanPropertyWriter bpw = new BeanPropertyWriter(
                propDef,
                af,
                am,
                type,
                null,
                null,
                type,
                true, // suppressNulls = true
                BeanPropertyWriter.MARKER_FOR_EMPTY
        );

        NullBean bean = new NullBean();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = objectMapper.getFactory().createGenerator(out);

        gen.writeStartObject();
        bpw.serializeAsField(bean, gen, serializerProvider);
        gen.writeEndObject();
        gen.close();

        String json = out.toString("UTF-8");
        assertEquals("{}", json);
    }

    @Test
    public void testSerializeAsOmitAttribute() throws Exception {
        JavaType type = objectMapper.constructType(String.class);
        BeanPropertyWriter bpw = createWriter(DummyBean.class, "field", type);
        DummyBean bean = new DummyBean();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = objectMapper.getFactory().createGenerator(out);
        gen.writeStartObject();
        bpw.serializeAsOmitAttribute(bean, gen, serializerProvider);
        gen.writeEndObject();
        gen.close();

        String json = out.toString("UTF-8");
        assertEquals("{}", json);
    }

    @Test
    public void testSerializeAsPlaceholder() throws Exception {
        JavaType type = objectMapper.constructType(String.class);
        BeanPropertyWriter bpw = createWriter(DummyBean.class, "field", type);
        DummyBean bean = new DummyBean();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = objectMapper.getFactory().createGenerator(out);
        gen.writeStartArray();
        bpw.serializeAsPlaceholder(bean, gen, serializerProvider);
        gen.writeEndArray();
        gen.close();

        String json = out.toString("UTF-8");
        assertEquals("[null]", json);
    }

    @Test
    public void testSerializeAsElement() throws Exception {
        JavaType type = objectMapper.constructType(String.class);
        BeanPropertyWriter bpw = createWriter(DummyBean.class, "field", type);
        bpw.assignSerializer(objectMapper.findValueSerializer(String.class, bpw));
        DummyBean bean = new DummyBean();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = objectMapper.getFactory().createGenerator(out);
        gen.writeStartArray();
        bpw.serializeAsElement(bean, gen, serializerProvider);
        gen.writeEndArray();
        gen.close();

        String json = out.toString("UTF-8");
        assertEquals("[\"testValue\"]", json);
    }

    @Test
    public void testDepositSchemaPropertyObjectNode() throws Exception {
        JavaType type = objectMapper.constructType(String.class);
        BeanPropertyWriter bpw = createWriter(DummyBean.class, "field", type);
        bpw.assignSerializer(objectMapper.findValueSerializer(String.class, bpw));

        ObjectNode propertiesNode = JsonNodeFactory.instance.objectNode();
        bpw.depositSchemaProperty(propertiesNode, serializerProvider);

        assertTrue(propertiesNode.has("field"));
        assertNotNull(propertiesNode.get("field"));
    }

    @Test
    public void testDepositSchemaPropertyVisitor() throws Exception {
        JavaType type = objectMapper.constructType(String.class);
        BeanPropertyWriter bpw = createWriter(DummyBean.class, "field", type);
        bpw.assignSerializer(objectMapper.findValueSerializer(String.class, bpw));

        JsonObjectFormatVisitor.Base visitor = new JsonObjectFormatVisitor.Base(serializerProvider);
        bpw.depositSchemaProperty(visitor, serializerProvider);
    }

    @Test
    public void testFixAccess() throws Exception {
        JavaType type = objectMapper.constructType(String.class);
        BeanPropertyWriter bpw = createWriter(DummyBean.class, "field", type);
        bpw.fixAccess(objectMapper.getSerializationConfig());
    }

    @Test
    public void testViewsConfiguration() throws Exception {
        JavaType type = objectMapper.constructType(String.class);
        Field f = DummyBean.class.getDeclaredField("field");
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(DummyBean.class, objectMapper.getDeserializationConfig());
        AnnotationMap am = new AnnotationMap();
        AnnotatedField af = new AnnotatedField(ac, f, am);
        SimpleBeanPropertyDefinition propDef = SimpleBeanPropertyDefinition.construct(
                objectMapper.getSerializationConfig(), af, new PropertyName("field"));

        BeanPropertyWriter bpw = new BeanPropertyWriter(
                propDef,
                af,
                am,
                type,
                null,
                null,
                type,
                false,
                null
        );

        Class<?>[] views = bpw.getViews();
        assertNull(views);
    }
}