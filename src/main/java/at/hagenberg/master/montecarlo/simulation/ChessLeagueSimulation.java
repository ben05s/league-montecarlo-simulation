package at.hagenberg.master.montecarlo.simulation;

import at.hagenberg.master.montecarlo.entities.*;
import at.hagenberg.master.montecarlo.simulation.settings.ChessLeagueSettings;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;

public class ChessLeagueSimulation extends AbstractMonteCarloSimulation {

    private ChessLeagueSettings settings;
    protected List<TeamMatch> matchList;

    public ChessLeagueSimulation(RandomGenerator randomGenerator, ChessLeagueSettings settings) {
        super(randomGenerator);

        Objects.requireNonNull(settings);
        this.settings = settings;
        this.matchList = initializeMatchList(settings.getOpponentList(), settings.getRoundGameResults());
    }

    protected List<TeamMatch> initializeMatchList(List<Team> teamList, Map<Integer, List<HeadToHeadMatch>> roundGameResults) {
        List<TeamMatch> matchList = new ArrayList<>();
        // fill already played rounds
        for (int i = 1; i <= settings.getPlayedRounds(); i++) {
            for (int j = 0; j < roundGameResults.get(i).size(); j++) {
                HeadToHeadMatch game = roundGameResults.get(i).get(j);

                TeamMatch match = new TeamMatch(randomGenerator, settings.getPredictionModel(), getTeam(game.getOpponentA().getTeamName(), teamList), getTeam(game.getOpponentB().getTeamName(), teamList));
                int idx = matchList.indexOf(match);
                if(idx != -1) {
                    match = matchList.get(idx);
                } else {
                    matchList.add(match);
                }
                match.addHeadToHeadMatchResult(game);
            }
        }

        // create remaining match-ups to be simulated
        for (int i = 0; i < teamList.size(); i++) {
            for (int x = 0; x < teamList.size(); x++) {
                if(i == x) continue;
                TeamMatch match = new TeamMatch(randomGenerator, settings.getPredictionModel(), teamList.get(i), teamList.get(x));
                if(!matchList.contains(match)) matchList.add(match);
            }
        }
        return matchList;
    }

    public Team getTeam(String teamName, List<Team> teams) {
        int idx = teams.indexOf(new Team(teamName));
        if(idx != -1) {
            return teams.get(idx);
        }
        return null;
    }

    @Override
    public SeasonResult runSimulation() {
        long startTime = System.currentTimeMillis();

        SeasonResult seasonResult = new SeasonResult();
        for (int x = 0; x < matchList.size(); x++) {
            TeamMatch match = matchList.get(x);
            MatchResult result = match.getMatchResult();
            if(result == null) { // only simulate the match if it has not been played yet - in case rest of the season is simulated
                LineupSelector lineupSelector = settings.getLineupSelector();
                for (int i = 0; i < this.settings.getGamesPerMatch(); i++) {
                    Player white = lineupSelector.pickPlayerFromTeam(randomGenerator, i, match.getOpponentA(), match.getOpponentB(), true);
                    Player black = lineupSelector.pickPlayerFromTeam(randomGenerator, i, match.getOpponentB(), match.getOpponentA(), false);
                    match.addHeadToHeadMatchToSimulate(new HeadToHeadMatch(randomGenerator, settings.getPredictionModel(), white, black));
                }
                result = match.playMatch();
            }
            seasonResult.addMatchResult(result);
        }
        seasonResult.setSimulationDurationMs(System.currentTimeMillis() - startTime);
        return seasonResult;
    }
}
