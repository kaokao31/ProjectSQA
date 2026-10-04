package org.jsoup.parser;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for the Token class and its subclasses.
 * Designed to achieve high code coverage and detect potential faults.
 */
public class TokenTest {

    private Token.Tag tag;
    private Token.StartTag startTag;
    private Token.EndTag endTag;
    private Token.Comment comment;
    private Token.Character character;
    private Token.EOF eof;
    private Token.Doctype doctype;

    @Before
    public void setUp() {
        tag = new Token.Tag();
        startTag = new Token.StartTag();
        endTag = new Token.EndTag();
        comment = new Token.Comment();
        character = new Token.Character();
        eof = new Token.EOF();
        doctype = new Token.Doctype();
    }

    // --- Token base class tests ---
    @Test
    public void testTokenType() {
        assertTrue(startTag instanceof Token);
        assertTrue(endTag instanceof Token);
        assertTrue(comment instanceof Token);
        assertTrue(character instanceof Token);
        assertTrue(eof instanceof Token);
        assertTrue(doctype instanceof Token);
    }

    // --- Tag subclass tests ---
    @Test
    public void testTagInitialState() {
        assertFalse(tag.isSelfClosing());
        assertNull(tag.name());
        assertTrue(tag.attributes.isEmpty());
    }

    @Test
    public void testTagName() {
        tag.name("div");
        assertEquals("div", tag.name());
        tag.name("span");
        assertEquals("span", tag.name());
    }

    @Test
    public void testTagNameNull() {
        tag.name(null);
        assertNull(tag.name());
    }

    @Test
    public void testTagNameEmpty() {
        tag.name("");
        assertEquals("", tag.name());
    }

    @Test
    public void testTagSelfClosing() {
        assertFalse(tag.isSelfClosing());
        tag.selfClosing(true);
        assertTrue(tag.isSelfClosing());
        tag.selfClosing(false);
        assertFalse(tag.isSelfClosing());
    }

    @Test
    public void testTagAttributes() {
        assertTrue(tag.attributes.isEmpty());
        tag.attributes.put("class", "test");
        assertEquals(1, tag.attributes.size());
        assertEquals("test", tag.attributes.get("class"));
    }

    @Test
    public void testTagReset() {
        tag.name("div");
        tag.selfClosing(true);
        tag.attributes.put("id", "main");
        tag.reset();
        assertNull(tag.name());
        assertFalse(tag.isSelfClosing());
        assertTrue(tag.attributes.isEmpty());
    }

    // --- StartTag subclass tests ---
    @Test
    public void testStartTagInitialState() {
        assertTrue(startTag.isStartTag());
        assertFalse(startTag.isEndTag());
        assertNull(startTag.name());
    }

    @Test
    public void testStartTagName() {
        startTag.name("a");
        assertEquals("a", startTag.name());
    }

    @Test
    public void testStartTagSelfClosing() {
        assertFalse(startTag.isSelfClosing());
        startTag.selfClosing(true);
        assertTrue(startTag.isSelfClosing());
    }

    @Test
    public void testStartTagAttributes() {
        startTag.attributes.put("href", "http://example.com");
        assertEquals(1, startTag.attributes.size());
    }

    @Test
    public void testStartTagReset() {
        startTag.name("img");
        startTag.selfClosing(true);
        startTag.attributes.put("src", "pic.jpg");
        startTag.reset();
        assertNull(startTag.name());
        assertFalse(startTag.isSelfClosing());
        assertTrue(startTag.attributes.isEmpty());
    }

    // --- EndTag subclass tests ---
    @Test
    public void testEndTagInitialState() {
        assertFalse(endTag.isStartTag());
        assertTrue(endTag.isEndTag());
        assertNull(endTag.name());
    }

    @Test
    public void testEndTagName() {
        endTag.name("div");
        assertEquals("div", endTag.name());
    }

    @Test
    public void testEndTagSelfClosing() {
        assertFalse(endTag.isSelfClosing());
        endTag.selfClosing(true);
        assertTrue(endTag.isSelfClosing());
    }

    @Test
    public void testEndTagReset() {
        endTag.name("p");
        endTag.selfClosing(true);
        endTag.reset();
        assertNull(endTag.name());
        assertFalse(endTag.isSelfClosing());
    }

    // --- Comment subclass tests ---
    @Test
    public void testCommentInitialState() {
        assertTrue(comment.isComment());
        assertEquals("", comment.getData());
    }

    @Test
    public void testCommentData() {
        comment.data("<!-- test -->");
        assertEquals("<!-- test -->", comment.getData());
    }

    @Test
    public void testCommentDataNull() {
        comment.data(null);
        assertNull(comment.getData());
    }

    @Test
    public void testCommentDataEmpty() {
        comment.data("");
        assertEquals("", comment.getData());
    }

    @Test
    public void testCommentReset() {
        comment.data("some comment");
        comment.reset();
        assertEquals("", comment.getData());
    }

    // --- Character subclass tests ---
    @Test
    public void testCharacterInitialState() {
        assertTrue(character.isCharacter());
        assertEquals("", character.getData());
    }

    @Test
    public void testCharacterData() {
        character.data("text content");
        assertEquals("text content", character.getData());
    }

    @Test
    public void testCharacterDataNull() {
        character.data(null);
        assertNull(character.getData());
    }

    @Test
    public void testCharacterDataEmpty() {
        character.data("");
        assertEquals("", character.getData());
    }

    @Test
    public void testCharacterReset() {
        character.data("some text");
        character.reset();
        assertEquals("", character.getData());
    }

    // --- EOF subclass tests ---
    @Test
    public void testEOFInitialState() {
        assertTrue(eof.isEOF());
    }

    @Test
    public void testEOFReset() {
        eof.reset();
        assertTrue(eof.isEOF());
    }

    // --- Doctype subclass tests ---
    @Test
    public void testDoctypeInitialState() {
        assertTrue(doctype.isDoctype());
        assertNull(doctype.getName());
        assertNull(doctype.getPublicIdentifier());
        assertNull(doctype.getSystemIdentifier());
    }

    @Test
    public void testDoctypeName() {
        doctype.name("html");
        assertEquals("html", doctype.getName());
    }

    @Test
    public void testDoctypePublicIdentifier() {
        doctype.pubIdentifier("-//W3C//DTD XHTML 1.0 Strict//EN");
        assertEquals("-//W3C//DTD XHTML 1.0 Strict//EN", doctype.getPublicIdentifier());
    }

    @Test
    public void testDoctypeSystemIdentifier() {
        doctype.sysIdentifier("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd");
        assertEquals("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd", doctype.getSystemIdentifier());
    }

    @Test
    public void testDoctypeReset() {
        doctype.name("html");
        doctype.pubIdentifier("public");
        doctype.sysIdentifier("system");
        doctype.reset();
        assertNull(doctype.getName());
        assertNull(doctype.getPublicIdentifier());
        assertNull(doctype.getSystemIdentifier());
    }

    // --- Edge cases and potential bug triggers ---
    @Test
    public void testTagNameWithSpecialCharacters() {
        tag.name("div#id.class");
        assertEquals("div#id.class", tag.name());
    }

    @Test
    public void testStartTagNameWithUppercase() {
        startTag.name("DIV");
        assertEquals("DIV", startTag.name());
    }

    @Test
    public void testEndTagNameWithUppercase() {
        endTag.name("SPAN");
        assertEquals("SPAN", endTag.name());
    }

    @Test
    public void testMultipleAttributes() {
        startTag.attributes.put("class", "main");
        startTag.attributes.put("id", "content");
        startTag.attributes.put("style", "color:red");
        assertEquals(3, startTag.attributes.size());
    }

    @Test
    public void testAttributeWithEmptyKey() {
        startTag.attributes.put("", "value");
        assertEquals(1, startTag.attributes.size());
        assertEquals("value", startTag.attributes.get(""));
    }

    @Test
    public void testAttributeWithEmptyValue() {
        startTag.attributes.put("key", "");
        assertEquals(1, startTag.attributes.size());
        assertEquals("", startTag.attributes.get("key"));
    }

    @Test
    public void testAttributeWithNullKey() {
        // Assuming attributes is a LinkedHashMap, null key might be allowed
        startTag.attributes.put(null, "value");
        assertEquals(1, startTag.attributes.size());
        assertEquals("value", startTag.attributes.get(null));
    }

    @Test
    public void testAttributeWithNullValue() {
        startTag.attributes.put("key", null);
        assertEquals(1, startTag.attributes.size());
        assertNull(startTag.attributes.get("key"));
    }

    @Test
    public void testCommentWithSpecialCharacters() {
        comment.data("<!-- <test> & more -->");
        assertEquals("<!-- <test> & more -->", comment.getData());
    }

    @Test
    public void testCharacterWithSpecialCharacters() {
        character.data("&amp; < > \" '");
        assertEquals("&amp; < > \" '", character.getData());
    }

    @Test
    public void testDoctypeWithNullIdentifiers() {
        doctype.name("html");
        assertNull(doctype.getPublicIdentifier());
        assertNull(doctype.getSystemIdentifier());
    }

    @Test
    public void testDoctypeWithEmptyIdentifiers() {
        doctype.name("html");
        doctype.pubIdentifier("");
        doctype.sysIdentifier("");
        assertEquals("", doctype.getPublicIdentifier());
        assertEquals("", doctype.getSystemIdentifier());
    }

    @Test
    public void testMultipleResets() {
        startTag.name("div");
        startTag.selfClosing(true);
        startTag.attributes.put("class", "test");
        startTag.reset();
        startTag.reset(); // second reset should be safe
        assertNull(startTag.name());
        assertFalse(startTag.isSelfClosing());
        assertTrue(startTag.attributes.isEmpty());
    }

    @Test
    public void testTagIsStartTagAndEndTag() {
        assertTrue(startTag.isStartTag());
        assertFalse(startTag.isEndTag());
        assertFalse(endTag.isStartTag());
        assertTrue(endTag.isEndTag());
    }

    @Test
    public void testTokenToString() {
        // Ensure toString does not throw exception
        assertNotNull(startTag.toString());
        assertNotNull(endTag.toString());
        assertNotNull(comment.toString());
        assertNotNull(character.toString());
        assertNotNull(eof.toString());
        assertNotNull(doctype.toString());
    }

    @Test
    public void testTokenHashCode() {
        // Ensure hashCode does not throw exception
        startTag.hashCode();
        endTag.hashCode();
        comment.hashCode();
        character.hashCode();
        eof.hashCode();
        doctype.hashCode();
    }

    @Test
    public void testTokenEquals() {
        // Basic equals checks
        assertFalse(startTag.equals(null));
        assertFalse(startTag.equals(new Object()));
        assertTrue(startTag.equals(startTag));
        // Different types should not be equal
        assertFalse(startTag.equals(endTag));
    }
}