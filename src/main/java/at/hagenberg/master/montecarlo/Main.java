package at.hagenberg.master.montecarlo;

import at.hagenberg.master.montecarlo.entities.*;
import at.hagenberg.master.montecarlo.entities.enums.LineupStrategy;
import at.hagenberg.master.montecarlo.exceptions.PgnParserException;
import at.hagenberg.master.montecarlo.lineup.AbstractLineupSelector;
import at.hagenberg.master.montecarlo.lineup.AscendingRatingSelection;
import at.hagenberg.master.montecarlo.lineup.OptimizedLineup;
import at.hagenberg.master.montecarlo.lineup.RandomSelection;
import at.hagenberg.master.montecarlo.parser.PgnAnalysis;
import at.hagenberg.master.montecarlo.simulation.*;
import at.hagenberg.master.montecarlo.prediction.ChessPredictionModel;
import at.hagenberg.master.montecarlo.simulation.LeagueSettings;
import at.hagenberg.master.montecarlo.util.EloRatingSystemUtil;
import at.hagenberg.master.montecarlo.util.ResultsFileUtil;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        SeasonResult result = new SeasonResult();
        try {
            String division = "mitte";
            String file = "1011autcht" + division + ".pgn";
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

            AbstractLineupSelector lineupSelector = new RandomSelection(randomGenerator, gamesPerMatch, true, true);
            OptimizedLineup optimizedLineup = new OptimizedLineup("ESV Austria Graz", LineupStrategy.getLineupSelector(1, randomGenerator, gamesPerMatch));
            System.out.println("Optimized Ascending Lineup Selection for Team: " + optimizedLineup.getTeamName());
            LeagueSettings<Team> settings = new LeagueSettings(predictionModel, teamList, roundsPerSeason, lineupSelector, optimizedLineup, roundsToSimulate, analysis.getRoundGameResults());
            System.out.println(settings.toString());


            // create actual season result - to measure the error
            List<String> actualTeamResult = new ArrayList<>();
            settings.setRoundsToSimulate(0);
            ChessLeagueSimulation pseudo = new ChessLeagueSimulation(randomGenerator, settings);
            SeasonResult actualResult = pseudo.runSimulation();
            actualTeamResult.addAll(actualResult.getTeamSeasonScoreMap().keySet());
            System.out.println("Actual Promoted Team: " + actualTeamResult.get(0));
            System.out.println("Actual Relegated Team: " + actualTeamResult.get(actualTeamResult.size()-1));
            settings.setRoundsToSimulate(roundsToSimulate);
            ChessLeagueSimulation simulation = new ChessLeagueSimulation(randomGenerator, settings, actualTeamResult);
            result = simulation.runSimulation(); // run the simulation

            System.out.println("\nResult:");
            result.getTeamSeasonScoreMap().forEach((k, v) -> System.out.println(k + " " + v.toString()));
            double rmsePromotion = Math.sqrt(result.getPromotionError());
            double rmseRelegation = Math.sqrt(result.getRelegationError());

            System.out.println("\nRootMeanSquareError Promotion: " + rmsePromotion);
            System.out.println("\nRootMeanSquareError Relegation: " + rmseRelegation);
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