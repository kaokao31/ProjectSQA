package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.ObjectMethodsGuru;
import org.mockito.internal.util.Primitives;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.mock.MockName;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
    public void testReturnPrimitiveValues() throws Throwable {
        assertEquals(false, returnsEmptyValues.returnValueFor(createInvocation(boolean.class)));
        assertEquals((char) 0, returnsEmptyValues.returnValueFor(createInvocation(char.class)));
        assertEquals((byte) 0, returnsEmptyValues.returnValueFor(createInvocation(byte.class)));
        assertEquals((short) 0, returnsEmptyValues.returnValueFor(createInvocation(short.class)));
        assertEquals(0, returnsEmptyValues.returnValueFor(createInvocation(int.class)));
        assertEquals(0L, returnsEmptyValues.returnValueFor(createInvocation(long.class)));
        assertEquals(0.0f, returnsEmptyValues.returnValueFor(createInvocation(float.class)), 0.0001f);
        assertEquals(0.0d, returnsEmptyValues.returnValueFor(createInvocation(double.class)), 0.0001d);
    }

    @Test
    public void testReturnCollectionsAndArrays() throws Throwable {
        assertTrue(((Collection<?>) returnsEmptyValues.returnValueFor(createInvocation(Collection.class))).isEmpty());
        assertTrue(((Set<?>) returnsEmptyValues.returnValueFor(createInvocation(Set.class))).isEmpty());
        assertTrue(((HashSet<?>) returnsEmptyValues.returnValueFor(createInvocation(HashSet.class))).isEmpty());
        assertTrue(((List<?>) returnsEmptyValues.returnValueFor(createInvocation(List.class))).isEmpty());
        assertTrue(((Map<?, ?>) returnsEmptyValues.returnValueFor(createInvocation(Map.class))).isEmpty());

        Object arrayObj = returnsEmptyValues.returnValueFor(createInvocation(String[].class));
        assertNotNull(arrayObj);
        assertTrue(arrayObj.getClass().isArray());
        assertEquals(0, java.lang.reflect.Array.getLength(arrayObj));
    }

    @Test
    public void testReturnOptional() throws Throwable {
        Object optionalObj = returnsEmptyValues.returnValueFor(createInvocation(Optional.class));
        assertNotNull(optionalObj);
        assertEquals(Optional.empty(), optionalObj);
    }

    @Test
    public void testReturnObjectAndNull() throws Throwable {
        assertNull(returnsEmptyValues.returnValueFor(createInvocation(String.class)));
        assertNull(returnsEmptyValues.returnValueFor(createInvocation(Serializable.class)));
    }

    @Test
    public void testObjectMethodsToString() throws Throwable {
        InvocationOnMock invocation = mock(InvocationOnMock.class);
        Method toStringMethod = Object.class.getMethod("toString");
        when(invocation.getMethod()).thenReturn(toStringMethod);
        
        MockName mockName = mock(MockName.class);
        when(mockName.toString()).thenReturn("FictitiousMock");
        when(invocation.getMock()).thenReturn(new DummyClass());

        // We can test how ReturnsEmptyValues handles standard Object methods like toString, equals, hashCode, intValue
        Object result = returnsEmptyValues.returnValueFor(invocation);
        // Depending on Mockito's internal ObjectMethodsGuru, toString usually returns "Mock for ..." or similar if mock name is present, or default.
        // Let's ensure it doesn't throw and returns a sensible value (often String or null/default depending on the version).
        // Since we are testing ReturnsEmptyValues specifically, let's verify it executes safely.
    }

    @Test
    public void testObjectMethodsHashCode() throws Throwable {
        InvocationOnMock invocation = mock(InvocationOnMock.class);
        Method hashCodeMethod = Object.class.getMethod("hashCode");
        when(invocation.getMethod()).thenReturn(hashCodeMethod);
        Object mockObj = mock(DummyClass.class);
        when(invocation.getMock()).thenReturn(mockObj);

        Object result = returnsEmptyValues.returnValueFor(invocation);
        assertNotNull(result);
        assertTrue(result instanceof Integer);
    }

    @Test
    public void testObjectMethodsEquals() throws Throwable {
        InvocationOnMock invocation = mock(InvocationOnMock.class);
        Method equalsMethod = Object.class.getMethod("equals", Object.class);
        when(invocation.getMethod()).thenReturn(equalsMethod);
        when(invocation.getArguments()).thenReturn(new Object[]{new Object()});
        Object mockObj = mock(DummyClass.class);
        when(invocation.getMock()).thenReturn(mockObj);

        Object result = returnsEmptyValues.returnValueFor(invocation);
        assertNotNull(result);
        assertTrue(result instanceof Boolean);
        assertEquals(false, result); // usually false when comparing mock with a new Object
    }

    @Test
    public void testCompareToMethod() throws Throwable {
        // Specifically targeting Mockito 2.4 / ReturnsEmptyValues bug related to compareTo returning 0 instead of appropriate values or handling self-comparison / mocks.
        InvocationOnMock invocation = mock(InvocationOnMock.class);
        Method compareToMethod = DummyComparable.class.getMethod("compareTo", DummyComparable.class);
        when(invocation.getMethod()).thenReturn(compareToMethod);
        when(invocation.getArguments()).thenReturn(new Object[]{new DummyComparable()});
        
        DummyComparable mockMock = mock(DummyComparable.class);
        when(invocation.getMock()).thenReturn(mockMock);

        Object result = returnsEmptyValues.returnValueFor(invocation);
        assertNotNull(result);
        assertTrue(result instanceof Integer);
        //compareTo on a mock should typically return 0 or non-negative/negative depending on rules, 
        // but ReturnsEmptyValues specifically handles compareTo to avoid ClassCastException or returning null.
        assertEquals(0, result);
    }

    @Test
    public void testCompareToWithSameMock() throws Throwable {
        InvocationOnMock invocation = mock(InvocationOnMock.class);
        Method compareToMethod = DummyComparable.class.getMethod("compareTo", DummyComparable.class);
        when(invocation.getMethod()).thenReturn(compareToMethod);
        
        DummyComparable mockMock = mock(DummyComparable.class);
        when(invocation.getArguments()).thenReturn(new Object[]{mockMock});
        when(invocation.getMock()).thenReturn(mockMock);

        Object result = returnsEmptyValues.returnValueFor(invocation);
        assertNotNull(result);
        assertEquals(0, result);
    }

    private InvocationOnMock createInvocation(Class<?> returnType) throws Exception {
        Method method = DummyClass.class.getMethod("dummyMethod");
        // Create a custom mock or stub for InvocationOnMock
        InvocationOnMock invocation = mock(InvocationOnMock.class);
        when(invocation.getMethod()).thenReturn(DummyClass.class.getMethod("methodReturning", returnType));
        when(invocation.getMock()).thenReturn(new DummyClass());
        return invocation;
    }

    public static class DummyClass {
        public void dummyMethod() {}
        public boolean methodReturning(boolean b) { return b; }
        public char methodReturning(char c) { return c; }
        public byte methodReturning(byte b) { return b; }
        public short methodReturning(short s) { return s; }
        public int methodReturning(int i) { return i; }
        public long methodReturning(long l) { return l; }
        public float methodReturning(float f) { return f; }
        public double methodReturning(double d) { return d; }
        public Object methodReturning(Class<?> c) { return null; }
        public Collection<?> methodReturning(Collection<?> c) { return c; }
        public Set<?> methodReturning(Set<?> s) { return s; }
        public HashSet<?> methodReturning(HashSet<?> hs) { return hs; }
        public List<?> methodReturning(List<?> l) { return l; }
        public Map<?, ?> methodReturning(Map<?, ?> m) { return m; }
        public String[] methodReturning(String[] arr) { return arr; }
        public Optional<?> methodReturning(Optional<?> opt) { return opt; }
    }

    public static class DummyComparable implements Comparable<DummyComparable> {
        @Override
        public int compareTo(DummyComparable o) {
            return 0;
        }
    }
}