package at.hagenberg.master.montecarlo.simulation.settings;

import at.hagenberg.master.montecarlo.entities.Team;
import at.hagenberg.master.montecarlo.entities.enums.LineupStrategy;

public class ChessLineupSettings {

    private LineupStrategy lineupStrategy;

    private String optimizeLineupTeamName;

    public int whiteBlackPerformanceStrategyLineupInfluenceFactor = 4;
    public int playerPerformanceAndEloLineupInfluenceFactor = 2;

    public ChessLineupSettings() {
        this(LineupStrategy.DESCENDING_RATING_STRENGTH, null);
    }

    public ChessLineupSettings(LineupStrategy lineupStrategy) {
        this(lineupStrategy, null);
    }

    public ChessLineupSettings(String optimizeLineupTeamName) {
        this(LineupStrategy.DESCENDING_RATING_STRENGTH, optimizeLineupTeamName);
    }

    public ChessLineupSettings(int lineupStrategyId, String optimizeLineupTeamName) {
        this(LineupStrategy.valueOf(lineupStrategyId), optimizeLineupTeamName);
    }

    public ChessLineupSettings(LineupStrategy lineupStrategy, String optimizeLineupTeamName) {
        this.lineupStrategy = lineupStrategy;
        this.optimizeLineupTeamName = optimizeLineupTeamName;
    }

    @Override
    public String toString() {
        return "ChessLineupSettings{" +
                "lineupStrategy=" + lineupStrategy +
                ", optimizeLineupTeamName=" + optimizeLineupTeamName +
                ", whiteBlackPerformanceStrategyLineupInfluenceFactor=" + whiteBlackPerformanceStrategyLineupInfluenceFactor +
                ", playerPerformanceAndEloLineupInfluenceFactor=" + playerPerformanceAndEloLineupInfluenceFactor +
                "}";
    }

    public LineupStrategy getLineupStrategy() { return lineupStrategy; }

    public String getOptimizeLineupTeamName() { return optimizeLineupTeamName; }
}
