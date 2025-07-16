package ru.yandex.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.request.model.ParticipationRequest;

import java.util.List;
import java.util.Optional;


public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {


     Long countByEventIdAndStatus(Long eventId, ParticipationRequest.RequestStatus status);

     List<ParticipationRequest> findByEventIdInAndStatus(List<Long> eventIds, ParticipationRequest.RequestStatus status);

     List<ParticipationRequest> findAllByEventId(Long eventId);

     List<ParticipationRequest> findAllByIdIn(List<Long> ids);

     Optional<ParticipationRequest> findByIdAndEventId(Long requestId, Long eventId);

     List<ParticipationRequest> findAllByRequesterId(Long requesterId);

     List<ParticipationRequest> findAllByEventIdAndRequesterId(Long eventId, Long requesterId);
}

