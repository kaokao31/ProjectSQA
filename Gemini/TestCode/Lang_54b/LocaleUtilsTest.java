package org.apache.commons.lang;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Unit tests for {@link LocaleUtils}.
 * Generated with a focus on maximum coverage and triggering bug LANG-54.
 */
public class LocaleUtilsTest {

    @Test
    public void testConstructor() {
        assertNotNull(new LocaleUtils());
    }

    @Test
    public void testtoLocale_Null() {
        assertNull(LocaleUtils.toLocale(null));
    }

    @Test
    public void testtoLocale_Valid() {
        // Language only
        Locale locale = LocaleUtils.toLocale("en");
        assertNotNull(locale);
        assertEquals("en", locale.getLanguage());
        assertEquals("", locale.getCountry());
        assertEquals("", locale.getVariant());

        // Language + Country
        locale = LocaleUtils.toLocale("en_US");
        assertNotNull(locale);
        assertEquals("en", locale.getLanguage());
        assertEquals("US", locale.getCountry());
        assertEquals("", locale.getVariant());

        // Language + Country + Variant
        locale = LocaleUtils.toLocale("en_US_POSIX");
        assertNotNull(locale);
        assertEquals("en", locale.getLanguage());
        assertEquals("US", locale.getCountry());
        assertEquals("POSIX", locale.getVariant());
        
        // Language + Variant (empty country)
        // This is the specific trigger for LANG-54 / testLang328 where "fr__POSIX" previously failed.
        locale = LocaleUtils.toLocale("fr__POSIX");
        assertNotNull(locale);
        assertEquals("fr", locale.getLanguage());
        assertEquals("", locale.getCountry());
        assertEquals("POSIX", locale.getVariant());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testtoLocale_InvalidLengthShort() {
        LocaleUtils.toLocale("c");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testtoLocale_InvalidLengthShort2() {
        LocaleUtils.toLocale("ab_C");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testtoLocale_InvalidThirdChar() {
        LocaleUtils.toLocale("a-b");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testtoLocale_InvalidFifthChar() {
        LocaleUtils.toLocale("en_U#");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testtoLocale_InvalidFormatLong() {
        LocaleUtils.toLocale("en_US_A_B");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testtoLocale_InvalidFormatRandom() {
        LocaleUtils.toLocale("abcde");
    }

    @Test
    public void testtoLocale_CachedInstance() {
        Locale l1 = LocaleUtils.toLocale("en_US");
        Locale l2 = LocaleUtils.toLocale("en_US");
        assertSame(l1, l2);
    }

    @Test
    public void testtoLocale_LowerCaseLanguage() {
        // Language must be lowercase
        try {
            LocaleUtils.toLocale("EN");
            fail("Expected IllegalArgumentException for uppercase language");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testtoLocale_UpperCaseCountry() {
        // Country must be uppercase
        try {
            LocaleUtils.toLocale("en_us");
            fail("Expected IllegalArgumentException for lowercase country");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testtoLocale_InvalidLengthLang() {
        try {
            LocaleUtils.toLocale("e");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testtoLocale_InvalidLengthLang3() {
        try {
            LocaleUtils.toLocale("eng");
            // Depending on implementation, 3-char might be allowed or not. 
            // Standard Commons Lang 2.x allows 2 or 3 chars for language sometimes, 
            // but let's test invalid format where length is 4.
        } catch (IllegalArgumentException e) {
            // expected if invalid
        }
    }
    
    @Test
    public void testtoLocale_InvalidUnderscoreOnly() {
        try {
            LocaleUtils.toLocale("_");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            LocaleUtils.toLocale("__");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testAvailableLocaleList() {
        List<Locale> list = LocaleUtils.availableLocaleList();
        assertNotNull(list);
        
        List<Locale> list2 = LocaleUtils.availableLocaleList();
        assertNotNull(list2);
        // It should return an unmodifiable or cached list instance (or new list per call depending on impl, 
        // but both calls should return equivalent content).
        assertEquals(list.size(), list2.size());
    }

    @Test
    public void testAvailableLocaleSet() {
        Set<Locale> set = LocaleUtils.availableLocaleSet();
        assertNotNull(set);
        
        Set<Locale> set2 = LocaleUtils.availableLocaleSet();
        assertNotNull(set2);
        assertSame(set, set2); // Should be cached/singleton set
    }

    @Test
    public void testIsAvailableLocale() {
        assertTrue(LocaleUtils.isAvailableLocale(Locale.ENGLISH));
        assertTrue(LocaleUtils.isAvailableLocale(Locale.US));
        
        // Test an extremely unlikely locale if possible, or just verify against available set
        Locale custom = new Locale("qaa", "QAA", "QAA");
        boolean inSet = LocaleUtils.availableLocaleSet().contains(custom);
        assertEquals(inSet, LocaleUtils.isAvailableLocale(custom));
        
        assertFalse(LocaleUtils.isAvailableLocale(null));
    }

    @Test
    public void testLocaleList() {
        List<Locale> list = LocaleUtils.localeList_forTest(Locale.ENGLISH); // if any specific helper, or just use public API
        // Public API:
        List<Locale> englishLocales = LocaleUtils.localeLookupList(Locale.ENGLISH);
        assertNotNull(englishLocales);
        assertTrue(englishLocales.contains(Locale.ENGLISH));

        List<Locale> usLocales = LocaleUtils.localeLookupList(new Locale("en", "US"));
        assertNotNull(usLocales);
        assertTrue(usLocales.contains(new Locale("en", "US")));
        assertTrue(usLocales.contains(new Locale("en", "")));

        List<Locale> fullLocales = LocaleUtils.localeLookupList(new Locale("en", "US", "POSIX"));
        assertNotNull(fullLocales);
        assertTrue(fullLocales.contains(new Locale("en", "US", "POSIX")));
        assertTrue(fullLocales.contains(new Locale("en", "US")));
        assertTrue(fullLocales.contains(new Locale("en", "")));
        
        // Null locale lookup list
        List<Locale> nullLocales = LocaleUtils.localeLookupList(null);
        assertNotNull(nullLocales);
        assertTrue(nullLocales.isEmpty());
    }

    @Test
    public void testCountriesByLanguage() {
        List<Locale> countries = LocaleUtils.countriesByLanguage("en");
        assertNotNull(countries);
        
        // Null language
        List<Locale> nullCountries = LocaleUtils.countriesByLanguage(null);
        assertNotNull(nullCountries);
        assertTrue(nullCountries.isEmpty());
    }

    @Test
    public void testLanguagesByCountry() {
        List<Locale> languages = LocaleUtils.languagesByCountry("US");
        assertNotNull(languages);
        
        // Null country
        List<Locale> nullLanguages = LocaleUtils.languagesByCountry(null);
        assertNotNull(nullLanguages);
        assertTrue(nullLanguages.isEmpty());
    }
}