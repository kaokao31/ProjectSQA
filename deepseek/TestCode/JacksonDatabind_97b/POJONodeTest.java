package com.fasterxml.jackson.databind.node;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Objects;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for POJONode.
 * Designed to achieve high coverage and detect potential bugs (e.g., Defects4J bug 97).
 */
public class POJONodeTest {

    private ObjectMapper mapper;
    private SerializerProvider provider;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        provider = mapper.getSerializerProvider();
    }

    // --- Constructor and basic getters ---

    @Test
    public void testConstructorNull() {
        POJONode node = new POJONode(null);
        assertNull("pojo should be null", node.getPojo());
        assertEquals("asToken should be VALUE_EMBEDDED_OBJECT", JsonToken.VALUE_EMBEDDED_OBJECT, node.asToken());
        assertEquals("nodeType should be POJO", JsonNodeType.POJO, node.getNodeType());
    }

    @Test
    public void testConstructorNonNull() {
        String pojo = "test";
        POJONode node = new POJONode(pojo);
        assertSame("pojo should be the same object", pojo, node.getPojo());
        assertEquals("asToken should be VALUE_EMBEDDED_OBJECT", JsonToken.VALUE_EMBEDDED_OBJECT, node.asToken());
        assertEquals("nodeType should be POJO", JsonNodeType.POJO, node.getNodeType());
    }

    // --- toString ---

    @Test
    public void testToStringNullPojo() {
        POJONode node = new POJONode(null);
        assertEquals("toString should return 'null'", "null", node.toString());
    }

    @Test
    public void testToStringNonNullPojo() {
        String pojo = "hello";
        POJONode node = new POJONode(pojo);
        assertEquals("toString should return pojo.toString()", pojo, node.toString());
    }

    @Test
    public void testToStringObjectWithCustomToString() {
        Object pojo = new Object() {
            @Override
            public String toString() {
                return "custom";
            }
        };
        POJONode node = new POJONode(pojo);
        assertEquals("toString should return custom toString", "custom", node.toString());
    }

    // --- equals ---

    @Test
    public void testEqualsSameObject() {
        POJONode node = new POJONode("value");
        assertTrue("should equal itself", node.equals(node));
    }

    @Test
    public void testEqualsNull() {
        POJONode node = new POJONode("value");
        assertFalse("should not equal null", node.equals(null));
    }

    @Test
    public void testEqualsDifferentType() {
        POJONode node = new POJONode("value");
        assertFalse("should not equal a different node type", node.equals(new TextNode("value")));
    }

    @Test
    public void testEqualsSamePojo() {
        String pojo = "test";
        POJONode node1 = new POJONode(pojo);
        POJONode node2 = new POJONode(pojo);
        assertTrue("nodes with same pojo should be equal", node1.equals(node2));
    }

    @Test
    public void testEqualsDifferentPojo() {
        POJONode node1 = new POJONode("a");
        POJONode node2 = new POJONode("b");
        assertFalse("nodes with different pojo should not be equal", node1.equals(node2));
    }

    @Test
    public void testEqualsBothNullPojo() {
        POJONode node1 = new POJONode(null);
        POJONode node2 = new POJONode(null);
        assertTrue("both null pojo should be equal", node1.equals(node2));
    }

    @Test
    public void testEqualsOneNullPojo() {
        POJONode node1 = new POJONode(null);
        POJONode node2 = new POJONode("x");
        assertFalse("one null pojo should not be equal", node1.equals(node2));
    }

    // --- hashCode ---

    @Test
    public void testHashCodeConsistency() {
        POJONode node = new POJONode("value");
        int hash1 = node.hashCode();
        int hash2 = node.hashCode();
        assertEquals("hashCode must be consistent", hash1, hash2);
    }

    @Test
    public void testHashCodeNullPojo() {
        POJONode node = new POJONode(null);
        int hash = node.hashCode();
        // Should not throw, and should be consistent
        assertEquals("hashCode for null pojo should be consistent", hash, node.hashCode());
    }

    @Test
    public void testHashCodeEqualObjects() {
        POJONode node1 = new POJONode("test");
        POJONode node2 = new POJONode("test");
        assertEquals("equal objects must have equal hashCodes", node1.hashCode(), node2.hashCode());
    }

    // --- serialize (JsonGenerator, SerializerProvider) ---

    @Test
    public void testSerializeNullPojo() throws IOException {
        POJONode node = new POJONode(null);
        JsonGenerator gen = mapper.getFactory().createGenerator(System.out);
        // We'll capture output via a different approach: use a JsonNode that writes to a string
        // Actually, we can use ObjectMapper's writeValueAsString to test serialization
        String json = mapper.writeValueAsString(node);
        assertEquals("null pojo should serialize as 'null'", "null", json);
    }

    @Test
    public void testSerializeStringPojo() throws IOException {
        POJONode node = new POJONode("hello");
        String json = mapper.writeValueAsString(node);
        // POJONode serializes the pojo using its serializer; for a String, it becomes a JSON string
        assertEquals("string pojo should serialize as JSON string", "\"hello\"", json);
    }

    @Test
    public void testSerializeIntegerPojo() throws IOException {
        POJONode node = new POJONode(42);
        String json = mapper.writeValueAsString(node);
        assertEquals("integer pojo should serialize as number", "42", json);
    }

    @Test
    public void testSerializeCustomObjectPojo() throws IOException {
        // A simple POJO with a field
        MyPojo pojo = new MyPojo();
        pojo.value = "test";
        POJONode node = new POJONode(pojo);
        String json = mapper.writeValueAsString(node);
        // Should serialize the MyPojo object
        assertTrue("should contain the field value", json.contains("\"value\":\"test\""));
    }

    // --- serialize with TypeSerializer (for polymorphic types) ---

    @Test
    public void testSerializeWithTypeSerializerNullPojo() throws IOException {
        POJONode node = new POJONode(null);
        // We can't easily test TypeSerializer without a full context, but we can ensure no exception
        // Use a dummy TypeSerializer that does nothing? For coverage, we can call the method with a mock.
        // Since we don't have mocking framework, we'll rely on the fact that the method likely delegates to serialize(gen, provider) if typeSerializer is null.
        // We'll just call it with a non-null TypeSerializer from the mapper.
        TypeSerializer typeSer = mapper.getSerializerProvider().findTypeSerializer(mapper.constructType(Object.class));
        // This might throw if not configured; we'll catch and ignore for coverage.
        try {
            node.serializeWithType(null, null, typeSer); // null generator will cause NPE, but we want to test the method path
            fail("Expected NullPointerException due to null generator");
        } catch (NullPointerException e) {
            // expected
        }
    }

    // --- asToken --- (already tested in constructor tests)

    // --- getNodeType --- (already tested)

    // --- deepCopy --- (if exists)

    @Test
    public void testDeepCopy() {
        POJONode node = new POJONode("original");
        JsonNode copy = node.deepCopy();
        assertNotNull("deepCopy should not return null", copy);
        assertTrue("deepCopy should be a POJONode", copy instanceof POJONode);
        POJONode pojoCopy = (POJONode) copy;
        // deepCopy should return the same pojo reference (shallow copy of pojo)
        assertSame("deepCopy should share the same pojo reference", node.getPojo(), pojoCopy.getPojo());
    }

    @Test
    public void testDeepCopyNullPojo() {
        POJONode node = new POJONode(null);
        JsonNode copy = node.deepCopy();
        assertNotNull("deepCopy should not return null", copy);
        assertTrue("deepCopy should be a POJONode", copy instanceof POJONode);
        assertNull("deepCopy pojo should be null", ((POJONode) copy).getPojo());
    }

    // --- Additional edge cases ---

    @Test
    public void testEqualsWithNonPOJONodeSubclass() {
        // If there is a subclass of POJONode, equals might behave differently
        POJONode node = new POJONode("value");
        // Create an anonymous subclass for testing
        POJONode sub = new POJONode("value") {
            // no additional fields
        };
        // By default, equals uses getClass() comparison, so they should not be equal
        assertFalse("subclass should not equal parent class even with same pojo", node.equals(sub));
    }

    @Test
    public void testHashCodeWithNullPojoAndNonNullPojo() {
        POJONode nullNode = new POJONode(null);
        POJONode nonNullNode = new POJONode("x");
        assertNotEquals("hashCodes should differ", nullNode.hashCode(), nonNullNode.hashCode());
    }

    // Helper class for custom POJO serialization test
    public static class MyPojo {
        public String value;
    }
}