package at.hagenberg.master.montecarlo.simulation;

import at.hagenberg.master.montecarlo.exceptions.ChessMonteCarloSimulationException;
import at.hagenberg.master.montecarlo.entities.MatchResult;
import at.hagenberg.master.montecarlo.entities.SeasonResult;
import at.hagenberg.master.montecarlo.entities.Team;
import at.hagenberg.master.montecarlo.simulation.settings.MonteCarloSettings;

import java.util.*;

public class MonteCarloSimulation {

    private MonteCarloSettings settings;
    private List<ChessMatch> matchList = new ArrayList<>();

    public MonteCarloSimulation(MonteCarloSettings settings, List<Team> teamList, Map<Integer, List<ChessGame>> roundGameResults) {
        this.settings = settings;
        initializeMatchList(teamList, roundGameResults);
    }

    private void initializeMatchList(List<Team> teamList, Map<Integer, List<ChessGame>> roundGameResults) {
        // fill already played rounds
        for (int i = 1; i <= settings.getSeasonSettings().getPlayedRounds(); i++) {
            for (int j = 0; j < roundGameResults.get(i).size(); j++) {
                ChessGame game = roundGameResults.get(i).get(j);

                ChessMatch match = new ChessMatch(game.getWhite().getTeam(), game.getBlack().getTeam());
                if(matchList.contains(match)) {
                    match = getMatch(match);
                } else {
                    matchList.add(match);
                }
                match.addGameToActualMatchResult(game);
            }
        }

        // create remaining match-ups
        for (int i = 0; i < teamList.size(); i++) {
            for (int x = 0; x < teamList.size(); x++) {
                if(i == x) continue;
                ChessMatch match = new ChessMatch(teamList.get(i), teamList.get(x));
                if(!this.matchList.contains(match)) this.matchList.add(match);
            }
        }
    }

    public SeasonResult runSimulation() throws ChessMonteCarloSimulationException {
        long startTime = System.currentTimeMillis();

        SeasonResult seasonResult = new SeasonResult();
        for (int x = 0; x < matchList.size(); x++) {
            ChessMatch match = matchList.get(x);
            MatchResult result = match.getMatchResult();
            // only simulate the match if it has not been played yet - in case rest of the season is simulated
            if(result == null) {
                result = match.playMatch(settings);
            }
            seasonResult.addMatchResult(result);
        }
        seasonResult.setSimulationDurationMs(System.currentTimeMillis() - startTime);
        return seasonResult;
    }

    private ChessMatch getMatch(ChessMatch match) {
        for (int i = 0; i < matchList.size(); i++) {
            if(match.equals(matchList.get(i))) return matchList.get(i);
        }
        return null;
    }
}
