package at.hagenberg.master.montecarlo.evaluation;

import at.hagenberg.master.montecarlo.entities.Evaluation;

import at.hagenberg.master.montecarlo.entities.MatchResult;
import at.hagenberg.master.montecarlo.entities.Team;
import at.hagenberg.master.montecarlo.prediction.ChessPredictionModel;
import at.hagenberg.master.montecarlo.simulation.HeadToHeadMatch;
import at.hagenberg.master.montecarlo.simulation.TeamMatch;
import at.hagenberg.master.montecarlo.simulation.LeagueSettings;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.ArrayList;
import java.util.List;

public class Evaluator {

    private RandomGenerator randomGenerator;
    private LeagueSettings settings;
    private List<HeadToHeadMatch> headToHeadMatches;

    public Evaluator(RandomGenerator randomGenerator, LeagueSettings settings, List<HeadToHeadMatch> headToHeadMatches) {
        this.randomGenerator = randomGenerator;
        this.settings = settings;
        this.headToHeadMatches = headToHeadMatches;
    }

    public Evaluation evaluateAvg(final int N) throws Exception {
        List<Evaluation> evaluations = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            Evaluation evaluator = evaluatePredictor();
            evaluations.add(evaluator);
        }
        return avgEvaluation(evaluations);
    }

    private Evaluation evaluatePredictor() throws Exception {
        int wrongPredictions = 0;
        int correctPredictions = 0;
        int draws = 0;
        int white = 0;
        int black = 0;
        int correctWhite = 0;
        int correctBlack = 0;
        int correctDraw = 0;
        List<Double> predictionError = new ArrayList<>();

        TeamMatch match = new TeamMatch(randomGenerator, settings.getPredictionModel(), new Team("A"), new Team("B"), settings.getLineupSelector().getGamesPerMatch());
        match.setHeadToHeadMatches(headToHeadMatches);
        MatchResult result = match.playMatch();

        for (int i = 0; i < result.getHeadToHeadMatches().size(); i++) {
            HeadToHeadMatch gameResult = result.getHeadToHeadMatches().get(i);
            if(gameResult.getOpponentA().equals(gameResult.getMatchResult().getWinner())) {
                white++;
            } else if(gameResult.getOpponentB().equals(gameResult.getMatchResult().getWinner())) {
                black++;
            } else {
                draws++;
            }

            if(gameResult.isCorrectPrediction()) {
                if(gameResult.getOpponentA().equals(gameResult.getMatchPrediction().getWinner())) {
                    correctWhite++;
                } else if(gameResult.getOpponentB().equals(gameResult.getMatchPrediction().getWinner())) {
                    correctBlack++;
                } else {
                    correctDraw++;
                }
                correctPredictions++;
            } else {
                wrongPredictions++;
            }
            //predictedResult = p.getExpectedWinPlayerOne(); // without discrete result TODO MINOR which is better to measure the error?
            predictionError.add(new Double(gameResult.getError()));
            //System.out.println(gameResult.getPlayerOne().getElo() + " vs " + gameResult.getPlayerTwo().getElo() + ": prediction " + predictedResult + " result " + actualResult);
        }

        Evaluation evaluation = new Evaluation((ChessPredictionModel) settings.getPredictionModel(), result.getHeadToHeadMatches());
        evaluation.pCorrect = (double) correctPredictions / (correctPredictions + wrongPredictions);
        evaluation.pCorrectWhite = (double) correctWhite / white;
        evaluation.pCorrectDraw = (double) correctDraw / draws;
        evaluation.pCorrectBlack = (double) correctBlack / black;

        double sum = predictionError.stream().mapToDouble(d -> d).sum();
        evaluation.setRootMeanSquareError(Math.sqrt(sum / predictionError.size()));
        return evaluation;
    }

    public static Evaluation avgEvaluation(List<Evaluation> evaluations) {
        if(evaluations.isEmpty()) return null;

        List<HeadToHeadMatch> games = new ArrayList<>();
        for (int i = 0; i < evaluations.size(); i++) {
            games.addAll(evaluations.get(i).games);
        }

        double pAdvWhite = evaluations.stream().mapToDouble(evaluator -> evaluator.pm.advWhiteProbability).sum() / evaluations.size();
        double avgElo = evaluations.stream().mapToDouble(evaluator -> evaluator.pm.avgElo).sum() / evaluations.size();

        double pCorrect = evaluations.stream().mapToDouble(evaluator -> evaluator.pCorrect).sum() / evaluations.size();
        double pCorrectWhite = evaluations.stream().mapToDouble(evaluator -> evaluator.pCorrectWhite).sum() / evaluations.size();
        double pCorrectDraw = evaluations.stream().mapToDouble(evaluator -> evaluator.pCorrectDraw).sum() / evaluations.size();
        double pCorrectBlack = evaluations.stream().mapToDouble(evaluator -> evaluator.pCorrectBlack).sum() / evaluations.size();
        double rootMeanSquareError = evaluations.stream().mapToDouble(evaluator -> evaluator.rootMeanSquareError).sum() / evaluations.size();

        //System.out.println("pCorrect: " + pCorrect + " RMSE: " + rootMeanSquareError + " pCorrectWhite: " + pCorrectWhite + " pCorrectDraw: " + pCorrectDraw + " pCorrectBlack: " + pCorrectBlack);

        Evaluation e = new Evaluation(evaluations.get(0).pm, games);
        e.pAdvWhite = pAdvWhite;
        e.avgElo = avgElo;
        e.pCorrect = pCorrect;
        e.pCorrectWhite = pCorrectWhite;
        e.pCorrectDraw = pCorrectDraw;
        e.pCorrectBlack = pCorrectBlack;
        e.rootMeanSquareError = rootMeanSquareError;
        return e;
    }

    public static List<ChessPredictionModel> permutatePredictionParameters() {
        List<ChessPredictionModel> predictionModels = new ArrayList<>();

        ChessPredictionModel pm01 = new ChessPredictionModel(false, false, false, false, false);
        ChessPredictionModel pm02 = new ChessPredictionModel(false, true, false, false, false);
        ChessPredictionModel pm03 = new ChessPredictionModel(false, false, true, false, false);
        ChessPredictionModel pm04 = new ChessPredictionModel(false, false, false, true, false);
        ChessPredictionModel pm04_1 = new ChessPredictionModel(false, false, false, true, false);
        pm04_1.statsFactor = 2;
        ChessPredictionModel pm04_2 = new ChessPredictionModel(false, false, false, true, false);
        pm04_2.statsFactor = 3;
        ChessPredictionModel pm04_3 = new ChessPredictionModel(false, false, false, true, false);
        pm04_3.statsFactor = 4;
        ChessPredictionModel pm04_4 = new ChessPredictionModel(false, false, false, true, false);
        pm04_4.statsFactor = 5;
        ChessPredictionModel pm04_5 = new ChessPredictionModel(false, false, false, true, false);
        pm04_5.statsFactor = 8;
        ChessPredictionModel pm04_6 = new ChessPredictionModel(false, false, false, true, false);
        pm04_6.statsFactor = 10;

        ChessPredictionModel pm05 = new ChessPredictionModel(false, false, false, false, true);
        ChessPredictionModel pm06 = new ChessPredictionModel(false, true, true, false, false);
        ChessPredictionModel pm07 = new ChessPredictionModel(false, true, false, true, false);
        ChessPredictionModel pm07_1 = new ChessPredictionModel(false, true, false, true, false);
        pm07_1.statsFactor = 8;

        ChessPredictionModel pm08 = new ChessPredictionModel(false, true, false, false, true);
        ChessPredictionModel pm09 = new ChessPredictionModel(false, false, true, true, false);
        ChessPredictionModel pm010 = new ChessPredictionModel(false, false, true, false, true);
        ChessPredictionModel pm011 = new ChessPredictionModel(false, false, false, true, true);
        ChessPredictionModel pm011_1 = new ChessPredictionModel(false, false, false, true, true);
        pm011_1.statsFactor = 8;

        ChessPredictionModel pm012 = new ChessPredictionModel(false, true, true, true, false);
        ChessPredictionModel pm013 = new ChessPredictionModel(false, true, true, false, true);
        ChessPredictionModel pm014 = new ChessPredictionModel(false, true, false, true, true);
        ChessPredictionModel pm014_1 = new ChessPredictionModel(false, true, false, true, true);
        pm014_1.statsFactor = 8;

        ChessPredictionModel pm015 = new ChessPredictionModel(false, false, true, true, true);
        ChessPredictionModel pm016 = new ChessPredictionModel(false, true, true, true, true);

        ChessPredictionModel pm1 = new ChessPredictionModel(true, false, false, false, false);
        // different draw fractions
        ChessPredictionModel pm1_1 = new ChessPredictionModel(true, false, false, false, false);
        pm1_1.winDrawFraction = 4;
        ChessPredictionModel pm1_2 = new ChessPredictionModel(true, false, false, false, false);
        pm1_2.winDrawFraction = 8;
        ChessPredictionModel pm1_3 = new ChessPredictionModel(true, false, false, false, false);
        pm1_3.winDrawFraction = 1000;

        ChessPredictionModel pm1_0_0 = new ChessPredictionModel(true, false, false, false, false);
        pm1_0_0.drawInfluence = 580;
        ChessPredictionModel pm1_0_1 = new ChessPredictionModel(true, false, false, false, false);
        pm1_0_1.drawInfluence = 600;
        ChessPredictionModel pm1_0_2 = new ChessPredictionModel(true, false, false, false, false);
        pm1_0_2.drawInfluence = 620;
        ChessPredictionModel pm1_0_3 = new ChessPredictionModel(true, false, false, false, false);
        pm1_0_3.drawInfluence = 660;
        ChessPredictionModel pm1_0_4 = new ChessPredictionModel(true, false, false, false, false);
        pm1_0_4.drawInfluence = 680;

        ChessPredictionModel pm2 = new ChessPredictionModel(true, true, false, false, false);
        // different advantages for white
        ChessPredictionModel pm2_1 = new ChessPredictionModel(true, true, false, false, false);
        pm2_1.advWhiteProbability = 0.54;
        ChessPredictionModel pm2_2 = new ChessPredictionModel(true, true, false, false, false);
        pm2_2.advWhiteProbability = 0.55;

        ChessPredictionModel pm3 = new ChessPredictionModel(true, false, true, false, false);
        // different influence of strength trend of a player
        ChessPredictionModel pm3_1 = new ChessPredictionModel(true, false, true, false, false);
        pm3_1.strengthTrendFraction = 1000;

        ChessPredictionModel pm4 = new ChessPredictionModel(true, false, false, true, false);
        // different influence of player stats for colors
        ChessPredictionModel pm4_1 = new ChessPredictionModel(true, false, false, true, false);
        pm4_1.statsFactor = 2;
        ChessPredictionModel pm4_2 = new ChessPredictionModel(true, false, false, true, false);
        pm4_2.statsFactor = 3;

        ChessPredictionModel pm4_2_0 = new ChessPredictionModel(true, false, false, true, false);
        pm4_2_0.statsFactor = 3;
        pm4_2_0.drawInfluence = 580;
        ChessPredictionModel pm4_2_1 = new ChessPredictionModel(true, false, false, true, false);
        pm4_2_1.statsFactor = 3;
        pm4_2_1.drawInfluence = 600;
        ChessPredictionModel pm4_2_2 = new ChessPredictionModel(true, false, false, true, false);
        pm4_2_2.statsFactor = 3;
        pm4_2_2.drawInfluence = 620;
        ChessPredictionModel pm4_2_3 = new ChessPredictionModel(true, false, false, true, false);
        pm4_2_3.statsFactor = 3;
        pm4_2_3.drawInfluence = 660;
        ChessPredictionModel pm4_2_4 = new ChessPredictionModel(true, false, false, true, false);
        pm4_2_4.statsFactor = 3;
        pm4_2_4.drawInfluence = 680;

        ChessPredictionModel pm4_3 = new ChessPredictionModel(true, false, false, true, false);
        pm4_3.statsFactor = 4;
        ChessPredictionModel pm4_4 = new ChessPredictionModel(true, false, false, true, false);
        pm4_4.statsFactor = 5;

        ChessPredictionModel pm5 = new ChessPredictionModel(true, false, false, false, true);
        // different regularization influences
        ChessPredictionModel pm5_1 = new ChessPredictionModel(true, false, false, false, true);
        pm5_1.regularizeThreshold = 11;
        ChessPredictionModel pm5_2 = new ChessPredictionModel(true, false, false, false, true);
        pm5_2.regularizeThreshold = 33;
        ChessPredictionModel pm5_3 = new ChessPredictionModel(true, false, false, false, true);
        pm5_3.regularizeFraction = 1;
        ChessPredictionModel pm5_4 = new ChessPredictionModel(true, false, false, false, true);
        pm5_4.regularizeFraction = 3;

        ChessPredictionModel pm6 = new ChessPredictionModel(true, true, true, false, false);
        ChessPredictionModel pm7 = new ChessPredictionModel(true, true, false, true, false);
        ChessPredictionModel pm7_1 = new ChessPredictionModel(true, true, false, true, false);
        pm7_1.statsFactor = 2;
        ChessPredictionModel pm7_1_0 = new ChessPredictionModel(true, true, false, true, false);
        pm7_1_0.statsFactor = 2;
        pm7_1_0.drawInfluence = 580;
        ChessPredictionModel pm7_1_1 = new ChessPredictionModel(true, true, false, true, false);
        pm7_1_1.statsFactor = 2;
        pm7_1_1.drawInfluence = 600;
        ChessPredictionModel pm7_1_2 = new ChessPredictionModel(true, true, false, true, false);
        pm7_1_2.statsFactor = 2;
        pm7_1_2.drawInfluence = 620;
        ChessPredictionModel pm7_1_3 = new ChessPredictionModel(true, true, false, true, false);
        pm7_1_3.statsFactor = 2;
        pm7_1_3.drawInfluence = 660;
        ChessPredictionModel pm7_1_4 = new ChessPredictionModel(true, true, false, true, false);
        pm7_1_4.statsFactor = 2;
        pm7_1_4.drawInfluence = 680;

        ChessPredictionModel pm7_2 = new ChessPredictionModel(true, true, false, true, false);
        pm7_2.statsFactor = 3;
        ChessPredictionModel pm7_3 = new ChessPredictionModel(true, true, false, true, false);
        pm7_3.statsFactor = 4;
        ChessPredictionModel pm7_4 = new ChessPredictionModel(true, true, false, true, false);
        pm7_4.statsFactor = 5;

        ChessPredictionModel pm8 = new ChessPredictionModel(true, true, false, false, true);
        ChessPredictionModel pm9 = new ChessPredictionModel(true, false, true, true, false);
        ChessPredictionModel pm9_1 = new ChessPredictionModel(true, false, true, true, false);
        pm9_1.statsFactor = 2;
        ChessPredictionModel pm9_2 = new ChessPredictionModel(true, false, true, true, false);
        pm9_2.statsFactor = 3;
        ChessPredictionModel pm9_3 = new ChessPredictionModel(true, false, true, true, false);
        pm9_3.statsFactor = 4;
        ChessPredictionModel pm9_4 = new ChessPredictionModel(true, false, true, true, false);
        pm9_4.statsFactor = 5;

        ChessPredictionModel pm10 = new ChessPredictionModel(true, false, true, false, true);
        ChessPredictionModel pm11 = new ChessPredictionModel(true, false, false, true, true);
        ChessPredictionModel pm11_1 = new ChessPredictionModel(true, false, false, true, true);
        pm11_1.statsFactor = 2;
        ChessPredictionModel pm11_2 = new ChessPredictionModel(true, false, false, true, true);
        pm11_2.statsFactor = 3;
        ChessPredictionModel pm11_3 = new ChessPredictionModel(true, false, false, true, true);
        pm11_3.statsFactor = 4;
        ChessPredictionModel pm11_4 = new ChessPredictionModel(true, false, false, true, true);
        pm11_4.statsFactor = 5;

        ChessPredictionModel pm12 = new ChessPredictionModel(true, true, true, true, false);
        ChessPredictionModel pm12_1 = new ChessPredictionModel(true, true, true, true, false);
        pm12_1.statsFactor = 2;
        ChessPredictionModel pm12_2 = new ChessPredictionModel(true, true, true, true, false);
        pm12_2.statsFactor = 3;
        ChessPredictionModel pm12_3 = new ChessPredictionModel(true, true, true, true, false);
        pm12_3.statsFactor = 4;
        ChessPredictionModel pm12_4 = new ChessPredictionModel(true, true, true, true, false);
        pm12_4.statsFactor = 5;

        ChessPredictionModel pm13 = new ChessPredictionModel(true, true, false, true, true);
        ChessPredictionModel pm13_1 = new ChessPredictionModel(true, true, false, true, true);
        pm13_1.statsFactor = 2;
        ChessPredictionModel pm13_2 = new ChessPredictionModel(true, true, false, true, true);
        pm13_2.statsFactor = 3;
        ChessPredictionModel pm13_3 = new ChessPredictionModel(true, true, false, true, true);
        pm13_3.statsFactor = 4;
        ChessPredictionModel pm13_4 = new ChessPredictionModel(true, true, false, true, true);
        pm13_4.statsFactor = 5;

        ChessPredictionModel pm14 = new ChessPredictionModel(true, true, true, false, true);
        ChessPredictionModel pm15 = new ChessPredictionModel(true, false, true, true, true);
        ChessPredictionModel pm15_1 = new ChessPredictionModel(true, false, true, true, true);
        pm15_1.statsFactor = 2;
        ChessPredictionModel pm15_2 = new ChessPredictionModel(true, false, true, true, true);
        pm15_2.statsFactor = 3;
        ChessPredictionModel pm15_3 = new ChessPredictionModel(true, false, true, true, true);
        pm15_3.statsFactor = 4;
        ChessPredictionModel pm15_4 = new ChessPredictionModel(true, false, true, true, true);
        pm15_4.statsFactor = 5;

        ChessPredictionModel pm16 = new ChessPredictionModel(true, true, true, true, true);
        ChessPredictionModel pm16_1 = new ChessPredictionModel(true, true, true, true, true);
        pm16_1.statsFactor = 2;
        ChessPredictionModel pm16_2 = new ChessPredictionModel(true, true, true, true, true);
        pm16_2.statsFactor = 3;
        ChessPredictionModel pm16_3 = new ChessPredictionModel(true, true, true, true, true);
        pm16_3.statsFactor = 4;
        ChessPredictionModel pm16_4 = new ChessPredictionModel(true, true, true, true, true);
        pm16_4.statsFactor = 5;

        predictionModels.add(pm01);
        predictionModels.add(pm02);
        predictionModels.add(pm03);
        predictionModels.add(pm04);
        predictionModels.add(pm04_1);
        predictionModels.add(pm04_2);
        predictionModels.add(pm04_3);
        predictionModels.add(pm04_4);
        predictionModels.add(pm04_5);
        predictionModels.add(pm04_6);
        predictionModels.add(pm05);
        predictionModels.add(pm06);
        predictionModels.add(pm07);
        predictionModels.add(pm07_1);
        predictionModels.add(pm08);
        predictionModels.add(pm09);
        predictionModels.add(pm010);
        predictionModels.add(pm011);
        predictionModels.add(pm011_1);
        predictionModels.add(pm012);
        predictionModels.add(pm013);
        predictionModels.add(pm014);
        predictionModels.add(pm014_1);
        predictionModels.add(pm015);
        predictionModels.add(pm016);
        predictionModels.add(pm1);
        predictionModels.add(pm1_0_0);
        predictionModels.add(pm1_0_1);
        predictionModels.add(pm1_0_2);
        predictionModels.add(pm1_0_3);
        predictionModels.add(pm1_0_4);
        predictionModels.add(pm1_1);
        predictionModels.add(pm1_2);
        predictionModels.add(pm1_3);
        predictionModels.add(pm2);
        predictionModels.add(pm2_1);
        predictionModels.add(pm2_2);
        predictionModels.add(pm3);
        predictionModels.add(pm3_1);
        predictionModels.add(pm4);
        predictionModels.add(pm4_1);
        predictionModels.add(pm4_2);
        predictionModels.add(pm4_2_0);
        predictionModels.add(pm4_2_1);
        predictionModels.add(pm4_2_2);
        predictionModels.add(pm4_2_3);
        predictionModels.add(pm4_2_4);
        predictionModels.add(pm4_3);
        predictionModels.add(pm4_4);
        predictionModels.add(pm5);
        predictionModels.add(pm5_1);
        predictionModels.add(pm5_2);
        predictionModels.add(pm5_3);
        predictionModels.add(pm5_4);
        predictionModels.add(pm6);
        predictionModels.add(pm7);
        predictionModels.add(pm7_1);
        predictionModels.add(pm7_1_0);
        predictionModels.add(pm7_1_1);
        predictionModels.add(pm7_1_2);
        predictionModels.add(pm7_1_3);
        predictionModels.add(pm7_1_4);
        predictionModels.add(pm7_2);
        predictionModels.add(pm7_3);
        predictionModels.add(pm7_4);
        predictionModels.add(pm8);
        predictionModels.add(pm9);
        predictionModels.add(pm10);
        predictionModels.add(pm11);
        predictionModels.add(pm11_1);
        predictionModels.add(pm11_2);
        predictionModels.add(pm11_3);
        predictionModels.add(pm11_4);
        predictionModels.add(pm12);
        predictionModels.add(pm12_1);
        predictionModels.add(pm12_2);
        predictionModels.add(pm12_3);
        predictionModels.add(pm12_4);
        predictionModels.add(pm13);
        predictionModels.add(pm13_1);
        predictionModels.add(pm13_2);
        predictionModels.add(pm13_3);
        predictionModels.add(pm13_4);
        predictionModels.add(pm14);
        predictionModels.add(pm15);
        predictionModels.add(pm15_1);
        predictionModels.add(pm15_2);
        predictionModels.add(pm15_3);
        predictionModels.add(pm15_4);
        predictionModels.add(pm16);
        predictionModels.add(pm16_1);
        predictionModels.add(pm16_2);
        predictionModels.add(pm16_3);
        predictionModels.add(pm16_4);

        return predictionModels;
    }
}
