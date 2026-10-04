package com.fasterxml.jackson.core.filter;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.filter.FilteringParserDelegate;
import com.fasterxml.jackson.core.filter.JsonPointerBasedFilter;
import com.fasterxml.jackson.core.filter.TokenFilter;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for FilteringParserDelegate.
 * Designed to achieve high coverage and detect faults (e.g., Defects4J bug 15).
 */
public class FilteringParserDelegateTest {

    private JsonFactory factory;

    @Before
    public void setUp() {
        factory = new JsonFactory();
    }

    // Helper to parse a JSON string into a JsonParser
    private JsonParser createParser(String json) throws IOException {
        return factory.createParser(json);
    }

    // Helper to create a FilteringParserDelegate with a JsonPointer filter
    private FilteringParserDelegate createFilteredParser(String json, String pointer) throws IOException {
        JsonParser delegate = createParser(json);
        TokenFilter filter = new JsonPointerBasedFilter(pointer);
        return new FilteringParserDelegate(delegate, filter, false, false);
    }

    // Helper to read all tokens from a FilteringParserDelegate and return as a string
    private String readTokens(FilteringParserDelegate parser) throws IOException {
        StringBuilder sb = new StringBuilder();
        while (parser.nextToken() != null) {
            sb.append(parser.getCurrentToken().toString());
            if (parser.getCurrentToken().isScalarValue()) {
                sb.append(":").append(parser.getText());
            }
            sb.append(" ");
        }
        return sb.toString().trim();
    }

    // ============================================================
    // Basic filtering tests
    // ============================================================

    @Test
    public void testSimpleObjectFilter() throws IOException {
        String json = "{\"a\":1, \"b\":2}";
        FilteringParserDelegate parser = createFilteredParser(json, "/a");
        String result = readTokens(parser);
        assertEquals("START_OBJECT FIELD_NAME:a VALUE_NUMBER_INT:1 END_OBJECT", result);
    }

    @Test
    public void testArrayFilter() throws IOException {
        String json = "{\"arr\":[1,2,3]}";
        FilteringParserDelegate parser = createFilteredParser(json, "/arr/1");
        String result = readTokens(parser);
        assertEquals("START_OBJECT FIELD_NAME:arr START_ARRAY VALUE_NUMBER_INT:2 END_ARRAY END_OBJECT", result);
    }

    @Test
    public void testNestedFilter() throws IOException {
        String json = "{\"x\":{\"y\":3}}";
        FilteringParserDelegate parser = createFilteredParser(json, "/x/y");
        String result = readTokens(parser);
        assertEquals("START_OBJECT FIELD_NAME:x START_OBJECT FIELD_NAME:y VALUE_NUMBER_INT:3 END_OBJECT END_OBJECT", result);
    }

    // ============================================================
    // Edge cases targeting Defects4J bug 15 (NPE on empty array)
    // ============================================================

    @Test
    public void testEmptyArrayFilter() throws IOException {
        String json = "{\"arr\":[]}";
        FilteringParserDelegate parser = createFilteredParser(json, "/arr");
        String result = readTokens(parser);
        // Should produce START_OBJECT, FIELD_NAME, START_ARRAY, END_ARRAY, END_OBJECT
        assertEquals("START_OBJECT FIELD_NAME:arr START_ARRAY END_ARRAY END_OBJECT", result);
    }

    @Test
    public void testEmptyArrayWithIndexFilter() throws IOException {
        String json = "{\"arr\":[]}";
        FilteringParserDelegate parser = createFilteredParser(json, "/arr/0");
        String result = readTokens(parser);
        // No matching element, so should produce nothing? Actually, the filter matches the path but no value.
        // Depending on implementation, might produce START_OBJECT, FIELD_NAME, START_ARRAY, END_ARRAY, END_OBJECT
        // or just nothing. We'll check that no exception is thrown.
        assertNotNull(result);
        // The expected behavior: the array is empty, so no token for index 0, but the array itself is included.
        // Let's assert that the result contains the array markers.
        assertTrue(result.contains("START_ARRAY"));
        assertTrue(result.contains("END_ARRAY"));
    }

    @Test
    public void testEmptyNestedObjectFilter() throws IOException {
        String json = "{\"a\":{}}";
        FilteringParserDelegate parser = createFilteredParser(json, "/a");
        String result = readTokens(parser);
        assertEquals("START_OBJECT FIELD_NAME:a START_OBJECT END_OBJECT END_OBJECT", result);
    }

    // ============================================================
    // Multiple matches and wildcard (if supported)
    // ============================================================

    @Test
    public void testMultipleMatches() throws IOException {
        String json = "{\"a\":1, \"b\":2, \"a\":3}"; // duplicate keys allowed in streaming
        FilteringParserDelegate parser = createFilteredParser(json, "/a");
        String result = readTokens(parser);
        // Should include both "a" fields
        assertTrue(result.contains("VALUE_NUMBER_INT:1"));
        assertTrue(result.contains("VALUE_NUMBER_INT:3"));
    }

    // ============================================================
    // Null and boundary inputs
    // ============================================================

    @Test(expected = NullPointerException.class)
    public void testNullDelegate() {
        new FilteringParserDelegate(null, new JsonPointerBasedFilter("/a"), false, false);
    }

    @Test(expected = NullPointerException.class)
    public void testNullFilter() throws IOException {
        JsonParser delegate = createParser("{}");
        new FilteringParserDelegate(delegate, null, false, false);
    }

    @Test
    public void testEmptyJson() throws IOException {
        String json = "";
        JsonParser delegate = createParser(json);
        TokenFilter filter = new JsonPointerBasedFilter("/a");
        FilteringParserDelegate parser = new FilteringParserDelegate(delegate, filter, false, false);
        // Should have no tokens
        assertNull(parser.nextToken());
    }

    @Test
    public void testWhitespaceJson() throws IOException {
        String json = "   ";
        JsonParser delegate = createParser(json);
        TokenFilter filter = new JsonPointerBasedFilter("/a");
        FilteringParserDelegate parser = new FilteringParserDelegate(delegate, filter, false, false);
        assertNull(parser.nextToken());
    }

    // ============================================================
    // Test with matchPath and matchElement flags
    // ============================================================

    @Test
    public void testMatchPathFlag() throws IOException {
        // When matchPath is true, the filter matches the path itself, not just the value.
        String json = "{\"a\":1}";
        JsonParser delegate = createParser(json);
        TokenFilter filter = new JsonPointerBasedFilter("/a");
        FilteringParserDelegate parser = new FilteringParserDelegate(delegate, filter, true, false);
        String result = readTokens(parser);
        // Should include the field name and value
        assertEquals("START_OBJECT FIELD_NAME:a VALUE_NUMBER_INT:1 END_OBJECT", result);
    }

    @Test
    public void testMatchElementFlag() throws IOException {
        // matchElement: if true, filter matches array elements
        String json = "{\"arr\":[1,2]}";
        JsonParser delegate = createParser(json);
        TokenFilter filter = new JsonPointerBasedFilter("/arr/0");
        FilteringParserDelegate parser = new FilteringParserDelegate(delegate, filter, false, true);
        String result = readTokens(parser);
        // Should include the array and the matched element
        assertTrue(result.contains("START_ARRAY"));
        assertTrue(result.contains("VALUE_NUMBER_INT:1"));
        assertTrue(result.contains("END_ARRAY"));
    }

    // ============================================================
    // Test with deep nesting and multiple levels
    // ============================================================

    @Test
    public void testDeepNesting() throws IOException {
        String json = "{\"a\":{\"b\":{\"c\":[1,2,3]}}}";
        FilteringParserDelegate parser = createFilteredParser(json, "/a/b/c/2");
        String result = readTokens(parser);
        assertEquals("START_OBJECT FIELD_NAME:a START_OBJECT FIELD_NAME:b START_OBJECT FIELD_NAME:c START_ARRAY VALUE_NUMBER_INT:3 END_ARRAY END_OBJECT END_OBJECT END_OBJECT", result);
    }

    // ============================================================
    // Test with non-existent path
    // ============================================================

    @Test
    public void testNonExistentPath() throws IOException {
        String json = "{\"a\":1}";
        FilteringParserDelegate parser = createFilteredParser(json, "/b");
        String result = readTokens(parser);
        // Should produce no tokens (empty string)
        assertEquals("", result);
    }

    // ============================================================
    // Test with array index out of bounds
    // ============================================================

    @Test
    public void testArrayIndexOutOfBounds() throws IOException {
        String json = "{\"arr\":[1,2]}";
        FilteringParserDelegate parser = createFilteredParser(json, "/arr/5");
        String result = readTokens(parser);
        // Should produce array tokens but no value
        assertTrue(result.contains("START_ARRAY"));
        assertTrue(result.contains("END_ARRAY"));
        assertFalse(result.contains("VALUE_NUMBER_INT"));
    }

    // ============================================================
    // Test with negative array index (should be invalid)
    // ============================================================

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeArrayIndex() throws IOException {
        String json = "{\"arr\":[1,2]}";
        // JsonPointer does not support negative indices; this should throw
        createFilteredParser(json, "/arr/-1");
    }

    // ============================================================
    // Test with special characters in field names
    // ============================================================

    @Test
    public void testSpecialCharactersInField() throws IOException {
        String json = "{\"field/name\":\"value\"}";
        // Escape slash in pointer: "/field~1name"
        FilteringParserDelegate parser = createFilteredParser(json, "/field~1name");
        String result = readTokens(parser);
        assertEquals("START_OBJECT FIELD_NAME:field/name VALUE_STRING:value END_OBJECT", result);
    }

    // ============================================================
    // Test with multiple filters (not directly supported, but can chain)
    // ============================================================

    // Not applicable; FilteringParserDelegate takes a single filter.

    // ============================================================
    // Test with null token in stream (simulate via custom parser?)
    // Not easily done; skip.

    // ============================================================
    // Test close() and getCurrentLocation()
    // ============================================================

    @Test
    public void testClose() throws IOException {
        String json = "{\"a\":1}";
        FilteringParserDelegate parser = createFilteredParser(json, "/a");
        parser.close();
        // After close, nextToken should return null
        assertNull(parser.nextToken());
    }

    @Test
    public void testGetCurrentLocation() throws IOException {
        String json = "{\"a\":1}";
        FilteringParserDelegate parser = createFilteredParser(json, "/a");
        assertNotNull(parser.getCurrentLocation());
        parser.nextToken();
        assertNotNull(parser.getCurrentLocation());
    }

    // ============================================================
    // Test with empty pointer (root)
    // ============================================================

    @Test
    public void testEmptyPointer() throws IOException {
        String json = "{\"a\":1}";
        FilteringParserDelegate parser = createFilteredParser(json, "");
        String result = readTokens(parser);
        // Should return the whole document
        assertEquals("START_OBJECT FIELD_NAME:a VALUE_NUMBER_INT:1 END_OBJECT", result);
    }

    // ============================================================
    // Test with array root
    // ============================================================

    @Test
    public void testArrayRoot() throws IOException {
        String json = "[1,2,3]";
        FilteringParserDelegate parser = createFilteredParser(json, "/0");
        String result = readTokens(parser);
        assertEquals("START_ARRAY VALUE_NUMBER_INT:1 END_ARRAY", result);
    }

    // ============================================================
    // Test with multiple levels and array inside object
    // ============================================================

    @Test
    public void testMixedNested() throws IOException {
        String json = "{\"data\":[{\"id\":1},{\"id\":2}]}";
        FilteringParserDelegate parser = createFilteredParser(json, "/data/0/id");
        String result = readTokens(parser);
        assertEquals("START_OBJECT FIELD_NAME:data START_ARRAY START_OBJECT FIELD_NAME:id VALUE_NUMBER_INT:1 END_OBJECT END_ARRAY END_OBJECT", result);
    }
}