package at.hagenberg.master.montecarlo.simulation;

import at.hagenberg.master.montecarlo.entities.MatchResult;
import at.hagenberg.master.montecarlo.entities.Player;
import at.hagenberg.master.montecarlo.prediction.ResultPrediction;
import at.hagenberg.master.montecarlo.entities.enums.GameResult;
import at.hagenberg.master.montecarlo.prediction.PredictionModel;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.stream.DoubleStream;

public class HeadToHeadMatch extends Match<Player> {

    public final static int NUMBER_OF_SAMPLES = 1;

    public HeadToHeadMatch(RandomGenerator randomGenerator, PredictionModel predictionModel, Player playerOne, Player playerTwo) {
        super(randomGenerator, predictionModel, playerOne, playerTwo);
    }

    public HeadToHeadMatch(RandomGenerator randomGenerator, PredictionModel predictionModel, Player playerOne, Player playerTwo, MatchResult result) {
        super(randomGenerator, predictionModel, playerOne, playerTwo, result);
    }

    @Override
    public MatchResult playMatch() {
        ResultPrediction p = predictionModel.calculatePrediction(opponentA, opponentB);

        int[] numsToGenerate = new int[] { 2, 1, 0};
        double[] discreteProbabilities = new double[] { p.getExpectedWinPlayerOne(), p.getExpectedDraw(), p.getExpectedWinPlayerTwo() };

        // ensure non negative zeros (https://stackoverflow.com/questions/6724031/how-can-a-primitive-float-value-be-0-0-what-does-that-mean)
        discreteProbabilities = DoubleStream.of(discreteProbabilities).map(d -> Math.abs(d)).toArray();

        EnumeratedIntegerDistribution distribution = new EnumeratedIntegerDistribution(randomGenerator, numsToGenerate, discreteProbabilities);
        int[] samples = distribution.sample(NUMBER_OF_SAMPLES);

        MatchResult result = new MatchResult(opponentA, opponentB, GameResult.valueOf(new Double(samples[0] / 2.0)));
        matchPrediction = result;
        return result;
    }

    public String print() {
        final StringBuffer sb = new StringBuffer();
        sb.append(opponentA.getName().replace(",", "")).append(",").append(opponentA.getElo()).append(",");
        sb.append(opponentB.getName().replace(",", "")).append(",").append(opponentB.getElo()).append(",");
        if(matchPrediction != null) sb.append(matchPrediction.getWinner() != null ? matchPrediction.getWinner().getName() : "DRAW");
        if(matchResult != null) sb.append(matchResult.getWinner() != null ? matchResult.getWinner().getName() : "DRAW");
        sb.append("\n");
        return sb.toString();
    }
}
