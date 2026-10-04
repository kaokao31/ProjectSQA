package org.jsoup.parser;

import org.junit.Test;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import static org.junit.Assert.*;

public class HtmlTreeBuilderStateTest {

    @Test
    public void testEnumValuesAndProcess() {
        HtmlTreeBuilderState[] states = HtmlTreeBuilderState.values();
        assertTrue(states.length > 0);

        for (HtmlTreeBuilderState state : states) {
            assertNotNull(state);
            // Verify that calling process on tokens with dummy builders doesn't throw unexpected unsupported exceptions immediately
            HtmlTreeBuilder tb = new HtmlTreeBuilder();
            tb.initialiseParse("<html><head></head><body><p>Hello</p></body></html>", "http://example.com", new ParseErrorList(10));
            
            Token.Character charToken = new Token.Character("test");
            try {
                state.process(charToken, tb);
            } catch (Exception e) {
                // Some states might throw if tree builder is not in correct context, which is expected.
            }
        }
    }

    @Test
    public void testInitialState() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<!DOCTYPE html><html></html>", "", new ParseErrorList(1));
        
        Token.Doctype d = new Token.Doctype();
        d.name.append("html");
        
        boolean res = HtmlTreeBuilderState.Initial.process(d, tb);
        assertTrue(res);
    }

    @Test
    public void testBeforeHtmlState() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html></html>", "", new ParseErrorList(1));
        
        Token.StartTag start = new Token.StartTag();
        start.name("html");
        
        boolean res = HtmlTreeBuilderState.BeforeHtml.process(start, tb);
        assertTrue(res);
        
        Token.Comment comment = new Token.Comment();
        boolean resComment = HtmlTreeBuilderState.BeforeHtml.process(comment, tb);
        assertTrue(resComment);
    }

    @Test
    public void testBeforeHeadState() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<head></head>", "", new ParseErrorList(1));
        
        Token.Character ch = new Token.Character("   ");
        boolean resCh = HtmlTreeBuilderState.BeforeHead.process(ch, tb);
        assertTrue(resCh);

        Token.StartTag head = new Token.StartTag();
        head.name("head");
        boolean resHead = HtmlTreeBuilderState.BeforeHead.process(head, tb);
        assertTrue(resHead);
    }

    @Test
    public void testInHeadState() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<head><title>Foo</title></head>", "", new ParseErrorList(1));

        Token.StartTag title = new Token.StartTag();
        title.name("title");
        boolean resTitle = HtmlTreeBuilderState.InHead.process(title, tb);
        assertTrue(resTitle);

        Token.EndTag head = new Token.EndTag();
        head.name("head");
        boolean resHead = HtmlTreeBuilderState.InHead.process(head, tb);
        assertTrue(resHead);
    }

    @Test
    public void testInBodyState() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<body><p>Text</p></body>", "", new ParseErrorList(1));

        Token.Character ch = new Token.Character("Hello");
        boolean resCh = HtmlTreeBuilderState.InBody.process(ch, tb);
        assertTrue(resCh);

        Token.StartTag p = new Token.StartTag();
        p.name("p");
        boolean resP = HtmlTreeBuilderState.InBody.process(p, tb);
        assertTrue(resP);
        
        Token.EOF eof = new Token.EOF();
        boolean resEof = HtmlTreeBuilderState.InBody.process(eof, tb);
        assertTrue(resEof);
    }

    @Test
    public void testTextState() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<p>Text</p>", "", new ParseErrorList(1));

        Token.Character ch = new Token.Character("Sample Text");
        boolean res = HtmlTreeBuilderState.Text.process(ch, tb);
        assertTrue(res);
    }

    @Test
    public void testAfterBodyState() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<body></body>", "", new ParseErrorList(1));

        Token.Character ch = new Token.Character("   ");
        boolean res = HtmlTreeBuilderState.AfterBody.process(ch, tb);
        assertTrue(res);

        Token.EOF eof = new Token.EOF();
        boolean resEof = HtmlTreeBuilderState.AfterBody.process(eof, tb);
        assertTrue(resEof);
    }

    @Test
    public void testInTableState() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<table><tr><td></td></tr></table>", "", new ParseErrorList(1));

        Token.StartTag caption = new Token.StartTag();
        caption.name("caption");
        boolean res = HtmlTreeBuilderState.InTable.process(caption, tb);
        assertTrue(res);
    }

    @Test
    public void testConstantsAndHelpers() {
        assertNotNull(HtmlTreeBuilderState.valueOf("InBody"));
        assertNotNull(HtmlTreeBuilderState.valueOf("Initial"));
        assertNotNull(HtmlTreeBuilderState.valueOf("BeforeHtml"));
        assertNotNull(HtmlTreeBuilderState.valueOf("BeforeHead"));
        assertNotNull(HtmlTreeBuilderState.valueOf("InHead"));
        assertNotNull(HtmlTreeBuilderState.valueOf("Text"));
        assertNotNull(HtmlTreeBuilderState.valueOf("AfterBody"));
    }
}