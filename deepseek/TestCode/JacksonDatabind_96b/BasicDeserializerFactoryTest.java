package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class BasicDeserializerFactoryTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    // --- Test DTOs ---

    public enum MyEnum {
        VALUE_A, VALUE_B, VALUE_C
    }

    public static class SimpleBean {
        public int intValue;
        public String stringValue;
        public boolean boolValue;
    }

    public static class ListBean {
        public List<String> values;
    }

    public static class MapBean {
        public Map<String, Integer> values;
    }

    public static class EnumBean {
        public MyEnum enumValue;
    }

    public static class RawListBean {
        public List values;
    }

    public static class RawMapBean {
        public Map values;
    }

    public static class ObjectListBean {
        public List<Object> values;
    }

    public static class ObjectMapBean {
        public Map<String, Object> values;
    }

    public static class ObjectFieldBean {
        public Object value;
    }

    // --- Tests for basic bean deserialization ---

    @Test
    public void testSimpleBeanDeserialization() throws Exception {
        String json = "{\"intValue\":42,\"stringValue\":\"hello\",\"boolValue\":true}";
        SimpleBean bean = mapper.readValue(json, SimpleBean.class);
        assertNotNull(bean);
        assertEquals(42, bean.intValue);
        assertEquals("hello", bean.stringValue);
        assertTrue(bean.boolValue);
    }

    @Test
    public void testSimpleBeanNullValues() throws Exception {
        String json = "{\"intValue\":null,\"stringValue\":null,\"boolValue\":null}";
        SimpleBean bean = mapper.readValue(json, SimpleBean.class);
        assertNotNull(bean);
        assertEquals(0, bean.intValue);
        assertNull(bean.stringValue);
        assertFalse(bean.boolValue);
    }

    @Test
    public void testSimpleBeanEmptyStringForInt() throws Exception {
        String json = "{\"intValue\":\"\"}";
        try {
            mapper.readValue(json, SimpleBean.class);
            fail("Should have failed to deserialize empty string to int");
        } catch (Exception e) {
            // expected
        }
    }

    // --- Tests for collections ---

    @Test
    public void testListBean() throws Exception {
        String json = "{\"values\":[\"a\",\"b\",\"c\"]}";
        ListBean bean = mapper.readValue(json, ListBean.class);
        assertNotNull(bean);
        assertNotNull(bean.values);
        assertEquals(Arrays.asList("a", "b", "c"), bean.values);
    }

    @Test
    public void testListBeanEmpty() throws Exception {
        String json = "{\"values\":[]}";
        ListBean bean = mapper.readValue(json, ListBean.class);
        assertNotNull(bean);
        assertNotNull(bean.values);
        assertTrue(bean.values.isEmpty());
    }

    @Test
    public void testMapBean() throws Exception {
        String json = "{\"values\":{\"key1\":1,\"key2\":2}}";
        MapBean bean = mapper.readValue(json, MapBean.class);
        assertNotNull(bean);
        assertNotNull(bean.values);
        assertEquals(2, bean.values.size());
        assertEquals(Integer.valueOf(1), bean.values.get("key1"));
        assertEquals(Integer.valueOf(2), bean.values.get("key2"));
    }

    @Test
    public void testMapBeanEmpty() throws Exception {
        String json = "{\"values\":{}}";
        MapBean bean = mapper.readValue(json, MapBean.class);
        assertNotNull(bean);
        assertNotNull(bean.values);
        assertTrue(bean.values.isEmpty());
    }

    // --- Tests for enums ---

    @Test
    public void testEnumBean() throws Exception {
        String json = "{\"enumValue\":\"VALUE_B\"}";
        EnumBean bean = mapper.readValue(json, EnumBean.class);
        assertNotNull(bean);
        assertEquals(MyEnum.VALUE_B, bean.enumValue);
    }

    @Test
    public void testEnumBeanUnknownValue() throws Exception {
        String json = "{\"enumValue\":\"UNKNOWN\"}";
        try {
            mapper.readValue(json, EnumBean.class);
            fail("Should have failed to deserialize unknown enum value");
        } catch (Exception e) {
            // expected
        }
    }

    // --- Tests for raw types (potential bug area) ---

    @SuppressWarnings("unchecked")
    @Test
    public void testRawListBean() throws Exception {
        String json = "{\"values\":[\"a\",\"b\"]}";
        RawListBean bean = mapper.readValue(json, RawListBean.class);
        assertNotNull(bean);
        assertNotNull(bean.values);
        assertEquals(2, bean.values.size());
        assertTrue(bean.values instanceof ArrayList);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRawMapBean() throws Exception {
        String json = "{\"values\":{\"k\":\"v\"}}";
        RawMapBean bean = mapper.readValue(json, RawMapBean.class);
        assertNotNull(bean);
        assertNotNull(bean.values);
        assertEquals(1, bean.values.size());
        assertTrue(bean.values instanceof LinkedHashMap);
    }

    // --- Tests for Object fields ---

    @Test
    public void testObjectFieldWithInteger() throws Exception {
        String json = "{\"value\":123}";
        ObjectFieldBean bean = mapper.readValue(json, ObjectFieldBean.class);
        assertNotNull(bean);
        assertTrue(bean.value instanceof Integer);
        assertEquals(123, bean.value);
    }

    @Test
    public void testObjectFieldWithString() throws Exception {
        String json = "{\"value\":\"text\"}";
        ObjectFieldBean bean = mapper.readValue(json, ObjectFieldBean.class);
        assertNotNull(bean);
        assertTrue(bean.value instanceof String);
        assertEquals("text", bean.value);
    }

    @Test
    public void testObjectFieldWithArray() throws Exception {
        String json = "{\"value\":[1,2,3]}";
        ObjectFieldBean bean = mapper.readValue(json, ObjectFieldBean.class);
        assertNotNull(bean);
        assertTrue(bean.value instanceof List);
        assertEquals(3, ((List<?>) bean.value).size());
    }

    @Test
    public void testObjectFieldWithObject() throws Exception {
        String json = "{\"value\":{\"name\":\"test\"}}";
        ObjectFieldBean bean = mapper.readValue(json, ObjectFieldBean.class);
        assertNotNull(bean);
        assertTrue(bean.value instanceof Map);
        assertEquals(1, ((Map<?, ?>) bean.value).size());
    }

    @Test
    public void testObjectFieldNull() throws Exception {
        String json = "{\"value\":null}";
        ObjectFieldBean bean = mapper.readValue(json, ObjectFieldBean.class);
        assertNotNull(bean);
        assertNull(bean.value);
    }

    // --- Tests for nested collection/map with Object contents ---

    @Test
    public void testObjectListBean() throws Exception {
        String json = "{\"values\":[\"str\",123,true,{\"a\":1},[2]]}";
        ObjectListBean bean = mapper.readValue(json, ObjectListBean.class);
        assertNotNull(bean);
        assertNotNull(bean.values);
        assertEquals(5, bean.values.size());
        assertEquals("str", bean.values.get(0));
        assertEquals(123, bean.values.get(1));
        assertEquals(Boolean.TRUE, bean.values.get(2));
        assertTrue(bean.values.get(3) instanceof Map);
        assertTrue(bean.values.get(4) instanceof List);
    }

    @Test
    public void testObjectMapBean() throws Exception {
        String json = "{\"values\":{\"a\":\"text\",\"b\":42,\"c\":true,\"d\":{\"x\":1},\"e\":[5]}}";
        ObjectMapBean bean = mapper.readValue(json, ObjectMapBean.class);
        assertNotNull(bean);
        assertNotNull(bean.values);
        assertEquals(5, bean.values.size());
        assertEquals("text", bean.values.get("a"));
        assertEquals(42, bean.values.get("b"));
        assertEquals(Boolean.TRUE, bean.values.get("c"));
        assertTrue(bean.values.get("d") instanceof Map);
        assertTrue(bean.values.get("e") instanceof List);
    }

    // --- Tests for handling of missing properties and unknown properties ---

    @Test
    public void testMissingProperties() throws Exception {
        String json = "{\"stringValue\":\"only\"}";
        SimpleBean bean = mapper.readValue(json, SimpleBean.class);
        assertNotNull(bean);
        assertEquals(0, bean.intValue);
        assertEquals("only", bean.stringValue);
        assertFalse(bean.boolValue);
    }

    @Test
    public void testUnknownProperty() throws Exception {
        String json = "{\"unknown\":\"value\",\"intValue\":1}";
        SimpleBean bean = mapper.readValue(json, SimpleBean.class);
        assertNotNull(bean);
        assertEquals(1, bean.intValue);
        assertNull(bean.stringValue);
    }

    // --- Test for tree model to ensure JsonNode usage ---

    @Test
    public void testReadTree() throws Exception {
        String json = "{\"a\":1,\"b\":\"two\",\"c\":[true,false]}";
        JsonNode node = mapper.readTree(json);
        assertNotNull(node);
        assertTrue(node.isObject());
        assertTrue(node.has("a"));
        assertTrue(node.has("b"));
        assertTrue(node.has("c"));
    }

    // --- Edge cases with raw collection/map inside more complex types ---

    @SuppressWarnings("unchecked")
    @Test
    public void testRawCollectionNestedInMap() throws Exception {
        // This test exercises raw collection handling within a map property
        String json = "{\"values\":{\"list\":[1,2,3]}}";
        RawMapBean bean = mapper.readValue(json, RawMapBean.class);
        assertNotNull(bean);
        assertNotNull(bean.values);
        assertTrue(bean.values.get("list") instanceof List);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRawListWithMixedTypes() throws Exception {
        // Raw list should preserve mixed types as Object
        String json = "{\"values\":[1,\"two\",3.0,true,null]}";
        RawListBean bean = mapper.readValue(json, RawListBean.class);
        assertNotNull(bean);
        assertEquals(5, bean.values.size());
        assertTrue(bean.values.get(0) instanceof Integer);
        assertTrue(bean.values.get(1) instanceof String);
        assertTrue(bean.values.get(2) instanceof Double);
        assertTrue(bean.values.get(3) instanceof Boolean);
        assertNull(bean.values.get(4));
    }
}