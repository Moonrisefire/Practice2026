package dev.vorstu.services.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.function.DoubleSupplier;

@Component
public class EmailFailureSimulator {

    private final double simulateFailureRate;
    private final DoubleSupplier randomSupplier;

    @Autowired
    public EmailFailureSimulator(
            @Value("${email.simulate-failure-rate:0.3}") double simulateFailureRate) {
        this(simulateFailureRate, new Random()::nextDouble);
    }

    EmailFailureSimulator(double simulateFailureRate, DoubleSupplier randomSupplier) {
        this.simulateFailureRate = simulateFailureRate;
        this.randomSupplier = randomSupplier;
    }

    public boolean shouldSimulateFailure() {
        return simulateFailureRate > 0 && randomSupplier.getAsDouble() < simulateFailureRate;
    }
}
