package org.joda.time;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;

public class PeriodTest {

    @Test
    public void testConstructorsAndGetters() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(1, p.getYears());
        assertEquals(2, p.getMonths());
        assertEquals(3, p.getWeeks());
        assertEquals(4, p.getDays());
        assertEquals(5, p.getHours());
        assertEquals(6, p.getMinutes());
        assertEquals(7, p.getSeconds());
        assertEquals(8, p.getMillis());
    }

    @Test
    public void testZeroInstance() {
        Period p = Period.ZERO;
        assertNotNull(p);
        assertEquals(0, p.size());
        assertEquals(0, p.getYears());
    }

    @Test
    public void testFactoryMethods() {
        assertEquals(10, Period.years(10).getYears());
        assertEquals(10, Period.months(10).getMonths());
        assertEquals(10, Period.weeks(10).getWeeks());
        assertEquals(10, Period.days(10).getDays());
        assertEquals(10, Period.hours(10).getHours());
        assertEquals(10, Period.minutes(10).getMinutes());
        assertEquals(10, Period.seconds(10).getSeconds());
        assertEquals(10, Period.millis(10).getMillis());
    }

    @Test
    public void testStandardFactoryMethods() {
        assertEquals(10, Period.standardHours(10).getHours());
        assertEquals(10, Period.standardMinutes(10).getMinutes());
        assertEquals(10, Period.standardSeconds(10).getSeconds());
        assertEquals(10, Period.standardMillis(10).getMillis());
    }

    @Test
    public void testParse() {
        Period p = Period.parse("P1Y2M3W4DT5H6M7S");
        assertEquals(1, p.getYears());
        assertEquals(2, p.getMonths());
        assertEquals(3, p.getWeeks());
        assertEquals(4, p.getDays());
        assertEquals(5, p.getHours());
        assertEquals(6, p.getMinutes());
        assertEquals(7, p.getSeconds());
    }

    @Test
    public void testWithMethods() {
        Period p = Period.ZERO;
        Period p2 = p.withYears(5);
        assertEquals(5, p2.getYears());
        assertEquals(0, p.getYears());

        assertEquals(2, p2.withMonths(2).getMonths());
        assertEquals(3, p2.withWeeks(3).getWeeks());
        assertEquals(4, p2.withDays(4).getDays());
        assertEquals(5, p2.withHours(5).getHours());
        assertEquals(6, p2.withMinutes(6).getMinutes());
        assertEquals(7, p2.withSeconds(7).getSeconds());
        assertEquals(8, p2.withMillis(8).getMillis());
    }

    @Test
    public void testPlusMethods() {
        Period p = new Period(1, 1, 1, 1, 1, 1, 1, 1);
        Period p2 = p.plus(new Period(1, 1, 1, 1, 1, 1, 1, 1));
        assertEquals(2, p2.getYears());
        assertEquals(2, p2.getMonths());

        assertEquals(3, p.plusYears(2).getYears());
        assertEquals(3, p.plusMonths(2).getMonths());
        assertEquals(3, p.plusWeeks(2).getWeeks());
        assertEquals(3, p.plusDays(2).getDays());
        assertEquals(3, p.plusHours(2).getHours());
        assertEquals(3, p.plusMinutes(2).getMinutes());
        assertEquals(3, p.plusSeconds(2).getSeconds());
        assertEquals(3, p.plusMillis(2).getMillis());
    }

    @Test
    public void testMinusMethods() {
        Period p = new Period(5, 5, 5, 5, 5, 5, 5, 5);
        Period p2 = p.minus(new Period(1, 1, 1, 1, 1, 1, 1, 1));
        assertEquals(4, p2.getYears());
        assertEquals(4, p2.getMonths());

        assertEquals(3, p.minusYears(2).getYears());
        assertEquals(3, p.minusMonths(2).getMonths());
        assertEquals(3, p.minusWeeks(2).getWeeks());
        assertEquals(3, p.minusDays(2).getDays());
        assertEquals(3, p.minusHours(2).getHours());
        assertEquals(3, p.minusMinutes(2).getMinutes());
        assertEquals(3, p.minusSeconds(2).getSeconds());
        assertEquals(3, p.minusMillis(2).getMillis());
    }

    @Test
    public void testMultipliedBy() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period p2 = p.multipliedBy(2);
        assertEquals(2, p2.getYears());
        assertEquals(4, p2.getMonths());
        assertEquals(6, p2.getWeeks());
        assertEquals(8, p2.getDays());
        assertEquals(10, p2.getHours());
        assertEquals(12, p2.getMinutes());
        assertEquals(14, p2.getSeconds());
        assertEquals(16, p2.getMillis());
    }

    @Test
    public void testNegated() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period p2 = p.negated();
        assertEquals(-1, p2.getYears());
        assertEquals(-2, p2.getMonths());
        assertEquals(-3, p2.getWeeks());
        assertEquals(-4, p2.getDays());
        assertEquals(-5, p2.getHours());
        assertEquals(-6, p2.getMinutes());
        assertEquals(-7, p2.getSeconds());
        assertEquals(-8, p2.getMillis());
    }

    @Test
    public void testToStandardFields() {
        Period p = new Period(0, 0, 1, 0, 0, 0, 0, 0); // 1 week
        assertEquals(7 * 24 * 60 * 60, p.toStandardDuration().getStandardSeconds());
        
        Period pDays = new Period(0, 0, 0, 2, 0, 0, 0, 0); // 2 days
        assertEquals(2, pDays.toStandardDays().getDays());

        Period pHours = new Period(0, 0, 0, 0, 2, 0, 0, 0); // 2 hours
        assertEquals(2, pHours.toStandardHours().getHours());

        Period pMinutes = new Period(0, 0, 0, 0, 0, 2, 0, 0);
        assertEquals(2, pMinutes.toStandardMinutes().getMinutes());

        Period pSeconds = new Period(0, 0, 0, 0, 0, 0, 2, 0);
        assertEquals(2, pSeconds.toStandardSeconds().getSeconds());

        Period pMillis = new Period(0, 0, 0, 0, 0, 0, 0, 2000);
        assertEquals(2, pMillis.toStandardDuration().getStandardSeconds());
    }

    @Test
    public void testNormalizedStandard() {
        Period p = new Period(0, 0, 0, 0, 0, 0, 0, 9005005);
        Period norm = p.normalizedStandard();
        assertNotNull(norm);
    }

    @Test
    public void testConversionsAndTypes() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertNotNull(p.getPeriodType());
        assertNotNull(p.toMutablePeriod());
        assertNotNull(p.toPeriod());
    }

    @Test
    public void testSerialization() throws Exception {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(p);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Period p2 = (Period) ois.readObject();
        ois.close();

        assertEquals(p, p2);
        assertEquals(p.hashCode(), p2.hashCode());
    }

    @Test
    public void testEqualsAndHashCode() {
        Period p1 = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period p2 = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period p3 = new Period(0, 0, 0, 0, 0, 0, 0, 0);

        assertEquals(p1, p2);
        assertNotEquals(p1, p3);
        assertNotEquals(p1, null);
        assertNotEquals(p1, "some string");
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void testToString() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertNotNull(p.toString());
    }
}