package org.jsoup.nodes;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for the Comment class.
 * Designed to achieve maximum code coverage and fault detection.
 */
public class CommentTest {

    private Comment comment;

    @Before
    public void setUp() {
        comment = new Comment("test comment");
    }

    // --- Constructor and basic getData ---

    @Test
    public void testConstructorAndGetData() {
        assertEquals("test comment", comment.getData());
    }

    @Test
    public void testEmptyComment() {
        Comment empty = new Comment("");
        assertEquals("", empty.getData());
    }

    @Test(expected = NullPointerException.class)
    public void testNullDataInConstructor() {
        new Comment(null);
    }

    // --- setData ---

    @Test
    public void testSetData() {
        comment.setData("new data");
        assertEquals("new data", comment.getData());
    }

    @Test
    public void testSetDataEmpty() {
        comment.setData("");
        assertEquals("", comment.getData());
    }

    @Test(expected = NullPointerException.class)
    public void testSetDataNull() {
        comment.setData(null);
    }

    // --- toString ---

    @Test
    public void testToString() {
        assertEquals("<!--test comment-->", comment.toString());
    }

    @Test
    public void testToStringEmpty() {
        Comment empty = new Comment("");
        assertEquals("<!---->", empty.toString());
    }

    @Test
    public void testToStringWithSpecialChars() {
        Comment special = new Comment("a < b & c > d");
        assertEquals("<!--a < b & c > d-->", special.toString());
    }

    @Test
    public void testToStringWithDoubleDash() {
        Comment dd = new Comment("a -- b");
        assertEquals("<!--a -- b-->", dd.toString());
    }

    // --- outerHtml (inherited from Node, uses outerHtmlHead/outerHtmlTail) ---

    @Test
    public void testOuterHtml() {
        assertEquals("<!--test comment-->", comment.outerHtml());
    }

    @Test
    public void testOuterHtmlEmpty() {
        Comment empty = new Comment("");
        assertEquals("<!---->", empty.outerHtml());
    }

    // --- isXmlDeclaration ---

    @Test
    public void testIsXmlDeclarationTrue() {
        Comment xmlDecl = new Comment("<?xml version=\"1.0\"?>");
        assertTrue(xmlDecl.isXmlDeclaration());
    }

    @Test
    public void testIsXmlDeclarationFalse() {
        assertFalse(comment.isXmlDeclaration());
    }

    @Test
    public void testIsXmlDeclarationWithLeadingWhitespace() {
        Comment ws = new Comment("  <?xml version=\"1.0\"?>");
        assertFalse(ws.isXmlDeclaration());
    }

    @Test
    public void testIsXmlDeclarationCaseSensitive() {
        Comment lower = new Comment("<?xml version=\"1.0\"?>");
        assertTrue(lower.isXmlDeclaration());
        Comment upper = new Comment("<?XML version=\"1.0\"?>");
        assertFalse(upper.isXmlDeclaration());
    }

    @Test
    public void testIsXmlDeclarationEmpty() {
        Comment empty = new Comment("");
        assertFalse(empty.isXmlDeclaration());
    }

    // --- clone ---

    @Test
    public void testClone() {
        Comment cloned = comment.clone();
        assertNotNull(cloned);
        assertNotSame(comment, cloned);
        assertEquals(comment.getData(), cloned.getData());
        assertTrue(cloned instanceof Comment);
    }

    @Test
    public void testCloneIndependence() {
        Comment cloned = comment.clone();
        cloned.setData("modified");
        assertEquals("test comment", comment.getData());
        assertEquals("modified", cloned.getData());
    }

    @Test
    public void testCloneEmpty() {
        Comment empty = new Comment("");
        Comment cloned = empty.clone();
        assertEquals("", cloned.getData());
    }

    // --- Additional edge cases ---

    @Test
    public void testCommentWithOnlyWhitespace() {
        Comment ws = new Comment("   ");
        assertEquals("   ", ws.getData());
        assertEquals("<!--   -->", ws.toString());
    }

    @Test
    public void testCommentWithNewlines() {
        Comment nl = new Comment("line1\nline2");
        assertEquals("line1\nline2", nl.getData());
        assertEquals("<!--line1\nline2-->", nl.toString());
    }

    @Test
    public void testCommentWithHtmlTags() {
        Comment tags = new Comment("<div>");
        assertEquals("<div>", tags.getData());
        assertEquals("<!--<div>-->", tags.toString());
    }

    @Test
    public void testCommentWithAmpersand() {
        Comment amp = new Comment("&amp;");
        assertEquals("&amp;", amp.getData());
        assertEquals("<!--&amp;-->", amp.toString());
    }

    // --- isXmlDeclaration with various patterns ---

    @Test
    public void testIsXmlDeclarationWithQuestionMark() {
        Comment q = new Comment("<?something?>");
        assertFalse(q.isXmlDeclaration());
    }

    @Test
    public void testIsXmlDeclarationWithXmlPi() {
        Comment pi = new Comment("<?xml-stylesheet ...?>");
        assertFalse(pi.isXmlDeclaration());
    }

    @Test
    public void testIsXmlDeclarationExact() {
        Comment exact = new Comment("<?xml?>");
        assertTrue(exact.isXmlDeclaration());
    }

    @Test
    public void testIsXmlDeclarationWithAttributes() {
        Comment attr = new Comment("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        assertTrue(attr.isXmlDeclaration());
    }
}