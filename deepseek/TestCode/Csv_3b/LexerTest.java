package lexer;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit 4 test suite for the Lexer class.
 * Designed to achieve high line and branch coverage and to reveal potential faults.
 */
public class LexerTest {

    private Lexer lexer;

    @Before
    public void setUp() {
        // Initialize lexer with empty input by default; each test will set its own input.
        lexer = new Lexer("");
    }

    // ===================== Basic Tokenization Tests =====================

    @Test
    public void testEmptyInput() {
        lexer = new Lexer("");
        assertNull("Empty input should return null token", lexer.nextToken());
    }

    @Test
    public void testWhitespaceOnly() {
        lexer = new Lexer("   \t\n\r  ");
        assertNull("Whitespace-only input should return null token", lexer.nextToken());
    }

    @Test
    public void testSingleIdentifier() {
        lexer = new Lexer("abc");
        Token token = lexer.nextToken();
        assertNotNull("Should return a token for identifier", token);
        assertEquals("Token type should be IDENTIFIER", TokenType.IDENTIFIER, token.getType());
        assertEquals("Token value should be 'abc'", "abc", token.getValue());
        assertNull("No more tokens after identifier", lexer.nextToken());
    }

    @Test
    public void testSingleNumber() {
        lexer = new Lexer("123");
        Token token = lexer.nextToken();
        assertNotNull("Should return a token for number", token);
        assertEquals("Token type should be NUMBER", TokenType.NUMBER, token.getType());
        assertEquals("Token value should be '123'", "123", token.getValue());
        assertNull("No more tokens after number", lexer.nextToken());
    }

    @Test
    public void testSingleOperator() {
        lexer = new Lexer("+");
        Token token = lexer.nextToken();
        assertNotNull("Should return a token for operator", token);
        assertEquals("Token type should be OPERATOR", TokenType.OPERATOR, token.getType());
        assertEquals("Token value should be '+'", "+", token.getValue());
        assertNull("No more tokens after operator", lexer.nextToken());
    }

    @Test
    public void testSingleStringLiteral() {
        lexer = new Lexer("\"hello\"");
        Token token = lexer.nextToken();
        assertNotNull("Should return a token for string literal", token);
        assertEquals("Token type should be STRING", TokenType.STRING, token.getType());
        assertEquals("Token value should be 'hello' (without quotes)", "hello", token.getValue());
        assertNull("No more tokens after string literal", lexer.nextToken());
    }

    // ===================== Multiple Tokens Tests =====================

    @Test
    public void testMultipleTokensWithWhitespace() {
        lexer = new Lexer("a + 123");
        Token t1 = lexer.nextToken();
        assertEquals(TokenType.IDENTIFIER, t1.getType());
        assertEquals("a", t1.getValue());

        Token t2 = lexer.nextToken();
        assertEquals(TokenType.OPERATOR, t2.getType());
        assertEquals("+", t2.getValue());

        Token t3 = lexer.nextToken();
        assertEquals(TokenType.NUMBER, t3.getType());
        assertEquals("123", t3.getValue());

        assertNull(lexer.nextToken());
    }

    @Test
    public void testTokensWithoutWhitespace() {
        lexer = new Lexer("a+123");
        Token t1 = lexer.nextToken();
        assertEquals(TokenType.IDENTIFIER, t1.getType());
        assertEquals("a", t1.getValue());

        Token t2 = lexer.nextToken();
        assertEquals(TokenType.OPERATOR, t2.getType());
        assertEquals("+", t2.getValue());

        Token t3 = lexer.nextToken();
        assertEquals(TokenType.NUMBER, t3.getType());
        assertEquals("123", t3.getValue());

        assertNull(lexer.nextToken());
    }

    // ===================== Edge Cases and Boundary Tests =====================

    @Test
    public void testNullInput() {
        try {
            lexer = new Lexer(null);
            fail("Expected IllegalArgumentException for null input");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testNumberStartingWithZero() {
        lexer = new Lexer("0123");
        Token token = lexer.nextToken();
        assertNotNull(token);
        assertEquals(TokenType.NUMBER, token.getType());
        assertEquals("0123", token.getValue());
    }

    @Test
    public void testNumberWithLeadingZeros() {
        lexer = new Lexer("000");
        Token token = lexer.nextToken();
        assertNotNull(token);
        assertEquals(TokenType.NUMBER, token.getType());
        assertEquals("000", token.getValue());
    }

    @Test
    public void testIdentifierWithUnderscore() {
        lexer = new Lexer("my_var");
        Token token = lexer.nextToken();
        assertNotNull(token);
        assertEquals(TokenType.IDENTIFIER, token.getType());
        assertEquals("my_var", token.getValue());
    }

    @Test
    public void testIdentifierStartingWithUnderscore() {
        lexer = new Lexer("_private");
        Token token = lexer.nextToken();
        assertNotNull(token);
        assertEquals(TokenType.IDENTIFIER, token.getType());
        assertEquals("_private", token.getValue());
    }

    @Test
    public void testIdentifierWithDigits() {
        lexer = new Lexer("var123");
        Token token = lexer.nextToken();
        assertNotNull(token);
        assertEquals(TokenType.IDENTIFIER, token.getType());
        assertEquals("var123", token.getValue());
    }

    @Test
    public void testOperatorPlus() {
        lexer = new Lexer("+");
        Token token = lexer.nextToken();
        assertEquals(TokenType.OPERATOR, token.getType());
        assertEquals("+", token.getValue());
    }

    @Test
    public void testOperatorMinus() {
        lexer = new Lexer("-");
        Token token = lexer.nextToken();
        assertEquals(TokenType.OPERATOR, token.getType());
        assertEquals("-", token.getValue());
    }

    @Test
    public void testOperatorMultiply() {
        lexer = new Lexer("*");
        Token token = lexer.nextToken();
        assertEquals(TokenType.OPERATOR, token.getType());
        assertEquals("*", token.getValue());
    }

    @Test
    public void testOperatorDivide() {
        lexer = new Lexer("/");
        Token token = lexer.nextToken();
        assertEquals(TokenType.OPERATOR, token.getType());
        assertEquals("/", token.getValue());
    }

    @Test
    public void testOperatorAssignment() {
        lexer = new Lexer("=");
        Token token = lexer.nextToken();
        assertEquals(TokenType.OPERATOR, token.getType());
        assertEquals("=", token.getValue());
    }

    @Test
    public void testOperatorComparison() {
        lexer = new Lexer("==");
        Token token = lexer.nextToken();
        assertEquals(TokenType.OPERATOR, token.getType());
        assertEquals("==", token.getValue());
    }

    @Test
    public void testOperatorNotEqual() {
        lexer = new Lexer("!=");
        Token token = lexer.nextToken();
        assertEquals(TokenType.OPERATOR, token.getType());
        assertEquals("!=", token.getValue());
    }

    @Test
    public void testOperatorGreaterThan() {
        lexer = new Lexer(">");
        Token token = lexer.nextToken();
        assertEquals(TokenType.OPERATOR, token.getType());
        assertEquals(">", token.getValue());
    }

    @Test
    public void testOperatorLessThan() {
        lexer = new Lexer("<");
        Token token = lexer.nextToken();
        assertEquals(TokenType.OPERATOR, token.getType());
        assertEquals("<", token.getValue());
    }

    @Test
    public void testOperatorGreaterOrEqual() {
        lexer = new Lexer(">=");
        Token token = lexer.nextToken();
        assertEquals(TokenType.OPERATOR, token.getType());
        assertEquals(">=", token.getValue());
    }

    @Test
    public void testOperatorLessOrEqual() {
        lexer = new Lexer("<=");
        Token token = lexer.nextToken();
        assertEquals(TokenType.OPERATOR, token.getType());
        assertEquals("<=", token.getValue());
    }

    @Test
    public void testOperatorLogicalAnd() {
        lexer = new Lexer("&&");
        Token token = lexer.nextToken();
        assertEquals(TokenType.OPERATOR, token.getType());
        assertEquals("&&", token.getValue());
    }

    @Test
    public void testOperatorLogicalOr() {
        lexer = new Lexer("||");
        Token token = lexer.nextToken();
        assertEquals(TokenType.OPERATOR, token.getType());
        assertEquals("||", token.getValue());
    }

    @Test
    public void testOperatorLogicalNot() {
        lexer = new Lexer("!");
        Token token = lexer.nextToken();
        assertEquals(TokenType.OPERATOR, token.getType());
        assertEquals("!", token.getValue());
    }

    @Test
    public void testOperatorBitwiseAnd() {
        lexer = new Lexer("&");
        Token token = lexer.nextToken();
        assertEquals(TokenType.OPERATOR, token.getType());
        assertEquals("&", token.getValue());
    }

    @Test
    public void testOperatorBitwiseOr() {
        lexer = new Lexer("|");
        Token token = lexer.nextToken();
        assertEquals(TokenType.OPERATOR, token.getType());
        assertEquals("|", token.getValue());
    }

    @Test
    public void testOperatorBitwiseXor() {
        lexer = new Lexer("^");
        Token token = lexer.nextToken();
        assertEquals(TokenType.OPERATOR, token.getType());
        assertEquals("^", token.getValue());
    }

    @Test
    public void testOperatorModulus() {
        lexer = new Lexer("%");
        Token token = lexer.nextToken();
        assertEquals(TokenType.OPERATOR, token.getType());
        assertEquals("%", token.getValue());
    }

    @Test
    public void testOperatorIncrement() {
        lexer = new Lexer("++");
        Token token = lexer.nextToken();
        assertEquals(TokenType.OPERATOR, token.getType());
        assertEquals("++", token.getValue());
    }

    @Test
    public void testOperatorDecrement() {
        lexer = new Lexer("--");
        Token token = lexer.nextToken();
        assertEquals(TokenType.OPERATOR, token.getType());
        assertEquals("--", token.getValue());
    }

    @Test
    public void testOperatorArrow() {
        lexer = new Lexer("->");
        Token token = lexer.nextToken();
        assertEquals(TokenType.OPERATOR, token.getType());
        assertEquals("->", token.getValue());
    }

    // ===================== String Literal Edge Cases =====================

    @Test
    public void testEmptyStringLiteral() {
        lexer = new Lexer("\"\"");
        Token token = lexer.nextToken();
        assertNotNull(token);
        assertEquals(TokenType.STRING, token.getType());
        assertEquals("", token.getValue());
    }

    @Test
    public void testStringLiteralWithEscapeSequence() {
        lexer = new Lexer("\"hello\\nworld\"");
        Token token = lexer.nextToken();
        assertNotNull(token);
        assertEquals(TokenType.STRING, token.getType());
        assertEquals("hello\\nworld", token.getValue()); // Assuming escape sequences are not interpreted
    }

    @Test
    public void testStringLiteralWithQuotesInside() {
        lexer = new Lexer("\"he said \\\"hello\\\"\"");
        Token token = lexer.nextToken();
        assertNotNull(token);
        assertEquals(TokenType.STRING, token.getType());
        assertEquals("he said \\\"hello\\\"", token.getValue());
    }

    @Test
    public void testUnterminatedStringLiteral() {
        lexer = new Lexer("\"hello");
        // Depending on implementation, may throw exception or return token with unterminated flag
        // We expect an exception to be thrown for unterminated string
        try {
            Token token = lexer.nextToken();
            // If no exception, token might be returned with partial value
            // We'll check that token is not null and type is STRING (but value may be incomplete)
            assertNotNull(token);
            assertEquals(TokenType.STRING, token.getType());
            // This test may need adjustment based on actual behavior
        } catch (Exception e) {
            // Expected: unterminated string literal
        }
    }

    // ===================== Comment Handling Tests =====================

    @Test
    public void testSingleLineComment() {
        lexer = new Lexer("// this is a comment\nabc");
        Token token = lexer.nextToken();
        assertNotNull(token);
        assertEquals(TokenType.IDENTIFIER, token.getType());
        assertEquals("abc", token.getValue());
    }

    @Test
    public void testMultiLineComment() {
        lexer = new Lexer("/* comment */abc");
        Token token = lexer.nextToken();
        assertNotNull(token);
        assertEquals(TokenType.IDENTIFIER, token.getType());
        assertEquals("abc", token.getValue());
    }

    @Test
    public void testUnterminatedMultiLineComment() {
        lexer = new Lexer("/* unterminated");
        // Should throw exception or return null after EOF
        try {
            Token token = lexer.nextToken();
            // If no exception, token may be null
            assertNull(token);
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testCommentAtEndOfInput() {
        lexer = new Lexer("abc// comment");
        Token token = lexer.nextToken();
        assertNotNull(token);
        assertEquals(TokenType.IDENTIFIER, token.getType());
        assertEquals("abc", token.getValue());
        assertNull(lexer.nextToken());
    }

    // ===================== Special Characters and Edge Cases =====================

    @Test
    public void testSemicolon() {
        lexer = new Lexer(";");
        Token token = lexer.nextToken();
        assertNotNull(token);
        assertEquals(TokenType.SEPARATOR, token.getType());
        assertEquals(";", token.getValue());
    }

    @Test
    public void testComma() {
        lexer = new Lexer(",");
        Token token = lexer.nextToken();
        assertNotNull(token);
        assertEquals(TokenType.SEPARATOR, token.getType());
        assertEquals(",", token.getValue());
    }

    @Test
    public void testDot() {
        lexer = new Lexer(".");
        Token token = lexer.nextToken();
        assertNotNull(token);
        assertEquals(TokenType.SEPARATOR, token.getType());
        assertEquals(".", token.getValue());
    }

    @Test
    public void testParentheses() {
        lexer = new Lexer("()");
        Token t1 = lexer.nextToken();
        assertEquals(TokenType.SEPARATOR, t1.getType());
        assertEquals("(", t1.getValue());

        Token t2 = lexer.nextToken();
        assertEquals(TokenType.SEPARATOR, t2.getType());
        assertEquals(")", t2.getValue());

        assertNull(lexer.nextToken());
    }

    @Test
    public void testBraces() {
        lexer = new Lexer("{}");
        Token t1 = lexer.nextToken();
        assertEquals(TokenType.SEPARATOR, t1.getType());
        assertEquals("{", t1.getValue());

        Token t2 = lexer.nextToken();
        assertEquals(TokenType.SEPARATOR, t2.getType());
        assertEquals("}", t2.getValue());

        assertNull(lexer.nextToken());
    }

    @Test
    public void testBrackets() {
        lexer = new Lexer("[]");
        Token t1 = lexer.nextToken();
        assertEquals(TokenType.SEPARATOR, t1.getType());
        assertEquals("[", t1.getValue());

        Token t2 = lexer.nextToken();
        assertEquals(TokenType.SEPARATOR, t2.getType());
        assertEquals("]", t2.getValue());

        assertNull(lexer.nextToken());
    }

    @Test
    public void testKeyword() {
        // Assuming keywords are tokenized as IDENTIFIER or special type
        lexer = new Lexer("if");
        Token token = lexer.nextToken();
        assertNotNull(token);
        // If lexer distinguishes keywords, type should be KEYWORD; otherwise IDENTIFIER
        // We'll test for IDENTIFIER as fallback
        assertTrue("Keyword 'if' should be recognized",
                   token.getType() == TokenType.KEYWORD || token.getType() == TokenType.IDENTIFIER);
        assertEquals("if", token.getValue());
    }

    @Test
    public void testMultipleKeywords() {
        lexer = new Lexer("if else while for");
        Token t1 = lexer.nextToken();
        assertEquals("if", t1.getValue());
        Token t2 = lexer.nextToken();
        assertEquals("else", t2.getValue());
        Token t3 = lexer.nextToken();
        assertEquals("while", t3.getValue());
        Token t4 = lexer.nextToken();
        assertEquals("for", t4.getValue());
        assertNull(lexer.nextToken());
    }

    // ===================== Complex Expressions =====================

    @Test
    public void testArithmeticExpression() {
        lexer = new Lexer("a + b * c - d / e");
        Token[] expectedTypes = {
            TokenType.IDENTIFIER, TokenType.OPERATOR,
            TokenType.IDENTIFIER, TokenType.OPERATOR,
            TokenType.IDENTIFIER, TokenType.OPERATOR,
            TokenType.IDENTIFIER, TokenType.OPERATOR,
            TokenType.IDENTIFIER
        };
        String[] expectedValues = {"a", "+", "b", "*", "c", "-", "d", "/", "e"};
        for (int i = 0; i < expectedTypes.length; i++) {
            Token token = lexer.nextToken();
            assertNotNull("Token " + i + " should not be null", token);
            assertEquals("Token " + i + " type", expectedTypes[i], token.getType());
            assertEquals("Token " + i + " value", expectedValues[i], token.getValue());
        }
        assertNull(lexer.nextToken());
    }

    @Test
    public void testExpressionWithParentheses() {
        lexer = new Lexer("(a + b) * c");
        Token[] expectedTypes = {
            TokenType.SEPARATOR, TokenType.IDENTIFIER, TokenType.OPERATOR,
            TokenType.IDENTIFIER, TokenType.SEPARATOR, TokenType.OPERATOR,
            TokenType.IDENTIFIER
        };
        String[] expectedValues = {"(", "a", "+", "b", ")", "*", "c"};
        for (int i = 0; i < expectedTypes.length; i++) {
            Token token = lexer.nextToken();
            assertNotNull("Token " + i + " should not be null", token);
            assertEquals("Token " + i + " type", expectedTypes[i], token.getType());
            assertEquals("Token " + i + " value", expectedValues[i], token.getValue());
        }
        assertNull(lexer.nextToken());
    }

    // ===================== Fault-Triggering Tests (Defects4J style) =====================

    @Test
    public void testConsecutiveOperators() {
        // Potential bug: lexer may incorrectly combine operators or skip
        lexer = new Lexer("+++");
        Token t1 = lexer.nextToken();
        // Depending on implementation, may tokenize as "++" and "+" or as three "+"
        // We'll test that at least two tokens are produced
        assertNotNull(t1);
        Token t2 = lexer.nextToken();
        assertNotNull(t2);
        // Ensure no exception
    }

    @Test
    public void testNumberFollowedByIdentifier() {
        // Potential bug: lexer may treat "123abc" as number or identifier
        lexer = new Lexer("123abc");
        Token token = lexer.nextToken();
        assertNotNull(token);
        // Typically, this should be tokenized as number "123" and identifier "abc"
        // But some lexers may treat as identifier starting with digit (invalid)
        // We'll check that first token is NUMBER
        assertEquals(TokenType.NUMBER, token.getType());
        assertEquals("123", token.getValue());
        Token t2 = lexer.nextToken();
        assertNotNull(t2);
        assertEquals(TokenType.IDENTIFIER, t2.getType());
        assertEquals("abc", t2.getValue());
    }

    @Test
    public void testIdentifierWithNumberInside() {
        lexer = new Lexer("a1b2c");
        Token token = lexer.nextToken();
        assertNotNull(token);
        assertEquals(TokenType.IDENTIFIER, token.getType());
        assertEquals("a1b2c", token.getValue());
    }

    @Test
    public void testStringLiteralWithNewline() {
        // String literals should not contain unescaped newlines
        lexer = new Lexer("\"hello\nworld\"");
        // May throw exception or tokenize incorrectly
        try {
            Token token = lexer.nextToken();
            // If no exception, token may be returned with newline inside
            assertNotNull(token);
            assertEquals(TokenType.STRING, token.getType());
            // Value may contain newline
        } catch (Exception e) {
            // Expected: unterminated string or invalid character
        }
    }

    @Test
    public void testTabCharacterInString() {
        lexer = new Lexer("\"hello\tworld\"");
        Token token = lexer.nextToken();
        assertNotNull(token);
        assertEquals(TokenType.STRING, token.getType());
        assertEquals("hello\tworld", token.getValue());
    }

    @Test
    public void testBackslashAtEndOfString() {
        lexer = new Lexer("\"hello\\");
        // Unterminated string or escape sequence at end
        try {
            Token token = lexer.nextToken();
            // If no exception, token may be returned with backslash
            assertNotNull(token);
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testVeryLongIdentifier() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append('a');
        }
        lexer = new Lexer(sb.toString());
        Token token = lexer.nextToken();
        assertNotNull(token);
        assertEquals(TokenType.IDENTIFIER, token.getType());
        assertEquals(sb.toString(), token.getValue());
    }

    @Test
    public void testVeryLongNumber() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append('9');
        }
        lexer = new Lexer(sb.toString());
        Token token = lexer.nextToken();
        assertNotNull(token);
        assertEquals(TokenType.NUMBER, token.getType());
        assertEquals(sb.toString(), token.getValue());
    }

    @Test
    public void testMixedWhitespaceCharacters() {
        lexer = new Lexer("a\tb\nc\rd");
        Token t1 = lexer.nextToken();
        assertEquals("a", t1.getValue());
        Token t2 = lexer.nextToken();
        assertEquals("b", t2.getValue());
        Token t3 = lexer.nextToken();
        assertEquals("c", t3.getValue());
        Token t4 = lexer.nextToken();
        assertEquals("d", t4.getValue());
        assertNull(lexer.nextToken());
    }

    @Test
    public void testOnlySeparators() {
        lexer = new Lexer(";.,(){}[]");
        int count = 0;
        while (lexer.nextToken() != null) {
            count++;
        }
        assertEquals("Should have 8 separators", 8, count);
    }

    @Test
    public void testInputWithOnlyOperators() {
        lexer = new Lexer("+-*/%=!&|^<>");
        int count = 0;
        while (lexer.nextToken() != null) {
            count++;
        }
        // Depending on multi-char operators, count may vary
        assertTrue("Should have at least 10 operators", count >= 10);
    }

    @Test
    public void testInputWithOnlyNumbers() {
        lexer = new Lexer("1 2 3 4 5");
        int count = 0;
        while (lexer.nextToken() != null) {
            count++;
        }
        assertEquals("Should have 5 numbers", 5, count);
    }

    @Test
    public void testInputWithOnlyIdentifiers() {
        lexer = new Lexer("a b c d e");
        int count = 0;
        while (lexer.nextToken() != null) {
            count++;
        }
        assertEquals("Should have 5 identifiers", 5, count);
    }

    @Test
    public void testInputWithOnlyStrings() {
        lexer = new Lexer("\"a\" \"b\" \"c\"");
        int count = 0;
        while (lexer.nextToken() != null) {
            count++;
        }
        assertEquals("Should have 3 strings", 3, count);
    }

    @Test
    public void testInputWithCommentsAndTokens() {
        lexer = new Lexer("/* comment */ a // line comment\n b");
        Token t1 = lexer.nextToken();
        assertEquals("a", t1.getValue());
        Token t2 = lexer.nextToken();
        assertEquals("b", t2.getValue());
        assertNull(lexer.nextToken());
    }

    @Test
    public void testNestedComments() {
        // Some lexers may not support nested comments
        lexer = new Lexer("/* outer /* inner */ outer */");
        // Depending on implementation, may tokenize incorrectly
        // We'll just ensure no crash
        Token token = lexer.nextToken();
        // After comment, there should be no tokens if rest is comment
        // But if nested not supported, the first "*/" ends the comment, leaving " outer */"
        // We'll just check that token is null or something
        // This test is to trigger potential bugs
        assertNotNull(lexer.nextToken()); // May be null or token
    }

    @Test
    public void testInputWithUnicodeCharacters() {
        lexer = new Lexer("αβγ");
        Token token = lexer.nextToken();
        assertNotNull(token);
        assertEquals(TokenType.IDENTIFIER, token.getType());
        assertEquals("αβγ", token.getValue());
    }

    @Test
    public void testInputWithOnlyWhitespaceAndComments() {
        lexer = new Lexer("   // comment\n  /* another */  ");
        assertNull("Should return null for whitespace and comments only", lexer.nextToken());
    }

    @Test
    public void testMultipleCallsToNextTokenAfterEOF() {
        lexer = new Lexer("a");
        lexer.nextToken();
        assertNull("First call after EOF should return null", lexer.nextToken());
        assertNull("Second call after EOF should return null", lexer.nextToken());
    }

    @Test
    public void testResetPosition() {
        // If Lexer supports reset, test it; otherwise skip
        // Assuming no reset method, we just test that lexer works after full consumption
        lexer = new Lexer("a b");
        lexer.nextToken();
        lexer.nextToken();
        assertNull(lexer.nextToken());
    }

    // ===================== Helper Methods (if needed) =====================
    // No helper methods required; all tests are self-contained.
}