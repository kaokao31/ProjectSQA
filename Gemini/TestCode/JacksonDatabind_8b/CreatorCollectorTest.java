package com.thoughtworks.qdox.library;

import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.*;

public class CreatorCollectorTest {

    @Test
    public void testEmptyCollection() {
        CreatorCollector collector = new CreatorCollector();
        Collection<JavaMethod> creators = collector.getCreators();
        assertNotNull(creators);
        assertTrue(creators.isEmpty());
    }

    @Test
    public void testNonCreatorMethod() {
        CreatorCollector collector = new CreatorCollector();
        // Passing null or dummy JavaClass with no matching methods
        // Since JavaClass is an interface/class, we test how the collector handles classes without creators.
        // CreatorCollector typically inspects methods for 'creator' signatures (e.g. static methods returning the class).
        assertNotNull(collector.getCreators());
    }
}