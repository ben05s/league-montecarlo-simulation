package at.hagenberg.master.montecarlo;

import at.hagenberg.master.montecarlo.entities.MatchResult;
import at.hagenberg.master.montecarlo.exceptions.PgnParserException;
import at.hagenberg.master.montecarlo.simulation.AbstractPredictionModel;
import at.hagenberg.master.montecarlo.simulation.HeadToHeadMatch;
import at.hagenberg.master.montecarlo.entities.Player;
import at.hagenberg.master.montecarlo.entities.Team;
import at.hagenberg.master.montecarlo.simulation.settings.LeagueSettings;
import at.hagenberg.master.montecarlo.util.EloRatingSystemUtil;
import com.supareno.pgnparser.PGNParser;
import com.supareno.pgnparser.jaxb.Game;
import com.supareno.pgnparser.jaxb.Games;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.log4j.Level;
import at.hagenberg.master.montecarlo.util.PgnUtil;

import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PgnAnalysis {

    private final int gamesPerMatch;
    private final int roundsPerSeason;

    private List<Game> seasonToSimulateGames = new ArrayList<>();
    private List<Game> historicalGames = new ArrayList<>();
    private Map<String, List<Game>> historicalGamesPerSeason = new HashMap<>();

    private  Map<String, Team> teamMap = new HashMap<>();
    private List<HeadToHeadMatch> gameResults = new ArrayList<>();
    private Map<Integer, List<HeadToHeadMatch>> roundGameResults = new HashMap<>();


    public PgnAnalysis(String fileContentSeasonToSimulate, String fileContentHistoricalSeasons, final int roundsPerSeason, final int gamesPerMatch) throws PgnParserException {
        this.gamesPerMatch = gamesPerMatch;
        this.roundsPerSeason = roundsPerSeason;

        PGNParser parser = new PGNParser(Level.ALL);

        Games games = parser.parseFile(new StringReader(fileContentSeasonToSimulate));
        if(games == null || games.getGame().isEmpty())
            throw new PgnParserException("no games found in pgn file for season to simulate");

        Games tmpGames = parser.parseFile(new StringReader(fileContentHistoricalSeasons));
        // remove incomplete games (game without result)
        tmpGames.getGame().removeIf(game -> PgnUtil.isInvalidGame(game));
        this.historicalGames.addAll(tmpGames.getGame());

        this.historicalGames.forEach(game -> {
            List<Game> gamesOfSeason = this.historicalGamesPerSeason.get(game.getEvent());
            if(gamesOfSeason == null) gamesOfSeason = new ArrayList<>();
            gamesOfSeason.add(game);
            this.historicalGamesPerSeason.put(game.getEvent(), gamesOfSeason);
        });

        processPgnFromSeasonToSimulate(games.getGame());
    }

    public PgnAnalysis(String fileSeasonToSimulate, List<String> fileHistoricalSeasons, final int roundsPerSeason, final int gamesPerMatch) throws PgnParserException {
        this.gamesPerMatch = gamesPerMatch;
        this.roundsPerSeason = roundsPerSeason;

        PGNParser parser = new PGNParser(Level.ALL);

        // TODO MINOR: count rounds per season and games per match from seasonToSimulate and set variables in MonteCarloSettings
        Games games = parser.parseFile(fileSeasonToSimulate);
        if(games == null || games.getGame().isEmpty())
            throw new PgnParserException("no games found in pgn file for season to simulate");
        this.seasonToSimulateGames = games.getGame();

        fileHistoricalSeasons.forEach(season -> {
            Games tmpGames = parser.parseFile(season);
            // remove incomplete games (game without result)
            tmpGames.getGame().removeIf(game -> PgnUtil.isInvalidGame(game));
            this.historicalGames.addAll(tmpGames.getGame());
            this.historicalGamesPerSeason.put(season, tmpGames.getGame());
        });

        processPgnFromSeasonToSimulate(games.getGame());

    }

    public void fillGamesFromSeasonToSimulate(RandomGenerator randomGenerator, AbstractPredictionModel predictionModel) throws PgnParserException {
        for (int i = 0; i < this.seasonToSimulateGames.size(); i++) {
            Game game = this.seasonToSimulateGames.get(i);

            // save actual game result for validation of chess prediction model
            Player white = getPlayerFromTeamMap(game.getWhite());
            Player black = getPlayerFromTeamMap(game.getBlack());
            MatchResult matchResult = new MatchResult(white, black, PgnUtil.getGameResult(game.getResult()));
            HeadToHeadMatch chessGame = new HeadToHeadMatch(randomGenerator, predictionModel, white, black, matchResult);
            this.gameResults.add(chessGame);

            int round = Integer.parseInt(game.getRound());
            List<HeadToHeadMatch> roundChessGames = this.roundGameResults.get(round);
            if(roundChessGames == null) roundChessGames = new ArrayList<>();

            roundChessGames.add(chessGame);
            this.roundGameResults.put(round, roundChessGames);
        }
    }

    private Player getPlayerFromTeamMap(String playerName) throws PgnParserException {
        for (Team team : this.teamMap.values()) {
            int idx = team.getPlayerList().indexOf(new Player(playerName));
            if(idx != -1) {
                return team.getPlayerList().get(idx);
            }
        }
        throw new PgnParserException("Player not found in team map: " + playerName);
    }

    public double calculateWhiteAdvantage() {
        if(this.historicalGames.isEmpty()) return 0.0;
        long whiteWins = this.historicalGames.stream().filter(game -> PgnUtil.isResult(game, PgnUtil.WHITE_WINS)).count();
        long blackWins = this.historicalGames.stream().filter(game -> PgnUtil.isResult(game, PgnUtil.BLACK_WINS)).count();
        return (double) whiteWins / (whiteWins + blackWins);
        //return whiteWins > blackWins ? (double) (whiteWins - blackWins) / this.historicalGames.size() : 0.0;
    }

    public double calculateOverallProbability(final String RESULT) {
        if(this.historicalGames.isEmpty()) return 0.0;
        long amount = this.historicalGames.stream().filter(game -> PgnUtil.isResult(game, RESULT)).count();
        return (double) amount / this.historicalGames.size();
    }

    private double calculateWeightedProbabilityWhite(final String RESULT, Player player) {
        long whiteGames = this.historicalGames.stream().filter(game -> PgnUtil.isWhite(game, player)).count();
        if(whiteGames == 0) return 0.0;

        long amount = this.historicalGames.stream().filter(game -> PgnUtil.isWhite(game, player) && PgnUtil.isResult(game, RESULT)).count();
        long playedGames = totalGames(player);
        long maxGames = 11 * this.historicalGamesPerSeason.size();

        return ((double) amount / whiteGames) * ((double) playedGames / maxGames);
    }

    private double calculateWeightedProbabilityBlack(final String RESULT, Player player) {
        long blackGames = this.historicalGames.stream().filter(game -> PgnUtil.isBlack(game, player)).count();
        if(blackGames == 0) return 0.0;

        long amount = this.historicalGames.stream().filter(game -> PgnUtil.isBlack(game, player) && PgnUtil.isResult(game, RESULT)).count();
        long playedGames = totalGames(player);
        long maxGames = 11 * this.historicalGamesPerSeason.size();

        return ((double) amount / blackGames) * ((double) playedGames / maxGames);
    }

    private long totalGames(Player player) {
        long games = this.historicalGames.stream().filter(game -> PgnUtil.isPlaying(game, player)).count();
        return games;
    }

    private long whiteCount(final String RESULT, Player player) {
        long whiteGames = this.historicalGames.stream().filter(game -> PgnUtil.isWhite(game, player) && PgnUtil.isResult(game, RESULT)).count();
        return whiteGames;
    }

    private long blackCount(final String RESULT, Player player) {
        long blackGames = this.historicalGames.stream().filter(game -> PgnUtil.isBlack(game, player) && PgnUtil.isResult(game, RESULT)).count();
        return blackGames;
    }

    private List<Double> calculateLineUpProbabilities(Player player) {
        final int gamesPerMatch = this.gamesPerMatch;
        long[] p = new long[gamesPerMatch];

        IntStream.range(0, gamesPerMatch).forEach(i -> {
            p[i] = this.historicalGames.stream().filter(game -> PgnUtil.isPlaying(game, player) && game.getBoard().equals(""+(i+1))).count();
        });
        long sumNominations = Arrays.stream(p).sum();

        long seasonsPlayed = this.historicalGamesPerSeason.entrySet().stream()
                .filter(e -> e.getValue().stream().filter(game -> PgnUtil.isPlaying(game, player)).count() != 0).count();
        // TODO MINOR: could be slightly improved by counting how many matches the team actually played in the pgn file
        // some pgn files are incomplete - not all match rounds are present
        long maxNominations = this.roundsPerSeason * seasonsPlayed;

        List<Double> lineup = new ArrayList<>(gamesPerMatch);
        for(int i = 0; i< gamesPerMatch; i++) {
            double l = 0.0;
            if(sumNominations > 0)
                l = ((double) sumNominations / maxNominations) * ((double) p[i] / sumNominations);
            lineup.add(new Double(l));
        }
        return lineup;
    }

    private void processPgnFromSeasonToSimulate(List<Game> games) throws PgnParserException {
        Map<String, Team> teamMap = aggregateTeamsFromGames(games);
        if(teamMap.size() < 2)
            throw new PgnParserException("not enough teams found in pgn file: at least 2 teams must be present.");
        for(Team team : teamMap.values()) {
            if(team.getPlayerList().size() < gamesPerMatch || team.getPlayerList().size() < gamesPerMatch)
                throw new PgnParserException("not enough players in team: " + team.getName() +
                        " players: " + team.getPlayerList().size() + " should be at least " + gamesPerMatch);

            team.setLineup(transposeLineupProbabilities(team.getPlayerList()));
        }
        this.teamMap = teamMap;
    }

    private Map<String, Team> aggregateTeamsFromGames(List<Game> games) {
        Map<String, Team> teamMap = new HashMap<>();

        for (int i = 0; i < games.size(); i++) {
            Game game = games.get(i);

            Player whitePlayer = new Player(game.getWhite());
            whitePlayer.setElo(Integer.parseInt(game.getWhiteElo()));

            Player blackPlayer = new Player(game.getBlack());
            blackPlayer.setElo(Integer.parseInt(game.getBlackElo()));

            Team whiteTeam = teamMap.get(game.getWhiteTeam());
            if(whiteTeam == null) {
                whiteTeam = new Team(game.getWhiteTeam());
            }

            addPlayerToTeam(whitePlayer, whiteTeam);
            teamMap.put(whiteTeam.getName(), whiteTeam);

            Team blackTeam = teamMap.get(game.getBlackTeam());
            if(blackTeam == null) {
                blackTeam = new Team(game.getBlackTeam());
            }

            addPlayerToTeam(blackPlayer, blackTeam);

            teamMap.put(blackTeam.getName(), blackTeam);
        }
        return teamMap;
    }

    private void addPlayerToTeam(Player player, Team team) {
        player.setTeam(team);

        if(!team.getPlayerList().contains(player)) {
            // only do this once (expensive operations)
            player.setEloDelta(EloRatingSystemUtil.eloDelta(player, this.historicalGames));
            player.setTotalGames(totalGames(player));

            player.setWhiteWins(whiteCount(PgnUtil.WHITE_WINS, player));
            player.setWhiteDraws(whiteCount(PgnUtil.DRAW, player));
            player.setWhiteLoss(whiteCount(PgnUtil.BLACK_WINS, player));

            player.setBlackWins(blackCount(PgnUtil.BLACK_WINS, player));
            player.setBlackDraws(blackCount(PgnUtil.DRAW, player));
            player.setBlackLoss((blackCount(PgnUtil.WHITE_WINS, player)));

            player.setpWhiteWin(calculateWeightedProbabilityWhite(PgnUtil.WHITE_WINS, player));
            player.setpWhiteDraw(calculateWeightedProbabilityWhite(PgnUtil.DRAW, player));
            player.setpWhiteLoss(calculateWeightedProbabilityWhite(PgnUtil.BLACK_WINS, player));

            player.setpBlackWin(calculateWeightedProbabilityBlack(PgnUtil.BLACK_WINS, player));
            player.setpBlackDraw(calculateWeightedProbabilityBlack(PgnUtil.DRAW, player));
            player.setpBlackLoss(calculateWeightedProbabilityBlack(PgnUtil.WHITE_WINS, player));

            player.setpLineUp(calculateLineUpProbabilities(player));

            team.addPlayer(player);
        }
    }

    /**
     * Arrange Lineup Probabilities 2-dimensional x-axis lineup probability of player, y-axis lineup position
     * e.g.: lineup probabilities of a team having 6 players
     * 	 Position 0:	(0) 0,0000		(1) 0,0404		(2) 0,0000		(3) 0,0000		(4) 0,0000		(5) 0,0000
     *   Position 1:	(0) 0,0000		(1) 0,2121		(2) 0,0000		(3) 0,0101		(4) 0,0000		(5) 0,0000
     *   Position 2:	(0) 0,0000		(1) 0,1717		(2) 0,0000		(3) 0,1414		(4) 0,0303		(5) 0,0000
     *   Position 3:	(0) 0,0808		(1) 0,0303		(2) 0,0000		(3) 0,2525		(4) 0,3434		(5) 0,0000
     *   Position 4:	(0) 0,0000		(1) 0,0000		(2) 0,0000		(3) 0,2020		(4) 0,0606		(5) 0,0000
     *   Position 5:	(0) 0,0000		(1) 0,0000		(2) 0,0000		(3) 0,0000		(4) 0,1111		(5) 0,0909
     * @param playerList
     * @return
     */
    public List<Map<Player, Double>> transposeLineupProbabilities(List<Player> playerList) {
        List<Map<Player, Double>> lineup = new ArrayList<>(gamesPerMatch);
        IntStream.range(0, gamesPerMatch).forEach((int i) -> {
            Map<Player, Double> pMap = playerList.stream()
                    .collect(
                            Collectors.toMap(
                                    player -> player, player -> player.getpLineUp().get(i),
                                    (oldValue, newValue) -> oldValue,
                                    LinkedHashMap::new)
                    );
            lineup.add(pMap);
        });
        return lineup;
    }

    public List<Team> getTeams() {
        return new ArrayList<>(this.teamMap.values());
    }

    public List<HeadToHeadMatch> getGames() {
        return this.gameResults;
    }

    public Map<Integer, List<HeadToHeadMatch>> getRoundGameResults() { return this.roundGameResults; }
}
