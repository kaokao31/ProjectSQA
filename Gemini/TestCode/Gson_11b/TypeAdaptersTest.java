package com.google.gson.internal.bind;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

import static org.junit.Assert.*;

public class TypeAdaptersTest {

    @Test
    public void testAtomicBooleanAdapter() throws IOException {
        Gson gson = new Gson();
        TypeAdapter<AtomicBoolean> adapter = TypeAdapters.ATOMIC_BOOLEAN;

        AtomicBoolean valTrue = new AtomicBoolean(true);
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        adapter.write(jw, valTrue);
        assertTrue(sw.toString().contains("true"));

        JsonReader jr = new JsonReader(new StringReader("true"));
        AtomicBoolean readVal = adapter.read(jr);
        assertTrue(readVal.get());

        // Test factory
        TypeAdapterFactory factory = TypeAdapters.ATOMIC_BOOLEAN_FACTORY;
        assertNotNull(factory.create(gson, TypeToken.get(AtomicBoolean.class)));
        assertNull(factory.create(gson, TypeToken.get(Boolean.class)));
    }

    @Test
    public void testAtomicIntegerAdapter() throws IOException {
        Gson gson = new Gson();
        TypeAdapter<AtomicInteger> adapter = TypeAdapters.ATOMIC_INTEGER;

        AtomicInteger val = new AtomicInteger(42);
        StringWriter sw = new StringWriter();
        adapter.write(new JsonWriter(sw), val);
        assertEquals("42", sw.toString());

        JsonReader jr = new JsonReader(new StringReader("100"));
        assertEquals(100, adapter.read(jr).get());

        TypeAdapterFactory factory = TypeAdapters.ATOMIC_INTEGER_FACTORY;
        assertNotNull(factory.create(gson, TypeToken.get(AtomicInteger.class)));
        assertNull(factory.create(gson, TypeToken.get(Integer.class)));
    }

    @Test
    public void testAtomicIntegerArrayAdapter() throws IOException {
        Gson gson = new Gson();
        TypeAdapter<AtomicIntegerArray> adapter = TypeAdapters.ATOMIC_INTEGER_ARRAY;

        AtomicIntegerArray arr = new AtomicIntegerArray(new int[]{1, 2, 3});
        StringWriter sw = new StringWriter();
        adapter.write(new JsonWriter(sw), arr);
        assertTrue(sw.toString().contains("1"));

        JsonReader jr = new JsonReader(new StringReader("[10, 20]"));
        AtomicIntegerArray readArr = adapter.read(jr);
        assertEquals(10, readArr.get(0));
        assertEquals(20, readArr.get(1));

        TypeAdapterFactory factory = TypeAdapters.ATOMIC_INTEGER_ARRAY_FACTORY;
        assertNotNull(factory.create(gson, TypeToken.get(AtomicIntegerArray.class)));
        assertNull(factory.create(gson, TypeToken.get(int[].class)));
    }

    @Test
    public void testBigDecimalAdapter() throws IOException {
        TypeAdapter<BigDecimal> adapter = TypeAdapters.BIG_DECIMAL;
        BigDecimal bd = new BigDecimal("12345.6789");
        StringWriter sw = new StringWriter();
        adapter.write(new JsonWriter(sw), bd);
        assertEquals("12345.6789", sw.toString());

        JsonReader jr = new JsonReader(new StringReader("98765.4321"));
        assertEquals(0, bd.compareTo(new BigDecimal("12345.6789")));
        assertEquals(0, adapter.read(jr).compareTo(new BigDecimal("98765.4321")));
        
        assertNull(adapter.read(new JsonReader(new StringReader("null"))));
    }

    @Test
    public void testBigIntegerAdapter() throws IOException {
        TypeAdapter<BigInteger> adapter = TypeAdapters.BIG_INTEGER;
        BigInteger bi = new BigInteger("12345678901234567890");
        StringWriter sw = new StringWriter();
        adapter.write(new JsonWriter(sw), bi);
        assertEquals("12345678901234567890", sw.toString());

        JsonReader jr = new JsonReader(new StringReader("98765432109876543210"));
        assertEquals(bi.toString(), new BigInteger("12345678901234567890").toString());
        assertEquals(new BigInteger("98765432109876543210"), adapter.read(jr));

        assertNull(adapter.read(new JsonReader(new StringReader("null"))));
    }

    @Test
    public void testBitSetAdapter() throws IOException {
        TypeAdapter<BitSet> adapter = TypeAdapters.BIT_SET;
        BitSet bs = new BitSet();
        bs.set(0);
        bs.set(2);

        StringWriter sw = new StringWriter();
        adapter.write(new JsonWriter(sw), bs);
        assertTrue(sw.toString().contains("1") || sw.toString().contains("5") || sw.toString().contains("["));

        JsonReader jr = new JsonReader(new StringReader("[0, 2]"));
        BitSet readBs = adapter.read(jr);
        assertTrue(readBs.get(0));
        assertFalse(readBs.get(1));
        assertTrue(readBs.get(2));

        assertNull(adapter.read(new JsonReader(new StringReader("null"))));
    }

    @Test
    public void testBooleanAdapters() throws IOException {
        TypeAdapter<Boolean> adapter = TypeAdapters.BOOLEAN;
        StringWriter sw = new StringWriter();
        adapter.write(new JsonWriter(sw), Boolean.TRUE);
        assertEquals("true", sw.toString());

        JsonReader jr = new JsonReader(new StringReader("false"));
        assertFalse(adapter.read(jr));

        JsonReader jrStr = new JsonReader(new StringReader("\"true\""));
        assertTrue(adapter.read(jrStr));

        TypeAdapter<Boolean> adapterBoxed = TypeAdapters.BOOLEAN_AS_STRING;
        StringWriter sw2 = new StringWriter();
        adapterBoxed.write(new JsonWriter(sw2), Boolean.FALSE);
        assertEquals("\"false\"", sw2.toString());
        
        assertNull(adapter.read(new JsonReader(new StringReader("null"))));
    }

    @Test
    public void testByteAdapter() throws IOException {
        TypeAdapter<Number> adapter = TypeAdapters.BYTE;
        StringWriter sw = new StringWriter();
        adapter.write(new JsonWriter(sw), (byte) 123);
        assertEquals("123", sw.toString());

        JsonReader jr = new JsonReader(new StringReader("55"));
        assertEquals((byte) 55, adapter.read(jr).byteValue());
        
        assertNull(adapter.read(new JsonReader(new StringReader("null"))));
    }

    @Test
    public void testCalendarAdapter() throws IOException {
        Gson gson = new Gson();
        TypeAdapterFactory factory = TypeAdapters.CALENDAR_FACTORY;
        TypeAdapter<Calendar> adapter = TypeAdapters.CALENDAR;

        assertNotNull(factory.create(gson, TypeToken.get(Calendar.class)));
        assertNotNull(factory.create(gson, TypeToken.get(GregorianCalendar.class)));
        assertNull(factory.create(gson, TypeToken.get(Integer.class)));

        Calendar cal = Calendar.getInstance();
        StringWriter sw = new StringWriter();
        adapter.write(new JsonWriter(sw), cal);
        assertTrue(sw.toString().contains("year"));

        JsonReader jr = new JsonReader(new StringReader("{\"year\":2020,\"month\":0,\"dayOfMonth\":1,\"hourOfDay\":0,\"minute\":0,\"second\":0}"));
        Calendar readCal = adapter.read(jr);
        assertNotNull(readCal);

        assertNull(adapter.read(new JsonReader(new StringReader("null"))));
    }

    @Test
    public void testCharacterAdapter() throws IOException {
        TypeAdapter<Character> adapter = TypeAdapters.CHARACTER;
        StringWriter sw = new StringWriter();
        adapter.write(new JsonWriter(sw), 'A');
        assertEquals("\"A\"", sw.toString());

        JsonReader jr = new JsonReader(new StringReader("\"B\""));
        assertEquals(Character.valueOf('B'), adapter.read(jr));

        assertNull(adapter.read(new JsonReader(new StringReader("null"))));
    }

    @Test
    public void testCurrencyAdapter() throws IOException {
        Gson gson = new Gson();
        TypeAdapterFactory factory = TypeAdapters.CURRENCY_FACTORY;
        TypeAdapter<Currency> adapter = TypeAdapters.CURRENCY;

        assertNotNull(factory.create(gson, TypeToken.get(Currency.class)));

        Currency currency = Currency.getInstance("USD");
        StringWriter sw = new StringWriter();
        adapter.write(new JsonWriter(sw), currency);
        assertEquals("\"USD\"", sw.toString());

        JsonReader jr = new JsonReader(new StringReader("\"EUR\""));
        assertEquals(Currency.getInstance("EUR"), adapter.read(jr));

        assertNull(adapter.read(new JsonReader(new StringReader("null"))));
    }

    @Test
    public void testInetAddressAdapter() throws IOException {
        Gson gson = new Gson();
        TypeAdapterFactory factory = TypeAdapters.INET_ADDRESS_FACTORY;
        TypeAdapter<InetAddress> adapter = TypeAdapters.INET_ADDRESS;

        assertNotNull(factory.create(gson, TypeToken.get(InetAddress.class)));

        InetAddress addr = InetAddress.getByName("127.0.0.1");
        StringWriter sw = new StringWriter();
        adapter.write(new JsonWriter(sw), addr);
        assertTrue(sw.toString().contains("127.0.0.1"));

        JsonReader jr = new JsonReader(new StringReader("\"127.0.0.1\""));
        assertEquals(addr, adapter.read(jr));

        assertNull(adapter.read(new JsonReader(new StringReader("null"))));
    }

    @Test
    public void testIntegerAdapter() throws IOException {
        TypeAdapter<Number> adapter = TypeAdapters.INTEGER;
        StringWriter sw = new StringWriter();
        adapter.write(new JsonWriter(sw), 12345);
        assertEquals("12345", sw.toString());

        JsonReader jr = new JsonReader(new StringReader("54321"));
        assertEquals(54321, adapter.read(jr).intValue());

        assertNull(adapter.read(new JsonReader(new StringReader("null"))));
    }

    @Test
    public void testLocaleAdapter() throws IOException {
        TypeAdapter<Locale> adapter = TypeAdapters.LOCALE;
        StringWriter sw = new StringWriter();
        adapter.write(new JsonWriter(sw), Locale.US);
        assertEquals("\"en_US\"", sw.toString());

        JsonReader jr = new JsonReader(new StringReader("\"en_US\""));
        assertEquals(Locale.US, adapter.read(jr));

        assertNull(adapter.read(new JsonReader(new StringReader("null"))));
    }

    @Test
    public void testStringBufferBuilderAdapters() throws IOException {
        TypeAdapter<StringBuffer> sbAdapter = TypeAdapters.STRING_BUFFER;
        StringWriter sw = new StringWriter();
        sbAdapter.write(new JsonWriter(sw), new StringBuffer("Hello"));
        assertEquals("\"Hello\"", sw.toString());
        assertEquals("Hello", sbAdapter.read(new JsonReader(new StringReader("\"Hello\""))).toString());
        assertNull(sbAdapter.read(new JsonReader(new StringReader("null"))));

        TypeAdapter<StringBuilder> sbdAdapter = TypeAdapters.STRING_BUILDER;
        StringWriter sw2 = new StringWriter();
        sbdAdapter.write(new JsonWriter(sw2), new StringBuilder("World"));
        assertEquals("\"World\"", sw2.toString());
        assertEquals("World", sbdAdapter.read(new JsonReader(new StringReader("\"World\""))).toString());
        assertNull(sbdAdapter.read(new JsonReader(new StringReader("null"))));
    }

    @Test
    public void testURIAdapter() throws IOException {
        TypeAdapter<URI> adapter = TypeAdapters.URI;
        URI uri = URI.create("http://google.com");
        StringWriter sw = new StringWriter();
        adapter.write(new JsonWriter(sw), uri);
        assertEquals("\"http://google.com\"", sw.toString());

        JsonReader jr = new JsonReader(new StringReader("\"http://google.com\""));
        assertEquals(uri, adapter.read(jr));

        assertNull(adapter.read(new JsonReader(new StringReader("null"))));
    }

    @Test
    public void testURLAdapter() throws IOException {
        TypeAdapter<URL> adapter = TypeAdapters.URL;
        URL url = new URL("http://google.com");
        StringWriter sw = new StringWriter();
        adapter.write(new JsonWriter(sw), url);
        assertEquals("\"http://google.com\"", sw.toString());

        JsonReader jr = new JsonReader(new StringReader("\"http://google.com\""));
        assertEquals(url, adapter.read(jr));

        assertNull(adapter.read(new JsonReader(new StringReader("null"))));
    }

    @Test
    public void testUUIDAdapter() throws IOException {
        TypeAdapter<UUID> adapter = TypeAdapters.UUID;
        UUID uuid = UUID.randomUUID();
        StringWriter sw = new StringWriter();
        adapter.write(new JsonWriter(sw), uuid);
        assertEquals("\"" + uuid.toString() + "\"", sw.toString());

        JsonReader jr = new JsonReader(new StringReader("\"" + uuid.toString() + "\""));
        assertEquals(uuid, adapter.read(jr));

        assertNull(adapter.read(new JsonReader(new StringReader("null"))));
    }

    @Test
    public void testEnumTypeAdapter() throws IOException {
        TypeAdapterFactory factory = TypeAdapters.ENUM_FACTORY;
        Gson gson = new Gson();
        TypeAdapter<DummyEnum> adapter = (TypeAdapter<DummyEnum>) factory.create(gson, TypeToken.get(DummyEnum.class));
        assertNotNull(adapter);

        StringWriter sw = new StringWriter();
        adapter.write(new JsonWriter(sw), DummyEnum.VALUE_A);
        assertEquals("\"VALUE_A\"", sw.toString());

        JsonReader jr = new JsonReader(new StringReader("\"VALUE_B\""));
        assertEquals(DummyEnum.VALUE_B, adapter.read(jr));

        assertNull(adapter.read(new JsonReader(new StringReader("null"))));
    }

    enum DummyEnum {
        VALUE_A, VALUE_B
    }
}