package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

import static org.junit.Assert.*;

public class JacksonAnnotationIntrospectorTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector());
    }

    // --- Custom serializers / deserializers ---------------------------------

    public static class UpperStringSerializer extends JsonSerializer<String> {
        @Override
        public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value == null ? null : value.toUpperCase());
        }
    }

    public static class LowerStringDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.getText().toLowerCase();
        }
    }

    public static class NullStringSerializer extends JsonSerializer<String> {
        @Override
        public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString("NULL");
        }
    }

    // --- Enum test fixtures --------------------------------------------------

    public enum EnumWithJsonProperty {
        @JsonProperty("alpha") ALPHA,
        @JsonProperty("beta") BETA,
        GAMMA
    }

    public enum EnumWithEmptyJsonProperty {
        @JsonProperty("") EMPTY
    }

    public enum EnumWithDefaultJsonProperty {
        @JsonProperty DEFAULT
    }

    public enum EnumWithJsonValue {
        A("aVal"), B("bVal");
        private final String value;
        EnumWithJsonValue(String value) { this.value = value; }
        @JsonValue
        public String value() { return value; }
    }

    public enum EnumWithCreatorAndValue {
        A("a-value"), B("b-value");
        private final String value;
        EnumWithCreatorAndValue(String value) { this.value = value; }
        @JsonValue
        public String value() { return value; }
        @JsonCreator
        public static EnumWithCreatorAndValue fromValue(String value) {
            if ("a-value".equals(value)) return A;
            if ("b-value".equals(value)) return B;
            return null;
        }
    }

    // --- POJO fixtures -------------------------------------------------------

    public static class PropertyBean {
        @JsonProperty("renamed")
        public String name = "x";
    }

    public static class IgnoreBean {
        public int visible = 1;
        @JsonIgnore
        public int hidden = 2;
    }

    @JsonIgnoreProperties({"b"})
    public static class IgnorePropsBean {
        public int a;
        public int b;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IgnoreUnknownBean {
        public int a;
    }

    public static class AnyBean {
        public int known;
        private final Map<String, Object> extras = new LinkedHashMap<String, Object>();

        @JsonAnySetter
        public void add(String key, Object value) {
            extras.put(key, value);
        }

        @JsonAnyGetter
        public Map<String, Object> any() {
            return extras;
        }
    }

    public static class CreatorBean {
        private final int x;
        private final String y;

        @JsonCreator
        public CreatorBean(@JsonProperty("x") int x, @JsonProperty("y") String y) {
            this.x = x;
            this.y = y;
        }

        public int getX() { return x; }
        public String getY() { return y; }
    }

    public static class JsonValueBean {
        public int id = 3;

        @JsonValue
        public String toValue() {
            return "id:" + id;
        }
    }

    public static class RawBean {
        public String name = "x";
        @JsonRawValue
        public String raw = "{\"a\":1}";
    }

    public static class SerializeUsingBean {
        @JsonSerialize(using = UpperStringSerializer.class)
        public String name = "abc";
    }

    public static class DeserializeUsingBean {
        @JsonDeserialize(using = LowerStringDeserializer.class)
        public String name;
    }

    public static class ContentUsingBean {
        @JsonSerialize(contentUsing = UpperStringSerializer.class)
        public List<String> values = Arrays.asList("a", "b");
    }

    public static class ContentDeserUsingBean {
        @JsonDeserialize(contentUsing = LowerStringDeserializer.class)
        public List<String> values;
    }

    public static class NullUsingBean {
        @JsonSerialize(nullUsing = NullStringSerializer.class)
        public String value;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = Dog.class, name = "dog"),
        @JsonSubTypes.Type(value = Cat.class, name = "cat")
    })
    public static class Animal {
        public String name;
    }

    public static class Dog extends Animal {
        public int boneCount;
    }

    @JsonTypeName("cat")
    public static class Cat extends Animal {
        public boolean lives;
    }

    @JsonRootName("root")
    public static class RootBean {
        public int value = 3;
    }

    @JsonFilter("myFilter")
    public static class FilteredBean {
        public String visible = "v";
        public String hidden = "h";
    }

    public static class InnerBean {
        public int x = 1;
        public int y = 2;
    }

    public static class UnwrappedBean {
        public String name = "n";
        @JsonUnwrapped
        public InnerBean inner = new InnerBean();
    }

    public static class TreeNode {
        public String name;
        @JsonManagedReference("node-child")
        public List<TreeNodeChild> children = new ArrayList<TreeNodeChild>();
    }

    public static class TreeNodeChild {
        public String name;
        @JsonBackReference("node-child")
        public TreeNode parent;
    }

    @JsonDeserialize(builder = BuilderBean.Builder.class)
    public static class BuilderBean {
        private final int x;
        private final String name;

        private BuilderBean(int x, String name) {
            this.x = x;
            this.name = name;
        }

        public int getX() { return x; }
        public String getName() { return name; }

        @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "with")
        public static class Builder {
            private int x;
            private String name = "default";

            @JsonProperty("x")
            public Builder withX(int x) {
                this.x = x;
                return this;
            }

            @JsonProperty("name")
            public Builder withName(String name) {
                this.name = name;
                return this;
            }

            public BuilderBean build() {
                return new BuilderBean(x, name);
            }
        }
    }

    @JsonNaming(PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy.class)
    public static class NamingBean {
        public int someValue = 3;
    }

    public static class IncludeBean {
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public String name;
        public int x = 1;
    }

    @Deprecated
    public static class DeprecatedBean { }

    // --- Direct introspector tests -------------------------------------------

    @Test
    public void testFindEnumValueWithJsonProperty() {
        JacksonAnnotationIntrospector intr = new JacksonAnnotationIntrospector();
        assertEquals("alpha", intr.findEnumValue(EnumWithJsonProperty.ALPHA));
        assertEquals("beta", intr.findEnumValue(EnumWithJsonProperty.BETA));
        assertEquals("GAMMA", intr.findEnumValue(EnumWithJsonProperty.GAMMA));
    }

    @Test
    public void testFindEnumValueWithEmptyJsonProperty() {
        assertEquals("EMPTY", new JacksonAnnotationIntrospector().findEnumValue(EnumWithEmptyJsonProperty.EMPTY));
    }

    @Test
    public void testFindEnumValueWithDefaultJsonProperty() {
        assertEquals("DEFAULT", new JacksonAnnotationIntrospector().findEnumValue(EnumWithDefaultJsonProperty.DEFAULT));
    }

    @Test
    public void testFindEnumValueWithJsonValue() {
        JacksonAnnotationIntrospector intr = new JacksonAnnotationIntrospector();
        assertEquals("aVal", intr.findEnumValue(EnumWithJsonValue.A));
        assertEquals("bVal", intr.findEnumValue(EnumWithJsonValue.B));
    }

    @Test
    public void testIsAnnotationBundle() throws Exception {
        JacksonAnnotationIntrospector intr = new JacksonAnnotationIntrospector();
        Field field = PropertyBean.class.getField("name");
        assertTrue(intr.isAnnotationBundle(field.getAnnotation(JsonProperty.class)));
        Deprecated deprecated = DeprecatedBean.class.getAnnotation(Deprecated.class);
        assertFalse(intr.isAnnotationBundle(deprecated));
    }

    // --- Annotation-driven serialization / deserialization tests -------------

    @Test
    public void testJsonPropertySerializationAndDeserialization() throws Exception {
        String json = mapper.writeValueAsString(new PropertyBean());
        assertTrue(json.contains("\"renamed\":\"x\""));
        assertFalse(json.contains("\"name\""));

        PropertyBean bean = mapper.readValue("{\"renamed\":\"y\"}", PropertyBean.class);
        assertEquals("y", bean.name);
    }

    @Test
    public void testJsonIgnore() throws Exception {
        String json = mapper.writeValueAsString(new IgnoreBean());
        assertTrue(json.contains("\"visible\":1"));
        assertFalse(json.contains("hidden"));

        IgnoreBean bean = mapper.readValue("{\"visible\":3,\"hidden\":4}", IgnoreBean.class);
        assertEquals(3, bean.visible);
        assertEquals(2, bean.hidden);
    }

    @Test
    public void testJsonIgnoreProperties() throws Exception {
        IgnorePropsBean bean = new IgnorePropsBean();
        bean.a = 1;
        bean.b = 2;

        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"a\":1"));
        assertFalse(json.contains("\"b\""));

        IgnorePropsBean result = mapper.readValue("{\"a\":3,\"b\":4}", IgnorePropsBean.class);
        assertEquals(3, result.a);
        assertEquals(0, result.b);
    }

    @Test
    public void testJsonIgnoreUnknownProperties() throws Exception {
        IgnoreUnknownBean result = mapper.readValue("{\"a\":5,\"unknown\":10}", IgnoreUnknownBean.class);
        assertEquals(5, result.a);
    }

    @Test
    public void testAnyGetterAndAnySetter() throws Exception {
        AnyBean bean = new AnyBean();
        bean.known = 1;
        bean.add("foo", "bar");

        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"known\":1"));
        assertTrue(json.contains("\"foo\":\"bar\""));

        AnyBean result = mapper.readValue("{\"known\":2,\"foo\":\"baz\"}", AnyBean.class);
        assertEquals(2, result.known);
        assertEquals("baz", result.any().get("foo"));
    }

    @Test
    public void testJsonCreator() throws Exception {
        CreatorBean bean = mapper.readValue("{\"x\":7,\"y\":\"hello\"}", CreatorBean.class);
        assertEquals(7, bean.getX());
        assertEquals("hello", bean.getY());
    }

    @Test
    public void testJsonValue() throws Exception {
        JsonValueBean bean = new JsonValueBean();
        bean.id = 42;
        assertEquals("\"id:42\"", mapper.writeValueAsString(bean).trim());
    }

    @Test
    public void testJsonRawValue() throws Exception {
        String json = mapper.writeValueAsString(new RawBean());
        assertTrue(json.contains("\"raw\":{\"a\":1}"));
        assertFalse(json.contains("\"raw\":\"{\\\"a\\\":1}\""));
    }

    @Test
    public void testJsonSerializeUsing() throws Exception {
        String json = mapper.writeValueAsString(new SerializeUsingBean());
        assertTrue(json.contains("\"name\":\"ABC\""));
    }

    @Test
    public void testJsonDeserializeUsing() throws Exception {
        DeserializeUsingBean bean = mapper.readValue("{\"name\":\"XYZ\"}", DeserializeUsingBean.class);
        assertEquals("xyz", bean.name);
    }

    @Test
    public void testJsonSerializeContentUsing() throws Exception {
        String json = mapper.writeValueAsString(new ContentUsingBean());
        assertTrue(json.contains("\"values\":[\"A\",\"B\"]"));
    }

    @Test
    public void testJsonDeserializeContentUsing() throws Exception {
        ContentDeserUsingBean bean = mapper.readValue("{\"values\":[\"A\",\"B\"]}", ContentDeserUsingBean.class);
        assertEquals(Arrays.asList("a", "b"), bean.values);
    }

    @Test
    public void testJsonSerializeNullUsing() throws Exception {
        String json = mapper.writeValueAsString(new NullUsingBean());
        assertTrue(json.contains("\"value\":\"NULL\""));
    }

    @Test
    public void testJsonTypeInfoSerialization() throws Exception {
        Dog dog = new Dog();
        dog.name = "Rex";
        dog.boneCount = 3;

        String json = mapper.writeValueAsString(dog);
        assertTrue(json.contains("\"type\":\"dog\""));
        assertTrue(json.contains("\"name\":\"Rex\""));
        assertTrue(json.contains("\"boneCount\":3"));
    }

    @Test
    public void testJsonTypeInfoDeserialization() throws Exception {
        Animal animal = mapper.readValue("{\"type\":\"dog\",\"name\":\"Rex\",\"boneCount\":3}", Animal.class);
        assertTrue(animal instanceof Dog);
        assertEquals("Rex", animal.name);
        assertEquals(3, ((Dog) animal).boneCount);

        Animal cat = mapper.readValue("{\"type\":\"cat\",\"name\":\"Tom\",\"lives\":true}", Animal.class);
        assertTrue(cat instanceof Cat);
        assertEquals("Tom", cat.name);
        assertTrue(((Cat) cat).lives);
    }

    @Test
    public void testJsonRootName() throws Exception {
        String json = mapper.writer().with(SerializationFeature.WRAP_ROOT_VALUE)
                .writeValueAsString(new RootBean());
        assertTrue(json.contains("\"root\""));

        RootBean bean = mapper.readerFor(RootBean.class)
                .with(DeserializationFeature.UNWRAP_ROOT_VALUE)
                .readValue("{\"root\":{\"value\":5}}");
        assertEquals(5, bean.value);
    }

    @Test
    public void testJsonFilter() throws Exception {
        SimpleFilterProvider filters = new SimpleFilterProvider();
        filters.addFilter("myFilter", SimpleBeanPropertyFilter.serializeAllExcept("hidden"));

        String json = mapper.writer(filters).writeValueAsString(new FilteredBean());
        assertTrue(json.contains("\"visible\":\"v\""));
        assertFalse(json.contains("\"hidden\""));
    }

    @Test
    public void testJsonUnwrapped() throws Exception {
        String json = mapper.writeValueAsString(new UnwrappedBean());
        assertTrue(json.contains("\"name\":\"n\""));
        assertTrue(json.contains("\"x\":1"));
        assertTrue(json.contains("\"y\":2"));
        assertFalse(json.contains("\"inner\""));
    }

    @Test
    public void testJsonManagedBackReference() throws Exception {
        TreeNode parent = new TreeNode();
        parent.name = "root";

        TreeNodeChild child = new TreeNodeChild();
        child.name = "child";
        child.parent = parent;
        parent.children.add(child);

        String json = mapper.writeValueAsString(parent);
        assertTrue(json.contains("\"children\""));
        assertTrue(json.contains("\"child\""));
        assertFalse(json.contains("\"parent\""));
    }

    @Test
    public void testJsonPOJOBuilder() throws Exception {
        BuilderBean bean = mapper.readValue("{\"x\":4,\"name\":\"n\"}", BuilderBean.class);
        assertEquals(4, bean.getX());
        assertEquals("n", bean.getName());
    }

    @Test
    public void testJsonNaming() throws Exception {
        String json = mapper.writeValueAsString(new NamingBean());
        assertTrue(json.contains("\"some_value\":3"));
    }

    @Test
    public void testJsonInclude() throws Exception {
        String json = mapper.writeValueAsString(new IncludeBean());
        assertTrue(json.contains("\"x\":1"));
        assertFalse(json.contains("\"name\""));
    }

    @Test
    public void testEnumSerializationWithJsonProperty() throws Exception {
        assertEquals("\"alpha\"", mapper.writeValueAsString(EnumWithJsonProperty.ALPHA));
        assertEquals("\"beta\"", mapper.writeValueAsString(EnumWithJsonProperty.BETA));
        assertEquals("\"GAMMA\"", mapper.writeValueAsString(EnumWithJsonProperty.GAMMA));
    }

    @Test
    public void testEnumSerializationAndDeserializationWithCreatorAndValue() throws Exception {
        assertEquals("\"a-value\"", mapper.writeValueAsString(EnumWithCreatorAndValue.A));
        assertEquals("\"b-value\"", mapper.writeValueAsString(EnumWithCreatorAndValue.B));
        assertEquals(EnumWithCreatorAndValue.A, mapper.readValue("\"a-value\"", EnumWithCreatorAndValue.class));
        assertEquals(EnumWithCreatorAndValue.B, mapper.readValue("\"b-value\"", EnumWithCreatorAndValue.class));
    }
}