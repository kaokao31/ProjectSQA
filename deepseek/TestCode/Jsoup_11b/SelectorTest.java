package org.jsoup.select;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class SelectorTest {
    private Document doc;

    @Before
    public void setUp() {
        doc = Jsoup.parse("<html><body><div id='content' class='main'><p>Hello</p><span>World</span><a href='http://example.com'>Link</a></div></body></html>");
    }

    @Test
    public void testSelectTag() {
        Elements elements = Selector.select("span", doc);
        assertEquals(1, elements.size());
        assertEquals("span", elements.get(0).tagName());
    }

    @Test
    public void testSelectClass() {
        Elements elements = Selector.select(".main", doc);
        assertEquals(1, elements.size());
        assertEquals("div", elements.get(0).tagName());
    }

    @Test
    public void testSelectId() {
        Elements elements = Selector.select("#content", doc);
        assertEquals(1, elements.size());
        assertEquals("div", elements.get(0).id());
    }

    @Test
    public void testSelectAttribute() {
        Elements elements = Selector.select("[href]", doc);
        assertEquals(1, elements.size());
        assertTrue(elements.get(0).hasAttr("href"));
    }

    @Test
    public void testSelectDescendantCombinator() {
        Elements elements = Selector.select("div p", doc);
        assertEquals(1, elements.size());
    }

    @Test
    public void testSelectChildCombinator() {
        Elements elements = Selector.select("div > p", doc);
        assertEquals(1, elements.size());
    }

    @Test
    public void testSelectAdjacentSibling() {
        Elements elements = Selector.select("p + span", doc);
        assertEquals(1, elements.size());
    }

    @Test
    public void testSelectGeneralSibling() {
        Elements elements = Selector.select("p ~ span", doc);
        assertEquals(1, elements.size());
    }

    @Test
    public void testSelectHasPseudoClass() {
        Elements elements = Selector.select("div:has(p)", doc);
        assertEquals(1, elements.size());
        assertEquals("div", elements.get(0).tagName());
    }

    @Test
    public void testSelectContainsPseudoClass() {
        Elements elements = Selector.select("p:contains(Hello)", doc);
        assertEquals(1, elements.size());
    }

    @Test
    public void testSelectMatchesPseudoClass() {
        Elements elements = Selector.select("p:matches(\\w+)", doc);
        assertEquals(1, elements.size());
    }

    @Test(expected = Selector.SelectorParseException.class)
    public void testInvalidSelectorThrowsException() {
        Selector.select("div:unknown", doc);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullQueryThrowsException() {
        Selector.select(null, doc);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyQueryThrowsException() {
        Selector.select("", doc);
    }

    @Test
    public void testSelectMultipleSelectors() {
        Elements elements = Selector.select("p, span", doc);
        assertEquals(2, elements.size());
    }

    @Test
    public void testSelectWithNoMatch() {
        Elements elements = Selector.select("table", doc);
        assertTrue(elements.isEmpty());
    }
}