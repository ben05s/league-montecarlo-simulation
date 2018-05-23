package at.hagenberg.master.montecarlo.simulation;

import at.hagenberg.master.montecarlo.entities.Team;
import at.hagenberg.master.montecarlo.evaluation.Evaluator;
import at.hagenberg.master.montecarlo.lineup.AbstractLineupSelector;
import at.hagenberg.master.montecarlo.lineup.RandomSelection;
import at.hagenberg.master.montecarlo.parser.PgnAnalysis;
import at.hagenberg.master.montecarlo.entities.Evaluation;
import at.hagenberg.master.montecarlo.exceptions.PgnParserException;
import at.hagenberg.master.montecarlo.prediction.ChessPredictionModel;
import at.hagenberg.master.montecarlo.util.EloRatingSystemUtil;
import at.hagenberg.master.montecarlo.util.ResultsFileUtil;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ChessGamePredictorTest {

    @Test
    public void testSpecificPredictionModel() {
        ChessPredictionModel pm = new ChessPredictionModel(false, true, false, true, true);
        pm.statsFactor = 8;

        List<Evaluation> predictionModelEvaluations = new ArrayList<>();

        List<Evaluation> evaluations = new ArrayList<>();
        Evaluation eval1 = evaluate(pm,"west", "1011autchtwest.pgn");
        Evaluation eval2 = evaluate(pm,"west", "1112autchtwest.pgn");
        Evaluation eval3 = evaluate(pm,"west", "1314autchtwest.pgn");
        Evaluation eval4 = evaluate(pm,"west", "1415autchtwest.pgn");
        Evaluation eval5 = evaluate(pm,"west", "1516autchtwest.pgn");
        Evaluation eval6 = evaluate(pm,"ost", "1011autchtost.pgn");
        Evaluation eval7 = evaluate(pm,"ost", "1112autchtost.pgn");
        Evaluation eval8 = evaluate(pm,"ost", "1314autchtost.pgn");
        Evaluation eval9 = evaluate(pm,"ost", "1415autchtost.pgn");
        Evaluation eval10 = evaluate(pm,"ost", "1516autchtost.pgn");
        if(eval1 != null) evaluations.add(eval1);
        if(eval2 != null) evaluations.add(eval2);
        if(eval3 != null) evaluations.add(eval3);
        if(eval4 != null) evaluations.add(eval4);
        if(eval5 != null) evaluations.add(eval5);
        if(eval6 != null) evaluations.add(eval6);
        if(eval7 != null) evaluations.add(eval7);
        if(eval8 != null) evaluations.add(eval8);
        if(eval9 != null) evaluations.add(eval9);
        if(eval10 != null) evaluations.add(eval10);

        predictionModelEvaluations.add(Evaluator.avgEvaluation(evaluations));

        ResultsFileUtil.writeEvalutations("evaluations-prediction-model-specific", predictionModelEvaluations);
    }

    @Test
    public void testChessPredictionModel() {
        List<Evaluation> predictionModelEvaluations = new ArrayList<>();
        Evaluator.permutatePredictionParameters().forEach(pm -> {
            List<Evaluation> evaluations = new ArrayList<>();
            /* training data */
            Evaluation eval1 = evaluate(pm,"west", "1011autchtwest.pgn");
            Evaluation eval2 = evaluate(pm,"west", "1112autchtwest.pgn");
            Evaluation eval3 = evaluate(pm,"west", "1314autchtwest.pgn");
            Evaluation eval4 = evaluate(pm,"west", "1415autchtwest.pgn");
            Evaluation eval5 = evaluate(pm,"west", "1516autchtwest.pgn");
            Evaluation eval6 = evaluate(pm,"ost", "1011autchtost.pgn");
            Evaluation eval7 = evaluate(pm,"ost", "1112autchtost.pgn");
            Evaluation eval8 = evaluate(pm,"ost", "1314autchtost.pgn");
            Evaluation eval9 = evaluate(pm,"ost", "1415autchtost.pgn");
            Evaluation eval10 = evaluate(pm,"ost", "1516autchtost.pgn");
            if(eval1 != null) evaluations.add(eval1);
            if(eval2 != null) evaluations.add(eval2);
            if(eval3 != null) evaluations.add(eval3);
            if(eval4 != null) evaluations.add(eval4);
            if(eval5 != null) evaluations.add(eval5);
            if(eval6 != null) evaluations.add(eval6);
            if(eval7 != null) evaluations.add(eval7);
            if(eval8 != null) evaluations.add(eval8);
            if(eval9 != null) evaluations.add(eval9);
            if(eval10 != null) evaluations.add(eval10);

            /* validation data */
            Evaluation eval11 = evaluate(pm,"mitte", "1011autchtmitte.pgn");
            Evaluation eval12 = evaluate(pm,"mitte", "1112autchtmitte.pgn");
            Evaluation eval13 = evaluate(pm,"mitte", "1314autchtmitte.pgn");
            Evaluation eval14 = evaluate(pm,"mitte", "1415autchtmitte.pgn");
            Evaluation eval15 = evaluate(pm,"mitte", "1516autchtmitte.pgn");
            if(eval11 != null) evaluations.add(eval11);
            if(eval12 != null) evaluations.add(eval12);
            if(eval13 != null) evaluations.add(eval13);
            if(eval14 != null) evaluations.add(eval14);
            if(eval15 != null) evaluations.add(eval15);

            predictionModelEvaluations.add(Evaluator.avgEvaluation(evaluations));
        });

        ResultsFileUtil.writeEvalutations("evaluations-prediction-model-validation", predictionModelEvaluations);
    }

    private Evaluation evaluate(final ChessPredictionModel pm, String division, String file) {
        ChessPredictionModel cpm = new ChessPredictionModel(pm);
        final int N = 1;
        String seasonToSimulate = null;
        String historicalSeasons = null;
        try {
            seasonToSimulate = new String(Files.readAllBytes(Paths.get("games/" + division + "/" + file)));
            historicalSeasons = new String(Files.readAllBytes(Paths.get("games/" + division + "/historicData" + file)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        RandomGenerator randomGenerator = new Well19937c();
        final int gamesPerMatch = 6;
        final int roundsPerSeason = 11;
        final int roundsToSimulate = 11;

        PgnAnalysis analysis = null;
        try {
            analysis = new PgnAnalysis(seasonToSimulate, historicalSeasons, roundsPerSeason, gamesPerMatch);
            cpm.setStatistics(analysis);
            analysis.fillGamesFromSeasonToSimulate(randomGenerator, cpm);
        } catch (PgnParserException e) {
            e.printStackTrace();
        }
        List<HeadToHeadMatch> gameResults = analysis.getGames();
        List<Team> teamList = analysis.getTeams();
        if(cpm.useRatingRegularization) {
            EloRatingSystemUtil.regularizePlayerRatingsForGames(gameResults, cpm.getAvgElo(), cpm.regularizeThreshold, cpm.regularizeFraction);
            teamList = EloRatingSystemUtil.regularizePlayerRatingsForTeams(teamList, cpm.getAvgElo(), cpm.regularizeThreshold, cpm.regularizeFraction);
        }

        AbstractLineupSelector lineupSelector = new RandomSelection(randomGenerator, gamesPerMatch, true);
        LeagueSettings<Team> settings = new LeagueSettings(cpm, teamList, roundsPerSeason, lineupSelector, null, roundsToSimulate, analysis.getRoundGameResults());

        //System.out.println(pm.toString());
        Evaluator evaluator = new Evaluator(randomGenerator, settings, gameResults);
        Evaluation evaluation = null;
        try {
            evaluation = evaluator.evaluateAvg(N);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(file + " " + evaluation.print());
        return evaluation;
    }
}
