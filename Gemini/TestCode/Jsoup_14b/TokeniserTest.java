package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class TokeniserTest {

    @Test
    public void testTokeniserInitializationAndBasicState() {
        CharacterReader reader = new CharacterReader("<span>Hello</span>");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        assertNotNull(tokeniser);
        assertEquals(TokeniserState.Data, tokeniser.getState());
    }

    @Test
    public void testEmitTagPending() {
        CharacterReader reader = new CharacterReader("<div>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        
        tokeniser.tagPending = new Token.StartTag("div");
        tokeniser.emitTagPending();
        
        Token t = tokeniser.read();
        assertTrue(t instanceof Token.StartTag);
        assertEquals("div", ((Token.StartTag) t).name());
    }

    @Test
    public void testEmitString() {
        CharacterReader reader = new CharacterReader("test");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        tokeniser.emit("hello world");
        Token t = tokeniser.read();

        assertTrue(t instanceof Token.Char);
        assertEquals("hello world", t.toString());
    }

    @Test
    public void testEmitCharacter() {
        CharacterReader reader = new CharacterReader("test");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        tokeniser.emit('a');
        Token t = tokeniser.read();

        assertTrue(t instanceof Token.Char);
        assertEquals("a", t.toString());
    }

    @Test
    public void testEmitEOF() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        tokeniser.emit(new Token.EOF());
        Token t = tokeniser.read();

        assertTrue(t instanceof Token.EOF);
    }

    @Test
    public void testStateTransitions() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        tokeniser.transition(TokeniserState.TagOpen);
        assertEquals(TokeniserState.TagOpen, tokeniser.getState());
    }

    @Test
    public void testCreateCommentPending() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        tokeniser.createCommentPending();
        assertNotNull(tokeniser.commentPending);
    }

    @Test
    public void testCreateTagPending() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        tokeniser.createTagPending(true);
        assertTrue(tokeniser.tagPending instanceof Token.StartTag);

        tokeniser.createTagPending(false);
        assertTrue(tokeniser.tagPending instanceof Token.EndTag);
    }

    @Test
    public void testCreateDoctypePending() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        tokeniser.createDoctypePending();
        assertNotNull(tokeniser.doctypePending);
    }

    @Test
    public void testEmitCommentPending() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        tokeniser.createCommentPending();
        tokeniser.commentPending.append("test comment");
        tokeniser.emitCommentPending();

        Token t = tokeniser.read();
        assertTrue(t instanceof Token.Comment);
        assertEquals("test comment", ((Token.Comment) t).getData());
    }

    @Test
    public void testEmitDoctypePending() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        tokeniser.createDoctypePending();
        tokeniser.doctypePending.name.append("html");
        tokeniser.emitDoctypePending();

        Token t = tokeniser.read();
        assertTrue(t instanceof Token.Doctype);
        assertEquals("html", ((Token.Doctype) t).getName());
    }

    @Test
    public void testCreateTempBuffer() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        tokeniser.createTempBuffer();
        assertNotNull(tokeniser.dataBuffer);
    }

    @Test
    public void testIsAppropriateEndTagToken() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        tokeniser.lastStartTag = new Token.StartTag("div");
        tokeniser.tagPending = new Token.EndTag("div");

        assertTrue(tokeniser.isAppropriateEndTagToken());

        tokeniser.tagPending = new Token.EndTag("span");
        assertFalse(tokeniser.isAppropriateEndTagToken());
        
        tokeniser.lastStartTag = null;
        assertFalse(tokeniser.isAppropriateEndTagToken());
    }

    @Test
    public void testConsumeTagName() {
        CharacterReader reader = new CharacterReader("div></div");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        tokeniser.createTagPending(true);

        tokeniser.tagPending.appendTagName('a');
        assertEquals("a", tokeniser.tagPending.name);
    }

    @Test
    public void testBogusCommentEmission() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        tokeniser.createCommentPending();
        tokeniser.commentPending.append("bogus");
        tokeniser.emitCommentPending();

        Token t = tokeniser.read();
        assertTrue(t instanceof Token.Comment);
    }

    @Test
    public void testParseErrorTracking() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        CharacterReader reader = new CharacterReader("<?xml>");
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        tokeniser.error(TokeniserState.Data);
        assertFalse(errors.isEmpty());
    }

    @Test
    public void testEofError() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        tokeniser.eofError(TokeniserState.Data);
        assertFalse(errors.isEmpty());
    }

    @Test
    public void testConsumeAttribute() {
        CharacterReader reader = new CharacterReader("id=\"test\">");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        tokeniser.createTagPending(true);
        tokeniser.tagPending.newAttribute();
        
        tokeniser.tagPending.appendAttributeName('i');
        tokeniser.tagPending.appendAttributeName('d');
        tokeniser.tagPending.appendAttributeValue('t');
        
        assertEquals("id", tokeniser.tagPending.attributes.get(0).getKey());
        assertEquals("t", tokeniser.tagPending.attributes.get(0).getValue());
    }
}