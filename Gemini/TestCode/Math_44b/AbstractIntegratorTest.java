package org.apache.commons.math.ode;

import org.apache.commons.math.analysis.solvers.UnivariateRealSolver;
import org.apache.commons.math.exception.DimensionMismatchException;
import org.apache.commons.math.exception.MaxCountExceededException;
import org.apache.commons.math.exception.NumberIsTooSmallException;
import org.apache.commons.math.ode.events.EventHandler;
import org.apache.commons.math.ode.sampling.StepHandler;
import org.apache.commons.math.ode.sampling.StepInterpolator;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;

public class AbstractIntegratorTest {

    private static final class ConcreteIntegrator extends AbstractIntegrator {
        public ConcreteIntegrator(final String name) {
            super(name);
        }

        @Override
        public void integrate(final ExpandableStatefulODE equations, final double t)
                throws DimensionMismatchException, NumberIsTooSmallException,
                MaxCountExceededException {
            // Dummy implementation for testing abstract class infrastructure
            setStepStart(equations.getTime());
            equations.setTime(t);
        }
    }

    private static final class DummyODE implements FirstOrderDifferentialEquations {
        @Override
        public int getDimension() {
            return 1;
        }

        @Override
        public void computeDerivatives(double t, double[] y, double[] yDot) {
            yDot[0] = 1.0;
        }
    }

    @Test
    public void testGetName() {
        AbstractIntegrator integrator = new ConcreteIntegrator("TestIntegrator");
        Assert.assertEquals("TestIntegrator", integrator.getName());
    }

    @Test
    public void testStepHandlers() {
        AbstractIntegrator integrator = new ConcreteIntegrator("TestIntegrator");
        Assert.assertTrue(integrator.getStepHandlers().isEmpty());

        StepHandler handler = new StepHandler() {
            @Override
            public void init(double t0, double[] y0, double t) {}
            @Override
            public void handleStep(StepInterpolator interpolator, boolean isLast) {}
        };

        integrator.addStepHandler(handler);
        Collection<StepHandler> handlers = integrator.getStepHandlers();
        Assert.assertEquals(1, handlers.size());
        Assert.assertTrue(handlers.contains(handler));

        integrator.clearStepHandlers();
        Assert.assertTrue(integrator.getStepHandlers().isEmpty());
    }

    @Test
    public void testEventHandlers() {
        AbstractIntegrator integrator = new ConcreteIntegrator("TestIntegrator");
        Assert.assertTrue(integrator.getEventHandlers().isEmpty());

        EventHandler handler = new EventHandler() {
            @Override
            public void init(double t0, double[] y0, double t) {}
            @Override
            public double g(double t, double[] y) { return 0; }
            @Override
            public Action eventOccurred(double t, double[] y, boolean increasing) { return Action.STOP; }
            @Override
            public void resetState(double t, double[] y) {}
        };

        integrator.addEventHandler(handler, 1.0, 1e-6, 100);
        Collection<EventHandler> handlers = integrator.getEventHandlers();
        Assert.assertEquals(1, handlers.size());
        Assert.assertTrue(handlers.contains(handler));

        integrator.clearEventHandlers();
        Assert.assertTrue(integrator.getEventHandlers().isEmpty());
    }

    @Test
    public void testMaxEvaluations() {
        AbstractIntegrator integrator = new ConcreteIntegrator("TestIntegrator");
        Assert.assertEquals(Integer.MAX_VALUE, integrator.getMaxEvaluations());

        integrator.setMaxEvaluations(500);
        Assert.assertEquals(500, integrator.getMaxEvaluations());
    }

    @Test
    public void testGetEvaluations() {
        AbstractIntegrator integrator = new ConcreteIntegrator("TestIntegrator");
        Assert.assertEquals(0, integrator.getEvaluations());
    }

    @Test
    public void testStepStartAndSize() {
        AbstractIntegrator integrator = new ConcreteIntegrator("TestIntegrator");
        Assert.assertTrue(Double.isNaN(integrator.getCurrentStepStart()));
        Assert.assertTrue(Double.isNaN(integrator.getCurrentSignedStep()));

        ExpandableStatefulODE ode = new ExpandableStatefulODE(new DummyODE());
        ode.setTime(0.0);
        ode.setCompleteState(new double[] { 1.0 });

        integrator.integrate(ode, 1.0);
        Assert.assertEquals(0.0, integrator.getCurrentStepStart(), 1e-12);
        Assert.assertEquals(1.0, ode.getTime(), 1e-12);
    }

    @Test
    public void testEvaluationsCounter() {
        AbstractIntegrator integrator = new ConcreteIntegrator("TestIntegrator");
        ExpandableStatefulODE ode = new ExpandableStatefulODE(new DummyODE());
        ode.setTime(0.0);
        ode.setCompleteState(new double[] { 0.0 });

        // Indirectly test evaluation tracking through computeDerivatives wrapper
        FirstOrderDifferentialEquations wrapped = integrator.computeDerivatives(ode);
        double[] yDot = new double[1];
        wrapped.computeDerivatives(0.0, new double[] { 0.0 }, yDot);
        Assert.assertEquals(1, integrator.getEvaluations());
    }
}