package org.apache.commons.math.geometry.euclidean.threed;

import org.junit.Assert;
import org.junit.Test;

public class RotationTest {

    @Test
    public synchronized void testConstructorScalarAndVectorOrder() {
        // Test primary constructor: Rotation(double q0, double q1, double q2, double q3, boolean needsNormalization)
        Rotation r1 = new Rotation(1.0, 0.0, 0.0, 0.0, false);
        Assert.assertEquals(1.0, r1.getQ0(), 1.0e-12);
        Assert.assertEquals(0.0, r1.getQ1(), 1.0e-12);
        Assert.assertEquals(0.0, r1.getQ2(), 1.0e-12);
        Assert.assertEquals(0.0, r1.getQ3(), 1.0e-12);

        // Test normalization branch when needsNormalization is true
        Rotation r2 = new Rotation(2.0, 0.0, 0.0, 0.0, true);
        Assert.assertEquals(1.0, r2.getQ0(), 1.0e-12);
        Assert.assertEquals(0.0, r2.getQ1(), 1.0e-12);
        Assert.assertEquals(0.0, r2.getQ2(), 1.0e-12);
        Assert.assertEquals(0.0, r2.getQ3(), 1.0e-12);
    }

    @Test
    public synchronized void testConstructorVectorAndAngle() {
        Vector3D axis = new Vector3D(1.0, 0.0, 0.0);
        Rotation r = new Rotation(axis, Math.PI);
        Assert.assertEquals(0.0, r.getQ0(), 1.0e-12);
        Assert.assertEquals(1.0, r.getQ1(), 1.0e-12);
        Assert.assertEquals(0.0, r.getQ2(), 1.0e-12);
        Assert.assertEquals(0.0, r.getQ3(), 1.0e-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public synchronized void testConstructorVectorAndAngleZeroNorm() {
        Vector3D zeroAxis = new Vector3D(0.0, 0.0, 0.0);
        new Rotation(zeroAxis, Math.PI);
    }

    @Test
    public synchronized void testConstructorMatrix() {
        // Identity matrix
        double[][] m = {
            {1.0, 0.0, 0.0},
            {0.0, 1.0, 0.0},
            {0.0, 0.0, 1.0}
        };
        try {
            Rotation r = new Rotation(m, 1.0e-10);
            Assert.assertEquals(1.0, r.getQ0(), 1.0e-12);
        } catch (NotARotationMatrixException e) {
            Assert.fail("Identity matrix should be a valid rotation matrix");
        }
    }

    @Test(expected = NotARotationMatrixException.class)
    public synchronized void testConstructorInvalidMatrix() throws NotARotationMatrixException {
        // Bad matrix (determinant not 1)
        double[][] m = {
            {2.0, 0.0, 0.0},
            {0.0, 2.0, 0.0},
            {0.0, 0.0, 2.0}
        };
        new Rotation(m, 1.0e-10);
    }

    @Test
    public synchronized void testConstructorVectorPairs() {
        Vector3D u1 = new Vector3D(1.0, 0.0, 0.0);
        Vector3D u2 = new Vector3D(0.0, 1.0, 0.0);
        Vector3D v1 = new Vector3D(0.0, 1.0, 0.0);
        Vector3D v2 = new Vector3D(-1.0, 0.0, 0.0);

        Rotation r = new Rotation(u1, u2, v1, v2);
        Vector3D applied = r.applyTo(u1);
        Assert.assertEquals(v1.getX(), applied.getX(), 1.0e-12);
        Assert.assertEquals(v1.getY(), applied.getY(), 1.0e-12);
        Assert.assertEquals(v1.getZ(), applied.getZ(), 1.0e-12);
    }

    @Test
    public synchronized void testConstructorCardanAngles() {
        try {
            Rotation r = new Rotation(RotationOrder.XYZ, 0.1, 0.2, 0.3);
            double[] angles = r.getAngles(RotationOrder.XYZ);
            Assert.assertEquals(0.1, angles[0], 1.0e-10);
            Assert.assertEquals(0.2, angles[1], 1.0e-10);
            Assert.assertEquals(0.3, angles[2], 1.0e-10);
        } catch (CardanEulerSingularityException e) {
            Assert.fail("Unexpected singularity");
        }
    }

    @Test
    public synchronized void testGetQuaternion() {
        Rotation r = new Rotation(0.5, 0.5, 0.5, 0.5, true);
        Assert.assertEquals(0.5, r.getQ0(), 1.0e-12);
        Assert.assertEquals(0.5, r.getQ1(), 1.0e-12);
        Assert.assertEquals(0.5, r.getQ2(), 1.0e-12);
        Assert.assertEquals(0.5, r.getQ3(), 1.0e-12);
    }

    @Test
    public synchronized void testGetAxis() {
        Rotation r = new Rotation(new Vector3D(0.0, 0.0, 1.0), Math.PI / 2);
        Vector3D axis = r.getAxis();
        Assert.assertEquals(0.0, axis.getX(), 1.0e-12);
        Assert.assertEquals(0.0, axis.getY(), 1.0e-12);
        Assert.assertEquals(1.0, Math.abs(axis.getZ()), 1.0e-12);
    }

    @Test
    public synchronized void testGetAngle() {
        Rotation r = new Rotation(new Vector3D(1.0, 0.0, 0.0), Math.PI / 3);
        Assert.assertEquals(Math.PI / 3, r.getAngle(), 1.0e-12);
    }

    @Test
    public synchronized void testGetMatrix() {
        Rotation r = Rotation.IDENTITY;
        double[][] m = r.getMatrix();
        Assert.assertEquals(1.0, m[0][0], 1.0e-12);
        Assert.assertEquals(0.0, m[0][1], 1.0e-12);
        Assert.assertEquals(0.0, m[0][2], 1.0e-12);
        Assert.assertEquals(0.0, m[1][0], 1.0e-12);
        Assert.assertEquals(1.0, m[1][1], 1.0e-12);
        Assert.assertEquals(0.0, m[1][2], 1.0e-12);
        Assert.assertEquals(0.0, m[2][0], 1.0e-12);
        Assert.assertEquals(0.0, m[2][1], 1.0e-12);
        Assert.assertEquals(1.0, m[2][2], 1.0e-12);
    }

    @Test
    public synchronized void testApplyToVector3D() {
        Rotation r = new Rotation(new Vector3D(0.0, 0.0, 1.0), Math.PI / 2);
        Vector3D v = new Vector3D(1.0, 0.0, 0.0);
        Vector3D result = r.applyTo(v);
        Assert.assertEquals(0.0, result.getX(), 1.0e-12);
        Assert.assertEquals(1.0, result.getY(), 1.0e-12);
        Assert.assertEquals(0.0, result.getZ(), 1.0e-12);
    }

    @Test
    public synchronized void testApplyToRotation() {
        Rotation r1 = new Rotation(new Vector3D(0.0, 0.0, 1.0), Math.PI / 2);
        Rotation r2 = new Rotation(new Vector3D(0.0, 0.0, 1.0), Math.PI / 2);
        Rotation composed = r1.applyTo(r2);
        Assert.assertEquals(Math.PI, composed.getAngle(), 1.0e-10);
    }

    @Test
    public synchronized void testApplyInverseToVector3D() {
        Rotation r = new Rotation(new Vector3D(0.0, 0.0, 1.0), Math.PI / 2);
        Vector3D v = new Vector3D(0.0, 1.0, 0.0);
        Vector3D result = r.applyInverseTo(v);
        Assert.assertEquals(1.0, result.getX(), 1.0e-12);
        Assert.assertEquals(0.0, result.getY(), 1.0e-12);
        Assert.assertEquals(0.0, result.getZ(), 1.0e-12);
    }

    @Test
    public synchronized void testApplyInverseToRotation() {
        Rotation r1 = new Rotation(new Vector3D(0.0, 0.0, 1.0), Math.PI / 2);
        Rotation r2 = new Rotation(new Vector3D(0.0, 0.0, 1.0), Math.PI / 2);
        Rotation composed = r1.applyInverseTo(r2);
        Assert.assertEquals(0.0, composed.getAngle(), 1.0e-10);
    }

    @Test
    public synchronized void testReversed() {
        Rotation r = new Rotation(new Vector3D(1.0, 0.0, 0.0), Math.PI / 4);
        Rotation rev = r.revers();
        Assert.assertEquals(-r.getQ1(), rev.getQ1(), 1.0e-12);
        Assert.assertEquals(-r.getQ2(), rev.getQ2(), 1.0e-12);
        Assert.assertEquals(-r.getQ3(), rev.getQ3(), 1.0e-12);
    }

    @Test
    public synchronized void testDistance() {
        Rotation r1 = Rotation.IDENTITY;
        Rotation r2 = new Rotation(new Vector3D(1.0, 0.0, 0.0), Math.PI / 2);
        double dist = Rotation.distance(r1, r2);
        Assert.assertEquals(Math.PI / 2, dist, 1.0e-12);
    }

    @Test
    public synchronized void testIdentityConstant() {
        Rotation id = Rotation.IDENTITY;
        Assert.assertEquals(1.0, id.getQ0(), 1.0e-12);
        Assert.assertEquals(0.0, id.getQ1(), 1.0e-12);
        Assert.assertEquals(0.0, id.getQ2(), 1.0e-12);
        Assert.assertEquals(0.0, id.getQ3(), 1.0e-12);
    }
}