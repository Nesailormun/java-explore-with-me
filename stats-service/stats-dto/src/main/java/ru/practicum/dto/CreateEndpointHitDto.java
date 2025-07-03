package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NotNull
public class CreateEndpointHitDto {

    @NotNull(message = "Название сервиса не должно быть null")
    @NotBlank(message = "Название сервиса не должно быть пустым.")
    private String app;

    @NotNull(message = "URI не должен быть null")
    @NotBlank(message = "URI не должен быть пустым")
    private String uri;

    @NotBlank(message = "IP пользователя не должен быть пустым")
    @NotNull(message = "IP пользователя не должен быть null")
    private String ip;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "Время создания не должен быть null")
    private LocalDateTime timestamp;
}

