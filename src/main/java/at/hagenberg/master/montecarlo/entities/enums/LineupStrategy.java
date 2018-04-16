package at.hagenberg.master.montecarlo.entities.enums;

import java.util.HashMap;
import java.util.Map;

public enum LineupStrategy {
    AVOID_STRONG_OPPONENTS(1), MATCH_STRONG_OPPONENTS(2), WHITE_BLACK_PERFORMANCE(3), DESCENDING_RATING_STRENGTH(4), ASCENDING_RATING_STRENGTH(5), RANDOM(6), TRADITIONAL(7);

    private final int id;

    private static Map<Integer, LineupStrategy> map = new HashMap<>();

    static {
        for (LineupStrategy e : LineupStrategy.values()) {
            map.put(e.id, e);
        }
    }

    LineupStrategy(int id) {
        this.id = id;
    }

    public int getId() { return this.id; }

    public static LineupStrategy valueOf(int id) {
        return map.get(id);
    }
}
