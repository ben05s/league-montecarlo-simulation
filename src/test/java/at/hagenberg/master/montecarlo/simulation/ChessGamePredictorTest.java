package at.hagenberg.master.montecarlo.simulation;

import at.hagenberg.master.montecarlo.PgnAnalysis;
import at.hagenberg.master.montecarlo.entities.Evaluation;
import at.hagenberg.master.montecarlo.entities.enums.LineupStrategy;
import at.hagenberg.master.montecarlo.exceptions.PgnParserException;
import at.hagenberg.master.montecarlo.simulation.settings.LeagueSettings;
import at.hagenberg.master.montecarlo.util.EloRatingSystemUtil;
import at.hagenberg.master.montecarlo.util.ResultsFileUtil;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChessGamePredictorTest {

    @Test
    public void testChessPredictionModel() throws PgnParserException {
        testChessPredictionModel("west");
        testChessPredictionModel("ost");
        testChessPredictionModel("mitte");
    }

    public void testChessPredictionModel(String division) throws PgnParserException {
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

        RandomGenerator randomGenerator = new Well19937c();
        final int gamesPerMatch = 6;
        final int roundsPerSeason = 11;
        final int roundsToSimulate = 2;
        ChessPredictionModel predictionModel = new ChessPredictionModel(false, false, false, true);

        PgnAnalysis analysis = new PgnAnalysis(seasonToSimulate, historicalSeasons, roundsPerSeason, gamesPerMatch);
        predictionModel.setStatistics(analysis);
        analysis.fillGamesFromSeasonToSimulate(randomGenerator, predictionModel);
        List<HeadToHeadMatch> gameResults = analysis.getGames();

        EloRatingSystemUtil.regularizePlayerRatingsForGames(gameResults, predictionModel.getAvgElo(), predictionModel.regularizeThreshold, predictionModel.regularizeFraction);

        final int N = 10;

        List<Evaluation> evaluations = new ArrayList<>();

        Evaluator.permutatePredictionParameters(predictionModel).forEach(pm -> {
            //System.out.println(pm.toString());
            Evaluator evaluator = new Evaluator(division, randomGenerator, pm, gameResults);
            Evaluation avgE = null;
            try {
                avgE = evaluator.evaluateAvg(N);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(avgE != null) evaluations.add(avgE);
        });

        ResultsFileUtil.writeEvalutations("evaluations-prediction-model-" + division + "-avg-" + N + "-runs", evaluations);
    }
}
