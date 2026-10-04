package com.fasterxml.jackson.databind.node;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static org.junit.Assert.*;

public class ObjectNodeTest {

    private ObjectMapper objectMapper;
    private JsonNodeFactory nodeFactory;
    private ObjectNode objectNode;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        nodeFactory = JsonNodeFactory.instance;
        objectNode = new ObjectNode(nodeFactory);
    }

    @After
    public void tearDown() {
        objectNode = null;
        objectMapper = null;
    }

    @Test
    public void testNodeOfType() {
        assertEquals(JsonNodeType.OBJECT, objectNode.getNodeType());
        assertTrue(objectNode.isObject());
        assertFalse(objectNode.isArray());
        assertFalse(objectNode.isValueNode());
    }

    @Test
    public void testEmptyObjectNode() {
        assertEquals(0, objectNode.size());
        assertTrue(objectNode.isEmpty());
        assertFalse(objectNode.fieldNames().hasNext());
        assertFalse(objectNode.fields().hasNext());
        assertNull(objectNode.get("nonExistent"));
    }

    @Test
    public void testPutMethods() {
        objectNode.put("strKey", "strValue");
        objectNode.put("intKey", 42);
        objectNode.put("longKey", 100L);
        objectNode.put("doubleKey", 3.14);
        objectNode.put("floatKey", 2.5f);
        objectNode.put("boolKey", true);
        objectNode.put("nullKey", (String) null);

        assertEquals(7, objectNode.size());
        assertEquals("strValue", objectNode.get("strKey").asText());
        assertEquals(42, objectNode.get("intKey").asInt());
        assertEquals(100L, objectNode.get("longKey").asLong());
        assertEquals(3.14, objectNode.get("doubleKey").asDouble(), 0.001);
        assertEquals(2.5f, (float) objectNode.get("floatKey").asDouble(), 0.001);
        assertTrue(objectNode.get("boolKey").asBoolean());
        assertTrue(objectNode.get("nullKey").isNull());

        // Test boxing/wrapper types overrides
        objectNode.put("intObjKey", Integer.valueOf(10));
        objectNode.put("longObjKey", Long.valueOf(20L));
        objectNode.put("doubleObjKey", Double.valueOf(1.1));
        objectNode.put("floatObjKey", Float.valueOf(1.2f));
        objectNode.put("boolObjKey", Boolean.FALSE);
        objectNode.put("bigDecKey", BigDecimal.TEN);
        objectNode.put("bigIntKey", BigInteger.ONE);

        assertEquals(10, objectNode.get("intObjKey").asInt());
        assertEquals(20L, objectNode.get("longObjKey").asLong());
        assertEquals(1.1, objectNode.get("doubleObjKey").asDouble(), 0.001);
        assertFalse(objectNode.get("boolObjKey").asBoolean());
        assertEquals(BigDecimal.TEN, objectNode.get("bigDecKey").decimalValue());
        assertEquals(BigInteger.ONE, objectNode.get("bigIntKey").bigIntegerValue());
    }

    @Test
    public void testPutNullValues() {
        objectNode.put("nullInt", (Integer) null);
        objectNode.put("nullLong", (Long) null);
        objectNode.put("nullDouble", (Double) null);
        objectNode.put("nullFloat", (Float) null);
        objectNode.put("nullBool", (Boolean) null);
        objectNode.put("nullBigDec", (BigDecimal) null);
        objectNode.put("nullBigInt", (BigInteger) null);

        assertTrue(objectNode.get("nullInt").isNull());
        assertTrue(objectNode.get("nullLong").isNull());
        assertTrue(objectNode.get("nullDouble").isNull());
        assertTrue(objectNode.get("nullFloat").isNull());
        assertTrue(objectNode.get("nullBool").isNull());
        assertTrue(objectNode.get("nullBigDec").isNull());
        assertTrue(objectNode.get("nullBigInt").isNull());
    }

    @Test
    public void testPutArrayAndObject() {
        ArrayNode arrayNode = objectNode.putArray("arrayField");
        assertNotNull(arrayNode);
        assertTrue(objectNode.get("arrayField").isArray());

        ObjectNode childObject = objectNode.putObject("objectField");
        assertNotNull(childObject);
        assertTrue(objectNode.get("objectField").isObject());
    }

    @Test
    public void testSetAndRemove() {
        JsonNode addedNode = TextNode.valueOf("testText");
        JsonNode prev = objectNode.set("key1", addedNode);
        assertNull(prev);
        assertEquals(addedNode, objectNode.get("key1"));

        // Overwrite
        JsonNode replacement = TextNode.valueOf("newText");
        prev = objectNode.set("key1", replacement);
        assertEquals(addedNode, prev);
        assertEquals(replacement, objectNode.get("key1"));

        // Remove
        JsonNode removed = objectNode.remove("key1");
        assertEquals(replacement, removed);
        assertNull(objectNode.get("key1"));

        // Remove non-existent
        assertNull(objectNode.remove("nonExistent"));

        // Set null value via set
        objectNode.set("keyNull", null);
        assertTrue(objectNode.get("keyNull").isNull());
    }

    @Test
    public void testSetAll() {
        Map<String, JsonNode> map = new HashMap<String, JsonNode>();
        map.put("a", TextNode.valueOf("apple"));
        map.put("b", IntNode.valueOf(5));

        objectNode.setAll(map);
        assertEquals(2, objectNode.size());
        assertEquals("apple", objectNode.get("a").asText());
        assertEquals(5, objectNode.get("b").asInt());

        ObjectNode otherObject = new ObjectNode(nodeFactory);
        otherObject.put("c", "cherry");
        
        objectNode.setAll(otherObject);
        assertEquals(3, objectNode.size());
        assertEquals("cherry", objectNode.get("c").asText());
    }

    @Test
    public void testRemoveAll() {
        objectNode.put("a", 1);
        objectNode.put("b", 2);
        assertEquals(2, objectNode.size());

        ObjectNode cleared = objectNode.removeAll();
        assertSame(objectNode, cleared);
        assertEquals(0, objectNode.size());

        Collection<String> names = new ArrayList<String>();
        names.add("a");
        objectNode.put("a", 1);
        objectNode.put("b", 2);
        objectNode.remove(names);
        assertNull(objectNode.get("a"));
        assertNotNull(objectNode.get("b"));
    }

    @Test
    public void testPutIfAbsent() {
        JsonNode first = objectNode.putIfAbsent("key", TextNode.valueOf("first"));
        assertNull(first);
        assertEquals("first", objectNode.get("key").asText());

        JsonNode second = objectNode.putIfAbsent("key", TextNode.valueOf("second"));
        assertNotNull(second);
        assertEquals("first", objectNode.get("key").asText()); // Should remain unchanged
    }

    @Test
    public void testPathAndGet() {
        objectNode.put("existing", "value");
        
        assertEquals("value", objectNode.path("existing").asText());
        assertTrue(objectNode.path("nonExisting").isMissingNode());
        
        assertEquals("value", objectNode.get("existing").asText());
        assertNull(objectNode.get("nonExisting"));
    }

    @Test
    public void testFindValues() {
        objectNode.put("target", "foundMe");
        ObjectNode child = objectNode.putObject("child");
        child.put("target", "foundChild");

        List<JsonNode> values = objectNode.findValues("target");
        assertEquals(2, values.size());

        List<String> textValues = objectNode.findValuesAsText("target");
        assertEquals(2, textValues.size());
        assertTrue(textValues.contains("foundMe"));
        assertTrue(textValues.contains("foundChild"));

        List<JsonNode> missing = objectNode.findValues("missing");
        assertTrue(missing.isEmpty());

        JsonNode foundNode = objectNode.findValue("target");
        assertNotNull(foundNode);
        
        assertNull(objectNode.findValue("missingKey"));
    }

    @Test
    public void testFindParents() {
        objectNode.put("target", "val");
        List<JsonNode> parents = objectNode.findParents("target");
        assertEquals(1, parents.size());
        assertSame(objectNode, parents.get(0));

        assertNull(objectNode.findParent("missing"));
    }

    @Test
    public void testDeepCopy() {
        objectNode.put("key", "value");
        ObjectNode copy = objectNode.deepCopy();
        
        assertNotSame(objectNode, copy);
        assertEquals(objectNode, copy);
        assertEquals("value", copy.get("key").asText());
    }

    @Test
    public void testEqualsAndHashCode() {
        ObjectNode node1 = new ObjectNode(nodeFactory);
        node1.put("k", "v");

        ObjectNode node2 = new ObjectNode(nodeFactory);
        node2.put("k", "v");

        ObjectNode node3 = new ObjectNode(nodeFactory);
        node3.put("k", "different");

        assertEquals(node1, node1);
        assertEquals(node1, node2);
        assertNotEquals(node1, node3);
        assertNotEquals(node1, null);
        assertNotEquals(node1, TextNode.valueOf("k"));

        assertEquals(node1.hashCode(), node2.hashCode());
    }

    @Test
    public void testSerializationToString() {
        objectNode.put("test", 123);
        String jsonStr = objectNode.toString();
        assertTrue(jsonStr.contains("\"test\""));
        assertTrue(jsonStr.contains("123"));
    }

    @Test
    public void testWithMethodsForDefects4J20() {
        // Specific tests for ObjectNode 'with' methods which are often focal points in JacksonDatabind bug 20
        ObjectNode child1 = objectNode.with("childProperty");
        assertNotNull(child1);
        assertTrue(objectNode.get("childProperty").isObject());

        // Calling 'with' on an already existing object property should return the same object
        ObjectNode child1Again = objectNode.with("childProperty");
        assertSame(child1, child1Again);

        // Calling 'with' on an existing non-object property should throw UnsupportedOperationException
        objectNode.put("nonObjectProperty", "stringValue");
        try {
            objectNode.with("nonObjectProperty");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }
}