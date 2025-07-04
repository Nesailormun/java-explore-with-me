package ru.practicum.mapper;

import ru.practicum.dto.CreateEndpointHitDto;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.model.EndpointHit;

public class EndpointHitMapper {

    public static EndpointHitDto toDto(EndpointHit endpoint) {

        return EndpointHitDto.builder()
                .id(endpoint.getId())
                .app(endpoint.getApp())
                .uri(endpoint.getUri())
                .ip(endpoint.getIp())
                .timestamp(endpoint.getTimestamp())
                .build();
    }

    public static EndpointHit toEntity(CreateEndpointHitDto endpointHitDto) {

        return EndpointHit.builder()
                .app(endpointHitDto.getApp())
                .uri(endpointHitDto.getUri())
                .ip(endpointHitDto.getIp())
                .timestamp(endpointHitDto.getTimestamp())
                .build();
    }
}
