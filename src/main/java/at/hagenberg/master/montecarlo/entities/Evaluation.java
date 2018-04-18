package at.hagenberg.master.montecarlo.entities;

import at.hagenberg.master.montecarlo.simulation.HeadToHeadMatch;
import at.hagenberg.master.montecarlo.simulation.ChessPredictionModel;

import java.util.List;

public class Evaluation {

    public ChessPredictionModel predictionModel;
    public List<HeadToHeadMatch> games;
    public String division;
    public double pCorrect;
    public double pCorrectWhite;
    public double pCorrectDraw;
    public double pCorrectBlack;
    public double rootMeanSquareError;

    public Evaluation(ChessPredictionModel pm, List<HeadToHeadMatch> games, String division) {
        this.predictionModel = pm;
        this.games = games;
        this.division = division;
    }

    public void setRootMeanSquare(double... nums) {
        double sum = 0.0;
        for (double num : nums)
            sum += num * num;
        this.rootMeanSquareError = Math.sqrt(sum / nums.length);
    }
}
