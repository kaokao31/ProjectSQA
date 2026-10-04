package org.mockito.internal.stubbing.answers;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.*;

public class CallsRealMethodsTest {

    interface DummyInterface {
        String execute();
    }

    static class DummyClass implements DummyInterface {
        public String execute() {
            return "real method executed";
        }

        public String overloaded(String val) {
            return "val: " + val;
        }

        public String overloaded(int val) {
            return "int: " + val;
        }
    }

    @Test
    public void testCallsRealMethodsExecution() throws Throwable {
        CallsRealMethods answer = new CallsRealMethods();
        DummyClass dummy = new DummyClass();

        // Create a mock invocation
        InvocationOnMock invocation = org.mockito.Mockito.mock(InvocationOnMock.class);
        org.mockito.Mockito.when(invocation.getMock()).thenReturn(dummy);
        org.mockito.Mockito.when(invocation.getMethod()).thenReturn(DummyClass.class.getMethod("execute"));
        org.mockito.Mockito.when(invocation.getArguments()).thenReturn(new Object[0]);

        Object result = answer.answer(invocation);
        assertEquals("real method executed", result);
    }

    @Test(expected = Throwable.class)
    public void testCallsRealMethodsAbstractMethod() throws Throwable {
        CallsRealMethods answer = new CallsRealMethods();
        
        InvocationOnMock invocation = org.mockito.Mockito.mock(InvocationOnMock.class);
        org.mockito.Mockito.when(invocation.getMock()).thenReturn(new DummyInterface() {
            @Override
            public String execute() {
                return null;
            }
        });
        org.mockito.Mockito.when(invocation.getMethod()).thenReturn(DummyInterface.class.getMethod("execute"));
        org.mockito.Mockito.when(invocation.getArguments()).thenReturn(new Object[0]);

        // Calling a method where real implementation might fail or abstract checks apply
        // Depending on Mockito's internal handling of abstract/interface methods with CallsRealMethods
        answer.answer(invocation);
    }
}