package dev.vorstu.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StudentTeacherUpdateDto {
    @Size(min = 2, max = 100, message = "ФИО должно содержать от 2 до 100 символов")
    private String fio;

    private String groupName;
}
