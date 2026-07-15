package dev.vorstu.schedulers;

import dev.vorstu.services.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegistrationExpiryScheduler {

    private final RegistrationService registrationService;

    @Scheduled(fixedRate = 3600000)
    public void revokeExpiredRegistrationRequests() {
        int count = registrationService.revokeExpiredRequests();
        if (count > 0) {
            log.info("Отозвано просроченных заявок на регистрацию: {}", count);
        }
    }
}
