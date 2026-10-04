package org.apache.commons.math.geometry.euclidean.threed;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import org.apache.commons.math.util.MathUtils;
import org.apache.commons.math.geometry.Vector3D;

public class RotationTest {

    private static final double EPS = 1e-10;

    // Identity rotation
    private Rotation identity;

    @Before
    public void setUp() {
        identity = new Rotation(1.0, 0.0, 0.0, 0.0, false);
    }

    // ========== Constructor Tests ==========

    @Test
    public void testIdentity() {
        assertTrue(identity.isIdentity());
        assertEquals(0.0, identity.getAngle(), EPS);
        assertArrayEquals(new double[]{1, 0, 0, 0}, new double[]{identity.getQ0(), identity.getQ1(), identity.getQ2(), identity.getQ3()}, EPS);
    }

    @Test
    public void testConstructorAxisAngle() {
        Vector3D axis = new Vector3D(1, 0, 0);
        Rotation r = new Rotation(axis, Math.PI / 2);
        assertEquals(Math.PI / 2, r.getAngle(), EPS);
        assertTrue(Vector3D.distance(axis, r.getAxis()) < EPS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorAxisAngleZeroAxis() {
        new Rotation(Vector3D.ZERO, 1.0);
    }

    @Test
    public void testConstructorTwoVectors() {
        Vector3D u = new Vector3D(1, 0, 0);
        Vector3D v = new Vector3D(0, 1, 0);
        Rotation r = new Rotation(u, v);
        Vector3D rotated = r.applyTo(u);
        assertTrue(Vector3D.distance(rotated, v) < EPS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorTwoVectorsNullU() {
        new Rotation(null, new Vector3D(1, 0, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorTwoVectorsNullV() {
        new Rotation(new Vector3D(1, 0, 0), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorTwoVectorsZeroU() {
        new Rotation(Vector3D.ZERO, new Vector3D(1, 0, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorTwoVectorsZeroV() {
        new Rotation(new Vector3D(1, 0, 0), Vector3D.ZERO);
    }

    @Test
    public void testConstructorMatrix() {
        double[][] m = new double[][]{{1, 0, 0}, {0, 0, -1}, {0, 1, 0}};
        Rotation r = new Rotation(m, 1e-10);
        assertEquals(Math.PI / 2, r.getAngle(), EPS);
        Vector3D axis = r.getAxis();
        assertTrue(Vector3D.distance(new Vector3D(1, 0, 0), axis) < EPS);
    }

    @Test(expected = NotARotationMatrixException.class)
    public void testConstructorMatrixNotOrthogonal() {
        double[][] m = new double[][]{{1, 0, 0}, {0, 1, 0}, {0, 0, 2}};
        new Rotation(m, 1e-10);
    }

    @Test
    public void testConstructorQuaternion() {
        Rotation r = new Rotation(0.5, 0.5, 0.5, 0.5, true);
        assertTrue(r.isIdentity() == false);
        assertEquals(2 * Math.acos(0.5), r.getAngle(), EPS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorQuaternionNotNormalized() {
        new Rotation(1.0, 0.0, 0.0, 0.0, true); // norm = 1, ok
        new Rotation(2.0, 0.0, 0.0, 0.0, true); // norm != 1, should throw
    }

    // ========== getAngles Tests ==========

    @Test
    public void testGetAnglesIdentity() {
        Rotation r = identity;
        double[] angles = r.getAngles();
        assertEquals(0.0, angles[0], EPS);
        assertEquals(0.0, angles[1], EPS);
        assertEquals(0.0, angles[2], EPS);
    }

    @Test
    public void testGetAnglesX90() {
        Rotation r = new Rotation(new Vector3D(1, 0, 0), Math.PI / 2);
        double[] angles = r.getAngles();
        assertEquals(Math.PI / 2, angles[0], EPS);
        assertEquals(0.0, angles[1], EPS);
        assertEquals(0.0, angles[2], EPS);
    }

    @Test
    public void testGetAnglesY90() {
        Rotation r = new Rotation(new Vector3D(0, 1, 0), Math.PI / 2);
        double[] angles = r.getAngles();
        assertEquals(0.0, angles[0], EPS);
        assertEquals(Math.PI / 2, angles[1], EPS);
        assertEquals(0.0, angles[2], EPS);
    }

    @Test
    public void testGetAnglesZ90() {
        Rotation r = new Rotation(new Vector3D(0, 0, 1), Math.PI / 2);
        double[] angles = r.getAngles();
        assertEquals(0.0, angles[0], EPS);
        assertEquals(0.0, angles[1], EPS);
        assertEquals(Math.PI / 2, angles[2], EPS);
    }

    @Test
    public void testGetAnglesNearSingularity() {
        // Rotation near singularity (angle near pi/2 for second angle)
        // Use a rotation that makes second angle close to pi/2
        Rotation r = new Rotation(new Vector3D(0, 1, 0), Math.PI / 2 - 1e-5);
        double[] angles = r.getAngles();
        // Should not produce NaN
        assertFalse(Double.isNaN(angles[0]));
        assertFalse(Double.isNaN(angles[1]));
        assertFalse(Double.isNaN(angles[2]));
        assertEquals(Math.PI / 2 - 1e-5, angles[1], 1e-4);
    }

    @Test
    public void testGetAnglesAtSingularity() {
        // Exactly at singularity: second angle = pi/2
        Rotation r = new Rotation(new Vector3D(0, 1, 0), Math.PI / 2);
        double[] angles = r.getAngles();
        // Should handle singularity gracefully, not NaN
        assertFalse(Double.isNaN(angles[0]));
        assertFalse(Double.isNaN(angles[1]));
        assertFalse(Double.isNaN(angles[2]));
        assertEquals(Math.PI / 2, angles[1], EPS);
    }

    @Test
    public void testGetAnglesRandom() {
        // Random rotation
        Rotation r = new Rotation(new Vector3D(1, 2, 3).normalize(), 0.7);
        double[] angles = r.getAngles();
        // Reconstruct rotation from angles and verify
        Rotation reconstructed = new Rotation(
            new Rotation(new Vector3D(1, 0, 0), angles[0]),
            new Rotation(new Vector3D(0, 1, 0), angles[1]),
            new Rotation(new Vector3D(0, 0, 1), angles[2])
        );
        assertTrue(r.applyTo(new Vector3D(1, 0, 0)).distance(reconstructed.applyTo(new Vector3D(1, 0, 0))) < 1e-6);
    }

    // ========== applyTo / applyInverseTo Tests ==========

    @Test
    public void testApplyToIdentity() {
        Vector3D v = new Vector3D(1, 2, 3);
        assertEquals(v, identity.applyTo(v));
    }

    @Test
    public void testApplyToRotation() {
        Rotation r = new Rotation(new Vector3D(0, 0, 1), Math.PI / 2);
        Vector3D v = new Vector3D(1, 0, 0);
        Vector3D expected = new Vector3D(0, 1, 0);
        assertTrue(Vector3D.distance(expected, r.applyTo(v)) < EPS);
    }

    @Test
    public void testApplyInverseTo() {
        Rotation r = new Rotation(new Vector3D(0, 0, 1), Math.PI / 2);
        Vector3D v = new Vector3D(0, 1, 0);
        Vector3D expected = new Vector3D(1, 0, 0);
        assertTrue(Vector3D.distance(expected, r.applyInverseTo(v)) < EPS);
    }

    @Test
    public void testApplyToNull() {
        try {
            identity.applyTo(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testApplyInverseToNull() {
        try {
            identity.applyInverseTo(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    // ========== compose / applyTo (Rotation) Tests ==========

    @Test
    public void testCompose() {
        Rotation r1 = new Rotation(new Vector3D(0, 0, 1), Math.PI / 2);
        Rotation r2 = new Rotation(new Vector3D(0, 0, 1), Math.PI / 2);
        Rotation composed = r1.applyTo(r2);
        Rotation expected = new Rotation(new Vector3D(0, 0, 1), Math.PI);
        assertTrue(composed.applyTo(new Vector3D(1, 0, 0)).distance(expected.applyTo(new Vector3D(1, 0, 0))) < EPS);
    }

    @Test
    public void testComposeInverse() {
        Rotation r = new Rotation(new Vector3D(0, 0, 1), Math.PI / 2);
        Rotation inverse = r.applyInverseTo(r);
        assertTrue(inverse.isIdentity());
    }

    // ========== getAxis / getAngle Tests ==========

    @Test
    public void testGetAxisAngleIdentity() {
        Rotation r = identity;
        assertEquals(0.0, r.getAngle(), EPS);
        // Axis is arbitrary for identity, but should be non-zero
        assertNotNull(r.getAxis());
        assertTrue(r.getAxis().getNorm() > 0);
    }

    @Test
    public void testGetAxisAngle180() {
        Rotation r = new Rotation(new Vector3D(1, 0, 0), Math.PI);
        assertEquals(Math.PI, r.getAngle(), EPS);
        Vector3D axis = r.getAxis();
        assertTrue(Vector3D.distance(new Vector3D(1, 0, 0), axis) < EPS);
    }

    @Test
    public void testGetAxisAngleSmall() {
        Rotation r = new Rotation(new Vector3D(1, 1, 1).normalize(), 1e-5);
        assertEquals(1e-5, r.getAngle(), 1e-10);
        Vector3D axis = r.getAxis();
        assertTrue(Vector3D.distance(new Vector3D(1, 1, 1).normalize(), axis) < 1e-10);
    }

    // ========== getMatrix Tests ==========

    @Test
    public void testGetMatrixIdentity() {
        double[][] m = identity.getMatrix();
        double[][] expected = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
        for (int i = 0; i < 3; i++) {
            assertArrayEquals(expected[i], m[i], EPS);
        }
    }

    @Test
    public void testGetMatrixRotation() {
        Rotation r = new Rotation(new Vector3D(0, 0, 1), Math.PI / 2);
        double[][] m = r.getMatrix();
        double[][] expected = {{0, -1, 0}, {1, 0, 0}, {0, 0, 1}};
        for (int i = 0; i < 3; i++) {
            assertArrayEquals(expected[i], m[i], EPS);
        }
    }

    // ========== Quaternion getters ==========

    @Test
    public void testGetQ0Q1Q2Q3() {
        Rotation r = new Rotation(0.5, 0.5, 0.5, 0.5, true);
        assertEquals(0.5, r.getQ0(), EPS);
        assertEquals(0.5, r.getQ1(), EPS);
        assertEquals(0.5, r.getQ2(), EPS);
        assertEquals(0.5, r.getQ3(), EPS);
    }

    // ========== isIdentity Tests ==========

    @Test
    public void testIsIdentityTrue() {
        assertTrue(identity.isIdentity());
    }

    @Test
    public void testIsIdentityFalse() {
        Rotation r = new Rotation(new Vector3D(1, 0, 0), 0.5);
        assertFalse(r.isIdentity());
    }

    // ========== Edge Cases ==========

    @Test
    public void testAngleNearPi() {
        Rotation r = new Rotation(new Vector3D(1, 0, 0), Math.PI - 1e-5);
        assertEquals(Math.PI - 1e-5, r.getAngle(), 1e-10);
        Vector3D axis = r.getAxis();
        assertTrue(Vector3D.distance(new Vector3D(1, 0, 0), axis) < 1e-10);
    }

    @Test
    public void testAngleExactlyPi() {
        Rotation r = new Rotation(new Vector3D(1, 0, 0), Math.PI);
        assertEquals(Math.PI, r.getAngle(), EPS);
        // Axis should be well-defined
        Vector3D axis = r.getAxis();
        assertTrue(Vector3D.distance(new Vector3D(1, 0, 0), axis) < EPS);
    }

    @Test
    public void testAngleZero() {
        Rotation r = new Rotation(new Vector3D(1, 0, 0), 0.0);
        assertTrue(r.isIdentity());
        assertEquals(0.0, r.getAngle(), EPS);
    }

    @Test
    public void testNegativeAngle() {
        Rotation r = new Rotation(new Vector3D(0, 0, 1), -Math.PI / 2);
        assertEquals(Math.PI / 2, r.getAngle(), EPS); // angle is normalized to [0, pi]
        Vector3D axis = r.getAxis();
        // Axis should be opposite direction
        assertTrue(Vector3D.distance(new Vector3D(0, 0, -1), axis) < EPS);
    }

    @Test
    public void testApplyToVectorWithLargeValues() {
        Rotation r = new Rotation(new Vector3D(1, 0, 0), Math.PI / 2);
        Vector3D v = new Vector3D(1e6, 2e6, 3e6);
        Vector3D result = r.applyTo(v);
        // Should not overflow
        assertFalse(Double.isNaN(result.getX()));
        assertFalse(Double.isNaN(result.getY()));
        assertFalse(Double.isNaN(result.getZ()));
    }

    @Test
    public void testGetAnglesWithGimbalLock() {
        // Gimbal lock: second angle = pi/2, first and third angles are not uniquely defined
        Rotation r = new Rotation(new Vector3D(0, 1, 0), Math.PI / 2);
        double[] angles = r.getAngles();
        // Should not throw exception or return NaN
        assertFalse(Double.isNaN(angles[0]));
        assertFalse(Double.isNaN(angles[1]));
        assertFalse(Double.isNaN(angles[2]));
        // Verify that the rotation matrix matches
        Rotation reconstructed = new Rotation(
            new Rotation(new Vector3D(1, 0, 0), angles[0]),
            new Rotation(new Vector3D(0, 1, 0), angles[1]),
            new Rotation(new Vector3D(0, 0, 1), angles[2])
        );
        assertTrue(r.applyTo(new Vector3D(1, 0, 0)).distance(reconstructed.applyTo(new Vector3D(1, 0, 0))) < 1e-6);
    }

    // ========== Additional Coverage: equals/hashCode (if present) ==========
    // Rotation class does not override equals, but we can test that identity is not null

    @Test
    public void testIdentityNotNull() {
        assertNotNull(identity);
    }

    // ========== Test for bug Math-52: getAngles returning NaN ==========
    @Test
    public void testBugMath52() {
        // This test reproduces the bug scenario: rotation with axis near (0,0,1) and angle near pi/2
        // The getAngles method should not return NaN
        Vector3D axis = new Vector3D(0, 0, 1);
        double angle = Math.PI / 2;
        Rotation r = new Rotation(axis, angle);
        double[] angles = r.getAngles();
        assertFalse("First angle is NaN", Double.isNaN(angles[0]));
        assertFalse("Second angle is NaN", Double.isNaN(angles[1]));
        assertFalse("Third angle is NaN", Double.isNaN(angles[2]));
        // Additional check: angles should be finite
        assertTrue(Double.isFinite(angles[0]));
        assertTrue(Double.isFinite(angles[1]));
        assertTrue(Double.isFinite(angles[2]));
    }

    @Test
    public void testBugMath52NearSingularity() {
        // Near singularity where second angle is close to pi/2
        Vector3D axis = new Vector3D(0, 1, 0);
        double angle = Math.PI / 2 - 1e-12;
        Rotation r = new Rotation(axis, angle);
        double[] angles = r.getAngles();
        assertFalse("First angle is NaN", Double.isNaN(angles[0]));
        assertFalse("Second angle is NaN", Double.isNaN(angles[1]));
        assertFalse("Third angle is NaN", Double.isNaN(angles[2]));
    }

    @Test
    public void testBugMath52AnotherAxis() {
        // Another axis that might cause issues
        Vector3D axis = new Vector3D(1, 1, 0).normalize();
        double angle = 0.5;
        Rotation r = new Rotation(axis, angle);
        double[] angles = r.getAngles();
        assertFalse("First angle is NaN", Double.isNaN(angles[0]));
        assertFalse("Second angle is NaN", Double.isNaN(angles[1]));
        assertFalse("Third angle is NaN", Double.isNaN(angles[2]));
    }
}