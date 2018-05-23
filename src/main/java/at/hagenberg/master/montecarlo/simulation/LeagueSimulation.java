package at.hagenberg.master.montecarlo.simulation;

import at.hagenberg.master.montecarlo.entities.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;

public abstract class LeagueSimulation<T extends Match> {

    protected RandomGenerator randomGenerator;
    protected LeagueSettings settings;
    protected List<T> matchList;

    public LeagueSimulation(RandomGenerator randomGenerator, LeagueSettings settings) {
        Objects.requireNonNull(randomGenerator);
        Objects.requireNonNull(settings);

        this.randomGenerator = randomGenerator;
        this.settings = settings;
        this.matchList = initializeSimulation(settings);
    }

    protected abstract List<T> initializeSimulation(LeagueSettings settings);

    public abstract SimulationResult runSimulation();
}
