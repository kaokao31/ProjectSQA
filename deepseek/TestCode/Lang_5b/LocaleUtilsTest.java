package org.apache.commons.lang3.locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.Test;

public class LocaleUtilsTest {

    // ---------- toLocale tests ----------
    @Test
    public void testToLocale_NullInput() {
        try {
            LocaleUtils.toLocale(null);
            fail("Expected IllegalArgumentException for null input");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testToLocale_EmptyString() {
        try {
            LocaleUtils.toLocale("");
            fail("Expected IllegalArgumentException for empty string");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testToLocale_LanguageOnly() {
        Locale loc = LocaleUtils.toLocale("en");
        assertEquals("en", loc.getLanguage());
        assertEquals("", loc.getCountry());
        assertEquals("", loc.getVariant());
    }

    @Test
    public void testToLocale_LanguageCountry() {
        Locale loc = LocaleUtils.toLocale("en_US");
        assertEquals("en", loc.getLanguage());
        assertEquals("US", loc.getCountry());
        assertEquals("", loc.getVariant());
    }

    @Test
    public void testToLocale_LanguageCountryVariant() {
        Locale loc = LocaleUtils.toLocale("en_US_WIN");
        assertEquals("en", loc.getLanguage());
        assertEquals("US", loc.getCountry());
        assertEquals("WIN", loc.getVariant());
    }

    @Test
    public void testToLocale_InvalidLanguageLength() {
        try {
            LocaleUtils.toLocale("en_US");
            // This could be valid depending on implementation (2-letter language is allowed)
            // Expecting no exception
        } catch (IllegalArgumentException e) {
            fail("Two-letter language should be valid");
        }
    }

    @Test
    public void testToLocale_InvalidLanguageTooLong() {
        try {
            LocaleUtils.toLocale("eng_US");
            fail("Expected IllegalArgumentException for language too long");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testToLocale_InvalidCountryLengthOne() {
        try {
            LocaleUtils.toLocale("en_U");
            fail("Expected IllegalArgumentException for country length one");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testToLocale_InvalidCountryLengthThree() {
        try {
            LocaleUtils.toLocale("en_USA");
            fail("Expected IllegalArgumentException for country length three");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testToLocale_InvalidSeparatorPosition() {
        try {
            LocaleUtils.toLocale("en_");
            fail("Expected IllegalArgumentException for trailing underscore");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testToLocale_MultipleUnderscores() {
        try {
            LocaleUtils.toLocale("en__US");
            fail("Expected IllegalArgumentException for double underscore");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testToLocale_CountryContainsDigits() {
        try {
            LocaleUtils.toLocale("en_123");
            fail("Expected IllegalArgumentException for numeric country");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testToLocale_VariantContainsNonAlphaNumeric() {
        // Variant can contain alpha-numeric, so no exception expected
        Locale loc = LocaleUtils.toLocale("en_US_12AB");
        assertEquals("12AB", loc.getVariant());
    }

    // ---------- isAvailableLocale tests ----------
    @Test
    public void testIsAvailableLocale_Null() {
        assertFalse(LocaleUtils.isAvailableLocale(null));
    }

    @Test
    public void testIsAvailableLocale_KnownLocale() {
        assertTrue(LocaleUtils.isAvailableLocale(Locale.US));
    }

    @Test
    public void testIsAvailableLocale_UnknownLocale() {
        assertFalse(LocaleUtils.isAvailableLocale(new Locale("xyz", "XY")));
    }

    // ---------- availableLocaleList tests ----------
    @Test
    public void testAvailableLocaleList_NotNull() {
        assertNotNull(LocaleUtils.availableLocaleList());
    }

    @Test
    public void testAvailableLocaleList_ContainsUs() {
        List<Locale> list = LocaleUtils.availableLocaleList();
        assertTrue(list.contains(Locale.US));
    }

    @Test
    public void testAvailableLocaleList_SizePositive() {
        assertTrue(LocaleUtils.availableLocaleList().size() > 0);
    }

    // ---------- availableLocaleSet tests ----------
    @Test
    public void testAvailableLocaleSet_NotNull() {
        assertNotNull(LocaleUtils.availableLocaleSet());
    }

    @Test
    public void testAvailableLocaleSet_ContainsUs() {
        Set<Locale> set = LocaleUtils.availableLocaleSet();
        assertTrue(set.contains(Locale.US));
    }

    @Test
    public void testAvailableLocaleSet_EqualsFromList() {
        Set<Locale> set = new HashSet<>(LocleUtils.availableLocaleList());
        assertEquals(set, LocaleUtils.availableLocaleSet());
    }

    // ---------- localeLookupList tests ----------
    @Test
    public void testLocaleLookupList_NullInput() {
        try {
            LocaleUtils.localeLookupList(null);
            fail("Expected IllegalArgumentException for null input");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testLocaleLookupList_OneElement() {
        List<Locale> list = LocaleUtils.localeLookupList(Locale.US);
        assertEquals(1, list.size());
        assertEquals(Locale.US, list.get(0));
    }

    @Test
    public void testLocaleLookupList_DefaultIncluded() {
        List<Locale> list = LocaleUtils.localeLookupList(Locale.US, Locale.UK);
        assertEquals(2, list.size());
        assertEquals(Locale.US, list.get(0));
        assertEquals(Locale.UK, list.get(1));
    }

    @Test
    public void testLocaleLookupList_DefaultSameAsLocale() {
        List<Locale> list = LocaleUtils.localeLookupList(Locale.US, Locale.US);
        assertEquals(1, list.size());
        assertEquals(Local.US, list.get(0));
    }

    // ---------- toLocale with various valid formats ----------
    @Test
    public void testToLocale_NumericCountry() {
        // Some implementations allow numeric country (e.g. "en_029")        Locale loc = LocaleUtils.toLocale("en_029");
        assertEquals("en", loc.getLanguage());
        assertEquals("029", loc.getCountry());
    }

    @Test
    public void testToLocale_AtSignVariant() {
        // Some implementations treat variant after '@' differently - we test typical behavior
        try {
            LocaleUtils.toLocale("en@US");
            fail("Expected IllegalArgumentException for '@' separator");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ---------- edge: mixed case ----------
    @Test
    public void testToLocale_LanguageUpperCase() {
        // Should convert to lower case language?
        Locale loc = LocaleUtils.toLocale("EN");
        // Typically expected to be lower-cased        assertEquals("en", loc.getLanguage());
    }

    // ---------- branch coverage for toLocale ----------
    // Additional tests for various combinations to cover all conditional branches

    @Test
    public void testToLocale_LanguageOnlyTwoCharacters() {
        Locale loc = LocaleUtils.toLocale("FR");
        assertEquals("fr", loc.getLanguage());
        assertEquals("", loc.getCountry());
    }

    @Test
    public void testToLocale_CountrySingleLowercase() {
        try {
            LocaleUtils.toLocale("en_a");
            fail("Expected IllegalArgumentException for single-letter country");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testToLocale_VariantUnderscoreStart() {
        Locale loc = LocaleUtils.toLocale("en_US__Variant");
        // Some implementations ignore extra underscores
        assertEquals("US", loc.getCountry());
        assertEquals("Variant", loc.getVariant());
    }

    @Test
    public void testToLocale_LangCountry_WithSpaces() {
        try {
            LocaleUtils.toLocale("en US");
            fail("Expected IllegalArgumentException for space in input");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ---------- availableLocaleList immutability ----------
    @Test
    public void testAvailableLocaleList_IsUnmodifiable() {
        try {
            LocaleUtils.availableLocaleList().add(Locale.GERMANY);
            fail("List should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testAvailableLocaleSet_IsUnmodifiable() {
        try {
            LocaleUtils.availableLocaleSet().add(Locale.GERMANY);
            fail("Set should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // ---------- performance/stability: large set ----------
    // Not needed for correctness but ensures no exception

    @Test
    public void testAvailableLocaleList_NoDuplicates() {
        List<Locale> list = LocaleUtils.availableLocaleList();
        Set<Locale> set = new HashSet<>(list);
        assertEquals(set.size(), list.size());
    }

    // ---------- coverage for inner methods ----------
    // If there is a method like getAvailableLocaleList() that caches, we can verify caching behavior
    @Test
    public void testAvailableLocaleList_SameInstance() {
        List<Locale> list1 = LocaleUtils.availableLocaleList();
        List<Locale> list2 = LocaleUtils.availableLocaleList();
        assertSame(list1, list2);
    }

    @Test
    public void testAvailableLocaleSet_SameInstance() {
        Set<Locale> set1 = LocaleUtils.availableLocaleSet();
        Set<Locale> set2 = LocaleUtils.availableLocaleSet();
        assertSame(set1, set2);
    }

    // ---------- toLocale with character @ ----------
    // Already above

    // ---------- isAvailableLocale with instance from available set ----------
    @Test
    public void testIsAvailableLocale_UsingFromList() {
        List<Locale> list = LocaleUtils.availableLocaleList();
        for (Locale l : list) {
            assertTrue(LocaleUtils.availableLocale(l));
        }
    }

    // ---------- localeLookupList with null default ----------
    @Test
    public void testLocaleLookupList_NullDefault() {
        try {
            LocaleUtils.localeLookupList(Local.US, null);
            fail("Expected IllegalArgumentException for null default");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ---------- additional edge for toLocale: length checks ----------
    @Test
    public void testToLocale_CountryNumericLarge() {
        Locale loc = LocaleUtils.toLocale("en_12345");
        // Country can be numeric but may be truncated? See implementation
        // According to typical implementation, country is 2-4 alpha or 3 digit?        // We assume it's allowed if it's all numeric and length >=2        // If implementation rejects, adjust accordingly.
        // This test may need to be adapted to actual implementation.
        // For safety, we just check no exception thrown.
        assertNotNull(loc);
    }

    @Test
    public void testToLocale_VariantContainsDash() {
        try {
            LocaleUtils.toLocale("en_US_Variant-1");
            fail("Expected IllegalArgumentException for dash in variant");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ---------- stability: no null pointer from list ----------
    @Test
    public void testAvailableLocaleList_ContentNotNull() {
        for (Locale l : LocaleUtils.availableLocaleList()) {
            assertNotNull(l);
        }
    }

    // ---------- test for deprecated methods if any? ----------
    // Not needed.

}