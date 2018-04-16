package at.hagenberg.master.montecarlo.entities;

import java.util.*;
import java.util.stream.Collectors;

public class SeasonResult {

    private Map<String, SeasonScore> teamSeasonScoreMap = new HashMap<>();
    private List<MatchResult> matchResults = new ArrayList<>();

    private long simulationDurationMs;

    public SeasonResult() {
    }

    public void addMatchResult(MatchResult matchResult) {
        matchResults.add(matchResult);

        SeasonScore scoreA = teamSeasonScoreMap.get(matchResult.getTeamA().getName());
        if (scoreA == null)
            scoreA = new SeasonScore();

        SeasonScore scoreB = teamSeasonScoreMap.get(matchResult.getTeamB().getName());
        if (scoreB == null)
            scoreB = new SeasonScore();

        scoreA.addPointsScored(matchResult.getScoreTeamA());
        scoreA.addPointsConceded(matchResult.getScoreTeamB());

        scoreB.addPointsScored(matchResult.getScoreTeamB());
        scoreB.addPointsConceded(matchResult.getScoreTeamA());

        if(matchResult.getWinner() == null) {
            scoreA.addDraw();
            scoreB.addDraw();
        } else if(matchResult.getWinner().equals(matchResult.getTeamA())) {
            scoreA.addWin();
        } else if(matchResult.getWinner().equals(matchResult.getTeamB())) {
            scoreB.addWin();
        }

        teamSeasonScoreMap.put(matchResult.getTeamA().getName(), scoreA);
        teamSeasonScoreMap.put(matchResult.getTeamB().getName(), scoreB);
    }

    private static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        return map.entrySet().stream().sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    public Map<String, SeasonScore> getTeamSeasonScoreMap() {
        return sortByValue(this.teamSeasonScoreMap);
    }

    public List<MatchResult> getMatchResults() {
        return matchResults;
    }

    public long getSimulationDurationMs() {
        return simulationDurationMs;
    }

    public void setSimulationDurationMs(long simulationDurationMs) {
        this.simulationDurationMs = simulationDurationMs;
    }

    public String print(int iteration) {
        //"MC-Iteration|Team|SeasonPoints|PointsScored|PointsConceded|Promotion|Relegation"
        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<String, SeasonScore>> it = this.teamSeasonScoreMap.entrySet().iterator();

        if(it.hasNext()) {
            Map.Entry<String, SeasonScore> entry = it.next();
            sb.append(iteration).append(",").append(entry.getKey()).append(",")
                    .append(entry.getValue().getSeasonPoints()).append(",")
                    .append(entry.getValue().getPointsScored()).append(",").append(entry.getValue().getPointsConceded())
                    .append(",").append(true);
        }

        while(it.hasNext()) {
            sb.append(",").append(false).append("\n");
            Map.Entry<String, SeasonScore> entry = it.next();
            sb.append(iteration).append(",").append(entry.getKey()).append(",")
                    .append(entry.getValue().getSeasonPoints()).append(",")
                    .append(entry.getValue().getPointsScored()).append(",").append(entry.getValue().getPointsConceded())
                    .append(",").append(false);
        }
        sb.append(",").append(true).append("\n");
        return sb.toString();
    }
}
