package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.*;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

public class BeanDeserializerFactoryTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    @Test
    public void testDefaultInstanceAvailable() {
        assertNotNull(BeanDeserializerFactory.instance);
    }

    @Test
    public void testWithConfigIsIdentityAndCreatesNewFactory() {
        DeserializerFactoryConfig cfg = new DeserializerFactoryConfig();
        BeanDeserializerFactory factory = new BeanDeserializerFactory(cfg);

        DeserializerFactory same = factory.withConfig(cfg);
        assertSame(factory, same);

        DeserializerFactory copy = factory.withConfig(new DeserializerFactoryConfig());
        assertNotNull(copy);
        assertTrue(copy instanceof BeanDeserializerFactory);
        assertNotSame(factory, copy);
    }

    @Test
    public void testSimpleBeanDeserialization() throws Exception {
        SimpleBean bean = mapper.readValue("{\"value\":42}", SimpleBean.class);
        assertEquals(42, bean.getValue());
    }

    @Test
    public void testEmptyBeanDeserialization() throws Exception {
        EmptyBean bean = mapper.readValue("{}", EmptyBean.class);
        assertNotNull(bean);
    }

    @Test
    public void testBeanWithAnySetter() throws Exception {
        AnySetterBean bean = mapper.readValue("{\"extra\":7}", AnySetterBean.class);
        assertEquals(1, bean.getExtras().size());
        assertEquals(Integer.valueOf(7), bean.getExtras().get("extra"));
    }

    @Test
    public void testBeanWithAnySetterMap() throws Exception {
        AnySetterMapBean bean = mapper.readValue("{\"a\":1,\"b\":true}", AnySetterMapBean.class);
        assertEquals(2, bean.getValues().size());
        assertEquals(Integer.valueOf(1), bean.getValues().get("a"));
        assertEquals(Boolean.TRUE, bean.getValues().get("b"));
    }

    @Test
    public void testCreatorDeserialization() throws Exception {
        CreatorBean bean = mapper.readValue("{\"x\":3,\"y\":\"z\"}", CreatorBean.class);
        assertEquals(3, bean.getX());
        assertEquals("z", bean.getY());
    }

    @Test
    public void testIgnoredPropertiesFromAnnotation() throws Exception {
        IgnoreBean bean = mapper.readValue("{\"value\":4,\"ignored\":99}", IgnoreBean.class);
        assertEquals(4, bean.value);
        assertEquals(0, bean.ignored);
    }

    @Test
    public void testIgnoreUnknownPropertiesFromAnnotation() throws Exception {
        LooseBean bean = mapper.readValue("{\"value\":5,\"unknown\":\"x\"}", LooseBean.class);
        assertEquals(5, bean.value);
    }

    @Test
    public void testJsonPropertyIgnored() throws Exception {
        IgnorePropBean bean = mapper.readValue("{\"visible\":1,\"hidden\":2}", IgnorePropBean.class);
        assertEquals(1, bean.visible);
        assertEquals(0, bean.hidden);
    }

    @Test
    public void testUnknownPropertyIgnoredWithFeature() throws Exception {
        ObjectMapper lenient = new ObjectMapper();
        lenient.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        SimpleBean bean = lenient.readValue("{\"value\":1,\"unknown\":2}", SimpleBean.class);
        assertEquals(1, bean.getValue());
    }

    @Test
    public void testManagedAndBackReferenceDeserialization() throws Exception {
        ParentBean parent = mapper.readValue(
                "{\"name\":\"p\",\"children\":[{\"name\":\"c\"}]}", ParentBean.class);

        assertEquals(1, parent.getChildren().size());
        ChildBean child = parent.getChildren().get(0);
        assertNotNull(child.getParent());
        assertSame(parent, child.getParent());
    }

    @Test
    public void testThrowableDeserialization() throws Exception {
        MyException ex = mapper.readValue("{\"message\":\"boom\"}", MyException.class);
        assertEquals("boom", ex.getMessage());
    }

    @Test
    public void testAbstractTypeDeserialization() throws Exception {
        Animal animal = mapper.readValue(
                "{\"type\":\"dog\",\"name\":\"Rex\",\"bone\":2}", Animal.class);

        assertTrue(animal instanceof Dog);
        assertEquals("Rex", animal.name);
        assertEquals(2, ((Dog) animal).bone);
    }

    @Test
    public void testEnumDeserialization() throws Exception {
        Color color = mapper.readValue("\"RED\"", Color.class);
        assertSame(Color.RED, color);
    }

    @Test
    public void testCustomBeanDeserializer() throws Exception {
        ObjectMapper customMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(CustomBean.class, new SampleDeserializer());
        customMapper.registerModule(module);

        CustomBean bean = customMapper.readValue("{}", CustomBean.class);
        assertNotNull(bean);
    }

    @Test
    public void testBuilderBasedDeserialization() throws Exception {
        BuildableBean bean = mapper.readValue(
                "{\"id\":3,\"name\":\"n\"}", BuildableBean.class);

        assertEquals(3, bean.getId());
        assertEquals("n", bean.getName());
    }

    @Test
    public void testInjectableValuesDeserialization() throws Exception {
        InjectableValues.Std inject = new InjectableValues.Std().addValue("injected", 42);
        ObjectReader reader = mapper.reader(inject).forType(InjectableBean.class);

        InjectableBean bean = reader.readValue("{}");
        assertEquals(42, bean.getValue());
    }

    @Test
    public void testObjectIdDeserialization() throws Exception {
        IdentifiedBean bean = mapper.readValue(
                "{\"id\":1,\"name\":\"a\",\"other\":{\"id\":2,\"name\":\"b\"}}",
                IdentifiedBean.class);

        assertEquals(1, bean.id);
        assertNotNull(bean.other);
        assertEquals(2, bean.other.id);
    }

    @Test
    public void testUnwrappedDeserialization() throws Exception {
        UnwrappedBean bean = mapper.readValue("{\"a\":1,\"b\":2}", UnwrappedBean.class);
        assertEquals(1, bean.a);
        assertNotNull(bean.inner);
        assertEquals(2, bean.inner.b);
    }

    public static class SimpleBean {
        private int value;

        public SimpleBean() {
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    public static class EmptyBean {
    }

    public static class AnySetterBean {
        private Map<String, Object> extras = new HashMap<String, Object>();

        @JsonAnySetter
        public void add(String key, Object value) {
            extras.put(key, value);
        }

        public Map<String, Object> getExtras() {
            return extras;
        }
    }

    public static class AnySetterMapBean {
        private Map<String, Object> values = new HashMap<String, Object>();

        @JsonAnySetter
        public void addAll(Map<String, Object> values) {
            this.values.putAll(values);
        }

        public Map<String, Object> getValues() {
            return values;
        }
    }

    public static class CreatorBean {
        private final int x;
        private final String y;

        @JsonCreator
        public static CreatorBean create(@JsonProperty("x") int x, @JsonProperty("y") String y) {
            return new CreatorBean(x, y);
        }

        private CreatorBean(int x, String y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public String getY() {
            return y;
        }
    }

    @JsonIgnoreProperties({"ignored"})
    public static class IgnoreBean {
        public int value;
        public int ignored;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LooseBean {
        public int value;
    }

    public static class IgnorePropBean {
        public int visible;

        @JsonIgnore
        public int hidden;
    }

    public static class ParentBean {
        public String name;
        private List<ChildBean> children = new ArrayList<ChildBean>();

        @JsonManagedReference
        public List<ChildBean> getChildren() {
            return children;
        }

        public void setChildren(List<ChildBean> children) {
            this.children = children;
        }
    }

    public static class ChildBean {
        public String name;
        private ParentBean parent;

        @JsonBackReference
        public ParentBean getParent() {
            return parent;
        }

        public void setParent(ParentBean parent) {
            this.parent = parent;
        }
    }

    @SuppressWarnings("serial")
    public static class MyException extends Exception {
        @JsonCreator
        public MyException(@JsonProperty("message") String message) {
            super(message);
        }
    }

    @JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type")
    @JsonSubTypes({@JsonSubTypes.Type(value = Dog.class, name = "dog")})
    public static abstract class Animal {
        public String name;
    }

    public static class Dog extends Animal {
        public int bone;
    }

    public enum Color {
        RED,
        GREEN
    }

    public static class CustomBean {
        public int value;
    }

    public static class SampleDeserializer extends JsonDeserializer<CustomBean> {
        @Override
        public CustomBean deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return new CustomBean();
        }
    }

    @JsonDeserialize(builder = BuildableBean.Builder.class)
    public static class BuildableBean {
        private int id;
        private String name;

        private BuildableBean() {
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @JsonPOJOBuilder(buildMethodName = "create", withPrefix = "with")
        public static class Builder {
            private int id;
            private String name;

            public Builder withId(int id) {
                this.id = id;
                return this;
            }

            public Builder withName(String name) {
                this.name = name;
                return this;
            }

            public BuildableBean create() {
                BuildableBean bean = new BuildableBean();
                bean.id = id;
                bean.name = name;
                return bean;
            }
        }
    }

    public static class InjectableBean {
        private int value;

        public InjectableBean() {
        }

        @JacksonInject("injected")
        public void setValue(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    public static class IdentifiedBean {
        public int id;
        public String name;
        public IdentifiedBean other;
    }

    public static class UnwrappedBean {
        public int a;

        @JsonUnwrapped
        public InnerBean inner;

        public static class InnerBean {
            public int b;
        }
    }
}