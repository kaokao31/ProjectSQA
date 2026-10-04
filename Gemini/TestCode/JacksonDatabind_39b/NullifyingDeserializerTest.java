package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

public class NullifyingDeserializerTest {

    @Test
    public void testInstance() {
        Assert.assertNotNull(NullifyingDeserializer.instance);
    }

    @Test
    public void testDeserialize() throws Exception {
        NullifyingDeserializer deserializer = new NullifyingDeserializer();
        ObjectMapper mapper = new ObjectMapper();
        JsonParser jp = mapper.getFactory().createParser("{\"a\": 1, \"b\": [1, 2, 3]}");
        DeserializationContext ctxt = mapper.getDeserializationContext();

        // Advance to START_OBJECT or let the deserializer handle it
        jp.nextToken(); // should be START_OBJECT

        Object result = deserializer.deserialize(jp, ctxt);
        Assert.assertNull(result);
    }

    @Test
    public void testDeserializeWithType() throws Exception {
        NullifyingDeserializer deserializer = new NullifyingDeserializer();
        ObjectMapper mapper = new ObjectMapper();
        JsonParser jp = mapper.getFactory().createParser("\"some string\"");
        DeserializationContext ctxt = mapper.getDeserializationContext();

        jp.nextToken(); // VALUE_STRING

        Object result = deserializer.deserializeWithType(jp, ctxt, null);
        Assert.assertNull(result);
    }

    @Test
    public void testCreateContextual() throws Exception {
        NullifyingDeserializer deserializer = new NullifyingDeserializer();
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        JsonDeserializer<?> contextual = deserializer.createContextual(ctxt, (BeanProperty) null);
        Assert.assertSame(deserializer, contextual);
    }
}