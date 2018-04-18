package at.hagenberg.master.montecarlo.entities;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public class Team extends Opponent {

    private List<Player> playerList = new ArrayList<>();
    private double averageElo = 0.0;
    private double stdDeviationElo = 0.0;
    private List<Map<Player, Double>> lineup = new ArrayList<>();

    public Team(String name) {
        super(name);
    }

    public void addPlayer(Player player) {
        this.playerList.add(player);
        this.averageElo = this.calculateAvgElo(playerList);
        this.stdDeviationElo = this.calculateStdDeviationElo(playerList, averageElo);
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

    public List<Player> getPlayerList() {
        return playerList;
    }

    public List<Map<Player, Double>> getLineup() {
        return lineup;
    }

    public void setLineup(List<Map<Player, Double>> lineup) {
        this.lineup = lineup;
    }

    public double getAverageElo() { return averageElo; }

    public double getStdDeviationElo() { return stdDeviationElo; }

    public void setPlayerList(List<Player> playerList) {
        this.playerList = playerList;
    }

    public void setAverageElo(double averageElo) {
        this.averageElo = averageElo;
    }

    public void setStdDeviationElo(double stdDeviationElo) {
        this.stdDeviationElo = stdDeviationElo;
    }
}
