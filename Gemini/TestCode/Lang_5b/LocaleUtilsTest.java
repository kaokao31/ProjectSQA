package org.apache.commons.lang3;

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
 * Unit tests for {@link org.apache.commons.lang3.LocaleUtils}.
 */
public class LocaleUtilsTest {

    private static final Locale LOCALE_EN = new Locale("en", "");
    private static final Locale LOCALE_EN_US = new Locale("en", "US");
    private static final Locale LOCALE_EN_US_WIN = new Locale("en", "US", "WIN");
    private static final Locale LOCALE_EN_WIN = new Locale("en", "", "WIN");
    private static final Locale LOCALE_FR = new Locale("fr", "");
    private static final Locale LOCALE_FR_CA = new Locale("fr", "CA");
    private static final Locale LOCALE_QQ = new Locale("qq", "");
    private static final Locale LOCALE_QQ_ZZ = new Locale("qq", "ZZ");

    @Before
    public void setUp() throws Exception {
        // Warm up / initialize to ensure clean state
        LocaleUtils.isAvailableLocale(Locale.getDefault());
    }

    // -----------------------------------------------------------------------
    @Test
    public void testConstructor() {
        assertNotNull(new LocaleUtils());
        Constructor<?>[] cons = LocaleUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertTrue(Modifier.isPublic(cons[0].getModifiers()));
        assertTrue(Modifier.isPublic(LocaleUtils.class.getModifiers()));
        assertFalse(Modifier.isFinal(LocaleUtils.class.getModifiers()));
    }

    // -----------------------------------------------------------------------
    @Test
    public void testToLocale_null() {
        assertNull(LocaleUtils.toLocale(null));
    }

    @Test
    public void testToLocale_LanguageOnly() {
        assertValidToLocale("us", "us", "");
        assertValidToLocale("fr", "fr", "");
        assertValidToLocale("de", "de", "");
        assertValidToLocale("zh", "zh", "");
        assertValidToLocale("qq", "qq", "");

        try {
            LocaleUtils.toLocale("Us");
            fail("Should fail if not lowercase");
        } catch (final IllegalArgumentException iae) {
        }
        try {
            LocaleUtils.toLocale("US");
            fail("Should fail if not lowercase");
        } catch (final IllegalArgumentException iae) {
        }
        try {
            LocaleUtils.toLocale("uS");
            fail("Should fail if not lowercase");
        } catch (final IllegalArgumentException iae) {
        }
        try {
            LocaleUtils.toLocale("u#");
            fail("Should fail if not lowercase");
        } catch (final IllegalArgumentException iae) {
        }
        try {
            LocaleUtils.toLocale("#s");
            fail("Should fail if not lowercase");
        } catch (final IllegalArgumentException iae) {
        }
    }

    @Test
    public void testToLocale_LanguageAndCountry() {
        assertValidToLocale("us_EN", "us", "EN");
        assertValidToLocale("us_ZH", "us", "ZH");
        assertValidToLocale("fr_FR", "fr", "FR");
        assertValidToLocale("de_DE", "de", "DE");
        assertValidToLocale("qq_ZZ", "qq", "ZZ");

        try {
            LocaleUtils.toLocale("us-EN");
            fail("Should fail if not underscore separated");
        } catch (final IllegalArgumentException iae) {
        }
        try {
            LocaleUtils.toLocale("us_En");
            fail("Should fail if not uppercase country");
        } catch (final IllegalArgumentException iae) {
        }
        try {
            LocaleUtils.toLocale("us_en");
            fail("Should fail if not uppercase country");
        } catch (final IllegalArgumentException iae) {
        }
        try {
            LocaleUtils.toLocale("us_eN");
            fail("Should fail if not uppercase country");
        } catch (final IllegalArgumentException iae) {
        }
        try {
            LocaleUtils.toLocale("uS_EN");
            fail("Should fail if not lowercase language");
        } catch (final IllegalArgumentException iae) {
        }
        try {
            LocaleUtils.toLocale("us_E#");
            fail("Should fail if country not letters");
        } catch (final IllegalArgumentException iae) {
        }
        try {
            LocaleUtils.toLocale("us_#N");
            fail("Should fail if country not letters");
        } catch (final IllegalArgumentException iae) {
        }
    }

    @Test
    public void testToLocale_LanguageAndVariant() {
        assertValidToLocale("us__POSIX", "us", "", "POSIX");
        assertValidToLocale("fr__POSIX", "fr", "", "POSIX");
        assertValidToLocale("de__POSIX", "de", "", "POSIX");
        assertValidToLocale("qq__POSIX", "qq", "", "POSIX");
        assertValidToLocale("en__A", "en", "", "A");
        assertValidToLocale("en__a", "en", "", "a");
        assertValidToLocale("en__1", "en", "", "1");

        try {
            LocaleUtils.toLocale("us__");
            fail("Should fail with empty variant");
        } catch (final IllegalArgumentException iae) {
        }
    }

    @Test
    public void testToLocale_LanguageAndCountryAndVariant() {
        assertValidToLocale("us_EN_A", "us", "EN", "A");
        assertValidToLocale("us_EN_a", "us", "EN", "a");
        assertValidToLocale("us_EN_SF#HOME", "us", "EN", "SF#HOME");
        assertValidToLocale("us_EN_POSIX", "us", "EN", "POSIX");
        assertValidToLocale("fr_FR_POSIX", "fr", "FR", "POSIX");
        assertValidToLocale("de_DE_POSIX", "de", "DE", "POSIX");
        assertValidToLocale("qq_ZZ_POSIX", "qq", "ZZ", "POSIX");

        try {
            LocaleUtils.toLocale("us_EN-POSIX");
            fail("Should fail if not underscore separated");
        } catch (final IllegalArgumentException iae) {
        }
        try {
            LocaleUtils.toLocale("us_EN_");
            fail("Should fail if empty variant");
        } catch (final IllegalArgumentException iae) {
        }
        try {
            LocaleUtils.toLocale("us_eN_POSIX");
            fail("Should fail if not uppercase country");
        } catch (final IllegalArgumentException iae) {
        }
    }

    @Test
    public void testToLocale_CountryOnlyOrCountryAndVariant() {
        assertValidToLocale("_GB", "", "GB");
        assertValidToLocale("_GB_POSIX", "", "GB", "POSIX");
        assertValidToLocale("_GB_P", "", "GB", "P");

        try {
            LocaleUtils.toLocale("_G");
            fail("Must be at least 3 chars if starts with underscore");
        } catch (final IllegalArgumentException iae) {
        }
        try {
            LocaleUtils.toLocale("_Gb");
            fail("Country must be uppercase");
        } catch (final IllegalArgumentException iae) {
        }
        try {
            LocaleUtils.toLocale("_gB");
            fail("Country must be uppercase");
        } catch (final IllegalArgumentException iae) {
        }
        try {
            LocaleUtils.toLocale("_12");
            fail("Country must be letters");
        } catch (final IllegalArgumentException iae) {
        }
        try {
            LocaleUtils.toLocale("_GB_");
            fail("Variant cannot be empty when trailing underscore");
        } catch (final IllegalArgumentException iae) {
        }
        try {
            LocaleUtils.toLocale("_GB-POSIX");
            fail("Must use underscore");
        } catch (final IllegalArgumentException iae) {
        }
    }

    @Test
    public void testToLocale_InvalidFormats() {
        assertInvalidToLocale("");
        assertInvalidToLocale(" ");
        assertInvalidToLocale("a");
        assertInvalidToLocale("1");
        assertInvalidToLocale("12");
        assertInvalidToLocale("en_");
        assertInvalidToLocale("en_A");
        assertInvalidToLocale("en_USA");
        assertInvalidToLocale("en_GB_");
        assertInvalidToLocale("en_GB_a_b");
        assertInvalidToLocale("en_gb");
        assertInvalidToLocale("EN_GB");
        assertInvalidToLocale("en_gB");
        assertInvalidToLocale("en_Gb");
        assertInvalidToLocale("en-GB");
        assertInvalidToLocale("en_GB-POSIX");
        assertInvalidToLocale("_");
        assertInvalidToLocale("__");
        assertInvalidToLocale("___");
        assertInvalidToLocale("____");
        assertInvalidToLocale("_____");
    }

    private void assertValidToLocale(final String localeString, final String language, final String country) {
        final Locale locale = LocaleUtils.toLocale(localeString);
        assertNotNull("valid locale", locale);
        assertEquals(language, locale.getLanguage());
        assertEquals(country, locale.getCountry());
        assertEquals("", locale.getVariant());
    }

    private void assertValidToLocale(final String localeString, final String language, final String country, final String variant) {
        final Locale locale = LocaleUtils.toLocale(localeString);
        assertNotNull("valid locale", locale);
        assertEquals(language, locale.getLanguage());
        assertEquals(country, locale.getCountry());
        assertEquals(variant, locale.getVariant());
    }

    private void assertInvalidToLocale(final String localeString) {
        try {
            LocaleUtils.toLocale(localeString);
            fail("Must fail for: " + localeString);
        } catch (final IllegalArgumentException iae) {
            // expected
        }
    }

    // -----------------------------------------------------------------------
    @Test
    public void testLocaleLookupList_Locale() {
        assertNull(LocaleUtils.localeLookupList(null));

        List<Locale> list = LocaleUtils.localeLookupList(LOCALE_QQ);
        assertEquals(1, list.size());
        assertEquals(LOCALE_QQ, list.get(0));

        list = LocaleUtils.localeLookupList(LOCALE_EN);
        assertEquals(1, list.size());
        assertEquals(LOCALE_EN, list.get(0));

        list = LocaleUtils.localeLookupList(LOCALE_EN_US);
        assertEquals(2, list.size());
        assertEquals(LOCALE_EN_US, list.get(0));
        assertEquals(LOCALE_EN, list.get(1));

        list = LocaleUtils.localeLookupList(LOCALE_EN_US_WIN);
        assertEquals(3, list.size());
        assertEquals(LOCALE_EN_US_WIN, list.get(0));
        assertEquals(LOCALE_EN_US, list.get(1));
        assertEquals(LOCALE_EN, list.get(2));

        list = LocaleUtils.localeLookupList(LOCALE_EN_WIN);
        assertEquals(2, list.size());
        assertEquals(LOCALE_EN_WIN, list.get(0));
        assertEquals(LOCALE_EN, list.get(1));
    }

    @Test
    public void testLocaleLookupList_Locale_Locale() {
        assertNull(LocaleUtils.localeLookupList(null, null));
        assertNull(LocaleUtils.localeLookupList(null, LOCALE_QQ));

        List<Locale> list = LocaleUtils.localeLookupList(LOCALE_QQ, null);
        assertEquals(1, list.size());
        assertEquals(LOCALE_QQ, list.get(0));

        list = LocaleUtils.localeLookupList(LOCALE_QQ, LOCALE_QQ);
        assertEquals(1, list.size());
        assertEquals(LOCALE_QQ, list.get(0));

        list = LocaleUtils.localeLookupList(LOCALE_EN, LOCALE_QQ);
        assertEquals(2, list.size());
        assertEquals(LOCALE_EN, list.get(0));
        assertEquals(LOCALE_QQ, list.get(1));

        list = LocaleUtils.localeLookupList(LOCALE_EN_US, LOCALE_QQ);
        assertEquals(3, list.size());
        assertEquals(LOCALE_EN_US, list.get(0));
        assertEquals(LOCALE_EN, list.get(1));
        assertEquals(LOCALE_QQ, list.get(2));

        list = LocaleUtils.localeLookupList(LOCALE_EN_US_WIN, LOCALE_QQ);
        assertEquals(4, list.size());
        assertEquals(LOCALE_EN_US_WIN, list.get(0));
        assertEquals(LOCALE_EN_US, list.get(1));
        assertEquals(LOCALE_EN, list.get(2));
        assertEquals(LOCALE_QQ, list.get(3));

        list = LocaleUtils.localeLookupList(LOCALE_EN_WIN, LOCALE_QQ);
        assertEquals(3, list.size());
        assertEquals(LOCALE_EN_WIN, list.get(0));
        assertEquals(LOCALE_EN, list.get(1));
        assertEquals(LOCALE_QQ, list.get(2));

        list = LocaleUtils.localeLookupList(LOCALE_EN_US, LOCALE_EN);
        assertEquals(2, list.size());
        assertEquals(LOCALE_EN_US, list.get(0));
        assertEquals(LOCALE_EN, list.get(1));

        list = LocaleUtils.localeLookupList(LOCALE_EN_US_WIN, LOCALE_EN_US);
        assertEquals(3, list.size());
        assertEquals(LOCALE_EN_US_WIN, list.get(0));
        assertEquals(LOCALE_EN_US, list.get(1));
        assertEquals(LOCALE_EN, list.get(2));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testLocaleLookupList_Unmodifiable() {
        final List<Locale> list = LocaleUtils.localeLookupList(LOCALE_EN_US);
        list.add(LOCALE_FR);
    }

    // -----------------------------------------------------------------------
    @Test
    public void testAvailableLocaleList() {
        final List<Locale> list = LocaleUtils.availableLocaleList();
        final List<Locale> list2 = LocaleUtils.availableLocaleList();
        assertNotNull(list);
        assertSame(list, list2);
        assertTrue(list.size() > 0);

        final List<Locale> jdkList = Arrays.asList(Locale.getAvailableLocales());
        assertEquals(jdkList.size(), list.size());
        assertTrue(list.containsAll(jdkList));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAvailableLocaleList_Unmodifiable() {
        final List<Locale> list = LocaleUtils.availableLocaleList();
        list.add(LOCALE_FR);
    }

    // -----------------------------------------------------------------------
    @Test
    public void testAvailableLocaleSet() {
        final Set<Locale> set = LocaleUtils.availableLocaleSet();
        final Set<Locale> set2 = LocaleUtils.availableLocaleSet();
        assertNotNull(set);
        assertSame(set, set2);
        assertTrue(set.size() > 0);

        final List<Locale> jdkList = Arrays.asList(Locale.getAvailableLocales());
        assertEquals(new HashSet<Locale>(jdkList).size(), set.size());
        assertTrue(set.containsAll(jdkList));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAvailableLocaleSet_Unmodifiable() {
        final Set<Locale> set = LocaleUtils.availableLocaleSet();
        set.add(LOCALE_FR);
    }

    // -----------------------------------------------------------------------
    @Test
    public void testIsAvailableLocale() {
        final Set<Locale> set = LocaleUtils.availableLocaleSet();
        for (final Locale locale : set) {
            assertTrue(LocaleUtils.isAvailableLocale(locale));
        }
        assertFalse(LocaleUtils.isAvailableLocale(new Locale("qq", "ZZ", "POSIX")));
        assertFalse(LocaleUtils.isAvailableLocale(new Locale("xx", "YY")));
        assertFalse(LocaleUtils.isAvailableLocale(null));
    }

    // -----------------------------------------------------------------------
    @Test
    public void testLanguagesByCountry() {
        assertNotNull(LocaleUtils.languagesByCountry(null));
        assertTrue(LocaleUtils.languagesByCountry(null).isEmpty());

        assertNotNull(LocaleUtils.languagesByCountry("ZZ"));
        assertTrue(LocaleUtils.languagesByCountry("ZZ").isEmpty());

        final List<Locale> listUS = LocaleUtils.languagesByCountry("US");
        assertNotNull(listUS);
        assertTrue(listUS.size() > 0);
        for (final Locale locale : listUS) {
            assertEquals("US", locale.getCountry());
        }

        final List<Locale> listGB = LocaleUtils.languagesByCountry("GB");
        assertNotNull(listGB);
        assertTrue(listGB.size() > 0);
        for (final Locale locale : listGB) {
            assertEquals("GB", locale.getCountry());
        }

        final List<Locale> listCached = LocaleUtils.languagesByCountry("US");
        assertSame(listUS, listCached);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testLanguagesByCountry_Unmodifiable() {
        final List<Locale> list = LocaleUtils.languagesByCountry("US");
        list.add(LOCALE_FR);
    }

    // -----------------------------------------------------------------------
    @Test
    public void testCountriesByLanguage() {
        assertNotNull(LocaleUtils.countriesByLanguage(null));
        assertTrue(LocaleUtils.countriesByLanguage(null).isEmpty());

        assertNotNull(LocaleUtils.countriesByLanguage("zz"));
        assertTrue(LocaleUtils.countriesByLanguage("zz").isEmpty());

        final List<Locale> listEn = LocaleUtils.countriesByLanguage("en");
        assertNotNull(listEn);
        assertTrue(listEn.size() > 0);
        for (final Locale locale : listEn) {
            assertEquals("en", locale.getLanguage());
            assertFalse(locale.getCountry().isEmpty());
        }

        final List<Locale> listFr = LocaleUtils.countriesByLanguage("fr");
        assertNotNull(listFr);
        assertTrue(listFr.size() > 0);
        for (final Locale locale : listFr) {
            assertEquals("fr", locale.getLanguage());
            assertFalse(locale.getCountry().isEmpty());
        }

        final List<Locale> listCached = LocaleUtils.countriesByLanguage("en");
        assertSame(listEn, listCached);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCountriesByLanguage_Unmodifiable() {
        final List<Locale> list = LocaleUtils.countriesByLanguage("en");
        list.add(LOCALE_FR);
    }
}