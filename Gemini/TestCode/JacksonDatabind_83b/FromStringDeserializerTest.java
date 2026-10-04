package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.UUID;

import static org.junit.Assert.*;

public class FromStringDeserializerTest {

    static class DummyFromStringDeserializer extends FromStringDeserializer<Object> {
        private final boolean returnNullOnEmpty;

        public DummyFromStringDeserializer(Class<?> vc) {
            super(vc);
            this.returnNullOnEmpty = false;
        }

        public DummyFromStringDeserializer(Class<?> vc, boolean returnNullOnEmpty) {
            super(vc);
            this.returnNullOnEmpty = returnNullOnEmpty;
        }

        @Override
        protected Object _deserialize(String value, DeserializationContext ctxt) throws IOException {
            if ("invalid".equals(value)) {
                throw new IllegalArgumentException("Invalid value");
            }
            if ("null-val".equals(value)) {
                return null;
            }
            return value;
        }

        @Override
        protected Object _deserializeEmbedded(Object embedded, DeserializationContext ctxt) throws IOException {
            return super._deserializeEmbedded(embedded, ctxt);
        }

        @Override
        protected boolean _shouldUnwrap() {
            return super._shouldUnwrap();
        }
    }

    @Test
    public void testFindDeserializer() {
        Class<?>[] types = new Class<?>[] {
                UUID.class,
                URI.class,
                URL.class,
                java.util.Date.class,
                java.util.Calendar.class,
                java.util.Locale.class,
                java.nio.charset.Charset.class,
                java.util.TimeZone.class,
                java.io.File.class,
                java.lang.Class.class,
                com.fasterxml.jackson.databind.util.NameTransformer.class
        };

        for (Class<?> cls : types) {
            assertNotNull(FromStringDeserializer.findDeserializer(cls));
        }

        // Test unsupported class
        assertNull(FromStringDeserializer.findDeserializer(Object.class));
    }

    @Test
    public void testAllStdTypesExist() {
        Class<?>[] stdTypes = FromStringDeserializer.types();
        assertNotNull(stdTypes);
        assertTrue(stdTypes.length > 0);
        for (Class<?> cls : stdTypes) {
            assertNotNull(FromStringDeserializer.findDeserializer(cls));
        }
    }

    @Test
    public void testConstructorsAndFind() {
        DummyFromStringDeserializer deser = new DummyFromStringDeserializer(String.class);
        assertNotNull(deser);
    }
}