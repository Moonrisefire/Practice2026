package dev.vorstu.services.email;

public interface RegistrationEmailDispatcher {
    void dispatch(Long requestId, String email, String token);
}
