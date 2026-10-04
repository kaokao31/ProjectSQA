package org.apache.commons.math.linear;

import org.junit.Test;
import static org.junit.Assert.*;

public class EigenDecompositionImplTest {

    @Test
    public void testMath80BugTrigger() {
        // This test specifically targets the bug in Math 80 where 
        // EigenDecompositionImpl's initial 4 * (n - 1) transform 
        // used a reference to an un-allocated or incorrectly-sized work array
        // or skipped/flipped shift indices during QZ-like decompositions (findEigenVector).
        
        // Let's construct a matrix that historically triggered Math 80 or similar out of bounds/wrong eigenvector issues.
        // A known matrix configuration that exposes improper shift handling or secondary diagonal extraction in Math 80:
        double[][] data = {
            { 0.0, 1.0, 0.0, 0.0 },
            { 1.0, 0.0, 1.0, 0.0 },
            { 0.0, 1.0, 0.0, 1.0 },
            { 0.0, 0.0, 1.0, 0.0 }
        };

        RealMatrix matrix = MatrixUtils.createRealMatrix(data);
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix, 0.0);

        // Accessing getV(), getD(), getRealEigenvalues() forces the internal transformations
        // and eigenvector computations where the 4*(n-1) bug resides.
        RealMatrix v = ed.getV();
        assertNotNull(v);
        
        RealMatrix d = ed.getD();
        assertNotNull(d);

        double[] eigenvalues = ed.getRealEigenvalues();
        assertEquals(4, eigenvalues.length);

        for (int i = 0; i < eigenvalues.length; i++) {
            assertNotNull(ed.getEigenvector(i));
        }
    }

    @Test
    public void testIdentityMatrixEigenvalues() {
        RealMatrix matrix = MatrixUtils.createRealIdentityMatrix(3);
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix, 0.0);
        
        double[] realEigenvalues = ed.getRealEigenvalues();
        assertEquals(3, realEigenvalues.length);
        assertEquals(1.0, realEigenvalues[0], 1e-12);
        assertEquals(1.0, realEigenvalues[1], 1e-12);
        assertEquals(1.0, realEigenvalues[2], 1e-12);

        RealMatrix V = ed.getV();
        assertNotNull(V);
        RealMatrix D = ed.getD();
        assertNotNull(D);
        
        assertEquals(1.0, ed.getDeterminant(), 1e-12);
        assertEquals(3.0, ed.getTrace(), 1e-12);
    }

    @Test
    public void testDiagonalMatrixEigenvalues() {
        double[][] data = {
            { 2.0, 0.0, 0.0 },
            { 0.0, 3.0, 0.0 },
            { 0.0, 0.0, 5.0 }
        };
        RealMatrix matrix = MatrixUtils.createRealMatrix(data);
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix, 0.0);

        double[] realEigenvalues = ed.getRealEigenvalues();
        assertEquals(3, realEigenvalues.length);
        
        // Check components
        assertNotNull(ed.getV());
        assertNotNull(ed.getVT());
        assertNotNull(ed.getD());
    }

    @Test(expected = InvalidMatrixException.class)
    public void testNonSymmetricMatrix() {
        // EigenDecompositionImpl in Apache Commons Math usually expects symmetric matrices 
        // depending on constructor, but let's pass a non-symmetric one or test 
        // behavior with flag set to false if applicable, or check constructor constraints.
        double[][] data = {
            { 1.0, 2.0, 3.0 },
            { 4.0, 5.0, 6.0 },
            { 7.0, 8.0, 9.0 }
        };
        RealMatrix matrix = MatrixUtils.createRealMatrix(data);
        // Depending on constructor, pass split/tolerance parameters
        new EigenDecompositionImpl(matrix, 0.0);
    }

    @Test
    public void testImaginaryEigenvalues() {
        // A simple rotation-like or asymmetric matrix that yields complex eigenvalues 
        // if supported, or handled gracefully.
        double[][] data = {
            { 0.0, -1.0 },
            { 1.0,  0.0 }
        };
        RealMatrix matrix = MatrixUtils.createRealMatrix(data);
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix, 0.0);
        
        assertEquals(2, ed.getImagEigenvalues().length);
    }

    @Test
    public void testSolversAndProperties() {
        double[][] data = {
            { 1.0, 2.0 },
            { 2.0, 1.0 }
        };
        RealMatrix matrix = MatrixUtils.createRealMatrix(data);
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix, 1e-14);

        DecompositionSolver solver = ed.getSolver();
        assertTrue(solver.isNonSingular());

        RealVector b = new ArrayRealVector(new double[] { 3.0, 3.0 });
        RealVector x = solver.solve(b);
        assertNotNull(x);
    }
}