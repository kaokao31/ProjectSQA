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
    public void testHtmlOutputWithEntitiesBug37() {
        // Specifically target Jsoup-37 where html() / html(StringBuilder, Document.OutputSettings)
        // or setting element html containing unescaped entities/tags behaves incorrectly
        // or when dealing with append / prepend / html with entities.
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.html("<p>Hello &amp; welcome</p>");
        
        // Verify that html() returns properly structured and formatted HTML
        String html = parent.html();
        assertNotNull(html);
        assertTrue(html.contains("<p>"));

        // Test appending or setting html via syntactic methods
        parent.html("");
        assertEquals("", parent.html());
    }

    @Test
    public void testElementConstructorAndBasics() {
        Tag tag = Tag.valueOf("div");
        Element el = new Element(tag, "http://example.com", new Attributes());
        
        assertEquals("div", el.tagName());
        assertEquals(tag, el.tag());
        assertEquals("http://example.com", el.baseUri());
        assertNotNull(el.attributes());
        assertTrue(el.childNodes().isEmpty());
    }

    @Test
    public void testChildNodeManipulation() {
        Element el = new Element(Tag.valueOf("div"), "");
        Element child1 = new Element(Tag.valueOf("span"), "").text("one");
        Element child2 = new Element(Tag.valueOf("span"), "").text("two");

        el.appendChild(child1);
        assertEquals(1, el.childNodeSize());
        assertEquals(child1, el.child(0));

        el.prependChild(child2);
        assertEquals(2, el.childNodeSize());
        assertEquals(child2, el.child(0));
        assertEquals(child1, el.child(1));

        child1.remove();
        assertEquals(1, el.childNodeSize());
        assertEquals(child2, el.child(0));
    }

    @Test
    public void testAttributesDelegation() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.attr("class", "my-class");
        el.attr("id", "my-id");

        assertEquals("my-class", el.attr("class"));
        assertEquals("my-id", el.id());
        assertTrue(el.hasClass("my-class"));

        el.removeAttr("class");
        assertFalse(el.hasClass("my-class"));
        assertEquals("", el.attr("class"));
    }

    @Test
    public void testTextAndData() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.text("Hello World");
        assertEquals("Hello World", el.text());
        assertEquals("<div />", el.normalName()); // depending on implementation or tag

        Element script = new Element(Tag.valueOf("script"), "");
        script.data("var x = 1;");
        assertEquals("var x = 1;", script.data());
    }

    @Test
    public void testTraversalAndQuery() {
        Document doc = Jsoup.parse("<div id='root'><p class='p1'>Text 1</p><p class='p2'>Text 2</p></div>");
        Element root = doc.getElementById("root");
        assertNotNull(root);

        Elements pElements = root.getElementsByTag("p");
        assertEquals(2, pElements.size());

        Element p1 = root.getElementsByClass("p1").first();
        assertNotNull(p1);
        assertEquals("Text 1", p1.text());

        assertNotNull(root.child(0));
        assertEquals(2, root.children().size());
    }

    @Test
    public void testSiblingAndParentTraversal() {
        Document doc = Jsoup.parse("<div><span id='s1'>1</span><span id='s2'>2</span><span id='s3'>3</span></div>");
        Element s2 = doc.getElementById("s2");
        assertNotNull(s2);

        Element next = s2.nextElementSibling();
        assertNotNull(next);
        assertEquals("s3", next.id());

        Element prev = s2.previousElementSibling();
        assertNotNull(prev);
        assertEquals("s1", prev.id());

        Element parent = s2.parent();
        assertNotNull(parent);
        assertEquals("div", parent.tagName());
    }

    @Test
    public void testClassOperations() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.addClass("foo").addClass("bar");
        
        Set<String> classNames = el.classNames();
        assertTrue(classNames.contains("foo"));
        assertTrue(classNames.contains("bar"));

        el.removeClass("foo");
        assertFalse(el.hasClass("foo"));
        assertTrue(el.hasClass("bar"));

        el.toggleClass("bar");
        assertFalse(el.hasClass("bar"));
        
        el.toggleClass("baz");
        assertTrue(el.hasClass("baz"));
    }

    @Test
    public void testCloneAndDeepCopy() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.attr("id", "test");
        Element child = new Element(Tag.valueOf("p"), "http://example.com").text("child");
        el.appendChild(child);

        Element clone = el.clone();
        assertEquals(el.tagName(), clone.tagName());
        assertEquals(el.id(), clone.id());
        assertEquals(el.childNodeSize(), clone.childNodeSize());
        assertNotSame(el, clone);
        assertNotSame(el.child(0), clone.child(0));
    }

    @Test
    public void testFormElementsAndData() {
        Document doc = Jsoup.parse("<form><input type='text' name='user' value='john'/><input type='checkbox' name='cb' checked/></form>");
        Element form = doc.select("form").first();
        assertNotNull(form);
        
        List<Connection.KeyVal> data = form.formData();
        assertFalse(data.isEmpty());
    }

    @Test
    public void testEmptyElementAndData() {
        Element el = new Element(Tag.valueOf("br"), "");
        assertTrue(el.tag().isEmpty());
    }

    @Test
    public void testWrapAndUnwrap() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div>");
        Element p = doc.select("p").first();
        assertNotNull(p);
        
        Element wrapped = p.wrap("<div class='wrapper'></div>");
        assertNotNull(wrapped);
        assertTrue(doc.select("div.wrapper").size() > 0);

        p.unwrap();
        assertTrue(doc.select("div.wrapper").isEmpty());
    }
}