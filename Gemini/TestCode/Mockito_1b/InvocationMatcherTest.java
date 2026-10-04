package org.mockito.internal.invocation;

import org.hamcrest.Matcher;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.ArrayEquals;
import org.mockito.internal.matchers.CapturesArguments;
import org.mockito.internal.matchers.Equals;
import org.mockito.internal.reporting.PrintSettings;
import org.mockito.invocation.Invocation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class InvocationMatcherTest {

    private Invocation invocation;
    private Method testMethod;
    private Method otherMethod;
    private Object mockObject;
    private Object otherMockObject;

    @Before
    public void setUp() throws Exception {
        mockObject = new Object();
        otherMockObject = new Object();
        testMethod = String.class.getMethod("substring", int.class);
        otherMethod = String.class.getMethod("substring", int.class, int.class);

        invocation = mock(Invocation.class);
        when(invocation.getMethod()).thenReturn(testMethod);
        when(invocation.getMock()).thenReturn(mockObject);
        when(invocation.getRawArguments()).thenReturn(new Object[]{1});
        when(invocation.getArguments()).thenReturn(new Object[]{1});
    }

    @Test
    public void shouldConstructWithInvocationOnly() {
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        assertEquals(invocation, matcher.getInvocation());
        assertEquals(testMethod, matcher.getMethod());
        assertEquals(1, matcher.getMatchers().size());
        assertTrue(matcher.getMatchers().get(0) instanceof Equals);
    }

    @Test
    public void shouldConstructWithExplicitMatchers() {
        Matcher<?> customMatcher = IsNull.nullValue();
        InvocationMatcher matcher = new InvocationMatcher(invocation, Collections.<Matcher>singletonList(customMatcher));

        assertEquals(invocation, matcher.getInvocation());
        assertEquals(testMethod, matcher.getMethod());
        assertEquals(1, matcher.getMatchers().size());
        assertSame(customMatcher, matcher.getMatchers().get(0));
    }

    @Test
    public void shouldMatchIdenticalInvocation() {
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        Invocation candidate = mock(Invocation.class);
        when(candidate.getMock()).thenReturn(mockObject);
        when(candidate.getMethod()).thenReturn(testMethod);
        when(candidate.getRawArguments()).thenReturn(new Object[]{1});
        when(candidate.getArguments()).thenReturn(new Object[]{1});

        assertTrue(matcher.matches(candidate));
    }

    @Test
    public void shouldNotMatchWhenMockIsDifferent() {
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        Invocation candidate = mock(Invocation.class);
        when(candidate.getMock()).thenReturn(otherMockObject);
        when(candidate.getMethod()).thenReturn(testMethod);
        when(candidate.getRawArguments()).thenReturn(new Object[]{1});
        when(candidate.getArguments()).thenReturn(new Object[]{1});

        assertFalse(matcher.matches(candidate));
    }

    @Test
    public void shouldNotMatchWhenMethodIsDifferent() {
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        Invocation candidate = mock(Invocation.class);
        when(candidate.getMock()).thenReturn(mockObject);
        when(candidate.getMethod()).thenReturn(otherMethod);
        when(candidate.getRawArguments()).thenReturn(new Object[]{1, 2});
        when(candidate.getArguments()).thenReturn(new Object[]{1, 2});

        assertFalse(matcher.matches(candidate));
    }

    @Test
    public void shouldNotMatchWhenArgumentsDoNotMatch() {
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        Invocation candidate = mock(Invocation.class);
        when(candidate.getMock()).thenReturn(mockObject);
        when(candidate.getMethod()).thenReturn(testMethod);
        when(candidate.getRawArguments()).thenReturn(new Object[]{2});
        when(candidate.getArguments()).thenReturn(new Object[]{2});

        assertFalse(matcher.matches(candidate));
    }

    @Test
    public void shouldNotMatchWhenArgumentLengthDiffers() {
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        Invocation candidate = mock(Invocation.class);
        when(candidate.getMock()).thenReturn(mockObject);
        when(candidate.getMethod()).thenReturn(testMethod);
        when(candidate.getRawArguments()).thenReturn(new Object[]{1, 2});
        when(candidate.getArguments()).thenReturn(new Object[]{1, 2});

        assertFalse(matcher.matches(candidate));
    }

    @Test
    public void shouldMatchWithHamcrestMatcher() {
        Matcher<Object> equalMatcher = IsEqual.equalTo((Object) 1);
        InvocationMatcher matcher = new InvocationMatcher(invocation, Collections.<Matcher>singletonList(equalMatcher));

        Invocation candidate = mock(Invocation.class);
        when(candidate.getMock()).thenReturn(mockObject);
        when(candidate.getMethod()).thenReturn(testMethod);
        when(candidate.getRawArguments()).thenReturn(new Object[]{1});
        when(candidate.getArguments()).thenReturn(new Object[]{1});

        assertTrue(matcher.matches(candidate));
    }

    @Test
    public void shouldMatchWithArrayEqualsMatcher() {
        Object[] arrayArg = new Object[]{"a", "b"};
        when(invocation.getRawArguments()).thenReturn(new Object[]{arrayArg});
        when(invocation.getArguments()).thenReturn(new Object[]{arrayArg});

        Matcher<Object> arrayMatcher = new ArrayEquals(arrayArg);
        InvocationMatcher matcher = new InvocationMatcher(invocation, Collections.<Matcher>singletonList(arrayMatcher));

        Invocation candidate = mock(Invocation.class);
        when(candidate.getMock()).thenReturn(mockObject);
        when(candidate.getMethod()).thenReturn(testMethod);
        when(candidate.getRawArguments()).thenReturn(new Object[]{new Object[]{"a", "b"}});
        when(candidate.getArguments()).thenReturn(new Object[]{new Object[]{"a", "b"}});

        assertTrue(matcher.matches(candidate));
    }

    @Test
    public void shouldReturnHasSameMethodTrueForIdenticalMethod() {
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        Invocation candidate = mock(Invocation.class);
        when(candidate.getMethod()).thenReturn(testMethod);

        assertTrue(matcher.hasSameMethod(candidate));
    }

    @Test
    public void shouldReturnHasSameMethodFalseForDifferentMethod() {
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        Invocation candidate = mock(Invocation.class);
        when(candidate.getMethod()).thenReturn(otherMethod);

        assertFalse(matcher.hasSameMethod(candidate));
    }

    @Test
    public void shouldReturnHasSimilarMethodTrueForOverloadedMethodOnSameMock() {
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        Invocation candidate = mock(Invocation.class);
        when(candidate.getMock()).thenReturn(mockObject);
        when(candidate.getMethod()).thenReturn(otherMethod);
        when(candidate.isVerified()).thenReturn(false);

        assertTrue(matcher.hasSimilarMethod(candidate));
    }

    @Test
    public void shouldReturnHasSimilarMethodFalseWhenMockIsDifferent() {
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        Invocation candidate = mock(Invocation.class);
        when(candidate.getMock()).thenReturn(otherMockObject);
        when(candidate.getMethod()).thenReturn(otherMethod);
        when(candidate.isVerified()).thenReturn(false);

        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    @Test
    public void shouldReturnHasSimilarMethodFalseWhenMethodNameIsDifferent() throws Exception {
        Method differentNameMethod = String.class.getMethod("length");
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        Invocation candidate = mock(Invocation.class);
        when(candidate.getMock()).thenReturn(mockObject);
        when(candidate.getMethod()).thenReturn(differentNameMethod);
        when(candidate.isVerified()).thenReturn(false);

        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    @Test
    public void shouldReturnHasSimilarMethodFalseWhenCandidateIsVerified() {
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        Invocation candidate = mock(Invocation.class);
        when(candidate.getMock()).thenReturn(mockObject);
        when(candidate.getMethod()).thenReturn(testMethod);
        when(candidate.isVerified()).thenReturn(true);

        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    @Test
    public void shouldCaptureArgumentsFromInvocation() {
        CapturesArguments capturesMatcher = mock(CapturesArguments.class);
        InvocationMatcher matcher = new InvocationMatcher(invocation, Arrays.<Matcher>asList((Matcher) capturesMatcher));

        Invocation candidate = mock(Invocation.class);
        when(candidate.getRawArguments()).thenReturn(new Object[]{"capturedValue"});
        when(candidate.getArguments()).thenReturn(new Object[]{"capturedValue"});

        matcher.captureArgumentsFrom(candidate);

        verify(capturesMatcher, times(1)).captureFrom("capturedValue");
    }

    @Test
    public void shouldCaptureVarargsCorrectlyWhenMatchersMatchVarArgs() {
        CapturesArguments capturesMatcher1 = mock(CapturesArguments.class);
        CapturesArguments capturesMatcher2 = mock(CapturesArguments.class);
        InvocationMatcher matcher = new InvocationMatcher(invocation, Arrays.<Matcher>asList((Matcher) capturesMatcher1, (Matcher) capturesMatcher2));

        Invocation candidate = mock(Invocation.class);
        when(candidate.getRawArguments()).thenReturn(new Object[]{"arg1", "arg2"});
        when(candidate.getArguments()).thenReturn(new Object[]{"arg1", "arg2"});

        matcher.captureArgumentsFrom(candidate);

        verify(capturesMatcher1).captureFrom("arg1");
        verify(capturesMatcher2).captureFrom("arg2");
    }

    @Test
    public void shouldCreateFromListOfInvocations() {
        Invocation inv1 = mock(Invocation.class);
        Invocation inv2 = mock(Invocation.class);
        when(inv1.getRawArguments()).thenReturn(new Object[]{1});
        when(inv2.getRawArguments()).thenReturn(new Object[]{2});

        List<InvocationMatcher> matchers = InvocationMatcher.createFrom(Arrays.asList(inv1, inv2));

        assertEquals(2, matchers.size());
        assertEquals(inv1, matchers.get(0).getInvocation());
        assertEquals(inv2, matchers.get(1).getInvocation());
    }

    @Test
    public void shouldCreateFromEmptyList() {
        List<InvocationMatcher> matchers = InvocationMatcher.createFrom(Collections.<Invocation>emptyList());
        assertNotNull(matchers);
        assertTrue(matchers.isEmpty());
    }

    @Test
    public void shouldDelegateGetLocationToInvocation() {
        org.mockito.invocation.Location location = mock(org.mockito.invocation.Location.class);
        when(invocation.getLocation()).thenReturn(location);

        InvocationMatcher matcher = new InvocationMatcher(invocation);

        assertSame(location, matcher.getLocation());
    }

    @Test
    public void shouldDelegateToStringToInvocationWhenNoSettings() {
        when(invocation.toString(any(List.class), any(PrintSettings.class))).thenReturn("mockedToString");

        InvocationMatcher matcher = new InvocationMatcher(invocation);
        String result = matcher.toString(new PrintSettings());

        assertNotNull(result);
        assertEquals("mockedToString", result);
    }

    @Test
    public void shouldFallbackToStringWithDefaultPrintSettings() {
        when(invocation.toString(any(List.class), any(PrintSettings.class))).thenReturn("mockedToStringDefault");

        InvocationMatcher matcher = new InvocationMatcher(invocation);
        String result = matcher.toString();

        assertEquals("mockedToStringDefault", result);
    }
}