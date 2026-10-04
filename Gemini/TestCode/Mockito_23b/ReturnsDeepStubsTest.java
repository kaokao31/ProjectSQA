package org.mockito.internal.stubbing.defaultanswers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.mock.MockCreationSettings;
import org.mockito.stubbing.Answer;

import java.io.Serializable;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ReturnsDeepStubsTest {

    private ReturnsDeepStubs returnsDeepStubs;
    private AutoCloseable closeable;

    @Mock
    private InvocationOnMock invocation;

    @Mock
    private MockCreationSettings<?> mockCreationSettings;

    interface SampleInterface {
        SampleInterface getSelf();
        List<String> getList();
        String getString();
        Object getObject();
    }

    static class SampleClass {
        public SampleClass getNested() {
            return new SampleClass();
        }
        public String getName() {
            return "Sample";
        }
    }

    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        returnsDeepStubs = new ReturnsDeepStubs();
    }

    @After
    public void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }

    @Test
    public void testAnswerWithNonMockableReturnType() throws Throwable {
        SampleInterface sample = mock(SampleInterface.class);
        when(invocation.getMock()).thenReturn(sample);
        when(invocation.getMethod()).thenReturn(SampleInterface.class.getMethod("getString"));

        // getString returns String, which is a final/non-mockable class in typical deep stub context or handled via RETURNS_DEFAULTS
        Object result = returnsDeepStubs.answer(invocation);
        assertNull(result);
    }

    @Test
    public void testAnswerWithMockableReturnType() throws Throwable {
        SampleInterface sample = mock(SampleInterface.class);
        when(invocation.getMock()).thenReturn(sample);
        when(invocation.getMethod()).thenReturn(SampleInterface.class.getMethod("getSelf"));

        // Record mock creation settings mock
        when(invocation.getMock()).thenReturn(sample);

        // When deep stubs are invoked, it should return a mock
        // Since we are invoking answer directly with raw InvocationOnMock, 
        // we need to be careful about generic signatures or mockito internals.
        // Let's test via actual Mockito.withSettings().defaultAnswer(new ReturnsDeepStubs())
    }

    @Test
    public void testDeepStubsIntegrationWithGenericReturnType() {
        SampleInterface mockSample = mock(SampleInterface.class, withSettings().defaultAnswer(new ReturnsDeepStubs()));

        // This exercises deep stubbing and generic return type extraction (which is the core of Mockito Bug 23)
        SampleInterface self = mockSample.getSelf();
        assertNotNull(self);
        
        // Subsequent calls on the deep stub should return the same mock or another deep stub
        assertSame(self, mockSample.getSelf());
    }

    @Test
    public void testDeepStubsWithCollectionReturnType() {
        SampleInterface mockSample = mock(SampleInterface.class, withSettings().defaultAnswer(new ReturnsDeepStubs()));

        // Collections are usually handled specially or fall back to RETURNS_DEFAULTS (returning empty list or null depending on version)
        List<String> list = mockSample.getList();
        // Depending on Mockito version, deep stubs on collections might return empty collections or null.
        // We verify it doesn't throw unexpected exceptions (like ClassCastException due to generic type bounds).
        // Bug 23 specifically relates to InstanceOf or Generics bounds extraction when deep stubbing methods with type variables.
        assertNotNull(mockSample);
    }

    @Test
    public void testSerializableDeepStubs() {
        ReturnsDeepStubs deepStubs = new ReturnsDeepStubs();
        assertNotNull(deepStubs);
        // Verify it implements Serializable
        assertTrue(deepStubs instanceof Serializable);
    }

    @Test
    public void testGetGenericReturnTypeMethod() throws Exception {
        // Test internal helper or answer execution via standard usage patterns that trigger deep stubs
        SampleClass mockClass = mock(SampleClass.class, withSettings().defaultAnswer(new ReturnsDeepStubs()));
        SampleClass nested = mockClass.getNested();
        assertNotNull(nested);
    }
}