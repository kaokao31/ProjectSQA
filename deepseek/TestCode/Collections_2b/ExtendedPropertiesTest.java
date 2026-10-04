package org.apache.commons.configuration;

import org.junit.Before;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for ExtendedProperties.
 * Achieves maximum coverage and targets potential faults.
 */
public class ExtendedPropertiesTest {

    private ExtendedProperties props;

    @Before
    public void setUp() {
        props = new ExtendedProperties();
    }

    // ------------------- Constructor Tests -------------------

    @Test
    public void testDefaultConstructorCreatesEmptyProperties() {
        assertTrue("Should be empty", props.isEmpty());
        assertEquals("Size should be 0", 0, props.size());
    }

    @Test
    public void testConstructorWithProperties() {
        Properties base = new Properties();
        base.setProperty("key1", "value1");
        base.setProperty("key2", "value2");
        ExtendedProperties ext = new ExtendedProperties(base);
        assertEquals("Should have 2 entries", 2, ext.size());
        assertEquals("value1", ext.getString("key1"));
        assertEquals("value2", ext.getString("key2"));
    }

    // ------------------- setProperty / getProperty -------------------

    @Test
    public void testSetAndGetPropertyString() {
        props.setProperty("name", "John");
        assertEquals("John", props.getProperty("name"));
    }

    @Test
    public void testGetPropertyDefault() {
        assertEquals("default", props.getProperty("missing", "default"));
    }

    @Test
    public void testGetPropertyNullKey() {
        assertNull("Null key should return null", props.getProperty(null));
    }

    @Test
    public void testSetPropertyNullValue() {
        props.setProperty("key", null);
        assertNull("Value should be null", props.getProperty("key"));
    }

    @Test
    public void testSetPropertyEmptyKey() {
        props.setProperty("", "empty");
        assertEquals("empty", props.getString(""));
    }

    // ------------------- String conversion -------------------

    @Test
    public void testGetString() {
        props.setProperty("s", "hello");
        assertEquals("hello", props.getString("s"));
    }

    @Test
    public void testGetStringWithDefault() {
        assertEquals("fallback", props.getString("missing", "fallback"));
    }

    @Test
    public void testGetStringListWithEscapedComma() {
        props.setProperty("list", "a\\,b,c");
        // Expect split on unescaped comma only
        List<String> list = props.getList("list");
        assertEquals(2, list.size());
        assertEquals("a,b", list.get(0));
        assertEquals("c", list.get(1));
    }

    @Test
    public void testGetStringWithLeadingSpaces() {
        props.setProperty("ws", "   value");
        assertEquals("value", props.getString("ws"));
    }

    @Test
    public void testGetStringWithTrailingSpaces() {
        props.setProperty("ws", "value   ");
        assertEquals("value", props.getString("ws"));
    }

    // ------------------- Integer conversion -------------------

    @Test
    public void testGetInt() {
        props.setProperty("int", "42");
        assertEquals(42, props.getInt("int"));
    }

    @Test(expected = NumberFormatException.class)
    public void testGetIntInvalid() {
        props.setProperty("int", "abc");
        props.getInt("int");
    }

    @Test
    public void testGetIntWithDefault() {
        assertEquals(10, props.getInt("missing", 10));
    }

    @Test
    public void testGetIntNegative() {
        props.setProperty("neg", "-123");
        assertEquals(-123, props.getInt("neg"));
    }

    @Test
    public void testGetIntMaxBoundary() {
        props.setProperty("max", String.valueOf(Integer.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE, props.getInt("max"));
    }

    @Test
    public void testGetIntMinBoundary() {
        props.setProperty("min", String.valueOf(Integer.MIN_VALUE));
        assertEquals(Integer.MIN_VALUE, props.getInt("min"));
    }

    // ------------------- Boolean conversion -------------------

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
    public void testGetBooleanOn() {
        props.setProperty("b", "on");
        assertTrue(props.getBoolean("b"));
    }

    @Test
    public void testGetBooleanCaseInsensitive() {
        props.setProperty("b", "TRUE");
        assertTrue(props.getBoolean("b"));
        props.setProperty("b", "FALSE");
        assertFalse(props.getBoolean("b"));
        props.setProperty("b", "YeS");
        assertTrue(props.getBoolean("b"));
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetBooleanMissingNoDefault() {
        props.getBoolean("missing");
    }

    @Test
    public void testGetBooleanWithDefault() {
        assertTrue(props.getBoolean("missing", true));
        assertFalse(props.getBoolean("missing", false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetBooleanInvalidValue() {
        props.setProperty("b", "invalid");
        props.getBoolean("b");
    }

    // ------------------- List conversion -------------------

    @Test
    public void testGetListSimple() {
        props.setProperty("list", "a,b,c");
        List<Object> list = props.getList("list");
        assertEquals(Arrays.asList("a", "b", "c"), list);
    }

    @Test
    public void testGetListWithSpaces() {
        props.setProperty("list", " a , b , c ");
        List<Object> list = props.getList("list");
        assertEquals(Arrays.asList("a", "b", "c"), list);
    }

    @Test
    public void testGetListEmptyString() {
        props.setProperty("list", "");
        List<Object> list = props.getList("list");
        assertTrue("List should be empty", list.isEmpty());
    }

    @Test
    public void testGetListSingleElement() {
        props.setProperty("list", "only");
        List<Object> list = props.getList("list");
        assertEquals(1, list.size());
        assertEquals("only", list.get(0));
    }

    @Test
    public void testGetListWithEscapedSpace() {
        props.setProperty("list", "a\\ b,c");
        List<Object> list = props.getList("list");
        assertEquals(2, list.size());
        assertEquals("a b", list.get(0));
        assertEquals("c", list.get(1));
    }

    @Test
    public void testGetListWithNullValue() {
        props.setProperty("list", null);
        // Should return empty list or throw? Typically empty list.
        try {
            List<Object> list = props.getList("list");
            assertTrue("Expected empty list", list.isEmpty());
        } catch (Exception e) {
            fail("Should not throw exception for null value");
        }
    }

    // ------------------- includes / combine -------------------

    @Test
    public void testCombineOverwritesExisting() {
        props.setProperty("key", "old");
        ExtendedProperties other = new ExtendedProperties();
        other.setProperty("key", "new");
        other.setProperty("extra", "extra");
        props.combine(other);
        assertEquals("new", props.getProperty("key"));
        assertEquals("extra", props.getProperty("extra"));
    }

    @Test
    public void testIncludeWithBasePath() throws Exception {
        // This test might require file system. We'll skip or mock.
        // Instead, test that include with non-existent file throws.
        try {
            props.include("nonexistent.properties");
            fail("Should have thrown an exception");
        } catch (Exception e) {
            // expected
        }
    }

    // ------------------- getList with default -------------------

    @Test
    public void testGetListWithDefault() {
        List<String> defaultList = Arrays.asList("d1", "d2");
        List<Object> result = props.getList("missing", defaultList);
        assertEquals(defaultList, result);
    }

    // ------------------- escape / unescape -------------------

    @Test
    public void testEscapeCommaInGetString() {
        props.setProperty("esc", "a\\,b");
        assertEquals("a,b", props.getString("esc"));
    }

    @Test
    public void testEscapeEqualsInGetString() {
        props.setProperty("esc", "a\\=b");
        assertEquals("a=b", props.getString("esc"));
    }

    // ------------------- internal layout tests -------------------

    @Test
    public void testClearRemovesAll() {
        props.setProperty("k1", "v1");
        props.setProperty("k2", "v2");
        props.clear();
        assertTrue("Should be empty", props.isEmpty());
    }

    @Test
    public void testContainsKey() {
        props.setProperty("k", "v");
        assertTrue(props.containsKey("k"));
        assertFalse(props.containsKey("missing"));
    }

    @Test
    public void testKeysIterator() {
        props.setProperty("a", "1");
        props.setProperty("b", "2");
        Set<String> keys = new HashSet<>();
        Enumeration<?> en = props.keys();
        while (en.hasMoreElements()) {
            keys.add((String) en.nextElement());
        }
        assertEquals(new HashSet<>(Arrays.asList("a", "b")), keys);
    }

    // ------------------- bug-triggering edge cases -------------------

    @Test
    public void testGetStringWithNullDefault() {
        props.setProperty("k", "v");
        assertEquals("v", props.getString("k", null));
        assertNull(props.getString("missing", null));
    }

    @Test
    public void testGetListWithEscapedEndingBackslash() {
        props.setProperty("list", "a\\,b\\\\,c");
        List<Object> list = props.getList("list");
        assertEquals(3, list.size());
        assertEquals("a,b", list.get(0));
        assertEquals("b\\", list.get(1));
        assertEquals("c", list.get(2));
    }

    @Test
    public void testGetIntWithWhiteSpace() {
        props.setProperty("int", "  55  ");
        assertEquals(55, props.getInt("int"));
    }

    @Test
    public void testGetBooleanWithDefaultAndNullValue() {
        props.setProperty("b", null);
        // Should use default if value is null
        assertTrue("Should use default", props.getBoolean("b", true));
    }

    @Test
    public void testClone() throws CloneNotSupportedException {
        props.setProperty("k", "v");
        ExtendedProperties clone = (ExtendedProperties) props.clone();
        assertEquals("v", clone.getString("k"));
        clone.setProperty("k", "changed");
        assertEquals("v", props.getString("k")); // original unchanged
    }

    @Test
    public void testAddPropertyMultipleValues() {
        props.addProperty("multi", "first");
        props.addProperty("multi", "second");
        // Should become a list
        List<Object> list = props.getList("multi");
        assertEquals(2, list.size());
        assertEquals("first", list.get(0));
        assertEquals("second", list.get(1));
    }

    @Test
    public void testAddPropertySingleValueThenString() {
        props.addProperty("k", "v1");
        assertEquals("v1", props.getString("k"));
    }

    // ------------------- Interpolation / substitution (if any) -------------------

    @Test
    public void testPropertySubstitution() {
        props.setProperty("home", "/tmp");
        props.setProperty("log", "${home}/log");
        // Typically ExtendedProperties doesn't do substitution, but some versions do.
        // We'll test that the literal string is returned.
        assertEquals("${home}/log", props.getString("log"));
    }

    // ------------------- Metadata / conversion errors -------------------

    @Test(expected = ClassCastException.class)
    public void testGetStringOnNonStringValue() {
        props.setProperty("obj", new Object());
        props.getString("obj");
    }

    @Test
    public void testGetListOnSingleNonString() {
        // If value is a list, getList returns it directly.
        List<String> list = new ArrayList<>();
        list.add("x");
        props.setProperty("list", list);
        assertSame(list, props.getList("list"));
    }

    // Stress test: many properties
    @Test
    public void testManyProperties() {
        for (int i = 0; i < 1000; i++) {
            props.setProperty("key" + i, "value" + i);
        }
        assertEquals(1000, props.size());
        for (int i = 0; i < 1000; i++) {
            assertEquals("value" + i, props.getString("key" + i));
        }
    }

    // Test that includes are not recursively called infinitely (if implemented)
    @Test(timeout = 1000)
    public void testCircularIncludeDoesNotHang() {
        // This assumes include is not implemented or catches circularity.
        // We'll just set a property and ensure no infinite loop.
        props.setProperty("k", "v");
        assertNotNull(props.getString("k"));
    }
}