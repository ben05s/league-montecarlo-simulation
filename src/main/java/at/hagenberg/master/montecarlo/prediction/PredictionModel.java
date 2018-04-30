package at.hagenberg.master.montecarlo.prediction;

import at.hagenberg.master.montecarlo.entities.Player;

public interface PredictionModel {

    ResultPrediction calculatePrediction(Player one, Player two);
}
