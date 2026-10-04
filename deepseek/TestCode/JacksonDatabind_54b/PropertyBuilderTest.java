package com.fasterxml.jackson.databind.ser;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.util.SimpleBeanPropertyDefinition;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class PropertyBuilderTest {

    private ObjectMapper mapper;
    private SerializerProvider prov;
    private BeanDescription beanDesc;
    private PropertyBuilder builder;

    @Before
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
        prov = mapper.getSerializerProvider();
        // Use a simple bean description for testing
        beanDesc = mapper.getSerializationConfig().introspect(mapper.constructType(SimpleBean.class));
        builder = new PropertyBuilder(prov, beanDesc);
    }

    // Helper class with various property configurations
    static class SimpleBean {
        public String name;
        @JsonUnwrapped
        public Address address;
        public int age;
        // transient field to test exclusion
        transient String temp;
    }

    static class Address {
        public String street;
        public String city;
    }

    // Test basic property writer creation
    @Test
    public void testBuildWriterForSimpleProperty() throws Exception {
        BeanPropertyDefinition propDef = findProperty("name");
        JsonSerializer<?> serializer = builder.buildWriter(propDef);
        assertNotNull("Serializer should not be null", serializer);
    }

    // Test property with @JsonUnwrapped (the bug context)
    @Test
    public void testBuildWriterForUnwrappedProperty() throws Exception {
        BeanPropertyDefinition propDef = findProperty("address");
        JsonSerializer<?> serializer = builder.buildWriter(propDef);
        assertNotNull("Unwrapped property should produce a serializer", serializer);
        // Verify it's an unwrapping serializer
        assertTrue("Should be an UnwrappingBeanSerializer",
                serializer.getClass().getName().contains("Unwrapping"));
    }

    // Test with null property definition
    @Test(expected = NullPointerException.class)
    public void testBuildWriterWithNullProperty() throws Exception {
        builder.buildWriter(null);
    }

    // Test with property that has no getter (only field)
    @Test
    public void testBuildWriterForFieldOnlyProperty() throws Exception {
        // age is a field with public visibility
        BeanPropertyDefinition propDef = findProperty("age");
        JsonSerializer<?> serializer = builder.buildWriter(propDef);
        assertNotNull("Field-only property should have serializer", serializer);
    }

    // Test with transient property (should be excluded)
    @Test
    public void testBuildWriterForTransientProperty() throws Exception {
        BeanPropertyDefinition propDef = findProperty("temp");
        // Transient properties are typically ignored; buildWriter may return null
        JsonSerializer<?> serializer = builder.buildWriter(propDef);
        assertNull("Transient property should not have serializer", serializer);
    }

    // Test buildProperty method
    @Test
    public void testBuildProperty() throws Exception {
        BeanPropertyDefinition propDef = findProperty("name");
        BeanPropertyWriter writer = builder.buildProperty(propDef);
        assertNotNull("BeanPropertyWriter should be created", writer);
        assertEquals("name", writer.getName());
    }

    // Test buildProperty with null definition
    @Test(expected = NullPointerException.class)
    public void testBuildPropertyWithNull() throws Exception {
        builder.buildProperty(null);
    }

    // Test buildProperty with unwrapped property
    @Test
    public void testBuildPropertyForUnwrapped() throws Exception {
        BeanPropertyDefinition propDef = findProperty("address");
        BeanPropertyWriter writer = builder.buildProperty(propDef);
        // For unwrapped, buildProperty may return null or a special writer
        // In bug 54, it might have thrown an exception; we test that it doesn't
        assertNotNull("Unwrapped property should produce a writer", writer);
    }

    // Test with empty property definition (no annotations)
    @Test
    public void testBuildWriterWithEmptyAnnotations() throws Exception {
        // Create a property definition with no annotations
        AnnotatedField field = beanDesc.findProperties().iterator().next().getField();
        BeanPropertyDefinition emptyDef = SimpleBeanPropertyDefinition.construct(
                mapper.getSerializationConfig(), field, "emptyProp");
        JsonSerializer<?> serializer = builder.buildWriter(emptyDef);
        assertNotNull("Property with no annotations should still have serializer", serializer);
    }

    // Test with custom filter
    @Test
    public void testBuildWriterWithFilter() throws Exception {
        // Set up a filter provider that excludes "name"
        SimpleFilterProvider filters = new SimpleFilterProvider();
        filters.addFilter("testFilter", SimpleBeanPropertyFilter.serializeAllExcept("name"));
        mapper.setFilterProvider(filters);
        // Recreate builder with new config
        prov = mapper.getSerializerProvider();
        builder = new PropertyBuilder(prov, beanDesc);
        BeanPropertyDefinition propDef = findProperty("name");
        JsonSerializer<?> serializer = builder.buildWriter(propDef);
        // With filter, the property might be excluded; serializer could be null
        // This tests the filter logic branch
        assertNull("Filtered property should not have serializer", serializer);
    }

    // Test with null accessor (no getter or field)
    @Test(expected = IllegalArgumentException.class)
    public void testBuildWriterWithNoAccessor() throws Exception {
        // Create a property definition with no accessor (simulate error)
        AnnotatedMember member = null;
        BeanPropertyDefinition badDef = SimpleBeanPropertyDefinition.construct(
                mapper.getSerializationConfig(), member, "bad");
        builder.buildWriter(badDef);
    }

    // Helper to find a property by name
    private BeanPropertyDefinition findProperty(String name) {
        for (BeanPropertyDefinition prop : beanDesc.findProperties()) {
            if (prop.getName().equals(name)) {
                return prop;
            }
        }
        throw new IllegalArgumentException("Property not found: " + name);
    }
}