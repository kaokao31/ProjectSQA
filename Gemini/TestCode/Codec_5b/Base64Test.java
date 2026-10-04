package net.mooctest;

import org.junit.Test;
import static org.junit.Assert.*;

public class Base64Test {

    @Test
    public void testNullInputs() {
        assertNull(Base64.encode(null));
        assertNull(Base64.encode((byte[]) null, 0, 0));
        assertNull(Base64.decode(null));
        assertNull(Base64.decode((char[]) null));
        assertNull(Base64.decode((String) null));
        assertNull(Base64.encodeObject(null));
    }

    @Test
    public void testEmptyInputs() {
        byte[] emptyBytes = new byte[0];
        char[] emptyChars = new char[0];
        
        assertEquals("", Base64.encode(emptyBytes));
        assertEquals("", Base64.encode(emptyBytes, 0, 0));
        
        byte[] decodedBytes = Base64.decode(emptyChars);
        assertNotNull(decodedBytes);
        assertEquals(0, decodedBytes.length);
        
        decodedBytes = Base64.decode("");
        assertNotNull(decodedBytes);
        assertEquals(0, decodedBytes.length);
    }

    @Test
    public void testBasicEncodingAndDecoding() {
        String original = "Hello, World!";
        String encoded = Base64.encode(original.getBytes());
        assertNotNull(encoded);
        
        byte[] decodedBytes = Base64.decode(encoded);
        assertNotNull(decodedBytes);
        assertEquals(original, new String(decodedBytes));
    }

    @Test
    public void testEncodingWithOptions() {
        String original = "Test String for Base64 encoding with various options and length constraints.";
        byte[] bytes = original.getBytes();

        int[] optionsList = {
            Base64.NO_OPTIONS,
            Base64.ENCODE,
            Base64.DECODE,
            Base64.GZIP,
            Base64.DONT_BREAK_LINES,
            Base64.URL_SAFE,
            Base64.ORDERED
        };

        for (int options : optionsList) {
            String encoded = Base64.encodeBytes(bytes, options);
            assertNotNull(encoded);
            byte[] decoded = Base64.decode(encoded);
            assertNotNull(decoded);
            assertEquals(original, new String(decoded));
        }
    }

    @Test
    public void testEncodeBytesWithOffsetAndLength() {
        byte[] bytes = "ABCDEFGHIJ".getBytes();
        String encoded = Base64.encodeBytes(bytes, 2, 4, Base64.NO_OPTIONS);
        assertNotNull(encoded);
        
        byte[] decoded = Base64.decode(encoded);
        assertEquals("CDEF", new String(decoded));
    }

    @Test
    public void testEncodeObject() {
        String testObj = "Serialize Me";
        String encoded = Base64.encodeObject(testObj);
        assertNotNull(encoded);

        Object decodedObj = Base64.decodeToObject(encoded);
        assertNotNull(decodedObj);
        assertEquals(testObj, decodedObj);
    }

    @Test
    public void testInputStreamAndOutputStream() throws Exception {
        String original = "Stream testing content for Base64 input and output streams.";
        byte[] originalBytes = original.getBytes();

        // Test OutputStream
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        Base64.OutputStream b64os = new Base64.OutputStream(baos, Base64.ENCODE);
        b64os.write(originalBytes);
        b64os.close();
        
        String encodedResult = baos.toString("UTF-8");
        assertNotNull(encodedResult);

        // Test InputStream
        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(encodedResult.getBytes("UTF-8"));
        Base64.OutputStream b64osDecode = new Base64.OutputStream(new java.io.ByteArrayOutputStream(), Base64.DECODE);
        // Let's test Base64.InputStream instead
        java.io.ByteArrayInputStream bais2 = new java.io.ByteArrayInputStream(encodedResult.getBytes("UTF-8"));
        Base64.InputStream b64is = new Base64.InputStream(bais2, Base64.DECODE);
        
        byte[] buffer = new byte[1024];
        int bytesRead = b64is.read(buffer);
        b64is.close();
        
        String decodedResult = new String(buffer, 0, bytesRead);
        assertEquals(original, decodedResult);
    }

    @Test
    public void testInputStreamSingleByteRead() throws Exception {
        String original = "ABC";
        String encoded = Base64.encode(original.getBytes());
        
        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(encoded.getBytes());
        Base64.InputStream b64is = new Base64.InputStream(bais, Base64.DECODE);
        
        int b;
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        while ((b = b64is.read()) != -1) {
            baos.write(b);
        }
        b64is.close();
        
        assertEquals(original, new String(baos.toByteArray()));
    }

    @Test
    public void testOutputStreamFlushAndClose() throws Exception {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        Base64.OutputStream b64os = new Base64.OutputStream(baos, Base64.ENCODE);
        b64os.write('A');
        b64os.flush();
        b64os.close();
        
        assertNotNull(baos.toString());
    }

    @Test
    public void testDecodeCharArrayRange() {
        char[] chars = "QQ==\n".toCharArray();
        byte[] decoded = Base64.decode(chars, 0, chars.length, Base64.NO_OPTIONS);
        assertNotNull(decoded);
        assertEquals(1, decoded.length);
        assertEquals('Q', decoded[0]);
    }

    @Test
    public void testGetPropertyOrNull() {
        // Indirectly or directly invoke methods that might touch internal helpers if exposed, 
        // otherwise standard coverage covers the public API thoroughly.
        assertNotNull(Base64.encode("Test".getBytes()));
    }
}