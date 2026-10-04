package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.mock.MockSettings;
import org.mockito.stubbing.Answer;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;

import static org.junit.Assert.*;

public class ReturnsDeepStubsTest {

    interface SampleInterface {
        SampleInterface getNested();
        List<String> getList();
        String getString();
        <T> T genericMethod();
    }

    @Test
    public void testReturnsDeepStubsAnswer() throws Throwable {
        ReturnsDeepStubs deepStubs = new ReturnsDeepStubs();
        assertNotNull(deepStubs);

        SampleInterface mock = Mockito.mock(SampleInterface.class, deepStubs);
        assertNotNull(mock);

        // This will trigger the deep stub creation logic (ReturnsDeepStubs execution)
        SampleInterface nestedMock = mock.getNested();
        assertNotNull(nestedMock);

        // Chaining further
        assertNotNull(nestedMock.getNested());
    }

    @Test
    public void testDeepStubsGenericReturnType() throws Throwable {
        ReturnsDeepStubs deepStubs = new ReturnsDeepStubs();
        SampleInterface mock = Mockito.mock(SampleInterface.class, deepStubs);

        // Invoking a method with a generic return type or collection
        List<String> list = mock.getList();
        // Depending on deep stubs handling of collections/generics
        assertNotNull(list);
    }

    @Test
    public void testAnswerWithDifferentReturnTypes() throws Throwable {
        ReturnsDeepStubs deepStubs = new ReturnsDeepStubs();
        SampleInterface mock = Mockito.mock(SampleInterface.class, deepStubs);

        String str = mock.getString();
        // For non-mockable or final or primitive/String types, deep stubs returns empty/default or null
        // Let's verify it doesn't throw unexpected exceptions
        assertNull(str);
    }

    @Test
    public void testSerializableDeepStubs() {
        ReturnsDeepStubs deepStubs = new ReturnsDeepStubs();
        assertTrue(deepStubs instanceof Serializable);
    }
}