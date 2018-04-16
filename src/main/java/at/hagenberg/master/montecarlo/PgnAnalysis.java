package at.hagenberg.master.montecarlo;

import at.hagenberg.master.montecarlo.simulation.ChessGame;
import at.hagenberg.master.montecarlo.exceptions.ChessMonteCarloSimulationException;
import at.hagenberg.master.montecarlo.entities.Player;
import at.hagenberg.master.montecarlo.entities.Team;
import at.hagenberg.master.montecarlo.simulation.settings.SeasonSettings;
import com.supareno.pgnparser.PGNParser;
import com.supareno.pgnparser.jaxb.Game;
import com.supareno.pgnparser.jaxb.Games;
import org.apache.log4j.Level;
import at.hagenberg.master.montecarlo.util.PgnUtil;

import java.io.StringReader;
import java.util.*;
import java.util.stream.IntStream;

public class PgnAnalysis {

    private SeasonSettings seasonSettings;

    private List<Game> historicalGames = new ArrayList<>();
    private Map<String, List<Game>> historicalGamesPerSeason = new HashMap<>();

    private  Map<String, Team> teamMap = new HashMap<>();
    private List<ChessGame> gameResults = new ArrayList<>();
    private Map<Integer, List<ChessGame>> roundGameResults = new HashMap<>();


    public PgnAnalysis(SeasonSettings seasonSettings, String fileContentSeasonToSimulate, String fileContentHistoricalSeasons) throws ChessMonteCarloSimulationException {
        this.seasonSettings = seasonSettings;

        PGNParser parser = new PGNParser(Level.ALL);

        Games games = parser.parseFile(new StringReader(fileContentSeasonToSimulate));
        if(games == null || games.getGame().isEmpty())
            throw new ChessMonteCarloSimulationException("no games found in pgn file for season to simulate");

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

    public PgnAnalysis(SeasonSettings seasonSettings, String fileSeasonToSimulate, List<String> fileHistoricalSeasons) throws  ChessMonteCarloSimulationException {
        this.seasonSettings = seasonSettings;

        PGNParser parser = new PGNParser(Level.ALL);

        // TODO MINOR: count rounds per season and games per match from seasonToSimulate and set variables in MonteCarloSettings
        Games games = parser.parseFile(fileSeasonToSimulate);
        if(games == null || games.getGame().isEmpty())
            throw new ChessMonteCarloSimulationException("no games found in pgn file for season to simulate");

        fileHistoricalSeasons.forEach(season -> {
            Games tmpGames = parser.parseFile(season);
            // remove incomplete games (game without result)
            tmpGames.getGame().removeIf(game -> PgnUtil.isInvalidGame(game));
            this.historicalGames.addAll(tmpGames.getGame());
            this.historicalGamesPerSeason.put(season, tmpGames.getGame());
        });

        processPgnFromSeasonToSimulate(games.getGame());
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

    public double calculateAverageElo() {
        double avgElo = this.teamMap.values().stream()
                .mapToDouble(team -> team.getPlayerList().stream()
                        .mapToDouble(player -> player.getElo())
                        .average().getAsDouble())
                .average().getAsDouble();
        return avgElo;
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

    private int eloDelta(Player player) {
        // white player
        Game minWhite = this.historicalGames.stream()
                .filter(game -> PgnUtil.isWhite(game, player) && !game.getWhiteElo().isEmpty() && Integer.parseInt(game.getWhiteElo()) > 0)
                .min(Comparator.comparing(Game::getWhiteElo))
                .orElse(null);
        // black player
        Game minBlack = this.historicalGames.stream()
                .filter(game -> PgnUtil.isBlack(game, player) && !game.getBlackElo().isEmpty() && Integer.parseInt(game.getBlackElo()) > 0)
                .min(Comparator.comparing(Game::getBlackElo))
                .orElse(null);
        int minElo;
        if(minWhite != null && minBlack != null) {
            minElo = Math.min(Integer.parseInt(minWhite.getWhiteElo()), Integer.parseInt(minBlack.getBlackElo()));
        } else if(minBlack != null) {
            minElo = Integer.parseInt(minBlack.getBlackElo());
        } else if(minWhite != null) {
            minElo = Integer.parseInt(minWhite.getWhiteElo());
        } else {
            minElo = player.getElo(); // fallback if player is not found in historical pgn files
        }
        return player.getElo() - minElo;
    }

    private List<Double> calculateLineUpProbabilities(Player player) {
        final int gamesPerMatch = this.seasonSettings.getGamesPerMatch();
        long[] p = new long[gamesPerMatch];

        IntStream.range(0, gamesPerMatch).forEach(i -> {
            p[i] = this.historicalGames.stream().filter(game -> PgnUtil.isPlaying(game, player) && game.getBoard().equals(""+(i+1))).count();
        });
        long sumNominations = Arrays.stream(p).sum();

        long seasonsPlayed = this.historicalGamesPerSeason.entrySet().stream()
                .filter(e -> e.getValue().stream().filter(game -> PgnUtil.isPlaying(game, player)).count() != 0).count();
        // TODO MINOR: could be slightly improved by counting how many matches the team actually played in the pgn file
        // some pgn files are incomplete - not all match rounds are present
        long maxNominations = this.seasonSettings.getRoundsPerSeason() * seasonsPlayed;

        List<Double> lineup = new ArrayList<>(gamesPerMatch);
        for(int i = 0; i< gamesPerMatch; i++) {
            double l = 0.0;
            if(sumNominations > 0)
                l = ((double) sumNominations / maxNominations) * ((double) p[i] / sumNominations);
            lineup.add(new Double(l));
        }
        return lineup;
    }

    private void processPgnFromSeasonToSimulate(List<Game> games) throws ChessMonteCarloSimulationException {
        Map<String, Team> teamMap = aggregateTeamsFromGames(games);
        if(teamMap.size() < 2)
            throw new ChessMonteCarloSimulationException("not enough teams found in pgn file: at least 2 teams must be present.");
        this.teamMap = teamMap;
    }

    private Map<String, Team> aggregateTeamsFromGames(List<Game> games) {
        Map<String, Team> teamMap = new HashMap<>();

        for (int i = 0; i < games.size(); i++) {
            Game game = games.get(i);

            Player whitePlayer = new Player();
            whitePlayer.setName(game.getWhite());
            whitePlayer.setElo(Integer.parseInt(game.getWhiteElo()));

            Player blackPlayer = new Player();
            blackPlayer.setName(game.getBlack());
            blackPlayer.setElo(Integer.parseInt(game.getBlackElo()));

            Team whiteTeam = teamMap.get(game.getWhiteTeam());
            if(whiteTeam == null) {
                whiteTeam = new Team(game.getWhiteTeam(), seasonSettings.getGamesPerMatch());
            }

            addPlayerToTeam(whitePlayer, whiteTeam);
            teamMap.put(whiteTeam.getName(), whiteTeam);

            Team blackTeam = teamMap.get(game.getBlackTeam());
            if(blackTeam == null) {
                blackTeam = new Team(game.getBlackTeam(), seasonSettings.getGamesPerMatch());
            }

            addPlayerToTeam(blackPlayer, blackTeam);

            // save actual game result for validation of chess prediction model
            ChessGame chessGame = new ChessGame(whitePlayer, blackPlayer, PgnUtil.getGameResult(game.getResult()));
            this.gameResults.add(chessGame);

            int round = Integer.parseInt(game.getRound());
            List<ChessGame> roundChessGames = this.roundGameResults.get(round);
            if(roundChessGames == null) roundChessGames = new ArrayList<>();

            roundChessGames.add(chessGame);
            this.roundGameResults.put(round, roundChessGames);

            teamMap.put(blackTeam.getName(), blackTeam);
        }
        return teamMap;
    }

    private void addPlayerToTeam(Player player, Team team) {
        player.setTeam(team);

        if(!team.getPlayerList().contains(player)) {
            // only do this once (expensive operations)
            player.setEloDelta(eloDelta(player));
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

    public List<Team> getTeams() {
        return new ArrayList<>(this.teamMap.values());
    }

    public List<ChessGame> getGames() {
        return this.gameResults;
    }

    public Map<Integer, List<ChessGame>> getRoundGameResults() { return this.roundGameResults; }
}
