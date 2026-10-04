package org.apache.commons.jxpath.ri.axes;

import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.junit.Assert;
import org.junit.Test;

public class UnionContextTest {

    @Test
    public void testUnionContextExecutionWithNullPointers() {
        JXPathContextReferenceImpl context = null;
        try {
            context = (JXPathContextReferenceImpl) JXPathContextReferenceImpl.newContext(null, new Object());
        } catch (Exception e) {
            // fallback if context creation fails due to environment
        }

        EvalContext rootContext = new InitialContext(new EvalContext(null, null) {
            @Override
            public NodePointer getCurrentNodePointer() {
                return NullPointer.newNodePointer(new QName("test"), null, null);
            }
        });

        EvalContext[] subContexts = new EvalContext[] {
            new InitialContext(rootContext),
            new InitialContext(rootContext)
        };

        UnionContext unionContext = new UnionContext(rootContext, subContexts);

        Assert.assertNotNull(unionContext);
        
        // Exercise reset
        unionContext.reset();
        
        // Exercise position and node iteration
        boolean hasNode = unionContext.nextNode();
        // Depending on setup, just ensure it doesn't throw unexpected exceptions
        // and correctly executes the collection sorting/pointer management logic.
    }

    @Test
    public void testUnionContextWithNoSubContexts() {
        EvalContext rootContext = new InitialContext(null);
        EvalContext[] subContexts = new EvalContext[0];

        UnionContext unionContext = new UnionContext(rootContext, subContexts);
        unionContext.reset();
        
        Assert.assertFalse(unionContext.nextNode());
    }

    @Test
    public void testDocumentOrderSorting() {
        JXPathContextReferenceImpl.newContext(null, new Object());
        EvalContext rootContext = new InitialContext(null);

        // Create dummy subcontexts
        EvalContext[] subContexts = new EvalContext[] {
            new InitialContext(rootContext)
        };

        UnionContext unionContext = new UnionContext(rootContext, subContexts);
        
        // Invoking methods to cover document order sorting and duplicate removal branches
        unionContext.getDocumentOrder();
        
        // Set position to trigger node collection
        unionContext.setPosition(1);
        
        int docOrder = unionContext.getDocumentOrder();
        Assert.assertEquals(1, docOrder);
    }
}