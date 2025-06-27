package ru.practicum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEndpointHitDto {

    @NotNull(message = "Service name should not be null")
    @NotBlank(message = "Service name should not be empty")
    private String app;
    @NotNull(message = "URI of request should not be null")
    @NotBlank(message = "URI of request should not be empty")
    private String uri;
    @NotBlank(message = "Users IP should not be empty")
    @NotNull(message = "Users IP should not be null")
    private String ip;
    @NotNull(message = "Timestamp should not be null")
    private LocalDateTime timestamp;
}

