package org.mockito.exceptions;

import org.junit.Test;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.exceptions.verification.ArgumentsAreDifferent;
import org.mockito.exceptions.verification.NoInteractionsWanted;
import org.mockito.exceptions.verification.TooManyActualInvocations;
import org.mockito.exceptions.verification.VerificationInOrderFailure;
import org.mockito.exceptions.verification.WantedButNotInvoked;
import org.mockito.internal.reporting.Discrepancy;
import org.mockito.invocation.Invocation;
import org.mockito.invocation.Location;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

public class ReporterTest {

    @Test
    public void testCheckedExceptionCannotBeThrown() {
        Exception checkedEx = new Exception("checked");
        MockitoException ex = Reporter.checkedExceptionCannotBeThrown(checkedEx);
        assertNotNull(ex);
        assertTrue(ex.getMessage().contains("checked"));
        assertSame(checkedEx, ex.getCause());
    }

    @Test
    public void testCannotInitializeAnnotations() {
        MockitoException ex = Reporter.cannotInitializeAnnotations("fieldX", null);
        assertNotNull(ex);
        assertTrue(ex.getMessage().contains("fieldX"));
    }

    @Test
    public void testCannotInitializeMockForAnnotation() {
        MockitoException ex = Reporter.cannotInitializeMockForAnnotation("classX", null);
        assertNotNull(ex);
        assertTrue(ex.getMessage().contains("classX"));
    }

    @Test
    public void testFieldInitialisationThrewException() {
        Throwable cause = new RuntimeException("fail");
        MockitoException ex = Reporter.fieldInitialisationThrewException(null, cause);
        assertNotNull(ex);
        assertSame(cause, ex.getCause());
    }

    @Test
    public void testCannotStubNullMethod() {
        MockitoException ex = Reporter.cannotStubNullMethod();
        assertNotNull(ex);
    }

    @Test
    public void testMoreThanAllowedActualInvocations() {
        Discrepancy discrepancy = new Discrepancy(2, 5);
        Location location = null;
        TooManyActualInvocations ex = Reporter.moreThanAllowedActualInvocations(discrepancy, location);
        assertNotNull(ex);
    }

    @Test
    public void testNeverWantedButInvoked() {
        WantedButNotInvoked ex = Reporter.neverWantedButInvoked(null, null);
        assertNotNull(ex);
    }

    @Test
    public void testTooManyActualInvocations() {
        TooManyActualInvocations ex = Reporter.tooManyActualInvocations(1, 2, null, null);
        assertNotNull(ex);
    }

    @Test
    public void testTooLittleActualInvocations() {
        Discrepancy discrepancy = new Discrepancy(3, 1);
        MockitoException ex = Reporter.tooLittleActualInvocations(discrepancy, null, null);
        assertNotNull(ex);
    }

    @Test
    public void testTooLittleActualInvocationsInOrder() {
        Discrepancy discrepancy = new Discrepancy(3, 1);
        MockitoException ex = Reporter.tooLittleActualInvocationsInOrder(discrepancy, null, null);
        assertNotNull(ex);
    }

    @Test
    public void testWantedButNotInvoked() {
        WantedButNotInvoked ex = Reporter.wantedButNotInvoked(null, null);
        assertNotNull(ex);
    }

    @Test
    public void testWantedButNotInvokedInOrder() {
        WantedButNotInvoked ex = Reporter.wantedButNotInvokedInOrder(null, null);
        assertNotNull(ex);
    }

    @Test
    public void testArgumentsAreDifferent() {
        ArgumentsAreDifferent ex = Reporter.argumentsAreDifferent("wanted", "actual", null);
        assertNotNull(ex);
        assertTrue(ex.getMessage().contains("wanted"));
        assertTrue(ex.getMessage().contains("actual"));
    }

    @Test
    public void testNoInteractionsWanted() {
        NoInteractionsWanted ex = Reporter.noInteractionsWanted(null, null);
        assertNotNull(ex);
    }

    @Test
    public void testVerificationInOrderFailure() {
        VerificationInOrderFailure ex = Reporter.verificationInOrderFailure("some message");
        assertNotNull(ex);
        assertTrue(ex.getMessage().contains("some message"));
    }

    @Test
    public void testMocksHaveDifferentNames() {
        MockitoException ex = Reporter.mocksHaveDifferentNames("mock1", "mock2");
        assertNotNull(ex);
        assertTrue(ex.getMessage().contains("mock1"));
        assertTrue(ex.getMessage().contains("mock2"));
    }

    @Test
    public void testCannotInjectDependencies() {
        MockitoException ex = Reporter.cannotInjectDependencies("field", null, null);
        assertNotNull(ex);
    }

    @Test
    public void testNullArgument() {
        MockitoException ex = Reporter.nullArgument();
        assertNotNull(ex);
    }

    @Test
    public void testNotAMockPassedToVerify() {
        MockitoException ex = Reporter.notAMockPassedToVerify(null);
        assertNotNull(ex);
    }

    @Test
    public void testNullPassedToVerify() {
        MockitoException ex = Reporter.nullPassedToVerify();
        assertNotNull(ex);
    }

    @Test
    public void testNotAMockPassedToVerifyNoMoreInteractions() {
        MockitoException ex = Reporter.notAMockPassedToVerifyNoMoreInteractions();
        assertNotNull(ex);
    }

    @Test
    public void testNullPassedToVerifyNoMoreInteractions() {
        MockitoException ex = Reporter.nullPassedToVerifyNoMoreInteractions();
        assertNotNull(ex);
    }

    @Test
    public void testNotAMockPassedToWhen() {
        MockitoException ex = Reporter.notAMockPassedToWhen();
        assertNotNull(ex);
    }

    @Test
    public void testNullPassedToWhen() {
        MockitoException ex = Reporter.nullPassedToWhen();
        assertNotNull(ex);
    }

    @Test
    public void testSmartNullPointerException() {
        MockitoException ex = Reporter.smartNullPointerException("method", null);
        assertNotNull(ex);
        assertTrue(ex.getMessage().contains("method"));
    }

    @Test
    public void testNoMoreInteractionsWanted() {
        NoInteractionsWanted ex = Reporter.noMoreInteractionsWanted(null, null);
        assertNotNull(ex);
    }

    @Test
    public void testWrongTypeOfReturnValue() {
        MockitoException ex = Reporter.wrongTypeOfReturnValue("String", "Integer", "method");
        assertNotNull(ex);
        assertTrue(ex.getMessage().contains("String"));
        assertTrue(ex.getMessage().contains("Integer"));
    }

    @Test
    public void testShortcutOfVoidMethodsInWrongContext() {
        MockitoException ex = Reporter.shortcutOfVoidMethodsInWrongContext();
        assertNotNull(ex);
    }

    @Test
    public void testInvalidUseOfMatchers() {
        MockitoException ex = Reporter.invalidUseOfMatchers(1, null);
        assertNotNull(ex);
    }

    @Test
    public void testUnfinishedVerificationException() {
        MockitoException ex = Reporter.unfinishedVerificationException();
        assertNotNull(ex);
    }

    @Test
    public void testUnfinishedStubbing() {
        MockitoException ex = Reporter.unfinishedStubbing(null);
        assertNotNull(ex);
    }

    @Test
    public void testWritingInspectors() {
        MockitoException ex = Reporter.writingInspectors();
        assertNotNull(ex);
    }

    @Test
    public void testMissingMethodInvocation() {
        MockitoException ex = Reporter.missingMethodInvocation();
        assertNotNull(ex);
    }

    @Test
    public void testCollectionOfreturnValues() {
        Collection<String> col = Arrays.asList("a", "b");
        String res = Reporter.join(col);
        assertNotNull(res);
    }
}