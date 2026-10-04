package org.apache.commons.collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

import static org.junit.Assert.*;

public class ExtendedPropertiesTest {

    private File tempFile;

    @Before
    public void setUp() throws Exception {
        tempFile = File.createTempFile("extended_properties_test", ".properties");
    }

    @After
    public void tearDown() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    public void testDefaultConstructor() {
        ExtendedProperties props = new ExtendedProperties();
        assertTrue(props.isEmpty());
    }

    @Test
    public void testConstructorsWithFileAndString() throws Exception {
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("test.key=test.value\n");
        }

        ExtendedProperties props1 = new ExtendedProperties(tempFile.getAbsolutePath());
        assertEquals("test.value", props1.getString("test.key"));

        ExtendedProperties props2 = new ExtendedProperties(tempFile.getAbsolutePath(), "UTF-8");
        assertEquals("test.value", props2.getString("test.key"));
    }

    @Test
    public void testLoadAndSave() throws Exception {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key1", "value1");
        props.setProperty("key2", "value2");

        props.save(tempFile, "Header comment");
        assertTrue(tempFile.exists());

        ExtendedProperties loadedProps = new ExtendedProperties();
        try (java.io.FileReader reader = new java.io.FileReader(tempFile)) {
            loadedProps.load(reader);
        }

        assertEquals("value1", loadedProps.getString("key1"));
        assertEquals("value2", loadedProps.getString("key2"));
    }

    @Test
    public void testLoadFromStringReader() throws Exception {
        String data = "a=1\nb=2\n";
        ExtendedProperties props = new ExtendedProperties();
        props.load(new StringReader(data));
        assertEquals("1", props.getString("a"));
        assertEquals("2", props.getString("b"));
    }

    @Test(expected = RuntimeException.class)
    public void testLoadInvalidStream() throws Exception {
        ExtendedProperties props = new ExtendedProperties();
        // Passing null or throwing exception during load
        props.load(null);
    }

    @Test
    public void testGettersAndSettersPrimitives() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("bool.true", "true");
        props.setProperty("bool.false", "false");
        props.setProperty("int.val", "42");
        props.setProperty("long.val", "123456789");
        props.setProperty("float.val", "3.14");
        props.setProperty("double.val", "2.71828");
        props.setProperty("short.val", "10");
        props.setProperty("byte.val", "5");

        assertTrue(props.getBoolean("bool.true"));
        assertFalse(props.getBoolean("bool.false"));
        assertTrue(props.getBoolean("bool.missing", true));
        assertFalse(props.getBoolean("bool.missing", false));

        assertEquals(42, props.getInt("int.val"));
        assertEquals(100, props.getInt("int.missing", 100));

        assertEquals(123456789L, props.getLong("long.val"));
        assertEquals(999L, props.getLong("long.missing", 999L));

        assertEquals(3.14f, props.getFloat("float.val"), 0.001f);
        assertEquals(1.23f, props.getFloat("float.missing", 1.23f), 0.001f);

        assertEquals(2.71828, props.getDouble("double.val"), 0.00001);
        assertEquals(4.56, props.getDouble("double.missing", 4.56), 0.00001);

        assertEquals(10, props.getShort("short.val"));
        assertEquals(15, props.getShort("short.missing", (short) 15));

        assertEquals(5, props.getByte("byte.val"));
        assertEquals(7, props.getByte("byte.missing", (byte) 7));
    }

    @Test
    public void testGetVector() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("list", "a, b, c");
        
        Vector<Object> vec = props.getVector("list");
        assertNotNull(vec);
        assertEquals(3, vec.size());
        assertEquals("a", vec.get(0));
        assertEquals("b", vec.get(1));
        assertEquals("c", vec.get(2));

        Vector<Object> defaultVec = new Vector<>();
        assertSame(defaultVec, props.getVector("missing.list", defaultVec));
    }

    @Test
    printList:
    public void testGetList() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("list", "item1, item2");

        List<Object> list = props.getList("list");
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("item1", list.get(0));
        assertEquals("item2", list.get(1));

        List<Object> defaultList = new ArrayList<>();
        assertSame(defaultList, props.getList("missing.list", defaultList));
    }

    @Test
    public void testGetSubset() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("database.host", "localhost");
        props.setProperty("database.port", "3306");
        props.setProperty("server.name", "myserver");

        ExtendedProperties subset = props.subset("database");
        assertNotNull(subset);
        assertEquals("localhost", subset.getString("host"));
        assertEquals("3306", subset.getString("port"));
        assertNull(subset.getString("server.name"));
    }

    @Test
    public void testInterpolation() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("application.name", "MyApp");
        props.setProperty("application.path", "/app");
        props.setProperty("application.url", "${application.path}/home");

        assertEquals("/app/home", props.getString("application.url"));
    }

    @Test
    public void testAddPropertyAndSetProperty() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", "val1");
        props.addProperty("key", "val2");

        List<Object> list = props.getList("key");
        assertEquals(2, list.size());
        assertEquals("val1", list.get(0));
        assertEquals("val2", list.get(1));

        props.setProperty("key", "val3");
        List<Object> listAfterSet = props.getList("key");
        assertEquals(1, listAfterSet.size());
        assertEquals("val3", listAfterSet.get(0));
    }

    @Test
    public void testClearAndContainsKey() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("test", "value");
        assertTrue(props.containsKey("test"));

        props.clear();
        assertFalse(props.containsKey("test"));
        assertTrue(props.isEmpty());
    }

    @Test
    public void testGetKeys() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("k1", "v1");
        props.setProperty("k2", "v2");

        Iterator<String> keys = props.getKeys();
        assertNotNull(keys);
        List<String> keyList = new ArrayList<>();
        while (keys.hasNext()) {
            keyList.add(keys.next());
        }
        assertEquals(2, keyList.size());
        assertTrue(keyList.contains("k1"));
        assertTrue(keyList.contains("k2"));

        Iterator<String> prefixKeys = props.getKeys("k");
        assertNotNull(prefixKeys);
    }

    @Test
    public void testIncludeDirective() throws Exception {
        File includeFile = File.createTempFile("include", ".properties");
        try (FileWriter writer = new FileWriter(includeFile)) {
            writer.write("included.key=included.value\n");
        }

        File mainFile = File.createTempFile("main", ".properties");
        try (FileWriter writer = new FileWriter(mainFile)) {
            writer.write("include=" + includeFile.getAbsolutePath() + "\n");
        }

        ExtendedProperties props = new ExtendedProperties(mainFile.getAbsolutePath());
        assertEquals("included.value", props.getString("included.key"));

        includeFile.delete();
        mainFile.delete();
    }

    @Test
    public void testGetBooleanExceptionsAndDefaults() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("invalid.bool", "not-a-boolean");
        
        // Should return default when parsing fails or type mismatch occurs
        assertTrue(props.getBoolean("invalid.bool", true));
        assertFalse(props.getBoolean("invalid.bool", false));
    }

    @Test
    public void testNumberConversionEdgeCases() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("bad.int", "abc");
        props.setProperty("bad.long", "abc");
        props.setProperty("bad.float", "abc");
        props.setProperty("bad.double", "abc");
        props.setProperty("bad.short", "abc");
        props.setProperty("bad.byte", "abc");

        assertEquals(10, props.getInt("bad.int", 10));
        assertEquals(20L, props.getLong("bad.long", 20L));
        assertEquals(30.0f, props.getFloat("bad.float", 30.0f), 0.001f);
        assertEquals(40.0, props.getDouble("bad.double", 40.0), 0.001);
        assertEquals((short) 50, props.getShort("bad.short", (short) 50));
        assertEquals((byte) 60, props.getByte("bad.byte", (byte) 60));
    }

    @Test
    public void testConfigurationInterpolator() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("user", "john");
        props.setProperty("greeting", "Hello ${user}");
        assertEquals("Hello john", props.getString("greeting"));
    }

    @Test
    public void testIncludeWithoutExtension() {
        ExtendedProperties props = new ExtendedProperties();
        assertNotNull(props);
    }
}