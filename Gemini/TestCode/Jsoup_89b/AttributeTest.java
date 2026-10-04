package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

public class AttributeTest {

    @Test
    public void testConstructorAndGetters() {
        Attribute attr = new Attribute("data-test", "value1");
        assertEquals("data-test", attr.getKey());
        assertEquals("value1", attr.getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullKey() {
        new Attribute(null, "value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyKey() {
        new Attribute("   ", "value");
    }

    @Test
    public void testSetValue() {
        Attribute attr = new Attribute("key", "oldVal");
        String old = attr.setValue("newVal");
        assertEquals("oldVal", old);
        assertEquals("newVal", attr.getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetValueNullKeyViaSetKey() {
        Attribute attr = new Attribute("key", "val");
        attr.setKey(null);
    }

    @Test
    public void testHtmlGeneration() {
        Attribute attr = new Attribute("href", "http://example.com");
        assertEquals("href=\"http://example.com\"", attr.html());
        assertEquals("href=\"http://example.com\"", attr.toString());
    }

    @Test
    public void testBooleanAttributeHtml() {
        Attribute attr = new Attribute("hidden", "");
        assertEquals("hidden", attr.html());

        Attribute nullValAttr = new Attribute("disabled", null);
        assertEquals("disabled", nullValAttr.html());
    }

    @Test
    public void testCreateFromEncoded() {
        Attribute attr = Attribute.createFromEncoded("data-foo", "bar&amp;baz");
        assertEquals("data-foo", attr.getKey());
        assertEquals("bar&baz", attr.getValue());
    }

    @Test
    public void testIsDataAttribute() {
        Attribute attr1 = new Attribute("data-name", "val");
        assertTrue(attr1.isDataAttribute());

        Attribute attr2 = new Attribute("data-", "val");
        assertFalse(attr2.isDataAttribute());

        Attribute attr3 = new Attribute("nodata", "val");
        assertFalse(attr3.isDataAttribute());
    }

    @Test
    public void testShouldBeBoolean() {
        // Test known boolean attributes based on HTML spec / jsoup implementation
        assertTrue(Attribute.isBooleanAttribute("allowfullscreen"));
        assertTrue(Attribute.isBooleanAttribute("checked"));
        
        Attribute attr = new Attribute("checked", "checked");
        assertTrue(attr.shouldBeBoolean(new Document.OutputSettings()));

        Attribute nonBool = new Attribute("class", "checked");
        assertFalse(nonBool.shouldBeBoolean(new Document.OutputSettings()));
    }

    @Test
    public void testEqualsAndHashCode() {
        Attribute attr1 = new Attribute("key", "val");
        Attribute attr2 = new Attribute("key", "val");
        Attribute attr3 = new Attribute("key", "diff");
        Attribute attr4 = new Attribute("other", "val");

        assertEquals(attr1, attr1);
        assertEquals(attr1, attr2);
        assertEquals(attr1.hashCode(), attr2.hashCode());

        assertNotEquals(attr1, attr3);
        assertNotEquals(attr1, attr4);
        assertNotEquals(attr1, null);
        assertNotEquals(attr1, "some string");
    }

    @Test
    public void testClone() {
        Attribute attr = new Attribute("key", "val");
        Attribute clone = attr.clone();

        assertEquals(attr, clone);
        assertNotSame(attr, clone);
    }

    @Test
    public void testSetKeyTrimming() {
        Attribute attr = new Attribute("  key  ", "val");
        assertEquals("key", attr.getKey());

        attr.setKey("  newKey  ");
        assertEquals("newKey", attr.getKey());
    }
}