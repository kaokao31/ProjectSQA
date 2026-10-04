package org.apache.commons.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.Test;

/**
 * Test suite for LocaleUtils.
 * Designed to achieve high coverage and detect the NullPointerException bug (Defects4J bug 57).
 */
public class LocaleUtilsTest {

    // --- Constructor test ---
    @Test
    public void testConstructor() throws Exception {
        // Verify that the constructor is private and cannot be instantiated
        Constructor<LocaleUtils> constructor = LocaleUtils.class.getDeclaredConstructor();
        assertTrue("Constructor should be private", Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
            fail("Expected IllegalAccessException or InvocationTargetException");
        } catch (Exception e) {
            // Expected: either IllegalAccessException (if setAccessible not allowed) or InvocationTargetException
            assertTrue(e instanceof IllegalAccessException || e instanceof java.lang.reflect.InvocationTargetException);
        }
    }

    // --- toLocale tests ---
    @Test
    public void testToLocale_1Part() {
        // Valid 2-letter language
        Locale result = LocaleUtils.toLocale("en");
        assertEquals("en", result.getLanguage());
        assertNull(result.getCountry());
        assertNull(result.getVariant());

        // Valid 2-letter language (uppercase)
        result = LocaleUtils.toLocale("FR");
        assertEquals("fr", result.getLanguage()); // Locale normalizes to lowercase
        assertNull(result.getCountry());

        // Invalid: empty string
        try {
            LocaleUtils.toLocale("");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        // Invalid: null input
        try {
            LocaleUtils.toLocale(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        // Invalid: too short (1 char)
        try {
            LocaleUtils.toLocale("a");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        // Invalid: 3 chars (should be 2 or 5 or 8+)
        try {
            LocaleUtils.toLocale("abc");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testToLocale_2Part() {
        // Valid language_country
        Locale result = LocaleUtils.toLocale("en_US");
        assertEquals("en", result.getLanguage());
        assertEquals("US", result.getCountry());
        assertNull(result.getVariant());

        // Valid with lowercase country (should be normalized to uppercase)
        result = LocaleUtils.toLocale("en_us");
        assertEquals("en", result.getLanguage());
        assertEquals("US", result.getCountry());

        // Invalid: missing underscore
        try {
            LocaleUtils.toLocale("enUS");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        // Invalid: underscore at wrong position
        try {
            LocaleUtils.toLocale("en_U");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        // Invalid: null
        try {
            LocaleUtils.toLocale(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        // Invalid: empty string
        try {
            LocaleUtils.toLocale("");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testToLocale_3Part() {
        // Valid language_country_variant
        Locale result = LocaleUtils.toLocale("en_US_WIN");
        assertEquals("en", result.getLanguage());
        assertEquals("US", result.getCountry());
        assertEquals("WIN", result.getVariant());

        // Valid with variant containing underscore (should be treated as part of variant)
        result = LocaleUtils.toLocale("en_US_WIN_XP");
        assertEquals("en", result.getLanguage());
        assertEquals("US", result.getCountry());
        assertEquals("WIN_XP", result.getVariant());

        // Invalid: missing country part (only language and variant)
        try {
            LocaleUtils.toLocale("en__WIN");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        // Invalid: null
        try {
            LocaleUtils.toLocale(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        // Invalid: too many parts (more than 3 underscores)
        try {
            LocaleUtils.toLocale("en_US_WIN_XP_EXTRA");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // --- availableLocaleSet tests ---
    @Test
    public void testAvailableLocaleSet() {
        Set<Locale> set = LocaleUtils.availableLocaleSet();
        assertNotNull("availableLocaleSet should not be null", set);
        assertFalse("availableLocaleSet should not be empty", set.isEmpty());
        // Check that it contains common locales
        assertTrue(set.contains(Locale.US));
        assertTrue(set.contains(Locale.UK));
        assertTrue(set.contains(Locale.FRANCE));
        // Check that it is unmodifiable
        try {
            set.add(Locale.CANADA);
            fail("Set should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // --- isAvailableLocale tests ---
    @Test
    public void testIsAvailableLocale() {
        assertTrue(LocaleUtils.isAvailableLocale(Locale.US));
        assertTrue(LocaleUtils.isAvailableLocale(Locale.GERMANY));
        assertFalse(LocaleUtils.isAvailableLocale(new Locale("xx", "YY")));
        // Null input
        try {
            LocaleUtils.isAvailableLocale(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected due to bug? Actually the bug causes NPE in many methods.
            // We expect NPE for null input (consistent with bug)
        }
    }

    // --- availableLocaleList tests ---
    @Test
    public void testAvailableLocaleList() {
        List<Locale> list = LocaleUtils.availableLocaleList();
        assertNotNull("availableLocaleList should not be null", list);
        assertFalse("availableLocaleList should not be empty", list.isEmpty());
        // Check that it contains common locales
        assertTrue(list.contains(Locale.US));
        assertTrue(list.contains(Locale.UK));
        // Check that it is unmodifiable
        try {
            list.add(Locale.CANADA);
            fail("List should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // --- countriesByLanguage tests ---
    @Test
    public void testCountriesByLanguage() {
        // Valid language
        List<Locale> countries = LocaleUtils.countriesByLanguage("en");
        assertNotNull(countries);
        assertTrue(countries.contains(Locale.US));
        assertTrue(countries.contains(Locale.UK));
        assertTrue(countries.contains(Locale.CANADA));
        // Language with no countries
        countries = LocaleUtils.countriesByLanguage("xx");
        assertNotNull(countries);
        assertTrue(countries.isEmpty());
        // Null input
        try {
            LocaleUtils.countriesByLanguage(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        // Empty string
        countries = LocaleUtils.countriesByLanguage("");
        assertNotNull(countries);
        assertTrue(countries.isEmpty());
    }

    // --- languagesByCountry tests ---
    @Test
    public void testLanguagesByCountry() {
        // Valid country
        List<Locale> languages = LocaleUtils.languagesByCountry("US");
        assertNotNull(languages);
        assertTrue(languages.contains(Locale.ENGLISH));
        // Country with multiple languages (e.g., CA)
        languages = LocaleUtils.languagesByCountry("CA");
        assertNotNull(languages);
        assertTrue(languages.contains(Locale.ENGLISH));
        assertTrue(languages.contains(Locale.FRENCH));
        // Invalid country code
        languages = LocaleUtils.languagesByCountry("ZZ");
        assertNotNull(languages);
        assertTrue(languages.isEmpty());
        // Null input
        try {
            LocaleUtils.languagesByCountry(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        // Empty string
        languages = LocaleUtils.languagesByCountry("");
        assertNotNull(languages);
        assertTrue(languages.isEmpty());
    }

    // --- localeLookupList (single argument) tests ---
    @Test
    public void testLocaleLookupList_Locale() {
        // Valid locale
        List<Locale> list = LocaleUtils.localeLookupList(Locale.US);
        assertNotNull(list);
        assertTrue(list.size() >= 1);
        assertEquals(Locale.US, list.get(0));
        // Null input
        try {
            LocaleUtils.localeLookupList(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    // --- localeLookupList (two arguments) tests ---
    @Test
    public void testLocaleLookupList_LocaleLocale() {
        // Both non-null
        List<Locale> list = LocaleUtils.localeLookupList(Locale.US, Locale.UK);
        assertNotNull(list);
        assertTrue(list.size() >= 1);
        assertEquals(Locale.US, list.get(0));
        // First null, second non-null
        list = LocaleUtils.localeLookupList(null, Locale.UK);
        assertNotNull(list);
        assertTrue(list.size() >= 1);
        assertEquals(Locale.UK, list.get(0));
        // Both null
        try {
            LocaleUtils.localeLookupList(null, null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        // First non-null, second null
        list = LocaleUtils.localeLookupList(Locale.US, null);
        assertNotNull(list);
        assertTrue(list.size() >= 1);
        assertEquals(Locale.US, list.get(0));
    }

    // --- Additional edge cases for coverage ---
    @Test
    public void testToLocale_EdgeCases() {
        // Language with underscore but no country (invalid)
        try {
            LocaleUtils.toLocale("en_");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        // Variant with only one character after second underscore
        try {
            LocaleUtils.toLocale("en_US_A");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        // Valid 3-part with variant containing underscore
        Locale result = LocaleUtils.toLocale("en_US_WIN_XP");
        assertEquals("en", result.getLanguage());
        assertEquals("US", result.getCountry());
        assertEquals("WIN_XP", result.getVariant());
    }

    @Test
    public void testIsAvailableLocale_EdgeCases() {
        // Locale with null fields? Not possible via constructor, but we can test with a locale that has null country
        Locale localeWithNullCountry = new Locale("en", null);
        // This should be handled gracefully
        assertFalse(LocaleUtils.isAvailableLocale(localeWithNullCountry));
    }

    @Test
    public void testAvailableLocaleSet_Consistency() {
        Set<Locale> set = LocaleUtils.availableLocaleSet();
        List<Locale> list = LocaleUtils.availableLocaleList();
        assertEquals("Set and List should have same elements", new HashSet<Locale>(list), set);
    }

    @Test
    public void testCountriesByLanguage_EdgeCases() {
        // Language with multiple countries
        List<Locale> countries = LocaleUtils.countriesByLanguage("en");
        assertTrue(countries.contains(Locale.US));
        assertTrue(countries.contains(Locale.GB));
        // Language with no countries
        countries = LocaleUtils.countriesByLanguage("zz");
        assertTrue(countries.isEmpty());
    }

    @Test
    public void testLanguagesByCountry_EdgeCases() {
        // Country with multiple languages
        List<Locale> languages = LocaleUtils.languagesByCountry("CH");
        assertNotNull(languages);
        // Switzerland has multiple official languages
        assertTrue(languages.contains(new Locale("de")));
        assertTrue(languages.contains(new Locale("fr")));
        assertTrue(languages.contains(new Locale("it")));
        // Country with no languages
        languages = LocaleUtils.languagesByCountry("ZZ");
        assertTrue(languages.isEmpty());
    }

    @Test
    public void testLocaleLookupList_EdgeCases() {
        // Locale with variant
        Locale localeWithVariant = new Locale("en", "US", "WIN");
        List<Locale> list = LocaleUtils.localeLookupList(localeWithVariant);
        assertNotNull(list);
        assertTrue(list.size() >= 1);
        assertEquals(localeWithVariant, list.get(0));
        // Locale with only language
        Locale languageOnly = new Locale("en");
        list = LocaleUtils.localeLookupList(languageOnly);
        assertNotNull(list);
        assertTrue(list.size() >= 1);
        assertEquals(languageOnly, list.get(0));
    }

    @Test
    public void testToLocale_NullInputs() {
        // Ensure that null input throws IllegalArgumentException (as per spec)
        try {
            LocaleUtils.toLocale(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testAvailableLocaleSet_NullSafety() {
        // The set should never be null
        assertNotNull(LocaleUtils.availableLocaleSet());
    }

    @Test
    public void testAvailableLocaleList_NullSafety() {
        assertNotNull(LocaleUtils.availableLocaleList());
    }
}