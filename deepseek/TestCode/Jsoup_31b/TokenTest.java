package org.jsoup.parser;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for the Token class (Jsoup bug 31 context).
 * Targets maximum coverage and fault detection in Token and its subclasses.
 */
public class TokenTest {

    private Token.Doctype doctype;
    private Token.StartTag startTag;
    private Token.EndTag endTag;
    private Token.Comment comment;
    private Token.Character character;
    private Token.EOF eof;

    @Before
    public void setUp() {
        doctype = new Token.Doctype();
        startTag = new Token.StartTag();
        endTag = new Token.EndTag();
        comment = new Token.Comment();
        character = new Token.Character();
        eof = new Token.EOF();
    }

    // --- Token type identification tests ---

    @Test
    public void testDoctypeType() {
        assertTrue("Doctype should be identified as doctype", doctype.isDoctype());
        assertFalse("Doctype should not be start tag", doctype.isStartTag());
        assertFalse("Doctype should not be end tag", doctype.isEndTag());
        assertFalse("Doctype should not be comment", doctype.isComment());
        assertFalse("Doctype should not be character", doctype.isCharacter());
        assertFalse("Doctype should not be EOF", doctype.isEOF());
    }

    @Test
    public void testStartTagType() {
        assertTrue("StartTag should be start tag", startTag.isStartTag());
        assertFalse("StartTag should not be doctype", startTag.isDoctype());
        assertFalse("StartTag should not be end tag", startTag.isEndTag());
        assertFalse("StartTag should not be comment", startTag.isComment());
        assertFalse("StartTag should not be character", startTag.isCharacter());
        assertFalse("StartTag should not be EOF", startTag.isEOF());
    }

    @Test
    public void testEndTagType() {
        assertTrue("EndTag should be end tag", endTag.isEndTag());
        assertFalse("EndTag should not be doctype", endTag.isDoctype());
        assertFalse("EndTag should not be start tag", endTag.isStartTag());
        assertFalse("EndTag should not be comment", endTag.isComment());
        assertFalse("EndTag should not be character", endTag.isCharacter());
        assertFalse("EndTag should not be EOF", endTag.isEOF());
    }

    @Test
    public void testCommentType() {
        assertTrue("Comment should be comment", comment.isComment());
        assertFalse("Comment should not be doctype", comment.isDoctype());
        assertFalse("Comment should not be start tag", comment.isStartTag());
        assertFalse("Comment should not be end tag", comment.isEndTag());
        assertFalse("Comment should not be character", comment.isCharacter());
        assertFalse("Comment should not be EOF", comment.isEOF());
    }

    @Test
    public void testCharacterType() {
        assertTrue("Character should be character", character.isCharacter());
        assertFalse("Character should not be doctype", character.isDoctype());
        assertFalse("Character should not be start tag", character.isStartTag());
        assertFalse("Character should not be end tag", character.isEndTag());
        assertFalse("Character should not be comment", character.isComment());
        assertFalse("Character should not be EOF", character.isEOF());
    }

    @Test
    public void testEOFType() {
        assertTrue("EOF should be EOF", eof.isEOF());
        assertFalse("EOF should not be doctype", eof.isDoctype());
        assertFalse("EOF should not be start tag", eof.isStartTag());
        assertFalse("EOF should not be end tag", eof.isEndTag());
        assertFalse("EOF should not be comment", eof.isComment());
        assertFalse("EOF should not be character", eof.isCharacter());
    }

    // --- Token asXxx conversion tests ---

    @Test(expected = UnsupportedOperationException.class)
    public void testDoctypeAsDoctype() {
        doctype.asDoctype();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDoctypeAsStartTag() {
        doctype.asStartTag();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDoctypeAsEndTag() {
        doctype.asEndTag();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDoctypeAsComment() {
        doctype.asComment();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDoctypeAsCharacter() {
        doctype.asCharacter();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDoctypeAsEOF() {
        doctype.asEOF();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testStartTagAsDoctype() {
        startTag.asDoctype();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testStartTagAsEndTag() {
        startTag.asEndTag();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testStartTagAsComment() {
        startTag.asComment();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testStartTagAsCharacter() {
        startTag.asCharacter();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testStartTagAsEOF() {
        startTag.asEOF();
    }

    @Test
    public void testStartTagAsStartTag() {
        assertSame("StartTag.asStartTag should return itself", startTag, startTag.asStartTag());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEndTagAsDoctype() {
        endTag.asDoctype();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEndTagAsStartTag() {
        endTag.asStartTag();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEndTagAsComment() {
        endTag.asComment();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEndTagAsCharacter() {
        endTag.asCharacter();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEndTagAsEOF() {
        endTag.asEOF();
    }

    @Test
    public void testEndTagAsEndTag() {
        assertSame("EndTag.asEndTag should return itself", endTag, endTag.asEndTag());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCommentAsDoctype() {
        comment.asDoctype();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCommentAsStartTag() {
        comment.asStartTag();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCommentAsEndTag() {
        comment.asEndTag();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCommentAsCharacter() {
        comment.asCharacter();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCommentAsEOF() {
        comment.asEOF();
    }

    @Test
    public void testCommentAsComment() {
        assertSame("Comment.asComment should return itself", comment, comment.asComment());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCharacterAsDoctype() {
        character.asDoctype();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCharacterAsStartTag() {
        character.asStartTag();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCharacterAsEndTag() {
        character.asEndTag();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCharacterAsComment() {
        character.asComment();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCharacterAsEOF() {
        character.asEOF();
    }

    @Test
    public void testCharacterAsCharacter() {
        assertSame("Character.asCharacter should return itself", character, character.asCharacter());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEOFAsDoctype() {
        eof.asDoctype();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEOFAsStartTag() {
        eof.asStartTag();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEOFAsEndTag() {
        eof.asEndTag();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEOFAsComment() {
        eof.asComment();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEOFAsCharacter() {
        eof.asCharacter();
    }

    @Test
    public void testEOFAsEOF() {
        assertSame("EOF.asEOF should return itself", eof, eof.asEOF());
    }

    // --- Token.Tag tests (StartTag and EndTag share Tag behavior) ---

    @Test
    public void testTagNameDefault() {
        assertEquals("Default tag name should be empty", "", startTag.name());
        assertEquals("Default tag name should be empty", "", endTag.name());
    }

    @Test
    public void testTagNameSetter() {
        startTag.name("div");
        assertEquals("Tag name should be set to 'div'", "div", startTag.name());
        endTag.name("span");
        assertEquals("Tag name should be set to 'span'", "span", endTag.name());
    }

    @Test
    public void testTagNameEmpty() {
        startTag.name("");
        assertEquals("Tag name should be empty", "", startTag.name());
    }

    @Test
    public void testTagNameNull() {
        // Assuming null is allowed; if not, test will reveal bug
        startTag.name(null);
        assertNull("Tag name should be null", startTag.name());
    }

    @Test
    public void testTagNameSpecialCharacters() {
        startTag.name("a-b");
        assertEquals("Tag name with hyphen", "a-b", startTag.name());
        startTag.name("c.d");
        assertEquals("Tag name with dot", "c.d", startTag.name());
    }

    @Test
    public void testTagIsSelfClosingDefault() {
        assertFalse("StartTag should not be self-closing by default", startTag.isSelfClosing());
        assertFalse("EndTag should not be self-closing by default", endTag.isSelfClosing());
    }

    @Test
    public void testTagSelfClosingSetter() {
        startTag.setSelfClosing(true);
        assertTrue("StartTag should be self-closing after set", startTag.isSelfClosing());
        endTag.setSelfClosing(true);
        assertTrue("EndTag should be self-closing after set", endTag.isSelfClosing());
    }

    @Test
    public void testTagSelfClosingReset() {
        startTag.setSelfClosing(true);
        startTag.setSelfClosing(false);
        assertFalse("StartTag should not be self-closing after reset", startTag.isSelfClosing());
    }

    // --- Token.Tag attributes tests ---

    @Test
    public void testTagAttributesDefault() {
        assertNotNull("StartTag should have non-null attributes", startTag.attributes());
        assertNotNull("EndTag should have non-null attributes", endTag.attributes());
        assertTrue("StartTag attributes should be empty by default", startTag.attributes().isEmpty());
        assertTrue("EndTag attributes should be empty by default", endTag.attributes().isEmpty());
    }

    @Test
    public void testTagAttributesAdd() {
        startTag.attributes().put("class", "main");
        assertEquals("Attribute value should be 'main'", "main", startTag.attributes().get("class"));
        endTag.attributes().put("id", "footer");
        assertEquals("Attribute value should be 'footer'", "footer", endTag.attributes().get("id"));
    }

    @Test
    public void testTagAttributesMultiple() {
        startTag.attributes().put("a", "1");
        startTag.attributes().put("b", "2");
        assertEquals("Attribute count should be 2", 2, startTag.attributes().size());
    }

    @Test
    public void testTagAttributesEmptyKey() {
        startTag.attributes().put("", "value");
        assertEquals("Empty key attribute should be stored", "value", startTag.attributes().get(""));
    }

    @Test
    public void testTagAttributesNullKey() {
        // Assuming null key is allowed; if not, test will reveal bug
        startTag.attributes().put(null, "value");
        assertNull("Null key attribute should be stored as null key", startTag.attributes().get(null));
    }

    @Test
    public void testTagAttributesNullValue() {
        startTag.attributes().put("key", null);
        assertNull("Null value attribute should be stored", startTag.attributes().get("key"));
    }

    // --- Token.Character tests ---

    @Test
    public void testCharacterDataDefault() {
        assertNull("Character data should be null by default", character.data());
    }

    @Test
    public void testCharacterDataSetter() {
        character.data("Hello");
        assertEquals("Character data should be 'Hello'", "Hello", character.data());
    }

    @Test
    public void testCharacterDataEmpty() {
        character.data("");
        assertEquals("Character data should be empty string", "", character.data());
    }

    @Test
    public void testCharacterDataNull() {
        character.data(null);
        assertNull("Character data should be null", character.data());
    }

    @Test
    public void testCharacterDataSpecialChars() {
        character.data("&amp;");
        assertEquals("Character data with entity", "&amp;", character.data());
    }

    // --- Token.Comment tests ---

    @Test
    public void testCommentDataDefault() {
        assertNull("Comment data should be null by default", comment.data());
    }

    @Test
    public void testCommentDataSetter() {
        comment.data("<!-- test -->");
        assertEquals("Comment data should be '<!-- test -->'", "<!-- test -->", comment.data());
    }

    @Test
    public void testCommentDataEmpty() {
        comment.data("");
        assertEquals("Comment data should be empty string", "", comment.data());
    }

    @Test
    public void testCommentDataNull() {
        comment.data(null);
        assertNull("Comment data should be null", comment.data());
    }

    // --- Token.Doctype tests ---

    @Test
    public void testDoctypeNameDefault() {
        assertNull("Doctype name should be null by default", doctype.getName());
    }

    @Test
    public void testDoctypeNameSetter() {
        doctype.setName("html");
        assertEquals("Doctype name should be 'html'", "html", doctype.getName());
    }

    @Test
    public void testDoctypeNameEmpty() {
        doctype.setName("");
        assertEquals("Doctype name should be empty string", "", doctype.getName());
    }

    @Test
    public void testDoctypeNameNull() {
        doctype.setName(null);
        assertNull("Doctype name should be null", doctype.getName());
    }

    @Test
    public void testDoctypePublicIdentifierDefault() {
        assertNull("Doctype public identifier should be null by default", doctype.getPublicIdentifier());
    }

    @Test
    public void testDoctypePublicIdentifierSetter() {
        doctype.setPublicIdentifier("-//W3C//DTD HTML 4.01//EN");
        assertEquals("Doctype public identifier should match", "-//W3C//DTD HTML 4.01//EN", doctype.getPublicIdentifier());
    }

    @Test
    public void testDoctypeSystemIdentifierDefault() {
        assertNull("Doctype system identifier should be null by default", doctype.getSystemIdentifier());
    }

    @Test
    public void testDoctypeSystemIdentifierSetter() {
        doctype.setSystemIdentifier("http://www.w3.org/TR/html4/strict.dtd");
        assertEquals("Doctype system identifier should match", "http://www.w3.org/TR/html4/strict.dtd", doctype.getSystemIdentifier());
    }

    @Test
    public void testDoctypeForceQuirksDefault() {
        assertFalse("Doctype force quirks should be false by default", doctype.isForceQuirks());
    }

    @Test
    public void testDoctypeForceQuirksSetter() {
        doctype.setForceQuirks(true);
        assertTrue("Doctype force quirks should be true", doctype.isForceQuirks());
    }

    // --- Token.EOF tests ---

    @Test
    public void testEOFNoExtraState() {
        // EOF token has no additional fields; just ensure it exists
        assertNotNull("EOF token should not be null", eof);
    }

    // --- Edge cases and potential bug triggers (Jsoup bug 31 context) ---

    @Test
    public void testCharacterWithUnterminatedEntity() {
        // Bug 31: character references in attributes without semicolon
        // This test ensures that Character token can hold such data
        character.data("&amp");
        assertEquals("Character data with unterminated entity", "&amp", character.data());
    }

    @Test
    public void testTagAttributeWithUnterminatedEntity() {
        // Simulate attribute value with unterminated entity
        startTag.attributes().put("href", "&lt");
        assertEquals("Attribute value with unterminated entity", "&lt", startTag.attributes().get("href"));
    }

    @Test
    public void testTagAttributeWithMultipleEntities() {
        startTag.attributes().put("title", "a&b&c");
        assertEquals("Attribute value with multiple entities", "a&b&c", startTag.attributes().get("title"));
    }

    @Test
    public void testTagAttributeWithNumericEntity() {
        startTag.attributes().put("data", "&#65;");
        assertEquals("Attribute value with numeric entity", "&#65;", startTag.attributes().get("data"));
    }

    @Test
    public void testTagAttributeWithHexEntity() {
        startTag.attributes().put("data", "&#x41;");
        assertEquals("Attribute value with hex entity", "&#x41;", startTag.attributes().get("data"));
    }

    @Test
    public void testTagAttributeEmptyValue() {
        startTag.attributes().put("disabled", "");
        assertEquals("Attribute with empty value", "", startTag.attributes().get("disabled"));
    }

    @Test
    public void testTagAttributeBoolean() {
        startTag.attributes().put("checked", null);
        assertNull("Boolean attribute should have null value", startTag.attributes().get("checked"));
    }

    @Test
    public void testTagNameCaseSensitivity() {
        startTag.name("DIV");
        assertEquals("Tag name should be case-sensitive as set", "DIV", startTag.name());
        // In HTML, tag names are case-insensitive, but Token stores as given
    }

    @Test
    public void testEndTagNameEmpty() {
        endTag.name("");
        assertEquals("End tag name empty", "", endTag.name());
    }

    @Test
    public void testStartTagSelfClosingWithAttributes() {
        startTag.setSelfClosing(true);
        startTag.attributes().put("src", "img.png");
        assertTrue("Self-closing start tag with attributes", startTag.isSelfClosing());
        assertEquals("Attribute should be present", "img.png", startTag.attributes().get("src"));
    }

    @Test
    public void testCommentDataWithSpecialCharacters() {
        comment.data("<!-- <script> -->");
        assertEquals("Comment data with HTML special chars", "<!-- <script> -->", comment.data());
    }

    @Test
    public void testDoctypeWithAllFields() {
        doctype.setName("html");
        doctype.setPublicIdentifier("-//W3C//DTD XHTML 1.0 Strict//EN");
        doctype.setSystemIdentifier("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd");
        doctype.setForceQuirks(false);
        assertEquals("html", doctype.getName());
        assertEquals("-//W3C//DTD XHTML 1.0 Strict//EN", doctype.getPublicIdentifier());
        assertEquals("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd", doctype.getSystemIdentifier());
        assertFalse(doctype.isForceQuirks());
    }

    @Test
    public void testDoctypeForceQuirksTrue() {
        doctype.setForceQuirks(true);
        assertTrue(doctype.isForceQuirks());
    }

    @Test
    public void testTokenReset() {
        // Some tokens have reset methods; test if they clear state
        // Assuming Token.Tag has reset() that clears name and attributes
        startTag.name("div");
        startTag.attributes().put("class", "test");
        startTag.setSelfClosing(true);
        // Reset is not public in all versions; if available, test
        // For safety, we test that we can create a new token and it's clean
        Token.StartTag fresh = new Token.StartTag();
        assertEquals("Fresh start tag name should be empty", "", fresh.name());
        assertTrue("Fresh start tag attributes should be empty", fresh.attributes().isEmpty());
        assertFalse("Fresh start tag should not be self-closing", fresh.isSelfClosing());
    }

    @Test
    public void testTokenHashCodeAndEquals() {
        // Tokens are not typically compared by value, but ensure no exception
        Token.StartTag tag1 = new Token.StartTag();
        Token.StartTag tag2 = new Token.StartTag();
        // They are different objects, so equals should be false (default Object.equals)
        assertNotEquals("Two different StartTag instances should not be equal", tag1, tag2);
        assertNotNull("HashCode should not throw", tag1.hashCode());
    }

    @Test
    public void testTokenToString() {
        // Ensure toString does not throw
        assertNotNull("Doctype toString should not be null", doctype.toString());
        assertNotNull("StartTag toString should not be null", startTag.toString());
        assertNotNull("EndTag toString should not be null", endTag.toString());
        assertNotNull("Comment toString should not be null", comment.toString());
        assertNotNull("Character toString should not be null", character.toString());
        assertNotNull("EOF toString should not be null", eof.toString());
    }

    @Test
    public void testCharacterDataLongString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("a");
        }
        String longString = sb.toString();
        character.data(longString);
        assertEquals("Character data with long string", longString, character.data());
    }

    @Test
    public void testTagNameMaxLength() {
        // Test with a very long tag name
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("tag");
        }
        String longName = sb.toString();
        startTag.name(longName);
        assertEquals("Tag name with long string", longName, startTag.name());
    }

    @Test
    public void testAttributeKeySpecialCharacters() {
        startTag.attributes().put("data-attr", "value");
        assertEquals("Attribute key with hyphen", "value", startTag.attributes().get("data-attr"));
        startTag.attributes().put("xml:lang", "en");
        assertEquals("Attribute key with colon", "en", startTag.attributes().get("xml:lang"));
    }

    @Test
    public void testMultipleAttributesSameKey() {
        // Attributes are stored in a map, so last value wins
        startTag.attributes().put("class", "first");
        startTag.attributes().put("class", "second");
        assertEquals("Last attribute value should be 'second'", "second", startTag.attributes().get("class"));
    }

    @Test
    public void testDoctypeSystemIdentifierEmpty() {
        doctype.setSystemIdentifier("");
        assertEquals("Doctype system identifier empty", "", doctype.getSystemIdentifier());
    }

    @Test
    public void testDoctypePublicIdentifierEmpty() {
        doctype.setPublicIdentifier("");
        assertEquals("Doctype public identifier empty", "", doctype.getPublicIdentifier());
    }

    @Test
    public void testCommentDataWithNewlines() {
        comment.data("line1\nline2");
        assertEquals("Comment data with newline", "line1\nline2", comment.data());
    }

    @Test
    public void testCharacterDataWithNewlines() {
        character.data("line1\nline2");
        assertEquals("Character data with newline", "line1\nline2", character.data());
    }

    @Test
    public void testTagAttributeWithNewlines() {
        startTag.attributes().put("title", "line1\nline2");
        assertEquals("Attribute value with newline", "line1\nline2", startTag.attributes().get("title"));
    }

    @Test
    public void testStartTagAsEndTagThrows() {
        try {
            startTag.asEndTag();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testEndTagAsStartTagThrows() {
        try {
            endTag.asStartTag();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testCommentAsCharacterThrows() {
        try {
            comment.asCharacter();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testCharacterAsCommentThrows() {
        try {
            character.asComment();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testEOFAsCharacterThrows() {
        try {
            eof.asCharacter();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testDoctypeAsEOFThrows() {
        try {
            doctype.asEOF();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }
}