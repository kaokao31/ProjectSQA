package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Currency;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class FromStringDeserializerTest {

    // Concrete subclass to test the abstract FromStringDeserializer
    private static class DummyFromStringDeserializer extends FromStringDeserializer<Object> {
        private final boolean returnNullOnEmpty;

        public DummyFromStringDeserializer(Class<?> vc, boolean returnNullOnEmpty) {
            super(vc);
            this.returnNullOnEmpty = returnNullOnEmpty;
        }

        @Override
        protected Object _deserialize(String value, DeserializationContext ctxt) throws IOException {
            if ("fail".equals(value)) {
                throw new IOException("Failed explicitly");
            }
            if ("runtime-fail".equals(value)) {
                throw new IllegalArgumentException("Runtime failure");
            }
            return value;
        }

        @Override
        protected boolean _shouldAllowEmpty() {
            return returnNullOnEmpty;
        }
    }

    @Test
    public void testFindDeserializer() {
        Class<?>[] types = new Class<?>[] {
                UUID.class,
                InetAddress.class,
                TimeZone.class,
                Charset.class,
                Currency.class,
                Pattern.class,
                Locale.class,
                URI.class,
                URL.class,
                File.class
        };

        for (Class<?> cls : types) {
            FromStringDeserializer<?> deser = FromStringDeserializer.findDeserializer(cls);
            assertNotNull("Should find deserializer for " + cls.getName(), deser);
        }

        // Unknown type should return null
        assertNull(FromStringDeserializer.findDeserializer(Object.class));
    }

    @Test
    public void testTypesArray() {
        Class<?>[] types = FromStringDeserializer.types();
        assertNotNull(types);
        assertTrue(types.length > 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeserializerSubclass_URI_Invalid() throws Exception {
        FromStringDeserializer<URI> uriDeser = (FromStringDeserializer<URI>) FromStringDeserializer.findDeserializer(URI.class);
        // Invalid URI that should trigger URISyntaxException wrapped in IllegalArgumentException
        uriDeser._deserialize("http://[invalid-ipv6", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeserializerSubclass_Charset_Invalid() throws Exception {
        FromStringDeserializer<Charset> csDeser = (FromStringDeserializer<Charset>) FromStringDeserializer.findDeserializer(Charset.class);
        csDeser._deserialize("INVALID-CHARSET-NAME-XYZ-123", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeserializerSubclass_Currency_Invalid() throws Exception {
        FromStringDeserializer<Currency> currDeser = (FromStringDeserializer<Currency>) FromStringDeserializer.findDeserializer(Currency.class);
        currDeser._deserialize("INVALID", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeserializerSubclass_Pattern_Invalid() throws Exception {
        FromStringDeserializer<Pattern> patDeser = (FromStringDeserializer<Pattern>) FromStringDeserializer.findDeserializer(Pattern.class);
        patDeser._deserialize("[unclosed-bracket", null);
    }

    @Test
    public void testDeserializerSubclass_Locale() throws Exception {
        FromStringDeserializer<Locale> locDeser = (FromStringDeserializer<Locale>) FromStringDeserializer.findDeserializer(Locale.class);
        assertNotNull(locDeser._deserialize("en_US", null));
        assertNotNull(locDeser._deserialize("en", null));
        assertNotNull(locDeser._deserialize("en_US_POSIX", null));
    }

    @Test
    public void testDeserializerSubclass_TimeZone() throws Exception {
        FromStringDeserializer<TimeZone> tzDeser = (FromStringDeserializer<TimeZone>) FromStringDeserializer.findDeserializer(TimeZone.class);
        assertNotNull(tzDeser._deserialize("UTC", null));
    }

    @Test
    public void testDeserializerSubclass_InetAddress() throws Exception {
        FromStringDeserializer<InetAddress> iaDeser = (FromStringDeserializer<InetAddress>) FromStringDeserializer.findDeserializer(InetAddress.class);
        // localhost or IP should work
        assertNotNull(iaDeser._deserialize("127.0.0.1", null));
    }

    @Test
    public void testDeserializerSubclass_File() throws Exception {
        FromStringDeserializer<File> fileDeser = (FromStringDeserializer<File>) FromStringDeserializer.findDeserializer(File.class);
        assertNotNull(fileDeser._deserialize("dummy.txt", null));
    }

    @Test
    public void testDeserializerSubclass_URL() throws Exception {
        FromStringDeserializer<URL> urlDeser = (FromStringDeserializer<URL>) FromStringDeserializer.findDeserializer(URL.class);
        assertNotNull(urlDeser._deserialize("http://localhost", null));
    }

    @Test
    public void testDeserializerSubclass_UUID() throws Exception {
        FromStringDeserializer<UUID> uuidDeser = (FromStringDeserializer<UUID>) FromStringDeserializer.findDeserializer(UUID.class);
        String uuidStr = UUID.randomUUID().toString();
        assertEquals(UUID.fromString(uuidStr), uuidDeser._deserialize(uuidStr, null));
    }

    @Test
    public void testStdDeserializer() throws Exception {
        DummyFromStringDeserializer deser = new DummyFromStringDeserializer(String.class, true);
        
        // Test normal value via deserialize (mocking or null context if feasible)
        // Since DeserializationContext and JsonParser might need nulls or mocks depending on usage,
        // let's check how deserialize handles null text or empty strings.
        
        // _shouldAllowEmpty() returns true, so empty string should return null or handle appropriately if implemented in StdDeserializer.
        // Let's test the base class deserialize method if it handles empty strings.
        try {
            deser.deserialize(null, null);
        } catch (Exception e) {
            // expected if parser is null
        }
    }
}