package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Callable;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReturnsEmptyValuesTest {

    private ReturnsEmptyValues returnsEmptyValues;

    @Before
    public void setUp() {
        returnsEmptyValues = new ReturnsEmptyValues();
    }

    @Test
    public void testPrimitiveBoolean() throws Throwable {
        Object result = returnsEmptyValues.answer(mockInvocation("booleanValue", boolean.class));
        assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void testPrimitiveChar() throws Throwable {
        Object result = returnsEmptyValues.answer(mockInvocation("charValue", char.class));
        assertEquals(Character.valueOf((char) 0), result);
    }

    @Test
    public void testPrimitiveByte() throws Throwable {
        Object result = returnsEmptyValues.answer(mockInvocation("byteValue", byte.class));
        assertEquals(Byte.valueOf((byte) 0), result);
    }

    @Test
    public void testPrimitiveShort() throws Throwable {
        Object result = returnsEmptyValues.answer(mockInvocation("shortValue", short.class));
        assertEquals(Short.valueOf((short) 0), result);
    }

    @Test
    public void testPrimitiveInt() throws Throwable {
        Object result = returnsEmptyValues.answer(mockInvocation("intValue", int.class));
        assertEquals(Integer.valueOf(0), result);
    }

    @Test
    public void testPrimitiveLong() throws Throwable {
        Object result = returnsEmptyValues.answer(mockInvocation("longValue", long.class));
        assertEquals(Long.valueOf(0L), result);
    }

    @Test
    public void testPrimitiveFloat() throws Throwable {
        Object result = returnsEmptyValues.answer(mockInvocation("floatValue", float.class));
        assertEquals(Float.valueOf(0.0f), result);
    }

    @Test
    public void testPrimitiveDouble() throws Throwable {
        Object result = returnsEmptyValues.answer(mockInvocation("doubleValue", double.class));
        assertEquals(Double.valueOf(0.0d), result);
    }

    @Test
    public void testStringReturnsEmptyString() throws Throwable {
        Object result = returnsEmptyValues.answer(mockInvocation("stringValue", String.class));
        assertEquals("", result);
    }

    @Test
    public void testObjectArrayReturnsEmptyArray() throws Throwable {
        Object result = returnsEmptyValues.answer(mockInvocation("stringArray", String[].class));
        assertTrue(result instanceof String[]);
        assertEquals(0, ((String[]) result).length);
    }

    @Test
    public void testPrimitiveArrayReturnsEmptyArray() throws Throwable {
        Object result = returnsEmptyValues.answer(mockInvocation("intArray", int[].class));
        assertTrue(result instanceof int[]);
        assertEquals(0, ((int[]) result).length);
    }

    @Test
    public void testListReturnsEmptyCollection() throws Throwable {
        Object result = returnsEmptyValues.answer(mockInvocation("listValue", List.class));
        assertTrue(result instanceof List);
        assertTrue(((List<?>) result).isEmpty());
    }

    @Test
    public void testSetReturnsEmptySet() throws Throwable {
        Object result = returnsEmptyValues.answer(mockInvocation("setValue", Set.class));
        assertTrue("Expected an empty Set but was " + result.getClass().getName(),
                result instanceof Set);
        assertTrue(((Set<?>) result).isEmpty());
    }

    @Test
    public void testSortedSetReturnsEmptySortedSet() throws Throwable {
        Object result = returnsEmptyValues.answer(mockInvocation("sortedSetValue", SortedSet.class));
        assertTrue("Expected a SortedSet but was " + result.getClass().getName(),
                result instanceof SortedSet);
        assertTrue(((SortedSet<?>) result).isEmpty());
    }

    @Test
    public void testMapReturnsEmptyMap() throws Throwable {
        Object result = returnsEmptyValues.answer(mockInvocation("mapValue", Map.class));
        assertTrue(result instanceof Map);
        assertTrue(((Map<?, ?>) result).isEmpty());
    }

    @Test
    public void testIterableReturnsEmptyIterable() throws Throwable {
        Object result = returnsEmptyValues.answer(mockInvocation("iterableValue", Iterable.class));
        assertTrue(result instanceof Iterable);
        assertFalse(((Iterable<?>) result).iterator().hasNext());
    }

    @Test
    public void testObjectReturnsNull() throws Throwable {
        Object result = returnsEmptyValues.answer(mockInvocation("objectValue", Object.class));
        assertNull(result);
    }

    @Test
    public void testCustomClassReturnsNull() throws Throwable {
        Object result = returnsEmptyValues.answer(mockInvocation("callableValue", Callable.class));
        assertNull(result);
    }

    @Test
    public void testCollectionSubtypeReturnsCompatibleEmptyCollection() throws Throwable {
        // Regression test for Mockito bug #18 – should return a collection assignable to the declared type.
        Object result = returnsEmptyValues.answer(mockInvocation("linkedListValue", LinkedList.class));
        assertTrue(result instanceof LinkedList);
        assertTrue(((LinkedList<?>) result).isEmpty());
    }

    private InvocationOnMock mockInvocation(String methodName, Class<?> returnType) throws NoSuchMethodException {
        InvocationOnMock invocation = mock(InvocationOnMock.class);
        Method method = DummyMethods.class.getMethod(methodName);
        when(invocation.getMethod()).thenReturn(method);
        return invocation;
    }

    // Dummy class containing methods that match the required return types.
    public static class DummyMethods {
        public boolean booleanValue() { return false; }
        public char charValue() { return 0; }
        public byte byteValue() { return 0; }
        public short shortValue() { return 0; }
        public int intValue() { return 0; }
        public long longValue() { return 0L; }
        public float floatValue() { return 0.0f; }
        public double doubleValue() { return 0.0d; }
        public String stringValue() { return ""; }
        public String[] stringArray() { return new String[0]; }
        public int[] intArray() { return new int[0]; }
        public List listValue() { return null; }
        public Set setValue() { return null; }
        public SortedSet sortedSetValue() { return null; }
        public Map mapValue() { return null; }
        public Iterable iterableValue() { return null; }
        public Object objectValue() { return null; }
        public Callable callableValue() { return null; }
        public LinkedList linkedListValue() { return null; }
    }
}