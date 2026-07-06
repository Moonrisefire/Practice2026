package dev.vorstu.dto;

import dev.vorstu.validation.ValidEmail;
import dev.vorstu.validation.ValidPhone;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class TeacherCreateDto {
    @NotBlank(message = "ФИО не может быть пустым")
    private String fio;

    private String username;

    @ValidEmail
    @NotBlank(message = "Email обязателен")
    private String email;

    @ValidPhone
    private String phone;

    private Set<String> assignedGroups;

    @NotBlank(message = "Пароль обязателен при создании")
    @Size(min = 4, message = "Пароль должен быть не менее 4 символов")
    private String password;

    public TeacherDto toTeacherDto() {
        TeacherDto dto = new TeacherDto();
        dto.setFio(fio);
        dto.setUsername(username);
        dto.setEmail(email);
        dto.setPhone(phone);
        dto.setAssignedGroups(assignedGroups);
        dto.setPassword(password);
        return dto;
    }
}
