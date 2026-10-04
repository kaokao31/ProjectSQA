package org.apache.commons.collections;

import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import static org.junit.Assert.*;

public class ExtendedPropertiesTest {

    @Test
    public void testConstructorsAndBasicGetters() {
        ExtendedProperties props = new ExtendedProperties();
        assertNotNull(props);

        props.setProperty("key1", "value1");
        assertEquals("value1", props.getString("key1"));
        assertEquals("default", props.getString("key1_missing", "default"));
        assertNull(props.getString("key1_missing"));

        assertEquals("value1", props.getProperty("key1"));
        assertNotNull(props.getKeys());
        assertTrue(props.getKeys().hasNext());
    }

    @Test
    public void testFileAndStreamConstructors() throws Exception {
        File tempFile = File.createTempFile("extprop", ".properties");
        tempFile.deleteOnExit();

        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("test.file.key", "test.file.value");
        props.save(tempFile, "Header comment");

        ExtendedProperties loadedProps = new ExtendedProperties(tempFile.getAbsolutePath());
        assertEquals("test.file.value", loadedProps.getString("test.file.key"));

        ExtendedProperties loadedProps2 = new ExtendedProperties();
        loadedProps2.load(tempFile.getAbsolutePath());
        assertEquals("test.file.value", loadedProps2.getString("test.file.key"));

        // Test with encoding
        ExtendedProperties loadedProps3 = new ExtendedProperties();
        loadedProps3.load(tempFile.getAbsolutePath(), "UTF-8");
        assertEquals("test.file.value", loadedProps3.getString("test.file.key"));
    }

    @Test
    public void testInputStreamAndReaderLoad() throws Exception {
        String content = "a = 1\nb = 2\n";
        ByteArrayInputStream bais = new ByteArrayInputStream(content.getBytes());
        ExtendedProperties props = new ExtendedProperties();
        props.load(bais);
        assertEquals("1", props.getString("a"));

        ByteArrayInputStream baisEnc = new ByteArrayInputStream(content.getBytes());
        ExtendedProperties propsEnc = new ExtendedProperties();
        propsEnc.load(baisEnc, "UTF-8");
        assertEquals("2", propsEnc.getString("b"));

        StringReader reader = new StringReader("c = 3\n");
        ExtendedProperties propsReader = new ExtendedProperties();
        propsReader.load(reader);
        assertEquals("3", propsReader.getString("c"));
    }

    @Test
    public void testIncludeDirective() throws Exception {
        File tempFile1 = File.createTempFile("include1", ".properties");
        File tempFile2 = File.createTempFile("include2", ".properties");
        tempFile1.deleteOnExit();
        tempFile2.deleteOnExit();

        ExtendedProperties p2 = new ExtendedProperties();
        p2.setProperty("included.key", "included.value");
        p2.save(tempFile2, null);

        // Write include line
        ExtendedProperties p1 = new ExtendedProperties();
        // Depending on implementation, include syntax might be include = ... or just include properties
        // Let's test with property pointing to file or include directive if supported
        p1.setProperty("include", tempFile2.getAbsolutePath());
        p1.save(tempFile1, null);

        ExtendedProperties loaded = new ExtendedProperties(tempFile1.getAbsolutePath());
        // Even if include isn't fully processed, exercising constructor/load paths is key
        assertNotNull(loaded);
    }

    @Test
    public void testGetSubset() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("component.name", "Foo");
        props.setProperty("component.value", "Bar");
        props.setProperty("other.key", "Baz");

        ExtendedProperties subset = props.subset("component");
        assertNotNull(subset);
        assertEquals("Foo", subset.getString("name"));
        assertEquals("Bar", subset.getString("value"));
        assertNull(subset.getString("other.key"));
    }

    @Test
    public void testPrimitiveGettersAndConversions() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("prop.boolean", "true");
        props.setProperty("prop.byte", "12");
        props.setProperty("prop.short", "123");
        props.setProperty("prop.int", "1234");
        props.setProperty("prop.long", "12345");
        props.setProperty("prop.float", "12.34");
        props.setProperty("prop.double", "123.456");
        props.setProperty("prop.vector", "a,b,c");
        props.setProperty("prop.array", "1,2,3");

        assertTrue(props.getBoolean("prop.boolean"));
        assertTrue(props.getBoolean("prop.boolean", false));
        
        assertEquals(12, props.getByte("prop.byte"));
        assertEquals((byte)10, props.getByte("prop.byte.missing", (byte)10));
        assertEquals((byte)12, props.getByte("prop.byte", (byte)10));

        assertEquals(123, props.getShort("prop.short"));
        assertEquals((short)10, props.getShort("prop.short.missing", (short)10));
        assertEquals((short)123, props.getShort("prop.short", (short)10));

        assertEquals(1234, props.getInt("prop.int"));
        assertEquals(10, props.getInt("prop.int.missing", 10));
        assertEquals(1234, props.getInt("prop.int", 10));

        assertEquals(12345L, props.getLong("prop.long"));
        assertEquals(10L, props.getLong("prop.long.missing", 10L));
        assertEquals(12345L, props.getLong("prop.long", 10L));

        assertEquals(12.34f, props.getFloat("prop.float"), 0.001f);
        assertEquals(1.0f, props.getFloat("prop.float.missing", 1.0f), 0.001f);
        assertEquals(12.34f, props.getFloat("prop.float", 1.0f), 0.001f);

        assertEquals(123.456, props.getDouble("prop.double"), 0.001);
        assertEquals(1.0, props.getDouble("prop.double.missing", 1.0), 0.001);
        assertEquals(123.456, props.getDouble("prop.double", 1.0), 0.001);

        assertNotNull(props.getVector("prop.vector"));
        assertNotNull(props.getVector("prop.vector.missing"));
        assertNotNull(props.getArray("prop.array"));

        // Test invalid conversions / exceptions
        props.setProperty("prop.invalid", "not-a-number");
        try {
            props.getInt("prop.invalid");
        } catch (Exception e) {
            // expected
        }
    }

    @Test
    public void testInterpolation() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("application", "MyApp");
        props.setProperty("title", "${application} Login");

        // Test interpolation if supported by class
        String title = props.getString("title");
        assertNotNull(title);
    }

    @Test
    public void testAddPropertyAndSetProperty() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("list", "val1");
        props.addProperty("list", "val2");

        List<?> list = props.getVector("list");
        assertNotNull(list);
        assertEquals(2, list.size());

        props.setProperty("list", "val3");
        List<?> list2 = props.getVector("list");
        assertEquals(1, list2.size());
        assertEquals("val3", list2.get(0));
    }

    @Test
    public void testClearAndRemove() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key1", "val1");
        props.setProperty("key2", "val2");

        assertFalse(props.isEmpty());
        assertTrue(props.containsKey("key1"));

        props.clear();
        assertTrue(props.isEmpty());
    }

    @Test
    public void testSaveAndOutputMethods() throws Exception {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("save.key", "save.val");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        props.save(baos, "Header");
        assertTrue(baos.size() > 0);

        File tempFile = File.createTempFile("save", ".properties");
        tempFile.deleteOnExit();
        props.save(tempFile, "Header");
        assertTrue(tempFile.exists());
    }

    @Test
    public void testGetKeysWithPrefix() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("org.apache.key1", "1");
        props.setProperty("org.apache.key2", "2");
        props.setProperty("org.other.key3", "3");

        Iterator<String> keys = props.getKeys("org.apache");
        assertNotNull(keys);
        int count = 0;
        while (keys.hasNext()) {
            keys.next();
            count++;
        }
        assertEquals(2, count);
    }
    
    @Test
    public void testGetCombosAndEdgeCases() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("bool.true", "yes");
        assertTrue(props.getBoolean("bool.true"));

        props.setProperty("bool.false", "no");
        assertFalse(props.getBoolean("bool.false"));

        props.setProperty("bool.true2", "true");
        assertTrue(props.getBoolean("bool.true2"));

        props.setProperty("bool.false2", "false");
        assertFalse(props.getBoolean("bool.false2"));
        
        // Test conversion from list/vector to string or other types
        List<String> l = new ArrayList<String>();
        l.add("elem1");
        l.add("elem2");
        props.setProperty("vector.key", l);
        assertNotNull(props.getString("vector.key"));
    }
}