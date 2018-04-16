package at.hagenberg.master.montecarlo.simulation.settings;

import at.hagenberg.master.montecarlo.simulation.ChessPredictionModel;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;

import java.util.Objects;

public class MonteCarloSettings {

    private SeasonSettings seasonSettings;
    private RandomGenerator randomGenerator;
    private ChessPredictionModel predictionModel;
    private ChessLineupSettings lineupSettings;

    public MonteCarloSettings(SeasonSettings seasonSettings, ChessPredictionModel predictionModel, ChessLineupSettings lineupSettings) {
        this(seasonSettings, predictionModel, lineupSettings, new Well19937c());
    }

    public MonteCarloSettings(SeasonSettings seasonSettings, ChessPredictionModel predictionModel, ChessLineupSettings lineupSettings, RandomGenerator randomGenerator) {
        Objects.requireNonNull(seasonSettings);
        Objects.requireNonNull(randomGenerator);
        Objects.requireNonNull(predictionModel);
        Objects.requireNonNull(lineupSettings);

        this.seasonSettings = seasonSettings;
        this.randomGenerator = randomGenerator;
        this.predictionModel = predictionModel;
        this.lineupSettings = lineupSettings;
    }

    @Override
    public String toString() {
        return "MonteCarloSettings{" +
                "\n\tseasonSettings=" + seasonSettings +
                "\n\trandomGenerator=" + randomGenerator +
                ",\n\tpredictionModel=" + predictionModel +
                ",\n\tlineupSettings=" + lineupSettings +
                "}";
    }

    public SeasonSettings getSeasonSettings() { return seasonSettings; }

    public RandomGenerator getRandomGenerator() { return randomGenerator; }

    public ChessPredictionModel getPredictionModel() { return predictionModel; }

    public ChessLineupSettings getLineupSettings() { return lineupSettings; }
}
