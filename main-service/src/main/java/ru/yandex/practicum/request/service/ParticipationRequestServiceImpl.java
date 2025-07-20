package ru.yandex.practicum.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.event.repository.EventRepository;
import ru.yandex.practicum.exception.ConflictException;
import ru.yandex.practicum.exception.NotFoundException;
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
@Transactional
@Slf4j
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    private final ParticipationRequestRepository participationRequestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ParticipationRequestMapper participationRequestMapper;


    @Override
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {

        log.info("Creating request for user {} and event {}", userId, eventId);
        User requester = checkAndGetUser(userId);
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> {
                    log.error("Event {} not found", eventId);
                    return new NotFoundException(String.format("Event with id %s not found", eventId));
                }
        );

        if (participationRequestRepository.findByRequesterIdAndEventId(userId, eventId).isPresent()) {
            log.error("Participation request for user {} and event {} already exists", userId, eventId);
            throw new ConflictException("Participation request for user " + userId + " and event " + eventId
                    + " already exists");
        }

        if (event.getInitiator().getId().equals(userId)) {
            log.error("Initiator of event cannot create participation request");
            throw new ConflictException("Initiator of event cannot create participation request");
        }

        if (!event.getState().equals(Event.EventState.PUBLISHED)) {
            log.error("Event are not published. Cannot create participation request");
            throw new ConflictException("Event are not published. Cannot create participation request");
        }

        int limit = event.getParticipantLimit();
        if (limit != 0 && limit <= participationRequestRepository
                .countByEventIdAndStatus(eventId, ParticipationRequest.RequestStatus.CONFIRMED)) {
            log.error("The number of participants has reached the limit");
            throw new ConflictException("The number of participants has reached the limit");
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .event(event)
                .requester(requester)
                .created(LocalDateTime.now())
                .status((!event.getRequestModeration() || limit == 0)
                        ? ParticipationRequest.RequestStatus.CONFIRMED
                        : ParticipationRequest.RequestStatus.PENDING)
                .build();

        return participationRequestMapper.toDto(participationRequestRepository.save(request));

    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getUserRequests(Long userId) {

        log.info("Getting user with id={} requests", userId);
        checkAndGetUser(userId);
        return participationRequestMapper.toDtoList(participationRequestRepository.findAllByRequesterId(userId));
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {

        log.info("Cancelling request with id={} for user with id={}", requestId, userId);
        checkAndGetUser(userId);
        ParticipationRequest request = participationRequestRepository.findById(requestId).orElseThrow(
                () -> {
                    log.error("Request with id={} not found", requestId);
                    return new NotFoundException("Request with requestId=" + requestId + " not found");
                }
        );
        request.setStatus(ParticipationRequest.RequestStatus.CANCELED);
        return participationRequestMapper.toDto(participationRequestRepository.save(request));
    }

    private User checkAndGetUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> {
                    log.error("User with id {} not found", userId);
                    return new NotFoundException("User with id " + userId + " not found");
                }
        );
    }
}
