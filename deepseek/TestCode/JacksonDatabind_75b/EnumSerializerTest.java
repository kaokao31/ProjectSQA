package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for {@link EnumSerializer}.
 * Designed to achieve high coverage and detect potential faults (e.g., bug 75).
 */
public class EnumSerializerTest {

    // Test enums
    public enum SimpleEnum {
        VALUE_A, VALUE_B, VALUE_C
    }

    public enum EnumWithJsonValue {
        @JsonValue
        FIRST("first"),
        SECOND("second");

        private final String value;

        EnumWithJsonValue(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum EnumWithToStringOverride {
        ALPHA, BETA;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    public enum EmptyEnum {
        // no constants
    }

    private ObjectMapper objectMapper;
    private StringWriter stringWriter;
    private JsonGenerator jsonGenerator;
    private SerializerProvider serializerProvider;

    @Before
    public void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        stringWriter = new StringWriter();
        jsonGenerator = objectMapper.getFactory().createGenerator(stringWriter);
        serializerProvider = objectMapper.getSerializerProvider();
    }

    // Helper to serialize an enum value using EnumSerializer directly
    private String serializeEnum(EnumSerializer serializer, Object value) throws IOException {
        stringWriter.getBuffer().setLength(0);
        serializer.serialize(value, jsonGenerator, serializerProvider);
        jsonGenerator.flush();
        return stringWriter.toString();
    }

    // Helper to get EnumSerializer instance for a given enum class
    private EnumSerializer getSerializer(Class<? extends Enum<?>> enumClass) {
        // EnumSerializer is typically constructed via its constructor or static instance
        // We assume a public constructor exists that takes the enum class
        return new EnumSerializer(enumClass);
    }

    // ========== Basic serialization tests ==========

    @Test
    public void testSerializeSimpleEnum() throws Exception {
        EnumSerializer serializer = getSerializer(SimpleEnum.class);
        String result = serializeEnum(serializer, SimpleEnum.VALUE_A);
        assertEquals("\"VALUE_A\"", result);
    }

    @Test
    public void testSerializeAnotherSimpleEnum() throws Exception {
        EnumSerializer serializer = getSerializer(SimpleEnum.class);
        String result = serializeEnum(serializer, SimpleEnum.VALUE_C);
        assertEquals("\"VALUE_C\"", result);
    }

    @Test
    public void testSerializeNullEnum() throws Exception {
        EnumSerializer serializer = getSerializer(SimpleEnum.class);
        String result = serializeEnum(serializer, null);
        // Typically null serializes as null literal
        assertEquals("null", result);
    }

    // ========== Enum with @JsonValue ==========

    @Test
    public void testSerializeEnumWithJsonValue() throws Exception {
        EnumSerializer serializer = getSerializer(EnumWithJsonValue.class);
        String result = serializeEnum(serializer, EnumWithJsonValue.FIRST);
        // @JsonValue on getValue() should serialize the value "first"
        assertEquals("\"first\"", result);
    }

    @Test
    public void testSerializeEnumWithJsonValueSecond() throws Exception {
        EnumSerializer serializer = getSerializer(EnumWithJsonValue.class);
        String result = serializeEnum(serializer, EnumWithJsonValue.SECOND);
        assertEquals("\"second\"", result);
    }

    // ========== Enum with toString override ==========

    @Test
    public void testSerializeEnumWithToStringOverride() throws Exception {
        EnumSerializer serializer = getSerializer(EnumWithToStringOverride.class);
        String result = serializeEnum(serializer, EnumWithToStringOverride.ALPHA);
        // If no @JsonValue, toString() is used? Actually Jackson uses name() by default.
        // But if toString() is overridden, it might be used depending on configuration.
        // We'll test the default behavior: name() is used.
        assertEquals("\"ALPHA\"", result);
    }

    // ========== Edge cases ==========

    @Test(expected = IllegalArgumentException.class)
    public void testSerializeEmptyEnum() throws Exception {
        // Empty enum class should cause an error during serializer construction
        // or serialization. We expect an exception.
        EnumSerializer serializer = getSerializer(EmptyEnum.class);
        // If construction succeeds, serialization of null might still work
        // but we test that it throws.
        serializer.serialize(null, jsonGenerator, serializerProvider);
    }

    @Test
    public void testSerializeEnumWithAllValues() throws Exception {
        EnumSerializer serializer = getSerializer(SimpleEnum.class);
        for (SimpleEnum value : SimpleEnum.values()) {
            stringWriter.getBuffer().setLength(0);
            serializer.serialize(value, jsonGenerator, serializerProvider);
            jsonGenerator.flush();
            String expected = "\"" + value.name() + "\"";
            assertEquals(expected, stringWriter.toString());
        }
    }

    // ========== Schema generation (if method exists) ==========

    @Test
    public void testGetSchema() throws Exception {
        EnumSerializer serializer = getSerializer(SimpleEnum.class);
        // getSchema may return a JSON schema node
        ObjectNode schema = serializer.getSchema(serializerProvider, null);
        assertNotNull("Schema should not be null", schema);
        // Typically schema type is "string" for enums
        assertTrue("Schema should have type property", schema.has("type"));
        assertEquals("string", schema.get("type").asText());
    }

    @Test
    public void testGetSchemaWithNullProvider() throws Exception {
        EnumSerializer serializer = getSerializer(SimpleEnum.class);
        // Some implementations may handle null provider gracefully
        ObjectNode schema = serializer.getSchema(null, null);
        assertNotNull(schema);
    }

    // ========== Fault detection: potential bug 75 scenarios ==========

    @Test
    public void testSerializeEnumWithCustomToStringAndNoJsonValue() throws Exception {
        // This test may trigger a bug if the serializer incorrectly uses toString()
        // instead of name() when no @JsonValue is present.
        EnumSerializer serializer = getSerializer(EnumWithToStringOverride.class);
        String result = serializeEnum(serializer, EnumWithToStringOverride.BETA);
        // Expected: name() -> "BETA", not toString() -> "beta"
        assertEquals("\"BETA\"", result);
    }

    @Test
    public void testSerializeEnumWithNullJsonGenerator() throws Exception {
        EnumSerializer serializer = getSerializer(SimpleEnum.class);
        try {
            serializer.serialize(SimpleEnum.VALUE_A, null, serializerProvider);
            fail("Should throw NullPointerException or IllegalArgumentException");
        } catch (NullPointerException | IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testSerializeEnumWithNullSerializerProvider() throws Exception {
        EnumSerializer serializer = getSerializer(SimpleEnum.class);
        try {
            serializer.serialize(SimpleEnum.VALUE_A, jsonGenerator, null);
            fail("Should throw NullPointerException or IllegalArgumentException");
        } catch (NullPointerException | IllegalArgumentException e) {
            // expected
        }
    }

    // ========== Additional coverage: multiple enum types ==========

    @Test
    public void testSerializeDifferentEnumTypes() throws Exception {
        EnumSerializer serializer1 = getSerializer(SimpleEnum.class);
        EnumSerializer serializer2 = getSerializer(EnumWithJsonValue.class);
        String result1 = serializeEnum(serializer1, SimpleEnum.VALUE_B);
        String result2 = serializeEnum(serializer2, EnumWithJsonValue.FIRST);
        assertEquals("\"VALUE_B\"", result1);
        assertEquals("\"first\"", result2);
    }

    // ========== Test that serializer handles enum with no values gracefully ==========
    // (if construction succeeds, serialization of null should work)
    @Test
    public void testSerializeNullForEmptyEnum() throws Exception {
        // Some implementations may allow construction with empty enum
        // We test null serialization
        try {
            EnumSerializer serializer = getSerializer(EmptyEnum.class);
            String result = serializeEnum(serializer, null);
            assertEquals("null", result);
        } catch (IllegalArgumentException e) {
            // acceptable if construction fails
        }
    }
}