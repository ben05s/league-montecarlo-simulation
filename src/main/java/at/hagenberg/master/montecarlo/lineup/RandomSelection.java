package at.hagenberg.master.montecarlo.lineup;

import at.hagenberg.master.montecarlo.entities.Player;
import at.hagenberg.master.montecarlo.entities.Team;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.HashMap;
import java.util.Map;

public class RandomSelection extends AbstractLineupSelector {

    public RandomSelection(RandomGenerator randomGenerator, final int gamesPerMatch, final boolean useOfficialLineupRules, final boolean descending) {
        super(randomGenerator, gamesPerMatch, useOfficialLineupRules, descending);
    }

    @Override
    protected Map<Player, Double> calculateLineupProbabilities(Map<Player, Double> lineupProbabilities, int slot, Team team, Team opponentTeam) {
        Map<Player, Double> newLineupP = new HashMap<>(lineupProbabilities);
        newLineupP.replaceAll((k,v) -> (1.0 / lineupProbabilities.size()));
        return newLineupP;
    }
}
