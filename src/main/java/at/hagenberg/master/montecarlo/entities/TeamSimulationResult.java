package at.hagenberg.master.montecarlo.entities;

import java.lang.Comparable;

public class TeamSimulationResult implements Comparable<TeamSimulationResult> {
    String teamName;
    int totalSeasons = 0;
    int totalPoints = 0;
    double totalScore = 0;
    int totalPromotions = 0;
    int totalRelegations = 0;
    double ratioPromotion = 0.0;

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public void setRatioPromotion(double ratioPromotion) {
        this.ratioPromotion = ratioPromotion;
    }

    public void setRatioRelegation(double ratioRelegation) {
        this.ratioRelegation = ratioRelegation;
    }

    double ratioRelegation = 0.0;
    double highestSeasonPoints = 0.0;
    double lowestSeasonPoints = 0.0;
    double highestSeasonScore = 0.0;
    double lowestSeasonScore = 0.0;

    int iterationStart = 0;
    int iterationEnd = 0;

    public TeamSimulationResult(String teamName, int totalSeasons) {
        this.teamName = teamName;
        this.totalSeasons = totalSeasons;
    }

    public TeamSimulationResult() {
    }

    @Override
    public boolean equals(Object ts) {
        return this.teamName == ((TeamSimulationResult) ts).getTeamName();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.teamName != null ? this.teamName.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(TeamSimulationResult o) {
        if (this.totalPromotions > o.totalPromotions) {
            return -1;
        } else if (this.totalPromotions == o.totalPromotions) {
            if (this.totalRelegations < o.totalRelegations) {
                return -1;
            } else if (this.totalRelegations == o.totalRelegations) {
                return 0; // ex-equo
            } else {
                return 1;
            }
        } else {
            return 1;
        }
    }

    public void aggregate(TeamSimulationResult res) {
        this.highestSeasonPoints = res.getHighestSeasonPoints();
        this.lowestSeasonPoints = res.getLowestSeasonPoints();
        this.highestSeasonScore = res.getHighestSeasonScore();
        this.lowestSeasonScore = res.getLowestSeasonScore();

        this.totalPoints += res.getTotalPoints();
        this.totalScore += res.getTotalScore();
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

    public void addPoints(int points) {
        this.totalPoints += points;
        if (points > this.highestSeasonPoints) {
            this.highestSeasonPoints = points;
        }
        if (this.lowestSeasonPoints == 0.0) {
            this.lowestSeasonPoints = points;
        }
        if (points < this.lowestSeasonPoints) {
            this.lowestSeasonPoints = points;
        }
    }

    public void addScore(double score) {
        this.totalScore += score;
        if (score > this.highestSeasonScore) {
            this.highestSeasonScore = score;
        }
        if (this.lowestSeasonScore == 0.0) {
            this.lowestSeasonScore = score;
        }
        if (score < this.lowestSeasonScore) {
            this.lowestSeasonScore = score;
        }
    }

    public String getTeamName() {
        return teamName;
    }

    public int getTotalSeasons() {
        return totalSeasons;
    }

    public double getTotalScore() {
        return totalScore;
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

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public double getHighestSeasonPoints() {
        return highestSeasonPoints;
    }

    public void setHighestSeasonPoints(double highestSeasonPoints) {
        this.highestSeasonPoints = highestSeasonPoints;
    }

    public double getLowestSeasonPoints() {
        return lowestSeasonPoints;
    }

    public void setLowestSeasonPoints(double lowestSeasonPoints) {
        this.lowestSeasonPoints = lowestSeasonPoints;
    }
}
