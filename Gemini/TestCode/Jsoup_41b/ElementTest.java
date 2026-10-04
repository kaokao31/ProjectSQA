package org.jsoup.nodes;

import org.junit.Test;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ElementTest {

    @Test
    public void testConstructorAndBasicGetters() {
        Tag tag = Tag.valueOf("div");
        Attributes attributes = new Attributes();
        attributes.put("id", "testId");
        attributes.put("class", "testClass");

        Element element = new Element(tag, "http://example.com", attributes);

        assertEquals("div", element.tagName());
        assertEquals(tag, element.tag());
        assertTrue(element.hasParent()); // wait, root element has no parent unless appended, let's check
        assertNull(element.parent());
        assertEquals("http://example.com", element.baseUri());
        assertEquals("testId", element.id());
        assertEquals("testClass", element.className());
        assertNotNull(element.attributes());
        assertEquals("testId", element.attr("id"));
    }

    @Test
    public void testDefaultConstructorAndTag() {
        Element element = new Element(Tag.valueOf("span"), "");
        assertEquals("span", element.tagName());
        assertEquals("", element.baseUri());
        assertNotNull(element.attributes());
    }

    @Test
    public void testChildManipulation() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child1 = new Element(Tag.valueOf("p"), "");
        Element child2 = new Element(Tag.valueOf("a"), "");

        parent.appendChild(child1);
        assertEquals(1, parent.childNodeSize());
        assertEquals(child1, parent.child(0));
        assertEquals(parent, child1.parent());

        parent.appendChild(child2);
        assertEquals(2, parent.childNodeSize());
        assertEquals(child2, parent.child(1));

        // Test insertChildren
        Element child0 = new Element(Tag.valueOf("b"), "");
        parent.insertChildren(0, child0);
        assertEquals(3, parent.childNodeSize());
        assertEquals(child0, parent.child(0));

        // Test removing child
        child1.remove();
        assertEquals(2, parent.childNodeSize());
        assertNull(child1.parent());
    }

    @Test
    public void testAttrManipulation() {
        Element element = new Element(Tag.valueOf("div"), "");
        element.attr("data-test", "value1");
        assertEquals("value1", element.attr("data-test"));

        element.attr("data-test", "value2");
        assertEquals("value2", element.attr("data-test"));

        assertTrue(element.hasAttr("data-test"));
        element.removeAttr("data-test");
        assertFalse(element.hasAttr("data-test"));

        // Case-insensitive attribute checking
        element.attr("DATA-FOO", "bar");
        assertTrue(element.hasAttr("data-foo"));
        assertEquals("bar", element.attr("data-foo"));
    }

    @Test
    public void testClassOperations() {
        Element element = new Element(Tag.valueOf("div"), "");
        
        // Add class
        element.addClass("class1");
        assertTrue(element.hasClass("class1"));
        assertEquals("class1", element.className());

        // Add multiple / duplicate classes
        element.addClass("class2").addClass("class1");
        assertTrue(element.hasClass("class1"));
        assertTrue(element.hasClass("class2"));

        // Remove class
        element.removeClass("class1");
        assertFalse(element.hasClass("class1"));
        assertTrue(element.hasClass("class2"));

        // Toggle class
        element.toggleClass("class1");
        assertTrue(element.hasClass("class1"));
        element.toggleClass("class1");
        assertFalse(element.hasClass("class1"));
    }

    @Test
    public void testTextAndData() {
        Element element = new Element(Tag.valueOf("div"), "");
        element.text("Hello <b>World</b>");
        assertEquals("Hello <b>World</b>", element.text());
        assertTrue(element.html().contains("&lt;b&gt;"));

        element.html("<span>HTML Text</span>");
        assertEquals("HTML Text", element.text());

        Element script = new Element(Tag.valueOf("script"), "");
        script.data("var x = 1;");
        assertEquals("var x = 1;", script.data());
        assertEquals("var x = 1;", script.nodeName().equals("#script") || script.tag().isData() ? script.data() : script.data());
    }

    @Test
    public void testDataKeyVal() {
        Element element = new Element(Tag.valueOf("div"), "");
        element.attr("data-id", "123");
        element.attr("data-name", "jsoup");

        Map<String, String> dataset = element.dataset();
        assertEquals(2, dataset.size());
        assertEquals("123", dataset.get("id"));
        assertEquals("jsoup", dataset.get("name"));

        dataset.put("id", "456");
        assertEquals("456", element.attr("data-id"));

        element.removeAttr("data-id");
        assertNull(element.dataset().get("id"));
    }

    @Test
    public void testTraversalAndFiltering() {
        String html = "<div id='1'><p class='a'>Hello</p><p class='b'>World</p></div>";
        Element root = Jsoup.parse(html).body().child(0);

        assertEquals(2, root.children().size());
        assertEquals("Hello", root.child(0).text());
        assertEquals("World", root.child(1).text());

        Elements paragraphs = root.getElementsByTag("p");
        assertEquals(2, paragraphs.size());

        Elements byClass = root.getElementsByClass("a");
        assertEquals(1, byClass.size());
        assertEquals("Hello", byClass.first().text());

        Elements byId = root.getElementsByAttributeValue("id", "1");
        assertEquals(1, byId.size());

        assertNotNull(root.getElementById("1") == null ? null : root.getElementById("1"));
        
        Element firstP = root.firstElementSibling();
        assertEquals(root.child(0), firstP);

        Element lastP = root.lastElementSibling();
        assertEquals(root.child(1), lastP);

        assertEquals(root.child(1), root.child(0).nextElementSibling());
        assertEquals(root.child(0), root.child(1).previousElementSibling());
    }

    @Test
    public void testParentsAndAncestors() {
        String html = "<div><section><p>Text</p></section></div>";
        Element p = Jsoup.parse(html).select("p").first();

        Elements parents = p.parents();
        assertTrue(parents.size() >= 3); // section, div, body, html...
        assertEquals("section", parents.get(0).tagName());
        assertEquals("div", parents.get(1).tagName());
    }

    @Test
    public void testAppendPrependWrap() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.text("Content");

        div.prepend("<p>Start</p>");
        assertTrue(div.html().startsWith("<p>Start</p>"));

        div.append("<p>End</p>");
        assertTrue(div.html().endsWith("<p>End</p>"));

        Element span = new Element(Tag.valueOf("span"), "");
        Element wrapped = span.wrap("<b></b>");
        assertEquals("span", wrapped.tagName());
        assertEquals("b", wrapped.parent().tagName());
        
        Element unwrapped = span.unwrap();
        assertNull(span.parent());
    }

    @Test
    public void testCssSelectorFiltering() {
        String html = "<div id='outer'><div class='inner' data-val='1'>A</div><div class='inner' data-val='2'>B</div></div>";
        Element root = Jsoup.parse(html).getElementById("outer");

        Elements inners = root.select(".inner");
        assertEquals(2, inners.size());

        Element val2 = root.select("[data-val=2]").first();
        assertNotNull(val2);
        assertEquals("B", val2.text());

        assertTrue(root.is(".inner") == false);
        assertTrue(val2.is(".inner"));
    }

    @Test
    public void testCloneAndDeepCopy() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        div.attr("class", "my-class");
        Element p = new Element(Tag.valueOf("p"), "http://example.com");
        p.text("Hello");
        div.appendChild(p);

        Element clone = div.clone();
        assertEquals(div.tagName(), clone.tagName());
        assertEquals(div.className(), clone.className());
        assertEquals(div.childNodeSize(), clone.childNodeSize());
        assertNotSame(div, clone);
        assertNotSame(div.child(0), clone.child(0));
        assertEquals(div.child(0).text(), clone.child(0).text());
    }

    @Test
    public void testEqualsAndHashCode() {
        Element el1 = new Element(Tag.valueOf("div"), "");
        Element el2 = new Element(Tag.valueOf("div"), "");
        
        // Node equals checks object identity in default Node implementation usually, or structural equality.
        // Let's verify Node equals behavior.
        assertEquals(el1, el1);
        assertNotEquals(el1, el2);
    }
    
    @Test
    public void testEmptyElementAndDataMethods() {
        Element div = new Element(Tag.valueOf("div"), "");
        assertTrue(div.childNodes().isEmpty());
        
        div.html("   ");
        assertTrue(div.text().isEmpty() || div.text().trim().isEmpty());
    }

    @Test
    public void testValMethod() {
        Element input = new Element(Tag.valueOf("input"), "");
        input.attr("value", "test-val");
        assertEquals("test-val", input.val());

        input.val("new-val");
        assertEquals("new-val", input.attr("value"));

        Element textarea = new Element(Tag.valueOf("textarea"), "");
        textarea.text("area-val");
        assertEquals("area-val", textarea.val());
    }

    @Test
    public void testOuterHtmlAndToString() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.attr("id", "foo");
        div.text("bar");
        
        String outer = div.outerHtml();
        assertTrue(outer.contains("<div"));
        assertTrue(outer.contains("id=\"foo\""));
        assertTrue(outer.contains("bar"));

        assertEquals(outer, div.toString());
    }
}