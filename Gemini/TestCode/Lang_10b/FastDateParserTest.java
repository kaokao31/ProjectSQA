package org.apache.commons.lang3.time;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class FastDateParserTest {

    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private static final TimeZone NEW_YORK = TimeZone.getTimeZone("America/New_York");
    private static final Locale US = Locale.US;
    private static final Locale JAPANESE = Locale.JAPANESE;

    private static final String YMD_SLASH = "yyyy/MM/dd";
    private static final String FULL_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS Z";

    private DateParser getInstance(final String format) {
        return getInstance(format, TimeZone.getDefault(), Locale.getDefault());
    }

    private DateParser getInstance(final String format, final TimeZone timeZone) {
        return getInstance(format, timeZone, Locale.getDefault());
    }

    private DateParser getInstance(final String format, final Locale locale) {
        return getInstance(format, TimeZone.getDefault(), locale);
    }

    private DateParser getInstance(final String format, final TimeZone timeZone, final Locale locale) {
        return new FastDateParser(format, timeZone, locale);
    }

    @Test
    public void testLANG_831() throws Exception {
        final FastDateParser fdf = new FastDateParser("M E", TimeZone.getDefault(), Locale.ENGLISH);
        final SimpleDateFormat sdf = new SimpleDateFormat("M E", Locale.ENGLISH);
        final String input = "3  Tue";
        try {
            sdf.parse(input);
            final Date fdfDate = fdf.parse(input);
            assertEquals(sdf.parse(input), fdfDate);
        } catch (final ParseException e) {
            try {
                fdf.parse(input);
                fail("Expected FDF failure, but got date for [" + input + "]");
            } catch (final ParseException pe) {
                // Expected
            }
        }
    }

    @Test
    public void testParsers() throws Exception {
        final DateParser fdp = getInstance(YMD_SLASH);
        final Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2004, Calendar.FEBRUARY, 3);
        final Date expected = cal.getTime();

        final Date actual = fdp.parse("2004/02/03");
        assertEquals(expected, actual);

        final ParsePosition pp = new ParsePosition(0);
        final Date actualFromPP = fdp.parse("2004/02/03", pp);
        assertEquals(expected, actualFromPP);
        assertEquals(10, pp.getIndex());

        final ParsePosition ppObject = new ParsePosition(0);
        final Object actualObject = fdp.parseObject("2004/02/03", ppObject);
        assertEquals(expected, actualObject);
        assertEquals(10, ppObject.getIndex());

        final Object actualObjectFull = fdp.parseObject("2004/02/03");
        assertEquals(expected, actualObjectFull);
    }

    @Test
    public void testParseNull() {
        final DateParser fdp = getInstance(YMD_SLASH);
        try {
            fdp.parse(null);
            fail("Expected NullPointerException");
        } catch (final ParseException e) {
            fail("Expected NullPointerException");
        } catch (final NullPointerException npe) {
            // expected
        }
    }

    @Test(expected = ParseException.class)
    public void testParseInvalidInput() throws ParseException {
        final DateParser fdp = getInstance(YMD_SLASH);
        fdp.parse("invalid date");
    }

    @Test
    public void testParsePositionInvalidInput() {
        final DateParser fdp = getInstance(YMD_SLASH);
        final ParsePosition pp = new ParsePosition(0);
        final Date result = fdp.parse("invalid date", pp);
        assertNull(result);
        assertEquals(0, pp.getIndex());
        assertTrue(pp.getErrorIndex() >= 0);
    }

    @Test
    public void testPatternMatching() throws Exception {
        final String[] patterns = {
                "yyyy-MM-dd HH:mm:ss",
                "yyyy/MM/dd",
                "MM/dd/yyyy",
                "dd.MM.yyyy",
                "yyyyMMddHHmmss",
                "yyyy-DDD",
                "yyyy-w",
                "yyyy-W",
                "yyyy-MM-dd G",
                "K:mm:ss a",
                "k:mm:ss",
                "h:mm:ss a",
                "H:mm:ss",
                "s.S",
                "E, dd MMM yyyy HH:mm:ss Z",
                "yyyy-MM-dd'T'HH:mm:ss.SSS z",
                "''yyyy''"
        };

        final Date now = new Date();

        for (final String pattern : patterns) {
            final SimpleDateFormat sdf = new SimpleDateFormat(pattern, US);
            sdf.setTimeZone(GMT);
            final String dateStr = sdf.format(now);

            final DateParser fdp = getInstance(pattern, GMT, US);
            final Date expected = sdf.parse(dateStr);
            final Date actual = fdp.parse(dateStr);

            assertEquals("Failed for pattern: " + pattern, expected, actual);
        }
    }

    @Test
    public void testLocales() throws Exception {
        final String pattern = "MMMM d, yyyy";
        final SimpleDateFormat sdf = new SimpleDateFormat(pattern, JAPANESE);
        final DateParser fdp = getInstance(pattern, JAPANESE);

        final Date now = new Date();
        final String dateStr = sdf.format(now);

        final Date expected = sdf.parse(dateStr);
        final Date actual = fdp.parse(dateStr);
        assertEquals(expected, actual);
    }

    @Test
    public void testTimeZones() throws Exception {
        final String pattern = "yyyy/MM/dd HH:mm:ss z";

        final FastDateParser fdpNY = new FastDateParser(pattern, NEW_YORK, US);
        final SimpleDateFormat sdfNY = new SimpleDateFormat(pattern, US);
        sdfNY.setTimeZone(NEW_YORK);

        final String dateStrNY = "2020/07/04 12:00:00 EDT";
        final Date expectedNY = sdfNY.parse(dateStrNY);
        final Date actualNY = fdpNY.parse(dateStrNY);
        assertEquals(expectedNY, actualNY);

        final FastDateParser fdpGMT = new FastDateParser(pattern, GMT, US);
        final SimpleDateFormat sdfGMT = new SimpleDateFormat(pattern, US);
        sdfGMT.setTimeZone(GMT);

        final String dateStrGMT = "2020/07/04 12:00:00 GMT";
        final Date expectedGMT = sdfGMT.parse(dateStrGMT);
        final Date actualGMT = fdpGMT.parse(dateStrGMT);
        assertEquals(expectedGMT, actualGMT);
    }

    @Test
    public void testQuotesAndSpecialCharacters() throws Exception {
        final String pattern = "''yyyy'年'MM'月'dd'日'''";
        final DateParser fdp = getInstance(pattern, US);
        final SimpleDateFormat sdf = new SimpleDateFormat(pattern, US);

        final Date now = new Date();
        final String dateStr = sdf.format(now);
        assertEquals(sdf.parse(dateStr), fdp.parse(dateStr));
    }

    @Test
    public void testTwoDigitYear() throws Exception {
        final String pattern = "yy/MM/dd";
        final FastDateParser fdp = new FastDateParser(pattern, GMT, US);
        final SimpleDateFormat sdf = new SimpleDateFormat(pattern, US);
        sdf.setTimeZone(GMT);

        final String dateStr = "20/01/01";
        final Date expected = sdf.parse(dateStr);
        final Date actual = fdp.parse(dateStr);
        assertEquals(expected, actual);
    }

    @Test
    public void testCenturyAdjustment() throws Exception {
        final Calendar cal = Calendar.getInstance(GMT, US);
        cal.clear();
        cal.set(1980, Calendar.JANUARY, 1);
        final Date definedCenturyStart = cal.getTime();

        final FastDateParser fdp = new FastDateParser("yy-MM-dd", GMT, US, definedCenturyStart);
        final Date parsedDate = fdp.parse("85-05-20");

        cal.clear();
        cal.set(1985, Calendar.MAY, 20);
        assertEquals(cal.getTime(), parsedDate);

        final Date parsedDate2 = fdp.parse("15-05-20");
        cal.clear();
        cal.set(2015, Calendar.MAY, 20);
        assertEquals(cal.getTime(), parsedDate2);
    }

    @Test
    public void testEqualsAndHashCodeAndToString() {
        final FastDateParser parser1 = new FastDateParser(YMD_SLASH, GMT, US);
        final FastDateParser parser2 = new FastDateParser(YMD_SLASH, GMT, US);
        final FastDateParser parserDiffPattern = new FastDateParser("yyyy-MM-dd", GMT, US);
        final FastDateParser parserDiffZone = new FastDateParser(YMD_SLASH, NEW_YORK, US);
        final FastDateParser parserDiffLocale = new FastDateParser(YMD_SLASH, GMT, Locale.GERMANY);

        assertEquals(parser1, parser2);
        assertEquals(parser1.hashCode(), parser2.hashCode());

        assertFalse(parser1.equals(null));
        assertFalse(parser1.equals("A String"));
        assertFalse(parser1.equals(parserDiffPattern));
        assertFalse(parser1.equals(parserDiffZone));
        assertFalse(parser1.equals(parserDiffLocale));

        assertEquals(YMD_SLASH, parser1.getPattern());
        assertEquals(GMT, parser1.getTimeZone());
        assertEquals(US, parser1.getLocale());

        final String str = parser1.toString();
        assertTrue(str.contains(YMD_SLASH));
        assertTrue(str.contains("GMT"));
        assertTrue(str.contains("en_US") || str.contains("en"));
    }

    @Test
    public void testSerialization() throws Exception {
        final FastDateParser parser = new FastDateParser(FULL_FORMAT, GMT, US);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(parser);
        oos.close();

        final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        final FastDateParser deserialized = (FastDateParser) ois.readObject();
        ois.close();

        assertEquals(parser, deserialized);

        final String testDate = "2021-12-31T23:59:59.999 +0000";
        assertEquals(parser.parse(testDate), deserialized.parse(testDate));
    }

    @Test
    public void testDayOfWeekInMonth() throws Exception {
        final String pattern = "yyyy-MM F";
        final FastDateParser parser = new FastDateParser(pattern, GMT, US);
        final SimpleDateFormat sdf = new SimpleDateFormat(pattern, US);
        sdf.setTimeZone(GMT);

        final String dateStr = "2021-03 2";
        assertEquals(sdf.parse(dateStr), parser.parse(dateStr));
    }

    @Test
    public void testDayOfYear() throws Exception {
        final String pattern = "yyyy-D";
        final FastDateParser parser = new FastDateParser(pattern, GMT, US);
        final SimpleDateFormat sdf = new SimpleDateFormat(pattern, US);
        sdf.setTimeZone(GMT);

        final String dateStr = "2021-150";
        assertEquals(sdf.parse(dateStr), parser.parse(dateStr));
    }

    @Test
    public void testAmPmParsing() throws Exception {
        final String pattern = "yyyy-MM-dd K:mm a";
        final FastDateParser parser = new FastDateParser(pattern, GMT, US);
        final SimpleDateFormat sdf = new SimpleDateFormat(pattern, US);
        sdf.setTimeZone(GMT);

        final String amDate = "2021-05-10 10:30 AM";
        final String pmDate = "2021-05-10 10:30 PM";

        assertEquals(sdf.parse(amDate), parser.parse(amDate));
        assertEquals(sdf.parse(pmDate), parser.parse(pmDate));
    }

    @Test
    public void testShortAndLongMonths() throws Exception {
        final String shortPattern = "yyyy-MMM-dd";
        final FastDateParser shortParser = new FastDateParser(shortPattern, GMT, US);
        final SimpleDateFormat shortSdf = new SimpleDateFormat(shortPattern, US);
        shortSdf.setTimeZone(GMT);

        final String longPattern = "yyyy-MMMM-dd";
        final FastDateParser longParser = new FastDateParser(longPattern, GMT, US);
        final SimpleDateFormat longSdf = new SimpleDateFormat(longPattern, US);
        longSdf.setTimeZone(GMT);

        final String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        final String[] fullMonthNames = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

        for (int i = 0; i < 12; i++) {
            final String shortStr = "2020-" + monthNames[i] + "-15";
            assertEquals(shortSdf.parse(shortStr), shortParser.parse(shortStr));

            final String longStr = "2020-" + fullMonthNames[i] + "-15";
            assertEquals(longSdf.parse(longStr), longParser.parse(longStr));
        }
    }

    @Test
    public void testEraParsing() throws Exception {
        final String pattern = "yyyy-MM-dd G";
        final FastDateParser parser = new FastDateParser(pattern, GMT, US);
        final SimpleDateFormat sdf = new SimpleDateFormat(pattern, US);
        sdf.setTimeZone(GMT);

        final String adDate = "2020-01-01 AD";
        final String bcDate = "0050-01-01 BC";

        assertEquals(sdf.parse(adDate), parser.parse(adDate));
        assertEquals(sdf.parse(bcDate), parser.parse(bcDate));
    }

    @Test
    public void testTimeZonesWithStrategy() throws Exception {
        final String pattern = "yyyy-MM-dd HH:mm:ss z";
        final FastDateParser parser = new FastDateParser(pattern, GMT, US);

        final Date d1 = parser.parse("2021-01-01 12:00:00 GMT+02:00");
        final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+02:00"));
        cal.clear();
        cal.set(2021, Calendar.JANUARY, 1, 12, 0, 0);
        assertEquals(cal.getTime(), d1);
    }
}