package org.example;

import org.junit.Before;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for ExtendedProperties class.
 * Assumes ExtendedProperties extends java.util.Properties and provides additional methods:
 * - getString(String key, String defaultValue)
 * - getInt(String key, int defaultValue)
 * - getBoolean(String key, boolean defaultValue)
 * - getList(String key, List<String> defaultValue)
 * - getStringArray(String key)
 * - setProperty(String key, Object value) (overrides Properties.setProperty)
 * - includes(String key)
 * - getProperty(String key, String defaultValue)
 * - clone()
 * - clear()
 * - size()
 * - isEmpty()
 * - keys()
 * - values()
 * - containsKey(Object key)
 * - containsValue(Object value)
 * - put(Object key, Object value)
 * - remove(Object key)
 * - get(Object key)
 * - propertyNames()
 * - load(InputStream) / load(Reader) (inherited)
 * - store(OutputStream, String) / store(Writer, String) (inherited)
 * 
 * Tests aim for high line/branch coverage and fault detection.
 */
public class ExtendedPropertiesTest {

    private ExtendedProperties props;

    @Before
    public void setUp() {
        props = new ExtendedProperties();
    }

    // ---------- Basic property operations ----------

    @Test
    public void testSetAndGetProperty() {
        props.setProperty("key1", "value1");
        assertEquals("value1", props.getProperty("key1"));
    }

    @Test
    public void testGetPropertyWithDefault() {
        assertEquals("default", props.getProperty("nonexistent", "default"));
    }

    @Test
    public void testGetPropertyNullDefault() {
        assertNull(props.getProperty("nonexistent", null));
    }

    @Test
    public void testGetPropertyExistingWithDefaultIgnored() {
        props.setProperty("key", "actual");
        assertEquals("actual", props.getProperty("key", "default"));
    }

    @Test
    public void testSetPropertyOverwrites() {
        props.setProperty("key", "first");
        props.setProperty("key", "second");
        assertEquals("second", props.getProperty("key"));
    }

    @Test
    public void testSetPropertyNullKey() {
        try {
            props.setProperty(null, "value");
            fail("Should throw NullPointerException or IllegalArgumentException");
        } catch (NullPointerException | IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testSetPropertyNullValue() {
        props.setProperty("key", null);
        assertNull(props.getProperty("key"));
    }

    @Test
    public void testContainsKey() {
        props.setProperty("a", "1");
        assertTrue(props.containsKey("a"));
        assertFalse(props.containsKey("b"));
    }

    @Test
    public void testContainsKeyNull() {
        assertFalse(props.containsKey(null));
    }

    @Test
    public void testContainsValue() {
        props.setProperty("a", "val");
        assertTrue(props.containsValue("val"));
        assertFalse(props.containsValue("other"));
    }

    @Test
    public void testContainsValueNull() {
        props.setProperty("a", null);
        assertTrue(props.containsValue(null));
    }

    @Test
    public void testRemove() {
        props.setProperty("x", "y");
        assertEquals("y", props.remove("x"));
        assertNull(props.getProperty("x"));
    }

    @Test
    public void testRemoveNonExistent() {
        assertNull(props.remove("nonexistent"));
    }

    @Test
    public void testRemoveNull() {
        assertNull(props.remove(null));
    }

    @Test
    public void testSize() {
        assertEquals(0, props.size());
        props.setProperty("a", "1");
        assertEquals(1, props.size());
        props.setProperty("b", "2");
        assertEquals(2, props.size());
        props.remove("a");
        assertEquals(1, props.size());
    }

    @Test
    public void testIsEmpty() {
        assertTrue(props.isEmpty());
        props.setProperty("a", "1");
        assertFalse(props.isEmpty());
    }

    @Test
    public void testClear() {
        props.setProperty("a", "1");
        props.setProperty("b", "2");
        props.clear();
        assertTrue(props.isEmpty());
        assertEquals(0, props.size());
    }

    @Test
    public void testKeys() {
        props.setProperty("k1", "v1");
        props.setProperty("k2", "v2");
        Enumeration<?> e = props.keys();
        Set<String> keys = new HashSet<>();
        while (e.hasMoreElements()) {
            keys.add((String) e.nextElement());
        }
        assertEquals(2, keys.size());
        assertTrue(keys.contains("k1"));
        assertTrue(keys.contains("k2"));
    }

    @Test
    public void testPropertyNames() {
        props.setProperty("a", "1");
        props.setProperty("b", "2");
        Enumeration<?> e = props.propertyNames();
        Set<String> names = new HashSet<>();
        while (e.hasMoreElements()) {
            names.add((String) e.nextElement());
        }
        assertEquals(2, names.size());
    }

    // ---------- ExtendedProperties specific methods ----------

    @Test
    public void testGetString() {
        props.setProperty("s", "hello");
        assertEquals("hello", props.getString("s"));
    }

    @Test
    public void testGetStringWithDefault() {
        assertEquals("default", props.getString("missing", "default"));
    }

    @Test
    public void testGetStringNullDefault() {
        assertNull(props.getString("missing", null));
    }

    @Test
    public void testGetStringExistingWithDefault() {
        props.setProperty("s", "actual");
        assertEquals("actual", props.getString("s", "default"));
    }

    @Test
    public void testGetStringNullKey() {
        try {
            props.getString(null);
            fail("Should throw exception");
        } catch (NullPointerException | IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testGetInt() {
        props.setProperty("i", "42");
        assertEquals(42, props.getInt("i"));
    }

    @Test
    public void testGetIntWithDefault() {
        assertEquals(10, props.getInt("missing", 10));
    }

    @Test
    public void testGetIntInvalidFormat() {
        props.setProperty("i", "notanumber");
        try {
            props.getInt("i");
            fail("Should throw NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test
    public void testGetIntWithDefaultInvalidFormat() {
        props.setProperty("i", "bad");
        assertEquals(5, props.getInt("i", 5));
    }

    @Test
    public void testGetIntNullValue() {
        props.setProperty("i", null);
        assertEquals(0, props.getInt("i", 0));
    }

    @Test
    public void testGetBooleanTrue() {
        props.setProperty("b", "true");
        assertTrue(props.getBoolean("b"));
    }

    @Test
    public void testGetBooleanFalse() {
        props.setProperty("b", "false");
        assertFalse(props.getBoolean("b"));
    }

    @Test
    public void testGetBooleanYes() {
        props.setProperty("b", "yes");
        assertTrue(props.getBoolean("b"));
    }

    @Test
    public void testGetBooleanNo() {
        props.setProperty("b", "no");
        assertFalse(props.getBoolean("b"));
    }

    @Test
    public void testGetBooleanOn() {
        props.setProperty("b", "on");
        assertTrue(props.getBoolean("b"));
    }

    @Test
    public void testGetBooleanOff() {
        props.setProperty("b", "off");
        assertFalse(props.getBoolean("b"));
    }

    @Test
    public void testGetBooleanCaseInsensitive() {
        props.setProperty("b", "True");
        assertTrue(props.getBoolean("b"));
        props.setProperty("b", "FALSE");
        assertFalse(props.getBoolean("b"));
    }

    @Test
    public void testGetBooleanInvalid() {
        props.setProperty("b", "maybe");
        try {
            props.getBoolean("b");
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testGetBooleanWithDefault() {
        assertTrue(props.getBoolean("missing", true));
        assertFalse(props.getBoolean("missing", false));
    }

    @Test
    public void testGetBooleanWithDefaultInvalid() {
        props.setProperty("b", "invalid");
        assertTrue(props.getBoolean("b", true));
    }

    @Test
    public void testGetList() {
        props.setProperty("list", "a,b,c");
        List<String> expected = Arrays.asList("a", "b", "c");
        assertEquals(expected, props.getList("list"));
    }

    @Test
    public void testGetListWithDefault() {
        List<String> def = Arrays.asList("x", "y");
        assertEquals(def, props.getList("missing", def));
    }

    @Test
    public void testGetListEmptyString() {
        props.setProperty("list", "");
        List<String> result = props.getList("list");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetListSingleElement() {
        props.setProperty("list", "only");
        List<String> expected = Collections.singletonList("only");
        assertEquals(expected, props.getList("list"));
    }

    @Test
    public void testGetListNullValue() {
        props.setProperty("list", null);
        assertNull(props.getList("list"));
    }

    @Test
    public void testGetStringArray() {
        props.setProperty("arr", "x,y,z");
        String[] expected = {"x", "y", "z"};
        assertArrayEquals(expected, props.getStringArray("arr"));
    }

    @Test
    public void testGetStringArrayEmpty() {
        props.setProperty("arr", "");
        assertArrayEquals(new String[0], props.getStringArray("arr"));
    }

    @Test
    public void testGetStringArrayNull() {
        props.setProperty("arr", null);
        assertNull(props.getStringArray("arr"));
    }

    @Test
    public void testIncludes() {
        props.setProperty("inc", "value");
        assertTrue(props.includes("inc"));
        assertFalse(props.includes("missing"));
    }

    @Test
    public void testIncludesNull() {
        assertFalse(props.includes(null));
    }

    // ---------- Clone ----------

    @Test
    public void testClone() {
        props.setProperty("a", "1");
        props.setProperty("b", "2");
        ExtendedProperties clone = (ExtendedProperties) props.clone();
        assertNotNull(clone);
        assertEquals(props.size(), clone.size());
        assertEquals("1", clone.getProperty("a"));
        assertEquals("2", clone.getProperty("b"));
        // Modify original, clone should be independent
        props.setProperty("a", "changed");
        assertEquals("1", clone.getProperty("a"));
    }

    @Test
    public void testCloneEmpty() {
        ExtendedProperties clone = (ExtendedProperties) props.clone();
        assertTrue(clone.isEmpty());
    }

    // ---------- Inheritance from Properties ----------

    @Test
    public void testPutAndGet() {
        props.put("key", "value");
        assertEquals("value", props.get("key"));
    }

    @Test
    public void testPutNullKey() {
        try {
            props.put(null, "value");
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testPutNullValue() {
        props.put("key", null);
        assertNull(props.get("key"));
    }

    @Test
    public void testPutOverwrites() {
        props.put("k", "v1");
        props.put("k", "v2");
        assertEquals("v2", props.get("k"));
    }

    @Test
    public void testPutAll() {
        Map<String, String> map = new HashMap<>();
        map.put("a", "1");
        map.put("b", "2");
        props.putAll(map);
        assertEquals(2, props.size());
        assertEquals("1", props.getProperty("a"));
    }

    @Test
    public void testPutAllWithNullMap() {
        try {
            props.putAll(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    // ---------- Edge cases and fault detection ----------

    @Test
    public void testGetPropertyWithEmptyKey() {
        props.setProperty("", "emptykey");
        assertEquals("emptykey", props.getProperty(""));
    }

    @Test
    public void testGetPropertyWithWhitespaceKey() {
        props.setProperty("  ", "whitespace");
        assertEquals("whitespace", props.getProperty("  "));
    }

    @Test
    public void testGetPropertyWithSpecialCharacters() {
        props.setProperty("key.with.dots", "value");
        assertEquals("value", props.getProperty("key.with.dots"));
    }

    @Test
    public void testSetPropertyWithEmptyValue() {
        props.setProperty("k", "");
        assertEquals("", props.getProperty("k"));
    }

    @Test
    public void testSetPropertyWithWhitespaceValue() {
        props.setProperty("k", "   ");
        assertEquals("   ", props.getProperty("k"));
    }

    @Test
    public void testMultipleProperties() {
        for (int i = 0; i < 100; i++) {
            props.setProperty("key" + i, "val" + i);
        }
        assertEquals(100, props.size());
        for (int i = 0; i < 100; i++) {
            assertEquals("val" + i, props.getProperty("key" + i));
        }
    }

    @Test
    public void testOverwriteWithDifferentType() {
        props.setProperty("k", "string");
        props.setProperty("k", 123); // integer value
        assertEquals("123", props.getProperty("k"));
    }

    @Test
    public void testGetIntWithLeadingZeros() {
        props.setProperty("i", "007");
        assertEquals(7, props.getInt("i"));
    }

    @Test
    public void testGetIntNegative() {
        props.setProperty("i", "-5");
        assertEquals(-5, props.getInt("i"));
    }

    @Test
    public void testGetIntMaxValue() {
        props.setProperty("i", String.valueOf(Integer.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE, props.getInt("i"));
    }

    @Test
    public void testGetIntMinValue() {
        props.setProperty("i", String.valueOf(Integer.MIN_VALUE));
        assertEquals(Integer.MIN_VALUE, props.getInt("i"));
    }

    @Test
    public void testGetIntOverflow() {
        props.setProperty("i", "999999999999");
        try {
            props.getInt("i");
            fail("Should throw NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test
    public void testGetBooleanWithDefaultNullValue() {
        props.setProperty("b", null);
        assertFalse(props.getBoolean("b", false));
        assertTrue(props.getBoolean("b", true));
    }

    @Test
    public void testGetListWithCustomDelimiter() {
        // Assuming getList uses comma by default; test with comma in value?
        props.setProperty("list", "a,b,c");
        List<String> expected = Arrays.asList("a", "b", "c");
        assertEquals(expected, props.getList("list"));
    }

    @Test
    public void testGetListWithTrailingComma() {
        props.setProperty("list", "a,b,");
        List<String> expected = Arrays.asList("a", "b", "");
        assertEquals(expected, props.getList("list"));
    }

    @Test
    public void testGetListWithLeadingComma() {
        props.setProperty("list", ",a,b");
        List<String> expected = Arrays.asList("", "a", "b");
        assertEquals(expected, props.getList("list"));
    }

    @Test
    public void testGetListWithConsecutiveCommas() {
        props.setProperty("list", "a,,b");
        List<String> expected = Arrays.asList("a", "", "b");
        assertEquals(expected, props.getList("list"));
    }

    @Test
    public void testGetStringArrayWithTrailingComma() {
        props.setProperty("arr", "x,y,");
        String[] expected = {"x", "y", ""};
        assertArrayEquals(expected, props.getStringArray("arr"));
    }

    @Test
    public void testGetStringArrayWithLeadingComma() {
        props.setProperty("arr", ",x,y");
        String[] expected = {"", "x", "y"};
        assertArrayEquals(expected, props.getStringArray("arr"));
    }

    // ---------- Null/empty handling for extended methods ----------

    @Test
    public void testGetStringNullValue() {
        props.setProperty("s", null);
        assertNull(props.getString("s"));
    }

    @Test
    public void testGetStringEmptyValue() {
        props.setProperty("s", "");
        assertEquals("", props.getString("s"));
    }

    @Test
    public void testGetBooleanNullValueWithDefault() {
        props.setProperty("b", null);
        assertTrue(props.getBoolean("b", true));
    }

    @Test
    public void testGetBooleanEmptyValue() {
        props.setProperty("b", "");
        try {
            props.getBoolean("b");
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testGetListNullDefault() {
        assertNull(props.getList("missing", null));
    }

    @Test
    public void testGetStringArrayNullDefault() {
        assertNull(props.getStringArray("missing"));
    }

    // ---------- Thread safety (basic) ----------

    @Test
    public void testConcurrentAccess() throws InterruptedException {
        final ExtendedProperties shared = new ExtendedProperties();
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                shared.setProperty("k" + i, "v" + i);
            }
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                shared.getProperty("k" + i);
            }
        });
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        // No exception expected; size may vary but at least some properties set
        assertTrue(shared.size() > 0);
    }

    // ---------- Inheritance from Properties: store/load (not fully tested) ----------

    @Test
    public void testStoreAndLoad() throws Exception {
        props.setProperty("a", "1");
        props.setProperty("b", "2");
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        props.store(out, "test");
        java.io.ByteArrayInputStream in = new java.io.ByteArrayInputStream(out.toByteArray());
        ExtendedProperties loaded = new ExtendedProperties();
        loaded.load(in);
        assertEquals("1", loaded.getProperty("a"));
        assertEquals("2", loaded.getProperty("b"));
    }

    // ---------- Fault detection: potential bugs ----------

    @Test
    public void testGetPropertyAfterClear() {
        props.setProperty("k", "v");
        props.clear();
        assertNull(props.getProperty("k"));
    }

    @Test
    public void testGetPropertyAfterRemove() {
        props.setProperty("k", "v");
        props.remove("k");
        assertNull(props.getProperty("k"));
    }

    @Test
    public void testGetPropertyWithDefaultAfterRemove() {
        props.setProperty("k", "v");
        props.remove("k");
        assertEquals("default", props.getProperty("k", "default"));
    }

    @Test
    public void testCloneAfterModification() {
        props.setProperty("a", "1");
        ExtendedProperties clone = (ExtendedProperties) props.clone();
        props.setProperty("b", "2");
        assertFalse(clone.containsKey("b"));
    }

    @Test
    public void testGetBooleanWithYesNoCase() {
        props.setProperty("b", "YES");
        assertTrue(props.getBoolean("b"));
        props.setProperty("b", "NO");
        assertFalse(props.getBoolean("b"));
    }

    @Test
    public void testGetBooleanWithOnOffCase() {
        props.setProperty("b", "ON");
        assertTrue(props.getBoolean("b"));
        props.setProperty("b", "OFF");
        assertFalse(props.getBoolean("b"));
    }

    @Test
    public void testGetBooleanWithTrueFalseCase() {
        props.setProperty("b", "TRUE");
        assertTrue(props.getBoolean("b"));
        props.setProperty("b", "FALSE");
        assertFalse(props.getBoolean("b"));
    }

    @Test
    public void testGetIntWithNullKey() {
        try {
            props.getInt(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testGetBooleanWithNullKey() {
        try {
            props.getBoolean(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testGetListWithNullKey() {
        try {
            props.getList(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testGetStringArrayWithNullKey() {
        try {
            props.getStringArray(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testIncludesWithNullKey() {
        assertFalse(props.includes(null));
    }

    @Test
    public void testSetPropertyWithEmptyKey() {
        props.setProperty("", "value");
        assertTrue(props.containsKey(""));
    }

    @Test
    public void testSetPropertyWithNullKeyThrows() {
        try {
            props.setProperty(null, "value");
            fail("Should throw exception");
        } catch (NullPointerException | IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testPutAllWithEmptyMap() {
        props.putAll(new HashMap<>());
        assertTrue(props.isEmpty());
    }

    @Test
    public void testEqualsAndHashCode() {
        ExtendedProperties p1 = new ExtendedProperties();
        ExtendedProperties p2 = new ExtendedProperties();
        p1.setProperty("a", "1");
        p2.setProperty("a", "1");
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        p2.setProperty("b", "2");
        assertNotEquals(p1, p2);
    }

    @Test
    public void testToString() {
        props.setProperty("k", "v");
        String str = props.toString();
        assertTrue(str.contains("k"));
        assertTrue(str.contains("v"));
    }
}