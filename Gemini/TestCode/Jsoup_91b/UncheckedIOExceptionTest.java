package org.jsoup;

import org.junit.Test;
import java.io.IOException;
import java.io.UncheckedIOException;

import static org.junit.Assert.*;

public class UncheckedIOExceptionTest {

    @Test
    public void testConstructorAndCause() {
        IOException cause = new IOException("Test IO exception");
        UncheckedIOException exception = new UncheckedIOException(cause);

        assertNotNull(exception);
        assertEquals(cause, exception.getCause());
        assertEquals("java.io.IOException: Test IO exception", exception.getMessage());
    }

    @Test
    public void testCauseWithNullMessage() {
        IOException cause = new IOException();
        UncheckedIOException exception = new UncheckedIOException(cause);

        assertNotNull(exception);
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testInheritance() {
        IOException cause = new IOException();
        UncheckedIOException exception = new UncheckedIOException(cause);

        assertTrue(exception instanceof RuntimeException);
        assertTrue(exception instanceof java.io.UncheckedIOException);
    }
}