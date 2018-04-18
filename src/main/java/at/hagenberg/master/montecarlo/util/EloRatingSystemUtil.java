package at.hagenberg.master.montecarlo.util;

import at.hagenberg.master.montecarlo.entities.Opponent;
import at.hagenberg.master.montecarlo.entities.Player;
import at.hagenberg.master.montecarlo.entities.Team;
import at.hagenberg.master.montecarlo.simulation.HeadToHeadMatch;
import com.supareno.pgnparser.jaxb.Game;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EloRatingSystemUtil {

    public static double calculateAverageElo(List<Team> teams) {
        double avgElo = teams.stream()
                .mapToDouble(team -> team.getPlayerList().stream()
                        .mapToDouble(player -> player.getElo())
                        .average().getAsDouble())
                .average().getAsDouble();
        return avgElo;
    }

    public static int eloDelta(Player player, List<Game> games) {
        // white player
        Game minWhite = games.stream()
                .filter(game -> PgnUtil.isWhite(game, player) && !game.getWhiteElo().isEmpty() && Integer.parseInt(game.getWhiteElo()) > 0)
                .min(Comparator.comparing(Game::getWhiteElo))
                .orElse(null);
        // black player
        Game minBlack = games.stream()
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

    public static List<Team> regularizePlayerRatingsForTeams(List<Team> teamList, double avgElo, int regularizeThreshold, int regularizeFraction) {
        for (int i = 0; i < teamList.size(); i++) {
            for (int j = 0; j < teamList.get(i).getPlayerList().size(); j++) {
                regularizeRating(teamList.get(i).getPlayerList().get(j), avgElo, regularizeThreshold, regularizeFraction);
            }
        }
        return teamList;
    }

    public static void regularizePlayerRatingsForGames(List<HeadToHeadMatch> games, double avgElo, int regularizeThreshold, int regularizeFraction) {
        Map<String, Player> alreadyRegRating = new HashMap<>();
        for (int i = 0; i < games.size(); i++) {
            ensureAtomicRegularization(games.get(i).getOpponentA(), alreadyRegRating, avgElo, regularizeThreshold, regularizeFraction);
            ensureAtomicRegularization(games.get(i).getOpponentB(), alreadyRegRating, avgElo, regularizeThreshold, regularizeFraction);
        }
    }

    private static void ensureAtomicRegularization(Player player, Map<String, Player> alreadyRegRating, double avgElo, int regularizeThreshold, int regularizeFraction) {
        if(alreadyRegRating.get(player.getName()) != null) {
            Player tmp = alreadyRegRating.get(player.getName());
            player.setRegElo(tmp.getRegElo());
        } else {
            regularizeRating(player, avgElo, regularizeThreshold, regularizeFraction);
            alreadyRegRating.put(player.getName(), player);
        }
    }

    private static void regularizeRating(Player player, double avgElo, int regularizeThreshold, int regularizeFraction) {
        if(player.getTotalGames() < regularizeThreshold) {
            int elo = player.getElo();
            double delta = Math.pow(1.0 - ((double) player.getTotalGames() / regularizeThreshold), 2) * ((elo - avgElo) / regularizeFraction);
            player.setRegElo(elo - new Double(delta).intValue());
            //System.out.println("Name: " + player.getName() + " Elo " + elo + " delta: " + delta + " regElo: " + player.getRegElo() + " total games: " + player.getTotalGames());
        } else {
            player.setRegElo(player.getElo());
            //System.out.println("Name: " + player.getName() + " Elo: " + player.getElo() + " total games: " + player.getTotalGames());
        }
    }
}
