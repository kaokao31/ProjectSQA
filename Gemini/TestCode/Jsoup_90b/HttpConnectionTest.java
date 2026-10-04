package org.jsoup.helper;

import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class HttpConnectionTest {

    @Test
    public void testUrlConstructorAndGetter() throws IOException {
        String urlString = "http://example.com";
        HttpConnection connection = (HttpConnection) HttpConnection.connect(urlString);
        assertNotNull(connection);
        assertEquals(urlString, connection.url().toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConnectNullString() {
        HttpConnection.connect((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConnectNullUrl() {
        HttpConnection.connect((URL) null);
    }

    @Test
    public void testUserAgent() {
        HttpConnection connection = (HttpConnection) HttpConnection.connect("http://example.com");
        Connection.Request req = connection.request();
        assertNotNull(req.userAgent());
        
        connection.userAgent("CustomAgent");
        assertEquals("CustomAgent", req.userAgent());
        assertEquals("CustomAgent", req.header("User-Agent"));
    }

    @Test
    public void testTimeout() {
        HttpConnection connection = (HttpConnection) HttpConnection.connect("http://example.com");
        Connection.Request req = connection.request();
        
        connection.timeout(5000);
        assertEquals(5000, req.timeout());
    }

    @Test
    public void testMaxBodySize() {
        HttpConnection connection = (HttpConnection) HttpConnection.connect("http://example.com");
        Connection.Request req = connection.request();
        
        connection.maxBodySize(1024);
        assertEquals(1024, req.maxBodySize());
    }

    @Test
    public void testFollowRedirects() {
        HttpConnection connection = (HttpConnection) HttpConnection.connect("http://example.com");
        Connection.Request req = connection.request();
        
        connection.followRedirects(false);
        assertFalse(req.followRedirects());
        
        connection.followRedirects(true);
        assertTrue(req.followRedirects());
    }

    @Test
    public void testMethod() {
        HttpConnection connection = (HttpConnection) HttpConnection.connect("http://example.com");
        Connection.Request req = connection.request();
        
        connection.method(Connection.Method.POST);
        assertEquals(Connection.Method.POST, req.method());
    }

    @Test
    public void testIgnoreHttpErrors() {
        HttpConnection connection = (HttpConnection) HttpConnection.connect("http://example.com");
        Connection.Request req = connection.request();
        
        connection.ignoreHttpErrors(true);
        assertTrue(req.ignoreHttpErrors());
    }

    @Test
    public void testIgnoreContentType() {
        HttpConnection connection = (HttpConnection) HttpConnection.connect("http://example.com");
        Connection.Request req = connection.request();
        
        connection.ignoreContentType(true);
        assertTrue(req.ignoreContentType());
    }

    @Test
    public void testDataHandling() {
        HttpConnection connection = (HttpConnection) HttpConnection.connect("http://example.com");
        connection.data("key1", "value1");
        connection.data("key2", "value2");

        Connection.Request req = connection.request();
        boolean foundKey1 = false;
        for (Connection.KeyVal kv : req.data()) {
            if ("key1".equals(kv.key())) {
                assertEquals("value1", kv.value());
                foundKey1 = true;
            }
        }
        assertTrue(foundKey1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDataNullKey() {
        HttpConnection connection = (HttpConnection) HttpConnection.connect("http://example.com");
        connection.data(null, "value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDataEmptyKey() {
        HttpConnection connection = (HttpConnection) HttpConnection.connect("http://example.com");
        connection.data("", "value");
    }

    @Test
    public void testHeadersHandling() {
        HttpConnection connection = (HttpConnection) HttpConnection.connect("http://example.com");
        connection.header("X-Test", "HeaderValue");
        assertEquals("HeaderValue", connection.header("X-Test"));
        assertTrue(connection.hasHeader("X-Test"));
        
        connection.removeHeader("X-Test");
        assertFalse(connection.hasHeader("X-Test"));
    }

    @Test
    public void testCookieHandling() {
        HttpConnection connection = (HttpConnection) HttpConnection.connect("http://example.com");
        connection.cookie("sessionid", "12345");
        assertEquals("12345", connection.cookie("sessionid"));
        
        connection.removeCookie("sessionid");
        assertEquals("", connection.cookie("sessionid"));
    }

    @Test
    public void testProxyHandling() {
        HttpConnection connection = (HttpConnection) HttpConnection.connect("http://example.com");
        connection.proxy("127.0.0.1", 8080);
        assertNotNull(connection.request().proxy());
    }

    @Test
    public void testRequestDataMap() {
        HttpConnection connection = (HttpConnection) HttpConnection.connect("http://example.com");
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("a", "1");
        dataMap.put("b", "2");
        
        connection.data(dataMap);
        Connection.Request req = connection.request();
        assertEquals(2, req.data().size());
    }

    @Test
    public void testRequestKeyVals() {
        HttpConnection connection = (HttpConnection) HttpConnection.connect("http://example.com");
        Connection.KeyVal kv1 = HttpConnection.KeyVal.create("k1", "v1");
        connection.data(kv1);
        
        assertEquals("k1", kv1.key());
        assertEquals("v1", kv1.value());
        
        kv1.key("k2");
        kv1.value("v2");
        kv1.inputStream(new ByteArrayInputStream(new byte[0]));
        
        assertEquals("k2", kv1.key());
        assertEquals("v2", kv1.value());
        assertNotNull(kv1.inputStream());
    }

    @Test
    public void testResponseProperties() {
        HttpConnection.Response res = new HttpConnection.Response();
        res.charset("UTF-8");
        assertEquals("UTF-8", res.charset());
        
        res.contentType("text/html");
        assertEquals("text/html", res.contentType());
    }

    @Test
    public void testParseEmptyBody() throws IOException {
        HttpConnection.Response res = new HttpConnection.Response();
        res.byteData = new byte[0];
        res.charset("UTF-8");
        res.contentType("text/html");
        
        // This exercises response body parsing branches (Jsoup bug 90 area: charset detection / content type handling)
        org.jsoup.nodes.Document doc = res.parse();
        assertNotNull(doc);
    }
}