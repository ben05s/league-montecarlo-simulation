package at.hagenberg.master.montecarlo.simulation;

import at.hagenberg.master.montecarlo.entities.Player;
import at.hagenberg.master.montecarlo.entities.ResultProbabilities;
import at.hagenberg.master.montecarlo.entities.enums.GameResult;
import at.hagenberg.master.montecarlo.exceptions.ChessMonteCarloSimulationException;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.stream.DoubleStream;

import static at.hagenberg.master.montecarlo.entities.enums.GameResult.*;

public class ChessGame {

    public final static int NUMBER_OF_SAMPLES = 1;

    private Player white;
    private Player black;
    private GameResult prediction;
    private GameResult result;

    public ChessGame(Player white, Player black) {
        this.white = white;
        this.black = black;
    }

    public ChessGame(Player white, Player black, GameResult result) {
        this(white, black);
        this.result = result;
    }

    public GameResult playGame(RandomGenerator randomGenerator, ChessPredictionModel predictionModel) throws ChessMonteCarloSimulationException {
        ChessGamePredictor predictor = new ChessGamePredictor(white, black, predictionModel);
        ResultProbabilities p = predictor.calculateGameResultProbabilities();

        int[] numsToGenerate = new int[] { 2, 1, 0};
        double[] discreteProbabilities = new double[] { p.getExpectedWinWhite(), p.getExpectedDraw(), p.getExpectedWinBlack() };

        // ensure non negative zeros (https://stackoverflow.com/questions/6724031/how-can-a-primitive-float-value-be-0-0-what-does-that-mean)
        discreteProbabilities = DoubleStream.of(discreteProbabilities).map(d -> Math.abs(d)).toArray();

        EnumeratedIntegerDistribution distribution = new EnumeratedIntegerDistribution(randomGenerator, numsToGenerate, discreteProbabilities);
        // TODO MINOR: maybe sample more than once
        int[] samples = distribution.sample(NUMBER_OF_SAMPLES);
        return evaluateGameResult(samples);
    }

    private GameResult evaluateGameResult(int[] samples) {
        GameResult gameResult = null;
        if(samples[0] == 2) {
            gameResult = WHITE;
        }
        if(samples[0] == 1) {
            gameResult = DRAW;
        }
        if(samples[0] == 0) {
            gameResult = BLACK;
        }
        this.prediction = gameResult;
        return gameResult;
    }

    public boolean isCorrectPrediction() {
        return this.prediction.getValue() == this.result.getValue();
    }

    public String print() {
        final StringBuffer sb = new StringBuffer();
        sb.append(white.getName().replace(",", "")).append(",").append(white.getElo()).append(",");
        sb.append(black.getName().replace(",", "")).append(",").append(black.getElo()).append(",");
        if(prediction != null) sb.append(prediction.name());
        if(result != null) sb.append(result.name());
        sb.append("\n");
        return sb.toString();
    }

    public Player getWhite() {
        return white;
    }

    public void setWhite(Player white) {
        this.white = white;
    }

    public Player getBlack() {
        return black;
    }

    public void setBlack(Player black) {
        this.black = black;
    }

    public GameResult getResult() {
        return result;
    }

    public void setResult(GameResult result) {
        this.result = result;
    }

    public GameResult getPrediction() {
        return prediction;
    }

    public void setPrediction(GameResult prediction) {
        this.prediction = prediction;
    }
}
