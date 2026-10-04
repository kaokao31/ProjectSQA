package org.mockito.internal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MockHandlerTest {

    @Mock
    private List<String> mockList;

    // Simple interface for testing
    interface Foo {
        String bar(String arg);
        int baz(int a, int b);
    }

    @Test
    public void testStubbing() {
        Foo mock = mock(Foo.class);
        when(mock.bar("hello")).thenReturn("world");
        assertEquals("world", mock.bar("hello"));
        assertNull(mock.bar("other"));
    }

    @Test
    public void testVerificationWithTimesOne() {
        Foo mock = mock(Foo.class);
        mock.bar("a");
        verify(mock, times(1)).bar("a");
    }

    @Test
    public void testVerificationWithTimesZeroWhenNotCalled() {
        Foo mock = mock(Foo.class);
        verify(mock, times(0)).bar("anything");
    }

    @Test
    public void testVerificationWithTimesZeroWhenCalledWithDifferentArgs() {
        // This is the core of Mockito bug 14: times(0) should pass when
        // the method was called with different arguments.
        Foo mock = mock(Foo.class);
        mock.bar("hello");
        // Should not throw – the bug would cause a false failure here.
        verify(mock, times(0)).bar("world");
    }

    @Test(expected = AssertionError.class)
    public void testVerificationWithTimesZeroWhenCalledWithSameArgs() {
        Foo mock = mock(Foo.class);
        mock.bar("hello");
        verify(mock, times(0)).bar("hello");
    }

    @Test
    public void testVerificationWithAtLeastOnce() {
        Foo mock = mock(Foo.class);
        mock.bar("x");
        mock.bar("x");
        verify(mock, atLeastOnce()).bar("x");
    }

    @Test
    public void testVerificationWithAtMost() {
        Foo mock = mock(Foo.class);
        mock.bar("y");
        verify(mock, atMost(1)).bar("y");
    }

    @Test
    public void testVerificationWithOnly() {
        Foo mock = mock(Foo.class);
        mock.bar("z");
        verify(mock, only()).bar("z");
    }

    @Test
    public void testVerificationWithTimesTwo() {
        Foo mock = mock(Foo.class);
        mock.bar("a");
        mock.bar("a");
        verify(mock, times(2)).bar("a");
    }

    @Test
    public void testArgumentMatchers() {
        Foo mock = mock(Foo.class);
        mock.bar("match");
        verify(mock).bar(anyString());
    }

    @Test
    public void testNullArgument() {
        Foo mock = mock(Foo.class);
        mock.bar(null);
        verify(mock).bar(isNull());
    }

    @Test
    public void testVarargs() {
        List<String> mock = mock(List.class);
        mock.addAll("a", "b", "c");
        verify(mock).addAll("a", "b", "c");
    }

    @Test(expected = RuntimeException.class)
    public void testExceptionThrowingStub() {
        Foo mock = mock(Foo.class);
        when(mock.bar("boom")).thenThrow(new RuntimeException("expected"));
        mock.bar("boom");
    }

    @Test
    public void testConsecutiveStubbing() {
        Foo mock = mock(Foo.class);
        when(mock.bar("first")).thenReturn("one").thenReturn("two");
        assertEquals("one", mock.bar("first"));
        assertEquals("two", mock.bar("first"));
        assertEquals("two", mock.bar("first")); // subsequent calls return last value
    }

    @Test
    public void testInOrderVerification() {
        Foo mock = mock(Foo.class);
        mock.bar("first");
        mock.bar("second");
        InOrder inOrder = inOrder(mock);
        inOrder.verify(mock).bar("first");
        inOrder.verify(mock).bar("second");
    }

    @Test
    public void testVerificationWithAtLeastZero() {
        Foo mock = mock(Foo.class);
        // atLeast(0) should always pass
        verify(mock, atLeast(0)).bar("never");
    }

    @Test
    public void testVerificationWithNever() {
        Foo mock = mock(Foo.class);
        verify(mock, never()).bar("never");
    }

    @Test
    public void testStubbingWithAnswer() {
        Foo mock = mock(Foo.class);
        when(mock.bar(anyString())).thenAnswer(invocation -> invocation.getArgument(0).toUpperCase());
        assertEquals("HELLO", mock.bar("hello"));
    }

    @Test
    public void testMockListAdd() {
        mockList.add("item");
        verify(mockList).add("item");
        assertEquals(0, mockList.size()); // default stub returns 0
    }

    @Test
    public void testMockListGet() {
        when(mockList.get(0)).thenReturn("first");
        assertEquals("first", mockList.get(0));
        assertNull(mockList.get(1));
    }

    @Test
    public void testVerificationWithTimeout() {
        Foo mock = mock(Foo.class);
        mock.bar("timeout");
        verify(mock, timeout(100)).bar("timeout");
    }

    @Test
    public void testVerificationWithDescription() {
        Foo mock = mock(Foo.class);
        mock.bar("desc");
        verify(mock, description("should be called once")).bar("desc");
    }
}