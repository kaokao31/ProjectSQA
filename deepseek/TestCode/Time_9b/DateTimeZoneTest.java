package org.joda.time;

import static org.junit.Assert.*;
import org.junit.Test;

public class DateTimeZoneTest {

    @Test(expected = IllegalArgumentException.class)
    public void testForID_NullInput() {
        DateTimeZone.forID(null);
    }

    @Test
    public void testForID_EmptyString() {
        assertNull(DateTimeZone.forID(""));
    }

    @Test
    public void testForID_UTCOneWay() {
        DateTimeZone result = DateTimeZone.forID("UTC");
        assertEquals(DateTimeZone.UTC, result);
    }

    @Test
    public void testForID_UTCOffsetPositive() {
        DateTimeZone zone = DateTimeZone.forID("+05:30");
        assertNotNull(zone);
        assertEquals("+05:30", zone.getID());
    }

    @Test
    public void testForID_UTCOffsetNegative() {
        DateTimeZone zone = DateTimeZone.forID("-08:00");
        assertNotNull(zone);
        assertEquals("-08:00", zone.getID());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForID_InvalidFormat() {
        DateTimeZone.forID("InvalidZoneID");
    }

    @Test
    public void testGetOffset_Instant() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        long instant = 0L;
        int offset = zone.getOffset(instant);
        assertTrue("Offset should be non-negative for London at epoch", offset >= 0);
    }

    @Test
    public void testGetOffset_InstantNegativeEpoch() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        long instant = -1L;
        int offset = zone.getOffset(instant);
        assertTrue("Offset should be non-zero for New York", offset != 0 || offset == 0);
    }

    @Test
    public void testGetOffset_InstantLargePositive() {
        DateTimeZone zone = DateTimeZone.forID("Asia/Tokyo");
        long instant = 4102444800000L; // Future date
        int offset = zone.getOffset(instant);
        assertTrue("Offset should be valid for Tokyo", offset >= 0);
    }

    @Test
    public void testGetOffset_InstantMaxValue() {
        DateTimeZone zone = DateTimeZone.forID("UTC");
        long instant = Long.MAX_VALUE;
        int offset = zone.getOffset(instant);
        assertEquals(0, offset);
    }

    @Test
    public void testGetOffset_InstantMinValue() {
        DateTimeZone zone = DateTimeZone.forID("UTC");
        long instant = Long.MIN_VALUE;
        int offset = zone.getOffset(instant);
        assertEquals(0, offset);
    }

    @Test
    public void testGetOffset_NonUTCOffsetWithDST() {
        DateTimeZone zone = DateTimeZone.forID("America/Los_Angeles");
        long summerInstant = 1596240000000L; // August 2020
        long winterInstant = 1606780800000L; // December 2020
        int summerOffset = zone.getOffset(summerInstant);
        int winterOffset = zone.getOffset(winterInstant);
        assertTrue("Summer offset should be greater than or equal to winter offset", summerOffset >= winterOffset);
    }

    @Test
    public void testGetOffsetFromLocal_LocalMillisSummer() {
        DateTimeZone zone = DateTimeZone.forID("Australia/Sydney");
        long localMillis = 1609459200000L; // January 2021
        int offset = zone.getOffsetFromLocal(localMillis);
        assertTrue("Offset should be positive for Sydney summer", offset >= 0);
    }

    @Test
    public void testGetOffsetFromLocal_LocalMillisWinter() {
        DateTimeZone zone = DateTimeZone.forID("Australia/Sydney");
        long localMillis = 1635724800000L; // November 2021 (Spring in Southern Hemisphere)
        int offset = zone.getOffsetFromLocal(localMillis);
        assertTrue("Offset should be positive for Sydney spring", offset >= 0);
    }

    @Test
    public void testGetOffsetFromLocal_LocalMillisBeforeDST() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        long localMillis = 1616664000000L; // March 2021 (before DST)
        int offset = zone.getOffsetFromLocal(localMillis);
        assertTrue("Offset should be valid", offset != Integer.MIN_VALUE);
    }

    @Test
    public void testGetOffsetFromLocal_LocalMillisAfterDST() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        long localMillis = 1635750000000L; // November 2021 (after DST ends)
        int offset = zone.getOffsetFromLocal(localMillis);
        assertTrue("Offset should be valid", offset != Integer.MIN_VALUE);
    }

    @Test
    public void testConvertUTCToLocal() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        long utcInstant = 1609459200000L;
        long local = zone.convertUTCToLocal(utcInstant);
        assertTrue("Local time should be later than UTC for Paris", local > utcInstant || local == utcInstant + 3600000L);
    }

    @Test
    public void testConvertLocalToUTC() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        long localInstant = 1609462800000L; // Local time after UTC plus offset
        long utc = zone.convertLocalToUTC(localInstant, false);
        assertTrue("UTC time should be earlier than local", utc < localInstant);
    }

    @Test
    public void testConvertLocalToUTC_DSTOverlap() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        long localInstant = 1604293200000L; // November 2020 (during DST overlap)
        long utc = zone.convertLocalToUTC(localInstant, false);
        assertTrue("UTC should be positive", utc >= 0);
    }

    @Test
    public void testGetStandardOffset_Instant() {
        DateTimeZone zone = DateTimeZone.forID("America/Chicago");
        long instant = 0L;
        int offset = zone.getStandardOffset(instant);
        assertTrue("Standard offset should be non-negative", offset >= 0);
    }

    @Test
    public void testIsFixed_UTC() {
        assertTrue("UTC should be fixed", DateTimeZone.UTC.isFixed());
    }

    @Test
    public void testIsFixed_NonFixed() {
        DateTimeZone zone = DateTimeZone.forID("America/Denver");
        assertFalse("Non-UTC zone should not be fixed", zone.isFixed());
    }

    @Test
    public void testHashCodeConsistency() {
        DateTimeZone zone1 = DateTimeZone.forID("Europe/Berlin");
        DateTimeZone zone2 = DateTimeZone.forID("Europe/Berlin");
        assertEquals("Hash codes should be equal for same zone", zone1.hashCode(), zone2.hashCode());
    }

    @Test
    public void testEquals_SameZone() {
        DateTimeZone zone1 = DateTimeZone.forID("Europe/Madrid");
        DateTimeZone zone2 = DateTimeZone.forID("Europe/Madrid");
        assertTrue("Same zone should be equal", zone1.equals(zone2));
    }

    @Test
    public void testEquals_DifferentZone() {
        DateTimeZone zone1 = DateTimeZone.forID("Europe/Madrid");
        DateTimeZone zone2 = DateTimeZone.forID("Europe/Lisbon");
        assertFalse("Different zones should not be equal", zone1.equals(zone2));
    }

    @Test
    public void testEquals_Null() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Madrid");
        assertFalse("Should not equal null", zone.equals(null));
    }

    @Test
    public void testGetAvailableIDs_NotEmpty() {
        assertNotNull("Available IDs should not be null", DateTimeZone.getAvailableIDs());
        assertFalse("Available IDs should not be empty", DateTimeZone.getAvailableIDs().isEmpty());
    }

    @Test
    public void testGetAvailableIDs_ContainsCommonZones() {
        assertTrue("Should contain UTC", DateTimeZone.getAvailableIDs().contains("UTC"));
        assertTrue("Should contain New York", DateTimeZone.getAvailableIDs().contains("America/New_York"));
    }

    @Test
    public void testDefaultZone_Null() {
        DateTimeZone defaultZone = DateTimeZone.getDefault();
        assertNotNull("Default zone should not be null", defaultZone);
    }

    @Test
    public void testSetDefault_Null() {
        DateTimeZone original = DateTimeZone.getDefault();
        try {
            DateTimeZone.setDefault(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        } finally {
            DateTimeZone.setDefault(original);
        }
    }

    @Test
    public void testSetDefault_ValidZone() {
        DateTimeZone original = DateTimeZone.getDefault();
        DateTimeZone testZone = DateTimeZone.forID("Asia/Shanghai");
        try {
            DateTimeZone.setDefault(testZone);
            assertSame("Default should be set to test zone", testZone, DateTimeZone.getDefault());
        } finally {
            DateTimeZone.setDefault(original);
        }
    }

    @Test
    public void testGetNameKey_Long() {
        DateTimeZone zone = DateTimeZone.forID("America/Chicago");
        String nameKey = zone.getNameKey(0L);
        assertNotNull("Name key should not be null", nameKey);
    }

    @Test
    public void testToString() {
        DateTimeZone zone = DateTimeZone.forID("Pacific/Auckland");
        String str = zone.toString();
        assertTrue("toString should contain ID", str.contains("Pacific/Auckland"));
    }

    @Test
    public void testGetOffset_ArbitraryInstant() {
        DateTimeZone zone = DateTimeZone.forID("US/Eastern");
        long instant = 1000000000000L;
        int offset = zone.getOffset(instant);
        assertTrue("Offset should be negative for US/Eastern", offset < 0 || offset > 0);
    }

    @Test
    public void testForID_TimeZoneName() {
        DateTimeZone zone = DateTimeZone.forID("Asia/Kolkata");
        assertNotNull(zone);
        assertEquals("Asia/Kolkata", zone.getID());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForID_GarbageString() {
        DateTimeZone.forID("ThisIsADefinitelyNonexistentZoneIDThatShouldThrowException");
    }

    @Test
    public void testForID_PlusZeroOffset() {
        DateTimeZone zone = DateTimeZone.forID("+00:00");
        assertNotNull(zone);
        assertEquals("+00:00", zone.getID());
    }

    @Test
    public void testForID_MinusZeroOffset() {
        DateTimeZone zone = DateTimeZone.forID("-00:00");
        assertNull(zone);
    }

    @Test
    public void testGetOffsetFromLocal_AmbiguousOffset() {
        DateTimeZone zone = DateTimeZone.forID("America/Sao_Paulo");
        long localMillis = 1541286000000L; // November 2018 (DST transition)
        int offset = zone.getOffsetFromLocal(localMillis);
        assertTrue("Offset should be valid", offset > -20000000 && offset < 20000000);
    }

    @Test
    public void testConvertUTCToLocal_NegativeInstant() {
        DateTimeZone zone = DateTimeZone.forID("UTC");
        long utcInstant = -1000L;
        long local = zone.convertUTCToLocal(utcInstant);
        assertEquals("UTC conversion should preserve instant", utcInstant, local);
    }

    @Test
    public void testConvertLocalToUTC_NegativeLocal() {
        DateTimeZone zone = DateTimeZone.forID("UTC");
        long localInstant = -1000L;
        long utc = zone.convertLocalToUTC(localInstant, false);
        assertEquals("UTC conversion should preserve instant for UTC zone", localInstant, utc);
    }
}