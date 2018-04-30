package at.hagenberg.master.montecarlo.lineup;

import at.hagenberg.master.montecarlo.entities.Player;
import at.hagenberg.master.montecarlo.entities.Team;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DescendingRatingSelection extends AbstractLineupSelector {

    public DescendingRatingSelection(int gamesPerMatch) {
        super(gamesPerMatch);
    }

    public DescendingRatingSelection(int gamesPerMatch, String optimizeLineupTeamName) {
        super(gamesPerMatch, optimizeLineupTeamName);
    }

    @Override
    protected Map<Player, Double> calculateLineupProbabilities(Map<Player, Double> lineupProbabilities, int slot, Team team, Team opponentTeam, boolean selectForWhite) {
        Map<Player, Double> newLineupP = new HashMap<>(lineupProbabilities);
        newLineupP.replaceAll((k,v) -> 0.0); // clear lineup probabilities from historical games

        Iterator<Map.Entry<Player, Double>> sortedDescIt = newLineupP.entrySet().stream().sorted(Map.Entry.comparingByKey(Comparator.comparing(Player::getElo, Comparator.reverseOrder()))).iterator();
        Map.Entry<Player, Double> entryDesc = getEntryAtPosition(slot, sortedDescIt);
        if(entryDesc != null)
            newLineupP.replace(entryDesc.getKey(), entryDesc.getValue() + 1.0);
        return newLineupP;
    }

    private Map.Entry<Player, Double> getEntryAtPosition(int slot, Iterator<Map.Entry<Player, Double>> sortedIt) {
        int count = 0;
        while(count++ < slot)
            if(sortedIt.hasNext()) sortedIt.next();
        if(sortedIt.hasNext())
            return sortedIt.next();
        return null;
    }
}
