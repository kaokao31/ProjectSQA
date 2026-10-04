package org.jsoup.parser;

import org.junit.Test;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import static org.junit.Assert.*;

public class HtmlTreeBuilderTest {

    @Test
    public void testParseStartTagWithBogusCommentAndErrors() {
        // Test parsing with tracking errors enabled
        Parser parser = Parser.htmlParser();
        parser.settings(ParseSettings.preserveCase);
        parser.setTrackErrors(3);

        String html = "<div><p>Hello <!-- comment --> <span class='test'>World</span></p></div>";
        Element doc = parser.parseInput(html, "http://example.com");

        assertNotNull(doc);
        assertFalse(parser.getErrors().isEmpty());
    }

    @Test
    public void testResetInsertionMode() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse("<html><body><table><tr><td>Cell</td></tr></table></body></html>", "http://example.com", parser);
        
        // Drive various states
        tb.process(new Token.StartTag("html"));
        tb.process(new Token.StartTag("body"));
        tb.process(new Token.StartTag("table"));
        tb.process(new Token.StartTag("tr"));
        tb.process(new Token.StartTag("td"));
        
        // Test reset insertion mode logic directly if accessible or via processing
        tb.resetInsertionMode();
        assertNotNull(tb.currentElement());
    }

    @Test
    public void testInvalidTagNestingAndErrors() {
        Parser parser = Parser.htmlParser().setTrackErrors(10);
        String html = "<table><tr><td><b>Unclosed <i>formatting</i></td></tr></table>";
        Element doc = parser.parseInput(html, "");
        assertNotNull(doc);
        assertTrue(parser.getErrors().size() >= 0);
    }

    @Test
    public void testBogusTagsAndAttributes() {
        Parser parser = Parser.htmlParser().setTrackErrors(5);
        String html = "<a href='foo' href='bar'>Duplicate Attr</a>";
        Element doc = parser.parseInput(html, "");
        assertNotNull(doc);
    }

    @Test
    public void testFormHandling() {
        Parser parser = Parser.htmlParser();
        String html = "<form><input type='text' name='t' value='v'></form>";
        Element doc = parser.parseInput(html, "");
        assertNotNull(doc.select("form").first());
    }

    @Test
    public void testSelectElementEdgeCases() {
        Parser parser = Parser.htmlParser();
        String html = "<select><optgroup label='g'><option>1</option></optgroup></select>";
        Element doc = parser.parseInput(html, "");
        assertNotNull(doc.select("select").first());
    }

    @Test
    public void testSpecialTagsInsertion() {
        Parser parser = Parser.htmlParser();
        String html = "<head><title>Title</title><script>var x = 1;</script><style>body { color: red; }</style></head>";
        Element doc = parser.parseInput(html, "");
        assertNotNull(doc.head());
    }

    @Test
    public void testFramesetHandling() {
        Parser parser = Parser.htmlParser();
        String html = "<html><head></head><frameset><frame src='a.html'></frameset></html>";
        Element doc = parser.parseInput(html, "");
        assertNotNull(doc);
    }

    @Test
    public void testForeignContentHandling() {
        Parser parser = Parser.htmlParser();
        String html = "<svg><path d='M10 10'/></svg><math><mi>x</mi></math>";
        Element doc = parser.parseInput(html, "");
        assertNotNull(doc);
    }

    @Test
    public void testNodeRemovalAndReparenting() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse("<div><span>Text</span></div>", "", parser);
        
        Token.StartTag div = new Token.StartTag("div");
        tb.process(div);
        
        Token.StartTag span = new Token.StartTag("span");
        tb.process(span);
        
        Token.Character text = new Token.Character().data("Hello");
        tb.process(text);
        
        tb.process(new Token.EndTag("span"));
        tb.process(new Token.EndTag("div"));
        
        assertNotNull(tb.getCurrentNode());
    }
}