package dev.vorstu.services.email;

import dev.vorstu.dto.RegistrationEmailMessage;
import dev.vorstu.models.EmailDeliveryStatus;
import dev.vorstu.models.RegistrationRequest;
import dev.vorstu.models.RegistrationStatus;
import dev.vorstu.models.Role;
import dev.vorstu.repositories.RegistrationRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaRegistrationEmailDispatcherTest {

    @Mock
    private KafkaTemplate<String, RegistrationEmailMessage> kafkaTemplate;
    @Mock
    private RegistrationRequestRepository registrationRequestRepository;

    @InjectMocks
    private KafkaRegistrationEmailDispatcher dispatcher;

    private RegistrationRequest request;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(dispatcher, "topic", "registration-emails");

        request = new RegistrationRequest();
        request.setId(1L);
        request.setEmail("user@test.com");
        request.setToken("token-123");
        request.setStatus(RegistrationStatus.PENDING);
        request.setRole(Role.STUDENT);
        request.setExpiresAt(LocalDateTime.now().plusHours(24));
    }

    @Test
    void dispatch_publishesMessage() {
        when(registrationRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(registrationRequestRepository.save(request)).thenReturn(request);

        dispatcher.dispatch(1L, "user@test.com", "token-123");

        assertEquals(EmailDeliveryStatus.PENDING, request.getEmailStatus());

        ArgumentCaptor<RegistrationEmailMessage> captor = ArgumentCaptor.forClass(RegistrationEmailMessage.class);
        verify(kafkaTemplate).send(eq("registration-emails"), eq("1"), captor.capture());

        RegistrationEmailMessage message = captor.getValue();
        assertEquals(1L, message.getRequestId());
        assertEquals("user@test.com", message.getEmail());
        assertEquals("token-123", message.getToken());
        assertEquals(0, message.getAttempt());
    }
}
