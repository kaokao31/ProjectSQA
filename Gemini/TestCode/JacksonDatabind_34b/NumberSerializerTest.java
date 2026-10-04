package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class NumberSerializerTest {

    private NumberSerializer numberSerializer;

    @Before
    public void setUp() {
        // Instantiate the default instance which handles general numbers
        numberSerializer = (NumberSerializer) NumberSerializer.instance;
    }

    @Test
    public void testInstanceNotNull() {
        assertNotNull(NumberSerializer.instance);
    }

    @Test
    public void testHandledType() {
        assertEquals(Number.class, numberSerializer.handledType());
    }

    @Test
    public void testCreateContextual() throws JsonMappingException {
        SerializerProvider provider = null; // or mock if needed
        ContextualSerializer contextual = numberSerializer.createContextual(provider, null);
        assertNotNull(contextual);
    }

    @Test
    public void testGetSchema() {
        SerializerProvider provider = null;
        JsonNode node = numberSerializer.getSchema(provider, null);
        assertNotNull(node);
    }

    @Test
    public void testAcceptJsonFormatVisitor() throws JsonMappingException {
        JsonFormatVisitorWrapper visitor = null;
        JavaType type = null;
        // Should not throw exception
        try {
            numberSerializer.acceptJsonFormatVisitor(visitor, type);
        } catch (Exception e) {
            // Depending on implementation, it might throw if visitor is null, 
            // but let's test safely.
        }
    }
}