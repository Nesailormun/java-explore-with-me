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
import ru.yandex.practicum.event.dto.UpdateEventUserRequest;
import ru.yandex.practicum.event.mapper.EventMapper;
import ru.yandex.practicum.event.mapper.LocationMapper;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.event.repository.EventRepository;
import ru.yandex.practicum.exception.BadRequestException;
import ru.yandex.practicum.exception.ConflictException;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.yandex.practicum.request.dto.ParticipationRequestDto;
import ru.yandex.practicum.request.mapper.ParticipationRequestMapper;
import ru.yandex.practicum.request.model.ParticipationRequest;
import ru.yandex.practicum.request.repository.ParticipationRequestRepository;
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
    private final ParticipationRequestRepository participationRequestRepository;
    private final ParticipationRequestMapper participationRequestMapper;

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto dto) {

        log.info("Create new event by user={}, dto={}", userId, dto.toString());
        User user = checkAndGetUser(userId);

        Category category = checkAndGetCategory(dto.getCategory());

        Event event = eventMapper.fromDto(dto);
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(2L))) {
            log.error("Event date should start at least after 2 hours");
            throw new ConflictException("Event date should start at least after 2 hours");
        }
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
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest request) {

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

        if (request.getEventDate() != null) {
            if (request.getEventDate().isBefore(LocalDateTime.now().plusHours(2L))) {
                log.error("Event with id={} has incorrect eventDate={}", eventId, event.getEventDate());
                throw new BadRequestException("Event date must be at least 2 hours in the future");
            }
            event.setEventDate(request.getEventDate());
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
            if (request.getParticipantLimit() < 0) {
                log.error("Participant limit cannot be negative");
                throw new BadRequestException("Participant limit cannot be negative");
            }
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case SEND_TO_REVIEW:
                    event.setState(Event.EventState.PENDING);
                    break;
                case CANCEL_REVIEW:
                    event.setState(Event.EventState.CANCELED);
                    break;
                default:
                    log.error("Unknown event state action={}", request.getStateAction());
                    throw new ConflictException("Invalid state action");
            }
        }
        return eventMapper.toFullDto(eventRepository.save(event));
    }

    @Override
    public List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId) {

        log.info("Getting participation requests in event={} of user={}", eventId, userId);
        checkAndGetUser(userId);
        Event event = checkAndGetEvent(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            log.error("Only event initiator can view participation requests");
            throw new ConflictException("Access denied to get events participation requests");
        }

        List<ParticipationRequest> request = participationRequestRepository.findAllByEventIdAndRequesterId(eventId, userId);
        return participationRequestMapper.toDtoList(request);

    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest request) {

        log.info("Changing requests status by user={} in event={}", userId, eventId);
        User user = checkAndGetUser(userId);
        Event event = checkAndGetEvent(eventId);

        if (!event.getInitiator().getId().equals(user.getId())) {
            log.error("Only event initiator can change participation requests status");
            throw new ConflictException("Access denied to change participation requests status");
        }

        List<ParticipationRequest> participationRequests = participationRequestRepository
                .findAllByIdIn(request.getRequestIds());

        participationRequests.forEach(participationRequest -> {
            if (!participationRequest.getEvent().getId().equals(eventId)) {
                log.error("Request doesn't belong to this event");
                throw new BadRequestException("Request doesn't belong to this event");
            }
            if (!participationRequest.getStatus().equals(ParticipationRequest.RequestStatus.PENDING)) {
                log.error("Participation request status must be PENDING");
                throw new ConflictException("Request must have status PENDING");
            }
        });

        return switch (request.getStatus()) {
            case ParticipationRequest.RequestStatus.CONFIRMED ->
                    confirmParticipationRequests(event, participationRequests);
            case ParticipationRequest.RequestStatus.REJECTED -> rejectParticipationRequests(participationRequests);
            default -> {
                log.error("Unknown request status={}", request.getStatus());
                throw new BadRequestException("Unknown request status");
            }
        };

    }

    private EventRequestStatusUpdateResult confirmParticipationRequests(Event event, List<ParticipationRequest> requests) {
        int limit = event.getParticipantLimit();
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        if (limit != 0) {
            long confirmedRequestsCount = participationRequestRepository.countByEventIdAndStatus(event.getId(),
                    ParticipationRequest.RequestStatus.CONFIRMED);
            if (confirmedRequestsCount >= limit) {
                log.error("Events participant limit reached limit={}, confirmed participants={}", limit, confirmedRequestsCount);
                throw new ConflictException("Events participant limit reached");
            }
            if (requests.size() + confirmedRequestsCount > limit) {
                int available = (int) (limit - confirmedRequestsCount);
                List<ParticipationRequest> toConfirm = requests.subList(0, available);
                List<ParticipationRequest> toReject = requests.subList(available, requests.size());

                toConfirm.forEach(req -> req.setStatus(ParticipationRequest.RequestStatus.CONFIRMED));
                toReject.forEach(req -> req.setStatus(ParticipationRequest.RequestStatus.REJECTED));

                participationRequestRepository.saveAll(requests);
                result.setConfirmedRequests(participationRequestMapper.toDtoList(toConfirm));
                result.setRejectedRequests(participationRequestMapper.toDtoList(toReject));
                return result;
            }
        }
        requests.forEach(participationRequest -> {
            participationRequest.setStatus(ParticipationRequest.RequestStatus.CONFIRMED);
        });
        participationRequestRepository.saveAll(requests);
        result.setConfirmedRequests(participationRequestMapper.toDtoList(requests));
        result.setRejectedRequests(List.of());
        return result;
    }

    private EventRequestStatusUpdateResult rejectParticipationRequests(List<ParticipationRequest> requests) {

        requests.forEach(participationRequest -> {
            participationRequest.setStatus(ParticipationRequest.RequestStatus.REJECTED);
        });
        participationRequestRepository.saveAll(requests);
        return EventRequestStatusUpdateResult.builder()
                .rejectedRequests(participationRequestMapper.toDtoList(requests))
                .confirmedRequests(List.of())
                .build();
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
