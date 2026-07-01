package dev.vorstu.dto;

import dev.vorstu.models.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StudentDto {
    private Long id;
    private String username;
    private Role role;

    @NotBlank(message = "ФИО не может быть пустым")
    @Size(min = 2, max = 100, message = "ФИО должно содержать от 2 до 100 символов")
    private String fio;

    @NotBlank(message = "Название группы не может быть пустым")
    private String groupName;

    @NotBlank(message = "Пароль обязателен при создании")
    @Size(min = 4, message = "Пароль должен быть не менее 4 символов")
    private String password;
}