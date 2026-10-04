package org.jsoup.parser;

import org.junit.Before;
import org.junit.Test;
import java.io.StringReader;
import java.io.Reader;
import static org.junit.Assert.*;

public class TokeniserTest {

    private Tokeniser tokeniser;

    @Before
    public void setUp() {
        // Initialize with empty input by default; each test will set its own input
        tokeniser = new Tokeniser(new StringReader(""), "");
    }

    private void setInput(String input) {
        tokeniser = new Tokeniser(new StringReader(input), "");
    }

    @Test
    public void testEmptyInput() {
        setInput("");
        Token token = tokeniser.read();
        assertEquals(Token.TokenType.EOF, token.type());
    }

    @Test
    public void testSimpleText() {
        setInput("Hello World");
        Token token = tokeniser.read();
        assertEquals(Token.TokenType.Character, token.type());
        assertEquals("Hello World", ((Token.Character) token).getData());
        token = tokeniser.read();
        assertEquals(Token.TokenType.EOF, token.type());
    }

    @Test
    public void testOpenTag() {
        setInput("<div>");
        Token token = tokeniser.read();
        assertEquals(Token.TokenType.StartTag, token.type());
        assertEquals("div", ((Token.Tag) token).name());
        token = tokeniser.read();
        assertEquals(Token.TokenType.EOF, token.type());
    }

    @Test
    public void testCloseTag() {
        setInput("</div>");
        Token token = tokeniser.read();
        assertEquals(Token.TokenType.EndTag, token.type());
        assertEquals("div", ((Token.Tag) token).name());
        token = tokeniser.read();
        assertEquals(Token.TokenType.EOF, token.type());
    }

    @Test
    public void testTagWithAttribute() {
        setInput("<a href=\"http://example.com\">");
        Token token = tokeniser.read();
        assertEquals(Token.TokenType.StartTag, token.type());
        Token.Tag tag = (Token.Tag) token;
        assertEquals("a", tag.name());
        assertEquals(1, tag.attributes().size());
        assertEquals("href", tag.attributes().get(0).getKey());
        assertEquals("http://example.com", tag.attributes().get(0).getValue());
        token = tokeniser.read();
        assertEquals(Token.TokenType.EOF, token.type());
    }

    @Test
    public void testNamedEntity() {
        setInput("&amp;");
        Token token = tokeniser.read();
        assertEquals(Token.TokenType.Character, token.type());
        // The entity should be decoded to '&'
        assertEquals("&", ((Token.Character) token).getData());
        token = tokeniser.read();
        assertEquals(Token.TokenType.EOF, token.type());
    }

    @Test
    public void testIncompleteNamedEntity() {
        // Bug 14: incomplete entity without semicolon should be treated as text
        setInput("&amp");
        Token token = tokeniser.read();
        assertEquals(Token.TokenType.Character, token.type());
        assertEquals("&amp", ((Token.Character) token).getData());
        token = tokeniser.read();
        assertEquals(Token.TokenType.EOF, token.type());
    }

    @Test
    public void testNumericEntityDecimal() {
        setInput("&#65;");
        Token token = tokeniser.read();
        assertEquals(Token.TokenType.Character, token.type());
        assertEquals("A", ((Token.Character) token).getData());
        token = tokeniser.read();
        assertEquals(Token.TokenType.EOF, token.type());
    }

    @Test
    public void testNumericEntityHex() {
        setInput("&#x41;");
        Token token = tokeniser.read();
        assertEquals(Token.TokenType.Character, token.type());
        assertEquals("A", ((Token.Character) token).getData());
        token = tokeniser.read();
        assertEquals(Token.TokenType.EOF, token.type());
    }

    @Test
    public void testAmpersandAlone() {
        setInput("&");
        Token token = tokeniser.read();
        assertEquals(Token.TokenType.Character, token.type());
        assertEquals("&", ((Token.Character) token).getData());
        token = tokeniser.read();
        assertEquals(Token.TokenType.EOF, token.type());
    }

    @Test
    public void testAmpersandAtEndOfInput() {
        setInput("text&");
        Token token = tokeniser.read();
        assertEquals(Token.TokenType.Character, token.type());
        assertEquals("text&", ((Token.Character) token).getData());
        token = tokeniser.read();
        assertEquals(Token.TokenType.EOF, token.type());
    }

    @Test
    public void testComment() {
        setInput("<!-- comment -->");
        Token token = tokeniser.read();
        assertEquals(Token.TokenType.Comment, token.type());
        assertEquals(" comment ", ((Token.Comment) token).getData());
        token = tokeniser.read();
        assertEquals(Token.TokenType.EOF, token.type());
    }

    @Test
    public void testDoctype() {
        setInput("<!DOCTYPE html>");
        Token token = tokeniser.read();
        assertEquals(Token.TokenType.Doctype, token.type());
        assertEquals("html", ((Token.Doctype) token).getName());
        token = tokeniser.read();
        assertEquals(Token.TokenType.EOF, token.type());
    }

    @Test
    public void testCDATA() {
        setInput("<![CDATA[ content ]]>");
        Token token = tokeniser.read();
        assertEquals(Token.TokenType.Character, token.type());
        assertEquals(" content ", ((Token.Character) token).getData());
        token = tokeniser.read();
        assertEquals(Token.TokenType.EOF, token.type());
    }

    @Test
    public void testMultipleTokens() {
        setInput("<p>text</p>");
        Token token = tokeniser.read();
        assertEquals(Token.TokenType.StartTag, token.type());
        assertEquals("p", ((Token.Tag) token).name());
        token = tokeniser.read();
        assertEquals(Token.TokenType.Character, token.type());
        assertEquals("text", ((Token.Character) token).getData());
        token = tokeniser.read();
        assertEquals(Token.TokenType.EndTag, token.type());
        assertEquals("p", ((Token.Tag) token).name());
        token = tokeniser.read();
        assertEquals(Token.TokenType.EOF, token.type());
    }

    @Test(expected = NullPointerException.class)
    public void testNullReader() {
        new Tokeniser(null, "");
    }

    @Test
    public void testLargeInput() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("a");
        }
        setInput(sb.toString());
        Token token = tokeniser.read();
        assertEquals(Token.TokenType.Character, token.type());
        assertEquals(sb.toString(), ((Token.Character) token).getData());
        token = tokeniser.read();
        assertEquals(Token.TokenType.EOF, token.type());
    }

    @Test
    public void testEntityAtEndOfInput() {
        setInput("&amp");
        Token token = tokeniser.read();
        assertEquals(Token.TokenType.Character, token.type());
        assertEquals("&amp", ((Token.Character) token).getData());
        token = tokeniser.read();
        assertEquals(Token.TokenType.EOF, token.type());
    }

    @Test
    public void testMultipleEntities() {
        setInput("&amp;&lt;&gt;");
        Token token = tokeniser.read();
        assertEquals(Token.TokenType.Character, token.type());
        assertEquals("&", ((Token.Character) token).getData());
        token = tokeniser.read();
        assertEquals(Token.TokenType.Character, token.type());
        assertEquals("<", ((Token.Character) token).getData());
        token = tokeniser.read();
        assertEquals(Token.TokenType.Character, token.type());
        assertEquals(">", ((Token.Character) token).getData());
        token = tokeniser.read();
        assertEquals(Token.TokenType.EOF, token.type());
    }

    @Test
    public void testIncompleteNumericEntity() {
        setInput("&#65");
        Token token = tokeniser.read();
        assertEquals(Token.TokenType.Character, token.type());
        assertEquals("&#65", ((Token.Character) token).getData());
        token = tokeniser.read();
        assertEquals(Token.TokenType.EOF, token.type());
    }

    @Test
    public void testIncompleteHexEntity() {
        setInput("&#x41");
        Token token = tokeniser.read();
        assertEquals(Token.TokenType.Character, token.type());
        assertEquals("&#x41", ((Token.Character) token).getData());
        token = tokeniser.read();
        assertEquals(Token.TokenType.EOF, token.type());
    }

    @Test
    public void testEntityWithSemicolonOnly() {
        setInput("&;");
        Token token = tokeniser.read();
        assertEquals(Token.TokenType.Character, token.type());
        assertEquals("&;", ((Token.Character) token).getData());
        token = tokeniser.read();
        assertEquals(Token.TokenType.EOF, token.type());
    }

    @Test
    public void testCommentWithDashes() {
        setInput("<!--- comment --->");
        Token token = tokeniser.read();
        assertEquals(Token.TokenType.Comment, token.type());
        assertEquals("- comment --", ((Token.Comment) token).getData());
        token = tokeniser.read();
        assertEquals(Token.TokenType.EOF, token.type());
    }

    @Test
    public void testDoctypeWithPublicIdentifier() {
        setInput("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\">");
        Token token = tokeniser.read();
        assertEquals(Token.TokenType.Doctype, token.type());
        assertEquals("html", ((Token.Doctype) token).getName());
        token = tokeniser.read();
        assertEquals(Token.TokenType.EOF, token.type());
    }

    @Test
    public void testSelfClosingTag() {
        setInput("<br/>");
        Token token = tokeniser.read();
        assertEquals(Token.TokenType.StartTag, token.type());
        Token.Tag tag = (Token.Tag) token;
        assertEquals("br", tag.name());
        assertTrue(tag.isSelfClosing());
        token = tokeniser.read();
        assertEquals(Token.TokenType.EOF, token.type());
    }

    @Test
    public void testAttributeWithNoValue() {
        setInput("<input disabled>");
        Token token = tokeniser.read();
        assertEquals(Token.TokenType.StartTag, token.type());
        Token.Tag tag = (Token.Tag) token;
        assertEquals("input", tag.name());
        assertEquals(1, tag.attributes().size());
        assertEquals("disabled", tag.attributes().get(0).getKey());
        assertEquals("", tag.attributes().get(0).getValue());
        token = tokeniser.read();
        assertEquals(Token.TokenType.EOF, token.type());
    }

    @Test
    public void testTextWithEntitiesAndTags() {
        setInput("a &amp; b < c > d");
        Token token = tokeniser.read();
        assertEquals(Token.TokenType.Character, token.type());
        assertEquals("a & b ", ((Token.Character) token).getData());
        token = tokeniser.read();
        assertEquals(Token.TokenType.StartTag, token.type());
        assertEquals("c", ((Token.Tag) token).name());
        token = tokeniser.read();
        assertEquals(Token.TokenType.EndTag, token.type());
        assertEquals("c", ((Token.Tag) token).name());
        token = tokeniser.read();
        assertEquals(Token.TokenType.Character, token.type());
        assertEquals(" d", ((Token.Character) token).getData());
        token = tokeniser.read();
        assertEquals(Token.TokenType.EOF, token.type());
    }
}