package com.example;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for StringArrayDeserializer.
 * Designed to achieve high code coverage and detect potential faults.
 */
public class StringArrayDeserializerTest {

    private StringArrayDeserializer deserializer;

    @Before
    public void setUp() {
        deserializer = new StringArrayDeserializer();
    }

    // --- Null and empty input ---
    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeNullInput() {
        deserializer.deserialize(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeEmptyString() {
        deserializer.deserialize("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeBlankString() {
        deserializer.deserialize("   ");
    }

    // --- Valid JSON array ---
    @Test
    public void testDeserializeSimpleArray() {
        String[] result = deserializer.deserialize("[\"a\",\"b\",\"c\"]");
        assertNotNull(result);
        assertEquals(3, result.length);
        assertArrayEquals(new String[]{"a", "b", "c"}, result);
    }

    @Test
    public void testDeserializeEmptyArray() {
        String[] result = deserializer.deserialize("[]");
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    public void testDeserializeSingleElement() {
        String[] result = deserializer.deserialize("[\"only\"]");
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("only", result[0]);
    }

    @Test
    public void testDeserializeArrayWithEscapedQuotes() {
        String[] result = deserializer.deserialize("[\"hello\\\"world\",\"test\"]");
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("hello\"world", result[0]);
        assertEquals("test", result[1]);
    }

    @Test
    public void testDeserializeArrayWithNumbersAsStrings() {
        String[] result = deserializer.deserialize("[\"123\",\"456\"]");
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("123", result[0]);
        assertEquals("456", result[1]);
    }

    // --- Malformed input ---
    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeMissingOpeningBracket() {
        deserializer.deserialize("\"a\",\"b\"]");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeMissingClosingBracket() {
        deserializer.deserialize("[\"a\",\"b\"");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeExtraComma() {
        deserializer.deserialize("[\"a\",,\"b\"]");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeUnquotedString() {
        deserializer.deserialize("[a,b]");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeNestedArray() {
        deserializer.deserialize("[[\"a\"]]");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeNonStringElement() {
        deserializer.deserialize("[\"a\",123]");
    }

    // --- Boundary and edge cases ---
    @Test
    public void testDeserializeArrayWithSpaces() {
        String[] result = deserializer.deserialize("  [ \"a\" , \"b\" ]  ");
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("a", result[0]);
        assertEquals("b", result[1]);
    }

    @Test
    public void testDeserializeArrayWithNewlines() {
        String[] result = deserializer.deserialize("[\n\"a\",\n\"b\"\n]");
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("a", result[0]);
        assertEquals("b", result[1]);
    }

    @Test
    public void testDeserializeLargeArray() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 1000; i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(i).append("\"");
        }
        sb.append("]");
        String[] result = deserializer.deserialize(sb.toString());
        assertNotNull(result);
        assertEquals(1000, result.length);
        assertEquals("0", result[0]);
        assertEquals("999", result[999]);
    }

    // --- Fault detection: potential null pointer or index out of bounds ---
    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeOnlyComma() {
        deserializer.deserialize("[,]");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeTrailingComma() {
        deserializer.deserialize("[\"a\",]");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeLeadingComma() {
        deserializer.deserialize("[,\"a\"]");
    }

    @Test
    public void testDeserializeEmptyStringElement() {
        String[] result = deserializer.deserialize("[\"\",\"b\"]");
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("", result[0]);
        assertEquals("b", result[1]);
    }

    @Test
    public void testDeserializeAllEmptyStrings() {
        String[] result = deserializer.deserialize("[\"\",\"\",\"\"]");
        assertNotNull(result);
        assertEquals(3, result.length);
        for (String s : result) {
            assertEquals("", s);
        }
    }

    // --- Additional edge: very long string element ---
    @Test
    public void testDeserializeLongStringElement() {
        StringBuilder longStr = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longStr.append("x");
        }
        String input = "[\"" + longStr.toString() + "\"]";
        String[] result = deserializer.deserialize(input);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(longStr.toString(), result[0]);
    }
}