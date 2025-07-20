package ru.yandex.practicum.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class NewCategoryDto {

    @NotBlank(message = "Название категории должно быть указано.")
    @Size(min = 1, max = 50)
    private String name;
}
