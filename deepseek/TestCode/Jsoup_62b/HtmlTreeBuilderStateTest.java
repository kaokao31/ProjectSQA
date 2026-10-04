package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HtmlTreeBuilderStateTest {

    private HtmlTreeBuilder newBuilder() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("", "http://example.com", new ParseErrorList(16, 0));
        return tb;
    }

    private HtmlTreeBuilder treeInBody() {
        HtmlTreeBuilder tb = newBuilder();
        tb.process(new Token.StartTag("html"));
        tb.process(new Token.StartTag("head"));
        tb.process(new Token.EndTag("head"));
        tb.process(new Token.StartTag("body"));
        return tb;
    }

    @Test
    public void initialStateProcessesCommentAndHtmlStartTag() {
        HtmlTreeBuilder tb = newBuilder();
        tb.process(new Token.Comment("hello"));
        assertEquals(1, tb.getDocument().childNodeSize());
        assertTrue(tb.getDocument().childNode(0).outerHtml().contains("hello"));

        tb.process(new Token.StartTag("html"));
        assertEquals(HtmlTreeBuilderState.BeforeHead, tb.state());
    }

    @Test
    public void beforeHeadProcessesHeadStartTag() {
        HtmlTreeBuilder tb = newBuilder();
        tb.transition(HtmlTreeBuilderState.BeforeHead);
        tb.process(new Token.StartTag("head"));
        assertEquals(HtmlTreeBuilderState.InHead, tb.state());
    }

    @Test
    public void inHeadProcessesTitleAndRawText() {
        HtmlTreeBuilder tb = newBuilder();
        tb.process(new Token.StartTag("html"));
        tb.process(new Token.StartTag("head"));
        tb.process(new Token.StartTag("title"));
        tb.process(new Token.Character("My Title"));
        tb.process(new Token.EndTag("title"));

        assertEquals(HtmlTreeBuilderState.InHead, tb.state());
        assertEquals("My Title", tb.getDocument().title());
    }

    @Test
    public void inHeadNoscriptProcessesContent() {
        Document doc = Jsoup.parse(
            "<html><head><noscript><meta charset='utf-8'></noscript></head><body></body></html>");
        assertEquals(1, doc.select("noscript").size());
    }

    @Test
    public void afterHeadProcessesBodyStartTag() {
        HtmlTreeBuilder tb = newBuilder();
        tb.process(new Token.StartTag("html"));
        tb.process(new Token.StartTag("head"));
        tb.process(new Token.EndTag("head"));
        tb.process(new Token.StartTag("body"));

        assertEquals(HtmlTreeBuilderState.InBody, tb.state());
        assertNotNull(tb.getDocument().body());
    }

    @Test
    public void inBodyProcessesParagraphAndText() {
        HtmlTreeBuilder tb = treeInBody();
        tb.process(new Token.StartTag("p"));
        tb.process(new Token.Character("Hello"));
        tb.process(new Token.EndTag("p"));

        assertEquals("Hello", tb.getDocument().body().text());
        assertEquals("p", tb.getDocument().body().children().first().tagName());
    }

    @Test
    public void inBodyHandlesEndBrAsStartBr() {
        HtmlTreeBuilder tb = treeInBody();
        tb.process(new Token.StartTag("p"));
        tb.process(new Token.Character("a"));
        tb.process(new Token.EndTag("br"));
        tb.process(new Token.EndTag("p"));

        assertEquals(1, tb.getDocument().select("br").size());
        assertEquals("a<br>", tb.getDocument().select("p").first().html());
    }

    @Test
    public void textStatePreservesScriptContent() {
        HtmlTreeBuilder tb = newBuilder();
        tb.process(new Token.StartTag("html"));
        tb.process(new Token.StartTag("head"));
        tb.process(new Token.StartTag("script"));
        tb.process(new Token.Character("if (a < b) { x = '</script>'; }"));
        tb.process(new Token.EndTag("script"));

        assertEquals(HtmlTreeBuilderState.InHead, tb.state());
        assertEquals("if (a < b) { x = '</script>'; }",
            tb.getDocument().select("script").first().data());
    }

    @Test
    public void inTableProcessesTableBodyRowAndCell() {
        HtmlTreeBuilder tb = treeInBody();
        tb.process(new Token.StartTag("table"));
        tb.process(new Token.StartTag("tbody"));
        tb.process(new Token.StartTag("tr"));
        tb.process(new Token.StartTag("td"));
        tb.process(new Token.Character("cell"));
        tb.process(new Token.EndTag("td"));
        tb.process(new Token.EndTag("tr"));
        tb.process(new Token.EndTag("tbody"));
        tb.process(new Token.EndTag("table"));

        assertEquals(1, tb.getDocument().select("table").size());
        assertEquals("cell", tb.getDocument().select("td").text());
    }

    @Test
    public void inTableTextHandlesWhitespaceAndCharacters() {
        HtmlTreeBuilder tb = treeInBody();
        tb.process(new Token.StartTag("table"));
        tb.process(new Token.Character("\n  "));
        tb.process(new Token.Character("text"));
        tb.process(new Token.EndTag("table"));

        assertTrue(tb.getDocument().select("table").size() == 1);
    }

    @Test
    public void inCaptionProcessesCaptionContent() {
        HtmlTreeBuilder tb = treeInBody();
        tb.process(new Token.StartTag("table"));
        tb.process(new Token.StartTag("caption"));
        tb.process(new Token.Character("Cap"));
        tb.process(new Token.EndTag("caption"));
        tb.process(new Token.EndTag("table"));

        assertEquals("Cap", tb.getDocument().select("caption").text());
    }

    @Test
    public void inColumnGroupProcessesColumn() {
        HtmlTreeBuilder tb = treeInBody();
        tb.process(new Token.StartTag("colgroup"));
        tb.process(new Token.StartTag("col"));
        tb.process(new Token.EndTag("colgroup"));

        assertEquals(1, tb.getDocument().select("colgroup").size());
        assertEquals(1, tb.getDocument().select("col").size());
    }

    @Test
    public void inSelectProcessesOptions() {
        HtmlTreeBuilder tb = treeInBody();
        tb.process(new Token.StartTag("select"));
        tb.process(new Token.StartTag("option"));
        tb.process(new Token.Character("Choice"));
        tb.process(new Token.EndTag("option"));
        tb.process(new Token.EndTag("select"));

        assertEquals(1, tb.getDocument().select("select").size());
        assertEquals("Choice", tb.getDocument().select("option").text());
    }

    @Test
    public void inSelectInTableParsesSelectInsideTableCell() {
        Document doc = Jsoup.parse(
            "<table><tr><td><select><option>one</option></select></td></tr></table>");
        assertEquals(1, doc.select("select").size());
        assertEquals("one", doc.select("option").text());
    }

    @Test
    public void afterBodyProcessesEndHtml() {
        HtmlTreeBuilder tb = treeInBody();
        tb.process(new Token.EndTag("body"));
        assertEquals(HtmlTreeBuilderState.AfterBody, tb.state());

        tb.process(new Token.EndTag("html"));
        assertEquals(HtmlTreeBuilderState.AfterAfterBody, tb.state());
    }

    @Test
    public void inFramesetProcessesFrames() {
        HtmlTreeBuilder tb = newBuilder();
        tb.process(new Token.StartTag("html"));
        tb.process(new Token.StartTag("head"));
        tb.process(new Token.EndTag("head"));
        tb.process(new Token.StartTag("frameset"));
        tb.process(new Token.StartTag("frame"));
        tb.process(new Token.EndTag("frameset"));

        assertEquals(1, tb.getDocument().select("frameset").size());
        assertEquals(1, tb.getDocument().select("frame").size());
    }

    @Test
    public void afterFramesetProcessesNoframes() {
        Document doc = Jsoup.parse(
            "<html><head></head><frameset><frame src='x'></frameset><noframes><body>No</body></noframes></html>");
        assertEquals(1, doc.select("frameset").size());
        assertEquals(1, doc.select("noframes").size());
    }

    @Test
    public void foreignContentProcessesSvgElements() {
        HtmlTreeBuilder tb = treeInBody();
        tb.process(new Token.StartTag("svg"));
        tb.process(new Token.StartTag("circle"));

        assertEquals(1, tb.getDocument().select("circle").size());
    }

    @Test
    public void typicalDocumentUsesAllMajorStates() {
        Document doc = Jsoup.parse(
            "<!DOCTYPE html><html><head><title>Test</title></head>" +
            "<body><p>Hello</p><table><tr><td>Cell</td></tr></table></body></html>");

        assertNotNull(doc.documentType());
        assertEquals("Test", doc.title());
        assertEquals("Hello", doc.body().text());
        assertEquals(1, doc.select("table").size());
        assertEquals("Cell", doc.select("td").text());
    }

    @Test
    public void nestedTablesAreParsed() {
        Document doc = Jsoup.parse(
            "<table><tr><td>outer<table><tr><td>inner</td></tr></table></td></tr></table>");
        assertEquals(2, doc.select("table").size());
        assertEquals("inner", doc.select("table").get(1).text());
    }

    @Test
    public void templateElementContentIsParsed() {
        Document doc = Jsoup.parse("<template><p>shadow</p></template>");
        assertEquals(1, doc.select("template").size());
        assertEquals("shadow", doc.select("template").first().text());
    }

    @Test
    public void commentsSurviveBodyParsing() {
        Document doc = Jsoup.parse("<html><body>a<!-- comment -->b</body></html>");
        assertTrue(doc.body().html().contains("<!-- comment -->"));
    }

    @Test
    public void paragraphClosureOnParagraphStart() {
        Document doc = Jsoup.parse("<p>one<p>two");
        assertEquals(2, doc.select("p").size());
        assertEquals("onetwo", doc.select("p").text());
    }
}