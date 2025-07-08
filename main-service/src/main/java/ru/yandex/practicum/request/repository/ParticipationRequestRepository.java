package ru.yandex.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.request.model.ParticipationRequest;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {
}
