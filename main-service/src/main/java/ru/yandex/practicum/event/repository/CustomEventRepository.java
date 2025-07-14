package ru.yandex.practicum.event.repository;

import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.event.model.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface CustomEventRepository {

    List<Event> searchPublicEvents(String text,
                                   List<Long> categoryIds,
                                   Boolean paid,
                                   LocalDateTime rangeStart,
                                   LocalDateTime rangeEnd,
                                   Boolean onlyAvailable,
                                   String sort,
                                   Pageable pageable);

    List<Event> searchAdminEvents(List<Long> userIds,
                                  List<Event.EventState> states,
                                  List<Long> categoryIds,
                                  LocalDateTime rangeStart,
                                  LocalDateTime rangeEnd,
                                  Pageable pageable);

}
