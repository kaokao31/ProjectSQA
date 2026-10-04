package com.fasterxml.jackson.databind.ser.std;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.ser.std.NumberSerializers.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class NumberSerializersTest {

    private ObjectMapper mapper;
    private NumberSerializers serializers;
    private SerializerProvider prov;
    private TypeFactory typeFactory;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        serializers = new NumberSerializers();
        prov = mapper.getSerializerProvider();
        typeFactory = TypeFactory.defaultInstance();
    }

    @Test
    public void testIntegerSerializer() throws Exception {
        assertEquals("0", mapper.writeValueAsString(0));
        assertEquals("1", mapper.writeValueAsString(1));
        assertEquals("-1", mapper.writeValueAsString(-1));
        assertEquals(String.valueOf(Integer.MAX_VALUE), mapper.writeValueAsString(Integer.MAX_VALUE));
        assertEquals(String.valueOf(Integer.MIN_VALUE), mapper.writeValueAsString(Integer.MIN_VALUE));
    }

    @Test
    public void testLongSerializer() throws Exception {
        assertEquals("0", mapper.writeValueAsString(0L));
        assertEquals("1", mapper.writeValueAsString(1L));
        assertEquals("-1", mapper.writeValueAsString(-1L));
        assertEquals(String.valueOf(Long.MAX_VALUE), mapper.writeValueAsString(Long.MAX_VALUE));
        assertEquals(String.valueOf(Long.MIN_VALUE), mapper.writeValueAsString(Long.MIN_VALUE));
    }

    @Test
    public void testFloatSerializer() throws Exception {
        assertEquals("0.0", mapper.writeValueAsString(0.0f));
        assertEquals("1.0", mapper.writeValueAsString(1.0f));
        assertEquals("-1.0", mapper.writeValueAsString(-1.0f));
        assertEquals("NaN", mapper.writeValueAsString(Float.NaN));
        assertEquals("Infinity", mapper.writeValueAsString(Float.POSITIVE_INFINITY));
        assertEquals("-Infinity", mapper.writeValueAsString(Float.NEGATIVE_INFINITY));
    }

    @Test
    public void testDoubleSerializer() throws Exception {
        assertEquals("0.0", mapper.writeValueAsString(0.0));
        assertEquals("1.0", mapper.writeValueAsString(1.0));
        assertEquals("-1.0", mapper.writeValueAsString(-1.0));
        assertEquals("NaN", mapper.writeValueAsString(Double.NaN));
        assertEquals("Infinity", mapper.writeValueAsString(Double.POSITIVE_INFINITY));
        assertEquals("-Infinity", mapper.writeValueAsString(Double.NEGATIVE_INFINITY));
    }

    @Test
    public void testBigDecimalSerializer() throws Exception {
        assertEquals("0", mapper.writeValueAsString(BigDecimal.ZERO));
        assertEquals("1", mapper.writeValueAsString(BigDecimal.ONE));
        assertEquals("10", mapper.writeValueAsString(BigDecimal.TEN));
        assertEquals("0.5", mapper.writeValueAsString(new BigDecimal("0.5")));
        assertEquals("-3.14", mapper.writeValueAsString(new BigDecimal("-3.14")));
        assertEquals("1e+10", mapper.writeValueAsString(new BigDecimal("1e10")));
    }

    @Test
    public void testBigIntegerSerializer() throws Exception {
        assertEquals("0", mapper.writeValueAsString(BigInteger.ZERO));
        assertEquals("1", mapper.writeValueAsString(BigInteger.ONE));
        assertEquals("10", mapper.writeValueAsString(BigInteger.TEN));
        assertEquals("-123456789", mapper.writeValueAsString(new BigInteger("-123456789")));
        assertEquals("9999999999999999999999999999", mapper.writeValueAsString(new BigInteger("9999999999999999999999999999")));
    }

    @Test
    public void testAtomicIntegerSerializer() throws Exception {
        assertEquals("0", mapper.writeValueAsString(new AtomicInteger(0)));
        assertEquals("42", mapper.writeValueAsString(new AtomicInteger(42)));
        assertEquals("-1", mapper.writeValueAsString(new AtomicInteger(-1)));
        assertEquals(String.valueOf(Integer.MAX_VALUE), mapper.writeValueAsString(new AtomicInteger(Integer.MAX_VALUE)));
        assertEquals(String.valueOf(Integer.MIN_VALUE), mapper.writeValueAsString(new AtomicInteger(Integer.MIN_VALUE)));
    }

    @Test
    public void testAtomicLongSerializer() throws Exception {
        assertEquals("0", mapper.writeValueAsString(new AtomicLong(0L)));
        assertEquals("42", mapper.writeValueAsString(new AtomicLong(42L)));
        assertEquals("-1", mapper.writeValueAsString(new AtomicLong(-1L)));
        assertEquals(String.valueOf(Long.MAX_VALUE), mapper.writeValueAsString(new AtomicLong(Long.MAX_VALUE)));
        assertEquals(String.valueOf(Long.MIN_VALUE), mapper.writeValueAsString(new AtomicLong(Long.MIN_VALUE)));
    }

    @Test
    public void testNullValue() throws Exception {
        assertEquals("null", mapper.writeValueAsString(null));
    }

    @Test
    public void testFindSerializerForInteger() throws Exception {
        JavaType type = typeFactory.constructType(Integer.class);
        JsonSerializer<?> ser = serializers.findSerializer(prov, type);
        assertNotNull("Serializer for Integer should not be null", ser);
        assertTrue("Serializer should be IntegerSerializer", ser instanceof IntegerSerializer);
    }

    @Test
    public void testFindSerializerForLong() throws Exception {
        JavaType type = typeFactory.constructType(Long.class);
        JsonSerializer<?> ser = serializers.findSerializer(prov, type);
        assertNotNull("Serializer for Long should not be null", ser);
        assertTrue("Serializer should be LongSerializer", ser instanceof LongSerializer);
    }

    @Test
    public void testFindSerializerForFloat() throws Exception {
        JavaType type = typeFactory.constructType(Float.class);
        JsonSerializer<?> ser = serializers.findSerializer(prov, type);
        assertNotNull("Serializer for Float should not be null", ser);
        assertTrue("Serializer should be FloatSerializer", ser instanceof FloatSerializer);
    }

    @Test
    public void testFindSerializerForDouble() throws Exception {
        JavaType type = typeFactory.constructType(Double.class);
        JsonSerializer<?> ser = serializers.findSerializer(prov, type);
        assertNotNull("Serializer for Double should not be null", ser);
        assertTrue("Serializer should be DoubleSerializer", ser instanceof DoubleSerializer);
    }

    @Test
    public void testFindSerializerForBigDecimal() throws Exception {
        JavaType type = typeFactory.constructType(BigDecimal.class);
        JsonSerializer<?> ser = serializers.findSerializer(prov, type);
        assertNotNull("Serializer for BigDecimal should not be null", ser);
        assertTrue("Serializer should be BigDecimalSerializer", ser instanceof BigDecimalSerializer);
    }

    @Test
    public void testFindSerializerForBigInteger() throws Exception {
        JavaType type = typeFactory.constructType(BigInteger.class);
        JsonSerializer<?> ser = serializers.findSerializer(prov, type);
        assertNotNull("Serializer for BigInteger should not be null", ser);
        assertTrue("Serializer should be BigIntegerSerializer", ser instanceof BigIntegerSerializer);
    }

    @Test
    public void testFindSerializerForAtomicInteger() throws Exception {
        JavaType type = typeFactory.constructType(AtomicInteger.class);
        JsonSerializer<?> ser = serializers.findSerializer(prov, type);
        assertNotNull("Serializer for AtomicInteger should not be null", ser);
        assertTrue("Serializer should be AtomicIntegerSerializer", ser instanceof AtomicIntegerSerializer);
    }

    @Test
    public void testFindSerializerForAtomicLong() throws Exception {
        JavaType type = typeFactory.constructType(AtomicLong.class);
        JsonSerializer<?> ser = serializers.findSerializer(prov, type);
        assertNotNull("Serializer for AtomicLong should not be null", ser);
        assertTrue("Serializer should be AtomicLongSerializer", ser instanceof AtomicLongSerializer);
    }

    @Test
    public void testFindSerializerForNumber() throws Exception {
        JavaType type = typeFactory.constructType(Number.class);
        JsonSerializer<?> ser = serializers.findSerializer(prov, type);
        assertNotNull("Serializer for Number should not be null", ser);
        assertTrue("Serializer should be NumberSerializer", ser instanceof NumberSerializer);
    }

    @Test
    public void testCustomNumberSubclass() throws Exception {
        Number custom = new Number() {
            @Override
            public int intValue() { return 123; }
            @Override
            public long longValue() { return 123L; }
            @Override
            public float floatValue() { return 123.0f; }
            @Override
            public double doubleValue() { return 123.0; }
            @Override
            public String toString() { return "custom"; }
        };
        // NumberSerializer uses toString() for unknown Number subclasses
        assertEquals("\"custom\"", mapper.writeValueAsString(custom));
    }

    @Test
    public void testEdgeCasesForDouble() throws Exception {
        // Double.MIN_VALUE is the smallest positive nonzero double
        assertEquals("4.9E-324", mapper.writeValueAsString(Double.MIN_VALUE));
        // Double.MAX_VALUE
        assertEquals("1.7976931348623157E308", mapper.writeValueAsString(Double.MAX_VALUE));
        // Negative zero
        assertEquals("-0.0", mapper.writeValueAsString(-0.0));
    }

    @Test
    public void testEdgeCasesForFloat() throws Exception {
        assertEquals("1.4E-45", mapper.writeValueAsString(Float.MIN_VALUE));
        assertEquals("3.4028235E38", mapper.writeValueAsString(Float.MAX_VALUE));
        assertEquals("-0.0", mapper.writeValueAsString(-0.0f));
    }

    @Test
    public void testLargeBigDecimal() throws Exception {
        BigDecimal large = new BigDecimal("12345678901234567890.123456789");
        assertEquals("12345678901234567890.123456789", mapper.writeValueAsString(large));
    }

    @Test
    public void testNegativeBigInteger() throws Exception {
        BigInteger neg = new BigInteger("-999999999999999999999999999999");
        assertEquals("-999999999999999999999999999999", mapper.writeValueAsString(neg));
    }
}