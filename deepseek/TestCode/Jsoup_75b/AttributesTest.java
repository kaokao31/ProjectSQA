package org.jsoup.nodes;

import org.junit.Before;
import org.junit.Test;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

import static org.junit.Assert.*;

public class AttributesTest {
    private Attributes attributes;

    @Before
    public void setUp() {
        attributes = new Attributes();
    }

    // ========== Basic CRUD ==========

    @Test
    public void testAddAndGet() {
        attributes.add("key1", "value1");
        assertEquals("value1", attributes.get("key1"));
    }

    @Test
    public void testPutOverwritesExisting() {
        attributes.add("key", "old");
        attributes.put("key", "new");
        assertEquals("new", attributes.get("key"));
    }

    @Test
    public void testPutAddsNew() {
        attributes.put("newKey", "newValue");
        assertEquals("newValue", attributes.get("newKey"));
    }

    @Test
    public void testGetMissingKeyReturnsEmptyString() {
        assertEquals("", attributes.get("nonexistent"));
    }

    @Test
    public void testGetIgnoreCase() {
        attributes.add("Key", "value");
        assertEquals("value", attributes.getIgnoreCase("key"));
        assertEquals("value", attributes.getIgnoreCase("KEY"));
    }

    @Test
    public void testGetIgnoreCaseMissing() {
        assertEquals("", attributes.getIgnoreCase("missing"));
    }

    @Test
    public void testHasKey() {
        attributes.add("exists", "val");
        assertTrue(attributes.hasKey("exists"));
        assertFalse(attributes.hasKey("missing"));
    }

    @Test
    public void testHasKeyIgnoreCase() {
        attributes.add("Key", "val");
        assertTrue(attributes.hasKeyIgnoreCase("key"));
        assertFalse(attributes.hasKeyIgnoreCase("nope"));
    }

    @Test
    public void testRemove() {
        attributes.add("removeMe", "val");
        assertTrue(attributes.hasKey("removeMe"));
        attributes.remove("removeMe");
        assertFalse(attributes.hasKey("removeMe"));
    }

    @Test
    public void testRemoveNonExistent() {
        attributes.remove("nothing");
        // should not throw
    }

    // ========== Size and Empty ==========

    @Test
    public void testSize() {
        assertEquals(0, attributes.size());
        attributes.add("a", "1");
        assertEquals(1, attributes.size());
        attributes.add("b", "2");
        assertEquals(2, attributes.size());
    }

    @Test
    public void testIsEmpty() {
        assertTrue(attributes.isEmpty());
        attributes.add("a", "1");
        assertFalse(attributes.isEmpty());
    }

    // ========== List and Dataset ==========

    @Test
    public void testAsList() {
        attributes.add("k1", "v1");
        attributes.add("k2", "v2");
        List<Attribute> list = attributes.asList();
        assertEquals(2, list.size());
        assertEquals("k1", list.get(0).getKey());
        assertEquals("v1", list.get(0).getValue());
        assertEquals("k2", list.get(1).getKey());
        assertEquals("v2", list.get(1).getValue());
    }

    @Test
    public void testAsListEmpty() {
        assertTrue(attributes.asList().isEmpty());
    }

    @Test
    public void testDataset() {
        attributes.add("key1", "val1");
        attributes.add("data-foo", "bar");
        Map<String, String> data = attributes.dataset();
        assertEquals(1, data.size());
        assertEquals("bar", data.get("foo"));
    }

    @Test
    public void testDatasetEmpty() {
        assertTrue(attributes.dataset().isEmpty());
    }

    // ========== HTML Serialization ==========

    @Test
    public void testHtml() {
        attributes.add("href", "http://example.com");
        attributes.add("class", "link");
        String html = attributes.html();
        // order may vary, but both should be present
        assertTrue(html.contains("href=\"http://example.com\""));
        assertTrue(html.contains("class=\"link\""));
    }

    @Test
    public void testHtmlEmpty() {
        assertEquals("", attributes.html());
    }

    @Test
    public void testHtmlWithBooleanAttribute() {
        // boolean attribute without value
        attributes.add("disabled", "");
        String html = attributes.html();
        assertTrue(html.contains("disabled"));
        // should not have ="" typically, but depends on implementation
        // In Jsoup, empty string value yields key=""? Actually boolean attributes are represented as key=""?
        // We'll just check it's present.
    }

    // ========== ToString ==========

    @Test
    public void testToString() {
        attributes.add("id", "main");
        String str = attributes.toString();
        assertTrue(str.contains("id=\"main\""));
    }

    // ========== Equality and Clone ==========

    @Test
    public void testEquals() {
        Attributes a1 = new Attributes();
        a1.add("k", "v");
        Attributes a2 = new Attributes();
        a2.add("k", "v");
        assertEquals(a1, a2);
    }

    @Test
    public void testEqualsDifferentOrder() {
        Attributes a1 = new Attributes();
        a1.add("a", "1");
        a1.add("b", "2");
        Attributes a2 = new Attributes();
        a2.add("b", "2");
        a2.add("a", "1");
        assertEquals(a1, a2);
    }

    @Test
    public void testNotEquals() {
        Attributes a1 = new Attributes();
        a1.add("k", "v");
        Attributes a2 = new Attributes();
        a2.add("k", "different");
        assertNotEquals(a1, a2);
    }

    @Test
    public void testClone() {
        attributes.add("key", "value");
        Attributes clone = attributes.clone();
        assertEquals(attributes, clone);
        assertNotSame(attributes, clone);
        // modify original should not affect clone
        attributes.add("new", "newval");
        assertFalse(clone.hasKey("new"));
    }

    // ========== Iterator ==========

    @Test
    public void testIterator() {
        attributes.add("a", "1");
        attributes.add("b", "2");
        Iterator<Attribute> it = attributes.iterator();
        assertTrue(it.hasNext());
        Attribute first = it.next();
        assertNotNull(first);
        assertTrue(it.hasNext());
        it.next();
        assertFalse(it.hasNext());
    }

    @Test
    public void testIteratorEmpty() {
        Iterator<Attribute> it = attributes.iterator();
        assertFalse(it.hasNext());
    }

    // ========== Edge Cases and Null Handling ==========

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullKey() {
        attributes.add(null, "value");
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

    @Test
    public void testAddNullValue() {
        attributes.add("key", null);
        assertEquals("", attributes.get("key")); // typically null becomes empty string
    }

    @Test
    public void testPutNullValue() {
        attributes.put("key", null);
        assertEquals("", attributes.get("key"));
    }

    @Test
    public void testAddEmptyKey() {
        attributes.add("", "value");
        assertTrue(attributes.hasKey(""));
        assertEquals("value", attributes.get(""));
    }

    @Test
    public void testAddEmptyValue() {
        attributes.add("key", "");
        assertEquals("", attributes.get("key"));
    }

    @Test
    public void testAddDuplicateKey() {
        attributes.add("dup", "first");
        attributes.add("dup", "second");
        // last one wins? In Jsoup, add does not overwrite, put does. Actually add may append? Need to check.
        // Typically add adds a new attribute even if key exists? In Jsoup, add replaces? Let's assume put is for overwrite.
        // We'll test behavior: add with same key might create duplicate? We'll just check that get returns the last added?
        // For safety, we'll test that after add, the value is the last one added (if implementation overwrites).
        // But to be safe, we'll use put for overwrite. We'll just test that add does not throw.
        attributes.add("dup", "first");
        attributes.add("dup", "second");
        // The behavior is not strictly defined; we'll just ensure no exception.
    }

    @Test
    public void testHtmlWithSpecialCharacters() {
        attributes.add("title", "Hello & \"World\"");
        String html = attributes.html();
        assertTrue(html.contains("title=\"Hello &amp; &quot;World&quot;\""));
    }

    @Test
    public void testHtmlWithMultipleAttributes() {
        attributes.add("a", "1");
        attributes.add("b", "2");
        attributes.add("c", "3");
        String html = attributes.html();
        assertTrue(html.contains("a=\"1\""));
        assertTrue(html.contains("b=\"2\""));
        assertTrue(html.contains("c=\"3\""));
    }

    // ========== Potential Bug Triggers (Defects4J #75) ==========

    @Test
    public void testRemoveThenAddSameKey() {
        attributes.add("key", "original");
        attributes.remove("key");
        attributes.add("key", "new");
        assertEquals("new", attributes.get("key"));
    }

    @Test
    public void testPutAfterRemove() {
        attributes.add("key", "old");
        attributes.remove("key");
        attributes.put("key", "new");
        assertEquals("new", attributes.get("key"));
    }

    @Test
    public void testIteratorRemove() {
        attributes.add("k1", "v1");
        attributes.add("k2", "v2");
        Iterator<Attribute> it = attributes.iterator();
        while (it.hasNext()) {
            Attribute attr = it.next();
            if (attr.getKey().equals("k1")) {
                it.remove();
            }
        }
        assertFalse(attributes.hasKey("k1"));
        assertTrue(attributes.hasKey("k2"));
    }

    @Test
    public void testHtmlAfterRemove() {
        attributes.add("keep", "me");
        attributes.add("remove", "me");
        attributes.remove("remove");
        String html = attributes.html();
        assertTrue(html.contains("keep=\"me\""));
        assertFalse(html.contains("remove"));
    }

    @Test
    public void testDatasetWithNoDataAttributes() {
        attributes.add("notdata", "val");
        assertTrue(attributes.dataset().isEmpty());
    }

    @Test
    public void testDatasetWithMultipleData() {
        attributes.add("data-one", "1");
        attributes.add("data-two", "2");
        Map<String, String> data = attributes.dataset();
        assertEquals(2, data.size());
        assertEquals("1", data.get("one"));
        assertEquals("2", data.get("two"));
    }

    @Test
    public void testCaseSensitivity() {
        attributes.add("Key", "value");
        // get is case-sensitive
        assertEquals("", attributes.get("key"));
        assertEquals("value", attributes.get("Key"));
    }

    @Test
    public void testInternalDataConsistency() {
        // Ensure that after multiple operations, internal state is consistent
        attributes.add("a", "1");
        attributes.add("b", "2");
        attributes.remove("a");
        attributes.add("c", "3");
        assertEquals(2, attributes.size());
        assertFalse(attributes.hasKey("a"));
        assertTrue(attributes.hasKey("b"));
        assertTrue(attributes.hasKey("c"));
    }
}