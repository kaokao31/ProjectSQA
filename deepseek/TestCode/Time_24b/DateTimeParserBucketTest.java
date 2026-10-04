package org.joda.time.format;

import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for DateTimeParserBucket.
 * Designed to achieve high line and branch coverage, and to trigger
 * known defect (Defects4J Time bug 24) related to state restore.
 */
public class DateTimeParserBucketTest {

    private static final DateTimeZone ZONE = DateTimeZone.UTC;
    private static final Locale LOCALE = Locale.US;
    private static final Integer PIVOT_YEAR = 2020;
    private static final int DEFAULT_YEAR = 2000;

    private DateTimeParserBucket bucket;

    @Before
    public void setUp() {
        bucket = new DateTimeParserBucket(ZONE, LOCALE, PIVOT_YEAR, DEFAULT_YEAR);
    }

    @After
    public void tearDown() {
        bucket = null;
    }

    // ---------- Constructor Tests ----------

    @Test
    public void testConstructorWithAllParams() {
        assertNotNull("Bucket should be created", bucket);
        assertEquals(ZONE, bucket.getZone());
        assertEquals(LOCALE, bucket.getLocale());
        assertEquals(PIVOT_YEAR, bucket.getPivotYear());
        assertEquals(DEFAULT_YEAR, bucket.getDefaultYear().intValue());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullZone() {
        new DateTimeParserBucket(null, LOCALE, PIVOT_YEAR, DEFAULT_YEAR);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullLocale() {
        new DateTimeParserBucket(ZONE, null, PIVOT_YEAR, DEFAULT_YEAR);
    }

    @Test
    public void testConstructorNullPivotYear() {
        DateTimeParserBucket b = new DateTimeParserBucket(ZONE, LOCALE, null, DEFAULT_YEAR);
        assertNull("Pivot year should be null", b.getPivotYear());
    }

    // ---------- Zone Tests ----------

    @Test
    public void testGetSetZone() {
        DateTimeZone newZone = DateTimeZone.forID("America/New_York");
        bucket.setZone(newZone);
        assertEquals(newZone, bucket.getZone());
        bucket.setZone(null);
        assertNull("Zone should be null after setting null", bucket.getZone());
    }

    // ---------- Offset Tests ----------

    @Test
    public void testGetSetOffset() {
        assertNull("Initial offset should be null", bucket.getOffset());
        bucket.setOffset(3600000);
        assertEquals(Integer.valueOf(3600000), bucket.getOffset());
        bucket.setOffset(null);
        assertNull("Offset should be null after setting null", bucket.getOffset());
    }

    // ---------- Pivot Year Tests ----------

    @Test
    public void testGetSetPivotYear() {
        assertEquals(PIVOT_YEAR, bucket.getPivotYear());
        bucket.setPivotYear(1999);
        assertEquals(Integer.valueOf(1999), bucket.getPivotYear());
        bucket.setPivotYear(null);
        assertNull("Pivot year should be null", bucket.getPivotYear());
    }

    // ---------- Default Year Tests ----------

    @Test
    public void testGetSetDefaultYear() {
        assertEquals(DEFAULT_YEAR, bucket.getDefaultYear().intValue());
        bucket.setDefaultYear(2010);
        assertEquals(Integer.valueOf(2010), bucket.getDefaultYear());
        bucket.setDefaultYear(null);
        assertNull("Default year should be null", bucket.getDefaultYear());
    }

    // ---------- Locale Tests ----------

    @Test
    public void testGetLocale() {
        assertEquals(LOCALE, bucket.getLocale());
    }

    // ---------- Save / Restore State Tests ----------

    @Test
    public void testSaveRestoreBasic() {
        // initially: zone=UTC, offset=null, pivotYear=2020, defaultYear=2000
        Object state = bucket.saveState();
        assertNotNull("Saved state must not be null", state);

        // change everything
        bucket.setZone(DateTimeZone.forID("Europe/London"));
        bucket.setOffset(7200000);
        bucket.setPivotYear(1990);
        bucket.setDefaultYear(2015);

        // restore
        bucket.restoreState(state);

        // verify original values restored
        assertEquals(ZONE, bucket.getZone());
        assertNull("Offset should be null after restore", bucket.getOffset());
        assertEquals(PIVOT_YEAR, bucket.getPivotYear());
        assertEquals(DEFAULT_YEAR, bucket.getDefaultYear().intValue());
    }

    @Test
    public void testSaveRestoreWithDefaultYearBug() {
        // Defects4J Time bug 24: default year not properly restored after restoreState.
        bucket.setDefaultYear(2000);
        Object state = bucket.saveState();
        bucket.setDefaultYear(2010);
        bucket.restoreState(state);
        // After restore, defaultYear should be 2000 (the saved value)
        assertEquals("Default year should be restored to original",
                Integer.valueOf(2000), bucket.getDefaultYear());
    }

    @Test
    public void testSaveRestoreMultipleStates() {
        Object state1 = bucket.saveState();
        bucket.setDefaultYear(2001);
        Object state2 = bucket.saveState();
        bucket.setDefaultYear(2002);
        Object state3 = bucket.saveState();

        // restore to state2
        bucket.restoreState(state2);
        assertEquals(Integer.valueOf(2001), bucket.getDefaultYear());

        // restore to state1
        bucket.restoreState(state1);
        assertEquals(Integer.valueOf(2000), bucket.getDefaultYear());

        // restore to state3
        bucket.restoreState(state3);
        assertEquals(Integer.valueOf(2002), bucket.getDefaultYear());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRestoreInvalidState() {
        bucket.restoreState(new Object());
    }

    // ---------- Edge Cases ----------

    @Test
    public void testSaveStateWithNullDefaultYear() {
        bucket.setDefaultYear(null);
        Object state = bucket.saveState();
        assertNotNull(state);
        bucket.setDefaultYear(2000);
        bucket.restoreState(state);
        assertNull("Default year should be null after restore", bucket.getDefaultYear());
    }

    @Test
    public void testSetOffsetMaxValue() {
        bucket.setOffset(Integer.MAX_VALUE);
        assertEquals(Integer.valueOf(Integer.MAX_VALUE), bucket.getOffset());
    }

    @Test
    public void testSetOffsetMinValue() {
        bucket.setOffset(Integer.MIN_VALUE);
        assertEquals(Integer.valueOf(Integer.MIN_VALUE), bucket.getOffset());
    }

    @Test
    public void testSetPivotYearMinValue() {
        bucket.setPivotYear(Integer.MIN_VALUE);
        assertEquals(Integer.valueOf(Integer.MIN_VALUE), bucket.getPivotYear());
    }

    @Test
    public void testSetDefaultYearMaxValue() {
        bucket.setDefaultYear(Integer.MAX_VALUE);
        assertEquals(Integer.valueOf(Integer.MAX_VALUE), bucket.getDefaultYear());
    }

    // ---------- Loop / Branch Coverage ----------
    // (The saveState/restoreState methods contain internal loops and if-conditions;
    //  these tests exercise different paths.)

    @Test
    public void testMultipleSaveStateSameObject() {
        Object state1 = bucket.saveState();
        Object state2 = bucket.saveState();
        // Both should be independent
        assertNotSame(state1, state2);
        bucket.setDefaultYear(3000);
        bucket.restoreState(state1);
        assertEquals(DEFAULT_YEAR, bucket.getDefaultYear().intValue());
        bucket.restoreState(state2);
        assertEquals(DEFAULT_YEAR, bucket.getDefaultYear().intValue());
    }

    @Test
    public void testSaveStateAfterZoneChange() {
        bucket.setZone(DateTimeZone.forOffsetHours(5));
        Object state = bucket.saveState();
        bucket.setZone(ZONE);
        bucket.restoreState(state);
        assertEquals(DateTimeZone.forOffsetHours(5), bucket.getZone());
    }
}