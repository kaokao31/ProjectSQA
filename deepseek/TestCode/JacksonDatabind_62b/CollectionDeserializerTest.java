package com.fasterxml.jackson.databind.deser.std;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.type.CollectionType;

/**
 * Unit tests for CollectionDeserializer.
 * Targets edge cases, null handling, and potential bugs (e.g., Defects4J #62).
 */
public class CollectionDeserializerTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------

    private CollectionDeserializer getCollectionDeserializer(JavaType type) throws Exception {
        JsonDeserializer<Object> deser = mapper.getDeserializationConfig()
                .getDeserializerProvider()
                .findValueDeserializer(mapper.getDeserializationConfig(), type, null);
        assertTrue("Expected CollectionDeserializer, got: " + deser.getClass().getName(),
                deser instanceof CollectionDeserializer);
        return (CollectionDeserializer) deser;
    }

    private DeserializationContext getDeserializationContext(JsonParser p) throws Exception {
        // Use the mapper to create a context bound to the parser.
        // In Jackson 2.x this is done via DefaultDeserializationContext.Impl.
        return mapper.getDeserializationContext();
    }

    // ------------------------------------------------------------------------
    // Tests for deserialize()
    // ------------------------------------------------------------------------

    @Test
    public void testDeserializeSimpleList() throws Exception {
        CollectionType type = mapper.getTypeFactory().constructCollectionType(List.class, String.class);
        String json = "[\"a\",\"b\"]";
        @SuppressWarnings("unchecked")
        List<String> list = (List<String>) mapper.readValue(json, type);
        assertEquals(Arrays.asList("a", "b"), list);
    }

    @Test
    public void testDeserializeEmptyList() throws Exception {
        CollectionType type = mapper.getTypeFactory().constructCollectionType(List.class, String.class);
        @SuppressWarnings("unchecked")
        List<String> list = (List<String>) mapper.readValue("[]", type);
        assertTrue(list.isEmpty());
    }

    @Test
    public void testDeserializeNullJson() throws Exception {
        CollectionType type = mapper.getTypeFactory().constructCollectionType(List.class, String.class);
        Object result = mapper.readValue("null", type);
        assertNull(result);
    }

    @Test
    public void testDeserializeListWithNullElement() throws Exception {
        CollectionType type = mapper.getTypeFactory().constructCollectionType(List.class, String.class);
        @SuppressWarnings("unchecked")
        List<String> list = (List<String>) mapper.readValue("[\"a\",null,\"b\"]", type);
        assertEquals(Arrays.asList("a", null, "b"), list);
    }

    @Test
    public void testDeserializeSet() throws Exception {
        CollectionType type = mapper.getTypeFactory().constructCollectionType(Set.class, String.class);
        @SuppressWarnings("unchecked")
        Set<String> set = (Set<String>) mapper.readValue("[\"a\",\"b\",\"a\"]", type);
        assertEquals(new HashSet<>(Arrays.asList("a", "b")), set);
    }

    @Test
    public void testDeserializeListWithObjectValue() throws Exception {
        // Regression test for bug #62: should handle Object values without NPE.
        CollectionType type = mapper.getTypeFactory().constructCollectionType(List.class, Object.class);
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) mapper.readValue("[1, \"two\", true]", type);
        assertEquals(3, list.size());
        assertEquals(1, list.get(0));
        assertEquals("two", list.get(1));
        assertEquals(Boolean.TRUE, list.get(2));
    }

    @Test(expected = JsonParseException.class)
    public void testDeserializeMalformedJson() throws Exception {
        CollectionType type = mapper.getTypeFactory().constructCollectionType(List.class, String.class);
        // Missing closing bracket
        mapper.readValue("[\"a\",\"b\"", type);
    }

    // ------------------------------------------------------------------------
    // Tests for withResolved()
    // ------------------------------------------------------------------------

    @Test
    public void testWithResolvedReturnsNonNull() throws Exception {
        CollectionType type = mapper.getTypeFactory().constructCollectionType(List.class, String.class);
        CollectionDeserializer deser = getCollectionDeserializer(type);

        // Use same underlying deserializers; should still return a usable instance.
        JsonDeserializer<?> valueDeser = deser.getContentDeserializer();
        TypeDeserializer typeDeser = deser.getTypeDeserializer();
        JsonDeserializer<?> resolved = deser.withResolved(valueDeser, typeDeser);
        assertNotNull(resolved);
        assertTrue(resolved instanceof CollectionDeserializer);

        // Verify the resolved deserializer still works.
        CollectionDeserializer resolvedDeser = (CollectionDeserializer) resolved;
        JsonParser p = mapper.getFactory().createParser("[\"x\",\"y\"]");
        DeserializationContext ctxt = mapper.getDeserializationContext();
        @SuppressWarnings("unchecked")
        List<String> list = (List<String>) resolvedDeser.deserialize(p, ctxt);
        assertEquals(Arrays.asList("x", "y"), list);
        p.close();
    }

    @Test
    public void testWithResolvedNullValueDeserializer() throws Exception {
        // Bug #62 context: passing null value deserializer should not throw NPE.
        CollectionType type = mapper.getTypeFactory().constructCollectionType(List.class, String.class);
        CollectionDeserializer deser = getCollectionDeserializer(type);

        // Force a null value deserializer (simulates a problematic configuration).
        CollectionDeserializer resolved = (CollectionDeserializer) deser.withResolved(null, deser.getTypeDeserializer());
        assertNotNull(resolved);

        // Even with null value deser, deserialization should not crash.
        JsonParser p = mapper.getFactory().createParser("[\"a\",\"b\"]");
        DeserializationContext ctxt = mapper.getDeserializationContext();
        @SuppressWarnings("unchecked")
        List<String> list = (List<String>) resolved.deserialize(p, ctxt);
        assertEquals(Arrays.asList("a", "b"), list);
        p.close();
    }

    // ------------------------------------------------------------------------
    // Tests for getNullValue()
    // ------------------------------------------------------------------------

    @Test
    public void testGetNullValue() throws Exception {
        CollectionType type = mapper.getTypeFactory().constructCollectionType(List.class, String.class);
        CollectionDeserializer deser = getCollectionDeserializer(type);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        assertNull(deser.getNullValue(ctxt));
    }

    // ------------------------------------------------------------------------
    // Tests for deserializeWithType()
    // ------------------------------------------------------------------------

    @Test
    public void testDeserializeWithType() throws Exception {
        // Enable default typing to have a TypeDeserializer.
        ObjectMapper typedMapper = new ObjectMapper();
        typedMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

        // A list of polymorphic objects (here just Strings and integers)
        String json = "[\"java.lang.String\",\"hello\"]";
        JavaType listType = typedMapper.getTypeFactory().constructCollectionType(List.class, Object.class);

        // The deserializer should handle the type wrapper.
        List<?> list = typedMapper.readValue(json, listType);
        assertEquals(1, list.size());
        assertEquals("hello", list.get(0));
    }

    // ------------------------------------------------------------------------
    // Direct deserializer tests (using the parser/context directly)
    // ------------------------------------------------------------------------

    @Test
    public void testDirectDeserialize() throws Exception {
        CollectionType type = mapper.getTypeFactory().constructCollectionType(List.class, String.class);
        CollectionDeserializer deser = getCollectionDeserializer(type);

        JsonParser p = mapper.getFactory().createParser("[\"a\",\"b\"]");
        assertToken(p.nextToken(), JsonToken.START_ARRAY);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        @SuppressWarnings("unchecked")
        List<String> list = (List<String>) deser.deserialize(p, ctxt);
        assertEquals(Arrays.asList("a", "b"), list);
        p.close();
    }

    @Test
    public void testDirectDeserializeNull() throws Exception {
        CollectionType type = mapper.getTypeFactory().constructCollectionType(List.class, String.class);
        CollectionDeserializer deser = getCollectionDeserializer(type);

        JsonParser p = mapper.getFactory().createParser("null");
        assertToken(p.nextToken(), JsonToken.VALUE_NULL);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser.deserialize(p, ctxt);
        assertNull(result);
        p.close();
    }

    private void assertToken(JsonToken actual, JsonToken expected) {
        assertEquals("Unexpected token", expected, actual);
    }
}