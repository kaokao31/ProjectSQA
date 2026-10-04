package org.jsoup.nodes;

import org.jsoup.Connection;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class FormElementTest {

    @Test
    public void testControlAddAndElementsList() {
        // Test parsing / creating a form with elements and adding a control
        String html = "<form><input name=\"q\" value=\"search\"></form>";
        Document doc = Document.parse(html);
        FormElement form = doc.select("form").first();

        assertNotNull(form);
        assertEquals(1, form.elements().size());

        // Test adding an element (control)
        Element div = new Element(Tag.valueOf("div"), "");
        Tag inputTag = Tag.valueOf("input");
        Attributes inputAttrs = new Attributes();
        inputAttrs.put("name", "foo");
        inputAttrs.put("value", "bar");
        Element input = new Element(inputTag, "", inputAttrs);

        form.addElement(input);
        assertEquals(2, form.elements().size());
        assertTrue(form.elements().contains(input));
    }

    @Test
    public void testSubmitDataNormal() {
        // Test submit data extraction with typical form elements
        String html = "<form>" +
                "<input name=\"user\" value=\"john\">" +
                "<input name=\"pass\" value=\"secret\">" +
                "<input type=\"checkbox\" name=\"remember\" value=\"on\" checked>" +
                "<input type=\"checkbox\" name=\"offbox\" value=\"on\">" + // not checked, should be ignored
                "<input type=\"radio\" name=\"gender\" value=\"m\" checked>" +
                "<input type=\"radio\" name=\"gender\" value=\"f\">" + // not checked, should be ignored
                "<select name=\"city\">" +
                  "<option value=\"ny\" selected>New York</option>" +
                  "<option value=\"la\">Los Angeles</option>" +
                "</select>" +
                "<textarea name=\"notes\">some notes</textarea>" +
                "<input type=\"submit\" name=\"sub\" value=\"Submit\">" +
                "</form>";

        Document doc = Document.parse(html);
        FormElement form = doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();
        
        // Expected parameters:
        // user=john
        // pass=secret
        // remember=on
        // gender=m
        // city=ny
        // notes=some notes
        // sub=Submit (Wait, does submit button get included? Let's verify Jsoup behavior)

        assertFalse(data.isEmpty());
        boolean foundUser = false;
        boolean foundRemember = false;
        boolean foundCity = false;
        boolean foundNotes = false;

        for (Connection.KeyVal kv : data) {
            if ("user".equals(kv.key())) {
                assertEquals("john", kv.value());
                foundUser = true;
            } else if ("remember".equals(kv.key())) {
                assertEquals("on", kv.value());
                foundRemember = true;
            } else if ("city".equals(kv.key())) {
                assertEquals("ny", kv.value());
                foundCity = true;
            } else if ("notes".equals(kv.key())) {
                assertEquals("some notes", kv.value());
                foundNotes = true;
            } else if ("offbox".equals(kv.key())) {
                fail("Unchecked box should not be submitted");
            }
        }

        assertTrue(foundUser);
        assertTrue(foundRemember);
        assertTrue(foundCity);
        assertTrue(foundNotes);
    }

    @Test
    public void testSubmitWithoutNameAttribute() {
        // Elements without a 'name' attribute should be ignored in formData
        String html = "<form>" +
                "<input value=\"no-name-val\">" +
                "<input name=\"has-name\" value=\"has-name-val\">" +
                "</form>";

        Document doc = Document.parse(html);
        FormElement form = doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("has-name", data.get(0).key());
        assertEquals("has-name-val", data.get(0).value());
    }

    @Test
    public void testSubmitDisabledElement() {
        // Disabled elements should be ignored
        String html = "<form>" +
                "<input name=\"enabled\" value=\"1\">" +
                "<input name=\"disabled\" value=\"2\" disabled>" +
                "</form>";

        Document doc = Document.parse(html);
        FormElement form = doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("enabled", data.get(0).key());
    }

    @Test
    public void testSubmitSelectWithoutOption() {
        // Select element with no options selected
        String html = "<form>" +
                "<select name=\"sel\">" +
                  "<option value=\"a\">A</option>" +
                "</select>" +
                "</form>";

        Document doc = Document.parse(html);
        FormElement form = doc.select("form").first();

        // If no option is marked 'selected', Jsoup typically picks the first option.
        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("sel", data.get(0).key());
        assertEquals("a", data.get(0).value());
    }

    @Test
    public void testSubmitMultipleSelectOptions() {
        // Select with multiple options or multiple attribute if supported, or multiple selected options
        String html = "<form>" +
                "<select name=\"sel\" multiple>" +
                  "<option value=\"a\" selected>A</option>" +
                  "<option value=\"b\" selected>B</option>" +
                "</select>" +
                "</form>";

        Document doc = Document.parse(html);
        FormElement form = doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertEquals(2, data.size());
        assertEquals("sel", data.get(0).key());
        assertEquals("a", data.get(0).value());
        assertEquals("sel", data.get(1).key());
        assertEquals("b", data.get(1).value());
    }

    @Test
    public void testSubmitActionResolution() {
        // Test form submission action URL resolution
        String html = "<form action=\"/submit.php\"><input name=\"x\"></form>";
        Document doc = Document.parse(html, "http://example.com/path/page.html");
        FormElement form = doc.select("form").first();

        Connection con = form.submit();
        assertNotNull(con);
        assertEquals("http://example.com/submit.php", con.url().toExternalForm());
    }

    @Test
    public void testSubmitActionEmpty() {
        // Test form submission when action attribute is empty or missing
        String html = "<form><input name=\"x\"></form>";
        Document doc = Document.parse(html, "http://example.com/path/page.html?query=1");
        FormElement form = doc.select("form").first();

        Connection con = form.submit();
        assertNotNull(con);
        // Should resolve to base URI
        assertEquals("http://example.com/path/page.html", con.url().toExternalForm());
    }
}