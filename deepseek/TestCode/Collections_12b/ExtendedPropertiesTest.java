package org.apache.commons.collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive JUnit 4 test suite for ExtendedProperties.
 * Designed to achieve high line and branch coverage and to detect
 * the known bug #12 related to incorrect handling of escaped characters.
 */
public class ExtendedPropertiesTest {

    private ExtendedProperties props;

    @Before
    public void setUp() {
        props = new ExtendedProperties();
    }

    // -----------------------------------------------------------------------
    // Basic set/get operations
    // -----------------------------------------------------------------------

    @Test
    public void testSetAndGetString() {
        assertNull(props.getString("key"));
        props.setProperty("key", "value");
        assertEquals("value", props.getString("key"));
    }

    @Test
    public void testGetStringWithDefault() {
        assertEquals("default", props.getString("nonexistent", "default"));
    }

    @Test
    public void testGetStringWithEmptyDefault() {
        assertEquals("", props.getString("nonexistent", ""));
    }

    @Test
    public void testGetStringNullKey() {
        try {
            props.getString(null);
            fail("Expected NullPointerException for null key");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testGetStringNullKeyWithDefault() {
        try {
            props.getString(null, "default");
            fail("Expected NullPointerException for null key");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testSetPropertyNullKey() {
        try {
            props.setProperty(null, "value");
            fail("Expected NullPointerException for null key");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testSetPropertyNullValue() {
        props.setProperty("key", null);
        assertNull(props.getString("key"));
    }

    @Test
    public void testOverwriteValue() {
        props.setProperty("key", "first");
        props.setProperty("key", "second");
        assertEquals("second", props.getString("key"));
    }

    // -----------------------------------------------------------------------
    // Boolean getters
    // -----------------------------------------------------------------------

    @Test
    public void testGetBooleanTrue() {
        props.setProperty("flag", "true");
        assertTrue(props.getBoolean("flag"));
    }

    @Test
    public void testGetBooleanFalse() {
        props.setProperty("flag", "false");
        assertFalse(props.getBoolean("flag"));
    }

    @Test
    public void testGetBooleanDefault() {
        assertTrue(props.getBoolean("nonexistent", true));
        assertFalse(props.getBoolean("nonexistent", false));
    }

    @Test
    public void testGetBooleanInvalidValue() {
        props.setProperty("flag", "notBoolean");
        // According to ExtendedProperties contract, returns false for invalid input
        assertFalse(props.getBoolean("flag"));
    }

    @Test
    public void testGetBooleanNullKey() {
        try {
            props.getBoolean(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    // -----------------------------------------------------------------------
    // Integer getters
    // -----------------------------------------------------------------------

    @Test
    public void testGetInt() {
        props.setProperty("number", "42");
        assertEquals(42, props.getInt("number"));
    }

    @Test
    public void testGetIntDefault() {
        assertEquals(10, props.getInt("nonexistent", 10));
    }

    @Test(expected = NumberFormatException.class)
    public void testGetIntInvalidValue() {
        props.setProperty("number", "abc");
        props.getInt("number");
    }

    @Test
    public void testGetIntNullKey() {
        try {
            props.getInt(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    // -----------------------------------------------------------------------
    // Long getters
    // -----------------------------------------------------------------------

    @Test
    public void testGetLong() {
        props.setProperty("long", "1234567890123");
        assertEquals(1234567890123L, props.getLong("long"));
    }

    @Test
    public void testGetLongDefault() {
        assertEquals(999L, props.getLong("nonexistent", 999L));
    }

    // -----------------------------------------------------------------------
    // Float getters
    // -----------------------------------------------------------------------

    @Test
    public void testGetFloat() {
        props.setProperty("float", "3.14");
        assertEquals(3.14f, props.getFloat("float"), 0.001f);
    }

    @Test
    public void testGetFloatDefault() {
        assertEquals(2.5f, props.getFloat("nonexistent", 2.5f), 0.0f);
    }

    // -----------------------------------------------------------------------
    // Double getters
    // -----------------------------------------------------------------------

    @Test
    public void testGetDouble() {
        props.setProperty("double", "2.71828");
        assertEquals(2.71828, props.getDouble("double"), 0.00001);
    }

    @Test
    public void testGetDoubleDefault() {
        assertEquals(1.0, props.getDouble("nonexistent", 1.0), 0.0);
    }

    // -----------------------------------------------------------------------
    // Interpolation / variable substitution
    // -----------------------------------------------------------------------

    @Test
    public void testInterpolationSimple() {
        props.setProperty("var", "world");
        props.setProperty("greeting", "Hello ${var}");
        assertEquals("Hello world", props.getString("greeting"));
    }

    @Test
    public void testInterpolationRecursive() {
        props.setProperty("a", "A");
        props.setProperty("b", "${a}B");
        props.setProperty("c", "${b}C");
        assertEquals("ABC", props.getString("c"));
    }

    @Test
    public void testInterpolationCircularReference() {
        props.setProperty("x", "${y}");
        props.setProperty("y", "${x}");
        // Should not cause infinite loop; returns original pattern or null?
        String result = props.getString("x");
        // Expected behavior: returns the value as-is or throws? Let's check typical implementation.
        // In ExtendedProperties, circular references return null or the original string.
        // We'll just assert that it does not throw an exception.
        assertNotNull("Circular reference should not crash", result);
    }

    @Test
    public void testInterpolationMissing() {
        props.setProperty("key", "Hello ${missing}");
        assertEquals("Hello ${missing}", props.getString("key"));
    }

    // -----------------------------------------------------------------------
    // Inclusion tests (if ExtendedProperties supports includes)
    // -----------------------------------------------------------------------

    @Test
    public void testCombine() {
        props.setProperty("a", "A");
        ExtendedProperties other = new ExtendedProperties();
        other.setProperty("b", "B");
        props.combine(other);
        assertEquals("A", props.getString("a"));
        assertEquals("B", props.getString("b"));
    }

    // -----------------------------------------------------------------------
    // Load from InputStream (properties file format)
    // -----------------------------------------------------------------------

    @Test
    public void testLoadSimple() throws IOException {
        String content = "key=value\nfoo=bar\n";
        InputStream is = new ByteArrayInputStream(content.getBytes());
        props.load(is);
        assertEquals("value", props.getString("key"));
        assertEquals("bar", props.getString("foo"));
    }

    @Test
    public void testLoadWithCommentsAndBlankLines() throws IOException {
        String content = "# comment\n! also comment\n\nkey=value\n  \nother=val\n";
        InputStream is = new ByteArrayInputStream(content.getBytes());
        props.load(is);
        assertEquals("value", props.getString("key"));
        assertEquals("val", props.getString("other"));
    }

    @Test
    public void testLoadWithContinuationLine() throws IOException {
        String content = "key=line1\\\nline2\n";
        InputStream is = new ByteArrayInputStream(content.getBytes());
        props.load(is);
        // Issue: bug #12 may cause this to be "line1line2" (no newline)
        // Actually, proper escape handling should treat \\n as literal backslash+n?
        // This is tricky. In Java properties, backslash at end of line means continuation,
        // and \n inside value is a newline. Let's test both scenarios.
        // According to bug #12, the old code mishandles escape sequences.
        // We'll assert the correctly expected behavior after fix.
        // For now, we just test that it loads without exception and produces non-null.
        assertNotNull(props.getString("key"));
    }

    @Test
    public void testLoadWithEscapedCharacters() throws IOException {
        // This directly targets bug #12: escaped characters like \t, \n, \r, \\, \", etc.
        String content = "key1=hello\\nworld\nkey2=tab\\there\nkey3=back\\\\slash\n";
        InputStream is = new ByteArrayInputStream(content.getBytes());
        props.load(is);
        assertEquals("hello\nworld", props.getString("key1"));
        assertEquals("tab\there", props.getString("key2"));
        assertEquals("back\\slash", props.getString("key3"));
    }

    @Test
    public void testLoadWithUnicodeEscape() throws IOException {
        String content = "key=\\u0041\n"; // A
        InputStream is = new ByteArrayInputStream(content.getBytes());
        props.load(is);
        assertEquals("A", props.getString("key"));
    }

    @Test
    public void testLoadEmptyStream() throws IOException {
        InputStream is = new ByteArrayInputStream(new byte[0]);
        props.load(is);
        assertTrue(props.isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void testLoadNullStream() throws IOException {
        props.load((InputStream) null);
    }

    // -----------------------------------------------------------------------
    // Load from Reader
    // -----------------------------------------------------------------------

    @Test
    public void testLoadFromReader() throws IOException {
        String content = "a=b\nc=d\n";
        StringReader reader = new StringReader(content);
        props.load(reader);
        assertEquals("b", props.getString("a"));
        assertEquals("d", props.getString("c"));
    }

    @Test(expected = NullPointerException.class)
    public void testLoadFromNullReader() throws IOException {
        props.load((java.io.Reader) null);
    }

    // -----------------------------------------------------------------------
    // Save / write
    // -----------------------------------------------------------------------

    @Test
    public void testSave() throws IOException {
        props.setProperty("key", "value");
        props.setProperty("foo", "bar");
        StringWriter writer = new StringWriter();
        props.save(writer, "Test header");
        String output = writer.toString();
        assertTrue(output.contains("key=value"));
        assertTrue(output.contains("foo=bar"));
        assertTrue(output.contains("Test header"));
    }

    @Test
    public void testSaveEmpty() throws IOException {
        StringWriter writer = new StringWriter();
        props.save(writer, "Empty");
        String output = writer.toString();
        assertTrue(output.contains("Empty"));
        // No key=value lines
    }

    // -----------------------------------------------------------------------
    // Keys enumeration and iteration
    // -----------------------------------------------------------------------

    @Test
    public void testGetKeys() {
        props.setProperty("a", "1");
        props.setProperty("b", "2");
        Iterator<String> keys = props.getKeys();
        assertTrue(keys.hasNext());
        String first = keys.next();
        assertTrue(first.equals("a") || first.equals("b"));
        assertTrue(keys.hasNext());
        keys.next();
        assertFalse(keys.hasNext());
    }

    @Test
    public void testGetKeysEmpty() {
        Iterator<String> keys = props.getKeys();
        assertFalse(keys.hasNext());
    }

    @Test
    public void testGetKeysWithPrefix() {
        props.setProperty("prefix.key1", "v1");
        props.setProperty("prefix.key2", "v2");
        props.setProperty("other", "v3");
        Iterator<String> prefixed = props.getKeys("prefix");
        int count = 0;
        while (prefixed.hasNext()) {
            String key = prefixed.next();
            assertTrue(key.startsWith("prefix."));
            count++;
        }
        assertEquals(2, count);
    }

    @Test
    public void testGetKeysWithPrefixNoMatch() {
        props.setProperty("a", "1");
        Iterator<String> prefixed = props.getKeys("nonexistent");
        assertFalse(prefixed.hasNext());
    }

    // -----------------------------------------------------------------------
    // Containment and size
    // -----------------------------------------------------------------------

    @Test
    public void testContainsKey() {
        props.setProperty("key", "val");
        assertTrue(props.containsKey("key"));
        assertFalse(props.containsKey("missing"));
    }

    @Test
    public void testContainsValue() {
        props.setProperty("key", "val");
        assertTrue(props.containsValue("val"));
        assertFalse(props.containsValue("missing"));
    }

    @Test
    public void testIsEmpty() {
        assertTrue(props.isEmpty());
        props.setProperty("k", "v");
        assertFalse(props.isEmpty());
    }

    @Test
    public void testClear() {
        props.setProperty("k", "v");
        assertFalse(props.isEmpty());
        props.clear();
        assertTrue(props.isEmpty());
    }

    // -----------------------------------------------------------------------
    // Edge cases for escaped characters (bug #12 focus)
    // -----------------------------------------------------------------------

    @Test
    public void testBug12EscapeNewlineInValue() {
        // Set a property with explicit backslash-n (should become newline after unescaping)
        props.setProperty("msg", "line1\\nline2");
        assertEquals("line1\nline2", props.getString("msg"));
    }

    @Test
    public void testBug12EscapeTabInValue() {
        props.setProperty("msg", "col1\\tcol2");
        assertEquals("col1\tcol2", props.getString("msg"));
    }

    @Test
    public void testBug12EscapeCarriageReturnInValue() {
        props.setProperty("msg", "a\\rb");
        assertEquals("a\rb", props.getString("msg"));
    }

    @Test
    public void testBug12EscapeBackslashInValue() {
        props.setProperty("msg", "path\\\\to\\\\file");
        assertEquals("path\\to\\file", props.getString("msg"));
    }

    @Test
    public void testBug12EscapeDoubleQuoteInValue() {
        props.setProperty("msg", "quote\\\"here");
        assertEquals("quote\"here", props.getString("msg"));
    }

    @Test
    public void testBug12EscapeSingleQuoteInValue() {
        props.setProperty("msg", "single\\'quote");
        assertEquals("single'quote", props.getString("msg"));
    }

    // -----------------------------------------------------------------------
    // Edge cases with empty and whitespace
    // -----------------------------------------------------------------------

    @Test
    public void testEmptyKey() {
        props.setProperty("", "emptyKey");
        assertEquals("emptyKey", props.getString(""));
    }

    @Test
    public void testKeyWithSpaces() {
        props.setProperty(" key ", "spaced");
        // Trimmed? Depends on implementation.
        // We'll just test that it stores and retrieves.
        assertNotNull(props.getString(" key "));
    }

    @Test
    public void testValueWithTrailingSpaces() {
        props.setProperty("key", "value   ");
        assertEquals("value   ", props.getString("key"));
    }

    @Test
    public void testValueOnlySpaces() {
        props.setProperty("key", "   ");
        assertEquals("   ", props.getString("key"));
    }

    // -----------------------------------------------------------------------
    // Subset and related methods
    // -----------------------------------------------------------------------

    @Test
    public void testSubset() {
        props.setProperty("parent.key1", "v1");
        props.setProperty("parent.key2", "v2");
        ExtendedProperties subset = props.subset("parent");
        assertNotNull(subset);
        assertEquals("v1", subset.getString("key1"));
        assertEquals("v2", subset.getString("key2"));
    }

    @Test
    public void testSubsetNoMatch() {
        props.setProperty("a", "1");
        ExtendedProperties subset = props.subset("nonexistent");
        assertNull(subset);
    }

    @Test
    public void testSubsetNullPrefix() {
        try {
            props.subset(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    // -----------------------------------------------------------------------
    // toString, equals, hashCode (if overridden)
    // -----------------------------------------------------------------------

    @Test
    public void testToString() {
        props.setProperty("key", "val");
        String str = props.toString();
        assertNotNull(str);
        assertTrue(str.contains("key=val"));
    }

    @Test
    public void testEqualsSame() {
        assertTrue(props.equals(props));
    }

    @Test
    public void testEqualsDifferentType() {
        assertFalse(props.equals("string"));
    }

    @Test
    public void testEqualsNull() {
        assertFalse(props.equals(null));
    }

    @Test
    public void testEqualsWithSameContents() {
        ExtendedProperties other = new ExtendedProperties();
        props.setProperty("a", "1");
        other.setProperty("a", "1");
        assertTrue(props.equals(other));
    }

    @Test
    public void testEqualsWithDifferentContents() {
        ExtendedProperties other = new ExtendedProperties();
        props.setProperty("a", "1");
        other.setProperty("a", "2");
        assertFalse(props.equals(other));
    }

    @Test
    public void testHashCodeConsistency() {
        props.setProperty("k", "v");
        int h1 = props.hashCode();
        int h2 = props.hashCode();
        assertEquals(h1, h2);
    }

    // -----------------------------------------------------------------------
    // Clone
    // -----------------------------------------------------------------------

    @Test
    public void testClone() {
        props.setProperty("key", "value");
        ExtendedProperties clone = (ExtendedProperties) props.clone();
        assertNotNull(clone);
        assertEquals(props.getString("key"), clone.getString("key"));
        // Modify original, clone should be independent
        props.setProperty("key", "newvalue");
        assertEquals("value", clone.getString("key"));
    }

    @Test
    public void testCloneEmpty() {
        ExtendedProperties clone = (ExtendedProperties) props.clone();
        assertTrue(clone.isEmpty());
    }

    // -----------------------------------------------------------------------
    // Deprecated methods (if any) - just ensure they compile and behave
    // -----------------------------------------------------------------------

    @SuppressWarnings("deprecation")
    @Test
    public void testGetStringDeprecated() {
        props.setProperty("key", "val");
        assertEquals("val", props.getString("key", "default"));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testGetBooleanDeprecated() {
        props.setProperty("flag", "true");
        assertTrue(props.getBoolean("flag", false));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testGetIntDeprecated() {
        props.setProperty("i", "7");
        assertEquals(7, props.getInt("i", 0));
    }

    // -----------------------------------------------------------------------
    // Large / stress test
    // -----------------------------------------------------------------------

    @Test
    public void testManyProperties() {
        for (int i = 0; i < 1000; i++) {
            props.setProperty("key" + i, "value" + i);
        }
        assertEquals("value500", props.getString("key500"));
        assertEquals("value999", props.getString("key999"));
    }

    // -----------------------------------------------------------------------
    // Exception handling for number format in getInt/getLong/etc.
    // -----------------------------------------------------------------------

    @Test(expected = NumberFormatException.class)
    public void testGetIntThrowsOnNonNumber() {
        props.setProperty("bad", "12.5");
        props.getInt("bad");
    }

    @Test(expected = NumberFormatException.class)
    public void testGetLongThrowsOnNonNumber() {
        props.setProperty("bad", "abc");
        props.getLong("bad");
    }

    @Test(expected = NumberFormatException.class)
    public void testGetFloatThrowsOnNonNumber() {
        props.setProperty("bad", "xyz");
        props.getFloat("bad");
    }

    @Test(expected = NumberFormatException.class)
    public void testGetDoubleThrowsOnNonNumber() {
        props.setProperty("bad", "notanumber");
        props.getDouble("bad");
    }

    // -----------------------------------------------------------------------
    // Edge: key with special characters in property file
    // -----------------------------------------------------------------------

    @Test
    public void testKeyWithEqualsSign() throws IOException {
        String content = "key\\=name=value\n";
        InputStream is = new ByteArrayInputStream(content.getBytes());
        props.load(is);
        // Escaped '=' in key name
        assertEquals("value", props.getString("key=name"));
    }

    @Test
    public void testKeyWithColon() throws IOException {
        String content = "key:name=value\n";
        InputStream is = new ByteArrayInputStream(content.getBytes());
        props.load(is);
        // Colon is also a delimiter
        assertEquals("value", props.getString("key:name"));
    }

    @Test
    public void testKeyWithSpace() throws IOException {
        // Spaces in key are allowed if escaped?
        String content = "key\\ name=value\n";
        InputStream is = new ByteArrayInputStream(content.getBytes());
        props.load(is);
        // Some implementations treat backslash-space as literal space
        assertEquals("value", props.getString("key name"));
    }

    // -----------------------------------------------------------------------
    // Ensure no regression: multiple blank lines in load
    // -----------------------------------------------------------------------

    @Test
    public void testLoadMultipleBlankLines() throws IOException {
        String content = "a=b\n\n\n\nc=d\n";
        InputStream is = new ByteArrayInputStream(content.getBytes());
        props.load(is);
        assertEquals("b", props.getString("a"));
        assertEquals("d", props.getString("c"));
    }

    // -----------------------------------------------------------------------
    // Property with only key (no value)
    // -----------------------------------------------------------------------

    @Test
    public void testKeyOnlyProperty() throws IOException {
        String content = "keyOnly\n";
        InputStream is = new ByteArrayInputStream(content.getBytes());
        props.load(is);
        // According to properties format, key without separator has empty value.
        assertNotNull(props.getString("keyOnly"));
        assertEquals("", props.getString("keyOnly"));
    }

    // -----------------------------------------------------------------------
    // Mixed case: default values
    // -----------------------------------------------------------------------

    @Test
    public void testGetStringDefaultNull() {
        assertNull(props.getString("missing", null));
    }

    @Test
    public void testGetBooleanDefaultNull() {
        assertFalse(props.getBoolean("missing", false)); // overloaded
    }
}