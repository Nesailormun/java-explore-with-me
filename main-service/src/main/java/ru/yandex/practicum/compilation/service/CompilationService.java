package ru.yandex.practicum.compilation.service;

import ru.yandex.practicum.compilation.dto.CompilationDto;
import ru.yandex.practicum.compilation.dto.NewCompilationDto;
import ru.yandex.practicum.compilation.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    CompilationDto createCompilation(NewCompilationDto newCompilationDto);

    CompilationDto updateCompilation(Long comId, UpdateCompilationRequest updateCompilationRequest);

    void deleteCompilation(Long id);

    CompilationDto getCompilation(Long id);

    List<CompilationDto> getCompilationByFilter(boolean pinned, int from, int size);
}
