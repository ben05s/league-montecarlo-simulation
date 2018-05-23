package at.hagenberg.master.montecarlo.lineup;

import java.util.Objects;

public class OptimizedLineup {

    private String teamName;
    private AbstractLineupSelector lineupSelector;

    public OptimizedLineup(String teamName, AbstractLineupSelector lineupSelector) {
        Objects.requireNonNull(teamName);
        Objects.requireNonNull(lineupSelector);

        this.teamName = teamName;
        this.lineupSelector = lineupSelector;
    }

    public String getTeamName() {
        return teamName;
    }

    public AbstractLineupSelector getLineupSelector() {
        return lineupSelector;
    }
}
