package com.fasterxml.jackson.databind.deser.std;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.junit.Before;
import org.junit.Test;

public class MapDeserializerTest {

    private ObjectMapper mapper;
    private DeserializationContext ctxt;
    private JsonFactory jsonFactory;
    private MapDeserializer deserializer;

    @Before
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
        ctxt = mapper.getDeserializationContext();
        jsonFactory = mapper.getFactory();
        JavaType mapType = mapper.getTypeFactory().constructMapType(HashMap.class, String.class, String.class);
        deserializer = new MapDeserializer(mapType, new PrefixKeyDeserializer(), new PrefixValueDeserializer(), null);
        deserializer.resolve(ctxt);
    }

    @Test
    public void testDeserializeFromStartObject() throws Exception {
        JsonParser p = jsonFactory.createParser("{\"a\":\"b\"}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());

        Map<Object, Object> result = deserializer.deserialize(p, ctxt);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("value:b", result.get("key:a"));
    }

    @Test
    public void testDeserializeFromFieldName() throws Exception {
        JsonParser p = jsonFactory.createParser("{\"a\":\"b\"}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());

        Map<Object, Object> result = deserializer.deserialize(p, ctxt);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("value:b", result.get("key:a"));
    }

    @Test(expected = JsonMappingException.class)
    public void testDeserializeInvalidToken() throws Exception {
        JsonParser p = jsonFactory.createParser("\"not a map\"");
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());

        deserializer.deserialize(p, ctxt);
    }

    @Test
    public void testDeserializeEmptyObject() throws Exception {
        JsonParser p = jsonFactory.createParser("{}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());

        Map<Object, Object> result = deserializer.deserialize(p, ctxt);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testDeserializeNullValue() throws Exception {
        JsonParser p = jsonFactory.createParser("{\"a\":null,\"b\":\"c\"}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());

        Map<Object, Object> result = deserializer.deserialize(p, ctxt);

        assertTrue(result.containsKey("key:a"));
        assertNull(result.get("key:a"));
        assertEquals("value:c", result.get("key:b"));
    }

    @Test
    public void testDeserializeIntoExistingMap() throws Exception {
        JsonParser p = jsonFactory.createParser("{\"a\":\"b\"}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());

        Map<Object, Object> existing = new LinkedHashMap<Object, Object>();
        existing.put("old", "value");

        Map<Object, Object> result = deserializer.deserialize(p, ctxt, existing);

        assertSame(existing, result);
        assertEquals(2, result.size());
        assertEquals("value:b", result.get("key:a"));
    }

    @Test
    public void testDeserializeIgnoredProperty() throws Exception {
        Field ignorable = MapDeserializer.class.getDeclaredField("_ignorableProperties");
        ignorable.setAccessible(true);
        ignorable.set(deserializer, Collections.singleton("a"));

        JsonParser p = jsonFactory.createParser("{\"a\":\"ignored\",\"b\":\"keep\"}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());

        Map<Object, Object> result = deserializer.deserialize(p, ctxt);

        assertEquals(1, result.size());
        assertFalse(result.containsKey("key:a"));
        assertEquals("value:keep", result.get("key:b"));
    }

    @Test
    public void testDeserializeIgnoredNestedProperty() throws Exception {
        Field ignorable = MapDeserializer.class.getDeclaredField("_ignorableProperties");
        ignorable.setAccessible(true);
        ignorable.set(deserializer, Collections.singleton("a"));

        JsonParser p = jsonFactory.createParser("{\"a\":{\"x\":1},\"b\":\"keep\"}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());

        Map<Object, Object> result = deserializer.deserialize(p, ctxt);

        assertEquals(1, result.size());
        assertFalse(result.containsKey("key:a"));
        assertEquals("value:keep", result.get("key:b"));
    }

    @Test
    public void testDeserializeWithStandardStringKey() throws Exception {
        Field standardKey = MapDeserializer.class.getDeclaredField("_standardStringKey");
        standardKey.setAccessible(true);
        standardKey.setBoolean(deserializer, true);

        JsonParser p = jsonFactory.createParser("{\"a\":\"b\"}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());

        Map<Object, Object> result = deserializer.deserialize(p, ctxt);

        assertEquals("value:b", result.get("a"));
    }

    @Test
    public void testResolve() throws Exception {
        JavaType mapType = mapper.getTypeFactory().constructMapType(HashMap.class, String.class, String.class);
        MapDeserializer fresh = new MapDeserializer(mapType, new PrefixKeyDeserializer(), new PrefixValueDeserializer(), null);

        fresh.resolve(ctxt);

        JsonParser p = jsonFactory.createParser("{\"a\":\"b\"}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());

        Map<Object, Object> result = fresh.deserialize(p, ctxt);

        assertEquals("value:b", result.get("key:a"));
    }

    @Test
    public void testDeserializeEmptyStringKey() throws Exception {
        Map<String, String> result = mapper.readValue(
                "{\"\":\"empty\"}",
                new TypeReference<Map<String, String>>() {});

        assertEquals("empty", result.get(""));
    }

    @Test
    public void testDeserializeDuplicateKeys() throws Exception {
        Map<String, Integer> result = mapper.readValue(
                "{\"a\":1,\"a\":2}",
                new TypeReference<Map<String, Integer>>() {});

        assertEquals(Integer.valueOf(2), result.get("a"));
    }

    @Test
    public void testDeserializeIntegerKeys() throws Exception {
        Map<Integer, String> result = mapper.readValue(
                "{\"1\":\"one\",\"2\":\"two\"}",
                new TypeReference<Map<Integer, String>>() {});

        assertEquals("one", result.get(Integer.valueOf(1)));
        assertEquals("two", result.get(Integer.valueOf(2)));
    }

    @Test
    public void testDeserializeBooleanKeys() throws Exception {
        Map<Boolean, String> result = mapper.readValue(
                "{\"true\":\"yes\",\"false\":\"no\"}",
                new TypeReference<Map<Boolean, String>>() {});

        assertEquals("yes", result.get(Boolean.TRUE));
        assertEquals("no", result.get(Boolean.FALSE));
    }

    @Test
    public void testDeserializeNestedMap() throws Exception {
        Map<String, Object> result = mapper.readValue(
                "{\"outer\":{\"inner\":42}}",
                new TypeReference<Map<String, Object>>() {});

        assertEquals(Collections.singletonMap("inner", 42), result.get("outer"));
    }

    @Test
    public void testDeserializeListValues() throws Exception {
        Map<String, List<Integer>> result = mapper.readValue(
                "{\"numbers\":[1,2,3]}",
                new TypeReference<Map<String, List<Integer>>>() {});

        assertEquals(Arrays.asList(1, 2, 3), result.get("numbers"));
    }

    @Test
    public void testCustomKeyAndValueDeserializerViaAnnotation() throws Exception {
        MapBean bean = mapper.readValue("{\"map\":{\"a\":\"b\"}}", MapBean.class);

        assertNotNull(bean);
        assertNotNull(bean.map);
        assertEquals(1, bean.map.size());
        assertTrue(bean.map.containsKey("A"));
        assertEquals("val:b", bean.map.get("A"));
    }

    @Test
    public void testPolymorphicMapValues() throws Exception {
        Map<String, Base> result = mapper.readValue(
                "{\"x\":{\"type\":\"a\",\"a\":1}}",
                new TypeReference<Map<String, Base>>() {});

        assertTrue(result.get("x") instanceof BaseA);
    }

    public static class MapBean {
        @JsonDeserialize(keyUsing = UpperKeyDeserializer.class, contentUsing = PrefixValueDeserializer.class)
        public Map<String, String> map;
    }

    public static class PrefixKeyDeserializer extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
            return "key:" + key;
        }
    }

    public static class UpperKeyDeserializer extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
            return key.toUpperCase(Locale.ROOT);
        }
    }

    public static class PrefixValueDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return "value:" + p.getText();
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = BaseA.class, name = "a"),
        @JsonSubTypes.Type(value = BaseB.class, name = "b")
    })
    public static class Base { }

    public static class BaseA extends Base {
        public int a;
    }

    public static class BaseB extends Base {
        public int b;
    }
}