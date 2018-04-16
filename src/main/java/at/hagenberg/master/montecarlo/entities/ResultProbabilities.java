package at.hagenberg.master.montecarlo.entities;

public class ResultProbabilities {

    private double expectedWinWhite;
    private double expectedDraw;
    private double expectedWinBlack;

    public ResultProbabilities(double expectedWinWhite, double expectedDraw, double expectedWinBlack) {
        this.expectedWinWhite = expectedWinWhite;
        this.expectedDraw = expectedDraw;
        this.expectedWinBlack = expectedWinBlack;
    }

    public double getExpectedWinWhite() {
        return expectedWinWhite;
    }

    public double getExpectedDraw() {
        return expectedDraw;
    }

    public double getExpectedWinBlack() {
        return expectedWinBlack;
    }

    public void increaseWinWhite(double p) {
        expectedWinWhite += p;
        expectedDraw -= p/2;
        expectedWinBlack -= p/2;

        if(expectedWinWhite > 1.0) {
            expectedWinWhite = 1.0;
            expectedDraw = 0.0;
            expectedWinBlack = 0.0;
        }

        if(expectedDraw < 0) {
            expectedWinBlack -= Math.abs(expectedDraw);
            expectedDraw = 0.0;
        }

        if(expectedWinBlack < 0) {
            expectedDraw -= Math.abs(expectedWinBlack);
            expectedWinBlack = 0.0;
        }
    }

    public void decreaseWinWhite(double p) {
        expectedWinWhite -= p;
        expectedDraw += p/2;
        expectedWinBlack += p/2;

        if(expectedWinWhite < 0) {
            expectedDraw -= Math.abs(expectedWinWhite);
            expectedWinWhite = 0.0;
        }

        if(expectedDraw > 1.0) {
            expectedDraw = 1.0;
            expectedWinBlack = 0.0;
            expectedWinWhite = 0.0;
        }

        if(expectedWinBlack > 1.0) {
            expectedWinBlack = 1.0;
            expectedDraw = 0.0;
            expectedWinWhite = 0.0;
        }
    }

    public void increaseWinBlack(double p) {
        expectedWinWhite -= p/2;
        expectedDraw -= p/2;
        expectedWinBlack += p;

        if(expectedWinWhite < 0) {
            expectedDraw -= Math.abs(expectedWinWhite);
            expectedWinWhite = 0.0;
        }

        if(expectedDraw < 0) {
            expectedWinWhite -= Math.abs(expectedDraw);
            expectedDraw = 0.0;
        }

        if(expectedWinBlack > 1.0) {
            expectedWinBlack = 1.0;
            expectedDraw = 0.0;
            expectedWinWhite = 0.0;
        }
    }


    public void decreaseWinBlack(double p) {
        expectedWinWhite += p/2;
        expectedDraw += p/2;
        expectedWinBlack -= p;

        if(expectedWinWhite > 1.0) {
            expectedWinWhite = 1.0;
            expectedDraw = 0.0;
            expectedWinBlack = 0.0;
        }

        if(expectedDraw > 1.0) {
            expectedWinWhite = 0.0;
            expectedDraw = 1.0;
            expectedWinBlack = 0.0;
        }

        if(expectedWinBlack < 0) {
            expectedDraw -= Math.abs(expectedWinBlack);
            expectedWinBlack = 0.0;
        }
    }

    public void increaseDraw(double p) {
        expectedWinWhite -= p/2;
        expectedDraw += p;
        expectedWinBlack -= p/2;

        if(expectedWinWhite < 0) {
            expectedWinBlack -= Math.abs(expectedWinWhite);
            expectedWinWhite = 0.0;
        }

        if(expectedDraw > 1.0) {
            expectedDraw = 1.0;
            expectedWinBlack = 0.0;
            expectedWinWhite = 0.0;
        }

        if(expectedWinBlack < 0) {
            expectedWinWhite -= Math.abs(expectedWinBlack);
            expectedWinBlack = 0.0;
        }
    }
}
