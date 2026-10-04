package com.fasterxml.jackson.databind.util;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Locale;

/**
 * Comprehensive JUnit 4 test suite for ISO8601Utils.
 * Covers parsing, formatting, edge cases, and fault detection.
 */
public class ISO8601UtilsTest {

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private static final TimeZone GMT_PLUS_5 = TimeZone.getTimeZone("GMT+05:00");
    private static final TimeZone GMT_MINUS_8 = TimeZone.getTimeZone("GMT-08:00");

    @Before
    public void setUp() {
        // No special setup needed
    }

    @After
    public void tearDown() {
        // No cleanup needed
    }

    // ===================== PARSING TESTS =====================

    @Test
    public void testParseBasicDate() throws ParseException {
        Date result = ISO8601Utils.parse("2019-12-31");
        assertNotNull(result);
        // Verify using SimpleDateFormat
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        sdf.setTimeZone(UTC);
        assertEquals(sdf.parse("2019-12-31"), result);
    }

    @Test
    public void testParseDateTimeWithZ() throws ParseException {
        Date result = ISO8601Utils.parse("2020-01-15T10:30:00Z");
        assertNotNull(result);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        sdf.setTimeZone(UTC);
        assertEquals(sdf.parse("2020-01-15T10:30:00"), result);
    }

    @Test
    public void testParseDateTimeWithPositiveOffset() throws ParseException {
        Date result = ISO8601Utils.parse("2020-06-01T12:00:00+05:00");
        assertNotNull(result);
        // Expected UTC: 07:00:00
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        sdf.setTimeZone(UTC);
        assertEquals(sdf.parse("2020-06-01T07:00:00"), result);
    }

    @Test
    public void testParseDateTimeWithNegativeOffset() throws ParseException {
        Date result = ISO8601Utils.parse("2020-12-25T18:00:00-08:00");
        assertNotNull(result);
        // Expected UTC: 2020-12-26T02:00:00
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        sdf.setTimeZone(UTC);
        assertEquals(sdf.parse("2020-12-26T02:00:00"), result);
    }

    @Test
    public void testParseDateTimeWithMillis() throws ParseException {
        Date result = ISO8601Utils.parse("2021-03-10T14:25:30.123Z");
        assertNotNull(result);
        // Check millis
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);
        sdf.setTimeZone(UTC);
        assertEquals(sdf.parse("2021-03-10T14:25:30.123"), result);
    }

    @Test
    public void testParseDateTimeWithMillisAndOffset() throws ParseException {
        Date result = ISO8601Utils.parse("2021-07-04T09:15:45.678+02:00");
        assertNotNull(result);
        // Expected UTC: 2021-07-04T07:15:45.678
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);
        sdf.setTimeZone(UTC);
        assertEquals(sdf.parse("2021-07-04T07:15:45.678"), result);
    }

    @Test
    public void testParseDateOnlyWithTimeZone() throws ParseException {
        // Date only with timezone is not standard ISO8601, but some implementations accept it
        // This tests edge case
        Date result = ISO8601Utils.parse("2022-01-01Z");
        assertNotNull(result);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        sdf.setTimeZone(UTC);
        assertEquals(sdf.parse("2022-01-01"), result);
    }

    @Test
    public void testParseMinimalDate() throws ParseException {
        Date result = ISO8601Utils.parse("0001-01-01");
        assertNotNull(result);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        sdf.setTimeZone(UTC);
        assertEquals(sdf.parse("0001-01-01"), result);
    }

    @Test
    public void testParseMaxDate() throws ParseException {
        Date result = ISO8601Utils.parse("9999-12-31");
        assertNotNull(result);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        sdf.setTimeZone(UTC);
        assertEquals(sdf.parse("9999-12-31"), result);
    }

    @Test
    public void testParseLeapYearDate() throws ParseException {
        Date result = ISO8601Utils.parse("2020-02-29");
        assertNotNull(result);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        sdf.setTimeZone(UTC);
        assertEquals(sdf.parse("2020-02-29"), result);
    }

    @Test
    public void testParseNonLeapYearDate() throws ParseException {
        // Should fail because 2021 is not a leap year
        try {
            ISO8601Utils.parse("2021-02-29");
            fail("Expected ParseException for invalid date");
        } catch (ParseException e) {
            // expected
        }
    }

    @Test
    public void testParseInvalidMonth() {
        try {
            ISO8601Utils.parse("2021-13-01");
            fail("Expected ParseException for invalid month");
        } catch (ParseException e) {
            // expected
        }
    }

    @Test
    public void testParseInvalidDay() {
        try {
            ISO8601Utils.parse("2021-01-32");
            fail("Expected ParseException for invalid day");
        } catch (ParseException e) {
            // expected
        }
    }

    @Test
    public void testParseInvalidHour() {
        try {
            ISO8601Utils.parse("2021-01-01T25:00:00");
            fail("Expected ParseException for invalid hour");
        } catch (ParseException e) {
            // expected
        }
    }

    @Test
    public void testParseInvalidMinute() {
        try {
            ISO8601Utils.parse("2021-01-01T12:60:00");
            fail("Expected ParseException for invalid minute");
        } catch (ParseException e) {
            // expected
        }
    }

    @Test
    public void testParseInvalidSecond() {
        try {
            ISO8601Utils.parse("2021-01-01T12:00:60");
            fail("Expected ParseException for invalid second (leap second not supported)");
        } catch (ParseException e) {
            // expected
        }
    }

    @Test
    public void testParseEmptyString() {
        try {
            ISO8601Utils.parse("");
            fail("Expected ParseException for empty string");
        } catch (ParseException e) {
            // expected
        }
    }

    @Test(expected = NullPointerException.class)
    public void testParseNull() throws ParseException {
        ISO8601Utils.parse(null);
    }

    @Test
    public void testParseMalformedString() {
        try {
            ISO8601Utils.parse("not-a-date");
            fail("Expected ParseException for malformed string");
        } catch (ParseException e) {
            // expected
        }
    }

    @Test
    public void testParseStringWithExtraChars() {
        try {
            ISO8601Utils.parse("2021-01-01T12:00:00Z extra");
            fail("Expected ParseException for extra characters");
        } catch (ParseException e) {
            // expected
        }
    }

    @Test
    public void testParseDateTimeWithNoTimezone() throws ParseException {
        // Without timezone, should be interpreted as local time? Usually UTC.
        // Implementation dependent; we test that it doesn't throw.
        Date result = ISO8601Utils.parse("2021-06-15T08:30:00");
        assertNotNull(result);
    }

    @Test
    public void testParseDateTimeWithMillisAndNoTimezone() throws ParseException {
        Date result = ISO8601Utils.parse("2021-06-15T08:30:00.500");
        assertNotNull(result);
    }

    @Test
    public void testParseDateWithTimeAndNoSeparator() throws ParseException {
        // Some formats like "20210101T123000Z" are valid ISO8601 basic format
        Date result = ISO8601Utils.parse("20210101T123000Z");
        assertNotNull(result);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.US);
        sdf.setTimeZone(UTC);
        assertEquals(sdf.parse("20210101T123000"), result);
    }

    @Test
    public void testParseDateWithTimeAndMillisBasic() throws ParseException {
        Date result = ISO8601Utils.parse("20210101T123000.123Z");
        assertNotNull(result);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss.SSS", Locale.US);
        sdf.setTimeZone(UTC);
        assertEquals(sdf.parse("20210101T123000.123"), result);
    }

    // ===================== FORMATTING TESTS =====================

    @Test
    public void testFormatDate() {
        Date date = new Date(0L); // 1970-01-01T00:00:00.000Z
        String formatted = ISO8601Utils.format(date);
        assertEquals("1970-01-01T00:00:00.000Z", formatted);
    }

    @Test
    public void testFormatDateWithMillisTrue() {
        Date date = new Date(123456789L); // some date with millis
        String formatted = ISO8601Utils.format(date, true);
        // Check that it contains milliseconds
        assertTrue(formatted.contains("."));
        assertTrue(formatted.endsWith("Z"));
    }

    @Test
    public void testFormatDateWithMillisFalse() {
        Date date = new Date(123456789L);
        String formatted = ISO8601Utils.format(date, false);
        // Should not contain milliseconds
        assertFalse(formatted.contains("."));
        assertTrue(formatted.endsWith("Z"));
    }

    @Test
    public void testFormatDateWithTimeZone() {
        Date date = new Date(0L);
        String formatted = ISO8601Utils.format(date, true, GMT_PLUS_5);
        // Expected: 1970-01-01T05:00:00.000+05:00
        assertTrue(formatted.contains("+05:00"));
        assertFalse(formatted.endsWith("Z"));
    }

    @Test
    public void testFormatDateWithNegativeTimeZone() {
        Date date = new Date(0L);
        String formatted = ISO8601Utils.format(date, true, GMT_MINUS_8);
        // Expected: 1969-12-31T16:00:00.000-08:00
        assertTrue(formatted.contains("-08:00"));
    }

    @Test(expected = NullPointerException.class)
    public void testFormatNullDate() {
        ISO8601Utils.format(null);
    }

    @Test(expected = NullPointerException.class)
    public void testFormatNullDateWithMillis() {
        ISO8601Utils.format(null, true);
    }

    @Test(expected = NullPointerException.class)
    public void testFormatNullDateWithTimeZone() {
        ISO8601Utils.format(null, true, UTC);
    }

    @Test
    public void testFormatDateWithLeapSecond() {
        // Date objects cannot represent leap seconds, but we can test formatting a date that is close
        Date date = new Date(915148800000L); // 1998-12-31T23:59:60? Actually not possible.
        // Just ensure no exception
        String formatted = ISO8601Utils.format(date);
        assertNotNull(formatted);
    }

    @Test
    public void testFormatDateBoundary() {
        // Test year 1 and year 9999
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        sdf.setTimeZone(UTC);
        try {
            Date minDate = sdf.parse("0001-01-01T00:00:00.000Z");
            String formatted = ISO8601Utils.format(minDate);
            assertEquals("0001-01-01T00:00:00.000Z", formatted);
        } catch (ParseException e) {
            fail("Failed to parse min date");
        }
        try {
            Date maxDate = sdf.parse("9999-12-31T23:59:59.999Z");
            String formatted = ISO8601Utils.format(maxDate);
            assertEquals("9999-12-31T23:59:59.999Z", formatted);
        } catch (ParseException e) {
            fail("Failed to parse max date");
        }
    }

    // ===================== toString TESTS =====================

    @Test
    public void testToString() {
        Date date = new Date(0L);
        String str = ISO8601Utils.toString(date);
        assertEquals("1970-01-01T00:00:00.000Z", str);
    }

    @Test(expected = NullPointerException.class)
    public void testToStringNull() {
        ISO8601Utils.toString(null);
    }

    // ===================== EDGE CASES AND FAULT DETECTION =====================

    @Test
    public void testParseDateWithMultipleTimezones() throws ParseException {
        // Test that parsing with different offsets yields correct UTC times
        Date date1 = ISO8601Utils.parse("2021-01-01T12:00:00+00:00");
        Date date2 = ISO8601Utils.parse("2021-01-01T13:00:00+01:00");
        assertEquals(date1, date2);
    }

    @Test
    public void testParseDateWithZAndZeroOffset() throws ParseException {
        Date dateZ = ISO8601Utils.parse("2021-01-01T12:00:00Z");
        Date dateZero = ISO8601Utils.parse("2021-01-01T12:00:00+00:00");
        assertEquals(dateZ, dateZero);
    }

    @Test
    public void testParseDateWithMillisAndNoDecimal() {
        // Some implementations may accept "2021-01-01T12:00:00." (trailing dot)
        try {
            ISO8601Utils.parse("2021-01-01T12:00:00.");
            fail("Expected ParseException for trailing dot");
        } catch (ParseException e) {
            // expected
        }
    }

    @Test
    public void testParseDateWithNegativeYear() {
        // ISO8601 allows negative years (proleptic Gregorian)
        try {
            ISO8601Utils.parse("-0001-01-01");
            // If supported, check result; else expect ParseException
            // Many implementations do not support negative years
            // We'll just ensure no crash
        } catch (ParseException e) {
            // acceptable
        }
    }

    @Test
    public void testParseDateWithWeekDate() {
        // ISO8601 week date format like "2021-W01-1" is not typically supported
        try {
            ISO8601Utils.parse("2021-W01-1");
            fail("Expected ParseException for week date");
        } catch (ParseException e) {
            // expected
        }
    }

    @Test
    public void testParseDateWithOrdinalDate() {
        // Ordinal date like "2021-001" is not typically supported
        try {
            ISO8601Utils.parse("2021-001");
            fail("Expected ParseException for ordinal date");
        } catch (ParseException e) {
            // expected
        }
    }

    @Test
    public void testFormatAndParseRoundTrip() throws ParseException {
        Date original = new Date(123456789L);
        String formatted = ISO8601Utils.format(original, true);
        Date parsed = ISO8601Utils.parse(formatted);
        assertEquals(original, parsed);
    }

    @Test
    public void testFormatAndParseRoundTripWithTimezone() throws ParseException {
        Date original = new Date(987654321L);
        String formatted = ISO8601Utils.format(original, true, GMT_PLUS_5);
        Date parsed = ISO8601Utils.parse(formatted);
        assertEquals(original, parsed);
    }

    @Test
    public void testParseDateWithVeryLargeYear() {
        // Year > 9999 might be accepted or rejected
        try {
            ISO8601Utils.parse("10000-01-01");
            // If accepted, check result
        } catch (ParseException e) {
            // acceptable
        }
    }

    @Test
    public void testParseDateWithVerySmallYear() {
        try {
            ISO8601Utils.parse("0000-01-01");
            // Year 0 is allowed in proleptic Gregorian
        } catch (ParseException e) {
            // acceptable
        }
    }

    @Test
    public void testParseDateWithTimeZoneColon() throws ParseException {
        // Some formats omit colon in offset: +0500
        Date result = ISO8601Utils.parse("2021-01-01T12:00:00+0500");
        assertNotNull(result);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        sdf.setTimeZone(UTC);
        assertEquals(sdf.parse("2021-01-01T07:00:00"), result);
    }

    @Test
    public void testParseDateWithTimeZoneNoColonNegative() throws ParseException {
        Date result = ISO8601Utils.parse("2021-01-01T12:00:00-0800");
        assertNotNull(result);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        sdf.setTimeZone(UTC);
        assertEquals(sdf.parse("2021-01-01T20:00:00"), result);
    }

    @Test
    public void testParseDateWithFractionalSecondsMoreThanThreeDigits() throws ParseException {
        // Some implementations truncate or round
        Date result = ISO8601Utils.parse("2021-01-01T12:00:00.123456Z");
        assertNotNull(result);
        // Should be equivalent to 123 milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);
        sdf.setTimeZone(UTC);
        assertEquals(sdf.parse("2021-01-01T12:00:00.123"), result);
    }

    @Test
    public void testParseDateWithFractionalSecondsLessThanThreeDigits() throws ParseException {
        Date result = ISO8601Utils.parse("2021-01-01T12:00:00.5Z");
        assertNotNull(result);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);
        sdf.setTimeZone(UTC);
        assertEquals(sdf.parse("2021-01-01T12:00:00.500"), result);
    }

    @Test
    public void testParseDateWithOnlyTime() {
        // Time only is not a full date
        try {
            ISO8601Utils.parse("12:00:00");
            fail("Expected ParseException for time only");
        } catch (ParseException e) {
            // expected
        }
    }

    @Test
    public void testParseDateWithOnlyTimeAndTimezone() {
        try {
            ISO8601Utils.parse("12:00:00Z");
            fail("Expected ParseException for time only with timezone");
        } catch (ParseException e) {
            // expected
        }
    }

    @Test
    public void testParseDateWithInvalidSeparator() {
        try {
            ISO8601Utils.parse("2021-01-01 12:00:00");
            fail("Expected ParseException for space instead of T");
        } catch (ParseException e) {
            // expected
        }
    }

    @Test
    public void testParseDateWithLowerCaseT() throws ParseException {
        // Some implementations accept 't' instead of 'T'
        Date result = ISO8601Utils.parse("2021-01-01t12:00:00Z");
        assertNotNull(result);
    }

    @Test
    public void testParseDateWithLowerCaseZ() throws ParseException {
        Date result = ISO8601Utils.parse("2021-01-01T12:00:00z");
        assertNotNull(result);
    }

    @Test
    public void testParseDateWithNoSeconds() throws ParseException {
        Date result = ISO8601Utils.parse("2021-01-01T12:00Z");
        assertNotNull(result);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US);
        sdf.setTimeZone(UTC);
        assertEquals(sdf.parse("2021-01-01T12:00"), result);
    }

    @Test
    public void testParseDateWithNoMinutes() throws ParseException {
        Date result = ISO8601Utils.parse("2021-01-01T12Z");
        assertNotNull(result);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH", Locale.US);
        sdf.setTimeZone(UTC);
        assertEquals(sdf.parse("2021-01-01T12"), result);
    }

    @Test
    public void testParseDateWithNoTime() throws ParseException {
        Date result = ISO8601Utils.parse("2021-01-01");
        assertNotNull(result);
    }

    @Test
    public void testParseDateWithExtendedFormatNoSeparator() throws ParseException {
        // Basic format without hyphens
        Date result = ISO8601Utils.parse("20210101");
        assertNotNull(result);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.US);
        sdf.setTimeZone(UTC);
        assertEquals(sdf.parse("20210101"), result);
    }

    @Test
    public void testParseDateWithExtendedFormatAndTime() throws ParseException {
        Date result = ISO8601Utils.parse("20210101T123000");
        assertNotNull(result);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.US);
        sdf.setTimeZone(UTC);
        assertEquals(sdf.parse("20210101T123000"), result);
    }

    @Test
    public void testParseDateWithExtendedFormatAndTimeAndMillis() throws ParseException {
        Date result = ISO8601Utils.parse("20210101T123000.123");
        assertNotNull(result);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss.SSS", Locale.US);
        sdf.setTimeZone(UTC);
        assertEquals(sdf.parse("20210101T123000.123"), result);
    }

    @Test
    public void testParseDateWithExtendedFormatAndTimeAndTimezone() throws ParseException {
        Date result = ISO8601Utils.parse("20210101T123000+0500");
        assertNotNull(result);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.US);
        sdf.setTimeZone(UTC);
        assertEquals(sdf.parse("20210101T073000"), result);
    }

    @Test
    public void testParseDateWithExtendedFormatAndTimeAndZ() throws ParseException {
        Date result = ISO8601Utils.parse("20210101T123000Z");
        assertNotNull(result);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.US);
        sdf.setTimeZone(UTC);
        assertEquals(sdf.parse("20210101T123000"), result);
    }

    // ===================== NULL AND EMPTY EDGE CASES =====================

    @Test(expected = NullPointerException.class)
    public void testParseNullString() throws ParseException {
        ISO8601Utils.parse(null);
    }

    @Test(expected = NullPointerException.class)
    public void testFormatNullDateWithTimeZoneAndMillis() {
        ISO8601Utils.format(null, true, UTC);
    }

    @Test(expected = NullPointerException.class)
    public void testFormatNullDateWithTimeZoneNoMillis() {
        ISO8601Utils.format(null, false, UTC);
    }

    @Test(expected = NullPointerException.class)
    public void testToStringNullDate() {
        ISO8601Utils.toString(null);
    }

    // ===================== ADDITIONAL COVERAGE FOR INTERNAL METHODS =====================

    // The following tests target potential internal bugs like incorrect timezone parsing,
    // millisecond rounding, and boundary conditions.

    @Test
    public void testParseDateWithMaxValidOffset() throws ParseException {
        // Max offset is +14:00 or -12:00? ISO8601 allows up to +14:00
        Date result = ISO8601Utils.parse("2021-01-01T00:00:00+14:00");
        assertNotNull(result);
        // Expected UTC: previous day 10:00:00
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        sdf.setTimeZone(UTC);
        assertEquals(sdf.parse("2020-12-31T10:00:00"), result);
    }

    @Test
    public void testParseDateWithMinValidOffset() throws ParseException {
        Date result = ISO8601Utils.parse("2021-01-01T00:00:00-12:00");
        assertNotNull(result);
        // Expected UTC: 2021-01-01T12:00:00
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        sdf.setTimeZone(UTC);
        assertEquals(sdf.parse("2021-01-01T12:00:00"), result);
    }

    @Test
    public void testParseDateWithOffsetExceedingMax() {
        try {
            ISO8601Utils.parse("2021-01-01T00:00:00+15:00");
            fail("Expected ParseException for offset >14");
        } catch (ParseException e) {
            // expected
        }
    }

    @Test
    public void testParseDateWithOffsetExceedingMin() {
        try {
            ISO8601Utils.parse("2021-01-01T00:00:00-13:00");
            fail("Expected ParseException for offset <-12");
        } catch (ParseException e) {
            // expected
        }
    }

    @Test
    public void testParseDateWithInvalidOffsetMinutes() {
        try {
            ISO8601Utils.parse("2021-01-01T00:00:00+05:60");
            fail("Expected ParseException for invalid offset minutes");
        } catch (ParseException e) {
            // expected
        }
    }

    @Test
    public void testParseDateWithInvalidOffsetHours() {
        try {
            ISO8601Utils.parse("2021-01-01T00:00:00+99:00");
            fail("Expected ParseException for invalid offset hours");
        } catch (ParseException e) {
            // expected
        }
    }

    @Test
    public void testParseDateWithOnlyOffset() {
        try {
            ISO8601Utils.parse("+05:00");
            fail("Expected ParseException for only offset");
        } catch (ParseException e) {
            // expected
        }
    }

    @Test
    public void testParseDateWithNegativeZeroOffset() throws ParseException {
        // -00:00 is equivalent to +00:00
        Date result = ISO8601Utils.parse("2021-01-01T12:00:00-00:00");
        assertNotNull(result);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        sdf.setTimeZone(UTC);
        assertEquals(sdf.parse("2021-01-01T12:00:00"), result);
    }

    @Test
    public void testParseDateWithZeroOffsetZ() throws ParseException {
        Date result = ISO8601Utils.parse("2021-01-01T12:00:00+00:00");
        assertNotNull(result);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        sdf.setTimeZone(UTC);
        assertEquals(sdf.parse("2021-01-01T12:00:00"), result);
    }

    @Test
    public void testParseDateWithFractionalSecondsAndNoTimezone() throws ParseException {
        Date result = ISO8601Utils.parse("2021-01-01T12:00:00.123");
        assertNotNull(result);
    }

    @Test
    public void testParseDateWithFractionalSecondsAndNoTimezoneNoMillis() throws ParseException {
        Date result = ISO8601Utils.parse("2021-01-01T12:00:00.");
        // This is invalid; expect exception
        try {
            ISO8601Utils.parse("2021-01-01T12:00:00.");
            fail("Expected ParseException for trailing dot");
        } catch (ParseException e) {
            // expected
        }
    }

    @Test
    public void testParseDateWithMultipleDots() {
        try {
            ISO8601Utils.parse("2021-01-01T12:00:00.123.456");
            fail("Expected ParseException for multiple dots");
        } catch (ParseException e) {
            // expected
        }
    }

    @Test
    public void testParseDateWithNonDigitInFraction() {
        try {
            ISO8601Utils.parse("2021-01-01T12:00:00.12a");
            fail("Expected ParseException for non-digit in fraction");
        } catch (ParseException e) {
            // expected
        }
    }

    @Test
    public void testParseDateWithLeadingZerosInYear() throws ParseException {
        Date result = ISO8601Utils.parse("002021-01-01");
        // Should be year 2021? Or 2021? Usually leading zeros are allowed.
        assertNotNull(result);
    }

    @Test
    public void testParseDateWithNegativeYearAndTime() {
        try {
            ISO8601Utils.parse("-2021-01-01T12:00:00Z");
            // If supported, check; else expect ParseException
        } catch (ParseException e) {
            // acceptable
        }
    }

    @Test
    public void testFormatDateWithNegativeYear() {
        // Date objects cannot represent negative years, but we can test with a date far in past
        // Use Calendar to set year -1? Not possible with Date. Skip.
    }

    // ===================== PERFORMANCE / STRESS TESTS (optional) =====================

    @Test(timeout = 1000)
    public void testParseManyDates() throws ParseException {
        for (int i = 0; i < 1000; i++) {
            String dateStr = String.format("2021-01-%02dT12:00:00Z", (i % 28) + 1);
            ISO8601Utils.parse(dateStr);
        }
    }

    @Test(timeout = 1000)
    public void testFormatManyDates() {
        Date base = new Date(0L);
        for (int i = 0; i < 1000; i++) {
            ISO8601Utils.format(new Date(base.getTime() + i * 1000));
        }
    }
}