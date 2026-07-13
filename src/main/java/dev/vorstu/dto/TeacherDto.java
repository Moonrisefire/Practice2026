package dev.vorstu.dto;

import dev.vorstu.validation.ValidEmail;
import dev.vorstu.validation.ValidPhone;
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

    private String username;

    @ValidEmail
    @NotBlank(message = "Email обязателен")
    private String email;

    @ValidPhone
    private String phone;

    private Set<String> assignedGroups;

    @Pattern(regexp = "^$|^.{4,}$", message = "Пароль должен быть не менее 4 символов")
    private String password;
}
