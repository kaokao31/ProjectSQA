package org.apache.commons.jxpath.ri.axes;

import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for AttributeContext.
 * Targeted to expose the fault in JxPath-18 (NPE when parent context is null
 * or when attribute iteration fails due to improper handling).
 */
public class AttributeContextTest {

    private EvalContext parentContext;

    @Before
    public void setUp() {
        parentContext = new EvalContext(null) {
            @Override
            public Pointer getSingleNodePointer() {
                return null;
            }

            @Override
            public boolean nextNode() {
                return false;
            }

            @Override
            public boolean nextSet() {
                return false;
            }

            @Override
            public boolean setPosition(int position) {
                return false;
            }

            @Override
            public NodePointer getCurrentNodePointer() {
                return null;
            }
        };
    }

    // ========== Constructor Nullability ==========

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullParent() {
        new AttributeContext(null, new NodeNameTest(new QName("attr")));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullNodeTest() {
        new AttributeContext(parentContext, null);
    }

    @Test
    public void testConstructorWithNonNullArgs() {
        AttributeContext ctx = new AttributeContext(parentContext,
                new NodeNameTest(new QName("attr")));
        assertNotNull("AttributeContext should be created", ctx);
    }

    // ========== nextNode behavior with no attributes ==========

    @Test
    public void testNextNodeReturnsFalseWhenNoAttributes() {
        AttributeContext ctx = new AttributeContext(parentContext,
                new NodeNameTest(new QName("attr")));
        assertFalse("nextNode() should return false when no attributes",
                ctx.nextNode());
    }

    @Test
    public void testNextNodeReturnsFalseRepeatedly() {
        AttributeContext ctx = new AttributeContext(parentContext,
                new NodeNameTest(new QName("attr")));
        assertFalse("First call", ctx.nextNode());
        assertFalse("Second call", ctx.nextNode());
    }

    // ========== setPosition behavior ==========

    @Test
    public void testSetPositionInvalid() {
        AttributeContext ctx = new AttributeContext(parentContext,
                new NodeNameTest(new QName("attr")));
        assertFalse("setPosition(0) should return false", ctx.setPosition(0));
        assertFalse("setPosition(1) should return false", ctx.setPosition(1));
        assertFalse("setPosition(-1) should return false", ctx.setPosition(-1));
    }

    // ========== getCurrentNodePointer ==========

    @Test
    public void testGetCurrentNodePointerBeforeIteration() {
        AttributeContext ctx = new AttributeContext(parentContext,
                new NodeNameTest(new QName("attr")));
        assertNull("getCurrentNodePointer() should be null before any iteration",
                ctx.getCurrentNodePointer());
    }

    @Test
    public void testGetCurrentNodePointerAfterFailedNext() {
        AttributeContext ctx = new AttributeContext(parentContext,
                new NodeNameTest(new QName("attr")));
        ctx.nextNode();
        assertNull("getCurrentNodePointer() should remain null after failed nextNode()",
                ctx.getCurrentNodePointer());
    }

    // ========== Interaction with parent that provides a node with attributes ==========

    @Test
    public void testNextNodeWithParentProvidingAttributes() {
        // Create a parent context that returns a node that has attributes.
        // We'll use a custom NodePointer that has simple attributes.
        final List<NodePointer> attributes = new ArrayList<>();
        attributes.add(new NodePointer() {
            @Override
            public QName getName() {
                return new QName("attr1");
            }

            @Override
            public Object getValue() {
                return "value1";
            }

            @Override
            public Object getBaseValue() {
                return "value1";
            }

            @Override
            public Object getImmediateNode() {
                return null;
            }

            @Override
            public boolean isActual() {
                return true;
            }

            @Override
            public boolean isCollection() {
                return false;
            }

            @Override
            public int getLength() {
                return 1;
            }

            @Override
            public Object getImmediateValue() {
                return null;
            }

            @Override
            public boolean isLeaf() {
                return false;
            }

            @Override
            public int compareChildNodePointers(NodePointer object1, NodePointer object2) {
                return 0;
            }

            @Override
            public String asPath() {
                return "";
            }

            @Override
            public NodePointer getParent() {
                return null;
            }

            @Override
            public boolean isRoot() {
                return false;
            }
        });

        EvalContext parentWithAttributes = new EvalContext(null) {
            private boolean done = false;

            @Override
            public Pointer getSingleNodePointer() {
                return null;
            }

            @Override
            public boolean nextNode() {
                if (!done) {
                    done = true;
                    return true;
                }
                return false;
            }

            @Override
            public boolean nextSet() {
                return false;
            }

            @Override
            public boolean setPosition(int position) {
                return position == 1;
            }

            @Override
            public NodePointer getCurrentNodePointer() {
                // Return a NodePointer whose attributes() returns our attributes list.
                return new NodePointer() {
                    @Override
                    public QName getName() {
                        return new QName("element");
                    }

                    @Override
                    public Object getValue() {
                        return null;
                    }

                    @Override
                    public Object getBaseValue() {
                        return null;
                    }

                    @Override
                    public Object getImmediateNode() {
                        return null;
                    }

                    @Override
                    public boolean isActual() {
                        return true;
                    }

                    @Override
                    public boolean isCollection() {
                        return false;
                    }

                    @Override
                    public int getLength() {
                        return 1;
                    }

                    @Override
                    public Object getImmediateValue() {
                        return null;
                    }

                    @Override
                    public boolean isLeaf() {
                        return false;
                    }

                    @Override
                    public int compareChildNodePointers(NodePointer object1, NodePointer object2) {
                        return 0;
                    }

                    @Override
                    public String asPath() {
                        return "";
                    }

                    @Override
                    public NodePointer getParent() {
                        return null;
                    }

                    @Override
                    public boolean isRoot() {
                        return false;
                    }

                    @Override
                    public Iterator<NodePointer> attributes() {
                        return attributes.iterator();
                    }
                };
            }
        };

        NodeNameTest nodeNameTest = new NodeNameTest(new QName("attr1"));
        AttributeContext ctx = new AttributeContext(parentWithAttributes, nodeNameTest);
        assertTrue("nextNode() should return true because attribute attr1 exists",
                ctx.nextNode());
        assertNotNull("getCurrentNodePointer() should not be null after successful nextNode()",
                ctx.getCurrentNodePointer());
        assertEquals("Attribute name should be attr1",
                new QName("attr1"), ctx.getCurrentNodePointer().getName());
    }

    // ========== Edge case: No matching attribute among many ==========

    @Test
    public void testNextNodeWithNonMatchingAttribute() {
        final List<NodePointer> attributes = new ArrayList<>();
        attributes.add(new NodePointer() {
            @Override
            public QName getName() {
                return new QName("otherAttr");
            }

            @Override
            public Object getValue() {
                return "value";
            }

            @Override
            public Object getBaseValue() {
                return "value";
            }

            @Override
            public Object getImmediateNode() {
                return null;
            }

            @Override
            public boolean isActual() {
                return true;
            }

            @Override
            public boolean isCollection() {
                return false;
            }

            @Override
            public int getLength() {
                return 1;
            }

            @Override
            public Object getImmediateValue() {
                return null;
            }

            @Override
            public boolean isLeaf() {
                return false;
            }

            @Override
            public int compareChildNodePointers(NodePointer object1, NodePointer object2) {
                return 0;
            }

            @Override
            public String asPath() {
                return "";
            }

            @Override
            public NodePointer getParent() {
                return null;
            }

            @Override
            public boolean isRoot() {
                return false;
            }
        });

        EvalContext parent = new EvalContext(null) {
            private boolean done = false;

            @Override
            public Pointer getSingleNodePointer() {
                return null;
            }

            @Override
            public boolean nextNode() {
                if (!done) {
                    done = true;
                    return true;
                }
                return false;
            }

            @Override
            public boolean nextSet() {
                return false;
            }

            @Override
            public boolean setPosition(int position) {
                return position == 1;
            }

            @Override
            public NodePointer getCurrentNodePointer() {
                return new NodePointer() {
                    @Override
                    public QName getName() {
                        return new QName("element");
                    }

                    @Override
                    public Object getValue() {
                        return null;
                    }

                    @Override
                    public Object getBaseValue() {
                        return null;
                    }

                    @Override
                    public Object getImmediateNode() {
                        return null;
                    }

                    @Override
                    public boolean isActual() {
                        return true;
                    }

                    @Override
                    public boolean isCollection() {
                        return false;
                    }

                    @Override
                    public int getLength() {
                        return 1;
                    }

                    @Override
                    public Object getImmediateValue() {
                        return null;
                    }

                    @Override
                    public boolean isLeaf() {
                        return false;
                    }

                    @Override
                    public int compareChildNodePointers(NodePointer object1, NodePointer object2) {
                        return 0;
                    }

                    @Override
                    public String asPath() {
                        return "";
                    }

                    @Override
                    public NodePointer getParent() {
                        return null;
                    }

                    @Override
                    public boolean isRoot() {
                        return false;
                    }

                    @Override
                    public Iterator<NodePointer> attributes() {
                        return attributes.iterator();
                    }
                };
            }
        };

        NodeNameTest nodeNameTest = new NodeNameTest(new QName("nonexistent"));
        AttributeContext ctx = new AttributeContext(parent, nodeNameTest);
        // Even though parent provides a node, no attribute matches the test.
        assertFalse("nextNode() should return false because no attribute matches",
                ctx.nextNode());
        assertNull("getCurrentNodePointer() should be null",
                ctx.getCurrentNodePointer());
    }
}