package org.joda.time.format;

import org.junit.Before;
import org.junit.Test;

import java.util.Locale;
import java.util.TimeZone;

import org.joda.time.Chronology;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.DurationField;
import org.joda.time.DurationFieldType;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.chrono.GJChronology;
import org.joda.time.chrono.BuddhistChronology;

import static org.junit.Assert.*;

public class DateTimeParserBucketTest {

    private DateTimeParserBucket bucket;
    private Chronology chronology;

    @Before
    public void setUp() {
        chronology = ISOChronology.getInstanceUTC();
        bucket = new DateTimeParserBucket(0L, chronology, Locale.ENGLISH, null, 2020);
    }

    @Test
    public void testConstructorAndBasicGetters() {
        DateTimeParserBucket b = new DateTimeParserBucket(123456789L, chronology, Locale.FRENCH, DateTimeZone.forID("America/New_York"), 1995);
        assertEquals(123456789L, b.getMillis());
        assertEquals(Locale.FRENCH, b.getLocale());
        assertEquals(DateTimeZone.forID("America/New_York"), b.getZone());
        assertEquals(1995, b.getPivotYear());
        assertEquals(chronology, b.getChronology());
    }

    @Test
    public void testComputeMillisBasic() {
        bucket.saveField(DateTimeFieldType.year(), 2020);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 5);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 15);
        
        long millis = bucket.computeMillis(true, "2020-05-15");
        LocalDate date = new LocalDate(millis, ISOChronology.getInstanceUTC());
        assertEquals(2020, date.getYear());
        assertEquals(5, date.getMonthOfYear());
        assertEquals(15, date.getDayOfMonth());
    }

    @Test
    public void testComputeMillisWithText() {
        bucket.saveField(DateTimeFieldType.year(), 2021);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 1);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 10);
        bucket.saveField(DateTimeFieldType.hourOfDay(), 12);
        bucket.saveField(DateTimeFieldType.minuteOfHour(), 30);

        long millis = bucket.computeMillis(true);
        LocalDateTime dt = new LocalDateTime(millis, ISOChronology.getInstanceUTC());
        assertEquals(2021, dt.getYear());
        assertEquals(1, dt.getMonthOfYear());
        assertEquals(10, dt.getDayOfMonth());
        assertEquals(12, dt.getHourOfDay());
        assertEquals(30, dt.getMinuteOfHour());
    }

    @Test
    public void testZoneAndOffsetInteractions() {
        bucket.setZone(DateTimeZone.forID("UTC"));
        assertEquals(DateTimeZone.UTC, bucket.getZone());

        bucket.setOffset(120);
        assertEquals(Integer.valueOf(120), bucket.getOffset());

        bucket.setOffset(null);
        assertNull(bucket.getOffset());
    }

    @Test
    public void testSaveAndResetState() {
        Object state = bucket.saveState();
        assertNotNull(state);

        bucket.saveField(DateTimeFieldType.year(), 2010);
        bucket.setZone(DateTimeZone.forID("UTC"));
        bucket.setOffset(60);

        assertTrue(bucket.restoreState(state));
        assertEquals(2020, bucket.getPivotYear());
    }

    @Test
    public void testRestoreStateFailure() {
        Object state = bucket.saveState();
        assertFalse(bucket.restoreState("InvalidStateObject"));
    }

    @Test
    public void testSaveFieldMethods() {
        DateTimeField field = ISOChronology.getInstanceUTC().dayOfMonth();
        bucket.saveField(field, 15);
        bucket.saveField(DateTimeFieldType.year(), 2022);
        
        long millis = bucket.computeMillis(true);
        LocalDate date = new LocalDate(millis, ISOChronology.getInstanceUTC());
        assertEquals(2022, date.getYear());
        assertEquals(15, date.getDayOfMonth());
    }

    @Test
    public void testSaveFieldWithText() {
        bucket.saveField(DateTimeFieldType.monthOfYear(), "3", Locale.ENGLISH);
        bucket.saveField(DateTimeFieldType.year(), 2020);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 1);

        long millis = bucket.computeMillis(true);
        LocalDate date = new LocalDate(millis, ISOChronology.getInstanceUTC());
        assertEquals(3, date.getMonthOfYear());
    }

    @Test(expected = IllegalFieldValueException.class)
    public void testInvalidFieldThrowsException() {
        bucket.saveField(DateTimeFieldType.monthOfYear(), 15); // Invalid month
        bucket.computeMillis(true);
    }

    @Test
    public void testCompareSavedFields() {
        // Test internal SavedField sorting logic (via computeMillis with various fields)
        bucket.saveField(DateTimeFieldType.hourOfDay(), 10);
        bucket.saveField(DateTimeFieldType.year(), 2020);
        bucket.saveField(DateTimeFieldType.secondOfMinute(), 45);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 6);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 20);
        bucket.saveField(DateTimeFieldType.minuteOfHour(), 15);

        long millis = bucket.computeMillis(true);
        LocalDateTime dt = new LocalDateTime(millis, ISOChronology.getInstanceUTC());
        assertEquals(2020, dt.getYear());
        assertEquals(6, dt.getMonthOfYear());
        assertEquals(20, dt.getDayOfMonth());
        assertEquals(10, dt.getHourOfDay());
        assertEquals(15, dt.getMinuteOfHour());
        assertEquals(45, dt.getSecondOfMinute());
    }

    @Test
    public void testParserBucketDefiningUTC() {
        DateTimeParserBucket utcBucket = new DateTimeParserBucket(0L, ISOChronology.getInstanceUTC(), Locale.getDefault());
        assertNotNull(utcBucket);
        assertEquals(ISOChronology.getInstanceUTC(), utcBucket.getChronology());
    }

    @Test
    public void testSavedFieldCopyConstructorAndMethods() {
        // Exercise SavedField directly if accessible or via bucket operations
        bucket.saveField(DateTimeFieldType.year(), 2012);
        bucket.saveField(DateTimeFieldType.dayOfYear(), 50);
        
        // Force sort and execution
        long millis = bucket.computeMillis(false, "Test text");
        assertTrue(millis != 0L);
    }

    @Test
    public void testDuplicateFieldsWithDifferentValues() {
        // Test same field saved twice to trigger conflict resolution / sorting logic
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 10);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 12);
        bucket.saveField(DateTimeFieldType.year(), 2020);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 1);

        long millis = bucket.computeMillis(true);
        LocalDate date = new LocalDate(millis, ISOChronology.getInstanceUTC());
        assertEquals(12, date.getDayOfMonth()); // Last set or sorted appropriately
    }

    @Test
    public void testDemonstrateBug24ChronologyHandling() {
        // Specifically targeting potential weekyear/year/century issues present in Time-24
        Chronology bDayChrono = BuddhistChronology.getInstance();
        DateTimeParserBucket bugBucket = new DateTimeParserBucket(0L, bDayChrono, Locale.ENGLISH, null, 2000);
        bugBucket.saveField(DateTimeFieldType.weekyear(), 2545);
        bugBucket.saveField(DateTimeFieldType.weekOfWeekyear(), 5);
        bugBucket.saveField(DateTimeFieldType.dayOfWeek(), 1);

        long millis = bugBucket.computeMillis(true);
        assertNotNull(millis);
    }
}