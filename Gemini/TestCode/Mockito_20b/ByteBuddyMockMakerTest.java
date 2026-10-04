package org.mockito.internal.creation.bytebuddy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockSettings;
import org.mockito.internal.creation.settings.CreationSettings;
import org.mockito.invocation.MockHandler;
import org.mockito.mock.MockCreationSettings;

import static org.junit.Assert.*;

public class ByteBuddyMockMakerTest {

    private ByteBuddyMockMaker mockMaker;

    @Before
    public void setUp() {
        mockMaker = new ByteBuddyMockMaker();
    }

    @After
    public void tearDown() {
        mockMaker = null;
    }

    @Test
    public void testCreateMockAndGetHandler() {
        MockSettings settings = new CreationSettings<Runnable>();
        settings.typeToMock(Runnable.class);

        MockHandler<?> mockHandler = new MockHandler<Runnable>() {
            @Override
            public Object handle(org.mockito.invocation.InvocationOnMock invocation) throws Throwable {
                return null;
            }

            @Override
            public MockCreationSettings getMockSettings() {
                return (MockCreationSettings) settings;
            }
        };

        Runnable mock = mockMaker.createMock((MockCreationSettings<Runnable>) settings, (MockHandler) mockHandler);
        assertNotNull(mock);

        MockHandler<?> retrievedHandler = mockMaker.getHandler(mock);
        assertNotNull(retrievedHandler);
        assertSame(mockHandler, retrievedHandler);
    }

    @Test
    public void testResetMock() {
        MockSettings settings = new CreationSettings<Runnable>();
        settings.typeToMock(Runnable.class);

        MockHandler<?> mockHandler = new MockHandler<Runnable>() {
            @Override
            public Object handle(org.mockito.invocation.InvocationOnMock invocation) throws Throwable {
                return null;
            }

            @Override
            public MockCreationSettings getMockSettings() {
                return (MockCreationSettings) settings;
            }
        };

        Runnable mock = mockMaker.createMock((MockCreationSettings<Runnable>) settings, (MockHandler) mockHandler);
        assertNotNull(mock);

        MockHandler<?> newHandler = new MockHandler<Runnable>() {
            @Override
            public Object handle(org.mockito.invocation.InvocationOnMock invocation) throws Throwable {
                return "reset";
            }

            @Override
            public MockCreationSettings getMockSettings() {
                return (MockCreationSettings) settings;
            }
        };

        mockMaker.resetMock(mock, (MockHandler) newHandler, (MockCreationSettings) settings);

        MockHandler<?> retrievedHandler = mockMaker.getHandler(mock);
        assertSame(newHandler, retrievedHandler);
    }

    @Test
    public void testGetHandlerForNonMock() {
        Object nonMock = new Object();
        MockHandler<?> handler = mockMaker.getHandler(nonMock);
        assertNull(handler);
    }
}