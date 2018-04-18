package at.hagenberg.master.montecarlo.simulation.settings;

import at.hagenberg.master.montecarlo.entities.Opponent;
import at.hagenberg.master.montecarlo.simulation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LeagueSettings<T extends Opponent, K extends Match> {

    private AbstractPredictionModel predictionModel;
    protected List<T> opponentList;
    private Map<Integer, List<K>> roundGameResults;

    private final int roundsPerSeason;
    private final int roundsToSimulate;

    public LeagueSettings(AbstractPredictionModel predictionModel, List<T> opponentList, Map<Integer, List<K>> roundGameResults,
                          final int roundsPerSeason, final int roundsToSimulate) {
        Objects.requireNonNull(predictionModel);
        Objects.requireNonNull(opponentList);
        Objects.requireNonNull(roundGameResults);

        if(roundsPerSeason <= 0 || roundsPerSeason < (roundsPerSeason - roundsToSimulate))
            throw new IllegalArgumentException("Invalid amount of rounds and games per match");

        if(roundsToSimulate > roundsPerSeason)
            throw new IllegalArgumentException("roundToSimulate must not be greater than roundPerSeason");

        this.roundsPerSeason = roundsPerSeason;
        this.roundsToSimulate = roundsToSimulate;
        this.predictionModel = predictionModel;
        this.opponentList = opponentList;
        this.roundGameResults = roundGameResults;
    }


    public AbstractPredictionModel getPredictionModel() { return predictionModel; }

    public List<T> getOpponentList() {
        return opponentList;
    }

    public Map<Integer, List<K>> getRoundGameResults() {
        return roundGameResults;
    }

    public int getRoundsPerSeason() {
        return roundsPerSeason;
    }

    public int getRoundsToSimulate() {
        return roundsToSimulate;
    }

    public int getPlayedRounds() {
        return roundsPerSeason - roundsToSimulate;
    }
}
