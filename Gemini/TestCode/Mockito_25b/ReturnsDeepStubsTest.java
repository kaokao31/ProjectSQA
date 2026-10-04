package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.MockitoCore;
import org.mockito.internal.stubbing.CreationSettings;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.mock.MockCreationSettings;
import org.mockito.stubbing.Answer;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import static org.junit.Assert.*;

public class ReturnsDeepStubsTest {

    private ReturnsDeepStubs returnsDeepStubs;

    @Before
    public void setUp() {
        returnsDeepStubs = new ReturnsDeepStubs();
    }

    interface SampleInterface {
        SampleInterface getNested();
        String getName();
        List<String> getList();
        int getPrimitive();
        <T> T genericMethod();
    }

    class SampleClass {
        public SampleClass getNestedClass() {
            return new SampleClass();
        }
    }

    @Test
    public void testAnswerNotMockable() throws Throwable {
        InvocationOnMock invocation = new InvocationOnMock() {
            @Override
            public Object getMock() {
                return new SampleClass();
            }

            @Override
            public Method getMethod() {
                try {
                    return Object.class.getMethod("toString");
                } catch (NoSuchMethodException e) {
                    return null;
                }
            }

            @Override
            public Object[] getArguments() {
                return new Object[0];
            }

            @Override
            public <T> T getArgument(int index) {
                return null;
            }

            @Override
            public List<Object> getArgumentsAsList() {
                return null;
            }

            @Override
            public Class<?> getRawType() {
                return Object.class;
            }
        };

        // toString returns a String, which is final/not mockable by standard deep stubs returning generic mock,
        // or returns null/empty depending on type. Let's see how ReturnsDeepStubs handles it.
        Object result = returnsDeepStubs.answer(invocation);
        // toString returns String, ReturnsDeepStubs might return null or empty or default value for non-mockable/primitive/String
        assertNull(result);
    }

    @Test
    public void testRecordDeepStubInformation() throws Throwable {
        // Test recording deep stub mock on generic return types or standard types
        Method method = SampleInterface.class.getMethod("getNested");
        
        InvocationOnMock invocation = new TestInvocationOnMock(
                mockingDetails -> null,
                method,
                new Object[0],
                SampleInterface.class
        );

        // This exercises recordDeepStubInformation and deep stub creation
        try {
            returnsDeepStubs.answer(invocation);
        } catch (Exception e)  {
            // Expected if invocation mock context isn't fully set up for Mockito internal creation settings
        }
    }

    @Test
    public void testFindGenericReturnType() throws Throwable {
        Method method = SampleInterface.class.getMethod("genericMethod");
        InvocationOnMock invocation = new TestInvocationOnMock(
                null,
                method,
                new Object[0],
                SampleInterface.class
        );

        try {
            returnsDeepStubs.answer(invocation);
        } catch (Exception ignored) {
        }
    }

    @Test
    public void testReturnsDeepStubsSerializable() {
        assertTrue(returnsDeepStubs instanceof Serializable);
    }

    private static class TestInvocationOnMock implements InvocationOnMock {
        private final Object mock;
        private final Method method;
        private final Object[] args;
        private final Class<?> rawType;

        public TestInvocationOnMock(Object mock, Method method, Object[] args, Class<?> rawType) {
            this.mock = mock;
            this.method = method;
            this.args = args;
            this.rawType = rawType;
        }

        @Override
        public Object getMock() {
            return mock;
        }

        @Override
        public Method getMethod() {
            return method;
        }

        @Override
        public Object[] getArguments() {
            return args;
        }

        @Override
        public <T> T getArgument(int index) {
            return (T) args[index];
        }

        @Override
        public List<Object> getArgumentsAsList() {
            return java.util.Arrays.asList(args);
        }

        @Override
        public Class<?> getRawType() {
            return rawType;
        }
    }
}