package dev.vorstu.dto;

import dev.vorstu.models.Role;
import lombok.Data;

@Data
public class StudentDto {
    private Long id;
    private String username;
    private Role role;
    private String fio;
    private String groupName;
}
