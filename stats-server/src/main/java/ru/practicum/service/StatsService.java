package ru.practicum.service;

import ru.practicum.dto.CreateEndpointHitDto;
import ru.practicum.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {

   void createHit(CreateEndpointHitDto createEndpointHitDto);

    List<ViewStats> getAllStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
