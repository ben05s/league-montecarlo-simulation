package at.hagenberg.master.montecarlo;

import at.hagenberg.master.montecarlo.entities.*;
import at.hagenberg.master.montecarlo.entities.enums.LineupStrategy;
import at.hagenberg.master.montecarlo.exceptions.PgnParserException;
import at.hagenberg.master.montecarlo.lineup.LineupSelector;
import at.hagenberg.master.montecarlo.parser.PgnAnalysis;
import at.hagenberg.master.montecarlo.simulation.*;
import at.hagenberg.master.montecarlo.prediction.ChessPredictionModel;
import at.hagenberg.master.montecarlo.simulation.settings.LeagueSettings;
import at.hagenberg.master.montecarlo.util.EloRatingSystemUtil;
import at.hagenberg.master.montecarlo.util.ResultsFileUtil;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;

import java.util.*;

public class Main {

    public static void main(String[] args) {
        String division = "west";
        // Note: All historical seasons must be complete (all games for all 11 rounds must be present)
        // as the statistical analysis of player performance and probable lineups assumes 11 rounds have been played
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
        SeasonResult result = new SeasonResult();
        try {
            // TODO MAJOR verify one season without simulation if results are processed correctly
            RandomGenerator randomGenerator = new Well19937c();
            final int gamesPerMatch = 6;
            final int roundsPerSeason = 11;
            final int roundsToSimulate = 11;
            LineupSelector lineupSelector = new LineupSelector(LineupStrategy.DESCENDING_RATING_STRENGTH, gamesPerMatch);
            ChessPredictionModel predictionModel = new ChessPredictionModel(false, false, false, true);

            PgnAnalysis analysis = new PgnAnalysis(seasonToSimulate, historicalSeasons, roundsPerSeason, gamesPerMatch);
            List<Team> teamList = analysis.getTeams();
            predictionModel.setStatistics(analysis);
            analysis.fillGamesFromSeasonToSimulate(randomGenerator, predictionModel);

            if(predictionModel.useRatingRegularization)
                teamList = EloRatingSystemUtil.regularizePlayerRatingsForTeams(teamList, predictionModel.getAvgElo(), predictionModel.regularizeThreshold, predictionModel.regularizeFraction);

            ResultsFileUtil.writePlayerStats("player-stats", teamList);

            LeagueSettings<Team> settings = new LeagueSettings(predictionModel, teamList, roundsPerSeason, lineupSelector, roundsToSimulate, analysis.getRoundGameResults());
            System.out.println(settings.toString());

            ChessLeagueSimulation simulation = new ChessLeagueSimulation(randomGenerator, settings);
            result = simulation.runSimulation(); // run the simulation

            System.out.println("\nResult:");
            result.getTeamSeasonScoreMap().forEach((k, v) -> System.out.println(k + " " + v.toString()));
            System.out.println("\nSimulation duration: " + result.getSimulationDurationMs() + "ms");

            ResultsFileUtil.writeGameResultToFile("mc-it-0", result.getMatchResults());
            ResultsFileUtil.writeMatchResultToFile("mc-it-0", result.getMatchResults());
            ResultsFileUtil.writeSeasonResultToFile(result, 0);
        } catch (PgnParserException e) {
            System.out.println(e.getMessage());
            return;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return;
        }
        return;
    }
}

/*
Test stuff
            Path path1 = Paths.get(seasonToSimulate);
            String content1 = String.join("\n", Files.readAllLines(path1));

            Path path2 = Paths.get("games/" + division + "/1415autcht" + division + ".pgn");
            String content2 = String.join("\n", Files.readAllLines(path2));
            Path path3 = Paths.get("games/" + division + "/1314autcht" + division + ".pgn");
            content2 += String.join("\n", Files.readAllLines(path3));

            PgnAnalysis analysis1 = new PgnAnalysis(content1, content2);

            ResultsFileUtil.writePlayerStats("player-stats1", analysis1.getTeams());
 */