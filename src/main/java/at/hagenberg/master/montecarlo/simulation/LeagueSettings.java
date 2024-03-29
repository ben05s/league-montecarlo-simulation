package at.hagenberg.master.montecarlo.simulation;

import at.hagenberg.master.montecarlo.entities.Opponent;
import at.hagenberg.master.montecarlo.lineup.AbstractLineupSelector;
import at.hagenberg.master.montecarlo.lineup.LineupSelector;
import at.hagenberg.master.montecarlo.lineup.OptimizedLineup;
import at.hagenberg.master.montecarlo.prediction.PredictionModel;
import at.hagenberg.master.montecarlo.simulation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LeagueSettings<T extends Opponent> {

    private PredictionModel predictionModel;
    private List<T> opponentList;
    private Map<Integer, List<HeadToHeadMatch>> roundGameResults = new HashMap<>();

    private AbstractLineupSelector lineupSelector;
    private OptimizedLineup optimizedLineup;

    private int roundsPerSeason;
    private int roundsToSimulate;

    public LeagueSettings(PredictionModel predictionModel, List<T> opponentList, final int roundsPerSeason) {
        Objects.requireNonNull(predictionModel);
        Objects.requireNonNull(opponentList);

        if(roundsPerSeason <= 0)
            throw new IllegalArgumentException("Invalid amount of rounds per season");

        this.predictionModel = predictionModel;
        this.opponentList = opponentList;
        this.roundsPerSeason = roundsPerSeason;
        this.roundsToSimulate = 0;
    }

    public LeagueSettings(PredictionModel predictionModel, List<T> opponentList, final int roundsPerSeason, AbstractLineupSelector lineupSelector, OptimizedLineup optimizedLineup) {
        this(predictionModel, opponentList, roundsPerSeason);

        Objects.requireNonNull(lineupSelector);

        if(lineupSelector.getGamesPerMatch() <= 0)
            throw new IllegalArgumentException("Games per Match must be a number higher than 0");

        this.lineupSelector = lineupSelector;
        this.optimizedLineup = optimizedLineup;
    }

    public LeagueSettings(PredictionModel predictionModel, List<T> opponentList, final int roundsPerSeason, AbstractLineupSelector lineupSelector, OptimizedLineup optimizedLineup, final int roundsToSimulate, Map<Integer, List<HeadToHeadMatch>> roundGameResults) {
        this(predictionModel, opponentList, roundsPerSeason, lineupSelector, optimizedLineup);

        Objects.requireNonNull(roundGameResults);

        if(roundsToSimulate > roundsPerSeason)
            throw new IllegalArgumentException("roundToSimulate must not be greater than roundPerSeason");

        this.roundsToSimulate = roundsToSimulate;
        this.roundGameResults = roundGameResults;
    }

    public PredictionModel getPredictionModel() { return predictionModel; }

    public List<T> getOpponentList() {
        return opponentList;
    }

    public Map<Integer, List<HeadToHeadMatch>> getRoundGameResults() {
        return roundGameResults;
    }

    public int getRoundsPerSeason() {
        return roundsPerSeason;
    }

    public int getRoundsToSimulate() {
        return roundsToSimulate;
    }

    public void setRoundsToSimulate(final int roundsToSimulate) {
        this.roundsToSimulate = roundsToSimulate;
    }

    public int getPlayedRounds() {
        return roundsPerSeason - roundsToSimulate;
    }

    public AbstractLineupSelector getLineupSelector() {
        return this.lineupSelector;
    }

    public OptimizedLineup getOptimizedLineup() {
        return optimizedLineup;
    }
}
