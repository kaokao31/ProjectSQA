package org.jsoup.select;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Selector;
import org.jsoup.select.SelectorParseException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for the Selector class.
 * Designed to achieve high code coverage and detect faults,
 * including the bug #12 related to attribute selectors with spaces.
 */
public class SelectorTest {
    private Document doc;
    private Element root;

    @Before
    public void setUp() {
        String html = "<html><body>" +
                "<div id=\"main\" class=\"content\">" +
                "<a href=\"http://example.com\" class=\"link\" title=\"hello world\">Example</a>" +
                "<a href=\"http://example.org\" class=\"link external\" title=\"hello\">Example2</a>" +
                "<span class=\"text\">Text</span>" +
                "<p id=\"para\">Paragraph</p>" +
                "<ul>" +
                "<li>Item 1</li>" +
                "<li>Item 2</li>" +
                "<li>Item 3</li>" +
                "</ul>" +
                "<b></b>" +
                "<div id=\"nested\">" +
                "<span>Only child</span>" +
                "</div>" +
                "</div>" +
                "</body></html>";
        doc = Jsoup.parse(html);
        root = doc.body();
    }

    // ---------- Basic selectors ----------

    @Test
    public void testTagSelector() {
        Selector selector = new Selector("a");
        Elements results = selector.select(root);
        assertEquals(2, results.size());
    }

    @Test
    public void testIdSelector() {
        Selector selector = new Selector("#main");
        Elements results = selector.select(root);
        assertEquals(1, results.size());
        assertEquals("main", results.get(0).id());
    }

    @Test
    public void testClassSelector() {
        Selector selector = new Selector(".link");
        Elements results = selector.select(root);
        assertEquals(2, results.size());
    }

    // ---------- Attribute selectors (no spaces) ----------

    @Test
    public void testAttributeSelectorNoSpaces() {
        Selector selector = new Selector("[href=\"http://example.com\"]");
        Elements results = selector.select(root);
        assertEquals(1, results.size());
        assertEquals("Example", results.get(0).text());
    }

    @Test
    public void testAttributeSelectorEmptyValue() {
        Selector selector = new Selector("[href=\"\"]");
        Elements results = selector.select(root);
        assertEquals(0, results.size());
    }

    @Test
    public void testAttributeSelectorValueWithSpaces() {
        Selector selector = new Selector("[title=\"hello world\"]");
        Elements results = selector.select(root);
        assertEquals(1, results.size());
        assertEquals("Example", results.get(0).text());
    }

    @Test
    public void testAttributeSelectorStartsWith() {
        Selector selector = new Selector("[href^=\"http://example.com\"]");
        Elements results = selector.select(root);
        assertEquals(1, results.size());
    }

    @Test
    public void testAttributeSelectorEndsWith() {
        Selector selector = new Selector("[href$=\".com\"]");
        Elements results = selector.select(root);
        assertEquals(2, results.size());
    }

    @Test
    public void testAttributeSelectorContains() {
        Selector selector = new Selector("[href*=\"example\"]");
        Elements results = selector.select(root);
        assertEquals(2, results.size());
    }

    @Test
    public void testAttributeSelectorWordMatch() {
        Selector selector = new Selector("[class~=\"link\"]");
        Elements results = selector.select(root);
        assertEquals(2, results.size());
    }

    @Test
    public void testAttributeSelectorHyphenMatch() {
        Selector selector = new Selector("[lang|=\"en\"]");
        Elements results = selector.select(root);
        assertEquals(0, results.size());
    }

    // ---------- Attribute selectors with spaces (bug #12) ----------

    @Test
    public void testAttributeSelectorWithSpaces() {
        Selector selector = new Selector("[href = \"http://example.com\"]");
        Elements results = selector.select(root);
        assertEquals(1, results.size());
        assertEquals("Example", results.get(0).text());
    }

    @Test
    public void testAttributeSelectorSpaceBefore() {
        Selector selector = new Selector("[href =\"http://example.com\"]");
        Elements results = selector.select(root);
        assertEquals(1, results.size());
        assertEquals("Example", results.get(0).text());
    }

    @Test
    public void testAttributeSelectorSpaceAfter() {
        Selector selector = new Selector("[href= \"http://example.com\"]");
        Elements results = selector.select(root);
        assertEquals(1, results.size());
        assertEquals("Example", results.get(0).text());
    }

    @Test
    public void testAttributeSelectorMultipleSpaces() {
        Selector selector = new Selector("[href  =  \"http://example.com\"]");
        Elements results = selector.select(root);
        assertEquals(1, results.size());
        assertEquals("Example", results.get(0).text());
    }

    @Test
    public void testAttributeSelectorNoQuotes() {
        Selector selector = new Selector("[href = http://example.com]");
        Elements results = selector.select(root);
        assertEquals(1, results.size());
        assertEquals("Example", results.get(0).text());
    }

    @Test
    public void testAttributeSelectorNoQuotesWithSpaces() {
        Selector selector = new Selector("[class = link]");
        Elements results = selector.select(root);
        assertEquals(1, results.size());
    }

    @Test
    public void testAttributeSelectorSingleQuotes() {
        Selector selector = new Selector("[href = 'http://example.com']");
        Elements results = selector.select(root);
        assertEquals(1, results.size());
        assertEquals("Example", results.get(0).text());
    }

    @Test
    public void testAttributeSelectorStartsWithWithSpaces() {
        Selector selector = new Selector("[href ^= \"http://example.com\"]");
        Elements results = selector.select(root);
        assertEquals(1, results.size());
    }

    @Test
    public void testAttributeSelectorEndsWithWithSpaces() {
        Selector selector = new Selector("[href $= \".com\"]");
        Elements results = selector.select(root);
        assertEquals(2, results.size());
    }

    @Test
    public void testAttributeSelectorContainsWithSpaces() {
        Selector selector = new Selector("[href *= \"example\"]");
        Elements results = selector.select(root);
        assertEquals(2, results.size());
    }

    @Test
    public void testAttributeSelectorWordMatchWithSpaces() {
        Selector selector = new Selector("[class ~= \"link\"]");
        Elements results = selector.select(root);
        assertEquals(2, results.size());
    }

    @Test
    public void testAttributeSelectorHyphenMatchWithSpaces() {
        Selector selector = new Selector("[lang |= \"en\"]");
        Elements results = selector.select(root);
        assertEquals(0, results.size());
    }

    // ---------- Combinators ----------

    @Test
    public void testDescendantCombinator() {
        Selector selector = new Selector("div a");
        Elements results = selector.select(root);
        assertEquals(2, results.size());
    }

    @Test
    public void testChildCombinator() {
        Selector selector = new Selector("div > a");
        Elements results = selector.select(root);
        assertEquals(2, results.size());
    }

    @Test
    public void testAdjacentSiblingCombinator() {
        Selector selector = new Selector("a + span");
        Elements results = selector.select(root);
        assertEquals(1, results.size());
        assertEquals("Text", results.get(0).text());
    }

    @Test
    public void testGeneralSiblingCombinator() {
        Selector selector = new Selector("a ~ span");
        Elements results = selector.select(root);
        assertEquals(1, results.size());
        assertEquals("Text", results.get(0).text());
    }

    // ---------- Pseudo-classes ----------

    @Test
    public void testPseudoFirstChild() {
        Selector selector = new Selector("li:first-child");
        Elements results = selector.select(root);
        assertEquals(1, results.size());
        assertEquals("Item 1", results.get(0).text());
    }

    @Test
    public void testPseudoLastChild() {
        Selector selector = new Selector("li:last-child");
        Elements results = selector.select(root);
        assertEquals(1, results.size());
        assertEquals("Item 3", results.get(0).text());
    }

    @Test
    public void testPseudoNthChild() {
        Selector selector = new Selector("li:nth-child(2)");
        Elements results = selector.select(root);
        assertEquals(1, results.size());
        assertEquals("Item 2", results.get(0).text());
    }

    @Test
    public void testPseudoNthChildOdd() {
        Selector selector = new Selector("li:nth-child(odd)");
        Elements results = selector.select(root);
        assertEquals(2, results.size());
    }

    @Test
    public void testPseudoNthLastChild() {
        Selector selector = new Selector("li:nth-last-child(2)");
        Elements results = selector.select(root);
        assertEquals(1, results.size());
        assertEquals("Item 2", results.get(0).text());
    }

    @Test
    public void testPseudoFirstOfType() {
        Selector selector = new Selector("a:first-of-type");
        Elements results = selector.select(root);
        assertEquals(1, results.size());
        assertEquals("Example", results.get(0).text());
    }

    @Test
    public void testPseudoLastOfType() {
        Selector selector = new Selector("a:last-of-type");
        Elements results = selector.select(root);
        assertEquals(1, results.size());
        assertEquals("Example2", results.get(0).text());
    }

    @Test
    public void testPseudoNthOfType() {
        Selector selector = new Selector("a:nth-of-type(2)");
        Elements results = selector.select(root);
        assertEquals(1, results.size());
        assertEquals("Example2", results.get(0).text());
    }

    @Test
    public void testPseudoNot() {
        Selector selector = new Selector("a:not(.external)");
        Elements results = selector.select(root);
        assertEquals(1, results.size());
        assertEquals("Example", results.get(0).text());
    }

    @Test
    public void testPseudoContains() {
        Selector selector = new Selector("a:contains(\"Example\")");
        Elements results = selector.select(root);
        assertEquals(2, results.size());
    }

    @Test
    public void testPseudoHas() {
        Selector selector = new Selector("div:has(a)");
        Elements results = selector.select(root);
        assertEquals(1, results.size());
        assertEquals("main", results.get(0).id());
    }

    @Test
    public void testPseudoEmpty() {
        Selector selector = new Selector("b:empty");
        Elements results = selector.select(root);
        assertEquals(1, results.size());
    }

    @Test
    public void testPseudoOnlyChild() {
        Selector selector = new Selector("span:only-child");
        Elements results = selector.select(root);
        assertEquals(1, results.size());
        assertEquals("Only child", results.get(0).text());
    }

    @Test
    public void testPseudoRoot() {
        Selector selector = new Selector(":root");
        Elements results = selector.select(doc);
        assertEquals(1, results.size());
        assertEquals("html", results.get(0).tagName());
    }

    // ---------- Multiple selectors ----------

    @Test
    public void testMultipleSelectors() {
        Selector selector = new Selector("div, span");
        Elements results = selector.select(root);
        assertEquals(4, results.size()); // 2 divs + 2 spans
    }

    // ---------- Edge cases and error handling ----------

    @Test(expected = IllegalArgumentException.class)
    public void testNullSelector() {
        new Selector(null);
    }

    @Test(expected = SelectorParseException.class)
    public void testEmptySelector() {
        new Selector("");
    }

    @Test(expected = SelectorParseException.class)
    public void testInvalidSelector() {
        new Selector("div[");
    }

    // ---------- Selector.matches() ----------

    @Test
    public void testMatches() {
        Selector selector = new Selector("a");
        Element firstLink = doc.select("a").first();
        assertTrue(selector.matches(firstLink));
        Element span = doc.select("span").first();
        assertFalse(selector.matches(span));
    }

    // ---------- Selector.select(Elements) ----------

    @Test
    public void testSelectElements() {
        Selector selector = new Selector("a");
        Elements allElements = doc.select("*");
        Elements results = selector.select(allElements);
        assertEquals(2, results.size());
    }
}