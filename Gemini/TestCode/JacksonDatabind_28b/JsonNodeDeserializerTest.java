package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class JsonNodeDeserializerTest {

    private ObjectMapper objectMapper;
    private DeserializationContext deserializationContext;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        deserializationContext = objectMapper.getDeserializationContext();
    }

    @After
    public void tearDown() {
        objectMapper = null;
        deserializationContext = null;
    }

    @Test
    public void testGetDeserializerForObject() {
        JsonNodeDeserializer deserializer = JsonNodeDeserializer.getDeserializer(ObjectNode.class);
        assertNotNull(deserializer);
    }

    @Test
    public void testGetDeserializerForArray() {
        JsonNodeDeserializer deserializer = JsonNodeDeserializer.getDeserializer(ArrayNode.class);
        assertNotNull(deserializer);
    }

    @Test
    public void testGetDeserializerForGenericJsonNode() {
        JsonNodeDeserializer deserializer = JsonNodeDeserializer.getDeserializer(JsonNode.class);
        assertNotNull(deserializer);
    }

    @Test
    public void testDeserializeObject() throws IOException {
        String json = "{\"field\":\"value\"}";
        JsonParser jp = objectMapper.getFactory().createParser(json);
        jp.nextToken(); // move to START_OBJECT

        JsonNodeDeserializer deserializer = (JsonNodeDeserializer) JsonNodeDeserializer.getDeserializer(ObjectNode.class);
        JsonNode result = deserializer.deserialize(jp, deserializationContext);

        assertNotNull(result);
        assertTrue(result.isObject());
        assertEquals("value", result.path("field").asText());
    }

    @Test
    public void testDeserializeArray() throws IOException {
        String json = "[\"element1\", \"element2\"]";
        JsonParser jp = objectMapper.getFactory().createParser(json);
        jp.nextToken(); // move to START_ARRAY

        JsonNodeDeserializer deserializer = (JsonNodeDeserializer) JsonNodeDeserializer.getDeserializer(ArrayNode.class);
        JsonNode result = deserializer.deserialize(jp, deserializationContext);

        assertNotNull(result);
        assertTrue(result.isArray());
        assertEquals(2, result.size());
        assertEquals("element1", result.get(0).asText());
        assertEquals("element2", result.get(1).asText());
    }

    @Test
    public void testDeserializeObjectUsingDeserializeWithType() throws IOException {
        String json = "{\"key\":123}";
        JsonParser jp = objectMapper.getFactory().createParser(json);
        jp.nextToken();

        JsonNodeDeserializer deserializer = (JsonNodeDeserializer) JsonNodeDeserializer.getDeserializer(ObjectNode.class);
        JsonNode result = (JsonNode) deserializer.deserializeWithType(jp, deserializationContext, null);

        assertNotNull(result);
        assertTrue(result.isObject());
        assertEquals(123, result.path("key").asInt());
    }

    @Test
    public void testNullValueHandling() {
        JsonNodeDeserializer deserializer = (JsonNodeDeserializer) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        assertNull(deserializer.getNullValue());
    }

    @Test
    public void testCachable() {
        JsonNodeDeserializer deserializer = (JsonNodeDeserializer) JsonNodeDeserializer.getDeserializer(JsonNode.class);
        assertTrue(deserializer.isCachable());
    }
}