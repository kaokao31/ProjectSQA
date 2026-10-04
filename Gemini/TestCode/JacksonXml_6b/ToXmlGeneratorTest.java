package com.fasterxml.jackson.dataformat.xml.ser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.util.XmlStringEncoder;
import org.codehaus.stax2.XMLStreamWriter2;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class ToXmlGeneratorTest {

    private IOContext ioContext;
    private ObjectMapper objectMapper;
    private StringWriter stringWriter;
    private XMLStreamWriter2 xmlStreamWriter;

    @Before
    public void setUp() throws Exception {
        ioContext = new IOContext(new BufferRecycler(), null, false);
        objectMapper = new ObjectMapper();
        stringWriter = new StringWriter();
        
        XMLOutputFactory xof = XMLOutputFactory.newFactory();
        XMLStreamWriter sw = xof.createXMLStreamWriter(stringWriter);
        xmlStreamWriter = (XMLStreamWriter2) sw;
    }

    @After
    public void tearDown() throws Exception {
        if (xmlStreamWriter != null) {
            try {
                xmlStreamWriter.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    @Test
    public void testInitializationAndBasicGetters() throws Exception {
        ToXmlGenerator gen = new ToXmlGenerator(
                ioContext, 0, 0, objectMapper, xmlStreamWriter
        );

        assertNotNull(gen.getCodec());
        assertNotNull(gen.getOutputTarget());
        assertEquals(0, gen.getFeatures());
        
        // Test high-level features if any
        gen.disable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
        assertFalse(gen.isEnabled(ToXmlGenerator.Feature.WRITE_XML_DECLARATION));
        gen.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
        assertTrue(gen.isEnabled(ToXmlGenerator.Feature.WRITE_XML_DECLARATION));
        
        gen.close();
        assertTrue(gen.isClosed());
    }

    @Test
    public void testNameHandlingAndStartEnd() throws Exception {
        ToXmlGenerator gen = new ToXmlGenerator(
                ioContext, 0, 0, objectMapper, xmlStreamWriter
        );

        gen.writeStartObject();
        gen.writeFieldName("rootElem");
        gen.writeString("testValue");
        gen.writeEndObject();
        gen.flush();
        gen.close();

        String xml = stringWriter.toString();
        assertTrue(xml.contains("rootElem"));
        assertTrue(xml.contains("testValue"));
    }

    @Test
    public void testWriteNull() throws Exception {
        ToXmlGenerator gen = new ToXmlGenerator(
                ioContext, 0, 0, objectMapper, xmlStreamWriter
        );

        gen.writeStartObject();
        gen.writeFieldName("nullElem");
        gen.writeNull();
        gen.writeEndObject();
        gen.close();

        assertTrue(stringWriter.toString().length() > 0);
    }

    @Test
    public void testWritePrimitivesAndNumbers() throws Exception {
        ToXmlGenerator gen = new ToXmlGenerator(
                ioContext, 0, 0, objectMapper, xmlStreamWriter
        );

        gen.writeStartObject();
        
        gen.writeFieldName("boolTrue");
        gen.writeBoolean(true);
        
        gen.writeFieldName("boolFalse");
        gen.writeBoolean(false);
        
        gen.writeFieldName("intVal");
        gen.writeNumber(42);
        
        gen.writeFieldName("longVal");
        gen.writeNumber(100L);
        
        gen.writeFieldName("doubleVal");
        gen.writeNumber(3.14);
        
        gen.writeFieldName("floatVal");
        gen.writeNumber(2.5f);
        
        gen.writeFieldName("bigDecimalVal");
        gen.writeNumber(new BigDecimal("123.456"));
        
        gen.writeFieldName("bigIntegerVal");
        gen.writeNumber(new BigInteger("9999"));
        
        gen.writeFieldName("strVal");
        gen.writeNumber("123");

        gen.writeEndObject();
        gen.close();

        String output = stringWriter.toString();
        assertTrue(output.contains("42"));
        assertTrue(output.contains("3.14"));
        assertTrue(output.contains("123.456"));
    }

    @Test
    public void testWriteRaw() throws Exception {
        ToXmlGenerator gen = new ToXmlGenerator(
                ioContext, 0, 0, objectMapper, xmlStreamWriter
        );

        gen.writeStartObject();
        gen.writeFieldName("rawElem");
        gen.writeRaw("<raw>data</raw>");
        gen.writeEndObject();
        gen.close();

        assertTrue(stringWriter.toString().contains("<raw>data</raw>"));
    }

    @Test
    public void testWriteBinary() throws Exception {
        ToXmlGenerator gen = new ToXmlGenerator(
                ioContext, 0, 0, objectMapper, xmlStreamWriter
        );

        gen.writeStartObject();
        gen.writeFieldName("binaryElem");
        byte[] data = "Hello World".getBytes("UTF-8");
        gen.writeBinary(data, 0, data.length);
        gen.writeEndObject();
        gen.close();

        assertTrue(stringWriter.toString().length() > 0);
    }

    @Test
    public void testOverrideNamespace() throws Exception {
        ToXmlGenerator gen = new ToXmlGenerator(
                ioContext, 0, 0, objectMapper, xmlStreamWriter
        );

        // Test namespace override mechanism if present
        gen.setNextName("http://example.com", "localName");
        gen.writeStartObject();
        gen.writeFieldName("field");
        gen.writeString("val");
        gen.writeEndObject();
        gen.close();

        assertTrue(stringWriter.toString().length() > 0);
    }

    @Test
    public void testCopyCurrentEvent() throws Exception {
        ToXmlGenerator gen = new ToXmlGenerator(
                ioContext, 0, 0, objectMapper, xmlStreamWriter
        );

        ObjectMapper mapper = new ObjectMapper();
        JsonParser jp = mapper.createParser("{\"a\":1}");
        jp.nextToken(); // START_OBJECT
        
        gen.copyCurrentEvent(jp);
        jp.nextToken(); // FIELD_NAME
        gen.copyCurrentEvent(jp);
        jp.nextToken(); // VALUE_NUMBER_INT
        gen.copyCurrentEvent(jp);
        
        gen.close();
        jp.close();
        assertTrue(stringWriter.toString().length() > 0);
    }

    @Test
    public void testCopyCurrentStructure() throws Exception {
        ToXmlGenerator gen = new ToXmlGenerator(
                ioContext, 0, 0, objectMapper, xmlStreamWriter
        );

        ObjectMapper mapper = new ObjectMapper();
        JsonParser jp = mapper.createParser("{\"a\":[1,2]}");
        
        gen.copyCurrentStructure(jp);
        
        gen.close();
        jp.close();
        assertTrue(stringWriter.toString().length() > 0);
    }

    @Test
    public void testSerializationWithRootName() throws Exception {
        ToXmlGenerator gen = new ToXmlGenerator(
                ioContext, 0, 0, objectMapper, xmlStreamWriter
        );

        gen.setNextIsRoot(true);
        gen.writeStartObject();
        gen.writeFieldName("child");
        gen.writeString("val");
        gen.writeEndObject();
        gen.close();

        assertTrue(stringWriter.toString().length() > 0);
    }
}