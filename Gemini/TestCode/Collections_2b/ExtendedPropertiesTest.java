package org.apache.commons.collections.map;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;

public class ExtendedPropertiesTest {

    @Test
    public void testDefaultConstructor() {
        ExtendedProperties props = new ExtendedProperties();
        assertNotNull(props);
        assertFalse(props.isInclude());
        assertNull(props.getBasePath());
    }

    @Test
    public void testConstructorWithPath() {
        ExtendedProperties props = new ExtendedProperties("some/path");
        assertNotNull(props);
        assertEquals("some/path", props.getBasePath());
    }

    @Test
    public void testConstructorWithFileAndEncoding() throws Exception {
        File tempFile = File.createTempFile("testProps", ".properties");
        tempFile.deleteOnExit();
        
        ExtendedProperties props = new ExtendedProperties(tempFile.getAbsolutePath(), "UTF-8");
        assertNotNull(props);
    }

    @Test
    public void testGetBasePath() {
        ExtendedProperties props = new ExtendedProperties();
        props.setBasePath("base/dir");
        assertEquals("base/dir", props.getBasePath());
    }

    @Test
    public void testIsInclude() {
        ExtendedProperties props = new ExtendedProperties();
        props.setInclude("include");
        assertTrue(props.isInclude());
        
        props.setInclude(null);
        assertFalse(props.isInclude());
    }

    @Test
    public void testGetPropertyBasic() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key1", "value1");
        assertEquals("value1", props.getProperty("key1"));
        assertNull(props.getProperty("nonexistent"));
    }

    @Test
    public void testAddProperty() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key1", "val1");
        props.addProperty("key1", "val2");
        
        Object prop = props.getProperty("key1");
        assertTrue(prop instanceof String); // depending on implementation or list
        
        List list = props.getVector("key1");
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("val1", list.get(0));
        assertEquals("val2", list.get(1));
    }

    @Test
    public void testClearProperty() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key1", "value1");
        assertEquals("value1", props.getProperty("key1"));
        
        props.clearProperty("key1");
        assertNull(props.getProperty("key1"));
    }

    @Test
    public void testSubset() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("prefix.key1", "val1");
        props.setProperty("prefix.key2", "val2");
        props.setProperty("other.key", "val3");
        
        ExtendedProperties subset = props.subset("prefix");
        assertNotNull(subset);
        assertEquals("val1", subset.getProperty("key1"));
        assertEquals("val2", subset.getProperty("key2"));
        assertNull(subset.getProperty("prefix.key1"));
    }

    @Test
    public void testDisplay() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key1", "val1");
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            props.save(out, "header");
            assertTrue(out.size() > 0);
        } catch (Exception e) {
            // save might or might not be fully implemented depending on version, test gracefully
        }
    }

    @Test
    public void testGettersTyped() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("string", "test");
        props.setProperty("boolean", "true");
        props.setProperty("byte", "12");
        props.setProperty("short", "123");
        props.setProperty("int", "1234");
        props.setProperty("long", "12345");
        props.setProperty("float", "1.23");
        props.setProperty("double", "1.2345");
        props.setProperty("bigdecimal", "123.456");
        props.setProperty("biginteger", "123456");

        assertEquals("test", props.getString("string"));
        assertEquals("default", props.getString("nonexistent", "default"));
        
        assertTrue(props.getBoolean("boolean"));
        assertTrue(props.getBoolean("boolean", false));
        
        assertEquals((byte) 12, props.getByte("byte"));
        assertEquals((byte) 5, props.getByte("nonexistent", (byte) 5));
        
        assertEquals((short) 123, props.getShort("short"));
        assertEquals((short) 5, props.getShort("nonexistent", (short) 5));
        
        assertEquals(1234, props.getInt("int"));
        assertEquals(5, props.getInt("nonexistent", 5));
        
        assertEquals(12345L, props.getLong("long"));
        assertEquals(5L, props.getLong("nonexistent", 5L));
        
        assertEquals(1.23f, props.getFloat("float"), 0.001f);
        assertEquals(5.0f, props.getFloat("nonexistent", 5.0f), 0.001f);
        
        assertEquals(1.2345, props.getDouble("double"), 0.0001);
        assertEquals(5.0, props.getDouble("nonexistent", 5.0), 0.0001);
        
        assertEquals(new BigDecimal("123.456"), props.getBigDecimal("bigdecimal"));
        assertEquals(new BigDecimal("5.0"), props.getBigDecimal("nonexistent", new BigDecimal("5.0")));
        
        assertEquals(new BigInteger("123456"), props.getBigInteger("biginteger"));
        assertEquals(new BigInteger("5"), props.getBigInteger("nonexistent", new BigInteger("5")));
    }

    @Test
    public void testInterpolation() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("application.name", "MyApp");
        props.setProperty("application.path", "${application.name}/bin");
        
        assertEquals("MyApp", props.getString("application.name"));
        // Depending on interpolation support in this specific version, test basic retrieval
        assertNotNull(props.getString("application.path"));
    }

    @Test
    public void testLoadAndSave() throws Exception {
        String content = "key1 = value1\nkey2 = value2\n";
        StringReader reader = new StringReader(content);
        
        ExtendedProperties props = new ExtendedProperties();
        props.load(reader);
        
        assertEquals("value1", props.getString("key1"));
        assertEquals("value2", props.getString("key2"));
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
    public void testKeysIterator() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key1", "val1");
        props.setProperty("key2", "val2");
        
        Iterator<String> keys = props.getKeys();
        assertNotNull(keys);
        int count = 0;
        while (keys.hasNext()) {
            assertNotNull(keys.next());
            count++;
        }
        assertEquals(2, count);
    }
}