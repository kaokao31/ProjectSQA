package com.fasterxml.jackson.databind.ser.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.*;

public class WritableObjectIdTest {

    private ObjectMapper mapper;
    private StringWriter stringWriter;
    private JsonGenerator jsonGenerator;
    private SerializerProvider serializerProvider;
    private SimpleObjectIdGenerator generator;
    private WritableObjectId writableObjectId;
    private ObjectIdWriter objectIdWriter;
    private Object pojo;

    @Before
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
        stringWriter = new StringWriter();
        jsonGenerator = mapper.getFactory().createGenerator(stringWriter);
        serializerProvider = mapper.getSerializerProvider();
        generator = new SimpleObjectIdGenerator();
        writableObjectId = new WritableObjectId(generator);
        JavaType idType = TypeFactory.defaultInstance().constructType(Object.class);
        objectIdWriter = ObjectIdWriter.construct(idType, "id", generator, false);
        pojo = new Object();
    }

    @Test
    public void testConstructorAndGetters() {
        assertNotNull(writableObjectId.generator);
        assertNull(writableObjectId.id);
        assertNull(writableObjectId.idProperty);
    }

    @Test
    public void testGenerateId() {
        Object id = writableObjectId.generateId(pojo);
        assertNotNull(id);
        assertNotNull(writableObjectId.id);
        assertEquals(id, writableObjectId.id);
    }

    @Test(expected = NullPointerException.class)
    public void testGenerateIdWithNullPojo() {
        writableObjectId.generateId(null);
    }

    @Test
    public void testWriteAsIdWithNullId() throws IOException {
        // id is null initially, so writeAsId should generate and write it
        writableObjectId.writeAsId(jsonGenerator, serializerProvider, objectIdWriter);
        assertNotNull(writableObjectId.id);
        String output = stringWriter.toString();
        assertTrue(output.contains("\"id\""));
    }

    @Test
    public void testWriteAsIdWithExistingId() throws IOException {
        // Set an id first
        Object id = writableObjectId.generateId(pojo);
        stringWriter.getBuffer().setLength(0); // clear previous output
        writableObjectId.writeAsId(jsonGenerator, serializerProvider, objectIdWriter);
        String output = stringWriter.toString();
        assertTrue(output.contains("\"id\""));
        // The id should be the same as generated
        assertTrue(output.contains(id.toString()));
    }

    @Test
    public void testWriteId() throws IOException {
        // writeId should always generate and write the id
        writableObjectId.writeId(jsonGenerator, serializerProvider, objectIdWriter);
        assertNotNull(writableObjectId.id);
        String output = stringWriter.toString();
        assertTrue(output.contains("\"id\""));
    }

    @Test(expected = NullPointerException.class)
    public void testWriteAsIdWithNullGenerator() throws IOException {
        WritableObjectId badId = new WritableObjectId(null);
        badId.writeAsId(jsonGenerator, serializerProvider, objectIdWriter);
    }

    @Test(expected = NullPointerException.class)
    public void testWriteAsIdWithNullProvider() throws IOException {
        writableObjectId.writeAsId(jsonGenerator, null, objectIdWriter);
    }

    @Test(expected = NullPointerException.class)
    public void testWriteAsIdWithNullGen() throws IOException {
        writableObjectId.writeAsId(null, serializerProvider, objectIdWriter);
    }

    @Test
    public void testWriteAsIdWithNullWriter() throws IOException {
        // Should not throw, but may produce no output
        writableObjectId.writeAsId(jsonGenerator, serializerProvider, null);
        String output = stringWriter.toString();
        assertTrue(output.isEmpty());
    }

    @Test
    public void testWriteIdWithNullWriter() throws IOException {
        writableObjectId.writeId(jsonGenerator, serializerProvider, null);
        String output = stringWriter.toString();
        assertTrue(output.isEmpty());
    }

    @Test
    public void testToString() {
        assertNotNull(writableObjectId.toString());
        assertTrue(writableObjectId.toString().contains("generator"));
    }

    @Test
    public void testWriteAsIdWithAlwaysAsId() throws IOException {
        // Create ObjectIdWriter with alwaysAsId = true
        ObjectIdWriter alwaysWriter = ObjectIdWriter.construct(
                TypeFactory.defaultInstance().constructType(Object.class),
                "id", generator, true);
        writableObjectId.writeAsId(jsonGenerator, serializerProvider, alwaysWriter);
        String output = stringWriter.toString();
        assertTrue(output.contains("\"id\""));
    }

    @Test
    public void testWriteIdWithAlwaysAsId() throws IOException {
        ObjectIdWriter alwaysWriter = ObjectIdWriter.construct(
                TypeFactory.defaultInstance().constructType(Object.class),
                "id", generator, true);
        writableObjectId.writeId(jsonGenerator, serializerProvider, alwaysWriter);
        String output = stringWriter.toString();
        assertTrue(output.contains("\"id\""));
    }

    @Test
    public void testGenerateIdCalledTwice() {
        Object id1 = writableObjectId.generateId(pojo);
        Object id2 = writableObjectId.generateId(pojo);
        // Should return the same id (cached)
        assertSame(id1, id2);
    }

    @Test
    public void testWriteAsIdWithDifferentPojo() throws IOException {
        // Generate id for one pojo, then writeAsId with a different pojo
        writableObjectId.generateId(pojo);
        Object otherPojo = new Object();
        // writeAsId should use the existing id, not generate a new one
        writableObjectId.writeAsId(jsonGenerator, serializerProvider, objectIdWriter);
        String output = stringWriter.toString();
        // The id should be the one generated for the first pojo
        assertTrue(output.contains(writableObjectId.id.toString()));
    }
}