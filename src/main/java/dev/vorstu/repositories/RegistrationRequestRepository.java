package dev.vorstu.repositories;

import dev.vorstu.models.RegistrationRequest;
import dev.vorstu.models.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRequestRepository extends JpaRepository<RegistrationRequest, Long> {
    Optional<RegistrationRequest> findByToken(String token);
    boolean existsByEmailAndStatus(String email, RegistrationStatus status);
    List<RegistrationRequest> findByStatusAndExpiresAtBefore(RegistrationStatus status, LocalDateTime expiresAt);
}
