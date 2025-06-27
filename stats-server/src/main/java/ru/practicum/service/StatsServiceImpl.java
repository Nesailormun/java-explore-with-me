package ru.practicum.service;

import org.springframework.stereotype.Service;
import ru.practicum.dto.CreateEndpointHitDto;
import ru.practicum.dto.ViewStats;
import ru.practicum.mapper.EndpointHitMapper;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    public StatsServiceImpl(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    @Override
    public void createHit(CreateEndpointHitDto createEndpointHitDto) {
        statsRepository.save(EndpointHitMapper.toEntity(createEndpointHitDto));
    }

    @Override
    public List<ViewStats> getAllStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        return List.of();
    }
}
