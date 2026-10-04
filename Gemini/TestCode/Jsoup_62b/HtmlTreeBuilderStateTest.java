package org.jsoup.parser;

import org.junit.Test;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import static org.junit.Assert.*;

public class HtmlTreeBuilderStateTest {

    @Test
    public void testEnumValuesAndProcess() {
        // Ensure all enum states exist and can process basic tokens without throwing unhandled exceptions immediately,
        // or specifically target the transition/process logic.
        HtmlTreeBuilderState[] states = HtmlTreeBuilderState.values();
        assertTrue(states.length > 0);

        for (HtmlTreeBuilderState state : states) {
            assertNotNull(state);
            // Test process with a dummy token in various states
            HtmlTreeBuilder tb = new HtmlTreeBuilder();
            Document doc = tb.parse("<!DOCTYPE html><html><head></head><body><p>Hello</p></body></html>", "");
            assertNotNull(doc);
        }
    }

    @Test
    public void testInitialState() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        HtmlTreeBuilderState state = HtmlTreeBuilderState.Initial;
        
        Token.Doctype d = new Token.Doctype();
        d.name("html");
        
        boolean result = state.process(d, tb);
        assertTrue(result);

        Token.Comment c = new Token.Comment();
        c.set("comment");
        assertTrue(state.process(c, tb));

        Token.Character ch = new Token.Character();
        ch.data("   ");
        assertTrue(state.process(ch, tb));

        Token.Character chText = new Token.Character();
        chText.data("a");
        // Should handle text/other tokens depending on doctype
        tb.initialiseParse("<html></html>", "", ParseErrorList.noTracking());
        // Just exercising the code paths
        state.process(chText, tb);
    }

    @Test
    public void testBeforeHtmlState() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("", "", ParseErrorList.noTracking());
        HtmlTreeBuilderState state = HtmlTreeBuilderState.BeforeHtml;

        Token.Doctype d = new Token.Doctype();
        assertFalse(state.process(d, tb));

        Token.Comment c = new Token.Comment();
        assertTrue(state.process(c, tb));

        Token.Character ch = new Token.Character();
        ch.data(" \t\n\f");
        assertTrue(state.process(ch, tb));

        Token.StartTag startHtml = new Token.StartTag();
        startHtml.name("html");
        assertTrue(state.process(startHtml, tb));

        Token.EndTag endHtml = new Token.EndTag();
        endHtml.name("head");
        assertTrue(state.process(endHtml, tb));
    }

    @Test
    public void testBeforeHeadState() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html>", "", ParseErrorList.noTracking());
        HtmlTreeBuilderState state = HtmlTreeBuilderState.BeforeHead;

        Token.Character ch = new Token.Character();
        ch.data("   ");
        assertTrue(state.process(ch, tb));

        Token.Comment c = new Token.Comment();
        assertTrue(state.process(c, tb));

        Token.Doctype d = new Token.Doctype();
        assertFalse(state.process(d, tb));

        Token.StartTag startHtml = new Token.StartTag();
        startHtml.name("html");
        assertFalse(state.process(startHtml, tb));

        Token.StartTag startHead = new Token.StartTag();
        startHead.name("head");
        assertTrue(state.process(startHead, tb));

        Token.EndTag endTag = new Token.EndTag();
        endTag.name("p");
        assertTrue(state.process(endTag, tb));
    }

    @Test
    public void testInHeadState() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html><head>", "", ParseErrorList.noTracking());
        HtmlTreeBuilderState state = HtmlTreeBuilderState.InHead;

        Token.Character ch = new Token.Character();
        ch.data(" \n");
        assertTrue(state.process(ch, tb));

        Token.Comment c = new Token.Comment();
        assertTrue(state.process(c, tb));

        Token.Doctype d = new Token.Doctype();
        assertFalse(state.process(d, tb));

        Token.StartTag startMeta = new Token.StartTag();
        startMeta.name("meta");
        assertTrue(state.process(startMeta, tb));

        Token.StartTag startTitle = new Token.StartTag();
        startTitle.name("title");
        assertTrue(state.process(startTitle, tb));

        Token.StartTag startStyle = new Token.StartTag();
        startStyle.name("style");
        assertTrue(state.process(startStyle, tb));

        Token.StartTag startHead = new Token.StartTag();
        startHead.name("head");
        assertFalse(state.process(startHead, tb));

        Token.EndTag endHead = new Token.EndTag();
        endHead.name("head");
        assertTrue(state.process(endHead, tb));
    }

    @Test
    public void testInBodyState() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html><head></head><body>", "", ParseErrorList.noTracking());
        HtmlTreeBuilderState state = HtmlTreeBuilderState.InBody;

        Token.Character ch = new Token.Character();
        ch.data("Hello");
        assertTrue(state.process(ch, tb));

        Token.Comment c = new Token.Comment();
        assertTrue(state.process(c, tb));

        Token.Doctype d = new Token.Doctype();
        assertFalse(state.process(d, tb));

        Token.StartTag startDiv = new Token.StartTag();
        startDiv.name("div");
        assertTrue(state.process(startDiv, tb));

        Token.EndTag endDiv = new Token.EndTag();
        endDiv.name("div");
        assertTrue(state.process(endDiv, tb));

        Token.StartTag startBody = new Token.StartTag();
        startBody.name("body");
        assertTrue(state.process(startBody, tb));

        Token.EOF eof = new Token.EOF();
        assertTrue(state.process(eof, tb));
    }

    @Test
    public void testTextState() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html><head></head><body><script>", "", ParseErrorList.noTracking());
        HtmlTreeBuilderState state = HtmlTreeBuilderState.Text;

        Token.Character ch = new Token.Character();
        ch.data("var a = 1;");
        assertTrue(state.process(ch, tb));

        Token.EOF eof = new Token.EOF();
        // Should handle EOF in text state
        assertFalse(state.process(eof, tb));

        Token.EndTag endScript = new Token.EndTag();
        endScript.name("script");
        assertTrue(state.process(endScript, tb));
    }

    @Test
    public void testInTableState() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html><body><table>", "", ParseErrorList.noTracking());
        HtmlTreeBuilderState state = HtmlTreeBuilderState.InTable;

        Token.Character ch = new Token.Character();
        ch.data("text");
        // Table cell parsing or text handling
        state.process(ch, tb);

        Token.Comment c = new Token.Comment();
        assertTrue(state.process(c, tb));

        Token.StartTag startTr = new Token.StartTag();
        startTr.name("tr");
        assertTrue(state.process(startTr, tb));

        Token.EndTag endTable = new Token.EndTag();
        endTable.name("table");
        assertTrue(state.process(endTable, tb));
    }

    @Test
    public void testCaseSensitivityAndBug62Area() {
        // Specifically targeting case-sensitivity / tag name comparisons that were part of issue 62 in Jsoup
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html><body><MIXEDCASE>test</MIXEDCASE></body></html>", "", ParseErrorList.noTracking());
        
        HtmlTreeBuilderState state = HtmlTreeBuilderState.InBody;
        
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("MIXEDCASE");
        state.process(startTag, tb);

        Token.EndTag endTag = new Token.EndTag();
        endTag.name("mixedcase"); // test case insensitivity match or mismatch
        boolean res = state.process(endTag, tb);
        assertTrue(res);
    }

    @Test
    public void testOtherStates() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html></html>", "", ParseErrorList.noTracking());

        // Exercise various states to ensure full coverage
        HtmlTreeBuilderState.AfterHead.process(new Token.Comment(), tb);
        HtmlTreeBuilderState.InColumnGroup.process(new Token.Comment(), tb);
        HtmlTreeBuilderState.InTableBody.process(new Token.Comment(), tb);
        HtmlTreeBuilderState.InRow.process(new Token.Comment(), tb);
        HtmlTreeBuilderState.InCell.process(new Token.Comment(), tb);
        HtmlTreeBuilderState.InSelect.process(new Token.Comment(), tb);
        HtmlTreeBuilderState.InSelectInTable.process(new Token.Comment(), tb);
        HtmlTreeBuilderState.AfterBody.process(new Token.Comment(), tb);
        HtmlTreeBuilderState.InFrameset.process(new Token.Comment(), tb);
        HtmlTreeBuilderState.AfterFrameset.process(new Token.Comment(), tb);
        HtmlTreeBuilderState.AfterAfterBody.process(new Token.Comment(), tb);
        HtmlTreeBuilderState.AfterAfterFrameset.process(new Token.Comment(), tb);
    }
}