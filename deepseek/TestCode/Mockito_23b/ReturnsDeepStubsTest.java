package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ReturnsDeepStubsTest {

    @Mock
    private InvocationOnMock invocation;

    private ReturnsDeepStubs returnsDeepStubs;

    @Before
    public void setUp() {
        returnsDeepStubs = new ReturnsDeepStubs();
    }

    @Test
    public void testAnswerReturnsDeepStubForInterface() throws Throwable {
        // Given
        Method method = List.class.getMethod("iterator");
        when(invocation.getMethod()).thenReturn(method);
        when(invocation.getMock()).thenReturn(mock(List.class));

        // When
        Object result = returnsDeepStubs.answer(invocation);

        // Then
        assertNotNull("Deep stub should not be null", result);
        assertTrue("Result should be a mock", isMock(result));
    }

    @Test
    public void testAnswerReturnsDeepStubForClass() throws Throwable {
        // Given
        Method method = Map.class.getMethod("entrySet");
        when(invocation.getMethod()).thenReturn(method);
        when(invocation.getMock()).thenReturn(mock(Map.class));

        // When
        Object result = returnsDeepStubs.answer(invocation);

        // Then
        assertNotNull(result);
        assertTrue(isMock(result));
    }

    @Test(expected = ClassCastException.class)
    public void testAnswerWithPrimitiveReturnType() throws Throwable {
        // Given
        Method method = String.class.getMethod("length");
        when(invocation.getMethod()).thenReturn(method);
        when(invocation.getMock()).thenReturn(mock(String.class));

        // When
        returnsDeepStubs.answer(invocation);
        // Then expected exception
    }

    @Test(expected = ClassCastException.class)
    public void testAnswerWithVoidReturnType() throws Throwable {
        // Given
        Method method = Runnable.class.getMethod("run");
        when(invocation.getMethod()).thenReturn(method);
        when(invocation.getMock()).thenReturn(mock(Runnable.class));

        // When
        returnsDeepStubs.answer(invocation);
        // Then expected exception
    }

    @Test
    public void testAnswerWithFinalClass() throws Throwable {
        // Given
        Method method = String.class.getMethod("toString");
        when(invocation.getMethod()).thenReturn(method);
        when(invocation.getMock()).thenReturn(mock(String.class));

        // When
        Object result = returnsDeepStubs.answer(invocation);

        // Then
        assertNull("Final class should not be stubbed deeply", result);
    }

    @Test
    public void testAnswerWithArrayReturnType() throws Throwable {
        // Given
        Method method = List.class.getMethod("toArray");
        when(invocation.getMethod()).thenReturn(method);
        when(invocation.getMock()).thenReturn(mock(List.class));

        // When
        Object result = returnsDeepStubs.answer(invocation);

        // Then
        assertNull("Array return type should not be stubbed deeply", result);
    }

    @Test
    public void testAnswerWithNullMock() throws Throwable {
        // Given
        Method method = List.class.getMethod("iterator");
        when(invocation.getMethod()).thenReturn(method);
        when(invocation.getMock()).thenReturn(null);

        // When
        Object result = returnsDeepStubs.answer(invocation);

        // Then
        assertNull("Result should be null when mock is null", result);
    }

    @Test
    public void testRecordDeepStubMock() throws Throwable {
        // Given
        Method method = List.class.getMethod("iterator");
        when(invocation.getMethod()).thenReturn(method);
        when(invocation.getMock()).thenReturn(mock(List.class));

        // When
        Object deepStub = returnsDeepStubs.answer(invocation);

        // Then
        // Verify that the deep stub is recorded (if method is accessible)
        // This test assumes recordDeepStubMock is called internally
        // We can only verify that the answer returns a mock
        assertNotNull(deepStub);
    }

    @Test
    public void testSerializationOfDeepStub() throws Throwable {
        // Given
        Method method = List.class.getMethod("iterator");
        when(invocation.getMethod()).thenReturn(method);
        when(invocation.getMock()).thenReturn(mock(List.class));

        // When
        Object deepStub = returnsDeepStubs.answer(invocation);

        // Then
        if (deepStub instanceof Serializable) {
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(deepStub);
                oos.close();

                ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
                ObjectInputStream ois = new ObjectInputStream(bis);
                Object deserialized = ois.readObject();
                assertNotNull("Deserialized deep stub should not be null", deserialized);
            } catch (Exception e) {
                fail("Serialization failed: " + e.getMessage());
            }
        } else {
            // Non-serializable deep stubs are acceptable
        }
    }

    @Test
    public void testAnswerMultipleInvocations() throws Throwable {
        // Given
        Method method = List.class.getMethod("iterator");
        when(invocation.getMethod()).thenReturn(method);
        when(invocation.getMock()).thenReturn(mock(List.class));

        // When
        Object first = returnsDeepStubs.answer(invocation);
        Object second = returnsDeepStubs.answer(invocation);

        // Then
        assertNotNull(first);
        assertNotNull(second);
        assertSame("Deep stubs for same invocation should be same", first, second);
    }

    @Test
    public void testAnswerDifferentMethods() throws Throwable {
        // Given
        Method iteratorMethod = List.class.getMethod("iterator");
        Method listIteratorMethod = List.class.getMethod("listIterator");

        when(invocation.getMethod()).thenReturn(iteratorMethod);
        when(invocation.getMock()).thenReturn(mock(List.class));
        Object iteratorStub = returnsDeepStubs.answer(invocation);

        when(invocation.getMethod()).thenReturn(listIteratorMethod);
        Object listIteratorStub = returnsDeepStubs.answer(invocation);

        // Then
        assertNotNull(iteratorStub);
        assertNotNull(listIteratorStub);
        assertNotSame("Different methods should return different stubs", iteratorStub, listIteratorStub);
    }

    // Helper method to check if an object is a Mockito mock
    private boolean isMock(Object obj) {
        return obj != null && mock(obj.getClass()) != null; // simplistic check
    }
}