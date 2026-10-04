package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

public class DocumentTypeTest {

    @Test
    public void testConstructorAndGetters() {
        // Test normal initialization with name, publicId, and systemId
        DocumentType docType = new DocumentType("html", "-//W3C//DTD HTML 4.01//EN", "http://www.w3.org/TR/html4/strict.dtd", "");
        
        assertEquals("html", docType.nodeName());
        assertEquals("html", docType.attr("name"));
        assertEquals("-//W3C//DTD HTML 4.01//EN", docType.attr("publicId"));
        assertEquals("http://www.w3.org/TR/html4/strict.dtd", docType.attr("systemId"));
    }

    @Test
    public void testConstructorWithBaseUri() {
        // Test constructor that accepts baseUri (inherited from Node)
        DocumentType docType = new DocumentType("html", "", "", "http://example.com");
        assertEquals("http://example.com", docType.baseUri());
    }

    @Test
    public void testOuterHtmlGenerationFull() {
        // Test when all fields (name, publicId, systemId) are present
        DocumentType docType = new DocumentType("html", "-//W3C//DTD HTML 4.01//EN", "http://www.w3.org/TR/html4/strict.dtd", "");
        
        StringBuilder accum = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        
        docType.outerHtmlHead(accum, 0, out);
        
        assertEquals("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">", accum.toString());
    }

    @Test
    public void testOuterHtmlGenerationNameOnly() {
        // Test when publicId and systemId are empty/missing
        DocumentType docType = new DocumentType("html", "", "", "");
        
        StringBuilder accum = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        
        docType.outerHtmlHead(accum, 0, out);
        
        assertEquals("<!DOCTYPE html>", accum.toString());
    }

    @Test
    public void testOuterHtmlGenerationPublicIdOnly() {
        // Test when only publicId is present (no systemId)
        DocumentType docType = new DocumentType("html", "-//W3C//DTD HTML 4.01//EN", "", "");
        
        StringBuilder accum = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        
        docType.outerHtmlHead(accum, 0, out);
        
        assertEquals("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\">", accum.toString());
    }

    @Test
    public void testOuterHtmlGenerationSystemIdOnly() {
        // Test when only systemId is present (no publicId) - specifically relevant to Jsoup-56 fix
        DocumentType docType = new DocumentType("html", "", "http://www.w3.org/TR/html4/strict.dtd", "");
        
        StringBuilder accum = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        
        docType.outerHtmlHead(accum, 0, out);
        
        assertEquals("<!DOCTYPE html SYSTEM \"http://www.w3.org/TR/html4/strict.dtd\">", accum.toString());
    }

    @Test
    public void testOuterHtmlTailDoesNothing() {
        DocumentType docType = new DocumentType("html", "", "", "");
        StringBuilder accum = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        
        docType.outerHtmlTail(accum, 0, out);
        assertEquals("", accum.toString());
    }
}