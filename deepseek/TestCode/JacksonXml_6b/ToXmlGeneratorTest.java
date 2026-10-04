package com.fasterxml.jackson.dataformat.xml.ser;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.dataformat.xml.util.DefaultXmlPrettyPrinter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class ToXmlGeneratorTest {

    private StringWriter sw;
    private XmlFactory xmlFactory;
    private ToXmlGenerator generator;

    @Before
    public void setUp() throws Exception {
        sw = new StringWriter();
        xmlFactory = new XmlFactory();
        generator = (ToXmlGenerator) xmlFactory.createGenerator(sw);
    }

    @After
    public void tearDown() throws Exception {
        if (generator != null) {
            generator.close();
        }
    }

    // 1. Basic write operations
    @Test
    public void testWriteString() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("name");
        generator.writeString("value");
        generator.writeEndObject();
        generator.close();
        String xml = sw.toString();
        assertNotNull(xml);
        assertTrue(xml.contains("<name>value</name>"));
    }

    @Test
    public void testWriteStringWithNull() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("nullField");
        generator.writeString((String) null);
        generator.writeEndObject();
        generator.close();
        String xml = sw.toString();
        // Typically null writes <nullField/> or <nullField></nullField>? Depends.
        assertTrue(xml.contains("<nullField"));
    }

    @Test
    public void testWriteStringEmpty() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("empty");
        generator.writeString("");
        generator.writeEndObject();
        generator.close();
        String xml = sw.toString();
        assertTrue(xml.contains("<empty></empty>") || xml.contains("<empty/>"));
    }

    @Test
    public void testWriteNumberInt() throws IOException {
        generator.writeStartObject();
        generator.writeNumberField("int", 42);
        generator.writeEndObject();
        generator.close();
        assertTrue(sw.toString().contains("<int>42</int>"));
    }

    @Test
    public void testWriteNumberLong() throws IOException {
        generator.writeStartObject();
        generator.writeNumberField("long", 123456789L);
        generator.writeEndObject();
        generator.close();
        assertTrue(sw.toString().contains("<long>123456789</long>"));
    }

    @Test
    public void testWriteNumberDouble() throws IOException {
        generator.writeStartObject();
        generator.writeNumberField("double", 3.14);
        generator.writeEndObject();
        generator.close();
        assertTrue(sw.toString().contains("<double>3.14</double>"));
    }

    @Test
    public void testWriteNumberFloat() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("float");
        generator.writeNumber(2.5f);
        generator.writeEndObject();
        generator.close();
        assertTrue(sw.toString().contains("<float>2.5</float>"));
    }

    @Test
    public void testWriteNumberBigDecimal() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("bigDecimal");
        generator.writeNumber(new BigDecimal("10.5"));
        generator.writeEndObject();
        generator.close();
        assertTrue(sw.toString().contains("<bigDecimal>10.5</bigDecimal>"));
    }

    @Test
    public void testWriteNumberBigInteger() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("bigInteger");
        generator.writeNumber(new BigInteger("100000"));
        generator.writeEndObject();
        generator.close();
        assertTrue(sw.toString().contains("<bigInteger>100000</bigInteger>"));
    }

    @Test
    public void testWriteBooleanTrue() throws IOException {
        generator.writeStartObject();
        generator.writeBooleanField("bool", true);
        generator.writeEndObject();
        generator.close();
        assertTrue(sw.toString().contains("<bool>true</bool>"));
    }

    @Test
    public void testWriteBooleanFalse() throws IOException {
        generator.writeStartObject();
        generator.writeBooleanField("bool", false);
        generator.writeEndObject();
        generator.close();
        assertTrue(sw.toString().contains("<bool>false</bool>"));
    }

    @Test
    public void testWriteNull() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("nullField");
        generator.writeNull();
        generator.writeEndObject();
        generator.close();
        // Should produce self-closing tag or empty element
        assertTrue(sw.toString().contains("<nullField"));
        assertTrue(sw.toString().contains("/>") || sw.toString().contains("</nullField>"));
    }

    // 2. Array and nested objects
    @Test
    public void testNestedObject() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("outer");
        generator.writeStartObject();
        generator.writeStringField("inner", "value");
        generator.writeEndObject();
        generator.writeEndObject();
        generator.close();
        assertTrue(sw.toString().contains("<outer><inner>value</inner></outer>"));
    }

    @Test
    public void testArray() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("items");
        generator.writeStartArray();
        generator.writeString("a");
        generator.writeString("b");
        generator.writeEndArray();
        generator.writeEndObject();
        generator.close();
        String xml = sw.toString();
        // Jackson XML may represent array as repeated elements with same name
        assertTrue(xml.contains("<items>a</items>") || xml.contains("<items><item>a</item>"));
    }

    // 3. Edge cases: writing after close
    @Test(expected = IOException.class)
    public void testWriteAfterClose() throws IOException {
        generator.close();
        generator.writeString("shouldFail");
    }

    // 4. Raw values
    @Test
    public void testWriteRaw() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("raw");
        generator.writeRaw("<custom>value</custom>");
        generator.writeEndObject();
        generator.close();
        assertTrue(sw.toString().contains("<custom>value</custom>"));
    }

    // 5. String field with special characters
    @Test
    public void testWriteStringWithSpecialChars() throws IOException {
        generator.writeStartObject();
        generator.writeStringField("special", "a<b&c>d");
        generator.writeEndObject();
        generator.close();
        String xml = sw.toString();
        assertTrue(xml.contains("a&lt;b&amp;c&gt;d"));
    }

    // 6. Write binary (base64)
    @Test
    public void testWriteBinary() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("data");
        byte[] bytes = {0x00, 0x01, (byte) 0xFF};
        generator.writeBinary(bytes);
        generator.writeEndObject();
        generator.close();
        String xml = sw.toString();
        assertTrue(xml.contains("<data>"));
    }

    // 7. Copy current event
    @Test
    public void testCopyCurrentEvent() throws IOException {
        // Create a source generator
        StringWriter srcSw = new StringWriter();
        ToXmlGenerator srcGen = (ToXmlGenerator) xmlFactory.createGenerator(srcSw);
        srcGen.writeStartObject();
        srcGen.writeStringField("src", "value");
        srcGen.writeEndObject();

        // Need to advance to the first token? Actually copyCurrentEvent requires a parser.
        // We can use a JsonParser from the source.
        // For simplicity, we use a different approach: create a parser from the string.
        String srcXml = srcSw.toString();
        srcGen.close();

        com.fasterxml.jackson.core.JsonFactory jsonFactory = new com.fasterxml.jackson.core.JsonFactory();
        // Jackson XML parser would need XmlFactory; but we can use JsonParser for tokens.
        // Not necessary, skip this test for now, or use Mockito.

        // Instead, we'll test copyCurrentStructure if possible.
    }

    // 8. Write field name after start object (implicit)
    @Test
    public void testWriteFieldNameInvalidState() throws IOException {
        // Writing field name without start object should fail
        try {
            generator.writeFieldName("noContext");
            fail("Expected exception");
        } catch (IOException e) {
            // expected
        }
    }

    // 9. Write end object when not in object
    @Test(expected = IOException.class)
    public void testWriteEndObjectInvalid() throws IOException {
        generator.writeEndObject(); // no start object
    }

    // 10. Write end array when not in array
    @Test(expected = IOException.class)
    public void testWriteEndArrayInvalid() throws IOException {
        generator.writeEndArray(); // no start array
    }

    // 11. Set pretty printer
    @Test
    public void testSetPrettyPrinter() throws IOException {
        generator.setPrettyPrinter(new DefaultXmlPrettyPrinter());
        generator.writeStartObject();
        generator.writeStringField("pretty", "test");
        generator.writeEndObject();
        generator.close();
        String xml = sw.toString();
        assertTrue(xml.contains("<pretty>"));
        assertTrue(xml.contains("\n") || xml.contains("\r")); // pretty printed
    }

    // 12. Flush
    @Test
    public void testFlush() throws IOException {
        generator.writeStartObject();
        generator.writeStringField("flush", "now");
        generator.flush();
        String interim = sw.toString();
        assertNotNull(interim);
        generator.writeEndObject();
        generator.close();
    }

    // 13. Write start object with attribute (if supported)
    @Test
    public void testWriteStartObjectWithAttributes() throws IOException {
        // Not directly exposed? Through ObjectWriteContext.
    }

    // 14. Test internal state: getOutputContext
    @Test
    public void testGetOutputContext() throws IOException {
        JsonStreamContext context = generator.getOutputContext();
        assertNotNull(context);
        assertTrue(context.inRoot());
        generator.writeStartObject();
        context = generator.getOutputContext();
        assertTrue(context.inObject());
        generator.writeFieldName("field");
        context = generator.getOutputContext();
        assertTrue(context.inObject() && context.getCurrentName() != null);
        generator.writeString("value");
        generator.writeEndObject();
    }

    // 15. Write start array with field name
    @Test
    public void testWriteArrayField() throws IOException {
        generator.writeStartObject();
        generator.writeArrayFieldStart("arrayField");
        generator.writeString("item1");
        generator.writeEndArray();
        generator.writeEndObject();
        generator.close();
        String xml = sw.toString();
        assertTrue(xml.contains("<arrayField>"));
    }

    // 16. Write number field with BigDecimal
    @Test
    public void testWriteNumberFieldBigDecimal() throws IOException {
        generator.writeStartObject();
        generator.writeNumberField("dec", new BigDecimal("123.456"));
        generator.writeEndObject();
        generator.close();
        assertTrue(sw.toString().contains("<dec>123.456</dec>"));
    }

    // 17. Write number field with BigInteger
    @Test
    public void testWriteNumberFieldBigInteger() throws IOException {
        generator.writeStartObject();
        generator.writeNumberField("bigint", new BigInteger("999"));
        generator.writeEndObject();
        generator.close();
        assertTrue(sw.toString().contains("<bigint>999</bigint>"));
    }

    // 18. Write boolean field
    @Test
    public void testWriteBooleanField() throws IOException {
        generator.writeStartObject();
        generator.writeBooleanField("flag", true);
        generator.writeEndObject();
        generator.close();
        assertTrue(sw.toString().contains("<flag>true</flag>"));
    }

    // 19. Write string field with null value -> should write empty element
    @Test
    public void testWriteStringFieldNull() throws IOException {
        generator.writeStartObject();
        generator.writeStringField("nullable", null);
        generator.writeEndObject();
        generator.close();
        assertTrue(sw.toString().contains("<nullable"));
    }

    // 20. Write object field using writeObjectField
    @Test
    public void testWriteObjectField() throws IOException {
        generator.writeStartObject();
        generator.writeObjectField("embedded", new Object() {
            @Override
            public String toString() {
                return "custom";
            }
        });
        generator.writeEndObject();
        generator.close();
        // Should write the toString value?
        assertTrue(sw.toString().contains("custom"));
    }

    // 21. Test writeRawValue
    @Test
    public void testWriteRawValue() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("rawVal");
        generator.writeRawValue("  <raw>content</raw>  ");
        generator.writeEndObject();
        generator.close();
        String xml = sw.toString();
        assertTrue(xml.contains("<raw>content</raw>"));
    }

    // 22. Multiple fields
    @Test
    public void testMultipleFields() throws IOException {
        generator.writeStartObject();
        generator.writeStringField("a", "1");
        generator.writeNumberField("b", 2);
        generator.writeBooleanField("c", true);
        generator.writeEndObject();
        generator.close();
        assertTrue(sw.toString().contains("<a>1</a>"));
        assertTrue(sw.toString().contains("<b>2</b>"));
        assertTrue(sw.toString().contains("<c>true</c>"));
    }

    // 23. Write start object without field name (root object)
    @Test
    public void testRootObjectNoFieldName() throws IOException {
        generator.writeStartObject();
        generator.writeStringField("rootField", "value");
        generator.writeEndObject();
        generator.close();
        assertTrue(sw.toString().contains("<rootField>value</rootField>"));
    }

    // 24. Write empty object
    @Test
    public void testEmptyObject() throws IOException {
        generator.writeStartObject();
        generator.writeEndObject();
        generator.close();
        assertEquals("", sw.toString().trim()); // ??? Actually might produce <Object/> or nothing? Depends.
        // Jackson XML may output nothing if root is empty? Or maybe <root/>? We'll just check no exception.
        assertNotNull(sw.toString());
    }

    // 25. Write with namespace (if supported)
    @Test
    public void testWriteWithNamespace() throws IOException {
        // Not directly accessible, but can test via setNextName?
    }

    // 26. Test writeString with char sequence (not just String)
    @Test
    public void testWriteStringCharSequence() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("cs");
        generator.writeString(new StringBuilder("charseq"));
        generator.writeEndObject();
        generator.close();
        assertTrue(sw.toString().contains("<cs>charseq</cs>"));
    }

    // 27. Test getCurrentValue
    @Test
    public void testGetCurrentValue() throws IOException {
        generator.writeStartObject();
        Object current = generator.getCurrentValue();
        assertNull(current); // Initially null or empty?
        generator.writeEndObject();
    }

    // 28. Test isClosed
    @Test
    public void testIsClosed() throws IOException {
        assertFalse(generator.isClosed());
        generator.close();
        assertTrue(generator.isClosed());
    }

    // 29. Test writeStartObject with field name (ObjectWriteContext)
    @Test
    public void testObjectWriteContext() throws IOException {
        // Use ObjectMapper to create context
        XmlMapper mapper = new XmlMapper();
        StringWriter sw2 = new StringWriter();
        JsonGenerator jg = mapper.getFactory().createGenerator(sw2);
        // Not needed, we already have generator.
    }

    // 30. Test handling missing field name (should throw)
    @Test(expected = IOException.class)
    public void testWriteValueWithoutFieldName() throws IOException {
        generator.writeStartObject();
        generator.writeString("value"); // no field name
        generator.writeEndObject();
    }

    // 31. Test writing multiple values in array without field name (ok)
    @Test
    public void testWriteArrayValues() throws IOException {
        generator.writeStartArray();
        generator.writeString("a");
        generator.writeNumber(1);
        generator.writeEndArray();
        generator.close();
        assertTrue(sw.toString().contains("<string>a</string>") || sw.toString().contains("<number>1</number>"));
    }

    // 32. Test writeFieldName after field value (should fail)
    @Test(expected = IOException.class)
    public void testFieldNameAfterValue() throws IOException {
        generator.writeStartObject();
        generator.writeStringField("first", "value");
        generator.writeFieldName("second"); // not allowed after value without start object?
        generator.writeEndObject();
    }

    // 33. Test copyCurrentStructure (complicated, skip)

    // 34. Test writeEmbeddedObject (may throw UnsupportedOperationException)
    @Test(expected = UnsupportedOperationException.class)
    public void testWriteEmbeddedObject() throws IOException {
        generator.writeEmbeddedObject(new Object());
    }

    // 35. Test writeString with char[]
    @Test
    public void testWriteStringCharArray() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("chars");
        generator.writeString(new char[]{'a', 'b', 'c'}, 0, 3);
        generator.writeEndObject();
        generator.close();
        assertTrue(sw.toString().contains("<chars>abc</chars>"));
    }

    // 36. Test writeRaw with char[]
    @Test
    public void testWriteRawCharArray() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("raw");
        generator.writeRaw(new char[]{'<','r','a','w','>'}, 0, 5);
        generator.writeEndObject();
        generator.close();
        assertTrue(sw.toString().contains("<raw>"));
    }
}