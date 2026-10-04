package org.jsoup.parser;

import org.junit.Test;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Attribute;

import static org.junit.Assert.*;

/**
 * JUnit 4 Test Suite for TreeBuilderState (Jsoup Bug 15 context).
 */
public class TreeBuilderStateTest {

    @Test
    public void testInitialState() throws Exception {
        TreeBuilder tb = new TreeBuilder();
        tb.initial("<html><head></head><body><p>Hello</p></body></html>", "http://example.com", new ParseErrorList(0));
        
        Token.Doctype doctypeToken = new Token.Doctype();
        doctypeToken.name.append("html");
        
        boolean result = TreeBuilderState.Initial.process(doctypeToken, tb);
        assertTrue(result);

        Token.Comment commentToken = new Token.Comment();
        result = TreeBuilderState.Initial.process(commentToken, tb);
        assertTrue(result);

        Token.Character charToken = new Token.Character();
        charToken.data("   ");
        result = TreeBuilderState.Initial.process(charToken, tb);
        assertTrue(result);

        Token.Character nonWhitespaceChar = new Token.Character();
        nonWhitespaceChar.data("a");
        result = TreeBuilderState.Initial.process(nonWhitespaceChar, tb);
        assertTrue(result);

        Token.StartTag startTag = new Token.StartTag();
        startTag.name("html");
        result = TreeBuilderState.Initial.process(startTag, tb);
        assertTrue(result);
    }

    @Test
    public void testBeforeHtmlState() throws Exception {
        TreeBuilder tb = new TreeBuilder();
        tb.initial("<html></html>", "http://example.com", new ParseErrorList(0));

        Token.Doctype doctype = new Token.Doctype();
        assertFalse(TreeBuilderState.BeforeHtml.process(doctype, tb));

        Token.Comment comment = new Token.Comment();
        assertTrue(TreeBuilderState.BeforeHtml.process(comment, tb));

        Token.Character space = new Token.Character();
        space.data(" \n\r\t");
        assertTrue(TreeBuilderState.BeforeHtml.process(space, tb));

        Token.Character nonSpace = new Token.Character();
        nonSpace.data("x");
        assertTrue(TreeBuilderState.BeforeHtml.process(nonSpace, tb));

        Token.StartTag htmlTag = new Token.StartTag();
        htmlTag.name("html");
        assertTrue(TreeBuilderState.BeforeHtml.process(htmlTag, tb));
        assertEquals(TreeBuilderState.BeforeHead, tb.state());

        Token.EndTag endTag = new Token.EndTag();
        endTag.name("head");
        assertTrue(TreeBuilderState.BeforeHtml.process(endTag, tb));
        
        Token.EndTag otherEndTag = new Token.EndTag();
        otherEndTag.name("p");
        assertTrue(TreeBuilderState.BeforeHtml.process(otherEndTag, tb));
    }

    @Test
    public void testBeforeHeadState() throws Exception {
        TreeBuilder tb = new TreeBuilder();
        tb.initial("<html></html>", "http://example.com", new ParseErrorList(0));

        Token.Character space = new Token.Character();
        space.data("   ");
        assertTrue(TreeBuilderState.BeforeHead.process(space, tb));

        Token.Comment comment = new Token.Comment();
        assertTrue(TreeBuilderState.BeforeHead.process(comment, tb));

        Token.Doctype doctype = new Token.Doctype();
        assertFalse(TreeBuilderState.BeforeHead.process(doctype, tb));

        Token.StartTag htmlTag = new Token.StartTag();
        htmlTag.name("html");
        assertTrue(TreeBuilderState.BeforeHead.process(htmlTag, tb));

        Token.StartTag headTag = new Token.StartTag();
        headTag.name("head");
        assertTrue(TreeBuilderState.BeforeHead.process(headTag, tb));
        assertEquals(TreeBuilderState.InHead, tb.state());

        Token.EndTag endTag = new Token.EndTag();
        endTag.name("p");
        assertTrue(TreeBuilderState.BeforeHead.process(endTag, tb));

        Token.StartTag otherTag = new Token.StartTag();
        otherTag.name("body");
        assertTrue(TreeBuilderState.BeforeHead.process(otherTag, tb));
        assertEquals(TreeBuilderState.InHead, tb.state());
    }

    @Test
    public void testInHeadState() throws Exception {
        TreeBuilder tb = new TreeBuilder();
        tb.initial("<html><head></head></html>", "http://example.com", new ParseErrorList(0));

        Token.Character space = new Token.Character();
        space.data(" \t\n");
        assertTrue(TreeBuilderState.InHead.process(space, tb));

        Token.Comment comment = new Token.Comment();
        assertTrue(TreeBuilderState.InHead.process(comment, tb));

        Token.Doctype doctype = new Token.Doctype();
        assertFalse(TreeBuilderState.InHead.process(doctype, tb));

        Token.StartTag htmlTag = new Token.StartTag();
        htmlTag.name("html");
        assertTrue(TreeBuilderState.InHead.process(htmlTag, tb));

        Token.StartTag headTag = new Token.StartTag();
        headTag.name("head");
        assertTrue(TreeBuilderState.InHead.process(headTag, tb));

        Token.StartTag baseTag = new Token.StartTag();
        baseTag.name("base");
        assertTrue(TreeBuilderState.InHead.process(baseTag, tb));

        Token.StartTag metaTag = new Token.StartTag();
        metaTag.name("meta");
        assertTrue(TreeBuilderState.InHead.process(metaTag, tb));

        Token.StartTag titleTag = new Token.StartTag();
        titleTag.name("title");
        assertTrue(TreeBuilderState.InHead.process(titleTag, tb));
        assertEquals(TreeBuilderState.Text, tb.state());

        tb.transition(TreeBuilderState.InHead);
        Token.StartTag noscriptTag = new Token.StartTag();
        noscriptTag.name("noscript");
        assertTrue(TreeBuilderState.InHead.process(noscriptTag, tb));

        tb.transition(TreeBuilderState.InHead);
        Token.StartTag scriptTag = new Token.StartTag();
        scriptTag.name("script");
        assertTrue(TreeBuilderState.InHead.process(scriptTag, tb));
        assertEquals(TreeBuilderState.ScriptData, tb.state());

        tb.transition(TreeBuilderState.InHead);
        Token.StartTag styleTag = new Token.StartTag();
        styleTag.name("style");
        assertTrue(TreeBuilderState.InHead.process(styleTag, tb));

        tb.transition(TreeBuilderState.InHead);
        Token.StartTag headEndTag = new Token.StartTag();
        headEndTag.name("head");
        assertTrue(TreeBuilderState.InHead.process(headEndTag, tb));
        assertEquals(TreeBuilderState.AfterHead, tb.state());

        tb.transition(TreeBuilderState.InHead);
        Token.EndTag otherEnd = new Token.EndTag();
        otherEnd.name("body");
        assertTrue(TreeBuilderState.InHead.process(otherEnd, tb));

        tb.transition(TreeBuilderState.InHead);
        Token.StartTag anyOther = new Token.StartTag();
        anyOther.name("p");
        assertTrue(TreeBuilderState.InHead.process(anyOther, tb));
        assertEquals(TreeBuilderState.AfterHead, tb.state());
    }

    @Test
    public void testInHeadNoscriptState() throws Exception {
        TreeBuilder tb = new TreeBuilder();
        tb.initial("<html><head><noscript></noscript></head></html>", "http://example.com", new ParseErrorList(0));

        Token.Doctype doctype = new Token.Doctype();
        assertFalse(TreeBuilderState.InHeadNoscript.process(doctype, tb));

        Token.Comment comment = new Token.Comment();
        assertTrue(TreeBuilderState.InHeadNoscript.process(comment, tb));

        Token.Character space = new Token.Character();
        space.data("   ");
        assertTrue(TreeBuilderState.InHeadNoscript.process(space, tb));

        Token.Character nonSpace = new Token.Character();
        nonSpace.data("text");
        assertTrue(TreeBuilderState.InHeadNoscript.process(nonSpace, tb));

        Token.StartTag htmlTag = new Token.StartTag();
        htmlTag.name("html");
        assertTrue(TreeBuilderState.InHeadNoscript.process(htmlTag, tb));

        Token.StartTag basefontTag = new Token.StartTag();
        basefontTag.name("basefont");
        assertTrue(TreeBuilderState.InHeadNoscript.process(basefontTag, tb));

        Token.EndTag noscriptEnd = new Token.EndTag();
        noscriptEnd.name("noscript");
        assertTrue(TreeBuilderState.InHeadNoscript.process(noscriptEnd, tb));
        assertEquals(TreeBuilderState.InHead, tb.state());

        Token.EndTag otherEnd = new Token.EndTag();
        otherEnd.name("p");
        assertTrue(TreeBuilderState.InHeadNoscript.process(otherEnd, tb));
    }

    @Test
    public void testAfterHeadState() throws Exception {
        TreeBuilder tb = new TreeBuilder();
        tb.initial("<html><head></head><body></body></html>", "http://example.com", new ParseErrorList(0));

        Token.Character space = new Token.Character();
        space.data(" \r\n\t");
        assertTrue(TreeBuilderState.AfterHead.process(space, tb));

        Token.Comment comment = new Token.Comment();
        assertTrue(TreeBuilderState.AfterHead.process(comment, tb));

        Token.Doctype doctype = new Token.Doctype();
        assertFalse(TreeBuilderState.AfterHead.process(doctype, tb));

        Token.StartTag htmlTag = new Token.StartTag();
        htmlTag.name("html");
        assertTrue(TreeBuilderState.AfterHead.process(htmlTag, tb));

        Token.StartTag bodyTag = new Token.StartTag();
        bodyTag.name("body");
        assertTrue(TreeBuilderState.AfterHead.process(bodyTag, tb));
        assertEquals(TreeBuilderState.InBody, tb.state());

        tb.transition(TreeBuilderState.AfterHead);
        Token.StartTag framesetTag = new Token.StartTag();
        framesetTag.name("frameset");
        assertTrue(TreeBuilderState.AfterHead.process(framesetTag, tb));
        assertEquals(TreeBuilderState.InFrameset, tb.state());

        tb.transition(TreeBuilderState.AfterHead);
        Token.StartTag headTag = new Token.StartTag();
        headTag.name("head");
        assertTrue(TreeBuilderState.AfterHead.process(headTag, tb));

        tb.transition(TreeBuilderState.AfterHead);
        Token.EndTag endTag = new Token.EndTag();
        endTag.name("body");
        assertTrue(TreeBuilderState.AfterHead.process(endTag, tb));

        tb.transition(TreeBuilderState.AfterHead);
        Token.StartTag anyOther = new Token.StartTag();
        anyOther.name("p");
        assertTrue(TreeBuilderState.AfterHead.process(anyOther, tb));
        assertEquals(TreeBuilderState.InBody, tb.state());
    }

    @Test
    public void testInBodyState() throws Exception {
        TreeBuilder tb = new TreeBuilder();
        tb.initial("<html><head></head><body></body></html>", "http://example.com", new ParseErrorList(0));

        Token.Character nullChar = new Token.Character();
        nullChar.data("\u0000");
        assertTrue(TreeBuilderState.InBody.process(nullChar, tb));

        Token.Character space = new Token.Character();
        space.data("   ");
        assertTrue(TreeBuilderState.InBody.process(space, tb));

        Token.Comment comment = new Token.Comment();
        assertTrue(TreeBuilderState.InBody.process(comment, tb));

        Token.Doctype doctype = new Token.Doctype();
        assertFalse(TreeBuilderState.InBody.process(doctype, tb));

        Token.StartTag htmlTag = new Token.StartTag();
        htmlTag.name("html");
        assertTrue(TreeBuilderState.InBody.process(htmlTag, tb));

        Token.StartTag bodyTag = new Token.StartTag();
        bodyTag.name("body");
        assertTrue(TreeBuilderState.InBody.process(bodyTag, tb));

        Token.StartTag framesetTag = new Token.StartTag();
        framesetTag.name("frameset");
        assertTrue(TreeBuilderState.InBody.process(framesetTag, tb));

        Token.EOF eof = new Token.EOF();
        assertTrue(TreeBuilderState.InBody.process(eof, tb));

        Token.EndTag bodyEnd = new Token.EndTag();
        bodyEnd.name("body");
        assertTrue(TreeBuilderState.InBody.process(bodyEnd, tb));
        assertEquals(TreeBuilderState.AfterBody, tb.state());

        tb.transition(TreeBuilderState.InBody);
        Token.EndTag htmlEnd = new Token.EndTag();
        htmlEnd.name("html");
        assertTrue(TreeBuilderState.InBody.process(htmlEnd, tb));

        tb.transition(TreeBuilderState.InBody);
        Token.StartTag pTag = new Token.StartTag();
        pTag.name("p");
        assertTrue(TreeBuilderState.InBody.process(pTag, tb));

        tb.transition(TreeBuilderState.InBody);
        Token.StartTag addressTag = new Token.StartTag();
        addressTag.name("address");
        assertTrue(TreeBuilderState.InBody.process(addressTag, tb));

        tb.transition(TreeBuilderState.InBody);
        Token.StartTag divTag = new Token.StartTag();
        divTag.name("div");
        assertTrue(TreeBuilderState.InBody.process(divTag, tb));

        tb.transition(TreeBuilderState.InBody);
        Token.StartTag formTag = new Token.StartTag();
        formTag.name("form");
        assertTrue(TreeBuilderState.InBody.process(formTag, tb));

        tb.transition(TreeBuilderState.InBody);
        Token.StartTag liTag = new Token.StartTag();
        liTag.name("li");
        assertTrue(TreeBuilderState.InBody.process(liTag, tb));

        tb.transition(TreeBuilderState.InBody);
        Token.StartTag aTag = new Token.StartTag();
        aTag.name("a");
        assertTrue(TreeBuilderState.InBody.process(aTag, tb));

        tb.transition(TreeBuilderState.InBody);
        Token.StartTag bTag = new Token.StartTag();
        bTag.name("b");
        assertTrue(TreeBuilderState.InBody.process(bTag, tb));

        tb.transition(TreeBuilderState.InBody);
        Token.StartTag tableTag = new Token.StartTag();
        tableTag.name("table");
        assertTrue(TreeBuilderState.InBody.process(tableTag, tb));
        assertEquals(TreeBuilderState.InTable, tb.state());

        tb.transition(TreeBuilderState.InBody);
        Token.StartTag inputTag = new Token.StartTag();
        inputTag.name("input");
        assertTrue(TreeBuilderState.InBody.process(inputTag, tb));

        tb.transition(TreeBuilderState.InBody);
        Token.StartTag hrTag = new Token.StartTag();
        hrTag.name("hr");
        assertTrue(TreeBuilderState.InBody.process(hrTag, tb));

        tb.transition(TreeBuilderState.InBody);
        Token.StartTag imageTag = new Token.StartTag();
        imageTag.name("image");
        assertTrue(TreeBuilderState.InBody.process(imageTag, tb));

        tb.transition(TreeBuilderState.InBody);
        Token.StartTag textareaTag = new Token.StartTag();
        textareaTag.name("textarea");
        assertTrue(TreeBuilderState.InBody.process(textareaTag, tb));
        assertEquals(TreeBuilderState.Text, tb.state());
    }

    @Test
    public void testTextState() throws Exception {
        TreeBuilder tb = new TreeBuilder();
        tb.initial("<html><head><title>Test</title></head></html>", "http://example.com", new ParseErrorList(0));
        tb.transition(TreeBuilderState.Text);

        Token.Character charToken = new Token.Character();
        charToken.data("Hello");
        assertTrue(TreeBuilderState.Text.process(charToken, tb));

        Token.EOF eof = new Token.EOF();
        assertTrue(TreeBuilderState.Text.process(eof, tb));

        Token.EndTag endTag = new Token.EndTag();
        endTag.name("title");
        assertTrue(TreeBuilderState.Text.process(endTag, tb));
    }

    @Test
    public void testInTableState() throws Exception {
        TreeBuilder tb = new TreeBuilder();
        tb.initial("<html><body><table></table></body></html>", "http://example.com", new ParseErrorList(0));
        tb.transition(TreeBuilderState.InTable);

        Token.Character charToken = new Token.Character();
        charToken.data("abc");
        assertTrue(TreeBuilderState.InTable.process(charToken, tb));

        Token.Comment comment = new Token.Comment();
        assertTrue(TreeBuilderState.InTable.process(comment, tb));

        Token.Doctype doctype = new Token.Doctype();
        assertTrue(TreeBuilderState.InTable.process(doctype, tb));

        Token.StartTag tableTag = new Token.StartTag();
        tableTag.name("table");
        assertTrue(TreeBuilderState.InTable.process(tableTag, tb));

        Token.EndTag tableEnd = new Token.EndTag();
        tableEnd.name("table");
        assertTrue(TreeBuilderState.InTable.process(tableEnd, tb));
        assertEquals(TreeBuilderState.InBody, tb.state());

        tb.transition(TreeBuilderState.InTable);
        Token.StartTag bodyTag = new Token.StartTag();
        bodyTag.name("body");
        assertTrue(TreeBuilderState.InTable.process(bodyTag, tb));

        tb.transition(TreeBuilderState.InTable);
        Token.StartTag trTag = new Token.StartTag();
        trTag.name("tr");
        assertTrue(TreeBuilderState.InTable.process(trTag, tb));
        assertEquals(TreeBuilderState.InRow, tb.state());

        tb.transition(TreeBuilderState.InTable);
        Token.StartTag inputTag = new Token.StartTag();
        inputTag.name("input");
        assertTrue(TreeBuilderState.InTable.process(inputTag, tb));

        tb.transition(TreeBuilderState.InTable);
        Token.StartTag formTag = new Token.StartTag();
        formTag.name("form");
        assertTrue(TreeBuilderState.InTable.process(formTag, tb));

        tb.transition(TreeBuilderState.InTable);
        Token.EOF eof = new Token.EOF();
        assertTrue(TreeBuilderState.InTable.process(eof, tb));
    }

    @Test
    public void testInCaptionState() throws Exception {
        TreeBuilder tb = new TreeBuilder();
        tb.initial("<html><body><table><caption></caption></table></body></html>", "http://example.com", new ParseErrorList(0));
        tb.transition(TreeBuilderState.InCaption);

        Token.EndTag captionEnd = new Token.EndTag();
        captionEnd.name("caption");
        assertTrue(TreeBuilderState.InCaption.process(captionEnd, tb));
        assertEquals(TreeBuilderState.InTable, tb.state());

        tb.transition(TreeBuilderState.InCaption);
        Token.StartTag colTag = new Token.StartTag();
        colTag.name("col");
        assertTrue(TreeBuilderState.InCaption.process(colTag, tb));

        tb.transition(TreeBuilderState.InCaption);
        Token.EndTag tableEnd = new Token.EndTag();
        tableEnd.name("table");
        assertTrue(TreeBuilderState.InCaption.process(tableEnd, tb));
        assertEquals(TreeBuilderState.InTable, tb.state());

        tb.transition(TreeBuilderState.InCaption);
        Token.StartTag anyOther = new Token.StartTag();
        anyOther.name("p");
        assertTrue(TreeBuilderState.InCaption.process(anyOther, tb));
    }

    @Test
    public void testInCellState() throws Exception {
        TreeBuilder tb = new TreeBuilder();
        tb.initial("<html><body><table><tr><td></td></tr></table></body></html>", "http://example.com", new ParseErrorList(0));
        tb.transition(TreeBuilderState.InCell);

        Token.EndTag tdEnd = new Token.EndTag();
        tdEnd.name("td");
        assertTrue(TreeBuilderState.InCell.process(tdEnd, tb));
        assertEquals(TreeBuilderState.InRow, tb.state());

        tb.transition(TreeBuilderState.InCell);
        Token.StartTag bodyTag = new Token.StartTag();
        bodyTag.name("body");
        assertTrue(TreeBuilderState.InCell.process(bodyTag, tb));

        tb.transition(TreeBuilderState.InCell);
        Token.EndTag tableEnd = new Token.EndTag();
        tableEnd.name("table");
        assertTrue(TreeBuilderState.InCell.process(tableEnd, tb));
        assertEquals(TreeBuilderState.InBody, tb.state());
    }

    @Test
    public void testInRowState() throws Exception {
        TreeBuilder tb = new TreeBuilder();
        tb.initial("<html><body><table><tr></tr></table></body></html>", "http://example.com", new ParseErrorList(0));
        tb.transition(TreeBuilderState.InRow);

        Token.EndTag trEnd = new Token.EndTag();
        trEnd.name("tr");
        assertTrue(TreeBuilderState.InRow.process(trEnd, tb));
        assertEquals(TreeBuilderState.InTableBody, tb.state());

        tb.transition(TreeBuilderState.InRow);
        Token.StartTag tdTag = new Token.StartTag();
        tdTag.name("td");
        assertTrue(TreeBuilderState.InRow.process(tdTag, tb));
        assertEquals(TreeBuilderState.InCell, tb.state());

        tb.transition(TreeBuilderState.InRow);
        Token.EndTag tableEnd = new Token.EndTag();
        tableEnd.name("table");
        assertTrue(TreeBuilderState.InRow.process(tableEnd, tb));
        assertEquals(TreeBuilderState.InTableBody, tb.state());
    }

    @Test
    public void testAfterBodyState() throws Exception {
        TreeBuilder tb = new TreeBuilder();
        tb.initial("<html><body></body></html>", "http://example.com", new ParseErrorList(0));
        tb.transition(TreeBuilderState.AfterBody);

        Token.Character space = new Token.Character();
        space.data("   ");
        assertTrue(TreeBuilderState.AfterBody.process(space, tb));

        Token.Comment comment = new Token.Comment();
        assertTrue(TreeBuilderState.AfterBody.process(comment, tb));

        Token.Doctype doctype = new Token.Doctype();
        assertTrue(TreeBuilderState.AfterBody.process(doctype, tb));

        Token.StartTag htmlTag = new Token.StartTag();
        htmlTag.name("html");
        assertTrue(TreeBuilderState.AfterBody.process(htmlTag, tb));

        Token.EndTag htmlEnd = new Token.EndTag();
        htmlEnd.name("html");
        assertTrue(TreeBuilderState.AfterBody.process(htmlEnd, tb));
        assertEquals(TreeBuilderState.AfterAfterBody, tb.state());

        tb.transition(TreeBuilderState.AfterBody);
        Token.EOF eof = new Token.EOF();
        assertTrue(TreeBuilderState.AfterBody.process(eof, tb));

        tb.transition(TreeBuilderState.AfterBody);
        Token.StartTag anyOther = new Token.StartTag();
        anyOther.name("p");
        assertTrue(TreeBuilderState.AfterBody.process(anyOther, tb));
        assertEquals(TreeBuilderState.InBody, tb.state());
    }

    @Test
    public void testAfterAfterBodyState() throws Exception {
        TreeBuilder tb = new TreeBuilder();
        tb.initial("<html><body></body></html>", "http://example.com", new ParseErrorList(0));
        tb.transition(TreeBuilderState.AfterAfterBody);

        Token.Comment comment = new Token.Comment();
        assertTrue(TreeBuilderState.AfterAfterBody.process(comment, tb));

        Token.Doctype doctype = new Token.Doctype();
        assertTrue(TreeBuilderState.AfterAfterBody.process(doctype, tb));

        Token.Character space = new Token.Character();
        space.data("   ");
        assertTrue(TreeBuilderState.AfterAfterBody.process(space, tb));

        Token.EOF eof = new Token.EOF();
        assertTrue(TreeBuilderState.AfterAfterBody.process(eof, tb));

        Token.StartTag anyOther = new Token.StartTag();
        anyOther.name("p");
        assertTrue(TreeBuilderState.AfterAfterBody.process(anyOther, tb));
        assertEquals(TreeBuilderState.InBody, tb.state());
    }

    @Test
    public void testInFramesetState() throws Exception {
        TreeBuilder tb = new TreeBuilder();
        tb.initial("<html><frameset></frameset></html>", "http://example.com", new ParseErrorList(0));
        tb.transition(TreeBuilderState.InFrameset);

        Token.Character space = new Token.Character();
        space.data("   ");
        assertTrue(TreeBuilderState.InFrameset.process(space, tb));

        Token.Comment comment = new Token.Comment();
        assertTrue(TreeBuilderState.InFrameset.process(comment, tb));

        Token.Doctype doctype = new Token.Doctype();
        assertFalse(TreeBuilderState.InFrameset.process(doctype, tb));

        Token.StartTag htmlTag = new Token.StartTag();
        htmlTag.name("html");
        assertTrue(TreeBuilderState.InFrameset.process(htmlTag, tb));

        Token.StartTag framesetTag = new Token.StartTag();
        framesetTag.name("frameset");
        assertTrue(TreeBuilderState.InFrameset.process(framesetTag, tb));

        Token.StartTag frameTag = new Token.StartTag();
        frameTag.name("frame");
        assertTrue(TreeBuilderState.InFrameset.process(frameTag, tb));

        Token.StartTag noframesTag = new Token.StartTag();
        noframesTag.name("noframes");
        assertTrue(TreeBuilderState.InFrameset.process(noframesTag, tb));

        Token.EndTag framesetEnd = new Token.EndTag();
        framesetEnd.name("frameset");
        assertTrue(TreeBuilderState.InFrameset.process(framesetEnd, tb));
        assertEquals(TreeBuilderState.AfterFrameset, tb.state());

        tb.transition(TreeBuilderState.InFrameset);
        Token.EOF eof = new Token.EOF();
        assertTrue(TreeBuilderState.InFrameset.process(eof, tb));

        tb.transition(TreeBuilderState.InFrameset);
        Token.StartTag anyOther = new Token.StartTag();
        anyOther.name("p");
        assertTrue(TreeBuilderState.InFrameset.process(anyOther, tb));
    }

    @Test
    public void testAfterFramesetState() throws Exception {
        TreeBuilder tb = new TreeBuilder();
        tb.initial("<html><frameset></frameset></html>", "http://example.com", new ParseErrorList(0));
        tb.transition(TreeBuilderState.AfterFrameset);

        Token.Character space = new Token.Character();
        space.data("   ");
        assertTrue(TreeBuilderState.AfterFrameset.process(space, tb));

        Token.Comment comment = new Token.Comment();
        assertTrue(TreeBuilderState.AfterFrameset.process(comment, tb));

        Token.Doctype doctype = new Token.Doctype();
        assertFalse(TreeBuilderState.AfterFrameset.process(doctype, tb));

        Token.StartTag htmlTag = new Token.StartTag();
        htmlTag.name("html");
        assertTrue(TreeBuilderState.AfterFrameset.process(htmlTag, tb));

        Token.EndTag htmlEnd = new Token.EndTag();
        htmlEnd.name("html");
        assertTrue(TreeBuilderState.AfterFrameset.process(htmlEnd, tb));
        assertEquals(TreeBuilderState.AfterAfterFrameset, tb.state());

        tb.transition(TreeBuilderState.AfterFrameset);
        Token.EOF eof = new Token.EOF();
        assertTrue(TreeBuilderState.AfterFrameset.process(eof, tb));

        tb.transition(TreeBuilderState.AfterFrameset);
        Token.StartTag noframesTag = new Token.StartTag();
        noframesTag.name("noframes");
        assertTrue(TreeBuilderState.AfterFrameset.process(noframesTag, tb));

        tb.transition(TreeBuilderState.AfterFrameset);
        Token.StartTag anyOther = new Token.StartTag();
        anyOther.name("p");
        assertTrue(TreeBuilderState.AfterFrameset.process(anyOther, tb));
    }

    @Test
    public void testAfterAfterFramesetState() throws Exception {
        TreeBuilder tb = new TreeBuilder();
        tb.initial("<html><frameset></frameset></html>", "http://example.com", new ParseErrorList(0));
        tb.transition(TreeBuilderState.AfterAfterFrameset);

        Token.Comment comment = new Token.Comment();
        assertTrue(TreeBuilderState.AfterAfterFrameset.process(comment, tb));

        Token.Doctype doctype = new Token.Doctype();
        assertTrue(TreeBuilderState.AfterAfterFrameset.process(doctype, tb));

        Token.Character space = new Token.Character();
        space.data("   ");
        assertTrue(TreeBuilderState.AfterAfterFrameset.process(space, tb));

        Token.EOF eof = new Token.EOF();
        assertTrue(TreeBuilderState.AfterAfterFrameset.process(eof, tb));

        Token.StartTag frameTag = new Token.StartTag();
        frameTag.name("frame");
        assertTrue(TreeBuilderState.AfterAfterFrameset.process(frameTag, tb));

        Token.StartTag anyOther = new Token.StartTag();
        anyOther.name("p");
        assertTrue(TreeBuilderState.AfterAfterFrameset.process(anyOther, tb));
    }
}