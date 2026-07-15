package dev.vorstu.dto;

import dev.vorstu.models.EmailDeliveryStatus;
import dev.vorstu.models.RegistrationStatus;
import dev.vorstu.models.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RegistrationRequestDto {
    private Long id;
    private String fio;
    private String email;
    private String groupName;
    private Role role;
    private RegistrationStatus status;
    private LocalDateTime expiresAt;
    private EmailDeliveryStatus emailStatus;
    private int emailAttempts;
    private String lastEmailError;
}
