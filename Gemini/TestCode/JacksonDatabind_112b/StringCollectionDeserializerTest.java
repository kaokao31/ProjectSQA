package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

public class StringCollectionDeserializerTest {

    @Test
    public void testContextualAndCreation() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(java.util.List.class, String.class);
        
        JsonDeserializer<?> valueDeser = mapper.getDeserializationContext().findContextualValueDeserializer(
                TypeFactory.defaultInstance().constructType(String.class), null);
        
        ValueInstantiator instantiator = mapper.getDeserializationConfig().getBase64Variant(); // just a placeholder or default instantiator
        ValueInstantiator defaultInstantiator = mapper.getDeserializationConfig().getAnnotationIntrospector() != null ? 
                mapper.getDeserializationConfig().getAnnotationIntrospector().findValueInstantiator(
                        mapper.getSerializationConfig().introspect(type).getClassInfo(), null) : null;

        // Use standard constructor for StringCollectionDeserializer
        // public StringCollectionDeserializer(JavaType collectionType, JsonDeserializer<?> valueDeser, ValueInstantiator valueInstantiator)
        StringCollectionDeserializer des1 = new StringCollectionDeserializer(type, valueDeser, null);
        
        // Test createWithDelegate
        JsonDeserializer<?> delegateDeser = mapper.getDeserializationContext().findContextualValueDeserializer(
                TypeFactory.defaultInstance().constructType(String.class), null);
        StringCollectionDeserializer des2 = new StringCollectionDeserializer(type, valueDeser, null, delegateDeser, null);

        Assert.assertNotNull(des1);
        Assert.assertNotNull(des2);

        // Test createContextual with null property
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JsonDeserializer<?> contextual1 = des1.createContextual(ctxt, null);
        Assert.assertNotNull(contextual1);

        // Test createContextual with property and JSON format override (e.g. EmptyStringAsNull)
        BeanProperty.Std property = new BeanProperty.Std(
                com.fasterxml.jackson.databind.util.NameTransformer.NOP.transform("test"),
                type, null, null, null,
                JsonFormat.Value.empty(),
                com.fasterxml.jackson.databind.PropertyMetadata.STD_REQUIRED
        );
        
        JsonDeserializer<?> contextual2 = des1.createContextual(ctxt, property);
        Assert.assertNotNull(contextual2);
    }

    @Test
    public void testFindContentDeserializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(java.util.List.class, String.class);
        
        JsonDeserializer<?> valueDeser = mapper.getDeserializationContext().findContextualValueDeserializer(
                TypeFactory.defaultInstance().constructType(String.class), null);
        
        StringCollectionDeserializer des = new StringCollectionDeserializer(type, valueDeser, null);
        
        // Test with a property having content deserializer annotation
        BeanProperty.Std property = new BeanProperty.Std(
                com.fasterxml.jackson.databind.util.NameTransformer.NOP.transform("test"),
                type, null, null, null,
                JsonFormat.Value.empty(),
                com.fasterxml.jackson.databind.PropertyMetadata.STD_REQUIRED
        );
        
        DeserializationContext ctxt = mapper.getDeserializationContext();
        // Should handle gracefully even if no annotation is present
        JsonDeserializer<?> contextual = des.createContextual(ctxt, property);
        Assert.assertNotNull(contextual);
    }
}