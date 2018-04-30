package at.hagenberg.master.montecarlo.prediction;

public class ResultPrediction {

    private double expectedWinPlayerOne;
    private double expectedDraw;
    private double expectedWinPlayerTwo;

    public ResultPrediction(double expectedWinPlayerOne, double expectedDraw, double expectedWinPlayerTwo) {
        this.expectedWinPlayerOne = expectedWinPlayerOne;
        this.expectedDraw = expectedDraw;
        this.expectedWinPlayerTwo = expectedWinPlayerTwo;
    }

    public double getExpectedWinPlayerOne() {
        return expectedWinPlayerOne;
    }

    public double getExpectedDraw() {
        return expectedDraw;
    }

    public double getExpectedWinPlayerTwo() {
        return expectedWinPlayerTwo;
    }

    public void increaseWinWhite(double p) {
        expectedWinPlayerOne += p;
        expectedDraw -= p/2;
        expectedWinPlayerTwo -= p/2;

        if(expectedWinPlayerOne > 1.0) {
            expectedWinPlayerOne = 1.0;
            expectedDraw = 0.0;
            expectedWinPlayerTwo = 0.0;
        }

        if(expectedDraw < 0) {
            expectedWinPlayerTwo -= Math.abs(expectedDraw);
            expectedDraw = 0.0;
        }

        if(expectedWinPlayerTwo < 0) {
            expectedDraw -= Math.abs(expectedWinPlayerTwo);
            expectedWinPlayerTwo = 0.0;
        }
    }

    public void decreaseWinWhite(double p) {
        expectedWinPlayerOne -= p;
        expectedDraw += p/2;
        expectedWinPlayerTwo += p/2;

        if(expectedWinPlayerOne < 0) {
            expectedDraw -= Math.abs(expectedWinPlayerOne);
            expectedWinPlayerOne = 0.0;
        }

        if(expectedDraw > 1.0) {
            expectedDraw = 1.0;
            expectedWinPlayerTwo = 0.0;
            expectedWinPlayerOne = 0.0;
        }

        if(expectedWinPlayerTwo > 1.0) {
            expectedWinPlayerTwo = 1.0;
            expectedDraw = 0.0;
            expectedWinPlayerOne = 0.0;
        }
    }

    public void increaseWinBlack(double p) {
        expectedWinPlayerOne -= p/2;
        expectedDraw -= p/2;
        expectedWinPlayerTwo += p;

        if(expectedWinPlayerOne < 0) {
            expectedDraw -= Math.abs(expectedWinPlayerOne);
            expectedWinPlayerOne = 0.0;
        }

        if(expectedDraw < 0) {
            expectedWinPlayerOne -= Math.abs(expectedDraw);
            expectedDraw = 0.0;
        }

        if(expectedWinPlayerTwo > 1.0) {
            expectedWinPlayerTwo = 1.0;
            expectedDraw = 0.0;
            expectedWinPlayerOne = 0.0;
        }
    }


    public void decreaseWinBlack(double p) {
        expectedWinPlayerOne += p/2;
        expectedDraw += p/2;
        expectedWinPlayerTwo -= p;

        if(expectedWinPlayerOne > 1.0) {
            expectedWinPlayerOne = 1.0;
            expectedDraw = 0.0;
            expectedWinPlayerTwo = 0.0;
        }

        if(expectedDraw > 1.0) {
            expectedWinPlayerOne = 0.0;
            expectedDraw = 1.0;
            expectedWinPlayerTwo = 0.0;
        }

        if(expectedWinPlayerTwo < 0) {
            expectedDraw -= Math.abs(expectedWinPlayerTwo);
            expectedWinPlayerTwo = 0.0;
        }
    }

    public void increaseDraw(double p) {
        expectedWinPlayerOne -= p/2;
        expectedDraw += p;
        expectedWinPlayerTwo -= p/2;

        if(expectedWinPlayerOne < 0) {
            expectedWinPlayerTwo -= Math.abs(expectedWinPlayerOne);
            expectedWinPlayerOne = 0.0;
        }

        if(expectedDraw > 1.0) {
            expectedDraw = 1.0;
            expectedWinPlayerTwo = 0.0;
            expectedWinPlayerOne = 0.0;
        }

        if(expectedWinPlayerTwo < 0) {
            expectedWinPlayerOne -= Math.abs(expectedWinPlayerTwo);
            expectedWinPlayerTwo = 0.0;
        }
    }
}
