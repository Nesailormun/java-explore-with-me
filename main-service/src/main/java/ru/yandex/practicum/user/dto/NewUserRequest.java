package ru.yandex.practicum.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewUserRequest {

    @NotBlank(message = "Имя пользователя не должно быть пустым")
    @Size(min = 2, max = 250)
    private String name;
    @NotBlank(message = "email должен быть указан")
    @Size(min = 6, max = 254)
    @Email
    private String email;
}
