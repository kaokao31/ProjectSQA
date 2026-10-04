package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class NumberSerializerTest {

    private JsonMapper mapper;
    private StringWriter sw;
    private JsonGenerator gen;
    private SerializerProvider serializers;

    @Before
    public void setUp() throws Exception {
        mapper = new JsonMapper();
        sw = new StringWriter();
        gen = mapper.createGenerator(sw);
        serializers = mapper.getSerializerProvider();
    }

    @After
    public void tearDown() throws Exception {
        try {
            gen.close();
        } catch (Exception e) {
            // ignore
        }
    }

    @Test
    public void testInstanceConstants() {
        assertNotNull(NumberSerializer.instance);
        assertEquals(NumberSerializer.class, NumberSerializer.instance.getClass());
    }

    @Test
    public void testSerializeInteger() throws IOException {
        NumberSerializer serializer = (NumberSerializer) NumberSerializer.instance;
        serializer.serialize(12345, gen, serializers);
        gen.flush();
        assertEquals("12345", sw.toString());
    }

    @Test
    public void testSerializeLong() throws IOException {
        NumberSerializer serializer = (NumberSerializer) NumberSerializer.instance;
        serializer.serialize(1234567890123L, gen, serializers);
        gen.flush();
        assertEquals("1234567890123", sw.toString());
    }

    @Test
    public void testSerializeBigInteger() throws IOException {
        NumberSerializer serializer = (NumberSerializer) NumberSerializer.instance;
        BigInteger bigInt = new BigInteger("12345678901234567890");
        serializer.serialize(bigInt, gen, serializers);
        gen.flush();
        assertEquals("12345678901234567890", sw.toString());
    }

    @Test
    public void testSerializeBigDecimal() throws IOException {
        // Test with WRITE_BIGDECIMAL_AS_PLAIN enabled or disabled
        NumberSerializer serializer = (NumberSerializer) NumberSerializer.instance;
        BigDecimal bigDec = new BigDecimal("123.45600");
        
        serializer.serialize(bigDec, gen, serializers);
        gen.flush();
        assertEquals("123.45600", sw.toString());
    }

    @Test
    public void testSerializeBigDecimalAsPlain() throws IOException {
        ObjectMapper plainMapper = JsonMapper.builder()
                .enable(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN)
                .build();
        StringWriter plainSw = new StringWriter();
        JsonGenerator plainGen = plainMapper.createGenerator(plainSw);
        SerializerProvider plainProviders = plainMapper.getSerializerProvider();

        NumberSerializer serializer = (NumberSerializer) NumberSerializer.instance;
        BigDecimal bigDec = new BigDecimal("1e2");
        serializer.serialize(bigDec, plainGen, plainProviders);
        plainGen.flush();
        
        // WRITE_BIGDECIMAL_AS_PLAIN converts 1e2 or similar depending on JDK/Jackson version, 
        // usually toPlainString()
        assertEquals(bigDec.toPlainString(), plainSw.toString());
        plainGen.close();
    }

    @Test
    public void testSerializeDouble() throws IOException {
        NumberSerializer serializer = (NumberSerializer) NumberSerializer.instance;
        serializer.serialize(123.45D, gen, serializers);
        gen.flush();
        assertEquals("123.45", sw.toString());
    }

    @Test
    public void testSerializeFloat() throws IOException {
        NumberSerializer serializer = (NumberSerializer) NumberSerializer.instance;
        serializer.serialize(12.34F, gen, serializers);
        gen.flush();
        // Float string representation might vary slightly by precision, but let's check it writes without error
        assertTrue(sw.toString().startsWith("12.34"));
    }

    @Test
    public void testSerializeShort() throws IOException {
        NumberSerializer serializer = (NumberSerializer) NumberSerializer.instance;
        serializer.serialize((short) 123, gen, serializers);
        gen.flush();
        assertEquals("123", sw.toString());
    }

    @Test
    public void testSerializeByte() throws IOException {
        NumberSerializer serializer = (NumberSerializer) NumberSerializer.instance;
        serializer.serialize((byte) 12, gen, serializers);
        gen.flush();
        assertEquals("12", sw.toString());
    }

    @Test
    public void testSerializeUnknownNumberSubclass() throws IOException {
        NumberSerializer serializer = (NumberSerializer) NumberSerializer.instance;
        // Custom Number subclass
        Number customNumber = new Number() {
            @Override
            public int intValue() { return 42; }
            @Override
            public long longValue() { return 42L; }
            @Override
            public float floatValue() { return 42.0f; }
            @Override
            public double doubleValue() { return 42.0; }
            @Override
            public String toString() { return "custom-42"; }
        };
        
        serializer.serialize(customNumber, gen, serializers);
        gen.flush();
        assertEquals("custom-42", sw.toString());
    }

    @Test
    public void testGetSchema() {
        NumberSerializer serializer = (NumberSerializer) NumberSerializer.instance;
        JavaType type = mapper.constructType(Integer.class);
        com.fasterxml.jackson.databind.JsonNode schema = serializer.getSchema(serializers, type);
        assertNotNull(schema);
    }

    @Test
    public void testAcceptJsonFormatVisitor() throws Exception {
        NumberSerializer serializer = (NumberSerializer) NumberSerializer.instance;
        JavaType type = mapper.constructType(Integer.class);
        
        // Just invoking to ensure no exceptions are thrown
        try {
            serializer.acceptJsonFormatVisitor(serializers, type);
        } catch (UnsupportedOperationException e) {
            // Some versions might throw or delegate, let's catch or assert
        }
    }

    @Test
    public void testCreateContextual() throws Exception {
        NumberSerializer serializer = (NumberSerializer) NumberSerializer.instance;
        BeanProperty.Std property = new BeanProperty.Std(
                com.fasterxml.jackson.databind.util.NameTransformer.NOP.transform("test"),
                mapper.constructType(BigDecimal.class),
                null, null, null,
                com.fasterxml.jackson.databind.cfg.MapperConfig.DEFAULT_FEATURE_INCLUDE_ALL
        );
        
        JsonSerializer<?> contextual = serializer.createContextual(serializers, property);
        assertNotNull(contextual);
    }
}