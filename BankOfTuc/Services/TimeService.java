package BankOfTuc.Services;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TimeService {

    private static final TimeService INSTANCE = new TimeService();
    private final SimulatedClock clock = new SimulatedClock();

    private TimeService() {}

    public static TimeService getInstance() {
        return INSTANCE;
    }

    public Clock clock() {
        return clock;
    }

    // Start the simulation before calling this
    public LocalDateTime now() {
        ensureSimulationStarted();
        return LocalDateTime.now(clock);
    }

    // Start the simulation before calling this
    public LocalDate today() {
        ensureSimulationStarted();
        return LocalDate.now(clock);
    }

    // Start simulation
    public void startSimulation() {
        clock.startSimulation();
    }

    // Advance time by days
    public void advanceDays(long days) {
        ensureSimulationStarted();  // Ensure simulation is started before advancing time
        clock.advanceDays(days);
    }

    // Advance time by hours
    public void advanceHours(long hours) {
        ensureSimulationStarted();  // Ensure simulation is started before advancing time
        clock.advanceHours(hours);
    }

    // Stop simulation and return to real time
    public void stopSimulation() {
        clock.stopSimulation();
    }

    // Ensure simulation has started before using time methods
    private void ensureSimulationStarted() {
        // If simulation is not started, start it automatically
        if (!clock.isSimulated()) {
            startSimulation();
        }
    }

    // Method to check if the simulation is running
    public boolean isSimulated() {
        return clock.isSimulated();
    }
}
