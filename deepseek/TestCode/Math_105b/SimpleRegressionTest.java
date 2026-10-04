package org.apache.commons.math.stat.regression;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class SimpleRegressionTest {

    private SimpleRegression regression;
    private static final double DELTA = 1e-10;

    @Before
    public void setUp() {
        regression = new SimpleRegression();
    }

    // ---------- Empty regression (n=0) ----------
    @Test
    public void testEmptyRegression() {
        assertEquals(0, regression.getN());
        assertTrue(Double.isNaN(regression.getSlope()));
        assertTrue(Double.isNaN(regression.getIntercept()));
        assertTrue(Double.isNaN(regression.getSlopeStdErr()));
        assertTrue(Double.isNaN(regression.getInterceptStdErr()));
        assertTrue(Double.isNaN(regression.getMeanSquareError()));
        assertTrue(Double.isNaN(regression.getR()));
        assertTrue(Double.isNaN(regression.getRSquare()));
        assertTrue(Double.isNaN(regression.getRegressionSumSquares()));
        assertTrue(Double.isNaN(regression.getSumSquaredErrors()));
        assertTrue(Double.isNaN(regression.getTotalSumSquares()));
        assertTrue(Double.isNaN(regression.predict(0.0)));
    }

    // ---------- Single point (n=1) ----------
    @Test
    public void testSinglePoint() {
        regression.addData(1.0, 2.0);
        assertEquals(1, regression.getN());
        assertTrue(Double.isNaN(regression.getSlope()));
        assertTrue(Double.isNaN(regression.getIntercept()));
        assertTrue(Double.isNaN(regression.getSlopeStdErr()));
        assertTrue(Double.isNaN(regression.getInterceptStdErr()));
        assertTrue(Double.isNaN(regression.getMeanSquareError()));
        assertTrue(Double.isNaN(regression.getR()));
        assertTrue(Double.isNaN(regression.getRSquare()));
        assertTrue(Double.isNaN(regression.getRegressionSumSquares()));
        assertTrue(Double.isNaN(regression.getSumSquaredErrors()));
        assertTrue(Double.isNaN(regression.getTotalSumSquares()));
        assertTrue(Double.isNaN(regression.predict(0.0)));
    }

    // ---------- Two points (perfect line) ----------
    @Test
    public void testTwoPoints() {
        regression.addData(0.0, 0.0);
        regression.addData(1.0, 1.0);
        assertEquals(2, regression.getN());
        assertEquals(1.0, regression.getSlope(), DELTA);
        assertEquals(0.0, regression.getIntercept(), DELTA);
        assertEquals(0.0, regression.getMeanSquareError(), DELTA);
        assertEquals(1.0, regression.getR(), DELTA);
        assertEquals(1.0, regression.getRSquare(), DELTA);
        assertEquals(0.5, regression.getTotalSumSquares(), DELTA);
        assertEquals(0.5, regression.getRegressionSumSquares(), DELTA);
        assertEquals(0.0, regression.getSumSquaredErrors(), DELTA);
        // predict
        assertEquals(2.0, regression.predict(2.0), DELTA);
        assertEquals(-1.0, regression.predict(-1.0), DELTA);
    }

    // ---------- Three points with known regression ----------
    @Test
    public void testThreePoints() {
        // points: (1,2), (2,3), (3,5)
        regression.addData(1.0, 2.0);
        regression.addData(2.0, 3.0);
        regression.addData(3.0, 5.0);
        assertEquals(3, regression.getN());
        // slope = (n*sumXY - sumX*sumY) / (n*sumXX - sumX^2)
        // sumX=6, sumY=10, sumXY=1*2+2*3+3*5=2+6+15=23, sumXX=1+4+9=14
        // slope = (3*23 - 6*10) / (3*14 - 36) = (69-60)/(42-36)=9/6=1.5
        assertEquals(1.5, regression.getSlope(), DELTA);
        // intercept = (sumY - slope*sumX)/n = (10 - 1.5*6)/3 = (10-9)/3 = 1/3 ≈ 0.3333333333
        assertEquals(1.0/3.0, regression.getIntercept(), DELTA);
        // total sum squares = sumY^2 - (sumY)^2/n = (4+9+25) - 100/3 = 38 - 33.333... = 4.6666667
        assertEquals(4.666666666666667, regression.getTotalSumSquares(), DELTA);
        // regression sum squares = slope * (sumXY - sumX*sumY/n) = 1.5 * (23 - 60/3) = 1.5 * (23-20) = 1.5*3 = 4.5
        assertEquals(4.5, regression.getRegressionSumSquares(), DELTA);
        // error sum squares = total - regression = 4.6666667 - 4.5 = 0.1666667
        assertEquals(0.16666666666666674, regression.getSumSquaredErrors(), DELTA);
        // mean square error = error / (n-2) = 0.1666667 / 1 = 0.1666667
        assertEquals(0.16666666666666674, regression.getMeanSquareError(), DELTA);
        // R^2 = regression / total = 4.5 / 4.6666667 = 0.9642857
        assertEquals(0.9642857142857143, regression.getRSquare(), DELTA);
        // R = sqrt(R^2) = 0.9819805
        assertEquals(0.9819805060619657, regression.getR(), DELTA);
        // slope std err = sqrt(MSE / sumXX - sumX^2/n) = sqrt(0.1666667 / (14 - 36/3)) = sqrt(0.1666667 / (14-12)) = sqrt(0.1666667/2) = sqrt(0.0833333) = 0.2886751
        assertEquals(0.2886751345948129, regression.getSlopeStdErr(), DELTA);
        // intercept std err = sqrt(MSE * (1/n + meanX^2 / (sumXX - sumX^2/n))) = sqrt(0.1666667 * (1/3 + (2)^2 / 2)) = sqrt(0.1666667 * (0.33333 + 4/2)) = sqrt(0.1666667 * (0.33333+2)) = sqrt(0.1666667*2.33333) = sqrt(0.3888889) = 0.6236096
        assertEquals(0.6236095644623237, regression.getInterceptStdErr(), DELTA);
        // predict at x=4: 1.5*4 + 0.33333 = 6.33333
        assertEquals(6.333333333333333, regression.predict(4.0), DELTA);
    }

    // ---------- Constant x (zero variance) ----------
    @Test
    public void testConstantX() {
        regression.addData(1.0, 2.0);
        regression.addData(1.0, 3.0);
        regression.addData(1.0, 5.0);
        assertEquals(3, regression.getN());
        assertTrue(Double.isNaN(regression.getSlope()));
        assertTrue(Double.isNaN(regression.getIntercept()));
        assertTrue(Double.isNaN(regression.getSlopeStdErr()));
        assertTrue(Double.isNaN(regression.getInterceptStdErr()));
        assertTrue(Double.isNaN(regression.getMeanSquareError()));
        assertTrue(Double.isNaN(regression.getR()));
        assertTrue(Double.isNaN(regression.getRSquare()));
        assertTrue(Double.isNaN(regression.getRegressionSumSquares()));
        assertTrue(Double.isNaN(regression.getSumSquaredErrors()));
        assertTrue(Double.isNaN(regression.getTotalSumSquares()));
        assertTrue(Double.isNaN(regression.predict(2.0)));
    }

    // ---------- Constant y (zero slope) ----------
    @Test
    public void testConstantY() {
        regression.addData(1.0, 5.0);
        regression.addData(2.0, 5.0);
        regression.addData(3.0, 5.0);
        assertEquals(3, regression.getN());
        assertEquals(0.0, regression.getSlope(), DELTA);
        assertEquals(5.0, regression.getIntercept(), DELTA);
        assertEquals(0.0, regression.getMeanSquareError(), DELTA);
        assertEquals(0.0, regression.getR(), DELTA);
        assertEquals(0.0, regression.getRSquare(), DELTA);
        assertEquals(0.0, regression.getTotalSumSquares(), DELTA);
        assertEquals(0.0, regression.getRegressionSumSquares(), DELTA);
        assertEquals(0.0, regression.getSumSquaredErrors(), DELTA);
        assertEquals(5.0, regression.predict(100.0), DELTA);
    }

    // ---------- Negative values ----------
    @Test
    public void testNegativeValues() {
        regression.addData(-2.0, -4.0);
        regression.addData(-1.0, -2.0);
        regression.addData(0.0, 0.0);
        regression.addData(1.0, 2.0);
        regression.addData(2.0, 4.0);
        assertEquals(5, regression.getN());
        assertEquals(2.0, regression.getSlope(), DELTA);
        assertEquals(0.0, regression.getIntercept(), DELTA);
        assertEquals(0.0, regression.getMeanSquareError(), DELTA);
        assertEquals(1.0, regression.getR(), DELTA);
        assertEquals(1.0, regression.getRSquare(), DELTA);
        assertEquals(40.0, regression.getTotalSumSquares(), DELTA);
        assertEquals(40.0, regression.getRegressionSumSquares(), DELTA);
        assertEquals(0.0, regression.getSumSquaredErrors(), DELTA);
        assertEquals(6.0, regression.predict(3.0), DELTA);
        assertEquals(-4.0, regression.predict(-2.0), DELTA);
    }

    // ---------- Large numbers (overflow check) ----------
    @Test
    public void testLargeNumbers() {
        regression.addData(1e10, 1e10);
        regression.addData(2e10, 2e10);
        assertEquals(2, regression.getN());
        assertEquals(1.0, regression.getSlope(), DELTA);
        assertEquals(0.0, regression.getIntercept(), DELTA);
        assertEquals(0.0, regression.getMeanSquareError(), DELTA);
        assertEquals(1.0, regression.getR(), DELTA);
        assertEquals(1.0, regression.getRSquare(), DELTA);
        assertEquals(5e19, regression.getTotalSumSquares(), 1e10);
        assertEquals(5e19, regression.getRegressionSumSquares(), 1e10);
        assertEquals(0.0, regression.getSumSquaredErrors(), DELTA);
        assertEquals(3e10, regression.predict(3e10), DELTA);
    }

    // ---------- Remove data ----------
    @Test
    public void testRemoveData() {
        regression.addData(1.0, 2.0);
        regression.addData(2.0, 3.0);
        regression.addData(3.0, 5.0);
        assertEquals(3, regression.getN());
        // remove middle point
        regression.removeData(2.0, 3.0);
        assertEquals(2, regression.getN());
        // now points: (1,2) and (3,5)
        assertEquals(1.5, regression.getSlope(), DELTA);
        assertEquals(0.5, regression.getIntercept(), DELTA);
        assertEquals(0.0, regression.getMeanSquareError(), DELTA);
        assertEquals(1.0, regression.getR(), DELTA);
        assertEquals(1.0, regression.getRSquare(), DELTA);
        assertEquals(4.5, regression.getTotalSumSquares(), DELTA);
        assertEquals(4.5, regression.getRegressionSumSquares(), DELTA);
        assertEquals(0.0, regression.getSumSquaredErrors(), DELTA);
        assertEquals(3.5, regression.predict(2.0), DELTA);
    }

    // ---------- Remove all data ----------
    @Test
    public void testRemoveAllData() {
        regression.addData(1.0, 2.0);
        regression.addData(2.0, 3.0);
        regression.removeData(1.0, 2.0);
        regression.removeData(2.0, 3.0);
        assertEquals(0, regression.getN());
        assertTrue(Double.isNaN(regression.getSlope()));
        assertTrue(Double.isNaN(regression.getIntercept()));
        assertTrue(Double.isNaN(regression.predict(0.0)));
    }

    // ---------- Add data with array ----------
    @Test
    public void testAddDataArray() {
        double[][] data = {{1.0, 2.0}, {2.0, 3.0}, {3.0, 5.0}};
        regression.addData(data);
        assertEquals(3, regression.getN());
        assertEquals(1.5, regression.getSlope(), DELTA);
        assertEquals(1.0/3.0, regression.getIntercept(), DELTA);
    }

    // ---------- Remove data with array ----------
    @Test
    public void testRemoveDataArray() {
        double[][] data = {{1.0, 2.0}, {2.0, 3.0}, {3.0, 5.0}};
        regression.addData(data);
        double[][] remove = {{2.0, 3.0}};
        regression.removeData(remove);
        assertEquals(2, regression.getN());
        assertEquals(1.5, regression.getSlope(), DELTA);
        assertEquals(0.5, regression.getIntercept(), DELTA);
    }

    // ---------- Edge: add same point multiple times ----------
    @Test
    public void testAddSamePointMultipleTimes() {
        regression.addData(1.0, 2.0);
        regression.addData(1.0, 2.0);
        assertEquals(2, regression.getN());
        // two identical points: slope undefined (x constant)
        assertTrue(Double.isNaN(regression.getSlope()));
        assertTrue(Double.isNaN(regression.getIntercept()));
    }

    // ---------- Edge: remove non-existent point ----------
    @Test
    public void testRemoveNonExistentPoint() {
        regression.addData(1.0, 2.0);
        regression.removeData(2.0, 3.0); // not present
        assertEquals(1, regression.getN());
        // should still have one point
        assertTrue(Double.isNaN(regression.getSlope()));
    }

    // ---------- Test getSlopeStdErr with n=2 ----------
    @Test
    public void testSlopeStdErrWithTwoPoints() {
        regression.addData(0.0, 0.0);
        regression.addData(1.0, 1.0);
        // MSE = 0, so std err = 0? Actually, with n=2, MSE=0, slope std err = sqrt(0/(sumXX - sumX^2/n)) = 0
        assertEquals(0.0, regression.getSlopeStdErr(), DELTA);
        assertEquals(0.0, regression.getInterceptStdErr(), DELTA);
    }

    // ---------- Test getSlopeStdErr with n=3 and non-zero error ----------
    @Test
    public void testSlopeStdErrNonZero() {
        regression.addData(0.0, 0.0);
        regression.addData(1.0, 2.0);
        regression.addData(2.0, 4.0);
        // points: (0,0), (1,2), (2,4) -> perfect line slope 2, intercept 0, MSE=0
        assertEquals(0.0, regression.getSlopeStdErr(), DELTA);
        assertEquals(0.0, regression.getInterceptStdErr(), DELTA);
    }

    // ---------- Test getR with negative correlation ----------
    @Test
    public void testNegativeCorrelation() {
        regression.addData(0.0, 3.0);
        regression.addData(1.0, 1.0);
        regression.addData(2.0, -1.0);
        // slope = -2, intercept = 3, R = -1
        assertEquals(-1.0, regression.getR(), DELTA);
        assertEquals(1.0, regression.getRSquare(), DELTA);
    }

    // ---------- Test getR with no correlation ----------
    @Test
    public void testNoCorrelation() {
        regression.addData(0.0, 1.0);
        regression.addData(1.0, 2.0);
        regression.addData(2.0, 1.0);
        // points: (0,1), (1,2), (2,1) -> slope 0, intercept 4/3? Actually compute: sumX=3, sumY=4, sumXY=0*1+1*2+2*1=4, sumXX=0+1+4=5, n=3
        // slope = (3*4 - 3*4)/(3*5 - 9) = (12-12)/(15-9)=0/6=0
        // intercept = (4 - 0*3)/3 = 4/3 ≈ 1.3333
        // total sum squares = (1+4+1) - 16/3 = 6 - 5.3333 = 0.6667
        // regression sum squares = 0
        // error sum squares = 0.6667
        // R = 0
        assertEquals(0.0, regression.getR(), DELTA);
        assertEquals(0.0, regression.getRSquare(), DELTA);
        assertEquals(0.0, regression.getSlope(), DELTA);
        assertEquals(4.0/3.0, regression.getIntercept(), DELTA);
    }

    // ---------- Test predict with NaN slope (should return NaN) ----------
    @Test
    public void testPredictWithNaN() {
        regression.addData(1.0, 2.0);
        assertTrue(Double.isNaN(regression.predict(0.0)));
    }

    // ---------- Test getN after add and remove ----------
    @Test
    public void testGetN() {
        assertEquals(0, regression.getN());
        regression.addData(1.0, 2.0);
        assertEquals(1, regression.getN());
        regression.addData(2.0, 3.0);
        assertEquals(2, regression.getN());
        regression.removeData(1.0, 2.0);
        assertEquals(1, regression.getN());
        regression.removeData(2.0, 3.0);
        assertEquals(0, regression.getN());
    }

    // ---------- Test clear ----------
    @Test
    public void testClear() {
        regression.addData(1.0, 2.0);
        regression.addData(2.0, 3.0);
        regression.clear();
        assertEquals(0, regression.getN());
        assertTrue(Double.isNaN(regression.getSlope()));
        assertTrue(Double.isNaN(regression.getIntercept()));
    }

    // ---------- Test addData with large number of points ----------
    @Test
    public void testManyPoints() {
        for (int i = 0; i < 1000; i++) {
            regression.addData(i, 2 * i + 1);
        }
        assertEquals(1000, regression.getN());
        assertEquals(2.0, regression.getSlope(), 1e-10);
        assertEquals(1.0, regression.getIntercept(), 1e-10);
        assertEquals(0.0, regression.getMeanSquareError(), 1e-10);
        assertEquals(1.0, regression.getR(), 1e-10);
        assertEquals(1.0, regression.getRSquare(), 1e-10);
    }

    // ---------- Test regression with floating point precision ----------
    @Test
    public void testFloatingPointPrecision() {
        regression.addData(0.1, 0.2);
        regression.addData(0.2, 0.3);
        regression.addData(0.3, 0.5);
        // same as three points but scaled
        assertEquals(1.5, regression.getSlope(), 1e-10);
        assertEquals(0.05, regression.getIntercept(), 1e-10); // intercept = (0.2+0.3+0.5 - 1.5*(0.1+0.2+0.3))/3 = (1.0 - 1.5*0.6)/3 = (1.0 - 0.9)/3 = 0.1/3 = 0.03333? Wait recalc:
        // sumX=0.6, sumY=1.0, sumXY=0.1*0.2+0.2*0.3+0.3*0.5=0.02+0.06+0.15=0.23, sumXX=0.01+0.04+0.09=0.14
        // slope = (3*0.23 - 0.6*1.0)/(3*0.14 - 0.36) = (0.69 - 0.6)/(0.42 - 0.36) = 0.09/0.06 = 1.5
        // intercept = (1.0 - 1.5*0.6)/3 = (1.0 - 0.9)/3 = 0.1/3 = 0.0333333
        assertEquals(0.03333333333333333, regression.getIntercept(), 1e-10);
    }

    // ---------- Test getSlopeStdErr and getInterceptStdErr with n=3 and error ----------
    @Test
    public void testStdErrWithError() {
        regression.addData(0.0, 0.0);
        regression.addData(1.0, 1.0);
        regression.addData(2.0, 3.0);
        // points: (0,0), (1,1), (2,3) -> slope = (3* (0+1+6) - (0+1+2)*(0+1+3)) / (3*(0+1+4) - (0+1+2)^2) = (3*7 - 3*4)/(3*5 - 9) = (21-12)/(15-9)=9/6=1.5
        // intercept = (4 - 1.5*3)/3 = (4 - 4.5)/3 = -0.5/3 = -0.1666667
        // total sum squares = (0+1+9) - 16/3 = 10 - 5.3333 = 4.6667
        // regression sum squares = 1.5 * (7 - 3*4/3) = 1.5 * (7 - 4) = 1.5*3 = 4.5
        // error sum squares = 4.6667 - 4.5 = 0.1667
        // MSE = 0.1667/1 = 0.1667
        // sumXX - sumX^2/n = 5 - 9/3 = 5 - 3 = 2
        // slope std err = sqrt(0.1667/2) = sqrt(0.08333) = 0.288675
        assertEquals(0.2886751345948129, regression.getSlopeStdErr(), 1e-10);
        // intercept std err = sqrt(MSE * (1/n + meanX^2 / (sumXX - sumX^2/n))) = sqrt(0.1667 * (1/3 + (1)^2 / 2)) = sqrt(0.1667 * (0.33333 + 0.5)) = sqrt(0.1667 * 0.83333) = sqrt(0.1388889) = 0.372678
        assertEquals(0.3726779962499649, regression.getInterceptStdErr(), 1e-10);
    }

    // ---------- Test getRegressionSumSquares and getTotalSumSquares relationship ----------
    @Test
    public void testSumSquaresRelationship() {
        regression.addData(1.0, 2.0);
        regression.addData(2.0, 3.0);
        regression.addData(3.0, 5.0);
        double total = regression.getTotalSumSquares();
        double regressionSS = regression.getRegressionSumSquares();
        double errorSS = regression.getSumSquaredErrors();
        assertEquals(total, regressionSS + errorSS, 1e-10);
    }

    // ---------- Test getR with perfect negative correlation ----------
    @Test
    public void testPerfectNegativeCorrelation() {
        regression.addData(0.0, 2.0);
        regression.addData(1.0, 1.0);
        regression.addData(2.0, 0.0);
        assertEquals(-1.0, regression.getR(), DELTA);
        assertEquals(1.0, regression.getRSquare(), DELTA);
    }

    // ---------- Test addData with null array (should handle gracefully) ----------
    @Test(expected = NullPointerException.class)
    public void testAddDataNullArray() {
        regression.addData((double[][]) null);
    }

    // ---------- Test removeData with null array ----------
    @Test(expected = NullPointerException.class)
    public void testRemoveDataNullArray() {
        regression.removeData((double[][]) null);
    }
}