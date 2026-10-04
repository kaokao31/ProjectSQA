package org.apache.commons.lang3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.junit.Test;

/**
 * Unit tests for {@link SystemUtils}.
 */
public class SystemUtilsTest {

    @Test
    public void testConstructor() {
        assertNotNull(new SystemUtils());
        Constructor<?>[] cons = SystemUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertTrue(Modifier.isPublic(cons[0].getModifiers()));
        assertTrue(Modifier.isPublic(SystemUtils.class.getModifiers()));
        assertFalse(Modifier.isFinal(SystemUtils.class.getModifiers()));
    }

    @Test
    public void testGetJavaHome() {
        File dir = SystemUtils.getJavaHome();
        assertNotNull(dir);
        assertTrue(dir.exists());
    }

    @Test
    public void testGetJavaIoTmpDir() {
        File dir = SystemUtils.getJavaIoTmpDir();
        assertNotNull(dir);
        assertTrue(dir.exists());
    }

    @Test
    public void testGetUserDir() {
        File dir = SystemUtils.getUserDir();
        assertNotNull(dir);
        assertTrue(dir.exists());
    }

    @Test
    public void testGetUserHome() {
        File dir = SystemUtils.getUserHome();
        assertNotNull(dir);
        assertTrue(dir.exists());
    }

    @Test
    public void testIsJavaAwtHeadless() {
        boolean headless = SystemUtils.isJavaAwtHeadless();
        String headlessProperty = System.getProperty("java.awt.headless");
        if (headlessProperty == null) {
            assertFalse(headless);
        } else {
            assertEquals(Boolean.valueOf(headlessProperty).booleanValue(), headless);
        }
    }

    @Test
    public void testIsJavaVersionAtLeastJavaVersion() {
        if (SystemUtils.IS_JAVA_1_1) {
            assertTrue(SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_1));
            assertFalse(SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_2));
        } else if (SystemUtils.IS_JAVA_1_2) {
            assertTrue(SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_1));
            assertTrue(SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_2));
            assertFalse(SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_3));
        } else if (SystemUtils.IS_JAVA_1_3) {
            assertTrue(SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_1));
            assertTrue(SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_2));
            assertTrue(SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_3));
            assertFalse(SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_4));
        } else if (SystemUtils.IS_JAVA_1_4) {
            assertTrue(SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_1));
            assertTrue(SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_2));
            assertTrue(SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_3));
            assertTrue(SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_4));
            assertFalse(SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_5));
        } else if (SystemUtils.IS_JAVA_1_5) {
            assertTrue(SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_1));
            assertTrue(SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_2));
            assertTrue(SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_3));
            assertTrue(SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_4));
            assertTrue(SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_5));
            assertFalse(SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_6));
        } else if (SystemUtils.IS_JAVA_1_6) {
            assertTrue(SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_1));
            assertTrue(SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_2));
            assertTrue(SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_3));
            assertTrue(SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_4));
            assertTrue(SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_5));
            assertTrue(SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_6));
            assertFalse(SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_7));
        } else if (SystemUtils.IS_JAVA_1_7) {
            assertTrue(SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_1));
            assertTrue(SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_2));
            assertTrue(SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_3));
            assertTrue(SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_4));
            assertTrue(SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_5));
            assertTrue(SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_6));
            assertTrue(SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_7));
            assertFalse(SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_8));
        }
    }

    @Test
    public void testIsJavaVersionAtLeastInt() {
        assertTrue(SystemUtils.isJavaVersionAtLeast(0));
        assertTrue(SystemUtils.isJavaVersionAtLeast(100));
        assertFalse(SystemUtils.isJavaVersionAtLeast(99900));
    }

    @Test
    public void testIsJavaVersionAtLeastFloat() {
        assertTrue(SystemUtils.isJavaVersionAtLeast(0.0f));
        assertTrue(SystemUtils.isJavaVersionAtLeast(1.1f));
        assertFalse(SystemUtils.isJavaVersionAtLeast(999.0f));
    }

    @Test
    public void testIsOSMatch() {
        assertFalse(SystemUtils.isOSMatch(null, null, "Windows", "6.1"));
        assertFalse(SystemUtils.isOSMatch("Windows 7", "6.1", null, "6.1"));
        assertFalse(SystemUtils.isOSMatch("Windows 7", null, "Windows", "6.1"));
        assertFalse(SystemUtils.isOSMatch("Windows 7", "6.1", "Windows", null));
        assertTrue(SystemUtils.isOSMatch("Windows 7", "6.1", "Windows", "6.1"));
        assertFalse(SystemUtils.isOSMatch("Windows 7", "6.1", "Mac", "6.1"));
        assertFalse(SystemUtils.isOSMatch("Windows 7", "6.1", "Windows", "5.1"));
    }

    @Test
    public void testIsOSNameMatch() {
        assertFalse(SystemUtils.isOSNameMatch(null, "Windows"));
        assertFalse(SystemUtils.isOSNameMatch("Windows 7", null));
        assertTrue(SystemUtils.isOSNameMatch("Windows 7", "Windows"));
        assertFalse(SystemUtils.isOSNameMatch("Windows 7", "Mac"));
    }

    @Test
    public void testJavaVersionAsFloat() {
        assertEquals(0.0f, SystemUtils.toJavaVersionFloat(null), 0.000001f);
        assertEquals(0.0f, SystemUtils.toJavaVersionFloat(""), 0.000001f);
        assertEquals(0.0f, SystemUtils.toJavaVersionFloat("0"), 0.000001f);
        assertEquals(1.1f, SystemUtils.toJavaVersionFloat("1.1"), 0.000001f);
        assertEquals(1.2f, SystemUtils.toJavaVersionFloat("1.2"), 0.000001f);
        assertEquals(1.3f, SystemUtils.toJavaVersionFloat("1.3.0"), 0.000001f);
        assertEquals(1.4f, SystemUtils.toJavaVersionFloat("1.4.1"), 0.000001f);
        assertEquals(1.5f, SystemUtils.toJavaVersionFloat("1.5.0_06"), 0.000001f);
        assertEquals(1.6f, SystemUtils.toJavaVersionFloat("1.6.0_03-b05"), 0.000001f);
        assertEquals(1.7f, SystemUtils.toJavaVersionFloat("1.7.0_02-ea-b07"), 0.000001f);
        assertEquals(1.8f, SystemUtils.toJavaVersionFloat("1.8.0"), 0.000001f);
    }

    @Test
    public void testJavaVersionAsInt() {
        assertEquals(0, SystemUtils.toJavaVersionInt(null));
        assertEquals(0, SystemUtils.toJavaVersionInt(""));
        assertEquals(0, SystemUtils.toJavaVersionInt("0"));
        assertEquals(110, SystemUtils.toJavaVersionInt("1.1"));
        assertEquals(120, SystemUtils.toJavaVersionInt("1.2"));
        assertEquals(130, SystemUtils.toJavaVersionInt("1.3.0"));
        assertEquals(140, SystemUtils.toJavaVersionInt("1.4.1"));
        assertEquals(150, SystemUtils.toJavaVersionInt("1.5.0_06"));
        assertEquals(160, SystemUtils.toJavaVersionInt("1.6.0_03-b05"));
        assertEquals(170, SystemUtils.toJavaVersionInt("1.7.0_02-ea-b07"));
        assertEquals(180, SystemUtils.toJavaVersionInt("1.8.0"));
    }

    @Test
    public void testJavaVersionMatches() {
        String version = SystemUtils.JAVA_VERSION;
        if (version != null) {
            assertTrue(SystemUtils.JAVA_VERSION_FLOAT > 0.0f);
            assertTrue(SystemUtils.JAVA_VERSION_INT > 0);
        }
    }

    @Test
    public void testOSMatches() {
        String osName = SystemUtils.OS_NAME;
        if (osName != null) {
            if (osName.startsWith("AIX")) {
                assertTrue(SystemUtils.IS_OS_AIX);
            } else if (osName.startsWith("HP-UX")) {
                assertTrue(SystemUtils.IS_OS_HP_UX);
            } else if (osName.startsWith("Irix")) {
                assertTrue(SystemUtils.IS_OS_IRIX);
            } else if (osName.startsWith("Linux") || osName.startsWith("LINUX")) {
                assertTrue(SystemUtils.IS_OS_LINUX);
            } else if (osName.startsWith("Mac")) {
                assertTrue(SystemUtils.IS_OS_MAC);
            } else if (osName.startsWith("Mac OS X")) {
                assertTrue(SystemUtils.IS_OS_MAC_OSX);
            } else if (osName.startsWith("FreeBSD")) {
                assertTrue(SystemUtils.IS_OS_FREE_BSD);
            } else if (osName.startsWith("OpenBSD")) {
                assertTrue(SystemUtils.IS_OS_OPEN_BSD);
            } else if (osName.startsWith("NetBSD")) {
                assertTrue(SystemUtils.IS_OS_NET_BSD);
            } else if (osName.startsWith("OS/2")) {
                assertTrue(SystemUtils.IS_OS_OS2);
            } else if (osName.startsWith("Solaris")) {
                assertTrue(SystemUtils.IS_OS_SOLARIS);
            } else if (osName.startsWith("SunOS")) {
                assertTrue(SystemUtils.IS_OS_SUN_OS);
            } else if (osName.startsWith("Windows")) {
                assertTrue(SystemUtils.IS_OS_WINDOWS);
            }
        }
    }
}