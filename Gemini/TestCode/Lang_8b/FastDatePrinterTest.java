package org.apache.commons.lang3.time;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class FastDatePrinterTest {

    private static final String YYYY_MM_DD = "yyyy-MM-dd";
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private static final TimeZone NEW_YORK = TimeZone.getTimeZone("America/New_York");
    private static final Locale US = Locale.US;
    private static final Locale GERMANY = Locale.GERMANY;
    private static final Locale JAPAN = Locale.JAPAN;

    private Calendar cal;
    private Date testDate;

    @Before
    public void setUp() {
        cal = Calendar.getInstance(UTC, US);
        cal.clear();
        cal.set(2023, Calendar.MARCH, 15, 13, 45, 30);
        cal.set(Calendar.MILLISECOND, 789);
        testDate = cal.getTime();
    }

    private FastDatePrinter getInstance(String pattern) {
        return new FastDatePrinter(pattern, UTC, US);
    }

    private FastDatePrinter getInstance(String pattern, TimeZone timeZone) {
        return new FastDatePrinter(pattern, timeZone, US);
    }

    private FastDatePrinter getInstance(String pattern, Locale locale) {
        return new FastDatePrinter(pattern, UTC, locale);
    }

    private FastDatePrinter getInstance(String pattern, TimeZone timeZone, Locale locale) {
        return new FastDatePrinter(pattern, timeZone, locale);
    }

    @Test
    public void testBasicPatterns() {
        FastDatePrinter printer = getInstance(YYYY_MM_DD);
        assertEquals("2023-03-15", printer.format(testDate));
        assertEquals("2023-03-15", printer.format(cal));
        assertEquals("2023-03-15", printer.format(testDate.getTime()));

        StringBuffer sb = new StringBuffer();
        assertEquals("2023-03-15", printer.format(testDate, sb).toString());

        StringBuilder sbuilder = new StringBuilder();
        assertEquals("2023-03-15", printer.format(testDate, sbuilder).toString());
    }

    @Test
    public void testPatternYearFormatting() {
        // yy - TwoDigitYearField
        FastDatePrinter printerYY = getInstance("yy");
        assertEquals("23", printerYY.format(testDate));

        // yyyy - PaddedNumberField
        FastDatePrinter printerYYYY = getInstance("yyyy");
        assertEquals("2023", printerYYYY.format(testDate));

        // y - NumberRule / Unpadded
        FastDatePrinter printerY = getInstance("y");
        assertEquals("2023", printerY.format(testDate));

        // Boundary year values
        Calendar c = Calendar.getInstance(UTC, US);
        c.clear();
        c.set(Calendar.YEAR, 7);
        assertEquals("07", printerYY.format(c));
        assertEquals("0007", printerYYYY.format(c));
        assertEquals("7", printerY.format(c));

        c.set(Calendar.YEAR, 2000);
        assertEquals("00", printerYY.format(c));

        c.set(Calendar.YEAR, 1999);
        assertEquals("99", printerYY.format(c));
    }

    @Test
    public void testPatternMonthFormatting() {
        // M - UnpaddedMonthField
        FastDatePrinter pM = getInstance("M");
        assertEquals("3", pM.format(testDate));

        // MM - TwoDigitMonthField
        FastDatePrinter pMM = getInstance("MM");
        assertEquals("03", pMM.format(testDate));

        // MMM - short month name
        FastDatePrinter pMMM = getInstance("MMM");
        assertEquals("Mar", pMMM.format(testDate));

        // MMMM - long month name
        FastDatePrinter pMMMM = getInstance("MMMM");
        assertEquals("March", pMMMM.format(testDate));

        Calendar c = Calendar.getInstance(UTC, US);
        c.clear();
        c.set(2023, Calendar.OCTOBER, 1);
        assertEquals("10", pM.format(c));
        assertEquals("10", pMM.format(c));
        assertEquals("Oct", pMMM.format(c));
        assertEquals("October", pMMMM.format(c));
    }

    @Test
    public void testPatternEraFormatting() {
        FastDatePrinter pG = getInstance("G");
        assertEquals("AD", pG.format(testDate));

        FastDatePrinter pGGGG = getInstance("GGGG");
        assertEquals("Anno Domini", pGGGG.format(testDate));

        Calendar bcCal = Calendar.getInstance(UTC, US);
        bcCal.clear();
        bcCal.set(Calendar.ERA, GregorianCalendar.BC);
        bcCal.set(Calendar.YEAR, 50);
        assertEquals("BC", pG.format(bcCal));
        assertEquals("Before Christ", pGGGG.format(bcCal));
    }

    @Test
    public void testPatternDayOfMonthAndYear() {
        FastDatePrinter pD = getInstance("d");
        assertEquals("15", pD.format(testDate));

        FastDatePrinter pDD = getInstance("dd");
        assertEquals("15", pDD.format(testDate));

        FastDatePrinter pDayOfYear = getInstance("D");
        assertEquals(String.valueOf(cal.get(Calendar.DAY_OF_YEAR)), pDayOfYear.format(testDate));

        FastDatePrinter pDayOfYearPadded = getInstance("DDD");
        String formattedDayOfYear = pDayOfYearPadded.format(testDate);
        assertTrue(formattedDayOfYear.length() >= 3);

        FastDatePrinter pDayOfWeekInMonth = getInstance("F");
        assertEquals(String.valueOf(cal.get(Calendar.DAY_OF_WEEK_IN_MONTH)), pDayOfWeekInMonth.format(testDate));
    }

    @Test
    public void testPatternHourFormatting() {
        // H: 0-23
        FastDatePrinter pH = getInstance("H");
        FastDatePrinter pHH = getInstance("HH");
        assertEquals("13", pH.format(testDate));
        assertEquals("13", pHH.format(testDate));

        // k: 1-24 (TwentyFourHourField)
        FastDatePrinter pk = getInstance("k");
        FastDatePrinter pkk = getInstance("kk");
        assertEquals("13", pk.format(testDate));
        assertEquals("13", pkk.format(testDate));

        // K: 0-11
        FastDatePrinter pK = getInstance("K");
        FastDatePrinter pKK = getInstance("KK");
        assertEquals("1", pK.format(testDate));
        assertEquals("01", pKK.format(testDate));

        // h: 1-12 (TwelveHourField)
        FastDatePrinter ph = getInstance("h");
        FastDatePrinter phh = getInstance("hh");
        assertEquals("1", ph.format(testDate));
        assertEquals("01", phh.format(testDate));

        // Test midnight (hour = 0)
        Calendar midnightCal = Calendar.getInstance(UTC, US);
        midnightCal.clear();
        midnightCal.set(2023, Calendar.MARCH, 15, 0, 0, 0);

        assertEquals("0", pH.format(midnightCal));
        assertEquals("00", pHH.format(midnightCal));
        assertEquals("24", pk.format(midnightCal));
        assertEquals("24", pkk.format(midnightCal));
        assertEquals("0", pK.format(midnightCal));
        assertEquals("00", pKK.format(midnightCal));
        assertEquals("12", ph.format(midnightCal));
        assertEquals("12", phh.format(midnightCal));

        // Test noon (hour = 12)
        Calendar noonCal = Calendar.getInstance(UTC, US);
        noonCal.clear();
        noonCal.set(2023, Calendar.MARCH, 15, 12, 0, 0);

        assertEquals("12", pH.format(noonCal));
        assertEquals("12", pHH.format(noonCal));
        assertEquals("12", pk.format(noonCal));
        assertEquals("12", pkk.format(noonCal));
        assertEquals("0", pK.format(noonCal));
        assertEquals("00", pKK.format(noonCal));
        assertEquals("12", ph.format(noonCal));
        assertEquals("12", phh.format(noonCal));
    }

    @Test
    public void testPatternMinutesSecondsMilliseconds() {
        FastDatePrinter pm = getInstance("m");
        FastDatePrinter pmm = getInstance("mm");
        assertEquals("45", pm.format(testDate));
        assertEquals("45", pmm.format(testDate));

        FastDatePrinter ps = getInstance("s");
        FastDatePrinter pss = getInstance("ss");
        assertEquals("30", ps.format(testDate));
        assertEquals("30", pss.format(testDate));

        FastDatePrinter pS = getInstance("S");
        FastDatePrinter pSSS = getInstance("SSS");
        assertEquals("789", pS.format(testDate));
        assertEquals("789", pSSS.format(testDate));

        Calendar smallValues = Calendar.getInstance(UTC, US);
        smallValues.clear();
        smallValues.set(2023, Calendar.MARCH, 15, 1, 5, 4);
        smallValues.set(Calendar.MILLISECOND, 7);

        assertEquals("5", pm.format(smallValues));
        assertEquals("05", pmm.format(smallValues));
        assertEquals("4", ps.format(smallValues));
        assertEquals("04", pss.format(smallValues));
        assertEquals("7", pS.format(smallValues));
        assertEquals("007", pSSS.format(smallValues));
    }

    @Test
    public void testPatternAmPm() {
        FastDatePrinter pa = getInstance("a");
        assertEquals("PM", pa.format(testDate));

        Calendar morningCal = Calendar.getInstance(UTC, US);
        morningCal.clear();
        morningCal.set(2023, Calendar.MARCH, 15, 9, 0, 0);
        assertEquals("AM", pa.format(morningCal));
    }

    @Test
    public void testPatternDayOfWeek() {
        FastDatePrinter pE = getInstance("E");
        FastDatePrinter pEEE = getInstance("EEE");
        FastDatePrinter pEEEE = getInstance("EEEE");

        assertEquals("Wed", pE.format(testDate));
        assertEquals("Wed", pEEE.format(testDate));
        assertEquals("Wednesday", pEEEE.format(testDate));
    }

    @Test
    public void testPatternWeekInYearAndMonth() {
        FastDatePrinter pw = getInstance("w");
        FastDatePrinter pww = getInstance("ww");
        FastDatePrinter pW = getInstance("W");

        assertEquals(String.valueOf(cal.get(Calendar.WEEK_OF_YEAR)), pw.format(testDate));
        String formattedWW = pww.format(testDate);
        assertTrue(formattedWW.length() >= 2);
        assertEquals(String.valueOf(cal.get(Calendar.WEEK_OF_MONTH)), pW.format(testDate));
    }

    @Test
    public void testPatternLiteralsAndQuotes() {
        FastDatePrinter printer = getInstance("'Today is' EEEE, MMMM d, yyyy 'at' h:mm a");
        assertEquals("Today is Wednesday, March 15, 2023 at 1:45 PM", printer.format(testDate));

        FastDatePrinter escapedQuotes = getInstance("''yyyy''");
        assertEquals("'2023'", escapedQuotes.format(testDate));

        FastDatePrinter singleCharLiterals = getInstance("yyyy-MM-dd'T'HH:mm:ss");
        assertEquals("2023-03-15T13:45:30", singleCharLiterals.format(testDate));

        FastDatePrinter quotedWord = getInstance("'hello' yyyy 'world'");
        assertEquals("hello 2023 world", quotedWord.format(testDate));
    }

    @Test
    public void testPatternTimeZone() {
        FastDatePrinter pz = getInstance("z", UTC);
        assertEquals("UTC", pz.format(testDate));

        FastDatePrinter pzzzz = getInstance("zzzz", UTC);
        assertEquals("Coordinated Universal Time", pzzzz.format(testDate));

        FastDatePrinter pZ = getInstance("Z", UTC);
        assertEquals("+0000", pZ.format(testDate));

        FastDatePrinter pZZ = getInstance("ZZ", UTC);
        assertEquals("+00:00", pZZ.format(testDate));

        FastDatePrinter pZZZ = getInstance("ZZZ", UTC);
        assertEquals("UTC", pZZZ.format(testDate));

        // TimeZone with offset: America/New_York (EDT in March, UTC-4)
        FastDatePrinter pZNy = getInstance("Z", NEW_YORK);
        FastDatePrinter pZZNy = getInstance("ZZ", NEW_YORK);
        FastDatePrinter pzNy = getInstance("z", NEW_YORK);
        assertEquals("-0400", pZNy.format(testDate));
        assertEquals("-04:00", pZZNy.format(testDate));
        assertEquals("EDT", pzNy.format(testDate));
    }

    @Test
    public void testPatternIso8601TimeZones() {
        FastDatePrinter pX1 = getInstance("X", UTC);
        FastDatePrinter pX2 = getInstance("XX", UTC);
        FastDatePrinter pX3 = getInstance("XXX", UTC);

        assertEquals("Z", pX1.format(testDate));
        assertEquals("Z", pX2.format(testDate));
        assertEquals("Z", pX3.format(testDate));

        FastDatePrinter pX1Ny = getInstance("X", NEW_YORK);
        FastDatePrinter pX2Ny = getInstance("XX", NEW_YORK);
        FastDatePrinter pX3Ny = getInstance("XXX", NEW_YORK);

        assertEquals("-04", pX1Ny.format(testDate));
        assertEquals("-0400", pX2Ny.format(testDate));
        assertEquals("-04:00", pX3Ny.format(testDate));

        // Test with a positive timezone offset (e.g., Japan +09:00)
        FastDatePrinter pX1Jp = getInstance("X", JAPAN != null ? TimeZone.getTimeZone("Asia/Tokyo") : UTC);
        FastDatePrinter pX2Jp = getInstance("XX", JAPAN != null ? TimeZone.getTimeZone("Asia/Tokyo") : UTC);
        FastDatePrinter pX3Jp = getInstance("XXX", JAPAN != null ? TimeZone.getTimeZone("Asia/Tokyo") : UTC);

        assertEquals("+09", pX1Jp.format(testDate));
        assertEquals("+0900", pX2Jp.format(testDate));
        assertEquals("+09:00", pX3Jp.format(testDate));
    }

    @Test
    public void testLocaleFormatting() {
        FastDatePrinter printerDe = getInstance("EEEE, d. MMMM yyyy", GERMANY);
        assertEquals("Mittwoch, 15. März 2023", printerDe.format(testDate));

        FastDatePrinter printerJa = getInstance("yyyy年M月d日(E)", Locale.JAPANESE);
        assertEquals("2023年3月15日(水)", printerJa.format(testDate));
    }

    @Test
    public void testEqualsAndHashCode() {
        FastDatePrinter p1 = new FastDatePrinter(YYYY_MM_DD, UTC, US);
        FastDatePrinter p2 = new FastDatePrinter(YYYY_MM_DD, UTC, US);
        FastDatePrinter p3 = new FastDatePrinter("yyyy/MM/dd", UTC, US);
        FastDatePrinter p4 = new FastDatePrinter(YYYY_MM_DD, NEW_YORK, US);
        FastDatePrinter p5 = new FastDatePrinter(YYYY_MM_DD, UTC, GERMANY);

        assertTrue(p1.equals(p1));
        assertTrue(p1.equals(p2));
        assertEquals(p1.hashCode(), p2.hashCode());

        assertFalse(p1.equals(null));
        assertFalse(p1.equals("A String"));
        assertFalse(p1.equals(p3));
        assertFalse(p1.equals(p4));
        assertFalse(p1.equals(p5));
    }

    @Test
    public void testGettersAndToString() {
        FastDatePrinter printer = new FastDatePrinter(YYYY_MM_DD, UTC, US);
        assertEquals(YYYY_MM_DD, printer.getPattern());
        assertEquals(UTC, printer.getTimeZone());
        assertEquals(US, printer.getLocale());
        assertTrue(printer.getMaxLengthEstimate() > 0);

        String toString = printer.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("FastDatePrinter["));
        assertTrue(toString.contains(YYYY_MM_DD));
        assertTrue(toString.contains("UTC"));
        assertTrue(toString.contains("en_US") || toString.contains("en"));
    }

    @Test
    public void testFormatAppendable() {
        FastDatePrinter printer = getInstance(YYYY_MM_DD);

        StringBuilder sb = new StringBuilder("Prefix: ");
        Appendable result = printer.format(testDate, sb);
        assertEquals("Prefix: 2023-03-15", result.toString());

        StringBuffer sbuf = new StringBuffer("Prefix: ");
        Appendable result2 = printer.format(cal, sbuf);
        assertEquals("Prefix: 2023-03-15", result2.toString());

        StringBuilder sbMillis = new StringBuilder("Prefix: ");
        Appendable result3 = printer.format(testDate.getTime(), sbMillis);
        assertEquals("Prefix: 2023-03-15", result3.toString());
    }

    @Test
    public void testSerialization() throws Exception {
        FastDatePrinter printer = new FastDatePrinter("yyyy-MM-dd HH:mm:ss.SSS Z", UTC, US);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(printer);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        FastDatePrinter deserialized = (FastDatePrinter) ois.readObject();
        ois.close();

        assertEquals(printer, deserialized);
        assertEquals(printer.format(testDate), deserialized.format(testDate));
    }

    @Test
    public void testComplexPatternsMatchingSimpleDateFormat() {
        String[] patterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                "EEEE, MMMM d, yyyy G",
                "h:mm:ss a z",
                "K:mm:ss a",
                "k:mm:ss",
                "w W D d F E",
                "yy/M/d H:m:s"
        };

        for (String pattern : patterns) {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern, US);
            sdf.setTimeZone(UTC);
            FastDatePrinter fdp = new FastDatePrinter(pattern, UTC, US);

            String expected = sdf.format(testDate);
            String actual = fdp.format(testDate);
            assertEquals("Pattern mismatch for: " + pattern, expected, actual);
        }
    }

    @Test
    public void testCustomPaddedNumberField() {
        FastDatePrinter printer = getInstance("yyyyy-MMMMM-ddddd");
        String formatted = printer.format(testDate);
        assertEquals("02023-March-00015", formatted);
    }

    @Test
    public void testInvalidPatternThrowsException() {
        try {
            new FastDatePrinter("yyyy-MM-dd B", UTC, US);
            fail("Expected IllegalArgumentException for illegal pattern character");
        } catch (IllegalArgumentException expected) {
            // Success
        }

        try {
            new FastDatePrinter("yyyy 'unclosed quote", UTC, US);
            fail("Expected IllegalArgumentException for unclosed quote");
        } catch (IllegalArgumentException expected) {
            // Success
        }
    }

    @Test
    public void testNullPointerExceptionOnNullArguments() {
        try {
            new FastDatePrinter(null, UTC, US);
            fail("Expected NullPointerException for null pattern");
        } catch (NullPointerException expected) {
            // Success
        }

        try {
            new FastDatePrinter(YYYY_MM_DD, null, US);
            fail("Expected NullPointerException for null timeZone");
        } catch (NullPointerException expected) {
            // Success
        }

        try {
            new FastDatePrinter(YYYY_MM_DD, UTC, null);
            fail("Expected NullPointerException for null locale");
        } catch (NullPointerException expected) {
            // Success
        }
    }

    @Test
    public void testTimeZoneCaching() {
        FastDatePrinter p1 = getInstance("zzzz", NEW_YORK);
        FastDatePrinter p2 = getInstance("zzzz", NEW_YORK);
        assertEquals(p1.format(testDate), p2.format(testDate));

        FastDatePrinter p3 = getInstance("z", NEW_YORK);
        FastDatePrinter p4 = getInstance("z", NEW_YORK);
        assertEquals(p3.format(testDate), p4.format(testDate));
    }
}