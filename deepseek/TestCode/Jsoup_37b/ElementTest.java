package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for org.jsoup.nodes.Element, targeting bug 37 (getElementsByTag with namespaces).
 */
public class ElementTest {

    // ---------- getElementsByTag tests ----------

    @Test
    public void testGetElementsByTagBasic() {
        String html = "<div><p>Hello</p><span>World</span></div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        Elements ps = div.getElementsByTag("p");
        assertEquals(1, ps.size());
        assertEquals("p", ps.get(0).tagName());
    }

    @Test
    public void testGetElementsByTagWithNamespace() {
        // Namespaced elements like <div:foo> should NOT be returned by getElementsByTag("div")
        String html = "<div:foo>bar</div:foo><div>normal</div>";
        Document doc = Jsoup.parse(html);
        Elements divs = doc.getElementsByTag("div");
        assertEquals(1, divs.size());
        assertEquals("div", divs.get(0).tagName());
    }

    @Test
    public void testGetElementsByTagCaseInsensitive() {
        String html = "<DIV><P>text</P></DIV>";
        Document doc = Jsoup.parse(html);
        Elements divs = doc.getElementsByTag("div");
        assertEquals(1, divs.size());
        Elements ps = doc.getElementsByTag("p");
        assertEquals(1, ps.size());
    }

    @Test
    public void testGetElementsByTagMultiple() {
        String html = "<ul><li>one</li><li>two</li><li>three</li></ul>";
        Document doc = Jsoup.parse(html);
        Elements lis = doc.getElementsByTag("li");
        assertEquals(3, lis.size());
    }

    @Test
    public void testGetElementsByTagNoMatch() {
        String html = "<div><span>text</span></div>";
        Document doc = Jsoup.parse(html);
        Elements ps = doc.getElementsByTag("p");
        assertTrue(ps.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetElementsByTagNull() {
        String html = "<div>text</div>";
        Document doc = Jsoup.parse(html);
        doc.getElementsByTag(null);
    }

    @Test
    public void testGetElementsByTagEmpty() {
        String html = "<div>text</div>";
        Document doc = Jsoup.parse(html);
        Elements empty = doc.getElementsByTag("");
        assertTrue(empty.isEmpty());
    }

    @Test
    public void testGetElementsByTagDeepNested() {
        String html = "<div><p><span><a>link</a></span></p></div>";
        Document doc = Jsoup.parse(html);
        Elements spans = doc.getElementsByTag("span");
        assertEquals(1, spans.size());
        Elements as = doc.getElementsByTag("a");
        assertEquals(1, as.size());
    }

    @Test
    public void testGetElementsByTagSelfClosing() {
        String html = "<div><br/><hr/></div>";
        Document doc = Jsoup.parse(html);
        Elements brs = doc.getElementsByTag("br");
        assertEquals(1, brs.size());
        Elements hrs = doc.getElementsByTag("hr");
        assertEquals(1, hrs.size());
    }

    @Test
    public void testGetElementsByTagFromDocument() {
        String html = "<html><body><p>text</p></body></html>";
        Document doc = Jsoup.parse(html);
        Elements ps = doc.getElementsByTag("p");
        assertEquals(1, ps.size());
    }

    @Test
    public void testGetElementsByTagFromElement() {
        String html = "<div><p>inner</p></div><p>outer</p>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        Elements ps = div.getElementsByTag("p");
        assertEquals(1, ps.size()); // only the inner p
    }

    // ---------- html() tests ----------

    @Test
    public void testHtmlBasic() {
        String html = "<div><p>Hello</p></div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        assertEquals("<p>Hello</p>", div.html());
    }

    @Test
    public void testHtmlWithAttributes() {
        String html = "<div id='main' class='container'><p>text</p></div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        assertEquals("<p>text</p>", div.html());
    }

    @Test
    public void testHtmlEmpty() {
        String html = "<div></div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        assertEquals("", div.html());
    }

    // ---------- text() tests ----------

    @Test
    public void testTextBasic() {
        String html = "<div><p>Hello</p><p>World</p></div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        assertEquals("Hello World", div.text());
    }

    @Test
    public void testTextWithWhitespace() {
        String html = "<div>  Hello   World  </div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        assertEquals("Hello World", div.text());
    }

    @Test
    public void testTextEmpty() {
        String html = "<div></div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        assertEquals("", div.text());
    }

    // ---------- attr() tests ----------

    @Test
    public void testAttrBasic() {
        String html = "<a href='http://example.com'>link</a>";
        Document doc = Jsoup.parse(html);
        Element a = doc.select("a").first();
        assertEquals("http://example.com", a.attr("href"));
    }

    @Test
    public void testAttrMissing() {
        String html = "<a>link</a>";
        Document doc = Jsoup.parse(html);
        Element a = doc.select("a").first();
        assertEquals("", a.attr("href"));
    }

    @Test
    public void testAttrNull() {
        String html = "<a>link</a>";
        Document doc = Jsoup.parse(html);
        Element a = doc.select("a").first();
        assertEquals("", a.attr(null));
    }

    // ---------- tagName() tests ----------

    @Test
    public void testTagNameBasic() {
        String html = "<div>text</div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        assertEquals("div", div.tagName());
    }

    @Test
    public void testTagNameWithNamespace() {
        // Namespaced tag should preserve the full name
        String html = "<div:foo>bar</div:foo>";
        Document doc = Jsoup.parse(html);
        Element el = doc.select("div\\:foo").first(); // CSS selector needs escaping
        // Actually, Jsoup's CSS selector may not handle namespace well; use getElementsByTag
        Elements els = doc.getElementsByTag("div:foo");
        assertEquals(1, els.size());
        assertEquals("div:foo", els.get(0).tagName());
    }

    // ---------- id() tests ----------

    @Test
    public void testIdBasic() {
        String html = "<div id='myid'>text</div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        assertEquals("myid", div.id());
    }

    @Test
    public void testIdMissing() {
        String html = "<div>text</div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        assertEquals("", div.id());
    }

    // ---------- className() tests ----------

    @Test
    public void testClassNameBasic() {
        String html = "<div class='myclass'>text</div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        assertEquals("myclass", div.className());
    }

    @Test
    public void testClassNameMultiple() {
        String html = "<div class='class1 class2'>text</div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        assertEquals("class1 class2", div.className());
    }

    @Test
    public void testClassNameMissing() {
        String html = "<div>text</div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        assertEquals("", div.className());
    }

    // ---------- hasClass() tests ----------

    @Test
    public void testHasClassTrue() {
        String html = "<div class='myclass'>text</div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        assertTrue(div.hasClass("myclass"));
    }

    @Test
    public void testHasClassFalse() {
        String html = "<div class='other'>text</div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        assertFalse(div.hasClass("myclass"));
    }

    @Test
    public void testHasClassNull() {
        String html = "<div class='myclass'>text</div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        assertFalse(div.hasClass(null));
    }

    // ---------- childNodeSize() tests ----------

    @Test
    public void testChildNodeSize() {
        String html = "<div><p>one</p><p>two</p></div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        assertEquals(2, div.childNodeSize());
    }

    @Test
    public void testChildNodeSizeEmpty() {
        String html = "<div></div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        assertEquals(0, div.childNodeSize());
    }

    // ---------- children() tests ----------

    @Test
    public void testChildrenBasic() {
        String html = "<div><p>one</p><p>two</p></div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        Elements children = div.children();
        assertEquals(2, children.size());
        assertEquals("p", children.get(0).tagName());
    }

    @Test
    public void testChildrenEmpty() {
        String html = "<div></div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        assertTrue(div.children().isEmpty());
    }

    // ---------- parent() tests ----------

    @Test
    public void testParent() {
        String html = "<div><p>text</p></div>";
        Document doc = Jsoup.parse(html);
        Element p = doc.select("p").first();
        Element parent = p.parent();
        assertNotNull(parent);
        assertEquals("div", parent.tagName());
    }

    @Test
    public void testParentOfDocument() {
        String html = "<html><body><p>text</p></body></html>";
        Document doc = Jsoup.parse(html);
        Element htmlEl = doc.select("html").first();
        Element parent = htmlEl.parent();
        assertNotNull(parent);
        assertTrue(parent instanceof Document);
    }

    // ---------- select() tests ----------

    @Test
    public void testSelectBasic() {
        String html = "<div><p class='foo'>text</p></div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        Elements selected = div.select("p.foo");
        assertEquals(1, selected.size());
    }

    @Test
    public void testSelectNoMatch() {
        String html = "<div><p>text</p></div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        Elements selected = div.select("span");
        assertTrue(selected.isEmpty());
    }

    // ---------- appendChild() tests ----------

    @Test
    public void testAppendChild() {
        String html = "<div></div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        Element p = new Element("p");
        p.text("new");
        div.appendChild(p);
        assertEquals(1, div.children().size());
        assertEquals("new", div.text());
    }

    // ---------- prependChild() tests ----------

    @Test
    public void testPrependChild() {
        String html = "<div><p>old</p></div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        Element p = new Element("p");
        p.text("new");
        div.prependChild(p);
        assertEquals(2, div.children().size());
        assertEquals("new old", div.text());
    }

    // ---------- remove() tests ----------

    @Test
    public void testRemove() {
        String html = "<div><p>text</p></div>";
        Document doc = Jsoup.parse(html);
        Element p = doc.select("p").first();
        p.remove();
        assertEquals(0, doc.select("p").size());
    }

    // ---------- empty() tests ----------

    @Test
    public void testEmpty() {
        String html = "<div><p>text</p></div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        div.empty();
        assertEquals(0, div.children().size());
        assertEquals("", div.html());
    }

    // ---------- data() tests ----------

    @Test
    public void testData() {
        String html = "<script>var x = 1;</script>";
        Document doc = Jsoup.parse(html);
        Element script = doc.select("script").first();
        assertEquals("var x = 1;", script.data());
    }

    @Test
    public void testDataEmpty() {
        String html = "<script></script>";
        Document doc = Jsoup.parse(html);
        Element script = doc.select("script").first();
        assertEquals("", script.data());
    }

    // ---------- equals() tests ----------

    @Test
    public void testEqualsSameObject() {
        String html = "<div>text</div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        assertTrue(div.equals(div));
    }

    @Test
    public void testEqualsDifferentObject() {
        String html = "<div>text</div>";
        Document doc = Jsoup.parse(html);
        Element div1 = doc.select("div").first();
        Element div2 = doc.select("div").first();
        assertTrue(div1.equals(div2)); // same element from same document
    }

    @Test
    public void testEqualsNull() {
        String html = "<div>text</div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        assertFalse(div.equals(null));
    }

    // ---------- hashCode() tests ----------

    @Test
    public void testHashCodeConsistency() {
        String html = "<div>text</div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        int hash1 = div.hashCode();
        int hash2 = div.hashCode();
        assertEquals(hash1, hash2);
    }

    // ---------- toString() tests ----------

    @Test
    public void testToString() {
        String html = "<div><p>text</p></div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        String str = div.toString();
        assertTrue(str.contains("<div>"));
        assertTrue(str.contains("</div>"));
    }
}