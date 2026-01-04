package BankOfTuc.Services;
import java.time.*;

public class SimulatedClock extends Clock {

    private Instant currentInstant;
    private boolean simulated = false;

    public SimulatedClock() {}

    public void startSimulation() {
        this.currentInstant = Instant.now();
        this.simulated = true;
    }

    public void advanceHours(long hours) {
        ensureSimulated();
        currentInstant = currentInstant.plus(Duration.ofHours(hours));
    }

    public void advanceDays(long days) {
        ensureSimulated();
        currentInstant = currentInstant.plus(Duration.ofDays(days));
    }

    //stop the simulation and return to real time
    public void stopSimulation() {
        this.simulated = false;
        this.currentInstant = null; //reset
    }

    @Override
    public Instant instant() {
        return simulated ? currentInstant : Instant.now();
    }

    @Override
    public ZoneId getZone() {
        return ZoneId.systemDefault();
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return simulated
                ? Clock.fixed(currentInstant, zone)
                : Clock.system(zone);
    }

    //ensure simulation is started before using
    private void ensureSimulated() {
        if (!simulated) {
            throw new IllegalStateException("Simulation has not been started yet");
        }
    }

    public boolean isSimulated() {
        return simulated;
    }
}
