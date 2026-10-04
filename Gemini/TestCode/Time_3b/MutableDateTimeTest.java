package org.joda.time;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class MutableDateTimeTest {

    private MutableDateTime mutableDateTime;

    @Before
    public void setUp() {
        mutableDateTime = new MutableDateTime();
    }

    @Test
    public void testConstructors() {
        MutableDateTime m1 = new MutableDateTime();
        assertNotNull(m1);

        MutableDateTime m2 = new MutableDateTime(DateTimeZone.UTC);
        assertEquals(DateTimeZone.UTC, m2.getZone());

        MutableDateTime m3 = new MutableDateTime(2020, 5, 15, 12, 30, 0, 0);
        assertEquals(2020, m3.getYear());
        assertEquals(5, m3.getMonthOfYear());
        assertEquals(15, m3.getDayOfMonth());
        assertEquals(12, m3.getHourOfDay());
        assertEquals(30, m3.getMinuteOfHour());
        assertEquals(0, m3.getSecondOfMinute());
        assertEquals(0, m3.getMillisOfSecond());

        MutableDateTime m4 = new MutableDateTime(2020, 5, 15, 12, 30, 0, 0, DateTimeZone.UTC);
        assertEquals(DateTimeZone.UTC, m4.getZone());

        MutableDateTime m5 = new MutableDateTime(0L);
        assertEquals(0L, m5.getMillis());

        MutableDateTime m6 = new MutableDateTime(0L, DateTimeZone.UTC);
        assertEquals(0L, m6.getMillis());
        assertEquals(DateTimeZone.UTC, m6.getZone());

        MutableDateTime m7 = new MutableDateTime(0L, ISOChronology.getInstanceUTC());
        assertEquals(ISOChronology.getInstanceUTC(), m7.getChronology());

        Date date = new Date(1000L);
        MutableDateTime m8 = new MutableDateTime(date);
        assertEquals(1000L, m8.getMillis());

        MutableDateTime m9 = new MutableDateTime(date, DateTimeZone.UTC);
        assertEquals(1000L, m9.getMillis());

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(2000L);
        MutableDateTime m10 = new MutableDateTime(calendar);
        assertEquals(2000L, m10.getMillis());

        MutableDateTime m11 = new MutableDateTime(m3);
        assertEquals(m3.getMillis(), m11.getMillis());

        Object obj = "2020-05-15T12:30:00.000Z";
        MutableDateTime m12 = new MutableDateTime(obj, DateTimeZone.UTC);
        assertNotNull(m12);

        MutableDateTime m13 = new MutableDateTime(obj, ISOChronology.getInstanceUTC());
        assertNotNull(m13);
        
        MutableDateTime m14 = new MutableDateTime((Object) null);
        assertNotNull(m14);
        
        MutableDateTime m15 = new MutableDateTime((Object) null, DateTimeZone.UTC);
        assertNotNull(m15);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalidObject() {
        new MutableDateTime(new Object());
    }

    @Test
    public void testFactoryMethods() {
        MutableDateTime m1 = MutableDateTime.now();
        assertNotNull(m1);

        MutableDateTime m2 = MutableDateTime.now(DateTimeZone.UTC);
        assertEquals(DateTimeZone.UTC, m2.getZone());

        MutableDateTime m3 = MutableDateTime.now(ISOChronology.getInstanceUTC());
        assertEquals(ISOChronology.getInstanceUTC(), m3.getChronology());

        MutableDateTime m4 = MutableDateTime.parse("2020-05-15T12:30:00.000Z");
        assertNotNull(m4);

        MutableDateTime m5 = MutableDateTime.parse("2020-05-15T12:30:00.000Z", DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
        assertNotNull(m5);
    }

    @Test
    public void testCopy() {
        MutableDateTime m = new MutableDateTime();
        MutableDateTime copy = m.copy();
        assertEquals(m, copy);
        assertNotSame(m, copy);
    }

    @Test
    public void testGettersAndSetters() {
        mutableDateTime.setMillis(10000L);
        assertEquals(10000L, mutableDateTime.getMillis());

        mutableDateTime.setChronology(ISOChronology.getInstanceUTC());
        assertEquals(ISOChronology.getInstanceUTC(), mutableDateTime.getChronology());

        mutableDateTime.setZone(DateTimeZone.UTC);
        assertEquals(DateTimeZone.UTC, mutableDateTime.getZone());

        mutableDateTime.setZoneRetainFields(DateTimeZone.getDefault());
        assertNotEquals(DateTimeZone.UTC, mutableDateTime.getZone());

        mutableDateTime.setDate(2021, 6, 16);
        assertEquals(2021, mutableDateTime.getYear());
        assertEquals(6, mutableDateTime.getMonthOfYear());
        assertEquals(16, mutableDateTime.getDayOfMonth());

        mutableDateTime.setTime(13, 40, 15, 500);
        assertEquals(13, mutableDateTime.getHourOfDay());
        assertEquals(40, mutableDateTime.getMinuteOfHour());
        assertEquals(15, mutableDateTime.getSecondOfMinute());
        assertEquals(500, mutableDateTime.getMillisOfSecond());

        mutableDateTime.setDateTime(2022, 1, 1, 10, 0, 0, 0);
        assertEquals(2022, mutableDateTime.getYear());
        assertEquals(1, mutableDateTime.getMonthOfYear());
        assertEquals(1, mutableDateTime.getDayOfMonth());
        assertEquals(10, mutableDateTime.getHourOfDay());

        mutableDateTime.set(DateTimeFieldType.year(), 2023);
        assertEquals(2023, mutableDateTime.getYear());

        mutableDateTime.add(100L);
        mutableDateTime.add((ReadableDuration) new Duration(100L));
        mutableDateTime.add((ReadableDuration) new Duration(100L), 2);
        mutableDateTime.add((ReadablePeriod) new Period(100L));
        mutableDateTime.add((ReadablePeriod) new Period(100L), 2);
        mutableDateTime.addDays(1);
        mutableDateTime.addHours(1);
        mutableDateTime.addMillis(1);
        mutableDateTime.addMinutes(1);
        mutableDateTime.addMonths(1);
        mutableDateTime.addSeconds(1);
        mutableDateTime.addWeeks(1);
        mutableDateTime.addYears(1);

        mutableDateTime.add(DateTimeFieldType.dayOfMonth(), 1);

        mutableDateTime.subtract(100L);
        mutableDateTime.subtract((ReadableDuration) new Duration(100L));
        mutableDateTime.subtract((ReadableDuration) new Duration(100L), 2);
        mutableDateTime.subtract((ReadablePeriod) new Period(100L));
        mutableDateTime.subtract((ReadablePeriod) new Period(100L), 2);
        mutableDateTime.subtractDays(1);
        mutableDateTime.subtractHours(1);
        mutableDateTime.subtractMillis(1);
        mutableDateTime.subtractMinutes(1);
        mutableDateTime.subtractMonths(1);
        mutableDateTime.subtractSeconds(1);
        mutableDateTime.subtractWeeks(1);
        mutableDateTime.subtractYears(1);
    }

    @Test
    public void testPropertyMethods() {
        assertNotNull(mutableDateTime.property(DateTimeFieldType.year()));
        assertNotNull(mutableDateTime.era());
        assertNotNull(mutableDateTime.centuryOfEra());
        assertNotNull(mutableDateTime.yearOfCentury());
        assertNotNull(mutableDateTime.yearOfEra());
        assertNotNull(mutableDateTime.year());
        assertNotNull(mutableDateTime.monthOfYear());
        assertNotNull(mutableDateTime.weekyear());
        assertNotNull(mutableDateTime.weekOfWeekyear());
        assertNotNull(mutableDateTime.dayOfYear());
        assertNotNull(mutableDateTime.dayOfMonth());
        assertNotNull(mutableDateTime.dayOfWeek());
        assertNotNull(mutableDateTime.hourOfDay());
        assertNotNull(mutableDateTime.minuteOfDay());
        assertNotNull(mutableDateTime.minuteOfHour());
        assertNotNull(mutableDateTime.secondOfDay());
        assertNotNull(mutableDateTime.secondOfMinute());
        assertNotNull(mutableDateTime.millisOfDay());
        assertNotNull(mutableDateTime.millisOfSecond());
    }

    @Test
    public void testRounding() {
        mutableDateTime.setMillis(1500L);
        mutableDateTime.rounding(null, true);
        mutableDateTime.rounding(DateTimeFieldType.secondOfMinute(), true);
        mutableDateTime.roundCenturyOfEra(true);
        mutableDateTime.roundCenturyOfEra(false);
        mutableDateTime.roundYearOfCentury(true);
        mutableDateTime.roundYearOfCentury(false);
        mutableDateTime.roundYear(true);
        mutableDateTime.roundYear(false);
        mutableDateTime.roundMonthOfYear(true);
        mutableDateTime.roundMonthOfYear(false);
        mutableDateTime.roundWeekyear(true);
        mutableDateTime.roundWeekyear(false);
        mutableDateTime.roundWeekOfWeekyear(true);
        mutableDateTime.roundWeekOfWeekyear(false);
        mutableDateTime.roundDayOfYear(true);
        mutableDateTime.roundDayOfYear(false);
        mutableDateTime.roundDayOfMonth(true);
        mutableDateTime.roundDayOfMonth(false);
        mutableDateTime.roundDayOfWeek(true);
        mutableDateTime.roundDayOfWeek(false);
        mutableDateTime.roundHourOfDay(true);
        mutableDateTime.roundHourOfDay(false);
        mutableDateTime.roundMinuteOfDay(true);
        mutableDateTime.roundMinuteOfDay(false);
        mutableDateTime.roundMinuteOfHour(true);
        mutableDateTime.roundMinuteOfHour(false);
        mutableDateTime.roundSecondOfDay(true);
        mutableDateTime.roundSecondOfDay(false);
        mutableDateTime.roundSecondOfMinute(true);
        mutableDateTime.roundSecondOfMinute(false);
        mutableDateTime.roundMillisOfDay(true);
        mutableDateTime.roundMillisOfDay(false);
        mutableDateTime.roundMillisOfSecond(true);
        mutableDateTime.roundMillisOfSecond(false);
    }

    @Test
    public void testConversions() {
        assertNotNull(mutableDateTime.toInstant());
        assertNotNull(mutableDateTime.toDateTime());
        assertNotNull(mutableDateTime.toDateTimeISO());
        assertNotNull(mutableDateTime.toDateTime(DateTimeZone.UTC));
        assertNotNull(mutableDateTime.toDateTime(ISOChronology.getInstanceUTC()));
        assertNotNull(mutableDateTime.toMutableDateTime());
        assertNotNull(mutableDateTime.toMutableDateTimeISO());
        assertNotNull(mutableDateTime.toMutableDateTime(DateTimeZone.UTC));
        assertNotNull(mutableDateTime.toMutableDateTime(ISOChronology.getInstanceUTC()));
        assertNotNull(mutableDateTime.toDate());
        assertNotNull(mutableDateTime.toCalendar(Locale.getDefault()));
        assertNotNull(mutableDateTime.toGregorianCalendar());
    }

    @Test
    public void testPropertyInnerClass() {
        MutableDateTime.Property prop = mutableDateTime.year();
        assertEquals(mutableDateTime, prop.getMutableDateTime());
        prop.set(2025);
        assertEquals(2025, prop.get());
        prop.add(1);
        prop.addNoWrap(1);
        prop.addWrapField(1);
        prop.roundCeiling();
        prop.roundFloor();
        prop.roundHalfCeiling();
        prop.roundHalfEven();
        prop.roundHalfFloor();
        assertNotNull(prop.setCopy(2020));
        assertNotNull(prop.addToCopy(1));
        assertNotNull(prop.addWrapFieldToCopy(1));
        assertNotNull(prop.roundFloorCopy());
        assertNotNull(prop.roundCeilingCopy());
        assertNotNull(prop.roundHalfFloorCopy());
        assertNotNull(prop.roundHalfCeilingCopy());
        assertNotNull(prop.roundHalfEvenCopy());
    }
}