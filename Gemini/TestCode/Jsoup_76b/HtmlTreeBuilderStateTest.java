package org.jsoup.parser;

import org.junit.Test;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;

import static org.junit.Assert.*;

public class HtmlTreeBuilderStateTest {

    @Test
    public void testInitialState() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html><head></head><body><p>Hello</p></body></html>", "http://example.com", parser);
        
        // Exercise Initial state process
        Token.Doctype doctypeToken = new Token.Doctype();
        doctypeToken.name.append("html");
        boolean res = HtmlTreeBuilderState.Initial.process(doctypeToken, tb);
        assertTrue(res);

        Token.Comment commentToken = new Token.Comment();
        res = HtmlTreeBuilderState.Initial.process(commentToken, tb);
        assertTrue(res);

        Token.Character cToken = new Token.Character();
        cToken.data("   ");
        res = HtmlTreeBuilderState.Initial.process(cToken, tb);
        assertTrue(res);

        Token.Character cTokenNonWs = new Token.Character();
        cTokenNonWs.data("a");
        res = HtmlTreeBuilderState.Initial.process(cTokenNonWs, tb);
        assertTrue(res);
    }

    @Test
    public void testBeforeHtmlState() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html></html>", "http://example.com", parser);

        Token.Doctype doctype = new Token.Doctype();
        assertFalse(HtmlTreeBuilderState.BeforeHtml.process(doctype, tb));

        Token.Comment comment = new Token.Comment();
        assertTrue(HtmlTreeBuilderState.BeforeHtml.process(comment, tb));

        Token.Character cWs = new Token.Character();
        cWs.data(" \n\r\t");
        assertTrue(HtmlTreeBuilderState.BeforeHtml.process(cWs, tb));

        Token.StartTag startHtml = new Token.StartTag();
        startHtml.name("html");
        assertTrue(HtmlTreeBuilderState.BeforeHtml.process(startHtml, tb));

        Token.StartTag startOther = new Token.StartTag();
        startOther.name("div");
        assertTrue(HtmlTreeBuilderState.BeforeHtml.process(startOther, tb));

        Token.EndTag endTag = new Token.EndTag();
        endTag.name("p");
        assertTrue(HtmlTreeBuilderState.BeforeHtml.process(endTag, tb));
    }

    @Test
    public void testBeforeHeadState() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html><body></body></html>", "http://example.com", parser);

        Token.Character cWs = new Token.Character();
        cWs.data("   ");
        assertTrue(HtmlTreeBuilderState.BeforeHead.process(cWs, tb));

        Token.Comment comment = new Token.Comment();
        assertTrue(HtmlTreeBuilderState.BeforeHead.process(comment, tb));

        Token.Doctype doctype = new Token.Doctype();
        assertFalse(HtmlTreeBuilderState.BeforeHead.process(doctype, tb));

        Token.StartTag startHtml = new Token.StartTag();
        startHtml.name("html");
        assertTrue(HtmlTreeBuilderState.BeforeHead.process(startHtml, tb));

        Token.StartTag startHead = new Token.StartTag();
        startHead.name("head");
        assertTrue(HtmlTreeBuilderState.BeforeHead.process(startHead, tb));

        // Reset tb state for other tags
        tb.initialiseParse("<html><body></body></html>", "http://example.com", parser);
        Token.StartTag startOther = new Token.StartTag();
        startOther.name("body");
        assertTrue(HtmlTreeBuilderState.BeforeHead.process(startOther, tb));

        tb.initialiseParse("<html><body></body></html>", "http://example.com", parser);
        Token.EndTag endTag = new Token.EndTag();
        endTag.name("body");
        assertTrue(HtmlTreeBuilderState.BeforeHead.process(endTag, tb));
    }

    @Test
    public void testInHeadState() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html><head></head><body></body></html>", "http://example.com", parser);

        Token.Character cWs = new Token.Character();
        cWs.data(" \n");
        assertTrue(HtmlTreeBuilderState.InHead.process(cWs, tb));

        Token.Comment comment = new Token.Comment();
        assertTrue(HtmlTreeBuilderState.InHead.process(comment, tb));

        Token.Doctype doctype = new Token.Doctype();
        assertFalse(HtmlTreeBuilderState.InHead.process(doctype, tb));

        Token.StartTag startHtml = new Token.StartTag();
        startHtml.name("html");
        assertTrue(HtmlTreeBuilderState.InHead.process(startHtml, tb));

        Token.StartTag startMeta = new Token.StartTag();
        startMeta.name("meta");
        assertTrue(HtmlTreeBuilderState.InHead.process(startMeta, tb));

        Token.StartTag startTitle = new Token.StartTag();
        startTitle.name("title");
        assertTrue(HtmlTreeBuilderState.InHead.process(startTitle, tb));

        Token.StartTag startNoscript = new Token.StartTag();
        startNoscript.name("noscript");
        assertTrue(HtmlTreeBuilderState.InHead.process(startNoscript, tb));

        Token.StartTag startScript = new Token.StartTag();
        startScript.name("script");
        assertTrue(HtmlTreeBuilderState.InHead.process(startScript, tb));

        Token.StartTag startHead = new Token.StartTag();
        startHead.name("head");
        assertFalse(HtmlTreeBuilderState.InHead.process(startHead, tb));

        Token.EndTag endHead = new Token.EndTag();
        endHead.name("head");
        assertTrue(HtmlTreeBuilderState.InHead.process(endHead, tb));

        Token.EndTag endOther = new Token.EndTag();
        endOther.name("body");
        assertTrue(HtmlTreeBuilderState.InHead.process(endOther, tb));
    }

    @Test
    public void testInHeadNoscriptState() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html><head><noscript></noscript></head><body></body></html>", "http://example.com", parser);

        Token.Doctype doctype = new Token.Doctype();
        assertFalse(HtmlTreeBuilderState.InHeadNoscript.process(doctype, tb));

        Token.StartTag startHtml = new Token.StartTag();
        startHtml.name("html");
        assertTrue(HtmlTreeBuilderState.InHeadNoscript.process(startHtml, tb));

        Token.EndTag endNoscript = new Token.EndTag();
        endNoscript.name("noscript");
        assertTrue(HtmlTreeBuilderState.InHeadNoscript.process(endNoscript, tb));

        Token.Character cWs = new Token.Character();
        cWs.data(" ");
        assertTrue(HtmlTreeBuilderState.InHeadNoscript.process(cWs, tb));

        Token.Comment comment = new Token.Comment();
        assertTrue(HtmlTreeBuilderState.InHeadNoscript.process(comment, tb));

        Token.StartTag startBasefont = new Token.StartTag();
        startBasefont.name("basefont");
        assertTrue(HtmlTreeBuilderState.InHeadNoscript.process(startBasefont, tb));

        Token.EndTag endBr = new Token.EndTag();
        endBr.name("br");
        assertTrue(HtmlTreeBuilderState.InHeadNoscript.process(endBr, tb));
    }

    @Test
    public void testAfterHeadState() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html><head></head><body></body></html>", "http://example.com", parser);

        Token.Character cWs = new Token.Character();
        cWs.data(" \r\n");
        assertTrue(HtmlTreeBuilderState.AfterHead.process(cWs, tb));

        Token.Comment comment = new Token.Comment();
        assertTrue(HtmlTreeBuilderState.AfterHead.process(comment, tb));

        Token.Doctype doctype = new Token.Doctype();
        assertFalse(HtmlTreeBuilderState.AfterHead.process(doctype, tb));

        Token.StartTag startHtml = new Token.StartTag();
        startHtml.name("html");
        assertTrue(HtmlTreeBuilderState.AfterHead.process(startHtml, tb));

        Token.StartTag startBody = new Token.StartTag();
        startBody.name("body");
        assertTrue(HtmlTreeBuilderState.AfterHead.process(startBody, tb));

        Token.StartTag startFrameset = new Token.StartTag();
        startFrameset.name("frameset");
        assertTrue(HtmlTreeBuilderState.AfterHead.process(startFrameset, tb));

        Token.StartTag startHead = new Token.StartTag();
        startHead.name("head");
        assertFalse(HtmlTreeBuilderState.AfterHead.process(startHead, tb));

        Token.EndTag endTag = new Token.EndTag();
        endTag.name("p");
        assertTrue(HtmlTreeBuilderState.AfterHead.process(endTag, tb));
    }

    @Test
    public void testInBodyState() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html><head></head><body><p>Hello</p></body></html>", "http://example.com", parser);

        Token.Character cNull = new Token.Character();
        // simulating null char
        boolean processed = HtmlTreeBuilderState.InBody.process(cNull, tb);
        // Depending on parser state, just ensure it doesn't crash
        assertNotNull(tb.getState());

        Token.Comment comment = new Token.Comment();
        assertTrue(HtmlTreeBuilderState.InBody.process(comment, tb));

        Token.Doctype doctype = new Token.Doctype();
        assertFalse(HtmlTreeBuilderState.InBody.process(doctype, tb));

        Token.StartTag startHtml = new Token.StartTag();
        startHtml.name("html");
        assertTrue(HtmlTreeBuilderState.InBody.process(startHtml, tb));

        Token.StartTag startBody = new Token.StartTag();
        startBody.name("body");
        assertTrue(HtmlTreeBuilderState.InBody.process(startBody, tb));

        Token.StartTag startA = new Token.StartTag();
        startA.name("a");
        assertTrue(HtmlTreeBuilderState.InBody.process(startA, tb));

        Token.EndTag endBody = new Token.EndTag();
        endBody.name("body");
        assertTrue(HtmlTreeBuilderState.InBody.process(endBody, tb));

        Token.EOF eof = new Token.EOF();
        assertTrue(HtmlTreeBuilderState.InBody.process(eof, tb));
    }

    @Test
    public void testTextState() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html><head></head><body><script>abc</script></body></html>", "http://example.com", parser);

        Token.Character c = new Token.Character();
        c.data("content");
        assertTrue(HtmlTreeBuilderState.Text.process(c, tb));

        Token.EOF eof = new Token.EOF();
        assertTrue(HtmlTreeBuilderState.Text.process(eof, tb));

        Token.EndTag endTag = new Token.EndTag();
        endTag.name("script");
        assertTrue(HtmlTreeBuilderState.Text.process(endTag, tb));
    }

    @Test
    public void testInTableState() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html><head></head><body><table></table></body></html>", "http://example.com", parser);

        Token.Comment comment = new Token.Comment();
        assertTrue(HtmlTreeBuilderState.InTable.process(comment, tb));

        Token.Doctype doctype = new Token.Doctype();
        assertFalse(HtmlTreeBuilderState.InTable.process(doctype, tb));

        Token.StartTag startTable = new Token.StartTag();
        startTable.name("table");
        assertTrue(HtmlTreeBuilderState.InTable.process(startTable, tb));

        Token.EndTag endTable = new Token.EndTag();
        endTable.name("table");
        assertTrue(HtmlTreeBuilderState.InTable.process(endTable, tb));

        Token.StartTag startCaption = new Token.StartTag();
        startCaption.name("caption");
        assertTrue(HtmlTreeBuilderState.InTable.process(startCaption, tb));
    }

    @Test
    public void testInCaptionState() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html><head></head><body><table><caption></caption></table></body></html>", "http://example.com", parser);

        Token.EndTag endCaption = new Token.EndTag();
        endCaption.name("caption");
        assertTrue(HtmlTreeBuilderState.InCaption.process(endCaption, tb));

        Token.StartTag startCol = new Token.StartTag();
        startCol.name("col");
        assertTrue(HtmlTreeBuilderState.InCaption.process(startCol, tb));
    }

    @Test
    public void testInColumnGroupState() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html><head></head><body><table><colgroup></colgroup></table></body></html>", "http://example.com", parser);

        Token.Character cWs = new Token.Character();
        cWs.data(" ");
        assertTrue(HtmlTreeBuilderState.InColumnGroup.process(cWs, tb));

        Token.Comment comment = new Token.Comment();
        assertTrue(HtmlTreeBuilderState.InColumnGroup.process(comment, tb));

        Token.Doctype doctype = new Token.Doctype();
        assertFalse(HtmlTreeBuilderState.InColumnGroup.process(doctype, tb));

        Token.StartTag startHtml = new Token.StartTag();
        startHtml.name("html");
        assertTrue(HtmlTreeBuilderState.InColumnGroup.process(startHtml, tb));

        Token.EndTag endColgroup = new Token.EndTag();
        endColgroup.name("colgroup");
        assertTrue(HtmlTreeBuilderState.InColumnGroup.process(endColgroup, tb));
    }

    @Test
    public void testInTableBodyState() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html><head></head><body><table><tbody></tbody></table></body></html>", "http://example.com", parser);

        Token.StartTag startTr = new Token.StartTag();
        startTr.name("tr");
        assertTrue(HtmlTreeBuilderState.InTableBody.process(startTr, tb));

        Token.EndTag endTbody = new Token.EndTag();
        endTbody.name("tbody");
        assertTrue(HtmlTreeBuilderState.InTableBody.process(endTbody, tb));
    }

    @Test
    public void testInRowState() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html><head></head><body><table><tbody><tr></tr></tbody></table></body></html>", "http://example.com", parser);

        Token.StartTag startTd = new Token.StartTag();
        startTd.name("td");
        assertTrue(HtmlTreeBuilderState.InRow.process(startTd, tb));

        Token.EndTag endTr = new Token.EndTag();
        endTr.name("tr");
        assertTrue(HtmlTreeBuilderState.InRow.process(endTr, tb));
    }

    @Test
    public void testInCellState() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html><head></head><body><table><tbody><tr><td></td></tr></tbody></table></body></html>", "http://example.com", parser);

        Token.EndTag endTd = new Token.EndTag();
        endTd.name("td");
        assertTrue(HtmlTreeBuilderState.InCell.process(endTd, tb));
    }

    @Test
    public void testInSelectState() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html><head></head><body><select></select></body></html>", "http://example.com", parser);

        Token.Character c = new Token.Character();
        c.data("val");
        assertTrue(HtmlTreeBuilderState.InSelect.process(c, tb));

        Token.StartTag startOption = new Token.StartTag();
        startOption.name("option");
        assertTrue(HtmlTreeBuilderState.InSelect.process(startOption, tb));

        Token.EndTag endSelect = new Token.EndTag();
        endSelect.name("select");
        assertTrue(HtmlTreeBuilderState.InSelect.process(endSelect, tb));
    }

    @Test
    public void testInSelectInTableState() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html><head></head><body><table><select></select></table></body></html>", "http://example.com", parser);

        Token.StartTag startCaption = new Token.StartTag();
        startCaption.name("caption");
        assertTrue(HtmlTreeBuilderState.InSelectInTable.process(startCaption, tb));

        Token.EndTag endTable = new Token.EndTag();
        endTable.name("table");
        assertTrue(HtmlTreeBuilderState.InSelectInTable.process(endTable, tb));
    }

    @Test
    public void testAfterBodyState() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html><head></head><body></body></html>", "http://example.com", parser);

        Token.Comment comment = new Token.Comment();
        assertTrue(HtmlTreeBuilderState.AfterBody.process(comment, tb));

        Token.Doctype doctype = new Token.Doctype();
        assertFalse(HtmlTreeBuilderState.AfterBody.process(doctype, tb));

        Token.StartTag startHtml = new Token.StartTag();
        startHtml.name("html");
        assertTrue(HtmlTreeBuilderState.AfterBody.process(startHtml, tb));

        Token.EndTag endHtml = new Token.EndTag();
        endHtml.name("html");
        assertTrue(HtmlTreeBuilderState.AfterBody.process(endHtml, tb));

        Token.EOF eof = new Token.EOF();
        assertTrue(HtmlTreeBuilderState.AfterBody.process(eof, tb));
    }

    @Test
    public void testInFramesetState() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html><head></head><frameset></frameset></html>", "http://example.com", parser);

        Token.Character cWs = new Token.Character();
        cWs.data(" ");
        assertTrue(HtmlTreeBuilderState.InFrameset.process(cWs, tb));

        Token.StartTag startFrameset = new Token.StartTag();
        startFrameset.name("frameset");
        assertTrue(HtmlTreeBuilderState.InFrameset.process(startFrameset, tb));

        Token.EndTag endFrameset = new Token.EndTag();
        endFrameset.name("frameset");
        assertTrue(HtmlTreeBuilderState.InFrameset.process(endFrameset, tb));
    }

    @Test
    public void testAfterFramesetState() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html><head></head><frameset></frameset></html>", "http://example.com", parser);

        Token.Comment comment = new Token.Comment();
        assertTrue(HtmlTreeBuilderState.AfterFrameset.process(comment, tb));

        Token.StartTag startHtml = new Token.StartTag();
        startHtml.name("html");
        assertTrue(HtmlTreeBuilderState.AfterFrameset.process(startHtml, tb));

        Token.EOF eof = new Token.EOF();
        assertTrue(HtmlTreeBuilderState.AfterFrameset.process(eof, tb));
    }

    @Test
    public void testForeignContentState() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html><head></head><body><math></math></body></html>", "http://example.com", parser);

        Token.Character c = new Token.Character();
        c.data("text");
        assertTrue(HtmlTreeBuilderState.ForeignContent.process(c, tb));

        Token.Comment comment = new Token.Comment();
        assertTrue(HtmlTreeBuilderState.ForeignContent.process(comment, tb));

        Token.StartTag startTag = new Token.StartTag();
        startTag.name("mglyph");
        assertTrue(HtmlTreeBuilderState.ForeignContent.process(startTag, tb));
    }
}