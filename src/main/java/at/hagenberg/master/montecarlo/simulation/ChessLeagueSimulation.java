package at.hagenberg.master.montecarlo.simulation;

import at.hagenberg.master.montecarlo.entities.MatchResult;
import at.hagenberg.master.montecarlo.entities.SeasonResult;
import at.hagenberg.master.montecarlo.entities.Team;
import at.hagenberg.master.montecarlo.lineup.AbstractLineupSelector;
import at.hagenberg.master.montecarlo.lineup.RandomSelection;
import at.hagenberg.master.montecarlo.simulation.settings.LeagueSettings;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChessLeagueSimulation extends LeagueSimulation<TeamMatch> {

    private List<Team> teamList;

    private List<String> actualTeamResult = new ArrayList<>();

    public ChessLeagueSimulation(RandomGenerator randomGenerator, LeagueSettings<Team> settings) {
        this(randomGenerator, settings, null);
    }

    public ChessLeagueSimulation(RandomGenerator randomGenerator, LeagueSettings<Team> settings, List<String> actualTeamResult) {
        super(randomGenerator, settings);
        this.actualTeamResult = actualTeamResult;
    }

    protected List<TeamMatch> initializeSimulation(LeagueSettings settings) {
        // generate rigid lineup for each team
        this.teamList = settings.getOpponentList();
        teamList.forEach(team -> team.setPlayerList(settings.getLineupSelector().pickLineupFromTeam(team)));

        List<TeamMatch> matchList = new ArrayList<>();

        // fill already played rounds
        Map<Integer, List<HeadToHeadMatch>> roundGameResults = settings.getRoundGameResults();
        for (int i = 1; i <= settings.getPlayedRounds(); i++) {
            for (int j = 0; j < roundGameResults.get(i).size(); j++) {
                HeadToHeadMatch game = roundGameResults.get(i).get(j);

                TeamMatch match = new TeamMatch(randomGenerator, settings.getPredictionModel(),
                        getTeam(game.getOpponentA().getTeamName(), teamList),
                        getTeam(game.getOpponentB().getTeamName(), teamList),
                        settings.getLineupSelector().getGamesPerMatch());
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
                TeamMatch match = new TeamMatch(randomGenerator, settings.getPredictionModel(), teamList.get(i), teamList.get(x), settings.getLineupSelector().getGamesPerMatch());
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

        if(this.actualTeamResult != null) {
            List<String> teamTable = new ArrayList<>(seasonResult.getTeamSeasonScoreMap().keySet());
            int promotionPredictionIdx = teamTable.indexOf(this.actualTeamResult.get(0));
            int relegationPredictionIdx = teamTable.indexOf(this.actualTeamResult.get(this.actualTeamResult.size() - 1));

            seasonResult.setPromotionError((promotionPredictionIdx - 0.0) * (promotionPredictionIdx - 0.0));
            seasonResult.setRelegationError((relegationPredictionIdx - (this.actualTeamResult.size() - 1.0)) * (relegationPredictionIdx - (this.actualTeamResult.size() - 1.0)));
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