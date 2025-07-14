package ru.yandex.practicum.event.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.event.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    boolean existsByCategoryId(Long categoryId);

    @EntityGraph(attributePaths = {"initiator", "category"})
    Optional<Event> findById(Long id);

    @EntityGraph(attributePaths = {"initiator", "category"})
    List<Event> findByIdIn(List<Long> ids);

    @EntityGraph(attributePaths = {"initiator", "category"})
    List<Event> findByInitiatorId(Long initiatorId, Pageable pageable);

    @EntityGraph(attributePaths = {"initiator", "category"})
    List<Event> findByCategoryId(Long categoryId);

    List<Event> findByState(Event.EventState state);

    @EntityGraph(attributePaths = {"initiator", "category"})
    List<Event> findByStateAndEventDateAfter(Event.EventState state, LocalDateTime after, Pageable pageable);

    Optional<Event> findByIdAndState(Long id, Event.EventState state);
}
