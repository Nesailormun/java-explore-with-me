package ru.yandex.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.category.dto.CategoryDto;
import ru.yandex.practicum.category.dto.NewCategoryDto;
import ru.yandex.practicum.category.mapper.CategoryMapper;
import ru.yandex.practicum.category.model.Category;
import ru.yandex.practicum.category.repository.CategoryRepository;
import ru.yandex.practicum.exception.ConflictException;
import ru.yandex.practicum.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;
    private final CategoryMapper mapper;

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {

        repository.findByName(newCategoryDto.getName()).ifPresent(c -> {
            throw new ConflictException("Категория уже существует.");
        });

        Category saved = repository.save(mapper.toEntity(newCategoryDto));
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) {
        Category existing = repository.findById(id).orElseThrow(
                () -> new NotFoundException("Категория не найдена.")
        );

        if (!existing.getName().equals(categoryDto.getName()) &&
                repository.findByName(categoryDto.getName()).isPresent()) {
            throw new ConflictException("Категория уже существует.");
        }

        existing.setName(categoryDto.getName());
        return mapper.toDto(repository.save(existing));
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Категория не найдена.");
        }
        repository.deleteById(id);
    }

    @Override
    public List<CategoryDto> getAllCategories(int from, int size) {
        return repository.findAll(PageRequest.of(from / size, size)).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategory(Long id) {
        return repository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new NotFoundException("Категория не найдена."));
    }
}
