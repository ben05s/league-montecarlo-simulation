package at.hagenberg.master.montecarlo.entities;

public abstract class SimulationResult {

    protected long simulationDurationMs;

    public SimulationResult() {}

    public long getSimulationDurationMs() {
        return simulationDurationMs;
    }

    public void setSimulationDurationMs(long simulationDurationMs) {
        this.simulationDurationMs = simulationDurationMs;
    }

}
