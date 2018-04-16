package at.hagenberg.master.montecarlo.entities;

public class SeasonScore implements Comparable<SeasonScore> {
    private int seasonPoints = 0;
    private double pointsScored = 0;
    private double pointsConceded = 0;

    public SeasonScore() {
        this(0,0,0);
    }

    public SeasonScore(int seasonPoints, int pointsScored, int pointsConceded) {
        this.seasonPoints = seasonPoints;
        this.pointsScored = pointsScored;
        this.pointsConceded = pointsConceded;
    }

    public void addWin() { this.seasonPoints += 2; }

    public void addDraw() { this.seasonPoints += 1; }

    public void addPointsScored(double score) { this.pointsScored += score; }

    public void addPointsConceded(double score) {
        this.pointsConceded += score;
    }

    public double getPointsScored() {
        return pointsScored;
    }

    public double getPointsConceded() {
        return pointsConceded;
    }

    public double getSeasonPoints() { return seasonPoints; }

    @Override
    public int compareTo(SeasonScore o) {
        if (this.seasonPoints < o.seasonPoints) {
            return -1;
        } else if (this.seasonPoints == o.seasonPoints) {
            if (this.pointsScored < o.pointsScored) {
                return -1;
            } else if (this.pointsScored == o.pointsScored) {
                return 0; // ex-equo
            } else {
                return 1;
            }
        } else {
            return 1;
        }
    }

    @Override
    public String toString() {
        return this.seasonPoints + "(" + this.pointsScored + ":" + this.pointsConceded + ")";
    }
}
