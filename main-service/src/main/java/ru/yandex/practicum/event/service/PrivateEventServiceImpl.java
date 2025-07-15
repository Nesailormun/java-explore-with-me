package ru.yandex.practicum.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.yandex.practicum.category.model.Category;
import ru.yandex.practicum.category.repository.CategoryRepository;
import ru.yandex.practicum.event.dto.EventFullDto;
import ru.yandex.practicum.event.dto.EventShortDto;
import ru.yandex.practicum.event.dto.NewEventDto;
import ru.yandex.practicum.event.dto.UpdateEventRequest;
import ru.yandex.practicum.event.mapper.EventMapper;
import ru.yandex.practicum.event.mapper.LocationMapper;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.event.repository.EventRepository;
import ru.yandex.practicum.exception.ConflictException;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.user.model.User;
import ru.yandex.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PrivateEventServiceImpl implements PrivateEventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;


    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto dto) {

        log.info("Create new event by user={}, dto={}", userId, dto.toString());
        User user = checkAndGetUser(userId);

        Category category = checkAndGetCategory(dto.getCategory());

        Event event = eventMapper.fromDto(dto);
        event.setInitiator(user);
        event.setCategory(category);
        eventRepository.save(event);
        return eventMapper.toFullDto(event);
    }


    @Override
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {

        log.info("Getting events by user={}, from={}, size={}", userId, from, size);
        checkAndGetUser(userId);
        return eventRepository.findByInitiatorId(userId, PageRequest.of(from / size, size)).stream()
                .map(eventMapper::toShortDto)
                .toList();
    }

    @Override
    public EventFullDto getUserEvent(Long userId, Long eventId) {

        log.info("Getting event with id={} by user={}", eventId, userId);
        checkAndGetUser(userId);
        return eventMapper.toFullDto(checkAndGetEvent(eventId));
    }

    @Override
    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventRequest request) {

        log.info("Updating event with id={} by user={}; body={}", eventId, userId, request);
        checkAndGetUser(userId);
        Event event = checkAndGetEvent(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            log.error("Event with id {} has another initiator", eventId);
            throw new ConflictException("Access denied to update event");
        }

        if (event.getState() == Event.EventState.PUBLISHED) {
            log.error("Event with id {} has already published", eventId);
            throw new ConflictException("Cannot update published event");
        }

        if (request.getEventDate() != null &&
                request.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            log.error("Event with id={} has incorrect eventDate={}", eventId, event.getEventDate());
            throw new ConflictException("Event date must be at least 2 hours in the future");
        }
        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getCategory() != null) {
            event.setCategory(checkAndGetCategory(request.getCategory()));
        }
        if (request.getLocation() != null) {
            event.setLocation(locationMapper.toEntity(request.getLocation()));
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
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
        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case "SEND_TO_REVIEW":
                    event.setState(Event.EventState.PENDING);
                    break;
                case "CANCEL_REVIEW":
                    event.setState(Event.EventState.CANCELED);
                    break;
                default:
                    log.error("Unknown event state action={}", request.getStateAction());
                    throw new ConflictException("Invalid state action");
            }
        }
        return eventMapper.toFullDto(eventRepository.save(event));
    }

    private Category checkAndGetCategory(Long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(
                () -> {
                    log.error("Category with id {} not found", categoryId);
                    return new NotFoundException("Category with id " + categoryId + " not found");
                }
        );
    }

    private User checkAndGetUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> {
                    log.error("User with id {} not found", userId);
                    return new NotFoundException("User with id " + userId + " not found");
                }
        );
    }

    private Event checkAndGetEvent(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(
                () -> {
                    log.error("Event with id {} not found", eventId);
                    return new NotFoundException("Event with id " + eventId + " not found");
                }
        );
    }
}
