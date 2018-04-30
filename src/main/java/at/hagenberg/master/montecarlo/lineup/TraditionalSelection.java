package at.hagenberg.master.montecarlo.lineup;

import at.hagenberg.master.montecarlo.entities.Player;
import at.hagenberg.master.montecarlo.entities.Team;

import java.util.Map;

public class TraditionalSelection extends AbstractLineupSelector {

    public TraditionalSelection(int gamesPerMatch) {
        super(gamesPerMatch);
    }

    public TraditionalSelection(int gamesPerMatch, String optimizeLineupTeamName) {
        super(gamesPerMatch, optimizeLineupTeamName);
    }

    @Override
    protected Map<Player, Double> calculateLineupProbabilities(Map<Player, Double> lineupProbabilities, int slot, Team team, Team opponentTeam, boolean selectForWhite) {
        return lineupProbabilities;
    }
}
