package dev.vorstu.services.email;

import dev.vorstu.dto.RegistrationEmailMessage;
import dev.vorstu.exceptions.EmailDeliveryException;
import dev.vorstu.exceptions.SimulatedEmailFailureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RegistrationEmailConsumerTest {

    @Mock
    private RegistrationEmailSender registrationEmailSender;
    @Mock
    private KafkaTemplate<String, RegistrationEmailMessage> kafkaTemplate;

    @InjectMocks
    private RegistrationEmailConsumer consumer;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(consumer, "retryTopic", "registration-emails-retry");
        ReflectionTestUtils.setField(consumer, "maxRetries", 3);
    }

    @Test
    void consume_success() {
        RegistrationEmailMessage message = RegistrationEmailMessage.builder()
                .requestId(1L)
                .email("user@test.com")
                .token("token-123")
                .attempt(0)
                .build();

        consumer.consume(message);

        verify(registrationEmailSender).send(1L, "user@test.com", "token-123");
        verify(kafkaTemplate, never()).send(eq("registration-emails-retry"), eq("1"), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void consume_failure_republishesToRetry() {
        RegistrationEmailMessage message = RegistrationEmailMessage.builder()
                .requestId(1L)
                .email("user@test.com")
                .token("token-123")
                .attempt(0)
                .build();

        doThrow(new EmailDeliveryException("fail"))
                .when(registrationEmailSender).send(1L, "user@test.com", "token-123");

        consumer.consume(message);

        ArgumentCaptor<RegistrationEmailMessage> captor = ArgumentCaptor.forClass(RegistrationEmailMessage.class);
        verify(kafkaTemplate).send(eq("registration-emails-retry"), eq("1"), captor.capture());
        assertEquals(1, captor.getValue().getAttempt());
    }

    @Test
    void consume_simulatedFailure_doesNotRepublishToRetry() {
        RegistrationEmailMessage message = RegistrationEmailMessage.builder()
                .requestId(1L)
                .email("user@test.com")
                .token("token-123")
                .attempt(0)
                .build();

        doThrow(new SimulatedEmailFailureException("simulated"))
                .when(registrationEmailSender).send(1L, "user@test.com", "token-123");

        consumer.consume(message);

        verify(kafkaTemplate, never()).send(eq("registration-emails-retry"), eq("1"), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void consumeRetry_maxRetriesExceeded() {
        RegistrationEmailMessage message = RegistrationEmailMessage.builder()
                .requestId(1L)
                .email("user@test.com")
                .token("token-123")
                .attempt(3)
                .build();

        doThrow(new EmailDeliveryException("fail"))
                .when(registrationEmailSender).send(1L, "user@test.com", "token-123");

        consumer.consumeRetry(message);

        verify(kafkaTemplate, never()).send(eq("registration-emails-retry"), eq("1"), org.mockito.ArgumentMatchers.any());
    }
}
