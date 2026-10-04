package org.jsoup.helper;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for HttpConnection, targeting maximum coverage and fault detection.
 * Focuses on URL encoding (bug #90) and edge cases.
 */
public class HttpConnectionTest {
    private HttpConnection conn;

    @Before
    public void setUp() {
        conn = new HttpConnection();
    }

    // --- encodeUrl tests ---

    @Test
    public void encodeUrl_withoutQuery_returnsSame() {
        String url = conn.encodeUrl("http://example.com/path");
        assertEquals("http://example.com/path", url);
    }

    @Test
    public void encodeUrl_withSpacesInQuery_encodesSpaces() {
        String url = conn.encodeUrl("http://example.com?q=hello world");
        assertEquals("http://example.com?q=hello%20world", url);
    }

    @Test
    public void encodeUrl_withSpecialCharacters_encodesThem() {
        String url = conn.encodeUrl("http://example.com?q=a&b=c d+e=f%g");
        // &, =, space, % should be encoded
        assertEquals("http://example.com?q=a%26b%3Dc%20d%2Be%3Df%25g", url);
    }

    @Test
    public void encodeUrl_withAlreadyEncodedQuery_doesNotDoubleEncode() {
        String url = conn.encodeUrl("http://example.com?q=hello%20world");
        assertEquals("http://example.com?q=hello%20world", url);
    }

    @Test
    public void encodeUrl_withMultipleParameters_encodesAll() {
        String url = conn.encodeUrl("http://example.com?p1=value1&p2=value two&p3=three");
        assertEquals("http://example.com?p1=value1&p2=value%20two&p3=three", url);
    }

    @Test
    public void encodeUrl_withFragment_encodesQueryButNotFragment() {
        String url = conn.encodeUrl("http://example.com?q=hello world#section");
        assertEquals("http://example.com?q=hello%20world#section", url);
    }

    @Test(expected = IllegalArgumentException.class)
    public void encodeUrl_withNull_throwsException() {
        conn.encodeUrl(null);
    }

    @Test
    public void encodeUrl_withEmptyString_returnsEmpty() {
        String url = conn.encodeUrl("");
        assertEquals("", url);
    }

    @Test(expected = IllegalArgumentException.class)
    public void encodeUrl_withNoScheme_throwsException() {
        conn.encodeUrl("example.com/path");
    }

    @Test
    public void encodeUrl_withNonAsciiCharacters_encodes() {
        String url = conn.encodeUrl("http://example.com?q=üñíçödé");
        // Non-ASCII should be percent-encoded
        assertEquals("http://example.com?q=%C3%BC%C3%B1%C3%AD%C3%A7%C3%B6d%C3%A9", url);
    }

    @Test
    public void encodeUrl_withPort_keepsPort() {
        String url = conn.encodeUrl("http://example.com:8080/path?q=test");
        assertEquals("http://example.com:8080/path?q=test", url);
    }

    @Test
    public void encodeUrl_withAuthentication_keepsAuth() {
        String url = conn.encodeUrl("http://user:pass@example.com/path?q=test");
        assertEquals("http://user:pass@example.com/path?q=test", url);
    }

    // --- execute tests (error handling, no network) ---

    @Test(expected = IllegalArgumentException.class)
    public void execute_withNullUrl_throwsException() {
        conn.execute((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void execute_withEmptyUrl_throwsException() {
        conn.execute("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void execute_withInvalidUrl_throwsException() {
        conn.execute("not a url");
    }

    // Additional edge cases for encodeUrl

    @Test
    public void encodeUrl_withQueryContainingEqualsAndAmpersand_encodesCorrectly() {
        String url = conn.encodeUrl("http://example.com?key=value&another=test&special=a=b&c=d");
        // The '=' and '&' in the query values should be encoded
        assertEquals("http://example.com?key=value&another=test&special=a%3Db&c=d", url);
    }

    @Test
    public void encodeUrl_withTrailingSlash_keepsSlash() {
        String url = conn.encodeUrl("http://example.com/");
        assertEquals("http://example.com/", url);
    }

    @Test
    public void encodeUrl_withHttpsScheme_works() {
        String url = conn.encodeUrl("https://example.com?q=test");
        assertEquals("https://example.com?q=test", url);
    }

    @Test
    public void encodeUrl_withQueryOnly_encodes() {
        String url = conn.encodeUrl("http://example.com?q=hello world");
        assertEquals("http://example.com?q=hello%20world", url);
    }

    @Test
    public void encodeUrl_withMultipleSpaces_encodesAll() {
        String url = conn.encodeUrl("http://example.com?q=hello   world");
        assertEquals("http://example.com?q=hello%20%20%20world", url);
    }
}