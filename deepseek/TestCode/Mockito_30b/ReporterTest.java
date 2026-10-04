package org.mockito.exceptions;

import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.MockUtil;
import org.mockito.internal.util.MockName;
import org.mockito.internal.debugging.LocationImpl;
import org.mockito.internal.invocation.InvocationImpl;
import org.mockito.internal.invocation.MatchersBinder;
import org.mockito.internal.invocation.StubInfoImpl;
import org.mockito.internal.progress.ThreadSafeMockingProgress;
import org.mockito.invocation.Invocation;
import org.mockito.invocation.Location;
import org.mockito.invocation.MockHandler;
import org.mockito.mock.MockCreationSettings;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class ReporterTest {

    private Reporter reporter;
    private MockitoLogger logger;

    @Before
    public void setUp() {
        logger = new MockitoLogger();
        reporter = new Reporter(logger);
    }

    @Test
    public void testCannotStubVoidMethod() {
        try {
            reporter.cannotStubVoidMethod();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("void"));
        }
    }

    @Test
    public void testWantedButNotInvoked() {
        try {
            reporter.wantedButNotInvoked(null);
            fail("Expected WantedButNotInvoked");
        } catch (WantedButNotInvoked e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testWantedButNotInvokedWithInvocation() {
        Invocation invocation = createMockInvocation();
        try {
            reporter.wantedButNotInvoked(invocation);
            fail("Expected WantedButNotInvoked");
        } catch (WantedButNotInvoked e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testSmartNullWithPrimitiveType() {
        // This test targets potential NPE when type is a primitive class
        // Bug context: Mockito 30 - smartNulls with primitives
        try {
            reporter.smartNull(int.class);
            // If no exception, the method returned something (likely a string)
        } catch (Exception e) {
            fail("smartNull should not throw exception for primitive type: " + e.getMessage());
        }
    }

    @Test
    public void testSmartNullWithObjectType() {
        String result = reporter.smartNull(String.class);
        assertNotNull(result);
        assertTrue(result.contains("smart"));
    }

    @Test
    public void testSmartNullWithNullType() {
        // Edge case: null type
        try {
            reporter.smartNull(null);
            fail("Expected NullPointerException or IllegalArgumentException");
        } catch (NullPointerException | IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testSensibleExceptionWithNull() {
        try {
            reporter.sensibleException(null);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testSensibleExceptionWithPrimitive() {
        try {
            reporter.sensibleException(int.class);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testSensibleExceptionWithObject() {
        try {
            reporter.sensibleException(String.class);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testCannotCallRealMethodOnInterface() {
        try {
            reporter.cannotCallRealMethodOnInterface();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testRedundantInvocation() {
        Invocation invocation = createMockInvocation();
        try {
            reporter.redundantInvocation(invocation);
            fail("Expected RedundantInvocation");
        } catch (RedundantInvocation e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testRedundantInvocationWithNull() {
        try {
            reporter.redundantInvocation(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test
    public void testTooLittleActualInvocations() {
        try {
            reporter.tooLittleActualInvocations(new LinkedList<Invocation>(), new LinkedList<Invocation>());
            fail("Expected TooLittleActualInvocations");
        } catch (TooLittleActualInvocations e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testTooLittleActualInvocationsWithNull() {
        try {
            reporter.tooLittleActualInvocations(null, null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test
    public void testTooManyActualInvocations() {
        try {
            reporter.tooManyActualInvocations(1, 2, new LinkedList<Invocation>());
            fail("Expected TooManyActualInvocations");
        } catch (TooManyActualInvocations e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testTooManyActualInvocationsWithNull() {
        try {
            reporter.tooManyActualInvocations(1, 2, null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test
    public void testArgumentsAreDifferent() {
        try {
            reporter.argumentsAreDifferent(null, null, null);
            fail("Expected ArgumentsAreDifferent");
        } catch (ArgumentsAreDifferent e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testStubPassedToVerify() {
        try {
            reporter.stubPassedToVerify(null);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testNotAMockPassedToVerify() {
        try {
            reporter.notAMockPassedToVerify(null);
            fail("Expected NotAMockException");
        } catch (NotAMockException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testNullPassedToVerify() {
        try {
            reporter.nullPassedToVerify();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testNoMoreInteractionsWanted() {
        try {
            reporter.noMoreInteractionsWanted(null);
            fail("Expected NoInteractionsWanted");
        } catch (NoInteractionsWanted e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testNoMoreInteractionsWantedWithInvocation() {
        Invocation invocation = createMockInvocation();
        try {
            reporter.noMoreInteractionsWanted(invocation);
            fail("Expected NoInteractionsWanted");
        } catch (NoInteractionsWanted e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testCannotMockFinalClass() {
        try {
            reporter.cannotMockFinalClass(String.class);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testCannotMockFinalClassWithNull() {
        try {
            reporter.cannotMockFinalClass(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test
    public void testCannotStubAbstractMethod() {
        try {
            reporter.cannotStubAbstractMethod();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testUnfinishedStubbing() {
        try {
            reporter.unfinishedStubbing(new LocationImpl());
            fail("Expected UnfinishedStubbingException");
        } catch (UnfinishedStubbingException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testUnfinishedStubbingWithNullLocation() {
        try {
            reporter.unfinishedStubbing(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test
    public void testUnfinishedVerificationException() {
        try {
            reporter.unfinishedVerificationException(new LocationImpl());
            fail("Expected UnfinishedVerificationException");
        } catch (UnfinishedVerificationException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testUnfinishedVerificationExceptionWithNull() {
        try {
            reporter.unfinishedVerificationException(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test
    public void testMissingMethodInvocation() {
        try {
            reporter.missingMethodInvocation();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testNotAMockPassedWhenVerifyingNoMoreInteractions() {
        try {
            reporter.notAMockPassedWhenVerifyingNoMoreInteractions();
            fail("Expected NotAMockException");
        } catch (NotAMockException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testExceptionMessageShouldContainMethodName() {
        // This test verifies that exception messages contain the method name
        Invocation invocation = createMockInvocation();
        try {
            reporter.wantedButNotInvoked(invocation);
        } catch (WantedButNotInvoked e) {
            assertTrue(e.getMessage().contains("someMethod"));
        }
    }

    // Helper method to create a mock Invocation
    private Invocation createMockInvocation() {
        MockHandler handler = mock(MockHandler.class);
        MockCreationSettings settings = mock(MockCreationSettings.class);
        MockUtil mockUtil = new MockUtil();
        // Create a simple invocation using InvocationImpl
        try {
            return new InvocationImpl(
                    mock(Object.class),
                    Object.class.getMethod("toString"),
                    new Object[0],
                    1,
                    new ThreadSafeMockingProgress()
            );
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}