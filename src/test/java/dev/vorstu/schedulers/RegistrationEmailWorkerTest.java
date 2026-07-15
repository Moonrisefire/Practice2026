package dev.vorstu.schedulers;

import dev.vorstu.exceptions.EmailDeliveryException;
import dev.vorstu.models.EmailDeliveryStatus;
import dev.vorstu.models.RegistrationRequest;
import dev.vorstu.models.RegistrationStatus;
import dev.vorstu.models.Role;
import dev.vorstu.repositories.RegistrationRequestRepository;
import dev.vorstu.services.email.RegistrationEmailSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationEmailWorkerTest {

    @Mock
    private RegistrationRequestRepository registrationRequestRepository;
    @Mock
    private RegistrationEmailSender registrationEmailSender;

    @InjectMocks
    private RegistrationEmailWorker worker;

    private RegistrationRequest request;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(worker, "maxRetries", 3);

        request = new RegistrationRequest();
        request.setId(1L);
        request.setEmail("user@test.com");
        request.setToken("token-123");
        request.setStatus(RegistrationStatus.PENDING);
        request.setEmailStatus(EmailDeliveryStatus.PENDING);
        request.setEmailAttempts(0);
        request.setRole(Role.STUDENT);
        request.setExpiresAt(LocalDateTime.now().plusHours(24));
    }

    @Test
    void processPendingEmails_sendsEmails() {
        when(registrationRequestRepository.findByEmailStatusInAndEmailAttemptsLessThan(any(), anyInt()))
                .thenReturn(List.of(request));

        worker.processPendingEmails();

        verify(registrationEmailSender).send(1L, "user@test.com", "token-123");
    }

    @Test
    void processPendingEmails_noPending() {
        when(registrationRequestRepository.findByEmailStatusInAndEmailAttemptsLessThan(any(), anyInt()))
                .thenReturn(List.of());

        worker.processPendingEmails();

        verify(registrationEmailSender, never()).send(any(), any(), any());
    }

    @Test
    void processPendingEmails_handlesFailure() {
        when(registrationRequestRepository.findByEmailStatusInAndEmailAttemptsLessThan(any(), anyInt()))
                .thenReturn(List.of(request));
        doThrow(new EmailDeliveryException("fail"))
                .when(registrationEmailSender).send(1L, "user@test.com", "token-123");

        worker.processPendingEmails();

        verify(registrationEmailSender).send(1L, "user@test.com", "token-123");
    }
}
