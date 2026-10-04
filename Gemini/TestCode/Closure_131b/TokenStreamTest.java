package com.google.javascript.rhino;

import org.junit.Test;
import static org.junit.Assert.*;

public class TokenStreamTest {

    @Test
    public void testIsKeyword() {
        // Test known keywords
        assertTrue(TokenStream.isKeyword("function"));
        assertTrue(TokenStream.isKeyword("var"));
        assertTrue(TokenStream.isKeyword("if"));
        assertTrue(TokenStream.isKeyword("else"));
        assertTrue(TokenStream.isKeyword("return"));
        assertTrue(TokenStream.isKeyword("true"));
        assertTrue(TokenStream.isKeyword("false"));
        assertTrue(TokenStream.isKeyword("null"));
        assertTrue(TokenStream.isKeyword("this"));

        // Test non-keywords / identifiers
        assertFalse(TokenStream.isKeyword("notAKeyword"));
        assertFalse(TokenStream.isKeyword(""));
        assertFalse(TokenStream.isKeyword(null));
        assertFalse(TokenStream.isKeyword("functionx"));
    }

    @Test
    public void testIsIdentifier() {
        // Valid identifiers
        assertTrue(TokenStream.isIdentifier("a"));
        assertTrue(TokenStream.isIdentifier("abc"));
        assertTrue(TokenStream.isIdentifier("_abc"));
        assertTrue(TokenStream.isIdentifier("$abc"));
        assertTrue(TokenStream.isIdentifier("abc123_$"));

        // Invalid identifiers
        assertFalse(TokenStream.isIdentifier(null));
        assertFalse(TokenStream.isIdentifier(""));
        assertFalse(TokenStream.isIdentifier("123abc")); // Starts with digit
        assertFalse(TokenStream.isIdentifier("a b"));    // Contains space
        assertFalse(TokenStream.isIdentifier("a-b"));    // Contains hyphen
        assertFalse(TokenStream.isIdentifier(".a"));     // Starts with dot
    }

    @Test
    public void testIsJSIdentifier() {
        // Valid JS identifiers
        assertTrue(TokenStream.isJSIdentifier("a"));
        assertTrue(TokenStream.isJSIdentifier("abc"));
        assertTrue(TokenStream.isJSIdentifier("_abc"));
        assertTrue(TokenStream.isJSIdentifier("$abc"));
        assertTrue(TokenStream.isJSIdentifier("abc123_$"));

        // Invalid JS identifiers
        assertFalse(TokenStream.isJSIdentifier(null));
        assertFalse(TokenStream.isJSIdentifier(""));
        assertFalse(TokenStream.isJSIdentifier("123abc"));
        assertFalse(TokenStream.isJSIdentifier("a b"));
    }

    @Test
    public void testIsAllWhitespace() {
        assertTrue(TokenStream.isAllWhitespace(""));
        assertTrue(TokenStream.isAllWhitespace("   "));
        assertTrue(TokenStream.isAllWhitespace("\t\n\r"));
        
        assertFalse(TokenStream.isAllWhitespace(null));
        assertFalse(TokenStream.isAllWhitespace(" a "));
        assertFalse(TokenStream.isAllWhitespace("abc"));
    }

    @Test
    public void testGetTokenFromName() {
        assertEquals(Token.FUNCTION, TokenStream.getTokenFromName("function"));
        assertEquals(Token.VAR, TokenStream.getTokenFromName("var"));
        assertEquals(Token.IF, TokenStream.getTokenFromName("if"));
        
        // Non-existent name
        assertEquals(Token.ERROR, TokenStream.getTokenFromName("notAKeywordAtAll"));
        assertEquals(Token.ERROR, TokenStream.getTokenFromName(""));
        assertEquals(Token.ERROR, TokenStream.getTokenFromName(null));
    }

    @Test
    public void testIdentifierCharChecking() {
        assertTrue(TokenStream.isIdentifierData('a'));
        assertTrue(TokenStream.isIdentifierData('Z'));
        assertTrue(TokenStream.isIdentifierData('_'));
        assertTrue(TokenStream.isIdentifierData('$'));
        assertTrue(TokenStream.isIdentifierData('0'));
        
        assertFalse(TokenStream.isIdentifierData(' '));
        assertFalse(TokenStream.isIdentifierData('-'));
        assertFalse(TokenStream.isIdentifierData('.'));
    }
}