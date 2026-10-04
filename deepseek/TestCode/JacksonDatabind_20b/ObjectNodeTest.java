package com.fasterxml.jackson.databind.node;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class ObjectNodeTest {

    private ObjectNode node;

    @Before
    public void setUp() {
        node = new ObjectNode(JsonNodeFactory.instance);
    }

    // ---------- put(String, String) ----------
    @Test
    public void testPutString() {
        node.put("key", "value");
        assertTrue(node.has("key"));
        assertEquals("value", node.get("key").asText());
    }

    @Test
    public void testPutStringNullValue() {
        node.put("key", (String) null);
        assertTrue(node.has("key"));
        assertTrue(node.get("key").isNull());
    }

    // ---------- put(String, int) ----------
    @Test
    public void testPutInt() {
        node.put("key", 42);
        assertEquals(42, node.get("key").asInt());
    }

    @Test
    public void testPutIntOverwrite() {
        node.put("key", 1);
        node.put("key", 2);
        assertEquals(2, node.get("key").asInt());
    }

    // ---------- put(String, long) ----------
    @Test
    public void testPutLong() {
        node.put("key", 123456789L);
        assertEquals(123456789L, node.get("key").asLong());
    }

    // ---------- put(String, double) ----------
    @Test
    public void testPutDouble() {
        node.put("key", 3.14);
        assertEquals(3.14, node.get("key").asDouble(), 0.0001);
    }

    // ---------- put(String, float) ----------
    @Test
    public void testPutFloat() {
        node.put("key", 2.5f);
        assertEquals(2.5f, node.get("key").asDouble(), 0.0001);
    }

    // ---------- put(String, boolean) ----------
    @Test
    public void testPutBooleanTrue() {
        node.put("key", true);
        assertTrue(node.get("key").asBoolean());
    }

    @Test
    public void testPutBooleanFalse() {
        node.put("key", false);
        assertFalse(node.get("key").asBoolean());
    }

    // ---------- put(String, BigInteger) ----------
    @Test
    public void testPutBigInteger() {
        BigInteger value = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE);
        node.put("key", value);
        assertEquals(value, node.get("key").asBigInteger());
    }

    @Test
    public void testPutBigIntegerNull() {
        // Bug trigger: null BigInteger should not throw NPE
        node.put("key", (BigInteger) null);
        assertTrue(node.get("key").isNull());
    }

    // ---------- put(String, BigDecimal) ----------
    @Test
    public void testPutBigDecimal() {
        BigDecimal value = new BigDecimal("123.456");
        node.put("key", value);
        assertEquals(value, node.get("key").asBigDecimal());
    }

    @Test
    public void testPutBigDecimalNull() {
        // Bug trigger: null BigDecimal should not throw NPE
        node.put("key", (BigDecimal) null);
        assertTrue(node.get("key").isNull());
    }

    // ---------- put(String, Object) ----------
    @Test
    public void testPutObjectInteger() {
        node.put("key", Integer.valueOf(100));
        assertEquals(100, node.get("key").asInt());
    }

    @Test
    public void testPutObjectString() {
        node.put("key", "hello");
        assertEquals("hello", node.get("key").asText());
    }

    @Test
    public void testPutObjectNull() {
        node.put("key", (Object) null);
        assertTrue(node.get("key").isNull());
    }

    // ---------- put(String, byte[]) ----------
    @Test
    public void testPutBinary() {
        byte[] data = {1, 2, 3};
        node.put("key", data);
        assertArrayEquals(data, node.get("key").binaryValue());
    }

    @Test
    public void testPutBinaryNull() {
        node.put("key", (byte[]) null);
        assertTrue(node.get("key").isNull());
    }

    // ---------- get / has ----------
    @Test
    public void testGetExistingKey() {
        node.put("a", 1);
        assertNotNull(node.get("a"));
    }

    @Test
    public void testGetNonExistingKey() {
        assertNull(node.get("nonexistent"));
    }

    @Test
    public void testHasExistingKey() {
        node.put("a", 1);
        assertTrue(node.has("a"));
    }

    @Test
    public void testHasNonExistingKey() {
        assertFalse(node.has("nonexistent"));
    }

    // ---------- remove ----------
    @Test
    public void testRemoveExistingKey() {
        node.put("a", 1);
        assertNotNull(node.remove("a"));
        assertFalse(node.has("a"));
    }

    @Test
    public void testRemoveNonExistingKey() {
        assertNull(node.remove("nonexistent"));
    }

    // ---------- set ----------
    @Test
    public void testSet() {
        node.set("key", TextNode.valueOf("value"));
        assertEquals("value", node.get("key").asText());
    }

    @Test
    public void testSetNullValue() {
        node.set("key", null);
        assertTrue(node.get("key").isNull());
    }

    // ---------- replace ----------
    @Test
    public void testReplaceExistingKey() {
        node.put("key", "old");
        JsonNode replaced = node.replace("key", TextNode.valueOf("new"));
        assertEquals("old", replaced.asText());
        assertEquals("new", node.get("key").asText());
    }

    @Test
    public void testReplaceNonExistingKey() {
        assertNull(node.replace("nonexistent", TextNode.valueOf("val")));
    }

    // ---------- withArray ----------
    @Test
    public void testWithArrayNew() {
        ArrayNode arr = node.withArray("arr");
        assertNotNull(arr);
        assertTrue(node.get("arr").isArray());
    }

    @Test
    public void testWithArrayExisting() {
        ArrayNode arr = node.withArray("arr");
        arr.add(1);
        ArrayNode same = node.withArray("arr");
        assertSame(arr, same);
        assertEquals(1, same.size());
    }

    // ---------- withObject ----------
    @Test
    public void testWithObjectNew() {
        ObjectNode obj = node.withObject("obj");
        assertNotNull(obj);
        assertTrue(node.get("obj").isObject());
    }

    @Test
    public void testWithObjectExisting() {
        ObjectNode obj = node.withObject("obj");
        obj.put("x", 1);
        ObjectNode same = node.withObject("obj");
        assertSame(obj, same);
        assertEquals(1, same.get("x").asInt());
    }

    // ---------- size ----------
    @Test
    public void testSizeEmpty() {
        assertEquals(0, node.size());
    }

    @Test
    public void testSizeAfterPuts() {
        node.put("a", 1);
        node.put("b", 2);
        assertEquals(2, node.size());
    }

    // ---------- fields ----------
    @Test
    public void testFields() {
        node.put("a", 1);
        node.put("b", 2);
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        assertTrue(fields.hasNext());
        Map.Entry<String, JsonNode> entry = fields.next();
        assertNotNull(entry.getKey());
        assertNotNull(entry.getValue());
    }

    // ---------- elements ----------
    @Test
    public void testElements() {
        node.put("a", 1);
        node.put("b", 2);
        Iterator<JsonNode> elements = node.elements();
        assertTrue(elements.hasNext());
        assertNotNull(elements.next());
    }

    // ---------- deepCopy ----------
    @Test
    public void testDeepCopy() {
        node.put("a", 1);
        ObjectNode copy = node.deepCopy();
        assertEquals(node, copy);
        copy.put("a", 2);
        assertNotEquals(node.get("a").asInt(), copy.get("a").asInt());
    }

    // ---------- equals / hashCode ----------
    @Test
    public void testEqualsSameContent() {
        ObjectNode other = new ObjectNode(JsonNodeFactory.instance);
        other.put("a", 1);
        node.put("a", 1);
        assertEquals(node, other);
    }

    @Test
    public void testEqualsDifferentContent() {
        ObjectNode other = new ObjectNode(JsonNodeFactory.instance);
        other.put("a", 2);
        node.put("a", 1);
        assertNotEquals(node, other);
    }

    @Test
    public void testHashCodeConsistency() {
        node.put("a", 1);
        int hc1 = node.hashCode();
        node.put("b", 2);
        int hc2 = node.hashCode();
        assertNotEquals(hc1, hc2);
    }

    // ---------- toString ----------
    @Test
    public void testToStringEmpty() {
        assertEquals("{}", node.toString());
    }

    @Test
    public void testToStringWithContent() {
        node.put("a", 1);
        assertTrue(node.toString().contains("\"a\""));
    }

    // ---------- null key handling ----------
    @Test(expected = IllegalArgumentException.class)
    public void testPutNullKey() {
        node.put(null, "value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetNullKey() {
        node.get(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveNullKey() {
        node.remove(null);
    }

    // ---------- edge cases ----------
    @Test
    public void testPutEmptyKey() {
        node.put("", "empty");
        assertTrue(node.has(""));
        assertEquals("empty", node.get("").asText());
    }

    @Test
    public void testPutManyEntries() {
        for (int i = 0; i < 100; i++) {
            node.put("key" + i, i);
        }
        assertEquals(100, node.size());
    }

    @Test
    public void testPutAll() {
        ObjectNode other = new ObjectNode(JsonNodeFactory.instance);
        other.put("x", 10);
        other.put("y", 20);
        node.putAll(other);
        assertEquals(2, node.size());
        assertEquals(10, node.get("x").asInt());
    }

    @Test
    public void testRetainAll() {
        node.put("a", 1);
        node.put("b", 2);
        node.put("c", 3);
        // retain only "a" and "b"
        assertTrue(node.retain("a", "b"));
        assertEquals(2, node.size());
        assertTrue(node.has("a"));
        assertTrue(node.has("b"));
        assertFalse(node.has("c"));
    }

    @Test
    public void testRemoveAll() {
        node.put("a", 1);
        node.put("b", 2);
        node.removeAll();
        assertEquals(0, node.size());
    }
}