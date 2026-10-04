package org.joda.time.field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.joda.time.DurationField;
import org.joda.time.DurationFieldType;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link UnsupportedDurationField}.
 * Generated to achieve maximum coverage and fault detection.
 */
public class UnsupportedDurationFieldTest {

    private DurationFieldType type;
    private UnsupportedDurationField field;

    @Before
    public void setUp() {
        type = DurationFieldType.years();
        field = UnsupportedDurationField.getInstance(type);
    }

    // -----------------------------------------------------------------------
    @Test
    public void testGetInstance() {
        assertNotNull(field);
        assertSame(field, UnsupportedDurationField.getInstance(type));
        // Different type should return different instance
        DurationFieldType months = DurationFieldType.months();
        assertFalse(field == UnsupportedDurationField.getInstance(months));
    }

    @Test(expected = NullPointerException.class)
    public void testGetInstanceNull() {
        UnsupportedDurationField.getInstance(null);
    }

    // -----------------------------------------------------------------------
    @Test
    public void testGetType() {
        assertSame(type, field.getType());
    }

    @Test
    public void testGetName() {
        assertEquals(type.getName(), field.getName());
    }

    // -----------------------------------------------------------------------
    @Test
    public void testIsSupported() {
        assertFalse(field.isSupported());
    }

    // -----------------------------------------------------------------------
    @Test
    public void testIsPrecise() {
        assertTrue(field.isPrecise());
    }

    // -----------------------------------------------------------------------
    @Test
    public void testGetMillisUnit() {
        assertEquals(0L, field.getUnitMillis());
    }

    // -----------------------------------------------------------------------
    @Test
    public void testGetMillisInt() {
        assertEquals(0L, field.getMillis(1));
        assertEquals(0L, field.getMillis(0));
        assertEquals(0L, field.getMillis(-1));
        assertEquals(0L, field.getMillis(Integer.MAX_VALUE));
        assertEquals(0L, field.getMillis(Integer.MIN_VALUE));
    }

    @Test
    public void testGetMillisLong() {
        assertEquals(0L, field.getMillis(1L));
        assertEquals(0L, field.getMillis(0L));
        assertEquals(0L, field.getMillis(-1L));
        assertEquals(0L, field.getMillis(Long.MAX_VALUE));
        assertEquals(0L, field.getMillis(Long.MIN_VALUE));
    }

    // -----------------------------------------------------------------------
    @Test
    public void testAddInt() {
        assertEquals(0, field.add(0, 1));
        assertEquals(0, field.add(0, 0));
        assertEquals(0, field.add(0, -1));
        assertEquals(0, field.add(5, 3));
        assertEquals(0, field.add(-5, 3));
        assertEquals(0, field.add(Integer.MAX_VALUE, 1));
        assertEquals(0, field.add(Integer.MIN_VALUE, -1));
    }

    @Test
    public void testAddLong() {
        assertEquals(0L, field.add(0L, 1));
        assertEquals(0L, field.add(0L, 0));
        assertEquals(0L, field.add(0L, -1));
        assertEquals(0L, field.add(5L, 3));
        assertEquals(0L, field.add(-5L, 3));
        assertEquals(0L, field.add(Long.MAX_VALUE, 1));
        assertEquals(0L, field.add(Long.MIN_VALUE, -1));
    }

    @Test
    public void testAddLongLong() {
        assertEquals(0L, field.add(0L, 1L));
        assertEquals(0L, field.add(0L, 0L));
        assertEquals(0L, field.add(0L, -1L));
        assertEquals(0L, field.add(5L, 3L));
        assertEquals(0L, field.add(-5L, 3L));
        assertEquals(0L, field.add(Long.MAX_VALUE, 1L));
        assertEquals(0L, field.add(Long.MIN_VALUE, -1L));
    }

    // -----------------------------------------------------------------------
    @Test
    public void testGetDifferenceInt() {
        assertEquals(0, field.getDifference(0, 0));
        assertEquals(0, field.getDifference(1, 0));
        assertEquals(0, field.getDifference(0, 1));
        assertEquals(0, field.getDifference(Integer.MAX_VALUE, Integer.MIN_VALUE));
    }

    @Test
    public void testGetDifferenceLong() {
        assertEquals(0L, field.getDifference(0L, 0L));
        assertEquals(0L, field.getDifference(1L, 0L));
        assertEquals(0L, field.getDifference(0L, 1L));
        assertEquals(0L, field.getDifference(Long.MAX_VALUE, Long.MIN_VALUE));
    }

    @Test
    public void testGetDifferenceAsLong() {
        assertEquals(0L, field.getDifferenceAsLong(0L, 0L));
        assertEquals(0L, field.getDifferenceAsLong(1L, 0L));
        assertEquals(0L, field.getDifferenceAsLong(0L, 1L));
        assertEquals(0L, field.getDifferenceAsLong(Long.MAX_VALUE, Long.MIN_VALUE));
    }

    // -----------------------------------------------------------------------
    @Test
    public void testCompareTo() {
        // Compare to itself
        assertEquals(0, field.compareTo(field));
        // Compare to another unsupported field of different type
        DurationField other = UnsupportedDurationField.getInstance(DurationFieldType.months());
        // According to Joda-Time, compareTo returns -1 if this type is less than other type (alphabetical)
        // years vs months: "years" > "months" alphabetically? 'y' > 'm', so years > months => compareTo should be >0
        // But the bug in Defects4J (Joda-Time 4) might cause incorrect sign.
        int result = field.compareTo(other);
        // We expect positive because "years" > "months"
        assertTrue("years should be greater than months", result > 0);
        // Compare to a supported field (should return -1)
        DurationField supported = DurationFieldType.eras().getField(null); // eras is supported
        assertEquals(-1, field.compareTo(supported));
    }

    @Test(expected = NullPointerException.class)
    public void testCompareToNull() {
        field.compareTo(null);
    }

    // -----------------------------------------------------------------------
    @Test
    public void testToString() {
        assertEquals("UnsupportedDurationField[years]", field.toString());
    }

    // -----------------------------------------------------------------------
    @Test
    public void testEquals() {
        assertTrue(field.equals(field));
        assertFalse(field.equals(null));
        assertFalse(field.equals(""));
        assertFalse(field.equals(UnsupportedDurationField.getInstance(DurationFieldType.months())));
        assertTrue(field.equals(UnsupportedDurationField.getInstance(type)));
    }

    @Test
    public void testHashCode() {
        assertEquals(type.hashCode(), field.hashCode());
        // Different type should have different hash
        assertFalse(field.hashCode() == UnsupportedDurationField.getInstance(DurationFieldType.months()).hashCode());
    }

    // -----------------------------------------------------------------------
    // Additional edge cases for getMillis with overflow
    @Test
    public void testGetMillisIntOverflow() {
        // getMillis(int) multiplies by unitMillis (0) so always 0, no overflow
        assertEquals(0L, field.getMillis(Integer.MAX_VALUE));
        assertEquals(0L, field.getMillis(Integer.MIN_VALUE));
    }

    @Test
    public void testGetMillisLongOverflow() {
        // getMillis(long) multiplies by unitMillis (0) so always 0
        assertEquals(0L, field.getMillis(Long.MAX_VALUE));
        assertEquals(0L, field.getMillis(Long.MIN_VALUE));
    }

    // -----------------------------------------------------------------------
    // Test that add methods do not throw on extreme values
    @Test
    public void testAddIntExtreme() {
        assertEquals(0, field.add(Integer.MAX_VALUE, Integer.MAX_VALUE));
        assertEquals(0, field.add(Integer.MIN_VALUE, Integer.MIN_VALUE));
    }

    @Test
    public void testAddLongExtreme() {
        assertEquals(0L, field.add(Long.MAX_VALUE, Integer.MAX_VALUE));
        assertEquals(0L, field.add(Long.MIN_VALUE, Integer.MIN_VALUE));
    }

    @Test
    public void testAddLongLongExtreme() {
        assertEquals(0L, field.add(Long.MAX_VALUE, Long.MAX_VALUE));
        assertEquals(0L, field.add(Long.MIN_VALUE, Long.MIN_VALUE));
    }

    // -----------------------------------------------------------------------
    // Test getDifference with extreme values
    @Test
    public void testGetDifferenceIntExtreme() {
        assertEquals(0, field.getDifference(Integer.MAX_VALUE, Integer.MIN_VALUE));
        assertEquals(0, field.getDifference(Integer.MIN_VALUE, Integer.MAX_VALUE));
    }

    @Test
    public void testGetDifferenceLongExtreme() {
        assertEquals(0L, field.getDifference(Long.MAX_VALUE, Long.MIN_VALUE));
        assertEquals(0L, field.getDifference(Long.MIN_VALUE, Long.MAX_VALUE));
    }

    @Test
    public void testGetDifferenceAsLongExtreme() {
        assertEquals(0L, field.getDifferenceAsLong(Long.MAX_VALUE, Long.MIN_VALUE));
        assertEquals(0L, field.getDifferenceAsLong(Long.MIN_VALUE, Long.MAX_VALUE));
    }

    // -----------------------------------------------------------------------
    // Test that compareTo works correctly with different types (alphabetical order)
    @Test
    public void testCompareToDifferentTypes() {
        DurationField years = UnsupportedDurationField.getInstance(DurationFieldType.years());
        DurationField months = UnsupportedDurationField.getInstance(DurationFieldType.months());
        DurationField days = UnsupportedDurationField.getInstance(DurationFieldType.days());
        DurationField hours = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        DurationField millis = UnsupportedDurationField.getInstance(DurationFieldType.millis());
        DurationField weeks = UnsupportedDurationField.getInstance(DurationFieldType.weeks());

        // Alphabetical order: days < hours < millis < months < weeks < years
        assertTrue(days.compareTo(hours) < 0);
        assertTrue(hours.compareTo(millis) < 0);
        assertTrue(millis.compareTo(months) < 0);
        assertTrue(months.compareTo(weeks) < 0);
        assertTrue(weeks.compareTo(years) < 0);
        // Reverse
        assertTrue(years.compareTo(weeks) > 0);
        assertTrue(weeks.compareTo(months) > 0);
        assertTrue(months.compareTo(millis) > 0);
        assertTrue(millis.compareTo(hours) > 0);
        assertTrue(hours.compareTo(days) > 0);
    }

    // -----------------------------------------------------------------------
    // Test that getInstance returns the same instance for the same type
    @Test
    public void testGetInstanceSingleton() {
        assertSame(field, UnsupportedDurationField.getInstance(type));
        assertSame(UnsupportedDurationField.getInstance(DurationFieldType.months()),
                   UnsupportedDurationField.getInstance(DurationFieldType.months()));
    }

    // -----------------------------------------------------------------------
    // Test that the field is not supported
    @Test
    public void testIsSupportedFalse() {
        assertFalse(field.isSupported());
    }
}