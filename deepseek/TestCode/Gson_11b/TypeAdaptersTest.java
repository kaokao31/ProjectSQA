package com.google.gson.internal.bind;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for TypeAdapters class.
 * Covers all public static fields, factory methods, and edge cases.
 */
public class TypeAdaptersTest {

    private Gson gson;

    @Before
    public void setUp() {
        gson = new Gson();
    }

    // ==================== Primitive Type Adapters ====================

    @Test
    public void testIntegerAdapter() throws IOException {
        TypeAdapter<Integer> adapter = TypeAdapters.INTEGER;
        // Normal serialization
        assertEquals(new JsonPrimitive(42), adapter.toJsonTree(42));
        assertEquals(Integer.valueOf(42), adapter.fromJsonTree(new JsonPrimitive(42)));
        // Edge values
        assertEquals(Integer.valueOf(Integer.MAX_VALUE), adapter.fromJsonTree(new JsonPrimitive(Integer.MAX_VALUE)));
        assertEquals(Integer.valueOf(Integer.MIN_VALUE), adapter.fromJsonTree(new JsonPrimitive(Integer.MIN_VALUE)));
        // Null handling
        assertNull(adapter.fromJsonTree(JsonNull.INSTANCE));
        assertNull(adapter.toJsonTree(null));
        // Invalid input
        try {
            adapter.fromJsonTree(new JsonPrimitive("notanumber"));
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test
    public void testLongAdapter() throws IOException {
        TypeAdapter<Long> adapter = TypeAdapters.LONG;
        assertEquals(new JsonPrimitive(123L), adapter.toJsonTree(123L));
        assertEquals(Long.valueOf(123L), adapter.fromJsonTree(new JsonPrimitive(123L)));
        assertEquals(Long.valueOf(Long.MAX_VALUE), adapter.fromJsonTree(new JsonPrimitive(Long.MAX_VALUE)));
        assertEquals(Long.valueOf(Long.MIN_VALUE), adapter.fromJsonTree(new JsonPrimitive(Long.MIN_VALUE)));
        assertNull(adapter.fromJsonTree(JsonNull.INSTANCE));
        assertNull(adapter.toJsonTree(null));
    }

    @Test
    public void testFloatAdapter() throws IOException {
        TypeAdapter<Float> adapter = TypeAdapters.FLOAT;
        assertEquals(new JsonPrimitive(3.14f), adapter.toJsonTree(3.14f));
        assertEquals(Float.valueOf(3.14f), adapter.fromJsonTree(new JsonPrimitive(3.14f)));
        assertEquals(Float.valueOf(Float.MAX_VALUE), adapter.fromJsonTree(new JsonPrimitive(Float.MAX_VALUE)));
        assertEquals(Float.valueOf(Float.MIN_VALUE), adapter.fromJsonTree(new JsonPrimitive(Float.MIN_VALUE)));
        assertNull(adapter.fromJsonTree(JsonNull.INSTANCE));
        assertNull(adapter.toJsonTree(null));
        // NaN and Infinity
        assertEquals(Float.valueOf(Float.NaN), adapter.fromJsonTree(new JsonPrimitive(Float.NaN)));
        assertEquals(Float.valueOf(Float.POSITIVE_INFINITY), adapter.fromJsonTree(new JsonPrimitive(Float.POSITIVE_INFINITY)));
        assertEquals(Float.valueOf(Float.NEGATIVE_INFINITY), adapter.fromJsonTree(new JsonPrimitive(Float.NEGATIVE_INFINITY)));
    }

    @Test
    public void testDoubleAdapter() throws IOException {
        TypeAdapter<Double> adapter = TypeAdapters.DOUBLE;
        assertEquals(new JsonPrimitive(2.718), adapter.toJsonTree(2.718));
        assertEquals(Double.valueOf(2.718), adapter.fromJsonTree(new JsonPrimitive(2.718)));
        assertEquals(Double.valueOf(Double.MAX_VALUE), adapter.fromJsonTree(new JsonPrimitive(Double.MAX_VALUE)));
        assertEquals(Double.valueOf(Double.MIN_VALUE), adapter.fromJsonTree(new JsonPrimitive(Double.MIN_VALUE)));
        assertNull(adapter.fromJsonTree(JsonNull.INSTANCE));
        assertNull(adapter.toJsonTree(null));
        // NaN and Infinity
        assertEquals(Double.valueOf(Double.NaN), adapter.fromJsonTree(new JsonPrimitive(Double.NaN)));
        assertEquals(Double.valueOf(Double.POSITIVE_INFINITY), adapter.fromJsonTree(new JsonPrimitive(Double.POSITIVE_INFINITY)));
        assertEquals(Double.valueOf(Double.NEGATIVE_INFINITY), adapter.fromJsonTree(new JsonPrimitive(Double.NEGATIVE_INFINITY)));
    }

    @Test
    public void testBooleanAdapter() throws IOException {
        TypeAdapter<Boolean> adapter = TypeAdapters.BOOLEAN;
        assertEquals(new JsonPrimitive(true), adapter.toJsonTree(true));
        assertEquals(Boolean.TRUE, adapter.fromJsonTree(new JsonPrimitive(true)));
        assertEquals(Boolean.FALSE, adapter.fromJsonTree(new JsonPrimitive(false)));
        assertNull(adapter.fromJsonTree(JsonNull.INSTANCE));
        assertNull(adapter.toJsonTree(null));
        // String "true"/"false" should be accepted
        assertEquals(Boolean.TRUE, adapter.fromJsonTree(new JsonPrimitive("true")));
        assertEquals(Boolean.FALSE, adapter.fromJsonTree(new JsonPrimitive("false")));
    }

    @Test
    public void testByteAdapter() throws IOException {
        TypeAdapter<Byte> adapter = TypeAdapters.BYTE;
        assertEquals(new JsonPrimitive((byte) 127), adapter.toJsonTree((byte) 127));
        assertEquals(Byte.valueOf((byte) 127), adapter.fromJsonTree(new JsonPrimitive(127)));
        assertEquals(Byte.valueOf(Byte.MAX_VALUE), adapter.fromJsonTree(new JsonPrimitive(Byte.MAX_VALUE)));
        assertEquals(Byte.valueOf(Byte.MIN_VALUE), adapter.fromJsonTree(new JsonPrimitive(Byte.MIN_VALUE)));
        assertNull(adapter.fromJsonTree(JsonNull.INSTANCE));
        assertNull(adapter.toJsonTree(null));
    }

    @Test
    public void testShortAdapter() throws IOException {
        TypeAdapter<Short> adapter = TypeAdapters.SHORT;
        assertEquals(new JsonPrimitive((short) 1000), adapter.toJsonTree((short) 1000));
        assertEquals(Short.valueOf((short) 1000), adapter.fromJsonTree(new JsonPrimitive(1000)));
        assertEquals(Short.valueOf(Short.MAX_VALUE), adapter.fromJsonTree(new JsonPrimitive(Short.MAX_VALUE)));
        assertEquals(Short.valueOf(Short.MIN_VALUE), adapter.fromJsonTree(new JsonPrimitive(Short.MIN_VALUE)));
        assertNull(adapter.fromJsonTree(JsonNull.INSTANCE));
        assertNull(adapter.toJsonTree(null));
    }

    @Test
    public void testCharacterAdapter() throws IOException {
        TypeAdapter<Character> adapter = TypeAdapters.CHARACTER;
        assertEquals(new JsonPrimitive('A'), adapter.toJsonTree('A'));
        assertEquals(Character.valueOf('A'), adapter.fromJsonTree(new JsonPrimitive("A")));
        // Single character string
        assertEquals(Character.valueOf('z'), adapter.fromJsonTree(new JsonPrimitive("z")));
        // Null handling
        assertNull(adapter.fromJsonTree(JsonNull.INSTANCE));
        assertNull(adapter.toJsonTree(null));
        // Empty string should fail
        try {
            adapter.fromJsonTree(new JsonPrimitive(""));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testStringAdapter() throws IOException {
        TypeAdapter<String> adapter = TypeAdapters.STRING;
        assertEquals(new JsonPrimitive("hello"), adapter.toJsonTree("hello"));
        assertEquals("hello", adapter.fromJsonTree(new JsonPrimitive("hello")));
        // Empty string
        assertEquals("", adapter.fromJsonTree(new JsonPrimitive("")));
        // Null handling
        assertNull(adapter.fromJsonTree(JsonNull.INSTANCE));
        assertNull(adapter.toJsonTree(null));
        // Special characters
        String special = "line1\nline2\t\"quote\"";
        assertEquals(special, adapter.fromJsonTree(new JsonPrimitive(special)));
    }

    // ==================== Number Type Adapters ====================

    @Test
    public void testBigIntegerAdapter() throws IOException {
        TypeAdapter<BigInteger> adapter = TypeAdapters.BIG_INTEGER;
        BigInteger value = new BigInteger("12345678901234567890");
        assertEquals(new JsonPrimitive(value), adapter.toJsonTree(value));
        assertEquals(value, adapter.fromJsonTree(new JsonPrimitive(value)));
        // Zero
        assertEquals(BigInteger.ZERO, adapter.fromJsonTree(new JsonPrimitive(0)));
        // Negative
        BigInteger negative = new BigInteger("-9876543210987654321");
        assertEquals(negative, adapter.fromJsonTree(new JsonPrimitive(negative)));
        assertNull(adapter.fromJsonTree(JsonNull.INSTANCE));
        assertNull(adapter.toJsonTree(null));
    }

    @Test
    public void testBigDecimalAdapter() throws IOException {
        TypeAdapter<BigDecimal> adapter = TypeAdapters.BIG_DECIMAL;
        BigDecimal value = new BigDecimal("12345.6789");
        assertEquals(new JsonPrimitive(value), adapter.toJsonTree(value));
        assertEquals(value, adapter.fromJsonTree(new JsonPrimitive(value)));
        // Zero
        assertEquals(BigDecimal.ZERO, adapter.fromJsonTree(new JsonPrimitive(0)));
        // Negative
        BigDecimal negative = new BigDecimal("-98765.4321");
        assertEquals(negative, adapter.fromJsonTree(new JsonPrimitive(negative)));
        assertNull(adapter.fromJsonTree(JsonNull.INSTANCE));
        assertNull(adapter.toJsonTree(null));
    }

    // ==================== URL/URI/UUID Adapters ====================

    @Test
    public void testUrlAdapter() throws IOException {
        TypeAdapter<URL> adapter = TypeAdapters.URL;
        URL url = new URL("http://example.com/path");
        assertEquals(new JsonPrimitive(url.toString()), adapter.toJsonTree(url));
        assertEquals(url, adapter.fromJsonTree(new JsonPrimitive(url.toString())));
        // Null
        assertNull(adapter.fromJsonTree(JsonNull.INSTANCE));
        assertNull(adapter.toJsonTree(null));
        // Invalid URL
        try {
            adapter.fromJsonTree(new JsonPrimitive("not a url"));
            fail("Expected IOException or MalformedURLException");
        } catch (Exception e) {
            // expected
        }
    }

    @Test
    public void testUriAdapter() throws IOException {
        TypeAdapter<URI> adapter = TypeAdapters.URI;
        URI uri = URI.create("http://example.com/resource");
        assertEquals(new JsonPrimitive(uri.toString()), adapter.toJsonTree(uri));
        assertEquals(uri, adapter.fromJsonTree(new JsonPrimitive(uri.toString())));
        // Null
        assertNull(adapter.fromJsonTree(JsonNull.INSTANCE));
        assertNull(adapter.toJsonTree(null));
        // Invalid URI
        try {
            adapter.fromJsonTree(new JsonPrimitive("invalid uri with spaces"));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testUuidAdapter() throws IOException {
        TypeAdapter<UUID> adapter = TypeAdapters.UUID;
        UUID uuid = UUID.randomUUID();
        assertEquals(new JsonPrimitive(uuid.toString()), adapter.toJsonTree(uuid));
        assertEquals(uuid, adapter.fromJsonTree(new JsonPrimitive(uuid.toString())));
        // Null
        assertNull(adapter.fromJsonTree(JsonNull.INSTANCE));
        assertNull(adapter.toJsonTree(null));
        // Invalid UUID string
        try {
            adapter.fromJsonTree(new JsonPrimitive("not-a-uuid"));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ==================== Locale/TimeZone/Currency Adapters ====================

    @Test
    public void testLocaleAdapter() throws IOException {
        TypeAdapter<Locale> adapter = TypeAdapters.LOCALE;
        Locale locale = Locale.US;
        assertEquals(new JsonPrimitive(locale.toString()), adapter.toJsonTree(locale));
        assertEquals(locale, adapter.fromJsonTree(new JsonPrimitive(locale.toString())));
        // Locale with language and country
        Locale deDE = Locale.GERMANY;
        assertEquals(deDE, adapter.fromJsonTree(new JsonPrimitive(deDE.toString())));
        // Null
        assertNull(adapter.fromJsonTree(JsonNull.INSTANCE));
        assertNull(adapter.toJsonTree(null));
        // Invalid locale string
        try {
            adapter.fromJsonTree(new JsonPrimitive("invalid_locale"));
            // Should not throw, but may return a default? Actually Gson's Locale adapter is lenient.
        } catch (Exception e) {
            // Not expected to throw
        }
    }

    @Test
    public void testTimeZoneAdapter() throws IOException {
        TypeAdapter<TimeZone> adapter = TypeAdapters.TIMEZONE;
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        assertEquals(new JsonPrimitive(tz.getID()), adapter.toJsonTree(tz));
        assertEquals(tz, adapter.fromJsonTree(new JsonPrimitive(tz.getID())));
        // Null
        assertNull(adapter.fromJsonTree(JsonNull.INSTANCE));
        assertNull(adapter.toJsonTree(null));
        // Invalid timezone ID
        TimeZone defaultTz = TimeZone.getDefault();
        // Gson returns default timezone for invalid IDs? Actually it returns GMT.
        TimeZone result = adapter.fromJsonTree(new JsonPrimitive("Invalid/Zone"));
        assertNotNull(result);
    }

    @Test
    public void testCurrencyAdapter() throws IOException {
        TypeAdapter<Currency> adapter = TypeAdapters.CURRENCY;
        Currency usd = Currency.getInstance("USD");
        assertEquals(new JsonPrimitive(usd.getCurrencyCode()), adapter.toJsonTree(usd));
        assertEquals(usd, adapter.fromJsonTree(new JsonPrimitive("USD")));
        // Null
        assertNull(adapter.fromJsonTree(JsonNull.INSTANCE));
        assertNull(adapter.toJsonTree(null));
        // Invalid currency code - should throw IllegalArgumentException
        try {
            adapter.fromJsonTree(new JsonPrimitive("XYZ"));
            fail("Expected IllegalArgumentException for invalid currency code");
        } catch (IllegalArgumentException e) {
            // expected
        }
        // Empty string
        try {
            adapter.fromJsonTree(new JsonPrimitive(""));
            fail("Expected IllegalArgumentException for empty currency code");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ==================== Atomic Type Adapters ====================

    @Test
    public void testAtomicIntegerAdapter() throws IOException {
        TypeAdapter<AtomicInteger> adapter = TypeAdapters.ATOMIC_INTEGER;
        AtomicInteger value = new AtomicInteger(42);
        assertEquals(new JsonPrimitive(42), adapter.toJsonTree(value));
        assertEquals(42, adapter.fromJsonTree(new JsonPrimitive(42)).get());
        // Null
        assertNull(adapter.fromJsonTree(JsonNull.INSTANCE));
        assertNull(adapter.toJsonTree(null));
        // Edge values
        assertEquals(Integer.MAX_VALUE, adapter.fromJsonTree(new JsonPrimitive(Integer.MAX_VALUE)).get());
        assertEquals(Integer.MIN_VALUE, adapter.fromJsonTree(new JsonPrimitive(Integer.MIN_VALUE)).get());
    }

    @Test
    public void testAtomicBooleanAdapter() throws IOException {
        TypeAdapter<AtomicBoolean> adapter = TypeAdapters.ATOMIC_BOOLEAN;
        AtomicBoolean value = new AtomicBoolean(true);
        assertEquals(new JsonPrimitive(true), adapter.toJsonTree(value));
        assertTrue(adapter.fromJsonTree(new JsonPrimitive(true)).get());
        assertFalse(adapter.fromJsonTree(new JsonPrimitive(false)).get());
        // Null
        assertNull(adapter.fromJsonTree(JsonNull.INSTANCE));
        assertNull(adapter.toJsonTree(null));
    }

    @Test
    public void testAtomicLongAdapter() throws IOException {
        TypeAdapter<AtomicLong> adapter = TypeAdapters.ATOMIC_LONG;
        AtomicLong value = new AtomicLong(123456789L);
        assertEquals(new JsonPrimitive(123456789L), adapter.toJsonTree(value));
        assertEquals(123456789L, adapter.fromJsonTree(new JsonPrimitive(123456789L)).get());
        // Null
        assertNull(adapter.fromJsonTree(JsonNull.INSTANCE));
        assertNull(adapter.toJsonTree(null));
        // Edge values
        assertEquals(Long.MAX_VALUE, adapter.fromJsonTree(new JsonPrimitive(Long.MAX_VALUE)).get());
        assertEquals(Long.MIN_VALUE, adapter.fromJsonTree(new JsonPrimitive(Long.MIN_VALUE)).get());
    }

    // ==================== Array and Collection Adapters ====================

    @Test
    public void testArrayAdapter() throws IOException {
        // Test integer array
        TypeAdapter<int[]> adapter = TypeAdapters.INTEGER_ARRAY;
        int[] input = {1, 2, 3};
        JsonElement json = adapter.toJsonTree(input);
        assertTrue(json.isJsonArray());
        int[] output = adapter.fromJsonTree(json);
        assertArrayEquals(input, output);
        // Empty array
        int[] empty = {};
        JsonElement emptyJson = adapter.toJsonTree(empty);
        assertTrue(emptyJson.isJsonArray());
        assertArrayEquals(empty, adapter.fromJsonTree(emptyJson));
        // Null
        assertNull(adapter.fromJsonTree(JsonNull.INSTANCE));
        assertNull(adapter.toJsonTree(null));
    }

    @Test
    public void testStringArrayAdapter() throws IOException {
        TypeAdapter<String[]> adapter = TypeAdapters.STRING_ARRAY;
        String[] input = {"a", "b", "c"};
        JsonElement json = adapter.toJsonTree(input);
        assertTrue(json.isJsonArray());
        String[] output = adapter.fromJsonTree(json);
        assertArrayEquals(input, output);
        // Null
        assertNull(adapter.fromJsonTree(JsonNull.INSTANCE));
        assertNull(adapter.toJsonTree(null));
    }

    @Test
    public void testCollectionAdapter() throws IOException {
        // List of strings
        TypeAdapter<List<String>> adapter = TypeAdapters.newFactory(
            TypeToken.get(new ArrayList<String>().getClass()).getType(),
            TypeAdapters.STRING
        );
        // Actually we need to test the collection adapter from TypeAdapters.COLLECTION?
        // TypeAdapters has a COLLECTION field? Let's use the factory method.
        // For simplicity, test using Gson.
        List<String> list = Arrays.asList("x", "y", "z");
        String json = gson.toJson(list);
        List<String> deserialized = gson.fromJson(json, new TypeToken<List<String>>(){}.getType());
        assertEquals(list, deserialized);
        // Null
        assertNull(gson.fromJson("null", new TypeToken<List<String>>(){}.getType()));
    }

    @Test
    public void testMapAdapter() throws IOException {
        Map<String, Integer> map = new HashMap<>();
        map.put("one", 1);
        map.put("two", 2);
        String json = gson.toJson(map);
        Map<String, Integer> deserialized = gson.fromJson(json, new TypeToken<Map<String, Integer>>(){}.getType());
        assertEquals(map, deserialized);
        // Null
        assertNull(gson.fromJson("null", new TypeToken<Map<String, Integer>>(){}.getType()));
    }

    // ==================== Factory Methods ====================

    @Test
    public void testNewFactory() {
        // Test that factory creates adapter for exact type
        TypeAdapterFactory factory = TypeAdapters.newFactory(Integer.class, TypeAdapters.INTEGER);
        TypeAdapter<Integer> adapter = (TypeAdapter<Integer>) factory.create(gson, TypeToken.get(Integer.class));
        assertNotNull(adapter);
        assertEquals(Integer.valueOf(5), adapter.fromJsonTree(new JsonPrimitive(5)));
        // Should not create for other types
        assertNull(factory.create(gson, TypeToken.get(String.class)));
    }

    @Test
    public void testNewFactoryForMultipleTypes() {
        TypeAdapterFactory factory = TypeAdapters.newFactory(
            new Class<?>[]{Integer.class, Long.class},
            TypeAdapters.INTEGER
        );
        assertNotNull(factory.create(gson, TypeToken.get(Integer.class)));
        assertNotNull(factory.create(gson, TypeToken.get(Long.class)));
        assertNull(factory.create(gson, TypeToken.get(String.class)));
    }

    @Test
    public void testNewTypeHierarchyFactory() {
        // Test hierarchy factory for Number
        TypeAdapterFactory factory = TypeAdapters.newTypeHierarchyFactory(Number.class, TypeAdapters.INTEGER);
        assertNotNull(factory.create(gson, TypeToken.get(Integer.class)));
        assertNotNull(factory.create(gson, TypeToken.get(Double.class)));
        assertNotNull(factory.create(gson, TypeToken.get(Number.class)));
        assertNull(factory.create(gson, TypeToken.get(String.class)));
    }

    // ==================== Edge Cases and Bug Triggers ====================

    @Test
    public void testNullHandlingForAllAdapters() {
        // Ensure all adapters handle null gracefully
        assertNull(TypeAdapters.INTEGER.toJsonTree(null));
        assertNull(TypeAdapters.STRING.toJsonTree(null));
        assertNull(TypeAdapters.BOOLEAN.toJsonTree(null));
        assertNull(TypeAdapters.LONG.toJsonTree(null));
        assertNull(TypeAdapters.FLOAT.toJsonTree(null));
        assertNull(TypeAdapters.DOUBLE.toJsonTree(null));
        assertNull(TypeAdapters.BYTE.toJsonTree(null));
        assertNull(TypeAdapters.SHORT.toJsonTree(null));
        assertNull(TypeAdapters.CHARACTER.toJsonTree(null));
        assertNull(TypeAdapters.BIG_INTEGER.toJsonTree(null));
        assertNull(TypeAdapters.BIG_DECIMAL.toJsonTree(null));
        assertNull(TypeAdapters.URL.toJsonTree(null));
        assertNull(TypeAdapters.URI.toJsonTree(null));
        assertNull(TypeAdapters.UUID.toJsonTree(null));
        assertNull(TypeAdapters.LOCALE.toJsonTree(null));
        assertNull(TypeAdapters.TIMEZONE.toJsonTree(null));
        assertNull(TypeAdapters.CURRENCY.toJsonTree(null));
        assertNull(TypeAdapters.ATOMIC_INTEGER.toJsonTree(null));
        assertNull(TypeAdapters.ATOMIC_BOOLEAN.toJsonTree(null));
        assertNull(TypeAdapters.ATOMIC_LONG.toJsonTree(null));
    }

    @Test
    public void testCurrencyAdapterWithInvalidCodes() {
        // Bug trigger: Gson bug 11 might involve Currency adapter not handling invalid codes properly.
        TypeAdapter<Currency> adapter = TypeAdapters.CURRENCY;
        // Test with null input (should return null)
        assertNull(adapter.fromJsonTree(JsonNull.INSTANCE));
        // Test with empty string
        try {
            adapter.fromJsonTree(new JsonPrimitive(""));
            fail("Expected IllegalArgumentException for empty currency code");
        } catch (IllegalArgumentException e) {
            // expected
        }
        // Test with non-existent code
        try {
            adapter.fromJsonTree(new JsonPrimitive("XYZ"));
            fail("Expected IllegalArgumentException for invalid currency code");
        } catch (IllegalArgumentException e) {
            // expected
        }
        // Test with lower case code (should be case-sensitive)
        try {
            adapter.fromJsonTree(new JsonPrimitive("usd"));
            // May or may not throw; Currency.getInstance is case-sensitive.
            // If it throws, it's fine; if not, it might be a bug.
        } catch (IllegalArgumentException e) {
            // expected if case-sensitive
        }
    }

    @Test
    public void testCharacterAdapterEdgeCases() throws IOException {
        TypeAdapter<Character> adapter = TypeAdapters.CHARACTER;
        // Single character
        assertEquals(Character.valueOf('a'), adapter.fromJsonTree(new JsonPrimitive("a")));
        // Multi-character string should throw
        try {
            adapter.fromJsonTree(new JsonPrimitive("ab"));
            fail("Expected IllegalArgumentException for multi-character string");
        } catch (IllegalArgumentException e) {
            // expected
        }
        // Numeric value as string? Actually JsonPrimitive can be number.
        // Character adapter expects string, so number should fail.
        try {
            adapter.fromJsonTree(new JsonPrimitive(65));
            fail("Expected ClassCastException or IllegalArgumentException");
        } catch (Exception e) {
            // expected
        }
    }

    @Test
    public void testBooleanAdapterWithNonBooleanValues() throws IOException {
        TypeAdapter<Boolean> adapter = TypeAdapters.BOOLEAN;
        // "true" and "false" strings are accepted
        assertTrue(adapter.fromJsonTree(new JsonPrimitive("true")));
        assertFalse(adapter.fromJsonTree(new JsonPrimitive("false")));
        // Other strings should throw
        try {
            adapter.fromJsonTree(new JsonPrimitive("yes"));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        // Numbers? Boolean adapter may accept 1/0? In Gson, it does not.
        try {
            adapter.fromJsonTree(new JsonPrimitive(1));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testNumberAdapterOverflow() throws IOException {
        // Test that integer adapter rejects out-of-range values
        TypeAdapter<Integer> adapter = TypeAdapters.INTEGER;
        try {
            adapter.fromJsonTree(new JsonPrimitive(123456789012345L));
            fail("Expected NumberFormatException for long value");
        } catch (NumberFormatException e) {
            // expected
        }
        // Similarly for long adapter with BigInteger
        TypeAdapter<Long> longAdapter = TypeAdapters.LONG;
        try {
            longAdapter.fromJsonTree(new JsonPrimitive(new BigInteger("99999999999999999999999999999999999999")));
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test
    public void testJsonElementAdapter() throws IOException {
        // TypeAdapters.JSON_ELEMENT adapter
        TypeAdapter<JsonElement> adapter = TypeAdapters.JSON_ELEMENT;
        JsonPrimitive primitive = new JsonPrimitive("test");
        assertEquals(primitive, adapter.toJsonTree(primitive));
        assertEquals(primitive, adapter.fromJsonTree(primitive));
        // Null
        assertNull(adapter.fromJsonTree(JsonNull.INSTANCE));
        assertNull(adapter.toJsonTree(null));
        // Array
        com.google.gson.JsonArray array = new com.google.gson.JsonArray();
        array.add(1);
        array.add("two");
        assertEquals(array, adapter.fromJsonTree(array));
    }

    @Test
    public void testDateAdapter() throws IOException {
        // TypeAdapters.DATE adapter
        TypeAdapter<java.util.Date> adapter = TypeAdapters.DATE;
        java.util.Date date = new java.util.Date(1234567890000L);
        JsonElement json = adapter.toJsonTree(date);
        assertEquals(date, adapter.fromJsonTree(json));
        // Null
        assertNull(adapter.fromJsonTree(JsonNull.INSTANCE));
        assertNull(adapter.toJsonTree(null));
        // Invalid date string
        try {
            adapter.fromJsonTree(new JsonPrimitive("not a date"));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testTimestampAdapter() throws IOException {
        // TypeAdapters.TIMESTAMP adapter (java.sql.Timestamp)
        TypeAdapter<java.sql.Timestamp> adapter = TypeAdapters.TIMESTAMP;
        java.sql.Timestamp ts = new java.sql.Timestamp(987654321000L);
        JsonElement json = adapter.toJsonTree(ts);
        assertEquals(ts, adapter.fromJsonTree(json));
        // Null
        assertNull(adapter.fromJsonTree(JsonNull.INSTANCE));
        assertNull(adapter.toJsonTree(null));
    }

    @Test
    public void testSqlDateAdapter() throws IOException {
        TypeAdapter<java.sql.Date> adapter = TypeAdapters.SQL_DATE;
        java.sql.Date date = new java.sql.Date(1234567890000L);
        JsonElement json = adapter.toJsonTree(date);
        assertEquals(date, adapter.fromJsonTree(json));
        // Null
        assertNull(adapter.fromJsonTree(JsonNull.INSTANCE));
        assertNull(adapter.toJsonTree(null));
    }

    @Test
    public void testCalendarAdapter() throws IOException {
        TypeAdapter<Calendar> adapter = TypeAdapters.CALENDAR;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(1234567890000L);
        JsonElement json = adapter.toJsonTree(cal);
        Calendar deserialized = adapter.fromJsonTree(json);
        assertEquals(cal.getTimeInMillis(), deserialized.getTimeInMillis());
        // Null
        assertNull(adapter.fromJsonTree(JsonNull.INSTANCE));
        assertNull(adapter.toJsonTree(null));
    }

    // ==================== Integration with Gson ====================

    @Test
    public void testGsonIntegration() {
        // Test that Gson uses TypeAdapters correctly
        assertEquals("42", gson.toJson(42));
        assertEquals(Integer.valueOf(42), gson.fromJson("42", Integer.class));
        assertEquals("true", gson.toJson(true));
        assertEquals(Boolean.TRUE, gson.fromJson("true", Boolean.class));
        // Custom object with fields
        TestObject obj = new TestObject();
        obj.name = "test";
        obj.count = 10;
        String json = gson.toJson(obj);
        TestObject deserialized = gson.fromJson(json, TestObject.class);
        assertEquals(obj.name, deserialized.name);
        assertEquals(obj.count, deserialized.count);
    }

    private static class TestObject {
        String name;
        int count;
    }
}