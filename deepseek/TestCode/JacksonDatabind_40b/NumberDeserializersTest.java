package com.fasterxml.jackson.databind.deser.std;

import static org.junit.Assert.*;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Comprehensive JUnit 4 test suite for NumberDeserializers.
 * Targets maximum code coverage and fault detection for Defects4J bug 40.
 */
public class NumberDeserializersTest {

    private final ObjectMapper mapper = new ObjectMapper();

    // Helper to create a JsonParser from a string
    private JsonParser createParser(String json) throws IOException {
        return mapper.getFactory().createParser(json);
    }

    // Helper to get DeserializationContext
    private DeserializationContext getContext() {
        return mapper.getDeserializationContext();
    }

    // ---------- BooleanDeserializer ----------
    @Test
    public void testBooleanDeserializerTrue() throws IOException {
        BooleanDeserializer deser = new BooleanDeserializer(Boolean.class, null);
        JsonParser p = createParser("true");
        p.nextToken();
        Boolean result = deser.deserialize(p, getContext());
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testBooleanDeserializerFalse() throws IOException {
        BooleanDeserializer deser = new BooleanDeserializer(Boolean.class, null);
        JsonParser p = createParser("false");
        p.nextToken();
        Boolean result = deser.deserialize(p, getContext());
        assertEquals(Boolean.FALSE, result);
    }

    @Test(expected = IOException.class)
    public void testBooleanDeserializerInvalid() throws IOException {
        BooleanDeserializer deser = new BooleanDeserializer(Boolean.class, null);
        JsonParser p = createParser("\"notBoolean\"");
        p.nextToken();
        deser.deserialize(p, getContext());
    }

    @Test
    public void testBooleanDeserializerNull() throws IOException {
        BooleanDeserializer deser = new BooleanDeserializer(Boolean.class, null);
        JsonParser p = createParser("null");
        p.nextToken();
        Boolean result = deser.deserialize(p, getContext());
        assertNull(result);
    }

    // ---------- ByteDeserializer ----------
    @Test
    public void testByteDeserializerValid() throws IOException {
        ByteDeserializer deser = new ByteDeserializer(Byte.class, null);
        JsonParser p = createParser("42");
        p.nextToken();
        Byte result = deser.deserialize(p, getContext());
        assertEquals(Byte.valueOf((byte) 42), result);
    }

    @Test(expected = IOException.class)
    public void testByteDeserializerOverflow() throws IOException {
        ByteDeserializer deser = new ByteDeserializer(Byte.class, null);
        JsonParser p = createParser("128");
        p.nextToken();
        deser.deserialize(p, getContext());
    }

    @Test(expected = IOException.class)
    public void testByteDeserializerUnderflow() throws IOException {
        ByteDeserializer deser = new ByteDeserializer(Byte.class, null);
        JsonParser p = createParser("-129");
        p.nextToken();
        deser.deserialize(p, getContext());
    }

    @Test
    public void testByteDeserializerNull() throws IOException {
        ByteDeserializer deser = new ByteDeserializer(Byte.class, null);
        JsonParser p = createParser("null");
        p.nextToken();
        Byte result = deser.deserialize(p, getContext());
        assertNull(result);
    }

    // ---------- ShortDeserializer ----------
    @Test
    public void testShortDeserializerValid() throws IOException {
        ShortDeserializer deser = new ShortDeserializer(Short.class, null);
        JsonParser p = createParser("12345");
        p.nextToken();
        Short result = deser.deserialize(p, getContext());
        assertEquals(Short.valueOf((short) 12345), result);
    }

    @Test(expected = IOException.class)
    public void testShortDeserializerOverflow() throws IOException {
        ShortDeserializer deser = new ShortDeserializer(Short.class, null);
        JsonParser p = createParser("32768");
        p.nextToken();
        deser.deserialize(p, getContext());
    }

    @Test(expected = IOException.class)
    public void testShortDeserializerUnderflow() throws IOException {
        ShortDeserializer deser = new ShortDeserializer(Short.class, null);
        JsonParser p = createParser("-32769");
        p.nextToken();
        deser.deserialize(p, getContext());
    }

    @Test
    public void testShortDeserializerNull() throws IOException {
        ShortDeserializer deser = new ShortDeserializer(Short.class, null);
        JsonParser p = createParser("null");
        p.nextToken();
        Short result = deser.deserialize(p, getContext());
        assertNull(result);
    }

    // ---------- IntegerDeserializer ----------
    @Test
    public void testIntegerDeserializerValid() throws IOException {
        IntegerDeserializer deser = new IntegerDeserializer(Integer.class, null);
        JsonParser p = createParser("123456789");
        p.nextToken();
        Integer result = deser.deserialize(p, getContext());
        assertEquals(Integer.valueOf(123456789), result);
    }

    @Test(expected = IOException.class)
    public void testIntegerDeserializerOverflow() throws IOException {
        IntegerDeserializer deser = new IntegerDeserializer(Integer.class, null);
        JsonParser p = createParser("2147483648");
        p.nextToken();
        deser.deserialize(p, getContext());
    }

    @Test(expected = IOException.class)
    public void testIntegerDeserializerUnderflow() throws IOException {
        IntegerDeserializer deser = new IntegerDeserializer(Integer.class, null);
        JsonParser p = createParser("-2147483649");
        p.nextToken();
        deser.deserialize(p, getContext());
    }

    @Test
    public void testIntegerDeserializerNull() throws IOException {
        IntegerDeserializer deser = new IntegerDeserializer(Integer.class, null);
        JsonParser p = createParser("null");
        p.nextToken();
        Integer result = deser.deserialize(p, getContext());
        assertNull(result);
    }

    // ---------- LongDeserializer ----------
    @Test
    public void testLongDeserializerValid() throws IOException {
        LongDeserializer deser = new LongDeserializer(Long.class, null);
        JsonParser p = createParser("9876543210");
        p.nextToken();
        Long result = deser.deserialize(p, getContext());
        assertEquals(Long.valueOf(9876543210L), result);
    }

    @Test(expected = IOException.class)
    public void testLongDeserializerOverflow() throws IOException {
        LongDeserializer deser = new LongDeserializer(Long.class, null);
        JsonParser p = createParser("9223372036854775808");
        p.nextToken();
        deser.deserialize(p, getContext());
    }

    @Test(expected = IOException.class)
    public void testLongDeserializerUnderflow() throws IOException {
        LongDeserializer deser = new LongDeserializer(Long.class, null);
        JsonParser p = createParser("-9223372036854775809");
        p.nextToken();
        deser.deserialize(p, getContext());
    }

    @Test
    public void testLongDeserializerNull() throws IOException {
        LongDeserializer deser = new LongDeserializer(Long.class, null);
        JsonParser p = createParser("null");
        p.nextToken();
        Long result = deser.deserialize(p, getContext());
        assertNull(result);
    }

    // ---------- FloatDeserializer ----------
    @Test
    public void testFloatDeserializerValid() throws IOException {
        FloatDeserializer deser = new FloatDeserializer(Float.class, null);
        JsonParser p = createParser("3.14");
        p.nextToken();
        Float result = deser.deserialize(p, getContext());
        assertEquals(Float.valueOf(3.14f), result);
    }

    @Test
    public void testFloatDeserializerNaN() throws IOException {
        FloatDeserializer deser = new FloatDeserializer(Float.class, null);
        JsonParser p = createParser("\"NaN\"");
        p.nextToken();
        Float result = deser.deserialize(p, getContext());
        assertTrue(Float.isNaN(result));
    }

    @Test
    public void testFloatDeserializerInfinity() throws IOException {
        FloatDeserializer deser = new FloatDeserializer(Float.class, null);
        JsonParser p = createParser("\"Infinity\"");
        p.nextToken();
        Float result = deser.deserialize(p, getContext());
        assertEquals(Float.POSITIVE_INFINITY, result);
    }

    @Test
    public void testFloatDeserializerNegativeInfinity() throws IOException {
        FloatDeserializer deser = new FloatDeserializer(Float.class, null);
        JsonParser p = createParser("\"-Infinity\"");
        p.nextToken();
        Float result = deser.deserialize(p, getContext());
        assertEquals(Float.NEGATIVE_INFINITY, result);
    }

    @Test(expected = IOException.class)
    public void testFloatDeserializerInvalidString() throws IOException {
        FloatDeserializer deser = new FloatDeserializer(Float.class, null);
        JsonParser p = createParser("\"notFloat\"");
        p.nextToken();
        deser.deserialize(p, getContext());
    }

    @Test
    public void testFloatDeserializerNull() throws IOException {
        FloatDeserializer deser = new FloatDeserializer(Float.class, null);
        JsonParser p = createParser("null");
        p.nextToken();
        Float result = deser.deserialize(p, getContext());
        assertNull(result);
    }

    // ---------- DoubleDeserializer ----------
    @Test
    public void testDoubleDeserializerValid() throws IOException {
        DoubleDeserializer deser = new DoubleDeserializer(Double.class, null);
        JsonParser p = createParser("2.71828");
        p.nextToken();
        Double result = deser.deserialize(p, getContext());
        assertEquals(Double.valueOf(2.71828), result);
    }

    @Test
    public void testDoubleDeserializerNaN() throws IOException {
        DoubleDeserializer deser = new DoubleDeserializer(Double.class, null);
        JsonParser p = createParser("\"NaN\"");
        p.nextToken();
        Double result = deser.deserialize(p, getContext());
        assertTrue(Double.isNaN(result));
    }

    @Test
    public void testDoubleDeserializerInfinity() throws IOException {
        DoubleDeserializer deser = new DoubleDeserializer(Double.class, null);
        JsonParser p = createParser("\"Infinity\"");
        p.nextToken();
        Double result = deser.deserialize(p, getContext());
        assertEquals(Double.POSITIVE_INFINITY, result);
    }

    @Test
    public void testDoubleDeserializerNegativeInfinity() throws IOException {
        DoubleDeserializer deser = new DoubleDeserializer(Double.class, null);
        JsonParser p = createParser("\"-Infinity\"");
        p.nextToken();
        Double result = deser.deserialize(p, getContext());
        assertEquals(Double.NEGATIVE_INFINITY, result);
    }

    @Test(expected = IOException.class)
    public void testDoubleDeserializerInvalidString() throws IOException {
        DoubleDeserializer deser = new DoubleDeserializer(Double.class, null);
        JsonParser p = createParser("\"notDouble\"");
        p.nextToken();
        deser.deserialize(p, getContext());
    }

    @Test
    public void testDoubleDeserializerNull() throws IOException {
        DoubleDeserializer deser = new DoubleDeserializer(Double.class, null);
        JsonParser p = createParser("null");
        p.nextToken();
        Double result = deser.deserialize(p, getContext());
        assertNull(result);
    }

    // ---------- BigIntegerDeserializer ----------
    @Test
    public void testBigIntegerDeserializerValid() throws IOException {
        BigIntegerDeserializer deser = new BigIntegerDeserializer();
        JsonParser p = createParser("12345678901234567890");
        p.nextToken();
        BigInteger result = deser.deserialize(p, getContext());
        assertEquals(new BigInteger("12345678901234567890"), result);
    }

    @Test(expected = IOException.class)
    public void testBigIntegerDeserializerInvalid() throws IOException {
        BigIntegerDeserializer deser = new BigIntegerDeserializer();
        JsonParser p = createParser("\"notBigInteger\"");
        p.nextToken();
        deser.deserialize(p, getContext());
    }

    @Test
    public void testBigIntegerDeserializerNull() throws IOException {
        BigIntegerDeserializer deser = new BigIntegerDeserializer();
        JsonParser p = createParser("null");
        p.nextToken();
        BigInteger result = deser.deserialize(p, getContext());
        assertNull(result);
    }

    // ---------- BigDecimalDeserializer ----------
    @Test
    public void testBigDecimalDeserializerValid() throws IOException {
        BigDecimalDeserializer deser = new BigDecimalDeserializer();
        JsonParser p = createParser("123.456");
        p.nextToken();
        BigDecimal result = deser.deserialize(p, getContext());
        assertEquals(new BigDecimal("123.456"), result);
    }

    @Test(expected = IOException.class)
    public void testBigDecimalDeserializerInvalid() throws IOException {
        BigDecimalDeserializer deser = new BigDecimalDeserializer();
        JsonParser p = createParser("\"notBigDecimal\"");
        p.nextToken();
        deser.deserialize(p, getContext());
    }

    @Test
    public void testBigDecimalDeserializerNull() throws IOException {
        BigDecimalDeserializer deser = new BigDecimalDeserializer();
        JsonParser p = createParser("null");
        p.nextToken();
        BigDecimal result = deser.deserialize(p, getContext());
        assertNull(result);
    }

    // ---------- NumberDeserializer (generic) ----------
    @Test
    public void testNumberDeserializerInteger() throws IOException {
        NumberDeserializer deser = new NumberDeserializer();
        JsonParser p = createParser("42");
        p.nextToken();
        Number result = deser.deserialize(p, getContext());
        assertEquals(Integer.valueOf(42), result);
    }

    @Test
    public void testNumberDeserializerLong() throws IOException {
        NumberDeserializer deser = new NumberDeserializer();
        JsonParser p = createParser("1234567890123");
        p.nextToken();
        Number result = deser.deserialize(p, getContext());
        assertEquals(Long.valueOf(1234567890123L), result);
    }

    @Test
    public void testNumberDeserializerDouble() throws IOException {
        NumberDeserializer deser = new NumberDeserializer();
        JsonParser p = createParser("3.14");
        p.nextToken();
        Number result = deser.deserialize(p, getContext());
        assertEquals(Double.valueOf(3.14), result);
    }

    @Test
    public void testNumberDeserializerNull() throws IOException {
        NumberDeserializer deser = new NumberDeserializer();
        JsonParser p = createParser("null");
        p.nextToken();
        Number result = deser.deserialize(p, getContext());
        assertNull(result);
    }

    // ---------- Edge cases for empty/blank strings ----------
    @Test(expected = IOException.class)
    public void testBooleanDeserializerEmptyString() throws IOException {
        BooleanDeserializer deser = new BooleanDeserializer(Boolean.class, null);
        JsonParser p = createParser("\"\"");
        p.nextToken();
        deser.deserialize(p, getContext());
    }

    @Test(expected = IOException.class)
    public void testIntegerDeserializerEmptyString() throws IOException {
        IntegerDeserializer deser = new IntegerDeserializer(Integer.class, null);
        JsonParser p = createParser("\"\"");
        p.nextToken();
        deser.deserialize(p, getContext());
    }

    @Test(expected = IOException.class)
    public void testLongDeserializerEmptyString() throws IOException {
        LongDeserializer deser = new LongDeserializer(Long.class, null);
        JsonParser p = createParser("\"\"");
        p.nextToken();
        deser.deserialize(p, getContext());
    }

    @Test(expected = IOException.class)
    public void testFloatDeserializerEmptyString() throws IOException {
        FloatDeserializer deser = new FloatDeserializer(Float.class, null);
        JsonParser p = createParser("\"\"");
        p.nextToken();
        deser.deserialize(p, getContext());
    }

    @Test(expected = IOException.class)
    public void testDoubleDeserializerEmptyString() throws IOException {
        DoubleDeserializer deser = new DoubleDeserializer(Double.class, null);
        JsonParser p = createParser("\"\"");
        p.nextToken();
        deser.deserialize(p, getContext());
    }

    @Test(expected = IOException.class)
    public void testBigIntegerDeserializerEmptyString() throws IOException {
        BigIntegerDeserializer deser = new BigIntegerDeserializer();
        JsonParser p = createParser("\"\"");
        p.nextToken();
        deser.deserialize(p, getContext());
    }

    @Test(expected = IOException.class)
    public void testBigDecimalDeserializerEmptyString() throws IOException {
        BigDecimalDeserializer deser = new BigDecimalDeserializer();
        JsonParser p = createParser("\"\"");
        p.nextToken();
        deser.deserialize(p, getContext());
    }

    // ---------- Test with DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS ----------
    @Test
    public void testNumberDeserializerWithBigDecimalForFloats() throws IOException {
        ObjectMapper mapperWithFeature = new ObjectMapper();
        mapperWithFeature.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        JsonParser p = mapperWithFeature.getFactory().createParser("3.14");
        p.nextToken();
        NumberDeserializer deser = new NumberDeserializer();
        Number result = deser.deserialize(p, mapperWithFeature.getDeserializationContext());
        assertTrue(result instanceof BigDecimal);
        assertEquals(new BigDecimal("3.14"), result);
    }

    @Test
    public void testNumberDeserializerWithBigIntegerForInts() throws IOException {
        ObjectMapper mapperWithFeature = new ObjectMapper();
        mapperWithFeature.enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);
        JsonParser p = mapperWithFeature.getFactory().createParser("42");
        p.nextToken();
        NumberDeserializer deser = new NumberDeserializer();
        Number result = deser.deserialize(p, mapperWithFeature.getDeserializationContext());
        assertTrue(result instanceof BigInteger);
        assertEquals(BigInteger.valueOf(42), result);
    }

    // ---------- Test primitive types (boolean, int, long, double) ----------
    @Test
    public void testBooleanDeserializerPrimitive() throws IOException {
        BooleanDeserializer deser = new BooleanDeserializer(Boolean.TYPE, null);
        JsonParser p = createParser("true");
        p.nextToken();
        Object result = deser.deserialize(p, getContext());
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testIntegerDeserializerPrimitive() throws IOException {
        IntegerDeserializer deser = new IntegerDeserializer(Integer.TYPE, null);
        JsonParser p = createParser("99");
        p.nextToken();
        Object result = deser.deserialize(p, getContext());
        assertEquals(Integer.valueOf(99), result);
    }

    @Test
    public void testLongDeserializerPrimitive() throws IOException {
        LongDeserializer deser = new LongDeserializer(Long.TYPE, null);
        JsonParser p = createParser("999");
        p.nextToken();
        Object result = deser.deserialize(p, getContext());
        assertEquals(Long.valueOf(999), result);
    }

    @Test
    public void testDoubleDeserializerPrimitive() throws IOException {
        DoubleDeserializer deser = new DoubleDeserializer(Double.TYPE, null);
        JsonParser p = createParser("1.23");
        p.nextToken();
        Object result = deser.deserialize(p, getContext());
        assertEquals(Double.valueOf(1.23), result);
    }

    // ---------- Test handling of null tokens ----------
    @Test
    public void testBooleanDeserializerNullToken() throws IOException {
        BooleanDeserializer deser = new BooleanDeserializer(Boolean.class, null);
        JsonParser p = createParser("null");
        p.nextToken();
        Boolean result = deser.deserialize(p, getContext());
        assertNull(result);
    }

    // Additional edge: test with JSON array (should fail)
    @Test(expected = IOException.class)
    public void testIntegerDeserializerArrayToken() throws IOException {
        IntegerDeserializer deser = new IntegerDeserializer(Integer.class, null);
        JsonParser p = createParser("[1]");
        p.nextToken(); // START_ARRAY
        deser.deserialize(p, getContext());
    }

    // Test with negative zero for float/double
    @Test
    public void testFloatDeserializerNegativeZero() throws IOException {
        FloatDeserializer deser = new FloatDeserializer(Float.class, null);
        JsonParser p = createParser("-0.0");
        p.nextToken();
        Float result = deser.deserialize(p, getContext());
        assertEquals(-0.0f, result, 0.0f);
    }

    @Test
    public void testDoubleDeserializerNegativeZero() throws IOException {
        DoubleDeserializer deser = new DoubleDeserializer(Double.class, null);
        JsonParser p = createParser("-0.0");
        p.nextToken();
        Double result = deser.deserialize(p, getContext());
        assertEquals(-0.0, result, 0.0);
    }

    // Test with very large numbers for BigInteger/BigDecimal
    @Test
    public void testBigIntegerDeserializerVeryLarge() throws IOException {
        BigIntegerDeserializer deser = new BigIntegerDeserializer();
        String big = "1234567890123456789012345678901234567890";
        JsonParser p = createParser(big);
        p.nextToken();
        BigInteger result = deser.deserialize(p, getContext());
        assertEquals(new BigInteger(big), result);
    }

    @Test
    public void testBigDecimalDeserializerVeryLarge() throws IOException {
        BigDecimalDeserializer deser = new BigDecimalDeserializer();
        String big = "12345678901234567890.1234567890123456789";
        JsonParser p = createParser(big);
        p.nextToken();
        BigDecimal result = deser.deserialize(p, getContext());
        assertEquals(new BigDecimal(big), result);
    }

    // Test with scientific notation for float/double
    @Test
    public void testFloatDeserializerScientific() throws IOException {
        FloatDeserializer deser = new FloatDeserializer(Float.class, null);
        JsonParser p = createParser("1.23e4");
        p.nextToken();
        Float result = deser.deserialize(p, getContext());
        assertEquals(12300.0f, result, 0.0f);
    }

    @Test
    public void testDoubleDeserializerScientific() throws IOException {
        DoubleDeserializer deser = new DoubleDeserializer(Double.class, null);
        JsonParser p = createParser("1.23e4");
        p.nextToken();
        Double result = deser.deserialize(p, getContext());
        assertEquals(12300.0, result, 0.0);
    }

    // Test with hex integer (should fail for most deserializers except maybe BigInteger)
    @Test(expected = IOException.class)
    public void testIntegerDeserializerHex() throws IOException {
        IntegerDeserializer deser = new IntegerDeserializer(Integer.class, null);
        JsonParser p = createParser("0x1A");
        p.nextToken();
        deser.deserialize(p, getContext());
    }

    // Test with octal (leading zero) - may be interpreted as decimal
    @Test
    public void testIntegerDeserializerOctal() throws IOException {
        IntegerDeserializer deser = new IntegerDeserializer(Integer.class, null);
        JsonParser p = createParser("077");
        p.nextToken();
        Integer result = deser.deserialize(p, getContext());
        assertEquals(Integer.valueOf(77), result); // Jackson treats as decimal
    }

    // Test with leading plus sign
    @Test
    public void testIntegerDeserializerLeadingPlus() throws IOException {
        IntegerDeserializer deser = new IntegerDeserializer(Integer.class, null);
        JsonParser p = createParser("+123");
        p.nextToken();
        Integer result = deser.deserialize(p, getContext());
        assertEquals(Integer.valueOf(123), result);
    }

    // Test with whitespace in number string (should fail)
    @Test(expected = IOException.class)
    public void testIntegerDeserializerWhitespace() throws IOException {
        IntegerDeserializer deser = new IntegerDeserializer(Integer.class, null);
        JsonParser p = createParser(" 123 ");
        p.nextToken();
        deser.deserialize(p, getContext());
    }

    // Test with multiple dots (should fail)
    @Test(expected = IOException.class)
    public void testDoubleDeserializerMultipleDots() throws IOException {
        DoubleDeserializer deser = new DoubleDeserializer(Double.class, null);
        JsonParser p = createParser("1.2.3");
        p.nextToken();
        deser.deserialize(p, getContext());
    }

    // Test with empty array (should fail)
    @Test(expected = IOException.class)
    public void testNumberDeserializerEmptyArray() throws IOException {
        NumberDeserializer deser = new NumberDeserializer();
        JsonParser p = createParser("[]");
        p.nextToken();
        deser.deserialize(p, getContext());
    }

    // Test with object (should fail)
    @Test(expected = IOException.class)
    public void testNumberDeserializerObject() throws IOException {
        NumberDeserializer deser = new NumberDeserializer();
        JsonParser p = createParser("{}");
        p.nextToken();
        deser.deserialize(p, getContext());
    }
}