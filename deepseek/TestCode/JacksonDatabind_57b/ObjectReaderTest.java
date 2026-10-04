package com.fasterxml.jackson.databind;

import static org.junit.Assert.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.junit.Before;
import org.junit.Test;

public class ObjectReaderTest {

    private ObjectMapper mapper;
    private ObjectReader reader;

    // ------ Test POJOs ------
    public static class SimpleBean {
        public int id;
        public String name;

        public SimpleBean() { }
        public SimpleBean(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public static class ViewA { }
    public static class ViewB { }

    public static class BeanWithView {
        @JsonView(ViewA.class)
        public String name;
        @JsonView(ViewB.class)
        public int id;
    }

    public static class BeanWithRoot {
        public String value;
    }

    public static abstract class Animal {
        public String name;
    }

    public static class Dog extends Animal {
        public String breed;
    }

    public static class Zoo {
        public Animal animal;
    }

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        reader = mapper.readerFor(SimpleBean.class);
    }

    // ---------- Basic readValue tests ----------

    @Test
    public void testReadValueFromString() throws Exception {
        SimpleBean bean = reader.readValue("{\"id\":1,\"name\":\"test\"}");
        assertNotNull(bean);
        assertEquals(1, bean.id);
        assertEquals("test", bean.name);
    }

    @Test
    public void testReadValueFromBytes() throws Exception {
        byte[] bytes = "{\"id\":2,\"name\":\"bytes\"}".getBytes("UTF-8");
        SimpleBean bean = reader.readValue(bytes);
        assertNotNull(bean);
        assertEquals(2, bean.id);
        assertEquals("bytes", bean.name);
    }

    @Test
    public void testReadValueFromFile() throws Exception {
        File temp = File.createTempFile("test", ".json");
        try {
            FileWriter writer = new FileWriter(temp);
            writer.write("{\"id\":3,\"name\":\"file\"}");
            writer.close();
            SimpleBean bean = reader.readValue(temp);
            assertNotNull(bean);
            assertEquals(3, bean.id);
            assertEquals("file", bean.name);
        } finally {
            temp.delete();
        }
    }

    @Test
    public void testReadValueFromInputStream() throws Exception {
        String json = "{\"id\":4,\"name\":\"stream\"}";
        InputStream in = new ByteArrayInputStream(json.getBytes("UTF-8"));
        SimpleBean bean = reader.readValue(in);
        assertNotNull(bean);
        assertEquals(4, bean.id);
        assertEquals("stream", bean.name);
    }

    @Test
    public void testReadValueFromReader() throws Exception {
        String json = "{\"id\":5,\"name\":\"reader\"}";
        Reader r = new StringReader(json);
        SimpleBean bean = reader.readValue(r);
        assertNotNull(bean);
        assertEquals(5, bean.id);
        assertEquals("reader", bean.name);
    }

    @Test
    public void testReadValueFromJsonParser() throws Exception {
        String json = "{\"id\":6,\"name\":\"parser\"}";
        com.fasterxml.jackson.core.JsonParser p = mapper.getFactory().createParser(json);
        SimpleBean bean = reader.readValue(p);
        assertNotNull(bean);
        assertEquals(6, bean.id);
        assertEquals("parser", bean.name);
        // ensures parser is closed by reader? Could be expected.
    }

    // ---------- Type parameter tests ----------

    @Test
    public void testReadValueWithTypeReference() throws Exception {
        ObjectReader typeReader = mapper.readerFor(new TypeReference<List<SimpleBean>>() { });
        List<SimpleBean> beans = typeReader.readValue("[{\"id\":1,\"name\":\"a\"},{\"id\":2,\"name\":\"b\"}]");
        assertNotNull(beans);
        assertEquals(2, beans.size());
        assertEquals(1, beans.get(0).id);
        assertEquals("b", beans.get(1).name);
    }

    @Test
    public void testReadValueWithJavaType() throws Exception {
        JavaType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, SimpleBean.class);
        ObjectReader mapReader = mapper.readerFor(type);
        Map<String, SimpleBean> map = mapReader.readValue("{\"x\":{\"id\":7,\"name\":\"seven\"}}");
        assertNotNull(map);
        assertEquals(1, map.size());
        assertEquals(7, map.get("x").id);
    }

    // ---------- Configuration methods tests ----------

    @Test
    public void testWithView() throws Exception {
        ObjectReader viewReader = mapper.readerFor(BeanWithView.class)
                .withView(ViewA.class);
        // Only name should be set, id remains default 0
        BeanWithView bean = viewReader.readValue("{\"name\":\"view\",\"id\":99}");
        assertEquals("view", bean.name);
        assertEquals(0, bean.id); // because id is in ViewB only
    }

    @Test
    public void testWithRootName() throws Exception {
        ObjectReader rootReader = mapper.readerFor(BeanWithRoot.class)
                .withRootName("root");
        BeanWithRoot bean = rootReader.readValue("{\"root\":{\"value\":\"rooted\"}}");
        assertNotNull(bean);
        assertEquals("rooted", bean.value);
    }

    @Test
    public void testWithLocale() throws Exception {
        ObjectReader localeReader = mapper.readerFor(SimpleBean.class)
                .with(Locale.FRENCH);
        SimpleBean bean = localeReader.readValue("{\"id\":1,\"name\":\"test\"}");
        assertNotNull(bean);
        assertEquals("test", bean.name);
    }

    @Test
    public void testWithTimeZone() throws Exception {
        ObjectReader tzReader = mapper.readerFor(SimpleBean.class)
                .with(TimeZone.getTimeZone("GMT"));
        SimpleBean bean = tzReader.readValue("{\"id\":1,\"name\":\"test\"}");
        assertNotNull(bean);
    }

    @Test
    public void testWithAttribute() throws Exception {
        ObjectReader attrReader = mapper.readerFor(SimpleBean.class)
                .withAttribute("key", "value");
        // Attribute doesn't affect deserialization but should not fail
        SimpleBean bean = attrReader.readValue("{\"id\":1,\"name\":\"test\"}");
        assertNotNull(bean);
        // Also check that attributes are copied to new instance
        assertEquals("value", attrReader.getAttributes().getAttribute("key"));
    }

    @Test
    public void testWithAndWithoutFeatures() throws Exception {
        // Test enabling/disabling a feature
        ObjectReader withFeature = mapper.readerFor(SimpleBean.class)
                .with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // This should still work, as the feature is enabled by default, but we set it explicitly
        SimpleBean bean = withFeature.readValue("{\"id\":1,\"name\":\"test\",\"unknown\":1}");
        assertNotNull(bean);
    }

    @Test
    public void testWithoutFeature() throws Exception {
        // Disable unknown property detection; should ignore unknown fields
        ObjectReader withoutFeature = mapper.readerFor(SimpleBean.class)
                .without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        SimpleBean bean = withoutFeature.readValue("{\"id\":1,\"name\":\"test\",\"unknown\":1}");
        assertNotNull(bean);
        assertEquals(1, bean.id);
    }

    // ---------- Edge cases and null/error handling ----------

    @Test(expected = IllegalArgumentException.class)
    public void testReadValueNullString() throws Exception {
        reader.readValue((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadValueNullBytes() throws Exception {
        reader.readValue((byte[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadValueNullInputStream() throws Exception {
        reader.readValue((InputStream) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadValueNullReader() throws Exception {
        reader.readValue((Reader) null);
    }

    @Test
    public void testReadEmptyString() throws Exception {
        try {
            reader.readValue("");
            fail("Should throw an exception for empty input");
        } catch (Exception e) {
            // expected
        }
    }

    @Test(expected = com.fasterxml.jackson.core.JsonProcessingException.class)
    public void testReadInvalidJson() throws Exception {
        reader.readValue("{\"id\":1,,}");
    }

    @Test
    public void testReadWithNullNode() throws Exception {
        // Reading a JSON null should return null for an object type
        ObjectReader nullReader = mapper.readerFor(SimpleBean.class);
        SimpleBean bean = nullReader.readValue("null");
        assertNull(bean);
    }

    // ---------- Polymorphic deserialization ----------

    @Test
    public void testReadValueWithPolymorphic() throws Exception {
        ObjectMapper mapperWithTyping = new ObjectMapper();
        mapperWithTyping.enableDefaultTyping();
        ObjectReader zooReader = mapperWithTyping.readerFor(Zoo.class);
        String json = "{\"animal\":{\"@class\":\"com.fasterxml.jackson.databind.ObjectReaderTest$Dog\",\"name\":\"Rex\",\"breed\":\"Lab\"}}";
        Zoo zoo = zooReader.readValue(json);
        assertNotNull(zoo);
        assertTrue(zoo.animal instanceof Dog);
        Dog dog = (Dog) zoo.animal;
        assertEquals("Rex", dog.name);
        assertEquals("Lab", dog.breed);
    }

    // ---------- Date handling ----------

    @Test
    public void testReadValueWithDate() throws Exception {
        ObjectReader dateReader = mapper.readerFor(SimpleBean.class);
        String json = "{\"id\":1,\"name\":\"test\"}";
        SimpleBean bean = dateReader.readValue(json);
        assertNotNull(bean);
        // No date fields, but ensure it works
    }

    // ---------- Immutability of ObjectReader ----------

    @Test
    public void testReaderImmutability() throws Exception {
        ObjectReader original = mapper.readerFor(SimpleBean.class);
        ObjectReader modified = original.withRootName("root");
        assertNotSame(original, modified);
        // Original should not have root name
        assertNull(original.getConfig().getRootName());
        // Modified should have it
        assertNotNull(modified.getConfig().getRootName());
    }

    // ---------- ReadValue with value update ----------

    @Test
    public void testReadValueWithValueToUpdate() throws Exception {
        SimpleBean target = new SimpleBean();
        ObjectReader updater = mapper.readerForUpdating(target);
        SimpleBean result = updater.readValue("{\"id\":99,\"name\":\"updated\"}");
        assertSame(target, result);
        assertEquals(99, target.id);
        assertEquals("updated", target.name);
    }

    // ---------- Additional tests for coverage ----------

    @Test
    public void testReadValueFromJsonNode() throws Exception {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("id", 123);
        node.put("name", "node");
        ObjectReader nodeReader = mapper.readerFor(SimpleBean.class);
        SimpleBean bean = nodeReader.readValue(node);
        assertNotNull(bean);
        assertEquals(123, bean.id);
        assertEquals("node", bean.name);
    }

    @Test
    public void testReadValueFromDataInput() throws Exception {
        // DataInput is a JDK 7+ interface, but Jackson supports it.
        byte[] bytes = "{\"id\":55,\"name\":\"data\"}".getBytes("UTF-8");
        DataInput input = new DataInputStream(new ByteArrayInputStream(bytes));
        ObjectReader dataReader = mapper.readerFor(SimpleBean.class);
        SimpleBean bean = dataReader.readValue(input);
        assertNotNull(bean);
        assertEquals(55, bean.id);
        assertEquals("data", bean.name);
    }

    // ---------- Tests to trigger bug #57 specific scenarios ----------
    // Based on Defects4J, bug 57 may involve reading with a TypeReference
    // and generics, or handling of nulls in collections.
    // We'll add a few more tests that exercise those paths.

    @Test
    public void testReadGenericListWithNullElement() throws Exception {
        ObjectReader listReader = mapper.readerFor(new TypeReference<List<SimpleBean>>() { });
        List<SimpleBean> beans = listReader.readValue("[null,{\"id\":2,\"name\":\"two\"}]");
        assertNotNull(beans);
        assertEquals(2, beans.size());
        assertNull(beans.get(0));
        assertNotNull(beans.get(1));
    }

    @Test
    public void testReadMapWithNullValue() throws Exception {
        JavaType mapType = mapper.getTypeFactory().constructMapType(HashMap.class, String.class, SimpleBean.class);
        ObjectReader mapReader = mapper.readerFor(mapType);
        Map<String, SimpleBean> map = mapReader.readValue("{\"a\":null,\"b\":{\"id\":10,\"name\":\"ten\"}}");
        assertNotNull(map);
        assertEquals(2, map.size());
        assertTrue(map.containsKey("a"));
        assertNull(map.get("a"));
        assertEquals(10, map.get("b").id);
    }

    @Test
    public void testReadValueFromEmptyArray() throws Exception {
        ObjectReader listReader = mapper.readerFor(new TypeReference<List<Integer>>() { });
        List<Integer> list = listReader.readValue("[]");
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    // ---------- Additional edge: unknown properties in different config ----------

    @Test
    public void testUnknownPropertiesIgnoredWhenFeatureDisabled() throws Exception {
        ObjectReader lenientReader = mapper.readerFor(SimpleBean.class)
                .without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        SimpleBean bean = lenientReader.readValue("{\"id\":1,\"name\":\"ok\",\"extra\":\"ignored\"}");
        assertNotNull(bean);
        assertEquals(1, bean.id);
    }

    // ---------- Tests for getConfig and getTypeFactory ----------

    @Test
    public void testGetConfigNotNull() {
        assertNotNull(reader.getConfig());
    }

    @Test
    public void testGetTypeFactoryNotNull() {
        assertNotNull(reader.getTypeFactory());
    }

    // ---------- Tests for withType and withValueToUpdate ----------

    @Test
    public void testWithType() throws Exception {
        ObjectReader typedReader = mapper.readerFor(SimpleBean.class)
                .withType(SimpleBean.class);
        SimpleBean bean = typedReader.readValue("{\"id\":1,\"name\":\"type\"}");
        assertNotNull(bean);
        assertEquals(1, bean.id);
    }

    // ---------- Test for readValue with JsonNode input when base type is JsonNode ----------

    @Test
    public void testReadValueIntoJsonNode() throws Exception {
        ObjectReader nodeReader = mapper.readerFor(JsonNode.class);
        JsonNode node = nodeReader.readValue("{\"key\":\"value\"}");
        assertNotNull(node);
        assertEquals("value", node.get("key").asText());
    }
}