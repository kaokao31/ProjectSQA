package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class TokenTest {

    @Test
    public void testDoctypeToken() {
        Token.Doctype t = new Token.Doctype();
        assertEquals("#doctype", t.tokenType());
        
        t.setName("html");
        assertEquals("html", t.getName());
        
        t.getPubSysKey().append("public");
        assertEquals("public", t.getPubSysKey().toString());
        
        t.setPublicIdentifier("pubId");
        assertEquals("pubId", t.getPublicIdentifier());
        
        t.setSystemIdentifier("sysId");
        assertEquals("sysId", t.getSystemIdentifier());
        
        assertFalse(t.isForceQuirks());
        t.forceQuirks(true);
        assertTrue(t.isForceQuirks());
        
        Token.Doctype reset = t.reset();
        assertNotNull(reset);
        assertEquals("", t.getName());
        assertEquals("", t.getPubSysKey().toString());
        assertNull(t.getPublicIdentifier());
        assertNull(t.getSystemIdentifier());
        assertFalse(t.isForceQuirks());
    }

    @Test
    public void testStartTagToken() {
        Token.StartTag t = new Token.StartTag();
        assertEquals("StartTag", t.tokenType());
        
        t.name("div");
        assertEquals("div", t.name());
        
        t.normalName("p");
        assertEquals("p", t.normalName);

        t.appendAttributeName("class");
        t.appendAttributeValue("container");
        t.appendAttributeValue(" active");
        
        t.finaliseTag();
        
        Token.StartTag reset = (Token.StartTag) t.reset();
        assertNotNull(reset);
        assertNull(t.name);
        assertNull(t.normalName);
    }

    @Test
    public void testEndTagToken() {
        Token.EndTag t = new Token.EndTag();
        assertEquals("EndTag", t.tokenType());
        
        t.name("span");
        assertEquals("span", t.name());
        
        Token.Tag reset = t.reset();
        assertNotNull(reset);
    }

    @Test
    public void testCommentToken() {
        Token.Comment t = new Token.Comment();
        assertEquals("Comment", t.tokenType());
        
        t.comment.append("some comment");
        assertEquals("some comment", t.getData());
        
        t.bogus = true;
        assertTrue(t.bogus);
        
        Token.Comment reset = (Token.Comment) t.reset();
        assertNotNull(reset);
        assertEquals("", t.getData());
    }

    @Test
    public void testCharacterToken() {
        Token.Character t = new Token.Character();
        assertEquals("Character", t.tokenType());
        
        t.data("abc");
        assertEquals("abc", t.getData());
        
        Token.Character reset = (Token.Character) t.reset();
        assertNotNull(reset);
        assertNull(t.getData());
    }

    @Test
    public void testEOFToken() {
        Token.EOF t = new Token.EOF();
        assertEquals("EOF", t.tokenType());
        
        Token.EOF reset = (Token.EOF) t.reset();
        assertNotNull(reset);
    }

    @Test
    public void testTagNewAttribute() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("a");
        tag.appendAttributeName("href");
        tag.appendAttributeValue("http://example.com");
        tag.finaliseTag();

        assertEquals(1, tag.attributes.size());
        assertEquals("http://example.com", tag.attributes.get("href"));
    }

    @Test
    public void testTagNewAttributeWithExistingName() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("a");
        tag.appendAttributeName("class");
        tag.appendAttributeValue("foo");
        // Append another attribute with the same name to exercise the branch where attribute already exists
        tag.appendAttributeName("class");
        tag.appendAttributeValue("bar");
        tag.finaliseTag();

        assertEquals(1, tag.attributes.size());
        assertEquals("foobar", tag.attributes.get("class"));
    }

    @Test
    public void testTagAttributeWithoutValue() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("input");
        tag.appendAttributeName("disabled");
        tag.setEmptyAttributeValue();
        tag.finaliseTag();

        assertEquals(1, tag.attributes.size());
        assertEquals("", tag.attributes.get("disabled"));
    }
}