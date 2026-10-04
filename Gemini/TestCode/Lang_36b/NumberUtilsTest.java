package org.apache.commons.lang3.math;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for {@link org.apache.commons.lang3.math.NumberUtils}.
 */
public class NumberUtilsTest {

    @Test
    public void testConstructor() {
        assertNotNull(new NumberUtils());
        Constructor<?>[] cons = NumberUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertTrue(Modifier.isPublic(cons[0].getModifiers()));
        assertTrue(Modifier.isPublic(NumberUtils.class.getModifiers()));
        assertFalse(Modifier.isFinal(NumberUtils.class.getModifiers()));
    }

    // -----------------------------------------------------------------------
    // toInt, toLong, toFloat, toDouble, toByte, toShort tests
    // -----------------------------------------------------------------------

    @Test
    public void testToInt() {
        assertEquals("toInt(String) 1 failed", 0, NumberUtils.toInt(null));
        assertEquals("toInt(String) 2 failed", 0, NumberUtils.toInt(""));
        assertEquals("toInt(String) 3 failed", 0, NumberUtils.toInt("empty"));
        assertEquals("toInt(String) 4 failed", 123, NumberUtils.toInt("123"));
        assertEquals("toInt(String) 5 failed", -123, NumberUtils.toInt("-123"));

        assertEquals("toInt(String,int) 1 failed", 1, NumberUtils.toInt(null, 1));
        assertEquals("toInt(String,int) 2 failed", 1, NumberUtils.toInt("", 1));
        assertEquals("toInt(String,int) 3 failed", 1, NumberUtils.toInt("empty", 1));
        assertEquals("toInt(String,int) 4 failed", 123, NumberUtils.toInt("123", 1));
    }

    @Test
    public void testToLong() {
        assertEquals("toLong(String) 1 failed", 0L, NumberUtils.toLong(null));
        assertEquals("toLong(String) 2 failed", 0L, NumberUtils.toLong(""));
        assertEquals("toLong(String) 3 failed", 0L, NumberUtils.toLong("empty"));
        assertEquals("toLong(String) 4 failed", 1234567890123L, NumberUtils.toLong("1234567890123"));
        assertEquals("toLong(String) 5 failed", -1234567890123L, NumberUtils.toLong("-1234567890123"));

        assertEquals("toLong(String,long) 1 failed", 1L, NumberUtils.toLong(null, 1L));
        assertEquals("toLong(String,long) 2 failed", 1L, NumberUtils.toLong("", 1L));
        assertEquals("toLong(String,long) 3 failed", 1L, NumberUtils.toLong("empty", 1L));
        assertEquals("toLong(String,long) 4 failed", 1234567890123L, NumberUtils.toLong("1234567890123", 1L));
    }

    @Test
    public void testToFloat() {
        assertEquals("toFloat(String) 1 failed", 0.0f, NumberUtils.toFloat(null), 0.0001f);
        assertEquals("toFloat(String) 2 failed", 0.0f, NumberUtils.toFloat(""), 0.0001f);
        assertEquals("toFloat(String) 3 failed", 0.0f, NumberUtils.toFloat("empty"), 0.0001f);
        assertEquals("toFloat(String) 4 failed", 123.45f, NumberUtils.toFloat("123.45"), 0.0001f);

        assertEquals("toFloat(String,float) 1 failed", 1.1f, NumberUtils.toFloat(null, 1.1f), 0.0001f);
        assertEquals("toFloat(String,float) 2 failed", 1.1f, NumberUtils.toFloat("", 1.1f), 0.0001f);
        assertEquals("toFloat(String,float) 3 failed", 1.1f, NumberUtils.toFloat("empty", 1.1f), 0.0001f);
        assertEquals("toFloat(String,float) 4 failed", 123.45f, NumberUtils.toFloat("123.45", 1.1f), 0.0001f);
    }

    @Test
    public void testToDouble() {
        assertEquals("toDouble(String) 1 failed", 0.0d, NumberUtils.toDouble(null), 0.0001d);
        assertEquals("toDouble(String) 2 failed", 0.0d, NumberUtils.toDouble(""), 0.0001d);
        assertEquals("toDouble(String) 3 failed", 0.0d, NumberUtils.toDouble("empty"), 0.0001d);
        assertEquals("toDouble(String) 4 failed", 123.45d, NumberUtils.toDouble("123.45"), 0.0001d);

        assertEquals("toDouble(String,double) 1 failed", 1.1d, NumberUtils.toDouble(null, 1.1d), 0.0001d);
        assertEquals("toDouble(String,double) 2 failed", 1.1d, NumberUtils.toDouble("", 1.1d), 0.0001d);
        assertEquals("toDouble(String,double) 3 failed", 1.1d, NumberUtils.toDouble("empty", 1.1d), 0.0001d);
        assertEquals("toDouble(String,double) 4 failed", 123.45d, NumberUtils.toDouble("123.45", 1.1d), 0.0001d);
    }

    @Test
    public void testToByte() {
        assertEquals("toByte(String) 1 failed", 0, NumberUtils.toByte(null));
        assertEquals("toByte(String) 2 failed", 0, NumberUtils.toByte(""));
        assertEquals("toByte(String) 3 failed", 0, NumberUtils.toByte("empty"));
        assertEquals("toByte(String) 4 failed", 123, NumberUtils.toByte("123"));

        assertEquals("toByte(String,byte) 1 failed", 1, NumberUtils.toByte(null, (byte) 1));
        assertEquals("toByte(String,byte) 2 failed", 1, NumberUtils.toByte("", (byte) 1));
        assertEquals("toByte(String,byte) 3 failed", 1, NumberUtils.toByte("empty", (byte) 1));
        assertEquals("toByte(String,byte) 4 failed", 123, NumberUtils.toByte("123", (byte) 1));
    }

    @Test
    public void testToShort() {
        assertEquals("toShort(String) 1 failed", 0, NumberUtils.toShort(null));
        assertEquals("toShort(String) 2 failed", 0, NumberUtils.toShort(""));
        assertEquals("toShort(String) 3 failed", 0, NumberUtils.toShort("empty"));
        assertEquals("toShort(String) 4 failed", 1234, NumberUtils.toShort("1234"));

        assertEquals("toShort(String,short) 1 failed", 1, NumberUtils.toShort(null, (short) 1));
        assertEquals("toShort(String,short) 2 failed", 1, NumberUtils.toShort("", (short) 1));
        assertEquals("toShort(String,short) 3 failed", 1, NumberUtils.toShort("empty", (short) 1));
        assertEquals("toShort(String,short) 4 failed", 1234, NumberUtils.toShort("1234", (short) 1));
    }

    // -----------------------------------------------------------------------
    // createNumber tests
    // -----------------------------------------------------------------------

    @Test
    public void testCreateNumber() {
        assertNull(NumberUtils.createNumber(null));
        assertEquals("createNumber(String) failed", Integer.valueOf(12345), NumberUtils.createNumber("12345"));
        assertEquals("createNumber(String) failed", Long.valueOf(123456789012L), NumberUtils.createNumber("123456789012"));
        assertEquals("createNumber(String) failed", Float.valueOf(123.45f), NumberUtils.createNumber("123.45f"));
        assertEquals("createNumber(String) failed", Float.valueOf(123.45F), NumberUtils.createNumber("123.45F"));
        assertEquals("createNumber(String) failed", Double.valueOf(123.45), NumberUtils.createNumber("123.45d"));
        assertEquals("createNumber(String) failed", Double.valueOf(123.45), NumberUtils.createNumber("123.45D"));
        assertEquals("createNumber(String) failed", Double.valueOf(123.45), NumberUtils.createNumber("123.45"));
        assertEquals("createNumber(String) failed", Long.valueOf(12345), NumberUtils.createNumber("12345L"));
        assertEquals("createNumber(String) failed", Long.valueOf(12345), NumberUtils.createNumber("12345l"));
        assertEquals("createNumber(String) failed", new BigInteger("123456789012345678901234567890"), NumberUtils.createNumber("123456789012345678901234567890"));
        assertEquals("createNumber(String) failed", new BigDecimal("123456789012345678901234567890.1234567890"), NumberUtils.createNumber("123456789012345678901234567890.1234567890"));

        // Hex numbers
        assertEquals("createNumber(String) failed", Integer.valueOf(0x1234), NumberUtils.createNumber("0x1234"));
        assertEquals("createNumber(String) failed", Integer.valueOf(0x1234), NumberUtils.createNumber("0X1234"));
        assertEquals("createNumber(String) failed", Integer.valueOf(-0x1234), NumberUtils.createNumber("-0x1234"));
        assertEquals("createNumber(String) failed", Integer.valueOf(-0x1234), NumberUtils.createNumber("-0X1234"));
        assertEquals("createNumber(String) failed", Integer.valueOf(0x1234), NumberUtils.createNumber("#1234"));
        assertEquals("createNumber(String) failed", Integer.valueOf(-0x1234), NumberUtils.createNumber("-#1234"));

        // Leading dot, trailing dot, exponent tests
        assertEquals("createNumber(String) failed", Float.valueOf(0.2f), NumberUtils.createNumber(".2f"));
        assertEquals("createNumber(String) failed", Double.valueOf(0.2d), NumberUtils.createNumber(".2d"));
        assertEquals("createNumber(String) failed", Double.valueOf(0.2), NumberUtils.createNumber(".2"));
        assertEquals("createNumber(String) failed", Float.valueOf(2.0f), NumberUtils.createNumber("2.f"));
        assertEquals("createNumber(String) failed", Double.valueOf(2.0d), NumberUtils.createNumber("2.d"));
        assertEquals("createNumber(String) failed", Float.valueOf(2.0f), NumberUtils.createNumber("2.F"));
        assertEquals("createNumber(String) failed", Double.valueOf(2.0d), NumberUtils.createNumber("2.D"));
        assertEquals("createNumber(String) failed", Float.valueOf(2.0f), NumberUtils.createNumber("2.0f"));
        assertEquals("createNumber(String) failed", Double.valueOf(2.0d), NumberUtils.createNumber("2.0d"));

        // LANG-521: Trailing decimal point without explicit type specifier
        assertEquals("createNumber(String) LANG-521 failed", Float.valueOf("2."), NumberUtils.createNumber("2."));

        // Scientific notations
        assertEquals("createNumber(String) failed", Double.valueOf(1.2e3), NumberUtils.createNumber("1.2e3"));
        assertEquals("createNumber(String) failed", Double.valueOf(1.2e-3), NumberUtils.createNumber("1.2e-3"));
        assertEquals("createNumber(String) failed", Double.valueOf(1.2E3), NumberUtils.createNumber("1.2E3"));
        assertEquals("createNumber(String) failed", Double.valueOf(1.2E-3), NumberUtils.createNumber("1.2E-3"));
        assertEquals("createNumber(String) failed", Float.valueOf(1.2e3f), NumberUtils.createNumber("1.2e3f"));
        assertEquals("createNumber(String) failed", Double.valueOf(1.2e3d), NumberUtils.createNumber("1.2e3d"));

        // Exceptions
        checkCreateNumberFailure("");
        checkCreateNumberFailure("   ");
        checkCreateNumberFailure("--1234");
        checkCreateNumberFailure("0x");
        checkCreateNumberFailure("-0x");
        checkCreateNumberFailure("#");
        checkCreateNumberFailure("-#");
        checkCreateNumberFailure("123a");
        checkCreateNumberFailure("1.2.3");
        checkCreateNumberFailure("1e2e3");
        checkCreateNumberFailure("1e");
        checkCreateNumberFailure("1e-");
        checkCreateNumberFailure("1e+");
        checkCreateNumberFailure("1eE2");
        checkCreateNumberFailure("1a");
        checkCreateNumberFailure("1Lz");
        checkCreateNumberFailure(".e2");
        checkCreateNumberFailure(".");
        checkCreateNumberFailure("foo");
    }

    private void checkCreateNumberFailure(String val) {
        try {
            NumberUtils.createNumber(val);
            fail("createNumber(\"" + val + "\") should have thrown NumberFormatException");
        } catch (NumberFormatException expected) {
            // Success
        }
    }

    // -----------------------------------------------------------------------
    // Individual create methods
    // -----------------------------------------------------------------------

    @Test
    public void testCreateFloat() {
        assertNull(NumberUtils.createFloat(null));
        assertEquals(Float.valueOf(123.45f), NumberUtils.createFloat("123.45"));
        try {
            NumberUtils.createFloat("invalid");
            fail("Should throw NumberFormatException");
        } catch (NumberFormatException expected) {}
    }

    @Test
    public void testCreateDouble() {
        assertNull(NumberUtils.createDouble(null));
        assertEquals(Double.valueOf(123.45), NumberUtils.createDouble("123.45"));
        try {
            NumberUtils.createDouble("invalid");
            fail("Should throw NumberFormatException");
        } catch (NumberFormatException expected) {}
    }

    @Test
    public void testCreateInteger() {
        assertNull(NumberUtils.createInteger(null));
        assertEquals(Integer.valueOf(12345), NumberUtils.createInteger("12345"));
        assertEquals(Integer.valueOf(0x12), NumberUtils.createInteger("0x12"));
        assertEquals(Integer.valueOf(012), NumberUtils.createInteger("012"));
        try {
            NumberUtils.createInteger("invalid");
            fail("Should throw NumberFormatException");
        } catch (NumberFormatException expected) {}
    }

    @Test
    public void testCreateLong() {
        assertNull(NumberUtils.createLong(null));
        assertEquals(Long.valueOf(123456789012L), NumberUtils.createLong("123456789012"));
        assertEquals(Long.valueOf(0x12L), NumberUtils.createLong("0x12"));
        try {
            NumberUtils.createLong("invalid");
            fail("Should throw NumberFormatException");
        } catch (NumberFormatException expected) {}
    }

    @Test
    public void testCreateBigInteger() {
        assertNull(NumberUtils.createBigInteger(null));
        assertEquals(new BigInteger("12345678901234567890"), NumberUtils.createBigInteger("12345678901234567890"));
        assertEquals(new BigInteger("12", 16), NumberUtils.createBigInteger("0x12"));
        assertEquals(new BigInteger("12", 16), NumberUtils.createBigInteger("#12"));
        assertEquals(new BigInteger("-12", 16), NumberUtils.createBigInteger("-0x12"));
        assertEquals(new BigInteger("-12", 16), NumberUtils.createBigInteger("-#12"));
        try {
            NumberUtils.createBigInteger("invalid");
            fail("Should throw NumberFormatException");
        } catch (NumberFormatException expected) {}
    }

    @Test
    public void testCreateBigDecimal() {
        assertNull(NumberUtils.createBigDecimal(null));
        assertEquals(new BigDecimal("12345678901234567890.1234567890"), NumberUtils.createBigDecimal("12345678901234567890.1234567890"));
        try {
            NumberUtils.createBigDecimal("invalid");
            fail("Should throw NumberFormatException");
        } catch (NumberFormatException expected) {}
    }

    // -----------------------------------------------------------------------
    // min / max tests
    // -----------------------------------------------------------------------

    @Test
    public void testMinLong() {
        assertEquals(1L, NumberUtils.min(new long[]{1L, 2L, 3L}));
        assertEquals(1L, NumberUtils.min(new long[]{3L, 2L, 1L}));
        assertEquals(1L, NumberUtils.min(new long[]{3L, 1L, 2L}));
        assertEquals(1L, NumberUtils.min(1L, 2L, 3L));
        assertEquals(1L, NumberUtils.min(3L, 1L, 2L));
        assertEquals(1L, NumberUtils.min(3L, 2L, 1L));

        try {
            NumberUtils.min((long[]) null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {}

        try {
            NumberUtils.min(new long[0]);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {}
    }

    @Test
    public void testMinInt() {
        assertEquals(1, NumberUtils.min(new int[]{1, 2, 3}));
        assertEquals(1, NumberUtils.min(new int[]{3, 2, 1}));
        assertEquals(1, NumberUtils.min(new int[]{3, 1, 2}));
        assertEquals(1, NumberUtils.min(1, 2, 3));
        assertEquals(1, NumberUtils.min(3, 1, 2));
        assertEquals(1, NumberUtils.min(3, 2, 1));

        try {
            NumberUtils.min((int[]) null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {}

        try {
            NumberUtils.min(new int[0]);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {}
    }

    @Test
    public void testMinShort() {
        assertEquals((short) 1, NumberUtils.min(new short[]{1, 2, 3}));
        assertEquals((short) 1, NumberUtils.min(new short[]{3, 2, 1}));
        assertEquals((short) 1, NumberUtils.min(new short[]{3, 1, 2}));
        assertEquals((short) 1, NumberUtils.min((short) 1, (short) 2, (short) 3));
        assertEquals((short) 1, NumberUtils.min((short) 3, (short) 1, (short) 2));
        assertEquals((short) 1, NumberUtils.min((short) 3, (short) 2, (short) 1));

        try {
            NumberUtils.min((short[]) null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {}

        try {
            NumberUtils.min(new short[0]);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {}
    }

    @Test
    public void testMinByte() {
        assertEquals((byte) 1, NumberUtils.min(new byte[]{1, 2, 3}));
        assertEquals((byte) 1, NumberUtils.min(new byte[]{3, 2, 1}));
        assertEquals((byte) 1, NumberUtils.min(new byte[]{3, 1, 2}));
        assertEquals((byte) 1, NumberUtils.min((byte) 1, (byte) 2, (byte) 3));
        assertEquals((byte) 1, NumberUtils.min((byte) 3, (byte) 1, (byte) 2));
        assertEquals((byte) 1, NumberUtils.min((byte) 3, (byte) 2, (byte) 1));

        try {
            NumberUtils.min((byte[]) null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {}

        try {
            NumberUtils.min(new byte[0]);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {}
    }

    @Test
    public void testMinDouble() {
        assertEquals(1.1d, NumberUtils.min(new double[]{1.1d, 2.2d, 3.3d}), 0.0001d);
        assertEquals(1.1d, NumberUtils.min(new double[]{3.3d, 2.2d, 1.1d}), 0.0001d);
        assertEquals(1.1d, NumberUtils.min(new double[]{3.3d, 1.1d, 2.2d}), 0.0001d);
        assertEquals(1.1d, NumberUtils.min(new double[]{Double.NaN, 1.1d, 2.2d}), 0.0001d);
        assertEquals(1.1d, NumberUtils.min(new double[]{1.1d, Double.NaN, 2.2d}), 0.0001d);
        assertEquals(1.1d, NumberUtils.min(1.1d, 2.2d, 3.3d), 0.0001d);
        assertEquals(1.1d, NumberUtils.min(3.3d, 1.1d, 2.2d), 0.0001d);
        assertEquals(1.1d, NumberUtils.min(3.3d, 2.2d, 1.1d), 0.0001d);
        assertEquals(1.1d, NumberUtils.min(Double.NaN, 1.1d, 2.2d), 0.0001d);
        assertEquals(1.1d, NumberUtils.min(1.1d, Double.NaN, 2.2d), 0.0001d);
        assertEquals(1.1d, NumberUtils.min(1.1d, 2.2d, Double.NaN), 0.0001d);

        try {
            NumberUtils.min((double[]) null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {}

        try {
            NumberUtils.min(new double[0]);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {}
    }

    @Test
    public void testMinFloat() {
        assertEquals(1.1f, NumberUtils.min(new float[]{1.1f, 2.2f, 3.3f}), 0.0001f);
        assertEquals(1.1f, NumberUtils.min(new float[]{3.3f, 2.2f, 1.1f}), 0.0001f);
        assertEquals(1.1f, NumberUtils.min(new float[]{3.3f, 1.1f, 2.2f}), 0.0001f);
        assertEquals(1.1f, NumberUtils.min(new float[]{Float.NaN, 1.1f, 2.2f}), 0.0001f);
        assertEquals(1.1f, NumberUtils.min(new float[]{1.1f, Float.NaN, 2.2f}), 0.0001f);
        assertEquals(1.1f, NumberUtils.min(1.1f, 2.2f, 3.3f), 0.0001f);
        assertEquals(1.1f, NumberUtils.min(3.3f, 1.1f, 2.2f), 0.0001f);
        assertEquals(1.1f, NumberUtils.min(3.3f, 2.2f, 1.1f), 0.0001f);
        assertEquals(1.1f, NumberUtils.min(Float.NaN, 1.1f, 2.2f), 0.0001f);
        assertEquals(1.1f, NumberUtils.min(1.1f, Float.NaN, 2.2f), 0.0001f);
        assertEquals(1.1f, NumberUtils.min(1.1f, 2.2f, Float.NaN), 0.0001f);

        try {
            NumberUtils.min((float[]) null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {}

        try {
            NumberUtils.min(new float[0]);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {}
    }

    @Test
    public void testMaxLong() {
        assertEquals(3L, NumberUtils.max(new long[]{1L, 2L, 3L}));
        assertEquals(3L, NumberUtils.max(new long[]{3L, 2L, 1L}));
        assertEquals(3L, NumberUtils.max(new long[]{1L, 3L, 2L}));
        assertEquals(3L, NumberUtils.max(1L, 2L, 3L));
        assertEquals(3L, NumberUtils.max(3L, 1L, 2L));
        assertEquals(3L, NumberUtils.max(1L, 3L, 2L));

        try {
            NumberUtils.max((long[]) null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {}

        try {
            NumberUtils.max(new long[0]);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {}
    }

    @Test
    public void testMaxInt() {
        assertEquals(3, NumberUtils.max(new int[]{1, 2, 3}));
        assertEquals(3, NumberUtils.max(new int[]{3, 2, 1}));
        assertEquals(3, NumberUtils.max(new int[]{1, 3, 2}));
        assertEquals(3, NumberUtils.max(1, 2, 3));
        assertEquals(3, NumberUtils.max(3, 1, 2));
        assertEquals(3, NumberUtils.max(1, 3, 2));

        try {
            NumberUtils.max((int[]) null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {}

        try {
            NumberUtils.max(new int[0]);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {}
    }

    @Test
    public void testMaxShort() {
        assertEquals((short) 3, NumberUtils.max(new short[]{1, 2, 3}));
        assertEquals((short) 3, NumberUtils.max(new short[]{3, 2, 1}));
        assertEquals((short) 3, NumberUtils.max(new short[]{1, 3, 2}));
        assertEquals((short) 3, NumberUtils.max((short) 1, (short) 2, (short) 3));
        assertEquals((short) 3, NumberUtils.max((short) 3, (short) 1, (short) 2));
        assertEquals((short) 3, NumberUtils.max((short) 1, (short) 3, (short) 2));

        try {
            NumberUtils.max((short[]) null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {}

        try {
            NumberUtils.max(new short[0]);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {}
    }

    @Test
    public void testMaxByte() {
        assertEquals((byte) 3, NumberUtils.max(new byte[]{1, 2, 3}));
        assertEquals((byte) 3, NumberUtils.max(new byte[]{3, 2, 1}));
        assertEquals((byte) 3, NumberUtils.max(new byte[]{1, 3, 2}));
        assertEquals((byte) 3, NumberUtils.max((byte) 1, (byte) 2, (byte) 3));
        assertEquals((byte) 3, NumberUtils.max((byte) 3, (byte) 1, (byte) 2));
        assertEquals((byte) 3, NumberUtils.max((byte) 1, (byte) 3, (byte) 2));

        try {
            NumberUtils.max((byte[]) null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {}

        try {
            NumberUtils.max(new byte[0]);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {}
    }

    @Test
    public void testMaxDouble() {
        assertEquals(3.3d, NumberUtils.max(new double[]{1.1d, 2.2d, 3.3d}), 0.0001d);
        assertEquals(3.3d, NumberUtils.max(new double[]{3.3d, 2.2d, 1.1d}), 0.0001d);
        assertEquals(3.3d, NumberUtils.max(new double[]{1.1d, 3.3d, 2.2d}), 0.0001d);
        assertEquals(3.3d, NumberUtils.max(new double[]{Double.NaN, 1.1d, 3.3d}), 0.0001d);
        assertEquals(3.3d, NumberUtils.max(new double[]{1.1d, Double.NaN, 3.3d}), 0.0001d);
        assertEquals(3.3d, NumberUtils.max(1.1d, 2.2d, 3.3d), 0.0001d);
        assertEquals(3.3d, NumberUtils.max(3.3d, 1.1d, 2.2d), 0.0001d);
        assertEquals(3.3d, NumberUtils.max(1.1d, 3.3d, 2.2d), 0.0001d);
        assertEquals(3.3d, NumberUtils.max(Double.NaN, 1.1d, 3.3d), 0.0001d);
        assertEquals(3.3d, NumberUtils.max(1.1d, Double.NaN, 3.3d), 0.0001d);
        assertEquals(3.3d, NumberUtils.max(1.1d, 3.3d, Double.NaN), 0.0001d);

        try {
            NumberUtils.max((double[]) null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {}

        try {
            NumberUtils.max(new double[0]);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {}
    }

    @Test
    public void testMaxFloat() {
        assertEquals(3.3f, NumberUtils.max(new float[]{1.1f, 2.2f, 3.3f}), 0.0001f);
        assertEquals(3.3f, NumberUtils.max(new float[]{3.3f, 2.2f, 1.1f}), 0.0001f);
        assertEquals(3.3f, NumberUtils.max(new float[]{1.1f, 3.3f, 2.2f}), 0.0001f);
        assertEquals(3.3f, NumberUtils.max(new float[]{Float.NaN, 1.1f, 3.3f}), 0.0001f);
        assertEquals(3.3f, NumberUtils.max(new float[]{1.1f, Float.NaN, 3.3f}), 0.0001f);
        assertEquals(3.3f, NumberUtils.max(1.1f, 2.2f, 3.3f), 0.0001f);
        assertEquals(3.3f, NumberUtils.max(3.3f, 1.1f, 2.2f), 0.0001f);
        assertEquals(3.3f, NumberUtils.max(1.1f, 3.3f, 2.2f), 0.0001f);
        assertEquals(3.3f, NumberUtils.max(Float.NaN, 1.1f, 3.3f), 0.0001f);
        assertEquals(3.3f, NumberUtils.max(1.1f, Float.NaN, 3.3f), 0.0001f);
        assertEquals(3.3f, NumberUtils.max(1.1f, 3.3f, Float.NaN), 0.0001f);

        try {
            NumberUtils.max((float[]) null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {}

        try {
            NumberUtils.max(new float[0]);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {}
    }

    // -----------------------------------------------------------------------
    // isDigits tests
    // -----------------------------------------------------------------------

    @Test
    public void testIsDigits() {
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
        assertTrue(NumberUtils.isDigits("12345"));
        assertFalse(NumberUtils.isDigits("123.45"));
        assertFalse(NumberUtils.isDigits("123a"));
        assertFalse(NumberUtils.isDigits("-12345"));
    }

    // -----------------------------------------------------------------------
    // isNumber tests
    // -----------------------------------------------------------------------

    @Test
    public void testIsNumber() {
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber("   "));

        assertTrue(NumberUtils.isNumber("123"));
        assertTrue(NumberUtils.isNumber("-123"));
        assertTrue(NumberUtils.isNumber("+123"));
        assertTrue(NumberUtils.isNumber("123.45"));
        assertTrue(NumberUtils.isNumber("-123.45"));
        assertTrue(NumberUtils.isNumber(".45"));
        assertTrue(NumberUtils.isNumber("-.45"));

        // LANG-521: Trailing dot
        assertTrue("isNumber(String) LANG-521 failed", NumberUtils.isNumber("2."));

        assertTrue(NumberUtils.isNumber("1234L"));
        assertTrue(NumberUtils.isNumber("1234l"));
        assertTrue(NumberUtils.isNumber("123.4f"));
        assertTrue(NumberUtils.isNumber("123.4F"));
        assertTrue(NumberUtils.isNumber("123.4d"));
        assertTrue(NumberUtils.isNumber("123.4D"));

        // Hex
        assertTrue(NumberUtils.isNumber("0x1234"));
        assertTrue(NumberUtils.isNumber("-0x1234"));
        assertTrue(NumberUtils.isNumber("+0x1234"));
        assertTrue(NumberUtils.isNumber("0X1234"));
        assertTrue(NumberUtils.isNumber("-0X1234"));
        assertTrue(NumberUtils.isNumber("+0X1234"));
        assertTrue(NumberUtils.isNumber("0xabcdefABCDEF"));
        assertFalse(NumberUtils.isNumber("0x"));
        assertFalse(NumberUtils.isNumber("-0x"));
        assertFalse(NumberUtils.isNumber("0xG"));

        // Exponent
        assertTrue(NumberUtils.isNumber("1.2e3"));
        assertTrue(NumberUtils.isNumber("1.2E3"));
        assertTrue(NumberUtils.isNumber("1.2e-3"));
        assertTrue(NumberUtils.isNumber("1.2e+3"));
        assertTrue(NumberUtils.isNumber("1.2e3f"));
        assertTrue(NumberUtils.isNumber("1.2e3d"));
        assertTrue(NumberUtils.isNumber("1.2e3F"));
        assertTrue(NumberUtils.isNumber("1.2e3D"));
        assertTrue(NumberUtils.isNumber("1e3"));

        assertFalse(NumberUtils.isNumber("1.2e"));
        assertFalse(NumberUtils.isNumber("1.2e-"));
        assertFalse(NumberUtils.isNumber("1.2e+"));
        assertFalse(NumberUtils.isNumber("1.2e3e4"));
        assertFalse(NumberUtils.isNumber("1.2.3"));
        assertFalse(NumberUtils.isNumber("."));
        assertFalse(NumberUtils.isNumber("--123"));
        assertFalse(NumberUtils.isNumber("++123"));
        assertFalse(NumberUtils.isNumber("123a"));
        assertFalse(NumberUtils.isNumber("123e+a"));
        assertFalse(NumberUtils.isNumber("123L2"));
    }
}