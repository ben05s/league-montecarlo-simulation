package at.hagenberg.master.montecarlo.lineup;

import at.hagenberg.master.montecarlo.entities.Player;
import at.hagenberg.master.montecarlo.entities.Team;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;

public class TraditionalSelection extends AbstractLineupSelector {

    public TraditionalSelection(RandomGenerator randomGenerator, final int gamesPerMatch) {
        super(randomGenerator, gamesPerMatch, false);
    }

    @Override
    protected Map<Player, Double> calculateLineupProbabilities(Map<Player, Double> lineupProbabilities, int slot, Team team, Team opponentTeam) {
        return lineupProbabilities;
    }
}
