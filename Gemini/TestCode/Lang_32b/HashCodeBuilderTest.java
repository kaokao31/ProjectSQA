package org.apache.commons.lang3.builder;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Set;

public class HashCodeBuilderTest {

    static class TestObject {
        private int a;

        public TestObject(int a) {
            this.a = a;
        }

        public int getA() {
            return a;
        }

        public void setA(int a) {
            this.a = a;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof TestObject)) {
                return false;
            }
            TestObject rhs = (TestObject) o;
            return a == rhs.a;
        }

        @Override
        public int hashCode() {
            return a;
        }
    }

    static class TestSubObject extends TestObject {
        private int b;

        @SuppressWarnings("unused")
        private transient int t;

        public TestSubObject() {
            super(0);
        }

        public TestSubObject(int a, int b, int t) {
            super(a);
            this.b = b;
            this.t = t;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof TestSubObject)) {
                return false;
            }
            TestSubObject rhs = (TestSubObject) o;
            return super.equals(o) && b == rhs.b;
        }

        @Override
        public int hashCode() {
            return b * 17 + super.hashCode();
        }
    }

    static class ReflectionTestCycleA {
        ReflectionTestCycleB b;

        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }
    }

    static class ReflectionTestCycleB {
        ReflectionTestCycleA a;

        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }
    }

    @Test
    public void testConstructor() {
        assertEquals(17 * 37, new HashCodeBuilder().toHashCode());
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).toHashCode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorZeroInitialEven() {
        new HashCodeBuilder(0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEvenInitial() {
        new HashCodeBuilder(2, 3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEvenMultiplier() {
        new HashCodeBuilder(3, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNegativeEven() {
        new HashCodeBuilder(-2, 3);
    }

    @Test
    public void testReflectionHashCode() {
        assertEquals(17 * 37, HashCodeBuilder.reflectionHashCode(new TestObject(0)));
        assertEquals(17 * 37 + 12345, HashCodeBuilder.reflectionHashCode(new TestObject(12345)));
    }

    @Test
    public void testReflectionHierarchyHashCode() {
        assertEquals(17 * 37 * 37, HashCodeBuilder.reflectionHashCode(new TestSubObject(0, 0, 0)));
        assertEquals(17 * 37 * 37 + 12345, HashCodeBuilder.reflectionHashCode(new TestSubObject(0, 12345, 0)));
        assertEquals((17 * 37 + 12345) * 37 + 67890, HashCodeBuilder.reflectionHashCode(new TestSubObject(12345, 67890, 0)));
    }

    @Test
    public void testReflectionHierarchyHashCodeWithTransients() {
        assertEquals(17 * 37 * 37 * 37, HashCodeBuilder.reflectionHashCode(new TestSubObject(0, 0, 0), true));
        assertEquals((17 * 37 * 37 + 12345) * 37 + 99999, HashCodeBuilder.reflectionHashCode(new TestSubObject(0, 12345, 99999), true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReflectionHashCodeNullObject() {
        HashCodeBuilder.reflectionHashCode(null);
    }

    @Test
    public void testReflectionHashCodeExcludeFields() {
        TestSubObject obj = new TestSubObject(1, 2, 3);
        int expected = (17 * 37 + 1) * 37;
        int actual = HashCodeBuilder.reflectionHashCode(obj, "b");
        assertEquals(expected, actual);
    }

    @Test
    public void testReflectionHashCodeExcludeFieldsCollection() {
        TestSubObject obj = new TestSubObject(1, 2, 3);
        Collection<String> excludes = java.util.Collections.singletonList("b");
        int expected = (17 * 37 + 1) * 37;
        int actual = HashCodeBuilder.reflectionHashCode(17, 37, obj, false, null, "b");
        assertEquals(expected, actual);
        int actualCollection = HashCodeBuilder.reflectionHashCode(17, 37, obj, false, null, excludes.toArray(new String[0]));
        assertEquals(expected, actualCollection);
    }

    @Test
    public void testReflectionHashCodeWithSuperclass() {
        TestSubObject obj = new TestSubObject(1, 2, 3);
        int actual = HashCodeBuilder.reflectionHashCode(17, 37, obj, false, TestSubObject.class);
        int expected = 17 * 37 + 2;
        assertEquals(expected, actual);
    }

    @Test
    public void testReflectionObjectCycle() {
        ReflectionTestCycleA a = new ReflectionTestCycleA();
        ReflectionTestCycleB b = new ReflectionTestCycleB();
        a.b = b;
        b.a = a;

        // Ensure reflectionHashCode doesn't cause StackOverflowError
        int hashA = a.hashCode();
        int hashB = b.hashCode();
        assertTrue(hashA != 0);
        assertTrue(hashB != 0);

        // Registry should be clean after reflection calculation
        Set<IDKey> registry = HashCodeBuilder.getRegistry();
        assertTrue(registry == null || registry.isEmpty());
    }

    @Test
    public void testAppendSuper() {
        assertEquals(17 * 37 + 100, new HashCodeBuilder(17, 37).appendSuper(100).toHashCode());
    }

    @Test
    public void testBoolean() {
        assertEquals(17 * 37 + 0, new HashCodeBuilder(17, 37).append(true).toHashCode());
        assertEquals(17 * 37 + 1, new HashCodeBuilder(17, 37).append(false).toHashCode());
    }

    @Test
    public void testBooleanArray() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((boolean[]) null).toHashCode());
        assertEquals(17 * 37 * 37, new HashCodeBuilder(17, 37).append(new boolean[0]).toHashCode());
        assertEquals((17 * 37 + 0) * 37 + 1, new HashCodeBuilder(17, 37).append(new boolean[]{true, false}).toHashCode());
    }

    @Test
    public void testByte() {
        assertEquals(17 * 37 + 123, new HashCodeBuilder(17, 37).append((byte) 123).toHashCode());
    }

    @Test
    public void testByteArray() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((byte[]) null).toHashCode());
        assertEquals(17 * 37 * 37, new HashCodeBuilder(17, 37).append(new byte[0]).toHashCode());
        assertEquals((17 * 37 + 5) * 37 + 6, new HashCodeBuilder(17, 37).append(new byte[]{5, 6}).toHashCode());
    }

    @Test
    public void testChar() {
        assertEquals(17 * 37 + (int) 'a', new HashCodeBuilder(17, 37).append('a').toHashCode());
    }

    @Test
    public void testCharArray() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((char[]) null).toHashCode());
        assertEquals(17 * 37 * 37, new HashCodeBuilder(17, 37).append(new char[0]).toHashCode());
        assertEquals((17 * 37 + (int) 'a') * 37 + (int) 'b', new HashCodeBuilder(17, 37).append(new char[]{'a', 'b'}).toHashCode());
    }

    @Test
    public void testDouble() {
        assertEquals(17 * 37 + (int) (Double.doubleToLongBits(1.23d) ^ (Double.doubleToLongBits(1.23d) >>> 32)),
                new HashCodeBuilder(17, 37).append(1.23d).toHashCode());
    }

    @Test
    public void testDoubleArray() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((double[]) null).toHashCode());
        assertEquals(17 * 37 * 37, new HashCodeBuilder(17, 37).append(new double[0]).toHashCode());
        long d1 = Double.doubleToLongBits(1.0);
        long d2 = Double.doubleToLongBits(2.0);
        int expected = (17 * 37 + (int) (d1 ^ (d1 >>> 32))) * 37 + (int) (d2 ^ (d2 >>> 32));
        assertEquals(expected, new HashCodeBuilder(17, 37).append(new double[]{1.0, 2.0}).toHashCode());
    }

    @Test
    public void testFloat() {
        assertEquals(17 * 37 + Float.floatToIntBits(1.23f), new HashCodeBuilder(17, 37).append(1.23f).toHashCode());
    }

    @Test
    public void testFloatArray() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((float[]) null).toHashCode());
        assertEquals(17 * 37 * 37, new HashCodeBuilder(17, 37).append(new float[0]).toHashCode());
        int f1 = Float.floatToIntBits(1.0f);
        int f2 = Float.floatToIntBits(2.0f);
        int expected = (17 * 37 + f1) * 37 + f2;
        assertEquals(expected, new HashCodeBuilder(17, 37).append(new float[]{1.0f, 2.0f}).toHashCode());
    }

    @Test
    public void testInt() {
        assertEquals(17 * 37 + 12345, new HashCodeBuilder(17, 37).append(12345).toHashCode());
    }

    @Test
    public void testIntArray() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((int[]) null).toHashCode());
        assertEquals(17 * 37 * 37, new HashCodeBuilder(17, 37).append(new int[0]).toHashCode());
        assertEquals((17 * 37 + 1) * 37 + 2, new HashCodeBuilder(17, 37).append(new int[]{1, 2}).toHashCode());
    }

    @Test
    public void testLong() {
        long l = 1234567890123L;
        assertEquals(17 * 37 + (int) (l ^ (l >>> 32)), new HashCodeBuilder(17, 37).append(l).toHashCode());
    }

    @Test
    public void testLongArray() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((long[]) null).toHashCode());
        assertEquals(17 * 37 * 37, new HashCodeBuilder(17, 37).append(new long[0]).toHashCode());
        long l1 = 123L;
        long l2 = 456L;
        int expected = (17 * 37 + (int) (l1 ^ (l1 >>> 32))) * 37 + (int) (l2 ^ (l2 >>> 32));
        assertEquals(expected, new HashCodeBuilder(17, 37).append(new long[]{l1, l2}).toHashCode());
    }

    @Test
    public void testObject() {
        Object obj = new Object();
        assertEquals(17 * 37 + obj.hashCode(), new HashCodeBuilder(17, 37).append(obj).toHashCode());
        assertEquals(17 * 37 * 37, new HashCodeBuilder(17, 37).append((Object) null).toHashCode());
    }

    @Test
    public void testObjectArray() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((Object[]) null).toHashCode());
        assertEquals(17 * 37 * 37, new HashCodeBuilder(17, 37).append(new Object[0]).toHashCode());
        Object obj1 = new Object();
        Object obj2 = new Object();
        int expected = (17 * 37 + obj1.hashCode()) * 37 + obj2.hashCode();
        assertEquals(expected, new HashCodeBuilder(17, 37).append(new Object[]{obj1, obj2}).toHashCode());
    }

    @Test
    public void testObjectArrayAsObject() {
        Object[] array = new Object[]{"a", "b"};
        int expected = new HashCodeBuilder(17, 37).append(array).toHashCode();
        int actual = new HashCodeBuilder(17, 37).append((Object) array).toHashCode();
        assertEquals(expected, actual);
    }

    @Test
    public void testMultiDimensionalArray() {
        int[][] multiInt = new int[][]{{1, 2}, {3, 4}};
        int hash1 = new HashCodeBuilder(17, 37).append(multiInt).toHashCode();
        int hash2 = new HashCodeBuilder(17, 37).append(multiInt).toHashCode();
        assertEquals(hash1, hash2);

        long[][] multiLong = new long[][]{{1L, 2L}, {3L, 4L}};
        assertEquals(new HashCodeBuilder(17, 37).append(multiLong).toHashCode(),
                new HashCodeBuilder(17, 37).append(multiLong).toHashCode());
    }

    @Test
    public void testShort() {
        assertEquals(17 * 37 + (short) 123, new HashCodeBuilder(17, 37).append((short) 123).toHashCode());
    }

    @Test
    public void testShortArray() {
        assertEquals(17 * 37, new HashCodeBuilder(17, 37).append((short[]) null).toHashCode());
        assertEquals(17 * 37 * 37, new HashCodeBuilder(17, 37).append(new short[0]).toHashCode());
        assertEquals((17 * 37 + 1) * 37 + 2, new HashCodeBuilder(17, 37).append(new short[]{1, 2}).toHashCode());
    }

    @Test
    public void testBuild() {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(123);
        assertEquals(Integer.valueOf(builder.toHashCode()), builder.build());
    }

    @Test
    public void testHashCodeSelf() {
        HashCodeBuilder builder = new HashCodeBuilder();
        assertEquals(builder.toHashCode(), builder.hashCode());
    }
}