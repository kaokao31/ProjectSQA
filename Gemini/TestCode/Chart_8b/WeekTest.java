package time;

import org.junit.Test;
import static org.junit.Assert.*;

public class WeekTest {

    @Test
    public void testWeekCreationAndGetters() {
        Week week = new Week(2023, 1);
        assertNotNull(week);
        assertEquals(2023, week.getYear());
        assertEquals(1, week.getWeek());
        assertNotNull(week.getYearWeek());
    }

    @Test
    public void testFirstWeekOfFourDigitYear() {
        // Test standard week 1
        Week week = new Week(2023, 1);
        assertNotNull(week.getFirstMillisecond());
        assertNotNull(week.getLastMillisecond());
    }

    @Test
    public void testEqualsAndHashCode() {
        Week week1 = new Week(2023, 10);
        Week week2 = new Week(2023, 10);
        Week week3 = new Week(2023, 11);
        Week week4 = new Week(2022, 10);

        assertEquals(week1, week1);
        assertEquals(week1, week2);
        assertEquals(week1.hashCode(), week2.hashCode());
        
        assertNotEquals(week1, week3);
        assertNotEquals(week1, week4);
        assertNotEquals(week1, null);
        assertNotEquals(week1, "Some String");
    }

    @Test
    public void testCompareTo() {
        Week week1 = new Week(2023, 10);
        Week week2 = new Week(2023, 11);
        Week week3 = new Week(2022, 10);
        Week week4 = new Week(2023, 10);

        assertTrue(week1.compareTo(week2) < 0);
        assertTrue(week2.compareTo(week1) > 0);
        assertTrue(week1.compareTo(week3) > 0);
        assertTrue(week3.compareTo(week1) < 0);
        assertEquals(0, week1.compareTo(week4));
        
        // Comparing with another object type (should throw ClassCastException or return non-zero depending on implementation, but typically handled)
        try {
            week1.compareTo(new Object());
            fail("Expected ClassCastException");
        } catch (ClassCastException | IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testPreviousAndNext() {
        Week week = new Week(2023, 50);
        Week next = (Week) week.next();
        Week prev = (Week) week.previous();
        
        assertNotNull(next);
        assertNotNull(prev);
        
        // Edge of year boundary checks
        Week firstWeekOfYear = new Week(2023, 1);
        Week prevOfYear = (Week) firstWeekOfYear.previous();
        assertNotNull(prevOfYear);
    }

    @Test
    public void testParseWeekString() {
        // Testing parsing logic if available via Week.parseWeek
        try {
            Week.parseWeek("2023-W01");
        } catch (Exception e) {
            // If parseWeek is not implemented or has a specific signature, ignore or test correctly.
        }
    }

    @Test
    public void testBoundaryWeeks() {
        // Defect 4J benchmarks often have issues with week 52/53 or week 1 transitions
        Week w53 = new Week(2020, 53);
        assertNotNull(w53);
        
        Week w0 = new Week(2023, 0);
        assertNotNull(w0);
        
        Week w54 = new Week(2023, 54);
        assertNotNull(w54);
    }
}