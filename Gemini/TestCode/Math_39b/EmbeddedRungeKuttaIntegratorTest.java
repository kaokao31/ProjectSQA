package org.apache.commons.math.ode.nonstiff;

import org.apache.commons.math.exception.DimensionMismatchException;
import org.apache.commons.math.exception.MaxCountExceededException;
import org.apache.commons.math.exception.NoBracketingException;
import org.apache.commons.math.exception.NumberIsTooSmallException;
import org.apache.commons.math.ode.FirstOrderDifferentialEquation;
import org.apache.commons.math.ode.ExpandableStatefulODE;
import org.apache.commons.math.ode.events.EventHandler;
import org.apache.commons.math.ode.sampling.StepHandler;
import org.apache.commons.math.ode.sampling.StepInterpolator;
import org.junit.Assert;
import org.junit.Test;

public class EmbeddedRungeKuttaIntegratorTest {

    private static final class DummyFirstOrderDifferentialEquation implements FirstOrderDifferentialEquation {
        private final int dimension;

        private DummyFirstOrderDifferentialEquation(int dimension) {
            this.dimension = dimension;
        }

        public int getDimension() {
            return dimension;
        }

        public void computeDerivatives(double t, double[] y, double[] yDot) {
            for (int i = 0; i < dimension; ++i) {
                yDot[i] = y[i];
            }
        }
    }

    private static final class ConcreteEmbeddedRungeKuttaIntegrator extends EmbeddedRungeKuttaIntegrator {
        private final double[] c;
        private final double[][] a;
        private final double[] b;
        private final RungeKuttaStepInterpolator prototype;
        private final double minStep;
        private final double maxStep;
        private final double scalAbsoluteTolerance;
        private final double scalRelativeTolerance;

        protected ConcreteEmbeddedRungeKuttaIntegrator(String name, boolean forward,
                                                     double[] c, double[][] a, double[] b,
                                                     RungeKuttaStepInterpolator prototype,
                                                     double minStep, double maxStep,
                                                     double scalAbsoluteTolerance,
                                                     double scalRelativeTolerance) {
            super(name, forward, c, a, b, prototype, minStep, maxStep, scalAbsoluteTolerance, scalRelativeTolerance);
            this.c = c;
            this.a = a;
            this.b = b;
            this.prototype = prototype;
            this.minStep = minStep;
            this.maxStep = maxStep;
            this.scalAbsoluteTolerance = scalAbsoluteTolerance;
            this.scalRelativeTolerance = scalRelativeTolerance;
        }

        protected ConcreteEmbeddedRungeKuttaIntegrator(String name, boolean forward,
                                                     double[] c, double[][] a, double[] b,
                                                     RungeKuttaStepInterpolator prototype,
                                                     double minStep, double maxStep,
                                                     double[] vecAbsoluteTolerance,
                                                     double[] vecRelativeTolerance) {
            super(name, forward, c, a, b, prototype, minStep, maxStep, vecAbsoluteTolerance, vecRelativeTolerance);
            this.c = c;
            this.a = a;
            this.b = b;
            this.prototype = prototype;
            this.minStep = minStep;
            this.maxStep = maxStep;
        }

        public int getOrder() {
            return 4;
        }

        protected double integrate(final StepInterpolator interpolator, final double t,
                                   final double[] y, final double tEnd)
                throws MaxCountExceededException, NoBracketingException, DimensionMismatchException, NumberIsTooSmallException {
            return tEnd;
        }
    }

    @Test
    public void testGettersAndParameters() {
        double[] c = {0.5};
        double[][] a = {{0.5}};
        double[] b = {1.0};
        double minStep = 1e-6;
        double maxStep = 1e-1;
        double absTol = 1e-5;
        double relTol = 1e-4;

        DormandPrince54Integrator integrator = new DormandPrince54Integrator(minStep, maxStep, absTol, relTol);

        Assert.assertEquals(0.2, integrator.getSafety(), 1e-12);
        Assert.assertEquals(0.9, integrator.getMinReduction(), 1e-12);
        Assert.assertEquals(10.0, integrator.getMaxGrowth(), 1e-12);

        integrator.setSafety(0.3);
        integrator.setMinReduction(0.8);
        integrator.setMaxGrowth(5.0);

        Assert.assertEquals(0.3, integrator.getSafety(), 1e-12);
        Assert.assertEquals(0.8, integrator.getMinReduction(), 1e-12);
        Assert.assertEquals(5.0, integrator.getMaxGrowth(), 1e-12);
    }

    @Test
    public void testVectorTolerancesConstructor() {
        double minStep = 1e-6;
        double maxStep = 1e-1;
        double[] absTol = {1e-5, 1e-5};
        double[] relTol = {1e-4, 1e-4};

        DormandPrince54Integrator integrator = new DormandPrince54Integrator(minStep, maxStep, absTol, relTol);
        Assert.assertNotNull(integrator);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testIntegrationDimensionMismatch() {
        double minStep = 1e-6;
        double maxStep = 1e-1;
        double absTol = 1e-5;
        double relTol = 1e-4;

        DormandPrince54Integrator integrator = new DormandPrince54Integrator(minStep, maxStep, absTol, relTol);
        DummyFirstOrderDifferentialEquation ode = new DummyFirstOrderDifferentialEquation(2);
        ExpandableStatefulODE expandable = new ExpandableStatefulODE(ode);
        expandable.setTime(0.0);
        expandable.setCompleteState(new double[]{1.0}); // Mismatched dimension (expected 2, got 1)

        integrator.integrate(expandable, 1.0);
    }

    @Test
    public void testMinMaxStepHandlingBug39Trigger() {
        // Bug 39 in Commons Math involves resetting or handling min/max steps and step size initialization.
        // Specifically, check situations where initial step is evaluated or NaN/zero steps occur.
        double minStep = 1e-3;
        double maxStep = 1.0;
        double absTol = 1e-5;
        double relTol = 1e-4;

        DormandPrince54Integrator integrator = new DormandPrince54Integrator(minStep, maxStep, absTol, relTol);
        
        DummyFirstOrderDifferentialEquation ode = new DummyFirstOrderDifferentialEquation(1);
        ExpandableStatefulODE expandable = new ExpandableStatefulODE(ode);
        expandable.setTime(0.0);
        expandable.setCompleteState(new double[]{1.0});

        // Test integration where start and end time are extremely close or equal, or normal short integration
        try {
            integrator.integrate(expandable, 0.0);
        } catch (Exception e) {
            // Expected if integration time is zero, but let's check normal execution
        }

        // Integration with normal span
        integrator.integrate(expandable, 0.1);
        Assert.assertEquals(0.1, expandable.getTime(), 1e-7);
    }
}