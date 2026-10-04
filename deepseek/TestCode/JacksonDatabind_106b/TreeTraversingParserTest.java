package com.fasterxml.jackson.databind.node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Comprehensive test suite for TreeTraversingParser.
 * Aims to achieve high code coverage and trigger potential bugs
 * (e.g., Defects4J bug 106 in JacksonDatabind).
 */
public class TreeTraversingParserTest {

    private ObjectMapper mapper;
    private JsonNodeFactory factory;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        factory = JsonNodeFactory.instance;
    }

    // ----------------------------------------------------------
    // Test constructing with null node
    // ----------------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
    public void testNullNodeConstruction() {
        new TreeTraversingParser(null, mapper.getDeserializationConfig());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullCodecConstruction() {
        new TreeTraversingParser(factory.nullNode(), null);
    }

    // ----------------------------------------------------------
    // Test traversing a NullNode
    // ----------------------------------------------------------
    @Test
    public void testNullNodeTraversal() throws Exception {
        JsonNode nullNode = factory.nullNode();
        try (TreeTraversingParser parser = new TreeTraversingParser(nullNode, mapper.getDeserializationConfig())) {
            assertNull(parser.getCurrentToken());
            assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
            assertEquals(JsonToken.VALUE_NULL, parser.getCurrentToken());
            assertNull(parser.getCurrentName());
            assertNull(parser.getText());
            assertNull(parser.nextToken()); // end of stream
            assertNull(parser.getCurrentToken());
            // check close
            parser.close();
            assertTrue(parser.isClosed());
        }
    }

    // ----------------------------------------------------------
    // Test traversing an empty ObjectNode
    // ----------------------------------------------------------
    @Test
    public void testEmptyObjectNode() throws Exception {
        ObjectNode root = factory.objectNode();
        try (TreeTraversingParser parser = new TreeTraversingParser(root, mapper.getDeserializationConfig())) {
            assertEquals(JsonToken.START_OBJECT, parser.nextToken());
            assertEquals(JsonToken.START_OBJECT, parser.getCurrentToken());
            assertNull(parser.getCurrentName()); // root object has no name
            assertEquals(JsonToken.END_OBJECT, parser.nextToken());
            assertEquals(JsonToken.END_OBJECT, parser.getCurrentToken());
            assertNull(parser.nextToken()); // end
        }
    }

    // ----------------------------------------------------------
    // Test traversing an empty ArrayNode
    // ----------------------------------------------------------
    @Test
    public void testEmptyArrayNode() throws Exception {
        ArrayNode root = factory.arrayNode();
        try (TreeTraversingParser parser = new TreeTraversingParser(root, mapper.getDeserializationConfig())) {
            assertEquals(JsonToken.START_ARRAY, parser.nextToken());
            assertEquals(JsonToken.END_ARRAY, parser.nextToken());
            assertNull(parser.nextToken());
        }
    }

    // ----------------------------------------------------------
    // Test traversing a simple object with fields
    // ----------------------------------------------------------
    @Test
    public void testSimpleObject() throws Exception {
        ObjectNode root = factory.objectNode();
        root.put("string", "value");
        root.put("int", 42);
        root.put("boolean", true);
        root.put("null", (String) null);
        root.set("obj", factory.objectNode());
        root.set("arr", factory.arrayNode().add(1).add(2));

        try (TreeTraversingParser parser = new TreeTraversingParser(root, mapper.getDeserializationConfig())) {
            // START_OBJECT
            assertEquals(JsonToken.START_OBJECT, parser.nextToken());
            assertNull(parser.getCurrentName());

            // field "string"
            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("string", parser.getCurrentName());
            assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
            assertEquals("value", parser.getText());

            // field "int"
            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("int", parser.getCurrentName());
            assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
            assertEquals(42, parser.getIntValue());

            // field "boolean"
            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("boolean", parser.getCurrentName());
            assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());

            // field "null"
            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("null", parser.getCurrentName());
            assertEquals(JsonToken.VALUE_NULL, parser.nextToken());

            // field "obj"
            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("obj", parser.getCurrentName());
            assertEquals(JsonToken.START_OBJECT, parser.nextToken());
            assertEquals(JsonToken.END_OBJECT, parser.nextToken());

            // field "arr"
            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("arr", parser.getCurrentName());
            assertEquals(JsonToken.START_ARRAY, parser.nextToken());
            assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
            assertEquals(1, parser.getIntValue());
            assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
            assertEquals(2, parser.getIntValue());
            assertEquals(JsonToken.END_ARRAY, parser.nextToken());

            // END_OBJECT
            assertEquals(JsonToken.END_OBJECT, parser.nextToken());
            assertNull(parser.nextToken());
        }
    }

    // ----------------------------------------------------------
    // Test traversing an array with mixed elements
    // ----------------------------------------------------------
    @Test
    public void testArrayWithMixedElements() throws Exception {
        ArrayNode root = factory.arrayNode();
        root.add("text");
        root.add(3.14);
        root.add(factory.booleanNode(false));
        root.add(factory.nullNode());
        root.add(factory.objectNode().put("inner", "value"));
        root.add(factory.arrayNode().add(1));

        try (TreeTraversingParser parser = new TreeTraversingParser(root, mapper.getDeserializationConfig())) {
            assertEquals(JsonToken.START_ARRAY, parser.nextToken());

            assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
            assertEquals("text", parser.getText());

            assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
            assertEquals(3.14, parser.getDoubleValue(), 1e-9);

            assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());

            assertEquals(JsonToken.VALUE_NULL, parser.nextToken());

            // object inside array: START_OBJECT, FIELD_NAME, VALUE_STRING, END_OBJECT
            assertEquals(JsonToken.START_OBJECT, parser.nextToken());
            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("inner", parser.getCurrentName());
            assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
            assertEquals("value", parser.getText());
            assertEquals(JsonToken.END_OBJECT, parser.nextToken());

            // array inside array
            assertEquals(JsonToken.START_ARRAY, parser.nextToken());
            assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
            assertEquals(1, parser.getIntValue());
            assertEquals(JsonToken.END_ARRAY, parser.nextToken());

            assertEquals(JsonToken.END_ARRAY, parser.nextToken());
            assertNull(parser.nextToken());
        }
    }

    // ----------------------------------------------------------
    // Test BigInteger and BigDecimal nodes
    // ----------------------------------------------------------
    @Test
    public void testBigIntegers() throws Exception {
        BigInteger bigInt = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE);
        NumericNode node = factory.numberNode(bigInt);
        try (TreeTraversingParser parser = new TreeTraversingParser(node, mapper.getDeserializationConfig())) {
            assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
            assertEquals(bigInt, parser.getBigIntegerValue());
            assertTrue(parser.getIntValue() == 0); // overflow but not NPE
        }
    }

    @Test
    public void testBigDecimals() throws Exception {
        BigDecimal bd = new BigDecimal("12345678901234567890.123456789");
        NumericNode node = factory.numberNode(bd);
        try (TreeTraversingParser parser = new TreeTraversingParser(node, mapper.getDeserializationConfig())) {
            assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
            assertEquals(bd, parser.getDecimalValue());
        }
    }

    // ----------------------------------------------------------
    // Test BinaryNode (base64 encoded)
    // ----------------------------------------------------------
    @Test
    public void testBinaryNode() throws Exception {
        byte[] data = {0, 1, 2, -1, 127};
        BinaryNode node = new BinaryNode(data);
        try (TreeTraversingParser parser = new TreeTraversingParser(node, mapper.getDeserializationConfig())) {
            assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
            String encoded = parser.getText();
            assertNotNull(encoded);
            assertTrue(encoded.length() > 0);
            byte[] decoded = parser.getBinaryValue();
            assertEquals(data.length, decoded.length);
            for (int i = 0; i < data.length; i++) {
                assertEquals(data[i], decoded[i]);
            }
        }
    }

    // ----------------------------------------------------------
    // Test parsing context info
    // ----------------------------------------------------------
    @Test
    public void testParsingContext() throws Exception {
        ObjectNode root = factory.objectNode();
        root.put("array", factory.arrayNode().add(1).add(2));
        try (TreeTraversingParser parser = new TreeTraversingParser(root, mapper.getDeserializationConfig())) {
            parser.nextToken(); // START_OBJECT
            assertNotNull(parser.getParsingContext());
            assertEquals(0, parser.getParsingContext().getEntryCount());

            parser.nextToken(); // FIELD_NAME
            assertEquals(1, parser.getParsingContext().getEntryCount());

            parser.nextToken(); // START_ARRAY
            JsonParser current = parser;
            // getParsingContext should reflect array
            assertNotNull(parser.getParsingContext());
            // After entering array, entry count is from array perspective
            parser.nextToken(); // 1
            parser.nextToken(); // 2
            parser.nextToken(); // END_ARRAY
            parser.nextToken(); // END_OBJECT
        }
    }

    // ----------------------------------------------------------
    // Test deep nesting to check stack or recursion limits
    // ----------------------------------------------------------
    @Test
    public void testDeeplyNestedObject() throws Exception {
        int depth = 1000; // sufficient to trigger potential StackOveflow
        ObjectNode root = factory.objectNode();
        ObjectNode current = root;
        for (int i = 0; i < depth; i++) {
            ObjectNode child = factory.objectNode();
            current.set("a", child);
            current = child;
        }
        // Traverse fully
        try (TreeTraversingParser parser = new TreeTraversingParser(root, mapper.getDeserializationConfig())) {
            // There will be depth+1 START_OBJECT tokens, then depth+1 END_OBJECT tokens
            for (int i = 0; i <= depth; i++) {
                assertEquals("Failed at depth " + i, JsonToken.START_OBJECT, parser.nextToken());
                if (i < depth) {
                    assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
                    assertEquals("a", parser.getCurrentName());
                }
            }
            for (int i = 0; i <= depth; i++) {
                assertEquals(JsonToken.END_OBJECT, parser.nextToken());
            }
            assertNull(parser.nextToken());
        }
    }

    // ----------------------------------------------------------
    // Test number parsing for overflow/underflow
    // ----------------------------------------------------------
    @Test
    public void testNumberOverflow() throws Exception {
        // Large long that fits but will cause overflow if cast to int
        long largeLong = 20000000000L;
        NumericNode node = factory.numberNode(largeLong);
        try (TreeTraversingParser parser = new TreeTraversingParser(node, mapper.getDeserializationConfig())) {
            assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
            assertEquals(largeLong, parser.getLongValue());
            // getIntValue() should truncate but not throw
            int intVal = parser.getIntValue();
            assertEquals((int) largeLong, intVal);
        }
    }

    // ----------------------------------------------------------
    // Test unexpected getIntValue on text node
    // ----------------------------------------------------------
    @Test(expected = JsonParseException.class)
    public void testGetIntValueOnTextNode() throws Exception {
        TextNode node = factory.textNode("not a number");
        try (TreeTraversingParser parser = new TreeTraversingParser(node, mapper.getDeserializationConfig())) {
            parser.nextToken();
            parser.getIntValue(); // should throw JsonParseException
        }
    }

    // ----------------------------------------------------------
    // Test getBinaryValue on non-binary node
    // ----------------------------------------------------------
    @Test(expected = JsonParseException.class)
    public void testGetBinaryValueOnTextNode() throws Exception {
        TextNode node = factory.textNode("plain text");
        try (TreeTraversingParser parser = new TreeTraversingParser(node, mapper.getDeserializationConfig())) {
            parser.nextToken();
            parser.getBinaryValue(); // should throw
        }
    }

    // ----------------------------------------------------------
    // Test getValueAsX methods on null node
    // ----------------------------------------------------------
    @Test
    public void testNullDefaults() throws Exception {
        NullNode node = factory.nullNode();
        try (TreeTraversingParser parser = new TreeTraversingParser(node, mapper.getDeserializationConfig())) {
            parser.nextToken();
            assertEquals("default", parser.getValueAsString("default"));
            assertEquals(0, parser.getValueAsInt(0));
            assertEquals(0L, parser.getValueAsLong(0));
            assertEquals(0.0, parser.getValueAsDouble(0.0), 0.0);
            assertFalse(parser.getValueAsBoolean(false));
        }
    }

    // ----------------------------------------------------------
    // Test getEmbeddedObject for various nodes
    // ----------------------------------------------------------
    @Test
    public void testGetEmbeddedObject() throws Exception {
        // For NullNode
        try (TreeTraversingParser parser = new TreeTraversingParser(factory.nullNode(), mapper.getDeserializationConfig())) {
            parser.nextToken();
            assertNull(parser.getEmbeddedObject());
        }
        // For ObjectNode
        try (TreeTraversingParser parser = new TreeTraversingParser(factory.objectNode(), mapper.getDeserializationConfig())) {
            parser.nextToken();
            Object obj = parser.getEmbeddedObject();
            assertTrue(obj instanceof ObjectNode);
        }
        // For ArrayNode
        try (TreeTraversingParser parser = new TreeTraversingParser(factory.arrayNode(), mapper.getDeserializationConfig())) {
            parser.nextToken();
            assertTrue(parser.getEmbeddedObject() instanceof ArrayNode);
        }
        // For TextNode
        try (TreeTraversingParser parser = new TreeTraversingParser(factory.textNode("test"), mapper.getDeserializationConfig())) {
            parser.nextToken();
            assertEquals("test", parser.getEmbeddedObject());
        }
    }

    // ----------------------------------------------------------
    // Test skipChildren for object and array
    // ----------------------------------------------------------
    @Test
    public void testSkipChildrenObject() throws Exception {
        ObjectNode root = factory.objectNode();
        root.put("a", 1);
        root.put("b", 2);
        try (TreeTraversingParser parser = new TreeTraversingParser(root, mapper.getDeserializationConfig())) {
            assertEquals(JsonToken.START_OBJECT, parser.nextToken());
            parser.skipChildren(); // skip entire object
            assertEquals(JsonToken.END_OBJECT, parser.getCurrentToken());
            assertNull(parser.nextToken());
        }
    }

    @Test
    public void testSkipChildrenArray() throws Exception {
        ArrayNode root = factory.arrayNode();
        root.add(1);
        root.add(2);
        try (TreeTraversingParser parser = new TreeTraversingParser(root, mapper.getDeserializationConfig())) {
            assertEquals(JsonToken.START_ARRAY, parser.nextToken());
            parser.skipChildren();
            assertEquals(JsonToken.END_ARRAY, parser.getCurrentToken());
            assertNull(parser.nextToken());
        }
    }

    // ----------------------------------------------------------
    // Test fields order preservation (if object insertion order is kept)
    // ----------------------------------------------------------
    @Test
    public void testFieldOrdering() throws Exception {
        ObjectNode root = factory.objectNode();
        root.put("z", 1);
        root.put("a", 2);
        root.put("m", 3);
        try (TreeTraversingParser parser = new TreeTraversingParser(root, mapper.getDeserializationConfig())) {
            parser.nextToken(); // START_OBJECT
            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("z", parser.getCurrentName());
            parser.nextToken();
            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("a", parser.getCurrentName());
            parser.nextToken();
            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("m", parser.getCurrentName());
            parser.nextToken();
            assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        }
    }

    // ----------------------------------------------------------
    // Test duplicate field names (allowed by JsonNode)
    // ----------------------------------------------------------
    @Test
    public void testDuplicateFieldNames() throws Exception {
        ObjectNode root = factory.objectNode();
        root.put("x", 1);
        root.put("x", 2);
        try (TreeTraversingParser parser = new TreeTraversingParser(root, mapper.getDeserializationConfig())) {
            parser.nextToken(); // START_OBJECT
            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("x", parser.getCurrentName());
            assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
            assertEquals(1, parser.getIntValue());
            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("x", parser.getCurrentName());
            assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
            assertEquals(2, parser.getIntValue());
            assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        }
    }

    // ----------------------------------------------------------
    // Test traversal with missing codec (null config)
    // ----------------------------------------------------------
    @Test
    public void testNullCodecParsing() throws Exception {
        // Parser should work without codec for basic traversal
        ObjectNode root = factory.objectNode().put("a", 1);
        try (TreeTraversingParser parser = new TreeTraversingParser(root, (com.fasterxml.jackson.core.ObjectCodec) null)) {
            assertEquals(JsonToken.START_OBJECT, parser.nextToken());
            // getCodec should return null
            assertNull(parser.getCodec());
            // should still work
            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("a", parser.getCurrentName());
            assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
            assertEquals(1, parser.getIntValue());
            assertEquals(JsonToken.END_OBJECT, parser.nextToken());
            assertNull(parser.nextToken());
        }
    }

    // ----------------------------------------------------------
    // Test close and re-traverse (should not be allowed)
    // ----------------------------------------------------------
    @Test(expected = JsonParseException.class)
    public void testTraverseAfterClose() throws Exception {
        NullNode node = factory.nullNode();
        TreeTraversingParser parser = new TreeTraversingParser(node, mapper.getDeserializationConfig());
        parser.close();
        parser.nextToken(); // should throw
    }

    // ----------------------------------------------------------
    // Test getTokenLocation and getCurrentLocation
    // ----------------------------------------------------------
    @Test
    public void testLocations() throws Exception {
        ValueNode node = factory.numberNode(1);
        try (TreeTraversingParser parser = new TreeTraversingParser(node, mapper.getDeserializationConfig())) {
            assertEquals(0, parser.getTokenLocation().getCharOffset());
            assertEquals(0, parser.getCurrentLocation().getCharOffset());
            parser.nextToken();
            assertNotNull(parser.getTokenLocation());
            assertNotNull(parser.getCurrentLocation());
        }
    }

    // ----------------------------------------------------------
    // Test version
    // ----------------------------------------------------------
    @Test
    public void testVersion() {
        TreeTraversingParser parser = new TreeTraversingParser(factory.nullNode(), mapper.getDeserializationConfig());
        assertNotNull(parser.version());
        assertEquals(com.fasterxml.jackson.core.util.VersionUtil.parseVersion(
                "2.13.0", "com.fasterxml.jackson.datatype", "jackson-databind"), parser.version());
    }

    // ----------------------------------------------------------
    // Test getNumberType on various numeric nodes
    // ----------------------------------------------------------
    @Test
    public void testGetNumberType() throws Exception {
        // IntNode
        try (TreeTraversingParser parser = new TreeTraversingParser(factory.numberNode(42), mapper.getDeserializationConfig())) {
            parser.nextToken();
            assertEquals(com.fasterxml.jackson.core.JsonParser.NumberType.INT, parser.getNumberType());
        }
        // LongNode
        try (TreeTraversingParser parser = new TreeTraversingParser(factory.numberNode(Long.MAX_VALUE), mapper.getDeserializationConfig())) {
            parser.nextToken();
            assertEquals(com.fasterxml.jackson.core.JsonParser.NumberType.LONG, parser.getNumberType());
        }
        // BigIntegerNode
        try (TreeTraversingParser parser = new TreeTraversingParser(factory.numberNode(BigInteger.ONE), mapper.getDeserializationConfig())) {
            parser.nextToken();
            assertEquals(com.fasterxml.jackson.core.JsonParser.NumberType.BIG_INTEGER, parser.getNumberType());
        }
        // FloatNode
        try (TreeTraversingParser parser = new TreeTraversingParser(factory.numberNode(1.5f), mapper.getDeserializationConfig())) {
            parser.nextToken();
            assertEquals(com.fasterxml.jackson.core.JsonParser.NumberType.FLOAT, parser.getNumberType());
        }
        // DoubleNode
        try (TreeTraversingParser parser = new TreeTraversingParser(factory.numberNode(2.5), mapper.getDeserializationConfig())) {
            parser.nextToken();
            assertEquals(com.fasterxml.jackson.core.JsonParser.NumberType.DOUBLE, parser.getNumberType());
        }
        // DecimalNode (BigDecimal)
        try (TreeTraversingParser parser = new TreeTraversingParser(factory.numberNode(new BigDecimal("10.5")), mapper.getDeserializationConfig())) {
            parser.nextToken();
            assertEquals(com.fasterxml.jackson.core.JsonParser.NumberType.BIG_DECIMAL, parser.getNumberType());
        }
    }

    // ----------------------------------------------------------
    // Test getIntValue on various nodes (including overflow)
    // ----------------------------------------------------------
    @Test
    public void testGetIntValueOnLongOverflow() throws Exception {
        // Long value > Integer.MAX_VALUE
        long big = (long) Integer.MAX_VALUE + 10;
        try (TreeTraversingParser parser = new TreeTraversingParser(factory.numberNode(big), mapper.getDeserializationConfig())) {
            parser.nextToken();
            // should not throw, just truncate
            parser.getIntValue(); // fine
        }
    }

    // ----------------------------------------------------------
    // Test getFloatValue on IntNode
    // ----------------------------------------------------------
    @Test
    public void testGetFloatValueOnIntNode() throws Exception {
        try (TreeTraversingParser parser = new TreeTraversingParser(factory.numberNode(123), mapper.getDeserializationConfig())) {
            parser.nextToken();
            assertEquals(123.0f, parser.getFloatValue(), 0.0f);
        }
    }

    // ----------------------------------------------------------
    // Test parsing of BooleanNode
    // ----------------------------------------------------------
    @Test
    public void testBooleanNodeTrue() throws Exception {
        try (TreeTraversingParser parser = new TreeTraversingParser(factory.booleanNode(true), mapper.getDeserializationConfig())) {
            assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
            assertTrue(parser.getBooleanValue());
        }
    }

    @Test
    public void testBooleanNodeFalse() throws Exception {
        try (TreeTraversingParser parser = new TreeTraversingParser(factory.booleanNode(false), mapper.getDeserializationConfig())) {
            assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
            assertFalse(parser.getBooleanValue());
        }
    }

    // ----------------------------------------------------------
    // Test getText on TextNode
    // ----------------------------------------------------------
    @Test
    public void testTextNode() throws Exception {
        String text = "Hello, World!";
        try (TreeTraversingParser parser = new TreeTraversingParser(factory.textNode(text), mapper.getDeserializationConfig())) {
            assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
            assertEquals(text, parser.getText());
        }
    }

    // ----------------------------------------------------------
    // Test getText on NumericNode (should return number as string)
    // ----------------------------------------------------------
    @Test
    public void testGetTextOnNumericNode() throws Exception {
        try (TreeTraversingParser parser = new TreeTraversingParser(factory.numberNode(100), mapper.getDeserializationConfig())) {
            assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
            assertEquals("100", parser.getText());
        }
    }

    // ----------------------------------------------------------
    // Test nextValue() method
    // ----------------------------------------------------------
    @Test
    public void testNextValue() throws Exception {
        ObjectNode root = factory.objectNode();
        root.put("a", "x");
        try (TreeTraversingParser parser = new TreeTraversingParser(root, mapper.getDeserializationConfig())) {
            parser.nextToken(); // START_OBJECT
            // nextValue should skip field name and move to value
            assertEquals(JsonToken.VALUE_STRING, parser.nextValue());
            assertEquals("x", parser.getText());
            assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        }
    }

    // ----------------------------------------------------------
    // Test finishToken() for completeness (if needed)
    // ----------------------------------------------------------
    // Not explicitly needed as it's internal, but we can call indirectly

    // ----------------------------------------------------------
    // Test getParsingContext after end-of-stream
    // ----------------------------------------------------------
    @Test
    public void testParsingContextAfterEndOfStream() throws Exception {
        try (TreeTraversingParser parser = new TreeTraversingParser(factory.nullNode(), mapper.getDeserializationConfig())) {
            parser.nextToken();
            parser.nextToken(); // null, end
            assertNotNull(parser.getParsingContext());
            assertTrue(parser.getParsingContext().inRoot());
        }
    }

    // ----------------------------------------------------------
    // Test null text node (can't be created via factory, but via ObjectNode.putNull)
    // ----------------------------------------------------------
    @Test
    public void testNullValueInsideObject() throws Exception {
        ObjectNode root = factory.objectNode();
        root.putNull("nullField");
        try (TreeTraversingParser parser = new TreeTraversingParser(root, mapper.getDeserializationConfig())) {
            parser.nextToken(); // START_OBJECT
            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("nullField", parser.getCurrentName());
            assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
            assertEquals("null", parser.getText());
            assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        }
    }

    // ----------------------------------------------------------
    // Test NaN and Infinity double values (if allowed)
    // ----------------------------------------------------------
    @Test
    public void testNaNInfinityDoubles() throws Exception {
        // Jackson can handle NaN, Infinity in JSON, but as nodes?
        // DoubleNode can hold them.
        try (TreeTraversingParser parser = new TreeTraversingParser(factory.numberNode(Double.NaN), mapper.getDeserializationConfig())) {
            parser.nextToken();
            assertTrue(Double.isNaN(parser.getDoubleValue()));
            assertEquals("NaN", parser.getText());
        }
        try (TreeTraversingParser parser = new TreeTraversingParser(factory.numberNode(Double.POSITIVE_INFINITY), mapper.getDeserializationConfig())) {
            parser.nextToken();
            assertTrue(Double.isInfinite(parser.getDoubleValue()));
            assertEquals("Infinity", parser.getText());
        }
        try (TreeTraversingParser parser = new TreeTraversingParser(factory.numberNode(Double.NEGATIVE_INFINITY), mapper.getDeserializationConfig())) {
            parser.nextToken();
            assertTrue(parser.getDoubleValue() < 0);
            assertEquals("-Infinity", parser.getText());
        }
    }

    // ----------------------------------------------------------
    // Test big numbers: long vs BigInteger boundary
    // ----------------------------------------------------------
    @Test
    public void testLongMaxValueNode() throws Exception {
        long max = Long.MAX_VALUE;
        try (TreeTraversingParser parser = new TreeTraversingParser(factory.numberNode(max), mapper.getDeserializationConfig())) {
            parser.nextToken();
            assertEquals(max, parser.getLongValue());
            assertEquals(com.fasterxml.jackson.core.JsonParser.NumberType.LONG, parser.getNumberType());
        }
    }

    @Test
    public void testLongMinValueNode() throws Exception {
        long min = Long.MIN_VALUE;
        try (TreeTraversingParser parser = new TreeTraversingParser(factory.numberNode(min), mapper.getDeserializationConfig())) {
            parser.nextToken();
            assertEquals(min, parser.getLongValue());
            assertEquals(com.fasterxml.jackson.core.JsonParser.NumberType.LONG, parser.getNumberType());
        }
    }

    // ----------------------------------------------------------
    // Test that parsing multiple times yields the same tokens
    // (idempotency not guaranteed, but just ensure no exception)
    // ----------------------------------------------------------
    @Test
    public void testMultipleTraversalsOnNewParser() throws Exception {
        ArrayNode root = factory.arrayNode();
        root.add(1);
        root.add(2);
        // Each parser instance should produce correct sequence
        for (int i = 0; i < 3; i++) {
            try (TreeTraversingParser parser = new TreeTraversingParser(root, mapper.getDeserializationConfig())) {
                assertEquals(JsonToken.START_ARRAY, parser.nextToken());
                assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
                assertEquals(1, parser.getIntValue());
                assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
                assertEquals(2, parser.getIntValue());
                assertEquals(JsonToken.END_ARRAY, parser.nextToken());
                assertNull(parser.nextToken());
            }
        }
    }

    // ----------------------------------------------------------
    // Test empty POJO (object with no fields) after fields
    // ----------------------------------------------------------
    @Test
    public void testEmptyObjectAtRoot() throws Exception {
        ObjectNode root = factory.objectNode();
        try (TreeTraversingParser parser = new TreeTraversingParser(root, mapper.getDeserializationConfig())) {
            assertEquals(JsonToken.START_OBJECT, parser.nextToken());
            assertEquals(JsonToken.END_OBJECT, parser.nextToken());
            assertNull(parser.nextToken());
        }
    }

    // ----------------------------------------------------------
    // Test object with many levels of nesting and arrays
    // ----------------------------------------------------------
    @Test
    public void testComplexNestedStructure() throws Exception {
        ObjectNode root = factory.objectNode();
        ObjectNode innerObj = factory.objectNode();
        innerObj.put("key", "val");
        ArrayNode innerArr = factory.arrayNode();
        innerArr.add(1).add(2).add(3);
        root.set("obj", innerObj);
        root.set("arr", innerArr);
        root.put("simple", true);

        try (TreeTraversingParser parser = new TreeTraversingParser(root, mapper.getDeserializationConfig())) {
            parser.nextToken(); // START_OBJECT
            // obj field
            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("obj", parser.getCurrentName());
            assertEquals(JsonToken.START_OBJECT, parser.nextToken());
            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("key", parser.getCurrentName());
            assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
            assertEquals("val", parser.getText());
            assertEquals(JsonToken.END_OBJECT, parser.nextToken());

            // arr field
            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("arr", parser.getCurrentName());
            assertEquals(JsonToken.START_ARRAY, parser.nextToken());
            assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
            assertEquals(1, parser.getIntValue());
            assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
            assertEquals(2, parser.getIntValue());
            assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
            assertEquals(3, parser.getIntValue());
            assertEquals(JsonToken.END_ARRAY, parser.nextToken());

            // simple field
            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("simple", parser.getCurrentName());
            assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());

            assertEquals(JsonToken.END_OBJECT, parser.nextToken());
            assertNull(parser.nextToken());
        }
    }

    // ----------------------------------------------------------
    // Test an edge case: JsonNode that is not Object/Array, but used as root
    // ----------------------------------------------------------
    @Test
    public void testValueNodeAsRoot() throws Exception {
        ValueNode node = factory.numberNode(10);
        try (TreeTraversingParser parser = new TreeTraversingParser(node, mapper.getDeserializationConfig())) {
            assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
            assertEquals(10, parser.getIntValue());
            assertNull(parser.nextToken());
        }
    }

    // ----------------------------------------------------------
    // Test getCurrentToken on fresh parser
    // ----------------------------------------------------------
    @Test
    public void testCurrentTokenBeforeNext() throws Exception {
        try (TreeTraversingParser parser = new TreeTraversingParser(factory.nullNode(), mapper.getDeserializationConfig())) {
            assertNull(parser.getCurrentToken());
            parser.nextToken();
            assertNotNull(parser.getCurrentToken());
        }
    }

    // ----------------------------------------------------------
    // Test clearCurrentToken
    // ----------------------------------------------------------
    @Test
    public void testClearCurrentToken() throws Exception {
        try (TreeTraversingParser parser = new TreeTraversingParser(factory.nullNode(), mapper.getDeserializationConfig())) {
            parser.nextToken();
            assertNotNull(parser.getCurrentToken());
            parser.clearCurrentToken();
            assertNull(parser.getCurrentToken());
        }
    }

    // ----------------------------------------------------------
    // Test getTextCharacters, getTextOffset, getTextLength
    // ----------------------------------------------------------
    @Test
    public void testTextCharArray() throws Exception {
        String text = "hello";
        try (TreeTraversingParser parser = new TreeTraversingParser(factory.textNode(text), mapper.getDeserializationConfig())) {
            parser.nextToken();
            char[] chars = parser.getTextCharacters();
            int offset = parser.getTextOffset();
            int len = parser.getTextLength();
            assertEquals(text, new String(chars, offset, len));
        }
    }

    // ----------------------------------------------------------
    // Test hasTextCharacters for value nodes
    // ----------------------------------------------------------
    @Test
    public void testHasTextCharacters() throws Exception {
        try (TreeTraversingParser parser = new TreeTraversingParser(factory.textNode("abc"), mapper.getDeserializationConfig())) {
            parser.nextToken();
            assertTrue(parser.hasTextCharacters());
        }
        try (TreeTraversingParser parser = new TreeTraversingParser(factory.numberNode(1), mapper.getDeserializationConfig())) {
            parser.nextToken();
            // For numeric nodes, hasTextCharacters may return false or true depending on implementation
            // Just ensure no exception
            parser.hasTextCharacters();
        }
    }

    // ----------------------------------------------------------
    // Test releaseBuffered
    // ----------------------------------------------------------
    @Test
    public void testReleaseBuffered() throws Exception {
        try (TreeTraversingParser parser = new TreeTraversingParser(factory.textNode("test"), mapper.getDeserializationConfig())) {
            // No buffer to release, just call to ensure no exception
            parser.releaseBuffered(null);
            parser.releaseBuffered(System.out);
        }
    }

    // ----------------------------------------------------------
    // Test overrideCurrentName
    // ----------------------------------------------------------
    @Test
    public void testOverrideCurrentName() throws Exception {
        ObjectNode root = factory.objectNode();
        root.put("foo", 1);
        try (TreeTraversingParser parser = new TreeTraversingParser(root, mapper.getDeserializationConfig())) {
            parser.nextToken(); // START_OBJECT
            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("foo", parser.getCurrentName());
            parser.overrideCurrentName("bar");
            assertEquals("bar", parser.getCurrentName());
        }
    }

    // ----------------------------------------------------------
    // Test canReadObjectId, canReadTypeId, etc. (should return false)
    // ----------------------------------------------------------
    @Test
    public void testCapabilities() throws Exception {
        try (TreeTraversingParser parser = new TreeTraversingParser(factory.nullNode(), mapper.getDeserializationConfig())) {
            assertFalse(parser.canReadObjectId());
            assertFalse(parser.canReadTypeId());
            assertFalse(parser.canReadObjectId());
            // getObjectId, getTypeId should throw or return null
            assertNull(parser.getObjectId());
            assertNull(parser.getTypeId());
        }
    }
}