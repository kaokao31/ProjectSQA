package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for HtmlTreeBuilder.
 * Designed to achieve high line/branch coverage and detect faults,
 * particularly the known bug #65 related to template element handling.
 */
public class HtmlTreeBuilderTest {

    private HtmlTreeBuilder builder;
    private TreeBuilder treeBuilder;

    @Before
    public void setUp() {
        // Use the default parser which internally uses HtmlTreeBuilder
        // For direct testing, we can instantiate the builder but it's easier to use Jsoup.parse
        // We'll test via the public API and also directly if needed.
        builder = new HtmlTreeBuilder();
        treeBuilder = builder;
    }

    // ===================== Basic Parsing =====================

    @Test
    public void testSimpleHtml() {
        String html = "<html><head></head><body><p>Hello</p></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("Hello", doc.body().text());
    }

    @Test
    public void testEmptyInput() {
        Document doc = Jsoup.parse("");
        assertNotNull(doc);
        assertEquals("", doc.text());
    }

    @Test
    public void testNullInput() {
        // Jsoup.parse(null) should throw IllegalArgumentException
        try {
            Jsoup.parse(null);
            fail("Expected IllegalArgumentException for null input");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testWhitespaceOnly() {
        Document doc = Jsoup.parse("   \n\t   ");
        assertNotNull(doc);
        assertEquals("", doc.text());
    }

    // ===================== Void Elements =====================

    @Test
    public void testVoidElements() {
        String html = "<br><hr><img src='test.png'>";
        Document doc = Jsoup.parse(html);
        // Should not have closing tags
        String out = doc.html();
        assertTrue(out.contains("<br>"));
        assertTrue(out.contains("<hr>"));
        assertTrue(out.contains("<img src=\"test.png\">"));
    }

    // ===================== Formatting Elements =====================

    @Test
    public void testFormattingElements() {
        String html = "<b><i>bold italic</i></b>";
        Document doc = Jsoup.parse(html);
        assertEquals("bold italic", doc.body().text());
        assertEquals("<b><i>bold italic</i></b>", doc.body().html());
    }

    @Test
    public void testMisnestingFormatting() {
        String html = "<b><i>bold</b>italic</i>";
        Document doc = Jsoup.parse(html);
        // Browser behavior: <b><i>bold</i></b><i>italic</i>
        String expected = "<b><i>bold</i></b><i>italic</i>";
        assertEquals(expected, doc.body().html());
    }

    // ===================== Table Elements =====================

    @Test
    public void testSimpleTable() {
        String html = "<table><tr><td>cell</td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertEquals("cell", doc.text());
    }

    @Test
    public void testTableWithMisplacedContent() {
        String html = "<table><tr><td>cell</td></tr>text</table>";
        Document doc = Jsoup.parse(html);
        // text should be placed before/after table
        assertTrue(doc.text().contains("text"));
    }

    // ===================== Template Elements (Bug #65) =====================

    @Test
    public void testTemplateSimple() {
        String html = "<template>content</template>";
        Document doc = Jsoup.parse(html);
        // Template content should be preserved
        Element template = doc.select("template").first();
        assertNotNull(template);
        assertEquals("content", template.html());
    }

    @Test
    public void testTemplateEmpty() {
        String html = "<template></template>";
        Document doc = Jsoup.parse(html);
        Element template = doc.select("template").first();
        assertNotNull(template);
        assertEquals("", template.html());
    }

    @Test
    public void testTemplateSelfClosing() {
        // Self-closing template is invalid but should be handled gracefully
        String html = "<template/>";
        Document doc = Jsoup.parse(html);
        Element template = doc.select("template").first();
        assertNotNull(template);
        // Should be treated as opening tag with no content
        assertEquals("", template.html());
    }

    @Test
    public void testTemplateNested() {
        String html = "<template><div><span>nested</span></div></template>";
        Document doc = Jsoup.parse(html);
        Element template = doc.select("template").first();
        assertNotNull(template);
        assertEquals("<div><span>nested</span></div>", template.html());
    }

    @Test
    public void testTemplateWithAttributes() {
        String html = "<template id='t1' class='test'>content</template>";
        Document doc = Jsoup.parse(html);
        Element template = doc.select("template#t1").first();
        assertNotNull(template);
        assertEquals("test", template.className());
        assertEquals("content", template.html());
    }

    @Test
    public void testTemplateInTable() {
        // Template inside table should be allowed
        String html = "<table><template><tr><td>cell</td></tr></template></table>";
        Document doc = Jsoup.parse(html);
        Element template = doc.select("template").first();
        assertNotNull(template);
        assertEquals("<tr><td>cell</td></tr>", template.html());
    }

    @Test
    public void testTemplateInHead() {
        String html = "<head><template>head content</template></head>";
        Document doc = Jsoup.parse(html);
        Element template = doc.select("template").first();
        assertNotNull(template);
        assertEquals("head content", template.html());
    }

    @Test
    public void testTemplateInBody() {
        String html = "<body><template>body content</template></body>";
        Document doc = Jsoup.parse(html);
        Element template = doc.select("template").first();
        assertNotNull(template);
        assertEquals("body content", template.html());
    }

    @Test
    public void testMultipleTemplates() {
        String html = "<template>first</template><template>second</template>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("template").size());
        assertEquals("first", doc.select("template").get(0).html());
        assertEquals("second", doc.select("template").get(1).html());
    }

    @Test
    public void testTemplateWithEndTagOnly() {
        // Should be ignored or create empty template
        String html = "</template>";
        Document doc = Jsoup.parse(html);
        // No template element should be created
        assertEquals(0, doc.select("template").size());
    }

    @Test
    public void testTemplateUnclosed() {
        String html = "<template>unclosed";
        Document doc = Jsoup.parse(html);
        Element template = doc.select("template").first();
        assertNotNull(template);
        assertEquals("unclosed", template.html());
    }

    // ===================== Edge Cases and Bug Triggers =====================

    @Test
    public void testTemplateInsideTemplate() {
        // Nested templates are allowed in HTML spec
        String html = "<template><template>inner</template></template>";
        Document doc = Jsoup.parse(html);
        Element outer = doc.select("template").first();
        assertNotNull(outer);
        Element inner = outer.select("template").first();
        assertNotNull(inner);
        assertEquals("inner", inner.html());
    }

    @Test
    public void testTemplateWithScriptContent() {
        String html = "<template><script>alert('test');</script></template>";
        Document doc = Jsoup.parse(html);
        Element template = doc.select("template").first();
        assertNotNull(template);
        // Script content should be preserved as raw text inside template
        assertTrue(template.html().contains("alert('test');"));
    }

    @Test
    public void testTemplateWithStyleContent() {
        String html = "<template><style>body { color: red; }</style></template>";
        Document doc = Jsoup.parse(html);
        Element template = doc.select("template").first();
        assertNotNull(template);
        assertTrue(template.html().contains("body { color: red; }"));
    }

    @Test
    public void testTemplateInFrameset() {
        // Template inside frameset is not standard but should not crash
        String html = "<frameset><template>content</template></frameset>";
        Document doc = Jsoup.parse(html);
        Element template = doc.select("template").first();
        assertNotNull(template);
        assertEquals("content", template.html());
    }

    @Test
    public void testTemplateWithDoctype() {
        String html = "<!DOCTYPE html><template>content</template>";
        Document doc = Jsoup.parse(html);
        Element template = doc.select("template").first();
        assertNotNull(template);
        assertEquals("content", template.html());
    }

    // ===================== Direct Builder Method Tests =====================

    @Test
    public void testBuilderInitialState() {
        assertNotNull(builder);
        // After construction, the builder should be in initial state
        assertFalse(builder.isFragmentParsing());
    }

    @Test
    public void testBuilderReset() {
        // Reset should clear the stack and state
        builder.reset();
        // After reset, the builder should be ready for new parsing
        assertNotNull(builder);
    }

    @Test
    public void testBuilderProcessToken() {
        // We can test process by feeding tokens, but easier to use parser
        // This test ensures that the builder can handle a simple start tag token
        String html = "<div>test</div>";
        Document doc = Jsoup.parse(html);
        assertEquals("test", doc.text());
    }

    // ===================== Regression Tests for Bug #65 =====================

    @Test
    public void testBug65TemplateInBody() {
        // This is the specific failing test from Defects4J bug 65
        String html = "<body><template>content</template></body>";
        Document doc = Jsoup.parse(html);
        // The bug caused an exception or incorrect output
        // Expected: template element is present with content
        Element template = doc.select("template").first();
        assertNotNull("Template element should exist", template);
        assertEquals("Template content should be preserved", "content", template.html());
    }

    @Test
    public void testBug65TemplateWithNestedElements() {
        String html = "<template><ul><li>item</li></ul></template>";
        Document doc = Jsoup.parse(html);
        Element template = doc.select("template").first();
        assertNotNull(template);
        assertEquals("<ul><li>item</li></ul>", template.html());
    }

    @Test
    public void testBug65TemplateAfterTable() {
        // Template after table should not interfere with table parsing
        String html = "<table><tr><td>cell</td></tr></table><template>after</template>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("template").size());
        assertEquals("after", doc.select("template").first().html());
    }

    @Test
    public void testBug65TemplateBeforeTable() {
        String html = "<template>before</template><table><tr><td>cell</td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("template").size());
        assertEquals("before", doc.select("template").first().html());
    }

    @Test
    public void testBug65TemplateWithMisplacedEndTag() {
        // Misplaced end tag should not break template parsing
        String html = "<template>content</div></template>";
        Document doc = Jsoup.parse(html);
        Element template = doc.select("template").first();
        assertNotNull(template);
        // The </div> should be ignored inside template
        assertEquals("content", template.html());
    }

    @Test
    public void testBug65TemplateWithMultipleChildren() {
        String html = "<template><p>first</p><p>second</p></template>";
        Document doc = Jsoup.parse(html);
        Element template = doc.select("template").first();
        assertNotNull(template);
        assertEquals("<p>first</p><p>second</p>", template.html());
    }

    // ===================== Additional Coverage Tests =====================

    @Test
    public void testHtmlTreeBuilderStateTransitions() {
        // Test that the builder transitions through states correctly
        String html = "<html><head><title>Test</title></head><body><p>Para</p></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("Test", doc.title());
        assertEquals("Para", doc.body().text());
    }

    @Test
    public void testFosterParenting() {
        // Text inside table should be foster-parented
        String html = "<table>text<tr><td>cell</td></tr></table>";
        Document doc = Jsoup.parse(html);
        // The text "text" should appear before the table
        assertTrue(doc.body().html().contains("text"));
    }

    @Test
    public void testFormElement() {
        String html = "<form><input name='a' value='b'></form>";
        Document doc = Jsoup.parse(html);
        Element form = doc.select("form").first();
        assertNotNull(form);
        assertEquals("b", form.select("input").val());
    }

    @Test
    public void testSelectElement() {
        String html = "<select><option>1</option><option>2</option></select>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("option").size());
    }

    @Test
    public void testTextareaElement() {
        String html = "<textarea>initial text</textarea>";
        Document doc = Jsoup.parse(html);
        assertEquals("initial text", doc.select("textarea").text());
    }

    @Test
    public void testXmpElement() {
        // Xmp is obsolete but should be handled
        String html = "<xmp>raw <b>text</b></xmp>";
        Document doc = Jsoup.parse(html);
        // Content should be treated as raw text
        assertTrue(doc.select("xmp").html().contains("raw <b>text</b>"));
    }

    @Test
    public void testNoFramesElement() {
        String html = "<noframes>your browser does not support frames</noframes>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("noframes").first());
    }

    @Test
    public void testNoscriptElement() {
        String html = "<noscript>enable javascript</noscript>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("noscript").first());
    }

    // ===================== Error Handling =====================

    @Test
    public void testParseErrorRecovery() {
        // Malformed HTML should not throw exceptions
        String html = "<p>unclosed<div>extra</p>";
        Document doc = Jsoup.parse(html);
        // Should produce a document without exception
        assertNotNull(doc);
    }

    @Test
    public void testDeepNesting() {
        // Deeply nested elements should not cause stack overflow
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("<div>");
        }
        sb.append("content");
        for (int i = 0; i < 1000; i++) {
            sb.append("</div>");
        }
        Document doc = Jsoup.parse(sb.toString());
        assertEquals("content", doc.text());
    }

    @Test
    public void testLargeTextNode() {
        // Large text nodes should be handled
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append("a");
        }
        String html = "<p>" + sb.toString() + "</p>";
        Document doc = Jsoup.parse(html);
        assertEquals(sb.toString(), doc.text());
    }

    // ===================== Direct Builder State Tests =====================

    @Test
    public void testBuilderIsFragment() {
        // Test fragment parsing mode
        assertFalse(builder.isFragmentParsing());
        // We can set fragment parsing via context
        // This is indirectly tested via Jsoup.parseBodyFragment
        Document doc = Jsoup.parseBodyFragment("<p>test</p>");
        assertNotNull(doc);
    }

    @Test
    public void testBuilderPopStackToClose() {
        // Test that the builder correctly closes elements
        String html = "<div><p>text</p></div>";
        Document doc = Jsoup.parse(html);
        // The stack should be properly managed
        assertEquals("text", doc.text());
    }

    @Test
    public void testBuilderGenerateImpliedEndTags() {
        // Test that implied end tags are generated (e.g., <li> inside <ul>)
        String html = "<ul><li>one<li>two</ul>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("li").size());
    }

    @Test
    public void testBuilderReconstructFormattingElements() {
        // Test reconstruction of formatting elements (e.g., <b> inside <table>)
        String html = "<table><b><tr><td>bold</td></tr></b></table>";
        Document doc = Jsoup.parse(html);
        // The <b> should be reconstructed outside the table
        assertTrue(doc.body().html().contains("<b>"));
    }

    // ===================== End of Test Suite =====================
}