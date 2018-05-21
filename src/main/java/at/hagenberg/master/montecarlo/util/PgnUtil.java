package at.hagenberg.master.montecarlo.util;

import at.hagenberg.master.montecarlo.entities.enums.GameResult;
import com.supareno.pgnparser.jaxb.Game;
import at.hagenberg.master.montecarlo.entities.Player;

public class PgnUtil {

    public static final String DRAW = "1/2-1/2";
    public static final String WHITE_WINS = "1-0";
    public static final String BLACK_WINS = "0-1";

    public static GameResult getGameResult(String result) {
        if(WHITE_WINS.equals(result)) {
            return GameResult.WHITE;
        } else if(BLACK_WINS.equals(result)) {
            return GameResult.BLACK;
        }
        return GameResult.DRAW;
    }

    public static boolean isInvalidGame(Game game) {
        return game.getWhite().isEmpty() || game.getWhiteElo().isEmpty() || game.getWhiteElo().equals("0") || game.getBlack().isEmpty() || game.getBlackElo().isEmpty() || game.getBlackElo().equals("0") || isResult(game, "*");
    }

    public static boolean isPlaying(Game game, Player player) {
        return game.getWhite().equals(player.getName()) || game.getBlack().equals(player.getName());
    }

    public static boolean isWhite(Game game, Player player) {
        return game.getWhite().equals(player.getName());
    }

    public static boolean isBlack(Game game, Player player) {
        return game.getBlack().equals(player.getName());
    }

    public static boolean isResult(Game game, final String RESULT) {
        return game.getResult().equals(RESULT);
    }
}
