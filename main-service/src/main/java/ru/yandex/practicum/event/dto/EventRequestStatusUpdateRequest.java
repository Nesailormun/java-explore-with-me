package ru.yandex.practicum.event.dto;

import lombok.*;
import ru.yandex.practicum.request.model.ParticipationRequest;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateRequest {

    private List<Long> requestIds;
    private ParticipationRequest.RequestStatus status;
}
