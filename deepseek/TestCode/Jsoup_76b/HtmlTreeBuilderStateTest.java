package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HtmlTreeBuilderStateTest {

    @Test
    public void initialStateHandlesDoctype() {
        Document doc = Jsoup.parse("<!DOCTYPE html><html><body>foo</body></html>");
        assertEquals("#doctype", doc.childNode(0).nodeName());
        assertEquals("foo", doc.body().text());
    }

    @Test
    public void beforeHtmlPlainTextCreatesHtml() {
        Document doc = Jsoup.parse("text");
        assertEquals("html", doc.children().first().tagName());
        assertEquals("text", doc.body().text());
    }

    @Test
    public void beforeHeadFindsHead() {
        Document doc = Jsoup.parse("<html><head><title>x</title></head><body></body></html>");
        assertEquals("x", doc.title());
    }

    @Test
    public void inHeadElements() {
        Document doc = Jsoup.parse(
            "<head><meta charset=\"utf-8\"><link rel=\"icon\"><base href=\"/\">" +
            "<style>p{}</style></head><body>x</body>");
        assertEquals(1, doc.getElementsByTag("meta").size());
        assertEquals(1, doc.getElementsByTag("link").size());
        assertEquals(1, doc.getElementsByTag("base").size());
        assertEquals("p{}", doc.select("style").first().wholeText());
    }

    @Test
    public void inHeadNoscript() {
        Document doc = Jsoup.parse(
            "<head><noscript><link rel=\"stylesheet\"></noscript></head><body>x</body>");
        assertEquals(1, doc.getElementsByTag("noscript").size());
        assertEquals(1, doc.select("noscript link").size());
    }

    @Test
    public void afterHeadBodyAndWhitespace() {
        Document doc = Jsoup.parse("<html><head></head> <body>x</body></html>");
        assertEquals("x", doc.body().text());
    }

    @Test
    public void inBodyBuildsTree() {
        Document doc = Jsoup.parse("<div><span>one</span><span>two</span></div>");
        Element div = doc.getElementsByTag("div").first();
        assertEquals(2, div.children().size());
        assertEquals("one", div.child(0).ownText());
    }

    @Test
    public void inBodyPClosesP() {
        Document doc = Jsoup.parse("<p>one<p>two");
        List<Element> p = doc.getElementsByTag("p");
        assertEquals(2, p.size());
        assertEquals("one", p.get(0).ownText());
        assertEquals("two", p.get(1).ownText());
    }

    @Test
    public void mainTagIsRecognizedAsStructural() {
        Document doc = Jsoup.parse("<p>one<main>two</main></p>");
        Element p = doc.getElementsByTag("p").first();
        Element main = doc.getElementsByTag("main").first();
        assertNotNull(main);
        assertEquals("one", p.ownText());
        assertEquals("two", main.ownText());
        assertEquals("body", main.parent().tagName());
        assertEquals(0, p.children().size());
    }

    @Test
    public void semanticElementsCloseOpenP() {
        String[] tags = {"address", "article", "aside", "blockquote", "center", "details",
            "dir", "div", "dl", "fieldset", "figcaption", "figure", "footer",
            "header", "hgroup", "main", "menu", "nav", "ol", "section", "summary", "ul"};
        for (String tag : tags) {
            Document doc = Jsoup.parse("<p>x<" + tag + ">y</" + tag + ">");
            Element p = doc.select("p").first();
            Element el = doc.select(tag).first();
            assertNotNull("Element should be parsed: " + tag, el);
            assertEquals("parent of " + tag, "body", el.parent().tagName());
            assertEquals("p should have no children: " + tag, 0, p.children().size());
        }
    }

    @Test
    public void inBodyHeadingsCloseP() {
        Document doc = Jsoup.parse("<p>one<h1>two</h1>");
        assertEquals("one", doc.getElementsByTag("p").first().ownText());
        assertEquals("two", doc.getElementsByTag("h1").first().ownText());
    }

    @Test
    public void inBodyPrePreservesWhitespace() {
        Document doc = Jsoup.parse("<pre>  one\ntwo  </pre>");
        assertEquals("  one\ntwo  ", doc.select("pre").first().wholeText());
    }

    @Test
    public void inBodyTextareaPreservesWhitespace() {
        Document doc = Jsoup.parse("<textarea>  one\ntwo  </textarea>");
        assertEquals("  one\ntwo  ", doc.select("textarea").first().wholeText());
    }

    @Test
    public void inBodyTitleHandlesRcdata() {
        Document doc = Jsoup.parse("<title>  Hello <b>world</b>  </title>");
        assertEquals("  Hello <b>world</b>  ", doc.title());
    }

    @Test
    public void inBodyScriptRawText() {
        Document doc = Jsoup.parse("<script>if (a<b) { a++; }</script>");
        assertEquals("if (a<b) { a++; }", doc.select("script").first().wholeText());
    }

    @Test
    public void inBodyListNesting() {
        Document doc = Jsoup.parse("<ul><li>one<li>two</ul>");
        assertEquals(2, doc.select("li").size());
        assertEquals("one", doc.select("li").get(0).ownText());
        assertEquals("two", doc.select("li").get(1).ownText());
    }

    @Test
    public void inBodyFormAndButton() {
        Document doc = Jsoup.parse("<form><button>a</button></form>");
        assertEquals("a", doc.select("button").first().text());
    }

    @Test
    public void inBodyFormClosesP() {
        Document doc = Jsoup.parse("<p>a<form>b</form>");
        assertEquals("body", doc.select("form").first().parent().tagName());
        assertEquals(0, doc.select("p").first().children().size());
    }

    @Test
    public void inBodyConsecutiveAnchorsClosePrevious() {
        Document doc = Jsoup.parse("<a href=\"1\">one<a href=\"2\">two</a>");
        List<Element> anchors = doc.select("a");
        assertEquals(2, anchors.size());
        assertEquals("one", anchors.get(0).ownText());
        assertEquals("two", anchors.get(1).ownText());
    }

    @Test
    public void inBodyMiscInlineElements() {
        Document doc = Jsoup.parse("<p>a<b>b</b><i>i</i><u>u</u><s>s</s></p>");
        assertEquals("a", doc.select("p").first().ownText());
        assertEquals("b", doc.select("b").first().text());
        assertEquals("i", doc.select("i").first().text());
        assertEquals("u", doc.select("u").first().text());
        assertEquals("s", doc.select("s").first().text());
    }

    @Test
    public void inTableCaptionsAndCell() {
        Document doc = Jsoup.parse(
            "<table><caption>C</caption><colgroup><col></colgroup>" +
            "<thead><tr><th>h</th></tr></thead><tbody><tr><td>d</td></tr></tbody></table>");
        assertEquals("C", doc.select("caption").first().ownText());
        assertEquals("h", doc.select("th").first().ownText());
        assertEquals("d", doc.select("td").first().ownText());
    }

    @Test
    public void inTableUnknownTagFosterParsed() {
        Document doc = Jsoup.parse("<table><div>X</div><tr><td>Y</td></tr></table>");
        Element div = doc.select("div").first();
        assertNotNull(div);
        assertEquals("body", div.parent().tagName());
    }

    @Test
    public void inCaptionParsesContent() {
        Document doc = Jsoup.parse("<table><caption><b>bold</b></caption><tr><td>x</td></tr></table>");
        assertEquals("bold", doc.select("caption b").first().text());
    }

    @Test
    public void inColumnGroupParsesCols() {
        Document doc = Jsoup.parse("<table><colgroup><col span=\"2\"></colgroup><tr><td>x</td></tr></table>");
        assertEquals(1, doc.select("colgroup").size());
        assertEquals(1, doc.select("col").size());
    }

    @Test
    public void inTableBodyProcessesRows() {
        Document doc = Jsoup.parse("<table><tbody><tr><td>a</td></tr></tbody></table>");
        assertEquals(1, doc.select("tbody tr").size());
    }

    @Test
    public void inRowProcessesCells() {
        Document doc = Jsoup.parse("<table><tr><td>a</td><td>b</td></tr></table>");
        assertEquals(2, doc.select("tr td").size());
    }

    @Test
    public void inCellClosesOnNextCell() {
        Document doc = Jsoup.parse("<table><tr><td>a<td>b</tr></table>");
        assertEquals("a", doc.select("td").get(0).ownText());
        assertEquals("b", doc.select("td").get(1).ownText());
    }

    @Test
    public void inSelectParsesOptions() {
        Document doc = Jsoup.parse("<select><option>a</option><option>b</option></select>");
        assertEquals(2, doc.select("option").size());
    }

    @Test
    public void afterBodyProcessesFurtherContent() {
        Document doc = Jsoup.parse("<html><body>a</body><div>b</div></html>");
        Element div = doc.select("div").first();
        assertNotNull(div);
        assertEquals("b", div.text());
    }

    @Test
    public void inFramesetParsesFrames() {
        Document doc = Jsoup.parse(
            "<html><head></head><frameset><frame src=\"a\"><frame src=\"b\"></frameset></html>");
        assertEquals(2, doc.select("frame").size());
    }

    @Test
    public void afterFramesetParsesNoFrames() {
        Document doc = Jsoup.parse(
            "<html><head></head><frameset><frame src=\"a\"></frameset><noframes>hi</noframes></html>");
        assertEquals("hi", doc.select("noframes").first().text());
    }

    @Test
    public void afterAfterBodyProcessesComments() {
        Document doc = Jsoup.parse("<html><body>a</body></html><!-- after -->");
        boolean found = false;
        for (org.jsoup.nodes.Node node : doc.childNodes()) {
            if (node.nodeName().equals("#comment") && node.toString().contains("after")) {
                found = true;
            }
        }
        assertTrue(found);
    }

    @Test
    public void anyOtherEndTagHandlesMissingStart() {
        Document doc = Jsoup.parse("</span>hello");
        assertEquals("hello", doc.body().text());
    }

    @Test
    public void whitespaceOnlyBodyIsEmpty() {
        Document doc = Jsoup.parse("   ");
        assertEquals(0, doc.body().children().size());
    }

    @Test
    public void unknownTagsStayInPlace() {
        Document doc = Jsoup.parse("<div><foo>bar</foo></div>");
        Element foo = doc.select("foo").first();
        assertNotNull(foo);
        assertEquals("div", foo.parent().tagName());
    }

    @Test
    public void rubyTagsParsed() {
        Document doc = Jsoup.parse("<ruby><rb>base</rb><rt>note</rt></ruby>");
        assertEquals("base", doc.select("rb").first().ownText());
        assertEquals("note", doc.select("rt").first().ownText());
    }

    @Test
    public void mainTagIsBlockLevel() {
        Document doc = Jsoup.parse("<main>one</main>");
        Element main = doc.select("main").first();
        assertEquals("one", main.text());
        assertEquals("body", main.parent().tagName());
    }

    @Test
    public void allKnownTagsDoNotCrash() {
        String html = "<html><head><title>t</title><meta><link><style>s</style><script>js</script>" +
            "<noscript>n</noscript></head><body>" +
            "<address/><article/><aside/><blockquote/><center/><details/><dir/><div/><dl/>" +
            "<fieldset/><figcaption/><figure/><footer/><header/><hgroup/><main/><menu/><nav/>" +
            "<ol/><p/><section/><summary/><ul/><h1/><pre/><form/><button/><table/><caption/>" +
            "<colgroup/><col/><thead/><tbody/><tfoot/><tr/><td/><th/><select/><option/>" +
            "<optgroup/><svg/><math/><br/><img/><input/><hr/><wbr/><iframe/><embed/><object/>" +
            "<param/><video/><audio/><source/><canvas/><ruby/><rb/><rt/><rp/><script/>" +
            "<style/><textarea/><title/></body></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
    }
}