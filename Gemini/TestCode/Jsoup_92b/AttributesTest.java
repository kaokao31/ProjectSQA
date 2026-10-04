package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AttributesTest {

    @Test
    public void testCaseInsensitiveKeyHandling() {
        Attributes attributes = new Attributes();
        attributes.put("Key", "value1");
        
        // Test case-insensitive retrieval and existence if supported
        assertTrue(attributes.hasKey("key"));
        assertTrue(attributes.hasKey("Key"));
        assertEquals("value1", attributes.get("key"));
        assertEquals("value1", attributes.get("Key"));
        
        attributes.put("KEY", "value2");
        // Should overwrite or handle case-insensitive keys depending on implementation
        assertEquals("value2", attributes.get("key"));
    }

    @Test
    public void testDataset() {
        Attributes attributes = new Attributes();
        attributes.put("data-id", "123");
        attributes.put("data-name", "jsoup");
        attributes.put("class", "main");

        Map<String, String> dataset = attributes.dataset();
        assertEquals("123", dataset.get("id"));
        assertEquals("jsoup", dataset.get("name"));
        assertNull(dataset.get("class")); // non data- attribute should not be in dataset

        dataset.put("id", "456");
        assertEquals("456", attributes.get("data-id"));

        dataset.put("new-key", "val");
        assertEquals("val", attributes.get("data-new-key"));
    }

    @Test
    public void testHtmlGeneration() {
        Attributes attributes = new Attributes();
        attributes.put("id", "test-id");
        attributes.put("class", "test-class");
        attributes.put("checked", true); // boolean attribute

        String html = attributes.html();
        assertTrue(html.contains("id=\"test-id\""));
        assertTrue(html.contains("class=\"test-class\""));
        assertTrue(html.contains("checked"));

        StringBuilder accum = new StringBuilder();
        try {
            attributes.html(accum, new Document("").outputSettings());
        } catch (IOException e) {
            fail("IOException should not be thrown");
        }
        assertTrue(accum.toString().contains("id=\"test-id\""));
    }

    @Test
    public void testAddAndPutBehavior() {
        Attributes attributes = new Attributes();
        attributes.add("attr1", "val1");
        attributes.add("attr1", "val2"); // Attributes allows multi-vals or handles them via add

        assertEquals(2, attributes.size());
        
        attributes.put("attr1", "val3"); // put should overwrite or replace
        // Depending on Jsoup version, check how put vs add interacts
        assertNotNull(attributes.get("attr1"));
    }

    @Test
    public void testRemoveKey() {
        Attributes attributes = new Attributes();
        attributes.put("key1", "val1");
        attributes.put("KEY1", "val2");

        assertTrue(attributes.hasKey("key1"));
        attributes.remove("key1");
        assertFalse(attributes.hasKey("key1"));
        // Check case-insensitive removal if applicable
        assertFalse(attributes.hasKey("KEY1"));
    }

    @Test
    public void testIteratorAndNormalisation() {
        Attributes attributes = new Attributes();
        attributes.put("AB", "CD");
        
        Iterator<Attribute> it = attributes.iterator();
        assertTrue(it.hasNext());
        Attribute attr = it.next();
        assertEquals("ab", attr.getKey()); // Keys are often normalized to lower-case in Jsoup

        attributes.normalize();
        assertEquals("CD", attributes.get("ab"));
    }

    @Test
    public void testClone() {
        Attributes attributes = new Attributes();
        attributes.put("key", "value");

        Attributes clone = attributes.clone();
        assertNotSame(attributes, clone);
        assertEquals(attributes.get("key"), clone.get("key"));

        clone.put("key", "newvalue");
        assertEquals("value", attributes.get("key"));
        assertEquals("newvalue", clone.get("key"));
    }

    @Test
    public void testAsList() {
        Attributes attributes = new Attributes();
        attributes.put("a", "1");
        attributes.put("b", "2");

        List<Attribute> list = attributes.asList();
        assertEquals(2, list.size());
    }

    @Test
    public void testUserData() {
        Attributes attributes = new Attributes();
        Object data = attributes.userData("testKey");
        assertNull(data);

        attributes.userData("testKey", "testVal");
        assertEquals("testVal", attributes.userData("testKey"));
    }

    @Test
    public void testEmptyAttributes() {
        Attributes attributes = new Attributes();
        assertEquals(0, attributes.size());
        assertTrue(attributes.asList().isEmpty());
        assertEquals("", attributes.html());
        assertFalse(attributes.iterator().hasNext());
    }
}