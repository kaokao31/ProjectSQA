package org.jsoup.helper;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for HttpConnection, targeting maximum coverage and fault detection.
 * Includes a local HTTP server to simulate various response scenarios, including the
 * bug #48 case (empty response body, e.g., HTTP 204 No Content).
 */
public class HttpConnectionTest {

    private HttpServer server;
    private int port;

    @Before
    public void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/success", new SuccessHandler());
        server.createContext("/nocontent", new NoContentHandler());
        server.createContext("/redirect", new RedirectHandler());
        server.setExecutor(null);
        server.start();
        port = server.getAddress().getPort();
    }

    @After
    public void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    // --- Test cases ---

    @Test
    public void testSuccessfulGet() throws IOException {
        Connection.Response response = Jsoup.connect("http://localhost:" + port + "/success")
                .method(Connection.Method.GET)
                .execute();
        assertEquals(200, response.statusCode());
        assertEquals("text/html", response.contentType());
        Document doc = response.parse();
        assertNotNull(doc);
        assertEquals("Hello World", doc.body().text());
    }

    @Test
    public void testNoContentResponse() throws IOException {
        // This test targets the bug #48 scenario: HTTP 204 No Content (empty body)
        Connection.Response response = Jsoup.connect("http://localhost:" + port + "/nocontent")
                .method(Connection.Method.GET)
                .execute();
        assertEquals(204, response.statusCode());
        // The response body should be empty; parsing should not throw an exception
        Document doc = response.parse();
        assertNotNull(doc);
        assertTrue(doc.body().text().isEmpty());
    }

    @Test(expected = MalformedURLException.class)
    public void testMalformedUrl() throws IOException {
        Jsoup.connect("not a valid url").execute();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullUrl() throws IOException {
        Jsoup.connect(null).execute();
    }

    @Test
    public void testPostRequest() throws IOException {
        Connection.Response response = Jsoup.connect("http://localhost:" + port + "/success")
                .method(Connection.Method.POST)
                .data("key", "value")
                .execute();
        assertEquals(200, response.statusCode());
    }

    @Test
    public void testCustomHeaders() throws IOException {
        Connection.Response response = Jsoup.connect("http://localhost:" + port + "/success")
                .header("X-Custom", "test")
                .execute();
        assertEquals(200, response.statusCode());
        // Verify that the header was sent (the handler echoes it back)
        assertEquals("test", response.header("X-Custom-Echo"));
    }

    @Test
    public void testCookies() throws IOException {
        Connection.Response response = Jsoup.connect("http://localhost:" + port + "/success")
                .cookie("session", "abc123")
                .execute();
        assertEquals(200, response.statusCode());
        // The handler echoes the cookie value
        assertEquals("abc123", response.cookie("session"));
    }

    @Test
    public void testTimeout() throws IOException {
        // Set a very short timeout; the server responds quickly, so no timeout expected
        Connection.Response response = Jsoup.connect("http://localhost:" + port + "/success")
                .timeout(5000)
                .execute();
        assertEquals(200, response.statusCode());
    }

    @Test
    public void testFollowRedirects() throws IOException {
        // By default, Jsoup follows redirects. The redirect handler returns 302 to /success.
        Connection.Response response = Jsoup.connect("http://localhost:" + port + "/redirect")
                .followRedirects(true)
                .execute();
        assertEquals(200, response.statusCode());
        assertEquals("Hello World", response.parse().body().text());
    }

    @Test
    public void testIgnoreContentType() throws IOException {
        // Test that we can parse even if content type is not HTML
        Connection.Response response = Jsoup.connect("http://localhost:" + port + "/success")
                .ignoreContentType(true)
                .execute();
        Document doc = response.parse();
        assertNotNull(doc);
    }

    // --- HTTP Handlers ---

    private static class SuccessHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String responseBody = "<html><body>Hello World</body></html>";
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            // Echo custom header if present
            String customHeader = exchange.getRequestHeaders().getFirst("X-Custom");
            if (customHeader != null) {
                exchange.getResponseHeaders().set("X-Custom-Echo", customHeader);
            }
            // Echo cookie if present
            String cookie = exchange.getRequestHeaders().getFirst("Cookie");
            if (cookie != null && cookie.startsWith("session=")) {
                String value = cookie.substring("session=".length());
                exchange.getResponseHeaders().set("Set-Cookie", "session=" + value);
            }
            exchange.sendResponseHeaders(200, responseBody.getBytes(StandardCharsets.UTF_8).length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBody.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    private static class NoContentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Return 204 No Content with no body
            exchange.sendResponseHeaders(204, -1);
        }
    }

    private static class RedirectHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Location", "/success");
            exchange.sendResponseHeaders(302, -1);
        }
    }
}