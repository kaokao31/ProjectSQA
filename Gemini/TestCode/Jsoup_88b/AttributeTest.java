package org.jsoup.nodes;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class AttributeTest {

    @Test
    public void testConstructorAndGetters() {
        Attribute attr = new Attribute("Key", "Value");
        assertEquals("Key", attr.getKey());
        assertEquals("Value", attr.getValue());

        attr.setKey("NewKey");
        assertEquals("NewKey", attr.getKey());

        String newVal = attr.setValue("NewValue");
        assertEquals("Value", newVal);
        assertEquals("NewValue", attr.getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullKey() {
        new Attribute(null, "value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyKey() {
        new Attribute("   ", "value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetKeyNull() {
        Attribute attr = new Attribute("key", "value");
        attr.setKey(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetKeyEmpty() {
        Attribute attr = new Attribute("key", "value");
        attr.setKey("   ");
    }

    @Test
    public void testHtml() {
        Attribute attr1 = new Attribute("href", "http://example.com");
        assertEquals("href=\"http://example.com\"", attr1.html());

        Attribute attr2 = new Attribute("checked", null);
        // Depending on Html.OutputSettings or default implementation
        assertTrue(attr2.html().contains("checked"));

        Attribute attr3 = new Attribute("foo", "bar\"baz");
        assertEquals("foo=\"bar&quot;baz\"", attr3.html());
    }

    @Test
    public void testHtmlAppendable() throws IOException {
        Attribute attr = new Attribute("data-test", "val");
        StringBuilder accum = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        
        attr.html(accum, out);
        assertEquals("data-test=\"val\"", accum.toString());
    }

    @Test
    public void testToString() {
        Attribute attr = new Attribute("class", "container");
        assertEquals("class=\"container\"", attr.toString());
        assertEquals(attr.html(), attr.toString());
    }

    @Test
    public void testCreateFromEncoded() {
        Attribute attr = Attribute.createFromEncoded("data-foo", "bar&amp;baz");
        assertEquals("data-foo", attr.getKey());
        assertEquals("bar&baz", attr.getValue());
    }

    @Test
    public void testIsDataAttribute() {
        Attribute dataAttr = new Attribute("data-name", "val");
        assertTrue(dataAttr.isDataAttribute());

        Attribute nonDataAttr = new Attribute("name", "val");
        assertFalse(nonDataAttr.isDataAttribute());

        Attribute shortData = new Attribute("data-", "val");
        assertFalse(shortData.isDataAttribute());
    }

    @Test
    public void testIsBooleanAttribute() {
        // Test known boolean attributes
        Attribute boolAttr = new Attribute("checked", "");
        assertTrue(Attribute.isBooleanAttribute("checked"));
        assertTrue(boolAttr.isBooleanAttribute());

        Attribute nonBoolAttr = new Attribute("href", "val");
        assertFalse(Attribute.isBooleanAttribute("href"));
        assertFalse(nonBoolAttr.isBooleanAttribute());

        assertFalse(Attribute.isBooleanAttribute("unknown"));
    }

    @Test
    public void testEqualsAndHashCode() {
        Attribute attr1 = new Attribute("key", "value");
        Attribute attr2 = new Attribute("key", "value");
        Attribute attr3 = new Attribute("key", "other");
        Attribute attr4 = new Attribute("other", "value");

        assertEquals(attr1, attr2);
        assertEquals(attr1.hashCode(), attr2.hashCode());

        assertNotEquals(attr1, attr3);
        assertNotEquals(attr1, attr4);
        assertNotEquals(attr1, null);
        assertNotEquals(attr1, "some string");
        
        assertEquals(attr1, attr1);
    }

    @Test
    public void testClone() {
        Attribute attr = new Attribute("key", "value");
        Attribute clone = attr.clone();

        assertEquals(attr, clone);
        assertNotSame(attr, clone);
    }

    @Test
    public void testShouldPreserveAttribute() {
        // Checking shouldPreserveAttribute via boolean output settings or similar if accessible
        Document.OutputSettings out = new Document.OutputSettings();
        Attribute attr = new Attribute("checked", "checked");
        assertFalse(attr.shouldPreserveAttribute(out));
    }
}