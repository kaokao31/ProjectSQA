package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.creation.MockSettingsImpl;
import org.mockito.internal.handler.MockHandlerImpl;
import org.mockito.internal.invocation.InvocationImpl;
import org.mockito.internal.stubbing.InvocationContainerImpl;
import org.mockito.internal.util.MockUtil;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.mock.MockCreationValidator;
import org.mockito.mock.MockSettings;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for ReturnsDeepStubs, targeting high coverage and fault detection.
 */
@RunWith(org.mockito.runners.MockitoJUnitRunner.class)
public class ReturnsDeepStubsTest {

    private ReturnsDeepStubs returnsDeepStubs;

    @Mock
    private InvocationOnMock invocation;

    @Mock
    private MockCreationValidator mockCreationValidator;

    @Mock
    private MockSettingsImpl mockSettings;

    @Mock
    private MockHandlerImpl<?> mockHandler;

    @Mock
    private InvocationContainerImpl invocationContainer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        returnsDeepStubs = new ReturnsDeepStubs();
    }

    @Test
    public void testAnswerWithSimpleReturnType() throws Throwable {
        // Simulate a method that returns a String
        Method toStringMethod = Object.class.getMethod("toString");
        when(invocation.getMethod()).thenReturn(toStringMethod);
        when(invocation.getMock()).thenReturn(mock(Object.class));

        Object result = returnsDeepStubs.answer(invocation);
        assertNotNull("Result should not be null for simple return type", result);
        assertTrue("Result should be a mock", MockUtil.isMock(result));
    }

    @Test
    public void testAnswerWithGenericReturnType() throws Throwable {
        // Simulate a method that returns List<String>
        Method listMethod = SampleClass.class.getMethod("getList");
        when(invocation.getMethod()).thenReturn(listMethod);
        when(invocation.getMock()).thenReturn(mock(SampleClass.class));

        Object result = returnsDeepStubs.answer(invocation);
        assertNotNull("Result should not be null for generic return type", result);
        assertTrue("Result should be a mock", MockUtil.isMock(result));
        assertTrue("Result should be a List", result instanceof List);
    }

    @Test
    public void testAnswerWithPrimitiveReturnType() throws Throwable {
        // Simulate a method that returns int
        Method intMethod = SampleClass.class.getMethod("getInt");
        when(invocation.getMethod()).thenReturn(intMethod);
        when(invocation.getMock()).thenReturn(mock(SampleClass.class));

        Object result = returnsDeepStubs.answer(invocation);
        assertEquals("Primitive int should return default value 0", 0, result);
    }

    @Test
    public void testAnswerWithVoidReturnType() throws Throwable {
        // Simulate a void method
        Method voidMethod = SampleClass.class.getMethod("voidMethod");
        when(invocation.getMethod()).thenReturn(voidMethod);
        when(invocation.getMock()).thenReturn(mock(SampleClass.class));

        Object result = returnsDeepStubs.answer(invocation);
        assertNull("Void method should return null", result);
    }

    @Test
    public void testAnswerWithFinalClassReturnType() throws Throwable {
        // Simulate a method that returns a final class (e.g., String)
        Method stringMethod = SampleClass.class.getMethod("getString");
        when(invocation.getMethod()).thenReturn(stringMethod);
        when(invocation.getMock()).thenReturn(mock(SampleClass.class));

        Object result = returnsDeepStubs.answer(invocation);
        assertNull("Final class should return null (cannot be mocked)", result);
    }

    @Test
    public void testAnswerWithArrayReturnType() throws Throwable {
        // Simulate a method that returns an array
        Method arrayMethod = SampleClass.class.getMethod("getArray");
        when(invocation.getMethod()).thenReturn(arrayMethod);
        when(invocation.getMock()).thenReturn(mock(SampleClass.class));

        Object result = returnsDeepStubs.answer(invocation);
        assertNull("Array return type should return null", result);
    }

    @Test
    public void testAnswerWithNullMock() throws Throwable {
        // When the mock itself is null, answer should handle gracefully
        Method toStringMethod = Object.class.getMethod("toString");
        when(invocation.getMethod()).thenReturn(toStringMethod);
        when(invocation.getMock()).thenReturn(null);

        Object result = returnsDeepStubs.answer(invocation);
        assertNull("Result should be null when mock is null", result);
    }

    @Test
    public void testRecordDeepStubMockCreatesMockWithCorrectSettings() throws Throwable {
        // Use reflection to access private method recordDeepStubMock
        Method recordMethod = ReturnsDeepStubs.class.getDeclaredMethod("recordDeepStubMock", InvocationOnMock.class, Object.class);
        recordMethod.setAccessible(true);

        // Create a mock for the invocation
        InvocationOnMock mockInvocation = mock(InvocationOnMock.class);
        Method sampleMethod = SampleClass.class.getMethod("getList");
        when(mockInvocation.getMethod()).thenReturn(sampleMethod);
        when(mockInvocation.getMock()).thenReturn(mock(SampleClass.class));

        // Call recordDeepStubMock
        Object deepStub = recordMethod.invoke(returnsDeepStubs, mockInvocation, mockInvocation.getMock());
        assertNotNull("Deep stub should not be null", deepStub);
        assertTrue("Deep stub should be a mock", MockUtil.isMock(deepStub));
    }

    @Test
    public void testAnswerCachesSameMockForSameInvocation() throws Throwable {
        // Simulate two identical invocations
        Method toStringMethod = Object.class.getMethod("toString");
        when(invocation.getMethod()).thenReturn(toStringMethod);
        when(invocation.getMock()).thenReturn(mock(Object.class));

        Object firstResult = returnsDeepStubs.answer(invocation);
        Object secondResult = returnsDeepStubs.answer(invocation);
        assertSame("Same invocation should return same mock", firstResult, secondResult);
    }

    @Test
    public void testAnswerWithGenericTypeVariable() throws Throwable {
        // Simulate a method that returns a type variable (e.g., T)
        Method typeVarMethod = GenericClass.class.getMethod("getT");
        when(invocation.getMethod()).thenReturn(typeVarMethod);
        when(invocation.getMock()).thenReturn(mock(GenericClass.class));

        Object result = returnsDeepStubs.answer(invocation);
        assertNotNull("Result should not be null for type variable", result);
        assertTrue("Result should be a mock", MockUtil.isMock(result));
    }

    @Test
    public void testAnswerWithParameterizedType() throws Throwable {
        // Simulate a method that returns List<String>
        Method paramMethod = SampleClass.class.getMethod("getList");
        when(invocation.getMethod()).thenReturn(paramMethod);
        when(invocation.getMock()).thenReturn(mock(SampleClass.class));

        Object result = returnsDeepStubs.answer(invocation);
        assertNotNull("Result should not be null for parameterized type", result);
        assertTrue("Result should be a List", result instanceof List);
    }

    @Test
    public void testAnswerWithNullReturnType() throws Throwable {
        // Simulate a method that returns null (should not happen, but test edge)
        Method nullMethod = SampleClass.class.getMethod("getNull");
        when(invocation.getMethod()).thenReturn(nullMethod);
        when(invocation.getMock()).thenReturn(mock(SampleClass.class));

        Object result = returnsDeepStubs.answer(invocation);
        assertNull("Result should be null for null return type", result);
    }

    // Helper classes for reflection
    public static class SampleClass {
        public List<String> getList() { return null; }
        public int getInt() { return 0; }
        public void voidMethod() {}
        public String getString() { return null; }
        public int[] getArray() { return null; }
        public Object getNull() { return null; }
    }

    public static class GenericClass<T> {
        public T getT() { return null; }
    }
}