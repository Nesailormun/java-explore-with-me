package ru.practicum.service;

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
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    public StatsServiceImpl(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    @Override
    public void createHit(CreateEndpointHitDto createEndpointHitDto) {
        statsRepository.save(EndpointHitMapper.toEntity(createEndpointHitDto));
    }

    @Transactional(readOnly = true)
    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {

        if (uris == null || uris.isEmpty()) {
            return unique ?
                    statsRepository.findAllUniqueStats(start, end) :
                    statsRepository.findAllStats(start, end);
        }
        return unique ?
                statsRepository.findUniqueStatsInUris(start, end, uris) :
                statsRepository.findStatsInUris(start, end, uris);
    }
}

