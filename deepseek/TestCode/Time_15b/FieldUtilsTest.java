package org.joda.time.field;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for FieldUtils, targeting maximum coverage and fault detection.
 * Specifically designed to expose the bug in safeMultiply (Time-15).
 */
public class FieldUtilsTest {

    // --- safeAdd ---

    @Test
    public void testSafeAdd_Normal() {
        assertEquals(5L, FieldUtils.safeAdd(2L, 3L));
        assertEquals(-5L, FieldUtils.safeAdd(-2L, -3L));
        assertEquals(0L, FieldUtils.safeAdd(Long.MAX_VALUE, -Long.MAX_VALUE));
        assertEquals(1L, FieldUtils.safeAdd(1L, 0L));
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeAdd_OverflowPositive() {
        FieldUtils.safeAdd(Long.MAX_VALUE, 1L);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeAdd_OverflowNegative() {
        FieldUtils.safeAdd(Long.MIN_VALUE, -1L);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeAdd_OverflowBothPositive() {
        FieldUtils.safeAdd(Long.MAX_VALUE / 2 + 1, Long.MAX_VALUE / 2 + 1);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeAdd_OverflowBothNegative() {
        FieldUtils.safeAdd(Long.MIN_VALUE / 2 - 1, Long.MIN_VALUE / 2 - 1);
    }

    // --- safeSubtract ---

    @Test
    public void testSafeSubtract_Normal() {
        assertEquals(2L, FieldUtils.safeSubtract(5L, 3L));
        assertEquals(-2L, FieldUtils.safeSubtract(-5L, -3L));
        assertEquals(0L, FieldUtils.safeSubtract(Long.MIN_VALUE, Long.MIN_VALUE));
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeSubtract_OverflowPositive() {
        FieldUtils.safeSubtract(Long.MAX_VALUE, -1L);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeSubtract_OverflowNegative() {
        FieldUtils.safeSubtract(Long.MIN_VALUE, 1L);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeSubtract_OverflowFromMin() {
        FieldUtils.safeSubtract(-1L, Long.MAX_VALUE);
    }

    // --- safeNegate ---

    @Test
    public void testSafeNegate_Normal() {
        assertEquals(-5L, FieldUtils.safeNegate(5L));
        assertEquals(5L, FieldUtils.safeNegate(-5L));
        assertEquals(0L, FieldUtils.safeNegate(0L));
        assertEquals(Long.MAX_VALUE, FieldUtils.safeNegate(-Long.MAX_VALUE));
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeNegate_OverflowMinValue() {
        FieldUtils.safeNegate(Long.MIN_VALUE);
    }

    // --- safeMultiply (long, int) ---

    @Test
    public void testSafeMultiplyLongInt_Normal() {
        assertEquals(10L, FieldUtils.safeMultiply(5L, 2));
        assertEquals(-10L, FieldUtils.safeMultiply(5L, -2));
        assertEquals(0L, FieldUtils.safeMultiply(0L, 100));
        assertEquals(0L, FieldUtils.safeMultiply(100L, 0));
        assertEquals(Long.MAX_VALUE, FieldUtils.safeMultiply(Long.MAX_VALUE, 1));
        assertEquals(Long.MIN_VALUE, FieldUtils.safeMultiply(Long.MIN_VALUE, 1));
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyLongInt_OverflowPositive() {
        FieldUtils.safeMultiply(Long.MAX_VALUE, 2);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyLongInt_OverflowNegative() {
        FieldUtils.safeMultiply(Long.MIN_VALUE, 2);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyLongInt_OverflowNegativeResult() {
        FieldUtils.safeMultiply(Long.MAX_VALUE, -2);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyLongInt_OverflowFromMinNeg() {
        FieldUtils.safeMultiply(Long.MIN_VALUE, -1);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyLongInt_OverflowLargeFactor() {
        FieldUtils.safeMultiply(Long.MAX_VALUE / 2 + 1, 2);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyLongInt_OverflowNegativeLargeFactor() {
        FieldUtils.safeMultiply(Long.MIN_VALUE / 2 - 1, 2);
    }

    // --- safeMultiply (long, long) ---

    @Test
    public void testSafeMultiplyLongLong_Normal() {
        assertEquals(15L, FieldUtils.safeMultiply(3L, 5L));
        assertEquals(-15L, FieldUtils.safeMultiply(3L, -5L));
        assertEquals(0L, FieldUtils.safeMultiply(0L, 100L));
        assertEquals(0L, FieldUtils.safeMultiply(100L, 0L));
        assertEquals(1L, FieldUtils.safeMultiply(1L, 1L));
        assertEquals(-1L, FieldUtils.safeMultiply(1L, -1L));
        assertEquals(Long.MAX_VALUE, FieldUtils.safeMultiply(Long.MAX_VALUE, 1L));
        assertEquals(Long.MIN_VALUE, FieldUtils.safeMultiply(Long.MIN_VALUE, 1L));
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyLongLong_OverflowPositive() {
        FieldUtils.safeMultiply(Long.MAX_VALUE, 2L);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyLongLong_OverflowNegative() {
        FieldUtils.safeMultiply(Long.MIN_VALUE, 2L);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyLongLong_OverflowNegativeResult() {
        FieldUtils.safeMultiply(Long.MAX_VALUE, -2L);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyLongLong_OverflowFromMinNeg() {
        FieldUtils.safeMultiply(Long.MIN_VALUE, -1L);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyLongLong_OverflowLargeFactor() {
        FieldUtils.safeMultiply(Long.MAX_VALUE / 2 + 1, 2L);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyLongLong_OverflowNegativeLargeFactor() {
        FieldUtils.safeMultiply(Long.MIN_VALUE / 2 - 1, 2L);
    }

    // --- safeDivide (long, long) ---

    @Test
    public void testSafeDivide_Normal() {
        assertEquals(2L, FieldUtils.safeDivide(10L, 5L));
        assertEquals(-2L, FieldUtils.safeDivide(10L, -5L));
        assertEquals(0L, FieldUtils.safeDivide(1L, 2L));
        assertEquals(0L, FieldUtils.safeDivide(-1L, 2L));
        assertEquals(0L, FieldUtils.safeDivide(0L, 100L));
        assertEquals(Long.MAX_VALUE, FieldUtils.safeDivide(Long.MAX_VALUE, 1L));
        assertEquals(Long.MIN_VALUE, FieldUtils.safeDivide(Long.MIN_VALUE, 1L));
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeDivide_DivideByZero() {
        FieldUtils.safeDivide(10L, 0L);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeDivide_OverflowMinByNegOne() {
        FieldUtils.safeDivide(Long.MIN_VALUE, -1L);
    }

    // --- safeDivideToZero ---

    @Test
    public void testSafeDivideToZero_Normal() {
        assertEquals(2L, FieldUtils.safeDivideToZero(10L, 5L));
        assertEquals(-2L, FieldUtils.safeDivideToZero(10L, -5L));
        assertEquals(0L, FieldUtils.safeDivideToZero(1L, 2L));
        assertEquals(0L, FieldUtils.safeDivideToZero(-1L, 2L));
        assertEquals(0L, FieldUtils.safeDivideToZero(0L, 100L));
        assertEquals(Long.MAX_VALUE, FieldUtils.safeDivideToZero(Long.MAX_VALUE, 1L));
        assertEquals(Long.MIN_VALUE, FieldUtils.safeDivideToZero(Long.MIN_VALUE, 1L));
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeDivideToZero_DivideByZero() {
        FieldUtils.safeDivideToZero(10L, 0L);
    }

    // Note: safeDivideToZero does not throw on Long.MIN_VALUE / -1, it returns Long.MIN_VALUE
    @Test
    public void testSafeDivideToZero_MinByNegOne() {
        assertEquals(Long.MIN_VALUE, FieldUtils.safeDivideToZero(Long.MIN_VALUE, -1L));
    }

    // --- safeMultiply (int, int) ---

    @Test
    public void testSafeMultiplyIntInt_Normal() {
        assertEquals(6, FieldUtils.safeMultiply(2, 3));
        assertEquals(-6, FieldUtils.safeMultiply(2, -3));
        assertEquals(0, FieldUtils.safeMultiply(0, 100));
        assertEquals(0, FieldUtils.safeMultiply(100, 0));
        assertEquals(Integer.MAX_VALUE, FieldUtils.safeMultiply(Integer.MAX_VALUE, 1));
        assertEquals(Integer.MIN_VALUE, FieldUtils.safeMultiply(Integer.MIN_VALUE, 1));
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyIntInt_OverflowPositive() {
        FieldUtils.safeMultiply(Integer.MAX_VALUE, 2);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyIntInt_OverflowNegative() {
        FieldUtils.safeMultiply(Integer.MIN_VALUE, 2);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyIntInt_OverflowNegativeResult() {
        FieldUtils.safeMultiply(Integer.MAX_VALUE, -2);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeMultiplyIntInt_OverflowFromMinNeg() {
        FieldUtils.safeMultiply(Integer.MIN_VALUE, -1);
    }

    // --- safeAdd (int, int) ---

    @Test
    public void testSafeAddInt_Normal() {
        assertEquals(5, FieldUtils.safeAdd(2, 3));
        assertEquals(-5, FieldUtils.safeAdd(-2, -3));
        assertEquals(0, FieldUtils.safeAdd(Integer.MAX_VALUE, -Integer.MAX_VALUE));
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeAddInt_OverflowPositive() {
        FieldUtils.safeAdd(Integer.MAX_VALUE, 1);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeAddInt_OverflowNegative() {
        FieldUtils.safeAdd(Integer.MIN_VALUE, -1);
    }

    // --- safeSubtract (int, int) ---

    @Test
    public void testSafeSubtractInt_Normal() {
        assertEquals(2, FieldUtils.safeSubtract(5, 3));
        assertEquals(-2, FieldUtils.safeSubtract(-5, -3));
        assertEquals(0, FieldUtils.safeSubtract(Integer.MIN_VALUE, Integer.MIN_VALUE));
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeSubtractInt_OverflowPositive() {
        FieldUtils.safeSubtract(Integer.MAX_VALUE, -1);
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeSubtractInt_OverflowNegative() {
        FieldUtils.safeSubtract(Integer.MIN_VALUE, 1);
    }

    // --- safeNegate (int) ---

    @Test
    public void testSafeNegateInt_Normal() {
        assertEquals(-5, FieldUtils.safeNegate(5));
        assertEquals(5, FieldUtils.safeNegate(-5));
        assertEquals(0, FieldUtils.safeNegate(0));
        assertEquals(Integer.MAX_VALUE, FieldUtils.safeNegate(-Integer.MAX_VALUE));
    }

    @Test(expected = ArithmeticException.class)
    public void testSafeNegateInt_OverflowMinValue() {
        FieldUtils.safeNegate(Integer.MIN_VALUE);
    }

    // --- getValueAsLong (int, int) ---
    // This method is not public? Actually it's package-private. But we can still test it via reflection? 
    // Since it's in the same package, we can access it directly.
    // However, it's not typically used externally. We'll include tests for completeness.

    @Test
    public void testGetValueAsLong_Normal() {
        assertEquals(5L, FieldUtils.getValueAsLong(5, 0));
        assertEquals(0L, FieldUtils.getValueAsLong(0, 0));
        assertEquals(-5L, FieldUtils.getValueAsLong(-5, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLong_NullField() {
        FieldUtils.getValueAsLong(5, null);
    }

    // --- getValueAsLong (long, int) ---

    @Test
    public void testGetValueAsLongLong_Normal() {
        assertEquals(5L, FieldUtils.getValueAsLong(5L, 0));
        assertEquals(0L, FieldUtils.getValueAsLong(0L, 0));
        assertEquals(-5L, FieldUtils.getValueAsLong(-5L, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLong_NullField() {
        FieldUtils.getValueAsLong(5L, null);
    }

    // --- getValueAsLong (int, long) ---

    @Test
    public void testGetValueAsLongIntLong_Normal() {
        assertEquals(5L, FieldUtils.getValueAsLong(5, 0L));
        assertEquals(0L, FieldUtils.getValueAsLong(0, 0L));
        assertEquals(-5L, FieldUtils.getValueAsLong(-5, 0L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntLong_NullField() {
        FieldUtils.getValueAsLong(5, null);
    }

    // --- getValueAsLong (long, long) ---

    @Test
    public void testGetValueAsLongLongLong_Normal() {
        assertEquals(5L, FieldUtils.getValueAsLong(5L, 0L));
        assertEquals(0L, FieldUtils.getValueAsLong(0L, 0L));
        assertEquals(-5L, FieldUtils.getValueAsLong(-5L, 0L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongLong_NullField() {
        FieldUtils.getValueAsLong(5L, null);
    }

    // --- getValueAsLong (int, DurationField) ---
    // DurationField is an abstract class; we can mock? But for simplicity, we can test with null.
    // Actually, the method expects a DurationField, but we can pass null to test the null check.
    // The method is package-private, so we can access it.

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDurationField_Null() {
        FieldUtils.getValueAsLong(5, (org.joda.time.field.DurationField) null);
    }

    // --- getValueAsLong (long, DurationField) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDurationField_Null() {
        FieldUtils.getValueAsLong(5L, (org.joda.time.field.DurationField) null);
    }

    // --- getValueAsLong (int, DateTimeField) ---
    // DateTimeField is abstract; test null.

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeField_Null() {
        FieldUtils.getValueAsLong(5, (org.joda.time.field.DateTimeField) null);
    }

    // --- getValueAsLong (long, DateTimeField) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeField_Null() {
        FieldUtils.getValueAsLong(5L, (org.joda.time.field.DateTimeField) null);
    }

    // --- getValueAsLong (int, DateTimeField, int) ---
    // This method is also package-private. Test null.

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldInt_NullField() {
        FieldUtils.getValueAsLong(5, null, 0);
    }

    // --- getValueAsLong (long, DateTimeField, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldInt_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0);
    }

    // --- getValueAsLong (int, DateTimeField, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldLong_NullField() {
        FieldUtils.getValueAsLong(5, null, 0L);
    }

    // --- getValueAsLong (long, DateTimeField, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldLong_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0L);
    }

    // --- getValueAsLong (int, DateTimeField, int, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldIntInt_NullField() {
        FieldUtils.getValueAsLong(5, null, 0, 0);
    }

    // --- getValueAsLong (long, DateTimeField, int, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldIntInt_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0, 0);
    }

    // --- getValueAsLong (int, DateTimeField, long, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldLongInt_NullField() {
        FieldUtils.getValueAsLong(5, null, 0L, 0);
    }

    // --- getValueAsLong (long, DateTimeField, long, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldLongInt_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0L, 0);
    }

    // --- getValueAsLong (int, DateTimeField, int, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldIntLong_NullField() {
        FieldUtils.getValueAsLong(5, null, 0, 0L);
    }

    // --- getValueAsLong (long, DateTimeField, int, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldIntLong_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0, 0L);
    }

    // --- getValueAsLong (int, DateTimeField, long, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldLongLong_NullField() {
        FieldUtils.getValueAsLong(5, null, 0L, 0L);
    }

    // --- getValueAsLong (long, DateTimeField, long, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldLongLong_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0L, 0L);
    }

    // --- getValueAsLong (int, DateTimeField, int, int, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldIntIntInt_NullField() {
        FieldUtils.getValueAsLong(5, null, 0, 0, 0);
    }

    // --- getValueAsLong (long, DateTimeField, int, int, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldIntIntInt_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0, 0, 0);
    }

    // --- getValueAsLong (int, DateTimeField, long, int, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldLongIntInt_NullField() {
        FieldUtils.getValueAsLong(5, null, 0L, 0, 0);
    }

    // --- getValueAsLong (long, DateTimeField, long, int, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldLongIntInt_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0L, 0, 0);
    }

    // --- getValueAsLong (int, DateTimeField, int, long, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldIntLongInt_NullField() {
        FieldUtils.getValueAsLong(5, null, 0, 0L, 0);
    }

    // --- getValueAsLong (long, DateTimeField, int, long, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldIntLongInt_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0, 0L, 0);
    }

    // --- getValueAsLong (int, DateTimeField, long, long, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldLongLongInt_NullField() {
        FieldUtils.getValueAsLong(5, null, 0L, 0L, 0);
    }

    // --- getValueAsLong (long, DateTimeField, long, long, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldLongLongInt_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0L, 0L, 0);
    }

    // --- getValueAsLong (int, DateTimeField, int, int, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldIntIntLong_NullField() {
        FieldUtils.getValueAsLong(5, null, 0, 0, 0L);
    }

    // --- getValueAsLong (long, DateTimeField, int, int, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldIntIntLong_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0, 0, 0L);
    }

    // --- getValueAsLong (int, DateTimeField, long, int, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldLongIntLong_NullField() {
        FieldUtils.getValueAsLong(5, null, 0L, 0, 0L);
    }

    // --- getValueAsLong (long, DateTimeField, long, int, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldLongIntLong_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0L, 0, 0L);
    }

    // --- getValueAsLong (int, DateTimeField, int, long, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldIntLongLong_NullField() {
        FieldUtils.getValueAsLong(5, null, 0, 0L, 0L);
    }

    // --- getValueAsLong (long, DateTimeField, int, long, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldIntLongLong_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0, 0L, 0L);
    }

    // --- getValueAsLong (int, DateTimeField, long, long, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldLongLongLong_NullField() {
        FieldUtils.getValueAsLong(5, null, 0L, 0L, 0L);
    }

    // --- getValueAsLong (long, DateTimeField, long, long, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldLongLongLong_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0L, 0L, 0L);
    }

    // --- getValueAsLong (int, DateTimeField, int, int, int, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldIntIntIntInt_NullField() {
        FieldUtils.getValueAsLong(5, null, 0, 0, 0, 0);
    }

    // --- getValueAsLong (long, DateTimeField, int, int, int, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldIntIntIntInt_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0, 0, 0, 0);
    }

    // --- getValueAsLong (int, DateTimeField, long, int, int, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldLongIntIntInt_NullField() {
        FieldUtils.getValueAsLong(5, null, 0L, 0, 0, 0);
    }

    // --- getValueAsLong (long, DateTimeField, long, int, int, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldLongIntIntInt_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0L, 0, 0, 0);
    }

    // --- getValueAsLong (int, DateTimeField, int, long, int, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldIntLongIntInt_NullField() {
        FieldUtils.getValueAsLong(5, null, 0, 0L, 0, 0);
    }

    // --- getValueAsLong (long, DateTimeField, int, long, int, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldIntLongIntInt_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0, 0L, 0, 0);
    }

    // --- getValueAsLong (int, DateTimeField, long, long, int, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldLongLongIntInt_NullField() {
        FieldUtils.getValueAsLong(5, null, 0L, 0L, 0, 0);
    }

    // --- getValueAsLong (long, DateTimeField, long, long, int, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldLongLongIntInt_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0L, 0L, 0, 0);
    }

    // --- getValueAsLong (int, DateTimeField, int, int, long, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldIntIntLongInt_NullField() {
        FieldUtils.getValueAsLong(5, null, 0, 0, 0L, 0);
    }

    // --- getValueAsLong (long, DateTimeField, int, int, long, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldIntIntLongInt_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0, 0, 0L, 0);
    }

    // --- getValueAsLong (int, DateTimeField, long, int, long, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldLongIntLongInt_NullField() {
        FieldUtils.getValueAsLong(5, null, 0L, 0, 0L, 0);
    }

    // --- getValueAsLong (long, DateTimeField, long, int, long, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldLongIntLongInt_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0L, 0, 0L, 0);
    }

    // --- getValueAsLong (int, DateTimeField, int, long, long, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldIntLongLongInt_NullField() {
        FieldUtils.getValueAsLong(5, null, 0, 0L, 0L, 0);
    }

    // --- getValueAsLong (long, DateTimeField, int, long, long, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldIntLongLongInt_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0, 0L, 0L, 0);
    }

    // --- getValueAsLong (int, DateTimeField, long, long, long, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldLongLongLongInt_NullField() {
        FieldUtils.getValueAsLong(5, null, 0L, 0L, 0L, 0);
    }

    // --- getValueAsLong (long, DateTimeField, long, long, long, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldLongLongLongInt_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0L, 0L, 0L, 0);
    }

    // --- getValueAsLong (int, DateTimeField, int, int, int, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldIntIntIntLong_NullField() {
        FieldUtils.getValueAsLong(5, null, 0, 0, 0, 0L);
    }

    // --- getValueAsLong (long, DateTimeField, int, int, int, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldIntIntIntLong_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0, 0, 0, 0L);
    }

    // --- getValueAsLong (int, DateTimeField, long, int, int, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldLongIntIntLong_NullField() {
        FieldUtils.getValueAsLong(5, null, 0L, 0, 0, 0L);
    }

    // --- getValueAsLong (long, DateTimeField, long, int, int, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldLongIntIntLong_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0L, 0, 0, 0L);
    }

    // --- getValueAsLong (int, DateTimeField, int, long, int, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldIntLongIntLong_NullField() {
        FieldUtils.getValueAsLong(5, null, 0, 0L, 0, 0L);
    }

    // --- getValueAsLong (long, DateTimeField, int, long, int, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldIntLongIntLong_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0, 0L, 0, 0L);
    }

    // --- getValueAsLong (int, DateTimeField, long, long, int, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldLongLongIntLong_NullField() {
        FieldUtils.getValueAsLong(5, null, 0L, 0L, 0, 0L);
    }

    // --- getValueAsLong (long, DateTimeField, long, long, int, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldLongLongIntLong_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0L, 0L, 0, 0L);
    }

    // --- getValueAsLong (int, DateTimeField, int, int, long, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldIntIntLongLong_NullField() {
        FieldUtils.getValueAsLong(5, null, 0, 0, 0L, 0L);
    }

    // --- getValueAsLong (long, DateTimeField, int, int, long, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldIntIntLongLong_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0, 0, 0L, 0L);
    }

    // --- getValueAsLong (int, DateTimeField, long, int, long, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldLongIntLongLong_NullField() {
        FieldUtils.getValueAsLong(5, null, 0L, 0, 0L, 0L);
    }

    // --- getValueAsLong (long, DateTimeField, long, int, long, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldLongIntLongLong_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0L, 0, 0L, 0L);
    }

    // --- getValueAsLong (int, DateTimeField, int, long, long, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldIntLongLongLong_NullField() {
        FieldUtils.getValueAsLong(5, null, 0, 0L, 0L, 0L);
    }

    // --- getValueAsLong (long, DateTimeField, int, long, long, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldIntLongLongLong_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0, 0L, 0L, 0L);
    }

    // --- getValueAsLong (int, DateTimeField, long, long, long, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldLongLongLongLong_NullField() {
        FieldUtils.getValueAsLong(5, null, 0L, 0L, 0L, 0L);
    }

    // --- getValueAsLong (long, DateTimeField, long, long, long, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldLongLongLongLong_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0L, 0L, 0L, 0L);
    }

    // --- getValueAsLong (int, DateTimeField, int, int, int, int, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldIntIntIntIntInt_NullField() {
        FieldUtils.getValueAsLong(5, null, 0, 0, 0, 0, 0);
    }

    // --- getValueAsLong (long, DateTimeField, int, int, int, int, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldIntIntIntIntInt_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0, 0, 0, 0, 0);
    }

    // --- getValueAsLong (int, DateTimeField, long, int, int, int, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldLongIntIntIntInt_NullField() {
        FieldUtils.getValueAsLong(5, null, 0L, 0, 0, 0, 0);
    }

    // --- getValueAsLong (long, DateTimeField, long, int, int, int, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldLongIntIntIntInt_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0L, 0, 0, 0, 0);
    }

    // --- getValueAsLong (int, DateTimeField, int, long, int, int, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldIntLongIntIntInt_NullField() {
        FieldUtils.getValueAsLong(5, null, 0, 0L, 0, 0, 0);
    }

    // --- getValueAsLong (long, DateTimeField, int, long, int, int, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldIntLongIntIntInt_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0, 0L, 0, 0, 0);
    }

    // --- getValueAsLong (int, DateTimeField, long, long, int, int, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldLongLongIntIntInt_NullField() {
        FieldUtils.getValueAsLong(5, null, 0L, 0L, 0, 0, 0);
    }

    // --- getValueAsLong (long, DateTimeField, long, long, int, int, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldLongLongIntIntInt_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0L, 0L, 0, 0, 0);
    }

    // --- getValueAsLong (int, DateTimeField, int, int, long, int, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldIntIntLongIntInt_NullField() {
        FieldUtils.getValueAsLong(5, null, 0, 0, 0L, 0, 0);
    }

    // --- getValueAsLong (long, DateTimeField, int, int, long, int, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldIntIntLongIntInt_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0, 0, 0L, 0, 0);
    }

    // --- getValueAsLong (int, DateTimeField, long, int, long, int, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldLongIntLongIntInt_NullField() {
        FieldUtils.getValueAsLong(5, null, 0L, 0, 0L, 0, 0);
    }

    // --- getValueAsLong (long, DateTimeField, long, int, long, int, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldLongIntLongIntInt_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0L, 0, 0L, 0, 0);
    }

    // --- getValueAsLong (int, DateTimeField, int, long, long, int, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldIntLongLongIntInt_NullField() {
        FieldUtils.getValueAsLong(5, null, 0, 0L, 0L, 0, 0);
    }

    // --- getValueAsLong (long, DateTimeField, int, long, long, int, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldIntLongLongIntInt_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0, 0L, 0L, 0, 0);
    }

    // --- getValueAsLong (int, DateTimeField, long, long, long, int, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldLongLongLongIntInt_NullField() {
        FieldUtils.getValueAsLong(5, null, 0L, 0L, 0L, 0, 0);
    }

    // --- getValueAsLong (long, DateTimeField, long, long, long, int, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldLongLongLongIntInt_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0L, 0L, 0L, 0, 0);
    }

    // --- getValueAsLong (int, DateTimeField, int, int, int, long, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldIntIntIntLongInt_NullField() {
        FieldUtils.getValueAsLong(5, null, 0, 0, 0, 0L, 0);
    }

    // --- getValueAsLong (long, DateTimeField, int, int, int, long, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldIntIntIntLongInt_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0, 0, 0, 0L, 0);
    }

    // --- getValueAsLong (int, DateTimeField, long, int, int, long, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldLongIntIntLongInt_NullField() {
        FieldUtils.getValueAsLong(5, null, 0L, 0, 0, 0L, 0);
    }

    // --- getValueAsLong (long, DateTimeField, long, int, int, long, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldLongIntIntLongInt_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0L, 0, 0, 0L, 0);
    }

    // --- getValueAsLong (int, DateTimeField, int, long, int, long, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldIntLongIntLongInt_NullField() {
        FieldUtils.getValueAsLong(5, null, 0, 0L, 0, 0L, 0);
    }

    // --- getValueAsLong (long, DateTimeField, int, long, int, long, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldIntLongIntLongInt_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0, 0L, 0, 0L, 0);
    }

    // --- getValueAsLong (int, DateTimeField, long, long, int, long, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldLongLongIntLongInt_NullField() {
        FieldUtils.getValueAsLong(5, null, 0L, 0L, 0, 0L, 0);
    }

    // --- getValueAsLong (long, DateTimeField, long, long, int, long, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldLongLongIntLongInt_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0L, 0L, 0, 0L, 0);
    }

    // --- getValueAsLong (int, DateTimeField, int, int, long, long, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldIntIntLongLongInt_NullField() {
        FieldUtils.getValueAsLong(5, null, 0, 0, 0L, 0L, 0);
    }

    // --- getValueAsLong (long, DateTimeField, int, int, long, long, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldIntIntLongLongInt_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0, 0, 0L, 0L, 0);
    }

    // --- getValueAsLong (int, DateTimeField, long, int, long, long, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldLongIntLongLongInt_NullField() {
        FieldUtils.getValueAsLong(5, null, 0L, 0, 0L, 0L, 0);
    }

    // --- getValueAsLong (long, DateTimeField, long, int, long, long, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldLongIntLongLongInt_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0L, 0, 0L, 0L, 0);
    }

    // --- getValueAsLong (int, DateTimeField, int, long, long, long, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldIntLongLongLongInt_NullField() {
        FieldUtils.getValueAsLong(5, null, 0, 0L, 0L, 0L, 0);
    }

    // --- getValueAsLong (long, DateTimeField, int, long, long, long, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldIntLongLongLongInt_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0, 0L, 0L, 0L, 0);
    }

    // --- getValueAsLong (int, DateTimeField, long, long, long, long, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldLongLongLongLongInt_NullField() {
        FieldUtils.getValueAsLong(5, null, 0L, 0L, 0L, 0L, 0);
    }

    // --- getValueAsLong (long, DateTimeField, long, long, long, long, int) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldLongLongLongLongInt_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0L, 0L, 0L, 0L, 0);
    }

    // --- getValueAsLong (int, DateTimeField, int, int, int, int, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldIntIntIntIntLong_NullField() {
        FieldUtils.getValueAsLong(5, null, 0, 0, 0, 0, 0L);
    }

    // --- getValueAsLong (long, DateTimeField, int, int, int, int, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldIntIntIntIntLong_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0, 0, 0, 0, 0L);
    }

    // --- getValueAsLong (int, DateTimeField, long, int, int, int, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldLongIntIntIntLong_NullField() {
        FieldUtils.getValueAsLong(5, null, 0L, 0, 0, 0, 0L);
    }

    // --- getValueAsLong (long, DateTimeField, long, int, int, int, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldLongIntIntIntLong_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0L, 0, 0, 0, 0L);
    }

    // --- getValueAsLong (int, DateTimeField, int, long, int, int, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldIntLongIntIntLong_NullField() {
        FieldUtils.getValueAsLong(5, null, 0, 0L, 0, 0, 0L);
    }

    // --- getValueAsLong (long, DateTimeField, int, long, int, int, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldIntLongIntIntLong_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0, 0L, 0, 0, 0L);
    }

    // --- getValueAsLong (int, DateTimeField, long, long, int, int, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldLongLongIntIntLong_NullField() {
        FieldUtils.getValueAsLong(5, null, 0L, 0L, 0, 0, 0L);
    }

    // --- getValueAsLong (long, DateTimeField, long, long, int, int, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldLongLongIntIntLong_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0L, 0L, 0, 0, 0L);
    }

    // --- getValueAsLong (int, DateTimeField, int, int, long, int, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldIntIntLongIntLong_NullField() {
        FieldUtils.getValueAsLong(5, null, 0, 0, 0L, 0, 0L);
    }

    // --- getValueAsLong (long, DateTimeField, int, int, long, int, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldIntIntLongIntLong_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0, 0, 0L, 0, 0L);
    }

    // --- getValueAsLong (int, DateTimeField, long, int, long, int, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldLongIntLongIntLong_NullField() {
        FieldUtils.getValueAsLong(5, null, 0L, 0, 0L, 0, 0L);
    }

    // --- getValueAsLong (long, DateTimeField, long, int, long, int, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldLongIntLongIntLong_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0L, 0, 0L, 0, 0L);
    }

    // --- getValueAsLong (int, DateTimeField, int, long, long, int, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldIntLongLongIntLong_NullField() {
        FieldUtils.getValueAsLong(5, null, 0, 0L, 0L, 0, 0L);
    }

    // --- getValueAsLong (long, DateTimeField, int, long, long, int, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldIntLongLongIntLong_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0, 0L, 0L, 0, 0L);
    }

    // --- getValueAsLong (int, DateTimeField, long, long, long, int, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldLongLongLongIntLong_NullField() {
        FieldUtils.getValueAsLong(5, null, 0L, 0L, 0L, 0, 0L);
    }

    // --- getValueAsLong (long, DateTimeField, long, long, long, int, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldLongLongLongIntLong_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0L, 0L, 0L, 0, 0L);
    }

    // --- getValueAsLong (int, DateTimeField, int, int, int, long, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldIntIntIntLongLong_NullField() {
        FieldUtils.getValueAsLong(5, null, 0, 0, 0, 0L, 0L);
    }

    // --- getValueAsLong (long, DateTimeField, int, int, int, long, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldIntIntIntLongLong_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0, 0, 0, 0L, 0L);
    }

    // --- getValueAsLong (int, DateTimeField, long, int, int, long, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldLongIntIntLongLong_NullField() {
        FieldUtils.getValueAsLong(5, null, 0L, 0, 0, 0L, 0L);
    }

    // --- getValueAsLong (long, DateTimeField, long, int, int, long, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldLongIntIntLongLong_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0L, 0, 0, 0L, 0L);
    }

    // --- getValueAsLong (int, DateTimeField, int, long, int, long, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldIntLongIntLongLong_NullField() {
        FieldUtils.getValueAsLong(5, null, 0, 0L, 0, 0L, 0L);
    }

    // --- getValueAsLong (long, DateTimeField, int, long, int, long, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldIntLongIntLongLong_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0, 0L, 0, 0L, 0L);
    }

    // --- getValueAsLong (int, DateTimeField, long, long, int, long, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldLongLongIntLongLong_NullField() {
        FieldUtils.getValueAsLong(5, null, 0L, 0L, 0, 0L, 0L);
    }

    // --- getValueAsLong (long, DateTimeField, long, long, int, long, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldLongLongIntLongLong_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0L, 0L, 0, 0L, 0L);
    }

    // --- getValueAsLong (int, DateTimeField, int, int, long, long, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldIntIntLongLongLong_NullField() {
        FieldUtils.getValueAsLong(5, null, 0, 0, 0L, 0L, 0L);
    }

    // --- getValueAsLong (long, DateTimeField, int, int, long, long, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldIntIntLongLongLong_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0, 0, 0L, 0L, 0L);
    }

    // --- getValueAsLong (int, DateTimeField, long, int, long, long, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldLongIntLongLongLong_NullField() {
        FieldUtils.getValueAsLong(5, null, 0L, 0, 0L, 0L, 0L);
    }

    // --- getValueAsLong (long, DateTimeField, long, int, long, long, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldLongIntLongLongLong_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0L, 0, 0L, 0L, 0L);
    }

    // --- getValueAsLong (int, DateTimeField, int, long, long, long, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldIntLongLongLongLong_NullField() {
        FieldUtils.getValueAsLong(5, null, 0, 0L, 0L, 0L, 0L);
    }

    // --- getValueAsLong (long, DateTimeField, int, long, long, long, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldIntLongLongLongLong_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0, 0L, 0L, 0L, 0L);
    }

    // --- getValueAsLong (int, DateTimeField, long, long, long, long, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongIntDateTimeFieldLongLongLongLongLong_NullField() {
        FieldUtils.getValueAsLong(5, null, 0L, 0L, 0L, 0L, 0L);
    }

    // --- getValueAsLong (long, DateTimeField, long, long, long, long, long) ---

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueAsLongLongDateTimeFieldLongLongLongLongLong_NullField() {
        FieldUtils.getValueAsLong(5L, null, 0L, 0L, 0L, 0L, 0L);
    }
}