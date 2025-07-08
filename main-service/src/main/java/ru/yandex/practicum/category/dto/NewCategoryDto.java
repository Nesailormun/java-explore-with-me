package ru.yandex.practicum.category.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor

public class NewCategoryDto {

    @NotBlank(message = "Название категории должно быть указано.")
    private String name;
}
