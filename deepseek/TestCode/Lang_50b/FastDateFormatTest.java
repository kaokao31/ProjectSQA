package org.apache.commons.lang.time;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for FastDateFormat.
 * Targets Defects4J bug 50 (locale caching issue) and full coverage.
 */
public class FastDateFormatTest {

    private Locale originalLocale;
    private TimeZone originalTimeZone;
    private Date testDate;

    @Before
    public void setUp() {
        originalLocale = Locale.getDefault();
        originalTimeZone = TimeZone.getDefault();
        // Use a fixed date for reproducible format testing
        testDate = new Date(0L); // 1970-01-01 00:00:00 UTC
    }

    @After
    public void tearDown() {
        Locale.setDefault(originalLocale);
        TimeZone.setDefault(originalTimeZone);
    }

    // -----------------------------------------------------------------------
    // Basic instantiation and caching
    // -----------------------------------------------------------------------

    @Test
    public void testGetDateInstance() {
        FastDateFormat fdf = FastDateFormat.getDateInstance(FastDateFormat.SHORT);
        assertNotNull("Basic SHORT date instance should not be null", fdf);
    }

    @Test
    public void testGetDateInstanceWithStyleAndTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        FastDateFormat fdf = FastDateFormat.getDateInstance(FastDateFormat.LONG, tz);
        assertNotNull("LONG date instance with GMT timezone should not be null", fdf);
        assertEquals("Timezone should be GMT", tz, fdf.getTimeZone());
    }

    @Test
    public void testGetDateInstanceWithStyleAndLocale() {
        Locale loc = Locale.FRANCE;
        FastDateFormat fdf = FastDateFormat.getDateInstance(FastDateFormat.FULL, loc);
        assertNotNull("FULL date instance with France locale should not be null", fdf);
        assertEquals("Locale should be France", loc, fdf.getLocale());
    }

    @Test
    public void testGetDateInstanceWithAllParams() {
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        Locale loc = Locale.JAPAN;
        FastDateFormat fdf = FastDateFormat.getDateInstance(FastDateFormat.MEDIUM, tz, loc);
        assertNotNull("MEDIUM date instance with explicit time zone and locale should not be null", fdf);
        assertEquals("Timezone should be America/New_York", tz, fdf.getTimeZone());
        assertEquals("Locale should be Japan", loc, fdf.getLocale());
    }

    @Test
    public void testGetTimeInstance() {
        FastDateFormat fdf = FastDateFormat.getTimeInstance(FastDateFormat.MEDIUM);
        assertNotNull("Basic MEDIUM time instance should not be null", fdf);
    }

    @Test
    public void testGetDateTimeInstance() {
        FastDateFormat fdf = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.LONG);
        assertNotNull("Combined SHORT date and LONG time instance should not be null", fdf);
    }

    // -----------------------------------------------------------------------
    // Caching behavior – same parameters return the same instance
    // -----------------------------------------------------------------------

    @Test
    public void testCachingSameStyleAndTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        FastDateFormat fdf1 = FastDateFormat.getDateInstance(FastDateFormat.SHORT, tz);
        FastDateFormat fdf2 = FastDateFormat.getDateInstance(FastDateFormat.SHORT, tz);
        assertSame("Caching should reuse instances for same style and timezone", fdf1, fdf2);
    }

    @Test
    public void testCachingSameStyleAndLocale() {
        Locale loc = Locale.UK;
        FastDateFormat fdf1 = FastDateFormat.getDateInstance(FastDateFormat.LONG, loc);
        FastDateFormat fdf2 = FastDateFormat.getDateInstance(FastDateFormat.LONG, loc);
        assertSame("Caching should reuse instances for same style and locale", fdf1, fdf2);
    }

    @Test
    public void testCachingSameAllParams() {
        TimeZone tz = TimeZone.getTimeZone("PST");
        Locale loc = Locale.CANADA;
        FastDateFormat fdf1 = FastDateFormat.getDateInstance(FastDateFormat.FULL, tz, loc);
        FastDateFormat fdf2 = FastDateFormat.getDateInstance(FastDateFormat.FULL, tz, loc);
        assertSame("Caching should reuse instances for same style, timezone, and locale", fdf1, fdf2);
    }

    @Test
    public void testCachingDifferentStyle() {
        FastDateFormat fdf1 = FastDateFormat.getDateInstance(FastDateFormat.SHORT);
        FastDateFormat fdf2 = FastDateFormat.getDateInstance(FastDateFormat.LONG);
        assertNotSame("Different styles should produce different instances", fdf1, fdf2);
    }

    // -----------------------------------------------------------------------
    // Bug reproduction: Locale change should not affect previously cached instances
    // -----------------------------------------------------------------------

    @Test
    public void testChangeDefaultLocale_DateInstance() {
        // Set default to German
        Locale.setDefault(Locale.GERMANY);
        FastDateFormat fdf1 = FastDateFormat.getDateInstance(FastDateFormat.FULL);
        assertSame("After setting default to GERMANY, the locale should be GERMANY",
                Locale.GERMANY, fdf1.getLocale());

        // Change default to US
        Locale.setDefault(Locale.US);
        FastDateFormat fdf2 = FastDateFormat.getDateInstance(FastDateFormat.FULL);
        // Bug: fdf2 should be the same instance as fdf1 (with German locale)
        // but currently (without fix) it might be a new instance with US locale.
        // This assertion will fail if the bug is present.
        assertSame("Changing default locale should not create a new instance; " +
                "the cached instance with original locale should be reused",
                fdf1, fdf2);
        assertSame("Even after locale change, the locale of the cached instance should remain GERMANY",
                Locale.GERMANY, fdf2.getLocale());
    }

    @Test
    public void testChangeDefaultLocale_DateTimeInstance() {
        // Set default to German
        Locale.setDefault(Locale.GERMANY);
        FastDateFormat fdf1 = FastDateFormat.getDateTimeInstance(FastDateFormat.LONG, FastDateFormat.MEDIUM);
        assertSame("After setting default to GERMANY, the locale should be GERMANY",
                Locale.GERMANY, fdf1.getLocale());

        // Change default to US
        Locale.setDefault(Locale.US);
        FastDateFormat fdf2 = FastDateFormat.getDateTimeInstance(FastDateFormat.LONG, FastDateFormat.MEDIUM);
        assertSame("Changing default locale should not create a new instance for DateTimeInstance",
                fdf1, fdf2);
        assertSame("Locale should remain GERMANY after locale change",
                Locale.GERMANY, fdf2.getLocale());
    }

    @Test
    public void testChangeDefaultLocale_TimeInstance() {
        Locale.setDefault(Locale.GERMANY);
        FastDateFormat fdf1 = FastDateFormat.getTimeInstance(FastDateFormat.SHORT);
        assertSame(Locale.GERMANY, fdf1.getLocale());

        Locale.setDefault(Locale.US);
        FastDateFormat fdf2 = FastDateFormat.getTimeInstance(FastDateFormat.SHORT);
        assertSame("Time instance should also be cached across locale changes", fdf1, fdf2);
        assertSame(Locale.GERMANY, fdf2.getLocale());
    }

    // -----------------------------------------------------------------------
    // Format method tests
    // -----------------------------------------------------------------------

    @Test
    public void testFormatDate() {
        FastDateFormat fdf = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT, Locale.US);
        String formatted = fdf.format(testDate);
        // With default US locale, SHORT date and time format: "1/1/70 12:00 AM" (depending on timezone)
        // Use a known pattern to verify
        assertNotNull("Formatted string should not be null", formatted);
        assertTrue("Formatted string should contain '1/1/70'", formatted.contains("1/1/70") || formatted.contains("1/01/70"));
    }

    @Test
    public void testFormatDateWithTimezone() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        FastDateFormat fdf = FastDateFormat.getDateTimeInstance(FastDateFormat.MEDIUM, FastDateFormat.MEDIUM, Locale.UK);
        // UK locale uses 'dd/MM/yyyy' for date format? Actually for MEDIUM it's 'dd-MMM-yyyy'
        // Let's just check it's not empty and contains expected year.
        String result = fdf.format(new Date(0));
        assertNotNull(result);
        assertTrue("Formatted date should contain 1970 (year)", result.contains("1970"));
    }

    @Test
    public void testFormatWithNullDate() {
        FastDateFormat fdf = FastDateFormat.getDateInstance(FastDateFormat.SHORT);
        try {
            fdf.format((Date) null);
            fail("Expected IllegalArgumentException for null date");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testFormatWithLong() {
        FastDateFormat fdf = FastDateFormat.getDateTimeInstance(FastDateFormat.LONG, FastDateFormat.LONG, Locale.CANADA);
        String formatted = fdf.format(0L);
        assertNotNull("Formatted string from long should not be null", formatted);
    }

    // -----------------------------------------------------------------------
    // Pattern and locale/zone retrieval
    // -----------------------------------------------------------------------

    @Test
    public void testGetPattern() {
        FastDateFormat fdf = FastDateFormat.getDateInstance(FastDateFormat.FULL, Locale.US);
        String pattern = fdf.getPattern();
        assertNotNull("Pattern should not be null", pattern);
        assertFalse("Pattern should not be empty", pattern.isEmpty());
    }

    @Test
    public void testGetLocale() {
        Locale loc = Locale.ITALY;
        FastDateFormat fdf = FastDateFormat.getDateInstance(FastDateFormat.FULL, loc);
        assertEquals("Locale should be Italy", loc, fdf.getLocale());
    }

    @Test
    public void testGetTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("Asia/Tokyo");
        FastDateFormat fdf = FastDateFormat.getDateInstance(FastDateFormat.LONG, tz);
        assertEquals("Timezone should be Asia/Tokyo", tz, fdf.getTimeZone());
    }

    @Test
    public void testGetTimeZoneDefault() {
        FastDateFormat fdf = FastDateFormat.getDateInstance(FastDateFormat.MEDIUM);
        assertEquals("Default timezone should be the system default", TimeZone.getDefault(), fdf.getTimeZone());
    }

    // -----------------------------------------------------------------------
    // Null arguments
    // -----------------------------------------------------------------------

    @Test
    public void testNullTimeZone() {
        FastDateFormat fdf = FastDateFormat.getDateInstance(FastDateFormat.SHORT, (TimeZone) null);
        assertNotNull("Null timezone should default to system timezone", fdf);
        assertEquals(TimeZone.getDefault(), fdf.getTimeZone());
    }

    @Test
    public void testNullLocale() {
        FastDateFormat fdf = FastDateFormat.getDateInstance(FastDateFormat.SHORT, (Locale) null);
        assertNotNull("Null locale should default to system locale", fdf);
        assertEquals(Locale.getDefault(), fdf.getLocale());
    }

    @Test
    public void testNullTimeZoneAndLocale() {
        FastDateFormat fdf = FastDateFormat.getDateInstance(FastDateFormat.LONG, (TimeZone) null, (Locale) null);
        assertNotNull("Both null should use defaults", fdf);
        assertEquals(TimeZone.getDefault(), fdf.getTimeZone());
        assertEquals(Locale.getDefault(), fdf.getLocale());
    }

    // -----------------------------------------------------------------------
    // All styles
    // -----------------------------------------------------------------------

    @Test
    public void testAllDateStyles() {
        int[] styles = {FastDateFormat.SHORT, FastDateFormat.MEDIUM, FastDateFormat.LONG, FastDateFormat.FULL};
        for (int style : styles) {
            FastDateFormat fdf = FastDateFormat.getDateInstance(style, Locale.US);
            assertNotNull("Style " + style + " should be valid", fdf);
            assertFalse("Pattern for style " + style + " should not be empty",
                    fdf.getPattern().isEmpty());
        }
    }

    @Test
    public void testAllTimeStyles() {
        int[] styles = {FastDateFormat.SHORT, FastDateFormat.MEDIUM, FastDateFormat.LONG, FastDateFormat.FULL};
        for (int style : styles) {
            FastDateFormat fdf = FastDateFormat.getTimeInstance(style, Locale.US);
            assertNotNull("Time style " + style + " should be valid", fdf);
            assertFalse("Pattern for time style " + style + " should not be empty",
                    fdf.getPattern().isEmpty());
        }
    }

    @Test
    public void testAllDateTimeStyleCombinations() {
        int[] styles = {FastDateFormat.SHORT, FastDateFormat.MEDIUM, FastDateFormat.LONG, FastDateFormat.FULL};
        for (int dateStyle : styles) {
            for (int timeStyle : styles) {
                FastDateFormat fdf = FastDateFormat.getDateTimeInstance(dateStyle, timeStyle, Locale.US);
                assertNotNull("Date style " + dateStyle + " and time style " + timeStyle + " should be valid", fdf);
                assertFalse("Pattern should not be empty", fdf.getPattern().isEmpty());
            }
        }
    }

    // -----------------------------------------------------------------------
    // TimeZone change should not affect cached same-style instances
    // -----------------------------------------------------------------------

    @Test
    public void testChangeDefaultTimeZone() {
        TimeZone original = TimeZone.getDefault();
        FastDateFormat fdf1 = FastDateFormat.getDateInstance(FastDateFormat.SHORT);
        // Change default timezone
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+5"));
        FastDateFormat fdf2 = FastDateFormat.getDateInstance(FastDateFormat.SHORT);
        // Because the first call used the original default timezone, the second call
        // should return the same cached instance (since only style is specified,
        // timezone is taken from default at first call and stored).
        // This is similar to the locale bug, but here the caching key includes timezone
        // so if timezone changes, a new instance would be created. However, the intended
        // behavior might be to cache based on the provided timezone, and if none is provided,
        // it uses the default at the time of creation. Changing the default later should not
        // affect the cached instance. The factory methods store the default at creation.
        // So fdf1 and fdf2 should be the same instance if the timezone has not been explicitly set.
        // Actually, if the cache key includes the timezone that was passed (which is the default at the time),
        // then after changing the default, the second call will pass a different timezone (new default),
        // resulting in a different cache key and a new instance. This is the same bug pattern as locale.
        // We test that it does NOT create a new instance.
        // The current implementation likely has this bug as well? But the test suite is for locale bug;
        // we include this to be thorough.
        // This test may fail if the caching also includes timezone directly from the default at call time.
        // We assume the intended behavior is that the timezone used is the one in effect at the first call.
        assertSame("Changing default timezone should not create a new instance for same style",
                fdf1, fdf2);
        // Restore original timezone
        TimeZone.setDefault(original);
    }

    // -----------------------------------------------------------------------
    // Edge / boundary cases
    // -----------------------------------------------------------------------

    @Test
    public void testStyleOutOfRange() {
        // FastDateFormat only accepts 0..3 (SHORT=3, MEDIUM=2, LONG=1, FULL=0)
        // But the constants are defined; invalid values should not be accepted.
        // The method may throw IllegalArgumentException.
        try {
            FastDateFormat.getDateInstance(-1);
            fail("Expected IllegalArgumentException for negative style");
        } catch (IllegalArgumentException e) {
            // expected
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

    @Test
    public void testFormatWithDateObject() {
        FastDateFormat fdf = FastDateFormat.getDateInstance(FastDateFormat.SHORT, Locale.US);
        String result = fdf.format(new Date(0));
        assertEquals("Formatted date should be '1/1/70' or '01/01/70' (depending on pattern)",
                "1/1/70", result); // SHORT US: "M/d/yy" => 1/1/70
    }

    // -----------------------------------------------------------------------
    // Thread safety not tested explicitly here, but we can test concurrency
    // with simple two-thread access to ensure no exceptions.
    // -----------------------------------------------------------------------

    @Test
    public void testConcurrentAccess() throws InterruptedException {
        final FastDateFormat fdf = FastDateFormat.getDateInstance(FastDateFormat.FULL, Locale.US);
        final int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        final boolean[] failed = {false};
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < 100; j++) {
                        fdf.format(new Date());
                    }
                } catch (Exception e) {
                    failed[0] = true;
                }
            });
            threads[i].start();
        }
        for (Thread t : threads) {
            t.join();
        }
        assertFalse("Concurrent access should not throw exceptions", failed[0]);
    }
}