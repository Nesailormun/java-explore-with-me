package ru.yandex.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.request.model.ParticipationRequest;

import java.util.List;


public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {


     Long countByEventIdAndStatus(Long eventId, ParticipationRequest.RequestStatus status);

     List<ParticipationRequest> findByEventIdInAndStatus(List<Long> eventIds, ParticipationRequest.RequestStatus status);
}

