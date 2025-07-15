package ru.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CreateEndpointHitDto;
import ru.practicum.dto.ViewStats;
import ru.practicum.mapper.EndpointHitMapper;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@Slf4j
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    public StatsServiceImpl(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    @Override
    public void createHit(CreateEndpointHitDto createEndpointHitDto) {
        log.info("Creating hit for {}", createEndpointHitDto.getUri());
        statsRepository.save(EndpointHitMapper.toEntity(createEndpointHitDto));
    }

    @Transactional(readOnly = true)
    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        log.info("Getting stats for {}", uris);
        if (uris == null || uris.isEmpty()) {
            return unique ?
                    statsRepository.findAllUniqueStats(start, end) :
                    statsRepository.findAllStats(start, end);
        }
        return unique ?
                statsRepository.findUniqueStatsInUris(start, end, uris) :
                statsRepository.findStatsInUris(start, end, uris);
    }

    @Override
    public Long getViewsCountByEventId(Long eventId) {
        log.info("Getting views count for event with id: {}", eventId);
        return statsRepository.countAllHitsByEventId(eventId);
    }
}

