package at.hagenberg.master.montecarlo.entities;

public abstract class AbstractMonteCarloResult {

    protected long simulationDurationMs;

    public AbstractMonteCarloResult() {}

    public long getSimulationDurationMs() {
        return simulationDurationMs;
    }

    public void setSimulationDurationMs(long simulationDurationMs) {
        this.simulationDurationMs = simulationDurationMs;
    }

}
