package dev.vorstu.services.email;

import dev.vorstu.models.EmailDeliveryStatus;
import dev.vorstu.models.RegistrationRequest;
import dev.vorstu.models.RegistrationStatus;
import dev.vorstu.models.Role;
import dev.vorstu.repositories.RegistrationRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkerRegistrationEmailDispatcherTest {

    @Mock
    private RegistrationRequestRepository registrationRequestRepository;

    @InjectMocks
    private WorkerRegistrationEmailDispatcher dispatcher;

    private RegistrationRequest request;

    @BeforeEach
    void setUp() {
        request = new RegistrationRequest();
        request.setId(1L);
        request.setEmail("user@test.com");
        request.setToken("token-123");
        request.setStatus(RegistrationStatus.PENDING);
        request.setEmailStatus(EmailDeliveryStatus.FAILED);
        request.setRole(Role.STUDENT);
        request.setExpiresAt(LocalDateTime.now().plusHours(24));
    }

    @Test
    void dispatch_setsPendingStatus() {
        when(registrationRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(registrationRequestRepository.save(request)).thenReturn(request);

        dispatcher.dispatch(1L, "user@test.com", "token-123");

        assertEquals(EmailDeliveryStatus.PENDING, request.getEmailStatus());
        verify(registrationRequestRepository).save(request);
    }
}
