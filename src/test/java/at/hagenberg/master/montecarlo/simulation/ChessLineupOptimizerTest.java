package at.hagenberg.master.montecarlo.simulation;

import at.hagenberg.master.montecarlo.PgnAnalysis;
import at.hagenberg.master.montecarlo.entities.Player;
import at.hagenberg.master.montecarlo.entities.Team;
import at.hagenberg.master.montecarlo.entities.enums.LineupStrategy;
import at.hagenberg.master.montecarlo.exceptions.ChessMonteCarloSimulationException;
import at.hagenberg.master.montecarlo.simulation.settings.ChessLineupSettings;
import at.hagenberg.master.montecarlo.simulation.settings.SeasonSettings;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ChessLineupOptimizerTest {

    private SeasonSettings seasonSettings;
    private RandomGenerator randomGenerator;
    private PgnAnalysis analysis;
    private List<Team> teamList;
    private ChessLineupSelector opt;

    @Before
    public void setUp() throws ChessMonteCarloSimulationException {
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
        this.seasonSettings = new SeasonSettings(11,6);
        this.analysis = new PgnAnalysis(seasonSettings, seasonToSimulate, historicalSeasons);
        this.teamList = analysis.getTeams();
        this.randomGenerator = new Well19937c();
    }

    @Test
    public void testRandomLineup() throws ChessMonteCarloSimulationException {
        opt = new ChessLineupSelector(this.randomGenerator, new ChessLineupSettings(LineupStrategy.RANDOM), seasonSettings);
        for (int i = 0; i < seasonSettings.getGamesPerMatch(); i++) {
            Player selectedPlayer = opt.pickPlayerFromTeam(i, this.teamList.get(0), this.teamList.get(1), true);
            assertTrue(selectedPlayer != null);
        }
    }

    @Test
    public void testProbableLineup() throws ChessMonteCarloSimulationException {
        opt = new ChessLineupSelector(this.randomGenerator, new ChessLineupSettings(LineupStrategy.TRADITIONAL), seasonSettings);

        Team team = this.teamList.get(0);
        team.getPlayerList().forEach(player -> player.setpLineUp(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)));

        // player at position 1
        Player player = new Player();
        player.setName("Player 1");
        player.setTeam(this.teamList.get(0));
        player.setElo(2000);
        player.setpLineUp(Arrays.asList(1.0, 0.0, 0.0, 0.0, 0.0, 0.0));
        team.addPlayer(player);

        Player selectedPlayer = opt.pickPlayerFromTeam(0, team, this.teamList.get(1), true);
        assertSame(player, selectedPlayer);

        //Team teamToOptimizeLineup = this.settings.getTeamList().stream().filter(team -> "Rochade Rum".equals(team.getName())).findFirst().orElseThrow(() -> new ChessMonteCarloSimulationException("Team to optimize not found in given PGN file"));

    }

    @Test
    public void testAvoidStrongOpponentLineupStrategy() throws ChessMonteCarloSimulationException {

        Team opponentTeam = this.teamList.get(1);
        // strong opponent player at position 1
        Player strongOpponentPlayer = new Player();
        strongOpponentPlayer.setName("Strong Player");
        strongOpponentPlayer.setTeam(opponentTeam);
        strongOpponentPlayer.setElo(2600);
        strongOpponentPlayer.setpLineUp(Arrays.asList(1.0, 0.0, 0.0, 0.0, 0.0, 0.0));
        opponentTeam.addPlayer(strongOpponentPlayer);

        Team teamToOptimizeLineup = this.teamList.get(0);
        // my weak player
        Player myWeakPlayer = new Player();
        myWeakPlayer.setName("Weak Player");
        myWeakPlayer.setTeam(teamToOptimizeLineup);
        myWeakPlayer.setElo(500);
        myWeakPlayer.setpLineUp(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0));

        teamToOptimizeLineup.addPlayer(myWeakPlayer);

        opt = new ChessLineupSelector(this.randomGenerator, new ChessLineupSettings(LineupStrategy.AVOID_STRONG_OPPONENTS, teamToOptimizeLineup.getName()), seasonSettings);

        Player selectedPlayer = opt.pickPlayerFromTeam(0, teamToOptimizeLineup, opponentTeam, true);
        assertSame(myWeakPlayer, selectedPlayer);

        //selectedPlayer = opt.pickPlayerFromTeam(0, teamToOptimizeLineup, opponentTeam, false);
        assertSame(myWeakPlayer, selectedPlayer);
    }
}