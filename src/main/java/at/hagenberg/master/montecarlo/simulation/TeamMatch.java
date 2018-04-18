package at.hagenberg.master.montecarlo.simulation;

import at.hagenberg.master.montecarlo.entities.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.ArrayList;
import java.util.List;

public class TeamMatch extends Match<Team> {

    private List<HeadToHeadMatch> headToHeadMatches = new ArrayList<>();

    public TeamMatch(RandomGenerator randomGenerator, AbstractPredictionModel predictionModel, Team teamA, Team teamB) {
        super(randomGenerator, predictionModel, teamA, teamB);
    }

    @Override
    public MatchResult playMatch() {
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

    public void addHeadToHeadMatchToSimulate(HeadToHeadMatch match) {
        this.headToHeadMatches.add(match);
    }

    public void setHeadToHeadMatches(List<HeadToHeadMatch> headToHeadMatches) {
        this.headToHeadMatches = headToHeadMatches;
    }
}
