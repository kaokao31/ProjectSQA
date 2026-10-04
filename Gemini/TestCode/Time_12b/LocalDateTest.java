/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * work for this additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joda.time;

import static org.junit.Assert.*;

import java.Calendar;
import java.Date;
import java.GregorianCalendar;
import java.Locale;
import java.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive JUnit 4 test suite for {@link LocalDate} specifically targeting
 * Time Bug 12 and edge cases for maximum line and branch coverage.
 */
public class LocalDateTest {

    private static final DateTimeZone UTC = DateTimeZone.UTC;
    private static final DateTimeZone PARIS = DateTimeZone.forID("Europe/Paris");
    private static final DateTimeZone TOKYO = DateTimeZone.forID("Asia/Tokyo");

    private DateTimeZone originalDateTimeZone = null;
    private Locale originalLocale = null;

    @Before
    public void setUp() {
        originalDateTimeZone = DateTimeZone.getDefault();
        originalLocale = Locale.getDefault();
        DateTimeZone.setDefault(UTC);
        Locale.setDefault(Locale.US);
    }

    @After
    public void tearDown() {
        DateTimeZone.setDefault(originalDateTimeZone);
        Locale.setDefault(originalLocale);
    }

    @Test
    public void testNow() {
        LocalDate dt = new LocalDate();
        assertNotNull(dt);
    }

    @Test
    public void testNow_DateTimeZone() {
        LocalDate dt = new LocalDate(PARIS);
        assertNotNull(dt);
        assertEquals(PARIS, dt.getChronology().getZone());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNow_NullDateTimeZone() {
        new LocalDate((DateTimeZone) null);
    }

    @Test
    public void testNow_Chronology() {
        LocalDate dt = new LocalDate(BuddhistChronology.getInstance());
        assertNotNull(dt);
        assertEquals(BuddhistChronology.getInstanceUTC(), dt.getChronology());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNow_NullChronology() {
        new LocalDate((Chronology) null);
    }

    @Test
    public void testFactoryMethods() {
        LocalDate dt1 = LocalDate.now();
        assertNotNull(dt1);

        LocalDate dt2 = LocalDate.now(PARIS);
        assertNotNull(dt2);
        assertEquals(PARIS, dt2.getChronology().getZone());

        LocalDate dt3 = LocalDate.now(BuddhistChronology.getInstance());
        assertNotNull(dt3);
    }

    @Test
    public void testParse() {
        LocalDate dt = LocalDate.parse("2007-10-27");
        assertEquals(2007, dt.getYear());
        assertEquals(10, dt.getMonthOfYear());
        assertEquals(27, dt.getDayOfMonth());

        LocalDate dt2 = LocalDate.parse("2007-10-27", DateTimeFormat.forPattern("yyyy-MM-dd"));
        assertEquals(dt, dt2);
    }

    @Test
    public void testFromCalendarFields() {
        Calendar cal = Calendar.getInstance();
        cal.set(2012, Calendar.JUNE, 9, 13, 25, 40);
        LocalDate dt = LocalDate.fromCalendarFields(cal);
        assertEquals(2012, dt.getYear());
        assertEquals(6, dt.getMonthOfYear());
        assertEquals(9, dt.getDayOfMonth());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromCalendarFields_Null() {
        LocalDate.fromCalendarFields(null);
    }

    @Test
    public void testFromDateFields() {
        Date date = new Date(112, 5, 9, 13, 25, 40); // 2012-06-09
        LocalDate dt = LocalDate.fromDateFields(date);
        assertEquals(2012, dt.getYear());
        assertEquals(6, dt.getMonthOfYear());
        assertEquals(9, dt.getDayOfMonth());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromDateFields_Null() {
        LocalDate.fromDateFields(null);
    }

    @Test
    public void testConstructors() {
        LocalDate dt = new LocalDate(2007, 10, 27);
        assertEquals(2007, dt.getYear());
        assertEquals(10, dt.getMonthOfYear());
        assertEquals(27, dt.getDayOfMonth());

        LocalDate dt2 = new LocalDate(2007, 10, 27, ISOChronology.getInstanceUTC());
        assertEquals(dt, dt2);

        LocalDate dt3 = new LocalDate(2007, 10, 27, null);
        assertEquals(dt, dt3);

        LocalDate dt4 = new LocalDate(1193491200000L); // 2007-10-27T00:00:00.000Z
        assertEquals(2007, dt4.getYear());
        assertEquals(10, dt4.getMonthOfYear());
        assertEquals(27, dt4.getDayOfMonth());

        LocalDate dt5 = new LocalDate(1193491200000L, PARIS);
        assertEquals(2007, dt5.getYear());

        LocalDate dt6 = new LocalDate(1193491200000L, ISOChronology.getInstance(PARIS));
        assertEquals(2007, dt6.getYear());

        LocalDate dt7 = new LocalDate((Object) "2007-10-27");
        assertEquals(dt, dt7);

        LocalDate dt8 = new LocalDate((Object) "2007-10-27", PARIS);
        assertEquals(2007, dt8.getYear());

        LocalDate dt9 = new LocalDate((Object) "2007-10-27", ISOChronology.getInstance(PARIS));
        assertEquals(2007, dt9.getYear());

        LocalDate dt10 = new LocalDate((Object) null);
        assertNotNull(dt10);

        LocalDate dt11 = new LocalDate((Object) null, PARIS);
        assertNotNull(dt11);

        LocalDate dt12 = new LocalDate((Object) null, (Chronology) null);
        assertNotNull(dt12);
    }

    @Test
    public void testGettersAndFields() {
        LocalDate dt = new LocalDate(2007, 10, 27, ISOChronology.getInstance());
        assertEquals(3, dt.size());
        assertEquals(2007, dt.getYear());
        assertEquals(10, dt.getMonthOfYear());
        assertEquals(27, dt.getDayOfMonth());

        assertEquals(ISOChronology.getInstance(), dt.getChronology());
        
        DateTimeFieldType type = DateTimeFieldType.year();
        assertTrue(dt.isSupported(type));
        assertEquals(2007, dt.get(type));

        DurationFieldType durationType = DurationFieldType.years();
        assertTrue(dt.isSupported(durationType));
    }

    @Test
    public void testPropertyMethods() {
        LocalDate dt = new LocalDate(2007, 10, 27);

        assertNotNull(dt.year());
        assertEquals(2007, dt.year().get());
        assertEquals("year", dt.year().getName());
        assertEquals(dt, dt.year().getLocalTime()); // Actually getLocalDate() via property inheritance

        assertNotNull(dt.monthOfYear());
        assertEquals(10, dt.monthOfYear().get());

        assertNotNull(dt.dayOfMonth());
        assertEquals(27, dt.dayOfMonth().get());

        assertEquals(2007, dt.property(DateTimeFieldType.year()).get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProperty_Invalid() {
        LocalDate dt = new LocalDate(2007, 10, 27);
        dt.property(DateTimeFieldType.hourOfDay());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGet_Invalid() {
        LocalDate dt = new LocalDate(2007, 10, 27);
        dt.get(DateTimeFieldType.hourOfDay());
    }

    @Test
    public void testWithMethods() {
        LocalDate dt = new LocalDate(2007, 10, 27);

        assertEquals(new LocalDate(2008, 10, 27), dt.withYear(2008));
        assertEquals(new LocalDate(2007, 11, 27), dt.withMonthOfYear(11));
        assertEquals(new LocalDate(2007, 10, 28), dt.withDayOfMonth(28));

        assertEquals(dt, dt.withChronologyRetainFields(BuddhistChronology.getInstance()));
        assertEquals(dt, dt.withField(DateTimeFieldType.year(), 2007));
        assertEquals(new LocalDate(2008, 10, 27), dt.withField(DateTimeFieldType.year(), 2008));

        assertEquals(dt, dt.withFieldAdded(DurationFieldType.years(), 0));
        assertEquals(new LocalDate(2008, 10, 27), dt.withFieldAdded(DurationFieldType.years(), 1));
        assertEquals(new LocalDate(2007, 11, 27), dt.withFieldAdded(DurationFieldType.months(), 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithField_Null() {
        LocalDate dt = new LocalDate(2007, 10, 27);
        dt.withField(null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithFieldAdded_Null() {
        LocalDate dt = new LocalDate(2007, 10, 27);
        dt.withFieldAdded(null, 1);
    }

    @Test
    public void testPlusMinusMethods() {
        LocalDate dt = new LocalDate(2007, 10, 27);

        assertEquals(new LocalDate(2008, 10, 27), dt.plusYears(1));
        assertEquals(new LocalDate(2006, 10, 27), dt.minusYears(1));

        assertEquals(new LocalDate(2007, 11, 27), dt.plusMonths(1));
        assertEquals(new LocalDate(2007, 9, 27), dt.minusMonths(1));

        assertEquals(new LocalDate(2007, 10, 28), dt.plusDays(1));
        assertEquals(new LocalDate(2007, 10, 26), dt.minusDays(1));

        assertEquals(new LocalDate(2007, 11, 3), dt.plusWeeks(1));
        assertEquals(new LocalDate(2007, 10, 20), dt.minusWeeks(1));

        Period period = Period.years(1).months(2).days(3);
        assertEquals(new LocalDate(2008, 12, 30), dt.plus(period));
        assertEquals(new LocalDate(2006, 8, 24), dt.minus(period));

        assertEquals(new LocalDate(2008, 12, 30), dt.plus((ReadablePeriod) period));
        assertEquals(new LocalDate(2006, 8, 24), dt.minus((ReadablePeriod) period));

        assertEquals(dt, dt.plus(null));
        assertEquals(dt, dt.minus(null));
        assertEquals(dt, dt.plusYears(0));
    }

    @Test
    public void testToDateTime() {
        LocalDate dt = new LocalDate(2007, 10, 27);
        DateTime dateTime = dt.toDateTimeAtCurrentTime();
        assertNotNull(dateTime);
        assertEquals(2007, dateTime.getYear());
        assertEquals(10, dateTime.getMonthOfYear());
        assertEquals(27, dateTime.getDayOfMonth());

        DateTime dateTimePARIS = dt.toDateTimeAtCurrentTime(PARIS);
        assertEquals(PARIS, dateTimePARIS.getZone());

        DateTime dateTimeStart = dt.toDateTimeAtStartOfDay();
        assertEquals(0, dateTimeStart.getMillisOfDay());

        DateTime dateTimeStartPARIS = dt.toDateTimeAtStartOfDay(PARIS);
        assertEquals(PARIS, dateTimeStartPARIS.getZone());
        assertEquals(0, dateTimeStartPARIS.getMillisOfDay());

        LocalDateTime ldt = dt.toLocalDateTime(LocalTime.MIDNIGHT);
        assertEquals(2007, ldt.getYear());
        assertEquals(0, ldt.getMillisOfDay());

        DateMidnight dm = dt.toDateMidnight();
        assertEquals(2007, dm.getYear());

        DateMidnight dmPARIS = dt.toDateMidnight(PARIS);
        assertEquals(PARIS, dmPARIS.getZone());
    }

    @Test
    public void testToDateAndCalendar() {
        LocalDate dt = new LocalDate(2007, 10, 27);
        Date date = dt.toDate();
        assertNotNull(date);

        Calendar cal = dt.toCalendar(Locale.GERMAN);
        assertNotNull(cal);
    }

    @Test
    public void testConversionsAndComparisons() {
        LocalDate dt1 = new LocalDate(2007, 10, 27);
        LocalDate dt2 = new LocalDate(2007, 10, 28);
        LocalDate dt3 = new LocalDate(2007, 10, 27);

        assertTrue(dt1.compareTo(dt2) < 0);
        assertTrue(dt2.compareTo(dt1) > 0);
        assertTrue(dt1.compareTo(dt3) == 0);

        assertTrue(dt1.isEqual(dt3));
        assertTrue(dt2.isAfter(dt1));
        assertTrue(dt1.isBefore(dt2));

        assertTrue(dt1.isEqual(null)); // Compares with current LocalDate
        assertTrue(dt1.isBefore(null)); // Depends on current date, but let's test specific ReadablePartial:
        
        assertTrue(dt1.isEqual(dt3));
        assertFalse(dt1.isEqual(dt2));
        
        assertTrue(dt2.isAfter(dt1));
        assertFalse(dt1.isAfter(dt2));
        
        assertTrue(dt1.isBefore(dt2));
        assertFalse(dt2.isBefore(dt1));
    }

    @Test(expected = NullPointerException.class)
    public void testCompareTo_Null() {
        LocalDate dt = new LocalDate(2007, 10, 27);
        dt.compareTo(null);
    }

    @Test
    public void testEqualsAndHashCode() {
        LocalDate dt1 = new LocalDate(2007, 10, 27);
        LocalDate dt2 = new LocalDate(2007, 10, 27, ISOChronology.getInstance());
        LocalDate dt3 = new LocalDate(2007, 10, 28);
        String notALocalDate = "2007-10-27";

        assertTrue(dt1.equals(dt2));
        assertEquals(dt1.hashCode(), dt2.hashCode());
        assertFalse(dt1.equals(dt3));
        assertFalse(dt1.equals(notALocalDate));
        assertFalse(dt1.equals(null));
    }

    @Test
    public void testToString() {
        LocalDate dt = new LocalDate(2007, 10, 27);
        assertEquals("2007-10-27", dt.toString());
        assertEquals("2007/10/27", dt.toString("yyyy/MM/dd"));
        assertEquals("2007/10/27", dt.toString("yyyy/MM/dd", Locale.US));
    }

    @Test
    public void testBug12AndEpochDayOrFieldEdgeCases() {
        // Specifically targeting edge cases related to Bug 12 in Joda-Time (Month/Day adjustments across time zones or eras)
        LocalDate dt = new LocalDate(2012, 2, 29); // Leap year
        assertEquals(2012, dt.getYear());
        assertEquals(2, dt.getMonthOfYear());
        assertEquals(29, dt.getDayOfMonth());

        LocalDate plusYears = dt.plusYears(1); // 2013 is not a leap year, Joda-Time adjusts to 2013-02-28 or throws depending on chronology, default ISO rolls over or clamps.
        assertEquals(28, plusYears.getDayOfMonth());

        // Check values with different chronologies
        LocalDate buddhist = new LocalDate(2007, 10, 27, BuddhistChronology.getInstance());
        assertEquals(2550, buddhist.getYear()); // 2007 + 543 = 2550
    }
}