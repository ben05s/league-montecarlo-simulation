package at.hagenberg.master.montecarlo.simulation;

import at.hagenberg.master.montecarlo.exceptions.ChessMonteCarloSimulationException;
import at.hagenberg.master.montecarlo.entities.*;
import at.hagenberg.master.montecarlo.simulation.settings.MonteCarloSettings;
import at.hagenberg.master.montecarlo.simulation.settings.SeasonSettings;

public class ChessMatch {

    private Team teamA;
    private Team teamB;
    private MatchResult matchPrediction;
    private MatchResult matchResult;

    public ChessMatch(Team teamA, Team teamB) {
        this.teamA = teamA;
        this.teamB = teamB;
    }

    public MatchResult playMatch(MonteCarloSettings settings) throws ChessMonteCarloSimulationException {
        SeasonSettings seasonSettings = settings.getSeasonSettings();
        if(teamA.getPlayerList().size() < seasonSettings.getGamesPerMatch() || teamB.getPlayerList().size() < seasonSettings.getGamesPerMatch())
            throw new ChessMonteCarloSimulationException("not enough players in either team");

        MatchResult matchResult = new MatchResult(teamA, teamB);

        ChessLineupSelector lineupSelector = new ChessLineupSelector(settings.getRandomGenerator(), settings.getLineupSettings(), settings.getSeasonSettings());

        for (int i = 0; i < seasonSettings.getGamesPerMatch(); i++) {
            Player white = lineupSelector.pickPlayerFromTeam(i, teamA, teamB, true);
            Player black = lineupSelector.pickPlayerFromTeam(i, teamB, teamA, false);

            ChessGame game = new ChessGame(white, black);
            game.playGame(settings.getRandomGenerator(), settings.getPredictionModel());
            matchResult.addGame(game, true);
        }
        matchPrediction = matchResult;
        return matchResult;
    }

    public void addGameToActualMatchResult(ChessGame game) {
        if(matchResult == null) matchResult = new MatchResult(teamA, teamB);
        matchResult.addGame(game, false);
    }

    public MatchResult getMatchResult() {
        return matchResult;
    }

    @Override
    public String toString() {
        return "\nChessMatch{" +
                "teamA=" + teamA.getName() +
                ", teamB=" + teamB.getName() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChessMatch match = (ChessMatch) o;

        if (!teamA.equals(match.teamA) && !teamA.equals(match.teamB)) return false;
        return teamB.equals(match.teamB) || teamB.equals(match.teamA);
    }

    @Override
    public int hashCode() {
        int result = teamA.hashCode();
        result = 31 * result + teamB.hashCode();
        return result;
    }
}
