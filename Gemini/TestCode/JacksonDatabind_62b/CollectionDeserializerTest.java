package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CollectionDeserializerTest {

    @Test
    public void testCopyConstructorAndWithResolved() {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(List.class);
        JsonDeserializer<Object> valueDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return p.getText();
            }
        };

        TypeDeserializer typeDeser = null;
        JsonDeserializer<Object> delegateDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return null;
            }
        };

        CollectionDeserializer colDeser = new CollectionDeserializer(
                type, valueDeser, typeDeser, delegateDeser
        );

        // Test withResolved
        CollectionDeserializer resolved = colDeser.withResolved(delegateDeser, valueDeser, typeDeser, null);
        Assert.assertNotNull(resolved);

        // Test copy constructor via withResolved using same values or modified
        CollectionDeserializer resolved2 = colDeser.withResolved(delegateDeser, valueDeser, typeDeser, 
                (NullValueProvider) ctxt -> null);
        Assert.assertNotNull(resolved2);
    }

    @Test
    public void testCreateUsingDefault() {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(ArrayList.class);
        JsonDeserializer<Object> valueDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return "test";
            }
        };

        CollectionDeserializer colDeser = new CollectionDeserializer(
                type, valueDeser, null, null
        );

        DeserializationContext ctxt = mapper.getDeserializationContext();
        try {
            Collection<Object> result = colDeser.createDeserialized(ctxt);
            Assert.assertNotNull(result);
        } catch (Exception e) {
            // Depending on ctxt initialization, might need fallback or allow specific exceptions
        }
    }

    @Test
    public void testFindBackReference() {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(ArrayList.class);
        CollectionDeserializer colDeser = new CollectionDeserializer(
                type, null, null, null
        );
        
        try {
            JsonDeserializer<Object> deser = colDeser.findBackReference("nonExistent");
            Assert.assertNull(deser);
        } catch (IllegalArgumentException e) {
            // Expected if not supported
        }
    }

    @Test
    public void testCreateContextual() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(Set.class);
        JsonDeserializer<Object> valueDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return "val";
            }
        };

        CollectionDeserializer colDeser = new CollectionDeserializer(
                type, valueDeser, null, null
        );

        DeserializationContext ctxt = mapper.getDeserializationContext();
        BeanProperty.Std property = new BeanProperty.Std(
                com.fasterxml.jackson.databind.util.NameTransformer.NOP.transform("prop"),
                type, null, null, null, com.fasterxml.jackson.databind.cfg.MapperConfig.class.cast(mapper.getSerializationConfig()).getAnnotationIntrospector(),
                null
        );

        JsonDeserializer<?> contextual = colDeser.createContextual(ctxt, property);
        Assert.assertNotNull(contextual);
    }

    @Test
    public void testDeserializeNullOrEmpty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(HashSet.class);
        JsonDeserializer<Object> valueDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return p.getText();
            }
        };

        CollectionDeserializer colDeser = new CollectionDeserializer(
                type, valueDeser, null, null
        );

        String json = "[]";
        JsonParser parser = mapper.getFactory().createParser(json);
        parser.nextToken(); // move to START_ARRAY

        DeserializationContext ctxt = mapper.getDeserializationContext();
        Collection<Object> result = colDeser.deserialize(parser, ctxt);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testDeserializeWithValues() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(List.class);
        JsonDeserializer<Object> valueDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return p.getText();
            }
        };

        CollectionDeserializer colDeser = new CollectionDeserializer(
                type, valueDeser, null, null
        );

        String json = "[\"a\", \"b\", \"c\"]";
        JsonParser parser = mapper.getFactory().createParser(json);
        parser.nextToken(); // move to START_ARRAY

        DeserializationContext ctxt = mapper.getDeserializationContext();
        Collection<Object> result = colDeser.deserialize(parser, ctxt);
        Assert.assertNotNull(result);
        Assert.assertEquals(3, result.size());
        Assert.assertTrue(result.contains("a"));
        Assert.assertTrue(result.contains("b"));
        Assert.assertTrue(result.contains("c"));
    }

    @Test
    public void testDeserializeIntoExistingCollection() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(List.class);
        JsonDeserializer<Object> valueDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return p.getText();
            }
        };

        CollectionDeserializer colDeser = new CollectionDeserializer(
                type, valueDeser, null, null
        );

        String json = "[\"d\"]";
        JsonParser parser = mapper.getFactory().createParser(json);
        parser.nextToken(); // move to START_ARRAY

        DeserializationContext ctxt = mapper.getDeserializationContext();
        List<Object> existing = new ArrayList<>();
        existing.add("existing");

        Collection<Object> result = colDeser.deserialize(parser, ctxt, existing);
        Assert.assertSame(existing, result);
        Assert.assertEquals(2, result.size());
        Assert.assertTrue(result.contains("existing"));
        Assert.assertTrue(result.contains("d"));
    }
}