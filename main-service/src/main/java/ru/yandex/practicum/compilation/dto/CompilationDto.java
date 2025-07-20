package ru.yandex.practicum.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.yandex.practicum.event.dto.EventShortDto;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompilationDto {

    @NotNull
    @Positive
    private Long id;

    @NotBlank
    @Size(min = 1, max = 50)
    private String title;
    @NotNull
    private Boolean pinned;
    private List<EventShortDto> events;
}
