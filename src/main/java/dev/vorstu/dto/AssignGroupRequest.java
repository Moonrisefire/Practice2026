package dev.vorstu.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssignGroupRequest {
    @NotBlank(message = "Название группы обязательно")
    private String groupName;
}
