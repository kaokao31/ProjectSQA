package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for BeanDeserializer.
 * Designed to achieve high code coverage and detect faults,
 * including the cyclic reference bug (Defects4J bug 50).
 */
public class BeanDeserializerTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    // --- Basic deserialization tests ---

    @Test
    public void testSimpleBean() throws IOException {
        String json = "{\"name\":\"test\",\"value\":42}";
        SimpleBean bean = mapper.readValue(json, SimpleBean.class);
        assertEquals("test", bean.getName());
        assertEquals(42, bean.getValue());
    }

    @Test
    public void testBeanWithNullField() throws IOException {
        String json = "{\"name\":null,\"value\":0}";
        SimpleBean bean = mapper.readValue(json, SimpleBean.class);
        assertNull(bean.getName());
        assertEquals(0, bean.getValue());
    }

    @Test
    public void testBeanWithMissingField() throws IOException {
        String json = "{\"name\":\"only\"}";
        SimpleBean bean = mapper.readValue(json, SimpleBean.class);
        assertEquals("only", bean.getName());
        assertEquals(0, bean.getValue()); // default int
    }

    @Test(expected = UnrecognizedPropertyException.class)
    public void testBeanWithUnknownProperty() throws IOException {
        String json = "{\"name\":\"test\",\"unknown\":\"value\"}";
        mapper.readValue(json, SimpleBean.class);
    }

    // --- Edge cases: empty, null, boundary ---

    @Test
    public void testEmptyBean() throws IOException {
        String json = "{}";
        SimpleBean bean = mapper.readValue(json, SimpleBean.class);
        assertNull(bean.getName());
        assertEquals(0, bean.getValue());
    }

    @Test(expected = IOException.class)
    public void testNullInput() throws IOException {
        mapper.readValue((String) null, SimpleBean.class);
    }

    @Test
    public void testBeanWithMaxIntValue() throws IOException {
        String json = "{\"value\":2147483647}";
        SimpleBean bean = mapper.readValue(json, SimpleBean.class);
        assertEquals(Integer.MAX_VALUE, bean.getValue());
    }

    @Test
    public void testBeanWithMinIntValue() throws IOException {
        String json = "{\"value\":-2147483648}";
        SimpleBean bean = mapper.readValue(json, SimpleBean.class);
        assertEquals(Integer.MIN_VALUE, bean.getValue());
    }

    // --- Tests for cyclic references (bug 50) ---

    @Test
    public void testCyclicReferenceWithManagedBackReference() throws IOException {
        // Create a parent with a child that references back to parent
        String json = "{\"id\":1,\"children\":[{\"id\":2,\"parent\":{\"id\":1}}]}";
        Parent parent = mapper.readValue(json, Parent.class);
        assertNotNull(parent);
        assertEquals(1, parent.getId());
        assertNotNull(parent.getChildren());
        assertEquals(1, parent.getChildren().size());
        Child child = parent.getChildren().get(0);
        assertEquals(2, child.getId());
        // The back reference should point to the same parent object
        assertSame(parent, child.getParent());
    }

    @Test
    public void testCyclicReferenceWithMultipleChildren() throws IOException {
        String json = "{\"id\":1,\"children\":[{\"id\":2,\"parent\":{\"id\":1}},{\"id\":3,\"parent\":{\"id\":1}}]}";
        Parent parent = mapper.readValue(json, Parent.class);
        assertEquals(2, parent.getChildren().size());
        for (Child child : parent.getChildren()) {
            assertSame(parent, child.getParent());
        }
    }

    @Test
    public void testCyclicReferenceSelfLoop() throws IOException {
        // A node that references itself (if allowed)
        String json = "{\"id\":1,\"self\":{\"id\":1}}";
        SelfRefNode node = mapper.readValue(json, SelfRefNode.class);
        assertNotNull(node);
        assertEquals(1, node.getId());
        // The self reference should be the same object (if cyclic handled)
        // This may cause StackOverflow if not handled; test expects success
        assertSame(node, node.getSelf());
    }

    // --- Tests for nested beans and collections ---

    @Test
    public void testNestedBean() throws IOException {
        String json = "{\"inner\":{\"name\":\"inner\",\"value\":10}}";
        OuterBean outer = mapper.readValue(json, OuterBean.class);
        assertNotNull(outer.getInner());
        assertEquals("inner", outer.getInner().getName());
        assertEquals(10, outer.getInner().getValue());
    }

    @Test
    public void testBeanWithList() throws IOException {
        String json = "{\"items\":[\"a\",\"b\",\"c\"]}";
        BeanWithList bean = mapper.readValue(json, BeanWithList.class);
        assertNotNull(bean.getItems());
        assertEquals(3, bean.getItems().size());
        assertEquals("a", bean.getItems().get(0));
    }

    @Test
    public void testBeanWithEmptyList() throws IOException {
        String json = "{\"items\":[]}";
        BeanWithList bean = mapper.readValue(json, BeanWithList.class);
        assertNotNull(bean.getItems());
        assertTrue(bean.getItems().isEmpty());
    }

    // --- Exception handling tests ---

    @Test(expected = IOException.class)
    public void testMalformedJson() throws IOException {
        String json = "{invalid}";
        mapper.readValue(json, SimpleBean.class);
    }

    @Test(expected = IOException.class)
    public void testIncompleteJson() throws IOException {
        String json = "{\"name\":\"test\"";
        mapper.readValue(json, SimpleBean.class);
    }

    // --- Helper classes for testing ---

    static class SimpleBean {
        private String name;
        private int value;

        public SimpleBean() {}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
    }

    static class OuterBean {
        private SimpleBean inner;

        public OuterBean() {}

        public SimpleBean getInner() { return inner; }
        public void setInner(SimpleBean inner) { this.inner = inner; }
    }

    static class BeanWithList {
        private List<String> items;

        public BeanWithList() {}

        public List<String> getItems() { return items; }
        public void setItems(List<String> items) { this.items = items; }
    }

    // Classes for cyclic reference testing (bug 50)
    static class Parent {
        private int id;
        private List<Child> children;

        public Parent() {}

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        @JsonManagedReference
        public List<Child> getChildren() { return children; }
        public void setChildren(List<Child> children) { this.children = children; }
    }

    static class Child {
        private int id;
        private Parent parent;

        public Child() {}

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        @JsonBackReference
        public Parent getParent() { return parent; }
        public void setParent(Parent parent) { this.parent = parent; }
    }

    // Self-referential node (may cause StackOverflow if not handled)
    static class SelfRefNode {
        private int id;
        private SelfRefNode self;

        public SelfRefNode() {}

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public SelfRefNode getSelf() { return self; }
        public void setSelf(SelfRefNode self) { this.self = self; }
    }
}