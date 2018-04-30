package at.hagenberg.master.montecarlo.simulation;

import at.hagenberg.master.montecarlo.entities.*;
import at.hagenberg.master.montecarlo.simulation.settings.LeagueSettings;
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
        this.matchList = initializeMatchList(settings);
    }

    protected abstract List<T> initializeMatchList(LeagueSettings settings);

    public abstract SimulationResult runSimulation();
}
