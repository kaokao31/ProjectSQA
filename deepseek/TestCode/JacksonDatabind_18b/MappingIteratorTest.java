package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for MappingIterator.
 * Designed to achieve high line/branch coverage and detect potential faults.
 */
public class MappingIteratorTest {

    private ObjectMapper mapper;
    private JsonFactory factory;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        factory = new JsonFactory();
    }

    @After
    public void tearDown() {
        // No global cleanup needed per test
    }

    // ---------- Helper methods ----------

    private MappingIterator<Integer> createIntIterator(String json) throws IOException {
        JsonParser parser = factory.createParser(json);
        // Advance to first token (usually START_ARRAY)
        parser.nextToken();
        return new MappingIterator<>(Integer.class, parser, TypeFactory.defaultInstance().constructType(Integer.class));
    }

    private MappingIterator<String> createStringIterator(String json) throws IOException {
        JsonParser parser = factory.createParser(json);
        parser.nextToken();
        return new MappingIterator<>(String.class, parser, TypeFactory.defaultInstance().constructType(String.class));
    }

    // ---------- Basic iteration tests ----------

    @Test
    public void testEmptyArray() throws IOException {
        MappingIterator<Integer> it = createIntIterator("[]");
        assertFalse("Empty array should have no elements", it.hasNext());
        assertFalse("Second call to hasNext should still be false", it.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void testNextOnEmptyArray() throws IOException {
        MappingIterator<Integer> it = createIntIterator("[]");
        it.next(); // Should throw NoSuchElementException
    }

    @Test
    public void testSingleElement() throws IOException {
        MappingIterator<Integer> it = createIntIterator("[42]");
        assertTrue("Should have one element", it.hasNext());
        assertEquals(Integer.valueOf(42), it.next());
        assertFalse("No more elements", it.hasNext());
    }

    @Test
    public void testMultipleElements() throws IOException {
        MappingIterator<Integer> it = createIntIterator("[1, 2, 3]");
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(3), it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testNullElement() throws IOException {
        MappingIterator<Integer> it = createIntIterator("[null]");
        assertTrue(it.hasNext());
        assertNull("Null element should be returned as null", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testStringElements() throws IOException {
        MappingIterator<String> it = createStringIterator("[\"a\", \"b\", \"c\"]");
        assertTrue(it.hasNext());
        assertEquals("a", it.next());
        assertEquals("b", it.next());
        assertEquals("c", it.next());
        assertFalse(it.hasNext());
    }

    // ---------- Nested structures ----------

    @Test
    public void testNestedArray() throws IOException {
        // Each element is a list; MappingIterator returns List objects
        MappingIterator<Object> it = new MappingIterator<>(Object.class,
                factory.createParser("[[1,2], [3,4]]").nextToken(),
                TypeFactory.defaultInstance().constructType(Object.class));
        assertTrue(it.hasNext());
        Object first = it.next();
        assertTrue(first instanceof java.util.List);
        assertEquals(2, ((java.util.List<?>) first).size());
        assertTrue(it.hasNext());
        Object second = it.next();
        assertTrue(second instanceof java.util.List);
        assertEquals(2, ((java.util.List<?>) second).size());
        assertFalse(it.hasNext());
    }

    @Test
    public void testEmptyNestedArray() throws IOException {
        MappingIterator<Object> it = new MappingIterator<>(Object.class,
                factory.createParser("[[]]").nextToken(),
                TypeFactory.defaultInstance().constructType(Object.class));
        assertTrue(it.hasNext());
        Object inner = it.next();
        assertTrue(inner instanceof java.util.List);
        assertTrue(((java.util.List<?>) inner).isEmpty());
        assertFalse(it.hasNext());
    }

    // ---------- Edge cases with tokens ----------

    @Test
    public void testIteratorAfterEndOfStream() throws IOException {
        MappingIterator<Integer> it = createIntIterator("[1]");
        it.next();
        // After consuming all, hasNext should be false
        assertFalse(it.hasNext());
        // Calling next again should throw
        try {
            it.next();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    @Test
    public void testHasNextDoesNotAdvance() throws IOException {
        MappingIterator<Integer> it = createIntIterator("[10, 20]");
        assertTrue(it.hasNext());
        assertTrue(it.hasNext()); // Should still be true
        assertEquals(Integer.valueOf(10), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(20), it.next());
        assertFalse(it.hasNext());
    }

    // ---------- Close and resource management ----------

    @Test
    public void testCloseReleasesParser() throws IOException {
        JsonParser parser = factory.createParser("[1, 2, 3]");
        parser.nextToken();
        MappingIterator<Integer> it = new MappingIterator<>(Integer.class, parser,
                TypeFactory.defaultInstance().constructType(Integer.class));
        it.close();
        // After close, parser should be closed
        assertTrue("Parser should be closed", parser.isClosed());
    }

    @Test
    public void testHasNextAfterClose() throws IOException {
        MappingIterator<Integer> it = createIntIterator("[1]");
        it.close();
        // hasNext should return false (or throw? Typically returns false)
        assertFalse("hasNext after close should return false", it.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void testNextAfterClose() throws IOException {
        MappingIterator<Integer> it = createIntIterator("[1]");
        it.close();
        it.next(); // Should throw
    }

    @Test
    public void testCloseMultipleTimes() throws IOException {
        MappingIterator<Integer> it = createIntIterator("[1]");
        it.close();
        it.close(); // Should not throw
    }

    // ---------- IOException handling ----------

    @Test(expected = IOException.class)
    public void testIOExceptionOnMalformedJson() throws IOException {
        // Malformed JSON: missing closing bracket
        JsonParser parser = factory.createParser("[1, 2");
        parser.nextToken();
        MappingIterator<Integer> it = new MappingIterator<>(Integer.class, parser,
                TypeFactory.defaultInstance().constructType(Integer.class));
        // Attempt to iterate should throw IOException
        it.hasNext();
    }

    @Test(expected = IOException.class)
    public void testIOExceptionOnNextWithMalformed() throws IOException {
        JsonParser parser = factory.createParser("[1, ");
        parser.nextToken();
        MappingIterator<Integer> it = new MappingIterator<>(Integer.class, parser,
                TypeFactory.defaultInstance().constructType(Integer.class));
        it.next(); // Should throw IOException
    }

    // ---------- Type mismatch ----------

    @Test(expected = com.fasterxml.jackson.databind.exc.MismatchedInputException.class)
    public void testTypeMismatch() throws IOException {
        // Expecting Integer but get string
        MappingIterator<Integer> it = createIntIterator("[\"not an int\"]");
        it.next(); // Should throw MismatchedInputException
    }

    // ---------- Empty input (no tokens) ----------

    @Test(expected = IOException.class)
    public void testEmptyInput() throws IOException {
        JsonParser parser = factory.createParser("");
        // No tokens at all; nextToken() will return null
        parser.nextToken();
        MappingIterator<Integer> it = new MappingIterator<>(Integer.class, parser,
                TypeFactory.defaultInstance().constructType(Integer.class));
        it.hasNext(); // Should throw IOException because parser is at END
    }

    // ---------- Object iteration (non-array) ----------

    @Test
    public void testObjectIteration() throws IOException {
        // MappingIterator can also iterate over object fields if configured
        // Here we test with a simple object: {"a":1, "b":2}
        // The iterator will yield field values (1, 2)
        JsonParser parser = factory.createParser("{\"a\":1, \"b\":2}");
        parser.nextToken(); // START_OBJECT
        MappingIterator<Integer> it = new MappingIterator<>(Integer.class, parser,
                TypeFactory.defaultInstance().constructType(Integer.class));
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testEmptyObject() throws IOException {
        JsonParser parser = factory.createParser("{}");
        parser.nextToken(); // START_OBJECT
        MappingIterator<Integer> it = new MappingIterator<>(Integer.class, parser,
                TypeFactory.defaultInstance().constructType(Integer.class));
        assertFalse("Empty object should have no values", it.hasNext());
    }

    // ---------- Bug-specific tests (Defects4J #18) ----------
    // Bug: MappingIterator may throw exception on empty array when using certain configurations
    // Test that empty array is handled gracefully

    @Test
    public void testEmptyArrayWithObjectMapper() throws IOException {
        // Using ObjectMapper.readValues to create iterator
        MappingIterator<Integer> it = mapper.readValues(
                factory.createParser("[]"), Integer.class);
        assertFalse("Empty array should be handled without exception", it.hasNext());
    }

    @Test
    public void testEmptyArrayNextThrowsNoSuchElement() throws IOException {
        MappingIterator<Integer> it = mapper.readValues(
                factory.createParser("[]"), Integer.class);
        try {
            it.next();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    // ---------- Large number of elements ----------

    @Test
    public void testManyElements() throws IOException {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 1000; i++) {
            if (i > 0) sb.append(",");
            sb.append(i);
        }
        sb.append("]");
        MappingIterator<Integer> it = createIntIterator(sb.toString());
        int count = 0;
        while (it.hasNext()) {
            assertEquals(Integer.valueOf(count), it.next());
            count++;
        }
        assertEquals(1000, count);
    }

    // ---------- Concurrent modification? Not applicable ----------

    // ---------- Test that close does not affect other iterators ----------

    @Test
    public void testCloseDoesNotAffectOtherParsers() throws IOException {
        JsonParser parser1 = factory.createParser("[1]");
        parser1.nextToken();
        MappingIterator<Integer> it1 = new MappingIterator<>(Integer.class, parser1,
                TypeFactory.defaultInstance().constructType(Integer.class));

        JsonParser parser2 = factory.createParser("[2]");
        parser2.nextToken();
        MappingIterator<Integer> it2 = new MappingIterator<>(Integer.class, parser2,
                TypeFactory.defaultInstance().constructType(Integer.class));

        it1.close();
        // it2 should still work
        assertTrue(it2.hasNext());
        assertEquals(Integer.valueOf(2), it2.next());
        it2.close();
    }

    // ---------- Test that hasNext can be called multiple times without side effects ----------

    @Test
    public void testHasNextIdempotent() throws IOException {
        MappingIterator<Integer> it = createIntIterator("[1, 2]");
        assertTrue(it.hasNext());
        assertTrue(it.hasNext());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertFalse(it.hasNext());
        assertFalse(it.hasNext());
    }

    // ---------- Test with null token (should not happen normally) ----------

    @Test(expected = NullPointerException.class)
    public void testNullParser() throws IOException {
        new MappingIterator<>(Integer.class, null,
                TypeFactory.defaultInstance().constructType(Integer.class));
    }

    // ---------- Test that iterator works with JsonParser that is already advanced ----------

    @Test
    public void testParserAlreadyAtFirstElement() throws IOException {
        JsonParser parser = factory.createParser("[10, 20]");
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // VALUE_NUMBER_INT 10
        // Now parser is at first value, not at START_ARRAY
        MappingIterator<Integer> it = new MappingIterator<>(Integer.class, parser,
                TypeFactory.defaultInstance().constructType(Integer.class));
        // The iterator should treat current token as first element
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(10), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(20), it.next());
        assertFalse(it.hasNext());
    }

    // ---------- Test that iterator handles trailing data after array ----------

    @Test
    public void testTrailingDataAfterArray() throws IOException {
        // JSON with extra data after array: [1] 2
        JsonParser parser = factory.createParser("[1] 2");
        parser.nextToken(); // START_ARRAY
        MappingIterator<Integer> it = new MappingIterator<>(Integer.class, parser,
                TypeFactory.defaultInstance().constructType(Integer.class));
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertFalse(it.hasNext()); // Should stop after array end
    }

    // ---------- Test that iterator does not close parser automatically ----------

    @Test
    public void testParserNotClosedAfterIteration() throws IOException {
        JsonParser parser = factory.createParser("[1, 2]");
        parser.nextToken();
        MappingIterator<Integer> it = new MappingIterator<>(Integer.class, parser,
                TypeFactory.defaultInstance().constructType(Integer.class));
        while (it.hasNext()) {
            it.next();
        }
        assertFalse("Parser should not be closed automatically", parser.isClosed());
        it.close(); // Explicit close
        assertTrue(parser.isClosed());
    }
}