package org.jsoup.parser;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for TreeBuilderState, targeting bug 17 in Jsoup.
 * Focuses on table parsing states and null current element handling.
 */
public class TreeBuilderStateTest {

    private HtmlTreeBuilder treeBuilder;
    private Token.StartTag startTag;

    @Before
    public void setUp() {
        treeBuilder = new HtmlTreeBuilder();
        // Initialize with a basic document context
        treeBuilder.initialiseParse("<html><body></body></html>", "http://example.com", new Parser(treeBuilder));
        startTag = new Token.StartTag();
    }

    // Helper to process a token in a given state
    private boolean processInState(TreeBuilderState state, Token token) {
        return state.process(token, treeBuilder);
    }

    // --- Tests for InTable state (bug 17 context) ---

    @Test
    public void testInTableProcessStartTagTh() {
        // Simulate a <th> start tag inside a table
        startTag.name = "th";
        startTag.attributes = new Attributes();
        // Ensure current element is null to trigger potential NPE
        treeBuilder.setCurrentElement(null);
        boolean result = processInState(TreeBuilderState.InTable, startTag);
        // The bug caused NPE; after fix, should return false (not processed) or handle gracefully
        // We assert that no exception is thrown and result is false (since th is not handled in InTable)
        assertFalse("Expected false for <th> in InTable when current element is null", result);
    }

    @Test
    public void testInTableProcessStartTagTd() {
        startTag.name = "td";
        startTag.attributes = new Attributes();
        treeBuilder.setCurrentElement(null);
        boolean result = processInState(TreeBuilderState.InTable, startTag);
        assertFalse("Expected false for <td> in InTable when current element is null", result);
    }

    @Test
    public void testInTableProcessStartTagThWithValidParent() {
        // Normal case: current element is a table row
        Element tr = new Element(Tag.valueOf("tr"), "");
        treeBuilder.setCurrentElement(tr);
        startTag.name = "th";
        startTag.attributes = new Attributes();
        boolean result = processInState(TreeBuilderState.InTable, startTag);
        // In normal flow, <th> should cause state transition to InRow or InCell
        // We expect true because it is processed
        assertTrue("Expected true for <th> in InTable with valid parent", result);
    }

    @Test
    public void testInTableProcessStartTagTdWithValidParent() {
        Element tr = new Element(Tag.valueOf("tr"), "");
        treeBuilder.setCurrentElement(tr);
        startTag.name = "td";
        startTag.attributes = new Attributes();
        boolean result = processInState(TreeBuilderState.InTable, startTag);
        assertTrue("Expected true for <td> in InTable with valid parent", result);
    }

    // --- Tests for other states to ensure coverage ---

    @Test
    public void testInBodyProcessStartTagDiv() {
        startTag.name = "div";
        startTag.attributes = new Attributes();
        boolean result = processInState(TreeBuilderState.InBody, startTag);
        assertTrue("Expected true for <div> in InBody", result);
    }

    @Test
    public void testInBodyProcessEndTagDiv() {
        Token.EndTag endTag = new Token.EndTag();
        endTag.name = "div";
        boolean result = processInState(TreeBuilderState.InBody, endTag);
        assertTrue("Expected true for </div> in InBody", result);
    }

    @Test
    public void testInBodyProcessCharacter() {
        Token.Character charToken = new Token.Character();
        charToken.data("text");
        boolean result = processInState(TreeBuilderState.InBody, charToken);
        assertTrue("Expected true for character token in InBody", result);
    }

    @Test
    public void testInTableProcessEndTagTable() {
        Token.EndTag endTag = new Token.EndTag();
        endTag.name = "table";
        boolean result = processInState(TreeBuilderState.InTable, endTag);
        assertTrue("Expected true for </table> in InTable", result);
    }

    @Test
    public void testInTableProcessCharacter() {
        Token.Character charToken = new Token.Character();
        charToken.data("text");
        boolean result = processInState(TreeBuilderState.InTable, charToken);
        // Characters in table are usually reprocessed in foster parenting
        assertTrue("Expected true for character token in InTable", result);
    }

    @Test
    public void testInSelectProcessStartTagOption() {
        startTag.name = "option";
        startTag.attributes = new Attributes();
        boolean result = processInState(TreeBuilderState.InSelect, startTag);
        assertTrue("Expected true for <option> in InSelect", result);
    }

    @Test
    public void testInSelectProcessEndTagSelect() {
        Token.EndTag endTag = new Token.EndTag();
        endTag.name = "select";
        boolean result = processInState(TreeBuilderState.InSelect, endTag);
        assertTrue("Expected true for </select> in InSelect", result);
    }

    // --- Edge cases: null token, unknown state ---

    @Test(expected = NullPointerException.class)
    public void testProcessNullToken() {
        processInState(TreeBuilderState.InBody, null);
    }

    @Test
    public void testProcessUnknownStartTag() {
        startTag.name = "unknown";
        startTag.attributes = new Attributes();
        boolean result = processInState(TreeBuilderState.InBody, startTag);
        // Unknown tags are typically processed as generic elements
        assertTrue("Expected true for unknown start tag in InBody", result);
    }

    @Test
    public void testProcessComment() {
        Token.Comment comment = new Token.Comment();
        comment.data("comment");
        boolean result = processInState(TreeBuilderState.InBody, comment);
        assertTrue("Expected true for comment token in InBody", result);
    }

    @Test
    public void testProcessDoctype() {
        Token.Doctype doctype = new Token.Doctype();
        doctype.name("html");
        boolean result = processInState(TreeBuilderState.InBody, doctype);
        assertTrue("Expected true for doctype token in InBody", result);
    }

    // --- Additional coverage for InTable with other tokens ---

    @Test
    public void testInTableProcessStartTagCaption() {
        startTag.name = "caption";
        startTag.attributes = new Attributes();
        boolean result = processInState(TreeBuilderState.InTable, startTag);
        assertTrue("Expected true for <caption> in InTable", result);
    }

    @Test
    public void testInTableProcessStartTagColgroup() {
        startTag.name = "colgroup";
        startTag.attributes = new Attributes();
        boolean result = processInState(TreeBuilderState.InTable, startTag);
        assertTrue("Expected true for <colgroup> in InTable", result);
    }

    @Test
    public void testInTableProcessStartTagCol() {
        startTag.name = "col";
        startTag.attributes = new Attributes();
        boolean result = processInState(TreeBuilderState.InTable, startTag);
        assertTrue("Expected true for <col> in InTable", result);
    }

    @Test
    public void testInTableProcessStartTagTable() {
        startTag.name = "table";
        startTag.attributes = new Attributes();
        boolean result = processInState(TreeBuilderState.InTable, startTag);
        // Nested table should be handled
        assertTrue("Expected true for <table> in InTable", result);
    }

    @Test
    public void testInTableProcessStartTagTbody() {
        startTag.name = "tbody";
        startTag.attributes = new Attributes();
        boolean result = processInState(TreeBuilderState.InTable, startTag);
        assertTrue("Expected true for <tbody> in InTable", result);
    }

    @Test
    public void testInTableProcessStartTagThead() {
        startTag.name = "thead";
        startTag.attributes = new Attributes();
        boolean result = processInState(TreeBuilderState.InTable, startTag);
        assertTrue("Expected true for <thead> in InTable", result);
    }

    @Test
    public void testInTableProcessStartTagTfoot() {
        startTag.name = "tfoot";
        startTag.attributes = new Attributes();
        boolean result = processInState(TreeBuilderState.InTable, startTag);
        assertTrue("Expected true for <tfoot> in InTable", result);
    }

    @Test
    public void testInTableProcessStartTagTr() {
        startTag.name = "tr";
        startTag.attributes = new Attributes();
        boolean result = processInState(TreeBuilderState.InTable, startTag);
        assertTrue("Expected true for <tr> in InTable", result);
    }

    @Test
    public void testInTableProcessStartTagStyle() {
        startTag.name = "style";
        startTag.attributes = new Attributes();
        boolean result = processInState(TreeBuilderState.InTable, startTag);
        assertTrue("Expected true for <style> in InTable", result);
    }

    @Test
    public void testInTableProcessStartTagScript() {
        startTag.name = "script";
        startTag.attributes = new Attributes();
        boolean result = processInState(TreeBuilderState.InTable, startTag);
        assertTrue("Expected true for <script> in InTable", result);
    }

    @Test
    public void testInTableProcessEndTagBody() {
        Token.EndTag endTag = new Token.EndTag();
        endTag.name = "body";
        boolean result = processInState(TreeBuilderState.InTable, endTag);
        assertFalse("Expected false for </body> in InTable", result);
    }

    @Test
    public void testInTableProcessEndTagHtml() {
        Token.EndTag endTag = new Token.EndTag();
        endTag.name = "html";
        boolean result = processInState(TreeBuilderState.InTable, endTag);
        assertFalse("Expected false for </html> in InTable", result);
    }
}