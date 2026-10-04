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
        tempFile = File.createTempFile("test-props", ".properties");
    }

    @After
    public void tearDown() throws Exception {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    public void testDefaultConstructors() {
        ExtendedProperties props = new ExtendedProperties();
        assertNotNull(props);
        assertFalse(props.isInclude("include"));
    }

    @Test
    public void testConstructorWithFile() throws Exception {
        FileWriter writer = new FileWriter(tempFile);
        writer.write("key1 = value1\n");
        writer.write("key2 = value2,value3\n");
        writer.close();

        ExtendedProperties props = new ExtendedProperties(tempFile.getAbsolutePath());
        assertEquals("value1", props.getString("key1"));
        assertEquals("value2", props.getString("key2"));
    }

    @Test
    public void testConstructorWithFileAndEncoding() throws Exception {
        FileWriter writer = new FileWriter(tempFile);
        writer.write("key1 = value1\n");
        writer.close();

        ExtendedProperties props = new ExtendedProperties(tempFile.getAbsolutePath(), "UTF-8");
        assertEquals("value1", props.getString("key1"));
    }

    @Test
    public void testLoadAndSave() throws Exception {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("test.key", "test.value");

        props.save(tempFile, "Header comment");
        assertTrue(tempFile.exists());

        ExtendedProperties props2 = new ExtendedProperties(tempFile.getAbsolutePath());
        assertEquals("test.value", props2.getString("test.key"));
    }

    @Test
    public void testLoadFromStringReader() throws Exception {
        String content = "prop1 = val1\nprop2 = val2";
        StringReader reader = new StringReader(content);
        ExtendedProperties props = new ExtendedProperties();
        props.load(reader);

        assertEquals("val1", props.getString("prop1"));
        assertEquals("val2", props.getString("prop2"));
    }

    @Test
    public void testGettersAndSettersTypes() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("string", "hello");
        props.setProperty("boolean", "true");
        props.setProperty("boolean_false", "false");
        props.setProperty("int", "123");
        props.setProperty("long", "123456789");
        props.setProperty("float", "12.34");
        props.setProperty("double", "123.456");
        props.setProperty("short", "12");
        props.setProperty("byte", "1");

        assertEquals("hello", props.getString("string"));
        assertEquals("default", props.getString("nonexistent", "default"));
        
        assertTrue(props.getBoolean("boolean"));
        assertFalse(props.getBoolean("boolean_false"));
        assertTrue(props.getBoolean("nonexistent", true));

        assertEquals(123, props.getInt("int"));
        assertEquals(99, props.getInt("nonexistent", 99));

        assertEquals(123456789L, props.getLong("long"));
        assertEquals(99L, props.getLong("nonexistent", 99L));

        assertEquals(12.34f, props.getFloat("float"), 0.001f);
        assertEquals(9.9f, props.getFloat("nonexistent", 9.9f), 0.001f);

        assertEquals(123.456, props.getDouble("double"), 0.001);
        assertEquals(99.9, props.getDouble("nonexistent", 99.9), 0.001);

        assertEquals((short) 12, props.getShort("short"));
        assertEquals((short) 5, props.getShort("nonexistent", (short) 5));

        assertEquals((byte) 1, props.getByte("byte"));
        assertEquals((byte) 5, props.getByte("nonexistent", (byte) 5));
    }

    @Test
    public void testGetVectorAndList() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("list", "a");
        props.addProperty("list", "b");
        props.addProperty("list", "c");

        Vector vector = props.getVector("list");
        assertNotNull(vector);
        assertEquals(3, vector.size());
        assertEquals("a", vector.get(0));

        List list = props.getList("list");
        assertNotNull(list);
        assertEquals(3, list.size());
        assertEquals("b", list.get(1));

        assertNotNull(props.getVector("nonexistent", new Vector()));
        assertNotNull(props.getList("nonexistent", new ArrayList()));
    }

    @Test
    public void testGetSubset() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("org.apache.key1", "val1");
        props.setProperty("org.apache.key2", "val2");
        props.setProperty("other.key", "val3");

        ExtendedProperties subset = props.subset("org.apache");
        assertNotNull(subset);
        assertEquals("val1", subset.getString("key1"));
        assertEquals("val2", subset.getString("key2"));
        assertNull(subset.getString("other.key"));
    }

    @Test
    public void testInterpolation() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("application.name", "MyApp");
        props.setProperty("application.home", "/home/${application.name}");

        assertEquals("/home/MyApp", props.getString("application.home"));
    }

    @Test
    public void testIncludeDirective() throws Exception {
        File includedFile = File.createTempFile("included", ".properties");
        FileWriter writer = new FileWriter(includedFile);
        writer.write("included.key = included.value\n");
        writer.close();

        File mainFile = File.createTempFile("main", ".properties");
        FileWriter mainWriter = new FileWriter(mainFile);
        mainWriter.write("include = " + includedFile.getAbsolutePath() + "\n");
        mainWriter.write("main.key = main.value\n");
        mainWriter.close();

        ExtendedProperties props = new ExtendedProperties(mainFile.getAbsolutePath());
        assertEquals("main.value", props.getString("main.key"));
        assertEquals("included.value", props.getString("included.key"));

        includedFile.delete();
        mainFile.delete();
    }

    @Test
    public void testGetKeys() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key1", "val1");
        props.setProperty("key2", "val2");

        Iterator keys = props.getKeys();
        assertNotNull(keys);
        List<String> keyList = new ArrayList<>();
        while (keys.hasNext()) {
            keyList.add((String) keys.next());
        }
        assertTrue(keyList.contains("key1"));
        assertTrue(keyList.contains("key2"));

        Iterator prefixKeys = props.getKeys("key");
        assertNotNull(prefixKeys);
    }

    @Test
    public void testClearAndContainsKey() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key1", "val1");
        assertTrue(props.containsKey("key1"));
        assertFalse(props.containsKey("nonexistent"));

        props.clearProperty("key1");
        assertFalse(props.containsKey("key1"));

        props.setProperty("key2", "val2");
        assertFalse(props.isEmpty());
        props.clear();
        assertTrue(props.isEmpty());
    }

    @Test
    public void testCombine() {
        ExtendedProperties props1 = new ExtendedProperties();
        props1.setProperty("key1", "val1");

        ExtendedProperties props2 = new ExtendedProperties();
        props2.setProperty("key2", "val2");

        props1.combine(props2);
        assertEquals("val1", props1.getString("key1"));
        assertEquals("val2", props1.getString("key2"));
    }

    @Test
    public void testIncludePath() {
        ExtendedProperties props = new ExtendedProperties();
        props.setInclude("include");
        assertTrue(props.isInclude("include"));
        props.setIncludePath(new String[]{"/path"});
        assertNotNull(props.getBasePath());
    }
}