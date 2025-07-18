package ru.yandex.practicum.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.category.model.Category;
import ru.yandex.practicum.category.repository.CategoryRepository;
import ru.yandex.practicum.event.dto.EventFullDto;
import ru.yandex.practicum.event.dto.UpdateEventAdminRequest;
import ru.yandex.practicum.event.mapper.EventMapper;
import ru.yandex.practicum.event.mapper.LocationMapper;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.event.repository.CustomEventRepository;
import ru.yandex.practicum.event.repository.EventRepository;
import ru.yandex.practicum.exception.BadRequestException;
import ru.yandex.practicum.exception.ConflictException;
import ru.yandex.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AdminEventServiceImpl implements AdminEventService {

    private final EventRepository eventRepository;
    private final CustomEventRepository customEventRepository;
    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;
    private final CategoryRepository categoryRepository;


    @Override
    public List<EventFullDto> searchEvents(List<Long> userIds,
                                           List<String> states,
                                           List<Long> categoryIds,
                                           LocalDateTime rangeStart,
                                           LocalDateTime rangeEnd,
                                           int from,
                                           int size) {
        log.info("Admin searching for events with parameters");
        List<Event.EventState> stateEnums = null;
        if (states != null && !states.isEmpty()) {
            stateEnums = states.stream()
                    .map(Event.EventState::valueOf)
                    .toList();
        }

        List<Event> events = customEventRepository.searchAdminEvents(
                userIds,
                stateEnums,
                categoryIds,
                rangeStart,
                rangeEnd,
                PageRequest.of(from / size, size)
        );

        return events.stream()
                .map(eventMapper::toFullDto)
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest request) {

        log.info("Admin is updating event with id={}, request={}", eventId, request);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.info("Event with id={} not found", eventId);
                    return new NotFoundException("Event not found");
                });

        if (event.getState() == Event.EventState.PUBLISHED) {
            log.warn("Cannot update event. Event already published");
            throw new ConflictException("Published event cannot be changed");
        }

        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getEventDate() != null) {
            if (request.getEventDate().isBefore(LocalDateTime.now().plusHours(2L))) {
                log.error("Event with id={} has incorrect eventDate={}", eventId, event.getEventDate());
                throw new BadRequestException("Event date must be at least 2 hours in the future");
            }
            event.setEventDate(request.getEventDate());
        }
        if (request.getLocation() != null) {
            event.setLocation(locationMapper.toEntity(request.getLocation()));
        }
        if (request.getCategory() != null) {
            Category category = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> {
                        log.warn("Category with id={} not found", request.getCategory());
                        return new NotFoundException("Category not found");
                    });
            event.setCategory(category);
        }

        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case PUBLISH_EVENT:
                    if (event.getState() != Event.EventState.PENDING) {
                        log.warn("Cannot publish event. Event already published or canceled");
                        throw new ConflictException("Only pending events can be published");
                    }
                    event.setState(Event.EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case REJECT_EVENT:
                    if (event.getState() == Event.EventState.PUBLISHED) {
                        log.warn("Cannot reject event. Event already published");
                        throw new ConflictException("Cannot reject published event");
                    }
                    event.setState(Event.EventState.CANCELED);
                    break;
            }
        }

        return eventMapper.toFullDto(eventRepository.save(event));
    }
}
