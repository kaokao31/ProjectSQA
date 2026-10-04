package org.jsoup.parser;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.XmlTreeBuilder;
import org.jsoup.parser.Parser;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class XmlTreeBuilderTest {

    @Test
    public void testDefaultSettings() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        assertEquals(ParseSettings.htmlDefault, builder.defaultSettings());
    }

    @Test
    public void testInitialiseParse() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        builder.initialiseParse("http://example.com/", new Parser(builder));
        assertNotNull(builder.doc);
        assertEquals(Document.OutputSettings.Syntax.xml, builder.doc.outputSettings().syntax());
        assertFalse(builder.doc.outputSettings().prettyPrint());
    }

    @Test
    public void testParseReader() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<root><child/></root>", "http://example.com/");
        assertNotNull(doc);
        assertEquals("root", doc.child(0).tagName());
        assertEquals("child", doc.child(0).child(0).tagName());
    }

    @Test
    public void testParseInputStream() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse(new java.io.ByteArrayInputStream("<root>data</root>".getBytes()), "UTF-8", "http://example.com/");
        assertNotNull(doc);
        assertEquals("root", doc.child(0).tagName());
        assertEquals("data", doc.child(0).text());
    }

    @Test
    public void testParseFragment() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        List<Node> nodes = builder.parseFragment("<child>text</child>", "root", "http://example.com/", new Parser(builder));
        assertNotNull(nodes);
        assertEquals(1, nodes.size());
        assertEquals("child", nodes.get(0).nodeName());
    }

    @Test
    public void testProcessStartTag() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        builder.initialiseParse("", new Parser(builder));
        
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("test");
        boolean result = builder.process(startTag);
        assertTrue(result);
        assertEquals("test", builder.currentElement().tagName());
    }

    @Test
    public void testProcessEndTag() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        builder.initialiseParse("", new Parser(builder));
        
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("test");
        builder.process(startTag);
        
        Token.EndTag endTag = new Token.EndTag();
        endTag.name("test");
        boolean result = builder.process(endTag);
        assertTrue(result);
        assertEquals("Document", builder.currentElement().nodeName());
    }

    @Test
    public void testProcessComment() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        builder.initialiseParse("", new Parser(builder));
        
        Token.Comment commentToken = new Token.Comment();
        commentToken.comment.append("this is a comment");
        boolean result = builder.process(commentToken);
        assertTrue(result);
        
        List<Node> children = builder.doc.childNodes();
        assertEquals(1, children.size());
        assertTrue(children.get(0) instanceof Comment);
        assertEquals("this is a comment", ((Comment) children.get(0)).getData());
    }

    @Test
    public void testProcessCharacter() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        builder.initialiseParse("", new Parser(builder));
        
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("p");
        builder.process(startTag);
        
        Token.Character charToken = new Token.Character();
        charToken.data("hello world");
        boolean result = builder.process(charToken);
        assertTrue(result);
        
        Element current = builder.currentElement();
        assertEquals(1, current.childNodeSize());
        assertTrue(current.childNode(0) instanceof TextNode);
        assertEquals("hello world", ((TextNode) current.childNode(0)).text());
    }

    @Test
    public void testProcessDoctype() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        builder.initialiseParse("", new Parser(builder));
        
        Token.Doctype doctypeToken = new Token.Doctype();
        doctypeToken.name.append("html");
        doctypeToken.publicIdentifier.append("pubId");
        doctypeToken.systemIdentifier.append("sysId");
        
        boolean result = builder.process(doctypeToken);
        assertTrue(result);
        
        List<Node> children = builder.doc.childNodes();
        assertEquals(1, children.size());
    }

    @Test
    public void testInsertNode() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        builder.initialiseParse("", new Parser(builder));
        
        Comment comment = new Comment("test comment");
        builder.insertNode(comment);
        
        assertEquals(1, builder.doc.childNodeSize());
        assertEquals(comment, builder.doc.childNode(0));
    }

    @Test
    public void testInsertComment() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        builder.initialiseParse("", new Parser(builder));
        
        Token.Comment token = new Token.Comment();
        token.comment.append("my comment");
        builder.insert(token);
        
        assertEquals(1, builder.doc.childNodeSize());
        assertTrue(builder.doc.childNode(0) instanceof Comment);
        assertEquals("my comment", ((Comment) builder.doc.childNode(0)).getData());
    }

    @Test
    public void testInsertCharacter() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        builder.initialiseParse("", new Parser(builder));
        
        Token.Character token = new Token.Character();
        token.data("some data");
        builder.insert(token);
        
        assertEquals(1, builder.doc.childNodeSize());
        assertTrue(builder.doc.childNode(0) instanceof TextNode);
        assertEquals("some data", ((TextNode) builder.doc.childNode(0)).text());
    }

    @Test
    public void testInsertDoctype() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        builder.initialiseParse("", new Parser(builder));
        
        Token.Doctype token = new Token.Doctype();
        token.name.append("root");
        token.pubSysKey = "PUBLIC";
        token.publicIdentifier.append("pub");
        token.systemIdentifier.append("sys");
        
        builder.insert(token);
        assertEquals(1, builder.doc.childNodeSize());
    }

    @Test
    public void testPopStackToClose() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        builder.initialiseParse("", new Parser(builder));
        
        Token.StartTag t1 = new Token.StartTag();
        t1.name("a");
        builder.process(t1);
        
        Token.StartTag t2 = new Token.StartTag();
        t2.name("b");
        builder.process(t2);
        
        assertEquals("b", builder.currentElement().tagName());
        
        Token.EndTag endA = new Token.EndTag();
        endA.name("a");
        builder.popStackToClose(endA);
        
        assertEquals("Document", builder.currentElement().nodeName());
    }

    @Test
    public void testParseXmlDeclaration() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        // Trigger XML processing instruction / declaration bug path if present in Jsoup 80
        Document doc = builder.parse("<?xml version=\"1.0\" encoding=\"UTF-8\"?><root/>", "http://example.com/");
        assertNotNull(doc);
    }
}