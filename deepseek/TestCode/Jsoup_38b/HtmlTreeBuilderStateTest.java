package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HtmlTreeBuilderStateTest {

    @Test
    public void parsesEmptyDocument() {
        Document doc = Jsoup.parse("");
        assertNotNull(doc);
        assertNotNull(doc.body());
        assertEquals("", doc.body().text());
    }

    @Test
    public void handlesInitialDoctype() {
        Document doc = Jsoup.parse("<!DOCTYPE html><html><body>Hello</body></html>");
        assertNotNull(doc.documentType());
        assertEquals("html", doc.documentType().attr("name"));
        assertEquals("Hello", doc.body().text());
    }

    @Test
    public void handlesTextBeforeHtml() {
        Document doc = Jsoup.parse("Hello");
        assertEquals("Hello", doc.body().text());
    }

    @Test
    public void handlesBasicBodyContent() {
        Document doc = Jsoup.parse("<p>Hello world</p>");
        assertEquals("Hello world", doc.select("p").first().text());
    }

    @Test
    public void imageTagInHtmlIsConvertedToImg() {
        Document doc = Jsoup.parse("<image src=\"test.png\">");
        assertEquals(1, doc.getElementsByTag("img").size());
        assertEquals(0, doc.getElementsByTag("image").size());
    }

    @Test
    public void imageTagInSvgIsNotConvertedToImg() {
        Document doc = Jsoup.parse("<svg><image xlink:href=\"test.png\"></image></svg>");
        assertEquals(1, doc.getElementsByTag("image").size());
        assertEquals(0, doc.getElementsByTag("img").size());
    }

    @Test
    public void tableGetsImplicitTbody() {
        Document doc = Jsoup.parse("<table><tr><td>Cell</td></tr></table>");
        Element table = doc.select("table").first();
        assertEquals("tbody", table.child(0).tagName());
        assertEquals("Cell", doc.select("td").first().text());
    }

    @Test
    public void tableIgnoresMisplacedText() {
        Document doc = Jsoup.parse("<table>Forgotten<tr><td>Cell</td></tr></table>");
        assertTrue(doc.body().text().contains("Cell"));
        assertFalse(doc.body().text().contains("Forgotten"));
    }

    @Test
    public void captionHandledInTable() {
        Document doc = Jsoup.parse("<table><caption>Cap</caption><tr><td>Cell</td></tr></table>");
        assertEquals("Cap", doc.select("caption").first().text());
        assertEquals("Cell", doc.select("td").first().text());
    }

    @Test
    public void colgroupHandledInTable() {
        Document doc = Jsoup.parse("<table><colgroup><col style=\"width:10px\"></colgroup><tr><td>Cell</td></tr></table>");
        assertEquals(1, doc.select("col").size());
        assertEquals("Cell", doc.select("td").first().text());
    }

    @Test
    public void tableSectionsHandled() {
        Document doc = Jsoup.parse("<table><thead><tr><th>Head</th></tr></thead><tbody><tr><td>Body</td></tr></tbody><tfoot><tr><td>Foot</td></tr></tfoot></table>");
        assertEquals(1, doc.select("thead").size());
        assertEquals(1, doc.select("tbody").size());
        assertEquals(1, doc.select("tfoot").size());
    }

    @Test
    public void selectHandled() {
        Document doc = Jsoup.parse("<select><option>One</option><option>Two</option></select>");
        assertEquals(2, doc.select("option").size());
    }

    @Test
    public void scriptContentPreserved() {
        Document doc = Jsoup.parse("<script>if (a < b) run();</script>");
        Element script = doc.select("script").first();
        assertNotNull(script);
        assertTrue(script.data().contains("a < b"));
    }

    @Test
    public void styleContentPreserved() {
        Document doc = Jsoup.parse("<style>body { color: red }</style>");
        Element style = doc.select("style").first();
        assertNotNull(style);
        assertTrue(style.data().contains("color"));
    }

    @Test
    public void pTagAutoCloses() {
        Document doc = Jsoup.parse("<p>One<p>Two");
        assertEquals(2, doc.select("p").size());
    }

    @Test
    public void liTagAutoCloses() {
        Document doc = Jsoup.parse("<ul><li>One<li>Two</ul>");
        assertEquals(2, doc.select("li").size());
    }

    @Test
    public void unknownTagsPreserved() {
        Document doc = Jsoup.parse("<custom><data>Hello</data></custom>");
        assertEquals(1, doc.select("custom").size());
        assertEquals("Hello", doc.select("custom").first().text());
    }

    @Test
    public void tagNamesLowercased() {
        Document doc = Jsoup.parse("<DIV><P>Hello</P></DIV>");
        assertEquals(1, doc.select("div").size());
        assertEquals("p", doc.select("p").first().tagName());
    }

    @Test
    public void attrNormalized() {
        Document doc = Jsoup.parse("<div CLASS=\"foo\">Hello</div>");
        assertEquals("foo", doc.select("div").first().attr("class"));
    }

    @Test
    public void entitiesDecoded() {
        Document doc = Jsoup.parse("<p>&amp; &lt; &gt; &quot; &apos;</p>");
        assertEquals("& < > \" '", doc.select("p").text());
    }

    @Test
    public void framesetHandled() {
        Document doc = Jsoup.parse("<html><frameset><frame src=\"a.html\"></frameset></html>");
        assertEquals(1, doc.select("frameset").size());
        assertEquals(1, doc.select("frame").size());
    }

    @Test
    public void afterBodyWhitespaceIgnored() {
        Document doc = Jsoup.parse("<html><body>Hello</body>   </html>");
        assertEquals("Hello", doc.body().text());
    }

    @Test
    public void directImageTagProcessedByInBodyState() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.parse("<html><body>", "http://example.com/", ParseErrorList.noTracking());
        tb.state(HtmlTreeBuilderState.InBody);

        Token.StartTag image = new Token.StartTag();
        image.name("image").attr("src", "test.png");

        boolean processed = HtmlTreeBuilderState.InBody.process(image, tb);
        assertTrue(processed);
        assertEquals("img", image.name());
    }
}