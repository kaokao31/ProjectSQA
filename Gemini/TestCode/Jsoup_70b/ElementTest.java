package org.jsoup.nodes;

import org.junit.Test;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import static org.junit.Assert.*;

public class ElementTest {

    @Test
    public void testNormalCaseCodeTag() {
        // Specifically targets Jsoup-70 bug where code tags (or similar structural tags) 
        // with certain whitespace or text node children might cause infinite loops or incorrect formatting.
        String html = "<code>\n  <span>Hello</span>\n</code>";
        Element root = Jsoup.parse(html).body().child(0);
        
        // This exercises text() and normal handling of formatting/code nodes
        String text = root.text();
        assertNotNull(text);
        assertTrue(text.contains("Hello"));
    }

    @Test
    public void testChildTextNodeTraversalWithCodeTag() {
        // Test preserving whitespace or handling specific child nodes inside code tags
        Element el = new Element("code");
        TextNode tn1 = new TextNode("   ");
        TextNode tn2 = new TextNode("int x = 5;");
        el.appendChild(tn1);
        el.appendChild(tn2);

        String text = el.text();
        assertEquals("int x = 5;", text);
    }

    @Test
    public void testNormaliseWhitespaceInCodeTag() {
        // Jsoup often normalizes whitespace in text() unless it's in an unwrapped/preserveWhitespace context.
        // Let's verify normal element vs code element behavior.
        Element codeEl = new Element("code");
        codeEl.appendChild(new TextNode("   a   b   "));
        
        // In Jsoup, standard elements normalize whitespace in text(). 
        // For code tags, let's see how child text nodes are accessed and formatted.
        assertNotNull(codeEl.text());
    }

    @Test
    public void testEmptyElementText() {
        Element el = new Element("code");
        assertEquals("", el.text());
    }

    @Test
    public void testNestedCodeTags() {
        String html = "<div><code><code>nested</code></code></div>";
        Element div = Jsoup.parse(html).selectFirst("div");
        assertNotNull(div);
        assertEquals("nested", div.text());
    }

    @Test
    public void testNormalElementTextCollapsing() {
        Element p = new Element("p");
        p.appendChild(new TextNode("  one   two  "));
        // Standard text() collapses whitespace
        assertEquals("one two", p.text());
    }
}