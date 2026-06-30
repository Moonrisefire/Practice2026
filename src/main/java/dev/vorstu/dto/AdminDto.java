package dev.vorstu.dto;

import dev.vorstu.models.Role;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminDto {
    private Long id;
    private String username;
    private Role role;

    @NotBlank(message = "Уровень прав администратора не может быть пустым")
    private String adminLevel;
}