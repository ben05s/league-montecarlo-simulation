package at.hagenberg.master.montecarlo;

import at.hagenberg.master.montecarlo.entities.*;
import at.hagenberg.master.montecarlo.entities.enums.LineupStrategy;
import at.hagenberg.master.montecarlo.exceptions.PgnParserException;
import at.hagenberg.master.montecarlo.lineup.AbstractLineupSelector;
import at.hagenberg.master.montecarlo.lineup.LineupSelector;
import at.hagenberg.master.montecarlo.lineup.RandomSelection;
import at.hagenberg.master.montecarlo.parser.PgnAnalysis;
import at.hagenberg.master.montecarlo.simulation.*;
import at.hagenberg.master.montecarlo.prediction.ChessPredictionModel;
import at.hagenberg.master.montecarlo.simulation.settings.LeagueSettings;
import at.hagenberg.master.montecarlo.util.EloRatingSystemUtil;
import at.hagenberg.master.montecarlo.util.ResultsFileUtil;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        SeasonResult result = new SeasonResult();
        try {
            String division = "mitte";
            String file = "1516autcht" + division + ".pgn";
            String seasonToSimulate = new String(Files.readAllBytes(Paths.get("games/" + division + "/" + file)));
            String historicalSeasons = new String(Files.readAllBytes(Paths.get("games/" + division + "/historicData" + file)));

            RandomGenerator randomGenerator = new Well19937c();
            final int gamesPerMatch = 6;
            final int roundsPerSeason = 11;
            final int roundsToSimulate = 11;
            ChessPredictionModel predictionModel = new ChessPredictionModel(true, false, false, false, true);

            PgnAnalysis analysis = new PgnAnalysis(seasonToSimulate, historicalSeasons, roundsPerSeason, gamesPerMatch);
            List<Team> teamList = analysis.getTeams();
            predictionModel.setStatistics(analysis);
            analysis.fillGamesFromSeasonToSimulate(randomGenerator, predictionModel);

            if(predictionModel.useRatingRegularization)
                teamList = EloRatingSystemUtil.regularizePlayerRatingsForTeams(teamList, predictionModel.getAvgElo(), predictionModel.regularizeThreshold, predictionModel.regularizeFraction);

            ResultsFileUtil.writePlayerStats("player-stats", teamList);

            LeagueSettings<Team> settings = new LeagueSettings(predictionModel, teamList, roundsPerSeason, new RandomSelection(randomGenerator, gamesPerMatch, true), roundsToSimulate, analysis.getRoundGameResults());
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