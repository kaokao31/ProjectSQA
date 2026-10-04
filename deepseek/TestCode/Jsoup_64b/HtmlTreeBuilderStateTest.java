package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HtmlTreeBuilderStateTest {

    @Test
    public void testAllStatesAreDefined() {
        assertTrue(HtmlTreeBuilderState.values().length > 0);
    }

    @Test
    public void testDoctypeAndInitialState() {
        Document doc = Jsoup.parse("<!DOCTYPE html><html><head><title>t</title></head><body>Hello</body></html>");
        assertEquals("t", doc.title());
        assertEquals("Hello", doc.body().text());
        assertTrue(doc.toString().contains("<!DOCTYPE html>"));
    }

    @Test
    public void testBeforeHeadWithWhitespaceAndComment() {
        Document doc = Jsoup.parse("<!--c--><html>  <head><title>T</title></head><body>B</body></html>");
        assertEquals("T", doc.title());
        assertEquals("B", doc.body().text());
    }

    @Test
    public void testInHeadMetaBaseLink() {
        Document doc = Jsoup.parse("<html><head><meta charset=\"utf-8\"><base href=\"http://example.com/\">"
                + "<link rel=\"stylesheet\" href=\"a.css\"><title>Title</title></head><body>Body</body></html>");
        assertEquals("http://example.com/", doc.select("base").first().attr("href"));
        assertEquals("a.css", doc.select("link").first().attr("href"));
    }

    @Test
    public void testInHeadStyleAndScript() {
        Document doc = Jsoup.parse("<html><head><style>a{}</style><script>var x = 1;</script></head><body></body></html>");
        assertEquals("a{}", doc.select("style").first().data());
        assertEquals("var x = 1;", doc.select("script").first().data());
    }

    @Test
    public void testAfterHeadAndInBody() {
        Document doc = Jsoup.parse("<html><head></head><body><p>One</p>Two</body></html>");
        assertEquals("One Two", doc.body().text());
    }

    @Test
    public void testInBodyFormattingTags() {
        Document doc = Jsoup.parse("<b><i>bold</i></b> normal <p>para</p>");
        assertEquals("bold normal para", doc.body().text());
    }

    @Test
    public void testRawTextElements() {
        Document doc = Jsoup.parse("<textarea>  raw  </textarea><script>var s = '<x>';</script>");
        assertEquals("  raw  ", doc.select("textarea").first().val());
        assertEquals("var s = '<x>';", doc.select("script").first().data());
    }

    @Test
    public void testTableWithCaptionColgroupHeadBody() {
        Document doc = Jsoup.parse("<table><caption>Cap</caption><colgroup><col></colgroup>"
                + "<thead><tr><th>Head</th></tr></thead><tbody><tr><td>Cell</td></tr></tbody></table>");
        assertEquals("Cap", doc.select("caption").text());
        assertEquals(1, doc.select("colgroup").size());
        assertEquals(1, doc.select("col").size());
        assertEquals("Head", doc.select("th").text());
        assertEquals("Cell", doc.select("td").text());
        assertEquals(1, doc.select("tbody tr").size());
    }

    @Test
    public void testTableImplicitTbody() {
        Document doc = Jsoup.parse("<table><tr><td>Cell</td></tr></table>");
        assertEquals(1, doc.select("table tbody tr").size());
        assertEquals("Cell", doc.select("td").text());
    }

    @Test
    public void testNestedTable() {
        Document doc = Jsoup.parse("<table><tr><td><table><tr><td>inner</td></tr></table></td></tr></table>");
        assertEquals("inner", doc.select("table table td").text());
        assertEquals(1, doc.select("table table").size());
    }

    @Test
    public void testTableStyleAndScript() {
        Document doc = Jsoup.parse("<table><style>p{}</style><script>var x=1;</script><tr><td>Cell</td></tr></table>");
        Element style = doc.select("style").first();
        Element script = doc.select("script").first();
        assertNotNull(style);
        assertNotNull(script);
        assertEquals("p{}", style.data());
        assertEquals("var x=1;", script.data());
    }

    @Test
    public void testInputInTableWithoutTypeDoesNotThrow() {
        Document doc = Jsoup.parse("<table><input><tr><td>Cell</td></tr></table>");
        assertNotNull(doc.select("input").first());
        assertEquals("Cell", doc.select("td").text());
    }

    @Test
    public void testHiddenInputInTable() {
        Document doc = Jsoup.parse("<table><input type=\"hidden\"><tr><td>Cell</td></tr></table>");
        assertNotNull(doc.select("input").first());
        assertEquals("Cell", doc.select("td").text());
    }

    @Test
    public void testTableTextFosterParenting() {
        Document doc = Jsoup.parse("<table>outside<tr><td>cell</td></tr></table>");
        String bodyText = doc.body().text();
        assertTrue(bodyText.contains("outside"));
        assertTrue(bodyText.contains("cell"));
    }

    @Test
    public void testTableWhitespaceIgnored() {
        Document doc = Jsoup.parse("<table> \n <tr><td>cell</td></tr></table>");
        assertEquals("cell", doc.select("td").text());
    }

    @Test
    public void testSelect() {
        Document doc = Jsoup.parse("<select><option>One</option><option>Two</option></select>");
        assertEquals(2, doc.select("option").size());
        assertEquals("One Two", doc.select("select").text());
    }

    @Test
    public void testSelectInTable() {
        Document doc = Jsoup.parse("<table><tr><td><select><option>One</option></select></td></tr></table>");
        assertEquals("One", doc.select("select option").text());
    }

    @Test
    public void testFrameset() {
        Document doc = Jsoup.parse("<html><head></head><frameset><frame src=\"a\"><frame src=\"b\"></frameset></html>");
        assertEquals(2, doc.select("frame").size());
    }

    @Test
    public void testAfterBodyAndAfterAfterBody() {
        Document doc = Jsoup.parse("<html><body>x</body></html><!-- after -->");
        assertEquals("x", doc.body().text());
        assertTrue(doc.toString().contains("after"));
    }

    @Test
    public void testEofClosesOpenTags() {
        Document doc = Jsoup.parse("<html><body><div><p>unclosed");
        assertEquals("unclosed", doc.body().text());
    }

    @Test
    public void testMisnestedFormattingTags() {
        Document doc = Jsoup.parse("<b><i>bold</b>italic</i>");
        assertEquals("bold italic", doc.body().text());
    }

    @Test
    public void testMalformedTableRows() {
        Document doc = Jsoup.parse("<table><tr><td>a<tr><td>b</table>");
        assertEquals(2, doc.select("table td").size());
    }

    @Test
    public void testExtraTableEndTagIgnored() {
        Document doc = Jsoup.parse("<table><tr><td>a</td></tr></table></table>");
        assertEquals(1, doc.select("table").size());
        assertEquals("a", doc.select("td").text());
    }

    @Test
    public void testFormAndInput() {
        Document doc = Jsoup.parse("<form action=\"/submit\"><input name=\"q\"></form>");
        assertEquals("/submit", doc.select("form").first().attr("action"));
        assertEquals(1, doc.select("input").size());
    }

    @Test
    public void testLists() {
        Document doc = Jsoup.parse("<ul><li>a<li>b</ul><ol><li>1</li><li>2</li></ol>");
        assertEquals(4, doc.select("li").size());
    }
}