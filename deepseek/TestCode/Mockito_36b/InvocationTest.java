package org.mockito.internal.invocation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.invocation.InvocationMatcher;
import org.mockito.invocation.Invocation;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for Invocation class, targeting maximum coverage and fault detection.
 * Designed to reveal bugs like Mockito #36 (varargs handling).
 */
public class InvocationTest {

    private List<String> mockList;
    private Invocation invocationAdd;
    private Invocation invocationVarargs;
    private Method methodAdd;
    private Method methodVarargs;

    // Sample interface with varargs method for testing
    interface VarargsInterface {
        String varargsMethod(String... args);
    }

    @Before
    public void setUp() throws Exception {
        mockList = mock(List.class);
        // Capture invocation from a simple add call
        mockList.add("test");
        List<Invocation> invocations = mockingDetails(mockList).getInvocations();
        invocationAdd = invocations.get(0);
        methodAdd = List.class.getMethod("add", Object.class);

        // Create mock for varargs interface
        VarargsInterface varargsMock = mock(VarargsInterface.class);
        varargsMock.varargsMethod("a", "b", "c");
        invocations = mockingDetails(varargsMock).getInvocations();
        invocationVarargs = invocations.get(0);
        methodVarargs = VarargsInterface.class.getMethod("varargsMethod", String[].class);
    }

    @Test
    public void testGetMock() {
        assertNotNull(invocationAdd.getMock());
        assertSame(mockList, invocationAdd.getMock());
    }

    @Test
    public void testGetMethod() {
        assertEquals(methodAdd, invocationAdd.getMethod());
        assertEquals(methodVarargs, invocationVarargs.getMethod());
    }

    @Test
    public void testGetArguments() {
        // Basic argument retrieval
        Object[] args = invocationAdd.getArguments();
        assertNotNull(args);
        assertEquals(1, args.length);
        assertEquals("test", args[0]);

        // Varargs expansion: getArguments should expand varargs
        Object[] varargs = invocationVarargs.getArguments();
        assertNotNull(varargs);
        assertEquals(3, varargs.length);
        assertEquals("a", varargs[0]);
        assertEquals("b", varargs[1]);
        assertEquals("c", varargs[2]);
    }

    @Test
    public void testGetRawArguments() {
        // Raw arguments should not expand varargs
        Object[] rawArgs = invocationVarargs.getRawArguments();
        assertNotNull(rawArgs);
        assertEquals(1, rawArgs.length);
        assertTrue(rawArgs[0] instanceof String[]);
        String[] rawArray = (String[]) rawArgs[0];
        assertEquals(3, rawArray.length);
    }

    @Test
    public void testArgumentsToMatchers() {
        // Default matchers from invocation
        List<org.mockito.internal.matchers.LocalizedMatcher> matchers = invocationAdd.argumentsToMatchers();
        assertNotNull(matchers);
        assertEquals(1, matchers.size());
        // The matcher should be an instance of Equals (default)
        assertTrue(matchers.get(0).getMatcher() instanceof org.mockito.internal.matchers.Equals);
    }

    @Test
    public void testIsVerifiedAndMarkVerified() {
        assertFalse(invocationAdd.isVerified());
        invocationAdd.markVerified();
        assertTrue(invocationAdd.isVerified());
        // Marking again should still be true
        invocationAdd.markVerified();
        assertTrue(invocationAdd.isVerified());
    }

    @Test
    public void testMatchesWithSameInvocation() {
        // An invocation should match itself
        InvocationMatcher matcher = new InvocationMatcher(invocationAdd);
        assertTrue(matcher.matches(invocationAdd));
    }

    @Test
    public void testMatchesWithDifferentInvocation() {
        // Create a different invocation with same method and arguments
        List<String> anotherMock = mock(List.class);
        anotherMock.add("test");
        Invocation otherInvocation = mockingDetails(anotherMock).getInvocations().get(0);
        InvocationMatcher matcher = new InvocationMatcher(invocationAdd);
        assertTrue(matcher.matches(otherInvocation));
    }

    @Test
    public void testMatchesWithDifferentArguments() {
        // Different arguments should not match
        List<String> anotherMock = mock(List.class);
        anotherMock.add("different");
        Invocation otherInvocation = mockingDetails(anotherMock).getInvocations().get(0);
        InvocationMatcher matcher = new InvocationMatcher(invocationAdd);
        assertFalse(matcher.matches(otherInvocation));
    }

    @Test
    public void testMatchesWithAnyMatcher() {
        // Using any() matcher should match any argument
        InvocationMatcher matcher = new InvocationMatcher(invocationAdd);
        // Replace the default matcher with any()
        matcher = new InvocationMatcher(invocationAdd, Arrays.asList(org.mockito.Matchers.any()));
        List<String> anotherMock = mock(List.class);
        anotherMock.add("anything");
        Invocation otherInvocation = mockingDetails(anotherMock).getInvocations().get(0);
        assertTrue(matcher.matches(otherInvocation));
    }

    @Test
    public void testVarargsWithNullArgument() {
        // Test varargs with null argument (should not cause NPE)
        VarargsInterface varargsMock = mock(VarargsInterface.class);
        varargsMock.varargsMethod((String) null);
        List<Invocation> invocations = mockingDetails(varargsMock).getInvocations();
        Invocation inv = invocations.get(0);
        Object[] args = inv.getArguments();
        assertNotNull(args);
        assertEquals(1, args.length);
        assertNull(args[0]);
    }

    @Test
    public void testVarargsWithEmptyArray() {
        // Test varargs with empty array
        VarargsInterface varargsMock = mock(VarargsInterface.class);
        varargsMock.varargsMethod();
        List<Invocation> invocations = mockingDetails(varargsMock).getInvocations();
        Invocation inv = invocations.get(0);
        Object[] args = inv.getArguments();
        assertNotNull(args);
        assertEquals(0, args.length);
    }

    @Test
    public void testVarargsWithMixedArguments() {
        // Test varargs with some fixed and varargs (if method had both)
        // Our interface only has varargs, but we can test with multiple calls
        VarargsInterface varargsMock = mock(VarargsInterface.class);
        varargsMock.varargsMethod("x", "y");
        List<Invocation> invocations = mockingDetails(varargsMock).getInvocations();
        Invocation inv = invocations.get(0);
        Object[] args = inv.getArguments();
        assertEquals(2, args.length);
        assertEquals("x", args[0]);
        assertEquals("y", args[1]);
    }

    @Test
    public void testBug36Scenario() {
        // Simulate the bug: varargs with any() matcher should not cause NPE
        // Create an invocation with varargs and then create an InvocationMatcher with any()
        VarargsInterface varargsMock = mock(VarargsInterface.class);
        varargsMock.varargsMethod("a", "b");
        List<Invocation> invocations = mockingDetails(varargsMock).getInvocations();
        Invocation inv = invocations.get(0);

        // Create matcher with any() for varargs
        InvocationMatcher matcher = new InvocationMatcher(inv, Arrays.asList(org.mockito.Matchers.any()));
        // This should not throw NullPointerException
        assertNotNull(matcher);
        // The matcher should match the same invocation
        assertTrue(matcher.matches(inv));
    }

    @Test
    public void testGetSequenceNumber() {
        // Sequence number should be positive
        assertTrue(invocationAdd.getSequenceNumber() > 0);
    }

    @Test
    public void testGetLocation() {
        // Location should not be null
        assertNotNull(invocationAdd.getLocation());
    }

    @Test
    public void testStubInfo() {
        // Initially no stub info
        assertNull(invocationAdd.stubInfo());
    }

    @Test
    public void testIsIgnoredForVerification() {
        assertFalse(invocationAdd.isIgnoredForVerification());
    }

    @Test
    public void testCallRealMethod() throws Exception {
        // Not easily testable without real implementation, but we can check method exists
        // This is a placeholder for coverage
        // We'll just ensure no exception when calling on mock invocation
        // Real invocation on a mock will throw, but we can test the method signature
        try {
            invocationAdd.callRealMethod();
            fail("Expected exception");
        } catch (Exception e) {
            // Expected: mock invocation cannot call real method
        }
    }
}