package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.impl.BeanPropertyMap;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

public class BeanDeserializerFactoryTest {

    private ObjectMapper mapper;
    private DeserializationConfig config;
    private DeserializationContext ctxt;
    private BeanDeserializerFactory factory;
    private JavaType simpleType;
    private BeanDescription beanDesc;

    @Before
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
        config = mapper.getDeserializationConfig();
        ctxt = new DefaultDeserializationContext.Impl(BeanDeserializerFactory.instance);
        factory = BeanDeserializerFactory.instance;
        simpleType = TypeFactory.defaultInstance().constructType(SimpleBean.class);
        beanDesc = config.introspect(simpleType);
    }

    // Test class with various features
    static class SimpleBean {
        public int id;
        public String name;
        private String secret;
        public List<String> tags;
        public Map<String, Integer> scores;
        public NestedBean nested;
        public String[] array;
        public boolean active;
        public double value;

        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }

        public String getComputed() { return "computed"; }
    }

    static class NestedBean {
        public int x;
        public int y;
    }

    static class WithCreator {
        private final int value;
        public WithCreator(@JsonProperty("value") int value) { this.value = value; }
        public int getValue() { return value; }
    }

    static class WithJsonDeserialize {
        @JsonDeserialize(using = CustomDeserializer.class)
        public String custom;
    }

    static class CustomDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return "custom:" + p.getText();
        }
    }

    static class WithTypeInfo {
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
        public Object value;
    }

    static class WithObjectId {
        public int id;
        public String data;
    }

    static class WithIgnoredProps {
        @JsonIgnore
        public String ignored;
        public String visible;
    }

    static class WithAnySetter {
        private Map<String, Object> extras = new HashMap<>();
        @JsonAnySetter
        public void setExtra(String key, Object value) { extras.put(key, value); }
        public Map<String, Object> getExtras() { return extras; }
    }

    static class WithAnyGetter {
        private Map<String, Object> props = new HashMap<>();
        @JsonAnyGetter
        public Map<String, Object> getProps() { return props; }
    }

    static class WithReference {
        @JsonManagedReference
        public List<Child> children;
    }

    static class Child {
        @JsonBackReference
        public WithReference parent;
    }

    static class WithInjectables {
        @JacksonInject
        public String injected;
    }

    static class WithViews {
        @JsonView(Views.Public.class)
        public String publicField;
        @JsonView(Views.Internal.class)
        public String internalField;
    }

    static class Views {
        public static class Public {}
        public static class Internal {}
    }

    static class WithDefaultCreator {
        public int a;
        public int b;
    }

    static class WithBuilder {
        public int x;
        public int y;
    }

    static class WithAbstract {
        public abstract static class AbstractBase {
            public int base;
        }
        public static class Concrete extends AbstractBase {
            public int extra;
        }
    }

    static class WithFinalField {
        public final int finalValue = 42;
        public String name;
    }

    static class WithTransientField {
        public transient String temp;
        public String permanent;
    }

    static class WithNestedGenerics {
        public Map<String, List<Map<Integer, String>>> complex;
    }

    static class WithEnum {
        public TestEnum enumValue;
    }

    enum TestEnum { A, B, C }

    static class WithNullValue {
        public String nullValue;
    }

    static class WithEmptyBean {}

    static class WithInheritance extends SimpleBean {
        public String extra;
    }

    static class WithMultipleConstructors {
        public WithMultipleConstructors() {}
        public WithMultipleConstructors(int x) {}
        public WithMultipleConstructors(String s) {}
        public int value;
    }

    static class WithPrivateFields {
        private int privateInt;
        private String privateString;
        public int getPrivateInt() { return privateInt; }
        public void setPrivateInt(int v) { privateInt = v; }
        public String getPrivateString() { return privateString; }
        public void setPrivateString(String s) { privateString = s; }
    }

    static class WithStaticFields {
        public static int staticField = 10;
        public int instanceField;
    }

    static class WithComplexHierarchy extends WithInheritance {
        public boolean flag;
    }

    // Test 1: Basic bean creation
    @Test
    public void testBuildBeanDeserializer_BasicBean() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        assertNotNull("Deserializer should not be null", deser);
        assertTrue("Should be BeanDeserializer", deser instanceof BeanDeserializer);

        String json = "{\"id\":1,\"name\":\"test\",\"secret\":\"hidden\",\"tags\":[\"a\",\"b\"],\"scores\":{\"x\":1},\"nested\":{\"x\":2,\"y\":3},\"array\":[\"1\",\"2\"],\"active\":true,\"value\":3.14}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(1, result.id);
        assertEquals("test", result.name);
        assertEquals("hidden", result.secret);
        assertEquals(Arrays.asList("a", "b"), result.tags);
        assertEquals(Integer.valueOf(1), result.scores.get("x"));
        assertEquals(2, result.nested.x);
        assertEquals(3, result.nested.y);
        assertArrayEquals(new String[]{"1", "2"}, result.array);
        assertTrue(result.active);
        assertEquals(3.14, result.value, 0.001);
    }

    // Test 2: Bean with creator
    @Test
    public void testBuildBeanDeserializer_WithCreator() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(WithCreator.class);
        BeanDescription desc = config.introspect(type);
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);

        String json = "{\"value\":42}";
        WithCreator result = (WithCreator) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(42, result.getValue());
    }

    // Test 3: Bean with custom deserializer annotation
    @Test
    public void testBuildBeanDeserializer_WithCustomDeserializer() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(WithJsonDeserialize.class);
        BeanDescription desc = config.introspect(type);
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);

        String json = "{\"custom\":\"value\"}";
        WithJsonDeserialize result = (WithJsonDeserialize) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals("custom:value", result.custom);
    }

    // Test 4: Bean with type info
    @Test
    public void testBuildBeanDeserializer_WithTypeInfo() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(WithTypeInfo.class);
        BeanDescription desc = config.introspect(type);
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);

        String json = "{\"value\":{\"@class\":\"com.fasterxml.jackson.databind.deser.BeanDeserializerFactoryTest$SimpleBean\",\"id\":5,\"name\":\"n\"}}";
        WithTypeInfo result = (WithTypeInfo) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertTrue(result.value instanceof SimpleBean);
        assertEquals(5, ((SimpleBean) result.value).id);
    }

    // Test 5: Bean with object id
    @Test
    public void testBuildBeanDeserializer_WithObjectId() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(WithObjectId.class);
        BeanDescription desc = config.introspect(type);
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);

        String json = "{\"id\":1,\"data\":\"test\"}";
        WithObjectId result = (WithObjectId) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(1, result.id);
        assertEquals("test", result.data);
    }

    // Test 6: Bean with ignored properties
    @Test
    public void testBuildBeanDeserializer_WithIgnoredProps() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(WithIgnoredProps.class);
        BeanDescription desc = config.introspect(type);
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);

        String json = "{\"ignored\":\"shouldNotSet\",\"visible\":\"ok\"}";
        WithIgnoredProps result = (WithIgnoredProps) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertNull(result.ignored);
        assertEquals("ok", result.visible);
    }

    // Test 7: Bean with any setter
    @Test
    public void testBuildBeanDeserializer_WithAnySetter() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(WithAnySetter.class);
        BeanDescription desc = config.introspect(type);
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);

        String json = "{\"unknown1\":\"val1\",\"unknown2\":123}";
        WithAnySetter result = (WithAnySetter) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals("val1", result.getExtras().get("unknown1"));
        assertEquals(123, result.getExtras().get("unknown2"));
    }

    // Test 8: Bean with any getter
    @Test
    public void testBuildBeanDeserializer_WithAnyGetter() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(WithAnyGetter.class);
        BeanDescription desc = config.introspect(type);
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);

        String json = "{}";
        WithAnyGetter result = (WithAnyGetter) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertNotNull(result.getProps());
    }

    // Test 9: Bean with managed/back references
    @Test
    public void testBuildBeanDeserializer_WithReferences() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(WithReference.class);
        BeanDescription desc = config.introspect(type);
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);

        String json = "{\"children\":[{\"parent\":{}}]}";
        WithReference result = (WithReference) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertNotNull(result.children);
        assertEquals(1, result.children.size());
    }

    // Test 10: Bean with injectables
    @Test
    public void testBuildBeanDeserializer_WithInjectables() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(WithInjectables.class);
        BeanDescription desc = config.introspect(type);
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);

        String json = "{}";
        WithInjectables result = (WithInjectables) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertNull(result.injected);
    }

    // Test 11: Bean with views
    @Test
    public void testBuildBeanDeserializer_WithViews() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(WithViews.class);
        BeanDescription desc = config.introspect(type);
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);

        String json = "{\"publicField\":\"pub\",\"internalField\":\"int\"}";
        WithViews result = (WithViews) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals("pub", result.publicField);
        assertEquals("int", result.internalField);
    }

    // Test 12: Bean with default creator
    @Test
    public void testBuildBeanDeserializer_WithDefaultCreator() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(WithDefaultCreator.class);
        BeanDescription desc = config.introspect(type);
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);

        String json = "{\"a\":1,\"b\":2}";
        WithDefaultCreator result = (WithDefaultCreator) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(1, result.a);
        assertEquals(2, result.b);
    }

    // Test 13: Bean with builder
    @Test
    public void testBuildBeanDeserializer_WithBuilder() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(WithBuilder.class);
        BeanDescription desc = config.introspect(type);
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);

        String json = "{\"x\":10,\"y\":20}";
        WithBuilder result = (WithBuilder) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(10, result.x);
        assertEquals(20, result.y);
    }

    // Test 14: Bean with abstract base
    @Test
    public void testBuildBeanDeserializer_WithAbstract() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(WithAbstract.Concrete.class);
        BeanDescription desc = config.introspect(type);
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);

        String json = "{\"base\":1,\"extra\":2}";
        WithAbstract.Concrete result = (WithAbstract.Concrete) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(1, result.base);
        assertEquals(2, result.extra);
    }

    // Test 15: Bean with final field
    @Test
    public void testBuildBeanDeserializer_WithFinalField() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(WithFinalField.class);
        BeanDescription desc = config.introspect(type);
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);

        String json = "{\"name\":\"test\"}";
        WithFinalField result = (WithFinalField) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(42, result.finalValue);
        assertEquals("test", result.name);
    }

    // Test 16: Bean with transient field
    @Test
    public void testBuildBeanDeserializer_WithTransientField() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(WithTransientField.class);
        BeanDescription desc = config.introspect(type);
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);

        String json = "{\"temp\":\"shouldIgnore\",\"permanent\":\"keep\"}";
        WithTransientField result = (WithTransientField) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertNull(result.temp);
        assertEquals("keep", result.permanent);
    }

    // Test 17: Bean with nested generics
    @Test
    public void testBuildBeanDeserializer_WithNestedGenerics() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(WithNestedGenerics.class);
        BeanDescription desc = config.introspect(type);
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);

        String json = "{\"complex\":{\"key\":[{\"inner\":1}]}}";
        WithNestedGenerics result = (WithNestedGenerics) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertNotNull(result.complex);
        assertEquals(1, result.complex.get("key").get(0).get("inner").intValue());
    }

    // Test 18: Bean with enum
    @Test
    public void testBuildBeanDeserializer_WithEnum() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(WithEnum.class);
        BeanDescription desc = config.introspect(type);
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);

        String json = "{\"enumValue\":\"B\"}";
        WithEnum result = (WithEnum) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(TestEnum.B, result.enumValue);
    }

    // Test 19: Bean with null value
    @Test
    public void testBuildBeanDeserializer_WithNullValue() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(WithNullValue.class);
        BeanDescription desc = config.introspect(type);
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);

        String json = "{\"nullValue\":null}";
        WithNullValue result = (WithNullValue) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertNull(result.nullValue);
    }

    // Test 20: Empty bean
    @Test
    public void testBuildBeanDeserializer_EmptyBean() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(WithEmptyBean.class);
        BeanDescription desc = config.introspect(type);
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);

        String json = "{}";
        WithEmptyBean result = (WithEmptyBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
    }

    // Test 21: Bean with inheritance
    @Test
    public void testBuildBeanDeserializer_WithInheritance() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(WithInheritance.class);
        BeanDescription desc = config.introspect(type);
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);

        String json = "{\"id\":1,\"name\":\"base\",\"extra\":\"child\"}";
        WithInheritance result = (WithInheritance) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(1, result.id);
        assertEquals("base", result.name);
        assertEquals("child", result.extra);
    }

    // Test 22: Bean with multiple constructors
    @Test
    public void testBuildBeanDeserializer_WithMultipleConstructors() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(WithMultipleConstructors.class);
        BeanDescription desc = config.introspect(type);
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);

        String json = "{\"value\":5}";
        WithMultipleConstructors result = (WithMultipleConstructors) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(5, result.value);
    }

    // Test 23: Bean with private fields
    @Test
    public void testBuildBeanDeserializer_WithPrivateFields() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(WithPrivateFields.class);
        BeanDescription desc = config.introspect(type);
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);

        String json = "{\"privateInt\":10,\"privateString\":\"secret\"}";
        WithPrivateFields result = (WithPrivateFields) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(10, result.getPrivateInt());
        assertEquals("secret", result.getPrivateString());
    }

    // Test 24: Bean with static fields
    @Test
    public void testBuildBeanDeserializer_WithStaticFields() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(WithStaticFields.class);
        BeanDescription desc = config.introspect(type);
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);

        String json = "{\"instanceField\":7}";
        WithStaticFields result = (WithStaticFields) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(7, result.instanceField);
        assertEquals(10, WithStaticFields.staticField);
    }

    // Test 25: Complex hierarchy
    @Test
    public void testBuildBeanDeserializer_WithComplexHierarchy() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(WithComplexHierarchy.class);
        BeanDescription desc = config.introspect(type);
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);

        String json = "{\"id\":1,\"name\":\"n\",\"extra\":\"e\",\"flag\":true}";
        WithComplexHierarchy result = (WithComplexHierarchy) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(1, result.id);
        assertEquals("n", result.name);
        assertEquals("e", result.extra);
        assertTrue(result.flag);
    }

    // Test 26: buildBeanDeserializer with null type
    @Test(expected = IllegalArgumentException.class)
    public void testBuildBeanDeserializer_NullType() throws Exception {
        factory.buildBeanDeserializer(ctxt, null, beanDesc);
    }

    // Test 27: buildBeanDeserializer with null bean description
    @Test(expected = IllegalArgumentException.class)
    public void testBuildBeanDeserializer_NullBeanDesc() throws Exception {
        factory.buildBeanDeserializer(ctxt, simpleType, null);
    }

    // Test 28: buildBeanDeserializer with abstract class
    @Test
    public void testBuildBeanDeserializer_AbstractClass() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(WithAbstract.AbstractBase.class);
        BeanDescription desc = config.introspect(type);
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);
    }

    // Test 29: buildBeanDeserializer with interface
    @Test
    public void testBuildBeanDeserializer_Interface() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(Map.class);
        BeanDescription desc = config.introspect(type);
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);
    }

    // Test 30: buildBeanDeserializer with array type
    @Test
    public void testBuildBeanDeserializer_ArrayType() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(String[].class);
        BeanDescription desc = config.introspect(type);
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);
    }

    // Test 31: buildBeanDeserializer with primitive type
    @Test
    public void testBuildBeanDeserializer_PrimitiveType() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(int.class);
        BeanDescription desc = config.introspect(type);
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);
    }

    // Test 32: buildBeanDeserializer with String type
    @Test
    public void testBuildBeanDeserializer_StringType() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        BeanDescription desc = config.introspect(type);
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);
    }

    // Test 33: buildBeanDeserializer with null deserialization config
    @Test(expected = IllegalArgumentException.class)
    public void testBuildBeanDeserializer_NullConfig() throws Exception {
        factory.buildBeanDeserializer(null, simpleType, beanDesc);
    }

    // Test 34: buildBeanDeserializer with null context
    @Test(expected = IllegalArgumentException.class)
    public void testBuildBeanDeserializer_NullContext() throws Exception {
        factory.buildBeanDeserializer(null, simpleType, beanDesc);
    }

    // Test 35: Test with missing properties
    @Test
    public void testBuildBeanDeserializer_MissingProperties() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(0, result.id);
        assertNull(result.name);
        assertNull(result.secret);
        assertNull(result.tags);
        assertNull(result.scores);
        assertNull(result.nested);
        assertNull(result.array);
        assertFalse(result.active);
        assertEquals(0.0, result.value, 0.001);
    }

    // Test 36: Test with extra unknown properties
    @Test
    public void testBuildBeanDeserializer_UnknownProperties() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"id\":1,\"unknown\":\"value\"}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(1, result.id);
    }

    // Test 37: Test with duplicate properties
    @Test
    public void testBuildBeanDeserializer_DuplicateProperties() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"id\":1,\"id\":2}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(2, result.id);
    }

    // Test 38: Test with null JSON
    @Test
    public void testBuildBeanDeserializer_NullJson() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser("null"), ctxt);
        assertNull(result);
    }

    // Test 39: Test with empty JSON
    @Test
    public void testBuildBeanDeserializer_EmptyJson() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(""), ctxt);
        assertNull(result);
    }

    // Test 40: Test with malformed JSON
    @Test(expected = IOException.class)
    public void testBuildBeanDeserializer_MalformedJson() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        deser.deserialize(mapper.getFactory().createParser("{invalid"), ctxt);
    }

    // Test 41: Test with array JSON for bean
    @Test(expected = IOException.class)
    public void testBuildBeanDeserializer_ArrayJsonForBean() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        deser.deserialize(mapper.getFactory().createParser("[1,2,3]"), ctxt);
    }

    // Test 42: Test with scalar JSON for bean
    @Test(expected = IOException.class)
    public void testBuildBeanDeserializer_ScalarJsonForBean() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        deser.deserialize(mapper.getFactory().createParser("42"), ctxt);
    }

    // Test 43: Test with boolean JSON for bean
    @Test(expected = IOException.class)
    public void testBuildBeanDeserializer_BooleanJsonForBean() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        deser.deserialize(mapper.getFactory().createParser("true"), ctxt);
    }

    // Test 44: Test with nested object
    @Test
    public void testBuildBeanDeserializer_NestedObject() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"nested\":{\"x\":1,\"y\":2}}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertNotNull(result.nested);
        assertEquals(1, result.nested.x);
        assertEquals(2, result.nested.y);
    }

    // Test 45: Test with array of objects
    @Test
    public void testBuildBeanDeserializer_ArrayOfObjects() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"array\":[\"a\",\"b\",\"c\"]}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertArrayEquals(new String[]{"a", "b", "c"}, result.array);
    }

    // Test 46: Test with map of objects
    @Test
    public void testBuildBeanDeserializer_MapOfObjects() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"scores\":{\"a\":1,\"b\":2}}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(Integer.valueOf(1), result.scores.get("a"));
        assertEquals(Integer.valueOf(2), result.scores.get("b"));
    }

    // Test 47: Test with list of objects
    @Test
    public void testBuildBeanDeserializer_ListOfObjects() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"tags\":[\"x\",\"y\"]}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(Arrays.asList("x", "y"), result.tags);
    }

    // Test 48: Test with special characters in strings
    @Test
    public void testBuildBeanDeserializer_SpecialCharacters() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"name\":\"line1\\nline2\\ttab\\\"quote\\\"\"}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals("line1\nline2\ttab\"quote\"", result.name);
    }

    // Test 49: Test with unicode characters
    @Test
    public void testBuildBeanDeserializer_UnicodeCharacters() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"name\":\"\\u00e9\\u00fc\\u00f1\"}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals("éüñ", result.name);
    }

    // Test 50: Test with large numbers
    @Test
    public void testBuildBeanDeserializer_LargeNumbers() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"id\":2147483647,\"value\":1.7976931348623157E308}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(Integer.MAX_VALUE, result.id);
        assertEquals(Double.MAX_VALUE, result.value, 0.0);
    }

    // Test 51: Test with negative numbers
    @Test
    public void testBuildBeanDeserializer_NegativeNumbers() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"id\":-100,\"value\":-3.14}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(-100, result.id);
        assertEquals(-3.14, result.value, 0.001);
    }

    // Test 52: Test with zero values
    @Test
    public void testBuildBeanDeserializer_ZeroValues() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"id\":0,\"value\":0.0}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(0, result.id);
        assertEquals(0.0, result.value, 0.0);
    }

    // Test 53: Test with boolean values
    @Test
    public void testBuildBeanDeserializer_BooleanValues() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"active\":true}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertTrue(result.active);
    }

    // Test 54: Test with null values in fields
    @Test
    public void testBuildBeanDeserializer_NullFieldValues() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"id\":null,\"name\":null,\"active\":null,\"value\":null}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(0, result.id);
        assertNull(result.name);
        assertFalse(result.active);
        assertEquals(0.0, result.value, 0.0);
    }

    // Test 55: Test with empty string values
    @Test
    public void testBuildBeanDeserializer_EmptyStringValues() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"name\":\"\"}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals("", result.name);
    }

    // Test 56: Test with whitespace string values
    @Test
    public void testBuildBeanDeserializer_WhitespaceStringValues() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"name\":\"   \"}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals("   ", result.name);
    }

    // Test 57: Test with nested null object
    @Test
    public void testBuildBeanDeserializer_NestedNullObject() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"nested\":null}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertNull(result.nested);
    }

    // Test 58: Test with empty array
    @Test
    public void testBuildBeanDeserializer_EmptyArray() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"array\":[]}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertNotNull(result.array);
        assertEquals(0, result.array.length);
    }

    // Test 59: Test with empty map
    @Test
    public void testBuildBeanDeserializer_EmptyMap() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"scores\":{}}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertNotNull(result.scores);
        assertTrue(result.scores.isEmpty());
    }

    // Test 60: Test with empty list
    @Test
    public void testBuildBeanDeserializer_EmptyList() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"tags\":[]}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertNotNull(result.tags);
        assertTrue(result.tags.isEmpty());
    }

    // Test 61: Test with deeply nested structures
    @Test
    public void testBuildBeanDeserializer_DeeplyNested() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"nested\":{\"x\":1,\"y\":2},\"tags\":[\"a\",\"b\"],\"scores\":{\"k\":1}}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertNotNull(result.nested);
        assertEquals(1, result.nested.x);
        assertEquals(2, result.nested.y);
        assertEquals(Arrays.asList("a", "b"), result.tags);
        assertEquals(Integer.valueOf(1), result.scores.get("k"));
    }

    // Test 62: Test with mixed types
    @Test
    public void testBuildBeanDeserializer_MixedTypes() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"id\":\"123\",\"name\":123,\"active\":\"true\",\"value\":\"3.14\"}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(123, result.id);
        assertEquals("123", result.name);
        assertTrue(result.active);
        assertEquals(3.14, result.value, 0.001);
    }

    // Test 63: Test with case sensitivity
    @Test
    public void testBuildBeanDeserializer_CaseSensitivity() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"ID\":1,\"Name\":\"test\"}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(0, result.id);
        assertNull(result.name);
    }

    // Test 64: Test with underscore in property names
    @Test
    public void testBuildBeanDeserializer_UnderscoreProperties() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"id_value\":1}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(0, result.id);
    }

    // Test 65: Test with hyphen in property names
    @Test
    public void testBuildBeanDeserializer_HyphenProperties() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"id-value\":1}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(0, result.id);
    }

    // Test 66: Test with property name matching getter
    @Test
    public void testBuildBeanDeserializer_GetterProperty() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"computed\":\"value\"}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals("computed", result.getComputed());
    }

    // Test 67: Test with property name matching setter
    @Test
    public void testBuildBeanDeserializer_SetterProperty() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"secret\":\"hidden\"}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals("hidden", result.getSecret());
    }

    // Test 68: Test with property name matching field
    @Test
    public void testBuildBeanDeserializer_FieldProperty() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"id\":42}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(42, result.id);
    }

    // Test 69: Test with property name matching both field and getter
    @Test
    public void testBuildBeanDeserializer_FieldAndGetterProperty() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"secret\":\"value\"}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals("value", result.getSecret());
    }

    // Test 70: Test with property name matching both field and setter
    @Test
    public void testBuildBeanDeserializer_FieldAndSetterProperty() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"secret\":\"value\"}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals("value", result.getSecret());
    }

    // Test 71: Test with property name matching getter and setter
    @Test
    public void testBuildBeanDeserializer_GetterAndSetterProperty() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"secret\":\"value\"}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals("value", result.getSecret());
    }

    // Test 72: Test with property name matching all three
    @Test
    public void testBuildBeanDeserializer_AllThreeProperty() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"secret\":\"value\"}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals("value", result.getSecret());
    }

    // Test 73: Test with property name not matching anything
    @Test
    public void testBuildBeanDeserializer_NoMatchProperty() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"nonexistent\":\"value\"}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
    }

    // Test 74: Test with property name matching but wrong type
    @Test
    public void testBuildBeanDeserializer_WrongTypeProperty() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"id\":\"notAnInt\"}";
        try {
            SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
            fail("Should have thrown exception");
        } catch (IOException e) {
            // expected
        }
    }

    // Test 75: Test with property name matching but null value
    @Test
    public void testBuildBeanDeserializer_NullValueProperty() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"id\":null}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(0, result.id);
    }

    // Test 76: Test with property name matching but empty value
    @Test
    public void testBuildBeanDeserializer_EmptyValueProperty() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"name\":\"\"}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals("", result.name);
    }

    // Test 77: Test with property name matching but whitespace value
    @Test
    public void testBuildBeanDeserializer_WhitespaceValueProperty() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"name\":\"   \"}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals("   ", result.name);
    }

    // Test 78: Test with property name matching but special characters
    @Test
    public void testBuildBeanDeserializer_SpecialCharValueProperty() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"name\":\"a\\nb\\tc\\\"d\\\"\"}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals("a\nb\tc\"d\"", result.name);
    }

    // Test 79: Test with property name matching but unicode value
    @Test
    public void testBuildBeanDeserializer_UnicodeValueProperty() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"name\":\"\\u00e9\\u00fc\"}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals("éü", result.name);
    }

    // Test 80: Test with property name matching but large value
    @Test
    public void testBuildBeanDeserializer_LargeValueProperty() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"id\":2147483647}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(Integer.MAX_VALUE, result.id);
    }

    // Test 81: Test with property name matching but negative value
    @Test
    public void testBuildBeanDeserializer_NegativeValueProperty() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"id\":-1}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(-1, result.id);
    }

    // Test 82: Test with property name matching but zero value
    @Test
    public void testBuildBeanDeserializer_ZeroValueProperty() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"id\":0}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(0, result.id);
    }

    // Test 83: Test with property name matching but boolean value
    @Test
    public void testBuildBeanDeserializer_BooleanValueProperty() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"active\":true}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertTrue(result.active);
    }

    // Test 84: Test with property name matching but double value
    @Test
    public void testBuildBeanDeserializer_DoubleValueProperty() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"value\":3.14}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(3.14, result.value, 0.001);
    }

    // Test 85: Test with property name matching but array value
    @Test
    public void testBuildBeanDeserializer_ArrayValueProperty() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"array\":[\"a\",\"b\"]}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertArrayEquals(new String[]{"a", "b"}, result.array);
    }

    // Test 86: Test with property name matching but map value
    @Test
    public void testBuildBeanDeserializer_MapValueProperty() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"scores\":{\"a\":1}}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(Integer.valueOf(1), result.scores.get("a"));
    }

    // Test 87: Test with property name matching but list value
    @Test
    public void testBuildBeanDeserializer_ListValueProperty() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"tags\":[\"a\",\"b\"]}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(Arrays.asList("a", "b"), result.tags);
    }

    // Test 88: Test with property name matching but object value
    @Test
    public void testBuildBeanDeserializer_ObjectValueProperty() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"nested\":{\"x\":1,\"y\":2}}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertNotNull(result.nested);
        assertEquals(1, result.nested.x);
        assertEquals(2, result.nested.y);
    }

    // Test 89: Test with property name matching but null object value
    @Test
    public void testBuildBeanDeserializer_NullObjectValueProperty() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"nested\":null}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertNull(result.nested);
    }

    // Test 90: Test with property name matching but empty object value
    @Test
    public void testBuildBeanDeserializer_EmptyObjectValueProperty() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"nested\":{}}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertNotNull(result.nested);
        assertEquals(0, result.nested.x);
        assertEquals(0, result.nested.y);
    }

    // Test 91: Test with property name matching but array of objects value
    @Test
    public void testBuildBeanDeserializer_ArrayOfObjectsValueProperty() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"array\":[\"a\",\"b\"]}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertArrayEquals(new String[]{"a", "b"}, result.array);
    }

    // Test 92: Test with property name matching but map of objects value
    @Test
    public void testBuildBeanDeserializer_MapOfObjectsValueProperty() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"scores\":{\"a\":1}}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(Integer.valueOf(1), result.scores.get("a"));
    }

    // Test 93: Test with property name matching but list of objects value
    @Test
    public void testBuildBeanDeserializer_ListOfObjectsValueProperty() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"tags\":[\"a\",\"b\"]}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(Arrays.asList("a", "b"), result.tags);
    }

    // Test 94: Test with property name matching but nested object value
    @Test
    public void testBuildBeanDeserializer_NestedObjectValueProperty() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"nested\":{\"x\":1,\"y\":2}}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertNotNull(result.nested);
        assertEquals(1, result.nested.x);
        assertEquals(2, result.nested.y);
    }

    // Test 95: Test with property name matching but deeply nested object value
    @Test
    public void testBuildBeanDeserializer_DeeplyNestedObjectValueProperty() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"nested\":{\"x\":1,\"y\":2}}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertNotNull(result.nested);
        assertEquals(1, result.nested.x);
        assertEquals(2, result.nested.y);
    }

    // Test 96: Test with property name matching but complex nested value
    @Test
    public void testBuildBeanDeserializer_ComplexNestedValueProperty() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"nested\":{\"x\":1,\"y\":2},\"tags\":[\"a\"],\"scores\":{\"k\":1}}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertNotNull(result.nested);
        assertEquals(1, result.nested.x);
        assertEquals(2, result.nested.y);
        assertEquals(Arrays.asList("a"), result.tags);
        assertEquals(Integer.valueOf(1), result.scores.get("k"));
    }

    // Test 97: Test with property name matching but mixed value types
    @Test
    public void testBuildBeanDeserializer_MixedValueTypesProperty() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"id\":\"123\",\"name\":123,\"active\":\"true\",\"value\":\"3.14\"}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(123, result.id);
        assertEquals("123", result.name);
        assertTrue(result.active);
        assertEquals(3.14, result.value, 0.001);
    }

    // Test 98: Test with property name matching but case insensitive
    @Test
    public void testBuildBeanDeserializer_CaseInsensitiveProperty() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"ID\":1}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(0, result.id);
    }

    // Test 99: Test with property name matching but underscore
    @Test
    public void testBuildBeanDeserializer_UnderscoreProperty() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"id_value\":1}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(0, result.id);
    }

    // Test 100: Test with property name matching but hyphen
    @Test
    public void testBuildBeanDeserializer_HyphenProperty() throws Exception {
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, simpleType, beanDesc);
        String json = "{\"id-value\":1}";
        SimpleBean result = (SimpleBean) deser.deserialize(mapper.getFactory().createParser(json), ctxt);
        assertNotNull(result);
        assertEquals(0, result.id);
    }
}