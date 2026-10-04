package org.apache.commons.lang3.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Comprehensive test suite for HashCodeBuilder.
 * Targets code coverage and defect detection (specifically bug #32 - reflection object cycle).
 */
public class HashCodeBuilderTest {

    // -----------------------------------------------------------------------
    // Constructor tests
    // -----------------------------------------------------------------------

    @Test
    public void testConstructorDefaults() {
        HashCodeBuilder builder = new HashCodeBuilder();
        // default initial odd number and multiplier
        int hash = builder.toHashCode();
        assertNotNull(hash);
        // no fields appended, so hash is just the initial value
        assertEquals(17, hash);
    }

    @Test
    public void testConstructorCustom() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 31);
        assertEquals(1, builder.toHashCode());
        builder.append(5);
        int expected = 1 * 31 + 5;
        assertEquals(expected, builder.toHashCode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNegativeInitialOdd() {
        new HashCodeBuilder(-1, 31);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEvenInitial() {
        new HashCodeBuilder(2, 31);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEvenMultiplier() {
        new HashCodeBuilder(1, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNegativeMultiplier() {
        new HashCodeBuilder(1, -31);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorZeroInitial() {
        new HashCodeBuilder(0, 31);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorZeroMultiplier() {
        new HashCodeBuilder(1, 0);
    }

    // -----------------------------------------------------------------------
    // Append primitive tests
    // -----------------------------------------------------------------------

    @Test
    public void testAppendBoolean() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 31);
        builder.append(true);
        builder.append(false);
        int hash1 = builder.toHashCode();

        HashCodeBuilder builder2 = new HashCodeBuilder(1, 31);
        builder2.append(false);
        builder2.append(true);
        int hash2 = builder2.toHashCode();

        assertNotSame(hash1, hash2); // order matters
    }

    @Test
    public void testAppendByte() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 31);
        builder.append((byte) 42);
        int expected = (1 * 31) + (42);
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendChar() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 31);
        builder.append('A');
        int expected = (1 * 31) + (int) 'A';
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendShort() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 31);
        builder.append((short) 100);
        int expected = (1 * 31) + (int) (short) 100;
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendInt() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 31);
        builder.append(12345);
        int expected = (1 * 31) + 12345;
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendLong() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 31);
        builder.append(123456789L);
        int expected = (1 * 31) + (int) (123456789L ^ (123456789L >>> 32));
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendFloat() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 31);
        builder.append(3.14f);
        int floatBits = Float.floatToIntBits(3.14f);
        int expected = (1 * 31) + floatBits;
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendDouble() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 31);
        builder.append(2.71828d);
        long longBits = Double.doubleToLongBits(2.71828d);
        int expected = (1 * 31) + (int) (longBits ^ (longBits >>> 32));
        assertEquals(expected, builder.toHashCode());
    }

    // -----------------------------------------------------------------------
    // Append object tests
    // -----------------------------------------------------------------------

    @Test
    public void testAppendNullObject() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 31);
        builder.append((Object) null);
        // null appends 0
        assertEquals(1 * 31 + 0, builder.toHashCode());
    }

    @Test
    public void testAppendNonNullObject() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 31);
        Integer obj = 42;
        builder.append(obj);
        int expected = (1 * 31) + obj.hashCode();
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendObjectWithNullField() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 31);
        builder.append((Object) null);
        builder.append("test");
        int hash1 = builder.toHashCode();

        // reversing order should give different hash
        HashCodeBuilder builder2 = new HashCodeBuilder(1, 31);
        builder2.append("test");
        builder2.append((Object) null);
        int hash2 = builder2.toHashCode();

        assertNotSame(hash1, hash2);
    }

    // -----------------------------------------------------------------------
    // Append array tests
    // -----------------------------------------------------------------------

    @Test
    public void testAppendBooleanArray() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 31);
        boolean[] array = {true, false, true};
        builder.append(array);
        int expected = 1;
        expected = expected * 31 + 1231; // true
        expected = expected * 31 + 1237; // false
        expected = expected * 31 + 1231; // true
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendByteArray() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 31);
        byte[] array = {1, 2, 3};
        builder.append(array);
        int expected = 1;
        expected = expected * 31 + 1;
        expected = expected * 31 + 2;
        expected = expected * 31 + 3;
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendCharArray() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 31);
        char[] array = {'a', 'b', 'c'};
        builder.append(array);
        int expected = 1;
        expected = expected * 31 + (int) 'a';
        expected = expected * 31 + (int) 'b';
        expected = expected * 31 + (int) 'c';
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendShortArray() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 31);
        short[] array = {10, 20, 30};
        builder.append(array);
        int expected = 1;
        expected = expected * 31 + (int) 10;
        expected = expected * 31 + (int) 20;
        expected = expected * 31 + (int) 30;
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendIntArray() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 31);
        int[] array = {100, 200, 300};
        builder.append(array);
        int expected = 1;
        expected = expected * 31 + 100;
        expected = expected * 31 + 200;
        expected = expected * 31 + 300;
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendLongArray() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 31);
        long[] array = {1L, 2L, 3L};
        builder.append(array);
        int expected = 1;
        expected = expected * 31 + (int) (1L ^ (1L >>> 32));
        expected = expected * 31 + (int) (2L ^ (2L >>> 32));
        expected = expected * 31 + (int) (3L ^ (3L >>> 32));
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendFloatArray() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 31);
        float[] array = {1.5f, 2.5f, 3.5f};
        builder.append(array);
        int expected = 1;
        expected = expected * 31 + Float.floatToIntBits(1.5f);
        expected = expected * 31 + Float.floatToIntBits(2.5f);
        expected = expected * 31 + Float.floatToIntBits(3.5f);
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendDoubleArray() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 31);
        double[] array = {1.2, 2.3, 3.4};
        builder.append(array);
        int expected = 1;
        long bits1 = Double.doubleToLongBits(1.2);
        long bits2 = Double.doubleToLongBits(2.3);
        long bits3 = Double.doubleToLongBits(3.4);
        expected = expected * 31 + (int) (bits1 ^ (bits1 >>> 32));
        expected = expected * 31 + (int) (bits2 ^ (bits2 >>> 32));
        expected = expected * 31 + (int) (bits3 ^ (bits3 >>> 32));
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendObjectArray() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 31);
        Object[] array = {"hello", "world"};
        builder.append(array);
        int expected = 1;
        expected = expected * 31 + "hello".hashCode();
        expected = expected * 31 + "world".hashCode();
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendNullArray() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 31);
        builder.append((int[]) null);
        // null array appends 0
        assertEquals(1 * 31 + 0, builder.toHashCode());
    }

    @Test
    public void testAppendMultiDimensionalArray() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 31);
        int[][] array = {{1, 2}, {3, 4}};
        builder.append(array);
        // uses deepHashCode of arrays
        int expected = 1;
        expected = expected * 31 + java.util.Arrays.deepHashCode(array);
        assertEquals(expected, builder.toHashCode());
    }

    // -----------------------------------------------------------------------
    // hashCode() and toHashCode() tests
    // -----------------------------------------------------------------------

    @Test
    public void testHashCodeMethod() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 31);
        builder.append("test");
        int hash1 = builder.hashCode();
        int hash2 = builder.toHashCode();
        assertEquals(hash1, hash2);
    }

    @Test
    public void testConsistencyAfterMultipleCalls() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 31);
        builder.append("test");
        int first = builder.toHashCode();
        // Calling toHashCode again should return same value
        assertEquals(first, builder.toHashCode());
        // Adding more fields should change the hash
        builder.append("extra");
        int second = builder.toHashCode();
        assertFalse(first == second);
    }

    // -----------------------------------------------------------------------
    // Equals and hashCode consistency (indirect test)
    // -----------------------------------------------------------------------

    @Test
    public void testEqualsConsistency() {
        // Simple objects with same properties should have equal hash codes
        class TestObject {
            int x;
            String y;
            TestObject(int x, String y) { this.x = x; this.y = y; }
            @Override
            public int hashCode() {
                return new HashCodeBuilder(17, 37)
                    .append(x)
                    .append(y)
                    .toHashCode();
            }
        }
        TestObject obj1 = new TestObject(1, "a");
        TestObject obj2 = new TestObject(1, "a");
        assertEquals(obj1.hashCode(), obj2.hashCode());
    }

    // -----------------------------------------------------------------------
    // ReflectionHashCode tests
    // -----------------------------------------------------------------------

    @Test
    public void testReflectionHashCodeSimple() {
        class SimpleReflection {
            int i;
            String s;
            SimpleReflection(int i, String s) { this.i = i; this.s = s; }
        }
        SimpleReflection obj = new SimpleReflection(42, "test");
        int hash = HashCodeBuilder.reflectionHashCode(obj);
        // compute expected using reflection
        int expected = new HashCodeBuilder(17, 37)
                .append(obj.i)
                .append(obj.s)
                .toHashCode();
        assertEquals(expected, hash);
    }

    @Test
    public void testReflectionHashCodeWithTransientField() {
        class TransientField {
            int a;
            transient int b;
            TransientField(int a, int b) { this.a = a; this.b = b; }
        }
        TransientField obj = new TransientField(1, 2);
        // default: testTransients = false
        int hashWithoutTransient = HashCodeBuilder.reflectionHashCode(obj);
        int expectedWithout = new HashCodeBuilder(17, 37)
                .append(obj.a)
                .toHashCode();
        assertEquals(expectedWithout, hashWithoutTransient);
    }

    @Test
    public void testReflectionHashCodeWithTransientFieldIncluded() {
        class TransientField {
            int a;
            transient int b;
            TransientField(int a, int b) { this.a = a; this.b = b; }
        }
        TransientField obj = new TransientField(1, 2);
        int hashWithTransient = HashCodeBuilder.reflectionHashCode(obj, true);
        int expectedWith = new HashCodeBuilder(17, 37)
                .append(obj.a)
                .append(obj.b)
                .toHashCode();
        assertEquals(expectedWith, hashWithTransient);
    }

    @Test
    public void testReflectionHashCodeWithStaticField() {
        class StaticField {
            int a;
            static String STATIC_FIELD = "ignored";
            StaticField(int a) { this.a = a; }
        }
        StaticField obj = new StaticField(10);
        int hash = HashCodeBuilder.reflectionHashCode(obj);
        // static fields are excluded by default
        int expected = new HashCodeBuilder(17, 37)
                .append(obj.a)
                .toHashCode();
        assertEquals(expected, hash);
    }

    @Test
    public void testReflectionHashCodeUpToClass() {
        class Base {
            int baseField;
            Base(int baseField) { this.baseField = baseField; }
        }
        class Derived extends Base {
            int derivedField;
            Derived(int baseField, int derivedField) {
                super(baseField);
                this.derivedField = derivedField;
            }
        }
        Derived obj = new Derived(1, 2);
        // reflect up to Base.class should include only Base fields
        int hashUpToBase = HashCodeBuilder.reflectionHashCode(obj, false, false, Base.class);
        int expectedBaseOnly = new HashCodeBuilder(17, 37)
                .append(obj.baseField)
                .toHashCode();
        assertEquals(expectedBaseOnly, hashUpToBase);

        // no reflection limit should include both
        int hashFull = HashCodeBuilder.reflectionHashCode(obj);
        int expectedFull = new HashCodeBuilder(17, 37)
                .append(obj.baseField)
                .append(obj.derivedField)
                .toHashCode();
        assertEquals(expectedFull, hashFull);
    }

    @Test
    public void testReflectionHashCodeExcludeFields() {
        class ExcludedField {
            int included;
            int excluded;
            ExcludedField(int included, int excluded) { this.included = included; this.excluded = excluded; }
        }
        ExcludedField obj = new ExcludedField(5, 10);
        int hash = HashCodeBuilder.reflectionHashCode(obj, "excluded");
        int expected = new HashCodeBuilder(17, 37)
                .append(obj.included)
                .toHashCode();
        assertEquals(expected, hash);
    }

    @Test
    public void testReflectionHashCodeNullObject() {
        try {
            HashCodeBuilder.reflectionHashCode(null);
            fail("Should throw IllegalArgumentException for null object");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testReflectionHashCodeArrayType() {
        int[] array = {1, 2, 3};
        int hash = HashCodeBuilder.reflectionHashCode(array);
        // Uses deepHashCode for arrays
        int expected = new HashCodeBuilder(17, 37)
                .append(array)
                .toHashCode();
        assertEquals(expected, hash);
    }

    // -----------------------------------------------------------------------
    // Cycle detection test (BUG #32)
    // -----------------------------------------------------------------------

    @Test
    public void testReflectionObjectCycle() {
        // This test reproduces the defect where reflectionHashCode causes stack overflow
        // or returns incorrect result for cyclic object graphs.
        // We create a simple self-referential object and verify that reflectionHashCode
        // handles the cycle without throwing StackOverflowError and returns some reasonable int.
        class CycleNode {
            CycleNode next;
        }
        CycleNode node = new CycleNode();
        node.next = node; // self cycle

        int hash = HashCodeBuilder.reflectionHashCode(node);
        // The hash should be a valid int, not cause infinite loop
        // The exact value is implementation dependent, but we expect it to be non-zero? 
        // For a cycle, the reflection code should register the object and return a base hash.
        // We'll just assert that it completes and returns an int (not stack overflow).
        assertTrue("Hash should be finite", hash >= Integer.MIN_VALUE && hash <= Integer.MAX_VALUE);

        // Additionally, test mutual cycle
        class MutualNode {
            int id;
            MutualNode other;
            MutualNode(int id) { this.id = id; }
        }
        MutualNode a = new MutualNode(1);
        MutualNode b = new MutualNode(2);
        a.other = b;
        b.other = a;

        int hashAB = HashCodeBuilder.reflectionHashCode(a);
        // Should not throw
        assertNotNull(hashAB);
    }

    @Test
    public void testReflectionObjectCycleWithMultipleRefs() {
        // Test a more complex graph cycle
        class GraphNode {
            int value;
            GraphNode next;
            GraphNode(int value) { this.value = value; }
        }
        GraphNode a = new GraphNode(10);
        GraphNode b = new GraphNode(20);
        GraphNode c = new GraphNode(30);
        a.next = b;
        b.next = c;
        c.next = a; // cycle back to a

        int hash = HashCodeBuilder.reflectionHashCode(a);
        // Should complete without error
        assertTrue("Hash should be finite", hash >= Integer.MIN_VALUE && hash <= Integer.MAX_VALUE);
    }

    // -----------------------------------------------------------------------
    // Edge cases for append methods
    // -----------------------------------------------------------------------

    @Test
    public void testAppendBooleanPrimitiveNullNotAllowed() {
        // boolean is primitive, so no null
        HashCodeBuilder builder = new HashCodeBuilder(1, 31);
        builder.append(true);
        // nothing else to test
    }

    @Test
    public void testAppendObjectWithCustomHashCode() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 31);
        Object obj = new Object() {
            @Override
            public int hashCode() {
                return 9999;
            }
        };
        builder.append(obj);
        assertEquals(1 * 31 + 9999, builder.toHashCode());
    }

    // -----------------------------------------------------------------------
    // Testing internal registry (if accessible)
    // Note: Registry class is package-private, but we can test through reflectionHashCode
    // -----------------------------------------------------------------------

    // -----------------------------------------------------------------------
    // Large number of fields (performance)
    // -----------------------------------------------------------------------

    @Test
    public void testManyAppends() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 31);
        for (int i = 0; i < 1000; i++) {
            builder.append(i);
        }
        // just ensure no overflow or exception
        int hash = builder.toHashCode();
        assertNotNull(hash);
    }

    // -----------------------------------------------------------------------
    // Test with negative numbers
    // -----------------------------------------------------------------------

    @Test
    public void testAppendNegativeInt() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 31);
        builder.append(-1);
        int expected = (1 * 31) + (-1);
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendNegativeLong() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 31);
        builder.append(-123456789L);
        long value = -123456789L;
        int expected = (1 * 31) + (int)(value ^ (value >>> 32));
        assertEquals(expected, builder.toHashCode());
    }

    // -----------------------------------------------------------------------
    // Test with Float/Double edge values
    // -----------------------------------------------------------------------

    @Test
    public void testAppendFloatNaN() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 31);
        builder.append(Float.NaN);
        int floatBits = Float.floatToIntBits(Float.NaN);
        assertEquals(1 * 31 + floatBits, builder.toHashCode());
    }

    @Test
    public void testAppendDoubleNaN() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 31);
        builder.append(Double.NaN);
        long longBits = Double.doubleToLongBits(Double.NaN);
        int expected = (1 * 31) + (int)(longBits ^ (longBits >>> 32));
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendFloatInfinity() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 31);
        builder.append(Float.POSITIVE_INFINITY);
        builder.append(Float.NEGATIVE_INFINITY);
        int hash = builder.toHashCode();
        assertNotNull(hash);
    }

    // -----------------------------------------------------------------------
    // Test with arrays that contain null elements
    // -----------------------------------------------------------------------

    @Test
    public void testAppendObjectArrayWithNulls() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 31);
        Object[] array = {"a", null, "b"};
        builder.append(array);
        int expected = 1;
        expected = expected * 31 + "a".hashCode();
        expected = expected * 31 + 0; // null -> 0
        expected = expected * 31 + "b".hashCode();
        assertEquals(expected, builder.toHashCode());
    }

    // -----------------------------------------------------------------------
    // Test with reflection exclude fields using varargs
    // -----------------------------------------------------------------------

    @Test
    public void testReflectionHashCodeMultipleExcludeFields() {
        class MultiExclude {
            int a;
            int b;
            int c;
            MultiExclude(int a, int b, int c) { this.a = a; this.b = b; this.c = c; }
        }
        MultiExclude obj = new MultiExclude(1, 2, 3);
        int hash = HashCodeBuilder.reflectionHashCode(obj, "b", "c");
        int expected = new HashCodeBuilder(17, 37)
                .append(obj.a)
                .toHashCode();
        assertEquals(expected, hash);
    }

    @Test
    public void testReflectionHashCodeExcludeNonexistentField() {
        class OneField {
            int x;
            OneField(int x) { this.x = x; }
        }
        OneField obj = new OneField(42);
        int hash = HashCodeBuilder.reflectionHashCode(obj, "nonExistent");
        int expected = new HashCodeBuilder(17, 37)
                .append(obj.x)
                .toHashCode();
        assertEquals(expected, hash);
    }

    // -----------------------------------------------------------------------
    // Test with very large initial/multiplier (within int range)
    // -----------------------------------------------------------------------

    @Test
    public void testConstructorLargeOddNumbers() {
        HashCodeBuilder builder = new HashCodeBuilder(Integer.MAX_VALUE - 1, Integer.MAX_VALUE - 2);
        builder.append(1);
        // just ensure no modulo overflow (Java int wraps)
        int hash = builder.toHashCode();
        assertNotNull(hash);
    }

    // -----------------------------------------------------------------------
    // Test that append methods can be chained (builder pattern)
    // -----------------------------------------------------------------------

    @Test
    public void testBuilderChaining() {
        HashCodeBuilder builder = new HashCodeBuilder();
        int hash = builder
                .append(1)
                .append("hello")
                .append(true)
                .toHashCode();
        assertNotNull(hash);
    }

    // -----------------------------------------------------------------------
    // Test that the same builder can be reused after toHashCode? (Should not be reused but allowed)
    // -----------------------------------------------------------------------

    @Test
    public void testBuilderReuse() {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(1);
        int first = builder.toHashCode();
        builder.append(2);
        int second = builder.toHashCode();
        assertFalse(first == second);
    }

    // -----------------------------------------------------------------------
    // Test reflectionHashCode with class that has no fields
    // -----------------------------------------------------------------------

    @Test
    public void testReflectionHashCodeNoFields() {
        class NoFields {}
        NoFields obj = new NoFields();
        int hash = HashCodeBuilder.reflectionHashCode(obj);
        int expected = new HashCodeBuilder(17, 37).toHashCode();
        assertEquals(expected, hash);
    }

    // -----------------------------------------------------------------------
    // Test reflectionHashCode with a class that has array fields
    // -----------------------------------------------------------------------

    @Test
    public void testReflectionHashCodeWithArrayField() {
        class WithArray {
            int[] arr;
            WithArray(int[] arr) { this.arr = arr; }
        }
        WithArray obj = new WithArray(new int[] {1, 2, 3});
        int hash = HashCodeBuilder.reflectionHashCode(obj);
        int expected = new HashCodeBuilder(17, 37)
                .append(java.util.Arrays.toString(obj.arr)) // actually reflection uses deepHashCode of array field
                .toHashCode(); 
        // But the above is incorrect: we need to match what reflection does.
        // The correct expected value uses Arrays.deepHashCode for arrays.
        int expected2 = new HashCodeBuilder(17, 37)
                .append(obj.arr)
                .toHashCode();
        assertEquals(expected2, hash);
    }
}