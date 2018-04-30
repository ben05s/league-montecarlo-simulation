package at.hagenberg.master.montecarlo.lineup;

import at.hagenberg.master.montecarlo.entities.Player;
import at.hagenberg.master.montecarlo.entities.Team;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class AvoidStrongOpponents extends AbstractLineupSelector {

    public int playerPerformanceAndEloLineupInfluenceFactor = 2;

    public AvoidStrongOpponents(final int gamesPerMatch) {
        super(gamesPerMatch, null);
    }

    public AvoidStrongOpponents(final int gamesPerMatch, String optimizeLineupTeamName) {
        super(gamesPerMatch, optimizeLineupTeamName);
    }

    @Override
    protected Map<Player, Double> calculateLineupProbabilities(Map<Player, Double> lineupProbabilities, int slot, Team team, Team opponentTeam, boolean selectForWhite) {
        Map<Player, Double> newLineupP = new HashMap<>(lineupProbabilities);
        newLineupP.replaceAll((k,v) -> 0.0); // clear lineup probabilities from historical games

        if(!applyOptimizedLineup(team)) return newLineupP;

        Player opp = adjustLineupProbabilitiesBasedOnLikelyOpponent(newLineupP, opponentTeam.getLineup().get(slot), selectForWhite);
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

    private Player adjustLineupProbabilitiesBasedOnLikelyOpponent(Map<Player, Double> lineupProbabilities, Map<Player, Double> lineupProbabilitiesOpponent, boolean selectForWhite) {
        Map.Entry<Player, Double> likelyOpponentEntry = lineupProbabilitiesOpponent.entrySet().stream().max(Comparator.comparing(Map.Entry<Player, Double>::getValue)).get();
        Player likelyOpponent = likelyOpponentEntry.getKey();
        // select actual player from opponent team rather than always taking the one with max probability
        // TODO evaluate this if this is actually better
        // likelyOpponent = selectPlayer(opponentTeam.getLineup().get(slot));

        lineupProbabilities.replaceAll((player,p) -> {
            double diffWhiteBlackDelta = (player.getpWhiteWin() - player.getpWhiteLoss()) - (likelyOpponent.getpBlackWin() - likelyOpponent.getpBlackLoss());
            double diffBlackWhiteDelta = (player.getpBlackWin() - player.getpBlackLoss()) - (likelyOpponent.getpWhiteWin() - likelyOpponent.getpWhiteLoss());
            double eloDiff = player.getElo() - likelyOpponent.getElo();
            eloDiff /= 1000;

            if (selectForWhite) {
                double influence = (diffWhiteBlackDelta + eloDiff) * this.playerPerformanceAndEloLineupInfluenceFactor;
                return Math.max(p + influence, 0.0);
            } else {
                double influence = (diffBlackWhiteDelta + eloDiff) * this.playerPerformanceAndEloLineupInfluenceFactor;
                return Math.max(p + influence, 0.0);
            }
        });
        return likelyOpponent;
    }
}
