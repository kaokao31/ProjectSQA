package org.apache.commons.math.ode.nonstiff;

import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.FirstOrderDifferentialEquation;
import org.apache.commons.math.ode.IntegratorException;
import org.junit.Assert;
import org.junit.Test;

public class EmbeddedRungeKuttaIntegratorTest {

    private static class DummyStepHandler implements org.apache.commons.math.ode.sampling.StepHandler {
        private boolean called = false;

        public void handleStep(org.apache.commons.math.ode.sampling.StepInterpolator interpolator, boolean isLast)
                throws DerivativeException {
            called = true;
        }

        public boolean requiresDenseOutput() {
            return false;
        }

        public void reset() {
            called = false;
        }

        public boolean isCalled() {
            return called;
        }
    }

    private static class SimpleEquation implements FirstOrderDifferentialEquation {
        private final int dimension;

        public SimpleEquation(int dimension) {
            this.dimension = dimension;
        }

        public int getDimension() {
            return dimension;
        }

        public void computeDerivatives(double t, double[] y, double[] yDot)
                throws DerivativeException {
            for (int i = 0; i < dimension; ++i) {
                yDot[i] = y[i];
            }
        }
    }

    @Test
    public void testExceedMaxEvaluations() {
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince54Integrator(
                1.0e-8, 100.0, 1.0e-10, 1.0e-10);
        integrator.setMaxEvaluations(2);

        FirstOrderDifferentialEquation ode = new SimpleEquation(1);
        double[] y = new double[] { 1.0 };

        try {
            integrator.integrate(ode, 0.0, y, 5.0, new double[1]);
            Assert.fail("Expected IntegratorException");
        } catch (IntegratorException e) {
            // Expected
        } catch (DerivativeException e) {
            Assert.fail("Unexpected DerivativeException");
        }
    }

    @Test
    public void testIntegrationStepSizeControl() throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince853Integrator(
                1.0e-12, 10.0, 1.0e-8, 1.0e-8);
        
        integrator.setSafety(0.9);
        Assert.assertEquals(0.9, integrator.getSafety(), 1.0e-12);

        integrator.setMinReduction(0.2);
        Assert.assertEquals(0.2, integrator.getMinReduction(), 1.0e-12);

        integrator.setMaxGrowth(10.0);
        Assert.assertEquals(10.0, integrator.getMaxGrowth(), 1.0e-12);

        FirstOrderDifferentialEquation ode = new SimpleEquation(2);
        double[] y = new double[] { 1.0, 0.0 };
        double[] yDot = new double[2];

        DummyStepHandler stepHandler = new DummyStepHandler();
        integrator.addStepHandler(stepHandler);

        double tEnd = integrator.integrate(ode, 0.0, y, 1.0, yDot);
        Assert.assertEquals(1.0, tEnd, 1.0e-6);
        Assert.assertTrue(stepHandler.isCalled());
    }

    @Test
    public void testGettersAndParameters() {
        EmbeddedRungeKuttaIntegrator integrator = new RungeKutta45Integrator(
                "RK45", 0.01, 1.0, 1.0e-5, 1.0e-5);
        
        Assert.assertEquals(0.01, integrator.getMinStep(), 1.0e-12);
        Assert.assertEquals(1.0, integrator.getMaxStep(), 1.0e-12);
        Assert.assertNotNull(integrator.getName());
    }

    @Test
    public void testBackwardIntegration() throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator = new HighamHall54Integrator(
                1.0e-8, 1.0, 1.0e-10, 1.0e-10);
        
        FirstOrderDifferentialEquation ode = new SimpleEquation(1);
        double[] y = new double[] { 1.0 };
        double[] yDot = new double[1];

        double tEnd = integrator.integrate(ode, 1.0, y, 0.0, yDot);
        Assert.assertEquals(0.0, tEnd, 1.0e-6);
    }
}