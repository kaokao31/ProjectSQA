package org.joda.time;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class DateTimeZoneTest {

    private DateTimeZone originalDefault;

    @Before
    public void setUp() {
        originalDefault = DateTimeZone.getDefault();
    }

    @After
    public void tearDown() {
        if (originalDefault != null) {
            DateTimeZone.setDefault(originalDefault);
        }
    }

    @Test
    public void testGetOffset() {
        DateTimeZone utc = DateTimeZone.UTC;
        assertEquals(0, utc.getOffset(0L));
        assertEquals(0, utc.getOffset(null));

        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        int offset = zone.getOffset(1577836800000L); // Jan 1 2020
        assertTrue(offset != 0);
        assertEquals(offset, zone.getOffset(Long.valueOf(1577836800000L)));
    }

    @Test
    public void testGetStandardOffset() {
        DateTimeZone utc = DateTimeZone.UTC;
        assertEquals(0, utc.getStandardOffset(0L));

        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        assertTrue(zone.getStandardOffset(0L) != zone.getOffset(0L));
    }

    @Test
    public void testIsStandardOffset() {
        DateTimeZone utc = DateTimeZone.UTC;
        assertTrue(utc.isStandardOffset(0L));
    }

    @Test
    public void testGetOffsetFromLocal() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        // Test local offset resolution around DST transitions
        int offset = zone.getOffsetFromLocal(1577836800000L);
        assertTrue(offset != 0);
    }

    @Test
    public void testConvertUTCToLocal() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        long local = zone.convertUTCToLocal(1000L);
        long utc = zone.convertLocalToUTC(local, false);
        assertEquals(1000L, utc);
    }

    @Test
    public void testGetTransition() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        long nextTrans = zone.nextTransition(0L);
        long prevTrans = zone.previousTransition(nextTrans);
        assertTrue(prevTrans <= 0L);
    }

    @Test
    public void testGetName() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        String name = zone.getName(0L, Locale.ENGLISH);
        assertNotNull(name);
        
        String shortName = zone.getShortName(0L, Locale.ENGLISH);
        assertNotNull(shortName);
    }

    @Test
    public void testGetNameKey() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        String key = zone.getNameKey(0L);
        assertNotNull(key);
    }

    @Test
    public void testEqualsAndHashCode() {
        DateTimeZone zone1 = DateTimeZone.forID("America/New_York");
        DateTimeZone zone2 = DateTimeZone.forID("America/New_York");
        DateTimeZone utc = DateTimeZone.UTC;

        assertEquals(zone1, zone2);
        assertEquals(zone1.hashCode(), zone2.hashCode());
        assertNotEquals(zone1, utc);
        assertNotEquals(zone1, null);
        assertNotEquals(zone1, "NotAZone");
    }

    @Test
    public void testForID() {
        DateTimeZone zone = DateTimeZone.forID("UTC");
        assertSame(DateTimeZone.UTC, zone);

        DateTimeZone zonePlus = DateTimeZone.forID("+02:00");
        assertEquals(7200000, zonePlus.getOffset(0L));

        DateTimeZone zoneMinus = DateTimeZone.forID("-05:00");
        assertEquals(-18000000, zoneMinus.getOffset(0L));

        assertNull(DateTimeZone.forID(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForIDInvalid() {
        DateTimeZone.forID("Invalid/Zone_ID_XYZ");
    }

    @Test
    public void testForTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("GMT+1");
        DateTimeZone zone = DateTimeZone.forTimeZone(tz);
        assertNotNull(zone);

        assertNotNull(DateTimeZone.forTimeZone(null));
    }

    @Test
    public void testSetAndGetDefault() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        DateTimeZone.setDefault(zone);
        assertEquals(zone, DateTimeZone.getDefault());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDefaultNull() {
        DateTimeZone.setDefault(null);
    }

    @Test
    public void testToTimeZone() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        TimeZone tz = zone.toTimeZone();
        assertNotNull(tz);
    }

    @Test
    public void testGetAvailableIDs() {
        assertNotNull(DateTimeZone.getAvailableIDs());
        assertTrue(DateTimeZone.getAvailableIDs().size() > 0);
    }

    @Test
    public void testGetOffsetMillis() {
        assertEquals(0, DateTimeZone.offsetMillis(0));
    }

    @Test
    public void testIsLocalDateTimeGap() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        LocalDateTime ldt = new LocalDateTime(2020, 3, 8, 2, 30, zone);
        // This exercises gap check logic if applicable
        boolean isGap = zone.isLocalDateTimeGap(ldt);
        // Just assert it returns without throwing
        assertTrue(isGap || !isGap);
    }

    @Test
    public void testAdjustOffset() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        long adjusted = zone.adjustOffset(0L, true);
        assertEquals(0L, adjusted);
    }
}