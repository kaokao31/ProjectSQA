package com.fasterxml.jackson.databind.node;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

public class POJONodeTest {

    // Dummy class that implements JsonSerializable for testing serialization paths
    static class DummySerializable implements JsonSerializable {
        private final String value;

        public DummySerializable(String value) {
            this.value = value;
        }

        @Override
        public void serialize(JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value);
        }

        @Override
        public void serializeWithType(JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
            gen.writeStartObject();
            gen.writeStringField("type", "DummySerializable");
            gen.writeStringField("value", value);
            gen.writeEndObject();
        }
    }

    @Test
    public void testNullPojo() {
        POJONode node = new POJONode(null);
        Assert.assertNull(node.getPojoValue());
        Assert.assertEquals(JsonNodeType.POJO, node.getNodeType());
        Assert.assertEquals("null", node.asText());
        Assert.assertEquals("", node.asText("default"));
        Assert.assertFalse(node.asBoolean(true));
        Assert.assertEquals(0, node.asInt(5));
        Assert.assertEquals(0L, node.asLong(5L));
        Assert.assertEquals(0.0, node.asDouble(5.0), 0.001);
    }

    @Test
    public void testStringPojo() {
        POJONode node = new POJONode("Hello World");
        Assert.assertEquals("Hello World", node.getPojoValue());
        Assert.assertEquals(JsonNodeType.POJO, node.getNodeType());
        Assert.assertEquals("Hello World", node.asText());
        Assert.assertEquals("Hello World", node.asText("default"));
    }

    @Test
    public void testEqualsAndHashCode() {
        Object obj1 = "test";
        Object obj2 = "test";
        Object obj3 = "different";

        POJONode node1 = new POJONode(obj1);
        POJONode node2 = new POJONode(obj2);
        POJONode node3 = new POJONode(obj3);
        POJONode nodeNull1 = new POJONode(null);
        POJONode nodeNull2 = new POJONode(null);

        Assert.assertTrue(node1.equals(node2));
        Assert.assertFalse(node1.equals(node3));
        Assert.assertFalse(node1.equals(null));
        Assert.assertFalse(node1.equals("some string"));
        Assert.assertTrue(nodeNull1.equals(nodeNull2));
        Assert.assertFalse(node1.equals(nodeNull1));

        Assert.assertEquals(node1.hashCode(), node2.hashCode());
        Assert.assertEquals(nodeNull1.hashCode(), nodeNull2.hashCode());
    }

    @Test
    public void testSerializeWithJsonSerializable() throws IOException {
        DummySerializable dummy = new DummySerializable("serialize-me");
        POJONode node = new POJONode(dummy);

        JsonFactoryWrapper factoryWrapper = new JsonFactoryWrapper();
        JsonGenerator gen = factoryWrapper.generator;
        DefaultSerializerProvider.Impl serializers = factoryWrapper.serializers;

        node.serialize(gen, serializers);
        gen.flush();
        
        Assert.assertEquals("\"serialize-me\"", factoryWrapper.writer.toString());
    }

    @Test
    public void testSerializeWithNullPojo() throws IOException {
        POJONode node = new POJONode(null);

        JsonFactoryWrapper factoryWrapper = new JsonFactoryWrapper();
        JsonGenerator gen = factoryWrapper.generator;
        DefaultSerializerProvider.Impl serializers = factoryWrapper.serializers;

        node.serialize(gen, serializers);
        gen.flush();
        
        Assert.assertEquals("null", factoryWrapper.writer.toString());
    }

    @Test
    public void testSerializeWithStandardObject() throws IOException {
        POJONode node = new POJONode(12345);

        JsonFactoryWrapper factoryWrapper = new JsonFactoryWrapper();
        JsonGenerator gen = factoryWrapper.generator;
        DefaultSerializerProvider.Impl serializers = factoryWrapper.serializers;

        node.serialize(gen, serializers);
        gen.flush();
        
        // Standard object serialization relies on SerializerProvider which by default writes numbers or uses default serialization
        // Depending on provider configuration, but let's verify it doesn't throw and writes something
        Assert.assertTrue(factoryWrapper.writer.toString().length() > 0);
    }

    // Helper class to setup Jackson JSON generator and provider for testing serialize methods
    private static class JsonFactoryWrapper {
        final StringWriter writer;
        final JsonGenerator generator;
        final DefaultSerializerProvider.Impl serializers;

        JsonFactoryWrapper() {
            try {
                writer = new StringWriter();
                com.fasterxml.jackson.core.JsonFactory f = new com.fasterxml.jackson.core.JsonFactory();
                generator = f.createGenerator(writer);
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                serializers = (DefaultSerializerProvider.Impl) mapper.getSerializerProviderInstance();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}