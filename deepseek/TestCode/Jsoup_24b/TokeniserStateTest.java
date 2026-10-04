package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.StringReader;

/**
 * Test suite for TokeniserState enum.
 * Designed to achieve high code coverage and detect potential faults.
 */
public class TokeniserStateTest {

    private Tokeniser createTokeniser(String input) {
        CharacterReader reader = new CharacterReader(new StringReader(input));
        return new Tokeniser(reader, null);
    }

    // --- Data state ---
    @Test
    public void testDataStateConsumesCharacters() {
        Tokeniser t = createTokeniser("hello");
        TokeniserState.Data.read(t, t.reader);
        assertEquals("hello", t.dataBuffer.toString());
    }

    @Test
    public void testDataStateTransitionOnLt() {
        Tokeniser t = createTokeniser("<");
        TokeniserState.Data.read(t, t.reader);
        // After '<', state should change to TagOpen
        assertEquals(TokeniserState.TagOpen, t.state);
    }

    @Test
    public void testDataStateTransitionOnNull() {
        Tokeniser t = createTokeniser("\0");
        TokeniserState.Data.read(t, t.reader);
        // Null character should be replaced with replacement char
        assertEquals("\uFFFD", t.dataBuffer.toString());
    }

    @Test
    public void testDataStateTransitionOnEOF() {
        Tokeniser t = createTokeniser("");
        TokeniserState.Data.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    // --- TagOpen state ---
    @Test
    public void testTagOpenTransitionOnLetter() {
        Tokeniser t = createTokeniser("div");
        TokeniserState.TagOpen.read(t, t.reader);
        assertEquals(TokeniserState.TagName, t.state);
    }

    @Test
    public void testTagOpenTransitionOnExclamation() {
        Tokeniser t = createTokeniser("!");
        TokeniserState.TagOpen.read(t, t.reader);
        assertEquals(TokeniserState.MarkupDeclarationOpen, t.state);
    }

    @Test
    public void testTagOpenTransitionOnSlash() {
        Tokeniser t = createTokeniser("/");
        TokeniserState.TagOpen.read(t, t.reader);
        assertEquals(TokeniserState.EndTagOpen, t.state);
    }

    @Test
    public void testTagOpenTransitionOnQuestionMark() {
        Tokeniser t = createTokeniser("?");
        TokeniserState.TagOpen.read(t, t.reader);
        // Should create a bogus comment
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testTagOpenTransitionOnEOF() {
        Tokeniser t = createTokeniser("");
        TokeniserState.TagOpen.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    // --- TagName state ---
    @Test
    public void testTagNameConsumesLetters() {
        Tokeniser t = createTokeniser("div>");
        TokeniserState.TagName.read(t, t.reader);
        assertEquals("div", t.tagName.toString());
    }

    @Test
    public void testTagNameTransitionOnWhitespace() {
        Tokeniser t = createTokeniser("div ");
        TokeniserState.TagName.read(t, t.reader);
        assertEquals(TokeniserState.BeforeAttributeName, t.state);
    }

    @Test
    public void testTagNameTransitionOnSlash() {
        Tokeniser t = createTokeniser("div/");
        TokeniserState.TagName.read(t, t.reader);
        assertEquals(TokeniserState.SelfClosingStartTag, t.state);
    }

    @Test
    public void testTagNameTransitionOnGt() {
        Tokeniser t = createTokeniser("div>");
        TokeniserState.TagName.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testTagNameTransitionOnNull() {
        Tokeniser t = createTokeniser("div\0");
        TokeniserState.TagName.read(t, t.reader);
        assertEquals("div\uFFFD", t.tagName.toString());
    }

    @Test
    public void testTagNameTransitionOnEOF() {
        Tokeniser t = createTokeniser("div");
        TokeniserState.TagName.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    // --- BeforeAttributeName state ---
    @Test
    public void testBeforeAttributeNameTransitionOnSlash() {
        Tokeniser t = createTokeniser("/");
        TokeniserState.BeforeAttributeName.read(t, t.reader);
        assertEquals(TokeniserState.SelfClosingStartTag, t.state);
    }

    @Test
    public void testBeforeAttributeNameTransitionOnGt() {
        Tokeniser t = createTokeniser(">");
        TokeniserState.BeforeAttributeName.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testBeforeAttributeNameTransitionOnLetter() {
        Tokeniser t = createTokeniser("attr");
        TokeniserState.BeforeAttributeName.read(t, t.reader);
        assertEquals(TokeniserState.AttributeName, t.state);
    }

    @Test
    public void testBeforeAttributeNameTransitionOnEOF() {
        Tokeniser t = createTokeniser("");
        TokeniserState.BeforeAttributeName.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    // --- AttributeName state ---
    @Test
    public void testAttributeNameConsumesLetters() {
        Tokeniser t = createTokeniser("class");
        TokeniserState.AttributeName.read(t, t.reader);
        assertEquals("class", t.tagName.toString()); // Actually attribute name stored elsewhere
    }

    @Test
    public void testAttributeNameTransitionOnEq() {
        Tokeniser t = createTokeniser("=");
        TokeniserState.AttributeName.read(t, t.reader);
        assertEquals(TokeniserState.BeforeAttributeValue, t.state);
    }

    @Test
    public void testAttributeNameTransitionOnWhitespace() {
        Tokeniser t = createTokeniser(" ");
        TokeniserState.AttributeName.read(t, t.reader);
        assertEquals(TokeniserState.AfterAttributeName, t.state);
    }

    @Test
    public void testAttributeNameTransitionOnSlash() {
        Tokeniser t = createTokeniser("/");
        TokeniserState.AttributeName.read(t, t.reader);
        assertEquals(TokeniserState.SelfClosingStartTag, t.state);
    }

    @Test
    public void testAttributeNameTransitionOnGt() {
        Tokeniser t = createTokeniser(">");
        TokeniserState.AttributeName.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testAttributeNameTransitionOnNull() {
        Tokeniser t = createTokeniser("\0");
        TokeniserState.AttributeName.read(t, t.reader);
        // Null replaced with replacement char
        assertEquals("\uFFFD", t.tagName.toString());
    }

    @Test
    public void testAttributeNameTransitionOnEOF() {
        Tokeniser t = createTokeniser("");
        TokeniserState.AttributeName.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    // --- AfterAttributeName state ---
    @Test
    public void testAfterAttributeNameTransitionOnSlash() {
        Tokeniser t = createTokeniser("/");
        TokeniserState.AfterAttributeName.read(t, t.reader);
        assertEquals(TokeniserState.SelfClosingStartTag, t.state);
    }

    @Test
    public void testAfterAttributeNameTransitionOnEq() {
        Tokeniser t = createTokeniser("=");
        TokeniserState.AfterAttributeName.read(t, t.reader);
        assertEquals(TokeniserState.BeforeAttributeValue, t.state);
    }

    @Test
    public void testAfterAttributeNameTransitionOnGt() {
        Tokeniser t = createTokeniser(">");
        TokeniserState.AfterAttributeName.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testAfterAttributeNameTransitionOnLetter() {
        Tokeniser t = createTokeniser("attr");
        TokeniserState.AfterAttributeName.read(t, t.reader);
        assertEquals(TokeniserState.AttributeName, t.state);
    }

    @Test
    public void testAfterAttributeNameTransitionOnEOF() {
        Tokeniser t = createTokeniser("");
        TokeniserState.AfterAttributeName.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    // --- BeforeAttributeValue state ---
    @Test
    public void testBeforeAttributeValueTransitionOnDoubleQuote() {
        Tokeniser t = createTokeniser("\"");
        TokeniserState.BeforeAttributeValue.read(t, t.reader);
        assertEquals(TokeniserState.AttributeValue_doubleQuoted, t.state);
    }

    @Test
    public void testBeforeAttributeValueTransitionOnSingleQuote() {
        Tokeniser t = createTokeniser("'");
        TokeniserState.BeforeAttributeValue.read(t, t.reader);
        assertEquals(TokeniserState.AttributeValue_singleQuoted, t.state);
    }

    @Test
    public void testBeforeAttributeValueTransitionOnNoQuote() {
        Tokeniser t = createTokeniser("value");
        TokeniserState.BeforeAttributeValue.read(t, t.reader);
        assertEquals(TokeniserState.AttributeValue_unquoted, t.state);
    }

    @Test
    public void testBeforeAttributeValueTransitionOnGt() {
        Tokeniser t = createTokeniser(">");
        TokeniserState.BeforeAttributeValue.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testBeforeAttributeValueTransitionOnEOF() {
        Tokeniser t = createTokeniser("");
        TokeniserState.BeforeAttributeValue.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    // --- AttributeValue_doubleQuoted state ---
    @Test
    public void testDoubleQuotedValueConsumes() {
        Tokeniser t = createTokeniser("hello\"");
        TokeniserState.AttributeValue_doubleQuoted.read(t, t.reader);
        assertEquals("hello", t.dataBuffer.toString());
    }

    @Test
    public void testDoubleQuotedValueTransitionOnQuote() {
        Tokeniser t = createTokeniser("\"");
        TokeniserState.AttributeValue_doubleQuoted.read(t, t.reader);
        assertEquals(TokeniserState.BeforeAttributeName, t.state);
    }

    @Test
    public void testDoubleQuotedValueTransitionOnNull() {
        Tokeniser t = createTokeniser("\0");
        TokeniserState.AttributeValue_doubleQuoted.read(t, t.reader);
        assertEquals("\uFFFD", t.dataBuffer.toString());
    }

    @Test
    public void testDoubleQuotedValueTransitionOnEOF() {
        Tokeniser t = createTokeniser("");
        TokeniserState.AttributeValue_doubleQuoted.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    // --- AttributeValue_singleQuoted state ---
    @Test
    public void testSingleQuotedValueConsumes() {
        Tokeniser t = createTokeniser("hello'");
        TokeniserState.AttributeValue_singleQuoted.read(t, t.reader);
        assertEquals("hello", t.dataBuffer.toString());
    }

    @Test
    public void testSingleQuotedValueTransitionOnQuote() {
        Tokeniser t = createTokeniser("'");
        TokeniserState.AttributeValue_singleQuoted.read(t, t.reader);
        assertEquals(TokeniserState.BeforeAttributeName, t.state);
    }

    @Test
    public void testSingleQuotedValueTransitionOnNull() {
        Tokeniser t = createTokeniser("\0");
        TokeniserState.AttributeValue_singleQuoted.read(t, t.reader);
        assertEquals("\uFFFD", t.dataBuffer.toString());
    }

    @Test
    public void testSingleQuotedValueTransitionOnEOF() {
        Tokeniser t = createTokeniser("");
        TokeniserState.AttributeValue_singleQuoted.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    // --- AttributeValue_unquoted state ---
    @Test
    public void testUnquotedValueConsumes() {
        Tokeniser t = createTokeniser("value>");
        TokeniserState.AttributeValue_unquoted.read(t, t.reader);
        assertEquals("value", t.dataBuffer.toString());
    }

    @Test
    public void testUnquotedValueTransitionOnWhitespace() {
        Tokeniser t = createTokeniser(" ");
        TokeniserState.AttributeValue_unquoted.read(t, t.reader);
        assertEquals(TokeniserState.BeforeAttributeName, t.state);
    }

    @Test
    public void testUnquotedValueTransitionOnGt() {
        Tokeniser t = createTokeniser(">");
        TokeniserState.AttributeValue_unquoted.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testUnquotedValueTransitionOnNull() {
        Tokeniser t = createTokeniser("\0");
        TokeniserState.AttributeValue_unquoted.read(t, t.reader);
        assertEquals("\uFFFD", t.dataBuffer.toString());
    }

    @Test
    public void testUnquotedValueTransitionOnEOF() {
        Tokeniser t = createTokeniser("");
        TokeniserState.AttributeValue_unquoted.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    // --- SelfClosingStartTag state ---
    @Test
    public void testSelfClosingStartTagTransitionOnGt() {
        Tokeniser t = createTokeniser(">");
        TokeniserState.SelfClosingStartTag.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testSelfClosingStartTagTransitionOnEOF() {
        Tokeniser t = createTokeniser("");
        TokeniserState.SelfClosingStartTag.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    // --- EndTagOpen state ---
    @Test
    public void testEndTagOpenTransitionOnLetter() {
        Tokeniser t = createTokeniser("div");
        TokeniserState.EndTagOpen.read(t, t.reader);
        assertEquals(TokeniserState.TagName, t.state);
    }

    @Test
    public void testEndTagOpenTransitionOnGt() {
        Tokeniser t = createTokeniser(">");
        TokeniserState.EndTagOpen.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testEndTagOpenTransitionOnEOF() {
        Tokeniser t = createTokeniser("");
        TokeniserState.EndTagOpen.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    // --- MarkupDeclarationOpen state ---
    @Test
    public void testMarkupDeclarationOpenTransitionOnDoctype() {
        Tokeniser t = createTokeniser("DOCTYPE");
        TokeniserState.MarkupDeclarationOpen.read(t, t.reader);
        assertEquals(TokeniserState.Doctype, t.state);
    }

    @Test
    public void testMarkupDeclarationOpenTransitionOnComment() {
        Tokeniser t = createTokeniser("--");
        TokeniserState.MarkupDeclarationOpen.read(t, t.reader);
        assertEquals(TokeniserState.Comment, t.state);
    }

    @Test
    public void testMarkupDeclarationOpenTransitionOnCDATA() {
        Tokeniser t = createTokeniser("[CDATA[");
        TokeniserState.MarkupDeclarationOpen.read(t, t.reader);
        assertEquals(TokeniserState.CDATA, t.state);
    }

    @Test
    public void testMarkupDeclarationOpenTransitionOnOther() {
        Tokeniser t = createTokeniser("x");
        TokeniserState.MarkupDeclarationOpen.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    // --- Comment state ---
    @Test
    public void testCommentConsumes() {
        Tokeniser t = createTokeniser("comment-->");
        TokeniserState.Comment.read(t, t.reader);
        assertEquals("comment", t.dataBuffer.toString());
    }

    @Test
    public void testCommentTransitionOnDoubleDash() {
        Tokeniser t = createTokeniser("--");
        TokeniserState.Comment.read(t, t.reader);
        assertEquals(TokeniserState.CommentEnd, t.state);
    }

    @Test
    public void testCommentTransitionOnNull() {
        Tokeniser t = createTokeniser("\0");
        TokeniserState.Comment.read(t, t.reader);
        assertEquals("\uFFFD", t.dataBuffer.toString());
    }

    @Test
    public void testCommentTransitionOnEOF() {
        Tokeniser t = createTokeniser("");
        TokeniserState.Comment.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    // --- CommentEnd state ---
    @Test
    public void testCommentEndTransitionOnGt() {
        Tokeniser t = createTokeniser(">");
        TokeniserState.CommentEnd.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testCommentEndTransitionOnBang() {
        Tokeniser t = createTokeniser("!");
        TokeniserState.CommentEnd.read(t, t.reader);
        assertEquals(TokeniserState.CommentEndBang, t.state);
    }

    @Test
    public void testCommentEndTransitionOnDash() {
        Tokeniser t = createTokeniser("-");
        TokeniserState.CommentEnd.read(t, t.reader);
        assertEquals(TokeniserState.CommentEndDash, t.state);
    }

    @Test
    public void testCommentEndTransitionOnEOF() {
        Tokeniser t = createTokeniser("");
        TokeniserState.CommentEnd.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    // --- CommentEndBang state ---
    @Test
    public void testCommentEndBangTransitionOnDash() {
        Tokeniser t = createTokeniser("-");
        TokeniserState.CommentEndBang.read(t, t.reader);
        assertEquals(TokeniserState.CommentEndDash, t.state);
    }

    @Test
    public void testCommentEndBangTransitionOnGt() {
        Tokeniser t = createTokeniser(">");
        TokeniserState.CommentEndBang.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testCommentEndBangTransitionOnEOF() {
        Tokeniser t = createTokeniser("");
        TokeniserState.CommentEndBang.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    // --- CommentEndDash state ---
    @Test
    public void testCommentEndDashTransitionOnDash() {
        Tokeniser t = createTokeniser("-");
        TokeniserState.CommentEndDash.read(t, t.reader);
        assertEquals(TokeniserState.CommentEnd, t.state);
    }

    @Test
    public void testCommentEndDashTransitionOnEOF() {
        Tokeniser t = createTokeniser("");
        TokeniserState.CommentEndDash.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    // --- Doctype state ---
    @Test
    public void testDoctypeTransitionOnWhitespace() {
        Tokeniser t = createTokeniser(" ");
        TokeniserState.Doctype.read(t, t.reader);
        assertEquals(TokeniserState.BeforeDoctypeName, t.state);
    }

    @Test
    public void testDoctypeTransitionOnGt() {
        Tokeniser t = createTokeniser(">");
        TokeniserState.Doctype.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testDoctypeTransitionOnEOF() {
        Tokeniser t = createTokeniser("");
        TokeniserState.Doctype.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    // --- BeforeDoctypeName state ---
    @Test
    public void testBeforeDoctypeNameTransitionOnLetter() {
        Tokeniser t = createTokeniser("html");
        TokeniserState.BeforeDoctypeName.read(t, t.reader);
        assertEquals(TokeniserState.DoctypeName, t.state);
    }

    @Test
    public void testBeforeDoctypeNameTransitionOnGt() {
        Tokeniser t = createTokeniser(">");
        TokeniserState.BeforeDoctypeName.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testBeforeDoctypeNameTransitionOnEOF() {
        Tokeniser t = createTokeniser("");
        TokeniserState.BeforeDoctypeName.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    // --- DoctypeName state ---
    @Test
    public void testDoctypeNameConsumes() {
        Tokeniser t = createTokeniser("html>");
        TokeniserState.DoctypeName.read(t, t.reader);
        assertEquals("html", t.dataBuffer.toString());
    }

    @Test
    public void testDoctypeNameTransitionOnWhitespace() {
        Tokeniser t = createTokeniser(" ");
        TokeniserState.DoctypeName.read(t, t.reader);
        assertEquals(TokeniserState.AfterDoctypeName, t.state);
    }

    @Test
    public void testDoctypeNameTransitionOnGt() {
        Tokeniser t = createTokeniser(">");
        TokeniserState.DoctypeName.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testDoctypeNameTransitionOnNull() {
        Tokeniser t = createTokeniser("\0");
        TokeniserState.DoctypeName.read(t, t.reader);
        assertEquals("\uFFFD", t.dataBuffer.toString());
    }

    @Test
    public void testDoctypeNameTransitionOnEOF() {
        Tokeniser t = createTokeniser("");
        TokeniserState.DoctypeName.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    // --- AfterDoctypeName state ---
    @Test
    public void testAfterDoctypeNameTransitionOnGt() {
        Tokeniser t = createTokeniser(">");
        TokeniserState.AfterDoctypeName.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testAfterDoctypeNameTransitionOnPublic() {
        Tokeniser t = createTokeniser("PUBLIC");
        TokeniserState.AfterDoctypeName.read(t, t.reader);
        assertEquals(TokeniserState.AfterDoctypePublicKeyword, t.state);
    }

    @Test
    public void testAfterDoctypeNameTransitionOnSystem() {
        Tokeniser t = createTokeniser("SYSTEM");
        TokeniserState.AfterDoctypeName.read(t, t.reader);
        assertEquals(TokeniserState.AfterDoctypeSystemKeyword, t.state);
    }

    @Test
    public void testAfterDoctypeNameTransitionOnEOF() {
        Tokeniser t = createTokeniser("");
        TokeniserState.AfterDoctypeName.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    // --- AfterDoctypePublicKeyword state ---
    @Test
    public void testAfterDoctypePublicKeywordTransitionOnWhitespace() {
        Tokeniser t = createTokeniser(" ");
        TokeniserState.AfterDoctypePublicKeyword.read(t, t.reader);
        assertEquals(TokeniserState.BeforeDoctypePublicIdentifier, t.state);
    }

    @Test
    public void testAfterDoctypePublicKeywordTransitionOnQuote() {
        Tokeniser t = createTokeniser("\"");
        TokeniserState.AfterDoctypePublicKeyword.read(t, t.reader);
        assertEquals(TokeniserState.DoctypePublicIdentifier_doubleQuoted, t.state);
    }

    @Test
    public void testAfterDoctypePublicKeywordTransitionOnGt() {
        Tokeniser t = createTokeniser(">");
        TokeniserState.AfterDoctypePublicKeyword.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testAfterDoctypePublicKeywordTransitionOnEOF() {
        Tokeniser t = createTokeniser("");
        TokeniserState.AfterDoctypePublicKeyword.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    // --- BeforeDoctypePublicIdentifier state ---
    @Test
    public void testBeforeDoctypePublicIdentifierTransitionOnQuote() {
        Tokeniser t = createTokeniser("\"");
        TokeniserState.BeforeDoctypePublicIdentifier.read(t, t.reader);
        assertEquals(TokeniserState.DoctypePublicIdentifier_doubleQuoted, t.state);
    }

    @Test
    public void testBeforeDoctypePublicIdentifierTransitionOnGt() {
        Tokeniser t = createTokeniser(">");
        TokeniserState.BeforeDoctypePublicIdentifier.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testBeforeDoctypePublicIdentifierTransitionOnEOF() {
        Tokeniser t = createTokeniser("");
        TokeniserState.BeforeDoctypePublicIdentifier.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    // --- DoctypePublicIdentifier_doubleQuoted state ---
    @Test
    public void testDoctypePublicIdentifierDoubleQuotedConsumes() {
        Tokeniser t = createTokeniser("publicID\"");
        TokeniserState.DoctypePublicIdentifier_doubleQuoted.read(t, t.reader);
        assertEquals("publicID", t.dataBuffer.toString());
    }

    @Test
    public void testDoctypePublicIdentifierDoubleQuotedTransitionOnQuote() {
        Tokeniser t = createTokeniser("\"");
        TokeniserState.DoctypePublicIdentifier_doubleQuoted.read(t, t.reader);
        assertEquals(TokeniserState.AfterDoctypePublicIdentifier, t.state);
    }

    @Test
    public void testDoctypePublicIdentifierDoubleQuotedTransitionOnNull() {
        Tokeniser t = createTokeniser("\0");
        TokeniserState.DoctypePublicIdentifier_doubleQuoted.read(t, t.reader);
        assertEquals("\uFFFD", t.dataBuffer.toString());
    }

    @Test
    public void testDoctypePublicIdentifierDoubleQuotedTransitionOnEOF() {
        Tokeniser t = createTokeniser("");
        TokeniserState.DoctypePublicIdentifier_doubleQuoted.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    // --- AfterDoctypePublicIdentifier state ---
    @Test
    public void testAfterDoctypePublicIdentifierTransitionOnWhitespace() {
        Tokeniser t = createTokeniser(" ");
        TokeniserState.AfterDoctypePublicIdentifier.read(t, t.reader);
        assertEquals(TokeniserState.BetweenDoctypePublicAndSystemIdentifiers, t.state);
    }

    @Test
    public void testAfterDoctypePublicIdentifierTransitionOnGt() {
        Tokeniser t = createTokeniser(">");
        TokeniserState.AfterDoctypePublicIdentifier.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testAfterDoctypePublicIdentifierTransitionOnEOF() {
        Tokeniser t = createTokeniser("");
        TokeniserState.AfterDoctypePublicIdentifier.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    // --- BetweenDoctypePublicAndSystemIdentifiers state ---
    @Test
    public void testBetweenDoctypePublicAndSystemIdentifiersTransitionOnGt() {
        Tokeniser t = createTokeniser(">");
        TokeniserState.BetweenDoctypePublicAndSystemIdentifiers.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testBetweenDoctypePublicAndSystemIdentifiersTransitionOnQuote() {
        Tokeniser t = createTokeniser("\"");
        TokeniserState.BetweenDoctypePublicAndSystemIdentifiers.read(t, t.reader);
        assertEquals(TokeniserState.DoctypeSystemIdentifier_doubleQuoted, t.state);
    }

    @Test
    public void testBetweenDoctypePublicAndSystemIdentifiersTransitionOnEOF() {
        Tokeniser t = createTokeniser("");
        TokeniserState.BetweenDoctypePublicAndSystemIdentifiers.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    // --- AfterDoctypeSystemKeyword state ---
    @Test
    public void testAfterDoctypeSystemKeywordTransitionOnWhitespace() {
        Tokeniser t = createTokeniser(" ");
        TokeniserState.AfterDoctypeSystemKeyword.read(t, t.reader);
        assertEquals(TokeniserState.BeforeDoctypeSystemIdentifier, t.state);
    }

    @Test
    public void testAfterDoctypeSystemKeywordTransitionOnQuote() {
        Tokeniser t = createTokeniser("\"");
        TokeniserState.AfterDoctypeSystemKeyword.read(t, t.reader);
        assertEquals(TokeniserState.DoctypeSystemIdentifier_doubleQuoted, t.state);
    }

    @Test
    public void testAfterDoctypeSystemKeywordTransitionOnGt() {
        Tokeniser t = createTokeniser(">");
        TokeniserState.AfterDoctypeSystemKeyword.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testAfterDoctypeSystemKeywordTransitionOnEOF() {
        Tokeniser t = createTokeniser("");
        TokeniserState.AfterDoctypeSystemKeyword.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    // --- BeforeDoctypeSystemIdentifier state ---
    @Test
    public void testBeforeDoctypeSystemIdentifierTransitionOnQuote() {
        Tokeniser t = createTokeniser("\"");
        TokeniserState.BeforeDoctypeSystemIdentifier.read(t, t.reader);
        assertEquals(TokeniserState.DoctypeSystemIdentifier_doubleQuoted, t.state);
    }

    @Test
    public void testBeforeDoctypeSystemIdentifierTransitionOnGt() {
        Tokeniser t = createTokeniser(">");
        TokeniserState.BeforeDoctypeSystemIdentifier.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testBeforeDoctypeSystemIdentifierTransitionOnEOF() {
        Tokeniser t = createTokeniser("");
        TokeniserState.BeforeDoctypeSystemIdentifier.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    // --- DoctypeSystemIdentifier_doubleQuoted state ---
    @Test
    public void testDoctypeSystemIdentifierDoubleQuotedConsumes() {
        Tokeniser t = createTokeniser("systemID\"");
        TokeniserState.DoctypeSystemIdentifier_doubleQuoted.read(t, t.reader);
        assertEquals("systemID", t.dataBuffer.toString());
    }

    @Test
    public void testDoctypeSystemIdentifierDoubleQuotedTransitionOnQuote() {
        Tokeniser t = createTokeniser("\"");
        TokeniserState.DoctypeSystemIdentifier_doubleQuoted.read(t, t.reader);
        assertEquals(TokeniserState.AfterDoctypeSystemIdentifier, t.state);
    }

    @Test
    public void testDoctypeSystemIdentifierDoubleQuotedTransitionOnNull() {
        Tokeniser t = createTokeniser("\0");
        TokeniserState.DoctypeSystemIdentifier_doubleQuoted.read(t, t.reader);
        assertEquals("\uFFFD", t.dataBuffer.toString());
    }

    @Test
    public void testDoctypeSystemIdentifierDoubleQuotedTransitionOnEOF() {
        Tokeniser t = createTokeniser("");
        TokeniserState.DoctypeSystemIdentifier_doubleQuoted.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    // --- AfterDoctypeSystemIdentifier state ---
    @Test
    public void testAfterDoctypeSystemIdentifierTransitionOnWhitespace() {
        Tokeniser t = createTokeniser(" ");
        TokeniserState.AfterDoctypeSystemIdentifier.read(t, t.reader);
        assertEquals(TokeniserState.BogusDoctype, t.state);
    }

    @Test
    public void testAfterDoctypeSystemIdentifierTransitionOnGt() {
        Tokeniser t = createTokeniser(">");
        TokeniserState.AfterDoctypeSystemIdentifier.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testAfterDoctypeSystemIdentifierTransitionOnEOF() {
        Tokeniser t = createTokeniser("");
        TokeniserState.AfterDoctypeSystemIdentifier.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    // --- BogusDoctype state ---
    @Test
    public void testBogusDoctypeConsumesUntilGt() {
        Tokeniser t = createTokeniser("some bogus>");
        TokeniserState.BogusDoctype.read(t, t.reader);
        assertEquals("some bogus", t.dataBuffer.toString());
    }

    @Test
    public void testBogusDoctypeTransitionOnGt() {
        Tokeniser t = createTokeniser(">");
        TokeniserState.BogusDoctype.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testBogusDoctypeTransitionOnEOF() {
        Tokeniser t = createTokeniser("");
        TokeniserState.BogusDoctype.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    // --- CDATA state ---
    @Test
    public void testCDATAConsumes() {
        Tokeniser t = createTokeniser("cdata]]>");
        TokeniserState.CDATA.read(t, t.reader);
        assertEquals("cdata", t.dataBuffer.toString());
    }

    @Test
    public void testCDATATransitionOnCloseBracket() {
        Tokeniser t = createTokeniser("]");
        TokeniserState.CDATA.read(t, t.reader);
        assertEquals(TokeniserState.CDATA, t.state); // stays in CDATA until two brackets
    }

    @Test
    public void testCDATATransitionOnTwoCloseBrackets() {
        Tokeniser t = createTokeniser("]]");
        TokeniserState.CDATA.read(t, t.reader);
        assertEquals(TokeniserState.CDATA, t.state); // still not > after
    }

    @Test
    public void testCDATATransitionOnFullClose() {
        Tokeniser t = createTokeniser("]]>");
        TokeniserState.CDATA.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testCDATATransitionOnNull() {
        Tokeniser t = createTokeniser("\0");
        TokeniserState.CDATA.read(t, t.reader);
        assertEquals("\uFFFD", t.dataBuffer.toString());
    }

    @Test
    public void testCDATATransitionOnEOF() {
        Tokeniser t = createTokeniser("");
        TokeniserState.CDATA.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    // --- Character reference states (simplified) ---
    @Test
    public void testCharacterReferenceInData() {
        Tokeniser t = createTokeniser("&amp;");
        TokeniserState.CharacterReferenceInData.read(t, t.reader);
        // Should emit '&' and then process the reference
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testCharacterReferenceInDataNoSemicolon() {
        Tokeniser t = createTokeniser("&amp");
        TokeniserState.CharacterReferenceInData.read(t, t.reader);
        // Should emit '&' and then the characters
        assertTrue(t.isEmitPending());
    }

    // --- Additional edge cases ---
    @Test
    public void testDataStateWithMultipleLt() {
        Tokeniser t = createTokeniser("<<");
        TokeniserState.Data.read(t, t.reader);
        // First '<' triggers TagOpen, second '<' should be handled in TagOpen
        assertEquals(TokeniserState.TagOpen, t.state);
    }

    @Test
    public void testTagNameWithUpperCase() {
        Tokeniser t = createTokeniser("DIV>");
        TokeniserState.TagName.read(t, t.reader);
        assertEquals("div", t.tagName.toString()); // lowercased
    }

    @Test
    public void testAttributeNameWithUpperCase() {
        Tokeniser t = createTokeniser("CLASS=");
        TokeniserState.AttributeName.read(t, t.reader);
        assertEquals("class", t.tagName.toString()); // lowercased
    }

    @Test
    public void testUnquotedAttributeValueWithSpecialChars() {
        Tokeniser t = createTokeniser("val>");
        TokeniserState.AttributeValue_unquoted.read(t, t.reader);
        assertEquals("val", t.dataBuffer.toString());
    }

    @Test
    public void testCommentWithEmptyContent() {
        Tokeniser t = createTokeniser("-->");
        TokeniserState.Comment.read(t, t.reader);
        assertEquals("", t.dataBuffer.toString());
    }

    @Test
    public void testDoctypeWithNoName() {
        Tokeniser t = createTokeniser(">");
        TokeniserState.Doctype.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testBogusDoctypeWithNoContent() {
        Tokeniser t = createTokeniser(">");
        TokeniserState.BogusDoctype.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testCDATAWithNoContent() {
        Tokeniser t = createTokeniser("]]>");
        TokeniserState.CDATA.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    // --- Null and empty input edge cases ---
    @Test(expected = NullPointerException.class)
    public void testNullReader() {
        Tokeniser t = createTokeniser("");
        t.reader = null;
        TokeniserState.Data.read(t, null);
    }

    @Test
    public void testEmptyInputData() {
        Tokeniser t = createTokeniser("");
        TokeniserState.Data.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testEmptyInputTagOpen() {
        Tokeniser t = createTokeniser("");
        TokeniserState.TagOpen.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testEmptyInputTagName() {
        Tokeniser t = createTokeniser("");
        TokeniserState.TagName.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testEmptyInputBeforeAttributeName() {
        Tokeniser t = createTokeniser("");
        TokeniserState.BeforeAttributeName.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testEmptyInputAttributeName() {
        Tokeniser t = createTokeniser("");
        TokeniserState.AttributeName.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testEmptyInputAfterAttributeName() {
        Tokeniser t = createTokeniser("");
        TokeniserState.AfterAttributeName.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testEmptyInputBeforeAttributeValue() {
        Tokeniser t = createTokeniser("");
        TokeniserState.BeforeAttributeValue.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testEmptyInputDoubleQuotedValue() {
        Tokeniser t = createTokeniser("");
        TokeniserState.AttributeValue_doubleQuoted.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testEmptyInputSingleQuotedValue() {
        Tokeniser t = createTokeniser("");
        TokeniserState.AttributeValue_singleQuoted.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testEmptyInputUnquotedValue() {
        Tokeniser t = createTokeniser("");
        TokeniserState.AttributeValue_unquoted.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testEmptyInputSelfClosingStartTag() {
        Tokeniser t = createTokeniser("");
        TokeniserState.SelfClosingStartTag.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testEmptyInputEndTagOpen() {
        Tokeniser t = createTokeniser("");
        TokeniserState.EndTagOpen.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testEmptyInputMarkupDeclarationOpen() {
        Tokeniser t = createTokeniser("");
        TokeniserState.MarkupDeclarationOpen.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testEmptyInputComment() {
        Tokeniser t = createTokeniser("");
        TokeniserState.Comment.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testEmptyInputCommentEnd() {
        Tokeniser t = createTokeniser("");
        TokeniserState.CommentEnd.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testEmptyInputCommentEndBang() {
        Tokeniser t = createTokeniser("");
        TokeniserState.CommentEndBang.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testEmptyInputCommentEndDash() {
        Tokeniser t = createTokeniser("");
        TokeniserState.CommentEndDash.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testEmptyInputDoctype() {
        Tokeniser t = createTokeniser("");
        TokeniserState.Doctype.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testEmptyInputBeforeDoctypeName() {
        Tokeniser t = createTokeniser("");
        TokeniserState.BeforeDoctypeName.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testEmptyInputDoctypeName() {
        Tokeniser t = createTokeniser("");
        TokeniserState.DoctypeName.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testEmptyInputAfterDoctypeName() {
        Tokeniser t = createTokeniser("");
        TokeniserState.AfterDoctypeName.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testEmptyInputAfterDoctypePublicKeyword() {
        Tokeniser t = createTokeniser("");
        TokeniserState.AfterDoctypePublicKeyword.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testEmptyInputBeforeDoctypePublicIdentifier() {
        Tokeniser t = createTokeniser("");
        TokeniserState.BeforeDoctypePublicIdentifier.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testEmptyInputDoctypePublicIdentifierDoubleQuoted() {
        Tokeniser t = createTokeniser("");
        TokeniserState.DoctypePublicIdentifier_doubleQuoted.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testEmptyInputAfterDoctypePublicIdentifier() {
        Tokeniser t = createTokeniser("");
        TokeniserState.AfterDoctypePublicIdentifier.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testEmptyInputBetweenDoctypePublicAndSystemIdentifiers() {
        Tokeniser t = createTokeniser("");
        TokeniserState.BetweenDoctypePublicAndSystemIdentifiers.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testEmptyInputAfterDoctypeSystemKeyword() {
        Tokeniser t = createTokeniser("");
        TokeniserState.AfterDoctypeSystemKeyword.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testEmptyInputBeforeDoctypeSystemIdentifier() {
        Tokeniser t = createTokeniser("");
        TokeniserState.BeforeDoctypeSystemIdentifier.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testEmptyInputDoctypeSystemIdentifierDoubleQuoted() {
        Tokeniser t = createTokeniser("");
        TokeniserState.DoctypeSystemIdentifier_doubleQuoted.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testEmptyInputAfterDoctypeSystemIdentifier() {
        Tokeniser t = createTokeniser("");
        TokeniserState.AfterDoctypeSystemIdentifier.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testEmptyInputBogusDoctype() {
        Tokeniser t = createTokeniser("");
        TokeniserState.BogusDoctype.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testEmptyInputCDATA() {
        Tokeniser t = createTokeniser("");
        TokeniserState.CDATA.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }

    @Test
    public void testEmptyInputCharacterReferenceInData() {
        Tokeniser t = createTokeniser("");
        TokeniserState.CharacterReferenceInData.read(t, t.reader);
        assertTrue(t.isEmitPending());
    }
}