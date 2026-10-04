package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

public class AttributesTest {

    @Test
    public void testCaseInsensitiveGet() {
        Attributes attributes = new Attributes();
        attributes.put("TestKey", "TestValue");
        
        // Exact match
        assertEquals("TestValue", attributes.get("TestKey"));
        
        // Case-insensitive match (specifically targeting the bug in Jsoup 57 where case-insensitive 
        // lookup fails to find keys if they were added with mixed case and accessed differently,
        // or during removeIgnoreCase)
        assertEquals("TestValue", attributes.getIgnoreCase("testkey"));
        assertEquals("TestValue", attributes.getIgnoreCase("TESTKEY"));
        
        assertEquals("", attributes.get("NonExistent"));
        assertEquals("", attributes.getIgnoreCase("NonExistent"));
    }

    @Test
    public void testRemoveIgnoreCase() {
        Attributes attributes = new Attributes();
        attributes.put("Data-Id", "123");
        attributes.put("class", "container");

        // Remove using different case
        attributes.removeIgnoreCase("DATA-ID");
        
        assertFalse(attributes.hasKey("Data-Id"));
        assertFalse(attributes.hasKey("data-id"));
        assertTrue(attributes.hasKey("class"));
        
        // Remove non-existent
        attributes.removeIgnoreCase("nonexistent");
        assertEquals(1, attributes.size());
    }

    @Test
    public void testHasKeyIgnoreCase() {
        Attributes attributes = new Attributes();
        attributes.put("MyAttr", "value");

        assertTrue(attributes.hasKeyIgnoreCase("myattr"));
        assertTrue(attributes.hasKeyIgnoreCase("MYATTR"));
        assertTrue(attributes.hasKeyIgnoreCase("MyAttr"));
        assertFalse(attributes.hasKeyIgnoreCase("other"));
    }

    @Test
    public void testHtmlGeneration() {
        Attributes attributes = new Attributes();
        attributes.put("id", "main");
        attributes.put("class", "content\"'<>");
        
        String html = attributes.html();
        assertTrue(html.contains("id=\"main\""));
        assertTrue(html.contains("class="));
    }

    @Test
    public void testDataset() {
        Attributes attributes = new Attributes();
        attributes.put("data-name", "jsoup");
        attributes.put("data-val-ue", "1");
        attributes.put("normal", "notdata");

        java.util.Map<String, String> dataset = attributes.dataset();
        assertEquals(2, dataset.size());
        assertEquals("jsoup", dataset.get("name"));
        assertEquals("1", dataset.get("valUe"));
    }

    @Test
    public void testIteratorAndAddAll() {
        Attributes attributes = new Attributes();
        attributes.put("a", "1");
        attributes.put("b", "2");

        Iterator<Attribute> iter = attributes.iterator();
        assertTrue(iter.hasNext());
        Attribute first = iter.next();
        assertEquals("a", first.getKey());

        Attributes clone = attributes.clone();
        assertEquals(attributes.size(), clone.size());
        assertEquals(attributes.get("a"), clone.get("a"));

        Attributes other = new Attributes();
        other.put("c", "3");
        attributes.addAll(other);
        assertEquals(3, attributes.size());
        assertEquals("3", attributes.get("c"));
    }

    @Test
    public void testEqualsAndHashCode() {
        Attributes a1 = new Attributes();
        a1.put("key", "val");

        Attributes a2 = new Attributes();
        a2.put("key", "val");

        Attributes a3 = new Attributes();
        a3.put("key", "diff");

        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());
        assertNotEquals(a1, a3);
        assertNotEquals(a1, null);
        assertNotEquals(a1, "string");
    }

    @Test
    public void testEmptyAttributes() {
        Attributes attributes = new Attributes();
        assertTrue(attributes.asList().isEmpty());
        assertEquals(0, attributes.size());
        assertFalse(attributes.iterator().hasNext());
        assertEquals("", attributes.html());
    }
}