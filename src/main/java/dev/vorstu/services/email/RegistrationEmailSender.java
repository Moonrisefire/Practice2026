package dev.vorstu.services.email;

import dev.vorstu.exceptions.EmailDeliveryException;
import dev.vorstu.exceptions.SimulatedEmailFailureException;
import dev.vorstu.exceptions.ResourceNotFoundException;
import dev.vorstu.models.EmailDeliveryStatus;
import dev.vorstu.models.RegistrationRequest;
import dev.vorstu.models.RegistrationStatus;
import dev.vorstu.repositories.RegistrationRequestRepository;
import dev.vorstu.services.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationEmailSender {

    private final RegistrationRequestRepository registrationRequestRepository;
    private final EmailService emailService;
    private final EmailFailureSimulator emailFailureSimulator;

    @Transactional
    public void send(Long requestId, String email, String token) {
        RegistrationRequest request = registrationRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Заявка не найдена"));

        if (request.getStatus() != RegistrationStatus.PENDING) {
            throw new EmailDeliveryException("Заявка не ожидает подтверждения");
        }

        if (request.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new EmailDeliveryException("Срок действия заявки истёк");
        }

        if (request.getEmailStatus() == EmailDeliveryStatus.SENT) {
            log.info("Письмо для заявки {} уже отправлено", requestId);
            return;
        }

        try {
            if (emailFailureSimulator.shouldSimulateFailure()) {
                throw new SimulatedEmailFailureException("Симулированная ошибка отправки письма");
            }

            emailService.sendRegistrationLink(email, token);
            request.setEmailStatus(EmailDeliveryStatus.SENT);
            request.setLastEmailError(null);
            registrationRequestRepository.save(request);
            log.info("Письмо для заявки {} успешно отправлено", requestId);
        } catch (SimulatedEmailFailureException e) {
            markFailed(request, requestId, e.getMessage());
            throw e;
        } catch (Exception e) {
            markFailed(request, requestId, e.getMessage());
            log.warn("Ошибка отправки письма для заявки {}: {}", requestId, e.getMessage());
            throw new EmailDeliveryException(
                    e.getMessage() != null ? e.getMessage() : "Ошибка отправки письма", e);
        }
    }

    private void markFailed(RegistrationRequest request, Long requestId, String message) {
        request.setEmailStatus(EmailDeliveryStatus.FAILED);
        request.setEmailAttempts(request.getEmailAttempts() + 1);
        request.setLastEmailError(truncate(message));
        registrationRequestRepository.save(request);
    }

    private String truncate(String message) {
        if (message == null) {
            return null;
        }
        return message.length() > 500 ? message.substring(0, 500) : message;
    }
}
