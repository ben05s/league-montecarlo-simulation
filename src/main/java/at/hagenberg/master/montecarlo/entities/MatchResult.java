package at.hagenberg.master.montecarlo.entities;

import at.hagenberg.master.montecarlo.entities.enums.GameResult;
import at.hagenberg.master.montecarlo.simulation.ChessGame;
import com.supareno.pgnparser.jaxb.Game;

import java.util.ArrayList;
import java.util.List;

import static at.hagenberg.master.montecarlo.entities.enums.GameResult.BLACK;
import static at.hagenberg.master.montecarlo.entities.enums.GameResult.DRAW;
import static at.hagenberg.master.montecarlo.entities.enums.GameResult.WHITE;

public class MatchResult {

    private Team teamA;
    private Team teamB;
    private double scoreTeamA;
    private double scoreTeamB;
    private Team winner;

    private List<ChessGame> games = new ArrayList<>();

    public MatchResult(Team teamA, Team teamB) {
        this.teamA = teamA;
        this.teamB = teamB;
    }

    public void addGame(ChessGame game, boolean isPrediction) {
        this.games.add(game);

        GameResult gameResult = game.getResult();
        if(isPrediction && game.getPrediction() != null) gameResult = game.getPrediction();

        if((WHITE.equals(gameResult) && game.getWhite().getTeam().equals(teamA))
                || (BLACK.equals(gameResult) && game.getBlack().getTeam().equals(teamA))) {
            this.addWinTeamA();
        } else if((WHITE.equals(gameResult) && game.getWhite().getTeam().equals(teamB))
                || (BLACK.equals(gameResult) && game.getBlack().getTeam().equals(teamB))) {
            this.addWinTeamB();
        } else if(DRAW.equals(gameResult)) {
            this.addDraw();
        }

        this.determineWinner();
    }

    private void addWinTeamA() {
        this.scoreTeamA += 1;
    }

    private void addWinTeamB() {
        this.scoreTeamB += 1;
    }

    private void addDraw() {
        this.scoreTeamA += 0.5;
        this.scoreTeamB += 0.5;
    }

    private void determineWinner() {
        if(this.scoreTeamA != this.scoreTeamB) {
            this.winner = this.scoreTeamA > this.scoreTeamB ? teamA : teamB;
        }
    }

    public Team getTeamA() {
        return teamA;
    }

    public Team getTeamB() {
        return teamB;
    }

    public double getScoreTeamA() {
        return scoreTeamA;
    }

    public double getScoreTeamB() {
        return scoreTeamB;
    }

    public Team getWinner() {
        return winner;
    }

    public List<ChessGame> getGames() { return games; }

    public String print() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.teamA.getName()).append(",").append(this.scoreTeamA).append(",");
        sb.append(this.teamB.getName()).append(",").append(this.scoreTeamB).append(",");
        sb.append(this.winner != null ? this.winner.getName() : "Draw").append("\n");
        return sb.toString();
    }
}
