package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ReturnsEmptyValuesTest {

    private ReturnsEmptyValues answer;
    private InvocationOnMock invocation;

    @Before
    public void setUp() {
        answer = new ReturnsEmptyValues();
        invocation = mock(InvocationOnMock.class);
    }

    private void mockReturnType(Class<?> returnType) throws Exception {
        Method method = mock(Method.class);
        when(method.getReturnType()).thenReturn(returnType);
        when(invocation.getMethod()).thenReturn(method);
    }

    @Test
    public void testPrimitiveBoolean() throws Exception {
        mockReturnType(boolean.class);
        Object result = answer.answer(invocation);
        assertEquals(false, result);
    }

    @Test
    public void testPrimitiveByte() throws Exception {
        mockReturnType(byte.class);
        Object result = answer.answer(invocation);
        assertEquals((byte) 0, result);
    }

    @Test
    public void testPrimitiveShort() throws Exception {
        mockReturnType(short.class);
        Object result = answer.answer(invocation);
        assertEquals((short) 0, result);
    }

    @Test
    public void testPrimitiveInt() throws Exception {
        mockReturnType(int.class);
        Object result = answer.answer(invocation);
        assertEquals(0, result);
    }

    @Test
    public void testPrimitiveLong() throws Exception {
        mockReturnType(long.class);
        Object result = answer.answer(invocation);
        assertEquals(0L, result);
    }

    @Test
    public void testPrimitiveFloat() throws Exception {
        mockReturnType(float.class);
        Object result = answer.answer(invocation);
        assertEquals(0.0f, result);
    }

    @Test
    public void testPrimitiveDouble() throws Exception {
        mockReturnType(double.class);
        Object result = answer.answer(invocation);
        assertEquals(0.0, result);
    }

    @Test
    public void testPrimitiveChar() throws Exception {
        mockReturnType(char.class);
        Object result = answer.answer(invocation);
        assertEquals('\u0000', result);
    }

    @Test
    public void testWrapperBoolean() throws Exception {
        mockReturnType(Boolean.class);
        Object result = answer.answer(invocation);
        assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void testWrapperByte() throws Exception {
        mockReturnType(Byte.class);
        Object result = answer.answer(invocation);
        assertEquals(Byte.valueOf((byte) 0), result);
    }

    @Test
    public void testWrapperShort() throws Exception {
        mockReturnType(Short.class);
        Object result = answer.answer(invocation);
        assertEquals(Short.valueOf((short) 0), result);
    }

    @Test
    public void testWrapperInteger() throws Exception {
        mockReturnType(Integer.class);
        Object result = answer.answer(invocation);
        assertEquals(Integer.valueOf(0), result);
    }

    @Test
    public void testWrapperLong() throws Exception {
        mockReturnType(Long.class);
        Object result = answer.answer(invocation);
        assertEquals(Long.valueOf(0L), result);
    }

    @Test
    public void testWrapperFloat() throws Exception {
        mockReturnType(Float.class);
        Object result = answer.answer(invocation);
        assertEquals(Float.valueOf(0.0f), result);
    }

    @Test
    public void testWrapperDouble() throws Exception {
        mockReturnType(Double.class);
        Object result = answer.answer(invocation);
        assertEquals(Double.valueOf(0.0), result);
    }

    @Test
    public void testWrapperCharacter() throws Exception {
        mockReturnType(Character.class);
        Object result = answer.answer(invocation);
        assertEquals(Character.valueOf('\u0000'), result);
    }

    @Test
    public void testString() throws Exception {
        mockReturnType(String.class);
        Object result = answer.answer(invocation);
        assertEquals("", result);
    }

    @Test
    public void testObject() throws Exception {
        mockReturnType(Object.class);
        Object result = answer.answer(invocation);
        assertNull(result);
    }

    @Test
    public void testList() throws Exception {
        mockReturnType(List.class);
        Object result = answer.answer(invocation);
        assertTrue(result instanceof List);
        assertTrue(((List<?>) result).isEmpty());
    }

    @Test
    public void testSet() throws Exception {
        mockReturnType(Set.class);
        Object result = answer.answer(invocation);
        assertTrue(result instanceof Set);
        assertTrue(((Set<?>) result).isEmpty());
    }

    @Test
    public void testSortedSet() throws Exception {
        mockReturnType(SortedSet.class);
        Object result = answer.answer(invocation);
        assertTrue(result instanceof SortedSet);
        assertTrue(((SortedSet<?>) result).isEmpty());
    }

    @Test
    public void testMap() throws Exception {
        mockReturnType(Map.class);
        Object result = answer.answer(invocation);
        assertTrue(result instanceof Map);
        assertTrue(((Map<?, ?>) result).isEmpty());
    }

    @Test
    public void testSortedMap() throws Exception {
        mockReturnType(SortedMap.class);
        Object result = answer.answer(invocation);
        assertTrue(result instanceof SortedMap);
        assertTrue(((SortedMap<?, ?>) result).isEmpty());
    }

    @Test
    public void testArrayOfObjects() throws Exception {
        mockReturnType(String[].class);
        Object result = answer.answer(invocation);
        assertTrue(result instanceof String[]);
        assertEquals(0, ((String[]) result).length);
    }

    @Test
    public void testArrayOfPrimitives() throws Exception {
        mockReturnType(int[].class);
        Object result = answer.answer(invocation);
        assertTrue(result instanceof int[]);
        assertEquals(0, ((int[]) result).length);
    }

    @Test
    public void testOptional() throws Exception {
        mockReturnType(Optional.class);
        Object result = answer.answer(invocation);
        assertNotNull(result);
        assertTrue(result instanceof Optional);
        assertFalse(((Optional<?>) result).isPresent());
    }

    @Test
    public void testOptionalInt() throws Exception {
        mockReturnType(OptionalInt.class);
        Object result = answer.answer(invocation);
        assertNotNull(result);
        assertTrue(result instanceof OptionalInt);
        assertFalse(((OptionalInt) result).isPresent());
    }

    @Test
    public void testOptionalLong() throws Exception {
        mockReturnType(OptionalLong.class);
        Object result = answer.answer(invocation);
        assertNotNull(result);
        assertTrue(result instanceof OptionalLong);
        assertFalse(((OptionalLong) result).isPresent());
    }

    @Test
    public void testOptionalDouble() throws Exception {
        mockReturnType(OptionalDouble.class);
        Object result = answer.answer(invocation);
        assertNotNull(result);
        assertTrue(result instanceof OptionalDouble);
        assertFalse(((OptionalDouble) result).isPresent());
    }

    @Test
    public void testStream() throws Exception {
        mockReturnType(Stream.class);
        Object result = answer.answer(invocation);
        assertNotNull(result);
        assertTrue(result instanceof Stream);
        assertEquals(0, ((Stream<?>) result).count());
    }

    @Test
    public void testIntStream() throws Exception {
        mockReturnType(IntStream.class);
        Object result = answer.answer(invocation);
        assertNotNull(result);
        assertTrue(result instanceof IntStream);
        assertEquals(0, ((IntStream) result).count());
    }

    @Test
    public void testLongStream() throws Exception {
        mockReturnType(LongStream.class);
        Object result = answer.answer(invocation);
        assertNotNull(result);
        assertTrue(result instanceof LongStream);
        assertEquals(0, ((LongStream) result).count());
    }

    @Test
    public void testDoubleStream() throws Exception {
        mockReturnType(DoubleStream.class);
        Object result = answer.answer(invocation);
        assertNotNull(result);
        assertTrue(result instanceof DoubleStream);
        assertEquals(0, ((DoubleStream) result).count());
    }

    @Test
    public void testCustomClassReturnsNull() throws Exception {
        mockReturnType(ReturnsEmptyValuesTest.class);
        Object result = answer.answer(invocation);
        assertNull(result);
    }

    @Test
    public void testVoidReturnType() throws Exception {
        mockReturnType(void.class);
        Object result = answer.answer(invocation);
        assertNull(result);
    }

    @Test(expected = NullPointerException.class)
    public void testNullInvocationThrowsException() throws Exception {
        answer.answer(null);
    }

    @Test
    public void testCollectionSubtypeReturnsEmptyList() throws Exception {
        mockReturnType(ArrayList.class);
        Object result = answer.answer(invocation);
        assertTrue(result instanceof List);
        assertTrue(((List<?>) result).isEmpty());
    }

    @Test
    public void testMapSubtypeReturnsEmptyMap() throws Exception {
        mockReturnType(HashMap.class);
        Object result = answer.answer(invocation);
        assertTrue(result instanceof Map);
        assertTrue(((Map<?, ?>) result).isEmpty());
    }

    @Test
    public void testStringBuilderReturnsEmptyStringBuilder() throws Exception {
        mockReturnType(StringBuilder.class);
        Object result = answer.answer(invocation);
        assertNotNull(result);
        assertTrue(result instanceof StringBuilder);
        assertEquals(0, ((StringBuilder) result).length());
    }

    @Test
    public void testStringBufferReturnsEmptyStringBuffer() throws Exception {
        mockReturnType(StringBuffer.class);
        Object result = answer.answer(invocation);
        assertNotNull(result);
        assertTrue(result instanceof StringBuffer);
        assertEquals(0, ((StringBuffer) result).length());
    }

    @Test
    public void testDateReturnsNull() throws Exception {
        mockReturnType(Date.class);
        Object result = answer.answer(invocation);
        assertNull(result);
    }

    @Test
    public void testEnumReturnsNull() throws Exception {
        mockReturnType(Thread.State.class);
        Object result = answer.answer(invocation);
        assertNull(result);
    }
}