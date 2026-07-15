package dev.vorstu.services.email;

import dev.vorstu.models.EmailDeliveryStatus;
import dev.vorstu.models.RegistrationRequest;
import dev.vorstu.repositories.RegistrationRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "email.dispatch.mode", havingValue = "worker")
public class WorkerRegistrationEmailDispatcher implements RegistrationEmailDispatcher {

    private final RegistrationRequestRepository registrationRequestRepository;

    @Override
    @Transactional
    public void dispatch(Long requestId, String email, String token) {
        RegistrationRequest request = registrationRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена: " + requestId));

        request.setEmailStatus(EmailDeliveryStatus.PENDING);
        registrationRequestRepository.save(request);
        log.info("Заявка {} поставлена в очередь worker для отправки письма на {}", requestId, email);
    }
}
