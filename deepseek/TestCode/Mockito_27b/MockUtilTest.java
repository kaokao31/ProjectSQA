package org.mockito.internal.util;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockSettings;
import org.mockito.Mockito;
import org.mockito.internal.handler.MockHandler;
import org.mockito.internal.util.MockUtil;
import org.mockito.internal.util.MockName;

import java.util.List;

import static org.junit.Assert.*;

public class MockUtilTest {

    private MockUtil mockUtil;

    @Before
    public void setUp() {
        mockUtil = new MockUtil();
    }

    // ---------- isMock tests ----------

    @Test
    public void isMock_shouldReturnFalseForNull() {
        assertFalse(mockUtil.isMock(null));
    }

    @Test
    public void isMock_shouldReturnFalseForNonMockObject() {
        Object nonMock = new Object();
        assertFalse(mockUtil.isMock(nonMock));
    }

    @Test
    public void isMock_shouldReturnTrueForMockCreatedWithDefaultSettings() {
        List<?> mock = Mockito.mock(List.class);
        assertTrue(mockUtil.isMock(mock));
    }

    @Test
    public void isMock_shouldReturnTrueForMockCreatedWithVerboseLogging() {
        MockSettings settings = Mockito.withSettings().verboseLogging();
        List<?> mock = Mockito.mock(List.class, settings);
        assertTrue(mockUtil.isMock(mock));
    }

    // ---------- getMockHandler tests ----------

    @Test(expected = IllegalArgumentException.class)
    public void getMockHandler_shouldThrowForNull() {
        mockUtil.getMockHandler(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getMockHandler_shouldThrowForNonMock() {
        mockUtil.getMockHandler(new Object());
    }

    @Test
    public void getMockHandler_shouldReturnHandlerForDefaultMock() {
        List<?> mock = Mockito.mock(List.class);
        MockHandler<?> handler = mockUtil.getMockHandler(mock);
        assertNotNull(handler);
    }

    @Test
    public void getMockHandler_shouldReturnHandlerForVerboseLoggingMock() {
        MockSettings settings = Mockito.withSettings().verboseLogging();
        List<?> mock = Mockito.mock(List.class, settings);
        MockHandler<?> handler = mockUtil.getMockHandler(mock);
        assertNotNull(handler);
    }

    // ---------- getMockName tests ----------

    @Test(expected = IllegalArgumentException.class)
    public void getMockName_shouldThrowForNull() {
        mockUtil.getMockName(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getMockName_shouldThrowForNonMock() {
        mockUtil.getMockName(new Object());
    }

    @Test
    public void getMockName_shouldReturnNameForDefaultMock() {
        List<?> mock = Mockito.mock(List.class);
        MockName name = mockUtil.getMockName(mock);
        assertNotNull(name);
        assertTrue(name.isDefault());
    }

    @Test
    public void getMockName_shouldReturnCustomNameWhenProvided() {
        MockSettings settings = Mockito.withSettings().name("myMock");
        List<?> mock = Mockito.mock(List.class, settings);
        MockName name = mockUtil.getMockName(mock);
        assertEquals("myMock", name.toString());
    }

    // ---------- resetMock tests ----------

    @Test(expected = IllegalArgumentException.class)
    public void resetMock_shouldThrowForNull() {
        mockUtil.resetMock(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void resetMock_shouldThrowForNonMock() {
        mockUtil.resetMock(new Object());
    }

    @Test
    public void resetMock_shouldClearInvocationsOnDefaultMock() {
        List<?> mock = Mockito.mock(List.class);
        mock.hashCode(); // some invocation
        mockUtil.resetMock(mock);
        // after reset, verify no interactions
        Mockito.verifyZeroInteractions(mock);
    }

    @Test
    public void resetMock_shouldClearInvocationsOnVerboseLoggingMock() {
        MockSettings settings = Mockito.withSettings().verboseLogging();
        List<?> mock = Mockito.mock(List.class, settings);
        mock.hashCode();
        mockUtil.resetMock(mock);
        Mockito.verifyZeroInteractions(mock);
    }

    // ---------- createMock tests (if accessible) ----------
    // Note: createMock is package-private or public? In Mockito 1.x it might be public.
    // We'll test it if it's accessible.

    @Test
    public void createMock_shouldCreateMockWithDefaultSettings() {
        List<?> mock = mockUtil.createMock(List.class, Mockito.withSettings());
        assertNotNull(mock);
        assertTrue(mockUtil.isMock(mock));
    }

    @Test
    public void createMock_shouldCreateMockWithVerboseLogging() {
        MockSettings settings = Mockito.withSettings().verboseLogging();
        List<?> mock = mockUtil.createMock(List.class, settings);
        assertNotNull(mock);
        assertTrue(mockUtil.isMock(mock));
        MockHandler<?> handler = mockUtil.getMockHandler(mock);
        assertNotNull(handler);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createMock_shouldThrowForNullClass() {
        mockUtil.createMock(null, Mockito.withSettings());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createMock_shouldThrowForNullSettings() {
        mockUtil.createMock(List.class, null);
    }
}