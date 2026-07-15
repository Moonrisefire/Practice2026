package dev.vorstu.dto;

import dev.vorstu.models.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StudentCreateDto {
    @NotBlank(message = "Имя пользователя обязательно")
    private String username;

    @NotBlank(message = "ФИО не может быть пустым")
    @Size(min = 2, max = 100, message = "ФИО должно содержать от 2 до 100 символов")
    private String fio;

    @NotBlank(message = "Название группы не может быть пустым")
    private String groupName;

    @NotBlank(message = "Пароль обязателен при создании")
    @Size(min = 4, message = "Пароль должен быть не менее 4 символов")
    private String password;

    public StudentDto toStudentDto() {
        StudentDto dto = new StudentDto();
        dto.setUsername(username);
        dto.setFio(fio);
        dto.setGroupName(groupName);
        dto.setPassword(password);
        dto.setRole(Role.STUDENT);
        return dto;
    }
}
