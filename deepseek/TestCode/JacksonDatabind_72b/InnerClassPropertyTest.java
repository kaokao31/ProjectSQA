package com.fasterxml.jackson.databind.deser.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Unit tests for InnerClassProperty, targeting coverage and potential bugs
 * (specifically related to Defects4J bug 72: outer class reference not set).
 */
public class InnerClassPropertyTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        // Ensure that default typing is not enabled to avoid interference
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
    }

    // Outer class for testing non-static inner class deserialization
    static class OuterClass {
        public String outerField = "outer";

        // Non-static inner class with a property
        class InnerClass {
            @JsonProperty("value")
            public String value;

            // Method to access outer class instance
            public OuterClass getOuter() {
                return OuterClass.this;
            }
        }

        // Factory method to create inner class instance (for testing)
        public InnerClass createInner() {
            return new InnerClass();
        }
    }

    // Test basic deserialization of a non-static inner class
    @Test
    public void testDeserializeInnerClass() throws IOException {
        OuterClass outer = new OuterClass();
        // Use ObjectReader with a specific outer instance
        ObjectReader reader = mapper.readerFor(OuterClass.InnerClass.class)
                .with(outer);
        String json = "{\"value\":\"test\"}";
        OuterClass.InnerClass result = reader.readValue(json);
        assertNotNull("Deserialized inner class should not be null", result);
        assertEquals("Inner class value should match", "test", result.value);
        assertSame("Outer class reference should be set correctly", outer, result.getOuter());
    }

    // Test deserialization with null value for property
    @Test
    public void testDeserializeInnerClassWithNullValue() throws IOException {
        OuterClass outer = new OuterClass();
        ObjectReader reader = mapper.readerFor(OuterClass.InnerClass.class)
                .with(outer);
        String json = "{\"value\":null}";
        OuterClass.InnerClass result = reader.readValue(json);
        assertNotNull(result);
        assertNull("Value should be null", result.value);
        assertSame(outer, result.getOuter());
    }

    // Test deserialization with missing property
    @Test
    public void testDeserializeInnerClassMissingProperty() throws IOException {
        OuterClass outer = new OuterClass();
        ObjectReader reader = mapper.readerFor(OuterClass.InnerClass.class)
                .with(outer);
        String json = "{}";
        OuterClass.InnerClass result = reader.readValue(json);
        assertNotNull(result);
        assertNull("Value should be null when missing", result.value);
        assertSame(outer, result.getOuter());
    }

    // Test deserialization with empty JSON object
    @Test
    public void testDeserializeInnerClassEmptyObject() throws IOException {
        OuterClass outer = new OuterClass();
        ObjectReader reader = mapper.readerFor(OuterClass.InnerClass.class)
                .with(outer);
        String json = "{}";
        OuterClass.InnerClass result = reader.readValue(json);
        assertNotNull(result);
        assertNull(result.value);
        assertSame(outer, result.getOuter());
    }

    // Test deserialization with extra unknown properties (should be ignored)
    @Test
    public void testDeserializeInnerClassWithUnknownProperties() throws IOException {
        OuterClass outer = new OuterClass();
        ObjectReader reader = mapper.readerFor(OuterClass.InnerClass.class)
                .with(outer);
        String json = "{\"value\":\"x\",\"unknown\":\"y\"}";
        OuterClass.InnerClass result = reader.readValue(json);
        assertNotNull(result);
        assertEquals("x", result.value);
        assertSame(outer, result.getOuter());
    }

    // Test that deserialization fails when no outer instance is provided (bug scenario)
    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeInnerClassWithoutOuterInstance() throws IOException {
        // Attempt to read inner class without providing outer instance
        // This should throw an exception because InnerClassProperty requires outer instance
        ObjectReader reader = mapper.readerFor(OuterClass.InnerClass.class);
        String json = "{\"value\":\"test\"}";
        reader.readValue(json);
    }

    // Test deserialization with multiple inner class instances (ensuring outer reference is correct)
    @Test
    public void testDeserializeMultipleInnerClassInstances() throws IOException {
        OuterClass outer1 = new OuterClass();
        OuterClass outer2 = new OuterClass();
        outer1.outerField = "outer1";
        outer2.outerField = "outer2";

        ObjectReader reader1 = mapper.readerFor(OuterClass.InnerClass.class).with(outer1);
        ObjectReader reader2 = mapper.readerFor(OuterClass.InnerClass.class).with(outer2);

        String json = "{\"value\":\"a\"}";
        OuterClass.InnerClass result1 = reader1.readValue(json);
        OuterClass.InnerClass result2 = reader2.readValue(json);

        assertSame("Outer1 should be referenced", outer1, result1.getOuter());
        assertSame("Outer2 should be referenced", outer2, result2.getOuter());
        assertEquals("a", result1.value);
        assertEquals("a", result2.value);
    }

    // Test deserialization with special characters in value
    @Test
    public void testDeserializeInnerClassWithSpecialChars() throws IOException {
        OuterClass outer = new OuterClass();
        ObjectReader reader = mapper.readerFor(OuterClass.InnerClass.class).with(outer);
        String json = "{\"value\":\"hello\\nworld\"}";
        OuterClass.InnerClass result = reader.readValue(json);
        assertNotNull(result);
        assertEquals("hello\nworld", result.value);
        assertSame(outer, result.getOuter());
    }

    // Test that InnerClassProperty handles null token gracefully (if applicable)
    @Test
    public void testDeserializeInnerClassWithNullToken() throws IOException {
        OuterClass outer = new OuterClass();
        ObjectReader reader = mapper.readerFor(OuterClass.InnerClass.class).with(outer);
        // JSON null literal should result in null object
        String json = "null";
        OuterClass.InnerClass result = reader.readValue(json);
        assertNull("Deserializing null should return null", result);
    }

    // Test that InnerClassProperty handles empty string (should fail)
    @Test(expected = MismatchedInputException.class)
    public void testDeserializeInnerClassWithEmptyString() throws IOException {
        OuterClass outer = new OuterClass();
        ObjectReader reader = mapper.readerFor(OuterClass.InnerClass.class).with(outer);
        String json = "";
        reader.readValue(json);
    }

    // Test that InnerClassProperty handles array (should fail)
    @Test(expected = MismatchedInputException.class)
    public void testDeserializeInnerClassWithArray() throws IOException {
        OuterClass outer = new OuterClass();
        ObjectReader reader = mapper.readerFor(OuterClass.InnerClass.class).with(outer);
        String json = "[]";
        reader.readValue(json);
    }

    // Test that InnerClassProperty handles boolean (should fail)
    @Test(expected = MismatchedInputException.class)
    public void testDeserializeInnerClassWithBoolean() throws IOException {
        OuterClass outer = new OuterClass();
        ObjectReader reader = mapper.readerFor(OuterClass.InnerClass.class).with(outer);
        String json = "true";
        reader.readValue(json);
    }

    // Test that InnerClassProperty handles number (should fail)
    @Test(expected = MismatchedInputException.class)
    public void testDeserializeInnerClassWithNumber() throws IOException {
        OuterClass outer = new OuterClass();
        ObjectReader reader = mapper.readerFor(OuterClass.InnerClass.class).with(outer);
        String json = "42";
        reader.readValue(json);
    }
}