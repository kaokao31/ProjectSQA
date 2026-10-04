package org.apache.commons.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link org.apache.commons.lang.LocaleUtils}.
 */
public class LocaleUtilsTest {

    private static final Locale LOCALE_EN = new Locale("en", "");
    private static final Locale LOCALE_EN_US = new Locale("en", "US");
    private static final Locale LOCALE_EN_US_WIN = new Locale("en", "US", "WIN");
    private static final Locale LOCALE_EN_GB = new Locale("en", "GB");
    private static final Locale LOCALE_FR = new Locale("fr", "");
    private static final Locale LOCALE_FR_CA = new Locale("fr", "CA");
    private static final Locale LOCALE_FR_CH = new Locale("fr", "CH");
    private static final Locale LOCALE_DE = new Locale("de", "");
    private static final Locale LOCALE_DE_CH = new Locale("de", "CH");
    private static final Locale LOCALE_DE_AT = new Locale("de", "AT");
    private static final Locale LOCALE_IT = new Locale("it", "");
    private static final Locale LOCALE_IT_CH = new Locale("it", "CH");

    @Before
    public void setUp() throws Exception {
        // Warm up static collections to test initialization consistency
        LocaleUtils.isAvailableLocale(Locale.getDefault());
    }

    //-----------------------------------------------------------------------
    @Test
    public void testConstructor() {
        assertNotNull(new LocaleUtils());
        Constructor<?>[] cons = LocaleUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertTrue(Modifier.isPublic(cons[0].getModifiers()));
        assertTrue(Modifier.isPublic(LocaleUtils.class.getModifiers()));
        assertFalse(Modifier.isFinal(LocaleUtils.class.getModifiers()));
    }

    //-----------------------------------------------------------------------
    @Test
    public void testToLocale_1Part() {
        assertNull(LocaleUtils.toLocale(null));
        
        assertValidToLocale("us", "us", "");
        assertValidToLocale("fr", "fr", "");
        assertValidToLocale("de", "de", "");
        assertValidToLocale("zh", "zh", "");
        assertValidToLocale("qq", "qq", "");
        
        assertInvalidToLocale("Us");
        assertInvalidToLocale("uS");
        assertInvalidToLocale("u#");
        assertInvalidToLocale("u");
        assertInvalidToLocale("usa");
        assertInvalidToLocale("");
    }

    @Test
    public void testToLocale_2Part() {
        assertValidToLocale("us_EN", "us", "EN");
        assertValidToLocale("us_ZH", "us", "ZH");
        assertValidToLocale("fr_FR", "fr", "FR");
        assertValidToLocale("de_DE", "de", "DE");
        assertValidToLocale("fr_CA", "fr", "CA");
        
        assertInvalidToLocale("us-EN");
        assertInvalidToLocale("us_En");
        assertInvalidToLocale("us_eN");
        assertInvalidToLocale("us_en");
        assertInvalidToLocale("us_E#");
        assertInvalidToLocale("uS_EN");
        assertInvalidToLocale("us_E");
        assertInvalidToLocale("us_ENG");
        assertInvalidToLocale("us_   ");
        assertInvalidToLocale("us__");
    }

    @Test
    public void testToLocale_3Part() {
        assertValidToLocale("us_EN_A", "us", "EN", "A");
        assertValidToLocale("us_EN_a", "us", "EN", "a");
        assertValidToLocale("us_EN_SF", "us", "EN", "SF");
        assertValidToLocale("us_EN_POSIX", "us", "EN", "POSIX");
        assertValidToLocale("us_EN_WIN_NT", "us", "EN", "WIN_NT");
        assertValidToLocale("us_EN__", "us", "EN", "_");
        
        assertInvalidToLocale("us_EN-A");
        assertInvalidToLocale("us_EN_");
        assertInvalidToLocale("us_eN_A");
        assertInvalidToLocale("uS_EN_A");
        assertInvalidToLocale("us_E#_A");
    }

    private void assertValidToLocale(String localeString, String language, String country) {
        Locale locale = LocaleUtils.toLocale(localeString);
        assertNotNull("valid locale", locale);
        assertEquals(language, locale.getLanguage());
        assertEquals(country, locale.getCountry());
        assertEquals("", locale.getVariant());
    }

    private void assertValidToLocale(String localeString, String language, String country, String variant) {
        Locale locale = LocaleUtils.toLocale(localeString);
        assertNotNull("valid locale", locale);
        assertEquals(language, locale.getLanguage());
        assertEquals(country, locale.getCountry());
        assertEquals(variant, locale.getVariant());
    }

    private void assertInvalidToLocale(String localeString) {
        try {
            LocaleUtils.toLocale(localeString);
            fail("Expected IllegalArgumentException for: " + localeString);
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    //-----------------------------------------------------------------------
    @Test
    public void testLocaleLookupList_Locale() {
        assertLocaleLookupList(null, null, new Locale[0]);
        assertLocaleLookupList(LOCALE_EN, null, new Locale[]{LOCALE_EN});
        assertLocaleLookupList(LOCALE_EN_US, null, new Locale[]{LOCALE_EN_US, LOCALE_EN});
        assertLocaleLookupList(LOCALE_EN_US_WIN, null, new Locale[]{LOCALE_EN_US_WIN, LOCALE_EN_US, LOCALE_EN});
    }

    @Test
    public void testLocaleLookupList_LocaleLocale() {
        assertLocaleLookupList(LOCALE_EN, LOCALE_FR, new Locale[]{LOCALE_EN, LOCALE_FR});
        assertLocaleLookupList(LOCALE_EN_US, LOCALE_FR, new Locale[]{LOCALE_EN_US, LOCALE_EN, LOCALE_FR});
        assertLocaleLookupList(LOCALE_EN_US_WIN, LOCALE_FR, new Locale[]{LOCALE_EN_US_WIN, LOCALE_EN_US, LOCALE_EN, LOCALE_FR});
        
        assertLocaleLookupList(LOCALE_EN_US, LOCALE_EN, new Locale[]{LOCALE_EN_US, LOCALE_EN});
        assertLocaleLookupList(LOCALE_EN_US_WIN, LOCALE_EN_US, new Locale[]{LOCALE_EN_US_WIN, LOCALE_EN_US, LOCALE_EN});
        assertLocaleLookupList(LOCALE_EN_US_WIN, LOCALE_EN_US_WIN, new Locale[]{LOCALE_EN_US_WIN, LOCALE_EN_US, LOCALE_EN});
        
        assertLocaleLookupList(null, LOCALE_FR, new Locale[]{LOCALE_FR});
        assertLocaleLookupList(null, null, new Locale[0]);
    }

    private void assertLocaleLookupList(Locale locale, Locale defaultLocale, Locale[] expected) {
        List<Locale> list;
        if (defaultLocale == null) {
            list = LocaleUtils.localeLookupList(locale);
        } else {
            list = LocaleUtils.localeLookupList(locale, defaultLocale);
        }
        assertEquals(expected.length, list.size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], list.get(i));
        }
        try {
            list.add(Locale.GERMANY);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException ex) {
            // expected
        }
    }

    //-----------------------------------------------------------------------
    @Test
    public void testAvailableLocaleList() {
        List<Locale> list = LocaleUtils.availableLocaleList();
        List<Locale> list2 = LocaleUtils.availableLocaleList();
        assertNotNull(list);
        assertSame(list, list2);
        assertUnmodifiableCollection(list);
        
        Locale[] jdkLocaleArray = Locale.getAvailableLocales();
        List<Locale> jdkLocaleList = Arrays.asList(jdkLocaleArray);
        assertEquals(jdkLocaleList.size(), list.size());
        assertTrue(list.containsAll(jdkLocaleList));
    }

    @Test
    public void testAvailableLocaleSet() {
        Set<Locale> set = LocaleUtils.availableLocaleSet();
        Set<Locale> set2 = LocaleUtils.availableLocaleSet();
        assertNotNull(set);
        assertSame(set, set2);
        assertUnmodifiableCollection(set);
        
        Locale[] jdkLocaleArray = Locale.getAvailableLocales();
        List<Locale> jdkLocaleList = Arrays.asList(jdkLocaleArray);
        assertEquals(jdkLocaleList.size(), set.size());
        assertTrue(set.containsAll(jdkLocaleList));
    }

    @Test
    public void testIsAvailableLocale() {
        Set<Locale> set = LocaleUtils.availableLocaleSet();
        for (Locale locale : set) {
            assertTrue(LocaleUtils.isAvailableLocale(locale));
        }
        assertFalse(LocaleUtils.isAvailableLocale(new Locale("xx", "YY", "zz")));
        assertFalse(LocaleUtils.isAvailableLocale(new Locale("zzz", "ZZZ")));
        assertFalse(LocaleUtils.isAvailableLocale(null));
    }

    //-----------------------------------------------------------------------
    @Test
    public void testLanguagesByCountry() {
        assertLanguageByCountry(null, new String[0]);
        assertLanguageByCountry("US", new String[]{"en"});
        assertLanguageByCountry("GB", new String[]{"en"});
        assertLanguageByCountry("CH", new String[]{"de", "fr", "it"});
        assertLanguageByCountry("ZZ", new String[0]);
        
        List<Locale> list = LocaleUtils.languagesByCountry("US");
        assertUnmodifiableCollection(list);
        
        List<Locale> list2 = LocaleUtils.languagesByCountry("US");
        assertSame(list, list2);
    }

    private void assertLanguageByCountry(String country, String[] languages) {
        List<Locale> list = LocaleUtils.languagesByCountry(country);
        if (languages.length == 0) {
            assertTrue(list.isEmpty());
        } else {
            Set<String> set = new HashSet<String>();
            for (Locale locale : list) {
                set.add(locale.getLanguage());
                assertEquals(country, locale.getCountry());
                assertEquals("", locale.getVariant());
            }
            for (String language : languages) {
                assertTrue("Expected " + language + " in languagesByCountry(" + country + ")", set.contains(language));
            }
        }
    }

    //-----------------------------------------------------------------------
    @Test
    public void testCountriesByLanguage() {
        assertCountriesByLanguage(null, new String[0]);
        assertCountriesByLanguage("en", new String[]{"US", "GB"});
        assertCountriesByLanguage("fr", new String[]{"FR", "CA", "CH"});
        assertCountriesByLanguage("de", new String[]{"DE", "AT", "CH"});
        assertCountriesByLanguage("it", new String[]{"IT", "CH"});
        assertCountriesByLanguage("zz", new String[0]);
        
        List<Locale> list = LocaleUtils.countriesByLanguage("en");
        assertUnmodifiableCollection(list);
        
        List<Locale> list2 = LocaleUtils.countriesByLanguage("en");
        assertSame(list, list2);
    }

    private void assertCountriesByLanguage(String language, String[] countries) {
        List<Locale> list = LocaleUtils.countriesByLanguage(language);
        if (countries.length == 0) {
            assertTrue(list.isEmpty());
        } else {
            Set<String> set = new HashSet<String>();
            for (Locale locale : list) {
                set.add(locale.getCountry());
                assertEquals(language, locale.getLanguage());
                assertEquals("", locale.getVariant());
            }
            for (String country : countries) {
                assertTrue("Expected " + country + " in countriesByLanguage(" + language + ")", set.contains(country));
            }
        }
    }

    //-----------------------------------------------------------------------
    private static void assertUnmodifiableCollection(Collection<?> coll) {
        try {
            coll.clear();
            fail("Collection is not unmodifiable (clear succeeded)");
        } catch (UnsupportedOperationException ex) {
            // success
        }
        try {
            coll.remove(null);
            fail("Collection is not unmodifiable (remove succeeded)");
        } catch (UnsupportedOperationException ex) {
            // success
        }
        Iterator<?> it = coll.iterator();
        if (it.hasNext()) {
            it.next();
            try {
                it.remove();
                fail("Collection iterator is not unmodifiable (remove succeeded)");
            } catch (UnsupportedOperationException ex) {
                // success
            }
        }
    }
}