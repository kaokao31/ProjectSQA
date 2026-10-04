package com.fasterxml.jackson.databind.deser.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.impl.ExternalTypeHandler;
import com.fasterxml.jackson.databind.deser.impl.ExternalTypeHandler.Builder;
import com.fasterxml.jackson.databind.deser.impl.ExternalTypeHandler.ExtTypedProperty;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for ExternalTypeHandler, targeting high coverage and fault detection.
 * Based on JacksonDatabind bug #29 context.
 */
public class ExternalTypeHandlerTest {

    private DeserializationContext ctxt;
    private JsonParser p;
    private SettableBeanProperty property;
    private ExtTypedProperty extTypedProperty;
    private Builder builder;

    @Before
    public void setUp() {
        ctxt = mock(DeserializationContext.class);
        p = mock(JsonParser.class);
        property = mock(SettableBeanProperty.class);
        extTypedProperty = mock(ExtTypedProperty.class);
        builder = new Builder();
    }

    // --- Builder tests ---

    @Test
    public void testBuilderAddProperty() {
        builder.addExternal(extTypedProperty, property);
        ExternalTypeHandler handler = builder.build();
        assertNotNull("Handler should be built", handler);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderAddNullProperty() {
        builder.addExternal(null, property);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderAddNullBeanProperty() {
        builder.addExternal(extTypedProperty, null);
    }

    @Test
    public void testBuilderBuildEmpty() {
        ExternalTypeHandler handler = builder.build();
        assertNotNull("Empty handler should be built", handler);
    }

    // --- ExternalTypeHandler constructor tests ---

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullProperties() {
        new ExternalTypeHandler(null, null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullTypeIds() {
        List<ExtTypedProperty> props = new ArrayList<>();
        props.add(extTypedProperty);
        new ExternalTypeHandler(props, null, null, null);
    }

    // --- start() method tests ---

    @Test
    public void testStartReturnsNewInstance() throws IOException {
        ExternalTypeHandler handler = builder.build();
        ExternalTypeHandler started = handler.start();
        assertNotNull("start() should return a new handler", started);
        assertNotSame("Should be different instance", handler, started);
    }

    // --- handleProperty() tests ---

    @Test
    public void testHandlePropertyWithTypeId() throws IOException {
        // Setup: property name matches type property name
        when(extTypedProperty.getTypePropertyName()).thenReturn("typeId");
        when(p.getCurrentName()).thenReturn("typeId");
        when(p.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(p.getText()).thenReturn("MyType");

        builder.addExternal(extTypedProperty, property);
        ExternalTypeHandler handler = builder.build();
        ExternalTypeHandler started = handler.start();

        boolean handled = started.handleProperty(p, ctxt);
        assertTrue("Type property should be handled", handled);
    }

    @Test
    public void testHandlePropertyWithValueProperty() throws IOException {
        when(extTypedProperty.getTypePropertyName()).thenReturn("typeId");
        when(extTypedProperty.getPropertyName()).thenReturn("value");
        when(p.getCurrentName()).thenReturn("value");
        when(p.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(p.getText()).thenReturn("someValue");

        builder.addExternal(extTypedProperty, property);
        ExternalTypeHandler handler = builder.build();
        ExternalTypeHandler started = handler.start();

        boolean handled = started.handleProperty(p, ctxt);
        assertTrue("Value property should be handled", handled);
    }

    @Test
    public void testHandlePropertyUnknownProperty() throws IOException {
        when(p.getCurrentName()).thenReturn("unknown");
        when(p.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);

        builder.addExternal(extTypedProperty, property);
        ExternalTypeHandler handler = builder.build();
        ExternalTypeHandler started = handler.start();

        boolean handled = started.handleProperty(p, ctxt);
        assertFalse("Unknown property should not be handled", handled);
    }

    @Test
    public void testHandlePropertyNullToken() throws IOException {
        when(p.getCurrentName()).thenReturn("typeId");
        when(p.getCurrentToken()).thenReturn(null);

        builder.addExternal(extTypedProperty, property);
        ExternalTypeHandler handler = builder.build();
        ExternalTypeHandler started = handler.start();

        boolean handled = started.handleProperty(p, ctxt);
        assertFalse("Null token should not be handled", handled);
    }

    @Test(expected = IOException.class)
    public void testHandlePropertyDuplicateTypeId() throws IOException {
        when(extTypedProperty.getTypePropertyName()).thenReturn("typeId");
        when(p.getCurrentName()).thenReturn("typeId");
        when(p.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(p.getText()).thenReturn("MyType");

        builder.addExternal(extTypedProperty, property);
        ExternalTypeHandler handler = builder.build();
        ExternalTypeHandler started = handler.start();

        started.handleProperty(p, ctxt); // first call
        started.handleProperty(p, ctxt); // second call should throw
    }

    // --- complete() method tests ---

    @Test
    public void testCompleteWithBothProperties() throws Exception {
        when(extTypedProperty.getTypePropertyName()).thenReturn("typeId");
        when(extTypedProperty.getPropertyName()).thenReturn("value");
        when(extTypedProperty.getProperty()).thenReturn(property);
        when(property.deserializeAndSet(any(JsonParser.class), any(DeserializationContext.class), any())).thenReturn(null);

        builder.addExternal(extTypedProperty, property);
        ExternalTypeHandler handler = builder.build();
        ExternalTypeHandler started = handler.start();

        // Simulate handling type and value
        when(p.getCurrentName()).thenReturn("typeId");
        when(p.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(p.getText()).thenReturn("MyType");
        started.handleProperty(p, ctxt);

        when(p.getCurrentName()).thenReturn("value");
        when(p.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(p.getText()).thenReturn("someValue");
        started.handleProperty(p, ctxt);

        Object bean = new Object();
        Object result = started.complete(p, ctxt, bean);
        assertNotNull("Complete should return bean", result);
        assertSame("Should be same bean", bean, result);
    }

    @Test(expected = IOException.class)
    public void testCompleteMissingTypeId() throws Exception {
        when(extTypedProperty.getTypePropertyName()).thenReturn("typeId");
        when(extTypedProperty.getPropertyName()).thenReturn("value");

        builder.addExternal(extTypedProperty, property);
        ExternalTypeHandler handler = builder.build();
        ExternalTypeHandler started = handler.start();

        // Only handle value, not type
        when(p.getCurrentName()).thenReturn("value");
        when(p.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(p.getText()).thenReturn("someValue");
        started.handleProperty(p, ctxt);

        started.complete(p, ctxt, new Object());
    }

    @Test(expected = IOException.class)
    public void testCompleteMissingValueProperty() throws Exception {
        when(extTypedProperty.getTypePropertyName()).thenReturn("typeId");
        when(extTypedProperty.getPropertyName()).thenReturn("value");

        builder.addExternal(extTypedProperty, property);
        ExternalTypeHandler handler = builder.build();
        ExternalTypeHandler started = handler.start();

        // Only handle type, not value
        when(p.getCurrentName()).thenReturn("typeId");
        when(p.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(p.getText()).thenReturn("MyType");
        started.handleProperty(p, ctxt);

        started.complete(p, ctxt, new Object());
    }

    @Test
    public void testCompleteWithNullBean() throws Exception {
        when(extTypedProperty.getTypePropertyName()).thenReturn("typeId");
        when(extTypedProperty.getPropertyName()).thenReturn("value");

        builder.addExternal(extTypedProperty, property);
        ExternalTypeHandler handler = builder.build();
        ExternalTypeHandler started = handler.start();

        when(p.getCurrentName()).thenReturn("typeId");
        when(p.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(p.getText()).thenReturn("MyType");
        started.handleProperty(p, ctxt);

        when(p.getCurrentName()).thenReturn("value");
        when(p.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(p.getText()).thenReturn("someValue");
        started.handleProperty(p, ctxt);

        Object result = started.complete(p, ctxt, null);
        assertNotNull("Complete with null bean should still return non-null", result);
    }

    // --- Edge cases for ExtTypedProperty ---

    @Test
    public void testExtTypedPropertyCreation() {
        ExtTypedProperty extProp = new ExtTypedProperty(property, "typePropName");
        assertNotNull(extProp);
        assertEquals("typePropName", extProp.getTypePropertyName());
        assertSame(property, extProp.getProperty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExtTypedPropertyNullProperty() {
        new ExtTypedProperty(null, "typePropName");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExtTypedPropertyNullTypeName() {
        new ExtTypedProperty(property, null);
    }

    // --- Additional coverage for internal methods ---

    @Test
    public void testHandlePropertyWithArrayToken() throws IOException {
        when(extTypedProperty.getTypePropertyName()).thenReturn("typeId");
        when(p.getCurrentName()).thenReturn("typeId");
        when(p.getCurrentToken()).thenReturn(JsonToken.START_ARRAY);

        builder.addExternal(extTypedProperty, property);
        ExternalTypeHandler handler = builder.build();
        ExternalTypeHandler started = handler.start();

        boolean handled = started.handleProperty(p, ctxt);
        assertTrue("Array token should be handled", handled);
    }

    @Test
    public void testHandlePropertyWithObjectToken() throws IOException {
        when(extTypedProperty.getTypePropertyName()).thenReturn("typeId");
        when(p.getCurrentName()).thenReturn("typeId");
        when(p.getCurrentToken()).thenReturn(JsonToken.START_OBJECT);

        builder.addExternal(extTypedProperty, property);
        ExternalTypeHandler handler = builder.build();
        ExternalTypeHandler started = handler.start();

        boolean handled = started.handleProperty(p, ctxt);
        assertTrue("Object token should be handled", handled);
    }

    @Test
    public void testCompleteWithMultipleProperties() throws Exception {
        // Add two external properties
        SettableBeanProperty property2 = mock(SettableBeanProperty.class);
        ExtTypedProperty extProp2 = mock(ExtTypedProperty.class);
        when(extProp2.getTypePropertyName()).thenReturn("typeId2");
        when(extProp2.getPropertyName()).thenReturn("value2");
        when(extProp2.getProperty()).thenReturn(property2);

        builder.addExternal(extTypedProperty, property);
        builder.addExternal(extProp2, property2);
        ExternalTypeHandler handler = builder.build();
        ExternalTypeHandler started = handler.start();

        // Handle first property
        when(extTypedProperty.getTypePropertyName()).thenReturn("typeId");
        when(p.getCurrentName()).thenReturn("typeId");
        when(p.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(p.getText()).thenReturn("Type1");
        started.handleProperty(p, ctxt);

        when(p.getCurrentName()).thenReturn("value");
        when(p.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(p.getText()).thenReturn("val1");
        started.handleProperty(p, ctxt);

        // Handle second property
        when(extProp2.getTypePropertyName()).thenReturn("typeId2");
        when(p.getCurrentName()).thenReturn("typeId2");
        when(p.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(p.getText()).thenReturn("Type2");
        started.handleProperty(p, ctxt);

        when(p.getCurrentName()).thenReturn("value2");
        when(p.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(p.getText()).thenReturn("val2");
        started.handleProperty(p, ctxt);

        Object bean = new Object();
        Object result = started.complete(p, ctxt, bean);
        assertNotNull(result);
    }

    @Test(expected = IOException.class)
    public void testCompleteWithUnresolvedTypeId() throws Exception {
        when(extTypedProperty.getTypePropertyName()).thenReturn("typeId");
        when(extTypedProperty.getPropertyName()).thenReturn("value");

        builder.addExternal(extTypedProperty, property);
        ExternalTypeHandler handler = builder.build();
        ExternalTypeHandler started = handler.start();

        // Handle value but not type
        when(p.getCurrentName()).thenReturn("value");
        when(p.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(p.getText()).thenReturn("someValue");
        started.handleProperty(p, ctxt);

        // Try to complete with missing type
        started.complete(p, ctxt, new Object());
    }

    @Test
    public void testHandlePropertyWithNullCurrentName() throws IOException {
        when(p.getCurrentName()).thenReturn(null);
        when(p.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);

        builder.addExternal(extTypedProperty, property);
        ExternalTypeHandler handler = builder.build();
        ExternalTypeHandler started = handler.start();

        boolean handled = started.handleProperty(p, ctxt);
        assertFalse("Null current name should not be handled", handled);
    }

    @Test
    public void testHandlePropertyWithEmptyStringName() throws IOException {
        when(p.getCurrentName()).thenReturn("");
        when(p.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);

        builder.addExternal(extTypedProperty, property);
        ExternalTypeHandler handler = builder.build();
        ExternalTypeHandler started = handler.start();

        boolean handled = started.handleProperty(p, ctxt);
        assertFalse("Empty string name should not be handled", handled);
    }
}