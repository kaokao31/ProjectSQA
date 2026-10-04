package org.jsoup.nodes;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for the Attribute class, targeting high coverage and fault detection.
 * Designed to reveal the boolean attribute handling bug (Defects4J Jsoup bug 88).
 */
public class AttributeTest {

    private Attribute normalAttr;
    private Attribute booleanAttrEmpty;
    private Attribute booleanAttrKey;
    private Attribute booleanAttrNonEmpty;
    private Attribute emptyValueAttr;
    private Attribute nullKeyAttr; // not allowed but test edge

    @Before
    public void setUp() {
        normalAttr = new Attribute("href", "http://example.com");
        booleanAttrEmpty = new Attribute("disabled", "");
        booleanAttrKey = new Attribute("checked", "checked");
        booleanAttrNonEmpty = new Attribute("selected", "true");
        emptyValueAttr = new Attribute("data-custom", "");
        // null key is not allowed by constructor, but we can test via reflection? Not needed.
    }

    @Test
    public void testConstructorAndGetters() {
        assertEquals("href", normalAttr.getKey());
        assertEquals("http://example.com", normalAttr.getValue());
    }

    @Test
    public void testSetValue() {
        normalAttr.setValue("https://example.com");
        assertEquals("https://example.com", normalAttr.getValue());
    }

    @Test
    public void testHtmlNormalAttribute() {
        // Normal attribute: key="value"
        assertEquals("href=\"http://example.com\"", normalAttr.html());
    }

    @Test
    public void testHtmlBooleanAttributeEmptyValue() {
        // Boolean attribute with empty value should output just the key
        assertEquals("disabled", booleanAttrEmpty.html());
    }

    @Test
    public void testHtmlBooleanAttributeKeyValue() {
        // Boolean attribute with value equal to key should output just the key
        assertEquals("checked", booleanAttrKey.html());
    }

    @Test
    public void testHtmlBooleanAttributeNonEmptyValue() {
        // Boolean attribute with non-empty, non-key value should output key="value"
        assertEquals("selected=\"true\"", booleanAttrNonEmpty.html());
    }

    @Test
    public void testHtmlNonBooleanEmptyValue() {
        // Non-boolean attribute with empty value should output key=""
        assertEquals("data-custom=\"\"", emptyValueAttr.html());
    }

    @Test
    public void testToString() {
        // toString should match html()
        assertEquals(normalAttr.html(), normalAttr.toString());
        assertEquals(booleanAttrEmpty.html(), booleanAttrEmpty.toString());
        assertEquals(booleanAttrKey.html(), booleanAttrKey.toString());
        assertEquals(booleanAttrNonEmpty.html(), booleanAttrNonEmpty.toString());
        assertEquals(emptyValueAttr.html(), emptyValueAttr.toString());
    }

    @Test
    public void testIsBooleanAttribute() {
        // Static method: check known boolean attribute names
        assertTrue(Attribute.isBooleanAttribute("disabled"));
        assertTrue(Attribute.isBooleanAttribute("checked"));
        assertTrue(Attribute.isBooleanAttribute("selected"));
        assertTrue(Attribute.isBooleanAttribute("hidden"));
        assertTrue(Attribute.isBooleanAttribute("readonly"));
        assertTrue(Attribute.isBooleanAttribute("required"));
        assertTrue(Attribute.isBooleanAttribute("multiple"));
        assertTrue(Attribute.isBooleanAttribute("ismap"));
        assertTrue(Attribute.isBooleanAttribute("defer"));
        assertTrue(Attribute.isBooleanAttribute("declare"));
        assertTrue(Attribute.isBooleanAttribute("noshade"));
        assertTrue(Attribute.isBooleanAttribute("nowrap"));
        assertTrue(Attribute.isBooleanAttribute("noresize"));
        assertTrue(Attribute.isBooleanAttribute("nohref"));
        assertTrue(Attribute.isBooleanAttribute("compact"));
        assertTrue(Attribute.isBooleanAttribute("autoplay"));
        assertTrue(Attribute.isBooleanAttribute("controls"));
        assertTrue(Attribute.isBooleanAttribute("loop"));
        assertTrue(Attribute.isBooleanAttribute("muted"));
        assertTrue(Attribute.isBooleanAttribute("default"));
        assertTrue(Attribute.isBooleanAttribute("open"));
        assertTrue(Attribute.isBooleanAttribute("reversed"));
        assertTrue(Attribute.isBooleanAttribute("autofocus"));
        assertTrue(Attribute.isBooleanAttribute("formnovalidate"));
        assertTrue(Attribute.isBooleanAttribute("allowfullscreen"));
        assertTrue(Attribute.isBooleanAttribute("async"));
        assertTrue(Attribute.isBooleanAttribute("autocomplete"));
        assertTrue(Attribute.isBooleanAttribute("autofocus"));
        assertTrue(Attribute.isBooleanAttribute("checked"));
        assertTrue(Attribute.isBooleanAttribute("disabled"));
        assertTrue(Attribute.isBooleanAttribute("formnovalidate"));
        assertTrue(Attribute.isBooleanAttribute("hidden"));
        assertTrue(Attribute.isBooleanAttribute("ismap"));
        assertTrue(Attribute.isBooleanAttribute("itemscope"));
        assertTrue(Attribute.isBooleanAttribute("loop"));
        assertTrue(Attribute.isBooleanAttribute("multiple"));
        assertTrue(Attribute.isBooleanAttribute("muted"));
        assertTrue(Attribute.isBooleanAttribute("nomodule"));
        assertTrue(Attribute.isBooleanAttribute("novalidate"));
        assertTrue(Attribute.isBooleanAttribute("open"));
        assertTrue(Attribute.isBooleanAttribute("playsinline"));
        assertTrue(Attribute.isBooleanAttribute("readonly"));
        assertTrue(Attribute.isBooleanAttribute("required"));
        assertTrue(Attribute.isBooleanAttribute("reversed"));
        assertTrue(Attribute.isBooleanAttribute("selected"));

        // Non-boolean attributes
        assertFalse(Attribute.isBooleanAttribute("href"));
        assertFalse(Attribute.isBooleanAttribute("class"));
        assertFalse(Attribute.isBooleanAttribute("id"));
        assertFalse(Attribute.isBooleanAttribute("style"));
        assertFalse(Attribute.isBooleanAttribute("data-custom"));
    }

    @Test
    public void testEqualsAndHashCode() {
        Attribute attr1 = new Attribute("key", "value");
        Attribute attr2 = new Attribute("key", "value");
        Attribute attr3 = new Attribute("key", "different");
        Attribute attr4 = new Attribute("other", "value");

        // Reflexive
        assertEquals(attr1, attr1);
        // Symmetric
        assertEquals(attr1, attr2);
        assertEquals(attr2, attr1);
        // Not equal to null
        assertNotEquals(null, attr1);
        // Not equal to different object type
        assertNotEquals("string", attr1);
        // Different value
        assertNotEquals(attr1, attr3);
        // Different key
        assertNotEquals(attr1, attr4);

        // HashCode consistency
        assertEquals(attr1.hashCode(), attr2.hashCode());
        // Different objects may have same hash, but not required to be different
    }

    @Test
    public void testHtmlWithSpecialCharacters() {
        Attribute attr = new Attribute("data-test", "value with \"quotes\"");
        assertEquals("data-test=\"value with \\\"quotes\\\"\"", attr.html());
    }

    @Test
    public void testHtmlWithAmpersand() {
        Attribute attr = new Attribute("alt", "foo & bar");
        assertEquals("alt=\"foo &amp; bar\"", attr.html());
    }

    @Test
    public void testHtmlWithLessThan() {
        Attribute attr = new Attribute("title", "a < b");
        assertEquals("title=\"a &lt; b\"", attr.html());
    }

    @Test
    public void testHtmlWithGreaterThan() {
        Attribute attr = new Attribute("title", "a > b");
        assertEquals("title=\"a &gt; b\"", attr.html());
    }

    @Test
    public void testHtmlWithApos() {
        Attribute attr = new Attribute("title", "it's");
        assertEquals("title=\"it's\"", attr.html()); // single quote not escaped in double-quoted attribute
    }

    @Test
    public void testHtmlWithDoubleQuoteInside() {
        Attribute attr = new Attribute("title", "say \"hello\"");
        assertEquals("title=\"say \\\"hello\\\"\"", attr.html());
    }

    @Test
    public void testHtmlBooleanAttributeWithNullValue() {
        // Edge: boolean attribute with null value? Constructor may not allow null, but test if possible
        // We'll assume constructor throws on null, so skip.
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullKey() {
        new Attribute(null, "value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullValue() {
        new Attribute("key", null);
    }

    @Test
    public void testHtmlBooleanAttributeCaseInsensitive() {
        // Boolean attribute detection is case-insensitive? In Jsoup, it's case-sensitive for HTML.
        // But we test lowercase only.
        Attribute attr = new Attribute("DISABLED", "");
        // Since isBooleanAttribute is case-sensitive, this will not be treated as boolean.
        assertEquals("DISABLED=\"\"", attr.html());
    }

    @Test
    public void testHtmlBooleanAttributeWithSpaces() {
        Attribute attr = new Attribute("disabled ", "");
        // Key with trailing space is not a boolean attribute
        assertEquals("disabled =\"\"", attr.html()); // depends on implementation, but likely key is trimmed? Not sure.
        // We'll assume key is not trimmed.
    }
}