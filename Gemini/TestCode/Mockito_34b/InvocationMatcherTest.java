package org.mockito.internal.invocation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.Invocation;
import org.mockito.internal.matchers.CapturesArguments;
import org.hamcrest.Matcher;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class InvocationMatcherTest {

    private Method sampleMethod;
    private Method anotherMethod;
    private Invocation sampleInvocation;

    @Before
    public void setUp() throws Exception {
        sampleMethod = String.class.getMethod("substring", int.class);
        anotherMethod = String.class.getMethod("substring", int.class, int.class);
        
        Invocation realInvocation = mock(Invocation.class);
        when(realInvocation.getMethod()).thenReturn(sampleMethod);
        when(realInvocation.getArguments()).thenReturn(new Object[]{1});
        
        sampleInvocation = realInvocation;
    }

    @Test
    public void testConstructorAndGetters() {
        InvocationMatcher matcher = new InvocationMatcher(sampleInvocation, null);
        assertSame(sampleInvocation, matcher.getInvocation());
        assertEquals(sampleMethod, matcher.getMethod());
        assertNotNull(matcher.getMatchers());
        assertTrue(matcher.getMatchers().isEmpty());
    }

    @Test
    public void testCaptureArgumentsFrom() {
        CapturesArguments capturingMatcher = mock(CapturesArguments.class);
        List<Matcher> matchers = Collections.singletonList((Matcher) capturingMatcher);
        
        InvocationMatcher invocationMatcher = new InvocationMatcher(sampleInvocation, matchers);
        invocationMatcher.captureArgumentsFrom(sampleInvocation);

        verify(capturingMatcher).captureFrom(1);
    }

    @Test
    public void testCaptureArgumentsFromWithEmptyMatchers() {
        InvocationMatcher invocationMatcher = new InvocationMatcher(sampleInvocation, Collections.emptyList());
        // Should not throw any exception
        invocationMatcher.captureArgumentsFrom(sampleInvocation);
    }

    @Test
    public void testMatches() {
        InvocationMatcher matcher = new InvocationMatcher(sampleInvocation, Collections.emptyList());
        
        assertTrue(matcher.matches(sampleInvocation));

        Invocation differentMethodInvocation = mock(Invocation.class);
        when(differentMethodInvocation.getMethod()).thenReturn(anotherMethod);
        when(differentMethodInvocation.getArguments()).thenReturn(new Object[]{1, 2});

        assertFalse(matcher.matches(differentMethodInvocation));
        assertFalse(matcher.matches(null));
    }

    @Test
    public void testToString() {
        InvocationMatcher matcher = new InvocationMatcher(sampleInvocation, Collections.emptyList());
        assertNotNull(matcher.toString());
    }

    @Test
    public void testCreateFromMethodInvocations() {
        List<Invocation> invocations = Arrays.asList(sampleInvocation);
        List<InvocationMatcher> matchers = InvocationMatcher.createFrom(invocations);
        
        assertEquals(1, matchers.size());
        assertEquals(sampleMethod, matchers.get(0).getMethod());
    }
}