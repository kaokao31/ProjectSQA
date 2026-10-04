package org.jsoup.parser;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.junit.Assert.*;

public class TreeBuilderTest {

    // Concrete implementation of abstract TreeBuilder for testing purposes
    private static class DummyTreeBuilder extends TreeBuilder {
        @Override
        ParseSettings defaultSettings() {
            return ParseSettings.htmlDefault;
        }

        @Override
        protected void initialiseParse(Reader input, String baseUri, ParseErrorList errors, ParseSettings settings) {
            super.initialiseParse(input, baseUri, errors, settings);
        }

        @Override
        boolean process(Token token) {
            return true;
        }

        @Override
        public List<Node> parseFragment(String fragmentFragment, Element context, String baseUri, ParseErrorList errors, ParseSettings settings) {
            return null;
        }
    }

    @Test
    public void testInitialiseParseAndParserReference() {
        DummyTreeBuilder tb = new DummyTreeBuilder();
        Parser parser = new Parser(tb);
        StringReader reader = new StringReader("<html></html>");
        ParseErrorList errors = ParseErrorList.noTracking();
        
        tb.initialiseParse(reader, "http://example.com", errors, tb.defaultSettings());
        
        assertSame(parser, tb.parser);
        assertSame(reader, tb.reader);
        assertSame(tb.settings, tb.defaultSettings());
        assertEquals("http://example.com", tb.baseUri);
        assertNotNull(tb.doc);
        assertNotNull(tb.stack);
    }

    @Test
    public void testRunParser() {
        DummyTreeBuilder tb = new DummyTreeBuilder();
        new Parser(tb);
        StringReader reader = new StringReader("test");
        ParseErrorList errors = ParseErrorList.noTracking();
        tb.initialiseParse(reader, "", errors, tb.defaultSettings());
        
        Document doc = tb.runParser();
        assertNotNull(doc);
    }

    @Test
    public void testProcessTokenStartTag() {
        DummyTreeBuilder tb = new DummyTreeBuilder();
        new Parser(tb);
        StringReader reader = new StringReader("");
        tb.initialiseParse(reader, "", ParseErrorList.noTracking(), tb.defaultSettings());

        Token.StartTag startTag = new Token.StartTag();
        startTag.name("div");
        boolean result = tb.process(startTag);
        assertTrue(result);
    }

    @Test
    public void testProcessTokenStartTagWithAttributes() {
        DummyTreeBuilder tb = new DummyTreeBuilder();
        new Parser(tb);
        StringReader reader = new StringReader("");
        tb.initialiseParse(reader, "", ParseErrorList.noTracking(), tb.defaultSettings());

        Token.StartTag startTag = new Token.StartTag();
        startTag.name("a");
        startTag.attributes = new Attributes();
        startTag.attributes.put("href", "http://example.com");
        
        boolean result = tb.process(startTag);
        assertTrue(result);
    }

    @Test
    public void testProcessTokenEndTag() {
        DummyTreeBuilder tb = new DummyTreeBuilder();
        new Parser(tb);
        StringReader reader = new StringReader("");
        tb.initialiseParse(reader, "", ParseErrorList.noTracking(), tb.defaultSettings());

        Token.EndTag endTag = new Token.EndTag();
        endTag.name("div");
        boolean result = tb.process(endTag);
        assertTrue(result);
    }

    @Test
    public void testProcessTokenComment() {
        DummyTreeBuilder tb = new DummyTreeBuilder();
        new Parser(tb);
        StringReader reader = new StringReader("");
        tb.initialiseParse(reader, "", ParseErrorList.noTracking(), tb.defaultSettings());

        Token.Comment comment = new Token.Comment();
        comment.comment = "test comment";
        boolean result = tb.process(comment);
        assertTrue(result);
    }

    @Test
    public void testProcessTokenCharacter() {
        DummyTreeBuilder tb = new DummyTreeBuilder();
        new Parser(tb);
        StringReader reader = new StringReader("");
        tb.initialiseParse(reader, "", ParseErrorList.noTracking(), tb.defaultSettings());

        Token.Character character = new Token.Character();
        character.data("some text");
        boolean result = tb.process(character);
        assertTrue(result);
    }

    @Test
    public void testProcessTokenEOF() {
        DummyTreeBuilder tb = new DummyTreeBuilder();
        new Parser(tb);
        StringReader reader = new StringReader("");
        tb.initialiseParse(reader, "", ParseErrorList.noTracking(), tb.defaultSettings());

        Token.EOF eof = new Token.EOF();
        boolean result = tb.process(eof);
        assertTrue(result);
    }

    @Test
    public void testCurrentElementOnEmptyStack() {
        DummyTreeBuilder tb = new DummyTreeBuilder();
        new Parser(tb);
        StringReader reader = new StringReader("");
        tb.initialiseParse(reader, "", ParseErrorList.noTracking(), tb.defaultSettings());
        
        // Clear stack to test edge case
        tb.stack.clear();
        assertNull(tb.currentElement());
    }

    @Test
    public void testCurrentElementWithElements() {
        DummyTreeBuilder tb = new DummyTreeBuilder();
        new Parser(tb);
        StringReader reader = new StringReader("");
        tb.initialiseParse(reader, "", ParseErrorList.noTracking(), tb.defaultSettings());

        Element root = new Element(Tag.valueOf("root"), "");
        Element child = new Element(Tag.valueOf("child"), "");
        tb.stack.add(root);
        tb.stack.add(child);

        assertSame(child, tb.currentElement());
    }
}