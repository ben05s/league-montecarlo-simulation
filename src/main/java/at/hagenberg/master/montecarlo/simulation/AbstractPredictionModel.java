package at.hagenberg.master.montecarlo.simulation;

import at.hagenberg.master.montecarlo.entities.Player;
import at.hagenberg.master.montecarlo.entities.ResultProbabilities;

public abstract class AbstractPredictionModel {

    public abstract ResultProbabilities calculateGameResultProbabilities(Player one, Player two);
}
