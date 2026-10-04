package org.joda.time;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import org.joda.time.chrono.ISOChronology;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.chrono.JulianChronology;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Comprehensive JUnit 4 test suite for MutableDateTime.
 * Covers constructors, setters, adders, edge cases, null handling, and boundary conditions.
 */
public class MutableDateTimeTest {

    private static final DateTimeZone UTC = DateTimeZone.UTC;
    private static final DateTimeZone PARIS = DateTimeZone.forID("Europe/Paris");
    private static final DateTimeZone NEW_YORK = DateTimeZone.forID("America/New_York");
    private static final long MILLIS_2000 = 946684800000L; // 2000-01-01T00:00:00.000Z

    private MutableDateTime base;

    @Before
    public void setUp() {
        base = new MutableDateTime(2000, 6, 15, 12, 30, 45, 500, ISOChronology.getInstanceUTC());
    }

    // ==================== Constructors ====================

    @Test
    public void testConstructorNoArg() {
        MutableDateTime mdt = new MutableDateTime();
        assertNotNull(mdt);
        assertTrue(mdt.getMillis() > 0);
        assertEquals(ISOChronology.getInstance(), mdt.getChronology());
    }

    @Test
    public void testConstructorLong() {
        MutableDateTime mdt = new MutableDateTime(MILLIS_2000);
        assertEquals(MILLIS_2000, mdt.getMillis());
        assertEquals(ISOChronology.getInstanceUTC(), mdt.getChronology());
    }

    @Test
    public void testConstructorLongChronology() {
        MutableDateTime mdt = new MutableDateTime(MILLIS_2000, GregorianChronology.getInstanceUTC());
        assertEquals(MILLIS_2000, mdt.getMillis());
        assertEquals(GregorianChronology.getInstanceUTC(), mdt.getChronology());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorLongNullChronology() {
        new MutableDateTime(MILLIS_2000, (Chronology) null);
    }

    @Test
    public void testConstructorObject() {
        MutableDateTime mdt = new MutableDateTime(new java.util.Date(MILLIS_2000));
        assertEquals(MILLIS_2000, mdt.getMillis());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullObject() {
        new MutableDateTime((Object) null);
    }

    @Test
    public void testConstructorObjectChronology() {
        MutableDateTime mdt = new MutableDateTime(new java.util.Date(MILLIS_2000), JulianChronology.getInstanceUTC());
        assertEquals(MILLIS_2000, mdt.getMillis());
        assertEquals(JulianChronology.getInstanceUTC(), mdt.getChronology());
    }

    @Test
    public void testConstructorInts() {
        MutableDateTime mdt = new MutableDateTime(2000, 1, 1, 0, 0, 0, 0, ISOChronology.getInstanceUTC());
        assertEquals(MILLIS_2000, mdt.getMillis());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorIntsNullChronology() {
        new MutableDateTime(2000, 1, 1, 0, 0, 0, 0, (Chronology) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalidDate() {
        new MutableDateTime(2000, 2, 30, 0, 0, 0, 0, ISOChronology.getInstanceUTC());
    }

    // ==================== setMillis ====================

    @Test
    public void testSetMillis() {
        base.setMillis(0L);
        assertEquals(0L, base.getMillis());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMillisNegative() {
        base.setMillis(-1L);
    }

    @Test
    public void testSetMillisLongMax() {
        base.setMillis(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, base.getMillis());
    }

    // ==================== setChronology ====================

    @Test
    public void testSetChronology() {
        Chronology julian = JulianChronology.getInstanceUTC();
        base.setChronology(julian);
        assertSame(julian, base.getChronology());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetChronologyNull() {
        base.setChronology(null);
    }

    // ==================== setDateTime ====================

    @Test
    public void testSetDateTime() {
        base.setDateTime(1999, 12, 31, 23, 59, 59, 999);
        assertEquals(1999, base.getYear());
        assertEquals(12, base.getMonthOfYear());
        assertEquals(31, base.getDayOfMonth());
        assertEquals(23, base.getHourOfDay());
        assertEquals(59, base.getMinuteOfHour());
        assertEquals(59, base.getSecondOfMinute());
        assertEquals(999, base.getMillisOfSecond());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDateTimeInvalid() {
        base.setDateTime(2000, 2, 30, 0, 0, 0, 0);
    }

    // ==================== set methods ====================

    @Test
    public void testSetYear() {
        base.setYear(1999);
        assertEquals(1999, base.getYear());
    }

    @Test
    public void testSetMonthOfYear() {
        base.setMonthOfYear(12);
        assertEquals(12, base.getMonthOfYear());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMonthOfYearInvalid() {
        base.setMonthOfYear(13);
    }

    @Test
    public void testSetDayOfMonth() {
        base.setDayOfMonth(1);
        assertEquals(1, base.getDayOfMonth());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDayOfMonthInvalid() {
        base.setDayOfMonth(32);
    }

    @Test
    public void testSetHourOfDay() {
        base.setHourOfDay(0);
        assertEquals(0, base.getHourOfDay());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetHourOfDayInvalid() {
        base.setHourOfDay(24);
    }

    @Test
    public void testSetMinuteOfHour() {
        base.setMinuteOfHour(59);
        assertEquals(59, base.getMinuteOfHour());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMinuteOfHourInvalid() {
        base.setMinuteOfHour(60);
    }

    @Test
    public void testSetSecondOfMinute() {
        base.setSecondOfMinute(0);
        assertEquals(0, base.getSecondOfMinute());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetSecondOfMinuteInvalid() {
        base.setSecondOfMinute(60);
    }

    @Test
    public void testSetMillisOfSecond() {
        base.setMillisOfSecond(999);
        assertEquals(999, base.getMillisOfSecond());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMillisOfSecondInvalid() {
        base.setMillisOfSecond(1000);
    }

    // ==================== add methods ====================

    @Test
    public void testAddYears() {
        base.addYears(1);
        assertEquals(2001, base.getYear());
    }

    @Test
    public void testAddYearsNegative() {
        base.addYears(-1);
        assertEquals(1999, base.getYear());
    }

    @Test
    public void testAddMonths() {
        base.addMonths(6);
        assertEquals(12, base.getMonthOfYear());
        assertEquals(2000, base.getYear());
    }

    @Test
    public void testAddMonthsWrap() {
        base.addMonths(7);
        assertEquals(1, base.getMonthOfYear());
        assertEquals(2001, base.getYear());
    }

    @Test
    public void testAddDays() {
        base.addDays(1);
        assertEquals(16, base.getDayOfMonth());
    }

    @Test
    public void testAddDaysNegative() {
        base.addDays(-1);
        assertEquals(14, base.getDayOfMonth());
    }

    @Test
    public void testAddWeeks() {
        base.addWeeks(1);
        assertEquals(22, base.getDayOfMonth());
    }

    @Test
    public void testAddHours() {
        base.addHours(1);
        assertEquals(13, base.getHourOfDay());
    }

    @Test
    public void testAddHoursWrap() {
        base.addHours(12);
        assertEquals(0, base.getHourOfDay());
        assertEquals(16, base.getDayOfMonth());
    }

    @Test
    public void testAddMinutes() {
        base.addMinutes(30);
        assertEquals(1, base.getHourOfDay());
        assertEquals(0, base.getMinuteOfHour());
    }

    @Test
    public void testAddSeconds() {
        base.addSeconds(15);
        assertEquals(45, base.getSecondOfMinute());
    }

    @Test
    public void testAddMillis() {
        base.addMillis(500);
        assertEquals(1000, base.getMillisOfSecond());
        assertEquals(46, base.getSecondOfMinute());
    }

    @Test
    public void testAddDuration() {
        base.add(new Duration(1000L));
        assertEquals(45, base.getSecondOfMinute());
        assertEquals(501, base.getMillisOfSecond());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullDuration() {
        base.add((Duration) null);
    }

    @Test
    public void testAddReadablePeriod() {
        base.add(Period.years(1));
        assertEquals(2001, base.getYear());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullPeriod() {
        base.add((ReadablePeriod) null);
    }

    // ==================== zone and chronology edge cases ====================

    @Test
    public void testSetZone() {
        base.setZone(PARIS);
        assertEquals(PARIS, base.getZone());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetZoneNull() {
        base.setZone(null);
    }

    @Test
    public void testDSTTransition() {
        // Spring forward: March 14, 2021 at 2:00 AM EST -> 3:00 AM EDT
        MutableDateTime dst = new MutableDateTime(2021, 3, 14, 1, 59, 59, 999, NEW_YORK);
        dst.addMillis(1);
        // After adding 1 ms, time should be 3:00:00.000 EDT
        assertEquals(3, dst.getHourOfDay());
        assertEquals(0, dst.getMinuteOfHour());
        assertEquals(0, dst.getSecondOfMinute());
        assertEquals(0, dst.getMillisOfSecond());
    }

    @Test
    public void testDSTFallBack() {
        // Fall back: November 7, 2021 at 2:00 AM EDT -> 1:00 AM EST
        MutableDateTime dst = new MutableDateTime(2021, 11, 7, 1, 59, 59, 999, NEW_YORK);
        dst.addMillis(1);
        // After adding 1 ms, time should be 1:00:00.000 EST (repeated hour)
        assertEquals(1, dst.getHourOfDay());
        assertEquals(0, dst.getMinuteOfHour());
        assertEquals(0, dst.getSecondOfMinute());
        assertEquals(0, dst.getMillisOfSecond());
    }

    // ==================== toDateTime / toMutableDateTime ====================

    @Test
    public void testToDateTime() {
        DateTime dt = base.toDateTime();
        assertEquals(base.getMillis(), dt.getMillis());
        assertEquals(base.getChronology(), dt.getChronology());
    }

    @Test
    public void testToMutableDateTime() {
        MutableDateTime copy = base.toMutableDateTime();
        assertEquals(base, copy);
        assertNotSame(base, copy);
    }

    // ==================== equals and hashCode ====================

    @Test
    public void testEqualsSame() {
        MutableDateTime other = new MutableDateTime(base);
        assertEquals(base, other);
        assertEquals(base.hashCode(), other.hashCode());
    }

    @Test
    public void testEqualsDifferentMillis() {
        MutableDateTime other = new MutableDateTime(base.getMillis() + 1);
        assertNotEquals(base, other);
    }

    @Test
    public void testEqualsNull() {
        assertFalse(base.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        assertFalse(base.equals("string"));
    }

    // ==================== toString ====================

    @Test
    public void testToString() {
        String str = base.toString();
        assertNotNull(str);
        assertTrue(str.contains("2000"));
    }

    @Test
    public void testToStringFormatter() {
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        String str = base.toString(fmt);
        assertEquals("2000-06-15T12:30:45.500Z", str);
    }

    // ==================== clone ====================

    @Test
    public void testClone() {
        MutableDateTime clone = (MutableDateTime) base.clone();
        assertEquals(base, clone);
        assertNotSame(base, clone);
    }

    // ==================== compareTo ====================

    @Test
    public void testCompareToEqual() {
        MutableDateTime other = new MutableDateTime(base);
        assertEquals(0, base.compareTo(other));
    }

    @Test
    public void testCompareToLess() {
        MutableDateTime earlier = new MutableDateTime(base.getMillis() - 1000);
        assertTrue(earlier.compareTo(base) < 0);
    }

    @Test
    public void testCompareToGreater() {
        MutableDateTime later = new MutableDateTime(base.getMillis() + 1000);
        assertTrue(later.compareTo(base) > 0);
    }

    @Test(expected = NullPointerException.class)
    public void testCompareToNull() {
        base.compareTo(null);
    }

    // ==================== boundary conditions ====================

    @Test
    public void testMinDate() {
        MutableDateTime min = new MutableDateTime(Long.MIN_VALUE, ISOChronology.getInstanceUTC());
        // Should not throw; just check it's very negative
        assertTrue(min.getMillis() < 0);
    }

    @Test
    public void testMaxDate() {
        MutableDateTime max = new MutableDateTime(Long.MAX_VALUE, ISOChronology.getInstanceUTC());
        assertTrue(max.getMillis() > 0);
    }

    @Test
    public void testLeapYear() {
        MutableDateTime leap = new MutableDateTime(2000, 2, 29, 0, 0, 0, 0, ISOChronology.getInstanceUTC());
        assertEquals(29, leap.getDayOfMonth());
        assertEquals(2, leap.getMonthOfYear());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonLeapYearFeb29() {
        new MutableDateTime(1900, 2, 29, 0, 0, 0, 0, ISOChronology.getInstanceUTC());
    }

    @Test
    public void testSetMillisOfSecondMax() {
        base.setMillisOfSecond(999);
        assertEquals(999, base.getMillisOfSecond());
    }

    @Test
    public void testSetMillisOfSecondMin() {
        base.setMillisOfSecond(0);
        assertEquals(0, base.getMillisOfSecond());
    }

    // ==================== serialization (if applicable) ====================

    @Test
    public void testSerialization() throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(base);
        oos.close();

        java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bos.toByteArray());
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bis);
        MutableDateTime deserialized = (MutableDateTime) ois.readObject();
        ois.close();

        assertEquals(base, deserialized);
    }
}