package at.hagenberg.master.montecarlo.entities;

import at.hagenberg.master.montecarlo.prediction.ChessPredictionModel;
import at.hagenberg.master.montecarlo.simulation.HeadToHeadMatch;

import java.util.List;

public class Evaluation {

    public ChessPredictionModel pm;
    public List<HeadToHeadMatch> games;
    public double pAdvWhite;
    public double avgElo;
    public double pCorrect;
    public double pCorrectWhite;
    public double pCorrectDraw;
    public double pCorrectBlack;
    public double rootMeanSquareError;

    public Evaluation(ChessPredictionModel pm, List<HeadToHeadMatch> games) {
        this.pm = pm;
        this.games = games;
    }

    public void setRootMeanSquare(double... nums) {
        double sum = 0.0;
        for (double num : nums)
            sum += num * num;
        this.rootMeanSquareError = Math.sqrt(sum / nums.length);
    }

    public String print() {
        return pm.useEloRating +
            ";" + pm.useHomeAdvantage +
            ";" + pm.useStrengthTrend +
            ";" + pm.usePlayerPerformances +
            ";" + pm.useRatingRegularization +
            ";" + pm.regularizeThreshold +
            ";" + pm.regularizeFraction +
            ";" + pm.winDrawFraction +
            ";" + pm.statsFactor +
            ";" + pm.strengthTrendFraction +
            ";" + String.format("%.4f",this.pAdvWhite).replace(",", ",") +
            ";" + String.format("%.4f",this.avgElo).replace(",", ",") +
            ";" + String.format("%.4f",this.pCorrect).replace(",", ",") +
            ";" + String.format("%.4f",this.rootMeanSquareError).replace(",", ",") +
            ";" + String.format("%.4f",this.pCorrectWhite).replace(",", ",") +
            ";" + String.format("%.4f",this.pCorrectDraw).replace(",", ",") +
            ";" + String.format("%.4f",this.pCorrectBlack).replace(",", ",") +
            ";" + games.size() +"\n";
    }
}
