package ru.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CreateEndpointHitDto;
import ru.practicum.dto.ViewStats;
import ru.practicum.exception.DateTimeValidationException;
import ru.practicum.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void createHit(@RequestBody @Valid CreateEndpointHitDto hitDto) {
        log.info("Получен запрос на сохранение информации о просмотре: app={}, uri={}, ip={}.",
                hitDto.getApp(), hitDto.getUri(), hitDto.getIp());
        statsService.createHit(hitDto);
        log.debug("Информация о просмотре успешно сохранена.");
    }

    @GetMapping("/stats")
    public List<ViewStats> getStats(
            @RequestParam @NotNull @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @NotNull @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") boolean unique) {

        log.info("Получен запрос на получение статистики за период с {} по {}, uris={}, unique={}.",
                start, end, uris, unique);
        if (start.isAfter(end)) {
            log.warn("Ошибка валидации дат.");
            throw new DateTimeValidationException("Дата начала поиска должна быть раньше даты окончания поиска.");
        }

        return statsService.getStats(start, end, uris, unique);
    }

    @GetMapping("/stats")
    public Long getViewsCountByEventId(@RequestParam @Positive Long eventId) {
        log.info("GET /stats/{}.", eventId);
        return statsService.getViewsCountByEventId(eventId);
    }

}
