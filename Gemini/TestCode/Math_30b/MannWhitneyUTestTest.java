package org.apache.commons.math3.stat.inference;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.exception.DimensionMismatchException;

public class MannWhitneyUTestTest {

    private MannWhitneyUTest testInstance;

    @Before
    public void setUp() {
        testInstance = new MannWhitneyUTest();
    }

    @Test
    public void testMannWhitneyUStandard() {
        double[] x = {1.0, 2.0, 5.0};
        double[] y = {3.0, 4.0, 6.0};
        double u = testInstance.mannWhitneyU(x, y);
        // Sum ranks for x: 1.0 (rank 1), 2.0 (rank 2), 5.0 (rank 5) -> sum = 8
        // U1 = 8 - 3*(4)/2 = 8 - 6 = 2
        Assert.assertEquals(2.0, u, 1e-6);
    }

    @Test
    public void testMannWhitneyUTestBasic() {
        double[] x = {19.0, 22.0, 16.0, 29.0, 24.0};
        double[] y = {20.0, 11.0, 17.0, 12.0};
        double pValue = testInstance.mannWhitneyUTest(x, y);
        Assert.assertTrue(pValue >= 0.0 && pValue <= 1.0);
    }

    @Test(expected = NullArgumentException.class)
    public void testNullFirstSampleU() {
        double[] y = {1.0, 2.0};
        testInstance.mannWhitneyU(null, y);
    }

    @Test(expected = NullArgumentException.class)
    public void testNullSecondSampleU() {
        double[] x = {1.0, 2.0};
        testInstance.mannWhitneyU(x, null);
    }

    @Test(expected = NoDataException.class)
    public void testEmptyFirstSampleU() {
        double[] x = {};
        double[] y = {1.0, 2.0};
        testInstance.mannWhitneyU(x, y);
    }

    @Test(expected = NoDataException.class)
    public void testEmptySecondSampleU() {
        double[] x = {1.0, 2.0};
        double[] y = {};
        testInstance.mannWhitneyU(x, y);
    }

    @Test(expected = NullArgumentException.class)
    public void testNullFirstSampleTest() {
        double[] y = {1.0, 2.0};
        testInstance.mannWhitneyUTest(null, y);
    }

    @Test(expected = NullArgumentException.class)
    public void testNullSecondSampleTest() {
        double[] x = {1.0, 2.0};
        testInstance.mannWhitneyUTest(x, null);
    }

    @Test(expected = NoDataException.class)
    public void testEmptyFirstSampleTest() {
        double[] x = {};
        double[] y = {1.0, 2.0};
        testInstance.mannWhitneyUTest(x, y);
    }

    @Test(expected = NoDataException.class)
    public void testEmptySecondSampleTest() {
        double[] x = {1.0, 2.0};
        double[] y = {};
        testInstance.mannWhitneyUTest(x, y);
    }

    @Test
    public void testMannWhitneyUTiedValues() {
        // Test with tied values to exercise the tie-correction branch in calculateAsymptoticPValue / rank handling
        double[] x = {1.0, 2.0, 2.0, 3.0};
        double[] y = {2.0, 3.0, 4.0, 5.0};
        double pValue = testInstance.mannWhitneyUTest(x, y);
        Assert.assertTrue(pValue >= 0.0 && pValue <= 1.0);
    }

    @Test
    public void testLargeSampleAsymptotic() {
        // Generate arrays large enough (> 30 combined or as configured in implementation) to trigger normal approximation
        double[] x = new double[20];
        double[] y = new double[20];
        for (int i = 0; i < 20; i++) {
            x[i] = i;
            y[i] = i + 10;
        }
        double pValue = testInstance.mannWhitneyUTest(x, y);
        Assert.assertTrue(pValue >= 0.0 && pValue <= 1.0);
    }
}