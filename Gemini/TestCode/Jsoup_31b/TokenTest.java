package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class TokenTest {

    @Test
    public void testTokenType() {
        Token.Doctype d = new Token.Doctype();
        assertEquals(Token.TokenType.Doctype, d.tokenType());

        Token.StartTag s = new Token.StartTag();
        assertEquals(Token.TokenType.StartTag, s.tokenType());

        Token.EndTag e = new Token.EndTag();
        assertEquals(Token.TokenType.EndTag, e.tokenType());

        Token.Comment c = new Token.Comment();
        assertEquals(Token.TokenType.Comment, c.tokenType());

        Token.Character t = new Token.Character();
        assertEquals(Token.TokenType.Character, t.tokenType());

        Token.EOF eof = new Token.EOF();
        assertEquals(Token.TokenType.EOF, eof.tokenType());
    }

    @Test
    public void testDoctypeToken() {
        Token.Doctype d = new Token.Doctype();
        d.name.append("html");
        d.publicIdentifier.append("pubId");
        d.systemIdentifier.append("sysId");
        d.forceQuirks(true);

        assertEquals("html", d.getName());
        assertEquals("pubId", d.getPublicIdentifier());
        assertEquals("sysId", d.getSystemIdentifier());
        assertTrue(d.isForceQuirks());

        Token resetToken = d.reset();
        assertSame(d, resetToken);
        assertEquals("", d.getName());
        assertEquals("", d.getPublicIdentifier());
        assertEquals("", d.getSystemIdentifier());
        assertFalse(d.isForceQuirks());

        // Test null scenarios for Doctype builders
        Token.Doctype d2 = new Token.Doctype();
        d2.name.append((String) null);
        d2.publicIdentifier.append((String) null);
        d2.systemIdentifier.append((String) null);
        assertNull(d2.getName());
        assertNull(d2.getPublicIdentifier());
        assertNull(d2.getSystemIdentifier());
    }

    @Test
    public void testTagToken() {
        Token.StartTag start = new Token.StartTag();
        start.name("div");
        start.appendAttribute_("class", "container");
        start.appendAttribute_("id", "main");
        
        assertEquals("div", start.tagName());
        assertNotNull(start.attributes);
        assertEquals("container", start.attributes.get("class"));
        assertEquals("id", start.attributes.get("id"));

        Token resetStart = start.reset();
        assertSame(start, resetStart);
        assertEquals("", start.tagName());
        assertNull(start.attributes);

        // Test tag with attributes and pending attribute data
        Token.EndTag end = new Token.EndTag();
        end.name("span");
        assertEquals("span", end.tagName());

        // Cover newTag method
        Token.StartTag newStart = (Token.StartTag) start.name("a");
        assertEquals("a", newStart.tagName());
        
        start.newAttribute();
        start.appendAttributeName("data-test");
        start.appendAttributeValue("val");
        start.appendAttributeValue('s');
        char[] valChars = {'a', 'b'};
        start.appendAttributeValue(valChars);
        start.finaliseTag();
    }

    @Test
    public void testCommentToken() {
        Token.Comment comment = new Token.Comment();
        comment.comment.append("test comment");
        assertEquals("test comment", comment.getData());

        Token resetComment = comment.reset();
        assertSame(comment, resetComment);
        assertEquals("", comment.getData());

        Token.Comment commentNull = new Token.Comment();
        commentNull.comment.append((String) null);
        assertNull(commentNull.getData());
    }

    @Test
    public void testCharacterToken() {
        Token.Character character = new Token.Character();
        character.data("some data");
        assertEquals("some data", character.getData());

        Token resetChar = character.reset();
        assertSame(character, resetChar);
        assertNull(character.getData());
    }

    @Test
    public void testEOFToken() {
        Token.EOF eof = new Token.EOF();
        Token resetEof = eof.reset();
        assertSame(eof, resetEof);
    }

    @Test
    public void testIsMethods() {
        Token.Doctype doctype = new Token.Doctype();
        Token.StartTag startTag = new Token.StartTag();
        Token.EndTag endTag = new Token.EndTag();
        Token.Comment comment = new Token.Comment();
        Token.Character character = new Token.Character();
        Token.EOF eof = new Token.EOF();

        assertTrue(doctype.isDoctype());
        assertFalse(doctype.isStartTag());
        assertFalse(doctype.isEndTag());
        assertFalse(doctype.isComment());
        assertFalse(doctype.isCharacter());

        assertTrue(startTag.isStartTag());
        assertFalse(startTag.isDoctype());
        assertFalse(startTag.isEndTag());

        assertTrue(endTag.isEndTag());
        assertFalse(endTag.isStartTag());

        assertTrue(comment.isComment());
        assertFalse(comment.isCharacter());

        assertTrue(character.isCharacter());
        assertFalse(character.isEOF());

        assertTrue(eof.isEOF());
        assertFalse(eof.isStartTag());
    }

    @Test
    public void testAsMethods() {
        Token.Doctype doctype = new Token.Doctype();
        Token.StartTag startTag = new Token.StartTag();
        Token.EndTag endTag = new Token.EndTag();
        Token.Comment comment = new Token.Comment();
        Token.Character character = new Token.Character();

        assertSame(doctype, doctype.asDoctype());
        assertSame(startTag, startTag.asStartTag());
        assertSame(startTag, startTag.asTag());
        assertSame(endTag, endTag.asEndTag());
        assertSame(endTag, endTag.asTag());
        assertSame(comment, comment.asComment());
        assertSame(character, character.asCharacter());
    }

    @Test(expected = ClassCastException.class)
    public void testInvalidAsDoctype() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.asDoctype();
    }

    @Test(expected = ClassCastException.class)
    public void testInvalidAsStartTag() {
        Token.EndTag endTag = new Token.EndTag();
        endTag.asStartTag();
    }

    @Test(expected = ClassCastException.class)
    public void testInvalidAsEndTag() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.asEndTag();
    }

    @Test(expected = ClassCastException.class)
    public void testInvalidAsComment() {
        Token.Character character = new Token.Character();
        character.asComment();
    }

    @Test(expected = ClassCastException.class)
    public void testInvalidAsCharacter() {
        Token.Comment comment = new Token.Comment();
        comment.asCharacter();
    }

    @Test
    public void testTagAttributeHelpers() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("test");
        tag.newAttribute();
        tag.appendAttributeName("attr1");
        tag.appendAttributeName('2');
        tag.appendAttributeValue("val1");
        tag.appendAttributeValue('v');
        tag.appendAttributeValue(new char[]{'2'});
        
        tag.setAttrName("attrNew");
        
        // Finalise should place attributes into attributes map
        tag.finaliseTag();
        assertNotNull(tag.attributes);
        assertEquals("valv2", tag.attributes.get("attr12"));
    }
}