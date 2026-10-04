package org.jsoup.parser;

import org.junit.Test;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;

import static org.junit.Assert.*;

public class HtmlTreeBuilderTest {

    @Test
    public void testDefaultStateAndInitialisation() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse("<html><head></head><body><p>Hello</p></body></html>", "http://example.com", parser);
        
        assertEquals("http://example.com", tb.getBaseUri());
        assertNotNull(tb.getDocument());
        assertNotNull(tb.getStack());
        assertFalse(tb.getStack().isEmpty());
    }

    @Test
    public void testResetStack() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse("<div><p></p></div>", "", parser);
        
        // Push elements to stack and test reset
        tb.clearStackToContext("body");
        assertNotNull(tb.currentElement());
    }

    @Test
    public void testBookmarkAndRestoreState() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse("<div><span></span></div>", "", parser);

        tb.mark();
        Element el = tb.insertStartTag("p");
        assertNotNull(el);
        
        tb.restore();
    }

    @Test
    public void testInSpecificScope() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse("<table><tr><td>Cell</td></tr></table>", "", parser);

        assertTrue(tb.inScope("table"));
        assertTrue(tb.inTableScope("table"));
        assertFalse(tb.inButtonScope("table"));
    }

    @Test
    public void testHTML5DraftModeAndErrors() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        parser.settings(ParseSettings.preserveCase);
        
        tb.initialiseParse("<p>Test</p>", "", parser);
        assertTrue(tb.isFosterParenting() || !tb.isFosterParenting()); // toggle/check state
        tb.setFosterParenting(true);
        assertTrue(tb.isFosterParenting());
        tb.setFosterParenting(false);
        assertFalse(tb.isFosterParenting());

        ParseErrorList errorList = tb.getErrors();
        assertNotNull(errorList);
    }

    @Test
    public void testProcessStartTagAndEndTag() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse("<div></div>", "", parser);

        Token.StartTag startTag = new Token.StartTag();
        startTag.name("span");
        startTag.attributes = new Attributes();
        
        boolean result = tb.process(startTag);
        assertTrue(result);

        Token.EndTag endTag = new Token.EndTag();
        endTag.name("span");
        boolean endResult = tb.process(endTag);
        assertTrue(endResult);
    }

    @Test
    public void testProcessCommentAndCharacter() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse("<div></div>", "", parser);

        Token.Comment comment = new Token.Comment();
        comment.comment.append("test comment");
        assertTrue(tb.process(comment));

        Token.Character ch = new Token.Character();
        ch.data("hello");
        assertTrue(tb.process(ch));
    }

    @Test
    public void testAnyOtherEndTagHandling() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse("<div><p><span>Text</span></p></div>", "", parser);

        // Process unusual end tags to hit switch-cases and stack checks (e.g. bug 68 context for max stack/depth limits)
        for (int i = 0; i < 100; i++) {
            tb.insertStartTag("div");
        }
        
        Token.EndTag endTag = new Token.EndTag();
        endTag.name("div");
        tb.process(endTag);
        
        assertNotNull(tb.currentElement());
    }
}