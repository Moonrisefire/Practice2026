package dev.vorstu.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConfirmRegistrationResponse {
    private String message;
    private String username;
    private String temporaryPassword;
}
