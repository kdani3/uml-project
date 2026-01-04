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

    public LocalDateTime now() {
        ensureSimulationStarted();
        return LocalDateTime.now(clock);
    }

    public LocalDate today() {
        ensureSimulationStarted();
        return LocalDate.now(clock);
    }

    //start simulation
    public void startSimulation() {
        clock.startSimulation();
    }

    //advance time by days
    public void advanceDays(long days) {
        ensureSimulationStarted();  //ensure simulation is started before advancing time
        clock.advanceDays(days);
    }

    //advance time by hours
    public void advanceHours(long hours) {
        ensureSimulationStarted();  //ensure simulation is started before advancing time
        clock.advanceHours(hours);
    }

    //stop simulation and return to real time
    public void stopSimulation() {
        clock.stopSimulation();
    }

    //ensure simulation has started before using time methods
    private void ensureSimulationStarted() {

        if (!clock.isSimulated()) {
            startSimulation();
        }
    }

    public boolean isSimulated() {
        return clock.isSimulated();
    }
}
