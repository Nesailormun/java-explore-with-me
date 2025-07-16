package ru.yandex.practicum.compilation.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.compilation.dto.CompilationDto;
import ru.yandex.practicum.compilation.dto.NewCompilationDto;
import ru.yandex.practicum.compilation.dto.UpdateCompilationRequest;
import ru.yandex.practicum.compilation.mapper.CompilationMapper;
import ru.yandex.practicum.compilation.model.Compilation;
import ru.yandex.practicum.compilation.repository.CompilationRepository;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.event.repository.EventRepository;
import ru.yandex.practicum.exception.NotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
@Slf4j
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;

    public CompilationServiceImpl(CompilationRepository compilationRepository, EventRepository eventRepository,
                                  CompilationMapper compilationMapper) {
        this.compilationRepository = compilationRepository;
        this.eventRepository = eventRepository;
        this.compilationMapper = compilationMapper;
    }

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        log.info("Creating new compilation: {}", newCompilationDto);
        Set<Event> events = new HashSet<>();
        Compilation compilation = new Compilation();

        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            events.addAll(eventRepository.findAllById(newCompilationDto.getEvents()));
        }
        compilation.setTitle(newCompilationDto.getTitle());
        compilation.setEvents(events);
        compilation.setPinned(newCompilationDto.getPinned() != null && newCompilationDto.getPinned());
        return compilationMapper.toDto(compilationRepository.save(compilation));
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        log.info("Updating compilation with id: {}, body: {}", compId, updateCompilationRequest);
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> {
                    log.warn("Compilation with id: {} not found", compId);
                    return new NotFoundException("Compilation with id: " + compId + " not found");
                });
        if (updateCompilationRequest.getEvents() != null) {
            Set<Event> events = new HashSet<>(eventRepository.findAllById(updateCompilationRequest.getEvents()));
            compilation.setEvents(events);
        }
        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }
        if (updateCompilationRequest.getTitle() != null) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }
        return compilationMapper.toDto(compilationRepository.save(compilation));
    }

    @Override
    public void deleteCompilation(Long id) {
        log.info("Deleting compilation with id: {}", id);
        if (!compilationRepository.existsById(id)) {
            log.warn("Compilation with id: {} not found", id);
            throw new NotFoundException("Compilation with id: " + id + " not found");
        }
        compilationRepository.deleteById(id);
        log.info("Compilation with id: {} deleted", id);
    }

    @Override
    public CompilationDto getCompilation(Long id) {
        log.info("Getting compilation with id: {}", id);
        Compilation compilation = compilationRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Compilation with id: {} not found", id);
                    return new NotFoundException("Compilation with id: " + id + " not found");
                });
        return compilationMapper.toDto(compilation);
    }

    @Override
    public List<CompilationDto> getCompilationByFilter(boolean pinned, int from, int size) {
        log.info("Getting compilations by filter with parameters: pinned={}, from={}, size={}", pinned, from, size);
        PageRequest request = PageRequest.of(from / size, size);
        List<Compilation> compilations = (pinned)
                ? compilationRepository.findAllByPinned(pinned, request)
                : compilationRepository.findAll(request).getContent();
        return compilations.stream()
                .map(compilationMapper::toDto)
                .toList();
    }
}
