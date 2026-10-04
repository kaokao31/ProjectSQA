package org.jsoup.parser;

import org.junit.Test;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HtmlTreeBuilderStateTest {

    private Document parse(String html) {
        return Jsoup.parse(html, "http://example.com/");
    }

    private HtmlTreeBuilder treeBuilder(String html) {
        Parser parser = Parser.htmlParser();
        parser.parseInput(html, "http://example.com/");
        return (HtmlTreeBuilder) parser.getTreeBuilder();
    }

    private HtmlTreeBuilderState stateAfter(String html) {
        return treeBuilder(html).state();
    }

    @Test
    public void testEmptyDocument() {
        Document doc = parse("");
        assertNotNull(doc);
        assertEquals("", doc.body().text());
    }

    @Test
    public void testCompleteDocument() {
        String html = "<!DOCTYPE html><html><head><title>Title</title></head><body><p>Body</p></body></html>";
        Document doc = parse(html);
        assertEquals("Title", doc.title());
        assertEquals("Body", doc.body().text());
        assertEquals(HtmlTreeBuilderState.AfterAfterBody, stateAfter(html));
    }

    @Test
    public void testLeadingWhitespaceAndComment() {
        Document doc = parse(" \n<!-- comment --><html><head></head><body><p>x</p></body></html>");
        assertEquals("x", doc.body().text());
    }

    @Test
    public void testHeadStates() {
        String html = "<html><head><title>T</title><meta charset=\"utf-8\"><noscript></noscript></head><body><p>B</p></body></html>";
        Document doc = parse(html);
        assertEquals("T", doc.title());
        assertEquals("B", doc.body().text());
        assertEquals(1, doc.select("noscript").size());
    }

    @Test
    public void testBodyFormatting() {
        Document doc = parse("<body>Hello <b>bold</b> and <i>italic</i>");
        assertEquals("Hello bold and italic", doc.body().text());
    }

    @Test
    public void testRcdataElements() {
        Document doc = parse(
                "<html><head><title><b>not bold</b></title></head>" +
                "<body><textarea><i>not italic</i></textarea></body></html>");
        assertEquals("<b>not bold</b>", doc.title());
        assertEquals("<i>not italic</i>", doc.select("textarea").first().text());
    }

    @Test
    public void testScriptRawText() {
        Document doc = parse("<script>if (a < b) { c(); }</script>");
        assertEquals("if (a < b) { c(); }", doc.select("script").first().data());
    }

    @Test
    public void testTableCharacterFosterParenting() {
        Document doc = parse("<table>hello<tr><td>world</td></tr></table>");
        assertTrue(doc.body().html().startsWith("hello<table"));
    }

    @Test
    public void testTableTags() {
        String html = "<table><caption>C</caption><colgroup><col></colgroup>" +
                "<thead><tr><th>H</th></tr></thead><tbody><tr><td>D</td></tr></tbody></table>";
        Document doc = parse(html);
        assertEquals("C", doc.select("caption").first().text());
        assertEquals(1, doc.select("col").size());
        assertEquals("H", doc.select("th").first().text());
        assertEquals("D", doc.select("td").first().text());
    }

    @Test
    public void testTableImplicitTbodyAndRows() {
        Document doc = parse("<table><tr><td>one<tr><td>two</table>");
        assertEquals(2, doc.select("tr").size());
        assertEquals(2, doc.select("td").size());
    }

    @Test
    public void testTableWithNestedTable() {
        Document doc = parse("<table><tr><td>outer<table><tr><td>inner</td></tr></table>");
        assertEquals(2, doc.select("table").size());
    }

    @Test
    public void testSelectStates() {
        Document doc = parse("<select><option>One</option><option>Two</option></select>");
        assertEquals(2, doc.select("option").size());
    }

    @Test
    public void testSelectInTable() {
        Document doc = parse("<table><tr><td><select><option>One</option></select></td></tr></table>");
        assertEquals(1, doc.select("select").size());
        assertEquals(1, doc.select("option").size());
    }

    @Test
    public void testFramesetState() {
        Document doc = parse("<html><frameset cols=\"*\"><frame src=\"a\"><frame src=\"b\"></frameset></html>");
        assertEquals(2, doc.select("frame").size());
    }

    @Test
    public void testForeignContent() {
        Document doc = parse("<svg><circle cx=\"1\" r=\"2\"></circle></svg>");
        assertEquals(1, doc.select("circle").size());
    }

    @Test
    public void testNestedAnchors() {
        Document doc = parse("<a href=\"1\">one<a href=\"2\">two</a>");
        Elements anchors = doc.select("a");
        assertEquals(2, anchors.size());
        assertEquals("one", anchors.get(0).text());
        assertEquals("two", anchors.get(1).text());
    }

    @Test
    public void testNestedFormIgnored() {
        Document doc = parse("<form id=\"one\"><form id=\"two\"><input></form>");
        assertEquals(1, doc.select("form").size());
    }

    @Test
    public void testListItemAutoClosure() {
        Document doc = parse("<ul><li>one<li>two</ul>");
        assertEquals(2, doc.select("li").size());
    }

    @Test
    public void testParagraphEndTagWithoutOpen() {
        Document doc = parse("</p>after");
        assertEquals("after", doc.body().text());
        assertEquals(1, doc.select("p").size());
    }

    @Test
    public void testVoidTagsAndSelfClosing() {
        Document doc = parse("<div><br><hr><img src=x></div>");
        assertEquals(1, doc.select("br").size());
        assertEquals(1, doc.select("hr").size());
        assertEquals(1, doc.select("img").size());
    }

    @Test
    public void testUnexpectedEndTags() {
        Document doc = parse("</div><p>text</span>");
        assertEquals("text", doc.body().text());
    }

    @Test
    public void testCharacterReferences() {
        Document doc = parse("<div>&amp;&lt;&gt;&quot;&apos;</div>");
        assertEquals("&<>\"'", doc.select("div").first().text());
    }

    @Test
    public void testNullCharacterReplacement() {
        Document doc = parse("<div>a\0b</div>");
        assertEquals("a\uFFFDb", doc.select("div").first().text());
    }

    @Test
    public void testImageTagHandling() {
        Document doc = parse("<image src=x>");
        assertEquals("img", doc.select("img").first().tagName());
    }

    @Test
    public void testStateAfterCompleteDocument() {
        assertEquals(HtmlTreeBuilderState.AfterAfterBody,
                stateAfter("<!DOCTYPE html><html><head></head><body></body></html>"));
    }
}