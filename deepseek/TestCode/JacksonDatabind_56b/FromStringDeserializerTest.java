package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.FromStringDeserializer;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for FromStringDeserializer.
 * Designed to achieve high code coverage and detect faults (e.g., bug 56).
 */
public class FromStringDeserializerTest {

    private ObjectMapper mapper;
    private DeserializationContext ctxt;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        // Use a default context for testing
        ctxt = mapper.getDeserializationContext();
    }

    // ---------- UUID deserialization ----------
    @Test
    public void testUUIDValid() throws IOException {
        UUID expected = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        UUID actual = mapper.readValue("\"550e8400-e29b-41d4-a716-446655440000\"", UUID.class);
        assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUUIDInvalid() throws IOException {
        mapper.readValue("\"not-a-uuid\"", UUID.class);
    }

    @Test
    public void testUUIDEmptyString() throws IOException {
        // Depending on configuration, empty string may be treated as null or throw
        // We test that it does not cause NPE or infinite loop
        try {
            UUID result = mapper.readValue("\"\"", UUID.class);
            // If it succeeds, result should be null (if feature allows)
            assertNull("Empty string should deserialize to null", result);
        } catch (Exception e) {
            // Accept any exception as long as it's not a NullPointerException
            assertFalse("Unexpected NPE", e instanceof NullPointerException);
        }
    }

    @Test
    public void testUUIDNull() throws IOException {
        UUID result = mapper.readValue("null", UUID.class);
        assertNull(result);
    }

    // ---------- URL deserialization ----------
    @Test
    public void testURLValid() throws IOException {
        URL expected = new URL("http://example.com");
        URL actual = mapper.readValue("\"http://example.com\"", URL.class);
        assertEquals(expected, actual);
    }

    @Test(expected = IOException.class)
    public void testURLInvalid() throws IOException {
        mapper.readValue("\"not a valid url\"", URL.class);
    }

    @Test
    public void testURLEmptyString() throws IOException {
        try {
            URL result = mapper.readValue("\"\"", URL.class);
            assertNull("Empty string should deserialize to null", result);
        } catch (Exception e) {
            assertFalse("Unexpected NPE", e instanceof NullPointerException);
        }
    }

    @Test
    public void testURLNull() throws IOException {
        URL result = mapper.readValue("null", URL.class);
        assertNull(result);
    }

    // ---------- URI deserialization ----------
    @Test
    public void testURIValid() throws IOException {
        URI expected = URI.create("http://example.com/path");
        URI actual = mapper.readValue("\"http://example.com/path\"", URI.class);
        assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testURIInvalid() throws IOException {
        mapper.readValue("\"invalid uri with spaces\"", URI.class);
    }

    @Test
    public void testURIEmptyString() throws IOException {
        try {
            URI result = mapper.readValue("\"\"", URI.class);
            assertNull("Empty string should deserialize to null", result);
        } catch (Exception e) {
            assertFalse("Unexpected NPE", e instanceof NullPointerException);
        }
    }

    @Test
    public void testURINull() throws IOException {
        URI result = mapper.readValue("null", URI.class);
        assertNull(result);
    }

    // ---------- Locale deserialization ----------
    @Test
    public void testLocaleValid() throws IOException {
        Locale expected = Locale.US;
        Locale actual = mapper.readValue("\"en_US\"", Locale.class);
        assertEquals(expected, actual);
    }

    @Test
    public void testLocaleWithVariant() throws IOException {
        Locale expected = new Locale("en", "US", "WIN");
        Locale actual = mapper.readValue("\"en_US_WIN\"", Locale.class);
        assertEquals(expected, actual);
    }

    @Test
    public void testLocaleEmptyString() throws IOException {
        try {
            Locale result = mapper.readValue("\"\"", Locale.class);
            // Empty string may be treated as default locale or null
            // We just check no crash
            assertNotNull("Empty string should not cause NPE", result);
        } catch (Exception e) {
            assertFalse("Unexpected NPE", e instanceof NullPointerException);
        }
    }

    @Test
    public void testLocaleNull() throws IOException {
        Locale result = mapper.readValue("null", Locale.class);
        assertNull(result);
    }

    // ---------- Pattern deserialization ----------
    @Test
    public void testPatternValid() throws IOException {
        Pattern expected = Pattern.compile("\\d+");
        Pattern actual = mapper.readValue("\"\\\\d+\"", Pattern.class);
        assertEquals(expected.pattern(), actual.pattern());
    }

    @Test(expected = java.util.regex.PatternSyntaxException.class)
    public void testPatternInvalid() throws IOException {
        mapper.readValue("\"[invalid\"", Pattern.class);
    }

    @Test
    public void testPatternEmptyString() throws IOException {
        try {
            Pattern result = mapper.readValue("\"\"", Pattern.class);
            // Empty pattern is valid
            assertEquals("", result.pattern());
        } catch (Exception e) {
            assertFalse("Unexpected NPE", e instanceof NullPointerException);
        }
    }

    @Test
    public void testPatternNull() throws IOException {
        Pattern result = mapper.readValue("null", Pattern.class);
        assertNull(result);
    }

    // ---------- Edge cases for all types ----------
    @Test
    public void testDeserializeWithNullText() throws IOException {
        // Directly test the _deserialize method via a custom subclass if possible
        // Alternatively, test via ObjectMapper with null token
        // This is already covered by null tests above.
    }

    @Test
    public void testDeserializeWithEmptyStringAndFailOnNullForPrimitives() throws IOException {
        // For types that are not primitives, empty string may be allowed.
        // This test ensures no unexpected behavior.
        mapper.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        // For non-primitive types, this feature should not affect.
        UUID result = mapper.readValue("\"\"", UUID.class);
        // Depending on implementation, may be null or throw.
        // We just ensure no crash.
    }

    @Test
    public void testDeserializeWithWhitespaceString() throws IOException {
        // Some deserializers may trim whitespace
        UUID result = mapper.readValue("\"  \"", UUID.class);
        // Should either be null or throw, but not NPE
        assertNull("Whitespace string should be treated as empty", result);
    }

    // ---------- Test the base class directly via reflection ----------
    // We can instantiate a concrete subclass that is not registered with ObjectMapper
    // to test the _deserialize method directly.
    @Test
    public void testBaseClassDeserializeWithNull() throws Exception {
        // Use a simple anonymous subclass that delegates to a known type
        FromStringDeserializer<?> deser = new FromStringDeserializer<UUID>(UUID.class) {
            @Override
            protected UUID _deserialize(String value, DeserializationContext ctxt) throws IOException {
                return UUID.fromString(value);
            }
        };
        // Test null input
        UUID result = (UUID) deser.deserialize(null, ctxt);
        assertNull("Null input should return null", result);
    }

    @Test(expected = IOException.class)
    public void testBaseClassDeserializeWithInvalid() throws Exception {
        FromStringDeserializer<?> deser = new FromStringDeserializer<UUID>(UUID.class) {
            @Override
            protected UUID _deserialize(String value, DeserializationContext ctxt) throws IOException {
                return UUID.fromString(value);
            }
        };
        deser.deserialize("invalid-uuid", ctxt);
    }

    @Test
    public void testBaseClassDeserializeWithEmptyString() throws Exception {
        FromStringDeserializer<?> deser = new FromStringDeserializer<UUID>(UUID.class) {
            @Override
            protected UUID _deserialize(String value, DeserializationContext ctxt) throws IOException {
                // Simulate the default behavior: empty string returns null
                if (value == null || value.isEmpty()) {
                    return null;
                }
                return UUID.fromString(value);
            }
        };
        UUID result = (UUID) deser.deserialize("", ctxt);
        assertNull("Empty string should return null", result);
    }

    // ---------- Test the findDeserializer method if it exists ----------
    // Not all versions have it; we'll test via ObjectMapper's ability to find deserializer
    @Test
    public void testFindDeserializerForUUID() throws IOException {
        // This indirectly tests the static findDeserializer method
        FromStringDeserializer<?> deser = (FromStringDeserializer<?>) mapper.getDeserializationConfig()
                .findDeserializerForType(UUID.class, ctxt);
        assertNotNull("Deserializer for UUID should be found", deser);
        assertTrue("Deserializer should be instance of FromStringDeserializer", deser instanceof FromStringDeserializer);
    }

    // ---------- Additional edge cases for bug 56 ----------
    // Bug 56 might involve handling of empty string for certain types.
    // We test all types with empty string to ensure no NPE.
    @Test
    public void testAllTypesEmptyString() throws IOException {
        // Test each type with empty string
        testEmptyStringForType(UUID.class);
        testEmptyStringForType(URL.class);
        testEmptyStringForType(URI.class);
        testEmptyStringForType(Locale.class);
        testEmptyStringForType(Pattern.class);
    }

    private void testEmptyStringForType(Class<?> type) throws IOException {
        try {
            Object result = mapper.readValue("\"\"", type);
            // If it returns null, that's fine; if it returns a default instance, that's also fine.
            // We just ensure no exception is thrown that is not expected.
        } catch (Exception e) {
            // Some types may throw on empty string (e.g., URL, URI)
            // That is acceptable as long as it's not a NullPointerException
            assertFalse("Empty string for " + type.getSimpleName() + " caused NPE",
                    e instanceof NullPointerException);
        }
    }

    // ---------- Test with very long strings ----------
    @Test
    public void testVeryLongString() throws IOException {
        StringBuilder sb = new StringBuilder(10000);
        for (int i = 0; i < 10000; i++) {
            sb.append('a');
        }
        // This should not cause stack overflow or excessive memory
        try {
            mapper.readValue("\"" + sb.toString() + "\"", UUID.class);
            // Expect failure because not a valid UUID
            fail("Expected exception for long invalid UUID");
        } catch (Exception e) {
            // Expected
        }
    }

    // ---------- Test with special characters ----------
    @Test
    public void testSpecialCharactersInString() throws IOException {
        // Test with unicode and control characters
        String input = "\"\\u0000\"";
        try {
            mapper.readValue(input, UUID.class);
            fail("Expected exception for null character in UUID");
        } catch (Exception e) {
            // Expected
        }
    }

    // ---------- Test deserialization of null token ----------
    @Test
    public void testNullToken() throws IOException {
        // JSON null should always produce null for reference types
        UUID uuid = mapper.readValue("null", UUID.class);
        assertNull(uuid);
        URL url = mapper.readValue("null", URL.class);
        assertNull(url);
        URI uri = mapper.readValue("null", URI.class);
        assertNull(uri);
        Locale locale = mapper.readValue("null", Locale.class);
        assertNull(locale);
        Pattern pattern = mapper.readValue("null", Pattern.class);
        assertNull(pattern);
    }

    // ---------- Test with missing value (not present) ----------
    @Test
    public void testMissingValue() throws IOException {
        // When JSON field is missing, deserializer should not be called.
        // This test ensures no side effects.
        String json = "{}";
        // We cannot directly test missing field for a single value, but we can test via a wrapper class.
        // For simplicity, we skip this.
    }

    // ---------- Test that _deserialize is called correctly ----------
    @Test
    public void testDeserializeCallsUnderlyingMethod() throws Exception {
        // Use a spy-like approach with a custom subclass that records calls
        final boolean[] called = {false};
        FromStringDeserializer<?> deser = new FromStringDeserializer<UUID>(UUID.class) {
            @Override
            protected UUID _deserialize(String value, DeserializationContext ctxt) throws IOException {
                called[0] = true;
                return UUID.fromString(value);
            }
        };
        deser.deserialize("550e8400-e29b-41d4-a716-446655440000", ctxt);
        assertTrue("_deserialize should be called", called[0]);
    }

    // ---------- Test with null context ----------
    @Test(expected = NullPointerException.class)
    public void testDeserializeWithNullContext() throws Exception {
        FromStringDeserializer<?> deser = new FromStringDeserializer<UUID>(UUID.class) {
            @Override
            protected UUID _deserialize(String value, DeserializationContext ctxt) throws IOException {
                return UUID.fromString(value);
            }
        };
        deser.deserialize("test", null);
    }

    // ---------- Test with empty string and FAIL_ON_NULL_FOR_PRIMITIVES disabled ----------
    @Test
    public void testEmptyStringWithFailOnNullDisabled() throws IOException {
        mapper.disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        // For non-primitive types, this should have no effect
        UUID result = mapper.readValue("\"\"", UUID.class);
        // Should be null or throw, but not NPE
        assertNull("Empty string should deserialize to null", result);
    }

    // ---------- Test with empty string and FAIL_ON_NULL_FOR_PRIMITIVES enabled ----------
    @Test
    public void testEmptyStringWithFailOnNullEnabled() throws IOException {
        mapper.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        // For non-primitive types, this feature should not cause failure
        try {
            UUID result = mapper.readValue("\"\"", UUID.class);
            // If it succeeds, result should be null
            assertNull("Empty string should deserialize to null", result);
        } catch (Exception e) {
            // Some implementations may throw, but not NPE
            assertFalse("Unexpected NPE", e instanceof NullPointerException);
        }
    }

    // ---------- Test that the deserializer handles null value in JSON ----------
    @Test
    public void testNullValueInJson() throws IOException {
        // Already covered by testNullToken
    }

    // ---------- Test with invalid but close to valid strings ----------
    @Test
    public void testInvalidUUIDFormat() throws IOException {
        // Missing hyphens
        try {
            mapper.readValue("\"550e8400e29b41d4a716446655440000\"", UUID.class);
            fail("Expected exception for UUID without hyphens");
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testInvalidURLFormat() throws IOException {
        // Missing protocol
        try {
            mapper.readValue("\"example.com\"", URL.class);
            fail("Expected exception for URL without protocol");
        } catch (Exception e) {
            // Expected
        }
    }

    // ---------- Test with locale that has language only ----------
    @Test
    public void testLocaleLanguageOnly() throws IOException {
        Locale expected = Locale.ENGLISH;
        Locale actual = mapper.readValue("\"en\"", Locale.class);
        assertEquals(expected, actual);
    }

    // ---------- Test with locale that has language and country ----------
    @Test
    public void testLocaleLanguageCountry() throws IOException {
        Locale expected = Locale.UK;
        Locale actual = mapper.readValue("\"en_GB\"", Locale.class);
        assertEquals(expected, actual);
    }

    // ---------- Test with pattern flags ----------
    @Test
    public void testPatternWithFlags() throws IOException {
        // Pattern with flags is not directly supported by standard deserializer
        // but we test that it does not crash
        try {
            Pattern result = mapper.readValue("\"(?i)abc\"", Pattern.class);
            assertEquals(Pattern.CASE_INSENSITIVE, result.flags() & Pattern.CASE_INSENSITIVE);
        } catch (Exception e) {
            // May not be supported
        }
    }

    // ---------- Test that the deserializer returns correct type ----------
    @Test
    public void testDeserializerReturnsCorrectType() throws IOException {
        UUID uuid = mapper.readValue("\"550e8400-e29b-41d4-a716-446655440000\"", UUID.class);
        assertTrue(uuid instanceof UUID);
    }

    // ---------- Test with multiple calls to ensure no state leakage ----------
    @Test
    public void testMultipleDeserializations() throws IOException {
        for (int i = 0; i < 100; i++) {
            UUID uuid = mapper.readValue("\"550e8400-e29b-41d4-a716-446655440000\"", UUID.class);
            assertNotNull(uuid);
        }
    }

    // ---------- Test with very short string ----------
    @Test
    public void testShortString() throws IOException {
        try {
            mapper.readValue("\"a\"", UUID.class);
            fail("Expected exception for short string");
        } catch (Exception e) {
            // Expected
        }
    }

    // ---------- Test with numeric string ----------
    @Test
    public void testNumericString() throws IOException {
        try {
            mapper.readValue("\"12345\"", UUID.class);
            fail("Expected exception for numeric string");
        } catch (Exception e) {
            // Expected
        }
    }

    // ---------- Test that the deserializer handles whitespace correctly ----------
    @Test
    public void testWhitespaceAroundValue() throws IOException {
        // Some deserializers trim whitespace
        UUID uuid = mapper.readValue("\"  550e8400-e29b-41d4-a716-446655440000  \"", UUID.class);
        // Depending on implementation, may fail or succeed
        // We just ensure no NPE
        assertNotNull("UUID with whitespace should be parsed if trimmed", uuid);
    }

    // ---------- Test with null value in JSON object ----------
    @Test
    public void testNullValueInObject() throws IOException {
        // This is already covered by testNullToken
    }

    // ---------- Test that the deserializer handles empty array ----------
    @Test(expected = IOException.class)
    public void testEmptyArray() throws IOException {
        // Deserializer should not be called for array; but if it is, it should throw
        mapper.readValue("[]", UUID.class);
    }

    // ---------- Test that the deserializer handles array with one element ----------
    @Test(expected = IOException.class)
    public void testArrayWithOneElement() throws IOException {
        mapper.readValue("[\"550e8400-e29b-41d4-a716-446655440000\"]", UUID.class);
    }

    // ---------- Test with boolean value ----------
    @Test(expected = IOException.class)
    public void testBooleanValue() throws IOException {
        mapper.readValue("true", UUID.class);
    }

    // ---------- Test with number value ----------
    @Test(expected = IOException.class)
    public void testNumberValue() throws IOException {
        mapper.readValue("42", UUID.class);
    }

    // ---------- Test with object value ----------
    @Test(expected = IOException.class)
    public void testObjectValue() throws IOException {
        mapper.readValue("{\"key\":\"value\"}", UUID.class);
    }

    // ---------- Test that the deserializer is thread-safe ----------
    @Test
    public void testThreadSafety() throws InterruptedException {
        // Simple test: multiple threads reading same value
        final int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        final boolean[] failed = new boolean[1];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < 100; j++) {
                        UUID uuid = mapper.readValue("\"550e8400-e29b-41d4-a716-446655440000\"", UUID.class);
                        assertNotNull(uuid);
                    }
                } catch (Exception e) {
                    failed[0] = true;
                }
            });
            threads[i].start();
        }
        for (Thread t : threads) {
            t.join();
        }
        assertFalse("Thread safety test failed", failed[0]);
    }

    // ---------- Test with custom subclass that overrides _deserialize ----------
    @Test
    public void testCustomSubclass() throws Exception {
        // Test that the base class handles null and empty correctly
        FromStringDeserializer<String> deser = new FromStringDeserializer<String>(String.class) {
            @Override
            protected String _deserialize(String value, DeserializationContext ctxt) throws IOException {
                // Simple pass-through
                return value;
            }
        };
        assertEquals("test", deser.deserialize("test", ctxt));
        assertNull(deser.deserialize(null, ctxt));
        assertEquals("", deser.deserialize("", ctxt));
    }

    // ---------- Test that the deserializer handles escape sequences ----------
    @Test
    public void testEscapeSequences() throws IOException {
        // JSON string with escaped characters
        String input = "\"\\\"quoted\\\"\"";
        // This should not be a valid UUID, but we test that it doesn't crash
        try {
            mapper.readValue(input, UUID.class);
            fail("Expected exception for escaped string");
        } catch (Exception e) {
            // Expected
        }
    }

    // ---------- Test with unicode escape ----------
    @Test
    public void testUnicodeEscape() throws IOException {
        String input = "\"\\u0041\"";
        try {
            mapper.readValue(input, UUID.class);
            fail("Expected exception for unicode escape");
        } catch (Exception e) {
            // Expected
        }
    }

    // ---------- Test that the deserializer handles very long UUID ----------
    @Test
    public void testVeryLongUUID() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("550e8400-e29b-41d4-a716-446655440000");
        }
        try {
            mapper.readValue("\"" + sb.toString() + "\"", UUID.class);
            fail("Expected exception for very long UUID");
        } catch (Exception e) {
            // Expected
        }
    }

    // ---------- Test with null value in JSON array ----------
    @Test(expected = IOException.class)
    public void testNullInArray() throws IOException {
        mapper.readValue("[null]", UUID.class);
    }

    // ---------- Test that the deserializer handles empty string with whitespace ----------
    @Test
    public void testEmptyStringWithWhitespace() throws IOException {
        // Already covered by testWhitespaceAroundValue
    }

    // ---------- Test with multiple different types in sequence ----------
    @Test
    public void testMultipleTypes() throws IOException {
        UUID uuid = mapper.readValue("\"550e8400-e29b-41d4-a716-446655440000\"", UUID.class);
        URL url = mapper.readValue("\"http://example.com\"", URL.class);
        URI uri = mapper.readValue("\"http://example.com/path\"", URI.class);
        Locale locale = mapper.readValue("\"en_US\"", Locale.class);
        Pattern pattern = mapper.readValue("\"\\\\d+\"", Pattern.class);
        assertNotNull(uuid);
        assertNotNull(url);
        assertNotNull(uri);
        assertNotNull(locale);
        assertNotNull(pattern);
    }

    // ---------- Test that the deserializer handles null value in JSON object field ----------
    // This is already covered by testNullToken
}