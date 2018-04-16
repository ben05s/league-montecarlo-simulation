package at.hagenberg.master.montecarlo.simulation;

import at.hagenberg.master.montecarlo.PgnAnalysis;
import at.hagenberg.master.montecarlo.entities.Player;
import at.hagenberg.master.montecarlo.entities.Team;
import at.hagenberg.master.montecarlo.entities.enums.RatingSystem;
import at.hagenberg.master.montecarlo.util.PgnUtil;

import java.util.*;

public class ChessPredictionModel {

    public RatingSystem ratingSystem = RatingSystem.ELO;

    /* Prediction Parameters */
    public boolean useAdvWhite = false;
    public boolean useStrengthTrend = false;
    public boolean useStats = false;
    public boolean useRegularization = false;

    /* Tuning Parameter */
    public int regularizeThreshold = 22; // who should have their rating adjusted - 22 reassembles a player who as played 2 seasons approximately
    public int regularizeFraction = 2; // reduce the amount of rating points adjusted from regularizing
    public int winDrawFraction = 2; // reduce win and lose probabilities based on draw probability
    public int statsFactor = 1; // stats influence the game probabilities more
    public int strengthTrendFraction = 2500; // could also be 1000 then influence is higher
    public double advWhiteProbability = 0.0; // calculated based on all games in pgn files, could also be taken from literature 0.54

    public double avgElo; // Average Elo from all players who play in the season to simulate

    /* Overall Statistics from all PGN files */
    public double pWhiteWin;
    public double pDraw;
    public double pBlackWin;

    public ChessPredictionModel(boolean useAdvWhite, boolean useStrengthTrend, boolean useStats, boolean useRegularization) {
        this.useAdvWhite = useAdvWhite;
        this.useStrengthTrend = useStrengthTrend;
        this.useStats = useStats;
        this.useRegularization = useRegularization;
    }

    public List<Team> regularizePlayerRatingsForTeams(List<Team> teamList) {
        if(this.useRegularization) {
            for (int i = 0; i < teamList.size(); i++) {
                for (int j = 0; j < teamList.get(i).getPlayerList().size(); j++) {
                    regularizeRating(teamList.get(i).getPlayerList().get(j));
                }
            }
        }
        return teamList;
    }

    public void regularizePlayerRatingsForGames(List<ChessGame> games) {
        if(this.useRegularization) {
            Map<String, Player> alreadyRegRating = new HashMap<>();
            for (int i = 0; i < games.size(); i++) {
                ensureAtomicRegularization(games.get(i).getWhite(), alreadyRegRating);
                ensureAtomicRegularization(games.get(i).getBlack(), alreadyRegRating);
            }
        }
    }

    private void ensureAtomicRegularization(Player player, Map<String, Player> alreadyRegRating) {
        if(alreadyRegRating.get(player.getName()) != null) {
            Player tmp = alreadyRegRating.get(player.getName());
            player.setRegElo(tmp.getRegElo());
        } else {
            regularizeRating(player);
            alreadyRegRating.put(player.getName(), player);
        }
    }

    private void regularizeRating(Player player) {
        if(player.getTotalGames() < regularizeThreshold) {
            int elo = player.getElo();
            double delta = Math.pow(1.0 - ((double) player.getTotalGames() / regularizeThreshold), 2) * ((elo - this.avgElo) / regularizeFraction);
            player.setRegElo(elo - new Double(delta).intValue());
            //System.out.println("Name: " + player.getName() + " Elo " + elo + " delta: " + delta + " regElo: " + player.getRegElo() + " total games: " + player.getTotalGames());
        } else {
            player.setRegElo(player.getElo());
            //System.out.println("Name: " + player.getName() + " Elo: " + player.getElo() + " total games: " + player.getTotalGames());
        }
    }

    public double calculateExpectedWinWhite(Player white, Player black) {
        int whiteElo = white.getElo();
        int blackElo = black.getElo();

        if(this.useRegularization) {
            whiteElo = white.getRegElo();
            blackElo = black.getRegElo();
        }

        double expectedWinWhite = 1/3;
        double exponent = (double) -(whiteElo - blackElo) / 400;
        expectedWinWhite = (1.0 / (1 + (Math.pow(10, exponent))));
        return expectedWinWhite;
    }

    public double calculateExpectedDraw(Player white, Player black) {
        int whiteElo = white.getElo();
        int blackElo = black.getElo();

        if(this.useRegularization) {
            whiteElo = white.getRegElo();
            blackElo = black.getRegElo();
        }

        double expectedDraw = 1/3;
        double avgElo = (whiteElo + blackElo) / 2.0;
        expectedDraw = - Math.abs(whiteElo - blackElo) / 32.49 + Math.exp((avgElo - 2254.7) / 208.49) + 23.87;
        expectedDraw = Math.exp((avgElo)/640) - ((Math.abs(whiteElo - blackElo)*55) / (3000-avgElo)) + 15.0;
        expectedDraw /= 100;
        if(expectedDraw < 0) expectedDraw = 0.0;
        return expectedDraw;
    }

    public double calculateAdvantageWhite(double expectedWinWhite, double expectedWinBlack) {
        if(this.useAdvWhite) {
            expectedWinWhite = (this.advWhiteProbability * expectedWinWhite) / (this.advWhiteProbability * expectedWinWhite + (1-this.advWhiteProbability) * expectedWinBlack);
        }
        return expectedWinWhite;
    }

    public double calculateStrengthTrend(Player player) {
        double strength = 0.0;
        // TODO think about this how to incorporate strength trend together with regularization
        if(this.useStrengthTrend && !this.useRegularization) {
            if (player.getEloDelta() < 200 && player.getEloDelta() > -200) {
                strength = player.getEloDelta() / strengthTrendFraction;
            }
        }
        return strength;
    }

    public double calculateStatsStrengthAsWhite(Player player) {
        double strength = 0.0;
        if(this.useStats) {
            strength = (player.getpWhiteWin() - player.getpWhiteLoss());
        }
        return strength * statsFactor;
    }

    public double calculateStatsStrengthAsBlack(Player player) {
        double strength = 0.0;
        if(this.useStats) {
            strength = (player.getpBlackWin() - player.getpBlackLoss());
        }
        return strength * statsFactor;
    }

    public double calculateStatsStrengthDraw(Player white, Player black) {
        double strength = 0.0;
        if(this.useStats) {
            if (white.getpWhiteDraw() > white.getpWhiteWin() && white.getpWhiteDraw() > white.getpWhiteLoss()) {
                strength = Math.min(white.getpWhiteDraw() - white.getpWhiteWin(), white.getpWhiteDraw() - white.getpWhiteLoss());
            }
            if (black.getpBlackDraw() > black.getpBlackWin() && black.getpBlackDraw() > black.getpBlackLoss()) {
                strength = Math.min(black.getpBlackDraw() - black.getpBlackWin(), black.getpBlackDraw() - black.getpBlackLoss());
            }
        }
        return strength * statsFactor;
    }

    public double incorporateDrawProbability(double expectedWin, double expectedDraw) {
        return expectedWin - (expectedDraw / winDrawFraction);
    }

    public double calculateExpectedWinBlack(double expectedWinWhite) {
        return 1.0 - expectedWinWhite;
    }

    public void setStatistics(PgnAnalysis analysis) {
        advWhiteProbability = analysis.calculateWhiteAdvantage();
        avgElo = analysis.calculateAverageElo();
        pWhiteWin = analysis.calculateOverallProbability(PgnUtil.WHITE_WINS);
        pDraw = analysis.calculateOverallProbability(PgnUtil.DRAW);
        pBlackWin = analysis.calculateOverallProbability(PgnUtil.BLACK_WINS);
    }

    @Override
    public String toString() {
        return "ChessPredictionModel{" +
                "ratingSystem=" + ratingSystem +
                ", useAdvWhite=" + useAdvWhite +
                ", useStrengthTrend=" + useStrengthTrend +
                ", useStats=" + useStats +
                ", useRegularization=" + useRegularization +
                "\n\tregularizeThreshold=" + regularizeThreshold +
                ", regularizeFraction=" + regularizeFraction +
                ", winDrawFraction=" + winDrawFraction +
                ", statsFactor=" + statsFactor +
                ", strengthTrendFraction=" + strengthTrendFraction +
                ", advantageWhiteProbability=" + String.format("%.4f", advWhiteProbability) +
                ", avgElo=" + String.format("%.4f", avgElo) +
                ", pWhiteWin=" + pWhiteWin +
                ", pDraw=" + pDraw +
                ", pBlackWin=" + pBlackWin +
                "}";
    }

    public double getAdvWhiteProbability() { return advWhiteProbability; }

    public double getAvgElo() { return avgElo; }

    public double getpWhiteWin() { return pWhiteWin; }

    public double getpDraw() { return pDraw; }

    public double getpBlackWin() { return pBlackWin; }
}
