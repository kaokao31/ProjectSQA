package org.apache.commons.collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

public class ExtendedPropertiesTest {

    private ExtendedProperties extendedProperties;

    @Before
    public void setUp() {
        extendedProperties = new ExtendedProperties();
    }

    // ==================== Basic put/get tests ====================

    @Test
    public void testPutAndGetString() {
        extendedProperties.put("key1", "value1");
        assertEquals("value1", extendedProperties.getString("key1"));
    }

    @Test
    public void testGetStringWithDefault() {
        assertEquals("default", extendedProperties.getString("nonexistent", "default"));
    }

    @Test
    public void testGetStringNullKey() {
        extendedProperties.put(null, "nullkey");
        assertNull(extendedProperties.getString(null));
    }

    @Test
    public void testGetStringNullValue() {
        extendedProperties.put("nullval", null);
        assertNull(extendedProperties.getString("nullval"));
    }

    // ==================== Interpolation tests ====================

    @Test
    public void testInterpolateSimple() {
        extendedProperties.put("var", "world");
        extendedProperties.put("greeting", "Hello ${var}");
        assertEquals("Hello world", extendedProperties.getString("greeting"));
    }

    @Test
    public void testInterpolateUndefinedVariable() {
        extendedProperties.put("test", "value ${undefined}");
        assertEquals("value ${undefined}", extendedProperties.getString("test"));
    }

    @Test
    public void testInterpolateEscapedDollar() {
        // The backslash escapes the $ so it should be treated as literal
        extendedProperties.put("escaped", "\\${notvar}");
        // Depending on implementation, the backslash might be removed or kept
        // Expected: either "${notvar}" or "\\${notvar}"
        String result = extendedProperties.getString("escaped");
        assertNotNull(result);
        assertTrue(result.contains("${notvar}") || result.contains("\\${notvar}"));
    }

    @Test
    public void testInterpolateEscapedBackslash() {
        extendedProperties.put("backslash", "value\\\\");
        assertEquals("value\\\\", extendedProperties.getString("backslash"));
    }

    @Test
    public void testInterpolateNested() {
        extendedProperties.put("outer", "outer ${inner}");
        extendedProperties.put("inner", "inner");
        assertEquals("outer inner", extendedProperties.getString("outer"));
    }

    @Test
    public void testInterpolateMultiple() {
        extendedProperties.put("a", "A");
        extendedProperties.put("b", "B");
        extendedProperties.put("ab", "${a} and ${b}");
        assertEquals("A and B", extendedProperties.getString("ab"));
    }

    @Test
    public void testInterpolateRecursive() {
        extendedProperties.put("rec", "${rec}");
        String result = extendedProperties.getString("rec");
        assertTrue(result.contains("${rec}") || result.equals("${rec}"));
    }

    // ==================== Properties file loading ====================

    @Test
    public void testLoadFromStream() throws IOException {
        String propContent = "key=value\nfoo=bar\n";
        InputStream is = new ByteArrayInputStream(propContent.getBytes());
        extendedProperties.load(is);
        assertEquals("value", extendedProperties.getString("key"));
        assertEquals("bar", extendedProperties.getString("foo"));
    }

    @Test(expected = IOException.class)
    public void testLoadFromInvalidStream() throws IOException {
        InputStream is = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("test exception");
            }
        };
        extendedProperties.load(is);
    }

    @Test
    public void testLoadFromEmptyStream() throws IOException {
        InputStream is = new ByteArrayInputStream(new byte[0]);
        extendedProperties.load(is);
        assertTrue(extendedProperties.isEmpty());
    }

    // ==================== combine/subset tests ====================

    @Test
    public void testCombine() {
        ExtendedProperties other = new ExtendedProperties();
        other.put("key1", "fromOther");
        other.put("key2", "value2");
        extendedProperties.put("key1", "original");
        extendedProperties.combine(other);
        // combine overwrites existing keys
        assertEquals("fromOther", extendedProperties.getString("key1"));
        assertEquals("value2", extendedProperties.getString("key2"));
    }

    @Test
    public void testCombineEmptyOther() {
        ExtendedProperties other = new ExtendedProperties();
        extendedProperties.put("key", "value");
        extendedProperties.combine(other);
        assertEquals("value", extendedProperties.getString("key"));
    }

    @Test
    public void testSubset() {
        extendedProperties.put("prefix.key1", "val1");
        extendedProperties.put("prefix.key2", "val2");
        extendedProperties.put("other.key", "val3");
        ExtendedProperties subset = extendedProperties.subset("prefix");
        assertEquals(2, subset.size());
        assertEquals("val1", subset.getString("key1"));
        assertEquals("val2", subset.getString("key2"));
    }

    @Test
    public void testSubsetNoMatch() {
        extendedProperties.put("key", "value");
        ExtendedProperties subset = extendedProperties.subset("nonexistent");
        assertTrue(subset.isEmpty());
    }

    @Test
    public void testSubsetWithNullPrefix() {
        try {
            extendedProperties.subset(null);
            fail("Expected IllegalArgumentException for null prefix");
        } catch (IllegalArgumentException e) {
            // expected
        } catch (NullPointerException e) {
            // also acceptable
        }
    }

    // ==================== getList / getStringArray tests ====================

    @Test
    public void testGetList() {
        extendedProperties.put("list", "a,b,c");
        List<String> list = extendedProperties.getList("list");
        assertEquals(3, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c", list.get(2));
    }

    @Test
    public void testGetListWithDefaultSeparator() {
        extendedProperties.put("list", "x;y;z");
        // default separator is comma, so whole string is one element
        List<String> list = extendedProperties.getList("list");
        assertEquals(1, list.size());
        assertEquals("x;y;z", list.get(0));
    }

    @Test
    public void testGetListEmpty() {
        extendedProperties.put("empty", "");
        List<String> list = extendedProperties.getList("empty");
        assertTrue(list.isEmpty());
    }

    @Test
    public void testGetListMissing() {
        List<String> list = extendedProperties.getList("missing");
        assertNull(list);
    }

    @Test
    public void testGetStringArray() {
        extendedProperties.put("arr", "1,2,3");
        String[] arr = extendedProperties.getStringArray("arr");
        assertArrayEquals(new String[]{"1","2","3"}, arr);
    }

    // ==================== getBoolean / getInt / getLong tests ====================   @Test
    public void testGetBooleanTrue() {
        extendedProperties.put("flag", "true");
        assertTrue(extendedProperties.getBoolean("flag"));
    }

    @Test
    public void testGetBooleanFalse() {
        extendedProperties.put("flag", "false");
        assertFalse(extendedProperties.getBoolean("flag"));
    }

    @Test
    public void testGetBooleanDefault() {
        assertTrue(extendedProperties.getBoolean("missing", true));
        assertFalse(extendedProperties.getBoolean("missing", false));
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetBooleanMissingNoDefault() {
        extendedProperties.getBoolean("missing");
    }

    @Test
    public void testGetInt() {
        extendedProperties.put("num", "42");
        assertEquals(42, extendedProperties.getInt("num"));
    }

    @Test(expected = NumberFormatException.class)
    public void testGetIntInvalid() {
        extendedProperties.put("num", "abc");
        extendedProperties.getInt("num");
    }

    @Test
    public void testGetIntDefault() {
        assertEquals(99, extendedProperties.getInt("missing", 99));
    }

    @Test
    public void testGetLong() {
        extendedProperties.put("long", "123456789");
        assertEquals(123456789L, extendedProperties.getLong("long"));
    }

    @Test
    public void testGetLongDefault() {
        assertEquals(0L, extendedProperties.getLong("missing", 0L));
    }

    // =================== Edge cases and miscellaneous ===========

    @Test
    public void testClear() {
        extendedProperties.put("a", "1");
        extendedProperties.put("b", "2");
        extendedProperties.clear();
        assertTrue(extendedProperties.isEmpty());
    }

    @Test
    public void testContainsKey() {
        extendedProperties.put("key", "val");
        assertTrue(extendedProperties.containsKey("key"));
        assertFalse(extendedProperties.containsKey("missing"));
    }

    @Test
    public void testGetPropertyWithDefault() {
        assertEquals("default", extendedProperties.getProperty("none", "default"));
        extendedProperties.put("some", "value");
        assertEquals("value", extendedProperties.getProperty("some", "default"));
    }
}