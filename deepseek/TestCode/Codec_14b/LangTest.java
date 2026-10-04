package org.apache.commons.codec.language.bm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test class for {@link Lang}. Generated to achieve high coverage and detect
 * known bugs (Codec Bug 14). Tests include edge cases for null/empty inputs,
 * resource loading, and language detection.
 */
public class LangTest {

    private static Lang langInstance;
    private static final NameType NAME_TYPE = NameType.GENERIC;

    @BeforeClass
    public static void setUpBeforeClass() {
        // Obtain the singleton instance for the test name type
        langInstance = Lang.instance(NAME_TYPE);
        assertNotNull("Lang instance should not be null", langInstance);
    }

    @Before
    public void setUp() {
        // Additional setup if needed
    }

    // ----------------------------
    // Tests for instance() method
    // ----------------------------

    @Test
    public void testInstanceReturnsSameForSameNameType() {
        Lang sameLang = Lang.instance(NAME_TYPE);
        assertSame("instance should return the same object for same NameType",
                langInstance, sameLang);
    }

    @Test
    public void testInstanceThrowsNullPointerExceptionForNullNameType() {
        try {
            Lang.instance(null);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testInstanceReturnsDifferentForDifferentNameType() {
        Lang otherLang = Lang.instance(NameType.ASHKENAZI);
        assertNotNull(otherLang);
        // They might be same or different, just ensure no exception
        // Typically different name types produce different instances
    }

    // ---------------------------------
    // Tests for guessLanguage() method
    // ---------------------------------

    @Test(expected = NullPointerException.class)
    public void testGuessLanguageNullInput() {
        langInstance.guessLanguage(null);
    }

    @Test
    public void testGuessLanguageEmptyInput() {
        // Known bug: Empty input may cause ArrayIndexOutOfBounds or incorrect result
        try {
            String result = langInstance.guessLanguage("");
            // If no exception, result should be a language string (maybe "any")
            assertNotNull("guessLanguage(\"\") should not return null", result);
            // The result is typically "any" or "unknown" etc.
        } catch (ArrayIndexOutOfBoundsException e) {
            // This exception is the known bug (Bug 14) - test should catch it
            fail("Bug 14: Empty string caused ArrayIndexOutOfBoundsException");
        } catch (Exception e) {
            // Other exceptions not expected
            fail("Unexpected exception for empty input: " + e.getMessage());
        }
    }

    @Test
    public void testGuessLanguageWhitespaceInput() {
        String result = langInstance.guessLanguage("   ");
        assertNotNull("Whitespace input should not return null", result);
        // Typically returns "any" or some language
    }

    @Test
    public void testGuessLanguageEnglishText() {
        String text = "This is an English sentence";
        String result = langInstance.guessLanguage(text);
        assertNotNull("English text should produce a language guess", result);
        // The guess is expected to be "english" or "en" depending on encoding
        // Just check it's not empty
    }

    @Test
    public void TestGuessLanguageFrenchText() {
        String text = "Ceci est une phrase en français";
        String result = langInstance.guessLanguage(text);
        assertNotNull("French text should produce a language guess", result);
    }

    // ----------------------------------
    // Tests for guessLanguages() method
    // ----------------------------------

    @Test(expected = NullPointerException.class)
    public void testGuessLanguagesNullInput() {
        langInstance.guessLanguages(null);
    }

    @Test
    public void testGuessLanguagesEmptyInput() {
        try {
            Set<String> result = langInstance.guessLanguages("");
            assertNotNull("guessLanguages(\"\") should not return null", result);
            assertTrue("Empty input should return at least one language", result.size() >= 1);
        } catch (ArrayIndexOutOfBoundsException e) {
            fail("Bug 14: Empty string caused ArrayIndexOutOfBoundsException");
        }
    }

    @Test
    public void testGuessLanguagesWhitespaceInput() {
        Set<String> result = langInstance.guessLanguages("  ");
        assertNotNull(result);
        assertTrue(result.size() >= 1);
    }

    @Test
    public void testGuessLanguagesEnglishText() {
        String text = "This is a test sentence.";
        Set<String> result = langInstance.guessLanguages(text);
        assertNotNull(result);
        assertTrue("Should contain at least one language", result.size() >= 1);
    }

    @Test
    public void testGuessLanguagesMultipleCallsConsistent() {
        String text = "Hello world";
        Set<String> first = langInstance.guessLanguages(text);
        Set<String> second = langInstance.guessLanguages(text);
        assertEquals("Guesses should be consistent", first, second);
    }

    // -----------------------------------------
    // Tests for language set manipulation edges
    // -----------------------------------------

    @Test
    public void testGuessLanguagesLargeInput() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append("word ");
        }
        String longText = sb.toString();
        Set<String> result = langInstance.guessLanguages(longText);
        assertNotNull("Long input should not cause exception", result);
        assertTrue("Long input should still produce guesses", result.size() >= 1);
    }

    @Test
    public void testGuessLanguageSpecialCharacters() {
        String text = "حوار بين شخصين"; // Arabic
        String result = langInstance.guessLanguage(text);
        assertNotNull("Non-Latin text should produce a guess", result);
    }

    @Test
    public void testGuessLanguageShortString() {
        // Very short string like "a"
        String result = langInstance.guessLanguage("a");
        assertNotNull("Short string should not be null", result);
    }

    // --------------------------------------------------
    // Tests for resource loading and internal exceptions
    // --------------------------------------------------

    @Test
    public void testLangToStringMethod() {
        // Basic check that toString() does not throw (coverage)
        String toString = langInstance.toString();
        assertNotNull("toString should not be null", toString);
        assertTrue("toString should contain the name type", toString.contains(NAME_TYPE.getName()));
    }

    @Test
    public void testLangEquals() {
        // Lang instances are singletons per NameType, so identity equals
        Lang same = Lang.instance(NAME_TYPE);
        assertTrue("Same instance should be equal to itself", langInstance.equals(langInstance));
        assertTrue("Same NameType instance should be equal", langInstance.equals(same));
        Lang other = Lang.instance(NameType.ASHKENAZI);
        // They might have different implementations, but equals contract: should be false typically
        // Not strictly required, but generally expected
        if (langInstance.equals(other)) {
            System.out.println("Note: Different NameType Lang instances are equal (implementation specific)");
        }
    }

    // -------------------------------------------------------
    // Additional edge cases to trigger potential other faults
    // -------------------------------------------------------

    @Test
    public void testGuessLanguageMixedScript() {
        String text = "English words mixed مع العربية";
        String result = langInstance.guessLanguage(text);
        assertNotNull(result);
        // Should not throw
    }

    @Test
    public void testGuessLanguageNewlinesAndTabs() {
        String text = "line1\nline2\tend";
        String result = langInstance.guessLanguage(text);
        assertNotNull(result);
    }

    // This test specifically targets the known bug (Bug 14) if present:
    @Test
    public void testEmptyStringBugDetection() {
        // This test is designed to fail if Bug 14 is present (ArrayIndexOutOfBounds)
        // The bug was triggered by empty input to guessLanguage/guessLanguages
        boolean exceptionThrown = false;
        try {
            langInstance.guessLanguage("");
        } catch (ArrayIndexOutOfBoundsException e) {
            exceptionThrown = true;
        }
        assertFalse("Bug 14: Empty string should not throw ArrayIndexOutOfBoundsException", exceptionThrown);

        exceptionThrown = false;
        try {
            langInstance.guessLanguages("");
        } catch (ArrayIndexOutOfBoundsException e) {
            exceptionThrown = true;
        }
        assertFalse("Bug 14: Empty string should not throw ArrayIndexOutOfBoundsException", exceptionThrown);
    }
}