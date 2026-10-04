package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.POJOPropertiesCollector;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Test suite for POJOPropertiesCollector, targeting high coverage and fault detection.
 * Designed to trigger known issues (e.g., Defects4J bug 73: @JsonUnwrapped from superclass).
 */
public class POJOPropertiesCollectorTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    // Helper to collect properties from a class
    private POJOPropertiesCollector collectProperties(Class<?> cls) {
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(mapper.constructType(cls));
        // POJOPropertiesCollector is package-private; we use the factory method from BeanDescription
        // Actually, we need to access it via introspection or use the public API.
        // Since POJOPropertiesCollector is not public, we'll use the BeanDescription's method.
        // But BeanDescription does not expose POJOPropertiesCollector directly.
        // Instead, we can use ObjectMapper's _propertyCollector()? That's internal.
        // For testing, we'll rely on the fact that POJOPropertiesCollector is used internally.
        // We'll create a simple test that verifies properties via serialization.
        // However, to directly test POJOPropertiesCollector, we need to instantiate it.
        // It has a protected constructor? Actually, it's package-private.
        // We'll use reflection to create an instance for testing.
        // But that's fragile. Instead, we'll test indirectly through ObjectMapper.
        // For the purpose of this test suite, we'll test the behavior that POJOPropertiesCollector influences.
        // We'll use ObjectMapper to serialize and check that properties are present.
        // This is a pragmatic approach given the visibility constraints.
        // We'll still name the test class POJOPropertiesCollectorTest.
        // We'll create a helper that uses the internal API via reflection if needed.
        // For simplicity, we'll test via serialization.
        // But the user expects direct testing. Let's use reflection to access the package-private class.
        // We'll assume the test class is in the same package, so we can access it directly.
        // Since we are in the same package, we can instantiate POJOPropertiesCollector.
        // Its constructor: POJOPropertiesCollector(SerializationConfig, BeanDescription, boolean)
        // We'll use the config from mapper.
        com.fasterxml.jackson.databind.SerializationConfig config = mapper.getSerializationConfig();
        // We need a BeanDescription. We can get it from mapper's introspect.
        // But introspect returns a BasicBeanDescription which is a subclass.
        // We'll cast.
        BasicBeanDescription beanDesc2 = (BasicBeanDescription) mapper.getSerializationConfig().introspect(mapper.constructType(cls));
        // Now create POJOPropertiesCollector
        POJOPropertiesCollector collector = new POJOPropertiesCollector(config, beanDesc2, false);
        collector.collect();
        return collector;
    }

    // Simple test class
    static class SimpleBean {
        public String name;
        public int value;
    }

    @Test
    public void testCollectPropertiesSimple() {
        POJOPropertiesCollector collector = collectProperties(SimpleBean.class);
        Collection<POJOPropertyBuilder> props = collector.getProperties();
        assertNotNull(props);
        assertEquals(2, props.size());
        assertTrue(props.stream().anyMatch(p -> p.getName().equals("name")));
        assertTrue(props.stream().anyMatch(p -> p.getName().equals("value")));
    }

    // Test with superclass
    static class BaseBean {
        public String baseProp;
    }

    static class DerivedBean extends BaseBean {
        public String derivedProp;
    }

    @Test
    public void testCollectPropertiesWithSuperclass() {
        POJOPropertiesCollector collector = collectProperties(DerivedBean.class);
        Collection<POJOPropertyBuilder> props = collector.getProperties();
        assertEquals(2, props.size());
        assertTrue(props.stream().anyMatch(p -> p.getName().equals("baseProp")));
        assertTrue(props.stream().anyMatch(p -> p.getName().equals("derivedProp")));
    }

    // Test with @JsonUnwrapped on superclass (bug 73 scenario)
    static class UnwrappedBase {
        @JsonUnwrapped
        public Inner inner;
    }

    static class Inner {
        public String innerProp1;
        public int innerProp2;
    }

    static class UnwrappedDerived extends UnwrappedBase {
        public String derivedProp;
    }

    @Test
    public void testCollectPropertiesWithJsonUnwrappedFromSuperclass() {
        POJOPropertiesCollector collector = collectProperties(UnwrappedDerived.class);
        Collection<POJOPropertyBuilder> props = collector.getProperties();
        // Should include properties from Inner (innerProp1, innerProp2) and derivedProp
        // But note: @JsonUnwrapped flattens the inner properties, so they become properties of the derived class.
        // The collector should collect them.
        // Bug 73: properties from superclass with @JsonUnwrapped were not collected.
        // So we expect 3 properties: innerProp1, innerProp2, derivedProp
        assertEquals(3, props.size());
        assertTrue(props.stream().anyMatch(p -> p.getName().equals("innerProp1")));
        assertTrue(props.stream().anyMatch(p -> p.getName().equals("innerProp2")));
        assertTrue(props.stream().anyMatch(p -> p.getName().equals("derivedProp")));
    }

    // Test with ignored properties
    static class IgnoredBean {
        public String visible;
        @com.fasterxml.jackson.annotation.JsonIgnore
        public String hidden;
    }

    @Test
    public void testCollectPropertiesWithIgnored() {
        POJOPropertiesCollector collector = collectProperties(IgnoredBean.class);
        Collection<POJOPropertyBuilder> props = collector.getProperties();
        // Should only include visible, not hidden
        assertEquals(1, props.size());
        assertTrue(props.stream().anyMatch(p -> p.getName().equals("visible")));
        assertFalse(props.stream().anyMatch(p -> p.getName().equals("hidden")));
    }

    // Test with no properties
    static class EmptyBean {
        // no fields
    }

    @Test
    public void testCollectPropertiesEmpty() {
        POJOPropertiesCollector collector = collectProperties(EmptyBean.class);
        Collection<POJOPropertyBuilder> props = collector.getProperties();
        assertNotNull(props);
        assertTrue(props.isEmpty());
    }

    // Test with getter-only property
    static class GetterBean {
        private int x;
        public int getX() { return x; }
    }

    @Test
    public void testCollectPropertiesFromGetter() {
        POJOPropertiesCollector collector = collectProperties(GetterBean.class);
        Collection<POJOPropertyBuilder> props = collector.getProperties();
        assertEquals(1, props.size());
        assertTrue(props.stream().anyMatch(p -> p.getName().equals("x")));
    }

    // Test with field and getter (should be merged)
    static class FieldAndGetterBean {
        public int y;
        public int getY() { return y; }
    }

    @Test
    public void testCollectPropertiesFieldAndGetter() {
        POJOPropertiesCollector collector = collectProperties(FieldAndGetterBean.class);
        Collection<POJOPropertyBuilder> props = collector.getProperties();
        assertEquals(1, props.size());
        POJOPropertyBuilder prop = props.iterator().next();
        assertTrue(prop.hasField());
        assertTrue(prop.hasGetter());
    }

    // Edge case: null BeanDescription? Not possible via normal API, but we can test with null config?
    // We'll skip due to complexity.

    // Test with multiple levels of inheritance and @JsonUnwrapped
    static class Level1 {
        @JsonUnwrapped
        public Level2 l2;
    }

    static class Level2 {
        public String l2prop;
    }

    static class Level3 extends Level1 {
        public String l3prop;
    }

    @Test
    public void testCollectPropertiesMultiLevelUnwrapped() {
        POJOPropertiesCollector collector = collectProperties(Level3.class);
        Collection<POJOPropertyBuilder> props = collector.getProperties();
        // Should have l2prop and l3prop
        assertEquals(2, props.size());
        assertTrue(props.stream().anyMatch(p -> p.getName().equals("l2prop")));
        assertTrue(props.stream().anyMatch(p -> p.getName().equals("l3prop")));
    }

    // Test with @JsonUnwrapped on a field that is also a property in the same class
    static class SelfUnwrapped {
        @JsonUnwrapped
        public SelfUnwrapped self; // recursive, but should be handled
        public String other;
    }

    @Test
    public void testCollectPropertiesSelfUnwrapped() {
        // This may cause infinite recursion, but the collector should handle it.
        // We'll just test that it doesn't throw and returns at least 'other'.
        POJOPropertiesCollector collector = collectProperties(SelfUnwrapped.class);
        Collection<POJOPropertyBuilder> props = collector.getProperties();
        assertNotNull(props);
        // It should at least have 'other', and maybe 'self' but unwrapped might cause issues.
        // We'll just check that it doesn't crash and returns something.
        assertTrue(props.size() >= 1);
        assertTrue(props.stream().anyMatch(p -> p.getName().equals("other")));
    }
}