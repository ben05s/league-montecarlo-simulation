package at.hagenberg.master.montecarlo.prediction;

import at.hagenberg.master.montecarlo.entities.enums.RatingSystem;
import at.hagenberg.master.montecarlo.parser.PgnAnalysis;
import at.hagenberg.master.montecarlo.entities.Player;
import at.hagenberg.master.montecarlo.util.EloRatingSystemUtil;
import at.hagenberg.master.montecarlo.util.PgnUtil;

public class ChessPredictionModel implements PredictionModel {

    /* Prediction Parameters */
    public boolean useEloRating = false;
    public boolean useHomeAdvantage = false;
    public boolean useStrengthTrend = false;
    public boolean usePlayerPerformances = false;
    public boolean useRatingRegularization = false;

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

    public ChessPredictionModel() {
        this(false, false, false, false, false);
    }

    public ChessPredictionModel(boolean useEloRating, boolean useAdvWhite, boolean useStrengthTrend, boolean useStats, boolean useRegularization) {
        super();
        this.useEloRating = useEloRating;
        this.useHomeAdvantage = useHomeAdvantage;
        this.useStrengthTrend = useStrengthTrend;
        this.usePlayerPerformances = usePlayerPerformances;
        this.useRatingRegularization = useRatingRegularization;
    }

    @Override
    public ResultPrediction calculatePrediction(Player white, Player black) {
        double expectedWinWhite = 1.0 / 3.0;
        double expectedDraw = 1.0 / 3.0;
        double expectedWinBlack = 1.0 / 3.0;

        expectedWinWhite = this.calculateExpectedWinWhite(white, black);
        expectedWinBlack = this.calculateExpectedWinBlack(expectedWinWhite);

        expectedWinWhite += this.calculateColorStrengthAsWhite(white);
        expectedWinBlack += this.calculateColorStrengthAsBlack(black);

        expectedDraw = this.calculateExpectedDraw(white, black);

        expectedDraw += this.calculateColorStrengthDraw(white, black);

        expectedWinWhite = this.incorporateDrawProbability(expectedWinWhite, expectedDraw);
        expectedWinBlack = this.incorporateDrawProbability(expectedWinBlack, expectedDraw);

        //not used
        expectedWinWhite += this.calculateStrengthTrend(white);
        expectedWinBlack += this.calculateStrengthTrend(black);

        if(expectedWinWhite < 0) expectedWinWhite = 0.0;
        if(expectedWinBlack < 0) expectedWinBlack = 0.0;
        if(expectedDraw < 0) expectedDraw = 0.0;

        ResultPrediction p = new ResultPrediction(expectedWinWhite, expectedDraw, expectedWinBlack);
        return p;
    }

    private double calculateExpectedWinWhite(Player white, Player black) {
        if(!useEloRating) return 1.0 / 3.0;

        int whiteElo = white.getElo();
        int blackElo = black.getElo();

        if(this.useRatingRegularization) {
            whiteElo = white.getRegElo();
            blackElo = black.getRegElo();
        }

        double expectedWinWhite = 1/3;
        double exponent = (double) -(whiteElo - blackElo) / 400;
        expectedWinWhite = (1.0 / (1 + (Math.pow(10, exponent))));

        expectedWinWhite = this.calculateAdvantageWhite(expectedWinWhite);

        return expectedWinWhite;
    }

    private double calculateExpectedDraw(Player white, Player black) {
        if(!useEloRating) return 1.0 / 3.0;

        int whiteElo = white.getElo();
        int blackElo = black.getElo();

        if(this.useRatingRegularization) {
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

    private double calculateAdvantageWhite(double expectedWinWhite) {
        if(this.useHomeAdvantage) {
            expectedWinWhite = (this.advWhiteProbability * expectedWinWhite) / (this.advWhiteProbability * expectedWinWhite + (1-this.advWhiteProbability) * (1-expectedWinWhite));
        }
        return expectedWinWhite;
    }

    private double calculateStrengthTrend(Player player) {
        double strength = 0.0;
        // TODO think about this how to incorporate strength trend together with regularization
        if(this.useStrengthTrend && !this.useRatingRegularization) {
            if (player.getEloDelta() < 200 && player.getEloDelta() > -200) {
                strength = player.getEloDelta() / strengthTrendFraction;
            }
        }
        return strength;
    }

    private double calculateColorStrengthAsWhite(Player player) {
        double strength = 0.0;
        if(this.usePlayerPerformances) {
            strength = (player.getpWhiteWin() - player.getpWhiteLoss());
        }
        return strength * statsFactor;
    }

    private double calculateColorStrengthAsBlack(Player player) {
        double strength = 0.0;
        if(this.usePlayerPerformances) {
            strength = (player.getpBlackWin() - player.getpBlackLoss());
        }
        return strength * statsFactor;
    }

    private double calculateColorStrengthDraw(Player white, Player black) {
        double strength = 0.0;
        if(this.usePlayerPerformances) {
            if (white.getpWhiteDraw() > white.getpWhiteWin() && white.getpWhiteDraw() > white.getpWhiteLoss()) {
                strength = Math.min(white.getpWhiteDraw() - white.getpWhiteWin(), white.getpWhiteDraw() - white.getpWhiteLoss());
            }
            if (black.getpBlackDraw() > black.getpBlackWin() && black.getpBlackDraw() > black.getpBlackLoss()) {
                strength = Math.min(black.getpBlackDraw() - black.getpBlackWin(), black.getpBlackDraw() - black.getpBlackLoss());
            }
        }
        return strength * statsFactor;
    }

    private double incorporateDrawProbability(double expectedWin, double expectedDraw) {
        return expectedWin - (expectedDraw / winDrawFraction);
    }

    private double calculateExpectedWinBlack(double expectedWinWhite) {
        if(!useEloRating) return 1.0 / 3.0;

        return 1.0 - expectedWinWhite;
    }

    public void setStatistics(PgnAnalysis analysis) {
        advWhiteProbability = analysis.calculateWhiteAdvantage();
        avgElo = EloRatingSystemUtil.calculateAverageElo(analysis.getTeams());
        pWhiteWin = analysis.calculateOverallProbability(PgnUtil.WHITE_WINS);
        pDraw = analysis.calculateOverallProbability(PgnUtil.DRAW);
        pBlackWin = analysis.calculateOverallProbability(PgnUtil.BLACK_WINS);
    }

    @Override
    public String toString() {
        return "ChessPredictionModel{" +
                "useEloRating=" + useEloRating +
                ", useHomeAdvantage=" + useHomeAdvantage +
                ", useStrengthTrend=" + useStrengthTrend +
                ", usePlayerPerformances=" + usePlayerPerformances +
                ", useRatingRegularization=" + useRatingRegularization +
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
