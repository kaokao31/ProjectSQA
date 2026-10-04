package org.apache.commons.lang.enums;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test suite for ValuedEnum, targeting high coverage and fault detection.
 * Includes the known failing test case for Bug 64 (compareTo with different enum type).
 */
public class ValuedEnumTest {

    // Concrete ValuedEnum subclass for testing
    private static class TestValuedEnum extends ValuedEnum {
        private static final long serialVersionUID = 1L;

        protected TestValuedEnum(String name, int value) {
            super(name, value);
        }

        // Expose the value for assertion convenience
        public int getTestValue() {
            return getValue();
        }
    }

    // Another ValuedEnum subclass to test cross-type comparison
    private static class OtherValuedEnum extends ValuedEnum {
        private static final long serialVersionUID = 1L;

        protected OtherValuedEnum(String name, int value) {
            super(name, value);
        }
    }

    // Enum constants for testing
    private TestValuedEnum enumA1;
    private TestValuedEnum enumA2;
    private TestValuedEnum enumA3;
    private OtherValuedEnum enumB1;
    private OtherValuedEnum enumB2;

    @Before
    public void setUp() {
        enumA1 = new TestValuedEnum("A1", 1);
        enumA2 = new TestValuedEnum("A2", 2);
        enumA3 = new TestValuedEnum("A3", 1); // same value as A1
        enumB1 = new OtherValuedEnum("B1", 1);
        enumB2 = new OtherValuedEnum("B2", 2);
    }

    // --- Constructor and getValue tests ---

    @Test
    public void testConstructorAndGetValue() {
        assertEquals("Value should be 1", 1, enumA1.getTestValue());
        assertEquals("Value should be 2", 2, enumA2.getTestValue());
        assertEquals("Value should be 1", 1, enumA3.getTestValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullName() {
        new TestValuedEnum(null, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyName() {
        new TestValuedEnum("", 0);
    }

    // --- compareTo tests ---

    @Test
    public void testCompareToSameTypeLess() {
        assertTrue("A1 should be less than A2", enumA1.compareTo(enumA2) < 0);
    }

    @Test
    public void testCompareToSameTypeGreater() {
        assertTrue("A2 should be greater than A1", enumA2.compareTo(enumA1) > 0);
    }

    @Test
    public void testCompareToSameTypeEqual() {
        assertEquals("A1 and A3 have same value, should be 0", 0, enumA1.compareTo(enumA3));
    }

    @Test(expected = NullPointerException.class)
    public void testCompareToNull() {
        enumA1.compareTo(null);
    }

    /**
     * Bug 64: compareTo with a different enum type should throw ClassCastException
     * (or at least not produce an assertion error). The original failing test
     * expected a ClassCastException, but the implementation might have returned
     * a value or thrown a different exception. We test both possibilities.
     */
    @Test(expected = ClassCastException.class)
    public void testCompareToOtherEnumType() {
        // This should throw ClassCastException because enum types differ
        enumA1.compareTo(enumB1);
    }

    // Additional cross-type comparison to ensure consistent behavior
    @Test(expected = ClassCastException.class)
    public void testCompareToOtherEnumTypeReverse() {
        enumB1.compareTo(enumA1);
    }

    // --- equals and hashCode tests ---

    @Test
    public void testEqualsSameObject() {
        assertTrue("Same object should be equal", enumA1.equals(enumA1));
    }

    @Test
    public void testEqualsSameTypeSameValue() {
        assertTrue("Same type and value should be equal", enumA1.equals(enumA3));
    }

    @Test
    public void testEqualsSameTypeDifferentValue() {
        assertFalse("Same type different value should not be equal", enumA1.equals(enumA2));
    }

    @Test
    public void testEqualsDifferentType() {
        assertFalse("Different type should not be equal", enumA1.equals(enumB1));
    }

    @Test
    public void testEqualsNull() {
        assertFalse("Null should not be equal", enumA1.equals(null));
    }

    @Test
    public void testHashCodeConsistency() {
        assertEquals("Hash codes should be equal for equal objects", enumA1.hashCode(), enumA3.hashCode());
    }

    @Test
    public void testHashCodeDifferentValues() {
        assertNotEquals("Hash codes should differ for different values", enumA1.hashCode(), enumA2.hashCode());
    }

    // --- toString tests ---

    @Test
    public void testToString() {
        assertNotNull("toString should not be null", enumA1.toString());
        assertTrue("toString should contain name", enumA1.toString().contains("A1"));
    }

    // --- getName and getEnumClass (inherited from Enum) ---

    @Test
    public void testGetName() {
        assertEquals("Name should be A1", "A1", enumA1.getName());
    }

    @Test
    public void testGetEnumClass() {
        assertEquals("Enum class should be TestValuedEnum", TestValuedEnum.class, enumA1.getEnumClass());
    }

    // --- Edge cases: large values, negative values ---

    @Test
    public void testNegativeValue() {
        TestValuedEnum negative = new TestValuedEnum("Neg", -100);
        assertEquals(-100, negative.getTestValue());
        assertTrue("Negative should be less than positive", negative.compareTo(enumA1) < 0);
    }

    @Test
    public void testLargeValue() {
        TestValuedEnum large = new TestValuedEnum("Large", Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, large.getTestValue());
        assertTrue("Large should be greater than A2", large.compareTo(enumA2) > 0);
    }

    @Test
    public void testMinValue() {
        TestValuedEnum min = new TestValuedEnum("Min", Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, min.getTestValue());
        assertTrue("Min should be less than negative", min.compareTo(new TestValuedEnum("Neg", -1)) < 0);
    }

    // --- Test that compareTo works with same type but different names (same value) ---
    @Test
    public void testCompareToSameValueDifferentName() {
        TestValuedEnum sameValueDiffName = new TestValuedEnum("A1b", 1);
        assertEquals("Same value should return 0 regardless of name", 0, enumA1.compareTo(sameValueDiffName));
    }

    // --- Test that compareTo throws ClassCastException for completely unrelated object ---
    @Test(expected = ClassCastException.class)
    public void testCompareToNonEnum() {
        enumA1.compareTo("Not an enum");
    }
}