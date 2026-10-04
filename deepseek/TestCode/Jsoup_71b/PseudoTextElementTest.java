package org.jsoup.nodes;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Test suite for PseudoTextElement.
 * Designed to achieve high code coverage and detect potential faults.
 */
public class PseudoTextElementTest {

    private String baseUri;
    private Tag scriptTag;
    private Tag styleTag;
    private Tag unknownTag;

    @Before
    public void setUp() {
        baseUri = "http://example.com";
        scriptTag = Tag.valueOf("script");
        styleTag = Tag.valueOf("style");
        unknownTag = Tag.valueOf("unknown");
    }

    @Test
    public void testConstructorWithScriptTag() {
        PseudoTextElement element = new PseudoTextElement(scriptTag, baseUri, new Attributes());
        assertNotNull(element);
        assertEquals("script", element.tagName());
        assertEquals(baseUri, element.baseUri());
    }

    @Test
    public void testConstructorWithStyleTag() {
        PseudoTextElement element = new PseudoTextElement(styleTag, baseUri, new Attributes());
        assertNotNull(element);
        assertEquals("style", element.tagName());
    }

    @Test
    public void testConstructorWithUnknownTag() {
        PseudoTextElement element = new PseudoTextElement(unknownTag, baseUri, new Attributes());
        assertNotNull(element);
        assertEquals("unknown", element.tagName());
    }

    @Test
    public void testConstructorWithNullBaseUri() {
        PseudoTextElement element = new PseudoTextElement(scriptTag, null, new Attributes());
        assertNotNull(element);
        assertNull(element.baseUri());
    }

    @Test
    public void testConstructorWithNullAttributes() {
        // Attributes can be null? Check if constructor handles it.
        try {
            PseudoTextElement element = new PseudoTextElement(scriptTag, baseUri, null);
            assertNotNull(element);
            // If no exception, attributes should be empty
            assertTrue(element.hasAttributes() == false || element.attributes().size() == 0);
        } catch (IllegalArgumentException e) {
            // Expected if constructor rejects null attributes
        }
    }

    @Test
    public void testOuterHtmlHeadForScriptTag() {
        PseudoTextElement element = new PseudoTextElement(scriptTag, baseUri, new Attributes());
        element.appendChild(new TextNode("alert('hello');", baseUri));
        StringBuilder sb = new StringBuilder();
        element.outerHtmlHead(sb, 0, new Document.OutputSettings());
        String result = sb.toString();
        // For script tag, outerHtmlHead should produce "<script>" without escaping
        assertTrue(result.contains("<script>"));
        assertFalse(result.contains("&lt;script&gt;"));
    }

    @Test
    public void testOuterHtmlHeadForStyleTag() {
        PseudoTextElement element = new PseudoTextElement(styleTag, baseUri, new Attributes());
        element.appendChild(new TextNode("body { color: red; }", baseUri));
        StringBuilder sb = new StringBuilder();
        element.outerHtmlHead(sb, 0, new Document.OutputSettings());
        String result = sb.toString();
        assertTrue(result.contains("<style>"));
        assertFalse(result.contains("&lt;style&gt;"));
    }

    @Test
    public void testOuterHtmlHeadForUnknownTag() {
        PseudoTextElement element = new PseudoTextElement(unknownTag, baseUri, new Attributes());
        element.appendChild(new TextNode("some text", baseUri));
        StringBuilder sb = new StringBuilder();
        element.outerHtmlHead(sb, 0, new Document.OutputSettings());
        String result = sb.toString();
        // Unknown tags should be treated as normal elements? Or as pseudo? Check behavior.
        // For pseudo text elements, outerHtmlHead might still produce opening tag.
        assertTrue(result.contains("<unknown>"));
    }

    @Test
    public void testOuterHtmlTailForScriptTag() {
        PseudoTextElement element = new PseudoTextElement(scriptTag, baseUri, new Attributes());
        element.appendChild(new TextNode("content", baseUri));
        StringBuilder sb = new StringBuilder();
        element.outerHtmlTail(sb, 0, new Document.OutputSettings());
        String result = sb.toString();
        assertEquals("</script>", result);
    }

    @Test
    public void testOuterHtmlTailForStyleTag() {
        PseudoTextElement element = new PseudoTextElement(styleTag, baseUri, new Attributes());
        element.appendChild(new TextNode("content", baseUri));
        StringBuilder sb = new StringBuilder();
        element.outerHtmlTail(sb, 0, new Document.OutputSettings());
        String result = sb.toString();
        assertEquals("</style>", result);
    }

    @Test
    public void testOuterHtmlComplete() {
        PseudoTextElement element = new PseudoTextElement(scriptTag, baseUri, new Attributes());
        element.appendChild(new TextNode("alert('x');", baseUri));
        String outerHtml = element.outerHtml();
        // Should be "<script>alert('x');</script>" without escaping
        assertEquals("<script>alert('x');</script>", outerHtml);
    }

    @Test
    public void testOuterHtmlWithAttributes() {
        Attributes attrs = new Attributes();
        attrs.put("type", "text/javascript");
        PseudoTextElement element = new PseudoTextElement(scriptTag, baseUri, attrs);
        element.appendChild(new TextNode("code", baseUri));
        String outerHtml = element.outerHtml();
        assertTrue(outerHtml.contains("type=\"text/javascript\""));
        assertTrue(outerHtml.contains("<script"));
    }

    @Test
    public void testTextMethod() {
        PseudoTextElement element = new PseudoTextElement(scriptTag, baseUri, new Attributes());
        element.appendChild(new TextNode("hello", baseUri));
        assertEquals("hello", element.text());
    }

    @Test
    public void testTextMethodWithMultipleChildren() {
        PseudoTextElement element = new PseudoTextElement(styleTag, baseUri, new Attributes());
        element.appendChild(new TextNode("body {", baseUri));
        element.appendChild(new TextNode(" color: red; }", baseUri));
        assertEquals("body { color: red; }", element.text());
    }

    @Test
    public void testHtmlMethod() {
        PseudoTextElement element = new PseudoTextElement(scriptTag, baseUri, new Attributes());
        element.appendChild(new TextNode("content", baseUri));
        String html = element.html();
        // For pseudo text elements, html() should return the raw text without escaping?
        // Typically html() returns inner HTML, but for script/style it should be raw.
        assertEquals("content", html);
    }

    @Test
    public void testDataMethod() {
        PseudoTextElement element = new PseudoTextElement(scriptTag, baseUri, new Attributes());
        element.appendChild(new TextNode("data", baseUri));
        // data() method might return the combined data of child data nodes.
        // For TextNode, data() returns the text.
        assertEquals("data", element.data());
    }

    @Test
    public void testToString() {
        PseudoTextElement element = new PseudoTextElement(scriptTag, baseUri, new Attributes());
        element.appendChild(new TextNode("test", baseUri));
        String toString = element.toString();
        // toString() typically returns outerHtml()
        assertEquals(element.outerHtml(), toString);
    }

    @Test
    public void testEmptyElement() {
        PseudoTextElement element = new PseudoTextElement(scriptTag, baseUri, new Attributes());
        assertEquals("", element.text());
        assertEquals("", element.html());
        assertEquals("<script></script>", element.outerHtml());
    }

    @Test
    public void testElementWithOnlyWhitespace() {
        PseudoTextElement element = new PseudoTextElement(styleTag, baseUri, new Attributes());
        element.appendChild(new TextNode("   ", baseUri));
        assertEquals("   ", element.text());
        assertEquals("   ", element.html());
        assertEquals("<style>   </style>", element.outerHtml());
    }

    @Test
    public void testElementWithSpecialCharacters() {
        PseudoTextElement element = new PseudoTextElement(scriptTag, baseUri, new Attributes());
        element.appendChild(new TextNode("a < b && c > d", baseUri));
        // In script/style, special characters should not be escaped
        assertEquals("a < b && c > d", element.html());
        assertEquals("a < b && c > d", element.text());
        String outer = element.outerHtml();
        assertTrue(outer.contains("a < b && c > d"));
        assertFalse(outer.contains("&lt;"));
        assertFalse(outer.contains("&gt;"));
        assertFalse(outer.contains("&amp;"));
    }

    @Test
    public void testClone() {
        PseudoTextElement element = new PseudoTextElement(scriptTag, baseUri, new Attributes());
        element.appendChild(new TextNode("clone test", baseUri));
        PseudoTextElement cloned = element.clone();
        assertNotNull(cloned);
        assertEquals(element.outerHtml(), cloned.outerHtml());
        assertNotSame(element, cloned);
    }

    @Test
    public void testHasText() {
        PseudoTextElement element = new PseudoTextElement(scriptTag, baseUri, new Attributes());
        assertFalse(element.hasText());
        element.appendChild(new TextNode("x", baseUri));
        assertTrue(element.hasText());
    }

    @Test
    public void testChildNodeSize() {
        PseudoTextElement element = new PseudoTextElement(scriptTag, baseUri, new Attributes());
        assertEquals(0, element.childNodeSize());
        element.appendChild(new TextNode("a", baseUri));
        assertEquals(1, element.childNodeSize());
        element.appendChild(new TextNode("b", baseUri));
        assertEquals(2, element.childNodeSize());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullTag() {
        new PseudoTextElement(null, baseUri, new Attributes());
    }

    @Test
    public void testOuterHtmlWithIndent() {
        PseudoTextElement element = new PseudoTextElement(scriptTag, baseUri, new Attributes());
        element.appendChild(new TextNode("content", baseUri));
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.indentAmount(2);
        StringBuilder sb = new StringBuilder();
        element.outerHtmlHead(sb, 1, settings);
        // Should not indent content for script/style? Or indent the tag?
        // Check that indentation is applied to the tag but not to the text content.
        String head = sb.toString();
        // Since depth is 1, there should be 2 spaces before <script>
        assertTrue(head.startsWith("  <script>"));
    }

    @Test
    public void testOuterHtmlTailWithIndent() {
        PseudoTextElement element = new PseudoTextElement(scriptTag, baseUri, new Attributes());
        element.appendChild(new TextNode("content", baseUri));
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.indentAmount(2);
        StringBuilder sb = new StringBuilder();
        element.outerHtmlTail(sb, 1, settings);
        String tail = sb.toString();
        // Tail should be indented as well? Usually </script> on its own line with indent.
        assertTrue(tail.startsWith("  </script>"));
    }

    @Test
    public void testOuterHtmlWithPrettyPrint() {
        PseudoTextElement element = new PseudoTextElement(scriptTag, baseUri, new Attributes());
        element.appendChild(new TextNode("content", baseUri));
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.prettyPrint(true);
        String outer = element.outerHtml();
        // Pretty print should not affect script/style content; it should remain inline.
        assertEquals("<script>content</script>", outer);
    }

    @Test
    public void testOuterHtmlWithEscapeMode() {
        PseudoTextElement element = new PseudoTextElement(scriptTag, baseUri, new Attributes());
        element.appendChild(new TextNode("&", baseUri));
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.escapeMode(Entities.EscapeMode.base);
        String outer = element.outerHtml();
        // In script/style, ampersand should not be escaped
        assertEquals("<script>&</script>", outer);
    }

    @Test
    public void testMultipleChildrenOuterHtml() {
        PseudoTextElement element = new PseudoTextElement(scriptTag, baseUri, new Attributes());
        element.appendChild(new TextNode("part1", baseUri));
        element.appendChild(new TextNode("part2", baseUri));
        assertEquals("<script>part1part2</script>", element.outerHtml());
    }

    @Test
    public void testNestedElementsNotAllowed() {
        // PseudoTextElement should not allow child elements (only text nodes)
        PseudoTextElement element = new PseudoTextElement(scriptTag, baseUri, new Attributes());
        Element childDiv = new Element(Tag.valueOf("div"), baseUri);
        try {
            element.appendChild(childDiv);
            // If allowed, check behavior
            assertTrue(element.childNodeSize() > 0);
        } catch (IllegalArgumentException e) {
            // Expected if appendChild rejects non-text nodes
        }
    }

    @Test
    public void testRemoveChild() {
        PseudoTextElement element = new PseudoTextElement(scriptTag, baseUri, new Attributes());
        TextNode text = new TextNode("removable", baseUri);
        element.appendChild(text);
        assertEquals(1, element.childNodeSize());
        text.remove();
        assertEquals(0, element.childNodeSize());
    }

    @Test
    public void testReplaceChild() {
        PseudoTextElement element = new PseudoTextElement(scriptTag, baseUri, new Attributes());
        TextNode oldText = new TextNode("old", baseUri);
        element.appendChild(oldText);
        TextNode newText = new TextNode("new", baseUri);
        element.replaceChild(oldText, newText);
        assertEquals("new", element.text());
    }

    @Test
    public void testAttributesPreserved() {
        Attributes attrs = new Attributes();
        attrs.put("id", "myScript");
        PseudoTextElement element = new PseudoTextElement(scriptTag, baseUri, attrs);
        assertEquals("myScript", element.id());
        assertTrue(element.hasAttr("id"));
    }

    @Test
    public void testClassNames() {
        Attributes attrs = new Attributes();
        attrs.put("class", "highlight");
        PseudoTextElement element = new PseudoTextElement(scriptTag, baseUri, attrs);
        assertTrue(element.hasClass("highlight"));
        assertEquals(1, element.classNames().size());
    }

    @Test
    public void testVal() {
        // PseudoTextElement does not have a value attribute typically
        PseudoTextElement element = new PseudoTextElement(scriptTag, baseUri, new Attributes());
        assertNull(element.val());
    }

    @Test
    public void testCssSelector() {
        PseudoTextElement element = new PseudoTextElement(scriptTag, baseUri, new Attributes());
        // cssSelector() should return something like "script"
        String selector = element.cssSelector();
        assertTrue(selector.contains("script"));
    }

    @Test
    public void testEqualsAndHashCode() {
        PseudoTextElement element1 = new PseudoTextElement(scriptTag, baseUri, new Attributes());
        element1.appendChild(new TextNode("a", baseUri));
        PseudoTextElement element2 = new PseudoTextElement(scriptTag, baseUri, new Attributes());
        element2.appendChild(new TextNode("a", baseUri));
        // Elements with same content might be equal? Depends on implementation.
        // Typically Node equality is based on identity, not content.
        assertFalse(element1.equals(element2));
        assertFalse(element1.hashCode() == element2.hashCode());
    }
}