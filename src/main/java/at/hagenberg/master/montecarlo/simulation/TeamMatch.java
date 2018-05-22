package at.hagenberg.master.montecarlo.simulation;

import at.hagenberg.master.montecarlo.entities.*;
import at.hagenberg.master.montecarlo.lineup.LineupSelector;
import at.hagenberg.master.montecarlo.prediction.PredictionModel;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TeamMatch extends Match<Team> {

    private final int gamesPerMatch;
    private List<HeadToHeadMatch> headToHeadMatches = new ArrayList<>();

    public TeamMatch(RandomGenerator randomGenerator, PredictionModel predictionModel, Team teamA, Team teamB, final int gamesPerMatch) {
        super(randomGenerator, predictionModel, teamA, teamB);
        this.gamesPerMatch = gamesPerMatch;
    }

    @Override
    public MatchResult playMatch() {
        if(this.headToHeadMatches.isEmpty()) {
            for (int i = 0; i < this.gamesPerMatch; i++) {
                Player white = this.getOpponentA().getPlayerList().get(i);
                Player black = this.getOpponentB().getPlayerList().get(i);
                this.headToHeadMatches.add(new HeadToHeadMatch(randomGenerator, this.getPredictionModel(), white, black));
            }
        }
        MatchResult matchResult = new MatchResult(opponentA, opponentB);
        for (int i = 0; i < headToHeadMatches.size(); i++) {
            HeadToHeadMatch game = headToHeadMatches.get(i);
            game.playMatch();
            matchResult.addGame(game, true);
        }
        matchPrediction = matchResult;
        return matchResult;
    }

    @Override
    public String print() { return ""; }

    public void setHeadToHeadMatches(List<HeadToHeadMatch> headToHeadMatches) {
        this.headToHeadMatches = headToHeadMatches;
    }
}
