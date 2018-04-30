package at.hagenberg.master.montecarlo.lineup;

import at.hagenberg.master.montecarlo.entities.Player;
import at.hagenberg.master.montecarlo.entities.Team;

import java.util.HashMap;
import java.util.Map;

public class RandomSelection extends AbstractLineupSelector {

    public RandomSelection(final int gamesPerMatch) {
        super(gamesPerMatch, null);
    }

    public RandomSelection(int gamesPerMatch, String optimizeLineupTeamName) {
        super(gamesPerMatch, optimizeLineupTeamName);
    }

    @Override
    protected Map<Player, Double> calculateLineupProbabilities(Map<Player, Double> lineupProbabilities, int slot, Team team, Team opponentTeam, boolean selectForWhite) {
        Map<Player, Double> newLineupP = new HashMap<>(lineupProbabilities);
        newLineupP.replaceAll((k,v) -> (1.0 / lineupProbabilities.size()));
        return newLineupP;
    }
}
