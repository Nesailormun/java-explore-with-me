package ru.yandex.practicum.request.dto;

import lombok.*;
import ru.yandex.practicum.request.model.ParticipationRequest;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateRequest {

    private List<Long> requestIds;
    private ParticipationRequest.RequestStatus status;
}
