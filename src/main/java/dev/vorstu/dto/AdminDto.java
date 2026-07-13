package dev.vorstu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AdminDto {
    private Long id;

    @NotBlank(message = "Имя пользователя обязательно")
    private String username;

    @Pattern(regexp = "^$|^.{4,}$", message = "Пароль должен быть не менее 4 символов")
    private String password;
}
