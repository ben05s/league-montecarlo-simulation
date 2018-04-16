package at.hagenberg.master.montecarlo.entities;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public class Team {

    private int gamesPerMatch;

    private String name;
    private List<Player> playerList = new ArrayList<>();
    private double averageElo = 0.0;
    private double stdDeviationElo = 0.0;
    private List<Map<Player, Double>> lineup = new ArrayList<>();

    public Team(String name, int gamesPerMatch) {
        this.name = name;
        this.gamesPerMatch = gamesPerMatch;
    }

    public void addPlayer(Player player) {
        this.playerList.add(player);
        this.averageElo = this.calculateAvgElo(playerList);
        this.stdDeviationElo = this.calculateStdDeviationElo(playerList, averageElo);
        this.lineup = transposeLineupProbabilities(playerList);
    }

    private double calculateAvgElo(List<Player> playerList) {
        int sum = 0;
        for (int i = 0; i < playerList.size(); i++) {
            sum += playerList.get(i).getElo();
        }
        return (double) sum / playerList.size();
    }

    private double calculateStdDeviationElo(List<Player> playerList, double averageElo) {
        double[] values = new double[playerList.size()];
        for(int i = 0; i < playerList.size(); i++){
            values[i] = Math.pow((playerList.get(i).getElo() - averageElo),2);
        }
        double variance = DoubleStream.of(values).sum() / values.length;
        return Math.sqrt(variance);
    }

    /**
     * Arrange Lineup Probabilities 2-dimensional x-axis lineup probability of player, y-axis lineup position
     * e.g.: lineup probabilities of a team having 6 players
     * 	 Position 0:	(0) 0,0000		(1) 0,0404		(2) 0,0000		(3) 0,0000		(4) 0,0000		(5) 0,0000
     *   Position 1:	(0) 0,0000		(1) 0,2121		(2) 0,0000		(3) 0,0101		(4) 0,0000		(5) 0,0000
     *   Position 2:	(0) 0,0000		(1) 0,1717		(2) 0,0000		(3) 0,1414		(4) 0,0303		(5) 0,0000
     *   Position 3:	(0) 0,0808		(1) 0,0303		(2) 0,0000		(3) 0,2525		(4) 0,3434		(5) 0,0000
     *   Position 4:	(0) 0,0000		(1) 0,0000		(2) 0,0000		(3) 0,2020		(4) 0,0606		(5) 0,0000
     *   Position 5:	(0) 0,0000		(1) 0,0000		(2) 0,0000		(3) 0,0000		(4) 0,1111		(5) 0,0909
     * @param playerList
     * @return
     */
    private List<Map<Player, Double>> transposeLineupProbabilities(List<Player> playerList) {
        List<Map<Player, Double>> lineup = new ArrayList<>(gamesPerMatch);
        IntStream.range(0, gamesPerMatch).forEach((int i) -> {
            Map<Player, Double> pMap = playerList.stream()
                    .collect(
                            Collectors.toMap(
                                    player -> player, player -> player.getpLineUp().get(i),
                                    (oldValue, newValue) -> oldValue,
                                    LinkedHashMap::new)
                    );
            lineup.add(pMap);
        });
        return lineup;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return Objects.equals(name, team.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        String str = "Team: " + name + " avgElo " + String.format("%.2f", averageElo) + "\n";
        for (int i = 0; i < playerList.size(); i++) {
            str += "\t(" + i + ") " + playerList.get(i).toString() + "\n";
        }
        str += "Lineup Probabilities \n";
        for (int x = 0; x < lineup.size(); x++) {
            str += "\t Position " + x + ":";
            Iterator<Map.Entry<Player, Double>> it = lineup.get(x).entrySet().iterator();
            int y = 0;
            while(it.hasNext()) {
                str += "\t(" + y + ") " + String.format("%.4f", it.next().getValue()) + "\t";
                y++;
            }
            str += "\n";
        }
        return str;
    }

    public String getName() {
        return name;
    }

    public List<Player> getPlayerList() {
        return playerList;
    }

    public List<Map<Player, Double>> getLineup() {
        return lineup;
    }

    public double getAverageElo() { return averageElo; }

    public double getStdDeviationElo() { return stdDeviationElo; }
}
