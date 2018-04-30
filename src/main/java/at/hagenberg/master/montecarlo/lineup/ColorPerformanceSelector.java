package at.hagenberg.master.montecarlo.lineup;

import at.hagenberg.master.montecarlo.entities.Player;
import at.hagenberg.master.montecarlo.entities.Team;

import java.util.HashMap;
import java.util.Map;

public class ColorPerformanceSelector extends AbstractLineupSelector {

    public int whiteBlackPerformanceStrategyLineupInfluenceFactor = 4;

    public ColorPerformanceSelector(int gamesPerMatch) {
        super(gamesPerMatch);
    }

    public ColorPerformanceSelector(int gamesPerMatch, String optimizeLineupTeamName) {
        super(gamesPerMatch, optimizeLineupTeamName);
    }

    @Override
    protected Map<Player, Double> calculateLineupProbabilities(Map<Player, Double> lineupProbabilities, int slot, Team team, Team opponentTeam, boolean selectForWhite) {
        Map<Player, Double> newLineupP = new HashMap<>(lineupProbabilities);
        newLineupP.replaceAll((player,p) -> {
            double deltaWinLossWhite = player.getpWhiteWin() - player.getpWhiteLoss();
            double deltaWinLossBlack = player.getpBlackWin() - player.getpBlackLoss();

            if(selectForWhite) {
                if(deltaWinLossWhite > 0) return p + (deltaWinLossWhite * this.whiteBlackPerformanceStrategyLineupInfluenceFactor);
            } else {
                if(deltaWinLossBlack > 0) { return p + (deltaWinLossBlack * this.whiteBlackPerformanceStrategyLineupInfluenceFactor);
                }
            }
            return p;
        });
        return newLineupP;
    }
}
