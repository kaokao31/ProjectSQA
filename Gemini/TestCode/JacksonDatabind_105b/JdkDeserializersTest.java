package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.databind.JsonDeserializer;
import org.junit.Test;

import java.net.URI;
import java.net.URL;
import java.util.Currency;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class JdkDeserializersTest {

    @Test
    public void testFindNullType() {
        assertNull(JdkDeserializers.find(null, "some.class.Name"));
    }

    @Test
    public void testFindUnknownType() {
        assertNull(JdkDeserializers.find(Object.class, "java.lang.String"));
        assertNull(JdkDeserializers.find(URI.class, "com.unknown.Class"));
    }

    @Test
    public void testFindKnownTypes() {
        Class<?>[] knownClasses = new Class<?>[] {
            UUID.class,
            URL.class,
            URI.class,
            Locale.class,
            Currency.class,
            AtomicBoolean.class
        };

        for (Class<?> cls : knownClasses) {
            JsonDeserializer<?> deserializer = JdkDeserializers.find(cls, cls.getName());
            assertNotNull("Deserializer should not be null for " + cls.getName(), deserializer);
        }
    }

    @Test
    public void testFindWithClassOnlyOverload() {
        // Since JdkDeserializers might have overloaded find(Class<?>, String) vs find(Class<?>) or similar,
        // let's verify standard known types using their name.
        JsonDeserializer<?> deserUUID = JdkDeserializers.find(UUID.class, UUID.class.getName());
        assertNotNull(deserUUID);

        JsonDeserializer<?> deserURI = JdkDeserializers.find(URI.class, URI.class.getName());
        assertNotNull(deserURI);
    }
}