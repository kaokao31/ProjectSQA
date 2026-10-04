package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

public class AttributeTest {

    @Test
    public void testConstructorAndGetters() {
        Attribute attribute = new Attribute("  Key  ", "Value");
        assertEquals("Key", attribute.getKey());
        assertEquals("Value", attribute.getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullKey() {
        new Attribute(null, "Value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyKey() {
        new Attribute("   ", "Value");
    }

    @Test
    public void testSetKey() {
        Attribute attribute = new Attribute("key1", "val1");
        String oldKey = attribute.setKey("  key2  ");
        assertEquals("key1", oldKey);
        assertEquals("key2", attribute.getKey());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetKeyNull() {
        Attribute attribute = new Attribute("key1", "val1");
        attribute.setKey(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetKeyEmpty() {
        Attribute attribute = new Attribute("key1", "val1");
        attribute.setKey("   ");
    }

    @Test
    public void testSetValue() {
        Attribute attribute = new Attribute("key", "val1");
        String oldValue = attribute.setValue("val2");
        assertEquals("val1", oldValue);
        assertEquals("val2", attribute.getValue());
    }

    @Test
    public void testSetValueNull() {
        Attribute attribute = new Attribute("key", "val1");
        String oldValue = attribute.setValue(null);
        assertEquals("val1", oldValue);
        assertEquals("", attribute.getValue());
    }

    @Test
    public void testHtml() {
        Attribute attribute = new Attribute("href", "http://example.com");
        assertEquals("href=\"http://example.com\"", attribute.html());
    }

    @Test
    public void testToString() {
        Attribute attribute = new Attribute("class", "container");
        assertEquals("class=\"container\"", attribute.toString());
    }

    @Test
    public void testCreateFromEncoded() {
        Attribute attribute = Attribute.createFromEncoded("data-test", "val&amp;ue");
        assertEquals("data-test", attribute.getKey());
        assertEquals("val&ue", attribute.getValue());
    }

    @Test
    public void testIsDataAttribute() {
        Attribute dataAttr = new Attribute("data-name", "val");
        assertTrue(dataAttr.isDataAttribute());

        Attribute normalAttr = new Attribute("name", "val");
        assertFalse(normalAttr.isDataAttribute());

        Attribute shortDataAttr = new Attribute("data-", "val");
        assertFalse(shortDataAttr.isDataAttribute());
    }

    @Test
    public void testShouldBeBoolean() {
        Attributes parent = new Attributes();
        Attribute boolAttr = new Attribute("allowfullscreen", "");
        assertTrue(boolAttr.isBooleanAttribute());
        
        Attribute normalAttr = new Attribute("class", "foo");
        assertFalse(normalAttr.isBooleanAttribute());
    }

    @Test
    public void testEqualsAndHashCode() {
        Attribute attr1 = new Attribute("key", "value");
        Attribute attr2 = new Attribute("key", "value");
        Attribute attr3 = new Attribute("key", "other");
        Attribute attr4 = new Attribute("other", "value");

        assertTrue(attr1.equals(attr1));
        assertTrue(attr1.equals(attr2));
        assertEquals(attr1.hashCode(), attr2.hashCode());

        assertFalse(attr1.equals(attr3));
        assertFalse(attr1.equals(attr4));
        assertFalse(attr1.equals(null));
        assertFalse(attr1.equals(new Object()));
    }

    @Test
    public void testClone() {
        Attribute attr1 = new Attribute("key", "value");
        Attribute attr2 = attr1.clone();

        assertEquals(attr1, attr2);
        assertNotSame(attr1, attr2);
    }
}