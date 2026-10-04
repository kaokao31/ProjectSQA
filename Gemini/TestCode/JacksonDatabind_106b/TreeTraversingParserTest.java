package com.fasterxml.jackson.databind.node;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.DeserializationConfig;
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
    public void testNullNodeParsing() throws IOException {
        JsonNode node = NullNode.instance;
        TreeTraversingParser parser = new TreeTraversingParser(node, objectMapper);
        
        assertEquals(JsonToken.NOT_AVAILABLE, parser.currentToken());
        assertNull(parser.getCurrentToken());
        
        JsonToken t = parser.nextToken();
        assertEquals(JsonToken.VALUE_NULL, t);
        assertEquals(JsonToken.VALUE_NULL, parser.currentToken());
        assertEquals("null", parser.getText());
        
        // Next token should be null (end of stream/node)
        assertNull(parser.nextToken());
        assertEquals(JsonToken.NOT_AVAILABLE, parser.currentToken());
        
        parser.close();
        assertTrue(parser.isClosed());
    }

    @Test
    public void testBooleanNodeParsing() throws IOException {
        JsonNode trueNode = BooleanNode.TRUE;
        TreeTraversingParser parser = new TreeTraversingParser(trueNode, objectMapper);
        
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertTrue(parser.getBooleanValue());
        assertEquals("true", parser.getText());
        
        parser.close();

        JsonNode falseNode = BooleanNode.FALSE;
        TreeTraversingParser parser2 = new TreeTraversingParser(falseNode, objectMapper);
        assertEquals(JsonToken.VALUE_FALSE, parser2.nextToken());
        assertFalse(parser2.getBooleanValue());
        assertEquals("false", parser2.getText());
        parser2.close();
    }

    @Test
    public void testNumericNodes() throws IOException {
        // IntNode
        TreeTraversingParser p1 = new TreeTraversingParser(new IntNode(123), objectMapper);
        assertEquals(JsonToken.VALUE_NUMBER_INT, p1.nextToken());
        assertEquals(JsonParser.NumberType.INT, p1.getNumberType());
        assertEquals(123, p1.getIntValue());
        assertEquals(123L, p1.getLongValue());
        assertEquals(123.0, p1.getDoubleValue(), 0.0);
        assertEquals(Float.valueOf(123.0f), Float.valueOf(p1.getFloatValue()));
        assertEquals(BigInteger.valueOf(123), p1.getBigIntegerValue());
        assertEquals(BigDecimal.valueOf(123), p1.getDecimalValue());
        assertEquals("123", p1.getText());
        p1.close();

        // LongNode
        TreeTraversingParser p2 = new TreeTraversingParser(new LongNode(1234567890123L), objectMapper);
        assertEquals(JsonToken.VALUE_NUMBER_INT, p2.nextToken());
        assertEquals(JsonParser.NumberType.LONG, p2.getNumberType());
        assertEquals(1234567890123L, p2.getLongValue());
        p2.close();

        // FloatNode
        TreeTraversingParser p3 = new TreeTraversingParser(new FloatNode(1.23f), objectMapper);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p3.nextToken());
        assertEquals(JsonParser.NumberType.FLOAT, p3.getNumberType());
        assertEquals(1.23f, p3.getFloatValue(), 0.0001f);
        p3.close();

        // DoubleNode
        TreeTraversingParser p4 = new TreeTraversingParser(new DoubleNode(1.2345), objectMapper);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p4.nextToken());
        assertEquals(JsonParser.NumberType.DOUBLE, p4.getNumberType());
        assertEquals(1.2345, p4.getDoubleValue(), 0.00001);
        p4.close();

        // BigIntegerNode
        TreeTraversingParser p5 = new TreeTraversingParser(new BigIntegerNode(BigInteger.valueOf(999L)), objectMapper);
        assertEquals(JsonToken.VALUE_NUMBER_INT, p5.nextToken());
        assertEquals(JsonParser.NumberType.BIG_INTEGER, p5.getNumberType());
        assertEquals(BigInteger.valueOf(999L), p5.getBigIntegerValue());
        p5.close();

        // DecimalNode
        TreeTraversingParser p6 = new TreeTraversingParser(new DecimalNode(BigDecimal.TEN), objectMapper);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p6.nextToken());
        assertEquals(JsonParser.NumberType.BIG_DECIMAL, p6.getNumberType());
        assertEquals(BigDecimal.TEN, p6.getDecimalValue());
        p6.close();

        // ShortNode
        TreeTraversingParser p7 = new TreeTraversingParser(new ShortNode((short) 10), objectMapper);
        assertEquals(JsonToken.VALUE_NUMBER_INT, p7.nextToken());
        assertEquals(JsonParser.NumberType.INT, p7.getNumberType());
        assertEquals((short) 10, p7.getShortValue());
        p7.close();
    }

    @Test
    public void testStringAndBinaryNodes() throws IOException, UnsupportedOperationException {
        // TextNode
        TreeTraversingParser p1 = new TreeTraversingParser(new TextNode("hello"), objectMapper);
        assertEquals(JsonToken.VALUE_STRING, p1.nextToken());
        assertEquals("hello", p1.getText());
        char[] chars = p1.getTextCharacters();
        assertNotNull(chars);
        assertEquals(5, p1.getTextLength());
        assertEquals(0, p1.getTextOffset());
        p1.close();

        // BinaryNode
        byte[] data = new byte[]{1, 2, 3};
        TreeTraversingParser p2 = new TreeTraversingParser(BinaryNode.valueOf(data), objectMapper);
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p2.nextToken());
        assertArrayEquals(data, p2.getBinaryValue());
        assertEquals(data, p2.getEmbeddedObject());
        p2.close();

        // POJONode
        TreeTraversingParser p3 = new TreeTraversingParser(new POJONode("pojo-obj"), objectMapper);
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p3.nextToken());
        assertEquals("pojo-obj", p3.getEmbeddedObject());
        p3.close();
    }

    @Test
    public void testObjectNodeParsing() throws IOException {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("stringField", "value");
        root.put("intField", 42);

        TreeTraversingParser parser = new TreeTraversingParser(root, objectMapper);

        // Before first nextToken
        assertNull(parser.getCurrentName());
        
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertNull(parser.getCurrentName());
        
        // First field
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("stringField", parser.getCurrentName());
        assertEquals("stringField", parser.getText());
        
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("stringField", parser.getCurrentName());
        assertEquals("value", parser.getText());
        
        // Second field
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("intField", parser.getCurrentName());
        
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals("intField", parser.getCurrentName());
        assertEquals(42, parser.getIntValue());
        
        // End object
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.getCurrentName());
        
        // End of stream
        assertNull(parser.nextToken());
        
        parser.close();
    }

    @Test
    public void testArrayNodeParsing() throws IOException {
        ArrayNode root = objectMapper.createArrayNode();
        root.add("item1");
        root.add(100);

        TreeTraversingParser parser = new TreeTraversingParser(root, objectMapper);

        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("item1", parser.getText());
        
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(100, parser.getIntValue());
        
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testSkipChildren() throws IOException {
        ObjectNode root = objectMapper.createObjectNode();
        ObjectNode childObj = objectMapper.createObjectNode();
        childObj.put("subField", "subValue");
        root.set("child", childObj);
        root.put("afterChild", true);

        TreeTraversingParser parser = new TreeTraversingParser(root, objectMapper);
        
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("child", parser.getCurrentName());
        
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        
        // Skip children of 'child'
        parser.skipChildren();
        
        // Next should be field "afterChild"
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("afterChild", parser.getCurrentName());
        
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        
        parser.close();
    }

    @Test
    public void testMetadataAndConfigMethods() throws IOException {
        TextNode node = new TextNode("test");
        TreeTraversingParser parser = new TreeTraversingParser(node, objectMapper);

        assertNotNull(parser.getCodec());
        assertNotNull(parser.getVersion());
        assertNotNull(parser.getParsingContext());
        
        // Location methods
        assertNotNull(parser.getTokenLocation());
        assertNotNull(parser.getCurrentLocation());

        parser.nextToken();
        assertEquals(4, parser.getTokenCharacterOffset());
        assertEquals(4, parser.getTokenLength());

        parser.close();
        assertTrue(parser.hasCurrentToken());
    }

    @Test(expected = IOException.class)
    public void testInvalidNumberConversionThrowsException() throws IOException {
        TextNode node = new TextNode("not-a-number");
        TreeTraversingParser parser = new TreeTraversingParser(node, objectMapper);
        parser.nextToken();
        // Should throw NumberFormatException wrapped or handled as IOException/UnsupportedOperationException depending on implementation
        parser.getIntValue();
    }

    @Test
    public void testOverrideCodecAndSetCodec() {
        TextNode node = new TextNode("test");
        TreeTraversingParser parser = new TreeTraversingParser(node, objectMapper);
        
        ObjectMapper newCodec = new ObjectMapper();
        parser.setCodec(newCodec);
        assertEquals(newCodec, parser.getCodec());
    }

    @Test
    public void testGetNumberValue() throws IOException {
        TreeTraversingParser parser1 = new TreeTraversingParser(new IntNode(5), objectMapper);
        parser1.nextToken();
        assertEquals(5, parser1.getNumberValue().intValue());
        parser1.close();

        TreeTraversingParser parser2 = new TreeTraversingParser(new FloatNode(5.5f), objectMapper);
        parser2.nextToken();
        assertEquals(5.5f, parser2.getNumberValue().floatValue(), 0.0);
        parser2.close();
    }

    @Test
    public void testEmptyContainerParsing() throws IOException {
        ObjectNode emptyObj = objectMapper.createObjectNode();
        TreeTraversingParser p1 = new TreeTraversingParser(emptyObj, objectMapper);
        assertEquals(JsonToken.START_OBJECT, p1.nextToken());
        assertEquals(JsonToken.END_OBJECT, p1.nextToken());
        assertNull(p1.nextToken());
        p1.close();

        ArrayNode emptyArr = objectMapper.createArrayNode();
        TreeTraversingParser p2 = new TreeTraversingParser(emptyArr, objectMapper);
        assertEquals(JsonToken.START_ARRAY, p2.nextToken());
        assertEquals(JsonToken.END_ARRAY, p2.nextToken());
        assertNull(p2.nextToken());
        p2.close();
    }
}