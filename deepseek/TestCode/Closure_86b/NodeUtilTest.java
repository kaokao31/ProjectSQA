package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class NodeUtilTest {
    private Node createNode(int type) {
        return new Node(type);
    }

    private Node createNode(int type, Node child) {
        return new Node(type, child);
    }

    private Node createNode(int type, Node left, Node right) {
        return new Node(type, left, right);
    }

    @Before
    public void setUp() {
        // No setup needed for static calls, but keep for consistency
    }

    // ----- isBooleanResult tests -----

    @Test
    public void testIsBooleanResult_NullLiteral() {
        Node nullNode = createNode(Token.NULL);
        assertFalse("NULL literal should not be boolean result", NodeUtil.isBooleanResult(nullNode));
    }

    @Test
    public void testIsBooleanResult_TrueLiteral() {
        Node trueNode = createNode(Token.TRUE);
        assertTrue(NodeUtil.isBooleanResult(trueNode));
    }

    @Test
    public void testIsBooleanResult_FalseLiteral() {
        Node falseNode = createNode(Token.FALSE);
        assertTrue(NodeUtil.isBooleanResult(falseNode));
    }

    @Test
    public void testIsBooleanResult_NumberLiteral() {
        Node numNode = createNode(Token.NUMBER);
        assertFalse(NodeUtil.isBooleanResult(numNode));
    }

    @Test
    public void testIsBooleanResult_StringLiteral() {
        Node strNode = createNode(Token.STRING);
        assertFalse(NodeUtil.isBooleanResult(strNode));
    }

    @Test
    public void testIsBooleanResult_BinOpLogicalAnd() {
        Node andNode = createNode(Token.AND);
        assertTrue(NodeUtil.isBooleanResult(andNode));
    }

    @Test
    public void testIsBooleanResult_BinOpLogicalOr() {
        Node orNode = createNode(Token.OR);
        assertTrue(NodeUtil.isBooleanResult(orNode));
    }

    @Test
    public void testIsBooleanResult_BinOpLogicalNot() {
        Node notNode = createNode(Token.NOT);
        assertTrue(NodeUtil.isBooleanResult(notNode));
    }

    @Test
    public void testIsBooleanResult_RelationalOp() {
        Node eqNode = createNode(Token.EQ);
        assertTrue(NodeUtil.isBooleanResult(eqNode));
        Node ltNode = createNode(Token.LT);
        assertTrue(NodeUtil.isBooleanResult(ltNode));
    }

    @Test
    public void testIsBooleanResult_InstanceOf() {
        Node instanceOfNode = createNode(Token.INSTANCEOF);
        assertTrue(NodeUtil.isBooleanResult(instanceOfNode));
    }

    @Test
    public void testIsBooleanResult_Call() {
        Node callNode = createNode(Token.CALL);
        assertFalse("CALL node is not necessarily boolean", NodeUtil.isBooleanResult(callNode));
    }

    @Test
    public void testIsBooleanResult_ArrayLiteral() {
        Node arrayNode = createNode(Token.ARRAYLIT);
        assertFalse(NodeUtil.isBooleanResult(arrayNode));
    }

    @Test
    public void testIsBooleanResult_ObjectLiteral() {
        Node objNode = createNode(Token.OBJECTLIT);
        assertFalse(NodeUtil.isBooleanResult(objNode));
    }

    // ----- isNumberResult tests -----

    @Test
    public void testIsNumberResult_NumberLiteral() {
        assertTrue(NodeUtil.isNumberResult(createNode(Token.NUMBER)));
    }

    @Test
    public void testIsNumberResult_StringLiteral() {
        assertFalse(NodeUtil.isNumberResult(createNode(Token.STRING)));
    }

    @Test
    public void testIsNumberResult_UnaryMinus() {
        assertTrue(NodeUtil.isNumberResult(createNode(Token.NEG)));
    }

    @Test
    public void testIsNumberResult_BinOpArithmetic() {
        assertTrue(NodeUtil.isNumberResult(createNode(Token.ADD)));
        assertTrue(NodeUtil.isNumberResult(createNode(Token.SUB)));
        assertTrue(NodeUtil.isNumberResult(createNode(Token.MUL)));
        assertTrue(NodeUtil.isNumberResult(createNode(Token.DIV)));
    }

    @Test
    public void testIsNumberResult_ArrayLiteral() {
        assertFalse(NodeUtil.isNumberResult(createNode(Token.ARRAYLIT)));
    }

    // ----- isStringResult tests -----

    @Test
    public void testIsStringResult_StringLiteral() {
        assertTrue(NodeUtil.isStringResult(createNode(Token.STRING)));
    }

    @Test
    public void testIsStringResult_AddWithString() {
        Node addNode = createNode(Token.ADD);
        Node left = createNode(Token.STRING);
        Node right = createNode(Token.NUMBER);
        addNode.addChildToFront(left);
        addNode.addChildToFront(right);
        // The result of ADD is string if one operand is string
        assertTrue(NodeUtil.isStringResult(addNode));
    }

    @Test
    public void testIsStringResult_NumberAdd() {
        Node addNode = createNode(Token.ADD);
        Node left = createNode(Token.NUMBER);
        Node right = createNode(Token.NUMBER);
        addNode.addChildToFront(left);
        addNode.addChildToFront(right);
        // Both numbers, result is number
        assertFalse(NodeUtil.isStringResult(addNode));
    }

    @Test
    public void testIsStringResult_NotAdd() {
        assertFalse(NodeUtil.isStringResult(createNode(Token.MUL)));
    }

    // ----- isReference tests -----

    @Test
    public void testIsReference_Name() {
        Node nameNode = createNode(Token.NAME);
        assertTrue(NodeUtil.isReference(nameNode));
    }

    @Test
    public void testIsReference_GetProp() {
        Node getpropNode = createNode(Token.GETPROP);
        assertTrue(NodeUtil.isReference(getpropNode));
    }

    @Test
    public void testIsReference_StringLiteral() {
        assertFalse(NodeUtil.isReference(createNode(Token.STRING)));
    }

    @Test
    public void testIsReference_NumberLiteral() {
        assertFalse(NodeUtil.isReference(createNode(Token.NUMBER)));
    }

    // ----- isAssignmentOp tests -----

    @Test
    public void testIsAssignmentOp_Assign() {
        assertTrue(NodeUtil.isAssignmentOp(Token.ASSIGN));
    }

    @Test
    public void testIsAssignmentOp_AddAssign() {
        assertTrue(NodeUtil.isAssignmentOp(Token.ASSIGN_ADD));
    }

    @Test
    public void testIsAssignmentOp_Eq() {
        assertFalse(NodeUtil.isAssignmentOp(Token.EQ));
    }

    // ----- isLiteralValue tests -----

    @Test
    public void testIsLiteralValue_Null() {
        Node nullNode = createNode(Token.NULL);
        assertTrue(NodeUtil.isLiteralValue(nullNode));
    }

    @Test
    public void testIsLiteralValue_True() {
        assertTrue(NodeUtil.isLiteralValue(createNode(Token.TRUE)));
    }

    @Test
    public void testIsLiteralValue_Number() {
        assertTrue(NodeUtil.isLiteralValue(createNode(Token.NUMBER)));
    }

    @Test
    public void testIsLiteralValue_String() {
        assertTrue(NodeUtil.isLiteralValue(createNode(Token.STRING)));
    }

    @Test
    public void testIsLiteralValue_Void() {
        assertTrue(NodeUtil.isLiteralValue(createNode(Token.VOID)));
    }

    @Test
    public void testIsLiteralValue_Name() {
        assertFalse(NodeUtil.isLiteralValue(createNode(Token.NAME)));
    }

    // ----- isPureFunction tests -----

    @Test
    public void testIsPureFunction_NullNode() {
        Node nullNode = createNode(Token.NULL);
        assertFalse(NodeUtil.isPureFunction(nullNode));
    }

    @Test
    public void testIsPureFunction_Call() {
        Node callNode = createNode(Token.CALL);
        // Without a function name, assume not pure
        assertFalse(NodeUtil.isPureFunction(callNode));
    }

    // ----- isValidDefineValue tests -----

    @Test
    public void testIsValidDefineValue_Number() {
        assertTrue(NodeUtil.isValidDefineValue(createNode(Token.NUMBER)));
    }

    @Test
    public void testIsValidDefineValue_String() {
        assertTrue(NodeUtil.isValidDefineValue(createNode(Token.STRING)));
    }

    @Test
    public void testIsValidDefineValue_True() {
        assertTrue(NodeUtil.isValidDefineValue(createNode(Token.TRUE)));
    }

    @Test
    public void testIsValidDefineValue_False() {
        assertTrue(NodeUtil.isValidDefineValue(createNode(Token.FALSE)));
    }

    @Test
    public void testIsValidDefineValue_ArrayLiteral() {
        assertFalse(NodeUtil.isValidDefineValue(createNode(Token.ARRAYLIT)));
    }

    // ----- isExpressionNode tests -----

    @Test
    public void testIsExpressionNode_ExprResult() {
        Node exprResult = createNode(Token.EXPR_RESULT);
        assertTrue(NodeUtil.isExpressionNode(exprResult));
    }

    @Test
    public void testIsExpressionNode_Block() {
        assertFalse(NodeUtil.isExpressionNode(createNode(Token.BLOCK)));
    }

    // ----- evaluatesToLocalValue tests -----

    @Test
    public void testEvaluatesToLocalValue_NumberLiteral() {
        assertTrue(NodeUtil.evaluatesToLocalValue(createNode(Token.NUMBER)));
    }

    @Test
    public void testEvaluatesToLocalValue_StringLiteral() {
        assertTrue(NodeUtil.evaluatesToLocalValue(createNode(Token.STRING)));
    }

    @Test
    public void testEvaluatesToLocalValue_This() {
        assertFalse(NodeUtil.evaluatesToLocalValue(createNode(Token.THIS)));
    }

    @Test
    public void testEvaluatesToLocalValue_Name() {
        assertFalse(NodeUtil.evaluatesToLocalValue(createNode(Token.NAME)));
    }

    // ----- isThis tests -----

    @Test
    public void testIsThis_ThisNode() {
        assertTrue(NodeUtil.isThis(createNode(Token.THIS)));
    }

    @Test
    public void testIsThis_NameNode() {
        assertFalse(NodeUtil.isThis(createNode(Token.NAME)));
    }

    // ----- isObjectLitKey tests -----

    @Test
    public void testIsObjectLitKey_StringKey() {
        Node keyNode = createNode(Token.STRING_KEY);
        assertTrue(NodeUtil.isObjectLitKey(keyNode));
    }

    @Test
    public void testIsObjectLitKey_GetterDef() {
        Node getterNode = createNode(Token.GETTER_DEF);
        assertTrue(NodeUtil.isObjectLitKey(getterNode));
    }

    @Test
    public void testIsObjectLitKey_SetterDef() {
        Node setterNode = createNode(Token.SETTER_DEF);
        assertTrue(NodeUtil.isObjectLitKey(setterNode));
    }

    @Test
    public void testIsObjectLitKey_Name() {
        assertFalse(NodeUtil.isObjectLitKey(createNode(Token.NAME)));
    }

    // ----- isUnitNumberLiteral tests -----

    @Test
    public void testIsUnitNumberLiteral_One() {
        Node one = createNode(Token.NUMBER);
        one.putDoubleProp(Node.DOUBLE_PROP, 1.0);
        assertTrue(NodeUtil.isUnitNumberLiteral(one));
    }

    @Test
    public void testIsUnitNumberLiteral_Zero() {
        Node zero = createNode(Token.NUMBER);
        zero.putDoubleProp(Node.DOUBLE_PROP, 0.0);
        assertFalse(NodeUtil.isUnitNumberLiteral(zero));
    }

    @Test
    public void testIsUnitNumberLiteral_NotNumber() {
        assertFalse(NodeUtil.isUnitNumberLiteral(createNode(Token.STRING)));
    }

    // ----- isUnitNumberLiteral edge -----

    @Test
    public void testIsUnitNumberLiteral_OneInt() {
        Node one = createNode(Token.NUMBER);
        one.putDoubleProp(Node.DOUBLE_PROP, 1.0);
        assertTrue(NodeUtil.isUnitNumberLiteral(one));
    }

    // ----- isLiteralOrConstValue tests -----

    @Test
    public void testIsLiteralOrConstValue_Literal() {
        assertTrue(NodeUtil.isLiteralOrConstValue(createNode(Token.NUMBER)));
    }

    @Test
    public void testIsLiteralOrConstValue_Name() {
        // Name might be const, but without scope, it's false
        assertFalse(NodeUtil.isLiteralOrConstValue(createNode(Token.NAME)));
    }

    // ----- canBeSideEffected tests -----

    @Test
    public void testCanBeSideEffected_True() {
        assertTrue(NodeUtil.canBeSideEffected(createNode(Token.TRUE)));
    }

    @Test
    public void testCanBeSideEffected_False() {
        assertFalse(NodeUtil.canBeSideEffected(createNode(Token.FALSE)));
    }

    // ----- containsCall tests -----

    @Test
    public void testContainsCall_NoCall() {
        Node block = createNode(Token.BLOCK);
        block.addChildToFront(createNode(Token.NUMBER));
        assertFalse(NodeUtil.containsCall(block));
    }

    @Test
    public void testContainsCall_HasCall() {
        Node block = createNode(Token.BLOCK);
        Node call = createNode(Token.CALL);
        block.addChildToFront(call);
        assertTrue(NodeUtil.containsCall(block));
    }

    // ----- isForIn tests -----

    @Test
    public void testIsForIn_ForInLoop() {
        Node forIn = createNode(Token.FOR_IN);
        assertTrue(NodeUtil.isForIn(forIn));
    }

    @Test
    public void testIsForIn_ForLoop() {
        Node forLoop = createNode(Token.FOR);
        assertFalse(NodeUtil.isForIn(forLoop));
    }

    // ----- isArrayLiteral tests -----

    @Test
    public void testIsArrayLiteral_Array() {
        assertTrue(NodeUtil.isArrayLiteral(createNode(Token.ARRAYLIT)));
    }

    @Test
    public void testIsArrayLiteral_Object() {
        assertFalse(NodeUtil.isArrayLiteral(createNode(Token.OBJECTLIT)));
    }

    // ----- isObjectLiteral tests -----

    @Test
    public void testIsObjectLiteral_Object() {
        assertTrue(NodeUtil.isObjectLiteral(createNode(Token.OBJECTLIT)));
    }

    @Test
    public void testIsObjectLiteral_Array() {
        assertFalse(NodeUtil.isObjectLiteral(createNode(Token.ARRAYLIT)));
    }

    // ----- isNaN tests -----

    @Test
    public void testIsNaN_NumberNaN() {
        Node nanNode = createNode(Token.NUMBER);
        nanNode.putDoubleProp(Node.DOUBLE_PROP, Double.NaN);
        assertTrue(NodeUtil.isNaN(nanNode));
    }

    @Test
    public void testIsNaN_NumberNormal() {
        Node numNode = createNode(Token.NUMBER);
        numNode.putDoubleProp(Node.DOUBLE_PROP, 5.0);
        assertFalse(NodeUtil.isNaN(numNode));
    }

    @Test
    public void testIsNaN_NotNumber() {
        assertFalse(NodeUtil.isNaN(createNode(Token.STRING)));
    }

    // ----- isString tests -----

    @Test
    public void testIsString_String() {
        assertTrue(NodeUtil.isString(createNode(Token.STRING)));
    }

    @Test
    public void testIsString_Number() {
        assertFalse(NodeUtil.isString(createNode(Token.NUMBER)));
    }

    // ----- isNumber tests -----

    @Test
    public void testIsNumber_Number() {
        assertTrue(NodeUtil.isNumber(createNode(Token.NUMBER)));
    }

    @Test
    public void testIsNumber_String() {
        assertFalse(NodeUtil.isNumber(createNode(Token.STRING)));
    }

    // ----- isBoolean tests -----

    @Test
    public void testIsBoolean_True() {
        assertTrue(NodeUtil.isBoolean(createNode(Token.TRUE)));
    }

    @Test
    public void testIsBoolean_False() {
        assertTrue(NodeUtil.isBoolean(createNode(Token.FALSE)));
    }

    @Test
    public void testIsBoolean_Number() {
        assertFalse(NodeUtil.isBoolean(createNode(Token.NUMBER)));
    }

    // ----- isUndefined tests -----

    @Test
    public void testIsUndefined_Void() {
        assertTrue(NodeUtil.isUndefined(createNode(Token.VOID)));
    }

    @Test
    public void testIsUndefined_Null() {
        assertFalse(NodeUtil.isUndefined(createNode(Token.NULL)));
    }

    // ----- isNull tests -----

    @Test
    public void testIsNull_Null() {
        assertTrue(NodeUtil.isNull(createNode(Token.NULL)));
    }

    @Test
    public void testIsNull_Void() {
        assertFalse(NodeUtil.isNull(createNode(Token.VOID)));
    }

    // ----- isUndefinedOrNull tests -----

    @Test
    public void testIsUndefinedOrNull_Null() {
        assertTrue(NodeUtil.isUndefinedOrNull(createNode(Token.NULL)));
    }

    @Test
    public void testIsUndefinedOrNull_Void() {
        assertTrue(NodeUtil.isUndefinedOrNull(createNode(Token.VOID)));
    }

    @Test
    public void testIsUndefinedOrNull_String() {
        assertFalse(NodeUtil.isUndefinedOrNull(createNode(Token.STRING)));
    }

    // ----- areNullAndUndefinedEquivalent tests -----

    @Test
    public void testAreNullAndUndefinedEquivalent_NullVoid() {
        assertTrue(NodeUtil.areNullAndUndefinedEquivalent(createNode(Token.NULL), createNode(Token.VOID)));
    }

    @Test
    public void testAreNullAndUndefinedEquivalent_VoidNull() {
        assertTrue(NodeUtil.areNullAndUndefinedEquivalent(createNode(Token.VOID), createNode(Token.NULL)));
    }

    @Test
    public void testAreNullAndUndefinedEquivalent_NullNull() {
        assertTrue(NodeUtil.areNullAndUndefinedEquivalent(createNode(Token.NULL), createNode(Token.NULL)));
    }

    @Test
    public void testAreNullAndUndefinedEquivalent_NumberNull() {
        assertFalse(NodeUtil.areNullAndUndefinedEquivalent(createNode(Token.NUMBER), createNode(Token.NULL)));
    }

    // ----- isEquivalentTo tests -----

    @Test
    public void testIsEquivalentTo_SameLiteral() {
        Node a = createNode(Token.TRUE);
        Node b = createNode(Token.TRUE);
        assertTrue(NodeUtil.isEquivalentTo(a, b));
    }

    @Test
    public void testIsEquivalentTo_DifferentLiteral() {
        Node a = createNode(Token.TRUE);
        Node b = createNode(Token.FALSE);
        assertFalse(NodeUtil.isEquivalentTo(a, b));
    }

    @Test
    public void testIsEquivalentTo_NullVsVoid() {
        assertFalse(NodeUtil.isEquivalentTo(createNode(Token.NULL), createNode(Token.VOID)));
    }

    // ----- isSimpleOperator tests -----

    @Test
    public void testIsSimpleOperator_Add() {
        assertTrue(NodeUtil.isSimpleOperator(createNode(Token.ADD)));
    }

    @Test
    public void testIsSimpleOperator_Sub() {
        assertTrue(NodeUtil.isSimpleOperator(createNode(Token.SUB)));
    }

    @Test
    public void testIsSimpleOperator_Assign() {
        assertFalse(NodeUtil.isSimpleOperator(createNode(Token.ASSIGN)));
    }

    // ----- isArithmeticOperator tests -----

    @Test
    public void testIsArithmeticOperator_Mul() {
        assertTrue(NodeUtil.isArithmeticOperator(createNode(Token.MUL)));
    }

    @Test
    public void testIsArithmeticOperator_And() {
        assertFalse(NodeUtil.isArithmeticOperator(createNode(Token.AND)));
    }

    // ----- isComparisonOperator tests -----

    @Test
    public void testIsComparisonOperator_Eq() {
        assertTrue(NodeUtil.isComparisonOperator(createNode(Token.EQ)));
    }

    @Test
    public void testIsComparisonOperator_Add() {
        assertFalse(NodeUtil.isComparisonOperator(createNode(Token.ADD)));
    }

    // ----- isBitwiseOperator tests -----

    @Test
    public void testIsBitwiseOperator_BitAnd() {
        assertTrue(NodeUtil.isBitwiseOperator(createNode(Token.BITAND)));
    }

    @Test
    public void testIsBitwiseOperator_Add() {
        assertFalse(NodeUtil.isBitwiseOperator(createNode(Token.ADD)));
    }

    // ----- isInfixOperator tests -----

    @Test
    public void testIsInfixOperator_Add() {
        assertTrue(NodeUtil.isInfixOperator(createNode(Token.ADD)));
    }

    @Test
    public void testIsInfixOperator_Not() {
        assertFalse(NodeUtil.isInfixOperator(createNode(Token.NOT)));
    }

    // ----- isUnaryOperator tests -----

    @Test
    public void testIsUnaryOperator_Not() {
        assertTrue(NodeUtil.isUnaryOperator(createNode(Token.NOT)));
    }

    @Test
    public void testIsUnaryOperator_Add() {
        assertFalse(NodeUtil.isUnaryOperator(createNode(Token.ADD)));
    }

    // ----- isFunctionObjectApply tests -----

    @Test
    public void testIsFunctionObjectApply_ApplyCall() {
        Node getprop = createNode(Token.GETPROP);
        getprop.addChildToFront(createNode(Token.NAME)); // object
        Node applyStr = createNode(Token.STRING);
        applyStr.setString("apply");
        getprop.addChildToFront(applyStr);
        Node call = createNode(Token.CALL);
        call.addChildToFront(getprop);
        assertTrue(NodeUtil.isFunctionObjectApply(call));
    }

    @Test
    public void testIsFunctionObjectApply_CallCall() {
        Node getprop = createNode(Token.GETPROP);
        getprop.addChildToFront(createNode(Token.NAME)); // object
        Node callStr = createNode(Token.STRING);
        callStr.setString("call");
        getprop.addChildToFront(callStr);
        Node call = createNode(Token.CALL);
        call.addChildToFront(getprop);
        assertFalse(NodeUtil.isFunctionObjectApply(call));
    }

    // ----- isEmptyBlock tests -----

    @Test
    public void testIsEmptyBlock_EmptyBlock() {
        Node block = createNode(Token.BLOCK);
        assertTrue(NodeUtil.isEmptyBlock(block));
    }

    @Test
    public void testIsEmptyBlock_BlockWithChild() {
        Node block = createNode(Token.BLOCK);
        block.addChildToFront(createNode(Token.NUMBER));
        assertFalse(NodeUtil.isEmptyBlock(block));
    }

    @Test
    public void testIsEmptyBlock_NotBlock() {
        assertFalse(NodeUtil.isEmptyBlock(createNode(Token.SCRIPT)));
    }

    // ----- isLabelName tests -----

    @Test
    public void testIsLabelName_Label() {
        Node label = createNode(Token.LABEL_NAME);
        assertTrue(NodeUtil.isLabelName(label));
    }

    @Test
    public void testIsLabelName_Name() {
        assertFalse(NodeUtil.isLabelName(createNode(Token.NAME)));
    }

    // ----- isName tests -----

    @Test
    public void testIsName_Name() {
        assertTrue(NodeUtil.isName(createNode(Token.NAME)));
    }

    @Test
    public void testIsName_String() {
        assertFalse(NodeUtil.isName(createNode(Token.STRING)));
    }

    // ----- isGet tests -----

    @Test
    public void testIsGet_GetProp() {
        assertTrue(NodeUtil.isGet(createNode(Token.GETPROP)));
    }

    @Test
    public void testIsGet_GetElem() {
        assertTrue(NodeUtil.isGet(createNode(Token.GETELEM)));
    }

    @Test
    public void testIsGet_Name() {
        assertFalse(NodeUtil.isGet(createNode(Token.NAME)));
    }

    // ----- isCall tests -----

    @Test
    public void testIsCall_Call() {
        assertTrue(NodeUtil.isCall(createNode(Token.CALL)));
    }

    @Test
    public void testIsCall_New() {
        assertFalse(NodeUtil.isCall(createNode(Token.NEW)));
    }

    // ----- isNew tests -----

    @Test
    public void testIsNew_New() {
        assertTrue(NodeUtil.isNew(createNode(Token.NEW)));
    }

    @Test
    public void testIsNew_Call() {
        assertFalse(NodeUtil.isNew(createNode(Token.CALL)));
    }

    // ----- isFunction tests -----

    @Test
    public void testIsFunction_Function() {
        assertTrue(NodeUtil.isFunction(createNode(Token.FUNCTION)));
    }

    @Test
    public void testIsFunction_Script() {
        assertFalse(NodeUtil.isFunction(createNode(Token.SCRIPT)));
    }

    // ----- isScript tests -----

    @Test
    public void testIsScript_Script() {
        assertTrue(NodeUtil.isScript(createNode(Token.SCRIPT)));
    }

    @Test
    public void testIsScript_Block() {
        assertFalse(NodeUtil.isScript(createNode(Token.BLOCK)));
    }

    // ----- isThis tests -----

    @Test
    public void testIsThis_This() {
        assertTrue(NodeUtil.isThis(createNode(Token.THIS)));
    }

    @Test
    public void testIsThis_Super() {
        assertFalse(NodeUtil.isThis(createNode(Token.SUPER)));
    }

    // ----- isSuper tests -----

    @Test
    public void testIsSuper_Super() {
        assertTrue(NodeUtil.isSuper(createNode(Token.SUPER)));
    }

    @Test
    public void testIsSuper_This() {
        assertFalse(NodeUtil.isSuper(createNode(Token.THIS)));
    }

    // ----- isComma tests -----

    @Test
    public void testIsComma_Comma() {
        assertTrue(NodeUtil.isComma(createNode(Token.COMMA)));
    }

    @Test
    public void testIsComma_Semicolon() {
        assertFalse(NodeUtil.isComma(createNode(Token.SEMICOLON)));
    }

    // ----- isVar tests -----

    @Test
    public void testIsVar_Var() {
        assertTrue(NodeUtil.isVar(createNode(Token.VAR)));
    }

    @Test
    public void testIsVar_Let() {
        assertFalse(NodeUtil.isVar(createNode(Token.LET)));
    }

    // ----- isLet tests -----

    @Test
    public void testIsLet_Let() {
        assertTrue(NodeUtil.isLet(createNode(Token.LET)));
    }

    @Test
    public void testIsLet_Var() {
        assertFalse(NodeUtil.isLet(createNode(Token.VAR)));
    }

    // ----- isConst tests -----

    @Test
    public void testIsConst_Const() {
        assertTrue(NodeUtil.isConst(createNode(Token.CONST)));
    }

    @Test
    public void testIsConst_Var() {
        assertFalse(NodeUtil.isConst(createNode(Token.VAR)));
    }

    // ----- isVarOrConst tests -----

    @Test
    public void testIsVarOrConst_Var() {
        assertTrue(NodeUtil.isVarOrConst(createNode(Token.VAR)));
    }

    @Test
    public void testIsVarOrConst_Const() {
        assertTrue(NodeUtil.isVarOrConst(createNode(Token.CONST)));
    }

    @Test
    public void testIsVarOrConst_Let() {
        assertFalse(NodeUtil.isVarOrConst(createNode(Token.LET)));
    }

    // ----- isNamespace tests -----

    @Test
    public void testIsNamespace_GetProp() {
        Node getprop = createNode(Token.GETPROP);
        getprop.addChildToFront(createNode(Token.NAME)); // root
        getprop.addChildToFront(createNode(Token.STRING)); // property
        assertTrue(NodeUtil.isNamespace(getprop));
    }

    @Test
    public void testIsNamespace_Name() {
        assertTrue(NodeUtil.isNamespace(createNode(Token.NAME)));
    }

    @Test
    public void testIsNamespace_String() {
        assertFalse(NodeUtil.isNamespace(createNode(Token.STRING)));
    }

    // ----- isExternsFile tests -----

    @Test
    public void testIsExternsFile_Externs() {
        Node script = createNode(Token.SCRIPT);
        Node externs = createNode(Token.EXTERN);
        script.addChildToFront(externs);
        // Not directly testable without CompilerInput, skip for now
    }

    // Additional edge cases for isBooleanResult (bug-specific)
    @Test
    public void testIsBooleanResult_EmptyBlock() {
        Node block = createNode(Token.BLOCK);
        assertFalse("Empty block should not be boolean result", NodeUtil.isBooleanResult(block));
    }

    @Test
    public void testIsBooleanResult_Hook() {
        Node hook = createNode(Token.HOOK);
        assertTrue("Hook (ternary) is boolean result", NodeUtil.isBooleanResult(hook));
    }

    @Test
    public void testIsBooleanResult_Void() {
        Node voidNode = createNode(Token.VOID);
        assertFalse("Void expression is not boolean result", NodeUtil.isBooleanResult(voidNode));
    }

    // Test for bug 86: null literal should not be considered boolean
    @Test
    public void testIsBooleanResult_NullLiteral_FailsOnBug() {
        Node nullNode = createNode(Token.NULL);
        assertFalse("NULL literal is not a boolean result", NodeUtil.isBooleanResult(nullNode));
    }
}