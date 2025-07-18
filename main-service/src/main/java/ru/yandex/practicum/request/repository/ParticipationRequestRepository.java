package ru.yandex.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.yandex.practicum.request.model.ParticipationRequest;

import java.util.List;
import java.util.Optional;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {


    Long countByEventIdAndStatus(Long eventId, ParticipationRequest.RequestStatus status);

    List<ParticipationRequest> findByEventIdInAndStatus(List<Long> eventIds, ParticipationRequest.RequestStatus status);

    Optional<ParticipationRequest> findByRequesterIdAndEventId(Long userId, Long eventId);

    List<ParticipationRequest> findAllByIdIn(List<Long> ids);

    List<ParticipationRequest> findAllByRequesterId(Long requesterId);

    @Query("SELECT r FROM ParticipationRequest r " +
            "LEFT JOIN FETCH r.event " +
            "LEFT JOIN FETCH r.requester " +
            "WHERE r.event.id = :eventId")
    List<ParticipationRequest> findAllByEventIdWithDetails(Long eventId);
}

