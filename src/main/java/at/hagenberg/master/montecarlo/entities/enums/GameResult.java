package at.hagenberg.master.montecarlo.entities.enums;

public enum GameResult {
    WHITE(1), DRAW(0.5), BLACK(0);

    private final double points;

    GameResult(double points) {
        this.points = points;
    }

    public double getValue() {
        return this.points;
    }
}
