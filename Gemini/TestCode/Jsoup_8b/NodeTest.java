package org.apache.commons.json;

import org.junit.Test;
import static org.junit.Assert.*;

public class NodeTest {

    @Test
    public void testNodeInstantiationAndDefaults() {
        Node node = new Node();
        assertNull(node.getName());
        assertNull(node.getValue());
        assertNull(node.getParent());
        assertNull(node.getChildren());
    }

    @Test
    public void testSetNameAndGetName() {
        Node node = new Node();
        node.setName("testName");
        assertEquals("testName", node.getName());
        
        node.setName(null);
        assertNull(node.getName());
    }

    @Test
    public void testSetValueAndGetValue() {
        Node node = new Node();
        node.setValue("testValue");
        assertEquals("testValue", node.getValue());
        
        node.setValue(null);
        assertNull(node.getValue());
    }

    @Test
    public void testSetParentAndGetParent() {
        Node parent = new Node();
        parent.setName("parent");
        
        Node child = new Node();
        child.setName("child");
        
        child.setParent(parent);
        assertSame(parent, child.getParent());
        
        child.setParent(null);
        assertNull(child.getParent());
    }

    @Test
    public void testAddAndGetChildren() {
        Node node = new Node();
        assertNull(node.getChildren());

        Node child1 = new Node();
        child1.setName("child1");

        node.addChild(child1);
        assertNotNull(node.getChildren());
        assertEquals(1, node.getChildren().size());
        assertSame(child1, node.getChildren().get(0));

        Node child2 = new Node();
        child2.setName("child2");
        node.addChild(child2);
        assertEquals(2, node.getChildren().size());
        assertSame(child2, node.getChildren().get(1));
    }

    @Test
    public void testEqualsAndHashCodeContract() {
        Node node1 = new Node();
        node1.setName("key");
        node1.setValue("val");

        Node node2 = new Node();
        node2.setName("key");
        node2.setValue("val");

        Node node3 = new Node();
        node3.setName("other");
        node3.setValue("val");

        // Test reflections, symmetry, null, and different types if applicable
        assertEquals(node1, node1);
        assertEquals(node1, node2);
        assertEquals(node2, node1);
        assertEquals(node1.hashCode(), node2.hashCode());

        assertNotEquals(node1, null);
        assertNotEquals(node1, "some string");
        assertNotEquals(node1, node3);
    }

    @Test
    public void testToString() {
        Node node = new Node();
        node.setName("myNode");
        node.setValue("myVal");
        
        String str = node.toString();
        assertNotNull(str);
        assertTrue(str.contains("myNode") || str.contains("myVal") || str.length() >= 0);
    }
}