package at.hagenberg.master.montecarlo.evaluation;

import at.hagenberg.master.montecarlo.entities.Evaluation;

import at.hagenberg.master.montecarlo.entities.MatchResult;
import at.hagenberg.master.montecarlo.entities.Team;
import at.hagenberg.master.montecarlo.prediction.ChessPredictionModel;
import at.hagenberg.master.montecarlo.simulation.HeadToHeadMatch;
import at.hagenberg.master.montecarlo.simulation.TeamMatch;
import at.hagenberg.master.montecarlo.simulation.settings.LeagueSettings;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.ArrayList;
import java.util.List;

public class Evaluator {

    private String division;
    private RandomGenerator randomGenerator;
    private LeagueSettings settings;
    private List<HeadToHeadMatch> headToHeadMatches;

    public Evaluator(String division, RandomGenerator randomGenerator, LeagueSettings settings, List<HeadToHeadMatch> headToHeadMatches) {
        this.division = division;
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

        TeamMatch match = new TeamMatch(randomGenerator, settings.getPredictionModel(), settings.getLineupSelector(), new Team("A"), new Team("B"));
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

        Evaluation evaluator = new Evaluation(settings.getPredictionModel(), result.getHeadToHeadMatches(), division);
        evaluator.pCorrect = (double) correctPredictions / (correctPredictions + wrongPredictions);
        evaluator.pCorrectWhite = (double) correctWhite / white;
        evaluator.pCorrectDraw = (double) correctDraw / draws;
        evaluator.pCorrectBlack = (double) correctBlack / black;

        double[] errors = predictionError.stream().mapToDouble(d -> d).toArray();
        evaluator.setRootMeanSquare(errors);
        return evaluator;
    }

    private Evaluation avgEvaluation(List<Evaluation> evaluations) {
        double pCorrect = evaluations.stream().mapToDouble(evaluator -> evaluator.pCorrect).sum() / evaluations.size();
        double pCorrectWhite = evaluations.stream().mapToDouble(evaluator -> evaluator.pCorrectWhite).sum() / evaluations.size();
        double pCorrectDraw = evaluations.stream().mapToDouble(evaluator -> evaluator.pCorrectDraw).sum() / evaluations.size();
        double pCorrectBlack = evaluations.stream().mapToDouble(evaluator -> evaluator.pCorrectBlack).sum() / evaluations.size();
        double rootMeanSquareError = evaluations.stream().mapToDouble(evaluator -> evaluator.rootMeanSquareError).sum() / evaluations.size();

        //System.out.println("pCorrect: " + pCorrect + " RMSE: " + rootMeanSquareError + " pCorrectWhite: " + pCorrectWhite + " pCorrectDraw: " + pCorrectDraw + " pCorrectBlack: " + pCorrectBlack);

        if(evaluations.isEmpty()) return null;

        Evaluation e = new Evaluation(evaluations.get(0).predictionModel, evaluations.get(0).games, division);
        e.pCorrect = pCorrect;
        e.pCorrectWhite = pCorrectWhite;
        e.pCorrectDraw = pCorrectDraw;
        e.pCorrectBlack = pCorrectBlack;
        e.rootMeanSquareError = rootMeanSquareError;
        return e;
    }

    public static List<ChessPredictionModel> permutatePredictionParameters(ChessPredictionModel predictionModel) {
        List<ChessPredictionModel> predictionModels = new ArrayList<>();

        ChessPredictionModel pm1 = new ChessPredictionModel(false, false, false, false);
        // different draw fractions
        ChessPredictionModel pm1_1 = new ChessPredictionModel(false, false, false, false);
        pm1_1.winDrawFraction = 4;
        ChessPredictionModel pm1_2 = new ChessPredictionModel(false, false, false, false);
        pm1_2.winDrawFraction = 8;
        ChessPredictionModel pm1_3 = new ChessPredictionModel(false, false, false, false);
        pm1_3.winDrawFraction = 1000;

        ChessPredictionModel pm2 = new ChessPredictionModel(true, false, false, false);
        // different advantages for white
        ChessPredictionModel pm2_1 = new ChessPredictionModel(true, false, false, false);
        pm2_1.advWhiteProbability = 0.54;
        ChessPredictionModel pm2_2 = new ChessPredictionModel(true, false, false, false);
        pm2_2.advWhiteProbability = 0.55;

        ChessPredictionModel pm3 = new ChessPredictionModel(false, true, false, false);
        // different influence of strength trend of a player
        ChessPredictionModel pm3_1 = new ChessPredictionModel(false, true, false, false);
        pm3_1.strengthTrendFraction = 1000;

        ChessPredictionModel pm4 = new ChessPredictionModel(false, false, true, false);
        // different influence of player stats for colors
        ChessPredictionModel pm4_1 = new ChessPredictionModel(false, false, true, false);
        pm4_1.statsFactor = 2;
        ChessPredictionModel pm4_2 = new ChessPredictionModel(false, false, true, false);
        pm4_2.statsFactor = 3;

        ChessPredictionModel pm5 = new ChessPredictionModel(false, false, false, true);
        // different regularization influences
        ChessPredictionModel pm5_1 = new ChessPredictionModel(false, false, false, true);
        pm5_1.regularizeThreshold = 11;
        ChessPredictionModel pm5_2 = new ChessPredictionModel(false, false, false, true);
        pm5_2.regularizeThreshold = 33;
        ChessPredictionModel pm5_3 = new ChessPredictionModel(false, false, false, true);
        pm5_3.regularizeFraction = 1;
        ChessPredictionModel pm5_4 = new ChessPredictionModel(false, false, false, true);
        pm5_4.regularizeFraction = 3;

        ChessPredictionModel pm6 = new ChessPredictionModel(true, true, false, false);
        ChessPredictionModel pm7 = new ChessPredictionModel(true, false, true, false);
        ChessPredictionModel pm8 = new ChessPredictionModel(true, false, false, true);
        ChessPredictionModel pm9 = new ChessPredictionModel(false, true, true, false);
        ChessPredictionModel pm10 = new ChessPredictionModel(false, true, false, true);
        ChessPredictionModel pm11 = new ChessPredictionModel(false, false, true, true);
        ChessPredictionModel pm12 = new ChessPredictionModel(true, true, true, false);
        ChessPredictionModel pm13 = new ChessPredictionModel(true, false, true, true);
        ChessPredictionModel pm14 = new ChessPredictionModel(true, true, false, true);
        ChessPredictionModel pm15 = new ChessPredictionModel(false, true, true, true);
        ChessPredictionModel pm16 = new ChessPredictionModel(true, true, true, true);

        predictionModels.add(pm1);
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
        predictionModels.add(pm5);
        predictionModels.add(pm5_1);
        predictionModels.add(pm5_2);
        predictionModels.add(pm5_3);
        predictionModels.add(pm5_4);
        predictionModels.add(pm6);
        predictionModels.add(pm7);
        predictionModels.add(pm8);
        predictionModels.add(pm9);
        predictionModels.add(pm10);
        predictionModels.add(pm11);
        predictionModels.add(pm12);
        predictionModels.add(pm13);
        predictionModels.add(pm14);
        predictionModels.add(pm15);
        predictionModels.add(pm16);

        predictionModels.forEach(pm -> copy(predictionModel, pm));

        return predictionModels;
    }

    public static void copy(ChessPredictionModel o, ChessPredictionModel c) {
        if(c.advWhiteProbability == 0.0) c.advWhiteProbability = o.advWhiteProbability;
        c.avgElo = o.avgElo;
        c.pWhiteWin = o.pWhiteWin;
        c.pDraw = o.pDraw;
        c.pBlackWin = o.pBlackWin;
    }
}
