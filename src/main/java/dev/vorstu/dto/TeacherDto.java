package dev.vorstu.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.Set;

@Data
public class TeacherDto {
    private Long id;

    @NotBlank(message = "ФИО не может быть пустым")
    private String fio;

    @Email(message = "Некорректный формат email адреса")
    @NotBlank(message = "Email обязателен")
    private String email;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Некорректный формат номера телефона")
    private String phone;

    private Set<String> assignedGroups;

    @NotBlank(message = "Пароль обязателен при создании")
    @Size(min = 4, message = "Пароль должен быть не менее 4 символов")
    private String password;
}