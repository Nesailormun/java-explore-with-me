package ru.yandex.practicum.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.ViewStats;
import ru.yandex.practicum.client.exception.StatsClientException;
import ru.practicum.dto.CreateEndpointHitDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StatsClient {
    private final RestTemplate restTemplate;
    private final String statsServerUrl;

    public StatsClient(@Value("${stats-server.url}") String statsServerUrl) {
        this.restTemplate = new RestTemplate();
        this.statsServerUrl = statsServerUrl;
        log.info("Инициализирован клиент для сервиса статистики. Базовый URL: {}", statsServerUrl);
    }

    public void saveEndpointHit(CreateEndpointHitDto hitDto) {
        log.info("Попытка сохранить информацию о просмотре: app={}, uri={}, ip={}",
                hitDto.getApp(), hitDto.getUri(), hitDto.getIp());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<CreateEndpointHitDto> request = new HttpEntity<>(hitDto, headers);

        try {
            restTemplate.postForEntity(
                    statsServerUrl + "/hit",
                    request,
                    Void.class
            );
        } catch (RestClientException e) {
            log.warn("Ошибка соединения с сервисом статистики при сохранении просмотра. {}", e.getMessage());
            throw new StatsClientException("Ошибка при сохранении просмотра.");
        }
    }

    public Long getEventViews(Long eventId) {
        log.info("Запрос статистики просмотров для события с ID: {}", eventId);
        try {
            ResponseEntity<Long> response = restTemplate.getForEntity(
                    statsServerUrl + "/stats/{eventId}",
                    Long.class,
                    eventId);
            return response.getBody();
        } catch (RestClientException e) {
            log.warn("Ошибка соединения с сервисом статистики при получении данных. {}", e.getMessage());
            throw new StatsClientException("Ошибка при получении статистики.");
        }
    }

    public Map<Long, Long> getViewsForEvents(List<Long> eventIds) {
        log.info("Запрос статистики просмотров для событий: {}", eventIds);

        // Формируем список URI для запроса
        List<String> uris = eventIds.stream()
                .map(id -> "/events/" + id)
                .toList();

        // Вычисляем разумный диапазон времени (например, последний год)
        LocalDateTime start = LocalDateTime.now().minusYears(1);
        LocalDateTime end = LocalDateTime.now();

        // Параметры запроса
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(statsServerUrl + "/stats")
                .queryParam("start", start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .queryParam("end", end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .queryParam("uris", String.join(",", uris))
                .queryParam("unique", true);

        try {
            ResponseEntity<List<ViewStats>> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<ViewStats>>() {}
            );

            // Преобразуем результат в Map<eventId, views>
            return Objects.requireNonNull(response.getBody()).stream()
                    .collect(Collectors.toMap(
                            stats -> Long.parseLong(stats.getUri().substring(stats.getUri().lastIndexOf("/") + 1)),
                            ViewStats::getHits
                    ));
        } catch (RestClientException e) {
            log.warn("Ошибка соединения с сервисом статистики: {}", e.getMessage());
            return eventIds.stream().collect(Collectors.toMap(id -> id, id -> 0L));
        }
    }

}
