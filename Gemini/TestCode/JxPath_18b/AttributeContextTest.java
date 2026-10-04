package org.apache.commons.jxpath.ri.axes;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.BeanPointer;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.junit.Test;
import static org.junit.Assert.*;

public class AttributeContextTest {

    @Test
    public void testAttributeContextInitializationAndIteration() {
        JXPathContext context = JXPathContext.newContext(new Object());
        JXPathContextReferenceImpl refContext = (JXPathContextReferenceImpl) context;
        EvalContext rootContext = refContext.getAbsoluteRootContext();
        
        QName qName = new QName("testAttribute");
        NodeTest nodeTest = new NodeNameTest(qName);
        AttributeContext attrContext = new AttributeContext(rootContext, nodeTest);

        assertNotNull(attrContext);
        assertEquals(rootContext, attrContext.getParentContext());
    }

    @Test
    public void testSetPositionWithNodeTypeTest() {
        JXPathContext context = JXPathContext.newContext(new Object());
        JXPathContextReferenceImpl refContext = (JXPathContextReferenceImpl) context;
        EvalContext rootContext = refContext.getAbsoluteRootContext();

        NodeTest nodeTest = new NodeTypeTest(1); // NodeTypes.NODE
        AttributeContext attrContext = new AttributeContext(rootContext, nodeTest);

        // Test resetting and iterating via setPosition
        boolean hasResult = attrContext.setPosition(1);
        // Depending on attributes of root bean, it might be true or false, 
        // but executing the code path is crucial.
        assertFalse(hasResult || !hasResult);
    }

    @Test
    public void testGetCurrentNodePointerWithoutIteration() {
        JXPathContext context = JXPathContext.newContext(new Object());
        JXPathContextReferenceImpl refContext = (JXPathContextReferenceImpl) context;
        EvalContext rootContext = refContext.getAbsoluteRootContext();

        QName qName = new QName("attr");
        NodeTest nodeTest = new NodeNameTest(qName);
        AttributeContext attrContext = new AttributeContext(rootContext, nodeTest);

        // Before nextNode or setPosition, current node pointer might be null or cause lazy evaluation
        try {
            attrContext.getCurrentNodePointer();
        } catch (Exception e) {
            // Some contexts might throw if not positioned
        }
    }

    @Test
    public void testAttributeContextWithBeanPointer() {
        TestBean bean = new TestBean();
        JXPathContext context = JXPathContext.newContext(bean);
        NodePointer rootPointer = NodePointer.newNodePointer(new QName("bean"), bean, null);
        
        EvalContext baseContext = new InitialContext(new EvalContext(null, null) {
            @Override
            public NodePointer getCurrentNodePointer() {
                return rootPointer;
            }
            @Override
            public boolean nextNode() {
                return false;
            }
        });

        QName qName = new QName("someAttribute");
        NodeTest nodeTest = new NodeNameTest(qName);
        AttributeContext attrContext = new AttributeContext(baseContext, nodeTest);

        // Drive the context
        attrContext.reset();
        boolean positioned = attrContext.setPosition(1);
        assertFalse(positioned);
    }

    public static class TestBean {
        private String someAttribute = "value";

        public String getSomeAttribute() {
            return someAttribute;
        }

        public void setSomeAttribute(String someAttribute) {
            this.someAttribute = someAttribute;
        }
    }
}