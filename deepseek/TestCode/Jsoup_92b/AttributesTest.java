package org.jsoup.nodes;

import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class AttributesTest {

    private Attributes attributes;

    @Before
    public void setUp() {
        attributes = new Attributes();
    }

    @Test
    public void testPutAndGet() {
        attributes.put("key1", "value1");
        assertEquals("value1", attributes.get("key1"));
        assertNull(attributes.get("nonexistent"));
    }

    @Test
    public void testPutDuplicateKey() {
        attributes.put("key", "first");
        attributes.put("key", "second");
        assertEquals("second", attributes.get("key"));
        assertEquals(1, attributes.size());
    }

    @Test
    public void testPutEmptyKey() {
        attributes.put("", "emptyKey");
        assertEquals("emptyKey", attributes.get(""));
    }

    @Test
    public void testPutNullValue() {
        attributes.put("nullVal", null);
        assertNull(attributes.get("nullVal"));
        // In HTML, null value should be treated as empty string? Actually, Attributes stores null.
        // But html() may handle it. We'll test later.
    }

    @Test
    public void testHasKey() {
        attributes.put("exists", "yes");
        assertTrue(attributes.hasKey("exists"));
        assertFalse(attributes.hasKey("no"));
    }

    @Test
    public void testRemove() {
        attributes.put("removeMe", "value");
        assertTrue(attributes.hasKey("removeMe"));
        attributes.remove("removeMe");
        assertFalse(attributes.hasKey("removeMe"));
        assertEquals(0, attributes.size());
    }

    @Test
    public void testRemoveNonExistent() {
        attributes.remove("nothing");
        // should not throw
        assertEquals(0, attributes.size());
    }

    @Test
    public void testSizeAndIsEmpty() {
        assertTrue(attributes.isEmpty());
        assertEquals(0, attributes.size());
        attributes.put("a", "1");
        assertFalse(attributes.isEmpty());
        assertEquals(1, attributes.size());
        attributes.put("b", "2");
        assertEquals(2, attributes.size());
    }

    @Test
    public void testAddAll() {
        Attributes other = new Attributes();
        other.put("x", "10");
        other.put("y", "20");
        attributes.addAll(other);
        assertEquals(2, attributes.size());
        assertEquals("10", attributes.get("x"));
        assertEquals("20", attributes.get("y"));
    }

    @Test
    public void testAddAllOverwrites() {
        attributes.put("z", "old");
        Attributes other = new Attributes();
        other.put("z", "new");
        attributes.addAll(other);
        assertEquals("new", attributes.get("z"));
        assertEquals(1, attributes.size());
    }

    @Test
    public void testIterator() {
        attributes.put("a", "1");
        attributes.put("b", "2");
        Iterator<Map.Entry<String, String>> it = attributes.iterator();
        assertTrue(it.hasNext());
        Map.Entry<String, String> entry1 = it.next();
        assertEquals("a", entry1.getKey());
        assertEquals("1", entry1.getValue());
        assertTrue(it.hasNext());
        Map.Entry<String, String> entry2 = it.next();
        assertEquals("b", entry2.getKey());
        assertEquals("2", entry2.getValue());
        assertFalse(it.hasNext());
    }

    @Test
    public void testAsList() {
        attributes.put("k1", "v1");
        attributes.put("k2", "v2");
        List<Attribute> list = attributes.asList();
        assertEquals(2, list.size());
        assertEquals("k1", list.get(0).getKey());
        assertEquals("v1", list.get(0).getValue());
        assertEquals("k2", list.get(1).getKey());
        assertEquals("v2", list.get(1).getValue());
    }

    @Test
    public void testHtmlEscaping() {
        // Bug 92: attribute values with special characters should be escaped
        attributes.put("data", "a&b");
        String html = attributes.html();
        // Expected: data="a&amp;b"
        assertTrue(html.contains("&amp;"));
        assertFalse(html.contains("&"));
    }

    @Test
    public void testHtmlEscapingLessThan() {
        attributes.put("test", "<script>");
        String html = attributes.html();
        assertTrue(html.contains("&lt;"));
        assertFalse(html.contains("<script>"));
    }

    @Test
    public void testHtmlEscapingQuotes() {
        attributes.put("title", "say \"hello\"");
        String html = attributes.html();
        // In HTML, attribute values can be quoted with double quotes, so internal double quotes should be escaped as &quot;
        assertTrue(html.contains("&quot;"));
    }

    @Test
    public void testHtmlEmptyValue() {
        attributes.put("disabled", "");
        String html = attributes.html();
        // Empty value should be represented as key="" or just key? In Jsoup, it's key=""
        assertTrue(html.contains("disabled=\"\""));
    }

    @Test
    public void testHtmlNullValue() {
        attributes.put("checked", null);
        String html = attributes.html();
        // Null value: should be just the key (boolean attribute) or key=""? In Jsoup, null results in key without value.
        // Actually, Attributes.html() handles null by outputting just the key.
        assertTrue(html.contains("checked"));
        assertFalse(html.contains("=\""));
    }

    @Test
    public void testHtmlMultipleAttributes() {
        attributes.put("id", "main");
        attributes.put("class", "content");
        String html = attributes.html();
        assertTrue(html.contains("id=\"main\""));
        assertTrue(html.contains("class=\"content\""));
        // Order should be preserved (insertion order)
        assertTrue(html.indexOf("id") < html.indexOf("class"));
    }

    @Test
    public void testToString() {
        attributes.put("href", "http://example.com");
        String toString = attributes.toString();
        // toString() should return the same as html() with a leading space? Actually, Attributes.toString() returns html().
        assertEquals(attributes.html(), toString);
    }

    @Test
    public void testEqualsAndHashCode() {
        Attributes a1 = new Attributes();
        a1.put("key", "value");
        Attributes a2 = new Attributes();
        a2.put("key", "value");
        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());

        a2.put("extra", "x");
        assertNotEquals(a1, a2);
    }

    @Test
    public void testEqualsWithDifferentOrder() {
        Attributes a1 = new Attributes();
        a1.put("a", "1");
        a1.put("b", "2");
        Attributes a2 = new Attributes();
        a2.put("b", "2");
        a2.put("a", "1");
        // Order should not matter for equality
        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());
    }

    @Test
    public void testClone() {
        attributes.put("clone", "me");
        Attributes cloned = attributes.clone();
        assertNotSame(attributes, cloned);
        assertEquals(attributes, cloned);
        assertEquals(attributes.size(), cloned.size());
        assertEquals(attributes.get("clone"), cloned.get("clone"));
        // Modify original, clone should not be affected
        attributes.put("new", "value");
        assertFalse(cloned.hasKey("new"));
    }

    @Test
    public void testCloneDeepCopy() {
        attributes.put("key", "value");
        Attributes cloned = attributes.clone();
        cloned.put("key", "changed");
        assertEquals("value", attributes.get("key"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutNullKey() {
        attributes.put(null, "value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveNullKey() {
        attributes.remove(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHasKeyNull() {
        attributes.hasKey(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetNullKey() {
        attributes.get(null);
    }
}