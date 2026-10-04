package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for NullifyingDeserializer.
 * Designed to achieve high code coverage and detect potential faults,
 * including the known bug (JacksonDatabind #39) where deserialize with
 * a null token could cause a NullPointerException.
 */
public class NullifyingDeserializerTest {

    private NullifyingDeserializer deserializer;
    private JsonFactory jsonFactory;
    private ObjectMapper mapper;

    @Before
    public void setUp() {
        deserializer = new NullifyingDeserializer();
        jsonFactory = new JsonFactory();
        mapper = new ObjectMapper();
    }

    // ============================================================
    // Tests for deserialize(JsonParser, DeserializationContext)
    // ============================================================

    @Test
    public void testDeserializeWithNullToken() throws IOException {
        // Parser for "null" literal -> token is VALUE_NULL
        JsonParser parser = jsonFactory.createParser("null");
        parser.nextToken(); // advance to VALUE_NULL
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deserializer.deserialize(parser, ctxt);
        assertNull("Deserializing a null token should return null", result);
    }

    @Test
    public void testDeserializeWithNonNullToken() throws IOException {
        // Parser for a number -> token is VALUE_NUMBER_INT
        JsonParser parser = jsonFactory.createParser("123");
        parser.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deserializer.deserialize(parser, ctxt);
        assertNull("Deserializing any token should return null", result);
    }

    @Test(expected = NullPointerException.class)
    public void testDeserializeWithNullParser() throws IOException {
        // Passing null parser should throw NullPointerException
        DeserializationContext ctxt = mapper.getDeserializationContext();
        deserializer.deserialize(null, ctxt);
    }

    @Test(expected = NullPointerException.class)
    public void testDeserializeWithNullContext() throws IOException {
        // Passing null context should throw NullPointerException
        JsonParser parser = jsonFactory.createParser("null");
        parser.nextToken();
        deserializer.deserialize(parser, null);
    }

    // ============================================================
    // Tests for getNullValue()
    // ============================================================

    @Test
    public void testGetNullValue() {
        assertNull("getNullValue should return null", deserializer.getNullValue());
    }

    // ============================================================
    // Tests for getEmptyValue()
    // ============================================================

    @Test
    public void testGetEmptyValue() {
        assertNull("getEmptyValue should return null", deserializer.getEmptyValue());
    }

    // ============================================================
    // Tests for handledType()
    // ============================================================

    @Test
    public void testHandledType() {
        assertEquals("handledType should be Object.class",
                Object.class, deserializer.handledType());
    }

    // ============================================================
    // Additional edge-case: deserialize with empty input
    // ============================================================

    @Test(expected = IOException.class)
    public void testDeserializeWithEmptyInput() throws IOException {
        // Empty input will cause parser to throw on nextToken
        JsonParser parser = jsonFactory.createParser("");
        // nextToken will throw EOF exception
        DeserializationContext ctxt = mapper.getDeserializationContext();
        deserializer.deserialize(parser, ctxt);
    }

    // ============================================================
    // Test that deserialize does not advance the parser beyond
    // the current token (should skip the value)
    // ============================================================

    @Test
    public void testDeserializeDoesNotAdvanceParser() throws IOException {
        JsonParser parser = jsonFactory.createParser("null");
        parser.nextToken(); // VALUE_NULL
        JsonToken before = parser.currentToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        deserializer.deserialize(parser, ctxt);
        JsonToken after = parser.currentToken();
        assertEquals("Parser should not advance after deserialize",
                before, after);
    }
}