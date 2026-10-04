package com.fasterxml.jackson.core.base;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

public class GeneratorBaseTest {

    private GeneratorBase createGenerator(StringWriter sw, JsonGenerator.Feature... features) throws IOException {
        JsonFactory factory = new JsonFactory();
        for (JsonGenerator.Feature feature : features) {
            factory.enable(feature);
        }
        JsonGenerator generator = factory.createGenerator(sw);
        assertTrue("Expected a GeneratorBase but got " + generator.getClass().getName(),
                generator instanceof GeneratorBase);
        return (GeneratorBase) generator;
    }

    private void assertWrittenObject(Object value, String expected) throws IOException {
        StringWriter sw = new StringWriter();
        GeneratorBase generator = createGenerator(sw);
        generator.writeObject(value);
        generator.close();
        assertEquals(expected, sw.toString());
    }

    @Test
    public void testBaseState() throws IOException {
        StringWriter sw = new StringWriter();
        GeneratorBase generator = createGenerator(sw);

        assertNull(generator.getCodec());
        assertNotNull(generator.getOutputContext());
        assertTrue(generator.getOutputContext().inRoot());
        assertFalse(generator.isClosed());
        assertNull(generator.getPrettyPrinter());

        assertSame(generator, generator.setCodec(null));
        assertNull(generator.getCodec());

        assertSame(generator, generator.setPrettyPrinter(new DefaultPrettyPrinter()));
        assertNotNull(generator.getPrettyPrinter());

        generator.close();
        assertTrue(generator.isClosed());
    }

    @Test
    public void testUseDefaultPrettyPrinter() throws IOException {
        StringWriter sw = new StringWriter();
        GeneratorBase generator = createGenerator(sw);

        assertSame(generator, generator.useDefaultPrettyPrinter());
        assertNotNull(generator.getPrettyPrinter());

        DefaultPrettyPrinter custom = new DefaultPrettyPrinter();
        generator.setPrettyPrinter(custom);
        assertSame(custom, generator.useDefaultPrettyPrinter());
        assertSame(custom, generator.getPrettyPrinter());
    }

    @Test
    public void testOutputContextTransitions() throws IOException {
        StringWriter sw = new StringWriter();
        GeneratorBase generator = createGenerator(sw);

        generator.writeStartObject();
        assertTrue(generator.getOutputContext().inObject());
        generator.writeEndObject();
        assertTrue(generator.getOutputContext().inRoot());

        generator.writeStartArray();
        assertTrue(generator.getOutputContext().inArray());
        generator.writeEndArray();
        assertTrue(generator.getOutputContext().inRoot());

        generator.close();
    }

    @Test
    public void testWriteSerializableString() throws IOException {
        StringWriter sw = new StringWriter();
        GeneratorBase generator = createGenerator(sw);

        generator.writeString(new SerializedString("value"));
        generator.close();

        assertEquals("\"value\"", sw.toString());
    }

    @Test
    public void testWriteObjectNull() throws IOException {
        assertWrittenObject(null, "null");
    }

    @Test
    public void testWriteObjectSimpleTypes() throws IOException {
        assertWrittenObject("value", "\"value\"");
        assertWrittenObject(42, "42");
        assertWrittenObject(123456789012345L, "123456789012345");
        assertWrittenObject(1.5d, "1.5");
        assertWrittenObject(2.5f, "2.5");
        assertWrittenObject((short) 3, "3");
        assertWrittenObject((byte) 4, "4");
        assertWrittenObject(new BigInteger("123456789012345678901234567890"),
                "123456789012345678901234567890");
        assertWrittenObject(new BigDecimal("123.456"), "123.456");
        assertWrittenObject(Boolean.TRUE, "true");
        assertWrittenObject('x', "\"x\"");
        assertWrittenObject(new byte[] { 1, 2, 3 }, "\"AQID\"");
    }

    @Test
    public void testWriteObjectAtomicInteger() throws IOException {
        assertWrittenObject(new AtomicInteger(42), "42");
    }

    @Test
    public void testWriteObjectAtomicLong() throws IOException {
        assertWrittenObject(new AtomicLong(9876543210L), "9876543210");
    }

    @Test
    public void testWriteObjectAtomicBoolean() throws IOException {
        assertWrittenObject(new AtomicBoolean(true), "true");
    }

    @Test(expected = IllegalStateException.class)
    public void testWriteObjectUnsupportedWithoutCodec() throws IOException {
        StringWriter sw = new StringWriter();
        GeneratorBase generator = createGenerator(sw);
        generator.writeObject(new Object());
    }

    @Test
    public void testWriteTreeNull() throws IOException {
        StringWriter sw = new StringWriter();
        GeneratorBase generator = createGenerator(sw);

        generator.writeTree(null);
        generator.close();

        assertEquals("null", sw.toString());
    }

    @Test
    public void testNumbersAsStringsFeature() throws IOException {
        StringWriter sw = new StringWriter();
        GeneratorBase generator = createGenerator(sw, JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);

        generator.writeNumber(123);
        generator.close();

        assertEquals("\"123\"", sw.toString());
    }
}