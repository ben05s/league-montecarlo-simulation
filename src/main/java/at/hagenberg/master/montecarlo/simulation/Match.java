package at.hagenberg.master.montecarlo.simulation;

import at.hagenberg.master.montecarlo.entities.MatchResult;
import at.hagenberg.master.montecarlo.entities.Opponent;
import at.hagenberg.master.montecarlo.prediction.PredictionModel;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Objects;

public abstract class Match<T extends Opponent> {

    protected RandomGenerator randomGenerator;
    protected PredictionModel predictionModel;

    protected T opponentA;
    protected T opponentB;

    protected MatchResult matchPrediction;
    protected MatchResult matchResult;

    public Match() {}

    public Match(RandomGenerator randomGenerator, PredictionModel predictionModel, T opponentA, T opponentB) {
        this(randomGenerator, predictionModel, opponentA, opponentB, null);
    }

    public Match(RandomGenerator randomGenerator, PredictionModel predictionModel, T opponentA, T opponentB, MatchResult matchResult) {
        Objects.requireNonNull(randomGenerator);
        Objects.requireNonNull(predictionModel);
        Objects.requireNonNull(opponentA);
        Objects.requireNonNull(opponentB);

        this.randomGenerator = randomGenerator;
        this.predictionModel = predictionModel;
        this.opponentA = opponentA;
        this.opponentB = opponentB;
        this.matchResult = matchResult;
    }

    public abstract MatchResult playMatch();

    public abstract String print();

    public void addHeadToHeadMatchResult(HeadToHeadMatch match) {
        if(matchResult == null) matchResult = new MatchResult(opponentA, opponentB);
        matchResult.addGame(match, false);
    }

    public boolean isCorrectPrediction() {
        return this.matchPrediction.getWinner() == this.matchResult.getWinner();
    }

    public double getError() throws Exception {
        if(matchResult != null && matchPrediction != null) {
            return matchResult.getAbsoluteScore() - matchPrediction.getAbsoluteScore();
        }
        throw new Exception("No Prediction for this game yet");
    }

    public RandomGenerator getRandomGenerator() {
        return randomGenerator;
    }

    public PredictionModel getPredictionModel() {
        return predictionModel;
    }

    public MatchResult getMatchPrediction() {
        return matchPrediction;
    }

    public MatchResult getMatchResult() {
        return matchResult;
    }

    public T getOpponentA() {
        return opponentA;
    }

    public T getOpponentB() {
        return opponentB;
    }

    @Override
    public String toString() {
        return "Match{" +
                "randomGenerator=" + randomGenerator +
                ", predictionModel=" + predictionModel +
                ", opponentA=" + opponentA +
                ", opponentB=" + opponentB +
                ", matchPrediction=" + matchPrediction +
                ", matchResult=" + matchResult +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Match match = (Match) o;

        if (!opponentA.equals(match.opponentA) && !opponentA.equals(match.opponentB)) return false;
        return opponentB.equals(match.opponentB) || opponentB.equals(match.opponentA);
    }

    @Override
    public int hashCode() {
        int result = opponentA.hashCode();
        result = 31 * result + opponentB.hashCode();
        return result;
    }
}
