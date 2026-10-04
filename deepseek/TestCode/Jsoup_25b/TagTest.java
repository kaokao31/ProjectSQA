package org.jsoup.parser;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for the Tag class (Jsoup Bug 25 context).
 * Designed to achieve high coverage and detect case-sensitivity and caching bugs.
 */
public class TagTest {

    @Test
    public void testValueOfKnownTagReturnsCachedInstance() {
        Tag tag1 = Tag.valueOf("div");
        Tag tag2 = Tag.valueOf("div");
        assertSame("Same known tag should return cached instance", tag1, tag2);
    }

    @Test
    public void testValueOfKnownTagCaseInsensitiveReturnsSameInstance() {
        Tag lower = Tag.valueOf("div");
        Tag upper = Tag.valueOf("DIV");
        assertSame("Known tag should be case-insensitive and return same instance", lower, upper);
    }

    @Test
    public void testValueOfMixedCaseKnownTag() {
        Tag mixed = Tag.valueOf("DiV");
        Tag lower = Tag.valueOf("div");
        assertSame("Mixed case known tag should map to same instance", lower, mixed);
    }

    @Test
    public void testValueOfUnknownTagReturnsNewInstanceEachTime() {
        Tag tag1 = Tag.valueOf("custom");
        Tag tag2 = Tag.valueOf("custom");
        assertNotSame("Unknown tags should not be cached", tag1, tag2);
    }

    @Test
    public void testIsKnownTagForKnownTag() {
        assertTrue("div should be known", Tag.valueOf("div").isKnownTag());
    }

    @Test
    public void testIsKnownTagForKnownTagUpperCase() {
        assertTrue("DIV should be known", Tag.valueOf("DIV").isKnownTag());
    }

    @Test
    public void testIsKnownTagForUnknownTag() {
        assertFalse("custom should not be known", Tag.valueOf("custom").isKnownTag());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOfNullThrowsException() {
        Tag.valueOf(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOfEmptyStringThrowsException() {
        Tag.valueOf("");
    }

    @Test
    public void testDivIsBlock() {
        assertTrue("div should be block", Tag.valueOf("div").isBlock());
    }

    @Test
    public void testSpanIsInline() {
        assertFalse("span should not be block", Tag.valueOf("span").isBlock());
        assertTrue("span should be inline", Tag.valueOf("span").isInline());
    }

    @Test
    public void testBrIsSelfClosing() {
        assertTrue("br should be self-closing", Tag.valueOf("br").isSelfClosing());
    }

    @Test
    public void testImgIsSelfClosing() {
        assertTrue("img should be self-closing", Tag.valueOf("img").isSelfClosing());
    }

    @Test
    public void testDivIsNotSelfClosing() {
        assertFalse("div should not be self-closing", Tag.valueOf("div").isSelfClosing());
    }

    @Test
    public void testDivCanContainBlock() {
        assertTrue("div can contain block", Tag.valueOf("div").canContainBlock());
    }

    @Test
    public void testPCannotContainBlock() {
        assertFalse("p cannot contain block", Tag.valueOf("p").canContainBlock());
    }

    @Test
    public void testScriptIsData() {
        assertTrue("script should be data", Tag.valueOf("script").isData());
    }

    @Test
    public void testDivIsNotData() {
        assertFalse("div should not be data", Tag.valueOf("div").isData());
    }

    @Test
    public void testOptionIsFormSubmittable() {
        assertTrue("option should be form submittable", Tag.valueOf("option").isFormSubmittable());
    }

    @Test
    public void testDivIsNotFormSubmittable() {
        assertFalse("div should not be form submittable", Tag.valueOf("div").isFormSubmittable());
    }

    @Test
    public void testInputIsFormListed() {
        assertTrue("input should be form listed", Tag.valueOf("input").isFormListed());
    }

    @Test
    public void testDivIsNotFormListed() {
        assertFalse("div should not be form listed", Tag.valueOf("div").isFormListed());
    }

    @Test
    public void testFormatAsBlockForBlockTag() {
        assertTrue("div should format as block", Tag.valueOf("div").formatAsBlock());
    }

    @Test
    public void testFormatAsBlockForInlineTag() {
        assertFalse("span should not format as block", Tag.valueOf("span").formatAsBlock());
    }

    @Test
    public void testIsEmptyForVoidTags() {
        assertTrue("br should be empty", Tag.valueOf("br").isEmpty());
        assertTrue("img should be empty", Tag.valueOf("img").isEmpty());
        assertTrue("input should be empty", Tag.valueOf("input").isEmpty());
    }

    @Test
    public void testIsEmptyForNonVoidTags() {
        assertFalse("div should not be empty", Tag.valueOf("div").isEmpty());
        assertFalse("span should not be empty", Tag.valueOf("span").isEmpty());
    }

    @Test
    public void testValueOfReturnsCorrectTagName() {
        assertEquals("div", Tag.valueOf("div").getName());
        assertEquals("span", Tag.valueOf("span").getName());
        assertEquals("custom", Tag.valueOf("custom").getName());
    }

    @Test
    public void testValueOfPreservesOriginalCaseForUnknownTags() {
        Tag tag = Tag.valueOf("MyCustomTag");
        assertEquals("MyCustomTag", tag.getName());
    }

    @Test
    public void testKnownTagNameIsLowercase() {
        assertEquals("div", Tag.valueOf("DIV").getName());
        assertEquals("span", Tag.valueOf("SPAN").getName());
    }

    @Test
    public void testMultipleKnownTagsAreDistinct() {
        assertNotSame("div and span should be different instances", Tag.valueOf("div"), Tag.valueOf("span"));
    }

    @Test
    public void testUnknownTagIsNotBlock() {
        assertFalse("unknown tag should not be block", Tag.valueOf("unknown").isBlock());
    }

    @Test
    public void testUnknownTagIsNotSelfClosing() {
        assertFalse("unknown tag should not be self-closing", Tag.valueOf("unknown").isSelfClosing());
    }

    @Test
    public void testUnknownTagIsNotData() {
        assertFalse("unknown tag should not be data", Tag.valueOf("unknown").isData());
    }

    @Test
    public void testUnknownTagCanContainBlock() {
        assertTrue("unknown tag can contain block by default", Tag.valueOf("unknown").canContainBlock());
    }

    @Test
    public void testUnknownTagFormatAsBlock() {
        assertTrue("unknown tag should format as block by default", Tag.valueOf("unknown").formatAsBlock());
    }

    @Test
    public void testUnknownTagIsEmpty() {
        assertFalse("unknown tag should not be empty", Tag.valueOf("unknown").isEmpty());
    }

    @Test
    public void testUnknownTagIsInline() {
        assertFalse("unknown tag should not be inline", Tag.valueOf("unknown").isInline());
    }

    @Test
    public void testUnknownTagIsFormListed() {
        assertFalse("unknown tag should not be form listed", Tag.valueOf("unknown").isFormListed());
    }

    @Test
    public void testUnknownTagIsFormSubmittable() {
        assertFalse("unknown tag should not be form submittable", Tag.valueOf("unknown").isFormSubmittable());
    }

    @Test
    public void testEqualsAndHashCodeForKnownTags() {
        Tag tag1 = Tag.valueOf("div");
        Tag tag2 = Tag.valueOf("div");
        assertEquals("Known tags with same name should be equal", tag1, tag2);
        assertEquals("Hash codes should match", tag1.hashCode(), tag2.hashCode());
    }

    @Test
    public void testEqualsForUnknownTags() {
        Tag tag1 = Tag.valueOf("custom");
        Tag tag2 = Tag.valueOf("custom");
        // Unknown tags are not cached, but they should be equal based on name
        assertEquals("Unknown tags with same name should be equal", tag1, tag2);
        assertEquals("Hash codes should match", tag1.hashCode(), tag2.hashCode());
    }

    @Test
    public void testNotEqualsForDifferentTags() {
        assertNotEquals("div and span should not be equal", Tag.valueOf("div"), Tag.valueOf("span"));
    }

    @Test
    public void testToString() {
        assertEquals("div", Tag.valueOf("div").toString());
        assertEquals("custom", Tag.valueOf("custom").toString());
    }
}