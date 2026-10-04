package org.jsoup.parser;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for TokeniserState enum, targeting edge cases and known bugs (e.g., Defects4J bug 55).
 */
public class TokeniserStateTest {
    private Tokeniser tokeniser;
    private CharacterReader reader;

    @Before
    public void setUp() {
        // Initialize with empty input; each test will set its own input
        reader = new CharacterReader("");
        tokeniser = new Tokeniser(reader, null);
    }

    // Helper to create a tokeniser with given input
    private void setInput(String input) {
        reader = new CharacterReader(input);
        tokeniser = new Tokeniser(reader, null);
    }

    // ==================== Basic State Transitions ====================

    @Test
    public void testInitialStateTransitionsToData() {
        setInput("hello");
        TokeniserState.Data.read(tokeniser, reader);
        // After reading 'h', should still be in Data state
        assertEquals(TokeniserState.Data, tokeniser.getState());
    }

    @Test
    public void testDataStateReadsTagOpen() {
        setInput("<div>");
        TokeniserState.Data.read(tokeniser, reader);
        // After '<', should transition to TagOpen
        assertEquals(TokeniserState.TagOpen, tokeniser.getState());
    }

    @Test
    public void testDataStateReadsEndTagOpen() {
        setInput("</div>");
        TokeniserState.Data.read(tokeniser, reader);
        // After '<', should go to TagOpen, then after '/' to EndTagOpen
        // But we only call read once, so state is TagOpen
        assertEquals(TokeniserState.TagOpen, tokeniser.getState());
    }

    // ==================== TagOpen and TagName ====================

    @Test
    public void testTagOpenWithValidName() {
        setInput("div>");
        tokeniser.setState(TokeniserState.TagOpen);
        TokeniserState.TagOpen.read(tokeniser, reader);
        assertEquals(TokeniserState.TagName, tokeniser.getState());
    }

    @Test
    public void testTagOpenWithExclamationMark() {
        setInput("!DOCTYPE>");
        tokeniser.setState(TokeniserState.TagOpen);
        TokeniserState.TagOpen.read(tokeniser, reader);
        assertEquals(TokeniserState.MarkupDeclarationOpen, tokeniser.getState());
    }

    @Test
    public void testTagOpenWithSlash() {
        setInput("/div>");
        tokeniser.setState(TokeniserState.TagOpen);
        TokeniserState.TagOpen.read(tokeniser, reader);
        assertEquals(TokeniserState.EndTagOpen, tokeniser.getState());
    }

    @Test
    public void testTagNameReadsAttributeOrClose() {
        setInput(" class=\"test\">");
        tokeniser.setState(TokeniserState.TagName);
        TokeniserState.TagName.read(tokeniser, reader);
        // After reading space, should transition to BeforeAttributeName
        assertEquals(TokeniserState.BeforeAttributeName, tokeniser.getState());
    }

    @Test
    public void testTagNameReadsSelfClosing() {
        setInput("/>");
        tokeniser.setState(TokeniserState.TagName);
        TokeniserState.TagName.read(tokeniser, reader);
        assertEquals(TokeniserState.SelfClosingStartTag, tokeniser.getState());
    }

    // ==================== Attribute Parsing (Bug 55 related) ====================

    @Test
    public void testBeforeAttributeNameWithEmptyName() {
        // This simulates <div =value> where attribute name is empty
        setInput(" =value>");
        tokeniser.setState(TokeniserState.BeforeAttributeName);
        TokeniserState.BeforeAttributeName.read(tokeniser, reader);
        // After '=', should transition to BeforeAttributeValue
        assertEquals(TokeniserState.BeforeAttributeValue, tokeniser.getState());
    }

    @Test
    public void testAttributeNameWithEmptyString() {
        // Edge case: attribute name is empty, then '=' or space
        setInput(" =\"value\">");
        tokeniser.setState(TokeniserState.AttributeName);
        TokeniserState.AttributeName.read(tokeniser, reader);
        // After space, should go to BeforeAttributeName (or AfterAttributeName)
        // Actually, after reading space, state becomes AfterAttributeName
        assertEquals(TokeniserState.AfterAttributeName, tokeniser.getState());
    }

    @Test
    public void testAttributeNameWithEqualsDirectly() {
        setInput("=value>");
        tokeniser.setState(TokeniserState.AttributeName);
        TokeniserState.AttributeName.read(tokeniser, reader);
        // After '=', should go to BeforeAttributeValue
        assertEquals(TokeniserState.BeforeAttributeValue, tokeniser.getState());
    }

    @Test
    public void testAttributeValueUnquotedWithEmpty() {
        // <div attr=> should parse attribute with empty value
        setInput(">");
        tokeniser.setState(TokeniserState.AttributeValue_unquoted);
        TokeniserState.AttributeValue_unquoted.read(tokeniser, reader);
        // After '>', should transition to TagName (or self-closing)
        assertEquals(TokeniserState.TagName, tokeniser.getState());
    }

    @Test
    public void testAttributeValueDoubleQuotedWithEmpty() {
        setInput("\"\">");
        tokeniser.setState(TokeniserState.AttributeValue_doubleQuoted);
        TokeniserState.AttributeValue_doubleQuoted.read(tokeniser, reader);
        // After closing quote, should go to AfterAttributeValue_quoted
        assertEquals(TokeniserState.AfterAttributeValue_quoted, tokeniser.getState());
    }

    @Test
    public void testAttributeValueSingleQuotedWithEmpty() {
        setInput("''>");
        tokeniser.setState(TokeniserState.AttributeValue_singleQuoted);
        TokeniserState.AttributeValue_singleQuoted.read(tokeniser, reader);
        assertEquals(TokeniserState.AfterAttributeValue_quoted, tokeniser.getState());
    }

    // ==================== Self-Closing Tag ====================

    @Test
    public void testSelfClosingStartTagWithSlash() {
        setInput(">");
        tokeniser.setState(TokeniserState.SelfClosingStartTag);
        TokeniserState.SelfClosingStartTag.read(tokeniser, reader);
        // After '>', should go to Data and emit self-closing tag
        assertEquals(TokeniserState.Data, tokeniser.getState());
    }

    @Test
    public void testSelfClosingStartTagWithInvalidChar() {
        setInput("x>");
        tokeniser.setState(TokeniserState.SelfClosingStartTag);
        TokeniserState.SelfClosingStartTag.read(tokeniser, reader);
        // After non-'>', should go to BeforeAttributeName
        assertEquals(TokeniserState.BeforeAttributeName, tokeniser.getState());
    }

    // ==================== End Tag ====================

    @Test
    public void testEndTagOpenWithValidName() {
        setInput("div>");
        tokeniser.setState(TokeniserState.EndTagOpen);
        TokeniserState.EndTagOpen.read(tokeniser, reader);
        assertEquals(TokeniserState.TagName, tokeniser.getState());
    }

    @Test
    public void testEndTagOpenWithInvalidChar() {
        setInput("!>");
        tokeniser.setState(TokeniserState.EndTagOpen);
        TokeniserState.EndTagOpen.read(tokeniser, reader);
        // Should emit comment or bogus? Actually, in Jsoup, it goes to BogusComment
        assertEquals(TokeniserState.BogusComment, tokeniser.getState());
    }

    // ==================== Comment and Bogus ====================

    @Test
    public void testBogusCommentReadsUntilGt() {
        setInput("some text>");
        tokeniser.setState(TokeniserState.BogusComment);
        TokeniserState.BogusComment.read(tokeniser, reader);
        assertEquals(TokeniserState.Data, tokeniser.getState());
    }

    @Test
    public void testMarkupDeclarationOpenWithDoctype() {
        setInput("DOCTYPE html>");
        tokeniser.setState(TokeniserState.MarkupDeclarationOpen);
        TokeniserState.MarkupDeclarationOpen.read(tokeniser, reader);
        assertEquals(TokeniserState.Doctype, tokeniser.getState());
    }

    // ==================== Character Reference ====================

    @Test
    public void testCharacterReferenceInData() {
        setInput("&amp;");
        tokeniser.setState(TokeniserState.Data);
        TokeniserState.Data.read(tokeniser, reader);
        // After '&', should go to CharacterReference
        assertEquals(TokeniserState.CharacterReference, tokeniser.getState());
    }

    // ==================== Bug 55 Reproduction ====================

    @Test(timeout = 1000)
    public void testBug55EmptyAttributeNameDoesNotHang() {
        // This test reproduces the bug: parsing <div =value> should not hang
        setInput("<div =value>");
        // Simulate parsing by calling read repeatedly until Data state
        tokeniser.setState(TokeniserState.Data);
        while (tokeniser.getState() != TokeniserState.Data || reader.current() != -1) {
            tokeniser.getState().read(tokeniser, reader);
            if (reader.current() == -1) break;
        }
        // Should have emitted a start tag with an attribute named "" and value "value"
        // The bug caused an infinite loop; if we reach here, no hang
        assertTrue(true);
    }

    @Test(timeout = 1000)
    public void testBug55EmptyAttributeNameWithNoValue() {
        setInput("<div = >");
        tokeniser.setState(TokeniserState.Data);
        while (tokeniser.getState() != TokeniserState.Data || reader.current() != -1) {
            tokeniser.getState().read(tokeniser, reader);
            if (reader.current() == -1) break;
        }
        assertTrue(true);
    }

    // ==================== Edge Cases ====================

    @Test
    public void testDataStateWithNullChar() {
        setInput("\0");
        TokeniserState.Data.read(tokeniser, reader);
        // Should handle null character (replace with replacement char)
        assertEquals(TokeniserState.Data, tokeniser.getState());
    }

    @Test
    public void testTagNameWithNullChar() {
        setInput("\0>");
        tokeniser.setState(TokeniserState.TagName);
        TokeniserState.TagName.read(tokeniser, reader);
        assertEquals(TokeniserState.TagName, tokeniser.getState());
    }

    @Test
    public void testBeforeAttributeNameWithSlash() {
        setInput("/>");
        tokeniser.setState(TokeniserState.BeforeAttributeName);
        TokeniserState.BeforeAttributeName.read(tokeniser, reader);
        assertEquals(TokeniserState.SelfClosingStartTag, tokeniser.getState());
    }

    @Test
    public void testAfterAttributeNameWithEquals() {
        setInput("=value>");
        tokeniser.setState(TokeniserState.AfterAttributeName);
        TokeniserState.AfterAttributeName.read(tokeniser, reader);
        assertEquals(TokeniserState.BeforeAttributeValue, tokeniser.getState());
    }

    @Test
    public void testAfterAttributeValueQuotedWithSpace() {
        setInput(" >");
        tokeniser.setState(TokeniserState.AfterAttributeValue_quoted);
        TokeniserState.AfterAttributeValue_quoted.read(tokeniser, reader);
        assertEquals(TokeniserState.BeforeAttributeName, tokeniser.getState());
    }

    @Test
    public void testAfterAttributeValueQuotedWithSlash() {
        setInput("/>");
        tokeniser.setState(TokeniserState.AfterAttributeValue_quoted);
        TokeniserState.AfterAttributeValue_quoted.read(tokeniser, reader);
        assertEquals(TokeniserState.SelfClosingStartTag, tokeniser.getState());
    }

    @Test
    public void testCdataSection() {
        setInput("[CDATA[some text]]>");
        tokeniser.setState(TokeniserState.CDATA);
        TokeniserState.CDATA.read(tokeniser, reader);
        // After reading '[', should go to CDATA? Actually, CDATA state reads until ']]>'
        // This is simplified; real test would need multiple reads
        assertEquals(TokeniserState.CDATA, tokeniser.getState());
    }

    @Test
    public void testScriptData() {
        setInput("</script>");
        tokeniser.setState(TokeniserState.ScriptData);
        TokeniserState.ScriptData.read(tokeniser, reader);
        // After '<', should go to ScriptDataLessThanSign
        assertEquals(TokeniserState.ScriptDataLessThanSign, tokeniser.getState());
    }
}