package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.jsontype.SubtypeResolver;
import com.fasterxml.jackson.databind.jsontype.impl.StdSubtypeResolver;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    public void testDefaultConstructorAndFactories() {
        ObjectMapper mapper = new ObjectMapper();
        assertNotNull(mapper.getFactory());
        assertNotNull(mapper.getTypeFactory());
        assertNotNull(mapper.getSerializationConfig());
        assertNotNull(mapper.getDeserializationConfig());
    }

    @Test
    public void testConstructorWithJsonFactory() {
        JsonFactory f = new JsonFactory();
        ObjectMapper mapper = new ObjectMapper(f);
        assertSame(f, mapper.getFactory());
    }

    @Test
    public void testCopyConstructor() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectMapper copy = new ObjectMapper(mapper);
        assertNotNull(copy);
        assertNotSame(mapper, copy);
    }

    @Test
    public void testGettersAndSetters() {
        JsonNodeFactory jnf = JsonNodeFactory.instance;
        objectMapper.setNodeFactory(jnf);
        assertSame(jnf, objectMapper.getNodeFactory());

        SubtypeResolver str = new StdSubtypeResolver();
        objectMapper.setSubtypeResolver(str);
        assertSame(str, objectMapper.getSubtypeResolver());

        TypeFactory tf = TypeFactory.defaultInstance();
        objectMapper.setTypeFactory(tf);
        assertSame(tf, objectMapper.getTypeFactory());

        assertNotNull(objectMapper.getSerializationInstance());
        assertNotNull(objectMapper.getDeserializationContext());
    }

    @Test
    public void testVisibilityConfig() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        VisibilityChecker<?> vc = mapper.getVisibilityChecker();
        assertNotNull(vc);

        mapper.setVisibility(new JsonAutoDetect.Value() {
            @Override
            public Class<JsonAutoDetect> annotationType() {
                return JsonAutoDetect.class;
            }
            @Override public JsonAutoDetect.Visibility getter() { return JsonAutoDetect.Visibility.ANY; }
            @Override public JsonAutoDetect.Visibility isGetter() { return JsonAutoDetect.Visibility.ANY; }
            @Override public JsonAutoDetect.Visibility setter() { return JsonAutoDetect.Visibility.ANY; }
            @Override public JsonAutoDetect.Visibility creator() { return JsonAutoDetect.Visibility.ANY; }
            @Override public JsonAutoDetect.Visibility field() { return JsonAutoDetect.Visibility.ANY; }
        });
        assertNotNull(mapper.getVisibilityChecker());
    }

    @Test
    public void testSimpleSerializationAndDeserialization() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put("key", "value");

        String json = objectMapper.writeValueAsString(map);
        assertTrue(json.contains("key"));
        assertTrue(json.contains("value"));

        Map<?, ?> result = objectMapper.readValue(json, Map.class);
        assertEquals("value", result.get("key"));
    }

    @Test
    public void testWriteAndReadValueVariousTypes() throws Exception {
        // File
        File tempFile = File.createTempFile("jackson-test", ".json");
        tempFile.deleteOnExit();

        objectMapper.writeValue(tempFile, "test-string");
        String readFromFile = objectMapper.readValue(tempFile, String.class);
        assertEquals("test-string", readFromFile);

        // StringReader / Writer
        StringWriter sw = new StringWriter();
        objectMapper.writeValue(sw, 12345);
        assertEquals("12345", sw.toString());

        int readInt = objectMapper.readValue(new StringReader("12345"), int.class);
        assertEquals(12345, readInt);

        // byte[]
        byte[] bytes = objectMapper.writeValueAsBytes(true);
        boolean readBool = objectMapper.readValue(bytes, boolean.class);
        assertTrue(readBool);
    }

    @Test
    public void testReadValueWithJavaTypeAndTypeReference() throws Exception {
        String json = "[1, 2, 3]";
        JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, Integer.class);
        List<Integer> list = objectMapper.readValue(json, type);
        assertEquals(3, list.size());
        assertEquals(Integer.valueOf(2), list.get(1));

        List<Integer> list2 = objectMapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<List<Integer>>() {});
        assertEquals(3, list2.size());

        // Type
        Type t = List.class;
        JavaType jt = objectMapper.constructType(t);
        assertNotNull(jt);
    }

    @Test
    public void testTreeModelCreationAndReading() throws Exception {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("name", "Jackson");
        ArrayNode array = root.putArray("items");
        array.add(1);

        String json = objectMapper.writeValueAsString(root);
        JsonNode node = objectMapper.readTree(json);
        assertTrue(node.isObject());
        assertEquals("Jackson", node.get("name"].asText());
        assertEquals(1, node.get("items").size());

        JsonNode tree = objectMapper.valueToTree(mapOf("a", 1));
        assertTrue(tree.isObject());

        Map<?, ?> converted = objectMapper.treeToValue(tree, Map.class);
        assertEquals(1, converted.get("a"));
    }

    @Test
    public void testMapperFeatureControl() {
        boolean enabled = objectMapper.isEnabled(MapperFeature.USE_ANNOTATIONS);
        objectMapper.configure(MapperFeature.USE_ANNOTATIONS, !enabled);
        assertEquals(!enabled, objectMapper.isEnabled(MapperFeature.USE_ANNOTATIONS));

        objectMapper.enable(MapperFeature.USE_ANNOTATIONS);
        assertTrue(objectMapper.isEnabled(MapperFeature.USE_ANNOTATIONS));

        objectMapper.disable(MapperFeature.USE_ANNOTATIONS);
        assertFalse(objectMapper.isEnabled(MapperFeature.USE_ANNOTATIONS));
    }

    @Test
    public void testSerializationFeatureControl() {
        boolean enabled = objectMapper.isEnabled(SerializationFeature.INDENT_OUTPUT);
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, !enabled);
        assertEquals(!enabled, objectMapper.isEnabled(SerializationFeature.INDENT_OUTPUT));

        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        assertTrue(objectMapper.isEnabled(SerializationFeature.INDENT_OUTPUT));

        objectMapper.disable(SerializationFeature.INDENT_OUTPUT);
        assertFalse(objectMapper.isEnabled(SerializationFeature.INDENT_OUTPUT));
    }

    @Test
    public void testDeserializationFeatureControl() {
        boolean enabled = objectMapper.isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        objectMapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, !enabled);
        assertEquals(!enabled, objectMapper.isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS));

        objectMapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        assertTrue(objectMapper.isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS));

        objectMapper.disable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        assertFalse(objectMapper.isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS));
    }

    @Test
    public void testJsonParserAndGeneratorFeature() {
        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        assertTrue(objectMapper.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));

        objectMapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, false);
        assertFalse(objectMapper.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));
    }

    @Test
    public void testCustomSerializerAndDeserializerProviders() {
        DefaultSerializerProvider.Impl sp = new DefaultSerializerProvider.Impl();
        ObjectMapper mapper = objectMapper.setSerializerProvider(sp);
        assertSame(objectMapper, mapper);

        SerializerFactory sf = objectMapper.getSerializerFactory();
        assertNotNull(sf);
        ObjectMapper mapper2 = objectMapper.setSerializerFactory(sf);
        assertSame(objectMapper, mapper2);
    }

    @Test
    public void testRegisterModuleAndSubtypes() {
        objectMapper.registerModule(new com.fasterxml.jackson.databind.module.SimpleModule());
        objectMapper.registerModules(new ArrayList<com.fasterxml.jackson.databind.Module>());
        
        objectMapper.registerSubtypes(SimpleBean.class);
        objectMapper.registerSubtypes(new Class<?>[] { SimpleBean.class });
    }

    @Test
    public void testWriterAndReaderGeneration() {
        ObjectWriter writer = objectMapper.writer();
        assertNotNull(writer);

        ObjectWriter writerWithConfig = objectMapper.writer(SerializationFeature.INDENT_OUTPUT);
        assertNotNull(writerWithConfig);

        ObjectReader reader = objectMapper.reader();
        assertNotNull(reader);

        ObjectReader readerForType = objectMapper.readerFor(SimpleBean.class);
        assertNotNull(readerForType);
    }

    @Test
    public void testFindAndAddModules() {
        assertNotNull(ObjectMapper.findModules());
        assertNotNull(objectMapper.findAndAddModules());
    }

    @Test
    public void testJsonSchemaGeneration() throws Exception {
        JsonSchema schema = objectMapper.generateJsonSchema(SimpleBean.class);
        assertNotNull(schema);
    }

    @Test
    public void testCanSerializeAndDeserialize() {
        assertTrue(objectMapper.canSerialize(String.class));
        assertTrue(objectMapper.canDeserialize(objectMapper.constructType(String.class)));
    }

    private static Map<String, Object> mapOf(String key, Object value) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(key, value);
        return map;
    }

    public static class SimpleBean {
        public String name;
        public int id;
    }
}