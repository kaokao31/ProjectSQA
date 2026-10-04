package com.google.javascript.rhino;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TokenStreamTest {

  @Test
  public void testIsKeywordWithKeywords() {
    String[] keywords = {
      "abstract", "boolean", "break", "byte", "case", "catch", "char",
      "class", "const", "continue", "debugger", "default", "delete", "do",
      "double", "else", "enum", "export", "extends", "false", "final",
      "finally", "float", "for", "function", "goto", "if", "implements",
      "import", "in", "instanceof", "int", "interface", "long", "native",
      "new", "null", "package", "private", "protected", "public", "return",
      "short", "static", "super", "switch", "synchronized", "this", "throw",
      "throws", "transient", "true", "try", "typeof", "var", "void",
      "volatile", "while", "with", "get", "set"
    };

    for (String keyword : keywords) {
      assertTrue("isKeyword should return true for: " + keyword,
          TokenStream.isKeyword(keyword));
    }
  }

  @Test
  public void testIsKeywordWithNonKeywords() {
    String[] nonKeywords = {
      "", "foo", "bar", "getter", "setter", "classy", "constantly",
      "undefined", "eval", "arguments", "NaN", "Infinity", "constructor",
      "toString", "valueOf"
    };

    for (String nonKeyword : nonKeywords) {
      assertFalse("isKeyword should return false for: " + nonKeyword,
          TokenStream.isKeyword(nonKeyword));
    }
  }

  @Test
  public void testIsReservedKeywordWithReservedWords() {
    String[] reservedWords = {
      "abstract", "boolean", "break", "byte", "case", "catch", "char",
      "class", "const", "continue", "debugger", "default", "delete", "do",
      "double", "else", "enum", "export", "extends", "false", "final",
      "finally", "float", "for", "function", "goto", "if", "implements",
      "import", "in", "instanceof", "int", "interface", "long", "native",
      "new", "null", "package", "private", "protected", "public", "return",
      "short", "static", "super", "switch", "synchronized", "this", "throw",
      "throws", "transient", "true", "try", "typeof", "var", "void",
      "volatile", "while", "with"
    };

    for (String reserved : reservedWords) {
      assertTrue("isReservedKeyword should return true for: " + reserved,
          TokenStream.isReservedKeyword(reserved));
    }
  }

  @Test
  public void testIsReservedKeywordWithNonReservedWords() {
    assertFalse(TokenStream.isReservedKeyword(""));
    assertFalse(TokenStream.isReservedKeyword("foo"));
    assertFalse(TokenStream.isReservedKeyword("bar"));
    assertFalse(TokenStream.isReservedKeyword("get"));
    assertFalse(TokenStream.isReservedKeyword("set"));
    assertFalse(TokenStream.isReservedKeyword("undefined"));
    assertFalse(TokenStream.isReservedKeyword("eval"));
    assertFalse(TokenStream.isReservedKeyword("arguments"));
    assertFalse(TokenStream.isReservedKeyword("getter"));
    assertFalse(TokenStream.isReservedKeyword("setter"));
  }

  @Test
  public void testIsReservedKeywordWithDynamicStrings() {
    // These are intentionally not string literals so that the old
    // reference-equality bug in isReservedKeyword is triggered.
    String get = new String("get");
    String set = new String("set");
    String brk = new String("break");

    assertFalse("isReservedKeyword should return false for dynamic \"get\"",
        TokenStream.isReservedKeyword(get));
    assertFalse("isReservedKeyword should return false for dynamic \"set\"",
        TokenStream.isReservedKeyword(set));
    assertTrue("isReservedKeyword should return true for dynamic \"break\"",
        TokenStream.isReservedKeyword(brk));
  }

  @Test
  public void testKeywordToName() {
    assertEquals("break", TokenStream.keywordToName(Token.BREAK));
    assertEquals("if", TokenStream.keywordToName(Token.IF));
    assertEquals("get", TokenStream.keywordToName(Token.GET));
    assertEquals("set", TokenStream.keywordToName(Token.SET));
    assertNull(TokenStream.keywordToName(-1));
  }

  @Test
  public void testIsJSIdentifier() {
    assertTrue(TokenStream.isJSIdentifier("a"));
    assertTrue(TokenStream.isJSIdentifier("abc"));
    assertTrue(TokenStream.isJSIdentifier("a1"));
    assertTrue(TokenStream.isJSIdentifier("$"));
    assertTrue(TokenStream.isJSIdentifier("_"));
    assertTrue(TokenStream.isJSIdentifier("$foo"));
    assertTrue(TokenStream.isJSIdentifier("_foo"));
    assertTrue(TokenStream.isJSIdentifier("a_"));
    assertTrue(TokenStream.isJSIdentifier("a$"));

    assertFalse(TokenStream.isJSIdentifier(""));
    assertFalse(TokenStream.isJSIdentifier("1"));
    assertFalse(TokenStream.isJSIdentifier("1abc"));
    assertFalse(TokenStream.isJSIdentifier("-"));
    assertFalse(TokenStream.isJSIdentifier("a b"));
    assertFalse(TokenStream.isJSIdentifier("a-b"));
    assertFalse(TokenStream.isJSIdentifier("a.b"));
    assertFalse(TokenStream.isJSIdentifier("a/b"));
    assertFalse(TokenStream.isJSIdentifier("a+b"));
  }
}