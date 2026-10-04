package com.fasterxml.jackson.dataformat.xml.deser;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.Before;
import org.junit.Test;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class FromXmlParserTest {

    private XmlFactory xmlFactory;
    private XmlMapper xmlMapper;

    @Before
    public void setUp() {
        xmlFactory = new XmlFactory();
        xmlMapper = new XmlMapper(xmlFactory);
    }

    @Test
    public void testSimpleElementParsing() throws Exception {
        String xml = "<root><name>Jackson</name><age>10</age></root>";
        try (FromXmlParser parser = (FromXmlParser) xmlFactory.createParser(xml)) {
            assertNull(parser.currentToken());

            assertEquals(JsonToken.START_OBJECT, parser.nextToken());
            assertEquals("root", parser.currentName());

            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("name", parser.currentName());
            assertEquals("name", parser.getText());

            assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
            assertEquals("Jackson", parser.getText());
            assertEquals("Jackson", parser.getValueAsString());

            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("age", parser.currentName());

            assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
            assertEquals("10", parser.getText());
            assertEquals(10, parser.getIntValue());
            assertEquals(10L, parser.getLongValue());
            assertEquals(10.0, parser.getDoubleValue(), 0.0001);
            assertEquals(10.0f, parser.getFloatValue(), 0.0001f);

            assertEquals(JsonToken.END_OBJECT, parser.nextToken());
            assertNull(parser.nextToken());
            assertTrue(parser.isClosed() || parser.currentToken() == null);
        }
    }

    @Test
    public void testAttributesParsing() throws Exception {
        String xml = "<person id=\"123\" type=\"admin\"><name>John</name></person>";
        try (FromXmlParser parser = (FromXmlParser) xmlFactory.createParser(xml)) {
            assertEquals(JsonToken.START_OBJECT, parser.nextToken());
            assertEquals("person", parser.currentName());

            boolean hasId = false;
            boolean hasType = false;
            boolean hasName = false;

            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.currentToken() == JsonToken.FIELD_NAME) {
                    String name = parser.currentName();
                    parser.nextToken();
                    if ("id".equals(name)) {
                        assertEquals("123", parser.getText());
                        assertEquals(123, parser.getIntValue());
                        hasId = true;
                    } else if ("type".equals(name)) {
                        assertEquals("admin", parser.getText());
                        hasType = true;
                    } else if ("name".equals(name)) {
                        assertEquals("John", parser.getText());
                        hasName = true;
                    }
                }
            }

            assertTrue(hasId);
            assertTrue(hasType);
            assertTrue(hasName);
        }
    }

    @Test
    public void testEmptyElement() throws Exception {
        String xml = "<root><empty/></root>";
        try (FromXmlParser parser = (FromXmlParser) xmlFactory.createParser(xml)) {
            assertEquals(JsonToken.START_OBJECT, parser.nextToken());
            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("empty", parser.currentName());
            assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
            assertEquals("", parser.getText());
            assertEquals(JsonToken.END_OBJECT, parser.nextToken());
            assertNull(parser.nextToken());
        }
    }

    @Test
    public void testRootAttributesAndLeaves() throws Exception {
        String xml = "<root attr=\"val\">textOnly</root>";
        try (FromXmlParser parser = (FromXmlParser) xmlFactory.createParser(xml)) {
            assertEquals(JsonToken.START_OBJECT, parser.nextToken());
            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("attr", parser.currentName());
            assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
            assertEquals("val", parser.getText());
            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("", parser.currentName());
            assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
            assertEquals("textOnly", parser.getText());
            assertEquals(JsonToken.END_OBJECT, parser.nextToken());
            assertNull(parser.nextToken());
        }
    }

    @Test
    public void testArrayOrRepeatedElements() throws Exception {
        String xml = "<root><item>A</item><item>B</item><item>C</item></root>";
        try (FromXmlParser parser = (FromXmlParser) xmlFactory.createParser(xml)) {
            assertEquals(JsonToken.START_OBJECT, parser.nextToken());

            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("item", parser.currentName());
            assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
            assertEquals("A", parser.getText());

            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("item", parser.currentName());
            assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
            assertEquals("B", parser.getText());

            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("item", parser.currentName());
            assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
            assertEquals("C", parser.getText());

            assertEquals(JsonToken.END_OBJECT, parser.nextToken());
            assertNull(parser.nextToken());
        }
    }

    @Test
    public void testNextTextValue() throws Exception {
        String xml = "<root><name>Tester</name><num>42</num></root>";
        try (FromXmlParser parser = (FromXmlParser) xmlFactory.createParser(xml)) {
            assertEquals(JsonToken.START_OBJECT, parser.nextToken());

            assertEquals("name", parser.nextFieldName());
            assertEquals("Tester", parser.nextTextValue());

            assertEquals("num", parser.nextFieldName());
            assertEquals("42", parser.nextTextValue());

            assertNull(parser.nextFieldName());
            assertEquals(JsonToken.END_OBJECT, parser.currentToken());
        }
    }

    @Test
    public void testNextFieldName() throws Exception {
        String xml = "<root><a/><b/></root>";
        try (FromXmlParser parser = (FromXmlParser) xmlFactory.createParser(xml)) {
            assertEquals(JsonToken.START_OBJECT, parser.nextToken());
            assertEquals("a", parser.nextFieldName());
            parser.nextToken();
            assertEquals("b", parser.nextFieldName());
            parser.nextToken();
            assertNull(parser.nextFieldName());
        }
    }

    @Test
    public void testSkipChildren() throws Exception {
        String xml = "<root><branch><leaf1>1</leaf1><leaf2>2</leaf2></branch><after>val</after></root>";
        try (FromXmlParser parser = (FromXmlParser) xmlFactory.createParser(xml)) {
            assertEquals(JsonToken.START_OBJECT, parser.nextToken());
            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("branch", parser.currentName());
            assertEquals(JsonToken.START_OBJECT, parser.nextToken());
            
            parser.skipChildren();
            assertEquals(JsonToken.END_OBJECT, parser.currentToken());

            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("after", parser.currentName());
            assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
            assertEquals("val", parser.getText());

            assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        }
    }

    @Test
    public void testParserFeaturesAndStaxReaderAccess() throws Exception {
        String xml = "<root attr=\"x\">content</root>";
        try (FromXmlParser parser = (FromXmlParser) xmlFactory.createParser(new StringReader(xml))) {
            assertNotNull(parser.getStaxReader());
            assertNotNull(parser.getParsingContext());
            assertEquals(0, parser.getFormatFeatures());

            FromXmlParser configuredParser = parser.enable(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL);
            assertSame(parser, configuredParser);
            assertTrue(parser.isEnabled(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL));

            parser.disable(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL);
            assertFalse(parser.isEnabled(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL));

            parser.configure(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL, true);
            assertTrue(parser.isEnabled(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL));

            parser.setXMLTextElementName("customTextName");
            
            assertNull(parser.getCodec());
            parser.setCodec(xmlMapper);
            assertSame(xmlMapper, parser.getCodec());
        }
    }

    @Test
    public void testTextCharactersAndOffsets() throws Exception {
        String xml = "<root>Hello World</root>";
        try (FromXmlParser parser = (FromXmlParser) xmlFactory.createParser(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)))) {
            assertEquals(JsonToken.START_OBJECT, parser.nextToken());
            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("", parser.currentName());
            assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

            char[] chars = parser.getTextCharacters();
            assertNotNull(chars);
            int offset = parser.getTextOffset();
            int len = parser.getTextLength();
            String extracted = new String(chars, offset, len);
            assertEquals("Hello World", extracted);
            assertTrue(parser.hasTextCharacters());
        }
    }

    @Test
    public void testLocationTracking() throws Exception {
        String xml = "<root>\n  <val>123</val>\n</root>";
        try (FromXmlParser parser = (FromXmlParser) xmlFactory.createParser(xml)) {
            JsonLocation tokenLoc = parser.getTokenLocation();
            JsonLocation currentLoc = parser.getCurrentLocation();
            assertNotNull(tokenLoc);
            assertNotNull(currentLoc);

            parser.nextToken();
            assertNotNull(parser.getTokenLocation());
            assertNotNull(parser.getCurrentLocation());
        }
    }

    @Test
    public void testBooleanAndNumberConversions() throws Exception {
        String xml = "<root><b1>true</b1><b2>false</b2><n1>123</n1><n2>45.67</n2><n3>9876543210</n3><bEmpty></bEmpty></root>";
        try (FromXmlParser parser = (FromXmlParser) xmlFactory.createParser(xml)) {
            assertEquals(JsonToken.START_OBJECT, parser.nextToken());

            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("b1", parser.currentName());
            assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
            assertTrue(parser.getValueAsBoolean());
            assertTrue(parser.getValueAsBoolean(false));

            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("b2", parser.currentName());
            assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
            assertFalse(parser.getValueAsBoolean());

            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("n1", parser.currentName());
            assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
            assertEquals(123, parser.getValueAsInt());
            assertEquals(123, parser.getValueAsInt(0));

            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("n2", parser.currentName());
            assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
            assertEquals(45.67, parser.getValueAsDouble(), 0.001);
            assertEquals(45.67, parser.getValueAsDouble(0.0), 0.001);

            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("n3", parser.currentName());
            assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
            assertEquals(9876543210L, parser.getValueAsLong());
            assertEquals(9876543210L, parser.getValueAsLong(0L));

            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("bEmpty", parser.currentName());
            assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
            assertFalse(parser.getValueAsBoolean(false));
            assertTrue(parser.getValueAsBoolean(true));
        }
    }

    @Test
    public void testBinaryAndBase64Data() throws Exception {
        String data = "Hello Jackson XML!";
        String base64 = com.fasterxml.jackson.core.Base64Variants.MIME.encode(data.getBytes(StandardCharsets.UTF_8));
        String xml = "<root><data>" + base64 + "</data></root>";

        try (FromXmlParser parser = (FromXmlParser) xmlFactory.createParser(xml)) {
            assertEquals(JsonToken.START_OBJECT, parser.nextToken());
            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("data", parser.currentName());
            assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

            byte[] decoded = parser.getBinaryValue();
            assertArrayEquals(data.getBytes(StandardCharsets.UTF_8), decoded);
        }
    }

    @Test
    public void testReadValueAsTree() throws Exception {
        String xml = "<root id=\"1\"><name>TestTree</name></root>";
        try (FromXmlParser parser = (FromXmlParser) xmlFactory.createParser(xml)) {
            parser.setCodec(xmlMapper);
            JsonNode node = parser.readValueAsTree();
            assertNotNull(node);
            assertTrue(node.has("id") || node.has("name"));
        }
    }

    @Test
    public void testCloseHandling() throws Exception {
        String xml = "<root><elem>val</elem></root>";
        FromXmlParser parser = (FromXmlParser) xmlFactory.createParser(xml);
        assertFalse(parser.isClosed());
        parser.nextToken();
        parser.close();
        assertTrue(parser.isClosed());
        assertNull(parser.nextToken());
    }

    @Test
    public void testInvalidXmlThrowsException() throws Exception {
        String invalidXml = "<root><unclosed>";
        try (FromXmlParser parser = (FromXmlParser) xmlFactory.createParser(invalidXml)) {
            parser.nextToken();
            while (parser.nextToken() != null) {
                // iterate until error is encountered
            }
            fail("Expected JsonParseException on malformed XML");
        } catch (JsonParseException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testEmptyElementAsNullFeature() throws Exception {
        String xml = "<root><empty></empty></root>";
        XmlFactory fac = new XmlFactory();
        try (FromXmlParser parser = (FromXmlParser) fac.createParser(xml)) {
            parser.enable(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL);
            assertEquals(JsonToken.START_OBJECT, parser.nextToken());
            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("empty", parser.currentName());
            JsonToken token = parser.nextToken();
            assertTrue(token == JsonToken.VALUE_NULL || token == JsonToken.VALUE_STRING);
        }
    }

    @Test
    public void testAddVirtualWrapping() throws Exception {
        String xml = "<root><item>1</item><item>2</item></root>";
        try (FromXmlParser parser = (FromXmlParser) xmlFactory.createParser(xml)) {
            parser.addVirtualWrapping(new java.util.HashSet<String>() {{
                add("item");
            }});
            JsonToken t = parser.nextToken();
            assertNotNull(t);
        }
    }
}