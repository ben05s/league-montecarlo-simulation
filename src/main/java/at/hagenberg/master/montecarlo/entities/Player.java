package at.hagenberg.master.montecarlo.entities;

import java.util.*;

public class Player {

    private String name;
    private Team team;

    private int elo;
    private int eloDelta;

    private int regElo = 0;

    private long totalGames;
    private long whiteWins;
    private long whiteDraws;
    private long whiteLoss;
    private long blackWins;
    private long blackDraws;
    private long blackLoss;

    private double pWhiteWin;
    private double pWhiteDraw;
    private double pWhiteLoss;

    private double pBlackWin;
    private double pBlackDraw;
    private double pBlackLoss;

    private List<Double> pLineUp = new ArrayList<>();

    public Player() {
    }

    public List<Double> getpLineUp() {
        return pLineUp;
    }

    public void setpLineUp(List<Double> pLineUp) {
        this.pLineUp = pLineUp;
    }

    public long getWhiteLoss() {
        return whiteLoss;
    }

    public void setWhiteLoss(long whiteLoss) {
        this.whiteLoss = whiteLoss;
    }

    public long getBlackLoss() {
        return blackLoss;
    }

    public void setBlackLoss(long blackLoss) {
        this.blackLoss = blackLoss;
    }

    public double getpWhiteLoss() {
        return pWhiteLoss;
    }

    public void setpWhiteLoss(double pWhiteLoss) {
        this.pWhiteLoss = pWhiteLoss;
    }

    public double getpBlackLoss() {
        return pBlackLoss;
    }

    public void setpBlackLoss(double pBlackLoss) {
        this.pBlackLoss = pBlackLoss;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getElo() {
        return elo;
    }

    public void setElo(int elo) {
        this.elo = elo;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public long getWhiteWins() {
        return whiteWins;
    }

    public void setWhiteWins(long whiteWins) {
        this.whiteWins = whiteWins;
    }

    public long getWhiteDraws() {
        return whiteDraws;
    }

    public void setWhiteDraws(long whiteDraws) {
        this.whiteDraws = whiteDraws;
    }

    public long getBlackWins() {
        return blackWins;
    }

    public void setBlackWins(long blackWins) {
        this.blackWins = blackWins;
    }

    public long getBlackDraws() {
        return blackDraws;
    }

    public void setBlackDraws(long blackDraws) {
        this.blackDraws = blackDraws;
    }

    public int getEloDelta() { return eloDelta; }

    public void setEloDelta(int eloDelta) {
        this.eloDelta = eloDelta;
    }

    public int getRegElo() { return regElo; }

    public void setRegElo(int regElo) { this.regElo = regElo; }

    public long getTotalGames() {
        return totalGames;
    }

    public void setTotalGames(long totalGames) {
        this.totalGames = totalGames;
    }

    public double getpWhiteWin() {
        return pWhiteWin;
    }

    public void setpWhiteWin(double pWhiteWin) {
        this.pWhiteWin = pWhiteWin;
    }

    public double getpBlackWin() {
        return pBlackWin;
    }

    public void setpBlackWin(double pBlackWin) {
        this.pBlackWin = pBlackWin;
    }

    public double getpWhiteDraw() {
        return pWhiteDraw;
    }

    public void setpWhiteDraw(double pWhiteDraw) {
        this.pWhiteDraw = pWhiteDraw;
    }

    public double getpBlackDraw() {
        return pBlackDraw;
    }

    public void setpBlackDraw(double pBlackDraw) {
        this.pBlackDraw = pBlackDraw;
    }

    @Override
    public String toString() {
        String str = name + "\tElo=" + elo;
        return str;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Player player = (Player) o;
        return name.equals(player.name);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result;
        return result;
    }
}
