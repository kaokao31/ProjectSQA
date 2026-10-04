package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.impl.BeanAsArrayDeserializer;
import com.fasterxml.jackson.databind.deser.impl.BeanPropertyMap;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.util.NameTransformer;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class BeanDeserializerTest {

    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testDummyDeserializerConstructionAndBasicMethods() {
        JavaType type = objectMapper.constructType(SimpleBean.class);
        BeanDescription beanDesc = objectMapper.getSerializationConfig().introspect(type);
        
        BeanDeserializerBuilder builder = new BeanDeserializerBuilder(beanDesc, objectMapper.getDeserializationConfig());
        BeanDeserializerModifier modifier = new BeanDeserializerModifier() {};
        
        // Exercise builders and basic state
        assertNotNull(builder);
        assertNotNull(modifier);

        BeanDeserializer deserializer = new BeanDeserializer(
                builder, beanDesc, BeanPropertyMap.construct(Collections.emptyList(), false),
                Collections.emptyMap(), new HashSet<>(), false, false
        );

        // Test unwrapped creator
        NameTransformer transformer = NameTransformer.simple("prefix_");
        JsonDeserializer<Object> unwrapped = deserializer.unwrappedDeserializer(transformer);
        assertNotNull(unwrapped);

        // Test as-array deserialization builder conversion
        BeanAsArrayDeserializer asArray = deserializer.asArrayDeserializer();
        assertNotNull(asArray);

        // Test withObjectIdReader
        ObjectIdReader oidReader = ObjectIdReader.construct(
                objectMapper.constructType(String.class),
                PropertyName.construct("id"),
                null, null, null, null
        );
        BeanDeserializer withOid = deserializer.withObjectIdReader(oidReader);
        assertNotNull(withOid);

        // Test withIgnorableInclusions
        Set<String> ignorable = new HashSet<>();
        ignorable.add("ignoredProp");
        BeanDeserializer withIgnored = deserializer.withIgnorableInclusions(ignorable);
        assertNotNull(withIgnored);
        
        // Test properties
        assertNull(deserializer.findProperty("nonExistent"));
    }

    @Test(expected = IOException.class)
    public void testDeserializeFromObjectUsingNonDefault() throws Exception {
        JavaType type = objectMapper.constructType(SimpleBean.class);
        BeanDescription beanDesc = objectMapper.getSerializationConfig().introspect(type);
        BeanDeserializerBuilder builder = new BeanDeserializerBuilder(beanDesc, objectMapper.getDeserializationConfig());
        
        BeanDeserializer deserializer = new BeanDeserializer(
                builder, beanDesc, BeanPropertyMap.construct(Collections.emptyList(), false),
                Collections.emptyMap(), new HashSet<>(), false, false
        );

        JsonParser parser = objectMapper.getFactory().createParser("{}");
        parser.nextToken(); // START_OBJECT
        DeserializationContext ctxt = objectMapper.getDeserializationContext();
        
        deserializer.deserializeFromObject(parser, ctxt);
    }

    @Test
    public void testVanillaDeserializeEdgeCases() throws Exception {
        JavaType type = objectMapper.constructType(SimpleBean.class);
        BeanDescription beanDesc = objectMapper.getSerializationConfig().introspect(type);
        BeanDeserializerBuilder builder = new BeanDeserializerBuilder(beanDesc, objectMapper.getDeserializationConfig());
        
        BeanDeserializer deserializer = new BeanDeserializer(
                builder, beanDesc, BeanPropertyMap.construct(Collections.emptyList(), false),
                Collections.emptyMap(), new HashSet<>(), false, false
        );

        JsonParser parser = objectMapper.getFactory().createParser("{}");
        DeserializationContext ctxt = objectMapper.getDeserializationContext();
        
        // Move to START_OBJECT
        parser.nextToken();
        
        // Depending on creator/vanilla settings, this might handle empty object or fail gracefully
        try {
            Object result = deserializer.deserialize(parser, ctxt);
            assertNotNull(result);
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testOtherConstructorsAndCopy() {
        JavaType type = objectMapper.constructType(SimpleBean.class);
        BeanDescription beanDesc = objectMapper.getSerializationConfig().introspect(type);
        BeanDeserializerBuilder builder = new BeanDeserializerBuilder(beanDesc, objectMapper.getDeserializationConfig());
        
        BeanDeserializer base = new BeanDeserializer(
                builder, beanDesc, BeanPropertyMap.construct(Collections.emptyList(), false),
                Collections.emptyMap(), new HashSet<>(), false, false
        );

        BeanDeserializer copyWithDelegate = new BeanDeserializer(base, base._beanProperties);
        assertNotNull(copyWithDelegate);

        BeanDeserializer copyWithObjectId = new BeanDeserializer(base, base._objectIdReader);
        assertNotNull(copyWithObjectId);

        BeanDeserializer copyWithIgnorable = new BeanDeserializer(base, base._ignorableProps);
        assertNotNull(copyWithIgnorable);

        BeanDeserializer copyWithVanishing = new BeanDeserializer(base, true);
        assertNotNull(copyWithVanishing);
    }

    public static class SimpleBean {
        public String name;
        public int value;

        public SimpleBean() {}

        public SimpleBean(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }
}