package dev.vorstu.services.email;

import dev.vorstu.exceptions.EmailDeliveryException;
import dev.vorstu.exceptions.SimulatedEmailFailureException;
import dev.vorstu.exceptions.ResourceNotFoundException;
import dev.vorstu.models.EmailDeliveryStatus;
import dev.vorstu.models.RegistrationRequest;
import dev.vorstu.models.RegistrationStatus;
import dev.vorstu.models.Role;
import dev.vorstu.repositories.RegistrationRequestRepository;
import dev.vorstu.services.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationEmailSenderTest {

    @Mock
    private RegistrationRequestRepository registrationRequestRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private EmailFailureSimulator emailFailureSimulator;

    @InjectMocks
    private RegistrationEmailSender registrationEmailSender;

    private RegistrationRequest request;

    @BeforeEach
    void setUp() {
        request = new RegistrationRequest();
        request.setId(1L);
        request.setEmail("user@test.com");
        request.setToken("token-123");
        request.setStatus(RegistrationStatus.PENDING);
        request.setEmailStatus(EmailDeliveryStatus.PENDING);
        request.setEmailAttempts(0);
        request.setExpiresAt(LocalDateTime.now().plusHours(24));
        request.setRole(Role.STUDENT);
    }

    @Test
    void send_success() {
        when(registrationRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(emailFailureSimulator.shouldSimulateFailure()).thenReturn(false);

        registrationEmailSender.send(1L, "user@test.com", "token-123");

        verify(emailService).sendRegistrationLink("user@test.com", "token-123");
        assertEquals(EmailDeliveryStatus.SENT, request.getEmailStatus());
        verify(registrationRequestRepository).save(request);
    }

    @Test
    void send_simulatedFailure() {
        when(registrationRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(emailFailureSimulator.shouldSimulateFailure()).thenReturn(true);

        assertThrows(SimulatedEmailFailureException.class,
                () -> registrationEmailSender.send(1L, "user@test.com", "token-123"));

        assertEquals(EmailDeliveryStatus.FAILED, request.getEmailStatus());
        assertEquals(1, request.getEmailAttempts());
        verify(emailService, never()).sendRegistrationLink(any(), any());
    }

    @Test
    void send_emailServiceFailure() {
        when(registrationRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(emailFailureSimulator.shouldSimulateFailure()).thenReturn(false);
        doThrow(new RuntimeException("SMTP error"))
                .when(emailService).sendRegistrationLink("user@test.com", "token-123");

        assertThrows(EmailDeliveryException.class,
                () -> registrationEmailSender.send(1L, "user@test.com", "token-123"));

        assertEquals(EmailDeliveryStatus.FAILED, request.getEmailStatus());
        assertEquals(1, request.getEmailAttempts());
    }

    @Test
    void send_requestNotFound() {
        when(registrationRequestRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> registrationEmailSender.send(1L, "user@test.com", "token-123"));
    }

    @Test
    void send_alreadySent() {
        request.setEmailStatus(EmailDeliveryStatus.SENT);
        when(registrationRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        registrationEmailSender.send(1L, "user@test.com", "token-123");

        verify(emailService, never()).sendRegistrationLink(any(), any());
    }
}
