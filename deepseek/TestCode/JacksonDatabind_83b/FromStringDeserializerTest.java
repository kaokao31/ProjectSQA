package com.fasterxml.jackson.databind.deser.std;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.Currency;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.FromStringDeserializer;

@RunWith(MockitoJUnitRunner.class)
public class FromStringDeserializerTest {

    @Mock
    private JsonParser jp;

    @Mock
    private DeserializationContext ctxt;

    private FromStringDeserializer<?> deser;

    @Before
    public void setUp() throws Exception {
        // Default behavior for ctxt: not to fail on null or empty
        when(ctxt.isEnabled(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)).thenReturn(false);
        when(ctxt.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)).thenReturn(false);
        when(ctxt.handleMissingEndElementForSingle(any(), any())).thenThrow(new JsonMappingException(null, "mock"));
    }

    // Helper to create a concrete testable subclass
    private static class TestFromStringDeserializer extends FromStringDeserializer<String> {
        private static final long serialVersionUID = 1L;

        public TestFromStringDeserializer() {
            super(String.class);
        }

        @Override
        protected String _deserialize(String value, DeserializationContext ctxt) throws IOException {
            // Simple pass-through for testing base logic
            return value;
        }
    }

    // ---------- Tests for base class logic (null/empty handling) ----------

    @Test
    public void testDeserializeNullToken() throws Exception {
        deser = new TestFromStringDeserializer();
        when(jp.getCurrentToken()).thenReturn(JsonToken.VALUE_NULL);
        assertNull("Null token should return null", deser.deserialize(jp, ctxt));
    }

    @Test
    public void testDeserializeEmptyString() throws Exception {
        deser = new TestFromStringDeserializer();
        when(jp.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(jp.getText()).thenReturn("");
        // Without ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, empty string should be passed to _deserialize
        assertEquals("Empty string should be deserialized", "", deser.deserialize(jp, ctxt));
    }

    @Test
    public void testDeserializeEmptyStringAsNull() throws Exception {
        deser = new TestFromStringDeserializer();
        when(ctxt.isEnabled(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)).thenReturn(true);
        when(jp.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(jp.getText()).thenReturn("");
        assertNull("Empty string should be treated as null when feature enabled", deser.deserialize(jp, ctxt));
    }

    @Test
    public void testDeserializeEmptyArrayAsNull() throws Exception {
        deser = new TestFromStringDeserializer();
        when(ctxt.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)).thenReturn(true);
        when(jp.getCurrentToken()).thenReturn(JsonToken.START_ARRAY);
        when(jp.nextToken()).thenReturn(JsonToken.END_ARRAY);
        assertNull("Empty array should be treated as null when feature enabled", deser.deserialize(jp, ctxt));
    }

    @Test(expected = JsonMappingException.class)
    public void testDeserializeNonStringToken() throws Exception {
        deser = new TestFromStringDeserializer();
        when(jp.getCurrentToken()).thenReturn(JsonToken.START_OBJECT);
        deser.deserialize(jp, ctxt);
    }

    // ---------- Tests for specific types (UUID, Locale, Pattern, etc.) ----------

    @Test
    public void testUUIDDeserializer() throws Exception {
        deser = FromStringDeserializer.UUIDDeserializer.instance;
        when(jp.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(jp.getText()).thenReturn("550e8400-e29b-41d4-a716-446655440000");
        UUID result = (UUID) deser.deserialize(jp, ctxt);
        assertEquals(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), result);
    }

    @Test(expected = IOException.class)
    public void testUUIDDeserializerInvalid() throws Exception {
        deser = FromStringDeserializer.UUIDDeserializer.instance;
        when(jp.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(jp.getText()).thenReturn("not-a-uuid");
        deser.deserialize(jp, ctxt);
    }

    @Test
    public void testLocaleDeserializer() throws Exception {
        deser = FromStringDeserializer.LocaleDeserializer.instance;
        when(jp.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(jp.getText()).thenReturn("en_US");
        Locale result = (Locale) deser.deserialize(jp, ctxt);
        assertEquals(new Locale("en", "US"), result);
    }

    @Test
    public void testLocaleDeserializerWithHyphen() throws Exception {
        deser = FromStringDeserializer.LocaleDeserializer.instance;
        when(jp.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(jp.getText()).thenReturn("en-US");
        Locale result = (Locale) deser.deserialize(jp, ctxt);
        assertEquals(new Locale("en", "US"), result);
    }

    @Test
    public void testLocaleDeserializerWithVariant() throws Exception {
        deser = FromStringDeserializer.LocaleDeserializer.instance;
        when(jp.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(jp.getText()).thenReturn("en_US_WIN");
        Locale result = (Locale) deser.deserialize(jp, ctxt);
        assertEquals(new Locale("en", "US", "WIN"), result);
    }

    @Test(expected = IOException.class)
    public void testLocaleDeserializerInvalid() throws Exception {
        deser = FromStringDeserializer.LocaleDeserializer.instance;
        when(jp.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(jp.getText()).thenReturn("invalid_locale_");
        deser.deserialize(jp, ctxt);
    }

    @Test
    public void testPatternDeserializer() throws Exception {
        deser = FromStringDeserializer.PatternDeserializer.instance;
        when(jp.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(jp.getText()).thenReturn("\\d+");
        Pattern result = (Pattern) deser.deserialize(jp, ctxt);
        assertEquals(Pattern.compile("\\d+").pattern(), result.pattern());
    }

    @Test
    public void testPatternDeserializerWithFlags() throws Exception {
        deser = FromStringDeserializer.PatternDeserializer.instance;
        when(jp.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(jp.getText()).thenReturn("(?i)abc");
        Pattern result = (Pattern) deser.deserialize(jp, ctxt);
        assertEquals(Pattern.compile("(?i)abc").pattern(), result.pattern());
        assertEquals(Pattern.compile("(?i)abc").flags(), result.flags());
    }

    @Test(expected = IOException.class)
    public void testPatternDeserializerInvalid() throws Exception {
        deser = FromStringDeserializer.PatternDeserializer.instance;
        when(jp.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(jp.getText()).thenReturn("[invalid");
        deser.deserialize(jp, ctxt);
    }

    @Test
    public void testCurrencyDeserializer() throws Exception {
        deser = FromStringDeserializer.CurrencyDeserializer.instance;
        when(jp.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(jp.getText()).thenReturn("USD");
        Currency result = (Currency) deser.deserialize(jp, ctxt);
        assertEquals(Currency.getInstance("USD"), result);
    }

    @Test(expected = IOException.class)
    public void testCurrencyDeserializerInvalid() throws Exception {
        deser = FromStringDeserializer.CurrencyDeserializer.instance;
        when(jp.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(jp.getText()).thenReturn("XYZ");
        deser.deserialize(jp, ctxt);
    }

    @Test
    public void testURIDeserializer() throws Exception {
        deser = FromStringDeserializer.URIDeserializer.instance;
        when(jp.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(jp.getText()).thenReturn("http://example.com/path");
        URI result = (URI) deser.deserialize(jp, ctxt);
        assertEquals(URI.create("http://example.com/path"), result);
    }

    @Test(expected = IOException.class)
    public void testURIDeserializerInvalid() throws Exception {
        deser = FromStringDeserializer.URIDeserializer.instance;
        when(jp.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(jp.getText()).thenReturn("not a uri");
        deser.deserialize(jp, ctxt);
    }

    @Test
    public void testURLDeserializer() throws Exception {
        deser = FromStringDeserializer.URLDeserializer.instance;
        when(jp.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(jp.getText()).thenReturn("http://example.com");
        URL result = (URL) deser.deserialize(jp, ctxt);
        assertEquals(new URL("http://example.com"), result);
    }

    @Test(expected = IOException.class)
    public void testURLDeserializerInvalid() throws Exception {
        deser = FromStringDeserializer.URLDeserializer.instance;
        when(jp.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(jp.getText()).thenReturn("not a url");
        deser.deserialize(jp, ctxt);
    }

    @Test
    public void testInetAddressDeserializer() throws Exception {
        deser = FromStringDeserializer.InetAddressDeserializer.instance;
        when(jp.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(jp.getText()).thenReturn("192.168.1.1");
        InetAddress result = (InetAddress) deser.deserialize(jp, ctxt);
        assertEquals(InetAddress.getByName("192.168.1.1"), result);
    }

    @Test(expected = IOException.class)
    public void testInetAddressDeserializerInvalid() throws Exception {
        deser = FromStringDeserializer.InetAddressDeserializer.instance;
        when(jp.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(jp.getText()).thenReturn("not an address");
        deser.deserialize(jp, ctxt);
    }

    @Test
    public void testCharsetDeserializer() throws Exception {
        deser = FromStringDeserializer.CharsetDeserializer.instance;
        when(jp.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(jp.getText()).thenReturn("UTF-8");
        Object result = deser.deserialize(jp, ctxt);
        assertEquals(java.nio.charset.Charset.forName("UTF-8"), result);
    }

    @Test(expected = IOException.class)
    public void testCharsetDeserializerInvalid() throws Exception {
        deser = FromStringDeserializer.CharsetDeserializer.instance;
        when(jp.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(jp.getText()).thenReturn("invalid-charset");
        deser.deserialize(jp, ctxt);
    }

    @Test
    public void testTimeZoneDeserializer() throws Exception {
        deser = FromStringDeserializer.TimeZoneDeserializer.instance;
        when(jp.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(jp.getText()).thenReturn("America/New_York");
        TimeZone result = (TimeZone) deser.deserialize(jp, ctxt);
        assertEquals(TimeZone.getTimeZone("America/New_York"), result);
    }

    @Test(expected = IOException.class)
    public void testTimeZoneDeserializerInvalid() throws Exception {
        deser = FromStringDeserializer.TimeZoneDeserializer.instance;
        when(jp.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(jp.getText()).thenReturn("NotATimeZone");
        deser.deserialize(jp, ctxt);
    }

    @Test
    public void testClassDeserializer() throws Exception {
        deser = FromStringDeserializer.ClassDeserializer.instance;
        when(jp.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(jp.getText()).thenReturn("java.lang.String");
        Class<?> result = (Class<?>) deser.deserialize(jp, ctxt);
        assertEquals(String.class, result);
    }

    @Test(expected = IOException.class)
    public void testClassDeserializerInvalid() throws Exception {
        deser = FromStringDeserializer.ClassDeserializer.instance;
        when(jp.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(jp.getText()).thenReturn("nonexistent.Class");
        deser.deserialize(jp, ctxt);
    }

    // ---------- Edge cases for bug 83 (Locale underscore/hyphen) ----------

    @Test
    public void testLocaleDeserializerUnderscoreOnly() throws Exception {
        deser = FromStringDeserializer.LocaleDeserializer.instance;
        when(jp.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(jp.getText()).thenReturn("en");
        Locale result = (Locale) deser.deserialize(jp, ctxt);
        assertEquals(new Locale("en"), result);
    }

    @Test
    public void testLocaleDeserializerEmptyVariant() throws Exception {
        deser = FromStringDeserializer.LocaleDeserializer.instance;
        when(jp.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(jp.getText()).thenReturn("en_US_");
        Locale result = (Locale) deser.deserialize(jp, ctxt);
        assertEquals(new Locale("en", "US"), result);
    }

    @Test
    public void testLocaleDeserializerMultipleUnderscores() throws Exception {
        deser = FromStringDeserializer.LocaleDeserializer.instance;
        when(jp.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(jp.getText()).thenReturn("en_US_WIN_EXTRA");
        Locale result = (Locale) deser.deserialize(jp, ctxt);
        assertEquals(new Locale("en", "US", "WIN_EXTRA"), result);
    }

    // ---------- Test _deserialize directly via subclass ----------

    @Test
    public void testDeserializeDirectly() throws Exception {
        TestFromStringDeserializer testDeser = new TestFromStringDeserializer();
        assertEquals("hello", testDeser._deserialize("hello", ctxt));
    }

    @Test(expected = IOException.class)
    public void testDeserializeDirectlyThrows() throws Exception {
        // Subclass that throws on certain input
        FromStringDeserializer<String> throwingDeser = new FromStringDeserializer<String>(String.class) {
            private static final long serialVersionUID = 1L;
            @Override
            protected String _deserialize(String value, DeserializationContext ctxt) throws IOException {
                throw new IOException("forced");
            }
        };
        throwingDeser._deserialize("any", ctxt);
    }
}