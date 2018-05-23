package at.hagenberg.master.montecarlo.entities.enums;

import at.hagenberg.master.montecarlo.lineup.AbstractLineupSelector;
import at.hagenberg.master.montecarlo.lineup.AscendingRatingSelection;
import at.hagenberg.master.montecarlo.lineup.DescendingRatingSelection;
import at.hagenberg.master.montecarlo.lineup.RandomSelection;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;

import java.util.HashMap;
import java.util.Map;

public enum LineupStrategy {
    DESCENDING_RATING_STRENGTH(1), ASCENDING_RATING_STRENGTH(2), RANDOM(3), AVOID_STRONG_OPPONENTS(-1), MATCH_STRONG_OPPONENTS(-1), WHITE_BLACK_PERFORMANCE(-1), TRADITIONAL(-1);

    private final int id;

    LineupStrategy(int id) {
        this.id = id;
    }

    public int getId() { return this.id; }

    public static AbstractLineupSelector getLineupSelector(int id, RandomGenerator randomGenerator, final int gamesPerMatch) throws Exception {
        if(id == 1) {
            return new DescendingRatingSelection(randomGenerator, gamesPerMatch);
        } else if(id == 2) {
            return new AscendingRatingSelection(randomGenerator, gamesPerMatch);
        } else if(id == 3) {
            return new RandomSelection(randomGenerator, gamesPerMatch, false);
        }
        throw new Exception("Unknown Lineup Strategy with ID=" + id);
    }
}
