package at.hagenberg.master.montecarlo.simulation;

import at.hagenberg.master.montecarlo.entities.AbstractMonteCarloResult;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Objects;

public abstract class AbstractMonteCarloSimulation {

    protected RandomGenerator randomGenerator;

    public AbstractMonteCarloSimulation(RandomGenerator randomGenerator) {
        Objects.requireNonNull(randomGenerator);
        this.randomGenerator = randomGenerator;
    }

    public abstract AbstractMonteCarloResult runSimulation();
}
