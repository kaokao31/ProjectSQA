package org.mockito.internal.invocation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.CapturesArguments;
import org.mockito.internal.matchers.Equals;
import org.mockito.invocation.Invocation;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.invocation.Location;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class InvocationMatcherTest {

    private Invocation invocation;
    private InvocationMatcher matcher;
    private Method method;
    private List<org.mockito.Matcher> matchers;

    @Before
    public void setUp() throws Exception {
        // Create a mock invocation with a simple method
        invocation = mock(Invocation.class);
        method = Object.class.getMethod("equals", Object.class);
        when(invocation.getMethod()).thenReturn(method);
        when(invocation.getArguments()).thenReturn(new Object[]{"test"});
        when(invocation.getRawArguments()).thenReturn(new Object[]{"test"});
        when(invocation.getMock()).thenReturn("mock");
        when(invocation.getLocation()).thenReturn(mock(Location.class));
        when(invocation.isVerified()).thenReturn(false);
        when(invocation.isIgnoredForVerification()).thenReturn(false);
        when(invocation.getSequenceNumber()).thenReturn(1);

        // Create matchers list
        matchers = new ArrayList<org.mockito.Matcher>();
        matchers.add(new Equals("test"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForNullInvocation() {
        new InvocationMatcher(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForNullMatchers() {
        new InvocationMatcher(invocation, null);
    }

    @Test
    public void shouldReturnSameInvocation() {
        matcher = new InvocationMatcher(invocation);
        assertSame(invocation, matcher.getInvocation());
    }

    @Test
    public void shouldReturnMatchersFromConstructor() {
        matcher = new InvocationMatcher(invocation, matchers);
        assertEquals(matchers, matcher.getMatchers());
    }

    @Test
    public void shouldCreateMatchersFromInvocationArguments() {
        matcher = new InvocationMatcher(invocation);
        List<org.mockito.Matcher> generatedMatchers = matcher.getMatchers();
        assertNotNull(generatedMatchers);
        assertEquals(1, generatedMatchers.size());
        assertTrue(generatedMatchers.get(0) instanceof Equals);
    }

    @Test
    public void shouldMatchWhenInvocationMatches() {
        matcher = new InvocationMatcher(invocation, matchers);
        Invocation matchingInvocation = mock(Invocation.class);
        when(matchingInvocation.getMethod()).thenReturn(method);
        when(matchingInvocation.getArguments()).thenReturn(new Object[]{"test"});
        when(matchingInvocation.getRawArguments()).thenReturn(new Object[]{"test"});

        assertTrue(matcher.matches(matchingInvocation));
    }

    @Test
    public void shouldNotMatchWhenMethodDiffers() throws Exception {
        matcher = new InvocationMatcher(invocation, matchers);
        Method differentMethod = Object.class.getMethod("hashCode");
        Invocation differentInvocation = mock(Invocation.class);
        when(differentInvocation.getMethod()).thenReturn(differentMethod);
        when(differentInvocation.getArguments()).thenReturn(new Object[]{});

        assertFalse(matcher.matches(differentInvocation));
    }

    @Test
    public void shouldNotMatchWhenArgumentsDiffer() {
        matcher = new InvocationMatcher(invocation, matchers);
        Invocation differentArgsInvocation = mock(Invocation.class);
        when(differentArgsInvocation.getMethod()).thenReturn(method);
        when(differentArgsInvocation.getArguments()).thenReturn(new Object[]{"other"});
        when(differentArgsInvocation.getRawArguments()).thenReturn(new Object[]{"other"});

        assertFalse(matcher.matches(differentArgsInvocation));
    }

    @Test
    public void shouldMatchWhenNoMatchersAndNoArguments() throws Exception {
        Method noArgMethod = Object.class.getMethod("toString");
        Invocation noArgInvocation = mock(Invocation.class);
        when(noArgInvocation.getMethod()).thenReturn(noArgMethod);
        when(noArgInvocation.getArguments()).thenReturn(new Object[]{});
        when(noArgInvocation.getRawArguments()).thenReturn(new Object[]{});

        matcher = new InvocationMatcher(noArgInvocation);
        assertTrue(matcher.matches(noArgInvocation));
    }

    @Test
    public void shouldHaveSameMethodWhenMethodsEqual() throws Exception {
        matcher = new InvocationMatcher(invocation, matchers);
        Invocation sameMethodInvocation = mock(Invocation.class);
        when(sameMethodInvocation.getMethod()).thenReturn(method);

        assertTrue(matcher.hasSameMethod(sameMethodInvocation));
    }

    @Test
    public void shouldNotHaveSameMethodWhenMethodsDiffer() throws Exception {
        matcher = new InvocationMatcher(invocation, matchers);
        Method differentMethod = Object.class.getMethod("hashCode");
        Invocation differentMethodInvocation = mock(Invocation.class);
        when(differentMethodInvocation.getMethod()).thenReturn(differentMethod);

        assertFalse(matcher.hasSameMethod(differentMethodInvocation));
    }

    @Test
    public void shouldCaptureArgumentsFromInvocationWithCapturesArgumentsMatcher() {
        // Create a matcher that implements CapturesArguments
        org.mockito.Matcher capturingMatcher = mock(org.mockito.Matcher.class, withSettings().extraInterfaces(CapturesArguments.class));
        when(((CapturesArguments) capturingMatcher).captureFrom(any())).thenReturn(true);
        List<org.mockito.Matcher> matchersWithCapture = new ArrayList<org.mockito.Matcher>();
        matchersWithCapture.add(capturingMatcher);

        matcher = new InvocationMatcher(invocation, matchersWithCapture);
        Invocation invocationWithArgs = mock(Invocation.class);
        when(invocationWithArgs.getMethod()).thenReturn(method);
        when(invocationWithArgs.getArguments()).thenReturn(new Object[]{"captured"});
        when(invocationWithArgs.getRawArguments()).thenReturn(new Object[]{"captured"});

        matcher.captureArgumentsFrom(invocationWithArgs);

        verify((CapturesArguments) capturingMatcher).captureFrom("captured");
    }

    @Test
    public void shouldNotCaptureArgumentsWhenMatcherDoesNotImplementCapturesArguments() {
        org.mockito.Matcher nonCapturingMatcher = mock(org.mockito.Matcher.class);
        List<org.mockito.Matcher> matchersWithoutCapture = new ArrayList<org.mockito.Matcher>();
        matchersWithoutCapture.add(nonCapturingMatcher);

        matcher = new InvocationMatcher(invocation, matchersWithoutCapture);
        Invocation invocationWithArgs = mock(Invocation.class);
        when(invocationWithArgs.getMethod()).thenReturn(method);
        when(invocationWithArgs.getArguments()).thenReturn(new Object[]{"test"});
        when(invocationWithArgs.getRawArguments()).thenReturn(new Object[]{"test"});

        matcher.captureArgumentsFrom(invocationWithArgs);
        // No interaction with captureFrom, so just verify no exception
    }

    @Test
    public void shouldHandleVarargsWithCapturesArgumentsMatcher() throws Exception {
        // Simulate a varargs method: String.format(String, Object...)
        Method varargsMethod = String.class.getMethod("format", String.class, Object[].class);
        Invocation varargsInvocation = mock(Invocation.class);
        when(varargsInvocation.getMethod()).thenReturn(varargsMethod);
        when(varargsInvocation.getArguments()).thenReturn(new Object[]{"%s %d", new Object[]{"test", 42}});
        when(varargsInvocation.getRawArguments()).thenReturn(new Object[]{"%s %d", new Object[]{"test", 42}});

        // Create matchers: one for format string, one for varargs (CapturesArguments)
        org.mockito.Matcher formatMatcher = new Equals("%s %d");
        org.mockito.Matcher varargsMatcher = mock(org.mockito.Matcher.class, withSettings().extraInterfaces(CapturesArguments.class));
        when(((CapturesArguments) varargsMatcher).captureFrom(any())).thenReturn(true);

        List<org.mockito.Matcher> matchersList = Arrays.asList(formatMatcher, varargsMatcher);
        matcher = new InvocationMatcher(varargsInvocation, matchersList);

        // Create a matching invocation with different varargs
        Invocation targetInvocation = mock(Invocation.class);
        when(targetInvocation.getMethod()).thenReturn(varargsMethod);
        when(targetInvocation.getArguments()).thenReturn(new Object[]{"%s %d", new Object[]{"hello", 7}});
        when(targetInvocation.getRawArguments()).thenReturn(new Object[]{"%s %d", new Object[]{"hello", 7}});

        matcher.captureArgumentsFrom(targetInvocation);

        // The varargs matcher should capture the entire varargs array
        verify((CapturesArguments) varargsMatcher).captureFrom(new Object[]{"hello", 7});
    }

    @Test
    public void shouldHandleEmptyMatchersList() {
        matcher = new InvocationMatcher(invocation, Collections.<org.mockito.Matcher>emptyList());
        assertTrue(matcher.getMatchers().isEmpty());
    }

    @Test
    public void shouldReturnToString() {
        matcher = new InvocationMatcher(invocation, matchers);
        assertNotNull(matcher.toString());
        assertTrue(matcher.toString().contains("equals"));
    }

    @Test
    public void shouldNotMatchWhenInvocationIsNull() {
        matcher = new InvocationMatcher(invocation, matchers);
        assertFalse(matcher.matches(null));
    }

    @Test
    public void shouldNotHaveSameMethodWhenInvocationIsNull() {
        matcher = new InvocationMatcher(invocation, matchers);
        assertFalse(matcher.hasSameMethod(null));
    }

    @Test
    public void shouldCaptureArgumentsFromInvocationWithMultipleMatchers() {
        org.mockito.Matcher firstMatcher = mock(org.mockito.Matcher.class, withSettings().extraInterfaces(CapturesArguments.class));
        org.mockito.Matcher secondMatcher = mock(org.mockito.Matcher.class, withSettings().extraInterfaces(CapturesArguments.class));
        when(((CapturesArguments) firstMatcher).captureFrom(any())).thenReturn(true);
        when(((CapturesArguments) secondMatcher).captureFrom(any())).thenReturn(true);

        List<org.mockito.Matcher> multiMatchers = Arrays.asList(firstMatcher, secondMatcher);
        matcher = new InvocationMatcher(invocation, multiMatchers);

        Invocation multiArgInvocation = mock(Invocation.class);
        when(multiArgInvocation.getMethod()).thenReturn(method);
        when(multiArgInvocation.getArguments()).thenReturn(new Object[]{"arg1", "arg2"});
        when(multiArgInvocation.getRawArguments()).thenReturn(new Object[]{"arg1", "arg2"});

        matcher.captureArgumentsFrom(multiArgInvocation);

        verify((CapturesArguments) firstMatcher).captureFrom("arg1");
        verify((CapturesArguments) secondMatcher).captureFrom("arg2");
    }

    @Test
    public void shouldNotFailWhenMatchersCountDoesNotMatchArgumentsCount() {
        // Matchers list has more matchers than arguments
        List<org.mockito.Matcher> tooManyMatchers = Arrays.asList(new Equals("a"), new Equals("b"), new Equals("c"));
        matcher = new InvocationMatcher(invocation, tooManyMatchers);

        Invocation singleArgInvocation = mock(Invocation.class);
        when(singleArgInvocation.getMethod()).thenReturn(method);
        when(singleArgInvocation.getArguments()).thenReturn(new Object[]{"test"});
        when(singleArgInvocation.getRawArguments()).thenReturn(new Object[]{"test"});

        // Should not throw exception, just capture what it can
        matcher.captureArgumentsFrom(singleArgInvocation);
    }
}