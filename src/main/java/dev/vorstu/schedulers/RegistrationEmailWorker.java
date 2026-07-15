package dev.vorstu.schedulers;

import dev.vorstu.models.EmailDeliveryStatus;
import dev.vorstu.models.RegistrationRequest;
import dev.vorstu.repositories.RegistrationRequestRepository;
import dev.vorstu.services.email.RegistrationEmailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "email.dispatch.mode", havingValue = "worker")
public class RegistrationEmailWorker {

    private final RegistrationRequestRepository registrationRequestRepository;
    private final RegistrationEmailSender registrationEmailSender;

    @Value("${email.kafka.max-retries:3}")
    private int maxRetries;

    @Scheduled(fixedDelayString = "${email.worker.fixed-delay-ms:10000}")
    public void processPendingEmails() {
        List<RegistrationRequest> pending = registrationRequestRepository
                .findByEmailStatusInAndEmailAttemptsLessThan(
                        List.of(EmailDeliveryStatus.PENDING, EmailDeliveryStatus.FAILED),
                        maxRetries);

        if (pending.isEmpty()) {
            return;
        }

        log.info("Worker: найдено {} заявок для отправки письма", pending.size());

        for (RegistrationRequest request : pending) {
            try {
                registrationEmailSender.send(request.getId(), request.getEmail(), request.getToken());
            } catch (Exception e) {
                log.warn("Worker: не удалось отправить письмо для заявки {}: {}",
                        request.getId(), e.getMessage());
            }
        }
    }
}
