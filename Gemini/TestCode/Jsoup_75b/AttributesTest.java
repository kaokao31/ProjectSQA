package org.jsoup.nodes;

import org.junit.Test;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class AttributesTest {

    @Test
    public void testGetIgnoreCase() {
        Attributes attributes = new Attributes();
        attributes.put("TestKey", "TestValue");

        assertEquals("TestValue", attributes.get("TestKey"));
        assertEquals("TestValue", attributes.get("testkey"));
        assertEquals("", attributes.get("NonExistent"));
    }

    @Test
    public void testGetIgnoreCaseCaseInsensitiveDataset() {
        Attributes attributes = new Attributes();
        attributes.putIgnoreCase("TESTKEY", "IgnoreCaseValue");

        assertEquals("IgnoreCaseValue", attributes.get("testkey"));
        assertEquals("IgnoreCaseValue", attributes.get("TESTKEY"));
    }

    @Test
    public void testPutAndGet() {
        Attributes attributes = new Attributes();
        attributes.put("key1", "value1");
        attributes.put("KEY1", "value2"); // Should overwrite or create depending on handling, usually case-sensitive map unless specified

        assertEquals("value2", attributes.get("KEY1"));
        assertTrue(attributes.hasKey("key1"));
        assertTrue(attributes.hasKey("KEY1"));
        assertTrue(attributes.hasKeyIgnoreCase("key1"));
        assertTrue(attributes.hasKeyIgnoreCase("KEY1"));
    }

    @Test
    public void testPutBoolean() {
        Attributes attributes = new Attributes();
        attributes.put("hidden", true);
        
        assertTrue(attributes.hasKey("hidden"));
        assertEquals("", attributes.get("hidden")); // boolean attributes in jsoup often store empty string or flag
    }

    @Test
    public void testRemove() {
        Attributes attributes = new Attributes();
        attributes.put("key1", "value1");
        attributes.putIgnoreCase("Key2", "value2");

        assertTrue(attributes.hasKey("key1"));
        attributes.remove("key1");
        assertFalse(attributes.hasKey("key1"));

        assertTrue(attributes.hasKeyIgnoreCase("key2"));
        attributes.removeIgnoreCase("key2");
        assertFalse(attributes.hasKeyIgnoreCase("key2"));
    }

    @Test
    public void testHtmlOutput() throws IOException {
        Attributes attributes = new Attributes();
        attributes.put("class", "my-class");
        attributes.put("id", "my-id");
        attributes.put("muted", true);

        String html = attributes.html();
        assertTrue(html.contains("class=\"my-class\""));
        assertTrue(html.contains("id=\"my-id\""));
        assertTrue(html.contains("muted"));

        StringBuilder accum = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        attributes.html(accum, out);
        assertTrue(accum.length() > 0);
    }

    @Test
    public void testSizeAndIsEmpty() {
        Attributes attributes = new Attributes();
        assertTrue(attributes.isEmpty());
        assertEquals(0, attributes.size());

        attributes.put("a", "1");
        assertFalse(attributes.isEmpty());
        assertEquals(1, attributes.size());

        attributes.remove("a");
        assertTrue(attributes.isEmpty());
        assertEquals(0, attributes.size());
    }

    @Test
    public void testAddAll() {
        Attributes first = new Attributes();
        first.put("a", "1");

        Attributes second = new Attributes();
        second.put("b", "2");
        second.put("a", "updated");

        first.addAll(second);
        assertEquals(2, first.size());
        assertEquals("updated", first.get("a"));
        assertEquals("2", first.get("b"));
    }

    @Test
    public void testIterator() {
        Attributes attributes = new Attributes();
        attributes.put("k1", "v1");
        attributes.put("k2", "v2");

        Iterator<Attribute> it = attributes.iterator();
        assertNotNull(it);
        int count = 0;
        while (it.hasNext()) {
            Attribute attr = it.next();
            assertNotNull(attr);
            count++;
        }
        assertEquals(2, count);
    }

    @Test
    public void testAsList() {
        Attributes attributes = new Attributes();
        attributes.put("k1", "v1");
        List<Attribute> list = attributes.asList();
        assertEquals(1, list.size());
        assertEquals("k1", list.get(0).getKey());
        assertEquals("v1", list.get(0).getValue());
    }

    @Test
    public void testDataset() {
        Attributes attributes = new Attributes();
        attributes.put("data-test-id", "123");
        attributes.put("class", "foo");

        Map<String, String> dataset = attributes.dataset();
        assertEquals("123", dataset.get("testId"));
        
        dataset.put("testId", "456");
        assertEquals("456", attributes.get("data-test-id"));

        attributes.put("data-foo-bar", "baz");
        assertEquals("baz", attributes.dataset().get("fooBar"));
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
        assertNotEquals(a1, "NotAnAttributesObject");
        assertEquals(a1, a1);
    }

    @Test
    public void testClone() {
        Attributes attributes = new Attributes();
        attributes.put("key", "val");

        Attributes clone = attributes.clone();
        assertNotSame(attributes, clone);
        assertEquals(attributes, clone);
        assertEquals("val", clone.get("key"));
    }

    @Test
    public void testNormalise() {
        Attributes attributes = new Attributes();
        attributes.put("TESTKEY", "val");
        attributes.normalise();
        assertTrue(attributes.hasKey("testkey"));
    }
}