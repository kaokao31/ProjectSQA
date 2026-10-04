package org.mockito.internal.creation.bytebuddy;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockCreationException;
import org.mockito.MockSettings;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.invocation.MockHandler;
import org.mockito.mock.MockCreationSettings;
import org.mockito.mock.MockName;
import org.mockito.mock.SerializableMode;
import org.mockito.internal.creation.MockSettingsImpl;
import org.mockito.internal.handler.MockHandlerFactory;
import org.mockito.internal.configuration.plugins.PluginRegistry;
import org.mockito.plugins.MockMaker;
import org.mockito.stubbing.Answer;

import java.io.*;
import java.util.*;

/**
 * Test suite for ByteBuddyMockMaker focusing on high coverage and bug detection (Defects4J Mockito-20).
 */
public class ByteBuddyMockMakerTest {

    private ByteBuddyMockMaker mockMaker;
    private MockHandler<Object> defaultHandler;

    @Before
    public void setUp() {
        mockMaker = new ByteBuddyMockMaker();
        defaultHandler = MockHandlerFactory.createMockHandler(new MockSettingsImpl());
    }

    // -----------------------------------------------------------------------
    // Basic creation tests
    // -----------------------------------------------------------------------

    @Test
    public void createMock_basicInterface() {
        MockCreationSettings<Comparable> settings = new MockSettingsImpl<Comparable>()
                .setTypeToMock(Comparable.class);
        Object mock = mockMaker.createMock(settings, defaultHandler);
        assertNotNull("Mock should not be null", mock);
        assertTrue("Mock should implement Comparable", mock instanceof Comparable);
    }

    @Test
    public void createMock_basicClass() {
        MockCreationSettings<ArrayList> settings = new MockSettingsImpl<ArrayList>()
                .setTypeToMock(ArrayList.class);
        Object mock = mockMaker.createMock(settings, defaultHandler);
        assertNotNull(mock);
        assertTrue(mock instanceof ArrayList);
    }

    @Test(expected = MockCreationException.class)
    public void createMock_finalClassThrowsException() {
        MockCreationSettings<String> settings = new MockSettingsImpl<String>()
                .setTypeToMock(String.class);
        mockMaker.createMock(settings, defaultHandler);
    }

    @Test(expected = MockCreationException.class)
    public void createMock_primitiveThrowsException() {
        MockCreationSettings<Integer> settings = new MockSettingsImpl<Integer>()
                .setTypeToMock(Integer.class);
        mockMaker.createMock(settings, defaultHandler); // Integer is final, but also primitive wrapper? Actually Integer is final, so should throw.
    }

    @Test(expected = MockCreationException.class)
    public void createMock_voidTypeThrowsException() {
        MockCreationSettings<Void> settings = new MockSettingsImpl<Void>()
                .setTypeToMock(Void.class);
        mockMaker.createMock(settings, defaultHandler);
    }

    // -----------------------------------------------------------------------
    // Settings variations
    // -----------------------------------------------------------------------

    @Test
    public void createMock_withName() {
        MockSettingsImpl<String> settings = new MockSettingsImpl<String>()
                .setTypeToMock(Comparable.class)
                .setName("customMock");
        // ByteBuddyMockMaker might use internal naming, but we just check the mock object is not null
        Object mock = mockMaker.createMock(settings, defaultHandler);
        assertNotNull(mock);
    }

    @Test
    public void createMock_withSerializable() {
        MockSettingsImpl<Serializable> settings = new MockSettingsImpl<Serializable>()
                .setTypeToMock(Serializable.class)
                .serializable();
        Object mock = mockMaker.createMock(settings, defaultHandler);
        assertNotNull(mock);
        assertTrue("Mock should be Serializable", mock instanceof Serializable);
    }

    @Test
    public void createMock_serializableRoundtrip() throws Exception {
        // Create a mock of a plain interface with serializable mode
        MockSettingsImpl<Comparable> settings = new MockSettingsImpl<Comparable>()
                .setTypeToMock(Comparable.class)
                .serializable(SerializableMode.ACROSS_CLASSLOADERS); // Bug 20 often relates to ACROSS_CLASSLOADERS
        Object mock = mockMaker.createMock(settings, defaultHandler);

        // Serialize and deserialize
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(mock);
        oos.flush();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        Object deserialized = ois.readObject();

        assertNotNull(deserialized);
        assertTrue(deserialized instanceof Comparable);
        // If the bug is present, deserialization may throw an exception or produce wrong class
    }

    @Test
    public void createMock_withExtraInterfaces() {
        MockSettingsImpl<ArrayList> settings = new MockSettingsImpl<ArrayList>()
                .setTypeToMock(ArrayList.class)
                .extraInterfaces(Runnable.class, List.class);
        Object mock = mockMaker.createMock(settings, defaultHandler);
        assertTrue("Mock should implement Runnable", mock instanceof Runnable);
        assertTrue("Mock should implement List", mock instanceof List);
    }

    @Test(expected = MockCreationException.class)
    public void createMock_withInconsistentExtraInterfaceThrows() {
        // Trying to add an extra interface that is already the primary type
        MockSettingsImpl<ArrayList> settings = new MockSettingsImpl<ArrayList>()
                .setTypeToMock(ArrayList.class)
                .extraInterfaces(ArrayList.class);
        mockMaker.createMock(settings, defaultHandler);
    }

    @Test
    public void createMock_withStubOnly() {
        // StubOnly might affect invocation handling; we just check mock is created
        MockSettingsImpl<Comparable> settings = new MockSettingsImpl<Comparable>()
                .setTypeToMock(Comparable.class)
                .stubOnly();
        Object mock = mockMaker.createMock(settings, defaultHandler);
        assertNotNull(mock);
    }

    // -----------------------------------------------------------------------
    // Handler delegation tests
    // -----------------------------------------------------------------------

    @Test
    public void createMock_customHandlerIsUsed() throws Throwable {
        final List<String> invocations = new ArrayList<String>();
        MockHandler<Object> recordingHandler = new MockHandler<Object>() {
            @Override
            public Object handle(InvocationOnMock invocation) throws Throwable {
                invocations.add(invocation.getMethod().getName());
                return null;
            }

            // other MockHandler methods are not needed for this test
            @Override
            public MockCreationSettings<Object> getMockSettings() {
                return new MockSettingsImpl<Object>().setTypeToMock(Object.class);
            }

            @Override
            public void setAnswersForStubbing(List<Answer<?>> answers) { }

            @Override
            public void setInvocationListeners(List<?> listeners) { }

            @Override
            public Object getInvocationListeners() { return null; }
        };

        MockCreationSettings<Comparable> settings = new MockSettingsImpl<Comparable>()
                .setTypeToMock(Comparable.class);
        Object mock = mockMaker.createMock(settings, recordingHandler);
        // Explicitly call a method
        ((Comparable) mock).compareTo("test");
        assertEquals("Handler should have been invoked once", 1, invocations.size());
        assertEquals("compareTo", invocations.get(0));
    }

    // -----------------------------------------------------------------------
    // isTypeMockable tests
    // -----------------------------------------------------------------------

    @Test
    public void isTypeMockable_interface() {
        assertTrue(mockMaker.isTypeMockable(Comparable.class));
    }

    @Test
    public void isTypeMockable_class() {
        assertTrue(mockMaker.isTypeMockable(ArrayList.class));
    }

    @Test
    public void isTypeMockable_finalClass() {
        // String is final, should not be mockable
        assertFalse(mockMaker.isTypeMockable(String.class));
    }

    @Test
    public void isTypeMockable_primitive() {
        assertFalse(mockMaker.isTypeMockable(int.class));
    }

    @Test
    public void isTypeMockable_void() {
        assertFalse(mockMaker.isTypeMockable(void.class));
    }

    @Test
    public void isTypeMockable_enum() {
        assertFalse(mockMaker.isTypeMockable(Thread.State.class));
    }

    @Test
    public void isTypeMockable_array() {
        assertFalse(mockMaker.isTypeMockable(Object[].class));
    }

    // -----------------------------------------------------------------------
    // Edge cases and potential bug triggers
    // -----------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void createMock_nullSettingsThrows() {
        mockMaker.createMock(null, defaultHandler);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createMock_nullHandlerThrows() {
        MockCreationSettings<Object> settings = new MockSettingsImpl<Object>().setTypeToMock(Object.class);
        mockMaker.createMock(settings, null);
    }

    @Test(expected = MockCreationException.class)
    public void createMock_withAbstractClassWithoutConstructor() {
        // Abstract class with no default constructor might fail? Depends on implementation
        // We use a custom abstract class with no default constructor
        abstract class AbstractNoConstructor {
            AbstractNoConstructor(int x) {}
        }
        MockCreationSettings<AbstractNoConstructor> settings =
                new MockSettingsImpl<AbstractNoConstructor>().setTypeToMock(AbstractNoConstructor.class);
        mockMaker.createMock(settings, defaultHandler);
    }

    @Test
    public void createMock_withMultipleExtraInterfaces() {
        // Use many extra interfaces to stress the bytecode generation
        MockSettingsImpl<Object> settings = new MockSettingsImpl<Object>()
                .setTypeToMock(Object.class)
                .extraInterfaces(Runnable.class, Serializable.class, Cloneable.class, RandomAccess.class);
        Object mock = mockMaker.createMock(settings, defaultHandler);
        assertTrue(mock instanceof Runnable);
        assertTrue(mock instanceof Serializable);
        assertTrue(mock instanceof Cloneable);
        assertTrue(mock instanceof RandomAccess);
    }

    @Test
    public void createMock_withNullMockNameSettings() {
        // Some settings have null MockName; ensure mock maker handles it
        MockSettingsImpl<List> settings = new MockSettingsImpl<List>()
                .setTypeToMock(List.class)
                .setName(null);
        Object mock = mockMaker.createMock(settings, defaultHandler);
        assertNotNull(mock);
    }

    // -----------------------------------------------------------------------
    // Additional coverage for internal methods (if accessible)
    // -----------------------------------------------------------------------

    @Test
    public void createMock_withSerializableAndExtraInterfaces() {
        // Combination of serializable and extra interfaces might trigger bug
        MockSettingsImpl<List> settings = new MockSettingsImpl<List>()
                .setTypeToMock(List.class)
                .serializable()
                .extraInterfaces(Runnable.class);
        Object mock = mockMaker.createMock(settings, defaultHandler);
        assertTrue(mock instanceof List);
        assertTrue(mock instanceof Serializable);
        assertTrue(mock instanceof Runnable);
    }

    @Test
    public void createMock_withSerializableModeAcrossClassLoaders() {
        // This serialization mode is often problematic
        MockSettingsImpl<List> settings = new MockSettingsImpl<List>()
                .setTypeToMock(List.class)
                .serializable(SerializableMode.ACROSS_CLASSLOADERS);
        Object mock = mockMaker.createMock(settings, defaultHandler);
        assertNotNull(mock);
        // Serialize and deserialize across classloaders? Not easy in unit test, but at least creation works
    }

    @Test
    public void createMock_forThreadSafety() {
        // Simple test to ensure no immediate concurrency issues
        MockCreationSettings<Map> settings = new MockSettingsImpl<Map>()
                .setTypeToMock(Map.class);
        Map mock = (Map) mockMaker.createMock(settings, defaultHandler);
        // Basic operations should not throw
        mock.size();
        mock.isEmpty();
    }
}