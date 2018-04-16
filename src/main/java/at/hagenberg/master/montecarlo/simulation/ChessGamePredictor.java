package at.hagenberg.master.montecarlo.simulation;

import at.hagenberg.master.montecarlo.exceptions.ChessMonteCarloSimulationException;
import at.hagenberg.master.montecarlo.entities.Player;
import at.hagenberg.master.montecarlo.entities.ResultProbabilities;

public class ChessGamePredictor {

    private ChessPredictionModel predictionModel;
    private Player white;
    private Player black;

    public ChessGamePredictor(Player white, Player black, ChessPredictionModel predictionModel) {
        this.white = white;
        this.black = black;
        this.predictionModel = predictionModel;
    }

    public ResultProbabilities calculateGameResultProbabilities() throws ChessMonteCarloSimulationException {
        double expectedWinWhite = 1.0 / 3.0;
        double expectedDraw = 1.0 / 3.0;
        double expectedWinBlack = 1.0 / 3.0;

        switch(this.predictionModel.ratingSystem) {
            case ELO:
                expectedWinWhite = predictionModel.calculateExpectedWinWhite(white, black);
                expectedWinBlack = predictionModel.calculateExpectedWinBlack(expectedWinWhite);
                expectedDraw = predictionModel.calculateExpectedDraw(white, black);

                expectedWinWhite = predictionModel.incorporateDrawProbability(expectedWinWhite, expectedDraw);
                expectedWinBlack = predictionModel.incorporateDrawProbability(expectedWinBlack, expectedDraw);

                expectedWinWhite = predictionModel.calculateAdvantageWhite(expectedWinWhite, expectedWinBlack);

                expectedWinWhite += predictionModel.calculateStrengthTrend(white);
                expectedWinBlack += predictionModel.calculateStrengthTrend(black);

                expectedWinWhite += predictionModel.calculateStatsStrengthAsWhite(white);
                expectedWinBlack += predictionModel.calculateStatsStrengthAsBlack(black);
                expectedDraw += predictionModel.calculateStatsStrengthDraw(white, black);

                if(expectedWinWhite < 0) expectedWinWhite = 0.0;
                if(expectedWinBlack < 0) expectedWinBlack = 0.0;
                if(expectedDraw < 0) expectedDraw = 0.0;
                break;
            case ALTERNATIVE:
                throw new ChessMonteCarloSimulationException("alternative rating system not yet implemented");
            default:
                throw new ChessMonteCarloSimulationException("no rating system specified");
        }

        ResultProbabilities p = new ResultProbabilities(expectedWinWhite, expectedDraw, expectedWinBlack);
        return p;
    }

    private ResultProbabilities includePlayerStrengthTrend(ResultProbabilities p) {
        double smallInfluence = 0.02;
        double mediumInfluence = 0.04;
        double largeInfluence = 0.06;
        if(white.getEloDelta() > 50 && white.getEloDelta() < 100) {
            p.increaseWinWhite(smallInfluence);
        } else if(white.getEloDelta() > 100 && white.getEloDelta() < 200) {
            p.increaseWinWhite(mediumInfluence);
        } else if(white.getEloDelta() > 200) {
            p.increaseWinWhite(largeInfluence);
        } else if(white.getEloDelta() > -100 && white.getEloDelta() < -50) {
            p.decreaseWinWhite(smallInfluence);
        } else if(white.getEloDelta() > -200 && white.getEloDelta() < -100) {
            p.decreaseWinWhite(mediumInfluence);
        } else if(white.getEloDelta() < -200) {
            p.decreaseWinWhite(largeInfluence);
        }

        if(black.getEloDelta() >= 50 && black.getEloDelta() < 100) {
            p.increaseWinBlack(smallInfluence);
        } else if(black.getEloDelta() >= 100 && black.getEloDelta() < 200) {
            p.increaseWinBlack(mediumInfluence);
        } else if(black.getEloDelta() >= 200) {
            p.increaseWinBlack(largeInfluence);
        } else if(black.getEloDelta() > -100 && black.getEloDelta() <= -50) {
            p.decreaseWinBlack(smallInfluence);
        } else if(black.getEloDelta() > -200 && black.getEloDelta() <= -100) {
            p.decreaseWinBlack(mediumInfluence);
        } else if(black.getEloDelta() <= -200) {
            p.decreaseWinBlack(largeInfluence);
        }
        return p;
    }

    private ResultProbabilities includePlayerStats(ResultProbabilities p) {
        double deltaWinLossWhite = white.getpWhiteWin() - white.getpWhiteLoss();
        if(deltaWinLossWhite > 0) {
            p.increaseWinWhite(deltaWinLossWhite);
        } else {
            p.decreaseWinWhite(Math.abs(deltaWinLossWhite));
        }
        if(white.getpWhiteDraw() > white.getpWhiteWin() && white.getpWhiteDraw() > white.getpWhiteLoss()) {
            double deltaDrawWhite = Math.min(white.getpWhiteDraw() - white.getpWhiteWin(), white.getpWhiteDraw() - white.getpWhiteLoss());
            p.increaseDraw(deltaDrawWhite);
        }

        double deltaWinLossBlack = black.getpBlackWin() - black.getpBlackLoss();
        if(deltaWinLossBlack > 0) {
            p.increaseWinBlack(deltaWinLossBlack);
        } else {
            p.decreaseWinBlack(Math.abs(deltaWinLossBlack));
        }
        if(black.getpBlackDraw() > black.getpBlackWin() && black.getpBlackDraw() > black.getpBlackLoss()) {
            double deltaDrawBlack = Math.min(black.getpBlackDraw() - black.getpBlackWin(), black.getpBlackDraw() - black.getpBlackLoss());
            p.increaseDraw(deltaDrawBlack);
        }
        return p;
    }
}
