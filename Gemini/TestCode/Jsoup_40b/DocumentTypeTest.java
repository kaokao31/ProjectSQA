package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

public class DocumentTypeTest {

    @Test
    public void testConstructorAndGetters() {
        // Test normal initialization with name, publicId, and systemId
        DocumentType docType = new DocumentType("html", "-//W3C//DTD HTML 4.01 Transitional//EN", "http://www.w3.org/TR/html4/loose.dtd", "");
        
        assertEquals("html", docType.name());
        assertEquals("-//W3C//DTD HTML 4.01 Transitional//EN", docType.attr("publicId"));
        assertEquals("http://www.w3.org/TR/html4/loose.dtd", docType.attr("systemId"));
        assertEquals("#doctype", docType.nodeName());
    }

    @Test
    public void testHtml5Doctype() {
        // Test HTML5 doctype (typically just name="html", publicId="", systemId="")
        DocumentType docType = new DocumentType("html", "", "", "");
        
        assertEquals("html", docType.name());
        // Depending on implementation, empty attributes might return empty string or not be set
        // Let's verify outerHtml generation for HTML5
        StringBuilder accum = new StringBuilder();
        docType.outerHtml(accum, 0, new Document.OutputSettings());
        
        assertEquals("<!DOCTYPE html>", accum.toString().trim());
    }

    @Test
    public void testOuterHtmlWithPublicAndSystem() {
        DocumentType docType = new DocumentType("html", "-//IETF//DTD HTML 2.0//EN", "", "");
        StringBuilder accum = new StringBuilder();
        docType.outerHtml(accum, 0, new Document.OutputSettings());
        
        assertEquals("<!DOCTYPE html PUBLIC \"-//IETF//DTD HTML 2.0//EN\">", accum.toString().trim());
    }

    @Test
    public void testOuterHtmlWithSystemOnly() {
        DocumentType docType = new DocumentType("html", "", "http://www.ibm.com/data/dtd/v11/ibmxhtml1-transitional.dtd", "");
        StringBuilder accum = new StringBuilder();
        docType.outerHtml(accum, 0, new Document.OutputSettings());
        
        assertEquals("<!DOCTYPE html SYSTEM \"http://www.ibm.com/data/dtd/v11/ibmxhtml1-transitional.dtd\">", accum.toString().trim());
    }

    @Test
    public void testOuterHtmlWithBothPublicAndSystem() {
        DocumentType docType = new DocumentType("html", "-//W3C//DTD XHTML 1.0 Transitional//EN", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd", "");
        StringBuilder accum = new StringBuilder();
        docType.outerHtml(accum, 0, new Document.OutputSettings());
        
        assertEquals("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">", accum.toString().trim());
    }

    @Test
    public void testOuterHtmlUpperLowerCasesAndBlankHandling() {
        // Testing behavior when attributes are present or blank (addressing common Jsoup bugs around missing pub/sys IDs)
        DocumentType docType = new DocumentType("html", "   ", "   ", "");
        StringBuilder accum = new StringBuilder();
        docType.outerHtml(accum, 0, new Document.OutputSettings());
        
        // Should fallback or format cleanly as just <!DOCTYPE html> if pub and sys are blank/empty
        assertEquals("<!DOCTYPE html>", accum.toString().trim());
    }

    @Test
    public void testNodeName() {
        DocumentType docType = new DocumentType("anything", "pub", "sys", "base");
        assertEquals("#doctype", docType.nodeName());
    }

    @Test
    public void testTailMethodDoesNothing() {
        DocumentType docType = new DocumentType("html", "", "", "");
        StringBuilder accum = new StringBuilder();
        // tail() shouldn't append anything
        docType.tail(accum, 0, new Document.OutputSettings());
        assertEquals("", accum.toString());
    }
}