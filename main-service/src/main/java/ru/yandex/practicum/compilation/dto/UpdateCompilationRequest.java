package ru.yandex.practicum.compilation.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCompilationRequest {

    @Size(min = 1, max = 50)
    private String title;

    private Boolean pinned;
    private List<Long> events;
}
