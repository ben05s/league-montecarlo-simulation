package at.hagenberg.master.montecarlo.simulation;

import at.hagenberg.master.montecarlo.entities.MatchResult;
import at.hagenberg.master.montecarlo.entities.SeasonResult;
import at.hagenberg.master.montecarlo.entities.Team;
import at.hagenberg.master.montecarlo.simulation.settings.LeagueSettings;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChessLeagueSimulation extends LeagueSimulation<TeamMatch> {

    public ChessLeagueSimulation(RandomGenerator randomGenerator, LeagueSettings<Team> settings) {
        super(randomGenerator, settings);
    }

    protected List<TeamMatch> initializeMatchList(LeagueSettings settings) {
        List<Team> teamList = settings.getOpponentList();
        Map<Integer, List<HeadToHeadMatch>> roundGameResults = settings.getRoundGameResults();

        List<TeamMatch> matchList = new ArrayList<>();
        // fill already played rounds
        for (int i = 1; i <= settings.getPlayedRounds(); i++) {
            for (int j = 0; j < roundGameResults.get(i).size(); j++) {
                HeadToHeadMatch game = roundGameResults.get(i).get(j);

                TeamMatch match = new TeamMatch(randomGenerator, settings.getPredictionModel(), settings.getLineupSelector(), getTeam(game.getOpponentA().getTeamName(), teamList), getTeam(game.getOpponentB().getTeamName(), teamList));
                int idx = matchList.indexOf(match);
                if (idx != -1) {
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
                if (i == x) continue;
                TeamMatch match = new TeamMatch(randomGenerator, settings.getPredictionModel(), settings.getLineupSelector(), teamList.get(i), teamList.get(x));
                if (!matchList.contains(match)) matchList.add(match);
            }
        }
        return matchList;
    }

    @Override
    public SeasonResult runSimulation() {
        long startTime = System.currentTimeMillis();

        SeasonResult seasonResult = new SeasonResult();
        for (int x = 0; x < matchList.size(); x++) {
            TeamMatch match = matchList.get(x);
            MatchResult result = match.getMatchResult();
            if(result == null) { // only simulate the match if it has not been played yet - in case rest of the season is simulated
                result = match.playMatch();
            }
            seasonResult.addMatchResult(result);
        }
        seasonResult.setSimulationDurationMs(System.currentTimeMillis() - startTime);
        return seasonResult;
    }

    private Team getTeam(String teamName, List<Team> teams) {
        int idx = teams.indexOf(new Team(teamName));
        if(idx != -1) {
            return teams.get(idx);
        }
        return null;
    }
}