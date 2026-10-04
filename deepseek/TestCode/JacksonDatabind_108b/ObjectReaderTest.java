package com.fasterxml.jackson.databind;

import static org.junit.Assert.*;

import java.io.*;
import java.net.URL;
import java.util.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.node.*;
import org.junit.*;

public class ObjectReaderTest {

    private ObjectMapper mapper;
    private ObjectReader reader;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        reader = mapper.reader();
    }

    // =============== Basic readValue(String) ===============

    @Test
    public void testReadValueStringSimple() throws Exception {
        String json = "{\"name\":\"test\"}";
        Map<String, Object> result = reader.forType(Map.class).readValue(json);
        assertEquals("test", result.get("name"));
    }

    @Test(expected = JsonParseException.class)
    public void testReadValueStringInvalidJson() throws Exception {
        reader.readValue("not json");
    }

    @Test(expected = JsonMappingException.class)
    public void testReadValueStringTypeMismatch() throws Exception {
        String json = "{\"name\":\"test\"}";
        reader.forType(Integer.class).readValue(json);
    }

    @Test
    public void testReadValueStringNull() throws Exception {
        // null should be handled as empty? or throw NPE? depends on implementation
        try {
            reader.readValue((String) null);
            fail("Expected NullPointerException or equivalent");
        } catch (Exception e) {
            assertTrue(e instanceof NullPointerException || e instanceof JsonParseException);
        }
    }

    @Test
    public void testReadValueStringEmpty() throws Exception {
        // empty string - should throw JsonParseException
        try {
            reader.readValue("");
            fail("Expected JsonParseException");
        } catch (JsonParseException e) {
            // ok
        }
    }

    // =============== readValue(byte[]) ===============

    @Test
    public void testReadValueByteArray() throws Exception {
        String json = "{\"a\":1}";
        byte[] bytes = json.getBytes("UTF-8");
        Map<?, ?> result = reader.forType(Map.class).readValue(bytes);
        assertEquals(1, result.get("a"));
    }

    @Test(expected = JsonParseException.class)
    public void testReadValueByteArrayInvalid() throws Exception {
        byte[] bytes = "invalid".getBytes("UTF-8");
        reader.readValue(bytes);
    }

    @Test
    public void testReadValueByteArrayNull() throws Exception {
        try {
            reader.readValue((byte[]) null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // ok
        }
    }

    // =============== readValue(InputStream) ===============

    @Test
    public void testReadValueInputStream() throws Exception {
        String json = "{\"b\":true}";
        InputStream is = new ByteArrayInputStream(json.getBytes("UTF-8"));
        Map<?, ?> result = reader.forType(Map.class).readValue(is);
        assertEquals(true, result.get("b"));
        is.close();
    }

    @Test(expected = JsonParseException.class)
    public void testReadValueInputStreamInvalid() throws Exception {
        InputStream is = new ByteArrayInputStream("{{{".getBytes("UTF-8"));
        reader.readValue(is);
        is.close();
    }

    @Test
    public void testReadValueInputStreamNull() throws Exception {
        try {
            reader.readValue((InputStream) null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // ok
        }
    }

    // =============== readValue(Reader) ===============

    @Test
    public void testReadValueReader() throws Exception {
        String json = "{\"c\":\"d\"}";
        StringReader sr = new StringReader(json);
        Map<?, ?> result = reader.forType(Map.class).readValue(sr);
        assertEquals("d", result.get("c"));
        sr.close();
    }

    @Test(expected = JsonParseException.class)
    public void testReadValueReaderInvalid() throws Exception {
        StringReader sr = new StringReader("not json");
        reader.readValue(sr);
        sr.close();
    }

    @Test
    public void testReadValueReaderNull() throws Exception {
        try {
            reader.readValue((Reader) null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // ok
        }
    }

    // =============== readValue(File) ===============

    @Test
    public void testReadValueFile() throws Exception {
        File tempFile = File.createTempFile("test", ".json");
        tempFile.deleteOnExit();
        Writer writer = new FileWriter(tempFile);
        writer.write("{\"file\":true}");
        writer.close();
        Map<?, ?> result = reader.forType(Map.class).readValue(tempFile);
        assertEquals(true, result.get("file"));
    }

    @Test(expected = FileNotFoundException.class)
    public void testReadValueFileNotFound() throws Exception {
        reader.readValue(new File("nonexistent.json"));
    }

    // =============== readValue(URL) ===============

    @Test
    public void testReadValueURL() throws Exception {
        // Use a local experimental URL? Skip for now, due to network dependency.
        // Instead test that it throws expected exception for invalid URL
        try {
            reader.readValue(new URL("http://localhost:9999/nonexistent"));
            fail("Expected IOException");
        } catch (IOException e) {
            // ok
        }
    }

    // =============== readTree ===============

    @Test
    public void testReadTreeSimple() throws Exception {
        String json = "{\"x\":1}";
        JsonNode node = reader.readTree(json);
        assertEquals(1, node.get("x").asInt());
    }

    @Test
    public void testReadTreeArray() throws Exception {
        String json = "[1,2,3]";
        ArrayNode node = (ArrayNode) reader.readTree(json);
        assertEquals(3, node.size());
    }

    @Test
    public void testReadTreeNull() throws Exception {
        assertNull(reader.readTree("null"));
    }

    @Test(expected = JsonParseException.class)
    public void testReadTreeInvalid() throws Exception {
        reader.readTree("broken");
    }

    // =============== readValues ===============

    @Test
    public void testReadValuesSequence() throws Exception {
        String json = "[1,2,3]";
        MappingIterator<Integer> it = reader.forType(Integer.class).readValues(json);
        assertTrue(it.hasNext());
        assertEquals(1, (int) it.nextValue());
        assertEquals(2, (int) it.nextValue());
        assertEquals(3, (int) it.nextValue());
        assertFalse(it.hasNext());
    }

    @Test
    public void testReadValuesEmptyArray() throws Exception {
        String json = "[]";
        MappingIterator<Integer> it = reader.forType(Integer.class).readValues(json);
        assertFalse(it.hasNext());
    }

    @Test(expected = JsonParseException.class)
    public void testReadValuesInvalid() throws Exception {
        MappingIterator<Integer> it = reader.forType(Integer.class).readValues("not an array");
        it.next();
    }

    // =============== with(DeserializationConfig) ===============

    @Test
    public void testWithConfig() throws Exception {
        DeserializationConfig config = mapper.getDeserializationConfig();
        ObjectReader newReader = reader.with(config);
        assertNotNull(newReader);
    }

    @Test
    public void testWithConfigNull() throws Exception {
        try {
            reader.with((DeserializationConfig) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // ok
        }
    }

    // =============== withHandler ===============

    @Test
    public void testWithHandler() throws Exception {
        DeserializationProblemHandler handler = new DeserializationProblemHandler() {
            // no override, default behavior
        };
        ObjectReader readerWithHandler = reader.with(handler);
        assertNotNull(readerWithHandler);
    }

    // =============== forType(Class) ===============

    @Test
    public void testForTypeClass() throws Exception {
        ObjectReader typed = reader.forType(String.class);
        assertEquals("hello", typed.readValue("\"hello\""));
    }

    @Test(expected = JsonMappingException.class)
    public void testForTypeClassMismatch() throws Exception {
        ObjectReader typed = reader.forType(Integer.class);
        typed.readValue("\"not a number\"");
    }

    // =============== forType(TypeReference) ===============

    @Test
    public void testForTypeTypeReference() throws Exception {
        TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {};
        ObjectReader typed = reader.forType(typeRef);
        String json = "{\"key\":\"value\"}";
        Map<String, Object> map = typed.readValue(json);
        assertEquals("value", map.get("key"));
    }

    // =============== withRootName ===============

    @Test
    public void testWithRootName() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
        ObjectReader reader = mapper.reader().withRootName("root");
        String json = "{\"root\":{\"inner\":5}}";
        Map<String, Object> result = reader.forType(Map.class).readValue(json);
        assertEquals(5, result.get("inner"));
    }

    // =============== with(JsonParser.Feature) ===============

    @Test
    public void testWithParserFeature() throws Exception {
        ObjectReader readerWithFeature = reader.with(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        String json = "{'a':1}";
        Map<?, ?> result = readerWithFeature.forType(Map.class).readValue(json);
        assertEquals(1, result.get("a"));
    }

    // =============== without(JsonParser.Feature) ===============

    @Test(expected = JsonParseException.class)
    public void testWithoutParserFeature() throws Exception {
        ObjectReader readerWithout = reader.without(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        readerWithout.readValue("{'a':1}"); // should fail because single quotes not allowed
    }

    // =============== with(DeserializationFeature) ===============

    @Test
    public void testWithDeserializationFeature() throws Exception {
        ObjectReader readerWith = reader.with(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        // not easy to test directly, but ensure no exception constructing
        assertNotNull(readerWith);
    }

    // =============== without(DeserializationFeature) ===============

    @Test
    public void testWithoutDeserializationFeature() throws Exception {
        ObjectReader readerWithout = reader.without(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        assertNotNull(readerWithout);
    }

    // =============== withAttributes / getAttributes ===============

    @Test
    public void testWithAttributes() throws Exception {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("attr1", "value1");
        ObjectReader readerWithAttrs = reader.withAttributes(attrs);
        assertEquals(attrs, readerWithAttrs.getAttributes());
    }

    @Test
    public void testGetAttributesDefault() throws Exception {
        assertNull(reader.getAttributes());
    }

    // =============== withAttribute / withoutAttribute ===============

    @Test
    public void testWithAttribute() throws Exception {
        ObjectReader readerWithAttr = reader.withAttribute("key", "val");
        assertNotNull(readerWithAttr.getAttributes());
        assertEquals("val", readerWithAttr.getAttributes().get("key"));
        ObjectReader readerWithout = readerWithAttr.withoutAttribute("key");
        assertFalse(readerWithout.getAttributes().containsKey("key"));
    }

    // =============== withValueToUpdate ===============

    @Test
    public void testWithValueToUpdate() throws Exception {
        StringBuilder sb = new StringBuilder("initial");
        ObjectReader readerWithUpdate = reader.withValueToUpdate(sb);
        // Can't easily test without custom deserializer, but ensure it doesn't crash
        assertNotNull(readerWithUpdate);
    }

    // =============== Type handling with generics ===============

    @Test
    public void testGenericList() throws Exception {
        String json = "[\"a\",\"b\"]";
        List<String> list = reader.forType(new TypeReference<List<String>>() {}).readValue(json);
        assertEquals(Arrays.asList("a", "b"), list);
    }

    @Test
    public void testGenericMap() throws Exception {
        String json = "{\"k1\":\"v1\",\"k2\":\"v2\"}";
        Map<String, String> map = reader.forType(new TypeReference<Map<String, String>>() {}).readValue(json);
        assertEquals("v1", map.get("k1"));
        assertEquals("v2", map.get("k2"));
    }

    // =============== readValue with root unwrapping ===============

    @Test
    public void testRootUnwrapping() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
        ObjectReader reader = mapper.reader().withRootName("root");
        String json = "{\"root\":{\"a\":1}}";
        Map<String, Object> result = reader.forType(Map.class).readValue(json);
        assertEquals(1, result.get("a"));
    }

    // =============== Exception handling: IOException in stream ===============

    @Test(expected = IOException.class)
    public void testReadValueStreamIOException() throws Exception {
        InputStream broken = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("simulated");
            }
        };
        reader.readValue(broken);
    }

    // =============== readValues with custom type ===============

    @Test
    public void testReadValuesCustomClass() throws Exception {
        String json = "[{\"x\":1},{\"x\":2}]";
        MappingIterator<SimpleBean> it = reader.forType(SimpleBean.class).readValues(json);
        assertTrue(it.hasNext());
        SimpleBean first = it.nextValue();
        assertEquals(1, first.x);
        SimpleBean second = it.nextValue();
        assertEquals(2, second.x);
        assertFalse(it.hasNext());
    }

    // =============== Test that reads Boolean from value ===============

    @Test
    public void testReadValueBoolean() throws Exception {
        assertEquals(true, reader.forType(Boolean.class).readValue("true"));
        assertEquals(false, reader.forType(Boolean.class).readValue("false"));
    }

    // =============== readValue with null token ===============

    @Test
    public void testReadValueJsonTokenNull() throws Exception {
        JsonParser jp = new JsonFactory().createParser("null");
        assertNull(reader.readValue(jp));
    }

    // =============== Test with JsonNode -> ArrayNode ===============

    @Test
    public void testReadTreeArrayEmpty() throws Exception {
        JsonNode node = reader.readTree("[]");
        assertTrue(node.isArray());
        assertEquals(0, node.size());
    }

    // =============== Test with JsonNode -> ObjectNode ===============

    @Test
    public void testReadTreeObjectEmpty() throws Exception {
        JsonNode node = reader.readTree("{}");
        assertTrue(node.isObject());
        assertEquals(0, node.size());
    }

    // =============== Test readValue with JsonParser directly ===============

    @Test
    public void testReadValueWithJsonParser() throws Exception {
        String json = "{\"val\":42}";
        JsonFactory factory = new JsonFactory();
        JsonParser jp = factory.createParser(json);
        jp.nextToken(); // advance to START_OBJECT
        Map<String, Object> result = reader.forType(Map.class).readValue(jp);
        assertEquals(42, result.get("val"));
    }

    // =============== Test readValue with byte array offset/length ===============

    @Test
    public void testReadValueByteArrayOffsetLength() throws Exception {
        byte[] bytes = "{\"offset\":\"test\"}".getBytes("UTF-8");
        // wrap with extra bytes
        byte[] wrapped = new byte[bytes.length + 10];
        System.arraycopy(bytes, 0, wrapped, 5, bytes.length);
        Map<?, ?> result = reader.forType(Map.class).readValue(wrapped, 5, bytes.length);
        assertEquals("test", result.get("offset"));
    }

    // =============== Test readValue with Reader and buffer ===============

    @Test
    public void testReadValueReaderWithBuffer() throws Exception {
        String json = "{\"buffered\":true}";
        StringReader sr = new StringReader(json);
        Map<?, ?> result = reader.forType(Map.class).readValue(sr);
        assertEquals(true, result.get("buffered"));
        sr.close();
    }

    // =============== Test that reader is reusable ===============

    @Test
    public void testReaderReusability() throws Exception {
        ObjectReader r = reader.forType(Map.class);
        Map<?, ?> first = r.readValue("{\"a\":1}");
        assertEquals(1, first.get("a"));
        Map<?, ?> second = r.readValue("{\"b\":2}");
        assertEquals(2, second.get("b"));
    }

    // =============== Test that reader can handle different types ===============

    @Test
    public void testReaderMultipleTypes() throws Exception {
        ObjectReader stringReader = reader.forType(String.class);
        assertEquals("hello", stringReader.readValue("\"hello\""));
        ObjectReader intReader = reader.forType(Integer.class);
        assertEquals(123, (int) intReader.readValue("123"));
    }

    // =============== Test with null token in readValues ===============

    @Test
    public void testReadValuesNullToken() throws Exception {
        String json = "[null, 1, null]";
        MappingIterator<Integer> it = reader.forType(Integer.class).readValues(json);
        assertNull(it.nextValue());
        assertEquals(1, (int) it.nextValue());
        assertNull(it.nextValue());
        assertFalse(it.hasNext());
    }

    // =============== Test ObjectReader copy ===============

    @Test
    public void testCopy() throws Exception {
        ObjectReader copy = reader.copy();
        assertNotNull(copy);
        Map<?, ?> result = copy.forType(Map.class).readValue("{\"copy\":true}");
        assertEquals(true, result.get("copy"));
    }

    // =============== Test with parser features enabled ===============

    @Test
    public void testWithFeatures() throws Exception {
        ObjectReader r = reader.with(JsonParser.Feature.ALLOW_COMMENTS,
                JsonParser.Feature.ALLOW_YAML_COMMENTS);
        String json = "// comment\n{\"x\":1}";
        Map<?, ?> result = r.forType(Map.class).readValue(json);
        assertEquals(1, result.get("x"));
    }

    // =============== Test without features ===============

    @Test(expected = JsonParseException.class)
    public void testWithoutFeatures() throws Exception {
        ObjectReader r = reader.without(JsonParser.Feature.ALLOW_COMMENTS);
        String json = "/* comment */{}";
        r.readValue(json);
    }

    // =============== Test readTree with JsonParser ===============

    @Test
    public void testReadTreeWithJsonParser() throws Exception {
        String json = "{\"tree\":\"value\"}";
        JsonParser jp = new JsonFactory().createParser(json);
        jp.nextToken();
        JsonNode node = reader.readTree(jp);
        assertEquals("value", node.get("tree").asText());
    }

    // =============== Test readTree with JSON array ===============

    @Test
    public void testReadTreeArrayParser() throws Exception {
        String json = "[1,2,3]";
        JsonParser jp = new JsonFactory().createParser(json);
        jp.nextToken();
        JsonNode node = reader.readTree(jp);
        assertTrue(node.isArray());
        assertEquals(3, node.size());
    }

    // =============== Test value type detection with @JsonTypeInfo ===============

    // Not possible without custom class; skip

    // =============== Test readValue with root value ===============

    @Test
    public void testReadValueWithRootValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
        ObjectReader reader = mapper.reader();
        // No root name, will try to find default root name from class
        // Use a known bean: SimpleBean? But we need annotation. Simpler: use Map with wrapper?
        // Can't easily test; just verify no exception
        String json = "{\"SimpleBean\":{\"x\":1}}";
        // With default wrapping, it expects root name 'SimpleBean' for SimpleBean class
        SimpleBean result = reader.forType(SimpleBean.class).readValue(json);
        assertEquals(1, result.x);
    }

    // =============== Test with JavaType ===============

    @Test
    public void testForTypeJavaType() throws Exception {
        JavaType type = mapper.constructType(String.class);
        ObjectReader r = reader.forType(type);
        assertEquals("test", r.readValue("\"test\""));
    }

    // =============== Test readValue with JsonParser and type ===============

    @Test
    public void testReadValueWithParserAndType() throws Exception {
        String json = "42";
        JsonParser jp = new JsonFactory().createParser(json);
        jp.nextToken();
        int value = reader.forType(Integer.class).readValue(jp);
        assertEquals(42, value);
    }

    // =============== Test with injectable values ===============

    @Test
    public void testWithInjectableValues() throws Exception {
        InjectableValues inject = new InjectableValues.Std().addValue("key", "value");
        ObjectReader r = reader.with(inject);
        // Without a bean that uses @JacksonInject, we can't easily test the effect
        assertNotNull(r);
    }

    // =============== Test that readValue can accept char[] ===============

    @Test
    public void testReadValueCharArray() throws Exception {
        String json = "{\"chararray\":true}";
        char[] chars = json.toCharArray();
        Map<?, ?> result = reader.forType(Map.class).readValue(chars);
        assertEquals(true, result.get("chararray"));
    }

    // =============== Test readValue with char[] offset/length ===============

    @Test
    public void testReadValueCharArrayOffsetLength() throws Exception {
        String json = "{\"offset\":\"work\"}";
        char[] full = ("prefix" + json + "suffix").toCharArray();
        Map<?, ?> result = reader.forType(Map.class).readValue(full, 6, json.length());
        assertEquals("work", result.get("offset"));
    }

    // =============== Test readValue with char[] and buffer reuse ===============

    @Test
    public void testReadValueCharArrayReuse() throws Exception {
        char[] buf = new char[100];
        reader.readValue(buf); // likely not enough? but call to test coverage
    }
}

// Helper bean class for tests
class SimpleBean {
    public int x;
}