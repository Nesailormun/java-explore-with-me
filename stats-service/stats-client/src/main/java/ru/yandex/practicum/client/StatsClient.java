package ru.yandex.practicum.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.yandex.practicum.client.exception.StatsClientException;
import ru.practicum.dto.CreateEndpointHitDto;

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
}
