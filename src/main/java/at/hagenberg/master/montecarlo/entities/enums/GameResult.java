package at.hagenberg.master.montecarlo.entities.enums;

import java.util.HashMap;
import java.util.Map;

public enum GameResult {
    WHITE(1.0), DRAW(0.5), BLACK(0.0);

    private final Double points;

    private static Map<Double, GameResult> map = new HashMap<>();

    static {
        for (GameResult e : GameResult.values()) {
            map.put(e.points, e);
        }
    }

    GameResult(Double points) {
        this.points = points;
    }

    public double getValue() {
        return this.points;
    }

    public static GameResult valueOf(Double points) {
        return map.get(points);
    }
}
