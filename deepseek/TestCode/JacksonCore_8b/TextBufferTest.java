package textbuffer;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TextBufferTest {

    private TextBuffer buffer;

    @Before
    public void setUp() {
        buffer = new TextBuffer();
    }

    // Constructor tests
    @Test
    public void testDefaultConstructor() {
        assertEquals("", buffer.toString());
        assertEquals(0, buffer.length());
    }

    @Test
    public void testConstructorWithString() {
        buffer = new TextBuffer("hello");
        assertEquals("hello", buffer.toString());
        assertEquals(5, buffer.length());
    }

    @Test
    public void testConstructorWithNullString() {
        buffer = new TextBuffer(null);
        assertEquals("", buffer.toString());
        assertEquals(0, buffer.length());
    }

    // Append tests
    @Test
    public void testAppendString() {
        buffer.append("abc");
        assertEquals("abc", buffer.toString());
    }

    @Test
    public void testAppendEmptyString() {
        buffer.append("");
        assertEquals("", buffer.toString());
    }

    @Test
    public void testAppendNullString() {
        buffer.append((String) null);
        assertEquals("null", buffer.toString()); // typical behavior
    }

    @Test
    public void testAppendChar() {
        buffer.append('x');
        assertEquals("x", buffer.toString());
    }

    @Test
    public void testAppendMultiple() {
        buffer.append("a").append("b").append("c");
        assertEquals("abc", buffer.toString());
    }

    // Insert tests
    @Test
    public void testInsertAtBeginning() {
        buffer.append("world");
        buffer.insert(0, "hello ");
        assertEquals("hello world", buffer.toString());
    }

    @Test
    public void testInsertAtEnd() {
        buffer.append("hello");
        buffer.insert(5, " world");
        assertEquals("hello world", buffer.toString());
    }

    @Test
    public void testInsertInMiddle() {
        buffer.append("helorld");
        buffer.insert(3, "lo w");
        assertEquals("hello world", buffer.toString());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testInsertNegativeIndex() {
        buffer.insert(-1, "test");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testInsertIndexTooLarge() {
        buffer.append("abc");
        buffer.insert(4, "d");
    }

    @Test
    public void testInsertNullString() {
        buffer.append("abc");
        buffer.insert(1, null);
        assertEquals("anullbc", buffer.toString()); // typical behavior
    }

    // Delete tests
    @Test
    public void testDeleteRange() {
        buffer.append("hello world");
        buffer.delete(5, 6); // remove space
        assertEquals("helloworld", buffer.toString());
    }

    @Test
    public void testDeleteEntireString() {
        buffer.append("test");
        buffer.delete(0, 4);
        assertEquals("", buffer.toString());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testDeleteNegativeStart() {
        buffer.append("abc");
        buffer.delete(-1, 2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testDeleteEndGreaterThanLength() {
        buffer.append("abc");
        buffer.delete(1, 5);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testDeleteStartGreaterThanEnd() {
        buffer.append("abc");
        buffer.delete(2, 1);
    }

    // Length tests
    @Test
    public void testLengthAfterAppend() {
        buffer.append("12345");
        assertEquals(5, buffer.length());
    }

    @Test
    public void testLengthAfterDelete() {
        buffer.append("12345");
        buffer.delete(1, 3);
        assertEquals(3, buffer.length());
    }

    // toString tests
    @Test
    public void testToStringEmpty() {
        assertEquals("", buffer.toString());
    }

    @Test
    public void testToStringAfterMultipleOperations() {
        buffer.append("abc");
        buffer.insert(1, "123");
        buffer.delete(0, 1);
        assertEquals("123bc", buffer.toString());
    }

    // charAt tests
    @Test
    public void testCharAtValidIndex() {
        buffer.append("hello");
        assertEquals('e', buffer.charAt(1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testCharAtNegativeIndex() {
        buffer.append("abc");
        buffer.charAt(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testCharAtIndexEqualToLength() {
        buffer.append("abc");
        buffer.charAt(3);
    }

    // substring tests
    @Test
    public void testSubstringValidRange() {
        buffer.append("hello world");
        assertEquals("lo wo", buffer.substring(3, 8));
    }

    @Test
    public void testSubstringFullRange() {
        buffer.append("test");
        assertEquals("test", buffer.substring(0, 4));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSubstringNegativeStart() {
        buffer.append("abc");
        buffer.substring(-1, 2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSubstringEndGreaterThanLength() {
        buffer.append("abc");
        buffer.substring(1, 5);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSubstringStartGreaterThanEnd() {
        buffer.append("abc");
        buffer.substring(2, 1);
    }

    // Edge case: large buffer
    @Test
    public void testLargeAppend() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("a");
        }
        String large = sb.toString();
        buffer.append(large);
        assertEquals(1000, buffer.length());
        assertEquals(large, buffer.toString());
    }

    // Edge case: insert at boundary after append
    @Test
    public void testInsertAtLength() {
        buffer.append("abc");
        buffer.insert(3, "d");
        assertEquals("abcd", buffer.toString());
    }

    // Edge case: delete empty range
    @Test
    public void testDeleteEmptyRange() {
        buffer.append("abc");
        buffer.delete(1, 1); // no change
        assertEquals("abc", buffer.toString());
    }

    // Edge case: append char after delete
    @Test
    public void testAppendAfterDelete() {
        buffer.append("hello");
        buffer.delete(0, 5);
        buffer.append('!');
        assertEquals("!", buffer.toString());
    }

    // Edge case: null char append (if method exists)
    // Not applicable, char cannot be null

    // Edge case: insert at index 0 when buffer is empty
    @Test
    public void testInsertIntoEmptyBuffer() {
        buffer.insert(0, "first");
        assertEquals("first", buffer.toString());
    }

    // Edge case: multiple inserts and deletes
    @Test
    public void testComplexSequence() {
        buffer.append("abcdef");
        buffer.delete(2, 4); // "abef"
        buffer.insert(2, "cd"); // "abcdef"
        buffer.append("gh"); // "abcdefgh"
        buffer.delete(0, 8); // ""
        assertEquals("", buffer.toString());
    }
}