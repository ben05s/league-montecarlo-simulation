package at.hagenberg.master.montecarlo.lineup;

import at.hagenberg.master.montecarlo.entities.Player;
import at.hagenberg.master.montecarlo.entities.Team;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class AbstractLineupSelector {

    private RandomGenerator randomGenerator;
    private final boolean useOfficialLineupRules;
    private final boolean descending;
    private List<Player> selectedPlayers = new ArrayList<>();

    protected final int gamesPerMatch;

    public AbstractLineupSelector(RandomGenerator randomGenerator, final int gamesPerMatch, boolean useOfficialLineupRules) {
        this.randomGenerator = randomGenerator;
        this.gamesPerMatch = gamesPerMatch;
        this.useOfficialLineupRules = useOfficialLineupRules;
        this.descending = false;
    }

    public AbstractLineupSelector(RandomGenerator randomGenerator, final int gamesPerMatch, boolean useOfficialLineupRules, final boolean descending) {
        this.randomGenerator = randomGenerator;
        this.gamesPerMatch = gamesPerMatch;
        this.useOfficialLineupRules = useOfficialLineupRules;
        this.descending = descending;
    }

    public List<Player> pickLineupFromTeam(Team team) {
        return this.pickLineupFromTeam(team, null);
    }

    public List<Player> pickLineupFromTeam(Team team, Team opponentTeam) {
        do {
            this.selectedPlayers = new ArrayList<>(this.gamesPerMatch);
            for (int i = 0; i < this.gamesPerMatch; i++) {
                Map<Player, Double> teamPlayerProbabilities = new LinkedHashMap<>(team.getLineup().get(i));
                teamPlayerProbabilities = calculateLineupProbabilities(teamPlayerProbabilities, i, team, opponentTeam);

                Player selectedPlayer = selectPlayer(randomGenerator, teamPlayerProbabilities);
                while(this.selectedPlayers.contains(selectedPlayer)) {
                    selectedPlayer = selectPlayer(randomGenerator, teamPlayerProbabilities);
                }
                this.selectedPlayers.add(selectedPlayer);
            }
        } while(!isValidLineup());

        if(this.useOfficialLineupRules) return this.selectedPlayers;

        if(descending) {
            this.selectedPlayers = this.selectedPlayers.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        } else {
            this.selectedPlayers = this.selectedPlayers.stream().sorted().collect(Collectors.toList());
        }
        return this.selectedPlayers;
    }

    public int getGamesPerMatch() {
        return this.gamesPerMatch;
    }

    protected abstract Map<Player, Double> calculateLineupProbabilities(final Map<Player, Double> lineupProbabilities, int slot, Team team, Team opponentTeam);

    private boolean isValidLineup() {
        if(!this.useOfficialLineupRules) return true;

        for (int i = 0; i < this.selectedPlayers.size(); i++) {
            if(i == 0) continue;
            for (int j = 0; j < i; j++) {
                if(this.selectedPlayers.get(i).getElo() > this.selectedPlayers.get(j).getElo()+200)
                    return false;
            }
        }

        return this.selectedPlayers.size() == this.gamesPerMatch;
    }

    private Player selectPlayer(RandomGenerator randomGenerator, Map<Player, Double> lineupProbabilities) {
        int[] idxToGenerate = IntStream.range(0, lineupProbabilities.keySet().size()).toArray();
        double[] discreteProbabilities = lineupProbabilities.values().stream().mapToDouble(Double::doubleValue).toArray();
        EnumeratedIntegerDistribution distribution = new EnumeratedIntegerDistribution(randomGenerator, idxToGenerate, discreteProbabilities);

        int numSamples = 1;
        int[] samples = distribution.sample(numSamples);

        Player selectedPlayer = new ArrayList<>(lineupProbabilities.keySet()).get(samples[0]);
        return selectedPlayer;
    }
}
