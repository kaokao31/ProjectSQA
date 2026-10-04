package org.mockito.internal.configuration.injection.filter;

import org.junit.Test;
import org.mockito.internal.util.reflection.ParameterizedConstructorInstantiator;
import org.mockito.internal.util.reflection.PropertyAndSetterInjection;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FinalMockCandidateFilterTest {

    private static class SampleClass {
        public Dependency dependency;
    }

    private static class Dependency {
    }

    @Test
    public void testFilter_EmptyMocks() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        Collection<Object> mocks = Collections.emptyList();
        Field field = SampleClass.class.getField("dependency");
        Object fieldInstance = new SampleClass();

        OngoingInjecter ongoingInjecter = filter.filter(mocks, field, fieldInstance);

        assertNotNull(ongoingInjecter);
        // It returns a OngoingInjecter that does nothing or delegates properly
        ongoingInjecter.thenInject();
    }

    @Test
    public void testFilter_SingleMockNoMatch() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        Collection<Object> mocks = Collections.singletonList("someStringMock");
        Field field = SampleClass.class.getField("dependency");
        Object fieldInstance = new SampleClass();

        OngoingInjecter ongoingInjecter = filter.filter(mocks, field, fieldInstance);

        assertNotNull(ongoingInjecter);
        ongoingInjecter.thenInject();
    }

    @Test
    public void testFilter_SingleMockMatch() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        Dependency mockDependency = new Dependency();
        Collection<Object> mocks = Collections.singletonList(mockDependency);
        Field field = SampleClass.class.getField("dependency");
        SampleClass fieldInstance = new SampleClass();

        OngoingInjecter ongoingInjecter = filter.filter(mocks, field, fieldInstance);

        assertNotNull(ongoingInjecter);
        ongoingInjecter.thenInject();

        assertEquals(mockDependency, fieldInstance.dependency);
    }

    @Test
    public void testFilter_MultipleMocksNoMatch() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        List<Object> mocks = new ArrayList<>();
        mocks.add("mock1");
        mocks.add("mock2");
        Field field = SampleClass.class.getField("dependency");
        Object fieldInstance = new SampleClass();

        OngoingInjecter ongoingInjecter = filter.filter(mocks, field, fieldInstance);

        assertNotNull(ongoingInjecter);
        ongoingInjecter.thenInject();
    }

    @Test
    public void testFilter_MultipleMocksOneMatch() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        Dependency mockDependency = new Dependency();
        List<Object> mocks = new ArrayList<>();
        mocks.add("mock1");
        mocks.add(mockDependency);
        Field field = SampleClass.class.getField("dependency");
        SampleClass fieldInstance = new SampleClass();

        OngoingInjecter ongoingInjecter = filter.filter(mocks, field, fieldInstance);

        assertNotNull(ongoingInjecter);
        ongoingInjecter.thenInject();

        assertEquals(mockDependency, fieldInstance.dependency);
    }

    @Test
    public void testFilter_MultipleMocksMultipleMatches() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        Dependency mockDependency1 = new Dependency();
        Dependency mockDependency2 = new Dependency();
        List<Object> mocks = new ArrayList<>();
        mocks.add(mockDependency1);
        mocks.add(mockDependency2);
        Field field = SampleClass.class.getField("dependency");
        SampleClass fieldInstance = new SampleClass();

        OngoingInjecter ongoingInjecter = filter.filter(mocks, field, fieldInstance);

        assertNotNull(ongoingInjecter);
        ongoingInjecter.thenInject();
    }
}