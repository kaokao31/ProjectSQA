package pbt.harness;

import net.jqwik.api.lifecycle.*;

/**
 * Global jqwik hook (registered via META-INF/services) used by scripts/run_jqwik.py.
 *
 * - -Dpbt.seed=<long>   : forces the same seed for every property that does not fix its own seed,
 *                         so buggy and fixed versions see the same generated inputs and every
 *                         run is reproducible.
 * - -Dpbt.tries=<int>   : overrides tries for properties that do not set tries themselves.
 */
public class SeedControlHook implements AroundPropertyHook {

    @Override
    public PropagationMode propagateTo() {
        return PropagationMode.ALL_DESCENDANTS;
    }

    @Override
    public boolean appliesTo(java.util.Optional<java.lang.reflect.AnnotatedElement> element) {
        return true;
    }

    @Override
    public PropertyExecutionResult aroundProperty(PropertyLifecycleContext context, PropertyExecutor property) {
        String seed = System.getProperty("pbt.seed");
        if (seed != null && !seed.isEmpty() && !context.attributes().seed().isPresent()) {
            context.attributes().setSeed(seed);
        }
        String tries = System.getProperty("pbt.tries");
        if (tries != null && !tries.isEmpty() && !context.attributes().tries().isPresent()) {
            context.attributes().setTries(Integer.valueOf(tries));
        }
        return property.execute();
    }

    @Override
    public int aroundPropertyProximity() {
        return -100; // outermost: must run before the property is started
    }
}
