package org.jsoup.nodes;

import org.junit.Test;
import java.io.IOException;

import static org.junit.Assert.*;

public class CommentTest {

    @Test
    public void testConstructorAndGetData() {
        Comment comment = new Comment("some comment");
        assertEquals("some comment", comment.getData());
        assertEquals("#comment", comment.nodeName());
    }

    @Test
    public void testOuterHtmlHtml() throws IOException {
        Comment comment = new Comment("test comment");
        StringBuilder accum = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        
        // Default output settings (prettyPrint = true)
        out.prettyPrint(true);
        comment.outerHtmlHead(accum, 0, out);
        // Assuming indent is 0, outerHtmlHead appends "<!--", data, "-->"
        // Wait, let's check outerHtmlTail or outerHtmlHead implementations.
        // Usually Comment doesn't override outerHtmlTail, but let's check outerHtmlHead:
        // accum.append("<!--").append(getData()).append("-->"); plus maybe formatting if prettyPrint.
        // Let's test outerHtml() directly which uses outerHtmlHead/Tail.
        
        String html = comment.outerHtml();
        assertEquals("<!--test comment-->", html);
    }

    @Test
    public void testAsXmlDeclaration() {
        // Test a comment that is NOT an XML declaration
        Comment comment = new Comment("not an xml");
        assertNull(comment.asXmlDeclaration());

        // Test a comment that LOOKS LIKE an XML declaration: starts with '!' or '?'
        // Jsoup's Comment.asXmlDeclaration() usually checks if it can be parsed as XmlDeclaration.
        // Let's test with a valid-looking declaration or bang/question mark.
        Comment commentBang = new Comment("!xml version=\"1.0\"?");
        XmlDeclaration decl = commentBang.asXmlDeclaration();
        // Depending on Jsoup 86 implementation, it might return null or a decl. 
        // Let's exercise the code paths for asXmlDeclaration.
        Comment commentQ = new Comment("?xml version=\"1.0\"?");
        XmlDeclaration declQ = commentQ.asXmlDeclaration();
        
        // Also test an empty comment or blank comment
        Comment emptyComment = new Comment("");
        assertNull(emptyComment.asXmlDeclaration());
    }

    @Test
    public void testAsXmlDeclarationBug86Specific() {
        // Bug 86 in Jsoup involves XmlDeclaration parsing from a Comment, 
        // specifically when the comment starts with '!' or '?', e.g., <!--> or similar edge cases,
        // or when asXmlDeclaration() encounters an IndexOutOfBoundsException or similar.
        // Let's test various edge cases for data content.
        
        Comment c1 = new Comment("!");
        assertNull(c1.asXmlDeclaration());

        Comment c2 = new Comment("?#data");
        // Should handle gracefully without throwing unhandled exceptions
        XmlDeclaration dec = c2.asXmlDeclaration();
        
        Comment c3 = new Comment("   ");
        assertNull(c3.asXmlDeclaration());
    }

    @Test
    public void testToString() {
        Comment comment = new Comment("hello");
        assertEquals("<!--hello-->", comment.toString());
    }

    @Test
    public void testClone() {
        Comment comment = new Comment("clone me");
        Comment clone = comment.clone();
        assertEquals(comment.getData(), clone.getData());
        assertNotSame(comment, clone);
    }
}