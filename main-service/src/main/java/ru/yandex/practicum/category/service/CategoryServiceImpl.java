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
import ru.yandex.practicum.event.repository.EventRepository;
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
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {

        log.info("Обработка запроса на добавление категории: {}", newCategoryDto.toString());
        repository.findByName(newCategoryDto.getName()).ifPresent(c -> {
            log.warn("Ошибка. Категория с именем {} уже существует.", newCategoryDto.getName());
            throw new ConflictException("Категория уже существует.");
        });
        Category saved = repository.save(mapper.toEntity(newCategoryDto));
        log.info("Категория успешно сохранена.");
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) {
        log.info("Обработка запроса на обновление категории с id={}", id);
        Category existing = repository.findById(id).orElseThrow(
                () -> {
                    log.warn("Ошибка. Категория c id={} не найдена", id);
                    return new NotFoundException("Категория не найдена.");
                });

        if (!existing.getName().equals(categoryDto.getName()) &&
                repository.findByName(categoryDto.getName()).isPresent()) {
            log.warn("Ошибка. Категория: {} уже существует.", existing.getName());
            throw new ConflictException("Категория уже существует");
        }

        existing.setName(categoryDto.getName());
        log.info("Обновление категории успешно выполнено");
        return mapper.toDto(repository.save(existing));
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        log.info("Запрос на удаление категории с id={}", id);
        if (!repository.existsById(id)) {
            log.info("Category with id={} is not found.", id);
            throw new NotFoundException("Категория не найдена.");
        }

        if (eventRepository.existsById(id)) {
            log.warn("The category is not empty");
            throw new ConflictException("The category is not empty");
        }
        repository.deleteById(id);
        log.info("Категория успешно удалена");
    }

    @Override
    public List<CategoryDto> getAllCategories(int from, int size) {
        log.info("Запрос на получение всех категорий с параметрами from={}, size={}", from, size);
        return repository.findAll(PageRequest.of(from / size, size)).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategory(Long id) {
        log.info("Запрос на получение категории с id={}", id);
        return repository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> {
                    log.warn("Ошибка. Категория с id={} не найдена", id);
                    return new NotFoundException("Категория не найдена.");
                });
    }
}
