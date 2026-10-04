package com.fasterxml.jackson.databind.deser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.*;

import org.junit.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.*;

public class BeanDeserializerFactoryTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    // --- Simple bean -----------------------------------------------------------------

    public static class SimpleBean {
        public String name;
        public int age;
    }

    @Test
    public void testSimpleBeanDeserialization() throws Exception {
        SimpleBean bean = mapper.readValue("{\"name\":\"Joe\",\"age\":42}", SimpleBean.class);
        assertEquals("Joe", bean.name);
        assertEquals(42, bean.age);
    }

    public static class EmptyBean {
    }

    @Test
    public void testEmptyBeanDeserialization() throws Exception {
        EmptyBean bean = mapper.readValue("{}", EmptyBean.class);
        assertNotNull(bean);
    }

    // --- Creator constructors ----------------------------------------------------------

    public static class CreatorBean {
        public final int id;
        public final String name;

        @JsonCreator
        public CreatorBean(@JsonProperty("id") int id, @JsonProperty("name") String name) {
            this.id = id;
            this.name = name;
        }
    }

    @Test
    public void testCreatorConstructorDeserialization() throws Exception {
        CreatorBean bean = mapper.readValue("{\"id\":3,\"name\":\"Bob\"}", CreatorBean.class);
        assertEquals(3, bean.id);
        assertEquals("Bob", bean.name);
    }

    public static class SinglePropertyCreatorBean {
        public final int id;

        @JsonCreator
        public SinglePropertyCreatorBean(@JsonProperty("id") int id) {
            this.id = id;
        }
    }

    @Test
    public void testSinglePropertyCreatorDeserialization() throws Exception {
        SinglePropertyCreatorBean bean = mapper.readValue("{\"id\":7}", SinglePropertyCreatorBean.class);
        assertEquals(7, bean.id);
    }

    public static class DelegatingBean {
        public final String value;

        @JsonCreator
        public DelegatingBean(String value) {
            this.value = value;
        }
    }

    @Test
    public void testDelegatingCreatorDeserialization() throws Exception {
        DelegatingBean bean = mapper.readValue("\"abc\"", DelegatingBean.class);
        assertEquals("abc", bean.value);
    }

    // --- Static factory methods --------------------------------------------------------

    public static class FactoryBean {
        public final String value;

        private FactoryBean(String v) {
            this.value = v;
        }

        @JsonCreator
        public static FactoryBean create(@JsonProperty("value") String v) {
            return new FactoryBean(v);
        }
    }

    @Test
    public void testStaticFactoryMethodDeserialization() throws Exception {
        FactoryBean bean = mapper.readValue("{\"value\":\"factory\"}", FactoryBean.class);
        assertEquals("factory", bean.value);
    }

    public static class SinglePropertyFactoryBean {
        public final String value;

        private SinglePropertyFactoryBean(String v) {
            this.value = v;
        }

        @JsonCreator
        public static SinglePropertyFactoryBean create(@JsonProperty("value") String v) {
            return new SinglePropertyFactoryBean(v);
        }
    }

    @Test
    public void testSinglePropertyStaticFactoryMethodDeserialization() throws Exception {
        SinglePropertyFactoryBean bean = mapper.readValue("{\"value\":\"single\"}", SinglePropertyFactoryBean.class);
        assertEquals("single", bean.value);
    }

    // --- Any setter --------------------------------------------------------------------

    public static class AnySetterBean {
        public String known;
        public Map<String, Object> extra = new HashMap<String, Object>();

        @JsonAnySetter
        public void setExtra(String key, Object value) {
            extra.put(key, value);
        }
    }

    @Test
    public void testAnySetterDeserialization() throws Exception {
        AnySetterBean bean = mapper.readValue("{\"known\":\"k\",\"extra1\":1,\"extra2\":\"v\"}", AnySetterBean.class);
        assertEquals("k", bean.known);
        assertEquals(1, bean.extra.get("extra1"));
        assertEquals("v", bean.extra.get("extra2"));
    }

    // --- Reference properties ----------------------------------------------------------

    public static class ParentBean {
        public String name;
        @JsonManagedReference
        public List<ChildBean> children = new ArrayList<ChildBean>();
    }

    public static class ChildBean {
        public String name;
        @JsonBackReference
        public ParentBean parent;
    }

    @Test
    public void testManagedBackReferenceDeserialization() throws Exception {
        ParentBean parent = mapper.readValue(
                "{\"name\":\"p\",\"children\":[{\"name\":\"c\"}]}",
                ParentBean.class);
        assertEquals(1, parent.children.size());
        assertSame(parent, parent.children.get(0).parent);
    }

    // --- Object id ---------------------------------------------------------------------

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    public static class LinkedNode {
        public int id;
        public String name;
        public LinkedNode next;
    }

    @Test
    public void testObjectIdCycleDeserialization() throws Exception {
        LinkedNode node = mapper.readValue(
                "{\"id\":1,\"name\":\"a\",\"next\":{\"id\":2,\"name\":\"b\",\"next\":1}}",
                LinkedNode.class);
        assertEquals(1, node.id);
        assertNotNull(node.next);
        assertEquals(2, node.next.id);
        assertSame(node, node.next.next);
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    public static class ObjectIdCreatorBean {
        public final int id;
        public String name;
        public ObjectIdCreatorBean next;

        @JsonCreator
        public ObjectIdCreatorBean(@JsonProperty("id") int id) {
            this.id = id;
        }
    }

    @Test
    public void testObjectIdWithCreatorDeserialization() throws Exception {
        ObjectIdCreatorBean bean = mapper.readValue(
                "{\"id\":1,\"name\":\"first\",\"next\":{\"id\":1,\"name\":\"second\"}}",
                ObjectIdCreatorBean.class);
        assertSame(bean, bean.next);
    }

    // --- Injectable values --------------------------------------------------------------

    public static class InjectBean {
        @JacksonInject("id")
        public int id;
        public String name;
    }

    @Test
    public void testInjectedValueDeserialization() throws Exception {
        mapper.setInjectableValues(new InjectableValues.Std().addValue("id", 456));
        InjectBean bean = mapper.readValue("{\"name\":\"n\"}", InjectBean.class);
        assertEquals(456, bean.id);
        assertEquals("n", bean.name);
    }

    // --- Builder-based deserialization --------------------------------------------------

    @JsonDeserialize(builder = BuilderBean.Builder.class)
    public static class BuilderBean {
        public final int x;
        public final String y;

        private BuilderBean(int x, String y) {
            this.x = x;
            this.y = y;
        }

        @JsonPOJOBuilder(withPrefix = "with")
        public static class Builder {
            private int x;
            private String y;

            @JsonProperty("x")
            public Builder withX(int x) {
                this.x = x;
                return this;
            }

            @JsonProperty("y")
            public Builder withY(String y) {
                this.y = y;
                return this;
            }

            public BuilderBean build() {
                return new BuilderBean(x, y);
            }
        }
    }

    @Test
    public void testBuilderDeserialization() throws Exception {
        BuilderBean bean = mapper.readValue("{\"x\":3,\"y\":\"z\"}", BuilderBean.class);
        assertEquals(3, bean.x);
        assertEquals("z", bean.y);
    }

    // --- Unwrapped properties -----------------------------------------------------------

    public static class UnwrappedBean {
        public String name;
        @JsonUnwrapped
        public InnerBean inner;
    }

    public static class InnerBean {
        public int x;
    }

    @Test
    public void testUnwrappedPropertyDeserialization() throws Exception {
        UnwrappedBean bean = mapper.readValue("{\"name\":\"n\",\"x\":5}", UnwrappedBean.class);
        assertEquals("n", bean.name);
        assertNotNull(bean.inner);
        assertEquals(5, bean.inner.x);
    }

    // --- Custom property deserializer ---------------------------------------------------

    public static class UpperDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String value = p.getValueAsString();
            return (value == null) ? null : value.toUpperCase(Locale.ROOT);
        }
    }

    public static class CustomBean {
        @JsonDeserialize(using = UpperDeserializer.class)
        public String value;
    }

    @Test
    public void testCustomPropertyDeserializer() throws Exception {
        CustomBean bean = mapper.readValue("{\"value\":\"abc\"}", CustomBean.class);
        assertEquals("ABC", bean.value);
    }

    // --- Throwable deserialization ------------------------------------------------------

    @JsonIgnoreProperties({ "stackTrace", "suppressed" })
    public static class CustomException extends Exception {
        public String extra;

        @JsonCreator
        public CustomException(@JsonProperty("message") String message) {
            super(message);
        }
    }

    @Test
    public void testCustomExceptionDeserialization() throws Exception {
        CustomException ex = mapper.readValue(
                "{\"message\":\"boom\",\"extra\":\"x\"}",
                CustomException.class);
        assertEquals("boom", ex.getMessage());
        assertEquals("x", ex.extra);
    }

    // --- Abstract polymorphic deserialization ---------------------------------------------

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes({ @JsonSubTypes.Type(value = Dog.class, name = "dog") })
    public static abstract class Animal {
        public String name;
    }

    public static class Dog extends Animal {
        public int barkVolume;
    }

    @Test
    public void testAbstractPolymorphicDeserialization() throws Exception {
        Animal animal = mapper.readValue(
                "{\"type\":\"dog\",\"name\":\"Rex\",\"barkVolume\":3}",
                Animal.class);
        assertTrue(animal instanceof Dog);
        Dog dog = (Dog) animal;
        assertEquals("Rex", dog.name);
        assertEquals(3, dog.barkVolume);
    }

    // --- Ignored properties ----------------------------------------------------------------

    public static class IgnoredBean {
        @JsonIgnore
        public String value;
        public String visible;
    }

    @Test
    public void testIgnoredPropertyDeserialization() throws Exception {
        IgnoredBean bean = mapper.readValue(
                "{\"value\":\"ignored\",\"visible\":\"ok\"}",
                IgnoredBean.class);
        assertNull(bean.value);
        assertEquals("ok", bean.visible);
    }

    @JsonIgnoreProperties("unknown")
    public static class IgnoreUnknownBean {
        public String known;
    }

    @Test
    public void testIgnoredUnknownPropertiesViaAnnotation() throws Exception {
        IgnoreUnknownBean bean = mapper.readValue(
                "{\"known\":\"k\",\"unknown\":123}",
                IgnoreUnknownBean.class);
        assertEquals("k", bean.known);
    }

    @Test
    public void testUnknownPropertyFailure() throws Exception {
        try {
            mapper.readValue("{\"name\":\"Joe\",\"unknown\":123}", SimpleBean.class);
            fail("Should have thrown JsonMappingException for unknown property");
        } catch (JsonMappingException e) {
            // expected
        }
    }

    // --- Read-only properties ---------------------------------------------------------------

    public static class ReadOnlyBean {
        private int value = 1;

        @JsonProperty("value")
        public int getValue() {
            return value;
        }
    }

    @Test
    public void testGetterOnlyPropertyIsNotUsedForDeserialization() throws Exception {
        ReadOnlyBean bean = mapper.readValue("{\"value\":2}", ReadOnlyBean.class);
        assertEquals(1, bean.value);
    }
}