package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class HtmlTreeBuilderTest {

    @Test
    public void testSimpleDocument() {
        Document doc = Jsoup.parse("<html><head><title>T</title></head><body><p>Hello</p></body></html>");
        assertEquals("T", doc.title());
        assertEquals("Hello", doc.body().text());
    }

    @Test
    public void testParagraphsAutoClose() {
        Document doc = Jsoup.parse("<p>One<p>Two");
        Elements ps = doc.select("p");
        assertEquals(2, ps.size());
        assertEquals("One", ps.get(0).text());
        assertEquals("Two", ps.get(1).text());
    }

    @Test
    public void testListItemsAreSiblings() {
        Document doc = Jsoup.parse("<ul><li>One<li>Two</ul>");
        assertEquals(2, doc.select("ul > li").size());
        assertEquals("One", doc.select("li").get(0).text());
        assertEquals("Two", doc.select("li").get(1).text());
    }

    @Test
    public void testNestedTableInTableCell() {
        Document doc = Jsoup.parse("<table><tr><td><table><tr><td>inner</td></tr></table></td></tr></table>");
        assertEquals(2, doc.select("table").size());
        assertEquals(1, doc.select("table").first().select("table").size());
        assertEquals("inner", doc.select("table").get(1).text());
    }

    @Test
    public void testTableFosterParenting() {
        Document doc = Jsoup.parse("<table>text<tr><td>cell</td></tr></table>");
        assertEquals("cell", doc.select("table").first().text());
    }

    @Test
    public void testSelectOptionsAreSiblings() {
        Document doc = Jsoup.parse("<select><option>One<option>Two</select>");
        assertEquals(2, doc.select("select > option").size());
        assertEquals("One", doc.select("option").get(0).text());
        assertEquals("Two", doc.select("option").get(1).text());
    }

    @Test
    public void testFormattingElementsAdoptionAgency() {
        Document doc = Jsoup.parse("<b><i>bold</b>tail</i>");
        Elements b = doc.select("b");
        assertEquals(1, b.size());
        assertEquals("bold", b.first().text());

        Element next = b.first().nextElementSibling();
        assertNotNull(next);
        assertEquals("i", next.tagName());
        assertEquals("tail", next.text());
    }

    @Test
    public void testRawTextElementsPreserveMarkup() {
        Document doc = Jsoup.parse("<textarea><b>not bold</b></textarea>");
        assertEquals("<b>not bold</b>", doc.select("textarea").val());
    }

    @Test
    public void testUnknownTagsAndAttributes() {
        Document doc = Jsoup.parse("<body><foo bar='baz'>qux</foo></body>");
        Element foo = doc.select("foo").first();
        assertNotNull(foo);
        assertEquals("qux", foo.text());
        assertEquals("baz", foo.attr("bar"));
    }

    @Test
    public void testSelfClosingTags() {
        Document doc = Jsoup.parse("<div><br/><img src=\"x.png\"/></div>");
        assertEquals(1, doc.select("br").size());
        assertEquals(1, doc.select("img").size());
        assertEquals("x.png", doc.select("img").first().attr("src"));
    }

    @Test
    public void testButtonClosesPreviousButton() {
        Document doc = Jsoup.parse("<button>one<button>two");
        assertEquals(2, doc.select("button").size());
        assertEquals("one", doc.select("button").get(0).text());
        assertEquals("two", doc.select("button").get(1).text());
    }

    @Test
    public void testDeeplyNestedElements() {
        int depth = 1000;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append("<div>");
        }
        sb.append("x");
        for (int i = 0; i < depth; i++) {
            sb.append("</div>");
        }

        Document doc = Jsoup.parse(sb.toString());
        assertEquals(depth, doc.select("div").size());
        assertEquals("x", doc.body().text());
    }

    @Test
    public void testHtmlTreeBuilderDirectParse() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Document doc = tb.parse(
            "<html><body><p>direct</p></body></html>",
            "http://example.com/",
            ParseErrorList.noTracking());
        assertEquals("direct", doc.body().text());
    }

    @Test
    public void testFragmentInTableContext() {
        String baseUri = "http://example.com/";
        Element context = new Element(Tag.valueOf("tbody"), baseUri);
        HtmlTreeBuilder tb = new HtmlTreeBuilder();

        List<Node> nodes = tb.parseFragment(
            "<tr><td>cell</td></tr>",
            context,
            baseUri,
            ParseErrorList.noTracking());

        assertEquals(1, nodes.size());
        Element tr = (Element) nodes.get(0);
        assertEquals("tr", tr.tagName());
        assertEquals("cell", tr.text());
    }

    @Test
    public void testFragmentInSelectContext() {
        String baseUri = "http://example.com/";
        Element context = new Element(Tag.valueOf("select"), baseUri);
        HtmlTreeBuilder tb = new HtmlTreeBuilder();

        List<Node> nodes = tb.parseFragment(
            "<option>One<option>Two",
            context,
            baseUri,
            ParseErrorList.noTracking());

        assertEquals(2, nodes.size());
        assertEquals("One", ((Element) nodes.get(0)).text());
        assertEquals("Two", ((Element) nodes.get(1)).text());
    }
}