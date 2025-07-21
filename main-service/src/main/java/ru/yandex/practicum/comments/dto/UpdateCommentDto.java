package ru.yandex.practicum.comments.dto;

import lombok.*;
import jakarta.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCommentDto {

    @Size(min = 1, max = 1000)
    private String text;
}
