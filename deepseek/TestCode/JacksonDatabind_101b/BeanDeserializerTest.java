package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.util.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class BeanDeserializerTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    // ----------------------------------------------------------
    // Basic deserialization tests
    // ----------------------------------------------------------

    @Test
    public void testSimpleBean() throws Exception {
        SimpleBean bean = mapper.readValue("{\"name\":\"John\",\"age\":30}", SimpleBean.class);
        assertEquals("John", bean.getName());
        assertEquals(Integer.valueOf(30), bean.getAge());
    }

    @Test
    public void testBeanWithMissingProperties() throws Exception {
        SimpleBean bean = mapper.readValue("{\"name\":\"John\"}", SimpleBean.class);
        assertEquals("John", bean.getName());
        assertNull(bean.getAge());
    }

    @Test(expected = UnrecognizedPropertyException.class)
    public void testBeanWithExtraPropertiesFails() throws Exception {
        mapper.readValue("{\"name\":\"John\",\"extra\":\"value\"}", SimpleBean.class);
    }

    @Test
    public void testBeanWithExtraPropertiesIgnored() throws Exception {
        IgnoreExtraBean bean = mapper.readValue("{\"name\":\"John\",\"extra\":\"value\"}", IgnoreExtraBean.class);
        assertEquals("John", bean.getName());
    }

    @Test
    public void testBeanWithNull() throws Exception {
        SimpleBean bean = mapper.readValue("null", SimpleBean.class);
        assertNull(bean);
    }

    @Test
    public void testBeanWithEmptyObject() throws Exception {
        SimpleBean bean = mapper.readValue("{}", SimpleBean.class);
        assertNull(bean.getName());
        assertNull(bean.getAge());
    }

    // ----------------------------------------------------------
    // Nested objects, collections, enums
    // ----------------------------------------------------------

    @Test
    public void testBeanWithNestedObject() throws Exception {
        OuterBean bean = mapper.readValue("{\"inner\":{\"name\":\"inner\"}}", OuterBean.class);
        assertNotNull(bean.getInner());
        assertEquals("inner", bean.getInner().getName());
    }

    @Test
    public void testBeanWithList() throws Exception {
        ListBean bean = mapper.readValue("{\"items\":[\"a\",\"b\"]}", ListBean.class);
        assertNotNull(bean.getItems());
        assertEquals(2, bean.getItems().size());
        assertEquals("a", bean.getItems().get(0));
        assertEquals("b", bean.getItems().get(1));
    }

    @Test
    public void testBeanWithMap() throws Exception {
        MapBean bean = mapper.readValue("{\"map\":{\"key\":\"value\"}}", MapBean.class);
        assertNotNull(bean.getMap());
        assertEquals("value", bean.getMap().get("key"));
    }

    @Test
    public void testBeanWithEnum() throws Exception {
        EnumBean bean = mapper.readValue("{\"color\":\"RED\"}", EnumBean.class);
        assertEquals(Color.RED, bean.getColor());
    }

    // ----------------------------------------------------------
    // Constructor / factory method
    // ----------------------------------------------------------

    @Test
    public void testBeanWithConstructor() throws Exception {
        ConstructorBean bean = mapper.readValue("{\"name\":\"John\",\"age\":30}", ConstructorBean.class);
        assertEquals("John", bean.getName());
        assertEquals(30, bean.getAge());
    }

    @Test
    public void testBeanWithFactoryMethod() throws Exception {
        FactoryBean bean = mapper.readValue("{\"val\":\"test\"}", FactoryBean.class);
        assertEquals("test", bean.getValue());
    }

    // ----------------------------------------------------------
    // Any-setter, ignored properties
    // ----------------------------------------------------------

    @Test
    public void testBeanWithAnySetter() throws Exception {
        AnySetterBean bean = mapper.readValue("{\"name\":\"John\",\"extra\":\"value\"}", AnySetterBean.class);
        assertEquals("John", bean.getAny().get("name"));
        assertEquals("value", bean.getAny().get("extra"));
    }

    @Test
    public void testBeanWithIgnoredProperty() throws Exception {
        IgnoredPropertyBean bean = mapper.readValue("{\"name\":\"John\",\"ignored\":\"shouldBeIgnored\"}", IgnoredPropertyBean.class);
        assertEquals("John", bean.getName());
        assertNull(bean.getIgnored());
    }

    // ----------------------------------------------------------
    // Polymorphic types
    // ----------------------------------------------------------

    @Test
    public void testBeanWithPolymorphicType() throws Exception {
        BaseTypeBean bean = mapper.readValue("{\"type\":\"sub\",\"baseProp\":\"base\",\"subProp\":\"sub\"}", BaseTypeBean.class);
        assertTrue(bean instanceof SubTypeBean);
        SubTypeBean sub = (SubTypeBean) bean;
        assertEquals("base", sub.getBaseProp());
        assertEquals("sub", sub.getSubProp());
    }

    // ----------------------------------------------------------
    // Generic / bounded type parameters (Defects4J bug 101)
    // ----------------------------------------------------------

    @Test
    public void testBeanWithGenericBoundedType() throws Exception {
        // T = String, U extends T (U = String)
        GenericBounded<String, String> bean = mapper.readValue(
                "{\"value\":\"test\"}",
                new TypeReference<GenericBounded<String, String>>() {});
        assertEquals("test", bean.getValue());
    }

    @Test
    public void testBeanWithComparableBound() throws Exception {
        ComparableBound<String> bean = mapper.readValue(
                "{\"value\":\"test\"}",
                new TypeReference<ComparableBound<String>>() {});
        assertEquals("test", bean.getValue());
    }

    @Test
    public void testBeanWithMultipleBounds() throws Exception {
        MultipleBounds<String> bean = mapper.readValue(
                "{\"value\":\"test\"}",
                new TypeReference<MultipleBounds<String>>() {});
        assertEquals("test", bean.getValue());
    }

    // ----------------------------------------------------------
    // Custom deserializer
    // ----------------------------------------------------------

    @Test
    public void testBeanWithCustomDeserializer() throws Exception {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(CustomBean.class, new JsonDeserializer<CustomBean>() {
            @Override
            public CustomBean deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                p.skipChildren();
                return new CustomBean("custom");
            }
        });
        ObjectMapper localMapper = new ObjectMapper();
        localMapper.registerModule(module);
        CustomBean bean = localMapper.readValue("\"ignored\"", CustomBean.class);
        assertEquals("custom", bean.getValue());
    }

    // ==========================================================
    // Inner classes used by the tests
    // ==========================================================

    static class SimpleBean {
        private String name;
        private Integer age;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
    }

    @JsonIgnoreProperties("extra")
    static class IgnoreExtraBean {
        private String name;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    static class OuterBean {
        private InnerBean inner;

        public InnerBean getInner() { return inner; }
        public void setInner(InnerBean inner) { this.inner = inner; }
    }

    static class InnerBean {
        private String name;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    static class ListBean {
        private List<String> items;

        public List<String> getItems() { return items; }
        public void setItems(List<String> items) { this.items = items; }
    }

    static class MapBean {
        private Map<String, String> map;

        public Map<String, String> getMap() { return map; }
        public void setMap(Map<String, String> map) { this.map = map; }
    }

    enum Color { RED, GREEN, BLUE }

    static class EnumBean {
        private Color color;

        public Color getColor() { return color; }
        public void setColor(Color color) { this.color = color; }
    }

    static class ConstructorBean {
        private String name;
        private int age;

        @JsonCreator
        public ConstructorBean(@JsonProperty("name") String name, @JsonProperty("age") int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() { return name; }
        public int getAge() { return age; }
    }

    static class FactoryBean {
        private String value;

        private FactoryBean(String v) { this.value = v; }

        @JsonCreator
        public static FactoryBean create(@JsonProperty("val") String val) {
            return new FactoryBean(val);
        }

        public String getValue() { return value; }
    }

    static class AnySetterBean {
        private Map<String, Object> any = new HashMap<>();

        @JsonAnySetter
        public void setAny(String key, Object value) { any.put(key, value); }

        @JsonAnyGetter
        public Map<String, Object> getAny() { return any; }
    }

    static class IgnoredPropertyBean {
        private String name;
        @JsonIgnore
        private String ignored;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getIgnored() { return ignored; }
        public void setIgnored(String ignored) { this.ignored = ignored; }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes({ @JsonSubTypes.Type(value = SubTypeBean.class, name = "sub") })
    static class BaseTypeBean {
        private String baseProp;

        public String getBaseProp() { return baseProp; }
        public void setBaseProp(String baseProp) { this.baseProp = baseProp; }
    }

    static class SubTypeBean extends BaseTypeBean {
        private String subProp;

        public String getSubProp() { return subProp; }
        public void setSubProp(String subProp) { this.subProp = subProp; }
    }

    // Generic classes for bug 101

    static class GenericBounded<T, U extends T> {
        private U value;

        public U getValue() { return value; }
        public void setValue(U value) { this.value = value; }
    }

    static class ComparableBound<T extends Comparable<T>> {
        private T value;

        public T getValue() { return value; }
        public void setValue(T value) { this.value = value; }
    }

    static class MultipleBounds<T extends Comparable<T> & java.io.Serializable> {
        private T value;

        public T getValue() { return value; }
        public void setValue(T value) { this.value = value; }
    }

    static class CustomBean {
        private String value;

        public CustomBean(String v) { this.value = v; }
        public String getValue() { return value; }
    }
}