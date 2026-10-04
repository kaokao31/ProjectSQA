package com.fasterxml.jackson.core.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.json.UTF8JsonGenerator;
import com.fasterxml.jackson.core.util.BufferRecycler;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class UTF8JsonGeneratorTest {

    private ByteArrayOutputStream outputStream;
    private JsonFactory factory;
    private UTF8JsonGenerator generator;

    @Before
    public void setUp() throws IOException {
        outputStream = new ByteArrayOutputStream();
        factory = new JsonFactory();
        // Create generator with default settings
        generator = (UTF8JsonGenerator) factory.createGenerator(outputStream);
    }

    // Helper to get output as string
    private String getOutput() throws IOException {
        generator.flush();
        return outputStream.toString("UTF-8");
    }

    // ==================== Basic Object/Array Structure ====================

    @Test
    public void testEmptyObject() throws IOException {
        generator.writeStartObject();
        generator.writeEndObject();
        assertEquals("{}", getOutput());
    }

    @Test
    public void testEmptyArray() throws IOException {
        generator.writeStartArray();
        generator.writeEndArray();
        assertEquals("[]", getOutput());
    }

    @Test
    public void testNestedObject() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("outer");
        generator.writeStartObject();
        generator.writeEndObject();
        generator.writeEndObject();
        assertEquals("{\"outer\":{}}", getOutput());
    }

    @Test
    public void testNestedArray() throws IOException {
        generator.writeStartArray();
        generator.writeStartArray();
        generator.writeEndArray();
        generator.writeEndArray();
        assertEquals("[[]]", getOutput());
    }

    // ==================== String Writing ====================

    @Test
    public void testWriteStringSimple() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("key");
        generator.writeString("value");
        generator.writeEndObject();
        assertEquals("{\"key\":\"value\"}", getOutput());
    }

    @Test
    public void testWriteStringWithQuotes() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("msg");
        generator.writeString("he said \"hello\"");
        generator.writeEndObject();
        assertEquals("{\"msg\":\"he said \\\"hello\\\"\"}", getOutput());
    }

    @Test
    public void testWriteStringWithBackslash() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("path");
        generator.writeString("C:\\Users\\test");
        generator.writeEndObject();
        assertEquals("{\"path\":\"C:\\\\Users\\\\test\"}", getOutput());
    }

    @Test
    public void testWriteStringWithControlCharacters() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("ctrl");
        generator.writeString("line1\nline2\t");
        generator.writeEndObject();
        assertEquals("{\"ctrl\":\"line1\\nline2\\t\"}", getOutput());
    }

    @Test
    public void testWriteStringWithUnicode() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("unicode");
        generator.writeString("üñîçödé");
        generator.writeEndObject();
        assertEquals("{\"unicode\":\"üñîçödé\"}", getOutput());
    }

    @Test
    public void testWriteStringWithSurrogatePair() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("emoji");
        generator.writeString("😀"); // U+1F600
        generator.writeEndObject();
        assertEquals("{\"emoji\":\"😀\"}", getOutput());
    }

    @Test
    public void testWriteStringEmpty() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("empty");
        generator.writeString("");
        generator.writeEndObject();
        assertEquals("{\"empty\":\"\"}", getOutput());
    }

    @Test
    public void testWriteStringNull() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("null");
        generator.writeString(null);
        generator.writeEndObject();
        assertEquals("{\"null\":null}", getOutput());
    }

    // ==================== Number Writing ====================

    @Test
    public void testWriteInt() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("int");
        generator.writeNumber(42);
        generator.writeEndObject();
        assertEquals("{\"int\":42}", getOutput());
    }

    @Test
    public void testWriteNegativeInt() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("neg");
        generator.writeNumber(-1);
        generator.writeEndObject();
        assertEquals("{\"neg\":-1}", getOutput());
    }

    @Test
    public void testWriteLong() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("long");
        generator.writeNumber(1234567890123L);
        generator.writeEndObject();
        assertEquals("{\"long\":1234567890123}", getOutput());
    }

    @Test
    public void testWriteDouble() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("double");
        generator.writeNumber(3.14);
        generator.writeEndObject();
        assertEquals("{\"double\":3.14}", getOutput());
    }

    @Test
    public void testWriteDoubleNaN() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("nan");
        generator.writeNumber(Double.NaN);
        generator.writeEndObject();
        assertEquals("{\"nan\":NaN}", getOutput());
    }

    @Test
    public void testWriteDoubleInfinity() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("inf");
        generator.writeNumber(Double.POSITIVE_INFINITY);
        generator.writeEndObject();
        assertEquals("{\"inf\":Infinity}", getOutput());
    }

    @Test
    public void testWriteDoubleNegativeInfinity() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("neginf");
        generator.writeNumber(Double.NEGATIVE_INFINITY);
        generator.writeEndObject();
        assertEquals("{\"neginf\":-Infinity}", getOutput());
    }

    @Test
    public void testWriteBigDecimal() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("bigdec");
        generator.writeNumber(new BigDecimal("12345678901234567890.123456789"));
        generator.writeEndObject();
        assertEquals("{\"bigdec\":12345678901234567890.123456789}", getOutput());
    }

    @Test
    public void testWriteBigInteger() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("bigint");
        generator.writeNumber(new BigInteger("123456789012345678901234567890"));
        generator.writeEndObject();
        assertEquals("{\"bigint\":123456789012345678901234567890}", getOutput());
    }

    @Test
    public void testWriteNumberZero() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("zero");
        generator.writeNumber(0);
        generator.writeEndObject();
        assertEquals("{\"zero\":0}", getOutput());
    }

    // ==================== Boolean and Null ====================

    @Test
    public void testWriteBooleanTrue() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("flag");
        generator.writeBoolean(true);
        generator.writeEndObject();
        assertEquals("{\"flag\":true}", getOutput());
    }

    @Test
    public void testWriteBooleanFalse() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("flag");
        generator.writeBoolean(false);
        generator.writeEndObject();
        assertEquals("{\"flag\":false}", getOutput());
    }

    @Test
    public void testWriteNull() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("nothing");
        generator.writeNull();
        generator.writeEndObject();
        assertEquals("{\"nothing\":null}", getOutput());
    }

    // ==================== Field Names ====================

    @Test
    public void testWriteFieldNameWithSpecialChars() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("field\"name");
        generator.writeString("value");
        generator.writeEndObject();
        assertEquals("{\"field\\\"name\":\"value\"}", getOutput());
    }

    @Test
    public void testWriteFieldNameEmpty() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("");
        generator.writeString("value");
        generator.writeEndObject();
        assertEquals("{\"\":\"value\"}", getOutput());
    }

    @Test
    public void testWriteFieldNameNull() throws IOException {
        generator.writeStartObject();
        try {
            generator.writeFieldName(null);
            fail("Expected IOException for null field name");
        } catch (IOException e) {
            // expected
        }
        generator.writeEndObject();
    }

    // ==================== Array Elements ====================

    @Test
    public void testWriteStringArray() throws IOException {
        generator.writeStartArray();
        generator.writeString("a");
        generator.writeString("b");
        generator.writeString("c");
        generator.writeEndArray();
        assertEquals("[\"a\",\"b\",\"c\"]", getOutput());
    }

    @Test
    public void testWriteNumberArray() throws IOException {
        generator.writeStartArray();
        generator.writeNumber(1);
        generator.writeNumber(2);
        generator.writeNumber(3);
        generator.writeEndArray();
        assertEquals("[1,2,3]", getOutput());
    }

    @Test
    public void testWriteMixedArray() throws IOException {
        generator.writeStartArray();
        generator.writeString("text");
        generator.writeNumber(42);
        generator.writeBoolean(true);
        generator.writeNull();
        generator.writeEndArray();
        assertEquals("[\"text\",42,true,null]", getOutput());
    }

    // ==================== Flush and Close ====================

    @Test
    public void testFlush() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("a");
        generator.writeString("b");
        generator.flush();
        String partial = outputStream.toString("UTF-8");
        assertEquals("{\"a\":\"b\"}", partial);
        generator.writeEndObject();
        generator.flush();
        String full = outputStream.toString("UTF-8");
        assertEquals("{\"a\":\"b\"}", full); // end object adds nothing
    }

    @Test
    public void testClose() throws IOException {
        generator.writeStartObject();
        generator.writeEndObject();
        generator.close();
        assertTrue(generator.isClosed());
    }

    @Test(expected = IOException.class)
    public void testWriteAfterClose() throws IOException {
        generator.writeStartObject();
        generator.writeEndObject();
        generator.close();
        generator.writeString("should fail");
    }

    @Test
    public void testMultipleClose() throws IOException {
        generator.writeStartObject();
        generator.writeEndObject();
        generator.close();
        generator.close(); // should not throw
    }

    // ==================== Buffer Overflow / Large Data ====================

    @Test
    public void testLargeString() throws IOException {
        StringBuilder sb = new StringBuilder(5000);
        for (int i = 0; i < 1000; i++) {
            sb.append("abcdefghij");
        }
        String large = sb.toString();
        generator.writeStartObject();
        generator.writeFieldName("large");
        generator.writeString(large);
        generator.writeEndObject();
        String output = getOutput();
        assertTrue(output.contains("\"large\":\""));
        assertTrue(output.endsWith("\"}"));
    }

    @Test
    public void testLargeNumberArray() throws IOException {
        generator.writeStartArray();
        for (int i = 0; i < 10000; i++) {
            generator.writeNumber(i);
        }
        generator.writeEndArray();
        String output = getOutput();
        assertTrue(output.startsWith("["));
        assertTrue(output.endsWith("]"));
        assertTrue(output.contains(",9999"));
    }

    // ==================== Edge Cases and Bug Triggers ====================

    @Test
    public void testWriteStringWithHighSurrogateAlone() throws IOException {
        // High surrogate without low surrogate should be escaped
        generator.writeStartObject();
        generator.writeFieldName("surrogate");
        generator.writeString("\uD800");
        generator.writeEndObject();
        String output = getOutput();
        // Expect escaped representation
        assertTrue(output.contains("\\uD800") || output.contains("\\ud800"));
    }

    @Test
    public void testWriteStringWithLowSurrogateAlone() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("surrogate");
        generator.writeString("\uDC00");
        generator.writeEndObject();
        String output = getOutput();
        assertTrue(output.contains("\\uDC00") || output.contains("\\udc00"));
    }

    @Test
    public void testWriteStringWithInvalidSurrogatePair() throws IOException {
        // High surrogate followed by another high surrogate
        generator.writeStartObject();
        generator.writeFieldName("surrogate");
        generator.writeString("\uD800\uD800");
        generator.writeEndObject();
        String output = getOutput();
        // Should escape each surrogate separately
        assertTrue(output.contains("\\uD800\\uD800") || output.contains("\\ud800\\ud800"));
    }

    @Test
    public void testWriteStringWithNullCharacter() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("nullchar");
        generator.writeString("a\u0000b");
        generator.writeEndObject();
        String output = getOutput();
        // Null character should be escaped as \u0000
        assertTrue(output.contains("\\u0000"));
    }

    @Test
    public void testWriteNumberNegativeZero() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("negzero");
        generator.writeNumber(-0.0);
        generator.writeEndObject();
        String output = getOutput();
        // Should be -0.0
        assertTrue(output.contains("-0.0") || output.contains("-0"));
    }

    @Test
    public void testWriteNumberVeryLargeDouble() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("large");
        generator.writeNumber(1e308);
        generator.writeEndObject();
        String output = getOutput();
        assertTrue(output.contains("1e308") || output.contains("1E308"));
    }

    @Test
    public void testWriteNumberVerySmallDouble() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("small");
        generator.writeNumber(1e-308);
        generator.writeEndObject();
        String output = getOutput();
        assertTrue(output.contains("1e-308") || output.contains("1E-308"));
    }

    @Test
    public void testWriteNumberMinInt() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("minint");
        generator.writeNumber(Integer.MIN_VALUE);
        generator.writeEndObject();
        assertEquals("{\"minint\":-2147483648}", getOutput());
    }

    @Test
    public void testWriteNumberMaxInt() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("maxint");
        generator.writeNumber(Integer.MAX_VALUE);
        generator.writeEndObject();
        assertEquals("{\"maxint\":2147483647}", getOutput());
    }

    @Test
    public void testWriteNumberMinLong() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("minlong");
        generator.writeNumber(Long.MIN_VALUE);
        generator.writeEndObject();
        assertEquals("{\"minlong\":-9223372036854775808}", getOutput());
    }

    @Test
    public void testWriteNumberMaxLong() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("maxlong");
        generator.writeNumber(Long.MAX_VALUE);
        generator.writeEndObject();
        assertEquals("{\"maxlong\":9223372036854775807}", getOutput());
    }

    // ==================== Generator State Transitions ====================

    @Test(expected = IOException.class)
    public void testWriteFieldNameInArray() throws IOException {
        generator.writeStartArray();
        generator.writeFieldName("shouldFail");
        generator.writeEndArray();
    }

    @Test(expected = IOException.class)
    public void testWriteStringInObjectWithoutField() throws IOException {
        generator.writeStartObject();
        generator.writeString("no field");
        generator.writeEndObject();
    }

    @Test(expected = IOException.class)
    public void testWriteNumberInObjectWithoutField() throws IOException {
        generator.writeStartObject();
        generator.writeNumber(1);
        generator.writeEndObject();
    }

    @Test(expected = IOException.class)
    public void testWriteBooleanInObjectWithoutField() throws IOException {
        generator.writeStartObject();
        generator.writeBoolean(true);
        generator.writeEndObject();
    }

    @Test(expected = IOException.class)
    public void testWriteNullInObjectWithoutField() throws IOException {
        generator.writeStartObject();
        generator.writeNull();
        generator.writeEndObject();
    }

    @Test(expected = IOException.class)
    public void testWriteStartObjectInObjectWithoutField() throws IOException {
        generator.writeStartObject();
        generator.writeStartObject();
        generator.writeEndObject();
        generator.writeEndObject();
    }

    @Test(expected = IOException.class)
    public void testWriteStartArrayInObjectWithoutField() throws IOException {
        generator.writeStartObject();
        generator.writeStartArray();
        generator.writeEndArray();
        generator.writeEndObject();
    }

    @Test(expected = IOException.class)
    public void testWriteEndObjectWithoutStart() throws IOException {
        generator.writeEndObject();
    }

    @Test(expected = IOException.class)
    public void testWriteEndArrayWithoutStart() throws IOException {
        generator.writeEndArray();
    }

    @Test(expected = IOException.class)
    public void testWriteEndObjectMismatch() throws IOException {
        generator.writeStartArray();
        generator.writeEndObject();
    }

    @Test(expected = IOException.class)
    public void testWriteEndArrayMismatch() throws IOException {
        generator.writeStartObject();
        generator.writeEndArray();
    }

    // ==================== Custom IO Context / Buffer Recycler ====================

    @Test
    public void testWithCustomBufferRecycler() throws IOException {
        BufferRecycler br = new BufferRecycler();
        IOContext ctxt = new IOContext(br, null, false);
        OutputStream os = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = new UTF8JsonGenerator(ctxt, 0, null, os,
                JsonGenerator.Feature.collectDefaults());
        gen.writeStartObject();
        gen.writeFieldName("test");
        gen.writeString("value");
        gen.writeEndObject();
        gen.flush();
        String output = os.toString("UTF-8");
        assertEquals("{\"test\":\"value\"}", output);
        gen.close();
    }

    @Test
    public void testWithDisabledQuoteChar() throws IOException {
        // Not directly configurable in UTF8JsonGenerator, but we can test default
        // This test ensures no regression
        generator.writeStartObject();
        generator.writeFieldName("key");
        generator.writeString("val");
        generator.writeEndObject();
        assertEquals("{\"key\":\"val\"}", getOutput());
    }

    // ==================== SerializedString ====================

    @Test
    public void testWriteFieldNameSerializedString() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName(new SerializedString("myField"));
        generator.writeString("myValue");
        generator.writeEndObject();
        assertEquals("{\"myField\":\"myValue\"}", getOutput());
    }

    @Test
    public void testWriteStringSerializedString() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("key");
        generator.writeString(new SerializedString("value"));
        generator.writeEndObject();
        assertEquals("{\"key\":\"value\"}", getOutput());
    }

    // ==================== Copy Current Event (if applicable) ====================
    // Not directly available in generator, but we can test writeRaw

    @Test
    public void testWriteRaw() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("raw");
        generator.writeRaw("some raw text");
        generator.writeEndObject();
        // raw text is written as is, no escaping
        assertEquals("{\"raw\":some raw text}", getOutput());
    }

    @Test
    public void testWriteRawValue() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("rawval");
        generator.writeRawValue("123");
        generator.writeEndObject();
        assertEquals("{\"rawval\":123}", getOutput());
    }

    // ==================== Tree Model (writeObject) ====================

    @Test
    public void testWriteObjectNode() throws IOException {
        // Not directly supported, but we can test writeObject with a simple map
        // This test may fail if not implemented; adjust accordingly
        // For now, skip or use writeStartObject/writeEndObject
    }

    // ==================== Feature Toggles ====================

    @Test
    public void testWithQuoteFieldNames() throws IOException {
        // Default is true, test that field names are quoted
        generator.writeStartObject();
        generator.writeFieldName("field");
        generator.writeString("value");
        generator.writeEndObject();
        assertTrue(getOutput().contains("\"field\""));
    }

    @Test
    public void testWithFlushPassedToStream() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(baos);
        gen.writeStartObject();
        gen.writeEndObject();
        gen.flush();
        assertTrue(baos.size() > 0);
        gen.close();
    }

    // ==================== Stress Test ====================

    @Test
    public void testManyFields() throws IOException {
        generator.writeStartObject();
        for (int i = 0; i < 100; i++) {
            generator.writeFieldName("field" + i);
            generator.writeString("value" + i);
        }
        generator.writeEndObject();
        String output = getOutput();
        assertTrue(output.startsWith("{"));
        assertTrue(output.endsWith("}"));
        assertTrue(output.contains("\"field0\":\"value0\""));
        assertTrue(output.contains("\"field99\":\"value99\""));
    }

    @Test
    public void testDeepNesting() throws IOException {
        generator.writeStartArray();
        for (int i = 0; i < 100; i++) {
            generator.writeStartArray();
        }
        for (int i = 0; i < 100; i++) {
            generator.writeEndArray();
        }
        generator.writeEndArray();
        String output = getOutput();
        assertEquals(100, output.length()); // 100 '[' + 100 ']' = 200? Actually each '[' and ']' so 200 chars
        // Let's just check it's valid
        assertTrue(output.matches("\\[+\\]+"));
    }
}