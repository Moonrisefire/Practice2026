package dev.vorstu.services.email;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailFailureSimulatorTest {

    @Test
    void shouldSimulateFailure_rateZero() {
        EmailFailureSimulator simulator = new EmailFailureSimulator(0.0, () -> 0.5);
        assertFalse(simulator.shouldSimulateFailure());
    }

    @Test
    void shouldSimulateFailure_rateOne() {
        EmailFailureSimulator simulator = new EmailFailureSimulator(1.0, () -> 0.5);
        assertTrue(simulator.shouldSimulateFailure());
    }

    @Test
    void shouldSimulateFailure_belowThreshold() {
        EmailFailureSimulator simulator = new EmailFailureSimulator(0.5, () -> 0.3);
        assertTrue(simulator.shouldSimulateFailure());
    }

    @Test
    void shouldSimulateFailure_aboveThreshold() {
        EmailFailureSimulator simulator = new EmailFailureSimulator(0.5, () -> 0.7);
        assertFalse(simulator.shouldSimulateFailure());
    }
}
