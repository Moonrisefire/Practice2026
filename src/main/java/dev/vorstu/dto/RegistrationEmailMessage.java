package dev.vorstu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationEmailMessage {
    private Long requestId;
    private String email;
    private String token;
    private int attempt;
}
