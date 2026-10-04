package org.jsoup.parser;

import org.junit.Test;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.TextNode;

import java.io.StringReader;
import java.util.List;

import static org.junit.Assert.*;

public class XmlTreeBuilderTest {

    @Test
    public void testDefaultSettings() {
        XmlTreeBuilder parser = new XmlTreeBuilder();
        assertEquals(ParseSettings.xmlDefault, parser.defaultSettings());
    }

    @Test
    public void testParseReader() {
        XmlTreeBuilder parser = new XmlTreeBuilder();
        String xml = "<root><child id=\"1\">Text</child></root>";
        Document doc = parser.parse(new StringReader(xml), "http://example.com");
        assertNotNull(doc);
        assertEquals("root", doc.child(0).nodeName());
        assertEquals("1", doc.selectFirst("child").attr("id"));
        assertEquals("Text", doc.selectFirst("child").text());
    }

    @Test
    public void testParseString() {
        XmlTreeBuilder parser = new XmlTreeBuilder();
        String xml = "<?xml version=\"1.0\"?><data>Content</data>";
        Document doc = parser.parse(xml, "http://example.com");
        assertNotNull(doc);
        assertEquals("data", doc.child(0).nodeName());
        assertEquals("Content", doc.text());
    }

    @Test
    public void testNewInstance() {
        XmlTreeBuilder parser = new XmlTreeBuilder();
        ParseTreeBuilder newBuilder = parser.newInstance();
        assertNotNull(newBuilder);
        assertTrue(newBuilder instanceof XmlTreeBuilder);
    }

    @Test
    public void testProcessStartTag() {
        XmlTreeBuilder parser = new XmlTreeBuilder();
        parser.initialiseParse(new StringReader("<root/>"), "http://example.com", new ParseErrorList(0), ParseSettings.xmlDefault);
        
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("root");
        boolean result = parser.process(startTag);
        assertTrue(result);
    }

    @Test
    public void testProcessEndTag() {
        XmlTreeBuilder parser = new XmlTreeBuilder();
        parser.initialiseParse(new StringReader("<root></root>"), "http://example.com", new ParseErrorList(0), ParseSettings.xmlDefault);
        
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("root");
        parser.process(startTag);

        Token.EndTag endTag = new Token.EndTag();
        endTag.name("root");
        boolean result = parser.process(endTag);
        assertTrue(result);
    }

    @Test
    public void testProcessComment() {
        XmlTreeBuilder parser = new XmlTreeBuilder();
        parser.initialiseParse(new StringReader("<!-- comment -->"), "http://example.com", new ParseErrorList(0), ParseSettings.xmlDefault);
        
        Token.Comment commentToken = new Token.Comment();
        commentToken.comment("This is a comment");
        boolean result = parser.process(commentToken);
        assertTrue(result);
        
        Comment comment = (Comment) parser.doc.childNode(0);
        assertEquals("This is a comment", comment.getData());
    }

    @Test
    public void testProcessCharacter() {
        XmlTreeBuilder parser = new XmlTreeBuilder();
        parser.initialiseParse(new StringReader("text"), "http://example.com", new ParseErrorList(0), ParseSettings.xmlDefault);
        
        Token.Character charToken = new Token.Character();
        charToken.data("Some text");
        boolean result = parser.process(charToken);
        assertTrue(result);
        
        TextNode textNode = (TextNode) parser.doc.childNode(0);
        assertEquals("Some text", textNode.text());
    }

    @Test
    public void testProcessDoctype() {
        XmlTreeBuilder parser = new XmlTreeBuilder();
        parser.initialiseParse(new StringReader("<!DOCTYPE html>"), "http://example.com", new ParseErrorList(0), ParseSettings.xmlDefault);
        
        Token.Doctype doctypeToken = new Token.Doctype();
        doctypeToken.name("html");
        boolean result = parser.process(doctypeToken);
        assertTrue(result);
    }

    @Test
    public void testInsertNode() {
        XmlTreeBuilder parser = new XmlTreeBuilder();
        parser.initialiseParse(new StringReader("<root/>"), "http://example.com", new ParseErrorList(0), ParseSettings.xmlDefault);
        
        Comment comment = new Comment("test");
        parser.insertNode(comment);
        assertEquals(1, parser.doc.childNodeSize());
        assertEquals(comment, parser.doc.childNode(0));
    }

    @Test
    public void testPopStackToClose() {
        XmlTreeBuilder parser = new XmlTreeBuilder();
        String xml = "<parent><child><grandchild></grandchild></child></parent>";
        Document doc = parser.parse(xml, "");
        assertNotNull(doc);
        
        Token.EndTag endTag = new Token.EndTag();
        endTag.name("parent");
        
        parser.initialiseParse(new StringReader(xml), "", new ParseErrorList(0), ParseSettings.xmlDefault);
        parser.popStackToClose(endTag);
    }

    @Test
    public void testParseFragment() {
        XmlTreeBuilder parser = new XmlTreeBuilder();
        List<Node> nodes = parser.parseFragment("<child>one</child><child>two</child>", "http://example.com", new ParseErrorList(0), ParseSettings.xmlDefault);
        assertNotNull(nodes);
        assertEquals(2, nodes.size());
        assertEquals("child", nodes.get(0).nodeName());
    }

    @Test
    public void testParseFragmentWithContext() {
        XmlTreeBuilder parser = new XmlTreeBuilder();
        Element context = new Element(Tag.valueOf("root", ParseSettings.xmlDefault), "");
        List<Node> nodes = parser.parseFragment("<child>content</child>", context, "http://example.com", new ParseErrorList(0), ParseSettings.xmlDefault);
        assertNotNull(nodes);
        assertEquals(1, nodes.size());
        assertEquals("child", nodes.get(0).nodeName());
    }

    @Test
    public void testInvalidTagNestingAndCaseSensitivity() {
        XmlTreeBuilder parser = new XmlTreeBuilder();
        // XmlTreeBuilder should preserve case sensitivity based on settings
        ParseSettings settings = new ParseSettings(true, true);
        parser.initialiseParse(new StringReader("<ROOT><MixedCase>Text</MixedCase></ROOT>"), "", new ParseErrorList(0), settings);
        
        Token.StartTag t1 = new Token.StartTag();
        t1.name("ROOT");
        parser.process(t1);

        Token.StartTag t2 = new Token.StartTag();
        t2.name("MixedCase");
        parser.process(t2);

        Token.EndTag e2 = new Token.EndTag();
        e2.name("MixedCase");
        parser.process(e2);

        Token.EndTag e1 = new Token.EndTag();
        e1.name("ROOT");
        parser.process(e1);

        assertEquals("ROOT", parser.doc.child(0).nodeName());
        assertEquals("MixedCase", parser.doc.child(0).child(0).nodeName());
    }
}