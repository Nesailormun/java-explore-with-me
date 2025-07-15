package ru.yandex.practicum.request.service;

import ru.yandex.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

public class ParticipationRequestServiceImpl implements ParticipationRequestService {
    @Override
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        return null;
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        return List.of();
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        return null;
    }
}
