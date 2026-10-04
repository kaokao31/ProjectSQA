package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for InlineCostEstimator.
 * Designed to achieve maximum code coverage and detect potential faults.
 */
public class InlineCostEstimatorTest {

    private Node scriptNode;
    private Node emptyBlock;
    private Node functionNode;
    private Node callNode;
    private Node nameNode;
    private Node numberNode;
    private Node stringNode;
    private Node arrayNode;
    private Node objectNode;
    private Node thisNode;
    private Node nullNode;
    private Node addNode;
    private Node ifNode;
    private Node whileNode;
    private Node forNode;
    private Node returnNode;
    private Node throwNode;
    private Node tryNode;
    private Node switchNode;
    private Node caseNode;
    private Node defaultCase;
    private Node breakNode;
    private Node continueNode;
    private Node varNode;
    private Node assignNode;
    private Node commaNode;
    private Node notNode;
    private Node andNode;
    private Node orNode;
    private Node incNode;
    private Node decNode;
    private Node newNode;
    private Node delPropNode;
    private Node typeofNode;
    private Node voidNode;
    private Node unaryMinusNode;
    private Node bitwiseNotNode;
    private Node posNode;
    private Node negNode;
    private Node mulNode;
    private Node divNode;
    private Node modNode;
    private Node expNode;
    private Node eqNode;
    private Node neNode;
    private Node ltNode;
    private Node gtNode;
    private Node leNode;
    private Node geNode;
    private Node inNode;
    private Node instanceOfNode;
    private Node arrayLitNode;
    private Node objectLitNode;
    private Node regexpNode;
    private Node trueNode;
    private Node falseNode;
    private Node nullLitNode;
    private Node emptyNode;
    private Node largeNumberNode;
    private Node negativeNumberNode;
    private Node zeroNode;
    private Node nanNode;
    private Node infinityNode;
    private Node emptyStringNode;
    private Node longStringNode;
    private Node deepNestedNode;
    private Node manyChildrenNode;

    @Before
    public void setUp() {
        // Create various node types for testing
        scriptNode = new Node(Node.SCRIPT);
        emptyBlock = new Node(Node.BLOCK);
        functionNode = new Node(Node.FUNCTION);
        callNode = new Node(Node.CALL);
        nameNode = Node.newString(Token.NAME, "x");
        numberNode = Node.newNumber(42.0);
        stringNode = Node.newString(Token.STRING, "hello");
        arrayNode = new Node(Node.ARRAYLIT);
        objectNode = new Node(Node.OBJECTLIT);
        thisNode = new Node(Node.THIS);
        nullNode = null;
        addNode = new Node(Node.ADD);
        ifNode = new Node(Node.IF);
        whileNode = new Node(Node.WHILE);
        forNode = new Node(Node.FOR);
        returnNode = new Node(Node.RETURN);
        throwNode = new Node(Node.THROW);
        tryNode = new Node(Node.TRY);
        switchNode = new Node(Node.SWITCH);
        caseNode = new Node(Node.CASE);
        defaultCase = new Node(Node.DEFAULT_CASE);
        breakNode = new Node(Node.BREAK);
        continueNode = new Node(Node.CONTINUE);
        varNode = new Node(Node.VAR);
        assignNode = new Node(Node.ASSIGN);
        commaNode = new Node(Node.COMMA);
        notNode = new Node(Node.NOT);
        andNode = new Node(Node.AND);
        orNode = new Node(Node.OR);
        incNode = new Node(Node.INC);
        decNode = new Node(Node.DEC);
        newNode = new Node(Node.NEW);
        delPropNode = new Node(Node.DELPROP);
        typeofNode = new Node(Node.TYPEOF);
        voidNode = new Node(Node.VOID);
        unaryMinusNode = new Node(Node.NEG);
        bitwiseNotNode = new Node(Node.BITNOT);
        posNode = new Node(Node.POS);
        negNode = new Node(Node.NEG);
        mulNode = new Node(Node.MUL);
        divNode = new Node(Node.DIV);
        modNode = new Node(Node.MOD);
        expNode = new Node(Node.EXPONENT);
        eqNode = new Node(Node.EQ);
        neNode = new Node(Node.NE);
        ltNode = new Node(Node.LT);
        gtNode = new Node(Node.GT);
        leNode = new Node(Node.LE);
        geNode = new Node(Node.GE);
        inNode = new Node(Node.IN);
        instanceOfNode = new Node(Node.INSTANCEOF);
        arrayLitNode = new Node(Node.ARRAYLIT);
        objectLitNode = new Node(Node.OBJECTLIT);
        regexpNode = new Node(Node.REGEXP);
        trueNode = new Node(Node.TRUE);
        falseNode = new Node(Node.FALSE);
        nullLitNode = new Node(Node.NULL);
        emptyNode = new Node(Node.EMPTY);
        largeNumberNode = Node.newNumber(1e308);
        negativeNumberNode = Node.newNumber(-100.5);
        zeroNode = Node.newNumber(0.0);
        nanNode = Node.newNumber(Double.NaN);
        infinityNode = Node.newNumber(Double.POSITIVE_INFINITY);
        emptyStringNode = Node.newString(Token.STRING, "");
        longStringNode = Node.newString(Token.STRING, "a".repeat(1000));
        deepNestedNode = new Node(Node.BLOCK);
        Node child = new Node(Node.BLOCK);
        child.addChildToBack(new Node(Node.BLOCK));
        deepNestedNode.addChildToBack(child);
        manyChildrenNode = new Node(Node.BLOCK);
        for (int i = 0; i < 100; i++) {
            manyChildrenNode.addChildToBack(new Node(Node.EXPR_RESULT));
        }
    }

    @Test
    public void testNullNode() {
        try {
            InlineCostEstimator.getCost(nullNode, false);
            fail("Expected NullPointerException for null node");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testScriptNode() {
        int cost = InlineCostEstimator.getCost(scriptNode, false);
        assertTrue("Cost for SCRIPT should be non-negative", cost >= 0);
    }

    @Test
    public void testEmptyBlock() {
        int cost = InlineCostEstimator.getCost(emptyBlock, false);
        assertTrue("Cost for empty BLOCK should be non-negative", cost >= 0);
    }

    @Test
    public void testFunctionNode() {
        int cost = InlineCostEstimator.getCost(functionNode, false);
        assertTrue("Cost for FUNCTION should be non-negative", cost >= 0);
    }

    @Test
    public void testCallNode() {
        int cost = InlineCostEstimator.getCost(callNode, false);
        assertTrue("Cost for CALL should be non-negative", cost >= 0);
    }

    @Test
    public void testNameNode() {
        int cost = InlineCostEstimator.getCost(nameNode, false);
        assertTrue("Cost for NAME should be non-negative", cost >= 0);
    }

    @Test
    public void testNumberNode() {
        int cost = InlineCostEstimator.getCost(numberNode, false);
        assertTrue("Cost for NUMBER should be non-negative", cost >= 0);
    }

    @Test
    public void testStringNode() {
        int cost = InlineCostEstimator.getCost(stringNode, false);
        assertTrue("Cost for STRING should be non-negative", cost >= 0);
    }

    @Test
    public void testArrayLitNode() {
        int cost = InlineCostEstimator.getCost(arrayLitNode, false);
        assertTrue("Cost for ARRAYLIT should be non-negative", cost >= 0);
    }

    @Test
    public void testObjectLitNode() {
        int cost = InlineCostEstimator.getCost(objectLitNode, false);
        assertTrue("Cost for OBJECTLIT should be non-negative", cost >= 0);
    }

    @Test
    public void testThisNode() {
        int cost = InlineCostEstimator.getCost(thisNode, false);
        assertTrue("Cost for THIS should be non-negative", cost >= 0);
    }

    @Test
    public void testAddNode() {
        int cost = InlineCostEstimator.getCost(addNode, false);
        assertTrue("Cost for ADD should be non-negative", cost >= 0);
    }

    @Test
    public void testIfNode() {
        int cost = InlineCostEstimator.getCost(ifNode, false);
        assertTrue("Cost for IF should be non-negative", cost >= 0);
    }

    @Test
    public void testWhileNode() {
        int cost = InlineCostEstimator.getCost(whileNode, false);
        assertTrue("Cost for WHILE should be non-negative", cost >= 0);
    }

    @Test
    public void testForNode() {
        int cost = InlineCostEstimator.getCost(forNode, false);
        assertTrue("Cost for FOR should be non-negative", cost >= 0);
    }

    @Test
    public void testReturnNode() {
        int cost = InlineCostEstimator.getCost(returnNode, false);
        assertTrue("Cost for RETURN should be non-negative", cost >= 0);
    }

    @Test
    public void testThrowNode() {
        int cost = InlineCostEstimator.getCost(throwNode, false);
        assertTrue("Cost for THROW should be non-negative", cost >= 0);
    }

    @Test
    public void testTryNode() {
        int cost = InlineCostEstimator.getCost(tryNode, false);
        assertTrue("Cost for TRY should be non-negative", cost >= 0);
    }

    @Test
    public void testSwitchNode() {
        int cost = InlineCostEstimator.getCost(switchNode, false);
        assertTrue("Cost for SWITCH should be non-negative", cost >= 0);
    }

    @Test
    public void testCaseNode() {
        int cost = InlineCostEstimator.getCost(caseNode, false);
        assertTrue("Cost for CASE should be non-negative", cost >= 0);
    }

    @Test
    public void testDefaultCase() {
        int cost = InlineCostEstimator.getCost(defaultCase, false);
        assertTrue("Cost for DEFAULT_CASE should be non-negative", cost >= 0);
    }

    @Test
    public void testBreakNode() {
        int cost = InlineCostEstimator.getCost(breakNode, false);
        assertTrue("Cost for BREAK should be non-negative", cost >= 0);
    }

    @Test
    public void testContinueNode() {
        int cost = InlineCostEstimator.getCost(continueNode, false);
        assertTrue("Cost for CONTINUE should be non-negative", cost >= 0);
    }

    @Test
    public void testVarNode() {
        int cost = InlineCostEstimator.getCost(varNode, false);
        assertTrue("Cost for VAR should be non-negative", cost >= 0);
    }

    @Test
    public void testAssignNode() {
        int cost = InlineCostEstimator.getCost(assignNode, false);
        assertTrue("Cost for ASSIGN should be non-negative", cost >= 0);
    }

    @Test
    public void testCommaNode() {
        int cost = InlineCostEstimator.getCost(commaNode, false);
        assertTrue("Cost for COMMA should be non-negative", cost >= 0);
    }

    @Test
    public void testNotNode() {
        int cost = InlineCostEstimator.getCost(notNode, false);
        assertTrue("Cost for NOT should be non-negative", cost >= 0);
    }

    @Test
    public void testAndNode() {
        int cost = InlineCostEstimator.getCost(andNode, false);
        assertTrue("Cost for AND should be non-negative", cost >= 0);
    }

    @Test
    public void testOrNode() {
        int cost = InlineCostEstimator.getCost(orNode, false);
        assertTrue("Cost for OR should be non-negative", cost >= 0);
    }

    @Test
    public void testIncNode() {
        int cost = InlineCostEstimator.getCost(incNode, false);
        assertTrue("Cost for INC should be non-negative", cost >= 0);
    }

    @Test
    public void testDecNode() {
        int cost = InlineCostEstimator.getCost(decNode, false);
        assertTrue("Cost for DEC should be non-negative", cost >= 0);
    }

    @Test
    public void testNewNode() {
        int cost = InlineCostEstimator.getCost(newNode, false);
        assertTrue("Cost for NEW should be non-negative", cost >= 0);
    }

    @Test
    public void testDelPropNode() {
        int cost = InlineCostEstimator.getCost(delPropNode, false);
        assertTrue("Cost for DELPROP should be non-negative", cost >= 0);
    }

    @Test
    public void testTypeofNode() {
        int cost = InlineCostEstimator.getCost(typeofNode, false);
        assertTrue("Cost for TYPEOF should be non-negative", cost >= 0);
    }

    @Test
    public void testVoidNode() {
        int cost = InlineCostEstimator.getCost(voidNode, false);
        assertTrue("Cost for VOID should be non-negative", cost >= 0);
    }

    @Test
    public void testUnaryMinusNode() {
        int cost = InlineCostEstimator.getCost(unaryMinusNode, false);
        assertTrue("Cost for NEG should be non-negative", cost >= 0);
    }

    @Test
    public void testBitwiseNotNode() {
        int cost = InlineCostEstimator.getCost(bitwiseNotNode, false);
        assertTrue("Cost for BITNOT should be non-negative", cost >= 0);
    }

    @Test
    public void testPosNode() {
        int cost = InlineCostEstimator.getCost(posNode, false);
        assertTrue("Cost for POS should be non-negative", cost >= 0);
    }

    @Test
    public void testNegNode() {
        int cost = InlineCostEstimator.getCost(negNode, false);
        assertTrue("Cost for NEG should be non-negative", cost >= 0);
    }

    @Test
    public void testMulNode() {
        int cost = InlineCostEstimator.getCost(mulNode, false);
        assertTrue("Cost for MUL should be non-negative", cost >= 0);
    }

    @Test
    public void testDivNode() {
        int cost = InlineCostEstimator.getCost(divNode, false);
        assertTrue("Cost for DIV should be non-negative", cost >= 0);
    }

    @Test
    public void testModNode() {
        int cost = InlineCostEstimator.getCost(modNode, false);
        assertTrue("Cost for MOD should be non-negative", cost >= 0);
    }

    @Test
    public void testExpNode() {
        int cost = InlineCostEstimator.getCost(expNode, false);
        assertTrue("Cost for EXPONENT should be non-negative", cost >= 0);
    }

    @Test
    public void testEqNode() {
        int cost = InlineCostEstimator.getCost(eqNode, false);
        assertTrue("Cost for EQ should be non-negative", cost >= 0);
    }

    @Test
    public void testNeNode() {
        int cost = InlineCostEstimator.getCost(neNode, false);
        assertTrue("Cost for NE should be non-negative", cost >= 0);
    }

    @Test
    public void testLtNode() {
        int cost = InlineCostEstimator.getCost(ltNode, false);
        assertTrue("Cost for LT should be non-negative", cost >= 0);
    }

    @Test
    public void testGtNode() {
        int cost = InlineCostEstimator.getCost(gtNode, false);
        assertTrue("Cost for GT should be non-negative", cost >= 0);
    }

    @Test
    public void testLeNode() {
        int cost = InlineCostEstimator.getCost(leNode, false);
        assertTrue("Cost for LE should be non-negative", cost >= 0);
    }

    @Test
    public void testGeNode() {
        int cost = InlineCostEstimator.getCost(geNode, false);
        assertTrue("Cost for GE should be non-negative", cost >= 0);
    }

    @Test
    public void testInNode() {
        int cost = InlineCostEstimator.getCost(inNode, false);
        assertTrue("Cost for IN should be non-negative", cost >= 0);
    }

    @Test
    public void testInstanceOfNode() {
        int cost = InlineCostEstimator.getCost(instanceOfNode, false);
        assertTrue("Cost for INSTANCEOF should be non-negative", cost >= 0);
    }

    @Test
    public void testRegexpNode() {
        int cost = InlineCostEstimator.getCost(regexpNode, false);
        assertTrue("Cost for REGEXP should be non-negative", cost >= 0);
    }

    @Test
    public void testTrueNode() {
        int cost = InlineCostEstimator.getCost(trueNode, false);
        assertTrue("Cost for TRUE should be non-negative", cost >= 0);
    }

    @Test
    public void testFalseNode() {
        int cost = InlineCostEstimator.getCost(falseNode, false);
        assertTrue("Cost for FALSE should be non-negative", cost >= 0);
    }

    @Test
    public void testNullLitNode() {
        int cost = InlineCostEstimator.getCost(nullLitNode, false);
        assertTrue("Cost for NULL should be non-negative", cost >= 0);
    }

    @Test
    public void testEmptyNode() {
        int cost = InlineCostEstimator.getCost(emptyNode, false);
        assertTrue("Cost for EMPTY should be non-negative", cost >= 0);
    }

    @Test
    public void testLargeNumberNode() {
        int cost = InlineCostEstimator.getCost(largeNumberNode, false);
        assertTrue("Cost for large NUMBER should be non-negative", cost >= 0);
    }

    @Test
    public void testNegativeNumberNode() {
        int cost = InlineCostEstimator.getCost(negativeNumberNode, false);
        assertTrue("Cost for negative NUMBER should be non-negative", cost >= 0);
    }

    @Test
    public void testZeroNode() {
        int cost = InlineCostEstimator.getCost(zeroNode, false);
        assertTrue("Cost for zero NUMBER should be non-negative", cost >= 0);
    }

    @Test
    public void testNaNNode() {
        int cost = InlineCostEstimator.getCost(nanNode, false);
        assertTrue("Cost for NaN NUMBER should be non-negative", cost >= 0);
    }

    @Test
    public void testInfinityNode() {
        int cost = InlineCostEstimator.getCost(infinityNode, false);
        assertTrue("Cost for Infinity NUMBER should be non-negative", cost >= 0);
    }

    @Test
    public void testEmptyStringNode() {
        int cost = InlineCostEstimator.getCost(emptyStringNode, false);
        assertTrue("Cost for empty STRING should be non-negative", cost >= 0);
    }

    @Test
    public void testLongStringNode() {
        int cost = InlineCostEstimator.getCost(longStringNode, false);
        assertTrue("Cost for long STRING should be non-negative", cost >= 0);
    }

    @Test
    public void testDeepNestedNode() {
        int cost = InlineCostEstimator.getCost(deepNestedNode, false);
        assertTrue("Cost for deep nested BLOCK should be non-negative", cost >= 0);
    }

    @Test
    public void testManyChildrenNode() {
        int cost = InlineCostEstimator.getCost(manyChildrenNode, false);
        assertTrue("Cost for BLOCK with many children should be non-negative", cost >= 0);
    }

    @Test
    public void testCallNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(callNode, true);
        assertTrue("Cost for CALL with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testFunctionNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(functionNode, true);
        assertTrue("Cost for FUNCTION with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testNameNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(nameNode, true);
        assertTrue("Cost for NAME with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testNumberNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(numberNode, true);
        assertTrue("Cost for NUMBER with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testStringNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(stringNode, true);
        assertTrue("Cost for STRING with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testAddNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(addNode, true);
        assertTrue("Cost for ADD with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testIfNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(ifNode, true);
        assertTrue("Cost for IF with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testWhileNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(whileNode, true);
        assertTrue("Cost for WHILE with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testForNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(forNode, true);
        assertTrue("Cost for FOR with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testReturnNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(returnNode, true);
        assertTrue("Cost for RETURN with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testThrowNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(throwNode, true);
        assertTrue("Cost for THROW with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testTryNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(tryNode, true);
        assertTrue("Cost for TRY with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testSwitchNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(switchNode, true);
        assertTrue("Cost for SWITCH with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testCaseNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(caseNode, true);
        assertTrue("Cost for CASE with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testDefaultCaseWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(defaultCase, true);
        assertTrue("Cost for DEFAULT_CASE with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testBreakNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(breakNode, true);
        assertTrue("Cost for BREAK with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testContinueNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(continueNode, true);
        assertTrue("Cost for CONTINUE with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testVarNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(varNode, true);
        assertTrue("Cost for VAR with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testAssignNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(assignNode, true);
        assertTrue("Cost for ASSIGN with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testCommaNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(commaNode, true);
        assertTrue("Cost for COMMA with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testNotNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(notNode, true);
        assertTrue("Cost for NOT with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testAndNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(andNode, true);
        assertTrue("Cost for AND with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testOrNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(orNode, true);
        assertTrue("Cost for OR with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testIncNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(incNode, true);
        assertTrue("Cost for INC with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testDecNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(decNode, true);
        assertTrue("Cost for DEC with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testNewNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(newNode, true);
        assertTrue("Cost for NEW with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testDelPropNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(delPropNode, true);
        assertTrue("Cost for DELPROP with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testTypeofNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(typeofNode, true);
        assertTrue("Cost for TYPEOF with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testVoidNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(voidNode, true);
        assertTrue("Cost for VOID with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testUnaryMinusNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(unaryMinusNode, true);
        assertTrue("Cost for NEG with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testBitwiseNotNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(bitwiseNotNode, true);
        assertTrue("Cost for BITNOT with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testPosNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(posNode, true);
        assertTrue("Cost for POS with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testNegNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(negNode, true);
        assertTrue("Cost for NEG with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testMulNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(mulNode, true);
        assertTrue("Cost for MUL with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testDivNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(divNode, true);
        assertTrue("Cost for DIV with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testModNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(modNode, true);
        assertTrue("Cost for MOD with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testExpNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(expNode, true);
        assertTrue("Cost for EXPONENT with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testEqNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(eqNode, true);
        assertTrue("Cost for EQ with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testNeNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(neNode, true);
        assertTrue("Cost for NE with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testLtNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(ltNode, true);
        assertTrue("Cost for LT with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testGtNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(gtNode, true);
        assertTrue("Cost for GT with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testLeNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(leNode, true);
        assertTrue("Cost for LE with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testGeNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(geNode, true);
        assertTrue("Cost for GE with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testInNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(inNode, true);
        assertTrue("Cost for IN with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testInstanceOfNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(instanceOfNode, true);
        assertTrue("Cost for INSTANCEOF with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testRegexpNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(regexpNode, true);
        assertTrue("Cost for REGEXP with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testTrueNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(trueNode, true);
        assertTrue("Cost for TRUE with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testFalseNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(falseNode, true);
        assertTrue("Cost for FALSE with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testNullLitNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(nullLitNode, true);
        assertTrue("Cost for NULL with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testEmptyNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(emptyNode, true);
        assertTrue("Cost for EMPTY with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testLargeNumberNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(largeNumberNode, true);
        assertTrue("Cost for large NUMBER with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testNegativeNumberNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(negativeNumberNode, true);
        assertTrue("Cost for negative NUMBER with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testZeroNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(zeroNode, true);
        assertTrue("Cost for zero NUMBER with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testNaNNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(nanNode, true);
        assertTrue("Cost for NaN NUMBER with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testInfinityNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(infinityNode, true);
        assertTrue("Cost for Infinity NUMBER with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testEmptyStringNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(emptyStringNode, true);
        assertTrue("Cost for empty STRING with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testLongStringNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(longStringNode, true);
        assertTrue("Cost for long STRING with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testDeepNestedNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(deepNestedNode, true);
        assertTrue("Cost for deep nested BLOCK with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testManyChildrenNodeWithIsCallTrue() {
        int cost = InlineCostEstimator.getCost(manyChildrenNode, true);
        assertTrue("Cost for BLOCK with many children with isCall=true should be non-negative", cost >= 0);
    }

    @Test
    public void testCostConsistencyForSameNode() {
        int cost1 = InlineCostEstimator.getCost(nameNode, false);
        int cost2 = InlineCostEstimator.getCost(nameNode, false);
        assertEquals("Cost should be consistent for same node", cost1, cost2);
    }

    @Test
    public void testCostForNodeWithChildren() {
        Node parent = new Node(Node.ADD);
        parent.addChildToBack(new Node(Node.NUMBER, 1.0));
        parent.addChildToBack(new Node(Node.NUMBER, 2.0));
        int cost = InlineCostEstimator.getCost(parent, false);
        assertTrue("Cost for ADD with children should be non-negative", cost >= 0);
    }

    @Test
    public void testCostForNodeWithManyChildren() {
        Node parent = new Node(Node.ADD);
        for (int i = 0; i < 100; i++) {
            parent.addChildToBack(new Node(Node.NUMBER, i));
        }
        int cost = InlineCostEstimator.getCost(parent, false);
        assertTrue("Cost for ADD with many children should be non-negative", cost >= 0);
    }

    @Test
    public void testCostForNodeWithDeepNestedChildren() {
        Node deep = new Node(Node.BLOCK);
        Node current = deep;
        for (int i = 0; i < 100; i++) {
            Node child = new Node(Node.BLOCK);
            current.addChildToBack(child);
            current = child;
        }
        int cost = InlineCostEstimator.getCost(deep, false);
        assertTrue("Cost for deeply nested BLOCK should be non-negative", cost >= 0);
    }

    @Test
    public void testCostForNodeWithCircularReference() {
        // Create a node that references itself (should not happen in practice but test robustness)
        Node selfRef = new Node(Node.BLOCK);
        selfRef.addChildToBack(selfRef);
        try {
            int cost = InlineCostEstimator.getCost(selfRef, false);
            // If it doesn't throw, cost should be non-negative
            assertTrue("Cost for self-referential node should be non-negative", cost >= 0);
        } catch (StackOverflowError e) {
            // Expected if implementation doesn't handle cycles
        }
    }

    @Test
    public void testCostForNodeWithNullChild() {
        Node parent = new Node(Node.ADD);
        parent.addChildToBack(null);
        try {
            int cost = InlineCostEstimator.getCost(parent, false);
            // If it doesn't throw, cost should be non-negative
            assertTrue("Cost for node with null child should be non-negative", cost >= 0);
        } catch (NullPointerException e) {
            // Expected if implementation doesn't handle null children
        }
    }

    @Test
    public void testCostForNodeWithNegativeCost() {
        // Some implementations might return negative cost for certain nodes; test that it's non-negative
        int cost = InlineCostEstimator.getCost(numberNode, false);
        assertTrue("Cost should be non-negative", cost >= 0);
    }

    @Test
    public void testCostForNodeWithZeroCost() {
        // Test that some nodes might have zero cost
        int cost = InlineCostEstimator.getCost(emptyNode, false);
        assertTrue("Cost for EMPTY should be zero or positive", cost >= 0);
    }

    @Test
    public void testCostForNodeWithLargeCost() {
        // Test that cost doesn't overflow
        Node largeNode = new Node(Node.BLOCK);
        for (int i = 0; i < 10000; i++) {
            largeNode.addChildToBack(new Node(Node.EXPR_RESULT));
        }
        int cost = InlineCostEstimator.getCost(largeNode, false);
        assertTrue("Cost should not overflow", cost >= 0);
    }

    @Test
    public void testCostForNodeWithDifferentTokenTypes() {
        // Test all token types that are not covered above
        Node[] nodes = {
            new Node(Node.SCRIPT),
            new Node(Node.BLOCK),
            new Node(Node.LABEL),
            new Node(Node.LABEL_NAME),
            new Node(Node.FOR_IN),
            new Node(Node.WITH),
            new Node(Node.DO),
            new Node(Node.FOR),
            new Node(Node.IF),
            new Node(Node.ELSE),
            new Node(Node.SWITCH),
            new Node(Node.CASE),
            new Node(Node.DEFAULT_CASE),
            new Node(Node.WHILE),
            new Node(Node.DO),
            new Node(Node.TRY),
            new Node(Node.CATCH),
            new Node(Node.FINALLY),
            new Node(Node.THROW),
            new Node(Node.RETURN),
            new Node(Node.BREAK),
            new Node(Node.CONTINUE),
            new Node(Node.VAR),
            new Node(Node.CONST),
            new Node(Node.LET),
            new Node(Node.ASSIGN),
            new Node(Node.ASSIGN_BITOR),
            new Node(Node.ASSIGN_BITXOR),
            new Node(Node.ASSIGN_BITAND),
            new Node(Node.ASSIGN_LSH),
            new Node(Node.ASSIGN_RSH),
            new Node(Node.ASSIGN_URSH),
            new Node(Node.ASSIGN_ADD),
            new Node(Node.ASSIGN_SUB),
            new Node(Node.ASSIGN_MUL),
            new Node(Node.ASSIGN_DIV),
            new Node(Node.ASSIGN_MOD),
            new Node(Node.ASSIGN_EXPONENT),
            new Node(Node.HOOK),
            new Node(Node.COMMA),
            new Node(Node.OR),
            new Node(Node.AND),
            new Node(Node.BITOR),
            new Node(Node.BITXOR),
            new Node(Node.BITAND),
            new Node(Node.EQ),
            new Node(Node.NE),
            new Node(Node.LT),
            new Node(Node.GT),
            new Node(Node.LE),
            new Node(Node.GE),
            new Node(Node.IN),
            new Node(Node.INSTANCEOF),
            new Node(Node.LSH),
            new Node(Node.RSH),
            new Node(Node.URSH),
            new Node(Node.ADD),
            new Node(Node.SUB),
            new Node(Node.MUL),
            new Node(Node.DIV),
            new Node(Node.MOD),
            new Node(Node.EXPONENT),
            new Node(Node.NOT),
            new Node(Node.BITNOT),
            new Node(Node.POS),
            new Node(Node.NEG),
            new Node(Node.INC),
            new Node(Node.DEC),
            new Node(Node.NEW),
            new Node(Node.DELPROP),
            new Node(Node.TYPEOF),
            new Node(Node.VOID),
            new Node(Node.STRING),
            new Node(Node.NUMBER),
            new Node(Node.BOOLEAN),
            new Node(Node.NULL),
            new Node(Node.THIS),
            new Node(Node.SUPER),
            new Node(Node.ARRAYLIT),
            new Node(Node.OBJECTLIT),
            new Node(Node.REGEXP),
            new Node(Node.NAME),
            new Node(Node.GETELEM),
            new Node(Node.GETPROP),
            new Node(Node.SETPROP),
            new Node(Node.CALL),
            new Node(Node.NEW),
            new Node(Node.FUNCTION),
            new Node(Node.ARRAY_PATTERN),
            new Node(Node.OBJECT_PATTERN),
            new Node(Node.DEFAULT_VALUE),
            new Node(Node.REST),
            new Node(Node.SPREAD),
            new Node(Node.YIELD),
            new Node(Node.AWAIT),
            new Node(Node.TEMPLATELIT),
            new Node(Node.TAGGED_TEMPLATELIT),
            new Node(Node.MODULE),
            new Node(Node.IMPORT),
            new Node(Node.EXPORT),
            new Node(Node.CLASS),
            new Node(Node.CLASS_MEMBER),
            new Node(Node.MEMBER_VARIABLE_DEFINITION),
            new Node(Node.COMPUTED_PROP),
            new Node(Node.GETTER_DEF),
            new Node(Node.SETTER_DEF),
            new Node(Node.METHOD_DEF),
            new Node(Node.ARROW_FUNCTION),
            new Node(Node.GENERATOR_COMPREHENSION),
            new Node(Node.COMPREHENSION_REF),
            new Node(Node.COMPREHENSION_TAIL),
            new Node(Node.COMPREHENSION_IF),
            new Node(Node.COMPREHENSION_FOR),
            new Node(Node.ARRAY_COMPREHENSION),
            new Node(Node.GENERATOR),
            new Node(Node.LET),
            new Node(Node.CONST),
            new Node(Node.DEBUGGER),
            new Node(Node.EMPTY),
            new Node(Node.ANNOTATION),
            new Node(Node.PIPE),
            new Node(Node.OPTIONAL_CHAIN),
            new Node(Node.NULLISH_COALESCING),
            new Node(Node.LOGICAL_ASSIGN),
            new Node(Node.ASSIGN_NULLISH_COALESCING),
            new Node(Node.ASSIGN_AND),
            new Node(Node.ASSIGN_OR),
            new Node(Node.ASSIGN_EXPONENT),
            new Node(Node.ASSIGN_ADD),
            new Node(Node.ASSIGN_SUB),
            new Node(Node.ASSIGN_MUL),
            new Node(Node.ASSIGN_DIV),
            new Node(Node.ASSIGN_MOD),
            new Node(Node.ASSIGN_BITAND),
            new Node(Node.ASSIGN_BITOR),
            new Node(Node.ASSIGN_BITXOR),
            new Node(Node.ASSIGN_LSH),
            new Node(Node.ASSIGN_RSH),
            new Node(Node.ASSIGN_URSH),
            new Node(Node.ASSIGN_ADD),
            new Node(Node.ASSIGN_SUB),
            new Node(Node.ASSIGN_MUL),
            new Node(Node.ASSIGN_DIV),
            new Node(Node.ASSIGN_MOD),
            new Node(Node.ASSIGN_EXPONENT),
            new Node(Node.ASSIGN_BITAND),
            new Node(Node.ASSIGN_BITOR),
            new Node(Node.ASSIGN_BITXOR),
            new Node(Node.ASSIGN_LSH),
            new Node(Node.ASSIGN_RSH),
            new Node(Node.ASSIGN_URSH),
            new Node(Node.ASSIGN_ADD),
            new Node(Node.ASSIGN_SUB),
            new Node(Node.ASSIGN_MUL),
            new Node(Node.ASSIGN_DIV),
            new Node(Node.ASSIGN_MOD),
            new Node(Node.ASSIGN_EXPONENT)
        };
        for (Node node : nodes) {
            int cost = InlineCostEstimator.getCost(node, false);
            assertTrue("Cost for " + node.getToken() + " should be non-negative", cost >= 0);
        }
    }
}