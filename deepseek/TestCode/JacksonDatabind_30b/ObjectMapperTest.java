package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * JUnit 4 test suite for {@link ObjectMapper}.
 */
public class ObjectMapperTest {

    @Test
    public void testBasicPojoSerializationDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        Product product = new Product(42, "widget");
        product.tags = Arrays.asList("a", "b");

        String json = mapper.writeValueAsString(product);
        Product result = mapper.readValue(json, Product.class);

        assertEquals(product.id, result.id);
        assertEquals(product.name, result.name);
        assertEquals(product.tags, result.tags);
    }

    @Test
    public void testGenericCollectionDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        Map<String, List<Integer>> data = mapper.readValue(
                "{\"a\":[1,2,3],\"b\":[4,5]}",
                new TypeReference<Map<String, List<Integer>>>() {
                });

        assertEquals(Arrays.asList(1, 2, 3), data.get("a"));
        assertEquals(Arrays.asList(4, 5), data.get("b"));
    }

    @Test
    public void testWriteValueAsStringNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        assertEquals("null", mapper.writeValueAsString(null));
    }

    @Test
    public void testReadValueNullLiteral() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        assertNull(mapper.readValue("null", Product.class));
    }

    @Test
    public void testConfigureFeatures() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        assertTrue(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT));

        mapper.disable(SerializationFeature.INDENT_OUTPUT);
        assertFalse(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT));

        mapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        assertTrue(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));

        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        assertFalse(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
    }

    @Test
    public void testPropertyNamingStrategy() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_SNAKE_CASE);

        Product product = new Product(7, "gadget");
        String json = mapper.writeValueAsString(product);

        assertTrue(json.contains("\"id\":7"));
        assertTrue(json.contains("\"name\":\"gadget\""));

        Product result = mapper.readValue("{\"id\":9,\"name\":\"thing\"}", Product.class);
        assertEquals(9, result.id);
        assertEquals("thing", result.name);
    }

    @Test
    public void testCustomModuleSerializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addSerializer(CustomBean.class, new CustomBeanSerializer());
        mapper.registerModule(module);

        String json = mapper.writeValueAsString(new CustomBean("value"));
        assertEquals("{\"custom\":\"value\"}", json);
    }

    @Test
    public void testReaderForUpdating() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        Product product = new Product(0, "initial");
        product = mapper.readerForUpdating(product).readValue("{\"id\":5}");

        assertEquals(5, product.id);
    }

    @Test
    public void testTreeConversion() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode node = mapper.createObjectNode();
        node.put("id", 11);
        node.put("name", "tree");

        Product product = mapper.treeToValue(node, Product.class);
        assertEquals(11, product.id);
        assertEquals("tree", product.name);

        String json = mapper.writeValueAsString(product);
        assertTrue(json.contains("\"id\":11"));
    }

    @Test
    public void testReadValueFromInputStreamAndFile() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        Product product = mapper.readValue(
                new ByteArrayInputStream("{\"id\":1,\"name\":\"stream\"}".getBytes("UTF-8")),
                Product.class);
        assertEquals(1, product.id);
        assertEquals("stream", product.name);

        File tmp = File.createTempFile("object-mapper-test", ".json");
        try {
            FileOutputStream out = new FileOutputStream(tmp);
            out.write("{\"id\":2,\"name\":\"file\"}".getBytes("UTF-8"));
            out.close();

            Product fromFile = mapper.readValue(tmp, Product.class);
            assertEquals(2, fromFile.id);
            assertEquals("file", fromFile.name);
        } finally {
            tmp.delete();
        }
    }

    @Test
    public void testReadValueWithJsonParser() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        JsonParser parser = mapper.getFactory().createParser("{\"id\":3,\"name\":\"parser\"}");
        Product product = mapper.readValue(parser, Product.class);

        assertEquals(3, product.id);
        assertEquals("parser", product.name);
    }

    @Test
    public void testEnumSerializationDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        String json = mapper.writeValueAsString(Status.READY);
        assertEquals("\"READY\"", json);

        Status status = mapper.readValue("\"READY\"", Status.class);
        assertEquals(Status.READY, status);
    }

    @Test
    public void testArrayDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        int[] values = mapper.readValue("[1,2,3]", int[].class);
        assertEquals(3, values.length);
        assertEquals(2, values[1]);
    }

    @Test
    public void testMapWithObjectValues() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> map = mapper.readValue(
                "{\"id\":1,\"name\":\"map\",\"tags\":[\"x\",\"y\"]}",
                new TypeReference<Map<String, Object>>() {
                });

        assertEquals(1, ((Number) map.get("id")).intValue());
        assertEquals("map", map.get("name"));
        assertTrue(map.get("tags") instanceof List<?>);
    }

    @Test
    public void testUnknownPropertiesIgnore() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        Product product = mapper.readValue("{\"id\":4,\"name\":\"ok\",\"unknown\":123}", Product.class);
        assertEquals(4, product.id);
        assertEquals("ok", product.name);
    }

    @Test
    public void testMixInAnnotationsArePreservedByCopy() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixInAnnotations(Product.class, ProductMixIn.class);

        String originalJson = mapper.writeValueAsString(new Product(1, "mix"));
        assertFalse(originalJson.contains("secret"));

        ObjectMapper copy = mapper.copy();
        String copiedJson = copy.writeValueAsString(new Product(1, "mix"));
        assertFalse(copiedJson.contains("secret"));
        assertEquals(originalJson, copiedJson);
    }

    @Test
    public void testCopyKeepsFeatureSettingsAndIndependentChanges() throws Exception {
        ObjectMapper original = new ObjectMapper();
        original.enable(SerializationFeature.INDENT_OUTPUT);

        ObjectMapper copy = original.copy();
        assertTrue(copy.isEnabled(SerializationFeature.INDENT_OUTPUT));

        copy.disable(SerializationFeature.INDENT_OUTPUT);
        assertFalse(copy.isEnabled(SerializationFeature.INDENT_OUTPUT));
        assertTrue(original.isEnabled(SerializationFeature.INDENT_OUTPUT));
    }

    @Test
    public void testDefaultTypingForPolymorphicValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();

        Object value = new Dog("Rex");
        String json = mapper.writeValueAsString(value);

        assertTrue(json.contains("Dog") || json.contains("ObjectMapperTest"));

        Object result = mapper.readValue(json, Object.class);
        assertTrue(result instanceof Dog);
        assertEquals("Rex", ((Dog) result).name);
    }

    @Test
    public void testNestedCollectionWithNulls() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        List<List<String>> nested = mapper.readValue(
                "[[\"a\"],null,[\"b\",\"c\"]]",
                new TypeReference<List<List<String>>>() {
                });

        assertEquals(Arrays.asList("a"), nested.get(0));
        assertNull(nested.get(1));
        assertEquals(Arrays.asList("b", "c"), nested.get(2));
    }

    @Test
    public void testDuplicatePropertyHandling() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Product product = mapper.readValue("{\"id\":1,\"id\":2,\"name\":\"dup\"}", Product.class);
        assertEquals(2, product.id);
        assertEquals("dup", product.name);
    }

    public static class Product {
        public int id;
        public String name;
        public List<String> tags;
        public String secret = "sensitive";

        public Product() {
        }

        public Product(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }
    }

    public static class CustomBean {
        public String value;

        public CustomBean() {
        }

        public CustomBean(String value) {
            this.value = value;
        }
    }

    public static class CustomBeanSerializer extends com.fasterxml.jackson.databind.JsonSerializer<CustomBean> {
        @Override
        public void serialize(CustomBean bean, JsonGenerator gen,
                com.fasterxml.jackson.databind.SerializerProvider serializers) throws java.io.IOException {
            gen.writeStartObject();
            gen.writeStringField("custom", bean.value);
            gen.writeEndObject();
        }
    }

    public abstract static class ProductMixIn {
        @JsonIgnore
        public abstract String getSecret();
    }

    public enum Status {
        READY,
        NOT_READY
    }

    public static class Dog {
        public String name;

        public Dog() {
        }

        public Dog(String name) {
            this.name = name;
        }
    }
}