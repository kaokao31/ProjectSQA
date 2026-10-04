package com.google.gson.internal.bind;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class TypeAdaptersTest {

    private final Gson gson = new Gson();

    @Test
    public void testClassTypeAdapter() {
        TypeAdapter<Class> adapter = TypeAdapters.CLASS;
        String json = gson.toJson(String.class, Class.class);
        Assert.assertEquals("\"java.lang.String\"", json);

        Class deserialized = gson.fromJson("\"java.lang.String\"", Class.class);
        Assert.assertEquals(String.class, deserialized);

        // Test null
        Assert.assertNull(gson.fromJson("null", Class.class));
    }

    @Test
    public void testBitSetTypeAdapter() {
        BitSet bitSet = new BitSet();
        bitSet.set(0);
        bitSet.set(2);

        String json = gson.toJson(bitSet);
        BitSet deserialized = gson.fromJson(json, BitSet.class);

        Assert.assertTrue(deserialized.get(0));
        Assert.assertFalse(deserialized.get(1));
        Assert.assertTrue(deserialized.get(2));

        // Test null
        Assert.assertNull(gson.fromJson("null", BitSet.class));
        
        // Test array of integers parsing for BitSet
        BitSet fromArray = gson.fromJson("[0, 2]", BitSet.class);
        Assert.assertTrue(fromArray.get(0));
        Assert.assertFalse(fromArray.get(1));
        Assert.assertTrue(fromArray.get(2));
    }

    @Test
    public void testBooleanTypeAdapter() {
        Assert.assertEquals(Boolean.TRUE, gson.fromJson("true", Boolean.class));
        Assert.assertEquals(Boolean.FALSE, gson.fromJson("false", Boolean.class));
        Assert.assertEquals(Boolean.TRUE, gson.fromJson("\"true\"", Boolean.class));
        Assert.assertNull(gson.fromJson("null", Boolean.class));

        Boolean primitiveBool = gson.fromJson("true", boolean.class);
        Assert.assertTrue(primitiveBool);
    }

    @Test
    public void testByteTypeAdapter() {
        Assert.assertEquals(Byte.valueOf((byte) 120), gson.fromJson("120", Byte.class));
        Assert.assertNull(gson.fromJson("null", Byte.class));

        // Edge case: overflow or invalid number
        try {
            gson.fromJson("1000", byte.class);
            Assert.fail();
        } catch (Exception expected) {
        }
    }

    @Test
    public void testShortTypeAdapter() {
        Assert.assertEquals(Short.valueOf((short) 1234), gson.fromJson("1234", Short.class));
        Assert.assertNull(gson.fromJson("null", Short.class));
    }

    @Test
    public void testIntegerTypeAdapter() {
        Assert.assertEquals(Integer.valueOf(123456), gson.fromJson("123456", Integer.class));
        Assert.assertNull(gson.fromJson("null", Integer.class));
    }

    @Test
    public void testAtomicIntegerTypeAdapter() {
        AtomicInteger ai = gson.fromJson("123", AtomicInteger.class);
        Assert.assertEquals(123, ai.get());
        Assert.assertNull(gson.fromJson("null", AtomicInteger.class));
    }

    @Test
    public void testAtomicBooleanTypeAdapter() {
        AtomicBoolean ab = gson.fromJson("true", AtomicBoolean.class);
        Assert.assertTrue(ab.get());
        Assert.assertNull(gson.fromJson("null", AtomicBoolean.class));
    }

    @Test
    public void testAtomicIntegerArrayTypeAdapter() {
        AtomicIntegerArray aia = gson.fromJson("[1, 2, 3]", AtomicIntegerArray.class);
        Assert.assertEquals(3, aia.length());
        Assert.assertEquals(1, aia.get(0));
        Assert.assertEquals(2, aia.get(1));
        Assert.assertEquals(3, aia.get(2));
        Assert.assertNull(gson.fromJson("null", AtomicIntegerArray.class));
    }

    @Test
    public void testLongTypeAdapter() {
        Assert.assertEquals(Long.valueOf(123456789L), gson.fromJson("123456789", Long.class));
        Assert.assertNull(gson.fromJson("null", Long.class));
    }

    @Test
    public void testFloatTypeAdapter() {
        Float f = gson.fromJson("12.34", Float.class);
        Assert.assertEquals(12.34f, f, 0.001f);
        Assert.assertNull(gson.fromJson("null", Float.class));
    }

    @Test
    public void testDoubleTypeAdapter() {
        Double d = gson.fromJson("12.3456", Double.class);
        Assert.assertEquals(12.3456, d, 0.0001);
        Assert.assertNull(gson.fromJson("null", Double.class));
    }

    @Test
    public void testNumberTypeAdapter() {
        Number n = gson.fromJson("123.45", Number.class);
        Assert.assertTrue(n instanceof Double || n instanceof BigDecimal || n instanceof Long || n instanceof Integer);
        Assert.assertNull(gson.fromJson("null", Number.class));
    }

    @Test
    public void testCharacterTypeAdapter() {
        Character c = gson.fromJson("\"a\"", Character.class);
        Assert.assertEquals(Character.valueOf('a'), c);
        Assert.assertNull(gson.fromJson("null", Character.class));
    }

    @Test
    public void testStringTypeAdapter() {
        Assert.assertEquals("hello", gson.fromJson("\"hello\"", String.class));
        Assert.assertNull(gson.fromJson("null", String.class));
    }

    @Test
    public void testBigDecimalTypeAdapter() {
        BigDecimal bd = gson.fromJson("12345678901234567890.123456789", BigDecimal.class);
        Assert.assertEquals(new BigDecimal("12345678901234567890.123456789"), bd);
        Assert.assertNull(gson.fromJson("null", BigDecimal.class));
    }

    @Test
    public void testBigIntegerTypeAdapter() {
        BigInteger bi = gson.fromJson("12345678901234567890123456789", BigInteger.class);
        Assert.assertEquals(new BigInteger("12345678901234567890123456789"), bi);
        Assert.assertNull(gson.fromJson("null", BigInteger.class));
    }

    @Test
    public void testStringBuilderTypeAdapter() {
        StringBuilder sb = gson.fromJson("\"test\"", StringBuilder.class);
        Assert.assertEquals("test", sb.toString());
        Assert.assertNull(gson.fromJson("null", StringBuilder.class));
    }

    @Test
    public void testStringBufferTypeAdapter() {
        StringBuffer sb = gson.fromJson("\"test\"", StringBuffer.class);
        Assert.assertEquals("test", sb.toString());
        Assert.assertNull(gson.fromJson("null", StringBuffer.class));
    }

    @Test
    public void testURLTypeAdapter() {
        URL url = gson.fromJson("\"http://google.com\"", URL.class);
        Assert.assertEquals("http://google.com", url.toString());
        Assert.assertNull(gson.fromJson("null", URL.class));
    }

    @Test
    public void testURITypeAdapter() {
        URI uri = gson.fromJson("\"http://google.com\"", URI.class);
        Assert.assertEquals("http://google.com", uri.toString());
        Assert.assertNull(gson.fromJson("null", URI.class));
    }

    @Test
    public void testInetAddressTypeAdapter() {
        InetAddress address = gson.fromJson("\"127.0.0.1\"", InetAddress.class);
        Assert.assertNotNull(address);
        Assert.assertNull(gson.fromJson("null", InetAddress.class));
    }

    @Test
    public void testUUIDTypeAdapter() {
        UUID uuid = UUID.randomUUID();
        String json = gson.toJson(uuid);
        UUID deserialized = gson.fromJson(json, UUID.class);
        Assert.assertEquals(uuid, deserialized);
        Assert.assertNull(gson.fromJson("null", UUID.class));
    }

    @Test
    public void testCurrencyTypeAdapter() {
        Currency currency = Currency.getInstance("USD");
        String json = gson.toJson(currency);
        Currency deserialized = gson.fromJson(json, Currency.class);
        Assert.assertEquals(currency, deserialized);
        Assert.assertNull(gson.fromJson("null", Currency.class));
    }

    @Test
    public void testCalendarTypeAdapter() {
        Calendar calendar = Calendar.getInstance();
        String json = gson.toJson(calendar);
        Calendar deserialized = gson.fromJson(json, Calendar.class);
        Assert.assertNotNull(deserialized);
        Assert.assertNull(gson.fromJson("null", Calendar.class));
        
        // Also test GregorianCalendar specifically
        GregorianCalendar gc = gson.fromJson(json, GregorianCalendar.class);
        Assert.assertNotNull(gc);
    }

    @Test
    public void testLocaleTypeAdapter() {
        Locale locale = Locale.US;
        String json = gson.toJson(locale);
        Locale deserialized = gson.fromJson(json, Locale.class);
        Assert.assertEquals(locale, deserialized);
        Assert.assertNull(gson.fromJson("null", Locale.class));
    }

    @Test
    public void testJsonElementTypeAdapter() {
        JsonObject obj = new JsonObject();
        obj.addProperty("key", "value");
        String json = gson.toJson(obj);
        JsonElement element = gson.fromJson(json, JsonElement.class);
        Assert.assertTrue(element.isJsonObject());
        Assert.assertEquals("value", element.getAsJsonObject().get("key").getAsString());

        // Test JsonNull
        JsonElement nullElement = gson.fromJson("null", JsonElement.class);
        Assert.assertTrue(nullElement.isJsonNull());
        
        // Test JsonPrimitive, JsonArray
        JsonElement prim = gson.fromJson("123", JsonElement.class);
        Assert.assertTrue(prim.isJsonPrimitive());
        
        JsonElement arr = gson.fromJson("[1,2]", JsonElement.class);
        Assert.assertTrue(arr.isJsonArray());
    }

    enum TestEnum {
        A, B, C
    }

    @Test
    public void testEnumTypeAdapter() {
        String json = gson.toJson(TestEnum.B);
        Assert.assertEquals("\"B\"", json);
        TestEnum deserialized = gson.fromJson("\"B\"", TestEnum.class);
        Assert.assertEquals(TestEnum.B, deserialized);

        // Test SerializedName annotation scenario if any
        Assert.assertNull(gson.fromJson("null", TestEnum.class));
    }

    @Test
    public void testEnumTypeAdapterWithSerializedName() {
        String json = gson.toJson(EnumWithSerializedName.ANOTHER_NAME);
        Assert.assertEquals("\"custom\"", json);
        EnumWithSerializedName deserialized = gson.fromJson("\"custom\"", EnumWithSerializedName.class);
        Assert.assertEquals(EnumWithSerializedName.ANOTHER_NAME, deserialized);
    }

    enum EnumWithSerializedName {
        @SerializedName("custom")
        ANOTHER_NAME
    }

    @Test
    public void testFactoryAndTypeAdaptersAccessibility() {
        Assert.assertNotNull(TypeAdapters.newFactory(int.class, Integer.class, TypeAdapters.INTEGER));
        Assert.assertNotNull(TypeAdapters.newFactory(int.class, TypeAdapters.INTEGER));
        Assert.assertNotNull(TypeAdapters.newFactory(TestEnum.class, TypeAdapters.newEnumTypeAdapter(TestEnum.class)));
        Assert.assertNotNull(TypeAdapters.newFactoryForMultipleTypes(Object.class, Object.class, TypeAdapters.STRING));
    }
}