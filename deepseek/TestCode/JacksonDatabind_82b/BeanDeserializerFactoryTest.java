package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.introspect.BeanDescription;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class BeanDeserializerFactoryTest {

    static class SimpleBean {
        public int x;
        private String name;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getX() { return x; }
        public void setX(int x) { this.x = x; }
    }

    static class CreatorBean {
        int id;
        String name;

        @JsonCreator
        public CreatorBean(@JsonProperty("id") int id, @JsonProperty("name") String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() { return id; }
        public String getName() { return name; }
    }

    static class CreatorWithField {
        public int x;

        @JsonCreator
        public CreatorWithField(@JsonProperty("x") int x) {
            this.x = x;
        }
    }

    @JsonDeserialize(builder = BuilderBean.Builder.class)
    static class BuilderBean {
        final int a;
        final String b;

        private BuilderBean(int a, String b) {
            this.a = a;
            this.b = b;
        }

        public int getA() { return a; }
        public String getB() { return b; }

        static class Builder {
            private int a;
            private String b;

            @JsonProperty("a")
            public Builder withA(int a) { this.a = a; return this; }

            @JsonProperty("b")
            public Builder withB(String b) { this.b = b; return this; }

            public BuilderBean build() { return new BuilderBean(a, b); }
        }
    }

    static class RefParent {
        public int id;
        @JsonManagedReference
        public List<RefChild> children = new ArrayList<RefChild>();
    }

    static class RefChild {
        public int id;
        @JsonBackReference
        public RefParent parent;
    }

    static class InjectBean {
        @JacksonInject("id")
        public String id;
        public String name;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
    static class IdentBean {
        public int id;
        public String name;
    }

    @JsonIgnoreProperties({"ignored"})
    static class IgnoreBean {
        public int kept;
        public int ignored;
    }

    static class GetterOnlyBean {
        private List<String> items = new ArrayList<String>();

        public List<String> getItems() { return items; }
    }

    static class AnySetterBean {
        private Map<String, Object> extra = new HashMap<String, Object>();

        @JsonAnySetter
        public void setExtra(String key, Object value) { extra.put(key, value); }

        public Map<String, Object> getExtra() { return extra; }
    }

    static class MyException extends Exception {
        public MyException() { }
        public MyException(String message) { super(message); }
    }

    static class MyExceptionWithCause extends Exception {
        @JsonCreator
        public MyExceptionWithCause(@JsonProperty("message") String message,
                                    @JsonProperty("cause") Throwable cause) {
            super(message, cause);
        }
    }

    abstract static class AbstractWithCreator {
        int x;

        @JsonCreator
        public static AbstractWithCreator create(@JsonProperty("x") int x) {
            return new AbstractWithCreator() { { this.x = x; } };
        }

        public int getX() { return x; }
    }

    @JsonDeserialize(as = Impl.class)
    interface PolyBase { }

    static class Impl implements PolyBase {
        public int x;
    }

    private ObjectMapper mapper() {
        return new ObjectMapper();
    }

    private DeserializationContext context(ObjectMapper mapper) {
        return new DefaultDeserializationContext.Impl(mapper.getDeserializationConfig());
    }

    private JavaType type(ObjectMapper mapper, Class<?> cls) {
        return mapper.getTypeFactory().constructType(cls);
    }

    private BeanDescription description(ObjectMapper mapper, Class<?> cls) {
        return mapper.getDeserializationConfig().introspect(type(mapper, cls));
    }

    @Test
    public void testInstanceNotNull() {
        assertNotNull(BeanDeserializerFactory.instance);
    }

    @Test
    public void testWithConfigReturnsSameForSameConfig() {
        DeserializerFactoryConfig cfg = new DeserializerFactoryConfig();
        BeanDeserializerFactory factory = new BeanDeserializerFactory(cfg);
        DeserializerFactory result = factory.withConfig(cfg);
        assertSame(factory, result);
    }

    @Test
    public void testWithConfigReturnsNewForDifferentConfig() {
        BeanDeserializerFactory factory = new BeanDeserializerFactory(new DeserializerFactoryConfig());
        DeserializerFactory result = factory.withConfig(new DeserializerFactoryConfig());
        assertNotNull(result);
        assertTrue(result instanceof BeanDeserializerFactory);
        assertNotSame(factory, result);
    }

    @Test
    public void testCreateConcreteBeanDeserializer() throws Exception {
        ObjectMapper mapper = mapper();
        JsonDeserializer<Object> deser = BeanDeserializerFactory.instance.createBeanDeserializer(
                context(mapper), type(mapper, SimpleBean.class), description(mapper, SimpleBean.class));
        assertNotNull(deser);
    }

    @Test
    public void testCreateBeanDeserializerUsesCustomDeserializer() throws Exception {
        final JsonDeserializer<SimpleBean> custom = new JsonDeserializer<SimpleBean>() {
            @Override
            public SimpleBean deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return new SimpleBean();
            }
        };

        SimpleDeserializers deserializers = new SimpleDeserializers();
        deserializers.addDeserializer(SimpleBean.class, custom);
        DeserializerFactoryConfig cfg = new DeserializerFactoryConfig().withDeserializers(deserializers);
        BeanDeserializerFactory factory = new BeanDeserializerFactory(cfg);

        ObjectMapper mapper = mapper();
        JsonDeserializer<Object> result = factory.createBeanDeserializer(
                context(mapper), type(mapper, SimpleBean.class), description(mapper, SimpleBean.class));
        assertSame(custom, result);
    }

    @Test
    public void testThrowableDeserializerCreationAndUsage() throws Exception {
        ObjectMapper mapper = mapper();
        JsonDeserializer<Object> deser = BeanDeserializerFactory.instance.createBeanDeserializer(
                context(mapper), type(mapper, MyException.class), description(mapper, MyException.class));
        assertNotNull(deser);

        MyException ex = mapper.readValue("{\"message\":\"boom\"}", MyException.class);
        assertEquals("boom", ex.getMessage());
    }

    @Test
    public void testThrowableDeserializerWithCause() throws Exception {
        ObjectMapper mapper = mapper();
        MyExceptionWithCause ex = mapper.readValue(
                "{\"message\":\"outer\",\"cause\":{\"message\":\"inner\"}}", MyExceptionWithCause.class);
        assertEquals("outer", ex.getMessage());
        assertNotNull(ex.getCause());
        assertEquals("inner", ex.getCause().getMessage());
    }

    @Test
    public void testAbstractClassWithFactoryCreator() throws Exception {
        ObjectMapper mapper = mapper();
        JsonDeserializer<Object> deser = BeanDeserializerFactory.instance.createBeanDeserializer(
                context(mapper), type(mapper, AbstractWithCreator.class), description(mapper, AbstractWithCreator.class));
        assertNotNull(deser);

        AbstractWithCreator value = mapper.readValue("{\"x\":12}", AbstractWithCreator.class);
        assertNotNull(value);
        assertEquals(12, value.getX());
    }

    @Test
    public void testAbstractTypeWithAs() throws Exception {
        ObjectMapper mapper = mapper();
        PolyBase value = mapper.readValue("{\"x\":7}", PolyBase.class);
        assertTrue(value instanceof Impl);
        assertEquals(7, ((Impl) value).x);
    }

    @Test
    public void testBuilderBasedDeserializer() throws Exception {
        ObjectMapper mapper = mapper();
        BuilderBean value = mapper.readValue("{\"a\":3,\"b\":\"xyz\"}", BuilderBean.class);
        assertEquals(3, value.getA());
        assertEquals("xyz", value.getB());
    }

    @Test
    public void testCreatorProperties() throws Exception {
        ObjectMapper mapper = mapper();
        CreatorBean value = mapper.readValue("{\"id\":5,\"name\":\"n\"}", CreatorBean.class);
        assertEquals(5, value.getId());
        assertEquals("n", value.getName());
    }

    @Test
    public void testCreatorPropertyDoesNotConflictWithField() throws Exception {
        ObjectMapper mapper = mapper();
        CreatorWithField value = mapper.readValue("{\"x\":9}", CreatorWithField.class);
        assertEquals(9, value.x);
    }

    @Test
    public void testManagedBackReferenceProperties() throws Exception {
        ObjectMapper mapper = mapper();
        RefParent value = mapper.readValue("{\"id\":1,\"children\":[{\"id\":2}]}", RefParent.class);
        assertNotNull(value.children);
        assertEquals(1, value.children.size());
        assertSame(value, value.children.get(0).parent);
    }

    @Test
    public void testInjectableProperty() throws Exception {
        ObjectMapper mapper = mapper();
        mapper.setInjectableValues(new InjectableValues.Std().addValue("id", "injected"));
        InjectBean value = mapper.readValue("{\"name\":\"n\"}", InjectBean.class);
        assertEquals("injected", value.id);
        assertEquals("n", value.name);
    }

    @Test
    public void testObjectIdReader() throws Exception {
        ObjectMapper mapper = mapper();
        IdentBean value = mapper.readValue("{\"id\":3,\"name\":\"obj\"}", IdentBean.class);
        assertEquals(3, value.id);
        assertEquals("obj", value.name);
    }

    @Test
    public void testClassIgnoredProperties() throws Exception {
        ObjectMapper mapper = mapper();
        IgnoreBean value = mapper.readValue("{\"kept\":2,\"ignored\":99}", IgnoreBean.class);
        assertEquals(2, value.kept);
        assertEquals(0, value.ignored);
    }

    @Test
    public void testSetterlessCollectionProperty() throws Exception {
        ObjectMapper mapper = mapper();
        GetterOnlyBean value = mapper.readValue("{\"items\":[\"a\",\"b\"]}", GetterOnlyBean.class);
        assertNotNull(value.getItems());
        assertEquals(2, value.getItems().size());
        assertEquals("a", value.getItems().get(0));
    }

    @Test
    public void testAnySetterProperty() throws Exception {
        ObjectMapper mapper = mapper();
        AnySetterBean value = mapper.readValue("{\"unknown\":42}", AnySetterBean.class);
        assertTrue(value.getExtra().containsKey("unknown"));
        assertEquals(42, value.getExtra().get("unknown"));
    }
}