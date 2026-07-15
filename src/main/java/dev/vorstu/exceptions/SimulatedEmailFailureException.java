package dev.vorstu.exceptions;

public class SimulatedEmailFailureException extends EmailDeliveryException {

    public SimulatedEmailFailureException(String message) {
        super(message);
    }
}
