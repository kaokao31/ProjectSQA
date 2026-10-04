package org.jsoup.parser;

import org.junit.Test;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import static org.junit.Assert.*;

public class TreeBuilderStateTest {

    @Test
    public void testInitialStateProcess() {
        TreeBuilder tb = new TreeBuilder();
        tb.initialiseParse("<html><head></head><body><p>Hello</p></body></html>", "http://example.com", new ParseErrorList(0));
        
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("html");
        
        boolean result = TreeBuilderState.Initial.process(startTag, tb);
        assertTrue(result);
    }

    @Test
    public void testInitialStateProcessDoctype() {
        TreeBuilder tb = new TreeBuilder();
        tb.initialiseParse("<!DOCTYPE html><html></html>", "http://example.com", new ParseErrorList(0));
        
        Token.Doctype doctype = new Token.Doctype();
        doctype.name.append("html");
        
        boolean result = TreeBuilderState.Initial.process(doctype, tb);
        assertTrue(result);
    }

    @Test
    public void testBeforeHtmlProcess() {
        TreeBuilder tb = new TreeBuilder();
        tb.initialiseParse("<html></html>", "http://example.com", new ParseErrorList(0));
        
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("html");
        
        boolean result = TreeBuilderState.BeforeHtml.process(startTag, tb);
        assertTrue(result);
        assertNotNull(tb.getDocument().head());
    }

    @Test
    public void testBeforeHtmlComment() {
        TreeBuilder tb = new TreeBuilder();
        tb.initialiseParse("<!-- comment --><html></html>", "http://example.com", new ParseErrorList(0));
        
        Token.Comment comment = new Token.Comment();
        comment.comment.append("test comment");
        
        boolean result = TreeBuilderState.BeforeHtml.process(comment, tb);
        assertTrue(result);
    }

    @Test
    public void testBeforeHeadProcess() {
        TreeBuilder tb = new TreeBuilder();
        tb.initialiseParse("<html><head></head></html>", "http://example.com", new ParseErrorList(0));
        
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("head");
        
        boolean result = TreeBuilderState.BeforeHead.process(startTag, tb);
        assertTrue(result);
    }

    @Test
    public void testInHeadProcess() {
        TreeBuilder tb = new TreeBuilder();
        tb.initialiseParse("<html><head><title>Title</title></head></html>", "http://example.com", new ParseErrorList(0));
        tb.process(new Token.StartTag().name("html"));
        
        Token.StartTag titleTag = new Token.StartTag();
        titleTag.name("title");
        
        boolean result = TreeBuilderState.InHead.process(titleTag, tb);
        assertTrue(result);
    }

    @Test
    public void testInHeadNoscript() {
        TreeBuilder tb = new TreeBuilder();
        tb.initialiseParse("<html><head><noscript></noscript></head></html>", "http://example.com", new ParseErrorList(0));
        tb.process(new Token.StartTag().name("html"));
        
        Token.StartTag noscriptTag = new Token.StartTag();
        noscriptTag.name("noscript");
        
        boolean result = TreeBuilderState.InHead.process(noscriptTag, tb);
        assertTrue(result);
    }

    @Test
    public void testInHeadBodyEnd() {
        TreeBuilder tb = new TreeBuilder();
        tb.initialiseParse("<html><head></head><body></body></html>", "http://example.com", new ParseErrorList(0));
        tb.process(new Token.StartTag().name("html"));
        
        Token.EndTag bodyEnd = new Token.EndTag();
        bodyEnd.name("body");
        
        boolean result = TreeBuilderState.InHead.process(bodyEnd, tb);
        assertTrue(result);
    }

    @Test
    public void testInHeadHeadEnd() {
        TreeBuilder tb = new TreeBuilder();
        tb.initialiseParse("<html><head></head></html>", "http://example.com", new ParseErrorList(0));
        tb.process(new Token.StartTag().name("html"));
        tb.process(new Token.StartTag().name("head"));
        
        Token.EndTag headEnd = new Token.EndTag();
        headEnd.name("head");
        
        boolean result = TreeBuilderState.InHead.process(headEnd, tb);
        assertTrue(result);
    }

    @Test
    public void testAfterHeadProcess() {
        TreeBuilder tb = new TreeBuilder();
        tb.initialiseParse("<html><head></head><body></body></html>", "http://example.com", new ParseErrorList(0));
        tb.process(new Token.StartTag().name("html"));
        tb.process(new Token.StartTag().name("head"));
        tb.process(new Token.EndTag().name("head"));
        
        Token.StartTag bodyTag = new Token.StartTag();
        bodyTag.name("body");
        
        boolean result = TreeBuilderState.AfterHead.process(bodyTag, tb);
        assertTrue(result);
    }

    @Test
    public void testInBodyStartTag() {
        TreeBuilder tb = new TreeBuilder();
        tb.initialiseParse("<html><head></head><body><div></div></body></html>", "http://example.com", new ParseErrorList(0));
        tb.process(new Token.StartTag().name("html"));
        tb.process(new Token.StartTag().name("head"));
        tb.process(new Token.EndTag().name("head"));
        tb.process(new Token.StartTag().name("body"));
        
        Token.StartTag divTag = new Token.StartTag();
        divTag.name("div");
        
        boolean result = TreeBuilderState.InBody.process(divTag, tb);
        assertTrue(result);
    }

    @Test
    public void testInBodyEndTag() {
        TreeBuilder tb = new TreeBuilder();
        tb.initialiseParse("<html><head></head><body><div></div></body></html>", "http://example.com", new ParseErrorList(0));
        tb.process(new Token.StartTag().name("html"));
        tb.process(new Token.StartTag().name("head"));
        tb.process(new Token.EndTag().name("head"));
        tb.process(new Token.StartTag().name("body"));
        tb.process(new Token.StartTag().name("div"));
        
        Token.EndTag divEnd = new Token.EndTag();
        divEnd.name("div");
        
        boolean result = TreeBuilderState.InBody.process(divEnd, tb);
        assertTrue(result);
    }

    @Test
    public void testInBodyCharacters() {
        TreeBuilder tb = new TreeBuilder();
        tb.initialiseParse("<html><head></head><body>Hello World</body></html>", "http://example.com", new ParseErrorList(0));
        tb.process(new Token.StartTag().name("html"));
        tb.process(new Token.StartTag().name("head"));
        tb.process(new Token.EndTag().name("head"));
        tb.process(new Token.StartTag().name("body"));
        
        Token.Character chars = new Token.Character();
        chars.data("Hello World");
        
        boolean result = TreeBuilderState.InBody.process(chars, tb);
        assertTrue(result);
    }

    @Test
    public void testTextProcess() {
        TreeBuilder tb = new TreeBuilder();
        tb.initialiseParse("<html><head></head><body><script>code</script></body></html>", "http://example.com", new ParseErrorList(0));
        
        Token.Character chars = new Token.Character();
        chars.data("some script code");
        
        boolean result = TreeBuilderState.Text.process(chars, tb);
        assertTrue(result);
    }

    @Test
    public void testInTableProcess() {
        TreeBuilder tb = new TreeBuilder();
        tb.initialiseParse("<table><tr><td></td></tr></table>", "http://example.com", new ParseErrorList(0));
        
        Token.StartTag caption = new Token.StartTag();
        caption.name("caption");
        
        boolean result = TreeBuilderState.InTable.process(caption, tb);
        assertTrue(result);
    }

    @Test
    public void testInTableBodyProcess() {
        TreeBuilder tb = new TreeBuilder();
        tb.initialiseParse("<table><tbody><tr></tr></tbody></table>", "http://example.com", new ParseErrorList(0));
        
        Token.StartTag tr = new Token.StartTag();
        tr.name("tr");
        
        boolean result = TreeBuilderState.InTableBody.process(tr, tb);
        assertTrue(result);
    }

    @Test
    public void testInRowProcess() {
        TreeBuilder tb = new TreeBuilder();
        tb.initialiseParse("<table><tr><td></td></tr></table>", "http://example.com", new ParseErrorList(0));
        
        Token.StartTag td = new Token.StartTag();
        td.name("td");
        
        boolean result = TreeBuilderState.InRow.process(td, tb);
        assertTrue(result);
    }

    @Test
    public void testInCellProcess() {
        TreeBuilder tb = new Token.StartTag(); // Just placeholder for setup if needed
        TreeBuilder tbInstance = new TreeBuilder();
        tbInstance.initialiseParse("<td>Cell</td>", "http://example.com", new ParseErrorList(0));
        
        Token.EndTag tdEnd = new Token.EndTag();
        tdEnd.name("td");
        
        // Exercise Cell processor via generic routing or direct state
        boolean result = TreeBuilderState.InCell.process(tdEnd, tbInstance);
        assertTrue(result);
    }

    @Test
    public void testAfterBodyProcess() {
        TreeBuilder tb = new TreeBuilder();
        tb.initialiseParse("<html></html>", "http://example.com", new ParseErrorList(0));
        
        Token.Comment comment = new Token.Comment();
        comment.comment.append("after body");
        
        boolean result = TreeBuilderState.AfterBody.process(comment, tb);
        assertTrue(result);
    }

    @Test
    public void testInFramesetProcess() {
        TreeBuilder tb = new TreeBuilder();
        tb.initialiseParse("<frameset></frameset>", "http://example.com", new ParseErrorList(0));
        
        Token.StartTag frame = new Token.StartTag();
        frame.name("frame");
        
        boolean result = TreeBuilderState.InFrameset.process(frame, tb);
        assertTrue(result);
    }

    @Test
    public void testAfterFramesetProcess() {
        TreeBuilder tb = new TreeBuilder();
        tb.initialiseParse("<frameset></frameset><noframes></noframes>", "http://example.com", new ParseErrorList(0));
        
        Token.Comment comment = new Token.Comment();
        comment.comment.append("after frameset");
        
        boolean result = TreeBuilderState.AfterFrameset.process(comment, tb);
        assertTrue(result);
    }

    @Test
    public void testAfterAfterBodyProcess() {
        TreeBuilder tb = new TreeBuilder();
        tb.initialiseParse("<html></html>", "http://example.com", new ParseErrorList(0));
        
        Token.Comment comment = new Token.Comment();
        comment.comment.append("after after body");
        
        boolean result = TreeBuilderState.AfterAfterBody.process(comment, tb);
        assertTrue(result);
    }

    @Test
    public void testInSelectProcess() {
        TreeBuilder tb = new TreeBuilder();
        tb.initialiseParse("<select><option></option></select>", "http://example.com", new ParseErrorList(0));
        
        Token.StartTag option = new Token.StartTag();
        option.name("option");
        
        boolean result = TreeBuilderState.InSelect.process(option, tb);
        assertTrue(result);
    }
}