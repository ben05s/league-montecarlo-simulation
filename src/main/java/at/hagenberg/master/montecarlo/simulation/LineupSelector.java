package at.hagenberg.master.montecarlo.simulation;

import at.hagenberg.master.montecarlo.entities.Player;
import at.hagenberg.master.montecarlo.entities.Team;
import at.hagenberg.master.montecarlo.entities.enums.LineupStrategy;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;
import java.util.stream.IntStream;

public class LineupSelector {

    private final int gamesPerMatch;
    private LineupStrategy lineupStrategy;
    private String optimizeLineupTeamName;

    public int whiteBlackPerformanceStrategyLineupInfluenceFactor = 4;
    public int playerPerformanceAndEloLineupInfluenceFactor = 2;

    private List<Player> alreadySelectedPlayers = new ArrayList<>();

    public LineupSelector(final int gamesPerMatch) {
        this(LineupStrategy.DESCENDING_RATING_STRENGTH, gamesPerMatch, null);
    }

    public LineupSelector(LineupStrategy lineupStrategy, int gamesPerMatch) {
        this(lineupStrategy, gamesPerMatch, null);
    }

    public LineupSelector(LineupStrategy lineupStrategy, int gamesPerMatch, String optimizeLineupTeamName) {
        this.lineupStrategy = lineupStrategy;
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

    private Player selectPlayer(RandomGenerator randomGenerator, Map<Player, Double> lineupProbabilities) {
        int[] idxToGenerate = IntStream.range(0, lineupProbabilities.keySet().size()).toArray();
        double[] discreteProbabilities = lineupProbabilities.values().stream().mapToDouble(Double::doubleValue).toArray();
        EnumeratedIntegerDistribution distribution = new EnumeratedIntegerDistribution(randomGenerator, idxToGenerate, discreteProbabilities);

        int numSamples = 1;
        int[] samples = distribution.sample(numSamples);

        Player selectedPlayer = new ArrayList<>(lineupProbabilities.keySet()).get(samples[0]);
        return selectedPlayer;
    }

    private Map<Player, Double> calculateLineupProbabilities(final Map<Player, Double> lineupProbabilities, int slot, Team team, Team opponentTeam, boolean selectForWhite) {
        Map<Player, Double> newLineupP = new HashMap<>(lineupProbabilities);
        newLineupP.replaceAll((k,v) -> 0.0); // clear lineup probabilities from historical games

        switch (this.lineupStrategy) {
            case AVOID_STRONG_OPPONENTS:
                if(!applyOptimizedLineup(team)) break;

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
                break;
            case MATCH_STRONG_OPPONENTS:
                if(!applyOptimizedLineup(team)) break;

                Player opp2 = adjustLineupProbabilitiesBasedOnLikelyOpponent(newLineupP, opponentTeam.getLineup().get(slot), selectForWhite);
                // match strong opponents
                if(opp2.getElo() > (opponentTeam.getAverageElo() + opponentTeam.getStdDeviationElo())) {
                    long playersAboveAvgElo = newLineupP.keySet().stream().filter(player -> player.getElo() > (team.getAverageElo() + team.getStdDeviationElo())).count();
                    newLineupP.replaceAll((player,p) -> {
                        if (player.getElo() > (team.getAverageElo() + team.getStdDeviationElo()))
                            return p + (1.0 / playersAboveAvgElo);
                        if (player.getElo() < (team.getAverageElo() + team.getStdDeviationElo()))
                            return 0.0;

                        return p;
                    });
                }
                break;
            case WHITE_BLACK_PERFORMANCE:
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
                break;
            case DESCENDING_RATING_STRENGTH:
                Iterator<Map.Entry<Player, Double>> sortedDescIt = newLineupP.entrySet().stream().sorted(Map.Entry.comparingByKey(Comparator.comparing(Player::getElo, Comparator.reverseOrder()))).iterator();
                Map.Entry<Player, Double> entryDesc = getEntryAtPosition(slot, sortedDescIt);
                if(entryDesc != null)
                    newLineupP.replace(entryDesc.getKey(), entryDesc.getValue() + 1.0);
                break;
            case ASCENDING_RATING_STRENGTH:
                Iterator<Map.Entry<Player, Double>> sortedDesc = newLineupP.entrySet().stream().sorted(Map.Entry.comparingByKey(Comparator.comparing(Player::getElo, Comparator.reverseOrder()))).iterator();
                Map.Entry<Player, Double> entryAsc = getEntryAtPosition(this.gamesPerMatch - slot - 1, sortedDesc);
                if(entryAsc != null)
                    newLineupP.replace(entryAsc.getKey(), entryAsc.getValue() + 1.0);
                break;
            case RANDOM:
                // equal probability for every player
                newLineupP.replaceAll((k,v) -> (1.0 / lineupProbabilities.size()));
                break;
            case TRADITIONAL:
                newLineupP = lineupProbabilities; // use probability based on previous selections from the past season data
                break;
            default:
                // no lineup strategy - cannot happen
        }
        return newLineupP;
    }

    /**
     * Check whether or not one team should optimize their lineup to gain an advantage
     * @param team - the team for which the line up against a probable opponent team lineup should be optimized
     * @return true or false
     */
    private boolean applyOptimizedLineup(Team team) {
        return this.optimizeLineupTeamName != null
                && this.optimizeLineupTeamName.equals(team.getName());
    }

    private Map.Entry<Player, Double> getEntryAtPosition(int slot, Iterator<Map.Entry<Player, Double>> sortedIt) {
        int count = 0;
        while(count++ < slot)
            if(sortedIt.hasNext()) sortedIt.next();
        if(sortedIt.hasNext())
            return sortedIt.next();
        return null;
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

    /**
     * Ensure Probabilities sum to 1.0
     * @param discreteProbabilities
     * @return
     */
    private Map<Player, Double> normalizeProbabilities(Map<Player, Double> discreteProbabilities) {
        String sum = String.format("%.4f", discreteProbabilities.values().stream().mapToDouble(Double::doubleValue).sum());
        String target = String.format("%.4f", 1.0);
        while(!sum.equals(target)) {
            double pSum = discreteProbabilities.values().stream().mapToDouble(Double::doubleValue).sum();

            long divider = discreteProbabilities.size();
            if (pSum > 1)
                divider = discreteProbabilities.entrySet().stream().filter(entry -> entry.getValue() > 0.0).count();

            double pBase = (1 - pSum) / divider;

            // set probability to zero for already selected players
            // ensure non negative zeros (https://stackoverflow.com/questions/6724031/how-can-a-primitive-float-value-be-0-0-what-does-that-mean)
            discreteProbabilities.replaceAll((k,v) -> this.alreadySelectedPlayers.contains(k) ? 0.0 : Math.abs(Math.max(v + pBase, 0.0)));

            sum = String.format("%.4f", discreteProbabilities.values().stream().mapToDouble(Double::doubleValue).sum());
        }
        return discreteProbabilities;
    }

    /* output stuff
        //System.out.println("position " + slot + " team " + likelyOpponent.getTeamName()+ " " + likelyOpponent.getName() + "(" + likelyOpponent.getElo() + ") p " + String.format("%.4f", likelyOpponentEntry.getValue()));
        //System.out.println("avg elo " + opponentTeam.getAverageElo() + " std deviation " + opponentTeam.getStdDeviationElo());

        //System.out.println("Before lineup strategy");
        //lineupProbabilities.forEach((k,d) -> System.out.print(slot + " " + k.getName() + " " + String.format("%.4f", d) + " "));
        //System.out.println();

        //System.out.println("After lineup strategy");
        //lineupProbabilities.forEach((k,d) -> System.out.print(slot + " " + k.getName() + " " + String.format("%.4f", d) + " "));
        //System.out.println();
     */
}
