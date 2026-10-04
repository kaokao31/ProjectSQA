package org.mockito.internal.stubbing.answers;

import org.junit.Test;
import org.junit.Before;
import org.mockito.internal.invocation.InvocationMatcher;
import org.mockito.internal.stubbing.answers.AnswersValidator;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.stubbing.answers.Returns;
import org.mockito.internal.stubbing.answers.ThrowsException;
import org.mockito.internal.stubbing.answers.CallsRealMethods;
import org.mockito.internal.stubbing.answers.DoesNothing;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Method;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AnswersValidatorTest {

    private AnswersValidator validator;
    private InvocationOnMock invocation;
    private Method method;

    @Before
    public void setUp() throws Exception {
        validator = new AnswersValidator();
        // Create a mock invocation for testing
        invocation = mock(InvocationOnMock.class);
        method = Object.class.getMethod("toString");
        when(invocation.getMethod()).thenReturn(method);
    }

    @Test(expected = MockitoException.class)
    public void testValidate_ThrowsExceptionWithNullThrowable() {
        ThrowsException answer = new ThrowsException(null);
        validator.validate(answer, invocation);
    }

    @Test
    public void testValidate_ThrowsExceptionWithValidThrowable() {
        ThrowsException answer = new ThrowsException(new RuntimeException("test"));
        validator.validate(answer, invocation);
        // Should not throw exception
    }

    @Test(expected = MockitoException.class)
    public void testValidate_ReturnsWithNullValue() {
        Returns answer = new Returns(null);
        validator.validate(answer, invocation);
    }

    @Test
    public void testValidate_ReturnsWithValidValue() {
        Returns answer = new Returns("valid");
        validator.validate(answer, invocation);
        // Should not throw exception
    }

    @Test
    public void testValidate_DoesNothing() {
        DoesNothing answer = new DoesNothing();
        validator.validate(answer, invocation);
        // Should not throw exception
    }

    @Test
    public void testValidate_CallsRealMethods() {
        CallsRealMethods answer = new CallsRealMethods();
        validator.validate(answer, invocation);
        // Should not throw exception
    }

    @Test(expected = MockitoException.class)
    public void testValidate_WithNullAnswer() {
        validator.validate(null, invocation);
    }

    @Test(expected = MockitoException.class)
    public void testValidate_WithNullInvocation() {
        Returns answer = new Returns("test");
        validator.validate(answer, null);
    }

    @Test
    public void testValidate_WithVoidMethodAndReturns() throws Exception {
        Method voidMethod = Object.class.getMethod("wait");
        when(invocation.getMethod()).thenReturn(voidMethod);
        Returns answer = new Returns("test");
        validator.validate(answer, invocation);
        // Should not throw exception for void method with Returns
    }

    @Test(expected = MockitoException.class)
    public void testValidate_WithPrimitiveReturnTypeAndNullValue() throws Exception {
        Method intMethod = Integer.class.getMethod("intValue");
        when(invocation.getMethod()).thenReturn(intMethod);
        Returns answer = new Returns(null);
        validator.validate(answer, invocation);
    }

    @Test
    public void testValidate_WithPrimitiveReturnTypeAndValidValue() throws Exception {
        Method intMethod = Integer.class.getMethod("intValue");
        when(invocation.getMethod()).thenReturn(intMethod);
        Returns answer = new Returns(5);
        validator.validate(answer, invocation);
        // Should not throw exception
    }

    @Test(expected = MockitoException.class)
    public void testValidate_WithWrongTypeReturnValue() throws Exception {
        Method stringMethod = String.class.getMethod("length");
        when(invocation.getMethod()).thenReturn(stringMethod);
        Returns answer = new Returns(123); // Integer instead of int (autoboxing)
        validator.validate(answer, invocation);
    }

    @Test
    public void testValidate_WithMatchingTypeReturnValue() throws Exception {
        Method stringMethod = String.class.getMethod("length");
        when(invocation.getMethod()).thenReturn(stringMethod);
        Returns answer = new Returns(5); // int matches int return type
        validator.validate(answer, invocation);
        // Should not throw exception
    }

    @Test(expected = MockitoException.class)
    public void testValidate_ThrowsExceptionWithCheckedExceptionNotDeclared() throws Exception {
        Method toStringMethod = Object.class.getMethod("toString");
        when(invocation.getMethod()).thenReturn(toStringMethod);
        ThrowsException answer = new ThrowsException(new Exception("checked exception"));
        validator.validate(answer, invocation);
    }

    @Test
    public void testValidate_ThrowsExceptionWithRuntimeException() throws Exception {
        Method toStringMethod = Object.class.getMethod("toString");
        when(invocation.getMethod()).thenReturn(toStringMethod);
        ThrowsException answer = new ThrowsException(new RuntimeException("runtime"));
        validator.validate(answer, invocation);
        // Should not throw exception
    }

    @Test(expected = MockitoException.class)
    public void testValidate_ThrowsExceptionWithError() throws Exception {
        Method toStringMethod = Object.class.getMethod("toString");
        when(invocation.getMethod()).thenReturn(toStringMethod);
        ThrowsException answer = new ThrowsException(new Error("error"));
        validator.validate(answer, invocation);
    }

    @Test
    public void testValidate_ThrowsExceptionWithDeclaredCheckedException() throws Exception {
        // Create a method that declares throwing Exception
        class TestClass {
            @SuppressWarnings("unused")
            public void testMethod() throws Exception {
            }
        }
        Method testMethod = TestClass.class.getMethod("testMethod");
        when(invocation.getMethod()).thenReturn(testMethod);
        ThrowsException answer = new ThrowsException(new Exception("declared checked"));
        validator.validate(answer, invocation);
        // Should not throw exception
    }

    @Test(expected = MockitoException.class)
    public void testValidate_ThrowsExceptionWithNullThrowableAndVoidMethod() throws Exception {
        Method voidMethod = Object.class.getMethod("wait");
        when(invocation.getMethod()).thenReturn(voidMethod);
        ThrowsException answer = new ThrowsException(null);
        validator.validate(answer, invocation);
    }

    @Test
    public void testValidate_ThrowsExceptionWithVoidMethod() throws Exception {
        Method voidMethod = Object.class.getMethod("wait");
        when(invocation.getMethod()).thenReturn(voidMethod);
        ThrowsException answer = new ThrowsException(new RuntimeException("void method"));
        validator.validate(answer, invocation);
        // Should not throw exception
    }

    @Test(expected = MockitoException.class)
    public void testValidate_WithNullAnswerAndNullInvocation() {
        validator.validate(null, null);
    }

    @Test
    public void testValidate_WithMultipleValidations() {
        // Test that validator can handle multiple validations without state issues
        Returns answer1 = new Returns("first");
        Returns answer2 = new Returns("second");
        validator.validate(answer1, invocation);
        validator.validate(answer2, invocation);
        // Should not throw exception
    }

    @Test(expected = MockitoException.class)
    public void testValidate_WithInvalidReturnTypeForPrimitive() throws Exception {
        Method booleanMethod = Boolean.class.getMethod("booleanValue");
        when(invocation.getMethod()).thenReturn(booleanMethod);
        Returns answer = new Returns("not a boolean");
        validator.validate(answer, invocation);
    }

    @Test
    public void testValidate_WithValidPrimitiveReturn() throws Exception {
        Method booleanMethod = Boolean.class.getMethod("booleanValue");
        when(invocation.getMethod()).thenReturn(booleanMethod);
        Returns answer = new Returns(true);
        validator.validate(answer, invocation);
        // Should not throw exception
    }

    @Test(expected = MockitoException.class)
    public void testValidate_ThrowsExceptionWithNullThrowableAndNonNullInvocation() {
        ThrowsException answer = new ThrowsException(null);
        validator.validate(answer, invocation);
    }

    @Test
    public void testValidate_ThrowsExceptionWithNonNullThrowableAndNullInvocation() {
        ThrowsException answer = new ThrowsException(new RuntimeException());
        try {
            validator.validate(answer, null);
            fail("Should have thrown MockitoException");
        } catch (MockitoException e) {
            // Expected
        }
    }
}