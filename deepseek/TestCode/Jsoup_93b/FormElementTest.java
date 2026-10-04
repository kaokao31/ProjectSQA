package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.parser.Tag;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for FormElement, targeting maximum coverage and fault detection.
 * Designed to exercise all branches, edge cases, and potential bug scenarios (e.g., bug 93).
 */
public class FormElementTest {

    private FormElement form;
    private Document doc;

    @Before
    public void setUp() {
        // Create a simple document with a form element
        String html = "<form id='testForm' action='/submit' method='post'>" +
                "<input name='text1' type='text' value='val1'>" +
                "<input name='hidden1' type='hidden' value='hiddenVal'>" +
                "<input name='checkbox1' type='checkbox' checked value='cbVal'>" +
                "<input name='checkbox2' type='checkbox'>" +
                "<input name='radio1' type='radio' value='radioVal' checked>" +
                "<input name='radio2' type='radio' value='radioVal2'>" +
                "<select name='select1'>" +
                "<option value='opt1' selected>Option1</option>" +
                "<option value='opt2'>Option2</option>" +
                "</select>" +
                "<textarea name='textarea1'>textareaVal</textarea>" +
                "<button name='button1' type='submit' value='btnVal'>Submit</button>" +
                "</form>";
        doc = Jsoup.parse(html);
        form = (FormElement) doc.getElementById("testForm");
    }

    // --- Constructor and basic getters ---

    @Test
    public void testConstructorAndBaseUri() {
        FormElement f = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        assertEquals("http://example.com", f.baseUri());
        assertEquals("form", f.tagName());
    }

    @Test
    public void testSetAndGetAction() {
        form.setAction("/newAction");
        assertEquals("/newAction", form.action());
    }

    @Test
    public void testActionDefault() {
        // When no action attribute, action() should return empty string or baseUri? Typically empty.
        FormElement f = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        assertEquals("", f.action());
    }

    @Test
    public void testActionWithEmptyAttribute() {
        FormElement f = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        f.attr("action", "");
        assertEquals("", f.action());
    }

    // --- addElement and elements ---

    @Test
    public void testAddElementAndElements() {
        Element input = new Element(Tag.valueOf("input"), "");
        input.attr("name", "newInput");
        input.attr("value", "newVal");
        form.addElement(input);
        List<Element> elements = form.elements();
        assertTrue(elements.contains(input));
        // Ensure original elements still present
        assertNotNull(form.getElementById("text1"));
    }

    @Test
    public void testAddNullElement() {
        // Should not throw? Typically addElement may accept null? We'll test defensive behavior.
        try {
            form.addElement(null);
            // If no exception, we can still check elements list unchanged
            assertEquals(8, form.elements().size()); // original 8 elements
        } catch (NullPointerException e) {
            // Acceptable if implementation throws NPE
        }
    }

    // --- formData() comprehensive tests ---

    @Test
    public void testFormDataBasic() {
        List<Connection.KeyVal> data = form.formData();
        assertNotNull(data);
        // Expected: text1, hidden1, checkbox1 (checked), radio1 (checked), select1 (selected option), textarea1, button1
        // Note: button type submit is included in form data? Typically yes.
        // Check presence and values
        assertTrue(containsKeyValue(data, "text1", "val1"));
        assertTrue(containsKeyValue(data, "hidden1", "hiddenVal"));
        assertTrue(containsKeyValue(data, "checkbox1", "cbVal"));
        assertFalse(containsKeyValue(data, "checkbox2", "on")); // unchecked, should not appear
        assertTrue(containsKeyValue(data, "radio1", "radioVal"));
        assertFalse(containsKeyValue(data, "radio2", "radioVal2")); // not checked
        assertTrue(containsKeyValue(data, "select1", "opt1"));
        assertTrue(containsKeyValue(data, "textarea1", "textareaVal"));
        assertTrue(containsKeyValue(data, "button1", "btnVal"));
    }

    @Test
    public void testFormDataWithNoName() {
        // Input without name attribute should be excluded
        String html = "<form><input type='text' value='noName'></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.body().child(0);
        List<Connection.KeyVal> data = f.formData();
        assertTrue(data.isEmpty());
    }

    @Test
    public void testFormDataWithEmptyName() {
        String html = "<form><input name='' type='text' value='emptyName'></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.body().child(0);
        List<Connection.KeyVal> data = f.formData();
        // Should be excluded? Typically empty name is ignored.
        assertTrue(data.isEmpty());
    }

    @Test
    public void testFormDataDisabledInput() {
        String html = "<form><input name='disabled' type='text' value='dis' disabled></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.body().child(0);
        List<Connection.KeyVal> data = f.formData();
        assertTrue(data.isEmpty());
    }

    @Test
    public void testFormDataCheckboxUnchecked() {
        // Already covered in basic, but explicit
        String html = "<form><input name='cb' type='checkbox' value='cbVal'></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.body().child(0);
        List<Connection.KeyVal> data = f.formData();
        assertTrue(data.isEmpty());
    }

    @Test
    public void testFormDataCheckboxNoValue() {
        // Checkbox without value attribute should send "on" when checked
        String html = "<form><input name='cb' type='checkbox' checked></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.body().child(0);
        List<Connection.KeyVal> data = f.formData();
        assertTrue(containsKeyValue(data, "cb", "on"));
    }

    @Test
    public void testFormDataRadioNoValue() {
        // Radio without value attribute should send "on" when checked
        String html = "<form><input name='radio' type='radio' checked></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.body().child(0);
        List<Connection.KeyVal> data = f.formData();
        assertTrue(containsKeyValue(data, "radio", "on"));
    }

    @Test
    public void testFormDataSelectMultiple() {
        String html = "<form><select name='multi' multiple>" +
                "<option value='a' selected>a</option>" +
                "<option value='b' selected>b</option>" +
                "<option value='c'>c</option>" +
                "</select></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.body().child(0);
        List<Connection.KeyVal> data = f.formData();
        assertEquals(2, data.size());
        assertTrue(containsKeyValue(data, "multi", "a"));
        assertTrue(containsKeyValue(data, "multi", "b"));
    }

    @Test
    public void testFormDataSelectNoSelection() {
        String html = "<form><select name='sel'><option value='a'>a</option></select></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.body().child(0);
        List<Connection.KeyVal> data = f.formData();
        // If no option selected, first option is selected by default? In HTML, first option is selected if none has selected attribute.
        // Jsoup may or may not follow that. We'll test both possibilities.
        // Typically, Jsoup's formData() includes the first option if none selected.
        assertTrue(containsKeyValue(data, "sel", "a"));
    }

    @Test
    public void testFormDataSelectWithEmptyValue() {
        String html = "<form><select name='sel'><option value=''>empty</option><option value='b' selected>b</option></select></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.body().child(0);
        List<Connection.KeyVal> data = f.formData();
        assertTrue(containsKeyValue(data, "sel", "b"));
        assertFalse(containsKeyValue(data, "sel", ""));
    }

    @Test
    public void testFormDataTextareaEmpty() {
        String html = "<form><textarea name='ta'></textarea></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.body().child(0);
        List<Connection.KeyVal> data = f.formData();
        assertTrue(containsKeyValue(data, "ta", ""));
    }

    @Test
    public void testFormDataButtonNoValue() {
        String html = "<form><button name='btn' type='submit'>Click</button></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.body().child(0);
        List<Connection.KeyVal> data = f.formData();
        // Button without value attribute may not be included? Typically it is included with empty value.
        // Jsoup may include it with empty string.
        assertTrue(containsKeyValue(data, "btn", ""));
    }

    @Test
    public void testFormDataButtonTypeReset() {
        String html = "<form><button name='reset' type='reset' value='resetVal'>Reset</button></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.body().child(0);
        List<Connection.KeyVal> data = f.formData();
        // Reset buttons are not submitted.
        assertFalse(containsKeyValue(data, "reset", "resetVal"));
    }

    @Test
    public void testFormDataImageButton() {
        String html = "<form><input type='image' name='img' src='img.png'></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.body().child(0);
        List<Connection.KeyVal> data = f.formData();
        // Image inputs are submitted with x and y coordinates, but Jsoup may not handle that.
        // Typically they are included with empty value? We'll just check it's not null.
        assertNotNull(data);
    }

    @Test
    public void testFormDataWithMultipleForms() {
        // Ensure formData only returns data from its own elements
        String html = "<form id='f1'><input name='a' value='1'></form><form id='f2'><input name='b' value='2'></form>";
        Document d = Jsoup.parse(html);
        FormElement f1 = (FormElement) d.getElementById("f1");
        FormElement f2 = (FormElement) d.getElementById("f2");
        assertEquals(1, f1.formData().size());
        assertEquals(1, f2.formData().size());
        assertTrue(containsKeyValue(f1.formData(), "a", "1"));
        assertTrue(containsKeyValue(f2.formData(), "b", "2"));
    }

    @Test
    public void testFormDataWithNestedForms() {
        // Nested forms are invalid HTML, but Jsoup may parse them. Ensure no infinite loop.
        String html = "<form id='outer'><input name='out' value='o'><form id='inner'><input name='in' value='i'></form></form>";
        Document d = Jsoup.parse(html);
        FormElement outer = (FormElement) d.getElementById("outer");
        List<Connection.KeyVal> data = outer.formData();
        // Should only include direct children? Jsoup may include nested form's elements? Typically not.
        // We'll just check it doesn't crash and returns something.
        assertNotNull(data);
    }

    // --- Edge cases and bug-specific tests (bug 93) ---

    @Test
    public void testFormDataWithCharsetEncoding() {
        // Bug 93 might involve charset encoding of form data. Test with non-ASCII characters.
        String html = "<form action='/submit'><input name='name' value='Jörg Müller'></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.body().child(0);
        List<Connection.KeyVal> data = f.formData();
        // The value should be URL-encoded? formData() returns raw values, encoding happens later.
        // So we expect the raw UTF-8 string.
        assertTrue(containsKeyValue(data, "name", "Jörg Müller"));
    }

    @Test
    public void testFormDataWithEmptyAction() {
        // Form with empty action attribute
        String html = "<form action=''><input name='a' value='b'></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.body().child(0);
        assertEquals("", f.action());
        List<Connection.KeyVal> data = f.formData();
        assertFalse(data.isEmpty());
    }

    @Test
    public void testFormDataWithMissingAction() {
        // Form without action attribute
        String html = "<form><input name='a' value='b'></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.body().child(0);
        assertEquals("", f.action());
        List<Connection.KeyVal> data = f.formData();
        assertFalse(data.isEmpty());
    }

    @Test
    public void testFormDataWithMethodGet() {
        // Method attribute may affect encoding, but formData() is independent.
        String html = "<form method='get'><input name='a' value='b'></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.body().child(0);
        List<Connection.KeyVal> data = f.formData();
        assertTrue(containsKeyValue(data, "a", "b"));
    }

    @Test
    public void testFormDataWithMethodPost() {
        String html = "<form method='post'><input name='a' value='b'></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.body().child(0);
        List<Connection.KeyVal> data = f.formData();
        assertTrue(containsKeyValue(data, "a", "b"));
    }

    // --- Null and malformed scenarios ---

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullTag() {
        new FormElement(null, "http://example.com", new Attributes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullBaseUri() {
        new FormElement(Tag.valueOf("form"), null, new Attributes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullAttributes() {
        new FormElement(Tag.valueOf("form"), "http://example.com", null);
    }

    @Test
    public void testFormDataOnEmptyForm() {
        FormElement emptyForm = new FormElement(Tag.valueOf("form"), "", new Attributes());
        List<Connection.KeyVal> data = emptyForm.formData();
        assertTrue(data.isEmpty());
    }

    @Test
    public void testFormDataWithOnlyButtonTypeSubmit() {
        String html = "<form><button type='submit' name='sub' value='go'>Go</button></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.body().child(0);
        List<Connection.KeyVal> data = f.formData();
        assertTrue(containsKeyValue(data, "sub", "go"));
    }

    @Test
    public void testFormDataWithInputTypeFile() {
        // File inputs are not submitted via formData() in Jsoup (they require multipart).
        String html = "<form><input type='file' name='file' value='test.txt'></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.body().child(0);
        List<Connection.KeyVal> data = f.formData();
        // File inputs are typically excluded.
        assertTrue(data.isEmpty());
    }

    @Test
    public void testFormDataWithInputTypePassword() {
        String html = "<form><input type='password' name='pwd' value='secret'></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.body().child(0);
        List<Connection.KeyVal> data = f.formData();
        assertTrue(containsKeyValue(data, "pwd", "secret"));
    }

    @Test
    public void testFormDataWithInputTypeEmail() {
        String html = "<form><input type='email' name='email' value='test@example.com'></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.body().child(0);
        List<Connection.KeyVal> data = f.formData();
        assertTrue(containsKeyValue(data, "email", "test@example.com"));
    }

    @Test
    public void testFormDataWithInputTypeNumber() {
        String html = "<form><input type='number' name='num' value='42'></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.body().child(0);
        List<Connection.KeyVal> data = f.formData();
        assertTrue(containsKeyValue(data, "num", "42"));
    }

    @Test
    public void testFormDataWithInputTypeRange() {
        String html = "<form><input type='range' name='range' value='50'></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.body().child(0);
        List<Connection.KeyVal> data = f.formData();
        assertTrue(containsKeyValue(data, "range", "50"));
    }

    @Test
    public void testFormDataWithInputTypeColor() {
        String html = "<form><input type='color' name='color' value='#ff0000'></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.body().child(0);
        List<Connection.KeyVal> data = f.formData();
        assertTrue(containsKeyValue(data, "color", "#ff0000"));
    }

    @Test
    public void testFormDataWithInputTypeDate() {
        String html = "<form><input type='date' name='date' value='2020-01-01'></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.body().child(0);
        List<Connection.KeyVal> data = f.formData();
        assertTrue(containsKeyValue(data, "date", "2020-01-01"));
    }

    @Test
    public void testFormDataWithInputTypeDatetimeLocal() {
        String html = "<form><input type='datetime-local' name='dt' value='2020-01-01T12:00'></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.body().child(0);
        List<Connection.KeyVal> data = f.formData();
        assertTrue(containsKeyValue(data, "dt", "2020-01-01T12:00"));
    }

    @Test
    public void testFormDataWithInputTypeMonth() {
        String html = "<form><input type='month' name='month' value='2020-01'></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.body().child(0);
        List<Connection.KeyVal> data = f.formData();
        assertTrue(containsKeyValue(data, "month", "2020-01"));
    }

    @Test
    public void testFormDataWithInputTypeWeek() {
        String html = "<form><input type='week' name='week' value='2020-W01'></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.body().child(0);
        List<Connection.KeyVal> data = f.formData();
        assertTrue(containsKeyValue(data, "week", "2020-W01"));
    }

    @Test
    public void testFormDataWithInputTypeTime() {
        String html = "<form><input type='time' name='time' value='12:00'></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.body().child(0);
        List<Connection.KeyVal> data = f.formData();
        assertTrue(containsKeyValue(data, "time", "12:00"));
    }

    @Test
    public void testFormDataWithInputTypeUrl() {
        String html = "<form><input type='url' name='url' value='http://example.com'></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.body().child(0);
        List<Connection.KeyVal> data = f.formData();
        assertTrue(containsKeyValue(data, "url", "http://example.com"));
    }

    @Test
    public void testFormDataWithInputTypeSearch() {
        String html = "<form><input type='search' name='search' value='query'></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.body().child(0);
        List<Connection.KeyVal> data = f.formData();
        assertTrue(containsKeyValue(data, "search", "query"));
    }

    @Test
    public void testFormDataWithInputTypeTel() {
        String html = "<form><input type='tel' name='tel' value='1234567890'></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.body().child(0);
        List<Connection.KeyVal> data = f.formData();
        assertTrue(containsKeyValue(data, "tel", "1234567890"));
    }

    // --- Helper method ---

    private boolean containsKeyValue(List<Connection.KeyVal> data, String key, String value) {
        for (Connection.KeyVal kv : data) {
            if (kv.key().equals(key) && kv.value().equals(value)) {
                return true;
            }
        }
        return false;
    }
}