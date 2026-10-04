package org.mockito.internal.invocation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matcher;
import org.mockito.internal.invocation.Invocation;
import org.mockito.internal.invocation.InvocationMatcher;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class InvocationMatcherTest {

    static class TestMethods {
        public void varargsMethod(String... args) {}
        public void normalMethod(String arg) {}
        public void multipleArgs(String a, Integer b) {}
    }

    private Method varargsMethod;
    private Method normalMethod;
    private Method multipleArgsMethod;

    @Before
    public void setUp() throws Exception {
        varargsMethod = TestMethods.class.getMethod("varargsMethod", String[].class);
        normalMethod = TestMethods.class.getMethod("normalMethod", String.class);
        multipleArgsMethod = TestMethods.class.getMethod("multipleArgs", String.class, Integer.class);
    }

    private Invocation createMockInvocation(Method method, Object[] args) {
        Invocation invocation = mock(Invocation.class);
        when(invocation.getMethod()).thenReturn(method);
        when(invocation.getArguments()).thenReturn(args);
        return invocation;
    }

    // Constructor tests
    @Test
    public void shouldCreateMatcherWithInvocation() {
        Invocation expectedInvocation = mock(Invocation.class);
        InvocationMatcher matcher = new InvocationMatcher(expectedInvocation);
        assertNotNull(matcher);
        assertSame(expectedInvocation, matcher.getInvocation());
    }

    @Test
    public void shouldCreateMatcherWithInvocationAndMatchers() {
        Invocation expectedInvocation = mock(Invocation.class);
        List<Matcher> matchers = new ArrayList<>();
        matchers.add(any());
        InvocationMatcher matcher = new InvocationMatcher(expectedInvocation, matchers);
        assertNotNull(matcher);
        assertSame(expectedInvocation, matcher.getInvocation());
        assertSame(matchers, matcher.getMatchers());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenInvocationIsNull() {
        new InvocationMatcher(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenInvocationIsNullWithMatchers() {
        new InvocationMatcher(null, new ArrayList<Matcher>());
    }

    // getInvocation tests
    @Test
    public void shouldReturnInvocation() {
        Invocation expectedInvocation = mock(Invocation.class);
        InvocationMatcher matcher = new InvocationMatcher(expectedInvocation);
        assertSame(expectedInvocation, matcher.getInvocation());
    }

    // getMatchers tests
    @Test
    public void shouldReturnMatchersWhenProvided() {
        Invocation expectedInvocation = mock(Invocation.class);
        List<Matcher> matchers = new ArrayList<>();
        matchers.add(any());
        InvocationMatcher matcher = new InvocationMatcher(expectedInvocation, matchers);
        assertSame(matchers, matcher.getMatchers());
    }

    @Test
    public void shouldReturnEmptyMatchersWhenNotProvided() {
        Invocation expectedInvocation = mock(Invocation.class);
        InvocationMatcher matcher = new InvocationMatcher(expectedInvocation);
        List<Matcher> matchers = matcher.getMatchers();
        assertNotNull(matchers);
        assertTrue(matchers.isEmpty());
    }

    // hasSameMethod tests
    @Test
    public void shouldHaveSameMethodWhenMethodsAreEqual() {
        Invocation expectedInvocation = createMockInvocation(normalMethod, new Object[]{"test"});
        Invocation actualInvocation = createMockInvocation(normalMethod, new Object[]{"test"});
        InvocationMatcher matcher = new InvocationMatcher(expectedInvocation);
        assertTrue(matcher.hasSameMethod(actualInvocation));
    }

    @Test
    public void shouldNotHaveSameMethodWhenMethodsAreDifferent() {
        Invocation expectedInvocation = createMockInvocation(normalMethod, new Object[]{"test"});
        Invocation actualInvocation = createMockInvocation(varargsMethod, new Object[]{"test"});
        InvocationMatcher matcher = new InvocationMatcher(expectedInvocation);
        assertFalse(matcher.hasSameMethod(actualInvocation));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenCandidateIsNullInHasSameMethod() {
        Invocation expectedInvocation = mock(Invocation.class);
        InvocationMatcher matcher = new InvocationMatcher(expectedInvocation);
        matcher.hasSameMethod(null);
    }

    @Test
    public void shouldHandleNullMethodInHasSameMethod() {
        Invocation expectedInvocation = mock(Invocation.class);
        when(expectedInvocation.getMethod()).thenReturn(null);
        Invocation actualInvocation = mock(Invocation.class);
        when(actualInvocation.getMethod()).thenReturn(null);
        InvocationMatcher matcher = new InvocationMatcher(expectedInvocation);
        // Should not throw
        matcher.hasSameMethod(actualInvocation);
    }

    // matches tests
    @Test
    public void shouldMatchWhenInvocationsAreEqual() {
        Invocation expectedInvocation = createMockInvocation(normalMethod, new Object[]{"test"});
        Invocation actualInvocation = createMockInvocation(normalMethod, new Object[]{"test"});
        InvocationMatcher matcher = new InvocationMatcher(expectedInvocation);
        assertTrue(matcher.matches(actualInvocation));
    }

    @Test
    public void shouldNotMatchWhenArgumentsDiffer() {
        Invocation expectedInvocation = createMockInvocation(normalMethod, new Object[]{"test"});
        Invocation actualInvocation = createMockInvocation(normalMethod, new Object[]{"other"});
        InvocationMatcher matcher = new InvocationMatcher(expectedInvocation);
        assertFalse(matcher.matches(actualInvocation));
    }

    @Test
    public void shouldNotMatchWhenMethodsAreDifferent() {
        Invocation expectedInvocation = createMockInvocation(normalMethod, new Object[]{"test"});
        Invocation actualInvocation = createMockInvocation(varargsMethod, new Object[]{"test"});
        InvocationMatcher matcher = new InvocationMatcher(expectedInvocation);
        assertFalse(matcher.matches(actualInvocation));
    }

    @Test
    public void shouldMatchVarargsWithMoreArguments() {
        Invocation expectedInvocation = createMockInvocation(varargsMethod, new Object[]{"a"});
        Invocation actualInvocation = createMockInvocation(varargsMethod, new Object[]{"a", "b"});
        InvocationMatcher matcher = new InvocationMatcher(expectedInvocation);
        assertTrue("Varargs should match with more arguments", matcher.matches(actualInvocation));
    }

    @Test
    public void shouldMatchVarargsWithFewerArguments() {
        Invocation expectedInvocation = createMockInvocation(varargsMethod, new Object[]{"a", "b"});
        Invocation actualInvocation = createMockInvocation(varargsMethod, new Object[]{"a"});
        InvocationMatcher matcher = new InvocationMatcher(expectedInvocation);
        assertTrue("Varargs should match with fewer arguments", matcher.matches(actualInvocation));
    }

    @Test
    public void shouldMatchWithEmptyArguments() {
        Invocation expectedInvocation = createMockInvocation(varargsMethod, new Object[]{});
        Invocation actualInvocation = createMockInvocation(varargsMethod, new Object[]{});
        InvocationMatcher matcher = new InvocationMatcher(expectedInvocation);
        assertTrue(matcher.matches(actualInvocation));
    }

    @Test
    public void shouldNotMatchWhenExpectedHasNoArgumentsAndActualHasSome() {
        Invocation expectedInvocation = createMockInvocation(varargsMethod, new Object[]{});
        Invocation actualInvocation = createMockInvocation(varargsMethod, new Object[]{"a"});
        InvocationMatcher matcher = new InvocationMatcher(expectedInvocation);
        assertFalse(matcher.matches(actualInvocation));
    }

    @Test
    public void shouldNotMatchWhenExpectedHasOneArgumentAndActualHasNone() {
        Invocation expectedInvocation = createMockInvocation(varargsMethod, new Object[]{"a"});
        Invocation actualInvocation = createMockInvocation(varargsMethod, new Object[]{});
        InvocationMatcher matcher = new InvocationMatcher(expectedInvocation);
        assertFalse(matcher.matches(actualInvocation));
    }

    @Test
    public void shouldHandleNullArguments() {
        Invocation expectedInvocation = createMockInvocation(normalMethod, new Object[]{null});
        Invocation actualInvocation = createMockInvocation(normalMethod, new Object[]{null});
        InvocationMatcher matcher = new InvocationMatcher(expectedInvocation);
        assertTrue(matcher.matches(actualInvocation));
    }

    @Test
    public void shouldNotMatchWhenOneArgumentIsNullAndOtherIsNotNull() {
        Invocation expectedInvocation = createMockInvocation(normalMethod, new Object[]{null});
        Invocation actualInvocation = createMockInvocation(normalMethod, new Object[]{"not null"});
        InvocationMatcher matcher = new InvocationMatcher(expectedInvocation);
        assertFalse(matcher.matches(actualInvocation));
    }

    @Test
    public void shouldMatchWithMultipleArguments() {
        Invocation expectedInvocation = createMockInvocation(multipleArgsMethod, new Object[]{"test", 123});
        Invocation actualInvocation = createMockInvocation(multipleArgsMethod, new Object[]{"test", 123});
        InvocationMatcher matcher = new InvocationMatcher(expectedInvocation);
        assertTrue(matcher.matches(actualInvocation));
    }

    @Test
    public void shouldNotMatchWithMultipleArgumentsMismatch() {
        Invocation expectedInvocation = createMockInvocation(multipleArgsMethod, new Object[]{"test", 123});
        Invocation actualInvocation = createMockInvocation(multipleArgsMethod, new Object[]{"test", 456});
        InvocationMatcher matcher = new InvocationMatcher(expectedInvocation);
        assertFalse(matcher.matches(actualInvocation));
    }

    @Test
    public void shouldMatchUsingMatchers() {
        Invocation expectedInvocation = createMockInvocation(normalMethod, new Object[]{"test"});
        Invocation actualInvocation = createMockInvocation(normalMethod, new Object[]{"test"});
        List<Matcher> matchers = new ArrayList<>();
        matchers.add(any());
        InvocationMatcher matcher = new InvocationMatcher(expectedInvocation, matchers);
        assertTrue(matcher.matches(actualInvocation));
    }

    @Test
    public void shouldNotMatchWhenMatchersFail() {
        Invocation expectedInvocation = createMockInvocation(normalMethod, new Object[]{"test"});
        Invocation actualInvocation = createMockInvocation(normalMethod, new Object[]{"other"});
        List<Matcher> matchers = new ArrayList<>();
        matchers.add(eq("test"));
        InvocationMatcher matcher = new InvocationMatcher(expectedInvocation, matchers);
        assertFalse(matcher.matches(actualInvocation));
    }

    @Test
    public void shouldMatchVarargsWithMatchers() {
        Invocation expectedInvocation = createMockInvocation(varargsMethod, new Object[]{"a", "b"});
        Invocation actualInvocation = createMockInvocation(varargsMethod, new Object[]{"a", "b"});
        List<Matcher> matchers = new ArrayList<>();
        matchers.add(any());
        matchers.add(any());
        InvocationMatcher matcher = new InvocationMatcher(expectedInvocation, matchers);
        assertTrue(matcher.matches(actualInvocation));
    }

    @Test
    public void shouldNotMatchVarargsWithMatchersWhenArgumentsDiffer() {
        Invocation expectedInvocation = createMockInvocation(varargsMethod, new Object[]{"a", "b"});
        Invocation actualInvocation = createMockInvocation(varargsMethod, new Object[]{"a", "c"});
        List<Matcher> matchers = new ArrayList<>();
        matchers.add(any());
        matchers.add(eq("b"));
        InvocationMatcher matcher = new InvocationMatcher(expectedInvocation, matchers);
        assertFalse(matcher.matches(actualInvocation));
    }

    @Test
    public void shouldMatchVarargsWithMatchersAndMoreArguments() {
        Invocation expectedInvocation = createMockInvocation(varargsMethod, new Object[]{"a"});
        Invocation actualInvocation = createMockInvocation(varargsMethod, new Object[]{"a", "b"});
        List<Matcher> matchers = new ArrayList<>();
        matchers.add(any());
        InvocationMatcher matcher = new InvocationMatcher(expectedInvocation, matchers);
        assertTrue("Varargs should match with more arguments even with matchers", matcher.matches(actualInvocation));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenActualInvocationIsNullInMatches() {
        Invocation expectedInvocation = mock(Invocation.class);
        InvocationMatcher matcher = new InvocationMatcher(expectedInvocation);
        matcher.matches(null);
    }

    @Test
    public void shouldHandleNullMethodInMatches() {
        Invocation expectedInvocation = mock(Invocation.class);
        when(expectedInvocation.getMethod()).thenReturn(null);
        when(expectedInvocation.getArguments()).thenReturn(new Object[]{});
        Invocation actualInvocation = mock(Invocation.class);
        when(actualInvocation.getMethod()).thenReturn(null);
        when(actualInvocation.getArguments()).thenReturn(new Object[]{});
        InvocationMatcher matcher = new InvocationMatcher(expectedInvocation);
        // Should not throw
        matcher.matches(actualInvocation);
    }

    // captureArgumentsFrom tests
    @Test
    public void shouldCaptureArgumentsFromActualInvocation() {
        Invocation expectedInvocation = createMockInvocation(normalMethod, new Object[]{null});
        Invocation actualInvocation = createMockInvocation(normalMethod, new Object[]{"captured"});
        InvocationMatcher matcher = new InvocationMatcher(expectedInvocation);
        matcher.captureArgumentsFrom(actualInvocation);
        assertArrayEquals(new Object[]{"captured"}, expectedInvocation.getArguments());
    }

    @Test
    public void shouldCaptureArgumentsFromActualInvocationWithVarargs() {
        Invocation expectedInvocation = createMockInvocation(varargsMethod, new Object[]{null, null});
        Invocation actualInvocation = createMockInvocation(varargsMethod, new Object[]{"a", "b"});
        InvocationMatcher matcher = new InvocationMatcher(expectedInvocation);
        matcher.captureArgumentsFrom(actualInvocation);
        assertArrayEquals(new Object[]{"a", "b"}, expectedInvocation.getArguments());
    }

    @Test
    public void shouldCaptureArgumentsUsingMatchers() {
        Invocation expectedInvocation = createMockInvocation(normalMethod, new Object[]{null});
        Invocation actualInvocation = createMockInvocation(normalMethod, new Object[]{"captured"});
        List<Matcher> matchers = new ArrayList<>();
        matchers.add(any());
        InvocationMatcher matcher = new InvocationMatcher(expectedInvocation, matchers);
        matcher.captureArgumentsFrom(actualInvocation);
        assertArrayEquals(new Object[]{"captured"}, expectedInvocation.getArguments());
    }

    @Test
    public void shouldCaptureVarargsArgumentsUsingMatchers() {
        Invocation expectedInvocation = createMockInvocation(varargsMethod, new Object[]{null, null});
        Invocation actualInvocation = createMockInvocation(varargsMethod, new Object[]{"a", "b"});
        List<Matcher> matchers = new ArrayList<>();
        matchers.add(any());
        matchers.add(any());
        InvocationMatcher matcher = new InvocationMatcher(expectedInvocation, matchers);
        matcher.captureArgumentsFrom(actualInvocation);
        assertArrayEquals(new Object[]{"a", "b"}, expectedInvocation.getArguments());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenCapturingFromNullInvocation() {
        Invocation expectedInvocation = mock(Invocation.class);
        InvocationMatcher matcher = new InvocationMatcher(expectedInvocation);
        matcher.captureArgumentsFrom(null);
    }
}