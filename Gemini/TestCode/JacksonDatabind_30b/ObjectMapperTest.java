package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Map;

import static org.junit.Assert.*;

public class ObjectMapperTest {

    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
    }

    @After
    public void tearDown() {
        objectMapper = null;
    }

    @Test
    public void testDefaultConstructorAndFactory() {
        ObjectMapper mapper1 = new ObjectMapper();
        assertNotNull(mapper1);
        assertNotNull(mapper1.getFactory());

        JsonFactory jsonFactory = new JsonFactory();
        ObjectMapper mapper2 = new ObjectMapper(jsonFactory);
        assertNotNull(mapper2);
        assertEquals(jsonFactory, mapper2.getFactory());
    }

    @Test
    public void testCopyConstructorAndCopy() {
        ObjectMapper copy = new ObjectMapper(objectMapper);
        assertNotNull(copy);
        assertNotSame(objectMapper, copy);

        ObjectMapper deepCopy = objectMapper.copy();
        assertNotNull(deepCopy);
        assertNotSame(objectMapper, deepCopy);
    }

    @Test
    public void testVersion() {
        assertNotNull(objectMapper.version());
    }

    @Test
    public void testGettersAndSetters() {
        assertNotNull(objectMapper.getSerializationConfig());
        assertNotNull(objectMapper.getDeserializationConfig());
        assertNotNull(objectMapper.getDeserializationContext());
        assertNotNull(objectMapper.tokenStreamFactory());
        assertNotNull(objectMapper.getInjectableValues());
        assertNotNull(objectMapper.getSubtypeResolver());
    }

    @Test
    public void testSerializationBasic() throws Exception {
        SimpleBean bean = new SimpleBean();
        bean.setName("test");
        bean.setValue(123);

        String json = objectMapper.writeValueAsString(bean);
        assertNotNull(json);
        assertTrue(json.contains("test"));
        assertTrue(json.contains("123"));

        byte[] bytes = objectMapper.writeValueAsBytes(bean);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    public void testDeserializationBasic() throws Exception {
        String json = "{\"name\":\"test\",\"value\":123}";
        SimpleBean bean = objectMapper.readValue(json, SimpleBean.class);
        assertNotNull(bean);
        assertEquals("test", bean.getName());
        assertEquals(123, bean.getValue());

        byte[] bytes = json.getBytes("UTF-8");
        SimpleBean beanBytes = objectMapper.readValue(bytes, SimpleBean.class);
        assertNotNull(beanBytes);
        assertEquals("test", beanBytes.getName());
    }

    @Test
    public void testReadValueWithTypeReference() throws Exception {
        String json = "{\"name\":\"test\",\"value\":456}";
        TypeReference<SimpleBean> typeRef = new TypeReference<SimpleBean>() {};
        SimpleBean bean = objectMapper.readValue(json, typeRef);
        assertNotNull(bean);
        assertEquals(456, bean.getValue());
    }

    @Test
    public void testReadValueWithJavaType() throws Exception {
        String json = "{\"name\":\"test\",\"value\":789}";
        JavaType javaType = objectMapper.constructType(SimpleBean.class);
        SimpleBean bean = objectMapper.readValue(json, javaType);
        assertNotNull(bean);
        assertEquals(789, bean.getValue());
    }

    @Test
    public void testReadTree() throws Exception {
        String json = "{\"treeKey\":\"treeVal\"}";
        JsonNode node = objectMapper.readTree(json);
        assertNotNull(node);
        assertTrue(node.isObject());
        assertEquals("treeVal", node.get("treeKey").asText());

        JsonParser parser = objectMapper.getFactory().createParser(json);
        JsonNode nodeFromParser = objectMapper.readTree(parser);
        assertNotNull(nodeFromParser);
        assertEquals("treeVal", nodeFromParser.get("treeKey").asText());
    }

    @Test
    public void testCreateObjectNodeAndArrayNode() {
        ObjectNode objectNode = objectMapper.createObjectNode();
        assertNotNull(objectNode);

        assertNotNull(objectMapper.createArrayNode());
        assertNotNull(objectMapper.getJsonFactory());
    }

    @Test
    public void testValueToTreeAndTreeToValue() throws Exception {
        SimpleBean bean = new SimpleBean();
        bean.setValue(999);

        JsonNode node = objectMapper.valueToTree(bean);
        assertNotNull(node);
        assertEquals(999, node.get("value").asInt());

        SimpleBean convertedBean = objectMapper.treeToValue(node, SimpleBean.class);
        assertNotNull(convertedBean);
        assertEquals(999, convertedBean.getValue());
    }

    @Test
    public void testReadValueSources() throws Exception {
        File tempFile = File.createTempFile("test-jackson", ".json");
        tempFile.deleteOnExit();
        java.nio.file.Files.write(tempFile.toPath(), "{\"value\":55}".getBytes("UTF-8"));

        SimpleBean b1 = objectMapper.readValue(tempFile, SimpleBean.class);
        assertEquals(55, b1.getValue());

        URL url = tempFile.toURI().toURL();
        SimpleBean b2 = objectMapper.readValue(url, SimpleBean.class);
        assertEquals(55, b2.getValue());

        try (InputStream in = java.nio.file.Files.newInputStream(tempFile.toPath())) {
            SimpleBean b3 = objectMapper.readValue(in, SimpleBean.class);
            assertEquals(55, b3.getValue());
        }

        try (Reader reader = new java.io.InputStreamReader(java.nio.file.Files.newInputStream(tempFile.toPath()), "UTF-8")) {
            SimpleBean b4 = objectMapper.readValue(reader, SimpleBean.class);
            assertEquals(55, b4.getValue());
        }
    }

    @Test
    public void testWriterAndReaderOperations() {
        assertNotNull(objectMapper.writer());
        assertNotNull(objectMapper.writerWithDefaultPrettyPrinter());
        assertNotNull(objectMapper.reader());
        assertNotNull(objectMapper.readerFor(SimpleBean.class));
    }

    @Test
    public void testConfigOverridesAndMixIns() {
        objectMapper.addMixIn(SimpleBean.class, DummyMixIn.class);
        assertNotNull(objectMapper.findMixInClassFor(SimpleBean.class));

        assertNotNull(objectMapper.configOverride(SimpleBean.class));
    }

    // Helper classes for testing
    public static class SimpleBean {
        private String name;
        private int value;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
    }

    private interface DummyMixIn {
    }
}