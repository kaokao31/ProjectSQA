package com.fasterxml.jackson.databind.deser.impl;

import static org.junit.Assert.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import java.util.*;

import org.junit.Before;
import org.junit.Test;

public class JavaUtilCollectionsDeserializersTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    // ---- Singleton List ----
    @Test
    public void testSingletonListFromOneElementArray() throws Exception {
        List<String> result = mapper.readValue("[\"a\"]",
                new TypeReference<List<String>>() {});
        assertEquals(1, result.size());
        assertEquals("a", result.get(0));
        // Should be a singleton list (not modifiable)
        assertThrows(UnsupportedOperationException.class, () -> result.add("b"));
    }

    @Test(expected = MismatchedInputException.class)
    public void testSingletonListFromEmptyArrayFails() throws Exception {
        mapper.readValue("[]",
                new TypeReference<List<String>>() {});
    }

    @Test(expected = MismatchedInputException.class)
    public void testSingletonListFromMultiElementArrayFails() throws Exception {
        mapper.readValue("[\"a\", \"b\"]",
                new TypeReference<List<String>>() {});
    }

    // ---- Singleton Set ----
    @Test
    public void testSingletonSetFromOneElementArray() throws Exception {
        Set<String> result = mapper.readValue("[\"x\"]",
                new TypeReference<Set<String>>() {});
        assertEquals(1, result.size());
        assertTrue(result.contains("x"));
        assertThrows(UnsupportedOperationException.class, () -> result.add("y"));
    }

    @Test(expected = MismatchedInputException.class)
    public void testSingletonSetFromEmptyArrayFails() throws Exception {
        mapper.readValue("[]",
                new TypeReference<Set<String>>() {});
    }

    @Test(expected = MismatchedInputException.class)
    public void testSingletonSetFromMultiElementArrayFails() throws Exception {
        mapper.readValue("[\"1\",\"2\"]",
                new TypeReference<Set<String>>() {});
    }

    // ---- Singleton Map ----
    @Test
    public void testSingletonMapFromOneEntryObject() throws Exception {
        Map<String, Integer> result = mapper.readValue("{\"key\": 42}",
                new TypeReference<Map<String, Integer>>() {});
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(42), result.get("key"));
        assertThrows(UnsupportedOperationException.class, () -> result.put("new", 0));
    }

    @Test(expected = MismatchedInputException.class)
    public void testSingletonMapFromEmptyObjectFails() throws Exception {
        mapper.readValue("{}",
                new TypeReference<Map<String, Integer>>() {});
    }

    @Test(expected = MismatchedInputException.class)
    public void testSingletonMapFromMultiEntryObjectFails() throws Exception {
        mapper.readValue("{\"a\":1,\"b\":2}",
                new TypeReference<Map<String, Integer>>() {});
    }

    // ---- Unmodifiable List (via Collections.unmodifiableList) ----
    @Test
    public void testUnmodifiableListFromArray() throws Exception {
        List<String> result = mapper.readValue("[\"a\",\"b\"]",
                new TypeReference<List<String>>() {});
        assertEquals(2, result.size());
        // Unmodifiable list should throw on modification
        assertThrows(UnsupportedOperationException.class, () -> result.add("c"));
    }

    // Test sink for Edge Cases - NULL
    @Test(expected = MismatchedInputException.class)
    public void testSingletonListFromNullFails() throws Exception {
        mapper.readValue("null",
                new TypeReference<List<String>>() {});
    }

    // Test that we can read the type as a general List and it still is singleton when appropriate
    @Test
    public void testSingletonListTypeErasurePreservesBehavior() throws Exception {
        Object raw = mapper.readValue("[\"only\"]", List.class);
        assertTrue(raw instanceof List);
        List<?> list = (List<?>) raw;
        assertEquals(1, list.size());
        assertThrows(UnsupportedOperationException.class, () -> list.add("extra"));
    }

    // ---- Empty List/Sets/Maps (not singleton) ----
    @Test
    public void testEmptyListDeserialization() throws Exception {
        List<String> result = mapper.readValue("[]",
                mapper.getTypeFactory().constructCollectionType(List.class, String.class));
        assertEquals(0, result.size());
    }

    @Test
    public void testEmptySetDeserialization() throws Exception {
        Set<String> result = mapper.readValue("[]",
                mapper.getTypeFactory().constructCollectionType(Set.class, String.class));
        assertEquals(0, result.size());
    }

    @Test
    public void testEmptyMapDeserialization() throws Exception {
        Map<String, String> result = mapper.readValue("{}",
                mapper.getTypeFactory().constructMapType(Map.class, String.class, String.class));
        assertEquals(0, result.size());
    }

    // ---- Unmodifiable Set (via Collections.unmodifiableSet) ----
    @Test
    public void testUnmodifiableSetFromArray() throws Exception {
        Set<String> result = mapper.readValue("[\"a\",\"b\"]",
                new TypeReference<Set<String>>() {});
        assertEquals(2, result.size());
        assertThrows(UnsupportedOperationException.class, () -> result.add("c"));
    }

    // ---- Unmodifiable Map (via Collections.unmodifiableMap) ----
    @Test
    public void testUnmodifiableMapFromObject() throws Exception {
        Map<String, Integer> result = mapper.readValue("{\"x\":1,\"y\":2}",
                new TypeReference<Map<String, Integer>>() {});
        assertEquals(2, result.size());
        assertThrows(UnsupportedOperationException.class, () -> result.put("z", 3));
    }

    // ---- Edge Cases: Non‑empty collections that are NOT singletons but are unmodifiable ----
    // The deserializer may return an unmodifiable collection for any array/object,
    // or only for specific sizes. We test that both work.

    @Test
    public void testSingleElementListAsUnmodifiable() throws Exception {
        List<Integer> result = mapper.readValue("[999]",
                new TypeReference<List<Integer>>() {});
        assertEquals(1, result.size());
        assertThrows(UnsupportedOperationException.class, () -> result.add(0));
    }
}