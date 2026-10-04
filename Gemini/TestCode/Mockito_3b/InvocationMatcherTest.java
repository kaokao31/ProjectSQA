package org.mockito.internal.matchers;

import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.invocation.Invocation;
import org.mockito.internal.invocation.InvocationBuilder;

import static org.junit.Assert.*;

public class InvocationMatcherTest {

    @Test
    public void testToStringMatches() {
        Invocation invocation = new InvocationBuilder().method("toSting").toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        
        assertNotNull(matcher.toString());
    }

    @Test
    public void testGetMethod() {
        Invocation invocation = new InvocationBuilder().method("someMethod").toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        
        assertNotNull(matcher.getMethod());
        assertEquals("someMethod", matcher.getMethod().getName());
    }

    @Test
    public void testGetInvocation() {
        Invocation invocation = new InvocationBuilder().method("someMethod").toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        
        assertSame(invocation, matcher.getInvocation());
    }

    @Test
    public void testMatches() {
        Invocation invocation = new InvocationBuilder().method("someMethod").args("arg1").toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        assertTrue(matcher.matches(invocation));
        
        Invocation differentInvocation = new InvocationBuilder().method("otherMethod").args("arg1").toInvocation();
        assertFalse(matcher.matches(differentInvocation));
        
        assertFalse(matcher.matches(null));
    }

    @Test
    public void testCapturesFrom() {
        Invocation invocation = new InvocationBuilder().method("someMethod").args("arg1").toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        
        // Should not throw exception
        matcher.captureFrom(invocation);
    }

    @Test
    public void testFromMethodList() {
        Invocation invocation = new InvocationBuilder().method("someMethod").toInvocation();
        java.util.List<Invocation> invocations = java.util.Collections.singletonList(invocation);
        
        java.util.List<InvocationMatcher> matchers = InvocationMatcher.createFrom(invocations);
        
        assertNotNull(matchers);
        assertEquals(1, matchers.size());
        assertEquals("someMethod", matchers.get(0).getMethod().getName());
    }

    @Test
    public void testMatchesSafeArgumentWithAny() {
        Invocation invocation = new InvocationBuilder().method("someMethod").args(new Object[]{null}).toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        
        assertTrue(matcher.matches(invocation));
    }
}