package at.hagenberg.master.montecarlo.simulation;

import at.hagenberg.master.montecarlo.PgnAnalysis;
import at.hagenberg.master.montecarlo.entities.Player;
import at.hagenberg.master.montecarlo.entities.Team;
import at.hagenberg.master.montecarlo.entities.enums.LineupStrategy;
import at.hagenberg.master.montecarlo.exceptions.PgnParserException;
import at.hagenberg.master.montecarlo.simulation.settings.LeagueSettings;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ChessLineupOptimizerTest {

    private final int gamesPerMatch = 6;
    private RandomGenerator randomGenerator;
    private PgnAnalysis analysis;
    private List<Team> teamList;
    private LineupSelector opt;

    @Before
    public void setUp() throws PgnParserException {
        String seasonToSimulate = "games/west/1516autchtwest.pgn";
        List<String> historicalSeasons = Arrays.asList(("games/west/0607autchtwest.pgn," +
                "games/west/0708autchtwest.pgn," +
                "games/west/0809autchtwest.pgn," +
                "games/west/0910autchtwest.pgn," +
                "games/west/1011autchtwest.pgn," +
                "games/west/1112autchtwest.pgn," +
                "games/west/1213autchtwest.pgn," +
                "games/west/1314autchtwest.pgn," +
                "games/west/1415autchtwest.pgn").split(","));
        final int roundsPerSeason = 11;
        analysis = new PgnAnalysis(seasonToSimulate, historicalSeasons, roundsPerSeason, gamesPerMatch);
        this.teamList = analysis.getTeams();
        this.randomGenerator = new Well19937c();
    }

    @Test
    public void testRandomLineup() {
        opt = new LineupSelector(LineupStrategy.RANDOM, gamesPerMatch);
        for (int i = 0; i < gamesPerMatch; i++) {
            Player selectedPlayer = opt.pickPlayerFromTeam(this.randomGenerator, i, this.teamList.get(0), this.teamList.get(1), true);
            assertTrue(selectedPlayer != null);
        }
    }

    @Test
    public void testProbableLineup() {
        opt = new LineupSelector(LineupStrategy.TRADITIONAL, gamesPerMatch);

        Team team = this.teamList.get(0);
        team.getPlayerList().forEach(player -> player.setpLineUp(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)));

        // player at position 1
        Player player = new Player("Player 1");
        player.setTeam(this.teamList.get(0));
        player.setElo(2000);
        player.setpLineUp(Arrays.asList(1.0, 0.0, 0.0, 0.0, 0.0, 0.0));
        team.addPlayer(player);
        team.setLineup(analysis.transposeLineupProbabilities(team.getPlayerList()));

        Player selectedPlayer = opt.pickPlayerFromTeam(this.randomGenerator, 0, team, this.teamList.get(1), true);
        assertSame(player, selectedPlayer);

        //Team teamToOptimizeLineup = this.settings.getTeamList().stream().filter(team -> "Rochade Rum".equals(team.getName())).findFirst().orElseThrow(() -> new ChessMonteCarloSimulationException("Team to optimize not found in given PGN file"));

    }

    @Test
    public void testAvoidStrongOpponentLineupStrategy() {

        Team opponentTeam = this.teamList.get(1);
        // strong opponent player at position 1
        Player strongOpponentPlayer = new Player("Strong Player");
        strongOpponentPlayer.setTeam(opponentTeam);
        strongOpponentPlayer.setElo(2600);
        strongOpponentPlayer.setpLineUp(Arrays.asList(1.0, 0.0, 0.0, 0.0, 0.0, 0.0));
        opponentTeam.addPlayer(strongOpponentPlayer);
        opponentTeam.setLineup(analysis.transposeLineupProbabilities(opponentTeam.getPlayerList()));


        Team teamToOptimizeLineup = this.teamList.get(0);
        // my weak player
        Player myWeakPlayer = new Player("Weak Player");
        myWeakPlayer.setTeam(teamToOptimizeLineup);
        myWeakPlayer.setElo(500);
        myWeakPlayer.setpLineUp(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
        teamToOptimizeLineup.addPlayer(myWeakPlayer);
        teamToOptimizeLineup.setLineup(analysis.transposeLineupProbabilities(teamToOptimizeLineup.getPlayerList()));

        opt = new LineupSelector(LineupStrategy.AVOID_STRONG_OPPONENTS, gamesPerMatch, teamToOptimizeLineup.getName());

        Player selectedPlayer = opt.pickPlayerFromTeam(this.randomGenerator, 0, teamToOptimizeLineup, opponentTeam, true);
        assertSame(myWeakPlayer, selectedPlayer);

        //selectedPlayer = opt.pickPlayerFromTeam(0, teamToOptimizeLineup, opponentTeam, false);
        assertSame(myWeakPlayer, selectedPlayer);
    }
}