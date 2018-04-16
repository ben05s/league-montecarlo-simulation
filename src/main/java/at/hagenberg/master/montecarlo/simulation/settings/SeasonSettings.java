package at.hagenberg.master.montecarlo.simulation.settings;

public class SeasonSettings {
// TODO MAJOR make this somehow public accessible - singleton? shared
    private final int roundsPerSeason;
    private final int gamesPerMatch;
    private final int roundsToSimulate;

    public SeasonSettings(int roundsPerSeason, int gamesPerMatch) {
        this(roundsPerSeason, gamesPerMatch, roundsPerSeason);
    }

    public SeasonSettings(int roundsPerSeason, int gamesPerMatch, int roundsToSimulate) {
        this.roundsPerSeason = roundsPerSeason;
        this.gamesPerMatch = gamesPerMatch;
        this.roundsToSimulate = roundsToSimulate;
    }

    public int getRoundsPerSeason() {
        return roundsPerSeason;
    }

    public int getGamesPerMatch() {
        return gamesPerMatch;
    }

    public int getRoundsToSimulate() {
        return roundsToSimulate;
    }

    public int getPlayedRounds() {
        return roundsPerSeason - roundsToSimulate;
    }

    @Override
    public String toString() {
        return "SeasonSettings{" +
                "roundsPerSeason=" + roundsPerSeason +
                ", gamesPerMatch=" + gamesPerMatch +
                ", roundsToSimulate=" + roundsToSimulate +
                '}';
    }
}
