package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;

public class StringArrayDeserializerTest {

    @Test
    publictestDeserializeStandardArray() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "[\"a\", \"b\", \"c\"]";
        String[] result = mapper.readValue(json, String[].class);
        Assert.assertNotNull(result);
        Assert.assertEquals(3, result.length);
        Assert.assertEquals("a", result[0]);
        Assert.assertEquals("b", result[1]);
        Assert.assertEquals("c", result[2]);
    }

    @Test
    public void testDeserializeStringAsArrayDisabled() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, false);
        String json = "\"singleValue\"";
        try {
            mapper.readValue(json, String[].class);
            Assert.fail("Expected an exception because single value as array is disabled");
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("String[]"));
        }
    }

    @Test
    public void testDeserializeStringAsArrayEnabled() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        String json = "\"singleValue\"";
        String[] result = mapper.readValue(json, String[].class);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.length);
        Assert.assertEquals("singleValue", result[0]);
    }

    @Test
    public void testDeserializeNullStringAsArray() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        String json = "null";
        String[] result = mapper.readValue(json, String[].class);
        // Depending on configuration/deserializer behavior, null might map to null or empty array
        // Standard Jackson behavior for null token with ACCEPT_SINGLE_VALUE_AS_ARRAY or standard handling
        // Let's test standard deserializer directly if needed or via mapper
        JsonParser parser = mapper.getFactory().createParser("null");
        DeserializationContext ctxt = mapper.getDeserializationContext();
        StringArrayDeserializer deser = StringArrayDeserializer.instance;
        // Just exercising deserialize directly
        parser.nextToken();
        String[] res = deser.deserialize(parser, ctxt);
        // null token usually returns null for arrays unless handled differently
        Assert.assertNull(res);
    }

    @Test
    public void testDeserializeArrayWithNulls() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "[\"a\", null, \"c\"]";
        String[] result = mapper.readValue(json, String[].class);
        Assert.assertNotNull(result);
        Assert.assertEquals(3, result.length);
        Assert.assertEquals("a", result[0]);
        Assert.assertNull(result[1]);
        Assert.assertEquals("c", result[2]);
    }

    @Test
    public void testDeserializeEmptyArray() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "[]";
        String[] result = mapper.readValue(json, String[].class);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.length);
    }

    @Test
    public void testDeserializeWithCustomElementDeserializer() throws Exception {
        // Test constructing with a custom element deserializer
        JsonDeserializer<String> customElementDeser = new JsonDeserializer<String>() {
            @Override
            public String deserialize(JsonParser p, DeserializationContext ctxt) throws java.io.IOException {
                return p.getText() + "_custom";
            }
        };
        
        StringArrayDeserializer baseDeser = StringArrayDeserializer.instance;
        StringArrayDeserializer customDeser = new StringArrayDeserializer(baseDeser.getValueClass(), customElementDeser);
        
        ObjectMapper mapper = new ObjectMapper();
        // We can test the custom deserializer by invoking it directly via a parser
        String json = "[\"test\"]";
        JsonParser parser = mapper.getFactory().createParser(json);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        
        parser.nextToken(); // START_ARRAY
        String[] result = customDeser.deserialize(parser, ctxt);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.length);
        Assert.assertEquals("test_custom", result[0]);
    }

    @Test
    public void testDeserializeNonArrayHandling() throws Exception {
        // Directly test StringArrayDeserializer with unexpected token when ACCEPT_SINGLE_VALUE_AS_ARRAY is false
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, false);
        
        JsonParser parser = mapper.getFactory().createParser("123");
        DeserializationContext ctxt = mapper.getDeserializationContext();
        parser.nextToken();
        
        try {
            StringArrayDeserializer.instance.deserialize(parser, ctxt);
            Assert.fail("Expected exception for non-array token");
        } catch (Exception e) {
            Assert.assertNotNull(e);
        }
    }
}