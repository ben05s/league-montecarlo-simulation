package at.hagenberg.master.montecarlo.entities;

import java.lang.Comparable;
//TODO team season score
public class TeamResult implements Comparable<TeamResult> {
    String teamName;
    int totalSeasons = 0;
    double totalPoints = 0;
    int totalPromotions = 0;
    int totalRelegations = 0;
    double ratioPromotion = 0.0;
    double ratioRelegation = 0.0;
    double highestSeasonScore = 0.0;
    double lowestSeasonScore = 0.0;

    int iterationStart = 0;
    int iterationEnd = 0;

    public TeamResult(String teamName, int totalSeasons) {
        this.teamName = teamName;
        this.totalSeasons = totalSeasons;
    }

    public TeamResult() {
    }

    @Override
    public boolean equals(Object ts) {
        return this.teamName == ((TeamResult) ts).getTeamName();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.teamName != null ? this.teamName.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(TeamResult o) {
        if (this.totalPromotions > o.totalPromotions)
            return -1;
        else if (this.totalPromotions == o.totalPromotions)
            return 0;
        else
            return 1;
    }

    public void aggregate(TeamResult res) {
        this.highestSeasonScore = res.getHighestSeasonScore();
        this.lowestSeasonScore = res.getLowestSeasonScore();
        this.totalPoints += res.getTotalPoints();
        this.totalPromotions += res.getTotalPromotions();
        this.totalRelegations += res.getTotalRelegations();
        this.totalSeasons += res.getTotalSeasons();
        this.ratioPromotion = (double) this.totalPromotions / (double) this.totalSeasons;
        this.ratioRelegation = (double) this.totalRelegations / (double) this.totalSeasons;
    }

    public void addPromotion() {
        this.totalPromotions++;
        this.ratioPromotion = (double) this.totalPromotions / (double) this.totalSeasons;
    }

    public void addRelegation() {
        this.totalRelegations++;
        this.ratioRelegation = (double) this.totalRelegations / (double) this.totalSeasons;
    }

    public void addPointsScored(double pointsScored) {
        this.totalPoints += pointsScored;
        if (pointsScored > this.highestSeasonScore) {
            this.highestSeasonScore = pointsScored;
        }
        if (this.lowestSeasonScore == 0.0) {
            this.lowestSeasonScore = pointsScored;
        }
        if (pointsScored < this.lowestSeasonScore) {
            this.lowestSeasonScore = pointsScored;
        }
    }

    public String getTeamName() {
        return teamName;
    }

    public int getTotalSeasons() {
        return totalSeasons;
    }

    public double getTotalPoints() {
        return totalPoints;
    }

    public int getTotalPromotions() {
        return totalPromotions;
    }

    public int getTotalRelegations() {
        return totalRelegations;
    }

    public double getRatioPromotion() {
        return ratioPromotion;
    }

    public double getRatioRelegation() {
        return ratioRelegation;
    }

    public double getHighestSeasonScore() {
        return highestSeasonScore;
    }

    public double getLowestSeasonScore() {
        return lowestSeasonScore;
    }

    public int getIterationStart() {
        return iterationStart;
    }

    public void setIterationStart(int iterationStart) {
        this.iterationStart = iterationStart;
    }

    public int getIterationEnd() {
        return iterationEnd;
    }

    public void setIterationEnd(int iterationEnd) {
        this.iterationEnd = iterationEnd;
    }
}
