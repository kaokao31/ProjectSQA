package org.mockito.internal;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.exceptions.misusing.MissingMethodInvocationException;
import org.mockito.exceptions.misusing.UnfinishedStubbingException;
import org.mockito.invocation.Invocation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit 4 test suite for MockitoCore.
 * Designed to achieve high line/branch coverage and detect faults (e.g., Defects4J bug 16).
 */
public class MockitoCoreTest {

    @Mock
    private TestInterface mock;

    private MockitoCore mockitoCore;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockitoCore = new MockitoCore();
    }

    // ==================== Public API Tests ====================

    @Test
    public void testWhenWithObjectReturn() {
        when(mock.getString()).thenReturn("hello");
        assertEquals("hello", mock.getString());
    }

    @Test
    public void testWhenWithPrimitiveIntReturn() {
        when(mock.getInt()).thenReturn(42);
        assertEquals(42, mock.getInt());
    }

    @Test
    public void testWhenWithPrimitiveBooleanReturn() {
        when(mock.getBoolean()).thenReturn(true);
        assertTrue(mock.getBoolean());
    }

    @Test(expected = MissingMethodInvocationException.class)
    public void testWhenWithNullArgument() {
        when(null).thenReturn("x");
    }

    @Test
    public void testVerifySimple() {
        mock.getString();
        verify(mock).getString();
    }

    @Test
    public void testVerifyWithTimes() {
        mock.getString();
        mock.getString();
        verify(mock, times(2)).getString();
    }

    @Test
    public void testVerifyWithAtLeast() {
        mock.getString();
        mock.getString();
        verify(mock, atLeast(1)).getString();
    }

    @Test
    public void testVerifyWithAtMost() {
        mock.getString();
        mock.getString();
        verify(mock, atMost(2)).getString();
    }

    @Test
    public void testVerifyWithNever() {
        verify(mock, never()).getString();
    }

    @Test
    public void testVerifyWithOnly() {
        mock.getString();
        verify(mock, only()).getString();
    }

    @Test
    public void testVerifyZeroInteractions() {
        verifyZeroInteractions(mock);
    }

    @Test
    public void testVerifyNoMoreInteractions() {
        mock.getString();
        verify(mock).getString();
        verifyNoMoreInteractions(mock);
    }

    @Test
    public void testValidateMockitoUsageAfterProperUsage() {
        when(mock.getString()).thenReturn("a");
        mock.getString();
        validateMockitoUsage(); // should not throw
    }

    @Test(expected = UnfinishedStubbingException.class)
    public void testValidateMockitoUsageAfterMisuse() {
        when(mock.getString()); // incomplete stubbing
        validateMockitoUsage();
    }

    @Test
    public void testStubMethod() {
        stub(mock.getString()).toReturn("stub");
        assertEquals("stub", mock.getString());
    }

    @Test
    public void testWhenWithAnyMatchers() {
        when(mock.getString()).thenReturn("any");
        assertEquals("any", mock.getString());
    }

    @Test
    public void testThenAnswer() {
        when(mock.getString()).thenAnswer(invocation -> "answer");
        assertEquals("answer", mock.getString());
    }

    @Test
    public void testThenThrow() {
        when(mock.getString()).thenThrow(new RuntimeException("test"));
        try {
            mock.getString();
            fail("Expected exception");
        } catch (RuntimeException e) {
            assertEquals("test", e.getMessage());
        }
    }

    // ==================== Direct MockitoCore Method Tests ====================

    @Test
    public void testSetAndClearLastInvocation() {
        Invocation invocation = mock(Invocation.class);
        mockitoCore.setLastInvocation(invocation);
        assertSame(invocation, mockitoCore.getLastInvocation());
        mockitoCore.clearLastInvocation();
        assertNull(mockitoCore.getLastInvocation());
    }

    @Test
    public void testGetLastInvocationWhenNone() {
        assertNull(mockitoCore.getLastInvocation());
    }

    @Test
    public void testValidateMockitoUsageDirect() {
        when(mock.getString()).thenReturn("a");
        mock.getString();
        mockitoCore.validateMockitoUsage(); // should pass
    }

    @Test(expected = UnfinishedStubbingException.class)
    public void testValidateMockitoUsageDirectWithMisuse() {
        when(mock.getString()); // incomplete stubbing
        mockitoCore.validateMockitoUsage();
    }

    // ==================== Edge Cases and Bug Triggers ====================

    @Test
    public void testWhenWithPrimitiveIntReturnAndNullThenReturn() {
        // This should throw an exception because null cannot be returned for primitive
        try {
            when(mock.getInt()).thenReturn(null);
            fail("Expected exception for null primitive return");
        } catch (Exception e) {
            // Expected: either IllegalArgumentException or similar
        }
    }

    @Test
    public void testWhenWithPrimitiveBooleanReturnAndNullThenReturn() {
        try {
            when(mock.getBoolean()).thenReturn(null);
            fail("Expected exception for null primitive return");
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testVerifyWithCustomVerificationMode() {
        mock.getString();
        verify(mock, description("custom")).getString();
    }

    @Test
    public void testStubWithNullInvocation() {
        try {
            stub(null);
            fail("Expected MissingMethodInvocationException");
        } catch (MissingMethodInvocationException e) {
            // Expected
        }
    }

    // ==================== Helper Interface ====================

    private interface TestInterface {
        String getString();
        int getInt();
        boolean getBoolean();
        void doSomething();
    }
}