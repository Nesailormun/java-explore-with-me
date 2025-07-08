package ru.yandex.practicum.request.dto;

import lombok.*;
import ru.yandex.practicum.request.model.ParticipationRequest;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationRequestDto {
    private Long id;
    private Long event;
    private Long requester;
    private LocalDateTime created;
    private ParticipationRequest.RequestStatus status;
}
