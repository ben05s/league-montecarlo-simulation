package at.hagenberg.master.montecarlo.entities;

import at.hagenberg.master.montecarlo.entities.enums.GameResult;
import at.hagenberg.master.montecarlo.simulation.HeadToHeadMatch;

import java.util.ArrayList;
import java.util.List;

import static at.hagenberg.master.montecarlo.entities.enums.GameResult.BLACK;
import static at.hagenberg.master.montecarlo.entities.enums.GameResult.DRAW;
import static at.hagenberg.master.montecarlo.entities.enums.GameResult.WHITE;

public class MatchResult {

    private Opponent opponentA;
    private Opponent opponentB;
    private double scoreA;
    private double scoreB;

    private Opponent winner;

    private List<HeadToHeadMatch> headToHeadMatches = new ArrayList<>();

    public MatchResult(Opponent opponentA, Opponent opponentB) {
        this.opponentA = opponentA;
        this.opponentB = opponentB;
    }

    public MatchResult(Opponent opponentA, Opponent teamB, GameResult result) {
        this.opponentA = opponentA;
        this.opponentB = teamB;
        if(DRAW.getValue() == result.getValue()) {
            this.addDraw();
        } else if(WHITE.getValue() == result.getValue()) {
            this.addWinTeamA();
        } else if(BLACK.getValue() == result.getValue()) {
            this.addWinTeamB();
        }
        this.determineWinner();
    }

    public void addGame(HeadToHeadMatch game, boolean isPrediction) {
        this.headToHeadMatches.add(game);

        MatchResult matchResult = game.getMatchResult();
        if(isPrediction && game.getMatchPrediction() != null) matchResult = game.getMatchPrediction();

        if(matchResult.getWinner() == null) {
            this.addDraw();
        } else if((matchResult.getWinner().equals(game.getOpponentA())  && game.getOpponentA().getTeam().equals(opponentA))
                || (matchResult.getWinner().equals(game.getOpponentB()) && game.getOpponentB().getTeam().equals(opponentA))) {
            this.addWinTeamA();
        } else if((matchResult.getWinner().equals(game.getOpponentA()) && game.getOpponentA().getTeam().equals(opponentB))
                || (matchResult.getWinner().equals(game.getOpponentB()) && game.getOpponentB().getTeam().equals(opponentB))) {
            this.addWinTeamB();
        }

        this.determineWinner();
    }

    private void addWinTeamA() {
        this.scoreA += 1;
    }

    private void addWinTeamB() {
        this.scoreB += 1;
    }

    private void addDraw() {
        this.scoreA += 0.5;
        this.scoreB += 0.5;
    }

    private void determineWinner() {
        if(this.scoreA != this.scoreB) {
            this.winner = this.scoreA > this.scoreB ? opponentA : opponentB;
        }
    }

    public double getAbsoluteScore() {
        return Math.abs(this.scoreA - this.scoreB);
    }

    public Opponent getOpponentA() {
        return opponentA;
    }

    public Opponent getOpponentB() {
        return opponentB;
    }

    public double getScoreA() {
        return scoreA;
    }

    public double getScoreB() {
        return scoreB;
    }

    public Opponent getWinner() {
        return winner;
    }

    public List<HeadToHeadMatch> getHeadToHeadMatches() { return headToHeadMatches; }

    public String print() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.opponentA.getName()).append(",").append(this.scoreA).append(",");
        sb.append(this.opponentB.getName()).append(",").append(this.scoreB).append(",");
        sb.append(this.winner != null ? this.winner.getName() : "Draw").append("\n");
        return sb.toString();
    }
}
