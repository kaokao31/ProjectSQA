package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class TagTest {

    @Test
    public void testValueOfKnownTag() {
        Tag tag = Tag.valueOf("p");
        assertNotNull(tag);
        assertEquals("p", tag.getName());
        assertTrue(tag.isBlock());
        assertFalse(tag.isInline());
        assertTrue(tag.formatAsBlock());
        assertFalse(tag.canContainBlock());
        assertTrue(tag.isKnownTag());
    }

    @Test
    public void testValueOfKnownTagMixedCase() {
        Tag tag = Tag.valueOf("DIV");
        assertNotNull(tag);
        assertEquals("div", tag.getName());
        assertTrue(tag.isBlock());
        assertTrue(tag.isKnownTag());
    }

    @Test
    public void testValueOfUnknownTag() {
        Tag tag = Tag.valueOf("unknowncustomtag");
        assertNotNull(tag);
        assertEquals("unknowncustomtag", tag.getName());
        assertFalse(tag.isBlock());
        assertTrue(tag.isInline());
        assertTrue(tag.formatAsBlock());
        assertTrue(tag.canContainBlock());
        assertFalse(tag.isKnownTag());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOfNull() {
        Tag.valueOf(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOfEmpty() {
        Tag.valueOf("");
    }

    @Test
    public void testValueOfWhitespace() {
        Tag tag = Tag.valueOf("   ");
        assertNotNull(tag);
        assertEquals("   ", tag.getName());
        assertFalse(tag.isKnownTag());
    }

    @Test
    public void testSettingMethodsForCustomTag() {
        Tag tag = Tag.valueOf("custom");
        
        // Test default state of custom tag
        assertFalse(tag.isBlock());
        assertTrue(tag.canContainBlock());
        assertTrue(tag.formatAsBlock());
        assertFalse(tag.isSelfClosing());
        assertFalse(tag.isPreservedWhitespace());

        // Modify via fluent setters
        Tag blockTag = tag.setBlock();
        assertSame(tag, blockTag);
        assertTrue(tag.isBlock());

        Tag selfClosingTag = tag.setSelfClosing();
        assertSame(tag, selfClosingTag);
        assertTrue(tag.isSelfClosing());

        Tag preserveTag = tag.setPreserveWhitespace();
        assertSame(tag, preserveTag);
        assertTrue(tag.isPreservedWhitespace());
    }

    @Test
    public void testEqualityAndHashCode() {
        Tag tag1 = Tag.valueOf("p");
        Tag tag2 = Tag.valueOf("P");
        Tag tag3 = Tag.valueOf("div");
        Tag tag4 = Tag.valueOf("custom");
        Tag tag5 = Tag.valueOf("custom");

        assertEquals(tag1, tag2);
        assertEquals(tag1.hashCode(), tag2.hashCode());

        assertNotEquals(tag1, tag3);
        assertNotEquals(tag1, null);
        assertNotEquals(tag1, "p");

        assertEquals(tag4, tag5);
        assertEquals(tag4.hashCode(), tag5.hashCode());
    }

    @Test
    public void testToString() {
        Tag tag = Tag.valueOf("span");
        assertEquals("span", tag.toString());
    }

    @Test
    public void testSpecificTagsProperties() {
        // Test a self-closing tag like 'img' or 'br'
        Tag img = Tag.valueOf("img");
        assertTrue(img.isSelfClosing());
        assertTrue(img.isInline());

        // Test whitespace preserved tag like 'pre' or 'textarea'
        Tag pre = Tag.valueOf("pre");
        assertTrue(pre.isPreservedWhitespace());

        // Test table related tags
        Tag table = Tag.valueOf("table");
        assertTrue(table.formatAsBlock());

        Tag tr = Tag.valueOf("tr");
        assertTrue(tr.isBlock());
    }
}