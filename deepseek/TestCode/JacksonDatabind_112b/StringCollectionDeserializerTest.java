package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for StringCollectionDeserializer.
 * Covers edge cases, branch coverage, and potential bug patterns
 * (JacksonDatabind bug 112).
 */
public class StringCollectionDeserializerTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        // Use strict type handling to expose mismatches
        mapper.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        mapper.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
    }

    // ----------------------------------------------------------
    // Deserialization to List<String>
    // ----------------------------------------------------------

    @Test
    public void testListStringValid() throws Exception {
        List<String> result = mapper.readValue("[\"a\",\"b\",\"c\"]",
                new TypeReference<List<String>>() {});
        assertEquals(Arrays.asList("a","b","c"), result);
    }

    @Test
    public void testListStringEmpty() throws Exception {
        List<String> result = mapper.readValue("[]",
                new TypeReference<List<String>>() {});
        assertTrue(result.isEmpty());
    }

    @Test
    public void testListStringSingle() throws Exception {
        List<String> result = mapper.readValue("[\"only\"]",
                new TypeReference<List<String>>() {});
        assertEquals(Collections.singletonList("only"), result);
    }

    @Test
    public void testListStringWithNull() throws Exception {
        List<String> result = mapper.readValue("[\"a\", null, \"b\"]",
                new TypeReference<List<String>>() {});
        assertEquals(Arrays.asList("a", null, "b"), result);
    }

    @Test
    public void testListStringAllNulls() throws Exception {
        List<String> result = mapper.readValue("[null,null]",
                new TypeReference<List<String>>() {});
        assertEquals(Arrays.asList(null, null), result);
    }

    @Test(expected = MismatchedInputException.class)
    public void testListStringNotArray() throws Exception {
        mapper.readValue("\"notAnArray\"",
                new TypeReference<List<String>>() {});
    }

    @Test(expected = MismatchedInputException.class)
    public void testListStringInvalidElement() throws Exception {
        mapper.readValue("[1,2,3]",
                new TypeReference<List<String>>() {});
    }

    @Test
    public void testListStringWithSpecialChars() throws Exception {
        List<String> result = mapper.readValue("[\"hello\\nworld\", \"tab\\ttab\"]",
                new TypeReference<List<String>>() {});
        assertEquals(Arrays.asList("hello\nworld", "tab\ttab"), result);
    }

    @Test
    public void testListStringEscapedQuotes() throws Exception {
        List<String> result = mapper.readValue("[\"\\\"quote\\\"\"]",
                new TypeReference<List<String>>() {});
        assertEquals(Collections.singletonList("\"quote\""), result);
    }

    // ----------------------------------------------------------
    // Deserialization to Set<String>
    // ----------------------------------------------------------

    @Test
    public void testSetStringValid() throws Exception {
        Set<String> result = mapper.readValue("[\"x\",\"y\",\"z\"]",
                new TypeReference<Set<String>>() {});
        assertEquals(new HashSet<>(Arrays.asList("x","y","z")), result);
    }

    @Test
    public void testSetStringEmpty() throws Exception {
        Set<String> result = mapper.readValue("[]",
                new TypeReference<Set<String>>() {});
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSetStringWithDuplicates() throws Exception {
        Set<String> result = mapper.readValue("[\"a\",\"a\",\"b\"]",
                new TypeReference<Set<String>>() {});
        assertEquals(new HashSet<>(Arrays.asList("a","b")), result);
    }

    // ----------------------------------------------------------
    // Deserialization to Collection<String> (abstract)
    // ----------------------------------------------------------

    @Test
    public void testCollectionStringValid() throws Exception {
        Collection<String> result = mapper.readValue("[\"a\",\"b\"]",
                new TypeReference<Collection<String>>() {});
        // Default concrete type is ArrayList
        assertEquals(Arrays.asList("a","b"), new ArrayList<>(result));
    }

    @Test
    public void testCollectionStringEmpty() throws Exception {
        Collection<String> result = mapper.readValue("[]",
                new TypeReference<Collection<String>>() {});
        assertTrue(result.isEmpty());
    }

    // ----------------------------------------------------------
    // Edge cases and boundary values
    // ----------------------------------------------------------

    @Test
    public void testListStringVeryLarge() throws Exception {
        StringBuilder sb = new StringBuilder("[");
        int count = 1000;
        for (int i = 0; i < count; i++) {
            if (i > 0) sb.append(",");
            sb.append("\"str").append(i).append("\"");
        }
        sb.append("]");
        List<String> result = mapper.readValue(sb.toString(),
                new TypeReference<List<String>>() {});
        assertEquals(count, result.size());
        assertEquals("str0", result.get(0));
        assertEquals("str999", result.get(count-1));
    }

    @Test
    public void testListStringWithEmptyString() throws Exception {
        List<String> result = mapper.readValue("[\"\"]",
                new TypeReference<List<String>>() {});
        assertEquals(Collections.singletonList(""), result);
    }

    @Test
    public void testListStringWithWhitespaceElements() throws Exception {
        List<String> result = mapper.readValue("[\"  \", \" \t \", \"\"]",
                new TypeReference<List<String>>() {});
        assertEquals(Arrays.asList("  ", " \t ", ""), result);
    }

    @Test
    public void testListStringNullInput() throws Exception {
        // Note: FAIL_ON_NULL_FOR_PRIMITIVES does not affect String collections
        List<String> result = mapper.readValue("null",
                new TypeReference<List<String>>() {});
        assertNull(result);
    }

    // ----------------------------------------------------------
    // Deserialization with custom reader (to exercise more code paths)
    // ----------------------------------------------------------

    @Test
    public void testListStringWithReader() throws Exception {
        ObjectReader reader = mapper.readerFor(new TypeReference<List<String>>() {});
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) reader.readValue("[\"viaReader\"]");
        assertEquals(Collections.singletonList("viaReader"), result);
    }

    // ----------------------------------------------------------
    // Bug-prone scenarios inspired by Defects4J bug 112
    // ----------------------------------------------------------

    @Test
    public void testListStringWithNestedArray() throws Exception {
        // Expect failure; a nested array is not a valid String
        try {
            mapper.readValue("[[\"inner\"]]",
                    new TypeReference<List<String>>() {});
            fail("Expected MismatchedInputException for nested array");
        } catch (MismatchedInputException e) {
            // expected
        }
    }

    @Test
    public void testListStringWithObject() throws Exception {
        // Expect failure; object is not a valid String
        try {
            mapper.readValue("[{\"key\":\"value\"}]",
                    new TypeReference<List<String>>() {});
            fail("Expected MismatchedInputException for object element");
        } catch (MismatchedInputException e) {
            // expected
        }
    }

    @Test
    public void testListStringEmptyElementAfterNull() throws Exception {
        // Potential bug: handling of empty element after null
        List<String> result = mapper.readValue("[null, \"\"]",
                new TypeReference<List<String>>() {});
        assertEquals(Arrays.asList(null, ""), result);
    }

    @Test
    public void testListStringTypeResolutionWithGenerics() throws Exception {
        // Test that StringCollectionDeserializer correctly resolves content type
        // when used with a generic subtype
        List<String> result = mapper.readValue("[\"a\",\"b\"]",
                new TypeReference<ArrayList<String>>() {});
        assertEquals(Arrays.asList("a","b"), result);
    }

    @Test
    public void testSetStringTypeResolution() throws Exception {
        // Ensure Set<String> uses correct deserializer for String elements
        Set<String> result = mapper.readValue("[\"a\",\"b\"]",
                new TypeReference<LinkedHashSet<String>>() {});
        assertEquals(new LinkedHashSet<>(Arrays.asList("a","b")), result);
    }

    // ----------------------------------------------------------
    // Additional edge cases: non-standard collection implementations
    // ----------------------------------------------------------

    @Test
    public void testListStringAsLinkedList() throws Exception {
        List<String> result = mapper.readValue("[\"a\",\"b\"]",
                new TypeReference<LinkedList<String>>() {});
        assertEquals(new LinkedList<>(Arrays.asList("a","b")), result);
    }

    @Test
    public void testListStringWithAccentCharacters() throws Exception {
        List<String> result = mapper.readValue("[\"é\",\"ü\",\"ñ\"]",
                new TypeReference<List<String>>() {});
        assertEquals(Arrays.asList("é","ü","ñ"), result);
    }

    @Test
    public void testListStringWithUnicodeEscape() throws Exception {
        List<String> result = mapper.readValue("[\"\\u0041\"]",
                new TypeReference<List<String>>() {});
        assertEquals(Collections.singletonList("A"), result);
    }

    // ----------------------------------------------------------
    // Tests focusing on deserialization context handling
    // ----------------------------------------------------------

    @Test
    public void testListStringWithDeserializationFeatureEnabled() throws Exception {
        // Enable single value as array to test alternative path
        ObjectMapper localMapper = new ObjectMapper();
        localMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        List<String> result = localMapper.readValue("\"singleString\"",
                new TypeReference<List<String>>() {});
        assertEquals(Collections.singletonList("singleString"), result);
    }

    @Test
    public void testListStringFailOnNullForPrimitivesDisabled() throws Exception {
        // This should have no effect on String collections
        ObjectMapper localMapper = new ObjectMapper();
        localMapper.disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        List<String> result = localMapper.readValue("[null]",
                new TypeReference<List<String>>() {});
        assertEquals(Collections.singletonList(null), result);
    }
}