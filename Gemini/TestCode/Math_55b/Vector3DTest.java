package org.apache.commons.math.geometry;

import org.junit.Assert;
import org.junit.Test;

public class Vector3DTest {

    private static final double TOLERANCE = 1e-12;

    @Test
    public void testConstructorsAndGetters() {
        Vector3D v1 = new Vector3D(1.0, 2.0, 3.0);
        Assert.assertEquals(1.0, v1.getX(), TOLERANCE);
        Assert.assertEquals(2.0, v1.getY(), TOLERANCE);
        Assert.assertEquals(3.0, v1.getZ(), TOLERANCE);

        Vector3D v2 = new Vector3D(2.0, new Vector3D(1.0, 2.0, 3.0));
        Assert.assertEquals(2.0, v2.getX(), TOLERANCE);
        Assert.assertEquals(4.0, v2.getY(), TOLERANCE);
        Assert.assertEquals(6.0, v2.getZ(), TOLERANCE);

        Vector3D v3 = new Vector3D(2.0, v1, 3.0, v2);
        Assert.assertEquals(2.0 * 1.0 + 3.0 * 2.0, v3.getX(), TOLERANCE);
        Assert.assertEquals(2.0 * 2.0 + 3.0 * 4.0, v3.getY(), TOLERANCE);
        Assert.assertEquals(2.0 * 3.0 + 3.0 * 6.0, v3.getZ(), TOLERANCE);

        Vector3D v4 = new Vector3D(1.0, v1, 2.0, v2, 3.0, new Vector3D(1, 1, 1));
        Assert.assertEquals(1.0 * 1.0 + 2.0 * 2.0 + 3.0 * 1.0, v4.getX(), TOLERANCE);
        Assert.assertEquals(1.0 * 2.0 + 2.0 * 4.0 + 3.0 * 1.0, v4.getY(), TOLERANCE);
        Assert.assertEquals(1.0 * 3.0 + 2.0 * 6.0 + 3.0 * 1.0, v4.getZ(), TOLERANCE);

        Vector3D v5 = new Vector3D(1.0, v1, 2.0, v2, 3.0, new Vector3D(1, 1, 1), 4.0, new Vector3D(1, 1, 1));
        Assert.assertEquals(1.0 * 1.0 + 2.0 * 2.0 + 3.0 * 1.0 + 4.0 * 1.0, v5.getX(), TOLERANCE);
        Assert.assertEquals(1.0 * 2.0 + 2.0 * 4.0 + 3.0 * 1.0 + 4.0 * 1.0, v5.getY(), TOLERANCE);
        Assert.assertEquals(1.0 * 3.0 + 2.0 * 6.0 + 3.0 * 1.0 + 4.0 * 1.0, v5.getZ(), TOLERANCE);

        double[] alphaDelta = new double[] { 1.0, 0.5 };
        Vector3D v6 = new Vector3D(2.5, alphaDelta);
        Assert.assertNotNull(v6);

        Vector3D alphaDeltaV = new Vector3D(1.2, 0.3);
        Assert.assertEquals(1.2, alphaDeltaV.getAlpha(), TOLERANCE);
        Assert.assertEquals(0.3, alphaDeltaV.getDelta(), TOLERANCE);
    }

    @Test
    public void testNorms() {
        Vector3D v = new Vector3D(3.0, 4.0, 12.0);
        Assert.assertEquals(13.0, v.getNorm(), TOLERANCE);
        Assert.assertEquals(169.0, v.getNormSq(), TOLERANCE);
        Assert.assertEquals(13.0, Vector3D.distance(v, Vector3D.ZERO), TOLERANCE);
        Assert.assertEquals(169.0, Vector3D.distanceSq(v, Vector3D.ZERO), TOLERANCE);

        Vector3D vZero = new Vector3D(0, 0, 0);
        Assert.assertEquals(0.0, vZero.getNorm(), TOLERANCE);
        Assert.assertEquals(0.0, vZero.getNormSq(), TOLERANCE);
    }

    @Test
    public void testArithmetic() {
        Vector3D v1 = new Vector3D(1.0, 2.0, 3.0);
        Vector3D v2 = new Vector3D(4.0, 5.0, 6.0);

        Vector3D add = v1.add(v2);
        Assert.assertEquals(5.0, add.getX(), TOLERANCE);
        Assert.assertEquals(7.0, add.getY(), TOLERANCE);
        Assert.assertEquals(9.0, add.getZ(), TOLERANCE);

        Vector3D addScale = v1.add(2.0, v2);
        Assert.assertEquals(9.0, addScale.getX(), TOLERANCE);
        Assert.assertEquals(12.0, addScale.getY(), TOLERANCE);
        Assert.assertEquals(15.0, addScale.getZ(), TOLERANCE);

        Vector3D sub = v2.subtract(v1);
        Assert.assertEquals(3.0, sub.getX(), TOLERANCE);
        Assert.assertEquals(3.0, sub.getY(), TOLERANCE);
        Assert.assertEquals(3.0, sub.getZ(), TOLERANCE);

        Vector3D subScale = v2.subtract(2.0, v1);
        Assert.assertEquals(2.0, subScale.getX(), TOLERANCE);
        Assert.assertEquals(1.0, subScale.getY(), TOLERANCE);
        Assert.assertEquals(0.0, subScale.getZ(), TOLERANCE);

        Vector3D norm = v1.normalize();
        Assert.assertEquals(1.0, norm.getNorm(), TOLERANCE);

        Vector3D ortho = v1.orthogonal();
        Assert.assertEquals(0.0, Vector3D.dotProduct(v1, ortho), TOLERANCE);

        Vector3D neg = v1.negate();
        Assert.assertEquals(-1.0, neg.getX(), TOLERANCE);
        Assert.assertEquals(-2.0, neg.getY(), TOLERANCE);
        Assert.assertEquals(-3.0, neg.getZ(), TOLERANCE);

        Vector3D scale = v1.scalarMultiply(2.0);
        Assert.assertEquals(2.0, scale.getX(), TOLERANCE);
        Assert.assertEquals(4.0, scale.getY(), TOLERANCE);
        Assert.assertEquals(6.0, scale.getZ(), TOLERANCE);
    }

    @Test
    public void testProducts() {
        Vector3D v1 = new Vector3D(1.0, 0.0, 0.0);
        Vector3D v2 = new Vector3D(0.0, 1.0, 0.0);

        Assert.assertEquals(0.0, Vector3D.dotProduct(v1, v2), TOLERANCE);
        Assert.assertEquals(0.0, v1.dotProduct(v2), TOLERANCE);

        Vector3D cross = Vector3D.crossProduct(v1, v2);
        Assert.assertEquals(0.0, cross.getX(), TOLERANCE);
        Assert.assertEquals(0.0, cross.getY(), TOLERANCE);
        Assert.assertEquals(1.0, cross.getZ(), TOLERANCE);

        Vector3D crossInstance = v1.crossProduct(v2);
        Assert.assertEquals(cross.getX(), crossInstance.getX(), TOLERANCE);

        double angle = Vector3D.angle(v1, v2);
        Assert.assertEquals(Math.PI / 2.0, angle, TOLERANCE);
    }

    @Test
    public void testSpecialsAndConstants() {
        Assert.assertEquals(0.0, Vector3D.ZERO.getX(), TOLERANCE);
        Assert.assertEquals(1.0, Vector3D.PLUS_I.getX(), TOLERANCE);
        Assert.assertEquals(-1.0, Vector3D.MINUS_I.getX(), TOLERANCE);
        Assert.assertEquals(1.0, Vector3D.PLUS_J.getY(), TOLERANCE);
        Assert.assertEquals(-1.0, Vector3D.MINUS_J.getY(), TOLERANCE);
        Assert.assertEquals(1.0, Vector3D.PLUS_K.getZ(), TOLERANCE);
        Assert.assertEquals(-1.0, Vector3D.MINUS_K.getZ(), TOLERANCE);
        Assert.assertEquals(Double.NaN, Vector3D.NaN.getX(), TOLERANCE);
        Assert.assertEquals(Double.POSITIVE_INFINITY, Vector3D.POSITIVE_INFINITY.getX(), TOLERANCE);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, Vector3D.NEGATIVE_INFINITY.getX(), TOLERANCE);

        Assert.assertTrue(Vector3D.NaN.isNaN());
        Assert.assertFalse(Vector3D.ZERO.isNaN());
        Assert.assertTrue(Vector3D.POSITIVE_INFINITY.isInfinite());
        Assert.assertFalse(Vector3D.ZERO.isInfinite());
    }

    @Test
    public void testEqualsAndHashCode() {
        Vector3D v1 = new Vector3D(1.0, 2.0, 3.0);
        Vector3D v2 = new Vector3D(1.0, 2.0, 3.0);
        Vector3D v3 = new Vector3D(1.0, 2.0, 4.0);
        Vector3D vNaN = new Vector3D(Double.NaN, 0.0, 0.0);

        Assert.assertEquals(v1, v1);
        Assert.assertEquals(v1, v2);
        Assert.assertEquals(v1.hashCode(), v2.hashCode());
        Assert.assertNotEquals(v1, v3);
        Assert.assertNotEquals(v1, null);
        Assert.assertNotEquals(v1, "SomeString");

        Assert.assertEquals(vNaN, new Vector3D(Double.NaN, 1.0, 1.0));
        Assert.assertNotEquals(Vector3D.ZERO, vNaN);
    }

    @Test
    public void testToString() {
        Vector3D v = new Vector3D(1.0, 2.0, 3.0);
        Assert.assertNotNull(v.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testZeroNormNormalization() {
        Vector3D.ZERO.normalize();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testZeroNormOrthogonal() {
        Vector3D.ZERO.orthogonal();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAngleZeroNorm1() {
        Vector3D.angle(Vector3D.ZERO, Vector3D.PLUS_I);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAngleZeroNorm2() {
        Vector3D.angle(Vector3D.PLUS_I, Vector3D.ZERO);
    }

    @Test
    public void testBuggyCrossProductMethod() {
        // Specifically targeting crossProduct scale/precision bugs common in Vector3D (e.g. Defects4J Math 55)
        Vector3D v1 = new Vector3D(9.007199254740992E15, 0.0, 1.0);
        Vector3D v2 = new Vector3D(0.0, 9.007199254740992E15, 1.0);
        Vector3D cp = Vector3D.crossProduct(v1, v2);
        Assert.assertNotNull(cp);
    }
}