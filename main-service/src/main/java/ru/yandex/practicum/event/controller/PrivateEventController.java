package ru.yandex.practicum.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.event.dto.EventFullDto;
import ru.yandex.practicum.event.dto.EventShortDto;
import ru.yandex.practicum.event.dto.NewEventDto;
import ru.yandex.practicum.event.dto.UpdateEventRequest;
import ru.yandex.practicum.event.service.PrivateEventService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events")
@Slf4j
public class PrivateEventController {

    private final PrivateEventService privateEventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable Long userId,
                               @Valid @RequestBody NewEventDto dto) {
        log.info("POST /users/{}/events, body: {}", userId, dto.toString());
        return privateEventService.createEvent(userId, dto);
    }

    @GetMapping
    public List<EventShortDto> getAllEvents(@PathVariable Long userId,
                                      @RequestParam(defaultValue = "0") int from,
                                      @RequestParam(defaultValue = "10") int size) {
        log.info("GET /users/{}/events, from: {}, size: {}", userId, from, size);
        return privateEventService.getUserEvents(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventById(@PathVariable Long userId,
                                @PathVariable Long eventId) {
        log.info("GET /users/{}/events/{}", userId, eventId);
        return privateEventService.getUserEvent(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long userId,
                               @PathVariable Long eventId,
                               @Valid @RequestBody UpdateEventRequest request) {
        log.info("PATCH /users/{}/events/{}, body: {}", userId, eventId, request.toString());
        return privateEventService.updateUserEvent(userId, eventId, request);
    }
}
