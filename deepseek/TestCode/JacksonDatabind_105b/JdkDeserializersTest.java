package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.Currency;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for JdkDeserializers.
 * Designed to achieve high coverage and trigger potential faults (e.g., bug #105).
 */
public class JdkDeserializersTest {

    private DeserializationConfig config;
    private TypeFactory typeFactory;
    private JdkDeserializers deserializers;

    @Before
    public void setUp() {
        // Use default ObjectMapper configuration for testing
        ObjectMapper mapper = new ObjectMapper();
        config = mapper.getDeserializationConfig();
        typeFactory = TypeFactory.defaultInstance();
        deserializers = JdkDeserializers.instance();
    }

    // --- hasDeserializerFor tests ---

    @Test
    public void testHasDeserializerForString() {
        assertTrue("String should have a deserializer",
                JdkDeserializers.hasDeserializerFor(String.class));
    }

    @Test
    public void testHasDeserializerForInteger() {
        assertTrue("Integer should have a deserializer",
                JdkDeserializers.hasDeserializerFor(Integer.class));
    }

    @Test
    public void testHasDeserializerForBoolean() {
        assertTrue("Boolean should have a deserializer",
                JdkDeserializers.hasDeserializerFor(Boolean.class));
    }

    @Test
    public void testHasDeserializerForUUID() {
        assertTrue("UUID should have a deserializer",
                JdkDeserializers.hasDeserializerFor(UUID.class));
    }

    @Test
    public void testHasDeserializerForCurrency() {
        // Bug #105 may involve Currency deserialization
        assertTrue("Currency should have a deserializer",
                JdkDeserializers.hasDeserializerFor(Currency.class));
    }

    @Test
    public void testHasDeserializerForLocale() {
        assertTrue("Locale should have a deserializer",
                JdkDeserializers.hasDeserializerFor(Locale.class));
    }

    @Test
    public void testHasDeserializerForPattern() {
        assertTrue("Pattern should have a deserializer",
                JdkDeserializers.hasDeserializerFor(Pattern.class));
    }

    @Test
    public void testHasDeserializerForCustomClass() {
        assertFalse("Custom class should not have a deserializer",
                JdkDeserializers.hasDeserializerFor(getClass()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHasDeserializerForNull() {
        // Should throw IllegalArgumentException for null input
        JdkDeserializers.hasDeserializerFor(null);
    }

    // --- findDeserializer tests ---

    @Test
    public void testFindDeserializerForString() throws JsonMappingException {
        JavaType type = typeFactory.constructType(String.class);
        JsonDeserializer<?> deser = deserializers.findDeserializer(config, type);
        assertNotNull("Deserializer for String should not be null", deser);
        assertTrue("Deserializer should be instance of StringDeserializer",
                deser instanceof StringDeserializer);
    }

    @Test
    public void testFindDeserializerForInteger() throws JsonMappingException {
        JavaType type = typeFactory.constructType(Integer.class);
        JsonDeserializer<?> deser = deserializers.findDeserializer(config, type);
        assertNotNull("Deserializer for Integer should not be null", deser);
        assertTrue("Deserializer should be instance of NumberDeserializer",
                deser instanceof NumberDeserializer);
    }

    @Test
    public void testFindDeserializerForBoolean() throws JsonMappingException {
        JavaType type = typeFactory.constructType(Boolean.class);
        JsonDeserializer<?> deser = deserializers.findDeserializer(config, type);
        assertNotNull("Deserializer for Boolean should not be null", deser);
        assertTrue("Deserializer should be instance of BooleanDeserializer",
                deser instanceof BooleanDeserializer);
    }

    @Test
    public void testFindDeserializerForUUID() throws JsonMappingException {
        JavaType type = typeFactory.constructType(UUID.class);
        JsonDeserializer<?> deser = deserializers.findDeserializer(config, type);
        assertNotNull("Deserializer for UUID should not be null", deser);
        assertTrue("Deserializer should be instance of UUIDDeserializer",
                deser instanceof UUIDDeserializer);
    }

    @Test
    public void testFindDeserializerForCurrency() throws JsonMappingException {
        // Bug #105: Currency deserialization may fail
        JavaType type = typeFactory.constructType(Currency.class);
        JsonDeserializer<?> deser = deserializers.findDeserializer(config, type);
        assertNotNull("Deserializer for Currency should not be null", deser);
        assertTrue("Deserializer should be instance of CurrencyDeserializer",
                deser instanceof CurrencyDeserializer);
    }

    @Test
    public void testFindDeserializerForLocale() throws JsonMappingException {
        JavaType type = typeFactory.constructType(Locale.class);
        JsonDeserializer<?> deser = deserializers.findDeserializer(config, type);
        assertNotNull("Deserializer for Locale should not be null", deser);
        assertTrue("Deserializer should be instance of LocaleDeserializer",
                deser instanceof LocaleDeserializer);
    }

    @Test
    public void testFindDeserializerForPattern() throws JsonMappingException {
        JavaType type = typeFactory.constructType(Pattern.class);
        JsonDeserializer<?> deser = deserializers.findDeserializer(config, type);
        assertNotNull("Deserializer for Pattern should not be null", deser);
        assertTrue("Deserializer should be instance of PatternDeserializer",
                deser instanceof PatternDeserializer);
    }

    @Test(expected = JsonMappingException.class)
    public void testFindDeserializerForUnsupportedType() throws JsonMappingException {
        // Custom class not supported by JdkDeserializers
        JavaType type = typeFactory.constructType(getClass());
        deserializers.findDeserializer(config, type);
    }

    // --- Singleton instance test ---

    @Test
    public void testSingletonInstance() {
        JdkDeserializers instance1 = JdkDeserializers.instance();
        JdkDeserializers instance2 = JdkDeserializers.instance();
        assertSame("instance() should return the same singleton", instance1, instance2);
    }

    // --- Edge case: primitive types ---

    @Test
    public void testHasDeserializerForPrimitiveInt() {
        assertTrue("int should have a deserializer",
                JdkDeserializers.hasDeserializerFor(int.class));
    }

    @Test
    public void testHasDeserializerForPrimitiveBoolean() {
        assertTrue("boolean should have a deserializer",
                JdkDeserializers.hasDeserializerFor(boolean.class));
    }

    @Test
    public void testFindDeserializerForPrimitiveInt() throws JsonMappingException {
        JavaType type = typeFactory.constructType(int.class);
        JsonDeserializer<?> deser = deserializers.findDeserializer(config, type);
        assertNotNull("Deserializer for int should not be null", deser);
    }

    // --- Additional coverage: null type in findDeserializer ---

    @Test(expected = IllegalArgumentException.class)
    public void testFindDeserializerWithNullType() throws JsonMappingException {
        deserializers.findDeserializer(config, null);
    }

    // --- Test that findDeserializer returns the same deserializer for same type ---

    @Test
    public void testFindDeserializerCaching() throws JsonMappingException {
        JavaType type = typeFactory.constructType(String.class);
        JsonDeserializer<?> deser1 = deserializers.findDeserializer(config, type);
        JsonDeserializer<?> deser2 = deserializers.findDeserializer(config, type);
        assertSame("Deserializer for String should be cached", deser1, deser2);
    }
}