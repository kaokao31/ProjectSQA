package org.jsoup.helper;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.Assert;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;

public class HttpConnectionTest {

    private String originalProtocolHandlers;

    @Before
    public void setUp() {
        originalProtocolHandlers = System.getProperty("java.protocol.handler.pkgs");
    }

    @After
    public void tearDown() {
        if (originalProtocolHandlers != null) {
            System.setProperty("java.protocol.handler.pkgs", originalProtocolHandlers);
        } else {
            System.clearProperty("java.protocol.handler.pkgs");
        }
    }

    @Test
    public void testConnectString() {
        Connection con = HttpConnection.connect("http://example.com");
        Assert.assertNotNull(con);
        Assert.assertEquals("http://example.com", con.request().url().toString());
    }

    @Test
    public void testConnectURL() throws MalformedURLException {
        URL url = new URL("http://example.com");
        Connection con = HttpConnection.connect(url);
        Assert.assertNotNull(con);
        Assert.assertEquals(url, con.request().url());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConnectNullString() {
        HttpConnection.connect((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConnectNullURL() {
        HttpConnection.connect((URL) null);
    }

    @Test
    public void testRequestAndResponseSettersGetters() {
        HttpConnection.Request req = new HttpConnection.Request();
        req.url(null);
        Assert.assertNull(req.url());

        req.method(Method.POST);
        Assert.assertEquals(Method.POST, req.method());

        req.timeout(5000);
        Assert.assertEquals(5000, req.timeout());

        req.maxBodySize(1024);
        Assert.assertEquals(1024, req.maxBodySize());

        req.followRedirects(false);
        Assert.assertFalse(req.followRedirects());

        req.ignoreHttpErrors(true);
        Assert.assertTrue(req.ignoreHttpErrors());

        req.parser(null);
        Assert.assertNull(req.parser());

        req.postDataCharset("UTF-8");
        Assert.assertEquals("UTF-8", req.postDataCharset());

        Assert.assertFalse(req.data().iterator().hasNext());
    }

    @Test
    public void testRequestHeaders() {
        HttpConnection.Request req = new HttpConnection.Request();
        req.header("Test-Header", "Value1");
        Assert.assertEquals("Value1", req.header("Test-Header"));
        Assert.assertTrue(req.hasHeader("Test-Header"));

        req.removeHeader("Test-Header");
        Assert.assertFalse(req.hasHeader("Test-Header"));

        req.cookie("Cookie1", "Val1");
        Assert.assertEquals("Val1", req.cookie("Cookie1"));
        Assert.assertTrue(req.hasCookie("Cookie1"));

        req.removeCookie("Cookie1");
        Assert.assertFalse(req.hasCookie("Cookie1"));
    }

    @Test
    public void testResponseProperties() throws IOException {
        HttpConnection.Response res = new HttpConnection.Response();
        res.charset("UTF-8");
        Assert.assertEquals("UTF-8", res.charset());

        res.contentType("text/html");
        Assert.assertEquals("text/html", res.contentType());

        res.statusCode();
        res.statusMessage();

        Assert.assertNull(res.body());
        Assert.assertNull(res.bodyAsBytes());
        
        try {
            res.parse();
            Assert.fail("Expected IOException due to no body/connection");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void testResponseProcessResponseHeadersMultipleValues() {
        // Specifically targeting multiple headers with the same name (Jsoup 48 bug scenario)
        HttpConnection.Response res = new HttpConnection.Response();
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Set-Cookie", java.util.Arrays.asList("a=1", "b=2"));
        headers.put(null, java.util.Arrays.asList("null-value")); // test null header key handling
        headers.put("Cache-Control", java.util.Arrays.asList("no-cache"));

        res.processResponseHeaders(headers);

        Assert.assertEquals("a=1, b=2", res.cookie("a"));
        // Depending on implementation, multiple headers might be joined or handled.
        // Let's test standard header retrieval.
        Assert.assertEquals("no-cache", res.header("Cache-Control"));
    }

    @Test
    public void testKeyValCreation() {
        Connection.KeyVal kv = HttpConnection.KeyVal.create("key", "val");
        Assert.assertEquals("key", kv.key());
        Assert.assertEquals("val", kv.value());

        kv.key("newKey");
        kv.value("newVal");
        kv.inputStream(null);

        Assert.assertEquals("newKey", kv.key());
        Assert.assertEquals("newVal", kv.value());
        Assert.assertNull(kv.inputStream());
        
        Assert.assertEquals("newKey=newVal", kv.toString());
    }

    @Test
    public void testRequestDataHandling() {
        HttpConnection.Request req = new HttpConnection.Request();
        Connection.KeyVal kv = HttpConnection.KeyVal.create("testKey", "testVal");
        req.data(kv);
        
        boolean found = false;
        for (Connection.KeyVal keyVal : req.data()) {
            if ("testKey".equals(keyVal.key())) {
                found = true;
                break;
            }
        }
        Assert.assertTrue(found);
    }
}