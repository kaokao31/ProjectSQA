package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for ReturnsDeepStubs.
 * Covers primitive defaults, object mocking, final classes, void methods,
 * caching behavior, and generic return types.
 */
@RunWith(MockitoJUnitRunner.class)
public class ReturnsDeepStubsTest {

    // Interfaces for testing deep stubs
    interface DeepStubTarget {
        NonFinalObject getNonFinal();
        int getPrimitiveInt();
        boolean getPrimitiveBoolean();
        long getPrimitiveLong();
        double getPrimitiveDouble();
        String getFinalString();
        Integer getFinalWrapper();
        void doSomething();
        GenericHolder<String> getGeneric();
    }

    interface NonFinalObject {
        String getName();
    }

    static class GenericHolder<T> {
        private T value;
        public T getValue() { return value; }
        public void setValue(T value) { this.value = value; }
    }

    @Mock
    private DeepStubTarget mock;

    @Test
    public void testDeepStubReturnsMockForNonFinalObject() {
        // Given a mock with RETURNS_DEEP_STUBS
        DeepStubTarget deepMock = mock(DeepStubTarget.class, Answers.RETURNS_DEEP_STUBS);
        // When calling a method returning a non-final object
        NonFinalObject result = deepMock.getNonFinal();
        // Then it should return a mock (not null)
        assertNotNull("Deep stub should return a mock for non-final object", result);
        // And that mock should also be a deep stub
        String name = result.getName();
        assertNotNull("Nested deep stub should also return a mock", name);
    }

    @Test
    public void testDeepStubReturnsDefaultForPrimitiveInt() {
        DeepStubTarget deepMock = mock(DeepStubTarget.class, Answers.RETURNS_DEEP_STUBS);
        int result = deepMock.getPrimitiveInt();
        assertEquals("Deep stub should return 0 for int", 0, result);
    }

    @Test
    public void testDeepStubReturnsDefaultForPrimitiveBoolean() {
        DeepStubTarget deepMock = mock(DeepStubTarget.class, Answers.RETURNS_DEEP_STUBS);
        boolean result = deepMock.getPrimitiveBoolean();
        assertFalse("Deep stub should return false for boolean", result);
    }

    @Test
    public void testDeepStubReturnsDefaultForPrimitiveLong() {
        DeepStubTarget deepMock = mock(DeepStubTarget.class, Answers.RETURNS_DEEP_STUBS);
        long result = deepMock.getPrimitiveLong();
        assertEquals("Deep stub should return 0L for long", 0L, result);
    }

    @Test
    public void testDeepStubReturnsDefaultForPrimitiveDouble() {
        DeepStubTarget deepMock = mock(DeepStubTarget.class, Answers.RETURNS_DEEP_STUBS);
        double result = deepMock.getPrimitiveDouble();
        assertEquals("Deep stub should return 0.0 for double", 0.0, result, 0.0);
    }

    @Test
    public void testDeepStubReturnsNullForFinalClassString() {
        DeepStubTarget deepMock = mock(DeepStubTarget.class, Answers.RETURNS_DEEP_STUBS);
        String result = deepMock.getFinalString();
        assertNull("Deep stub should return null for final class String", result);
    }

    @Test
    public void testDeepStubReturnsNullForFinalWrapperInteger() {
        DeepStubTarget deepMock = mock(DeepStubTarget.class, Answers.RETURNS_DEEP_STUBS);
        Integer result = deepMock.getFinalWrapper();
        assertNull("Deep stub should return null for final wrapper Integer", result);
    }

    @Test
    public void testDeepStubHandlesVoidMethod() {
        DeepStubTarget deepMock = mock(DeepStubTarget.class, Answers.RETURNS_DEEP_STUBS);
        // Void method should not throw
        deepMock.doSomething();
        // No assertion needed, just ensure no exception
    }

    @Test
    public void testDeepStubCachesMockForSameMethod() {
        DeepStubTarget deepMock = mock(DeepStubTarget.class, Answers.RETURNS_DEEP_STUBS);
        NonFinalObject first = deepMock.getNonFinal();
        NonFinalObject second = deepMock.getNonFinal();
        assertSame("Deep stub should return the same mock for repeated calls", first, second);
    }

    @Test
    public void testDeepStubWithGenericReturnType() {
        DeepStubTarget deepMock = mock(DeepStubTarget.class, Answers.RETURNS_DEEP_STUBS);
        GenericHolder<String> holder = deepMock.getGeneric();
        assertNotNull("Deep stub should return a mock for generic holder", holder);
        // The nested generic value should also be a deep stub (or default)
        String value = holder.getValue();
        // Since String is final, it should be null
        assertNull("Nested deep stub for final type should be null", value);
    }

    @Test
    public void testDeepStubAnswerDirectly() {
        // Test the ReturnsDeepStubs answer directly using a mock invocation
        ReturnsDeepStubs answer = new ReturnsDeepStubs();
        // Create a mock invocation that returns a non-final type
        InvocationOnMock invocation = mock(InvocationOnMock.class);
        when(invocation.getMethod()).thenReturn(DeepStubTarget.class.getMethods()[0]); // getNonFinal
        when(invocation.getMock()).thenReturn(mock);
        // The answer should return a mock
        Object result = answer.answer(invocation);
        assertNotNull("Direct answer should return a mock", result);
        assertTrue("Result should be a Mockito mock", isMock(result));
    }

    @Test
    public void testDeepStubAnswerForPrimitiveInt() throws Exception {
        ReturnsDeepStubs answer = new ReturnsDeepStubs();
        InvocationOnMock invocation = mock(InvocationOnMock.class);
        when(invocation.getMethod()).thenReturn(DeepStubTarget.class.getMethod("getPrimitiveInt"));
        when(invocation.getMock()).thenReturn(mock);
        Object result = answer.answer(invocation);
        assertEquals("Direct answer for int should return 0", 0, result);
    }

    @Test
    public void testDeepStubAnswerForVoid() throws Exception {
        ReturnsDeepStubs answer = new ReturnsDeepStubs();
        InvocationOnMock invocation = mock(InvocationOnMock.class);
        when(invocation.getMethod()).thenReturn(DeepStubTarget.class.getMethod("doSomething"));
        when(invocation.getMock()).thenReturn(mock);
        Object result = answer.answer(invocation);
        assertNull("Direct answer for void should return null", result);
    }

    @Test
    public void testDeepStubAnswerForFinalString() throws Exception {
        ReturnsDeepStubs answer = new ReturnsDeepStubs();
        InvocationOnMock invocation = mock(InvocationOnMock.class);
        when(invocation.getMethod()).thenReturn(DeepStubTarget.class.getMethod("getFinalString"));
        when(invocation.getMock()).thenReturn(mock);
        Object result = answer.answer(invocation);
        assertNull("Direct answer for final class String should return null", result);
    }
}