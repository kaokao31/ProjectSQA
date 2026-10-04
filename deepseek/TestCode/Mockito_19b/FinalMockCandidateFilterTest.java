package org.mockito.internal.configuration.injection.filter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.configuration.injection.filter.FinalMockCandidateFilter;
import org.mockito.internal.configuration.injection.filter.MockCandidateFilter;
import org.mockito.internal.configuration.injection.filter.TypeBasedCandidateFilter;
import org.mockito.internal.configuration.injection.filter.NameBasedCandidateFilter;
import org.mockito.internal.util.reflection.BeanPropertySetter;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.invocation.MockHandler;
import org.mockito.mock.MockCreationValidator;
import org.mockito.plugins.MockMaker;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class FinalMockCandidateFilterTest {

    private FinalMockCandidateFilter filter;
    private MockCandidateFilter mockCandidateFilter;
    private Field field;
    private Object fieldInstance;
    private Object mockCandidate;
    private Object nonMockCandidate;

    @Before
    public void setUp() throws Exception {
        filter = new FinalMockCandidateFilter();
        mockCandidateFilter = mock(MockCandidateFilter.class);
        field = SomeClass.class.getDeclaredField("someField");
        fieldInstance = new SomeClass();
        mockCandidate = mock(Object.class);
        nonMockCandidate = new Object();
    }

    @Test
    public void shouldReturnFalseWhenCandidatesListIsNull() throws Exception {
        assertFalse(filter.filterCandidate(null, field, fieldInstance, mockCandidateFilter));
    }

    @Test
    public void shouldReturnFalseWhenCandidatesListIsEmpty() throws Exception {
        assertFalse(filter.filterCandidate(new ArrayList<>(), field, fieldInstance, mockCandidateFilter));
    }

    @Test
    public void shouldReturnFalseWhenOnlyCandidateIsNotAMock() throws Exception {
        List<Object> candidates = Collections.singletonList(nonMockCandidate);
        assertFalse(filter.filterCandidate(candidates, field, fieldInstance, mockCandidateFilter));
    }

    @Test
    public void shouldReturnTrueWhenOnlyCandidateIsAMock() throws Exception {
        List<Object> candidates = Collections.singletonList(mockCandidate);
        assertTrue(filter.filterCandidate(candidates, field, fieldInstance, mockCandidateFilter));
    }

    @Test
    public void shouldReturnTrueWhenMultipleCandidatesAndOneIsAMock() throws Exception {
        List<Object> candidates = Arrays.asList(nonMockCandidate, mockCandidate);
        assertTrue(filter.filterCandidate(candidates, field, fieldInstance, mockCandidateFilter));
    }

    @Test
    public void shouldReturnFalseWhenMultipleCandidatesAndNoneIsAMock() throws Exception {
        List<Object> candidates = Arrays.asList(new Object(), new Object());
        assertFalse(filter.filterCandidate(candidates, field, fieldInstance, mockCandidateFilter));
    }

    @Test
    public void shouldReturnFalseWhenMultipleCandidatesAndAllAreMocks() throws Exception {
        List<Object> candidates = Arrays.asList(mock(Object.class), mock(Object.class));
        assertFalse(filter.filterCandidate(candidates, field, fieldInstance, mockCandidateFilter));
    }

    @Test
    public void shouldReturnFalseWhenFieldIsNull() throws Exception {
        List<Object> candidates = Collections.singletonList(mockCandidate);
        assertFalse(filter.filterCandidate(candidates, null, fieldInstance, mockCandidateFilter));
    }

    @Test
    public void shouldReturnFalseWhenFieldInstanceIsNull() throws Exception {
        List<Object> candidates = Collections.singletonList(mockCandidate);
        assertFalse(filter.filterCandidate(candidates, field, null, mockCandidateFilter));
    }

    @Test
    public void shouldReturnFalseWhenMockCandidateFilterIsNull() throws Exception {
        List<Object> candidates = Collections.singletonList(mockCandidate);
        assertFalse(filter.filterCandidate(candidates, field, fieldInstance, null));
    }

    @Test
    public void shouldInvokeMockCandidateFilterWhenOnlyCandidateIsAMock() throws Exception {
        List<Object> candidates = Collections.singletonList(mockCandidate);
        when(mockCandidateFilter.filterCandidate(anyList(), any(Field.class), any(), any(MockCandidateFilter.class)))
                .thenReturn(true);
        assertTrue(filter.filterCandidate(candidates, field, fieldInstance, mockCandidateFilter));
        verify(mockCandidateFilter).filterCandidate(candidates, field, fieldInstance, filter);
    }

    @Test
    public void shouldNotInvokeMockCandidateFilterWhenNoMockCandidate() throws Exception {
        List<Object> candidates = Collections.singletonList(nonMockCandidate);
        filter.filterCandidate(candidates, field, fieldInstance, mockCandidateFilter);
        verify(mockCandidateFilter, never()).filterCandidate(anyList(), any(Field.class), any(), any(MockCandidateFilter.class));
    }

    @Test
    public void shouldSetFieldWhenOnlyCandidateIsAMock() throws Exception {
        List<Object> candidates = Collections.singletonList(mockCandidate);
        assertTrue(filter.filterCandidate(candidates, field, fieldInstance, mockCandidateFilter));
        assertEquals(mockCandidate, field.get(fieldInstance));
    }

    @Test
    public void shouldNotSetFieldWhenCandidateIsNotAMock() throws Exception {
        List<Object> candidates = Collections.singletonList(nonMockCandidate);
        assertFalse(filter.filterCandidate(candidates, field, fieldInstance, mockCandidateFilter));
        assertNull(field.get(fieldInstance));
    }

    @Test
    public void shouldSetFieldWhenMultipleCandidatesAndOneIsAMock() throws Exception {
        List<Object> candidates = Arrays.asList(nonMockCandidate, mockCandidate);
        assertTrue(filter.filterCandidate(candidates, field, fieldInstance, mockCandidateFilter));
        assertEquals(mockCandidate, field.get(fieldInstance));
    }

    @Test
    public void shouldNotSetFieldWhenMultipleCandidatesAndNoneIsAMock() throws Exception {
        List<Object> candidates = Arrays.asList(new Object(), new Object());
        assertFalse(filter.filterCandidate(candidates, field, fieldInstance, mockCandidateFilter));
        assertNull(field.get(fieldInstance));
    }

    // Helper class for reflection
    private static class SomeClass {
        private Object someField;
    }
}