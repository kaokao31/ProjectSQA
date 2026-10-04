package com.fasterxml.jackson.databind.node;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class TreeTraversingParserTest {

    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
    }

    @After
    public void tearDown() {
        objectMapper = null;
    }

    @Test
    public void testParserNullNode() throws IOException {
        NullNode node = NullNode.instance;
        TreeTraversingParser parser = new TreeTraversingParser(node, objectMapper);

        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertTrue(parser.getCurrentToken().isScalarValue());
        assertNull(parser.getText());
        assertEquals("null", parser.getValueAsString());
        assertNull(parser.getEmbeddedObject());
        assertFalse(parser.hasTextCharacters());

        parser.close();
        assertTrue(parser.isClosed());
    }

    @Test
    public void testParserBooleanNodes() throws IOException {
        // True node
        BooleanNode trueNode = BooleanNode.TRUE;
        TreeTraversingParser parser1 = new TreeTraversingParser(trueNode, objectMapper);
        assertEquals(JsonToken.VALUE_TRUE, parser1.nextToken());
        assertTrue(parser1.getBooleanValue());
        assertEquals("true", parser1.getText());
        parser1.close();

        // False node
        BooleanNode falseNode = BooleanNode.FALSE;
        TreeTraversingParser parser2 = new TreeTraversingParser(falseNode, objectMapper);
        assertEquals(JsonToken.VALUE_FALSE, parser2.nextToken());
        assertFalse(parser2.getBooleanValue());
        assertEquals("false", parser2.getText());
        parser2.close();
    }

    @Test
    public void testParserNumericNodes() throws IOException {
        // Int node
        NumericNode intNode = IntNode.valueOf(123);
        TreeTraversingParser parser1 = new TreeTraversingParser(intNode, objectMapper);
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser1.nextToken());
        assertEquals(JsonParser.NumberType.INT, parser1.getNumberType());
        assertEquals(123, parser1.getIntValue());
        assertEquals(123L, parser1.getLongValue());
        assertEquals(123.0, parser1.getDoubleValue(), 0.0);
        assertEquals(123.0f, parser1.getFloatValue(), 0.0f);
        assertEquals(BigInteger.valueOf(123), parser1.getBigIntegerValue());
        assertEquals(BigDecimal.valueOf(123), parser1.getDecimalValue());
        assertEquals(123, parser1.getNumberValue());
        parser1.close();

        // Long node
        NumericNode longNode = LongNode.valueOf(123456789L);
        TreeTraversingParser parser2 = new TreeTraversingParser(longNode, objectMapper);
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser2.nextToken());
        assertEquals(JsonParser.NumberType.LONG, parser2.getNumberType());
        assertEquals(123456789L, parser2.getLongValue());
        parser2.close();

        // Float node
        NumericNode floatNode = FloatNode.valueOf(12.34f);
        TreeTraversingParser parser3 = new TreeTraversingParser(floatNode, objectMapper);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser3.nextToken());
        assertEquals(JsonParser.NumberType.FLOAT, parser3.getNumberType());
        assertEquals(12.34f, parser3.getFloatValue(), 0.0001f);
        parser3.close();

        // Double node
        NumericNode doubleNode = DoubleNode.valueOf(123.456);
        TreeTraversingParser parser4 = new TreeTraversingParser(doubleNode, objectMapper);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser4.nextToken());
        assertEquals(JsonParser.NumberType.DOUBLE, parser4.getNumberType());
        assertEquals(123.456, parser4.getDoubleValue(), 0.0001);
        parser4.close();

        // BigInteger node
        NumericNode bigIntNode = BigIntegerNode.valueOf(BigInteger.valueOf(987654321L));
        TreeTraversingParser parser5 = new TreeTraversingParser(bigIntNode, objectMapper);
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser5.nextToken());
        assertEquals(JsonParser.NumberType.BIG_INTEGER, parser5.getNumberType());
        assertEquals(BigInteger.valueOf(987654321L), parser5.getBigIntegerValue());
        parser5.close();

        // BigDecimal node
        NumericNode bigDecNode = DecimalNode.valueOf(BigDecimal.valueOf(123.456789));
        TreeTraversingParser parser6 = new TreeTraversingParser(bigDecNode, objectMapper);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser6.nextToken());
        assertEquals(JsonParser.NumberType.BIG_DECIMAL, parser6.getNumberType());
        assertEquals(BigDecimal.valueOf(123.456789), parser6.getDecimalValue());
        parser6.close();

        // Short node
        NumericNode shortNode = ShortNode.valueOf((short) 12);
        TreeTraversingParser parser7 = new TreeTraversingParser(shortNode, objectMapper);
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser7.nextToken());
        assertEquals(JsonParser.NumberType.INT, parser7.getNumberType());
        assertEquals(12, parser7.getShortValue());
        parser7.close();
    }

    @Test
    public void testParserTextAndBinaryNodes() throws IOException, Exception {
        // Text node
        TextNode textNode = TextNode.valueOf("hello world");
        TreeTraversingParser parser1 = new TreeTraversingParser(textNode, objectMapper);
        assertEquals(JsonToken.VALUE_STRING, parser1.nextToken());
        assertEquals("hello world", parser1.getText());
        char[] chars = parser1.getTextCharacters();
        assertNotNull(chars);
        assertEquals(0, parser1.getTextOffset());
        assertEquals("hello world".length(), parser1.getTextLength());
        parser1.close();

        // Binary node
        byte[] data = new byte[]{1, 2, 3, 4};
        BinaryNode binaryNode = BinaryNode.valueOf(data);
        TreeTraversingParser parser2 = new TreeTraversingParser(binaryNode, objectMapper);
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, parser2.nextToken());
        assertArrayEquals(data, parser2.getBinaryValue());
        assertEquals(data, parser2.getEmbeddedObject());
        parser2.close();
        
        // POJO node
        POJONode pojoNode = new POJONode("custom-pojo");
        TreeTraversingParser parser3 = new TreeTraversingParser(pojoNode, objectMapper);
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, parser3.nextToken());
        assertEquals("custom-pojo", parser3.getEmbeddedObject());
        parser3.close();
    }

    @Test
    public void testParserObjectNode() throws IOException {
        ObjectNode objNode = objectMapper.createObjectNode();
        objNode.put("stringField", "val");
        objNode.put("intField", 42);
        
        TreeTraversingParser parser = new TreeTraversingParser(objNode, objectMapper);
        
        // Initially null token before nextToken()
        assertNull(parser.getCurrentToken());
        
        // START_OBJECT
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertTrue(parser.isExpectedStartObjectToken());
        
        // FIELD_NAME: stringField
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("stringField", parser.getCurrentName());
        assertEquals("stringField", parser.getText());
        
        // VALUE_STRING: val
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("val", parser.getText());
        assertEquals("stringField", parser.getCurrentName());
        
        // FIELD_NAME: intField
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("intField", parser.getCurrentName());
        
        // VALUE_NUMBER_INT: 42
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        
        // END_OBJECT
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        
        // EOF
        assertNull(parser.nextToken());
        assertEquals(JsonToken.NOT_AVAILABLE, parser.currentToken());

        parser.close();
    }

    @Test
    public void testParserArrayNode() throws IOException {
        ArrayNode arrNode = objectMapper.createArrayNode();
        arrNode.add("elem1");
        arrNode.add(100);

        TreeTraversingParser parser = new TreeTraversingParser(arrNode, objectMapper);

        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertTrue(parser.isExpectedStartArrayToken());

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("elem1", parser.getText());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(100, parser.getIntValue());

        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());

        parser.close();
    }

    @Test
    public void testParserNavigationAndScoping() throws IOException {
        ObjectNode root = objectMapper.createObjectNode();
        ObjectNode innerObj = objectMapper.createObjectNode();
        innerObj.put("a", 1);
        root.set("inner", innerObj);

        ArrayNode innerArr = objectMapper.createArrayNode();
        innerArr.add(true);
        root.set("arr", innerArr);

        TreeTraversingParser parser = new TreeTraversingParser(root, objectMapper);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("inner", parser.getCurrentName());
        
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(parser.getParsingContext().getParent().getCurrentName(), "inner");

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("arr", parser.getCurrentName());

        assertEquals(JsonToken.START_ARRAY, parser.nextToken());

        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());

        assertEquals(JsonToken.END_ARRAY, parser.nextToken());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());

        parser.close();
    }

    @Test
    public void testParserSkipChildren() throws IOException {
        ObjectNode root = objectMapper.createObjectNode();
        ObjectNode innerObj = objectMapper.createObjectNode();
        innerObj.put("x", 10);
        innerObj.put("y", 20);
        root.set("childObj", innerObj);
        root.put("after", "val");

        TreeTraversingParser parser = new TreeTraversingParser(root, objectMapper);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("childObj", parser.getCurrentName());

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        
        // Skip all tokens inside childObj
        parser.skipChildren();
        // After skipChildren, current token should point to END_OBJECT of childObj or equivalent depending on implementation
        
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("after", parser.getCurrentName());

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("val", parser.getText());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());

        parser.close();
    }

    @Test
    public void testParserOverrideCurrentNameAndClear() throws IOException {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("key", "value");

        TreeTraversingParser parser = new TreeTraversingParser(root, objectMapper);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("key", parser.getCurrentName());
        
        parser.setCurrentName("overriddenKey");
        assertEquals("overriddenKey", parser.getCurrentName());

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("value", parser.getText());
        // Verify current name is still accessible after value token
        assertEquals("overriddenKey", parser.getCurrentName());

        parser.clearCurrentToken();
        assertNull(parser.getCurrentToken());

        parser.close();
    }

    @Test
    public void testParserLocationAndCodec() {
        JsonNode node = NullNode.instance;
        TreeTraversingParser parser = new TreeTraversingParser(node, objectMapper);

        assertNotNull(parser.getCodec());
        assertEquals(objectMapper, parser.getCodec());

        assertNotNull(parser.getTokenLocation());
        assertNotNull(parser.getCurrentLocation());
        
        assertNotNull(parser.version());
        
        parser.setCodec(null);
        assertNull(parser.getCodec());
    }

    @Test(expected = IOException.class)
    public void testGetNumberValueThrowsOnInvalidNode() throws IOException, UnsupportedOperationException {
        NullNode node = NullNode.instance;
        TreeTraversingParser parser = new TreeTraversingParser(node, objectMapper);
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        // Null node should throw or return null/exception for number value
        parser.getNumberValue();
    }
}