package dev.vorstu.services.email;

import dev.vorstu.dto.RegistrationEmailMessage;
import dev.vorstu.models.EmailDeliveryStatus;
import dev.vorstu.models.RegistrationRequest;
import dev.vorstu.repositories.RegistrationRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "email.dispatch.mode", havingValue = "kafka", matchIfMissing = true)
public class KafkaRegistrationEmailDispatcher implements RegistrationEmailDispatcher {

    private final KafkaTemplate<String, RegistrationEmailMessage> kafkaTemplate;
    private final RegistrationRequestRepository registrationRequestRepository;

    @Value("${email.kafka.topic}")
    private String topic;

    @Override
    @Transactional
    public void dispatch(Long requestId, String email, String token) {
        RegistrationRequest request = registrationRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена: " + requestId));

        request.setEmailStatus(EmailDeliveryStatus.PENDING);
        registrationRequestRepository.save(request);

        RegistrationEmailMessage message = RegistrationEmailMessage.builder()
                .requestId(requestId)
                .email(email)
                .token(token)
                .attempt(0)
                .build();

        kafkaTemplate.send(topic, String.valueOf(requestId), message);
        log.info("Сообщение для отправки письма заявки {} опубликовано в Kafka", requestId);
    }
}
