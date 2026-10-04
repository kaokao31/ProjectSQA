package com.fasterxml.jackson.databind.deser.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.util.TokenBuffer;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class ExternalTypeHandlerTest {

    @Test
    public void testBuilderAndCopy() {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        assertNotNull(builder);

        // Test start() with null or empty properties
        ExternalTypeHandler handler = builder.build(null);
        assertNotNull(handler);

        // Test copy constructor through start() or builder
        ExternalTypeHandler copy = new ExternalTypeHandler(handler);
        assertNotNull(copy);
    }

    @Test
    public void testFirstTokenHandling() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser jp = mapper.getFactory().createParser("{}");
        jp.nextToken(); // move to START_OBJECT

        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        ExternalTypeHandler handler = builder.build(null);

        DeserializationContext ctxt = mapper.getDeserializationContext();
        
        // Test handleTypePropertyValue with non-matching property
        boolean handled = handler.handleTypePropertyValue(jp, ctxt, "nonExistent", null);
        assertFalse(handled);

        jp.close();
    }

    @Test
    public void testTokenBufferRecording() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser jp = mapper.getFactory().createParser("{\"type\":\"val\", \"data\":123}");
        
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        ExternalTypeHandler handler = builder.build(null);

        DeserializationContext ctxt = mapper.getDeserializationContext();

        // Advance parser
        while (jp.nextToken() != JsonToken.END_OBJECT && jp.getCurrentToken() != null) {
            handler.handlePropertyValue(jp, ctxt, jp.getCurrentName(), null);
        }

        jp.close();
    }

    @Test
    public void testCompleteMethodWithNullBean() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser jp = mapper.getFactory().createParser("{}");
        jp.nextToken();

        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        ExternalTypeHandler handler = builder.build(null);
        DeserializationContext ctxt = mapper.getDeserializationContext();

        Object bean = new Object();
        // Should handle gracefully even if no properties are configured
        Object result = handler.complete(jp, ctxt, bean);
        assertNotNull(result);

        jp.close();
    }
}