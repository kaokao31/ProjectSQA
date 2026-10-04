package org.apache.commons.collections.map;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;

public class ExtendedPropertiesTest {

    @Test
    public void testConstructorsAndDefaults() {
        ExtendedProperties props1 = new ExtendedProperties();
        assertNotNull(props1);

        ExtendedProperties props2 = new ExtendedProperties("nonExistentFile.properties");
        assertNotNull(props2);

        ExtendedProperties props3 = new ExtendedProperties();
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.setProperty("defaultKey", "defaultValue");
        props3.defaults = defaults;
        
        assertEquals("defaultValue", props3.getString("defaultKey"));
    }

    @Test
    public void testFileLoadingAndSave() throws IOException {
        File tempFile = File.createTempFile("testProps", ".properties");
        tempFile.deleteOnExit();

        FileWriter writer = new FileWriter(tempFile);
        writer.write("test.key = testValue\n");
        writer.write("include = nonExistentInclude.properties\n");
        writer.close();

        ExtendedProperties props = new ExtendedProperties(tempFile.getAbsolutePath());
        assertEquals("testValue", props.getString("test.key"));

        File saveFile = File.createTempFile("saveProps", ".properties");
        saveFile.deleteOnExit();

        props.save(saveFile, "Header comment");
        assertTrue(saveFile.exists());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        props.save(out, "Header");
        assertTrue(out.size() > 0);

        props.clear();
        assertTrue(props.isEmpty());

        tempFile.delete();
        saveFile.delete();
    }

    @Test
    public void testGetPropertyAndBasicTypes() {
        ExtendedProperties props = new ExtendedProperties();
        
        props.setProperty("strKey", "hello");
        props.setProperty("intKey", "123");
        props.setProperty("longKey", "123456789");
        props.setProperty("floatKey", "12.34");
        props.setProperty("doubleKey", "123.456");
        props.setProperty("boolKey", "true");
        props.setProperty("boolFalseKey", "false");

        assertEquals("hello", props.getString("strKey"));
        assertEquals("default", props.getString("strKey", "default"));
        assertEquals("default", props.getString("missingKey", "default"));

        assertEquals(123, props.getInt("intKey"));
        assertEquals(99, props.getInt("intKey", 99));
        assertEquals(99, props.getInt("missingKey", 99));

        assertEquals(123L, props.getLong("longKey"));
        assertEquals(99L, props.getLong("longKey", 99L));
        assertEquals(99L, props.getLong("missingKey", 99L));

        assertEquals(12.34f, props.getFloat("floatKey"), 0.001f);
        assertEquals(9.9f, props.getFloat("floatKey", 9.9f), 0.001f);
        assertEquals(9.9f, props.getFloat("missingKey", 9.9f), 0.001f);

        assertEquals(123.456, props.getDouble("doubleKey"), 0.001);
        assertEquals(9.9, props.getDouble("doubleKey", 9.9), 0.001);
        assertEquals(9.9, props.getDouble("missingKey", 9.9), 0.001);

        assertTrue(props.getBoolean("boolKey"));
        assertFalse(props.getBoolean("boolFalseKey"));
        assertTrue(props.getBoolean("boolKey", false));
        assertFalse(props.getBoolean("missingKey", false));
    }

    @Test
    public void testVectorAndListProperties() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("listKey", "a, b, c");
        props.addProperty("listKey", "d");

        List vector = props.getVector("listKey");
        assertNotNull(vector);
        assertEquals(4, vector.size());

        List missingVector = props.getVector("missingKey");
        assertNotNull(missingVector);
        assertTrue(missingVector.isEmpty());

        List defaultVector = props.getVector("missingKey", vector);
        assertEquals(vector, defaultVector);

        List list = props.getList("listKey");
        assertNotNull(list);
        
        List missingList = props.getList("missingKey");
        assertNotNull(missingList);
        assertTrue(missingList.isEmpty());
        
        List defaultList = props.getList("missingKey", list);
        assertEquals(list, defaultList);
    }

    @Test
    public void testSubsetAndInterpolation() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("base.name", "Apache");
        props.setProperty("base.project", "${base.name} Commons");

        ExtendedProperties subset = props.subset("base");
        assertNotNull(subset);
        assertEquals("Apache", subset.getString("name"));
        
        // Test interpolation mechanism
        assertEquals("Apache Commons", props.getString("base.project"));
    }

    @Test
    public void testKeysIterator() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("k1", "v1");
        props.setProperty("k2", "v2");

        Iterator<String> keys = props.getKeys();
        assertNotNull(keys);
        int count = 0;
        while (keys.hasNext()) {
            assertNotNull(keys.next());
            count++;
        }
        assertEquals(2, count);

        Iterator<String> subsetKeys = props.getKeys("k");
        assertNotNull(subsetKeys);
        assertTrue(subsetKeys.hasNext());
    }

    @Test
    public void testTestLoadWithInputStream() throws IOException {
        String content = "a=1\nb=2\n";
        ByteArrayInputStream bais = new ByteArrayInputStream(content.getBytes());
        
        ExtendedProperties props = new ExtendedProperties();
        props.load(bais);
        
        assertEquals("1", props.getString("a"));
        assertEquals("2", props.getString("b"));
    }

    @Test(expected = NoSuchElementException.class)
    public void testThrowExceptionWhenMissingStrict() {
        ExtendedProperties props = new ExtendedProperties();
        props.getString("non.existent.strict", true);
    }

    @Test
    public void testCombineProperties() {
        ExtendedProperties props1 = new ExtendedProperties();
        props1.setProperty("key1", "val1");
        
        ExtendedProperties props2 = new ExtendedProperties();
        props2.setProperty("key2", "val2");
        
        props1.combine(props2);
        assertEquals("val2", props1.getString("key2"));
    }

    @Test
    public void testInterpolateObject() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("test.prop", "resolved");
        
        Object interpolated = props.interpolate("${test.prop}");
        assertEquals("resolved", interpolated);

        Object notString = Integer.valueOf(123);
        assertEquals(notString, props.interpolate(notString));
        
        assertNull(props.interpolate(null));
    }
}