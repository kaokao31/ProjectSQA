package org.jsoup.select;

import org.junit.Test;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import static org.junit.Assert.*;

public class SelectorTest {

    @Test
    public void testSelectByTag() {
        Document doc = Jsoup.parse("<div><p>Hello</p><p>World</p></div>");
        Elements paragraphs = Selector.select("p", doc);
        assertEquals(2, paragraphs.size());
        assertEquals("Hello", paragraphs.get(0).text());
        assertEquals("World", paragraphs.get(1).text());
    }

    @Test
    public void testSelectById() {
        Document doc = Jsoup.parse("<div id='target'><p>Inner</p></div>");
        Elements el = Selector.select("#target", doc);
        assertEquals(1, el.size());
        assertEquals("div", el.get(0).tagName());
    }

    @Test
    public void testSelectByClass() {
        Document doc = Jsoup.parse("<div class='foo'>One</div><p class='foo'>Two</p><span class='bar'>Three</span>");
        Elements els = Selector.select(".foo", doc);
        assertEquals(2, els.size());
    }

    @Test
    public void testSelectCombination() {
        Document doc = Jsoup.parse("<div class='content'><p class='foo'>Match</p></div><p class='foo'>NoMatch</p>");
        Elements els = Selector.select("div.content p.foo", doc);
        assertEquals(1, els.size());
        assertEquals("Match", els.get(0).text());
    }

    @Test
    public void testSelectAttribute() {
        Document doc = Jsoup.parse("<a href='http://example.com'>Link</a><a href='http://jsoup.org'>Jsoup</a><a foo='bar'>No Href</a>");
        Elements els = Selector.select("a[href]", doc);
        assertEquals(2, els.size());
    }

    @Test
    public void testSelectAttributeWithValue() {
        Document doc = Jsoup.parse("<a href='http://example.com'>Link</a><a href='http://other.com'>Other</a>");
        Elements els = Selector.select("a[href=http://example.com]", doc);
        assertEquals(1, els.size());
        assertEquals("Link", els.get(0).text());
    }

    @Test
    public void testSelectDirectChild() {
        Document doc = Jsoup.parse("<div><p>Direct</p><span><p>Indirect</p></span></div>");
        Elements els = Selector.select("div > p", doc);
        assertEquals(1, els.size());
        assertEquals("Direct", els.get(0).text());
    }

    @Test
    public void testSelectAdjacentSibling() {
        Document doc = Jsoup.parse("<p>First</p><h2>Second</h2><p>Third</p>");
        Elements els = Selector.select("h2 + p", doc);
        assertEquals(1, els.size());
        assertEquals("Third", els.get(0).text());
    }

    @Test
    public void testSelectGeneralSibling() {
        Document doc = Jsoup.parse("<h2>Title</h2><p>First</p><span>Span</span><p>Second</p>");
        Elements els = Selector.select("h2 ~ p", doc);
        assertEquals(2, els.size());
        assertEquals("First", els.get(0).text());
        assertEquals("Second", els.get(1).text());
    }

    @Test
    public void testSelectMultipleQueries() {
        Document doc = Jsoup.parse("<div><span>Span</span><p>Paragraph</p></div>");
        Elements els = Selector.select("span, p", doc);
        assertEquals(2, els.size());
    }

    @Test
    public void testSelectPseudoFirstChild() {
        Document doc = Jsoup.parse("<div><p>First</p><p>Second</p></div>");
        Elements els = Selector.select("p:first-child", doc);
        assertEquals(1, els.size());
        assertEquals("First", els.get(0).text());
    }

    @Test
    public void testSelectWithRootElement() {
        Document doc = Jsoup.parse("<div id='1'><p>P1</p></div><div id='2'><p>P2</p></div>");
        Element root = doc.getElementById("2");
        Elements els = Selector.select("p", root);
        assertEquals(1, els.size());
        assertEquals("P2", els.get(0).text());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSelectNullQuery() {
        Document doc = Jsoup.parse("<div></div>");
        Selector.select(null, doc);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSelectNullRoot() {
        Selector.select("div", (Element) null);
    }

    @Test
    public void testSelectEmptyQuery() {
        Document doc = Jsoup.parse("<div></div>");
        Elements els = Selector.select("", doc);
        assertTrue(els.isEmpty());
    }

    @Test
    public void testSelectNoMatch() {
        Document doc = Jsoup.parse("<div></div>");
        Elements els = Selector.select("span", doc);
        assertTrue(els.isEmpty());
    }

    @Test
    public void testSelectAllUniversal() {
        Document doc = Jsoup.parse("<div><p><span>Text</span></p></div>");
        Elements els = Selector.select("*", doc);
        assertTrue(els.size() >= 3);
    }
}