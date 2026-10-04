package com.fasterxml.jackson.databind.deser;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.Test;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.*;

public class BeanDeserializerFactoryTest {

    public static class SimpleBean {
        private int id;
        private String name;

        public SimpleBean() { }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class CreatorBean {
        private final int value;
        private final String label;

        @JsonCreator
        public CreatorBean(@JsonProperty("value") int value,
                           @JsonProperty("label") String label) {
            this.value = value;
            this.label = label;
        }

        public int getValue() { return value; }
        public String getLabel() { return label; }
    }

    public static class CreatorAndSetterBean {
        private int value;

        public CreatorAndSetterBean() { }

        @JsonCreator
        public CreatorAndSetterBean(@JsonProperty("value") int value) {
            this.value = value;
        }

        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
    }

    @JsonDeserialize(builder = ImmutableBean.Builder.class)
    public static class ImmutableBean {
        private final int id;
        private final String name;

        private ImmutableBean(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() { return id; }
        public String getName() { return name; }

        public static class Builder {
            private int id;
            private String name;

            public Builder withId(int id) { this.id = id; return this; }
            public Builder withName(String name) { this.name = name; return this; }

            public ImmutableBean build() {
                return new ImmutableBean(id, name);
            }
        }
    }

    @JsonDeserialize(builder = PrefixlessBean.Builder.class)
    public static class PrefixlessBean {
        private final int value;

        private PrefixlessBean(int value) { this.value = value; }

        public int getValue() { return value; }

        @JsonPOJOBuilder(withPrefix = "")
        public static class Builder {
            private int value;

            public Builder value(int value) { this.value = value; return this; }

            public PrefixlessBean build() {
                return new PrefixlessBean(value);
            }
        }
    }

    @JsonDeserialize(as = ConcreteBean.class)
    public abstract static class AbstractBean {
        @JsonProperty("value")
        public abstract String getValue();
    }

    public static class ConcreteBean extends AbstractBean {
        private String value;

        public ConcreteBean() { }

        @Override
        public String getValue() { return value; }

        public void setValue(String value) { this.value = value; }
    }

    public abstract static class AbstractNoType {
        private String value;

        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    public static class MyException extends Exception {
        private String code;

        public MyException() { }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    public static class Node {
        private int id;
        public Node friend;

        public Node() { }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public Node getFriend() { return friend; }
        public void setFriend(Node friend) { this.friend = friend; }
    }

    public static class Parent {
        public String name;

        @JsonManagedReference
        public List<Child> children = new ArrayList<Child>();

        public Parent() { }
    }

    public static class Child {
        public String name;

        @JsonBackReference
        public Parent parent;

        public Child() { }
    }

    public static class AnySetterBean {
        private Map<String, Object> extras = new HashMap<String, Object>();

        @JsonAnySetter
        public void add(String key, Object value) {
            extras.put(key, value);
        }

        public Map<String, Object> getExtras() { return extras; }
    }

    public static class InjectBean {
        private int id;
        private String name;

        public InjectBean() { }

        @JacksonInject("id")
        public void setId(int id) { this.id = id; }

        @JsonProperty("name")
        public void setName(String name) { this.name = name; }

        public int getId() { return id; }
        public String getName() { return name; }
    }

    @Test
    public void testFactoryConfigLifecycle() {
        DeserializerFactoryConfig config = new DeserializerFactoryConfig();
        BeanDeserializerFactory factory = new BeanDeserializerFactory(config);

        assertSame(config, factory.getFactoryConfig());
        assertSame(factory, factory.withConfig(config));

        DeserializerFactoryConfig other = new DeserializerFactoryConfig();
        DeserializerFactory changed = factory.withConfig(other);
        assertNotSame(factory, changed);
        assertSame(other, changed.getFactoryConfig());
    }

    @Test
    public void testSimpleBeanDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        SimpleBean bean = mapper.readValue("{\"id\":3,\"name\":\"foo\"}", SimpleBean.class);

        assertEquals(3, bean.getId());
        assertEquals("foo", bean.getName());
    }

    @Test
    public void testCreatorBeanDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        CreatorBean bean = mapper.readValue("{\"value\":11,\"label\":\"eleven\"}", CreatorBean.class);

        assertEquals(11, bean.getValue());
        assertEquals("eleven", bean.getLabel());
    }

    @Test
    public void testCreatorWithSetterDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        CreatorAndSetterBean bean = mapper.readValue("{\"value\":5}", CreatorAndSetterBean.class);

        assertEquals(5, bean.getValue());
    }

    @Test
    public void testBuilderBasedDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        ImmutableBean bean = mapper.readValue("{\"id\":7,\"name\":\"builder\"}", ImmutableBean.class);

        assertEquals(7, bean.getId());
        assertEquals("builder", bean.getName());
    }

    @Test
    public void testPrefixlessBuilderDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        PrefixlessBean bean = mapper.readValue("{\"value\":9}", PrefixlessBean.class);

        assertEquals(9, bean.getValue());
    }

    @Test
    public void testAbstractClassWithDeserializeAs() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        AbstractBean bean = mapper.readValue("{\"value\":\"abstract\"}", AbstractBean.class);

        assertTrue(bean instanceof ConcreteBean);
        assertEquals("abstract", bean.getValue());
    }

    @Test(expected = JsonMappingException.class)
    public void testAbstractClassWithoutTypeInfoFails() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("{\"value\":\"x\"}", AbstractNoType.class);
    }

    @Test
    public void testThrowableDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        MyException ex = mapper.readValue("{\"code\":\"E1\"}", MyException.class);

        assertEquals("E1", ex.getCode());
    }

    @Test
    public void testObjectIdDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        Node node = mapper.readValue("{\"id\":1,\"friend\":{\"id\":1}}", Node.class);

        assertEquals(1, node.getId());
        assertNotNull(node.getFriend());
        assertSame(node, node.getFriend());
    }

    @Test
    public void testManagedBackReferenceDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        Parent parent = mapper.readValue("{\"name\":\"p\",\"children\":[{\"name\":\"c\"}]}", Parent.class);

        assertNotNull(parent.children);
        assertEquals(1, parent.children.size());
        assertEquals("c", parent.children.get(0).name);
        assertSame(parent, parent.children.get(0).parent);
    }

    @Test
    public void testAnySetterDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        AnySetterBean bean = mapper.readValue("{\"a\":1,\"b\":\"two\"}", AnySetterBean.class);

        assertEquals(2, bean.getExtras().size());
        assertEquals(1, bean.getExtras().get("a"));
        assertEquals("two", bean.getExtras().get("b"));
    }

    @Test
    public void testInjectableValues() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setInjectableValues(new InjectableValues.Std().addValue("id", 42));

        InjectBean bean = mapper.readValue("{\"name\":\"injected\"}", InjectBean.class);

        assertEquals(42, bean.getId());
        assertEquals("injected", bean.getName());
    }

    @Test(expected = JsonMappingException.class)
    public void testUnknownPropertyFailsByDefault() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("{\"id\":1,\"unknown\":2}", SimpleBean.class);
    }

    @Test
    public void testUnknownPropertyIgnoredWhenConfigured() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        SimpleBean bean = mapper.readValue("{\"id\":1,\"unknown\":2}", SimpleBean.class);

        assertEquals(1, bean.getId());
    }

    @Test
    public void testObjectClassUntypedDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        Object result = mapper.readValue("{\"a\":[1,true,null]}", Object.class);

        assertNotNull(result);
        assertTrue(result instanceof java.util.Map);
    }
}