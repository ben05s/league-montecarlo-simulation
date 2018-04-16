package at.hagenberg.master.montecarlo.simulation;

import at.hagenberg.master.montecarlo.PgnAnalysis;
import at.hagenberg.master.montecarlo.entities.Evaluation;
import at.hagenberg.master.montecarlo.entities.enums.GameResult;
import at.hagenberg.master.montecarlo.exceptions.ChessMonteCarloSimulationException;
import at.hagenberg.master.montecarlo.simulation.settings.SeasonSettings;
import at.hagenberg.master.montecarlo.util.PgnUtil;
import at.hagenberg.master.montecarlo.util.ResultsFileUtil;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ChessGamePredictorTest {

    @Test
    public void testChessPredictionModel() throws ChessMonteCarloSimulationException {
        testChessPredictionModel("west");
        testChessPredictionModel("ost");
        testChessPredictionModel("mitte");
    }

    public void testChessPredictionModel(String division) throws ChessMonteCarloSimulationException {
        String seasonToSimulate = "games/" + division + "/1516autcht" + division + ".pgn";
        List<String> historicalSeasons = Arrays.asList(("games/" + division + "/0607autcht" + division + ".pgn," +
                "games/" + division + "/0708autcht" + division + ".pgn," +
                "games/" + division + "/0809autcht" + division + ".pgn," +
                "games/" + division + "/0910autcht" + division + ".pgn," +
                "games/" + division + "/1011autcht" + division + ".pgn," +
                "games/" + division + "/1112autcht" + division + ".pgn," +
                "games/" + division + "/1213autcht" + division + ".pgn," +
                "games/" + division + "/1314autcht" + division + ".pgn," +
                "games/" + division + "/1415autcht" + division + ".pgn")
                .split(","));

        SeasonSettings seasonSettings = new SeasonSettings(11,6);
        PgnAnalysis analysis = new PgnAnalysis(seasonSettings, seasonToSimulate, historicalSeasons);
        List<ChessGame> gameResults = analysis.getGames();

        ChessPredictionModel predictionModel = new ChessPredictionModel(false, false, false, false);
        predictionModel.regularizePlayerRatingsForGames(gameResults);
        predictionModel.advWhiteProbability = analysis.calculateWhiteAdvantage();
        predictionModel.avgElo = analysis.calculateAverageElo();
        predictionModel.pWhiteWin = analysis.calculateOverallProbability(PgnUtil.WHITE_WINS);
        predictionModel.pDraw = analysis.calculateOverallProbability(PgnUtil.DRAW);
        predictionModel.pBlackWin = analysis.calculateOverallProbability(PgnUtil.BLACK_WINS);

        final int N = 10;
        RandomGenerator randomGenerator = new Well19937c();

        List<Evaluation> evaluations = new ArrayList<>();

        Evaluator.permutatePredictionParameters(predictionModel).forEach(pm -> {
            //System.out.println(pm.toString());

            Evaluator evaluator = new Evaluator(division, randomGenerator, pm, gameResults);
            try {
                Evaluation avgE = evaluator.evaluateAvg(N);
                if(avgE != null) evaluations.add(avgE);
            } catch (ChessMonteCarloSimulationException e) {
                e.printStackTrace();
            }
        });

        ResultsFileUtil.writeEvalutations("evaluations-prediction-model-" + division + "-avg-" + N + "-runs", evaluations);
    }
}
