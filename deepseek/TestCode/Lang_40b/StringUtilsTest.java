```java
package org.apache.commons.lang;

import org.junit.Test;
import static org.junit.Assert.*;

public class StringUtilsTest {

    @Test
    public void testContainsIgnoreCase_LocaleIndependence() {
        // Test case from the failing test - should pass with correct implementation
        String str = "ß";
        String searchStr = "SS";
        assertTrue("en: " + str + " " + searchStr, StringUtils.containsIgnoreCase(str, searchStr));
        
        // Additional locale independence checks
        assertTrue("tr: " + str + " " + searchStr, StringUtils.containsIgnoreCase(str, searchStr));
        assertTrue("de: " + str + " " + searchStr, StringUtils.containsIgnoreCase(str, searchStr));
        
        // Reverse case
        assertTrue("en: " + searchStr + " " + str, StringUtils.containsIgnoreCase(searchStr, str));
        
        // Test with different unicode characters
        assertTrue("Greek sigma", StringUtils.containsIgnoreCase("Σ", "σ"));
        assertTrue("Greek sigma reverse", StringUtils.containsIgnoreCase("σ", "Σ"));
        
        // Test with Turkish dotless i
        assertTrue("Turkish i", StringUtils.containsIgnoreCase("I", "ı"));
        assertTrue("Turkish i reverse", StringUtils.containsIgnoreCase("ı", "I"));
    }

    @Test
    public void testContainsIgnoreCase_NullAndEmpty() {
        // Null handling
        assertFalse(StringUtils.containsIgnoreCase(null, null));
        assertFalse(StringUtils.containsIgnoreCase(null, ""));
        assertFalse(StringUtils.containsIgnoreCase("", null));
        assertFalse(StringUtils.containsIgnoreCase("abc", null));
        assertFalse(StringUtils.containsIgnoreCase(null, "abc"));
        
        // Empty string handling
        assertTrue(StringUtils.containsIgnoreCase("", ""));
        assertTrue(StringUtils.containsIgnoreCase("abc", ""));
        assertFalse(StringUtils.containsIgnoreCase("", "abc"));
        
        // Whitespace handling
        assertTrue(StringUtils.containsIgnoreCase("  ", " "));
        assertTrue(StringUtils.containsIgnoreCase("a b c", " "));
        assertFalse(StringUtils.containsIgnoreCase("abc", " "));
    }

    @Test
    public void testContainsIgnoreCase_BasicCases() {
        // Basic case-insensitive contains
        assertTrue(StringUtils.containsIgnoreCase("Hello World", "hello"));
        assertTrue(StringUtils.containsIgnoreCase("Hello World", "WORLD"));
        assertTrue(StringUtils.containsIgnoreCase("Hello World", "o W"));
        assertTrue(StringUtils.containsIgnoreCase("Hello World", "HELLO WORLD"));
        
        // Case sensitivity
        assertTrue(StringUtils.containsIgnoreCase("Hello", "hELLo"));
        assertTrue(StringUtils.containsIgnoreCase("Hello", "HELLO"));
        assertTrue(StringUtils.containsIgnoreCase("Hello", "hello"));
        
        // Substring at boundaries
        assertTrue(StringUtils.containsIgnoreCase("Hello", "H"));
        assertTrue(StringUtils.containsIgnoreCase("Hello", "o"));
        assertTrue(StringUtils.containsIgnoreCase("Hello", "Hel"));
        assertTrue(StringUtils.containsIgnoreCase("Hello", "llo"));
        
        // Non-matching cases
        assertFalse(StringUtils.containsIgnoreCase("Hello", "xyz"));
        assertFalse(StringUtils.containsIgnoreCase("Hello", "helloo"));
        assertFalse(StringUtils.containsIgnoreCase("Hello", "Hllo"));
    }

    @Test
    public void testContainsIgnoreCase_SpecialCharacters() {
        // Special characters and symbols
        assertTrue(StringUtils.containsIgnoreCase("Hello, World!", "hello,"));
        assertTrue(StringUtils.containsIgnoreCase("Hello, World!", "WORLD!"));
        assertTrue(StringUtils.containsIgnoreCase("Hello@World", "hello@world"));
        assertTrue(StringUtils.containsIgnoreCase("Hello#World", "hello#world"));
        
        // Numbers
        assertTrue(StringUtils.containsIgnoreCase("Hello123", "hello123"));
        assertTrue(StringUtils.containsIgnoreCase("123Hello", "123hello"));
        assertTrue(StringUtils.containsIgnoreCase("123Hello456", "hello456"));
        
        // Mixed alphanumeric
        assertTrue(StringUtils.containsIgnoreCase("abc123DEF", "ABC123def"));
        assertFalse(StringUtils.containsIgnoreCase("abc123DEF", "abc123defg"));
    }

    @Test
    public void testContainsIgnoreCase_UnicodeAndExtendedChars() {
        // Extended ASCII characters
        assertTrue(StringUtils.containsIgnoreCase("café", "CAFÉ"));
        assertTrue(StringUtils.containsIgnoreCase("café", "café"));
        assertTrue(StringUtils.containsIgnoreCase("café", "CAFE"));
        
        // Accented characters
        assertTrue(StringUtils.containsIgnoreCase("résumé", "RÉSUMÉ"));
        assertTrue(StringUtils.containsIgnoreCase("résumé", "resume"));
        
        // Unicode characters
        assertTrue(StringUtils.containsIgnoreCase("日本語", "日本語"));
        assertTrue(StringUtils.containsIgnoreCase("日本語", "日本"));
        assertTrue(StringUtils.containsIgnoreCase("日本語", "語"));
        
        // Emoji and symbols
        assertTrue(StringUtils.containsIgnoreCase("Hello 😀 World", "hello 😀"));
        assertTrue(StringUtils.containsIgnoreCase("Hello 😀 World", "😀 WORLD"));
    }

    @Test
    public void testContainsIgnoreCase_LongStrings() {
        // Long strings
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("a");
        }
        String longStr = sb.toString();
        assertTrue(StringUtils.containsIgnoreCase(longStr, "AAA"));
        assertTrue(StringUtils.containsIgnoreCase(longStr, "a"));
        assertFalse(StringUtils.containsIgnoreCase(longStr, "b"));
        
        // Very long search string
        StringBuilder searchSb = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            searchSb.append("ab");
        }
        String longSearch = searchSb.toString();
        assertTrue(StringUtils.containsIgnoreCase(longStr + longSearch + longStr, longSearch));
    }

    @Test
    public void testContainsIgnoreCase_EdgeCases() {
        // Single character strings
        assertTrue(StringUtils.containsIgnoreCase("a", "A"));
        assertTrue(StringUtils.containsIgnoreCase("A", "a"));
        assertTrue(StringUtils.containsIgnoreCase("a", "a"));
        assertFalse(StringUtils.containsIgnoreCase("a", "b"));
        
        // Strings of different lengths
        assertFalse(StringUtils.containsIgnoreCase("a", "ab"));
        assertFalse(StringUtils.containsIgnoreCase("ab", "a"));
        
        // Search string longer than source
        assertFalse(StringUtils.containsIgnoreCase("abc", "abcd"));
        
        // Whitespace-only strings
        assertTrue(StringUtils.containsIgnoreCase("   ", " "));
        assertTrue(StringUtils.containsIgnoreCase("   ", "  "));
        assertFalse(StringUtils.containsIgnoreCase("   ", "a"));
        
        // Tabs and newlines
        assertTrue(StringUtils.containsIgnoreCase("Hello\tWorld", "hello\tworld"));
        assertTrue(StringUtils.containsIgnoreCase("Hello\nWorld", "hello\nworld"));
        assertTrue(StringUtils.containsIgnoreCase("Hello\r\nWorld", "hello\r\nworld"));
    }

    @Test
    public void testContainsIgnoreCase_Regression() {
        // Regression tests for known issues
        assertTrue(StringUtils.containsIgnoreCase("ß", "SS"));
        assertTrue(StringUtils.containsIgnoreCase("SS", "ß"));
        assertTrue(StringUtils.containsIgnoreCase("Straße", "STRASSE"));
        assertTrue(StringUtils.containsIgnoreCase("STRASSE", "straße"));
        
        // Turkish locale specific
        assertTrue(StringUtils.containsIgnoreCase("İ", "i"));
        assertTrue(StringUtils.containsIgnoreCase("i", "İ"));
        
        // German sharp s
        assertTrue(StringUtils.containsIgnoreCase("Maße", "MASSE"));
        assertTrue(StringUtils.containsIgnoreCase("MASSE", "Maße"));
    }
}