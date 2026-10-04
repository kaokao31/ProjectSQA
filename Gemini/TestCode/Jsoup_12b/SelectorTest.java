package org.jsoup.select;

import org.junit.Test;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import static org.junit.Assert.*;

public class SelectorTest {

    @Test
    public void testSelectorCombinatorsAndAttributes() {
        String html = "<div><p id='p1' class='class1'>Hello</p><p class='class2'>World</p><a href='http://example.com'>Link</a></div>";
        Document doc = Jsoup.parse(html);

        // Test basic tag selector
        Elements divs = Selector.select("div", doc);
        assertEquals(1, divs.size());

        // Test descendant selector
        Elements ps = Selector.select("div p", doc);
        assertEquals(2, ps.size());

        // Test ID selector
        Elements p1 = Selector.select("#p1", doc);
        assertEquals(1, p1.size());
        assertEquals("p1", p1.first().id());

        // Test class selector
        Elements class1 = Selector.select(".class1", doc);
        assertEquals(1, class1.size());

        // Test attribute selector
        Elements links = Selector.select("[href]", doc);
        assertEquals(1, links.size());

        // Test attribute value selector
        Elements specificLink = Selector.select("[href=http://example.com]", doc);
        assertEquals(1, specificLink.size());
    }

    @Test
    public void testSelectorDirectChildAndAdjacent() {
        String html = "<ul id='menu'>" +
                "<li>Item 1</li>" +
                "<li>Item 2<sub-item>Sub</sub-item></li>" +
                "</ul>" +
                "<div class='sibling'>Sibling</div>";
        Document doc = Jsoup.parse(html);

        // Test direct child selector (>)
        Elements directLis = Selector.select("ul > li", doc);
        assertEquals(2, directLis.size());

        // Test multiple children or deeper descendants where direct child doesn't match
        Elements subItems = Selector.select("ul > sub-item", doc);
        assertEquals(0, subItems.size()); // should be 0 because sub-item is under li, not directly under ul

        Elements nestedSub = Selector.select("ul li > sub-item", doc);
        assertEquals(1, nestedSub.size());
    }

    @Test
    public void testSelectorMultipleQueries() {
        String html = "<div id='d1'>Div 1</div><span id='s1'>Span 1</span><p id='p1'>Paragraph</p>";
        Document doc = Jsoup.parse(html);

        // Test comma-separated multiple selectors
        Elements elements = Selector.select("div, span", doc);
        assertEquals(2, elements.size());
    }

    @Test
    public void testSelectorEdgeCasesAndNulls() {
        Document doc = Jsoup.parse("<div><span>Test</span></div>");

        // Null or empty queries handling (depending on implementation, should handle gracefully or throw)
        try {
            Selector.select("", doc);
        } catch (Exception e) {
            // expected or handled
        }

        try {
            Selector.select(null, doc);
        } catch (Exception e) {
            // expected or handled
        }

        Elements none = Selector.select("nonexistent", doc);
        assertNotNull(none);
        assertTrue(none.isEmpty());
    }

    @Test
    public void testSelectorInstanceMethods() {
        String html = "<div class='test'>Content</div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();

        // Testing the constructor / static methods coverage of Selector class
        Elements result = Selector.select(".test", div);
        assertNotNull(result);
        assertEquals(1, result.size());
    }
}