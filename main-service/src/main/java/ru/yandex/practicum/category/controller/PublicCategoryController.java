package ru.yandex.practicum.category.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.category.dto.CategoryDto;
import ru.yandex.practicum.category.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Slf4j
public class PublicCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryDto> getAll(@RequestParam(defaultValue = "0") int from,
                                    @RequestParam(defaultValue = "10") int size) {
        log.info("GET /categories with params from={}, size={}", from, size);
        return categoryService.getAllCategories(from, size);
    }

    @GetMapping("/{catId}")
    public CategoryDto getById(@PathVariable Long catId) {
        log.info("GET /categories/{} ", catId);
        return categoryService.getCategory(catId);
    }
}
