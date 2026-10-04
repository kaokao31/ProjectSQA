package org.jsoup.nodes;

import org.junit.Test;
import org.jsoup.helper.StringUtil;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class ElementTest {

    @Test
    public void testElementConstructorAndBasicGetters() {
        Tag tag = Tag.valueOf("div");
        Attributes attributes = new Attributes();
        attributes.put("id", "testId");
        Element el = new Element(tag, "http://example.com", attributes);

        assertEquals("div", el.tagName());
        assertEquals(tag, el.tag());
        assertEquals("http://example.com", el.baseUri());
        assertEquals("testId", el.id());
        assertTrue(el.hasAttr("id"));
        assertEquals("testId", el.attr("id"));
    }

    @Test
    public void testNodeName() {
        Element el = new Element(Tag.valueOf("span"), "");
        assertEquals("span", el.nodeName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullTagThrowsException() {
        new Element(null, "");
    }

    @Test
    public void testChildNodeManipulation() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child1 = new Element(Tag.valueOf("p"), "");
        Element child2 = new Element(Tag.valueOf("span"), "");

        parent.appendChild(child1);
        parent.prependChild(child2);

        assertEquals(2, parent.childNodeSize());
        assertEquals(child2, parent.childNode(0));
        assertEquals(child1, parent.childNode(1));

        assertEquals(child2, parent.children().get(0));
        assertEquals(child1, parent.children().get(1));

        // Test elementIndex
        assertEquals(0, child2.elementSiblingIndex());
        assertEquals(1, child1.elementSiblingIndex());
    }

    @Test
    public void testAppendTextAndPrependText() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.appendText("Hello");
        el.prependText("World ");

        assertEquals("World Hello", el.text());
        assertEquals("World ", el.ownText());
    }

    @Test
    public void testAttrMethods() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.attr("class", "container");
        assertEquals("container", el.attr("class"));

        el.removeAttr("class");
        assertFalse(el.hasAttr("class"));
    }

    @Test
    public void testDataset() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.attr("data-id", "123");
        el.attr("data-name", "jsoup");
        el.attr("class", "not-data");

        Map<String, String> dataset = el.dataset();
        assertEquals("123", dataset.get("id"));
        assertEquals("jsoup", dataset.get("name"));
        assertNull(dataset.get("class"));

        dataset.put("id", "456");
        assertEquals("456", el.attr("data-id"));

        el.attr("data-", "invalid");
        assertEquals("invalid", el.dataset().get(""));
    }

    @Test
    public void testParentAndSiblingTraversal() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element first = new Element(Tag.valueOf("p"), "");
        Element second = new Element(Tag.valueOf("p"), "");
        root.appendChild(first);
        root.appendChild(second);

        assertEquals(root, first.parent());
        assertEquals(root, second.parent());
        assertEquals(second, first.nextElementSibling());
        assertEquals(first, second.previousElementSibling());
        assertNull(first.previousElementSibling());
        assertNull(second.nextElementSibling());
    }

    @Test
    public void testElementSiblings() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element first = new Element(Tag.valueOf("p"), "");
        Element second = new Element(Tag.valueOf("p"), "");
        root.appendChild(first);
        root.appendChild(second);

        Elements siblings = first.siblingElements();
        assertEquals(1, siblings.size());
        assertEquals(second, siblings.get(0));

        Element solo = new Element(Tag.valueOf("div"), "");
        assertEquals(0, solo.siblingElements().size());
    }

    @Test
    public void testFirstAndLastChild() {
        Element root = new Element(Tag.valueOf("div"), "");
        assertNull(root.firstElementChild());
        assertNull(root.lastElementChild());

        Element first = new Element(Tag.valueOf("p"), "");
        Element last = new Element(Tag.valueOf("span"), "");
        root.appendChild(first);
        root.appendChild(last);

        assertEquals(first, root.firstElementChild());
        assertEquals(last, root.lastElementChild());
    }

    @Test
    public void testChildren() {
        Element root = new Element(Tag.valueOf("div"), "");
        root.appendChild(new Element(Tag.valueOf("p"), ""));
        root.appendChild(new TextNode("text", ""));
        root.appendChild(new Element(Tag.valueOf("span"), ""));

        Elements children = root.children();
        assertEquals(2, children.size());
        assertEquals("p", children.get(0).tagName());
        assertEquals("span", children.get(1).tagName());
    }

    @Test
    public void testGetElementById() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element child = new Element(Tag.valueOf("p"), "");
        child.attr("id", "target");
        root.appendChild(child);

        assertEquals(child, root.getElementById("target"));
        assertNull(root.getElementById("nonexistent"));
        assertNull(root.getElementById(""));
        assertNull(root.getElementById(null));
        
        // Test finding itself
        root.attr("id", "target");
        assertEquals(root, root.getElementById("target"));
    }

    @Test
    public void testGetElementsByTag() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element child1 = new Element(Tag.valueOf("p"), "");
        Element child2 = new Element(Tag.valueOf("p"), "");
        root.appendChild(child1);
        root.appendChild(child2);

        Elements ps = root.getElementsByTag("p");
        assertEquals(2, ps.size());

        Elements divs = root.getElementsByTag("div");
        assertEquals(1, divs.size());
        assertEquals(root, divs.get(0));

        assertEquals(0, root.getElementsByTag("span").size());
        assertEquals(0, root.getElementsByTag("").size());
        assertEquals(0, root.getElementsByTag(null).size());
    }

    @Test
    public void testGetElementsByClass() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element child1 = new Element(Tag.valueOf("p"), "");
        child1.addClass("foo bar");
        Element child2 = new Element(Tag.valueOf("span"), "");
        child2.addClass("bar baz");
        root.appendChild(child1);
        root.appendChild(child2);

        Elements bars = root.getElementsByClass("bar");
        assertEquals(2, bars.size());

        Elements foos = root.getElementsByClass("foo");
        assertEquals(1, foos.size());
        assertEquals(child1, foos.get(0));

        assertEquals(0, root.getElementsByClass("nonexistent").size());
        assertEquals(0, root.getElementsByClass("").size());
        assertEquals(0, root.getElementsByClass(null).size());
    }

    @Test
    public void testGetElementsByAttribute() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element child1 = new Element(Tag.valueOf("p"), "");
        child1.attr("data-test", "val1");
        Element child2 = new Element(Tag.valueOf("span"), "");
        child2.attr("data-test", "val2");
        root.appendChild(child1);
        root.appendChild(child2);

        Elements attrs = root.getElementsByAttribute("data-test");
        assertEquals(2, attrs.size());

        assertEquals(0, root.getElementsByAttribute("other").size());
        assertEquals(0, root.getElementsByAttribute("").size());
        assertEquals(0, root.getElementsByAttribute(null).size());
    }

    @Test
    public void testGetElementsByAttributeValue() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element child1 = new Element(Tag.valueOf("p"), "");
        child1.attr("data-test", "val1");
        Element child2 = new Element(Tag.valueOf("span"), "");
        child2.attr("data-test", "val2");
        root.appendChild(child1);
        root.appendChild(child2);

        Elements attrs = root.getElementsByAttributeValue("data-test", "val1");
        assertEquals(1, attrs.size());
        assertEquals(child1, attrs.get(0));

        assertEquals(0, root.getElementsByAttributeValue("data-test", "val3").size());
    }

    @Test
    public void testGetElementsByAttributeValueStarting() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element child1 = new Element(Tag.valueOf("p"), "");
        child1.attr("class", "foo-bar");
        root.appendChild(child1);

        assertEquals(1, root.getElementsByAttributeValueStarting("class", "foo").size());
        assertEquals(0, root.getElementsByAttributeValueStarting("class", "bar").size());
    }

    @Test
    public void testGetElementsByAttributeValueEnding() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element child1 = new Element(Tag.valueOf("p"), "");
        child1.attr("class", "foo-bar");
        root.appendChild(child1);

        assertEquals(1, root.getElementsByAttributeValueEnding("class", "bar").size());
        assertEquals(0, root.getElementsByAttributeValueEnding("class", "foo").size());
    }

    @Test
    public void testGetElementsByAttributeValueContaining() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element child1 = new Element(Tag.valueOf("p"), "");
        child1.attr("class", "foo-bar-baz");
        root.appendChild(child1);

        assertEquals(1, root.getElementsByAttributeValueContaining("class", "bar").size());
        assertEquals(0, root.getElementsByAttributeValueContaining("class", "qux").size());
    }

    @Test
    public void testGetElementsByAttributeValueMatching() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element child1 = new Element(Tag.valueOf("p"), "");
        child1.attr("class", "item-123");
        root.appendChild(child1);

        assertEquals(1, root.getElementsByAttributeValueMatching("class", "item-\\d+").size());
        assertEquals(0, root.getElementsByAttributeValueMatching("class", "item-[a-z]+").size());
    }

    @Test
    public void testGetElementsByIndexLessThan() {
        Element root = new Element(Tag.valueOf("div"), "");
        root.appendChild(new Element(Tag.valueOf("p"), ""));
        root.appendChild(new Element(Tag.valueOf("p"), ""));
        root.appendChild(new Element(Tag.valueOf("p"), ""));

        assertEquals(2, root.getElementsByIndexLessThan(2).size());
    }

    @Test
    public void testGetElementsByIndexGreaterThan() {
        Element root = new Element(Tag.valueOf("div"), "");
        root.appendChild(new Element(Tag.valueOf("p"), ""));
        root.appendChild(new Element(Tag.valueOf("p"), ""));
        root.appendChild(new Element(Tag.valueOf("p"), ""));

        assertEquals(1, root.getElementsByIndexGreaterThan(1).size());
    }

    @Test
    public void testGetElementsByIndexEquals() {
        Element root = new Element(Tag.valueOf("div"), "");
        root.appendChild(new Element(Tag.valueOf("p"), ""));
        root.appendChild(new Element(Tag.valueOf("p"), ""));
        root.appendChild(new Element(Tag.valueOf("p"), ""));

        assertEquals(1, root.getElementsByIndexEquals(1).size());
    }

    @Test
    public void testGetElementsContainingText() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element child1 = new Element(Tag.valueOf("p"), "");
        child1.appendText("Hello World");
        root.appendChild(child1);

        assertEquals(1, root.getElementsContainingText("World").size());
        assertEquals(0, root.getElementsContainingText("Missing").size());
    }

    @Test
    public void testGetElementsContainingOwnText() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element child1 = new Element(Tag.valueOf("p"), "");
        Element child2 = new Element(Tag.valueOf("span"), "");
        child2.appendText("Inner Text");
        child1.appendChild(child2);
        child1.appendText("Outer Text");
        root.appendChild(child1);

        assertEquals(1, child1.getElementsContainingOwnText("Outer Text").size());
        assertEquals(0, child1.getElementsContainingOwnText("Inner Text").size());
    }

    @Test
    public void testGetElementsMatchingText() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element child1 = new Element(Tag.valueOf("p"), "");
        child1.appendText("Price: $99.99");
        root.appendChild(child1);

        assertEquals(1, root.getElementsMatchingText("\\$\\d+\\.\\d+").size());
        assertEquals(0, root.getElementsMatchingText("abc").size());
    }

    @Test
    public void testGetElementsMatchingOwnText() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element child1 = new Element(Tag.valueOf("p"), "");
        Element child2 = new Element(Tag.valueOf("span"), "");
        child2.appendText("99");
        child1.appendChild(child2);
        child1.appendText("Price: 55");
        root.appendChild(child1);

        assertEquals(1, child1.getElementsMatchingOwnText("Price: \\d+").size());
    }

    @Test
    public void testClassMethods() {
        Element el = new Element(Tag.valueOf("div"), "");
        assertFalse(el.hasClass("test"));

        el.addClass("test");
        assertTrue(el.hasClass("test"));
        assertEquals("test", el.attr("class"));

        el.addClass("foo");
        assertTrue(el.hasClass("foo"));
        assertEquals("test foo", el.attr("class"));

        el.removeClass("test");
        assertFalse(el.hasClass("test"));
        assertTrue(el.hasClass("foo"));
        assertEquals("foo", el.attr("class"));

        el.toggleClass("foo");
        assertFalse(el.hasClass("foo"));

        el.toggleClass("foo");
        assertTrue(el.hasClass("foo"));
    }

    @Test
    public void testValMethods() {
        Element el = new Element(Tag.valueOf("textarea"), "");
        el.val("initial value");
        assertEquals("initial value", el.val());

        Element input = new Element(Tag.valueOf("input"), "");
        input.attr("value", "input val");
        assertEquals("input val", input.val());
        input.val("new val");
        assertEquals("new val", input.attr("value"));
    }

    @Test
    public void testHtmlAndAppendHtml() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.html("<p>Hello</p>");
        assertEquals("<p>Hello</p>", el.html());

        el.append("<p>World</p>");
        assertEquals(2, el.childNodeSize());
        assertEquals("World", el.child(1).text());
    }

    @Test
    public void testPrepend() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.html("<p>Second</p>");
        el.prepend("<p>First</p>");
        assertEquals("First", el.child(0).text());
        assertEquals("Second", el.child(1).text());
    }

    @Test
    public void testBeforeAndAfter() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element el1 = new Element(Tag.valueOf("p"), "");
        parent.appendChild(el1);

        Element beforeEl = new Element(Tag.valueOf("span"), "");
        el1.before(beforeEl);
        assertEquals(beforeEl, parent.childNode(0));
        assertEquals(el1, parent.childNode(1));

        Element afterEl = new Element(Tag.valueOf("a"), "");
        el1.after(afterEl);
        assertEquals(el1, parent.childNode(1));
        assertEquals(afterEl, parent.childNode(2));

        // Test string versions
        el1.before("<b>before</b>");
        el1.after("<b>after</b>");
        assertEquals(5, parent.childNodeSize());
    }

    @Test
    public void testWrap() {
        Element el = new Element(Tag.valueOf("span"), "");
        el.appendText("text");
        el.wrap("<div class='wrapper'></div>");
        
        Element parent = el.parent();
        assertEquals("div", parent.tagName());
        assertEquals("wrapper", parent.attr("class"));
    }

    @Test
    public void testUnwrap() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element wrapper = new Element(Tag.valueOf("span"), "");
        Element child = new Element(Tag.valueOf("p"), "");
        wrapper.appendChild(child);
        parent.appendChild(wrapper);

        Node unwrapResult = wrapper.unwrap();
        assertEquals(child, unwrapResult);
        assertEquals(child, parent.childNode(0));
        assertEquals(parent, child.parent());
    }

    @Test
    public void testEmpty() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.appendChild(new Element(Tag.valueOf("p"), ""));
        el.appendText("some text");
        assertEquals(2, el.childNodeSize());

        el.empty();
        assertEquals(0, el.childNodeSize());
    }

    @Test
    public void testRemove() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child = new Element(Tag.valueOf("p"), "");
        parent.appendChild(child);
        assertEquals(1, parent.childNodeSize());

        child.remove();
        assertEquals(0, parent.childNodeSize());
        assertNull(child.parent());
    }

    @Test
    public void testSelect() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element child1 = new Element(Tag.valueOf("p"), "");
        child1.addClass("cls");
        root.appendChild(child1);

        Elements found = root.select("p.cls");
        assertEquals(1, found.size());
        assertEquals(child1, found.get(0));
    }

    @Test
    public void testParents() {
        Element grandParent = new Element(Tag.valueOf("div"), "");
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child = new Element(Tag.valueOf("p"), "");

        grandParent.appendChild(parent);
        parent.appendChild(child);

        Elements parents = child.parents();
        assertEquals(2, parents.size());
        assertEquals(parent, parents.get(0));
        assertEquals(grandParent, parents.get(1));
    }

    @Test
    public void testDataNodesAndCss() {
        Element style = new Element(Tag.valueOf("style"), "");
        style.data("body { color: red; }");
        List<DataNode> dataNodes = style.dataNodes();
        assertEquals(1, dataNodes.size());
        assertEquals("body { color: red; }", dataNodes.get(0).getWholeData());
    }

    @Test
    public void testClone() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.attr("id", "myId");
        Element clone = el.clone();

        assertEquals(el.tagName(), clone.tagName());
        assertEquals(el.baseUri(), clone.baseUri());
        assertEquals(el.attr("id"), clone.attr("id"));
        assertNotSame(el, clone);
    }
}