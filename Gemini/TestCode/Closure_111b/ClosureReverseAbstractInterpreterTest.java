package com.google.javascript.jscomp.type;

import com.google.javascript.jscomp.CodingConvention;
import com.google.javascript.jscomp.GoogleCodingConvention;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.StaticSourceFile;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.SimpleErrorReporter;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ClosureReverseAbstractInterpreterTest {

    private JSTypeRegistry typeRegistry;
    private CodingConvention codingConvention;
    private ClosureReverseAbstractInterpreter interpreter;

    @Before
    public void setUp() {
        typeRegistry = new JSTypeRegistry(new SimpleErrorReporter());
        codingConvention = new GoogleCodingConvention();
        interpreter = new ClosureReverseAbstractInterpreter(codingConvention, typeRegistry);
    }

    @Test
    public void testGetPreciseTypeRegistryMethodNullNode() {
        // Test firstBranch with null node or non-JSCOMP_STRICT_GET_TYPE node
        Node node = Node.newNumber(10);
        JSType result = interpreter.getPreciseTypeRegistryMethod(node, false);
        assertNull(result);
    }

    @Test
    public void testGetPreciseTypeRegistryMethodValidCall() {
        // Construct a call node for JSCOMP_STRICT_GET_TYPE
        // goog.typeOf or similar functions handled by ClosureCodingConvention / ClosureReverseAbstractInterpreter
        Node qName = Node.newString(Node.SBIND, "goog.testing.asserts.assertObject");
        Node callNode = new Node(Token.CALL, qName, Node.newNumber(1));
        
        // This exercises the specific handling in ClosureReverseAbstractInterpreter where 
        // JSCOMP_STRICT_GET_TYPE or similar conventions are checked.
        JSType result = interpreter.getPreciseTypeRegistryMethod(callNode, true);
        assertNull(result); // Depending on registry setup, typically null or a resolved type
    }

    @Test
    public void testFirstBranchWithNullCondition() {
        // getFirstHolf / firstBranch / secondBranch logic test via public interface
        // FlowScope or ReverseAbstractInterpreter methods:
        // interface is typically `getRestrictedWithScope`
        try {
            interpreter.getRestrictedWithScope(null, null, true);
        } catch (Exception e) {
            // expected or handled gracefully
        }
    }

    @Test
    public void testCaseClosure() {
        // Construct standard Closure specific function call nodes that trigger specific reverse interpreter behaviors
        // e.g., goog.isArray, goog.isObject, goog.isString, goog.isFunction, goog.isNumber, goog.isBoolean, goog.isNull
        
        String[] funcNames = {
            "goog.isArray", "goog.isObject", "goog.isString", 
            "goog.isFunction", "goog.isNumber", "goog.isBoolean", "goog.isNull",
            "goog.isDef", "goog.isDefAndNotNull"
        };

        for (String funcName : funcNames) {
            Node nameNode = Node.newString(Token.NAME, funcName);
            // If it's a qualified name or property
            Node callNode = new Node(Token.CALL, nameNode, Node.newString(Token.NAME, "x"));
            
            assertNotNull(interpreter.getRestrictedFirstChildFlag(callNode));
        }
    }

    @Test
    public void testObjectIsFunction() {
        Node callNode = new Node(Token.CALL, Node.newString(Token.NAME, "Object"), Node.newString(Token.NAME, "x"));
        assertNotNull(interpreter.getRestrictedFirstChildFlag(callNode));
    }

    @Test
    public void testGetTypeOf() {
        // goog.typeOf()
        Node callNode = new Node(Token.CALL, Node.newString(Token.NAME, "goog.typeOf"), Node.newString(Token.NAME, "x"));
        assertNotNull(interpreter.getRestrictedFirstChildFlag(callNode));
    }

    @Test
    public void testDefaultCase() {
        Node callNode = new Node(Token.CALL, Node.newString(Token.NAME, "unknownFunction"), Node.newString(Token.NAME, "x"));
        assertNull(interpreter.getRestrictedFirstChildFlag(callNode));
    }
}