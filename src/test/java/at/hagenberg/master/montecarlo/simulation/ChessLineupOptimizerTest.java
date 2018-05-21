package at.hagenberg.master.montecarlo.simulation;

import at.hagenberg.master.montecarlo.lineup.LineupSelector;
import at.hagenberg.master.montecarlo.parser.PgnAnalysis;
import at.hagenberg.master.montecarlo.entities.Player;
import at.hagenberg.master.montecarlo.entities.Team;
import at.hagenberg.master.montecarlo.entities.enums.LineupStrategy;
import at.hagenberg.master.montecarlo.exceptions.PgnParserException;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
        String division = "west";
        String file = "1516autchtwest.pgn";

        String seasonToSimulate = null;
        String historicalSeasons = null;
        try {
            seasonToSimulate = new String(Files.readAllBytes(Paths.get("games/" + division + "/" + file)));
            historicalSeasons = new String(Files.readAllBytes(Paths.get("games/" + division + "/historicData" + file)));
        } catch (IOException e) {
            e.printStackTrace();
        }

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
        player.setTeamName(team.getName());
        player.setElo(2000);
        player.setpLineUp(Arrays.asList(1.0, 0.0, 0.0, 0.0, 0.0, 0.0));
        team.addPlayer(player);
        team.setLineup(analysis.transposeLineupProbabilities(team.getPlayerList(), gamesPerMatch));

        Player selectedPlayer = opt.pickPlayerFromTeam(this.randomGenerator, 0, team, this.teamList.get(1), true);
        assertSame(player, selectedPlayer);

        //Team teamToOptimizeLineup = this.settings.getTeamList().stream().filter(team -> "Rochade Rum".equals(team.getName())).findFirst().orElseThrow(() -> new ChessMonteCarloSimulationException("Team to optimize not found in given PGN file"));

    }

    @Test
    public void testAvoidStrongOpponentLineupStrategy() {

        Team opponentTeam = this.teamList.get(1);
        // strong opponent player at position 1
        Player strongOpponentPlayer = new Player("Strong Player");
        strongOpponentPlayer.setTeamName(opponentTeam.getName());
        strongOpponentPlayer.setElo(2600);
        strongOpponentPlayer.setpLineUp(Arrays.asList(1.0, 0.0, 0.0, 0.0, 0.0, 0.0));
        opponentTeam.addPlayer(strongOpponentPlayer);
        opponentTeam.setLineup(analysis.transposeLineupProbabilities(opponentTeam.getPlayerList(), gamesPerMatch));


        Team teamToOptimizeLineup = this.teamList.get(0);
        // my weak player
        Player myWeakPlayer = new Player("Weak Player");
        myWeakPlayer.setTeamName(teamToOptimizeLineup.getName());
        myWeakPlayer.setElo(500);
        myWeakPlayer.setpLineUp(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
        teamToOptimizeLineup.addPlayer(myWeakPlayer);
        teamToOptimizeLineup.setLineup(analysis.transposeLineupProbabilities(teamToOptimizeLineup.getPlayerList(), gamesPerMatch));

        opt = new LineupSelector(LineupStrategy.AVOID_STRONG_OPPONENTS, gamesPerMatch, teamToOptimizeLineup.getName());

        Player selectedPlayer = opt.pickPlayerFromTeam(this.randomGenerator, 0, teamToOptimizeLineup, opponentTeam, true);
        assertSame(myWeakPlayer, selectedPlayer);

        //selectedPlayer = opt.pickPlayerFromTeam(0, teamToOptimizeLineup, opponentTeam, false);
        assertSame(myWeakPlayer, selectedPlayer);
    }
}