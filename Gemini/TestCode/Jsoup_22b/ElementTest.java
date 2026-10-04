package org.jsoup.nodes;

import org.junit.Test;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class ElementTest {

    @Test
    public void testElementConstructorAndBasicGetters() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        assertNotNull(el);
        assertEquals("div", el.tagName());
        assertEquals("http://example.com", el.baseUri());
        assertEquals(0, el.childNodeSize());
    }

    @Test
    public void testTagNameAndNormalizing() {
        Element el = new Element(Tag.valueOf("DIV"), "http://example.com");
        assertEquals("div", el.tagName());
        
        el.tagName("SPAN");
        assertEquals("span", el.tagName());
        
        Tag newTag = Tag.valueOf("p");
        el.tag(newTag);
        assertEquals(newTag, el.tag());
    }

    @Test
    public void testParentAndSiblingManipulation() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child1 = new Element(Tag.valueOf("p"), "");
        Element child2 = new Element(Tag.valueOf("span"), "");

        parent.appendChild(child1);
        parent.appendChild(child2);

        assertEquals(parent, child1.parent());
        assertEquals(parent, child2.parent());

        assertEquals(child2, child1.nextElementSibling());
        assertEquals(child1, child2.previousElementSibling());
        assertNull(child1.previousElementSibling());
        assertNull(child2.nextElementSibling());

        // Test elementSiblings
        Set<Element> siblings = child1.elementSiblings();
        assertTrue(siblings.contains(child2));
        assertFalse(siblings.contains(child1));
    }

    @Test
    public void testChildElementNavigation() {
        Element root = Jsoup.parse("<div><p>One</p><span>Two</span><a href='#'>Three</a></div>").body().child(0);

        assertEquals(3, root.children().size());
        assertEquals("p", root.child(0).tagName());
        assertEquals("span", root.child(1).tagName());
        assertEquals("a", root.child(2).tagName());

        Element first = root.firstElementSibling();
        Element last = root.lastElementSibling();

        assertEquals("p", first.tagName());
        assertEquals("a", last.tagName());
    }

    @Test
    public void testAttributesManagement() {
        Element el = new Element(Tag.valueOf("a"), "http://example.com");
        el.attr("href", "http://jsoup.org");
        el.attr("class", "external");

        assertEquals("http://jsoup.org", el.attr("href"));
        assertEquals("external", el.attr("class"));
        assertTrue(el.hasAttr("href"));
        assertFalse(el.hasAttr("id"));

        el.removeAttr("class");
        assertFalse(el.hasAttr("class"));

        // Dataset attributes
        Map<String, String> dataset = el.dataset();
        assertNotNull(dataset);

        el.attr("data-test-id", "123");
        assertEquals("123", el.dataset().get("testId"));
    }

    @Test
    public void testHtmlAndTextManipulation() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.text("Hello & welcome");
        assertEquals("Hello & welcome", el.text());
        // Verify HTML escaping or child creation
        assertTrue(el.html().contains("Hello"));

        el.html("<p>Child Paragraph</p>");
        assertEquals(1, el.childNodeSize());
        assertEquals("Child Paragraph", el.child(0).text());

        String wholeText = el.wholeText();
        assertNotNull(wholeText);
    }

    @Test
    public void testDataMethods() {
        Element script = new Element(Tag.valueOf("script"), "");
        script.appendChild(new DataNode("var x = 1;", ""));
        assertEquals("var x = 1;", script.data());

        Element div = new Element(Tag.valueOf("div"), "");
        assertEquals("", div.data());
    }

    @Test
    public void testClassNameOperations() {
        Element el = new Element(Tag.valueOf("div"), "");
        
        assertFalse(el.hasClass("foo"));
        
        el.addClass("foo");
        assertTrue(el.hasClass("foo"));
        assertEquals("foo", el.className());

        el.addClass("bar");
        assertTrue(el.hasClass("foo"));
        assertTrue(el.hasClass("bar"));

        el.removeClass("foo");
        assertFalse(el.hasClass("foo"));
        assertTrue(el.hasClass("bar"));

        el.toggleClass("bar");
        assertFalse(el.hasClass("bar"));

        el.toggleClass("bar");
        assertTrue(el.hasClass("bar"));
    }

    @Test
    public void testValOperations() {
        Element input = new Element(Tag.valueOf("input"), "");
        input.attr("value", "initial");
        assertEquals("initial", input.val());

        input.val("updated");
        assertEquals("updated", input.attr("value"));

        Element textarea = new Element(Tag.valueOf("textarea"), "");
        textarea.text("area text");
        assertEquals("area text", textarea.val());
    }

    @Test
    public void testAppendPrependAndTextMethods() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.text("text1");
        assertEquals("text1", el.text());

        el.append("<p>appended</p>");
        assertEquals(2, el.childNodeSize());
        assertEquals("appended", el.child(0).text()); // Wait, append to body puts it at the end

        el.prepend("<p>prepended</p>");
        assertEquals("prepended", el.child(0).text());

        Element wrapped = el.wrap("<div id='wrapper'></div>");
        assertNotNull(wrapped);
        assertEquals("wrapper", el.parent().id());
    }

    @Test
    public void testEmptyAndRemove() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.appendChild(new Element(Tag.valueOf("p"), ""));
        assertEquals(1, el.childNodeSize());

        el.empty();
        assertEquals(0, el.childNodeSize());

        Element child = new Element(Tag.valueOf("span"), "");
        el.appendChild(child);
        assertEquals(1, el.childNodeSize());
        
        child.remove();
        assertEquals(0, el.childNodeSize());
    }

    @Test
    public void testSelectAndParents() {
        Element doc = Jsoup.parse("<div><p class='target'>Text</p></div>").body();
        Elements found = doc.select(".target");
        assertEquals(1, found.size());
        assertEquals("p", found.first().tagName());

        Element p = found.first();
        Elements parents = p.parents();
        assertTrue(parents.size() >= 2); // div and body (and html depending on parse)
    }

    @Test
    public void testElementClone() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.attr("id", "test-id");
        Element clone = el.clone();

        assertNotSame(el, clone);
        assertEquals(el.tagName(), clone.tagName());
        assertEquals(el.baseUri(), clone.baseUri());
        assertEquals(el.id(), clone.id());
    }

    @Test
    public void testIdAndElementIdHandling() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.attr("id", "my-id");
        assertEquals("my-id", el.id());
    }

    @Test
    public void testOuterHtmlGeneration() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.attr("id", "foo");
        String outer = el.outerHtml();
        assertTrue(outer.contains("<div"));
        assertTrue(outer.contains("id=\"foo\""));
    }

    @Test
    public void testNodeName() {
        Element el = new Element(Tag.valueOf("span"), "");
        assertEquals("span", el.nodeName());
    }

    @Test
    public void testGetElementsByTagAndAttribute() {
        Element root = Jsoup.parse("<div><a href='1' class='a'>Link1</a><a href='2' class='b'>Link2</a></div>").body();
        Elements as = root.getElementsByTag("a");
        assertEquals(2, as.size());

        Elements byClass = root.getElementsByClass("b");
        assertEquals(1, byClass.size());
        assertEquals("Link2", byClass.first().text());

        Elements byAttribute = root.getElementsByAttribute("href");
        assertEquals(2, byAttribute.size());

        Elements byAttributeValue = root.getElementsByAttributeValue("href", "1");
        assertEquals(1, byAttributeValue.size());
        assertEquals("Link1", byAttributeValue.first().text());
    }
}