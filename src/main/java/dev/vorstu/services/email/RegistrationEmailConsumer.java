package dev.vorstu.services.email;

import dev.vorstu.dto.RegistrationEmailMessage;
import dev.vorstu.exceptions.EmailDeliveryException;
import dev.vorstu.exceptions.SimulatedEmailFailureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "email.dispatch.mode", havingValue = "kafka", matchIfMissing = true)
public class RegistrationEmailConsumer {

    private final RegistrationEmailSender registrationEmailSender;
    private final KafkaTemplate<String, RegistrationEmailMessage> kafkaTemplate;

    @Value("${email.kafka.retry-topic}")
    private String retryTopic;

    @Value("${email.kafka.max-retries:3}")
    private int maxRetries;

    @KafkaListener(topics = "${email.kafka.topic}", groupId = "registration-email-group",
            containerFactory = "registrationEmailKafkaListenerContainerFactory")
    public void consume(RegistrationEmailMessage message) {
        processMessage(message);
    }

    @KafkaListener(topics = "${email.kafka.retry-topic}", groupId = "registration-email-retry-group",
            containerFactory = "registrationEmailKafkaListenerContainerFactory")
    public void consumeRetry(RegistrationEmailMessage message) {
        processMessage(message);
    }

    private void processMessage(RegistrationEmailMessage message) {
        try {
            registrationEmailSender.send(message.getRequestId(), message.getEmail(), message.getToken());
        } catch (SimulatedEmailFailureException e) {
            log.warn("Симулированная ошибка для заявки {} (без auto-retry): {}",
                    message.getRequestId(), e.getMessage());
        } catch (EmailDeliveryException e) {
            int nextAttempt = message.getAttempt() + 1;
            if (nextAttempt < maxRetries) {
                RegistrationEmailMessage retryMessage = RegistrationEmailMessage.builder()
                        .requestId(message.getRequestId())
                        .email(message.getEmail())
                        .token(message.getToken())
                        .attempt(nextAttempt)
                        .build();
                kafkaTemplate.send(retryTopic, String.valueOf(message.getRequestId()), retryMessage);
                log.info("Сообщение для заявки {} отправлено в retry-топик (попытка {})",
                        message.getRequestId(), nextAttempt);
            } else {
                log.warn("Исчерпаны попытки отправки письма для заявки {}", message.getRequestId());
            }
        }
    }
}
