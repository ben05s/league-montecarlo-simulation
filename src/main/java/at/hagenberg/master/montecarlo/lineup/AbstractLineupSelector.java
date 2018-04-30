package at.hagenberg.master.montecarlo.lineup;

import at.hagenberg.master.montecarlo.entities.Player;
import at.hagenberg.master.montecarlo.entities.Team;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public abstract class AbstractLineupSelector {

    private final int gamesPerMatch;
    private String optimizeLineupTeamName;
    private List<Player> alreadySelectedPlayers = new ArrayList<>();


    public AbstractLineupSelector(final int gamesPerMatch) {
        this(gamesPerMatch, null);
    }

    public AbstractLineupSelector(int gamesPerMatch, String optimizeLineupTeamName) {
        this.gamesPerMatch = gamesPerMatch;
        this.optimizeLineupTeamName = optimizeLineupTeamName;
    }

    public Player pickPlayerFromTeam(RandomGenerator randomGenerator, int slot, Team teamA, Team teamB, boolean selectForWhite) {
        Team team = slot % 2 == 0 ? teamA : teamB;
        Team opponentTeam = slot % 2 == 0 ? teamB : teamA;

        Map<Player, Double> teamPlayerProbabilities = new LinkedHashMap<>(team.getLineup().get(slot));
        teamPlayerProbabilities = calculateLineupProbabilities(teamPlayerProbabilities, slot, team, opponentTeam, selectForWhite);

        Player selectedPlayer = selectPlayer(randomGenerator, teamPlayerProbabilities);
        this.alreadySelectedPlayers.add(selectedPlayer);

        return selectedPlayer;
    }

    public int getGamesPerMatch() {
        return this.gamesPerMatch;
    }

    protected abstract Map<Player, Double> calculateLineupProbabilities(final Map<Player, Double> lineupProbabilities, int slot, Team team, Team opponentTeam, boolean selectForWhite);

    private Player selectPlayer(RandomGenerator randomGenerator, Map<Player, Double> lineupProbabilities) {
        int[] idxToGenerate = IntStream.range(0, lineupProbabilities.keySet().size()).toArray();
        double[] discreteProbabilities = lineupProbabilities.values().stream().mapToDouble(Double::doubleValue).toArray();
        EnumeratedIntegerDistribution distribution = new EnumeratedIntegerDistribution(randomGenerator, idxToGenerate, discreteProbabilities);

        int numSamples = 1;
        int[] samples = distribution.sample(numSamples);

        Player selectedPlayer = new ArrayList<>(lineupProbabilities.keySet()).get(samples[0]);
        return selectedPlayer;
    }

    /**
     * Check whether or not one team should optimize their lineup to gain an advantage
     * @param team - the team for which the line up against a probable opponent team lineup should be optimized
     * @return true or false
     */
    protected boolean applyOptimizedLineup(Team team) {
        return this.optimizeLineupTeamName != null
                && this.optimizeLineupTeamName.equals(team.getName());
    }
}
