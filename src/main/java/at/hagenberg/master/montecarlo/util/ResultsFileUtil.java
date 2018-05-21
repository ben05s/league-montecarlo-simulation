package at.hagenberg.master.montecarlo.util;

import at.hagenberg.master.montecarlo.entities.*;
import at.hagenberg.master.montecarlo.prediction.ChessPredictionModel;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ResultsFileUtil {

    public static void writeEvalutations(String filename, List<Evaluation> evaluations) {
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter("result/evaluation/" + filename + ".csv");
            bw = new BufferedWriter(fw);
            bw.write("useEloRatingSystem;useHomeAdvantage;useStrengthTrend;usePlayerPerformances;useRatingRegularization;regularizeThreshold;regularizeFraction;winDrawFraction;statsFactor;strengthTrendFraction;advWhite;avgElo;pCorrect;RMSE;pCorrectWhite;pCorrectDraw;pCorrectBlack;games\n");
            for (int i = 0; i < evaluations.size(); i++) {
                Evaluation e = evaluations.get(i);
                bw.write(e.print());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fw != null)
                    fw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void writePlayerStats(String filename, List<Team> teamList) {
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter("result/player-stats/" + filename + ".csv");
            bw = new BufferedWriter(fw);
            bw.write("team,avgElo,player,elo,regElo,eloDelta,totalGames,whiteWinAmount,whiteWinP,whiteDrawAmount,whiteDrawP,whiteLossAmount,whiteLossP,blackWinAmount,blackWinP,blackDrawAmount,blackDrawP,blackLossAmount,blackLossP,lineup1,lineup2,lineup3,lineup4,lineup5,lineup6\n");
            for (int i = 0; i < teamList.size(); i++) {
                Team team = teamList.get(i);
                for (int x = 0; x < team.getPlayerList().size(); x++) {
                    Player p = team.getPlayerList().get(x);
                    bw.write("\"" + team.getName() + "\"" +
                            "," + team.getAverageElo() +
                            ",\"" + p.getName() + "\"" +
                            "," + p.getElo() +
                            "," + p.getRegElo() +
                            "," + p.getEloDelta() +
                            "," + p.getTotalGames() +
                            "," + p.getWhiteWins() + "," + String.format("%.4f", p.getpWhiteWin()).replace(",", ".") +
                            "," + p.getWhiteDraws() + "," + String.format("%.4f", p.getpWhiteDraw()).replace(",", ".") +
                            "," + p.getWhiteLoss() + "," + String.format("%.4f", p.getpWhiteLoss()).replace(",", ".") +
                            "," + p.getBlackWins() + "," + String.format("%.4f", p.getpBlackWin()).replace(",", ".") +
                            "," + p.getBlackDraws() + "," + String.format("%.4f", p.getpBlackDraw()).replace(",", ".") +
                            "," + p.getBlackLoss() + "," + String.format("%.4f", p.getpBlackLoss()).replace(",", ".") +
                            "," + String.format("%.4f", p.getpLineUp().get(0)).replace(",", ".") +
                            "," + String.format("%.4f", p.getpLineUp().get(1)).replace(",", ".") +
                            "," + String.format("%.4f", p.getpLineUp().get(2)).replace(",", ".") +
                            "," + String.format("%.4f", p.getpLineUp().get(3)).replace(",", ".") +
                            "," + String.format("%.4f", p.getpLineUp().get(4)).replace(",", ".") +
                            "," + String.format("%.4f", p.getpLineUp().get(5)).replace(",", ".") + "\n"
                    );
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fw != null)
                    fw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void writeSeasonResultToFile(SeasonResult seasonResult, int iteration) {
        String filename = "mc-results.csv";
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter("result/season-results/" + filename);
            bw = new BufferedWriter(fw);

            bw.write("MC-Iteration,Team,PointsScored,PointsConceded,Promotion,Relegation\n");
            bw.write(seasonResult.print(iteration));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fw != null)
                    fw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void writeMatchResultToFile(String filename, List<MatchResult> matchList) {
        BufferedWriter bw = null;
        FileWriter fw = null;

        try {
            fw = new FileWriter("result/iteration-results/" + filename + ".csv");
            bw = new BufferedWriter(fw);
            bw.write("TeamA,PointsTeamA,TeamB,PointsTeamB,Result\n");
            for (int x = 0; x < matchList.size(); x++) {
                bw.write( matchList.get(x).print());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fw != null)
                    fw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void writeGameResultToFile(String filename, List<MatchResult> matchList) {
        for(int i=0;i<matchList.size();i++) {
            MatchResult result = matchList.get(i);
            BufferedWriter bw = null;
            FileWriter fw = null;

            try {
                String file = filename + result.getOpponentA().getName() + "-" +result.getOpponentB().getName() + ".csv";
                fw = new FileWriter("result/match-results/" + file.replaceAll("[^a-zA-Z0-9.-]", "-"));
                bw = new BufferedWriter(fw);
                bw.write("PlayerWhite,EloWhite,PlayerBlack,EloBlack,Winner\n");
                for (int x = 0; x < result.getHeadToHeadMatches().size(); x++) {
                    bw.write(result.getHeadToHeadMatches().get(x).print());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (bw != null)
                        bw.close();
                    if (fw != null)
                        fw.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
