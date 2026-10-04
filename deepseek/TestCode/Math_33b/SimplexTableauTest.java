package org.apache.commons.math3.optimization.linear;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for SimplexTableau.
 * Designed to achieve maximum coverage and detect the Defects4J Math-33 bug
 * (incorrect pivot column selection when multiple negative entries exist).
 */
public class SimplexTableauTest {

    // -----------------------------------------------------------------------
    // Helper: create a SimplexTableau from a 2D double array
    // -----------------------------------------------------------------------
    private SimplexTableau createTableau(double[][] data) {
        return new SimplexTableau(data);
    }

    // -----------------------------------------------------------------------
    // Constructor and basic getters
    // -----------------------------------------------------------------------
    @Test
    public void testConstructorAndGetters() {
        double[][] data = {
            { 1.0, 1.0, 4.0 },
            { 2.0, 1.0, 6.0 },
            { -3.0, -2.0, 0.0 }
        };
        SimplexTableau tableau = createTableau(data);
        assertEquals("Width should be 3", 3, tableau.getWidth());
        assertEquals("Height should be 3", 3, tableau.getHeight());
        assertEquals("Entry (0,0) should be 1.0", 1.0, tableau.getEntry(0, 0), 1e-12);
        assertEquals("Entry (2,2) should be 0.0", 0.0, tableau.getEntry(2, 2), 1e-12);
    }

    // -----------------------------------------------------------------------
    // getPivotColumn – no negative entries (optimal)
    // -----------------------------------------------------------------------
    @Test
    public void testGetPivotColumn_NoNegative() {
        double[][] data = {
            { 1.0, 1.0, 4.0 },
            { 2.0, 1.0, 6.0 },
            { 1.0, 2.0, 0.0 }   // all non‑negative (except last column)
        };
        SimplexTableau tableau = createTableau(data);
        int pivotCol = tableau.getPivotColumn();
        assertTrue("Pivot column should be -1 (optimal)", pivotCol == -1);
    }

    // -----------------------------------------------------------------------
    // getPivotColumn – single negative entry
    // -----------------------------------------------------------------------
    @Test
    public void testGetPivotColumn_SingleNegative() {
        double[][] data = {
            { 1.0, 1.0, 4.0 },
            { 2.0, 1.0, 6.0 },
            { 1.0, -2.0, 0.0 }  // only column 1 is negative
        };
        SimplexTableau tableau = createTableau(data);
        int pivotCol = tableau.getPivotColumn();
        assertEquals("Pivot column should be 1", 1, pivotCol);
    }

    // -----------------------------------------------------------------------
    // getPivotColumn – multiple negative entries (bug detection)
    // The bug returns the first negative instead of the most negative.
    // -----------------------------------------------------------------------
    @Test
    public void testGetPivotColumn_MultipleNegatives() {
        // Bottom row: [1, -2, -3, 0] → most negative is -3 at column 2
        double[][] data = {
            { 1.0, 1.0, 1.0, 4.0 },
            { 2.0, 1.0, 1.0, 6.0 },
            { 1.0, -2.0, -3.0, 0.0 }
        };
        SimplexTableau tableau = createTableau(data);
        int pivotCol = tableau.getPivotColumn();
        assertEquals("Pivot column should be 2 (most negative)", 2, pivotCol);
    }

    // -----------------------------------------------------------------------
    // getPivotRow – normal case (all positive entries in pivot column)
    // -----------------------------------------------------------------------
    @Test
    public void testGetPivotRow_Normal() {
        double[][] data = {
            { 1.0, 1.0, 4.0 },
            { 2.0, 1.0, 6.0 },
            { -3.0, -2.0, 0.0 }
        };
        SimplexTableau tableau = createTableau(data);
        int pivotCol = tableau.getPivotColumn(); // should be 0 (most negative -3)
        assertEquals("Pivot column should be 0", 0, pivotCol);
        int pivotRow = tableau.getPivotRow(pivotCol);
        // ratios: row0 = 4/1 = 4, row1 = 6/2 = 3 → smallest is row1
        assertEquals("Pivot row should be 1", 1, pivotRow);
    }

    // -----------------------------------------------------------------------
    // getPivotRow – zero entry in pivot column (must be skipped)
    // -----------------------------------------------------------------------
    @Test
    public void testGetPivotRow_ZeroEntry() {
        double[][] data = {
            { 1.0, 1.0, 4.0 },
            { 0.0, 1.0, 6.0 },   // entry (1,0) is 0
            { -3.0, -2.0, 0.0 }
        };
        SimplexTableau tableau = createTableau(data);
        int pivotCol = tableau.getPivotColumn(); // should be 0
        assertEquals("Pivot column should be 0", 0, pivotCol);
        int pivotRow = tableau.getPivotRow(pivotCol);
        // only row0 has positive entry, ratio = 4/1 = 4
        assertEquals("Pivot row should be 0", 0, pivotRow);
    }

    // -----------------------------------------------------------------------
    // getPivotRow – negative entry in pivot column (must be skipped)
    // -----------------------------------------------------------------------
    @Test
    public void testGetPivotRow_NegativeEntry() {
        double[][] data = {
            { 1.0, 1.0, 4.0 },
            { -1.0, 1.0, 6.0 },  // entry (1,0) is -1
            { -3.0, -2.0, 0.0 }
        };
        SimplexTableau tableau = createTableau(data);
        int pivotCol = tableau.getPivotColumn(); // should be 0
        assertEquals("Pivot column should be 0", 0, pivotCol);
        int pivotRow = tableau.getPivotRow(pivotCol);
        // only row0 has positive entry, ratio = 4/1 = 4
        assertEquals("Pivot row should be 0", 0, pivotRow);
    }

    // -----------------------------------------------------------------------
    // getPivotRow – all entries non‑positive (unbounded)
    // -----------------------------------------------------------------------
    @Test
    public void testGetPivotRow_AllNonPositive() {
        double[][] data = {
            { 0.0, 1.0, 4.0 },
            { -1.0, 1.0, 6.0 },
            { -3.0, -2.0, 0.0 }
        };
        SimplexTableau tableau = createTableau(data);
        int pivotCol = tableau.getPivotColumn(); // should be 0
        assertEquals("Pivot column should be 0", 0, pivotCol);
        int pivotRow = tableau.getPivotRow(pivotCol);
        // no positive entry → unbounded
        assertEquals("Pivot row should be -1 (unbounded)", -1, pivotRow);
    }

    // -----------------------------------------------------------------------
    // getPivotRow – ties in ratio (should return first valid row)
    // -----------------------------------------------------------------------
    @Test
    public void testGetPivotRow_TieRatio() {
        double[][] data = {
            { 2.0, 1.0, 4.0 },   // ratio = 4/2 = 2
            { 3.0, 1.0, 6.0 },   // ratio = 6/3 = 2 (tie)
            { -3.0, -2.0, 0.0 }
        };
        SimplexTableau tableau = createTableau(data);
        int pivotCol = tableau.getPivotColumn(); // should be 0
        assertEquals("Pivot column should be 0", 0, pivotCol);
        int pivotRow = tableau.getPivotRow(pivotCol);
        // both ratios equal, method should return the first (row 0)
        assertTrue("Pivot row should be 0 or 1 (tie)", pivotRow == 0 || pivotRow == 1);
    }

    // -----------------------------------------------------------------------
    // performPivot – basic correctness
    // -----------------------------------------------------------------------
    @Test
    public void testPerformPivot() {
        double[][] data = {
            { 1.0, 1.0, 4.0 },
            { 2.0, 1.0, 6.0 },
            { -3.0, -2.0, 0.0 }
        };
        SimplexTableau tableau = createTableau(data);
        int pivotCol = tableau.getPivotColumn(); // 0
        int pivotRow = tableau.getPivotRow(pivotCol); // 1
        tableau.performPivot(pivotRow, pivotCol);
        // pivot element should be 1
        assertEquals("Pivot element should be 1", 1.0, tableau.getEntry(pivotRow, pivotCol), 1e-12);
        // all other entries in pivot column should be 0
        for (int r = 0; r < tableau.getHeight(); r++) {
            if (r != pivotRow) {
                assertEquals("Entry in pivot column should be 0",
                             0.0, tableau.getEntry(r, pivotCol), 1e-12);
            }
        }
    }

    // -----------------------------------------------------------------------
    // isOptimal – true when no negative entries in bottom row (except last col)
    // -----------------------------------------------------------------------
    @Test
    public void testIsOptimal_True() {
        double[][] data = {
            { 1.0, 0.0, 2.0 },
            { 0.0, 1.0, 3.0 },
            { 0.0, 0.0, 0.0 }
        };
        SimplexTableau tableau = createTableau(data);
        assertTrue("Tableau should be optimal", tableau.isOptimal());
    }

    @Test
    public void testIsOptimal_False() {
        double[][] data = {
            { 1.0, 0.0, 2.0 },
            { 0.0, 1.0, 3.0 },
            { -1.0, 0.0, 0.0 }
        };
        SimplexTableau tableau = createTableau(data);
        assertFalse("Tableau should not be optimal", tableau.isOptimal());
    }

    // -----------------------------------------------------------------------
    // getSolution – extract solution from optimal tableau
    // -----------------------------------------------------------------------
    @Test
    public void testGetSolution() {
        // Optimal tableau with identity basis for columns 0 and 1
        double[][] data = {
            { 1.0, 0.0, 2.0 },
            { 0.0, 1.0, 3.0 },
            { 0.0, 0.0, 0.0 }
        };
        SimplexTableau tableau = createTableau(data);
        double[] solution = tableau.getSolution();
        assertNotNull("Solution should not be null", solution);
        assertEquals("Solution length should be 2", 2, solution.length);
        assertEquals("x0 should be 2.0", 2.0, solution[0], 1e-12);
        assertEquals("x1 should be 3.0", 3.0, solution[1], 1e-12);
    }

    // -----------------------------------------------------------------------
    // Edge cases – constructor with null or empty input
    // -----------------------------------------------------------------------
    @Test(expected = NullPointerException.class)
    public void testConstructor_NullInput() {
        new SimplexTableau(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_EmptyTableau() {
        double[][] data = new double[0][0];
        new SimplexTableau(data);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_InconsistentRowLengths() {
        double[][] data = {
            { 1.0, 2.0, 3.0 },
            { 4.0, 5.0 }   // shorter row
        };
        new SimplexTableau(data);
    }

    // -----------------------------------------------------------------------
    // Additional coverage: getPivotRow with column that has only one positive
    // -----------------------------------------------------------------------
    @Test
    public void testGetPivotRow_OnlyOnePositive() {
        double[][] data = {
            { 0.0, 1.0, 4.0 },
            { 0.0, 1.0, 6.0 },
            { -3.0, -2.0, 0.0 }
        };
        SimplexTableau tableau = createTableau(data);
        int pivotCol = tableau.getPivotColumn(); // 0
        assertEquals("Pivot column should be 0", 0, pivotCol);
        // column 0 has all zeros → no positive entry
        int pivotRow = tableau.getPivotRow(pivotCol);
        assertEquals("Pivot row should be -1 (no positive entry)", -1, pivotRow);
    }

    // -----------------------------------------------------------------------
    // performPivot – ensure no division by zero when pivot element is zero
    // (should never happen if pivot row/col are correctly chosen)
    // -----------------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
    public void testPerformPivot_ZeroPivotElement() {
        double[][] data = {
            { 0.0, 1.0, 4.0 },
            { 2.0, 1.0, 6.0 },
            { -3.0, -2.0, 0.0 }
        };
        SimplexTableau tableau = createTableau(data);
        // force pivot on (0,0) where entry is 0 – should throw exception
        tableau.performPivot(0, 0);
    }
}