package at.hagenberg.master.montecarlo.simulation.settings;

import at.hagenberg.master.montecarlo.entities.Opponent;
import at.hagenberg.master.montecarlo.entities.Team;
import at.hagenberg.master.montecarlo.entities.enums.LineupStrategy;
import at.hagenberg.master.montecarlo.simulation.*;
import at.hagenberg.master.montecarlo.util.EloRatingSystemUtil;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChessLeagueSettings extends LeagueSettings<Team, HeadToHeadMatch> {

    private final int gamesPerMatch;
    private LineupSelector lineupSelector;

    public ChessLeagueSettings(ChessPredictionModel predictionModel, List<Team> opponentList, Map<Integer, List<HeadToHeadMatch>> roundGameResults,
                          final int roundsPerSeason, final int roundsToSimulate, final int gamesPerMatch, LineupSelector lineupSelector) {
        super(predictionModel, opponentList, roundGameResults, roundsPerSeason, roundsToSimulate);

        if(gamesPerMatch <= 0)
            throw new IllegalArgumentException("Games per Match must be a number higher than 0");
        Objects.requireNonNull(lineupSelector);

        this.gamesPerMatch = gamesPerMatch;
        this.lineupSelector = lineupSelector;

        if(predictionModel.useRegularization)
            this.opponentList = EloRatingSystemUtil.regularizePlayerRatingsForTeams(opponentList, predictionModel.getAvgElo(), predictionModel.regularizeThreshold, predictionModel.regularizeFraction);
    }

    public int getGamesPerMatch() { return gamesPerMatch; }

    public LineupSelector getLineupSelector() { return lineupSelector; }

}
