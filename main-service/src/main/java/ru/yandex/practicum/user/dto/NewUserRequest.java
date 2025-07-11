package ru.yandex.practicum.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewUserRequest {


    @NotBlank(message = "Имя пользователя не должно быть пустым")
    private String name;
    @NotBlank(message = "email должен быть указан")
    private String email;
}
