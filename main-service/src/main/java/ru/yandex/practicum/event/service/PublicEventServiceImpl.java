package ru.yandex.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CreateEndpointHitDto;
import ru.yandex.practicum.client.StatsClient;
import ru.yandex.practicum.event.dto.EventFullDto;
import ru.yandex.practicum.event.dto.EventShortDto;
import ru.yandex.practicum.event.mapper.EventMapper;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.event.repository.CustomEventRepository;
import ru.yandex.practicum.event.repository.EventRepository;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.request.model.ParticipationRequest;
import ru.yandex.practicum.request.repository.ParticipationRequestRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PublicEventServiceImpl implements PublicEventService {

    private final EventRepository eventRepository;
    private final CustomEventRepository customEventRepository;
    private final ParticipationRequestRepository requestRepository;
    private final StatsClient statsClient;
    private final EventMapper eventMapper;

    @Override
    public List<EventShortDto> getPublicEvents(String text,
                                               List<Long> categories,
                                               Boolean paid,
                                               LocalDateTime rangeStart,
                                               LocalDateTime rangeEnd,
                                               Boolean onlyAvailable,
                                               String sort,
                                               Integer from,
                                               Integer size,
                                               HttpServletRequest request) {

        log.info("Retrieving public events with filter");
        saveStats(request);

        List<Event> events = customEventRepository.searchPublicEvents(
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort,
                PageRequest.of(from / size, size));

        Map<Long, Long> confirmedRequests = getConfirmedRequestsCount(events);
        Map<Long, Long> views = getViewsCount(events);

        return events.stream()
                .map(event -> {
                    EventShortDto dto = eventMapper.toShortDto(event);
                    dto.setConfirmedRequests(confirmedRequests.getOrDefault(event.getId(), 0L));
                    dto.setViews(views.getOrDefault(event.getId(), 0L));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto getPublicEventById(Long eventId, HttpServletRequest request) {

        log.info("Retrieving public event with id={}", eventId);
        Event event = eventRepository.findByIdAndState(eventId, Event.EventState.PUBLISHED)
                .orElseThrow(() -> {
                    log.warn("Could not find public event with id={}", eventId);
                    return new NotFoundException("Event with id=" + eventId + " was not found");
                });

        saveStats(request);

        Long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId,
                ParticipationRequest.RequestStatus.CONFIRMED);

        Long views = statsClient.getEventViews(eventId);

        EventFullDto dto = eventMapper.toFullDto(event);
        dto.setConfirmedRequests(confirmedRequests);
        dto.setViews(views);
        return dto;
    }

    private void saveStats(HttpServletRequest request) {
        statsClient.saveEndpointHit(CreateEndpointHitDto.builder()
                .app("ewm-main-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build());
    }

    private Map<Long, Long> getConfirmedRequestsCount(List<Event> events) {
        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();

        return requestRepository.findByEventIdInAndStatus(eventIds, ParticipationRequest.RequestStatus.CONFIRMED)
                .stream()
                .map(ParticipationRequest::getEvent)
                .collect(Collectors.groupingBy(
                        Event::getId,
                        Collectors.counting()
                ));
    }

    private Map<Long, Long> getViewsCount(List<Event> events) {
        return events.stream()
                .collect(Collectors.toMap(
                        Event::getId,
                        event -> statsClient.getEventViews(event.getId())
                ));
    }
}