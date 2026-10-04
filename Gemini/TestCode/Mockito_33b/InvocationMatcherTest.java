package org.mockito.internal.invocation;

import org.junit.Test;
import org.mockito.invocation.Invocation;
import org.mockito.internal.matchers.CapturesArguments;
import org.hamcrest.Matcher;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class InvocationMatcherTest {

    // Dummy method used for creating Invocations via reflection
    public void sampleMethod(Object arg1, String arg2) {}
    public void sampleMethodWithArray(Object... args) {}

    private Invocation createDummyInvocation(Method method, Object... args) {
        return new Invocation(
            mock(Object.class),
            method,
            args,
            0,
            null
        );
    }

    @Test
    public void testConstructorAndGetters() throws Exception {
        Method method = InvocationMatcherTest.class.getMethod("sampleMethod", Object.class, String.class);
        Invocation invocation = createDummyInvocation(method, "test", "hello");
        
        InvocationMatcher matcher = new InvocationMatcher(invocation, null);
        
        assertSame(invocation, matcher.getInvocation());
        assertEquals(method, matcher.getMethod());
        assertNotNull(matcher.getMatchers());
    }

    @Test
    public void testMethodEquals() throws Exception {
        Method method1 = InvocationMatcherTest.class.getMethod("sampleMethod", Object.class, String.class);
        Method method2 = InvocationMatcherTest.class.getMethod("sampleMethodWithArray", Object[].class);

        Invocation inv1 = createDummyInvocation(method1, "a", "b");
        Invocation inv2 = createDummyInvocation(method1, "c", "d");
        Invocation inv3 = createDummyInvocation(method2, (Object) new Object[]{"a"});

        InvocationMatcher matcher1 = new InvocationMatcher(inv1);

        assertTrue(matcher1.matches(inv1));
        assertTrue(matcher1.matches(inv2)); // Matches same method signature
        assertFalse(matcher1.matches(inv3)); // Different method
    }

    @Test
    public void testMatchesWithNullInvocation() throws Exception {
        Method method = InvocationMatcherTest.class.getMethod("sampleMethod", Object.class, String.class);
        Invocation inv = createDummyInvocation(method, "a", "b");
        InvocationMatcher matcher = new InvocationMatcher(inv);

        assertFalse(matcher.matches(null));
    }

    @Test
    public void testCaptureArguments() throws Exception {
        Method method = InvocationMatcherTest.class.getMethod("sampleMethod", Object.class, String.class);
        Invocation inv = createDummyInvocation(method, "argVal", "strVal");
        
        CapturesArguments mockCaptor = mock(CapturesArguments.class);
        List<Matcher> matchers = Collections.<Matcher>singletonList((Matcher) mockCaptor);
        
        InvocationMatcher matcher = new InvocationMatcher(inv, matchers);
        matcher.captureArgumentsFrom(inv);
        
        assertNotNull(matcher.getMatchers());
    }

    @Test
    public void testToString() throws Exception {
        Method method = InvocationMatcherTest.class.getMethod("sampleMethod", Object.class, String.class);
        Invocation inv = createDummyInvocation(method, "a", "b");
        InvocationMatcher matcher = new InvocationMatcher(inv);

        assertNotNull(matcher.toString());
    }

    @Test
    public void testCreateFromList() throws Exception {
        Method method = InvocationMatcherTest.class.getMethod("sampleMethod", Object.class, String.class);
        Invocation inv = createDummyInvocation(method, "a", "b");
        List<Invocation> invocations = Collections.singletonList(inv);

        List<InvocationMatcher> matchers = InvocationMatcher.createFrom(invocations);
        assertEquals(1, matchers.size());
        assertEquals(method, matchers.get(0).getMethod());
    }
}