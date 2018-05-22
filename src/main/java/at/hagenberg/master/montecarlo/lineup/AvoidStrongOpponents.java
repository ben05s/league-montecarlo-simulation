package at.hagenberg.master.montecarlo.lineup;

import at.hagenberg.master.montecarlo.entities.Player;
import at.hagenberg.master.montecarlo.entities.Team;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class AvoidStrongOpponents extends AbstractLineupSelector {

    public int playerPerformanceAndEloLineupInfluenceFactor = 2;

    public AvoidStrongOpponents(RandomGenerator randomGenerator, final int gamesPerMatch) {
        super(randomGenerator, gamesPerMatch, false);
    }

    @Override
    protected Map<Player, Double> calculateLineupProbabilities(Map<Player, Double> lineupProbabilities, int slot, Team team, Team opponentTeam) {
        Map<Player, Double> newLineupP = new HashMap<>(lineupProbabilities);
        newLineupP.replaceAll((k,v) -> 0.0); // clear lineup probabilities from historical games

        Player opp = getLikelyOpponentPlayer(opponentTeam.getLineup().get(slot));
        // avoid strong opponents
        if(opp.getElo() > (opponentTeam.getAverageElo() + opponentTeam.getStdDeviationElo())) {
            long playersBelowAvgElo = newLineupP.keySet().stream().filter(player -> player.getElo() < (team.getAverageElo() - team.getStdDeviationElo())).count();
            newLineupP.replaceAll((player,p) -> {
                if (player.getElo() < (team.getAverageElo() - team.getStdDeviationElo()))
                    return p + (1.0 / playersBelowAvgElo);
                if (player.getElo() > (team.getAverageElo() + team.getStdDeviationElo()))
                    return 0.0;
                return p;
            });
        }
        return newLineupP;
    }

    private Player getLikelyOpponentPlayer(Map<Player, Double> lineupProbabilitiesOpponent) {
        Map.Entry<Player, Double> likelyOpponentEntry = lineupProbabilitiesOpponent.entrySet().stream().max(Comparator.comparing(Map.Entry<Player, Double>::getValue)).get();
        Player likelyOpponent = likelyOpponentEntry.getKey();
        // select actual player from opponent team rather than always taking the one with max probability
        // TODO evaluate this if this is actually better
        // likelyOpponent = selectPlayer(opponentTeam.getLineup().get(slot));
        return likelyOpponent;
    }
}
