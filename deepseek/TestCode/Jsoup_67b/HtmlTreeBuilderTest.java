package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class HtmlTreeBuilderTest {

    @Test
    public void treeBuilderUsesHtmlDefaultSettings() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        assertNotNull(builder);
        assertSame(ParseSettings.htmlDefault, builder.defaultSettings());
    }

    @Test
    public void parsesSimpleDocument() {
        Document doc = Jsoup.parse("<html><head><title>Hello</title></head><body><p>World</p></body></html>");
        assertEquals("Hello", doc.title());
        assertEquals("World", doc.body().text());
    }

    @Test
    public void parsesEmptyAndBlankDocuments() {
        Document doc = Jsoup.parse("");
        assertNotNull(doc.body());
        assertEquals("", doc.body().text());

        Document blank = Jsoup.parse("   \n\t ");
        assertNotNull(blank.body());
        assertEquals("", blank.body().text());
    }

    @Test
    public void parsesOptionalEndTags() {
        Document doc = Jsoup.parse("<p>One<p>Two<ul><li>Three<li>Four</ul>");
        assertEquals(2, doc.select("p").size());
        assertEquals("One", doc.select("p").get(0).text());
        assertEquals("Two", doc.select("p").get(1).text());
        assertEquals(2, doc.select("li").size());
        assertEquals("Three", doc.select("li").get(0).text());
        assertEquals("Four", doc.select("li").get(1).text());
    }

    @Test
    public void parsesMisnestedFormattingElements() {
        Document doc = Jsoup.parse("<b><i>bold italic</b> plain</i>");
        String text = doc.body().text();
        assertTrue(text.contains("bold italic"));
        assertTrue(text.contains("plain"));
    }

    @Test
    public void parsesFormattingElementsAcrossParagraphs() {
        Document doc = Jsoup.parse("<p><b>One</p><p>Two</p>");
        assertEquals(2, doc.select("p").size());
        assertEquals("One", doc.select("p").get(0).text());
        assertEquals("Two", doc.select("p").get(1).text());
    }

    @Test
    public void parsesLists() {
        Document doc = Jsoup.parse("<ul><li>One<li>Two</ul>");
        assertEquals(2, doc.select("li").size());
        assertEquals("One", doc.select("li").get(0).text());
        assertEquals("Two", doc.select("li").get(1).text());
    }

    @Test
    public void parsesNestedOrderedLists() {
        Document doc = Jsoup.parse("<ol><li>One<ul><li>Nested</li></ul></li><li>Two</li></ol>");
        assertEquals(2, doc.select("ol > li").size());
        assertEquals(1, doc.select("ul > li").size());
        assertEquals("Nested", doc.select("ul > li").first().text());
    }

    @Test
    public void parsesTables() {
        Document doc = Jsoup.parse("<table><tr><td>A</td><td>B</td></tr></table>");
        assertEquals(1, doc.select("table").size());
        assertEquals(1, doc.select("tr").size());
        assertEquals(2, doc.select("td").size());
        assertEquals("A", doc.select("td").get(0).text());
        assertEquals("B", doc.select("td").get(1).text());
    }

    @Test
    public void parsesTableWithFosterParentedText() {
        Document doc = Jsoup.parse("<table>before<tr><td>cell</td></tr>after</table>");
        assertEquals(1, doc.select("table").size());
        assertNotNull(doc.select("td").first());
        String bodyText = doc.body().text();
        assertTrue(bodyText.contains("before"));
        assertTrue(bodyText.contains("cell"));
        assertTrue(bodyText.contains("after"));
    }

    @Test
    public void parsesTableWithImplicitCell() {
        Document doc = Jsoup.parse("<table><td>cell</td></table>");
        assertEquals(1, doc.select("td").size());
        assertEquals("cell", doc.select("td").first().text());
    }

    @Test
    public void parsesFrameset() {
        Document doc = Jsoup.parse("<html><head><title>F</title></head><frameset cols=\"*\"><frame src=\"a.html\"></frameset></html>");
        assertEquals(1, doc.select("frameset").size());
        assertEquals(1, doc.select("frame").size());
    }

    @Test
    public void parsesFormsAndInputs() {
        Document doc = Jsoup.parse("<form action=\"/submit\"><input name=\"q\" type=\"text\"><button>Go</button></form>");
        assertEquals(1, doc.select("form").size());
        assertEquals(1, doc.select("input").size());
        assertEquals(1, doc.select("button").size());
    }

    @Test
    public void parsesSelectOptions() {
        Document doc = Jsoup.parse("<select><option>One</option><option selected>Two</option></select>");
        assertEquals(2, doc.select("option").size());
        assertEquals("One", doc.select("option").get(0).text());
        assertEquals("Two", doc.select("option").get(1).text());
        assertTrue(doc.select("option").get(1).hasAttr("selected"));
    }

    @Test
    public void parsesScriptData() {
        Document doc = Jsoup.parse("<script>if (a < b && c > d) { alert(\"x&y\"); }</script>");
        Elements scripts = doc.select("script");
        assertEquals(1, scripts.size());
        assertTrue(scripts.first().data().contains("a < b"));
        assertTrue(scripts.first().data().contains("x&y"));
    }

    @Test
    public void parsesStyleData() {
        Document doc = Jsoup.parse("<style>body { color: red; }</style>");
        Elements styles = doc.select("style");
        assertEquals(1, styles.size());
        assertTrue(styles.first().data().contains("color: red"));
    }

    @Test
    public void parsesPreAndTextarea() {
        Document doc = Jsoup.parse("<pre>line1\nline2</pre><textarea>line1\nline2</textarea>");
        assertNotNull(doc.select("pre").first());
        assertNotNull(doc.select("textarea").first());
        assertTrue(doc.select("pre").first().text().contains("line1"));
        assertTrue(doc.select("textarea").first().text().contains("line1"));
    }

    @Test
    public void parsesEntitiesInText() {
        Document doc = Jsoup.parse("<p>&amp; &lt; &gt; &quot; &copy; &euro;</p>");
        String text = doc.body().text();
        assertTrue(text.contains("&"));
        assertTrue(text.contains("<"));
        assertTrue(text.contains(">"));
        assertTrue(text.contains("\""));
        assertTrue(text.contains("\u00A9"));
        assertTrue(text.contains("\u20AC"));
    }

    @Test
    public void parsesEntitiesInAttributes() {
        Document doc = Jsoup.parse("<a href=\"?a=1&amp;b=2\">x</a>");
        assertEquals("?a=1&b=2", doc.select("a").first().attr("href"));
    }

    @Test
    public void parsesComments() {
        Document doc = Jsoup.parse("<!-- main comment --><p>text</p>");
        assertEquals(1, doc.select("p").size());
        assertEquals("text", doc.select("p").first().text());
    }

    @Test
    public void parsesDoctype() {
        Document doc = Jsoup.parse("<!DOCTYPE html><html><body>doc</body></html>");
        assertTrue(doc.outerHtml().toLowerCase().contains("<!doctype html>"));
        assertEquals("doc", doc.body().text());
    }

    @Test
    public void parsesAttributes() {
        Document doc = Jsoup.parse("<div id=\"one\" class=\"two\" data-x=\"v\">text</div>");
        Element div = doc.select("div").first();
        assertNotNull(div);
        assertEquals("one", div.id());
        assertEquals("two", div.className());
        assertEquals("v", div.attr("data-x"));
    }

    @Test
    public void parsesSelfClosingTags() {
        Document doc = Jsoup.parse("<img src=\"a.png\"><br><hr>");
        assertEquals(1, doc.select("img").size());
        assertEquals(1, doc.select("br").size());
        assertEquals(1, doc.select("hr").size());
    }

    @Test
    public void parsesCustomTags() {
        Document doc = Jsoup.parse("<custom>hello</custom><foo-bar>baz</foo-bar>");
        assertEquals("hello", doc.select("custom").first().text());
        assertEquals("baz", doc.select("foo-bar").first().text());
    }

    @Test
    public void handlesRogueEndTagBr() {
        Document doc = Jsoup.parse("</br>");
        assertEquals(1, doc.select("br").size());
    }

    @Test
    public void handlesUnexpectedEndTags() {
        Document doc = Jsoup.parse("</p></div>text");
        assertEquals("text", doc.body().text());
    }

    @Test
    public void parsesDeeplyNestedTags() {
        StringBuilder html = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            html.append("<div>");
        }
        html.append("deep");
        for (int i = 0; i < 200; i++) {
            html.append("</div>");
        }
        Document doc = Jsoup.parse(html.toString());
        assertEquals(200, doc.select("div").size());
        assertTrue(doc.body().text().contains("deep"));
    }

    @Test
    public void parsesManyFormattingElements() {
        StringBuilder html = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            html.append("<b>");
        }
        html.append("x");
        for (int i = 0; i < 50; i++) {
            html.append("</b>");
        }
        Document doc = Jsoup.parse(html.toString());
        assertEquals("x", doc.body().text());
    }

    @Test
    public void parsesLargeText() {
        String text = repeat('a', 10000);
        Document doc = Jsoup.parse("<p>" + text + "</p>");
        assertEquals(10000, doc.select("p").first().text().length());
    }

    @Test
    public void parsesFragment() {
        List<Node> nodes = Parser.parseFragment("<p>Hello</p>", null, "http://example.com");
        assertNotNull(nodes);
        assertEquals(1, nodes.size());
        assertEquals("p", nodes.get(0).nodeName());
    }

    @Test
    public void parsesBodyFragment() {
        Element context = Jsoup.parse("<div></div>").body();
        List<Node> nodes = Parser.parseFragment("<p>Hello</p>", context, "http://example.com");
        assertNotNull(nodes);
        assertEquals(1, nodes.size());
        assertEquals("p", nodes.get(0).nodeName());
    }

    @Test
    public void handlesTextAfterTable() {
        Document doc = Jsoup.parse("<table><tr><td>cell</td></tr></table>after");
        assertTrue(doc.body().text().contains("cell"));
        assertTrue(doc.body().text().contains("after"));
    }

    @Test
    public void handlesFormattingAndListClosures() {
        Document doc = Jsoup.parse("<ul><li>one<li><b>two</b></li><li>three</li></ul>");
        assertEquals(3, doc.select("li").size());
        assertEquals("one", doc.select("li").get(0).text());
        assertEquals("two", doc.select("li").get(1).text());
        assertEquals("three", doc.select("li").get(2).text());
    }

    @Test
    public void handlesNestedTables() {
        Document doc = Jsoup.parse("<table><tr><td>outer<table><tr><td>inner</td></tr></table></td></tr></table>");
        assertEquals(2, doc.select("table").size());
        assertEquals(2, doc.select("td").size());
        assertTrue(doc.body().text().contains("outer"));
        assertTrue(doc.body().text().contains("inner"));
    }

    private static String repeat(char c, int count) {
        char[] chars = new char[count];
        for (int i = 0; i < count; i++) {
            chars[i] = c;
        }
        return new String(chars);
    }
}