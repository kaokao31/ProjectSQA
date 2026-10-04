package org.mockito;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.internal.matchers.*;
import java.util.*;
import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for org.mockito.Matchers.
 * Covers all public static matcher methods including edge cases and null inputs.
 * Designed to maximize line and branch coverage.
 */
public class MatchersTest {

    // --- any() and any(Class) ---
    @Test
    public void testAnyObjectReturnsNull() {
        assertNull(Matchers.any());
    }

    @Test
    public void testAnyClassReturnsNull() {
        assertNull(Matchers.any(String.class));
        assertNull(Matchers.any(Integer.class));
        assertNull(Matchers.any(Object.class));
    }

    @Test(expected = ClassCastException.class)
    public void testAnyClassWithInvalidCast() {
        Object result = Matchers.any(String.class);
        Integer i = (Integer) result; // should throw ClassCastException at runtime
    }

    // --- primitive any() methods ---
    @Test
    public void testAnyBooleanReturnsFalse() {
        assertFalse(Matchers.anyBoolean());
    }

    @Test
    public void testAnyByteReturnsZero() {
        assertEquals((byte) 0, Matchers.anyByte());
    }

    @Test
    public void testAnyShortReturnsZero() {
        assertEquals((short) 0, Matchers.anyShort());
    }

    @Test
    public void testAnyIntReturnsZero() {
        assertEquals(0, Matchers.anyInt());
    }

    @Test
    public void testAnyLongReturnsZero() {
        assertEquals(0L, Matchers.anyLong());
    }

    @Test
    public void testAnyCharReturnsNullChar() {
        assertEquals('\0', Matchers.anyChar());
    }

    @Test
    public void testAnyDoubleReturnsZero() {
        assertEquals(0.0, Matchers.anyDouble(), 0.0);
    }

    @Test
    public void testAnyFloatReturnsZero() {
        assertEquals(0.0f, Matchers.anyFloat(), 0.0f);
    }

    @Test
    public void testAnyStringReturnsNull() {
        assertNull(Matchers.anyString());
    }

    @Test
    public void testAnyListReturnsNull() {
        assertNull(Matchers.anyList());
    }

    @Test
    public void testAnySetReturnsNull() {
        assertNull(Matchers.anySet());
    }

    @Test
    public void testAnyMapReturnsNull() {
        assertNull(Matchers.anyMap());
    }

    @Test
    public void testAnyCollectionReturnsNull() {
        assertNull(Matchers.anyCollection());
    }

    // --- eq() ---
    @Test
    public void testEqReturnsValue() {
        String value = "test";
        assertSame(value, Matchers.eq(value));
    }

    @Test
    public void testEqWithNull() {
        assertNull(Matchers.eq(null));
    }

    @Test
    public void testEqWithPrimitiveInt() {
        assertEquals(5, Matchers.eq(5));
    }

    @Test
    public void testEqWithBoolean() {
        assertTrue(Matchers.eq(true));
        assertFalse(Matchers.eq(false));
    }

    // --- same() ---
    @Test
    public void testSameReturnsSameInstance() {
        Object obj = new Object();
        assertSame(obj, Matchers.same(obj));
    }

    @Test
    public void testSameWithNull() {
        assertNull(Matchers.same(null));
    }

    // --- isA() ---
    @Test
    public void testIsAReturnsNull() {
        assertNull(Matchers.isA(String.class));
        assertNull(Matchers.isA(Integer.class));
    }

    @Test(expected = ClassCastException.class)
    public void testIsAWithInvalidCast() {
        Object result = Matchers.isA(String.class);
        Integer i = (Integer) result;
    }

    // --- contains() ---
    @Test
    public void testContainsReturnsNull() {
        assertNull(Matchers.contains("sub"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testContainsWithNullArgument() {
        Matchers.contains(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testContainsWithEmptyString() {
        Matchers.contains("");
    }

    // --- matches() ---
    @Test
    public void testMatchesReturnsNull() {
        assertNull(Matchers.matches(".*"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMatchesWithNullRegex() {
        Matchers.matches(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMatchesWithEmptyRegex() {
        Matchers.matches("");
    }

    // --- endsWith() ---
    @Test
    public void testEndsWithReturnsNull() {
        assertNull(Matchers.endsWith("suffix"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEndsWithNullSuffix() {
        Matchers.endsWith(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEndsWithEmptySuffix() {
        Matchers.endsWith("");
    }

    // --- startsWith() ---
    @Test
    public void testStartsWithReturnsNull() {
        assertNull(Matchers.startsWith("prefix"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartsWithNullPrefix() {
        Matchers.startsWith(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartsWithEmptyPrefix() {
        Matchers.startsWith("");
    }

    // --- isNull() ---
    @Test
    public void testIsNullReturnsNull() {
        assertNull(Matchers.isNull());
    }

    // --- isNotNull() ---
    @Test
    public void testIsNotNullReturnsNull() {
        assertNull(Matchers.isNotNull());
    }

    // --- notNull() ---
    @Test
    public void testNotNullReturnsNull() {
        assertNull(Matchers.notNull());
    }

    // --- nullable() ---
    @Test
    public void testNullableReturnsNull() {
        assertNull(Matchers.nullable(String.class));
        assertNull(Matchers.nullable(Integer.class));
    }

    // --- refEq() ---
    @Test
    public void testRefEqReturnsValue() {
        String value = "test";
        assertSame(value, Matchers.refEq(value));
    }

    @Test
    public void testRefEqWithNull() {
        assertNull(Matchers.refEq(null));
    }

    // --- argThat() ---
    @Test
    public void testArgThatReturnsNull() {
        assertNull(Matchers.argThat(new BaseMatcher<Object>() {
            @Override
            public boolean matches(Object item) {
                return true;
            }
            @Override
            public void describeTo(Description description) {
                description.appendText("always true");
            }
        }));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testArgThatWithNullMatcher() {
        Matchers.argThat(null);
    }

    // --- intThat() ---
    @Test
    public void testIntThatReturnsZero() {
        assertEquals(0, Matchers.intThat(new BaseMatcher<Integer>() {
            @Override
            public boolean matches(Integer item) {
                return true;
            }
            @Override
            public void describeTo(Description description) {
                description.appendText("always true");
            }
        }));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIntThatWithNullMatcher() {
        Matchers.intThat(null);
    }

    // --- Additional edge cases: Arrays and collections ---
    @Test
    public void testAnyListOfType() {
        assertNull(Matchers.anyListOf(String.class));
        assertNull(Matchers.anyListOf(Integer.class));
    }

    @Test
    public void testAnySetOfType() {
        assertNull(Matchers.anySetOf(String.class));
    }

    @Test
    public void testAnyMapOfType() {
        assertNull(Matchers.anyMapOf(String.class, Integer.class));
    }

    @Test
    public void testAnyCollectionOfType() {
        assertNull(Matchers.anyCollectionOf(String.class));
    }

    @Test
    public void testAnyObjectOfType() {
        assertNull(Matchers.anyObject());
        assertNull(Matchers.anyObject(String.class));
    }

    // --- eq with different types ---
    @Test
    public void testEqWithDouble() {
        assertEquals(3.14, Matchers.eq(3.14), 0.0);
    }

    @Test
    public void testEqWithFloat() {
        assertEquals(2.71f, Matchers.eq(2.71f), 0.0f);
    }

    @Test
    public void testEqWithChar() {
        assertEquals('A', Matchers.eq('A').charValue());
    }

    @Test
    public void testEqWithByte() {
        assertEquals((byte) 7, Matchers.eq((byte) 7).byteValue());
    }

    @Test
    public void testEqWithShort() {
        assertEquals((short) 42, Matchers.eq((short) 42).shortValue());
    }

    @Test
    public void testEqWithLong() {
        assertEquals(123456789L, Matchers.eq(123456789L).longValue());
    }

    // --- Boolean eq ---
    @Test
    public void testEqWithBooleanObject() {
        assertEquals(Boolean.TRUE, Matchers.eq(Boolean.TRUE));
        assertEquals(Boolean.FALSE, Matchers.eq(Boolean.FALSE));
        assertNull(Matchers.eq((Boolean) null));
    }

    // --- same with primitive wrappers (should still be same instance) ---
    @Test
    public void testSameWithInteger() {
        Integer value = 100;
        assertSame(value, Matchers.same(value));
    }

    // --- isA with collections ---
    @Test
    public void testIsAWithList() {
        assertNull(Matchers.isA(List.class));
    }

    @Test
    public void testIsAWithMap() {
        assertNull(Matchers.isA(Map.class));
    }

    // --- Null inputs for various matchers that accept objects ---
    @Test(expected = IllegalArgumentException.class)
    public void testContainsWithNullSubstring() {
        Matchers.contains(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEndsWithNullSuffix() {
        Matchers.endsWith(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartsWithNullPrefix() {
        Matchers.startsWith(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMatchesNullRegex() {
        Matchers.matches(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testArgThatNullMatcher() {
        Matchers.argThat(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIntThatNullMatcher() {
        Matchers.intThat(null);
    }

    // --- Ensure that any(Class) does not throw for primitive wrapper types ---
    @Test
    public void testAnyWithPrimitiveWrappers() {
        assertNull(Matchers.any(Integer.class));
        assertNull(Matchers.any(Boolean.class));
        assertNull(Matchers.any(Character.class));
        assertNull(Matchers.any(Byte.class));
        assertNull(Matchers.any(Short.class));
        assertNull(Matchers.any(Long.class));
        assertNull(Matchers.any(Float.class));
        assertNull(Matchers.any(Double.class));
    }

    // --- Edge case: any(Class<Void>) ---
    @Test
    public void testAnyVoid() {
        assertNull(Matchers.any(Void.class));
    }
}